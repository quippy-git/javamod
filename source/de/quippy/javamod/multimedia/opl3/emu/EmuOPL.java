/*
 * @(#) EmuOPL.java
 *
 * Created on 08.08.2020 by Daniel Becker
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

/**
 * @author Daniel Becker
 * @since 08.08.2020
 */
public abstract class EmuOPL
{
	public enum version { FMOPL_072_YM3526, FMOPL_072_YM3812, OPL3 }
	public static final String [] versionNames =
	{
		 "YM3526 (OPL2) V0.72  by Jarek Burczynski",
		 "YM3812 (OPL2) V0.72  by Jarek Burczynski",
		 "YMF262 (OPL3) V1.0.6 by Robson Cozendey"
	};
	public enum oplType { OPL2, DUAL_OPL2, OPL3 }
	public static final String[] oplTypeString =
	{
	 	"OPL2", "Dual OPL2", "OPL3"
	};
	
	protected float sampleRate;
	protected version ver;
	protected oplType OPLType;
	
	public static EmuOPL createInstance(final version ver, final float sampleRate, final oplType OPLType)
	{
		switch (ver)
		{
			case FMOPL_072_YM3526:
			case FMOPL_072_YM3812:
				return new EmuFMOPL_072(ver, sampleRate, OPLType);
			case OPL3:
				return new EmuOPL3(ver, sampleRate, OPLType);
		}
		return null;
	}

	/**
	 * Constructor for EmuOPL
	 */
	public EmuOPL(final version ver, final float sampleRate, final oplType OPLType)
	{
		this.ver = ver;
		this.sampleRate = sampleRate;
		this.OPLType = OPLType;
	}
	public static int getIndexForVersion(final version ver)
	{
		switch (ver)
		{
			case FMOPL_072_YM3526:	return 0;
			case FMOPL_072_YM3812:	return 1;
			case OPL3:				return 2;
		}
		return -1;
	}
	public static oplType getOPLTypeForIndex(final int index)
	{
		switch (index)
		{
			case 0: return oplType.OPL2;
			case 1: return oplType.DUAL_OPL2;
			case 2: return oplType.OPL3;
		}
		return null;
	}
	public static int getIndexForOPLType(final oplType OPLType)
	{
		switch (OPLType)
		{
			case OPL2:		return 0;
			case DUAL_OPL2:	return 1;
			case OPL3:		return 2;
		}
		return -1;
	}
	public static version getVersionForIndex(final int index)
	{
		switch (index)
		{
			case 0: return version.FMOPL_072_YM3526;
			case 1: return version.FMOPL_072_YM3812;
			case 2: return version.OPL3;
		}
		return null;
	}
	/**
	 * @since 16.08.2020
	 * @return the sample rate
	 */
	public float getSampleRate()
	{
		return sampleRate;
	}
	/**
	 * @since 16.08.2020
	 * @return the OPL CHIP type
	 */
	public version getVersion()
	{
		return ver;
	}
	/**
	 * @return the OPLType
	 */
	public oplType getOPLType()
	{
		return OPLType;
	}

	public abstract void resetOPL();
	public abstract void read(final int[] buffer);
	public abstract void writeOPL2(final int reg, final int value);
	public abstract void writeDualOPL2(final int bank, final int reg, final int value);
	public abstract void writeOPL3(final int base, final int reg, final int value);
}
