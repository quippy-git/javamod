/**
 *                           Important endian functions
 *                           --------------------------
 *  begin                : Mon Jul 3 2000
 *  copyright            : (C) 2000 by Simon White
 *  email                : s_a_white@email.com
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; either version 3 of the License, or
 *   (at your option) any later version.
 *
 * @author Ken H�ndel
 *
 */
package de.quippy.sidplay.libsidplay.common;

/**
 * NOTE: The optimizations in this file rely on the structure of memory e.g. 2
 * shorts being contained in 1 long. Although these sizes are checked to make
 * sure the optimization is ok, gcc 2.96 (and above) introduced better
 * Optimizations. This results in caching of values in internal registers and
 * therefore writes to ram through the aliases not being reflected in the CPU
 * regs. The use of the volatile keyword fixes this. <BR>
 * Labeling:
 * <UL>
 * <LI>0 - Low Word / Low Byte
 * <LI>1 - Low Word / High Byte
 * <LI>2 - High Word / Low Byte
 * <LI>3 - High Word / High Byte
 * </UL>
 * 
 * @author Ken H�ndel
 * 
 */
public class SIDEndian {

	/**
	 * byte-order: HIHI..3210..LO
	 */
	public static final int SID_WORDS_BIGENDIAN = 0;

	/**
	 * byte-order: LO..0123..HIHI
	 */
	public static final int SID_WORDS_LITTLEENDIAN = 1;

	/**
	 * SID_WORDS_LITTLEENDIAN or SID_WORDS_BIGENDIAN
	 */
	public static final int SID_WORDS = SID_WORDS_LITTLEENDIAN;

	// /////////////////////////////////////////////////////////////////
	// INT16 FUNCTIONS
	// /////////////////////////////////////////////////////////////////

	/**
	 * Set the lo byte (8 bit) in a word (16 bit)
	 * 
	 * @param word
	 * @param thebyte
	 * @return
	 */
	public static final int /* uint_least16_t */endian_16lo8(
			int /* uint_least16_t */word, short /* uint8_t */thebyte) {
		word &= 0xff00;
		word |= thebyte;
		return word;
	}

	/**
	 * Get the lo byte (8 bit) in a word (16 bit)
	 * 
	 * @param word
	 * @return
	 */
	public static final short /* uint8_t */endian_16lo8(
			int /* uint_least16_t */word) {
		return (short /* uint8_t */) (word & 0xff);
	}

	/**
	 * Set the hi byte (8 bit) in a word (16 bit)
	 * 
	 * @param word
	 * @param thebyte
	 * @return
	 */
	public static final int /* uint_least16_t */endian_16hi8(
			int /* uint_least16_t */word, short /* uint8_t */thebyte) {
		word &= 0x00ff;
		word |= (int /* uint_least16_t */) thebyte << 8;
		return word;
	}

	/**
	 * Get the hi byte (8 bit) in a word (16 bit)
	 * 
	 * @param word
	 * @return
	 */
	public static final short /* uint8_t */endian_16hi8(
			int /* uint_least16_t */word) {
		return (short /* uint8_t */) ((word >> 8) & 0xff);
	}

	/**
	 * Swap word endian.
	 * 
	 * @param word
	 * @return
	 */
	public static final int /* uint_least16_t */endian_16swap8(
			int /* uint_least16_t */word) {
		short /* uint8_t */lo = endian_16lo8(word);
		short /* uint8_t */hi = endian_16hi8(word);
		word = 0;
		word = endian_16lo8(word, hi);
		word |= endian_16hi8(word, lo);
		return word;
	}

	/**
	 * Convert high-byte and low-byte to 16-bit word.
	 * 
	 * @param hi
	 * @param lo
	 * @return
	 */
	public static final int /* uint_least16_t */endian_16(
			short /* uint8_t */hi, short /* uint8_t */lo) {
		int /* uint_least16_t */word = 0;
		word = endian_16lo8(word, lo);
		word |= endian_16hi8(word, hi);
		return word;
	}

	/**
	 * Convert high-byte and low-byte to 16-bit little endian word.
	 * 
	 * @param ptr
	 * @param pos
	 * @return
	 */
	public static final void endian_16(short /* uint8_t */ptr[], int pos,
			int /* uint_least16_t */word) {
//		if (SID_WORDS == SID_WORDS_BIGENDIAN) {
//			ptr[pos + 0] = endian_16hi8(word);
//			ptr[pos + 1] = endian_16lo8(word);
//		} else {
			ptr[pos + 0] = endian_16lo8(word);
			ptr[pos + 1] = endian_16hi8(word);
//		}
	}

	public static final void endian_16(char ptr[], int pos,
			int /* uint_least16_t */word) {
		short[] newptr = new short /* uint8_t */[] {
				(short) ptr[pos + 0], (short) ptr[pos + 1] };
		endian_16(newptr, 0, word);
		ptr[pos + 0] = (char) newptr[0];
		ptr[pos + 1] = (char) newptr[1];
	}

	/**
	 * Convert high-byte and low-byte to 16-bit little endian word.
	 * 
	 * @param ptr
	 * @param pos
	 * @return
	 */
	public static final int /* uint_least16_t */endian_little16(
			final short /* uint8_t */ptr[], int pos) {
		return endian_16(ptr[pos + 1], ptr[pos + 0]);
	}

	/**
	 * Write a little-endian 16-bit word to two bytes in memory.
	 * 
	 * @param ptr
	 * @param pos
	 * @param word
	 */
	public static final void endian_little16(short /* uint8_t */ptr[],
			int pos, int /* uint_least16_t */word) {
		ptr[pos + 0] = endian_16lo8(word);
		ptr[pos + 1] = endian_16hi8(word);
	}

	/**
	 * Convert high-byte and low-byte to 16-bit big endian word.
	 * 
	 * @param ptr
	 * @return
	 */
	public static final int /* uint_least16_t */endian_big16(
			final short /* uint8_t */ptr[], int pos) {
		return endian_16(ptr[pos + 0], ptr[pos + 1]);
	}

	/**
	 * Write a big-endian 16-bit word to two bytes in memory.
	 * 
	 * @param ptr
	 * @param pos
	 * @param word
	 */
	public static final void endian_big16(short /* uint8_t */ptr[], int pos,
			int /* uint_least16_t */word) {
		ptr[pos + 0] = endian_16hi8(word);
		ptr[pos + 1] = endian_16lo8(word);
	}

	// /////////////////////////////////////////////////////////////////
	// INT32 FUNCTIONS
	// /////////////////////////////////////////////////////////////////

	/**
	 * Set the lo word (16bit) in a dword (32 bit)
	 * 
	 * @param dword
	 * @param word
	 * @return
	 */
	public static final long /* uint_least32_t */endian_32lo16(
			long /* uint_least32_t */dword, int /* uint_least16_t */word) {
		dword &= (long /* uint_least32_t */) 0xffff0000;
		dword |= word;
		return dword;
	}

	/**
	 * Get the lo word (16bit) in a dword (32 bit)
	 * 
	 * @param dword
	 * @return
	 */
	public static final int /* uint_least16_t */endian_32lo16(
			long /* uint_least32_t */dword) {
		return (int /* uint_least16_t */) dword & 0xffff;
	}

	/**
	 * Set the hi word (16bit) in a dword (32 bit)
	 * 
	 * @param dword
	 * @param word
	 * @return
	 */
	public static final long /* uint_least32_t */endian_32hi16(
			long /* uint_least32_t */dword, int /* uint_least16_t */word) {
		dword &= (long /* uint_least32_t */) 0x0000ffff;
		dword |= (long /* uint_least32_t */) word << 16;
		return dword;
	}

	/**
	 * Get the hi word (16bit) in a dword (32 bit)
	 * 
	 * @param dword
	 * @return
	 */
	public static final int /* uint_least16_t */endian_32hi16(
			long /* uint_least32_t */dword) {
		return (int /* uint_least16_t */) dword >> 16;
	}

	/**
	 * Set the lo byte (8 bit) in a dword (32 bit)
	 * 
	 * @param dword
	 * @param theByte
	 * @return
	 */
	public static final long /* uint_least32_t */endian_32lo8(
			long /* uint_least32_t */dword, short /* uint8_t */theByte) {
		dword &= (long /* uint_least32_t */) 0xffffff00;
		dword |= (long /* uint_least32_t */) theByte;
		return dword;
	}

	/**
	 * Get the lo byte (8 bit) in a dword (32 bit)
	 * 
	 * @param dword
	 * @return
	 */
	public static final short /* uint8_t */endian_32lo8(
			long /* uint_least32_t */dword) {
		return (short /* uint8_t */) (dword & 0xff);
	}

	/**
	 * Set the hi byte (8 bit) in a dword (32 bit)
	 * 
	 * @param dword
	 * @param theByte
	 * @return
	 */
	public static final long /* uint_least32_t */endian_32hi8(
			long /* uint_least32_t */dword, short /* uint8_t */theByte) {
		dword &= (long /* uint_least32_t */) 0xffff00ff;
		dword |= (long /* uint_least32_t */) theByte << 8;
		return dword;
	}

	/**
	 * Get the hi byte (8 bit) in a dword (32 bit)
	 * 
	 * @param dword
	 * @return
	 */
	public static final short /* uint8_t */endian_32hi8(
			long /* uint_least32_t */dword) {
		return (short /* uint8_t */) ((dword >> 8) & 0xff);
	}

	/**
	 * Swap hi and lo words endian in 32 bit dword.
	 * 
	 * @param dword
	 * @return
	 */
	public static final long /* uint_least32_t */endian_32swap16(
			long /* uint_least32_t */dword) {
		int /* uint_least16_t */lo = endian_32lo16(dword);
		int /* uint_least16_t */hi = endian_32hi16(dword);
		/* uint_least32_t */dword = 0;
		dword |= endian_32lo16(dword, hi);
		dword |= endian_32hi16(dword, lo);
		return dword;
	}

	/**
	 * Swap word endian.
	 * 
	 * @param dword
	 * @return
	 */
	public static final long /* uint_least32_t */endian_32swap8(
			long /* uint_least32_t */dword) {
		int /* uint_least16_t */lo, hi;
		lo = endian_32lo16(dword);
		hi = endian_32hi16(dword);
		lo = endian_16swap8(lo);
		hi = endian_16swap8(hi);
		dword = 0;
		dword |= endian_32lo16(dword, hi);
		dword |= endian_32hi16(dword, lo);
		return dword;
	}

	/**
	 * Convert high-byte and low-byte to 32-bit word.
	 * 
	 * @param hihi
	 * @param hilo
	 * @param hi
	 * @param lo
	 * @return
	 */
	public static final long /* uint_least32_t */endian_32(
			short /* uint8_t */hihi, short /* uint8_t */hilo,
			short /* uint8_t */hi, short /* uint8_t */lo) {
		long /* uint_least32_t */dword = 0;
		int /* uint_least16_t */word = 0;
		dword = endian_32lo8(dword, lo);
		dword |= endian_32hi8(dword, hi);
		word = endian_16lo8(word, hilo);
		word |= endian_16hi8(word, hihi);
		dword |= endian_32hi16(dword, word);
		return dword;
	}

	/**
	 * Convert high-byte and low-byte to 32-bit little endian word.
	 * 
	 * @param ptr
	 * @param pos
	 * @return
	 */
	public static final long /* uint_least32_t */endian_little32(
			final short /* uint8_t */ptr[], int pos) {
		return endian_32(ptr[pos + 3], ptr[pos + 2], ptr[pos + 1], ptr[pos + 0]);
	}

	/**
	 * Write a little-endian 32-bit word to four bytes in memory.
	 * 
	 * @param ptr
	 * @param pos
	 * @param dword
	 */
	public static final void endian_little32(short /* uint8_t */ptr[],
			int pos, long /* uint_least32_t */dword) {
		int /* uint_least16_t */word = 0;
		ptr[pos + 0] = endian_32lo8(dword);
		ptr[pos + 1] = endian_32hi8(dword);
		word = endian_32hi16(dword);
		ptr[pos + 2] = endian_16lo8(word);
		ptr[pos + 3] = endian_16hi8(word);
	}

	/**
	 * Convert high-byte and low-byte to 32-bit big endian word.
	 * 
	 * @param ptr
	 * @param pos
	 * @return
	 */
	public static final long /* uint_least32_t */endian_big32(
			final short /* uint8_t */ptr[], int pos) {
		return endian_32(ptr[pos + 0], ptr[pos + 1], ptr[pos + 2], ptr[pos + 3]);
	}

	/**
	 * Write a big-endian 32-bit word to four bytes in memory.
	 * 
	 * @param ptr
	 * @param pos
	 * @param dword
	 */
	public static final void endian_big32(short /* uint8_t */ptr[], int pos,
			long /* uint_least32_t */dword) {
		int /* uint_least16_t */word = 0;
		word = endian_32hi16(dword);
		ptr[pos + 1] = endian_16lo8(word);
		ptr[pos + 0] = endian_16hi8(word);
		ptr[pos + 2] = endian_32hi8(dword);
		ptr[pos + 3] = endian_32lo8(dword);
	}

}
