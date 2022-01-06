/*
 * @(#) ID3v2Header.java
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
 *  This class reads all the information in the header of an id3v2 tag.
 *
 * @author:  Jonathan Hilliker modified by Daniel Becker
 */
public class ID3v2Header
{
	private static final String ENC_TYPE = "ISO-8859-1";

	private static final String TAG_START = "ID3";
	private static final int HEAD_SIZE = 10;
	private static final int HEAD_LOCATION = 0;
	private static final int NEW_MAJOR_VERSION = 3; // So winamp will accept it...
	private static final int NEW_MINOR_VERSION = 0;

	private boolean headerExists = false;
	private int majorVersion = NEW_MAJOR_VERSION;
	private int minorVersion = NEW_MINOR_VERSION;
	private boolean unsynchronisation = false;
	private boolean extended = false;
	private boolean experimental = false;
	private boolean footer = false;
	private int tagSize = 0;

	/**
	 * Create an id3v2header linked to the file passed as a parameter.  An
	 * attempt will be made to read the header from the file.  If a header
	 * exists, then information in the header will be extracted.  If a header
	 * doesn't exist, default data will be used.
	 *
	 * @param mp3 the file to attempt to read data from
	 * @exception FileNotFoundException if an error occurs
	 * @exception IOException if an error occurs
	 */
	public ID3v2Header(RandomAccessInputStream raf) throws IOException
	{
		headerExists = checkHeader(raf);
		if (headerExists) readHeader(raf);
	}

	/**
	 * Checks to see if there is an id3v2 header in the file provided to the
	 * constructor.
	 *
	 * @return true if an id3v2 header exists in the file
	 * @exception FileNotFoundException if an error occurs
	 * @exception IOException if an error occurs
	 */
	private boolean checkHeader(RandomAccessInputStream raf) throws IOException
	{
		raf.seek(HEAD_LOCATION);

		byte[] buf = new byte[HEAD_SIZE];
		if (raf.read(buf) != HEAD_SIZE)
		{
			throw new IOException("Error encountered finding id3v2 header");
		}

		String result = new String(buf, ENC_TYPE);
		if (result.substring(0, TAG_START.length()).equals(TAG_START))
		{
			if ((((int)buf[3]&0xFF) < 0xff) && (((int)buf[4]&0xFF) < 0xff))
			{
				if ((((int)buf[6]&0xFF) < 0x80) && (((int)buf[7]&0xFF) < 0x80) && (((int)buf[8]&0xFF) < 0x80) && (((int)buf[9]&0xFF) < 0x80))
				{
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Extracts the information from the header.
	 *
	 * @exception FileNotFoundException if an error occurs
	 * @exception IOException if an error occurs
	 */
	private void readHeader(RandomAccessInputStream raf) throws IOException
	{
		raf.seek(HEAD_LOCATION);

		byte[] head = new byte[HEAD_SIZE];
		if (raf.read(head) != HEAD_SIZE)
		{
			throw new IOException("Error encountered reading id3v2 header");
		}

		majorVersion = (int) head[3];

		if (majorVersion <= NEW_MAJOR_VERSION)
		{
			minorVersion = (int) head[4];
			unsynchronisation = (head[5]&0x80)!=0;
			extended = (head[5]&0x40)!=0;
			experimental = (head[5]&0x20)!=0;
			footer = (head[5]&0x10)!=0;
			tagSize = ID3v2Tag.convertDWordToInt(head, 6);
		}
	}

	/**
	 * Return an array of bytes representing the header.  This can be used
	 * to easily write the header to a file.
	 *
	 * @return a binary representation of this header
	 */
	public byte[] getBytes()
	{
		byte[] b = new byte[HEAD_SIZE];
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
	 * @return the flags byte of this header
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
	 * Returns true if a header exists
	 *
	 * @return true if a header exists
	 */
	public boolean headerExists()
	{
		return headerExists;
	}

	/**
	 * Returns the size (in bytes) of this header.  This is 10 if the header
	 * exists and 0 otherwise
	 *
	 * @return the size of this header
	 */
	public int getHeaderSize()
	{
		if (headerExists)
			return HEAD_SIZE;
		else
			return 0;
	}

	/**
	 * Returns the size (in bytes) of the frames and/or extended header portion
	 * of the id3v2 tag according to the size field in the header.
	 *
	 * @return the size field of the header
	 */
	public int getTagSize()
	{
		return tagSize;
	}

	/**
	 * Sets the size of the frames and/or extended header.  If this function
	 * is called, the headerExists function will return true.  This is called
	 * every time a frame is updated, added, or removed.
	 *
	 * @param size a value of type 'int'
	 */
	public void setTagSize(int size)
	{
		if (size > 0)
		{
			tagSize = size;
			headerExists = true;
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
	 * Returns true if the unsynchronisation bit is set in this header.
	 *
	 * @return true if the unsynchronisation bit is set in this header.
	 */
	public boolean getUnsynchronisation()
	{
		return unsynchronisation;
	}

	/**
	 * Set the unsynchronisation flag for this header.
	 *
	 * @param unsynch the new value of the unsynchronisation flag
	 */
	public void setUnsynchronisation(boolean unsynch)
	{
		unsynchronisation = unsynch;
	}

	/**
	 * Returns true if this tag has an extended header.
	 *
	 * @return true if this tag has an extended header
	 */
	public boolean getExtendedHeader()
	{
		return extended;
	}

	/**
	 * Set the value of the extended header bit of this header.
	 *
	 * @param extend the new value of the extended header bit
	 */
	public void setExtendedHeader(boolean extend)
	{
		extended = extend;
	}

	/**
	 * Returns true if the experimental bit of this header is set.
	 *
	 * @return true if the experimental bit of this header is set
	 */
	public boolean getExperimental()
	{
		return experimental;
	}

	/**
	 * Set the value of the experimental bit of this header.
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
	 * Sets the value of the footer bit for this header.
	 *
	 * @param foot the new value of the footer bit for this header
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
		return "ID3v2." + getMajorVersion() + "." + getMinorVersion() + "\n" + "TagSize:\t\t\t" + getTagSize() + " bytes\nUnsynchronisation:\t\t" + getUnsynchronisation() + "\nExtended Header:\t\t" + getExtendedHeader() + "\nExperimental:\t\t\t"
				+ getExperimental() + "\nFooter:\t\t\t\t" + getFooter();
	}

}
