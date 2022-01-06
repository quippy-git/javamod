/*
 * @(#) GraphicEQ.java
 *
 * Created on 09.01.2012 by Daniel Becker
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
package de.quippy.javamod.mixer.dsp.iir;

import javax.sound.sampled.AudioFormat;

import de.quippy.javamod.mixer.dsp.DSPEffekt;
import de.quippy.javamod.mixer.dsp.iir.filter.IIRBandpassFilter;
import de.quippy.javamod.mixer.dsp.iir.filter.IIRFilter;
import de.quippy.javamod.mixer.dsp.iir.filter.IIRFilterBase;

/**
 * Here we have the effekt callback which will register with AudioProcessor
 * This class will create all bands, give access to the gain per band
 * and give a preamp which can be calculated
 * @author Daniel Becker
 */
public class GraphicEQ implements DSPEffekt
{
	private static final float Q = 1.4f;
	private static final int [] CENTER_FREQUENCIES = 
	{
	 	60, 170, 310, 600, 1000, 3000, 6000, 12000, 14000, 16000
	};
	private int usedBands;
	private IIRFilter theFilter;
	private boolean isActive;

	/**
	 * 
	 * @since 09.01.2012
	 */
	public GraphicEQ()
	{
		super();
		IIRFilterBase [] filters = new IIRBandpassFilter[CENTER_FREQUENCIES.length];
		for (int i=0; i<CENTER_FREQUENCIES.length; i++)
		{
			filters[i] = new IIRBandpassFilter();
		}
		theFilter = new IIRFilter(filters);
		isActive = false;
	}
	/**
	 * @param audioFormat
	 * @see de.quippy.javamod.mixer.dsp.DSPEffekt#initialize(javax.sound.sampled.AudioFormat)
	 * @since 09.01.2012
	 */
	public void initialize(final AudioFormat audioFormat, final int sampleBufferSize)
	{
		final int currentSampleRate = (int)audioFormat.getSampleRate();
		int breakFreq = currentSampleRate>>1;
		IIRFilterBase [] filters = theFilter.getFilters();
		for (int i=0; i<CENTER_FREQUENCIES.length; i++)
		{
			filters[i].initialize(currentSampleRate, audioFormat.getChannels(), CENTER_FREQUENCIES[i], Q);
		}
		theFilter.initialize(audioFormat.getChannels(), sampleBufferSize);

		int bands = 0;
		while (bands<CENTER_FREQUENCIES.length) if (CENTER_FREQUENCIES[bands] > breakFreq) break; else bands++;
		usedBands = bands;
	}
	/**
	 * @param active
	 * @see de.quippy.javamod.mixer.dsp.DSPEffekt#setIsActive(boolean)
	 */
	public void setIsActive(boolean active)
	{
		isActive = active;
	}
	/**
	 * @return
	 * @see de.quippy.javamod.mixer.dsp.DSPEffekt#isActive()
	 */
	public boolean isActive()
	{
		return isActive;
	}
	/**
	 * @return the filters
	 * @since 13.01.2012
	 */
	public int getBandCount()
	{
		return CENTER_FREQUENCIES.length;
	}
	/**
	 * @since 15.01.2012
	 * @param bandIndex
	 * @return
	 */
	public int getCenterFreq(int bandIndex)
	{
		return CENTER_FREQUENCIES[bandIndex];
	}
	/**
	 * @since 14.01.2012
	 * @param bandIndex
	 * @param newDB
	 */
	public void setBand(int bandIndex, float newDB)
	{
		theFilter.setBand(bandIndex, newDB);
	}
	/**
	 * @since 14.01.2012
	 * @param bandIndex
	 * @return
	 */
	public float getBand(int bandIndex)
	{
		return theFilter.getBand(bandIndex);
	}
	/**
	 * @since 14.01.2012
	 * @param newPreAmpDB
	 */
	public void setPreAmp(float newPreAmpDB)
	{
		theFilter.setPreAmp(newPreAmpDB);
	}
	/**
	 * @since 14.01.2012
	 * @return
	 */
	public float getPreAmpDB()
	{
		return theFilter.getPreAmp();
	}
	/**
	 * @param buffer
	 * @param start
	 * @param length
	 * @see de.quippy.javamod.mixer.dsp.DSPEffekt#doEffekt(float[], int, int)
	 * @since 09.01.2012
	 */
	public int doEffekt(final float[] buffer, final int start, final int length)
	{
		if (!isActive) return length;
		return theFilter.doFilter(buffer, start, length, usedBands);
	}
}
