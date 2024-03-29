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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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
import de.quippy.javamod.multimedia.mod.gui.EnvelopePanel.EnvelopeType;
import de.quippy.javamod.multimedia.mod.loader.instrument.Instrument;
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
	private JScrollPane sampleMapScrollPane = null;
	private JTextArea sampleMap = null;

	private JTabbedPane envelopeTabbedPane = null;
	private EnvelopePanel volumeEnvelopePanel = null;
	private EnvelopePanel panningEnvelopePanel = null;
	private EnvelopePanel pitchEnvelopePanel = null;
	
	private SampleInstrumentPlayer player = null;
	private Instrument [] instruments = null;
	private ArrayList<String> spinnerModelData = null;
	private int noteIndexRow = ModConstants.BASENOTEINDEX;
	
	private ModInfoPanel myModInfoPanel;
	private ModMixer currentModMixer;
	private BasicModMixer currentMixer;

	/**
	 * Constructor for ModPatternDialog
	 */
	public ModInstrumentDialog(ModInfoPanel infoPanel)
	{
		super();
		myModInfoPanel = infoPanel;
		initialize();
	}
	/**
	 * Constructor for ModPatternDialog
	 * @param owner
	 * @param modal
	 */
	public ModInstrumentDialog(ModInfoPanel infoPanel, JFrame owner, boolean modal)
	{
		super(owner, modal);
		myModInfoPanel = infoPanel;
		initialize();
	}
	/**
	 * Constructor for ModPatternDialog
	 * @param owner
	 * @param modal
	 */
	public ModInstrumentDialog(ModInfoPanel infoPanel, JDialog owner, boolean modal)
	{
		super(owner, modal);
		myModInfoPanel = infoPanel;
		initialize();
	}
	private void initialize()
	{
        final Container baseContentPane = getContentPane();
		baseContentPane.setLayout(new java.awt.GridBagLayout());

		baseContentPane.add(getLabelSelectInstrument(), 	Helpers.getGridBagConstraint(0, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		baseContentPane.add(getSelectInstrument(), 			Helpers.getGridBagConstraint(1, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		baseContentPane.add(getInstrumentNamePanel(), 		Helpers.getGridBagConstraint(2, 0, 1, 3, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		baseContentPane.add(getButton_Play(), 				Helpers.getGridBagConstraint(5, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		baseContentPane.add(getGlobalInfoPanel(), 			Helpers.getGridBagConstraint(0, 1, 2, 3, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.NORTHWEST, 0.0, 0.0));
		baseContentPane.add(getFilterPanel(), 				Helpers.getGridBagConstraint(3, 1, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.NORTHWEST, 0.0, 0.0));
		baseContentPane.add(getNNAPanel(), 					Helpers.getGridBagConstraint(4, 1, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.NORTHWEST, 0.0, 0.0));
		baseContentPane.add(getSampleMapPanel(),			Helpers.getGridBagConstraint(5, 1, 2, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.NORTHWEST, 0.0, 0.0));
		baseContentPane.add(getRandomVariationPanel(), 		Helpers.getGridBagConstraint(3, 2, 1, 2, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.NORTHWEST, 0.0, 0.0));
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
				public void stateChanged(ChangeEvent e)
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

				public void actionPerformed(ActionEvent e)
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
			sampleMapPanel.setBorder(new TitledBorder(null, "Sample/Note Map", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, Helpers.getDialogFont(), null));
			sampleMapPanel.setLayout(new GridBagLayout());

			sampleMapPanel.add(getSampleMapScrollPane(), Helpers.getGridBagConstraint(0, 0, 1, 1, java.awt.GridBagConstraints.BOTH, java.awt.GridBagConstraints.WEST, 1.0, 1.0));
			
			final Insets inset = getSampleMapScrollPane().getInsets();
			final int scrollbarSpace = (getSampleMapScrollPane().getVerticalScrollBar().getPreferredSize().width<<1) + inset.left + inset.right; 
			final FontMetrics metrics = sampleMapPanel.getFontMetrics(Helpers.getDialogFont());
			final Dimension d = new Dimension((SAMPLE_MAP_LINE_LENGTH*metrics.charWidth('0')) + scrollbarSpace, 12*metrics.getHeight());
			sampleMapPanel.setSize(d);
			sampleMapPanel.setMinimumSize(d);
			sampleMapPanel.setMaximumSize(d);
			sampleMapPanel.setPreferredSize(d);
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
		catch (IllegalArgumentException ex)
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
				public void focusLost(FocusEvent e)
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
				public void mouseClicked(MouseEvent e)
				{
					if (e.isConsumed() || instruments == null) return;
					
					final int newRow = markRowInSampleMap(e.getPoint());
					if (newRow==-1)
						markRowInSampleMap(noteIndexRow);
					else
					{
						noteIndexRow = newRow;
						if (SwingUtilities.isLeftMouseButton(e))
						{
							if (e.getClickCount()>1)
							{
								// now get the sample and force sample dialog to open and show that:
								final int sampleIndex = getSampleIndex(getCurrentInstrument(), noteIndexRow);
								if (sampleIndex!=-1) myModInfoPanel.showSample(sampleIndex);
							}
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
				sb.append(ModConstants.getAsHex(sampleIndex[i], 2)).append('\n');
			}
		}
		return sb.toString();
	}
	private int getSampleIndex(final int instrumentIndex, final int row)
	{
		if (instruments!=null)
		{
			Instrument instrument = instruments[instrumentIndex];
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
		spinnerModelData = new ArrayList<String>(1);
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

		// after setting the new model, make the editor of the spinner un-editable
		((DefaultEditor)getSelectInstrument().getEditor()).getTextField().setEditable(false);
	}
	private void fillWithInstrument(Instrument newInstrument)
	{
		getInstrumentName().setText(newInstrument.name);
		getInstrumentName().setCaretPosition(0); getInstrumentName().moveCaretPosition(0);
		getFileName().setText(newInstrument.dosFileName);
		getFileName().setCaretPosition(0); getFileName().moveCaretPosition(0);
		
		getGlobalVolume().setText(Integer.toString(newInstrument.globalVolume));
		getFadeOutVolume().setText(Integer.toString(newInstrument.volumeFadeOut));
		getSetPan().setFixedState(newInstrument.defaultPan!=-1);
		getSetPanValue().setText(Integer.toString(newInstrument.defaultPan));
		
		getPitchPanSep().setText(Integer.toString(newInstrument.pitchPanSeparation));
		getPitchPanCenter().setText(ModConstants.getNoteNameForIndex(newInstrument.pitchPanCenter + 1));

		if (newInstrument.initialFilterResonance!=-1)
		{
			getSetResonance().setFixedState((newInstrument.initialFilterResonance&0x80)!=0);
			getResonanceValue().setText(Integer.toString(newInstrument.initialFilterResonance&0x7F));
		}
		else
		{
			getSetResonance().setFixedState(false);
			getResonanceValue().setText("-1");
		}
		if (newInstrument.initialFilterCutoff!=-1)
		{
			getSetCutOff().setFixedState((newInstrument.initialFilterCutoff&0x80)!=0);
			getCutOffValue().setText(Integer.toString(newInstrument.initialFilterCutoff&0x7F));
		}
		else
		{
			getSetCutOff().setFixedState(false);
			getCutOffValue().setText("-1");
		}
		
		getVolumeVariation().setText(Integer.toString(newInstrument.randomVolumeVariation));
		getPanningVariation().setText(Integer.toString(newInstrument.randomPanningVariation));
		getResonanceVariation().setText(Integer.toString(newInstrument.randomResonanceVariation));
		getCutOffVariation().setText(Integer.toString(newInstrument.randomCutOffVariation));
		
		getActionNNA().setText(getNNAActionString(newInstrument.NNA));
		getCheckDNA().setText(getDNACheckString(newInstrument.dublicateNoteCheck));
		getActionDNA().setText(getDNAActionString(newInstrument.dublicateNoteAction));
		
		getSampleMap().setText(getSampleMapString(newInstrument.noteIndex, newInstrument.sampleIndex));
		
		getVolumeEnvelopePanel().setEnvelope(newInstrument.volumeEnvelope, EnvelopeType.volume);
		getPanningEnvelopePanel().setEnvelope(newInstrument.panningEnvelope, EnvelopeType.panning);
		getPitchEnvelopePanel().setEnvelope(newInstrument.pitchEnvelope, EnvelopeType.pitch);

		markRowInSampleMap(noteIndexRow);
		getButton_Play().setEnabled(true);
	}
	public void fillWithInstrumentArray(final Instrument [] instruments)
	{
		this.instruments = instruments;
		if (instruments!=null)
		{
			spinnerModelData = new ArrayList<String>(instruments.length);
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
	public void setMixer(ModMixer theModMixer)
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
