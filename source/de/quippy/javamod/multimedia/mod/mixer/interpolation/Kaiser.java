/*
 * @(#) Kaiser.java
 *
 * Created on 21.02.2024 by Daniel Becker
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
package de.quippy.javamod.multimedia.mod.mixer.interpolation;

import de.quippy.javamod.multimedia.mod.ModConstants;
import de.quippy.javamod.system.FastMath;

/**
 * @author Daniel Becker
 * @since 21.02.2024
 */
public class Kaiser
{
	public  static final int SINC_PHASES_BITS		= 12;
	private static final int SINC_PHASES			= (1<<SINC_PHASES_BITS);
	public  static final int SINC_MASK				= (SINC_PHASES-1);
	public  static final int SINC_QUANTSHIFT		= 15;
	public  static final double SINC_QUANTSCALE		= (double)(1 << SINC_QUANTSHIFT);
	public  static final int SINC_FRACSHIFT			= ModConstants.SHIFT - SINC_PHASES_BITS;

	public  static final int SINC_WIDTH_8			= 8 * SINC_PHASES;
	public  static final int[] gKaiserSinc_8		= new int [SINC_WIDTH_8];
	public  static final int[] gDownsample13x_8		= new int [SINC_WIDTH_8];
	public  static final int[] gDownsample2x_8		= new int [SINC_WIDTH_8];

	public  static final int SINC_WIDTH_16			= 16 * SINC_PHASES;
	public  static final int[] gKaiserSinc_16		= new int [SINC_WIDTH_16];
	public  static final int[] gDownsample13x_16	= new int [SINC_WIDTH_16];
	public  static final int[] gDownsample2x_16		= new int [SINC_WIDTH_16];

	public  static final int gDownsample13x_Limit	= 0x13 << (ModConstants.SHIFT-4);
	public  static final int gDownsample2x_Limit	= 0x18 << (ModConstants.SHIFT-4);

	static
	{
		initialize();
	}

	/**
	 * Constructor for Kaiser
	 */
	public Kaiser()
	{
		super();
	}

	private static double iZero(final double y)
	{
		double s = 1d, ds = 1d, d = 0d;
		do
		{
			d = d + 2;
			ds = ds * (y * y) / (d * d);
			s = s + ds;
		}
		while(ds > 1E-9 * s);

		return s;
	}
	private static void getSinc(final int numTaps, final int[] lut, final double beta, double cutoff)
	{
		if(cutoff>0.999d)
		{
			// Avoid mixer overflows.
			// 1.0 itself does not make much sense.
			cutoff = 0.999d;
		}
		final double izeroBeta = iZero(beta);
		final double kPi = 4.0d * Math.atan(1.0d) * cutoff; // 4.0 * Math.atan(1.0d) is equal to PI - with highest precision
		
		final int length = numTaps * SINC_PHASES;
		final int tapBits = FastMath.log2(numTaps);
		final int tapsMinus1 = numTaps - 1;
		final double xMul = 1.0 / ((numTaps / 2) * (numTaps / 2));
		final int midTap = (numTaps / 2) * SINC_PHASES;
		
		for (int isrc = 0; isrc<length; isrc++)
		{
			final int ix = ((tapsMinus1 - (isrc & tapsMinus1)) * SINC_PHASES) + (isrc >> tapBits);

			double dsinc;
			if (ix == midTap)
			{
				dsinc = 1.0d;
			}
			else
			{
				final double x = (double)(ix - midTap) * (1.0d / (double)SINC_PHASES);
				final double xPi = x * kPi;
				dsinc = Math.sin(xPi) * iZero(beta * Math.sqrt(1.0d - x * x * xMul)) / (izeroBeta * xPi); // Kaiser window
			}
			final double coeff = dsinc * cutoff;
			lut[isrc] = (int) Math.floor(coeff * SINC_QUANTSCALE);
		}
	}
	private static void initialize()
	{
		getSinc( 8, gKaiserSinc_8,		9.6377d,	0.97d);
		getSinc( 8, gDownsample13x_8,	8.5d,		0.5d);
		getSinc( 8, gDownsample2x_8,	7.0d,		0.425d);
		
		getSinc(16, gKaiserSinc_16,		9.6377d,	0.97d);
		getSinc(16, gDownsample13x_16,	8.5d,		0.5d);
		getSinc(16, gDownsample2x_16,	7.0d,		0.425d);
	}
}
