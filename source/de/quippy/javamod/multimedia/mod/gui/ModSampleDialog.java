/*
 * @(#) ModSampleDialog.java
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
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerListModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.quippy.javamod.main.gui.tools.FixedStateCheckBox;
import de.quippy.javamod.multimedia.mod.ModConstants;
import de.quippy.javamod.multimedia.mod.loader.instrument.Sample;
import de.quippy.javamod.system.Helpers;

/**
 * @author Daniel Becker
 * @since 25.07.2020
 */
public class ModSampleDialog extends JDialog
{
	private static final long serialVersionUID = -9058637708283713743L;

	private JLabel labelSelectSample = null;
	private JSpinner selectSample = null;
	private JTextField sampleType = null;
	private SampleImagePanel imageBufferPanel = null;
	private JPanel sampleNameAndLoopsPanel = null;
	private JPanel sampleNamePanel = null;
	private JLabel sampleNameLabel = null;
	private JTextField sampleName = null;
	private JLabel dosFileNameLabel = null;
	private JTextField dosFileName = null;
	private JPanel volumePanel = null;
	private JPanel loopPanel = null;
	private JPanel sustainLoopPanel = null;
	private JPanel autoVibratoPanel = null;
	private JLabel defaultVolumeLabel = null;
	private JTextField defaultVolume = null;
	private JLabel globalVolumeLabel = null;
	private JTextField globalVolume = null;
	private FixedStateCheckBox setPan = null;
	private JTextField setPanValue = null;
	private JLabel finetuneLabel = null;
	private JTextField fineTuneValue = null;
	private JLabel baseFreqLabel = null;
	private JTextField baseFreqValue = null;
	private JLabel transposeLabel = null;
	private JTextField transposeValue = null;
	private JLabel loopTypeLabel = null;
	private JTextField loopTypeValue = null;
	private JLabel loopStartLabel = null;
	private JTextField loopStartValue = null;
	private JLabel loopEndLabel = null;
	private JTextField loopEndValue = null;
	private JLabel sustainLoopTypeLabel = null;
	private JTextField sustainLoopTypeValue = null;
	private JLabel sustainLoopStartLabel = null;
	private JTextField sustainLoopStartValue = null;
	private JLabel sustainLoopEndLabel = null;
	private JTextField sustainLoopEndValue = null;
	private JLabel autoVibTypeLabel = null;
	private JTextField autoVibTypeValue = null;
	private JLabel autoVibDepthLabel = null;
	private JTextField autoVibDepthValue = null;
	private JLabel autoVibSweepLabel = null;
	private JTextField autoVibSweepValue = null;
	private JLabel autoVibRateLabel = null;
	private JTextField autoVibRateValue = null;

	private Sample [] samples;

	/**
	 * Constructor for ModPatternDialog
	 */
	public ModSampleDialog()
	{
		super();
		initialize();
	}
	/**
	 * Constructor for ModPatternDialog
	 * @param owner
	 * @param modal
	 */
	public ModSampleDialog(JFrame owner, boolean modal)
	{
		super(owner, modal);
		initialize();
	}
	/**
	 * Constructor for ModPatternDialog
	 * @param owner
	 * @param modal
	 */
	public ModSampleDialog(JDialog owner, boolean modal)
	{
		super(owner, modal);
		initialize();
	}
	private void initialize()
	{
        final Container baseContentPane = getContentPane();
		baseContentPane.setLayout(new java.awt.GridBagLayout());
		
		baseContentPane.add(getLabelSelectSample(), 		Helpers.getGridBagConstraint(0, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		baseContentPane.add(getSelectSample(), 				Helpers.getGridBagConstraint(1, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		baseContentPane.add(getSampleNameAndLoopsPanel(),	Helpers.getGridBagConstraint(2, 0, 2, 0, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		baseContentPane.add(getVolumePanel(),				Helpers.getGridBagConstraint(0, 1, 2, 2, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		baseContentPane.add(getImageBufferPanel(), 			Helpers.getGridBagConstraint(0, 3, 1, 0, java.awt.GridBagConstraints.BOTH, java.awt.GridBagConstraints.WEST, 1.0, 1.0));
		
		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		addWindowListener(new java.awt.event.WindowAdapter()
		{
			@Override
			public void windowClosing(java.awt.event.WindowEvent e)
			{
				doClose();
			}
		});

		setName("Show mod samples");
		setTitle("Show mod samples");
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
	private JLabel getLabelSelectSample()
	{
		if (labelSelectSample==null)
		{
			labelSelectSample = new JLabel();
			labelSelectSample.setName("labelSelectSample");
			labelSelectSample.setText("Sample:");
			labelSelectSample.setFont(Helpers.getDialogFont());
		}
		return labelSelectSample;
	}
	private JSpinner getSelectSample()
	{
		if (selectSample==null)
		{
			selectSample = new JSpinner();
			selectSample.setName("playerSetUp_Channels");
			selectSample.setFont(Helpers.getDialogFont());
			selectSample.setEnabled(true);
			final FontMetrics metrics = selectSample.getFontMetrics(Helpers.getDialogFont());
			final Dimension d = new Dimension(6 * metrics.charWidth('0'), metrics.getHeight()+5);
			selectSample.setSize(d);
			selectSample.setMinimumSize(d);
			selectSample.setMaximumSize(d);
			selectSample.setPreferredSize(d);

			selectSample.addChangeListener(new ChangeListener()
			{
				@Override
				public void stateChanged(ChangeEvent e)
				{
					if (samples!=null)
					{
						Integer sampleIndex = (Integer)getSelectSample().getModel().getValue();
						fillWithSample(samples[sampleIndex.intValue()-1]);
					}
				}
			});
		}
		return selectSample;
	}
	private JTextField getSampleType()
	{
		if (sampleType==null)
		{
			sampleType = new JTextField();
			sampleType.setName("sampleType");
			sampleType.setEditable(false);
			sampleType.setFont(Helpers.getDialogFont());
//			final FontMetrics metrics = sampleType.getFontMetrics(Helpers.getDialogFont());
//			final Dimension d = new Dimension(8*metrics.charWidth('0'), metrics.getHeight());
//			sampleType.setSize(d);
//			sampleType.setMinimumSize(d);
//			sampleType.setMaximumSize(d);
//			sampleType.setPreferredSize(d);
		}
		return sampleType;
	}
	private JPanel getSampleNameAndLoopsPanel()
	{
		if (sampleNameAndLoopsPanel==null)
		{
			sampleNameAndLoopsPanel = new JPanel();
			sampleNameAndLoopsPanel.setLayout(new GridBagLayout());
			sampleNameAndLoopsPanel.add(getSampleType(),		Helpers.getGridBagConstraint(0, 0, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			sampleNameAndLoopsPanel.add(getSampleNamePanel(),	Helpers.getGridBagConstraint(0, 1, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			sampleNameAndLoopsPanel.add(getLoopPanel(),			Helpers.getGridBagConstraint(1, 2, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.NORTHWEST, 0.0, 0.0));
			sampleNameAndLoopsPanel.add(getSustainLoopPanel(),	Helpers.getGridBagConstraint(2, 2, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.NORTHWEST, 0.0, 0.0));
			sampleNameAndLoopsPanel.add(getAutoVibratoPanel(),	Helpers.getGridBagConstraint(3, 2, 1, 0, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.NORTHWEST, 0.0, 0.0));
		}
		return sampleNameAndLoopsPanel;
	}
	private JPanel getSampleNamePanel()
	{
		if (sampleNamePanel==null)
		{
			sampleNamePanel = new JPanel();
			sampleNamePanel.setBorder(new TitledBorder(null, "Names", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, Helpers.getDialogFont(), null));
			sampleNamePanel.setLayout(new GridBagLayout());
			sampleNamePanel.add(getSampleNameLabel(),	Helpers.getGridBagConstraint(0, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			sampleNamePanel.add(getSampleName(),		Helpers.getGridBagConstraint(1, 0, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
			sampleNamePanel.add(getDosFileNameLabel(),	Helpers.getGridBagConstraint(2, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			sampleNamePanel.add(getDosFileName(),		Helpers.getGridBagConstraint(3, 0, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		}
		return sampleNamePanel;
	}

	private JLabel getSampleNameLabel()
	{
		if (sampleNameLabel==null)
		{
			sampleNameLabel = new JLabel();
			sampleNameLabel.setName("sampleNameLabel");
			sampleNameLabel.setText("Name");
			sampleNameLabel.setFont(Helpers.getDialogFont());
		}
		return sampleNameLabel;
	}
	private JTextField getSampleName()
	{
		if (sampleName==null)
		{
			sampleName = new JTextField();
			sampleName.setName("sampleName");
			sampleName.setEditable(false);
			sampleName.setFont(Helpers.getDialogFont());
		}
		return sampleName;
	}
	private JLabel getDosFileNameLabel()
	{
		if (dosFileNameLabel==null)
		{
			dosFileNameLabel = new JLabel();
			dosFileNameLabel.setName("dosFileNameLabel");
			dosFileNameLabel.setText("File");
			dosFileNameLabel.setFont(Helpers.getDialogFont());
		}
		return dosFileNameLabel;
	}
	private JTextField getDosFileName()
	{
		if (dosFileName==null)
		{
			dosFileName = new JTextField();
			dosFileName.setName("dosFileName");
			dosFileName.setEditable(false);
			dosFileName.setFont(Helpers.getDialogFont());
		}
		return dosFileName;
	}
	private JPanel getVolumePanel()
	{
		if (volumePanel==null)
		{
			volumePanel = new JPanel();
			volumePanel.setBorder(new TitledBorder(null, "Volume / Finetune", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, Helpers.getDialogFont(), null));
			volumePanel.setLayout(new GridBagLayout());
			volumePanel.add(getDefaultVolumeLabel(), Helpers.getGridBagConstraint(0, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			volumePanel.add(getDefaultVolume(), 	 Helpers.getGridBagConstraint(1, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			volumePanel.add(getGlobalVolumeLabel(),  Helpers.getGridBagConstraint(0, 1, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			volumePanel.add(getGlobalVolume(), 	 	 Helpers.getGridBagConstraint(1, 1, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			volumePanel.add(getSetPan(),  			 Helpers.getGridBagConstraint(0, 2, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			volumePanel.add(getSetPanValue(),	 	 Helpers.getGridBagConstraint(1, 2, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			volumePanel.add(getFinetuneLabel(),		 Helpers.getGridBagConstraint(0, 3, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			volumePanel.add(getFineTuneValue(),	 	 Helpers.getGridBagConstraint(1, 3, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			volumePanel.add(getBaseFreqLabel(),		 Helpers.getGridBagConstraint(0, 4, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			volumePanel.add(getBaseFreqValue(),	 	 Helpers.getGridBagConstraint(1, 4, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			volumePanel.add(getTransposeLabel(),	 Helpers.getGridBagConstraint(0, 5, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			volumePanel.add(getTransposeValue(), 	 Helpers.getGridBagConstraint(1, 5, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		}
		return volumePanel;
	}
	private JLabel getDefaultVolumeLabel()
	{
		if (defaultVolumeLabel==null)
		{
			defaultVolumeLabel = new JLabel();
			defaultVolumeLabel.setName("defaultVolumeLabel");
			defaultVolumeLabel.setText("Default Volume");
			defaultVolumeLabel.setFont(Helpers.getDialogFont());
		}
		return defaultVolumeLabel;
	}
	private JTextField getDefaultVolume()
	{
		if (defaultVolume==null)
		{
			defaultVolume = new JTextField();
			defaultVolume.setName("defaultVolume");
			defaultVolume.setEditable(false);
			defaultVolume.setFont(Helpers.getDialogFont());
			final FontMetrics metrics = defaultVolume.getFontMetrics(Helpers.getDialogFont());
			final Dimension d = new Dimension(8*metrics.charWidth('0'), metrics.getHeight());
			defaultVolume.setSize(d);
			defaultVolume.setMinimumSize(d);
			defaultVolume.setMaximumSize(d);
			defaultVolume.setPreferredSize(d);
		}
		return defaultVolume;
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
			setPanValue.setName("globalVolume");
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
	private JLabel getFinetuneLabel()
	{
		if (finetuneLabel==null)
		{
			finetuneLabel = new JLabel();
			finetuneLabel.setName("globalVolumeLabel");
			finetuneLabel.setText("Finetune");
			finetuneLabel.setFont(Helpers.getDialogFont());
		}
		return finetuneLabel;
	}
	private JTextField getFineTuneValue()
	{
		if (fineTuneValue==null)
		{
			fineTuneValue = new JTextField();
			fineTuneValue.setName("fineTuneValue");
			fineTuneValue.setEditable(false);
			fineTuneValue.setFont(Helpers.getDialogFont());
			final FontMetrics metrics = fineTuneValue.getFontMetrics(Helpers.getDialogFont());
			final Dimension d = new Dimension(8*metrics.charWidth('0'), metrics.getHeight());
			fineTuneValue.setSize(d);
			fineTuneValue.setMinimumSize(d);
			fineTuneValue.setMaximumSize(d);
			fineTuneValue.setPreferredSize(d);
		}
		return fineTuneValue;
	}
	private JLabel getBaseFreqLabel()
	{
		if (baseFreqLabel==null)
		{
			baseFreqLabel = new JLabel();
			baseFreqLabel.setName("baseFreqLabel");
			baseFreqLabel.setText("Base Freq.");
			baseFreqLabel.setFont(Helpers.getDialogFont());
		}
		return baseFreqLabel;
	}
	private JTextField getBaseFreqValue()
	{
		if (baseFreqValue==null)
		{
			baseFreqValue = new JTextField();
			baseFreqValue.setName("baseFreqValue");
			baseFreqValue.setEditable(false);
			baseFreqValue.setFont(Helpers.getDialogFont());
			final FontMetrics metrics = baseFreqValue.getFontMetrics(Helpers.getDialogFont());
			final Dimension d = new Dimension(8*metrics.charWidth('0'), metrics.getHeight());
			baseFreqValue.setSize(d);
			baseFreqValue.setMinimumSize(d);
			baseFreqValue.setMaximumSize(d);
			baseFreqValue.setPreferredSize(d);
		}
		return baseFreqValue;
	}
	private JLabel getTransposeLabel()
	{
		if (transposeLabel==null)
		{
			transposeLabel = new JLabel();
			transposeLabel.setName("transposeLabel");
			transposeLabel.setText("Transpose");
			transposeLabel.setFont(Helpers.getDialogFont());
		}
		return transposeLabel;
	}
	private JTextField getTransposeValue()
	{
		if (transposeValue==null)
		{
			transposeValue = new JTextField();
			transposeValue.setName("transposeValue");
			transposeValue.setEditable(false);
			transposeValue.setFont(Helpers.getDialogFont());
			final FontMetrics metrics = transposeValue.getFontMetrics(Helpers.getDialogFont());
			final Dimension d = new Dimension(8*metrics.charWidth('0'), metrics.getHeight());
			transposeValue.setSize(d);
			transposeValue.setMinimumSize(d);
			transposeValue.setMaximumSize(d);
			transposeValue.setPreferredSize(d);
		}
		return transposeValue;
	}
	private JPanel getLoopPanel()
	{
		if (loopPanel==null)
		{
			loopPanel = new JPanel();
			loopPanel.setBorder(new TitledBorder(null, "Loop", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, Helpers.getDialogFont(), null));
			loopPanel.setLayout(new GridBagLayout());
			loopPanel.add(getLoopTypeLabel(),  Helpers.getGridBagConstraint(0, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			loopPanel.add(getLoopTypeValue(),  Helpers.getGridBagConstraint(1, 0, 1, 0, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			loopPanel.add(getLoopStartLabel(), Helpers.getGridBagConstraint(0, 1, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			loopPanel.add(getLoopStartValue(), Helpers.getGridBagConstraint(1, 1, 1, 0, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			loopPanel.add(getLoopEndLabel(),   Helpers.getGridBagConstraint(0, 2, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			loopPanel.add(getLoopEndValue(),   Helpers.getGridBagConstraint(1, 2, 1, 0, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		}
		return loopPanel;
	}
	private JLabel getLoopTypeLabel()
	{
		if (loopTypeLabel==null)
		{
			loopTypeLabel = new JLabel();
			loopTypeLabel.setName("loopTypeLabel");
			loopTypeLabel.setText("Type");
			loopTypeLabel.setFont(Helpers.getDialogFont());
		}
		return loopTypeLabel;
	}
	private JTextField getLoopTypeValue()
	{
		if (loopTypeValue==null)
		{
			loopTypeValue = new JTextField();
			loopTypeValue.setName("loopTypeValue");
			loopTypeValue.setEditable(false);
			loopTypeValue.setFont(Helpers.getDialogFont());
			final FontMetrics metrics = loopTypeValue.getFontMetrics(Helpers.getDialogFont());
			final Dimension d = new Dimension(8*metrics.charWidth('0'), metrics.getHeight());
			loopTypeValue.setSize(d);
			loopTypeValue.setMinimumSize(d);
			loopTypeValue.setMaximumSize(d);
			loopTypeValue.setPreferredSize(d);
		}
		return loopTypeValue;
	}
	private JLabel getLoopStartLabel()
	{
		if (loopStartLabel==null)
		{
			loopStartLabel = new JLabel();
			loopStartLabel.setName("loopStartLabel");
			loopStartLabel.setText("Start");
			loopStartLabel.setFont(Helpers.getDialogFont());
		}
		return loopStartLabel;
	}
	private JTextField getLoopStartValue()
	{
		if (loopStartValue==null)
		{
			loopStartValue = new JTextField();
			loopStartValue.setName("loopStartValue");
			loopStartValue.setEditable(false);
			loopStartValue.setFont(Helpers.getDialogFont());
			final FontMetrics metrics = loopStartValue.getFontMetrics(Helpers.getDialogFont());
			final Dimension d = new Dimension(8*metrics.charWidth('0'), metrics.getHeight());
			loopStartValue.setSize(d);
			loopStartValue.setMinimumSize(d);
			loopStartValue.setMaximumSize(d);
			loopStartValue.setPreferredSize(d);
		}
		return loopStartValue;
	}
	private JLabel getLoopEndLabel()
	{
		if (loopEndLabel==null)
		{
			loopEndLabel = new JLabel();
			loopEndLabel.setName("loopEndLabel");
			loopEndLabel.setText("End");
			loopEndLabel.setFont(Helpers.getDialogFont());
		}
		return loopEndLabel;
	}
	private JTextField getLoopEndValue()
	{
		if (loopEndValue==null)
		{
			loopEndValue = new JTextField();
			loopEndValue.setName("loopEndValue");
			loopEndValue.setEditable(false);
			loopEndValue.setFont(Helpers.getDialogFont());
			final FontMetrics metrics = loopEndValue.getFontMetrics(Helpers.getDialogFont());
			final Dimension d = new Dimension(8*metrics.charWidth('0'), metrics.getHeight());
			loopEndValue.setSize(d);
			loopEndValue.setMinimumSize(d);
			loopEndValue.setMaximumSize(d);
			loopEndValue.setPreferredSize(d);
		}
		return loopEndValue;
	}
	private JPanel getSustainLoopPanel()
	{
		if (sustainLoopPanel==null)
		{
			sustainLoopPanel = new JPanel();
			sustainLoopPanel.setBorder(new TitledBorder(null, "Sustain Loop", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, Helpers.getDialogFont(), null));
			sustainLoopPanel.setLayout(new GridBagLayout());
			sustainLoopPanel.add(getSustainLoopTypeLabel(),  Helpers.getGridBagConstraint(0, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			sustainLoopPanel.add(getSustainLoopTypeValue(),  Helpers.getGridBagConstraint(1, 0, 1, 0, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			sustainLoopPanel.add(getSustainLoopStartLabel(), Helpers.getGridBagConstraint(0, 1, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			sustainLoopPanel.add(getSustainLoopStartValue(), Helpers.getGridBagConstraint(1, 1, 1, 0, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			sustainLoopPanel.add(getSustainLoopEndLabel(),   Helpers.getGridBagConstraint(0, 2, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			sustainLoopPanel.add(getSustainLoopEndValue(),   Helpers.getGridBagConstraint(1, 2, 1, 0, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		}
		return sustainLoopPanel;
	}
	private JLabel getSustainLoopTypeLabel()
	{
		if (sustainLoopTypeLabel==null)
		{
			sustainLoopTypeLabel = new JLabel();
			sustainLoopTypeLabel.setName("sustainLoopTypeLabel");
			sustainLoopTypeLabel.setText("Type");
			sustainLoopTypeLabel.setFont(Helpers.getDialogFont());
		}
		return sustainLoopTypeLabel;
	}
	private JTextField getSustainLoopTypeValue()
	{
		if (sustainLoopTypeValue==null)
		{
			sustainLoopTypeValue = new JTextField();
			sustainLoopTypeValue.setName("sustainLoopTypeValue");
			sustainLoopTypeValue.setEditable(false);
			sustainLoopTypeValue.setFont(Helpers.getDialogFont());
			final FontMetrics metrics = sustainLoopTypeValue.getFontMetrics(Helpers.getDialogFont());
			final Dimension d = new Dimension(8*metrics.charWidth('0'), metrics.getHeight());
			sustainLoopTypeValue.setSize(d);
			sustainLoopTypeValue.setMinimumSize(d);
			sustainLoopTypeValue.setMaximumSize(d);
			sustainLoopTypeValue.setPreferredSize(d);
		}
		return sustainLoopTypeValue;
	}
	private JLabel getSustainLoopStartLabel()
	{
		if (sustainLoopStartLabel==null)
		{
			sustainLoopStartLabel = new JLabel();
			sustainLoopStartLabel.setName("sustainLoopStartLabel");
			sustainLoopStartLabel.setText("Start");
			sustainLoopStartLabel.setFont(Helpers.getDialogFont());
		}
		return sustainLoopStartLabel;
	}
	private JTextField getSustainLoopStartValue()
	{
		if (sustainLoopStartValue==null)
		{
			sustainLoopStartValue = new JTextField();
			sustainLoopStartValue.setName("sustainLoopStartValue");
			sustainLoopStartValue.setEditable(false);
			sustainLoopStartValue.setFont(Helpers.getDialogFont());
			final FontMetrics metrics = sustainLoopStartValue.getFontMetrics(Helpers.getDialogFont());
			final Dimension d = new Dimension(8*metrics.charWidth('0'), metrics.getHeight());
			sustainLoopStartValue.setSize(d);
			sustainLoopStartValue.setMinimumSize(d);
			sustainLoopStartValue.setMaximumSize(d);
			sustainLoopStartValue.setPreferredSize(d);
		}
		return sustainLoopStartValue;
	}
	private JLabel getSustainLoopEndLabel()
	{
		if (sustainLoopEndLabel==null)
		{
			sustainLoopEndLabel = new JLabel();
			sustainLoopEndLabel.setName("sustainLoopEndLabel");
			sustainLoopEndLabel.setText("End");
			sustainLoopEndLabel.setFont(Helpers.getDialogFont());
		}
		return sustainLoopEndLabel;
	}
	private JTextField getSustainLoopEndValue()
	{
		if (sustainLoopEndValue==null)
		{
			sustainLoopEndValue = new JTextField();
			sustainLoopEndValue.setName("sustainLoopEndValue");
			sustainLoopEndValue.setEditable(false);
			sustainLoopEndValue.setFont(Helpers.getDialogFont());
			final FontMetrics metrics = sustainLoopEndValue.getFontMetrics(Helpers.getDialogFont());
			final Dimension d = new Dimension(8*metrics.charWidth('0'), metrics.getHeight());
			sustainLoopEndValue.setSize(d);
			sustainLoopEndValue.setMinimumSize(d);
			sustainLoopEndValue.setMaximumSize(d);
			sustainLoopEndValue.setPreferredSize(d);
		}
		return sustainLoopEndValue;
	}
	private JPanel getAutoVibratoPanel()
	{
		if (autoVibratoPanel==null)
		{
			autoVibratoPanel = new JPanel();
			autoVibratoPanel.setBorder(new TitledBorder(null, "Auto Vibrato", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, Helpers.getDialogFont(), null));
			autoVibratoPanel.setLayout(new GridBagLayout());
			autoVibratoPanel.add(getAutoVibTypeLabel(),  Helpers.getGridBagConstraint(0, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			autoVibratoPanel.add(getAutoVibTypeValue(),  Helpers.getGridBagConstraint(1, 0, 1, 0, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			autoVibratoPanel.add(getAutoVibDepthLabel(),  Helpers.getGridBagConstraint(0, 1, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			autoVibratoPanel.add(getAutoVibDepthValue(),  Helpers.getGridBagConstraint(1, 1, 1, 0, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			autoVibratoPanel.add(getAutoVibSweepLabel(),  Helpers.getGridBagConstraint(0, 2, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			autoVibratoPanel.add(getAutoVibSweepValue(),  Helpers.getGridBagConstraint(1, 2, 1, 0, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			autoVibratoPanel.add(getAutoVibRateLabel(),  Helpers.getGridBagConstraint(0, 3, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			autoVibratoPanel.add(getAutoVibRateValue(),  Helpers.getGridBagConstraint(1, 3, 1, 0, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		}
		return autoVibratoPanel;
	}
	private JLabel getAutoVibTypeLabel()
	{
		if (autoVibTypeLabel==null)
		{
			autoVibTypeLabel = new JLabel();
			autoVibTypeLabel.setName("autoVibTypeLabel");
			autoVibTypeLabel.setText("Type");
			autoVibTypeLabel.setFont(Helpers.getDialogFont());
		}
		return autoVibTypeLabel;
	}
	private JTextField getAutoVibTypeValue()
	{
		if (autoVibTypeValue==null)
		{
			autoVibTypeValue = new JTextField();
			autoVibTypeValue.setName("autoVibTypeValue");
			autoVibTypeValue.setEditable(false);
			autoVibTypeValue.setFont(Helpers.getDialogFont());
			final FontMetrics metrics = autoVibTypeValue.getFontMetrics(Helpers.getDialogFont());
			final Dimension d = new Dimension(8*metrics.charWidth('0'), metrics.getHeight());
			autoVibTypeValue.setSize(d);
			autoVibTypeValue.setMinimumSize(d);
			autoVibTypeValue.setMaximumSize(d);
			autoVibTypeValue.setPreferredSize(d);
		}
		return autoVibTypeValue;
	}
	private JLabel getAutoVibDepthLabel()
	{
		if (autoVibDepthLabel==null)
		{
			autoVibDepthLabel = new JLabel();
			autoVibDepthLabel.setName("autoVibDepthLabel");
			autoVibDepthLabel.setText("Depth");
			autoVibDepthLabel.setFont(Helpers.getDialogFont());
		}
		return autoVibDepthLabel;
	}
	private JTextField getAutoVibDepthValue()
	{
		if (autoVibDepthValue==null)
		{
			autoVibDepthValue = new JTextField();
			autoVibDepthValue.setName("autoVibDepthValue");
			autoVibDepthValue.setEditable(false);
			autoVibDepthValue.setFont(Helpers.getDialogFont());
			final FontMetrics metrics = autoVibDepthValue.getFontMetrics(Helpers.getDialogFont());
			final Dimension d = new Dimension(8*metrics.charWidth('0'), metrics.getHeight());
			autoVibDepthValue.setSize(d);
			autoVibDepthValue.setMinimumSize(d);
			autoVibDepthValue.setMaximumSize(d);
			autoVibDepthValue.setPreferredSize(d);
		}
		return autoVibDepthValue;
	}
	private JLabel getAutoVibSweepLabel()
	{
		if (autoVibSweepLabel==null)
		{
			autoVibSweepLabel = new JLabel();
			autoVibSweepLabel.setName("autoVibSweepLabel");
			autoVibSweepLabel.setText("Sweep");
			autoVibSweepLabel.setFont(Helpers.getDialogFont());
		}
		return autoVibSweepLabel;
	}
	private JTextField getAutoVibSweepValue()
	{
		if (autoVibSweepValue==null)
		{
			autoVibSweepValue = new JTextField();
			autoVibSweepValue.setName("autoVibSweepValue");
			autoVibSweepValue.setEditable(false);
			autoVibSweepValue.setFont(Helpers.getDialogFont());
			final FontMetrics metrics = autoVibSweepValue.getFontMetrics(Helpers.getDialogFont());
			final Dimension d = new Dimension(8*metrics.charWidth('0'), metrics.getHeight());
			autoVibSweepValue.setSize(d);
			autoVibSweepValue.setMinimumSize(d);
			autoVibSweepValue.setMaximumSize(d);
			autoVibSweepValue.setPreferredSize(d);
		}
		return autoVibSweepValue;
	}
	private JLabel getAutoVibRateLabel()
	{
		if (autoVibRateLabel==null)
		{
			autoVibRateLabel = new JLabel();
			autoVibRateLabel.setName("autoVibSweepLabel");
			autoVibRateLabel.setText("Rate");
			autoVibRateLabel.setFont(Helpers.getDialogFont());
		}
		return autoVibRateLabel;
	}
	private JTextField getAutoVibRateValue()
	{
		if (autoVibRateValue==null)
		{
			autoVibRateValue = new JTextField();
			autoVibRateValue.setName("autoVibRateValue");
			autoVibRateValue.setEditable(false);
			autoVibRateValue.setFont(Helpers.getDialogFont());
			final FontMetrics metrics = autoVibRateValue.getFontMetrics(Helpers.getDialogFont());
			final Dimension d = new Dimension(8*metrics.charWidth('0'), metrics.getHeight());
			autoVibRateValue.setSize(d);
			autoVibRateValue.setMinimumSize(d);
			autoVibRateValue.setMaximumSize(d);
			autoVibRateValue.setPreferredSize(d);
		}
		return autoVibRateValue;
	}
	private SampleImagePanel getImageBufferPanel()
	{
		if (imageBufferPanel==null)
		{
			imageBufferPanel = new SampleImagePanel();
		}
		return imageBufferPanel;
	}
	private static String [] AUTOVIBRATO_TYPES = new String [] { "Sine", "Square", "Ramp Up", "Ramp Down", "Random" };
	private void fillWithSample(final Sample sample)
	{
		getSampleType().setText(sample.getSampleTypeString());
		getSampleName().setText(sample.name);
		getDosFileName().setText(sample.dosFileName);
		getDefaultVolume().setText(Integer.toString(sample.volume));
		getGlobalVolume().setText(Integer.toString(sample.globalVolume));
		getSetPan().setFixedState(sample.panning!=-1);
		getSetPanValue().setText(Integer.toString(sample.panning));
		getFineTuneValue().setText(Integer.toString(sample.fineTune));
		getBaseFreqValue().setText(Integer.toString(sample.baseFrequency));
		getTransposeValue().setText(Integer.toString(sample.transpose));
		getLoopTypeValue().setText((sample.loopType&ModConstants.LOOP_ON)==0?"Off":(sample.loopType&ModConstants.LOOP_IS_PINGPONG)==0?"On":"Bidi");
		getLoopStartValue().setText(Integer.toString(sample.loopStart));
		getLoopEndValue().setText(Integer.toString(sample.loopStop));
		getSustainLoopTypeValue().setText((sample.loopType&ModConstants.LOOP_SUSTAIN_ON)==0?"Off":(sample.loopType&ModConstants.LOOP_SUSTAIN_IS_PINGPONG)==0?"On":"Bidi");
		getSustainLoopStartValue().setText(Integer.toString(sample.sustainLoopStart));
		getSustainLoopEndValue().setText(Integer.toString(sample.sustainLoopStop));
		getAutoVibTypeValue().setText(AUTOVIBRATO_TYPES[sample.vibratoType]);
		getAutoVibDepthValue().setText(Integer.toString(sample.vibratoDepth));
		getAutoVibSweepValue().setText(Integer.toString(sample.vibratoSweep));
		getAutoVibRateValue().setText(Integer.toString(sample.vibratoRate));
		getImageBufferPanel().setSample(sample);
		getImageBufferPanel().drawMe();
	}
	public void fillWithSamples(final Sample [] samples)
	{
		this.samples = samples;
		if (samples!=null)
		{
			ArrayList<Integer> list = new ArrayList<Integer>(samples.length);
			for (int i=0; i<samples.length; i++) list.add(Integer.valueOf(i+1));
			getSelectSample().setModel(new SpinnerListModel(list));
			if (samples[0]!=null) fillWithSample(samples[0]);
		}
	}
}
