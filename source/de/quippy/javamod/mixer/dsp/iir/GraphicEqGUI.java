/*
 * @(#) GraphicEqGUI.java
 *
 * Created on 15.01.2012 by Daniel Becker
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
package de.quippy.javamod.mixer.dsp.iir;

import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.quippy.javamod.system.Helpers;

/**
 * @author Daniel Becker
 * @since 15.01.2012
 */
public class GraphicEqGUI extends JPanel
{
	private static final long serialVersionUID = 8091057988399658762L;
	
	private static final int SHIFT_DB = 100;
	private static final int SLIDER_MAX = 20  * SHIFT_DB;
	private static final int SLIDER_MIN = -20 * SHIFT_DB;
	private static final String DEZIBEL = "db";

	private GraphicEQ eq;
	
	private JPanel selectionPanel = null;
	private JPanel bandsPanel = null;
	private JPanel preAmpPanel = null;
	private JSlider [] sliders = null;
	private JLabel  [] slidersLable = null;
	private JSlider preAmpSlider = null;
	private JLabel  preAmpSliderLable = null;
	private JLabel minLabel = null;
	private JLabel centerLabel = null;
	private JLabel maxLabel = null;
	private JCheckBox equalizerActive = null;
	private JLabel presetSelectionLabel = null;
	private JComboBox<String> presetSelection = null;
	
	private boolean presetsActive;
	
	private static final String PRESET_NAMES[]=
	{
	 	"Select a preset...",
	 	"Flat",
	 	"Classical",
	 	"Club",
	 	"Cristal",
	 	"Dance",
	 	"Full bass",
	 	"Full bass & treble",
	 	"Full treble",
	 	"Laptop",
	 	"Live",
	 	"Party",
	 	"Pop",
	 	"Reggae",
	 	"Rock",
	 	"Techno",
	};
	
	private static final int PRESET_DB[][] =
	{
		{  0,   0,   0,   0,   0,   0,   0,   0,   0,   0},
		{  0,   0,   0,   0,   0,   0,   0,   0,   0,   0},
		{  0,   0,   0,   0,   0,   0,  -8,  -8,  -8, -10},
		{  0,   0,   4,   7,   7,   7,   4,   0,   0,   0},
		{ 20,  15,  10,   5,   0,   0,   5,  10,  15,  20},
		{ 10,   7,   2,   0,   0,  -6,  -8,  -8,   0,   0},
		{ 10,  10,  10,   6,   2,  -4, -10, -11, -11, -11},
		{  7,   7,   0,  -7,  -4,   2,   9,  12,  13,  13},
		{-11, -11, -11,  -4,   4,  11,  17,  17,  17,  17},
		{  5,  12,   6,  -4,  -3,   2,   5,  11,  14,  15},
		{ -6,   0,   4,   6,   7,   7,   4,   4,   4,   4},
		{  8,   8,   0,   0,   0,   0,   0,   0,   8,   8},
		{ -2,   5,   8,   8,   5,  -1,  -2,  -2,  -1,  -1},
		{  1,   1,   0,  -6,   1,   7,   7,   1,   1,   1},
		{  8,   5,  -5,  -8,  -2,   4,   9,  11,  11,  11},
		{  8,   7,   1,  -6,  -5,   1,   8,  11,  11,   9}
	};

	/**
	 * Constructor for GraphicEqGUI
	 */
	public GraphicEqGUI(GraphicEQ equalizer)
	{
		super();
		if (equalizer==null) throw new IllegalArgumentException("Equalizer must not be null!");
		eq = equalizer;
		presetsActive = eq.getBandCount()==10;
		initialize();
	}
	private void initialize()
	{
		setName("Equalizer");
		setLayout(new java.awt.GridBagLayout());
		add(getSelectionPanel(),	Helpers.getGridBagConstraint(0, 0, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.NORTHWEST, 1.0, 0.0));
		add(getBandsPanel(),		Helpers.getGridBagConstraint(0, 1, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.NORTHWEST, 1.0, 0.0));
		add(getPreAmpPanel(),		Helpers.getGridBagConstraint(1, 1, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.NORTHWEST, 1.0, 0.0));
	}
	private JPanel getSelectionPanel()
	{
		if (selectionPanel==null)
		{
			selectionPanel = new JPanel();
			selectionPanel.setName("selectionPanel");
			selectionPanel.setLayout(new GridBagLayout());
			selectionPanel.setBorder(new TitledBorder(null, "Selections", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, Helpers.getDialogFont(), null));
			selectionPanel.add(getEqualizerActive(),		Helpers.getGridBagConstraint(0, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
			selectionPanel.add(getPresetSelectionLabel(),	Helpers.getGridBagConstraint(1, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.EAST, 1.0, 0.0));
			selectionPanel.add(getPresetSelection(),		Helpers.getGridBagConstraint(2, 0, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.EAST, 1.0, 0.0));
		}
		return selectionPanel;
	}
	private JCheckBox getEqualizerActive()
	{
		if (equalizerActive == null)
		{
			equalizerActive = new javax.swing.JCheckBox();
			equalizerActive.setName("equalizerActive");
			equalizerActive.setText("activate equalizer");
			equalizerActive.setFont(Helpers.getDialogFont());
			if (eq!=null) equalizerActive.setSelected(eq.isActive());
			equalizerActive.addItemListener(new ItemListener()
			{
				public void itemStateChanged(ItemEvent e)
				{
					if (e.getStateChange()==ItemEvent.SELECTED || e.getStateChange()==ItemEvent.DESELECTED)
					{
						if (eq!=null) eq.setIsActive(getEqualizerActive().isSelected());
					}
				}
			});
		}
		return equalizerActive;
	}
	private JLabel getPresetSelectionLabel()
	{
		if (presetSelectionLabel==null)
		{
			presetSelectionLabel = new JLabel("Presets:");
			presetSelectionLabel.setFont(Helpers.getDialogFont());
			presetSelectionLabel.setEnabled(presetsActive);
		}
		return presetSelectionLabel;
	}
	private JComboBox<String> getPresetSelection()
	{
		if (presetSelection == null)
		{
			presetSelection = new JComboBox<String>();
			presetSelection.setName("presetSelection");

			DefaultComboBoxModel<String> theModel = new DefaultComboBoxModel<String>(PRESET_NAMES);
			presetSelection.setModel(theModel);
			presetSelection.setFont(Helpers.getDialogFont());
			presetSelection.setEnabled(presetsActive);
			presetSelection.addItemListener(new ItemListener()
			{
				public void itemStateChanged(ItemEvent e)
				{
					if (e.getStateChange()==ItemEvent.SELECTED)
					{
						setPreset(getPresetSelection().getSelectedIndex());
					}
				}
			});
		}
		return presetSelection;
	}
	private JSlider createDefaultSlider(float value)
	{
		if (value>(SLIDER_MAX / SHIFT_DB)) value = SLIDER_MAX / SHIFT_DB;
		else 
		if (value<(SLIDER_MIN / SHIFT_DB)) value = SLIDER_MIN / SHIFT_DB;
		JSlider slider = new JSlider(JSlider.VERTICAL, SLIDER_MIN, SLIDER_MAX, (int)(value*SHIFT_DB));
		slider.setFont(Helpers.getDialogFont());
		slider.setMinorTickSpacing(5*SHIFT_DB);
		slider.setMajorTickSpacing(10*SHIFT_DB);
		slider.setPaintTicks(true);
		slider.setSnapToTicks(false);
		slider.setPaintLabels(false);
		slider.setPaintTrack(true);
		slider.setToolTipText(Float.toString(Math.round(value*10f)/10f) + DEZIBEL);
		slider.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				if (e.getClickCount()>1)
				{
					((JSlider)e.getSource()).setValue(0);
					e.consume();
				}
			}
		});
		return slider;
	}
	private JPanel getBandsPanel()
	{
		if (bandsPanel == null)
		{
			bandsPanel = new JPanel();
			bandsPanel.setName("bandsPanel");
			bandsPanel.setLayout(new GridBagLayout());
			bandsPanel.setBorder(new TitledBorder(null, "Bands", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, Helpers.getDialogFont(), null));

			maxLabel = new JLabel("+" + Integer.toString(SLIDER_MAX/SHIFT_DB));
			maxLabel.setFont(Helpers.getDialogFont());
			bandsPanel.add(maxLabel,	Helpers.getGridBagConstraint(0, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.NORTH, 0.0, 1.0));
			centerLabel = new JLabel("0" + DEZIBEL);
			centerLabel.setFont(Helpers.getDialogFont());
			bandsPanel.add(centerLabel,	Helpers.getGridBagConstraint(0, 1, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.CENTER, 0.0, 1.0));
			minLabel = new JLabel(Integer.toString(SLIDER_MIN/SHIFT_DB));
			minLabel.setFont(Helpers.getDialogFont());
			bandsPanel.add(minLabel,	Helpers.getGridBagConstraint(0, 2, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.SOUTH, 0.0, 1.0));
			
			int bandCount = eq.getBandCount();
			sliders = new JSlider[bandCount];
			slidersLable = new JLabel[bandCount];
			for (int i=0; i<bandCount; i++)
			{
				sliders[i] = createDefaultSlider(eq.getBand(i));
				sliders[i].setName(Integer.toString(i));
				sliders[i].addChangeListener(new ChangeListener()
				{
					public void stateChanged(ChangeEvent e)
					{
						final JSlider slider = ((JSlider)e.getSource());
						final String sliderName = slider.getName();
						final int bandIndex = Integer.parseInt(sliderName);
						final int value = slider.getValue();
						eq.setBand(bandIndex, (float)value/(float)SHIFT_DB);
						slider.setToolTipText(Float.toString(Math.round(eq.getBand(bandIndex)*10f)/10f) + DEZIBEL);
					}
				});
				int centerFreq = eq.getCenterFreq(i);
				String lableString = (centerFreq >= 1000)?Integer.toString(centerFreq/1000) + "k":Integer.toString(centerFreq);
				slidersLable[i] = new JLabel(lableString);
				slidersLable[i].setFont(Helpers.getDialogFont());
				bandsPanel.add(sliders[i], 		Helpers.getGridBagConstraint(i+1, 0, 3, 1, java.awt.GridBagConstraints.VERTICAL, java.awt.GridBagConstraints.CENTER, 0.0, 1.0));
				bandsPanel.add(slidersLable[i],	Helpers.getGridBagConstraint(i+1, 3, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.CENTER, 0.0, 0.0));
			}
		}
		return bandsPanel;
	}
	private JPanel getPreAmpPanel()
	{
		if (preAmpPanel == null)
		{
			preAmpPanel = new JPanel();
			preAmpPanel.setName("preAmpPanel");
			preAmpPanel.setLayout(new GridBagLayout());
			preAmpPanel.setBorder(new TitledBorder(null, "Pre Amp", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, Helpers.getDialogFont(), null));
			preAmpSlider = createDefaultSlider(eq.getPreAmpDB());
			preAmpSlider.setName("PreAmp");
			preAmpSlider.addChangeListener(new ChangeListener()
			{
				public void stateChanged(ChangeEvent e)
				{
					JSlider slider = ((JSlider)e.getSource());
					int value = slider.getValue();
					eq.setPreAmp((float)value/(float)SHIFT_DB);
					slider.setToolTipText(Float.toString(Math.round(eq.getPreAmpDB()*10f)/10f) + DEZIBEL);
				}
			});
			preAmpSliderLable = new JLabel("PreAmp");
			preAmpSliderLable.setFont(Helpers.getDialogFont());
			preAmpPanel.add(preAmpSlider, 		Helpers.getGridBagConstraint(0, 0, 1, 0, java.awt.GridBagConstraints.VERTICAL, java.awt.GridBagConstraints.CENTER, 0.0, 1.0));
			preAmpPanel.add(preAmpSliderLable,	Helpers.getGridBagConstraint(0, 1, 1, 0, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.CENTER, 0.0, 0.0));
		}
		return preAmpPanel;
	}
	private void setPreset(int index)
	{
		int [] preset = PRESET_DB[index];
		for (int i=0; i<preset.length; i++)
		{
			sliders[i].setValue(preset[i] * SHIFT_DB);
		}
	}
}
