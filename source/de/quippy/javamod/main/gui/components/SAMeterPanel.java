/*
 * @(#) SAMeterPanel.java
 *
 * Created on 30.09.2007 by Daniel Becker
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
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

import de.quippy.javamod.mixer.dsp.FFT;
import de.quippy.javamod.system.FastMath;

/**
 * @author Daniel Becker
 * @since 30.09.2007
 */
public class SAMeterPanel extends MeterPanelBase
{
	private static final long serialVersionUID = 3032239961770238793L;

	private static final int FFT_SAMPLE_SIZE = 512;

	protected static final int DRAW_SA_METER = 0;
    protected static final int DRAW_WAVE_METER = 1;
    protected static final int DRAW_SK_METER = 2;
    
    private static final Color WAVEMETER_LINE_COLOR = Color.darkGray;
    private static final Color WAVEMETER_WAVE_COLOR = Color.green;

	private FFT fftCalc;
	private Color [] color;
	private Color [] SKcolor;
	private int SKMax;
	private float [] fftLevels;
	private float [] maxFFTLevels;
	
	private float [] floatSamples;
	private int anzSamples;

	private int bands;
	private int multiplier;
	private float rampDownValue;
	private float [] maxPeakLevelRampDownValue;
	private float maxPeakLevelRampDownDelay;
	
	private int myBottom;
	private float myHalfHeight;
	private int barWidth;
	
	private int drawWhat;
	private boolean switched;
    
	/**
	 * Constructor for SAMeterPanel
	 */
	public SAMeterPanel(int updateRate, int bands)
	{
		super(updateRate);
		
		this.bands = bands;
		this.fftCalc = new FFT(SAMeterPanel.FFT_SAMPLE_SIZE);
		this.multiplier = (SAMeterPanel.FFT_SAMPLE_SIZE>>1) / bands;
		
		this.fftLevels = new float[this.bands];
		this.maxFFTLevels = new float[this.bands];
		this.maxPeakLevelRampDownValue = new float[this.bands];

		this.drawWhat = DRAW_SA_METER;
		this.switched = true;
		
		this.prepareDisplayToggleListener();

		startThread();
	}
    /**
     * @since 06.10.2007
     */
    private void prepareDisplayToggleListener()
	{
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent pEvent)
			{
				if (pEvent.getButton() == MouseEvent.BUTTON1)
				{
					drawWhat++;
					if (drawWhat > DRAW_SK_METER) drawWhat = DRAW_SA_METER;
					switched = true;
				}
			}
		});
	}
    public void setDrawWhatTo(int newDrawWhat)
    {
    	drawWhat = newDrawWhat;
    }
	/**
	 * @return the drawWhat
	 */
	public int getDrawWhat()
	{
		return drawWhat;
	}
	/**
	 * Is called when the component is resized
	 * @see de.quippy.javamod.main.gui.components.MeterPanelBase#componentWasResized()
	 */
	@Override
    protected void componentWasResized(int newTop, int newLeft, int newWidth, int newHeight)
    {
		rampDownValue = (float)getDesiredFPS()/2000F;
		maxPeakLevelRampDownDelay = rampDownValue/50F;
		
		if (newTop<0 || newLeft<0 || newWidth<0 || newHeight<0) return;

		myBottom = newTop + newHeight;
		myHalfHeight = (float)newHeight/2f;
		barWidth = newWidth/bands;

		color = new Color[newHeight+1];
		for (int i=0; i<=newHeight; i++)
		{
			int color1 = i*255/newHeight;
			int color2 = 255-color1;
			color[i] = new Color(color1, color2, 0);
		}
		SKMax = 1024;
		SKcolor = new Color[SKMax];
		for (int i=0; i<256; i++)
		{
			SKcolor[i] = new Color(0, 0, i);
		}
		for (int i=256; i<512; i++)
		{
			SKcolor[i] = new Color(0, i-256, 511-i);
		}
		for (int i=512; i<768; i++)
		{
			SKcolor[i] = new Color(i-512, 767-i, 0);
		}
		for (int i=768; i<1024; i++)
		{
			SKcolor[i] = new Color(255, i-768, 0);
		}
    }
	/**
	 * Will set new Values
	 * @since 06.10.2007
	 * @param newSamples
	 */
	public void setMeter(float [] newSamples)
	{
		if (newSamples!=null)
		{
			anzSamples = newSamples.length;
			if (floatSamples==null || floatSamples.length != anzSamples) floatSamples = new float[anzSamples];
			System.arraycopy(newSamples, 0, floatSamples, 0, anzSamples);
			float [] resultFFTSamples = fftCalc.calculate(floatSamples);
	        
			for (int a=0, bd=0; bd<bands; a+=multiplier, bd++)
	        {
	            float wFs = resultFFTSamples[a];

	            for (int b=1; b<multiplier; b++) wFs+=resultFFTSamples[a+b];
	            wFs *= (float)FastMath.log(bd + 2);
	            
	            if (wFs > 1.0F) wFs = 1.0F;
	            if (wFs>fftLevels[bd])
	            {
	            	fftLevels[bd]=wFs;
					if (fftLevels[bd]>maxFFTLevels[bd])
					{
						maxFFTLevels[bd] = fftLevels[bd];
						maxPeakLevelRampDownValue[bd] = maxPeakLevelRampDownDelay;
					}
	            }
	        }
		}
		else
		{
			Arrays.fill(floatSamples, 0);
		}
	}
	/**
	 * @param g
	 * @see de.quippy.javamod.main.gui.components.MeterPanelBase#drawMeter(java.awt.Graphics)
	 */
	@Override
	protected void drawMeter(Graphics g, int newTop, int newLeft, int newWidth, int newHeight)
	{
		drawMeter(g, newTop, newLeft, newWidth, newHeight, true);
	}
	/**
	 * Internal method doing the drawing
	 * @param g
	 * @param newTop
	 * @param newLeft
	 * @param newWidth
	 * @param newHeight
	 * @param doClear
	 * @since 06.05.2011
	 */
	protected void drawMeter(Graphics g, int newTop, int newLeft, int newWidth, int newHeight, boolean doClear)
	{
		if (g!=null)
		{
			switch (drawWhat)
			{
				default:
				case DRAW_SA_METER: drawSAMeter(g, newTop, newLeft, newWidth, newHeight, doClear); break; 
				case DRAW_WAVE_METER: drawWaveMeter(g, newTop, newLeft, newWidth, newHeight, doClear); break; 
				case DRAW_SK_METER: drawSKMeter(g, newTop, newLeft, newWidth, newHeight); break; 
			}
		}
		for (int i=0; i<bands; i++)
		{
			fftLevels[i]-=rampDownValue;
			if (fftLevels[i]<0.0F) fftLevels[i]=0.0F;
			
			maxFFTLevels[i] -= maxPeakLevelRampDownValue[i];
			if (maxFFTLevels[i]<0.0F) 
				maxFFTLevels[i]=0.0F;
			else
				maxPeakLevelRampDownValue[i] += maxPeakLevelRampDownDelay;
		}
	}
	/**
	 * @since 06.10.2007
	 * @param g
	 */
	private void drawWaveMeter(Graphics g, int newTop, int newLeft, int newWidth, int newHeight, boolean doClear)
	{
		if (doClear)
		{
			g.setColor(Color.BLACK);
			g.fillRect(newLeft, newTop, newWidth, newHeight);
		}
		
		g.setColor(WAVEMETER_LINE_COLOR);
		g.drawLine(newLeft, newTop + (int)myHalfHeight, newLeft + newWidth, newTop + (int)myHalfHeight);

		if (floatSamples==null) return;
		
		int add = (anzSamples / newWidth)>>1;
		if (add<=0) add=1;
		
		int xpOld = 0;
		int ypOld = (int)(myHalfHeight-(floatSamples[0]*myHalfHeight));
		if (ypOld<0) ypOld=0; else if (ypOld>newHeight) ypOld=newHeight;

		g.setColor(WAVEMETER_WAVE_COLOR);
		for (int i=add; i<anzSamples; i+=add)
		{
			int xp = (i*newWidth)/anzSamples;
			if (xp<0) xp=0; else if (xp>newWidth) xp=newWidth;
				
			int yp = (int)(myHalfHeight-(floatSamples[i]*myHalfHeight));
			if (yp<0) yp=0; else if (yp>newHeight) yp=newHeight;
			
			g.drawLine(newLeft + xpOld, newTop + ypOld, newLeft + xp, newTop + yp);
			xpOld = xp;
			ypOld = yp;
		}
	}
	/**
	 * @since 06.10.2007
	 * @param g
	 */
	private void drawSAMeter(Graphics g, int newTop, int newLeft, int newWidth, int newHeight, boolean doClear)
	{
		if (doClear)
		{
			g.setColor(Color.BLACK);
			g.fillRect(newLeft, newTop, newWidth, newHeight);
		}
		
		for (int i=0; i<bands; i++)
		{
			// Let's Draw it...
			int barX = i*barWidth;
			int barX1 = barX + barWidth - 2;
			int barHeight = (int)(((float)newHeight)*fftLevels[i]);
			int maxBarHeight = (int)(((float)newHeight)*maxFFTLevels[i]);
//			if (barHeight >= color.length) barHeight = color.length-1;
//			if (maxBarHeight >= color.length) maxBarHeight = color.length-1;
			
			int c = barHeight;
			for (int y=myBottom-barHeight; y<myBottom; y++)
			{
				g.setColor(color[c--]);
				g.drawLine(newLeft + barX, newTop + y, newLeft + barX1, newTop + y);
			}
			if (maxBarHeight>barHeight)
			{
				g.setColor(color[maxBarHeight]);
				g.drawLine(newLeft + barX, newTop + myBottom - maxBarHeight, newLeft + barX1, newTop + myBottom - maxBarHeight);
			}
		}
	}
	/**
	 * @since 26.10.2007
	 * @param g
	 */
	private void drawSKMeter(Graphics g, int newTop, int newLeft, int newWidth, int newHeight)
	{
		if (switched)
		{
			g.setColor(Color.BLACK);
			g.fillRect(newLeft, newTop, newWidth, newHeight);
			switched = false;
		}
		g.copyArea(newLeft, newTop, newWidth-1, newHeight, 1, 0);
		int max = bands-1;
		for (int i=0; i<=max; i++)
		{
			int bary = (newHeight * (max-i)) / bands;
			g.setColor(SKcolor[(int)(((float)(SKMax-1))*fftLevels[i])]);
			g.drawLine(newLeft, newTop + bary, newLeft, newTop + bary + 2);
		}
	}
}
