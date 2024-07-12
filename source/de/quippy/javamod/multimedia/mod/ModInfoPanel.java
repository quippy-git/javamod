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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import de.quippy.javamod.main.gui.components.FixedStateCheckBox;
import de.quippy.javamod.multimedia.mod.gui.ModInstrumentDialog;
import de.quippy.javamod.multimedia.mod.gui.ModPatternDialog;
import de.quippy.javamod.multimedia.mod.gui.ModSampleDialog;
import de.quippy.javamod.multimedia.mod.loader.Module;
import de.quippy.javamod.multimedia.mod.loader.instrument.InstrumentsContainer;
import de.quippy.javamod.system.Helpers;

/**
 * @author Daniel Becker
 * @since 13.10.2007
 */
public class ModInfoPanel extends JPanel
{
	private static final long serialVersionUID = 6435757622273854276L;
	
	private static final int SAMPLE_INDEX = 0;
	private static final int INSTRUMENT_INDEX = 1;
	private static final int SONGMESSAGE_INDEX = 2;

	private JLabel modInfo_L_Filename = null;
	private JTextField modInfo_Filename = null;
	private JLabel modInfo_L_Modname = null;
	private JTextField modInfo_Modname = null;

	private JLabel modInfo_L_Trackername = null;
	private JTextField modInfo_Trackername = null;
	private JLabel modInfo_L_Author = null;
	private JTextField modInfo_Author = null;
	
	private JLabel modInfo_L_FreqTable = null;
	private JTextField modInfo_FreqTable = null;
	private JLabel modInfo_L_TempoMode = null;
	private JTextField modInfo_TempoMode = null;
	private JLabel modInfo_L_Ticks = null;
	private JTextField modInfo_Ticks = null;
	private JLabel modInfo_L_BPM = null;
	private JTextField modInfo_BPM = null;
	private FixedStateCheckBox modInfo_IsStereo = null;

	private JPanel modInfoAnzPanel = null;
	private JLabel modInfo_L_AnzChannels = null;
	private JTextField modInfo_AnzChannels = null;
	private JLabel modInfo_L_AnzPattern = null;
	private JTextField modInfo_AnzPattern = null;
	private JLabel modInfo_L_AnzSamples = null;
	private JTextField modInfo_AnzSamples = null;
	private JLabel modInfo_L_AnzInstruments = null;
	private JTextField modInfo_AnzInstruments = null;

	private JPanel modInfoButtonPanel = null;
	private JButton modInfo_openPatternDialog = null;
	private JButton modInfo_openSampleDialog = null;
	private JButton modInfo_openInstrumentDialog = null;
	private ModPatternDialog modPatternDialog = null;
	private ModSampleDialog modSampleDialog = null;
	private ModInstrumentDialog modInstrumentDialog = null;

	private JTabbedPane modInfoInsSamTabbedPane = null;
	private JScrollPane scrollPane_ModInfo_SongMessage = null;
	private JTextArea modInfo_SongMessage = null;
	private JScrollPane scrollPane_ModInfo_Instruments = null;
	private JTextArea modInfo_Instruments = null;
	private JScrollPane scrollPane_ModInfo_Samples = null;
	private JTextArea modInfo_Samples = null;

	private int oldModPatternDialogVisibility = -1;
	private int oldModSampleDialogVisibility = -1;
	private int oldModInstrumentDialogVisibility = -1;

	private Point patternDialogLocation = null;
	private Dimension patternDialogSize = null;
	private Point sampleDialogLocation = null;
	private Dimension sampleDialogSize = null;
	private Point instrumentDialogLocation = null;
	private Dimension instrumentDialogSize = null;

	private ModContainer parentContainer = null;
	private Window parentInfoDialog = null;
	private Module mod;
	
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
	 * @return the ModContainer that created us
	 */
	public ModContainer getParentContainer()
	{
		return parentContainer;
	}
	/**
	 * @param parentInfoDialog the parentInfoDialog to set
	 */
	public void setParentContainer(ModContainer parent)
	{
		parentContainer = parent;
	}
	/**
	 * We have dialogs in own responsibility nobody knows of, so do update...
	 * @see javax.swing.JPanel#updateUI()
	 */
	@Override
	public void updateUI()
	{
		super.updateUI();
		// do not use the getter Methods here. When this is initially called,
		// this panel does not have a parentInfoDialog container yet.
		if (modPatternDialog!=null) SwingUtilities.updateComponentTreeUI(modPatternDialog);
		if (modSampleDialog!=null) SwingUtilities.updateComponentTreeUI(modSampleDialog);
		if (modInstrumentDialog!=null) SwingUtilities.updateComponentTreeUI(modInstrumentDialog);
	}
	/**
	 * We want to close our possible open children dialogs
	 * when this panel gets removed from the MultimediaInfoPane.
	 * We remember the visibility status for reset.
	 * @see de.quippy.javamod.multimedia.mod.ModInfoPanel#addNotify
	 * @see javax.swing.JComponent#removeNotify()
	 */
	@Override
	public void removeNotify()
	{
		super.removeNotify();

		oldModPatternDialogVisibility = (getModPatternDialog().isVisible()) ? 1 : 0;
		getModPatternDialog().setVisible(false);

		oldModSampleDialogVisibility = (getModSampleDialog().isVisible()) ? 1 : 0;
		getModSampleDialog().setVisible(false);
		
		oldModInstrumentDialogVisibility = (getModInstrumentDialog().isVisible()) ? 1 : 0;
		getModInstrumentDialog().setVisible(false);
	}
	/**
	 * After being added to the MultimediaInfoPane, recreate the last status of
	 * visibility of potential open dialogs again.
	 * But check also, if an old value was set. If the old value == -1 the status is not set.
	 * Other is like C: 0==false, !0==true
	 * After using the value, invalidate the setting.
	 * @see javax.swing.JComponent#addNotify()
	 */
	@Override
	public void addNotify()
	{
		super.addNotify();
		
		if (parentInfoDialog == null)
		{
			// As we got added somewhere without a MultimediaContainer being
			// called, we do not know our parent dialog yet - which we need
			// to know for our sub dialogs
			// So lets find the parent window to which we were added
			Component p = getParent();
			while (p!=null)
			{
				if (p instanceof Window)
				{
					parentInfoDialog = (Window)p; 
					break;
				}
				p = p.getParent();
			}
		}
		
		if (oldModPatternDialogVisibility!=-1)
		{
			getModPatternDialog().setVisible(oldModPatternDialogVisibility!=0);
			oldModPatternDialogVisibility = -1;
		}
		if (oldModSampleDialogVisibility!=-1)
		{
			getModSampleDialog().setVisible(oldModSampleDialogVisibility!=0);
			oldModSampleDialogVisibility = -1;
		}
		if (oldModInstrumentDialogVisibility!=-1)
		{
			getModInstrumentDialog().setVisible(oldModInstrumentDialogVisibility!=0);
			oldModInstrumentDialogVisibility = -1;
		}
	}
	public boolean getModPatternDialogisVisible()
	{
		if (oldModPatternDialogVisibility==-1)
			return getModPatternDialog().isVisible();
		else
			return oldModPatternDialogVisibility!=0;
	}
	public boolean getModSampleDialogisVisible()
	{
		if (oldModSampleDialogVisibility==-1)
			return getModSampleDialog().isVisible();
		else
			return oldModSampleDialogVisibility!=0;
	}
	public boolean getModInstrumentDialogisVisible()
	{
		if (oldModInstrumentDialogVisibility==-1)
			return getModInstrumentDialog().isVisible();
		else
			return oldModInstrumentDialogVisibility!=0;
	}
	/**
	 * @param patternDialogLocation the patternDialogLocation to set
	 */
	protected void setPatternDialogLocation(final Point newPatternDialogLocation)
	{
		patternDialogLocation = newPatternDialogLocation;
	}
	/**
	 * @param patternDialogSize the patternDialogSize to set
	 */
	protected void setPatternDialogSize(final Dimension newPatternDialogSize)
	{
		patternDialogSize = newPatternDialogSize;
	}
	/**
	 * @param patternDialogVisable the patternDialogVisable to set
	 */
	protected void setPatternDialogVisable(final boolean newPatternDialogVisable)
	{
		oldModPatternDialogVisibility = (newPatternDialogVisable)?1:0;
	}
	/**
	 * @param sampleDialogLocation the sampleDialogLocation to set
	 */
	protected void setSampleDialogLocation(final Point newSampleDialogLocation)
	{
		sampleDialogLocation = newSampleDialogLocation;
	}
	/**
	 * @param sampleDialogSize the sampleDialogSize to set
	 */
	protected void setSampleDialogSize(final Dimension newSampleDialogSize)
	{
		sampleDialogSize = newSampleDialogSize;
	}
	/**
	 * @param sampleDialogVisable the sampleDialogVisable to set
	 */
	protected void setSampleDialogVisable(final boolean newSampleDialogVisable)
	{
		oldModSampleDialogVisibility = (newSampleDialogVisable)?1:0;
	}
	/**
	 * @param instrumentDialogLocation the instrumentDialogLocation to set
	 */
	protected void setInstrumentDialogLocation(final Point newInstrumentDialogLocation)
	{
		instrumentDialogLocation = newInstrumentDialogLocation;
	}
	/**
	 * @param instrumentDialogSize the instrumentDialogSize to set
	 */
	protected void setInstrumentDialogSize(final Dimension newInstrumentDialogSize)
	{
		instrumentDialogSize = newInstrumentDialogSize;
	}
	/**
	 * @param instrumentDialogVisable the instrumentDialogVisable to set
	 */
	protected void setInstrumentDialogVisable(final boolean newInstrumentDialogVisable)
	{
		oldModInstrumentDialogVisibility = (newInstrumentDialogVisable)?1:0;
	}
	/**
	 * @since 13.10.2007
	 */
	private void initialize()
	{
		setName("ModInfoPane");
		setLayout(new GridBagLayout());

		add(getModInfo_L_Filename(),		Helpers.getGridBagConstraint(0, 0, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0));
		add(getModInfo_Filename(),			Helpers.getGridBagConstraint(1, 0, 1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, 1.0, 0.0));
		add(getModInfo_L_Modname(),			Helpers.getGridBagConstraint(0, 1, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0));
		add(getModInfo_Modname(),			Helpers.getGridBagConstraint(1, 1, 1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, 1.0, 0.0));
		add(getModInfo_L_Author(),			Helpers.getGridBagConstraint(0, 2, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0));
		add(getModInfo_Author(),			Helpers.getGridBagConstraint(1, 2, 1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, 1.0, 0.0));
		add(getModInfo_L_Trackername(),		Helpers.getGridBagConstraint(0, 3, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0));
		add(getModInfo_Trackername(),		Helpers.getGridBagConstraint(1, 3, 1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, 1.0, 0.0));
		add(getModInfo_L_FreqTable(),		Helpers.getGridBagConstraint(0, 4, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0));
		add(getModInfo_FreqTable(),			Helpers.getGridBagConstraint(1, 4, 1, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, 0.5, 0.0));
		add(getModInfo_L_TempoMode(),		Helpers.getGridBagConstraint(2, 4, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0));
		add(getModInfo_TempoMode(),			Helpers.getGridBagConstraint(3, 4, 1, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, 0.5, 0.0));
		add(getModInfo_IsStereo(),			Helpers.getGridBagConstraint(4, 4, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0));
		add(getModInfoDialog_AnzPanel(),	Helpers.getGridBagConstraint(0, 5, 1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, 1.0, 0.0, Helpers.NULL_INSETS));
		add(getModInfoDialog_ButtonPanel(),	Helpers.getGridBagConstraint(0, 6, 1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, 1.0, 0.0, Helpers.NULL_INSETS));
		add(getModInfo_InsSamTabbedPane(),	Helpers.getGridBagConstraint(0, 7, 1, 0, GridBagConstraints.BOTH, GridBagConstraints.WEST, 1.0, 1.0));
		
		fillInfoPanelWith(null);
	}
	private JLabel getModInfo_L_Filename()
	{
		if (modInfo_L_Filename==null)
		{
			modInfo_L_Filename = new JLabel();
			modInfo_L_Filename.setName("modInfo_L_Filename");
			modInfo_L_Filename.setText("File:");
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
	private JLabel getModInfo_L_Modname()
	{
		if (modInfo_L_Modname==null)
		{
			modInfo_L_Modname = new JLabel();
			modInfo_L_Modname.setName("modInfo_L_Modname");
			modInfo_L_Modname.setText("Name:");
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
	private JLabel getModInfo_L_Author()
	{
		if (modInfo_L_Author==null)
		{
			modInfo_L_Author = new JLabel();
			modInfo_L_Author.setName("modInfo_L_Author");
			modInfo_L_Author.setText("Author:");
			modInfo_L_Author.setFont(Helpers.getDialogFont());
		}
		return modInfo_L_Author;
	}
	private JTextField getModInfo_Author()
	{
		if (modInfo_Author==null)
		{
			modInfo_Author = new JTextField();
			modInfo_Author.setName("modInfo_Author");
			modInfo_Author.setFont(Helpers.getDialogFont());
			modInfo_Author.setEditable(false);
		}
		return modInfo_Author;
	}
	private JLabel getModInfo_L_Trackername()
	{
		if (modInfo_L_Trackername==null)
		{
			modInfo_L_Trackername = new JLabel();
			modInfo_L_Trackername.setName("modInfo_L_Trackername");
			modInfo_L_Trackername.setText("Tracker:");
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
	private JLabel getModInfo_L_FreqTable()
	{
		if (modInfo_L_FreqTable==null)
		{
			modInfo_L_FreqTable = new JLabel();
			modInfo_L_FreqTable.setName("modInfo_L_FreqTable");
			modInfo_L_FreqTable.setText("Table:");
			modInfo_L_FreqTable.setFont(Helpers.getDialogFont());
		}
		return modInfo_L_FreqTable;
	}
	private JTextField getModInfo_FreqTable()
	{
		if (modInfo_FreqTable==null)
		{
			modInfo_FreqTable = new JTextField();
			modInfo_FreqTable.setName("modInfo_FreqTable");
			modInfo_FreqTable.setFont(Helpers.getDialogFont());
			modInfo_FreqTable.setEditable(false);
		}
		return modInfo_FreqTable;
	}
	private JLabel getModInfo_L_TempoMode()
	{
		if (modInfo_L_TempoMode==null)
		{
			modInfo_L_TempoMode = new JLabel();
			modInfo_L_TempoMode.setName("modInfo_L_TempoMode");
			modInfo_L_TempoMode.setText("Tempo mode:");
			modInfo_L_TempoMode.setFont(Helpers.getDialogFont());
		}
		return modInfo_L_TempoMode;
	}
	private JTextField getModInfo_TempoMode()
	{
		if (modInfo_TempoMode==null)
		{
			modInfo_TempoMode = new JTextField();
			modInfo_TempoMode.setName("modInfo_TempoMode");
			modInfo_TempoMode.setFont(Helpers.getDialogFont());
			modInfo_TempoMode.setEditable(false);
		}
		return modInfo_TempoMode;
	}
	private JLabel getModInfo_L_Ticks()
	{
		if (modInfo_L_Ticks==null)
		{
			modInfo_L_Ticks = new JLabel();
			modInfo_L_Ticks.setName("modInfo_L_Ticks");
			modInfo_L_Ticks.setText("Ticks:");
			modInfo_L_Ticks.setFont(Helpers.getDialogFont());
		}
		return modInfo_L_Ticks;
	}
	private JTextField getModInfo_Ticks()
	{
		if (modInfo_Ticks==null)
		{
			modInfo_Ticks = new JTextField();
			modInfo_Ticks.setName("modInfo_Ticks");
			modInfo_Ticks.setFont(Helpers.getDialogFont());
			modInfo_Ticks.setEditable(false);
		}
		return modInfo_Ticks;
	}
	private JLabel getModInfo_L_BPM()
	{
		if (modInfo_L_BPM==null)
		{
			modInfo_L_BPM = new JLabel();
			modInfo_L_BPM.setName("modInfo_L_BPM");
			modInfo_L_BPM.setText("BPM:");
			modInfo_L_BPM.setFont(Helpers.getDialogFont());
		}
		return modInfo_L_BPM;
	}
	private JTextField getModInfo_BPM()
	{
		if (modInfo_BPM==null)
		{
			modInfo_BPM = new JTextField();
			modInfo_BPM.setName("modInfo_BPM");
			modInfo_BPM.setFont(Helpers.getDialogFont());
			modInfo_BPM.setEditable(false);
		}
		return modInfo_BPM;
	}
	public FixedStateCheckBox getModInfo_IsStereo()
	{
		if (modInfo_IsStereo == null)
		{
			modInfo_IsStereo = new FixedStateCheckBox();
			modInfo_IsStereo.setName("modInfo_IsStereo");
			modInfo_IsStereo.setText("is Stereo");
			modInfo_IsStereo.setFont(Helpers.getDialogFont());
//			modInfo_IsStereo.setEnabled(false); // the FixedStateCheckBox is always disabled
		}
		return modInfo_IsStereo;
	}
	private JPanel getModInfoDialog_AnzPanel()
	{
		if (modInfoAnzPanel==null)
		{
			modInfoAnzPanel = new JPanel(new GridBagLayout());
			modInfoAnzPanel.setName("modInfoAnzPanel");
			//modInfoAnzPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

			modInfoAnzPanel.add(getModInfo_L_AnzChannels(),		Helpers.getGridBagConstraint(0, 0, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0));
			modInfoAnzPanel.add(getModInfo_AnzChannels(),		Helpers.getGridBagConstraint(1, 0, 1, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, 1.0, 0.0));
			modInfoAnzPanel.add(getModInfo_L_AnzPattern(),		Helpers.getGridBagConstraint(2, 0, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0));
			modInfoAnzPanel.add(getModInfo_AnzPattern(),		Helpers.getGridBagConstraint(3, 0, 1, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, 1.0, 0.0));
			modInfoAnzPanel.add(getModInfo_L_AnzSamples(),		Helpers.getGridBagConstraint(4, 0, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0));
			modInfoAnzPanel.add(getModInfo_AnzSamples(),		Helpers.getGridBagConstraint(5, 0, 1, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, 1.0, 0.0));
			modInfoAnzPanel.add(getModInfo_L_AnzInstruments(),	Helpers.getGridBagConstraint(6, 0, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0));
			modInfoAnzPanel.add(getModInfo_AnzInstruments(),	Helpers.getGridBagConstraint(7, 0, 1, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, 1.0, 0.0));
			modInfoAnzPanel.add(getModInfo_L_Ticks(),			Helpers.getGridBagConstraint(8, 0, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0));
			modInfoAnzPanel.add(getModInfo_Ticks(),				Helpers.getGridBagConstraint(9, 0, 1, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, 1.0, 0.0));
			modInfoAnzPanel.add(getModInfo_L_BPM(),				Helpers.getGridBagConstraint(10, 0, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0));
			modInfoAnzPanel.add(getModInfo_BPM(),				Helpers.getGridBagConstraint(11, 0, 1, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, 1.0, 0.0));
		}
		return modInfoAnzPanel;
	}
	private JLabel getModInfo_L_AnzChannels()
	{
		if (modInfo_L_AnzChannels==null)
		{
			modInfo_L_AnzChannels = new JLabel();
			modInfo_L_AnzChannels.setName("modInfo_L_AnzChannels");
			modInfo_L_AnzChannels.setText("Channels:");
			modInfo_L_AnzChannels.setFont(Helpers.getDialogFont());
		}
		return modInfo_L_AnzChannels;
	}
	private JTextField getModInfo_AnzChannels()
	{
		if (modInfo_AnzChannels==null)
		{
			modInfo_AnzChannels = new JTextField();
			modInfo_AnzChannels.setName("modInfo_AnzChannels");
			modInfo_AnzChannels.setFont(Helpers.getDialogFont());
			modInfo_AnzChannels.setEditable(false);
		}
		return modInfo_AnzChannels;
	}
	private JLabel getModInfo_L_AnzPattern()
	{
		if (modInfo_L_AnzPattern==null)
		{
			modInfo_L_AnzPattern = new JLabel();
			modInfo_L_AnzPattern.setName("modInfo_L_AnzPattern");
			modInfo_L_AnzPattern.setText("Pattern:");
			modInfo_L_AnzPattern.setFont(Helpers.getDialogFont());
		}
		return modInfo_L_AnzPattern;
	}
	private JTextField getModInfo_AnzPattern()
	{
		if (modInfo_AnzPattern==null)
		{
			modInfo_AnzPattern = new JTextField();
			modInfo_AnzPattern.setName("modInfo_AnzPattern");
			modInfo_AnzPattern.setFont(Helpers.getDialogFont());
			modInfo_AnzPattern.setEditable(false);
		}
		return modInfo_AnzPattern;
	}
	private JLabel getModInfo_L_AnzSamples()
	{
		if (modInfo_L_AnzSamples==null)
		{
			modInfo_L_AnzSamples = new JLabel();
			modInfo_L_AnzSamples.setName("modInfo_L_AnzSamples");
			modInfo_L_AnzSamples.setText("Samples:");
			modInfo_L_AnzSamples.setFont(Helpers.getDialogFont());
		}
		return modInfo_L_AnzSamples;
	}
	private JTextField getModInfo_AnzSamples()
	{
		if (modInfo_AnzSamples==null)
		{
			modInfo_AnzSamples = new JTextField();
			modInfo_AnzSamples.setName("modInfo_AnzSamples");
			modInfo_AnzSamples.setFont(Helpers.getDialogFont());
			modInfo_AnzSamples.setEditable(false);
		}
		return modInfo_AnzSamples;
	}
	private JLabel getModInfo_L_AnzInstruments()
	{
		if (modInfo_L_AnzInstruments==null)
		{
			modInfo_L_AnzInstruments = new JLabel();
			modInfo_L_AnzInstruments.setName("modInfo_L_AnzInstruments");
			modInfo_L_AnzInstruments.setText("Instruments:");
			modInfo_L_AnzInstruments.setFont(Helpers.getDialogFont());
		}
		return modInfo_L_AnzInstruments;
	}
	private JTextField getModInfo_AnzInstruments()
	{
		if (modInfo_AnzInstruments==null)
		{
			modInfo_AnzInstruments = new JTextField();
			modInfo_AnzInstruments.setName("modInfo_AnzInstruments");
			modInfo_AnzInstruments.setFont(Helpers.getDialogFont());
			modInfo_AnzInstruments.setEditable(false);
		}
		return modInfo_AnzInstruments;
	}
	private JPanel getModInfoDialog_ButtonPanel()
	{
		if (modInfoButtonPanel==null)
		{
			modInfoButtonPanel = new JPanel(new GridBagLayout());
			modInfoButtonPanel.setName("modInfoButtonPanel");
			//modInfoButtonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

			modInfoButtonPanel.add(getModInfo_OpenPatternDialog(), 		Helpers.getGridBagConstraint(0, 0, 1, 1, GridBagConstraints.NONE, GridBagConstraints.CENTER, 1.0, 0.0));
			modInfoButtonPanel.add(getModInfo_OpenSampleDialog(), 		Helpers.getGridBagConstraint(1, 0, 1, 1, GridBagConstraints.NONE, GridBagConstraints.CENTER, 1.0, 0.0));
			modInfoButtonPanel.add(getModInfo_OpenInstrumentDialog(),	Helpers.getGridBagConstraint(2, 0, 1, 1, GridBagConstraints.NONE, GridBagConstraints.CENTER, 1.0, 0.0));
		}
		return modInfoButtonPanel;
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
			modInfo_openPatternDialog.addActionListener(new ActionListener()
	        {
	            public void actionPerformed(ActionEvent evt)
	            {
	            	getModPatternDialog().setVisible(!getModPatternDialog().isVisible());
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
			modInfo_openSampleDialog.addActionListener(new ActionListener()
	        {
	            public void actionPerformed(ActionEvent evt)
	            {
	            	getModSampleDialog().setVisible(!getModSampleDialog().isVisible());
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
			modInfo_openInstrumentDialog.addActionListener(new ActionListener()
	        {
	            public void actionPerformed(ActionEvent evt)
	            {
	            	getModInstrumentDialog().setVisible(!getModInstrumentDialog().isVisible());
	            }
	        });
			
		}
		return modInfo_openInstrumentDialog;
	}
	protected ModPatternDialog getModPatternDialog()
	{
		if (modPatternDialog==null)
		{
			modPatternDialog = new ModPatternDialog(parentInfoDialog, false, this);
			if (patternDialogSize!=null)
			{
				modPatternDialog.setSize(patternDialogSize);
				modPatternDialog.setPreferredSize(patternDialogSize);
			}
			if (patternDialogLocation == null || (patternDialogLocation.getX()==-1 || patternDialogLocation.getY()==-1))
				patternDialogLocation = Helpers.getFrameCenteredLocation(modPatternDialog, parentInfoDialog); 
			modPatternDialog.setLocation(patternDialogLocation);
		}
		return modPatternDialog;
	}
	protected ModSampleDialog getModSampleDialog()
	{
		if (modSampleDialog==null)
		{
			modSampleDialog = new ModSampleDialog(parentInfoDialog, false, this);
			if (sampleDialogSize!=null)
			{
				modSampleDialog.setSize(sampleDialogSize);
				modSampleDialog.setPreferredSize(sampleDialogSize);
			}
			if (sampleDialogLocation == null || (sampleDialogLocation.getX()==-1 || sampleDialogLocation.getY()==-1))
				sampleDialogLocation = Helpers.getFrameCenteredLocation(modSampleDialog, parentInfoDialog); 
			modSampleDialog.setLocation(sampleDialogLocation);
		}
		return modSampleDialog;
	}
	protected ModInstrumentDialog getModInstrumentDialog()
	{
		if (modInstrumentDialog==null)
		{
			modInstrumentDialog = new ModInstrumentDialog(parentInfoDialog, false, this);
			if (instrumentDialogSize!=null)
			{
				modInstrumentDialog.setSize(instrumentDialogSize);
				modInstrumentDialog.setPreferredSize(instrumentDialogSize);
			}
			if (instrumentDialogLocation == null || (instrumentDialogLocation.getX()==-1 || instrumentDialogLocation.getY()==-1))
				instrumentDialogLocation = Helpers.getFrameCenteredLocation(modInstrumentDialog, parentInfoDialog); 
			modInstrumentDialog.setLocation(instrumentDialogLocation);
		}
		return modInstrumentDialog;
	}
	public JTabbedPane getModInfo_InsSamTabbedPane()
	{
		if (modInfoInsSamTabbedPane==null)
		{
			modInfoInsSamTabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
			modInfoInsSamTabbedPane.setFont(Helpers.getDialogFont());
			modInfoInsSamTabbedPane.add("Samples", getScrollPane_ModInfo_Samples());
			modInfoInsSamTabbedPane.add("Instruments", getScrollPane_ModInfo_Instruments());
			modInfoInsSamTabbedPane.add("Song Message", getScrollPane_ModInfo_SongMessage());
		}
		return modInfoInsSamTabbedPane;
	}
	private JScrollPane getScrollPane_ModInfo_SongMessage()
	{
		if (scrollPane_ModInfo_SongMessage == null)
		{
			scrollPane_ModInfo_SongMessage = new JScrollPane();
			scrollPane_ModInfo_SongMessage.setName("scrollPane_ModInfo_SongMessage");
			scrollPane_ModInfo_SongMessage.setViewportView(getModInfo_SongMessage());
		}
		return scrollPane_ModInfo_SongMessage;
	}
	private JTextArea getModInfo_SongMessage()
	{
		if (modInfo_SongMessage==null)
		{
			modInfo_SongMessage = new JTextArea();
			modInfo_SongMessage.setName("modInfo_SongMessage");
			modInfo_SongMessage.setEditable(false);
			modInfo_SongMessage.setFont(Helpers.getTextAreaFont());
		}
		return modInfo_SongMessage;
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
	private JScrollPane getScrollPane_ModInfo_Samples()
	{
		if (scrollPane_ModInfo_Samples == null)
		{
			scrollPane_ModInfo_Samples = new JScrollPane();
			scrollPane_ModInfo_Samples.setName("scrollPane_ModInfo_Samples");
			scrollPane_ModInfo_Samples.setViewportView(getModInfo_Samples());
		}
		return scrollPane_ModInfo_Samples;
	}
	private JTextArea getModInfo_Samples()
	{
		if (modInfo_Samples==null)
		{
			modInfo_Samples = new JTextArea();
			modInfo_Samples.setName("modInfo_Samples");
			modInfo_Samples.setEditable(false);
			modInfo_Samples.setFont(Helpers.getTextAreaFont());
		}
		return modInfo_Samples;
	}
	/**
	 * gets called from ModPatternDialog to show the selected instrument.
	 * If this mod has no instruments, it will show the sample
	 * @since 22.01.2024
	 * @param instrumentIndex
	 */
	public void showInstrument(final int instrumentIndex)
	{
		if (!mod.getInstrumentContainer().hasInstruments()) 
			showSample(instrumentIndex);
		else
		{
			getModInstrumentDialog().showInstrument(instrumentIndex);
			getModInstrumentDialog().setVisible(true);
		}
	}
	/**
	 * gets called from ModInstrumentDialog or ModPatternDialog
	 * to show the selected sample
	 * @since 20.12.2023
	 * @param sampleIndex
	 */
	public void showSample(final int sampleIndex)
	{
		getModSampleDialog().showSample(sampleIndex);
		getModSampleDialog().setVisible(true);
	}
	public void fillInfoPanelWith(final Module currentMod)
	{
		mod = currentMod;
		getModInfo_InsSamTabbedPane().setSelectedIndex(SAMPLE_INDEX);
		if (currentMod==null)
		{
	    	getModInfo_Filename().setText(Helpers.EMPTY_STING);
	    	getModInfo_Modname().setText(Helpers.EMPTY_STING);
	    	getModInfo_Author().setText(Helpers.EMPTY_STING);
	    	getModInfo_Trackername().setText(Helpers.EMPTY_STING);
	    	getModInfo_FreqTable().setText(Helpers.EMPTY_STING);
	    	getModInfo_TempoMode().setText(Helpers.EMPTY_STING);
	    	getModInfo_Ticks().setText(Helpers.EMPTY_STING);
	    	getModInfo_BPM().setText(Helpers.EMPTY_STING);
	    	getModInfo_IsStereo().setFixedState(false);
	    	getModInfo_AnzChannels().setText(Helpers.EMPTY_STING);
	    	getModInfo_AnzPattern().setText(Helpers.EMPTY_STING);
	    	getModInfo_AnzSamples().setText(Helpers.EMPTY_STING);
	    	getModInfo_AnzInstruments().setText(Helpers.EMPTY_STING);
	    	getModInfo_Samples().setText(Helpers.EMPTY_STING);
	    	getModInfo_Instruments().setText(Helpers.EMPTY_STING);
			getModInfo_InsSamTabbedPane().setEnabledAt(1, false);
	    	getModInfo_SongMessage().setText(Helpers.EMPTY_STING);
			getModInfo_InsSamTabbedPane().setEnabledAt(2, false);
		}
		else
		{
			String modFileName = currentMod.getFileName();
			int i = modFileName.lastIndexOf(File.separatorChar);
			if (i==-1) i = modFileName.lastIndexOf('/');
	    	getModInfo_Filename().setText(modFileName.substring(i+1));
	    	getModInfo_Filename().setCaretPosition(0); getModInfo_Filename().moveCaretPosition(0);
	    	getModInfo_Modname().setText(currentMod.getSongName());
	    	getModInfo_Modname().setCaretPosition(0); getModInfo_Modname().moveCaretPosition(0);
	    	getModInfo_Author().setText(currentMod.getAuthor());
	    	getModInfo_Author().setCaretPosition(0); getModInfo_Modname().moveCaretPosition(0);
	    	getModInfo_Trackername().setText(currentMod.getTrackerName());
	    	getModInfo_Trackername().setCaretPosition(0); getModInfo_Trackername().moveCaretPosition(0);
	    	getModInfo_FreqTable().setText(currentMod.getFrequencyTableString());
	    	getModInfo_FreqTable().setCaretPosition(0); getModInfo_FreqTable().moveCaretPosition(0);
	    	getModInfo_TempoMode().setText(ModConstants.TEMPOMODE_STRING[currentMod.getTempoMode()]);
	    	getModInfo_TempoMode().setCaretPosition(0); getModInfo_FreqTable().moveCaretPosition(0);
	    	getModInfo_Ticks().setText(Integer.toString(currentMod.getTempo()));
	    	getModInfo_Ticks().setCaretPosition(0); getModInfo_FreqTable().moveCaretPosition(0);
	    	getModInfo_BPM().setText(Integer.toString(currentMod.getBPMSpeed()));
	    	getModInfo_BPM().setCaretPosition(0); getModInfo_FreqTable().moveCaretPosition(0);
	    	getModInfo_IsStereo().setFixedState(currentMod.isStereo());
	    	getModInfo_AnzChannels().setText(Integer.toString(currentMod.getNChannels()));
	    	getModInfo_AnzChannels().setCaretPosition(0); getModInfo_AnzChannels().moveCaretPosition(0);  
	    	getModInfo_AnzPattern().setText(Integer.toString(currentMod.getNPattern()));
	    	getModInfo_AnzPattern().setCaretPosition(0); getModInfo_AnzPattern().moveCaretPosition(0);  

	    	final InstrumentsContainer instrumentContainer = currentMod.getInstrumentContainer(); 
	    	if (instrumentContainer!=null)
	    	{
		    	getModInfo_AnzSamples().setText(Integer.toString(currentMod.getNSamples()));
		    	getModInfo_AnzSamples().setCaretPosition(0); getModInfo_AnzSamples().moveCaretPosition(0);  
	    		getModInfo_Samples().setText(instrumentContainer.getSampleNames());
		    	getModInfo_Samples().setCaretPosition(0); getModInfo_Samples().moveCaretPosition(0);
	    		if (instrumentContainer.hasInstruments())
	    		{
	    			getModInfo_AnzInstruments().setText(Integer.toString(currentMod.getNInstruments()));
			    	getModInfo_AnzInstruments().setCaretPosition(0); getModInfo_AnzInstruments().moveCaretPosition(0);  
	    			getModInfo_Instruments().setText(instrumentContainer.getInstrumentNames());
			    	getModInfo_Instruments().setCaretPosition(0); getModInfo_Instruments().moveCaretPosition(0);
	    			getModInfo_InsSamTabbedPane().setEnabledAt(INSTRUMENT_INDEX, true);
	    			getModInfo_InsSamTabbedPane().setSelectedIndex(INSTRUMENT_INDEX);
	    		}
	    		else
	    		{
	    			getModInfo_AnzInstruments().setText("0");
			    	getModInfo_AnzInstruments().setCaretPosition(0); getModInfo_AnzInstruments().moveCaretPosition(0);  
	    			getModInfo_Instruments().setText(Helpers.EMPTY_STING);
	    			getModInfo_InsSamTabbedPane().setEnabledAt(INSTRUMENT_INDEX, false);
	    		}
	    	}

	    	final String songMessage = currentMod.getSongMessage();
	    	if (songMessage!=null && songMessage.length()>0)
	    	{
	    		getModInfo_SongMessage().setText(songMessage);
		    	getModInfo_SongMessage().setCaretPosition(0); getModInfo_SongMessage().moveCaretPosition(0);
    			getModInfo_InsSamTabbedPane().setEnabledAt(SONGMESSAGE_INDEX, true);
    			getModInfo_InsSamTabbedPane().setSelectedIndex(SONGMESSAGE_INDEX);
	    	}
	    	else
	    	{
	    		getModInfo_SongMessage().setText(Helpers.EMPTY_STING);
    			getModInfo_InsSamTabbedPane().setEnabledAt(SONGMESSAGE_INDEX, false);
	    	}
			
    		if (currentMod.getPatternContainer()!=null) getModPatternDialog().fillWithPatternArray(currentMod.getModType(), currentMod.getSongLength(), currentMod.getArrangement(), currentMod.getPatternContainer());
	    	if (currentMod.getInstrumentContainer()!=null)
	    	{
	    		getModSampleDialog().fillWithSamples(currentMod.getInstrumentContainer().getSamples());
	    		getModInstrumentDialog().fillWithInstrumentArray(currentMod.getInstrumentContainer().getInstruments());
	    	}
		}
	}
}
