/*
 * @(#) ID3v2Frame.java
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

import de.quippy.javamod.multimedia.mp3.id3.exceptions.ID3v2FormatException;
import de.quippy.javamod.system.Helpers;

/**
 * Description: 
 *  This class holds the information found in an id3v2 frame.  At this point at
 *  least, the information in the tag header is read-only and can only be set
 *  set in the constructor.  This is for simplicity's sake.  This object 
 *  doesn't automatically unsynchronise, encrypt, or compress the data.
 *
 * @author:  Jonathan Hilliker modified by Daniel Becker
 */
public class ID3v2Frame
{

	private static final int FRAME_HEAD_SIZE = 10;
	private static final int FRAME_FLAGS_SIZE = 2;
	private static final int MAX_EXTRA_DATA = 5;
	private static final String[] ENC_TYPES =
	{
		"ISO-8859-1", "UTF16", "UTF-16BE", "UTF-8"
	};

	private String id = null;
	private boolean tagAlterDiscard = false;
	private boolean fileAlterDiscard = false;
	private boolean readOnly = false;
	private boolean grouped = false;
	private boolean compressed = false;
	private boolean encrypted = false;
	private boolean unsynchronised = false;
	private boolean lengthIndicator = false;
	private byte group = 0;
	private byte encrType = 0;
	private int dataLength = -1;
	private byte[] frameData;

	/**
	 * Create and ID3v2 frame with the specified id and data.  All the flag
	 * bits are set to false.
	 *
	 * @param id the id of this frame
	 * @param data the data for this frame
	 */
	public ID3v2Frame(String id, byte[] data)
	{
		this.id = id;

		fileAlterDiscard = checkDefaultFileAlterDiscard();
		parseData(data);
	}

	/**
	 * Create an ID3v2Frame with a specified id, a byte array containing
	 * the frame header flags, and a byte array containing the data for this
	 * frame.
	 *
	 * @param id the id of this frame
	 * @param flags the flags found in the header of the frame (2 bytes)
	 * @param data the data found in this frame
	 * @exception ID3v2FormatException if an error occurs
	 */
	public ID3v2Frame(String id, byte[] flags, byte[] data) throws ID3v2FormatException
	{
		this(id, data);

		parseFlags(flags);
	}

	/**
	 * Create an ID3v2Frame with the specified id, data, and flags set.  It
	 * is expected that the corresponing data for the flags that require 
	 * extra data is found in the data array in the standard place.
	 *
	 * @param id the id for this frame
	 * @param data the data for this frame
	 * @param tagAlterDiscard the tag alter preservation flag
	 * @param fileAlterDiscard the file alter preservation flag
	 * @param readOnly the read only flag
	 * @param grouped the grouping identity flag
	 * @param compressed the compression flag
	 * @param encrypted the encryption flag
	 * @param unsynchronised the unsynchronisation flag
	 * @param lengthIndicator the data length indicator flag
	 */
	public ID3v2Frame(String id, byte[] data, boolean tagAlterDiscard, boolean fileAlterDiscard, boolean readOnly, boolean grouped, boolean compressed, boolean encrypted, boolean unsynchronised, boolean lengthIndicator)
	{

		this.id = id;
		this.tagAlterDiscard = tagAlterDiscard;
		this.fileAlterDiscard = fileAlterDiscard;
		this.readOnly = readOnly;
		this.grouped = grouped;
		this.compressed = compressed;
		this.encrypted = encrypted;
		this.unsynchronised = unsynchronised;
		this.lengthIndicator = lengthIndicator;

		group = 0;
		encrType = 0;
		dataLength = -1;

		parseData(data);
	}

	/**
	 * Returns true if this frame should have the file alter 
	 * preservation bit set by default.
	 *
	 * @return true if the file alter preservation should be set by default
	 */
	private boolean checkDefaultFileAlterDiscard()
	{
		return	id.equals(ID3v2Frames.AUDIO_SEEK_POINT_INDEX) || 
				id.equals(ID3v2Frames.AUDIO_ENCRYPTION) || 
				id.equals(ID3v2Frames.EVENT_TIMING_CODES) || 
				id.equals(ID3v2Frames.EQUALISATION) || 
				id.equals(ID3v2Frames.MPEG_LOCATION_LOOKUP_TABLE) || 
				id.equals(ID3v2Frames.POSITION_SYNCHRONISATION_FRAME) || 
				id.equals(ID3v2Frames.SEEK_FRAME) || 
				id.equals(ID3v2Frames.SYNCHRONISED_LYRIC) || 
				id.equals(ID3v2Frames.SYNCHRONISED_TEMPO_CODES) || 
				id.equals(ID3v2Frames.RELATIVE_VOLUME_ADJUSTMENT) || 
				id.equals(ID3v2Frames.ENCODED_BY) || 
				id.equals(ID3v2Frames.LENGTH);
	}

	/**
	 * Read the information from the flags array.
	 *
	 * @param flags the flags found in the frame header
	 * @exception ID3v2FormatException if an error occurs
	 */
	private void parseFlags(byte[] flags) throws ID3v2FormatException
	{
		if (flags.length != FRAME_FLAGS_SIZE)
		{
			throw new ID3v2FormatException("Error parsing flags of frame: " + id + ".  Expected 2 bytes.");
		}
		else
		{
			tagAlterDiscard = (flags[0]&0x40)!=0;
			fileAlterDiscard = (flags[0]&0x20)!=0;
			readOnly = (flags[0]&0x10)!=0;
			grouped = (flags[1]&0x40)!=0;
			compressed = (flags[1]&0x08)!=0;
			encrypted = (flags[1]&0x04)!=0;
			unsynchronised = (flags[1]&0x02)!=0;
			lengthIndicator = (flags[1]&0x01)!=0;

			if (compressed && !lengthIndicator)
			{
				throw new ID3v2FormatException("Error parsing flags of frame: " + id + ".  Compressed bit set without data length bit set.");
			}
		}
	}

	/**
	 * A helper function for the getFrameBytes method that processes the info
	 * in the frame and returns the 2 byte array of flags to be added to the 
	 * header.
	 *
	 * @return a value of type 'byte[]'
	 */
	private byte[] getFlagBytes()
	{
		byte flags[] = new byte[2];

		if (tagAlterDiscard) flags[0] |= 0x40;
		if (fileAlterDiscard) flags[0] |= 0x20;
		if (readOnly) flags[0] |= 0x10;
		if (grouped) flags[1] |= 0x40;
		if (compressed) flags[1] |= 0x08;
		if (encrypted) flags[1] |= 0x04;
		if (unsynchronised) flags[1] |= 0x02;
		if (lengthIndicator) flags[1] |= 0x01;

		return flags;
	}

	/**
	 * Pulls out extra information inserted in the frame data depending 
	 * on what flags are set.
	 *
	 * @param data the frame data
	 */
	private void parseData(byte[] data)
	{
		int bytesRead = 0;

		if (grouped)
		{
			group = data[bytesRead];
			bytesRead ++;
		}
		if (encrypted)
		{
			encrType = data[bytesRead];
			bytesRead ++;
		}
		if (lengthIndicator)
		{
			dataLength = ID3v2Tag.convertDWordToInt(data, bytesRead);
			bytesRead += 4;
		}

		frameData = new byte[data.length - bytesRead];
		System.arraycopy(data, bytesRead, frameData, 0, frameData.length);
	}

	/**
	 * Returns the data for this frame
	 *
	 * @return the data for this frame
	 */
	public byte[] getFrameData()
	{
		return frameData;
	}

	/**
	 * Set the data for this frame.  This does nothing if this frame is read
	 * only.
	 *
	 * @param newData a byte array contianing the new data
	 */
	public void setFrameData(byte[] newData)
	{
		if (!readOnly)
		{
			frameData = newData;
		}
	}

	/**
	 * Return the length of this frame in bytes, including the header.
	 *
	 * @return the length of this frame
	 */
	public int getFrameLength()
	{
		int length = frameData.length + FRAME_HEAD_SIZE;

		if (grouped)
		{
			length++;
		}
		if (encrypted)
		{
			length++;
		}
		if (lengthIndicator)
		{
			length += 4;
		}

		return length;
	}

	/**
	 * Returns a byte array representation of this frame that can be written
	 * to a file.  Includes the header and data.
	 *
	 * @return a binary representation of this frame to be written to a file
	 */
	public byte[] getFrameBytes()
	{
		int length = getFrameLength();
		int bytesWritten = 0;
		byte[] flags = getFlagBytes();
		byte[] extra = getExtraDataBytes();
		byte[] b;

		b = new byte[length];

		System.arraycopy(id.getBytes(), 0, b, 0, id.length());
		bytesWritten += id.length();
		System.arraycopy(ID3v2Tag.convertIntToDWord(length), 0, b, bytesWritten, 4);
		bytesWritten += 4;
		System.arraycopy(flags, 0, b, bytesWritten, flags.length);
		bytesWritten += flags.length;
		System.arraycopy(extra, 0, b, bytesWritten, extra.length);
		bytesWritten += extra.length;
		System.arraycopy(frameData, 0, b, bytesWritten, frameData.length);
		bytesWritten += frameData.length;

		return b;
	}

	/**
	 * A helper function for the getFrameBytes function that returns an array 
	 * of all the data contained in any extra fields that may be present in
	 * this frame.  This includes the group, the encryption type, and the 
	 * length indicator.  The length of the array returned is variable length.
	 *
	 * @return an array of bytes containing the extra data fields in the frame
	 */
	private byte[] getExtraDataBytes()
	{
		byte[] buf = new byte[MAX_EXTRA_DATA];
		byte[] ret;
		int bytesCopied = 0;

		if (grouped)
		{
			buf[bytesCopied] = group;
			bytesCopied++;
		}
		if (encrypted)
		{
			buf[bytesCopied] = encrType;
			bytesCopied++;
		}
		if (lengthIndicator)
		{
			System.arraycopy(ID3v2Tag.convertIntToDWord(dataLength), 0, buf, bytesCopied, 4);
			bytesCopied += 4;
		}

		ret = new byte[bytesCopied];
		System.arraycopy(buf, 0, ret, 0, bytesCopied);

		return ret;
	}

	/**
	 * Returns true if the tag alter preservation bit has been set.  If set
	 * then the frame should be discarded if it is altered and the id is
	 * unknown.
	 *
	 * @return true if the tag alter preservation bit has been set
	 */
	public boolean getTagAlterDiscard()
	{
		return tagAlterDiscard;
	}

	/**
	 * Returns true if the file alter preservation bit has been set.  If set
	 * then the frame should be discarded if the file is altered and the id
	 * is unknown.
	 *
	 * @return true if the file alter preservation bit has been set
	 */
	public boolean getFileAlterDiscard()
	{
		return fileAlterDiscard;
	}

	/**
	 * Returns true if this frame is read only
	 *
	 * @return true if this frame is read only
	 */
	public boolean getReadOnly()
	{
		return readOnly;
	}

	/**
	 * Returns true if this frame is a part of a group
	 *
	 * @return true if this frame is a part of a group
	 */
	public boolean getGrouped()
	{
		return grouped;
	}

	/**
	 * Returns true if this frame is compressed
	 *
	 * @return true if this frame is compressed
	 */
	public boolean getCompressed()
	{
		return compressed;
	}

	/**
	 * Returns true if this frame is encrypted
	 *
	 * @return true if this frame is encrypted
	 */
	public boolean getEncrypted()
	{
		return encrypted;
	}

	/**
	 * Returns true if this frame is unsynchronised
	 *
	 * @return true if this frame is unsynchronised
	 */
	public boolean getUnsynchronised()
	{
		return unsynchronised;
	}

	/**
	 * Returns true if this frame has a length indicator added
	 *
	 * @return true if this frame has a length indicator added
	 */
	public boolean getLengthIndicator()
	{
		return lengthIndicator;
	}

	/**
	 * Returns the group identifier if added.  Otherwise the null byte is 
	 * returned.
	 *
	 * @return the groupd identifier if added, null byte otherwise
	 */
	public byte getGroup()
	{
		return group;
	}

	/**
	 * If encrypted, this returns the encryption method byte.  If it is not
	 * encrypted, the null byte is returned.
	 *
	 * @return the encryption method if set and the null byte if not
	 */
	public byte getEncryptionType()
	{
		return encrType;
	}

	/**
	 * If a length indicator has been added, the length of the data is 
	 * returned.  Otherwise -1 is returned.
	 *
	 * @return the length of the data if a length indicator is present or -1
	 */
	public int getDataLength()
	{
		return dataLength;
	}

	/**
	 * If possible, this method attempts to convert textual part of the data
	 * into a string.  If this frame does not contain textual information, 
	 * an empty string is returned.
	 *
	 * @return the textual portion of the data in this frame
	 * @exception ID3v2FormatException if an error occurs
	 */
	public String getDataString() throws ID3v2FormatException
	{
		String str = null;

		if (frameData.length > 1)
		{
			final String enc_type = (frameData[0] < ENC_TYPES.length)?ENC_TYPES[frameData[0]]:ENC_TYPES[0];
			try
			{
				if ((id.charAt(0) == 'T') || id.equals(ID3v2Frames.OWNERSHIP_FRAME))
				{
					str = Helpers.retrieveAsString(frameData, 1, frameData.length-1, enc_type);
				}
				else 
				if (id.charAt(0) == 'W')
				{
					str = Helpers.retrieveAsString(frameData, 1, frameData.length-1, ENC_TYPES[0]);
				}
				else 
				if (id.equals(ID3v2Frames.USER_DEFINED_URL))
				{
					str = Helpers.retrieveAsString(frameData, 1, frameData.length-1, enc_type);
					if (str.length()<frameData.length-1)
					{
						str += '\n';
						str += Helpers.retrieveAsString(frameData, str.length()+2, frameData.length-2-str.length(), enc_type);
					}
				}
				else 
				if (id.equals(ID3v2Frames.UNSYNCHRONISED_LYRIC_TRANSCRIPTION) || id.equals(ID3v2Frames.COMMENTS) || id.equals(ID3v2Frames.TERMS_OF_USE))
				{
					str = Helpers.retrieveAsString(frameData, 4, frameData.length-4, enc_type);
				}
			}
			catch (Throwable e)
			{
				throw new ID3v2FormatException("Frame " + id + " had errors!", e);
			}
		}

		return str;
	}

	/**
	 * Return a string representation of this object that contains all the 
	 * information contained within it.
	 *
	 * @return a string representation of this object
	 */
	public String toString()
	{
		try
		{
			return id + "\nTagAlterDiscard:\t\t" + tagAlterDiscard + "\nFileAlterDiscard:\t\t" + fileAlterDiscard + "\nReadOnly:\t\t\t" + readOnly + "\nGrouped:\t\t\t" + grouped + "\nCompressed:\t\t\t" + compressed + "\nEncrypted:\t\t\t" + encrypted
					+ "\nUnsynchronised:\t\t\t" + unsynchronised + "\nLengthIndicator:\t\t" + lengthIndicator + "\nData:\t\t\t\t" + getDataString().toString();
		}
		catch (Exception e)
		{
			return e.getMessage();
		}
	}

}
