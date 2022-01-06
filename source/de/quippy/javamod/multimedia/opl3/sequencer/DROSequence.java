/*
 * @(#) DROSequence.java
 *
 * Created on 03.08.2020 by Daniel Becker
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
 *
 * As a proof of concept this was taken from dro.cpp and dro2.cpp
 * of the adplug project and ported to java.
 * Corrections and additions by to work with OPL3.java 
 * 2008 Robson Cozendey
 * 2020 Daniel Becker
 */
package de.quippy.javamod.multimedia.opl3.sequencer;

import java.io.IOException;
import java.net.URL;

import de.quippy.javamod.io.RandomAccessInputStreamImpl;
import de.quippy.javamod.multimedia.MultimediaContainerManager;
import de.quippy.javamod.multimedia.mod.ModConstants;
import de.quippy.javamod.multimedia.opl3.emu.EmuOPL;
import de.quippy.javamod.system.Helpers;

/**
 * @author Daniel Becker
 * @since 03.08.2020
 */
public class DROSequence extends OPL3Sequence
{
	private URL url;
	private int [] data = null;
	
	private int version;
	private boolean isOldVersion;
	private String magic;
	private long lengthInMilliseconds;
	private int length;
	private EmuOPL.oplType oplType;
	private int cmdDelayL;
	private int cmdDelayH;
	private int conversionTableLen;
	private int [] conversionTable;
	
	private String title;
	private String author;
	private String description;
	
	private int delay;
	private int pos;
	private int bank;
	
	/**
	 * Constructor for DROSequence
	 */
	public DROSequence()
	{
		super();
	}
	/**
	 * @param inputStream
	 * @throws IOException
	 * @see de.quippy.javamod.multimedia.opl3.sequencer.OPL3Sequence#readOPL3Sequence(de.quippy.javamod.io.RandomAccessInputStreamImpl)
	 */
	@Override
	protected void readOPL3Sequence(RandomAccessInputStreamImpl inputStream) throws IOException
	{
		if (inputStream==null || inputStream.available()<=0) return;
		
		final byte [] magicBytes = new byte[8];
		inputStream.read(magicBytes, 0, 8);
		magic = Helpers.retrieveAsString(magicBytes, 0, 8);
		if (!magic.equals("DBRAWOPL")) throw new IOException("Unsupported file type (unknown magic bytes)");
		
		version = inputStream.readIntelDWord();
		if ((version&0xFFFF)>2) throw new IOException("Unsupported file type (unknown version "+version+")");
		
		isOldVersion = ((version&0xFFFF)<2);
		
		if (!isOldVersion)
		{
			length = inputStream.readIntelDWord();
			length <<= 1;
			if (length<=0 || length>=1<<30 || length>inputStream.available()) throw new IOException("Unsupported file type (length read lied to us)");

			lengthInMilliseconds = (long)inputStream.readIntelDWord();
			final int OPLType = inputStream.read(); // OPL type (0 == OPL2, 1 == Dual OPL2, 2 == OPL3) 
			oplType = EmuOPL.getOPLTypeForIndex(OPLType);
			final int format = inputStream.read();
			if (format!=0) throw new IOException("Unsupported file type (unknown format)");
			final int compression = inputStream.read();
			if (compression!=0) throw new IOException("Unsupported file type (compression not supported)");
			cmdDelayL = inputStream.read();
			cmdDelayH = inputStream.read();
			conversionTableLen = inputStream.read();
			conversionTable = new int [conversionTableLen];
			for (int i=0; i<conversionTableLen; i++) 
				conversionTable[i] = inputStream.read();
		}
		else
		{
			lengthInMilliseconds = (long)inputStream.readIntelDWord();
			length = inputStream.readIntelDWord();
			if (length<3 || length>inputStream.available()) throw new IOException("Unsupported file type (length read lied to us)");
			
			final int OPLType = inputStream.read(); // OPL type (0 == OPL2, 1 == Dual OPL2, 2 == OPL3) 
			oplType = EmuOPL.getOPLTypeForIndex(OPLType);
			// constant values for cmdDelay
			cmdDelayL = 0x00; 
			cmdDelayH = 0x01;
			// let's see if the next three bytes are zero...
			byte [] zero = new byte[3];
			inputStream.read(zero, 0, 3);
			if (zero[0]!=0 || zero[1]!=0 || zero[2]!=0)
			{
				// need these three bytes!
				inputStream.seek(inputStream.getFilePointer() - 3);
			}
		}

		data = new int[length];
		for (int i=0; i<length; i++) data[i] = inputStream.read();
		
		final int tagSize = inputStream.available();
		if (tagSize>=3)
		{
			byte [] tagMagic = new byte[2];
			inputStream.read(tagMagic, 0, 2);
			if (tagMagic[0]==0xFF && tagMagic[1]==0xFF)
			{
				for (int i=0; i<3; i++) // three chunks
				{
					final int what = inputStream.read();
					if (what!=-1)
					{
						switch (what)
						{
							case 0x1A:
								title = inputStream.readString(40);
								break;
							case 0x1B:
								author = inputStream.readString(40);
								break;
							case 0x1C:
								description = inputStream.readString(1023);
								break;
						}
					}
				}
			}
		}
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.opl3.sequencer.OPL3Sequence#getSongName()
	 */
	@Override
	public String getSongName()
	{
		if (title!=null && title.length()>0) 
			return title;
		else
			return MultimediaContainerManager.getSongNameFromURL(url);
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.opl3.sequencer.OPL3Sequence#getAuthor()
	 */
	@Override
	public String getAuthor()
	{
		if (author!=null && author.length()!=0)
			return author;
		else
			return Helpers.EMPTY_STING;
	}
	private String getVersionString()
	{
		return ((new StringBuilder()).append("V").append(version&0xFFFF).append('.').append((version>>16)&0xFFFF)).toString();
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.opl3.sequencer.OPL3Sequence#getDescription()
	 */
	@Override
	public String getDescription()
	{
		StringBuilder sb = new StringBuilder();
		if (description!=null && description.length()!=0) sb.append(description).append("\n\nFile Informations:\n");
		sb.append("ID: ").append(magic).append('\n');
		sb.append("Version: ").append(getVersionString()).append('\n');
		sb.append("Length: ").append(length).append('\n');
		sb.append("length in ms (stored in file): ").append(Helpers.getTimeStringFromMilliseconds(lengthInMilliseconds)).append(" (").append(lengthInMilliseconds).append(")\n");
		sb.append("OPL Type: ").append(EmuOPL.oplTypeString[EmuOPL.getIndexForOPLType(oplType)]).append('\n');
		sb.append("Command delay small: 0x").append(ModConstants.getAsHex(cmdDelayL, 2)).append('\n');
		sb.append("Command delay high : 0x").append(ModConstants.getAsHex(cmdDelayH, 2)).append('\n');
		return sb.toString();
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.opl3.sequencer.OPL3Sequence#getTypeName()
	 */
	@Override
	public String getTypeName()
	{
		return ((new StringBuilder()).append("DOSBox Raw OPL File ").append(getVersionString())).toString();
	}
	/**
	 * @param url
	 * @see de.quippy.javamod.multimedia.opl3.sequencer.OPL3Sequence#setURL(java.net.URL)
	 */
	@Override
	public void setURL(URL url)
	{
		this.url = url;
	}
	/**
	 * @param opl
	 * @return
	 * @see de.quippy.javamod.multimedia.opl3.sequencer.OPL3Sequence#updateToOPL(de.quippy.opl3.OPL3)
	 */
	@Override
	public boolean updateToOPL(final EmuOPL opl)
	{
		if (!isOldVersion)
		{
			while (pos < length) 
			{
				final int index = data[pos++] & 0xFF;
				if (pos >= length) return false;
				final int value = data[pos++] & 0xFF;
				
				if (index == cmdDelayL)
				{
					delay = value + 1;
					return true;
				}
				else
				if (index == cmdDelayH)
				{
					delay = (value + 1) << 8;
					return true;
				}
				else
				{
					bank = (index >> 7) & 0x01;
					final int reg = conversionTable[index & 0x7F] & 0xFF;
					if (oplType==EmuOPL.oplType.OPL2)
						opl.writeOPL2(reg, value);
					else
					if (oplType==EmuOPL.oplType.DUAL_OPL2)
					{
						opl.writeDualOPL2(bank, reg, value);
					}
					else
						opl.writeOPL3(bank, reg, value);
				}
			}
		}
		else
		{
			while (pos < length) 
			{
				int index = data[pos++] & 0xFF;
				
				if (index == cmdDelayL)
				{
					if (pos >= length) return false;
					final int value = data[pos++] & 0xFF;
					delay = value + 1;
					return true;
				}
				else
				if (index == cmdDelayH)
				{
					if (pos+1 >= length) return false;
					delay = (data[pos] | (data[pos+1]<<8)) + 1;
					pos += 2;
					return true;
				}
				else
				if (index == 0x02  || index == 0x03) // Bankswitch
				{
					bank = index - 0x02;
				}
				else
				{
					if (index == 0x04)
					{
						if (pos >= length) return false;
						index = data[pos++];
					}
					
					if (pos >= length) return false;
					final int value = data[pos++] & 0xff;
					if (oplType==EmuOPL.oplType.OPL2)
						opl.writeOPL2(index, value);
					else
					if (oplType==EmuOPL.oplType.DUAL_OPL2)
					{
						opl.writeDualOPL2(bank, index, value);
					}
					else
						opl.writeOPL3(bank, index, value);
				}
			}
		}
		return pos < length;
	}
	/**
	 * @param opl
	 * @see de.quippy.javamod.multimedia.opl3.sequencer.OPL3Sequence#initialize(de.quippy.opl3.OPL3)
	 */
	@Override
	public void initialize(final EmuOPL opl)
	{
		pos = 0;
		delay = 0;
		bank = 0;
		resetOPL(opl);
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.opl3.sequencer.OPL3Sequence#getRefresh()
	 */
	@Override
	public double getRefresh()
	{
		if (delay!=0) return 1000d / (double)delay;
		else return 1000d;
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.opl3.sequencer.OPL3Sequence#getOPLType()
	 */
	@Override
	public de.quippy.javamod.multimedia.opl3.emu.EmuOPL.oplType getOPLType()
	{
		return oplType;
	}
}
