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


/**
 * Author: Dmitry Vaguine
 * Date: 04.03.2004
 * Time: 14:51:31
 */
public class ByteArrayWriter {

    private byte[] data = null;
    private int index = 0;

    public ByteArrayWriter() {
    }

    public ByteArrayWriter(final int size) {
        this.data = new byte[size];
    }

    public ByteArrayWriter(final byte[] data) {
        this.data = data;
    }

    public void reset(final int size) {
        this.data = new byte[size];
        index = 0;
    }

    public byte[] getBytes() {
        return data;
    }

    public void writeUnsignedByte(final short value) {
        if (value < 0 || value > 255)
            throw new JMACException("Wrong Value");
        data[index++] = (byte) value;
    }

    public void writeUnsignedShort(final int value) {
        if (value < 0 || value > 65535)
            throw new JMACException("Wrong Value");
        data[index++] = (byte) (value & 0xff);
        data[index++] = (byte) (value >> 8);
    }

    public void writeUnsignedInt(final long value) {
        if (value < 0 || value > 4294967295L)
            throw new JMACException("Wrong Value");
        data[index++] = (byte) (value & 0xff);
        data[index++] = (byte) ((value >> 8) & 0xff);
        data[index++] = (byte) ((value >> 16) & 0xff);
        data[index++] = (byte) ((value >> 24) & 0xff);
    }

    public void writeByte(final byte value) {
        data[index++] = value;
    }

    public void writeShort(final short value) {
        data[index++] = (byte) (value & 0xff);
        data[index++] = (byte) (value >> 8);
    }

    public void writeInt(final int value) {
        data[index++] = (byte) (value & 0xff);
        data[index++] = (byte) ((value >> 8) & 0xff);
        data[index++] = (byte) ((value >> 16) & 0xff);
        data[index++] = (byte) ((value >> 24) & 0xff);
    }

    public void writeBytes(final byte[] buf) {
        writeBytes(buf, 0, buf.length);
    }

    public void writeBytes(final byte[] buf, final int off, final int len) {
        System.arraycopy(buf, off, data, index, len);
        index += len;
    }

    public void writeString(final String value, final int size, final String encoding) {
        try {
            final byte[] bytes = value.getBytes(encoding);
            if (bytes.length != size)
                throw new JMACException("Wrong Value");
            System.arraycopy(bytes, 0, data, index, size);
        } catch (final java.io.UnsupportedEncodingException e) {
            throw new JMACException("Unsupported Encoding", e);
        }
        index += size;
    }

    public void writeZString(final String value, final String encoding) {
        try {
            final byte[] bytes = value.getBytes(encoding);
            System.arraycopy(bytes, 0, data, index, bytes.length);
            index += bytes.length;
        } catch (final java.io.UnsupportedEncodingException e) {
            throw new JMACException("Unsupported Encoding", e);
        }
        data[index++] = 0;
    }

}
