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
	// quantizer scale of window coefs
	public  static final int	WFIR_QUANTBITS		=	15;
	private static final int	WFIR_QUANTSCALE		=	1<<WFIR_QUANTBITS;
	// log2(number)-1 of precalculated taps range is [4..12]
	private static final int	WFIR_FRACBITS		=	10;
	private static final int	WFIR_LUTLEN			=	(1<<(WFIR_FRACBITS+1))+1;
	// number of samples in window
	private static final int	WFIR_LOG2WIDTH		=	3;
	private static final int	WFIR_WIDTH			=	1<<WFIR_LOG2WIDTH;
	// wfir types plus default:
	private static final int	WFIR_HANN			=	0;
	private static final int	WFIR_HAMMING		=	1;
	private static final int	WFIR_BLACKMANEXACT	=	2;
	private static final int	WFIR_BLACKMAN3T61	=	3;
	private static final int	WFIR_BLACKMAN3T67	=	4;
	private static final int	WFIR_BLACKMAN4T92	=	5;
	private static final int	WFIR_BLACKMAN4T74	=	6;
	private static final int	WFIR_KAISER4T		=	7;
	// Default settings:
//	private static final float	WFIR_CUTOFF			=	0.90f; // cutoff (1.0 == pi/2)
//	private static final int	WFIR_TYPE			=	WFIR_BLACKMANEXACT;
	private static final float	WFIR_CUTOFF			=	0.97f;
	private static final int	WFIR_TYPE			=	WFIR_KAISER4T;

	private static final double	M_zEPS				=	1e-8;

	// shifting of calculated samples:
	public  static final int	WFIR_FRACSHIFT		=	ModConstants.SHIFT - (WFIR_FRACBITS + 1 + WFIR_LOG2WIDTH);
	public  static final int	WFIR_FRACMASK		=	(((1<<((ModConstants.SHIFT + 1) - WFIR_FRACSHIFT)) - 1) & ~((1<<WFIR_LOG2WIDTH)-1));
	public  static final int	WFIR_FRACHALVE		=	1<<(ModConstants.SHIFT-(WFIR_FRACBITS+2));
	
	public static final int [] lut = new int [WFIR_LUTLEN*WFIR_WIDTH];
	
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
	/**
	 * Get a coeff.
	 * @since 15.06.2006
	 * @param cnr
	 * @param ofs
	 * @param cut
	 * @param width
	 * @param type
	 * @return
	 */
	private static double coef(final int cnr, final double ofs, final double cut, final int width, final int type)
	{
		final double widthM1 = width - 1;
		final double widthM1Half = 0.5d * widthM1;
		final double posU = ((double) cnr) - ofs;
		final double idl = 2.0d * Math.PI / widthM1;

		double pos = posU - widthM1Half;
		double wc, si;
		if (Math.abs(pos) < M_zEPS)
		{
			wc = 1.0;
			si = cut;
		}
		else
		{
			switch (type)
			{
				case WFIR_HANN:
					wc = 0.50 - 0.50 * Math.cos(idl * posU);
					break;
				case WFIR_HAMMING:
					wc = 0.54 - 0.46 * Math.cos(idl * posU);
					break;
				case WFIR_BLACKMANEXACT:
					wc = 0.42 - 0.50 * Math.cos(idl * posU) + 0.08 * Math.cos(2.0 * idl * posU);
					break;
				case WFIR_BLACKMAN3T61:
					wc = 0.44959 - 0.49364 * Math.cos(idl * posU) + 0.05677 * Math.cos(2.0 * idl * posU);
					break;
				case WFIR_BLACKMAN3T67:
					wc = 0.42323 - 0.49755 * Math.cos(idl * posU) + 0.07922 * Math.cos(2.0 * idl * posU);
					break;
				case WFIR_BLACKMAN4T92:
					wc = 0.35875 - 0.48829 * Math.cos(idl * posU) + 0.14128 * Math.cos(2.0 * idl * posU) - 0.01168 * Math.cos(3.0 * idl * posU);
					break;
				case WFIR_BLACKMAN4T74:
					wc = 0.40217 - 0.49703 * Math.cos(idl * posU) + 0.09392 * Math.cos(2.0 * idl * posU) - 0.00183 * Math.cos(3.0 * idl * posU);
					break;
				case WFIR_KAISER4T:
					wc = 0.40243 - 0.49804 * Math.cos(idl * posU) + 0.09831 * Math.cos(2.0 * idl * posU) - 0.00122 * Math.cos(3.0 * idl * posU);
					break;
				default:
					wc = 1.0;
					break;
			}
			pos *= Math.PI;
			si = Math.sin(cut * pos) / pos;
		}
		return wc * si;
	}
	/**
	 * Init the static params
	 * @since 15.06.2006
	 */
	private static void initialize()
	{
		final double cllen	= (double)(1L<<WFIR_FRACBITS);	// number of precalculated lines for 0..1 (-1..0)
		final double norm	= 1.0d / (double)(2.0d * cllen);
		final double cut	= WFIR_CUTOFF;
		final double scale	= (double)WFIR_QUANTSCALE;
		
		for (int cl=0; cl<WFIR_LUTLEN; cl++)
		{	
			final double [] coefs	= new double [WFIR_WIDTH];
			final double ofs		= ((double)cl-cllen)*norm;
			final int idx			= cl<<WFIR_LOG2WIDTH;
			
			double gain = 0.0d;
			for (int c=0; c<WFIR_WIDTH; c++)
				gain += (coefs[c] = coef(c, ofs, cut, WFIR_WIDTH, WFIR_TYPE));
			
			gain = 1.0d / gain;
			for (int c=0; c<WFIR_WIDTH; c++)
			{	
				final double coef = Math.floor( 0.5d + scale*coefs[c]*gain );
				lut[idx+c] = (int)( (coef<-scale)?-scale:((coef>scale)?scale:coef) );
			}
		}
	}
}
