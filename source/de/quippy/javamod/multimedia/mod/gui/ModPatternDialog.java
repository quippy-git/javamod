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

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;

import de.quippy.javamod.multimedia.mod.ModConstants;
import de.quippy.javamod.multimedia.mod.ModInfoPanel;
import de.quippy.javamod.multimedia.mod.loader.pattern.Pattern;
import de.quippy.javamod.multimedia.mod.loader.pattern.PatternContainer;
import de.quippy.javamod.multimedia.mod.loader.pattern.PatternElement;
import de.quippy.javamod.multimedia.mod.mixer.BasicModMixer;
import de.quippy.javamod.system.CircularBuffer;
import de.quippy.javamod.system.Helpers;

/**
 * @author Daniel Becker
 * @since 25.07.2020
 */
public class ModPatternDialog extends JDialog implements ModUpdateListener
{
	private static final long serialVersionUID = 4511905120124137632L;
	private static final JLabel EMPTY_LABLE_ROW = new JLabel(" "); // dirty hack... And it must have contend - otherwise it does not work
	private static final GridBagConstraints EMPTY_LABEL_CONSTRAINT_ROW = Helpers.getGridBagConstraint(0, 0, 1, 0, java.awt.GridBagConstraints.VERTICAL, java.awt.GridBagConstraints.NORTHWEST, 0.0, 1.0, Helpers.NULL_INSETS);
	private static final JLabel EMPTY_LABLE_CHANNEL = new JLabel(" "); // dirty hack... And it must have contend - otherwise it does not work
	private static final GridBagConstraints EMPTY_LABEL_CONSTRAINT_CHANNEL = Helpers.getGridBagConstraint(0, 0, 3, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0, Helpers.NULL_INSETS);
	
	private static final Color EVENLIGHTERGRAY = new Color(0xf2, 0xf2, 0xf2);
	private static final Color LIGHTESTGRAY = new Color(0xe6, 0xe6, 0xe6);
	private static final String PEEK_METER_BUTTON_BACKGROUND = "#E6E6E6"; // invisible peek meters
	private static final Color LIGHTERGRAY = new Color(0xcc, 0xcc, 0xcc);

	private static final int SOLOCHANNEL = 1;
	private static final int TOGGLEMUTE = 2;
	private static final int RESET = 3;
	
	/**
	 * This small caret replacement of DefaultCaret will prevent horizontal scrolling.
	 * This way also large patterns can be followed, vertical scrolling is still done
	 * automatically
	 * @author Daniel Becker
	 * @since 24.11.2023
	 */
	private class MyCaret extends DefaultCaret
	{
		private static final long serialVersionUID = -7313591346180933137L;
		private boolean doScrolling;

		public MyCaret()
		{
			super();
			doScrolling = true;
		}

		/**
		 * @param e
		 * @see javax.swing.text.DefaultCaret#focusLost(java.awt.event.FocusEvent)
		 */
		@Override
		public void focusLost(FocusEvent e)
		{
			super.focusLost(e);
			setSelectionVisible(true);
		}

		/**
		 * @param e
		 * @see javax.swing.text.DefaultCaret#positionCaret(java.awt.event.MouseEvent)
		 */
		@Override
		protected void positionCaret(MouseEvent e)
		{
			// just do nothing! User is not allowed to mark anything in this pattern
		}

		/**
		 * @param e
		 * @see javax.swing.text.DefaultCaret#moveCaret(java.awt.event.MouseEvent)
		 */
		@Override
		protected void moveCaret(MouseEvent e)
		{
			// just do nothing! User is not allowed to mark anything in this pattern
		}

		/**
		 * @param nloc
		 * @see javax.swing.text.DefaultCaret#adjustVisibility(java.awt.Rectangle)
		 */
		@Override
		protected void adjustVisibility(Rectangle nloc)
		{
	        if (doScrolling)
	        {
				final JTextComponent component = this.getComponent();
		        if (component!=null)
		        {
			        final Rectangle rect = component.getVisibleRect();
			        nloc.x = rect.x; // just never change x, only y... :)
					super.adjustVisibility(nloc);
		        }
	        }
		}
		public void doScrolling(final boolean newValue)
		{
			doScrolling = newValue;
		}
//		public boolean getDoScrolling()
//		{
//			return doScrolling;
//		}
	}
	
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
		private static final int INITIAL_SIZE = 0x1000; // 64 channel with 750ms sound buffer needs a push buffer of approx. 0xF00 size.
//		private static final int GROW_BY_SIZE = 0x100;
		private CircularBuffer<TimedInformation> buffer;
		
		private volatile boolean running;
		private volatile boolean hasStopped;
		private volatile boolean updating;
		private volatile boolean paused;
		private volatile boolean isPaused;
		private volatile boolean drain;

		public SongFollower()
		{
			super();
			buffer = new CircularBuffer<TimedInformation>(INITIAL_SIZE); //64 Channel mod has already possibly 64 + 1 Events per row - so give us some room!
			running = true;
			hasStopped = false;
			updating = false;
			paused = isPaused = false;
			drain = false;
			setName("InformerThread");
			setDaemon(true);
//			try { this.setPriority(Thread.MAX_PRIORITY); } catch (SecurityException ex) { /*NOOP*/ }
		}
		/**
		 * Add an event from outside
		 * @since 13.11.2023
		 * @param information
		 */
		public void push(final TimedInformation information)
		{
			while (drain) try { Thread.sleep(10L); } catch (InterruptedException ex) { /*NOOP*/ }
			if (running)
			{
				//if (buffer.isFull()) buffer.growBy(GROW_BY_SIZE); // we do not want to grow as that is not thread safe. If events cannot be pushed, forget them!
				buffer.push(information);
			}
		}
		/**
		 * Invalidate all events in the queue
		 * @since 24.11.2023
		 */
		public void flush()
		{
			buffer.flush();
			while (updating) try { Thread.sleep(10L); } catch (InterruptedException ex) { /*NOOP*/ }
		}
		/**
		 * Will halt adding of new events and deliver / drain all remains
		 * in buffer. Method blocks, till all left over events are gone.
		 * This will also block the push method, till all is delivered.
		 * @since 29.11.2023
		 */
		public void drain()
		{
			drain = true;
			while (!buffer.isEmpty()) try { Thread.sleep(10L); } catch (InterruptedException ex) { /*NOOP*/ }
			drain = false;
		}
		/**
		 * This will stop the thread gracefully and halt it. After this call
		 * the thread is gone!
		 * @since 13.11.2023
		 */
		public void stopMe()
		{
			running = false;
			flush();
			while (!hasStopped) try { Thread.sleep(10L); } catch (InterruptedException ex) { /*NOOP*/ }
		}
		/**
		 * Will pause/unpause the thread
		 * @since 28.11.2023
		 * @param isPaused
		 */
		public void pause(final boolean doPause)
		{
			paused = doPause;
			while (paused!=isPaused) try { Thread.sleep(10L); } catch (InterruptedException ex) { /*NOOP*/ }
		}
		public void run()
		{
			long additionalWait = 0;
			long lastTimeCode = 0;
			hasStopped=false;

			while (running)
			{
				// wait for the first event to appear
				while (buffer.isEmpty() && running) try { Thread.sleep(1L); } catch (InterruptedException ex) { /*NOOP*/ }
				if (!running) break; // if we got stopped meanwhile, let's drop out... 
				
				while (!buffer.isEmpty())
				{
					final long startNanoTime = System.nanoTime();

					TimedInformation information = buffer.peek(0);
					long nanoWait = ((information.timeCode - lastTimeCode) * 1000000L) - additionalWait;
					lastTimeCode = information.timeCode;
					if (nanoWait<=0)
						nanoWait = 0L;
					else
						try { Thread.sleep(nanoWait/1000000L); } catch (InterruptedException ex) { /*NOOP*/ }

					updating = true;
					while (!buffer.isEmpty() && ((TimedInformation)buffer.peek(0)).timeCode <= lastTimeCode)
					{
						information = buffer.pop();
						// if dialog is not visible, do not make any updates.
						if (ModPatternDialog.this.isVisible())
						{
							if (information instanceof PositionInformation)
								displayPattern((PositionInformation)information);
							else
							if (information instanceof PeekInformation)
								updateVolume(((PeekInformation)information).channel, ((PeekInformation)information).actPeekLeft, ((PeekInformation)information).actPeekRight);
						}
					}
					updating = false;

					// if this was the last event in the queue, wait for the next one - typically this is a pattern delay...
					while (buffer.isEmpty() && running) try { Thread.sleep(1L); } catch (InterruptedException ex) { /*NOOP*/ }

					if (paused) // if we should pause updates, wait here...
					{
						isPaused = true;
						while (paused && running) try { Thread.sleep(10L); } catch (InterruptedException ex) { /*NOOP*/ }
						isPaused = false;
					}
					if (!running) break; // if we got stopped meanwhile, let's drop out... 

					additionalWait = System.nanoTime() - startNanoTime - nanoWait;
				}
			}
			hasStopped=true;
		}
	}

	// The UpdateListener Thread - to decouple whoever wants to get informed
	private SongFollower songFollower;

	private JPanel topArrangementPanel = null;
	private JButton nextPatternButton = null;
	private JButton prevPatternButton = null;
	private JCheckBox followSongCheckBox = null;
	private JPanel arrangementPanel = null;
	private JScrollPane scrollPane_ArrangementData = null;
	private ButtonGroup buttonGroup = null;
	private JToggleButton [] buttonArrangement;
	
	private JTextArea textArea_PatternData= null;
	private JScrollPane scrollPane_PatternData = null;

	private JPanel channelHeadlinePanel = null;
	private JButton [] channelButtons = null;
	private JLabel [] effectLabels = null;
	private JLabel [] volEffectLabels = null;
    private JPopupMenu channelPopUp = null;
 	private JMenuItem popUpEntrySoloChannel = null;
 	private JMenuItem popUpEntryMuteChannel = null;
 	private JMenuItem popUpEntryUnMuteAll = null;
	
	private JPanel rowHeadlinePanel = null;
	private JLabel [] rowLabels = null;
	private GridBagConstraints [] rowLabelConstraints = null;
	private static final int START_ROWLABELS_AMOUNT = 0xFF; // let us create 256 row labels in advance. If that is not enough, let us create more
	private JPanel upperLeftCornerPanel = null;
	private JLabel patternNumberLabel = null;
	
	private Dimension PATTERNINDEX_BUTTON = null;
	private Dimension PATTERNINDEX_SIZE = null;
	private Dimension PATTERNBUTTON_SIZE = null;
	private Dimension CHANNELBUTTON_SIZE = null;

 	private BasicModMixer currentMixer;

 	private int [] arrangement = null;
	private PatternContainer patternContainer = null;
	private int currentIndex;
	private int selectedChannelNumber = -1; // Popup on which channel?

	private String peekMeterColorStrings[];
	
	@SuppressWarnings("unused")
	private ModInfoPanel myModInfoPanel;

	/**
	 * Constructor for ModPatternDialog
	 */
	public ModPatternDialog(ModInfoPanel infoPanel)
	{
		super();
		myModInfoPanel = infoPanel;
		initialize();
	}
	/**
	 * Constructor for ModPatternDialog
	 * @param owner
	 * @param modal
	 */
	public ModPatternDialog(ModInfoPanel infoPanel, JFrame owner, boolean modal)
	{
		super(owner, modal);
		myModInfoPanel = infoPanel;
		initialize();
	}
	/**
	 * Constructor for ModPatternDialog
	 * @param owner
	 * @param modal
	 */
	public ModPatternDialog(ModInfoPanel infoPanel, JDialog owner, boolean modal)
	{
		super(owner, modal);
		myModInfoPanel = infoPanel;
		initialize();
	}
	private void initialize()
	{
		// Let's first create the normal and darker color for the meters
		peekMeterColorStrings = new String[16];
		for (int i=0; i<8; i++)
		{
			final int r = i*255/8;
			final int g = 255-r;
			peekMeterColorStrings[i]="#"+ModConstants.getAsHex(r, 2)+ModConstants.getAsHex(g, 2)+"00";
			peekMeterColorStrings[i+8]=PEEK_METER_BUTTON_BACKGROUND; //="#"+ModConstants.getAsHex(r>>1, 2)+ModConstants.getAsHex(g>>1, 2)+"00";
		}

		final Container baseContentPane = getContentPane();

		final FontMetrics textAreaMetrics = baseContentPane.getFontMetrics(Helpers.getTextAreaFont());
		PATTERNBUTTON_SIZE = new Dimension(textAreaMetrics.charWidth('0') * Pattern.LINEINDEX_LENGTH, textAreaMetrics.getHeight());
		CHANNELBUTTON_SIZE = new Dimension(textAreaMetrics.charWidth('0') * Pattern.ROW_LENGTH, textAreaMetrics.getHeight());
		
		final FontMetrics dialogMetrics = baseContentPane.getFontMetrics(Helpers.getDialogFont());
		PATTERNINDEX_BUTTON = new Dimension(dialogMetrics.charWidth('0') * 6, dialogMetrics.getHeight() * 2);
		PATTERNINDEX_SIZE = new Dimension(dialogMetrics.charWidth('0'), dialogMetrics.getHeight() * 4);

		createRowLabels(START_ROWLABELS_AMOUNT);
		
		baseContentPane.setLayout(new java.awt.GridBagLayout());
		
		baseContentPane.add(getTopArrangementPanel(), 		Helpers.getGridBagConstraint(0, 0, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
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
	private void doClose()
	{
		setVisible(false);
		dispose();
	}
	private JPanel getTopArrangementPanel()
	{
		if (topArrangementPanel==null)
		{
			topArrangementPanel = new JPanel();
			topArrangementPanel.setLayout(new GridBagLayout());
			topArrangementPanel.add(getPrevPatternButton(),			Helpers.getGridBagConstraint(0, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			topArrangementPanel.add(getNextPatternButton(),			Helpers.getGridBagConstraint(1, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			topArrangementPanel.add(getFollowSongCheckBox(),		Helpers.getGridBagConstraint(0, 1, 1, 2, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.CENTER, 0.0, 0.0));
			topArrangementPanel.add(getScrollPane_ArrangementData(),Helpers.getGridBagConstraint(2, 0, 2, 1, java.awt.GridBagConstraints.BOTH, java.awt.GridBagConstraints.WEST, 1.0, 1.0));
			topArrangementPanel.setMinimumSize(PATTERNINDEX_SIZE);
		}
		return topArrangementPanel;
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
	private JCheckBox getFollowSongCheckBox()
	{
		if (followSongCheckBox==null)
		{
			followSongCheckBox = new JCheckBox();
			followSongCheckBox.setName("followSongCheckBox");
			followSongCheckBox.setText("Follow song");
			followSongCheckBox.setFont(Helpers.getDialogFont());
			followSongCheckBox.setToolTipText("Control whether to follow the song or not");
			followSongCheckBox.setSelected(true);
			// When "follow Song" is not selected, make the caret a bit lighter in color
			followSongCheckBox.addItemListener(new ItemListener()
			{
				@Override
				public void itemStateChanged(ItemEvent e)
				{
					if (e.getStateChange()==ItemEvent.SELECTED || e.getStateChange()==ItemEvent.DESELECTED)
					{
						if (!getFollowSongCheckBox().isSelected())
							getTextView_PatternData().setSelectionColor(EVENLIGHTERGRAY);
						else
							getTextView_PatternData().setSelectionColor(LIGHTERGRAY);
					}
				}
			});
		}
		return followSongCheckBox;
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
			arrangementPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
			fillButtonsForArrangement();
		}
		return arrangementPanel;
	}
	private JToggleButton createButtonForIndex(final int index, final int arrangementIndex, final Dimension size)
	{
		JToggleButton newButton = new JToggleButton();
		newButton.setName("ArrangementButton_" + index);
		newButton.setText((arrangementIndex>-1)?ModConstants.getAsHex(arrangementIndex, 2):"--");
		newButton.setFont(Helpers.getDialogFont());
		newButton.setToolTipText("Show pattern " + arrangementIndex + " of arrangement index " + index);
		newButton.setMargin(Helpers.NULL_INSETS);
		newButton.setSize(size);
		newButton.setMinimumSize(size);
		newButton.setMaximumSize(size);
		newButton.setPreferredSize(size);
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
	private void fillButtonsForArrangement()
	{
		final int length = (arrangement==null)?25:arrangement.length;

		getArrangementPanel().removeAll();
		buttonGroup = new ButtonGroup();
			
		buttonArrangement = new JToggleButton[length];
		for (int i=0; i<length; i++)
		{
			buttonArrangement[i] = createButtonForIndex(i, (arrangement==null)?-1:arrangement[i], PATTERNINDEX_BUTTON);
			buttonGroup.add(buttonArrangement[i]);
			getArrangementPanel().add(buttonArrangement[i], i);
		}

		// Set scroll bar increments to the width/height of one button.
		final JToggleButton firstButton = buttonArrangement[0];
		getScrollPane_ArrangementData().getHorizontalScrollBar().setUnitIncrement(firstButton.getWidth());
		getScrollPane_ArrangementData().getVerticalScrollBar().setUnitIncrement(firstButton.getHeight()); // BTW: we shall never see you!!

		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{				
				getScrollPane_ArrangementData().getHorizontalScrollBar().setValue(0);
				getScrollPane_ArrangementData().getVerticalScrollBar().setValue(0);
			}
		});
	}
	private JScrollPane getScrollPane_PatternData()
	{
		if (scrollPane_PatternData == null)
		{
			scrollPane_PatternData = new JScrollPane();
			scrollPane_PatternData.setName("scrollPane_PatternData");
			scrollPane_PatternData.setViewportView(getTextView_PatternData());
			scrollPane_PatternData.setColumnHeaderView(getChannelHeadlinePanel());
			scrollPane_PatternData.setRowHeaderView(getRowHeadlinePanel());
			scrollPane_PatternData.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, getUpperLeftCornerPanel());
			scrollPane_PatternData.setDoubleBuffered(true); //Yo, u better be smooth!
		}
		return scrollPane_PatternData;
	}
	private JTextArea getTextView_PatternData()
	{
		if (textArea_PatternData==null)
		{
			textArea_PatternData = new JTextArea();
			textArea_PatternData.setName("modInfo_PatternData");
			textArea_PatternData.setEditable(false); // no editing
			textArea_PatternData.setFont(Helpers.getTextAreaFont());
			MyCaret caret = new MyCaret(); // and add our special own caret for following
			textArea_PatternData.setCaret(caret);
			caret.setVisible(false); // no cursor
			// As in some cases, when the textbox gains focus, the cursor appears nevertheless, we just make it invisible...
			textArea_PatternData.setCaretColor(textArea_PatternData.getBackground());
			caret.setSelectionVisible(true); // but selection is visible
			textArea_PatternData.setSelectionColor(LIGHTERGRAY); // and has a light gray
			
		}
		return textArea_PatternData;
	}
	/**
	 * Do everything that is needed to display a new pattern.
	 * @since 13.11.2023
	 * @param information
	 */
	private void displayPattern(final PositionInformation information)
	{
		if (information!=null)
		{
			final boolean followSong = getFollowSongCheckBox().isSelected();
			final int index = information.patternIndex;
			if (index!=currentIndex && index<buttonArrangement.length && followSong)
				fillWithArrangementIndex(index); // this is already added to the gui event queue

			final int row = information.patternRow;
			final int patternIndex = arrangement[index];
			final Pattern pattern = patternContainer.getPattern(patternIndex);
			if (pattern!=null)
			{
				EventQueue.invokeLater(new Runnable()
				{
					public void run()
					{				
						try
						{
							final int lineLength = pattern.getPatternRowCharacterLength(false) + 1;
							final int startIndex = row * lineLength;
							final int endIndex = startIndex + lineLength;

							((MyCaret)getTextView_PatternData().getCaret()).doScrolling(followSong);
							getTextView_PatternData().setCaretPosition(startIndex);
							getTextView_PatternData().moveCaretPosition((index==currentIndex)?endIndex:startIndex);
							
							if (currentMixer!=null && effectLabels!=null)
							{
								final int channels = pattern.getChannels();
								for (int c=0; c<channels; c++)
								{
									final PatternElement element = pattern.getPatternElement(row, c);
									effectLabels[c].setText(currentMixer.getEffectName(element.getEffekt(), element.getEffektOp()));
									volEffectLabels[c].setText(currentMixer.getVolEffectName(element.getVolumeEffekt(), element.getVolumeEffektOp()));
								}
							}
						}
						catch (Throwable ex)
						{
							// Keep it!
						}
					}
				});
			}
		}
	}
    private JMenuItem getPopUpEntrySoloChannel()
    {
        if (popUpEntrySoloChannel == null)
        {
        	popUpEntrySoloChannel = new javax.swing.JMenuItem();
        	popUpEntrySoloChannel.setName("popUpEntrySoloChannel");
        	popUpEntrySoloChannel.setText("Solo channel");
        	popUpEntrySoloChannel.addActionListener(
				new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						doMute(SOLOCHANNEL, selectedChannelNumber);
					}
				});
        }
        return popUpEntrySoloChannel;
    }
    private JMenuItem getPopUpEntryMuteChannel()
    {
        if (popUpEntryMuteChannel == null)
        {
        	popUpEntryMuteChannel = new javax.swing.JMenuItem();
        	popUpEntryMuteChannel.setName("popUpEntryMuteChannel");
        	popUpEntryMuteChannel.setText("Mute channel");
        	popUpEntryMuteChannel.addActionListener(
				new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						doMute(TOGGLEMUTE, selectedChannelNumber);
					}
				});
        }
        return popUpEntryMuteChannel;
    }
    private JMenuItem getPopUpEntryUnMuteAll()
    {
        if (popUpEntryUnMuteAll == null)
        {
        	popUpEntryUnMuteAll = new javax.swing.JMenuItem();
        	popUpEntryUnMuteAll.setName("popUpEntryUnMuteAll");
        	popUpEntryUnMuteAll.setText("Reset all channels");
        	popUpEntryUnMuteAll.addActionListener(
				new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						doMute(RESET, selectedChannelNumber);
					}
				});
        }
        return popUpEntryUnMuteAll;
    }
    private JPopupMenu getPopup()
    {
    	if (channelPopUp==null)
    	{
    		channelPopUp = new javax.swing.JPopupMenu();
    		channelPopUp.setName("channelPopUp");
    		channelPopUp.add(getPopUpEntryUnMuteAll());
    		channelPopUp.add(new javax.swing.JSeparator());
    		channelPopUp.add(getPopUpEntrySoloChannel());
    		channelPopUp.add(getPopUpEntryMuteChannel());
    	}
    	getPopUpEntryMuteChannel().setEnabled(currentMixer!=null);
    	getPopUpEntrySoloChannel().setEnabled(currentMixer!=null);
    	return channelPopUp;
    }
	private JPanel getUpperLeftCornerPanel()
	{
		if (upperLeftCornerPanel==null)
		{
			upperLeftCornerPanel = new JPanel(new GridBagLayout());
			upperLeftCornerPanel.setBackground(getTextView_PatternData().getBackground());
			upperLeftCornerPanel.add(getPatternNumberLabel(), Helpers.getGridBagConstraint(0, 0, 3, 1, java.awt.GridBagConstraints.BOTH, java.awt.GridBagConstraints.WEST, 1.0, 1.0, Helpers.NULL_INSETS));
		}
		return upperLeftCornerPanel;
	}
	private JLabel getPatternNumberLabel()
	{
		if (patternNumberLabel==null)
		{
			patternNumberLabel = new JLabel();
			patternNumberLabel.setName("patternNumberLabel");
			patternNumberLabel.setText(Helpers.EMPTY_STING);
			patternNumberLabel.setHorizontalAlignment(SwingConstants.CENTER);
			patternNumberLabel.setVerticalAlignment(SwingConstants.CENTER);
			patternNumberLabel.setFont(Helpers.getDialogFont());
			patternNumberLabel.setToolTipText("Current number of pattern");
			patternNumberLabel.setOpaque(true);
			patternNumberLabel.setBackground(LIGHTESTGRAY);
			patternNumberLabel.setBorder(null);
		}
		return patternNumberLabel;
	}
	private JPanel getChannelHeadlinePanel()
	{
		if (channelHeadlinePanel==null)
		{
			channelHeadlinePanel = new JPanel(new GridBagLayout());
			channelHeadlinePanel.setBackground(getTextView_PatternData().getBackground());
		}
		return channelHeadlinePanel;
	}
	/**
	 * @since 28.11.2023
	 * @param channel
	 * @param highLeft
	 * @param highRight
	 * @return
	 */
	private String createChannelName(final int channel, final int highLeft, final int highRight)
	{
		StringBuilder sb = new StringBuilder("<html>");
		for (int i=7; i>0; i--) sb.append("<font color=").append(peekMeterColorStrings[(i>highLeft)?i+8:i]).append(">(</font>");
		sb.append(' ').append(channel+1).append(' ');
		for (int i=0; i<8; i++) sb.append("<font color=").append(peekMeterColorStrings[(i<highRight)?i:i+8]).append(">)</font>");
		sb.append("</html>");
		return sb.toString();
	}
	/**
	 * @since 28.11.2023
	 * @param channelNumber
	 * @return
	 */
	private JButton createChannelButton(final int channelNumber)
	{
		final JButton channelButton = new JButton();
		channelButton.setName("Channel_"+Integer.toString(channelNumber));
		channelButton.setText(createChannelName(channelNumber, 0, 0));
		channelButton.setHorizontalAlignment(SwingConstants.CENTER);
		channelButton.setFont(Helpers.getDialogFont());
		channelButton.setToolTipText("Channel " + Integer.toString(channelNumber) + " mute/unmute");
		channelButton.setBackground(LIGHTESTGRAY);
		channelButton.setBorder(null);
		channelButton.setBorderPainted(false);
		channelButton.setMargin(Helpers.NULL_INSETS);
		channelButton.setSize(CHANNELBUTTON_SIZE);
		channelButton.setMinimumSize(CHANNELBUTTON_SIZE);
		channelButton.setMaximumSize(CHANNELBUTTON_SIZE);
		channelButton.setPreferredSize(CHANNELBUTTON_SIZE);
		channelButton.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					doMute(TOGGLEMUTE, channelNumber);
				}
			});
		channelButton.addMouseListener(new MouseAdapter()
		{
			public void mousePressed(MouseEvent e) 
			{
				if (e.isConsumed()) return;
				if (SwingUtilities.isRightMouseButton(e))
		        {
					selectedChannelNumber = channelNumber;
					getPopup().show(channelButton, e.getX(), e.getY());
					e.consume();
		        }
			}
		});
		return channelButton;
	}
	private JLabel createEffectLabel(final int channelNumber)
	{
		final JLabel effektLabel = new JLabel();
		effektLabel.setName("EffectLabel_"+Integer.toString(channelNumber));
		effektLabel.setText(Helpers.EMPTY_STING);
		effektLabel.setHorizontalAlignment(SwingConstants.LEFT);
		effektLabel.setFont(Helpers.getDialogFont());
		effektLabel.setToolTipText("Channel " + Integer.toString(channelNumber) + " current effect");
		effektLabel.setOpaque(true);
		effektLabel.setBackground(LIGHTESTGRAY);
		effektLabel.setBorder(null);
		effektLabel.setSize(CHANNELBUTTON_SIZE);
		effektLabel.setMinimumSize(CHANNELBUTTON_SIZE);
		effektLabel.setMaximumSize(CHANNELBUTTON_SIZE);
		effektLabel.setPreferredSize(CHANNELBUTTON_SIZE);
		return effektLabel;
	}
	private JLabel createVolEffectLabel(final int channelNumber)
	{
		final JLabel volEffectLabel = new JLabel();
		volEffectLabel.setName("VolEffectLabel_"+Integer.toString(channelNumber));
		volEffectLabel.setText(Helpers.EMPTY_STING);
		volEffectLabel.setHorizontalAlignment(SwingConstants.LEFT);
		volEffectLabel.setFont(Helpers.getDialogFont());
		volEffectLabel.setToolTipText("Channel " + Integer.toString(channelNumber) + " current volume effect");
		volEffectLabel.setOpaque(true);
		volEffectLabel.setBackground(LIGHTESTGRAY);
		volEffectLabel.setBorder(null);
		volEffectLabel.setSize(CHANNELBUTTON_SIZE);
		volEffectLabel.setMinimumSize(CHANNELBUTTON_SIZE);
		volEffectLabel.setMaximumSize(CHANNELBUTTON_SIZE);
		volEffectLabel.setPreferredSize(CHANNELBUTTON_SIZE);
		return volEffectLabel;
	}
	/**
	 * Create the buttons for the channels
	 * @since 27.11.2023
	 * @param channels
	 */
	private void createChannelButtons(final int channels)
	{
		channelButtons = new JButton[channels];
		effectLabels = new JLabel[channels];
		volEffectLabels = new JLabel[channels];
		
		for (int i=0; i<channels; i++)
		{
			channelButtons[i] = createChannelButton(i);
			effectLabels[i] = createEffectLabel(i);
			volEffectLabels[i] = createVolEffectLabel(i);
		}
	}
	/**
	 * Fill the panel with all channel buttons. As these do not change
	 * during one piece, we will create them once, not every pattern change
	 * @since 27.11.2023
	 * @param patternIndex
	 * @param channels
	 */
	private void fillButtonsForChannels(final int patternIndex, final int channels)
	{
		getChannelHeadlinePanel().removeAll();
		for (int i=0; i<channels; i++)
		{
			getChannelHeadlinePanel().add(channelButtons[i], Helpers.getGridBagConstraint(i, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0, Helpers.NULL_INSETS));
			getChannelHeadlinePanel().add(volEffectLabels[i], Helpers.getGridBagConstraint(i, 1, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0, Helpers.NULL_INSETS));
			getChannelHeadlinePanel().add(effectLabels[i], Helpers.getGridBagConstraint(i, 2, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0, Helpers.NULL_INSETS));
		}
		// Hack: to make the buttons stay and not get horizontally centered, we give the panel something to make bigger instead. 
		EMPTY_LABEL_CONSTRAINT_CHANNEL.gridx = channels;
		getChannelHeadlinePanel().add(EMPTY_LABLE_CHANNEL, EMPTY_LABEL_CONSTRAINT_CHANNEL);

		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{				
				getChannelHeadlinePanel().repaint();
			}
		});
	}
	/**
	 * Get the row headers for the JScrollPane, so we see the rows
	 * @since 27.11.2023
	 * @return
	 */
	private JPanel getRowHeadlinePanel()
	{
		if (rowHeadlinePanel==null)
		{
			rowHeadlinePanel = new JPanel(new GridBagLayout());
			rowHeadlinePanel.setBackground(getTextView_PatternData().getBackground());
		}
		return rowHeadlinePanel;
	}
	/**
	 * Create the label for that specific row.
	 * @since 27.11.2023
	 * @param rowNumber
	 * @return
	 */
	private JLabel createRowLabel(final int rowNumber)
	{
		final JLabel rowLabel = new JLabel();
		rowLabel.setName("Row_"+Integer.toString(rowNumber));
		rowLabel.setText(ModConstants.getAsHex(rowNumber, 2));
		rowLabel.setHorizontalAlignment(SwingConstants.CENTER);
		rowLabel.setFont(Helpers.getDialogFont());
		rowLabel.setToolTipText("Row " + Integer.toString(rowNumber));
		rowLabel.setOpaque(true);
		rowLabel.setBackground((rowNumber%4)==0?LIGHTERGRAY:LIGHTESTGRAY);
		rowLabel.setBorder(null);
		rowLabel.setSize(PATTERNBUTTON_SIZE);
		rowLabel.setMinimumSize(PATTERNBUTTON_SIZE);
		rowLabel.setMaximumSize(PATTERNBUTTON_SIZE);
		rowLabel.setPreferredSize(PATTERNBUTTON_SIZE);
		return rowLabel;
	}
	/**
	 * create an amount of labels in advance, so we afterwards only need
	 * to add the labels with their constraints respectively.
	 * We need a gridbag so the labels will be stacked - and not side by side.
	 * @since 27.11.2023
	 * @param maxRows
	 */
	private void createRowLabels(final int maxRows)
	{
		if (rowLabels!=null && rowLabels.length>=maxRows) return;
		
		int start = 0;
		if (rowLabels==null)
		{
			rowLabels = new JLabel[maxRows];
			rowLabelConstraints = new GridBagConstraints[maxRows];
		}
		else
		if (maxRows>rowLabels.length) // if we already created rowLabels, check if we have enough!
		{
			start = rowLabels.length;
			JLabel [] newRowLabels = new JLabel[maxRows];
			GridBagConstraints [] newRowLabelConStraints = new GridBagConstraints[maxRows];
			
			System.arraycopy(rowLabels, 0, newRowLabels, 0, rowLabels.length);
			System.arraycopy(rowLabelConstraints, 0, newRowLabelConStraints, 0, rowLabelConstraints.length);
			rowLabels = newRowLabels;
			rowLabelConstraints = newRowLabelConStraints;
		}
		
		for (int i=start; i<maxRows; i++)
		{
			rowLabels[i] = createRowLabel(i);
			rowLabelConstraints[i] = Helpers.getGridBagConstraint(0, i, 1, 0, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.NORTHWEST, 0.0, 0.0, Helpers.NULL_INSETS);
		}
	}
	/**
	 * Fills the panel with the current amount of row labels and
	 * adds a blank label at the end for expansion.
	 * @since 27.11.2023
	 * @param rows
	 */
	private void fillLabelsForRows(final int rows)
	{
		// if we have more rows than pre-created, create more!
		if (rows > rowLabels.length) createRowLabels(rows);
		
		getRowHeadlinePanel().removeAll();
		for (int i=0; i<rows; i++)
			getRowHeadlinePanel().add(rowLabels[i], rowLabelConstraints[i]);
		// Hack: to make the buttons stay and not get horizontally centered, we give the panel something to make bigger instead. 
		EMPTY_LABEL_CONSTRAINT_ROW.gridy = rows;
		getRowHeadlinePanel().add(EMPTY_LABLE_ROW, EMPTY_LABEL_CONSTRAINT_ROW);

		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{				
				getRowHeadlinePanel().repaint();
			}
		});
	}
	/**
	 * Update the Strings on the channelButtons with new colors depending on volume.
	 * @since 28.11.2023
	 * @param volLeft
	 * @param volRight
	 */
	private void updateVolume(final int channel, final int peekVolumeLeft, final int peekVolumeRight)
	{
		// When this happens in the EventQueue, it is possible, that in the meantime
		// a new mod was loaded and the values do not fit anymore. We check now, but
		// do we need to add it to the EventQueue anyways?
//		EventQueue.invokeLater(new Runnable()
//		{
//			public void run()
//			{				
//				if (channelButtons!=null && channel<channelButtons.length)
					channelButtons[channel].setText(createChannelName(channel, peekVolumeLeft, peekVolumeRight));
//			}
//		});
	}
	/**
	 * @since 28.11.2023
	 */
	private void resetVolume()
	{
		if (patternContainer!=null)
		{
			final int maxChannels = patternContainer.getChannels();
			for (int c=0; c<maxChannels; c++)
			{
				updateVolume(c, 0, 0);
			}
		}
	}
	/**
	 * @since 28.11.2023
	 */
	private void updateMuteStatus()
	{
		if (patternContainer!=null) // if we do not display anything, there is nothing to update
		{
			boolean muteStatus [] = (currentMixer!=null)?currentMixer.getMuteStatus():null;
			final int channels = patternContainer.getChannels();
			for (int i=0; i<channels; i++)
			{
				Color c = (muteStatus==null || i>=muteStatus.length || !muteStatus[i])?Color.black:Color.lightGray;
				channelButtons[i].setForeground(c);
			}
		}
	}
	/**
	 * @since 28.11.2023
	 * @param whatMute
	 * @param channelNumber
	 */
	private void doMute(final int whatMute, final int channelNumber)
	{
		selectedChannelNumber = channelNumber;
		if (currentMixer!=null)
		{
			switch (whatMute)
			{
				case SOLOCHANNEL:
					currentMixer.makeChannelSolo(channelNumber);
					break;
				case TOGGLEMUTE:
					currentMixer.toggleMuteChannel(channelNumber);
					break;
				case RESET: 
					// Unmute all / reset to defaults
				default: 
					// is also the default fall through 
					currentMixer.unMuteAll();
			}
			updateMuteStatus();
		}
	}
	private void fillWithArrangementIndex(final int index)
	{
		// first safe the current index, so pattern << && >> work
		currentIndex = index;
		
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{				
				try
				{
					final int patternIndex = arrangement[index];
					// set the correct Button of the arrangement
					final JToggleButton theButton = buttonArrangement[index]; 
					theButton.setSelected(true);

					// create the row labels according to # of rows of this pattern and display it
					final Pattern pattern = patternContainer.getPattern(patternIndex);
					fillLabelsForRows(pattern.getRowCount());

					// fill with pattern data
					getTextView_PatternData().setText(pattern.toPatternDataString(false));
					
					getPatternNumberLabel().setText("#"+ModConstants.getAsHex(patternIndex, 2));
					
					// now scroll everything to become visible
					getArrangementPanel().scrollRectToVisible(theButton.getBounds());
					getTextView_PatternData().setCaretPosition(0);
					getTextView_PatternData().moveCaretPosition(0);
					getScrollPane_PatternData().getVerticalScrollBar().setValue(0); // otherwise will never see the 0 row
				}
				catch (Throwable ex)
				{
					// Keep it!
				}
			}
		});
	}
	/**
	 * This method will get called from outside to set a new MOD.
	 * If we have a mixer currently playing, there might still come updates from
	 * there for a mod we do not represent anymore. We need to kill all..
	 * @since 28.11.2023
	 * @param songLength
	 * @param newArrangement
	 * @param newPatternContainer
	 */
	public void fillWithPatternArray(final int songLength, final int [] newArrangement, final PatternContainer newPatternContainer)
	{
		// we receive Data for a new mod. If the old one is playing, stop any updates on that one
		stopUpdateThread();
		
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{				
				getTextView_PatternData().setCaretPosition(0);
				getTextView_PatternData().moveCaretPosition(0);
				getScrollPane_PatternData().getVerticalScrollBar().setValue(0);
				getScrollPane_PatternData().getHorizontalScrollBar().setValue(0);
			}
		});

		patternContainer = newPatternContainer;

		// We need to copy the arrangement to songLength values
		arrangement = new int [songLength];
		for (int i=0; i<songLength; i++) arrangement[i] = newArrangement[i];
		// and then display them
		fillButtonsForArrangement();

		createChannelButtons(patternContainer.getChannels());
		fillButtonsForChannels(0, patternContainer.getChannels());
		fillWithArrangementIndex(0);
	}
	/**
	 * For mute/unmute we need the current Mixer.
	 * ModContainer will take care of setting it. If no mixer is present,
	 * it is set to "null" here!
	 * @since 28.11.2023
	 * @param mixer
	 */
	public void setMixer(BasicModMixer mixer)
	{
		currentMixer = mixer;
		updateMuteStatus(); // THIS DOES ONLY WORK, OF THE PIECE IN HERE FITS TO THE MIXER! ModContainer takes care of that...
	}
	/**
	 * push an event into the songFollower queue
	 * @param infoObject
	 * @see de.quippy.javamod.multimedia.mod.gui.ModUpdateListener#getPositionInformation(de.quippy.javamod.multimedia.mod.gui.ModUpdateListener.PositionInformation)
	 */
	@Override
	public void getPositionInformation(PositionInformation infoObject)
	{
		if (songFollower!=null) songFollower.push(infoObject);
	}
	/**
	 * @param infoObject
	 * @see de.quippy.javamod.multimedia.mod.gui.ModUpdateListener#getPeekInformation(de.quippy.javamod.multimedia.mod.gui.ModUpdateListener.PeekInformation)
	 */
	@Override
	public void getPeekInformation(PeekInformation infoObject)
	{
		if (songFollower!=null) songFollower.push(infoObject);
	}
	/**
	 * @param infoObject
	 * @see de.quippy.javamod.multimedia.mod.gui.ModUpdateListener#getStatusInformation(de.quippy.javamod.multimedia.mod.gui.ModUpdateListener.StatusInformation)
	 */
	@Override
	public void getStatusInformation(StatusInformation infoObject)
	{
		if (!infoObject.status)
		{
			drainUpdateThread();
			resetVolume();
		}
	}
	/**
	 * Flush the update Thread buffer
	 * @since 24.11.2023
	 */
	public void flushUpdateThread()
	{
		if (songFollower!=null) songFollower.flush();
	}
	/**
	 * Drains all events left over. This will also block adding of new events
	 * till buffer is drained.
	 * @since 29.11.2023
	 */
	public void drainUpdateThread()
	{
		if (songFollower!=null) songFollower.drain();
	}
	/**
	 * @since 28.11.2023
	 * @param doPause
	 */
	public void pauseUpdateThread(final boolean doPause)
	{
		if (songFollower!=null) songFollower.pause(doPause);
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
}
