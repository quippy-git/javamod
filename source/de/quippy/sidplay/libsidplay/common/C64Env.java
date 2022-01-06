/**
 *                         The C64 environment interface.
 *                         ------------------------------
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
package de.quippy.sidplay.libsidplay.common;

/**
 * An implementation of of this class can be created to perform the C64
 * specifics. A pointer to this child class can then be passed to each of the
 * Components so they can interact with it.
 */
public abstract class C64Env {

	private IEventContext m_context;

	public C64Env(IEventContext context) {
		m_context = (context);
	}

	public final IEventContext context() {
		return m_context;
	}

	public abstract void interruptIRQ(boolean state);

	public abstract void interruptNMI();

	public abstract void interruptRST();

	public abstract void signalAEC(boolean state);

	public abstract short /* uint8_t */readMemRamByte(
			int /* uint_least16_t */addr);

	public abstract void sid2crc(short /* uint8_t */data);

	public abstract void lightpen();

}
