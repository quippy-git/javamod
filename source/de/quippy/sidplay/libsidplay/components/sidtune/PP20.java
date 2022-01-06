/**
 *                PowerPacker (AMIGA) "PP20" format decompressor.
 *              Copyright (C) Michael Schwendt <mschwendt@yahoo.com>
 *              ----------------------------------------------------
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

import de.quippy.sidplay.libsidplay.components.sidtune.SidTune.Decompressed;

public class PP20 {

	public PP20() {
		statusString = _pp20_txt_uncompressed;
	}

	public boolean isCompressed(final short[] /* const void* */source,
			final int /* const udword_ppt */size) {
		// Check minimum input size, PP20 ID, and efficiency table.
		if (size < 8) {
			return false;
		}
		// We hope that every file with a valid signature and a valid
		// efficiency table is PP-compressed actually.
		final short[] idPtr = source;
		if (!new String(new byte[] { (byte) idPtr[0], (byte) idPtr[1],
				(byte) idPtr[2], (byte) idPtr[3] }).equals(PP_ID)) {
			statusString = _pp20_txt_uncompressed;
			return false;
		}
		return checkEfficiency(source, 4);
	}

	/**
	 * If successful, allocates a new buffer containing the uncompresse data and
	 * returns the uncompressed length. Else, returns 0.
	 * 
	 * @return
	 */
	public int /* udword_ppt */decompress(
			final short[] /* const void* */source, int /* udword_ppt */size,
			final Decompressed decomp) {
		this.source = source;
		globalError = false; // assume no error

		readPtr = 0;

		if (!isCompressed(source, size)) {
			return 0;
		}

		// Uncompressed size is stored at end of source file.
		// Backwards decompression.
		readPtr += (size - 4);

		int /* udword_ppt */lastDword = readBEdword(source, readPtr);
		// Uncompressed length in bits 31-8 of last dword.
		int /* udword_ppt */outputLen = lastDword >> 8;

		// Allocate memory for output data.
		dest = new short /* ubyte_ppt */[outputLen];

		// Lowest dest. address for range-checks.
		// Put destptr to end of uncompressed data.
		writePtr = outputLen;

		// Read number of unused bits in 1st data dword
		// from lowest bits 7-0 of last dword.
		bits = 32 - (lastDword & 0xFF);

		// Main decompression loop.
		bytesTOdword();
		if (bits != 32)
			current >>= (32 - bits);
		do {
			if (readBits(1) == 0)
				bytes();
			if (writePtr > 0)
				sequence();
			if (globalError) {
				// statusString already set.
				outputLen = 0; // unsuccessful decompression
				break;
			}
		} while (writePtr > 0);

		// Finished.

		if (outputLen > 0) // successful
		{
			decomp.destBufRef = new short[dest.length];
			// Free any previously existing destination buffer.
			System.arraycopy(dest, 0, decomp.destBufRef, 0, dest.length);
		}

		return outputLen;
	}

	public final String getStatusString() {
		return statusString;
	}

	private boolean checkEfficiency(final short[] source, int pos) {
		final int /* udword_ppt */PP_BITS_FAST = 0x09090909;
		final int /* udword_ppt */PP_BITS_MEDIOCRE = 0x090a0a0a;
		final int /* udword_ppt */PP_BITS_GOOD = 0x090a0b0b;
		final int /* udword_ppt */PP_BITS_VERYGOOD = 0x090a0c0c;
		final int /* udword_ppt */PP_BITS_BEST = 0x090a0c0d;

		// Copy efficiency table.
		System.arraycopy((short[] /* ubyte_ppt* */) source, pos, efficiency,
				0, 4);
		int /* udword_ppt */eff = readBEdword(efficiency, 0);
		if ((eff != PP_BITS_FAST) && (eff != PP_BITS_MEDIOCRE)
				&& (eff != PP_BITS_GOOD) && (eff != PP_BITS_VERYGOOD)
				&& (eff != PP_BITS_BEST)) {
			statusString = _pp20_txt_unrecognized;
			return false;
		}

		// Define string describing compression encoding used.
		switch (eff) {
		case PP_BITS_FAST:
			statusString = _pp20_txt_fast;
			break;
		case PP_BITS_MEDIOCRE:
			statusString = _pp20_txt_mediocre;
			break;
		case PP_BITS_GOOD:
			statusString = _pp20_txt_good;
			break;
		case PP_BITS_VERYGOOD:
			statusString = _pp20_txt_verygood;
			break;
		case PP_BITS_BEST:
			statusString = _pp20_txt_best;
			break;
		}

		return true;
	}

	private void bytesTOdword() {
		readPtr -= 4;
		if (readPtr < 0) {
			statusString = _pp20_txt_packeddatacorrupt;
			globalError = true;
		} else {
			current = readBEdword(source, readPtr);
		}
	}

	private int /* udword_ppt */readBits(int count) {
		int /* udword_ppt */data = 0;
		// read 'count' bits of packed data
		for (; count > 0; count--) {
			// equal to shift left
			data += data;
			// merge bit 0
			data |= (current & 1);
			current >>= 1;
			if (--bits == 0) {
				bytesTOdword();
				bits = 32;
			}
		}
		return data;
	}

	private void bytes() {
		int /* udword_ppt */count, add;
		count = (add = readBits(2));
		while (add == 3) {
			add = readBits(2);
			count += add;
		}
		for (++count; count > 0; count--) {
			if (writePtr > 0) {
				dest[--writePtr] = (short /* ubyte_ppt */) readBits(8);
			} else {
				statusString = _pp20_txt_packeddatacorrupt;
				globalError = true;
			}
		}
	}

	private void sequence() {
		int /* udword_ppt */offset, add;
		int /* udword_ppt */length = readBits(2); // is length-2
		int offsetBitLen = (int) efficiency[length];
		length += 2;
		if (length != 5)
			offset = readBits(offsetBitLen);
		else {
			if (readBits(1) == 0)
				offsetBitLen = 7;
			offset = readBits(offsetBitLen);
			add = readBits(3);
			length += add;
			while (add == 7) {
				add = readBits(3);
				length += add;
			}
		}
		for (; length > 0; length--) {
			if (writePtr > 0) {
				--writePtr;
				dest[writePtr] = dest[writePtr + 1 + offset];
			} else {
				statusString = _pp20_txt_packeddatacorrupt;
				globalError = true;
			}
		}
	}

	private static final String _pp20_txt_packeddatacorrupt = "PowerPacker: Packed data is corrupt";
	private static final String _pp20_txt_unrecognized = "PowerPacker: Unrecognized compression method";
	private static final String _pp20_txt_uncompressed = "Not compressed with PowerPacker (PP20)";
	private static final String _pp20_txt_fast = "PowerPacker: fast compression";
	private static final String _pp20_txt_mediocre = "PowerPacker: mediocre compression";
	private static final String _pp20_txt_good = "PowerPacker: good compression";
	private static final String _pp20_txt_verygood = "PowerPacker: very good compression";
	private static final String _pp20_txt_best = "PowerPacker: best compression";

	private static final String PP_ID = "PP20";

	private short /* ubyte_ppt */efficiency[] = new short[4];

	private short[] source;
	short[] /* ubyte_ppt* */dest;

	private int /* ubyte_ppt* */readPtr;

	private int /* ubyte_ppt* */writePtr;

	/**
	 * compressed data longword
	 */
	private int /* udword_ppt */current;
	/**
	 * number of bits in 'current' to evaluate
	 */
	private int bits;

	/**
	 * exception-free version of code
	 */
	private boolean globalError;

	private String statusString;

	/**
	 * Read a big-endian 32-bit word from four bytes in memory. No
	 * endian-specific optimizations applied.
	 * 
	 * @param ptr
	 * @param pos
	 * @return
	 */
	int /* udword_ppt */readBEdword(final short[] /* ubyte_ppt[4] */ptr,
			int pos) {
		return (((((short /* udword_ppt */) ptr[pos + 0]) << 24)
				+ (((short /* udword_ppt */) ptr[pos + 1]) << 16)
				+ (((short /* udword_ppt */) ptr[pos + 2]) << 8) + ((short /* udword_ppt */) ptr[pos + 3])) << 0);
	}
}
