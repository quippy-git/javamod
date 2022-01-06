/*
 * @(#) SeekBarPanel.java
 *
 * Created on 09.09.2009 by Daniel Becker
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
package de.quippy.javamod.main.gui.components;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.BoundedRangeModel;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

import de.quippy.javamod.mixer.Mixer;
import de.quippy.javamod.system.Helpers;

/**
 * This panel displays a timeCode and a seekbar to allow seeking
 * @author Daniel Becker
 * @since 09.09.2009
 */
public class SeekBarPanel extends ThreadUpdatePanel
{
	private static final long serialVersionUID = -3570075762823459752L;

	private JTextField timeTextField = null;
	private JLabel timeLabel = null;
	private JTextField KBSField = null;
	private JLabel KBSLabel = null;
	private JTextField KHZField = null;
	private JLabel KHZLabel = null;
	private JTextField activeChannelsTextField = null;
	private JLabel activeChannelsLabel = null;
	private JProgressBar timeBar = null;
	
	private boolean showBarOnly;
	
	private long maxLengthInMillis = 0;
	private int displayWhat = 0;
	
	private Mixer currentMixer;
	
	private ArrayList<SeekBarPanelListener> listeners;
	
	/**
	 * Constructor for SeekBarPanel
	 * @param desiredFPS
	 */
	public SeekBarPanel(int desiredFPS, boolean showBarOnly)
	{
		super(desiredFPS);
		this.showBarOnly = showBarOnly;
		listeners = new ArrayList<SeekBarPanelListener>();
		initialize();
		startThread();
	}
	public synchronized void addListener(SeekBarPanelListener newListener)
	{
		if (!listeners.contains(newListener)) listeners.add(newListener);
	}
	public synchronized void removeListener(SeekBarPanelListener listener)
	{
		listeners.remove(listener);
	}
	public synchronized void fireValuesChanged(long milliseconds)
	{
		final int size = listeners.size();
		for (int i=0; i<size; i++)
		{
			listeners.get(i).valuesChanged(milliseconds);
		}
	}
	/**
	 * Will drop the graphical elements
	 * @since 09.09.2009
	 */
	private void initialize()
	{
		this.setLayout(new java.awt.GridBagLayout());
		if (!showBarOnly)
		{
			this.add(getTimeTextField(),			Helpers.getGridBagConstraint(0, 0, 1, 1, java.awt.GridBagConstraints.HORIZONTAL,java.awt.GridBagConstraints.WEST, 1.0, 0.0));
			this.add(getTimeLabel(),				Helpers.getGridBagConstraint(1, 0, 1, 1, java.awt.GridBagConstraints.NONE, 		java.awt.GridBagConstraints.EAST, 0.0, 0.0));
			this.add(getKBSField(),					Helpers.getGridBagConstraint(2, 0, 1, 1, java.awt.GridBagConstraints.HORIZONTAL,java.awt.GridBagConstraints.WEST, 1.0, 0.0));
			this.add(getKBSLabel(),					Helpers.getGridBagConstraint(3, 0, 1, 1, java.awt.GridBagConstraints.NONE, 		java.awt.GridBagConstraints.EAST, 0.0, 0.0));
			this.add(getKHZField(),					Helpers.getGridBagConstraint(4, 0, 1, 1, java.awt.GridBagConstraints.HORIZONTAL,java.awt.GridBagConstraints.WEST, 1.0, 0.0));
			this.add(getKHZLabel(),					Helpers.getGridBagConstraint(5, 0, 1, 1, java.awt.GridBagConstraints.NONE, 		java.awt.GridBagConstraints.EAST, 0.0, 0.0));
			this.add(getActiveChannelsTextField(),	Helpers.getGridBagConstraint(6, 0, 1, 1, java.awt.GridBagConstraints.HORIZONTAL,java.awt.GridBagConstraints.WEST, 1.0, 0.0));
			this.add(getActiveChannelsLabel(), 		Helpers.getGridBagConstraint(7, 0, 1, 0, java.awt.GridBagConstraints.NONE, 		java.awt.GridBagConstraints.EAST, 0.0, 0.0));
			this.add(getTimeBar(),					Helpers.getGridBagConstraint(0, 1, 1, 0, java.awt.GridBagConstraints.HORIZONTAL,java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		}
		else
		{
			this.add(getTimeTextField(),			Helpers.getGridBagConstraint(0, 0, 1, 1, java.awt.GridBagConstraints.NONE,		java.awt.GridBagConstraints.WEST, 0.0, 0.0));
			this.add(getTimeBar(),					Helpers.getGridBagConstraint(1, 0, 1, 0, java.awt.GridBagConstraints.HORIZONTAL,java.awt.GridBagConstraints.EAST, 1.0, 0.0));
		}
	}
	public JTextField getTimeTextField()
	{
		if (timeTextField==null)
		{
			timeTextField = new JTextField("0:00");
			timeTextField.setHorizontalAlignment(JTextField.TRAILING);
			timeTextField.setEditable(false);
			timeTextField.setName("timeTextField");
			timeTextField.setFont(Helpers.getDialogFont());
			timeTextField.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			
			// Preserve characters space - not less, not more!
			final FontMetrics metrics = timeTextField.getFontMetrics(Helpers.getDialogFont());
			final Dimension d = new Dimension(6 * metrics.charWidth('0'), metrics.getHeight());
			timeTextField.setSize(d);
			timeTextField.setMinimumSize(d);
			timeTextField.setMaximumSize(d);
			timeTextField.setPreferredSize(d);
			timeTextField.addMouseListener(new MouseAdapter()
			{
				public void mouseClicked(MouseEvent pEvent)
				{
					if (pEvent.getButton() == MouseEvent.BUTTON1)
					{
						if (currentMixer!=null)
						{
							displayWhat = 1 - displayWhat;
						}
					}
				}
			});
		}
		return timeTextField;
	}
	public javax.swing.JLabel getTimeLabel()
	{
		if (timeLabel==null)
		{
			timeLabel = new javax.swing.JLabel();
			timeLabel.setName("timeLabel");
			timeLabel.setText("time");
			timeLabel.setFont(Helpers.getDialogFont());
		}
		return timeLabel;
	}
	public javax.swing.JTextField getKBSField()
	{
		if (KBSField==null)
		{
			KBSField = new javax.swing.JTextField("--");
			KBSField.setHorizontalAlignment(JTextField.TRAILING);
			KBSField.setEditable(false);
			KBSField.setName("KBSField");
			KBSField.setFont(Helpers.getDialogFont());

			// Preserve characters space - not less, not more!
			final FontMetrics metrics = timeTextField.getFontMetrics(Helpers.getDialogFont());
			final Dimension d = new Dimension(8 * metrics.charWidth('0'), metrics.getHeight());
			KBSField.setSize(d);
			KBSField.setMinimumSize(d);
			KBSField.setMaximumSize(d);
			KBSField.setPreferredSize(d);
		}
		return KBSField;
	}
	public javax.swing.JLabel getKBSLabel()
	{
		if (KBSLabel==null)
		{
			KBSLabel = new javax.swing.JLabel();
			KBSLabel.setName("KBSLabel");
			KBSLabel.setText("KB/s");
			KBSLabel.setFont(Helpers.getDialogFont());
		}
		return KBSLabel;
	}
	public javax.swing.JTextField getKHZField()
	{
		if (KHZField==null)
		{
			KHZField = new javax.swing.JTextField("--");
			KHZField.setHorizontalAlignment(JTextField.TRAILING);
			KHZField.setEditable(false);
			KHZField.setName("KHzField");
			KHZField.setFont(Helpers.getDialogFont());

			// Preserve characters space - not less, not more!
			final FontMetrics metrics = timeTextField.getFontMetrics(Helpers.getDialogFont());
			final Dimension d = new Dimension(4 * metrics.charWidth('0'), metrics.getHeight());
			KHZField.setSize(d);
			KHZField.setMinimumSize(d);
			KHZField.setMaximumSize(d);
			KHZField.setPreferredSize(d);
		}
		return KHZField;
	}
	public javax.swing.JLabel getKHZLabel()
	{
		if (KHZLabel==null)
		{
			KHZLabel = new javax.swing.JLabel();
			KHZLabel.setName("KHzLabel");
			KHZLabel.setText("KHz");
			KHZLabel.setFont(Helpers.getDialogFont());
		}
		return KHZLabel;
	}
	public javax.swing.JTextField getActiveChannelsTextField()
	{
		if (activeChannelsTextField==null)
		{
			activeChannelsTextField = new javax.swing.JTextField("--");
			activeChannelsTextField.setHorizontalAlignment(JTextField.TRAILING);
			activeChannelsTextField.setEditable(false);
			activeChannelsTextField.setName("activeChannelsTextField");
			activeChannelsTextField.setFont(Helpers.getDialogFont());

			// Preserve characters space - not less, not more!
			final FontMetrics metrics = timeTextField.getFontMetrics(Helpers.getDialogFont());
			final Dimension d = new Dimension(4 * metrics.charWidth('0'), metrics.getHeight());
			activeChannelsTextField.setSize(d);
			activeChannelsTextField.setMinimumSize(d);
			activeChannelsTextField.setMaximumSize(d);
			activeChannelsTextField.setPreferredSize(d);
		}
		return activeChannelsTextField;
	}
	public javax.swing.JLabel getActiveChannelsLabel()
	{
		if (activeChannelsLabel==null)
		{
			activeChannelsLabel = new javax.swing.JLabel();
			activeChannelsLabel.setName("activeChannelsLabel");
			activeChannelsLabel.setText("Chn");
			activeChannelsLabel.setFont(Helpers.getDialogFont());
		}
		return activeChannelsLabel;
	}
	public JProgressBar getTimeBar()
	{
		if (timeBar==null)
		{
			timeBar = new JProgressBar(0, 0);
			timeBar.setValue(0);
			timeBar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			timeBar.addMouseListener(new MouseAdapter()
			{
				public void mouseClicked(MouseEvent pEvent)
				{
					if (pEvent.getButton() == MouseEvent.BUTTON1)
					{
						if (currentMixer!=null)
						{
							Point p = pEvent.getPoint();
							final double x = p.getX();
							final int width = timeBar.getWidth();
							final BoundedRangeModel model = getTimeBar().getModel();
							currentMixer.setMillisecondPosition((long)(model.getMaximum() * x) / width);
						}
					}
				}
			});
		}
		return timeBar;
	}
	public synchronized void setCurrentMixer(Mixer newMixer)
	{
		currentMixer = newMixer;
		getTimeBar().setValue(0);
		getTimeTextField().setText("0:00");
		getKBSField().setText("--");
		getKHZField().setText("--");
		getActiveChannelsTextField().setText("--");
		if (currentMixer!=null)
		{
			BoundedRangeModel model = getTimeBar().getModel();
			model.setMaximum((int)(maxLengthInMillis = currentMixer.getLengthInMilliseconds()));
		}
	}
	/**
	 * 
	 * @see de.quippy.javamod.main.gui.components.MeterPanelBase#doThreadUpdate()
	 */
	@Override
	protected synchronized void doThreadUpdate()
	{
		if (currentMixer!=null)
		{
			final long timeCode = currentMixer.getMillisecondPosition();
			getTimeBar().setValue((int)timeCode);
			
			if (!showBarOnly)
			{
				getTimeTextField().setText(Helpers.getTimeStringFromMilliseconds((displayWhat == 1) ? maxLengthInMillis - timeCode : timeCode));
				getKBSField().setText(Integer.toString(currentMixer.getCurrentKBperSecond()));
				getKHZField().setText(Integer.toString(currentMixer.getCurrentSampleRate() / 1000));
				getActiveChannelsTextField().setText(Integer.toString(currentMixer.getChannelCount()));
			}

			fireValuesChanged(timeCode);
		}
	}
}
