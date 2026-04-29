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

import java.util.Random;

/**
 * Optimierte Dither-Klasse mit Integer-Arithmetik (Fixed-Point).
 * Erwartet und liefert 32-Bit Signed Integer Samples.
 */
/**
 * Optimized version of a dithering based in integer arithmetic (fixed point)
 * expects 32 bit signed interger samples - uses long, because samples need not
 * to be clipped yet.
 * Result after "process" is always in target bit range. With byPassDither simply no
 * noise is added.
 * @author Daniel Becker
 * @since 29.04.2026
 */
public class Dither
{
	// Fixpunkt-Faktor: 2^14 = 16384 (genug für die Koeffizienten-Präzision)
	private static final int SHIFT = 14;
	private static final int SCALE = 1 << SHIFT;
	private static final int HALFSCALE = 1 << (SHIFT - 1);

	// static scaled filter coefficients based on SHIFT (Q14)
	private final int[] firstOrder =
	{
		0, 16384
	};
	private final int[] secondOrder =
	{
		0, 32768, -16384
	};
	private final int[] psychAccoust3 =
	{
		0, 26591, -16088, 1786
	};
	private final int[] psychAccoust5 =
	{
		0, 33309, -35470, 32096, -26050, 10075
	};
	private final int[] psychAccoust9 =
	{
		0, 39518, -55213, 64504, -68386, 54936, -36126, 20988, -9321, 1388
	};

	public static String[] FilterTypeNames =
	{
		"First Oder", "Second Order", "Psychoacoustic 3", "Psychoacoustic 5", "Psychoacoustic 9",
	};
	public static String[] DitherTypeNames =
	{
		"Rectangular", "Triangular", "High-Pass"
	};

	private long[][] oldSamples = null;
	private int[] lastRand = null;
	private final Random random = new Random();

	private int[] noiseShapeFilter = psychAccoust9;
	private int ditherType = 2;
	private int channels = 0;
	private boolean withNoiseShaping = true;
	private boolean withDither = true;
	private boolean byPassDither = false;

	// quantifying bit shifts - will be set with "setSampleSizeInBits" - and only there!
	private int qShift = 0;
	private long halfStep = 0;

	/**
	 * Constructor for Dither
	 */
	public Dither(final int channels, final int toBits, final int filterType, final int ditherType, final boolean byPassDither)
	{
		this.ditherType = ditherType;
		this.byPassDither = byPassDither;
		setFilterType(filterType);
		setSampleSizeInBits(toBits);
		setAnzChannels(channels); // also does "cleanStateCoefficients()"
	}

	public void setFilterType(final int i)
	{
		switch (i)
		{
			case 0:
				noiseShapeFilter = firstOrder;
				break;
			case 1:
				noiseShapeFilter = secondOrder;
				break;
			case 2:
				noiseShapeFilter = psychAccoust3;
				break;
			case 3:
				noiseShapeFilter = psychAccoust5;
				break;
			default:
			case 4:
				noiseShapeFilter = psychAccoust9;
				break;
		}
	}
	public void setDitherType(final int newDitherType)
	{
		ditherType = newDitherType;
	}
	public void setAnzChannels(final int newAnzChannels)
	{
		channels = newAnzChannels;
		cleanStateCoefficients();
	}
	public void setSampleSizeInBits(final int bits)
	{
		// how many bits to throw away
		this.qShift = 32 - bits;
		this.halfStep = 1 << (qShift - 1);
	}
	public void setWithDither(final boolean newWithDither)
	{
		withDither = newWithDither;
	}
	public void setWithNoiseShaping(final boolean newWithNoiseShaping)
	{
		withNoiseShaping = newWithNoiseShaping;
	}
	public void setBypass(final boolean newByPass)
	{
		byPassDither = newByPass;
	}
	private void cleanStateCoefficients()
	{
		if (channels <= 0) return;
		lastRand = new int[channels];
		int filterLength = noiseShapeFilter.length;
		oldSamples = new long[channels][filterLength];

		for (int i = 0; i < channels; i++)
		{
			lastRand[i] = random.nextInt(SCALE) - (HALFSCALE); // somewhat -0.5..0.5 but quantified
			for (int j = 0; j < filterLength; j++)
				oldSamples[i][j] = 0;
		}
	}
	/**
	 * Shift all entries of given Array one to the right
	 * set new value at position 0
	 */
	private void shiftAndSet(final long[] ad, final long newValue)
	{
		for (int k = ad.length - 1; k > 0; k--)
			ad[k] = ad[k - 1];
		ad[0] = newValue;
	}
	/**
	 * Core-logik in integer arithmetic
	 * @since 29.04.2026
	 * @param sample 32-Bit Signed Integer (yet unclipped, so data type is long)
	 * @param ch the channel to use
	 * @return the sample in target bit depth (plus dither or without)
	 */
	public final long process(final long sample, final int ch)
	{
		if (byPassDither) return (sample + halfStep) / (1<<qShift);

		// 1. Noise Shaping (Error Feedback)
		long errorPrediction = 0;
		if (withNoiseShaping)
		{
			final long[] oldSamplesChannel = oldSamples[ch];
			for (int i=0; i<noiseShapeFilter.length; i++)
				errorPrediction += (long)noiseShapeFilter[i] * oldSamplesChannel[i];
			errorPrediction /= (1<<SHIFT);
		}

		final long sampleNoiseShaped = sample - errorPrediction;

		// 2. Dithering
		long ditherValue = 0;
		if (withDither)
		{
			int r1 = random.nextInt(SCALE) - (HALFSCALE); // -8192 bis 8191
			switch (ditherType)
			{
				case 0: // Rectangular
					ditherValue = r1;
					break;
				case 1: // Triangular
					int r2 = random.nextInt(SCALE) - (HALFSCALE);
					ditherValue = r1 + r2;
					break;
				default:
				case 2: // High Pass
					ditherValue = r1 - lastRand[ch];
					lastRand[ch] = r1;
					break;
			}
			// scale dither to target bits
			ditherValue = (ditherValue << qShift) / (1<<SHIFT);
		}

		// 3. quantization (rounding via "halfStep")
	    final long inputToQuantizer = sampleNoiseShaped + ditherValue;
	    // 4. quantize for output plus rounding
		final long quantized = (inputToQuantizer + halfStep) / (1<<qShift);

		// 5. Now store the error, which is the sub sample part (rest) that was
		// lost in the target sample
		shiftAndSet(oldSamples[ch], (quantized << qShift) - inputToQuantizer);

		return quantized;
	}
}