/**
 *                                      description
 *                                      -----------
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

public interface IConf6510 {

	boolean MOS6510_CYCLE_BASED = true;

	boolean MOS6510_ACCURATE_CYCLES = true;

	boolean SIDPLAY = true;

	boolean MOS6510_STATE_6510 = false;

	boolean NO_RTS_UPON_BRK = false;
}
