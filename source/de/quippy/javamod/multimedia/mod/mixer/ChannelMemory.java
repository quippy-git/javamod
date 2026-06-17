/*
 * @(#) ChannelMemory.java
 *
 * Created on 10.06.2026 by Daniel Becker
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

import de.quippy.javamod.multimedia.mod.ModConstants;
import de.quippy.javamod.multimedia.mod.loader.instrument.Instrument;
import de.quippy.javamod.multimedia.mod.loader.instrument.Sample;
import de.quippy.javamod.multimedia.mod.loader.pattern.PatternElement;

/**
 * @author Daniel Becker
 * @since 10.06.2026
 */
public class ChannelMemory
{
	public int channelNumber;
	public boolean muted, muteWasITforced;
	public boolean isNNA;
	public ChannelMemory rampDownMemory; // will store last seen values for a short ramp down

	public PatternElement currentElement;

	// These currents* are a fresh copy from the current pattern. Only needed as interim memory for NNA, PatternDelay and NoteDelay
	public int currentAssignedNotePeriod, currentAssignedNoteIndex, currentAssignedEffekt, currentAssignedEffektParam, currentAssignedVolumeEffekt, currentAssignedVolumeEffektOp, currentAssignedInstrumentIndex;
	public Instrument currentAssignedInstrument;

	// The assigned* are those from the pattern, when ready to be copied and processed
	// for instance: if no instrument was set in pattern, current* / assigend* instrument is used (as the last instrument set)
	public int assignedNotePeriod, assignedNoteIndex, assignedEffekt, assignedEffektParam, assignedVolumeEffekt, assignedVolumeEffektOp, assignedInstrumentIndex;
	public Instrument assignedInstrument;
	public Sample assignedSample;

	// currentNoteperiod and these down here are then the values to handle with
	public int currentNotePeriod, currentFinetuneFrequency;
	public int currentNotePeriodSet; // used to save the current note period set with "setNewPlayerTuningFor"
	public int currentFineTune, currentTranspose;
	public Sample currentSample;
	public int currentTuning, currentTuningPos, currentSamplePos, interpolationMagic;
	public boolean isForwardDirection;
	public int volEnvTick, panEnvTick, pitchEnvTick;
	public int volXMEnvPos, panXMEnvPos;
	public boolean instrumentFinished, keyOff, noteCut, noteFade;
	public int tempNNAAction, tempVolEnv, tempPanEnv, tempPitchEnv;
	public int keyOffCounter;

	public int currentVolume, currentInstrumentVolume, channelVolume, fadeOutVolume, panning, currentInstrumentPanning;
	public int actVolumeLeft, actVolumeRight, actRampVolLeft, actRampVolRight, deltaVolLeft, deltaVolRight;
	public boolean doFastVolRamp;
	public int channelVolumSlideValue;

	public boolean doSurround;

	public int autoVibratoTablePos, autoVibratoAmplitude, autoVibratoSweep;

	// Midi Macros
	public int activeMidiMacro;
	public int lastZxxParam;
	public int midiVULeft, midiVURight;
	// for Midi Arpeggios and to send appropriate Midi_OFF-commands, we need to remember the
	// arpeggio base note and the last arpeggio note
	public int lastMidiNoteWithoutArp;
	public int arpeggioLastNote;
	public int mictroTuning; // use setter and getters for this one!
	public int calculatedVolume; // volume after envelopes and instrument fade for midi plugins
	public int calculatedPanning; // same for panning

	// Resonance Filter
	public boolean filterOn;
	public int filterMode;
	public int resonance, cutOff;
	public int swingVolume, swingPanning, swingResonance, swingCutOff;
	public long filter_A0, filter_B0, filter_B1, filter_HP;
	public long filter_Y1, filter_Y2, filter_Y3, filter_Y4;

	// The effect memories
	public boolean glissando;
	public int arpeggioIndex, arpeggioNote[], arpeggioParam;
	public int portaStepUp, portaStepUpEnd, portaStepDown, portaStepDownEnd;
	public int finePortaUp, finePortaDown, finePortaUpEx, finePortaDownEx;
	public int portaNoteStep, portaTargetNotePeriod, portamentoDirection_PT_FT;
	public int volumSlideValue, globalVolumSlideValue;
	public int XMFineVolSlideUp, XMFineVolSlideDown;
	public int panningSlideValue;
	public int vibratoTablePos, vibratoStep, vibratoAmplitude, vibratoType;
	public boolean vibratoOn, vibratoVolOn, vibratoNoRetrig;
	public int tremoloTablePos, tremoloStep, tremoloAmplitude, tremoloType;
	public boolean tremoloOn, tremoloNoRetrig;
	public int panbrelloTablePos, panbrelloStep, panbrelloAmplitude, panbrelloType, panbrelloRandomMemory; // panbrelloRandomMemory only for IT
	public boolean panbrelloOn, panbrelloNoRetrig;
	public int tremorOntime, tremorOfftime, tremorOntimeSet, tremorOfftimeSet;
	public boolean tremorWasActive;
	public int retrigCount, retrigMemo, retrigVolSlide;
	public boolean FT2AllowRetriggerQuirk;
	public int sampleOffset, highSampleOffset, prevSampleOffset;
	public int oldTempoParameter; // IT has Tempo memory
	public int S_Effect_Memory; // IT specific S00 Memory
	public int IT_EFG; // IT specific: linked memory
	public int EFxSpeed, EFxDelay, EFxOffset; // MOD specific: invertLoop (trash the sample)
	
	public int jumpLoopPatternRow, jumpLoopRepeatCount, jumpLoopITLastRow;

	public int noteDelayCount, noteCutCount;

	// only needed for display
	public long bigSampleLeft, bigSampleRight;

	public ChannelMemory()
	{
		channelNumber = -1;
		currentInstrumentPanning = panning = 128; // 0-256, this is therefore center
		actRampVolLeft =
		actRampVolRight =
		deltaVolLeft =
		deltaVolRight =
		currentVolume =
		currentInstrumentVolume =
		channelVolume =
		channelVolumSlideValue = 0;
		doFastVolRamp = false;
		fadeOutVolume = ModConstants.MAXFADEOUTVOLUME;

		muted = muteWasITforced = false;
		assignedNotePeriod = currentNotePeriod = currentNotePeriodSet =
		currentFinetuneFrequency = currentFineTune = 0;
		currentTuning = currentTuningPos = currentSamplePos = interpolationMagic = 0;
		isForwardDirection = true;
		instrumentFinished = true;
		keyOffCounter = -1;
		noteCut = keyOff = noteFade = false;
		tempNNAAction = tempVolEnv = tempPanEnv = tempPitchEnv = -1;

		volEnvTick = panEnvTick = pitchEnvTick = 0;
		volXMEnvPos = panXMEnvPos = 0;
		swingVolume = swingPanning = swingResonance = swingCutOff = 0;

		arpeggioIndex = noteDelayCount = noteCutCount = -1;
		arpeggioParam = 0;
		arpeggioNote = new int[3];
		portaStepUp = portaStepDown = portaStepUpEnd = portaStepDownEnd = 0;
		finePortaDown = finePortaUp = 0;
		finePortaDownEx = finePortaUpEx = 0;
		portaNoteStep = portamentoDirection_PT_FT = volumSlideValue = globalVolumSlideValue = 0;
		XMFineVolSlideUp = XMFineVolSlideDown = 0;
		portaTargetNotePeriod = -1;
		vibratoTablePos = vibratoStep = vibratoAmplitude = vibratoType = 0;
		vibratoOn = vibratoNoRetrig = false;
		autoVibratoTablePos = autoVibratoAmplitude = 0;
		tremoloTablePos = tremoloStep = tremoloAmplitude = tremoloType = 0;
		tremoloOn = tremoloNoRetrig = false;
		panbrelloTablePos = panbrelloStep = panbrelloAmplitude = panbrelloType = panbrelloRandomMemory = 0;
		panbrelloOn = panbrelloNoRetrig = false;
		glissando=false;
		tremorOntime = tremorOfftime = tremorOntimeSet = tremorOfftimeSet = 0;
		tremorWasActive = false;
		retrigCount = retrigMemo = retrigVolSlide = sampleOffset = highSampleOffset = prevSampleOffset = 0;
		oldTempoParameter = S_Effect_Memory = IT_EFG = 0;
		EFxSpeed = EFxDelay = EFxOffset = 0;

		doSurround = false;

		activeMidiMacro = 0;
		midiVULeft = midiVURight = 0;
		arpeggioLastNote = ModConstants.NO_NOTE;
		lastMidiNoteWithoutArp = ModConstants.NO_NOTE;
		mictroTuning = 0;
		calculatedVolume = calculatedPanning = 0;

		filterOn = false;
		filterMode = 0;
		resonance = 0;
		lastZxxParam = cutOff = 0x7F;
		filter_A0 = filter_B0 = filter_B1 = filter_HP = 0;
		filter_Y1 = filter_Y2 = filter_Y3 = filter_Y4 = 0;

		jumpLoopPatternRow = jumpLoopRepeatCount = jumpLoopITLastRow = -1;
	}
	/**
	 * Every possible way to create a 1:1 copy of ChannelMemory for NNA
	 * failed (Clone, Serializable, Reflection)
	 * However, this method is now generated via reflection by
	 * "de.quippy.javamod.test.TableGenerator.java"
	 * @since 11.06.2020
	 * @param fromMe
	 */
	protected void setUpFrom(final ChannelMemory fromMe)
	{
		channelNumber = fromMe.channelNumber;
		muted = fromMe.muted;
		muteWasITforced = fromMe.muteWasITforced;
		assignedInstrumentIndex = fromMe.assignedInstrumentIndex;
		assignedInstrument = fromMe.assignedInstrument;
		currentNotePeriod = fromMe.currentNotePeriod;
		currentFinetuneFrequency = fromMe.currentFinetuneFrequency;
		currentNotePeriodSet = fromMe.currentNotePeriodSet;
		currentFineTune = fromMe.currentFineTune;
		currentTranspose = fromMe.currentTranspose;
		currentSample = fromMe.currentSample;
		currentTuning = fromMe.currentTuning;
		currentTuningPos = fromMe.currentTuningPos;
		currentSamplePos = fromMe.currentSamplePos;
		interpolationMagic = fromMe.interpolationMagic;
		isForwardDirection = fromMe.isForwardDirection;
		volEnvTick = fromMe.volEnvTick;
		panEnvTick = fromMe.panEnvTick;
		pitchEnvTick = fromMe.pitchEnvTick;
		instrumentFinished = fromMe.instrumentFinished;
		keyOff = fromMe.keyOff;
		noteCut = fromMe.noteCut;
		noteFade = fromMe.noteFade;
		tempNNAAction = fromMe.tempNNAAction;
		tempVolEnv = fromMe.tempVolEnv;
		tempPanEnv = fromMe.tempPanEnv;
		tempPitchEnv = fromMe.tempPitchEnv;
		keyOffCounter = fromMe.keyOffCounter;
		currentVolume = fromMe.currentVolume;
		currentInstrumentVolume = fromMe.currentInstrumentVolume;
		channelVolume = fromMe.channelVolume;
		fadeOutVolume = fromMe.fadeOutVolume;
		panning = fromMe.panning;
		currentInstrumentPanning = fromMe.currentInstrumentPanning;
		actVolumeLeft = fromMe.actVolumeLeft;
		actVolumeRight = fromMe.actVolumeRight;
		actRampVolLeft = fromMe.actRampVolLeft;
		actRampVolRight = fromMe.actRampVolRight;
		deltaVolLeft = fromMe.deltaVolLeft;
		deltaVolRight = fromMe.deltaVolRight;
		channelVolumSlideValue = fromMe.channelVolumSlideValue;
		doSurround = fromMe.doSurround;
		autoVibratoTablePos = fromMe.autoVibratoTablePos;
		autoVibratoAmplitude = fromMe.autoVibratoAmplitude;
		autoVibratoSweep = fromMe.autoVibratoSweep;
		activeMidiMacro = fromMe.activeMidiMacro;
		lastZxxParam = fromMe.lastZxxParam;
		filterOn = fromMe.filterOn;
		filterMode = fromMe.filterMode;
		resonance = fromMe.resonance;
		cutOff = fromMe.cutOff;
		swingVolume = fromMe.swingVolume;
		swingPanning = fromMe.swingPanning;
		swingResonance = fromMe.swingResonance;
		swingCutOff = fromMe.swingCutOff;
		filter_A0 = fromMe.filter_A0;
		filter_B0 = fromMe.filter_B0;
		filter_B1 = fromMe.filter_B1;
		filter_HP = fromMe.filter_HP;
		filter_Y1 = fromMe.filter_Y1;
		filter_Y2 = fromMe.filter_Y2;
		filter_Y3 = fromMe.filter_Y3;
		filter_Y4 = fromMe.filter_Y4;

		prepareRampDown();
	}
	/**
	 * start the ramp down
	 * @since 07.06.2026
	 * @param aktMemo
	 */
	protected void setUpRampDown()
	{
		// is already checked in BasicModMixer::startRampDown
//		if (hasMidiOutput()) return; // no ramp down for midi channels

//		rampDownMemory.currentElement = currentElement; // DEBUG:where were we set? 

		if (rampDownMemory.currentSample==null) rampDownMemory.currentSample = currentSample;
		if (rampDownMemory.assignedInstrument==null) rampDownMemory.assignedInstrument = assignedInstrument;
		rampDownMemory.instrumentFinished = instrumentFinished;
		rampDownMemory.muted = muted;
		rampDownMemory.currentTuning = currentTuning;
		rampDownMemory.currentTuningPos = currentTuningPos;
		rampDownMemory.currentSamplePos = currentSamplePos;
		rampDownMemory.interpolationMagic = interpolationMagic;
		rampDownMemory.isForwardDirection = isForwardDirection;
		rampDownMemory.keyOff = keyOff;
		// Copy Resonance
		rampDownMemory.filterOn = filterOn;
		rampDownMemory.filter_A0 = filter_A0;
		rampDownMemory.filter_B0 = filter_B0;
		rampDownMemory.filter_B1 = filter_B1;
		rampDownMemory.filter_HP = filter_HP;
		rampDownMemory.filter_Y1 = filter_Y1;
		rampDownMemory.filter_Y2 = filter_Y2;
		rampDownMemory.filter_Y3 = filter_Y3;
		rampDownMemory.filter_Y4 = filter_Y4;
		// current volume reached
		rampDownMemory.actRampVolLeft = actRampVolLeft;
		rampDownMemory.actRampVolRight = actRampVolRight;
		// ramp down to silence
		rampDownMemory.actVolumeLeft = rampDownMemory.actVolumeRight = 0;
		rampDownMemory.doFastVolRamp = true;
	}
	/**
	 * Prepare / copy last seen values for a ramp down
	 * @since 07.06.2026
	 * @param aktMemo the channel memory to copy from
	 */
	protected void prepareRampDown()
	{
		if (!hasMidiOutput()) // no RampDown for midi channels
		{
			rampDownMemory.currentSample = currentSample;
			rampDownMemory.assignedInstrument = assignedInstrument;
		}
		// Do not mix it (yet!) - for instance because of note delay
		rampDownMemory.instrumentFinished = true;
	}
	/**
	 * This channel is active if
	 * - it has a sample set
	 * - its tuning is not 0
	 * - its playing instrument has not finished yet
	 * - its channelNumber is not -1 (that is a free NNA)
	 * - Silence is not a factor - samples need to be rendered even if silent (XMs)
	 * @since 30.03.2010
	 * @param aktMemo
	 * @return
	 */
	protected boolean isChannelActive()
	{
		return (!instrumentFinished && currentTuning!=0 && currentSample!=null && channelNumber!=-1);
	}
	/**
	 * @since 15.05.2026
	 * @return will return true, if the last instrument read from the pattern is a midi instrument
	 */
	protected boolean hasMidiOutput()
	{
		// we use the xm_enableMidi field - also for ITs and OMPTs - if valid midi data is present, we assume midi is used
		return (currentAssignedInstrument!=null && currentAssignedInstrument.hasValidMidiData);
	}
	/**
	 * @since 18.05.2026
	 * @return
	 */
	public int getMIDIPitchBend()
	{ 
		return (mictroTuning + 0x8000) >> 2; 
	}
	/**
	 * @since 18.05.2026
	 * @param high
	 * @param low
	 */
	public void setMIDIPitchBend(final int high, final int low)
	{
		mictroTuning = ((high << 9) | (low << 2)) - 0x8000;
	}
	/**
	 * @since 21.05.2026
	 * @return
	 */
	public int getPluginNote()
	{
		int plugNote = lastMidiNoteWithoutArp;
		if (currentAssignedInstrument!=null && plugNote>=ModConstants.NOTE_MIN)
		{
			plugNote = currentAssignedInstrument.getNoteIndex(plugNote - ModConstants.NOTE_MIN) + ModConstants.NOTE_MIN;
		}
		return plugNote;
	}
	/**
	 * Because of special notes like KEY_OFF, NOTE_CUT, NOTE_FADE this is *not*
	 * !hasNoNote()
	 * @since 11.03.2024
	 * @param element
	 * @return true, if the current Element has a note
	 */
	protected boolean hasNewNote()
	{
		return currentElement!=null && (currentElement.getPeriod()>ModConstants.NO_NOTE || currentElement.getNoteIndex()>ModConstants.NO_NOTE);
	}
	/**
	 * Because of special notes like KEY_OFF, NOTE_CUT, NOTE_FADE this is *not*
	 * !hasNewNote()
	 * @since 15.03.2024
	 * @param element
	 * @return true, if the current Element has no note
	 */
	protected boolean hasNoNote()
	{
		return currentElement!=null && (currentElement.getPeriod()==ModConstants.NO_NOTE && currentElement.getNoteIndex()==ModConstants.NO_NOTE);
	}
	/**
	 * @return some infos
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return  "Channel: "+channelNumber+(isNNA?"(NNA) ":" ")+
				((currentElement!=null)?currentElement.toString():"NONE")+
				" Note: "+ModConstants.getNoteNameForIndex(assignedNoteIndex)+
				" Volume: "+currentInstrumentVolume+
				" Instrument: \""+((assignedInstrument!=null)?assignedInstrument.toString():"NONE")+
				"\" Sample: \"" + ((assignedSample!=null)?assignedSample.toString():"NONE")+'\"';
	}
}
