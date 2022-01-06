/*
 * @(#) ID3v2Footer.java
 *
 * Created on 23.12.2008 by Daniel Becker
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
package de.quippy.javamod.multimedia.mp3.id3;

import java.io.FileNotFoundException;
import java.io.IOException;

import de.quippy.javamod.io.RandomAccessInputStream;
import de.quippy.javamod.system.Helpers;

/**
 * Description: 
 *  This class implements and id3v2 footer which is essentially the same as an id3v2
 *  header but occurs at the end of the tag and is optional.
 *
 * @author:  Jonathan Hilliker modified by Daniel Becker
 */
public class ID3v2Footer
{
	private static final String ENC_TYPE = "ISO-8859-1";

	private static final String TAG_START = "3DI";
	private static final int FOOT_SIZE = 10;
	private static final int NEW_MAJOR_VERSION = 3; // So winamp will accept it...
	private static final int NEW_MINOR_VERSION = 0;

	private boolean footerExists = false;
	private int majorVersion = NEW_MAJOR_VERSION;
	private int minorVersion = NEW_MINOR_VERSION;
	private boolean unsynchronisation = false;
	private boolean extended = false;
	private boolean experimental = false;
	private boolean footer = false;
	private int tagSize = 0;

	/**
	 * Creates and id3v2 footer.  This is almost identical to an id3v2 header
	 * but is placed at the end of the tag and is optional.  It should only
	 * be used when tags are appended.  An attempt will be made to read from
	 * the file provided from the location provided.
	 *
	 * @param mp3 the file to read from
	 * @param location the location to find the footer
	 * @exception FileNotFoundException if an error occurs
	 * @exception IOException if an error occurs
	 */
	public ID3v2Footer(RandomAccessInputStream raf, int location) throws IOException
	{
		footerExists = checkFooter(raf, location);
		if (footerExists) readFooter(raf, location);
	}

	/**
	 * Checks to see if there is an id3v2 footer in the file provided to the
	 * constructor.
	 *
	 * @param location where the footer should be located in the file
	 * @return true if an id3v2 footer exists in the file
	 * @exception FileNotFoundException if an error occurs
	 * @exception IOException if an error occurs
	 */
	private boolean checkFooter(RandomAccessInputStream raf, int location) throws IOException
	{
		raf.seek(location);
		byte[] buf = new byte[FOOT_SIZE];

		if (raf.read(buf) != FOOT_SIZE)
		{
			throw new IOException("Error encountered finding id3v2 footer");
		}

		String result = new String(buf, ENC_TYPE);
		if (result.substring(0, TAG_START.length()).equals(TAG_START))
		{
			if ((((int)buf[3]&0xFF) != 0xff) && (((int)buf[4]&0xFF) != 0xff))
			{
				if (((int)buf[6]&0x80)==0 && ((int)buf[7]&0x80)==0 && ((int)buf[8]&0x80)==0 && ((int)buf[9]&0x80)==0)
				{
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Extracts the information from the footer.
	 *
	 * @param location where the footer is in the file
	 * @exception FileNotFoundException if an error occurs
	 * @exception IOException if an error occurs
	 */
	private void readFooter(RandomAccessInputStream raf, int location) throws IOException
	{
		raf.seek(location);
		byte[] foot = new byte[FOOT_SIZE];

		if (raf.read(foot) != FOOT_SIZE)
		{
			throw new IOException("Error encountered reading id3v2 footer");
		}

		majorVersion = (int) foot[3];

		if (majorVersion <= NEW_MAJOR_VERSION)
		{
			minorVersion = (int) foot[4];
			unsynchronisation = (foot[5]&0x80)!=0;
			extended = (foot[5]&0x40)!=0;
			experimental = (foot[5]&0x20)!=0;
			footer = (foot[5]&0x10)!=0;
			tagSize = ID3v2Tag.convertDWordToInt(foot, 6);
		}
	}

	/**
	 * Return an array of bytes representing the footer.  This can be used
	 * to easily write the footer to a file.
	 *
	 * @return a binary representation of this footer
	 */
	public byte[] getBytes()
	{
		byte[] b = new byte[FOOT_SIZE];
		int bytesCopied = 0;

		System.arraycopy(Helpers.getBytesFromString(TAG_START, TAG_START.length(), ENC_TYPE), 0, b, 0, TAG_START.length());
		bytesCopied += TAG_START.length();
		b[bytesCopied++] = (byte) majorVersion;
		b[bytesCopied++] = (byte) minorVersion;
		b[bytesCopied++] = getFlagByte();
		System.arraycopy(ID3v2Tag.convertIntToDWord(tagSize), 0, b, bytesCopied, 4);
		bytesCopied += 4;

		return b;
	}

	/**
	 * A helper function for the getBytes function that returns a byte with
	 * the proper flags set.
	 *
	 * @return the flags byte of this footer
	 */
	private byte getFlagByte()
	{
		byte ret = 0;

		if (unsynchronisation) ret |= 0x80;
		if (extended) ret |= 0x40;
		if (experimental) ret |= 0x20;
		if (footer) ret |= 0x10;

		return ret;
	}

	/**
	 * Returns true if a footer exists
	 *
	 * @return true if a footer exists
	 */
	public boolean footerExists()
	{
		return footerExists;
	}

	/**
	 * Returns the size (in bytes) of this footer.  This is 10 if the footer
	 * exists and 0 otherwise
	 *
	 * @return the size of this footer
	 */
	public int getFooterSize()
	{
		if (footerExists)
			return FOOT_SIZE;
		return 0;
	}

	/**
	 * Returns the size (in bytes) of the frames and/or extended footer portion
	 * of the id3v2 tag according to the size field in the footer.
	 *
	 * @return the size field of the footer
	 */
	public int getTagSize()
	{
		return tagSize;
	}

	/**
	 * Sets the size of the frames and/or extended footer.  If this function
	 * is called, the footerExists function will return true.  This is called
	 * every time a frame is updated, added, or removed.
	 *
	 * @param size a value of type 'int'
	 */
	public void setTagSize(int size)
	{
		if (size > 0)
		{
			tagSize = size;
			footerExists = true;
		}
	}

	/**
	 * Returns the major version of this id3v2 tag.
	 *
	 * @return the major version of this id3v2 tag.
	 */
	public int getMajorVersion()
	{
		return majorVersion;
	}

	/**
	 * Return the minor version/revision of this id3v2 tag.
	 *
	 * @return the minor version/revision of this id3v2 tag.
	 */
	public int getMinorVersion()
	{
		return minorVersion;
	}

	/**
	 * Returns true if the unsynchronisation bit is set in this footer.
	 *
	 * @return true if the unsynchronisation bit is set in this footer.
	 */
	public boolean getUnsynchronisation()
	{
		return unsynchronisation;
	}

	/**
	 * Set the unsynchronisation flag for this footer.
	 *
	 * @param unsynch the new value of the unsynchronisation flag
	 */
	public void setUnsynchronisation(boolean unsynch)
	{
		unsynchronisation = unsynch;
	}

	/**
	 * Returns true if this tag has an extended footer.
	 *
	 * @return true if this tag has an extended footer
	 */
	public boolean getExtendedFooter()
	{
		return extended;
	}

	/**
	 * Set the value of the extended footer bit of this footer.
	 *
	 * @param extend the new value of the extended footer bit
	 */
	public void setExtendedFooter(boolean extend)
	{
		extended = extend;
	}

	/**
	 * Returns true if the experimental bit of this footer is set.
	 *
	 * @return true if the experimental bit of this footer is set
	 */
	public boolean getExperimental()
	{
		return experimental;
	}

	/**
	 * Set the value of the experimental bit of this footer.
	 *
	 * @param experiment the new value of the experimental bit
	 */
	public void setExperimental(boolean experiment)
	{
		experimental = experiment;
	}

	/**
	 * Returns true if this tag has a footer.
	 *
	 * @return true if this tag has a footer
	 */
	public boolean getFooter()
	{
		return footer;
	}

	/**
	 * Sets the value of the footer bit for this footer.
	 *
	 * @param foot the new value of the footer bit for this footer
	 */
	public void setFooter(boolean foot)
	{
		footer = foot;
	}

	/**
	 * Return a string representation of this object.  Contains all information
	 * contained within.
	 *
	 * @return a string representation of this object
	 */
	public String toString()
	{
		return "ID3v2." + getMajorVersion() + "." + getMinorVersion() + "\n" + "TagSize:\t\t\t" + getTagSize() + " bytes\nUnsynchronisation:\t\t" + getUnsynchronisation() + "\nExtended Footer:\t\t" + getExtendedFooter() + "\nExperimental:\t\t\t"
				+ getExperimental() + "\nFooter:\t\t\t\t" + getFooter();
	}

}
