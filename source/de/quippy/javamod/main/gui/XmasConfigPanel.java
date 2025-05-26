/*
 * @(#) XmasConfigPanel.java
 *
 * Created on 05.12.2023 by Daniel Becker
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

import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import de.quippy.javamod.system.Helpers;

/**
 * @author Daniel Becker
 * @since 05.12.2023
 */
public class XmasConfigPanel extends JPanel
{
	private static final long serialVersionUID = 4069039567731397482L;

	private static final String BULBS_PATH = "/de/quippy/javamod/main/gui/ressources/lightbulbs/";
	private static final int ANZ_BULBS = 15;

	private JTabbedPane screenSelectionPanel = null;
	private XmasScreenConfigPanel [] xmasScreenConfigPanels = null;

	private final int screenFPS;
	private ImageIcon[] bulbs;
	private final GraphicsDevice[] screens;

	/**
	 * Constructor for XmasConfigPanel
	 */
	public XmasConfigPanel(final int myDesiredFPS)
	{
		super();
		loadBulbs();
		screenFPS = myDesiredFPS;
		screens = getScreens();
		initialize();
	}
	private static GraphicsDevice[] getScreens()
	{
		final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		return ge.getScreenDevices();
	}
	private void loadBulbs()
	{
		bulbs = new ImageIcon[ANZ_BULBS];
		for (int i=0; i<ANZ_BULBS; i++)
			bulbs[i] = new ImageIcon(getClass().getResource(BULBS_PATH+Integer.toString(i)+".gif"));
	}

	private void initialize()
    {
    	setName("Xmas config");
		setLayout(new java.awt.GridBagLayout());
		add(getScreenSelectionPanel(),	Helpers.getGridBagConstraint(0, 0, 1, 1, java.awt.GridBagConstraints.BOTH, java.awt.GridBagConstraints.CENTER, 1.0, 1.0));
    }
	private JTabbedPane getScreenSelectionPanel()
	{
		if (screenSelectionPanel==null)
		{
			screenSelectionPanel = new JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
			screenSelectionPanel.setFont(Helpers.getDialogFont());
			xmasScreenConfigPanels = new XmasScreenConfigPanel[screens.length];
			for (int i=0; i<screens.length; i++)
			{
				final GraphicsDevice screen = screens[i];
				final StringBuilder sb = new StringBuilder(screen.getIDstring());
				final DisplayMode dm = screen.getDisplayMode();
				if (dm!=null) sb.append(" (").append(dm.getWidth()).append('x').append(dm.getHeight()).append(')');

				final JPanel xmasScreenConfigPanel = xmasScreenConfigPanels[i] = new XmasScreenConfigPanel(screenFPS, bulbs, screen);
				final JScrollPane containerScroller = new JScrollPane();
				containerScroller.setName("scrollPane_Config_" + sb.toString());
				containerScroller.setViewportView(xmasScreenConfigPanel);
				screenSelectionPanel.add(sb.toString(), containerScroller);
			}
		}
		return screenSelectionPanel;
	}

	public void readProperties(final Properties props)
	{
		for (int i=0; i<xmasScreenConfigPanels.length; i++)
			xmasScreenConfigPanels[i].readProperties(props, i);
	}
	public void writeProperties(final Properties props)
	{
		for (int i=0; i<xmasScreenConfigPanels.length; i++)
			xmasScreenConfigPanels[i].writeProperties(props, i);
	}
	public void stopThreads()
	{
		for (final XmasScreenConfigPanel panel : xmasScreenConfigPanels) panel.stopThread();
	}
}
