/*
 * @(#) CubicSpline.java
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
 * ------------------------------------------------------------------------------------------------
 *   cubic spline interpolation doc,
 *	(derived from "digital image warping", g. wolberg)
 *
 *	interpolation polynomial: f(x) = A3*(x-floor(x))**3 + A2*(x-floor(x))**2 + A1*(x-floor(x)) + A0
 *
 *	with Y = equispaced data points (dist=1), YD = first derivates of data points and IP = floor(x)
 *	the A[0..3] can be found by solving
 *	  A0  = Y[IP]
 *	  A1  = YD[IP]
 *	  A2  = 3*(Y[IP+1]-Y[IP])-2.0*YD[IP]-YD[IP+1]
 *	  A3  = -2.0 * (Y[IP+1]-Y[IP]) + YD[IP] - YD[IP+1]
 *
 *	with the first derivates as
 *	  YD[IP]    = 0.5 * (Y[IP+1] - Y[IP-1]);
 *	  YD[IP+1]  = 0.5 * (Y[IP+2] - Y[IP])
 *
 *	the coefs becomes
 *	  A0  = Y[IP]
 *	  A1  = YD[IP]
 *	      =  0.5 * (Y[IP+1] - Y[IP-1]);
 *	  A2  =  3.0 * (Y[IP+1]-Y[IP])-2.0*YD[IP]-YD[IP+1]
 *	      =  3.0 * (Y[IP+1] - Y[IP]) - 0.5 * 2.0 * (Y[IP+1] - Y[IP-1]) - 0.5 * (Y[IP+2] - Y[IP])
 *	      =  3.0 * Y[IP+1] - 3.0 * Y[IP] - Y[IP+1] + Y[IP-1] - 0.5 * Y[IP+2] + 0.5 * Y[IP]
 *	      = -0.5 * Y[IP+2] + 2.0 * Y[IP+1] - 2.5 * Y[IP] + Y[IP-1]
 *		  = Y[IP-1] + 2 * Y[IP+1] - 0.5 * (5.0 * Y[IP] + Y[IP+2])
 *	  A3  = -2.0 * (Y[IP+1]-Y[IP]) + YD[IP] + YD[IP+1]
 *	      = -2.0 * Y[IP+1] + 2.0 * Y[IP] + 0.5 * (Y[IP+1] - Y[IP-1]) + 0.5 * (Y[IP+2] - Y[IP])
 *	      = -2.0 * Y[IP+1] + 2.0 * Y[IP] + 0.5 * Y[IP+1] - 0.5 * Y[IP-1] + 0.5 * Y[IP+2] - 0.5 * Y[IP]
 *	      =  0.5 * Y[IP+2] - 1.5 * Y[IP+1] + 1.5 * Y[IP] - 0.5 * Y[IP-1]
 *		  =  0.5 * (3.0 * (Y[IP] - Y[IP+1]) - Y[IP-1] + YP[IP+2])
 *
 *	then interpolated data value is (horner rule)
 *	  out = (((A3*x)+A2)*x+A1)*x+A0
 *
 *	this gives parts of data points Y[IP-1] to Y[IP+2] of
 *	  part       x**3    x**2    x**1    x**0
 *	  Y[IP-1]    -0.5     1      -0.5    0
 *	  Y[IP]       1.5    -2.5     0      1
 *	  Y[IP+1]    -1.5     2       0.5    0
 *	  Y[IP+2]     0.5    -0.5     0      0
 * --------------------------------------------------------------------------------------------------
 */
public class CubicSpline
{
	// number of bits used to scale spline coefs
	public  static final int SPLINE_QUANTBITS	= 14;
	private static final int SPLINE_QUANTSCALE	= 1<<SPLINE_QUANTBITS;
	// log2(number) of precalculated splines (range is [4..14])
	private static final int SPLINE_FRACBITS 	= 10;
	private static final int SPLINE_LUTLEN		= 1<<SPLINE_FRACBITS;
	// Shifting of calculated Samples:
	public  static final int SPLINE_FRACSHIFT	= (ModConstants.SHIFT-SPLINE_FRACBITS)-2;
	public  static final int SPLINE_FRACMASK	= ((1<<(ModConstants.SHIFT-SPLINE_FRACSHIFT))-1) & ~3;

	public static final int [] lut = new int [4*SPLINE_LUTLEN]; // prevent a 2 dimensional array...

	static
	{
		initialize();
	}
	/**
	 * Constructor for CubicSpline
	 */
	private CubicSpline()
	{
		super();
	}

	/**
	 * Init the static params
	 * @since 15.06.2006
	 */
	private static void initialize()
	{
		final double len	= 1.0d / SPLINE_LUTLEN;
		final double scale	= SPLINE_QUANTSCALE;

		for(int i=0; i<SPLINE_LUTLEN; i++)
		{
			final double	x		= (i)*len;
			final int 		idx	= i<<2;
			final double	cm1	= Math.floor(0.5 + scale * (-0.5*x*x*x + 1.0 * x*x - 0.5 * x      ));
			final double	c0	= Math.floor(0.5 + scale * ( 1.5*x*x*x - 2.5 * x*x           + 1.0));
			final double	c1	= Math.floor(0.5 + scale * (-1.5*x*x*x + 2.0 * x*x + 0.5 * x      ));
			final double	c2	= Math.floor(0.5 + scale * ( 0.5*x*x*x - 0.5 * x*x                ));
			lut[idx  ]		= (int)((cm1 < -scale) ? -scale : ((cm1 > scale) ? scale : cm1));
			lut[idx+1]		= (int)((c0  < -scale) ? -scale : ((c0  > scale) ? scale : c0 ));
			lut[idx+2]		= (int)((c1  < -scale) ? -scale : ((c1  > scale) ? scale : c1 ));
			lut[idx+3]		= (int)((c2  < -scale) ? -scale : ((c2  > scale) ? scale : c2 ));

			// forces coefs-set to unity gain:
			final int sum	= lut[idx] + lut[idx+1] + lut[idx+2] + lut[idx+3];
			if (sum != SPLINE_QUANTSCALE)
			{
				int max = idx;
				if (lut[idx+1] > lut[max]) max = idx+1;
				if (lut[idx+2] > lut[max]) max = idx+2;
				if (lut[idx+3] > lut[max]) max = idx+3;
				lut[max] += SPLINE_QUANTSCALE - sum;
			}
		}
	}
}
