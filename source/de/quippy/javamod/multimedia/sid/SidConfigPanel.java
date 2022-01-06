/*
 * @(#) SidConfigPanel.java
 *
 * Created on 10.12.2011 by Daniel Becker
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
package de.quippy.javamod.multimedia.sid;

import java.awt.LayoutManager;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import de.quippy.javamod.system.Helpers;

/**
 * @author Daniel Becker
 * @since 10.12.2011
 */
public class SidConfigPanel extends JPanel
{
	private static final long serialVersionUID = -3720765201747394016L;

	private SIDContainer parentContainer;

	private JLabel playerSetUp_L_SampleRate = null;
	private JComboBox<String> playerSetUp_SampleRate = null;
	private JLabel playerSetUp_L_SIDModel = null;
	private JComboBox<String> playerSetUp_SIDModel = null;
	private JLabel playerSetUp_L_Optimization = null;
	private JPanel playerSetUpOptimizationPanel = null;
	private JRadioButton playerSetUp_Optimization_Level1 = null;
	private JRadioButton playerSetUp_Optimization_Level2 = null;
	private ButtonGroup playerSetUp_Optimization_Group = null;
	private JCheckBox playerSetUp_UseFilter = null;
	private JCheckBox playerSetUp_VirtualStereo = null;

	/**
	 * Constructor for SidConfigPanel
	 */
	public SidConfigPanel()
	{
		super();
		initialize();
	}
	/**
	 * Constructor for SidConfigPanel
	 * @param layout
	 */
	public SidConfigPanel(LayoutManager layout)
	{
		super(layout);
		initialize();
	}
	/**
	 * Constructor for SidConfigPanel
	 * @param isDoubleBuffered
	 */
	public SidConfigPanel(boolean isDoubleBuffered)
	{
		super(isDoubleBuffered);
		initialize();
	}
	/**
	 * Constructor for SidConfigPanel
	 * @param layout
	 * @param isDoubleBuffered
	 */
	public SidConfigPanel(LayoutManager layout, boolean isDoubleBuffered)
	{
		super(layout, isDoubleBuffered);
		initialize();
	}
	/**
	 * @return the parent
	 */
	public SIDContainer getParentContainer()
	{
		return parentContainer;
	}
	/**
	 * @param parent the parent to set
	 */
	public void setParentContainer(SIDContainer parent)
	{
		this.parentContainer = parent;
	}
	private void initialize()
	{
		this.setName("SidConfigPane");
		this.setLayout(new java.awt.GridBagLayout());
		this.add(getPlayerSetUp_L_SampleRate(), 		Helpers.getGridBagConstraint(0, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getPlayerSetUp_L_SIDModel(), 			Helpers.getGridBagConstraint(1, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getPlayerSetUp_L_Optimization(),		Helpers.getGridBagConstraint(2, 0, 1, 0, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getPlayerSetUp_SampleRate(),			Helpers.getGridBagConstraint(0, 1, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		this.add(getPlayerSetUp_SIDModel(),				Helpers.getGridBagConstraint(1, 1, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		this.add(getPlayerSetUp_Optimization_Panel(),	Helpers.getGridBagConstraint(2, 1, 1, 0, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		this.add(getPlayerSetUp_UseSIDFilter(),			Helpers.getGridBagConstraint(0, 2, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getPlayerSetUp_VirtualStereo(),		Helpers.getGridBagConstraint(1, 2, 1, 0, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
	}
	public javax.swing.JLabel getPlayerSetUp_L_SampleRate()
	{
		if (playerSetUp_L_SampleRate==null)
		{
			playerSetUp_L_SampleRate = new javax.swing.JLabel();
			playerSetUp_L_SampleRate.setName("playerSetUp_L_SampleRate");
			playerSetUp_L_SampleRate.setText("Frequency");
			playerSetUp_L_SampleRate.setFont(Helpers.getDialogFont());
		}
		return playerSetUp_L_SampleRate;
	}
	public javax.swing.JComboBox<String> getPlayerSetUp_SampleRate()
	{
		if (playerSetUp_SampleRate==null)
		{
			playerSetUp_SampleRate = new JComboBox<String>();
			playerSetUp_SampleRate.setName("playerSetUp_SampleRate");
			
			javax.swing.DefaultComboBoxModel<String> theModel = new javax.swing.DefaultComboBoxModel<String>(SIDContainer.SAMPLERATE);
			playerSetUp_SampleRate.setModel(theModel);
			playerSetUp_SampleRate.setFont(Helpers.getDialogFont());
			playerSetUp_SampleRate.setEnabled(true);
			playerSetUp_SampleRate.addItemListener(new ItemListener()
			{
				public void itemStateChanged(ItemEvent e)
				{
					if (e.getStateChange()==ItemEvent.SELECTED)
					{
						SIDContainer parent = getParentContainer();
						if (parent!=null)
						{
							SIDMixer currentMixer = parent.getCurrentMixer();
							if (currentMixer!=null)
								currentMixer.setSampleRate(Integer.parseInt(getPlayerSetUp_SampleRate().getSelectedItem().toString()));
						}
					}
				}
			});
		}
		return playerSetUp_SampleRate;
	}
	public javax.swing.JLabel getPlayerSetUp_L_SIDModel()
	{
		if (playerSetUp_L_SIDModel==null)
		{
			playerSetUp_L_SIDModel = new javax.swing.JLabel();
			playerSetUp_L_SIDModel.setName("playerSetUp_L_SIDModel");
			playerSetUp_L_SIDModel.setText("SID Model");
			playerSetUp_L_SIDModel.setFont(Helpers.getDialogFont());
		}
		return playerSetUp_L_SIDModel;
	}
	public javax.swing.JComboBox<String> getPlayerSetUp_SIDModel()
	{
		if (playerSetUp_SIDModel==null)
		{
			playerSetUp_SIDModel = new JComboBox<String>();
			playerSetUp_SIDModel.setName("playerSetUp_SIDModel");
			
			javax.swing.DefaultComboBoxModel<String> theModel = new javax.swing.DefaultComboBoxModel<String>(SIDContainer.SIDMODELS);
			playerSetUp_SIDModel.setModel(theModel);
			playerSetUp_SIDModel.setFont(Helpers.getDialogFont());
			playerSetUp_SIDModel.setEnabled(true);
			playerSetUp_SIDModel.addItemListener(new ItemListener()
			{
				public void itemStateChanged(ItemEvent e)
				{
					if (e.getStateChange()==ItemEvent.SELECTED)
					{
						SIDContainer parent = getParentContainer();
						if (parent!=null)
						{
							SIDMixer currentMixer = parent.getCurrentMixer();
							if (currentMixer!=null)
								currentMixer.setSIDModel(getPlayerSetUp_SIDModel().getSelectedIndex());
						}
					}
				}
			});
		}
		return playerSetUp_SIDModel;
	}
	public javax.swing.JLabel getPlayerSetUp_L_Optimization()
	{
		if (playerSetUp_L_Optimization==null)
		{
			playerSetUp_L_Optimization = new javax.swing.JLabel();
			playerSetUp_L_Optimization.setName("playerSetUp_L_Optimization");
			playerSetUp_L_Optimization.setText("Optimization");
			playerSetUp_L_Optimization.setFont(Helpers.getDialogFont());
		}
		return playerSetUp_L_Optimization;
	}
	public ButtonGroup getPlayerSetUp_Optimization()
	{
		if (playerSetUp_Optimization_Group==null)
		{
			playerSetUp_Optimization_Group = new ButtonGroup();
			playerSetUp_Optimization_Group.add(getPlayerSetUp_Optimization_Level1());
			playerSetUp_Optimization_Group.add(getPlayerSetUp_Optimization_Level2());
		}
		return playerSetUp_Optimization_Group;
	}
	public JPanel getPlayerSetUp_Optimization_Panel()
	{
		if (playerSetUpOptimizationPanel==null)
		{
			playerSetUpOptimizationPanel = new javax.swing.JPanel();
			playerSetUpOptimizationPanel.setName("playerSetUpOptimizationPanel");
			playerSetUpOptimizationPanel.setLayout(new java.awt.BorderLayout());
			playerSetUpOptimizationPanel.add(getPlayerSetUp_Optimization_Level1(), java.awt.BorderLayout.NORTH);
			playerSetUpOptimizationPanel.add(getPlayerSetUp_Optimization_Level2(), java.awt.BorderLayout.SOUTH);
			getPlayerSetUp_Optimization();
		}
		return playerSetUpOptimizationPanel;
	}
	public JRadioButton getPlayerSetUp_Optimization_Level1()
	{
		if (playerSetUp_Optimization_Level1==null)
		{
			playerSetUp_Optimization_Level1 = new JRadioButton("Level 1");
			playerSetUp_Optimization_Level1.setFont(Helpers.getDialogFont());
			playerSetUp_Optimization_Level1.addItemListener(new ItemListener()
			{
				public void itemStateChanged(ItemEvent e)
				{
					if (e.getStateChange()==ItemEvent.SELECTED)
					{
						if (getPlayerSetUp_Optimization_Level1().isSelected())
						{
							SIDContainer parent = getParentContainer();
							if (parent!=null)
							{
								SIDMixer currentMixer = parent.getCurrentMixer();
								if (currentMixer!=null)
									currentMixer.setOptimization(1);
							}
						}
					}
				}
			});
		}
		return playerSetUp_Optimization_Level1;
	}
	public JRadioButton getPlayerSetUp_Optimization_Level2()
	{
		if (playerSetUp_Optimization_Level2==null)
		{
			playerSetUp_Optimization_Level2 = new JRadioButton("Level 2");
			playerSetUp_Optimization_Level2.setFont(Helpers.getDialogFont());
			playerSetUp_Optimization_Level2.addItemListener(new ItemListener()
			{
				public void itemStateChanged(ItemEvent e)
				{
					if (e.getStateChange()==ItemEvent.SELECTED)
					{
						if (getPlayerSetUp_Optimization_Level2().isSelected())
						{
							SIDContainer parent = getParentContainer();
							if (parent!=null)
							{
								SIDMixer currentMixer = parent.getCurrentMixer();
								if (currentMixer!=null)
									currentMixer.setOptimization(2);
							}
						}
					}
				}
			});
		}
		return playerSetUp_Optimization_Level2;
	}
	public JCheckBox getPlayerSetUp_UseSIDFilter()
	{
		if (playerSetUp_UseFilter == null)
		{
			playerSetUp_UseFilter = new javax.swing.JCheckBox();
			playerSetUp_UseFilter.setName("playerSetUp_UseFilter");
			playerSetUp_UseFilter.setText("use SID Filter");
			playerSetUp_UseFilter.setFont(Helpers.getDialogFont());
			playerSetUp_UseFilter.addItemListener(new ItemListener()
			{
				public void itemStateChanged(ItemEvent e)
				{
					if (e.getStateChange()==ItemEvent.SELECTED || e.getStateChange()==ItemEvent.DESELECTED)
					{
						SIDContainer parent = getParentContainer();
						if (parent!=null)
						{
							SIDMixer currentMixer = parent.getCurrentMixer();
							if (currentMixer!=null)
								currentMixer.setUseSIDFilter(getPlayerSetUp_UseSIDFilter().isSelected());
						}
					}
				}
			});
		}
		return playerSetUp_UseFilter;
	}
	public JCheckBox getPlayerSetUp_VirtualStereo()
	{
		if (playerSetUp_VirtualStereo == null)
		{
			playerSetUp_VirtualStereo = new javax.swing.JCheckBox();
			playerSetUp_VirtualStereo.setName("playerSetUp_VirtualStereo");
			playerSetUp_VirtualStereo.setText("virtual Stereo");
			playerSetUp_VirtualStereo.setFont(Helpers.getDialogFont());
			playerSetUp_VirtualStereo.addItemListener(new ItemListener()
			{
				public void itemStateChanged(ItemEvent e)
				{
					if (e.getStateChange()==ItemEvent.SELECTED || e.getStateChange()==ItemEvent.DESELECTED)
					{
						SIDContainer parent = getParentContainer();
						if (parent!=null)
						{
							SIDMixer currentMixer = parent.getCurrentMixer();
							if (currentMixer!=null)
								currentMixer.setVirtualStereo(getPlayerSetUp_VirtualStereo().isSelected());
						}
					}
				}
			});
		}
		return playerSetUp_VirtualStereo;
	}
}
