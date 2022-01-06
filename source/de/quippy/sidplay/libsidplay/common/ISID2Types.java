/**
 *                            sidplay2 specific types
 *                            -----------------------
 *  begin                : Fri Aug 10 2001
 *  copyright            : (C) 2001 by Simon White
 *  email                : s_a_white@email.com
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; either version 3 of the License, or
 *   (at your option) any later version.
 *
 * @author Ken H�ndel
 *
 */
package de.quippy.sidplay.libsidplay.common;

import de.quippy.sidplay.libsidplay.components.sidtune.SidTuneInfo;

public interface ISID2Types {

	//
	// Maximum values
	//

	final short /* uint_least8_t */SID2_MAX_PRECISION = 16;

	final short /* uint_least8_t */SID2_MAX_OPTIMISATION = 2;

	/**
	 * Delays <= MAX produce constant results.<BR>
	 * Delays > MAX produce random results
	 */
	final int /* uint_least16_t */SID2_MAX_POWER_ON_DELAY = 0x1FFF;

	//
	// Default settings
	//

	final long /* uint_least32_t */SID2_DEFAULT_SAMPLING_FREQ = 44100;

	final short /* uint_least8_t */SID2_DEFAULT_PRECISION = 16;

	final byte /* uint_least8_t */SID2_DEFAULT_OPTIMISATION = 1;

	// final boolean SID2_DEFAULT_SID_SAMPLES = true; // Samples through sid

	final int /* uint_least16_t */SID2_DEFAULT_POWER_ON_DELAY = SID2_MAX_POWER_ON_DELAY + 1;

	//
	// Types
	//

	enum sid2_player_t {
		sid2_playing, sid2_paused, sid2_stopped
	}

	enum sid2_playback_t {
		sid2_left, sid2_mono, sid2_stereo, sid2_right
	}

	/**
	 * Environment Modes
	 * <UL>
	 * <LI>sid2_envPS = Playsid
	 * <LI>sid2_envTP = Sidplay - Transparent Rom
	 * <LI>sid2_envBS = Sidplay - Bankswitching
	 * <LI>sid2_envR = Sidplay2 - Real C64 Environment
	 * </UL>
	 */
	enum sid2_env_t {
		sid2_envPS, sid2_envTP, sid2_envBS, sid2_envR, sid2_envTR
	}

	enum sid2_model_t {
		SID2_MODEL_CORRECT, SID2_MOS6581, SID2_MOS8580
	}

	enum sid2_clock_t {
		SID2_CLOCK_CORRECT, SID2_CLOCK_PAL, SID2_CLOCK_NTSC
	}

	/**
	 * @author Ken H�ndel
	 * 
	 * Soundcard sample format
	 */
	enum sid2_sample_t {
		SID2_LITTLE_SIGNED, SID2_LITTLE_UNSIGNED, SID2_BIG_SIGNED, SID2_BIG_UNSIGNED
	}

	class sid2_config_t {
		/**
		 * Intended tune speed when unknown
		 */
		public sid2_clock_t clockDefault;

		public boolean clockForced;

		/**
		 * User requested emulation speed
		 */
		public sid2_clock_t clockSpeed;

		public sid2_env_t environment;

		public boolean forceDualSids;

		public boolean emulateStereo;

		public long /* uint_least32_t */frequency;

		public byte /* uint_least8_t */optimisation;

		public sid2_playback_t playback;

		public int precision;

		/**
		 * Intended sid model when unknown
		 */
		public sid2_model_t sidDefault;

		public SIDBuilder sidEmulation;

		/**
		 * User requested sid model
		 */
		public sid2_model_t sidModel;

		public boolean sidSamples;

		public long /* uint_least32_t */leftVolume;

		public long /* uint_least32_t */rightVolume;

		public sid2_sample_t sampleFormat;

		public int /* uint_least16_t */powerOnDelay;

		/**
		 * Max sid writes to form crc
		 */
		public long /* uint_least32_t */sid2crcCount;
	}

	class sid2_info_t {
		public String[] credits;

		public int /* uint */channels;

		public int /* uint_least16_t */driverAddr;

		public int /* uint_least16_t */driverLength;

		public String name;

		/**
		 * May not need this
		 */
		public SidTuneInfo tuneInfo;

		public String version;

		/**
		 * load, config and stop calls will reset this and remove all pending
		 * events! 10th sec resolution.
		 */
		public IEventContext eventContext;

		public int /* uint */maxsids;

		public sid2_env_t environment;

		public int /* uint_least16_t */powerOnDelay;

		public long /* uint_least32_t */sid2crc;

		/**
		 * Number of sid writes forming crc
		 */
		public long /* uint_least32_t */sid2crcCount;
	}
}
