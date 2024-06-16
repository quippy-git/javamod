/*
 * @(#) SampleInstrumentPlayer.java
 *
 * Created on 28.12.2023 by Daniel Becker
 * 
 * The purpose of this class is to play a certain sample / instrument
 * It is used by the ModSampleDialog and the ModInstrumentDialog for
 * spontaneous play back of displayed samples or instruments.
 * It is re-using a BasicModMixer instance and preparing a global
 * ChannelMemory with all needed information to perform the task.
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
package de.quippy.javamod.multimedia.mod.mixer;

import javax.sound.sampled.AudioFormat;

import de.quippy.javamod.io.SoundOutputStream;
import de.quippy.javamod.io.SoundOutputStreamImpl;
import de.quippy.javamod.mixer.dsp.iir.filter.Dither;
import de.quippy.javamod.multimedia.mod.ModConstants;
import de.quippy.javamod.multimedia.mod.ModMixer;
import de.quippy.javamod.multimedia.mod.loader.instrument.Instrument;
import de.quippy.javamod.multimedia.mod.loader.instrument.Sample;
import de.quippy.javamod.multimedia.mod.mixer.BasicModMixer.ChannelMemory;

/**
 * @author Daniel Becker
 * @since 28.12.2023
 */
public class SampleInstrumentPlayer
{
	private ChannelMemory aktMemo;
	private ModMixer currentModMixer;
	private BasicModMixer currentMixer;
	private boolean doHardStop;
	
	private Sample sample;
	private Instrument instrument;
	private int period;
	private int noteIndex;
	
	/**
	 * Constructor for SampleInstrumentPlayer
	 * @param theModMixer
	 */
	public SampleInstrumentPlayer(final ModMixer theModMixer)
	{
		super();
		doHardStop = false;
		setCurrentModMixer(theModMixer);
	}
	/**
	 * @param currentModMixer the currentModMixer to set
	 */
	private void setCurrentModMixer(ModMixer currentModMixer)
	{
		this.currentModMixer = currentModMixer;
		if (currentModMixer!=null)
		{
			currentMixer = currentModMixer.getModMixer();
			aktMemo = currentMixer.new ChannelMemory();
			aktMemo.instrumentFinished = true;
		}
	}
	/**
	 * @since 29.12.2023
	 * @param forInstrument: maybe null, if no instrument - then a Sample must be given
	 * @param forSample: the sample - null, if instrument given (is overwritten anyways)
	 * @param forNoteIndex the noteIndex (starting with 1) to play - a corresponding period is found here... 
	 */
	public void startPlayback(final Instrument forInstrument, final Sample forSample, final int forNoteIndex)
	{
		if (!aktMemo.instrumentFinished) stopPlayback();
		if ((forSample==null && forInstrument==null) || forNoteIndex<1) return;
		
		instrument = forInstrument;
		noteIndex = forNoteIndex;
		if (forInstrument!=null)
		{
			sample = currentModMixer.getMod().getInstrumentContainer().getSample(forInstrument.getSampleIndex(noteIndex - 1));
			noteIndex = instrument.getNoteIndex(noteIndex - 1) + 1;
		}
		else
		{
			sample = forSample;
		}
		period = ModConstants.noteValues[noteIndex - 1];
		
		doHardStop = false;
		aktMemo.instrumentFinished = false;
		playSample();
	}
	public void stopPlayback()
	{
		if (instrument!=null)
			aktMemo.keyOff = true;
		else
		{
			doHardStop = true;
			aktMemo.instrumentFinished = true;
		}
		while (doHardStop) try { Thread.sleep(10L); } catch (InterruptedException ex) { /*NOOP*/ }
	}
	public boolean isPlaying()
	{
		return !aktMemo.instrumentFinished;
	}
	private void prepareAktMemoAndMixer()
	{
		// initialize the mixer
		currentMixer.initializeMixer(false);

		// set ChannelMemory for the sample / instrument and note to play
		aktMemo.channelNumber = 1;
		aktMemo.currentSample = sample;
		aktMemo.assignedInstrument = instrument;
		aktMemo.currentNotePeriod = period<<ModConstants.PERIOD_SHIFT;
		aktMemo.assignedNoteIndex = noteIndex;
		aktMemo.keyOff = false;

		// Set volumes of sample
		aktMemo.channelVolume = 64;
		aktMemo.currentVolume = sample.volume;

		// plus set master and global volume back to maximum - we only have one voice
		currentMixer.masterVolume = ModConstants.MAX_MIXING_PREAMP;
		currentMixer.globalVolume = ModConstants.MAXGLOBALVOLUME;

		// now for the tuning of the current note set
		currentMixer.resetForNewSample(aktMemo);
		currentMixer.setPeriodBorders(aktMemo);
		currentMixer.setNewPlayerTuningFor(aktMemo, currentMixer.getFineTunePeriod(aktMemo));
		
		// ImpulseTracker specials
		if (currentMixer.isIT && instrument!=null)
		{
			boolean useFilter = !currentMixer.globalFilterMode;
			// Set filter and random variations (volume, panning)
			useFilter = currentMixer.setFilterAndRandomVariations(aktMemo, instrument, useFilter);
			if (aktMemo.cutOff<0x7F && useFilter) currentMixer.setupChannelFilter(aktMemo, true, 256);
		}
		
		// and calculate the samples per tick
		currentMixer.calculateSamplesPerTick();
	}
	private void playSample()
	{
		if (sample==null || sample.length==0) return;

		final int sampleRate = currentModMixer.getCurrentSampleRate();
		final int sampleSizeInBits = currentModMixer.getCurrentSampleSizeInBits();
		final int channels = currentModMixer.getCurrentChannels();

		final int bytesPerSample = sampleSizeInBits >> 3;
		final int shift = 32 - sampleSizeInBits;
		final long maximum = ModConstants.CLIPP32BIT_MAX >> shift;
		final long minimum = ModConstants.CLIPP32BIT_MIN >> shift;

		final Dither dither = new Dither(channels, sampleSizeInBits, 4, 2, false);

		prepareAktMemoAndMixer();
		final int samplesPerTick = currentMixer.samplesPerTick; 
		final long leftBuffer[] = new long[samplesPerTick];
		final long rightBuffer[] = new long[samplesPerTick];

		final int bufferSize = ((250 * sampleRate) / 1000) * bytesPerSample * channels;
		final byte [] output = new byte[bufferSize];

		SoundOutputStream outputStream = new SoundOutputStreamImpl(new AudioFormat(sampleRate, sampleSizeInBits, channels, true, false), null, null, false, false, bufferSize);
		outputStream.open();

		int ox = 0;
		while (!aktMemo.instrumentFinished)
		{
			// Process envelopes
			currentMixer.processEnvelopes(aktMemo);
			
			// Force a hard stop of any finished instrument (XM will not stop the instrument in this condition...)
			if (aktMemo.noteFade && (aktMemo.fadeOutVolume<=0 || (aktMemo.actVolumeLeft<=0 && aktMemo.actRampVolRight<=0))) aktMemo.instrumentFinished=true;

			//and then render one tick of sample data
			currentMixer.mixChannelIntoBuffers(leftBuffer, rightBuffer, 0, samplesPerTick, aktMemo);
			
			// copy those to the render buffer
			for (int s=0; s<samplesPerTick; s++)
			{
				// get a sample and set the buffer back to zero!
				long sampleL = leftBuffer[s]; leftBuffer[s]=0;
				long sampleR = rightBuffer[s]; rightBuffer[s]=0;

				// Dither
				if (sampleSizeInBits<32) // our maximum - no dithering needed
				{
					sampleL = (long)((dither.process((double)sampleL/(double)(0x7FFFFFFFL), 0)*(double)maximum) + 0.5d);
					sampleR = (long)((dither.process((double)sampleR/(double)(0x7FFFFFFFL), 1)*(double)maximum) + 0.5d);
				}
				
				// Clip the values to target:
				if (sampleL > maximum) sampleL = maximum;
				else if (sampleL < minimum) sampleL = minimum;
				if (sampleR > maximum) sampleR = maximum;
				else if (sampleR < minimum) sampleR = minimum;
				
				// and copy stereo / mono
				if (channels==2)
				{
					for (int i=0; i<bytesPerSample; i++)
					{
						output[ox] = (byte)sampleL;
						output[ox+bytesPerSample] = (byte)sampleR;
						ox++;
						sampleL>>=8;
						sampleR>>=8;
					}
					ox += bytesPerSample; // skip saved right channel
				}
				else
				{
					long sampleValue = (sampleL + sampleR)>>1; 
					for (int i=0; i<bytesPerSample; i++)
					{
						output[ox++] = (byte)sampleValue;
						sampleValue>>=8;
					}
				}
				
				// if render buffer is full, send to sound card
				if (ox==output.length)
				{
					outputStream.writeSampleData(output, 0, ox);
					ox = 0;
				}
			}
		}
		if (doHardStop)
			outputStream.flushLine();
		else
		{
			// send a rest, if any...
			if (ox>0)
			{
				outputStream.writeSampleData(output, 0, ox);
				ox = 0;
			}
		}
		outputStream.closeAllDevices();
		doHardStop = false;
	}
}
