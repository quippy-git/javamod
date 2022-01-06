/*
 * @(#) IIRFilterBase.java
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
 *
 * Source adopted from package com.db.media.audio.dsp.*;
 * 
 * Copyright (c) 2000 Silvere Martin-Michiellot All Rights Reserved.
 *
 * Silvere Martin-Michiellot grants you ("Licensee") a non-exclusive,
 * royalty free, license to use, modify and redistribute this
 * software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) Licensee does not utilize the software in a manner
 * which is disparaging to Silvere Martin-Michiellot.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. Silvere Martin-Michiellot
 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL
 * Silvere Martin-Michiellot OR ITS LICENSORS BE LIABLE
 * FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF Silvere Martin-Michiellot HAS BEEN
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * This software is not designed or intended for use in on-line control of
 * aircraft, air traffic, aircraft navigation or aircraft communications; or in
 * the design, construction, operation or maintenance of any nuclear
 * facility. Licensee represents and warrants that it will not use or
 * redistribute the Software for such purposes.
 * 
 */
package de.quippy.javamod.mixer.dsp.iir.filter;

/**
 * @author Daniel Becker
 */
public abstract class IIRFilterBase
{
	protected int frequency;
	protected int sampleRate;
    protected static final int HISTORYSIZE = 3;
    protected float[][] inArray;
    protected float[][] outArray;
    protected float alpha;
	protected float beta;
	protected float gamma;
	protected float amplitudeAdj;

    /**
     * Default Constructor - to already set the GAIN
     * @since 09.01.2012
     */
    public IIRFilterBase()
    {
    	super();
    }
	/**
	 * Call this to initialize. Will set the memory to 0
	 * @since 09.01.2012
	 */
	public void initialize(final int sampleRate, final int channels, final int frequency, final float parameter)
	{
		this.frequency = frequency;
		this.sampleRate = sampleRate;
		inArray = new float[channels][HISTORYSIZE];
		outArray = new float[channels][HISTORYSIZE];
		clearHistory();
	}
	/**
	 * Clean the history
	 * @since 14.01.2012
	 */
	public void clearHistory()
	{
		int channels = inArray.length;
		for (int c=0; c<channels; c++)
		{
			for (int i=0; i<HISTORYSIZE; i++)
			{
				inArray[c][i] = outArray[c][i] = 0f;
			}
		}
	}
	/**
	 * Convert from decimalValue to DeciBel
	 * @since 14.01.2012
	 * @param dbValue
	 * @return
	 */
	public static float getIIRDecimalValueFrom(float dbValue)
	{
		double decimalValue = Math.pow(10, dbValue / 20.0);
		return (float)((decimalValue<1.0)?-decimalValue:decimalValue);
	}
	/**
	 * convert from DeciBel to decimalValue
	 * @since 14.01.2012
	 * @param decimalValue
	 * @return
	 */
	public static float getIIRDBValueFrom(final float decimalValue)
	{
		return (float)Math.log10((decimalValue<0)?-decimalValue:decimalValue)*20.0f;
	}
	/**
	 * Given a frequency of interest, calculate radians/sample
	 * @since 07.01.2012
	 * @param freq
	 * @return
	 */
	protected float calcRadiansPerSample(float freq)
	{
		return (float)((2.0 * Math.PI * freq) / sampleRate);
	}
	/**
	 * Return the radiant per sample at the frequency of interest
	 * @since 07.01.2012
	 * @return
	 */
	protected float getThetaZero()
	{
		return calcRadiansPerSample(frequency);
	}
	/**
	 * Set the amplitude adjustment to be applied to filtered data
	 * Values typically range from -.25 to +4.0.
	 * @param amplitudeAdj
	 * @since 09.01.2012
	 */
	public void setAmplitudeAdj(float newAmplitudeAdj)
	{
		amplitudeAdj = newAmplitudeAdj;
	}
	public float getAmplitudeAdj()
	{
		return amplitudeAdj;
	}
	/**
	 * Set the amplitude adjustment to be applied to filtered data
	 * Values typically range from -12 to +12 db.
	 * @param dbValue
	 * @since 13.01.2012
	 */
	public void setGain(float dbValue)
	{
		setAmplitudeAdj(IIRFilterBase.getIIRDecimalValueFrom(dbValue));
	}
	/**
	 * Get the amplitude adjustment in db value
	 * @since 14.01.2012
	 * @return
	 */
	public float getGain()
	{
		return getIIRDBValueFrom(getAmplitudeAdj());
	}
	/**
	 * @param sample
	 * @param channel
	 * @param iIndex
	 * @param jIndex
	 * @param kIndex
	 * @return
	 * @since 12.01.2012
	 */
	protected abstract float performFilterCalculation(final float sample, final int channel, final int iIndex, final int jIndex, final int kIndex);
}
