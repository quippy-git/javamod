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

import de.quippy.javamod.multimedia.mod.ModConstants;
import de.quippy.javamod.multimedia.mod.loader.Module;
import de.quippy.javamod.multimedia.mod.loader.instrument.Sample;
import de.quippy.javamod.multimedia.mod.loader.pattern.PatternElement;
import de.quippy.javamod.multimedia.mod.midi.MidiMacros;
import de.quippy.javamod.system.Helpers;
import de.quippy.javamod.system.Log;

/**
 * This is the protracker mixing routine with all special mixing
 * on typical protracker events
 * @author Daniel Becker
 * @since 30.04.2006
 */
public class ProTrackerMixer extends BasicModMixer
{
	// if we ever want to support this, we can make a switch out of it...
	private boolean FT2LikeVolSlides = true; // false: Volslide and FineVolSlide share same memory - start with EAx or EBx and continue with A00
	
	/**
	 * Constructor for ProTrackerMixer
	 */
	public ProTrackerMixer(final Module mod, final int sampleRate, final int doISP, final int doNoLoops, final int maxNNAChannels)
	{
		super(mod, sampleRate, doISP, doNoLoops, maxNNAChannels);
	}

	/**
	 * Sets the borders for Portas
	 * @since 17.06.2010
	 * @param aktMemo
	 */
	protected void setPeriodBorders(ChannelMemory aktMemo)
	{
		if ((frequencyTableType&ModConstants.AMIGA_TABLE)!=0)
		{
			aktMemo.portaStepUpEnd = getFineTunePeriod(aktMemo, ModConstants.getNoteIndexForPeriod(113)+1);
			aktMemo.portaStepDownEnd = getFineTunePeriod(aktMemo, ModConstants.getNoteIndexForPeriod(856)+1);
		}
		else
		{
			aktMemo.portaStepUpEnd = getFineTunePeriod(aktMemo, 119); 
			aktMemo.portaStepDownEnd = getFineTunePeriod(aktMemo, 0);
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
		setPeriodBorders(aktMemo);
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
			if (forced || (nextElement.getVolumeEffekt()!=0x07 && nextElement.getEffekt()!=0x04 && nextElement.getEffekt()!=0x06)) //but only, if there is no vibrato following or we are forced
			{
				aktMemo.vibratoOn = false;
				if (!aktMemo.vibratoNoRetrig) aktMemo.vibratoTablePos = 0;
				setNewPlayerTuningFor(aktMemo);
			}
		}
		if (aktMemo.tremoloOn) // We have a tremolo for reset
		{
			if (forced || nextElement.getEffekt()!=0x07) //but only, if there is no tremolo following or we are forced
			{
				aktMemo.tremoloOn = false;
				if (!aktMemo.tremoloNoRetrig) aktMemo.tremoloTablePos = 0;
			}
		}
		if (aktMemo.panbrelloOn) // We have a panbrello for reset
		{
			if (forced || nextElement.getEffekt()!=0x22) //but only, if there is no panbrello following or we are forced
			{
				aktMemo.panbrelloOn = false;
				if (!aktMemo.panbrelloNoRetrig) aktMemo.panbrelloTablePos = 0;
			}
		}
	}
	/**
	 * Do the effects of a row. This is mostly the setting of effekts
	 * @param aktMemo
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
		final PatternElement element = aktMemo.currentElement;

		switch (aktMemo.effekt)
		{
			case 0x00:			// Arpeggio
				if (aktMemo.effektParam != 0) aktMemo.arpegioParam = aktMemo.effektParam;
				if (aktMemo.assignedNotePeriod!=0)
				{
					final int currentIndex = aktMemo.assignedNoteIndex + aktMemo.currentTranspose;
					aktMemo.arpegioNote[0] = getFineTunePeriod(aktMemo, currentIndex);
					aktMemo.arpegioNote[1] = getFineTunePeriod(aktMemo, currentIndex+(aktMemo.arpegioParam >>4));
					aktMemo.arpegioNote[2] = getFineTunePeriod(aktMemo, currentIndex+(aktMemo.arpegioParam&0xF));
					aktMemo.arpegioIndex=0;
				}
				break;
			case 0x01:			// Porta Up
				if (aktMemo.effektParam!=0)
				{
					aktMemo.portaStepUp=aktMemo.effektParam;
					setPeriodBorders(aktMemo);
				}
				break;
			case 0x02:			// Porta Down
				if (aktMemo.effektParam!=0)
				{
					aktMemo.portaStepDown=aktMemo.effektParam;
					setPeriodBorders(aktMemo);
				}
				break;
			case 0x03:	 		// Porta To Note
				if (element!=null && (element.getPeriod()>0 && element.getNoteIndex()>0)) aktMemo.portaTargetNotePeriod = getFineTunePeriod(aktMemo);
				if (aktMemo.effektParam!=0) aktMemo.portaNoteStep = aktMemo.effektParam;
				break;
			case 0x04:			// Vibrato
				if ((aktMemo.effektParam>>4)!=0) aktMemo.vibratoStep = aktMemo.effektParam>>4;
				if ((aktMemo.effektParam&0xF)!=0) aktMemo.vibratoAmplitude = aktMemo.effektParam&0xF;
				aktMemo.vibratoOn = true;
				break;
			case 0x05:			// Porta To Note + VolumeSlide
				if (element!=null && (element.getPeriod()>0 && element.getNoteIndex()>0)) aktMemo.portaTargetNotePeriod = getFineTunePeriod(aktMemo);
				// With Protracker Mods Porta without Parameter is just Porta, no Vol-Slide
				if (isMOD && aktMemo.effektParam==0) 
					aktMemo.volumSlideValue = 0;
				else
				{
					if (FT2LikeVolSlides)
					{
						if ((aktMemo.effektParam>>4)!=0)
							aktMemo.volumSlideValue = aktMemo.effektParam>>4;
						else
						if ((aktMemo.effektParam&0xF)!=0) 
							aktMemo.volumSlideValue = -(aktMemo.effektParam&0xF);
					}
					else
					{
						if (aktMemo.effektParam!=0) aktMemo.volumSlideValue = aktMemo.effektParam;
						if (isFineSlide(aktMemo.volumSlideValue))
							doVolumeSlideEffekt(aktMemo);
					}
				}
				break;
			case 0x06:			// Vibrato + VolumeSlide
				aktMemo.vibratoOn = true;
				// With Protracker Mods Vibrato without Parameter is just Vibrato, no Vol-Slide
				if (isMOD && aktMemo.effektParam==0) 
					aktMemo.volumSlideValue = 0;
				else
				{
					if (FT2LikeVolSlides)
					{
						if ((aktMemo.effektParam>>4)!=0)
							aktMemo.volumSlideValue = aktMemo.effektParam>>4;
						else
						if ((aktMemo.effektParam&0xF)!=0)
							aktMemo.volumSlideValue = -(aktMemo.effektParam&0xF);
					}
					else
					{
						if (aktMemo.effektParam!=0) aktMemo.volumSlideValue = aktMemo.effektParam;
						if (isFineSlide(aktMemo.volumSlideValue))
							doVolumeSlideEffekt(aktMemo);
					}
				}
				break;
			case 0x07:			// Tremolo
				if ((aktMemo.effektParam>>4)!=0) aktMemo.tremoloStep = aktMemo.effektParam>>4;
				if ((aktMemo.effektParam&0xF)!=0) aktMemo.tremoloAmplitude = aktMemo.effektParam&0xF;
				aktMemo.tremoloOn = true;
				break;
			case 0x08:			// Set Panning
				doPanning(aktMemo, aktMemo.effektParam, ModConstants.PanBits.Pan8Bit);
				break;
			case 0x09 : 		// Sample Offset
				if (aktMemo.effektParam!=0)
				{
					aktMemo.sampleOffset = aktMemo.highSampleOffset<<16 | aktMemo.effektParam<<8;
					aktMemo.highSampleOffset = 0;
				}
				if (element!=null && (element.getPeriod()>0 || element.getNoteIndex()>0) && aktMemo.currentSample!=null) 
				{
					final Sample sample = aktMemo.currentSample;
					if (aktMemo.sampleOffset >= sample.length || 
							((sample.loopType & ModConstants.LOOP_ON)!=0 && aktMemo.sampleOffset >= sample.loopStart) ||
							((sample.loopType & ModConstants.LOOP_SUSTAIN_ON)!=0 && aktMemo.sampleOffset >= sample.sustainLoopStart))
					{
						aktMemo.sampleOffset = aktMemo.currentSample.length-1;
					}
					aktMemo.currentSamplePos = aktMemo.sampleOffset; 
					aktMemo.isForwardDirection = true; aktMemo.currentTuningPos = 0;
				}
				break;
			case 0x0A:			// Volume Slide
				// With Protracker Mods Volumeslide without Parameter is not "old Parameter"
				if (isMOD && aktMemo.effektParam==0) 
					aktMemo.volumSlideValue = 0;
				else
				{
					if (FT2LikeVolSlides)
					{
						if ((aktMemo.effektParam>>4)!=0)
							aktMemo.volumSlideValue = aktMemo.effektParam>>4;
						else
						if ((aktMemo.effektParam&0xF)!=0) 
							aktMemo.volumSlideValue = -(aktMemo.effektParam&0xF);
					}
					else
					{
						if (aktMemo.effektParam!=0) aktMemo.volumSlideValue = aktMemo.effektParam;
						if (isFineSlide(aktMemo.volumSlideValue))
							doVolumeSlideEffekt(aktMemo);
					}
				}
				break;
			case 0x0B:			// Pattern position jump
				patternBreakJumpPatternIndex = aktMemo.effektParam;
				break;
			case 0x0C:			// Set volume
				aktMemo.currentVolume = aktMemo.effektParam;
				if (aktMemo.currentVolume>ModConstants.MAX_SAMPLE_VOL) aktMemo.currentVolume = ModConstants.MAX_SAMPLE_VOL;
				else
				if (aktMemo.currentVolume<ModConstants.MIN_SAMPLE_VOL) aktMemo.currentVolume = ModConstants.MIN_SAMPLE_VOL;
				aktMemo.currentInstrumentVolume = aktMemo.currentVolume;
				break;
			case 0x0D:			// Pattern break
				patternBreakRowIndex = ((aktMemo.effektParam>>4)*10)+(aktMemo.effektParam&0x0F);
				break;
			case 0x0E:
				final int effektOp = aktMemo.effektParam&0x0F;
				switch (aktMemo.effektParam>>4)
				{
					case 0x0:	// Set filter
						break;
					case 0x1:	// Fine Porta Up
						if (effektOp!=0) aktMemo.finePortaUp = effektOp<<ModConstants.PERIOD_SHIFT;
						aktMemo.currentNotePeriod -= aktMemo.finePortaUp;
						setNewPlayerTuningFor(aktMemo);
						break;
					case 0x2:	// Fine Porta Down
						if (effektOp!=0) aktMemo.finePortaDown = effektOp<<ModConstants.PERIOD_SHIFT; 
						aktMemo.currentNotePeriod += aktMemo.finePortaDown;
						setNewPlayerTuningFor(aktMemo);
						break;
					case 0x3:	// Glissando
						aktMemo.glissando = effektOp!=0;
						break;
					case 0x4:	// Set Vibrato Type
						aktMemo.vibratoType=effektOp&0x3;
						aktMemo.vibratoNoRetrig = (effektOp&0x4)!=0;
						break;
					case 0x5:	// Set FineTune
						aktMemo.currentFineTune = effektOp;
						aktMemo.currentFinetuneFrequency = ModConstants.it_fineTuneTable[effektOp];
						setNewPlayerTuningFor(aktMemo);
						break;
					case 0x6:	// JumpLoop
						if (effektOp==0) // Set a marker for loop
						{
							aktMemo.jumpLoopPatternRow = currentRow;
							aktMemo.jumpLoopPositionSet = true;
						}
						else
						{
							if (aktMemo.jumpLoopRepeatCount==-1) // was not set!
							{
								aktMemo.jumpLoopRepeatCount=effektOp;
								if (!aktMemo.jumpLoopPositionSet) // if not set, pattern Start is default!
								{
									aktMemo.jumpLoopPatternRow = 0; //aktMemo.lastJumpCounterRow + 1; --> IT use previous Breake Counter Position
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
					case 0x7:	// Set Tremolo Type
						aktMemo.tremoloType=effektOp&0x3;
						aktMemo.tremoloNoRetrig = (effektOp&0x4)==0x04;
						break;
					case 0x8:	// Set Fine Panning
						doPanning(aktMemo, effektOp, ModConstants.PanBits.Pan4Bit);
						break;
					case 0x9:	// Retrig Note
						aktMemo.retrigCount = aktMemo.retrigMemo = effektOp;
						break;
					case 0xA:	// Fine VolSlide Up
						if (FT2LikeVolSlides)
						{
							aktMemo.currentVolume += effektOp;
							if (aktMemo.currentVolume>ModConstants.MAX_SAMPLE_VOL) aktMemo.currentVolume = ModConstants.MAX_SAMPLE_VOL;
							aktMemo.currentInstrumentVolume = aktMemo.currentVolume;
						}
						else
						{
							if (effektOp!=0) aktMemo.volumSlideValue = ((effektOp<<4) & 0xF0) | 0x0F;
							doVolumeSlideEffekt(aktMemo);
						}
						break;
					case 0xB:	// Fine VolSlide Down
						if (FT2LikeVolSlides)
						{
							aktMemo.currentVolume -= effektOp;
							if (aktMemo.currentVolume<ModConstants.MIN_SAMPLE_VOL) aktMemo.currentVolume = ModConstants.MIN_SAMPLE_VOL;
							aktMemo.currentInstrumentVolume = aktMemo.currentVolume;
						}
						else
						{
							if (effektOp!=0) aktMemo.volumSlideValue = (effektOp & 0xF) | 0xF0;
							doVolumeSlideEffekt(aktMemo);
						}
						break;
					case 0xC:	// Note Cut
						if (aktMemo.noteCutCount<0)
						{
							if (effektOp==0) // instant noteCut on first Tick
								aktMemo.currentVolume = 0;
							else
								aktMemo.noteCutCount = effektOp;
						}
						break;
					case 0xD:	// Note Delay
						if (aktMemo.noteDelayCount<0) // is done in BasicModMixer::doRowEvents
						{
							if (effektOp==0)
								aktMemo.noteDelayCount = -1;
							else
								aktMemo.noteDelayCount = effektOp;
						}
						break;
					case 0xE:	// Pattern Delay --> # of Rows
						if (patternDelayCount<0) patternDelayCount=effektOp; // if currently in the patternDelay, do NOT reset the value. We would wait forever!!!
						break;
					case 0xF:	// set MIDI Makro (ProTracker: Funk It!)
						if (isXM) aktMemo.activeMidiMacro = aktMemo.effektParam&0x7F;
						break;
				}
				break;
			case 0x0F:			// SET SPEED / BPM
				if (aktMemo.effektParam>31 && !mod.getModSpeedIsTicks()) // set BPM
				{
					currentBPM = aktMemo.effektParam;
					samplePerTicks = calculateSamplesPerTick();
				}
				else
					currentTick = currentTempo = aktMemo.effektParam;
				break;
			case 0x10:			// Set global volume
				globalVolume = (aktMemo.effektParam)<<1;
				if (globalVolume>ModConstants.MAXGLOBALVOLUME) globalVolume = ModConstants.MAXGLOBALVOLUME;
				break;
			case 0x11:			// Global volume slide
				if (aktMemo.effektParam!=0) aktMemo.globalVolumSlideValue = aktMemo.effektParam;
				break;
			case 0x14:			// Key off
				aktMemo.keyOffCounter = aktMemo.effektParam;
				break;
			case 0x15:			// Set envelope position
				aktMemo.volEnvPos = aktMemo.effektParam;
				aktMemo.panEnvPos = aktMemo.effektParam;
				break;
			case 0x19:			// Panning slide
				if ((aktMemo.effektParam>>4)!=0)
					aktMemo.panningSlideValue = (aktMemo.effektParam>>4)<<2;
				else
				if ((aktMemo.effektParam&0xF)!=0)
					aktMemo.panningSlideValue = -((aktMemo.effektParam&0xF)<<2);
				break;
			case 0x1B:			// Multi retrig note
				if ((aktMemo.effektParam&0xF) !=0) aktMemo.retrigCount = aktMemo.retrigMemo = aktMemo.effektParam&0xF;
				if ((aktMemo.effektParam>>4)!=0) aktMemo.retrigVolSlide = aktMemo.effektParam>>4;
				doRetrigNote(aktMemo);
				break;
			case 0x1D:			// Tremor
				if (aktMemo.effektParam!=0)
				{
					aktMemo.currentInstrumentVolume = aktMemo.currentVolume;
					aktMemo.tremorOntimeSet = (aktMemo.effektParam>>4);
					aktMemo.tremorOfftimeSet = (aktMemo.effektParam&0xF);
				}
				doTremorEffekt(aktMemo);
				break;
			case 0x20:			// EMPTY
				// This effect can be set in OMPT, but is without function (yet?) 
				break;
			case 0x21:			// Extended XM Effects
				final int effektOpEx = aktMemo.effektParam&0x0F;
				switch (aktMemo.effektParam>>4)
				{
					case 0x1:	// Extra Fine Porta Up
						if (effektOpEx!=0) aktMemo.finePortaUpEx = effektOpEx<<2;
						aktMemo.currentNotePeriod -= aktMemo.finePortaUpEx;
						setNewPlayerTuningFor(aktMemo);
						break;
					case 0x2:	// Extra Fine Porta Down
						if (effektOpEx!=0) aktMemo.finePortaDownEx = effektOpEx<<2; 
						aktMemo.currentNotePeriod += aktMemo.finePortaDownEx;
						setNewPlayerTuningFor(aktMemo);
						break;
					case 0x5: 			// set PanBrello Waveform
						aktMemo.panbrelloType=effektOpEx&0x3;
						aktMemo.panbrelloNoRetrig = ((effektOpEx&0x04)!=0);
						break;
					case 0x6: 			// Fine Pattern Delay --> # of ticks
						if (patternTicksDelayCount<=0) patternTicksDelayCount = effektOpEx;
						break;
					case 0x9: // Sound Control
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
//							case 0xC: // Select global filter mode (IT compatibility). This is the default, when resonant filters are enabled with a Zxx effect, they will stay active until explicitely disabled by setting the cutoff frequency to the maximum (Z7F), and the resonance to the minimum (Z80).
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
					case 0xA: 			// Set High Offset
						aktMemo.highSampleOffset = aktMemo.effektParam&0x0F;
						break;
					default:
						Log.debug(String.format("Unknown Extended Effekt: Effect:%02X Op:%02X in [Pattern:%03d: Row:%03d Channel:%03d]", Integer.valueOf(aktMemo.effekt), Integer.valueOf(aktMemo.effektParam), Integer.valueOf(currentPatternIndex), Integer.valueOf(currentRow), Integer.valueOf(aktMemo.channelNumber+1)));
						break;
				}
				break;
			case 0x22: 			// Panbrello 
				if ((aktMemo.effektParam>>4)!=0) aktMemo.panbrelloStep = aktMemo.effektParam>>4;
				if ((aktMemo.effektParam&0xF)!=0) aktMemo.panbrelloAmplitude = aktMemo.effektParam&0xF;
				aktMemo.panbrelloOn = true;
				break;
			case 0x23:			// Midi Macro
				final MidiMacros macro = mod.getMidiConfig();
				if (macro!=null)
				{
		            if (aktMemo.effektParam<0x80)
		                processMIDIMacro(aktMemo, false, macro.getMidiSFXExt(aktMemo.activeMidiMacro), aktMemo.effektParam);
		            else
		                processMIDIMacro(aktMemo, false, macro.getMidiZXXExt(aktMemo.effektParam & 0x7F), 0);
				}
	            break;
			case 0x24:			// Smooth Midi Macro
				final MidiMacros smoothMacro = mod.getMidiConfig();
				if (smoothMacro!=null)
				{
		            if (aktMemo.effektParam<0x80)
		                processMIDIMacro(aktMemo, true, smoothMacro.getMidiSFXExt(aktMemo.activeMidiMacro), aktMemo.effektParam);
		            else
		                processMIDIMacro(aktMemo, true, smoothMacro.getMidiZXXExt(aktMemo.effektParam & 0x7F), 0);
				}
	            break;
			case 0x26:			// Parameter Extension
				// OMPT Specific, unsupported
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
				aktMemo.currentNotePeriod -= aktMemo.portaNoteStep<<ModConstants.PERIOD_SHIFT;
				if (aktMemo.currentNotePeriod<=aktMemo.portaTargetNotePeriod)
					aktMemo.currentNotePeriod=aktMemo.portaTargetNotePeriod;
			}
			else
			{
				aktMemo.currentNotePeriod += aktMemo.portaNoteStep<<ModConstants.PERIOD_SHIFT;
				if (aktMemo.currentNotePeriod>=aktMemo.portaTargetNotePeriod)
					aktMemo.currentNotePeriod=aktMemo.portaTargetNotePeriod;
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
	private void doPortaUp(final ChannelMemory aktMemo)
	{
		aktMemo.currentNotePeriod -= aktMemo.portaStepUp<<ModConstants.PERIOD_SHIFT;
		if (aktMemo.currentNotePeriod<aktMemo.portaStepUpEnd) aktMemo.currentNotePeriod = aktMemo.portaStepUpEnd;
		if (aktMemo.glissando)
			setNewPlayerTuningFor(aktMemo, getRoundedPeriod(aktMemo, aktMemo.currentNotePeriod>>ModConstants.PERIOD_SHIFT)<<ModConstants.PERIOD_SHIFT);
		else
			setNewPlayerTuningFor(aktMemo);
	}
	/**
	 * Convenient Method for the Porta Down Effekt
	 * @since 08.06.2020
	 * @param aktMemo
	 */
	private void doPortaDown(final ChannelMemory aktMemo)
	{
		aktMemo.currentNotePeriod += aktMemo.portaStepDown<<ModConstants.PERIOD_SHIFT;
		if (aktMemo.currentNotePeriod>aktMemo.portaStepDownEnd) aktMemo.currentNotePeriod = aktMemo.portaStepDownEnd;
		if (aktMemo.glissando)
			setNewPlayerTuningFor(aktMemo, getRoundedPeriod(aktMemo, aktMemo.currentNotePeriod>>ModConstants.PERIOD_SHIFT)<<ModConstants.PERIOD_SHIFT);
		else
			setNewPlayerTuningFor(aktMemo);
	}
	/**
	 * returns values in the range of -255..255
	 * @since 29.06.2020
	 * @param type
	 * @param position
	 * @return
	 */
	private int getVibratoDelta(final int type, int position)
	{
		position &= 0x3F;
		switch (type & 0x03)
		{
			default:
			case 0: //Sine
				return ModConstants.ModSinusTable[position];
			case 1: // Ramp Down / Sawtooth
				return ModConstants.ModRampDownTable[position];
			case 2: // Squarewave 
				return ModConstants.ModSquareTable[position];
			case 3: // random	
				return ModConstants.ModRandomTable[position];
		}
	}
	/**
	 * @param aktMemo
	 * @param currentSample
	 * @see de.quippy.javamod.multimedia.mod.mixer.BasicModMixer#doAutoVibratoEffekt(de.quippy.javamod.multimedia.mod.mixer.BasicModMixer.ChannelMemory, de.quippy.javamod.multimedia.mod.loader.instrument.Sample)
	 */
	@Override
	protected void doAutoVibratoEffekt(final ChannelMemory aktMemo, final Sample currentSample, final int currentPeriod)
	{
		if (currentSample.vibratoRate==0) return;
		
		final int vibDepthMod = currentSample.vibratoDepth << 8;
		if (currentSample.vibratoSweep == 0)
			aktMemo.autoVibratoAmplitude = vibDepthMod;
		else
		{
			if (!aktMemo.keyOff)
			{
				aktMemo.autoVibratoAmplitude += vibDepthMod / currentSample.vibratoSweep;
				if (aktMemo.autoVibratoAmplitude > vibDepthMod)
					aktMemo.autoVibratoAmplitude = vibDepthMod;
			}
		}
		
		aktMemo.autoVibratoTablePos += currentSample.vibratoRate;
		int periodAdd; // values -64..+64 - not -256..+256 !!
		switch (currentSample.vibratoType & 0x07) // No Random in FT2, as far as I know - but what the heck...
		{
			default:
			case 0:	periodAdd = -ModConstants.ITSinusTable[aktMemo.autoVibratoTablePos & 0xFF];		// Sine - With IT Table - but FT2 does that, too
					break;
			case 1:	periodAdd = ((aktMemo.autoVibratoTablePos & 0x80)!=0) ? +0x40 : -0x40;			// Square
					break;
			case 2:	periodAdd = ((0x40 + (aktMemo.autoVibratoTablePos >> 1)) & 0x7F) - 0x40;		// Ramp Up
					break;
			case 3:	periodAdd = ((0x40 - (aktMemo.autoVibratoTablePos >> 1)) & 0x7F) - 0x40;		// Ramp Down
					break;
			case 4:	periodAdd = ModConstants.ModRandomTable[aktMemo.autoVibratoTablePos & 0x3F]>>2;	// Random
					aktMemo.autoVibratoTablePos++;
					break;
		}
		periodAdd =	(periodAdd * aktMemo.autoVibratoAmplitude) >> (8+ModConstants.PERIOD_SHIFT);
		setNewPlayerTuningFor(aktMemo, currentPeriod + periodAdd);
	}
	/**
	 * Convenient Method for the vibrato effekt
	 * @param aktMemo
	 */
	protected void doVibratoEffekt(final ChannelMemory aktMemo)
	{
		int periodAdd = getVibratoDelta(aktMemo.vibratoType, aktMemo.vibratoTablePos);
		aktMemo.vibratoTablePos = aktMemo.vibratoTablePos + aktMemo.vibratoStep;

		periodAdd = ((periodAdd<<ModConstants.PERIOD_SHIFT) * aktMemo.vibratoAmplitude) >> 7;
		setNewPlayerTuningFor(aktMemo, aktMemo.currentNotePeriod + periodAdd);
	}
	/**
	 * Convenient Method for the panbrello effekt 
	 * @param aktMemo
	 */
	protected void doPanbrelloEffekt(final ChannelMemory aktMemo)
	{
		int newPanning = getVibratoDelta(aktMemo.panbrelloType, aktMemo.panbrelloTablePos >> 2);
		aktMemo.panbrelloTablePos = aktMemo.panbrelloTablePos + aktMemo.panbrelloStep;
		
		newPanning = ((newPanning * aktMemo.panbrelloAmplitude) + 8) >> 5; // +8: round me at bit 3
		newPanning += aktMemo.currentInstrumentPanning;
		aktMemo.panning = (newPanning<0)?0:((newPanning>256)?256:newPanning);
	}
	/**
	 * Convenient Method for the tremolo effekt 
	 * @param aktMemo
	 */
	protected void doTremoloEffekt(final ChannelMemory aktMemo)
	{
		int delta;
		if ((aktMemo.tremoloType & 0x03)==1 && mod.getFT2Tremolo())
		{
			// With FT2, tremolo ramp down implementation is affected by vibrato position (copy&paste bug) (found in OMPT)
			int ramp = (aktMemo.tremoloTablePos<<2)&0xFF;
			int vibPos = aktMemo.vibratoTablePos;
			if (aktMemo.vibratoOn) vibPos += aktMemo.vibratoStep;
			if ((vibPos & 0x3F)>=32) ramp ^= 0xFF;
			if ((aktMemo.tremoloTablePos & 0x3F)>=32)
				delta = -ramp;
			else
				delta = ramp;
		}
		else 
			delta = getVibratoDelta(aktMemo.tremoloType, aktMemo.tremoloTablePos);
		
		aktMemo.tremoloTablePos = aktMemo.tremoloTablePos + aktMemo.tremoloStep;
		aktMemo.currentVolume = aktMemo.currentInstrumentVolume + ((delta * aktMemo.tremoloAmplitude) >> 7); // normally >>8 because -256..+256
		if (aktMemo.currentVolume>ModConstants.MAX_SAMPLE_VOL) aktMemo.currentVolume = ModConstants.MAX_SAMPLE_VOL;
		else
		if (aktMemo.currentVolume<ModConstants.MIN_SAMPLE_VOL) aktMemo.currentVolume = ModConstants.MIN_SAMPLE_VOL;
	}
	/**
	 * The tremor effekt
	 * This effekt is added in FastTracker to play STMs
	 * So we will do it, like ImpulseTracker would do it.
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
	 * Convenient Method for the VolumeSlide Effekt
	 * @param aktMemo
	 */
	protected void doVolumeSlideEffekt(final ChannelMemory aktMemo)
	{
		if (FT2LikeVolSlides)
			aktMemo.currentVolume += aktMemo.volumSlideValue;
		else
			aktMemo.currentVolume += getFineSlideValue(aktMemo.volumSlideValue);

		if (aktMemo.currentVolume>ModConstants.MAX_SAMPLE_VOL) aktMemo.currentVolume = ModConstants.MAX_SAMPLE_VOL;
		else
		if (aktMemo.currentVolume<ModConstants.MIN_SAMPLE_VOL) aktMemo.currentVolume = ModConstants.MIN_SAMPLE_VOL;
		aktMemo.currentInstrumentVolume = aktMemo.currentVolume;
	}
	/**
	 * Convenient Method for the Global VolumeSlideEffekt
	 * @since 21.06.2006
	 * @param aktMemo
	 */
	protected void doGlobalVolumeSlideEffekt(final ChannelMemory aktMemo)
	{
		if ((aktMemo.globalVolumSlideValue&0xF0)!=0)
			globalVolume += (aktMemo.globalVolumSlideValue>>4)<<1;
		else
		if ((aktMemo.globalVolumSlideValue&0x0F)!=0)
			globalVolume -= (aktMemo.globalVolumSlideValue&0xF)<<1;

		if (globalVolume>ModConstants.MAXGLOBALVOLUME) globalVolume = ModConstants.MAXGLOBALVOLUME;
		else
		if (globalVolume<0) globalVolume = 0;
	}
	/**
	 * Convenient Method for the VolumeSlide Effekt
	 * @param aktMemo
	 */
	protected void doPanningSlideEffekt(final ChannelMemory aktMemo)
	{
		aktMemo.doSurround = false;
		aktMemo.panning += aktMemo.panningSlideValue;
		if (aktMemo.panning<0) aktMemo.panning = 0; else if (aktMemo.panning>256) aktMemo.panning = 256;
	}
	/**
	 * Retriggers the note and does volume slide
	 * @since 04.04.2020
	 * @param aktMemo
	 */
	protected void doRetrigNote(final ChannelMemory aktMemo)
	{
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
					case 0x8: /* No volume Change */ break;
					case 0x9: aktMemo.currentVolume++; break;
					case 0xA: aktMemo.currentVolume+=  2; break;
					case 0xB: aktMemo.currentVolume+=  4; break;
					case 0xC: aktMemo.currentVolume+=  8; break;
					case 0xD: aktMemo.currentVolume+= 16; break;
					case 0xE: aktMemo.currentVolume=  (aktMemo.currentVolume*3)>>1; break;
					case 0xF: aktMemo.currentVolume<<=1; break;
				}
				if (aktMemo.currentVolume>ModConstants.MAX_SAMPLE_VOL) aktMemo.currentVolume = ModConstants.MAX_SAMPLE_VOL;
				else
				if (aktMemo.currentVolume<ModConstants.MIN_SAMPLE_VOL) aktMemo.currentVolume = ModConstants.MIN_SAMPLE_VOL;
				aktMemo.currentInstrumentVolume = aktMemo.currentVolume;
			}
		}
	}
	/**
	 * Do the Effekts during Ticks
	 * @param aktMemo
	 */
	@Override
	protected void doTickEffekts(final ChannelMemory aktMemo)
	{
		if (aktMemo.effekt==0 && aktMemo.effektParam==0) return;
		
		switch (aktMemo.effekt)
		{
			case 0x00 :			// Arpeggio
				aktMemo.arpegioIndex = (aktMemo.arpegioIndex+1)%3;
				int nextNotePeriod = aktMemo.arpegioNote[aktMemo.arpegioIndex];
				if (nextNotePeriod!=0) setNewPlayerTuningFor(aktMemo, nextNotePeriod);
				break;
			case 0x01: 			// Porta Up
				doPortaUp(aktMemo);
				break;
			case 0x02: 			// Porta Down
				doPortaDown(aktMemo);
				break;
			case 0x03 :			// Porta to Note
				doPortaToNoteEffekt(aktMemo);
				break;
			case 0x04 :			// Vibrato
				doVibratoEffekt(aktMemo);
				break;
			case 0x05 :			// Porta to Note + VolumeSlide
				doPortaToNoteEffekt(aktMemo);
				if (FT2LikeVolSlides || !isFineSlide(aktMemo.volumSlideValue))
					doVolumeSlideEffekt(aktMemo);
				break;
			case 0x06:			// Vibrato + VolumeSlide
				doVibratoEffekt(aktMemo);
				if (FT2LikeVolSlides || !isFineSlide(aktMemo.volumSlideValue))
					doVolumeSlideEffekt(aktMemo);
				break;
			case 0x07 :			// Tremolo
				doTremoloEffekt(aktMemo);
				break;
			case 0x0A : 		// VolumeSlide
				if (FT2LikeVolSlides || !isFineSlide(aktMemo.volumSlideValue))
					doVolumeSlideEffekt(aktMemo);
				break;
			case 0x0E :			// Extended
				switch (aktMemo.effektParam>>4)
				{
					case 0x9 :	// Retrig Note
						aktMemo.retrigCount--;
						if (aktMemo.retrigCount<=0)
						{
							aktMemo.retrigCount = aktMemo.retrigMemo;
							resetInstrumentPointers(aktMemo);
						}
						break;
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
						// do This globally!
						break;
				}
				break;
			case 0x11 :			// Global volume slide
				doGlobalVolumeSlideEffekt(aktMemo);
				break;
			case 0x14 :			// Key off
				if (aktMemo.keyOffCounter>0)
				{
					aktMemo.keyOffCounter--;
					if (aktMemo.keyOffCounter<=0)
					{
						aktMemo.keyOffCounter = -1;
						aktMemo.keyOff = true;
					}
				}
				break;
			case 0x19 :			// Panning slide
				doPanningSlideEffekt(aktMemo);
				break;
			case 0x1B:			// Multi retrig note
				doRetrigNote(aktMemo);
				break;
			case 0x1D :			// Tremor
				doTremorEffekt(aktMemo);
				break;
			case 0x22:			// Panbrello
				doPanbrelloEffekt(aktMemo);
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
				if (FT2LikeVolSlides)
					aktMemo.volumSlideValue = -aktMemo.volumeEffektOp;
				else
					aktMemo.volumSlideValue = aktMemo.volumeEffektOp & 0x0F;
				break;
			case 0x03: // Volslide up
				if (FT2LikeVolSlides)
					aktMemo.volumSlideValue = aktMemo.volumeEffektOp;
				else
					aktMemo.volumSlideValue = (aktMemo.volumeEffektOp<<4) & 0xF0;
				break;
			case 0x04: // Fine VolSlide down
				if (FT2LikeVolSlides)
				{
					aktMemo.currentVolume -= aktMemo.volumeEffektOp;
					if (aktMemo.currentVolume<ModConstants.MIN_SAMPLE_VOL) aktMemo.currentVolume = ModConstants.MIN_SAMPLE_VOL;
					aktMemo.currentInstrumentVolume = aktMemo.currentVolume;
				}
				else
				{
					if (aktMemo.volumeEffektOp!=0) 
						aktMemo.volumSlideValue = (aktMemo.volumeEffektOp & 0xF) | 0xF0;
					else
						aktMemo.volumSlideValue = 0; // avoid 0xF0
					doVolumeSlideEffekt(aktMemo);
				}
				break;
			case 0x05: // Fine VolSlide up
				if (FT2LikeVolSlides)
				{
					aktMemo.currentVolume += aktMemo.volumeEffektOp;
					if (aktMemo.currentVolume>ModConstants.MAX_SAMPLE_VOL) aktMemo.currentVolume = ModConstants.MAX_SAMPLE_VOL;
					aktMemo.currentInstrumentVolume = aktMemo.currentVolume;
				}
				else
				{
					if (aktMemo.volumeEffektOp!=0) 
						aktMemo.volumSlideValue = ((aktMemo.volumeEffektOp<<4) & 0xF0) | 0x0F;
					else 
						aktMemo.volumSlideValue = 0; // avoid 0x0F
					doVolumeSlideEffekt(aktMemo);
				}
				break;
			case 0x06: // vibrato speed - does not activate
				aktMemo.vibratoStep = aktMemo.volumeEffektOp;
				break;
			case 0x07: // vibrato
				aktMemo.vibratoAmplitude = aktMemo.volumeEffektOp;
				aktMemo.vibratoOn = true;
				break;
			case 0x08: // Set Panning
				doPanning(aktMemo, aktMemo.volumeEffektOp, ModConstants.PanBits.Pan4Bit);
				break;
			case 0x09: // Panning Slide Left
				aktMemo.panningSlideValue = -aktMemo.volumeEffektOp;
				break;
			case 0x0A: // Panning Slide Right
				aktMemo.panningSlideValue = aktMemo.volumeEffektOp;
				break;
			case 0x0B: // Tone Porta
				final PatternElement element = aktMemo.currentElement;
				if (element!=null && (element.getPeriod()>0 && element.getNoteIndex()>0)) aktMemo.portaTargetNotePeriod = getFineTunePeriod(aktMemo);
				if (aktMemo.volumeEffektOp!=0)
				{
					aktMemo.portaNoteStep = aktMemo.volumeEffektOp<<4;
					if (isXM) // ProTracker does not have volume effects
					{
						// Yes, FT2 is *that* weird. If there is a Mx command in the volume column
						// and a normal 3xx command, the 3xx command is ignored but the Mx command's
						// effectiveness is doubled.
						// Found in comments of OpenMPT. In contrast to MPT we do not need
						// to kill the event of row command, it is overwritten here anyways
						if (aktMemo.currentElement.getEffekt()==0x03) // Tone Porta in Effect Column
							aktMemo.portaNoteStep<<=1;
					}
				}
				break;
//			case 0x0C: // Porta Down
//				if (aktMemo.volumeEffektOp!=0) aktMemo.portaStepDown = aktMemo.volumeEffektOp<<2;
//				break;
//			case 0x0D: // Porta Up
//				if (aktMemo.volumeEffektOp!=0) aktMemo.portaStepUp = aktMemo.volumeEffektOp<<2;
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
//			case 0x06: // vibrato speed - but does not enable vibrato
			case 0x07: // vibrato
				doVibratoEffekt(aktMemo);
				break;
			case 0x09: // Panning Slide Left
			case 0x0A: // Panning Slide Right
				doPanningSlideEffekt(aktMemo);
				break;
			case 0x0B: // Tone Porta
				doPortaToNoteEffekt(aktMemo);
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
	 * @param effektParam
	 * @return
	 * @see de.quippy.javamod.multimedia.mod.mixer.BasicModMixer#getEffektOpMemory(de.quippy.javamod.multimedia.mod.mixer.BasicModMixer.ChannelMemory, int, int)
	 */
	@Override
	protected int getEffektOpMemory(final ChannelMemory aktMemo, final int effekt, final int effektParam)
	{
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
			case 0x00: return "Arpeggio";
			case 0x01: return "Porta Up";
			case 0x02: return "Porta Down";
			case 0x03: return "Porta To Note";
			case 0x04: return "Vibrato";
			case 0x05: return "PortaNote + VolSlide";
			case 0x06: return "Vibrato + VolSlide";
			case 0x07: return "Tremolo";
			case 0x08: return "Set Panning";
			case 0x09: return "Sample Offset";
			case 0x0A: return "Volume Slide";
			case 0x0B: return "Pattern Position Jump";
			case 0x0C: return "Set volume";
			case 0x0D: return "Pattern break";
			case 0x0E:
				final int effektOp = effektParam&0x0F;
				switch (effektParam>>4)
				{
					case 0x0: return "(!)Set filter(!)";
					case 0x1: return "Fine Porta Up";
					case 0x2: return "Fine Porta Down";
					case 0x3: return "Glissando";
					case 0x4: return "Set Vibrato Type";
					case 0x5: return "Set FineTune";
					case 0x6: if (effektOp==0) return "Jump Loop Set"; else return "Jump Loop";
					case 0x7: return "Set Tremolo Type";
					case 0x8: return "Set Fine Panning";
					case 0x9: return "Retrig Note";
					case 0xA: return "Fine Volume Up";
					case 0xB: return "Fine Volume Down";
					case 0xC: return "Note Cut";
					case 0xD: return "Note Delay";
					case 0xE: return "Pattern Delay";
					case 0xF: if (isXM) return "Set MIDI Makro"; else return "Funk It!";
				}
				break;
			case 0x0F: if (effektParam>31 && !mod.getModSpeedIsTicks()) return "Set BPM"; else return "Set Speed"; 
			case 0x10: return "Set global volume";
			case 0x11: return "Global Volume Slide";
			case 0x14: return "Key off";
			case 0x15: return "Set Envelope Position";
			case 0x19: return "Panning Slide";
			case 0x1B: return "Retrig Note + VolSlide";
			case 0x1D: return "Tremor";
			case 0x20: return "Empty";
			case 0x21: // Extended XM Effects
				switch (effektParam>>4)
				{
					case 0x1: return "Extra Fine Porta Up";
					case 0x2: return "Extra Fine Porta Down";
					case 0x5: return "set Panbrello Waveform";
					case 0x6: return "Fine Pattern Delay";
					case 0x9: // Sound Control
						final int effektOpEx = effektParam&0x0F;
						switch (effektOpEx)
						{
							case 0x0: return "No Surround";
							case 0x1: return "Enabl. Surround";
						}
						break;
					case 0xA: return "Set High Offset";
				}
				break;
			case 0x22: return "Panbrello"; 
			case 0x23: return "Midi Macro";
			case 0x24: return "Smooth Midi Macro";
			case 0x26: return "Parameter Extension";
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
			case 0x06: return "Set Vibrato Speed";
			case 0x07: if (volumeEffektOp!=0) return "Set Vibrato Depth"; else return "Vibrato";
			case 0x08: return "Set Panning";
			case 0x09: return "Panning Slide Left";
			case 0x0A: return "Panning Slide Right";
			case 0x0B: return "Porta To Note";
		}
		return "Unknown: " + Integer.toHexString(volumeEffekt);
	}
}
