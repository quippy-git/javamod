/*
 * @(#) ScreamTrackerMixer.java
 *
 * Created on 07.05.2006 by Daniel Becker
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
package de.quippy.javamod.multimedia.mod.mixer;

import java.util.concurrent.atomic.AtomicInteger;

import de.quippy.javamod.multimedia.mod.ModConstants;
import de.quippy.javamod.multimedia.mod.loader.Module;
import de.quippy.javamod.multimedia.mod.loader.instrument.Envelope;
import de.quippy.javamod.multimedia.mod.loader.instrument.Instrument;
import de.quippy.javamod.multimedia.mod.loader.instrument.Sample;
import de.quippy.javamod.multimedia.mod.loader.pattern.PatternElement;
import de.quippy.javamod.multimedia.mod.midi.MidiMacros;

/**
 * This is the screamtracker mixing routine with all special mixing
 * on typical screamtracker events
 * @author Daniel Becker
 * @since 07.05.2006
 */
public class ScreamTrackerMixer extends BasicModMixer
{
	protected boolean isNotITCompatMode = false;
	protected boolean is_S3M_GUS = false;
	/**
	 * Constructor for ScreamTrackerMixer
	 * @param mod
	 * @param sampleRate
	 * @param doISP
	 */
	public ScreamTrackerMixer(final Module mod, final int sampleRate, final int doISP, final int doNoLoops, final int maxNNAChannels)
	{
		super(mod, sampleRate, doISP, doNoLoops, maxNNAChannels);
		isNotITCompatMode = (mod.getSongFlags() & ModConstants.SONG_ITCOMPATMODE)==0;
		is_S3M_GUS = (mod.getSongFlags() & ModConstants.SONG_S3M_GUS)!=0;
	}
	/**
	 * Sets the borders for Portas
	 * @since 17.06.2010
	 * @param aktMemo
	 * @see de.quippy.javamod.multimedia.mod.mixer.BasicModMixer#setPeriodBorders(de.quippy.javamod.multimedia.mod.mixer.BasicModMixer.ChannelMemory)
	 */
	@Override
	protected void setPeriodBorders(final ChannelMemory aktMemo)
	{
		if ((mod.getSongFlags()&ModConstants.SONG_AMIGALIMITS)!=0) // IT/S3M Amiga Limit flag
		{
			aktMemo.portaStepUpEnd = 113<<ModConstants.PERIOD_SHIFT;
			aktMemo.portaStepDownEnd = 856<<ModConstants.PERIOD_SHIFT;
		}
		else
		if (isS3M)
		{
			aktMemo.portaStepUpEnd = 0x40<<(ModConstants.PERIOD_SHIFT-2);
			aktMemo.portaStepDownEnd = 0x7FFF<<(ModConstants.PERIOD_SHIFT-2);
		}
		else
		{
			// For IT no limits... But no wrap around with 32Bit either :)
			aktMemo.portaStepUpEnd = 0;
			aktMemo.portaStepDownEnd = 0x00FFFFFF; // Short.MAX_VALUE<<ModConstants.PERIOD_SHIFT <- this is signed 0x7ffff then
		}
	}
	/**
	 * @param channel
	 * @param aktMemo
	 * @see de.quippy.javamod.multimedia.mod.mixer.BasicModMixer#initializeMixer(int, de.quippy.javamod.multimedia.mod.mixer.BasicModMixer.ChannelMemory)
	 */
	@Override
	protected void initializeMixer(final int channel, final ChannelMemory aktMemo)
	{
		aktMemo.muteWasITforced = aktMemo.muted = ((mod.getPanningValue(channel) & ModConstants.CHANNEL_IS_MUTED)!=0);
		aktMemo.doSurround = (mod.getPanningValue(channel) == ModConstants.CHANNEL_IS_SURROUND);
		setPeriodBorders(aktMemo);
	}
	/**
	 * @param aktMemo
	 * @param period
	 * @return
	 * @see de.quippy.javamod.multimedia.mod.mixer.BasicModMixer#getFineTunePeriod(de.quippy.javamod.multimedia.mod.mixer.BasicModMixer.ChannelMemory, int)
	 */
	@Override
	protected int getFineTunePeriod(final ChannelMemory aktMemo, final int period)
	{
		final int noteIndex = period - 1; // Period is only a note index now. No period - easier lookup
		switch (frequencyTableType)
		{
			case ModConstants.STM_S3M_TABLE:
			case ModConstants.IT_AMIGA_TABLE:
				final int s3mNote=ModConstants.FreqS3MTable[noteIndex%12];
				final int s3mOctave=noteIndex/12;
				// If I do not do it like this, it is too precise - and limits do not work
				return (int)(ModConstants.BASEFREQUENCY * ((long)s3mNote<<5) / ((long)aktMemo.currentFinetuneFrequency<<s3mOctave))<<(ModConstants.PERIOD_SHIFT-2);

			case ModConstants.IT_LINEAR_TABLE:
				return (ModConstants.FreqS3MTable[noteIndex%12]<<7)>>(noteIndex/12);

			default:
				return super.getFineTunePeriod(aktMemo, period);
		}
	}
	/**
	 * @param aktMemo
	 * @param newPeriod
	 * @see de.quippy.javamod.multimedia.mod.mixer.BasicModMixer#setNewPlayerTuningFor(de.quippy.javamod.multimedia.mod.mixer.BasicModMixer.ChannelMemory, int)
	 */
	@Override
	protected void setNewPlayerTuningFor(final ChannelMemory aktMemo, final int newPeriod)
	{
		aktMemo.currentNotePeriodSet = newPeriod;

		if (newPeriod<=0)
		{
			aktMemo.currentTuning = 0;
			return;
		}

		switch (frequencyTableType)
		{
			case ModConstants.IT_LINEAR_TABLE:
				final long itTuning = (((((long)ModConstants.BASEPERIOD)<<ModConstants.PERIOD_SHIFT) * aktMemo.currentFinetuneFrequency)<<ModConstants.SHIFT) / sampleRate;
				aktMemo.currentTuning = (int)(itTuning / newPeriod);
				return;
			case ModConstants.STM_S3M_TABLE:
			case ModConstants.IT_AMIGA_TABLE:
				if (isS3M)
				{
					if (newPeriod>aktMemo.portaStepDownEnd)
					{
						aktMemo.currentTuning = globalTuning / aktMemo.portaStepDownEnd;
						if (!is_S3M_GUS) aktMemo.currentNotePeriod = aktMemo.currentNotePeriodSet = aktMemo.portaStepDownEnd;
					}
					else
					if (newPeriod<=0)
						aktMemo.currentTuning = 0;
					else
						aktMemo.currentTuning = globalTuning / ((newPeriod<aktMemo.portaStepUpEnd)?aktMemo.portaStepUpEnd:newPeriod);
				}
				else
					aktMemo.currentTuning = globalTuning / ((newPeriod>aktMemo.portaStepDownEnd)?aktMemo.portaStepDownEnd:(newPeriod<aktMemo.portaStepUpEnd)?aktMemo.portaStepUpEnd:newPeriod);
				return;
			default:
				super.setNewPlayerTuningFor(aktMemo, newPeriod);
				return;
		}
	}
	/**
	 * @param aktMemo
	 * @return
	 * @see de.quippy.javamod.multimedia.mod.mixer.BasicModMixer#calculateExtendedValue(de.quippy.javamod.multimedia.mod.mixer.BasicModMixer.ChannelMemory)
	 */
	@Override
	protected int calculateExtendedValue(final ChannelMemory aktMemo, final AtomicInteger extendedRowsUsed)
	{
		if (extendedRowsUsed!=null) extendedRowsUsed.set(0);
		int val = aktMemo.assignedEffektParam;
		if (!isIT) return val;

		int row = currentRow;
		int lookAheadRows = 4;
		switch (aktMemo.assignedEffekt)
		{
			case 0x0F:	// sample offset
				// 24 bit command
				lookAheadRows = 2;
				break;
			case 0x14:	// Tempo
			case 0x02:	// Pattern position jump
			case 0x03:	// Pattern Break
				// 16 bit command
				lookAheadRows = 1;
				break;
			default:
				return val;
		}

		final int rowsLeft = currentPattern.getRowCount() - currentRow - 1;
		if (lookAheadRows > rowsLeft) lookAheadRows = rowsLeft;
		int rowsUsed = 0;
		while (lookAheadRows>0)
		{
			row++; lookAheadRows--;
			final PatternElement patternElement = currentPattern.getPatternRow(row).getPatternElement(aktMemo.channelNumber);
			if (patternElement.getEffekt() != 0x1B) break;
			rowsUsed++;
			val = (val<<8) | (patternElement.getEffektOp()&0xFF);
		}
		if (extendedRowsUsed!=null) extendedRowsUsed.set(rowsUsed);
		return val;
	}
	/**
	 * perform the duplicate note checks, if any are defined
	 * @since 08.07.2020
	 * @param aktMemo
	 */
	protected void doDNA(final ChannelMemory aktMemo)
	{
		final Instrument instr = aktMemo.assignedInstrument;
		// we can save the time, if no duplicate action is set
		if ((instr == null) || (instr.dublicateNoteCheck == ModConstants.DCT_NONE)) return;

		final int channelNumber = aktMemo.channelNumber;
		for (int c=channelNumber; c<maxChannels; c++)
		{
			// Only apply to background channels, or the same pattern channel
			if (c!=channelNumber && c<mod.getNChannels())
				continue;

			final ChannelMemory currentNNAChannel = channelMemory[c];
			if (!isChannelActive(currentNNAChannel)) continue;

			if (currentNNAChannel.channelNumber == channelNumber)
			{
				boolean applyDNA = false;
				// Check the Check
				switch (instr.dublicateNoteCheck)
				{
					case ModConstants.DCT_NONE:
						// this was checked earlier - but to be complete here...
						break;
					case ModConstants.DCT_NOTE:
						final int note = aktMemo.assignedNoteIndex;
						// ** With other players, the noteindex of instrument mapping
						// ** might count!! Would be this:
						//final int note = inst.getNoteIndex(aktMemo.assignedNoteIndex-1)+1;
						// *********
						if (note>0 &&
								note==currentNNAChannel.assignedNoteIndex &&
								instr==currentNNAChannel.assignedInstrument)
							applyDNA = true;
						break;
					case ModConstants.DCT_SAMPLE:
						final Sample sample = aktMemo.currentSample;
						if (sample!=null &&
								sample==currentNNAChannel.currentSample && // this compares only pointer. Should work, as samples exist only once!
								instr==currentNNAChannel.assignedInstrument) // IT: also same instrument
							applyDNA = true;
						break;
					case ModConstants.DCT_INSTRUMENT:
						if (instr==currentNNAChannel.assignedInstrument)
							applyDNA = true;
						break;
					case ModConstants.DCT_PLUGIN:
						// TODO: Unsupported
						break;
				}

				if (applyDNA)
				{
					// We have a match!
					switch (instr.dublicateNoteAction)
					{
						case ModConstants.DNA_CUT:	// CUT: note volume to zero
							doNoteCut(currentNNAChannel);
							break;
						case ModConstants.DNA_FADE:		// fade: fade out with fixed values
							initNoteFade(currentNNAChannel);
							break;
						case ModConstants.DNA_OFF: 		// OFF: fade out with instrument fade out value
							doKeyOff(currentNNAChannel);
							break;
					}
				}
			}
		}
	}
	/**
	 * @since 11.06.2020
	 * @param aktMemo
	 * @param NNA
	 */
	protected void doNNA(final ChannelMemory aktMemo, final int NNA)
	{
		switch (NNA)
		{
			case ModConstants.NNA_CONTINUE:	// continue: let the music play
				break;
			case ModConstants.NNA_CUT:		// CUT: note volume to zero
				doNoteCut(aktMemo);
				break;
			case ModConstants.NNA_FADE:		// fade: fade out with fixed values
				initNoteFade(aktMemo);
				break;
			case ModConstants.NNA_OFF: 		// OFF: fade out with instrument fade out value
				doKeyOff(aktMemo);
				break;
		}
	}
	/**
	 * @since 11.06.2020
	 * @param aktMemo
	 * @param NNA
	 */
	protected void doNNANew(final ChannelMemory aktMemo, final int NNA)
	{
		ChannelMemory newChannel = null;
		int lowVol = ModConstants.MAXCHANNELVOLUME;
		int envPos = 0;
		// Pick a Channel with lowest volume or silence
		for (int c=mod.getNChannels(); c<maxChannels; c++)
		{
			final ChannelMemory memo = channelMemory[c];
			if (!isChannelActive(memo))
			{
				newChannel = memo;
				break;
			}

			// to find the channel with the lowest volume,
			// add left and right target volumes but add the current
			// channelVolume so temporary silent channels are not killed
			// (left and right volume are shifted by 12+6 bit, so channel volume
			//  has space in the lower part)
			// additionally we also consider channels being far beyond their endpoint
			final int currentVolume = (memo.actVolumeLeft + memo.actVolumeRight) | memo.channelVolume;
			if ((currentVolume < lowVol) || (currentVolume == lowVol && memo.volEnvTick > envPos))
			{
				envPos = memo.volEnvTick;
				lowVol = currentVolume;
				newChannel = memo;
			}
		}

		if (newChannel!=null)
		{
			newChannel.setUpFrom(aktMemo);
			doDNA(aktMemo);
			doNNA(newChannel, NNA);
			// stop the current channel - it is copied
			aktMemo.instrumentFinished = true;
		}
	}
	/**
	 * @since 11.06.2020
	 * @param aktMemo
	 * @param NNA
	 */
	protected void doNNAforAllof(final ChannelMemory aktMemo, final int NNA)
	{
		final int channelNumber = aktMemo.channelNumber;
		for (int c=mod.getNChannels(); c<maxChannels; c++)
		{
			final ChannelMemory currentNNAChannel = channelMemory[c];
			if (!isChannelActive(currentNNAChannel)) continue;
			if (currentNNAChannel.channelNumber == channelNumber)
				doNNA(currentNNAChannel, NNA);
		}
	}
	/**
	 * @since 29.03.2010
	 * @param aktMemo
	 */
	protected void doNNAAutoInstrument(final ChannelMemory aktMemo)
	{
		if (!isChannelActive(aktMemo) || aktMemo.muted || aktMemo.noteCut) return;

		final Instrument currentInstrument = aktMemo.assignedInstrument;
		// NNA_CUT is default for instruments with no NNA
		// so do not copy this to a new channel for just finishing
		// it off then.
		if ((currentInstrument!=null) && (currentInstrument.NNA != ModConstants.NNA_CUT))
		{
			final int nna;
			if (aktMemo.tempNNAAction>-1)
			{
				nna = aktMemo.tempNNAAction;
				aktMemo.tempNNAAction = -1;
			}
			else
				nna = currentInstrument.NNA;

			doNNANew(aktMemo, nna);
		}
	}
	/**
	 * @since 10.07.2024
	 * @param aktMemo
	 */
	protected void doNoteCut(final ChannelMemory aktMemo)
	{
		aktMemo.noteCut = true;
		//currentVolume = 0;
		// Schism sets tuning=0 and deletes the last period
		setNewPlayerTuningFor(aktMemo, aktMemo.currentNotePeriod = 0);
		// that would be our way:
		//aktMemo.instrumentFinished = true;
		aktMemo.doFastVolRamp = true;
	}
	/**
	 * @since 26.07.2024
	 * @param aktMemo
	 */
	protected void doKeyOff(final ChannelMemory aktMemo)
	{
		aktMemo.keyOff = true;
	}
	/**
	 * Check if effect is a fine slide - then only on first tick!
	 * @since 04.04.2020
	 * @param slideValue
	 * @return
	 */
	protected boolean isFineSlide(final int slideValue)
	{
		return ((slideValue>>4)==0xF && (slideValue&0xF)!=0x0) ||
			   ((slideValue>>4)!=0x0 && (slideValue&0xF)==0xF);
	}
	/**
	 * To not over and over again implement the same algorithm, this method
	 * will return a -value or a value. Just add (or substract) it
	 * @since 22.12.2023
	 * @param effectOp
	 * @return
	 */
	protected int getFineSlideValue(final int effectOp)
	{
		final int x = (effectOp>>4)&0x0F;
		final int y = effectOp&0x0F;

		if (isSTM) // No fine slide with STMs, lower nibble has precedence
		{
			if (y!=0) return -y;
			return x;
		}

		// 0xFF can be fine slide up 15 or down 15. Per convention it is
		// fine up 15, so we test fine up first.
		if (y==0xF) // Fine Slide Up or normal slide down 15 (0x0F)
		{
			if (x!=0)
				return x;
			else
				return -15;
		}
		else
		if (x==0xF) // Fine Slide down or normal slide up
		{
			if (y!=0)
				return -y;
			else
				return 15;
		}
		else
		if (y!=0)
		{
			if (!isIT || x==0) return -y;
		}
		else if ((x!=0) && (!isIT || y==0)) return x;
		// Having OP with x and y set (like 15 or 84) is not supported with IT and does nothing
		return 0;
	}
	/**
	 * Is only called for ImpulseTracker now. Fast- and Pro-Tracker are handled
	 * completely differently
	 * @since 14.06.2020
	 * @param aktMemo
	 */
	protected void resetForNewSample(final ChannelMemory aktMemo)
	{
		resetInstrumentPointers(aktMemo, true);
		resetFineTune(aktMemo, aktMemo.currentSample);
		resetEnvelopes(aktMemo);
		resetAutoVibrato(aktMemo, aktMemo.currentSample);
		aktMemo.doFastVolRamp = true;
	}
	/**
	 * TODO: Clean up this mess - it is now only for STM, S3M, IT and MPTM
	 * @since 14.07.2024
	 * @param aktMemo
	 * @see de.quippy.javamod.multimedia.mod.mixer.BasicModMixer#setNewInstrumentAndPeriod(de.quippy.javamod.multimedia.mod.mixer.BasicModMixer.ChannelMemory)
	 */
	@Override
	protected void setNewInstrumentAndPeriod(final ChannelMemory aktMemo)
	{
		final PatternElement element = aktMemo.currentElement;
//		final boolean isNoteDelay = isNoteDelayEffekt(aktMemo.currentAssignedEffekt, aktMemo.currentAssignedEffektParam);
		final boolean isKeyOff = element.getPeriod()==ModConstants.KEY_OFF || element.getNoteIndex()==ModConstants.KEY_OFF;
		final boolean isNewNote = hasNewNote(element);
		final boolean isPortaToNoteEffect = isPortaToNoteEffekt(aktMemo.currentAssignedEffekt, aktMemo.currentAssignedEffektParam, aktMemo.currentAssignedVolumeEffekt, aktMemo.currentAssignedVolumeEffektOp, aktMemo.currentAssignedNotePeriod);

		// Do Instrument default NNA
		if (isIT && isNewNote &&
			!isPortaToNoteEffect &&
			!isNNAEffekt(aktMemo.currentAssignedEffekt, aktMemo.currentAssignedEffektParam)) // New Note Action
		{
			doNNAAutoInstrument(aktMemo);
		}

		// copy last seen values from pattern - only effect values first
		aktMemo.assignedEffekt = aktMemo.currentAssignedEffekt;
		aktMemo.assignedEffektParam = aktMemo.currentAssignedEffektParam;
		aktMemo.assignedVolumeEffekt = aktMemo.currentAssignedVolumeEffekt;
		aktMemo.assignedVolumeEffektOp = aktMemo.currentAssignedVolumeEffektOp;
		aktMemo.assignedNotePeriod = aktMemo.currentAssignedNotePeriod;
		aktMemo.assignedNoteIndex = aktMemo.currentAssignedNoteIndex;
		aktMemo.assignedSample = (aktMemo.currentAssignedInstrument!=null)?
		                            ((aktMemo.assignedNoteIndex>0)? // but only if we also have a note index, if not, ignore it!
		        				       mod.getInstrumentContainer().getSample(aktMemo.currentAssignedInstrument.getSampleIndex(aktMemo.assignedNoteIndex-1))
		        				       :null) // Instrument set without a note - so no mapping to sample possible!
		        				    :mod.getInstrumentContainer().getSample(aktMemo.currentAssignedInstrumentIndex-1);

		if (aktMemo.assignedEffekt!=0x11) aktMemo.retrigCount = -1; // Effect Retrigger Note: indicating, that a retrigger is not continuing

		boolean hasInstrument = element.getInstrument()>0 && aktMemo.assignedSample!=null;
		if (hasInstrument) // At this point we reset volume and panning for IT, STM, S3M
		{
			if (isPortaToNoteEffect) // Sample/Instrument change at Porta2Note needs special handling
			{
				if (isS3M) // set new sample volume, if sample is different, not null (already checked) and has samples
				{
					if (aktMemo.assignedSample.length>0)
						resetVolumeAndPanning(aktMemo, aktMemo.currentAssignedInstrument, aktMemo.assignedSample);
					else
					{
						aktMemo.currentAssignedInstrumentIndex = aktMemo.assignedInstrumentIndex;
						aktMemo.currentAssignedInstrument = aktMemo.assignedInstrument;
						if (aktMemo.currentSample!=null) aktMemo.assignedSample = aktMemo.currentSample;
						hasInstrument = false;
					}
				}
				else
				if (isSTM) // Ignore sample change
				{
					aktMemo.currentAssignedInstrumentIndex = aktMemo.assignedInstrumentIndex;
					aktMemo.currentAssignedInstrument = aktMemo.assignedInstrument;
					if (aktMemo.currentSample!=null) aktMemo.assignedSample = aktMemo.currentSample;
					hasInstrument = false;
				}
				else
				if (isIT)
				{
					if (aktMemo.currentSample!=aktMemo.assignedSample) // set sample - but also perform porta2note
					{
						aktMemo.currentSample = aktMemo.assignedSample;
						resetForNewSample(aktMemo);
					}
					else
					{
						// Old Instrument but new Sample (what a swap)
						resetVolumeAndPanning(aktMemo, aktMemo.assignedInstrument, aktMemo.assignedSample);
						resetEnvelopes(aktMemo, aktMemo.assignedInstrument);
					}
				}
			}
			else // only new Instrument, no Porta2Note: reset only volume and panning for now
			{
				resetVolumeAndPanning(aktMemo, aktMemo.currentAssignedInstrument, aktMemo.assignedSample);
			}
		}

		// Now safe those instruments for later re-use
		aktMemo.assignedInstrumentIndex = aktMemo.currentAssignedInstrumentIndex;
		aktMemo.assignedInstrument = aktMemo.currentAssignedInstrument;

		// Key Off, Note Cut, Note Fade or Period / noteIndex to set?
		if (isKeyOff)
		{
			doKeyOff(aktMemo);
		}
		else
		if (element.getPeriod()==ModConstants.NOTE_CUT || element.getNoteIndex()==ModConstants.NOTE_CUT)
		{
			doNoteCut(aktMemo);
		}
		else
		if (element.getPeriod()==ModConstants.NOTE_FADE || element.getNoteIndex()==ModConstants.NOTE_FADE)
		{
			initNoteFade(aktMemo);
		}
		else
		if ((isNewNote ||											// if there is a note, we need to calc the new tuning and activate a previous set instrument
			hasInstrument) &&										// but with Scream Tracker like mods, the old note value is used, if an instrument is set
			(!isPortaToNoteEffect || aktMemo.instrumentFinished)	// but ignore this if porta to note, except when the instrument finished
			)
		{
			final int savedNoteIndex = aktMemo.assignedNoteIndex; // save the noteIndex - if it is changed by an instrument, we use that one to generate the period, but set it back then
			boolean useFilter = !globalFilterMode;
			boolean newInstrumentWasSet = false;

			// because of sample offset (S3M recall old offset), reset to zero, if sample is set.
			if (isS3M && hasInstrument) aktMemo.prevSampleOffset = 0;

			// We have an instrument/sample assigned, so there was (once) an instrument set!
			if (aktMemo.assignedInstrument!=null || aktMemo.assignedInstrumentIndex>0)
			{
				// now the correct note index from the mapping table, if we have an instrument and a valid note index
				// the sample was already read before
				if (aktMemo.assignedInstrument!=null && aktMemo.assignedNoteIndex>0)
				{
					aktMemo.assignedNoteIndex = aktMemo.assignedInstrument.getNoteIndex(aktMemo.assignedNoteIndex-1)+1;
					// Now set filters from instrument for IT
					if (isIT) useFilter = setFilterAndRandomVariations(aktMemo, aktMemo.assignedInstrument, useFilter);
				}

				if (aktMemo.assignedSample!=null)
				{
					// Reset all pointers, if it's a new one...
					// or with IT: play sample even without note - but not only if it is
					// a new one but the same, and it's finished / silent...
					if (aktMemo.currentSample!=aktMemo.assignedSample)
					{
						// Now activate new Instrument...
						aktMemo.currentSample = aktMemo.assignedSample;
						//aktMemo.assignedSample = null;
						resetForNewSample(aktMemo);
						newInstrumentWasSet = true;
					}
					// With Scream Tracker this has to be checked! Always!
					// IT-MODS (and derivates) reset here, because a sample set is relevant (see below)
					if (aktMemo.instrumentFinished || isNewNote)
					{
						aktMemo.noteCut = aktMemo.keyOff = aktMemo.noteFade = false;
						aktMemo.tempVolEnv = aktMemo.tempPanEnv = aktMemo.tempPitchEnv = -1;
						resetInstrumentPointers(aktMemo, false);
						resetEnvelopes(aktMemo);
						resetAutoVibrato(aktMemo, aktMemo.currentSample);
					}
				}
			}

			if (!isPortaToNoteEffect ||
				(isPortaToNoteEffect && newInstrumentWasSet)) // With IT, if a new sample is set, ignore porta to note-->set it
			{
				// Now set the player Tuning and reset some things in advance.
				// normally we are here, because a note was set in the pattern.
				// Except for IT-MODs - then we are here, because either note or
				// instrument were set. If no note value was set, the old
				// note value is to be used.
				// However, we do not reset the instrument here - the reset was
				// already done above - so this is here for all sane players :)
				if (isNewNote || newInstrumentWasSet)
				{
					setNewPlayerTuningFor(aktMemo, aktMemo.currentNotePeriod = getFineTunePeriod(aktMemo));
					// With S3Ms (STMs?) the port2target is the last seen note value
					if (isS3M || isSTM) aktMemo.portaTargetNotePeriod = aktMemo.currentNotePeriod;
				}
				// and set the resonance, settings were stored above in instr. value copy
				if ((/*aktMemo.resonance>0 || */aktMemo.cutOff<0x7F) && useFilter) setupChannelFilter(aktMemo, true, 256);
				if (isNewNote && !isPortaToNoteEffect) reset_VibTremPan_TablePositions(aktMemo); // IT resets vibrato table position with a new note (and only that position)
			}
			// write back, if noteIndex was changed by instrument note mapping
			aktMemo.assignedNoteIndex = savedNoteIndex;
		}
	}
	/**
	 * Set the effect memory of non parameter effect at S3M
	 * @since 02.08.2024
	 * @param aktMemo
	 * @param param
	 */
	private void setS3MParameterMemory(final ChannelMemory aktMemo, final int param)
	{
		aktMemo.volumSlideValue = param;						// Dxy / Kxy / Lxy
		aktMemo.portaStepUp = param;							// Exx / Fxx
		aktMemo.portaStepDown = param;							// Exx / Fxx
		aktMemo.tremorOntimeSet = (param>>4)&0xF;				// Ixy
		aktMemo.tremorOfftimeSet = (param&0xF);
		if ((mod.getSongFlags() & ModConstants.SONG_ITOLDEFFECTS)!=0)
		{
			aktMemo.tremorOntimeSet++;
			aktMemo.tremorOfftimeSet++;
		}
		aktMemo.arpeggioParam = param;							// Jxy
		aktMemo.retrigMemo = param&0xF;							// Qxy
		aktMemo.retrigVolSlide = (param>>4)&0xF;
		if ((param>>4)!=0) aktMemo.tremoloStep = param>>4;		// Rxy
		if ((param&0xF)!=0) aktMemo.tremoloAmplitude = param&0xF;
		aktMemo.S_Effect_Memory = param;						// Sxy
		if (isNotITCompatMode) aktMemo.IT_EFG = param;			// when not IT Compat Mode!
	}
	/**
	 * @param aktMemo
	 * @see de.quippy.javamod.multimedia.mod.mixer.BasicModMixer#doRowEffects(de.quippy.javamod.multimedia.mod.mixer.BasicModMixer.ChannelMemory)
	 */
	@Override
	protected void doRowEffects(final ChannelMemory aktMemo)
	{
		if (aktMemo.tremorWasActive)
		{
			aktMemo.currentVolume = aktMemo.currentInstrumentVolume;
			aktMemo.tremorWasActive = false;
			aktMemo.doFastVolRamp = true;
		}

		if (aktMemo.assignedEffekt==0) return;

		final Instrument ins = aktMemo.assignedInstrument;
		final PatternElement element = aktMemo.currentElement;

		if (isS3M && aktMemo.assignedEffektParam!=0)
			setS3MParameterMemory(aktMemo, aktMemo.assignedEffektParam);

		switch (aktMemo.assignedEffekt)
		{
			case 0x00:			// no effect, only effect OP is set
				break;
			case 0x01:			// SET SPEED
				if ((mod.getSongFlags()&ModConstants.SONG_ST2TEMPO)!=0)
				{
					int newTempo = aktMemo.assignedEffektParam;
					if (isSTM)
					{
						if (newTempo==0) break;
						if ((mod.getVersion()&0x0F)<21) // set Tempo needs correction, depending on stm version.
							newTempo = ((newTempo/10)<<4)+(newTempo%10);
					}
					currentTick = currentTempo = ((newTempo>>4)!=0?newTempo>>4:1);
					currentBPM = ModConstants.convertST2tempo(newTempo);
				}
				else
				{
					currentTick = currentTempo = aktMemo.assignedEffektParam;
				}
				break;
			case 0x02:			// Pattern position jump
				patternBreakPatternIndex = calculateExtendedValue(aktMemo, null);
				patternBreakRowIndex = 0;
				patternBreakSet = true;
				break;
			case 0x03:			// Pattern break
				if (!(isS3M && aktMemo.assignedEffektParam>64)) // ST3 ignores illegal pattern breaks
				{
					patternBreakRowIndex = calculateExtendedValue(aktMemo, null);
					patternBreakSet = true;
				}
				break;
			case 0x04:			// Volume Slide
				if (aktMemo.assignedEffektParam!=0) aktMemo.volumSlideValue = aktMemo.assignedEffektParam;
				// Fine Volume Up/Down and FastSlides
				if (isFineSlide(aktMemo.volumSlideValue) || (mod.getSongFlags()&ModConstants.SONG_FASTVOLSLIDES)!=0)
					doVolumeSlideEffekt(aktMemo);
				break;
			case 0x05:			// Porta Down
				if (aktMemo.assignedEffektParam!=0)
				{
					aktMemo.portaStepDown = aktMemo.assignedEffektParam;
					if (isNotITCompatMode) aktMemo.IT_EFG = aktMemo.portaStepDown;
				}
				doPortaDown(aktMemo, true, false);
				break;
			case 0x06:			// Porta Up
				if (aktMemo.assignedEffektParam!=0)
				{
					aktMemo.portaStepUp=aktMemo.assignedEffektParam;
					if (isNotITCompatMode) aktMemo.IT_EFG = aktMemo.portaStepUp;
				}
				doPortaUp(aktMemo, true, false);
				break;
			case 0x07: 			// Porta To Note
				if (hasNewNote(element)) aktMemo.portaTargetNotePeriod = getFineTunePeriod(aktMemo);
				if (aktMemo.assignedEffektParam!=0)
				{
					aktMemo.portaNoteStep = aktMemo.assignedEffektParam;
					if (isNotITCompatMode) aktMemo.IT_EFG = aktMemo.portaNoteStep;
				}
				break;
			case 0x08:			// Vibrato
				if (isSTM && aktMemo.assignedEffektParam==0) break; // Tick Zero effect
				if ((aktMemo.assignedEffektParam>>4)!=0) aktMemo.vibratoStep = aktMemo.assignedEffektParam>>4;
				if ((aktMemo.assignedEffektParam&0xF)!=0) aktMemo.vibratoAmplitude = (aktMemo.assignedEffektParam&0xF)<<2;
				aktMemo.vibratoOn = true;
				doVibratoEffekt(aktMemo, false);
				break;
			case 0x09:			// Tremor
				if (aktMemo.assignedEffektParam!=0)
				{
					aktMemo.currentInstrumentVolume = aktMemo.currentVolume;
					aktMemo.tremorOntimeSet = (aktMemo.assignedEffektParam>>4);
					aktMemo.tremorOfftimeSet = (aktMemo.assignedEffektParam&0xF);
					if ((mod.getSongFlags() & ModConstants.SONG_ITOLDEFFECTS)!=0)
					{
						aktMemo.tremorOntimeSet++;
						aktMemo.tremorOfftimeSet++;
					}
				}
				if (aktMemo.tremorOntimeSet==0) aktMemo.tremorOntimeSet=1;
				if (aktMemo.tremorOfftimeSet==0) aktMemo.tremorOfftimeSet=1;
				doTremorEffekt(aktMemo);
				break;
			case 0x0A:			// Arpeggio
				if (aktMemo.assignedEffektParam!=0) aktMemo.arpeggioParam = aktMemo.assignedEffektParam;
				if (aktMemo.assignedNoteIndex!=0)
				{
					if (isSTM || isS3M)
					{
						aktMemo.arpeggioNote[0] = getFineTunePeriod(aktMemo);
						aktMemo.arpeggioNote[1] = getFineTunePeriod(aktMemo, aktMemo.assignedNoteIndex + (aktMemo.arpeggioParam >>4));
						aktMemo.arpeggioNote[2] = getFineTunePeriod(aktMemo, aktMemo.assignedNoteIndex + (aktMemo.arpeggioParam&0xF));
					}
					else
						aktMemo.arpeggioNote[0] = aktMemo.currentNotePeriod;

					aktMemo.arpeggioIndex=0;
				}
				break;
			case 0x0B:			// Vibrato + Volume Slide
				aktMemo.vibratoOn = true;
				doVibratoEffekt(aktMemo, false);
				// Fine Volume Up/Down and FastSlides
				if (aktMemo.assignedEffektParam!=0) aktMemo.volumSlideValue = aktMemo.assignedEffektParam;
				if (isFineSlide(aktMemo.volumSlideValue) || (mod.getSongFlags()&ModConstants.SONG_FASTVOLSLIDES)!=0)
					doVolumeSlideEffekt(aktMemo);
				break;
			case 0x0C:			// Porta To Note + VolumeSlide
				if (hasNewNote(element)) aktMemo.portaTargetNotePeriod = getFineTunePeriod(aktMemo);
				// Fine Volume Up/Down and FastSlides
				if (aktMemo.assignedEffektParam!=0) aktMemo.volumSlideValue = aktMemo.assignedEffektParam;
				if (isFineSlide(aktMemo.volumSlideValue) || (mod.getSongFlags()&ModConstants.SONG_FASTVOLSLIDES)!=0)
					doVolumeSlideEffekt(aktMemo);
				break;
			case 0x0D:			// Set Channel Volume
				aktMemo.channelVolume = aktMemo.assignedEffektParam;
				if (aktMemo.channelVolume>ModConstants.MAXSAMPLEVOLUME) aktMemo.channelVolume = ModConstants.MAXSAMPLEVOLUME;
				break;
			case 0x0E:			// Channel Volume Slide
				if (aktMemo.assignedEffektParam!=0) aktMemo.channelVolumSlideValue = aktMemo.assignedEffektParam;
				// Fine Volume Up/Down and FastSlides
				if (isFineSlide(aktMemo.channelVolumSlideValue) || (mod.getSongFlags()&ModConstants.SONG_FASTVOLSLIDES)!=0)
					doChannelVolumeSlideEffekt(aktMemo);
				break;
			case 0x0F: 			// Sample Offset
				final AtomicInteger rowsUsed = new AtomicInteger(0);
				final int newSampleOffset = calculateExtendedValue(aktMemo, rowsUsed);
				if (newSampleOffset!=0)
				{
					if (rowsUsed.get()==0) // old behavior
					{
						aktMemo.sampleOffset = aktMemo.highSampleOffset<<16 | newSampleOffset<<8;
//						aktMemo.highSampleOffset = 0; // TODO: set zero after usage?!
					}
					else
						aktMemo.sampleOffset = newSampleOffset;
				}
				aktMemo.prevSampleOffset = aktMemo.sampleOffset;
				doSampleOffsetEffekt(aktMemo, element);
				break;
			case 0x10:			// Panning Slide
				if (aktMemo.assignedEffektParam!=0) aktMemo.panningSlideValue = aktMemo.assignedEffektParam;
				if (isFineSlide(aktMemo.panningSlideValue))
					doPanningSlideEffekt(aktMemo);
				break;
			case 0x11:			// Retrig Note
				if ((aktMemo.assignedEffektParam&0xF)!=0)
				{
					aktMemo.retrigMemo = aktMemo.assignedEffektParam&0xF;
					aktMemo.retrigVolSlide = aktMemo.assignedEffektParam>>4;
				}
				doRetrigNote(aktMemo, aktMemo.retrigCount!=-1); // with retrigCount we indicate a continues retrigger. If that is the case, retrigger also on Tick Zero. It is reset in setNewInstrumentAndPeriod
				aktMemo.retrigCount = 0; // != -1
				break;
			case 0x12:			// Tremolo
				if ((aktMemo.assignedEffektParam>>4)!=0) aktMemo.tremoloStep = aktMemo.assignedEffektParam>>4;
				if ((aktMemo.assignedEffektParam&0xF)!=0) aktMemo.tremoloAmplitude = aktMemo.assignedEffektParam&0xF;
				aktMemo.tremoloOn = true;
				doTremoloEffekt(aktMemo);
				break;
			case 0x13: 			// Extended
				final int effektParam = (aktMemo.assignedEffektParam==0) ? aktMemo.S_Effect_Memory : (aktMemo.S_Effect_Memory=aktMemo.assignedEffektParam);
				final int effektOpEx = effektParam&0x0F;
				switch (effektParam>>4)
				{
					case 0x1:	// Glissando
						aktMemo.glissando = effektOpEx!=0;
						break;
					case 0x2:	// Set FineTune
						aktMemo.currentFineTune = ModConstants.IT_fineTuneTable[effektOpEx];
						aktMemo.currentFinetuneFrequency = ModConstants.IT_fineTuneTable[effektOpEx];
						setNewPlayerTuningFor(aktMemo, getFineTunePeriod(aktMemo));
						break;
					case 0x3:	// Set Vibrato Type
						aktMemo.vibratoType=effektOpEx&0x3;
						aktMemo.vibratoNoRetrig = ((effektOpEx&0x04)!=0);
						break;
					case 0x4:	// Set Tremolo Type
						aktMemo.tremoloType=effektOpEx&0x3;
						aktMemo.tremoloNoRetrig = ((effektOpEx&0x04)!=0);
						break;
					case 0x5:	// Set Panbrello Type
						aktMemo.panbrelloType=effektOpEx&0x3;
						aktMemo.panbrelloNoRetrig = ((effektOpEx&0x04)!=0);
						break;
					case 0x6:	// Pattern Delay Frame
						if (!isIT && !isModPlug) break; // only IT or ModPlug Mods
						patternTicksDelayCount += effektOpEx; // those add up
						break;
					case 0x7:	// set NNA and others
						if (!isIT && !isModPlug) break; // only IT or ModPlug Mods
						switch (effektOpEx)
						{
							case 0x0: // Note Cut all NNAs of this channel
								doNNAforAllof(aktMemo, ModConstants.NNA_CUT);
								break;
							case 0x1: // Note Off all NNAs of this channel
								doNNAforAllof(aktMemo, ModConstants.NNA_OFF);
								break;
							case 0x2: // Note Fade all NNAs of this channel
								doNNAforAllof(aktMemo, ModConstants.NNA_FADE);
								break;
							case 0x3: // NNA Cut
								aktMemo.tempNNAAction = ModConstants.NNA_CUT;
								break;
							case 0x4: // NNA Continue
								aktMemo.tempNNAAction = ModConstants.NNA_CONTINUE;
								break;
							case 0x5: // NNA Off
								aktMemo.tempNNAAction = ModConstants.NNA_OFF;
								break;
							case 0x6: // NNA Fade
								aktMemo.tempNNAAction = ModConstants.NNA_FADE;
								break;
							case 0x7: // Volume Envelope off
								if (ins!=null)
								{
									final Envelope volEnv = ins.volumeEnvelope;
									if (volEnv !=null) aktMemo.tempVolEnv = 0;
								}
								break;
							case 0x8: // Volume Envelope On
								if (ins!=null)
								{
									final Envelope volEnv = ins.volumeEnvelope;
									if (volEnv !=null) aktMemo.tempVolEnv = 1;
								}
								break;
							case 0x9: // Panning Envelope off
								if (ins!=null)
								{
									final Envelope panEnv = ins.panningEnvelope;
									if (panEnv !=null) aktMemo.tempPanEnv = 0;
								}
								break;
							case 0xA: // Panning Envelope On
								if (ins!=null)
								{
									final Envelope panEnv = ins.panningEnvelope;
									if (panEnv !=null) aktMemo.tempPanEnv = 1;
								}
								break;
							case 0xB: // Pitch Envelope off
								if (ins!=null)
								{
									final Envelope pitEnv = ins.pitchEnvelope;
									if (pitEnv !=null) aktMemo.tempPitchEnv = 0;
								}
								break;
							case 0xC: // Pitch Envelope On
								if (ins!=null)
								{
									final Envelope pitEnv = ins.pitchEnvelope;
									if (pitEnv !=null) aktMemo.tempPitchEnv = 1;
								}
								break;
						}
						break;
					case 0x8:	// Fine Panning
						doPanning(aktMemo, effektOpEx, ModConstants.PanBits.Pan4Bit);
						break;
					case 0x9:	// Sound Control
						if (!isIT && !isModPlug) break; // only IT or ModPlug Mods
						switch (effektOpEx)
						{
							case 0x0: // Disable surround for the current channel
								aktMemo.doSurround = false;
								break;
							case 0x1: //  Enable surround for the current channel. Note that a panning effect will automatically desactive the surround, unless the 4-way (Quad) surround mode has been activated with the S9B effect.
								aktMemo.doSurround = true;
								break;
							// MPT Effects only
//							case 0x8: // Disable reverb for this channel
//								break;
//							case 0x9: // Force reverb for this channel
//								break;
//							case 0xA: // Select mono surround mode (center channel). This is the default
//								break;
//							case 0xB: // Select quad surround mode: this allows you to pan in the rear channels, especially useful for 4-speakers playback. Note that S9A and S9B do not activate the surround for the current channel, it is a global setting that will affect the behavior of the surround for all channels. You can enable or disable the surround for individual channels by using the S90 and S91 effects. In quad surround mode, the channel surround will stay active until explicitely disabled by a S90 effect
//								break;
							case 0xC: // Select global filter mode (IT compatibility). This is the default, when resonant filters are enabled with a Zxx effect, they will stay active until explicitly disabled by setting the cutoff frequency to the maximum (Z7F), and the resonance to the minimum (Z80).
								globalFilterMode = false;
								break;
							case 0xD: // Select local filter mode (MPT beta compatibility): when this mode is selected, the resonant filter will only affect the current note. It will be deactivated when a new note is being played.
								globalFilterMode = true;
								break;
							case 0xE: // Play forward. You may use this to temporarily force the direction of a bidirectional loop to go forward.
								aktMemo.isForwardDirection = true;
								break;
							case 0xF: // Play backward. The current instrument will be played backwards, or it will temporarily set the direction of a loop to go backward.
								if (aktMemo.currentSample!=null && aktMemo.currentSamplePos==0 && aktMemo.currentSample.length>0 &&
									(hasNewNote(element) || (aktMemo.currentSample.loopType&ModConstants.LOOP_ON)!=0))
								{
									aktMemo.currentSamplePos = aktMemo.currentSample.length-1;
									aktMemo.currentTuningPos = 0;
								}
								aktMemo.isForwardDirection = false;
								break;
						}
						break;
					case 0xA:	// set High Offset / S3M: Stereo Control - whatever that was, not supported by Scream Tracker 3
						if (!isIT && !isModPlug) break; // only IT or ModPlug Mods
						aktMemo.highSampleOffset = effektOpEx;
						break;
					case 0xB:	// JumpLoop
						// S3M: this is such an ideocracy way of doing it
						// S3M has a central var for all of this - obviously
						final ChannelMemory aktMemoTmp = (isS3M)?channelMemory[0]:aktMemo;
						if (effektOpEx==0)
						{
							if (!isS3M || aktMemoTmp.jumpLoopPatternRow==-1) // ST3 does not overwrite a row set...
								aktMemoTmp.jumpLoopPatternRow = currentRow;
						}
						else
						{
							if (isS3M && patternJumpSet) break; // obviously other SBx events on the same row will not be executed, only the leftmost one

							if (aktMemoTmp.jumpLoopRepeatCount==-1)
							{
								aktMemoTmp.jumpLoopRepeatCount = effektOpEx;
								if (aktMemoTmp.jumpLoopPatternRow==-1) // if not set, pattern start is default!
									aktMemoTmp.jumpLoopPatternRow = (aktMemoTmp.jumpLoopITLastRow==-1)?mod.getSongRestart():aktMemoTmp.jumpLoopITLastRow;
							}

							if (aktMemoTmp.jumpLoopRepeatCount>0)
							{
								aktMemoTmp.jumpLoopRepeatCount--;
								patternJumpRowIndex = aktMemoTmp.jumpLoopPatternRow;
								patternJumpSet = true;
							}
							else
							{
								aktMemoTmp.jumpLoopPatternRow =
								aktMemoTmp.jumpLoopRepeatCount = -1;
								// remember last position behind SBx, for next SBx without target set
								aktMemoTmp.jumpLoopITLastRow = currentRow + 1;
								if (isS3M)
								{
									// plus for S3M prevent other SBx in the same row by forcing to next row.
									patternJumpRowIndex = aktMemoTmp.jumpLoopITLastRow;
									patternJumpSet = true;
								}
							}
						}
						break;
					case 0xC:	// Note Cut
						if (aktMemo.noteCutCount<0)
						{
							if (effektOpEx!=0) aktMemo.noteCutCount = effektOpEx;
							if (aktMemo.noteCutCount==0)
							{
								if (isIT) aktMemo.noteCutCount=1;
								else
								if (isS3M) aktMemo.noteCutCount=-1;
							}
						}
						break;
					case 0xD:	// Note Delay
						if (aktMemo.noteDelayCount<0) // is done in BasicModMixer::doRowEvents
						{
							if (isIT && effektOpEx==0)
								aktMemo.noteDelayCount=1;
							else
							if (isS3M && effektOpEx==0)
								aktMemo.noteDelayCount=-1;
							else
								aktMemo.noteDelayCount = effektOpEx;
						}
						// Note-Delays are handled centrally in "doRowAndTickEvents"
						break;
					case 0xE:	// Pattern Delay
						if (patternDelayCount<0 && effektOpEx!=0) patternDelayCount=effektOpEx;
						break;
					case 0xF:	// Set Active Macro (s3m: Funk Repeat, not implemented in Scream Tracker 3)
						if (isIT) aktMemo.activeMidiMacro = aktMemo.assignedEffektParam&0x7F;
						break;
					default :
						//Log.debug(String.format("Unknown Extended Effect: Effect:%02X Op:%02X in [Pattern:%03d: Row:%03d Channel:%03d]", Integer.valueOf(aktMemo.effekt), Integer.valueOf(aktMemo.effektParam), Integer.valueOf(currentPatternIndex), Integer.valueOf(currentRow), Integer.valueOf(aktMemo.channelNumber+1)));
						break;
				}
				break;
			case 0x14:			// set Tempo
				int newTempo = calculateExtendedValue(aktMemo, null);
				if (isIT || isModPlug)
				{
					if (newTempo!=0)
						aktMemo.oldTempoParameter = newTempo;
					else
						newTempo = aktMemo.oldTempoParameter;
				}
				if (newTempo>0x20) currentBPM = newTempo;
				if (isModPlug && currentBPM>0x200) currentBPM = 0x200; // 512 for MPT ITex
				else
				if (currentBPM>0xFF) currentBPM = 0xFF;
				break;
			case 0x15:			// Fine Vibrato
				// This effect is identical to the vibrato, but has a 4x smaller amplitude (more precise).
				if ((aktMemo.assignedEffektParam>>4)!=0) aktMemo.vibratoStep = aktMemo.assignedEffektParam>>4;
				if ((aktMemo.assignedEffektParam&0xF)!=0)
				{
					aktMemo.vibratoAmplitude = aktMemo.assignedEffektParam&0xF;
					// s3m: do not distinguish in memory, is done in doVibratoEffekt
					if (isS3M) aktMemo.vibratoAmplitude<<=2;
				}
				aktMemo.vibratoOn = true;
				doVibratoEffekt(aktMemo, true);
				break;
			case 0x16:			// Set Global Volume
				if (aktMemo.assignedEffektParam<=0x80)
				{
					globalVolume = aktMemo.assignedEffektParam;
					// normalize to 0x80 for others except IT
					if (!isIT)
					{
						globalVolume <<= 1;
						if (globalVolume>ModConstants.MAXGLOBALVOLUME) globalVolume = ModConstants.MAXGLOBALVOLUME;
					}
				}
				break;
			case 0x17:			// Global Volume Slide
				if (aktMemo.assignedEffektParam!=0) aktMemo.globalVolumSlideValue = aktMemo.assignedEffektParam;
				if (isFineSlide(aktMemo.globalVolumSlideValue))
					doGlobalVolumeSlideEffekt(aktMemo);
				break;
			case 0x18:			// Set Panning
				doPanning(aktMemo, aktMemo.assignedEffektParam, ModConstants.PanBits.Pan8Bit);
				break;
			case 0x19:			// Panbrello
				if ((aktMemo.assignedEffektParam>>4)!=0) aktMemo.panbrelloStep = aktMemo.assignedEffektParam>>4;
				if ((aktMemo.assignedEffektParam&0xF)!=0) aktMemo.panbrelloAmplitude = aktMemo.assignedEffektParam&0xF;
				aktMemo.panbrelloOn = true;
				doPanbrelloEffekt(aktMemo);
				break;
			case 0x1A:			// Midi Macro
				final MidiMacros macro = mod.getMidiConfig();
				if (macro!=null)
				{
		            if (aktMemo.assignedEffektParam<0x80)
		                processMIDIMacro(aktMemo, false, macro.getMidiSFXExt(aktMemo.activeMidiMacro), aktMemo.assignedEffektParam);
		            else
		                processMIDIMacro(aktMemo, false, macro.getMidiZXXExt(aktMemo.assignedEffektParam & 0x7F), 0);
				}
	            break;
			case 0x1B:			// Parameter Extension
				// OMPT Specific, done as a look ahead, so just break here
				break;
			case 0x1C:			// Smooth Midi Macro
				final MidiMacros smoothMacro = mod.getMidiConfig();
				if (smoothMacro!=null)
				{
		            if (aktMemo.assignedEffektParam<0x80)
		                processMIDIMacro(aktMemo, true, smoothMacro.getMidiSFXExt(aktMemo.activeMidiMacro), aktMemo.assignedEffektParam);
		            else
		                processMIDIMacro(aktMemo, true, smoothMacro.getMidiZXXExt(aktMemo.assignedEffektParam & 0x7F), 0);
				}
	            break;
			default:
				//Log.debug(String.format("Unknown Effect: Effect:%02X Op:%02X in [Pattern:%03d: Row:%03d Channel:%03d]", Integer.valueOf(aktMemo.effekt), Integer.valueOf(aktMemo.effektParam), Integer.valueOf(currentPatternIndex), Integer.valueOf(currentRow), Integer.valueOf(aktMemo.channelNumber+1)));
				break;
		}
	}
	/**
	 * @since 28.03.2024
	 * @param aktMemo
	 * @param slide
	 */
	private void doFreqSlide(final ChannelMemory aktMemo, final int slide)
	{
		if (frequencyTableType==ModConstants.IT_LINEAR_TABLE)
		{
			int slideIndex = (slide<0)?-slide:slide;
			if (slideIndex>255) slideIndex = 255;
			final long oldPeriod = aktMemo.currentNotePeriod;
			if (slide<0)
			{
				aktMemo.currentNotePeriod = (int)((aktMemo.currentNotePeriod * ((long)ModConstants.LinearSlideUpTable[slideIndex]))>>ModConstants.HALFTONE_SHIFT);
				if (oldPeriod == aktMemo.currentNotePeriod) aktMemo.currentNotePeriod--;
			}
			else
			{
				aktMemo.currentNotePeriod = (int)((aktMemo.currentNotePeriod * ((long)ModConstants.LinearSlideDownTable[slideIndex]))>>ModConstants.HALFTONE_SHIFT);
				if (oldPeriod == aktMemo.currentNotePeriod) aktMemo.currentNotePeriod++;
			}
		}
		else
			aktMemo.currentNotePeriod += slide<<ModConstants.PERIOD_SHIFT;

		setNewPlayerTuningFor(aktMemo);
	}
	/**
	 * Different
	 * @since 28.03.2024
	 * @param aktMemo
	 * @param slide
	 */
	private void doExtraFineSlide(final ChannelMemory aktMemo, final int slide)
	{
		if (frequencyTableType==ModConstants.IT_LINEAR_TABLE)
		{
			final int slideIndex = ((slide<0)?-slide:slide)&0x0F;
			if (slide<0)
			{
				aktMemo.currentNotePeriod = (int)((aktMemo.currentNotePeriod * ((long)ModConstants.FineLinearSlideUpTable[slideIndex]))>>ModConstants.HALFTONE_SHIFT);
			}
			else
			{
				aktMemo.currentNotePeriod = (int)((aktMemo.currentNotePeriod * ((long)ModConstants.FineLinearSlideDownTable[slideIndex]))>>ModConstants.HALFTONE_SHIFT);
			}
		}
		else
			aktMemo.currentNotePeriod += slide<<(ModConstants.PERIOD_SHIFT-2);

		setNewPlayerTuningFor(aktMemo);
	}
	/**
	 * @since 28.03.2024
	 * @param aktMemo
	 * @param slide
	 */
	private void doFineSlide(final ChannelMemory aktMemo, final int slide)
	{
		if (frequencyTableType==ModConstants.IT_LINEAR_TABLE)
		{
			final int slideIndex = ((slide<0)?-slide:slide)&0x0F;
			if (slide<0)
			{
				aktMemo.currentNotePeriod = (int)((aktMemo.currentNotePeriod * ((long)ModConstants.LinearSlideUpTable[slideIndex]))>>ModConstants.HALFTONE_SHIFT);
			}
			else
			{
				aktMemo.currentNotePeriod = (int)((aktMemo.currentNotePeriod * ((long)ModConstants.LinearSlideDownTable[slideIndex]))>>ModConstants.HALFTONE_SHIFT);
			}
		}
		else
			aktMemo.currentNotePeriod += slide<<ModConstants.PERIOD_SHIFT;

		setNewPlayerTuningFor(aktMemo);
	}
	/**
	 * Convenient Method for the Porta Up Effekt
	 * @since 08.06.2020
	 * @param aktMemo
	 */
	private void doPortaUp(final ChannelMemory aktMemo, final boolean firstTick, final boolean inVolColum)
	{
		final int indicatorPortaUp = aktMemo.portaStepUp&0xF0;
		if (inVolColum)
			doFreqSlide(aktMemo, -aktMemo.portaStepUp);
		else
		{
			switch (indicatorPortaUp)
			{
				case 0xE0:
					if (firstTick) doExtraFineSlide(aktMemo, -(aktMemo.portaStepUp&0x0F));
					break;
				case 0xF0:
					if (firstTick) doFineSlide(aktMemo, -(aktMemo.portaStepUp&0x0F));
					break;
				default:
					if (!firstTick) doFreqSlide(aktMemo, -aktMemo.portaStepUp);
					break;
			}
		}
	}
	/**
	 * Convenient Method for the Porta Down Effekt
	 * @since 08.06.2020
	 * @param aktMemo
	 */
	private void doPortaDown(final ChannelMemory aktMemo, final boolean firstTick, final boolean inVolColum)
	{
		final int indicatorPortaUp = aktMemo.portaStepDown&0xF0;
		if (inVolColum)
			doFreqSlide(aktMemo, aktMemo.portaStepDown);
		else
		{
			switch (indicatorPortaUp)
			{
				case 0xE0:
					if (firstTick) doExtraFineSlide(aktMemo, aktMemo.portaStepDown&0x0F);
					break;
				case 0xF0:
					if (firstTick) doFineSlide(aktMemo, aktMemo.portaStepDown&0x0F);
					break;
				default:
					if (!firstTick) doFreqSlide(aktMemo, aktMemo.portaStepDown);
					break;
			}
		}
	}
	/**
	 * Convenient Method for the Porta to note Effekt
	 * @param aktMemo
	 */
	private void doPortaToNoteEffekt(final ChannelMemory aktMemo)
	{
		if (aktMemo.portaTargetNotePeriod!=aktMemo.currentNotePeriod && aktMemo.portaTargetNotePeriod!=-1)
		{
			if (aktMemo.portaTargetNotePeriod<aktMemo.currentNotePeriod)
			{
				doFreqSlide(aktMemo, -aktMemo.portaNoteStep);
				if (aktMemo.currentNotePeriod<=aktMemo.portaTargetNotePeriod)
				{
					aktMemo.currentNotePeriod=aktMemo.portaTargetNotePeriod;
					aktMemo.portaTargetNotePeriod=-1;
				}
			}
			else
			{
				doFreqSlide(aktMemo, aktMemo.portaNoteStep);
				if (aktMemo.currentNotePeriod>=aktMemo.portaTargetNotePeriod)
				{
					aktMemo.currentNotePeriod=aktMemo.portaTargetNotePeriod;
					aktMemo.portaTargetNotePeriod=-1;
				}
			}
			if (aktMemo.glissando)
				setNewPlayerTuningFor(aktMemo, getRoundedPeriod(aktMemo, aktMemo.currentNotePeriod>>ModConstants.PERIOD_SHIFT)<<ModConstants.PERIOD_SHIFT);
			else
				setNewPlayerTuningFor(aktMemo);
		}
	}
	/**
	 * returns values in the range of -64..64
	 * @since 29.06.2020
	 * @param type
	 * @param position
	 * @return
	 */
	private int getVibratoDelta(final int type, int position)
	{
		position &= 0xFF;
		switch (type & 0x03)
		{
			default:
			case 0: //Sinus
				return ModConstants.ITSinusTable[position];
			case 1: // Ramp Down / Sawtooth
				return ModConstants.ITRampDownTable[position];
			case 2: // Squarewave
				return ModConstants.ITSquareTable[position];
			case 3: // random
				return (int)(128 * swinger.nextDouble() - 0x40);
		}
	}
	/**
	 * @param aktMemo
	 * @param currentSample
	 * @param currentPeriod
	 * @see de.quippy.javamod.multimedia.mod.mixer.BasicModMixer#doAutoVibratoEffekt(de.quippy.javamod.multimedia.mod.mixer.BasicModMixer.ChannelMemory, de.quippy.javamod.multimedia.mod.loader.instrument.Sample)
	 */
	@Override
	protected void doAutoVibratoEffekt(final ChannelMemory aktMemo, final Sample currentSample, final int currentPeriod)
	{
		if (currentSample.vibratoRate==0) return;
		final int maxDepth = currentSample.vibratoDepth<<8;
		int periodAdd = 0;

//		if (config.ITVibratoTremoloPanbrello)
//		{
			// Schism / OpenMPT implementation adopted
			final int position = aktMemo.autoVibratoTablePos & 0xFF;
			// sweep = rate<<2, rate = speed, depth = depth
			int depth = aktMemo.autoVibratoAmplitude;
			depth += currentSample.vibratoSweep&0xFF;
			if (depth>maxDepth) depth = maxDepth;
			aktMemo.autoVibratoAmplitude = depth;
			depth >>= 8;

			aktMemo.autoVibratoTablePos += currentSample.vibratoRate;
			switch (currentSample.vibratoType & 0x07)
			{
				default:
				case 0:	periodAdd = ModConstants.ITSinusTable[position];		// Sine
						break;
				case 1:	periodAdd = position<128? 0x40:0;						// Square
						break;
				case 2:	periodAdd = ((position + 1)>>1) - 0x40;					// Ramp Up
						break;
				case 3:	periodAdd = 0x40 - ((position + 1)>>1);					// Ramp Down
						break;
				case 4:	periodAdd = (int)(128 * swinger.nextDouble() - 0x40);	// Random
						break;
			}
			periodAdd = (periodAdd * depth) >> 6;

			final int[] linearSlideTable = (periodAdd<0)?ModConstants.LinearSlideUpTable:ModConstants.LinearSlideDownTable;
			final int[] fineLinearSlideTable = (periodAdd<0)?ModConstants.FineLinearSlideUpTable:ModConstants.FineLinearSlideDownTable;
			final int slideIndex = (periodAdd<0)?-periodAdd:periodAdd;
			if (slideIndex<16)
				periodAdd = ((int)((currentPeriod * (long)fineLinearSlideTable[slideIndex])>>ModConstants.HALFTONE_SHIFT)) - currentPeriod;
			else
				periodAdd = ((int)((currentPeriod * (long)linearSlideTable[slideIndex>>2])>>ModConstants.HALFTONE_SHIFT)) - currentPeriod;

			setNewPlayerTuningFor(aktMemo, currentPeriod - periodAdd);
//		}
//		else // ModPlug does this quit differently, but is only used if set via config - we need to read that TODO: read config for this!
//		{
//			aktMemo.autoVibratoAmplitude += currentSample.vibratoSweep<<1;
//			if (aktMemo.autoVibratoAmplitude>maxDepth) aktMemo.autoVibratoAmplitude = maxDepth;
//
//			aktMemo.autoVibratoTablePos += currentSample.vibratoRate;
//			switch (currentSample.vibratoType & 0x07)
//			{
//				default:
//				case 0:	periodAdd = -ModConstants.ITSinusTable[aktMemo.autoVibratoTablePos & 0xFF];		// Sine
//						break;
//				case 1:	periodAdd = (aktMemo.autoVibratoTablePos&0x80)!=0? 0x40:-0x40;					// Square
//						break;
//				case 2:	periodAdd = ((0x40 + (aktMemo.autoVibratoTablePos>>1)) & 0x7F) - 0x40;			// Ramp Up
//						break;
//				case 3:	periodAdd = ((0x40 - (aktMemo.autoVibratoTablePos>>1)) & 0x7F) - 0x40;			// Ramp Down
//						break;
//				case 4:	periodAdd = ModConstants.ModRandomTable[aktMemo.autoVibratoTablePos & 0x3F];	// Random
//						break;
//			}
//			int n = (periodAdd * aktMemo.autoVibratoAmplitude) >> 8;
//
//			int[] linearSlideTable;
//			if (n < 0)
//			{
//				n = -n;
//				linearSlideTable = ModConstants.LinearSlideUpTable;
//			}
//			else
//				linearSlideTable = ModConstants.LinearSlideDownTable;
//			final int n1 = n>>8;
//			final long df1 = linearSlideTable[n1];
//			final long df2 = linearSlideTable[n1+1];
//			n>>=2;
//			setNewPlayerTuningFor(aktMemo, (int)((currentPeriod * (df1 + (((df2 - df1) * ((long)n & 0x3F)) >> 6)))>>ModConstants.HALFTONE_SHIFT));
//		}
	}
	/**
	 * Convenient Method for the vibrato effekt
	 * @param aktMemo
	 */
	protected void doVibratoEffekt(final ChannelMemory aktMemo, final boolean doFineVibrato)
	{
		final boolean isTick0 = currentTick==currentTempo;
		final boolean oldITEffects = (mod.getSongFlags()&ModConstants.SONG_ITOLDEFFECTS)!=0;

		final int vibPos = aktMemo.vibratoTablePos & 0xFF;
		int periodAdd = getVibratoDelta(aktMemo.vibratoType, vibPos);

		int vdepth = 6;
//		if (config.ITVibratoTremoloPanbrello) // TODO: Config for IT / MPTP files - we need to read that.
//		{
		if (isIT && oldITEffects) // With old effects two times deeper and reversed
		{
			vdepth = 5;
			periodAdd = -periodAdd;
		}
		if (isS3M || isSTM)
		{
			if ((mod.getSongFlags()&ModConstants.SONG_ST2VIBRATO)!=0)
				vdepth = 5;
			// with s3m vibrato types are equal in effect memory - fine slide is done here...
			if (isS3M && doFineVibrato)
				vdepth += 2; // same result as periodAdd>>=2;
		}

		periodAdd = (periodAdd * aktMemo.vibratoAmplitude) >> vdepth; // more or less the same as "/(1<<attenuaton)" :)

		if (frequencyTableType==ModConstants.IT_LINEAR_TABLE)
		{
			int slideIndex = (periodAdd<0)?-periodAdd:periodAdd;
			if (slideIndex>(255<<2)) slideIndex = (255<<2);
			final long period = aktMemo.currentNotePeriod;

			// Formula: ((period*table[index / 4])-period) + ((period*fineTable[index % 4])-period)
			if (periodAdd<0)
			{
				periodAdd = (int)(((period * (ModConstants.LinearSlideUpTable[slideIndex>>2]))>>ModConstants.HALFTONE_SHIFT) - period);
				if ((slideIndex&0x03)!=0) periodAdd += (int)(((period * (ModConstants.FineLinearSlideUpTable[slideIndex&0x3]))>>ModConstants.HALFTONE_SHIFT) - period);
			}
			else
			{
				periodAdd = (int)(((period * (ModConstants.LinearSlideDownTable[slideIndex>>2]))>>ModConstants.HALFTONE_SHIFT) - period);
				if ((slideIndex&0x03)!=0) periodAdd += (int)(((period * (ModConstants.FineLinearSlideDownTable[slideIndex&0x3]))>>ModConstants.HALFTONE_SHIFT) - period);
			}
			setNewPlayerTuningFor(aktMemo, aktMemo.currentNotePeriod - periodAdd);
		}
		else
			setNewPlayerTuningFor(aktMemo, aktMemo.currentNotePeriod - (periodAdd<<2));

		if (!isTick0 || (isIT && !oldITEffects)) aktMemo.vibratoTablePos = (vibPos + (aktMemo.vibratoStep<<2))&0xFF;
	}
	/**
	 * Convenient Method for the panbrello effekt
	 * @param aktMemo
	 */
	protected void doPanbrelloEffekt(final ChannelMemory aktMemo)
	{
		int pDelta = getVibratoDelta(aktMemo.panbrelloType, aktMemo.panbrelloTablePos);
		// IT has a more precise table from 0..64
		// With s3m and stm we need values from 0..256
		// but we shift only one (0..128) because the back shift is only 3, not 4 (see at XM)
		if (!isIT) pDelta<<=1;

		if (isIT && aktMemo.panbrelloType==3) // Random type
		{
			// IT compatibility: Sample-and-hold style random panbrello (tremolo and vibrato don't use this mechanism in IT)
			if (aktMemo.panbrelloTablePos==0 || aktMemo.panbrelloTablePos>=aktMemo.panbrelloStep)
			{
				aktMemo.panbrelloTablePos = 0;
				aktMemo.panbrelloRandomMemory = pDelta;
			}
			aktMemo.panbrelloTablePos++;
			pDelta = aktMemo.panbrelloRandomMemory;
		}
		else
			aktMemo.panbrelloTablePos += aktMemo.panbrelloStep;

		final int newPanning = aktMemo.currentInstrumentPanning + (((pDelta * aktMemo.panbrelloAmplitude) + 2) >> 3); // +2: round me at bit 1
		aktMemo.panning = (newPanning<0)?0:((newPanning>256)?256:newPanning);
		aktMemo.doFastVolRamp=true;
	}
	/**
	 * Convenient Method for the tremolo effekt
	 * @param aktMemo
	 */
	protected void doTremoloEffekt(final ChannelMemory aktMemo)
	{
		final boolean isTick0 = currentTick==currentTempo;
		final boolean oldITEffects = (mod.getSongFlags()&ModConstants.SONG_ITOLDEFFECTS)!=0;
		// What the... ITs do not reset the tremolo table pos, when not set to use oldITEffects
		if (isTick0 && hasNewNote(aktMemo.currentElement) && !aktMemo.tremoloNoRetrig && (!isIT || oldITEffects)) aktMemo.tremoloTablePos = 0;

		if (aktMemo.currentVolume>0 || isIT)
		{
			final int delta = getVibratoDelta(aktMemo.tremoloType, aktMemo.tremoloTablePos);
			aktMemo.currentVolume = aktMemo.currentInstrumentVolume + ((delta * aktMemo.tremoloAmplitude) >> 5); // normally >>6 because -64..+64
			if (aktMemo.currentVolume>ModConstants.MAX_SAMPLE_VOL) aktMemo.currentVolume = ModConstants.MAX_SAMPLE_VOL;
			else
			if (aktMemo.currentVolume<ModConstants.MIN_SAMPLE_VOL) aktMemo.currentVolume = ModConstants.MIN_SAMPLE_VOL;
			aktMemo.doFastVolRamp = true;
		}
		if (!isTick0 || (isIT && !oldITEffects)) aktMemo.tremoloTablePos += aktMemo.tremoloStep<<2;
	}
	/**
	 * The tremor effekt
	 * @param aktMemo
	 */
	protected void doTremorEffekt(final ChannelMemory aktMemo)
	{
		aktMemo.tremorWasActive = true;
		// if both are not set, set to current values
		// see also commented reset after offtime reached
		if (aktMemo.tremorOntime<=0 && aktMemo.tremorOfftime<=0)
		{
			aktMemo.tremorOntime = aktMemo.tremorOntimeSet;
			aktMemo.tremorOfftime = aktMemo.tremorOfftimeSet;
		}

		if (aktMemo.tremorOntime>0)
		{
			aktMemo.tremorOntime--;
			// set Offtime to current value set.
			if (aktMemo.tremorOntime<=0) aktMemo.tremorOfftime = aktMemo.tremorOfftimeSet;
			aktMemo.currentVolume = aktMemo.currentInstrumentVolume;
			aktMemo.doFastVolRamp = true;
		}
		else
		if (aktMemo.tremorOfftime>0)
		{
			aktMemo.tremorOfftime--;
			// asynchronous! - in next row new values for ontime/offtime can be set
			// we need to take a look into next row first, so don't do this...
			//if (aktMemo.tremorOfftime<=0) aktMemo.tremorOntime = aktMemo.tremorOntimeSet;
			aktMemo.currentVolume = 0;
			aktMemo.doFastVolRamp = true;
		}
	}
	/**
	 * Will validate the new sampleOffset and do the S3M magic
	 * @since 05.08.2024
	 * @param aktMemo
	 * @param newSampleOffset
	 * @return
	 */
	protected int validateNewSampleOffset(final ChannelMemory aktMemo, final int newSampleOffset)
	{
		final Sample sample = aktMemo.currentSample;
		final boolean hasLoop = (sample.loopType & ModConstants.LOOP_ON)!=0;
		final int length = hasLoop?sample.loopStop:sample.length;

		if (newSampleOffset >= length)
		{
			if (isS3M)
			{
				// ST3 Compatibility: Don't play note if offset is beyond sample length (non-looped samples only)
				// else do offset wrap-around - does this in GUS mode, not in SoundBlaster mode
				if (!hasLoop || (mod.getSongFlags() & ModConstants.SONG_S3M_GUS)==0)
					return length-1;
				else
					return ((newSampleOffset - sample.loopStart) % sample.loopLength) + sample.loopStart;
			}
			else
			if (isIT)
			{
				if ((mod.getSongFlags() & ModConstants.SONG_ITOLDEFFECTS)!=0) // Old Effects
					return length-1;
				else
					return 0; // reset to start
			}
			else
			{
				if (hasLoop)
					return sample.loopStart;
				else
					return length-1;
			}
		}
		return newSampleOffset;
	}
	/**
	 * @since 03.07.2020
	 * @param aktMemo
	 * @param element
	 */
	protected void doSampleOffsetEffekt(final ChannelMemory aktMemo, final PatternElement element)
	{
		if (hasNoNote(element) || aktMemo.currentSample==null || aktMemo.sampleOffset==-1) return;

		// IT compatibility: If this note is not mapped to a sample, ignore it.
		// It is questionable, if this check is needed - aktMemo.currentSample should already be null...
		// BTW: aktMemo.assignedSample is null then, too
//		if (isIT)
//		{
//			if (aktMemo.currentAssignedInstrument!=null)
//			{
//				final int sampleIndex = aktMemo.currentAssignedInstrument.getSampleIndex(aktMemo.assignedNoteIndex-1);
//				if (sampleIndex<=0 || sampleIndex>mod.getNSamples()) return;
//			}
//		}

		aktMemo.currentSamplePos = validateNewSampleOffset(aktMemo, aktMemo.sampleOffset);
	}
	/**
	 * With IT Mods arpeggios refer to the current pitch (currentNotePeriod)
	 * and cannot be calculated in advance. We need to do that here, when
	 * it is needed. Formular is (currentNotePeriod / 2^(halftone/12))
	 * @since 06.06.2020
	 * @param aktMemo
	 */
	protected void doArpeggioEffekt(final ChannelMemory aktMemo)
	{
		aktMemo.arpeggioIndex = (aktMemo.arpeggioIndex+1)%3;
		int nextNotePeriod = 0;
		if (isIT)
		{
			if (aktMemo.arpeggioIndex==0)
				nextNotePeriod = aktMemo.currentNotePeriod;
			else
			{
				final long factor = ModConstants.halfToneTab[(aktMemo.arpeggioIndex==1)?(aktMemo.arpeggioParam>>4):(aktMemo.arpeggioParam&0xF)];
				nextNotePeriod = (int)(((aktMemo.currentNotePeriod) * factor)>>ModConstants.HALFTONE_SHIFT);
			}
		}
		else
		{
			nextNotePeriod = aktMemo.arpeggioNote[aktMemo.arpeggioIndex];
		}
		if (nextNotePeriod!=0) setNewPlayerTuningFor(aktMemo, nextNotePeriod);
	}
	/**
	 * Convenient Method for the VolumeSlide Effekt
	 * @param aktMemo
	 */
	protected void doVolumeSlideEffekt(final ChannelMemory aktMemo)
	{
		aktMemo.currentVolume += getFineSlideValue(aktMemo.volumSlideValue);
		if (aktMemo.currentVolume>ModConstants.MAX_SAMPLE_VOL) aktMemo.currentVolume = ModConstants.MAX_SAMPLE_VOL;
		else
		if (aktMemo.currentVolume<ModConstants.MIN_SAMPLE_VOL) aktMemo.currentVolume = ModConstants.MIN_SAMPLE_VOL;
		aktMemo.currentInstrumentVolume = aktMemo.currentVolume;
		aktMemo.doFastVolRamp = true;
	}
	/**
	 * Same as the volumeSlide, but affects the channel volume
	 * @since 21.06.2006
	 * @param aktMemo
	 */
	protected void doChannelVolumeSlideEffekt(final ChannelMemory aktMemo)
	{
		aktMemo.channelVolume += getFineSlideValue(aktMemo.channelVolumSlideValue);
		if (aktMemo.channelVolume>ModConstants.MAXSAMPLEVOLUME) aktMemo.channelVolume = ModConstants.MAXSAMPLEVOLUME;
		else
		if (aktMemo.channelVolume<0) aktMemo.channelVolume = 0;
	}
	/**
	 * Convenient Method for the panning slide Effekt
	 * @param aktMemo
	 */
	protected void doPanningSlideEffekt(final ChannelMemory aktMemo)
	{
		aktMemo.doSurround = false;
		aktMemo.panning -= getFineSlideValue(aktMemo.panningSlideValue)<<2;
		if (aktMemo.panning<0) aktMemo.panning=0; else if (aktMemo.panning>256) aktMemo.panning=256;
		aktMemo.currentInstrumentPanning = aktMemo.panning; // IT stays on panning value and pans around that one
		aktMemo.doFastVolRamp=true;
	}
	/**
	 * Convenient Method for the Global VolumeSlideEffekt
	 * @since 21.06.2006
	 * @param aktMemo
	 */
	protected void doGlobalVolumeSlideEffekt(final ChannelMemory aktMemo)
	{
		// Consider "Fine Global Slide" 0xFx || 0xxF
		if ((aktMemo.globalVolumSlideValue&0x0F)==0x0F && (aktMemo.globalVolumSlideValue&0xF0)!=0)
		{
			int param = aktMemo.globalVolumSlideValue>>4;
			if (!isIT) param <<=1;
			globalVolume += param;
		}
		else
		if ((aktMemo.globalVolumSlideValue&0xF0)==0xF0 && (aktMemo.globalVolumSlideValue&0x0F)!=0)
		{
			int param = aktMemo.globalVolumSlideValue&0xF;
			if (!isIT) param <<=1;
			globalVolume -= param;
		}
		else
		if ((aktMemo.globalVolumSlideValue&0xF0)!=0)
		{
			int param = aktMemo.globalVolumSlideValue>>4;
			if (!isIT) param <<=1;
			globalVolume += param;
		}
		else
		if ((aktMemo.globalVolumSlideValue&0x0F)!=0)
		{
			int param = aktMemo.globalVolumSlideValue&0xF;
			if (!isIT) param <<=1;
			globalVolume -= param;
		}

		if (globalVolume>ModConstants.MAXGLOBALVOLUME) globalVolume = ModConstants.MAXGLOBALVOLUME;
		else
		if (globalVolume<0) globalVolume = 0;
	}
	/**
	 * Re-Triggers the note and does volume slide
	 * @since 04.04.2020
	 * @param aktMemo
	 */
	protected void doRetrigNote(final ChannelMemory aktMemo, final boolean inTick)
	{
		if (((currentTempo - currentTick) % aktMemo.retrigMemo) == 0 && inTick)
		{
			aktMemo.retrigCount = aktMemo.retrigMemo;

			resetInstrumentPointers(aktMemo, true);

			if (aktMemo.retrigVolSlide>0)
			{
				switch (aktMemo.retrigVolSlide)
				{
					case 0x1: aktMemo.currentVolume--; break;
					case 0x2: aktMemo.currentVolume-=  2; break;
					case 0x3: aktMemo.currentVolume-=  4; break;
					case 0x4: aktMemo.currentVolume-=  8; break;
					case 0x5: aktMemo.currentVolume-= 16; break;
					case 0x6: aktMemo.currentVolume = (aktMemo.currentVolume<<1)/3; break;
					case 0x7: aktMemo.currentVolume>>=1; break;
					case 0x8: /* No volume change */ break;
					case 0x9: aktMemo.currentVolume++; break;
					case 0xA: aktMemo.currentVolume+=  2; break;
					case 0xB: aktMemo.currentVolume+=  4; break;
					case 0xC: aktMemo.currentVolume+=  8; break;
					case 0xD: aktMemo.currentVolume+= 16; break;
					case 0xE: aktMemo.currentVolume = (aktMemo.currentVolume*3)>>1; break;
					case 0xF: aktMemo.currentVolume<<=1; break;
				}
				if (aktMemo.currentVolume>ModConstants.MAX_SAMPLE_VOL) aktMemo.currentVolume = ModConstants.MAX_SAMPLE_VOL;
				else
				if (aktMemo.currentVolume<ModConstants.MIN_SAMPLE_VOL) aktMemo.currentVolume = ModConstants.MIN_SAMPLE_VOL;
				aktMemo.currentInstrumentVolume = aktMemo.currentVolume;
				aktMemo.doFastVolRamp = true;
			}
		}
	}
	/**
	 * @param aktMemo
	 * @see de.quippy.javamod.multimedia.mod.mixer.BasicModMixer#doTickEffekts(de.quippy.javamod.multimedia.mod.mixer.BasicModMixer.ChannelMemory)
	 */
	@Override
	protected void doTickEffekts(final ChannelMemory aktMemo)
	{
		if (aktMemo.assignedEffekt==0) return;

		switch (aktMemo.assignedEffekt)
		{
			case 0x04 : 		// VolumeSlide, BUT Fine Slide only on first Tick
				if (isSTM && aktMemo.assignedEffektParam==0) break;
				if (patternDelayCount>0 && currentTick==currentTempo) doRowEffects(aktMemo);
				else
				{
					if (!isFineSlide(aktMemo.volumSlideValue))
						doVolumeSlideEffekt(aktMemo);
				}
				break;
			case 0x05: 			// Porta Down
				if (isSTM && aktMemo.assignedEffektParam==0) break; // pick up target note!
				if (patternDelayCount>0 && currentTick==currentTempo) doRowEffects(aktMemo);
				else
				{
					doPortaDown(aktMemo, false, false);
				}
				break;
			case 0x06: 			// Porta Up
				if (isSTM && aktMemo.assignedEffektParam==0) break; // pick up target note!
				if (patternDelayCount>0 && currentTick==currentTempo) doRowEffects(aktMemo);
				else
				{
					doPortaUp(aktMemo, false, false);
				}
				break;
			case 0x07 :			// Porta to Note
				if (isSTM && aktMemo.assignedEffektParam==0) break; // pick up target note!
				doPortaToNoteEffekt(aktMemo);
				break;
			case 0x08 :			// Vibrato
				if (isSTM && aktMemo.assignedEffektParam==0) break; // pick up target note!
				doVibratoEffekt(aktMemo, false);
				break;
			case 0x09 :			// Tremor
				doTremorEffekt(aktMemo);
				break;
			case 0x0A :			// Arpeggio
				if (isSTM && aktMemo.assignedEffektParam==0) break; // pick up target note!
				doArpeggioEffekt(aktMemo);
				break;
			case 0x0B:			// Vibrato + VolumeSlide
				if (isSTM && aktMemo.assignedEffektParam==0) break; // pick up target note!
				if (patternDelayCount>0 && currentTick==currentTempo) doRowEffects(aktMemo);
				else
				{
					doVibratoEffekt(aktMemo, false);
					if (!isFineSlide(aktMemo.volumSlideValue))
						doVolumeSlideEffekt(aktMemo);
				}
				break;
			case 0x0C :			// Porta to Note + VolumeSlide
				if (isSTM && aktMemo.assignedEffektParam==0) break; // pick up target note!
				if (patternDelayCount>0 && currentTick==currentTempo) doRowEffects(aktMemo);
				else
				{
					doPortaToNoteEffekt(aktMemo);
					if (!isFineSlide(aktMemo.volumSlideValue))
						doVolumeSlideEffekt(aktMemo);
				}
				break;
			case 0x0E :			// Channel Volume Slide, if *NOT* Fine Slide
				if (patternDelayCount>0 && currentTick==currentTempo) doRowEffects(aktMemo);
				else
				{
					if (!isFineSlide(aktMemo.channelVolumSlideValue))
						doChannelVolumeSlideEffekt(aktMemo);
				}
				break;
			case 0x10 :			// Panning Slide
				if (patternDelayCount>0 && currentTick==currentTempo) doRowEffects(aktMemo);
				else
				{
					if (!isFineSlide(aktMemo.panningSlideValue))
						doPanningSlideEffekt(aktMemo);
				}
				break;
			case 0x11 :			// Retrig Note
				doRetrigNote(aktMemo, true);
				break;
			case 0x12 :			// Tremolo
				doTremoloEffekt(aktMemo);
				break;
			case 0x13 :			// Extended
				final int effektParam = (aktMemo.assignedEffektParam==0) ? aktMemo.S_Effect_Memory : aktMemo.assignedEffektParam;
				switch (effektParam>>4)
				{
					case 0x8:	// Fine Panning
						if (patternDelayCount>0 && currentTick==currentTempo) doRowEffects(aktMemo);
						break;
					case 0xC :	// Note Cut
						if (aktMemo.noteCutCount>0)
						{
							aktMemo.noteCutCount--;
							if (aktMemo.noteCutCount<=0)
							{
								aktMemo.noteCutCount=-1;
								doNoteCut(aktMemo);
								aktMemo.doFastVolRamp = true;
							}
						}
						break;
					case 0xD:	// Note Delay
						// we do this globally!
						break;
				}
				break;
			case 0x14 :			// Set Speed
				final int newTempo = aktMemo.oldTempoParameter;
				if ((newTempo&0xF0)==0x00)	// 0x0X
				{
					currentBPM -= newTempo&0xF;
					if (currentBPM<0x20) currentBPM = 0x20;
				}
				else
				if ((newTempo&0xF0)==0x10)	// 0x1X
				{
					currentBPM += newTempo&0xF;
					if (isModPlug && currentBPM>0x200) currentBPM = 0x200; // 512 for MPT ITex
					else
					if (currentBPM>0xFF) currentBPM = 0xFF;
				}
				break;
			case 0x15 :			// Fine Vibrato
				doVibratoEffekt(aktMemo, true);
				break;
			case 0x17 :			// Global Volume Slide
				if (!isFineSlide(aktMemo.globalVolumSlideValue))
					doGlobalVolumeSlideEffekt(aktMemo);
				break;
			case 0x19 :			// Panbrello
				doPanbrelloEffekt(aktMemo);
				break;
			case 0x1C:			// Smooth Midi Macro
				final MidiMacros smoothMacro = mod.getMidiConfig();
				if (smoothMacro!=null)
				{
		            if (aktMemo.assignedEffektParam<0x80)
		                processMIDIMacro(aktMemo, true, smoothMacro.getMidiSFXExt(aktMemo.activeMidiMacro), aktMemo.assignedEffektParam);
		            else
		                processMIDIMacro(aktMemo, true, smoothMacro.getMidiZXXExt(aktMemo.assignedEffektParam & 0x7F), 0);
				}
	            break;
		}
	}
	/**
	 * @param aktMemo
	 * @see de.quippy.javamod.multimedia.mod.mixer.BasicModMixer#doVolumeColumnRowEffekt(de.quippy.javamod.multimedia.mod.mixer.BasicModMixer.ChannelMemory)
	 */
	@Override
	protected void doVolumeColumnRowEffekt(final ChannelMemory aktMemo)
	{
		if (aktMemo.assignedVolumeEffekt==0) return;

		switch (aktMemo.assignedVolumeEffekt)
		{
			case 0x01: // Set Volume
				aktMemo.currentVolume = aktMemo.assignedVolumeEffektOp;
				if (aktMemo.currentVolume>ModConstants.MAX_SAMPLE_VOL) aktMemo.currentVolume = ModConstants.MAX_SAMPLE_VOL;
				else
				if (aktMemo.currentVolume<ModConstants.MIN_SAMPLE_VOL) aktMemo.currentVolume = ModConstants.MIN_SAMPLE_VOL;
				aktMemo.currentInstrumentVolume = aktMemo.currentVolume;
				aktMemo.doFastVolRamp = true;
				break;
			case 0x02: // Volslide down
				if (aktMemo.assignedVolumeEffektOp!=0) aktMemo.volumSlideValue = aktMemo.assignedVolumeEffektOp & 0xF;
				if ((mod.getSongFlags()&ModConstants.SONG_FASTVOLSLIDES)!=0)
					doVolumeSlideEffekt(aktMemo);
				break;
			case 0x03: // Volslide up
				if (aktMemo.assignedVolumeEffektOp!=0) aktMemo.volumSlideValue = (aktMemo.assignedVolumeEffektOp<<4) & 0xF0;
				if ((mod.getSongFlags()&ModConstants.SONG_FASTVOLSLIDES)!=0)
					doVolumeSlideEffekt(aktMemo);
				break;
			case 0x04: // Fine Volslide down
				if (aktMemo.assignedVolumeEffektOp!=0) aktMemo.volumSlideValue = (aktMemo.assignedVolumeEffektOp & 0xF) | 0xF0;
				doVolumeSlideEffekt(aktMemo);
				break;
			case 0x05: // Fine Volslide up
				if (aktMemo.assignedVolumeEffektOp!=0) aktMemo.volumSlideValue = ((aktMemo.assignedVolumeEffektOp<<4) & 0xF0) | 0x0F;
				doVolumeSlideEffekt(aktMemo);
				break;
			case 0x06: // vibrato speed - does not activate... // only ModPlug Version <= 1.17.02.54 did this...
				aktMemo.vibratoStep = aktMemo.assignedVolumeEffektOp;
				break;
			case 0x07: // vibrato depth and enable
				if (aktMemo.assignedVolumeEffektOp!=0) aktMemo.vibratoAmplitude = aktMemo.assignedVolumeEffektOp<<2;
				aktMemo.vibratoOn = true;
				doVibratoEffekt(aktMemo, false);
				break;
			case 0x08: // Set Panning
				doPanning(aktMemo, aktMemo.assignedVolumeEffektOp, ModConstants.PanBits.Pan6Bit);
				break;
			case 0x0B: // Tone Porta
				final PatternElement element = aktMemo.currentElement;
				if (hasNewNote(element)) aktMemo.portaTargetNotePeriod = getFineTunePeriod(aktMemo);
				if (aktMemo.assignedVolumeEffektOp!=0)
				{
					final int index = (aktMemo.assignedVolumeEffektOp>9)?9:aktMemo.assignedVolumeEffektOp&0x0F;
					aktMemo.portaNoteStep = ModConstants.IT_VolColumnPortaNoteSpeedTranslation[index];
					if (isNotITCompatMode) aktMemo.IT_EFG = aktMemo.portaNoteStep;
				}
				break;
			case 0x0C: // Porta Down
				if (aktMemo.assignedVolumeEffektOp!=0)
				{
					aktMemo.portaStepDown = aktMemo.assignedVolumeEffektOp<<2;
					if (isNotITCompatMode) aktMemo.IT_EFG = aktMemo.portaStepDown;
				}
				break;
			case 0x0D: // Porta Up
				if (aktMemo.assignedVolumeEffektOp!=0)
				{
					aktMemo.portaStepUp = aktMemo.assignedVolumeEffektOp<<2;
					if (isNotITCompatMode) aktMemo.IT_EFG = aktMemo.portaStepUp;
				}
				break;
			case 0x0E: // Sample Cues - MPT specific
				final Sample sample = aktMemo.currentSample;
				if (sample!=null)
				{
					final int [] cues = sample.getCues();
					if (cues!=null && aktMemo.assignedVolumeEffektOp <= cues.length)
					{
						if (aktMemo.assignedVolumeEffektOp!=0) aktMemo.sampleOffset = cues[aktMemo.assignedVolumeEffektOp - 1];
						doSampleOffsetEffekt(aktMemo, aktMemo.currentElement);
					}
				}
				break;
			default:
				//Log.debug(String.format("Unknown Volume Effect: Effect:%02X Op:%02X in [Pattern:%03d: Row:%03d Channel:%03d]", Integer.valueOf(aktMemo.volumeEffekt), Integer.valueOf(aktMemo.volumeEffektOp), Integer.valueOf(currentPatternIndex), Integer.valueOf(currentRow), Integer.valueOf(aktMemo.channelNumber+1)));
				break;
		}
	}
	/**
	 * @param aktMemo
	 * @see de.quippy.javamod.multimedia.mod.mixer.BasicModMixer#doVolumeColumnTickEffekt(de.quippy.javamod.multimedia.mod.mixer.BasicModMixer.ChannelMemory)
	 */
	@Override
	protected void doVolumeColumnTickEffekt(final ChannelMemory aktMemo)
	{
		if (aktMemo.assignedVolumeEffekt==0) return;

		switch (aktMemo.assignedVolumeEffekt)
		{
			case 0x02: // Volslide down
			case 0x03: // Volslide up
				doVolumeSlideEffekt(aktMemo);
				break;
			case 0x04: // Fine Volslide down
			case 0x05: // Fine Volslide up
				if (patternDelayCount>0 && currentTick==currentTempo) doVolumeColumnRowEffekt(aktMemo);
				break;
			case 0x07: // vibrato speed
				doVibratoEffekt(aktMemo, false);
				break;
			case 0x09: // Panning Slide Left
			case 0x0A: // Panning Slide Right
				doPanningSlideEffekt(aktMemo);
				break;
			case 0x0B: // Tone Porta
				doPortaToNoteEffekt(aktMemo);
				break;
			case 0x0C: // Porta Down
				doPortaDown(aktMemo, false, true);
				break;
			case 0x0D: // Porta Up
				doPortaUp(aktMemo, false, true);
				break;
		}
	}
	/**
	 * @param aktMemo
	 * @return true, if the Effekt and EffektOp indicate a NoteDelayEffekt
	 * @see de.quippy.javamod.multimedia.mod.mixer.BasicModMixer#isNoteDelayEffekt(de.quippy.javamod.multimedia.mod.mixer.BasicModMixer.ChannelMemory)
	 */
	@Override
	protected boolean isNoteDelayEffekt(final int effekt, final int effektParam)
	{
		final boolean isEffekt = effekt==0x13 && (effektParam>>4)==0x0D;
		if (isS3M && isEffekt && (effektParam&0xF)==0) return false;
		return isEffekt;
	}
	/**
	 * @param effekt
	 * @param assignedEffektParam
	 * @return
	 * @see de.quippy.javamod.multimedia.mod.mixer.BasicModMixer#isPatternFramesDelayEffekt(int, int)
	 */
	@Override
	protected boolean isPatternFramesDelayEffekt(final int effekt, final int effektParam)
	{
		return effekt==0x13 && (effektParam>>4)==0x06;
	}
	/**
	 * @param aktMemo
	 * @return true, if the Effekt and EffektOp indicate a PortaToNoteEffekt AND there was a Note set
	 * @see de.quippy.javamod.multimedia.mod.mixer.BasicModMixer#isPortaToNoteEffekt(de.quippy.javamod.multimedia.mod.mixer.BasicModMixer.ChannelMemory)
	 */
	@Override
	protected boolean isPortaToNoteEffekt(final int effekt, final int effektParam, final int volEffekt, final int volEffektParam, final int notePeriod)
	{
		return ((effekt==0x07 || effekt==0x0C) || volEffekt==0x0B) && notePeriod!=0;
	}
	/**
	 * @param aktMemo
	 * @return true, if the effekt indicates a SampleOffsetEffekt
	 * @see de.quippy.javamod.multimedia.mod.mixer.BasicModMixer#isSampleOffsetEffekt(de.quippy.javamod.multimedia.mod.mixer.BasicModMixer.ChannelMemory)
	 */
	@Override
	protected boolean isSampleOffsetEffekt(final int effekt)
	{
		return effekt==0x0F;
	}
	/**
	 * @param effekt
	 * @param effektParam
	 * @return
	 * @see de.quippy.javamod.multimedia.mod.mixer.BasicModMixer#isKeyOffEffekt(int, int)
	 */
	@Override
	protected boolean isKeyOffEffekt(final int effekt, final int effektParam)
	{
		return false;
	}
	/**
	 * @param aktMemo
	 * @return
	 * @see de.quippy.javamod.multimedia.mod.mixer.BasicModMixer#isNNAEffekt(de.quippy.javamod.multimedia.mod.mixer.BasicModMixer.ChannelMemory)
	 */
	@Override
	protected boolean isNNAEffekt(final int effekt, final int effektParam)
	{
		return effekt==0x13 && (effektParam>>4)==0x7 && (effektParam&0xF)<=0x6;
	}
	/**
	 * @param aktMemo
	 * @param effekt
	 * @param assignedEffektParam
	 * @return
	 * @see de.quippy.javamod.multimedia.mod.mixer.BasicModMixer#getEffektOpMemory(de.quippy.javamod.multimedia.mod.mixer.BasicModMixer.ChannelMemory, int, int)
	 */
	@Override
	protected int getEffektOpMemory(final ChannelMemory aktMemo, final int effekt, final int effektParam)
	{
		if (effekt==0x13 && effektParam == 0) return aktMemo.S_Effect_Memory;
		return effektParam;
	}
	/**
	 * Some effects in IT need to be done after the tick effects
	 * @since 06.07.2024
	 * @param effect
	 * @return
	 */
	private boolean isAfterEffect(final int effect)
	{
		switch (effect)
		{
			case 0x08: // Vibrato
			case 0x09: // Tremor
			case 0x0A: // Arpeggio
			case 0x0B: // Vibrato + VolSlide
				return isIT;
		}
		return false;
	}
	/**
	 * @param aktMemo
	 * @see de.quippy.javamod.multimedia.mod.mixer.BasicModMixer#processTickEffekts(de.quippy.javamod.multimedia.mod.mixer.BasicModMixer.ChannelMemory)
	 */
	@Override
	protected void processTickEffekts(final ChannelMemory aktMemo)
	{
		if (isS3M)
		{
			 // no effects in muted channels with S3Ms
			if (aktMemo.muteWasITforced/* || aktMemo.muted*/ || (!isModPlug && aktMemo.assignedEffekt>0x16)) return; // Effects not implemented in S3Ms
		}
		if (isSTM && aktMemo.assignedEffekt>0x0A) return; // even though these effects can be edited, they have no effect in ScreamTracker 2.2

		final boolean isAfterEffect = (isIT)?isAfterEffect(aktMemo.assignedEffekt):false;
		if (!isAfterEffect) doTickEffekts(aktMemo);
		doVolumeColumnTickEffekt(aktMemo);
		if (isAfterEffect) doTickEffekts(aktMemo);
	}
	/**
	 * @param aktMemo
	 * @see de.quippy.javamod.multimedia.mod.mixer.BasicModMixer#processEffekts(de.quippy.javamod.multimedia.mod.mixer.BasicModMixer.ChannelMemory)
	 */
	@Override
	protected void processEffekts(final ChannelMemory aktMemo)
	{
		if (isS3M)
		{
			 // no effects in muted channels with S3Ms
			if (aktMemo.muteWasITforced/* || aktMemo.muted*/ || (!isModPlug && aktMemo.assignedEffekt>0x16)) return; // Effects not implemented in S3Ms
		}
		if (isSTM && aktMemo.assignedEffekt>0x0A) return; // even though these effects can be edited, they have no effect in ScreamTracker 2.2

		// shared Effect memory EFG is sharing information only on tick 0!
		// we cannot share during effects. Only with IT Compat Mode off!
		// *** IT Compat Off means, old stm, s3m ... ***
		if (isNotITCompatMode)
			aktMemo.portaStepDown = aktMemo.portaStepUp = aktMemo.portaNoteStep = aktMemo.IT_EFG;

		final boolean isAfterEffect = (isIT)?isAfterEffect(aktMemo.assignedEffekt):false;
		if (!isAfterEffect) doRowEffects(aktMemo);
		doVolumeColumnRowEffekt(aktMemo);
		if (isAfterEffect) doRowEffects(aktMemo);
	}
}
