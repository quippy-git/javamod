/*
 * @(#) MidiMacros.java
 *
 * Created on 15.06.2020 by Daniel Becker
 * 
 * This stuff is inspired by the coding of OpenMPT and originally
 * developed by OpenMPT Devs. Ported to JAVA by me.
 * The OpenMPT source code is released under the BSD license. 
 * 
 *-----------------------------------------------------------------------
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
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
package de.quippy.javamod.multimedia.mod.midi;

import java.io.IOException;

import de.quippy.javamod.io.ModfileInputStream;

/**
 * @author Daniel Becker
 * @since 15.06.2020
 */
public class MidiMacros
{
	public static final int MIDIOUT_START	= 0;
	public static final int MIDIOUT_STOP	= 1;
	public static final int MIDIOUT_TICK	= 2;
	public static final int MIDIOUT_NOTEON	= 3;
	public static final int MIDIOUT_NOTEOFF	= 4;
	public static final int MIDIOUT_VOLUME	= 5;
	public static final int MIDIOUT_PAN		= 6;
	public static final int MIDIOUT_BANKSEL	= 7;
	public static final int MIDIOUT_PROGRAM	= 8;
	
	private static final String EMPTY_STRING = "";
	private static final int ANZ_GLB = 9;
	private static final int ANZ_SFX = 16;
	private static final int ANZ_ZXX = 128;
	private static final int MACRO_LEN = 32;
	public static final int SIZE_OF_SCTUCT = (ANZ_GLB+ANZ_SFX+ANZ_ZXX)*MACRO_LEN;
	
	private String [] midiGlobal;
	private String [] midiSFXExt;
	private String [] midiZXXExt;
	enum ParameteredMacroTypes
	{
		SFxUnused, SFxCutoff, SFxReso, SFxFltMode, SFxDryWet, SFxCC, 
		SFxPlugParam, SFxChannelAT, SFxPolyAT, SFxPitch, SFxProgChange, 
		SFxCustom,
	}
	enum FixedMacroTypes
	{
        ZxxUnused, ZxxReso4Bit, ZxxReso7Bit, ZxxCutoff, ZxxFltMode, 
        ZxxResoFltMode, ZxxChannelAT, ZxxPolyAT, ZxxPitch, ZxxProgChange, 
        ZxxCustom
	}

	/**
	 * Constructor for MidiMacros
	 */
	public MidiMacros()
	{
		midiGlobal = new String[ANZ_GLB];
		midiSFXExt = new String[ANZ_SFX]; // read 16!
		midiZXXExt = new String[ANZ_ZXX]; // read 128;
		resetMidiMacros();
	}
	
	/**
	 * @since 15.06.2020
	 */
	public void clearZxxMacros()
	{
		for (int i=0; i<ANZ_SFX; i++) midiSFXExt[i]=EMPTY_STRING;
		for (int i=0; i<ANZ_ZXX; i++) midiZXXExt[i]=EMPTY_STRING;
	}
	/**
	 * @since 15.06.2020
	 */
	public void clearAllMacros()
	{
		for (int i=0; i<ANZ_GLB; i++) midiGlobal[i]=EMPTY_STRING;
		clearZxxMacros();
	}
	/**
	 * create Zxx (Z00-Z7F) default macros
	 * @since 16.06.2020
	 * @param macroType
	 * @param subType
	 * @return
	 */
	public static String createParameteredMacro(ParameteredMacroTypes macroType, int subType)
	{
	    switch(macroType)
	    {
		    case SFxUnused:		return EMPTY_STRING;
		    case SFxCutoff:		return "F0F000z";
		    case SFxReso:		return "F0F001z";
		    case SFxFltMode:	return "F0F002z";
		    case SFxDryWet:		return "F0F003z";
		    case SFxCC:			return String.format("Bc%02X", Integer.valueOf(subType & 0x7F));
		    case SFxPlugParam:	return String.format("F0F%03X", Integer.valueOf((subType & 0x17F) + 0x80));
		    case SFxChannelAT:	return "Dcz"; 
		    case SFxPolyAT:		return "Acnz";
		    case SFxPitch:		return "Ec00z";
		    case SFxProgChange:	return "Ccz";
		    case SFxCustom:
		    default:			return EMPTY_STRING;
	    }
	}
	/**
	 * Create Zxx (Z80 - ZFF) default macros
	 * @since 16.06.2020
	 * @param macroType
	 * @return
	 */
	public static void createFixedMacro(String[] fixedMacros, FixedMacroTypes macroType)
	{
		for (int i = 0; i < ANZ_ZXX; i++)
		{
			String formatString = null;
			int param = i;
			switch (macroType)
			{
				case ZxxUnused:
					formatString = EMPTY_STRING;
					break;
				case ZxxReso4Bit:
					param = i * 8;
					if (i < 16)
						formatString = "F0F001%02X";
					else
						formatString = EMPTY_STRING;
					break;
				case ZxxReso7Bit:
					formatString = "F0F001%02X";
					break;
				case ZxxCutoff:
					formatString = "F0F000%02X";
					break;
				case ZxxFltMode:
					formatString = "F0F002%02X";
					break;
				case ZxxResoFltMode:
					param = (i & 0x0F) * 8;
					if (i < 16)
						formatString = "F0F001%02X";
					else if (i < 32)
						formatString = "F0F002%02X";
					else
						formatString = EMPTY_STRING;
					break;
				case ZxxChannelAT:
					formatString = "Dc%02X";
					break;
				case ZxxPolyAT:
					formatString = "Acn%02X";
					break;
				case ZxxPitch:
					formatString = "Ec00%02X";
					break;
				case ZxxProgChange:
					formatString = "Cc%02X";
					break;

				case ZxxCustom:
				default:
					formatString = EMPTY_STRING;
					continue;
			}

			fixedMacros[i] = String.format(formatString, Integer.valueOf(param));
		}
	}
	/**
	 * Delete all unwanted characters
	 * @since 16.06.2020
	 * @param macroString
	 * @return
	 */
	public static String getSafeMacro(final String macroString)
	{
	    StringBuilder sb = new StringBuilder();
	    for (char c : macroString.toCharArray())
	       if ("0123456789ABCDEFabchmnopsuvxyz".indexOf(c)!=-1) sb.append(c); 
	    return sb.toString();
	}
	/**
	 * get the midi command
	 * @since 16.06.2020
	 * @param macroString
	 * @return
	 */
	public static int getMacroPlugCommand(final String macroString)
	{
	    final char [] macro = MidiMacros.getSafeMacro(macroString).toCharArray();
	    return	(Character.digit(macro[0], 16)<<16) |
	    		(Character.digit(macro[1], 16)<< 8) |
	    		(Character.digit(macro[2], 16)<< 5) |
	    		(Character.digit(macro[3], 16));
	}
	/**
	 * get the midi command
	 * @since 16.06.2020
	 * @param macroIndex
	 * @return
	 */
	public int getMacroPlugCommand(final int macroIndex)
	{
	    return MidiMacros.getMacroPlugCommand(midiSFXExt[macroIndex]);
	}
	/**
	 * Get the value of the midi plug parameter
	 * @since 16.06.2020
	 * @param macroString
	 * @return
	 */
	public static int getMacroPlugParam(final String macroString)
	{
	    final char [] macro = MidiMacros.getSafeMacro(macroString).toCharArray();
	    int code = Character.digit(macro[4], 16)<<4 | Character.digit(macro[5], 16);
	    if (macro.length >= 4 && macro[3] == '0')
	        return (code - 128);
	    else
	        return (code + 128);
	}
	/**
	 * Get the value of the midi plug parameter
	 * from a midiSFXExt entry
	 * @since 16.06.2020
	 * @param macroIndex
	 * @return
	 */
	public int macroToPlugParam(final int macroIndex)
	{
		return MidiMacros.getMacroPlugParam(midiSFXExt[macroIndex]);
	}
	/**
	 * Get the value of the midi CC parameter
	 * @since 16.06.2020
	 * @param macroString
	 * @return
	 */
	public static int getMacroMidiCC(final String macroString)
	{
	    final char [] macro = MidiMacros.getSafeMacro(macroString).toCharArray();
	    int code = Character.digit(macro[2], 16)<<4 | Character.digit(macro[3], 16);
	    return code;
	}
	/**
	 * Get the value of the midi CC parameter
	 * from a midiSFXExt entry
	 * @since 16.06.2020
	 * @param macroIndex
	 * @return
	 */
	public int getMacroMidiCC(int macroIndex)
	{
	    return MidiMacros.getMacroMidiCC(midiSFXExt[macroIndex]);
	}
	/**
	 * @since 15.06.2020
	 */
	public void resetMidiMacros()
	{
		clearAllMacros();
		midiGlobal[MIDIOUT_START]	= "FF";
		midiGlobal[MIDIOUT_STOP]	= "FC";
		midiGlobal[MIDIOUT_NOTEON]	= "9c n v";
		midiGlobal[MIDIOUT_NOTEOFF]	= "9c n 0";
		midiGlobal[MIDIOUT_PROGRAM]	= "Cc p";
		midiSFXExt[0] = MidiMacros.createParameteredMacro(ParameteredMacroTypes.SFxCutoff, 0);
		MidiMacros.createFixedMacro(midiZXXExt, FixedMacroTypes.ZxxReso4Bit);
	}
	/**
	 * @since 15.06.2020
	 * @param inputStream
	 * @throws IOException
	 */
	public void loadFrom(ModfileInputStream inputStream) throws IOException
	{
		for (int i=0; i<ANZ_GLB; i++) midiGlobal[i]=inputStream.readString(MACRO_LEN);
		for (int i=0; i<ANZ_SFX; i++) midiSFXExt[i]=inputStream.readString(MACRO_LEN);
		for (int i=0; i<ANZ_ZXX; i++) midiZXXExt[i]=inputStream.readString(MACRO_LEN);
	}
	/**
	 * @since 16.06.2020
	 * @param index
	 * @return
	 */
	public String getMidiGlobal(final int index)
	{
		if (index<0 || index>=ANZ_GLB) return EMPTY_STRING;
		return midiGlobal[index];
	}
	/**
	 * @since 16.06.2020
	 * @param index
	 * @return
	 */
	public String getMidiSFXExt(final int index)
	{
		if (index<0 || index>=ANZ_SFX) return EMPTY_STRING;
		return midiSFXExt[index];
	}
	/**
	 * @since 16.06.2020
	 * @param index
	 * @return
	 */
	public String getMidiZXXExt(final int index)
	{
		if (index<0 || index>=ANZ_ZXX) return EMPTY_STRING;
		return midiZXXExt[index];
	}
}
