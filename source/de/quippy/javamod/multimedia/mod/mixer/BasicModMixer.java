/*
 * @(#) BasicModMixer.java
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

import java.util.ArrayList;
import java.util.Random;

import de.quippy.javamod.multimedia.mod.ModConstants;
import de.quippy.javamod.multimedia.mod.gui.ModUpdateListener;
import de.quippy.javamod.multimedia.mod.gui.ModUpdateListener.PeekInformation;
import de.quippy.javamod.multimedia.mod.gui.ModUpdateListener.PositionInformation;
import de.quippy.javamod.multimedia.mod.gui.ModUpdateListener.StatusInformation;
import de.quippy.javamod.multimedia.mod.loader.Module;
import de.quippy.javamod.multimedia.mod.loader.instrument.Envelope;
import de.quippy.javamod.multimedia.mod.loader.instrument.Instrument;
import de.quippy.javamod.multimedia.mod.loader.instrument.Sample;
import de.quippy.javamod.multimedia.mod.loader.pattern.Pattern;
import de.quippy.javamod.multimedia.mod.loader.pattern.PatternElement;
import de.quippy.javamod.multimedia.mod.loader.pattern.PatternRow;

/**
 * @author Daniel Becker
 * @since 30.04.2006
 */
public abstract class BasicModMixer
{
	public class ChannelMemory
	{
		public int channelNumber;
		public boolean muted, wasITforced;
		public boolean isNNA;
		
		public PatternElement currentElement;
		
		// These currents* are a fresh copy from the current pattern. Only needed as interim memory for NNA, PatternDelay and NoteDelay
		public int currentAssignedNotePeriod, currentAssignedNoteIndex, currentEffekt, currentEffektParam, currentVolumeEffekt, currentVolumeEffektOp, currentAssignedInstrumentIndex;
		public Instrument currentAssignedInstrument;

		// The assigned* are those from the pattern, when ready to be copied and processed
		// for instance: if no instrument was set in pattern, current* / assigend* instrument is used (as the last instrument set)
		public int assignedNotePeriod, assignedNoteIndex, effekt, effektParam, volumeEffekt, volumeEffektOp, assignedInstrumentIndex;
		public Instrument assignedInstrument;
		
		// currentNoteperiod and these down here are then the values to handle with
		public int currentNotePeriod, currentFinetuneFrequency;
		public int currentNotePeriodSet; // used to save the current note period set with "setNewPlayerTuningFor"
		public int currentFineTune, currentTranspose;
		public Sample currentSample;
		public int currentTuning, currentTuningPos, currentSamplePos, interpolationMagic, loopCounter;
		public boolean isForwardDirection;
		public int volEnvPos, panEnvPos, pitchEnvPos;
		public boolean instrumentFinished, keyOff, noteCut, noteFade;
		public int tempNNAAction, tempVolEnv, tempPanEnv, tempPitchEnv;
		public int keyOffCounter;
		
		public int currentVolume, currentInstrumentVolume, channelVolume, fadeOutVolume, panning, currentInstrumentPanning, actVolumeLeft, actVolumeRight;
		public int actRampVolLeft, actRampVolRight, deltaVolLeft, deltaVolRight;
		public boolean disableVolRamp;
		public int channelVolumSlideValue;
		
		// only needed for display
		public long bigSampleLeft, bigSampleRight;
		
		public boolean doSurround;
		
		public int autoVibratoTablePos, autoVibratoAmplitude;

		// Midi Macros
		public int activeMidiMacro;
		public int lastZxxParam;
		
		// Resonance Filter
		public boolean filterOn;
		public int filterMode;
		public int resonance, cutOff;
		public int swingVolume, swingPanning, swingResonance, swingCutOff;
		public long filter_A0, filter_B0, filter_B1, filter_HP;
		public long filter_Y1, filter_Y2, filter_Y3, filter_Y4;
		
		// The effect memories
		public boolean glissando;
		public int arpegioIndex, arpegioNote[], arpegioParam;
		public int portaStepUp, portaStepUpEnd, portaStepDown, portaStepDownEnd;
		public int finePortaUp, finePortaDown, finePortaUpEx, finePortaDownEx; 
		public int portaNoteStep, portaTargetNotePeriod;
		public int volumSlideValue, globalVolumSlideValue;
		public int panningSlideValue;
		public int vibratoTablePos, vibratoStep, vibratoAmplitude, vibratoType;
		public boolean vibratoOn, vibratoNoRetrig;
		public int tremoloTablePos, tremoloStep, tremoloAmplitude, tremoloType;
		public boolean tremoloOn, tremoloNoRetrig;
		public int panbrelloTablePos, panbrelloStep, panbrelloAmplitude, panbrelloType, panbrelloRandomMemory; // panbrelloRandomMemory only for IT
		public boolean panbrelloOn, panbrelloNoRetrig;
		public int tremorOntime, tremorOfftime, tremorOntimeSet, tremorOfftimeSet;
		public boolean tremorWasActive;
		public int retrigCount, retrigMemo, retrigVolSlide;
		public int sampleOffset, highSampleOffset;
		public int S_Effect_Memory; // IT specific S00 Memory
		public int IT_EFG; // IT specific: linked memory
		
		public int jumpLoopPatternRow, jumpLoopRepeatCount, lastJumpCounterRow;
		public boolean jumpLoopPositionSet;

		public int noteDelayCount, noteCutCount;

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
			disableVolRamp = false;
			fadeOutVolume = ModConstants.MAXFADEOUTVOLUME;
			
			muted = false; wasITforced = false;
			assignedNotePeriod = currentNotePeriod = currentNotePeriodSet =    
			currentFinetuneFrequency = currentFineTune = 0;
			currentTuning = currentTuningPos = currentSamplePos = interpolationMagic = loopCounter = 0;
			isForwardDirection = true; 
			instrumentFinished = true;
			keyOffCounter = -1;
			noteCut = keyOff = noteFade = false;
			tempNNAAction = tempVolEnv = tempPanEnv = tempPitchEnv = -1;
	
			volEnvPos = panEnvPos = pitchEnvPos = -1;
			swingVolume = swingPanning = swingResonance = swingCutOff = 0;
			
			arpegioIndex = noteDelayCount = noteCutCount = -1;
			arpegioParam = 0;
			arpegioNote = new int[3];
			portaStepUp = portaStepDown = portaStepUpEnd = portaStepDownEnd = 0; 
			finePortaDown = finePortaUp = 0; 
			finePortaDownEx = finePortaUpEx = 0; 
			portaNoteStep = volumSlideValue = globalVolumSlideValue = 0;
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
			retrigCount = retrigMemo = retrigVolSlide = sampleOffset = highSampleOffset = 0;
			S_Effect_Memory = IT_EFG = 0;
			
			doSurround = false;
			
			activeMidiMacro = 0;
			lastZxxParam = 0;
			
			filterOn = false;
			filterMode = 0;
			resonance = 0;
			cutOff = 0x7F;
			filter_A0 = filter_B0 = filter_B1 = filter_HP = 0;
			filter_Y1 = filter_Y2 = filter_Y3 = filter_Y4 = 0;
			
			jumpLoopPositionSet = false;
			patternJumpPatternIndex = jumpLoopPatternRow = jumpLoopRepeatCount = lastJumpCounterRow = -1;
		}
		/**
		 * Every possible way to create a 1:1 copy of ChannelMemory for NNA
		 * failed (Clone, Serializable, Reflection)
		 * However this method is now generated via reflection by
		 * "de.quippy.javamod.test.TableGenerator.java"
		 * @since 11.06.2020
		 * @param fromMe
		 */
		protected void setUpFrom(ChannelMemory fromMe)
		{
			channelNumber = fromMe.channelNumber;
			muted = fromMe.muted;
//			isNNA = fromMe.isNNA; // Effect memory - not for NNA
//			currentElement = fromMe.currentElement; // Effect memory - not for NNA
//			currentAssignedNotePeriod = fromMe.currentAssignedNotePeriod; // Effect memory - not for NNA
//			currentAssignedNoteIndex = fromMe.currentAssignedNoteIndex; // Effect memory - not for NNA
//			currentEffekt = fromMe.currentEffekt; // Effect memory - not for NNA
//			currentEffektParam = fromMe.currentEffektParam; // Effect memory - not for NNA
//			currentVolumeEffekt = fromMe.currentVolumeEffekt; // Effect memory - not for NNA
//			currentVolumeEffektOp = fromMe.currentVolumeEffektOp; // Effect memory - not for NNA
//			currentAssignedInstrumentIndex = fromMe.currentAssignedInstrumentIndex; // Effect memory - not for NNA
//			currentAssignedInstrument = fromMe.currentAssignedInstrument; // Effect memory - not for NNA
//			assignedNotePeriod = fromMe.assignedNotePeriod; // Effect memory - not for NNA
//			assignedNoteIndex = fromMe.assignedNoteIndex; // Effect memory - not for NNA
//			effekt = fromMe.effekt; // Effect memory - not for NNA
//			effektParam = fromMe.effektParam; // Effect memory - not for NNA
//			volumeEffekt = fromMe.volumeEffekt; // Effect memory - not for NNA
//			volumeEffektOp = fromMe.volumeEffektOp; // Effect memory - not for NNA
			assignedInstrumentIndex = fromMe.assignedInstrumentIndex;
			assignedInstrument = fromMe.assignedInstrument;
			currentNotePeriod = fromMe.currentNotePeriod;
			currentNotePeriodSet = fromMe.currentNotePeriodSet;
			currentFinetuneFrequency = fromMe.currentFinetuneFrequency;
			currentFineTune = fromMe.currentFineTune;
			currentTranspose = fromMe.currentTranspose;
			currentSample = fromMe.currentSample;
			currentTuning = fromMe.currentTuning;
			currentTuningPos = fromMe.currentTuningPos;
			currentSamplePos = fromMe.currentSamplePos;
			interpolationMagic = fromMe.interpolationMagic;
			loopCounter = fromMe.loopCounter;
			isForwardDirection = fromMe.isForwardDirection;
			volEnvPos = fromMe.volEnvPos;
			panEnvPos = fromMe.panEnvPos;
			pitchEnvPos = fromMe.pitchEnvPos;
			tempVolEnv = fromMe.tempVolEnv;
			tempPanEnv = fromMe.tempPanEnv;
			tempPitchEnv = fromMe.tempPitchEnv;
			instrumentFinished = fromMe.instrumentFinished;
			keyOff = fromMe.keyOff;
			noteCut = fromMe.noteCut;
			noteFade = fromMe.noteFade;
			tempNNAAction = fromMe.tempNNAAction;
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
//			glissando = fromMe.glissando; // Effect memory - not for NNA
//			arpegioIndex = fromMe.arpegioIndex; // Effect memory - not for NNA
//			for (int i=0; i<arpegioNote.length; i++) arpegioNote[i] = fromMe.arpegioNote[i]; // Effect memory - not for NNA
//			arpegioParam = fromMe.arpegioParam; // Effect memory - not for NNA
//			portaStepUp = fromMe.portaStepUp; // Effect memory - not for NNA
//			portaStepUpEnd = fromMe.portaStepUpEnd; // Effect memory - not for NNA
//			portaStepDown = fromMe.portaStepDown; // Effect memory - not for NNA
//			portaStepDownEnd = fromMe.portaStepDownEnd; // Effect memory - not for NNA
//			finePortaUp = fromMe.finePortaUp; // Effect memory - not for NNA
//			finePortaDown = fromMe.finePortaDown; // Effect memory - not for NNA
//			finePortaUpEx = fromMe.finePortaUpEx; // Effect memory - not for NNA
//			finePortaDownEx = fromMe.finePortaDownEx; // Effect memory - not for NNA
//			portaNoteStep = fromMe.portaNoteStep; // Effect memory - not for NNA
//			portaTargetNotePeriod = fromMe.portaTargetNotePeriod; // Effect memory - not for NNA
//			volumSlideValue = fromMe.volumSlideValue; // Effect memory - not for NNA
//			globalVolumSlideValue = fromMe.globalVolumSlideValue; // Effect memory - not for NNA
//			panningSlideValue = fromMe.panningSlideValue; // Effect memory - not for NNA
//			vibratoTablePos = fromMe.vibratoTablePos; // Effect memory - not for NNA
//			vibratoStep = fromMe.vibratoStep; // Effect memory - not for NNA
//			vibratoAmplitude = fromMe.vibratoAmplitude; // Effect memory - not for NNA
//			vibratoType = fromMe.vibratoType; // Effect memory - not for NNA
//			vibratoOn = fromMe.vibratoOn; // Effect memory - not for NNA
//			vibratoNoRetrig = fromMe.vibratoNoRetrig; // Effect memory - not for NNA
//			tremoloTablePos = fromMe.tremoloTablePos; // Effect memory - not for NNA
//			tremoloStep = fromMe.tremoloStep; // Effect memory - not for NNA
//			tremoloAmplitude = fromMe.tremoloAmplitude; // Effect memory - not for NNA
//			tremoloType = fromMe.tremoloType; // Effect memory - not for NNA
//			tremoloOn = fromMe.tremoloOn; // Effect memory - not for NNA
//			tremoloNoRetrig = fromMe.tremoloNoRetrig; // Effect memory - not for NNA
//			panbrelloTablePos = fromMe.panbrelloTablePos; // Effect memory - not for NNA
//			panbrelloStep = fromMe.panbrelloStep; // Effect memory - not for NNA
//			panbrelloAmplitude = fromMe.panbrelloAmplitude; // Effect memory - not for NNA
//			panbrelloType = fromMe.panbrelloType; // Effect memory - not for NNA
//			panbrelloOn = fromMe.panbrelloOn; // Effect memory - not for NNA
//			panbrelloNoRetrig = fromMe.panbrelloNoRetrig; // Effect memory - not for NNA
//			tremorOntime = fromMe.tremorOntime; // Effect memory - not for NNA
//			tremorOfftime = fromMe.tremorOfftime; // Effect memory - not for NNA
//			tremorOntimeSet = fromMe.tremorOntimeSet; // Effect memory - not for NNA
//			tremorOfftimeSet = fromMe.tremorOfftimeSet; // Effect memory - not for NNA
//			tremorWasActive = fromMe.tremorWasActive; // Effect memory - not for NNA
//			retrigCount = fromMe.retrigCount; // Effect memory - not for NNA
//			retrigMemo = fromMe.retrigMemo; // Effect memory - not for NNA
//			retrigVolSlide = fromMe.retrigVolSlide; // Effect memory - not for NNA
//			sampleOffset = fromMe.sampleOffset; // Effect memory - not for NNA
//			highSampleOffset = fromMe.highSampleOffset; // Effect memory - not for NNA
//			S_Effect_Memory = fromMe.S_Effect_Memory; // Effect memory - not for NNA
//			jumpLoopPatternRow = fromMe.jumpLoopPatternRow; // Effect memory - not for NNA
//			jumpLoopRepeatCount = fromMe.jumpLoopRepeatCount; // Effect memory - not for NNA
//			lastJumpCounterRow = fromMe.lastJumpCounterRow; // Effect memory - not for NNA
//			jumpLoopPositionSet = fromMe.jumpLoopPositionSet; // Effect memory - not for NNA
//			noteDelayCount = fromMe.noteDelayCount; // Effect memory - not for NNA
//			noteCutCount = fromMe.noteCutCount; // Effect memory - not for NNA
		}
		/**
		 * @return some infos
		 * @see java.lang.Object#toString()
		 */
		public String toString()
		{
			return "Channel: " + channelNumber + (isNNA?"(NNA) ":" ")+"Note: " + ModConstants.getNoteNameForIndex(assignedNoteIndex) + " Volume: " + currentInstrumentVolume + " Sample: " + ((assignedInstrument!=null)?assignedInstrument.toString():"NONE");
		}
	}

	protected ChannelMemory [] channelMemory;
	protected int maxNNAChannels; // configured value: the complete amount of mixing channels
	protected int maxChannels;
	
	protected Random swinger;
	
	// out sample buffer for two stereo samples
	private final long samples[] = new long[2];

	// Global FilterMode:
	protected boolean globalFilterMode;
	
	// Player specifics
	protected int frequencyTableType; // XM and IT Mods support this! Look at the constants

	protected int currentTempo, currentBPM;
	protected int globalTuning;
	protected int globalVolume, masterVolume, extraAttenuation;
	protected boolean useGlobalPreAmp, useSoftPanning;
	protected int currentTick, currentRow, currentArrangement, currentPatternIndex;
	protected int samplePerTicks;
	protected int leftOverSamplesPerTick; // the amount of data left to finish mixing a tick
	protected long samplesMixed; // the whole amount of samples mixed - as a time index for events

	protected int patternDelayCount, patternTicksDelayCount;
	protected Pattern currentPattern;
	protected int volRampLen;
	
	protected int patternBreakRowIndex; // -1== no pattern break, otherwise >=0
	protected int patternBreakJumpPatternIndex; // -1== no pattern pos jump
	protected int patternJumpPatternIndex;
	
	protected final Module mod;
	protected int sampleRate;
	protected int doISP; // 0: no ISP; 1:linear; 2:Cubic Spline; 3:Windowed FIR
	protected int doNoLoops; // activates infinite loop recognition
	
	protected boolean modFinished;
	//protected boolean isFastForward;
	
	// FadeOut
	protected boolean doLoopingGlobalFadeout; // means we are in a loop condition and do a fade out now. 0: deactivated, 1: fade out, 2: just ignore loop
	protected int loopingFadeOutValue;
	
	// RAMP volume interweaving
	protected long [] vRampL;
	protected long [] vRampR;
	protected long [] nvRampL;
	protected long [] nvRampR;
	
	// The listeners for update events - so far only one known off
	private ArrayList<ModUpdateListener> listeners;
	private boolean fireUpdates = false;
	
	protected boolean isFastTracker, isScreamTracker, isMOD, isXM, /*isSTM,*/ isS3M, isIT;
	

	/**
	 * Constructor for BasicModMixer
	 */
	public BasicModMixer(final Module mod, final int sampleRate, final int doISP, final int doNoLoops, final int maxNNAChannels)
	{
		super();
		this.mod = mod;
		this.sampleRate = sampleRate;
		this.doISP = doISP;
		this.doNoLoops = doNoLoops;
		this.maxNNAChannels = maxNNAChannels;

		vRampL = new long [ModConstants.VOL_RAMP_LEN];
		vRampR = new long [ModConstants.VOL_RAMP_LEN];
		nvRampL = new long [ModConstants.VOL_RAMP_LEN];
		nvRampR = new long [ModConstants.VOL_RAMP_LEN];
		
		listeners = new ArrayList<ModUpdateListener>();
		
		initializeMixer(false); // do not inform listeners, as there are no listeners registered yet
	}
	/**
	 * BE SHURE TO STOP PLAYBACK! Changing this during playback may (will!)
	 * cause crappy playback!
	 * @since 09.07.2006
	 * @param newSampleRate
	 */
	public void changeSampleRate(final int newSampleRate)
	{
		this.sampleRate = newSampleRate;
		this.samplePerTicks = calculateSamplesPerTick();
		calculateGlobalTuning();
		calculateVolRampLen();
		for (int c=0; c<maxChannels; c++) setNewPlayerTuningFor(channelMemory[c]);
	}
	/**
	 * Changes the interpolation routine. This can be done at any time
	 * @since 09.07.2006
	 * @param newISP
	 */
	public void changeISP(final int newISP)
	{
		this.doISP = newISP;
	}
	/**
	 * Changes the interpolation routine. This can be done at any time
	 * @since 09.07.2006
	 * @param newISP
	 */
	public void changeDoNoLoops(final int newDoNoLoops)
	{
		this.doNoLoops = newDoNoLoops;
	}
	/**
	 * BE SHURE TO STOP PLAYBACK! Changing this during playback may (will!)
	 * cause crappy playback!
	 * @since 23.06.2020
	 * @param newMaxNNAChannels
	 */
	public void changeMaxNNAChannels(final int newMaxNNAChannels)
	{
		maxNNAChannels = newMaxNNAChannels;
		final int nChannels = mod.getNChannels();
		int newMaxChannels = nChannels;
		if (isIT)
			newMaxChannels += maxNNAChannels;
		if (newMaxChannels!=maxChannels)
		{
			ChannelMemory [] newChannelMemory = new ChannelMemory[newMaxChannels];
			for (int c=0; c<newMaxChannels; c++)
			{
				if (c<maxChannels)
				{
					newChannelMemory[c] = channelMemory[c];
				}
				else
				{
					newChannelMemory[c] = new ChannelMemory();
					newChannelMemory[c].isNNA = true; // must be the default in this case...
				}
			}
			channelMemory = newChannelMemory;
			maxChannels = newMaxChannels;
		}
	}
	/**
	 * Do own inits
	 * Especially do the init of the panning depending on ModType
	 * @return
	 */
	protected abstract void initializeMixer(final int channel, final ChannelMemory aktMemo);
	
	/**
	 * Call this first!
	 */
	public void initializeMixer(final boolean rememberMuteStatus)
	{
		// to be a bit faster, we do some pre-calculations
		calculateGlobalTuning();
		
		isFastTracker=(mod.getModType()&ModConstants.MODTYPE_FASTTRACKER)!=0;
		isScreamTracker=(mod.getModType()&ModConstants.MODTYPE_IMPULSETRACKER)!=0;
		isMOD=(mod.getModType()&ModConstants.MODTYPE_MOD)!=0;
		isXM=(mod.getModType()&ModConstants.MODTYPE_XM)!=0;
		//isSTM=(mod.getModType()&ModConstants.MODTYPE_STM)!=0;
		isS3M=(mod.getModType()&ModConstants.MODTYPE_S3M)!=0;
		isIT=(mod.getModType()&ModConstants.MODTYPE_IT)!=0;

		// get Mod specific values
		frequencyTableType = mod.getFrequencyTable();
		currentTempo = mod.getTempo();
		currentBPM = mod.getBPMSpeed();
		globalVolume = mod.getBaseVolume();
		
		globalFilterMode = false; // IT default: every note resets filter to current values set - flattens the filter envelope
		swinger = new Random();

		if ((mod.getModType()&ModConstants.MODTYPE_MPT)==ModConstants.MODTYPE_MPT) // Legacy MPT?
		{
			// Do global Pre-Amp - with legacy ModPlug Tracker this was used...
			// legacy: that is MPT <=1.17RC2
			int channels = mod.getNChannels();
			if (channels<1) channels = 1; 
			else 
			if (channels>31) channels = 31;

			// (Open)MPT uses 0x100 as maxBaseVolume, so original 0x80 maxBaseVolume
			// of IT, which JavaMod uses, needs to be doubled
			int realMasterVolume = mod.getBaseVolume()<<1;
			if (realMasterVolume > 0x80)
			{
				//Attenuate global pre-amp depending on number of channels
				realMasterVolume = 0x80 + (((realMasterVolume - 0x80) * (channels + 4)) >> 4);
			}
			masterVolume = (realMasterVolume * mod.getMixingPreAmp())>>6;
			// no DSP automatic gain control (AGC) switch with JavaMod, so only PreAmp version:
			masterVolume = (masterVolume << 7) / ModConstants.PreAmpTable[channels>>1];

			extraAttenuation = 4; // set extraAttenuation
			useGlobalPreAmp = true; // with preAmp PreAmpShift is 7, otherwise 8
			useSoftPanning = false;
		}
		else
		if ((mod.getModType()&ModConstants.MODTYPE_OMPT)==ModConstants.MODTYPE_OMPT) // Open Modplug Tracker?
		{
			masterVolume = mod.getMixingPreAmp();
			extraAttenuation = 0;
			useGlobalPreAmp = false;
			useSoftPanning = true;
		}
//		else // Open ModPlug does it like this but we will not - too loud. FT2 with default AMP x4 is even more silent
//		if (isXM)
//		{
//			masterVolume = mod.getMixingPreAmp();
//			extraAttenuation = 0;
//			useGlobalPreAmp = false;
//			useSoftPanning = false;
//		}
		else // default ProTracker, FT2, s3m, ...
		{
			masterVolume = mod.getMixingPreAmp();
			extraAttenuation = 1;
			useGlobalPreAmp = false;
			useSoftPanning = false;
		}
		
		leftOverSamplesPerTick = samplePerTicks = calculateSamplesPerTick();
		samplesMixed = 0;
		
		currentTick = currentArrangement = currentRow = 0;
		patternDelayCount = patternTicksDelayCount = -1;
		currentArrangement = 0;
		currentPatternIndex = mod.getArrangement()[currentArrangement];
		currentPattern = mod.getPatternContainer().getPattern(currentPatternIndex);
		
		patternJumpPatternIndex = patternBreakRowIndex = patternBreakJumpPatternIndex = -1;
		
		modFinished = false;
		
		calculateVolRampLen();
		
		// Reset all rows played to false
		mod.resetLoopRecognition();

		// Reset FadeOut
		doLoopingGlobalFadeout = false;
		loopingFadeOutValue = ModConstants.MAXFADEOUTVOLUME;

		// initialize every used channel
		final int nChannels = mod.getNChannels();
		maxChannels = nChannels;
		if (isIT)
			maxChannels += maxNNAChannels;
		
		// This is only for seeking. We remember the mute status of channels
		// as this will get reseted when all channels get recreated
		boolean [] muteStatus = null;
		if (rememberMuteStatus)
		{
			muteStatus = new boolean[maxChannels];
			for (int c=0; c<maxChannels; c++)
			{
				final ChannelMemory aktMemo = channelMemory[c];
				muteStatus[c] = aktMemo.muted;
			}
		}
		channelMemory = new ChannelMemory[maxChannels];
		for (int c=0; c<maxChannels; c++)
		{
			final ChannelMemory aktMemo = channelMemory[c] = new ChannelMemory();
			if (c<nChannels)
			{
				aktMemo.isNNA = false;
				aktMemo.channelNumber = c;
				aktMemo.currentInstrumentPanning = aktMemo.panning = mod.getPanningValue(c);
				aktMemo.channelVolume = mod.getChannelVolume(c);
				initializeMixer(c, aktMemo); // additional Mod specific initializations
			}
			else
			{
				aktMemo.isNNA = true;
				aktMemo.channelNumber = -1;
			}
		}
		// and reset the mute status again
		if (muteStatus!=null)
		{
			for (int c=0; c<maxChannels; c++)
			{
				final ChannelMemory aktMemo = channelMemory[c];
				aktMemo.muted = muteStatus[c];
			}
		}
	}
	/**
	 * Does only a forward seek, so starts from the beginning
	 * @since 25.07.2020
	 */
	public long seek(final long milliseconds)
	{
		long fullLength = 0;
		final boolean fireUpdateStatus = getFireUpdates();
		try
		{
			setFireUpdates(false);
			initializeMixer(true);
			long currentMilliseconds = 0;
			final long stopAt = 60L*60L*sampleRate; // Just in case...
			boolean finished = false;
			while (fullLength<stopAt && currentMilliseconds<milliseconds && !finished)
			{
				fullLength += samplePerTicks;
				currentMilliseconds = fullLength * 1000L / (long)sampleRate;
				finished = doRowAndTickEvents();
			}
			// Silence all and everything to avoid clicks and arbitrary sounds...
			for (int c=0; c<maxChannels; c++)
			{
				final ChannelMemory aktMemo = channelMemory[c];
				aktMemo.actVolumeLeft = aktMemo.actVolumeRight = aktMemo.currentVolume = 
				aktMemo.actRampVolLeft = aktMemo.actRampVolRight = 0;
			}
		}
		finally
		{
			setFireUpdates(fireUpdateStatus);
		}
		return fullLength;
	}
	/**
	 * @since 25.07.2020
	 * @return
	 */
	public synchronized long getLengthInMilliseconds()
	{
		// do we need to measure it or do we already have a length?
		if (mod.getLengthInMilliseconds()==-1)
		{
			final boolean fireUpdateStatus = getFireUpdates();
			try
			{
				setFireUpdates(false);
				int oldDoNoLoops = doNoLoops;
				int oldSampleRate = sampleRate;
				changeDoNoLoops(ModConstants.PLAYER_LOOP_FADEOUT);
				changeSampleRate(44100);
		
				initializeMixer(false);
				long fullLength = 0;
				boolean finished = false;
				while (fullLength < 60L*60L*44100L && !finished)
				{
					fullLength += samplePerTicks;
					finished = doRowAndTickEvents();
				}
				changeDoNoLoops(oldDoNoLoops);
				changeSampleRate(oldSampleRate);
				initializeMixer(false);
				mod.setLengthInMilliseconds(fullLength * 1000L / 44100L);
			}
			finally
			{
				setFireUpdates(fireUpdateStatus);
			}
		}
		return mod.getLengthInMilliseconds();
	}
	/**
	 * Will create a long representing current
	 * positions. Form is as follows:<br>
	 * 0x1234 5678 9ABC DEF0:<br>
	 *   1234: currentArrangement position (>>48)<br>
	 *   5678: current Pattern Number (>>32)<br>
	 *   9ABC: current Row (>>16)<br>
	 *   DEF0: current tick<br>
	 * @since 30.03.2010
	 * @return
	 */
	public long getCurrentPatternPosition()
	{
		return ((long)(currentArrangement&0xFFFF)<<48) | ((long)(currentPatternIndex&0xFFFF)<<32) | ((long)(currentRow&0xFFFF)<<16) | ((long)(currentTempo - currentTick)&0xFFFF);
	}
	/**
	 * Will return all channels, that are active for rendering
	 * Also silenced channels will be counted, as the playback is
	 * still processing the active instrument / sample
	 * @since 30.03.2010
	 * @return
	 */
	public int getCurrentUsedChannels()
	{
		int result = 0;
		for (int i=0; i<maxChannels; i++)
		{
			final ChannelMemory aktMemo = channelMemory[i]; 
			if (isChannelActive(aktMemo)) result++;
		}
		return result;
	}
	/**
	 * @since 11.11.2023
	 * @return true, if mod playback is finished
	 */
	public boolean getModFinished()
	{
		return modFinished;
	}
	public int getISP()
	{
		return doISP;
	}
	/**
	 * Normally you would use the formula (25*samplerate)/(bpm*10)
	 * which is (2.5*sampleRate)/bpm. But (2.5 * sampleRate) is the same
	 * as (sampleRate*2) + (sampleRate/2)
	 * @param currentBPM
	 * @return
	 */
	protected int calculateSamplesPerTick()
	{
		return ((sampleRate<<1) + (sampleRate>>1)) / currentBPM;
	}
	/**
	 * The size of Volumeramping we intend to use
	 */
	private void calculateVolRampLen()
	{
		if (isXM)
			volRampLen = sampleRate * 5 / 1000; // FT2 XMs have smooth VolRamp of 5ms 
		else
			volRampLen = sampleRate * ModConstants.VOLRAMPLEN_YS / 100000; // 950yS
		if (volRampLen < 8) volRampLen = 8; // minimum of 8 samples
	}
	/**
	 * For faster tuning calculations, this is precalced 
	 */
	protected void calculateGlobalTuning()
	{
		this.globalTuning = (int)((((((long)ModConstants.BASEPERIOD)<<ModConstants.PERIOD_SHIFT) * ((long)ModConstants.BASEFREQUENCY))<<ModConstants.SHIFT) / ((long)sampleRate));
	}
	/**
	 * Retrieves a period value (see ModConstants.noteValues) shifted by 4 (*16)
	 * XM_LINEAR_TABLE and XM_AMIGA_TABLE is for XM-Mods,
	 * AMIGA_TABLE is for ProTrackerMods only (XM_AMIGA_TABLE is about the same though)
	 * With Mods the AMIGA_TABLE, IT_AMIGA_TABLE and XM_AMIGA_TABLE result in 
	 * the approximate same values, but to be purly compatible and correct,
	 * we use the protracker fintune period tables!
	 * The IT_AMIGA_TABLE is for STM, S3M and IT...
	 * Be careful: if XM_* is used, we expect a noteIndex (0..119), no period!
	 * @param aktMemo
	 * @param period or noteIndex
	 * @return
	 */
	protected int getFineTunePeriod(final ChannelMemory aktMemo, final int period)
	{
		int noteIndex = period - 1; // Period was both, a mod period or xm/it noteIndex - this changed (look at default!)
		switch (frequencyTableType)
		{
			case ModConstants.STM_S3M_TABLE:
			case ModConstants.IT_AMIGA_TABLE:
				final int s3mNote=ModConstants.FreqS3MTable[noteIndex%12];
				final int s3mOctave=noteIndex/12;
				return (int) ((long) ModConstants.BASEFREQUENCY * ((long)s3mNote << 7) / ((long) aktMemo.currentFinetuneFrequency << (s3mOctave)));
			
			case ModConstants.IT_LINEAR_TABLE:
				return (ModConstants.FreqS3MTable[noteIndex%12] << 7) >> (noteIndex/12);

			case ModConstants.AMIGA_TABLE:
				return (period<25)?0:ModConstants.protracker_fineTunedPeriods[(aktMemo.currentFineTune>>ModConstants.PERIOD_SHIFT)+8][period-25]; // Amiga has less octaves!
			
			case ModConstants.XM_AMIGA_TABLE:
				int fineTune=aktMemo.currentFineTune;
				int rFine=fineTune>>ModConstants.PERIOD_SHIFT;

				final int note=((noteIndex%12)<<3)+8; // !negativ finetune values! -8..+7 Therefore add 8
				final int octave=noteIndex/12;
				
				int logIndex = note + rFine; 
				if (logIndex<0) logIndex=0; else if (logIndex>103) logIndex=103;
				int v1=ModConstants.logtab[logIndex];
				if (fineTune<0)
				{
					rFine--;
					fineTune = -fineTune;
				} 
				else 
					rFine++;
				
				logIndex = note + rFine;
				if (logIndex<0) logIndex=0; else if (logIndex>103) logIndex=103;
				final int v2=ModConstants.logtab[logIndex];
				rFine = fineTune & 0x0F;
				return ((v1*(16-rFine)) + (v2*rFine)) >> (octave+4);

			case ModConstants.XM_LINEAR_TABLE:
				final int p = 7680 - (noteIndex<<6) - (aktMemo.currentFineTune>>1);
				if (p<1) return (1<<2);
				return p<<2;

			default: // Period is not a noteindex - this will never happen, but I once used it with protracker mods
				return (int)((long)ModConstants.BASEFREQUENCY*(long)period/(long)aktMemo.currentFinetuneFrequency);
		}
	}
	/**
	 * Calls getFineTunePeriod(ChannelMemory, int Period) with the actual Period assigned.
	 * All Effects changing the period need to call this
	 * @param aktMemo
	 * @return
	 */
	protected int getFineTunePeriod(final ChannelMemory aktMemo)
	{
		if ((frequencyTableType & (ModConstants.AMIGA_TABLE | ModConstants.XM_AMIGA_TABLE | ModConstants.XM_LINEAR_TABLE))!=0)
			return (aktMemo.assignedNoteIndex==0)?0:getFineTunePeriod(aktMemo, aktMemo.assignedNoteIndex + aktMemo.currentTranspose);
		else
		if ((frequencyTableType & (ModConstants.IT_LINEAR_TABLE | ModConstants.IT_AMIGA_TABLE | ModConstants.STM_S3M_TABLE))!=0) 
			return (aktMemo.assignedNoteIndex==0)?0:getFineTunePeriod(aktMemo, aktMemo.assignedNoteIndex);
		else
			return 0;
	}
	/**
	 * This Method now takes the current Period (e.g. 856<<ModConstants.PERIOD_SHIFT) and calculates 
	 * the playerTuning to be used. I.e. a value like 2, which means every second sample in the
	 * current instrument is to be played. A value of 0.5 means, every sample is played twice.
	 * As we use int-values, this again is shiftet. 
	 * @param aktMemo
	 * @param newPeriod
	 */
	protected void setNewPlayerTuningFor(final ChannelMemory aktMemo, final int newPeriod)
	{
		aktMemo.currentNotePeriodSet = newPeriod;

		if (newPeriod<=0)
			aktMemo.currentTuning = 0;
		else
		if (frequencyTableType==ModConstants.XM_LINEAR_TABLE)
		{
			final int linear_period_value = newPeriod>>2;
			final int newFrequency = ModConstants.lintab[linear_period_value % 768] >> (linear_period_value / 768);
			aktMemo.currentTuning = (int)(((long)newFrequency<<ModConstants.SHIFT) / sampleRate);
		}
		else
		if (frequencyTableType==ModConstants.IT_LINEAR_TABLE)
		{
			final long itTuning = (((((long)ModConstants.BASEPERIOD)<<ModConstants.PERIOD_SHIFT) * ((long)aktMemo.currentFinetuneFrequency))<<ModConstants.SHIFT) / ((long)sampleRate);
			aktMemo.currentTuning = (int)(itTuning / (long)newPeriod); 
		}
		else
			aktMemo.currentTuning = globalTuning / newPeriod; // in globalTuning, all constant values are already calculated. (see above)
	}
	/**
	 * Set the current tuning for the player
	 * @param aktMemo
	 */
	protected void setNewPlayerTuningFor(final ChannelMemory aktMemo)
	{
		setNewPlayerTuningFor(aktMemo, aktMemo.currentNotePeriod);
		// save for IT Arpeggios. Must be done here, not above, as above
		// service is used when not changed permanently through currentNotePeriod!
		if (isIT) aktMemo.arpegioNote[0] = aktMemo.currentNotePeriod;
	}
	/**
	 * Get the period of the nearest halftone
	 * @param period
	 * @return
	 */
	protected int getRoundedPeriod(final ChannelMemory aktMemo, final int period)
	{
		if (isMOD)
		{
			final int i = ModConstants.getNoteIndexForPeriod(period);
			if (i>0)
			{
				final int diff1 = ModConstants.noteValues[i-1] - period;
				final int diff2 = period - ModConstants.noteValues[i];
				if (diff1<diff2) return ModConstants.noteValues[i-1];
			}
			return ModConstants.noteValues[i];
		}
		else
		{
			for (int i=1; i<180; i++)
			{
				final int checkPeriod = getFineTunePeriod(aktMemo, i)>>ModConstants.PERIOD_SHIFT;
				if (checkPeriod>0 && checkPeriod<=period)
					return checkPeriod;
			}
			return 0;
		}
	}
	/**
	 * Simple 2-poles resonant filter 
	 * @since 31.03.2010
	 * @param aktMemo
	 * @param bReset
	 * @param flt_modifier
	 */
	protected void setupChannelFilter(ChannelMemory aktMemo, boolean reset, int envModifier)
	{
		final PatternElement element = aktMemo.currentElement;
		// Z7F (plus resonance==0) disables the filter, if set next to a note - otherwise not.
		if (aktMemo.cutOff>=0x7F && aktMemo.resonance==0 && (element!=null && (element.getPeriod()>0 || element.getNoteIndex()>0))) 
			aktMemo.filterOn = false;
		else
		{
			int cutOff = (aktMemo.cutOff & 0x7F) + aktMemo.swingCutOff;
			cutOff = (cutOff * (envModifier + 256)) >> 8;
			if (cutOff<0) cutOff=0; else if (cutOff>0xFF) cutOff=0xFF;
			int resonance = (aktMemo.resonance & 0x7F) + aktMemo.swingResonance;
			if (resonance<0) resonance=0; else if (resonance>0xFF) resonance=0xFF;

			final double fac = (((mod.getSongFlags()&ModConstants.SONG_EXFILTERRANGE)!=0)? (128.0d / (20.0d * 256.0d)) : (128.0d / (24.0d * 256.0d)));
			double frequency = 110.0d * Math.pow(2.0d, (double)cutOff * fac + 0.25d);
			if (frequency < 120d) frequency = 120d;
			if (frequency > 20000d) frequency = 20000d;
			if (frequency > sampleRate>>1) frequency = sampleRate>>1;
			frequency *= 2.0d * Math.PI;
			
			final double dmpFac = ModConstants.ResonanceTable[resonance];
			double e, d;
			if ((mod.getSongFlags()&ModConstants.SONG_EXFILTERRANGE)==0)
			{
				final double r = ((double)sampleRate) / frequency;
				d = dmpFac * r + dmpFac - 1.0d;
				e = r * r;
			}
			else
			{
		        final double d_dmpFac = 2.0d * dmpFac;
				final double r = frequency / ((double)(sampleRate));
		        d = (1.0d - d_dmpFac) * r;
		        if (d > 2.0d) d = 2.0d;
		        d = (d_dmpFac - d) / r;
		        e = 1.0d / (r * r);
			}

			final double fg = 1.0d / (1.0d + d + e);
			final double fb0 = (d + e + e) / (1.0d + d + e);
			final double fb1 = -e / (1.0d + d + e);

			switch(aktMemo.filterMode)
			{
				case ModConstants.FLTMODE_HIGHPASS:
					aktMemo.filter_A0 = (long)((1.0d - fg) * ModConstants.FILTER_PRECISION);
					aktMemo.filter_B0 = (long)(fb0 * ModConstants.FILTER_PRECISION);
					aktMemo.filter_B1 = (long)(fb1 * ModConstants.FILTER_PRECISION);
					aktMemo.filter_HP = -1;
					break;
				case ModConstants.FLTMODE_BANDPASS:
				case ModConstants.FLTMODE_LOWPASS:
				default:
					aktMemo.filter_A0 = (long)(fg * ModConstants.FILTER_PRECISION);
					aktMemo.filter_B0 = (long)(fb0 * ModConstants.FILTER_PRECISION);
					aktMemo.filter_B1 = (long)(fb1 * ModConstants.FILTER_PRECISION);
					aktMemo.filter_HP = 0;
					if (aktMemo.filter_A0 == 0) aktMemo.filter_A0 = 1;
					break;
			}

			if (reset) aktMemo.filter_Y1 = aktMemo.filter_Y2 = aktMemo.filter_Y3 = aktMemo.filter_Y4 = 0;
			
			aktMemo.filterOn = true;
		}
	}
	/**
	 * @since 05.07.2020
	 * @param buffer
	 */
	private void doResonance(final ChannelMemory aktMemo, final long buffer[])
	{
		long sampleAmp = buffer[0]<<ModConstants.FILTER_PREAMP_BITS; // with preAmp
		long fy = ((sampleAmp * aktMemo.filter_A0) + (aktMemo.filter_Y1 * aktMemo.filter_B0) + (aktMemo.filter_Y2 * aktMemo.filter_B1) + ModConstants.HALF_FILTER_PRECISION) >> ModConstants.FILTER_SHIFT_BITS;
		aktMemo.filter_Y2 = aktMemo.filter_Y1;
		aktMemo.filter_Y1 = fy - (sampleAmp & aktMemo.filter_HP);
		if (aktMemo.filter_Y1 < ModConstants.FILTER_CLIP_MIN) aktMemo.filter_Y1 = ModConstants.FILTER_CLIP_MIN;
		else if (aktMemo.filter_Y1 > ModConstants.FILTER_CLIP_MAX) aktMemo.filter_Y1 = ModConstants.FILTER_CLIP_MAX;
		buffer[0] = (fy + (1<<(ModConstants.FILTER_PREAMP_BITS-1))) >> ModConstants.FILTER_PREAMP_BITS;

		sampleAmp = buffer[1]<<ModConstants.FILTER_PREAMP_BITS; // with preAmp
		fy = ((sampleAmp * aktMemo.filter_A0) + (aktMemo.filter_Y3 * aktMemo.filter_B0) + (aktMemo.filter_Y4 * aktMemo.filter_B1) + ModConstants.HALF_FILTER_PRECISION) >> ModConstants.FILTER_SHIFT_BITS;
		aktMemo.filter_Y4 = aktMemo.filter_Y3;
		aktMemo.filter_Y3 = fy - (sampleAmp & aktMemo.filter_HP);
		if (aktMemo.filter_Y3 < ModConstants.FILTER_CLIP_MIN) aktMemo.filter_Y3 = ModConstants.FILTER_CLIP_MIN;
		else if (aktMemo.filter_Y3 > ModConstants.FILTER_CLIP_MAX) aktMemo.filter_Y3 = ModConstants.FILTER_CLIP_MAX;
		buffer[1] = (fy + (1<<(ModConstants.FILTER_PREAMP_BITS-1))) >> ModConstants.FILTER_PREAMP_BITS;
	}
	/**
	 * Do the effects of a row (tick==0). This is mostly the setting of effects
	 * @param aktMemo
	 */
	protected abstract void doRowEffects(final ChannelMemory aktMemo);
	/**
	 * Do the Effects during Ticks (tick!=0)
	 * @param aktMemo
	 */
	protected abstract void doTickEffekts(final ChannelMemory aktMemo);
	/**
	 * Used to process the volume column (tick==0)
	 * @param aktMemo
	 */
	protected abstract void doVolumeColumnRowEffekt(final ChannelMemory aktMemo);
	/**
	 * do the volume column tick effects (tick!=0)
	 * @param aktMemo
	 */
	protected abstract void doVolumeColumnTickEffekt(final ChannelMemory aktMemo);
	/**
	 * Do the autovibrato
	 * @param aktMemo
	 * @param currentSample
	 * @param currentPeriod
	 * @return currentPeriod after alternation
	 */
	protected abstract void doAutoVibratoEffekt(final ChannelMemory aktMemo, final Sample currentSample, int currentPeriod);
	/**
	 * Clear all effekts. Sometimes, if Effekts do continue, they are not stopped.
	 * @param aktMemo
	 * @param nextElement
	 */
	protected abstract void resetAllEffects(final ChannelMemory aktMemo, PatternElement nextElement, boolean forced);
	/**
	 * Returns true, if the Effekt and EffektOp indicate a NoteDelayEffekt
	 * @param effekt
	 * @param effektOp
	 * @return
	 */
	protected abstract boolean isNoteDelayEffekt(final int effekt, final int effektParam);
	/**
	 * Returns true, if the Effekt and EffektOp indicate a PortaToNoteEffekt
	 * @param effekt
	 * @param effektOp
	 * @return
	 */
	protected abstract boolean isPortaToNoteEffekt(final int effekt, final int effektParam, final int volEffekt, final int volEffektParam, final int notePeriod);
	/**
	 * Return true, if the effekt and effektop indicate the sample offset effekt
	 * @since 19.06.2006
	 * @param effekt
	 * @param effektParam
	 * @return
	 */
	protected abstract boolean isSampleOffsetEffekt(final int effekt);
	/**
	 * Returns true, if an NNA-Effekt is set. Than, no default instrument NNA
	 * should be processed.
	 * @since 11.06.2020
	 * @param aktMemo
	 * @return
	 */
	protected abstract boolean isNNAEffekt(final int effekt, final int effektParam);
	/**
	 * if effektParam is 0 an effect memory will be returned - if any
	 * Otherwise will return effektParam
	 * This is basically for S00 IT Memory
	 * @since 28.06.2020
	 * @param effekt
	 * @param effektParam
	 * @return
	 */
	protected abstract int getEffektOpMemory(final ChannelMemory aktMemo, final int effekt, final int effektParam);
	/**
	 * This will return the name of the current effekt referenced by aktMemo
	 * @since 30.11.2023
	 * @param aktMemo
	 * @return
	 */
	public abstract String getEffectName(final int effekt, final int effektParam);
	/**
	 * This will return the name of the current volume column effect
	 * @since 30.11.2023
	 * @param volumeEffekt
	 * @param volumeEffektOp
	 * @return
	 */
	public abstract String getVolEffectName(final int volumeEffekt, final int volumeEffektOp);
	/**
	 * @since 19.06.2020
	 * @param aktMemo
	 */
	protected void initNoteFade(final ChannelMemory aktMemo)
	{
		// do not reactivate a dead channel or reactivate a
		// running noteFade
		if (!aktMemo.noteFade && isChannelActive(aktMemo))
		{
			aktMemo.fadeOutVolume = ModConstants.MAXFADEOUTVOLUME;
			aktMemo.noteFade = true;
		}
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
			if (x==0) return -y;
		}
		else
		if (x!=0)
		{
			if (y==0) return x; 
		}
		// Having OP with x and y set (like 15 or 84) is not supported and does nothing
		return 0;
	}
	/**
	 * Processes the Envelopes
	 * This function now sets the volume - always!!
	 * @since 19.06.2006
	 * @param aktMemo
	 */
	protected void processEnvelopes(ChannelMemory aktMemo)
	{
		int currentVolume = aktMemo.currentVolume << ModConstants.VOLUMESHIFT; // typically it's the sample volume or a volume set 0..64
		int currentPanning = aktMemo.panning;
		int currentPeriod = aktMemo.currentNotePeriodSet;
		
		// The adjustments on the periods will change currentNotePeriodSet
		// That's bad in envelopes, because we want to "add on" here
		// and not on top of our self over and over again
		final int resetPeriodAfterEnvelopes = currentPeriod;

		final Sample sample = aktMemo.currentSample;
		int insVolume = (sample!=null)?sample.globalVolume<<1:ModConstants.MAXGLOBALVOLUME;	// max: 64, but make it equal to instrument volume (0..128)
		Envelope volumeEnv = null;		
		final Instrument currentInstrument = aktMemo.assignedInstrument;
		if (currentInstrument!=null)
		{
			insVolume = (insVolume * currentInstrument.globalVolume) >> 7; // combine sample and instrument volume
		
			volumeEnv = currentInstrument.volumeEnvelope;
			if (volumeEnv!=null)
			{
				final boolean volEnvOn = (aktMemo.tempVolEnv!=-1)?aktMemo.tempVolEnv==1:volumeEnv.on;
				if (volEnvOn)
				{
					aktMemo.volEnvPos = volumeEnv.updatePosition(aktMemo.volEnvPos, aktMemo.keyOff);
					int newVol = volumeEnv.getValueForPosition(aktMemo.volEnvPos); // 0..512
					currentVolume = (currentVolume * newVol) >> 9;
					// With ITs: if Envelope is finished, activate note fade via keyOff - but keep last Volume Envelope Position
					if (isIT && volumeEnv.envelopeFinished(aktMemo.volEnvPos)) aktMemo.keyOff = true;
				}
			}

			// set the panning envelope
			final Envelope panningEnv = currentInstrument.panningEnvelope;
			if (panningEnv!=null)
			{
				final boolean panEnvOn = (aktMemo.tempPanEnv!=-1)?aktMemo.tempPanEnv==1:panningEnv.on;
				if (panEnvOn)
				{
					aktMemo.panEnvPos = panningEnv.updatePosition(aktMemo.panEnvPos, aktMemo.keyOff);
					final int newPanValue = panningEnv.getValueForPosition(aktMemo.panEnvPos) - 256; // result -256..256
					currentPanning += (newPanValue * ((currentPanning >= 128)?(256 - currentPanning):currentPanning)) >> 8;
				}
			}

			// Pitch / Pan separation
			// That is the "piano" effekt: lower keys to the left, higher keys to the right
			// arranged around a center note, that is supposed to be in the middle
			if (currentInstrument.pitchPanSeparation>0 && currentPeriod>0)
			{
				currentPanning += ((currentPeriod - ((currentInstrument.pitchPanCenter + 1)<<ModConstants.PERIOD_SHIFT)) * currentInstrument.pitchPanSeparation) >> 7; // / 8 + >>ModConstants.PERIOD_SHIFT for note period
			}
			
			final Envelope pitchEnv = currentInstrument.pitchEnvelope;
			if (pitchEnv!=null)
			{
				final boolean pitchEnvOn = (aktMemo.tempPitchEnv!=-1)?aktMemo.tempPitchEnv==1:pitchEnv.on;
				if (pitchEnvOn)
				{
					aktMemo.pitchEnvPos = pitchEnv.updatePosition(aktMemo.pitchEnvPos, aktMemo.keyOff);
					int pitchValue = pitchEnv.getValueForPosition(aktMemo.pitchEnvPos) - 256; // result -256..256
					if (pitchEnv.filter)
						setupChannelFilter(aktMemo, !aktMemo.filterOn, pitchValue);
					else
					{
						long newPitch = 0;
						if (pitchValue < 0)
						{
							pitchValue = -pitchValue;
							if (pitchValue > 255) pitchValue = 255;
							newPitch = (long)ModConstants.LinearSlideDownTable[pitchValue];
						} 
						else
						{
							if (pitchValue > 255) pitchValue = 255;
							newPitch = (long)ModConstants.LinearSlideUpTable[pitchValue];
						}
						currentPeriod = (int)((((long)currentPeriod)*newPitch)>>16);
					}
					setNewPlayerTuningFor(aktMemo, currentPeriod);
				}
			}
		}

		if (aktMemo.keyOff) // Key Off without volume envelope or looping envelope:
		{
			// XMs do hard note cut, if no volumeEnv or not enabled
			if (isXM && (volumeEnv==null || !volumeEnv.on)) 
				currentVolume = aktMemo.currentInstrumentVolume = aktMemo.currentVolume = aktMemo.fadeOutVolume = 0;
			else // otherwise activate note fade, if not yet done 
				initNoteFade(aktMemo);
		}

		// Do the note fade
		// we are either from IT, which have this effect directly
		// or volume envelopes finished
		if (aktMemo.noteFade)
		{
			if (currentInstrument!=null && currentInstrument.volumeFadeOut>-1)
			{
				aktMemo.fadeOutVolume -= (currentInstrument.volumeFadeOut<<1);
				if (aktMemo.fadeOutVolume<0) aktMemo.fadeOutVolume = 0;
				currentVolume = (currentVolume * aktMemo.fadeOutVolume) >> ModConstants.MAXFADEOUTVOLSHIFT;
			}
			else
				currentVolume = aktMemo.fadeOutVolume = 0;

			// With IT a finished noteFade also sets the instrument as finished
			if (isIT && currentVolume <= 0 && isChannelActive(aktMemo))
			{
				aktMemo.instrumentFinished = true;
				if (aktMemo.isNNA) aktMemo.channelNumber = -1;
			}
		}

		if (aktMemo.noteCut) // Only with IT - XM does not know this one
		{
			currentVolume = 0;
		}
		else
		{
			// VolSwing - only if not silent
			if (currentVolume>0) currentVolume += aktMemo.swingVolume<<ModConstants.VOLUMESHIFT;
			 // Fade out initiated by recognized endless loop
			currentVolume = (currentVolume * loopingFadeOutValue) >> ModConstants.MAXFADEOUTVOLSHIFT;
			// Global Volumes
			currentVolume = (int)((((long)currentVolume * (long)globalVolume * (long)insVolume * (long)aktMemo.channelVolume) + (1<<(ModConstants.VOLUMESHIFT-1)) ) >> (7+7+6));
			// now for MasterVolume - which is SamplePreAmp, changed because of legacy MPT:
			currentVolume = (currentVolume * masterVolume) >> ((useGlobalPreAmp)?(ModConstants.PREAMP_SHIFT - 1):ModConstants.PREAMP_SHIFT);

			// Clipping Volume
			if (currentVolume>ModConstants.MAXCHANNELVOLUME) currentVolume=ModConstants.MAXCHANNELVOLUME;
			else
			if (currentVolume<ModConstants.MINCHANNELVOLUME) currentVolume=ModConstants.MINCHANNELVOLUME;
		}
		
		currentPanning += aktMemo.swingPanning; // Random value -128..+128
		if (currentPanning<0) currentPanning=0;
		else
		if (currentPanning>256) currentPanning=256;
		
		int panSep = mod.getPanningSeparation();
		if (panSep<128) // skip calculation if not needed...
		{
			currentPanning -= 128;
			currentPanning = (currentPanning * panSep)>>7;
			currentPanning += 128;
		}
		
		// IT Compatibility: Ensure that there is no pan swing, panbrello, panning envelopes, etc. applied on surround channels.
		if (isIT && aktMemo.doSurround) currentPanning = 128;
		
		// save current channel volume set
		aktMemo.actRampVolLeft = aktMemo.actVolumeLeft<<ModConstants.VOLRAMPLEN_FRAC;
		aktMemo.actRampVolRight = aktMemo.actVolumeRight<<ModConstants.VOLRAMPLEN_FRAC;
		
		// calculate new channel volume depending on currentVolume and panning
		if ((mod.getSongFlags()&ModConstants.SONG_ISSTEREO)==0)
		{
			aktMemo.actVolumeLeft = aktMemo.actVolumeRight = currentVolume;
		}
		else
		{
			if (isXM)
			{
				// From OpenMPT the following helpful hint:
				// FT2 uses square root panning. There is a 257-entry LUT for this,
				// but FT2's internal panning ranges from 0 to 255 only, meaning that
				// you can never truly achieve 100% right panning in FT2, only 100% left.
				if (currentPanning>255) currentPanning = 255;
				aktMemo.actVolumeLeft  = (currentVolume * ((currentPanning>0)?ModConstants.XMPanningTable[256-currentPanning]:0x10000)) >> 16;
				aktMemo.actVolumeRight = (currentVolume *					  ModConstants.XMPanningTable[    currentPanning]) >> 16;
			}
			else
			if (useSoftPanning) // OpenModPlug has this.
			{
				if (currentPanning<128)
				{
					aktMemo.actVolumeLeft  = (currentVolume *            128)>>8; 
					aktMemo.actVolumeRight = (currentVolume * currentPanning)>>8; // max:256
				}
				else
				{
					aktMemo.actVolumeLeft  = (currentVolume * (256 - currentPanning))>>8; 
					aktMemo.actVolumeRight = (currentVolume *                    128)>>8; // max:256
				}
			}
			else
			{
				aktMemo.actVolumeLeft  = (currentVolume * (256 - currentPanning))>>8; 
				aktMemo.actVolumeRight = (currentVolume * (      currentPanning))>>8; // max:256
			}
		}
		
		if (extraAttenuation>0) // for legacy MPT
		{
			aktMemo.actVolumeLeft >>= extraAttenuation;
			aktMemo.actVolumeRight >>= extraAttenuation;
		}
		
		// Surround
		if (aktMemo.doSurround) aktMemo.actVolumeRight = -aktMemo.actVolumeRight; 

		// now for ramping to target volume
		if (!aktMemo.disableVolRamp) // set, if ramping to target volume is forbidden (normally when a new sample is set)
		{
			if (aktMemo.actVolumeLeft != (aktMemo.actRampVolLeft>>ModConstants.VOLRAMPLEN_FRAC))
				aktMemo.deltaVolLeft = ((aktMemo.actVolumeLeft<<ModConstants.VOLRAMPLEN_FRAC) - aktMemo.actRampVolLeft) / volRampLen;
			else
				aktMemo.deltaVolLeft = 0;
			if (aktMemo.actVolumeRight != (aktMemo.actRampVolRight>>ModConstants.VOLRAMPLEN_FRAC))
				aktMemo.deltaVolRight = ((aktMemo.actVolumeRight<<ModConstants.VOLRAMPLEN_FRAC) - aktMemo.actRampVolRight) / volRampLen;
			else
				aktMemo.deltaVolRight = 0;
		}
		else
		{
			aktMemo.disableVolRamp = false;
			aktMemo.actRampVolLeft = aktMemo.actVolumeLeft<<ModConstants.VOLRAMPLEN_FRAC;
			aktMemo.deltaVolLeft = 0;
			aktMemo.actRampVolRight = aktMemo.actVolumeRight<<ModConstants.VOLRAMPLEN_FRAC;
			aktMemo.deltaVolRight = 0;
		}

		// AutoVibrato
		final Sample currentSample = aktMemo.currentSample;
		if (currentSample!=null && currentSample.vibratoDepth>0 && currentPeriod>0)
			doAutoVibratoEffekt(aktMemo, aktMemo.currentSample, currentPeriod);
		
		// Reset this. That way, envelope period changes are only temporary 
		// addons but considers temporarily set vibrato and arpegio effekts
		aktMemo.currentNotePeriodSet = currentPeriod = resetPeriodAfterEnvelopes;
	}
	/**
	 * Central Service called from ScreamTracker and ProTracker Mixers
	 * for the Panning Set effects
	 * @param aktMemo
	 * @param param
	 * @param bits
	 */
	protected void doPanning(final ChannelMemory aktMemo, int param, ModConstants.PanBits bits)
	{
		if (isMOD) return; // No panning set effect with ProTrackers
		
		aktMemo.doSurround = false;
		if (bits == ModConstants.PanBits.Pan4Bit) // 0..15
		{
			if (param>15) param=15;
			aktMemo.currentInstrumentPanning = aktMemo.panning = (((param&0xF)<<8) + 8 ) / 15;
		}
		else
		if (bits == ModConstants.PanBits.Pan6Bit) // 0..64
		{
			if (param>64) param=64;
			aktMemo.currentInstrumentPanning = aktMemo.panning = (param&0x7F)<<2;
		}
		else
		if (isS3M)
		{
			// This is special operation for S3M
			// ModConstants.PanBits.Pan8Bit now // 0..128
			if (param <= 0x80) // 7 Bit plus surround
			{
				aktMemo.currentInstrumentPanning = aktMemo.panning = param<<1;
			}
			else
			if (param==0xA4) // magic!
			{
				aktMemo.doSurround = true;
				aktMemo.currentInstrumentPanning = aktMemo.panning = 0x80;
			}
		}
		else
		{
			aktMemo.currentInstrumentPanning = aktMemo.panning = param&0xFF;
		}
		aktMemo.swingPanning = 0;
	}
	/**
	 * @since 17.01.2022
	 * @param aktMemo
	 * @param currentValue
	 * @param param
	 * @return
	 */
	protected float calculateSmoothParamChange(final ChannelMemory aktMemo, final float currentValue, final float param)
	{
		// currentTick is counted down from currentTempo (aka currentTicksPerRow)
		final int ticksLeft = currentTick;
		if (ticksLeft > 1)
			return currentValue + ((param - currentValue) / (float)ticksLeft);
		else
			return param;
	}
	/**
	 * @since 16.06.2020
	 * @param aktMemo
	 * @param isSmoothMidi
	 * @param midiMacro
	 * @param param
	 */
	protected void processMIDIMacro(final ChannelMemory aktMemo, final boolean isSmoothMidi, final String midiMacro, final int param)
	{
		final char[] midiMacroArray = midiMacro.toCharArray();
		int macroCommand =	((midiMacroArray[0]&0xFF)<<16) |
	    					((midiMacroArray[1]&0xFF)<<24) |
	    					((midiMacroArray[2]&0xFF)    ) |
	    					((midiMacroArray[3]&0xFF)<< 8);
		macroCommand &= 0x7F5F7F5F;
		
		if (macroCommand == 0x30463046) // internal code
		{
			int internalCode = -256;
			// Java supports Character.digit(char, radix) - but this will be slightly faster...
			// It's a bit of a risk because of unicode characters...
			//internalCode = (Character.digit(midiMacroArray[4], 16)<<4) | Character.digit(midiMacroArray[5], 16);
			if ((midiMacroArray[4] >= '0') && (midiMacroArray[4] <= '9')) internalCode  = (midiMacroArray[4] - '0') << 4;
			else
			if ((midiMacroArray[4] >= 'A') && (midiMacroArray[4] <= 'F')) internalCode  = (midiMacroArray[4] - 'A' + 0x0A) << 4;
			if ((midiMacroArray[5] >= '0') && (midiMacroArray[5] <= '9')) internalCode += (midiMacroArray[5] - '0');
			else
			if ((midiMacroArray[5] >= 'A') && (midiMacroArray[5] <= 'F')) internalCode += (midiMacroArray[5] - 'A' + 0x0A);
			
			if (internalCode >= 0)
			{
				char cData1 = midiMacroArray[6];
				int macroParam = 0;
				if ((cData1 == 'z') || (cData1 == 'Z'))
				{
					macroParam = param&0x7F;
					if (isSmoothMidi && aktMemo.lastZxxParam<0x80)
					{
						macroParam = (int)calculateSmoothParamChange(aktMemo, aktMemo.lastZxxParam, macroParam);
					}
					aktMemo.lastZxxParam = macroParam;
				}
				else
				{
					char cData2 = midiMacroArray[7];
					//macroParam = (Character.digit(cData1, 16)<<4) | Character.digit(cData2, 16);
					if ((cData1 >= '0') && (cData1 <= '9')) macroParam += (cData1 - '0') << 4; else
					if ((cData1 >= 'A') && (cData1 <= 'F')) macroParam += (cData1 - 'A' + 0x0A) << 4;
					if ((cData2 >= '0') && (cData2 <= '9')) macroParam += (cData2 - '0'); else
					if ((cData2 >= 'A') && (cData2 <= 'F')) macroParam += (cData2 - 'A' + 0x0A);
				}
				switch(internalCode) 
				{
					case 0x00: // F0.F0.00.xx: Set CutOff
						if (macroParam < 0x80) aktMemo.cutOff = macroParam;
						setupChannelFilter(aktMemo, !aktMemo.filterOn, 256);
						break;
					case 0x01: // F0.F0.01.xx: Set Resonance
						if (macroParam < 0x80) aktMemo.resonance = macroParam;
						setupChannelFilter(aktMemo, !aktMemo.filterOn, 256);
						break;
					case 0x02: // F0.F0.02.xx: Set filter mode
						if (macroParam < 0x20) aktMemo.filterMode = macroParam>>4;
						setupChannelFilter(aktMemo, !aktMemo.filterOn, 256);
						break;
				}
			}
		}
		else
		{
			// Forget about it for now
		}
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
	private boolean isChannelActive(ChannelMemory aktMemo)
	{
		return (aktMemo!=null)?(!aktMemo.instrumentFinished && aktMemo.currentTuning!=0 && aktMemo.currentSample!=null && aktMemo.channelNumber!=-1):false;
	}
	/**
	 * perform the duplicate note checks, if any are defined
	 * @since 08.07.2020
	 * @param aktMemo
	 */
	protected void doDNA(final ChannelMemory aktMemo)
	{
		final Instrument instr = aktMemo.assignedInstrument;
		if (instr == null) return;

		// we can save the time, if no duplicate action is set
		if (instr.dublicateNoteCheck == ModConstants.DCT_NONE) return;
		
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
						final int note = currentNNAChannel.assignedNoteIndex; 
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
						// Unsupported
						break;
				}
				
				if (applyDNA)
				{
					// We have a match!
					switch (instr.dublicateNoteAction)
					{
						case ModConstants.DNA_CUT:	// CUT: note volume to zero
							currentNNAChannel.noteCut = true;
							break;
						case ModConstants.DNA_FADE:		// fade: fade out with fixed values 
							initNoteFade(currentNNAChannel); 
							break;
						case ModConstants.DNA_OFF: 		// OFF: fade out with instrument fade out value
							currentNNAChannel.keyOff = true; 
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
			case ModConstants.NNA_CUT:		// CUT: note volume to zero 
				aktMemo.noteCut = true; 
				break;
			case ModConstants.NNA_CONTINUE:	// continue: let the music play 
				break;
			case ModConstants.NNA_FADE:		// fade: fade out with fixed values 
				initNoteFade(aktMemo); 
				break;
			case ModConstants.NNA_OFF: 		// OFF: fade out with instrument fade out value
				aktMemo.keyOff = true; 
				break;
		}
	}
	/**
	 * @since 11.06.2020
	 * @param aktMemo
	 * @param NNA
	 */
	private void doNNANew(final ChannelMemory aktMemo, final int NNA)
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
			// (left and right volume are shifted by 6 bit, so channel volume
			//  has space in the lower part)
			// additionally we also consider channels being far beyond their endpoint
			final int currentVolume = (memo.actVolumeLeft + memo.actVolumeRight) | memo.channelVolume;
			if ((currentVolume < lowVol) || (currentVolume == lowVol && memo.volEnvPos > envPos)) 
			{
				envPos = memo.volEnvPos;
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
	protected void doNNAAutoInstrument(ChannelMemory aktMemo)
	{
		if (!isIT || !isChannelActive(aktMemo) || aktMemo.muted || aktMemo.noteCut) return;
		
		Instrument currentInstrument = aktMemo.assignedInstrument;
		if (currentInstrument!=null)
		{
			// NNA_CUT is default for instruments with no NNA
			// so do not copy this to a new channel for just finishing
			// it off then.
			if (currentInstrument.NNA != ModConstants.NNA_CUT)
			{
				final int nna;// = (currentNNAChannel.tempNNAAction!=-1)?currentNNAChannel.tempNNAAction:NNA;
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
	}
	/**
	 * Service method to reset envelope pointers when
	 * new instrument / sample is set.
	 * Considers the carry flag
	 * @since 19.06.2020
	 * @param aktMemo
	 * @param force
	 */
	protected void resetEnvelopes(final ChannelMemory aktMemo)
	{
		Instrument ins = aktMemo.assignedInstrument;
		if (ins!=null)
		{
			Envelope volumeEnvelope = ins.volumeEnvelope;
			Envelope panningEnvelope = ins.panningEnvelope;
			Envelope pitchEnvelope = ins.pitchEnvelope;
			
			if (volumeEnvelope!=null && !volumeEnvelope.carry) aktMemo.volEnvPos = -1;
			if (panningEnvelope!=null &&!panningEnvelope.carry) aktMemo.panEnvPos = -1;
			if (pitchEnvelope!=null &&!pitchEnvelope.carry) aktMemo.pitchEnvPos = -1;
		}
	}
	/**
	 * Set all index values back to zero!
	 * Is for new notes or re-trigger a note
	 * @since 19.06.2006
	 * @param aktMemo
	 */
	protected void resetInstrumentPointers(final ChannelMemory aktMemo)
	{
		aktMemo.autoVibratoTablePos =
		aktMemo.autoVibratoAmplitude =
		aktMemo.currentTuningPos = 
		aktMemo.currentSamplePos = 
		aktMemo.interpolationMagic = 
		aktMemo.loopCounter = 0;
		aktMemo.isForwardDirection = true; 
		aktMemo.instrumentFinished = false;
	}
	/**
	 * Will only be called, if a new sample is set
	 * has to call the method above too
	 * Will additionally (so far) only reset finetuning 
	 * and transpose
	 * @since 14.06.2020
	 * @param aktMemo
	 */
	protected void resetForNewSample(final ChannelMemory aktMemo)
	{
		resetInstrumentPointers(aktMemo);
		aktMemo.currentFinetuneFrequency = aktMemo.currentSample.baseFrequency;
		aktMemo.currentFineTune = aktMemo.currentSample.fineTune;
		aktMemo.currentTranspose = aktMemo.currentSample.transpose;
	}
	/**
	 * Service method to reset Volume and Panning of current instrument / sample 
	 * @since 24.12.2023
	 * @param aktMemo
	 * @param newInstrument
	 * @param newSample
	 */
	private void resetVolumeAndPanning(final ChannelMemory aktMemo, final Instrument newInstrument, final Sample newSample)
	{
		int panning = -1;
		if (newInstrument!=null)
		{
			panning = newInstrument.defaultPan;
			if (panning!=-1) aktMemo.currentInstrumentPanning = aktMemo.panning = panning;
		}
		if (newSample!=null)
		{
			aktMemo.currentInstrumentVolume = aktMemo.currentVolume = newSample.volume;
			if (panning==-1)
			{
				panning = newSample.panning;
				if (panning!=-1) aktMemo.currentInstrumentPanning = aktMemo.panning = panning; 
			}
		}
		aktMemo.disableVolRamp = true; // resetting the volume means some kind of "re-trigger" - do not make it soft!
	}
	/**
	 * Will set the filters, if any - and return the filter-status set, for later use
	 * @since 29.12.2023
	 * @param aktMemo
	 * @param inst
	 */
	protected boolean setFilterAndRandomVariations(final ChannelMemory aktMemo, final Instrument inst, boolean useFilter)
	{
		// Set Resonance!
		if ((inst.initialFilterResonance & 0x80)!=0) { aktMemo.resonance = inst.initialFilterResonance & 0x7F; useFilter = true; }
		if ((inst.initialFilterCutoff & 0x80)!=0) { aktMemo.cutOff = inst.initialFilterCutoff & 0x7F; useFilter = true;}
		if (useFilter && inst.filterMode!=ModConstants.FLTMODE_UNCHANGED) aktMemo.filterMode = inst.filterMode;

		// first reset. This can be done safely here, because either IT-Mods have no instruments at all
		// or all samples are accessed through instruments. There is no mix
		aktMemo.swingVolume = aktMemo.swingPanning = aktMemo.swingResonance = aktMemo.swingCutOff = 0;
		
		// These values are added on top of their respective counterparts (channelvolume, panning, cutoff, resonance)
		// therefore the changes do not manipulate channel memories and restoring values are not necessary
		if (inst.randomVolumeVariation>=0)
		{
			// MPT uses the sample volume, IT use inst.globalVolume
			//aktMemo.swingVolume = (((((inst.randomVolumeVariation * (swinger.nextInt() % 0x80))>>6)+1) * inst.globalVolume / 199);
			aktMemo.swingVolume = (((((inst.randomVolumeVariation * (swinger.nextInt() % 0xFF))>>6)+1) * aktMemo.currentInstrumentVolume) / 199);
		}
		if (inst.randomPanningVariation>=0)
		{
			aktMemo.swingPanning = (int)((inst.randomPanningVariation<<2) * (swinger.nextInt() % 0x80)) >> 7;
		}
		// ModPlugTracker extended instruments. Yet, not read from files!
		if (inst.randomResonanceVariation>=0)
		{
			aktMemo.swingResonance = (((int)(inst.randomResonanceVariation * (swinger.nextInt() % 0x80)) >> 7) * aktMemo.resonance + 1) >> 7;
		}
		if (inst.randomCutOffVariation>=0)
		{
			aktMemo.swingCutOff = (((int)(inst.randomCutOffVariation * (swinger.nextInt() % 0x80)) >> 7) * aktMemo.cutOff + 1) >> 7; 
		}
		
		return useFilter;
	}
	/**
	 * @since 11.06.2006
	 * @param aktMemo
	 */
	protected void setNewInstrumentAndPeriod(final ChannelMemory aktMemo)
	{
		final PatternElement element = aktMemo.currentElement;
		final boolean isPortaToNoteEffekt = isPortaToNoteEffekt(aktMemo.currentEffekt, aktMemo.currentEffektParam, aktMemo.currentVolumeEffekt, aktMemo.currentVolumeEffektOp, aktMemo.currentAssignedNotePeriod);
		
		// Do Instrument default NNA
		if ((element.getPeriod()>0 || element.getNoteIndex()>0) && 
				!isPortaToNoteEffekt && 
				!isNNAEffekt(aktMemo.currentEffekt, aktMemo.currentEffektParam)) // New Note Action
		{
			doNNAAutoInstrument(aktMemo);
		}

		// copy last seen values from pattern
		aktMemo.assignedNotePeriod = aktMemo.currentAssignedNotePeriod; 
		aktMemo.assignedNoteIndex = aktMemo.currentAssignedNoteIndex; 
		aktMemo.effekt = aktMemo.currentEffekt;
		aktMemo.effektParam = aktMemo.currentEffektParam; 
		aktMemo.volumeEffekt = aktMemo.currentVolumeEffekt; 
		aktMemo.volumeEffektOp = aktMemo.currentVolumeEffektOp;

		// Before copying the last seen instrument, check for volume and panning sets
		if (element.getInstrument()>0)
		{
			// If this is a PortaToNote effect with an instrument set - and whatever is set, there is one still running, we need to use old instrument / sample set...
			if (isPortaToNoteEffekt && !aktMemo.instrumentFinished)
			{
				// There is a really oddly different behavior between Screamtracker, Fasttracker, OpenModPlug and Schismtracker
				// Schism sets all values from the new instrument/sample set - even the tuning!
				// Fasttracker will ignore the new instrument/sample and use the old one
				// ScreamTracker 2.14v5 does the same
				// Modplug however takes the volume of the old instrument but - with ITs - the panning from the new one
				// We cannot do all - so we will rely on FT2.09 and IT2.14 behavior - except: if no instrument is playing, set the new one (down below!
				resetVolumeAndPanning(aktMemo, aktMemo.assignedInstrument, aktMemo.currentSample);
			}
			else // (!isPortaToNoteEffekt || aktMemo.instrumentFinished):  no Porta to note or a porta to note but no instrument playing, so set the new one. 
			{
				// Volume and panning of given instrument is set here.
				// Long running samples are "reactivated", because volume of zero is reset to sample default volume. They are however not "re-triggered"
				
				Instrument newInstrument = aktMemo.currentAssignedInstrument; // same as mod.getInstrumentContainer().getInstrument(element.getInstrument()-1); - in this case, because was copied 
				// Get the correct sample from the mapping table, if there is an instrument set
				Sample newSample = (newInstrument!=null)?
				                       ((aktMemo.assignedNoteIndex>0)? // but only if we also have a note index, if not, ignore it!
				                           mod.getInstrumentContainer().getSample(newInstrument.getSampleIndex(aktMemo.assignedNoteIndex-1))
				                           :null)
				                       :mod.getInstrumentContainer().getSample(aktMemo.currentAssignedInstrumentIndex-1);
				
				// With FastTracker however (FastTracker 2.09!), these values 
				// are not used, if there is a new(!) sample with no note. Then
				// ignore them (only, if it is the same...)
				// Plus with IT ignore it, if no note is set.
				if (!(isXM && newSample!=aktMemo.currentSample && (element.getPeriod()==0 && element.getNoteIndex()==0)) &&
					!(isIT && (element.getPeriod()==0 && element.getNoteIndex()==0)))
				{
					resetVolumeAndPanning(aktMemo, newInstrument, newSample);
				}
			}
		}

		// Now safe those instruments for later re-use
		aktMemo.assignedInstrumentIndex = aktMemo.currentAssignedInstrumentIndex;
		aktMemo.assignedInstrument = aktMemo.currentAssignedInstrument;

		// Key Off, Note Cut or Period / noteIndex to set?
		if (element.getPeriod()==ModConstants.KEY_OFF || element.getNoteIndex()==ModConstants.KEY_OFF)
		{
			aktMemo.keyOff = true;
		}
		else
		if (element.getPeriod()==ModConstants.NOTE_CUT || element.getNoteIndex()==ModConstants.NOTE_CUT)
		{
			aktMemo.noteCut = true;
		}
		else
		if (element.getPeriod()==ModConstants.NOTE_FADE || element.getNoteIndex()==ModConstants.NOTE_FADE)
		{
			initNoteFade(aktMemo);
		}
		else
		if (	((element.getPeriod()>0 || element.getNoteIndex()>0) || // if there is a note, we need to calc the new tuning and activate a previous set instrument
				(isScreamTracker && element.getInstrument()>0)) // but with scream tracker like mods, the old notevalue is used, if an instrument is set
				 && (!isPortaToNoteEffekt || aktMemo.instrumentFinished) // but ignore this if porta to note (but do not ignore, if no instrument is playing)
			)
		{
			final int savedNoteIndex = aktMemo.assignedNoteIndex; // save the noteIndex - if it is changed by an instrument, we use that one to generate the period, but set it back then
			final Instrument inst = aktMemo.assignedInstrument; 
			boolean newInstrumentWasSet = false;
			boolean useFilter = !globalFilterMode;
			
			// We have an instrument assigned, so there was (once) an instrument set!
			if (inst!=null || aktMemo.assignedInstrumentIndex>0) 
			{
				Sample newSample = null;
				// Get the correct sample from the mapping table, if we have an instrument and a valid noteindex
				if (inst!=null && aktMemo.assignedNoteIndex>0)
				{
					newSample = mod.getInstrumentContainer().getSample(inst.getSampleIndex(aktMemo.assignedNoteIndex-1));
					aktMemo.assignedNoteIndex = inst.getNoteIndex(aktMemo.assignedNoteIndex-1)+1;

					// Now for some specials of IT
					if (isIT)
					{
						useFilter = setFilterAndRandomVariations(aktMemo, inst, useFilter);
					}
				}
				else
					newSample = mod.getInstrumentContainer().getSample(aktMemo.assignedInstrumentIndex-1);
				
				if (newSample!=null)
				{
					// Reset all pointers, if it's a new one...
					// or with IT: play sample even without note - but not only if it is
					// a new one but the same, and it's finished / silent...
					if (aktMemo.currentSample!=newSample)
					{
						aktMemo.disableVolRamp = newInstrumentWasSet = true; // Also disable VolumeRamping with new Sample set
						// Now activate new Instrument...
						aktMemo.currentSample = newSample; 
						resetForNewSample(aktMemo);
						resetEnvelopes(aktMemo);
					}
					// With IT this has to be checked! Always! 
					// IT-MODS (and derivates) reset here, because a sample set is relevant (see below)
					if (isScreamTracker && (aktMemo.instrumentFinished || (element.getPeriod()>0 || element.getNoteIndex()>0)))
					{
						resetAllEffects(aktMemo, element, true); // Reset Tremolo and such things... Forced! because of a new note
						aktMemo.noteCut = aktMemo.keyOff = aktMemo.noteFade = false;
						aktMemo.tempVolEnv = aktMemo.tempPanEnv = aktMemo.tempPitchEnv = -1;
						resetInstrumentPointers(aktMemo);
						resetEnvelopes(aktMemo);
					}
				}
			}
			
			if (!isPortaToNoteEffekt)
			{
				// Now set the player Tuning and reset some things in advance.
				// normally we are here, because a note was set in the pattern.
				// Except for IT-MODs - then we are here, because either note or
				// instrument were set. If no notevalue was set, the old 
				// notevalue is to be used.
				// However, we do not reset the instrument here - the reset was 
				// already done above - so this is here for all sane players :)
				if (isFastTracker) // MOD, XM...
				{
					resetAllEffects(aktMemo, element, true); // Reset Tremolo and such things... Forced! because of a new note
					aktMemo.noteCut = aktMemo.keyOff = aktMemo.noteFade = false;
					resetInstrumentPointers(aktMemo);
					resetEnvelopes(aktMemo);
				}
			
				// With impulse tracker, again we are here because of an instrument
				// set - we should now only reset the tuning (like automatically with
				// all others) when we have a note/period or a new instrument
				if (isScreamTracker)
				{
					if ((element.getPeriod()>0 || element.getNoteIndex()>0) || newInstrumentWasSet)
						setNewPlayerTuningFor(aktMemo, aktMemo.portaTargetNotePeriod = aktMemo.currentNotePeriod = getFineTunePeriod(aktMemo));
					// and set the resonance, settings were stored above in instr. value copy
					if ((/*aktMemo.resonance>0 || */aktMemo.cutOff<0x7F) && useFilter) setupChannelFilter(aktMemo, true, 256);
				}
				else // reset to last known note period if instrument is set without note (or current, which is set anyways)
					setNewPlayerTuningFor(aktMemo, aktMemo.portaTargetNotePeriod = aktMemo.currentNotePeriod = getFineTunePeriod(aktMemo));
			}
			// write back, if noteIndex was changed by instrument note mapping
			aktMemo.assignedNoteIndex = savedNoteIndex;
		}
	}
	/**
	 * Do first row or tick effects.
	 * IT: first Row, then VolumeColumn
	 * Others: vice versa
	 * @since 18.09.2010
	 * @param aktMemo
	 */
	protected void processEffekts(boolean inTick, final ChannelMemory aktMemo)
	{
		if (aktMemo.isNNA) // no effects for NNA Channel, only envelopes
			processEnvelopes(aktMemo);
		else
		{
			if (inTick)
			{
				if (isNoteDelayEffekt(aktMemo.currentEffekt, aktMemo.currentEffektParam) && aktMemo.noteDelayCount>0)
				{
					if (aktMemo.noteDelayCount >= currentTempo)
					{
						// illegal Notedelay - ignore it, do not copy new values (row is seen as empty)
						// to do so, we replace with the values from assigned*
						aktMemo.noteDelayCount = -1;
						
						aktMemo.currentAssignedNotePeriod = aktMemo.assignedNotePeriod;
						aktMemo.currentAssignedNoteIndex = aktMemo.assignedNoteIndex;
						aktMemo.currentEffekt = aktMemo.effekt;
						aktMemo.currentEffektParam = aktMemo.effektParam; 
						aktMemo.currentVolumeEffekt = aktMemo.volumeEffekt; 
						aktMemo.currentVolumeEffektOp = aktMemo.volumeEffektOp;
						
						// Not fully empty with IT - instrument is remembered!
						if (!isIT)
						{
							aktMemo.currentAssignedInstrumentIndex = aktMemo.assignedInstrumentIndex;
							aktMemo.currentAssignedInstrument = aktMemo.assignedInstrument;
						}
					}
					else
					{
						aktMemo.noteDelayCount--;
						if (aktMemo.noteDelayCount<=0)
						{
							aktMemo.noteDelayCount = -1;
	
							setNewInstrumentAndPeriod(aktMemo);
							if (isIT)
							{
								doTickEffekts(aktMemo);
								doVolumeColumnTickEffekt(aktMemo);
							}
							else
							{
								doVolumeColumnTickEffekt(aktMemo);
								doTickEffekts(aktMemo);
							}
						}
					}
				}
				else
				{
					// IT: first Row, then column!
					if (isIT)
					{
						doTickEffekts(aktMemo);
						doVolumeColumnTickEffekt(aktMemo);
					}
					else
					{
						doVolumeColumnTickEffekt(aktMemo);
						doTickEffekts(aktMemo);
					}
				}
				processEnvelopes(aktMemo);
			}
			else 
			{
				// set all new effect parameter - we cannot process envelopes
				// here, because first all effects must gain new parameter
				// especially the global volumes!
				if (isScreamTracker)
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
				else
				{
					doVolumeColumnRowEffekt(aktMemo);
					doRowEffects(aktMemo);
				}
			}
		}
	}
	protected boolean isInfiniteLoop(final int currentArrangement, final PatternRow patternRow)
	{
		return (mod.isArrangementPositionPlayed(currentArrangement) && patternRow.isRowPlayed());
	}
	protected boolean isInfiniteLoop(final int currentArrangement, final int currentRow)
	{
		return isInfiniteLoop(currentArrangement, currentPattern.getPatternRow(currentRow));
	}
	/**
	 * Do the Events of a new Row!
	 * @return true, if finished! 
	 */
	protected void doRowEvents()
	{
		final PatternRow patternRow = currentPattern.getPatternRow(currentRow);
		if (patternRow==null) return;

		patternRow.setRowPlayed();

		// inform listeners, that we are in a new row!
		firePositionUpdate(sampleRate, samplesMixed, getCurrentPatternPosition());

		for (int c=0; c<maxChannels; c++)
		{
			final ChannelMemory aktMemo = channelMemory[c];

			if (!aktMemo.isNNA) // no NNA Channel
			{
				// get pattern and channel memory data for current channel
				final PatternElement element = patternRow.getPatternElement(c);
				
				// reset all effects on this channel
				resetAllEffects(aktMemo, element, false);
				
				// Now copy the pattern data but remain old values for note and instrument
				aktMemo.currentElement = element;
				
				if (element.getPeriod()>0) aktMemo.currentAssignedNotePeriod = element.getPeriod(); 
				if (element.getNoteIndex()>0)  aktMemo.currentAssignedNoteIndex = element.getNoteIndex();

				aktMemo.currentEffekt = element.getEffekt();
				aktMemo.currentEffektParam = element.getEffektOp();
				aktMemo.currentVolumeEffekt = element.getVolumeEffekt();
				aktMemo.currentVolumeEffektOp = element.getVolumeEffektOp();
	
				if (element.getInstrument()>0)
				{
					aktMemo.currentAssignedInstrumentIndex = element.getInstrument();
					aktMemo.currentAssignedInstrument = mod.getInstrumentContainer().getInstrument(element.getInstrument()-1);
				}

				// so far for S00 effekt memory - if FastTracker has that too, we will elaborate
				if (isIT)
				{
					if (aktMemo.currentEffekt!=0 && aktMemo.currentEffektParam == 0)
						aktMemo.currentEffektParam = getEffektOpMemory(aktMemo, aktMemo.currentEffekt, aktMemo.currentEffektParam);
				}
	
				if (!isNoteDelayEffekt(aktMemo.currentEffekt, aktMemo.currentEffektParam)) // If this is a noteDelay, we lose for now, this is all done later!
				{
					setNewInstrumentAndPeriod(aktMemo);
					processEffekts(false, aktMemo);
				}
				else
				{
					if (isIT) // so far for S00 effect memory - if FastTracker has that too, we will elaborate
					{
						if (aktMemo.currentEffektParam!=0) aktMemo.S_Effect_Memory = aktMemo.currentEffektParam;
					}
					if (aktMemo.noteDelayCount<0)
					{
						final int effektOpEx = aktMemo.currentEffektParam&0x0F;
						
						if (isIT)
						{
							if (effektOpEx==0) aktMemo.noteDelayCount=1;
							else aktMemo.noteDelayCount = effektOpEx;
						}
						else
							aktMemo.noteDelayCount = effektOpEx;
					}
				}
			}
		}
		// with Row Effects, first all rows effect parameter need to be
		// processed - then we can do the envelopes and volume effects
		for (int c=0; c<maxChannels; c++)
			processEnvelopes(channelMemory[c]);
	}
	/**
	 * when stepping to a new Pattern - Position needs new set...
	 * @since 21.01.2014
	 */
	private void resetJumpPositionSet()
	{
		for (int c=0; c<maxChannels; c++)
			channelMemory[c].jumpLoopPositionSet = false;
	}
	/**
	 * Do the events during a Tick.
	 * @return true, if finished! 
	 */
	protected boolean doRowAndTickEvents()
	{
		// Global Fade Out because of recognized endless loop
		if (doLoopingGlobalFadeout)
		{
			loopingFadeOutValue-=ModConstants.FADEOUT_SUB;
			if (loopingFadeOutValue <= 0) return true; // We did a fadeout and are finished now
		}
		
		if (patternTicksDelayCount>0) // Fine Pattern Delay in # ticks
		{
			for (int c=0; c<maxChannels; c++)
				processEffekts(true, channelMemory[c]);
			patternTicksDelayCount--; 
		}
		else
		{
			currentTick--;
			if (currentTick<=0)
			{
				currentTick = currentTempo;
	
				// if PatternDelay, do it and return
				if (patternDelayCount>0)
				{
					// Process effects
					for (int c=0; c<maxChannels; c++)
						processEffekts(isIT, channelMemory[c]);
					// for IT reset note delays, but do not call doRowEvents (is for tick=0 only)
					if (isIT)
					{
						for (int c=0; c<maxChannels; c++)
						{
							final ChannelMemory aktMemo = channelMemory[c];
							if (isNoteDelayEffekt(aktMemo.currentEffekt, aktMemo.currentEffektParam))
							{
								if (aktMemo.noteDelayCount<0)
								{
									final int effektOpEx = aktMemo.currentEffektParam&0x0F;
									
									if (effektOpEx==0) aktMemo.noteDelayCount=1;
									else aktMemo.noteDelayCount = effektOpEx;
								}
								aktMemo.noteDelayCount = aktMemo.currentEffektParam&0xF;
							}
						}
					}
					
					patternDelayCount--;
					if (patternDelayCount<=0)
					{
						patternDelayCount = -1;
						currentRow++; // and move on!
					}
				}
				else
				{
					// Ticks finished and no new row --> FINITO!
					if (currentArrangement>=mod.getSongLength())
					{
						if (mod.getSongRestart()>-1 && (doNoLoops&ModConstants.PLAYER_LOOP_LOOPSONG)!=0)
						{
							initializeMixer(true);
							currentArrangement = mod.getSongRestart();
							currentPatternIndex = mod.getArrangement()[currentArrangement];
							currentPattern = mod.getPatternContainer().getPattern(currentPatternIndex);
							currentRow = 0;
							if ((doNoLoops&ModConstants.PLAYER_LOOP_FADEOUT)!=0)
								doLoopingGlobalFadeout = true;
						}
						else
							return true;
					}

					// Do the row events
					doRowEvents();

					// and step to the next row... Even if there are no more -  we will find out later!
					// However: if doRowEvents sets a patternDelay (patternDelayCount!=-1)
					// we should not move on, but process the patternDelay on the currentRow set
					// and if the patternDelayCount is finished, move on then.
					if (patternDelayCount<=0)
					{
						currentRow++;
						patternDelayCount = -1; // if patternDelayCount was "0", set it to uninitialized
					}

					if (patternJumpPatternIndex!=-1) // Do not check infinit Loops here, this is never infinit
					{
						currentRow = patternJumpPatternIndex;
						patternJumpPatternIndex = -1;
					}
				}
				
				// now check for end of pattern and perform patternJumps, if any
				if (currentRow>=currentPattern.getRowCount() || 
					patternBreakRowIndex!=-1 || patternBreakJumpPatternIndex!=-1)
				{
					mod.setArrangementPositionPlayed(currentArrangement);
					if (patternBreakJumpPatternIndex!=-1)
					{
						final boolean infiniteLoop = isInfiniteLoop(patternBreakJumpPatternIndex, (patternBreakRowIndex!=-1)?patternBreakRowIndex:currentRow-1);
						if (infiniteLoop && (doNoLoops&ModConstants.PLAYER_LOOP_IGNORE)!=0)
						{
							patternBreakRowIndex = patternBreakJumpPatternIndex = -1;
							resetJumpPositionSet();
							currentArrangement++;
						}
						else
						{
							currentArrangement = patternBreakJumpPatternIndex;
						}
						patternBreakJumpPatternIndex = -1;
						// and activate fadeout, if wished
						if (infiniteLoop && (doNoLoops&ModConstants.PLAYER_LOOP_FADEOUT)!=0)
							doLoopingGlobalFadeout = true;
					}
					else
					{
						resetJumpPositionSet();
						currentArrangement++;
					}
					
					if (patternBreakRowIndex!=-1)
					{
						currentRow = patternBreakRowIndex;
						patternBreakRowIndex = -1;
						// If pattern break to position is set and last pattern reached, loop song and restart at given position.
						// !But if loop_ignore is checked OR loop_song is not checked, don't do this!
						// So do it, if NOT ((doNoLoops&ModConstants.PLAYER_LOOP_IGNORE)!=0 || (doNoLoops&ModConstants.PLAYER_LOOP_LOOPSONG)==0)
						if (currentArrangement>=mod.getSongLength() && (doNoLoops&ModConstants.PLAYER_LOOP_IGNORE)==0 && (doNoLoops&ModConstants.PLAYER_LOOP_LOOPSONG)!=0)
						{
							currentArrangement = mod.getSongRestart(); // can be -1 if not set
							if (currentArrangement < 0) currentArrangement = 0;
							currentPatternIndex = mod.getArrangement()[currentArrangement];
							currentPattern = mod.getPatternContainer().getPattern(currentPatternIndex);
							// as this is per definition an infinite loop, activate fadeout, if wished
							if ((doNoLoops&ModConstants.PLAYER_LOOP_FADEOUT)!=0)
								doLoopingGlobalFadeout = true;
						}
					}
					else
						currentRow = 0;

					// End of song? Fetch new pattern if not...
					if (currentArrangement<mod.getSongLength())
					{
						currentPatternIndex = mod.getArrangement()[currentArrangement];
						currentPattern = mod.getPatternContainer().getPattern(currentPatternIndex);
					}
					else
					{
						currentPatternIndex = -1;
						currentPattern = null;
					}
				}
			}
			else
			{
				// Do all Tickevents, 'cause we are in a Tick...
				for (int c=0; c<maxChannels; c++)
					processEffekts(true, channelMemory[c]);
			}
		}

		return false;
	}
	/**
	 * Add current speed to samplepos and
	 * fit currentSamplePos into loop values
	 * or signal Sample finished
	 * @since 18.06.2006
	 * @param aktMemo
	 */
	protected void fitIntoLoops(final ChannelMemory aktMemo)
	{
		final Sample sample = aktMemo.currentSample;
		aktMemo.currentTuningPos += aktMemo.currentTuning;
		if (aktMemo.currentTuningPos >= ModConstants.SHIFT_MAX)
		{
			final int addToSamplePos = aktMemo.currentTuningPos >> ModConstants.SHIFT;
			aktMemo.currentTuningPos &= ModConstants.SHIFT_MASK;

			// Set the start/end loop position to check against...
			int loopStart = 0;
			int loopEnd = sample.length;
			int loopLength = loopEnd;
			int inLoop = 0;
			
			if ((sample.loopType&ModConstants.LOOP_SUSTAIN_ON)!=0 && !aktMemo.keyOff) // Sustain Loop on?
			{
				loopStart = sample.sustainLoopStart;
				loopEnd = sample.sustainLoopStop;
				loopLength = sample.sustainLoopLength;
				inLoop = ModConstants.LOOP_SUSTAIN_ON;
			}
			else
			if ((sample.loopType&ModConstants.LOOP_ON)!=0) 
			{
				loopStart = sample.loopStart;
				loopEnd = sample.loopStop;
				loopLength = sample.loopLength;
				inLoop = ModConstants.LOOP_ON;
			}

			// If Forward direction:
			if (aktMemo.isForwardDirection)
			{
				aktMemo.currentSamplePos += addToSamplePos;
				
				// do we have an overrun of border?
				if (aktMemo.currentSamplePos >= loopEnd)
				{
					// We need to check against a loop set - maybe a sustain loop is finished
					// but no normal loop is set:
					if (inLoop==0)
					{
						aktMemo.instrumentFinished = true;
						// if this is a NNA channel, free it
						if (aktMemo.isNNA) aktMemo.channelNumber = -1;
					}
					else
					{
						// This is needed if sample rate is very low or baseFreq very high. Then this will not have 
						// a fraction in Tuning, but addition of 2 or even more.
						final int aheadOfStop = ((aktMemo.currentSamplePos - loopEnd) % loopLength);
						
						// check if loop, that was enabled, is a ping pong
						if ((inLoop == ModConstants.LOOP_ON && (sample.loopType & ModConstants.LOOP_IS_PINGPONG)!=0) ||
							(inLoop == ModConstants.LOOP_SUSTAIN_ON && (sample.loopType & ModConstants.LOOP_SUSTAIN_IS_PINGPONG)!=0))
						{
							aktMemo.isForwardDirection = false;
							if (isFastTracker) // Protracker / XM need one less here!
								aktMemo.currentSamplePos = loopEnd - aheadOfStop;
							else
								aktMemo.currentSamplePos = loopEnd - 1 - aheadOfStop;
							aktMemo.loopCounter++;
						}
						else
						{
							aktMemo.currentSamplePos = loopStart + aheadOfStop;
							aktMemo.loopCounter++;
						}
					}
				}
			}
			else // Backwards in Ping Pong
			{
				aktMemo.currentSamplePos -= addToSamplePos;

				if (aktMemo.currentSamplePos < loopStart)
				{
					aktMemo.isForwardDirection = true;
					aktMemo.currentSamplePos = loopStart + ((loopStart - aktMemo.currentSamplePos) % loopLength);
				}
			}
			// after reposition of sample pointer, check for interpolation magic
			if (inLoop == ModConstants.LOOP_SUSTAIN_ON && !aktMemo.keyOff) // Sustain Loop on?
			{
				aktMemo.interpolationMagic = sample.getSustainLoopMagic(aktMemo.currentSamplePos, aktMemo.loopCounter);
			}
			else
			if (inLoop == ModConstants.LOOP_ON) 
			{
				aktMemo.interpolationMagic = sample.getLoopMagic(aktMemo.currentSamplePos, aktMemo.loopCounter);
			}
			else
				aktMemo.interpolationMagic = 0;
		}
	}
	/**
	 * Fill the buffers with channel data
	 * @since 18.06.2006
	 * @param leftBuffer
	 * @param rightBuffer
	 * @param startIndex
	 * @param amount
	 * @param aktMemo
	 */
	protected void mixChannelIntoBuffers(final long[] leftBuffer, final long[] rightBuffer, final int startIndex, final int endIndex, final ChannelMemory aktMemo)
	{
		aktMemo.bigSampleLeft = aktMemo.bigSampleRight = 0;
		for (int i=startIndex; i<endIndex; i++)
		{
			// Retrieve the sample data for this point (interpolated, if necessary)
			// the array "samples" is created with 2 elements per default
			// we will receive 2 long values even with mono samples
			aktMemo.currentSample.getInterpolatedSample(samples, doISP, aktMemo.currentSamplePos, aktMemo.currentTuningPos, !aktMemo.isForwardDirection, aktMemo.interpolationMagic);

			// Resonance Filters
			if (aktMemo.filterOn) doResonance(aktMemo, samples);

			long sampleL = samples[0];
			long sampleR = samples[1];

			// Volume Ramping
			int volL = aktMemo.actRampVolLeft>>ModConstants.VOLRAMPLEN_FRAC;
			if (aktMemo.deltaVolLeft!=0)
			{
				if ((aktMemo.deltaVolLeft>0 && volL>aktMemo.actVolumeLeft) ||
					(aktMemo.deltaVolLeft<0 && volL<aktMemo.actVolumeLeft))
				{
					// Target reached
					aktMemo.actRampVolLeft = (volL = aktMemo.actVolumeLeft)<<ModConstants.VOLRAMPLEN_FRAC;
					aktMemo.deltaVolLeft = 0;
				}
				else
					aktMemo.actRampVolLeft += aktMemo.deltaVolLeft;
			}
			
			int volR = aktMemo.actRampVolRight>>ModConstants.VOLRAMPLEN_FRAC;
			if (aktMemo.deltaVolRight!=0)
			{
				if ((aktMemo.deltaVolRight>0 && volR>aktMemo.actVolumeRight) ||
					(aktMemo.deltaVolRight<0 && volR<aktMemo.actVolumeRight))
				{
					// Target reached
					aktMemo.actRampVolRight = (volR = aktMemo.actVolumeRight)<<ModConstants.VOLRAMPLEN_FRAC;
					aktMemo.deltaVolRight = 0;
				}
				else
					aktMemo.actRampVolRight += aktMemo.deltaVolRight;
			}
			
			// Fit into volume for the two channels
			sampleL = (sampleL*(long)volL)>>ModConstants.MAXVOLUMESHIFT;
			sampleR = (sampleR*(long)volR)>>ModConstants.MAXVOLUMESHIFT;
			
			// and off you go
			leftBuffer [i] += sampleL;
			rightBuffer[i] += sampleR;

			// store the highest (absolute) sample for display in the ModPatternDialog
			if (sampleL<0) sampleL = -sampleL;
			if (sampleL>aktMemo.bigSampleLeft) aktMemo.bigSampleLeft = sampleL;
			if (sampleR<0) sampleR = -sampleR;
			if (sampleR>aktMemo.bigSampleRight) aktMemo.bigSampleRight = sampleR;
			
			// Now next sample plus fit into loops - if any
			fitIntoLoops(aktMemo);

			if (aktMemo.instrumentFinished) break;
		}
	}
	/**
	 * Retrieves Sample Data without manipulating the currentSamplePos and currentTuningPos and currentDirection
	 * (a kind of read ahead)
	 * @since 18.06.2006
	 * @param leftBuffer
	 * @param rightBuffer
	 * @param aktMemo
	 */
	private ChannelMemory rampDataBufferSaveMemory = new ChannelMemory();
	private void fillRampDataIntoBuffers(final long[] leftBuffer, final long[] rightBuffer, final ChannelMemory aktMemo)
	{
		// Remember changeable values
		rampDataBufferSaveMemory.setUpFrom(aktMemo);
		
		mixChannelIntoBuffers(leftBuffer, rightBuffer, 0, ModConstants.VOL_RAMP_LEN, aktMemo);
		
		// set them back
		aktMemo.setUpFrom(rampDataBufferSaveMemory);
	}
	/**
	 * Will mix #count 32bit signed samples in stereo into the two buffer.
	 * The buffers will contain 32Bit signed samples.
	 * @param leftBuffer
	 * @param rightBuffer
	 * @param bufferSize
	 * @return #of samples mixed, -1 if mixing finished
	 */
	public int mixIntoBuffer(final long[] leftBuffer, final long[] rightBuffer, final int bufferSize)
	{
		if (modFinished) return -1;
		
		int startIndex = 0; // we start at zero
		int endIndex = 0; // where to finish mixing
		int mixAmount;
		final int maxEndIndex = bufferSize - ModConstants.VOL_RAMP_LEN;

		while (endIndex < maxEndIndex && !modFinished)
		{
			mixAmount = endIndex+leftOverSamplesPerTick;
			if (mixAmount>=maxEndIndex)
				mixAmount = maxEndIndex - endIndex;
			else
				mixAmount = leftOverSamplesPerTick;
			
			endIndex += mixAmount;
			leftOverSamplesPerTick -= mixAmount;
			
			for (int c=0; c<maxChannels; c++)
			{
				final ChannelMemory aktMemo = channelMemory[c];

				// Mix this channel?
				if (!aktMemo.muted && isChannelActive(aktMemo))
				{
					// fill in those samples
					mixChannelIntoBuffers(leftBuffer, rightBuffer, startIndex, endIndex, aktMemo);
					
					// This is only for eye-candy
					if (!aktMemo.isNNA)
					{
						final boolean setZero = aktMemo.instrumentFinished || globalVolume==0 || masterVolume==0; 
						final long sampleL = (setZero)?0:((aktMemo.bigSampleLeft <<(7+((useGlobalPreAmp)?ModConstants.PREAMP_SHIFT-1:ModConstants.PREAMP_SHIFT))) / (long)globalVolume / (long)masterVolume)<<extraAttenuation;
						final long sampleR = (setZero)?0:((aktMemo.bigSampleRight<<(7+((useGlobalPreAmp)?ModConstants.PREAMP_SHIFT-1:ModConstants.PREAMP_SHIFT))) / (long)globalVolume / (long)masterVolume)<<extraAttenuation;
						firePeekUpdate(sampleRate, samplesMixed, c, sampleL, sampleR, aktMemo.doSurround);
					}

					// and get the ramp data for interweaving, if there is something left
					if (!aktMemo.instrumentFinished) fillRampDataIntoBuffers(nvRampL, nvRampR, aktMemo);
				}
			}

			// Now Interweave with last ticks ramp buffer data
			for (int n=0; n<ModConstants.VOL_RAMP_LEN; n++)
			{
				final int difFade = ModConstants.VOL_RAMP_LEN - n;
				
				long vl = ((leftBuffer [startIndex + n] * n) + (vRampL[n] * difFade))>>ModConstants.VOL_RAMP_FRAC;
				long vr = ((rightBuffer[startIndex + n] * n) + (vRampR[n] * difFade))>>ModConstants.VOL_RAMP_FRAC;

				leftBuffer [startIndex + n] = vl;
				rightBuffer[startIndex + n] = vr;
				
				// and copy in one step...
				vRampL[n] = nvRampL[n]; vRampR[n] = nvRampR[n];
				nvRampL[n] = nvRampR[n] = 0;
			}

			startIndex += mixAmount;
			if (leftOverSamplesPerTick<=0)
			{
				modFinished = doRowAndTickEvents();
				samplesMixed += samplePerTicks;
				leftOverSamplesPerTick = samplePerTicks; // Speed changes also change samplePerTicks - so always after doTickEvents!
			}
		}

		return startIndex;
	}
	/**
	 * after a mute change, copy this to all their NNA channels
	 * @since 28.11.2023
	 */
	private void setNNAMuteStatus()
	{
		for (int c=mod.getNChannels(); c<maxChannels; c++)
		{
			final ChannelMemory aktMemo = channelMemory[c];
			if (aktMemo.channelNumber > -1) // aktive NNA-Channel
			{
				aktMemo.muted = channelMemory[aktMemo.channelNumber].muted;
			}
		}
	}
	/**
	 * Will mute/unmute a channel
	 * @since 27.11.2023
	 * @param channelNumber
	 */
	public void toggleMuteChannel(final int channelNumber)
	{
		if (channelNumber>=0 && channelNumber<=maxChannels)
		{
			final ChannelMemory aktMemo = channelMemory[channelNumber];
			if (!aktMemo.wasITforced) aktMemo.muted = !aktMemo.muted;
			setNNAMuteStatus();
		}
	}
	/**
	 * Unmutes all Channels, except, if IT wanted this channel to be muted!
	 * @since 27.11.2023
	 */
	public void unMuteAll()
	{
		for (int c=0; c<maxChannels; c++)
		{
			final ChannelMemory aktMemo = channelMemory[c];
			aktMemo.muted = aktMemo.wasITforced;
		}
		setNNAMuteStatus();
	}
	/**
	 * All channels but this one will be muted
	 * @since 27.11.2023
	 * @param channelNumber
	 */
	public void makeChannelSolo(final int channelNumber)
	{
		if (channelNumber>=0 && channelNumber<=maxChannels)
		{
			for (int c=0; c<maxChannels; c++)
			{
				final ChannelMemory aktMemo = channelMemory[c];
				if (!aktMemo.wasITforced) aktMemo.muted = c!=channelNumber;
			}
			setNNAMuteStatus();
		}
	}
	/**
	 * @since 28.11.2023
	 * @return an array representing the mute status
	 */
	public boolean [] getMuteStatus()
	{
		final boolean [] mutedChannels = new boolean [maxChannels];
		for (int c=0; c<maxChannels; c++)
		{
			mutedChannels[c] = channelMemory[c].muted;
		}
		return mutedChannels;
	}
	/**
	 * @since 28.11.2023
	 * @param listener
	 */
	public void registerUpdateListener(final ModUpdateListener listener)
	{
		if (listeners!=null && !listeners.contains(listener)) listeners.add(listener);
	}
	/**
	 * @since 28.11.2023
	 * @param listener
	 */
	public void deregisterUpdateListener(final ModUpdateListener listener)
	{
		if (listeners!=null && listeners.contains(listener)) listeners.remove(listener);
	}
	/**
	 * @since 28.11.2023
	 * @param newFireUpdates
	 */
	public void setFireUpdates(final boolean newFireUpdates)
	{
		fireUpdates = newFireUpdates;
		fireInformationUpdate(fireUpdates);
	}
	/**
	 * @since 28.11.2023
	 * @return
	 */
	public boolean getFireUpdates()
	{
		return fireUpdates;
	}
	/**
	 * Pattern Update Position
	 * @since 28.11.2023
	 * @param sampleRate
	 * @param samplesMixed
	 * @param position
	 */
	public void firePositionUpdate(final int sampleRate, final long samplesMixed, final long position)
	{
		if (listeners!=null && fireUpdates)
		{
			PositionInformation information = new PositionInformation(sampleRate, samplesMixed, position);
			for (ModUpdateListener listener : listeners)
			{
				listener.getPositionInformation(information);
			}
		}
	}
	/**
	 * Volume at a certain position
	 * @since 28.11.2023
	 * @param sampleRate
	 * @param samplesMixed
	 * @param channel
	 * @param actPeekLeft
	 * @param actPeekRight
	 */
	public void firePeekUpdate(final int sampleRate, final long samplesMixed, final int channel, final long actPeekLeft, final long actPeekRight, final boolean isSurround)
	{
		if (listeners!=null && fireUpdates)
		{
			PeekInformation information = new PeekInformation(sampleRate, samplesMixed, channel, actPeekLeft, actPeekRight, isSurround);
			for (ModUpdateListener listener : listeners)
			{
				listener.getPeekInformation(information);
			}
		}
	}
	/**
	 * We started or stopped to fire updates.
	 * @since 28.11.2023
	 * @param newStatus
	 */
	public void fireInformationUpdate(final boolean newStatus)
	{
		if (listeners!=null)
		{
			StatusInformation information = new StatusInformation(newStatus);
			for (ModUpdateListener listener : listeners)
			{
				listener.getStatusInformation(information);
			}
		}
	}
}
