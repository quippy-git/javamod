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
	private boolean doDCRemoval;

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
	
	private ModDSP modDSP = new ModDSP();
	
	/**
	 * Constructor for ModMixer
	 */
	public ModMixer(final Module mod, final int sampleSizeInBits, final int channels, final int sampleRate, final int doISP, final boolean doWideStereoMix, final boolean doNoiseReduction, final boolean doMegaBass, final boolean doDCremoval, final int doNoLoops, final int maxNNAChannels, final int msBufferSize, final int ditherFilter, final int ditherType, final boolean ditherByPass)
	{
		super();
		this.mod = mod;
		this.sampleSizeInBits=sampleSizeInBits;
		this.channels=channels;
		this.sampleRate=sampleRate;
		this.doWideStereoMix = (channels<2)?false:doWideStereoMix;
		this.doNoiseReduction = doNoiseReduction;
		this.doMegaBass = doMegaBass;
		this.doDCRemoval = doDCremoval;
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
		bufferSize = (msBufferSize * sampleRate) / 1000;
		LBuffer = new long[bufferSize];
		RBuffer = new long[bufferSize];

		// For the DSP-Output
		outputBufferSize = bufferSize*channels; // For each channel!

		// Now for the bits (linebuffer):
		final int bytesPerSample = sampleSizeInBits>>3; // DIV 8;
		outputBufferSize *= bytesPerSample;
		output = new byte[outputBufferSize];
		setSourceLineBufferSize(outputBufferSize);
		
		// initialize the dithering for lower sample rates
		// always for maximum channels
		dither = new Dither(2, sampleSizeInBits, ditherFilterType, ditherType, ditherByPass);

		// Clipping and shifting samples to target buffer
		rounds = sampleSizeInBits >> 3;
		shift = 32 - sampleSizeInBits;
		maximum = ModConstants.CLIPP32BIT_MAX >> shift;
		minimum = ModConstants.CLIPP32BIT_MIN >> shift;
		
		// and init the modDSP (full!)
		modDSP.initModDSP(sampleRate);
		
		setAudioFormat(new AudioFormat(sampleRate, sampleSizeInBits, channels, true, false)); // signed, little endian
	}
	/**
	 * @param doNoiseReduction The doNoiseReduction to set.
	 */
	public void setDoNoiseReduction(final boolean doNoiseReduction)
	{
		this.doNoiseReduction = doNoiseReduction;
		if (doNoiseReduction) modDSP.initNoiseReduction();
	}
	/**
	 * @param doWideStereoMix The doWideStereoMix to set.
	 */
	public void setDoWideStereoMix(final boolean doWideStereoMix)
	{
		this.doWideStereoMix = doWideStereoMix;
//		if (doWideStereoMix) modDSP.initWideStereo(sampleRate);
		if (doWideStereoMix) modDSP.initSurround(sampleRate);
	}
	/**
	 * @param doMegaBass The doMegaBass to set.
	 */
	public void setDoMegaBass(final boolean doMegaBass)
	{
		this.doMegaBass = doMegaBass;
		if (doMegaBass) modDSP.initMegaBass(sampleRate);
	}
	/**
	 * @param doDCRemoval the doDCRemoval to set
	 */
	public void setDoDCRemoval(boolean doDCRemoval)
	{
		this.doDCRemoval = doDCRemoval;
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

		final boolean wasPaused = isPaused(); 
		final boolean wasPlaying = isPlaying();
		if (wasPlaying && !wasPaused) pausePlayback();

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
	
			if (!wasPaused) pausePlayback();
		}
	}
	/**
	 * @param sampleRate The sampleRate to set.
	 */
	public void setSampleRate(final int sampleRate)
	{
		final int oldSampleRate = this.sampleRate;

		final boolean wasPaused = isPaused(); 
		final boolean wasPlaying = isPlaying();
		if (wasPlaying && !wasPaused) pausePlayback();

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
	
			if (!wasPaused) pausePlayback();
		}
	}
	/**
	 * @param sampleSizeInBits The sampleSizeInBits to set.
	 */
	public void setSampleSizeInBits(final int sampleSizeInBits)
	{
		final int oldsampleSizeInBits = this.sampleSizeInBits;

		final boolean wasPaused = isPaused(); 
		final boolean wasPlaying = isPlaying();
		if (wasPlaying && !wasPaused) pausePlayback();

		this.sampleSizeInBits = sampleSizeInBits;
		if (wasPlaying)
		{
			initialize();
			openAudioDevice();
			if (!isInitialized())
			{
				this.sampleSizeInBits = oldsampleSizeInBits;
				initialize();
				openAudioDevice();
			}
	
			if (!wasPaused) pausePlayback();
		}
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
			mod.setLengthInMilliseconds(modMixer.getLengthInMilliseconds());
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
	public int getCurrentSampleSizeInBits()
	{
		return sampleSizeInBits;
	}
	public int getCurrentChannels()
	{
		return channels;
	}
	/**
	 * @since 22.06.2006
	 */
	@Override
	public void startPlayback()
	{
		initialize();
		currentSamplesWritten = 0; // not in initialize which is also called at freq. changes
		
		setIsPlaying();

		if (getSeekPosition()>0) seek(getSeekPosition());
		
		final long[] samples = new long[2];
		
		// how many Samples can we write out? We will need that to reset the currentSamplesWritten if MOD is looped.
		final long allSamplesWritten = (getLengthInMilliseconds()!=-1)?getLengthInMilliseconds() * sampleRate / 1000L:-1;
		
		try
		{
			openAudioDevice();
			if (!isInitialized()) return;
			
			// If we do export to wave and do not want to play during that, do not fire any updates
			if (exportFile==null || playDuringExport) modMixer.setFireUpdates(true);
			
			int count;
			do
			{
				// get "count" values of 32 bit signed sample data for mixing
				count = modMixer.mixIntoBuffer(LBuffer, RBuffer, bufferSize);
				if (count > 0)
				{
					int ox=0; int ix=0;
					while (ix < count)
					{
						// get Sample and reset to zero - the samples are clipped
						samples[0] = LBuffer[ix]; LBuffer[ix]=0;
						samples[1] = RBuffer[ix]; RBuffer[ix]=0;
						ix++;
						
						// DC Removal
						if (doDCRemoval) modDSP.processDCRemoval(samples);
						
						// Noise Reduction with a simple high pass filter:
						if (doNoiseReduction) modDSP.processNoiseReduction(samples);

						// MegaBass
						if (doMegaBass) modDSP.processMegaBass(samples);
						
						// WideStrereo Mixing - but only with stereo
						//if (doWideStereoMix && channels>1) modDSP.processWideStereo(samples);
						if (doWideStereoMix && channels>1) modDSP.processStereoSurround(samples);

						// Reduce to sample size by dithering - if necessary!
						if (sampleSizeInBits<32) // our maximum - no dithering needed
						{
							samples[0] = (long)((dither.process((double)samples[0]/(double)(0x7FFFFFFFL), 0)*(double)maximum) + 0.5d);
							samples[1] = (long)((dither.process((double)samples[1]/(double)(0x7FFFFFFFL), 1)*(double)maximum) + 0.5d);
						}

						// Clip the values to target:
						if (samples[0] > maximum) samples[0] = maximum;
						else if (samples[0] < minimum) samples[0] = minimum;
						if (samples[1] > maximum) samples[1] = maximum;
						else if (samples[1] < minimum) samples[1] = minimum;
						
						// and after that put them into the outputbuffer
						// to write to the soundstream
						if (channels==2)
						{
							for (int i=0; i<rounds; i++)
							{
								output[ox] = (byte)samples[0];
								output[ox+rounds] = (byte)samples[1];
								ox++;
								samples[0]>>=8;
								samples[1]>>=8;
							}
							ox += rounds; // skip saved right channel
						}
						else
						{
							long sample = (samples[0] + samples[1])>>1; 
							for (int i=0; i<rounds; i++)
							{
								output[ox++] = (byte)sample;
								sample>>=8;
							}
						}
					}
					
					writeSampleDataToLine(output, 0, ox);

					currentSamplesWritten += count;
					// let's reset the amount of samples written if we did a loop...
					if (allSamplesWritten!=-1 && currentSamplesWritten>allSamplesWritten) currentSamplesWritten -= allSamplesWritten;

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
			modMixer.setFireUpdates(false);
			setIsStopped();
			closeAudioDevice();
		}
	}
}
