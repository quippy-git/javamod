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

import de.quippy.javamod.multimedia.mod.loader.pattern.Pattern;
import de.quippy.javamod.system.Helpers;

/**
 * @author Daniel Becker
 * @since 25.07.2020
 */
public class ModPatternDialog extends JDialog
{
	private static final long serialVersionUID = 4511905120124137632L;

	private JScrollPane scrollPane_ArrangementData = null;
	private JPanel arrangementPanel = null;
	private JPanel fixedHightPanel = null;
	private JLabel labelArrangement = null;
	private JButton nextPatternButton = null;
	private JButton prevPatternButton = null;
	private JScrollPane scrollPane_PatternData = null;
	private JTextArea textArea_PatternData= null;
	private JToggleButton [] buttonArrangement;
	private ButtonGroup buttonGroup = null;

	private int [] arrangement;
	private Pattern [] patterns;
	private int currentIndex;
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
	private JTextArea getTextView_PatternData()
	{
		if (textArea_PatternData==null)
		{
			textArea_PatternData = new JTextArea();
			textArea_PatternData.setName("modInfo_Instruments");
			textArea_PatternData.setEditable(false);
			textArea_PatternData.setFont(Helpers.getTextAreaFont());
			fillWithArrangementIndex(0);
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
					if (arrangement!=null && currentIndex>0) buttonArrangement[currentIndex-1].doClick();
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
					if (arrangement!=null && currentIndex<(arrangement.length-1)) buttonArrangement[currentIndex+1].doClick();
				}
			});
		}
		return nextPatternButton;
	}
	private void fillWithArrangementIndex(final int index)
	{
		if (arrangement!=null)
		{
			currentIndex = index;
			int patternIndex = arrangement[index];
			Pattern pattern = patterns[patternIndex];
			if (pattern!=null)
			{
//				final StringBuilder fullText = new StringBuilder("<HTML><HEAD><meta charset=\"utf-8\"><style>table, th, td { border: 1px solid; } #coll { border-collapse: collapse; }</style></HEAD><BODY><TABLE ID=\"TABLE\" CELLPADDING=\"0\" CELLSPACING=\"0\" ");
//				fullText.append("style=\"")
//				.append("font-family:").append(Helpers.getTextAreaFont().getFamily()).append("; ")
//				.append("font-size:").append(Helpers.getTextAreaFont().getSize()).append(';')
//				.append("\">");
//				fullText.append(pattern.toHTMLString());
//				fullText.append("</TABLE></FONT></BODY></HTML>");
//				getTextView_PatternData().setText(fullText.toString());
				getTextView_PatternData().setText(pattern.toString());
				getTextView_PatternData().select(0,0);
			}
		}
	}
	public void fillWithPatternArray(final int songLength, final int [] arrangement, final Pattern [] patterns)
	{
		if (arrangement==null || patterns==null) return;
		this.arrangement = new int [songLength];
		for (int i=0; i<songLength; i++) this.arrangement[i] = arrangement[i];
		this.patterns = patterns;
		fillButtonsForArrangement();
		if (buttonArrangement!=null && buttonArrangement.length>0 && buttonArrangement[0]!=null) 
			buttonArrangement[0].doClick();
	}
}
