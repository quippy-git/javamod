/*
 * @(#) IIRHighpassFilter.java
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
public class IIRHighpassFilter extends IIRFilterBase
{
    /**
     * Default Constructor - to already set the GAIN
     * @since 09.01.2012
     */
    public IIRHighpassFilter()
    {
    	super();
    }
	/**
	 * @param sampleRate
	 * @param channels
	 * @param frequency
	 * @param parameter
	 * @see de.quippy.javamod.mixer.dsp.iir.filter.IIRFilterBase#initialize(int, int, int, float)
	 * @since 09.01.2012
	 */
	@Override
	public void initialize(final int sampleRate, final int channels, final int frequency, final float parameter)
	{
		super.initialize(sampleRate, channels, frequency, parameter);
        // thetaZero = 2 * Pi * Freq * T or (2 * Pi * Freq) / sampleRate
        // where Freq is cutoff frequency
        float thetaZero = getThetaZero();
        float theSin = parameter / (2.0f * (float)Math.sin(thetaZero));

        // Beta relates gain to cutoff freq
        beta = 0.5f * ((1.0f - theSin) / (1.0f + theSin));
        // Final filter coefficient
        gamma = (0.5f + beta) * (float)Math.cos(thetaZero);
        // For unity gain
        alpha = (0.5f + beta + gamma) / 4.0f;
        // multiply by two to save time later
        alpha *= 2f;
        beta *= 2f;
        gamma *= 2f;
	}
	/**
	 * @param sample
	 * @param channel
	 * @param iIndex
	 * @param jIndex
	 * @param kIndex
	 * @return
	 * @see de.quippy.javamod.mixer.dsp.iir.filter.IIRFilterBase#performFilterCalculation(float, int, int, int, int)
	 * @since 12.01.2012
	 */
	@Override
	protected float performFilterCalculation(final float sample, final int channel, final int iIndex, final int jIndex, final int kIndex)
	{
		final float [] x = inArray[channel];
		final float [] y = outArray[channel];
		
		y[iIndex] = (alpha * ((x[iIndex] = sample) - (2.0f * x[kIndex]) + x[jIndex])) +
					(gamma * y[kIndex]) - 
					(beta  * y[jIndex]);
		return y[iIndex];
	}
}
