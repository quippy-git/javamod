/*
 * @(#) RandomAccessInputStreamImpl.java
 *
 * Created on 31.12.2007 by Daniel Becker
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
package de.quippy.javamod.io;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.UTFDataFormatException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import de.quippy.javamod.system.Helpers;
import de.quippy.javamod.system.Log;

/**
 * This class mappes the RandomAccessFile to an InputStream type of class.
 * You can also instantiate this class with an URL. If this URL is not of
 * protocol type "file://" the resource will get downloaded and written to
 * a tmp file. The tempfile will be deleted with calling of "close".
 * Furthermore this input stream will also handle zip compressed content
 * If nothing else works the fallback strategy is to use an internal fullFileCache.
 * This will be also used by PowerPacked Modes (see ModfileInputSteam)
 *
 * Additionally, to speed up things, RandomAccessStreamImpl is provided with a
 * transparent buffer - changed on 18.01.22.
 * @author Daniel Becker
 * @since 31.12.2007
 */
public class RandomAccessInputStreamImpl extends InputStream implements RandomAccessInputStream
{
	private RandomAccessFile raFile = null;
	private File localFile = null;
	private File tmpFile = null;
	private int mark = 0;

	/* The readBuffer - 8K should be sufficient */
	private static final int STANDARD_LENGTH = 8192;
	private byte[] randomAccessBuffer = null;		// the buffer
	private int randomAccessBuffer_endPointer = 0;	// end pointer, equals length / bytes read
	private int randomAccessBuffer_readPointer = 0; // the index into the buffer
	private long randomAccessFilePosition = 0;		// position in file, start of buffer
	private long randomAccessFileLength = 0;		// the whole file length

	/* The fullFileCache */
	protected byte[] fullFileCache = null;
	protected int fullFileCache_readPointer = 0;
	protected int fullFileCache_length = 0;

	/**
	 * Constructor for RandomAccessInputStreamImpl
	 * @param file
	 * @throws FileNotFoundException
	 */
	public RandomAccessInputStreamImpl(File file) throws IOException, FileNotFoundException
	{
		super();
		if (!file.exists())
		{
			file = unpackFromZIPFile(file.toURI().toURL());
		}
		openRandomAccessStream(localFile = file);
	}
	/**
	 * Constructor for RandomAccessInputStreamImpl
	 * @param fileName
	 * @throws FileNotFoundException
	 */
	public RandomAccessInputStreamImpl(final String fileName) throws IOException, FileNotFoundException
	{
		this(new File(fileName));
	}
	public RandomAccessInputStreamImpl(final URL fromUrl) throws IOException, FileNotFoundException
	{
		super();
		if (Helpers.isFile(fromUrl))
		{
			try
			{
				File file = new File(fromUrl.toURI());
				if (!file.exists())
				{
					file = unpackFromZIPFile(fromUrl);
				}
				openRandomAccessStream(localFile = file);
			}
			catch (final URISyntaxException uriEx)
			{
				throw new MalformedURLException(uriEx.getMessage());
			}
		}
		else
		{
			final InputStream inputStream = new FileOrPackedInputStream(fromUrl);

			try
			{
				tmpFile = copyFullStream(inputStream);
				try { inputStream.close(); } catch (final IOException ex) { /* Log.error("IGNORED", ex); */ }
				openRandomAccessStream(localFile = tmpFile);
			}
			catch (final Throwable ex)
			{
				int size = inputStream.available();
				if (size<1024) size = 1024;
				final ByteArrayOutputStream out = new ByteArrayOutputStream(size);

				copyFullStream(inputStream, out);

				try { inputStream.close(); } catch (final IOException e) { /* Log.error("IGNORED", e); */ }
				out.close();

				fullFileCache = out.toByteArray();
				fullFileCache_length = fullFileCache.length;
				fullFileCache_readPointer = 0;
				raFile = null;
				localFile = null;
			}
		}
	}
	/**
	 * Constructor for RandomAccessInputStreamImpl from a direct byte array
	 * @param fromByteArray
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public RandomAccessInputStreamImpl(final byte[] fromByteArray) throws IOException, FileNotFoundException
	{
		super();
		fullFileCache = fromByteArray;
		fullFileCache_length = fullFileCache.length;
		fullFileCache_readPointer = 0;
		raFile = null;
		localFile = null;
	}
	/**
	 * @since 04.01.2011
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private File unpackFromZIPFile(final URL fromUrl) throws IOException, FileNotFoundException
	{
		final InputStream input = new FileOrPackedInputStream(fromUrl);
		return copyFullStream(input);
	}
	/**
	 * @since 02.01.2011
	 * @param input
	 * @return
	 * @throws IOException
	 */
	private File copyFullStream(final InputStream input) throws IOException
	{
		tmpFile = File.createTempFile("JavaMod", "ReadFile");
		tmpFile.deleteOnExit();

		final FileOutputStream out = new FileOutputStream(tmpFile);
		copyFullStream(input, out);
		out.close();

		return tmpFile;
	}
	/**
	 * @since 02.01.2008
	 * @param inputStream
	 * @param out
	 * @throws IOException
	 */
	private void copyFullStream(final InputStream inputStream, final OutputStream out) throws IOException
	{
		final byte[] input = new byte[STANDARD_LENGTH];
		int len;
		while ((len = inputStream.read(input, 0, STANDARD_LENGTH))!=-1)
		{
			out.write(input, 0, len);
		}
	}
	/**
	 * Will return the local file this RandomAccessFile works on
	 * or null using local fullFileCache and no file
	 * @since 09.01.2011
	 * @return
	 */
	@Override
	public File getFile()
	{
		return localFile;
	}
	/**
	 * @since 18.01.2022
	 * @param theFile
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void openRandomAccessStream(final File theFile) throws FileNotFoundException, IOException
	{
		raFile = new RandomAccessFile(theFile, "r");

		randomAccessBuffer = new byte[STANDARD_LENGTH];
		randomAccessFileLength = raFile.length();

		fillRandomAccessBuffer(0);
	}
	/**
	 * @since 18.01.2022
	 * @return
	 * @throws IOException
	 */
	private int fillRandomAccessBuffer(final long fromWhere) throws IOException
	{
		if (raFile!=null)
		{
			if (fromWhere != raFile.getFilePointer()) raFile.seek(fromWhere);
			randomAccessFilePosition = raFile.getFilePointer();
			randomAccessBuffer_endPointer = raFile.read(randomAccessBuffer);
			randomAccessBuffer_readPointer = 0;
			return randomAccessBuffer_endPointer;
		}
		return -1;
	}
	/**
	 * @since 18.01.2022
	 * @param b
	 * @param off
	 * @param len
	 * @return
	 * @throws IOException
	 */
	private int readBytes_internal(final byte b[], int off, final int len) throws IOException
	{
		if (randomAccessBuffer_endPointer<0) return -1; // already at end of stream, nothing more to read!

		int read = len;
		while (read>0 && randomAccessBuffer_endPointer>=0)
		{
			int canRead = randomAccessBuffer_endPointer - randomAccessBuffer_readPointer;
			if (canRead>0)
			{
				if (canRead>read) canRead = read;
				System.arraycopy(randomAccessBuffer, randomAccessBuffer_readPointer, b, off, canRead);
				randomAccessBuffer_readPointer += canRead;
				off += canRead;
				read -= canRead;
			}
			if (randomAccessBuffer_readPointer>=randomAccessBuffer_endPointer && read>0)
			{
				fillRandomAccessBuffer(this.getFilePointer());
			}
		}
		return len - read;
	}
	/**
	 * @since 18.01.2022
	 * @return
	 * @throws IOException
	 */
	private int readByte_internal() throws IOException
	{
		if (randomAccessBuffer_readPointer >= randomAccessBuffer_endPointer)
		{
			final int read = fillRandomAccessBuffer(this.getFilePointer());
			if (read==-1) return -1;
		}
		return (randomAccessBuffer[randomAccessBuffer_readPointer++])&0xFF;
	}
	/**
	 * @return
	 * @throws IOException
	 * @see java.io.InputStream#available()
	 */
	@Override
	public int available() throws IOException
	{
		if (raFile!=null)
			return (int)(randomAccessFileLength - this.getFilePointer());
		else
			return fullFileCache_length - fullFileCache_readPointer;
	}
	/**
	 * @throws IOException
	 * @see java.io.InputStream#close()
	 */
	@Override
	public void close() throws IOException
	{
		if (raFile!=null) raFile.close();
		super.close();
		if (tmpFile!=null)
		{
			final boolean ok = tmpFile.delete();
			if (!ok) Log.error("Could not delete temporary file: " + tmpFile.getCanonicalPath());
		}

		raFile = null;
		tmpFile = null;
		randomAccessBuffer = null;
		randomAccessBuffer_endPointer = 0;
		randomAccessBuffer_readPointer = 0;
		randomAccessFilePosition = 0;
		randomAccessFileLength = 0;

		fullFileCache = null;
		fullFileCache_length = 0;
		fullFileCache_readPointer = 0;
	}
	/**
	 * @param readlimit
	 * @see java.io.InputStream#mark(int)
	 */
	@Override
	public synchronized void mark(final int readlimit)
	{
		try
		{
			if (raFile!=null)
				mark = (int)this.getFilePointer();
			else
				mark = fullFileCache_readPointer;
		}
		catch (final IOException ex)
		{
		}
	}
	/**
	 * @return
	 * @see java.io.InputStream#markSupported()
	 */
	@Override
	public boolean markSupported()
	{
		return true;
	}
	/**
	 * @throws IOException
	 * @see java.io.InputStream#reset()
	 */
	@Override
	public synchronized void reset() throws IOException
	{
		if (raFile!=null)
			this.seek(mark);
		else
			fullFileCache_readPointer = mark;
	}
	/**
	 * @param n
	 * @return
	 * @throws IOException
	 * @see java.io.InputStream#skip(long)
	 */
	@Override
	public long skip(final long n) throws IOException
	{
		if (raFile!=null)
		{
	        if (n <= 0) return 0;
	        final long pos = randomAccessFilePosition + randomAccessBuffer_readPointer;
	        long newpos = pos + n;
	        if (newpos > randomAccessFileLength) newpos = randomAccessFileLength;
	        this.seek(newpos);
	        return newpos - pos;
		}
		else
		{
			if (n <= 0) return 0;
			int newpos = fullFileCache_readPointer + (int)n;
			if (newpos > fullFileCache_length) newpos = fullFileCache_length;
			final int skipped = newpos - fullFileCache_readPointer;
			fullFileCache_readPointer = newpos;
			return skipped;
		}
	}
	/**
	 * @param n
	 * @return
	 * @throws IOException
	 * @see de.quippy.javamod.io.RandomAccessInputStream#skipBack(int)
	 */
	@Override
	public int skipBack(final int n) throws IOException
	{
		final long currentPos = getFilePointer();
		seek(currentPos - n);
		return (int)(currentPos - getFilePointer());
	}
	/**
	 * @param n
	 * @return
	 * @throws IOException
	 * @see de.quippy.javamod.io.RandomAccessInputStream#skipBytes(int)
	 */
	@Override
	public int skipBytes(final int n) throws IOException
	{
		return (int)skip(n);
	}
	/**
	 * @return
	 * @throws IOException
	 * @see de.quippy.javamod.io.RandomAccessInputStream#getFilePointer()
	 */
	@Override
	public long getFilePointer() throws IOException
	{
		if (raFile!=null)
			return randomAccessFilePosition + randomAccessBuffer_readPointer;
		else
			return fullFileCache_readPointer;
	}
	/**
	 * @param pos
	 * @throws IOException
	 * @see de.quippy.javamod.io.RandomAccessInputStream#seek(long)
	 */
	@Override
	public void seek(final long pos) throws IOException
	{
		if (raFile!=null)
		{
			if (pos > (randomAccessFilePosition + randomAccessBuffer_endPointer) ||
				pos < randomAccessFilePosition)
			{
				fillRandomAccessBuffer(pos);
			}
			else
				randomAccessBuffer_readPointer = (int)(pos - randomAccessFilePosition);
		}
		else
			fullFileCache_readPointer = (int)pos;
	}
	/**
	 * @return
	 * @throws IOException
	 * @see de.quippy.javamod.io.RandomAccessInputStream#getLength()
	 */
	@Override
	public long getLength() throws IOException
	{
		return length();
	}
	/**
	 * @return
	 * @throws IOException
	 * @see de.quippy.javamod.io.RandomAccessInputStream#length()
	 */
	@Override
	public long length() throws IOException
	{
		if (raFile!=null)
			return randomAccessFileLength;
		else
			return fullFileCache_length;
	}
/********************* Core Read Methods **************************************/
	/**
	 * @return
	 * @throws IOException
	 * @see java.io.InputStream#read()
	 */
	@Override
	public int read() throws IOException
	{
		if (raFile!=null)
			return readByte_internal();
		else
			return (fullFileCache_readPointer<fullFileCache_length)? (fullFileCache[fullFileCache_readPointer++]) & 0xFF : -1;
	}
	/**
	 * @param b
	 * @param off
	 * @param len
	 * @return
	 * @throws IOException
	 * @see java.io.InputStream#read(byte[], int, int)
	 */
	@Override
	public int read(final byte[] b, final int off, int len) throws IOException
	{
		if (raFile!=null)
			return readBytes_internal(b, off, len);
		else
		{
			if (b == null)
				throw new NullPointerException();
			if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length) || ((off + len) < 0))
				throw new IndexOutOfBoundsException();
			if (fullFileCache_readPointer >= fullFileCache_length)
				return -1;
			if (fullFileCache_readPointer + len > fullFileCache_length)
				len = fullFileCache_length - fullFileCache_readPointer;
			if (len <= 0)
				return 0;
			System.arraycopy(fullFileCache, fullFileCache_readPointer, b, off, len);
			fullFileCache_readPointer += len;
			return len;
		}
	}
	/**
	 * @param b
	 * @return
	 * @throws IOException
	 * @see java.io.InputStream#read(byte[])
	 */
	@Override
	public int read(final byte[] b) throws IOException
	{
		if (b!=null) return read(b, 0, b.length); else throw new NullPointerException("Buffer is null");
	}
/*********************  Read & conversion Methods *****************************/
	/**
	 * @return
	 * @throws IOException
	 * @see de.quippy.javamod.io.RandomAccessInputStream#readByte()
	 */
	@Override
	public byte readByte() throws IOException
	{
		return (byte)this.read();
	}
	/**
	 * @return
	 * @throws IOException
	 * @see de.quippy.javamod.io.RandomAccessInputStream#readBoolean()
	 */
	@Override
	public boolean readBoolean() throws IOException
	{
		final int ch = this.read();
		if (ch < 0) throw new EOFException();
		return (ch != 0);
	}
	/**
	 * @return
	 * @throws IOException
	 * @see de.quippy.javamod.io.RandomAccessInputStream#readChar()
	 */
	@Override
	public char readChar() throws IOException
	{
		final int ch1 = this.read();
		final int ch2 = this.read();
		if ((ch1 | ch2) < 0) throw new EOFException();
		return (char)((ch1 << 8) | (ch2));
	}
	/**
	 * @return
	 * @throws IOException
	 * @see de.quippy.javamod.io.RandomAccessInputStream#readShort()
	 */
	@Override
	public short readShort() throws IOException
	{
		final int ch1 = this.read();
		final int ch2 = this.read();
		if ((ch1 | ch2) < 0) throw new EOFException();
		return (short)((ch1 << 8) | (ch2));
	}
	/**
	 * @return
	 * @throws IOException
	 * @see de.quippy.javamod.io.RandomAccessInputStream#readDouble()
	 */
	@Override
	public double readDouble() throws IOException
	{
		return Double.longBitsToDouble(this.readLong());
	}
	/**
	 * @return
	 * @throws IOException
	 * @see de.quippy.javamod.io.RandomAccessInputStream#readFloat()
	 */
	@Override
	public float readFloat() throws IOException
	{
		return Float.intBitsToFloat(this.readInt());
	}
	/**
	 * @return
	 * @throws IOException
	 * @see de.quippy.javamod.io.RandomAccessInputStream#readInt()
	 */
	@Override
	public int readInt() throws IOException
	{
		final int ch1 = this.read();
		final int ch2 = this.read();
		final int ch3 = this.read();
		final int ch4 = this.read();
		if ((ch1 | ch2 | ch3 | ch4) < 0) throw new EOFException();
		return ((ch1 << 24) | (ch2 << 16) | (ch3 << 8) | (ch4));
	}
	/**
	 * @return
	 * @throws IOException
	 * @see de.quippy.javamod.io.RandomAccessInputStream#readLine()
	 */
	@Override
	public String readLine() throws IOException
	{
		final StringBuilder input = new StringBuilder();
		int c = -1;
		boolean eol = false;

		while (!eol)
		{
			switch (c = read())
			{
				case -1:
				case '\n':
					eol = true;
					break;
				case '\r':
					eol = true;
					final long cur = getFilePointer();
					if ((read()) != '\n') seek(cur);
					break;
				default:
					input.append((char) c);
					break;
			}
		}

		if (c==-1 && input.isEmpty())
		{
			return null;
		}
		return input.toString();
	}
	/**
	 * @return
	 * @throws IOException
	 * @see de.quippy.javamod.io.RandomAccessInputStream#readLong()
	 */
	@Override
	public long readLong() throws IOException
	{
		return ((long)(readInt()) << 32) + (readInt() & 0xFFFFFFFFL);
	}
	/**
	 * @return
	 * @throws IOException
	 * @see de.quippy.javamod.io.RandomAccessInputStream#readUnsignedByte()
	 */
	@Override
	public int readUnsignedByte() throws IOException
	{
		final int ch = this.read();
		if (ch < 0) throw new EOFException();
		return ch;
	}
	/**
	 * @return
	 * @throws IOException
	 * @see de.quippy.javamod.io.RandomAccessInputStream#readUnsignedShort()
	 */
	@Override
	public int readUnsignedShort() throws IOException
	{
		final int ch1 = this.read();
		final int ch2 = this.read();
		if ((ch1 | ch2) < 0) throw new EOFException();
		return (ch1 << 8) | (ch2);
	}
	/**
	 * @return
	 * @throws IOException
	 * @see de.quippy.javamod.io.RandomAccessInputStream#readUTF()
	 */
	@Override
	public String readUTF() throws IOException
	{
		final int utflen = this.readUnsignedShort();
		final byte[] bytearr = new byte[utflen];
		final char[] chararr = new char[utflen];

		int c, char2, char3;
		int count = 0;
		int chararr_count = 0;

		read(bytearr, 0, utflen);

		while (count < utflen)
		{
			c = bytearr[count] & 0xff;
			if (c > 127) break;
			count++;
			chararr[chararr_count++] = (char) c;
		}

		while (count < utflen)
		{
			c = bytearr[count] & 0xff;
			switch (c >> 4)
			{
				case 0:
				case 1:
				case 2:
				case 3:
				case 4:
				case 5:
				case 6:
				case 7:
					/* 0xxxxxxx */
					count++;
					chararr[chararr_count++] = (char) c;
					break;
				case 12:
				case 13:
					/* 110x xxxx 10xx xxxx */
					count += 2;
					if (count > utflen) throw new UTFDataFormatException("malformed input: partial character at end");
					char2 = bytearr[count - 1];
					if ((char2 & 0xC0) != 0x80) throw new UTFDataFormatException("malformed input around byte " + count);
					chararr[chararr_count++] = (char) (((c & 0x1F) << 6) | (char2 & 0x3F));
					break;
				case 14:
					/* 1110 xxxx 10xx xxxx 10xx xxxx */
					count += 3;
					if (count > utflen) throw new UTFDataFormatException("malformed input: partial character at end");
					char2 = bytearr[count - 2];
					char3 = bytearr[count - 1];
					if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80)) throw new UTFDataFormatException("malformed input around byte " + (count - 1));
					chararr[chararr_count++] = (char) (((c & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0));
					break;
				default:
					/* 10xx xxxx, 1111 xxxx */
					throw new UTFDataFormatException("malformed input around byte " + count);
			}
		}
		// The number of chars produced may be less than utflen
		return new String(chararr, 0, chararr_count);
	}
/******************** added service methods for conversion ********************/
	/**
	 * @since 31.12.2007
	 * @param strLength
	 * @return a String
	 * @throws IOException
	 */
	public String readString(final int strLength) throws IOException
	{
		final byte [] buffer = new byte[strLength];
		final int read = read(buffer, 0, strLength);
		return Helpers.retrieveAsString(buffer, 0, read);
	}
	/**
	 * @since 05.08.2020
	 * @return
	 * @throws IOException
	 */
	public float readIntelFloat() throws IOException
	{
		return Float.intBitsToFloat(readIntelDWord());
	}
	/**
	 * @since 05.08.2020
	 * @return
	 * @throws IOException
	 */
	public double readIntelDouble() throws IOException
	{
		return Double.longBitsToDouble(readIntelLong());
	}
	/**
	 * @since 31.12.2007
	 */
	public int readMotorolaUnsignedWord() throws IOException
	{
		return (((readByte()&0xFF)<<8) | (readByte()&0xFF))&0xFFFF;
	}
	/**
	 * @since 31.12.2007
	 */
	public int readIntelUnsignedWord() throws IOException
	{
		return ((readByte()&0xFF) | ((readByte()&0xFF)<<8))&0xFFFF;
	}
	/**
	 * @since 31.12.2007
	 */
	public short readMotorolaWord() throws IOException
	{
		return (short)((((readByte()&0xFF)<<8) | (readByte()&0xFF))&0xFFFF);
	}
	/**
	 * @since 31.12.2007
	 */
	public short readIntelWord() throws IOException
	{
		return (short)(((readByte()&0xFF) | ((readByte()&0xFF)<<8))&0xFFFF);
	}
	/**
	 * @since 31.12.2007
	 */
	public int readMotorolaDWord() throws IOException
	{
		return ((readByte()&0xFF)<<24) | ((readByte()&0xFF)<<16) | ((readByte()&0xFF)<<8) | (readByte()&0xFF);
	}
	/**
	 * @since 31.12.2007
	 */
	public int readIntelDWord() throws IOException
	{
		return (readByte()&0xFF) | ((readByte()&0xFF)<<8) | ((readByte()&0xFF)<<16) | ((readByte()&0xFF)<<24);
	}
	/**
	 * @since 05.08.2020
	 * @return
	 * @throws IOException
	 */
	public long readMotorolaLong() throws IOException
	{
		return (((long)readMotorolaDWord())<<32) | (((long)readMotorolaDWord())&0xFFFFFFFF);
	}
	/**
	 * @since 05.08.2020
	 * @return
	 * @throws IOException
	 */
	public long readIntelLong() throws IOException
	{
		return (((long)readIntelDWord())&0xFFFFFFFF) | (((long)readIntelDWord())<<32);
	}
	/**
	 * Will read size bytes from a stream and convert that to an integer value of
	 * type byte (1), short (2), int (4), long(8).
	 * Sizes bigger than 8 will be ignored, but "size" bytes will be skipped.
	 * @since 03.02.2024
	 * @param size
	 * @return
	 * @throws IOException
	 */
	public long readMotorolaBytes(final int size) throws IOException
	{
		long result = 0;
		if (size!=0)
		{
			final int readBytes = (size>8)?8:size;
			for (int i=0; i<readBytes; i++)
				result = (result<<8) | (read()&0xFF);
			skip(size-readBytes);
		}
		return result;
	}
	/**
	 * Will read size bytes from a stream and convert that to an integer value of
	 * type byte (1), short (2), int (4), long(8).
	 * Sizes bigger than 8 will be ignored, but "size" bytes will be skipped.
	 * @since 03.02.2024
	 * @param size
	 * @return
	 * @throws IOException
	 */
	public long readIntelBytes(final int size) throws IOException
	{
		long result = 0;
		if (size!=0)
		{
			final int readBytes = (size>8)?8:size;
			int shift = 0;
			for (int i=0; i<readBytes; i++)
			{
				result |= (read()<<shift);
				shift+=8;
			}
			skip(size-readBytes);
		}
		return result;
	}
}
