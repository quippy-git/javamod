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

public abstract class SIDEmu implements IComponent {

	private SIDBuilder m_builder;

	public SIDEmu(SIDBuilder builder) {
		m_builder = (builder);
	}

	//
	// Standard component functions
	//

	public void reset() {
		reset((short) 0);
	}

	public abstract void reset(short /* uint8_t */volume);

	public abstract short /* uint8_t */read(short /* uint_least8_t */addr);

	public abstract void write(short /* uint_least8_t */addr,
			short /* uint8_t */data);

	public abstract String credits();

	//
	// Standard SID functions
	//

	public abstract long /* int_least32_t */output(
			short /* uint_least8_t */bits);

	public abstract void voice(short /* uint_least8_t */num,
			short /* uint_least8_t */vol, boolean mute);

	public abstract void gain(short /* uint_least8_t */precent);

	public void optimisation(byte /* uint_least8_t */level) {
		
	}

	final public SIDBuilder builder() {
		return m_builder;
	}

}
