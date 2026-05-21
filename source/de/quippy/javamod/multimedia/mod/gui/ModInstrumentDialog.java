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
import java.awt.Insets;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerListModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;

import de.quippy.javamod.main.gui.components.FixedStateCheckBox;
import de.quippy.javamod.multimedia.mod.ModConstants;
import de.quippy.javamod.multimedia.mod.ModInfoPanel;
import de.quippy.javamod.multimedia.mod.ModMixer;
import de.quippy.javamod.multimedia.mod.loader.instrument.Instrument;
import de.quippy.javamod.multimedia.mod.midi.ModMidiMixer;
import de.quippy.javamod.multimedia.mod.mixer.BasicModMixer;
import de.quippy.javamod.multimedia.mod.mixer.SampleInstrumentPlayer;
import de.quippy.javamod.system.Helpers;

/**
 * @author Daniel Becker
 * @since 25.07.2020
 */
public class ModInstrumentDialog extends JDialog
{
	private static final long serialVersionUID = -5890906666611603247L;

	private static final int SAMPLE_MAP_LINE_LENGTH = 15;
	private static final String THREE_BARS = "---";
	private static final char PLUSMINUS = '±';
	private static final String DEFAULT_STR = "Default";

	public static final String BUTTONPLAY_INACTIVE = "/de/quippy/javamod/main/gui/ressources/play.gif";
	public static final String BUTTONPLAY_ACTIVE = "/de/quippy/javamod/main/gui/ressources/play_aktiv.gif";
	public static final String BUTTONPLAY_NORMAL = "/de/quippy/javamod/main/gui/ressources/play_normal.gif";

	private ImageIcon buttonPlay_Active = null;
	private ImageIcon buttonPlay_Inactive = null;
	private ImageIcon buttonPlay_normal = null;

	private JLabel labelSelectInstrument = null;
	private JSpinner selectInstrument = null;

	private JButton button_Play = null;

	private JPanel instrumentNamePanel = null;
	private JLabel instrumentNameLabel = null;
	private JTextField instrumentName = null;
	private JLabel fileNameLabel = null;
	private JTextField fileName = null;
	private JPanel infoPanelOne = null;
	private JPanel infoPanelTwo = null;
	private JPanel infoPanelThree= null;

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

	private JPanel sampleQualityPanel = null;
	private JLabel rampingLabel = null;
	private JTextField ramping = null;
	private JLabel resamplingLabel = null;
	private JTextField resampling = null;

	private JPanel filterPanel = null;
	private FixedStateCheckBox setResonance = null;
	private JSlider resonanceValue = null;
	private FixedStateCheckBox setCutOff = null;
	private JSlider cutOffValue = null;
	private JLabel filterModeLabel = null;
	private JTextField filterMode = null;

	private JPanel randomVariationPanel = null;
	private JLabel volumeVariationLabel = null;
	private JSlider volumeVariation = null;
	private JLabel panningVariationLabel = null;
	private JSlider panningVariation = null;
	private JLabel resonanceVariationLabel = null;
	private JSlider resonanceVariation = null;
	private JLabel cutOffVariationLabel = null;
	private JSlider cutOffVariation = null;

	private JPanel NNAPanel = null;
	private JLabel actionNNALabel = null;
	private JTextField actionNNA = null;
	private JLabel checkDNALabel = null;
	private JTextField checkDNA = null;
	private JLabel actionDNALabel = null;
	private JTextField actionDNA = null;

	private JPanel pluginMidiPanel = null;
	private JLabel pluginLabel = null;
	private JTextField plugin = null;
	private JLabel midiChannelLabel = null;
	private JTextField midiChannel = null;
	private JLabel midiProgramlLabel = null;
	private JTextField midiProgram = null;
	private JLabel midiBankLabel = null;
	private JTextField midiBank = null;
	private JLabel pwdDepthLabel = null;
	private JTextField pwdDepth = null;
	private JLabel midiVolCmdHandlingLabel = null;
	private JTextField midiVolCmdHandling = null;
	private FixedStateCheckBox midiVelocityCmdHandling = null;

	private JPanel sampleMapPanel = null;
	private JScrollPane sampleMapScrollPane = null;
	private JTextArea sampleMap = null;

	private JTabbedPane envelopeTabbedPane = null;
	private EnvelopePanel volumeEnvelopePanel = null;
	private EnvelopePanel panningEnvelopePanel = null;
	private EnvelopePanel pitchEnvelopePanel = null;

	private SampleInstrumentPlayer player = null;
	private Instrument[] instruments = null;
	private ArrayList<String> spinnerModelData = null;
	private int noteIndexRow = ModConstants.BASENOTEINDEX;

	private final ModInfoPanel myModInfoPanel;
	private ModMixer currentModMixer;
	private BasicModMixer currentMixer;
	
	private String[] interpolationModel;

	/**
	 * Constructor for ModInstrumentDialog
	 * @param owner
	 * @param modal
	 * @param infoPanel
	 */
	public ModInstrumentDialog(final Window owner, final boolean modal, final ModInfoPanel infoPanel)
	{
		super(owner, modal ? DEFAULT_MODALITY_TYPE : ModalityType.MODELESS);
		myModInfoPanel = infoPanel;
		
		interpolationModel = new String[ModConstants.INTERPOLATION.length+1];
		interpolationModel[0]=DEFAULT_STR;
		for (int i=0; i<ModConstants.INTERPOLATION.length; i++) interpolationModel[i+1]=ModConstants.INTERPOLATION[i];
		
		initialize();
	}
	private void initialize()
	{
        final Container baseContentPane = getContentPane();
		baseContentPane.setLayout(new java.awt.GridBagLayout());

		baseContentPane.add(getLabelSelectInstrument(), 	Helpers.getGridBagConstraint(0, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		baseContentPane.add(getSelectInstrument(), 			Helpers.getGridBagConstraint(1, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		baseContentPane.add(getInstrumentNamePanel(), 		Helpers.getGridBagConstraint(2, 0, 1, 2, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		baseContentPane.add(getButton_Play(), 				Helpers.getGridBagConstraint(4, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		baseContentPane.add(getInfoPanelOne(), 				Helpers.getGridBagConstraint(0, 1, 1, 3, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		baseContentPane.add(getInfoPanelTwo(), 				Helpers.getGridBagConstraint(3, 1, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		baseContentPane.add(getInfoPanelThree(), 			Helpers.getGridBagConstraint(4, 1, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		baseContentPane.add(getTabbedPane(), 				Helpers.getGridBagConstraint(0, 2, 1, 0, java.awt.GridBagConstraints.BOTH, java.awt.GridBagConstraints.WEST, 1.0, 1.0));

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		addWindowListener(new java.awt.event.WindowAdapter()
		{
			@Override
			public void windowClosing(final java.awt.event.WindowEvent e)
			{
				doClose();
			}
		});

		setName("Show mod instruments");
		setTitle("Show mod instruments");
		setResizable(true);
        pack();

		clearInstrument();
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
	private int getCurrentInstrument()
	{
		return Integer.parseInt((String)getSelectInstrument().getModel().getValue(), 16) - 1;
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
				public void stateChanged(final ChangeEvent e)
				{
					if (instruments!=null)
					{
						fillWithInstrument(instruments[getCurrentInstrument()]);
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

				@Override
				public void actionPerformed(final ActionEvent e)
				{
					if (playing)
					{
						if (player!=null && player.isPlaying()) player.stopPlayback();
					}
					else
					{
						if (instruments == null) return;

						playing = true;
						getButton_Play().setIcon(buttonPlay_Active);
						player = new SampleInstrumentPlayer(myModInfoPanel.getParentContainer().createNewMixer0());
						// play inside a thread, so we do not block anything...
						new Thread(new Runnable()
						{
							@Override
							public void run()
							{
								player.startPlayback(instruments[getCurrentInstrument()], null, (noteIndexRow<0)?ModConstants.BASENOTEINDEX:noteIndexRow + 1);
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
	private JPanel getInfoPanelOne()
	{
		if (infoPanelOne==null)
		{
			infoPanelOne = new JPanel();
			infoPanelOne.setBorder(BorderFactory.createEmptyBorder());
			infoPanelOne.setLayout(new GridBagLayout());
			infoPanelOne.add(getGlobalVolumePanel(), 		Helpers.getGridBagConstraint(0, 0, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
			infoPanelOne.add(getPitchPanSepPanel(), 		Helpers.getGridBagConstraint(0, 1, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
			infoPanelOne.add(getSampleQualityPanel(), 		Helpers.getGridBagConstraint(0, 2, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		}
		return infoPanelOne;
	}
	private JPanel getInfoPanelTwo()
	{
		if (infoPanelTwo==null)
		{
			infoPanelTwo = new JPanel();
			infoPanelTwo.setBorder(BorderFactory.createEmptyBorder());
			infoPanelTwo.setLayout(new GridBagLayout());
			infoPanelTwo.add(getFilterPanel(), 				Helpers.getGridBagConstraint(0, 0, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
			infoPanelTwo.add(getRandomVariationPanel(), 	Helpers.getGridBagConstraint(0, 1, 1, 2, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		}
		return infoPanelTwo;
	}
	private JPanel getInfoPanelThree()
	{
		if (infoPanelThree==null)
		{
			infoPanelThree = new JPanel();
			infoPanelThree.setBorder(BorderFactory.createEmptyBorder());
			infoPanelThree.setLayout(new GridBagLayout());
			infoPanelThree.add(getNNAPanel(), 				Helpers.getGridBagConstraint(0, 0, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
			infoPanelThree.add(getPlugInMidiPanel(), 		Helpers.getGridBagConstraint(0, 1, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
			infoPanelThree.add(getSampleMapPanel(),			Helpers.getGridBagConstraint(1, 0, 2, 1, java.awt.GridBagConstraints.BOTH, java.awt.GridBagConstraints.WEST, 1.0, 1.0));
		}
		return infoPanelThree;
	}
	private JPanel getGlobalVolumePanel()
	{
		if (globalVolumePanel==null)
		{
			globalVolumePanel = new JPanel();
			globalVolumePanel.setBorder(new TitledBorder(null, "General", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, Helpers.getDialogFont(), null));
			globalVolumePanel.setLayout(new GridBagLayout());
			globalVolumePanel.add(getGlobalVolumeLabel(), 	Helpers.getGridBagConstraint(0, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			globalVolumePanel.add(getGlobalVolume(), 		Helpers.getGridBagConstraint(1, 0, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
			globalVolumePanel.add(getFadeOutVolumeLabel(), 	Helpers.getGridBagConstraint(0, 1, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			globalVolumePanel.add(getFadeOutVolume(), 		Helpers.getGridBagConstraint(1, 1, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
			globalVolumePanel.add(getSetPan(), 				Helpers.getGridBagConstraint(0, 2, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			globalVolumePanel.add(getSetPanValue(), 		Helpers.getGridBagConstraint(1, 2, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
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
			pitchPanSepPanel.add(getPitchPanSep(), 			Helpers.getGridBagConstraint(1, 0, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
			pitchPanSepPanel.add(getPitchPanCenterLabel(), 	Helpers.getGridBagConstraint(2, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			pitchPanSepPanel.add(getPitchPanCenter(), 		Helpers.getGridBagConstraint(3, 0, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
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
		}
		return pitchPanCenter;
	}
	private JPanel getSampleQualityPanel()
	{
		if (sampleQualityPanel==null)
		{
			sampleQualityPanel = new JPanel();
			sampleQualityPanel.setBorder(new TitledBorder(null, "Sample Quality", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, Helpers.getDialogFont(), null));
			sampleQualityPanel.setLayout(new GridBagLayout());
			sampleQualityPanel.add(getRampingLabel(), 		Helpers.getGridBagConstraint(0, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			sampleQualityPanel.add(getRamping(), 			Helpers.getGridBagConstraint(1, 0, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
			sampleQualityPanel.add(getResamplingLabel(), 	Helpers.getGridBagConstraint(0, 1, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			sampleQualityPanel.add(getResampling(), 		Helpers.getGridBagConstraint(1, 1, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		}
		return sampleQualityPanel;
	}
	private JLabel getRampingLabel()
	{
		if (rampingLabel==null)
		{
			rampingLabel = new JLabel();
			rampingLabel.setName("rampingLabel");
			rampingLabel.setText("Ramping");
			rampingLabel.setFont(Helpers.getDialogFont());
		}
		return rampingLabel;
	}
	private JTextField getRamping()
	{
		if (ramping==null)
		{
			ramping = new JTextField();
			ramping.setName("ramping");
			ramping.setEditable(false);
			ramping.setFont(Helpers.getDialogFont());
		}
		return ramping;
	}
	private JLabel getResamplingLabel()
	{
		if (resamplingLabel==null)
		{
			resamplingLabel = new JLabel();
			resamplingLabel.setName("resamplingLabel");
			resamplingLabel.setText("Resampling");
			resamplingLabel.setFont(Helpers.getDialogFont());
		}
		return resamplingLabel;
	}
	private JTextField getResampling()
	{
		if (resampling==null)
		{
			resampling = new JTextField();
			resampling.setName("resampling");
			resampling.setEditable(false);
			resampling.setFont(Helpers.getDialogFont());
		}
		return resampling;
	}
	private JPanel getFilterPanel()
	{
		if (filterPanel==null)
		{
			filterPanel = new JPanel();
			filterPanel.setBorder(new TitledBorder(null, "Filter", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, Helpers.getDialogFont(), null));
			filterPanel.setLayout(new GridBagLayout());
			filterPanel.add(getSetResonance(), 		Helpers.getGridBagConstraint(0, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			filterPanel.add(getResonanceValue(), 	Helpers.getGridBagConstraint(1, 0, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
			filterPanel.add(getSetCutOff(), 		Helpers.getGridBagConstraint(0, 1, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			filterPanel.add(getCutOffValue(), 		Helpers.getGridBagConstraint(1, 1, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
			filterPanel.add(getFilterModeLabel(),	Helpers.getGridBagConstraint(0, 2, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			filterPanel.add(getFilterMode(), 		Helpers.getGridBagConstraint(1, 2, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
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
	private JSlider getResonanceValue()
	{
		if (resonanceValue==null)
		{
			resonanceValue = new JSlider(0, 127)
			{
				/**
				 * @param n
				 * @see javax.swing.JSlider#setValue(int)
				 */
				@Override
				public void setValue(int n)
				{
					super.setValue(n);
					int db = ((n * 24) + (128/2)) / 128; // muldivr of openmodplug
					setToolTipText(Integer.toString(n)+" ("+Integer.toString(db)+"dB)");
				}
			};
			resonanceValue.setName("resonanceValue");
			resonanceValue.setEnabled(false);
			resonanceValue.setPaintLabels(false);
			resonanceValue.setPaintTicks(false);
			resonanceValue.setFont(Helpers.getDialogFont());
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
	private JSlider getCutOffValue()
	{
		if (cutOffValue==null)
		{
			cutOffValue = new JSlider(0, 127)
			{
				/**
				 * @param n
				 * @see javax.swing.JSlider#setValue(int)
				 */
				@Override
				public void setValue(int n)
				{
					super.setValue(n);
					setToolTipText(Integer.toString(n));
				}
			};
			cutOffValue.setName("cutOffValue");
			cutOffValue.setEnabled(false);
			cutOffValue.setPaintLabels(false);
			cutOffValue.setPaintTicks(false);
			cutOffValue.setFont(Helpers.getDialogFont());
		}
		return cutOffValue;
	}
	private JLabel getFilterModeLabel()
	{
		if (filterModeLabel==null)
		{
			filterModeLabel = new JLabel();
			filterModeLabel.setName("filterModeLabel");
			filterModeLabel.setText("Mode");
			filterModeLabel.setFont(Helpers.getDialogFont());
		}
		return filterModeLabel;
	}
	private JTextField getFilterMode()
	{
		if (filterMode==null)
		{
			filterMode = new JTextField();
			filterMode.setName("filterMode");
			filterMode.setEditable(false);
			filterMode.setFont(Helpers.getDialogFont());
		}
		return filterMode;
	}
	private JPanel getRandomVariationPanel()
	{
		if (randomVariationPanel==null)
		{
			randomVariationPanel = new JPanel();
			randomVariationPanel.setBorder(new TitledBorder(null, "Random Variation", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, Helpers.getDialogFont(), null));
			randomVariationPanel.setLayout(new GridBagLayout());
			randomVariationPanel.add(getVolumeVariationLabel(), 	Helpers.getGridBagConstraint(0, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			randomVariationPanel.add(getVolumeVariation(), 			Helpers.getGridBagConstraint(1, 0, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
			randomVariationPanel.add(getPanningVariationLabel(), 	Helpers.getGridBagConstraint(0, 1, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			randomVariationPanel.add(getPanningVariation(), 		Helpers.getGridBagConstraint(1, 1, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
			randomVariationPanel.add(getResonanceVariationLabel(), 	Helpers.getGridBagConstraint(0, 2, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			randomVariationPanel.add(getResonanceVariation(), 		Helpers.getGridBagConstraint(1, 2, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
			randomVariationPanel.add(getCutOffVariationLabel(), 	Helpers.getGridBagConstraint(0, 3, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			randomVariationPanel.add(getCutOffVariation(), 			Helpers.getGridBagConstraint(1, 3, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
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
	private JSlider getVolumeVariation()
	{
		if (volumeVariation==null)
		{
			volumeVariation =  new JSlider(0, 100)
			{
				/**
				 * @param n
				 * @see javax.swing.JSlider#setValue(int)
				 */
				@Override
				public void setValue(int n)
				{
					super.setValue(n);
					setToolTipText(PLUSMINUS + Integer.toString(n)+'%');
				}
			};
			volumeVariation.setName("volumeVariation");
			volumeVariation.setEnabled(false);
			volumeVariation.setPaintLabels(false);
			volumeVariation.setPaintTicks(false);
			volumeVariation.setFont(Helpers.getDialogFont());
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
	private JSlider getPanningVariation()
	{
		if (panningVariation==null)
		{
			panningVariation =  new JSlider(0, 64)
			{
				/**
				 * @param n
				 * @see javax.swing.JSlider#setValue(int)
				 */
				@Override
				public void setValue(int n)
				{
					super.setValue(n);
					setToolTipText(PLUSMINUS + Integer.toString(n));
				}
			};

			panningVariation.setName("panningVariation");
			panningVariation.setEnabled(false);
			panningVariation.setPaintLabels(false);
			panningVariation.setPaintTicks(false);
			panningVariation.setFont(Helpers.getDialogFont());
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
		}
		return resonanceVariationLabel;
	}
	private JSlider getResonanceVariation()
	{
		if (resonanceVariation==null)
		{
			resonanceVariation =  new JSlider(0, 64)
			{
				/**
				 * @param n
				 * @see javax.swing.JSlider#setValue(int)
				 */
				@Override
				public void setValue(int n)
				{
					super.setValue(n);
					setToolTipText(PLUSMINUS + Integer.toString(n));
				}
			};

			resonanceVariation.setName("resonanceVariation");
			resonanceVariation.setEnabled(false);
			resonanceVariation.setPaintLabels(false);
			resonanceVariation.setPaintTicks(false);
			resonanceVariation.setFont(Helpers.getDialogFont());
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
		}
		return cutOffVariationLabel;
	}
	private JSlider getCutOffVariation()
	{
		if (cutOffVariation==null)
		{
			cutOffVariation =  new JSlider(0, 64)
			{
				/**
				 * @param n
				 * @see javax.swing.JSlider#setValue(int)
				 */
				@Override
				public void setValue(int n)
				{
					super.setValue(n);
					setToolTipText(PLUSMINUS + Integer.toString(n));
				}
			};
			cutOffVariation.setName("cutOffVariation");
			cutOffVariation.setEnabled(false);
			cutOffVariation.setPaintLabels(false);
			cutOffVariation.setPaintTicks(false);
			cutOffVariation.setFont(Helpers.getDialogFont());
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
			NNAPanel.add(getActionNNA(), 		Helpers.getGridBagConstraint(1, 0, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
			NNAPanel.add(getCheckDNALabel(), 	Helpers.getGridBagConstraint(0, 1, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			NNAPanel.add(getCheckDNA(), 		Helpers.getGridBagConstraint(1, 1, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
			NNAPanel.add(getActionDNALabel(), 	Helpers.getGridBagConstraint(0, 2, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			NNAPanel.add(getActionDNA(), 		Helpers.getGridBagConstraint(1, 2, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
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
		}
		return actionDNA;
	}
	private JPanel getPlugInMidiPanel()
	{
		if (pluginMidiPanel==null)
		{
			pluginMidiPanel = new JPanel();
			pluginMidiPanel.setBorder(new TitledBorder(null, "Plugin / MIDI", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, Helpers.getDialogFont(), null));
			pluginMidiPanel.setLayout(new GridBagLayout());
			pluginMidiPanel.add(getPluginLabel(), 				Helpers.getGridBagConstraint(0, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			pluginMidiPanel.add(getPlugin(), 					Helpers.getGridBagConstraint(1, 0, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
			pluginMidiPanel.add(getPwdDepthLabel(), 			Helpers.getGridBagConstraint(2, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			pluginMidiPanel.add(getPwdDepth(), 					Helpers.getGridBagConstraint(3, 0, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
			pluginMidiPanel.add(getMidiChannelLabel(), 			Helpers.getGridBagConstraint(0, 1, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			pluginMidiPanel.add(getMidiChannel(), 				Helpers.getGridBagConstraint(1, 1, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
			pluginMidiPanel.add(getMidiVolCmdHandlingLabel(), 	Helpers.getGridBagConstraint(2, 1, 1, 2, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			pluginMidiPanel.add(getMidiVolCmdHandling(), 		Helpers.getGridBagConstraint(2, 2, 1, 2, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
			pluginMidiPanel.add(getMidiProgramLabel(),			Helpers.getGridBagConstraint(0, 2, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			pluginMidiPanel.add(getMidiProgram(), 				Helpers.getGridBagConstraint(1, 2, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
			pluginMidiPanel.add(getMidiVolCmdHandling(), 		Helpers.getGridBagConstraint(2, 2, 1, 2, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
			pluginMidiPanel.add(getMidiBankLabel(), 			Helpers.getGridBagConstraint(0, 3, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			pluginMidiPanel.add(getMidiBank(), 					Helpers.getGridBagConstraint(1, 3, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
			pluginMidiPanel.add(getMidiVelocityCmdHandling(), 	Helpers.getGridBagConstraint(2, 3, 1, 2, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		}
		return pluginMidiPanel;
	}
	private JLabel getPluginLabel()
	{
		if (pluginLabel==null)
		{
			pluginLabel = new JLabel();
			pluginLabel.setName("pluginLabel");
			pluginLabel.setText("Plugin");
			pluginLabel.setFont(Helpers.getDialogFont());
		}
		return pluginLabel;
	}
	private JTextField getPlugin()
	{
		if (plugin==null)
		{
			plugin = new JTextField();
			plugin.setName("plugin");
			plugin.setEditable(false);
			plugin.setFont(Helpers.getDialogFont());
		}
		return plugin;
	}
	private JLabel getMidiChannelLabel()
	{
		if (midiChannelLabel==null)
		{
			midiChannelLabel = new JLabel();
			midiChannelLabel.setName("midiChannelLabel");
			midiChannelLabel.setText("MIDI channel");
			midiChannelLabel.setFont(Helpers.getDialogFont());
		}
		return midiChannelLabel;
	}
	private JTextField getMidiChannel()
	{
		if (midiChannel==null)
		{
			midiChannel = new JTextField();
			midiChannel.setName("midiChannel");
			midiChannel.setEditable(false);
			midiChannel.setFont(Helpers.getDialogFont());
		}
		return midiChannel;
	}
	private JLabel getMidiProgramLabel()
	{
		if (midiProgramlLabel==null)
		{
			midiProgramlLabel = new JLabel();
			midiProgramlLabel.setName("midiProgramlLabel");
			midiProgramlLabel.setText("MIDI program");
			midiProgramlLabel.setFont(Helpers.getDialogFont());
		}
		return midiProgramlLabel;
	}
	private JTextField getMidiProgram()
	{
		if (midiProgram==null)
		{
			midiProgram = new JTextField();
			midiProgram.setName("midiProgram");
			midiProgram.setEditable(false);
			midiProgram.setFont(Helpers.getDialogFont());
		}
		return midiProgram;
	}
	private JLabel getMidiBankLabel()
	{
		if (midiBankLabel==null)
		{
			midiBankLabel = new JLabel();
			midiBankLabel.setName("midiBankLabel");
			midiBankLabel.setText("MIDI bank");
			midiBankLabel.setFont(Helpers.getDialogFont());
		}
		return midiBankLabel;
	}
	private JTextField getMidiBank()
	{
		if (midiBank==null)
		{
			midiBank = new JTextField();
			midiBank.setName("midiBank");
			midiBank.setEditable(false);
			midiBank.setFont(Helpers.getDialogFont());
		}
		return midiBank;
	}
	private JLabel getPwdDepthLabel()
	{
		if (pwdDepthLabel==null)
		{
			pwdDepthLabel = new JLabel();
			pwdDepthLabel.setName("pwdDepthLabel");
			pwdDepthLabel.setText("Pitch Wheel Depth");
			pwdDepthLabel.setFont(Helpers.getDialogFont());
		}
		return pwdDepthLabel;
	}
	private JTextField getPwdDepth()
	{
		if (pwdDepth==null)
		{
			pwdDepth = new JTextField();
			pwdDepth.setName("pwdDepth");
			pwdDepth.setEditable(false);
			pwdDepth.setFont(Helpers.getDialogFont());
		}
		return pwdDepth;
	}
	private JLabel getMidiVolCmdHandlingLabel()
	{
		if (midiVolCmdHandlingLabel==null)
		{
			midiVolCmdHandlingLabel = new JLabel();
			midiVolCmdHandlingLabel.setName("midiVolCmdHandlingLabel");
			midiVolCmdHandlingLabel.setText("Volume command handling");
			midiVolCmdHandlingLabel.setFont(Helpers.getDialogFont());
		}
		return midiVolCmdHandlingLabel;
	}
	private JTextField getMidiVolCmdHandling()
	{
		if (midiVolCmdHandling==null)
		{
			midiVolCmdHandling = new JTextField();
			midiVolCmdHandling.setName("midiVolCmdHandling");
			midiVolCmdHandling.setEditable(false);
			midiVolCmdHandling.setFont(Helpers.getDialogFont());
		}
		return midiVolCmdHandling;
	}
	private FixedStateCheckBox getMidiVelocityCmdHandling()
	{
		if (midiVelocityCmdHandling==null)
		{
			midiVelocityCmdHandling = new FixedStateCheckBox();
			midiVelocityCmdHandling.setName("midiVelocityCmdHandling");
			midiVelocityCmdHandling.setText("Volume cmd = velocity");
			midiVelocityCmdHandling.setFont(Helpers.getDialogFont());
		}
		return midiVelocityCmdHandling;
	}

	private JPanel getSampleMapPanel()
	{
		if (sampleMapPanel==null)
		{
			sampleMapPanel = new JPanel();
			sampleMapPanel.setBorder(new TitledBorder(null, "Sample/Note Map", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, Helpers.getDialogFont(), null));
			sampleMapPanel.setLayout(new GridBagLayout());

			sampleMapPanel.add(getSampleMapScrollPane(), Helpers.getGridBagConstraint(0, 0, 1, 1, java.awt.GridBagConstraints.BOTH, java.awt.GridBagConstraints.WEST, 1.0, 1.0));

//			final Insets inset = getSampleMapScrollPane().getInsets();
//			final int scrollbarSpace = (getSampleMapScrollPane().getVerticalScrollBar().getPreferredSize().width<<1) + inset.left + inset.right;
//			final FontMetrics metrics = sampleMapPanel.getFontMetrics(Helpers.getDialogFont());
//			final Dimension d = new Dimension((SAMPLE_MAP_LINE_LENGTH*metrics.charWidth('0')) + scrollbarSpace, 12*metrics.getHeight());
//			sampleMapPanel.setSize(d);
//			sampleMapPanel.setMinimumSize(d);
//			sampleMapPanel.setMaximumSize(d);
//			sampleMapPanel.setPreferredSize(d);
		}
		return sampleMapPanel;
	}
	private JScrollPane getSampleMapScrollPane()
	{
		if (sampleMapScrollPane==null)
		{
			sampleMapScrollPane = new JScrollPane();
			sampleMapScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			sampleMapScrollPane.setViewportView(getSampleMap());
		}
		return sampleMapScrollPane;
	}
	private int markRowInSampleMap(final int newNoteIndexRow)
	{
		final int startPoint = (newNoteIndexRow<0)?0:newNoteIndexRow * SAMPLE_MAP_LINE_LENGTH;
		final int endPoint = (newNoteIndexRow<0)?0:startPoint + SAMPLE_MAP_LINE_LENGTH;
		try
		{
			getSampleMap().setCaretPosition(startPoint);
			getSampleMap().moveCaretPosition(endPoint);
			return newNoteIndexRow;
		}
		catch (final IllegalArgumentException ex)
		{
			// Ignore it...
		}
		return -1;
	}
	private int markRowInSampleMap(final Point mouseCursor)
	{
		final int modelPos = getSampleMap().viewToModel2D(mouseCursor);
		return markRowInSampleMap(modelPos / SAMPLE_MAP_LINE_LENGTH); // 15 characters per line incl. LF
	}
	private JTextArea getSampleMap()
	{
		if (sampleMap==null)
		{
			sampleMap = new JTextArea();
			sampleMap.setName("SampleMap");
			sampleMap.setEditable(false); // no editing
			sampleMap.setFont(Helpers.getTextAreaFont());
			final Caret caret = new DefaultCaret() // create a caret that does not hide when fokus is lost
			{
				private static final long serialVersionUID = 1927570313134336141L;
				/**
				 * @param e
				 * @see javax.swing.text.DefaultCaret#focusLost(java.awt.event.FocusEvent)
				 */
				@Override
				public void focusLost(final FocusEvent e)
				{
					super.focusLost(e);
					setSelectionVisible(true);
				}
			};
			sampleMap.setCaret(caret); // must be set to the element !before! doing anything with it, otherwise "this.component is null"
			caret.setVisible(false); // no cursor visible
			caret.setSelectionVisible(true); // but selection is visible
			// As in some cases, when the textbox gains focus, the cursor appears nevertheless, we just make it invisible...
			sampleMap.setCaretColor(sampleMap.getBackground());

			sampleMap.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mouseClicked(final MouseEvent e)
				{
					if (e.isConsumed() || instruments == null) return;

					final int newRow = markRowInSampleMap(e.getPoint());
					if (newRow==-1)
						markRowInSampleMap(noteIndexRow);
					else
					{
						noteIndexRow = newRow;
						if (SwingUtilities.isLeftMouseButton(e) && (e.getClickCount()>1))
						{
							// now get the sample and force sample dialog to open and show that:
							final int sampleIndex = getSampleIndex(getCurrentInstrument(), noteIndexRow);
							if (sampleIndex!=-1 && myModInfoPanel!=null) myModInfoPanel.showSample(sampleIndex);
						}
					}
				}
			});
		}
		return sampleMap;
	}
	private JTabbedPane getTabbedPane()
	{
		if (envelopeTabbedPane==null)
		{
			envelopeTabbedPane = new JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
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
	private String getFilterModeString(final int filterMode)
	{
		switch (filterMode)
		{
			case ModConstants.FLTMODE_HIGHPASS:
				return "force highpass";
			case ModConstants.FLTMODE_BANDPASS:
				return "force bandpass";
			case ModConstants.FLTMODE_LOWPASS:
				return "force lowpass";
			case ModConstants.FLTMODE_UNCHANGED:
			default:
				return "Channel default";
		}
	}
	private String getSampleMapString(final int[] noteIndex, final int[] sampleIndex)
	{
		final StringBuilder sb = new StringBuilder();
		for (int i=0; i<noteIndex.length; i++)
		{
			sb.append(ModConstants.getNoteNameForIndex(i+1)).append(" | ");
			if ((noteIndex[i]&0x80)!=0)
			{
				sb.append("... | ..\n");
			}
			else
			{
				sb.append(ModConstants.getNoteNameForIndex(noteIndex[i]+1))
				  .append(" | ");
				if (sampleIndex[i]>0)
					sb.append(ModConstants.getAsHex(sampleIndex[i], 2));
				else
					sb.append("..");
				sb.append('\n');
			}
		}
		return sb.toString();
	}
	private int getSampleIndex(final int instrumentIndex, final int row)
	{
		if (instruments!=null)
		{
			final Instrument instrument = instruments[instrumentIndex];
			if (instrument!=null)
			{
				final int noteIndex = instrument.getNoteIndex(row);
				if ((noteIndex&0x80)==0) return instrument.getSampleIndex(row);
			}
		}
		return -1;
	}
	private void clearInstrument()
	{
		spinnerModelData = new ArrayList<>(1);
		spinnerModelData.add(ModConstants.getAsHex(0, 2));
		getSelectInstrument().setModel(new SpinnerListModel(spinnerModelData));

		getButton_Play().setEnabled(false);
		markRowInSampleMap(-1);

		getInstrumentName().setText(Helpers.EMPTY_STING);
		getFileName().setText(Helpers.EMPTY_STING);

		getGlobalVolume().setText(Helpers.EMPTY_STING);
		getFadeOutVolume().setText(Helpers.EMPTY_STING);
		getSetPan().setFixedState(false);
		getSetPanValue().setText(Helpers.EMPTY_STING);

		getPitchPanSep().setText(Helpers.EMPTY_STING);
		getPitchPanCenter().setText(Helpers.EMPTY_STING);

		getRamping().setText(DEFAULT_STR);
		getResampling().setText(interpolationModel[0]);

		getSetResonance().setFixedState(false);
		getResonanceValue().setValue(-1);
		getSetCutOff().setFixedState(false);
		getCutOffValue().setValue(-1);
		getFilterMode().setText(Helpers.EMPTY_STING);

		getVolumeVariation().setValue(-1);
		getPanningVariation().setValue(-1);
		getResonanceVariation().setValue(-1);
		getCutOffVariation().setValue(-1);

		getActionNNA().setText(Helpers.EMPTY_STING);
		getCheckDNA().setText(Helpers.EMPTY_STING);
		getActionDNA().setText(Helpers.EMPTY_STING);

		getPlugin().setText(Helpers.EMPTY_STING);
		getMidiChannel().setText(Helpers.EMPTY_STING);
		getMidiProgram().setText(Helpers.EMPTY_STING);
		getMidiBank().setText(Helpers.EMPTY_STING);
		getPwdDepth().setText(Helpers.EMPTY_STING);

		getSampleMap().setText(Helpers.EMPTY_STING);
		getSampleMap().select(0,0);

		getVolumeEnvelopePanel().setEnvelope(null);
		getPanningEnvelopePanel().setEnvelope(null);
		getPitchEnvelopePanel().setEnvelope(null);

		// after setting the new model, make the editor of the spinner un-editable
		((DefaultEditor)getSelectInstrument().getEditor()).getTextField().setEditable(false);
	}
	private void fillWithInstrument(final Instrument newInstrument)
	{
		getInstrumentName().setText(newInstrument.name);
		getInstrumentName().setCaretPosition(0); getInstrumentName().moveCaretPosition(0);
		getFileName().setText(newInstrument.dosFileName);
		getFileName().setCaretPosition(0); getFileName().moveCaretPosition(0);

		getGlobalVolume().setText(Integer.toString(newInstrument.globalVolume));
		getFadeOutVolume().setText(Integer.toString(newInstrument.volumeFadeOut));
		getSetPan().setFixedState(newInstrument.setPanning);
		getSetPanValue().setText(Integer.toString(newInstrument.defaultPanning));

		getPitchPanSep().setText(Integer.toString(newInstrument.pitchPanSeparation));
		getPitchPanCenter().setText(ModConstants.getNoteNameForIndex(newInstrument.pitchPanCenter + 1));

		getRamping().setText((newInstrument.volRampUp>0)?Integer.toString(newInstrument.volRampUp):DEFAULT_STR);
		getResampling().setText(interpolationModel[(newInstrument.resampling<0)?0:newInstrument.resampling+1]);

		if (newInstrument.initialFilterResonance!=-1)
		{
			getSetResonance().setFixedState((newInstrument.initialFilterResonance&0x80)!=0);
			getResonanceValue().setValue(newInstrument.initialFilterResonance&0x7F);
		}
		else
		{
			getSetResonance().setFixedState(false);
			getResonanceValue().setValue(-1);
		}
		if (newInstrument.initialFilterCutoff!=-1)
		{
			getSetCutOff().setFixedState((newInstrument.initialFilterCutoff&0x80)!=0);
			getCutOffValue().setValue(newInstrument.initialFilterCutoff&0x7F);
		}
		else
		{
			getSetCutOff().setFixedState(false);
			getCutOffValue().setValue(-1);
		}
		getFilterMode().setText(getFilterModeString(newInstrument.filterMode));

		getVolumeVariation().setValue(newInstrument.randomVolumeVariation);
		getPanningVariation().setValue(newInstrument.randomPanningVariation);
		getResonanceVariation().setValue(newInstrument.randomResonanceVariation);
		getCutOffVariation().setValue(newInstrument.randomCutOffVariation);

		getActionNNA().setText(getNNAActionString(newInstrument.NNA));
		getCheckDNA().setText(getDNACheckString(newInstrument.dublicateNoteCheck));
		getActionDNA().setText(getDNAActionString(newInstrument.dublicateNoteAction));

		getPlugin().setText((newInstrument.mixPlugIn<=0)?"No Plugin":Integer.toString(newInstrument.mixPlugIn));
		getMidiChannel().setText((newInstrument.midiChannel<=0)?"none":Integer.toString(newInstrument.midiChannel));
		getMidiProgram().setText((newInstrument.midiProgram==0)?THREE_BARS:Integer.toString(newInstrument.midiProgram));
		getMidiBank().setText((newInstrument.midiBank==0)?THREE_BARS:Integer.toString(newInstrument.midiBank));
		getPwdDepth().setText(Integer.toString(newInstrument.pitchWheelDepth));
		final int index = (newInstrument.pluginVolumeHandling>ModMidiMixer.PLUGIN_VOLUMEHANDLING_NAMES.length)?ModMidiMixer.PLUGIN_VOLUMEHANDLING_IGNORE:newInstrument.pluginVolumeHandling;
		getMidiVolCmdHandling().setText(ModMidiMixer.PLUGIN_VOLUMEHANDLING_NAMES[index]);
		getMidiVelocityCmdHandling().setFixedState(newInstrument.pluginVelocityHandling == ModMidiMixer.PLUGIN_VELOCITYHANDLING_CHANNEL);

		getSampleMap().setText(getSampleMapString(newInstrument.noteIndex, newInstrument.sampleIndex));

		getVolumeEnvelopePanel().setEnvelope(newInstrument.volumeEnvelope);
		getPanningEnvelopePanel().setEnvelope(newInstrument.panningEnvelope);
		getPitchEnvelopePanel().setEnvelope(newInstrument.pitchEnvelope);

		markRowInSampleMap(noteIndexRow);
		getButton_Play().setEnabled(true);
	}
	public void showInstrument(final int instrumentIndex)
	{
		if (instruments!=null) getSelectInstrument().setValue(spinnerModelData.get(instrumentIndex));
	}
	public void fillWithInstrumentArray(final Instrument[] instruments)
	{
		this.instruments = instruments;
		if (instruments!=null)
		{
			spinnerModelData = new ArrayList<>(instruments.length);
			for (int i=0; i<instruments.length; i++) spinnerModelData.add(ModConstants.getAsHex(i+1, 2));
			getSelectInstrument().setModel(new SpinnerListModel(spinnerModelData));
			getSelectInstrument().setValue(spinnerModelData.get(0)); // in some unknown cases, the index is not really set.
			fillWithInstrument(instruments[0]); // as index is normally not changed, no change event is fired

			// after setting the new model, make the editor of the spinner un-editable
			((DefaultEditor)getSelectInstrument().getEditor()).getTextField().setEditable(false);
		}
		else
			clearInstrument();
	}
	/**
	 * For mute/unmute we need the current Mixer.
	 * ModContainer will take care of setting it. If no mixer is present,
	 * it is set to "null" here!
	 * @since 28.11.2023
	 * @param mixer
	 */
	public void setMixer(final ModMixer theModMixer)
	{
		currentModMixer = theModMixer;

		if (currentModMixer!=null)
		{
			currentMixer = currentModMixer.getModMixer();
		}
		else
		{
			if (currentMixer!=null)
			{
				currentMixer=null;
			}
		}
	}
}
