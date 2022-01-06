/*
 * @(#) ROLSequence.java
 * 
 * Created on 03.08.2020 by Daniel Becker
 * 
 * -----------------------------------------------------------------------
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * ----------------------------------------------------------------------
 * 
 * Java port of ROL.CPP by OPLx for adplug project
 * 
 * Java port and optimizations by Daniel Becker
 * - verified loading against my old Effekter loading routine
 *   -> check for section start during load - some ROLs are corrupt!
 *   -> That the fillers contain the name of following section is undocumented
 *      but AdLib Composer wrote those
 * - volume calculation optimized
 * - unnecessary event checking during play removed.
 *   If there are no more events (array size exceeded), we find out
 *   without setting a marker
 * - play till real end, not only till last note (possible pitch event after 
 *   last note would be ignored)
 * - no table for note index or octave needed. Lookup takes same time as
 *   calculation (index = note%12; octave = note/12)
 */
package de.quippy.javamod.multimedia.opl3.sequencer;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import de.quippy.javamod.io.RandomAccessInputStreamImpl;
import de.quippy.javamod.multimedia.MultimediaContainerManager;
import de.quippy.javamod.multimedia.opl3.emu.EmuOPL;
import de.quippy.javamod.multimedia.opl3.emu.EmuOPL.oplType;
import de.quippy.javamod.system.Helpers;

/**
 * @author Daniel Becker
 * @since 03.08.2020
 */
public class ROLSequence extends OPL3Sequence
{
	private static final int ROL_COMMENT_SIZE					= 40;
//	private static final int ROL_UNUSED1_SIZE					= 1;
	private static final int ROL_UNUSED2_SIZE					= 90;
	private static final int ROL_FILLER0_SIZE					= 38;
	private static final int ROL_FILLER1_SIZE					= 15;
	private static final int ROL_FILLER_SIZE					= 15;
	private static final int ROL_MAX_NAME_SIZE					= 9;
	private static final int ROL_INSTRUMENT_EVENT_FILLER_SIZE	= 3; // 1 for filler, 2 for unused
	private static final int ROL_BNK_SIGNATURE_SIZE = 6;
	// ---------------------------------------------------------
	private static final int skMidPitch						= 0x2000;
	private static final int skNrStepPitch					= 25; // 25 steps within a half-tone for pitch bend
	private static final int skVersionMajor					= 0;
	private static final int skVersionMinor					= 4;
	private static final int skVolumeQualityShift			= 7;
	private static final int skMaxVolume					= (1 << skVolumeQualityShift);
	private static final int skMaxNotes						= 96;
	private static final int skCarrierOpOffset				= 3;
	private static final int skNumSemitonesInOctave			= 12;
	// ---------------------------------------------------------
	private static final int skOPL2_WaveCtrlBaseAddress		= 0x01; // Test LSI / Enable waveform control
	private static final int skOPL2_AaMultiBaseAddress		= 0x20; // Amp Mod / Vibrato / EG type / Key Scaling / Multiple
	private static final int skOPL2_KSLTLBaseAddress		= 0x40; // Key scaling level / Operator output level
	private static final int skOPL2_ArDrBaseAddress			= 0x60; // Attack Rate / Decay Rate
	private static final int skOPL2_SlrrBaseAddress			= 0x80; // Sustain Level / Release Rate
	private static final int skOPL2_FreqLoBaseAddress		= 0xA0; // Frequency (low 8 bits)
	private static final int skOPL2_KeyOnFreqHiBaseAddress	= 0xB0; // Key On / Octave / Frequency (high 2 bits)
	private static final int skOPL2_AmVibRhythmBaseAddress 	= 0xBD; // AM depth / Vibrato depth / Rhythm control
	private static final int skOPL2_FeedConBaseAddress		= 0xC0; // Feedback strength / Connection type
	private static final int skOPL2_WaveformBaseAddress 	= 0xE0; // Waveform select
	// ---------------------------------------------------------
	private static final int skOPL2_EnableWaveformSelectMask= 0x20;
	private static final int skOPL2_KeyOnMask 				= 0x20;
	private static final int skOPL2_RhythmMask				= 0x20;
	private static final int skOPL2_KSLMask					= 0xC0;
	private static final int skOPL2_TLMask					= 0x3F;
	private static final int skOPL2_TLMinLevel				= 0x3F;
	private static final int skOPL2_FNumLSBMask				= 0xFF;
	private static final int skOPL2_FNumMSBMask				= 0x03;
	private static final int skOPL2_FNumMSBShift			= 0x08;
	private static final int skOPL2_BlockNumberShift		= 0x02;
	// ---------------------------------------------------------
	private 			 String FILLER_NOTE_SECTION			= "Voix"; // Effekter uses "Notes" here - my bad!
	private static final String FILLER_EFFEKTER_SECTION		= "Notes";
	private static final String FILLER_TIMBRE_SECTION		= "Timbre";
	private static final String FILLER_VOLUME_SECTION		= "Volume";
	private static final String FILLER_PITCH_SECTION		= "Pitch";
	// ---------------------------------------------------------
	private static final String EFFEKTER_MAGIC_STRING		= "EFFEKTER"; // We can handle my own written effekter files.
	// ---------------------------------------------------------
	// Table below generated by initialize_fnum_table function (from Adlib Music SDK).
	private static final int skFNumNotes[]/* [skNrStepPitch][skNumSemitonesInOctave] */ =
	{
		343, 364, 385, 408, 433, 459, 486, 515, 546, 579, 614, 650,
		344, 365, 387, 410, 434, 460, 488, 517, 548, 581, 615, 652,
		345, 365, 387, 410, 435, 461, 489, 518, 549, 582, 617, 653,
		346, 366, 388, 411, 436, 462, 490, 519, 550, 583, 618, 655,
		346, 367, 389, 412, 437, 463, 491, 520, 551, 584, 619, 657,
		347, 368, 390, 413, 438, 464, 492, 522, 553, 586, 621, 658,
		348, 369, 391, 415, 439, 466, 493, 523, 554, 587, 622, 660,
		349, 370, 392, 415, 440, 467, 495, 524, 556, 589, 624, 661,
		350, 371, 393, 416, 441, 468, 496, 525, 557, 590, 625, 663,
		351, 372, 394, 417, 442, 469, 497, 527, 558, 592, 627, 665,
		351, 372, 395, 418, 443, 470, 498, 528, 559, 593, 628, 666,
		352, 373, 396, 419, 444, 471, 499, 529, 561, 594, 630, 668,
		353, 374, 397, 420, 445, 472, 500, 530, 562, 596, 631, 669,
		354, 375, 398, 421, 447, 473, 502, 532, 564, 597, 633, 671,
		355, 376, 398, 422, 448, 474, 503, 533, 565, 599, 634, 672,
		356, 377, 399, 423, 449, 475, 504, 534, 566, 600, 636, 674,
		356, 378, 400, 424, 450, 477, 505, 535, 567, 601, 637, 675,
		357, 379, 401, 425, 451, 478, 506, 537, 569, 603, 639, 677,
		358, 379, 402, 426, 452, 479, 507, 538, 570, 604, 640, 679,
		359, 380, 403, 427, 453, 480, 509, 539, 571, 606, 642, 680,
		360, 381, 404, 428, 454, 481, 510, 540, 572, 607, 643, 682,
		360, 382, 405, 429, 455, 482, 511, 541, 574, 608, 645, 683,
		361, 383, 406, 430, 456, 483, 512, 543, 575, 610, 646, 685,
		362, 384, 407, 431, 457, 484, 513, 544, 577, 611, 648, 687,
		363, 385, 408, 432, 458, 485, 514, 545, 578, 612, 649, 688
	};
	// ---------------------------------------------------------
	private static final int drum_op_table[] =
	{
		0x14, 0x12, 0x15, 0x11
	};
	private static final int op_table[] =
	{
		0x00, 0x01, 0x02, 0x08, 0x09, 0x0a, 0x10, 0x11, 0x12
	};
	// ---------------------------------------------------------
	private static final int kSizeofDataRecord		= 30;
	private static final int kMaxTickBeat 			= 60;
	private static final int kSilenceNote			= -12;
	private static final int kNumMelodicVoices		= 9;
	private static final int kNumPercussiveVoices	= 11;
	private static final int kBassDrumChannel		= 6;
	private static final int kSnareDrumChannel		= 7;
	private static final int kTomtomChannel			= 8;
	private static final int kTomTomNote			= 24;
	private static final int kTomTomToSnare			= 7; // 7 half-tones between voice 7 & 8
	private static final int kSnareNote				= kTomTomNote + kTomTomToSnare;
	private static final double kDefaultUpdateTme	= 18.2;

	// ---------------------------------------------------------
	private static final class SRolHeader
	{
		private int 	version_major;
		private int		version_minor;
		private String	comment;
		private int		ticks_per_beat;
		private int		beats_per_measure;
		private int		edit_scale_y;
		private int		edit_scale_x;
		private byte	unused1;
		private byte	mode;
		private byte[]	unused = new byte[ROL_UNUSED2_SIZE + ROL_FILLER0_SIZE + ROL_FILLER1_SIZE];
		private double	basic_tempo;

		private static SRolHeader readMe(RandomAccessInputStreamImpl inputStream) throws IOException
		{
			SRolHeader result = new SRolHeader();
			result.version_major = inputStream.readIntelWord();
			result.version_minor = inputStream.readIntelWord();
			result.comment = inputStream.readString(ROL_COMMENT_SIZE);
			result.ticks_per_beat = inputStream.readIntelWord();
			if (result.ticks_per_beat>kMaxTickBeat) result.ticks_per_beat = kMaxTickBeat;
			result.beats_per_measure = inputStream.readIntelWord();
			result.edit_scale_x = inputStream.readIntelWord();
			result.edit_scale_y = inputStream.readIntelWord();
			result.unused1 = inputStream.readByte();
			result.mode = inputStream.readByte();
			inputStream.read(result.unused);
			result.basic_tempo = (double)inputStream.readIntelFloat();
			return result;
		}
		@Override
		public String toString()
		{
			StringBuilder sb = new StringBuilder();
			sb.append("Version:").append(version_major).append('.').append(version_minor).
				append(" Comment:").append(comment).
				append(" Ticks: ").append(ticks_per_beat).append('/').append(beats_per_measure).
				append(" Edit scale: ").append(edit_scale_x).append('/').append(edit_scale_y).
				append(" Mode: ").append(mode).
				append(" Unused: [").append(unused1).append(", ").append(Arrays.toString(unused)).append(']');
			return sb.toString();
		}
	}

	private static final class STempoEvent
	{
		private int 	time;
		private double	multiplier;

		private static STempoEvent readMe(RandomAccessInputStreamImpl inputStream) throws IOException
		{
			STempoEvent event = new STempoEvent();
			event.time = inputStream.readIntelWord();
			event.multiplier = (double)inputStream.readIntelFloat();
			if (event.multiplier<0.01) event.multiplier = 0.01;
			else
			if (event.multiplier>10.0) event.multiplier = 10.0;
			return event;
		}
		@Override
		public String toString()
		{
			return "{" + time + ", " + multiplier + "}";
		}
	}

	private static final class SNoteEvent
	{
		private int number;
		private int duration;

		private static SNoteEvent readMe(RandomAccessInputStreamImpl inputStream) throws IOException
		{
			SNoteEvent event = new SNoteEvent();
			event.number = inputStream.readIntelWord();
			event.duration = inputStream.readIntelWord();
			event.number += kSilenceNote; // adding -12
			return event;
		}
		@Override
		public String toString()
		{
			return "{" + number + ", " + duration + "}";
		}
	}

	private static final class SInstrumentEvent
	{
		private int		time;
		private String	name;
		private int		ins_index;

		private static SInstrumentEvent readMe(RandomAccessInputStreamImpl inputStream) throws IOException
		{
			SInstrumentEvent event = new SInstrumentEvent();
			event.time = inputStream.readIntelWord();
			event.name = inputStream.readString(ROL_MAX_NAME_SIZE).toUpperCase();
			return event;
		}
		@Override
		public String toString()
		{
			return "{" + time + ", \"" + name + "\", " + ins_index + "}";
		}
	}

	private static final class SVolumeEvent
	{
		private int		time;
		private double	multiplier;

		private static SVolumeEvent readMe(RandomAccessInputStreamImpl inputStream) throws IOException
		{
			SVolumeEvent event = new SVolumeEvent();
			event.time = inputStream.readIntelWord();
			event.multiplier = (double)inputStream.readIntelFloat();
			if (event.multiplier<0) event.multiplier = 0;
			else
			if (event.multiplier>1.0) event.multiplier = 1.0;
			return event;
		}
		@Override
		public String toString()
		{
			return "{" + time + ", " + multiplier + "}";
		}
	}

	private static final class SPitchEvent
	{
		private int		time;
		private double	variation;

		private static SPitchEvent readMe(RandomAccessInputStreamImpl inputStream) throws IOException
		{
			SPitchEvent event = new SPitchEvent();
			event.time = inputStream.readIntelWord();
			event.variation = (double)inputStream.readIntelFloat();
			if (event.variation<0) event.variation = 0;
			else
			if (event.variation>2.0) event.variation = 2.0;
			return event;
		}
		@Override
		public String toString()
		{
			return "{" + time + ", " + variation + "}";
		}
	}

	private static final class CVoiceData
	{
		private ArrayList<SNoteEvent>		note_events;
		private ArrayList<SInstrumentEvent>	instrument_events;
		private ArrayList<SVolumeEvent>		volume_events;
		private ArrayList<SPitchEvent>		pitch_events;

		private int mNoteDuration;
		private int current_note_duration;
		private int next_note_event;
		private int next_instrument_event;
		private int next_volume_event;
		private int next_pitch_event;

		private CVoiceData()
		{
			reset();
		}

		private void reset()
		{
			mNoteDuration =
			current_note_duration = 
			next_note_event =
			next_instrument_event =
			next_volume_event =
			next_pitch_event = 0;
		}
	}

	// ---------------------------------------------------------
	private static final class SInstrumentName
	{
		private int		index;
		private byte	record_used;
		private String	name;

		private static SInstrumentName readMe(RandomAccessInputStreamImpl inputStream) throws IOException
		{
			SInstrumentName result = new SInstrumentName();
			result.index = inputStream.readIntelWord();
			result.record_used = inputStream.readByte();
			result.name = inputStream.readString(ROL_MAX_NAME_SIZE).toUpperCase();
			return result;
		}
		@Override
		public String toString()
		{
			return index + ". " + name + "[" + (record_used != 0 ? "X" : " ") + "]";
		}
	}

	private static final class SBnkHeader
	{
		private int		version_major;
		private int		version_minor;
		private String	signature;
		private int		number_of_list_entries_used;
		private int		total_number_of_list_entries;
		private long	abs_offset_of_name_list;
		private long	abs_offset_of_data;
		
		private ArrayList<SInstrumentName> ins_name_list;

		private static SBnkHeader readMe(RandomAccessInputStreamImpl inputStream) throws IOException
		{
			SBnkHeader result = new SBnkHeader();
			result.version_major = inputStream.read();
			result.version_minor = inputStream.read();
			result.signature = inputStream.readString(ROL_BNK_SIGNATURE_SIZE);
			result.number_of_list_entries_used = inputStream.readIntelWord();
			result.total_number_of_list_entries = inputStream.readIntelWord();
			result.abs_offset_of_name_list = inputStream.readIntelDWord();
			result.abs_offset_of_data = inputStream.readIntelDWord();
			result.ins_name_list = new ArrayList<SInstrumentName>(result.total_number_of_list_entries);

			inputStream.seek(result.abs_offset_of_name_list);
			for (int i = 0; i < result.total_number_of_list_entries; i++)
				result.ins_name_list.add(SInstrumentName.readMe(inputStream));

			return result;
		}
		@Override
		public String toString()
		{
			return signature + " V" + version_major + "." + version_minor + " " + number_of_list_entries_used + " of " + total_number_of_list_entries + " used";
		}
	}

	private static final class SFMOperator
	{
		private int key_scale_level;
		private int freq_multiplier;
		private int feed_back;
		private int attack_rate;
		private int sustain_level;
		private int sustaining_sound;
		private int decay_rate;
		private int release_rate;
		private int output_level;
		private int amplitude_vibrato;
		private int frequency_vibrato;
		private int envelope_scaling;
		private int fm_type;

		private static SFMOperator readMe(RandomAccessInputStreamImpl inputStream) throws IOException
		{
			SFMOperator result = new SFMOperator();
			result.key_scale_level = inputStream.read();
			result.freq_multiplier = inputStream.read();
			result.feed_back = inputStream.read();
			result.attack_rate = inputStream.read();
			result.sustain_level = inputStream.read();
			result.sustaining_sound = inputStream.read();
			result.decay_rate = inputStream.read();
			result.release_rate = inputStream.read();
			result.output_level = inputStream.read();
			result.amplitude_vibrato = inputStream.read();
			result.frequency_vibrato = inputStream.read();
			result.envelope_scaling = inputStream.read();
			result.fm_type = inputStream.read();
			return result;
		}
	}

	private static final class SOPL2Op
	{
		private int ammulti;
		private int ksltl;
		private int ardr;
		private int slrr;
		private int fbc;
		private int waveform;

		private static SOPL2Op readMe(RandomAccessInputStreamImpl inputStream) throws IOException
		{
			SFMOperator fm_op = SFMOperator.readMe(inputStream);
			SOPL2Op result = new SOPL2Op();
			result.ammulti	= fm_op.amplitude_vibrato	<< 7 | fm_op.frequency_vibrato << 6 | fm_op.sustaining_sound << 5 | fm_op.envelope_scaling << 4 | fm_op.freq_multiplier;
			result.ksltl	= fm_op.key_scale_level		<< 6 | fm_op.output_level;
			result.ardr		= fm_op.attack_rate			<< 4 | fm_op.decay_rate;
			result.slrr		= fm_op.sustain_level		<< 4 | fm_op.release_rate;
			result.fbc		= fm_op.feed_back			<< 1 | (fm_op.fm_type ^ 1);
			return result;
		}
		@Override
		public String toString()
		{
			return ammulti + "/" + ksltl + "/" + ardr + "/" + slrr + "/" + fbc;
		}
	}

	private static final class SRolInstrument
	{
		private int mode;
		private int voice_number;
		private SOPL2Op modulator;
		private SOPL2Op carrier;

		private static SRolInstrument readMe(RandomAccessInputStreamImpl inputStream) throws IOException
		{
			SRolInstrument result = new SRolInstrument();
			result.mode = inputStream.read();
			result.voice_number = inputStream.read();
			result.modulator = SOPL2Op.readMe(inputStream);
			result.carrier = SOPL2Op.readMe(inputStream);
			result.modulator.waveform = inputStream.read();
			result.carrier.waveform = inputStream.read();
			return result;
		}
		@Override
		public String toString()
		{
			return mode + "/" + voice_number + " modulator[" + modulator.toString() + "] carrier[" + carrier.toString() + "]";
		}
	}

	private static final class SInstrument
	{
		private String name;
		private SRolInstrument instrument;

		@Override
		public String toString()
		{
			return name + " " + instrument.toString();
		}
	}

	// ---------------------------------------------------------
	private SRolHeader				mpROLHeader;
	private int						mpOldFNumFreqPtr;
	private ArrayList<STempoEvent>	mTempoEvents;
	private ArrayList<CVoiceData>	mVoiceData;
	private ArrayList<SInstrument>	mInstrumentList;
	private int[]					mFNumFreqPtrList;
	private int[]					mHalfToneOffset;
	private int[]					mVolumeCache;
	private int[]					mKSLTLCache;
	private int[]					mNoteCache;
	private int[]					mKOnOctFNumCache;
	private boolean[]				mKeyOnCache;
	private double					mRefresh;
	private long					mOldPitchBendLength;
	private int						mPitchRangeStep;
	private int						mNextTempoEvent;
	private int						mCurrTick;
	private int						mTimeOfLastNote;
	private int						mOldHalfToneOffset;
	private int						mAMVibRhythmCache;
	// ---------------------------------------------------------
	private URL rolFile;
	private URL bnkFile;
	// private boolean comp_mode = false;

	/**
	 * Constructor for ROLSequence
	 */
	public ROLSequence()
	{
		super();
		mpROLHeader			= null;
		mpOldFNumFreqPtr	= 0;
		mTempoEvents		= null;
		mVoiceData			= null;
		mInstrumentList		= new ArrayList<SInstrument>();
		mFNumFreqPtrList	= new int[kNumPercussiveVoices];	//Arrays.fill(mFNumFreqPtrList, 0);
		mHalfToneOffset		= new int[kNumPercussiveVoices];	//Arrays.fill(mHalfToneOffset, 0);
		mVolumeCache		= new int[kNumPercussiveVoices];		//Arrays.fill(mVolumeCache, 0);
		mKSLTLCache			= new int[kNumPercussiveVoices];		//Arrays.fill(mKSLTLCache, 0);
		mNoteCache			= new int[kNumPercussiveVoices];			//Arrays.fill(mNoteCache, 0);
		mKOnOctFNumCache	= new int[kNumMelodicVoices];		//Arrays.fill(mKOnOctFNumCache, 0);
		mKeyOnCache			= new boolean[kNumPercussiveVoices];	//Arrays.fill(mKeyOnCache, false);
		mRefresh			= kDefaultUpdateTme;
		mOldPitchBendLength	= ~0;
		mPitchRangeStep		= skNrStepPitch;
		mNextTempoEvent		= 0;
		mCurrTick			= 0;
		mTimeOfLastNote		= 0;
		mOldHalfToneOffset	= 0;
		mAMVibRhythmCache	= 0;
	}
	// ---------------------------------------------------------
	/**
	 * @param inputStream
	 * @throws IOException
	 * @see de.quippy.javamod.multimedia.opl3.sequencer.OPL3Sequence#readOPL3Sequence(de.quippy.javamod.io.RandomAccessInputStreamImpl)
	 */
	@Override
	protected void readOPL3Sequence(final RandomAccessInputStreamImpl inputStream) throws IOException
	{
		if (inputStream==null || inputStream.available()<=0) return;

		mpROLHeader = SRolHeader.readMe(inputStream);
		if (mpROLHeader.version_major != skVersionMajor || mpROLHeader.version_minor != skVersionMinor) throw new IOException("Unsupported ROL-File version V" + mpROLHeader.version_major + "." + mpROLHeader.version_minor);

		// Effekter uses "Notes" instead of "Voix" for next voice section 
		if (mpROLHeader.comment.toUpperCase().contains(EFFEKTER_MAGIC_STRING)) FILLER_NOTE_SECTION = FILLER_EFFEKTER_SECTION;
		
		load_Tempo_Events(inputStream);
		load_Voice_Data(inputStream);
	}
	// ---------------------------------------------------------
	/**
	 * Read ahead and check for occurrence of string check
	 * This is to find new sections, if the ROL file lied
	 * to us about the length of a section
	 * @since 13.08.2020
	 * @param inputStream
	 * @param check
	 * @return true, if section found. False otherwise
	 * @throws IOException
	 */
	private boolean check_for_event(final RandomAccessInputStreamImpl inputStream, String check) throws IOException
	{
		final long currentPosition = inputStream.getFilePointer();
		final String compare = inputStream.readString(check.length());
		inputStream.seek(currentPosition);
		return compare.equalsIgnoreCase(check);
	}
	private void load_Tempo_Events(final RandomAccessInputStreamImpl inputStream) throws IOException
	{
		final int num_tempo_events = inputStream.readIntelWord();

		mTempoEvents = new ArrayList<STempoEvent>(num_tempo_events);

		for (int i=0; i<num_tempo_events && !check_for_event(inputStream, FILLER_NOTE_SECTION); i++)
		{
			mTempoEvents.add(STempoEvent.readMe(inputStream));
		}
	}
	private void load_Voice_Data(final RandomAccessInputStreamImpl inputStream) throws IOException
	{
		if (bnkFile==null || !Helpers.urlExists(bnkFile)) throw new IOException("Bankfile not found");

		RandomAccessInputStreamImpl bnkInputStream = null;
		try
		{
			bnkInputStream = new RandomAccessInputStreamImpl(bnkFile);
			SBnkHeader bnkHeader = SBnkHeader.readMe(bnkInputStream);

			// In my old sources loading a ROL file there was a check for a comp_mode:
			// this ignores the mode value in the header and sets to fix kNumPercussiveVoices
			// if
			// a) we want this check (comp_mode == true)
			// b) either "(AdLib file-mode)" or ".ROL" can be found in the header comment
//	        if (comp_mode && mpROLHeader.comment.contains("(AdLib file-mode)") && mpROLHeader.comment.indexOf(".ROL")==-1)
//	        	comp_mode = false;
			final int numVoices = /* (comp_mode)? kNumPercussiveVoices : */(mpROLHeader.mode != 0) ? kNumMelodicVoices : kNumPercussiveVoices;

			mVoiceData = new ArrayList<CVoiceData>(numVoices);
			for (int i=0; i<numVoices; i++)
			{
				CVoiceData voice = new CVoiceData();

				inputStream.skip(ROL_FILLER_SIZE); // Voix
				load_Note_Events(inputStream, voice);
				inputStream.skip(ROL_FILLER_SIZE); // Timbre
				load_Instrument_Events(inputStream, voice, bnkInputStream, bnkHeader);
				inputStream.skip(ROL_FILLER_SIZE); // Volume
				load_Volume_Events(inputStream, voice);
				inputStream.skip(ROL_FILLER_SIZE); // Pitch
				load_Pitch_Events(inputStream, voice);

				mVoiceData.add(voice);
			}
		}
		finally
		{
			if (bnkInputStream != null) try { bnkInputStream.close(); } catch (Exception ex) { /* NOOP */ }
		}
	}
	private void load_Note_Events(final RandomAccessInputStreamImpl inputStream, final CVoiceData voice) throws IOException
	{
		final ArrayList<SNoteEvent> note_events = voice.note_events = new ArrayList<SNoteEvent>();

		final int time_of_last_note = inputStream.readIntelWord();
		int total_duration = 0;

		while (total_duration<time_of_last_note && !check_for_event(inputStream, FILLER_TIMBRE_SECTION))
		{
			SNoteEvent event = SNoteEvent.readMe(inputStream);
			note_events.add(event);
			total_duration += event.duration;
		}

		final int newTimeOfLastNote = (total_duration<time_of_last_note)? total_duration : time_of_last_note; 
		if (newTimeOfLastNote > mTimeOfLastNote) mTimeOfLastNote = newTimeOfLastNote; 
	}
	private void load_Volume_Events(final RandomAccessInputStreamImpl inputStream, final CVoiceData voice) throws IOException
	{
		final int number_of_volume_events = inputStream.readIntelWord();

		voice.volume_events = new ArrayList<SVolumeEvent>(number_of_volume_events);

		for (int i=0; i<number_of_volume_events && !check_for_event(inputStream, FILLER_PITCH_SECTION); i++)
		{
			voice.volume_events.add(SVolumeEvent.readMe(inputStream));
		}
	}
	private void load_Pitch_Events(final RandomAccessInputStreamImpl inputStream, final CVoiceData voice) throws IOException
	{
		final int number_of_pitch_events = inputStream.readIntelWord();

		voice.pitch_events = new ArrayList<SPitchEvent>(number_of_pitch_events);

		for (int i=0; i<number_of_pitch_events && !check_for_event(inputStream, FILLER_NOTE_SECTION); i++)
		{
			voice.pitch_events.add(SPitchEvent.readMe(inputStream));
		}
	}
	private void load_Instrument_Events(final RandomAccessInputStreamImpl inputStream, final CVoiceData voice, final RandomAccessInputStreamImpl bnk_file, final SBnkHeader bnk_header) throws IOException
	{
		final int number_of_instrument_events = inputStream.readIntelWord();

		voice.instrument_events = new ArrayList<SInstrumentEvent>(number_of_instrument_events);

		for (int i=0; i<number_of_instrument_events && !check_for_event(inputStream, FILLER_VOLUME_SECTION); i++)
		{
			SInstrumentEvent event = SInstrumentEvent.readMe(inputStream);

			String event_name = event.name;
			event.ins_index = load_Rol_Instrument(bnk_file, bnk_header, event_name);

			voice.instrument_events.add(event);

			inputStream.skip(ROL_INSTRUMENT_EVENT_FILLER_SIZE);
		}
	}
	private int load_Rol_Instrument(final RandomAccessInputStreamImpl bnk_file, final SBnkHeader header, final String name) throws IOException
	{
		final ArrayList<SInstrumentName> ins_name_list = header.ins_name_list;

		final int ins_index = get_Instrument_Index(name);

		if (ins_index!=-1) return ins_index;

		final SInstrument usedInstrument = new SInstrument();
		usedInstrument.name = name;

		SInstrumentName instrument = null;
		final int size = ins_name_list.size();
		for (int i = 0; i < size; i++)
		{
			final SInstrumentName ins = ins_name_list.get(i);
			if (ins.name.equals(name))
			{
				instrument = ins;
				break;
			}
		}

		if (instrument != null)
		{
			final long seekOffs = header.abs_offset_of_data + (instrument.index * kSizeofDataRecord);
			bnk_file.seek(seekOffs);
			usedInstrument.instrument = SRolInstrument.readMe(bnk_file);
		}

		mInstrumentList.add(usedInstrument);
		// index of newly added instrument
		return mInstrumentList.size() - 1;
	}
	private int get_Instrument_Index(final String name)
	{
		final int size = mInstrumentList.size();
		for (int index = 0; index < size; index++)
		{
			final SInstrument instrument = mInstrumentList.get(index);
			if (instrument.name.equals(name)) return index;
		}
		return -1;
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.opl3.sequencer.OPL3Sequence#getSongName()
	 */
	@Override
	public String getSongName()
	{
		return MultimediaContainerManager.getSongNameFromURL(rolFile);
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.opl3.sequencer.OPL3Sequence#getAuthor()
	 */
	@Override
	public String getAuthor()
	{
		return Helpers.EMPTY_STING;
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.opl3.sequencer.OPL3Sequence#getDescription()
	 */
	@Override
	public String getDescription()
	{
		StringBuilder sb = new StringBuilder();
		if (mInstrumentList != null)
		{
			sb.append("Instruments used:").append('\n');
			for (SInstrument ins : mInstrumentList)
			{
				sb.append(ins.name).append('\n');
			}
		}
		return sb.toString();
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.opl3.sequencer.OPL3Sequence#getTypeName()
	 */
	@Override
	public String getTypeName()
	{
		if (mpROLHeader != null)
			return ((new StringBuilder()).append((mpROLHeader.comment.contains(EFFEKTER_MAGIC_STRING)?"Effekter V1.0 written":"AdLib Composer written")).append(" AdLib ROL File V").append(mpROLHeader.version_major).append('.').append(mpROLHeader.version_minor)).toString();
		else
			return "AdLib ROL File (no ROL Header?!)";
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.opl3.sequencer.OPL3Sequence#getOPLType()
	 */
	@Override
	public oplType getOPLType()
	{
		return EmuOPL.oplType.OPL2;
	}
	/**
	 * @param url
	 * @see de.quippy.javamod.multimedia.opl3.sequencer.OPL3Sequence#setURL(java.net.URL)
	 */
	@Override
	public void setURL(URL url)
	{
		this.rolFile = url;
	}
	/**
	 * @since 05.08.2020
	 * @param bnkURL
	 */
	public void setBNKFile(URL bnkURL)
	{
		this.bnkFile = bnkURL;
	}
	// ---------------------------------------------------------
	private void setRefresh(final double multiplier)
	{
		mRefresh = ((double)mpROLHeader.ticks_per_beat * mpROLHeader.basic_tempo * multiplier) / 60.0;
	}
	/**
	 * @param opl
	 * @return
	 * @see de.quippy.javamod.multimedia.opl3.sequencer.OPL3Sequence#updateToOPL(de.quippy.opl3.OPL3)
	 */
	@Override
	public boolean updateToOPL(final EmuOPL opl)
	{
		if ((mNextTempoEvent < mTempoEvents.size()) && (mTempoEvents.get(mNextTempoEvent).time == mCurrTick))
		{
			setRefresh(mTempoEvents.get(mNextTempoEvent++).multiplier);
		}

		final int anzVoices = mVoiceData.size();
		for (int voice = 0; voice < anzVoices; voice++)
			updateVoice(opl, voice, mVoiceData.get(voice));

		mCurrTick++;
		if (mCurrTick > mTimeOfLastNote) return false;
		return true;
	}
	private void updateVoice(final EmuOPL opl, final int voice, CVoiceData voiceData)
	{
		ArrayList<SInstrumentEvent> iEvents = voiceData.instrument_events;
		ArrayList<SVolumeEvent> vEvents = voiceData.volume_events;
		ArrayList<SNoteEvent> nEvents = voiceData.note_events;
		ArrayList<SPitchEvent> pEvents = voiceData.pitch_events;

		if (voiceData.next_instrument_event < iEvents.size())
		{
			final SInstrumentEvent instrumentEvent = iEvents.get(voiceData.next_instrument_event);
			if (instrumentEvent.time == mCurrTick)
			{
				send_Ins_Data_To_Chip(opl, voice, instrumentEvent.ins_index);
				voiceData.next_instrument_event++;
			}
		}

		if (voiceData.next_volume_event < vEvents.size())
		{
			final SVolumeEvent volumeEvent = vEvents.get(voiceData.next_volume_event);
			if (volumeEvent.time == mCurrTick)
			{
				final int volume = (int) (skMaxVolume * volumeEvent.multiplier);
				setVolume(opl, voice, volume);
				voiceData.next_volume_event++; // move to next volume event
			}
		}

		if (voiceData.current_note_duration == voiceData.mNoteDuration)
		{
			if (voiceData.next_note_event < nEvents.size())
			{
				SNoteEvent noteEvent = nEvents.get(voiceData.next_note_event);

				setNote(opl, voice, noteEvent.number);
				voiceData.current_note_duration = 0;
				voiceData.mNoteDuration = noteEvent.duration;
			}
			else
			{
				setNote(opl, voice, kSilenceNote);
			}
			voiceData.next_note_event++;
		}
		voiceData.current_note_duration++;

		if (voiceData.next_pitch_event < pEvents.size())
		{
			SPitchEvent pitchEvent = pEvents.get(voiceData.next_pitch_event);
			if (pitchEvent.time == mCurrTick)
			{
				setPitch(opl, voice, pitchEvent.variation);
				voiceData.next_pitch_event++;
			}
		}
	}
	private void setNote(final EmuOPL opl, final int voice, final int note)
	{
		if (voice < kBassDrumChannel || mpROLHeader.mode != 0)
		{
			setNoteMelodic(opl, voice, note);
		}
		else
		{
			setNotePercussive(opl, voice, note);
		}
	}
	private void setNotePercussive(final EmuOPL opl, final int voice, final int note)
	{
		final int channel_bit_mask = 1 << (4 - voice + kBassDrumChannel);

		mAMVibRhythmCache &= ~channel_bit_mask;
		opl.writeOPL2(skOPL2_AmVibRhythmBaseAddress, mAMVibRhythmCache);
		mKeyOnCache[voice] = false;

		if (note != kSilenceNote)
		{
			switch (voice)
			{
				case kTomtomChannel:
					setFreq(opl, kTomtomChannel, note);
					setFreq(opl, kSnareDrumChannel, note + kTomTomToSnare);
					break;

				case kBassDrumChannel:
					setFreq(opl, voice, note);
					break;
				default:
					// Does nothing
					break;
			}

			mKeyOnCache[voice] = true;
			mAMVibRhythmCache |= channel_bit_mask;
			opl.writeOPL2(skOPL2_AmVibRhythmBaseAddress, mAMVibRhythmCache);
		}
	}
	private void setNoteMelodic(final EmuOPL opl, final int voice, final int note)
	{
		opl.writeOPL2(skOPL2_KeyOnFreqHiBaseAddress + voice, mKOnOctFNumCache[voice] & ~skOPL2_KeyOnMask);
		mKeyOnCache[voice] = false;

		if (note != kSilenceNote)
		{
			setFreq(opl, voice, note, true);
		}
	}
	// From Adlib Music SDK's ADLIB.C ...
	private void changePitch(final int voice, final int pitchBend)
	{
		final int pitchBendLength = (pitchBend - skMidPitch) * mPitchRangeStep;

		if (mOldPitchBendLength == pitchBendLength)
		{
			// Optimization ...
			mFNumFreqPtrList[voice] = mpOldFNumFreqPtr;
			mHalfToneOffset[voice] = mOldHalfToneOffset;
		}
		else
		{
			final int pitchStepDir = pitchBendLength / skMidPitch;
			int delta;
			if (pitchStepDir < 0)
			{
				final int pitchStepDown = skNrStepPitch - 1 - pitchStepDir;
				mOldHalfToneOffset = mHalfToneOffset[voice] = -(pitchStepDown / skNrStepPitch);
				delta = (pitchStepDown - skNrStepPitch + 1) % skNrStepPitch;
				if (delta != 0)
				{
					delta = skNrStepPitch - delta;
				}
			}
			else
			{
				mOldHalfToneOffset = mHalfToneOffset[voice] = pitchStepDir / skNrStepPitch;
				delta = pitchStepDir % skNrStepPitch;
			}
			mpOldFNumFreqPtr = mFNumFreqPtrList[voice] = delta;
			mOldPitchBendLength = pitchBendLength;
		}
	}
	private void setPitch(final EmuOPL opl, final int voice, final double variation)
	{
		if (voice < kBassDrumChannel || mpROLHeader.mode != 0)
		{
			final int pitchBend = (variation == 1.0) ? skMidPitch : (int)((0x3fff >> 1) * variation);
			changePitch(voice, pitchBend);
			setFreq(opl, voice, mNoteCache[voice], mKeyOnCache[voice]);
		}
	}
	private void setFreq(final EmuOPL opl, final int voice, final int note)
	{
		setFreq(opl, voice, note, false);
	}
	private void setFreq(final EmuOPL opl, final int voice, final int note, final boolean keyOn)
	{
		int biased_note = note + mHalfToneOffset[voice];
		if (biased_note < 0) biased_note = 0;
		else 
		if (biased_note >= skMaxNotes) biased_note = skMaxNotes - 1;

		mNoteCache[voice] = note;
		mKeyOnCache[voice] = keyOn;

		final int octave = biased_note / skNumSemitonesInOctave;
		final int noteIndex = biased_note % skNumSemitonesInOctave;

		final int frequency = skFNumNotes[mFNumFreqPtrList[voice]* skNumSemitonesInOctave + noteIndex];
		mKOnOctFNumCache[voice] = (octave << skOPL2_BlockNumberShift) | ((frequency >> skOPL2_FNumMSBShift) & skOPL2_FNumMSBMask);

		opl.writeOPL2(skOPL2_FreqLoBaseAddress + voice, frequency & skOPL2_FNumLSBMask);
		opl.writeOPL2(skOPL2_KeyOnFreqHiBaseAddress + voice, mKOnOctFNumCache[voice] | (keyOn ? skOPL2_KeyOnMask : 0x0));
	}
	private int getKSLTL(final int voice)
	{
		final int baseVolume = skOPL2_TLMinLevel - (mKSLTLCache[voice] & skOPL2_TLMask); // max amplitude from instrument setting
		int newVolume = ((baseVolume * mVolumeCache[voice]) + (1 << (skVolumeQualityShift - 1))) >> skVolumeQualityShift;
		// clamp it
		if (newVolume < 0) newVolume = 0;
		else 
		if (newVolume > skOPL2_TLMinLevel) newVolume = skOPL2_TLMinLevel;
		// rebuild register output with old KSL plus new TL (logic vice verca: 0= maxvolume, 0x3F= minimum)
		return (mKSLTLCache[voice] & skOPL2_KSLMask) | ((skOPL2_TLMinLevel - newVolume) & 0x3F);
	}
	private void setVolume(final EmuOPL opl, final int voice, final int volume)
	{
		final int op_offset = (voice < kSnareDrumChannel || mpROLHeader.mode != 0) ? op_table[voice] + skCarrierOpOffset : drum_op_table[voice - kSnareDrumChannel];

		mVolumeCache[voice] = volume;

		opl.writeOPL2(skOPL2_KSLTLBaseAddress + op_offset, getKSLTL(voice));
	}
	private void send_Ins_Data_To_Chip(final EmuOPL opl, final int voice, final int ins_index)
	{
		final SRolInstrument instrument = mInstrumentList.get(ins_index).instrument;

		send_Operator(opl, voice, instrument.modulator, instrument.carrier);
	}
	private void send_Operator(final EmuOPL opl, final int voice, final SOPL2Op modulator, final SOPL2Op carrier)
	{
		if (voice < kSnareDrumChannel || mpROLHeader.mode != 0)
		{
			final int op_offset = op_table[voice];

			opl.writeOPL2(skOPL2_AaMultiBaseAddress + op_offset, modulator.ammulti);
			opl.writeOPL2(skOPL2_KSLTLBaseAddress + op_offset, modulator.ksltl);
			opl.writeOPL2(skOPL2_ArDrBaseAddress + op_offset, modulator.ardr);
			opl.writeOPL2(skOPL2_SlrrBaseAddress + op_offset, modulator.slrr);
			opl.writeOPL2(skOPL2_FeedConBaseAddress + voice, modulator.fbc);
			opl.writeOPL2(skOPL2_WaveformBaseAddress + op_offset, modulator.waveform);

			mKSLTLCache[voice] = carrier.ksltl;

			opl.writeOPL2(skOPL2_AaMultiBaseAddress + op_offset + skCarrierOpOffset, carrier.ammulti);
			opl.writeOPL2(skOPL2_KSLTLBaseAddress + op_offset + skCarrierOpOffset, getKSLTL(voice));
			opl.writeOPL2(skOPL2_ArDrBaseAddress + op_offset + skCarrierOpOffset, carrier.ardr);
			opl.writeOPL2(skOPL2_SlrrBaseAddress + op_offset + skCarrierOpOffset, carrier.slrr);
			opl.writeOPL2(skOPL2_WaveformBaseAddress + op_offset + skCarrierOpOffset, carrier.waveform);
		}
		else
		{
			final int op_offset = drum_op_table[voice - kSnareDrumChannel];

			mKSLTLCache[voice] = modulator.ksltl;

			opl.writeOPL2(skOPL2_AaMultiBaseAddress + op_offset, modulator.ammulti);
			opl.writeOPL2(skOPL2_KSLTLBaseAddress + op_offset, getKSLTL(voice));
			opl.writeOPL2(skOPL2_ArDrBaseAddress + op_offset, modulator.ardr);
			opl.writeOPL2(skOPL2_SlrrBaseAddress + op_offset, modulator.slrr);
			opl.writeOPL2(skOPL2_WaveformBaseAddress + op_offset, modulator.waveform);
		}
	}
	/**
	 * @param opl
	 * @see de.quippy.javamod.multimedia.opl3.sequencer.OPL3Sequence#initialize(de.quippy.opl3.OPL3)
	 */
	@Override
	public void initialize(final EmuOPL opl)
	{
		for (CVoiceData voice : mVoiceData)
			voice.reset();

		Arrays.fill(mHalfToneOffset, 0);
		Arrays.fill(mVolumeCache, skMaxVolume);
		Arrays.fill(mKSLTLCache, 0);
		Arrays.fill(mNoteCache, 0);
		Arrays.fill(mKOnOctFNumCache, 0);
		Arrays.fill(mKeyOnCache, false);

		mNextTempoEvent = 0;
		mCurrTick = 0;
		mAMVibRhythmCache = 0;

		resetOPL(opl);
		opl.writeOPL2(skOPL2_WaveCtrlBaseAddress, skOPL2_EnableWaveformSelectMask); // Enable waveform select

		if (mpROLHeader.mode == 0)
		{
			mAMVibRhythmCache = skOPL2_RhythmMask;
			opl.writeOPL2(skOPL2_AmVibRhythmBaseAddress, mAMVibRhythmCache); // Enable rhythm mode

			setFreq(opl, kTomtomChannel, kTomTomNote);
			setFreq(opl, kSnareDrumChannel, kSnareNote);
		}

		setRefresh(1.0f);
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.opl3.sequencer.OPL3Sequence#getRefresh()
	 */
	@Override
	public double getRefresh()
	{
		return mRefresh;
	}
}
