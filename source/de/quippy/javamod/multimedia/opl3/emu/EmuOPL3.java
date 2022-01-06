/*
 * @(#) EmuOPL3.java
 *
 * Created on 10.08.2020 by Daniel Becker
 * 
 *-----------------------------------------------------------------------
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *----------------------------------------------------------------------
 */
package de.quippy.javamod.multimedia.opl3.emu;

import de.quippy.opl3.OPL3;

/**
 * @author Daniel Becker
 * @since 10.08.2020
 */
public class EmuOPL3 extends EmuOPL
{
	private OPL3 opl3 = null;
	private int[] outbuffer = null;

	/**
	 * Constructor for EmuOPL3
	 * @param ver
	 * @param sampleRate
	 */
	public EmuOPL3(final version ver, final float sampleRate, final oplType OPLType)
	{
		super(ver, sampleRate, OPLType);
		opl3 = new OPL3();
		outbuffer = new int[4];
	}
	/**
	 * @see de.quippy.javamod.multimedia.opl3.emu.EmuOPL#resetOPL()
	 */
	@Override
	public void resetOPL()
	{
		for (int register=0; register<256; register++)
		{
			writeOPL3(0, register, 0);
			writeOPL3(1, register, 0);
		}
		if (OPLType==oplType.OPL3)
			writeOPL3(1, 5, 1);
	}
	/**
	 * @param buffer
	 * @see de.quippy.javamod.multimedia.opl3.emu.EmuOPL#read(int[])
	 */
	@Override
	public void read(final int[] buffer)
	{
		buffer[0]=buffer[1]=0;
		opl3.read(outbuffer, 1);
		for (int i=0; i<4; i++) 
			buffer[i&1] += outbuffer[i];
	}
	/**
	 * @param reg
	 * @param value
	 * @see de.quippy.javamod.multimedia.opl3.emu.EmuOPL#writeOPL2(int, int)
	 */
	@Override
	public void writeOPL2(final int reg, final int value)
	{
		opl3.write(0, reg, value);
	}
	/**
	 * @param bank
	 * @param reg
	 * @param value
	 * @see de.quippy.javamod.multimedia.opl3.emu.EmuOPL#writeDualOPL2(int, int, int)
	 */
	@Override
	public void writeDualOPL2(final int bank, final int reg, final int value)
	{
		opl3.write(bank, reg, value);
	}
	/**
	 * @param base
	 * @param reg
	 * @param value
	 * @see de.quippy.javamod.multimedia.opl3.emu.EmuOPL#writeOPL3(int, int, int)
	 */
	@Override
	public void writeOPL3(final int base, final int reg, final int value)
	{
		opl3.write(base, reg, value);
	}
}
