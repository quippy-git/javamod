/*
 * @(#) ModConfigPanel.java
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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Properties;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.quippy.javamod.mixer.dsp.iir.filter.Dither;
import de.quippy.javamod.system.Helpers;

/**
 * @author Daniel Becker
 * @since 13.10.2007
 */
public class ModConfigPanel extends JPanel
{
	private static final long serialVersionUID = -3417460833901933361L;

	private JCheckBox playerSetUp_WideStereoMix = null;
	private JCheckBox playerSetUp_NoiseReduction = null;
	private JCheckBox playerSetUp_MegaBass = null;
	private JCheckBox playerSetUp_DCRemoval = null;
	private JCheckBox playerSetUp_fadeOutLoops = null;
	private JCheckBox playerSetUp_ignoreLoops = null;
	private JCheckBox playerSetUp_loopSong = null;
	private JLabel playerSetUp_L_Channels = null;
	private JComboBox<String> playerSetUp_Channels = null;
	private JLabel playerSetUp_L_BitsPerSample = null;
	private JComboBox<String> playerSetUp_BitsPerSample = null;
	private JLabel playerSetUp_L_SampleRate = null;
	private JComboBox<String> playerSetUp_SampleRate = null;
	private JLabel playerSetUp_L_BufferSize = null;
	private JComboBox<String> playerSetUp_BufferSize = null;
	private JLabel playerSetUp_L_Interpolation = null;
	private JComboBox<String> playerSetUp_Interpolation = null;
	private JLabel playerSetUp_L_MaxNNAChannels = null;
	private JComboBox<String> playerSetUp_MaxNNAChannels = null;
	private JLabel playerSetUp_L_DitherFilterType = null;
	private JComboBox<String> playerSetUp_DitherFilterType = null;
	private JLabel playerSetUp_L_DitherType = null;
	private JComboBox<String> playerSetUp_DitherType = null;
	private JCheckBox playerSetUp_ByPassDither = null;

	private ModContainer parentContainer = null;

	/**
	 * Constructor for ModConfigPanel
	 */
	public ModConfigPanel()
	{
		super();
		initialize();
	}
	/**
	 * Constructor for ModConfigPanel
	 * @param layout
	 */
	public ModConfigPanel(final LayoutManager layout)
	{
		super(layout);
		initialize();
	}
	/**
	 * Constructor for ModConfigPanel
	 * @param isDoubleBuffered
	 */
	public ModConfigPanel(final boolean isDoubleBuffered)
	{
		super(isDoubleBuffered);
		initialize();
	}
	/**
	 * Constructor for ModConfigPanel
	 * @param layout
	 * @param isDoubleBuffered
	 */
	public ModConfigPanel(final LayoutManager layout, final boolean isDoubleBuffered)
	{
		super(layout, isDoubleBuffered);
		initialize();
	}
	/**
	 * @return the parent
	 */
	public ModContainer getParentContainer()
	{
		return parentContainer;
	}
	/**
	 * @param parent the parent to set
	 */
	public void setParentContainer(final ModContainer parent)
	{
		this.parentContainer = parent;
	}
	private void initialize()
	{
		this.setName("ModConfigPane");
		this.setLayout(new java.awt.GridBagLayout());

		this.add(getPlayerSetUp_WideStereoMix(),		Helpers.getGridBagConstraint(0, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getPlayerSetUp_NoiseReduction(),		Helpers.getGridBagConstraint(1, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getPlayerSetUp_MegaBass(),				Helpers.getGridBagConstraint(2, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getPlayerSetUp_DCRemoval(),			Helpers.getGridBagConstraint(3, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getPlayerSetUp_fadeOutLoops(),			Helpers.getGridBagConstraint(4, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));

		this.add(getPlayerSetUp_L_SampleRate(),			Helpers.getGridBagConstraint(0, 1, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getPlayerSetUp_L_Channels(),			Helpers.getGridBagConstraint(1, 1, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getPlayerSetUp_L_BitsPerSample(),		Helpers.getGridBagConstraint(2, 1, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getPlayerSetUp_L_BufferSize(),			Helpers.getGridBagConstraint(3, 1, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getPlayerSetUp_ignoreLoops(),			Helpers.getGridBagConstraint(4, 1, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));

		this.add(getPlayerSetUp_SampleRate(),			Helpers.getGridBagConstraint(0, 2, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		this.add(getPlayerSetUp_Channels(),				Helpers.getGridBagConstraint(1, 2, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		this.add(getPlayerSetUp_BitsPerSample(),		Helpers.getGridBagConstraint(2, 2, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		this.add(getPlayerSetUp_BufferSize(),			Helpers.getGridBagConstraint(3, 2, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		this.add(getPlayerSetUp_loopSong(),				Helpers.getGridBagConstraint(4, 2, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));

		this.add(getPlayerSetUp_L_DitherType(),			Helpers.getGridBagConstraint(0, 3, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getPlayerSetUp_L_DitherFilterType(),	Helpers.getGridBagConstraint(1, 3, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getPlayerSetUp_ByPassDither(),			Helpers.getGridBagConstraint(2, 3, 2, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		this.add(getPlayerSetUp_L_MaxNNAChannels(),		Helpers.getGridBagConstraint(3, 3, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getPlayerSetUp_L_Interpolation(),		Helpers.getGridBagConstraint(4, 3, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));

		this.add(getPlayerSetUp_DitherType(),			Helpers.getGridBagConstraint(0, 4, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		this.add(getPlayerSetUp_DitherFilterType(),		Helpers.getGridBagConstraint(1, 4, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		this.add(getPlayerSetUp_MaxNNAChannels(),		Helpers.getGridBagConstraint(3, 4, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		this.add(getPlayerSetUp_Interpolation(),		Helpers.getGridBagConstraint(4, 4, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
	}
	private JCheckBox getPlayerSetUp_WideStereoMix()
	{
		if (playerSetUp_WideStereoMix == null)
		{
			playerSetUp_WideStereoMix = new JCheckBox();
			playerSetUp_WideStereoMix.setName("playerSetUp_WideStereoMix");
			playerSetUp_WideStereoMix.setText("Surround Mix");
			playerSetUp_WideStereoMix.setFont(Helpers.getDialogFont());
			playerSetUp_WideStereoMix.addItemListener(new ItemListener()
			{
				@Override
				public void itemStateChanged(final ItemEvent e)
				{
					if (e.getStateChange()==ItemEvent.SELECTED || e.getStateChange()==ItemEvent.DESELECTED)
					{
						final ModContainer parent = getParentContainer();
						if (parent!=null)
						{
							final ModMixer currentMixer = parent.getCurrentMixer();
							if (currentMixer!=null)
								currentMixer.setDoWideStereoMix(getPlayerSetUp_WideStereoMix().isSelected());
						}
					}
				}
			});
		}
		return playerSetUp_WideStereoMix;
	}
	private JCheckBox getPlayerSetUp_NoiseReduction()
	{
		if (playerSetUp_NoiseReduction==null)
		{
			playerSetUp_NoiseReduction = new JCheckBox();
			playerSetUp_NoiseReduction.setName("playerSetUp_NoiseReduction");
			playerSetUp_NoiseReduction.setText("Noise Reduction");
			playerSetUp_NoiseReduction.setFont(Helpers.getDialogFont());
			playerSetUp_NoiseReduction.addItemListener(new ItemListener()
			{
				@Override
				public void itemStateChanged(final ItemEvent e)
				{
					if (e.getStateChange()==ItemEvent.SELECTED || e.getStateChange()==ItemEvent.DESELECTED)
					{
						final ModContainer parent = getParentContainer();
						if (parent!=null)
						{
							final ModMixer currentMixer = parent.getCurrentMixer();
							if (currentMixer!=null)
								currentMixer.setDoNoiseReduction(getPlayerSetUp_NoiseReduction().isSelected());
						}
					}
				}
			});
		}
		return playerSetUp_NoiseReduction;
	}
	private JCheckBox getPlayerSetUp_MegaBass()
	{
		if (playerSetUp_MegaBass==null)
		{
			playerSetUp_MegaBass = new JCheckBox();
			playerSetUp_MegaBass.setName("playerSetUp_MegaBass");
			playerSetUp_MegaBass.setText("Bass Boost");
			playerSetUp_MegaBass.setFont(Helpers.getDialogFont());
			playerSetUp_MegaBass.addItemListener(new ItemListener()
			{
				@Override
				public void itemStateChanged(final ItemEvent e)
				{
					if (e.getStateChange()==ItemEvent.SELECTED || e.getStateChange()==ItemEvent.DESELECTED)
					{
						final ModContainer parent = getParentContainer();
						if (parent!=null)
						{
							final boolean selected = getPlayerSetUp_MegaBass().isSelected();
							// With Mega Bass, we should also do DC Removal - so activate it automatically. User can still deselect it again
							if (selected) getPlayerSetUp_DCRemoval().setSelected(true);
							final ModMixer currentMixer = parent.getCurrentMixer();
							if (currentMixer!=null)
								currentMixer.setDoMegaBass(selected);
						}
					}
				}
			});
		}
		return playerSetUp_MegaBass;
	}
	private JCheckBox getPlayerSetUp_DCRemoval()
	{
		if (playerSetUp_DCRemoval==null)
		{
			playerSetUp_DCRemoval = new JCheckBox();
			playerSetUp_DCRemoval.setName("playerSetUp_DCRemoval");
			playerSetUp_DCRemoval.setText("DC Removal");
			playerSetUp_DCRemoval.setFont(Helpers.getDialogFont());
			playerSetUp_DCRemoval.addItemListener(new ItemListener()
			{
				@Override
				public void itemStateChanged(final ItemEvent e)
				{
					if (e.getStateChange()==ItemEvent.SELECTED || e.getStateChange()==ItemEvent.DESELECTED)
					{
						final ModContainer parent = getParentContainer();
						if (parent!=null)
						{
							final ModMixer currentMixer = parent.getCurrentMixer();
							if (currentMixer!=null)
								currentMixer.setDoDCRemoval(getPlayerSetUp_DCRemoval().isSelected());
						}
					}
				}
			});
		}
		return playerSetUp_DCRemoval;
	}
	private int getLoopValue()
	{
		int value = ModConstants.PLAYER_LOOP_DEACTIVATED;
		if (getPlayerSetUp_fadeOutLoops().isSelected()) value |= ModConstants.PLAYER_LOOP_FADEOUT;
		if (getPlayerSetUp_ignoreLoops().isSelected()) value |= ModConstants.PLAYER_LOOP_IGNORE;
		if (getPlayerSetUp_loopSong().isSelected()) value |= ModConstants.PLAYER_LOOP_LOOPSONG;
		return value;
	}
	/**
	 * Never ever call this Method from an event handler for those
	 * two buttons - endless loop!
	 * @param newLoopValue
	 * @since 11.01.2012
	 */
	private void setLoopValue(final int newLoopValue)
	{
		if (newLoopValue == ModConstants.PLAYER_LOOP_DEACTIVATED)
		{
			getPlayerSetUp_fadeOutLoops().setSelected(false);
			getPlayerSetUp_ignoreLoops().setSelected(false);
			getPlayerSetUp_loopSong().setSelected(false);
		}
		if ((newLoopValue&ModConstants.PLAYER_LOOP_FADEOUT)!=0)
		{
			getPlayerSetUp_fadeOutLoops().setSelected(true);
			getPlayerSetUp_ignoreLoops().setSelected(false);
		}
		else
		if ((newLoopValue&ModConstants.PLAYER_LOOP_IGNORE)!=0)
		{
			getPlayerSetUp_fadeOutLoops().setSelected(false);
			getPlayerSetUp_ignoreLoops().setSelected(true);
		}
		if ((newLoopValue&ModConstants.PLAYER_LOOP_LOOPSONG)!=0)
		{
			getPlayerSetUp_loopSong().setSelected(true);
		}
	}
	private void configMixerWithLoopValue()
	{
		final ModContainer parent = getParentContainer();
		if (parent!=null)
		{
			final ModMixer currentMixer = parent.getCurrentMixer();
			if (currentMixer!=null)
				currentMixer.setDoNoLoops(getLoopValue());
		}

	}
	private JCheckBox getPlayerSetUp_fadeOutLoops()
	{
		if (playerSetUp_fadeOutLoops==null)
		{
			playerSetUp_fadeOutLoops = new JCheckBox();
			playerSetUp_fadeOutLoops.setName("playerSetUp_fadeOutLoops");
			playerSetUp_fadeOutLoops.setText("Fade out infinite loops");
			playerSetUp_fadeOutLoops.setFont(Helpers.getDialogFont());
			playerSetUp_fadeOutLoops.addItemListener(new ItemListener()
			{
				@Override
				public void itemStateChanged(final ItemEvent e)
				{
					if (e.getStateChange()==ItemEvent.SELECTED || e.getStateChange()==ItemEvent.DESELECTED)
					{
						if (getPlayerSetUp_fadeOutLoops().isSelected())
							getPlayerSetUp_ignoreLoops().setSelected(false);
						configMixerWithLoopValue();
					}
				}
			});
		}
		return playerSetUp_fadeOutLoops;
	}
	private JCheckBox getPlayerSetUp_ignoreLoops()
	{
		if (playerSetUp_ignoreLoops==null)
		{
			playerSetUp_ignoreLoops = new JCheckBox();
			playerSetUp_ignoreLoops.setName("playerSetUp_ignoreLoops");
			playerSetUp_ignoreLoops.setText("Ignore infinite loops");
			playerSetUp_ignoreLoops.setFont(Helpers.getDialogFont());
			playerSetUp_ignoreLoops.addItemListener(new ItemListener()
			{
				@Override
				public void itemStateChanged(final ItemEvent e)
				{
					if (e.getStateChange()==ItemEvent.SELECTED || e.getStateChange()==ItemEvent.DESELECTED)
					{
						if (getPlayerSetUp_ignoreLoops().isSelected())
							getPlayerSetUp_fadeOutLoops().setSelected(false);
						configMixerWithLoopValue();
					}
				}
			});
		}
		return playerSetUp_ignoreLoops;
	}
	private JCheckBox getPlayerSetUp_loopSong()
	{
		if (playerSetUp_loopSong==null)
		{
			playerSetUp_loopSong = new JCheckBox();
			playerSetUp_loopSong.setName("playerSetUp_loopSong");
			playerSetUp_loopSong.setText("Loop song");
			playerSetUp_loopSong.setFont(Helpers.getDialogFont());
			playerSetUp_loopSong.addItemListener(new ItemListener()
			{
				@Override
				public void itemStateChanged(final ItemEvent e)
				{
					if (e.getStateChange()==ItemEvent.SELECTED || e.getStateChange()==ItemEvent.DESELECTED)
					{
						configMixerWithLoopValue();
					}
				}
			});
		}
		return playerSetUp_loopSong;
	}
	private JLabel getPlayerSetUp_L_BitsPerSample()
	{
		if (playerSetUp_L_BitsPerSample==null)
		{
			playerSetUp_L_BitsPerSample = new JLabel();
			playerSetUp_L_BitsPerSample.setName("playerSetUp_L_BitsPerSample");
			playerSetUp_L_BitsPerSample.setText("Resolution");
			playerSetUp_L_BitsPerSample.setFont(Helpers.getDialogFont());
		}
		return playerSetUp_L_BitsPerSample;
	}
	private JComboBox<String> getPlayerSetUp_BitsPerSample()
	{
		if (playerSetUp_BitsPerSample == null)
		{
			playerSetUp_BitsPerSample = new JComboBox<>();
			playerSetUp_BitsPerSample.setName("playerSetUp_BitsPerSample");

			final DefaultComboBoxModel<String> theModel = new DefaultComboBoxModel<>(ModContainer.BITSPERSAMPLE);
			playerSetUp_BitsPerSample.setModel(theModel);
			playerSetUp_BitsPerSample.setFont(Helpers.getDialogFont());
			playerSetUp_BitsPerSample.setEnabled(true);
			playerSetUp_BitsPerSample.addItemListener(new ItemListener()
			{
				@Override
				public void itemStateChanged(final ItemEvent e)
				{
					if (e.getStateChange()==ItemEvent.SELECTED)
					{
						final ModContainer parent = getParentContainer();
						if (parent!=null)
						{
							final ModMixer currentMixer = parent.getCurrentMixer();
							if (currentMixer!=null)
								currentMixer.setSampleSizeInBits(Integer.parseInt(getPlayerSetUp_BitsPerSample().getSelectedItem().toString()));
						}
					}
				}
			});
		}
		return playerSetUp_BitsPerSample;
	}
	private JLabel getPlayerSetUp_L_Channels()
	{
		if (playerSetUp_L_Channels==null)
		{
			playerSetUp_L_Channels = new JLabel();
			playerSetUp_L_Channels.setName("playerSetUp_L_Channels");
			playerSetUp_L_Channels.setText("Channels");
			playerSetUp_L_Channels.setFont(Helpers.getDialogFont());
		}
		return playerSetUp_L_Channels;
	}
	private JComboBox<String> getPlayerSetUp_Channels()
	{
		if (playerSetUp_Channels==null)
		{
			playerSetUp_Channels = new JComboBox<>();
			playerSetUp_Channels.setName("playerSetUp_Channels");

			final DefaultComboBoxModel<String> theModel = new DefaultComboBoxModel<>(ModContainer.CHANNELS);
			playerSetUp_Channels.setModel(theModel);
			playerSetUp_Channels.setFont(Helpers.getDialogFont());
			playerSetUp_Channels.setEnabled(true);
			playerSetUp_Channels.addItemListener(new ItemListener()
			{
				@Override
				public void itemStateChanged(final ItemEvent e)
				{
					if (e.getStateChange()==ItemEvent.SELECTED)
					{
						final ModContainer parent = getParentContainer();
						if (parent!=null)
						{
							final ModMixer currentMixer = parent.getCurrentMixer();
							if (currentMixer!=null)
								currentMixer.setChannels(Integer.parseInt(getPlayerSetUp_Channels().getSelectedItem().toString()));
						}
					}
				}
			});
		}
		return playerSetUp_Channels;
	}
	private JLabel getPlayerSetUp_L_SampleRate()
	{
		if (playerSetUp_L_SampleRate==null)
		{
			playerSetUp_L_SampleRate = new JLabel();
			playerSetUp_L_SampleRate.setName("playerSetUp_L_SampleRate");
			playerSetUp_L_SampleRate.setText("Frequency");
			playerSetUp_L_SampleRate.setFont(Helpers.getDialogFont());
		}
		return playerSetUp_L_SampleRate;
	}
	private JComboBox<String> getPlayerSetUp_SampleRate()
	{
		if (playerSetUp_SampleRate==null)
		{
			playerSetUp_SampleRate = new JComboBox<>();
			playerSetUp_SampleRate.setName("playerSetUp_SampleRate");

			final DefaultComboBoxModel<String> theModel = new DefaultComboBoxModel<>(ModContainer.SAMPLERATE);
			playerSetUp_SampleRate.setModel(theModel);
			playerSetUp_SampleRate.setFont(Helpers.getDialogFont());
			playerSetUp_SampleRate.setEnabled(true);
			playerSetUp_SampleRate.addItemListener(new ItemListener()
			{
				@Override
				public void itemStateChanged(final ItemEvent e)
				{
					if (e.getStateChange()==ItemEvent.SELECTED)
					{
						final ModContainer parent = getParentContainer();
						if (parent!=null)
						{
							final ModMixer currentMixer = parent.getCurrentMixer();
							if (currentMixer!=null)
								currentMixer.setSampleRate(Integer.parseInt(getPlayerSetUp_SampleRate().getSelectedItem().toString()));
						}
					}
				}
			});
		}
		return playerSetUp_SampleRate;
	}
	private JLabel getPlayerSetUp_L_BufferSize()
	{
		if (playerSetUp_L_BufferSize==null)
		{
			playerSetUp_L_BufferSize = new JLabel();
			playerSetUp_L_BufferSize.setName("playerSetUp_BufferSize");
			playerSetUp_L_BufferSize.setText("ms buffer size");
			playerSetUp_L_BufferSize.setFont(Helpers.getDialogFont());
		}
		return playerSetUp_L_BufferSize;
	}
	private JComboBox<String> getPlayerSetUp_BufferSize()
	{
		if (playerSetUp_BufferSize==null)
		{
			playerSetUp_BufferSize = new JComboBox<>();
			playerSetUp_BufferSize.setName("playerSetUp_BufferSize");

			final DefaultComboBoxModel<String> theModel = new DefaultComboBoxModel<>(ModContainer.BUFFERSIZE);
			playerSetUp_BufferSize.setModel(theModel);
			playerSetUp_BufferSize.setFont(Helpers.getDialogFont());
			playerSetUp_BufferSize.setEnabled(true);
			playerSetUp_BufferSize.addItemListener(new ItemListener()
			{
				@Override
				public void itemStateChanged(final ItemEvent e)
				{
					if (e.getStateChange()==ItemEvent.SELECTED)
					{
						final ModContainer parent = getParentContainer();
						if (parent!=null)
						{
							final ModMixer currentMixer = parent.getCurrentMixer();
							if (currentMixer!=null)
								currentMixer.setBufferSize(Integer.parseInt(getPlayerSetUp_BufferSize().getSelectedItem().toString()));
						}
					}
				}
			});
		}
		return playerSetUp_BufferSize;
	}
	private JLabel getPlayerSetUp_L_Interpolation()
	{
		if (playerSetUp_L_Interpolation==null)
		{
			playerSetUp_L_Interpolation = new JLabel();
			playerSetUp_L_Interpolation.setName("playerSetUp_L_Interpolation");
			playerSetUp_L_Interpolation.setText("Interpolation");
			playerSetUp_L_Interpolation.setFont(Helpers.getDialogFont());
		}
		return playerSetUp_L_Interpolation;
	}
	private JComboBox<String> getPlayerSetUp_Interpolation()
	{
		if (playerSetUp_Interpolation==null)
		{
			playerSetUp_Interpolation = new JComboBox<>();
			playerSetUp_Interpolation.setName("playerSetUp_Interpolation");

			final DefaultComboBoxModel<String> theModel = new DefaultComboBoxModel<>(ModContainer.INTERPOLATION);
			playerSetUp_Interpolation.setModel(theModel);
			playerSetUp_Interpolation.setFont(Helpers.getDialogFont());
			playerSetUp_Interpolation.setEnabled(true);
			playerSetUp_Interpolation.addItemListener(new ItemListener()
			{
				@Override
				public void itemStateChanged(final ItemEvent e)
				{
					if (e.getStateChange()==ItemEvent.SELECTED)
					{
						final ModContainer parent = getParentContainer();
						if (parent!=null)
						{
							final ModMixer currentMixer = parent.getCurrentMixer();
							if (currentMixer!=null)
								currentMixer.setDoISP(getPlayerSetUp_Interpolation().getSelectedIndex());
						}
					}
				}
			});
		}
		return playerSetUp_Interpolation;
	}
	private JLabel getPlayerSetUp_L_MaxNNAChannels()
	{
		if (playerSetUp_L_MaxNNAChannels==null)
		{
			playerSetUp_L_MaxNNAChannels = new JLabel();
			playerSetUp_L_MaxNNAChannels.setName("playerSetUp_L_MaxNNAChannels");
			playerSetUp_L_MaxNNAChannels.setText("max NNA channels");
			playerSetUp_L_MaxNNAChannels.setFont(Helpers.getDialogFont());
		}
		return playerSetUp_L_MaxNNAChannels;
	}
	private JComboBox<String> getPlayerSetUp_MaxNNAChannels()
	{
		if (playerSetUp_MaxNNAChannels==null)
		{
			playerSetUp_MaxNNAChannels = new JComboBox<>();
			playerSetUp_MaxNNAChannels.setName("playerSetUp_MaxNNAChannels");

			final DefaultComboBoxModel<String> theModel = new DefaultComboBoxModel<>(ModContainer.MAX_NNA_CHANNELS);
			playerSetUp_MaxNNAChannels.setModel(theModel);
			playerSetUp_MaxNNAChannels.setFont(Helpers.getDialogFont());
			playerSetUp_MaxNNAChannels.setEnabled(true);
			playerSetUp_MaxNNAChannels.addItemListener(new ItemListener()
			{
				@Override
				public void itemStateChanged(final ItemEvent e)
				{
					if (e.getStateChange()==ItemEvent.SELECTED)
					{
						final ModContainer parent = getParentContainer();
						if (parent!=null)
						{
							final ModMixer currentMixer = parent.getCurrentMixer();
							if (currentMixer!=null)
								currentMixer.setMaxNNAChannels(Integer.parseInt(getPlayerSetUp_MaxNNAChannels().getSelectedItem().toString()));
						}
					}
				}
			});
		}
		return playerSetUp_MaxNNAChannels;
	}
	private JLabel getPlayerSetUp_L_DitherType()
	{
		if (playerSetUp_L_DitherType==null)
		{
			playerSetUp_L_DitherType = new JLabel();
			playerSetUp_L_DitherType.setName("playerSetUp_L_DitherType");
			playerSetUp_L_DitherType.setText("Noise Shaping Type");
			playerSetUp_L_DitherType.setFont(Helpers.getDialogFont());
		}
		return playerSetUp_L_DitherType;
	}
	private JComboBox<String> getPlayerSetUp_DitherType()
	{
		if (playerSetUp_DitherType==null)
		{
			playerSetUp_DitherType = new JComboBox<>();
			playerSetUp_DitherType.setName("playerSetUp_DitherType");

			final DefaultComboBoxModel<String> theModel = new DefaultComboBoxModel<>(Dither.DitherTypeNames);
			playerSetUp_DitherType.setModel(theModel);
			playerSetUp_DitherType.setFont(Helpers.getDialogFont());
			playerSetUp_DitherType.setEnabled(true);
			playerSetUp_DitherType.addItemListener(new ItemListener()
			{
				@Override
				public void itemStateChanged(final ItemEvent e)
				{
					if (e.getStateChange()==ItemEvent.SELECTED)
					{
						final ModContainer parent = getParentContainer();
						if (parent!=null)
						{
							final ModMixer currentMixer = parent.getCurrentMixer();
							if (currentMixer!=null)
								currentMixer.setDitherType(getPlayerSetUp_DitherFilterType().getSelectedIndex());
						}
					}
				}
			});
		}
		return playerSetUp_DitherType;
	}
	private JLabel getPlayerSetUp_L_DitherFilterType()
	{
		if (playerSetUp_L_DitherFilterType==null)
		{
			playerSetUp_L_DitherFilterType = new JLabel();
			playerSetUp_L_DitherFilterType.setName("playerSetUp_L_DitherFilterType");
			playerSetUp_L_DitherFilterType.setText("Dither Filter Type");
			playerSetUp_L_DitherFilterType.setFont(Helpers.getDialogFont());
		}
		return playerSetUp_L_DitherFilterType;
	}
	private JComboBox<String> getPlayerSetUp_DitherFilterType()
	{
		if (playerSetUp_DitherFilterType==null)
		{
			playerSetUp_DitherFilterType = new JComboBox<>();
			playerSetUp_DitherFilterType.setName("playerSetUp_DitherFilterType");

			final DefaultComboBoxModel<String> theModel = new DefaultComboBoxModel<>(Dither.FilterTypeNames);
			playerSetUp_DitherFilterType.setModel(theModel);
			playerSetUp_DitherFilterType.setFont(Helpers.getDialogFont());
			playerSetUp_DitherFilterType.setEnabled(true);
			playerSetUp_DitherFilterType.addItemListener(new ItemListener()
			{
				@Override
				public void itemStateChanged(final ItemEvent e)
				{
					if (e.getStateChange()==ItemEvent.SELECTED)
					{
						final ModContainer parent = getParentContainer();
						if (parent!=null)
						{
							final ModMixer currentMixer = parent.getCurrentMixer();
							if (currentMixer!=null)
								currentMixer.setDitherFilterType(getPlayerSetUp_DitherFilterType().getSelectedIndex());
						}
					}
				}
			});
		}
		return playerSetUp_DitherFilterType;
	}
	private JCheckBox getPlayerSetUp_ByPassDither()
	{
		if (playerSetUp_ByPassDither == null)
		{
			playerSetUp_ByPassDither = new JCheckBox();
			playerSetUp_ByPassDither.setName("playerSetUp_ByPassDither");
			playerSetUp_ByPassDither.setText("bypass Dither");
			playerSetUp_ByPassDither.setFont(Helpers.getDialogFont());
			playerSetUp_ByPassDither.addItemListener(new ItemListener()
			{
				@Override
				public void itemStateChanged(final ItemEvent e)
				{
					if (e.getStateChange()==ItemEvent.SELECTED || e.getStateChange()==ItemEvent.DESELECTED)
					{
						final ModContainer parent = getParentContainer();
						if (parent!=null)
						{
							final ModMixer currentMixer = parent.getCurrentMixer();
							if (currentMixer!=null)
								currentMixer.setDitherByPass(getPlayerSetUp_ByPassDither().isSelected());
						}
					}
				}
			});
		}
		return playerSetUp_ByPassDither;
	}
	public void configurationChanged(final Properties props)
	{
		getPlayerSetUp_SampleRate().setSelectedItem(props.getProperty(ModContainer.PROPERTY_PLAYER_FREQUENCY, ModContainer.DEFAULT_SAMPLERATE));
		getPlayerSetUp_BufferSize().setSelectedItem(props.getProperty(ModContainer.PROPERTY_PLAYER_MSBUFFERSIZE, ModContainer.DEFAULT_MSBUFFERSIZE));
		getPlayerSetUp_BitsPerSample().setSelectedItem(props.getProperty(ModContainer.PROPERTY_PLAYER_BITSPERSAMPLE, ModContainer.DEFAULT_BITSPERSAMPLE));
		getPlayerSetUp_Channels().setSelectedItem(props.getProperty(ModContainer.PROPERTY_PLAYER_STEREO, ModContainer.DEFAULT_CHANNEL));
		getPlayerSetUp_Interpolation().setSelectedIndex(Integer.parseInt(props.getProperty(ModContainer.PROPERTY_PLAYER_ISP, ModContainer.DEFAULT_INTERPOLATION_INDEX)));
		getPlayerSetUp_WideStereoMix().setSelected(Boolean.parseBoolean(props.getProperty(ModContainer.PROPERTY_PLAYER_WIDESTEREOMIX, ModContainer.DEFAULT_WIDESTEREOMIX)));
		getPlayerSetUp_NoiseReduction().setSelected(Boolean.parseBoolean(props.getProperty(ModContainer.PROPERTY_PLAYER_NOISEREDUCTION, ModContainer.DEFAULT_NOISEREDUCTION)));
		getPlayerSetUp_MegaBass().setSelected(Boolean.parseBoolean(props.getProperty(ModContainer.PROPERTY_PLAYER_MEGABASS, ModContainer.DEFAULT_MEGABASS)));
		getPlayerSetUp_DCRemoval().setSelected(Boolean.parseBoolean(props.getProperty(ModContainer.PROPERTY_PLAYER_DCREMOVAL, ModContainer.DEFAULT_DCREMOVAL)));
		setLoopValue(Integer.parseInt(props.getProperty(ModContainer.PROPERTY_PLAYER_NOLOOPS, ModContainer.DEFAULT_NOLOOPS)));
		getPlayerSetUp_MaxNNAChannels().setSelectedItem(props.getProperty(ModContainer.PROPERTY_PLAYER_MAXNNACHANNELS, ModContainer.DEFAULT_MAXNNACHANNELS));
		getPlayerSetUp_DitherFilterType().setSelectedIndex(Integer.parseInt(props.getProperty(ModContainer.PROPERTY_PLAYER_DITHERFILTER, ModContainer.DEFAULT_DITHERFILTER)));
		getPlayerSetUp_DitherType().setSelectedIndex(Integer.parseInt(props.getProperty(ModContainer.PROPERTY_PLAYER_DITHERTYPE, ModContainer.DEFAULT_DITHERTYPE)));
		getPlayerSetUp_ByPassDither().setSelected(Boolean.parseBoolean(props.getProperty(ModContainer.PROPERTY_PLAYER_DITHERBYPASS, ModContainer.DEFAULT_DITHERBYPASS)));
	}
	public void configurationSave(final Properties props)
	{
		props.setProperty(ModContainer.PROPERTY_PLAYER_FREQUENCY, getPlayerSetUp_SampleRate().getSelectedItem().toString());
		props.setProperty(ModContainer.PROPERTY_PLAYER_MSBUFFERSIZE, getPlayerSetUp_BufferSize().getSelectedItem().toString());
		props.setProperty(ModContainer.PROPERTY_PLAYER_BITSPERSAMPLE, getPlayerSetUp_BitsPerSample().getSelectedItem().toString());
		props.setProperty(ModContainer.PROPERTY_PLAYER_STEREO, getPlayerSetUp_Channels().getSelectedItem().toString());
		props.setProperty(ModContainer.PROPERTY_PLAYER_ISP, Integer.toString(getPlayerSetUp_Interpolation().getSelectedIndex()));
		props.setProperty(ModContainer.PROPERTY_PLAYER_WIDESTEREOMIX, Boolean.toString(getPlayerSetUp_WideStereoMix().isSelected()));
		props.setProperty(ModContainer.PROPERTY_PLAYER_NOISEREDUCTION, Boolean.toString(getPlayerSetUp_NoiseReduction().isSelected()));
		props.setProperty(ModContainer.PROPERTY_PLAYER_MEGABASS, Boolean.toString(getPlayerSetUp_MegaBass().isSelected()));
		props.setProperty(ModContainer.PROPERTY_PLAYER_DCREMOVAL, Boolean.toString(getPlayerSetUp_DCRemoval().isSelected()));
		props.setProperty(ModContainer.PROPERTY_PLAYER_NOLOOPS, Integer.toString(getLoopValue()));
		props.setProperty(ModContainer.PROPERTY_PLAYER_MAXNNACHANNELS, getPlayerSetUp_MaxNNAChannels().getSelectedItem().toString());
		props.setProperty(ModContainer.PROPERTY_PLAYER_DITHERFILTER, Integer.toString(getPlayerSetUp_DitherFilterType().getSelectedIndex()));
		props.setProperty(ModContainer.PROPERTY_PLAYER_DITHERTYPE, Integer.toString(getPlayerSetUp_DitherType().getSelectedIndex()));
		props.setProperty(ModContainer.PROPERTY_PLAYER_DITHERBYPASS, Boolean.toString(getPlayerSetUp_ByPassDither().isSelected()));
	}
}
