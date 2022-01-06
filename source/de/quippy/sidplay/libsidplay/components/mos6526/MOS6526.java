/**
 *                         CIA timer to produce interrupts
 *                         -------------------------------
 *  begin                : Wed Jun 7 2000
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
package de.quippy.sidplay.libsidplay.components.mos6526;

import static de.quippy.sidplay.libsidplay.Config.S_A_WHITE_EMAIL;
import static de.quippy.sidplay.libsidplay.common.SIDEndian.endian_16hi8;
import static de.quippy.sidplay.libsidplay.common.SIDEndian.endian_16lo8;
import de.quippy.sidplay.libsidplay.common.Event;
import de.quippy.sidplay.libsidplay.common.IComponent;
import de.quippy.sidplay.libsidplay.common.IEventContext;
import de.quippy.sidplay.libsidplay.common.Event.event_phase_t;

public abstract class MOS6526 implements IComponent {

	public static final int INTERRUPT_TA = 1 << 0;

	public static final int INTERRUPT_TB = 1 << 1;

	public static final int INTERRUPT_ALARM = 1 << 2;

	public static final int INTERRUPT_SP = 1 << 3;

	public static final int INTERRUPT_FLAG = 1 << 4;

	public static final int INTERRUPT_REQUEST = 1 << 7;

	public static final int PRA = 0;

	public static final int PRB = 1;

	public static final int DDRA = 2;

	public static final int DDRB = 3;

	public static final int TAL = 4;

	public static final int TAH = 5;

	public static final int TBL = 6;

	public static final int TBH = 7;

	public static final int TOD_TEN = 8;

	public static final int TOD_SEC = 9;

	public static final int TOD_MIN = 10;

	public static final int TOD_HR = 11;

	public static final int SDR = 12;

	public static final int ICR = 13;

	public static final int IDR = 13;

	public static final int CRA = 14;

	public static final int CRB = 15;

	/**
	 * Optional information
	 */
	private static final String credit = "*MOS6526 (CIA) Emulation:\tCopyright (C) 2001-2004 Simon White <" + S_A_WHITE_EMAIL + ">";

	protected short /* uint8_t */regs[] = new short[0x10];

	protected boolean cnt_high;

	// Ports
	// protected short /* uint8_t & */pra;
	//
	// protected short /* uint8_t & */prb;
	//
	// protected short /* uint8_t &*/ddra;
	//
	// protected short /* uint8_t &*/ddrb;

	//
	// Timer A
	//

	protected short /* uint8_t */cra, cra_latch, dpa;

	protected int /* uint_least16_t */ta, ta_latch;

	protected boolean ta_underflow;

	//
	// Timer B
	//

	protected short /* uint8_t */crb;

	protected int /* uint_least16_t */tb, tb_latch;

	protected boolean tb_underflow;

	//
	// Serial Data Registers
	//

	protected short /* uint8_t */sdr_out;

	protected boolean sdr_buffered;

	protected int sdr_count;

	protected short /* uint8_t */icr, idr; // Interrupt Control Register

	protected long /* event_clock_t */m_accessClk;

	protected IEventContext event_context;

	protected event_phase_t m_phase;

	protected boolean m_todlatched;

	protected boolean m_todstopped;

	protected short /* uint8_t */m_todclock[] = new short[4],
			m_todalarm[] = new short[4], m_todlatch[] = new short[4];

	long /* event_clock_t */m_todCycles, m_todPeriod;

	protected static class EventTa extends Event {
		private MOS6526 m_cia;

		public void event() {
			m_cia.ta_event();
		}

		public EventTa(MOS6526 cia) {
			super("CIA Timer A");
			m_cia = cia;
		}
	}

	protected EventTa event_ta;

	// protected class EventStateMachineA extends Event {
	// private MOS6526 m_cia;
	//
	// public void event() {
	// m_cia.cra_event();
	// }
	//
	// public EventStateMachineA(MOS6526 cia) {
	// super("CIA Timer A (State Machine)");
	// m_cia = cia;
	// }
	// };
	//
	// protected EventStateMachineA event_stateMachineA;

	protected static class EventTb extends Event {
		private MOS6526 m_cia;

		public void event() {
			m_cia.tb_event();
		}

		public EventTb(MOS6526 cia) {
			super("CIA Timer B");
			m_cia = cia;
		}
	}

	protected EventTb event_tb;

	protected static class EventTod extends Event {
		private MOS6526 m_cia;

		public void event() {
			m_cia.tod_event();
		}

		public EventTod(MOS6526 cia) {
			super("CIA Time of Day");
			m_cia = cia;
		}
	}

	protected EventTod event_tod;

	protected MOS6526(IEventContext context) {
		idr = 0;
		event_context = (context);
		m_phase = event_phase_t.EVENT_CLOCK_PHI1;
		m_todPeriod = ~0 & 0xffffffffl; // Dummy
		event_ta = new EventTa(this);
		event_tb = new EventTb(this);
		event_tod = new EventTod(this);
		reset();
	}

	protected void ta_event() {
		// Timer Modes
		long /* event_clock_t */cycles;
		short /* uint8_t */mode = (short) (cra & 0x21);

		if (mode == 0x21) {
			if ((ta--) != 0)
				return;
		}

		cycles = event_context.getTime(m_accessClk, m_phase);
		m_accessClk += cycles;

		ta = ta_latch;
		ta_underflow ^= true; // toggle flipflop
		if ((cra & 0x08) != 0) {
			// one shot, stop timer A
			cra &= (~0x01 & 0xff);
		} else if (mode == 0x01) {
			// Reset event
			event_context.schedule(event_ta,
					(long /* event_clock_t */) ta + 1, m_phase);
		}
		trigger(INTERRUPT_TA);

		// Handle serial port
		if ((cra & 0x40) != 0) {
			if (sdr_count != 0) {
				if ((--sdr_count) == 0)
					trigger(INTERRUPT_SP);
			}
			if ((sdr_count == 0) && sdr_buffered) {
				sdr_out = regs[SDR];
				sdr_buffered = false;
				sdr_count = 16; // Output rate 8 bits at ta / 2
			}
		}

		switch (crb & 0x61) {
		case 0x01:
			tb -= cycles;
			break;
		case 0x41:
		case 0x61:
			tb_event();
			break;
		}
	}

	protected void tb_event() {
		// Timer Modes
		short /* uint8_t */mode = (short) (crb & 0x61);
		switch (mode) {
		case 0x01:
			break;

		case 0x21:
		case 0x41:
			if ((tb--) != 0)
				return;
			break;

		case 0x61:
			if (cnt_high) {
				if ((tb--) != 0)
					return;
			}
			break;

		default:
			return;
		}

		m_accessClk = event_context.getTime(m_phase);
		tb = tb_latch;
		tb_underflow ^= true; // toggle flipflop
		if ((crb & 0x08) != 0) {
			// one shot, stop timer A
			crb &= (~0x01 & 0xff);
		} else if (mode == 0x01) {
			// Reset event
			event_context.schedule(event_tb,
					(long /* event_clock_t */) tb + 1, m_phase);
		}
		trigger(INTERRUPT_TB);
	}

	//
	// TOD implementation taken from Vice
	//
	
	private final static short byte2bcd(short thebyte) {
		return (short) (((((thebyte) / 10) << 4) + ((thebyte) % 10)) & 0xff);
	}

	private final static short bcd2byte(short bcd) {
		return (short) (((10 * (((bcd) & 0xf0) >> 4)) + ((bcd) & 0xf)) & 0xff);
	}

	protected void tod_event() {
		// Reload divider according to 50/60 Hz flag
		// Only performed on expiry according to Frodo
		if ((cra & 0x80) != 0)
			m_todCycles += (m_todPeriod * 5);
		else
			m_todCycles += (m_todPeriod * 6);

		// Fixed precision 25.7
		event_context.schedule(event_tod, m_todCycles >> 7, m_phase);
		m_todCycles &= 0x7F; // Just keep the decimal part

		if (!m_todstopped) {
			// inc timer
			short /* uint8_t */tod[] = m_todclock;
			int todPos = 0;
			short /* uint8_t */t = (short) (bcd2byte(tod[todPos]) + 1);
			tod[todPos++] = byte2bcd((short) (t % 10));
			if (t >= 10) {
				t = (short) (bcd2byte(tod[todPos]) + 1);
				tod[todPos++] = byte2bcd((short) (t % 60));
				if (t >= 60) {
					t = (short) (bcd2byte(tod[todPos]) + 1);
					tod[todPos++] = byte2bcd((short) (t % 60));
					if (t >= 60) {
						short /* uint8_t */pm = (short) (tod[todPos] & 0x80);
						t = (short) (tod[todPos] & 0x1f);
						if (t == 0x11)
							pm ^= 0x80; // toggle am/pm on 0:59->1:00 hr
						if (t == 0x12)
							t = 1;
						else if (++t == 10)
							t = 0x10; // increment, adjust bcd
						t &= 0x1f;
						tod[todPos] = (short) (t | pm);
					}
				}
			}
			// check alarm
			if (!memcmp(m_todalarm, m_todclock, m_todalarm.length))
				trigger(INTERRUPT_ALARM);
		}
	}

	protected void trigger(int irq) {
		if (irq == 0) {
			// Clear any requested IRQs
			if ((idr & INTERRUPT_REQUEST) != 0)
				interrupt(false);
			idr = 0;
			return;
		}

		idr |= irq;
		if ((icr & idr) != 0) {
			if ((idr & INTERRUPT_REQUEST) == 0) {
				idr |= INTERRUPT_REQUEST;
				interrupt(true);
			}
		}
	}

	// protected void stateMachineA_event();

	//
	// Environment Interface
	//

	public abstract void interrupt(boolean state);

	public abstract void portA();

	public abstract void portB();

	//
	// Component Standard Calls
	//

	public void reset() {
		ta = ta_latch = 0xffff;
		tb = tb_latch = 0xffff;
		ta_underflow = tb_underflow = false;
		cra = crb = sdr_out = 0;
		sdr_count = 0;
		sdr_buffered = false;
		// Clear off any IRQs
		trigger(0);
		cnt_high = true;
		icr = idr = 0;
		m_accessClk = 0;
		dpa = 0xf0;
		for (int i = 0; i < regs.length; i++) {
			regs[i] = 0;
		}

		// Reset tod
		for (int i = 0; i < m_todclock.length; i++) {
			m_todclock[i] = 0;
		}
		for (int i = 0; i < m_todalarm.length; i++) {
			m_todalarm[i] = 0;
		}
		for (int i = 0; i < m_todlatch.length; i++) {
			m_todlatch[i] = 0;
		}

		m_todlatched = false;
		m_todstopped = true;
		m_todclock[TOD_HR - TOD_TEN] = 1; // the most common value
		m_todCycles = 0;

		// Remove outstanding events
		event_context.cancel(event_ta);
		event_context.cancel(event_tb);
		event_context.schedule(event_tod, 0, m_phase);
	}

	public short /* uint8_t */read(short /* uint_least8_t */addr) {
		long /* event_clock_t */cycles;
		if (addr > 0x0f)
			return 0;
		boolean ta_pulse = false, tb_pulse = false;

		cycles = event_context.getTime(m_accessClk, event_context.phase());
		m_accessClk += cycles;

		// Sync up timers
		if ((cra & 0x21) == 0x01) {
			ta -= cycles;
			if (ta == 0) {
				ta_event();
				ta_pulse = true;
			}
		}
		if ((crb & 0x61) == 0x01) {
			tb -= cycles;
			if (tb == 0) {
				tb_event();
				tb_pulse = true;
			}
		}

		switch (addr) {
		case PRA: // Simulate a serial port
			return (short) (regs[PRA] /* pra */| (~regs[DDRA] /* ddra */& 0xff));
		case PRB: {
			short /* uint8_t */data = (short) (regs[PRB] /* prb */| (~regs[DDRB] /* ddrb */& 0xff));
			// Timers can appear on the port
			if ((cra & 0x02) != 0) {
				data &= 0xbf;
				if ((cra & 0x04) != 0 ? ta_underflow : ta_pulse)
					data |= 0x40;
			}
			if ((crb & 0x02) != 0) {
				data &= 0x7f;
				if ((crb & 0x04) != 0 ? tb_underflow : tb_pulse)
					data |= 0x80;
			}
			return data;
		}
		case TAL:
			return endian_16lo8(ta);
		case TAH:
			return endian_16hi8(ta);
		case TBL:
			return endian_16lo8(tb);
		case TBH:
			return endian_16hi8(tb);

			// TOD implementation taken from Vice
			// TOD clock is latched by reading Hours, and released
			// upon reading Tenths of Seconds. The counter itself
			// keeps ticking all the time.
			// Also note that this latching is different from the input one.
		case TOD_TEN: // Time Of Day clock 1/10 s
		case TOD_SEC: // Time Of Day clock sec
		case TOD_MIN: // Time Of Day clock min
		case TOD_HR: // Time Of Day clock hour
			if (!m_todlatched) {
				for (int i = 0; i < m_todlatch.length; i++) {
					m_todlatch[i] = m_todclock[i];
				}
			}
			if (addr == TOD_TEN)
				m_todlatched = false;
			if (addr == TOD_HR)
				m_todlatched = true;
			return m_todlatch[addr - TOD_TEN];

		case IDR: {
			// Clear IRQs, and return interrupt
			// data register
			short /* uint8_t */ret = idr;
			trigger(0);
			return ret;
		}

		case CRA:
			return cra;
		case CRB:
			return crb;
		default:
			return regs[addr];
		}
	}

	public void write(short /* uint_least8_t */addr, short /* uint8_t */data) {
		long /* event_clock_t */cycles;
		if (addr > 0x0f)
			return;

		regs[addr] = data;
		cycles = event_context.getTime(m_accessClk, event_context.phase());

		if (cycles != 0) {
			m_accessClk += cycles;
			// Sync up timers
			if ((cra & 0x21) == 0x01) {
				ta -= cycles;
				if (ta == 0)
					ta_event();
			}
			if ((crb & 0x61) == 0x01) {
				tb -= cycles;
				if (tb == 0)
					tb_event();
			}
		}

		switch (addr) {
		case PRA:
		case DDRA:
			portA();
			break;
		case PRB:
		case DDRB:
			portB();
			break;
		case TAL:
			ta_latch = endian_16lo8(ta_latch, data);
			break;
		case TAH:
			ta_latch = endian_16hi8(ta_latch, data);
			if ((cra & 0x01) == 0) // Reload timer if stopped
				ta = ta_latch;
			break;

		case TBL:
			tb_latch = endian_16lo8(tb_latch, data);
			break;
		case TBH:
			tb_latch = endian_16hi8(tb_latch, data);
			if ((crb & 0x01) == 0) // Reload timer if stopped
				tb = tb_latch;
			break;

		// TOD implementation taken from Vice
		case TOD_HR: // Time Of Day clock hour
			// Flip AM/PM on hour 12
			// (Andreas Boose <viceteam@t-online.de> 1997/10/11).
			// Flip AM/PM only when writing time, not when writing alarm
			// (Alexander Bluhm <mam96ehy@studserv.uni-leipzig.de> 2000/09/17).
			data &= 0x9f;
			if ((data & 0x1f) == 0x12 && ((crb & 0x80) == 0))
				data ^= 0x80;
			// deliberate run on
		case TOD_TEN: // Time Of Day clock 1/10 s
		case TOD_SEC: // Time Of Day clock sec
		case TOD_MIN: // Time Of Day clock min
			if ((crb & 0x80) != 0)
				m_todalarm[addr - TOD_TEN] = data;
			else {
				if (addr == TOD_TEN)
					m_todstopped = false;
				if (addr == TOD_HR)
					m_todstopped = true;
				m_todclock[addr - TOD_TEN] = data;
			}
			// check alarm
			if (!m_todstopped
					&& !memcmp(m_todalarm, m_todclock, m_todalarm.length))
				trigger(INTERRUPT_ALARM);
			break;

		case SDR:
			if ((cra & 0x40) != 0)
				sdr_buffered = true;
			break;

		case ICR:
			if ((data & 0x80) != 0)
				icr |= data & 0x1f;
			else
				icr &= (~data & 0xff);
			trigger(idr);
			break;

		case CRA:
			// Reset the underflow flipflop for the data port
			if (((data & 1) != 0) && ((cra & 1) == 0)) {
				ta = ta_latch;
				ta_underflow = true;
			}
			cra = data;

			// Check for forced load
			if ((data & 0x10) != 0) {
				cra &= (~0x10 & 0xff);
				ta = ta_latch;
			}

			if ((data & 0x21) == 0x01) {
				// Active
				event_context.schedule(event_ta,
						(long /* event_clock_t */) ta + 1, m_phase);
			} else {
				// Inactive
				event_context.cancel(event_ta);
			}
			break;

		case CRB:
			// Reset the underflow flipflop for the data port
			if (((data & 1) != 0) && ((crb & 1) == 0)) {
				tb = tb_latch;
				tb_underflow = true;
			}
			// Check for forced load
			crb = data;
			if ((data & 0x10) != 0) {
				crb &= (~0x10 & 0xff);
				tb = tb_latch;
			}

			if ((data & 0x61) == 0x01) {
				// Active
				event_context.schedule(event_tb,
						(long /* event_clock_t */) tb + 1, m_phase);
			} else {
				// Inactive
				event_context.cancel(event_tb);
			}
			break;

		default:
			break;
		}
	}

	public final String credits() {
		return credit;
	}

	/**
	 * @FIXME@ This is not correct! There should be muliple schedulers running
	 * at different rates that are passed into different function calls.<BR>
	 * This is the same as have different clock freqs connected to pins on the
	 * IC.
	 * @param clock
	 */
	public void clock(double /* float64_t */clock) {
		// Fixed point 25.7
		m_todPeriod = (long /* event_clock_t */) (clock * (double /* float64_t */) (1 << 7));
	}

	private boolean memcmp(short[] m_todalarm2, short[] m_todclock2, int length) {
		for (int i = 0; i < length; i++) {
			if (m_todalarm2[i] != m_todclock2[i]) {
				return true;
			}
		}
		return false;
	}

}
