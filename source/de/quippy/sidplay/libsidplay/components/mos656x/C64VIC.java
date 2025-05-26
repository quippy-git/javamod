/**
 *                                      C64 VIC
 *                                      -------
 *  begin                : Fri Apr 4 2001
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

import de.quippy.sidplay.libsidplay.common.C64Env;

/**
 * The VIC emulation is very generic and here we need to effectively wire it
 * into the computer (like adding a chip to a PCB).
 *
 * @author Ken H�ndel
 *
 */
public class C64VIC extends MOS656X {

	private final C64Env m_env;

	@Override
	protected void interrupt(final boolean state) {
		m_env.interruptIRQ(state);
	}

	@Override
	protected void addrctrl(final boolean state) {
		m_env.signalAEC(state);
	}

	public C64VIC(final C64Env env) {
		super((env.context()));
		m_env = (env);
	}

	@Override
	public final String error() {
		return "";
	}

}
