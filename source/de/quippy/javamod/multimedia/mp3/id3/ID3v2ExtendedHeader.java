/*
 * @(#) ID3v2ExtendedHeader.java
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
import de.quippy.javamod.multimedia.mp3.id3.exceptions.ID3v2FormatException;

/**
 * Description: 
 *  If the id3v2 tag has an extended header, this class will read/write the 
 *  information contained within it.  NOTE: this class is untested and has
 *  no mutators.  In other words, this class will only be used if an mp3
 *  already has an extended header (at this point at least).
 *
 * @author:  Jonathan Hilliker modified by Daniel Becker
 */
public class ID3v2ExtendedHeader
{
	private static final int EXT_HEAD_LOCATION = 10;
	private static final int MIN_SIZE = 6;
	private static final int CRC_SIZE = 5;
	private static final int[] MAX_TAG_FRAMES_TABLE =
	{
			128, 64, 32, 32
	};
	private static final int[] MAX_TAG_SIZE_TABLE =
	{
			8000000, 1024000, 320000, 32000
	};
	private static final int[] MAX_TEXT_SIZE_TABLE =
	{
			-1, 1024, 128, 30
	};

	private int size = 0;
	private int numFlagBytes = 0;
	private boolean update = false;
	private boolean crced = false;
	private byte[] crc = new byte[CRC_SIZE];
	//private int maxFrames = -1;
	private int maxTagSize = -1;
	private boolean textEncode = false;
	private int maxTextSize = -1;
	private boolean imageEncode = false;
	private int imageRestrict = -1;

	/**
	 * Create an extended header object from the file passed.  Information
	 * in the file's extended header will be read and stored.
	 *
	 * @param mp3 the file to read/write to
	 * @exception FileNotFoundException if an error occurs
	 * @exception IOException if an error occurs
	 * @exception ID3v2FormatException if an error occurs
	 */
	public ID3v2ExtendedHeader(RandomAccessInputStream raf) throws IOException, ID3v2FormatException
	{
		readExtendedHeader(raf);
	}

	/**
	 * Read the information in the file's extended header
	 *
	 * @exception FileNotFoundException if an error occurs
	 * @exception IOException if an error occurs
	 * @exception ID3v2FormatException if an error occurs
	 */
	private void readExtendedHeader(RandomAccessInputStream raf) throws IOException, ID3v2FormatException
	{
		raf.seek(EXT_HEAD_LOCATION);

		byte[] buf = new byte[4];
		if (raf.read(buf) != buf.length)
		{
			throw new IOException("Error reading extended header:size");
		}

		size = ID3v2Tag.convertDWordToInt(buf, 0);
		if (size < MIN_SIZE)
		{
			throw new ID3v2FormatException("The extended header size data is less than the minimum required size (" + size + '<' + MIN_SIZE + ").");
		}

		numFlagBytes = raf.read();
		buf = new byte[numFlagBytes + 1];
		if (raf.read(buf) != buf.length)
		{
			throw new IOException("Error reading extended header:flags");
		}

		parseFlags(buf);
	}

	/**
	 * Parse the extended header flag bytes
	 *
	 * @param flags the array of extended flags
	 * @exception ID3v2FormatException if an error occurs
	 */
	private void parseFlags(byte[] flags) throws ID3v2FormatException
	{
		int bytesRead = 1;

		update = (flags[0]&0x80)!=0;
		if (update) bytesRead++;
		
		crced = (flags[0]&0x40)!=0;
		if (crced)
		{
			bytesRead++;
			for (int i = 0; i < crc.length; i++) crc[i] = flags[bytesRead++];
		}
		
		if ((flags[0]&0x80)!=0)
		{
			bytesRead++;
			byte b = flags[bytesRead];
			maxTagSize = (b&0xC0)>>6;
			textEncode = (b&0x20)!=0;
			maxTextSize = (b&0x18)>>3;
			imageEncode = (b&0x04)!=0;
			imageRestrict = (b&0x03);
			bytesRead++;
		}
		if (bytesRead != numFlagBytes)
		{
			throw new ID3v2FormatException("The number of found flag bytes in the extended header is not equal to the number specified in the extended header.");
		}
	}
	/**
	 * A helper function for the getBytes method that returns a byte array
	 * representing the extended flags field of the extended header.
	 *
	 * @return the extended flags field of the extended header
	 */
	private byte[] getFlagBytes()
	{
		byte[] b = new byte[numFlagBytes];
		int bytesCopied = 1;
		b[0] = 0;

		if (update)
		{
			b[0] |= 0x80;
			b[bytesCopied++] = 0;
		}
		if (crced)
		{
			b[0] |= 0x40;
			b[bytesCopied++] = (byte) crc.length;
			System.arraycopy(crc, 0, b, bytesCopied, crc.length);
			bytesCopied += crc.length;
		}
		if ((maxTagSize != -1) || textEncode || (maxTextSize != -1) || imageEncode || (imageRestrict != -1))
		{
			b[0] |= 0x20;
			b[bytesCopied++] = 0x01;
			byte restrict = 0;
			if (maxTagSize != -1) restrict |= (byte)((maxTagSize&0x3)<<6);
			if (textEncode) restrict |= 0x20;
			if (maxTextSize != -1) restrict |= (byte)((maxTextSize&0x3)<<3);
			if (imageEncode) restrict |= 0x04;
			if (imageRestrict != -1) restrict |= (byte)(imageRestrict&0x3);
			b[bytesCopied++] = restrict;
		}

		return b;
	}

	/**
	 * Return an array of bytes representing this extended header in the 
	 * standard format to be written to a file.
	 *
	 * @return a binary represenation of this extended header
	 */
	public byte[] getBytes()
	{
		byte[] b = new byte[size];
		int bytesCopied = 0;

		System.arraycopy(ID3v2Tag.convertIntToDWord(size), 0, b, bytesCopied, 4);
		bytesCopied += 4;
		b[bytesCopied++] = (byte) numFlagBytes;
		System.arraycopy(getFlagBytes(), 0, b, bytesCopied, numFlagBytes);
		bytesCopied += numFlagBytes;

		return b;
	}

	/**
	 * Returns the size of the extended header
	 *
	 * @return the size of the extended header
	 */
	public int getSize()
	{
		return size;
	}

	/**
	 * Returns the number of extended flag bytes
	 *
	 * @return the number of extended flag bytes
	 */
	public int getNumFlagBytes()
	{
		return numFlagBytes;
	}

	/**
	 * Returns the maximum number of frames if set.  If unset, returns -1
	 *
	 * @return the maximum number of frames or -1 if unset
	 */
	public int getMaxFrames()
	{
		int retval = -1;

		if ((maxTagSize >= 0) && (maxTagSize < MAX_TAG_FRAMES_TABLE.length))
		{
			retval = MAX_TAG_FRAMES_TABLE[maxTagSize];
		}

		return retval;
	}

	/**
	 * Returns the maximum tag size or -1 if unset
	 *
	 * @return the maximum tag size or -1 if unset
	 */
	public int getMaxTagSize()
	{
		int retval = -1;

		if ((maxTagSize >= 0) && (maxTagSize < MAX_TAG_SIZE_TABLE.length))
		{
			retval = MAX_TAG_SIZE_TABLE[maxTagSize];
		}

		return retval;
	}

	/**
	 * Returns true if the text encode flag is set
	 *
	 * @return true if the text encode flag is set
	 */
	public boolean getTextEncode()
	{
		return textEncode;
	}

	/**
	 * Returns the maximum length of a string if set or -1
	 *
	 * @return the maximum length of a string if set or -1
	 */
	public int getMaxTextSize()
	{
		int retval = -1;

		if ((maxTextSize >= 0) && (maxTextSize < MAX_TEXT_SIZE_TABLE.length))
		{
			retval = MAX_TEXT_SIZE_TABLE[maxTextSize];
		}

		return retval;
	}

	/**
	 * Returns true if the image encode flag is set
	 *
	 * @return true if the image encode flag is set
	 */
	public boolean getImageEncode()
	{
		return imageEncode;
	}

	/**
	 * Returns the value of the image restriction field or -1 if not set
	 *
	 * @return the value of the image restriction field or -1 if not set
	 */
	public int getImageRestriction()
	{
		return imageRestrict;
	}

	/**
	 * Returns true if this tag is an update of a previous tag
	 *
	 * @return true if this tag is an update of a previous tag
	 */
	public boolean getUpdate()
	{
		return update;
	}

	/**
	 * Returns true if CRC information is provided for this tag
	 *
	 * @return true if CRC information is provided for this tag
	 */
	public boolean getCRCed()
	{
		return crced;
	}

	/**
	 * If there is crc data in the extended header, then the attached 5 byte
	 * crc will be returned.  An empty array will be returned if this has
	 * not been set.
	 *
	 * @return the attached crc data if there is any
	 */
	public byte[] getCRC()
	{
		return crc;
	}

	/**
	 * Returns a string representation of this object that contains all
	 * information within.
	 *
	 * @return a string representation of this object
	 */
	public String toString()
	{
		return "ExtendedSize:\t\t\t" + getSize() + " bytes" + "\nNumFlagBytes:\t\t\t" + getNumFlagBytes() + "\nUpdated:\t\t\t" + getUpdate() + "\nCRC:\t\t\t\t" + getCRCed() + "\nMaxFrames:\t\t\t" + getMaxFrames() + "\nMaxTagSize:\t\t\t" + getMaxTagSize()
				+ "\nTextEncoded:\t\t\t" + getTextEncode() + "\nMaxTextSize:\t\t\t" + getMaxTextSize() + "\nImageEncoded:\t\t\t" + getImageEncode() + "\nImageRestriction:\t\t" + getImageRestriction();
	}

} // ID3v2ExtendedHeader
