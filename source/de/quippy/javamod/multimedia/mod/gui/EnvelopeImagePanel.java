/*
 * @(#) EnvelopeImagePanel.java
 *
 * Created on 31.07.2020 by Daniel Becker
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

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JComponent;

import de.quippy.javamod.multimedia.mod.loader.instrument.Envelope;

/**
 * @author Daniel Becker
 * @since 31.07.2020
 */
public class EnvelopeImagePanel extends JComponent
{
	private static final long serialVersionUID = 2409671172691613794L;
    
	private static final Color GRID_COLOR = Color.lightGray;
    private static final Color GRIDSUB1_COLOR = Color.gray;
    private static final Color GRIDSUB2_COLOR = Color.darkGray;
    private static final Color ENVELOPE_COLOR = Color.red;
    private static final Color BACKGROUND_COLOR = Color.black;
    private static final Color RECT_COLOR = Color.white;
    private static final Color LOOP_COLOR = Color.yellow;
    private static final Color SUSTAINLOOP_COLOR = Color.green;

    private static final int MAX_WIDTH = 512;
    private static final int SMALLESTGRID = 4;
    private static final int BOXWIDTH = 2;

	private Envelope envelope;
	
	/**
	 * Constructor for EnvelopeImagePanel
	 */
	public EnvelopeImagePanel()
	{
		super();
		setDoubleBuffered(true);
	}

	private void drawGrid(Graphics g, int top, int left, int width, int height)
	{
		final int halfHeight = height>>1;

		g.setColor(GRID_COLOR);
		g.drawLine(left, top + halfHeight, left + width, top + halfHeight);
		
		for (int i=0; i<MAX_WIDTH; i++)
		{
			int x = (i * width) / MAX_WIDTH;
			if ((i % (SMALLESTGRID*4*4))==0)
			{
				g.setColor(GRID_COLOR);
				g.drawLine(x, 0, x, height);
			}
			else
			if ((i % (SMALLESTGRID*4))==0)
			{
				g.setColor(GRIDSUB1_COLOR);
				g.drawLine(x, 0, x, height);
			}
			else
			if ((i % SMALLESTGRID)==0)
			{
				g.setColor(GRIDSUB2_COLOR);
				g.drawLine(x, 0, x, height);
			}
		}
	}
	private int getX(final int xPos)
	{
		return (xPos * getWidth()) / MAX_WIDTH;
	}
	private int getY(final int yPos)
	{
		return getHeight() - ((yPos  * getHeight()) >> 6);
	}
	/**
	 * @since 07.01.2024
	 * @param gfx
	 */
	private void drawEnvelope(Graphics g)
	{
		final int width = getWidth();
		final int height = getHeight();

		g.setColor(BACKGROUND_COLOR);
		g.fillRect(0, 0, width, height);
		
		drawGrid(g, 0, 0, width, height);
		
		if (envelope!=null)
		{
			int oldx = 0;
			int oldy = 0;
			for (int i=0; i<envelope.nPoints; i++)
			{
				int x = getX(envelope.positions[i]);
				if (x<0) x=0; else if (x>width) x=width;
			
				int y = getY(envelope.value[i]);
				if (y<0) y=0; else if (y>height) y=height;
				
				g.setColor(ENVELOPE_COLOR);
				if (i>0) g.drawLine(oldx, oldy, x, y);
				g.setColor(RECT_COLOR);
				g.drawRect(x-BOXWIDTH, y-BOXWIDTH, (BOXWIDTH<<1)+1, (BOXWIDTH<<1)+1);
				
				oldx = x;
				oldy = y;
			}
			if (envelope.loop)
			{
				g.setColor(LOOP_COLOR);
				int x = getX(envelope.positions[envelope.loopStartPoint]) - BOXWIDTH;
				if (x<0) x=0; else if (x>width) x=width;
				g.drawLine(x, 0, x, height);
				x = getX(envelope.positions[envelope.loopEndPoint]) + BOXWIDTH + 1;
				if (x<0) x=0; else if (x>width) x=width;
				g.drawLine(x, 0, x, height);
			}
			if (envelope.sustain)
			{
				g.setColor(SUSTAINLOOP_COLOR);
				int x = getX(envelope.positions[envelope.sustainStartPoint]) - BOXWIDTH;
				if (x<0) x=0; else if (x>width) x=width;
				g.drawLine(x, 0, x, height);
				x = getY(envelope.positions[envelope.sustainEndPoint]) + BOXWIDTH + 1;
				if (x<0) x=0; else if (x>width) x=width;
				g.drawLine(x, 0, x, height);
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
		Graphics gfx = g.create();
		try
		{
			drawEnvelope(gfx);
		}
		finally
		{
			g.dispose();
		}
	}
	public void setEnvelope(Envelope envelope)
	{
		this.envelope = envelope;
		repaint();
	}
}
