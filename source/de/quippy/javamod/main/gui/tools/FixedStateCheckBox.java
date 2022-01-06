/*
 * @(#) FixedStateCheckBox.java
 *
 * Created on 27.07.2020 by Daniel Becker
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
package de.quippy.javamod.main.gui.tools;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JCheckBox;

/**
 * @author Daniel Becker
 * @since 27.07.2020
 */
public class FixedStateCheckBox extends JCheckBox implements ItemListener
{
	private static final long serialVersionUID = -8129487219688669718L;
	
	private boolean fixedState;
	/**
	 * Constructor for FixedStateCheckBox
	 */
	public FixedStateCheckBox()
	{
		super();
		addItemListener(this);
		fixedState = isSelected();
	}
	/**
	 * Constructor for FixedStateCheckBox
	 * @param icon
	 */
	public FixedStateCheckBox(Icon icon)
	{
		super(icon);
		addItemListener(this);
		fixedState = isSelected();
	}
	/**
	 * Constructor for FixedStateCheckBox
	 * @param text
	 */
	public FixedStateCheckBox(String text)
	{
		super(text);
		addItemListener(this);
		fixedState = isSelected();
	}
	/**
	 * Constructor for FixedStateCheckBox
	 * @param a
	 */
	public FixedStateCheckBox(Action a)
	{
		super(a);
		addItemListener(this);
		fixedState = isSelected();
	}
	/**
	 * Constructor for FixedStateCheckBox
	 * @param icon
	 * @param selected
	 */
	public FixedStateCheckBox(Icon icon, boolean selected)
	{
		super(icon, selected);
		addItemListener(this);
		fixedState = isSelected();
	}
	/**
	 * Constructor for FixedStateCheckBox
	 * @param text
	 * @param selected
	 */
	public FixedStateCheckBox(String text, boolean selected)
	{
		super(text, selected);
		addItemListener(this);
		fixedState = isSelected();
	}
	/**
	 * Constructor for FixedStateCheckBox
	 * @param text
	 * @param icon
	 */
	public FixedStateCheckBox(String text, Icon icon)
	{
		super(text, icon);
		addItemListener(this);
		fixedState = isSelected();
	}
	/**
	 * Constructor for FixedStateCheckBox
	 * @param text
	 * @param icon
	 * @param selected
	 */
	public FixedStateCheckBox(String text, Icon icon, boolean selected)
	{
		super(text, icon, selected);
		addItemListener(this);
		fixedState = isSelected();
	}
	public void setFixedState(final boolean fixedState)
	{
		this.fixedState = fixedState;
		setSelected(fixedState);
	}
	/**
	 * @param e
	 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	@Override
	public void itemStateChanged(ItemEvent e)
	{
		setSelected(fixedState);
	}
}
