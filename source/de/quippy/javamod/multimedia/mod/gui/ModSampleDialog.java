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
import java.awt.EventQueue;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.JTextField;
import javax.swing.SpinnerListModel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.quippy.javamod.main.gui.components.FixedStateCheckBox;
import de.quippy.javamod.multimedia.mod.ModConstants;
import de.quippy.javamod.multimedia.mod.ModInfoPanel;
import de.quippy.javamod.multimedia.mod.loader.instrument.Sample;
import de.quippy.javamod.multimedia.mod.mixer.SampleInstrumentPlayer;
import de.quippy.javamod.system.Helpers;

/**
 * @author Daniel Becker
 * @since 25.07.2020
 */
public class ModSampleDialog extends JDialog
{
	private static final long serialVersionUID = -9058637708283713743L;

	private static String [] AUTOVIBRATO_TYPES = new String [] { "Sine", "Square", "Ramp Up", "Ramp Down", "Random" };
	private static String [] ZOOM_TYPES = new String [] { "Auto", "1:1", "2:1", "4:1", "8:1", "16:1", "32:1" };

	public static final String BUTTONPLAY_INACTIVE = "/de/quippy/javamod/main/gui/ressources/play.gif";
	public static final String BUTTONPLAY_ACTIVE = "/de/quippy/javamod/main/gui/ressources/play_aktiv.gif";
	public static final String BUTTONPLAY_NORMAL = "/de/quippy/javamod/main/gui/ressources/play_normal.gif";

	private ImageIcon buttonPlay_Active = null;
	private ImageIcon buttonPlay_Inactive = null;
	private ImageIcon buttonPlay_normal = null;

	private JLabel labelSelectSample = null;
	private JSpinner selectSample = null;
	private JComboBox<String> zoomSelector = null;
	private JComboBox<String> noteSelector = null;
	private JButton button_Play = null;
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

	private SampleInstrumentPlayer player = null;
	private Sample [] samples;
	private ArrayList<String> spinnerModelData = null;

	private ModInfoPanel myModInfoPanel;

	/**
	 * Constructor for ModSampleDialog
	 * @param owner
	 * @param modal
	 * @param infoPanel
	 */
	public ModSampleDialog(Window owner, boolean modal, ModInfoPanel infoPanel)
	{
		super(owner, modal ? DEFAULT_MODALITY_TYPE : ModalityType.MODELESS);
		myModInfoPanel = infoPanel;
		initialize();
	}
	private void initialize()
	{
        final Container baseContentPane = getContentPane();
		baseContentPane.setLayout(new java.awt.GridBagLayout());
		
		baseContentPane.add(getLabelSelectSample(), 		Helpers.getGridBagConstraint(0, 0, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0));
		baseContentPane.add(getSelectSample(), 				Helpers.getGridBagConstraint(1, 0, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0));
		baseContentPane.add(getZoomSelector(),				Helpers.getGridBagConstraint(2, 0, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0));
		baseContentPane.add(getNoteSelector(), 				Helpers.getGridBagConstraint(3, 0, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0));
		baseContentPane.add(getButton_Play(), 				Helpers.getGridBagConstraint(4, 0, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0));
		baseContentPane.add(getSampleType(),				Helpers.getGridBagConstraint(5, 0, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0));
		baseContentPane.add(getVolumePanel(),				Helpers.getGridBagConstraint(0, 1, 1, 3, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0));
		baseContentPane.add(getSampleNameAndLoopsPanel(),	Helpers.getGridBagConstraint(3, 1, 1, 0, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0));
		baseContentPane.add(getImageBufferPanel(), 			Helpers.getGridBagConstraint(0, 3, 1, 0, GridBagConstraints.BOTH, GridBagConstraints.WEST, 1.0, 1.0));
		
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
        pack();
		
		clearSample();
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
	private int getCurrentSampleIndex()
	{
		return Integer.parseInt((String)getSelectSample().getModel().getValue(), 16) - 1; 
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
						fillWithSample(samples[getCurrentSampleIndex()]);
					}
				}
			});
		}
		return selectSample;
	}
	private JComboBox getZoomSelector()
	{
		if (zoomSelector==null)
		{
			zoomSelector = new JComboBox<String>();
			zoomSelector.setName("zoomSelector");
			zoomSelector.setFont(Helpers.getDialogFont());

			for (int i=0; i<ZOOM_TYPES.length; i++) zoomSelector.addItem(ZOOM_TYPES[i]);
			zoomSelector.addItemListener(new ItemListener()
			{
				@Override
				public void itemStateChanged(ItemEvent e)
				{
					if (samples==null) return;
					changeZoom(getZoomSelector().getSelectedIndex());
				}
			});
		}
		
		return zoomSelector;
	}
	private JComboBox getNoteSelector()
	{
		if (noteSelector==null)
		{
			noteSelector = new JComboBox<String>();
			noteSelector.setName("noteSelector");
			noteSelector.setFont(Helpers.getDialogFont());

			for (int i=1; i<=ModConstants.noteValues.length; i++) noteSelector.addItem(ModConstants.getNoteNameForIndex(i));
			noteSelector.setSelectedIndex(ModConstants.getNoteIndexForPeriod(ModConstants.BASEPERIOD));
		}
		
		return noteSelector;
	}
	private JButton getButton_Play()
	{
		if (button_Play == null)
		{
			buttonPlay_normal = new ImageIcon(getClass().getResource(BUTTONPLAY_NORMAL));
			buttonPlay_Inactive = new ImageIcon(getClass().getResource(BUTTONPLAY_INACTIVE));
			buttonPlay_Active = new ImageIcon(getClass().getResource(BUTTONPLAY_ACTIVE));

			button_Play = new JButton();
			button_Play.setName("button_Play");
			button_Play.setText(Helpers.EMPTY_STING);
			button_Play.setToolTipText("play");
			button_Play.setHorizontalTextPosition(SwingConstants.CENTER);
			button_Play.setVerticalTextPosition(SwingConstants.BOTTOM);
			button_Play.setIcon(buttonPlay_normal);
			button_Play.setDisabledIcon(buttonPlay_Inactive);
			button_Play.setPressedIcon(buttonPlay_Active);
			button_Play.setMargin(new Insets(4, 6, 4, 6));
			button_Play.addActionListener(new ActionListener()
			{
				boolean playing = false;

				public void actionPerformed(ActionEvent e)
				{
					if (playing)
					{
						if (player!=null && player.isPlaying()) player.stopPlayback();
					}
					else
					{
						if (samples == null) return;

						playing = true;
						getButton_Play().setIcon(buttonPlay_Active);
						player = new SampleInstrumentPlayer(myModInfoPanel.getParentContainer().createNewMixer0());
						// play inside a thread, so we do not block anything...
						new Thread(new Runnable()
						{
							public void run()
							{
								player.startPlayback(null, samples[getCurrentSampleIndex()], getNoteSelector().getSelectedIndex()+1);
								getButton_Play().setIcon(buttonPlay_normal);
								player = null;
								playing = false;
							}
						}).start();
					}
				}
			});
					
		}
		return button_Play;
	}
	private JTextField getSampleType()
	{
		if (sampleType==null)
		{
			sampleType = new JTextField();
			sampleType.setName("sampleType");
			sampleType.setEditable(false);
			sampleType.setFont(Helpers.getDialogFont());
			final FontMetrics metrics = sampleType.getFontMetrics(Helpers.getDialogFont());
			final Dimension d = new Dimension(40*metrics.charWidth('0'), metrics.getHeight());
			sampleType.setSize(d);
			sampleType.setMinimumSize(d);
			sampleType.setMaximumSize(d);
			sampleType.setPreferredSize(d);
		}
		return sampleType;
	}
	private JPanel getSampleNameAndLoopsPanel()
	{
		if (sampleNameAndLoopsPanel==null)
		{
			sampleNameAndLoopsPanel = new JPanel();
			sampleNameAndLoopsPanel.setLayout(new GridBagLayout());
			sampleNameAndLoopsPanel.add(getSampleNamePanel(),	Helpers.getGridBagConstraint(0, 0, 1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, 0.0, 0.0));
			sampleNameAndLoopsPanel.add(getLoopPanel(),			Helpers.getGridBagConstraint(1, 1, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 0.0, 0.0));
			sampleNameAndLoopsPanel.add(getSustainLoopPanel(),	Helpers.getGridBagConstraint(2, 1, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 0.0, 0.0));
			sampleNameAndLoopsPanel.add(getAutoVibratoPanel(),	Helpers.getGridBagConstraint(3, 1, 1, 0, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 0.0, 0.0));
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
			sampleNamePanel.add(getSampleNameLabel(),	Helpers.getGridBagConstraint(0, 0, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0));
			sampleNamePanel.add(getSampleName(),		Helpers.getGridBagConstraint(1, 0, 1, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, 1.0, 0.0));
			sampleNamePanel.add(getDosFileNameLabel(),	Helpers.getGridBagConstraint(2, 0, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0));
			sampleNamePanel.add(getDosFileName(),		Helpers.getGridBagConstraint(3, 0, 1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, 1.0, 0.0));
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
			volumePanel.add(getDefaultVolumeLabel(), Helpers.getGridBagConstraint(0, 0, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0));
			volumePanel.add(getDefaultVolume(), 	 Helpers.getGridBagConstraint(1, 0, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0));
			volumePanel.add(getGlobalVolumeLabel(),  Helpers.getGridBagConstraint(0, 1, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0));
			volumePanel.add(getGlobalVolume(), 	 	 Helpers.getGridBagConstraint(1, 1, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0));
			volumePanel.add(getSetPan(),  			 Helpers.getGridBagConstraint(0, 2, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0));
			volumePanel.add(getSetPanValue(),	 	 Helpers.getGridBagConstraint(1, 2, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0));
			volumePanel.add(getFinetuneLabel(),		 Helpers.getGridBagConstraint(0, 3, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0));
			volumePanel.add(getFineTuneValue(),	 	 Helpers.getGridBagConstraint(1, 3, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0));
			volumePanel.add(getBaseFreqLabel(),		 Helpers.getGridBagConstraint(0, 4, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0));
			volumePanel.add(getBaseFreqValue(),	 	 Helpers.getGridBagConstraint(1, 4, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0));
			volumePanel.add(getTransposeLabel(),	 Helpers.getGridBagConstraint(0, 5, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0));
			volumePanel.add(getTransposeValue(), 	 Helpers.getGridBagConstraint(1, 5, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0));
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
			loopPanel.add(getLoopTypeLabel(),  Helpers.getGridBagConstraint(0, 0, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0));
			loopPanel.add(getLoopTypeValue(),  Helpers.getGridBagConstraint(1, 0, 1, 0, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0));
			loopPanel.add(getLoopStartLabel(), Helpers.getGridBagConstraint(0, 1, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0));
			loopPanel.add(getLoopStartValue(), Helpers.getGridBagConstraint(1, 1, 1, 0, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0));
			loopPanel.add(getLoopEndLabel(),   Helpers.getGridBagConstraint(0, 2, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0));
			loopPanel.add(getLoopEndValue(),   Helpers.getGridBagConstraint(1, 2, 1, 0, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0));
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
			sustainLoopPanel.add(getSustainLoopTypeLabel(),  Helpers.getGridBagConstraint(0, 0, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0));
			sustainLoopPanel.add(getSustainLoopTypeValue(),  Helpers.getGridBagConstraint(1, 0, 1, 0, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0));
			sustainLoopPanel.add(getSustainLoopStartLabel(), Helpers.getGridBagConstraint(0, 1, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0));
			sustainLoopPanel.add(getSustainLoopStartValue(), Helpers.getGridBagConstraint(1, 1, 1, 0, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0));
			sustainLoopPanel.add(getSustainLoopEndLabel(),   Helpers.getGridBagConstraint(0, 2, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0));
			sustainLoopPanel.add(getSustainLoopEndValue(),   Helpers.getGridBagConstraint(1, 2, 1, 0, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0));
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
			autoVibratoPanel.add(getAutoVibTypeLabel(),  Helpers.getGridBagConstraint(0, 0, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0));
			autoVibratoPanel.add(getAutoVibTypeValue(),  Helpers.getGridBagConstraint(1, 0, 1, 0, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0));
			autoVibratoPanel.add(getAutoVibDepthLabel(), Helpers.getGridBagConstraint(0, 1, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0));
			autoVibratoPanel.add(getAutoVibDepthValue(), Helpers.getGridBagConstraint(1, 1, 1, 0, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0));
			autoVibratoPanel.add(getAutoVibSweepLabel(), Helpers.getGridBagConstraint(0, 2, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0));
			autoVibratoPanel.add(getAutoVibSweepValue(), Helpers.getGridBagConstraint(1, 2, 1, 0, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0));
			autoVibratoPanel.add(getAutoVibRateLabel(),  Helpers.getGridBagConstraint(0, 3, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0));
			autoVibratoPanel.add(getAutoVibRateValue(),  Helpers.getGridBagConstraint(1, 3, 1, 0, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0));
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
	private JScrollPane imageBufferScrollPane = null;
	private JScrollPane getImageBufferScrollPane()
	{
		if (imageBufferScrollPane==null)
		{
			imageBufferScrollPane = new javax.swing.JScrollPane();
			imageBufferScrollPane.setName("imageBufferScrollPane");
			imageBufferScrollPane.setViewportView(getImageBufferPanel());
			imageBufferScrollPane.setDoubleBuffered(true);
		}
		return imageBufferScrollPane;
	}
	private SampleImagePanel getImageBufferPanel()
	{
		if (imageBufferPanel==null)
		{
			imageBufferPanel = new SampleImagePanel();
		}
		return imageBufferPanel;
	}
	private void changeZoom(final int newZoom)
	{
		final Dimension d = getImageBufferPanel().getSize();
		if (newZoom == 0)
		{
			getContentPane().remove(getImageBufferScrollPane());
			getContentPane().add(getImageBufferPanel(), Helpers.getGridBagConstraint(0, 3, 1, 0, GridBagConstraints.BOTH, GridBagConstraints.WEST, 1.0, 1.0));
		}
		else
		{
			final Sample theSample = getImageBufferPanel().getSample();
			if (theSample!=null)
			{
				final int scrollBarHeight = getImageBufferScrollPane().getHorizontalScrollBar().getPreferredSize().height;
				final Insets inset = getImageBufferScrollPane().getInsets();
				d.height= getImageBufferScrollPane().getHeight() - inset.top - inset.bottom - (scrollBarHeight<<1);
				d.width = theSample.length << (newZoom-1);
				getContentPane().remove(getImageBufferPanel());
				getImageBufferScrollPane().setViewportView(getImageBufferPanel());
				getContentPane().add(getImageBufferScrollPane(), Helpers.getGridBagConstraint(0, 3, 1, 0, GridBagConstraints.BOTH, GridBagConstraints.WEST, 1.0, 1.0));
			}
		}
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{				
				try
				{
					getImageBufferPanel().setSize(d);
					getImageBufferPanel().setMinimumSize(d);
					getImageBufferPanel().setMaximumSize(d);
					getImageBufferPanel().setPreferredSize(d);
					pack();
				}
				catch (Throwable ex)
				{
					// Keep it!
				}
			}
		});
	}
	private void clearSample()
	{
		spinnerModelData = new ArrayList<String>(1);
		spinnerModelData.add(ModConstants.getAsHex(0, 2));
		getSelectSample().setModel(new SpinnerListModel(spinnerModelData));
		
		getButton_Play().setEnabled(false);
		getZoomSelector().setEnabled(false);

		getSampleType().setText(Helpers.EMPTY_STING);
		getSampleName().setText(Helpers.EMPTY_STING);
		getDosFileName().setText(Helpers.EMPTY_STING);
		getDefaultVolume().setText(Helpers.EMPTY_STING);
		getGlobalVolume().setText(Helpers.EMPTY_STING);
		getSetPan().setFixedState(false);
		getSetPanValue().setText(Helpers.EMPTY_STING);
		getFineTuneValue().setText(Helpers.EMPTY_STING);
		getBaseFreqValue().setText(Helpers.EMPTY_STING);
		getTransposeValue().setText(Helpers.EMPTY_STING);
		getLoopTypeValue().setText(Helpers.EMPTY_STING);
		getLoopStartValue().setText(Helpers.EMPTY_STING);
		getLoopEndValue().setText(Helpers.EMPTY_STING);
		getSustainLoopTypeValue().setText(Helpers.EMPTY_STING);
		getSustainLoopStartValue().setText(Helpers.EMPTY_STING);
		getSustainLoopEndValue().setText(Helpers.EMPTY_STING);
		getAutoVibTypeValue().setText(Helpers.EMPTY_STING);
		getAutoVibDepthValue().setText(Helpers.EMPTY_STING);
		getAutoVibSweepValue().setText(Helpers.EMPTY_STING);
		getAutoVibRateValue().setText(Helpers.EMPTY_STING);
		
		getZoomSelector().setSelectedIndex(0);
		getImageBufferPanel().setSample(null);

		// after setting the new model, make the editor of the spinner un-editable
		((DefaultEditor)getSelectSample().getEditor()).getTextField().setEditable(false);
	}
	private void fillWithSample(final Sample sample)
	{
		getButton_Play().setEnabled(true);
		getZoomSelector().setEnabled(true);

		getSampleType().setText(sample.getSampleTypeString());
		getSampleType().setCaretPosition(0); getSampleType().moveCaretPosition(0);
		getSampleName().setText(sample.name);
		getSampleName().setCaretPosition(0); getSampleName().moveCaretPosition(0);
		getDosFileName().setText(sample.dosFileName);
		getDosFileName().setCaretPosition(0); getDosFileName().moveCaretPosition(0);
		getDefaultVolume().setText(Integer.toString(sample.volume));
		getGlobalVolume().setText(Integer.toString(sample.globalVolume));
		getSetPan().setFixedState(sample.setPanning);
		getSetPanValue().setText(Integer.toString(sample.defaultPanning));
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
	}
	public void showSample(final int sampleIndex)
	{
		if (samples!=null) getSelectSample().setValue(spinnerModelData.get(sampleIndex));
	}
	public void fillWithSamples(final Sample [] samples)
	{
		this.samples = samples;
		if (samples!=null)
		{
			spinnerModelData = new ArrayList<String>(samples.length);
			for (int i=0; i<samples.length; i++) spinnerModelData.add(ModConstants.getAsHex(i+1, 2));
			getSelectSample().setModel(new SpinnerListModel(spinnerModelData));
			getSelectSample().setValue(spinnerModelData.get(0)); // in some unknown cases, the index is not really set.
			fillWithSample(samples[0]); // as index is normally not changed, no change event is fired

			// after setting the new model, make the editor of the spinner un-editable
			((DefaultEditor)getSelectSample().getEditor()).getTextField().setEditable(false);
		}
		else
			clearSample();
		setPreferredSize(getSize());
		pack();
	}
}