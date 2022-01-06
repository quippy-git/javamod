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

import de.quippy.sidplay.libsidplay.common.Event.event_phase_t;

/**
 * @author Ken H�ndel
 *
 * Public Event Context
 */
public interface IEventContext {

	void cancel(Event event);

	void schedule(Event event, long /* event_clock_t */cycles,
			event_phase_t phase);

	long /* event_clock_t */getTime(event_phase_t phase);

	long /* event_clock_t */getTime(long /* event_clock_t */clock,
			event_phase_t phase);

	event_phase_t phase();
}
