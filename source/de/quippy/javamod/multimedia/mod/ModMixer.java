/*
 * @(#) ModMixer.java
 * 
 * Created on 30.04.2006 by Daniel Becker
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
package de.quippy.javamod.multimedia.mod;

import javax.sound.sampled.AudioFormat;

import de.quippy.javamod.mixer.BasicMixer;
import de.quippy.javamod.mixer.dsp.iir.filter.Dither;
import de.quippy.javamod.multimedia.mod.loader.Module;
import de.quippy.javamod.multimedia.mod.mixer.BasicModMixer;

/**
 * @author Daniel Becker
 * @since 30.04.2006
 */
public class ModMixer extends BasicMixer
{
	private final Module mod;
	private final BasicModMixer modMixer;
	
	private int bufferSize, outputBufferSize;
	private int sampleSizeInBits;
	private int channels;
	private int sampleRate;
	private int msBufferSize;
	private int maxNNAChannels;
	private boolean doWideStereoMix;
	private boolean doNoiseReduction;
	private boolean doMegaBass;

	// Wide Stereo Vars
	private int maxWideStereo;
	private long[] wideLBuffer;
	private long[] wideRBuffer;
	private int readPointer;
	private int writePointer;
	
	// Bass Expansion: low-pass filter
	private int nXBassSum;
	private int nXBassBufferPos;
	private int nXBassDlyPos;
	private int nXBassMask;
	private int nXBassDepth;
	private long [] XBassBuffer;
	private long [] XBassDelay;	

	// The mixing buffers
	private long [] LBuffer;
	private long [] RBuffer;
	private byte [] output;
	
	// Dithering
	private Dither dither;
	private int ditherFilterType;
	private int ditherType;
	private boolean ditherByPass;

	// Mixing variables
	private int rounds;
	private int shift;
	private long maximum;
	private long minimum;
	
	private long currentSamplesWritten;
	
	/**
	 * Constructor for ModMixer
	 */
	public ModMixer(final Module mod, final int sampleSizeInBits, final int channels, final int sampleRate, final int doISP, final boolean doWideStereoMix, final boolean doNoiseReduction, final boolean doMegaBass, final int doNoLoops, final int maxNNAChannels, final int msBufferSize, final int ditherFilter, final int ditherType, final boolean ditherByPass)
	{
		super();
		this.mod = mod;
		this.sampleSizeInBits=sampleSizeInBits;
		this.channels=channels;
		this.sampleRate=sampleRate;
		this.doWideStereoMix = (channels<2)?false:doWideStereoMix;
		this.doNoiseReduction = doNoiseReduction;
		this.doMegaBass = doMegaBass;
		this.msBufferSize = msBufferSize;
		this.ditherFilterType = ditherFilter;
		this.ditherType = ditherType;
		this.ditherByPass = ditherByPass;
		this.maxNNAChannels = maxNNAChannels;
		modMixer = this.mod.getModMixer(sampleRate, doISP, doNoLoops, maxNNAChannels);
	}
	private void initialize()
	{
		// create the mixing buffers.
		bufferSize = (msBufferSize * sampleRate + 500) / 1000;
		LBuffer = new long[bufferSize];
		RBuffer = new long[bufferSize];

		// For the DSP-Output
		outputBufferSize = bufferSize*channels; // For each channel!

		// Now for the bits (linebuffer):
		final int bytesPerSample = sampleSizeInBits>>3; // DIV 8;
		outputBufferSize *= bytesPerSample;
		output = new byte[outputBufferSize];
		
		// initialize the wide stereo mix
		maxWideStereo = sampleRate / 50;
		wideLBuffer = new long[maxWideStereo];
		wideRBuffer = new long[maxWideStereo];
		readPointer = 0;
		writePointer=maxWideStereo-1;
		
		// initialize the dithering for lower sample rates
		// always for maximum channels
		dither = new Dither(2, sampleSizeInBits, ditherFilterType, ditherType, ditherByPass);

		// Clipping and shifting samples to target buffer
		rounds = sampleSizeInBits >> 3;
		shift = 32 - sampleSizeInBits;
		maximum = ModConstants.CLIPP32BIT_MAX >> shift;
		minimum = ModConstants.CLIPP32BIT_MIN >> shift;
		
		initMegaBass();
		
		setAudioFormat(new AudioFormat(sampleRate, sampleSizeInBits, channels, true, false)); // signed, little endian
	}
	private void initMegaBass()
	{
		int nXBassSamples = (sampleRate * ModConstants.XBASS_DELAY) / 10000;
		if (nXBassSamples > ModConstants.XBASS_BUFFER) nXBassSamples = ModConstants.XBASS_BUFFER;
		int mask = 2;
		while (mask <= nXBassSamples) mask <<= 1;
		
		XBassBuffer = new long[ModConstants.XBASS_BUFFER];
		XBassDelay = new long[ModConstants.XBASS_BUFFER];
		nXBassMask = ((mask >> 1) - 1);
		nXBassSum = 0;
		nXBassBufferPos = 0;
		nXBassDlyPos = 0;
		nXBassDepth = 6;
	}
	/**
	 * @param doNoiseReduction The doNoiseReduction to set.
	 */
	public void setDoNoiseReduction(final boolean doNoiseReduction)
	{
		this.doNoiseReduction = doNoiseReduction;
	}
	/**
	 * @param doWideStereoMix The doWideStereoMix to set.
	 */
	public void setDoWideStereoMix(final boolean doWideStereoMix)
	{
		this.doWideStereoMix = doWideStereoMix;
	}
	/**
	 * @param doMegaBass The doMegaBass to set.
	 */
	public void setDoMegaBass(final boolean doMegaBass)
	{
		this.doMegaBass = doMegaBass;
	}
	/**
	 * @param doNoLoops the loop to set
	 */
	public void setDoNoLoops(final int doNoLoops)
	{
		modMixer.changeDoNoLoops(doNoLoops);
	}
	/**
	 * @param doISP The doISP to set.
	 */
	public void setDoISP(final int doISP)
	{
		modMixer.changeISP(doISP);
	}
	/**
	 * @param myBufferSize
	 */
	public void setBufferSize(final int msBufferSize)
	{
		final int oldMsBufferSize = this.msBufferSize;

		final boolean wasPlaying = !isPaused();
		if (wasPlaying) pausePlayback();

		this.msBufferSize = msBufferSize;
		if (wasPlaying)
		{
			initialize();
			openAudioDevice();
			if (!isInitialized())
			{
				this.msBufferSize = oldMsBufferSize;
				initialize();
				openAudioDevice();
			}
	
			pausePlayback();
		}
	}
	/**
	 * @param sampleRate The sampleRate to set.
	 */
	public void setSampleRate(final int sampleRate)
	{
		final int oldSampleRate = this.sampleRate;

		final boolean wasPlaying = !isPaused();
		if (wasPlaying) pausePlayback();

		this.sampleRate = sampleRate;
		if (wasPlaying)
		{
			modMixer.changeSampleRate(sampleRate);
			initialize();
			openAudioDevice();
			if (!isInitialized())
			{
				modMixer.changeSampleRate(this.sampleRate = oldSampleRate);
				initialize();
				openAudioDevice();
			}
	
			pausePlayback();
		}
	}
	/**
	 * @param sampleSizeInBits The sampleSizeInBits to set.
	 */
	public void setSampleSizeInBits(final int sampleSizeInBits)
	{
		final int oldsampleSizeInBits = this.sampleSizeInBits;

		final boolean wasPlaying = !isPaused();
		if (wasPlaying) pausePlayback();

		this.sampleSizeInBits = sampleSizeInBits;
		initialize();
		openAudioDevice();
		if (!isInitialized())
		{
			this.sampleSizeInBits = oldsampleSizeInBits;
			initialize();
			openAudioDevice();
		}

		if (wasPlaying) pausePlayback();
	}
	/**
	 * @param channels The channels to set.
	 */
	public void setChannels(final int channels)
	{
		final int oldChannels = this.channels;

		final boolean wasPlaying = !isPaused();
		if (wasPlaying) pausePlayback();

		this.channels = channels;
		initialize();
		openAudioDevice();
		if (!isInitialized())
		{
			this.channels = oldChannels;
			initialize();
			openAudioDevice();
		}

		if (wasPlaying) pausePlayback();
	}
	/**
	 * @param maxNNAChannels the maxNNAChannels to set
	 */
	public void setMaxNNAChannels(final int maxNNAChannels)
	{
		final int oldMaxNNAChannels = this.maxNNAChannels;

		final boolean wasPlaying = !isPaused();
		if (wasPlaying) pausePlayback();

		modMixer.changeMaxNNAChannels(this.maxNNAChannels = maxNNAChannels);
		initialize();
		openAudioDevice();
		if (!isInitialized())
		{
			modMixer.changeMaxNNAChannels(this.maxNNAChannels = oldMaxNNAChannels);
			initialize();
			openAudioDevice();
		}

		if (wasPlaying) pausePlayback();
	}
	/**
	 * @param ditherFilterType the ditherFilterType to set
	 */
	public void setDitherFilterType(final int newDitherFilterType)
	{
		final int oldDitherFilterType = ditherFilterType;
		
		final boolean wasPlaying = !isPaused();
		if (wasPlaying) pausePlayback();

		ditherFilterType = newDitherFilterType;
		if (wasPlaying)
		{
			initialize();
			openAudioDevice();
			if (!isInitialized())
			{
				ditherFilterType = oldDitherFilterType;
				initialize();
				openAudioDevice();
			}
	
			pausePlayback();
		}
	}
	/**
	 * @param ditherType the ditherType to set
	 */
	public void setDitherType(final int newDitherType)
	{
		final int oldDitherType = ditherType;
		
		final boolean wasPlaying = !isPaused();
		if (wasPlaying) pausePlayback();

		ditherType = newDitherType;
		if (wasPlaying)
		{
			initialize();
			openAudioDevice();
			if (!isInitialized())
			{
				ditherType = oldDitherType;
				initialize();
				openAudioDevice();
			}
	
			pausePlayback();
		}
	}
	/**
	 * @param ditherByPass set if dither is bypass
	 */
	public void setDitherByPass(final boolean newByPassDither)
	{
		ditherByPass = newByPassDither;
		if (dither!=null) dither.setBypass(ditherByPass);
	}
	/**
	 * @return the mod
	 */
	public Module getMod()
	{
		return mod;
	}
	/**
	 * @return the modMixer
	 */
	public BasicModMixer getModMixer()
	{
		return modMixer;
	}
	/**
	 * 
	 * @see de.quippy.javamod.mixer.Mixer#isSeekSupported()
	 */
	@Override
	public boolean isSeekSupported()
	{
		return true;
	}
	/**
	 * 
	 * @see de.quippy.javamod.mixer.Mixer#getMillisecondPosition()
	 */
	@Override
	public long getMillisecondPosition()
	{
		return currentSamplesWritten * 1000L / (long)sampleRate;
	}
	/**
	 * @param milliseconds
	 * @see de.quippy.javamod.mixer.BasicMixer#seek(long)
	 * @since 13.02.2012
	 */
	@Override
	protected void seek(final long milliseconds)
	{
		currentSamplesWritten = modMixer.seek(milliseconds);
	}
	/**
	 * 
	 * @see de.quippy.javamod.mixer.Mixer#getLengthInMilliseconds()
	 */
	@Override
	public long getLengthInMilliseconds()
	{
		if (mod.getLengthInMilliseconds()==-1)
		{
			mod.setLengthInMilliseconds(modMixer.getLengthInMilliseconds());
		}
		return mod.getLengthInMilliseconds();
	}
	/**
	 * @return
	 * @see de.quippy.javamod.mixer.Mixer#getChannelCount()
	 */
	@Override
	public int getChannelCount()
	{
		if (modMixer!=null)
			return modMixer.getCurrentUsedChannels();
		else
			return 0;
	}
	/**
	 * @return
	 * @see de.quippy.javamod.mixer.Mixer#getCurrentKBperSecond()
	 */
	@Override
	public int getCurrentKBperSecond()
	{
		return (getChannelCount()*sampleSizeInBits*sampleRate)/1000;
	}
	/**
	 * @return
	 * @see de.quippy.javamod.mixer.Mixer#getCurrentSampleRate()
	 */
	@Override
	public int getCurrentSampleRate()
	{
		return sampleRate;
	}
	/**
	 * @since 22.06.2006
	 */
	@Override
	public void startPlayback()
	{
		long leftNR = 0;
		long rightNR = 0;

		initialize();
		currentSamplesWritten = 0; // not in initialize which is also called at freq. changes
		
		setIsPlaying();

		final int xba = nXBassDepth+1;
		final int xbamask = (1 << xba) - 1;
		
		if (getSeekPosition()>0) seek(getSeekPosition());
		
		try
		{
			openAudioDevice();
			if (!isInitialized()) return;
			
			int count;
			do
			{
				// get "count" values of 32 bit signed sampledata for mixing
				count = modMixer.mixIntoBuffer(LBuffer, RBuffer, bufferSize);
				if (count > 0)
				{
					int ox=0; int ix=0;
					while (ix < count)
					{
						// get Sample and reset to zero - the samples are clipped
						long lsample = LBuffer[ix]; LBuffer[ix]=0;
						long rsample = RBuffer[ix]; RBuffer[ix]=0;
						ix++;
						
						// WideStrereo Mixing - but only with stereo
						if (doWideStereoMix && channels>1)
						{
							wideLBuffer[writePointer]=lsample;
							wideRBuffer[writePointer++]=rsample;
							if (writePointer>=maxWideStereo) writePointer=0;
	
							rsample+=(wideLBuffer[readPointer]>>1);
							lsample+=(wideRBuffer[readPointer++]>>1);
							if (readPointer>=maxWideStereo) readPointer=0;
						}
	
						// MegaBass
						if (doMegaBass)
						{
							nXBassSum -= XBassBuffer[nXBassBufferPos];
							long tmp0 = lsample + rsample;
							long tmp = (tmp0 + ((tmp0 >> 31) & xbamask)) >> xba;
							XBassBuffer[nXBassBufferPos] = tmp;
							nXBassSum += tmp;

							long v = XBassDelay[nXBassDlyPos];
							XBassDelay[nXBassDlyPos] = lsample;
							lsample = v + nXBassSum;
							
							v = XBassDelay[nXBassDlyPos+1];
							XBassDelay[nXBassDlyPos+1] = rsample;
							rsample = v + nXBassSum;
							
							nXBassDlyPos = (nXBassDlyPos + 2) & nXBassMask;
							nXBassBufferPos = (nXBassBufferPos+1) & nXBassMask;
						}
						
						// Noise Reduction with a simple high pass filter:
						if (doNoiseReduction)
						{
							long vnr = lsample>>1;
							lsample = vnr + leftNR;
							leftNR = vnr;
							
							vnr = rsample>>1;
							rsample = vnr + rightNR;
							rightNR = vnr;
						}
						
						// Reduce to samplesize by dithering - if necessary!
						if (sampleSizeInBits<32) // our maximum - no dithering needed
						{
							lsample = (long)(dither.process((double)lsample/(double)(0x7FFFFFFFL), 0)*(double)maximum);
							rsample = (long)(dither.process((double)rsample/(double)(0x7FFFFFFFL), 1)*(double)maximum);
						}

						// Clip the values to target:
						if (lsample > maximum) lsample = maximum;
						else if (lsample < minimum) lsample = minimum;
						if (rsample > maximum) rsample = maximum;
						else if (rsample < minimum) rsample = minimum;
						
						// and after that put them into the outputbuffer
						// to write to the soundstream
						if (channels==2)
						{
							for (int i=0; i<rounds; i++)
							{
								output[ox] = (byte)lsample;
								output[ox+rounds] = (byte)rsample;
								ox++;
								lsample>>=8;
								rsample>>=8;
							}
							ox += rounds; // skip saved right channel
						}
						else
						{
							long sample = (lsample + rsample)>>1; 
							for (int i=0; i<rounds; i++)
							{
								output[ox++] = (byte)sample;
								sample>>=8;
							}
						}
					}
					
					writeSampleDataToLine(output, 0, ox);

					currentSamplesWritten += count;
				}
				
				if (stopPositionIsReached()) setIsStopping();

				if (isStopping())
				{
					setIsStopped();
					break;
				}
				if (isPausing())
				{
					setIsPaused();
					while (isPaused())
					{
						try { Thread.sleep(10L); } catch (InterruptedException ex) { /*noop*/ }
					}
				}
				if (isInSeeking())
				{
					setIsSeeking();
					while (isInSeeking())
					{
						try { Thread.sleep(10L); } catch (InterruptedException ex) { /*noop*/ }
					}
				}
			}
			while (count!=-1);
			if (count<=0) setHasFinished(); // Piece was finished!
		}
		catch (Throwable ex)
		{
			throw new RuntimeException(ex);
		}
		finally
		{
			setIsStopped();
			closeAudioDevice();
		}
	}
}
