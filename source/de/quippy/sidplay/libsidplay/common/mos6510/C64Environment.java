/**
 *                        This is the environment file which
 *                        defines all the standard functions
 *                           to be inherited by the ICs.
 *                        ----------------------------------
 *  begin                : Thu May 11 2000
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
package de.quippy.sidplay.libsidplay.common.mos6510;

public class C64Environment {

	/**
	 * Sidplay2 Player Environment
	 */
	private C64Environment m_envp;

	public void setEnvironment (C64Environment envp) {
		m_envp = envp;
	}
	
	//
	// Environment functions
	//
	
	protected void envReset() {
		m_envp.envReset();
	}

	protected short /* uint8_t */envReadMemByte(int /* uint_least16_t */addr) {
		return m_envp.envReadMemByte(addr);
	}

	protected void envWriteMemByte(int /* uint_least16_t */addr,
			short /* uint8_t */data) {
		m_envp.envWriteMemByte(addr, data);
	}

	//
	// Interrupts
	//
	
	protected void envTriggerIRQ() {
		m_envp.envTriggerIRQ();
	}

	protected void envTriggerNMI() {
		m_envp.envTriggerNMI();
	}

	protected void envTriggerRST() {
		m_envp.envTriggerRST();
	}

	protected void envClearIRQ() {
		m_envp.envClearIRQ();
	}

	//
	// Sidplay compatibly functions
	//
	
	protected boolean envCheckBankJump(int /* uint_least16_t */addr) {
		return m_envp.envCheckBankJump(addr);
	}

	protected short /* uint8_t */envReadMemDataByte(int /* uint_least16_t */addr) {
		return m_envp.envReadMemDataByte(addr);
	}

	protected void envSleep() {
		m_envp.envSleep();
	}

	protected void envLoadFile(String file) {
		m_envp.envLoadFile(file);
	}
}
