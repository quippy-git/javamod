/*
 * @(#) PeekMeterComponent.java
 *
 * Created on 30.10.2025 by Daniel Becker
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
package de.quippy.javamod.main.gui.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.JComponent;

/**
 * @author Daniel Becker
 * @since 30.10.2025
 */
public class PeekMeterComponent extends JComponent
{
	private static final long serialVersionUID = -3051678250074031407L;
	
	private int peekLeft, peekRight;
	private boolean isSurround;
	
	private Color[] peekMeterColors;

	/**
	 * Constructor for PeekMeterComponent
	 */
	public PeekMeterComponent()
	{
		super();
		initialize();
	}
	private void initialize()
	{
		peekMeterColors = new Color[8];
		for (int i=0; i<8; i++)
		{
			final int r = i*255/8;
			final int g = 255-r;
			peekMeterColors[i] = new Color(r, g, 0);
		}
	}
	/**
	 * @since 30.10.2025
	 * @param peekLeft a value between 0 and 7
	 * @param peekRight a value between 0 and 7
	 * @param isSurround
	 */
	public void setMeterValues(final int peekLeft, final int peekRight, final boolean isSurround)
	{
		this.peekLeft = peekLeft;
		this.peekRight = peekRight;
		this.isSurround = isSurround;
		repaint();
	}
	/**
	 * @since 30.10.2025
	 * @param g
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param clipping
	 */
	private void fillRectWithClipping(final Graphics2D g, final int x, final int y, final int width, final int height, final Rectangle clipping)
	{
		Rectangle fillMe = new Rectangle(x, y, width, height);
		if (clipping!=null) fillMe = clipping.intersection(fillMe);
		if (fillMe.width>0 || fillMe.height>0) g.fillRect(fillMe.x, fillMe.y, fillMe.width, fillMe.height);
	}
	/**
	 * @since 30.10.2025
	 * @param g
	 */
	private void drawMeter(final Graphics2D g)
	{
		// let's clean up and fill with background
		final Dimension d = this.getSize();
		g.setColor(getBackground());
		g.fillRect(0, 0, d.width, d.height);

		// get dimensions
		final int middle = d.width>>1;
		final int barWidth = middle>>3;
		final int height = (d.height<3)?1:d.height-2;
		
		// with surround we will draw "backwards" towards center
		// just so we have some kind of indication
		int xLeft, xRight, addLeft, addRight;
		if (!isSurround)
		{
			xLeft    = middle - barWidth;
			xRight   = middle + 1;
			addLeft  = -barWidth;
			addRight = barWidth;
		}
		else
		{
			xLeft    = middle -     (barWidth * 8);
			xRight   = middle + 1 + (barWidth * 7);
			addLeft  = barWidth;
			addRight = -barWidth;
		}
		
		// draw the rectangles with clipping considered
		final Rectangle clipping = g.getClipBounds();
		for (int i=0; i<8; i++)
		{
			if (i<peekLeft)
			{
				g.setColor(peekMeterColors[i]);
				fillRectWithClipping(g, xLeft, 1, barWidth-1, height, clipping);
			}
			xLeft += addLeft;
			if (i<peekRight)
			{
				g.setColor(peekMeterColors[i]);
				fillRectWithClipping(g, xRight, 1, barWidth-1, height, clipping);
			}
			xRight += addRight;
		}
	}
	/**
	 * @param g
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	public void paintComponent(final Graphics g)
	{
		super.paintComponent(g);
		final Graphics2D gfx = (Graphics2D)g.create();
		try
		{
			drawMeter(gfx);
		}
		finally
		{
			gfx.dispose();
		}
	}
}
