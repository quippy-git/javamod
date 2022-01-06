/*
 * @(#) FFT2.java
 * 
 * Created on 21.01.2012 by Daniel Becker
 * 
 * -----------------------------------------------------------------------
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * ----------------------------------------------------------------------
 */
package de.quippy.javamod.mixer.dsp;

import de.quippy.javamod.system.FastMath;

/**
 * This class will be used for audio effects (pitch shift)
 * @author Daniel Becker
 * @since 21.01.2012
 */
public class FFT2
{
	public static final int FORWARD = -1;
	public static final int REVERSE = 1;
	private int frameSize = 0;
	private int bits = 0;
	private int flip[] = null;

	public FFT2(int frameSize)
	{
		this.frameSize = frameSize;
		bits = (int) (Math.log(frameSize) / Math.log(2D));
		createBitFlipArray();
	}

	private void createBitFlipArray()
	{
		flip = new int[frameSize];
		for (int i = 1; i < frameSize - 1; i++)
		{
			int j = 0;
			for (int bitm = 1; bitm < frameSize; bitm <<= 1)
			{
				if ((i & bitm) != 0) j++;
				j <<= 1;
			}

			flip[i] = j / 2;
		}
	}

	public void smsFft(float fftBuffer[], int sign)
	{
		if (sign != FORWARD && sign != REVERSE) throw new IllegalArgumentException("invalid sign: " + sign);
		for (int i = 1; i < frameSize - 1; i++)
		{
			final int j = flip[i];
			if (i < j)
			{
				int i2 = i<<1;
				int j2 = j<<1;
				float temp = fftBuffer[i2];
				fftBuffer[i2] = fftBuffer[j2];
				fftBuffer[j2] = temp;
				i2++; j2++;
				temp = fftBuffer[i2];
				fftBuffer[i2] = fftBuffer[j2];
				fftBuffer[j2] = temp;
			}
		}

		int k = 0;
		int le = 2;
		for (; k < bits; k++)
		{
			le <<= 1;
			final int le2 = le >> 1;
			float ur = 1.0f;
			float ui = 0.0f;
			final float arg = (float) Math.PI / (float) (le2 >> 1);
			final float wr = (float) FastMath.fastCos(arg);
			final float wi = (float) sign * (float) FastMath.fastSin(arg);
//			int idx = 0;
			for (int j = 0; j < le2; j += 2)
			{
				int p1r = j;
				int p1i = p1r + 1;
				int p2r = p1r + le2;
				int p2i = p2r + 1;
				for (int i = j; i<frameSize<<1; i += le)
				{
					float tr = fftBuffer[p2r] * ur - fftBuffer[p2i] * ui;
					float ti = fftBuffer[p2r] * ui + fftBuffer[p2i] * ur;
//					idx++;
					fftBuffer[p2r] = fftBuffer[p1r] - tr;
					fftBuffer[p2i] = fftBuffer[p1i] - ti;
					fftBuffer[p1r] += tr;
					fftBuffer[p1i] += ti;
					p1r += le;
					p1i += le;
					p2r += le;
					p2i += le;
				}

				final float tr = ur * wr - ui * wi;
				ui = ur * wi + ui * wr;
				ur = tr;
			}
		}
	}
}
