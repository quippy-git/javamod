/*
 * @(#) ImagePanel.java
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

import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JPanel;
import javax.swing.border.Border;

import de.quippy.javamod.system.Log;

/**
 * @author Daniel Becker
 * @since 25.07.2020
 */
public abstract class ImagePanel extends JPanel
{
	private static final long serialVersionUID = -321618334801726401L;

	private volatile int myTop;
	private volatile int myLeft;
	private volatile int myWidth;
	private volatile int myHeight;
	
	private Image imageBuffer;
	/**
	 * Constructor for ImagePanel
	 */
	public ImagePanel()
	{
		super(true);
		prepareComponentListener();
	}
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
	private synchronized void internalComponentWasResized()
	{
		imageBuffer=null;

		Border b = this.getBorder();
		Insets inset = (b==null)?new Insets(1,1,1,1):b.getBorderInsets(this);
		myTop = inset.top;
		myLeft = inset.left;
		myWidth = this.getWidth() - inset.left - inset.right;
		myHeight = this.getHeight() - inset.top - inset.bottom;
		
		repaint();
	}
	private Image getDoubleBuffer()
	{
    	if (imageBuffer==null && myWidth>0 && myHeight>0)
		{
			GraphicsConfiguration graConf = getGraphicsConfiguration();
			if (graConf!=null) imageBuffer = graConf.createCompatibleImage(myWidth, myHeight);
		}
		return imageBuffer;
	}
	public synchronized Image drawMe()
	{
       	Image buffer = getDoubleBuffer();
       	if (buffer!=null)
       	{
	   		try
	   		{
	   			drawImage(buffer.getGraphics(), 0, 0, myWidth, myHeight);
	   		}
	   		catch (Exception ex)
	   		{
	   			Log.error("drawMe:", ex);
	   		}
       	}
       	return buffer;
	}
	/**
	 * @param g
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		Image buffer = drawMe();
		if (buffer!=null) g.drawImage(buffer, myLeft, myTop, null);
	}
	/**
	 * Draws the image
	 */
	protected abstract void drawImage(Graphics g, int newTop, int newLeft, int newWidth, int newHeight);
}
