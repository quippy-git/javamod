/*
 * @(#) ID3v2Tag.java
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
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;

import de.quippy.javamod.io.RandomAccessInputStream;
import de.quippy.javamod.multimedia.mp3.id3.exceptions.ID3v2FormatException;
import de.quippy.javamod.system.Log;

/**
 * Description: 
 *  This class reads and writes id3v2 tags from a file.
 *
 * @author:  Jonathan Hilliker modified by Daniel Becker
 */
public class ID3v2Tag
{
	private static final String ENC_TYPE = "ISO-8859-1";

	private ID3v2Header head = null;
	private ID3v2ExtendedHeader ext_head = null;
	private ID3v2Frames<String, ID3v2Frame> frames = null;
	private ID3v2Footer foot = null;
	private int padding;
	private int origSize;
	private int origPadding;
	private boolean exists;

	/**
	 * Create an id3v2 tag bound to the file provided as a parameter.  If
	 * a tag exists in the file already, then all the information in the tag
	 * will be extracted.  If a tag doesn't exist, then this is the file that
	 * will be written to when the writeTag method is called.
	 *
	 * @param mp3 the file to write/read the the tag information to/from
	 * @exception FileNotFoundException if an error occurs
	 * @exception IOException if an error occurs
	 * @exception ID3v2FormatException if an exisiting id3v2 tag isn't correct
	 */
	public ID3v2Tag(RandomAccessInputStream raf) throws FileNotFoundException, IOException, ID3v2FormatException
	{
		frames = new ID3v2Frames<String, ID3v2Frame>();
		head = new ID3v2Header(raf);
		padding = 0;
		exists = head.headerExists();

		if (exists)
		{
			if (head.getExtendedHeader())
			{
				ext_head = new ID3v2ExtendedHeader(raf);
			}
			if (head.getFooter())
			{
				foot = new ID3v2Footer(raf, head.getTagSize() + head.getHeaderSize());
			}

			// For now only support id3v2.3.0 or greater
			if (head.getMajorVersion() >= 3)
			{
				parseFrames(raf);
			}

			origSize = getSize();
			origPadding = getPadding();
		}
	}

	/**
	 * Converts an integer to an Motorola 86000er dword byte array
	 * @since 23.12.2008
	 * @param value
	 * @param offset
	 * @return
	 */
	public static byte [] convertIntToDWord(final int value)
	{
		byte[] buf = new byte[4];
		buf[0] = (byte)((value>>24)&0xFF);
		buf[1] = (byte)((value>>16)&0xFF);
		buf[2] = (byte)((value>> 8)&0xFF);
		buf[3] = (byte)((value    )&0xFF);
		return buf;
	}

	/**
	 * Converts an Motorola 86000er dword to an integer (most significant byte first)
	 * @param buf
	 * @param offset
	 * @return
	 */
	public static int convertDWordToInt(final byte[] buf, final int offset)
	{
		return ((buf[offset]&0xFF)<<24) | ((buf[offset+1]&0xFF)<<16) | ((buf[offset+2]&0xFF)<<8) | (buf[offset+3]&0xFF);
	}
	
	/**
	 * Read the frames from the file and create ID3v2Frame objects from the
	 * data found.
	 *
	 * @param raf the open file to read from
	 * @exception FileNotFoundException if an error occurs
	 * @exception IOException if an error occurs
	 * @exception ID3v2FormatException if an error occurs
	 */
	private void parseFrames(RandomAccessInputStream raf) throws FileNotFoundException, IOException, ID3v2FormatException
	{
		int offset = head.getHeaderSize();
		int framesLength = head.getTagSize();

		if (head.getExtendedHeader())
		{
			framesLength -= ext_head.getSize();
			offset += ext_head.getSize();
		}

		raf.seek(offset);

		int bytesRead = 0;
		boolean done = false;
		while ((bytesRead < framesLength) && !done)
		{
			byte[] buf = new byte[4];
			bytesRead += raf.read(buf);

			if (buf[0] != 0)
			{
				String id = new String(buf);
				bytesRead += raf.read(buf);
				int curLength = ID3v2Tag.convertDWordToInt(buf, 0);
				byte [] flags = new byte[2];
				bytesRead += raf.read(flags);
				byte [] data = new byte[curLength];
				bytesRead += raf.read(data);
				ID3v2Frame frame = new ID3v2Frame(id, flags, data);
				frames.put(id, frame);
			}
			else
			{
				done = true;
				padding = framesLength - bytesRead - buf.length;
			}
		}
	}

	/**
	 * Saves all the information in the tag to the file passed to the 
	 * constructor.  If a tag doesn't exist, a tag is prepended to the file.
	 * If the padding has not changed since the creation of this object and
	 * the size is less than the original size + the original padding, then
	 * the previous tag and part of the previous padding will be overwritten.
	 * Otherwise, a new tag will be prepended to the file.
	 *
	 * @return true if the tag was successfully written
	 * @exception FileNotFoundException if an error occurs
	 * @exception IOException if an error occurs
	 */
	public void writeTag(RandomAccessFile raf) throws FileNotFoundException, IOException
	{
		int curSize = getSize();
		origPadding = padding;
		padding = getUpdatedPadding();

		// This means that the file does not need to change size
		if ((padding > origPadding) || ((padding == origPadding) && (curSize == origSize)))
		{

			byte[] out = getBytes();
			raf.seek(0);
			raf.write(out);
		}
		else
		{
			//TODO: This needs copying without full loading
			int bufSize = (int)(raf.length() + curSize);
			byte[] out = new byte[bufSize];
			System.arraycopy(getBytes(), 0, out, 0, curSize);
			
			int bufSize2 = (int)(raf.length() - origSize);
			byte[] in = new byte[bufSize2];
			raf.seek(origSize);

			if (raf.read(in) != in.length)
			{
				throw new IOException("Error reading mp3 file before writing");
			}

			System.arraycopy(in, 0, out, curSize, in.length);

			raf.setLength(bufSize2);
			raf.seek(0);
			raf.write(out);
		}
		
		origSize = curSize;
		exists = true;
	}

	/**
	 * Remove an existing id3v2 tag from the file passed to the constructor.
	 *
	 * @return true if the removal was a success
	 * @exception FileNotFoundException if an error occurs
	 * @exception IOException if an error occurs
	 */
	public void removeTag(RandomAccessFile raf) throws FileNotFoundException, IOException
	{
		if (exists)
		{
			int bufSize = (int)(raf.length() - origSize);
			byte[] buf = new byte[bufSize];

			raf.seek(origSize);

			if (raf.read(buf) != buf.length)
			{
				throw new IOException("Error encountered while removing " + "id3v2 tag.");
			}

			raf.setLength(bufSize);
			raf.seek(0);
			raf.write(buf);

			exists = false;
		}
	}

	/**
	 * Return a binary representation of this object to be written to a file.
	 * This is in the format of the id3v2 specifications.  This includes the
	 * header, extended header (if it exists), the frames, padding (if it 
	 * exists), and a footer (if it exists).
	 *
	 * @return a binary representation of this id3v2 tag
	 */
	public byte[] getBytes()
	{
		byte[] b = new byte[getSize() + padding];
		int bytesCopied = 0;
		int length = 0;

		length = head.getHeaderSize();
		System.arraycopy(head.getBytes(), 0, b, bytesCopied, length);
		bytesCopied += length;
		if (head.getExtendedHeader())
		{
			length = ext_head.getSize();
			System.arraycopy(ext_head.getBytes(), 0, b, bytesCopied, length);
			bytesCopied += length;
		}
		length = frames.getLength();
		System.arraycopy(frames.getBytes(), 0, b, bytesCopied, length);
		bytesCopied += length;

		// Bytes should all be zero's by default
		System.arraycopy(new byte[padding], 0, b, bytesCopied, padding);
		bytesCopied += padding;

		if (head.getFooter())
		{
			length = foot.getFooterSize();
			System.arraycopy(foot.getBytes(), 0, b, bytesCopied, length);
			bytesCopied += length;
		}

		return b;
	}

	/**
	 * Determines the new amount of padding to use.  If the user has not 
	 * changed the amount of padding then existing padding will be overwritten
	 * instead of increasing the size of the file.  That is only if there is 
	 * a sufficient amount of padding for the updated tag.
	 *
	 * @return the new amount of padding
	 */
	private int getUpdatedPadding()
	{
		int curSize = getSize();
		int pad = 0;

		if ((origPadding == padding) && (curSize > origSize) && (padding >= (curSize - origSize)))
		{
			pad = padding - (curSize - origSize);
		}
		else if (curSize < origSize)
		{
			pad = (origSize - curSize) + padding;
		}

		return pad;
	}

	/**
	 * Set the data contained in a text frame.  This includes all frames with
	 * an id that starts with 'T' but excludes "TXXX".  If an improper id
	 * is passed, then nothing will happen.
	 *
	 * @param id the id of the frame to set the data for
	 * @param data the data for the frame
	 */
	public void setTextFrame(String id, String data)
	{
		if ((id.charAt(0) == 'T') && !id.equals(ID3v2Frames.USER_DEFINED_TEXT_INFO))
		{

			try
			{
				byte[] b = new byte[data.length() + 1];
				b[0] = 0;
				System.arraycopy(data.getBytes(ENC_TYPE), 0, b, 1, data.length());

				updateFrameData(id, b);
			}
			catch (UnsupportedEncodingException e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Set the data contained in a URL frame.  This includes all frames with
	 * an id that starts with 'W' but excludes "WXXX".  If an improper id is
	 * passed, then nothing will happen.
	 *
	 * @param id the id of the frame to set the data for
	 * @param data the data for the frame
	 */
	public void setURLFrame(String id, String data)
	{
		if ((id.charAt(0) == 'W') && !id.equals(ID3v2Frames.USER_DEFINED_URL))
		{
			updateFrameData(id, data.getBytes());
		}
	}

	/**
	 * Sets the data contained in the user defined text frame (TXXX).
	 *
	 * @param description a description of the data
	 * @param value the data for the frame
	 */
	public void setUserDefinedTextFrame(String description, String value)
	{
		try
		{
			byte[] b = new byte[description.length() + value.length() + 2];
			int bytesCopied = 0;
			b[bytesCopied++] = 0;
			System.arraycopy(description.getBytes(ENC_TYPE), 0, b, bytesCopied, description.length());
			bytesCopied += description.length();
			b[bytesCopied++] = 0;
			System.arraycopy(value.getBytes(ENC_TYPE), 0, b, bytesCopied, value.length());
			bytesCopied += value.length();

			updateFrameData(ID3v2Frames.USER_DEFINED_TEXT_INFO, b);
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Sets the data contained in the user defined url frame (WXXX).
	 *
	 * @param description a description of the url
	 * @param value the url for the frame
	 */
	public void setUserDefinedURLFrame(String description, String value)
	{
		try
		{
			byte[] b = new byte[description.length() + value.length() + 2];
			int bytesCopied = 0;
			b[bytesCopied++] = 0;
			System.arraycopy(description.getBytes(ENC_TYPE), 0, b, bytesCopied, description.length());
			bytesCopied += description.length();
			b[bytesCopied++] = 0;
			System.arraycopy(value.getBytes(), 0, b, bytesCopied, value.length());
			bytesCopied += value.length();

			updateFrameData(ID3v2Frames.USER_DEFINED_URL, b);
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Set the data contained in the comments frame (COMM).
	 *
	 * @param description a description of the comment
	 * @param comment the comment
	 */
	public void setCommentFrame(String description, String comment)
	{
		try
		{
			byte[] b = new byte[description.length() + comment.length() + 5];
			int bytesCopied = 0;
			b[bytesCopied++] = 0;
			b[bytesCopied++] = 'e';
			b[bytesCopied++] = 'n';
			b[bytesCopied++] = 'g';
			System.arraycopy(description.getBytes(ENC_TYPE), 0, b, bytesCopied, description.length());
			bytesCopied += description.length();
			b[bytesCopied++] = 0;
			System.arraycopy(comment.getBytes(ENC_TYPE), 0, b, bytesCopied, comment.length());
			bytesCopied += comment.length();

			updateFrameData(ID3v2Frames.COMMENTS, b);
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Remove the frame with the specified id from the file.  If there is no
	 * frame with that id nothing will happen.
	 *
	 * @param id the id of the frame to remove
	 */
	public void removeFrame(String id)
	{
		frames.remove(id);
		updateSize();
	}

	/**
	 * Updates the data for the frame specified by id.  If no frame exists for
	 * the id specified, a new frame with that id is created.
	 *
	 * @param id the id of the frame to update
	 * @param data the data for the frame
	 */
	public void updateFrameData(String id, byte[] data)
	{
		if (frames.containsKey(id))
		{
			((ID3v2Frame) frames.get(id)).setFrameData(data);
		}
		else
		{
			ID3v2Frame frame = new ID3v2Frame(id, data);
			frames.put(id, frame);
		}

		updateSize();
	}

	/**
	 * Returns the textual information contained in the frame specified by the
	 * id.  Not every type of frame has textual information.  If an id is 
	 * specified that will not work, the empty string is returned.
	 *
	 * @param id the id of the frame to get text from
	 * @return the text information contained in the frame
	 * @exception ID3v2FormatException if an error is encountered parsing data
	 */
	public String getFrameDataString(String id)
	{
		try
		{
			if (frames.containsKey(id))
			{
				return ((ID3v2Frame) frames.get(id)).getDataString();
			}
		}
		catch (ID3v2FormatException ex)
		{
			Log.error("ID3v2Tag:", ex);
		}
		return null;
	}

	/**
	 * Returns the data found in the frame specified by the id.  If the frame
	 * doesn't exist, then a zero length array is returned.
	 *
	 * @param id the id of the frame to get the data from
	 * @return the data found in the frame
	 */
	public byte[] getFrameData(String id)
	{
		if (frames.containsKey(id))
		{
			return ((ID3v2Frame) frames.get(id)).getFrameData();
		}

		return null;
	}

	/**
	 * Updates the size field of the id3 header and footer (if it exists) 
	 * from the current size of the id3v2 frames plus the extended header 
	 * size (if it exists).
	 *
	 */
	private void updateSize()
	{
		int length = frames.getLength();

		if (head.getExtendedHeader())
		{
			length += ext_head.getSize();
		}

		head.setTagSize(length);

		if (head.getFooter())
		{
			foot.setTagSize(length);
		}
	}

	/**
	 * Returns true if an id3v2 tag exists in the file that was passed to the 
	 * constructor and false otherwise
	 *
	 * @return true if an id3v2 tag exists in the file passed to the ctor
	 */
	public boolean tagExists()
	{
		return exists;
	}

	/**
	 * Returns the size of this id3v2 tag.  This includes the header, 
	 * extended header, frames, padding, and footer.
	 *
	 * @return the size (in bytes) of the entire id3v2 tag
	 */
	public int getSize()
	{
		int retval = head.getTagSize() + head.getHeaderSize();

		if (head.getFooter())
		{
			retval += foot.getFooterSize();
		}

		return retval;
	}

	/**
	 * Returns the current number of padding bytes in this id3v2 tag.
	 *
	 * @return the current number of padding bytes in this id3v2 tag
	 */
	public int getPadding()
	{
		return padding;
	}

	/**
	 * Set the amount of padding to use when writing this tag.  There cannot
	 * be any padding if a footer exists.  Nothing will happen if this function
	 * is called and a footer exists or if the number is negative.
	 *
	 * @param pad the amount of padding to use when writing this tag
	 */
	public void setPadding(int pad)
	{
		if (!head.getFooter() && (pad >= 0))
		{
			padding = pad;
		}
	}

	/**
	 * Return a string representation of this object.  This includes all data
	 * contained in all parts of this tag.
	 *
	 * @return a string representation of this object
	 */
	public String toString()
	{
		String str = head.toString();

		str += "\nPadding:\t\t\t" + getPadding() + " bytes";

		if (head.getExtendedHeader())
		{
			str += "\n" + ext_head.toString();
		}

		str += "\n" + frames.toString();

		if (head.getFooter())
		{
			str += foot.toString();
		}

		return str;
	}

} // ID3v2Tag
