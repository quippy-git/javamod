/**
 *                          C64 P00 file format support.
 *                          ----------------------------
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

import static de.quippy.sidplay.libsidplay.components.sidtune.SidTune.SIDTUNE_COMPATIBILITY_BASIC;
import static de.quippy.sidplay.libsidplay.components.sidtune.SidTune.LoadStatus.LOAD_ERROR;
import static de.quippy.sidplay.libsidplay.components.sidtune.SidTune.LoadStatus.LOAD_NOT_MINE;
import static de.quippy.sidplay.libsidplay.components.sidtune.SidTune.LoadStatus.LOAD_OK;
import de.quippy.sidplay.libsidplay.components.sidtune.SidTune.LoadStatus;

public class P00 {

	private static final int X00_ID_LEN = 8;

	private static final int X00_NAME_LEN = 17;

	/**
	 * File format from PC64. PC64 automatically generates the filename from the
	 * cbm name (16 to 8 conversion) but we only need to worry about that when
	 * writing files should we want pc64 compatibility. The extension numbers
	 * are just an index to try to avoid repeats. Name conversion works by
	 * creating an initial filename from alphanumeric and ' ', '-' characters
	 * only with the later two being converted to '_'. Then it parses the
	 * filename from end to start removing characters stopping as soon as the
	 * filename becomes <= 8. The removal of characters occurs in three passes,
	 * the first removes all '_', then vowels and finally numerics. If the
	 * filename is still greater than 8 it is truncated. struct X00Header
	 * 
	 * @author Ken H�ndel
	 * 
	 */
	private static class X00Header {
		public static final int SIZE = 26;

		public X00Header(short[] s) {
			int off = 0;
			for (int i = 0; i < X00_ID_LEN; i++) {
				id[i] = (char) s[off++];
			}
			for (int i = 0; i < X00_NAME_LEN; i++) {
				name[i] = (char) s[off++];
			}
			//length = s[off++];
		}

		/**
		 * C64File
		 */
		char id[] = new char[X00_ID_LEN];

		/**
		 * C64 name
		 */
		char name[] = new char[X00_NAME_LEN];

		/**
		 * Rel files only (Bytes/Record), should be 0 for all other types
		 */
//		short length = 0;
	}

	enum X00Format {
		X00_UNKNOWN, X00_DEL, X00_SEQ, X00_PRG, X00_USR, X00_REL
	}

	private static final String _sidtune_id = "C64File";

	private static final String _sidtune_format_del = "Unsupported tape image file (DEL)";

	private static final String _sidtune_format_seq = "Unsupported tape image file (SEQ)";

	private static final String _sidtune_format_prg = "Tape image file (PRG)";

	private static final String _sidtune_format_usr = "Unsupported USR file (USR)";

	private static final String _sidtune_format_rel = "Unsupported tape image file (REL)";

	private static final String _sidtune_truncated = "ERROR: File is most likely truncated";

	private SidTune sidtune;

	private SidTuneInfo info;

	public P00(SidTune sidtune) {
		this.sidtune = sidtune;
		this.info = sidtune.info;
	}

	protected LoadStatus X00_fileSupport(final String fileName,
			Buffer_sidtt /* Buffer_sidtt<const uint_least8_t>& */dataBuf) {
		int lastIndexOf = fileName.lastIndexOf(".");
		String ext = lastIndexOf != -1 ? fileName.substring(lastIndexOf) : "";
		String format = null;
		final X00Header pHeader = new X00Header(dataBuf.get());
		int /* uint_least32_t */bufLen = dataBuf.len();

		// Combined extension & magic field identification
		if (ext.length() != 4)
			return LOAD_NOT_MINE;
		if (!Character.isDigit(ext.charAt(2))
				|| !Character.isDigit(ext.charAt(3)))
			return LOAD_NOT_MINE;

		X00Format type = X00Format.X00_UNKNOWN;
		switch (Character.toUpperCase(ext.charAt(1))) {
		case 'D':
			type = X00Format.X00_DEL;
			format = _sidtune_format_del;
			break;
		case 'S':
			type = X00Format.X00_SEQ;
			format = _sidtune_format_seq;
			break;
		case 'P':
			type = X00Format.X00_PRG;
			format = _sidtune_format_prg;
			break;
		case 'U':
			type = X00Format.X00_USR;
			format = _sidtune_format_usr;
			break;
		case 'R':
			type = X00Format.X00_REL;
			format = _sidtune_format_rel;
			break;
		}

		if (type == X00Format.X00_UNKNOWN)
			return LOAD_NOT_MINE;

		int idLen = new String(pHeader.id).indexOf((char)0);
		if (idLen == -1) {
			idLen = pHeader.id.length;
		}
		// Verify the file is what we think it is
		if (bufLen < X00_ID_LEN)
			return LOAD_NOT_MINE;
		else if (!new String(pHeader.id, 0, idLen ).equals(_sidtune_id))
			return LOAD_NOT_MINE;

		info.formatString = format;

		// File types current supported
		if (type != X00Format.X00_PRG)
			return LOAD_ERROR;

		if (bufLen < (X00Header.SIZE) + 2) {
			info.formatString = _sidtune_truncated;
			return LOAD_ERROR;
		}

		{ // Decode file name
			int nameLen = new String(pHeader.name).indexOf((char)0);
			if (nameLen == -1) {
				nameLen = pHeader.name.length;
			}
			short[] buf = new short[nameLen];
			for (int j = 0; j < nameLen; j++) {
				buf[j] = (short) pHeader.name[j];
			}
			SmartPtr_sidtt /* SmartPtr_sidtt<const uint8_t> */spPet = new SmartPtr_sidtt(
					buf, nameLen, false);
			StringBuffer lineInfo = new StringBuffer();
			sidtune.convertPetsciiToAscii(spPet, lineInfo);
			sidtune.infoString[0] = lineInfo.toString();
		}

		// Automatic settings
		sidtune.fileOffset = X00Header.SIZE;
		info.songs = 1;
		info.startSong = 1;
		info.compatibility = SIDTUNE_COMPATIBILITY_BASIC;
		info.numberOfInfoStrings = 1;
		info.infoString[0] = sidtune.infoString[0];

		// Create the speed/clock setting table.
		sidtune.convertOldStyleSpeedToTables(~0, info.clockSpeed);
		return LOAD_OK;
	}

}
