/**
 *                   Event scheduler (based on alarm from Vice)
 *                   ------------------------------------------
 *  begin                : Wed May 9 2001
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

public abstract class Event {

	public enum event_phase_t {
		EVENT_CLOCK_PHI1, EVENT_CLOCK_PHI2
	}

	public static final int EVENT_CONTEXT_MAX_PENDING_EVENTS = 0x100;

	public final String m_name;

	public long /* event_clock_t */m_clk;

	/**
	 * This variable is set by the event context when it is scheduled
	 */
	public boolean m_pending;

	/**
	 * Link to the next and previous events in the list.
	 */
	public Event m_next, m_prev;

	public Event(final String name) {
		m_name = name;
		m_pending = false;
	}

	public abstract void event();

	public boolean pending() {
		return m_pending;
	}
}
