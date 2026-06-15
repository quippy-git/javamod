/*
 * @(#) ModMidiMixer.java
 *
 * Created on 14.05.2026 by Daniel Becker
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
package de.quippy.javamod.multimedia.mod.midi;

import java.io.File;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.SysexMessage;

import de.quippy.javamod.multimedia.mod.ModConstants;
import de.quippy.javamod.multimedia.mod.loader.instrument.Instrument;
import de.quippy.javamod.multimedia.mod.mixer.ChannelMemory;
import de.quippy.javamod.system.Log;

/**
 * @author Daniel Becker
 * @since 14.05.2026
 */
public class ModMidiMixer
{
	public static final int MIDI_NOTE_MASK     = 0x0FF;
	public static final int MIDI_NOTE_OFF      = 0x100;  // Send note-off for a specific note
	public static final int MIDI_NOTE_ARPEGGIO = 0x200;  // Note is part of an arpeggio, don't store it as the last triggered note
	
	public static final int PLUGIN_VELOCITYHANDLING_CHANNEL		= 0;
	public static final int PLUGIN_VELOCITYHANDLING_VOLUME		= 1;

	public static final int PLUGIN_VOLUMEHANDLING_MIDI			= 0;
	public static final int PLUGIN_VOLUMEHANDLING_DRYWET		= 1;
	public static final int PLUGIN_VOLUMEHANDLING_IGNORE		= 2;
	public static final int PLUGIN_VOLUMEHANDLING_CUSTOM		= 3;
	public static final int PLUGIN_VOLUMEHANDLING_MAX			= 4;
	public static final String[] PLUGIN_VOLUMEHANDLING_NAMES =
	{
	 "MIDI Volume", "Dry/Wet ratio", "None", "Custom", "Max"
	};

	private static final int PITCHBENDSHIFT		= 12;
	private static final int PITCHBENDMASK		= (~1);
	private static final int VIBRATOFLAG		= 1;
	private static final int PITCHBENDMIN		= 0x00;
	private static final int PITCHBENDCENTER	= 0x2000;
	private static final int PITCHBENDMAX		= 0x3FFF;


	public static final int MIDICC_BankSelect_Coarse = 0;
	public static final int MIDICC_ModulationWheel_Coarse = 1;
	public static final int MIDICC_Breathcontroller_Coarse = 2;
	public static final int MIDICC_FootPedal_Coarse = 4;
	public static final int MIDICC_PortamentoTime_Coarse = 5;
	public static final int MIDICC_DataEntry_Coarse = 6;
	public static final int MIDICC_Volume_Coarse = 7;
	public static final int MIDICC_Balance_Coarse = 8;
	public static final int MIDICC_Panposition_Coarse = 10;
	public static final int MIDICC_Expression_Coarse = 11;
	public static final int MIDICC_EffectControl1_Coarse = 12;
	public static final int MIDICC_EffectControl2_Coarse = 13;
	public static final int MIDICC_GeneralPurposeSlider1 = 16;
	public static final int MIDICC_GeneralPurposeSlider2 = 17;
	public static final int MIDICC_GeneralPurposeSlider3 = 18;
	public static final int MIDICC_GeneralPurposeSlider4 = 19;
	public static final int MIDICC_BankSelect_Fine = 32;
	public static final int MIDICC_ModulationWheel_Fine = 33;
	public static final int MIDICC_Breathcontroller_Fine = 34;
	public static final int MIDICC_FootPedal_Fine = 36;
	public static final int MIDICC_PortamentoTime_Fine = 37;
	public static final int MIDICC_DataEntry_Fine = 38;
	public static final int MIDICC_Volume_Fine = 39;
	public static final int MIDICC_Balance_Fine = 40;
	public static final int MIDICC_Panposition_Fine = 42;
	public static final int MIDICC_Expression_Fine = 43;
	public static final int MIDICC_EffectControl1_Fine = 44;
	public static final int MIDICC_EffectControl2_Fine = 45;
	public static final int MIDICC_HoldPedal_OnOff = 64;
	public static final int MIDICC_Portamento_OnOff = 65;
	public static final int MIDICC_SustenutoPedal_OnOff = 66;
	public static final int MIDICC_SoftPedal_OnOff = 67;
	public static final int MIDICC_LegatoPedal_OnOff = 68;
	public static final int MIDICC_Hold2Pedal_OnOff = 69;
	public static final int MIDICC_SoundVariation = 70;
	public static final int MIDICC_SoundTimbre = 71;
	public static final int MIDICC_SoundReleaseTime = 72;
	public static final int MIDICC_SoundAttackTime = 73;
	public static final int MIDICC_SoundBrightness = 74;
	public static final int MIDICC_SoundControl6 = 75;
	public static final int MIDICC_SoundControl7 = 76;
	public static final int MIDICC_SoundControl8 = 77;
	public static final int MIDICC_SoundControl9 = 78;
	public static final int MIDICC_SoundControl10 = 79;
	public static final int MIDICC_GeneralPurposeButton1_OnOff = 80;
	public static final int MIDICC_GeneralPurposeButton2_OnOff = 81;
	public static final int MIDICC_GeneralPurposeButton3_OnOff = 82;
	public static final int MIDICC_GeneralPurposeButton4_OnOff = 83;
	public static final int MIDICC_EffectsLevel = 91;
	public static final int MIDICC_TremoloLevel = 92;
	public static final int MIDICC_ChorusLevel = 93;
	public static final int MIDICC_CelesteLevel = 94;
	public static final int MIDICC_PhaserLevel = 95;
	public static final int MIDICC_DataButtonincrement = 96;
	public static final int MIDICC_DataButtondecrement = 97;
	public static final int MIDICC_NonRegisteredParameter_Fine = 98;
	public static final int MIDICC_NonRegisteredParameter_Coarse = 99;
	public static final int MIDICC_RegisteredParameter_Fine = 100;
	public static final int MIDICC_RegisteredParameter_Coarse = 101;
	public static final int MIDICC_AllSoundOff = 120;
	public static final int MIDICC_AllControllersOff = 121;
	public static final int MIDICC_LocalKeyboard_OnOff = 122;
	public static final int MIDICC_AllNotesOff = 123;
	public static final int MIDICC_OmniModeOff = 124;
	public static final int MIDICC_OmniModeOn = 125;
	public static final int MIDICC_MonoOperation = 126;
	public static final int MIDICC_PolyOperation = 127;
	
	private class MidiChannelMemory
	{
		public int lastMidiBank, lastMidiProgram, lastMidiNote;
		public int midiPitchBendPos;
		public int[][] noteOnMap;
		
		public MidiChannelMemory(final int modChannels)
		{
			resetMidiChannelMemory(modChannels);
		}
		public void resetMidiChannelMemory(final int modChannels)
		{
			lastMidiProgram = lastMidiBank = -1;
			lastMidiNote = ModConstants.NO_NOTE;
			midiPitchBendPos = PITCHBENDCENTER<<PITCHBENDSHIFT;

			if (noteOnMap==null || noteOnMap[0]==null || noteOnMap[0].length!=modChannels)
				noteOnMap = new int[128][modChannels];
			else
			{
				for (int[] channelNoteOnMap : noteOnMap)
				{
					for (int channel=0; channel<channelNoteOnMap.length; channel++)
						channelNoteOnMap[channel]=0;
				}
			}
		}
	}
	
	public class RawMidiMessage extends MidiMessage
	{
		protected RawMidiMessage(byte[] data)
		{
			super(data);
		}

		@Override
		public Object clone()
		{
	        byte[] newData = new byte[length];
	        System.arraycopy(data, 0, newData, 0, newData.length);
			return new RawMidiMessage(data);
		}
	}
	
	private final MidiDevice.Info outputDeviceInfo;
	private final File soundBankFile;
	private MidiDevice midiOutput;
	private Receiver receiver;
	private MidiChannelMemory[] midiChan;

	/**
	 * Constructor for ModMidiMixer
	 */
	public ModMidiMixer(final MidiDevice.Info outputDeviceInfo, final File soundBankFile, final int modChannels)
	{
		super();
		this.midiOutput = null;
		this.outputDeviceInfo = outputDeviceInfo;
		this.soundBankFile = soundBankFile;
		resetMidiMixer(modChannels);
	}
	public void resetMidiMixer(final int modChannels)
	{
		if (midiChan==null) midiChan = new MidiChannelMemory[16];
		for (int i=0, len=midiChan.length; i<len; i++)
		{
			if (midiChan[i]==null)
				midiChan[i] = new MidiChannelMemory(modChannels);
			else
				midiChan[i].resetMidiChannelMemory(modChannels);
		}
	}
	public void openOutputDevice()
	{
		try
		{
			if (midiOutput!=null) closeOuptutDevice();
			
			midiOutput = MidiSystem.getMidiDevice(outputDeviceInfo);
			if (!midiOutput.isOpen()) midiOutput.open();
			
			// if this midi output is a Synthesizer and we have a soundbank file, load it
			// this is true for Gervill, the standard java midi device
			if (midiOutput instanceof Synthesizer && soundBankFile!=null)
			{
				try
				{
					final Soundbank bank = MidiSystem.getSoundbank(soundBankFile);
					((Synthesizer)midiOutput).loadAllInstruments(bank);
				}
				catch (final Exception ex)
				{
					Log.error("Error occured when opening soundfont bank", ex);
				}
			}

			// now open a receiver where we can send midi events to:
			receiver = midiOutput.getReceiver();
		}
		catch (final MidiUnavailableException ex)
		{
			closeOuptutDevice();
			Log.error("Error occured when opening midi device", ex);
		}
	}
	public void closeOuptutDevice()
	{
		if (receiver!=null) receiver.close();
		if (midiOutput!=null && midiOutput.isOpen()) midiOutput.close();
		midiOutput = null;
		receiver = null;
	}
	/**
	 * @since 29.05.2026
	 * @param data
	 */
	public void sendSysExToReceiver(final byte[] data)
	{
		try
		{
			if (receiver!=null)
				receiver.send(new SysexMessage(data, data.length), -1);
		}
		catch (Throwable ex)
		{
			Log.error("[ModMidiMixher]::sendSysExToReceiver", ex);
		}
		
	}
	/**
	 * @since 28.05.2026
	 * @param data
	 */
	public void sendToReceiver(final byte[] data)
	{
		try
		{
			if (receiver!=null)
				receiver.send(new RawMidiMessage(data), -1);
		}
		catch (Throwable ex)
		{
			Log.error("[ModMidiMixher]::sendToReceiver", ex);
		}
	}
	/**
	 * @since 23.05.2026
	 * @param command
	 * @param channel
	 * @param data1
	 * @param data2
	 */
	public void sendToReceiver(final int command, final int channel, final int data1, final int data2)
	{
		try
		{
			if (receiver!=null)
				receiver.send(new ShortMessage(command, channel, data1, data2), -1);
		}
		catch (InvalidMidiDataException ex)
		{
			Log.error("[ModMidiMixher]::sendToReceiver", ex);
		}
	}
	/**
	 * @since 17.05.2026
	 * @param aktMemo
	 * @param note
	 * @return
	 */
	public boolean isNotePlaying(final ChannelMemory aktMemo, final int note)
	{
		final Instrument instrument = aktMemo.currentAssignedInstrument;
		if (instrument==null) return false;

		final MidiChannelMemory midiMemo = midiChan[instrument.getMidiChannel(aktMemo.channelNumber)];
		final int trkChannel = aktMemo.channelNumber;
		
		if (note<=ModConstants.NO_NOTE || trkChannel>midiMemo.noteOnMap[note].length)
			return false;

		return (midiMemo.noteOnMap[note - ModConstants.NOTE_MIN][trkChannel] != 0);
	}
	/**
	 * @since 21.05.2026
	 * @param aktMemo
	 * @return
	 */
	private static int getMidiChannel(final ChannelMemory aktMemo)
	{
		if (aktMemo!=null)
		{
			final Instrument instrument = aktMemo.currentAssignedInstrument;
			if (instrument!=null && instrument.hasValidMidiChannel())
				return instrument.getMidiChannel(aktMemo.channelNumber);
		}
		return 0;
	}
	/**
	 * @since 21.05.2026
	 * @param value
	 * @param pwd
	 * @return
	 */
	private int applyPitchWheelDepth(final int value, final int pwd)
	{
		if(pwd != 0)
			return (value * ((PITCHBENDMAX - PITCHBENDCENTER + 1) / 64)) / pwd;
		else
			return 0;
	}
	/**
	 * Set MIDI pitch for given MIDI channel to the specified raw 14-bit position
	 * @since 17.05.2026
	 * @param midiChannel
	 * @param newPitchBendPos
	 */
	private void sendMidiPitchBend(final int midiChannel, final int newPitchBendPos)
	{
		final int change = newPitchBendPos >> PITCHBENDSHIFT;
		if (change>=PITCHBENDMIN && change<=PITCHBENDMAX)
		{
		    final int low = change & 0x7F;
		    final int high = (change >> 7) & 0x7F;
		    midiChan[midiChannel].midiPitchBendPos = newPitchBendPos;
			sendToReceiver(ShortMessage.PITCH_BEND, midiChannel, low, high);
		}
	}
	/**
	 * Set MIDI pitch for given MIDI channel to the specified raw 14-bit position
	 * @since 21.05.2026
	 * @param aktMemo
	 * @param pitchBend
	 */
	public void midiPitchBendRaw(final ChannelMemory aktMemo, final int pitchBend)
	{
		sendMidiPitchBend(getMidiChannel(aktMemo), ((pitchBend>PITCHBENDMAX)?PITCHBENDMAX:(pitchBend<PITCHBENDMIN)?PITCHBENDMIN:pitchBend)<<PITCHBENDSHIFT);
	}
	/**
	 * Bend MIDI pitch for given MIDI channel using fine tracker param (one unit = 1/64th of a note step)
	 * @since 21.05.2026
	 * @param aktMemo
	 * @param increment
	 * @param pwd
	 */
	public void midiPitchBend(final ChannelMemory aktMemo, final int inc, final int pwd)
	{
		int increment;
//		if(m_SndFile.m_playBehaviour[kOldMIDIPitchBends]) // TODO: where is this set 
//		{
//			// OpenMPT Legacy: Old pitch slides never were really accurate, but setting the PWD to 13 in plugins would give the closest results.
//			increment = (inc * 0x800 * 13) / (0xFF * pwd);
//			increment = increment<<PITCHBENDSHIFT;
//		}
//		else
//		{
			increment = inc<<PITCHBENDSHIFT;
			increment = applyPitchWheelDepth(increment, pwd);
//		}

		final int midiCh = getMidiChannel(aktMemo);
		int newPitchBendPos = (increment + midiChan[midiCh].midiPitchBendPos) & PITCHBENDMASK;
		newPitchBendPos = (newPitchBendPos>(PITCHBENDMAX<<PITCHBENDSHIFT))?PITCHBENDMAX<<PITCHBENDSHIFT:(newPitchBendPos<(PITCHBENDMIN<<PITCHBENDSHIFT))?PITCHBENDMIN<<PITCHBENDSHIFT:newPitchBendPos;
		sendMidiPitchBend(midiCh, newPitchBendPos);
	}
	/**
	 * @since 21.05.2026
	 * @param aktMemo
	 * @param inc
	 * @param newNote
	 * @param pwd
	 */
	public void midiTonePortamento(final ChannelMemory aktMemo, final int inc, final int newNote, final int pwd)
	{
		final int midiCh = getMidiChannel(aktMemo);
		int increment = inc;

		int targetBend = (64 * (newNote - midiChan[midiCh].lastMidiNote))<<PITCHBENDSHIFT;
		targetBend = applyPitchWheelDepth(targetBend, pwd);
		targetBend += PITCHBENDCENTER<<PITCHBENDSHIFT;

		if (targetBend<midiChan[midiCh].midiPitchBendPos)
			increment = -increment;
		increment <<= PITCHBENDSHIFT;
		increment = applyPitchWheelDepth(increment, pwd);

		int newPitchBendPos = (increment + midiChan[midiCh].midiPitchBendPos) & PITCHBENDMASK;
		if ((newPitchBendPos>targetBend && increment>0) || (newPitchBendPos<targetBend && increment<0))
			newPitchBendPos = targetBend;

		newPitchBendPos = (newPitchBendPos>(PITCHBENDMAX<<PITCHBENDSHIFT))?PITCHBENDMAX<<PITCHBENDSHIFT:(newPitchBendPos<(PITCHBENDMIN<<PITCHBENDSHIFT))?PITCHBENDMIN<<PITCHBENDSHIFT:newPitchBendPos;
		sendMidiPitchBend(midiCh, newPitchBendPos);
	}
	/**
	 * Apply vibrato effect through pitch wheel commands on a given MIDI channel.
	 * @since 21.05.2026
	 * @param aktMemo
	 * @param theDepth
	 * @param pwd
	 */
	public void midiVibrato(final ChannelMemory aktMemo, final int theDepth, final int pwd)
	{
		final int midiCh = getMidiChannel(aktMemo);
		int depth = theDepth<<PITCHBENDSHIFT;
		if (depth!= 0 || (midiChan[midiCh].midiPitchBendPos & VIBRATOFLAG)!=0)
		{
			depth = applyPitchWheelDepth(depth, pwd);

			// Temporarily add vibrato offset to current pitch
			int newPitchBendPos = (depth + midiChan[midiCh].midiPitchBendPos) & PITCHBENDMASK;
			newPitchBendPos = (newPitchBendPos>(PITCHBENDMAX<<PITCHBENDSHIFT))?PITCHBENDMAX<<PITCHBENDSHIFT:(newPitchBendPos<(PITCHBENDMIN<<PITCHBENDSHIFT))?PITCHBENDMIN<<PITCHBENDSHIFT:newPitchBendPos;
			sendMidiPitchBend(midiCh, newPitchBendPos);
		}

		// Update vibrato status
		if(depth != 0)
			midiChan[midiCh].midiPitchBendPos |= VIBRATOFLAG;
		else
			midiChan[midiCh].midiPitchBendPos &= ~VIBRATOFLAG;
	}
	/**
	 * Send portamento commands to plugins
	 * @since 21.05.2026
	 * @param aktMemo
	 * @param param
	 * @param doFineSlides
	 */
	public void midiPortamento(final ChannelMemory aktMemo, final int param, final boolean doFineSlides, final boolean firstTick)
	{
		final int actualParam = (param>0)?param:-param;
		int pitchBend = 0;

		// Old MIDI Pitch Bends:
		// - Applied on every tick
		// - No fine pitch slides (they are interpreted as normal slides)
		// New MIDI Pitch Bends:
		// - Behavior identical to sample pitch bends if the instrument's PWD parameter corresponds to the actual VSTi setting.
		if (doFineSlides && actualParam>=0xE0)
		{
			if (firstTick) // only first tick! - because of "else" not as && in if above
			{
				// Extra fine slide...
				pitchBend = (actualParam & 0x0F) * ((param<0)?-1:1);
				if(actualParam >= 0xF0)
				{
					pitchBend <<= 2; // ... or just a fine slide!
				}
			}
		}
		else
		{
			pitchBend = param << 2; // Regular slide
		}

		if (pitchBend!=0)
		{
			int pwd = 13; // Early OpenMPT legacy... Actually it's not *exactly* 13, but close enough...
			if (aktMemo.assignedInstrument!=null) pwd = aktMemo.assignedInstrument.pitchWheelDepth;
			midiPitchBend(aktMemo, pitchBend, pwd);
		}
	}
	/**
	 * @since 25.05.2026
	 * @param aktMemo
	 * @param isTick
	 */
	public void midiArpeggio(final ChannelMemory aktMemo, final int isTick)
	{
		if (aktMemo==null || aktMemo.muted || aktMemo.lastMidiNoteWithoutArp<=ModConstants.NO_NOTE) return;
		
		final Instrument instrument = aktMemo.currentAssignedInstrument;
		if (instrument==null || instrument.mute) return;
		
		int arpNote = aktMemo.lastMidiNoteWithoutArp;
		switch (isTick)
		{
			case 1 : arpNote += (aktMemo.arpeggioParam>>4)&0x0F; break;
			case 2 : arpNote += (aktMemo.arpeggioParam   )&0x0F; break;
		}
		// Arpeggio with velocity - either instrument global volume or channel volume
		if (arpNote!=aktMemo.arpeggioLastNote)
		{
			// when we enter this the first time, arpeggioLastNote should be NO_NOTE
			// In that case, lastMidiNoteWithoutArp (the base note) must be switched off
			// But if not, we played an arpeggio note, and need to switch that one off
			if (aktMemo.arpeggioLastNote>ModConstants.NO_NOTE)
			{
				if (aktMemo.arpeggioLastNote!=arpNote)
					sendMidiNote(aktMemo, aktMemo.arpeggioLastNote | MIDI_NOTE_OFF, 0);
			}
			else
			{
				sendMidiNote(aktMemo, aktMemo.lastMidiNoteWithoutArp | MIDI_NOTE_OFF, 0);
			}
			// Now turn the new note on:
			sendMidiNote(aktMemo, arpNote | MIDI_NOTE_ARPEGGIO, (instrument.pluginVelocityHandling == PLUGIN_VELOCITYHANDLING_CHANNEL) ? aktMemo.currentVolume<<2 : instrument.globalVolume<<1);
			aktMemo.arpeggioLastNote = arpNote;
		}
	}
	/**
	 * @since 17.05.2026
	 * @param aktMemo the current Mod Channel Memory
	 * @param instrument the instrument at hand
	 * @param note 0-127
	 * @param volume 0-256! - not midi range!
	 */
	public void midiCommand(final ChannelMemory aktMemo, final Instrument instrument, final int note, final int vol)
	{
		//if (aktMemo==null || instrument==null) return;
		
		int rawNote = note & MIDI_NOTE_MASK;
		final int midiBank = instrument.midiBank - 1;
		final int midiProg = instrument.midiProgram - 1;
		
		final int trkChannel = aktMemo.channelNumber;
		final int midiChannel = instrument.getMidiChannel(aktMemo.channelNumber);
		final MidiChannelMemory midiMemo = midiChan[midiChannel];
		final int maxNote = midiMemo.noteOnMap.length;
		final boolean bankChanged = midiMemo.lastMidiBank != midiBank && (midiBank< 0x4000);
		final boolean progChanged = midiMemo.lastMidiProgram != midiProg && (midiProg < 0x80);

		int volume = (vol+1)>>1; if (volume>127) volume=127;
		
		// Bank Change
		if (bankChanged)
		{
			sendToReceiver(ShortMessage.CONTROL_CHANGE, midiChannel, MIDICC_BankSelect_Coarse, (midiBank >> 7) & 0x7F);
			sendToReceiver(ShortMessage.CONTROL_CHANGE, midiChannel, MIDICC_BankSelect_Fine, midiBank & 0x7F);
			midiMemo.lastMidiBank = midiBank;
		}
		// Program change
		// According to the MIDI specs, a bank change alone doesn't have to change the active program - it will only change the bank of subsequent program changes.
		// Thus we send program changes also if only the bank has changed.
		if (progChanged || bankChanged)
		{
			sendToReceiver(ShortMessage.PROGRAM_CHANGE, midiChannel, midiProg, 0);
			midiMemo.lastMidiProgram = midiProg;
		}
		
		if (note==ModConstants.NOTE_CUT) // ^^
		{
			// "Hard core" All Sounds Off on this midi and tracker channel
			// This one doesn't check the note mask - just one note off per note.
			// Also less likely to cause a VST event buffer overflow.
			sendToReceiver(ShortMessage.CONTROL_CHANGE, midiChannel, MIDICC_AllNotesOff, 0);
			sendToReceiver(ShortMessage.CONTROL_CHANGE, midiChannel, MIDICC_AllSoundOff, 0);
			for (int n=0; n<maxNote; n++)
			{
				midiMemo.noteOnMap[n][trkChannel] = 0;
				sendToReceiver(ShortMessage.NOTE_OFF, midiChannel, n, volume);
			}
		}
		else
		if (note==ModConstants.KEY_OFF || note==ModConstants.NOTE_FADE)  // ==, ~~
		{
			for(int n=0; n<maxNote; n++)
			{
				while(midiMemo.noteOnMap[n][trkChannel]>0)
				{
					midiMemo.noteOnMap[n][trkChannel]--;
					sendToReceiver(ShortMessage.NOTE_OFF, midiChannel, n, 0);
				}
			}
		}
		else
		if (rawNote>=ModConstants.NOTE_MIN && rawNote<=maxNote) // NOTE_ON or NOTE_OFF
		{
			// Specific Note Off
			if ((note & MIDI_NOTE_OFF)!=0)
			{
				rawNote -= ModConstants.NOTE_MIN;
				if (midiChan[midiChannel].noteOnMap[rawNote][trkChannel]>0)
				{
					midiMemo.noteOnMap[rawNote][trkChannel]--;
					sendToReceiver(ShortMessage.NOTE_OFF, midiChannel, rawNote, 0);
				}
			}
			else
			// Note On
			{
				if ((note & MIDI_NOTE_ARPEGGIO)==0)
				{
					aktMemo.lastMidiNoteWithoutArp = midiMemo.lastMidiNote = rawNote; // NO -Note_MIN

					// Reset pitch bend on each new note, tracker style.
					// This is done if the pitch wheel has been moved or there was a vibrato on the previous row (in which case the "vstVibratoFlag" bit of the pitch bend memory is set)
					int newPitchBendPos = aktMemo.getMIDIPitchBend();
					if (newPitchBendPos<PITCHBENDMIN) newPitchBendPos = PITCHBENDMIN;
					else
					if (newPitchBendPos>PITCHBENDMAX) newPitchBendPos = PITCHBENDMAX;
					newPitchBendPos<<=PITCHBENDSHIFT;
					if(midiMemo.midiPitchBendPos != newPitchBendPos)
					{
						sendMidiPitchBend(midiChannel, newPitchBendPos);
					}
				}

				rawNote -= ModConstants.NOTE_MIN;
				if (midiMemo.noteOnMap[rawNote][trkChannel] < Integer.MAX_VALUE)
					midiMemo.noteOnMap[rawNote][trkChannel]++;
				sendToReceiver(ShortMessage.NOTE_ON, midiChannel, rawNote, volume);
			}
		}
	}
	/**
	 * @since 17.05.2026
	 * @param aktMemo
	 * @param note
	 * @param volume: 0..256 tracker volume
	 */
	public void sendMidiNote(final ChannelMemory aktMemo, final int note, final int volume)
	{
		final Instrument instrument = aktMemo.currentAssignedInstrument;
		if (instrument!=null && instrument.hasValidMidiChannel())
		{
			midiCommand(aktMemo, instrument, note, volume);
			if (note>ModConstants.NO_NOTE)
			{
				aktMemo.midiVULeft =  (volume * (256-aktMemo.panning))>>8;
				aktMemo.midiVURight = (volume * (    aktMemo.panning))>>8;
			}
		}
	}
	/**
	 * @since 17.05.2026
	 * @param aktMemo
	 * @param note - we do not change XMs to match IT noteindex - so we give the note index in (XM one octave higher)
	 * @param trackerVolCommand and as the effect parameters are not equal as well, we need to let us know here
	 * @param trackerVolColCommand dito!
	 */
	public void processMidiOut(final ChannelMemory aktMemo, final int noteCorrection, final int trackerVolCommand, final int trackerVolColCommand, final boolean isPortaToNoteEffekt)
	{
		if (aktMemo==null || aktMemo.muted) return;
		
		final Instrument instrument = aktMemo.currentAssignedInstrument;
		if (instrument==null || instrument.mute) return;
		
		// check for volume commands
		int commandVolume = 0xFF;
		if (aktMemo.assignedVolumeEffekt==trackerVolColCommand)
			commandVolume = aktMemo.assignedVolumeEffektOp;
		else
		if (aktMemo.assignedEffekt==trackerVolCommand)
			commandVolume = aktMemo.assignedEffektParam;
		if (commandVolume!=0xFF)
		{
			if (commandVolume>ModConstants.MAX_SAMPLE_VOL) commandVolume = ModConstants.MAX_SAMPLE_VOL;
			else
			if (commandVolume<ModConstants.MIN_SAMPLE_VOL) commandVolume = ModConstants.MIN_SAMPLE_VOL;
		}
		final boolean hasVolumeCommand = commandVolume!=0xFF;

		final int defaultVolume = instrument.globalVolume; // 0..128
		int note = aktMemo.currentElement.getNoteIndex();
		
		// send a new note, of given
		if (note!=ModConstants.NO_NOTE)
		{
			int velocity = defaultVolume<<1;
			// PLUGIN Velocity Handling
			if (instrument.pluginVelocityHandling == PLUGIN_VELOCITYHANDLING_CHANNEL)
			{
				// volume events weren't processed yet
				// How OMPT does this is unclear. processMidiOut is called right before 
				// CMD_VOLUME is processed. Only volcommand volumes are considered before
				if (hasVolumeCommand)
					velocity = commandVolume<<2;
				else
					velocity = aktMemo.currentVolume<<2; // 0..64 --> 0..256
			}
			
			velocity += aktMemo.swingVolume<<2; // 0..64 --> 0..256
			if (velocity<0) velocity = 0; else if (velocity>256) velocity = 256;
			
			if (note>ModConstants.NO_NOTE)
				note = aktMemo.assignedInstrument.getNoteIndex(note - ModConstants.NOTE_MIN) + ModConstants.NOTE_MIN + noteCorrection;

			if (!isPortaToNoteEffekt)
				sendMidiNote(aktMemo, note, velocity);
		}

		final boolean processVolumeAlsoOnNote = (instrument.pluginVelocityHandling == PLUGIN_VELOCITYHANDLING_VOLUME);
		if ((hasVolumeCommand && note==ModConstants.NO_NOTE) || (note>ModConstants.NO_NOTE && processVolumeAlsoOnNote))
		{
			final int midiChannel = instrument.getMidiChannel(aktMemo.channelNumber);
			switch(instrument.pluginVolumeHandling)
			{
				case PLUGIN_VOLUMEHANDLING_DRYWET: // TODO: dry / wet mix
//					if (hasVolumeCommand)
//						pPlugin->SetDryRatio(1.0f - (2 * vol) / 127.0f);
//					else
//						pPlugin->SetDryRatio(1.0f - static_cast<float>(2 * defaultVolume) / 127.0f);
					break;
				case PLUGIN_VOLUMEHANDLING_MIDI:
					if (hasVolumeCommand)
					{
						commandVolume<<=1;
						sendToReceiver(ShortMessage.CONTROL_CHANGE, midiChannel, MIDICC_Volume_Coarse, (commandVolume>127)?127:commandVolume);
					}
					else
					{
						sendToReceiver(ShortMessage.CONTROL_CHANGE, midiChannel, MIDICC_Volume_Coarse, (defaultVolume>127)?127:defaultVolume);
					}
					break;
				default:
					break;
			}
		}
	}
}
