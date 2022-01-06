/*
 * @(#) MidiInfoPanel.java
 *
 * Created on 29.10.2010 by Daniel Becker
 * 
 *-----------------------------------------------------------------------
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
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
package de.quippy.javamod.multimedia.midi;

import java.awt.LayoutManager;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.quippy.javamod.system.Helpers;

/**
 * @author Daniel Becker
 * @since 29.10.2010
 */
public class MidiInfoPanel extends JPanel
{
	private static final long serialVersionUID = -2853660365143541701L;

	private JLabel midiNameLabel = null;
	private JTextField midiName = null;
	private JLabel midiDurationLabel = null;
	private JTextField midiDuration = null;
	private javax.swing.JLabel midiInfoLabel = null;
	private javax.swing.JScrollPane scrollPane_midiInfo = null;
	private javax.swing.JTextArea midiInfo = null;

	/**
	 * Constructor for MidiInfoPanel
	 */
	public MidiInfoPanel()
	{
		super();
		initialize();
	}
	/**
	 * Constructor for MidiInfoPanel
	 * @param layout
	 */
	public MidiInfoPanel(LayoutManager layout)
	{
		super(layout);
		initialize();
	}
	/**
	 * Constructor for MidiInfoPanel
	 * @param isDoubleBuffered
	 */
	public MidiInfoPanel(boolean isDoubleBuffered)
	{
		super(isDoubleBuffered);
		initialize();
	}
	/**
	 * Constructor for MidiInfoPanel
	 * @param layout
	 * @param isDoubleBuffered
	 */
	public MidiInfoPanel(LayoutManager layout, boolean isDoubleBuffered)
	{
		super(layout, isDoubleBuffered);
		initialize();
	}
	private void initialize()
	{
		this.setName("MidiInfoPane");
		this.setLayout(new java.awt.GridBagLayout());
		this.add(getMidiNameLabel(), 	Helpers.getGridBagConstraint(0, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getMidiName(), 		Helpers.getGridBagConstraint(1, 0, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		this.add(getMidiDurationLabel(),Helpers.getGridBagConstraint(0, 1, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getMidiDuration(),		Helpers.getGridBagConstraint(1, 1, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		this.add(getMidiInfoLabel(),	Helpers.getGridBagConstraint(0, 2, 1, 0, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getScrollPane_MidiInfo(),	Helpers.getGridBagConstraint(0, 3, 1, 0, java.awt.GridBagConstraints.BOTH, java.awt.GridBagConstraints.WEST, 1.0, 1.0));
	}
	public javax.swing.JLabel getMidiNameLabel()
	{
		if (midiNameLabel==null)
		{
			midiNameLabel = new JLabel("Midi file name:");
			midiNameLabel.setFont(Helpers.getDialogFont());
		}
		return midiNameLabel;
	}
	public JTextField getMidiName()
	{
		if (midiName==null)
		{
			midiName = new javax.swing.JTextField();
			midiName.setEditable(false);
			midiName.setFont(Helpers.getDialogFont());
		}
		return midiName;
	}
	public javax.swing.JLabel getMidiDurationLabel()
	{
		if (midiDurationLabel==null)
		{
			midiDurationLabel = new JLabel("Duration:");
			midiDurationLabel.setFont(Helpers.getDialogFont());
		}
		return midiDurationLabel;
	}
	public JTextField getMidiDuration()
	{
		if (midiDuration==null)
		{
			midiDuration = new javax.swing.JTextField();
			midiDuration.setEditable(false);
			midiDuration.setHorizontalAlignment(JTextField.LEADING);
			midiDuration.setFont(Helpers.getDialogFont());
			midiDuration.setColumns(5);
		}
		return midiDuration;
	}
	public javax.swing.JLabel getMidiInfoLabel()
	{
		if (midiInfoLabel==null)
		{
			midiInfoLabel = new javax.swing.JLabel();
			midiInfoLabel.setName("midiInfoLabel");
			midiInfoLabel.setText("Midi information in File");
			midiInfoLabel.setFont(Helpers.getDialogFont());
		}
		return midiInfoLabel;
	}
	private javax.swing.JScrollPane getScrollPane_MidiInfo()
	{
		if (scrollPane_midiInfo == null)
		{
			scrollPane_midiInfo = new javax.swing.JScrollPane();
			scrollPane_midiInfo.setName("scrollPane_midiInfo");
			scrollPane_midiInfo.setViewportView(getMidiInfo());
		}
		return scrollPane_midiInfo;
	}
	public javax.swing.JTextArea getMidiInfo()
	{
		if (midiInfo==null)
		{
			midiInfo = new javax.swing.JTextArea();
			midiInfo.setName("midiInfo");
			midiInfo.setEditable(false);
			midiInfo.setFont(Helpers.getDialogFont());
		}
		return midiInfo;
	}
//	private static final int TEXT = 0x01;
//	private static final int COPYRIGHT_EVENT = 0x02;
//	private static final int TRACKNAME = 0x03;
//	private static final int INSTRUMENTNAME = 0x04; 
	public void fillInfoPanelWith(Sequence currentSequence, String songName)
	{
		getMidiDuration().setText(Helpers.getTimeStringFromMilliseconds(currentSequence.getMicrosecondLength() / 1000L));
		getMidiName().setText(songName);
		Track[] tracks = currentSequence.getTracks();
		StringBuilder fullText = new StringBuilder();
		for (int t=0; t<tracks.length; t++)
		{
			int size = tracks[t].size();
			for (int ticks=0; ticks<size; ticks++)
			{
				MidiEvent event = tracks[t].get(ticks);
				MidiMessage message = event.getMessage();
				if (message instanceof MetaMessage)
				{
					int type = ((MetaMessage)message).getType();
					if (type <= 0x04)
					{
						fullText.append(new String(((MetaMessage)message).getData())).append('\n');
					}
				}
			}
		}
		getMidiInfo().setText(fullText.toString());
		getMidiInfo().select(0, 0);
	}
}
