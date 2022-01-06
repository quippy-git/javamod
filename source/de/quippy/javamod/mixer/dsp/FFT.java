/*
 * @(#) FFT.java
 *
 * Created on 30.09.2007 by Daniel Becker
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
package de.quippy.javamod.mixer.dsp;

/**
 * This is an integer based version, returning float values though
 * @author Daniel Becker
 * @since 30.09.2007
 */
public class FFT
{
	private static final int FRAC_BITS = 16;
	public static final int FRAC_FAC = 1<<FRAC_BITS;
	
	private final long xre[];
	private final long xim[];
	private final float mag[];
	private final long fftSin[];
	private final long fftCos[];
	private final int fftBr[];
	private final int ss;
	private final int ss2;
	private final int nu;

	/**
	 * Constructor for FFT
	 */
	public FFT(int pSampleSize)
	{
		ss = pSampleSize;
		ss2 = ss >> 1;
		
		nu = (int) (Math.log(ss) / Math.log(2.0D));

		xre = new long[ss];
		xim = new long[ss];
		mag = new float[ss2];
		
		fftSin = new long[nu * ss2];
		fftCos = new long[nu * ss2];

		fftBr = new int[ss];

		prepareFFTTables();
	}

	/**
	 * We will here precalculate all sin and cos values
	 * and all other things we can store
	 * @since 03.10.2007
	 */
	private void prepareFFTTables()
	{
		int n2 = ss2;
		int nu1 = nu-1;
		int k = 0;
		int x = 0;
		for (int l=1; l<=nu; l++)
		{
			while (k < ss)
			{
				for (int i=1; i<=n2; i++)
				{
					double p = (double)bitrev(k >> nu1, nu);
					double arg = (Math.PI * p * 2.0D) / (double) ss;
					fftSin[x] = (long) (Math.sin(arg) * FRAC_FAC);
					fftCos[x] = (long) (Math.cos(arg) * FRAC_FAC);
					k++;
					x++;
				}

				k += n2;
			}
			k = 0;
			nu1--;
			n2 >>= 1;
		}

		for (k=0; k<ss; k++)
			fftBr[k] = bitrev(k, nu);
	}
	/**
	 * This will calculate the integer sqrt from a long value
	 * @since 03.10.2007
	 * @param value
	 * @return
	 */
	private static long longSqrt(long value)
	{
		final int scale = 8;
		int bits = 64;
		long sqrt=0;
		long rest=0;
		for (int i=0; i<scale; i++)
		{
			bits -= 8;
			rest = (rest<<8) | ((value>>bits)&0xFF);
			long i2 = (sqrt<<5)+1;
			
			int k0=0;
			while (true)
			{
				long i3 = rest-i2;
				if (i3<0) break;
				
				rest=i3;
				i2+=2;
				k0++;
			}
			sqrt=(sqrt<<4)+k0;
		}
		
		return sqrt;
	}
	private static int bitrev(int j, int nu)
	{
		int j1 = j;
		int k = 0;
		for (int i=1; i<=nu; i++)
		{
			int j2 = j1 >> 1;
			k = ((k << 1) + j1) - (j2 << 1);
			j1 = j2;
		}

		return k;
	}
	/**
	 * The main routine. We expect sampledata with 1.0<=x<=-1.0
	 * The return value (magnitude) will be in the same spectrum.
	 * @since 03.10.2007
	 * @param pSample
	 * @return
	 */
	public float[] calculate(float pSample[])
	{
		final int wAps = pSample.length / ss;
		int n2 = ss2;
//		int nu1 = nu - 1;
		int a = 0;

		for (int b=0; a<pSample.length; b++)
		{
			xre[b] = (long)(pSample[a]*FRAC_FAC);
			xim[b] = 0;
			a += wAps;
		}

		int x = 0;
		for (int l=1; l<=nu; l++)
		{
			for (int k=0; k<ss; k+=n2)
			{
				for (int i=1; i<=n2; i++)
				{
					final long c = fftCos[x];
					final long s = fftSin[x];
					final int kn2 = k + n2;
					final long tr = (xre[kn2]*c + xim[kn2]*s)>>FRAC_BITS;
					final long ti = (xim[kn2]*c - xre[kn2]*s)>>FRAC_BITS;
					xre[kn2] = xre[k] - tr;
					xim[kn2] = xim[k] - ti;
					xre[k] += tr;
					xim[k] += ti;
					k++;
					x++;
				}

			}

//			nu1--;
			n2 >>= 1;
		}

		for (int k=0; k<ss; k++)
		{
			final int r = fftBr[k];
			if (r > k)
			{
				final long tr = xre[k];
				final long ti = xim[k];
				xre[k] = xre[r];
				xim[k] = xim[r];
				xre[r] = tr;
				xim[r] = ti;
			}
		}

		mag[0] = ((float)((longSqrt(xre[0]*xre[0] + xim[0]*xim[0]))>>FRAC_BITS)) / ((float)ss);
		for (int i=1; i<ss2; i++)
			mag[i] = ((float)((longSqrt(xre[i]*xre[i] + xim[i]*xim[i])<<1)>>FRAC_BITS)) / ((float)ss);

		return mag;
	}
}
