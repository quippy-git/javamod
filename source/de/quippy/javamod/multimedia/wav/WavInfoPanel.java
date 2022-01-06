/*
 * @(#) WavInfoPanel.java
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
package de.quippy.javamod.multimedia.wav;

import java.awt.LayoutManager;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.quippy.javamod.system.Helpers;
import de.quippy.javamod.system.Log;

/**
 * @author Daniel Becker
 * @since 29.10.2010
 */
public class WavInfoPanel extends JPanel
{
	private static final long serialVersionUID = -2853660365143541701L;

	private JLabel wavNameLabel = null;
	private JTextField wavName = null;
	private JLabel wavDurationLabel = null;
	private JTextField wavDuration = null;
	private JLabel wavSampleSizeInBitsLabel = null;
	private JTextField wavSampleSizeInBits = null;
	private JLabel wavFrequencyLabel = null;
	private JTextField wavFrequency = null;
	private JLabel wavChannelsLabel = null;
	private JTextField wavChannels = null;
	private JLabel wavEncodingLabel = null;
	private JTextField wavEncoding = null;
	/**
	 * Constructor for WavInfoPanel
	 */
	public WavInfoPanel()
	{
		super();
		initialize();
	}
	/**
	 * Constructor for WavInfoPanel
	 * @param layout
	 */
	public WavInfoPanel(LayoutManager layout)
	{
		super(layout);
		initialize();
	}
	/**
	 * Constructor for WavInfoPanel
	 * @param isDoubleBuffered
	 */
	public WavInfoPanel(boolean isDoubleBuffered)
	{
		super(isDoubleBuffered);
		initialize();
	}
	/**
	 * Constructor for WavInfoPanel
	 * @param layout
	 * @param isDoubleBuffered
	 */
	public WavInfoPanel(LayoutManager layout, boolean isDoubleBuffered)
	{
		super(layout, isDoubleBuffered);
		initialize();
	}
	private void initialize()
	{
		this.setName("WavInfoPane");
		this.setLayout(new java.awt.GridBagLayout());
		this.add(getWavNameLabel(), 			Helpers.getGridBagConstraint(0, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getWavName(), 					Helpers.getGridBagConstraint(1, 0, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		this.add(getWavFrequencyLabel(),		Helpers.getGridBagConstraint(0, 1, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getWavFrequency(),				Helpers.getGridBagConstraint(1, 1, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getWavSampleSizeInBitsLabel(),	Helpers.getGridBagConstraint(2, 1, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getWavSampleSizeInBits(),		Helpers.getGridBagConstraint(3, 1, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getWavChannelsLabel(),			Helpers.getGridBagConstraint(4, 1, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getWavChannels(),				Helpers.getGridBagConstraint(5, 1, 1, 0, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getWavEncodingLabel(),			Helpers.getGridBagConstraint(0, 2, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getWavEncoding(),				Helpers.getGridBagConstraint(1, 2, 1, 3, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getWavDurationLabel(),			Helpers.getGridBagConstraint(4, 2, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getWavDuration(),				Helpers.getGridBagConstraint(5, 2, 1, 0, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		
	}
	public javax.swing.JLabel getWavNameLabel()
	{
		if (wavNameLabel==null)
		{
			wavNameLabel = new JLabel("Wav file name:");
			wavNameLabel.setFont(Helpers.getDialogFont());
		}
		return wavNameLabel;
	}
	public JTextField getWavName()
	{
		if (wavName==null)
		{
			wavName = new javax.swing.JTextField();
			wavName.setEditable(false);
			wavName.setFont(Helpers.getDialogFont());
		}
		return wavName;
	}
	public javax.swing.JLabel getWavDurationLabel()
	{
		if (wavDurationLabel==null)
		{
			wavDurationLabel = new JLabel("Duration:");
			wavDurationLabel.setFont(Helpers.getDialogFont());
		}
		return wavDurationLabel;
	}
	public JTextField getWavDuration()
	{
		if (wavDuration==null)
		{
			wavDuration = new javax.swing.JTextField();
			wavDuration.setEditable(false);
			wavDuration.setColumns(5);
			wavDuration.setFont(Helpers.getDialogFont());
		}
		return wavDuration;
	}
	public javax.swing.JLabel getWavFrequencyLabel()
	{
		if (wavFrequencyLabel==null)
		{
			wavFrequencyLabel = new JLabel("Rate:");
			wavFrequencyLabel.setFont(Helpers.getDialogFont());
		}
		return wavFrequencyLabel;
	}
	public JTextField getWavFrequency()
	{
		if (wavFrequency==null)
		{
			wavFrequency = new javax.swing.JTextField();
			wavFrequency.setEditable(false);
			wavFrequency.setColumns(5);
			wavFrequency.setFont(Helpers.getDialogFont());
		}
		return wavFrequency;
	}
	public javax.swing.JLabel getWavSampleSizeInBitsLabel()
	{
		if (wavSampleSizeInBitsLabel==null)
		{
			wavSampleSizeInBitsLabel = new JLabel("Bits:");
			wavSampleSizeInBitsLabel.setFont(Helpers.getDialogFont());
		}
		return wavSampleSizeInBitsLabel;
	}
	public JTextField getWavSampleSizeInBits()
	{
		if (wavSampleSizeInBits==null)
		{
			wavSampleSizeInBits = new javax.swing.JTextField();
			wavSampleSizeInBits.setEditable(false);
			wavSampleSizeInBits.setColumns(5);
			wavSampleSizeInBits.setFont(Helpers.getDialogFont());
		}
		return wavSampleSizeInBits;
	}
	public javax.swing.JLabel getWavChannelsLabel()
	{
		if (wavChannelsLabel==null)
		{
			wavChannelsLabel = new JLabel("Channel:");
			wavChannelsLabel.setFont(Helpers.getDialogFont());
		}
		return wavChannelsLabel;
	}
	public JTextField getWavChannels()
	{
		if (wavChannels==null)
		{
			wavChannels = new javax.swing.JTextField();
			wavChannels.setEditable(false);
			wavChannels.setColumns(5);
			wavChannels.setFont(Helpers.getDialogFont());
		}
		return wavChannels;
	}
	public javax.swing.JLabel getWavEncodingLabel()
	{
		if (wavEncodingLabel==null)
		{
			wavEncodingLabel = new JLabel("Encoding:");
			wavEncodingLabel.setFont(Helpers.getDialogFont());
		}
		return wavEncodingLabel;
	}
	public JTextField getWavEncoding()
	{
		if (wavEncoding==null)
		{
			wavEncoding = new javax.swing.JTextField();
			wavEncoding.setEditable(false);
			wavEncoding.setColumns(10);
			wavEncoding.setFont(Helpers.getDialogFont());
		}
		return wavEncoding;
	}
	public void fillInfoPanelWith(AudioInputStream audioInputStream, String songName)
	{
		getWavName().setText(songName);

		AudioFormat audioFormat = audioInputStream.getFormat();
		
		getWavFrequency().setText(Integer.toString((int)audioFormat.getSampleRate()));
		getWavSampleSizeInBits().setText(Integer.toString(audioFormat.getSampleSizeInBits()));
		getWavChannels().setText(Integer.toString(audioFormat.getChannels()));
		getWavEncoding().setText(audioFormat.getEncoding().toString());
		
		int lengthInMilliseconds = 0;
		try
		{
			lengthInMilliseconds = (int)(((long)audioInputStream.available() / ((long)audioFormat.getSampleSizeInBits()>>3) / (long)audioFormat.getChannels()) * 1000L / (long)audioFormat.getSampleRate());
		}
		catch (IOException ex)
		{
			Log.error("IGNORED", ex);
		}
		getWavDuration().setText(Helpers.getTimeStringFromMilliseconds(lengthInMilliseconds));
	}
}
