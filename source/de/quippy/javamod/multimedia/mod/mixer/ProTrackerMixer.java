/*
 * @(#) ProTrackerMixer.java
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
 * This is the protracker mixing routine with all special mixing
 * on typical protracker events
 * @author Daniel Becker
 * @since 30.04.2006
 */
public class ProTrackerMixer extends BasicModMixer
{
	// Pointer to the correct mapping table - like FT2 does it to not
	// check all the time
	private int [] note2Period;

	/**
	 * Constructor for ProTrackerMixer
	 */
	public ProTrackerMixer(final Module mod, final int sampleRate, final int doISP, final int doNoLoops, final int maxNNAChannels)
	{
		super(mod, sampleRate, doISP, doNoLoops, maxNNAChannels);
	}

	/**
	 * @param channel
	 * @param aktMemo
	 * @see de.quippy.javamod.multimedia.mod.mixer.BasicModMixer#initializeMixer(int, de.quippy.javamod.multimedia.mod.mixer.BasicModMixer.ChannelMemory)
	 */
	@Override
	protected void initializeMixer(final int channel, final ChannelMemory aktMemo)
	{
		if (frequencyTableType == ModConstants.XM_LINEAR_TABLE)
			note2Period = ModConstants.FT2_linearPeriods;
		else
		if (frequencyTableType == ModConstants.XM_AMIGA_TABLE)
			note2Period = ModConstants.FT2_amigaPeriods;
		setPeriodBorders(aktMemo);
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
		if (frequencyTableType == ModConstants.AMIGA_TABLE)
		{
			aktMemo.portaStepUpEnd = getFineTunePeriod(aktMemo, ModConstants.getNoteIndexForPeriod(113)+1);
			aktMemo.portaStepDownEnd = getFineTunePeriod(aktMemo, ModConstants.getNoteIndexForPeriod(856)+1);
		}
		else
		{
			aktMemo.portaStepUpEnd = getFineTunePeriod(aktMemo, 119); // 118 + 1
			aktMemo.portaStepDownEnd = getFineTunePeriod(aktMemo, 1); // 0 + 1
		}
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
			case ModConstants.AMIGA_TABLE:
				final int lookUpFineTune = ((aktMemo.currentFineTune<0)?aktMemo.currentFineTune+16:aktMemo.currentFineTune)*37;
				int proTrackerIndex = noteIndex - (3*12); // the noteindex we use has 3 more octaves than the PT period table
				if (proTrackerIndex>35) proTrackerIndex = 35; else if (proTrackerIndex<0) proTrackerIndex = 0;
				return ModConstants.periodTable[lookUpFineTune + proTrackerIndex]<<ModConstants.PERIOD_SHIFT;

			case ModConstants.XM_AMIGA_TABLE:
			case ModConstants.XM_LINEAR_TABLE:
				final int C4Period = (noteIndex<<4) + ((aktMemo.currentFineTune>>3) + 16); // 0..1920
				return note2Period[C4Period]<<(ModConstants.PERIOD_SHIFT-2); // table values are already shifted by 2

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
			case ModConstants.XM_AMIGA_TABLE:
			case ModConstants.AMIGA_TABLE:
				final int clampedPeriod = (newPeriod>aktMemo.portaStepDownEnd)?aktMemo.portaStepDownEnd:(newPeriod<aktMemo.portaStepUpEnd)?aktMemo.portaStepUpEnd:newPeriod;
				aktMemo.currentTuning = globalTuning / (aktMemo.currentNotePeriodSet = clampedPeriod);
				return;
			case ModConstants.XM_LINEAR_TABLE:
				// We have a different LUT table as original FT2 - to avoid the doubles used there
				// So we need some adoption to the algorithm used in FT2 but stay as close as possible to the coding there:
				final int period = (newPeriod>>(ModConstants.PERIOD_SHIFT-2)) & 0xFFFF;
				final int invPeriod = ((12 * 192 * 4) + 767 - period) & 0xFFFF; // 12 octaves * (12 * 16 * 4) LUT entries = 9216, add 767 for rounding
				final int quotient  = invPeriod / (12 * 16 * 4);
				final int remainder = period % (12 * 16 * 4);
				final int newFrequency = ModConstants.lintab[remainder] >> (((14 - quotient) & 0x1F)-2); // values are 4 times bigger in FT2
				aktMemo.currentTuning = (int)(((long)newFrequency<<ModConstants.SHIFT) / sampleRate);
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
		if (!isXM) return val;

		int row = currentRow;
		int lookAheadRows = 4;
		boolean xmTempoFix = false;
		switch (aktMemo.assignedEffekt)
		{
			case 0x09:	// sample offset
				// 24 bit command
				lookAheadRows = 2;
				break;
			case 0x0F:	// Tempo
				xmTempoFix = true;
			case 0x0B:	// Pattern position jump
			case 0x0D:	// Pattern Break
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
			if (patternElement.getEffekt()!=0x26) break;
			rowsUsed++;
			// With XM, 0x20 is the lowest tempo. Anything below changes ticks per row.
			// Moving that to the left will wrongly and unintentionally increase resulting BPM
			if (xmTempoFix && val>=0x20 && val<256) val-=0x20;
			val = (val<<8) | (patternElement.getEffektOp()&0xFF);
		}
		if (extendedRowsUsed!=null) extendedRowsUsed.set(rowsUsed);
		return val;
	}
	/**
	 * @since 11.07.2024
	 * @param aktMemo
	 * @param forNote
	 */
	protected void triggerFTNote(final ChannelMemory aktMemo, final int forNote)
	{
		if (forNote == ModConstants.KEY_OFF)
		{
			doKeyOff(aktMemo);
			return;
		}

		int note = forNote;
		if (note==0)
		{
			note = aktMemo.assignedNoteIndex;
			if (note==0) return;
		}
		aktMemo.assignedNoteIndex = note;

		final Instrument ins = mod.getInstrumentContainer().getInstrument(aktMemo.currentAssignedInstrumentIndex-1);
		aktMemo.assignedSample = (ins!=null)?mod.getInstrumentContainer().getSample(ins.getSampleIndex(note-1)):mod.getInstrumentContainer().getSample(aktMemo.currentAssignedInstrumentIndex-1);
		aktMemo.assignedInstrumentIndex = aktMemo.currentAssignedInstrumentIndex;
		aktMemo.assignedInstrument = aktMemo.currentAssignedInstrument;

		if (note>96) note = 96;
		aktMemo.currentSample = aktMemo.assignedSample;
		if (aktMemo.assignedSample!=null) note += aktMemo.assignedSample.transpose;

		note&=0xFF; // note is an uint8_t - simulate
		if (note>=10*12) return; // this is an uint8 compare and works for <0 as well - well at least when we do not get *that* negative...

		// Memorize volumes to set in FT2 code, but do not do it (chn->oldVol...)
		// resetVolumeAndPanning(aktMemo, aktMemo.assignedInstrument, aktMemo.currentSample);
		resetFineTune(aktMemo, aktMemo.currentSample); // fineTune and other resets...
		if (aktMemo.assignedEffekt==0x0E && (aktMemo.assignedEffektParam&0xF0)==0x50)
			aktMemo.currentFineTune = ((aktMemo.assignedEffektParam&0x0F)<<4)-128;

		if (note!=0)
		{
			// in favor of the uint8 used here, we re-implement it like FT2 does it. That way some quirks with
			// too low notes will result in the same behavior (i.e. a wrap around).
			//setNewPlayerTuningFor(aktMemo, aktMemo.currentNotePeriod = getFineTunePeriod(aktMemo, note));
			final int noteIndex = (((note-1)&0xFF)<<4) + ((aktMemo.currentFineTune>>3) + 16); // 0..1920
			aktMemo.currentNotePeriod = note2Period[noteIndex]<<(ModConstants.PERIOD_SHIFT-2); // table values are already shifted by 2
			setNewPlayerTuningFor(aktMemo, aktMemo.currentNotePeriod);
		}
		resetInstrumentPointers(aktMemo, true);
	}
	/**
	 * Convenient Method of FT2 retriggerInstrument
	 * @since 09.07.2024
	 * @param aktMemo
	 */
	protected void triggerFTInstrument(final ChannelMemory aktMemo)
	{
		reset_VibTremPan_TablePositions(aktMemo);
		aktMemo.keyOff = aktMemo.noteFade = false;
		aktMemo.retrigCount = aktMemo.tremorOfftimeSet = 0;
		resetEnvelopes(aktMemo);
		resetAutoVibrato(aktMemo, aktMemo.assignedSample);
	}
	/**
	 * Trigger the ProTracker period - doRetrg()
	 * From NoteDelay, setNewInstrumentAndPeriod
	 * @since 11.07.2024
	 * @param aktMemo
	 */
	private void triggerPTPeriod(final ChannelMemory aktMemo)
	{
		aktMemo.currentSample = aktMemo.assignedSample;
		resetInstrumentPointers(aktMemo, true);
		setNewPlayerTuningFor(aktMemo, aktMemo.currentNotePeriod = getFineTunePeriod(aktMemo));
	}
	/**
	 * @since 26.07.2024
	 * @param aktMemo
	 */
	protected void doKeyOff(final ChannelMemory aktMemo)
	{
		aktMemo.keyOff = true;
		// XM has a certain envelope tick reset with key off and existing envelopes - tick is reset to the previous start position
		// That way a sustain release is managed - however, with panning because of a bug, that does not work
		final Instrument currentInstrument = aktMemo.assignedInstrument;
		final Envelope volumeEnv = (currentInstrument!=null)?currentInstrument.volumeEnvelope:null;
		if (volumeEnv!=null && volumeEnv.on)
		{
			aktMemo.volEnvTick = volumeEnv.getXMResetPosition(aktMemo.volEnvTick, aktMemo.volXMEnvPos);
		}
		else
		{
			aktMemo.currentVolume = 0;
			aktMemo.doFastVolRamp = true;
		}
		final Envelope panningEnv = (currentInstrument!=null)?currentInstrument.panningEnvelope:null;
		if (panningEnv!=null && !panningEnv.on) // another FT2 Bug
		{
			aktMemo.panEnvTick = panningEnv.getXMResetPosition(aktMemo.panEnvTick, aktMemo.panXMEnvPos);
		}
	}
	/**
	 * @since 14.07.2024
	 * @param aktMemo
	 * @see de.quippy.javamod.multimedia.mod.mixer.BasicModMixer#setNewInstrumentAndPeriod(de.quippy.javamod.multimedia.mod.mixer.BasicModMixer.ChannelMemory)
	 */
	@Override
	protected void setNewInstrumentAndPeriod(final ChannelMemory aktMemo)
	{
		final PatternElement element = aktMemo.currentElement;
		final boolean isNoteDelayEffect = isNoteDelayEffekt(aktMemo.currentAssignedEffekt, aktMemo.currentAssignedEffektParam);
		final boolean isPortaToNoteEffect = isPortaToNoteEffekt(aktMemo.currentAssignedEffekt, aktMemo.currentAssignedEffektParam, aktMemo.currentAssignedVolumeEffekt, aktMemo.currentAssignedVolumeEffektOp, aktMemo.currentAssignedNotePeriod);

		// copy last seen values from pattern - only effect values first
		aktMemo.assignedEffekt = aktMemo.currentAssignedEffekt;
		aktMemo.assignedEffektParam = aktMemo.currentAssignedEffektParam;
		aktMemo.assignedVolumeEffekt = aktMemo.currentAssignedVolumeEffekt;
		aktMemo.assignedVolumeEffektOp = aktMemo.currentAssignedVolumeEffektOp;

		if (isMOD)
		{
			// We need to mangle some things, as ProTracker saves information early but
			// sets them in the Paula at given times. We have no Paula emulation
			// so we need to simulate some things that would work more or less automatically
			// with using Paula emulation

			// With an illegal note delay (longer than currentTempo) the period is set if no new note is present
			if (currentTempo==currentTick && aktMemo.noteDelayCount>=currentTempo)
			{
				aktMemo.noteDelayCount = -1;
				if (hasNoNote(aktMemo.currentElement)) // only if there is no note present
				{
					// simply set the period
					setNewPlayerTuningFor(aktMemo, aktMemo.currentNotePeriod = getFineTunePeriod(aktMemo));
				}
			}

			// With ProTracker 1/2, if a sample is presented, volume, fineTune and samplePointer
			// are always set - regardless of NoteDelayEffect or Porta2Note...
			final int sampleIndex = element.getInstrument();
			if (sampleIndex>0)
			{
				aktMemo.assignedSample = mod.getInstrumentContainer().getSample(sampleIndex-1);
				if (aktMemo.assignedSample!=null)
				{
					resetVolumeAndPanning(aktMemo, null, aktMemo.assignedSample);
					resetFineTune(aktMemo, aktMemo.assignedSample);
					aktMemo.prevSampleOffset=0;
					// Inplace sample swap:
					// This is an exception: inplace instrument is activated, if with retrigger effect or
					// previous was an empty one (instrumentFinished is not sufficient!)
					// or finished.
					// Otherwise, it will be set after loop
					// Of course not, if note delay effect
					if ((hasNoNote(element) && !isNoteDelayEffect) && // Inplace: no note - plus not with note delay
						((aktMemo.currentSample==null || !aktMemo.currentSample.hasSampleData()) || // previous is empty
						 (element.getEffekt()==0x0E && (element.getEffektOp()&0xF0)==0x90)) || // 0xE9x Re-Trigger command.
						(!isNoteDelayEffect && aktMemo.currentSample!=aktMemo.assignedSample && aktMemo.instrumentFinished)) // inplace of finished instrument, even with note
					{
						// Now activate new Instrument...
						aktMemo.currentSample = aktMemo.assignedSample;
						resetInstrumentPointers(aktMemo, true);
					}
				}
			}

			if (hasNewNote(element))
			{
				// copy for Porta2Note / NoteDelay
				aktMemo.assignedNotePeriod = aktMemo.currentAssignedNotePeriod;
				aktMemo.assignedNoteIndex = aktMemo.currentAssignedNoteIndex;
				if (!isPortaToNoteEffect && !isNoteDelayEffect)
				{
					reset_VibTremPan_TablePositions(aktMemo); // with MODs we reset vibrato/tremolo here
					triggerPTPeriod(aktMemo);
				}
			}
		}
		else
		if (isXM)
		{
			final boolean isKeyOff = element.getPeriod()==ModConstants.KEY_OFF || element.getNoteIndex()==ModConstants.KEY_OFF;
			final boolean isK00 = isKeyOffEffekt(aktMemo.currentAssignedEffekt, aktMemo.currentAssignedEffektParam) && aktMemo.currentAssignedEffektParam==0;
			// special K00/KeyOff handling of XMs - instrument and note are "invisible" with K00
			if (isKeyOff || isK00)
			{
				// Sample Change with Note Delay - with normal note delay we must be able to recover the index
				// so only hide with K00
				if (isK00)
					aktMemo.currentAssignedInstrumentIndex = aktMemo.assignedInstrumentIndex;

				aktMemo.currentAssignedNotePeriod = aktMemo.assignedNotePeriod;
				aktMemo.currentAssignedNoteIndex = aktMemo.assignedNoteIndex;
				aktMemo.currentAssignedInstrument = aktMemo.assignedInstrument; // Sample Change with Note Delay

				doKeyOff(aktMemo);

				if (element.getInstrument()>0)
					resetVolumeAndPanning(aktMemo, aktMemo.assignedInstrument, aktMemo.assignedSample);

				// with a delayed keyOff, this needs to be done!
				if (isNoteDelayEffect)
				{
					triggerFTInstrument(aktMemo);
				}
			}
			else
			{
				if (isPortaToNoteEffect)
				{
					aktMemo.assignedNotePeriod = aktMemo.currentAssignedNotePeriod;
					aktMemo.assignedNoteIndex = aktMemo.currentAssignedNoteIndex;
					// ignore the instrument, so set back the previous one
					aktMemo.currentAssignedInstrumentIndex = aktMemo.assignedInstrumentIndex;
					aktMemo.currentAssignedInstrument = aktMemo.assignedInstrument;
					if (aktMemo.currentSample!=null) aktMemo.assignedSample = aktMemo.currentSample;
					return;
				}
				if (element.getNoteIndex()>0 && !isNoteDelayEffect)
					triggerFTNote(aktMemo, element.getNoteIndex());
				if (element.getInstrument()>0)
				{
					// reset for new Instrument
					resetVolumeAndPanning(aktMemo, aktMemo.currentAssignedInstrument, aktMemo.assignedSample);
					if (!isKeyOff) triggerFTInstrument(aktMemo);
				}
			}
		}
	}
	/**
	 * Do the effects of a row. This is mostly the setting of effects
	 * @param aktMemo
	 */
	@Override
	protected void doRowEffects(final ChannelMemory aktMemo)
	{
		final AtomicInteger rowsUsed;

		if (aktMemo.tremorWasActive)
		{
			aktMemo.currentVolume = aktMemo.currentInstrumentVolume;
			aktMemo.tremorWasActive = false;
			aktMemo.doFastVolRamp = true;
		}

		final PatternElement element = aktMemo.currentElement;
		// reset FunkIt
		if (element!=null && element.getInstrument()!=0) aktMemo.EFxOffset = 0;

		if (aktMemo.assignedEffekt==0 && aktMemo.assignedEffektParam==0) return;

		switch (aktMemo.assignedEffekt)
		{
			case 0x00:			// Arpeggio
				if (aktMemo.assignedEffektParam != 0) aktMemo.arpeggioParam = aktMemo.assignedEffektParam;
				if (aktMemo.assignedNoteIndex>ModConstants.NO_NOTE)
				{
					// I consider it a bug that with arpeggios the base note is not newly calculated with finetune
					// but the notes 1 and 2 are. However, that is what PT2 did. I guess that was fixed.
					// FT2 however does not change instruments when no note is set - so there we are fine.
					if (isMOD)
					{
						aktMemo.arpeggioNote[0] = getFineTunePeriod(aktMemo);
						aktMemo.arpeggioNote[1] = adjustPTPeriodFromNote(aktMemo, aktMemo.arpeggioNote[0], (aktMemo.arpeggioParam >>4));
						aktMemo.arpeggioNote[2] = adjustPTPeriodFromNote(aktMemo, aktMemo.arpeggioNote[0], (aktMemo.arpeggioParam&0xF));
					}
					else
					{
						aktMemo.arpeggioNote[0] = aktMemo.currentNotePeriod;
						aktMemo.arpeggioNote[1] = adjustFTPeriodFromNote(aktMemo, aktMemo.arpeggioNote[0], aktMemo.arpeggioParam >>4);
						aktMemo.arpeggioNote[2] = adjustFTPeriodFromNote(aktMemo, aktMemo.arpeggioNote[0], aktMemo.arpeggioParam&0xF);
					}
					aktMemo.arpeggioIndex=0;
				}
				break;
			case 0x01:			// Porta Up
				if (isMOD && aktMemo.assignedEffektParam==0) // No effect memory with MODs
					aktMemo.portaStepUp = 0;
				else
				if (aktMemo.assignedEffektParam!=0)
					aktMemo.portaStepUp=(aktMemo.assignedEffektParam&0xFF)<<ModConstants.PERIOD_SHIFT;
				break;
			case 0x02:			// Porta Down
				if (isMOD && aktMemo.assignedEffektParam==0) // No effect memory with MODs
					aktMemo.portaStepDown = 0;
				else
				if (aktMemo.assignedEffektParam!=0)
					aktMemo.portaStepDown=(aktMemo.assignedEffektParam&0xFF)<<ModConstants.PERIOD_SHIFT;
				break;
			case 0x03:	 		// Porta To Note
				if (aktMemo.assignedEffektParam!=0) aktMemo.portaNoteStep = aktMemo.assignedEffektParam<<ModConstants.PERIOD_SHIFT;
				preparePortaToNoteEffect(aktMemo);
				break;
			case 0x04:			// Vibrato
				if ((aktMemo.assignedEffektParam>>4)!=0) aktMemo.vibratoStep = aktMemo.assignedEffektParam>>4;
				if ((aktMemo.assignedEffektParam&0xF)!=0) aktMemo.vibratoAmplitude = aktMemo.assignedEffektParam&0xF;
				aktMemo.vibratoOn = true;
				doVibratoEffekt(aktMemo);
				break;
			case 0x05:			// Porta To Note + VolumeSlide
				preparePortaToNoteEffect(aktMemo);
				// With Protracker Mods Porta without Parameter is just Porta, no Volume Slide - has not effect memory
				if (isMOD && aktMemo.assignedEffektParam==0)
					aktMemo.volumSlideValue = 0;
				else
				if (aktMemo.assignedEffektParam!=0) aktMemo.volumSlideValue = aktMemo.assignedEffektParam;
				break;
			case 0x06:			// Vibrato + VolumeSlide
				aktMemo.vibratoOn = true;
				doVibratoEffekt(aktMemo);
				// With Protracker Mods Vibrato without Parameter is just Vibrato, no Volume Slide - has not effect memory
				if (isMOD && aktMemo.assignedEffektParam==0)
					aktMemo.volumSlideValue = 0;
				else
				if (aktMemo.assignedEffektParam!=0) aktMemo.volumSlideValue = aktMemo.assignedEffektParam;
				break;
			case 0x07:			// Tremolo
				if ((aktMemo.assignedEffektParam>>4)!=0) aktMemo.tremoloStep = aktMemo.assignedEffektParam>>4;
				if ((aktMemo.assignedEffektParam&0xF)!=0) aktMemo.tremoloAmplitude = aktMemo.assignedEffektParam&0xF;
				aktMemo.tremoloOn = true;
				doTremoloEffekt(aktMemo);
				break;
			case 0x08:			// Set Panning
				doPanning(aktMemo, aktMemo.assignedEffektParam, ModConstants.PanBits.Pan8Bit);
				break;
			case 0x09 : 		// Sample Offset
				if (isXM && hasNoNote(element)) break; // is normally done in "triggerFTNote"
				rowsUsed = new AtomicInteger(0);
				final int newSampleOffset = calculateExtendedValue(aktMemo, rowsUsed);
				if (newSampleOffset!=0)
				{
					if (rowsUsed.get()==0) // old behavior
					{
						aktMemo.sampleOffset = aktMemo.highSampleOffset<<16 | newSampleOffset<<8;
//						aktMemo.highSampleOffset = 0; // set zero after usage?!
					}
					else
						aktMemo.sampleOffset = newSampleOffset;
				}
				doSampleOffsetEffekt(aktMemo, element);
				break;
			case 0x0A:			// Volume Slide
				// With Protracker Mods Volume Slide has not effect memory
				if (isMOD && aktMemo.assignedEffektParam==0)
					aktMemo.volumSlideValue = 0;
				else
				if (aktMemo.assignedEffektParam!=0) aktMemo.volumSlideValue = aktMemo.assignedEffektParam;
				break;
			case 0x0B:			// Pattern position jump
				patternBreakPatternIndex = calculateExtendedValue(aktMemo, null);
				patternBreakRowIndex = 0;
				patternJumpRowIndex = patternBreakRowIndex;
				patternBreakSet = true;
				break;
			case 0x0C:			// Set volume
				aktMemo.currentVolume = aktMemo.assignedEffektParam;
				if (aktMemo.currentVolume>ModConstants.MAX_SAMPLE_VOL) aktMemo.currentVolume = ModConstants.MAX_SAMPLE_VOL;
				else
				if (aktMemo.currentVolume<ModConstants.MIN_SAMPLE_VOL) aktMemo.currentVolume = ModConstants.MIN_SAMPLE_VOL;
				aktMemo.currentInstrumentVolume = aktMemo.currentVolume;
				aktMemo.doFastVolRamp = true;
				break;
			case 0x0D:			// Pattern break
				rowsUsed = new AtomicInteger(0);
				final int newPatternBreakRowIndex = calculateExtendedValue(aktMemo, rowsUsed);
				if (rowsUsed.get()==0)
					patternBreakRowIndex = ((aktMemo.assignedEffektParam>>4)*10)+(aktMemo.assignedEffektParam&0x0F);
				else
					patternBreakRowIndex = newPatternBreakRowIndex;
				if (!isModPlug && patternBreakRowIndex>63) patternBreakRowIndex = 0; // this is funny, as FT allows 0x100 rows... (MPT allows 0x400 though)
				patternJumpRowIndex = patternBreakRowIndex;
				patternBreakSet = true;
				break;
			case 0x0E:
				final int effektOp = aktMemo.assignedEffektParam&0x0F;
				switch (aktMemo.assignedEffektParam>>4)
				{
					case 0x0:	// Set filter (MODs and XMs!) - simulate with IT resonance filter
						// 0: on, 1: off (yes, really!)
						aktMemo.cutOff = ((effektOp&0x01)==0)?0x50:0x7F; // an educated guess on the value, that sounds reasonable...
						// other standard values for the simulation...
						aktMemo.filterMode = ModConstants.FLTMODE_LOWPASS;
						aktMemo.resonance = 0x00;
						setupChannelFilter(aktMemo, !aktMemo.filterOn, 256);
						break;
					case 0x1:	// Fine Porta Up
						if (isMOD && effektOp==0)
							aktMemo.finePortaUp = 0;
						else
						if (effektOp!=0) aktMemo.finePortaUp = effektOp<<ModConstants.PERIOD_SHIFT;
						doPortaUp(aktMemo, aktMemo.finePortaUp);
						break;
					case 0x2:	// Fine Porta Down
						if (isMOD && effektOp==0)
							aktMemo.finePortaDown = 0;
						else
						if (effektOp!=0) aktMemo.finePortaDown = effektOp<<ModConstants.PERIOD_SHIFT;
						doPortaDown(aktMemo, aktMemo.finePortaDown);
						break;
					case 0x3:	// Glissando
						aktMemo.glissando = effektOp!=0;
						break;
					case 0x4:	// Set Vibrato Type
						aktMemo.vibratoType=effektOp&0x3;
						aktMemo.vibratoNoRetrig = (effektOp&0x4)!=0;
						break;
					case 0x5:	// Set FineTune
//						if (isXM && hasNewNote(element)) // XMs: is now done in triggerNote, as FT2 does it
//						{
//							aktMemo.currentFineTune = (effektOp<<4)-128;
//							setNewPlayerTuningFor(aktMemo, getFineTunePeriod(aktMemo));
//						}
//						else
						if (frequencyTableType == ModConstants.AMIGA_TABLE)
						{
							aktMemo.currentFineTune = (effektOp>7)?effektOp-16:effektOp;
							if (hasNewNote(element)) setNewPlayerTuningFor(aktMemo, getFineTunePeriod(aktMemo));
						}
						break;
					case 0x6:	// JumpLoop
						if (effektOp==0) // Set a marker for loop
						{
							aktMemo.jumpLoopPatternRow = currentRow;
						}
						else
						{
							if (aktMemo.jumpLoopRepeatCount<=0) // was not set && effektOp!=0
							{
								aktMemo.jumpLoopRepeatCount = effektOp;
								if (aktMemo.jumpLoopPatternRow<0) aktMemo.jumpLoopPatternRow=0;
							}
							else
							if (--aktMemo.jumpLoopRepeatCount<=0)
							{
								break;
							}
							patternJumpRowIndex = aktMemo.jumpLoopPatternRow;
							patternBreakRowIndex = patternJumpRowIndex; // PT/FT only use one variable for this
							patternJumpSet = true;
						}
						break;
					case 0x7:	// Set Tremolo Type
						aktMemo.tremoloType=effektOp&0x3;
						aktMemo.tremoloNoRetrig = (effektOp&0x4)!=0;
						break;
					case 0x8:	// XM: undefinded, XM ModPlug extended: Fine Panning or MOD:Karplus Strong
						if (isMOD)
							doKarplusStrong(aktMemo);
						else
						if (isXM && isModPlug) doPanning(aktMemo, effektOp, ModConstants.PanBits.Pan4Bit);
						break;
					case 0x9:	// Retrig Note
						aktMemo.retrigCount = aktMemo.retrigMemo = effektOp;
						doMultiRetrigNote(aktMemo, false);
						break;
					case 0xA:	// Fine VolSlide Up
						// With Protracker Mods Fine Volume Slide has not effect memory
						if (isMOD && effektOp==0)
							aktMemo.XMFineVolSlideUp = 0;
						else
						if (effektOp!=0) aktMemo.XMFineVolSlideUp = effektOp;
						doVolumeSlideEffekt(aktMemo, aktMemo.XMFineVolSlideUp<<4);
						break;
					case 0xB:	// Fine VolSlide Down
						// With Protracker Mods Fine Volume Slide has not effect memory
						if (isMOD && effektOp==0)
							aktMemo.XMFineVolSlideDown = 0;
						else
						if (effektOp!=0) aktMemo.XMFineVolSlideDown = effektOp;
						doVolumeSlideEffekt(aktMemo, aktMemo.XMFineVolSlideDown);
						break;
					case 0xC:	// Note Cut
						if (aktMemo.noteCutCount<0)
						{
							if (effektOp==0) // instant noteCut on first Tick
							{
								aktMemo.currentVolume = 0;
								aktMemo.doFastVolRamp = true;
							}
							else
								aktMemo.noteCutCount = effektOp;
						}
						break;
					case 0xD:	// Note Delay
						if (isXM) aktMemo.noteDelayCount = effektOp;
						else
						{
							if (effektOp==0)
								aktMemo.noteDelayCount = -1;
							else
								aktMemo.noteDelayCount = effektOp;
						}
						break;
					case 0xE:	// Pattern Delay
						/*if (patternDelayCount<0)*/ patternDelayCount = effektOp;
						break;
					case 0xF:	// set MIDI Makro (ProTracker: Funk It!)
						if (isXM)
							aktMemo.activeMidiMacro = effektOp;
						else
						if (isMOD)
						{
							aktMemo.EFxSpeed = effektOp;
							doFunkIt(aktMemo);
						}
						break;
				}
				break;
			case 0x0F:			// SET SPEED / BPM
				if (aktMemo.assignedEffektParam>=0x20 && !mod.getModSpeedIsTicks()) // set BPM
				{
					if (isMOD)
					{
						// We do it the next round in either doTickEffects or doRowEffects (with speed 1)
						modSpeedSet = aktMemo.assignedEffektParam;
						if (modSpeedSet>0xFF) modSpeedSet=0xFF;
					}
					else
					{
						// FT:
						currentBPM = calculateExtendedValue(aktMemo, null);
						if (currentBPM>1000) currentBPM = 1000;
					}
				}
				else
				{
					// FT2 appears to be decrementing the tick count before checking for zero,
					// so it effectively counts down 65536 ticks with speed = 0 (song speed is a 16-bit variable in FT2)
					// With ProTracker we need to stop the song
					if (aktMemo.assignedEffektParam==0)
					{
						if (isXM)
							currentTick = currentTempo = 0xFFFF;
						else
						if (isMOD)
						{
							// we do this via the looping fade out with a fade out time of zero
							// to not introduce a new variable here.
							doLoopingGlobalFadeout = true;
							loopingFadeOutValue = 0;
						}
					}
					else
						currentTick = currentTempo = aktMemo.assignedEffektParam;
				}
				break;
			case 0x10:			// Set global volume
				globalVolume = (aktMemo.assignedEffektParam)<<1;
				if (globalVolume>ModConstants.MAXGLOBALVOLUME) globalVolume = ModConstants.MAXGLOBALVOLUME;
				break;
			case 0x11:			// Global volume slide
				doGlobalVolumeSlideEffekt(aktMemo); //ONLY TICK ZERO!
				break;
			case 0x14:			// Key off
				aktMemo.keyOffCounter = aktMemo.assignedEffektParam;
				break;
			case 0x15:			// Set envelope position
				setEnvelopePosition(aktMemo, aktMemo.assignedEffektParam);
				break;
			case 0x19:			// Panning slide
				if ((aktMemo.assignedEffektParam&0xF0)==0)
					aktMemo.panningSlideValue = -((aktMemo.assignedEffektParam&0xF)<<2);
				else
					aktMemo.panningSlideValue = (aktMemo.assignedEffektParam>>4)<<2;
				break;
			case 0x1B:			// Multi retrig note
				if ((aktMemo.assignedEffektParam&0xF)!=0) aktMemo.retrigMemo = aktMemo.assignedEffektParam&0xF;
				if ((aktMemo.assignedEffektParam>>4) !=0) aktMemo.retrigVolSlide = aktMemo.assignedEffektParam>>4;
				if (aktMemo.FT2AllowRetriggerQuirk) doMultiRetrigNote(aktMemo, true);
				break;
			case 0x1D:			// Tremor
				if (aktMemo.assignedEffektParam!=0) aktMemo.tremorOntimeSet = aktMemo.assignedEffektParam;
				break;
			case 0x20:			// EMPTY
				// This effect can be set in OMPT, but is without function (yet?)
				break;
			case 0x21:			// Extended XM Effects
				final int effektOpExParam = aktMemo.assignedEffektParam&0x0F;
				final int effektOpEx = aktMemo.assignedEffektParam>>4;
				if (!isModPlug && effektOpEx>2) break;
				switch (effektOpEx)
				{
					case 0x1:	// Extra Fine Porta Up
						if (effektOpExParam!=0) aktMemo.finePortaUpEx = effektOpExParam<<2;
						aktMemo.currentNotePeriod -= aktMemo.finePortaUpEx;
						if (isXM)
						{
							final int tmpPeriod = aktMemo.currentNotePeriod>>(ModConstants.PERIOD_SHIFT-2);
							if ((short)tmpPeriod<1) aktMemo.currentNotePeriod = 1<<(ModConstants.PERIOD_SHIFT-2);
						}
						setNewPlayerTuningFor(aktMemo);
						break;
					case 0x2:	// Extra Fine Porta Down
						if (effektOpExParam!=0) aktMemo.finePortaDownEx = effektOpExParam<<2;
						aktMemo.currentNotePeriod += aktMemo.finePortaDownEx;
						if (isXM)
						{
							// FT2 bug, should've been unsigned comparison
							final int tmpPeriod = aktMemo.currentNotePeriod>>(ModConstants.PERIOD_SHIFT-2);
							if ((short)tmpPeriod>32000) aktMemo.currentNotePeriod = (32000-1)<<(ModConstants.PERIOD_SHIFT-2);
						}
						setNewPlayerTuningFor(aktMemo);
						break;
					case 0x5: 			// set PanBrello Waveform
						aktMemo.panbrelloType=effektOpExParam&0x3;
						aktMemo.panbrelloNoRetrig = ((effektOpExParam&0x04)!=0);
						break;
					case 0x6: 			// Fine Pattern Delay --> # of ticks - OpenModPlug Effect!
						if (isModPlug /*&& patternDelayCount<0*/)
							patternTicksDelayCount += effektOpExParam;
						break;
					case 0x9: // Sound Control
						switch (effektOpExParam)
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
							case 0xC: // Select global filter mode (IT compatibility). This is the default, when resonant filters are enabled with a Zxx effect, they will stay active until explicitely disabled by setting the cutoff frequency to the maximum (Z7F), and the resonance to the minimum (Z80).
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
					case 0xA: 			// Set High Offset
						aktMemo.highSampleOffset = aktMemo.assignedEffektParam&0x0F;
						break;
					default:
						//Log.debug(String.format("Unknown Extended Effect: Effect:%02X Op:%02X in [Pattern:%03d: Row:%03d Channel:%03d]", Integer.valueOf(aktMemo.effekt), Integer.valueOf(aktMemo.effektParam), Integer.valueOf(currentPatternIndex), Integer.valueOf(currentRow), Integer.valueOf(aktMemo.channelNumber+1)));
						break;
				}
				break;
			case 0x22: 			// Panbrello
				if ((aktMemo.assignedEffektParam>>4)!=0) aktMemo.panbrelloStep = aktMemo.assignedEffektParam>>4;
				if ((aktMemo.assignedEffektParam&0xF)!=0) aktMemo.panbrelloAmplitude = aktMemo.assignedEffektParam&0xF;
				aktMemo.panbrelloOn = true;
				doPanbrelloEffekt(aktMemo);
				break;
			case 0x23:			// Midi Macro
				final MidiMacros macro = mod.getMidiConfig();
				if (macro!=null)
				{
		            if (aktMemo.assignedEffektParam<0x80)
		                processMIDIMacro(aktMemo, false, macro.getMidiSFXExt(aktMemo.activeMidiMacro), aktMemo.assignedEffektParam);
		            else
		                processMIDIMacro(aktMemo, false, macro.getMidiZXXExt(aktMemo.assignedEffektParam & 0x7F), 0);
				}
	            break;
			case 0x24:			// Smooth Midi Macro
				final MidiMacros smoothMacro = mod.getMidiConfig();
				if (smoothMacro!=null)
				{
		            if (aktMemo.assignedEffektParam<0x80)
		                processMIDIMacro(aktMemo, true, smoothMacro.getMidiSFXExt(aktMemo.activeMidiMacro), aktMemo.assignedEffektParam);
		            else
		                processMIDIMacro(aktMemo, true, smoothMacro.getMidiZXXExt(aktMemo.assignedEffektParam & 0x7F), 0);
				}
	            break;
			case 0x26:			// Parameter Extension
				// OMPT Specific, done as a look ahead, so just break here
				break;
			default:
				//Log.debug(String.format("Unknown Effect: Effect:%02X Op:%02X in [Pattern:%03d: Row:%03d Channel:%03d]", Integer.valueOf(aktMemo.effekt), Integer.valueOf(aktMemo.effektParam), Integer.valueOf(currentPatternIndex), Integer.valueOf(currentRow), Integer.valueOf(aktMemo.channelNumber+1)));
				break;
		}
	}
	/**
	 * PT2 Compatibility.
	 * @since 18.03.2024
	 * @param aktMemo
	 * @param myPeriod
	 * @param arpNote
	 * @return
	 */
	private int adjustPTPeriodFromNote(final ChannelMemory aktMemo, final int myPeriod, final int arpNote)
	{
		final int lookUpFineTune = ((aktMemo.currentFineTune<0)?aktMemo.currentFineTune+16:aktMemo.currentFineTune)*37;
		final int period = myPeriod>>ModConstants.PERIOD_SHIFT;
		// The original coding in ProTracker
		int i=0;
		while (true)
		{
			if (period>=ModConstants.periodTable[lookUpFineTune + i])
				break;
			if (++i>=37)
			{
				i=35;
				break;
			}
		}
		return ModConstants.periodTable[lookUpFineTune + i + arpNote]<<ModConstants.PERIOD_SHIFT;
	}
	/**
	 * FT2 Compatibility. This way we do not need to try it, we are...
	 * @since 14.03.2024
	 * @param aktMemo
	 * @param period
	 * @param arpNote
	 * @return
	 */
	private int adjustFTPeriodFromNote(final ChannelMemory aktMemo, final int myPeriod, final int arpNote)
	{
		final int period = myPeriod>>(ModConstants.PERIOD_SHIFT-2);
		int tmpPeriod;

		final int fineTune = (aktMemo.currentFineTune >> 3) + 16;

		// FT2 bug, should've been 10*12*16. Notes above B-7 (95) will have issues.
		// You can only achieve such high notes by having a high relative note setting.
		int hiPeriod = 8*12*16;
		int loPeriod = 0;

		for (int i=0; i<8; i++)
		{
			tmpPeriod = (((loPeriod + hiPeriod) >> 1) & ~15) + fineTune;

			int lookUp = tmpPeriod - 8;
			if (lookUp<0) lookUp = 0; // safety fix (C-0 w/ f.tune <= -65). This seems to result in 0 in FT2

			if (period>=note2Period[lookUp])
				hiPeriod = (tmpPeriod - fineTune) & ~15;
			else
				loPeriod = (tmpPeriod - fineTune) & ~15;
		}

		tmpPeriod = loPeriod + fineTune + (arpNote << 4);
		if (tmpPeriod >= (8*12*16+15)-1) // FT2 bug, should've been 10*12*16+16 (also notice the +2 difference)
			tmpPeriod = (8*12*16+16)-1;

		return note2Period[tmpPeriod]<<(ModConstants.PERIOD_SHIFT-2);
	}
	/**
	 * @since 14.03.2024
	 * @param aktMemo
	 */
	private void doArpeggio(final ChannelMemory aktMemo)
	{
		if (isMOD)
		{
			final int tick = currentTempo - currentTick; // we count downwards...
			aktMemo.arpeggioIndex = tick%3;
		}
		else
		{
			if (currentTick>16) aktMemo.arpeggioIndex = 2;
			else
			if (currentTick==16) aktMemo.arpeggioIndex = 0;
			else
				aktMemo.arpeggioIndex = currentTick % 3;
		}
		setNewPlayerTuningFor(aktMemo, aktMemo.arpeggioNote[aktMemo.arpeggioIndex]);
	}
	/**
	 * @since 20.03.2024
	 * @param aktMemo
	 */
	protected void preparePortaToNoteEffect(final ChannelMemory aktMemo)
	{
		final PatternElement element = aktMemo.currentElement;
		if (frequencyTableType == ModConstants.AMIGA_TABLE)
		{
			if (hasNewNote(element))
			{
				// The original coding in ProTracker
				final int lookUpFineTune = ((aktMemo.currentFineTune<0)?aktMemo.currentFineTune+16:aktMemo.currentFineTune)*37;
				final int period = aktMemo.assignedNotePeriod;
				int i=0;
				while (true)
				{
					if (period>=ModConstants.periodTable[lookUpFineTune + i])
						break;
					if (++i>=37)
					{
						i=35;
						break;
					}
				}

				if (aktMemo.currentFineTune<0 && i>0) i--;

				aktMemo.portaTargetNotePeriod = ModConstants.periodTable[lookUpFineTune + i]<<ModConstants.PERIOD_SHIFT;
				aktMemo.portamentoDirection_PT_FT = 0;

				if (aktMemo.currentNotePeriodSet == aktMemo.portaTargetNotePeriod) aktMemo.portaTargetNotePeriod = 0;
				else
				if (aktMemo.currentNotePeriodSet > aktMemo.portaTargetNotePeriod) aktMemo.portamentoDirection_PT_FT = 1;
			}
		}
		else
		{
			final boolean isKeyOff = (element.getPeriod()==ModConstants.KEY_OFF || element.getNoteIndex()==ModConstants.KEY_OFF);
			if (isKeyOff)
			{
				doKeyOff(aktMemo);
			}
			else
			if (hasNewNote(element)) // KeyOff is not a note...
			{
				// because of p->note being uint8, we need to simulate
				final int note = (((aktMemo.assignedNoteIndex + aktMemo.currentTranspose - 1)&0xFF)<<4) + ((aktMemo.currentFineTune>>3) + 16);
				if (note<(10*12*16)+16)
				{
					aktMemo.portaTargetNotePeriod = note2Period[note]<<(ModConstants.PERIOD_SHIFT-2);
					if (aktMemo.portaTargetNotePeriod == aktMemo.currentNotePeriod)
						aktMemo.portamentoDirection_PT_FT = 0;
					else if (aktMemo.portaTargetNotePeriod > aktMemo.currentNotePeriod)
						aktMemo.portamentoDirection_PT_FT = 1;
					else
						aktMemo.portamentoDirection_PT_FT = 2;
				}
			}

			if (element.getInstrument()>0)
			{
				resetVolumeAndPanning(aktMemo, aktMemo.assignedInstrument, aktMemo.assignedSample);
				if (!isKeyOff) triggerFTInstrument(aktMemo);
			}
		}
	}
	/**
	 * Convenient Method for the Porta to note Effekt
	 * @param aktMemo
	 */
	protected void doPortaToNoteEffect(final ChannelMemory aktMemo)
	{
		// in FT2, things are very special
		if ((isXM && aktMemo.portamentoDirection_PT_FT==0) ||
			(isMOD && aktMemo.portaTargetNotePeriod<=0)) return;

		if ((isXM && aktMemo.portamentoDirection_PT_FT>1) ||
			(isMOD && aktMemo.portamentoDirection_PT_FT>0))
		{
			aktMemo.currentNotePeriod -= aktMemo.portaNoteStep;
			if (aktMemo.currentNotePeriod<=aktMemo.portaTargetNotePeriod)
			{
				aktMemo.currentNotePeriod=aktMemo.portaTargetNotePeriod;
				if (isXM) aktMemo.portamentoDirection_PT_FT = 1;
				if (isMOD) aktMemo.portaTargetNotePeriod = 0;
			}
		}
		else
		{
			aktMemo.currentNotePeriod += aktMemo.portaNoteStep;
			if (aktMemo.currentNotePeriod>=aktMemo.portaTargetNotePeriod)
			{
				aktMemo.currentNotePeriod=aktMemo.portaTargetNotePeriod;
				if (isXM) aktMemo.portamentoDirection_PT_FT = 1;
				if (isMOD) aktMemo.portaTargetNotePeriod = 0;
			}
		}

		if (aktMemo.glissando)
		{
			if (isMOD)
				setNewPlayerTuningFor(aktMemo, adjustPTPeriodFromNote(aktMemo, aktMemo.currentNotePeriod, 0));
			else
				setNewPlayerTuningFor(aktMemo, adjustFTPeriodFromNote(aktMemo, aktMemo.currentNotePeriod, 0));
		}
		else
			setNewPlayerTuningFor(aktMemo);
	}
	/**
	 * Convenient Method for the Porta Up Effekt
	 * @since 08.06.2020
	 * @param aktMemo
	 */
	private void doPortaUp(final ChannelMemory aktMemo, final int op)
	{
		aktMemo.currentNotePeriod -= op;
		if (isMOD)
		{
			// PT BUG: sign removed before comparison, underflow not clamped!
			final int tmpPeriod = aktMemo.currentNotePeriod>>ModConstants.PERIOD_SHIFT;
			if ((tmpPeriod&0xFFF)<113) aktMemo.currentNotePeriod = ((tmpPeriod&0xF000) | 113)<<ModConstants.PERIOD_SHIFT;
		}
		else
		{
			// FT2 bug, should've been unsigned comparison
			final int tmpPeriod = aktMemo.currentNotePeriod>>(ModConstants.PERIOD_SHIFT-2);
			if ((short)tmpPeriod<1) aktMemo.currentNotePeriod = 1<<(ModConstants.PERIOD_SHIFT-2);
		}

		setNewPlayerTuningFor(aktMemo);
	}
	/**
	 * Convenient Method for the Porta Down Effekt
	 * @since 08.06.2020
	 * @param aktMemo
	 */
	private void doPortaDown(final ChannelMemory aktMemo, final int op)
	{
		aktMemo.currentNotePeriod += op;
		if (isMOD)
		{
			// PT BUG: sign removed before comparison, underflow not clamped!
			final int tmpPeriod = aktMemo.currentNotePeriod>>ModConstants.PERIOD_SHIFT;
			if ((tmpPeriod&0xFFF)>856) aktMemo.currentNotePeriod = ((tmpPeriod&0xF000) | 856)<<ModConstants.PERIOD_SHIFT;
		}
		else
		{
			// FT2 bug, should've been unsigned comparison
			final int tmpPeriod = aktMemo.currentNotePeriod>>(ModConstants.PERIOD_SHIFT-2);
			if ((short)tmpPeriod>32000) aktMemo.currentNotePeriod = (32000-1)<<(ModConstants.PERIOD_SHIFT-2);
		}

		setNewPlayerTuningFor(aktMemo);
	}
	/**
	 * @param aktMemo
	 * @param currentSample
	 * @see de.quippy.javamod.multimedia.mod.mixer.BasicModMixer#doAutoVibratoEffekt(de.quippy.javamod.multimedia.mod.mixer.BasicModMixer.ChannelMemory, de.quippy.javamod.multimedia.mod.loader.instrument.Sample)
	 */
	@Override
	protected void doAutoVibratoEffekt(final ChannelMemory aktMemo, final Sample currentSample, final int currentPeriod)
	{
		if (currentSample.vibratoDepth==0) return;

		int autoVibAmp;
		if (aktMemo.autoVibratoSweep > 0)
		{
			autoVibAmp = currentSample.vibratoSweep;
			if (!aktMemo.keyOff)
			{
				final int sampleAutoVibDepth = currentSample.vibratoDepth<<8;

				autoVibAmp += aktMemo.autoVibratoAmplitude;
				if (autoVibAmp > sampleAutoVibDepth)
				{
					autoVibAmp = sampleAutoVibDepth;
					aktMemo.autoVibratoSweep = 0;
				}
			}
		}
		else
			autoVibAmp = aktMemo.autoVibratoAmplitude;

		aktMemo.autoVibratoTablePos = (aktMemo.autoVibratoTablePos + currentSample.vibratoRate) & 0xFF;
		int periodAdd; // values -64..+64 - not -256..+256 !!
		switch (currentSample.vibratoType & 0x03)
		{
			default:
			case 0:	periodAdd = ModConstants.XMAutoVibSineTab[aktMemo.autoVibratoTablePos];	// Sine
					break;
			case 1:	periodAdd = (aktMemo.autoVibratoTablePos>127) ? +0x40 : -0x40;			// Square
					break;
			case 2:	periodAdd = ((0x40 + (aktMemo.autoVibratoTablePos>>1)) & 0x7F) - 0x40;	// Ramp Up
					break;
			case 3:	periodAdd = ((0x40 - (aktMemo.autoVibratoTablePos>>1)) & 0x7F) - 0x40;	// Ramp Down
					break;
		}
		periodAdd =	((periodAdd<<ModConstants.PERIOD_SHIFT) * autoVibAmp) >> (6+8+2); // copy from FT2 source code plus our PERIOD_SHIFT is 4, not 2

		int newPeriod = currentPeriod + periodAdd;
		if (newPeriod>=(32000<<ModConstants.PERIOD_SHIFT))
			newPeriod = 0;

		setNewPlayerTuningFor(aktMemo, newPeriod);
	}
	/**
	 * returns values in the range of -255..255
	 * @since 29.06.2020
	 * @param type
	 * @param position
	 * @return
	 */
	private int getVibratoDelta(final int type, final int position)
	{
		int value = position & 0x1F;
		final boolean positionOverrun = (position&0x3F)>=32;
		switch (type & 3)
		{
			case 0:
				value = ModConstants.ModVibratoTable[value];
				break;
			case 1:
				value <<= 3;
				if (positionOverrun) value = 255-value;
				break;
			default:
				value = 255;
		}
		return (positionOverrun)?-value:value;
	}
	/**
	 * Convenient Method for the vibrato effekt
	 * @param aktMemo
	 */
	private void doVibratoEffekt(final ChannelMemory aktMemo)
	{
		final boolean isTick0 = currentTick==currentTempo;
		if (isTick0) return; // nothing more to do

		int tmpVib = getVibratoDelta(aktMemo.vibratoType, aktMemo.vibratoTablePos);
		tmpVib = ((tmpVib<<ModConstants.PERIOD_SHIFT) * aktMemo.vibratoAmplitude) >> 7;

		setNewPlayerTuningFor(aktMemo, aktMemo.currentNotePeriod+tmpVib);
		if (!isTick0) aktMemo.vibratoTablePos += aktMemo.vibratoStep;
	}
	/**
	 * Convenient Method for the tremolo effekt
	 * @param aktMemo
	 */
	private void doTremoloEffekt(final ChannelMemory aktMemo)
	{
		final boolean isTick0 = currentTick==currentTempo;
		if (isTick0) return; // nothing more to do

		int delta;
		if ((aktMemo.tremoloType&0x3)==1 && mod.getFT2Tremolo())
		{
			int value = (aktMemo.tremoloTablePos & 0x1F)<<3;
			if ((aktMemo.vibratoTablePos&0x3F)>=32) value = 255-value; // FT2 copy&paste bug
			delta = ((aktMemo.tremoloTablePos&0x3F)>=32)?-value:value;
		}
		else
			delta = getVibratoDelta(aktMemo.tremoloType, aktMemo.tremoloTablePos);

		aktMemo.currentVolume = aktMemo.currentInstrumentVolume + ((delta * aktMemo.tremoloAmplitude) >> 6); // normally >>8 because -256..+256
		if (aktMemo.currentVolume>ModConstants.MAX_SAMPLE_VOL) aktMemo.currentVolume = ModConstants.MAX_SAMPLE_VOL;
		else
		if (aktMemo.currentVolume<ModConstants.MIN_SAMPLE_VOL) aktMemo.currentVolume = ModConstants.MIN_SAMPLE_VOL;

		if (!isTick0) aktMemo.tremoloTablePos += aktMemo.tremoloStep;
	}
	/**
	 * Convenient Method for the panbrello effekt (only OMPT extended XM style!)
	 * @param aktMemo
	 */
	private void doPanbrelloEffekt(final ChannelMemory aktMemo)
	{
		final int pDelta = getVibratoDelta(aktMemo.panbrelloType, (aktMemo.panbrelloTablePos+0x10)>>2); // start with top value and be slow
		final int newPanning = aktMemo.currentInstrumentPanning + (((pDelta * aktMemo.panbrelloAmplitude) + 4) >> 4); // +4: round me at bit 2
		aktMemo.panning = (newPanning<0)?0:((newPanning>256)?256:newPanning);
		aktMemo.doFastVolRamp=true;

		aktMemo.panbrelloTablePos += aktMemo.panbrelloStep;
	}
	/**
	 * The tremor effect
	 * It was basically implemented in FT2 for STM compatibility.
	 * However, as it is an FT2 effect, we should do it as FT2 implemented it
	 * @param aktMemo
	 */
	private void doTremorEffekt(final ChannelMemory aktMemo)
	{
		// For FT we need two parameter: tremorParam (we use tremorOntimeSet) and tremorPos (we use tremorOfftimeSet)
		// That way we can recycle those parameters for ScreamTracker
		// tremorOfftimeSet is reset at "triggerInstrument"
		final int param = aktMemo.tremorOntimeSet;
		int tremorSign = aktMemo.tremorOfftimeSet&0x80;
		int tremorData = aktMemo.tremorOfftimeSet&0x7F;

		tremorData--;
		if (tremorData<0)
		{
			if (tremorSign==0x80)
			{
				tremorSign = 0x00;
				tremorData = param&0x0F;
			}
			else
			{
				tremorSign = 0x80;
				tremorData = param>>4;
			}
		}

		aktMemo.tremorOfftimeSet = tremorSign | tremorData;
		aktMemo.currentVolume = (tremorSign==0x80)?aktMemo.currentInstrumentVolume:0;
		aktMemo.doFastVolRamp = true;
	}
	/**
	 * Convenient Method for the VolumeSlide effect
	 * @param aktMemo
	 */
	private void doVolumeSlideEffekt(final ChannelMemory aktMemo, final int volumeSlideValue)
	{
		if ((volumeSlideValue&0xF0)==0)
		{
			aktMemo.currentVolume -= volumeSlideValue&0xF;
			if (aktMemo.currentVolume<ModConstants.MIN_SAMPLE_VOL) aktMemo.currentVolume = ModConstants.MIN_SAMPLE_VOL;
		}
		else
		{
			aktMemo.currentVolume += volumeSlideValue>>4;
			if (aktMemo.currentVolume>ModConstants.MAX_SAMPLE_VOL) aktMemo.currentVolume = ModConstants.MAX_SAMPLE_VOL;
		}

		aktMemo.currentInstrumentVolume = aktMemo.currentVolume;
	}
	/**
	 * Convenient Method for the Global VolumeSlide effect
	 * Only on Tick Zero!
	 * @since 21.06.2006
	 * @param aktMemo
	 */
	private void doGlobalVolumeSlideEffekt(final ChannelMemory aktMemo)
	{
		// Global Volume here is 0..128 (not 0..64) - so <<1 the param
		if (aktMemo.assignedEffektParam!=0) aktMemo.globalVolumSlideValue = aktMemo.assignedEffektParam;
		if ((aktMemo.globalVolumSlideValue&0xF0)==0)
		{
			globalVolume -= (aktMemo.globalVolumSlideValue&0xF)<<1;
			if (globalVolume<0) globalVolume = 0;
		}
		else
		{
			globalVolume += ((aktMemo.globalVolumSlideValue>>4)&0xF)<<1;
			if (globalVolume>ModConstants.MAXGLOBALVOLUME) globalVolume = ModConstants.MAXGLOBALVOLUME;
		}
	}
	/**
	 * @since 18.01.2024
	 * @param aktMemo
	 * @param element
	 */
	private void doSampleOffsetEffekt(final ChannelMemory aktMemo, final PatternElement element)
	{
		if (isMOD) aktMemo.prevSampleOffset += aktMemo.sampleOffset;

		if (hasNoNote(element) || aktMemo.currentSample==null) return;

		final Sample sample = aktMemo.currentSample;
		final boolean hasLoop = (sample.loopType & ModConstants.LOOP_ON)!=0;
		final int length = hasLoop?sample.loopStop:sample.length;

		if (isMOD)
		{
			// ProTracker >64K sample offset "silent bug" (or early loop)
			if ((short)(aktMemo.sampleOffset>>1) < (short)(length>>1))
			{
				// ProTracker does not set the sample offset, but adds it to the
				// sample start plus reduces the sample length memorized for play back.
				// If length is consumed (DMA-Cycle finishes) the sample loop values
				// are prepared (which are 0->2 if no loop) - so setting length to 1
				// will result in an instant loop or sample stop.
				// This is done in a method called "checkMoreEffects",
				// which is called last in setPeriod, but with "0x9xx"-Effect
				// explicitly also before setPeriod.
				// That said: sampleOffsets not only add up but also do that twice.
				aktMemo.currentSamplePos = aktMemo.prevSampleOffset;
				aktMemo.prevSampleOffset += aktMemo.sampleOffset;
			}
			else
				aktMemo.currentSamplePos = length;

			if (aktMemo.currentSamplePos >= length)
			{
				if (hasLoop)
					aktMemo.currentSamplePos = sample.loopStart;
				else
					aktMemo.currentSamplePos = sample.length-1;
			}
		}
		else // FT2
		{
			aktMemo.currentSamplePos = aktMemo.sampleOffset;
			if (aktMemo.currentSamplePos >= length)
			{
				aktMemo.currentSamplePos = sample.length-1;
				aktMemo.instrumentFinished = true;
				setNewPlayerTuningFor(aktMemo, aktMemo.currentNotePeriod = 0); // FT2 Compatibility: Don't play note if offset is beyond sample/loop length
			}
		}
	}
	/**
	 * Convenient Method for the VolumeSlide Effect
	 * @param aktMemo
	 */
	private void doPanningSlideEffekt(final ChannelMemory aktMemo)
	{
		aktMemo.doSurround = false;
		aktMemo.panning += aktMemo.panningSlideValue;
		if (aktMemo.panning<0) aktMemo.panning=0; else if (aktMemo.panning>256) aktMemo.panning=256;
	}
	/**
	 * Retriggers the note and does volume slide (if withVolSlide is true)
	 * @since 04.04.2020
	 * @param aktMemo
	 */
	private void doMultiRetrigNote(final ChannelMemory aktMemo, final boolean withVolSlide)
	{
		final int tick = currentTempo - currentTick; // we count downwards, so convert...
		boolean doRetrig = false;

		// ProTracker and FastTracker implement re-trigger differently, which has effects at pattern delays
		if (isMOD)
		{
			if (aktMemo.retrigCount<=0)
				doRetrig = false;
			else
			{
				if (tick==0 && hasNewNote(aktMemo.currentElement)) // Re-trigger on first tick if there is no note (0 % x) is always 0
					doRetrig = false;
				else
					doRetrig = ((tick % aktMemo.retrigCount)==0);
			}
			if (doRetrig)
				resetInstrumentPointers(aktMemo, true);
			return;
		}

		// FT2 retrigger
		if (!withVolSlide) // normal retrigger
		{
			if (aktMemo.retrigMemo==-1) return;

			// E90 re-triggers once on tick 0 (in triggerNote)
			if (aktMemo.retrigMemo==0)
			{
				if (currentTick==currentTempo)
				{
					aktMemo.retrigMemo = -1;
					doRetrig = true;
				}
			}
			else
			if (tick>0 && (tick % aktMemo.retrigMemo) == 0) // only on tick non zero
			{
				doRetrig = true;
			}

			if (doRetrig)
			{
				// FT2 does triggerNote and triggerInstrument at this point
				// triggerNote will reset the sample and recalculate the period
				// and set that - but as that should not have changed, at the
				// end it's only resetting the sample pointer
				triggerFTNote(aktMemo, 0);
				// triggerInstrument:
				triggerFTInstrument(aktMemo);
			}
			return;
		}
		else // multi retrigger note
		{
			final int cnt = aktMemo.retrigCount + 1;
			if (cnt < aktMemo.retrigMemo)
			{
				aktMemo.retrigCount = cnt;
				return;
			}

			aktMemo.retrigCount = 0;

			int vol = aktMemo.currentVolume;
			switch (aktMemo.retrigVolSlide)
			{
				case 0x1: vol--; break;
				case 0x2: vol-=  2; break;
				case 0x3: vol-=  4; break;
				case 0x4: vol-=  8; break;
				case 0x5: vol-= 16; break;
				case 0x6: vol = (vol>>1) + (vol>>3) + (vol>>4); break;	// only approximately 2/3 (11/16)
				case 0x7: vol>>=1; break;
				case 0x8: /* No volume Change */ break;
				case 0x9: vol++; break;
				case 0xA: vol+=  2; break;
				case 0xB: vol+=  4; break;
				case 0xC: vol+=  8; break;
				case 0xD: vol+= 16; break;
				case 0xE: vol=  (vol>>1) + vol; break;					// exactly (vol*3)/2 (vol+(vol/2))
				case 0xF: vol<<=1; break;
				default: break;
			}
			if (vol>ModConstants.MAX_SAMPLE_VOL) vol = ModConstants.MAX_SAMPLE_VOL;
			else
			if (vol<ModConstants.MIN_SAMPLE_VOL) vol = ModConstants.MIN_SAMPLE_VOL;
			aktMemo.currentInstrumentVolume = aktMemo.currentVolume = vol;

			// FT2 resets volume column vol / panning at re-trigger
			if (aktMemo.currentAssignedVolumeEffekt==0x01 || aktMemo.currentAssignedVolumeEffekt==0x08)
				doVolumeColumnRowEffekt(aktMemo);

			aktMemo.doFastVolRamp = true;

			triggerFTNote(aktMemo, 0);
		}
	}
	/**
	 * 8bitbubsy delivered a solution for this effect, MPT implemented it as well
	 * Why MPT does also consider sustainLoops (MODs do not have that) is beyond
	 * my knowledge...
	 * @since 31.01.2024
	 * @param aktMemo
	 * @param effectOp
	 */
	private void doFunkIt(final ChannelMemory aktMemo)
	{
		if (aktMemo.EFxSpeed==0) return;

		final Sample sample = aktMemo.currentSample;
		if (sample==null || !sample.hasSampleData() || (sample.loopType&ModConstants.LOOP_ON)==0)
			return;

		aktMemo.EFxDelay += ModConstants.modEFxTable[aktMemo.EFxSpeed & 0x0F];
		if (aktMemo.EFxDelay>=0x80)
		{
			aktMemo.EFxDelay = 0;

			if (++aktMemo.EFxOffset >= sample.loopLength) aktMemo.EFxOffset = 0;

			final int sampleIndex = sample.loopStart + aktMemo.EFxOffset + Sample.INTERPOLATION_LOOK_AHEAD;
			sample.sampleL[sampleIndex] = ~sample.sampleL[sampleIndex];
			//sample.addInterpolationLookAheadData();
		}
	}
	/**
	 * 8bitbubsy says:
	 * This is a little used effect, despite being present in original ProTracker.
	 * E8x was sometimes entirely replaced with code used for demo fx syncing in
	 * demo mod players
	 * @since 09.03.2024
	 * @param aktMemo
	 */
	private void doKarplusStrong(final ChannelMemory aktMemo)
	{
		if (!ModConstants.SUPPORT_E8x_EFFECT) return;

		final Sample sample = aktMemo.currentSample;
		if (sample==null || sample.sampleL==null || (sample.loopType&(ModConstants.LOOP_ON | ModConstants.LOOP_SUSTAIN_ON))==0)
			return;

		final int loopStart = (sample.loopType&ModConstants.LOOP_ON)!=0?sample.loopStart:sample.sustainLoopStart;
		int loopLength = (sample.loopType&ModConstants.LOOP_ON)!=0?sample.loopLength:sample.sustainLoopLength;
		int sampleIndex = loopStart + Sample.INTERPOLATION_LOOK_AHEAD;
		do
		{
			final long a = sample.sampleL[sampleIndex];
			final long b = sample.sampleL[(loopLength==1)?loopStart + Sample.INTERPOLATION_LOOK_AHEAD:sampleIndex + 1];
			sample.sampleL[sampleIndex++] = (a + b) >> 1;
		}
		while (--loopLength >= 0);
	}
	/**
	 * We copied that from the source code of FT2.
	 * @since 02.07.2024
	 * @param env
	 * @param forTick
	 * @return
	 */
	protected int getEnvelopePositionForTick(final Envelope env, final int forTick)
	{
		int point = 0;
		boolean envUpdate = true;
		int tick = forTick;

		if (env.nPoints>1)
		{
			point++;
			for (int i=0; i<env.nPoints-1; i++)
			{
				if (tick<env.positions[point])
				{
					point--;

					tick -= env.positions[point];
					if (tick==0) // FT2 doesn't test for <= 0 here
					{
						envUpdate=false;
						break;
					}

					final int xDiff = env.positions[point+1] - env.positions[point];
					if (xDiff<=0)
					{
						envUpdate=true;
						break;
					}
					point++;
					envUpdate=false;
					break;
				}
				point++;
			}
			if (envUpdate) point--;
		}
		if (point >= env.nPoints)
		{
			point = env.nPoints-1;
			if (point<0) point = 0;
		}
		return point;
	}
	/**
	 * @since 02.07.2024
	 * @param aktMemo
	 * @param position
	 */
	protected void setEnvelopePosition(final ChannelMemory aktMemo, final int position)
	{
		final Instrument ins = aktMemo.currentAssignedInstrument;
		if (ins==null) return;
		final Envelope volEnv = ins.volumeEnvelope;
		if (volEnv!=null && volEnv.on)
		{
			aktMemo.volEnvTick = position - 1;
			aktMemo.volXMEnvPos = getEnvelopePositionForTick(volEnv, position);
		}
		final Envelope panEnv = ins.panningEnvelope;
		if (panEnv!=null && (volEnv!=null && volEnv.sustain)) // FT2 REPLAYER BUG: Should've been ins->panEnvFlags and ENV_ENABLED
		{
			aktMemo.panEnvTick = position - 1;
			aktMemo.panXMEnvPos = getEnvelopePositionForTick(panEnv, position);
		}
	}
	/**
	 * Do the Effekts during Ticks
	 * @param aktMemo
	 */
	@Override
	protected void doTickEffekts(final ChannelMemory aktMemo)
	{
		if (isMOD) doFunkIt(aktMemo);

		if (aktMemo.assignedEffekt==0 && aktMemo.assignedEffektParam==0) return;

		switch (aktMemo.assignedEffekt)
		{
			case 0x00 :			// Arpeggio
				doArpeggio(aktMemo);
				break;
			case 0x01: 			// Porta Up
				doPortaUp(aktMemo, aktMemo.portaStepUp);
				break;
			case 0x02: 			// Porta Down
				doPortaDown(aktMemo, aktMemo.portaStepDown);
				break;
			case 0x03 :			// Porta to Note
				doPortaToNoteEffect(aktMemo);
				break;
			case 0x04 :			// Vibrato
				doVibratoEffekt(aktMemo);
				break;
			case 0x05 :			// Porta to Note + VolumeSlide
				doPortaToNoteEffect(aktMemo);
				doVolumeSlideEffekt(aktMemo, aktMemo.volumSlideValue);
				break;
			case 0x06:			// Vibrato + VolumeSlide
				doVibratoEffekt(aktMemo);
				doVolumeSlideEffekt(aktMemo, aktMemo.volumSlideValue);
				break;
			case 0x07 :			// Tremolo
				doTremoloEffekt(aktMemo);
				break;
			case 0x0A : 		// VolumeSlide
				doVolumeSlideEffekt(aktMemo, aktMemo.volumSlideValue);
				break;
			case 0x0E :			// Extended
				switch (aktMemo.assignedEffektParam>>4)
				{
					// Fine* effects to repeat on patternDelayCount == 0 with MODs (!NOT! with XMs)
					case 0x1:	// Fine Porta Up
					case 0x2:	// Fine Porta Down
					case 0xA:	// Fine VolSlide Up
					case 0xB:	// Fine VolSlide Down
						if (isMOD && patternDelayCount>0 && currentTick==currentTempo) doRowEffects(aktMemo);
						break;
					case 0x8:	// KarpusStrong or Fine Panning (repeat that on first tick in patternDelay)
						if (isMOD) doKarplusStrong(aktMemo);
						break;
					case 0x9 :	// Retrig Note
						doMultiRetrigNote(aktMemo, false);
						break;
					case 0xC :	// Note Cut
						if (aktMemo.noteCutCount>0)
						{
							aktMemo.noteCutCount--;
							if (aktMemo.noteCutCount<=0)
							{
								aktMemo.noteCutCount=-1;
								aktMemo.currentVolume = 0;
								aktMemo.doFastVolRamp = true;
							}
						}
						break;
					case 0xD:	// Note Delay
						if ((currentTempo - currentTick) == aktMemo.noteDelayCount)
						{
							// retrigger instrument...
							if (isMOD)
							{
								triggerPTPeriod(aktMemo);
							}
							if (isXM)
							{
								// copy last seen values from pattern - only effect values first
								aktMemo.assignedEffekt = aktMemo.currentAssignedEffekt;
								aktMemo.assignedEffektParam = aktMemo.currentAssignedEffektParam;
								aktMemo.assignedVolumeEffekt = aktMemo.currentAssignedVolumeEffekt;
								aktMemo.assignedVolumeEffektOp = aktMemo.currentAssignedVolumeEffektOp;

								final PatternElement element = aktMemo.currentElement;
								triggerFTNote(aktMemo, element.getNoteIndex());
								if (element.getInstrument()>0) resetVolumeAndPanning(aktMemo, aktMemo.currentAssignedInstrument, aktMemo.currentSample);
								triggerFTInstrument(aktMemo);
								// With XMs, at finishing a note delay, only volume (0x01) or panning (0x08) are
								// executed (explicitly at noteDelay())
								// This affects volume slides and kills fine vol slides, vibrato adjustments or porta2note
								if (aktMemo.currentAssignedVolumeEffekt==0x01 || aktMemo.currentAssignedVolumeEffekt==0x08)
									doVolumeColumnRowEffekt(aktMemo);
							}
						}
						break;
				}
				break;
			case 0x11 :			// Global volume slide
				//doGlobalVolumeSlideEffekt(aktMemo); ONLY TICK ZERO!
				break;
			case 0x14 :			// Key off
				if (aktMemo.keyOffCounter>0)
				{
					aktMemo.keyOffCounter--;
					if (aktMemo.keyOffCounter<=0)
					{
						aktMemo.keyOffCounter = -1;
						doKeyOff(aktMemo);
					}
				}
				break;
			case 0x19 :			// Panning slide
				doPanningSlideEffekt(aktMemo);
				break;
			case 0x1B:			// Multi retrig note
				doMultiRetrigNote(aktMemo, true);
				break;
			case 0x1D :			// Tremor
				doTremorEffekt(aktMemo);
				break;
			case 0x22:			// Panbrello
				doPanbrelloEffekt(aktMemo);
				break;
			case 0x24:			// Smooth Midi Macro
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
	 * @param newVolume
	 * @see de.quippy.javamod.multimedia.mod.mixer.BasicModMixer#doVolumeColumnEffekt(de.quippy.javamod.multimedia.mod.mixer.BasicModMixer.ChannelMemory, int)
	 */
	@Override
	protected void doVolumeColumnRowEffekt(final ChannelMemory aktMemo)
	{
		// FT2 has a funny quirk with volume column effects: if the result of certain effects
		// is NOT zero, no multi note re-trigger on Tick0!
		aktMemo.FT2AllowRetriggerQuirk = aktMemo.assignedVolumeEffekt==0 && aktMemo.assignedVolumeEffektOp==0;

		if (aktMemo.assignedVolumeEffekt==0) return;

		switch (aktMemo.assignedVolumeEffekt)
		{
			case 0x01: // Set Volume
				aktMemo.currentVolume = aktMemo.assignedVolumeEffektOp;
				aktMemo.FT2AllowRetriggerQuirk = aktMemo.currentVolume==0;
				if (aktMemo.currentVolume>ModConstants.MAX_SAMPLE_VOL) aktMemo.currentVolume = ModConstants.MAX_SAMPLE_VOL;
				else
				if (aktMemo.currentVolume<ModConstants.MIN_SAMPLE_VOL) aktMemo.currentVolume = ModConstants.MIN_SAMPLE_VOL;
				aktMemo.currentInstrumentVolume = aktMemo.currentVolume;
				aktMemo.doFastVolRamp = true;
				break;
			case 0x02: // Volslide down
			case 0x03: // Volslide up
				break;
			case 0x04: // Fine VolSlide down
				doVolumeSlideEffekt(aktMemo, aktMemo.assignedVolumeEffektOp);
				aktMemo.FT2AllowRetriggerQuirk = aktMemo.currentVolume==0;
				break;
			case 0x05: // Fine VolSlide up
				doVolumeSlideEffekt(aktMemo, aktMemo.assignedVolumeEffektOp<<4);
				aktMemo.FT2AllowRetriggerQuirk = aktMemo.currentVolume==0;
				break;
			case 0x06: // vibrato speed - does not activate
				if (aktMemo.assignedVolumeEffektOp!=0) aktMemo.vibratoStep = aktMemo.assignedVolumeEffektOp;
				aktMemo.FT2AllowRetriggerQuirk = aktMemo.vibratoStep==0;
				break;
			case 0x07: // vibrato
				if (aktMemo.assignedVolumeEffektOp!=0) aktMemo.vibratoAmplitude = aktMemo.assignedVolumeEffektOp;
				aktMemo.vibratoVolOn = true;
				doVibratoEffekt(aktMemo);
				break;
			case 0x08: // Set Panning
				aktMemo.FT2AllowRetriggerQuirk = aktMemo.assignedVolumeEffektOp==0;
				// I was not able find out why, but a note delay with a note cut will ignore the panning in volume column.
				// However, a volume set is not ignored. The volume and panning are explicitly set in the noteDelay function
				// but somehow the panning gets reset.
				if (isXM && aktMemo.currentElement!=null && aktMemo.currentElement.getNoteIndex()==ModConstants.KEY_OFF && isNoteDelayEffekt(aktMemo.currentAssignedEffekt, aktMemo.currentAssignedEffektParam))
					break;
				doPanning(aktMemo, aktMemo.assignedVolumeEffektOp, ModConstants.PanBits.Pan4Bit);
				break;
			case 0x09: // Panning Slide Left
			case 0x0A: // Panning Slide Right
				break;
			case 0x0B: // Tone Porta
				// With XMs the porta2note effect is not changed, if a note delay is set.
				// However, no special treatment needed, as note delays are handled as in FT2 - so automatically working
				if (aktMemo.assignedVolumeEffektOp!=0)
					aktMemo.portaNoteStep = aktMemo.assignedVolumeEffektOp<<(ModConstants.PERIOD_SHIFT + 4);
				preparePortaToNoteEffect(aktMemo);
				break;
//			case 0x0C: // Porta Down
//				if (aktMemo.volumeEffektOp!=0) aktMemo.portaStepDown = aktMemo.volumeEffektOp<<2;
//				break;
//			case 0x0D: // Porta Up
//				if (aktMemo.volumeEffektOp!=0) aktMemo.portaStepUp = aktMemo.volumeEffektOp<<2;
//				break;
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
			case 0x01: // Set Volume
				break;
			case 0x02: // Volslide down
				doVolumeSlideEffekt(aktMemo, aktMemo.assignedVolumeEffektOp);
				break;
			case 0x03: // Volslide up
				doVolumeSlideEffekt(aktMemo, aktMemo.assignedVolumeEffektOp<<4);
				break;
			// Fine* effects to repeat on patternDelay when ticks reach tempo
			case 0x04: // Fine VolSlide down
			case 0x05: // Fine VolSlide up
				if (patternDelayCount>0 && currentTick==currentTempo) doVolumeColumnRowEffekt(aktMemo);
				break;
			case 0x06: // vibrato speed - but does not enable vibrato
				break;
			case 0x07: // vibrato
				doVibratoEffekt(aktMemo);
				break;
			case 0x08: // Set Panning
				break;
			case 0x09: // Panning Slide Left
				aktMemo.doSurround = false;
				// XM has a funny bug, as they are using unsigned byte (uint8) for the subtracting operation
				// The calculation of "uint16_t tmp16 = ch->outPan + (uint8_t)(0 - (ch->volColumnVol & 0x0F));"
				// will always be below 0x100 for a volColmnVol of 0 (zero) but the overflow check checks for
				// values below 0x100 to set a panning of 0.
				// so let us simulate that by setting full left panning in that case.
				if (isXM && aktMemo.assignedVolumeEffektOp==0)
					aktMemo.panning = 0;
				else
				{
					aktMemo.panning -= aktMemo.assignedVolumeEffektOp;
					if (aktMemo.panning<0) aktMemo.panning = 0;
				}
				break;
			case 0x0A: // Panning Slide Right
				aktMemo.doSurround = false;
				aktMemo.panning += aktMemo.assignedVolumeEffektOp;
				if (aktMemo.panning>256) aktMemo.panning = 256;
				break;
			case 0x0B: // Tone Porta
				doPortaToNoteEffect(aktMemo);
				break;
//			case 0x0C: // Porta Down
//				doPortaDown(aktMemo);
//				break;
//			case 0x0D: // Porta Up
//				doPortaUp(aktMemo);
//				break;
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
		return (effekt==0xE && (effektParam>>4)==0xD && (effektParam&0xF)!=0);
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
		return effekt==0x21 && (effektParam>>4)==0x06;
	}
	/**
	 * @param aktMemo
	 * @return true, if the Effekt and EffektOp indicate a PortaToNoteEffekt AND there was a Note set
	 * @see de.quippy.javamod.multimedia.mod.mixer.BasicModMixer#isPortaToNoteEffekt(de.quippy.javamod.multimedia.mod.mixer.BasicModMixer.ChannelMemory)
	 */
	@Override
	protected boolean isPortaToNoteEffekt(final int effekt, final int effektParam, final int volEffekt, final int volEffektParam, final int notePeriod)
	{
		return ((effekt==0x03 || effekt==0x05) || volEffekt==0x0B) && notePeriod!=0;
	}
	/**
	 * @param aktMemo
	 * @return true, if the effekt indicates a SampleOffsetEffekt
	 * @see de.quippy.javamod.multimedia.mod.mixer.BasicModMixer#isSampleOffsetEffekt(de.quippy.javamod.multimedia.mod.mixer.BasicModMixer.ChannelMemory)
	 */
	@Override
	protected boolean isSampleOffsetEffekt(final int effekt)
	{
		return effekt==0x09;
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
		return effekt==0x14;
	}
	/**
	 * @param aktMemo
	 * @return
	 * @see de.quippy.javamod.multimedia.mod.mixer.BasicModMixer#isNNAEffekt(de.quippy.javamod.multimedia.mod.mixer.BasicModMixer.ChannelMemory)
	 */
	@Override
	protected boolean isNNAEffekt(final int effekt, final int effektParam)
	{
		return false;
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
		return effektParam;
	}
	/**
	 * @param aktMemo
	 * @see de.quippy.javamod.multimedia.mod.mixer.BasicModMixer#processTickEffekts(de.quippy.javamod.multimedia.mod.mixer.BasicModMixer.ChannelMemory)
	 */
	@Override
	protected void processTickEffekts(final ChannelMemory aktMemo)
	{
		doVolumeColumnTickEffekt(aktMemo);
		doTickEffekts(aktMemo);
	}
	/**
	 * @param aktMemo
	 * @see de.quippy.javamod.multimedia.mod.mixer.BasicModMixer#processEffekts(de.quippy.javamod.multimedia.mod.mixer.BasicModMixer.ChannelMemory)
	 */
	@Override
	protected void processEffekts(final ChannelMemory aktMemo)
	{
		doVolumeColumnRowEffekt(aktMemo);
		// in getNewNote, volume column porta has precedence for effekt porta. The latter is not performed
		// plus, the "triggerNote" is not performed, but there, Sample Offset is done
		if (isXM && aktMemo.assignedVolumeEffekt==0x0B && ((aktMemo.assignedEffekt==0x03 || aktMemo.assignedEffekt==0x05) || aktMemo.assignedEffekt==0x09))
			return;
		doRowEffects(aktMemo);
	}
}
