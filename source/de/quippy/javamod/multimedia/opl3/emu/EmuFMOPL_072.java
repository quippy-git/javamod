/*
 * @(#) EmuFMOPL_072.java
 *
 * Created on 11.08.2020 by Daniel Becker
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

import de.quippy.opl3.FMOPL_072;
import de.quippy.opl3.FMOPL_072.FM_OPL;

/**
 * @author Daniel Becker
 * @since 11.08.2020
 */
public class EmuFMOPL_072 extends EmuOPL
{
	//private static final int CLOCK_RATE = 3579545;
	private static final int CLOCK_RATE = 3579552;	// 49716 * 72, Base Clock of Chip - is said to be 3.579MHz

	private FM_OPL[] opl = null;

	/**
	 * Constructor for EmuFMOPL_072
	 * @param ver the OPL Version
	 * @param sampleRate
	 * @param OPLType
	 * @throws IllegalArgumentException if OPLType is OPL3 
	 */
	public EmuFMOPL_072(final version ver, final float sampleRate, final oplType OPLType)
	{
		super(ver, sampleRate, OPLType);
		
		if (OPLType == oplType.OPL3)
		{
			final String type = (ver == EmuOPL.version.FMOPL_072_YM3526)?"YM3226":"YM3812"; 
			throw new IllegalArgumentException(type + " does not support OPL3.");
		}

		opl = new FM_OPL[2];
		if (ver == EmuOPL.version.FMOPL_072_YM3526)
		{
			opl[0] = FMOPL_072.init(FMOPL_072.OPL_TYPE_YM3526, CLOCK_RATE, (int)sampleRate);
			if (OPLType==oplType.DUAL_OPL2)
				opl[1] = FMOPL_072.init(FMOPL_072.OPL_TYPE_YM3526, CLOCK_RATE, (int)sampleRate);
		}
		else
		if (ver == EmuOPL.version.FMOPL_072_YM3812)
		{
			opl[0] = FMOPL_072.init(FMOPL_072.OPL_TYPE_YM3812, CLOCK_RATE, (int)sampleRate);
			if (OPLType==oplType.DUAL_OPL2)
				opl[1] = FMOPL_072.init(FMOPL_072.OPL_TYPE_YM3812, CLOCK_RATE, (int)sampleRate);
		}
//		else
//		if (ver == EmuOPL.version.FMOPL_072_Y8950)
//		{
//			opl[0] = FMOPL_072.y8950_init(CLOCK_RATE, (int)sampleRate);
//			if (dualOPL)
//				opl[1] = FMOPL_072.y8950_init(CLOCK_RATE, (int)sampleRate);
//		}
	}

	/**
	 * 
	 * @see de.quippy.javamod.multimedia.opl3.emu.EmuOPL#resetOPL()
	 */
	@Override
	public void resetOPL()
	{
		FMOPL_072.reset_chip(opl[0]);
		if (opl[1]!=null) 
			FMOPL_072.reset_chip(opl[1]);
	}

	/**
	 * @param buffer
	 * @see de.quippy.javamod.multimedia.opl3.emu.EmuOPL#read(int[])
	 */
	@Override
	public void read(final int[] buffer)
	{
//		if (ver == EmuOPL.version.FMOPL_072_Y8950)
//		{
//			FMOPL_072.y8950_update_one(opl[0], buffer, 1);
//			buffer[1] = buffer[0];
//			if (opl[1]!=null) 
//				FMOPL_072.y8950_update_one(opl[1], buffer, 1);
//		}
//		else
//		{
			FMOPL_072.update_one(opl[0], buffer, 1);
			buffer[1] = buffer[0];
			if (opl[1]!=null) 
				FMOPL_072.update_one(opl[1], buffer, 1);
//		}
	}

	/**
	 * @param reg
	 * @param value
	 * @see de.quippy.javamod.multimedia.opl3.emu.EmuOPL#writeOPL2(int, int)
	 */
	@Override
	public void writeOPL2(final int reg, final int value)
	{
		FMOPL_072.write(opl[0], 0, reg);
		FMOPL_072.write(opl[0], 1, value);
		if (opl[1]!=null)
		{
			FMOPL_072.write(opl[1], 0, reg);
			FMOPL_072.write(opl[1], 1, value);
		}
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
		FMOPL_072.write(opl[bank], 0, reg);
		FMOPL_072.write(opl[bank], 1, value);
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
//		FMOPL_072.write(opl[base], 0, reg);
//		FMOPL_072.write(opl[base], 1, value);
	}
}
