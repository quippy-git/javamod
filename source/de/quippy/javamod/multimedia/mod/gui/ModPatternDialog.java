/*
 * @(#) ModPatternDialog.java
 *
 * Created on 25.07.2020 by Daniel Becker
 * 
 *-----------------------------------------------------------------------
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *----------------------------------------------------------------------
 */
package de.quippy.javamod.multimedia.mod.gui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.text.Caret;

import de.quippy.javamod.multimedia.mod.loader.pattern.Pattern;
import de.quippy.javamod.system.CircularBuffer;
import de.quippy.javamod.system.Helpers;

/**
 * @author Daniel Becker
 * @since 25.07.2020
 */
public class ModPatternDialog extends JDialog implements ModUpdateListener
{
	private static final long serialVersionUID = 4511905120124137632L;
	
	/**
	 * This thread will update the pattern view and show the current pattern
	 * plus highlight the current row.
	 * We use a thread approach, because we do not want the mixing to be
	 * interrupted by a listener routine doing arbitrary things. This way that 
	 * is decoupled. Furthermore, we will synchronize to the time index send
	 * with each event - and that is done best in this local thread.
	 * 
	 * Whoever wants to be informed needs to implement the ModUpdateListener
	 * interface and register at BasicModMixer::registerUpdateListener
	 * 
	 * The linkage of ModPatternDialog and this songFollower is done in the
	 * ModContainer::createNewMixer. When a new mod mixer is created, we will
	 * first de-register the previous one and stop the songFollower thread - 
	 * but not earlier.  
	 * 
	 * In ModMixer::startPlayback we will toggle the fireUpdates-Flag in
	 * BasicModMixer to prevent updates fired when we do not want to get
	 * informed of any.
	 * 
	 * @author Daniel Becker
	 * @since 11.11.2023
	 */
	private class SongFollower extends Thread
	{
		private CircularBuffer<ModUpdateListener.InformationObject> buffer;
		private volatile boolean running;
		private volatile boolean hasStopped;
		private long additionalWait;
		private long lastTimeCode;

		public SongFollower()
		{
			super();
			buffer = new CircularBuffer<ModUpdateListener.InformationObject>(128);
			running = true;
			hasStopped = false;
			additionalWait = 0;
			lastTimeCode = 0;
			setName("InformerThread");
			setDaemon(true);
//			try { this.setPriority(Thread.MAX_PRIORITY); } catch (SecurityException ex) { /*NOOP*/ }
		}
		/**
		 * Add an event from outside
		 * @since 13.11.2023
		 * @param information
		 */
		public void push(final ModUpdateListener.InformationObject information)
		{
			buffer.push(information);
		}
		/**
		 * This will stop the thread gracefully and halt it. After this call
		 * the thread is gone!
		 * @since 13.11.2023
		 */
		public void stopMe()
		{
			running = false;
			buffer.flush();
			while (!hasStopped) try { Thread.sleep(10L); } catch (InterruptedException ex) { /*NOOP*/ }
		}
		/**
		 * Do everything that is needed to display a new pattern.
		 * @since 13.11.2023
		 * @param information
		 */
		private void displayPattern(final ModUpdateListener.InformationObject information)
		{
			if (ModPatternDialog.this.isVisible() && information!=null)
			{
				try
				{
					final int index = (int)((information.position >> 48)&0xFFFF);
					if (index!=currentIndex && index<buttonArrangement.length)
		            	fillWithArrangementIndex(index);
					
					final int row = (int)((information.position >> 16)&0xFFFF);
					final int patternIndex = arrangement[index];
					Pattern pattern = patterns[patternIndex];
					if (pattern!=null)
					{
						int lineLength = pattern.getPatternRowCharacterLength() + 1;
						int startIndex = row * lineLength;
						getTextView_PatternData().setCaretPosition(startIndex);
						getTextView_PatternData().moveCaretPosition(startIndex + lineLength);
					}
				}
				catch (Throwable ex)
				{
					//If anything happens here, it stays here.
				}
			}
		}
		public void run()
		{
			hasStopped=false;
			while (running)
			{
				// wait for the first event to appear
				while (buffer.isEmpty() && running) try { Thread.sleep(1L); } catch (InterruptedException ex) { /*NOOP*/ }
				if (!running) break; // if we got stopped meanwhile, let's drop out... 
				
				while (!buffer.isEmpty())
				{
					final long startNanoTime = System.nanoTime();

					ModUpdateListener.InformationObject information = buffer.pop();

					long nanoWait = ((information.timeCode - lastTimeCode) * 1000000L) - additionalWait;
					lastTimeCode = information.timeCode;

					if (nanoWait > 0) // are we far behind?!
						try { Thread.sleep(nanoWait/1000000L); } catch (InterruptedException ex) { /*NOOP*/ }
					else
					{
						nanoWait = 0;
						try { Thread.sleep(1L); } catch (InterruptedException ex) { /*NOOP*/ }
					}
					
					displayPattern(information);

					// if this was the last event in the queue, wait for the next one - typically this is a pattern delay...
					while (buffer.isEmpty() && running) try { Thread.sleep(1L); } catch (InterruptedException ex) { /*NOOP*/ }
					if (!running) break; // if we got stopped meanwhile, let's drop out... 

					additionalWait = System.nanoTime() - startNanoTime - nanoWait;
				}
			}
			hasStopped=true;
		}
	}

	// The UpdateListener Thread - to decouple whoever wants to get informed
	private SongFollower songFollower;

	private JScrollPane scrollPane_ArrangementData = null;
	private JPanel arrangementPanel = null;
	private JPanel fixedHightPanel = null;
	private JLabel labelArrangement = null;
	private JButton nextPatternButton = null;
	private JButton prevPatternButton = null;
	private JScrollPane scrollPane_PatternData = null;
//	private JTextPane textArea_PatternData= null;
	private JTextArea textArea_PatternData= null;
	private JToggleButton [] buttonArrangement;
	private ButtonGroup buttonGroup = null;

	private int [] arrangement;
	private Pattern [] patterns;
	private int currentIndex;
	
//	private static final char[] EOL_ARRAY = { '\n' };
//	private static Color NOTECOLOR = new Color(0x00, 0x33, 0xCC);
//	private static Color INSTRUMENTCOLOR = new Color(0x00, 0x99, 0xCC);
//	private static Color VOLUMECOLOR = new Color(0x00, 0x99, 0x33);
//	private static Color EFFECTCOLOR = new Color(0xCC, 0x00, 0xCC);
//	private AttributeSet foregroundAttSet;
//	private AttributeSet noteAttSet;
//	private AttributeSet instrumentAttSet;
//	private AttributeSet volumeAttSet;
//	private AttributeSet effectAttSet;
//
//	private class InternalDefaultStyleDocument extends DefaultStyledDocument
//	{
//		private static final long serialVersionUID = 8443419464715235236L;
//
//		public void processBatchUpdates(final int offs, final ArrayList<ElementSpec> batch) throws BadLocationException
//		{
//			ElementSpec[] inserts = new ElementSpec[batch.size()];
//			batch.toArray(inserts);
//
//			super.insert(offs, inserts);
//		}
//	}
//	private InternalDefaultStyleDocument EMPTY_DOCUMENT = null;

	/**
	 * Constructor for ModPatternDialog
	 */
	public ModPatternDialog()
	{
		super();
		initialize();
	}
	/**
	 * Constructor for ModPatternDialog
	 * @param owner
	 * @param modal
	 */
	public ModPatternDialog(JFrame owner, boolean modal)
	{
		super(owner, modal);
		initialize();
	}
	/**
	 * Constructor for ModPatternDialog
	 * @param owner
	 * @param modal
	 */
	public ModPatternDialog(JDialog owner, boolean modal)
	{
		super(owner, modal);
		initialize();
	}
	private void initialize()
	{
        final Container baseContentPane = getContentPane();
		baseContentPane.setLayout(new java.awt.GridBagLayout());
		
		baseContentPane.add(getFixedHightPanel(), 			Helpers.getGridBagConstraint(0, 0, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		baseContentPane.add(getScrollPane_PatternData(), 	Helpers.getGridBagConstraint(0, 1, 1, 0, java.awt.GridBagConstraints.BOTH, java.awt.GridBagConstraints.WEST, 1.0, 1.0));

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		addWindowListener(new java.awt.event.WindowAdapter()
		{
			@Override
			public void windowClosing(java.awt.event.WindowEvent e)
			{
				doClose();
			}
		});

		setName("Show mod pattern");
		setTitle("Show mod pattern");
		setResizable(true);
		setSize(640, 480);
		setPreferredSize(getSize());
        pack();
		setLocation(Helpers.getFrameCenteredLocation(this, getParent()));
	}
	/**
	 * push an event into the songFollower queue
	 * @param information
	 * @see de.quippy.javamod.multimedia.mod.gui.ModUpdateListener#getMixerInformation(de.quippy.javamod.multimedia.mod.gui.ModUpdateListener.InformationObject)
	 */
	public void getMixerInformation(final ModUpdateListener.InformationObject information)
	{
		if (songFollower!=null) songFollower.push(information);
	}
	/**
	 * Stop the thread
	 * @since 13.11.2023
	 */
	public void stopUpdateThread()
	{
		if (songFollower!=null)
		{
			songFollower.stopMe();
			songFollower = null;
		}
	}
	/**
	 * Create and start the Thread
	 * @since 13.11.2023
	 */
	public void startUpdateThread()
	{
		if (songFollower!=null) stopUpdateThread();
		songFollower = new SongFollower();
		songFollower.start();
	}
	private void doClose()
	{
		setVisible(false);
		dispose();
	}
	private JPanel getFixedHightPanel()
	{
		if (fixedHightPanel==null)
		{
			fixedHightPanel = new JPanel();
			fixedHightPanel.setLayout(new GridBagLayout());
			fixedHightPanel.add(getPrevPatternButton(), Helpers.getGridBagConstraint(0, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			fixedHightPanel.add(getNextPatternButton(), Helpers.getGridBagConstraint(1, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			fixedHightPanel.add(getScrollPane_ArrangementData(), Helpers.getGridBagConstraint(2, 0, 1, 0, java.awt.GridBagConstraints.BOTH, java.awt.GridBagConstraints.WEST, 1.0, 1.0));
			final FontMetrics metrics = fixedHightPanel.getFontMetrics(Helpers.getDialogFont());
			final Dimension d = new Dimension(metrics.charWidth('0'), metrics.getHeight()*4);
			//fixedHightPanel.setSize(d);
			fixedHightPanel.setMinimumSize(d);
			//fixedHightPanel.setMaximumSize(d);
			//fixedHightPanel.setPreferredSize(d);
		}
		return fixedHightPanel;
	}
	public JLabel getLabelArrangement()
	{
		if (labelArrangement==null)
		{
			labelArrangement = new JLabel();
			labelArrangement.setName("labelArrangement");
			labelArrangement.setText("Song arrangement:");
			labelArrangement.setFont(Helpers.getDialogFont());
		}
		return labelArrangement;
	}
	private JScrollPane getScrollPane_ArrangementData()
	{
		if (scrollPane_ArrangementData == null)
		{
			scrollPane_ArrangementData = new JScrollPane();
			scrollPane_ArrangementData.setName("scrollPane_ArrangementData");
			scrollPane_ArrangementData.setViewportView(getArrangementPanel());
		}
		return scrollPane_ArrangementData;
	}
	private JPanel getArrangementPanel()
	{
		if (arrangementPanel==null)
		{
			arrangementPanel = new JPanel();
			fillButtonsForArrangement();
		}
		return arrangementPanel;
	}
	private JScrollPane getScrollPane_PatternData()
	{
		if (scrollPane_PatternData == null)
		{
			scrollPane_PatternData = new JScrollPane();
			scrollPane_PatternData.setName("scrollPane_PatternData");
			scrollPane_PatternData.setViewportView(getTextView_PatternData());
		}
		return scrollPane_PatternData;
	}
//	private JTextPane getTextView_PatternData()
//	{
//		if (textArea_PatternData==null)
//		{
//			textArea_PatternData = new JTextPane(new InternalDefaultStyleDocument())
//			{
//				// Hack: make this TextPane not wrap at border!
//				private static final long serialVersionUID = 4656803550646960929L;
//
//				public boolean getScrollableTracksViewportWidth()
//				{
//					return getUI().getPreferredSize(this).width <= getParent().getSize().width;
//				}
//			};
//			textArea_PatternData.setName("modInfo_Instruments");
//			textArea_PatternData.setFont(Helpers.getTextAreaFont());
//			Caret caret = textArea_PatternData.getCaret();
//			if (caret!=null)
//			{
//				caret.setVisible(true);
//				caret.setSelectionVisible(true);
//			}
//
//			EMPTY_DOCUMENT = new InternalDefaultStyleDocument();
//	        StyleContext sc = StyleContext.getDefaultStyleContext();
//	        foregroundAttSet = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, textArea_PatternData.getForeground());
//    		noteAttSet = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, NOTECOLOR);
//	        instrumentAttSet = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, INSTRUMENTCOLOR);
//	        volumeAttSet = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, VOLUMECOLOR);
//	        effectAttSet = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, EFFECTCOLOR);
//		}
//		return textArea_PatternData;
//	}
	private JTextArea getTextView_PatternData()
	{
		if (textArea_PatternData==null)
		{
			textArea_PatternData = new JTextArea();
			textArea_PatternData.setName("modInfo_Instruments");
			textArea_PatternData.setEditable(false);
			textArea_PatternData.setFont(Helpers.getTextAreaFont());
			Caret caret = textArea_PatternData.getCaret();
			if (caret!=null)
			{
				caret.setVisible(true);
				caret.setSelectionVisible(true);
			}
		}
		return textArea_PatternData;
	}
	private void fillButtonsForArrangement()
	{
		final int length = (arrangement==null)?256:arrangement.length;

		getArrangementPanel().removeAll();
		GridLayout gbl = new GridLayout(1, length);
		gbl.setHgap(0);
		getArrangementPanel().setLayout(gbl);
		buttonGroup = new ButtonGroup();
			
		buttonArrangement = new JToggleButton[length];
		for (int i=0; i<length; i++)
		{
			buttonArrangement[i] = createButtonForIndex(i, (arrangement==null)?-1:arrangement[i]);
			buttonGroup.add(buttonArrangement[i]);
			getArrangementPanel().add(buttonArrangement[i]);
		}
		getArrangementPanel().validate();
		getScrollPane_ArrangementData().getHorizontalScrollBar().setValue(0);
		getScrollPane_ArrangementData().getVerticalScrollBar().setValue(0);
		getScrollPane_ArrangementData().updateUI();
	}
	private JToggleButton createButtonForIndex(final int index, final int arrangementIndex)
	{
		JToggleButton newButton = new JToggleButton();
		newButton.setName("ArrangementButton_" + index);
		newButton.setText((arrangementIndex>-1)?Integer.toString(arrangementIndex):"--");
		newButton.setFont(Helpers.getDialogFont());
		newButton.setToolTipText("Show pattern " + arrangementIndex + " of arrangement index " + index);
		if (arrangementIndex>-1)
		{
			newButton.addActionListener(new ActionListener()
	        {
				@Override
	            public void actionPerformed(ActionEvent evt)
	            {
	            	fillWithArrangementIndex(index);
	            }
	        });
		}
		return newButton;
	}
	private JButton getPrevPatternButton()
	{
		if (prevPatternButton==null)
		{
			prevPatternButton = new JButton();
			prevPatternButton.setName("prevPatternButton");
			prevPatternButton.setText("<<");
			prevPatternButton.setFont(Helpers.getDialogFont());
			prevPatternButton.setToolTipText("Show previous pattern");
			prevPatternButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					if (arrangement!=null && currentIndex>0)
		            	fillWithArrangementIndex(currentIndex-1);
				}
			});
		}
		return prevPatternButton;
	}
	private JButton getNextPatternButton()
	{
		if (nextPatternButton==null)
		{
			nextPatternButton = new JButton();
			nextPatternButton.setName("nextPatternButton");
			nextPatternButton.setText(">>");
			nextPatternButton.setFont(Helpers.getDialogFont());
			nextPatternButton.setToolTipText("Show previous pattern");
			nextPatternButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					if (arrangement!=null && currentIndex<(arrangement.length-1))
		            	fillWithArrangementIndex(currentIndex+1);
				}
			});
		}
		return nextPatternButton;
	}
//	private void fillWithArrangementIndex(final int index)
//	{
//		if (arrangement!=null)
//		{
//			buttonArrangement[currentIndex = index].setSelected(true);
//			InternalDefaultStyleDocument doc = (InternalDefaultStyleDocument)getTextView_PatternData().getDocument(); 
//			getTextView_PatternData().setDocument(EMPTY_DOCUMENT);
//			try { doc.remove(0, doc.getLength()); } catch (Throwable ex) { /*NOOP */ }
//			final int patternIndex = arrangement[currentIndex];
//			Pattern pattern = patterns[patternIndex];
//			if (pattern!=null)
//			{
//				PatternRow [] patternRows = pattern.getPatternRows();
//				if (patternRows!=null)
//				{
//					// Somehow this is upside down after batch insert...
//					for (int row=patternRows.length-1; row>=0; row--)
//					{
//						PatternElement [] patternElements = patternRows[row].getPatternElements();
//						if (patternElements!=null)
//						{
//							final ArrayList<ElementSpec> elementSpecs = new ArrayList<ElementSpec>();
//
//							String line = patternRows[row].toString();
//
//							elementSpecs.add(new ElementSpec(foregroundAttSet, ElementSpec.ContentType, (ModConstants.getAsHex(row, 2) + " |").toCharArray(), 0, 4));
//							for (int c=0; c<patternElements.length; c++)
//							{
//								int channelOffset = c*16;
//								elementSpecs.add(new ElementSpec(foregroundAttSet, ElementSpec.ContentType, line.substring(channelOffset, channelOffset = (channelOffset + 1)).toCharArray(), 0, 1));
//								elementSpecs.add(new ElementSpec(noteAttSet, ElementSpec.ContentType, line.substring(channelOffset, channelOffset = (channelOffset + 3)).toCharArray(), 0, 3));
//								elementSpecs.add(new ElementSpec(foregroundAttSet, ElementSpec.ContentType, line.substring(channelOffset, channelOffset = (channelOffset + 1)).toCharArray(), 0, 1));
//								elementSpecs.add(new ElementSpec(instrumentAttSet, ElementSpec.ContentType, line.substring(channelOffset, channelOffset = (channelOffset + 2)).toCharArray(), 0, 2));
//								elementSpecs.add(new ElementSpec(volumeAttSet, ElementSpec.ContentType, line.substring(channelOffset, channelOffset = (channelOffset + 3)).toCharArray(), 0, 3));
//								elementSpecs.add(new ElementSpec(foregroundAttSet, ElementSpec.ContentType, line.substring(channelOffset, channelOffset = (channelOffset + 1)).toCharArray(), 0, 1));
//								elementSpecs.add(new ElementSpec(effectAttSet, ElementSpec.ContentType, line.substring(channelOffset, channelOffset = (channelOffset + 3)).toCharArray(), 0, 3));
//								elementSpecs.add(new ElementSpec(foregroundAttSet, ElementSpec.ContentType, line.substring(channelOffset, channelOffset = (channelOffset + 2)).toCharArray(), 0, 2));
//							}
//							elementSpecs.add(new ElementSpec(foregroundAttSet, ElementSpec.ContentType, EOL_ARRAY, 0, 1));
//							Element paragraph = doc.getParagraphElement(0);
//							AttributeSet pattr = paragraph.getAttributes();
//							elementSpecs.add(new ElementSpec(null, ElementSpec.EndTagType));
//							elementSpecs.add(new ElementSpec(pattr, ElementSpec.StartTagType));
//							
//							try
//							{
//								doc.processBatchUpdates(0, elementSpecs);
//							}
//							catch (Throwable ex)
//							{
//								//NOOP
//							}
//						}
//					}
//				}
//			}
//			getTextView_PatternData().setDocument(doc);
//			getTextView_PatternData().setCaretPosition(0);
//			getTextView_PatternData().moveCaretPosition(0);
//		}
//	}
	private void fillWithArrangementIndex(final int index)
	{
		if (arrangement!=null)
		{
			buttonArrangement[currentIndex = index].setSelected(true);
			int patternIndex = arrangement[index];
			Pattern pattern = patterns[patternIndex];
			if (pattern!=null)
			{
				getTextView_PatternData().setText(pattern.toString());
				getTextView_PatternData().setCaretPosition(0);
				getTextView_PatternData().moveCaretPosition(0);
			}
		}
	}
	public void fillWithPatternArray(final int songLength, final int [] arrangement, final Pattern [] patterns)
	{
		this.arrangement = new int [songLength];
		if (patterns==null)
		{
			getTextView_PatternData().setText(Helpers.EMPTY_STING);
		}
		else
		{
			for (int i=0; i<songLength; i++) this.arrangement[i] = arrangement[i];
			this.patterns = patterns;
		}
		fillButtonsForArrangement();
		if (buttonArrangement!=null && buttonArrangement.length>0 && buttonArrangement[0]!=null)
			fillWithArrangementIndex(0);
	}
}
