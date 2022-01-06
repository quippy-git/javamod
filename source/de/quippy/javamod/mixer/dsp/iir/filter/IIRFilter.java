/*
 * @(#) IIRFilter.java
 *
 * Created on 12.01.2012 by Daniel Becker
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
package de.quippy.javamod.mixer.dsp.iir.filter;

import de.quippy.javamod.system.Helpers;

/**
 * This class will do the IIRFilter using an array of filter classes
 * in sequentially order. All filters need to implement a calculation
 * method "performFilterCalculation"
 * @author Daniel Becker
 */
public class IIRFilter
{
    private int channels;
    private int iIndex;
    private int jIndex;
    private int kIndex;
    private int sampleBufferSize;
    private int filterLength;
	private IIRFilterBase[] filters;
	private float preAmp;

	/**
	 * @since 12.01.2012
	 */
	public IIRFilter(IIRFilterBase[] theFilters)
	{
		super();
		this.filters = theFilters;
		filterLength = theFilters.length;
		preAmp = 1.0f;
	}
	public void initialize(int channels, int sampleBufferSize)
	{
		this.channels = channels;
		this.sampleBufferSize = sampleBufferSize;
		clearHistory();
	}
	/**
	 * @since 14.01.2012
	 */
	public void clearHistory()
	{
		for (int f=0; f<filterLength; f++)
		{
			filters[f].clearHistory();
		}
		iIndex = 0;
		jIndex = 1; // iIndex - 2
		kIndex = 2; // iIndex - 1
	}
	public float getBand(int index)
	{
		return filters[index].getGain();
	}
	public void setBand(int index, float newGain)
	{
		filters[index].setGain(newGain);
	}
	/**
	 * @return the preAmp
	 */
	public float getPreAmp()
	{
		return (float)Helpers.getDBValueFrom(preAmp);
	}
	/**
	 * @param preAmp the preAmp to set
	 */
	public void setPreAmp(float newPreAmpDB)
	{
		preAmp = (float)Helpers.getDecimalValueFrom(newPreAmpDB);
	}
	/**
	 * @since 15.01.2012
	 * @return
	 */
	public IIRFilterBase[] getFilters()
	{
		return filters;
	}
	/**
	 * This will perform the filter on the samples
	 * @param ringBuffer
	 * @param preAmpedResultBuffer
	 * @param start
	 * @param length
	 * @since 12.01.2012
	 */
	public int doFilter(final float[] ringBuffer, final int start, final int length, final int useBands)
	{
		final float internalPreAmp = 1f/useBands;
		final float rest = 1.0f - internalPreAmp;
		final int end = start + length;
		int index = start;
		while (index < end)
		{
			for (int c=0; c<channels; c++)
			{
				final int sampleIndex = (index++) % sampleBufferSize;
				
				float sample = 0;
				// Run the difference equation
				final float preAmpedSample = ringBuffer[sampleIndex] * preAmp * internalPreAmp; 
				for (int f=0; f<useBands; f++)
				{
					IIRFilterBase filter = filters[f];
					sample += filter.performFilterCalculation(preAmpedSample, c, iIndex, jIndex, kIndex) * filter.amplitudeAdj;
				}
				sample += (ringBuffer[sampleIndex] * rest);
				ringBuffer[sampleIndex] = (sample>1.0f)?1.0f:((sample<-1.0f)?-1.0f:sample);
			}
			// Do indices maintenance
			iIndex = (iIndex + 1) % IIRFilterBase.HISTORYSIZE;
			jIndex = (jIndex + 1) % IIRFilterBase.HISTORYSIZE;
			kIndex = (kIndex + 1) % IIRFilterBase.HISTORYSIZE;
		}
		return length;
	}
}
