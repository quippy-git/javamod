/*
 * @(#) ModInstrumentDialog.java
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
import java.util.ArrayList;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerListModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.quippy.javamod.main.gui.tools.FixedStateCheckBox;
import de.quippy.javamod.multimedia.mod.ModConstants;
import de.quippy.javamod.multimedia.mod.gui.EnvelopePanel.EnvelopeType;
import de.quippy.javamod.multimedia.mod.loader.instrument.Instrument;
import de.quippy.javamod.system.Helpers;

/**
 * @author Daniel Becker
 * @since 25.07.2020
 */
public class ModInstrumentDialog extends JDialog
{
	private static final long serialVersionUID = -5890906666611603247L;
	
	private JLabel labelSelectInstrument = null;
	private JSpinner selectInstrument = null;
	
	private JPanel instrumentNamePanel = null;
	private JLabel instrumentNameLabel = null;
	private JTextField instrumentName = null;
	private JLabel fileNameLabel = null;
	private JTextField fileName = null;
	private JPanel globalInfoPanel = null;

	private JPanel globalVolumePanel = null;
	private JLabel globalVolumeLabel = null;
	private JTextField globalVolume = null;
	private JLabel fadeOutVolumeLabel = null;
	private JTextField fadeOutVolume = null;
	private FixedStateCheckBox setPan = null;
	private JTextField setPanValue = null;

	private JPanel pitchPanSepPanel = null;
	private JLabel pitchPanSepLabel = null;
	private JTextField pitchPanSep = null;
	private JLabel pitchPanCenterLabel = null;
	private JTextField pitchPanCenter = null;
	
	private JPanel filterPanel = null;
	private FixedStateCheckBox setResonance = null;
	private JTextField resonanceValue = null;
	private FixedStateCheckBox setCutOff = null;
	private JTextField cutOffValue = null;
	
	private JPanel randomVariationPanel = null;
	private JLabel volumeVariationLabel = null;
	private JTextField volumeVariation = null;
	private JLabel panningVariationLabel = null;
	private JTextField panningVariation = null;
	private JLabel resonanceVariationLabel = null;
	private JTextField resonanceVariation = null;
	private JLabel cutOffVariationLabel = null;
	private JTextField cutOffVariation = null;

	private JPanel NNAPanel = null;
	private JLabel actionNNALabel = null;
	private JTextField actionNNA = null;
	private JLabel checkDNALabel = null;
	private JTextField checkDNA = null;
	private JLabel actionDNALabel = null;
	private JTextField actionDNA = null;

	private JPanel sampleMapPanel = null;
	private JTextArea sampleMap = null;

	private JTabbedPane envelopeTabbedPane = null;
	private EnvelopePanel volumeEnvelopePanel = null;
	private EnvelopePanel panningEnvelopePanel = null;
	private EnvelopePanel pitchEnvelopePanel = null;
	
	private Instrument [] instruments = null;
	

	/**
	 * Constructor for ModPatternDialog
	 */
	public ModInstrumentDialog()
	{
		super();
		initialize();
	}
	/**
	 * Constructor for ModPatternDialog
	 * @param owner
	 * @param modal
	 */
	public ModInstrumentDialog(JFrame owner, boolean modal)
	{
		super(owner, modal);
		initialize();
	}
	/**
	 * Constructor for ModPatternDialog
	 * @param owner
	 * @param modal
	 */
	public ModInstrumentDialog(JDialog owner, boolean modal)
	{
		super(owner, modal);
		initialize();
	}
	private void initialize()
	{
        final Container baseContentPane = getContentPane();
		baseContentPane.setLayout(new java.awt.GridBagLayout());

		baseContentPane.add(getLabelSelectInstrument(), 	Helpers.getGridBagConstraint(0, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		baseContentPane.add(getSelectInstrument(), 			Helpers.getGridBagConstraint(1, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		baseContentPane.add(getInstrumentNamePanel(), 		Helpers.getGridBagConstraint(2, 0, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		baseContentPane.add(getGlobalInfoPanel(), 			Helpers.getGridBagConstraint(0, 1, 2, 2, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.NORTHWEST, 0.0, 0.0));
		baseContentPane.add(getFilterPanel(), 				Helpers.getGridBagConstraint(2, 1, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.NORTHWEST, 0.0, 0.0));
		baseContentPane.add(getNNAPanel(), 					Helpers.getGridBagConstraint(3, 1, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.NORTHWEST, 0.0, 0.0));
		baseContentPane.add(getSampleMapPanel(),			Helpers.getGridBagConstraint(4, 1, 2, 0, java.awt.GridBagConstraints.BOTH, java.awt.GridBagConstraints.NORTHWEST, 0.0, 0.0));
		baseContentPane.add(getRandomVariationPanel(), 		Helpers.getGridBagConstraint(2, 2, 1, 2, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.NORTHWEST, 0.0, 0.0));
		baseContentPane.add(getTabbedPane(), 				Helpers.getGridBagConstraint(0, 3, 1, 0, java.awt.GridBagConstraints.BOTH, java.awt.GridBagConstraints.NORTHWEST, 1.0, 1.0));

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		addWindowListener(new java.awt.event.WindowAdapter()
		{
			@Override
			public void windowClosing(java.awt.event.WindowEvent e)
			{
				doClose();
			}
		});

		setName("Show mod instruments");
		setTitle("Show mod instruments");
		setResizable(true);
		setSize(640, 480);
		setPreferredSize(getSize());
        pack();
		setLocation(Helpers.getFrameCenteredLocation(this, getParent()));
	}
	public void doClose()
	{
		setVisible(false);
		dispose();
	}
	private JLabel getLabelSelectInstrument()
	{
		if (labelSelectInstrument==null)
		{
			labelSelectInstrument = new JLabel();
			labelSelectInstrument.setName("labelSelectInstrument");
			labelSelectInstrument.setText("Instrument:");
			labelSelectInstrument.setFont(Helpers.getDialogFont());
		}
		return labelSelectInstrument;
	}
	private JSpinner getSelectInstrument()
	{
		if (selectInstrument==null)
		{
			selectInstrument = new JSpinner();
			selectInstrument.setName("playerSetUp_Channels");
			selectInstrument.setFont(Helpers.getDialogFont());
			selectInstrument.setEnabled(true);
			final FontMetrics metrics = selectInstrument.getFontMetrics(Helpers.getDialogFont());
			final Dimension d = new Dimension(6 * metrics.charWidth('0'), metrics.getHeight()+5);
			selectInstrument.setSize(d);
			selectInstrument.setMinimumSize(d);
			selectInstrument.setMaximumSize(d);
			selectInstrument.setPreferredSize(d);

			selectInstrument.addChangeListener(new ChangeListener()
			{
				@Override
				public void stateChanged(ChangeEvent e)
				{
					if (instruments!=null)
					{
						Integer sampleIndex = (Integer)getSelectInstrument().getModel().getValue();
						fillWithInstrument(instruments[sampleIndex.intValue()-1]);
					}
				}
			});
		}
		return selectInstrument;
	}
	private JPanel getInstrumentNamePanel()
	{
		if (instrumentNamePanel==null)
		{
			instrumentNamePanel = new JPanel();
			instrumentNamePanel.setBorder(new TitledBorder(null, "Names", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, Helpers.getDialogFont(), null));
			instrumentNamePanel.setLayout(new GridBagLayout());
			instrumentNamePanel.add(getInstrumentNameLabel(), 		Helpers.getGridBagConstraint(0, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			instrumentNamePanel.add(getInstrumentName(), 			Helpers.getGridBagConstraint(1, 0, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
			instrumentNamePanel.add(getFileNameLabel(), 			Helpers.getGridBagConstraint(2, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			instrumentNamePanel.add(getFileName(), 					Helpers.getGridBagConstraint(3, 0, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
			
		}
		return instrumentNamePanel;
	}
	private JLabel getInstrumentNameLabel()
	{
		if (instrumentNameLabel==null)
		{
			instrumentNameLabel = new JLabel();
			instrumentNameLabel.setName("instrumentNameLabel");
			instrumentNameLabel.setText("Name");
			instrumentNameLabel.setFont(Helpers.getDialogFont());
		}
		return instrumentNameLabel;
	}
	private JTextField getInstrumentName()
	{
		if (instrumentName==null)
		{
			instrumentName = new JTextField();
			instrumentName.setName("instrumentName");
			instrumentName.setEditable(false);
			instrumentName.setFont(Helpers.getDialogFont());
		}
		return instrumentName;
	}
	private JLabel getFileNameLabel()
	{
		if (fileNameLabel==null)
		{
			fileNameLabel = new JLabel();
			fileNameLabel.setName("fileNameLabel");
			fileNameLabel.setText("File");
			fileNameLabel.setFont(Helpers.getDialogFont());
		}
		return fileNameLabel;
	}
	private JTextField getFileName()
	{
		if (fileName==null)
		{
			fileName = new JTextField();
			fileName.setName("fileName");
			fileName.setEditable(false);
			fileName.setFont(Helpers.getDialogFont());
		}
		return fileName;
	}
	private JPanel getGlobalInfoPanel()
	{
		if (globalInfoPanel==null)
		{
			globalInfoPanel = new JPanel();
			globalInfoPanel.setLayout(new GridBagLayout());
			globalInfoPanel.add(getGlobalVolumePanel(), 		Helpers.getGridBagConstraint(0, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.NORTHWEST, 0.0, 0.0));
			globalInfoPanel.add(getPitchPanSepPanel(), 			Helpers.getGridBagConstraint(0, 1, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.NORTHWEST, 0.0, 0.0));
		}
		return globalInfoPanel;
	}
	private JPanel getGlobalVolumePanel()
	{
		if (globalVolumePanel==null)
		{
			globalVolumePanel = new JPanel();
			globalVolumePanel.setBorder(new TitledBorder(null, "General", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, Helpers.getDialogFont(), null));
			globalVolumePanel.setLayout(new GridBagLayout());
			globalVolumePanel.add(getGlobalVolumeLabel(), 	Helpers.getGridBagConstraint(0, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			globalVolumePanel.add(getGlobalVolume(), 		Helpers.getGridBagConstraint(1, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			globalVolumePanel.add(getFadeOutVolumeLabel(), 	Helpers.getGridBagConstraint(0, 1, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			globalVolumePanel.add(getFadeOutVolume(), 		Helpers.getGridBagConstraint(1, 1, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			globalVolumePanel.add(getSetPan(), 				Helpers.getGridBagConstraint(0, 2, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			globalVolumePanel.add(getSetPanValue(), 		Helpers.getGridBagConstraint(1, 2, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		}
		return globalVolumePanel;
	}
	private JLabel getGlobalVolumeLabel()
	{
		if (globalVolumeLabel==null)
		{
			globalVolumeLabel = new JLabel();
			globalVolumeLabel.setName("globalVolumeLabel");
			globalVolumeLabel.setText("Global Volume");
			globalVolumeLabel.setFont(Helpers.getDialogFont());
		}
		return globalVolumeLabel;
	}
	private JTextField getGlobalVolume()
	{
		if (globalVolume==null)
		{
			globalVolume = new JTextField();
			globalVolume.setName("globalVolume");
			globalVolume.setEditable(false);
			globalVolume.setFont(Helpers.getDialogFont());
			final FontMetrics metrics = globalVolume.getFontMetrics(Helpers.getDialogFont());
			final Dimension d = new Dimension(8*metrics.charWidth('0'), metrics.getHeight());
			globalVolume.setSize(d);
			globalVolume.setMinimumSize(d);
			globalVolume.setMaximumSize(d);
			globalVolume.setPreferredSize(d);
		}
		return globalVolume;
	}
	private JLabel getFadeOutVolumeLabel()
	{
		if (fadeOutVolumeLabel==null)
		{
			fadeOutVolumeLabel = new JLabel();
			fadeOutVolumeLabel.setName("fadeOutVolumeLabel");
			fadeOutVolumeLabel.setText("Fade Out");
			fadeOutVolumeLabel.setFont(Helpers.getDialogFont());
		}
		return fadeOutVolumeLabel;
	}
	private JTextField getFadeOutVolume()
	{
		if (fadeOutVolume==null)
		{
			fadeOutVolume = new JTextField();
			fadeOutVolume.setName("fadeOutVolume");
			fadeOutVolume.setEditable(false);
			fadeOutVolume.setFont(Helpers.getDialogFont());
			final FontMetrics metrics = fadeOutVolume.getFontMetrics(Helpers.getDialogFont());
			final Dimension d = new Dimension(8*metrics.charWidth('0'), metrics.getHeight());
			fadeOutVolume.setSize(d);
			fadeOutVolume.setMinimumSize(d);
			fadeOutVolume.setMaximumSize(d);
			fadeOutVolume.setPreferredSize(d);
		}
		return fadeOutVolume;
	}
	private FixedStateCheckBox getSetPan()
	{
		if (setPan==null)
		{
			setPan = new FixedStateCheckBox();
			setPan.setName("setPan");
			setPan.setText("Set Pan");
			setPan.setFont(Helpers.getDialogFont());
		}
		return setPan;
	}
	private JTextField getSetPanValue()
	{
		if (setPanValue==null)
		{
			setPanValue = new JTextField();
			setPanValue.setName("setPanValue");
			setPanValue.setEditable(false);
			setPanValue.setFont(Helpers.getDialogFont());
			final FontMetrics metrics = setPanValue.getFontMetrics(Helpers.getDialogFont());
			final Dimension d = new Dimension(8*metrics.charWidth('0'), metrics.getHeight());
			setPanValue.setSize(d);
			setPanValue.setMinimumSize(d);
			setPanValue.setMaximumSize(d);
			setPanValue.setPreferredSize(d);
		}
		return setPanValue;
	}
	private JPanel getPitchPanSepPanel()
	{
		if (pitchPanSepPanel==null)
		{
			pitchPanSepPanel = new JPanel();
			pitchPanSepPanel.setBorder(new TitledBorder(null, "Pitch/Pan Separation", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, Helpers.getDialogFont(), null));
			pitchPanSepPanel.setLayout(new GridBagLayout());
			pitchPanSepPanel.add(getPitchPanSepLabel(), 	Helpers.getGridBagConstraint(0, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			pitchPanSepPanel.add(getPitchPanSep(), 			Helpers.getGridBagConstraint(1, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			pitchPanSepPanel.add(getPitchPanCenterLabel(), 	Helpers.getGridBagConstraint(2, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			pitchPanSepPanel.add(getPitchPanCenter(), 		Helpers.getGridBagConstraint(3, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		}
		return pitchPanSepPanel;
	}
	private JLabel getPitchPanSepLabel()
	{
		if (pitchPanSepLabel==null)
		{
			pitchPanSepLabel = new JLabel();
			pitchPanSepLabel.setName("pitchPanSepLabel");
			pitchPanSepLabel.setText("Sep");
			pitchPanSepLabel.setFont(Helpers.getDialogFont());
		}
		return pitchPanSepLabel;
	}
	private JTextField getPitchPanSep()
	{
		if (pitchPanSep==null)
		{
			pitchPanSep = new JTextField();
			pitchPanSep.setName("pitchPanSep");
			pitchPanSep.setEditable(false);
			pitchPanSep.setFont(Helpers.getDialogFont());
			final FontMetrics metrics = pitchPanSep.getFontMetrics(Helpers.getDialogFont());
			final Dimension d = new Dimension(5*metrics.charWidth('0'), metrics.getHeight());
			pitchPanSep.setSize(d);
			pitchPanSep.setMinimumSize(d);
			pitchPanSep.setMaximumSize(d);
			pitchPanSep.setPreferredSize(d);
		}
		return pitchPanSep;
	}
	private JLabel getPitchPanCenterLabel()
	{
		if (pitchPanCenterLabel==null)
		{
			pitchPanCenterLabel = new JLabel();
			pitchPanCenterLabel.setName("pitchPanCenterLabel");
			pitchPanCenterLabel.setText("Center");
			pitchPanCenterLabel.setFont(Helpers.getDialogFont());
		}
		return pitchPanCenterLabel;
	}
	private JTextField getPitchPanCenter()
	{
		if (pitchPanCenter==null)
		{
			pitchPanCenter = new JTextField();
			pitchPanCenter.setName("pitchPanSep");
			pitchPanCenter.setEditable(false);
			pitchPanCenter.setFont(Helpers.getDialogFont());
			final FontMetrics metrics = pitchPanCenter.getFontMetrics(Helpers.getDialogFont());
			final Dimension d = new Dimension(5*metrics.charWidth('0'), metrics.getHeight());
			pitchPanCenter.setSize(d);
			pitchPanCenter.setMinimumSize(d);
			pitchPanCenter.setMaximumSize(d);
			pitchPanCenter.setPreferredSize(d);
		}
		return pitchPanCenter;
	}
	private JPanel getFilterPanel()
	{
		if (filterPanel==null)
		{
			filterPanel = new JPanel();
			filterPanel.setBorder(new TitledBorder(null, "Filter", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, Helpers.getDialogFont(), null));
			filterPanel.setLayout(new GridBagLayout());
			filterPanel.add(getSetResonance(), 		Helpers.getGridBagConstraint(0, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			filterPanel.add(getResonanceValue(), 	Helpers.getGridBagConstraint(1, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			filterPanel.add(getSetCutOff(), 		Helpers.getGridBagConstraint(0, 1, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			filterPanel.add(getCutOffValue(), 		Helpers.getGridBagConstraint(1, 1, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		}
		return filterPanel;
	}
	private FixedStateCheckBox getSetResonance()
	{
		if (setResonance==null)
		{
			setResonance = new FixedStateCheckBox();
			setResonance.setName("setResonance");
			setResonance.setText("Resonance");
			setResonance.setFont(Helpers.getDialogFont());
		}
		return setResonance;
	}
	private JTextField getResonanceValue()
	{
		if (resonanceValue==null)
		{
			resonanceValue = new JTextField();
			resonanceValue.setName("resonanceValue");
			resonanceValue.setEditable(false);
			resonanceValue.setFont(Helpers.getDialogFont());
			final FontMetrics metrics = resonanceValue.getFontMetrics(Helpers.getDialogFont());
			final Dimension d = new Dimension(5*metrics.charWidth('0'), metrics.getHeight());
			resonanceValue.setSize(d);
			resonanceValue.setMinimumSize(d);
			resonanceValue.setMaximumSize(d);
			resonanceValue.setPreferredSize(d);
		}
		return resonanceValue;
	}
	private FixedStateCheckBox getSetCutOff()
	{
		if (setCutOff==null)
		{
			setCutOff = new FixedStateCheckBox();
			setCutOff.setName("setCutOff");
			setCutOff.setText("Cutoff");
			setCutOff.setFont(Helpers.getDialogFont());
		}
		return setCutOff;
	}
	private JTextField getCutOffValue()
	{
		if (cutOffValue==null)
		{
			cutOffValue = new JTextField();
			cutOffValue.setName("cutOffValue");
			cutOffValue.setEditable(false);
			cutOffValue.setFont(Helpers.getDialogFont());
			final FontMetrics metrics = cutOffValue.getFontMetrics(Helpers.getDialogFont());
			final Dimension d = new Dimension(5*metrics.charWidth('0'), metrics.getHeight());
			cutOffValue.setSize(d);
			cutOffValue.setMinimumSize(d);
			cutOffValue.setMaximumSize(d);
			cutOffValue.setPreferredSize(d);
		}
		return cutOffValue;
	}
	private JPanel getRandomVariationPanel()
	{
		if (randomVariationPanel==null)
		{
			randomVariationPanel = new JPanel();
			randomVariationPanel.setBorder(new TitledBorder(null, "Random Variation", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, Helpers.getDialogFont(), null));
			randomVariationPanel.setLayout(new GridBagLayout());
			randomVariationPanel.add(getVolumeVariationLabel(), 	Helpers.getGridBagConstraint(0, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			randomVariationPanel.add(getVolumeVariation(), 			Helpers.getGridBagConstraint(1, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			randomVariationPanel.add(getPanningVariationLabel(), 	Helpers.getGridBagConstraint(0, 1, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			randomVariationPanel.add(getPanningVariation(), 		Helpers.getGridBagConstraint(1, 1, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			randomVariationPanel.add(getResonanceVariationLabel(), 	Helpers.getGridBagConstraint(2, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			randomVariationPanel.add(getResonanceVariation(), 		Helpers.getGridBagConstraint(3, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			randomVariationPanel.add(getCutOffVariationLabel(), 	Helpers.getGridBagConstraint(2, 1, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			randomVariationPanel.add(getCutOffVariation(), 			Helpers.getGridBagConstraint(3, 1, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		}
		return randomVariationPanel;
	}
	private JLabel getVolumeVariationLabel()
	{
		if (volumeVariationLabel==null)
		{
			volumeVariationLabel = new JLabel();
			volumeVariationLabel.setName("volumeVariationLabel");
			volumeVariationLabel.setText("Volume");
			volumeVariationLabel.setFont(Helpers.getDialogFont());
		}
		return volumeVariationLabel;
	}
	private JTextField getVolumeVariation()
	{
		if (volumeVariation==null)
		{
			volumeVariation = new JTextField();
			volumeVariation.setName("volumeVariation");
			volumeVariation.setEditable(false);
			volumeVariation.setFont(Helpers.getDialogFont());
			final FontMetrics metrics = volumeVariation.getFontMetrics(Helpers.getDialogFont());
			final Dimension d = new Dimension(5*metrics.charWidth('0'), metrics.getHeight());
			volumeVariation.setSize(d);
			volumeVariation.setMinimumSize(d);
			volumeVariation.setMaximumSize(d);
			volumeVariation.setPreferredSize(d);
		}
		return volumeVariation;
	}
	private JLabel getPanningVariationLabel()
	{
		if (panningVariationLabel==null)
		{
			panningVariationLabel = new JLabel();
			panningVariationLabel.setName("panningVariationLabel");
			panningVariationLabel.setText("Panning");
			panningVariationLabel.setFont(Helpers.getDialogFont());
		}
		return panningVariationLabel;
	}
	private JTextField getPanningVariation()
	{
		if (panningVariation==null)
		{
			panningVariation = new JTextField();
			panningVariation.setName("panningVariation");
			panningVariation.setEditable(false);
			panningVariation.setFont(Helpers.getDialogFont());
			final FontMetrics metrics = panningVariation.getFontMetrics(Helpers.getDialogFont());
			final Dimension d = new Dimension(5*metrics.charWidth('0'), metrics.getHeight());
			panningVariation.setSize(d);
			panningVariation.setMinimumSize(d);
			panningVariation.setMaximumSize(d);
			panningVariation.setPreferredSize(d);
		}
		return panningVariation;
	}
	private JLabel getResonanceVariationLabel()
	{
		if (resonanceVariationLabel==null)
		{
			resonanceVariationLabel = new JLabel();
			resonanceVariationLabel.setName("resonanceVariationLabel");
			resonanceVariationLabel.setText("Resonance");
			resonanceVariationLabel.setFont(Helpers.getDialogFont());
			resonanceVariationLabel.setEnabled(false);
		}
		return resonanceVariationLabel;
	}
	private JTextField getResonanceVariation()
	{
		if (resonanceVariation==null)
		{
			resonanceVariation = new JTextField();
			resonanceVariation.setName("resonanceVariation");
			resonanceVariation.setEditable(false);
			resonanceVariation.setFont(Helpers.getDialogFont());
			final FontMetrics metrics = resonanceVariation.getFontMetrics(Helpers.getDialogFont());
			final Dimension d = new Dimension(5*metrics.charWidth('0'), metrics.getHeight());
			resonanceVariation.setSize(d);
			resonanceVariation.setMinimumSize(d);
			resonanceVariation.setMaximumSize(d);
			resonanceVariation.setPreferredSize(d);
			resonanceVariation.setEnabled(false);
		}
		return resonanceVariation;
	}
	private JLabel getCutOffVariationLabel()
	{
		if (cutOffVariationLabel==null)
		{
			cutOffVariationLabel = new JLabel();
			cutOffVariationLabel.setName("cutOffVariationLabel");
			cutOffVariationLabel.setText("Cutoff");
			cutOffVariationLabel.setFont(Helpers.getDialogFont());
			cutOffVariationLabel.setEnabled(false);
		}
		return cutOffVariationLabel;
	}
	private JTextField getCutOffVariation()
	{
		if (cutOffVariation==null)
		{
			cutOffVariation = new JTextField();
			cutOffVariation.setName("cutOffVariation");
			cutOffVariation.setEditable(false);
			cutOffVariation.setFont(Helpers.getDialogFont());
			final FontMetrics metrics = cutOffVariation.getFontMetrics(Helpers.getDialogFont());
			final Dimension d = new Dimension(5*metrics.charWidth('0'), metrics.getHeight());
			cutOffVariation.setSize(d);
			cutOffVariation.setMinimumSize(d);
			cutOffVariation.setMaximumSize(d);
			cutOffVariation.setPreferredSize(d);
			cutOffVariation.setEnabled(false);
		}
		return cutOffVariation;
	}
	private JPanel getNNAPanel()
	{
		if (NNAPanel==null)
		{
			NNAPanel = new JPanel();
			NNAPanel.setBorder(new TitledBorder(null, "New Note Action", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, Helpers.getDialogFont(), null));
			NNAPanel.setLayout(new GridBagLayout());
			NNAPanel.add(getActionNNALabel(), 	Helpers.getGridBagConstraint(0, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			NNAPanel.add(getActionNNA(), 		Helpers.getGridBagConstraint(1, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			NNAPanel.add(getCheckDNALabel(), 	Helpers.getGridBagConstraint(0, 1, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			NNAPanel.add(getCheckDNA(), 		Helpers.getGridBagConstraint(1, 1, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			NNAPanel.add(getActionDNALabel(), 	Helpers.getGridBagConstraint(0, 2, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			NNAPanel.add(getActionDNA(), 		Helpers.getGridBagConstraint(1, 2, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		}
		return NNAPanel;
	}
	private JLabel getActionNNALabel()
	{
		if (actionNNALabel==null)
		{
			actionNNALabel = new JLabel();
			actionNNALabel.setName("actionNNALabel");
			actionNNALabel.setText("Action");
			actionNNALabel.setFont(Helpers.getDialogFont());
		}
		return actionNNALabel;
	}
	private JTextField getActionNNA()
	{
		if (actionNNA==null)
		{
			actionNNA = new JTextField();
			actionNNA.setName("actionNNA");
			actionNNA.setEditable(false);
			actionNNA.setFont(Helpers.getDialogFont());
			final FontMetrics metrics = actionNNA.getFontMetrics(Helpers.getDialogFont());
			final Dimension d = new Dimension(10*metrics.charWidth('0'), metrics.getHeight());
			actionNNA.setSize(d);
			actionNNA.setMinimumSize(d);
			actionNNA.setMaximumSize(d);
			actionNNA.setPreferredSize(d);
		}
		return actionNNA;
	}
	private JLabel getCheckDNALabel()
	{
		if (checkDNALabel==null)
		{
			checkDNALabel = new JLabel();
			checkDNALabel.setName("checkDNALabel");
			checkDNALabel.setText("Duplicate Check");
			checkDNALabel.setFont(Helpers.getDialogFont());
		}
		return checkDNALabel;
	}
	private JTextField getCheckDNA()
	{
		if (checkDNA==null)
		{
			checkDNA = new JTextField();
			checkDNA.setName("checkDNA");
			checkDNA.setEditable(false);
			checkDNA.setFont(Helpers.getDialogFont());
			final FontMetrics metrics = checkDNA.getFontMetrics(Helpers.getDialogFont());
			final Dimension d = new Dimension(10*metrics.charWidth('0'), metrics.getHeight());
			checkDNA.setSize(d);
			checkDNA.setMinimumSize(d);
			checkDNA.setMaximumSize(d);
			checkDNA.setPreferredSize(d);
		}
		return checkDNA;
	}
	private JLabel getActionDNALabel()
	{
		if (actionDNALabel==null)
		{
			actionDNALabel = new JLabel();
			actionDNALabel.setName("actionDNALabel");
			actionDNALabel.setText("Duplicate Action");
			actionDNALabel.setFont(Helpers.getDialogFont());
		}
		return actionDNALabel;
	}
	private JTextField getActionDNA()
	{
		if (actionDNA==null)
		{
			actionDNA = new JTextField();
			actionDNA.setName("actionDNA");
			actionDNA.setEditable(false);
			actionDNA.setFont(Helpers.getDialogFont());
			final FontMetrics metrics = actionDNA.getFontMetrics(Helpers.getDialogFont());
			final Dimension d = new Dimension(10*metrics.charWidth('0'), metrics.getHeight());
			actionDNA.setSize(d);
			actionDNA.setMinimumSize(d);
			actionDNA.setMaximumSize(d);
			actionDNA.setPreferredSize(d);
		}
		return actionDNA;
	}
	private JPanel getSampleMapPanel()
	{
		if (sampleMapPanel==null)
		{
			sampleMapPanel = new JPanel();
			sampleMapPanel.setBorder(new TitledBorder(null, "Sampe/Note Map", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, Helpers.getDialogFont(), null));
			sampleMapPanel.setLayout(new GridBagLayout());
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setViewportView(getSampleMap());
			sampleMapPanel.add(scrollPane, 	Helpers.getGridBagConstraint(0, 0, 1, 1, java.awt.GridBagConstraints.BOTH, java.awt.GridBagConstraints.WEST, 1.0, 1.0));

		}
		return sampleMapPanel;
	}
	private JTextArea getSampleMap()
	{
		if (sampleMap==null)
		{
			sampleMap = new JTextArea();
			sampleMap.setFont(Helpers.getTextAreaFont());
		}
		return sampleMap;
	}
	private JTabbedPane getTabbedPane()
	{
		if (envelopeTabbedPane==null)
		{
			envelopeTabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
			envelopeTabbedPane.setFont(Helpers.getDialogFont());
			envelopeTabbedPane.add("Volume", getVolumeEnvelopePanel());
			envelopeTabbedPane.add("Panning", getPanningEnvelopePanel());
			envelopeTabbedPane.add("Pitch/Filter", getPitchEnvelopePanel());
		}
		return envelopeTabbedPane;
	}
	private EnvelopePanel getVolumeEnvelopePanel()
	{
		if (volumeEnvelopePanel==null)
		{
			volumeEnvelopePanel = new EnvelopePanel();
		}
		return volumeEnvelopePanel;
	}
	private EnvelopePanel getPanningEnvelopePanel()
	{
		if (panningEnvelopePanel==null)
		{
			panningEnvelopePanel = new EnvelopePanel();
		}
		return panningEnvelopePanel;
	}
	private EnvelopePanel getPitchEnvelopePanel()
	{
		if (pitchEnvelopePanel==null)
		{
			pitchEnvelopePanel = new EnvelopePanel();
		}
		return pitchEnvelopePanel;
	}
	private String getNNAActionString(final int nna)
	{
		switch (nna)
		{
			case -1:
				return "-1";
			case ModConstants.NNA_CONTINUE:
				return "Continue";
			case ModConstants.NNA_CUT:
				return "Note Cut";
			case ModConstants.NNA_FADE:
				return "Note Fade";
			case ModConstants.NNA_OFF:
				return "Note Off";
		}
		return "? (ERROR)";
	}
	private String getDNACheckString(final int dnacheck)
	{
		switch (dnacheck)
		{
			case -1:
				return "-1";
			case ModConstants.DCT_NONE:
				return "Disabled";
			case ModConstants.DCT_INSTRUMENT:
				return "Instrument";
			case ModConstants.DCT_NOTE:
				return "Note";
			case ModConstants.DCT_PLUGIN:
				return "Plugin";
			case ModConstants.DCT_SAMPLE:
				return "Sample";
		}
		return "? (ERROR)";
	}
	private String getDNAActionString(final int dna)
	{
		switch (dna)
		{
			case -1:
				return "-1";
			case ModConstants.DNA_CUT:
				return "Note Cut";
			case ModConstants.DNA_FADE:
				return "Note Fade";
			case ModConstants.DNA_OFF:
				return "Note Off";
		}
		return "? (ERROR)";
	}
	private String getSampleMapString(int [] noteIndex, int [] sampleIndex)
	{
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<noteIndex.length; i++)
		{
			sb.append(ModConstants.getNoteNameForIndex(i+1)).append(" | ");
			if ((noteIndex[i]&0x80)!=0)
			{
				sb.append("...").append(" | ");
				sb.append("..").append('\n');
			}
			else
			{
				sb.append(ModConstants.getNoteNameForIndex(noteIndex[i]+1)).append(" | ");
				sb.append(sampleIndex[i]).append('\n');
			}
		}
		return sb.toString();
	}
	private void clearInstrument()
	{
		getInstrumentName().setText(Helpers.EMPTY_STING);
		getFileName().setText(Helpers.EMPTY_STING);
		
		getGlobalVolume().setText(Helpers.EMPTY_STING);
		getFadeOutVolume().setText(Helpers.EMPTY_STING);
		getSetPan().setFixedState(false);
		getSetPanValue().setText(Helpers.EMPTY_STING);
		
		getPitchPanSep().setText(Helpers.EMPTY_STING);
		getPitchPanCenter().setText(Helpers.EMPTY_STING);

		getSetResonance().setFixedState(false);
		getResonanceValue().setText(Helpers.EMPTY_STING);
		getSetCutOff().setFixedState(false);
		getCutOffValue().setText(Helpers.EMPTY_STING);
		
		getVolumeVariation().setText(Helpers.EMPTY_STING);
		getPanningVariation().setText(Helpers.EMPTY_STING);
		getResonanceVariation().setText(Helpers.EMPTY_STING);
		getCutOffVariation().setText(Helpers.EMPTY_STING);
		
		getActionNNA().setText(Helpers.EMPTY_STING);
		getCheckDNA().setText(Helpers.EMPTY_STING);
		getActionDNA().setText(Helpers.EMPTY_STING);
		
		getSampleMap().setText(Helpers.EMPTY_STING);
		getSampleMap().select(0,0);
		
		getVolumeEnvelopePanel().setEnvelope(null, EnvelopeType.volume);
		getPanningEnvelopePanel().setEnvelope(null, EnvelopeType.panning);
		getPitchEnvelopePanel().setEnvelope(null, EnvelopeType.pitch);
	}
	private void fillWithInstrument(Instrument instrument)
	{
		getInstrumentName().setText(instrument.name);
		getFileName().setText(instrument.dosFileName);
		
		getGlobalVolume().setText(Integer.toString(instrument.globalVolume));
		getFadeOutVolume().setText(Integer.toString(instrument.volumeFadeOut));
		getSetPan().setFixedState(instrument.defaultPan!=-1);
		getSetPanValue().setText(Integer.toString(instrument.defaultPan));
		
		getPitchPanSep().setText(Integer.toString(instrument.pitchPanSeparation));
		getPitchPanCenter().setText(ModConstants.getNoteNameForIndex(instrument.pitchPanCenter + 1));

		if (instrument.initialFilterResonance!=-1)
		{
			getSetResonance().setFixedState((instrument.initialFilterResonance&0x80)!=0);
			getResonanceValue().setText(Integer.toString(instrument.initialFilterResonance&0x7F));
		}
		else
		{
			getSetResonance().setFixedState(false);
			getResonanceValue().setText("-1");
		}
		if (instrument.initialFilterCutoff!=-1)
		{
			getSetCutOff().setFixedState((instrument.initialFilterCutoff&0x80)!=0);
			getCutOffValue().setText(Integer.toString(instrument.initialFilterCutoff&0x7F));
		}
		else
		{
			getSetCutOff().setFixedState(false);
			getCutOffValue().setText("-1");
		}
		
		getVolumeVariation().setText(Integer.toString(instrument.randomVolumeVariation));
		getPanningVariation().setText(Integer.toString(instrument.randomPanningVariation));
		getResonanceVariation().setText(Integer.toString(instrument.randomResonanceVariation));
		getCutOffVariation().setText(Integer.toString(instrument.randomCutOffVariation));
		
		getActionNNA().setText(getNNAActionString(instrument.NNA));
		getCheckDNA().setText(getDNACheckString(instrument.dublicateNoteCheck));
		getActionDNA().setText(getDNAActionString(instrument.dublicateNoteAction));
		
		getSampleMap().setText(getSampleMapString(instrument.noteIndex, instrument.sampleIndex));
		getSampleMap().select(0,0);
		
		getVolumeEnvelopePanel().setEnvelope(instrument.volumeEnvelope, EnvelopeType.volume);
		getPanningEnvelopePanel().setEnvelope(instrument.panningEnvelope, EnvelopeType.panning);
		getPitchEnvelopePanel().setEnvelope(instrument.pitchEnvelope, EnvelopeType.pitch);
	}
	public void fillWithInstrumentArray(final Instrument [] instruments)
	{
		this.instruments = instruments;
		if (instruments!=null)
		{
			ArrayList<Integer> list = new ArrayList<Integer>(instruments.length);
			for (int i=0; i<instruments.length; i++) list.add(Integer.valueOf(i+1));
			getSelectInstrument().setModel(new SpinnerListModel(list));
			if (instruments[0]!=null) fillWithInstrument(instruments[0]);
		}
		else
			clearInstrument();
	}
}
