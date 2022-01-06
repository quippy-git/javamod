/**
 *                           Sid Builder Classes
 *                           -------------------
 *  begin                : Sat May 6 2001
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

import de.quippy.sidplay.libsidplay.common.ISID2Types.sid2_model_t;

/**
 * @author Ken H�ndel
 * 
 * Inherit this class to create a new SID emulations for libsidplay2.
 */
public abstract class SIDBuilder {
	private final String m_name;

	/**
	 * Determine current state of object (true = okay, false = error).
	 */
	protected boolean m_status;

	public SIDBuilder(final String name) {
		m_name = (name);
		m_status = (true);
	}
	
	public abstract SIDEmu lock(C64Env env, sid2_model_t model);

	public abstract void unlock(SIDEmu device);

	public final String name() {
		return m_name;
	}

	public abstract String error();

	public abstract String credits();
	
	public final boolean bool() {
		return m_status;
	}
}
