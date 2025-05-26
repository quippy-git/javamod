/*
 * @(#) XmasScreenConfigPanel.java
 *
 * Created on 07.12.2023 by Daniel Becker
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
package de.quippy.javamod.main.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.quippy.javamod.main.gui.components.XmasDecorationPanel;
import de.quippy.javamod.system.Helpers;

/**
 * @author Daniel Becker
 * @since 07.12.2023
 */
public class XmasScreenConfigPanel extends JPanel
{
	private static final long serialVersionUID = -8071971305563557958L;

	private static final String PROPERTY_XMAS_ENABLED = "javamod.xmas.enabled.";
	private static final String PROPERTY_XMAS_WITHSPACE = "javamod.xmas.withspace.";
	private static final String PROPERTY_XMAS_FLICKERTYPE = "javamod.xmas.flickertype.";
	private static final String PROPERTY_XMAS_UPDATEFPS = "javamod.xmas.updatefps.";

	private JCheckBox xmasEnabledCheckBox = null;
	private JCheckBox withSpaceCheckBox = null;
	private JLabel flickerTypeLabel = null;
	private JLabel updateFPSLabel = null;
	private JComboBox<String> flickerTypeSelector = null;
	private JSlider updateFPSSelector = null;

	private JWindow transparentJFrame = null;
	private XmasDecorationPanel xmasDecorationPanel = null;

	private final int screenFPS;
	private final ImageIcon[] bulbs;
	private final GraphicsDevice screen;
	private final int defaultScreenHeight;

	/**
	 * Constructor for XmasScreenConfigPanel
	 */
	public XmasScreenConfigPanel(final int myDesiredFPS, final ImageIcon[] allBulbs, final GraphicsDevice forScreen)
	{
		super();
		screenFPS = myDesiredFPS;
		bulbs = allBulbs;
		screen=forScreen;
		defaultScreenHeight = (bulbs!=null && bulbs[0]!=null)?bulbs[0].getIconHeight():32;
		initialize();
	}
    private void initialize()
    {
    	setName("Xmas Screen Config for Screen");
		setLayout(new java.awt.GridBagLayout());
		add(getXmasEnabledCheckBox(),	Helpers.getGridBagConstraint(0, 0, 1, 3, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		add(getWithSpaceCheckBox(),		Helpers.getGridBagConstraint(0, 1, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		add(getFlickerTypeLabel(),		Helpers.getGridBagConstraint(1, 1, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		add(getFlickerTypeSelector(),	Helpers.getGridBagConstraint(2, 1, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		add(getUpdateFPSLabel(),		Helpers.getGridBagConstraint(0, 2, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		add(getUpdateFPSSelector(),		Helpers.getGridBagConstraint(1, 2, 1, 2, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
    }
	private JCheckBox getXmasEnabledCheckBox()
	{
		if (xmasEnabledCheckBox == null)
		{
			xmasEnabledCheckBox = new javax.swing.JCheckBox();
			xmasEnabledCheckBox.setName("xmasEnabledCheckBox");
			xmasEnabledCheckBox.setText("Enable X-Mas decoration on this screen");
			xmasEnabledCheckBox.setFont(Helpers.getDialogFont());
			xmasEnabledCheckBox.setSelected(isXmasEnabled());
			xmasEnabledCheckBox.addItemListener(new ItemListener()
			{
				@Override
				public void itemStateChanged(final ItemEvent e)
				{
					if (e.getStateChange()==ItemEvent.SELECTED || e.getStateChange()==ItemEvent.DESELECTED)
					{
						final boolean isEnabled = getXmasEnabledCheckBox().isSelected();
						getTransparentJWindow().setVisible(isEnabled);
						if (isEnabled) startThread(); // else: do not stop the thread - that cannot be undone
					}
				}
			});
		}
		return xmasEnabledCheckBox;
	}
	private JCheckBox getWithSpaceCheckBox()
	{
		if (withSpaceCheckBox == null)
		{
			withSpaceCheckBox = new javax.swing.JCheckBox();
			withSpaceCheckBox.setName("withSpaceCheckBox");
			withSpaceCheckBox.setText("with Space");
			withSpaceCheckBox.setFont(Helpers.getDialogFont());
			withSpaceCheckBox.setSelected(isWithSpaceEnabled());
			withSpaceCheckBox.addItemListener(new ItemListener()
			{
				@Override
				public void itemStateChanged(final ItemEvent e)
				{
					if (e.getStateChange()==ItemEvent.SELECTED || e.getStateChange()==ItemEvent.DESELECTED)
					{
						getXmasDecorationPanel().setWithSpace(getWithSpaceCheckBox().isSelected());
					}
				}
			});
		}
		return withSpaceCheckBox;
	}
	private JLabel getFlickerTypeLabel()
	{
		if (flickerTypeLabel == null)
		{
			flickerTypeLabel = new JLabel("Effect type:");
			flickerTypeLabel.setName("flickerTypeLabel");
			flickerTypeLabel.setFont(Helpers.getDialogFont());
		}
		return flickerTypeLabel;
	}
	private JComboBox<String> getFlickerTypeSelector()
	{
		if (flickerTypeSelector==null)
		{
			flickerTypeSelector = new JComboBox<>();
			flickerTypeSelector.setName("flickerTypeSelector");

			final DefaultComboBoxModel<String> theModel = new DefaultComboBoxModel<>(XmasDecorationPanel.FLICKER_TYPES);
			flickerTypeSelector.setModel(theModel);
			flickerTypeSelector.setFont(Helpers.getDialogFont());
			flickerTypeSelector.setEnabled(true);
			flickerTypeSelector.addItemListener(new ItemListener()
			{
				@Override
				public void itemStateChanged(final ItemEvent e)
				{
					if (e.getStateChange()==ItemEvent.SELECTED)
					{
						getXmasDecorationPanel().setFlickerType(getFlickerType());
					}
				}
			});
		}
		return flickerTypeSelector;
	}
	private JLabel getUpdateFPSLabel()
	{
		if (updateFPSLabel == null)
		{
			updateFPSLabel = new JLabel("Updates per second:");
			updateFPSLabel.setName("updateFPSLabel");
			updateFPSLabel.setFont(Helpers.getDialogFont());
		}
		return updateFPSLabel;
	}
	private JSlider getUpdateFPSSelector()
	{
		if (updateFPSSelector==null)
		{
			updateFPSSelector = new JSlider(SwingConstants.HORIZONTAL, 0, screenFPS, 1);
			updateFPSSelector.setFont(Helpers.getDialogFont());
			updateFPSSelector.setMinorTickSpacing(1);
			updateFPSSelector.setMajorTickSpacing(10);
			updateFPSSelector.setPaintTicks(true);
			updateFPSSelector.setSnapToTicks(true);
			updateFPSSelector.setPaintLabels(false);
			updateFPSSelector.setPaintTrack(true);
			updateFPSSelector.setToolTipText(Integer.toString(updateFPSSelector.getValue()));
			updateFPSSelector.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mouseClicked(final MouseEvent e)
				{
					if (e.getClickCount()>1)
					{
						((JSlider)e.getSource()).setValue(2);
						e.consume();
					}
				}
			});
			updateFPSSelector.addChangeListener(new ChangeListener()
			{
				@Override
				public void stateChanged(final ChangeEvent e)
				{
					getUpdateFPSSelector().setToolTipText(Integer.toString(getUpdateFPSSelector().getValue()));
					getXmasDecorationPanel().setUpdateFPS(getUpdateFPSSelector().getValue());
				}
			});
		}
		return updateFPSSelector;
	}
	private JWindow getTransparentJWindow()
	{
		// Examples use always a java.awt.Window for this. That basically does work.
		// But whatever the difference of a JWindow to a Window is, a Window
		// does not work with the OpenGL render pipeline.
		// On Linux / KDE the OpenGL pipeline reduces flicker, however the
		// Window inherits the alpha value of the underlying window...
		if (transparentJFrame == null)
		{
			transparentJFrame = new JWindow();
			transparentJFrame.setAlwaysOnTop(true);

			final GraphicsConfiguration gc = screen.getDefaultConfiguration();
			final Rectangle bounds = gc.getBounds();
			bounds.height = defaultScreenHeight;
			transparentJFrame.setBounds(bounds);

			transparentJFrame.setBackground(new Color(0, true));
			transparentJFrame.setContentPane(getXmasDecorationPanel());
			transparentJFrame.setFocusable(false);
			transparentJFrame.setFocusableWindowState(false);
		}
		return transparentJFrame;
	}
	private XmasDecorationPanel getXmasDecorationPanel()
	{
		if (xmasDecorationPanel==null)
		{
			xmasDecorationPanel = new XmasDecorationPanel(screenFPS, bulbs);
			xmasDecorationPanel.setBorder(BorderFactory.createEmptyBorder());
			xmasDecorationPanel.setOpaque(false);
			xmasDecorationPanel.setBackground(new Color(0, true));
			final Dimension d = getTransparentJWindow().getSize();
			xmasDecorationPanel.setSize(d);
		}
		return xmasDecorationPanel;
	}
	private boolean isXmasEnabled()
	{
		return getXmasEnabledCheckBox().isSelected();
	}
	private void setXmasEnabled(final boolean xmasEnabled)
	{
		getXmasEnabledCheckBox().setSelected(xmasEnabled);
	}
	private boolean isWithSpaceEnabled()
	{
		return getWithSpaceCheckBox().isSelected();
	}
	private void setWithSpaceEnabled(final boolean spaceEnabled)
	{
		getWithSpaceCheckBox().setSelected(spaceEnabled);
	}
	private void setFlickerType(final int newFlickerType)
	{
		if (newFlickerType>=0 && newFlickerType<XmasDecorationPanel.FLICKER_TYPES.length)
			getFlickerTypeSelector().setSelectedIndex(newFlickerType);
	}
	private int getFlickerType()
	{
		return getFlickerTypeSelector().getSelectedIndex();
	}
	private void setUpdateFPS(final int newUpdateFPS)
	{
		if (newUpdateFPS>=getUpdateFPSSelector().getMinimum() && newUpdateFPS<=getUpdateFPSSelector().getMaximum())
			getUpdateFPSSelector().setValue(newUpdateFPS);
	}
	private int getUpdateFPS()
	{
		return getUpdateFPSSelector().getValue();
	}

//---------------------- public interface --------------------------------------

	public void readProperties(final Properties props, final int forScreen)
	{
		final String index = Integer.toString(forScreen);
		setFlickerType(Integer.parseInt(props.getProperty(PROPERTY_XMAS_FLICKERTYPE+index, "4")));
		setUpdateFPS(Integer.parseInt(props.getProperty(PROPERTY_XMAS_UPDATEFPS+index, "2")));
		setWithSpaceEnabled(Boolean.parseBoolean(props.getProperty(PROPERTY_XMAS_WITHSPACE+index, "FALSE")));
		setXmasEnabled(Boolean.parseBoolean(props.getProperty(PROPERTY_XMAS_ENABLED+index, "FALSE")));
	}
	public void writeProperties(final Properties props, final int forScreen)
	{
		final String index = Integer.toString(forScreen);
		props.setProperty(PROPERTY_XMAS_ENABLED+index, Boolean.toString(isXmasEnabled()));
		props.setProperty(PROPERTY_XMAS_WITHSPACE+index, Boolean.toString(isWithSpaceEnabled()));
		props.setProperty(PROPERTY_XMAS_FLICKERTYPE+index, Integer.toString(getFlickerType()));
		props.setProperty(PROPERTY_XMAS_UPDATEFPS+index, Integer.toString(getUpdateFPS()));
	}
	public void startThread()
	{
		getXmasDecorationPanel().startThread();
	}
	public void stopThread()
	{
		getXmasDecorationPanel().stopThread();
	}
}
