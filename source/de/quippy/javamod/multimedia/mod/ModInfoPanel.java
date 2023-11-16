/*
 * @(#) ModInfoPanel.java
 *
 * Created on 13.10.2007 by Daniel Becker
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
package de.quippy.javamod.multimedia.mod;

import java.awt.LayoutManager;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import de.quippy.javamod.multimedia.HasParentDialog;
import de.quippy.javamod.multimedia.mod.gui.ModInstrumentDialog;
import de.quippy.javamod.multimedia.mod.gui.ModPatternDialog;
import de.quippy.javamod.multimedia.mod.gui.ModSampleDialog;
import de.quippy.javamod.multimedia.mod.loader.Module;
import de.quippy.javamod.system.Helpers;

/**
 * @author Daniel Becker
 * @since 13.10.2007
 */
public class ModInfoPanel extends JPanel implements HasParentDialog
{
	private static final long serialVersionUID = 6435757622273854276L;

	private JLabel modInfo_L_Filename = null;
	private JTextField modInfo_Filename = null;
	private JLabel modInfo_L_Modname = null;
	private JTextField modInfo_Modname = null;
	private JLabel modInfo_L_Instruments = null;
	private JScrollPane scrollPane_ModInfo_Instruments = null;
	private JTextArea modInfo_Instruments = null;
	private JLabel modInfo_L_Trackername = null;
	private JTextField modInfo_Trackername = null;
	private JButton modInfo_openPatternDialog = null;
	private JButton modInfo_openSampleDialog = null;
	private JButton modInfo_openInstrumentDialog = null;
	private ModPatternDialog modPatternDialog = null;
	private ModSampleDialog modSampleDialog = null;
	private ModInstrumentDialog modInstrumentDialog = null;

	private JDialog parent = null;
	
	/**
	 * Constructor for ModInfoPanel
	 */
	public ModInfoPanel()
	{
		super();
		initialize();
	}
	/**
	 * Constructor for ModInfoPanel
	 * @param layout
	 */
	public ModInfoPanel(LayoutManager layout)
	{
		super(layout);
		initialize();
	}
	/**
	 * Constructor for ModInfoPanel
	 * @param isDoubleBuffered
	 */
	public ModInfoPanel(boolean isDoubleBuffered)
	{
		super(isDoubleBuffered);
		initialize();
	}
	/**
	 * Constructor for ModInfoPanel
	 * @param layout
	 * @param isDoubleBuffered
	 */
	public ModInfoPanel(LayoutManager layout, boolean isDoubleBuffered)
	{
		super(layout, isDoubleBuffered);
		initialize();
	}
	/**
	 * @param parent
	 * @see de.quippy.javamod.multimedia.HasParentDialog#setParentDialog(javax.swing.JDialog)
	 */
	@Override
	public void setParentDialog(JDialog parent)
	{
		this.parent = parent;
	}
	/**
	 * We have dialogs in own responsibility nobody knows of, so do update...
	 * @see javax.swing.JPanel#updateUI()
	 */
	@Override
	public void updateUI()
	{
		super.updateUI();
		// do not use "getModPatterDialog" here. When this is called, this
		// panel does not have a parent container.
		if (modPatternDialog!=null) SwingUtilities.updateComponentTreeUI(modPatternDialog);
		if (modSampleDialog!=null) SwingUtilities.updateComponentTreeUI(modSampleDialog);
		if (modInstrumentDialog!=null) SwingUtilities.updateComponentTreeUI(modInstrumentDialog);
	}
	private void initialize()
	{
		this.setName("ModInfoPane");
		this.setLayout(new java.awt.GridBagLayout());

		this.add(getModInfo_L_Filename(),				Helpers.getGridBagConstraint(0, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getModInfo_Filename(),					Helpers.getGridBagConstraint(1, 0, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		this.add(getModInfo_L_Modname(),				Helpers.getGridBagConstraint(0, 1, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getModInfo_Modname(),					Helpers.getGridBagConstraint(1, 1, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		this.add(getModInfo_OpenPatternDialog(), 		Helpers.getGridBagConstraint(1, 2, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.EAST, 1.0, 0.0));
		this.add(getModInfo_OpenSampleDialog(), 		Helpers.getGridBagConstraint(2, 2, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.EAST, 1.0, 0.0));
		this.add(getModInfo_OpenInstrumentDialog(),		Helpers.getGridBagConstraint(3, 2, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.EAST, 1.0, 0.0));
		this.add(getModInfo_L_Instruments(),			Helpers.getGridBagConstraint(0, 2, 1, 0, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		this.add(getScrollPane_ModInfo_Instruments(),	Helpers.getGridBagConstraint(0, 3, 1, 0, java.awt.GridBagConstraints.BOTH, java.awt.GridBagConstraints.WEST, 1.0, 1.0));
		this.add(getModInfo_L_Trackername(),			Helpers.getGridBagConstraint(0, 4, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getModInfo_Trackername(),				Helpers.getGridBagConstraint(1, 4, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
	}
	private JLabel getModInfo_L_Filename()
	{
		if (modInfo_L_Filename==null)
		{
			modInfo_L_Filename = new JLabel();
			modInfo_L_Filename.setName("modInfo_L_Filename");
			modInfo_L_Filename.setText("File");
			modInfo_L_Filename.setFont(Helpers.getDialogFont());
		}
		return modInfo_L_Filename;
	}
	private JTextField getModInfo_Filename()
	{
		if (modInfo_Filename==null)
		{
			modInfo_Filename = new JTextField();
			modInfo_Filename.setName("modInfo_Filename");
			modInfo_Filename.setFont(Helpers.getDialogFont());
			modInfo_Filename.setEditable(false);
		}
		return modInfo_Filename;
	}
	private JLabel getModInfo_L_Instruments()
	{
		if (modInfo_L_Instruments==null)
		{
			modInfo_L_Instruments = new JLabel();
			modInfo_L_Instruments.setName("modInfo_L_Instruments");
			modInfo_L_Instruments.setText("Song Informations");
			modInfo_L_Instruments.setFont(Helpers.getDialogFont());
		}
		return modInfo_L_Instruments;
	}
	private JButton getModInfo_OpenPatternDialog()
	{
		if (modInfo_openPatternDialog==null)
		{
			modInfo_openPatternDialog = new JButton();
			modInfo_openPatternDialog.setName("modInfo_openPatternDialog");
			modInfo_openPatternDialog.setText("Pattern");
			modInfo_openPatternDialog.setMnemonic('P');
			modInfo_openPatternDialog.setFont(Helpers.getDialogFont());
			modInfo_openPatternDialog.setToolTipText("Show the pattern data of this mod");
			modInfo_openPatternDialog.addActionListener(new java.awt.event.ActionListener()
	        {
	            public void actionPerformed(java.awt.event.ActionEvent evt)
	            {
	            	getModPatternDialog().setVisible(true);
	            }
	        });
		}
		return modInfo_openPatternDialog;
	}
	private JButton getModInfo_OpenSampleDialog()
	{
		if (modInfo_openSampleDialog==null)
		{
			modInfo_openSampleDialog = new JButton();
			modInfo_openSampleDialog.setName("modInfo_openSampleDialog");
			modInfo_openSampleDialog.setText("Sample");
			modInfo_openSampleDialog.setMnemonic('S');
			modInfo_openSampleDialog.setFont(Helpers.getDialogFont());
			modInfo_openSampleDialog.setToolTipText("Show the sample data of this mod");
			modInfo_openSampleDialog.addActionListener(new java.awt.event.ActionListener()
	        {
	            public void actionPerformed(java.awt.event.ActionEvent evt)
	            {
	            	getModSampleDialog().setVisible(true);
	            }
	        });
		}
		return modInfo_openSampleDialog;
	}
	private JButton getModInfo_OpenInstrumentDialog()
	{
		if (modInfo_openInstrumentDialog==null)
		{
			modInfo_openInstrumentDialog = new JButton();
			modInfo_openInstrumentDialog.setName("modInfo_openInstrumentDialog");
			modInfo_openInstrumentDialog.setText("Instruments");
			modInfo_openInstrumentDialog.setMnemonic('I');
			modInfo_openInstrumentDialog.setFont(Helpers.getDialogFont());
			modInfo_openInstrumentDialog.setToolTipText("Show the instrument data of this mod");
			modInfo_openInstrumentDialog.addActionListener(new java.awt.event.ActionListener()
	        {
	            public void actionPerformed(java.awt.event.ActionEvent evt)
	            {
	            	getModInstrumentDialog().setVisible(true);
	            }
	        });
			
		}
		return modInfo_openInstrumentDialog;
	}
	protected ModPatternDialog getModPatternDialog()
	{
		if (modPatternDialog==null)
		{
			modPatternDialog = new ModPatternDialog(parent, false);
			modPatternDialog.setLocation(Helpers.getFrameCenteredLocation(modPatternDialog, parent));
		}
		return modPatternDialog;
	}
	protected ModSampleDialog getModSampleDialog()
	{
		if (modSampleDialog==null)
		{
			modSampleDialog = new ModSampleDialog(parent, false);
			modSampleDialog.setLocation(Helpers.getFrameCenteredLocation(modSampleDialog, parent));
		}
		return modSampleDialog;
	}
	protected ModInstrumentDialog getModInstrumentDialog()
	{
		if (modInstrumentDialog==null)
		{
			modInstrumentDialog = new ModInstrumentDialog(parent, false);
			modInstrumentDialog.setLocation(Helpers.getFrameCenteredLocation(modInstrumentDialog, parent));
		}
		return modInstrumentDialog;
	}
	private JScrollPane getScrollPane_ModInfo_Instruments()
	{
		if (scrollPane_ModInfo_Instruments == null)
		{
			scrollPane_ModInfo_Instruments = new JScrollPane();
			scrollPane_ModInfo_Instruments.setName("scrollPane_ModInfo_Instruments");
			scrollPane_ModInfo_Instruments.setViewportView(getModInfo_Instruments());
		}
		return scrollPane_ModInfo_Instruments;
	}
	private JTextArea getModInfo_Instruments()
	{
		if (modInfo_Instruments==null)
		{
			modInfo_Instruments = new JTextArea();
			modInfo_Instruments.setName("modInfo_Instruments");
			modInfo_Instruments.setEditable(false);
			modInfo_Instruments.setFont(Helpers.getTextAreaFont());
		}
		return modInfo_Instruments;
	}
	private JLabel getModInfo_L_Modname()
	{
		if (modInfo_L_Modname==null)
		{
			modInfo_L_Modname = new JLabel();
			modInfo_L_Modname.setName("modInfo_L_Modname");
			modInfo_L_Modname.setText("Name");
			modInfo_L_Modname.setFont(Helpers.getDialogFont());
		}
		return modInfo_L_Modname;
	}
	private JTextField getModInfo_Modname()
	{
		if (modInfo_Modname==null)
		{
			modInfo_Modname = new JTextField();
			modInfo_Modname.setName("modInfo_Modname");
			modInfo_Modname.setFont(Helpers.getDialogFont());
			modInfo_Modname.setEditable(false);
		}
		return modInfo_Modname;
	}
	private JLabel getModInfo_L_Trackername()
	{
		if (modInfo_L_Trackername==null)
		{
			modInfo_L_Trackername = new JLabel();
			modInfo_L_Trackername.setName("modInfo_L_Trackername");
			modInfo_L_Trackername.setText("Tracker");
			modInfo_L_Trackername.setFont(Helpers.getDialogFont());
		}
		return modInfo_L_Trackername;
	}
	private JTextField getModInfo_Trackername()
	{
		if (modInfo_Trackername==null)
		{
			modInfo_Trackername = new JTextField();
			modInfo_Trackername.setName("modInfo_Trackername");
			modInfo_Trackername.setFont(Helpers.getDialogFont());
			modInfo_Trackername.setEditable(false);
		}
		return modInfo_Trackername;
	}
	public void fillInfoPanelWith(final Module currentMod)
	{
		if (currentMod==null)
		{
	    	getModInfo_Filename().setText(Helpers.EMPTY_STING);
	    	getModInfo_Modname().setText(Helpers.EMPTY_STING);
	    	getModInfo_Trackername().setText(Helpers.EMPTY_STING);
	    	getModInfo_Instruments().setText(Helpers.EMPTY_STING);
		}
		else
		{
			String modFileName = currentMod.getFileName();
			int i = modFileName.lastIndexOf(File.separatorChar);
			if (i==-1) i= modFileName.lastIndexOf('/');
	    	getModInfo_Filename().setText(modFileName.substring(i+1));
	    	getModInfo_Filename().select(0, 0);
	    	getModInfo_Modname().setText(currentMod.getSongName());
	    	getModInfo_Modname().select(0, 0);
	    	getModInfo_Trackername().setText(currentMod.toShortInfoString());
	    	getModInfo_Trackername().select(0, 0);
	    	StringBuilder songInfos = new StringBuilder();
	    	String songMessage = currentMod.getSongMessage();
	    	if (songMessage!=null && songMessage.length()>0) songInfos.append("Song Message:\n").append(songMessage).append("\n\nInstrument Names:\n");
	    	if (currentMod.getInstrumentContainer()!=null)
	    		songInfos.append(currentMod.getInstrumentContainer().toString());
	    	getModInfo_Instruments().setText(songInfos.toString());
	    	getModInfo_Instruments().select(0, 0);
	    	
	    	// Only do this, if we have a parent dialog. If not, these dialogs will never
	    	// get destroyed (and will never be visible anyways...)
	    	if (parent!=null)
	    	{
	    		if (currentMod.getPatternContainer()!=null) getModPatternDialog().fillWithPatternArray(currentMod.getSongLength(), currentMod.getArrangement(), currentMod.getPatternContainer().getPattern());
		    	if (currentMod.getInstrumentContainer()!=null)
		    	{
		    		getModSampleDialog().fillWithSamples(currentMod.getInstrumentContainer().getSamples());
		    		getModInstrumentDialog().fillWithInstrumentArray(currentMod.getInstrumentContainer().getInstruments());
		    	}
	    	}
		}
	}
}
