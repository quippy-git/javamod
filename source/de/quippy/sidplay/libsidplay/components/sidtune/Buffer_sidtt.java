/**
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
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package de.quippy.sidplay.libsidplay.components.sidtune;

public class Buffer_sidtt {
	public Buffer_sidtt() {
		dummy = (0);
		kill();
	}

	public Buffer_sidtt(short[] inBuf, int /* uint_least32_t */inLen) {
		dummy = (0);
		kill();
		if (inBuf != null && inLen != 0) {
			buf = inBuf;
			bufLen = inLen;
		}
	}

	public boolean assign(short[] newBuf, int /* uint_least32_t */newLen) {
		erase();
		buf = newBuf;
		bufLen = newLen;
		return (buf != null);
	}

	public final short[] get() {
		return buf;
	}

	public final int /* uint_least32_t */len() {
		return bufLen;
	}

	public short[] xferPtr() {
		short[] tmpBuf = buf;
		buf = null;
		return tmpBuf;
	}

	public int /* uint_least32_t */xferLen() {
		int /* uint_least32_t */tmpBufLen = bufLen;
		bufLen = 0;
		return tmpBufLen;
	}

	public short opAt(int /* uint_least32_t */index) {
		if (index < bufLen)
			return buf[index];
		else
			return dummy;
	}

	public final boolean isEmpty() {
		return (buf == null);
	}

	public void erase() {
		if (buf != null && bufLen != 0) {
			buf = null;
		}
		kill();
	}

	private short[] buf;

	private int /* uint_least32_t */bufLen;

	private short dummy;

	private void kill() {
		buf = null;
		bufLen = 0;
	}

}
