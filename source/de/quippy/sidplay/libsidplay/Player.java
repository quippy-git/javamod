/**
 *                                 Main Library Code
 *                                SIDs Mixer Routines
 *                             Library Configuration Code
 *                    xa65 - 6502 cross assembler and utility suite
 *                          reloc65 - relocates 'o65' files 
 *        Copyright (C) 1997 Andr� Fachat (a.fachat@physik.tu-chemnitz.de)
 *        ----------------------------------------------------------------
 *  begin                : Fri Jun 9 2000
 *  copyright            : (C) 2000 by Simon White
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
package de.quippy.sidplay.libsidplay;

import static de.quippy.sidplay.libsidplay.Config.PACKAGE_NAME;
import static de.quippy.sidplay.libsidplay.Config.PACKAGE_VERSION;
import static de.quippy.sidplay.libsidplay.Config.S_A_WHITE_EMAIL;
import static de.quippy.sidplay.libsidplay.common.Event.event_phase_t.EVENT_CLOCK_PHI1;
import static de.quippy.sidplay.libsidplay.common.ISID2Types.SID2_DEFAULT_OPTIMISATION;
import static de.quippy.sidplay.libsidplay.common.ISID2Types.SID2_DEFAULT_POWER_ON_DELAY;
import static de.quippy.sidplay.libsidplay.common.ISID2Types.SID2_DEFAULT_PRECISION;
import static de.quippy.sidplay.libsidplay.common.ISID2Types.SID2_DEFAULT_SAMPLING_FREQ;
import static de.quippy.sidplay.libsidplay.common.ISID2Types.SID2_MAX_OPTIMISATION;
import static de.quippy.sidplay.libsidplay.common.ISID2Types.SID2_MAX_POWER_ON_DELAY;
import static de.quippy.sidplay.libsidplay.common.ISID2Types.SID2_MAX_PRECISION;
import static de.quippy.sidplay.libsidplay.common.ISID2Types.sid2_clock_t.SID2_CLOCK_CORRECT;
import static de.quippy.sidplay.libsidplay.common.ISID2Types.sid2_clock_t.SID2_CLOCK_NTSC;
import static de.quippy.sidplay.libsidplay.common.ISID2Types.sid2_clock_t.SID2_CLOCK_PAL;
import static de.quippy.sidplay.libsidplay.common.ISID2Types.sid2_env_t.sid2_envBS;
import static de.quippy.sidplay.libsidplay.common.ISID2Types.sid2_env_t.sid2_envPS;
import static de.quippy.sidplay.libsidplay.common.ISID2Types.sid2_env_t.sid2_envR;
import static de.quippy.sidplay.libsidplay.common.ISID2Types.sid2_model_t.SID2_MODEL_CORRECT;
import static de.quippy.sidplay.libsidplay.common.ISID2Types.sid2_model_t.SID2_MOS6581;
import static de.quippy.sidplay.libsidplay.common.ISID2Types.sid2_model_t.SID2_MOS8580;
import static de.quippy.sidplay.libsidplay.common.ISID2Types.sid2_playback_t.sid2_left;
import static de.quippy.sidplay.libsidplay.common.ISID2Types.sid2_playback_t.sid2_mono;
import static de.quippy.sidplay.libsidplay.common.ISID2Types.sid2_playback_t.sid2_stereo;
import static de.quippy.sidplay.libsidplay.common.ISID2Types.sid2_player_t.sid2_paused;
import static de.quippy.sidplay.libsidplay.common.ISID2Types.sid2_player_t.sid2_playing;
import static de.quippy.sidplay.libsidplay.common.ISID2Types.sid2_player_t.sid2_stopped;
import static de.quippy.sidplay.libsidplay.common.ISID2Types.sid2_sample_t.SID2_LITTLE_SIGNED;
import static de.quippy.sidplay.libsidplay.common.SIDEndian.endian_16;
import static de.quippy.sidplay.libsidplay.common.SIDEndian.endian_16hi8;
import static de.quippy.sidplay.libsidplay.common.SIDEndian.endian_little16;
import static de.quippy.sidplay.libsidplay.common.mos6510.IOpCode.JMPi;
import static de.quippy.sidplay.libsidplay.common.mos6510.IOpCode.JMPw;
import static de.quippy.sidplay.libsidplay.common.mos6510.IOpCode.JSRw;
import static de.quippy.sidplay.libsidplay.common.mos6510.IOpCode.LDAb;
import static de.quippy.sidplay.libsidplay.common.mos6510.IOpCode.RTSn;
import static de.quippy.sidplay.libsidplay.common.mos6510.IOpCode.STAa;
import static de.quippy.sidplay.libsidplay.components.mos656x.MOS656X.mos656x_model_t.MOS6567R8;
import static de.quippy.sidplay.libsidplay.components.mos656x.MOS656X.mos656x_model_t.MOS6569;
import static de.quippy.sidplay.libsidplay.components.sidtune.SidTune.SIDTUNE_CLOCK_ANY;
import static de.quippy.sidplay.libsidplay.components.sidtune.SidTune.SIDTUNE_CLOCK_NTSC;
import static de.quippy.sidplay.libsidplay.components.sidtune.SidTune.SIDTUNE_CLOCK_PAL;
import static de.quippy.sidplay.libsidplay.components.sidtune.SidTune.SIDTUNE_CLOCK_UNKNOWN;
import static de.quippy.sidplay.libsidplay.components.sidtune.SidTune.SIDTUNE_COMPATIBILITY_BASIC;
import static de.quippy.sidplay.libsidplay.components.sidtune.SidTune.SIDTUNE_COMPATIBILITY_PSID;
import static de.quippy.sidplay.libsidplay.components.sidtune.SidTune.SIDTUNE_COMPATIBILITY_R64;
import static de.quippy.sidplay.libsidplay.components.sidtune.SidTune.SIDTUNE_SIDMODEL_6581;
import static de.quippy.sidplay.libsidplay.components.sidtune.SidTune.SIDTUNE_SIDMODEL_8580;
import static de.quippy.sidplay.libsidplay.components.sidtune.SidTune.SIDTUNE_SIDMODEL_ANY;
import static de.quippy.sidplay.libsidplay.components.sidtune.SidTune.SIDTUNE_SIDMODEL_UNKNOWN;
import static de.quippy.sidplay.libsidplay.components.sidtune.SidTune.SIDTUNE_SPEED_CIA_1A;
import static de.quippy.sidplay.libsidplay.components.sidtune.SidTune.SIDTUNE_SPEED_VBI;
import static de.quippy.sidplay.libsidplay.mem.IBasic.BASIC;
import static de.quippy.sidplay.libsidplay.mem.IChar.CHAR;
import static de.quippy.sidplay.libsidplay.mem.IKernal.KERNAL;
import static de.quippy.sidplay.libsidplay.mem.IPSIDDrv.PSIDDRV;
import static de.quippy.sidplay.libsidplay.mem.IPowerOn.POWERON;
import de.quippy.sidplay.libsidplay.common.C64Env;
import de.quippy.sidplay.libsidplay.common.Event;
import de.quippy.sidplay.libsidplay.common.EventScheduler;
import de.quippy.sidplay.libsidplay.common.IEventContext;
import de.quippy.sidplay.libsidplay.common.SIDBuilder;
import de.quippy.sidplay.libsidplay.common.SIDEmu;
import de.quippy.sidplay.libsidplay.common.ISID2Types.sid2_clock_t;
import de.quippy.sidplay.libsidplay.common.ISID2Types.sid2_config_t;
import de.quippy.sidplay.libsidplay.common.ISID2Types.sid2_env_t;
import de.quippy.sidplay.libsidplay.common.ISID2Types.sid2_info_t;
import de.quippy.sidplay.libsidplay.common.ISID2Types.sid2_model_t;
import de.quippy.sidplay.libsidplay.common.ISID2Types.sid2_player_t;
import de.quippy.sidplay.libsidplay.common.mos6510.C64Environment;
import de.quippy.sidplay.libsidplay.common.mos6510.MOS6510;
import de.quippy.sidplay.libsidplay.common.mos6510.SID6510;
import de.quippy.sidplay.libsidplay.components.mos6526.C64CIA;
import de.quippy.sidplay.libsidplay.components.mos6526.SID6526;
import de.quippy.sidplay.libsidplay.components.mos656x.C64VIC;
import de.quippy.sidplay.libsidplay.components.sidtune.SidTune;
import de.quippy.sidplay.libsidplay.components.sidtune.SidTuneInfo;
import de.quippy.sidplay.libsidplay.components.xsid.C64XSID;

public class Player extends C64Env /* extends C64Environment */{

	/**
	 * Resolve multiple inheritance
	 */
	private C64Environment envp;

	private static final double CLOCK_FREQ_NTSC = 1022727.14;

	private static final double CLOCK_FREQ_PAL = 985248.4;

	private static final double VIC_FREQ_PAL = 50.0;

	private static final double VIC_FREQ_NTSC = 60.0;

	//
	// These texts are used to override the sidtune settings.
	//

	private static final String TXT_PAL_VBI = "50 Hz VBI (PAL)";

	private static final String TXT_PAL_VBI_FIXED = "60 Hz VBI (PAL FIXED)";

	private static final String TXT_PAL_CIA = "CIA (PAL)";

	private static final String TXT_NTSC_VBI = "60 Hz VBI (NTSC)";

	private static final String TXT_NTSC_VBI_FIXED = "50 Hz VBI (NTSC FIXED)";

	private static final String TXT_NTSC_CIA = "CIA (NTSC)";

	private static final String TXT_NA = "NA";

	//
	// Error Strings
	//

	private static final String ERR_CONF_WHILST_ACTIVE = "SIDPLAYER ERROR: Trying to configure player whilst active.";

	private static final String ERR_UNSUPPORTED_FREQ = "SIDPLAYER ERROR: Unsupported sampling frequency.";

	private static final String ERR_UNSUPPORTED_PRECISION = "SIDPLAYER ERROR: Unsupported sample precision.";

	/**
	 * 10 credits max
	 */
	private static String credit[] = new String[10];

	private static final String ERR_PSIDDRV_NO_SPACE = "ERROR: No space to install psid driver in C64 ram";

	private static final String ERR_PSIDDRV_RELOC = "ERROR: Failed whilst relocating psid driver";

	private EventScheduler m_scheduler;

	/**
	 * SID6510 cpu(6510, "Main CPU");
	 */
	private SID6510 sid6510;

	private MOS6510 mos6510;

	private MOS6510 cpu;

	//
	// Sid objects to use.
	//

	private NullSID nullsid;

	private C64XSID xsid;

	private C64CIA.C64cia1 cia;

	private C64CIA.C64cia2 cia2;

	private SID6526 sid6526;

	private C64VIC vic;

	private SIDEmu sid[] = new SIDEmu[SID2_MAX_SIDS];

	/**
	 * Mapping table in d4xx-d7xx
	 */
	private int m_sidmapper[] = new int[32];

	private static class EventMixer extends Event {
		private Player m_player;

		public void event() {
			m_player.mixer();
		}

		public EventMixer(Player player) {
			super("Mixer");
			m_player = (player);
		}
	}

	private EventMixer mixerEvent;

	private static class EventRTC extends Event {
		private IEventContext m_eventContext;

		private long /* event_clock_t */m_seconds;

		private long /* event_clock_t */m_period;

		private long /* event_clock_t */m_clk;

		public void event() {
			// Fixed point 25.7 (approx 2 dp)
			long /* event_clock_t */cycles;
			m_clk += m_period;
			cycles = m_clk >> 7;
			m_clk &= 0x7F;
			m_seconds++;
			m_eventContext.schedule(this, cycles, EVENT_CLOCK_PHI1);
		}

		public EventRTC(IEventContext context) {
			super("RTC");
			m_eventContext = (context);
			m_seconds = (0);
		}

		public long /* event_clock_t */getTime() {
			return m_seconds;
		}

		public void reset() {
			// Fixed point 25.7
			m_seconds = 0;
			m_clk = m_period & 0x7F;
			m_eventContext.schedule(this, m_period >> 7, EVENT_CLOCK_PHI1);
		}

		public void clock(double /* float64_t */period) { // Fixed point 25.7
			m_period = (long /* event_clock_t */) (period / 10.0 * (double /* float64_t */) (1 << 7));
			reset();
		}
	}

	private EventRTC rtc;

	/**
	 * User Configuration Settings
	 */
	private SidTuneInfo m_tuneInfo = new SidTuneInfo();

	private SidTune m_tune;

	private short /* uint8_t */m_ram[], m_rom[];

	private sid2_info_t m_info = new sid2_info_t();

	private sid2_config_t m_cfg = new sid2_config_t();

	private String m_errorString;

	private double /* float64_t */m_fastForwardFactor;

	private long /* uint_least32_t */m_mileage;

	private long /* int_least32_t */m_leftVolume;

	private long /* int_least32_t */m_rightVolume;

	private sid2_player_t m_playerState;

	private boolean m_running;

	private int m_rand;

	private long /* uint_least32_t */m_sid2crc;

	private long /* uint_least32_t */m_sid2crcCount;

	private boolean m_emulateStereo;

	//
	// Mixer settings
	//

	private long /* event_clock_t */m_sampleClock;

	private long /* event_clock_t */m_samplePeriod;

	private int /* uint_least32_t */m_sampleCount;

	private int /* uint_least32_t */m_sampleIndex;

	private short[] m_sampleBuffer;

	//
	// C64 environment settings
	//

	private static class Port {
		short /* uint8_t */pr_out;

		short /* uint8_t */ddr;

		short /* uint8_t */pr_in;
	}

	private Port m_port = new Port();

	private short /* uint8_t */m_playBank;

	//
	// temp stuff -------------
	//

	private boolean isKernal;

	private boolean isBasic;

	private boolean isIO;

	private boolean isChar;

	private void evalBankSelect(short /* uint8_t */data) {
		// Determine new memory configuration.
		m_port.pr_out = data;
		m_port.pr_in = (short) ((data & m_port.ddr) | (~m_port.ddr
				& (m_port.pr_in | 0x17) & 0xdf));
		data |= ~m_port.ddr & 0xff;
		data &= 7;
		isBasic = ((data & 3) == 3);
		isIO = (data > 4);
		isKernal = ((data & 2) != 0);
		isChar = ((data ^ 4) > 4);
	}

	// private void c64_initialise();

	// ------------------------

	/**
	 * Clock speed changes due to loading a new song
	 * 
	 * @param userClock
	 * @param defaultClock
	 * @param forced
	 * @return
	 */
	private double /* float64_t */clockSpeed(sid2_clock_t userClock,
			sid2_clock_t defaultClock, boolean forced) {
		double /* float64_t */cpuFreq = CLOCK_FREQ_PAL;

		// Detect the Correct Song Speed
		// Determine song speed when unknown
		if (m_tuneInfo.clockSpeed == SIDTUNE_CLOCK_UNKNOWN) {
			switch (defaultClock) {
			case SID2_CLOCK_PAL:
				m_tuneInfo.clockSpeed = SIDTUNE_CLOCK_PAL;
				break;
			case SID2_CLOCK_NTSC:
				m_tuneInfo.clockSpeed = SIDTUNE_CLOCK_NTSC;
				break;
			case SID2_CLOCK_CORRECT:
				// No default so base it on emulation clock
				m_tuneInfo.clockSpeed = SIDTUNE_CLOCK_ANY;
			}
		}

		// Since song will run correct at any clock speed
		// set tune speed to the current emulation
		if (m_tuneInfo.clockSpeed == SIDTUNE_CLOCK_ANY) {
			if (userClock == SID2_CLOCK_CORRECT)
				userClock = defaultClock;

			switch (userClock) {
			case SID2_CLOCK_NTSC:
				m_tuneInfo.clockSpeed = SIDTUNE_CLOCK_NTSC;
				break;
			case SID2_CLOCK_PAL:
			default:
				m_tuneInfo.clockSpeed = SIDTUNE_CLOCK_PAL;
				break;
			}
		}

		if (userClock == SID2_CLOCK_CORRECT) {
			switch (m_tuneInfo.clockSpeed) {
			case SIDTUNE_CLOCK_NTSC:
				userClock = SID2_CLOCK_NTSC;
				break;
			case SIDTUNE_CLOCK_PAL:
				userClock = SID2_CLOCK_PAL;
				break;
			}
		}

		if (forced) {
			m_tuneInfo.clockSpeed = SIDTUNE_CLOCK_PAL;
			if (userClock == SID2_CLOCK_NTSC)
				m_tuneInfo.clockSpeed = SIDTUNE_CLOCK_NTSC;
		}

		if (m_tuneInfo.clockSpeed == SIDTUNE_CLOCK_PAL)
			vic.chip(MOS6569);
		else
			// if (tuneInfo.clockSpeed == SIDTUNE_CLOCK_NTSC)
			vic.chip(MOS6567R8);

		if (userClock == SID2_CLOCK_PAL) {
			cpuFreq = CLOCK_FREQ_PAL;
			m_tuneInfo.speedString = TXT_PAL_VBI;
			if (m_tuneInfo.songSpeed == SIDTUNE_SPEED_CIA_1A)
				m_tuneInfo.speedString = TXT_PAL_CIA;
			else if (m_tuneInfo.clockSpeed == SIDTUNE_CLOCK_NTSC)
				m_tuneInfo.speedString = TXT_PAL_VBI_FIXED;
		} else // if (userClock == SID2_CLOCK_NTSC)
		{
			cpuFreq = CLOCK_FREQ_NTSC;
			m_tuneInfo.speedString = TXT_NTSC_VBI;
			if (m_tuneInfo.songSpeed == SIDTUNE_SPEED_CIA_1A)
				m_tuneInfo.speedString = TXT_NTSC_CIA;
			else if (m_tuneInfo.clockSpeed == SIDTUNE_CLOCK_PAL)
				m_tuneInfo.speedString = TXT_NTSC_VBI_FIXED;
		}
		return cpuFreq;
	}

	private int environment(sid2_env_t env) {
		switch (m_tuneInfo.compatibility) {
		case SIDTUNE_COMPATIBILITY_R64:
		case SIDTUNE_COMPATIBILITY_BASIC:
			env = sid2_envR;
			break;
		case SIDTUNE_COMPATIBILITY_PSID:
			if (env == sid2_envR)
				env = sid2_envBS;
		}

		// Environment already set?
		if (!((m_ram != null) && (m_info.environment == env))) {
			// Setup new player environment
			m_info.environment = env;
			if (m_ram != null) {
				if (m_ram == m_rom)
					m_ram = null;
				else {
					m_rom = null;
					m_ram = null;
				}
			}

			m_ram = new short /* uint8_t */[0x10000];

			// Setup the access functions to the environment
			// and the properties the memory has.
			if (m_info.environment == sid2_envPS) {
				// Playsid has no roms and SID exists in ram space
				m_rom = m_ram;
				m_mem = new IMem() {

					public short m_readMemByte(int addr) {
						return readMemByte_plain(addr);
					}

					public void m_writeMemByte(int addr, short data) {
						writeMemByte_playsid(addr, data);
					}

					public short m_readMemDataByte(int addr) {
						return readMemByte_plain(addr);
					}

				};
			} else {
				m_rom = new short /* uint8_t */[0x10000];

				switch (m_info.environment) {
				case sid2_envTP:
					m_mem = new IMem() {

						public short m_readMemByte(int addr) {
							return readMemByte_plain(addr);
						}

						public void m_writeMemByte(int addr, short data) {
							writeMemByte_sidplay(addr, data);
						}

						public short m_readMemDataByte(int addr) {
							return readMemByte_sidplaytp(addr);
						}

					};
					break;

				// case sid2_envTR:
				case sid2_envBS:
					m_mem = new IMem() {

						public short m_readMemByte(int addr) {
							return readMemByte_plain(addr);
						}

						public void m_writeMemByte(int addr, short data) {
							writeMemByte_sidplay(addr, data);
						}

						public short m_readMemDataByte(int addr) {
							return readMemByte_sidplaybs(addr);
						}

					};
					break;

				case sid2_envR:
				default: // <-- Just to please compiler
					m_mem = new IMem() {

						public short m_readMemByte(int addr) {
							return readMemByte_sidplaybs(addr);
						}

						public void m_writeMemByte(int addr, short data) {
							writeMemByte_sidplay(addr, data);
						}

						public short m_readMemDataByte(int addr) {
							return readMemByte_sidplaybs(addr);
						}

					};
					break;
				}
			}
		}

		{ // Have to reload the song into memory as
			// everything has changed
			int ret;
			sid2_env_t old = m_info.environment;
			m_info.environment = env;
			ret = initialise();
			m_info.environment = old;
			return ret;
		}
	}

	/**
	 * Makes the next sequence of notes available. For de.quippy.sidplay.sidplay compatibility
	 * this function should be called from interrupt event
	 */
	private void fakeIRQ() {
		// Check to see if the play address has been provided or whether we
		// should pick it up from an IRQ vector
		int /* uint_least16_t */playAddr = m_tuneInfo.playAddr;

		// We have to reload the new play address
		if (playAddr != 0)
			evalBankSelect(m_playBank);
		else {
			if (isKernal) {
				// Setup the entry point from hardware IRQ
				playAddr = endian_little16(m_ram, 0x0314);
			} else {
				// Setup the entry point from software IRQ
				playAddr = endian_little16(m_ram, 0xFFFE);
			}
		}

		// Setup the entry point and restart the cpu
		cpu.triggerIRQ();
		sid6510.reset(playAddr, (short) 0, (short) 0, (short) 0);
	}

	private int initialise() {
		// Fix the mileage counter if just finished another song.
		mileageCorrect();
		m_mileage += time();

		reset();

		{
			long /* uint_least32_t */page = ((long /* uint_least32_t */) m_tuneInfo.loadAddr
					+ m_tuneInfo.c64dataLen - 1) >> 8;
			if (page > 0xff) {
				m_errorString = "SIDPLAYER ERROR: Size of music data exceeds C64 memory.";
				return -1;
			}
		}

		if (psidDrvReloc(m_tuneInfo, m_info) < 0)
			return -1;

		// The Basic ROM sets these values on loading a file.
		{ // Program end address
			int /* uint_least16_t */start = m_tuneInfo.loadAddr;
			int /* uint_least16_t */end = (int) (start + m_tuneInfo.c64dataLen);
			endian_little16(m_ram, 0x2d, end); // Variables start
			endian_little16(m_ram, 0x2f, end); // Arrays start
			endian_little16(m_ram, 0x31, end); // Strings start
			endian_little16(m_ram, 0xac, start);
			endian_little16(m_ram, 0xae, end);
		}

		if (!m_tune.placeSidTuneInC64mem(m_ram)) {
			// Rev 1.6 (saw) - Allow loop through errors
			m_errorString = m_tuneInfo.statusString;
			return -1;
		}

		psidDrvInstall(m_info);
		rtc.reset();
		envReset(false);
		return 0;
	}

	// private void nextSequence();

	private void mixer() {
		// Fixed point 16.16
		long /* event_clock_t */cycles;
		short[] buf = m_sampleBuffer;
		int bufOff = m_sampleIndex;
		m_sampleClock += m_samplePeriod;
		cycles = m_sampleClock >> 16;
		m_sampleClock &= 0x0FFFF;
		m_sampleIndex += output.output(buf, bufOff);

		// Schedule next sample event
		(context()).schedule(mixerEvent, cycles, EVENT_CLOCK_PHI1);

		// Filled buffer
		if (m_sampleIndex >= m_sampleCount)
			m_running = false;
	}

	private void mixerReset() {
		// Fixed point 16.16
		m_sampleClock = m_samplePeriod & 0x0FFFF;
		// Schedule next sample event
		(context())
				.schedule(mixerEvent, m_samplePeriod >> 24, EVENT_CLOCK_PHI1);
	}

	private void mileageCorrect() {
		// Calculate 1 bit below the timebase so we can round the mileage count
		if ((((m_sampleCount * 2 * SID2_TIME_BASE) / m_cfg.frequency) & 1) != 0)
			m_mileage++;
		m_sampleCount = 0;
	}

	/**
	 * Integrate SID emulation from the builder class into libsidplay2
	 * 
	 * @param builder
	 * @param userModel
	 * @param defaultModel
	 * @return
	 */
	private int sidCreate(SIDBuilder builder, sid2_model_t userModel,
			sid2_model_t defaultModel) {
		sid[0] = xsid.emulation();
		/***********************************************************************
		 * @FIXME@ Removed as prevents SID Model being updated
		 * *************************************** // If we are already using
		 * the emulation // then don't change if (builder == sid[0]->builder ()) {
		 * sid[0] = &xsid; return 0; }
		 **********************************************************************/

		// Make xsid forget it's emulation
		xsid.emulation(nullsid);

		{ // Release old sids
			for (int i = 0; i < SID2_MAX_SIDS; i++) {
				SIDBuilder b;
				b = sid[i].builder();
				if (b != null)
					b.unlock(sid[i]);
			}
		}

		if (builder == null || !builder.bool()) {
			// No sid
			for (int i = 0; i < SID2_MAX_SIDS; i++)
				sid[i] = nullsid;
		} else {
			// Detect the Correct SID model
			// Determine model when unknown
			if (m_tuneInfo.sidModel == SIDTUNE_SIDMODEL_UNKNOWN) {
				switch (defaultModel) {
				case SID2_MOS6581:
					m_tuneInfo.sidModel = SIDTUNE_SIDMODEL_6581;
					break;
				case SID2_MOS8580:
					m_tuneInfo.sidModel = SIDTUNE_SIDMODEL_8580;
					break;
				case SID2_MODEL_CORRECT:
					// No default so base it on emulation clock
					m_tuneInfo.sidModel = SIDTUNE_SIDMODEL_ANY;
				}
			}

			// Since song will run correct on any sid model
			// set it to the current emulation
			if (m_tuneInfo.sidModel == SIDTUNE_SIDMODEL_ANY) {
				if (userModel == SID2_MODEL_CORRECT)
					userModel = defaultModel;

				switch (userModel) {
				case SID2_MOS8580:
					m_tuneInfo.sidModel = SIDTUNE_SIDMODEL_8580;
					break;
				case SID2_MOS6581:
				default:
					m_tuneInfo.sidModel = SIDTUNE_SIDMODEL_6581;
					break;
				}
			}

			switch (userModel) {
			case SID2_MODEL_CORRECT:
				switch (m_tuneInfo.sidModel) {
				case SIDTUNE_SIDMODEL_8580:
					userModel = SID2_MOS8580;
					break;
				case SIDTUNE_SIDMODEL_6581:
					userModel = SID2_MOS6581;
					break;
				}
				break;
			// Fixup tune information if model is forced
			case SID2_MOS6581:
				m_tuneInfo.sidModel = SIDTUNE_SIDMODEL_6581;
				break;
			case SID2_MOS8580:
				m_tuneInfo.sidModel = SIDTUNE_SIDMODEL_8580;
				break;
			}

			for (int i = 0; i < SID2_MAX_SIDS; i++) {
				// Get first SID emulation
				sid[i] = builder.lock(this, userModel);
				if (sid[i] == null)
					sid[i] = nullsid;
				if ((i == 0) && !builder.bool())
					return -1;
				sid[0].optimisation(m_cfg.optimisation);
			}
		}
		xsid.emulation(sid[0]);
		sid[0] = xsid;
		return 0;
	}

	private void sidSamples(boolean enable) {
		byte /* int_least8_t */gain = 0;
		xsid.sidSamples(enable);

		// Now balance voices
		if (!enable)
			gain = -25;

		xsid.gain((short) (-100 - gain));
		sid[0] = xsid.emulation();
		for (int i = 0; i < SID2_MAX_SIDS; i++)
			sid[i].gain(gain);
		sid[0] = xsid;
	}

	private void reset() {
		m_playerState = sid2_stopped;
		m_running = false;
		m_sid2crc = 0xffffffff;
		m_info.sid2crc = m_sid2crc ^ 0xffffffff;
		m_sid2crcCount = m_info.sid2crcCount = 0;

		// Select Sidplay1 compatible CPU or real thing
		cpu = sid6510;
		sid6510.environment(m_info.environment);

		m_scheduler.reset();
		for (int i = 0; i < SID2_MAX_SIDS; i++) {
			SIDEmu s = sid[i];
			s.reset((short) 0x0f);
			// Synchronize the waveform generators
			// (must occur after reset)
			s.write((short) 0x04, (short) 0x08);
			s.write((short) 0x0b, (short) 0x08);
			s.write((short) 0x12, (short) 0x08);
			s.write((short) 0x04, (short) 0x00);
			s.write((short) 0x0b, (short) 0x00);
			s.write((short) 0x12, (short) 0x00);
		}

		if (m_info.environment == sid2_envR) {
			cia.reset();
			cia2.reset();
			vic.reset();
		} else {
			sid6526.reset(m_cfg.powerOnDelay <= SID2_MAX_POWER_ON_DELAY);
			sid6526.write((short) 0x0e, (short) 1); // Start timer
			if (m_tuneInfo.songSpeed == SIDTUNE_SPEED_VBI)
				sid6526.lock();
		}

		// Initialize Memory
		m_port.pr_in = 0;
		for (int i = 0; i < 0x10000; i++) {
			m_ram[i] = 0;
		}
		switch (m_info.environment) {
		case sid2_envPS:
			break;
		case sid2_envR: {
			// Initialize RAM with powerup pattern
			for (int i = 0x07c0; i < 0x10000; i += 128) {
				for (int j = 0; j < 64; j++) {
					m_ram[i + j] = 0xff;
				}
			}
			for (int i = 0; i < 0x10000; i++) {
				m_rom[i] = 0;
			}
			break;
		}
		default:
			for (int i = 0; i < 0x10000; i++) {
				m_rom[i] = 0;
			}
			for (int i = 0; i < 0x2000; i++) {
				m_rom[0xA000 + i] = RTSn;
			}
		}

		if (m_info.environment == sid2_envR) {
			for (int i = 0; i < KERNAL.length; i++) {
				m_rom[0xe000 + i] = KERNAL[i];
			}
			for (int i = 0; i < CHAR.length; i++) {
				m_rom[0xd000 + i] = CHAR[i];
			}
			m_rom[0xfd69] = 0x9f; // Bypass memory check
			m_rom[0xe55f] = 0x00; // Bypass screen clear
			m_rom[0xfdc4] = 0xea; // Ignore sid volume reset to avoid DC
			m_rom[0xfdc5] = 0xea; // click (potentially incompatibility)!!
			m_rom[0xfdc6] = 0xea;
			if (m_tuneInfo.compatibility == SIDTUNE_COMPATIBILITY_BASIC)
				for (int i = 0; i < BASIC.length; i++) {
					m_rom[0xa000 + i] = BASIC[i];
				}

			// Copy in power on settings. These were created by running
			// the kernel reset routine and storing the useful values
			// from $0000-$03ff. Format is:
			// -offset byte (bit 7 indicates presence rle byte)
			// -rle count byte (bit 7 indicates compression used)
			// data (single byte) or quantity represented by uncompressed count
			// -all counts and offsets are 1 less than they should be
			// if (m_tuneInfo.compatibility >= SIDTUNE_COMPATIBILITY_R64)
			{
				int /* uint_least16_t */addr = 0;
				for (int i = 0; i < POWERON.length;) {
					short /* uint8_t */off = POWERON[i++];
					short /* uint8_t */count = 0;
					boolean compressed = false;

					// Determine data count/compression
					if ((off & 0x80) != 0) {
						// fixup offset
						off &= 0x7f;
						count = POWERON[i++];
						if ((count & 0x80) != 0) {
							// fixup count
							count &= 0x7f;
							compressed = true;
						}
					}

					// Fix count off by ones (see format details)
					count++;
					addr += off;

					// Extract compressed data
					if (compressed) {
						short /* uint8_t */data = POWERON[i++];
						while (count-- > 0)
							m_ram[addr++] = data;
					}
					// Extract uncompressed data
					else {
						while (count-- > 0)
							m_ram[addr++] = POWERON[i++];
					}
				}
			}
		} else // !sid2_envR
		{
			for (int i = 0; i < 0x2000; i++) {
				m_rom[0xE000 + i] = RTSn;
			}
			// fake VBI-interrupts that do $D019, BMI ...
			m_rom[0x0d019] = 0xff;
			if (m_info.environment == sid2_envPS) {
				m_ram[0xff48] = JMPi;
				endian_little16(m_ram, 0xff49, 0x0314);
			}

			// Software vectors
			endian_little16(m_ram, 0x0314, 0xEA31); // IRQ
			endian_little16(m_ram, 0x0316, 0xFE66); // BRK
			endian_little16(m_ram, 0x0318, 0xFE47); // NMI
			// Hardware vectors
			if (m_info.environment == sid2_envPS)
				endian_little16(m_rom, 0xfffa, 0xFFFA); // NMI
			else
				endian_little16(m_rom, 0xfffa, 0xFE43); // NMI
			endian_little16(m_rom, 0xfffc, 0xFCE2); // RESET
			endian_little16(m_rom, 0xfffe, 0xFF48); // IRQ
			for (int i = 0; i < 6; i++) {
				m_ram[0xfffa + i] = m_rom[0xfffa + i];
			}
		}

		// Will get done later if can't now
		if (m_tuneInfo.clockSpeed == SIDTUNE_CLOCK_PAL)
			m_ram[0x02a6] = 1;
		else
			// SIDTUNE_CLOCK_NTSC
			m_ram[0x02a6] = 0;
	}

	/**
	 * Temporary hack till real bank switching code added
	 * 
	 * @param addr
	 * A 16-bit effective address
	 * @return A default bank-select value for $01.
	 */
	private short /* uint8_t */iomap(int /* uint_least16_t */addr) {
		if (m_info.environment != sid2_envPS) {
			// Force Real C64 Compatibility
			switch (m_tuneInfo.compatibility) {
			case SIDTUNE_COMPATIBILITY_R64:
			case SIDTUNE_COMPATIBILITY_BASIC:
				return 0; // Special case, converted to 0x37 later
			}

			if (addr == 0)
				return 0; // Special case, converted to 0x37 later
			if (addr < 0xa000)
				return 0x37; // Basic-ROM, Kernal-ROM, I/O
			if (addr < 0xd000)
				return 0x36; // Kernal-ROM, I/O
			if (addr >= 0xe000)
				return 0x35; // I/O only
		}
		return 0x34; // RAM only (special I/O in PlaySID mode)
	}

	private short /* uint8_t */readMemByte_plain(int /* uint_least16_t */addr) {
		// Bank Select Register Value DOES NOT get to ram
		if (addr > 1)
			return m_ram[addr & 0xffff];
		else if (addr != 0)
			return m_port.pr_in;
		return m_port.ddr;
	}

	private short /* uint8_t */readMemByte_io(int /* uint_least16_t */addr) {
		int /* uint_least16_t */tempAddr = (addr & 0xfc1f);

		// Not SID ?
		if ((tempAddr & 0xff00) != 0xd400) {
			if (m_info.environment == sid2_envR) {
				switch (endian_16hi8(addr)) {
				case 0:
				case 1:
					return readMemByte_plain(addr);
				case 0xdc:
					return cia.read((short) (addr & 0x0f));
				case 0xdd:
					return cia2.read((short) (addr & 0x0f));
				case 0xd0:
				case 0xd1:
				case 0xd2:
				case 0xd3:
					return vic.read((short) (addr & 0x3f));
				default:
					return m_rom[addr & 0xffff];
				}
			} else {
				switch (endian_16hi8(addr)) {
				case 0:
				case 1:
					return readMemByte_plain(addr);
					// Sidplay1 Random Extension CIA
				case 0xdc:
					return sid6526.read((short) (addr & 0x0f));
					// Sidplay1 Random Extension VIC
				case 0xd0:
					switch (addr & 0x3f) {
					case 0x11:
					case 0x12:
						return sid6526.read((short) ((addr - 13) & 0x0f));
					}
					// Deliberate run on
				default:
					return m_rom[addr & 0xffff];
				}
			}
		}

		{ // Read real sid for these
			int i = m_sidmapper[(addr >> 5) & (SID2_MAPPER_SIZE - 1)];
			return sid[i].read((short) (tempAddr & 0xff));
		}
	}

	private short /* uint8_t */readMemByte_sidplaytp(
			int /* uint_least16_t */addr) {
		if (addr < 0xD000)
			return readMemByte_plain(addr);
		else {
			// Get high-nibble of address.
			switch (addr >> 12) {
			case 0xd:
				if (isIO)
					return readMemByte_io(addr);
				else
					return m_ram[addr];
				// break;
			case 0xe:
			case 0xf:
			default: // <-- just to please the compiler
				return m_ram[addr & 0xffff];
			}
		}
	}

	private short /* uint8_t */readMemByte_sidplaybs(
			int /* uint_least16_t */addr) {
		if (addr < 0xA000)
			return readMemByte_plain(addr);
		else {
			// Get high-nibble of address.
			switch (addr >> 12) {
			case 0xa:
			case 0xb:
				if (isBasic)
					return m_rom[addr];
				else
					return m_ram[addr];
				// break;
			case 0xc:
				return m_ram[addr];
				// break;
			case 0xd:
				if (isIO)
					return readMemByte_io(addr);
				else if (isChar)
					return m_rom[addr];
				else
					return m_ram[addr];
				// break;
			case 0xe:
			case 0xf:
			default: // <-- just to please the compiler
				if (isKernal)
					return m_rom[addr & 0xffff];
				else
					return m_ram[addr & 0xffff];
			}
		}
	}

	private void writeMemByte_plain(int /* uint_least16_t */addr,
			short /* uint8_t */data) {
		if (addr > 1)
			m_ram[addr & 0xffff] = data;
		else if (addr != 0) { // Determine new memory configuration.
			evalBankSelect(data);
		} else {
			m_port.ddr = data;
			evalBankSelect(m_port.pr_out);
		}
	}

	private void writeMemByte_playsid(int /* uint_least16_t */addr,
			short /* uint8_t */data) {
		int /* uint_least16_t */tempAddr = (addr & 0xfc1f);

		// Not SID ?
		if ((tempAddr & 0xff00) != 0xd400) {
			if (m_info.environment == sid2_envR) {
				switch (endian_16hi8(addr)) {
				case 0:
				case 1:
					writeMemByte_plain(addr, data);
					return;
				case 0xdc:
					cia.write((short) (addr & 0x0f), data);
					return;
				case 0xdd:
					cia2.write((short) (addr & 0x0f), data);
					return;
				case 0xd0:
				case 0xd1:
				case 0xd2:
				case 0xd3:
					vic.write((short) (addr & 0x3f), data);
					return;
				default:
					m_rom[addr & 0xffff] = data;
					return;
				}
			} else {
				switch (endian_16hi8(addr)) {
				case 0:
				case 1:
					writeMemByte_plain(addr, data);
					return;
				case 0xdc: // Sidplay1 CIA
					sid6526.write((short) (addr & 0x0f), data);
					return;
				default:
					m_rom[addr & 0xffff] = data;
					return;
				}
			}
		}

		// $D41D/1E/1F, $D43D/3E/3F, ...
		// Map to real address to support PlaySID
		// Extended SID Chip Registers.
		sid2crc(data);
		if ((tempAddr & 0x00ff) >= 0x001d)
			xsid.write16(addr & 0x01ff, data);
		else // Mirrored SID.
		{
			int i = m_sidmapper[(addr >> 5) & (SID2_MAPPER_SIZE - 1)];
			// Convert address to that acceptable by resid
			sid[i].write((short) (tempAddr & 0xff), data);
			// Support dual sid
			if (m_emulateStereo)
				sid[1].write((short) (tempAddr & 0xff), data);
		}
	}

	private void writeMemByte_sidplay(int /* uint_least16_t */addr,
			short /* uint8_t */data) {
		if (addr < 0xA000)
			writeMemByte_plain(addr, data);
		else {
			// Get high-nibble of address.
			switch (addr >> 12) {
			case 0xa:
			case 0xb:
			case 0xc:
				m_ram[addr] = data;
				break;
			case 0xd:
				if (isIO)
					writeMemByte_playsid(addr, data);
				else
					m_ram[addr] = data;
				break;
			case 0xe:
			case 0xf:
			default: // <-- just to please the compiler
				m_ram[addr & 0xffff] = data;
			}
		}
	}

	private interface IMem {
		short /* uint8_t */m_readMemByte(int /* uint_least16_t */addr);

		void m_writeMemByte(int /* uint_least16_t */addr,
				short /* uint8_t */data);

		short /* uint8_t */m_readMemDataByte(int /* uint_least16_t */addr);
	}

	private IMem m_mem;

	/**
	 * This resets the cpu once the program is loaded to begin running. Also
	 * called when the emulation crashes
	 * 
	 * @param safe
	 */
	private void envReset(boolean safe) {
		if (safe) { // Emulation crashed so run in safe mode
			if (m_info.environment == sid2_envR) {
				short /* uint8_t */prg[] = {
						LDAb, 0x7f, STAa, 0x0d, 0xdc, RTSn };
				sid2_info_t info = new sid2_info_t();
				SidTuneInfo tuneInfo = new SidTuneInfo();
				// Install driver
				tuneInfo.relocStartPage = 0x09;
				tuneInfo.relocPages = 0x20;
				tuneInfo.initAddr = 0x0800;
				tuneInfo.songSpeed = SIDTUNE_SPEED_CIA_1A;
				info.environment = m_info.environment;
				psidDrvReloc(tuneInfo, info);
				// Install prg & driver
				for (int i = 0; i < prg.length; i++) {
					m_ram[0x0800 + i] = prg[i];
				}
				psidDrvInstall(info);
			} else {
				// If there is no irqs, song wont continue
				sid6526.reset();
			}

			// Make sids silent
			for (int i = 0; i < SID2_MAX_SIDS; i++)
				sid[i].reset((short) 0);
		}

		m_port.ddr = 0x2F;

		// defaults: Basic-ROM on, Kernal-ROM on, I/O on
		if (m_info.environment != sid2_envR) {
			short /* uint8_t */song = (short) (m_tuneInfo.currentSong - 1);
			short /* uint8_t */bank = iomap(m_tuneInfo.initAddr);
			evalBankSelect(bank);
			m_playBank = iomap(m_tuneInfo.playAddr);
			if (m_info.environment != sid2_envPS)
				sid6510.reset(m_tuneInfo.initAddr, song, (short) 0, (short) 0);
			else
				sid6510.reset(m_tuneInfo.initAddr, song, song, song);
		} else {
			evalBankSelect((short) 0x37);
			cpu.reset();
		}

		mixerReset();
		xsid.suppress(true);
	}

	interface IOutput {
		// Rev 2.0.3 Added - New Mixer Routines
		long /* uint_least32_t */output(short[] buffer, int off);

	}

	private IOutput output;

	//
	// Generic sound output generation routines
	// Rev 2.0.4 (saw) - Added to reduce code size
	//

	private long /* int_least32_t */monoOutGenericLeftIn(
			short /* uint_least8_t */bits) {
		return sid[0].output(bits) * m_leftVolume / VOLUME_MAX;
	}

	private long /* int_least32_t */monoOutGenericStereoIn(
			short /* uint_least8_t */bits) {
		// Convert to mono
		return ((sid[0].output(bits) * m_leftVolume) + (sid[1].output(bits) * m_rightVolume))
				/ (VOLUME_MAX * 2);
	}

	private long /* int_least32_t */monoOutGenericRightIn(
			short /* uint_least8_t */bits) {
		return sid[1].output(bits) * m_rightVolume / VOLUME_MAX;
	}

	//
	// 8 bit sound output generation routines
	//

	private long /* uint_least32_t */monoOut8MonoIn(short[] buffer, int off) {
		buffer[off] = (short) ((byte) (monoOutGenericLeftIn((short) 8) ^ 0x80));
		return 1;
	}

	private long /* uint_least32_t */monoOut8StereoIn(short[] buffer, int off) {
		buffer[off] = (short) ((byte) (monoOutGenericStereoIn((short) 8) ^ 0x80));
		return 1;
	}

	private long /* uint_least32_t */monoOut8StereoRIn(short[] buffer, int off) {
		buffer[off] = (short) ((byte) (monoOutGenericRightIn((short) 8) ^ 0x80));
		return 1;
	}

	private long /* uint_least32_t */stereoOut8MonoIn(short[] buffer, int off) {
		short sample = (short) ((byte) (monoOutGenericLeftIn((short) 8) ^ 0x80));
		buffer[off + 0] = sample;
		buffer[off + 1] = sample;
		return 2;
	}

	private long /* uint_least32_t */stereoOut8StereoIn(short[] buffer, int off) {
		buffer[off + 0] = (short) ((byte) (monoOutGenericLeftIn((short) 8) ^ 0x80));
		buffer[off + 1] = (short) ((byte) (monoOutGenericRightIn((short) 8) ^ 0x80));
		return 2;
	}

	//
	// 16 bit sound output generation routines
	// Rev 2.0.4 (jp) - Added 16 bit support
	//

	private long /* uint_least32_t */monoOut16MonoIn(short[] buffer, int off) {
		endian_16(buffer, off,
				(int /* uint_least16_t */) monoOutGenericLeftIn((short) 16));
		return 2;
	}

	private long /* uint_least32_t */monoOut16StereoIn(short[] buffer, int off) {
		endian_16(buffer, off,
				(int /* uint_least16_t */) monoOutGenericStereoIn((short) 16));
		return 2;
	}

	private long /* uint_least32_t */monoOut16StereoRIn(short[] buffer, int off) {
		endian_16(buffer, off,
				(int /* uint_least16_t */) monoOutGenericRightIn((short) 16));
		return 2;
	}

	private long /* uint_least32_t */stereoOut16MonoIn(short[] buffer, int off) {
		int /* uint_least16_t */sample = (int /* uint_least16_t */) monoOutGenericLeftIn((short) 16);
		endian_16(buffer, off, sample);
		endian_16(buffer, off + 2, sample);
		return (4);
	}

	private long /* uint_least32_t */stereoOut16StereoIn(short[] buffer,
			int off) {
		endian_16(buffer, off,
				(int /* uint_least16_t */) monoOutGenericLeftIn((short) 16));
		endian_16(buffer, off + 2,
				(int /* uint_least16_t */) monoOutGenericRightIn((short) 16));
		return (4);
	}

	public void interruptIRQ(boolean state) {
		if (state) {
			if (m_info.environment == sid2_envR)
				cpu.triggerIRQ();
			else
				fakeIRQ();
		} else
			cpu.clearIRQ();
	}

	public void interruptNMI() {
		cpu.triggerNMI();
	}

	public void interruptRST() {
		stop();
	}

	public void signalAEC(boolean state) {
		cpu.aecSignal(state);
	}

	public short /* uint8_t */readMemRamByte(int /* uint_least16_t */addr) {
		return m_ram[addr];
	}

	/**
	 * Used for sid2crc (tracking sid register writes)
	 */
	private static final long crc32Table[] = {
			0x00000000, 0x77073096, 0xEE0E612C, 0x990951BA, 0x076DC419,
			0x706AF48F, 0xE963A535, 0x9E6495A3, 0x0EDB8832, 0x79DCB8A4,
			0xE0D5E91E, 0x97D2D988, 0x09B64C2B, 0x7EB17CBD, 0xE7B82D07,
			0x90BF1D91, 0x1DB71064, 0x6AB020F2, 0xF3B97148, 0x84BE41DE,
			0x1ADAD47D, 0x6DDDE4EB, 0xF4D4B551, 0x83D385C7, 0x136C9856,
			0x646BA8C0, 0xFD62F97A, 0x8A65C9EC, 0x14015C4F, 0x63066CD9,
			0xFA0F3D63, 0x8D080DF5, 0x3B6E20C8, 0x4C69105E, 0xD56041E4,
			0xA2677172, 0x3C03E4D1, 0x4B04D447, 0xD20D85FD, 0xA50AB56B,
			0x35B5A8FA, 0x42B2986C, 0xDBBBC9D6, 0xACBCF940, 0x32D86CE3,
			0x45DF5C75, 0xDCD60DCF, 0xABD13D59, 0x26D930AC, 0x51DE003A,
			0xC8D75180, 0xBFD06116, 0x21B4F4B5, 0x56B3C423, 0xCFBA9599,
			0xB8BDA50F, 0x2802B89E, 0x5F058808, 0xC60CD9B2, 0xB10BE924,
			0x2F6F7C87, 0x58684C11, 0xC1611DAB, 0xB6662D3D, 0x76DC4190,
			0x01DB7106, 0x98D220BC, 0xEFD5102A, 0x71B18589, 0x06B6B51F,
			0x9FBFE4A5, 0xE8B8D433, 0x7807C9A2, 0x0F00F934, 0x9609A88E,
			0xE10E9818, 0x7F6A0DBB, 0x086D3D2D, 0x91646C97, 0xE6635C01,
			0x6B6B51F4, 0x1C6C6162, 0x856530D8, 0xF262004E, 0x6C0695ED,
			0x1B01A57B, 0x8208F4C1, 0xF50FC457, 0x65B0D9C6, 0x12B7E950,
			0x8BBEB8EA, 0xFCB9887C, 0x62DD1DDF, 0x15DA2D49, 0x8CD37CF3,
			0xFBD44C65, 0x4DB26158, 0x3AB551CE, 0xA3BC0074, 0xD4BB30E2,
			0x4ADFA541, 0x3DD895D7, 0xA4D1C46D, 0xD3D6F4FB, 0x4369E96A,
			0x346ED9FC, 0xAD678846, 0xDA60B8D0, 0x44042D73, 0x33031DE5,
			0xAA0A4C5F, 0xDD0D7CC9, 0x5005713C, 0x270241AA, 0xBE0B1010,
			0xC90C2086, 0x5768B525, 0x206F85B3, 0xB966D409, 0xCE61E49F,
			0x5EDEF90E, 0x29D9C998, 0xB0D09822, 0xC7D7A8B4, 0x59B33D17,
			0x2EB40D81, 0xB7BD5C3B, 0xC0BA6CAD, 0xEDB88320, 0x9ABFB3B6,
			0x03B6E20C, 0x74B1D29A, 0xEAD54739, 0x9DD277AF, 0x04DB2615,
			0x73DC1683, 0xE3630B12, 0x94643B84, 0x0D6D6A3E, 0x7A6A5AA8,
			0xE40ECF0B, 0x9309FF9D, 0x0A00AE27, 0x7D079EB1, 0xF00F9344,
			0x8708A3D2, 0x1E01F268, 0x6906C2FE, 0xF762575D, 0x806567CB,
			0x196C3671, 0x6E6B06E7, 0xFED41B76, 0x89D32BE0, 0x10DA7A5A,
			0x67DD4ACC, 0xF9B9DF6F, 0x8EBEEFF9, 0x17B7BE43, 0x60B08ED5,
			0xD6D6A3E8, 0xA1D1937E, 0x38D8C2C4, 0x4FDFF252, 0xD1BB67F1,
			0xA6BC5767, 0x3FB506DD, 0x48B2364B, 0xD80D2BDA, 0xAF0A1B4C,
			0x36034AF6, 0x41047A60, 0xDF60EFC3, 0xA867DF55, 0x316E8EEF,
			0x4669BE79, 0xCB61B38C, 0xBC66831A, 0x256FD2A0, 0x5268E236,
			0xCC0C7795, 0xBB0B4703, 0x220216B9, 0x5505262F, 0xC5BA3BBE,
			0xB2BD0B28, 0x2BB45A92, 0x5CB36A04, 0xC2D7FFA7, 0xB5D0CF31,
			0x2CD99E8B, 0x5BDEAE1D, 0x9B64C2B0, 0xEC63F226, 0x756AA39C,
			0x026D930A, 0x9C0906A9, 0xEB0E363F, 0x72076785, 0x05005713,
			0x95BF4A82, 0xE2B87A14, 0x7BB12BAE, 0x0CB61B38, 0x92D28E9B,
			0xE5D5BE0D, 0x7CDCEFB7, 0x0BDBDF21, 0x86D3D2D4, 0xF1D4E242,
			0x68DDB3F8, 0x1FDA836E, 0x81BE16CD, 0xF6B9265B, 0x6FB077E1,
			0x18B74777, 0x88085AE6, 0xFF0F6A70, 0x66063BCA, 0x11010B5C,
			0x8F659EFF, 0xF862AE69, 0x616BFFD3, 0x166CCF45, 0xA00AE278,
			0xD70DD2EE, 0x4E048354, 0x3903B3C2, 0xA7672661, 0xD06016F7,
			0x4969474D, 0x3E6E77DB, 0xAED16A4A, 0xD9D65ADC, 0x40DF0B66,
			0x37D83BF0, 0xA9BCAE53, 0xDEBB9EC5, 0x47B2CF7F, 0x30B5FFE9,
			0xBDBDF21C, 0xCABAC28A, 0x53B39330, 0x24B4A3A6, 0xBAD03605,
			0xCDD70693, 0x54DE5729, 0x23D967BF, 0xB3667A2E, 0xC4614AB8,
			0x5D681B02, 0x2A6F2B94, 0xB40BBE37, 0xC30C8EA1, 0x5A05DF1B,
			0x2D02EF8D };

	public void sid2crc(short /* uint8_t */data) {
		if (m_sid2crcCount < m_cfg.sid2crcCount) {
			m_info.sid2crcCount = ++m_sid2crcCount;
			m_sid2crc = (m_sid2crc >> 8)
					^ crc32Table[(int) ((m_sid2crc & 0xFF) ^ data)];
			m_info.sid2crc = m_sid2crc ^ 0xffffffff;
		}
	}

	public void lightpen() {
		vic.lightpen();
	}

	//
	// PSID driver
	//

	private int psidDrvReloc(SidTuneInfo tuneInfo, sid2_info_t info) {
		int /* uint_least16_t */relocAddr;
		int startlp = tuneInfo.loadAddr >> 8;
		int endlp = (int) ((tuneInfo.loadAddr + (tuneInfo.c64dataLen - 1)) >> 8);

		if (info.environment != sid2_envR) {
			// Sidplay1 modes require no psid driver
			info.driverAddr = 0;
			info.driverLength = 0;
			info.powerOnDelay = 0;
			return 0;
		}

		if (tuneInfo.compatibility == SIDTUNE_COMPATIBILITY_BASIC) {
			// The psiddrv is only used for initialization and to autorun basic
			// tunes as running the kernel falls into a manual load/run mode
			tuneInfo.relocStartPage = 0x04;
			tuneInfo.relocPages = 0x03;
		}

		// Check for free space in tune
		if (tuneInfo.relocStartPage == PSIDDRV_MAX_PAGE)
			tuneInfo.relocPages = 0;
		// Check if we need to find the reloc addr
		else if (tuneInfo.relocStartPage == 0) {
			// Tune is clean so find some free ram around the load image
			psidRelocAddr(tuneInfo, startlp, endlp);
		} else {
			// Check reloc information mode
			/* int startrp = tuneInfo.relocStartPage; */
			/* int endrp = startrp + (tuneInfo.relocPages - 1); */

			// New relocation implementation (exclude region)
			// to complement existing method rejected as being
			// unnecessary. From tests in most cases this
			// method increases memory availibility.
			/*******************************************************************
			 * if ((startrp <= startlp) && (endrp >= endlp)) { // Is describing
			 * used space so find some free // ram outside this range
			 * psidRelocAddr (tuneInfo, startrp, endrp); }
			 ******************************************************************/
		}

		if (tuneInfo.relocPages < 1) {
			m_errorString = ERR_PSIDDRV_NO_SPACE;
			return -1;
		}

		relocAddr = tuneInfo.relocStartPage << 8;

		{ // Place psid driver into ram
			short /* uint8_t */[] reloc_driver = PSIDDRV;
			int reloc_size = PSIDDRV.length;

			BufPos bp;
			if ((bp = reloc65(reloc_driver, reloc_size, relocAddr - 10)) == null) {
				m_errorString = ERR_PSIDDRV_RELOC;
				return -1;
			}
			reloc_driver = bp.fBuf;
			int reloc_driverPos = bp.fPos;
			reloc_size = bp.fSize;

			// Adjust size to not included initialization data.
			reloc_size -= 10;
			info.driverAddr = relocAddr;
			info.driverLength = (int /* uint_least16_t */) reloc_size;
			// Round length to end of page
			info.driverLength += 0xff;
			info.driverLength &= 0xff00;

			m_rom[0xfffc] = reloc_driver[reloc_driverPos + 0];/* RESET */
			m_rom[0xfffd] = reloc_driver[reloc_driverPos + 1];/* RESET */

			// If not a basic tune then the psiddrv must install
			// interrupt hooks and trap programs trying to restart basic
			if (tuneInfo.compatibility == SIDTUNE_COMPATIBILITY_BASIC) {
				// Install hook to set subtune number for basic
				short /* uint8_t */prg[] = {
						LDAb,
						(short /* uint8_t */) (tuneInfo.currentSong - 1),
						STAa, 0x0c, 0x03, JSRw, 0x2c, 0xa8, JMPw, 0xb1, 0xa7 };
				for (int i = 0; i < prg.length; i++) {
					m_rom[0xbf53 + i] = prg[i];
				}
				m_rom[0xa7ae] = JMPw;
				endian_little16(m_rom, 0xa7af, 0xbf53);
			} else { // Only install irq handle for RSID tunes
				if (tuneInfo.compatibility == SIDTUNE_COMPATIBILITY_R64) {
					m_ram[0x0314] = reloc_driver[reloc_driverPos + 2];
					m_ram[0x0315] = reloc_driver[reloc_driverPos + 2 + 1];
				} else {
					m_ram[0x0314] = reloc_driver[reloc_driverPos + 2];
					m_ram[0x0315] = reloc_driver[reloc_driverPos + 2 + 1];
					m_ram[0x0316] = reloc_driver[reloc_driverPos + 2 + 2];
					m_ram[0x0317] = reloc_driver[reloc_driverPos + 2 + 3];
					m_ram[0x0318] = reloc_driver[reloc_driverPos + 2 + 4];
					m_ram[0x0319] = reloc_driver[reloc_driverPos + 2 + 5];
				}
				// Experimental restart basic trap
				int /* uint_least16_t */addr;
				addr = endian_little16(reloc_driver, reloc_driverPos + 8);
				m_rom[0xa7ae] = JMPw;
				endian_little16(m_rom, 0xa7af, 0xffe1);
				endian_little16(m_ram, 0x0328, addr);
			}
			// Install driver to rom so it can be copied later into
			// ram once the tune is installed.
			// memcpy (&m_ram[relocAddr], &reloc_driver[10], reloc_size);
			for (int i = 0; i < reloc_size; i++) {
				m_rom[i] = reloc_driver[reloc_driverPos + 10 + i];
			}
		}

		{ // Setup the Initial entry point
			short /* uint8_t */[] addr = m_rom; // &m_ram[relocAddr];
			int pos = 0;

			// Tell C64 about song
			addr[pos++] = (short /* uint8_t */) (tuneInfo.currentSong - 1);
			if (tuneInfo.songSpeed == SIDTUNE_SPEED_VBI)
				addr[pos] = 0;
			else
				// SIDTUNE_SPEED_CIA_1A
				addr[pos] = 1;

			pos++;
			endian_little16(
					addr,
					pos,
					tuneInfo.compatibility == SIDTUNE_COMPATIBILITY_BASIC ? 0xbf55 /*
																					 * Was
																					 * 0xa7ae,
																					 * see
																					 * above
																					 */
							: tuneInfo.initAddr);
			pos += 2;
			endian_little16(addr, pos, tuneInfo.playAddr);
			pos += 2;
			// Initialize random number generator
			info.powerOnDelay = m_cfg.powerOnDelay;
			// Delays above MAX result in random delays
			if (info.powerOnDelay > SID2_MAX_POWER_ON_DELAY) {
				// Limit the delay to something sensible.
				info.powerOnDelay = (short /* uint_least16_t */) (m_rand >> 3)
						& SID2_MAX_POWER_ON_DELAY;
			}
			endian_little16(addr, pos, info.powerOnDelay);
			pos += 2;
			m_rand = m_rand * 13 + 1;
			addr[pos++] = iomap(m_tuneInfo.initAddr);
			addr[pos++] = iomap(m_tuneInfo.playAddr);
			addr[pos + 1] = (addr[pos + 0] = m_ram[0x02a6]); // PAL/NTSC flag
			pos++;

			// Add the required tune speed
			switch ((m_tune.getInfo()).clockSpeed) {
			case SIDTUNE_CLOCK_PAL:
				addr[pos++] = 1;
				break;
			case SIDTUNE_CLOCK_NTSC:
				addr[pos++] = 0;
				break;
			default: // UNKNOWN or ANY
				pos++;
				break;
			}

			// Default processor register flags on calling init
			if (tuneInfo.compatibility >= SIDTUNE_COMPATIBILITY_R64)
				addr[pos++] = 0;
			else
				addr[pos++] = 1 << MOS6510.SR_INTERRUPT;
		}
		return 0;
	}

	/**
	 * The driver is relocated above and here is actually installed into ram.
	 * The two operations are now split to allow the driver to be installed
	 * inside the load image
	 * 
	 * @param info
	 */
	private void psidDrvInstall(sid2_info_t info) {
		for (int i = 0; i < info.driverLength; i++) {
			m_ram[info.driverAddr + i] = m_rom[i];
		}
	}

	private void psidRelocAddr(SidTuneInfo tuneInfo, int startp, int endp) {
		// Used memory ranges.
		boolean pages[] = new boolean[256];
		int used[] = {
				0x00, 0x03, 0xa0, 0xbf, 0xd0, 0xff, startp,
				(startp <= endp) && (endp <= 0xff) ? endp : 0xff };

		// Mark used pages in table.
		for (int i = 0; i < pages.length; i++) {
			pages[i] = false;
		}
		for (int i = 0; i < used.length; i += 2) {
			for (int page = used[i]; page <= used[i + 1]; page++)
				pages[page] = true;
		}

		{ // Find largest free range.
			int relocPages, lastPage = 0;
			tuneInfo.relocPages = 0;
			for (int page = 0; page < pages.length; page++) {
				if (pages[page] == false)
					continue;
				relocPages = page - lastPage;
				if (relocPages > tuneInfo.relocPages) {
					tuneInfo.relocStartPage = (short) lastPage;
					tuneInfo.relocPages = (short) relocPages;
				}
				lastPage = page + 1;
			}
		}

		if (tuneInfo.relocPages == 0)
			tuneInfo.relocStartPage = PSIDDRV_MAX_PAGE;
	}

	/**
	 * Set the ICs environment variable to point to this player
	 */
	public Player() {
		// Set default settings for system
		super(new EventScheduler("SIDPlay 2"));
		m_scheduler = (EventScheduler) context();
		// Environment Function entry Points
		envp = new C64Environment() {

			@Override
			protected void envReset() {
				Player.this.envReset(true);
			}

			@Override
			protected short envReadMemByte(int addr) {
				// from plain only to prevent execution of rom code
				return m_mem.m_readMemByte(addr);
			}

			@Override
			protected void envWriteMemByte(int addr, short data) {
				m_mem.m_writeMemByte(addr, data);
			}

			@Override
			protected boolean envCheckBankJump(int addr) {
				switch (m_info.environment) {
				case sid2_envBS:
					if (addr >= 0xA000) {
						// Get high-nibble of address.
						switch (addr >> 12) {
						case 0xa:
						case 0xb:
							if (isBasic)
								return false;
							break;

						case 0xc:
							break;

						case 0xd:
							if (isIO)
								return false;
							break;

						case 0xe:
						case 0xf:
						default: // <-- just to please the compiler
							if (isKernal)
								return false;
							break;
						}
					}
					break;

				case sid2_envTP:
					if ((addr >= 0xd000) && isKernal)
						return false;
					break;

				default:
					break;
				}

				return true;
			}

			@Override
			protected short envReadMemDataByte(int addr) {
				// from plain only to prevent execution of rom code
				return m_mem.m_readMemDataByte(addr);
			}

			@Override
			protected void envSleep() {
				if (m_info.environment != sid2_envR) {
					// Start the sample sequence
					xsid.suppress(false);
					xsid.suppress(true);
				}
			}

			@Override
			protected void envLoadFile(String file) {
				StringBuffer name = new StringBuffer("E:/testsuite/");
				name.append(file);
				name.append(".prg");
				m_tune.load(name.toString());
				stop();
			}

		};
		sid6510 = new SID6510(m_scheduler);
		mos6510 = new MOS6510(m_scheduler);
		cpu = sid6510;
		xsid = new C64XSID(this, nullsid = new NullSID());
		//C64CIA ciaSupport = new C64CIA();
		cia = new C64CIA.C64cia1(this);
		cia2 = new C64CIA.C64cia2(this);
		sid6526 = new SID6526(this);
		vic = new C64VIC(this);
		mixerEvent = new EventMixer(this);
		rtc = new EventRTC(m_scheduler);
		m_tune = (null);
		m_ram = (null);
		m_rom = (null);
		m_errorString = (TXT_NA);
		m_fastForwardFactor = (1.0);
		m_mileage = (0);
		m_playerState = (sid2_stopped);
		m_running = (false);
		m_sid2crc = (0xffffffff);
		m_sid2crcCount = (0);
		m_emulateStereo = (true);
		m_sampleCount = (0);

		m_rand = (int /* uint_least32_t */) System.currentTimeMillis();

		// Set the ICs to use this environment
		sid6510.setEnvironment(envp);
		mos6510.setEnvironment(envp);

		// SID Initialize
		{
			for (int i = 0; i < SID2_MAX_SIDS; i++)
				sid[i] = nullsid;
		}
		xsid.emulation(sid[0]);
		sid[0] = xsid;
		// Setup sid mapping table
		{
			for (int i = 0; i < SID2_MAPPER_SIZE; i++)
				m_sidmapper[i] = 0;
		}

		// Setup exported info
		m_info.credits = credit;
		m_info.channels = 1;
		m_info.driverAddr = 0;
		m_info.driverLength = 0;
		m_info.name = PACKAGE_NAME;
		m_info.tuneInfo = null;
		m_info.version = PACKAGE_VERSION;
		m_info.eventContext = context();
		// Number of SIDs support by this library
		m_info.maxsids = SID2_MAX_SIDS;
		m_info.environment = sid2_envR;
		m_info.sid2crc = 0;
		m_info.sid2crcCount = 0;

		// Configure default settings
		m_cfg.clockDefault = SID2_CLOCK_CORRECT;
		m_cfg.clockForced = false;
		m_cfg.clockSpeed = SID2_CLOCK_CORRECT;
		m_cfg.environment = m_info.environment;
		m_cfg.forceDualSids = false;
		m_cfg.emulateStereo = m_emulateStereo;
		m_cfg.frequency = SID2_DEFAULT_SAMPLING_FREQ;
		m_cfg.optimisation = SID2_DEFAULT_OPTIMISATION;
		m_cfg.playback = sid2_mono;
		m_cfg.precision = SID2_DEFAULT_PRECISION;
		m_cfg.sidDefault = SID2_MODEL_CORRECT;
		m_cfg.sidEmulation = null;
		m_cfg.sidModel = SID2_MODEL_CORRECT;
		m_cfg.sidSamples = true;
		m_cfg.leftVolume = 255;
		m_cfg.rightVolume = 255;
		m_cfg.sampleFormat = SID2_LITTLE_SIGNED;
		m_cfg.powerOnDelay = SID2_DEFAULT_POWER_ON_DELAY;
		m_cfg.sid2crcCount = 0;

		// Configured by default for Sound Blaster (compatibles)
//		if (SID2_DEFAULT_PRECISION == 8)
//			m_cfg.sampleFormat = SID2_LITTLE_UNSIGNED;
		config(m_cfg);

		// Get component credits
		credit[0] = PACKAGE_NAME + " V" + PACKAGE_VERSION
				+ " Engine:\n\tCopyright (C) 2000 Simon White <"
				+ S_A_WHITE_EMAIL + ">\n"
				+ "\thttp://sidplay2.sourceforge.net\n";
		credit[1] = xsid.credits();
		credit[2] = "*MOS6510 (CPU) Emulation:\n\tCopyright (C) 2000 Simon White <"
				+ S_A_WHITE_EMAIL + ">\n";
		credit[3] = cia.credits();
		credit[4] = vic.credits();
		credit[5] = null;
	}

	public final sid2_config_t config() {
		return m_cfg;
	}

	public final sid2_info_t info() {
		return m_info;
	}

	public int config(final sid2_config_t cfg) {
		boolean monosid = false;

		if (m_running) {
			m_errorString = ERR_CONF_WHILST_ACTIVE;
			return -1;
		}

		// Check for base sampling frequency
		if (cfg.frequency < 4000) {
			// Rev 1.6 (saw) - Added descriptive error
			m_errorString = ERR_UNSUPPORTED_FREQ;
			return -1;
		}

		// Check for legal precision
		switch (cfg.precision) {
		case 8:
		case 16:
		case 24:
			if (cfg.precision > SID2_MAX_PRECISION) {
				// Rev 1.6 (saw) - Added
				// descriptive error
				m_errorString = ERR_UNSUPPORTED_PRECISION;
				return -1;
			}
			break;

		default:
			// Rev 1.6 (saw) - Added descriptive error
			m_errorString = ERR_UNSUPPORTED_PRECISION;
			return -1;
		}

		// Only do these if we have a loaded tune
		if (m_tune != null && m_tune.bool()) {
			if (m_playerState != sid2_paused)
				m_tuneInfo = m_tune.getInfo();

			// SID emulation setup (must be performed before the
			// environment setup call)
			if (sidCreate(cfg.sidEmulation, cfg.sidModel, cfg.sidDefault) < 0) {
				m_errorString = cfg.sidEmulation.error();
				m_cfg.sidEmulation = null;
				// Try restoring old configuration
				if (m_cfg != cfg)
					config(m_cfg);
				return -1;
			}

			if (m_playerState != sid2_paused) {
				double /* float64_t */cpuFreq;
				// Must be this order:
				// Determine clock speed
				cpuFreq = clockSpeed(cfg.clockSpeed, cfg.clockDefault,
						cfg.clockForced);
				// Fixed point conversion 16.16
				m_samplePeriod = (long /* event_clock_t */) (cpuFreq
						/ (double /* float64_t */) cfg.frequency * (1 << 16) * m_fastForwardFactor);
				// Setup fake cia
				sid6526.clock((int /* uint_least16_t */) (cpuFreq
						/ VIC_FREQ_PAL + 0.5));
				if (m_tuneInfo.songSpeed == SIDTUNE_SPEED_CIA_1A
						|| m_tuneInfo.clockSpeed == SIDTUNE_CLOCK_NTSC) {
					sid6526.clock((int /* uint_least16_t */) (cpuFreq
							/ VIC_FREQ_NTSC + 0.5));
				}

				// @FIXME@ see mos6526.h for details. Setup TOD clock
				if (m_tuneInfo.clockSpeed == SIDTUNE_CLOCK_PAL) {
					cia.clock(cpuFreq / VIC_FREQ_PAL);
					cia2.clock(cpuFreq / VIC_FREQ_PAL);
				} else {
					cia.clock(cpuFreq / VIC_FREQ_NTSC);
					cia2.clock(cpuFreq / VIC_FREQ_NTSC);
				}

				// Configure, setup and install C64 environment/events
				if (environment(cfg.environment) < 0) {
					// Try restoring old configuration
					if (m_cfg != cfg)
						config(m_cfg);
					return -1;
				}
				// Start the real time clock event
				rtc.clock(cpuFreq);
			}
		}
		sidSamples(cfg.sidSamples);

		// Setup sid mapping table
		// Note this should be based on m_tuneInfo.sidChipBase1
		// but this is only temporary code anyway
		{
			for (int i = 0; i < SID2_MAPPER_SIZE; i++)
				m_sidmapper[i] = 0;
		}
		if (m_tuneInfo.sidChipBase2 != 0) {
			monosid = false;
			// Assumed to be in d4xx-d7xx range
			m_sidmapper[(m_tuneInfo.sidChipBase2 >> 5) & (SID2_MAPPER_SIZE - 1)] = 1;
		}

		// All parameters check out, so configure player.
		monosid = m_tuneInfo.sidChipBase2 == 0;
		m_info.channels = 1;
		m_emulateStereo = false;
		if (cfg.playback == sid2_stereo) {
			m_info.channels++;
			// Enough sids are available to perform
			// stereo spliting
			if (monosid && (sid[1] != nullsid))
				m_emulateStereo = cfg.emulateStereo;
		}

		// Only force dual sids if second wasn't detected
		if (monosid && cfg.forceDualSids) {
			monosid = false;
			m_sidmapper[(0xd500 >> 5) & (SID2_MAPPER_SIZE - 1)] = 1; // Assumed
		}

		m_leftVolume = cfg.leftVolume;
		m_rightVolume = cfg.rightVolume;

		if (cfg.playback != sid2_mono) {
			// Try Splitting channels across 2 sids
			if (m_emulateStereo) {
				// Mute Voices
				sid[0].voice((short) 0, (short) 0, true);
				sid[0].voice((short) 2, (short) 0, true);
				sid[1].voice((short) 1, (short) 0, true);
				// 2 Voices scaled to unity from 4 (was !SID_VOL)
				// m_leftVolume *= 2;
				// m_rightVolume *= 2;
				// 2 Voices scaled to unity from 3 (was SID_VOL)
				// m_leftVolume *= 3;
				// m_leftVolume /= 2;
				// m_rightVolume *= 3;
				// m_rightVolume /= 2;
				monosid = false;
			}

			if (cfg.playback == sid2_left)
				xsid.mute(true);
		}

		// Setup the audio side, depending on the audio hardware
		// and the information returned by sidtune
		switch (cfg.precision) {
		case 8:
			if (monosid) {
				if (cfg.playback == sid2_stereo)
					output = new IOutput() {
						public long output(short[] buffer, int off) {
							return stereoOut8MonoIn(buffer, off);
						}
					};
				else
					output = new IOutput() {
						public long output(short[] buffer, int off) {
							return monoOut8MonoIn(buffer, off);
						}
					};
			} else {
				switch (cfg.playback) {
				case sid2_stereo: // Stereo Hardware
					output = new IOutput() {
						public long output(short[] buffer, int off) {
							return stereoOut8StereoIn(buffer, off);
						}
					};
					break;

				case sid2_right: // Mono Hardware,
					output = new IOutput() {
						public long output(short[] buffer, int off) {
							return monoOut8StereoRIn(buffer, off);
						}
					};
					break;

				case sid2_left:
					output = new IOutput() {
						public long output(short[] buffer, int off) {
							return monoOut8MonoIn(buffer, off);
						}
					};
					break;

				case sid2_mono:
					output = new IOutput() {
						public long output(short[] buffer, int off) {
							return monoOut8StereoIn(buffer, off);
						}
					};
					break;
				}
			}
			break;

		case 16:
			if (monosid) {
				if (cfg.playback == sid2_stereo)
					output = new IOutput() {
						public long output(short[] buffer, int off) {
							return stereoOut16MonoIn(buffer, off);
						}
					};
				else
					output = new IOutput() {
						public long output(short[] buffer, int off) {
							return monoOut16MonoIn(buffer, off);
						}
					};
			} else {
				switch (cfg.playback) {
				case sid2_stereo: // Stereo Hardware
					output = new IOutput() {
						public long output(short[] buffer, int off) {
							return stereoOut16StereoIn(buffer, off);
						}
					};
					break;

				case sid2_right: // Mono Hardware,
					output = new IOutput() {
						public long output(short[] buffer, int off) {
							return monoOut16StereoRIn(buffer, off);
						}
					};
					break;

				case sid2_left:
					output = new IOutput() {
						public long output(short[] buffer, int off) {
							return monoOut16MonoIn(buffer, off);
						}
					};
					break;

				case sid2_mono:
					output = new IOutput() {
						public long output(short[] buffer, int off) {
							return monoOut16StereoIn(buffer, off);
						}
					};
					break;
				}
			}
		}

		// Update Configuration
		m_cfg = cfg;

		if (m_cfg.optimisation > SID2_MAX_OPTIMISATION)
			m_cfg.optimisation = SID2_MAX_OPTIMISATION;
		return 0;

	}

	public int fastForward(int percent) {
		if (percent > 3200) {
			m_errorString = "SIDPLAYER ERROR: Percentage value out of range";
			return -1;
		}
		{
			double /* float64_t */fastForwardFactor;
			fastForwardFactor = (double /* float64_t */) percent / 100.0;
			// Conversion to fixed point 8.24
			m_samplePeriod = (long /* event_clock_t */) ((double /* float64_t */) m_samplePeriod
					/ m_fastForwardFactor * fastForwardFactor);
			m_fastForwardFactor = fastForwardFactor;
		}
		return 0;
	}

	public int load(SidTune tune) {
		m_tune = tune;
		if (tune == null || !tune.bool()) {
			// Unload tune
			m_info.tuneInfo = null;
			return 0;
		}

		// Un-mute all voices
		xsid.mute(false);

		for (int i = 0; i < SID2_MAX_SIDS; i++) {
			short /* uint_least8_t */v = 3;
			while ((v--) != 0)
				sid[i].voice(v, (short) 0, false);
		}

		{ // Must re-configure on fly for stereo support!
			int ret = config(m_cfg);
			// Failed configuration with new tune, reject it
			if (ret < 0) {
				m_tune = null;
				return -1;
			}
		}
		m_info.tuneInfo = m_tuneInfo;
		return 0;
	}

	public long /* uint_least32_t */mileage() {
		return m_mileage + time();
	}

	public void pause() {
		if (m_running) {
			m_playerState = sid2_paused;
			m_running = false;
		}
	}

	public long /* uint_least32_t */play(short[] buffer,
			int /* uint_least32_t */length) {
		// Make sure a _tune is loaded
		if (!m_tune.bool())
			return 0;

		// Setup Sample Information
		m_sampleIndex = 0;
		m_sampleCount = length;
		m_sampleBuffer = buffer;

		// Start the player loop
		m_playerState = sid2_playing;
		m_running = true;

		while (m_running)
			m_scheduler.clock();

		if (m_playerState == sid2_stopped)
			initialise();
		return m_sampleIndex;
	}

	public sid2_player_t state() {
		return m_playerState;
	}

	public void stop() {
		// Re-start song
		if (m_tune != null && m_tune.bool() && (m_playerState != sid2_stopped)) {
			if (!m_running)
				initialise();
			else {
				m_playerState = sid2_stopped;
				m_running = false;
			}
		}
	}

	public long /* uint_least32_t */time() {
		return rtc.getTime();
	}

	public void debug(boolean enable) {
		cpu.debug(enable);
	}

	public final String error() {
		return m_errorString;
	}

	static final int PSIDDRV_MAX_PAGE = 0xff;

	public final static long /* int_least32_t */VOLUME_MAX = 255;

	public final static int SID2_MAX_SIDS = 2;

	public final static int SID2_TIME_BASE = 10;

	public final static int SID2_MAPPER_SIZE = 32;

	public static final int BUF = (9 * 2 + 8); /* 16 bit header */

	private static class file65 {
		//private String fname;
		//private int fsize;
		private short /* unsigned char */[] buf;
		private int tbase, tlen, dbase, dlen, bbase/*, blen*/, zbase/*, zlen*/;
		private int tdiff, ddiff, bdiff, zdiff;
		private short /* unsigned char */[] segt;
		private short /* unsigned char */[] segd;
		private short /* unsigned char */[] utab;
		private short /* unsigned char */[] rttab;
		private short /* unsigned char */[] rdtab;
		private short /* unsigned char */[] extab;
	}

	private int read_options(short /* unsigned char */[] buf, int pos) {
		int c, l = 0;

		c = buf[pos + 0];
		while ((c != 0) && c != EOF) {
			c &= 255;
			l += c;
			c = buf[pos + l];
		}
		return ++l;
	}

	private int read_undef(short /* unsigned char */[] buf, int pos) {
		int n, l = 2;

		n = buf[pos + 0] + 256 * buf[pos + 1];
		while (n != 0) {
			n--;
			while (buf[pos + (l++)] == 0) {/*noop*/}
		}
		return l;
	}

	private int /* unsigned char */reloc_seg(short /* unsigned char */[] buf,
			int bufPos, int len, short /* unsigned char */[] rtab,
			int rtabPos, file65 fp) {
		int adr = -1;
		int type, seg, old, newv;
		/*
		 * printf("tdiff=%04x, ddiff=%04x, bdiff=%04x, zdiff=%04x\n", fp->tdiff,
		 * fp->ddiff, fp->bdiff, fp->zdiff);
		 */
		while (rtab[rtabPos] != 0) {
			if ((rtab[rtabPos] & 255) == 255) {
				adr += 254;
				rtabPos++;
			} else {
				adr += rtab[rtabPos] & 255;
				rtabPos++;
				type = rtab[rtabPos] & 0xe0;
				seg = rtab[rtabPos] & 0x07;
				/*
				 * printf("reloc entry @ rtab=%p (offset=%d), adr=%04x,
				 * type=%02x, seg=%d\n",rtab-1, *(rtab-1), adr, type, seg);
				 */
				rtabPos++;
				switch (type) {
				case 0x80:
					old = buf[bufPos + adr] + 256 * buf[bufPos + adr + 1];
					newv = old + reldiff(seg, fp);
					buf[bufPos + adr] = (short) (newv & 255);
					buf[bufPos + adr + 1] = (short) ((newv >> 8) & 255);
					break;
				case 0x40:
					old = buf[bufPos + adr] * 256 + rtab[rtabPos];
					newv = old + reldiff(seg, fp);
					buf[bufPos + adr] = (short) ((newv >> 8) & 255);
					rtab[rtabPos] = (short) (newv & 255);
					rtabPos++;
					break;
				case 0x20:
					old = buf[bufPos + adr];
					newv = old + reldiff(seg, fp);
					buf[bufPos + adr] = (short) (newv & 255);
					break;
				}
				if (seg == 0)
					rtabPos += 2;
			}
		}
		if (adr > len) {
			/*
			 * fprintf(stderr,"reloc65: %s: Warning: relocation table entries
			 * past segment end!\n", fp->fname);
			 */
		}
		return ++rtabPos;
	}

	private int /* unsigned char */reloc_globals(
			short /* unsigned char */[] buf, int bufPos, file65 fp) {
		int n, old, newv, seg;

		n = buf[bufPos + 0] + 256 * buf[bufPos + 1];
		bufPos += 2;

		while (n != 0) {
			/* printf("relocating %s, ", buf); */
			while ((buf[bufPos++]) != 0) {/*NOOP*/}
			seg = buf[bufPos];
			old = buf[bufPos + 1] + 256 * buf[bufPos + 2];
			newv = old + reldiff(seg, fp);
			/*
			 * printf("old=%04x, seg=%d, rel=%04x, new=%04x\n", old, seg,
			 * reldiff(seg), new);
			 */
			buf[bufPos + 1] = (short) (newv & 255);
			buf[bufPos + 2] = (short) ((newv >> 8) & 255);
			bufPos += 3;
			n--;
		}
		return bufPos;
	}

	private file65 file = new file65();

	private char /* unsigned char */cmp[] = {
			1, 0, 'o', '6', '5' };

	private static class BufPos {
		public BufPos(short[] buf, int pos, int size) {
			this.fBuf = buf;
			this.fPos = pos;
			this.fSize = size;
		}

		short[] fBuf;

		int fPos;

		int fSize;
	}

	private BufPos reloc65(short /* unsigned char** */[] buf,
			int /* * */fsize, int addr) {
		int mode, hlen;

		boolean tflag = false, dflag = false, bflag = false, zflag = false;
		int tbase = 0, dbase = 0, bbase = 0, zbase = 0;
		int extract = 0;

		file.buf = buf;
		//file.fsize = fsize;
		tflag = true;
		tbase = addr;
		extract = 1;

		for (int i = 0; i < 5; i++) {
			if (file.buf[i] != cmp[i]) {
				return null;
			}
		}

		mode = file.buf[7] * 256 + file.buf[6];
		if ((mode & 0x2000) != 0) {
			return null;
		} else if ((mode & 0x4000) != 0) {
			return null;
		}

		hlen = BUF + read_options(file.buf, BUF);

		file.tbase = file.buf[9] * 256 + file.buf[8];
		file.tlen = file.buf[11] * 256 + file.buf[10];
		file.tdiff = tflag ? tbase - file.tbase : 0;
		file.dbase = file.buf[13] * 256 + file.buf[12];
		file.dlen = file.buf[15] * 256 + file.buf[14];
		file.ddiff = dflag ? dbase - file.dbase : 0;
		file.bbase = file.buf[17] * 256 + file.buf[16];
		//file.blen = file.buf[19] * 256 + file.buf[18];
		file.bdiff = bflag ? bbase - file.bbase : 0;
		file.zbase = file.buf[21] * 256 + file.buf[20];
		//file.zlen = file.buf[23] * 256 + file.buf[21];
		file.zdiff = zflag ? zbase - file.zbase : 0;

		file.segt = file.buf;
		int segtPos = hlen;
		file.segd = file.segt;
		int sehdPos = segtPos + file.tlen;
		file.utab = file.segd;
		int utabPos = sehdPos + file.dlen;

		file.rttab = file.utab;
		int rttabPos = utabPos + read_undef(file.utab, utabPos);

		file.rdtab = file.rttab;
		file.extab = file.rdtab;
		int rdtabPos = reloc_seg(file.segt, segtPos, file.tlen, file.rttab, rttabPos, file);
		int extabPos = reloc_seg(file.segd, sehdPos, file.dlen, file.rdtab, rdtabPos, file);

		/*extabPos = */reloc_globals(file.extab, extabPos, file);

		if (tflag) {
			file.buf[9] = (short) ((tbase >> 8) & 255);
			file.buf[8] = (short) (tbase & 255);
		}
		if (dflag) {
			file.buf[13] = (short) ((dbase >> 8) & 255);
			file.buf[12] = (short) (dbase & 255);
		}
		if (bflag) {
			file.buf[17] = (short) ((bbase >> 8) & 255);
			file.buf[16] = (short) (bbase & 255);
		}
		if (zflag) {
			file.buf[21] = (short) ((zbase >> 8) & 255);
			file.buf[20] = (short) (zbase & 255);
		}

		switch (extract) {
		case 0: /* whole file */
			return new BufPos(buf, 0, fsize);
		case 1: /* text segment */
			return new BufPos(file.segt, segtPos, file.tlen);
		case 2:
			return new BufPos(file.segd, sehdPos, file.dlen);
		default:
			return null;
		}
	}

	final static int EOF = (-1);

	private int reldiff(int s, file65 fp) {
		return (((s) == 2) ? fp.tdiff : (((s) == 3) ? fp.ddiff
				: (((s) == 4) ? fp.bdiff : (((s) == 5) ? fp.zdiff : 0))));
	}

}
