/**
 *                         Support for Playsids Extended
 *                         -----------------------------
 *  begin                : Tue Jun 20 2000
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
package de.quippy.sidplay.libsidplay.components.xsid;

import static de.quippy.sidplay.libsidplay.Config.S_A_WHITE_EMAIL;
import static de.quippy.sidplay.libsidplay.common.SIDEndian.endian_16;

import java.util.logging.Level;
import java.util.logging.Logger;

import de.quippy.sidplay.libsidplay.common.Event;
import de.quippy.sidplay.libsidplay.common.IEventContext;
import de.quippy.sidplay.libsidplay.common.SIDEmu;
import de.quippy.sidplay.libsidplay.common.Event.event_phase_t;


/**
 * Effectively there is only 1 channel, which can either perform Galway Noise or
 * Sampling. However, to achieve all the effects on a C64, 2 sampling channels
 * are required. No divide by 2 is required and is compensated for automatically
 * in the C64 machine code.
 * <P>
 * Confirmed by Warren Pilkington using the tune Turbo Outrun: A new sample must
 * interrupt an existing sample running on the same channel.
 * <P>
 * Confirmed by Michael Schwendt and Antonia Vera using the tune Game Over: A
 * Galway Sample or Noise sequence cannot interrupt any other. However the last
 * of these new requested sequences will be played after the current sequence
 * ends.
 * <P>
 * Lastly playing samples through the SIDs volume is not as clean as playing
 * them on their own channel. Playing through the SID will effect the volume of
 * the other channels and this will be most noticable at low frequencies. These
 * effects are however present in the original SID music.
 * <P>
 * Some SIDs put values directly into the volume register. Others play samples
 * with respect to the current volume. We can't for definate know which the
 * author has chosen originally. We must just make a guess based on what the
 * volume is initially at the start of a sample sequence and from the details
 * xSID has been programmed with.
 * 
 * @author Ken H�ndel
 * 
 */
public abstract class XSID /* extends Event */extends SIDEmu {

	/**
	 * Debug support
	 */
	private static final Logger XSID = Logger.getLogger(XSID.class.getName());

	public static class Channel {

		//
		// general
		//

		private final String m_name;

		private IEventContext m_context;

		private event_phase_t m_phase;

		private XSID m_xsid;

		private static class SampleEvent extends Event {

			private Channel m_ch;

			public void event() {
				m_ch.sampleClock();
			}

			public SampleEvent(Channel ch) {
				super("xSID Sample");
				m_ch = ch;
			}

		}

		private SampleEvent sampleEvent;

		private static class GalwayEvent extends Event {

			private Channel m_ch;

			public void event() {
				m_ch.galwayClock();
			}

			public GalwayEvent(Channel ch) {
				super("xSID Galway");
				m_ch = ch;
			}
		}

		private GalwayEvent galwayEvent;

		private short /* uint8_t */reg[] = new short[0x10];

		private static final int FM_NONE = 0;

		private static final int FM_HUELS = 1;

		private static final int FM_GALWAY = 2;

		private int mode;

		private boolean active;

		private int /* uint_least16_t */address;

		/**
		 * Counts to zero and triggers!
		 */
		private int /* uint_least16_t */cycleCount;

		private short /* uint_least8_t */volShift;

		private short /* uint_least8_t */sampleLimit;

		private byte /* int8_t */sample;

		//
		// Sample Section
		//

		private short /* uint_least8_t */samRepeat;

		private short /* uint_least8_t */samScale;

		public static final int SO_LOWHIGH = 0;

		public static final int SO_HIGHLOW = 1;

		private short /* uint_least8_t */samOrder;

		private short /* uint_least8_t */samNibble;

		private int /* uint_least16_t */samEndAddr;

		private int /* uint_least16_t */samRepeatAddr;

		private int /* uint_least16_t */samPeriod;

		//
		// Galway Section
		//

		private short /* uint_least8_t */galTones;

		private short /* uint_least8_t */galInitLength;

		private short /* uint_least8_t */galLength;

		private short /* uint_least8_t */galVolume;

		private short /* uint_least8_t */galLoopWait;

		private short /* uint_least8_t */galNullWait;

		//
		// For Debugging
		//

		private long /* event_clock_t */cycles;

		private long /* event_clock_t */outputs;

		private Channel(final String name, IEventContext context, XSID xsid) {
			m_name = name;
			m_context = context;
			m_phase = event_phase_t.EVENT_CLOCK_PHI1;
			m_xsid = xsid;
			sampleEvent = new SampleEvent(this);
			galwayEvent = new GalwayEvent(this);
			for (int i = 0; i < reg.length; i++) {
				reg[i] = 0;
			}
			active = true;
			reset();
		}

		private void free() {
			active = false;
			cycleCount = 0;
			sampleLimit = 0;
			// Set XSID to stopped state
			reg[convertAddr(0x1d)] = 0;
			silence();
		}

		private void silence() {
			sample = 0;
			m_context.cancel(sampleEvent);
			m_context.cancel(galwayEvent);
			m_context.schedule(m_xsid.event, 0, m_phase);
		}

		private void sampleInit() {
			if (active && (mode == FM_GALWAY))
				return;

			if (XSID.isLoggable(Level.INFO)) {
				XSID.log(Level.FINE, String.format("XSID [%s]: Sample Init", m_name));
				if (active && (mode == FM_HUELS)) {
					XSID.log(Level.FINE, String.format(
							"XSID [%s]: Stopping Playing Sample", m_name));
				}
			}

			// Check all important parameters are legal
			short r = convertAddr(0x1d);
			volShift = (short /* uint_least8_t */) ((0 - reg[r]) >> 1);
			reg[r] = 0;
			// Use endian_16 as can't g
			r = convertAddr(0x1e);
			address = endian_16(reg[r + 1], reg[r]);
			r = convertAddr(0x3d);
			samEndAddr = endian_16(reg[r + 1], reg[r]);
			if (samEndAddr <= address)
				return;
			samScale = reg[convertAddr(0x5f)];
			r = convertAddr(0x5d);
			samPeriod = endian_16(reg[r + 1], reg[r]) >> samScale;
			if (samPeriod == 0) {
				// Stop this channel
				reg[convertAddr(0x1d)] = 0xfd;
				checkForInit();
				return;
			}

			// Load the other parameters
			samNibble = 0;
			samRepeat = reg[convertAddr(0x3f)];
			samOrder = reg[convertAddr(0x7d)];
			r = convertAddr(0x7e);
			samRepeatAddr = endian_16(reg[r + 1], reg[r]);
			cycleCount = samPeriod;

			// Support Galway Samples, but that
			// mode it setup only when as Galway
			// Noise sequence begins
			if (mode == FM_NONE)
				mode = FM_HUELS;

			active = true;
			cycles = 0;
			outputs = 0;

			sampleLimit = (short) (8 >> volShift);
			sample = sampleCalculate();

			// Calculate the sample offset
			m_xsid.sampleOffsetCalc();

			if (XSID.isLoggable(Level.INFO)) {
				if (XSID.isLoggable(Level.FINE)) {
					XSID.fine(String.format("XSID [%s]: Sample Start Address:  0x%04x", m_name, Integer.valueOf(address)));
					XSID.fine(String.format("XSID [%s]: Sample End Address:    0x%04x", m_name, Integer.valueOf(samEndAddr)));
					XSID.fine(String.format("XSID [%s]: Sample Repeat Address: 0x%04x", m_name, Integer.valueOf(samRepeatAddr)));
					XSID.fine(String.format("XSID [%s]: Sample Period: %d", m_name, Integer.valueOf(samPeriod)));
					XSID.fine(String.format("XSID [%s]: Sample Repeat: %d", m_name, Integer.valueOf(samRepeat)));
					XSID.fine(String.format("XSID [%s]: Sample Order : %d", m_name, Integer.valueOf(samOrder)));
				}
				XSID.log(Level.FINE, String.format("XSID [%s]: Sample Start", m_name));
			}

			// Schedule a sample update
			m_context.schedule(m_xsid.event, 0, m_phase);
			m_context.schedule(sampleEvent, cycleCount, m_phase);
		}

		private void sampleClock() {
			cycleCount = samPeriod;
			if (address >= samEndAddr) {
				if (samRepeat != 0xFF) {
					if (samRepeat != 0)
						samRepeat--;
					else
						samRepeatAddr = address;
				}

				address = samRepeatAddr;
				if (address >= samEndAddr) {
					// The sequence has completed
					short r = convertAddr(0x1d);
					short /* uint8_t & */status = reg[r];
					if (status == 0)
						reg[r] = 0xfd;
					if (status != 0xfd)
						active = false;

					if (XSID.isLoggable(Level.INFO)) {
						XSID
								.log(Level.FINE, String
										.format(
												"XSID [%s]: Sample Stop (%d Cycles, %d Outputs)",
												m_name, Long.valueOf(cycles), Long.valueOf(outputs)));
					}

					checkForInit();
					return;
				}
			}

			// We have reached the required sample
			// So now we need to extract the right nibble
			sample = sampleCalculate();
			cycles += cycleCount;
			// Schedule a sample update
			m_context.schedule(sampleEvent, cycleCount, m_phase);
			m_context.schedule(m_xsid.event, 0, m_phase);
		}

		private void galwayInit() {
			if (active)
				return;

			if (XSID.isLoggable(Level.INFO)) {
				XSID.log(Level.FINE, String.format("XSID [%s]: Galway Init", m_name));
			}

			// Check all important parameters are legal
			short r = convertAddr(0x1d);
			galTones = reg[r];
			reg[r] = 0;
			galInitLength = reg[convertAddr(0x3d)];
			if (galInitLength == 0)
				return;
			galLoopWait = reg[convertAddr(0x3f)];
			if (galLoopWait == 0)
				return;
			galNullWait = reg[convertAddr(0x5d)];
			if (galNullWait == 0)
				return;

			// Load the other parameters
			r = convertAddr(0x1e);
			address = endian_16(reg[r + 1], reg[r]);
			volShift = (short) (reg[convertAddr(0x3e)] & 0x0f);
			mode = FM_GALWAY;
			active = true;
			cycles = 0;
			outputs = 0;

			sampleLimit = 8;
			sample = (byte /* int8_t */) (galVolume - 8);
			galwayTonePeriod();

			// Calculate the sample offset
			m_xsid.sampleOffsetCalc();

			if (XSID.isLoggable(Level.INFO)) {
				XSID.log(Level.FINE, String.format("XSID [%s]: Galway Start", m_name));
			}

			// Schedule a sample update
			m_context.schedule(m_xsid.event, 0, m_phase);
			m_context.schedule(galwayEvent, cycleCount, m_phase);
		}

		private void galwayClock() {
			if (--galLength != 0)
				cycleCount = samPeriod;
			else if (galTones == 0xff) {
				// The sequence has completed
				int r = convertAddr(0x1d);
				short /* uint8_t & */status = reg[r];
				if (status == 0)
					reg[r] = 0xfd;
				if (status != 0xfd)
					active = false;

				if (XSID.isLoggable(Level.INFO)) {
					XSID.log(Level.FINE, String.format(
							"XSID [%s]: Galway Stop (%d Cycles, %d Outputs)",
							m_name, Long.valueOf(cycles), Long.valueOf(outputs)));
					if (status != 0xfd) {
						XSID
								.log(Level.FINE, String.format(
										"XSID [%s]: Starting Delayed Sequence",
										m_name));
					}
				}

				checkForInit();
				return;
			} else
				galwayTonePeriod();

			// See Galway Example...
			galVolume += volShift;
			galVolume &= 0x0f;
			sample = (byte /* int8_t */) (galVolume - 8);
			cycles += cycleCount;
			m_context.schedule(galwayEvent, cycleCount, m_phase);
			m_context.schedule(m_xsid.event, 0, m_phase);
		}

		/**
		 * Compress address to not leave so many spaces
		 * 
		 * @param addr
		 * @return
		 */
		private short /* uint_least8_t */convertAddr(
				int /* uint_least8_t */addr) {
			return (short) (((addr) & 0x3) | ((addr) >> 3) & 0x0c);
		}

		private void reset() {
			galVolume = 0; // This is left to free run until reset
			mode = FM_NONE;
			free();
			// Remove outstanding events
			m_context.cancel(m_xsid.event);
			m_context.cancel(sampleEvent);
			m_context.cancel(galwayEvent);
		}

		/**
		 * Unused method. Modifier set from private to public!
		 * 
		 * @param addr
		 * @return
		 */
		public short /* uint8_t */read(short /* uint_least8_t */addr) {
			return reg[convertAddr(addr)];
		}

		private void write(short /* uint_least8_t */addr,
				short /* uint8_t */data) {
			reg[convertAddr(addr)] = data;
		}

		private byte /* int8_t */output() {
			outputs++;
			return sample;
		}

		private boolean isGalway() {
			return mode == FM_GALWAY;
		}

		private short /* uint_least8_t */limit() {
			return sampleLimit;
		}

		private void checkForInit() {
			// Check to see mode of operation
			// See xsid documentation
			switch (reg[convertAddr(0x1d)]) {
			case 0xFF:
			case 0xFE:
			case 0xFC:
				sampleInit();
				break;
			case 0xFD:
				if (!active)
					return;
				free(); // Stop
				// Calculate the sample offset
				m_xsid.sampleOffsetCalc();
				break;
			case 0x00:
				break;
			default:
				galwayInit();
			}
		}

		private byte /* int8_t */sampleCalculate() {
			short /* uint_least8_t */tempSample = m_xsid.readMemByte(address);
			if (samOrder == SO_LOWHIGH) {
				if (samScale == 0) {
					if (samNibble != 0)
						tempSample >>= 4;
				}
				// AND 15 further below.
			} else // if (samOrder == SO_HIGHLOW)
			{
				if (samScale == 0) {
					if (samNibble == 0)
						tempSample >>= 4;
				} else
					// if (samScale != 0)
					tempSample >>= 4;
				// AND 15 further below.
			}

			// Move to next address
			address += samNibble;
			samNibble ^= 1;
			return (byte /* int8_t */) (((tempSample & 0x0f) - 0x08) >> volShift);
		}

		private void galwayTonePeriod() {
			// Calculate the number of cycles over which sample should last
			galLength = galInitLength;
			samPeriod = m_xsid.readMemByte(address + galTones);
			samPeriod *= galLoopWait;
			samPeriod += galNullWait;
			cycleCount = samPeriod;

			if (XSID.isLoggable(Level.INFO)) {
				XSID.log(Level.FINE, String.format("XSID [%s]: Galway Settings", m_name));
				XSID.log(Level.FINE, String.format("XSID [%s]: Length %d, LoopWait %d, NullWait %d", m_name, Short.valueOf(galLength), Short.valueOf(galLoopWait), Short.valueOf(galNullWait)));
				XSID.log(Level.FINE, String.format("XSID [%s]: Tones %d, Data %d", m_name, Short.valueOf(galTones), Short.valueOf(m_xsid.readMemByte(address + galTones))));
			}

			galTones--;
		}

		/**
		 * Used to indicate if channel is running
		 * 
		 * @return
		 */
		private final boolean bool() {
			return (active);
		}

	}

	private Channel ch4;

	private Channel ch5;

	private boolean muted;

	private boolean suppressed;

	private static final String credit = "xSID (Extended SID) Engine:"
			+ "\tCopyright (C) 2000 Simon White <" + S_A_WHITE_EMAIL + ">";

	private short sidData0x18;

	private boolean _sidSamples;

	private short sampleOffset;

	// Convert from 4 bit resolution to 8 bits
	/*
	 * Rev 2.0.5 (saw) - Removed for a more non-linear equivalent which better
	 * models the SIDS master volume register const int8_t
	 * XSID::sampleConvertTable[16] = { '\x80', '\x91', '\xa2', '\xb3', '\xc4',
	 * '\xd5', '\xe6', '\xf7', '\x08', '\x19', '\x2a', '\x3b', '\x4c', '\x5d',
	 * '\x6e', '\x7f' };
	 */
	private static final short sampleConvertTable[] = { 0x80, 0x94, 0xa9, 0xbc,
			0xce, 0xe1, 0xf2, 0x03, 0x1b, 0x2a, 0x3b, 0x49, 0x58, 0x66, 0x73,
			0x7f };

	private boolean wasRunning;

	/**
	 * Resolve multiple inheritance. XSID event.
	 */
	private Event event = new Event("xSID") {
		/**
		 * Resolve multiple inheritance.
		 */
		public void event() {
			if (ch4.bool() || ch5.bool()) {
				setSidData0x18();
				wasRunning = true;
			} else if (wasRunning) {
				recallSidData0x18();
				wasRunning = false;
			}
		}
	};

	private void setSidData0x18() {
		if (!_sidSamples || muted)
			return;

		short /* uint8_t */data = (short) (sidData0x18 & 0xf0);
		data |= ((sampleOffset + sampleOutput()) & 0x0f);

		if (XSID.isLoggable(Level.INFO)) {
			if ((sampleOffset + sampleOutput()) > 0x0f) {
				XSID.log(Level.FINE, String.format("XSID: Sample Wrapped [offset %d, sample %d]", Short.valueOf(sampleOffset), Byte.valueOf(sampleOutput())));
			}
			if (XSID.isLoggable(Level.FINE)) {
				XSID.fine(String.format("XSID: Writing Sample to SID Volume [0x %02x]", Short.valueOf(data)));
			}
		}

		writeMemByte(data);
	}

	private void recallSidData0x18() {
		// Rev 2.0.5 (saw) - Changed to recall
		// volume
		// differently depending on mode
		// Normally after samples volume should be restored to half volume,
		// however, Galway Tunes sound horrible and seem to require setting back
		// to the original volume. Setting back to the original volume for
		// normal samples can have nasty pulsing effects
		if (ch4.isGalway()) {
			if (_sidSamples && !muted)
				writeMemByte(sidData0x18);
		} else
			setSidData0x18();
	}

	private byte /* int8_t */sampleOutput() {
		byte /* int8_t */sample;
		sample = ch4.output();
		sample += ch5.output();
		// Automatically compensated for by C64 code
		// return (sample >> 1);
		return sample;
	}

	private void sampleOffsetCalc() {
		// Try to determine a sensible offset between voice
		// and sample volumes.
		short /* uint_least8_t */lower = (short) (ch4.limit() + ch5.limit());
		short /* uint_least8_t */upper;

		// Both channels seem to be off. Keep current offset!
		if (lower == 0)
			return;

		sampleOffset = (short) (sidData0x18 & 0x0f);

		// Is possible to compensate for both channels
		// set to 4 bits here, but should never happen.
		if (lower > 8)
			lower >>= 1;
		upper = (short) (0x0f - lower + 1);

		// Check against limits
		if (sampleOffset < lower)
			sampleOffset = lower;
		else if (sampleOffset > upper)
			sampleOffset = upper;

		if (XSID.isLoggable(Level.INFO)) {
			XSID.log(Level.FINE, String.format("XSID: Sample Offset %d based on channel(s) ", Short.valueOf(sampleOffset)));
			if (ch4.bool()) {
				XSID.log(Level.FINE, "4 ");
			}
			if (ch5.bool()) {
				XSID.log(Level.FINE, "5 ");
			}
		}

	}

	protected abstract short /* uint8_t */readMemByte(
			int /* uint_least16_t */addr);

	protected abstract void writeMemByte(short /* uint8_t */data);

	public XSID(IEventContext context) {
		super(null);
		ch4 = new Channel("CH4", context, this);
		ch5 = new Channel("CH5", context, this);
		muted = (false);
		suppressed = (false);
		wasRunning = (false);
		sidSamples(true);
	}

	//
	// Standard Calls
	//

	public void reset() {
		super.reset();
	}

	public void reset(short /* uint8_t */volume) {
		ch4.reset();
		ch5.reset();
		suppressed = false;
		wasRunning = false;
	}

	public short /* uint8_t */read(short /* uint_least8_t */addr) {
		return 0;
	}

	public void write(short /* uint_least8_t */addr, short /* uint8_t */data) {
		
	}

	public final String credits() {
		return credit;
	}

	//
	// Specialist Calls
	//

	public short /* uint8_t */read(int /* uint_least16_t */addr) {
		return 0;
	}

	public void write(int /* uint_least16_t */addr, short /* uint8_t */data) {
		Channel ch;
		short /* uint8_t */tempAddr;

		// Make sure address is legal
		if (((addr & 0xfe8c) ^ 0x000c) != 0)
			return;

		ch = ch4;
		if ((addr & 0x0100) != 0)
			ch = ch5;

		tempAddr = (short /* uint8_t */) addr;
		ch.write(tempAddr, data);

		if (XSID.isLoggable(Level.FINE)) {
			XSID.fine(String.format("XSID: Addr 0x%02x, Data 0x%02x", Short.valueOf(tempAddr), Short.valueOf(data)));
		}

		if (tempAddr == 0x1d) {
			if (suppressed) {
				if (XSID.isLoggable(Level.INFO)) {
					XSID.log(Level.FINE, String.format("XSID: Initialise Suppressed"));
				}
				return;
			}
			ch.checkForInit();
		}
	}

	// ----------------------------------------------------------------------------
	// Inline functions.
	// ----------------------------------------------------------------------------

	public long /* int_least32_t */output(short /* uint_least8_t */bits) {
		long /* int_least32_t */sample;
		if (_sidSamples || muted)
			return 0;
		sample = sampleConvertTable[sampleOutput() + 8];
		return sample << (bits - 8);
	}

	// ----------------------------------------------------------------------------
	// END Inline functions.
	// ----------------------------------------------------------------------------

	/**
	 * By muting samples they will start and play the at the appropriate time
	 * but no sound is produced. Un-muting will cause sound output from the
	 * current play position.
	 * 
	 * @param enable
	 */
	public void mute(boolean enable) {
		if (!muted && enable && wasRunning)
			recallSidData0x18();
		muted = enable;
	}

	public boolean isMuted() {
		return muted;
	}

	/**
	 * Use Suppress to delay the samples and start them later. Effectivly allows
	 * running samples in a frame based mode.
	 * 
	 * @param enable
	 */
	public void suppress(boolean enable) {
		// @FIXME@: Mute Temporary Hack
		suppressed = enable;
		if (!suppressed) {
			// Get the channels running

			if (XSID.isLoggable(Level.INFO)) {
				XSID.log(Level.FINE, "XSID: Un-suppressing");
			}

			ch4.checkForInit();
			ch5.checkForInit();
		} else {
			if (XSID.isLoggable(Level.INFO)) {
				XSID.log(Level.FINE, "XSID: Suppressing");
			}
		}
	}

	public void sidSamples(boolean enable) {
		_sidSamples = enable;
	}

	/**
	 * Return whether we care it was changed.
	 * 
	 * @param data
	 * @return
	 */
	public boolean storeSidData0x18(short /* uint8_t */data) {
		sidData0x18 = data;
		if (ch4.bool() || ch5.bool()) {
			// Force volume to be changed at next clock
			sampleOffsetCalc();
			if (_sidSamples) {

				if (XSID.isLoggable(Level.INFO)) {
					XSID
							.log(Level.FINE, "XSID: SID Volume changed externally (Corrected).");
				}

				return true;
			}
		}
		writeMemByte(sidData0x18);
		return false;
	}

}
