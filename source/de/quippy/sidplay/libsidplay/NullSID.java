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

	@Override
	public void reset()
	{
		super.reset();
	}

	@Override
	public void reset(final short /* uint8_t */volume)
	{
	}

	@Override
	public short /* uint8_t */read(final short /* uint_least8_t */addr)
	{
		return 0;
	}

	@Override
	public void write(final short /* uint_least8_t */addr, final short /* uint8_t */data)
	{
	}

	@Override
	public final String credits()
	{
		return "";
	}

	@Override
	public final String error()
	{
		return "";
	}

	//
	// Standard SID functions
	//

	@Override
	public long /* int_least32_t */output(final short /* uint_least8_t */volume)
	{
		return 0;
	}

	@Override
	public void voice(final short /* uint_least8_t */num, final short /* uint_least8_t */vol, final boolean mute)
	{
	}

	@Override
	public void gain(final short /* uint_least8_t */percent)
	{
	}
}
