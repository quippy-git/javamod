/*
 *  21.04.2004 Original verion. davagin@udm.ru.
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

package de.quippy.jmac.tools;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Author: Dmitry Vaguine
 * Date: 12.03.2004
 * Time: 13:35:13
 */
public class InputStreamFile extends File {
    private DataInputStream stream = null;
    private String name = null;

    public InputStreamFile(final URL url) throws IOException {
        this(url.openStream(), url.getPath());
    }

    public InputStreamFile(final InputStream stream) {
        this(stream, null);
    }

    public InputStreamFile(final InputStream stream, final String name) {
        this.stream = new DataInputStream(new BufferedInputStream(stream));
        this.name = name;
    }

    @Override
	public void mark(final int readlimit) throws IOException {
        stream.mark(readlimit);
    }

    @Override
	public void reset() throws IOException {
        stream.reset();
    }

    @Override
	public int read() throws IOException {
        return stream.read();
    }

    @Override
	public void readFully(final byte[] b) throws IOException {
        stream.readFully(b);
    }

    @Override
	public void readFully(final byte[] b, final int offs, final int len) throws IOException {
        stream.readFully(b, offs, len);
    }

    @Override
	public int read(final byte[] b) throws IOException {
        return stream.read(b);
    }

    @Override
	public int read(final byte[] b, final int offs, final int len) throws IOException {
        return stream.read(b, offs, len);
    }

    @Override
	public void close() throws IOException {
        stream.close();
    }

    @Override
	public boolean readBoolean() throws IOException {
        return stream.readBoolean();
    }

    @Override
	public byte readByte() throws IOException {
        return stream.readByte();
    }

    @Override
	public char readChar() throws IOException {
        return stream.readChar();
    }

    @Override
	public double readDouble() throws IOException {
        return stream.readDouble();
    }

    @Override
	public float readFloat() throws IOException {
        return stream.readFloat();
    }

    @Override
	public int readInt() throws IOException {
        return stream.readInt();
    }

    @Override
	public String readLine() throws IOException {
        final StringBuilder input = new StringBuilder();
        int c = -1;
        boolean eol = false;

        while (!eol) {
            switch (c = read()) {
                case -1:
                case '\n':
                    eol = true;
                    break;
                case '\r':
                    eol = true;
                    mark(1);
                    if ((read()) != '\n')
                        reset();
                    break;
                default:
                    input.append((char) c);
                    break;
            }
        }

        if ((c == -1) && (input.length() == 0))
            return null;

        return input.toString();
    }

    @Override
	public long readLong() throws IOException {
        return stream.readLong();
    }

    @Override
	public short readShort() throws IOException {
        return stream.readShort();
    }

    @Override
	public int readUnsignedByte() throws IOException {
        return stream.readUnsignedByte();
    }

    @Override
	public int readUnsignedShort() throws IOException {
        return stream.readUnsignedShort();
    }

    @Override
	public String readUTF() throws IOException {
        return stream.readUTF();
    }

    @Override
	public int skipBytes(final int n) throws IOException {
        return stream.skipBytes(n);
    }

    @Override
	public long length() throws IOException {
        throw new JMACException("Unsupported Method");
    }

    @Override
	public void seek(final long pos) throws IOException {
        throw new JMACException("Unsupported Method");
    }

    @Override
	public long getFilePointer() throws IOException {
        throw new JMACException("Unsupported Method");
    }

    public void setLength(final long newLength) throws IOException {
        throw new JMACException("Unsupported Method");
    }

    public void write(final byte[] b, final int off, final int len) throws IOException {
        throw new JMACException("Unsupported Method");
    }

    @Override
	public boolean isLocal() {
        return stream == null;
    }

    @Override
	public String getFilename() {
        return name;
    }
}
