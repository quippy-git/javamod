/*
 * @(#) Dither.java
 * Created on 23.05.2020 by Daniel Becker
 * -----------------------------------------------------------------------
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * ----------------------------------------------------------------------
 */
package de.quippy.javamod.mixer.dsp.iir.filter;

/**
 * @author Daniel Becker
 * @since 23.05.2020
 */
public class Dither
{
	private double firstOrder[] =
	{
		0.0D, 1.0D
	};
	private double secondOrder[] =
	{
		0.0D, 2D, -1D
	};
	private double psychAccoust3[] =
	{
		0.0D, 1.623D, -0.98199999999999998D, 0.109D
	};
	private double psychAccoust5[] =
	{
		0.0D, 2.0329999999999999D, -2.165D, 1.9590000000000001D, -1.5900000000000001D, 0.6149D
	};
	private double psychAccoust9[] =
	{
		0.0D, 2.4119999999999999D, -3.3700000000000001D, 3.9369999999999998D, -4.1740000000000004D, 3.3530000000000002D, -2.2050000000000001D, 1.2809999999999999D, -0.56899999999999995D, 0.084699999999999998D
	};
	public static enum FilterType {
		FirstOrder, SecondOrder, Psychoacoustic3, Psychoacoustic5, Psychoacoustic9
	}
	public static String [] FilterTypeNames = {
		"First Oder", "Second Order", "Psychoacoustic 3", "Psychoacoustic 5", "Psychoacoustic 9",  
	};
	
	public static enum DitherType {
		Rectangular, Triangular, HighPass
	}
	public static String[] DitherTypeNames = {
		"Rectangular", "Triangular", "High-Pass"
	};
	
	private int mChannels = 0;
	private double noiseShapeFilter[] = null;
	private double scalarProduct[] = null;
	private double oldSamples[][] = null;
	private double qt = 0;
	private boolean mByPass = false;
	private int toNumberBits = 0;
	private int mDitherType = 0;
	private boolean mWithNoiseShaping = true;
	private boolean mWithDither = true;
	private double randValue[] = null;

	/**
	 * Constructor for Dither
	 */
	public Dither()
	{
		noiseShapeFilter = psychAccoust9;
		mByPass = false;
		mDitherType = 0;
		mWithNoiseShaping = true;
		mWithDither = true;
	}
	public Dither(int channels, int toBits, int filterType, int ditherType, boolean byPassDither)
	{
		this();
		mChannels = channels;
		toNumberBits = toBits;
		mDitherType = ditherType;
		mByPass = byPassDither;
		setFilterType(filterType);
	}

	public void setAnzChannels(int i)
	{
		mChannels = i;
	}
	
	public void setSampleSizeInBits(int i)
	{
		toNumberBits = i;
		cleanStateCoefficients();
	}

	/**
	 * @since 23.05.2020
	 * @param i Values from 0 to 4
	 */
	public void setFilterType(int i)
	{
		switch (i)
		{
			case 0: noiseShapeFilter = firstOrder; break; 
			case 1: noiseShapeFilter = secondOrder; break; 
			case 2: noiseShapeFilter = psychAccoust3; break;
			case 3: noiseShapeFilter = psychAccoust5; break;
			default:
			case 4: noiseShapeFilter = psychAccoust9; break; 
		}
		cleanStateCoefficients();
	}

	/**
	 * @since 23.05.2020
	 * @param i Values from 0 to 2
	 */
	public void setDitherType(int i)
	{
		mDitherType = i;
		cleanStateCoefficients();
	}
	public void setWithDither(boolean withDither)
	{
		mWithDither = withDither;
	}
	public void setWithNoiseShaping(boolean withNoiseShaping)
	{
		mWithNoiseShaping = withNoiseShaping;
	}
	public void setBypass(boolean byPass)
	{
		mByPass = byPass;
	}
	public void cleanStateCoefficients()
	{
		randValue = new double[mChannels];
		scalarProduct = new double[mChannels];
		final int filterLength = noiseShapeFilter.length;
		oldSamples = new double[mChannels][filterLength];

		for (int i=0; i<mChannels; i++)
		{
			randValue[i] = 2D * Math.random() - 1.0D;
			scalarProduct[i] = 0.0D;
			for (int j = 0; j < filterLength; j++)
				oldSamples[i][j] = 0.0D;
		}

		qt = Math.pow(2D, 1 - toNumberBits);
	}

	public void byPassAlgorithm(boolean flag)
	{
		mByPass = flag;
	}

	public void useDither(boolean flag)
	{
		mWithDither = flag;
		for (int i=0; i<mChannels; i++)
		{
			scalarProduct[i] = 0.0D;
		}
	}

	public void useNoiseShaping(boolean flag)
	{
		mWithNoiseShaping = flag;
		cleanStateCoefficients();
	}

	public void numberChannels(int i)
	{
		mChannels = i;
	}

	final double scalarProduct(double ad[], double ad1[])
	{
		double d = 0.0D;
		int i = ad.length;
		int j = ad1.length;
		if (i != j) return 0.0D;
		for (int k = 0; k < i; k++)
			d += ad[k] * ad1[k];

		return d;
	}

	/**
 	 * Shift all entries of given Array one to the right
	 * set new value at position 0
	 * @since 30.05.2020
	 * @param ad
	 * @param newValue
	 */
	final void shiftAndSet(double ad[], double newValue)
	{
		int lastIndex = ad.length - 1;
		for (int k = lastIndex; k > 0; k--) ad[k] = ad[k-1];
		ad[0] = newValue;
	}

	/**
	 * Dither given sample to defined sample size for channel
	 * @since 30.05.2020
	 * @param sample
	 * @param forChannel
	 * @return
	 */
	public final double process(double sample, int forChannel)
	{
		scalarProduct[forChannel] = scalarProduct(noiseShapeFilter, oldSamples[forChannel]);

		final double sampleNoiseShaped = (mWithNoiseShaping)? sample - scalarProduct[forChannel] : sample;

		final double sampleDitherd;
		if (mWithDither)
		{
			final double rndOne = randValue[forChannel] = 2D * Math.random() - 1.0D;
			final double ditheredSample;
			switch (mDitherType)
			{
				case 0:
					ditheredSample = (rndOne * qt) / 2D;
					break;
				case 1:
					final double rndTwo = 2D * Math.random() - 1.0D;
					ditheredSample = ((rndOne + rndTwo) * qt) / 2D;
					break;
				case 2:
				default:
					ditheredSample = ((rndOne - randValue[forChannel]) * qt) / 2D;
					break;
			}
			sampleDitherd = Math.floor(((sampleNoiseShaped + ditheredSample) / qt) + 0.5D) * qt;
		}
		else
			sampleDitherd = Math.floor((sampleNoiseShaped / qt) + 0.5D) * qt;
		
		shiftAndSet(oldSamples[forChannel], sampleDitherd - sampleNoiseShaped);

		if (mByPass)
			return sample;
		else
			return sampleDitherd;
	}
}