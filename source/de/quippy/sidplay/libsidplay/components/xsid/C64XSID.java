/**
 *                               ReSid Wrapper
 *                               -------------
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
package de.quippy.sidplay.libsidplay.components.xsid;

import de.quippy.sidplay.libsidplay.common.C64Env;
import de.quippy.sidplay.libsidplay.common.SIDEmu;

/**
 * This file could be a specialisation of a sid implementation. However since
 * the sid emulation is not part of this project we are actually creating a
 * wrapper instead.
 */
public class C64XSID extends XSID {

	private C64Env m_env;

	private SIDEmu m_sid;

	private long /* int_least32_t */m_gain;

	protected  short /* uint8_t */readMemByte(int /* uint_least16_t */addr) {
		short /* uint8_t */data = m_env.readMemRamByte(addr);
		m_env.sid2crc(data);
		return data;
	}

	protected void writeMemByte(short /* uint8_t */data) {
		m_sid.write((short) 0x18, data);
	}

	public C64XSID(C64Env env, SIDEmu sid) {
		super(env.context());
		m_env = (env);
		m_sid = (sid);
		m_gain = (100);
	}

	//
	// Standard component interface
	//
	
	/**
	 * {@inheritDoc}
	 */
	public final String error() {
		return "";
	}

	public void reset() {
		super.reset();
	}

	public void reset(short /* uint8_t */volume) {
		super.reset(volume);
		m_sid.reset(volume);
	}

	public short /* uint8_t */read(short /* uint_least8_t */addr) {
		return m_sid.read(addr);
	}

	public void write(short /* uint_least8_t */addr, short /* uint8_t */data) {
		if (addr == 0x18)
			super.storeSidData0x18(data);
		else
			m_sid.write(addr, data);
	}

	public void write16(int /* uint_least16_t */addr, short /* uint8_t */data) {
		super.write(addr, data);
	}

	//
	// Standard SID interface
	//
	
	public long /* int_least32_t */output(short /* uint_least8_t */bits) {
		return m_sid.output(bits) + (super.output(bits) * m_gain / 100);
	}

	public void voice(short /* uint_least8_t */num,
			short /* uint_least8_t */vol, boolean mute) {
		if (num == 3)
			super.mute(mute);
		else
			m_sid.voice(num, vol, mute);
	}

	public void gain(short /* uint_least8_t */percent) {
		// 0 to 99 is loss, 101 - 200 is gain
		m_gain = percent;
		m_gain += 100;
		if (m_gain > 200)
			m_gain = 200;
	}

	//
	// Xsid specific
	//
	
	public void emulation(SIDEmu sid) {
		m_sid = sid;
	}

	public SIDEmu emulation() {
		return m_sid;
	}

}
