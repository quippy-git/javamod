/*
 * @(#) PitchShift.java
 *
 * Created on 21.01.2012 by Daniel Becker
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
package de.quippy.javamod.mixer.dsp.pitchshift;

import java.util.Arrays;

import javax.sound.sampled.AudioFormat;

import de.quippy.javamod.mixer.dsp.DSPEffekt;
import de.quippy.javamod.mixer.dsp.FFT2;
import de.quippy.javamod.system.FastMath;

/**
 * NAME: smbPitchShift.cpp
 * VERSION: 1.2
 * HOME URL: http://www.dspdimension.com
 * KNOWN BUGS: none
 *
 *
 * COPYRIGHT 1999-2006 Stephan M. Bernsee <smb [AT] dspdimension [DOT] com>
 *
 *                                              The Wide Open License (WOL)
 *
 * Permission to use, copy, modify, distribute and sell this software and its
 * documentation for any purpose is hereby granted without fee, provided that
 * the above copyright notice and this license appear in all source copies. 
 * THIS SOFTWARE IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY OF
 * ANY KIND. See http://www.dspguru.com/wol.htm for more information.
 *
 * @author Daniel Becker
 * @since 21.01.2012
 */
public class PitchShift implements DSPEffekt
{
	private static final int MAXFIFO = 2;
	
	private float gInFIFO[][] = null;
	private float gOutFIFO[][] = null;
	private float stretchFIFO[][] = null;
	private float gFFTworksp[] = null;
	private float gLastPhase[][] = null;
	private float gSumPhase[][] = null;
	private float gOutputAccum[][] = null;
	private float gAnaFreq[] = null;
	private float gAnaMagn[] = null;
	private float gSynFreq[] = null;
	private float gSynMagn[] = null;
	private float gWindow[] = null;
	private float gWindow2[] = null;
	private float gWindow3[] = null;
	private float outBuffer[] = null;
	private FFT2 fft = null;
	private int gRover;
	private float pitchScale;
	private float sampleScale; 
	private int fftFrameSize;
	private int osamp;
	private float sampleRate;
	private int fftFrameSize2;
	private int stepSize;
	private float freqPerBin;
	private float expct;
	private float expct2;
	private float inFifoLatency;

	private boolean isActive;
    private int sampleBufferSize;
    private int channels;

	/**
	 * Constructor for PitchShift
	 */
	public PitchShift(float pitchScale, float sampleScale, int fftFrameSize, int osamp)
	{
		this.pitchScale = pitchScale;
		this.sampleScale = sampleScale;
		this.fftFrameSize = fftFrameSize;
		this.osamp = osamp;
		this.isActive = false;
	}
	public PitchShift()
	{
		this(1.0f, 1.0f, 4096, 32);
	}
	/**
	 * @param audioFormat
	 * @param sampleBufferLength
	 * @see de.quippy.javamod.mixer.dsp.DSPEffekt#initialize(javax.sound.sampled.AudioFormat, int)
	 */
	public void initialize(AudioFormat audioFormat, int sampleBufferLength)
	{
		sampleBufferSize = sampleBufferLength;
		sampleRate = audioFormat.getSampleRate();
		channels = audioFormat.getChannels();
		outBuffer = new float[sampleBufferSize];
		changeFFTFrameSize(fftFrameSize);
	}
	/**
	 * @param active
	 * @see de.quippy.javamod.mixer.dsp.DSPEffekt#setIsActive(boolean)
	 */
	public void setIsActive(boolean active)
	{
		isActive = active;
	}
	/**
	 * @return
	 * @see de.quippy.javamod.mixer.dsp.DSPEffekt#isActive()
	 */
	public boolean isActive()
	{
		return isActive;
	}
	/**
	 * @return the pitchScale
	 */
	public synchronized float getPitchScale()
	{
		return pitchScale;
	}
	public synchronized void setPitchScale(float pitchScale)
	{
		this.pitchScale = pitchScale;
	}
	/**
	 * @return the pitchScale
	 */
	public synchronized float getSampleScale()
	{
		return sampleScale;
	}
	public synchronized void setSampleScale(float sampleScale)
	{
		this.sampleScale = sampleScale;
	}
	/**
	 * @since 22.01.2012
	 * @param pitchScale
	 * @param sampleScale
	 */
	public synchronized void setPitchAndSampleScale(float pitchScale, float sampleScale)
	{
		setPitchScale(pitchScale);
		setSampleScale(sampleScale);
	}
	/**
	 * @return the fftFrameSize
	 */
	public int getFftFrameSize()
	{
		return fftFrameSize;
	}
	public synchronized void setFFTFrameSize(int fftFrameSize)
	{
		changeFFTFrameSize(fftFrameSize);
	}
	/**
	 * @return the osamp
	 */
	public synchronized int getFFTOversampling()
	{
		return osamp;
	}
	public synchronized void setFFTOversampling(int osamp)
	{
		changeFFTOversampling(osamp);
	}
	private void changeFFTFrameSize(int newFFTFrameSize)
	{
		fftFrameSize = newFFTFrameSize;
		fftFrameSize2 = fftFrameSize>>1;
		stepSize = fftFrameSize / osamp;
		freqPerBin = sampleRate / (float)fftFrameSize;
		expct = ((float)(2.0*Math.PI) * (float)stepSize) / (float)fftFrameSize;
		expct2 = (float)(2.0*Math.PI) / (float)osamp;
		inFifoLatency = fftFrameSize - stepSize;
		gRover = (int)inFifoLatency;
		fft = new FFT2(fftFrameSize);
		
		stretchFIFO = new float[channels][MAXFIFO];
		gInFIFO = new float[channels][fftFrameSize];
		gOutFIFO = new float[channels][fftFrameSize];
		gFFTworksp = new float[fftFrameSize<<1];
		gLastPhase = new float[channels][fftFrameSize>>1];
		gSumPhase = new float[channels][fftFrameSize>>1];
		gOutputAccum = new float[channels][fftFrameSize<<1];
		gAnaFreq = new float[fftFrameSize];
		gAnaMagn = new float[fftFrameSize];
		gSynFreq = new float[fftFrameSize];
		gSynMagn = new float[fftFrameSize];
		gWindow = new float[fftFrameSize];
		gWindow2 = new float[fftFrameSize];
		gWindow3 = new float[fftFrameSize2];
		Arrays.fill(gAnaFreq, 0.0F);
		Arrays.fill(gAnaMagn, 0.0F);
		Arrays.fill(gSynFreq, 0.0F);
		Arrays.fill(gSynMagn, 0.0F);
		Arrays.fill(gFFTworksp, 0.0F);
		for (int c=0; c<channels; c++)
		{
			Arrays.fill(gInFIFO[c], 0.0F);
			Arrays.fill(gOutFIFO[c], 0.0F);
			Arrays.fill(gOutputAccum[c], 0.0F);
			Arrays.fill(gLastPhase[c], 0.0F);
			Arrays.fill(gSumPhase[c], 0.0F);
		}
		computeWindow();
	}
	private void changeFFTOversampling(int newOverSampling)
	{
		osamp = newOverSampling;
		stepSize = fftFrameSize / osamp;
		expct = ((float)(2.0*Math.PI) * (float)stepSize) / (float)fftFrameSize;
		inFifoLatency = fftFrameSize - stepSize;
		gRover = (int)inFifoLatency;
	}
	private void processFrame(int c)
	{
		windowAndInterleave(c);
		analyze(c);
		process();
		synthesize(c);
		windowAndAccumulate(c);
		System.arraycopy(gOutputAccum[c], 0, gOutFIFO[c], 0, stepSize);
		System.arraycopy(gOutputAccum[c], stepSize, gOutputAccum[c], 0, fftFrameSize);
		System.arraycopy(gInFIFO[c], stepSize, gInFIFO[c], 0, (int)inFifoLatency);
	}
	private void computeWindow()
	{
		for (int k = 0; k < fftFrameSize; k++)
		{
			gWindow[k] = -0.5f * (float)Math.cos((2.0 * Math.PI * (float)k) / (float)fftFrameSize) + 0.5f;
			gWindow2[k] = (2f * gWindow[k]) / (float)(fftFrameSize2 * osamp);
		}
		for (int k = 0; k < fftFrameSize2; k++)
		{
			gWindow3[k] = (float)k * expct;
		}
	}
	private void windowAndInterleave(int c)
	{
		for (int k = 0; k < fftFrameSize; k++)
		{
			gFFTworksp[k<<1] = gInFIFO[c][k] * gWindow[k];
			gFFTworksp[(k<<1) + 1] = 0;
		}
	}
	private void analyze(int c)
	{
		fft.smsFft(gFFTworksp, FFT2.FORWARD);
		for (int k = 0; k < fftFrameSize2; k++)
		{
			final float real = gFFTworksp[k<<1];
			final float imag = gFFTworksp[(k<<1) + 1];
			final float magn = 2f * (float)FastMath.sqrt(real * real + imag * imag);
			final float phase = (float)FastMath.atan2(imag, real);
			float tmp = phase - gLastPhase[c][k];
			gLastPhase[c][k] = phase;
			tmp -= k * expct;
			int qpd = (int)(tmp / Math.PI);
			if (qpd >= 0)
				qpd += qpd & 1;
			else
				qpd -= qpd & 1;
			tmp -= (float)Math.PI * (float)qpd;
			tmp = ((float)osamp * tmp) / (float)(2.0*Math.PI);
			tmp = (float)k * freqPerBin + tmp * freqPerBin;
			gAnaMagn[k] = magn;
			gAnaFreq[k] = tmp;
		}
	}

	private void process()
	{
		Arrays.fill(gSynMagn, 0, fftFrameSize, 0.0f);
		//Arrays.fill(gSynFreq[c], 0, fftFrameSize, 0.0f);
		for (int k = 0; k <= fftFrameSize2; k++)
		{
			final int index = (int)((float)k / pitchScale);
			if (index > fftFrameSize2) continue;
			if (gAnaMagn[index] > gSynMagn[k])
			{
				gSynMagn[k] = gAnaMagn[index];
				gSynFreq[k] = gAnaFreq[index] * pitchScale;
			}
			if (k > 0 && gSynFreq[k] == 0.0f)
			{
				gSynFreq[k] = gSynFreq[k - 1];
				gSynMagn[k] = gSynMagn[k - 1];
			}
		}
	}

	private void synthesize(int c)
	{
		for (int k = 0; k < fftFrameSize2; k++)
		{
			final float magn = gSynMagn[k];
			float tmp = gSynFreq[k];
			tmp = (tmp - (float)k * freqPerBin) / freqPerBin * expct2 + gWindow3[k];
			final float phase = (gSumPhase[c][k] += tmp);
			gFFTworksp[k<<1] = magn * (float)FastMath.fastCos(phase);
			gFFTworksp[(k<<1)+ 1] = magn * (float)FastMath.fastSin(phase);
		}

		Arrays.fill(gFFTworksp, fftFrameSize + 2, fftFrameSize<<1, 0L);
		fft.smsFft(gFFTworksp, FFT2.REVERSE);
	}

	private void windowAndAccumulate(int c)
	{
		for (int k = 0; k < fftFrameSize; k++)
			gOutputAccum[c][k] += gWindow2[k] * gFFTworksp[k<<1];
	}
	private float getSampleFrom(int channel, final float[] buffer, float index, float scale)
	{
		final float b[] = stretchFIFO[channel];
		final float s1 = b[0];
		final float s2 = b[1];
		final float sIndex = index + scale;
		final float steigung = sIndex - FastMath.floor(index);
		final int skipSamples = (int)sIndex - (int)(index);
		if (skipSamples > 0)
		{
			b[0] = b[1];
			//System.arraycopy(b, 1, b, 0, MAXFIFO - 1);
			b[1] = buffer[(((int)sIndex * channels) + channel) % sampleBufferSize];
		}
		return s1 + ((s2-s1)*steigung);
	}
	/**
	 * @param buffer
	 * @param start
	 * @param length
	 * @see de.quippy.javamod.mixer.dsp.DSPEffekt#doEffekt(float[], int, int)
	 */
	public synchronized int doEffekt(final float[] ringBuffer, final int start, final int length)
	{
		if (!isActive) return length;
		
		if (gRover == 0) gRover = (int)inFifoLatency;
		final float end = (float)(start + length) / (float)channels;
		float index = (float)start / (float)channels;
		int sampleIndex = 0;
		while (index < end)
		{
			for (int c=0; c<channels; c++)
			{
				gInFIFO[c][gRover] = getSampleFrom(c, ringBuffer, index, sampleScale);
				outBuffer[sampleIndex++] = gOutFIFO[c][gRover - (int)inFifoLatency];
			}
			index += sampleScale;
			gRover++;
			if (gRover >= fftFrameSize)
			{
				gRover = (int)inFifoLatency;
				for (int c=0; c<channels; c++) processFrame(c);
			}
		}
		int samples = sampleIndex;
		int targetBufferEnd = start + samples;
		if (targetBufferEnd >= sampleBufferSize)
		{
			targetBufferEnd -= sampleBufferSize;
			samples-=targetBufferEnd;
			System.arraycopy(outBuffer, samples, ringBuffer, 0, targetBufferEnd);
		}
		System.arraycopy(outBuffer, 0, ringBuffer, start, samples);
		return sampleIndex;
	}
}
