/**
 *                               Null SID Emulation
 *                               ------------------
 *  begin                : Thurs Sep 20 2001
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
package de.quippy.sidplay.libsidplay;

import de.quippy.sidplay.libsidplay.common.SIDEmu;

public class NullSID extends SIDEmu
{
	public NullSID()
	{
		super(null);
	}

	//
	// Standard component functions
	//

	public void reset()
	{
		super.reset();
	}

	public void reset(short /* uint8_t */volume)
	{
	}

	public short /* uint8_t */read(short /* uint_least8_t */addr)
	{
		return 0;
	}

	public void write(short /* uint_least8_t */addr, short /* uint8_t */data)
	{
	}

	public final String credits()
	{
		return "";
	}

	public final String error()
	{
		return "";
	}

	//
	// Standard SID functions
	//

	public long /* int_least32_t */output(short /* uint_least8_t */volume)
	{
		return 0;
	}

	public void voice(short /* uint_least8_t */num, short /* uint_least8_t */vol, boolean mute)
	{
	}

	public void gain(short /* uint_least8_t */percent)
	{
	}
}
