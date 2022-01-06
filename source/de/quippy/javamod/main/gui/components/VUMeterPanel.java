/*
 * @(#) VUMeterPanel.java
 *
 * Created on 26.09.2007 by Daniel Becker
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

import java.awt.Color;
import java.awt.Graphics;

/**
 * @author Daniel Becker
 * @since 26.09.2007
 */
public class VUMeterPanel extends MeterPanelBase
{
	private static final long serialVersionUID = -4587795218202329414L;

	private float currentLevel;
	private float currentMaxPeakLevel;
	private Color [] color;
	private Color [] colorLow;
	
	private float rampDownValue;
	private float maxPeakLevelRampDownValue;
	private float maxPeakLevelRampDownDelay;
	
	private int myAnzLines;

	/**
	 * Constructor for VUMeterPanel
	 */
	public VUMeterPanel(int updateRate)
	{
		super(updateRate);
		this.currentLevel = 0;
		this.currentMaxPeakLevel = 0;
		startThread();
	}

	/**
	 * Is called when the component is resized
	 * @see de.quippy.javamod.main.gui.components.MeterPanelBase#componentWasResized()
	 */
	@Override
	protected void componentWasResized(int newTop, int newLeft, int newWidth, int newHeight)
	{
		rampDownValue = (float)getDesiredFPS()/1000F;
		maxPeakLevelRampDownDelay = rampDownValue/100f;
		maxPeakLevelRampDownValue = 0;

		myAnzLines = newHeight>>1;
		
		color = new Color[myAnzLines];
		colorLow = new Color[myAnzLines];
		for (int i=0; i<myAnzLines; i++)
		{
			int color1 = i*255/myAnzLines;
			int color2 = 255-color1;
			color[i] = new Color(color1, color2, 0);
			colorLow[i] = new Color(color1>>1, color2>>1, 0);
		}
	}
	/**
	 * @since 29.09.2007
	 * @param samples
	 */
	public void setVUMeter(float [] samples)
	{
		if (samples!=null) setVUMeter(samples, samples.length);
	}
	/**
	 * @since 29.09.2007
	 * @param samples
	 * @param length
	 */
	public void setVUMeter(float [] samples, int length)
	{
		float newLevel = 0;
		for (int i=0; i<length; i++)
		{
			float v = samples[i];
			if (v<0) v*=-1f;
			if (v>newLevel) newLevel = v;
		}
		
		if (newLevel>1.0F) newLevel = 1.0F;

		if (newLevel>currentLevel)
		{
			currentLevel = newLevel;
			if (currentLevel>currentMaxPeakLevel)
			{
				currentMaxPeakLevel = currentLevel;
				maxPeakLevelRampDownValue = maxPeakLevelRampDownDelay;
			}
		}
	}
	/**
	 * @param g
	 * @see de.quippy.javamod.main.gui.components.MeterPanelBase#drawMeter(java.awt.Graphics)
	 */
	@Override
	protected void drawMeter(Graphics g, int newTop, int newLeft, int newWidth, int newHeight)
	{
		if (g!=null)
		{
			final int level = (int)(myAnzLines*currentLevel);
			final int maxPeakLevel = (int)(myAnzLines*currentMaxPeakLevel);
			
			for (int i=0; i<myAnzLines; i++)
			{
				g.setColor((i>=level && i!=maxPeakLevel)?colorLow[i]:color[i]);
				int ly = newHeight - (i<<1);
				g.drawLine(newLeft, newTop + ly, newLeft + newWidth, newTop + ly);
			}
		}

		currentLevel-=rampDownValue;
		if (currentLevel<0) currentLevel=0;
		
		currentMaxPeakLevel-=maxPeakLevelRampDownValue;
		if (currentMaxPeakLevel<0)
			currentMaxPeakLevel=0;
		else
			maxPeakLevelRampDownValue += maxPeakLevelRampDownDelay;
	}
}
