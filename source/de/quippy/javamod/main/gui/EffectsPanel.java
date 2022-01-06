/*
 * @(#) EffectsPanel.java
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
package de.quippy.javamod.main.gui;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import de.quippy.javamod.mixer.dsp.AudioProcessor;
import de.quippy.javamod.system.Helpers;

/**
 * @author Daniel Becker
 * @since 15.01.2012
 */
public class EffectsPanel extends JPanel
{
	private static final long serialVersionUID = -3590575857860754245L;

	private JCheckBox passThrough = null;
	private JCheckBox useGaplessAudio = null;
    private JTabbedPane tabbedPane = null;
    private JPanel[] effectPanels;

    private AudioProcessor audioProcessor;
    private MainForm parent;
    
	/**
	 * Constructor for EffectsPanel
	 */
	public EffectsPanel(MainForm parent, JPanel[] effectPanels, AudioProcessor audioProcessor)
	{
		this.effectPanels = effectPanels;
		this.audioProcessor = audioProcessor;
		this.parent = parent;
		initialize();
	}
	/**
	 * @param audioProcessor the audioProcessor to set
	 */
	public void setAudioProcessor(AudioProcessor newAudioProcessor)
	{
		audioProcessor = newAudioProcessor;
	}
	private void initialize()
	{
		setName("effectsTabbedPane");
		setLayout(new java.awt.GridBagLayout());
		add(getPassThrough(), 	  Helpers.getGridBagConstraint(0, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		add(getUseGaplessAudio(), Helpers.getGridBagConstraint(1, 0, 1, 0, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		add(getTabbedPane(), 	  Helpers.getGridBagConstraint(0, 1, 1, 0, java.awt.GridBagConstraints.BOTH, java.awt.GridBagConstraints.CENTER, 1.0, 1.0));
	}
	public JCheckBox getPassThrough()
	{
		if (passThrough == null)
		{
			passThrough = new javax.swing.JCheckBox();
			passThrough.setName("passThrough");
			passThrough.setText("activate effects");
			passThrough.setFont(Helpers.getDialogFont());
			if (audioProcessor!=null) passThrough.setSelected(audioProcessor.isDspEnabled());
			passThrough.addItemListener(new ItemListener()
			{
				public void itemStateChanged(ItemEvent e)
				{
					if (e.getStateChange()==ItemEvent.SELECTED || e.getStateChange()==ItemEvent.DESELECTED)
					{
						if (audioProcessor!=null)
							audioProcessor.setDspEnabled(getPassThrough().isSelected());
					}
				}
			});
		}
		return passThrough;
	}
	public JCheckBox getUseGaplessAudio()
	{
		if (useGaplessAudio == null)
		{
			useGaplessAudio = new javax.swing.JCheckBox();
			useGaplessAudio.setName("useGaplessAudio");
			useGaplessAudio.setText("use gapless audio stream");
			useGaplessAudio.setFont(Helpers.getDialogFont());
			if (parent!=null) useGaplessAudio.setSelected(parent.useGaplessAudio());
			useGaplessAudio.addItemListener(new ItemListener()
			{
				public void itemStateChanged(ItemEvent e)
				{
					if (e.getStateChange()==ItemEvent.SELECTED || e.getStateChange()==ItemEvent.DESELECTED)
					{
						if (parent!=null)
							parent.setUseGaplessAudio(getUseGaplessAudio().isSelected());
					}
				}
			});
		}
		return useGaplessAudio;
	}
	public JTabbedPane getTabbedPane()
	{
		if (tabbedPane==null)
		{
			tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
			tabbedPane.setFont(Helpers.getDialogFont());
			for (int i=0; i<effectPanels.length; i++)
			{
				JPanel effectPanel = effectPanels[i];
				if (effectPanel != null)
				{
					JScrollPane containerScroller = new JScrollPane();
					containerScroller.setName("scrollPane_Effect_" + effectPanel.getName());
					containerScroller.setViewportView(effectPanel);
					tabbedPane.add(effectPanel.getName(), containerScroller);
				}
			}
		}
		return tabbedPane;
	}
}
