/**
 *                                     C64 CIAs
 *                                     --------
 *  begin                : Fri Apr 4 2001
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

import de.quippy.sidplay.libsidplay.common.C64Env;

/**
 * The CIA emulations are very generic and here we need to effectively wire them
 * into the computer (like adding a chip to a PCB).
 *
 * @author Ken H�ndel
 *
 */
public class C64CIA {

	/**
	 * CIA 1 specifics: Generates IRQs
	 */
	public static class C64cia1 extends MOS6526 {
		private final C64Env m_env;

		private short /* uint8_t */lp;

		@Override
		public void interrupt(final boolean state) {
			m_env.interruptIRQ(state);
		}

		@Override
		public void portA() {
		}

		@Override
		public void portB() {
			final short /* uint8_t */lp = (short) ((regs[PRB] /* prb */| (~regs[DDRB] /* ddrb */& 0xff)) & 0x10);
			if (lp != this.lp)
				m_env.lightpen();
			this.lp = lp;
		}

		public C64cia1(final C64Env env) {
			super(env.context());
			m_env = (env);
		}

		@Override
		public final String error() {
			return "";
		}

		@Override
		public void reset() {
			lp = 0x10;
			super.reset();
		}
	}

	/**
	 * CIA 2 specifics: Generates NMIs
	 */
	public static class C64cia2 extends MOS6526 {
		private final C64Env m_env;

		@Override
		public void portA() {
		}

		@Override
		public void portB() {
		}

		@Override
		public void interrupt(final boolean state) {
			if (state)
				m_env.interruptNMI();
		}

		public C64cia2(final C64Env env) {
			super((env.context()));
			m_env = (env);
		}

		@Override
		public final String error() {
			return "";
		}
	}

}
