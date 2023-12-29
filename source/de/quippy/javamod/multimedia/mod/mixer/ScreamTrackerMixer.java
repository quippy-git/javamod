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

import de.quippy.javamod.multimedia.mod.ModConstants;
import de.quippy.javamod.multimedia.mod.loader.Module;
import de.quippy.javamod.multimedia.mod.loader.instrument.Envelope;
import de.quippy.javamod.multimedia.mod.loader.instrument.Instrument;
import de.quippy.javamod.multimedia.mod.loader.instrument.Sample;
import de.quippy.javamod.multimedia.mod.loader.pattern.PatternElement;
import de.quippy.javamod.multimedia.mod.midi.MidiMacros;
import de.quippy.javamod.system.Helpers;
import de.quippy.javamod.system.Log;

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
			aktMemo.wasITforced = aktMemo.muted = ((mod.getPanningValue(channel) & 0x200)!=0); // 0x80<<2
			aktMemo.doSurround = (mod.getPanningValue(channel) == 400); // 100<<2 - ist in der Tat kein HEX!!!
		}
		if ((mod.getSongFlags()&ModConstants.SONG_AMIGALIMITS)!=0) // S3M Amiga Limit flag
		{
			aktMemo.portaStepUpEnd = getFineTunePeriod(aktMemo, ModConstants.getNoteIndexForPeriod(113)+1);
			aktMemo.portaStepDownEnd = getFineTunePeriod(aktMemo, ModConstants.getNoteIndexForPeriod(856)+1);
		}
		else // For IT no limits... But no wrap around with 32Bit either :)
		{
			aktMemo.portaStepUpEnd = 0; 
			aktMemo.portaStepDownEnd = 0x00FFFFFF; // Short.MAX_VALUE<<ModConstants.PERIOD_SHIFT <- this is signed 0x7ffff then
		}
	}
	/**
	 * Clear all effekts. Sometimes, if Effekts do continue, they are not stopped.
	 * Primary this will eliminate the vibrato (the current elongated pitch)
	 * and reset table positions
	 * @param aktMemo
	 * @param nextElement
	 */
	@Override
	protected void resetAllEffects(final ChannelMemory aktMemo, PatternElement nextElement, boolean forced)
	{
		if (aktMemo.arpegioIndex>=0)
		{
			aktMemo.arpegioIndex=-1;
			int nextNotePeriod = aktMemo.arpegioNote[0];
			if (nextNotePeriod!=0)
			{
				setNewPlayerTuningFor(aktMemo, aktMemo.currentNotePeriod = nextNotePeriod);
			}
		}
		if (aktMemo.vibratoOn) // We have a vibrato for reset
		{
			if (forced || (nextElement.getVolumeEffekt()!=0x07 && nextElement.getEffekt()!=0x08 && nextElement.getEffekt()!=0x0B && nextElement.getEffekt()!=0x15)) // but only, if there is no vibrato following 
			{
				aktMemo.vibratoOn = false;
				if (!aktMemo.vibratoNoRetrig) aktMemo.vibratoTablePos = 0;
				setNewPlayerTuningFor(aktMemo);
			}
		}
		if (aktMemo.tremoloOn) // We have a tremolo for reset
		{
			if (forced || nextElement.getEffekt()!=0x12) //but only, if there is no tremolo following or we are forced
			{
				aktMemo.tremoloOn = false;
				if (!aktMemo.tremoloNoRetrig) aktMemo.tremoloTablePos = 0;
			}
		}
		if (aktMemo.panbrelloOn) // We have a panbrello for reset
		{
			if (forced || nextElement.getEffekt()!=0x19) //but only, if there is no panbrello following or we are forced
			{
				aktMemo.panbrelloOn = false;
				if (!aktMemo.panbrelloNoRetrig) aktMemo.panbrelloTablePos = 0;
			}
		}
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
		}

		if (aktMemo.effekt==0 && aktMemo.effektParam==0) return;
		
		final Instrument ins = aktMemo.assignedInstrument;
		final PatternElement element = aktMemo.currentElement;
		
		switch (aktMemo.effekt)
		{
			case 0x00:			// no effect, only effect OP is set
				break;
			case 0x01:			// SET SPEED
				currentTick = currentTempo = aktMemo.effektParam;
				break;
			case 0x02:			// Pattern position jump
				patternBreakJumpPatternIndex = aktMemo.effektParam;
				break;
			case 0x03:			// Pattern break
				patternBreakRowIndex = aktMemo.effektParam&0xFF;
				break;
			case 0x04:			// Volume Slide
				if (aktMemo.effektParam!=0) aktMemo.volumSlideValue = aktMemo.effektParam;
				// Fine Volume Up/Down and FastSlides
				if (isFineSlide(aktMemo.volumSlideValue) || (mod.getSongFlags()&ModConstants.SONG_FASTVOLSLIDES)!=0)
					doVolumeSlideEffekt(aktMemo);
				break;
			case 0x05:			// Porta Down
				if (aktMemo.effektParam!=0)
				{
					aktMemo.portaStepDown = aktMemo.effektParam;
					if ((mod.getSongFlags()&ModConstants.SONG_ITCOMPATMODE)==0) aktMemo.IT_EFG = aktMemo.portaStepDown; 
				}

				final int indicatorPortaDown = aktMemo.portaStepDown&0xF0;
				if (indicatorPortaDown==0xE0 || indicatorPortaDown==0xF0) // (Extra) Fine Porta Down
				{
					final int effektOp = aktMemo.portaStepDown&0xF;
					if (frequencyTableType==ModConstants.IT_LINEAR_TABLE)
					{
						final long period = aktMemo.currentNotePeriod;//>>ModConstants.PERIOD_SHIFT;
						int delta = (indicatorPortaDown==0xF0)?
							(int)(((period * ((long)ModConstants.LinearSlideDownTable[effektOp]))>>ModConstants.HALFTONE_SHIFT) - period)
							: // Extra Fine
							(int)(((period * ((long)ModConstants.FineLinearSlideDownTable[effektOp]))>>ModConstants.HALFTONE_SHIFT) - period);
						if (delta<1) delta = 1;
						aktMemo.currentNotePeriod += (delta/*<<ModConstants.PERIOD_SHIFT*/);
					}
					else
						aktMemo.currentNotePeriod += (indicatorPortaDown==0xE0)?effektOp<<2:effektOp<<4;
					
					if (aktMemo.currentNotePeriod > aktMemo.portaStepDownEnd) aktMemo.currentNotePeriod = aktMemo.portaStepDownEnd;
					setNewPlayerTuningFor(aktMemo);
				}
				break;
			case 0x06:			// Porta Up
				if (aktMemo.effektParam!=0)
				{
					aktMemo.portaStepUp=aktMemo.effektParam;
					if ((mod.getSongFlags() & ModConstants.SONG_ITCOMPATMODE)==0) aktMemo.IT_EFG = aktMemo.portaStepUp;
				}

				final int indicatorPortaUp = aktMemo.portaStepUp&0xF0;
				if (indicatorPortaUp==0xE0 || indicatorPortaUp==0xF0) // Extra Fine Porta Up
				{
					final int effektOp = aktMemo.portaStepUp&0xF;
					if (frequencyTableType==ModConstants.IT_LINEAR_TABLE)
					{
						final long period = aktMemo.currentNotePeriod;//>>ModConstants.PERIOD_SHIFT;
						int delta = (indicatorPortaUp==0xF0)?
							(int)(((period * ((long)ModConstants.LinearSlideUpTable[effektOp]))>>ModConstants.HALFTONE_SHIFT) - period)
							:
							(int)(((period * ((long)ModConstants.FineLinearSlideUpTable[effektOp]))>>ModConstants.HALFTONE_SHIFT) - period);
						if (delta>-1) delta = -1;
						aktMemo.currentNotePeriod += (delta/*<<ModConstants.PERIOD_SHIFT*/);
					}
					else
						aktMemo.currentNotePeriod -= (indicatorPortaUp==0xE0)?effektOp<<2:effektOp<<4;

					if (aktMemo.currentNotePeriod < aktMemo.portaStepUpEnd) aktMemo.currentNotePeriod = aktMemo.portaStepUpEnd;
					setNewPlayerTuningFor(aktMemo);
				}
				break;
			case 0x07: 		// Porta To Note
				if (element!=null && (element.getPeriod()>0 && element.getNoteIndex()>0)) aktMemo.portaTargetNotePeriod = getFineTunePeriod(aktMemo);
				if (aktMemo.effektParam!=0)
				{
					aktMemo.portaNoteStep = aktMemo.effektParam;
					if ((mod.getSongFlags() & ModConstants.SONG_ITCOMPATMODE)==0) aktMemo.IT_EFG = aktMemo.portaNoteStep;
				}
				break;
			case 0x08:			// Vibrato
				if ((aktMemo.effektParam>>4)!=0) aktMemo.vibratoStep = aktMemo.effektParam>>4;
				if ((aktMemo.effektParam&0xF)!=0) aktMemo.vibratoAmplitude = (aktMemo.effektParam&0xF)<<2;
				aktMemo.vibratoOn = true;
				if (isIT) doVibratoEffekt(aktMemo, false); // IT: on first TICK
				break;
			case 0x09:			// Tremor
				if (aktMemo.effektParam!=0)
				{
					aktMemo.currentInstrumentVolume = aktMemo.currentVolume;
					aktMemo.tremorOntimeSet = (aktMemo.effektParam>>4);
					aktMemo.tremorOfftimeSet = (aktMemo.effektParam&0xF);
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
				if (aktMemo.effektParam != 0) aktMemo.arpegioParam = aktMemo.effektParam;
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
				if (aktMemo.effektParam!=0) aktMemo.volumSlideValue = aktMemo.effektParam;
				// Fine Volume Up/Down and FastSlides
				if (isFineSlide(aktMemo.volumSlideValue) || (mod.getSongFlags()&ModConstants.SONG_FASTVOLSLIDES)!=0)
					doVolumeSlideEffekt(aktMemo);
				break;
			case 0x0C:			// Porta To Note + VolumeSlide
				if (element!=null && (element.getPeriod()>0 && element.getNoteIndex()>0)) aktMemo.portaTargetNotePeriod = getFineTunePeriod(aktMemo);
				if (aktMemo.effektParam!=0) aktMemo.volumSlideValue = aktMemo.effektParam;
				// Fine Volume Up/Down and FastSlides
				if (isFineSlide(aktMemo.volumSlideValue) || (mod.getSongFlags()&ModConstants.SONG_FASTVOLSLIDES)!=0)
					doVolumeSlideEffekt(aktMemo);
				break;
			case 0x0D:			// Set Channel Volume
				aktMemo.channelVolume = aktMemo.effektParam;
				if (aktMemo.channelVolume>ModConstants.MAXSAMPLEVOLUME) aktMemo.channelVolume = ModConstants.MAXSAMPLEVOLUME;
				break;
			case 0x0E:			// Channel Volume Slide
				if (aktMemo.effektParam!=0) aktMemo.channelVolumSlideValue = aktMemo.effektParam;
				// Fine Volume Up/Down and FastSlides
				if (isFineSlide(aktMemo.channelVolumSlideValue) || (mod.getSongFlags()&ModConstants.SONG_FASTVOLSLIDES)!=0)
					doChannelVolumeSlideEffekt(aktMemo);
				break;
			case 0x0F: 		// Sample Offset
				if (aktMemo.effektParam!=0)
				{
					aktMemo.sampleOffset = aktMemo.highSampleOffset<<16 | aktMemo.effektParam<<8;
					aktMemo.highSampleOffset = 0;
				}
				doSampleOffsetEffekt(aktMemo, element);
				break;
			case 0x10:			// Panning Slide
				if (aktMemo.effektParam!=0) aktMemo.panningSlideValue = aktMemo.effektParam;
				if (isFineSlide(aktMemo.panningSlideValue))
					doPanningSlideEffekt(aktMemo);
				break;
			case 0x11:			// Retrig Note
				if ((aktMemo.effektParam&0xF)!=0)
				{
					aktMemo.retrigMemo = aktMemo.effektParam&0xF;
					aktMemo.retrigVolSlide = aktMemo.effektParam>>4;
				}
				/*if (isIT)*/ doRetrigNote(aktMemo, false);
				break;
			case 0x12:			// Tremolo
				if ((aktMemo.effektParam>>4)!=0) aktMemo.tremoloStep = aktMemo.effektParam>>4;
				if ((aktMemo.effektParam&0xF)!=0) aktMemo.tremoloAmplitude = aktMemo.effektParam&0xF;
				aktMemo.tremoloOn = true;
				if (isIT) doTremoloEffekt(aktMemo); // Impulse Tracker: on first Tick
				break;
			case 0x13: 		// Extended
				final int effektParam = (aktMemo.effektParam==0) ? aktMemo.S_Effect_Memory : (aktMemo.S_Effect_Memory=aktMemo.effektParam);
				final int effektOpEx = effektParam&0x0F;
				switch (effektParam>>4)
				{
					case 0x1:	// Glissando
						aktMemo.glissando = effektOpEx!=0;
						break;
					case 0x2:	// Set FineTune
						aktMemo.currentFineTune = ModConstants.it_fineTuneTable[effektOpEx];
						aktMemo.currentFinetuneFrequency = ModConstants.it_fineTuneTable[effektOpEx];
						setNewPlayerTuningFor(aktMemo);
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
						if (patternTicksDelayCount<=0) patternTicksDelayCount = effektOpEx;
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
					case 0x8:	// Set Fine Panning
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
//							case 0xC: // Select global filter mode (IT compatibility). This is the default, when resonant filters are enabled with a Zxx effect, they will stay active until explicitly disabled by setting the cutoff frequency to the maximum (Z7F), and the resonance to the minimum (Z80).
//								globalFilterMode = false;
//								break;
//							case 0xD: // Select local filter mode (MPT beta compatibility): when this mode is selected, the resonant filter will only affect the current note. It will be deactivated when a new note is being played.
//								globalFilterMode = true;
//								break;
//							case 0xE: // Play forward. You may use this to temporarily force the direction of a bidirectional loop to go forward.
//								aktMemo.isForwardDirection = true;
//								break;
//							case 0xF: // Play backward. The current instrument will be played backwards, or it will temporarily set the direction of a loop to go backward.
//								aktMemo.isForwardDirection = false;
//								break;
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
							if (isIT)
							{
								if (effektOpEx==0) aktMemo.noteDelayCount=1;
								else aktMemo.noteDelayCount = effektOpEx;
							}
							else
							if (isS3M)
							{
								if (effektOpEx==0) aktMemo.noteDelayCount=-1;
								else aktMemo.noteDelayCount = effektOpEx;
							}
							else
								aktMemo.noteDelayCount = effektOpEx;
						}
						break;
					case 0xE:	// Pattern Delay - if currently in the patternDelay, do NOT reset the value. We would wait forever!!!
						if (patternDelayCount<0) patternDelayCount=effektOpEx;
						break;
					case 0xF:	// Set Active Macro (s3m: Funk Repeat)
						if (isIT) aktMemo.activeMidiMacro = aktMemo.effektParam&0x7F;
						break;
					default :
						Log.debug(String.format("Unknown Extended Effekt: Effect:%02X Op:%02X in [Pattern:%03d: Row:%03d Channel:%03d]", Integer.valueOf(aktMemo.effekt), Integer.valueOf(aktMemo.effektParam), Integer.valueOf(currentPatternIndex), Integer.valueOf(currentRow), Integer.valueOf(aktMemo.channelNumber+1)));
						break;
				}
				break;
			case 0x14:			// set Tempo
				if (aktMemo.effektParam>>4==0) 			// 0x0X
					currentBPM -= aktMemo.effektParam&0xF;
				else
				if (aktMemo.effektParam>>4==1) 			// 0x1X
					currentBPM += aktMemo.effektParam&0xF;
				else
					currentBPM = aktMemo.effektParam;	// 0x2X
				samplePerTicks = calculateSamplesPerTick();
				break;
			case 0x15:			// Fine Vibrato
				// This effect is identical to the vibrato, but has a 4x smaller amplitude (more precise).
				if ((aktMemo.effektParam>>4)!=0) aktMemo.vibratoStep = aktMemo.effektParam>>4;
				if ((aktMemo.effektParam&0xF)!=0)
				{
					aktMemo.vibratoAmplitude = aktMemo.effektParam&0xF;
					// s3m: do not distinguish in memory, is done in doVibratoEffekt
					if (isS3M) aktMemo.vibratoAmplitude<<=2;
				}
				aktMemo.vibratoOn = true;
				if (isIT) doVibratoEffekt(aktMemo, true); // IT: on first TICK
				break;
			case 0x16:			// Set Global Volume
				if (aktMemo.effektParam<=0x80)
				{
					globalVolume = aktMemo.effektParam;
					// normalize to 0x80 for others except IT
					if (!isIT)
					{
						globalVolume <<= 1; 
						if (globalVolume>ModConstants.MAXGLOBALVOLUME) globalVolume = ModConstants.MAXGLOBALVOLUME;
					}
				}
				break;
			case 0x17:			// Global Volume Slide
				if (aktMemo.effektParam!=0) aktMemo.globalVolumSlideValue = aktMemo.effektParam;
				if (isFineSlide(aktMemo.globalVolumSlideValue))
					doGlobalVolumeSlideEffekt(aktMemo);
				break;
			case 0x18:			// Set Panning
				doPanning(aktMemo, aktMemo.effektParam, ModConstants.PanBits.Pan8Bit);
				break;
			case 0x19:			// Panbrello
				if ((aktMemo.effektParam>>4)!=0) aktMemo.panbrelloStep = aktMemo.effektParam>>4;
				if ((aktMemo.effektParam&0xF)!=0) aktMemo.panbrelloAmplitude = aktMemo.effektParam&0xF;
				aktMemo.panbrelloOn = true;
				break;
			case 0x1A:			// Midi Macro
				final MidiMacros macro = mod.getMidiConfig();
				if (macro!=null)
				{
		            if (aktMemo.effektParam<0x80)
		                processMIDIMacro(aktMemo, false, macro.getMidiSFXExt(aktMemo.activeMidiMacro), aktMemo.effektParam);
		            else
		                processMIDIMacro(aktMemo, false, macro.getMidiZXXExt(aktMemo.effektParam & 0x7F), 0);
				}
	            break;
			case 0x1B:			// Smooth Midi Macro
				final MidiMacros smoothMacro = mod.getMidiConfig();
				if (smoothMacro!=null)
				{
		            if (aktMemo.effektParam<0x80)
		                processMIDIMacro(aktMemo, true, smoothMacro.getMidiSFXExt(aktMemo.activeMidiMacro), aktMemo.effektParam);
		            else
		                processMIDIMacro(aktMemo, true, smoothMacro.getMidiZXXExt(aktMemo.effektParam & 0x7F), 0);
				}
	            break;
			case 0x1C:			// Parameter Extension
				// MPT Specific, unsupported
				break;
			default:
				Log.debug(String.format("Unknown Effekt: Effect:%02X Op:%02X in [Pattern:%03d: Row:%03d Channel:%03d]", Integer.valueOf(aktMemo.effekt), Integer.valueOf(aktMemo.effektParam), Integer.valueOf(currentPatternIndex), Integer.valueOf(currentRow), Integer.valueOf(aktMemo.channelNumber+1)));
				break;
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
				if (frequencyTableType==ModConstants.IT_LINEAR_TABLE)
				{
					int slideIndex = aktMemo.portaNoteStep;
					if (slideIndex>255) slideIndex = 255;
					final long period = aktMemo.currentNotePeriod;//>>ModConstants.PERIOD_SHIFT;
					int delta = (int)(((period * ((long)ModConstants.LinearSlideUpTable[slideIndex]))>>ModConstants.HALFTONE_SHIFT) - period);
					if (delta>-1) delta = -1;
					aktMemo.currentNotePeriod += (delta/*<<ModConstants.PERIOD_SHIFT*/);
				}
				else
					aktMemo.currentNotePeriod -= aktMemo.portaNoteStep<<ModConstants.PERIOD_SHIFT;

				if (aktMemo.currentNotePeriod<=aktMemo.portaTargetNotePeriod)
				{
					aktMemo.currentNotePeriod=aktMemo.portaTargetNotePeriod;
					aktMemo.portaTargetNotePeriod=-1;
				}
			}
			else
			{
				if (frequencyTableType==ModConstants.IT_LINEAR_TABLE)
				{
					int slideIndex = aktMemo.portaNoteStep;
					if (slideIndex>255) slideIndex = 255;
					final long period = aktMemo.currentNotePeriod;//>>ModConstants.PERIOD_SHIFT;
					int delta = (int)(((period * ((long)ModConstants.LinearSlideDownTable[slideIndex]))>>ModConstants.HALFTONE_SHIFT) - period);
					if (delta<1) delta = 1;
					aktMemo.currentNotePeriod += (delta/*<<ModConstants.PERIOD_SHIFT*/);
				}
				else
					aktMemo.currentNotePeriod += aktMemo.portaNoteStep<<ModConstants.PERIOD_SHIFT;

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
			if (frequencyTableType==ModConstants.IT_LINEAR_TABLE)
			{
				int slideIndex = aktMemo.portaStepUp;
				if (slideIndex>255) slideIndex = 255;
				final long period = aktMemo.currentNotePeriod;//>>ModConstants.PERIOD_SHIFT;
				int delta = (int)(((period * ((long)ModConstants.LinearSlideUpTable[slideIndex]))>>ModConstants.HALFTONE_SHIFT) - period);
				if (delta>-1) delta = -1;
				aktMemo.currentNotePeriod += (delta/*<<ModConstants.PERIOD_SHIFT*/);
			}
			else
				aktMemo.currentNotePeriod -= aktMemo.portaStepUp<<ModConstants.PERIOD_SHIFT;

			if (aktMemo.currentNotePeriod < aktMemo.portaStepUpEnd) aktMemo.currentNotePeriod = aktMemo.portaStepUpEnd;
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
			if (frequencyTableType==ModConstants.IT_LINEAR_TABLE)
			{
				int slideIndex = aktMemo.portaStepDown;
				if (slideIndex>255) slideIndex = 255;
				final long period = aktMemo.currentNotePeriod;//>>ModConstants.PERIOD_SHIFT;
				int delta = (int)(((period * ((long)ModConstants.LinearSlideDownTable[slideIndex]))>>ModConstants.HALFTONE_SHIFT) - period);
				if (delta<1) delta = 1;
				aktMemo.currentNotePeriod += (delta/*<<ModConstants.PERIOD_SHIFT*/);
			}
			else
				aktMemo.currentNotePeriod += aktMemo.portaStepDown<<ModConstants.PERIOD_SHIFT;

			if (aktMemo.currentNotePeriod > aktMemo.portaStepDownEnd) aktMemo.currentNotePeriod = aktMemo.portaStepDownEnd;
			if (aktMemo.glissando)
				setNewPlayerTuningFor(aktMemo, getRoundedPeriod(aktMemo, aktMemo.currentNotePeriod>>ModConstants.PERIOD_SHIFT)<<ModConstants.PERIOD_SHIFT);
			else
				setNewPlayerTuningFor(aktMemo);
		}
	}
	/**
	 * @param aktMemo
	 * @param currentSample
	 * @param currentPeriod
	 * @see de.quippy.javamod.multimedia.mod.mixer.BasicModMixer#doAutoVibratoEffekt(de.quippy.javamod.multimedia.mod.mixer.BasicModMixer.ChannelMemory, de.quippy.javamod.multimedia.mod.loader.instrument.Sample)
	 */
	@Override
	protected void doAutoVibratoEffekt(final ChannelMemory aktMemo, final Sample currentSample, int currentPeriod)
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
			case 0:	periodAdd = ModConstants.ITSinusTable[position];	// Sine
					break;
			case 1:	periodAdd = position<128? 0x40:0;					// Square
					break;
			case 2:	periodAdd = ((position + 1)>>1) - 0x40;				// Ramp Up
					break;
			case 3:	periodAdd = 0x40 - ((position + 1)>>1);				// Ramp Down
					break;
			case 4:	periodAdd = (swinger.nextInt() % 0x40);				// Random
					break;
		}
		int slideIndex = (periodAdd * depth) >> 7; // periodAdd 0..128
		final long period = currentPeriod;//>>ModConstants.PERIOD_SHIFT;
		// Formula: ((period*table[index / 4])-period) + ((period*fineTable[index % 4])-period)
		if (slideIndex<0)
		{
			slideIndex = -slideIndex;
			if (slideIndex>255<<2) slideIndex = 255<<2;
			periodAdd = (int)(((period * ((long)ModConstants.LinearSlideUpTable[slideIndex>>2]))>>ModConstants.HALFTONE_SHIFT) - period);
			if ((slideIndex&0x03)!=0) periodAdd += (int)(((period * ((long)ModConstants.FineLinearSlideUpTable[slideIndex&0x3]))>>ModConstants.HALFTONE_SHIFT) - period);
		}
		else
		{
			if (slideIndex>255<<2) slideIndex = 255<<2;
			periodAdd = (int)(((period * ((long)ModConstants.LinearSlideDownTable[slideIndex>>2]))>>ModConstants.HALFTONE_SHIFT) - period);
			if ((slideIndex&0x03)!=0) periodAdd += (int)(((period * ((long)ModConstants.FineLinearSlideDownTable[slideIndex&0x3]))>>ModConstants.HALFTONE_SHIFT) - period);
		}

		setNewPlayerTuningFor(aktMemo, currentPeriod + (periodAdd/*<<ModConstants.PERIOD_SHIFT*/));
		
		// This is the old behavior - possibly suitable for others having linear slides like IT
		// So we will not throw it away... 
//		final int df1, df2;
//		if (periodAdd < 0)
//		{
//			periodAdd = -periodAdd;
//			final int n1 = periodAdd >> 8;
//			df1 = ModConstants.LinearSlideUpTable[n1];
//			df2 = ModConstants.LinearSlideUpTable[n1+1];
//		} 
//		else
//		{
//			final int n1 = periodAdd >> 8;
//			df1 = ModConstants.LinearSlideDownTable[n1];
//			df2 = ModConstants.LinearSlideDownTable[n1+1];
//		}
//		periodAdd >>= 2;
//		int newPeriod = (currentPeriod * (df1 + ((df2-df1)*(periodAdd&0x3F)>>6)))>>16;
//		setNewPlayerTuningFor(aktMemo, newPeriod);
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
				return 0x40 - ((position + 1)>>1);
			case 2: // Squarewave 
				return position<128? 0x40:0;
			case 3: // random	
				return (swinger.nextInt() % 0x40);
		}
	}
	/**
	 * Convenient Method for the vibrato effekt
	 * @param aktMemo
	 */
	protected void doVibratoEffekt(final ChannelMemory aktMemo, final boolean doFineVibrato)
	{
		// Yes: IT pre-incements the table position with vibrato
		if (isIT) aktMemo.vibratoTablePos = aktMemo.vibratoTablePos + (aktMemo.vibratoStep<<2);
		int periodAdd = getVibratoDelta(aktMemo.vibratoType, aktMemo.vibratoTablePos);
		if (!isIT) aktMemo.vibratoTablePos = aktMemo.vibratoTablePos + (aktMemo.vibratoStep<<2);
		
		int attenuation = 5; 
		// With old effects two times deeper (or with new effects half of it)
		if ((mod.getSongFlags()&ModConstants.SONG_ITOLDEFFECTS)==0)
		{
			attenuation++;
			periodAdd = -periodAdd;
		}
		// with s3m vibrato types are equal in effektmemory - fine slide is done here...
		if (doFineVibrato && isS3M) periodAdd >>= 2;

		periodAdd = (periodAdd * aktMemo.vibratoAmplitude) >> attenuation; // more or less the same as "/(1<<attenuaton)" :)
		
		if (frequencyTableType==ModConstants.IT_LINEAR_TABLE)
		{
			int slideIndex = periodAdd;
			final long period = aktMemo.currentNotePeriod;//>>ModConstants.PERIOD_SHIFT;
			// Formula: ((period*table[index / 4])-period) + ((period*fineTable[index % 4])-period)
			if (slideIndex<0)
			{
				slideIndex = -slideIndex;
				if (slideIndex>1020) slideIndex = 1020; // (255<<2)
				periodAdd = (int)(((period * ((long)ModConstants.LinearSlideUpTable[slideIndex>>2]))>>ModConstants.HALFTONE_SHIFT) - period);
				if ((slideIndex&0x03)!=0) periodAdd += (int)(((period * ((long)ModConstants.FineLinearSlideUpTable[slideIndex&0x3]))>>ModConstants.HALFTONE_SHIFT) - period);
			}
			else
			{
				if (slideIndex>1020) slideIndex = 1020; // (255<<2)
				periodAdd = (int)(((period * ((long)ModConstants.LinearSlideDownTable[slideIndex>>2]))>>ModConstants.HALFTONE_SHIFT) - period);
				if ((slideIndex&0x03)!=0) periodAdd += (int)(((period * ((long)ModConstants.FineLinearSlideDownTable[slideIndex&0x3]))>>ModConstants.HALFTONE_SHIFT) - period);
			}
			setNewPlayerTuningFor(aktMemo, aktMemo.currentNotePeriod + (periodAdd/*<<ModConstants.PERIOD_SHIFT*/));
		}
		else
			setNewPlayerTuningFor(aktMemo, aktMemo.currentNotePeriod + (periodAdd<<2));
	}
	/**
	 * @since 03.07.2020
	 * @param aktMemo
	 * @param element
	 */
	protected void doSampleOffsetEffekt(final ChannelMemory aktMemo, final PatternElement element)
	{
		if (element!=null && (element.getPeriod()>0 || element.getNoteIndex()>0) && aktMemo.currentSample!=null) 
		{
			final Sample sample = aktMemo.currentSample;
			if (aktMemo.sampleOffset >= sample.length || 
					((sample.loopType & ModConstants.LOOP_ON)!=0 && aktMemo.sampleOffset >= sample.loopStart) ||
					((sample.loopType & ModConstants.LOOP_SUSTAIN_ON)!=0 && aktMemo.sampleOffset >= sample.sustainLoopStart))
			{
				if ((mod.getSongFlags() & ModConstants.SONG_ITOLDEFFECTS)!=0) // Old Effects
					aktMemo.sampleOffset = aktMemo.currentSample.length-1;
				else
					aktMemo.sampleOffset = 0; // reset to start
			}
			if (aktMemo.sampleOffset!=-1)
			{
				aktMemo.currentSamplePos = aktMemo.sampleOffset; 
				aktMemo.isForwardDirection = true; aktMemo.currentTuningPos = 0;
			}
		}
	}
	/**
	 * Convenient Method for the panbrello effekt 
	 * @param aktMemo
	 */
	protected void doPanbrelloEffekt(final ChannelMemory aktMemo)
	{
		int newPanning = getVibratoDelta(aktMemo.panbrelloType, aktMemo.panbrelloTablePos);
		// IT compatibility: Sample-and-hold style random panbrello (tremolo and vibrato don't use this mechanism in IT)
		if (isIT && aktMemo.panbrelloType==3) // Random type
		{
			if (aktMemo.panbrelloTablePos==0 || aktMemo.panbrelloTablePos>=aktMemo.panbrelloStep)
			{
				aktMemo.panbrelloTablePos = 0;
				aktMemo.panbrelloRandomMemory = newPanning;
			}
			aktMemo.panbrelloTablePos++;
			newPanning = aktMemo.panbrelloRandomMemory;
		}
		else
			aktMemo.panbrelloTablePos = aktMemo.panbrelloTablePos + aktMemo.panbrelloStep;

		newPanning = ((newPanning * aktMemo.panbrelloAmplitude) + 2) >> 3; // +2: round me at bit 1
		newPanning += aktMemo.currentInstrumentPanning;
		aktMemo.panning = (newPanning<0)?0:((newPanning>256)?256:newPanning);
	}
	/**
	 * Convenient Method for the tremolo effekt 
	 * @param aktMemo
	 */
	protected void doTremoloEffekt(final ChannelMemory aktMemo)
	{
		int delta = getVibratoDelta(aktMemo.tremoloType, aktMemo.tremoloTablePos);
		aktMemo.tremoloTablePos = aktMemo.tremoloTablePos + (aktMemo.tremoloStep<<2);
		aktMemo.currentVolume = aktMemo.currentInstrumentVolume + ((delta * aktMemo.tremoloAmplitude) >> 5); // normally >>6 because -64..+64
		if (aktMemo.currentVolume>ModConstants.MAX_SAMPLE_VOL) aktMemo.currentVolume = ModConstants.MAX_SAMPLE_VOL;
		else
		if (aktMemo.currentVolume<ModConstants.MIN_SAMPLE_VOL) aktMemo.currentVolume = ModConstants.MIN_SAMPLE_VOL;
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
		}
		else
		if (aktMemo.tremorOfftime>0)
		{
			aktMemo.tremorOfftime--;
			// asynchronous! - in next row new values for ontime/offtime can be set
			// we need to take a look into next row first
			//if (aktMemo.tremorOfftime<=0) aktMemo.tremorOntime = aktMemo.tremorOntimeSet;
			aktMemo.currentVolume = 0;
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
		if (aktMemo.panning<0) aktMemo.panning = 0; else if (aktMemo.panning>256) aktMemo.panning = 256;
		aktMemo.currentInstrumentPanning = aktMemo.panning; // IT stays on panning value and pans around that one
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
			if ((aktMemo.currentElement.getPeriod()>0 || aktMemo.currentElement.getNoteIndex()>0) && !inTick)
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
		if (aktMemo.effekt==0 && aktMemo.effektParam==0) return;
		
		switch (aktMemo.effekt)
		{
			case 0x04 : 		// VolumeSlide, BUT Fine Slide only on first Tick
				if (!isFineSlide(aktMemo.volumSlideValue))
					doVolumeSlideEffekt(aktMemo);
				break;
			case 0x05: 			// Porta Down
				doPortaDown(aktMemo, false);
				break;
			case 0x06: 			// Porta Up
				doPortaUp(aktMemo, false);
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
				doVibratoEffekt(aktMemo, false);
				if (!isFineSlide(aktMemo.volumSlideValue))
					doVolumeSlideEffekt(aktMemo);
				break;
			case 0x0C :			// Porta to Note + VolumeSlide
				doPortaToNoteEffekt(aktMemo);
				if (!isFineSlide(aktMemo.volumSlideValue))
					doVolumeSlideEffekt(aktMemo);
				break;
			case 0x0E :			// Channel Volume Slide, if *NOT* Fine Slide
				if (!isFineSlide(aktMemo.channelVolumSlideValue))
					doChannelVolumeSlideEffekt(aktMemo);
				break;
			case 0x10 :			// Panning Slide
				if (!isFineSlide(aktMemo.panningSlideValue))
					doPanningSlideEffekt(aktMemo);
				break;
			case 0x11 :			// Retrig Note
				doRetrigNote(aktMemo, true);
				break;
			case 0x12 :			// Tremolo
				doTremoloEffekt(aktMemo);
				break;
			case 0x13 :			// Extended
				switch (aktMemo.effektParam>>4)
				{
					case 0xC :	// Note Cut
						if (aktMemo.noteCutCount>0)
						{
							aktMemo.noteCutCount--;
							if (aktMemo.noteCutCount<=0)
							{
								aktMemo.noteCutCount=-1;
								aktMemo.currentVolume = 0;
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
			case 0x1C:			// Smooth Midi Macro
				final MidiMacros macro = mod.getMidiConfig();
				if (macro!=null)
				{
		            if (aktMemo.effektParam<0x80)
		                processMIDIMacro(aktMemo, true, macro.getMidiSFXExt(aktMemo.activeMidiMacro), aktMemo.effektParam);
		            else
		                processMIDIMacro(aktMemo, true, macro.getMidiZXXExt(aktMemo.effektParam & 0x7F), 0);
				}
	            break;
			case 0x19 :			// Panbrello
				doPanbrelloEffekt(aktMemo);
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
		if (aktMemo.volumeEffekt==0) return;
		
		switch (aktMemo.volumeEffekt)
		{
			case 0x01: // Set Volume
				aktMemo.currentVolume = aktMemo.volumeEffektOp;
				if (aktMemo.currentVolume>ModConstants.MAX_SAMPLE_VOL) aktMemo.currentVolume = ModConstants.MAX_SAMPLE_VOL;
				else
				if (aktMemo.currentVolume<ModConstants.MIN_SAMPLE_VOL) aktMemo.currentVolume = ModConstants.MIN_SAMPLE_VOL;
				aktMemo.currentInstrumentVolume = aktMemo.currentVolume;
				break;
			case 0x02: // Volslide down
				if (aktMemo.volumeEffektOp!=0) aktMemo.volumSlideValue = aktMemo.volumeEffektOp & 0xF;
				if ((mod.getSongFlags()&ModConstants.SONG_FASTVOLSLIDES)!=0)
					doVolumeSlideEffekt(aktMemo);
				break;
			case 0x03: // Volslide up
				if (aktMemo.volumeEffektOp!=0) aktMemo.volumSlideValue = (aktMemo.volumeEffektOp<<4) & 0xF0;
				if ((mod.getSongFlags()&ModConstants.SONG_FASTVOLSLIDES)!=0)
					doVolumeSlideEffekt(aktMemo);
				break;
			case 0x04: // Fine Volslide down
				if (aktMemo.volumeEffektOp!=0) aktMemo.volumSlideValue = (aktMemo.volumeEffektOp & 0xF) | 0xF0;
				doVolumeSlideEffekt(aktMemo);
				break;
			case 0x05: // Fine Volslide up
				if (aktMemo.volumeEffektOp!=0) aktMemo.volumSlideValue = ((aktMemo.volumeEffektOp<<4) & 0xF0) | 0x0F;
				doVolumeSlideEffekt(aktMemo);
				break;
			case 0x07: // vibrato depth and enable
				if (aktMemo.volumeEffektOp!=0) aktMemo.vibratoAmplitude = aktMemo.volumeEffektOp<<2;
				aktMemo.vibratoOn = true;
				if (isIT) doVibratoEffekt(aktMemo, false); // IT: on first TICK
				break;
			case 0x08: // Set Panning
				doPanning(aktMemo, aktMemo.volumeEffektOp, ModConstants.PanBits.Pan6Bit);
				break;
			case 0x0B: // Tone Porta
				final PatternElement element = aktMemo.currentElement;
				if (element!=null && (element.getPeriod()>0 && element.getNoteIndex()>0)) aktMemo.portaTargetNotePeriod = getFineTunePeriod(aktMemo);
				if (aktMemo.volumeEffektOp!=0)
				{
					final int index = (aktMemo.volumeEffektOp>9)?9:aktMemo.volumeEffektOp&0x0F;
					aktMemo.portaNoteStep = ModConstants.IT_VolColumnPortaNotSpeedTranslation[index];
					if ((mod.getSongFlags() & ModConstants.SONG_ITCOMPATMODE)==0) aktMemo.IT_EFG = aktMemo.portaNoteStep;
				}
				break;
			case 0x0C: // Porta Down
				if (aktMemo.volumeEffektOp!=0)
				{
					aktMemo.portaStepDown = aktMemo.volumeEffektOp<<2;
					if ((mod.getSongFlags() & ModConstants.SONG_ITCOMPATMODE)==0) aktMemo.IT_EFG = aktMemo.portaStepDown;
				}
				break;
			case 0x0D: // Porta Up
				if (aktMemo.volumeEffektOp!=0)
				{
					aktMemo.portaStepUp = aktMemo.volumeEffektOp<<2;
					if ((mod.getSongFlags() & ModConstants.SONG_ITCOMPATMODE)==0) aktMemo.IT_EFG = aktMemo.portaStepUp;
				}
				break;
//			case 0x0E: // Sample Cues - MPT specific
//				final Sample sample = aktMemo.currentSample;
//				if (sample!=null && sample.cues!=null && aktMemo.volumeEffektOp <= sample.cues.length)
//				{
//					if (aktMemo.volumeEffektOp!=0) aktMemo.sampleOffset = sample.cues[aktMemo.volumeEffektOp - 1];
//					doSampleOffsetEffekt(aktMemo, aktMemo.currentElement);
//				}
//				break;
			default:
				Log.debug(String.format("Unknown VolEffekt: Effect:%02X Op:%02X in [Pattern:%03d: Row:%03d Channel:%03d]", Integer.valueOf(aktMemo.volumeEffekt), Integer.valueOf(aktMemo.volumeEffektOp), Integer.valueOf(currentPatternIndex), Integer.valueOf(currentRow), Integer.valueOf(aktMemo.channelNumber+1)));
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
		if (aktMemo.volumeEffekt==0) return;
		
		switch (aktMemo.volumeEffekt)
		{
			case 0x02: // Volslide down
			case 0x03: // Volslide up
				doVolumeSlideEffekt(aktMemo);
				break;
			case 0x07: // vibrato
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
		if (isEffekt && ((effektParam&0xF)==0) && isS3M) return false;
		return isEffekt;
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
	 * @param effekt
	 * @param effektParam
	 * @return
	 * @see de.quippy.javamod.multimedia.mod.mixer.BasicModMixer#getEffektOpMemory(int, int)
	 */
	@Override
	protected int getEffektOpMemory(final ChannelMemory aktMemo, final int effekt, int effektParam)
	{
		if (effekt==0x13 && effektParam == 0) return aktMemo.S_Effect_Memory;
		return effektParam;
	}
	/**
	 * @param effekt
	 * @param effektParam
	 * @return
	 * @see de.quippy.javamod.multimedia.mod.mixer.BasicModMixer#getEffectName(int, int)
	 */
	@Override
	public String getEffectName(final int effekt, final int effektParam)
	{
		if (effekt==0 && effektParam==0) return Helpers.EMPTY_STING;
		
		switch (effekt)
		{
			case 0x00: return "only OP set(!)";
			case 0x01: return "Set Speed";
			case 0x02: return "Pattern Position Jump";
			case 0x03: return "Pattern break";
			case 0x04: return "Volume Slide";
			case 0x05: return "Porta Down";
			case 0x06: return "Porta Up";
			case 0x07: return "Porta To Note";
			case 0x08: return "Vibrato";
			case 0x09: return "Tremor";
			case 0x0A: return "Arpeggio";
			case 0x0B: return "Vibrato + VolSlide";
			case 0x0C: return "PortaNote + VolSlide";
			case 0x0D: return "Set Volume";
			case 0x0E: return "Volume Slide";
			case 0x0F: return "Sample Offset";
			case 0x10: return "Panning Slide";
			case 0x11: return "Retrig Note";
			case 0x12: return "Tremolo";
			case 0x13 : 		// Extended
				final int effektOpEx = effektParam&0x0F;
				switch (effektParam>>4)
				{
					case 0x1: return "Glissando";
					case 0x2: return "Set FineTune";
					case 0x3: return "Set Vibrato Type";
					case 0x4: return "Set Tremolo Type";
					case 0x5: return "Set Panbrello Type";
					case 0x6: return "Pattern Delay Frame";
					case 0x7:	// set NNA and others
						switch (effektOpEx)
						{
							case 0x0: return "Note Cut all NNAs";
							case 0x1: return "Note Off all NNAs";
							case 0x2: return "Note Fade all NNAs";
							case 0x3: return "NNA Cut";
							case 0x4: return "NNA Continue";
							case 0x5: return "NNA Off";
							case 0x6: return "NNA Fade";
							case 0x7: return "Volume Envelope Off";
							case 0x8: return "Volume Envelope On";
							case 0x9: return "Panning Envelope Off";
							case 0xA: return "Panning Envelope On";
							case 0xB: return "Pitch Envelope Off";
							case 0xC: return "Pitch Envelope On";
						}
						break;
					case 0x8: return "Set Fine Panning";
					case 0x9:	// Sound Control
						switch (effektOpEx)
						{
							case 0x0: return "No Surround";
							case 0x1: return "Enabl. Surround";
						}
						break;
					case 0xA: return "Set High Offset";
					case 0xB: if (effektOpEx==0) return "Jump Loop Set"; else return "Jump Loop";
					case 0xC: return "Note Cut";
					case 0xD: return "Note Delay";
					case 0xE: return "Pattern Delay";
					case 0xF: if (isIT) return "Set Active Macro"; else return "Funk Repeat";
				}
				break;
			case 0x14: 
				if (effektParam>>4==0) 			// 0x0X
					return "Set BPM (slower)";
				else
				if (effektParam>>4==1) 			// 0x1X
					return "Set BPM (faster)";
				else
					return "Set BPM";			// 0x2X
			case 0x15: return "Fine Vibrato";
			case 0x16: return "Set Global Volume";
			case 0x17: return "Global Volume Slide";
			case 0x18: return "Set Panning";
			case 0x19: return "Panbrello";
			case 0x1A: return "Midi Macro";
			case 0x1B: return "Smooth Midi Macro";
			case 0x1C: return "Parameter Extension";
		}
		return "Unknown: " + Integer.toHexString(effekt) + "/" + Integer.toHexString(effektParam);
	}
	/**
	 * @param volumeEffekt
	 * @param volumeEffektOp
	 * @return
	 * @see de.quippy.javamod.multimedia.mod.mixer.BasicModMixer#getVolEffectName(int, int)
	 */
	@Override
	public String getVolEffectName(final int volumeEffekt, final int volumeEffektOp)
	{
		if (volumeEffekt==0) return Helpers.EMPTY_STING;
		
		switch (volumeEffekt)
		{
			case 0x01: return "Set Volume";
			case 0x02: return "Volslide down";
			case 0x03: return "Volslide up";
			case 0x04: return "Fine Volslide Down";
			case 0x05: return "Fine Volslide Up";
			case 0x07: if (volumeEffektOp!=0) return "Set Vibrato Depth"; else return "Vibrato";
			case 0x08: return "Set Panning";
			case 0x09: return "Panning Slide Left";
			case 0x0A: return "Panning Slide Right";
			case 0x0B: return "Porta To Note";
			case 0x0C: return "Porta Down";
			case 0x0D: return "Porta Up";
		}
		return "Unknown: " + Integer.toHexString(volumeEffekt);
	}
}
