/*
 * @(#) OPL3ConfigPanel.java
 *
 * Created on 03.08.2020 by Daniel Becker
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
package de.quippy.javamod.multimedia.opl3;

import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.net.URL;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import de.quippy.javamod.main.gui.tools.FileChooserFilter;
import de.quippy.javamod.main.gui.tools.FileChooserResult;
import de.quippy.javamod.multimedia.opl3.emu.EmuOPL;
import de.quippy.javamod.system.Helpers;

/**
 * @author Daniel Becker
 * @since 03.08.2020
 */
public class OPL3ConfigPanel extends JPanel
{
	private static final long serialVersionUID = 2068150448569323448L;

	private JPanel checkboxConfigPanel = null;
	private JCheckBox virtualStereo = null;
	private JLabel oplVersionLabel = null;
	private JComboBox<String> oplVersion = null;
	private JLabel rolSoundBankLabel = null;
	private JTextField rolSoundBankUrl = null;
	private JButton searchButton = null;

	OPL3Container parentContainer = null;
	/**
	 * Constructor for OPL3ConfigPanel
	 */
	public OPL3ConfigPanel()
	{
		super();
		initialize();
	}
	/**
	 * Constructor for OPL3ConfigPanel
	 * @param layout
	 */
	public OPL3ConfigPanel(LayoutManager layout)
	{
		super(layout);
		initialize();
	}
	/**
	 * Constructor for OPL3ConfigPanel
	 * @param isDoubleBuffered
	 */
	public OPL3ConfigPanel(boolean isDoubleBuffered)
	{
		super(isDoubleBuffered);
		initialize();
	}
	/**
	 * Constructor for OPL3ConfigPanel
	 * @param layout
	 * @param isDoubleBuffered
	 */
	public OPL3ConfigPanel(LayoutManager layout, boolean isDoubleBuffered)
	{
		super(layout, isDoubleBuffered);
		initialize();
	}
	/**
	 * @return the parent
	 */
	public OPL3Container getParentContainer()
	{
		return parentContainer;
	}
	/**
	 * @param parent the parent to set
	 */
	public void setParentContainer(OPL3Container parent)
	{
		this.parentContainer = parent;
	}
	private void initialize()
	{
		setName("OPL3ConfigPanel");
		setLayout(new GridBagLayout());
		
		add(getCheckboxConfigPanel(),Helpers.getGridBagConstraint(0, 0, 1, 0, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		add(getRolSoundBankLabel(),  Helpers.getGridBagConstraint(0, 1, 1, 0, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		add(getRolSoundBankURL(),	 Helpers.getGridBagConstraint(0, 2, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		add(getSearchButton(),		 Helpers.getGridBagConstraint(1, 2, 1, 0, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
	}
	private JPanel getCheckboxConfigPanel()
	{
		if (checkboxConfigPanel==null)
		{
			checkboxConfigPanel = new JPanel();
			checkboxConfigPanel.setName("checkboxConfigPanel");
			checkboxConfigPanel.setLayout(new GridBagLayout());
			
			checkboxConfigPanel.add(getVirtualStereo(), 	Helpers.getGridBagConstraint(0, 0, 2, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			checkboxConfigPanel.add(getOplVersionLabel(), 	Helpers.getGridBagConstraint(1, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			checkboxConfigPanel.add(getOplVersion(), 		Helpers.getGridBagConstraint(2, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		}
		return checkboxConfigPanel;
	}
	public JCheckBox getVirtualStereo()
	{
		if (virtualStereo == null)
		{
			virtualStereo = new JCheckBox();
			virtualStereo.setName("virtualStereo");
			virtualStereo.setText("Virtual Stereo Mix (not with OPL2)");
			virtualStereo.setFont(Helpers.getDialogFont());
			virtualStereo.addItemListener(new ItemListener()
			{
				public void itemStateChanged(ItemEvent e)
				{
					if (e.getStateChange()==ItemEvent.SELECTED || e.getStateChange()==ItemEvent.DESELECTED)
					{
						OPL3Container parent = getParentContainer();
						if (parent!=null)
						{
							OPL3Mixer currentMixer = parent.getCurrentMixer();
							if (currentMixer!=null)
								currentMixer.setDoVirtualSereoMix(getVirtualStereo().isSelected());
						}
					}
				}
			});
		}
		return virtualStereo;
	}
	public JLabel getOplVersionLabel()
	{
		if (oplVersionLabel==null)
		{
			oplVersionLabel = new JLabel();
			oplVersionLabel.setName("oplVersionLabel");
			oplVersionLabel.setText("OPL Version");
			oplVersionLabel.setFont(Helpers.getDialogFont());
		}
		return oplVersionLabel;
	}
	public JComboBox<String> getOplVersion()
	{
		if (oplVersion==null)
		{
			oplVersion = new JComboBox<String>();
			oplVersion.setName("oplVersion");
			
			DefaultComboBoxModel<String> theModel = new DefaultComboBoxModel<String>(EmuOPL.versionNames);
			oplVersion.setModel(theModel);
			oplVersion.setFont(Helpers.getDialogFont());
			oplVersion.setEnabled(true);
		}
		return oplVersion;
	}
	private JLabel getRolSoundBankLabel()
	{
		if (rolSoundBankLabel==null)
		{
			rolSoundBankLabel = new JLabel("ROL Soundbank File");
			rolSoundBankLabel.setFont(Helpers.getDialogFont());
		}
		return rolSoundBankLabel;
	}
	public JTextField getRolSoundBankURL()
	{
		if (rolSoundBankUrl==null)
		{
			rolSoundBankUrl = new javax.swing.JTextField();
			rolSoundBankUrl.setColumns(20);
			rolSoundBankUrl.setFont(Helpers.getDialogFont());
		}
		return rolSoundBankUrl;
	}
	private void doSelectSoundbankFile()
	{
		FileFilter[] fileFilter = new FileFilter[] { new FileChooserFilter("*", "All files"), new FileChooserFilter("bnk", "AdLib Soundbank file (*.bnk)") };
		FileChooserResult selectedFile = Helpers.selectFileNameFor(this, null, "Select soundbank file", fileFilter, false, 0, false, false);
		if (selectedFile!=null)
		{
			File select = selectedFile.getSelectedFile();
			getRolSoundBankURL().setText(select.toString());
		}
	}
	private JButton getSearchButton()
	{
		if (searchButton==null)
		{
			searchButton = new javax.swing.JButton();
			searchButton.setMnemonic('S');
			searchButton.setText("Search");
			searchButton.setFont(Helpers.getDialogFont());
			searchButton.setToolTipText("Search an AdLib soundbank file for the ROL synthesizer");
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
	public URL getSoundBankURL()
	{
		return Helpers.createURLfromString(getRolSoundBankURL().getText());
	}
	public EmuOPL.version getOPLVersion()
	{
		final int index = getOplVersion().getSelectedIndex();
		return EmuOPL.getVersionForIndex(index);
	}
	public void setOPLVersion(final EmuOPL.version version)
	{
		int index = EmuOPL.getIndexForVersion(version);
		if (index==-1) index = EmuOPL.getIndexForVersion(Enum.valueOf(EmuOPL.version.class, OPL3Container.DEFAULT_OPLVERSION));
		getOplVersion().setSelectedIndex(index);
	}
}
