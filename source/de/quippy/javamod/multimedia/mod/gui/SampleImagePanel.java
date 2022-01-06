/*
 * @(#) SampleImagePanel.java
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

import java.awt.Color;
import java.awt.Graphics;

import de.quippy.javamod.multimedia.mod.ModConstants;
import de.quippy.javamod.multimedia.mod.loader.instrument.Sample;

/**
 * @author Daniel Becker
 * @since 25.07.2020
 */
public class SampleImagePanel extends ImagePanel
{
	private static final long serialVersionUID = 1757748155250484172L;
    private static final Color LOOP_COLOR = Color.blue;
    private static final Color SUSTAINLOOP_COLOR = Color.green;
    private static final Color LINE_COLOR = Color.darkGray;
    private static final Color WAVE_COLOR = Color.red;
    private static final Color BACKGROUND_COLOR = Color.black;
    
    private Sample sample;

	/**
	 * Constructor for SampleImagePanel
	 */
	public SampleImagePanel()
	{
		super();
	}

	/**
	 * @param g
	 * @param newTop
	 * @param newLeft
	 * @param newWidth
	 * @param newHeight
	 * @see de.quippy.javamod.multimedia.mod.gui.ImagePanel#drawImage(java.awt.Graphics, int, int, int, int)
	 */
	@Override
	protected void drawImage(Graphics g, int newTop, int newLeft, int newWidth, int newHeight)
	{
		final int halfHeight = newHeight>>1;

		g.setColor(BACKGROUND_COLOR);
		g.fillRect(newLeft, newTop, newWidth, newHeight);
		
		if (sample==null)
		{
			g.setColor(LINE_COLOR);
			g.drawLine(newLeft, newTop + halfHeight, newLeft + newWidth, newTop + halfHeight);
		}
		else
		{
			int sustainLoopStart = -1;
			int sustainLoopEnd = -1;
			if ((sample.loopType&ModConstants.LOOP_SUSTAIN_ON)!=0)
			{
				sustainLoopStart = sample.sustainLoopStart;
				sustainLoopEnd = sample.sustainLoopStop;
			}
			int loopStart = -1;
			int loopEnd = -1;
			if ((sample.loopType&ModConstants.LOOP_ON)!=0)
			{
				loopStart = sample.loopStart;
				loopEnd = sample.loopStop;
			}
			
			if (sample.isStereo)
			{
				drawImage(g, newTop, newLeft, newWidth, halfHeight-1, loopStart, loopEnd, sustainLoopStart, sustainLoopEnd, sample.sampleL);
				drawImage(g, newTop + halfHeight+1, newLeft, newWidth, halfHeight, loopStart, loopEnd, sustainLoopStart, sustainLoopEnd, sample.sampleR);
			}
			else
			{
				drawImage(g, newTop, newLeft, newWidth, newHeight, loopStart, loopEnd, sustainLoopStart, sustainLoopEnd, sample.sampleL);
			}
		}
	}
	private void drawImage(Graphics g, int top, int left, int width, int height, int loopStart, int loopEnd, int sustainStart, int sustainEnd, long [] buffer)
	{
		final int halfHeight = height>>1;
		g.setColor(LINE_COLOR);
		g.drawLine(left, top + halfHeight, left + width, top + halfHeight);
		
		if (buffer!=null)
		{
			final int anzSamples = sample.length;
	//		final double xFactor = (double)width / (double)anzSamples;
	//		final double yFactor = (double)halfHeight / (double)ModConstants.CLIPP32BIT_MAX;
			
			int xpOld = 0;
			int ypOld = 0;
			g.setColor(WAVE_COLOR);
			for (int i=0; i<anzSamples; i++)
			{
	//			int xp = (int)((double)i * xFactor);
	//			int yp = halfHeight - (int)((double)buffer[i + Sample.INTERPOLATION_LOOK_AHEAD] * yFactor);
	
				int xp = (int)(((long)i*(long)width)/(long)anzSamples);
				int yp = halfHeight - (int)((buffer[i + Sample.INTERPOLATION_LOOK_AHEAD]*(long)halfHeight)>>31);

				if (xp<0) xp=0; else if (xp>width) xp=width;
				if (yp<0) yp=0; else if (yp>height) yp=height;
	
				if (i>0) g.drawLine(left + xpOld, top + ypOld, left + xp, top + yp);
				
				xpOld = xp;
				ypOld = yp;
			}
			if (loopStart!=-1)
			{
				g.setColor(LOOP_COLOR);
				int xp = (int)(((long)loopStart*(long)width)/(long)anzSamples);
				if (xp<0) xp=0; else if (xp>width) xp=width;
				g.drawLine(left + xp, top, left + xp, top + height);
				xp = (int)(((long)loopEnd*(long)width)/(long)anzSamples);
				if (xp<0) xp=0; else if (xp>width) xp=width;
				g.drawLine(left + xp, top, left + xp, top + height);
			}
			if (sustainStart!=-1)
			{
				g.setColor(SUSTAINLOOP_COLOR);
				int xp = (int)(((long)sustainStart*(long)width)/(long)anzSamples);
				if (xp<0) xp=0; else if (xp>width) xp=width;
				g.drawLine(left + xp, top, left + xp, top + height);
				xp = (int)(((long)sustainEnd*(long)width)/(long)anzSamples);
				if (xp<0) xp=0; else if (xp>width) xp=width;
				g.drawLine(left + xp, top, left + xp, top + height);
			}
		}
	}
	public void setSample(Sample sample)
	{
		this.sample = sample;
		repaint();
	}
}
