/*
 * @(#) MeterPanelBase.java
 *
 * Created on 01.01.2008 by Daniel Becker
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

import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.border.Border;

import de.quippy.javamod.system.Log;

/**
 * @author Daniel Becker
 * @since 01.01.2008
 */
public abstract class MeterPanelBase extends ThreadUpdatePanel
{
	private static final long serialVersionUID = -7284099301353768209L;

	private volatile int myTop;
	private volatile int myLeft;
	private volatile int myWidth;
	private volatile int myHeight;
	
	private Image imageBuffer;
	/**
	 * Constructor for MeterPanelBase
	 */
	public MeterPanelBase(int desiredFPS)
	{
		super(desiredFPS);
		prepareComponentListener();
	}
	/**
	 * @since 06.10.2007
	 */
	private void prepareComponentListener()
	{
		addComponentListener(new ComponentListener()
		{
			public void componentHidden(ComponentEvent e) {}
			public void componentMoved(ComponentEvent e) {}
			public void componentShown(ComponentEvent e) {}
			public void componentResized(ComponentEvent e)
			{
				internalComponentWasResized();
			}
		});
	}
	/**
	 * Is called when the component is resized
	 */
	protected synchronized void internalComponentWasResized()
	{
		imageBuffer=null;

		Border b = this.getBorder();
		Insets inset = (b==null)?new Insets(1,1,1,1):b.getBorderInsets(this);
		myTop = inset.top;
		myLeft = inset.left;
		myWidth = this.getWidth() - inset.left - inset.right;
		myHeight = this.getHeight() - inset.top - inset.bottom;
		
		if (myWidth>0 && myHeight>0) componentWasResized(0, 0, myWidth, myHeight);
	}
	protected synchronized Image getDoubleBuffer()
	{
    	if (imageBuffer==null && myWidth>0 && myHeight>0)
		{
			GraphicsConfiguration graConf = getGraphicsConfiguration();
			if (graConf!=null) imageBuffer = graConf.createCompatibleImage(myWidth, myHeight);
		}
		return imageBuffer;
	}
	/**
	 * @since 06.10.2007
	 */
	protected synchronized void doThreadUpdate()
	{
       	Image buffer = getDoubleBuffer();
       	if (buffer!=null)
       	{
	   		try
	   		{
	   			drawMeter(buffer.getGraphics(), 0, 0, myWidth, myHeight);
		   		repaint();
	   		}
	   		catch (Exception ex)
	   		{
	   			Log.error("[MeterPanelBase]:", ex);
	   		}
       	}
	}
	/**
	 * @param g
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		Image buffer = getDoubleBuffer();
		if (buffer!=null) g.drawImage(buffer, myLeft, myTop, null);
	}
	/**
	 * Draws the meter
	 * @since 01.01.2008
	 * @param g
	 */
	protected abstract void drawMeter(Graphics g, int newTop, int newLeft, int newWidth, int newHeight);
	/**
	 * Will be called from "internalComponentWasResized
	 * to signal a resize event
	 * @since 01.01.2008
	 */
	protected abstract void componentWasResized(int newTop, int newLeft, int newWidth, int newHeight);
}
