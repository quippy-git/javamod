/*
 * @(#) ModDSP.java
 *
 * Created on 25.01.2022 by Daniel Becker
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
package de.quippy.javamod.multimedia.mod;

/**
 * This class contains certain DSP effects that can be used on
 * the mod output stream
 * @author Olivier Lapicque (MPT project), adoption to JavaMod: Daniel Becker
 * @since 25.01.2022
 */
public class ModDSP
{
	private static final int DEFAULT_XBASS_RANGE	= 14;	// (x+2)*20 Hz (320Hz)
	private static final int DEFAULT_XBASS_DEPTH	=  6;	// 1+(3>>(x-4)) (+6dB)
	private static final int DCR_AMOUNT				=  9;
//	private static final int DEFAULT_WIDE_MS		= 20;
	private static final int DEFAULT_SURROUND_MS	= 20;
	private static final int DEFAULT_SURROUND_DEPTH	= 12;

	// Bass Expansion: low-pass filter
	private long nXBassFlt_Y1;
	private long nXBassFlt_X1;
	private long nXBassFlt_B0;
	private long nXBassFlt_B1;
	private long nXBassFlt_A1;

	// DC Removal Biquad
	private long nDCRFlt_Y1l;
	private long nDCRFlt_X1l;
	private long nDCRFlt_Y1r;
	private long nDCRFlt_X1r;

	// Noise Reduction
	private long leftNR;
	private long rightNR;

//	// Wide Stereo Mix
//	private int maxWideStereo;
//	private long[] wideLBuffer;
//	private long[] wideRBuffer;
//	private int readPointer;
//	private int writePointer;

	// Surround Mix
	// Surround Encoding: 1 delay line + low-pass filter + high-pass filter
	private int nSurroundSize;
	private int nSurroundPos;
	// Surround Biquads
	private long nDolbyHP_Y1;
	private long nDolbyHP_X1;
	private long nDolbyLP_Y1;
	private long nDolbyHP_B0;
	private long nDolbyHP_B1;
	private long nDolbyHP_A1;
	private long nDolbyLP_B0;
	private long nDolbyLP_B1;
	private long nDolbyLP_A1;

	private long surroundBuffer[];


	/**
	 * Constructor for ModDSP
	 */
	public ModDSP()
	{
		super();
	}
	/**
	 * Init all DSP Effects
	 * @since 25.01.2022
	 * @param sampleFreq
	 */
	public void initModDSP(final int sampleFreq)
	{
		initMegaBass(sampleFreq);
		initDCRemoval();
		initNoiseReduction();
//		initWideStereo(sampleFreq);
		initSurround(sampleFreq);
	}
	private static double sgn(final double x)
	{
		return (x >= 0) ? 1.0d : -1.0d;
	}
	/**
	 * @since 25.01.2022
	 * @param scale
	 * @param out - pre init with array of 3 elements for b0, b1 and a1
	 * @param F_c
	 * @param F_s
	 * @param gainDC
	 * @param gainFT
	 * @param gainPI
	 */
	private void shelfEQ(final int scale, final long[] out, final long F_c, final int F_s, final double gainDC, final double gainFT, final double gainPI)
	{
		double a1, b0, b1;
		double gainFT2, gainDC2, gainPI2;
		double alpha, beta0, beta1, rho;
		double wT, quad;

		wT = Math.PI * F_c / F_s;
		gainPI2 = gainPI * gainPI;
		gainFT2 = gainFT * gainFT;
		gainDC2 = gainDC * gainDC;

		quad = gainPI2 + gainDC2 - (gainFT2*2);

		alpha = 0;

		if (quad != 0)
		{
			final double lambda = (gainPI2 - gainDC2) / quad;
			alpha  = lambda - sgn(lambda)*Math.sqrt(lambda*lambda - 1.0d);
		}

		beta0 = 0.5d * ((gainDC + gainPI) + (gainDC - gainPI) * alpha);
		beta1 = 0.5d * ((gainDC - gainPI) + (gainDC + gainPI) * alpha);
		rho   = (Math.sin((wT*0.5d) - (Math.PI/4.0d))) / (Math.sin((wT*0.5d) + (Math.PI/4.0d)));

		quad  = 1.0d / (1.0d + rho*alpha);

		a1 = - ((rho + alpha) * quad);
		b0 = (beta0 + rho*beta1) * quad;
		b1 = (beta1 + rho*beta0) * quad;

		out[0] = (long)((a1 * scale) + 0.5d);
		out[1] = (long)((b0 * scale) + 0.5d);
		out[2] = (long)((b1 * scale) + 0.5d);
	}
	/**
	 * @since 25.01.2022
	 * @param sampleFreq
	 */
	public void initMegaBass(final int sampleFreq)
	{
		nXBassFlt_Y1 = 0;
		nXBassFlt_X1 = 0;

		final long nXBassCutOff = 50 + (DEFAULT_XBASS_RANGE+2) * 20;
		final long nXBassGain = DEFAULT_XBASS_DEPTH;
		// because of defaults we do not need to check this
		//if (nXBassGain<2) nXBassGain=1; else if (nXBassGain>8) nXBassGain=8;
		//if (nXBassCutOff<60) nXBassCutOff=60; else if (nXBassCutOff>600) nXBassCutOff=600;

		final long [] result = new long[3];
		shelfEQ(1024, result, nXBassCutOff, sampleFreq,
				1.0d + (1.0d/16.0d) * (0x300 >> nXBassGain),
				1.0d,
				0.0000001d);

		if (nXBassGain > 5)
		{
			result[1] >>= (nXBassGain-5);
			result[2] >>= (nXBassGain-5);
		}
		nXBassFlt_A1 = result[0];
		nXBassFlt_B0 = result[1];
		nXBassFlt_B1 = result[2];
	}
	/**
	 * @since 25.01.2022
	 * @param sample
	 */
	public void processMegaBass(final long[] sample)
	{
		long x1 = nXBassFlt_X1;
		long y1 = nXBassFlt_Y1;

		final long x_m = (sample[0]+sample[1]+0x100)>>9;
		y1 = (nXBassFlt_B0 * x_m + nXBassFlt_B1 * x1 + nXBassFlt_A1 * y1) >> (10-8);
		x1 = x_m;
		sample[0] += y1;
		sample[1] += y1;
		y1 = (y1+0x80) >> 8;

		nXBassFlt_X1 = x1;
		nXBassFlt_Y1 = y1;
	}
	/**
	 * @since 25.01.2022
	 */
	public void initDCRemoval()
	{
		// DC Removal Biquad
		nDCRFlt_Y1l = 0;
		nDCRFlt_X1l = 0;
		nDCRFlt_Y1r = 0;
		nDCRFlt_X1r = 0;
	}
	/**
	 * @since 25.01.2022
	 * @param sample
	 */
	public void processDCRemoval(final long[] sample)
	{
		long y1l = nDCRFlt_Y1l, x1l = nDCRFlt_X1l;
		long y1r = nDCRFlt_Y1r, x1r = nDCRFlt_X1r;

		final long inL = sample[0];
		final long inR = sample[1];
		final long diffL = x1l - inL;
		final long diffR = x1r - inR;
		x1l = inL;
		x1r = inR;
		final long outL = diffL / (1 << (DCR_AMOUNT + 1)) - diffL + y1l;
		final long outR = diffR / (1 << (DCR_AMOUNT + 1)) - diffR + y1r;
		sample[0] = outL;
		sample[1] = outR;
		y1l = outL - outL / (1 << DCR_AMOUNT);
		y1r = outR - outR / (1 << DCR_AMOUNT);

		nDCRFlt_Y1l = y1l;
		nDCRFlt_X1l = x1l;
		nDCRFlt_Y1r = y1r;
		nDCRFlt_X1r = x1r;
	}
	/**
	 * @since 25.01.2022
	 */
	public void initNoiseReduction()
	{
		leftNR = 0;
		rightNR = 0;
	}
	/**
	 * @since 25.01.2022
	 * @param sample
	 */
	public void processNoiseReduction(final long[] sample)
	{
		long vnr = sample[0]>>1;
		sample[0] = vnr + leftNR;
		leftNR = vnr;

		vnr = sample[1]>>1;
		sample[1] = vnr + rightNR;
		rightNR = vnr;
	}
////////////////////////////////////////////////////////////////////////////////
// SIMPLE WIDE STEREO REMOVED, REPLACED BY Surround
//	/**
//	 * @since 25.01.2022
//	 * @param sampleFreq
//	 */
//	public void initWideStereo(final int sampleFreq)
//	{
//		// initialize the wide stereo mix
//		maxWideStereo = (DEFAULT_WIDE_MS * sampleFreq) / 1000;
//		wideLBuffer = new long[maxWideStereo];
//		wideRBuffer = new long[maxWideStereo];
//		readPointer = 0;
//		writePointer=maxWideStereo-1;
//	}
//	/**
//	 * @since 25.01.2022
//	 * @param sample
//	 */
//	public void processWideStereo(final long[] sample)
//	{
//		wideLBuffer[writePointer]=sample[0];
//		wideRBuffer[writePointer++]=sample[1];
//		if (writePointer>=maxWideStereo) writePointer=0;
//
//		sample[1]+=(wideLBuffer[readPointer]>>1);
//		sample[0]+=(wideRBuffer[readPointer++]>>1);
//		if (readPointer>=maxWideStereo) readPointer=0;
//	}
////////////////////////////////////////////////////////////////////////////////
	/**
	 * @since 05.02.2022
	 * @param sampleFreq
	 */
	public void initSurround(final int sampleFreq)
	{
		nSurroundSize = (DEFAULT_SURROUND_MS * sampleFreq) / 1000;
		surroundBuffer = new long[nSurroundSize];

		int nDolbyDepth = DEFAULT_SURROUND_DEPTH;
		// because of defaults we do not need to check this
		//if (nDolbyDepth < 1) nDolbyDepth = 1; else if (nDolbyDepth > 16) nDolbyDepth = 16;

		nSurroundPos = 0;

		// Setup biquad filters
		final long [] result = new long[3];
		shelfEQ(1024, result, 200, sampleFreq, 0, 0.5d, 1);
		nDolbyHP_A1 = result[0];
		nDolbyHP_B0 = result[1];
		nDolbyHP_B1 = result[2];
		shelfEQ(1024, result, 7000, sampleFreq, 1, 0.75d, 0);
		nDolbyLP_A1 = result[0];
		nDolbyLP_B0 = result[1];
		nDolbyLP_B1 = result[2];
		nDolbyHP_X1 = nDolbyHP_Y1 = nDolbyLP_Y1 = 0;
		// Surround Level
		nDolbyHP_B0 = (nDolbyHP_B0 * nDolbyDepth) >> 5;
		nDolbyHP_B1 = (nDolbyHP_B1 * nDolbyDepth) >> 5;
		// +6dB
		nDolbyLP_B0 <<= 1;
		nDolbyLP_B1 <<= 1;
	}
	/**
	 * @since 05.02.2022
	 * @param sample
	 */
	public void processStereoSurround(final long[] sample)
	{
		// Delay
		final long sEcho = surroundBuffer[nSurroundPos];
		surroundBuffer[nSurroundPos++] = (sample[0]+sample[1]+256) >> 9;
		if (nSurroundPos >= nSurroundSize) nSurroundPos = 0;

		// High-pass
		final long v0 = (nDolbyHP_B0 * sEcho + nDolbyHP_B1 * nDolbyHP_X1 + nDolbyHP_A1 * nDolbyHP_Y1) >> 10;

		// Low-pass
		final long v = (nDolbyLP_B0 * v0 + nDolbyLP_B1 * nDolbyHP_Y1 + nDolbyLP_A1 * nDolbyLP_Y1) >> (10-8);

		// Add echo
		sample[0] += v;
		sample[1] -= v;

		// and remember
		nDolbyHP_Y1 = v0;
		nDolbyHP_X1 = sEcho;
		nDolbyLP_Y1 = v >> 8;
	}
}
