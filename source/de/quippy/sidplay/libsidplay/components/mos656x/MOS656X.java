/**
 *                              Minimal VIC emulation
 *                              ---------------------
 *  begin                : Wed May 21 2001
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
package de.quippy.sidplay.libsidplay.components.mos656x;

import static de.quippy.sidplay.libsidplay.Config.S_A_WHITE_EMAIL;
import static de.quippy.sidplay.libsidplay.common.SIDEndian.endian_16lo8;
import de.quippy.sidplay.libsidplay.common.Event;
import de.quippy.sidplay.libsidplay.common.IComponent;
import de.quippy.sidplay.libsidplay.common.IEventContext;
import de.quippy.sidplay.libsidplay.common.SIDEndian;

/**
 * References below are from: <BR>
 * The MOS 6567/6569 video controller (VIC-II) and its application in the
 * Commodore 64 http://www.uni-mainz.de/~bauec002/VIC-Article.gz
 * 
 * @author Ken H�ndel
 * 
 */
public abstract class MOS656X extends Event implements IComponent {

	public enum mos656x_model_t {
		MOS6567R56A, /* OLD NTSC CHIP */
		MOS6567R8, /* NTSC */
		MOS6569, /* PAL */
	}

	public static final int MOS6567R56A_SCREEN_HEIGHT = 262;

	public static final int MOS6567R56A_SCREEN_WIDTH = 64;

	public static final int MOS6567R56A_FIRST_DMA_LINE = 0x30;

	public static final int MOS6567R56A_LAST_DMA_LINE = 0xf7;

	public static final int MOS6567R8_SCREEN_HEIGHT = 263;

	public static final int MOS6567R8_SCREEN_WIDTH = 65;

	public static final int MOS6567R8_FIRST_DMA_LINE = 0x30;

	public static final int MOS6567R8_LAST_DMA_LINE = 0xf7;

	public static final int MOS6569_SCREEN_HEIGHT = 312;

	public static final int MOS6569_SCREEN_WIDTH = 63;

	public static final int MOS6569_FIRST_DMA_LINE = 0x30;

	public static final int MOS6569_LAST_DMA_LINE = 0xf7;

	/**
	 * Optional information
	 */
	private static final String credit = "*MOS656X (VICII) Emulation:"
			+ "\tCopyright (C) 2001 Simon White <" + S_A_WHITE_EMAIL + ">";

	protected short /* uint8_t */regs[] = new short[0x40];

	protected short /* uint8_t */icr, idr, ctrl1;

	protected int /* uint_least16_t */yrasters, xrasters, raster_irq;

	protected int /* uint_least16_t */raster_x, raster_y;

	protected int /* uint_least16_t */first_dma_line, last_dma_line, y_scroll;

	protected boolean bad_lines_enabled, bad_line;

	protected boolean vblanking;

	protected boolean lp_triggered;

	protected short /* uint8_t */lpx, lpy;

	// protected short /* uint8_t &*/sprite_enable, sprite_y_expansion;

	protected short /* uint8_t */sprite_dma, sprite_expand_y;

	protected short /* uint8_t */sprite_mc_base[] = new short[8];

	protected long /* event_clock_t */m_rasterClk;

	protected IEventContext event_context;

	protected event_phase_t m_phase;

	protected MOS656X(IEventContext context) {
		super("VIC Raster");
		event_context = context;
		m_phase = event_phase_t.EVENT_CLOCK_PHI1;
		// sprite_enable = regs[0x15];
		// sprite_y_expansion = regs[0x17];
		chip(mos656x_model_t.MOS6569);
	}

	public void event() {
		long /* event_clock_t */cycles = event_context.getTime(m_rasterClk,
				event_context.phase());

		// Cycle already executed check
		if (cycles == 0)
			return;

		long /* event_clock_t */delay = 1;
		int /* uint_least16_t */cycle;

		// Update x raster
		m_rasterClk += cycles;
		raster_x += cycles;
		cycle = (raster_x + 9) % xrasters;
		raster_x %= xrasters;

		switch (cycle) {
		case 0: { // Calculate sprite DMA
			short /* uint8_t */y = (short) (raster_y & 0xff);
			short /* uint8_t */mask = 1;
			sprite_expand_y ^= regs[0x17] /* sprite_y_expansion */; // 3.8.1-2
			for (int i = 1; i < 0x10; i += 2, mask <<= 1) {
				// 3.8.1-3
				if (((regs[0x15] /* sprite_enable */& mask) != 0)
						&& (y == regs[i])) {
					sprite_dma |= mask;
					sprite_mc_base[i >> 1] = 0;
					sprite_expand_y &= ~(regs[0x17] /* sprite_y_expansion */& mask) & 0xff;
				}
			}

			delay = 2;
			if ((sprite_dma & 0x01) != 0)
				addrctrl(false);
			else {
				addrctrl(true);
				// No sprites before next compulsory cycle
				if ((sprite_dma & 0x1f) == 0)
					delay = 9;
			}
			break;
		}

		case 1:
			break;

		case 2:
			if ((sprite_dma & 0x02) != 0)
				addrctrl(false);
			break;

		case 3:
			if ((sprite_dma & 0x03) == 0)
				addrctrl(true);
			break;

		case 4:
			if ((sprite_dma & 0x04) != 0)
				addrctrl(false);
			break;

		case 5:
			if ((sprite_dma & 0x06) == 0)
				addrctrl(true);
			break;

		case 6:
			if ((sprite_dma & 0x08) != 0)
				addrctrl(false);
			break;

		case 7:
			if ((sprite_dma & 0x0c) == 0)
				addrctrl(true);
			break;

		case 8:
			if ((sprite_dma & 0x10) != 0)
				addrctrl(false);
			break;

		case 9: // IRQ occurred (xraster != 0)
			if (raster_y == (yrasters - 1))
				vblanking = true;
			else {
				raster_y++;
				// Trigger raster IRQ if IRQ line reached
				if (raster_y == raster_irq)
					trigger(MOS656X_INTERRUPT_RST);
			}
			if ((sprite_dma & 0x18) == 0)
				addrctrl(true);
			break;

		case 10: // Vertical blank (line 0)
			if (vblanking) {
				vblanking = lp_triggered = false;
				raster_y = 0;
				// Trigger raster IRQ if IRQ in line 0
				if (raster_irq == 0)
					trigger(MOS656X_INTERRUPT_RST);
			}
			if ((sprite_dma & 0x20) != 0)
				addrctrl(false);
			// No sprites before next compulsory cycle
			else if ((sprite_dma & 0xf8) == 0)
				delay = 10;
			break;

		case 11:
			if ((sprite_dma & 0x30) == 0)
				addrctrl(true);
			break;

		case 12:
			if ((sprite_dma & 0x40) != 0)
				addrctrl(false);
			break;

		case 13:
			if ((sprite_dma & 0x60) == 0)
				addrctrl(true);
			break;

		case 14:
			if ((sprite_dma & 0x80) != 0)
				addrctrl(false);
			break;

		case 15:
			delay = 2;
			if ((sprite_dma & 0xc0) == 0) {
				addrctrl(true);
				delay = 5;
			}
			break;

		case 16:
			break;

		case 17:
			delay = 2;
			if ((sprite_dma & 0x80) == 0) {
				addrctrl(true);
				delay = 3;
			}
			break;

		case 18:
			break;

		case 19:
			addrctrl(true);
			break;

		case 20: // Start bad line
		{ // In line $30, the DEN bit controls if Bad Lines can occur
			if (raster_y == first_dma_line)
				bad_lines_enabled = (ctrl1 & 0x10) != 0;

			// Test for bad line condition
			bad_line = (raster_y >= first_dma_line)
					&& (raster_y <= last_dma_line)
					&& ((raster_y & 7) == y_scroll) && bad_lines_enabled;

			if (bad_line) {
				// DMA starts on cycle 23
				addrctrl(false);
			}
			delay = 3;
			break;
		}

		case 23: { // 3.8.1-7
			for (int i = 0; i < 8; i++) {
				if ((sprite_expand_y & (1 << i)) != 0)
					sprite_mc_base[i] += 2;
			}
			break;
		}

		case 24: {
			short /* uint8_t */mask = 1;
			for (int i = 0; i < 8; i++, mask <<= 1) { // 3.8.1-8
				if ((sprite_expand_y & mask) != 0)
					sprite_mc_base[i]++;
				if ((sprite_mc_base[i] & 0x3f) == 0x3f)
					sprite_dma &= ~mask & 0xff;
			}
			delay = 39;
			break;
		}

		case 63: // End DMA - Only get here for non PAL
			addrctrl(true);
			delay = xrasters - cycle;
			break;

		default:
			if (cycle < 23)
				delay = 23 - cycle;
			else if (cycle < 63)
				delay = 63 - cycle;
			else
				delay = xrasters - cycle;
		}

		event_context.schedule(this, delay
				- (event_context.phase() == event_phase_t.EVENT_CLOCK_PHI1 ? 0
						: 1), m_phase);
	}

	protected void trigger(int irq) {
		if (irq == 0) { // Clear any requested IRQs
			if ((idr & MOS656X_INTERRUPT_REQUEST) != 0)
				interrupt(false);
			idr = 0;
			return;
		}

		idr |= irq;
		if ((icr & idr) != 0) {
			if ((idr & MOS656X_INTERRUPT_REQUEST) == 0) {
				idr |= MOS656X_INTERRUPT_REQUEST;
				interrupt(true);
			}
		}
	}

	//
	// Environment Interface
	//

	protected abstract void interrupt(boolean state);

	protected abstract void addrctrl(boolean state);

	public void chip(mos656x_model_t model) {
		switch (model) {
		// Seems to be an older NTSC chip
		case MOS6567R56A:
			yrasters = MOS6567R56A_SCREEN_HEIGHT;
			xrasters = MOS6567R56A_SCREEN_WIDTH;
			first_dma_line = MOS6567R56A_FIRST_DMA_LINE;
			last_dma_line = MOS6567R56A_LAST_DMA_LINE;
			break;

		// NTSC Chip
		case MOS6567R8:
			yrasters = MOS6567R8_SCREEN_HEIGHT;
			xrasters = MOS6567R8_SCREEN_WIDTH;
			first_dma_line = MOS6567R8_FIRST_DMA_LINE;
			last_dma_line = MOS6567R8_LAST_DMA_LINE;
			break;

		// PAL Chip
		case MOS6569:
			yrasters = MOS6569_SCREEN_HEIGHT;
			xrasters = MOS6569_SCREEN_WIDTH;
			first_dma_line = MOS6569_FIRST_DMA_LINE;
			last_dma_line = MOS6569_LAST_DMA_LINE;
			break;
		}

		reset();
	}

	/**
	 * Handle light pen trigger
	 */
	public void lightpen() {
		// Synchronise simulation
		event();

		if (!lp_triggered) {
			// Latch current coordinates
			lpx = (short) (raster_x << 2);
			lpy = (short /* uint8_t */) (raster_y & 0xff);
			trigger(MOS656X_INTERRUPT_LP);
		}
	}

	//
	// Component Standard Calls
	//

	public void reset() {
		icr = idr = ctrl1 = 0;
		raster_irq = 0;
		y_scroll = 0;
		raster_y = yrasters - 1;
		raster_x = 0;
		bad_lines_enabled = false;
		m_rasterClk = 0;
		vblanking = lp_triggered = false;
		lpx = lpy = 0;
		sprite_dma = 0;
		sprite_expand_y = 0xff;
		for (int i = 0; i < regs.length; i++) {
			regs[i] = 0;
		}
		for (int i = 0; i < sprite_mc_base.length; i++) {
			sprite_mc_base[i] = 0;
		}
		event_context.schedule(this, 0, m_phase);
	}

	public short /* uint8_t */read(short /* uint_least8_t */addr) {
		if (addr > 0x3f)
			return 0;
		if (addr > 0x2e)
			return 0xff;

		// Sync up timers
		event();

		switch (addr) {
		case 0x11: // Control register 1
			return (short) ((ctrl1 & 0x7f) | ((raster_y & 0x100) >> 1));
		case 0x12: // Raster counter
			return (short) (raster_y & 0xFF);
		case 0x13:
			return lpx;
		case 0x14:
			return lpy;
		case 0x19: // IRQ flags
			return idr;
		case 0x1a: // IRQ mask
			return (short) (icr | 0xf0);
		default:
			return regs[addr];
		}
	}

	public void write(short /* uint_least8_t */addr, short /* uint8_t */data) {
		if (addr > 0x3f)
			return;

		regs[addr] = data;

		// Sync up timers
		event();

		switch (addr) {
		case 0x11: // Control register 1
		{
			raster_irq = SIDEndian
					.endian_16hi8(raster_irq, (short) (data >> 7));
			ctrl1 = data;
			y_scroll = data & 7;

			if (raster_x < 11)
				break;

			// In line $30, the DEN bit controls if Bad Lines can occur
			if ((raster_y == first_dma_line) && ((data & 0x10) != 0))
				bad_lines_enabled = true;

			// Bad Line condition?
			bad_line = (raster_y >= first_dma_line)
					&& (raster_y <= last_dma_line)
					&& ((raster_y & 7) == y_scroll) && bad_lines_enabled;

			// Start bad dma line now
			if (bad_line && (raster_x < 53))
				addrctrl(false);
			break;
		}

		case 0x12: // Raster counter
			raster_irq = endian_16lo8(raster_irq, data);
			break;

		case 0x17:
			sprite_expand_y |= ~data & 0xff; // 3.8.1-1
			break;

		case 0x19: // IRQ flags
			idr &= ((~data & 0x0f) | 0x80);
			if (idr == 0x80)
				trigger(0);
			break;

		case 0x1a: // IRQ mask
			icr = (short) (data & 0x0f);
			trigger(icr & idr);
			break;
		}
	}

	public String credits() {
		return credit;
	}

	// ----------------------------------------------------------------------------
	// Inline functions.
	// ----------------------------------------------------------------------------

	public static final int MOS656X_INTERRUPT_RST = 1 << 0;

	public static final int MOS656X_INTERRUPT_LP = 1 << 3;

	public static final int MOS656X_INTERRUPT_REQUEST = 1 << 7;

	// ----------------------------------------------------------------------------
	// END Inline functions.
	// ----------------------------------------------------------------------------

}
