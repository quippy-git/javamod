/**
 *                         Standard IC interface functions
 *                         -------------------------------
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

public interface IComponent {

	void reset();

	short /* uint8_t */read(short /* uint_least8_t */addr);

	void write(short /* uint_least8_t */addr, short /* uint_least8_t */data);

	String credits();

	String error();
}
