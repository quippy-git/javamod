/*
 * @(#) WindowedFIR.java
 *
 * Created on 15.06.2006 by Daniel Becker
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
package de.quippy.javamod.multimedia.mod.mixer.interpolation;

import de.quippy.javamod.multimedia.mod.ModConstants;

/**
 * @author Daniel Becker
 * @since 15.06.2006
 * This code is adopted from the Mod Plug Tracker by Olivier Lapicque <olivierl@jps.net>
 * But was now heavily reorganized and rewritten.
 * ------------------------------------------------------------------------------------------------
 *   fir interpolation doc,
 *	(derived from "an engineer's guide to fir digital filters", n.j. loy)
 *
 *	calculate coefficients for ideal lowpass filter (with cutoff = fc in 0..1 (mapped to 0..nyquist))
 *	  c[-N..N] = (i==0) ? fc : sin(fc*pi*i)/(pi*i)
 *
 *	then apply selected window to coefficients
 *	  c[-N..N] *= w(0..N)
 *	with n in 2*N and w(n) being a window function (see loy)
 *
 *	then calculate gain and scale filter coefs to have unity gain.
 * ------------------------------------------------------------------------------------------------
 */
public class WindowedFIR
{
	private static final int	SINC_PHASES_BITS	=	10;
	private static final int	SINC_PHASES			=	(1 << SINC_PHASES_BITS);
	public  static final int	SINC_MASK			=	(SINC_PHASES - 1);
	public  static final int	SINC_FRACSHIFT		=	ModConstants.SHIFT - SINC_PHASES_BITS;
	
	public  static final int	WFIR_QUANTBITS		=	15;
	private static final double	WFIR_QUANTSCALE		=	(double)(1 << WFIR_QUANTBITS);
	public  static final int	WFIR_WIDTH			=	8;

	// wfir types plus default:
	private static final int	WFIR_HANN			=	0;
	private static final int	WFIR_HAMMING		=	1;
	private static final int	WFIR_BLACKMANEXACT	=	2;
	private static final int	WFIR_BLACKMAN3T61	=	3;
	private static final int	WFIR_BLACKMAN3T67	=	4;
	private static final int	WFIR_BLACKMAN4T92	=	5;
	private static final int	WFIR_BLACKMAN4T74	=	6;
	private static final int	WFIR_KAISER4T		=	7;
	// Default settings of Schism:
//	private static final double	WFIR_CUTOFF			=	0.90d; // cutoff (1.0 == pi/2)
//	private static final int	WFIR_TYPE			=	WFIR_BLACKMANEXACT;
	// Default settings of ModPlugTracker:
	private static final double	WFIR_CUTOFF			=	0.97d;
	private static final int	WFIR_TYPE			=	WFIR_KAISER4T;

	public static final int[]	gWfirSinc_8			=	new int [SINC_PHASES * WFIR_WIDTH];
	public static final int[]	gDownsample13x_8	=	new int [SINC_PHASES * WFIR_WIDTH];
	public static final int[]	gDownsample2x_8		=	new int [SINC_PHASES * WFIR_WIDTH];

	public  static final int	gDownsample13x_Limit=	0x13 << (ModConstants.SHIFT-4);
	public  static final int	gDownsample2x_Limit	=	0x18 << (ModConstants.SHIFT-4);

	static
	{
		initialize();
	}

	/**
	 * Constructor for WindowedFIR
	 * is not needed!
	 */
	private WindowedFIR()
	{
		super();
	}
	private static double getCoefValue(final double x, final double cut, final int type)
	{
		if (Math.abs(x) < 1e-8d) return cut;

		final double xPi = x * Math.PI;
		final double si = Math.sin(cut * xPi) / xPi;
		final double posU = x + 4.0d; 
		final double idl = (2.0d * Math.PI) / 7.0d; // width - 1 = 7

		double wc;
		switch (type)
		{
			case WFIR_HANN:
				wc = 0.50d - 0.50d * Math.cos(idl * posU);
				break;
			case WFIR_HAMMING:
				wc = 0.54d - 0.46d * Math.cos(idl * posU);
				break;
			case WFIR_BLACKMANEXACT:
				wc = 0.42d - 0.50d * Math.cos(idl * posU) + 0.08d * Math.cos(2.0d * idl * posU);
				break;
			case WFIR_BLACKMAN3T61:
				wc = 0.44959d - 0.49364d * Math.cos(idl * posU) + 0.05677d * Math.cos(2.0d * idl * posU);
				break;
			case WFIR_BLACKMAN3T67:
				wc = 0.42323d - 0.49755d * Math.cos(idl * posU) + 0.07922d * Math.cos(2.0d * idl * posU);
				break;
			case WFIR_BLACKMAN4T92:
				wc = 0.35875d - 0.48829d * Math.cos(idl * posU) + 0.14128d * Math.cos(2.0d * idl * posU) - 0.01168d * Math.cos(3.0d * idl * posU);
				break;
			case WFIR_BLACKMAN4T74:
				wc = 0.40217d - 0.49703d * Math.cos(idl * posU) + 0.09392d * Math.cos(2.0d * idl * posU) - 0.00183d * Math.cos(3.0d * idl * posU);
				break;
			case WFIR_KAISER4T:
				wc = 0.40243d - 0.49804d * Math.cos(idl * posU) + 0.09831d * Math.cos(2.0d * idl * posU) - 0.00122d * Math.cos(3.0d * idl * posU);
				break;
			default:
				wc = 1.0d;
				break;
		}

		return wc * si;
	}
	private static void getSinc(final int[] targetLut, final double WFIRCutoff, final int WFIRType)
	{
		final double cut = WFIRCutoff;
		
		for (int phase = 0; phase < SINC_PHASES; phase++)
		{
			double fraction = (double)phase / (double)SINC_PHASES;
			final int idx = phase * WFIR_WIDTH;
			
			double[] coefs = new double[WFIR_WIDTH];
			double gain = 0.0d;
			
			for (int tap = 0; tap < WFIR_WIDTH; tap++)
			{
				double x = (double)(tap - 3) - fraction;
				coefs[tap] = getCoefValue(x, cut, WFIRType);
				gain += coefs[tap];
			}
			
			if (Math.abs(gain) > 1e-6d)
				gain = 1.0d / gain;
			else
				gain = 1.0d;
			
			for (int tap = 0; tap < WFIR_WIDTH; tap++)
			{
				double coef = Math.floor(0.5d + WFIR_QUANTSCALE * coefs[tap] * gain);
				targetLut[idx + tap] = (int)((coef < -WFIR_QUANTSCALE) ? -WFIR_QUANTSCALE : ((coef > WFIR_QUANTSCALE) ? WFIR_QUANTSCALE : coef));
			}
		}
	}
	private static void initialize()
	{
		// We have no extra low pass filter, we could use, so we do, as we do with Kaiser
		getSinc(gWfirSinc_8,		WFIR_CUTOFF,	WFIR_TYPE);
		getSinc(gDownsample13x_8,	0.50d,			WFIR_TYPE);
		getSinc(gDownsample2x_8,	0.425d,			WFIR_TYPE);
	}
}
