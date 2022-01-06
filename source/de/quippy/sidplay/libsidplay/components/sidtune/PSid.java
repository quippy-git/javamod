/**
 *                             PlaySID one-file format support.
 *                             --------------------------------
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

import static de.quippy.sidplay.libsidplay.common.SIDEndian.endian_big16;
import static de.quippy.sidplay.libsidplay.common.SIDEndian.endian_big32;
import static de.quippy.sidplay.libsidplay.components.sidtune.ISidTuneCfg.SIDTUNE_PSID2NG;
import static de.quippy.sidplay.libsidplay.components.sidtune.SidTune.SIDTUNE_CLOCK_ANY;
import static de.quippy.sidplay.libsidplay.components.sidtune.SidTune.SIDTUNE_CLOCK_NTSC;
import static de.quippy.sidplay.libsidplay.components.sidtune.SidTune.SIDTUNE_CLOCK_PAL;
import static de.quippy.sidplay.libsidplay.components.sidtune.SidTune.SIDTUNE_CLOCK_UNKNOWN;
import static de.quippy.sidplay.libsidplay.components.sidtune.SidTune.SIDTUNE_COMPATIBILITY_BASIC;
import static de.quippy.sidplay.libsidplay.components.sidtune.SidTune.SIDTUNE_COMPATIBILITY_C64;
import static de.quippy.sidplay.libsidplay.components.sidtune.SidTune.SIDTUNE_COMPATIBILITY_PSID;
import static de.quippy.sidplay.libsidplay.components.sidtune.SidTune.SIDTUNE_COMPATIBILITY_R64;
import static de.quippy.sidplay.libsidplay.components.sidtune.SidTune.SIDTUNE_MAX_SONGS;
import static de.quippy.sidplay.libsidplay.components.sidtune.SidTune.SIDTUNE_SIDMODEL_6581;
import static de.quippy.sidplay.libsidplay.components.sidtune.SidTune.SIDTUNE_SIDMODEL_8580;
import static de.quippy.sidplay.libsidplay.components.sidtune.SidTune.SIDTUNE_SIDMODEL_UNKNOWN;
import static de.quippy.sidplay.libsidplay.components.sidtune.SidTune.SIDTUNE_SPEED_CIA_1A;
import static de.quippy.sidplay.libsidplay.components.sidtune.SidTune.LoadStatus.LOAD_ERROR;
import static de.quippy.sidplay.libsidplay.components.sidtune.SidTune.LoadStatus.LOAD_NOT_MINE;
import static de.quippy.sidplay.libsidplay.components.sidtune.SidTune.LoadStatus.LOAD_OK;

import java.io.IOException;
import java.io.OutputStream;

import de.quippy.sidplay.libsidplay.components.sidtune.SidTune.LoadStatus;


public class PSid {

	public static final int PSID_ID = 0x50534944;

	public static final int RSID_ID = 0x52534944;

	/**
	 * Header has been extended for 'RSID' format<BR>
	 * 
	 * The following changes are present:
	 * <UL>
	 * <LI> id = 'RSID'
	 * <LI> version = 2 only
	 * <LI> play, load and speed reserved 0
	 * <LI> psid specific flag reserved 0
	 * <LI> init cannot be under ROMS/IO
	 * <LI> load cannot be less than 0x0801 (start of basic)
	 * </UL>
	 * all values big-endian
	 * 
	 * @author Ken H�ndel
	 * 
	 */
	public static class PHeader {

		public static final int SIZE = 124;

		public PHeader(short[] s, int offset) {
			for (int i = 0; i < 4; i++) {
				id[i] = s[offset++];
			}
			for (int i = 0; i < 2; i++) {
				version[i] = s[offset++];
			}
			for (int i = 0; i < 2; i++) {
				data[i] = s[offset++];
			}
			for (int i = 0; i < 2; i++) {
				load[i] = s[offset++];
			}
			for (int i = 0; i < 2; i++) {
				init[i] = s[offset++];
			}
			for (int i = 0; i < 2; i++) {
				play[i] = s[offset++];
			}
			for (int i = 0; i < 2; i++) {
				songs[i] = s[offset++];
			}
			for (int i = 0; i < 2; i++) {
				start[i] = s[offset++];
			}
			for (int i = 0; i < 4; i++) {
				speed[i] = s[offset++];
			}
			for (int i = 0; i < 32; i++) {
				name[i] = (char) s[offset++];
			}
			for (int i = 0; i < 32; i++) {
				author[i] = (char) s[offset++];
			}
			for (int i = 0; i < 32; i++) {
				released[i] = (char) s[offset++];
			}
			for (int i = 0; i < 2; i++) {
				flags[i] = s[offset++];
			}
			relocStartPage = s[offset++];
			relocPages = s[offset++];
			for (int i = 0; i < 2; i++) {
				reserved[i] = s[offset++];
			}
		}

		public PHeader() {
		}

		/**
		 * 'PSID' (ASCII)
		 */
		public short[] id = new short[4];

		/**
		 * 0x0001 or 0x0002
		 */
		public short /* uint8_t */version[] = new short[2];

		/**
		 * 16-bit offset to binary data in file
		 */
		public short /* uint8_t */data[] = new short[2];

		/**
		 * 16-bit C64 address to load file to
		 */
		public short /* uint8_t */load[] = new short[2];

		/**
		 * 16-bit C64 address of init subroutine
		 */
		public short /* uint8_t */init[] = new short[2];

		/**
		 * 16-bit C64 address of play subroutine
		 */
		public short /* uint8_t */play[] = new short[2];

		/**
		 * number of songs
		 */
		public short /* uint8_t */songs[] = new short[2];

		/**
		 * start song out of [1..256]
		 */
		public short /* uint8_t */start[] = new short[2];

		/**
		 * 32-bit speed info:<BR>
		 * 
		 * bit: 0=50 Hz, 1=CIA 1 Timer A (default: 60 Hz)
		 */
		public short /* uint8_t */speed[] = new short[4];

		/**
		 * ASCII strings, 31 characters long and terminated by a trailing zero
		 */
		public char name[] = new char[32];

		/**
		 * ASCII strings, 31 characters long and terminated by a trailing zero
		 */
		public char author[] = new char[32];

		/**
		 * ASCII strings, 31 characters long and terminated by a trailing zero
		 */
		public char released[] = new char[32];

		/**
		 * only version 0x0002
		 */
		public short /* uint8_t */flags[] = new short[2];

		/**
		 * only version 0x0002B
		 */
		public short /* uint8_t */relocStartPage;

		/**
		 * only version 0x0002B
		 */
		public short /* uint8_t */relocPages;

		/**
		 * only version 0x0002
		 */
		public short /* uint8_t */reserved[] = new short[2];

		public short[] getArray() {
			return new short[] {
					id[0], id[1], id[2], id[3], version[0], version[1],
					data[0], data[1], load[0], load[1], init[0], init[1],
					play[0], play[1], songs[0], songs[1], start[0], start[1],
					speed[0], speed[1], speed[2], speed[3], (short) name[0],
					(short) name[1], (short) name[2], (short) name[3],
					(short) name[4], (short) name[5], (short) name[6],
					(short) name[7], (short) name[8], (short) name[9],
					(short) name[10], (short) name[11], (short) name[12],
					(short) name[13], (short) name[14], (short) name[13],
					(short) name[16], (short) name[17], (short) name[18],
					(short) name[19], (short) name[20], (short) name[21],
					(short) name[22], (short) name[23], (short) name[24],
					(short) name[25], (short) name[26], (short) name[27],
					(short) name[28], (short) name[29], (short) name[30],
					(short) name[31],

					(short) author[0], (short) author[1], (short) author[2],
					(short) author[3], (short) author[4], (short) author[5],
					(short) author[6], (short) author[7], (short) author[8],
					(short) author[9], (short) author[10], (short) author[11],
					(short) author[12], (short) author[13], (short) author[14],
					(short) author[13], (short) author[16], (short) author[17],
					(short) author[18], (short) author[19], (short) author[20],
					(short) author[21], (short) author[22], (short) author[23],
					(short) author[24], (short) author[25], (short) author[26],
					(short) author[27], (short) author[28], (short) author[29],
					(short) author[30], (short) author[31],

					(short) released[0], (short) released[1],
					(short) released[2], (short) released[3],
					(short) released[4], (short) released[5],
					(short) released[6], (short) released[7],
					(short) released[8], (short) released[9],
					(short) released[10], (short) released[11],
					(short) released[12], (short) released[13],
					(short) released[14], (short) released[13],
					(short) released[16], (short) released[17],
					(short) released[18], (short) released[19],
					(short) released[20], (short) released[21],
					(short) released[22], (short) released[23],
					(short) released[24], (short) released[25],
					(short) released[26], (short) released[27],
					(short) released[28], (short) released[29],
					(short) released[30], (short) released[31],

					flags[0], flags[1], relocStartPage, relocPages,
					reserved[0], reserved[1], };
		}

	}

	//
	// PSID_SPECIFIC and PSID_BASIC are mutually exclusive
	//

	public static final int PSID_MUS = 1 << 0;

	public static final int PSID_SPECIFIC = 1 << 1;

	public static final int PSID_BASIC = 1 << 1;

	public static final int PSID_CLOCK = 3 << 2;

	public static final int PSID_SIDMODEL = 3 << 4;

	//
	// These are also used in the emulator engine!
	//

	public static final int PSID_CLOCK_UNKNOWN = 0;

	public static final int PSID_CLOCK_PAL = 1 << 2;

	public static final int PSID_CLOCK_NTSC = 1 << 3;

	public static final int PSID_CLOCK_ANY = (PSID_CLOCK_PAL | PSID_CLOCK_NTSC);

	//
	// SID model
	//

	public static final int PSID_SIDMODEL_UNKNOWN = 0;

	public static final int PSID_SIDMODEL_6581 = 1 << 4;

	public static final int PSID_SIDMODEL_8580 = 1 << 5;

	public static final int PSID_SIDMODEL_ANY = PSID_SIDMODEL_6581
			| PSID_SIDMODEL_8580;

	//
	// sidtune format errors
	//

	final static String _sidtune_format_psid = "PlaySID one-file format (PSID)";

	final static String _sidtune_format_rsid = "Real C64 one-file format (RSID)";

	final static String _sidtune_unknown_psid = "Unsupported PSID version";

	final static String _sidtune_unknown_rsid = "Unsupported RSID version";

	final static String _sidtune_truncated = "ERROR: File is most likely truncated";

	final static String _sidtune_invalid = "ERROR: File contains invalid data";

	final static int _sidtune_psid_maxStrLen = 31;

	private SidTune sidtune;

	private SidTuneInfo info;

	public PSid(SidTune sidtune) {
		this.sidtune = sidtune;
		this.info = sidtune.info;
	}

	protected final LoadStatus PSID_fileSupport(
			Buffer_sidtt /* Buffer_sidtt<const uint_least8_t>& */dataBuf) {
		short clock, compatibility;
		long /* uint_least32_t */speed;
		int /* uint_least32_t */bufLen = dataBuf.len();
		if (SIDTUNE_PSID2NG) {
			clock = SIDTUNE_CLOCK_UNKNOWN;
		} else {
			clock = info.clockSpeed;
		}
		compatibility = SIDTUNE_COMPATIBILITY_C64;

		// Require minimum size to allow access to the first few bytes.
		// Require a valid ID and version number.
		final PHeader pHeader = new PHeader(dataBuf.get(), 0);

		// File format check
		if (bufLen < 6)
			return LOAD_NOT_MINE;
		if (endian_big32((short[] /* const uint_least8_t* */) pHeader.id, 0) == PSID_ID) {
			switch (endian_big16(pHeader.version, 0)) {
			case 1:
				compatibility = SIDTUNE_COMPATIBILITY_PSID;
				// Deliberate run on
			case 2:
				break;
			default:
				info.formatString = _sidtune_unknown_psid;
				return LOAD_ERROR;
			}
			info.formatString = _sidtune_format_psid;
		} else if (endian_big32(
				(short[] /* const uint_least8_t* */) pHeader.id, 0) == RSID_ID) {
			if (endian_big16(pHeader.version, 0) != 2) {
				info.formatString = _sidtune_unknown_rsid;
				return LOAD_ERROR;
			}
			info.formatString = _sidtune_format_rsid;
			compatibility = SIDTUNE_COMPATIBILITY_R64;
		} else {
			return LOAD_NOT_MINE;
		}

		// Due to security concerns, input must be at least as long as version 1
		// header plus 16-bit C64 load address. That is the area which will be
		// accessed.
		if (bufLen < (PHeader.SIZE + 2)) {
			info.formatString = _sidtune_truncated;
			return LOAD_ERROR;
		}

		sidtune.fileOffset = endian_big16(pHeader.data, 0);
		info.loadAddr = endian_big16(pHeader.load, 0);
		info.initAddr = endian_big16(pHeader.init, 0);
		info.playAddr = endian_big16(pHeader.play, 0);
		info.songs = endian_big16(pHeader.songs, 0);
		info.startSong = endian_big16(pHeader.start, 0);
		info.sidChipBase1 = 0xd400;
		info.sidChipBase2 = 0;
		info.compatibility = compatibility;
		speed = endian_big32(pHeader.speed, 0);

		if (info.songs > SIDTUNE_MAX_SONGS) {
			info.songs = SIDTUNE_MAX_SONGS;
		}

		info.musPlayer = false;
		info.sidModel = SIDTUNE_SIDMODEL_UNKNOWN;
		info.relocPages = 0;
		info.relocStartPage = 0;
		if (endian_big16(pHeader.version, 0) >= 2) {
			int /* uint_least16_t */flags = endian_big16(pHeader.flags, 0);
			if ((flags & PSID_MUS) != 0) { // MUS tunes run at any speed
				clock = SIDTUNE_CLOCK_ANY;
				info.musPlayer = true;
			}

			if (SIDTUNE_PSID2NG) {
				// This flags is only available for the appropriate
				// file formats
				switch (compatibility) {
				case SIDTUNE_COMPATIBILITY_C64:
					if ((flags & PSID_SPECIFIC) != 0)
						info.compatibility = SIDTUNE_COMPATIBILITY_PSID;
					break;
				case SIDTUNE_COMPATIBILITY_R64:
					if ((flags & PSID_BASIC) != 0)
						info.compatibility = SIDTUNE_COMPATIBILITY_BASIC;
					break;
				}

				if ((flags & PSID_CLOCK_PAL) != 0)
					clock |= SIDTUNE_CLOCK_PAL;
				if ((flags & PSID_CLOCK_NTSC) != 0)
					clock |= SIDTUNE_CLOCK_NTSC;
				info.clockSpeed = clock;

				info.sidModel = SIDTUNE_SIDMODEL_UNKNOWN;
				if ((flags & PSID_SIDMODEL_6581) != 0)
					info.sidModel |= SIDTUNE_SIDMODEL_6581;
				if ((flags & PSID_SIDMODEL_8580) != 0)
					info.sidModel |= SIDTUNE_SIDMODEL_8580;

				info.relocStartPage = pHeader.relocStartPage;
				info.relocPages = pHeader.relocPages;
			} // SIDTUNE_PSID2NG
		}

		// Check reserved fields to force real c64 compliance
		// as required by the RSID specification
		if (compatibility == SIDTUNE_COMPATIBILITY_R64) {
			if ((info.loadAddr != 0) || (info.playAddr != 0) || (speed != 0)) {
				info.formatString = _sidtune_invalid;
				return LOAD_ERROR;
			}
			// Real C64 tunes appear as CIA
			speed = ~0;
		}
		// Create the speed/clock setting table.
		sidtune.convertOldStyleSpeedToTables(speed, clock);

		// Copy info strings, so they will not get lost.
		info.numberOfInfoStrings = 3;
		// Name
		int i;
		for ( i = 0; i < pHeader.name.length; i++) {
			if (pHeader.name[i]==0) {
				break;
			}
		}
		info.infoString[0] = sidtune.infoString[0] = new String(pHeader.name, 0, Math.min(i, _sidtune_psid_maxStrLen));
		// Author
		for ( i = 0; i < pHeader.author.length; i++) {
			if (pHeader.author[i]==0) {
				break;
			}
		}
		info.infoString[1] = sidtune.infoString[1] = new String(pHeader.author, 0, Math.min(i, _sidtune_psid_maxStrLen));
		// Released
		for ( i = 0; i < pHeader.released.length; i++) {
			if (pHeader.released[i]==0) {
				break;
			}
		}
		info.infoString[2] = sidtune.infoString[2] = new String(pHeader.released, 0, Math.min(i, _sidtune_psid_maxStrLen));

		if (info.musPlayer)
			return sidtune.MUS_load(dataBuf, false);
		return LOAD_OK;
	}

	protected boolean PSID_fileSupportSave(OutputStream fMyOut,
			final short[] /* uint_least8_t* */dataBuffer) {
		try {
			PHeader myHeader = new PHeader();
			endian_big32((short[] /* uint_least8_t* */) myHeader.id, 0, PSID_ID);
			endian_big16(myHeader.version, 0, 2);
			endian_big16(myHeader.data, 0, PHeader.SIZE);
			endian_big16(myHeader.songs, 0, info.songs);
			endian_big16(myHeader.start, 0, info.startSong);

			short /* uint_least32_t */speed = 0;
			//short check = 0;
			int /* uint_least32_t */maxBugSongs = ((info.songs <= 32) ? info.songs
					: 32);
			for (int /* uint_least32_t */s = 0; s < maxBugSongs; s++) {
				if (sidtune.songSpeed[s] == SIDTUNE_SPEED_CIA_1A)
				{
					speed |= (1 << s);
				}
				//check |= (1 << s);
			}
			endian_big32(myHeader.speed, 0, speed);

			int /* uint_least16_t */tmpFlags = 0;
			if (info.musPlayer) {
				endian_big16(myHeader.load, 0, 0);
				endian_big16(myHeader.init, 0, 0);
				endian_big16(myHeader.play, 0, 0);
				myHeader.relocStartPage = 0;
				myHeader.relocPages = 0;
				tmpFlags |= PSID_MUS;
			} else {
				endian_big16(myHeader.load, 0, 0);
				endian_big16(myHeader.init, 0, info.initAddr);
				myHeader.relocStartPage = info.relocStartPage;
				myHeader.relocPages = info.relocPages;

				switch (info.compatibility) {
				case SIDTUNE_COMPATIBILITY_BASIC:
					tmpFlags |= PSID_BASIC;
				case SIDTUNE_COMPATIBILITY_R64:
					endian_big32((short[] /* uint_least8_t* */) myHeader.id, 0,
							RSID_ID);
					endian_big16(myHeader.play, 0, 0);
					endian_big32(myHeader.speed, 0, 0);
					break;
				case SIDTUNE_COMPATIBILITY_PSID:
					tmpFlags |= PSID_SPECIFIC;
				default:
					endian_big16(myHeader.play, 0, info.playAddr);
					break;
				}
			}

			for (int /* uint */i = 0; i < 32; i++) {
				myHeader.name[i] = 0;
				myHeader.author[i] = 0;
				myHeader.released[i] = 0;
			}

			// @FIXME@ Need better solution. Make it possible to override MUS
			// strings
			if (info.numberOfInfoStrings == 3) {
				System.arraycopy(info.infoString[0], 0, myHeader.name, 0, Math.min(
						info.infoString[0].length(), _sidtune_psid_maxStrLen));
				System.arraycopy(info.infoString[1], 0, myHeader.author, 0, Math
						.min(info.infoString[1].length(), _sidtune_psid_maxStrLen));
				System.arraycopy(info.infoString[2], 0, myHeader.released, 0, Math
						.min(info.infoString[2].length(), _sidtune_psid_maxStrLen));
			}

			tmpFlags |= (info.clockSpeed << 2);
			tmpFlags |= (info.sidModel << 4);
			endian_big16(myHeader.flags, 0, tmpFlags);
			endian_big16(myHeader.reserved, 0, 0);

			write(fMyOut, myHeader.getArray(),0 , PHeader.SIZE);

			if (info.musPlayer)
				write(fMyOut, dataBuffer, 0, info.dataFileLen);
			else { // Save C64 lo/hi load address (little-endian).
				short /* uint_least8_t */saveAddr[] = new short[2];
				saveAddr[0] = (short) (info.loadAddr & 255);
				saveAddr[1] = (short) (info.loadAddr >> 8);
				write(fMyOut, saveAddr, 0, 2);

				// Data starts at: bufferaddr + fileoffset
				// Data length: datafilelen - fileoffset
				write(fMyOut, dataBuffer, sidtune.fileOffset, info.dataFileLen
						- sidtune.fileOffset);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private void write(OutputStream myOut, short[] dataBuffer, int offset,
			int length) throws IOException {
		for (int j = offset; j < length; j++) {
			myOut.write(dataBuffer[j]);
		}
	}
}
