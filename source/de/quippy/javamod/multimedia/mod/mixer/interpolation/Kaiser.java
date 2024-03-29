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

/**
 * @author Daniel Becker
 * @since 21.02.2024
 */
public class Kaiser
{
	public  static final int SINC_PHASES_BITS	= 12;
	private static final int SINC_PHASES		= (1<<SINC_PHASES_BITS);
	public  static final int SINC_WIDTH			= 8;
	public  static final int SINC_MASK			= (SINC_PHASES-1);
	private static final int SINC_PHASES_ALL	= SINC_PHASES * SINC_WIDTH;
	public  static final int SINC_QUANTSHIFT	= 15;
	public  static final int SINC_FRACSHIFT		= ModConstants.SHIFT - SINC_PHASES_BITS;
	
	public  static final int [] gKaiserSinc = new int [SINC_PHASES_ALL];
	public  static final int [] gDownsample13x = new int [SINC_PHASES_ALL];
	public  static final int [] gDownsample2x = new int [SINC_PHASES_ALL];
	
	public  static final int gDownsample2x_Limit	= 0x13 << (ModConstants.SHIFT-4);
	public  static final int gDownsample13x_Limit	= 0x18 << (ModConstants.SHIFT-4);

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
		double s = 1, ds = 1, d = 0;
		do
		{
			d = d + 2;
			ds = ds * (y * y) / (d * d);
			s = s + ds;
		}
		while(ds > 1E-7 * s);
		
		return s;
	}
	private static void getSinc(final int [] lut, final double beta, double cutoff)
	{
		if(cutoff>0.999)
		{
			// Avoid mixer overflows.
			// 1.0 itself does not make much sense.
			cutoff = 0.999;
		}
		final double izeroBeta = iZero(beta);
		final double kPi = 4.0 * Math.atan(1.0) * cutoff;
		for (int isrc = 0; isrc<SINC_PHASES_ALL; isrc++)
		{
			double fsinc;
			int ix = 7 - (isrc & 7);
			ix = (ix * SINC_PHASES) + (isrc >> 3);
			if(ix == ((SINC_WIDTH/2) * SINC_PHASES))
			{
				fsinc = 1.0;
			}
			else
			{
				final double x = (double)(ix - ((SINC_WIDTH/2) * SINC_PHASES)) * (double)(1.0 / SINC_PHASES);
				final double xPi = x * kPi;
				fsinc = Math.sin(xPi) * iZero(beta * Math.sqrt(1 - x * x * (1.0 / 16.0))) / (izeroBeta * xPi); // Kaiser window
			}
			final double coeff = fsinc * cutoff;
			lut[isrc] = (int) Math.floor(coeff * (1 << SINC_QUANTSHIFT));
		}
	}
	private static void initialize()
	{
		getSinc(gKaiserSinc, 9.6377d, 0.97d);
		getSinc(gDownsample13x, 8.5d, 0.5d);
		getSinc(gDownsample2x, 7.0d, 0.425d);
	}
}
