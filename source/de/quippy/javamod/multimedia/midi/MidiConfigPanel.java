/*
 * @(#) MidiConfigPanel.java
 *
 * Created on 24.10.2010 by Daniel Becker
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
import java.io.File;

import javax.sound.midi.MidiDevice;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import de.quippy.javamod.main.gui.tools.FileChooserFilter;
import de.quippy.javamod.main.gui.tools.FileChooserResult;
import de.quippy.javamod.system.Helpers;

/**
 * @author Daniel Becker
 * @since 24.10.2010
 */
public class MidiConfigPanel extends JPanel
{
	private static final long serialVersionUID = -3555440476406286002L;

	private JLabel midiOutputDeviceLabel = null; 
	private JComboBox<MidiDevice.Info> midiOutputDevice = null;
	private JLabel midiSoundBankLabel = null;
	private JTextField midiSoundBankUrl = null;
	private JButton searchButton = null;
	private JCheckBox capture = null;
	private JLabel mixerInputDeviceLabel = null; 
	private JComboBox<javax.sound.sampled.Mixer.Info> mixerInputDevice = null;

	private MidiContainer parentContainer;
	
	/**
	 * Constructor for MidiConfigPanel
	 */
	public MidiConfigPanel()
	{
		super();
		initialize();
	}
	/**
	 * Constructor for MidiConfigPanel
	 * @param layout
	 */
	public MidiConfigPanel(LayoutManager layout)
	{
		super(layout);
		initialize();
	}
	/**
	 * Constructor for MidiConfigPanel
	 * @param isDoubleBuffered
	 */
	public MidiConfigPanel(boolean isDoubleBuffered)
	{
		super(isDoubleBuffered);
		initialize();
	}
	/**
	 * Constructor for MidiConfigPanel
	 * @param layout
	 * @param isDoubleBuffered
	 */
	public MidiConfigPanel(LayoutManager layout, boolean isDoubleBuffered)
	{
		super(layout, isDoubleBuffered);
		initialize();
	}
	/**
	 * @return the parent
	 */
	public MidiContainer getParentContainer()
	{
		return parentContainer;
	}
	/**
	 * @param parent the parent to set
	 */
	public void setParentContainer(MidiContainer parent)
	{
		this.parentContainer = parent;
	}
	private void initialize()
	{
		this.setName("MidiConfigPane");
		this.setLayout(new java.awt.GridBagLayout());
		this.add(getMidiOutputDeviceLabel(), 	Helpers.getGridBagConstraint(0, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getMidiSoundBankLabel(), 		Helpers.getGridBagConstraint(1, 0, 1, 0, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getMidiOutputDevice(), 		Helpers.getGridBagConstraint(0, 1, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getMidiSoundBankURL(), 		Helpers.getGridBagConstraint(1, 1, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		this.add(getSearchButton(), 			Helpers.getGridBagConstraint(2, 1, 1, 0, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getCapture(), 					Helpers.getGridBagConstraint(0, 2, 1, 0, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getMixerInputDeviceLabel(), 	Helpers.getGridBagConstraint(1, 2, 1, 0, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getMixerInputDevice(), 		Helpers.getGridBagConstraint(1, 3, 1, 0, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
	}
	public JCheckBox getCapture()
	{
		if (capture==null)
		{
			capture = new JCheckBox("Capture Midi-Output");
			capture.setFont(Helpers.getDialogFont());
		}
		return capture;
	}

	public javax.swing.JLabel getMidiOutputDeviceLabel()
	{
		if (midiOutputDeviceLabel==null)
		{
			midiOutputDeviceLabel = new JLabel("Midi Ouput Devices");
			midiOutputDeviceLabel.setFont(Helpers.getDialogFont());
		}
		return midiOutputDeviceLabel;
	}
	public javax.swing.JLabel getMidiSoundBankLabel()
	{
		if (midiSoundBankLabel==null)
		{
			midiSoundBankLabel = new JLabel("soundbank file for default synthesizer");
			midiSoundBankLabel.setFont(Helpers.getDialogFont());
		}
		return midiSoundBankLabel;
	}
	public javax.swing.JLabel getMixerInputDeviceLabel()
	{
		if (mixerInputDeviceLabel==null)
		{
			mixerInputDeviceLabel = new JLabel("Capture Devices");
			mixerInputDeviceLabel.setFont(Helpers.getDialogFont());
		}
		return mixerInputDeviceLabel;
	}
	public JComboBox<MidiDevice.Info> getMidiOutputDevice()
	{
		if (midiOutputDevice==null)
		{
			midiOutputDevice = new JComboBox<MidiDevice.Info>();
			midiOutputDevice.setName("midiOutputDevice");
			
			if (MidiContainer.MIDIOUTDEVICEINFOS != null)
			{
				javax.swing.DefaultComboBoxModel<MidiDevice.Info> theModel = new javax.swing.DefaultComboBoxModel<MidiDevice.Info>(MidiContainer.MIDIOUTDEVICEINFOS);
				midiOutputDevice.setModel(theModel);
			}
			midiOutputDevice.setFont(Helpers.getDialogFont());
			midiOutputDevice.setEnabled(true);
			// Changing on the fly does not seem to work!!!
//			midiOutputDevice.addItemListener(new ItemListener()
//			{
//				public void itemStateChanged(ItemEvent e)
//				{
//					if (e.getStateChange()==ItemEvent.SELECTED)
//					{
//						MidiContainer parent = getParentContainer();
//						if (parent!=null)
//						{
//							MidiMixer midiMixer = parent.getCurrentMixer();
//							if (midiMixer!=null)
//							{
//								MidiDevice.Info info = (MidiDevice.Info)midiOutputDevice.getSelectedItem();
//								midiMixer.setNewOutputDevice(info);
//							}
//						}
//					}
//				}
//			});
		}
		return midiOutputDevice;
	}
	public JTextField getMidiSoundBankURL()
	{
		if (midiSoundBankUrl==null)
		{
			midiSoundBankUrl = new javax.swing.JTextField();
			midiSoundBankUrl.setColumns(20);
			midiSoundBankUrl.setFont(Helpers.getDialogFont());
		}
		return midiSoundBankUrl;
	}
	private void doSelectSoundbankFile()
	{
		FileFilter[] fileFilter = new FileFilter[] { new FileChooserFilter("*", "All files"), new FileChooserFilter("gm", "Soundbank file (*.gm)"), new FileChooserFilter("sf2", "Soundfont files (*.sf2)") };
		FileChooserResult selectedFile = Helpers.selectFileNameFor(this, null, "Select soundbank file", fileFilter, false, 0, false, false);
		if (selectedFile!=null)
		{
			File select = selectedFile.getSelectedFile();
			getMidiSoundBankURL().setText(select.toString());
		}
	}
	public JButton getSearchButton()
	{
		if (searchButton==null)
		{
			searchButton = new javax.swing.JButton();
			searchButton.setMnemonic('S');
			searchButton.setText("Search");
			searchButton.setFont(Helpers.getDialogFont());
			searchButton.setToolTipText("Search a soundbank file for the default synthesizer");
			searchButton.addActionListener(new java.awt.event.ActionListener()
	        {
	            public void actionPerformed(java.awt.event.ActionEvent evt)
	            {
	            	doSelectSoundbankFile();
	            }
	        });
		}
		return searchButton;
	}
	public JComboBox<javax.sound.sampled.Mixer.Info> getMixerInputDevice()
	{
		if (mixerInputDevice==null)
		{
			mixerInputDevice = new JComboBox<javax.sound.sampled.Mixer.Info>();
			mixerInputDevice.setName("mixerInputDevice");
			
			if (MidiContainer.MIXERDEVICEINFOS!=null)
			{
				javax.swing.DefaultComboBoxModel<javax.sound.sampled.Mixer.Info> theModel = new javax.swing.DefaultComboBoxModel<javax.sound.sampled.Mixer.Info>(MidiContainer.MIXERDEVICEINFOS);
				mixerInputDevice.setModel(theModel);
			}
			mixerInputDevice.setFont(Helpers.getDialogFont());
			mixerInputDevice.setEnabled(true);
		}
		return mixerInputDevice;
	}
}
