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
	/**
	 * Constructor for ScreamTrackerMixer
	 * @param mod
	 * @param sampleRate
	 * @param doISP
	 */
	public ScreamTrackerMixer(final Module mod, final int sampleRate, final int doISP, final int doNoLoops, final int maxNNAChannels)
	{
		super(mod, sampleRate, doISP, doNoLoops, maxNNAChannels);
	}
	protected void setPeriodBorders(final ChannelMemory aktMemo)
	{
		if ((mod.getSongFlags()&ModConstants.SONG_AMIGALIMITS)!=0) // IT/S3M Amiga Limit flag
		{
			if (aktMemo.currentFinetuneFrequency==0) aktMemo.currentFinetuneFrequency = 8363; // avoid DIV_0 error with unset sample
			aktMemo.portaStepUpEnd = getFineTunePeriod(aktMemo, ModConstants.getNoteIndexForPeriod(113)+1);
			aktMemo.portaStepDownEnd = getFineTunePeriod(aktMemo, ModConstants.getNoteIndexForPeriod(856)+1);
		}
		else 
		if (isS3M)
		{
			if (isModPlug) // s3m safed by ModPlug - open end...
			{
				aktMemo.portaStepUpEnd = 0;
				aktMemo.portaStepDownEnd = 0x00FFFFFF;
			}
			else // s3m default with no amigalimits set
			{
				aktMemo.portaStepUpEnd = 0x100;
				aktMemo.portaStepDownEnd = 0xFFFF;
			}
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
	protected void initializeMixer(int channel, ChannelMemory aktMemo)
	{
		if (isIT)
		{
			aktMemo.muteWasITforced = aktMemo.muted = ((mod.getPanningValue(channel) & 0x200)!=0); // 0x80<<2
			aktMemo.doSurround = (mod.getPanningValue(channel) == 400); // 100<<2 - no HEX, really!!!
		}
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
				return (int)((long)ModConstants.BASEFREQUENCY * ((long)s3mNote<<7) / ((long)aktMemo.currentFinetuneFrequency<<s3mOctave));
			
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
	protected void setNewPlayerTuningFor(ChannelMemory aktMemo, int newPeriod)
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
				final long itTuning = (((((long)ModConstants.BASEPERIOD)<<ModConstants.PERIOD_SHIFT) * (long)aktMemo.currentFinetuneFrequency)<<ModConstants.SHIFT) / (long)sampleRate;
				aktMemo.currentTuning = (int)(itTuning / (long)newPeriod); 
				return;
			case ModConstants.STM_S3M_TABLE:
			case ModConstants.IT_AMIGA_TABLE:
				final int clampedPeriod = (newPeriod>aktMemo.portaStepDownEnd)?aktMemo.portaStepDownEnd:(newPeriod<aktMemo.portaStepUpEnd)?aktMemo.portaStepUpEnd:newPeriod;
				aktMemo.currentTuning = globalTuning / clampedPeriod;
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

		if (aktMemo.assignedEffekt==0 && aktMemo.assignedEffektParam==0) return;
		
		final Instrument ins = aktMemo.assignedInstrument;
		final PatternElement element = aktMemo.currentElement;
		
		switch (aktMemo.assignedEffekt)
		{
			case 0x00:			// no effect, only effect OP is set
				break;
			case 0x01:			// SET SPEED
				currentTick = currentTempo = aktMemo.assignedEffektParam;
				break;
			case 0x02:			// Pattern position jump
				patternBreakJumpPatternIndex = calculateExtendedValue(aktMemo, null);
				//patternBreakJumpPatternIndex = aktMemo.effektParam;
				break;
			case 0x03:			// Pattern break
				if (!(isS3M && aktMemo.assignedEffektParam>64)) // ST3 ignores illegal pattern breaks
				{
					patternBreakRowIndex = calculateExtendedValue(aktMemo, null);
					//patternBreakRowIndex = aktMemo.effektParam&0xFF;
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
					if ((mod.getSongFlags()&ModConstants.SONG_ITCOMPATMODE)==0) aktMemo.IT_EFG = aktMemo.portaStepDown; 
				}

				final int indicatorPortaDown = aktMemo.portaStepDown&0xF0;
				if (indicatorPortaDown==0xE0 || indicatorPortaDown==0xF0)
				{
					if (indicatorPortaDown==0xE0) doExtraFineSlide(aktMemo, aktMemo.portaStepDown&0xF);
					else
					if (indicatorPortaDown==0xF0) doFineSlide(aktMemo, aktMemo.portaStepDown&0xF);
					
					setNewPlayerTuningFor(aktMemo);
				}
				break;
			case 0x06:			// Porta Up
				if (aktMemo.assignedEffektParam!=0)
				{
					aktMemo.portaStepUp=aktMemo.assignedEffektParam;
					if ((mod.getSongFlags() & ModConstants.SONG_ITCOMPATMODE)==0) aktMemo.IT_EFG = aktMemo.portaStepUp;
				}

				final int indicatorPortaUp = aktMemo.portaStepUp&0xF0;
				if (indicatorPortaUp==0xE0 || indicatorPortaUp==0xF0)
				{
					if (indicatorPortaUp==0xE0) doExtraFineSlide(aktMemo, -(aktMemo.portaStepUp&0xF));
					else
					if (indicatorPortaUp==0xF0) doFineSlide(aktMemo, -(aktMemo.portaStepUp&0xF));
					
					setNewPlayerTuningFor(aktMemo);
				}
				break;
			case 0x07: 			// Porta To Note
				if (hasNewNote(element)) aktMemo.portaTargetNotePeriod = getFineTunePeriod(aktMemo);
				if (aktMemo.assignedEffektParam!=0)
				{
					aktMemo.portaNoteStep = aktMemo.assignedEffektParam;
					if ((mod.getSongFlags() & ModConstants.SONG_ITCOMPATMODE)==0) aktMemo.IT_EFG = aktMemo.portaNoteStep;
				}
				break;
			case 0x08:			// Vibrato
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
				if (aktMemo.assignedEffektParam != 0) aktMemo.arpegioParam = aktMemo.assignedEffektParam;
				if (aktMemo.assignedNotePeriod!=0)
				{
					if (!isIT) // s3m, stm ?!
					{
						final int currentIndex = aktMemo.assignedNoteIndex - 1; // Index into noteValues Table
						aktMemo.arpegioNote[0] = getFineTunePeriod(aktMemo);
						aktMemo.arpegioNote[1] = getFineTunePeriod(aktMemo, currentIndex+(aktMemo.arpegioParam >>4));
						aktMemo.arpegioNote[2] = getFineTunePeriod(aktMemo, currentIndex+(aktMemo.arpegioParam&0xF));
					}
					else
						aktMemo.arpegioNote[0] = aktMemo.currentNotePeriod;

					aktMemo.arpegioIndex=0;
				}
				break;
			case 0x0B:			// Vibrato + Volume Slide
				aktMemo.vibratoOn = true;
				doVibratoEffekt(aktMemo, false);
				if (aktMemo.assignedEffektParam!=0) aktMemo.volumSlideValue = aktMemo.assignedEffektParam;
				// Fine Volume Up/Down and FastSlides
				if (isFineSlide(aktMemo.volumSlideValue) || (mod.getSongFlags()&ModConstants.SONG_FASTVOLSLIDES)!=0)
					doVolumeSlideEffekt(aktMemo);
				break;
			case 0x0C:			// Porta To Note + VolumeSlide
				if (hasNewNote(element)) aktMemo.portaTargetNotePeriod = getFineTunePeriod(aktMemo);
				if (aktMemo.assignedEffektParam!=0) aktMemo.volumSlideValue = aktMemo.assignedEffektParam;
				// Fine Volume Up/Down and FastSlides
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
//						aktMemo.highSampleOffset = 0; // set zero after usage?!
					}
					else
						aktMemo.sampleOffset = newSampleOffset;
				}
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
				/*if (isIT)*/ doRetrigNote(aktMemo, false);
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
						aktMemo.currentFineTune = ModConstants.it_fineTuneTable[effektOpEx];
						aktMemo.currentFinetuneFrequency = ModConstants.it_fineTuneTable[effektOpEx];
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
						/*if (patternDelayCount<0)*/ patternTicksDelayCount += effektOpEx;
						break;
					case 0x7:	// set NNA and others
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
									Envelope volEnv = ins.volumeEnvelope;
									if (volEnv !=null) aktMemo.tempVolEnv = 0;
								}
								break;
							case 0x8: // Volume Envelope On
								if (ins!=null)
								{
									Envelope volEnv = ins.volumeEnvelope;
									if (volEnv !=null) aktMemo.tempVolEnv = 1;
								}
								break;
							case 0x9: // Panning Envelope off
								if (ins!=null)
								{
									Envelope panEnv = ins.panningEnvelope;
									if (panEnv !=null) aktMemo.tempPanEnv = 0;
								}
								break;
							case 0xA: // Panning Envelope On
								if (ins!=null)
								{
									Envelope panEnv = ins.panningEnvelope;
									if (panEnv !=null) aktMemo.tempPanEnv = 1;
								}
								break;
							case 0xB: // Pitch Envelope off
								if (ins!=null)
								{
									Envelope pitEnv = ins.pitchEnvelope;
									if (pitEnv !=null) aktMemo.tempPitchEnv = 0;
								}
								break;
							case 0xC: // Pitch Envelope On
								if (ins!=null)
								{
									Envelope pitEnv = ins.pitchEnvelope;
									if (pitEnv !=null) aktMemo.tempPitchEnv = 1;
								}
								break;
						}
						break;
					case 0x8:	// Fine Panning
						doPanning(aktMemo, effektOpEx, ModConstants.PanBits.Pan4Bit);
						break;
					case 0x9:	// Sound Control
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
					case 0xA:	// set High Offset
						aktMemo.highSampleOffset = effektOpEx;
						break;
					case 0xB:	// JumpLoop
						if (effektOpEx==0) // Set a marker for loop
						{
							aktMemo.jumpLoopPatternRow = currentRow;
							aktMemo.jumpLoopPositionSet = true;
						}
						else
						{
							if (aktMemo.jumpLoopRepeatCount==-1) // was not set!
							{
								aktMemo.jumpLoopRepeatCount=effektOpEx;
								if (!aktMemo.jumpLoopPositionSet) // if not set, pattern Start is default!
								{
									aktMemo.jumpLoopPatternRow = aktMemo.lastJumpCounterRow + 1;
									aktMemo.jumpLoopPositionSet = true;
								}
							}
							aktMemo.lastJumpCounterRow = currentRow;
							
							if (aktMemo.jumpLoopRepeatCount>0 && aktMemo.jumpLoopPositionSet)
							{
								aktMemo.jumpLoopRepeatCount--;
								patternJumpPatternIndex = aktMemo.jumpLoopPatternRow;
							}
							else
							{
								aktMemo.jumpLoopPositionSet = false;
								aktMemo.jumpLoopRepeatCount = -1;
							}
						}
						break;
					case 0xC:	// Note Cut
						if (aktMemo.noteCutCount<0)
						{
							if (isIT)
							{
								if (effektOpEx==0) aktMemo.noteCutCount=1;
								else aktMemo.noteCutCount = effektOpEx;
							}
							else
							if (isS3M)
							{
								if (effektOpEx==0) aktMemo.noteCutCount=-1;
								else aktMemo.noteCutCount = effektOpEx;
							}
							else
								aktMemo.noteCutCount = effektOpEx;
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
						/*if (patternDelayCount<0)*/ patternDelayCount=effektOpEx;
						break;
					case 0xF:	// Set Active Macro (s3m: Funk Repeat)
						if (isIT) aktMemo.activeMidiMacro = aktMemo.assignedEffektParam&0x7F;
						break;
					default :
						//Log.debug(String.format("Unknown Extended Effect: Effect:%02X Op:%02X in [Pattern:%03d: Row:%03d Channel:%03d]", Integer.valueOf(aktMemo.effekt), Integer.valueOf(aktMemo.effektParam), Integer.valueOf(currentPatternIndex), Integer.valueOf(currentRow), Integer.valueOf(aktMemo.channelNumber+1)));
						break;
				}
				break;
			case 0x14:			// set Tempo
				int newTempo = calculateExtendedValue(aktMemo, null); 
				if ((isIT || isS3M) && newTempo!=0) aktMemo.oldTempoParameter = newTempo&0xFF; else newTempo = aktMemo.oldTempoParameter;

				if (newTempo>0x20)
					currentBPM = newTempo;
				else // Tempo Slide up
				if ((newTempo&0xF0)==0x10)	// 0x1X
					currentBPM += newTempo&0xF;
				else // Tempo Slide down
//				if ((newTempo&0xF0)==0x00)	// 0x0X
					currentBPM -= newTempo&0xF;

				if (currentBPM<0) currentBPM = 0;
				else 
				if (isModPlug)
				{
					if (currentBPM>0x200) currentBPM = 0x200; // 512 for MPT ITex
				}
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
				aktMemo.currentNotePeriod = (int)((oldPeriod * ((long)ModConstants.LinearSlideUpTable[slideIndex]))>>ModConstants.HALFTONE_SHIFT);
				if (oldPeriod == aktMemo.currentNotePeriod) aktMemo.currentNotePeriod--;
			}
			else
			{
				aktMemo.currentNotePeriod = (int)((oldPeriod * ((long)ModConstants.LinearSlideDownTable[slideIndex]))>>ModConstants.HALFTONE_SHIFT);
				if (oldPeriod == aktMemo.currentNotePeriod) aktMemo.currentNotePeriod++;
			}
		}
		else
			aktMemo.currentNotePeriod += slide<<ModConstants.PERIOD_SHIFT;
	}
	/**
	 * @since 28.03.2024
	 * @param aktMemo
	 * @param slide
	 */
	private void doExtraFineSlide(final ChannelMemory aktMemo, final int slide)
	{
		if (frequencyTableType==ModConstants.IT_LINEAR_TABLE)
		{
			int slideIndex = (slide<0)?-slide:slide;
			if (slideIndex>255) slideIndex = 255;
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
			int slideIndex = (slide<0)?-slide:slide;
			if (slideIndex>255) slideIndex = 255;
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
	 * Convenient Method for the Porta Up Effekt
	 * @since 08.06.2020
	 * @param aktMemo
	 */
	private void doPortaUp(final ChannelMemory aktMemo, final boolean inVolColum)
	{
		final int indicatorPortaUp = aktMemo.portaStepUp&0xF0;
		if (inVolColum || (indicatorPortaUp!=0xE0 && indicatorPortaUp!=0xF0)) // NO Extra Fine Porta Up
		{
			doFreqSlide(aktMemo, -aktMemo.portaStepUp);
			if (aktMemo.glissando)
				setNewPlayerTuningFor(aktMemo, getRoundedPeriod(aktMemo, aktMemo.currentNotePeriod>>ModConstants.PERIOD_SHIFT)<<ModConstants.PERIOD_SHIFT);
			else
				setNewPlayerTuningFor(aktMemo);
		}
	}
	/**
	 * Convenient Method for the Porta Down Effekt
	 * @since 08.06.2020
	 * @param aktMemo
	 */
	private void doPortaDown(final ChannelMemory aktMemo, final boolean inVolColum)
	{
		final int indicatorPortaDown = aktMemo.portaStepDown&0xF0;
		if (inVolColum || (indicatorPortaDown!=0xE0 && indicatorPortaDown!=0xF0)) // NO Extra Fine Porta Up
		{
			doFreqSlide(aktMemo, aktMemo.portaStepDown);
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
		// Schism / OpenMPT implementation adopted
		if (currentSample.vibratoRate==0) return;
		
		int depth = aktMemo.autoVibratoAmplitude;
		depth += currentSample.vibratoSweep;
		if (depth>(currentSample.vibratoDepth<<7)) depth = currentSample.vibratoDepth<<7; 
		aktMemo.autoVibratoAmplitude = depth;
		depth >>= 6;
		
		final int position = aktMemo.autoVibratoTablePos & 0xFF;
		aktMemo.autoVibratoTablePos += currentSample.vibratoRate;
		int periodAdd;
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
		periodAdd = (periodAdd * depth) >> 7; // periodAdd 0..128

		int slideIndex = (periodAdd<0)?-periodAdd:periodAdd;
		if (slideIndex>(255<<2)) slideIndex = (255<<2);
		final long period = currentPeriod;
		
		// Formula: ((period*table[index / 4])-period) + ((period*fineTable[index % 4])-period)
		if (periodAdd<0)
		{
			periodAdd = (int)(((period * ((long)ModConstants.LinearSlideUpTable[slideIndex>>2]))>>ModConstants.HALFTONE_SHIFT) - period);
			if ((slideIndex&0x03)!=0) periodAdd += (int)(((period * ((long)ModConstants.FineLinearSlideUpTable[slideIndex&0x3]))>>ModConstants.HALFTONE_SHIFT) - period);
		}
		else
		{
			periodAdd = (int)(((period * ((long)ModConstants.LinearSlideDownTable[slideIndex>>2]))>>ModConstants.HALFTONE_SHIFT) - period);
			if ((slideIndex&0x03)!=0) periodAdd += (int)(((period * ((long)ModConstants.FineLinearSlideDownTable[slideIndex&0x3]))>>ModConstants.HALFTONE_SHIFT) - period);
		}

		setNewPlayerTuningFor(aktMemo, currentPeriod - periodAdd);
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
		
		int attenuation = 6; 
		if (oldITEffects) // With old effects two times deeper and reversed
		{
			attenuation--;
			periodAdd = -periodAdd;
		}
		// with s3m vibrato types are equal in effect memory - fine slide is done here...
		if (doFineVibrato && isS3M) periodAdd >>= 2;

		periodAdd = (periodAdd * aktMemo.vibratoAmplitude) >> attenuation; // more or less the same as "/(1<<attenuaton)" :)
		
		if (frequencyTableType==ModConstants.IT_LINEAR_TABLE)
		{
			int slideIndex = (periodAdd<0)?-periodAdd:periodAdd;
			if (slideIndex>(255<<2)) slideIndex = (255<<2);
			final long period = aktMemo.currentNotePeriod;
			
			// Formula: ((period*table[index / 4])-period) + ((period*fineTable[index % 4])-period)
			if (periodAdd<0)
			{
				periodAdd = (int)(((period * ((long)ModConstants.LinearSlideUpTable[slideIndex>>2]))>>ModConstants.HALFTONE_SHIFT) - period);
				if ((slideIndex&0x03)!=0) periodAdd += (int)(((period * ((long)ModConstants.FineLinearSlideUpTable[slideIndex&0x3]))>>ModConstants.HALFTONE_SHIFT) - period);
			}
			else
			{
				periodAdd = (int)(((period * ((long)ModConstants.LinearSlideDownTable[slideIndex>>2]))>>ModConstants.HALFTONE_SHIFT) - period);
				if ((slideIndex&0x03)!=0) periodAdd += (int)(((period * ((long)ModConstants.FineLinearSlideDownTable[slideIndex&0x3]))>>ModConstants.HALFTONE_SHIFT) - period);
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
	 * @since 03.07.2020
	 * @param aktMemo
	 * @param element
	 */
	protected void doSampleOffsetEffekt(final ChannelMemory aktMemo, final PatternElement element)
	{
		if (hasNoNote(element) || aktMemo.currentSample==null || aktMemo.sampleOffset==-1) return;
		
		final Sample sample = aktMemo.currentSample;
		final boolean hasLoop = (sample.loopType & ModConstants.LOOP_ON)!=0;
		final int length = hasLoop?sample.loopStop:sample.length;
		
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
		
		aktMemo.currentSamplePos = aktMemo.sampleOffset; 

		if (aktMemo.currentSamplePos >= length)
		{
			if (isS3M)
			{
				// ST3 Compatibility: Don't play note if offset is beyond sample length (non-looped samples only)
				// else do offset wrap-around - does this in GUS mode, not in SoundBlaster mode	
				if (!hasLoop)
					setNewPlayerTuningFor(aktMemo, aktMemo.currentNotePeriod = 0);
				else
					aktMemo.currentSamplePos = ((aktMemo.currentSamplePos - sample.loopStart) % sample.loopLength) + sample.loopStart;
			}
			else
			if (isIT)
			{
				if ((mod.getSongFlags() & ModConstants.SONG_ITOLDEFFECTS)!=0) // Old Effects
					aktMemo.currentSamplePos = sample.length-1;
				else
					aktMemo.currentSamplePos = 0; // reset to start
			}
			else
			{
				if (hasLoop)
					aktMemo.currentSamplePos = sample.loopStart;
				else
					aktMemo.currentSamplePos = sample.length-1;
			}
		}
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
		aktMemo.arpegioIndex = (aktMemo.arpegioIndex+1)%3;
		int nextNotePeriod = 0;
		if (isIT)
		{
			if (aktMemo.arpegioIndex==0)
				nextNotePeriod = aktMemo.currentNotePeriod;
			else
			{
				final long factor = (long)ModConstants.halfToneTab[(aktMemo.arpegioIndex==1)?(aktMemo.arpegioParam>>4):(aktMemo.arpegioParam&0xF)];
				nextNotePeriod = (int)((((long)aktMemo.currentNotePeriod) * factor)>>ModConstants.HALFTONE_SHIFT);
			}
		}
		else
		{
			nextNotePeriod = aktMemo.arpegioNote[aktMemo.arpegioIndex];
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
	 * Retriggers the note and does volume slide
	 * @since 04.04.2020
	 * @param aktMemo
	 */
	protected void doRetrigNote(final ChannelMemory aktMemo, final boolean inTick)
	{
		if (!aktMemo.instrumentFinished)
		{
			// With a new note, reset the counter, otherwise decrement. (only, if first Tick!)
			if (hasNewNote(aktMemo.currentElement) && !inTick)
				aktMemo.retrigCount = aktMemo.retrigMemo;
			else
				aktMemo.retrigCount--;

			if (aktMemo.retrigCount<=0)
			{
				aktMemo.retrigCount = aktMemo.retrigMemo;
				resetInstrumentPointers(aktMemo);
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
	}
	/**
	 * @param aktMemo
	 * @see de.quippy.javamod.multimedia.mod.mixer.BasicModMixer#doTickEffekts(de.quippy.javamod.multimedia.mod.mixer.BasicModMixer.ChannelMemory)
	 */
	@Override
	protected void doTickEffekts(final ChannelMemory aktMemo)
	{
		if (aktMemo.assignedEffekt==0 && aktMemo.assignedEffektParam==0) return;
		
		switch (aktMemo.assignedEffekt)
		{
			case 0x04 : 		// VolumeSlide, BUT Fine Slide only on first Tick
				if (patternDelayCount>0 && currentTick==currentTempo) doRowEffects(aktMemo);
				else
				{
					if (!isFineSlide(aktMemo.volumSlideValue))
						doVolumeSlideEffekt(aktMemo);
				}
				break;
			case 0x05: 			// Porta Down
				if (patternDelayCount>0 && currentTick==currentTempo) doRowEffects(aktMemo);
				else
				{
					doPortaDown(aktMemo, false);
				}
				break;
			case 0x06: 			// Porta Up
				if (patternDelayCount>0 && currentTick==currentTempo) doRowEffects(aktMemo);
				else
				{
					doPortaUp(aktMemo, false);
				}
				break;
			case 0x07 :			// Porta to Note
				doPortaToNoteEffekt(aktMemo);
				break;
			case 0x08 :			// Vibrato
				doVibratoEffekt(aktMemo, false);
				break;
			case 0x09 :			// Tremor
				doTremorEffekt(aktMemo);
				break;
			case 0x0A :			// Arpeggio
				doArpeggioEffekt(aktMemo);
				break;
			case 0x0B:			// Vibrato + VolumeSlide
				if (patternDelayCount>0 && currentTick==currentTempo) doRowEffects(aktMemo);
				else
				{
					doVibratoEffekt(aktMemo, false);
					if (!isFineSlide(aktMemo.volumSlideValue))
						doVolumeSlideEffekt(aktMemo);
				}
				break;
			case 0x0C :			// Porta to Note + VolumeSlide
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
				switch (aktMemo.assignedEffektParam>>4)
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
								aktMemo.currentVolume = 0;
								aktMemo.doFastVolRamp = true;
							}
						}
						break;
					case 0xD:	// Note Delay
						// we do this globally!
						break;
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
					aktMemo.portaNoteStep = ModConstants.IT_VolColumnPortaNotSpeedTranslation[index];
					if ((mod.getSongFlags() & ModConstants.SONG_ITCOMPATMODE)==0) aktMemo.IT_EFG = aktMemo.portaNoteStep;
				}
				break;
			case 0x0C: // Porta Down
				if (aktMemo.assignedVolumeEffektOp!=0)
				{
					aktMemo.portaStepDown = aktMemo.assignedVolumeEffektOp<<2;
					if ((mod.getSongFlags() & ModConstants.SONG_ITCOMPATMODE)==0) aktMemo.IT_EFG = aktMemo.portaStepDown;
				}
				break;
			case 0x0D: // Porta Up
				if (aktMemo.assignedVolumeEffektOp!=0)
				{
					aktMemo.portaStepUp = aktMemo.assignedVolumeEffektOp<<2;
					if ((mod.getSongFlags() & ModConstants.SONG_ITCOMPATMODE)==0) aktMemo.IT_EFG = aktMemo.portaStepUp;
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
				doPortaDown(aktMemo, true);
				break;
			case 0x0D: // Porta Up
				doPortaUp(aktMemo, true);
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
		boolean isEffekt = effekt==0x13 && (effektParam>>4)==0x0D;
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
	protected boolean isPatternFramesDelayEffekt(int effekt, int effektParam)
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
	protected boolean isKeyOffEffekt(int effekt, int effektParam)
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
	protected int getEffektOpMemory(final ChannelMemory aktMemo, final int effekt, int effektParam)
	{
		if (effekt==0x13 && effektParam == 0) return aktMemo.S_Effect_Memory;
		return effektParam;
	}
	/**
	 * @param aktMemo
	 * @see de.quippy.javamod.multimedia.mod.mixer.BasicModMixer#processTickEffekts(de.quippy.javamod.multimedia.mod.mixer.BasicModMixer.ChannelMemory)
	 */
	@Override
	protected void processTickEffekts(final ChannelMemory aktMemo)
	{
		doTickEffekts(aktMemo);
		doVolumeColumnTickEffekt(aktMemo);
	}
	/**
	 * @param aktMemo
	 * @see de.quippy.javamod.multimedia.mod.mixer.BasicModMixer#processEffekts(de.quippy.javamod.multimedia.mod.mixer.BasicModMixer.ChannelMemory)
	 */
	@Override
	protected void processEffekts(final ChannelMemory aktMemo)
	{
		// shared Effect memory EFG is sharing information only on tick 0!
		// we cannot share during effects. Only with IT Compat Mode off!
		// *** IT Compat Off means, old stm, s3m ... ***
		if ((mod.getSongFlags() & ModConstants.SONG_ITCOMPATMODE)==0) 
			aktMemo.portaStepDown = aktMemo.portaStepUp = aktMemo.portaNoteStep = aktMemo.IT_EFG;
		// IT: first Row, then column!
		doRowEffects(aktMemo);
		doVolumeColumnRowEffekt(aktMemo);
	}
}
