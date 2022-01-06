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

import static de.quippy.sidplay.libsidplay.common.SIDEndian.endian_16;
import static de.quippy.sidplay.libsidplay.common.SIDEndian.endian_little16;
import static de.quippy.sidplay.libsidplay.components.sidtune.ISidTuneCfg.SIDTUNE_PSID2NG;
import static de.quippy.sidplay.libsidplay.components.sidtune.ISidTuneCfg.SIDTUNE_R64_MIN_LOAD_ADDR;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

import de.quippy.sidplay.libsidplay.components.sidtune.Mus.Voice3Index;


/**
 * @author Ken H�ndel
 * 
 */
public class SidTune {

	private static final Logger TUNE = Logger
			.getLogger(SidTune.class.getName());

	/**
	 * Also PSID file format limit.
	 */
	public static final int /* uint_least16_t */SIDTUNE_MAX_SONGS = 256;

	public static final int /* uint_least16_t */SIDTUNE_MAX_CREDIT_STRINGS = 10;

	/**
	 * 80 characters plus terminating zero.
	 */
	public static final int /* uint_least16_t */SIDTUNE_MAX_CREDIT_STRLEN = 80 + 1;

	/**
	 * C64KB
	 */
	public static final int /* uint_least32_t */SIDTUNE_MAX_MEMORY = 65536;

	/**
	 * C64KB+LOAD+PSID
	 */
	public static final int /* uint_least32_t */SIDTUNE_MAX_FILELEN = 65536 + 2 + 0x7C;

	/**
	 * Vertical-Blanking-Interrupt
	 */
	public static final int SIDTUNE_SPEED_VBI = 0;

	/**
	 * CIA 1 Timer A
	 */
	public static final int SIDTUNE_SPEED_CIA_1A = 60;

	public static final int SIDTUNE_CLOCK_UNKNOWN = 0x00;

	public static final int SIDTUNE_CLOCK_PAL = 0x01;

	public static final int SIDTUNE_CLOCK_NTSC = 0x02;

	public static final int SIDTUNE_CLOCK_ANY = (SIDTUNE_CLOCK_PAL | SIDTUNE_CLOCK_NTSC);

	public static final int SIDTUNE_SIDMODEL_UNKNOWN = 0x00;

	public static final int SIDTUNE_SIDMODEL_6581 = 0x01;

	public static final int SIDTUNE_SIDMODEL_8580 = 0x02;

	public static final int SIDTUNE_SIDMODEL_ANY = (SIDTUNE_SIDMODEL_6581 | SIDTUNE_SIDMODEL_8580);

	/**
	 * File is C64 compatible
	 */
	public static final int SIDTUNE_COMPATIBILITY_C64 = 0x00;

	/**
	 * File is PSID specific
	 */
	public static final int SIDTUNE_COMPATIBILITY_PSID = 0x01;

	/**
	 * File is Real C64 only
	 */
	public static final int SIDTUNE_COMPATIBILITY_R64 = 0x02;

	/**
	 * File requires C64 Basic
	 */
	public static final int SIDTUNE_COMPATIBILITY_BASIC = 0x03;

	public enum LoadStatus {
		LOAD_NOT_MINE, LOAD_OK, LOAD_ERROR
	}

	/**
	 * Load a sidtune from a file.<BR>
	 * 
	 * To retrieve data from standard input pass in filename "-". If you want to
	 * override the default filename extensions use this constructor. Please
	 * note, that if the specified ``sidTuneFileName'' does exist and the loader
	 * is able to determine its file format, this function does not try to
	 * append any file name extension. See ``SidTune.java'' for the default list
	 * of file name extensions. You can specific ``sidTuneFileName = 0'', if you
	 * do not want to load a sidtune. You can later load one with open().<BR>
	 * 
	 * @param fileName
	 * @param fileNameExt
	 */
	public SidTune(final String fileName, final String[] fileNameExt) {
		init();
		setFileNameExtensions(fileNameExt);
		// Filename ``-'' is used as a synonym for standard input.
		if (fileName != null && (fileName.equals("-"))) {
			getFromStdIn();
		} else if (fileName != null) {
			getFromFiles(fileName);
		}
	}

	/**
	 * Load a single-file sidtune from a memory buffer. Currently supported:
	 * <UL>
	 * <LI>PSID format
	 * </UL>
	 * 
	 * @param oneFileFormatSidtune
	 * @param sidtuneLength
	 */
	public SidTune(final short[] /* uint_least8_t* */oneFileFormatSidtune,
			final int /* uint_least32_t */sidtuneLength) {
		init();
		getFromBuffer(oneFileFormatSidtune, sidtuneLength);
	}

	/**
	 * The sidTune class does not copy the list of file name extensions, so make
	 * sure you keep it. If the provided pointer is 0, the default list will be
	 * activated. This is a static list which is used by all SidTune objects.
	 * 
	 * @param fileNameExt
	 */
	public static void setFileNameExtensions(final String[] fileNameExt) {
		fileNameExtensions = ((fileNameExt != null) ? fileNameExt : defaultFileNameExt);
	}

	/**
	 * Load a sidtune into an existing object. From a file.
	 * 
	 * @param fileName
	 * @return
	 */
	public boolean load(final String fileName) {
		cleanup();
		init();
		// Filename ``-'' is used as a synonym for standard input.
		if (fileName != null && (fileName.equals("-"))) {
			getFromStdIn();
		} else if (fileName != null) {
			getFromFiles(fileName);
		}
		return status;
	}

	/**
	 * From a buffer.
	 * 
	 * @param sourceBuffer
	 * @param bufferLen
	 * @return
	 */
	public boolean read(final short[] /* uint_least8_t* */sourceBuffer,
			final int /* uint_least32_t */bufferLen) {
		cleanup();
		init();
		getFromBuffer(sourceBuffer, bufferLen);
		return status;
	}

	/**
	 * Select sub-song (0 = default starting song) and retrieve active song
	 * information.
	 * 
	 * @param songNum
	 * @return
	 */
	public final SidTuneInfo opGet(final int /* uint_least16_t */songNum) {
		selectSong(songNum);
		return info;
	}

	/**
	 * Select sub-song (0 = default starting song) and return active song number
	 * out of [1,2,..,SIDTUNE_MAX_SONGS].
	 * 
	 * @param selectedSong
	 * @return
	 */
	public int /* uint_least16_t */selectSong(
			final int /* uint_least16_t */selectedSong) {
		if (!status)
			return 0;
		else
			info.statusString = txt_noErrors;

		int /* uint_least16_t */song = selectedSong;
		// Determine and set starting song number.
		if (selectedSong == 0)
			song = info.startSong;
		if (selectedSong > info.songs || selectedSong > SIDTUNE_MAX_SONGS) {
			song = info.startSong;
			info.statusString = txt_songNumberExceed;
		}
		info.currentSong = song;
		//info.songLength = songLength[song - 1];
		// Retrieve song speed definition.
		if (info.compatibility == SIDTUNE_COMPATIBILITY_R64)
			info.songSpeed = SIDTUNE_SPEED_CIA_1A;
		else
			info.songSpeed = songSpeed[song - 1];
		info.clockSpeed = clockSpeed[song - 1];
		// Assign song speed description string depending on clock speed.
		// Final speed description is available only after song init.
		if (info.songSpeed == SIDTUNE_SPEED_VBI)
			info.speedString = txt_VBI;
		else
			info.speedString = txt_CIA;
		return info.currentSong;
	}

	/**
	 * Retrieve sub-song specific information. Beware! Still member-wise copy!
	 * 
	 * @return
	 */
	public final SidTuneInfo getInfo() {
		return info;
	}

	/**
	 * Determine current state of object (true = okay, false = error). Upon
	 * error condition use ``getInfo'' to get a descriptive text string in
	 * ``SidTuneInfo.statusString''.
	 * 
	 * @return
	 */
	public boolean bool() {
		return status;
	}

	/**
	 * Determine current state of object (true = okay, false = error). Upon
	 * error condition use ``getInfo'' to get a descriptive text string in
	 * ``SidTuneInfo.statusString''.
	 * 
	 * @return
	 */
	public boolean getStatus() {
		return status;
	}

	/**
	 * Whether sidtune uses two SID chips.
	 * 
	 * @return
	 */
	public boolean isStereo() {
		return (info.sidChipBase1 != 0 && info.sidChipBase2 != 0);
	}

	/**
	 * Copy sidtune into C64 memory (64 KB).
	 * 
	 * @param c64buf
	 * @return
	 */
	public boolean placeSidTuneInC64mem(short[] /* uint_least8_t* */c64buf) {
		if (status && c64buf != null) {
			int /* uint_least32_t */endPos = info.loadAddr + info.c64dataLen;
			if (endPos <= SIDTUNE_MAX_MEMORY) {
				// Copy data from cache to the correct destination.
				System.arraycopy(cache.get(), fileOffset, c64buf,
						info.loadAddr, info.c64dataLen);
				info.statusString = txt_noErrors;

				for (int i = 0; i < info.c64dataLen; i += 16) {
					for (int j = 0; j < 16 && i + j < info.c64dataLen; j++) {
						TUNE.fine(String.format("0x%02x ", Short.valueOf(c64buf[info.loadAddr + i + j])));
					}
					TUNE.fine("\n");
				}

			} else {
				// Security - cut data which would exceed the end of the C64
				// memory. Memcpy could not detect this.
				//
				// NOTE: In libsidplay1 the rest gets wrapped to the beginning
				// of the C64 memory. It is an undocumented hack most likely not
				// used by any sidtune. Here we no longer do it like that, set
				// an error message, and hope the modified behavior will find
				// a few badly ripped sids.
				System.arraycopy(cache.get(), fileOffset, c64buf,
						info.loadAddr, info.c64dataLen
								- (endPos - SIDTUNE_MAX_MEMORY));
				info.statusString = txt_dataTooLong;
			}
			if (info.musPlayer) {
				MUS_installPlayer(c64buf);
			}
		}
		return (status && c64buf != null);
	}

	//
	// --- file save & format conversion ---
	// These functions work for any successfully created object.
	//

	/**
	 * @param destFileName
	 * @param overWriteFlag
	 *            true = Overwrite existing file, false = Default<BR>
	 *            One could imagine an "Are you sure ?"-checkbox before
	 *            overwriting any file.
	 * @return true = Successful, false = Error condition. error when file
	 *         already exists.
	 */
	public boolean saveC64dataFile(final String destFileName,
			final boolean overWriteFlag) {
		boolean success = false; // assume error
		// This prevents saving from a bad object.
		if (status) {
			try {
				FileOutputStream fMyOut = new FileOutputStream(destFileName,
						!overWriteFlag);

				if (!info.musPlayer) {
					// Save c64 lo/hi load address.
					short /* uint_least8_t */saveAddr[] = new short[2];
					saveAddr[0] = (short) (info.loadAddr & 255);
					saveAddr[1] = (short) (info.loadAddr >> 8);
					fMyOut.write(saveAddr[0]);
					fMyOut.write(saveAddr[1]);
				}

				// Data starts at: bufferaddr + fileOffset
				// Data length: info.dataFileLen - fileOffset
				if (!saveToOpenFile(fMyOut, cache.get(), fileOffset,
						info.dataFileLen - fileOffset)) {
					info.statusString = txt_fileIoError;
				} else {
					info.statusString = txt_noErrors;
					success = true;
				}
				fMyOut.close();
			} catch (IOException e) {
				info.statusString = txt_cantCreateFile;
			}
		}
		return success;
	}

	/**
	 * @param destFileName
	 * @param overWriteFlag
	 *            true = Overwrite existing file, false = Default<BR>
	 *            One could imagine an "Are you sure ?"-checkbox before
	 *            overwriting any file.
	 * @return true = Successful, false = Error condition. error when file
	 *         already exists.
	 */
	public boolean saveSIDfile(final String destFileName,
			final boolean overWriteFlag) {
		boolean success = false; // assume error
		// This prevents saving from a bad object.
		if (status) {
			try {
				FileOutputStream fMyOut = new FileOutputStream(destFileName,
						!overWriteFlag);
				if (!SID_fileSupportSave(fMyOut)) {
					info.statusString = txt_fileIoError;
				} else {
					info.statusString = txt_noErrors;
					success = true;
				}
				fMyOut.close();
			} catch (IOException e) {
				info.statusString = txt_cantCreateFile;
			}
		}
		return success;
	}

	/**
	 * @param destFileName
	 * @param overWriteFlag
	 *            true = Overwrite existing file, false = Default<BR>
	 *            One could imagine an "Are you sure ?"-checkbox before
	 *            overwriting any file.
	 * @return true = Successful, false = Error condition. error when file
	 *         already exists.
	 */
	public boolean savePSIDfile(final String destFileName,
			final boolean overWriteFlag) {
		boolean success = false; // assume error
		// This prevents saving from a bad object.
		if (status) {
			try {
				FileOutputStream fMyOut = new FileOutputStream(destFileName,
						!overWriteFlag);
				if (!PSID_fileSupportSave(fMyOut, cache.get())) {
					info.statusString = txt_fileIoError;
				} else {
					info.statusString = txt_noErrors;
					success = true;
				}
				fMyOut.close();
			} catch (IOException e) {
				info.statusString = txt_cantCreateFile;
			}
		}
		return success;
	}

	/**
	 * This function can be used to remove a duplicate C64 load address in the
	 * C64 data (example: FE 0F 00 10 4C ...). A duplicate load address of
	 * offset 0x02 is indicated by the ``fixLoad'' flag in the SidTuneInfo
	 * structure.
	 * 
	 * The ``force'' flag here can be used to remove the first load address and
	 * set new INIT/PLAY addresses regardless of whether a duplicate load
	 * address has been detected and indicated by ``fixLoad''. For instance,
	 * some position independent sidtunes contain a load address of 0xE000, but
	 * are loaded to 0x0FFE and call the player code at 0x1000.
	 * 
	 * Do not forget to save the sidtune file.
	 * 
	 * @param force
	 * @param init
	 * @param play
	 */
	public void fixLoadAddress(final boolean force,
			int /* uint_least16_t */init, int /* uint_least16_t */play) {
		if (info.fixLoad || force) {
			info.fixLoad = false;
			info.loadAddr += 2;
			fileOffset += 2;

			if (force) {
				info.initAddr = init;
				info.playAddr = play;
			}
		}
	}

	/**
	 * Does not affect status of object, and therefore can be used to load
	 * files. Error string is put into info.statusString, though.
	 * 
	 * @param fileName
	 * @param bufferRef
	 * @return
	 */
	public boolean loadFile(final String fileName,
			Buffer_sidtt /* Buffer_sidtt<const uint_least8_t>& */bufferRef) {
		FileInputStream myIn = null;
		Buffer_sidtt /* Buffer_sidtt<const uint_least8_t> */fileBuf = new Buffer_sidtt();
		int /* uint_least32_t */fileLen = 0;

		File file = new File(fileName);

		if (!file.exists() || !file.canRead()) {
			info.statusString = txt_cantOpenFile;
			return false;
		} else {
			try {
				myIn = new FileInputStream(file);

				fileLen = (int) file.length();
				if (!fileBuf.assign(new short /* uint_least8_t */[fileLen],
						fileLen)) {
					info.statusString = txt_notEnoughMemory;
					return false;
				}
				int /* uint_least32_t */restFileLen = fileLen;
				if (restFileLen > 0) {
					for (int i = 0; i < fileLen; i++) {
						fileBuf.get()[i] = (short) myIn.read();
					}
				}
				info.statusString = txt_noErrors;
				myIn.close();
			} catch (IOException e) {
				info.statusString = txt_cantLoadFile;
				return false;
			}
			finally {
				if (myIn!=null) try { myIn.close(); } catch (IOException ex) { info.statusString = ex.getMessage(); }
			}
		}
		if (fileLen == 0) {
			info.statusString = txt_empty;
			return false;
		}

		if (decompressPP20(fileBuf) < 0)
			return false;

		bufferRef.assign(fileBuf.xferPtr(), fileBuf.xferLen());
		return true;
	}

	public boolean saveToOpenFile(OutputStream toFile,
			final short[] /* uint_least8_t* */buffer, int bufferOffset,
			int /* uint_least32_t */bufLen) {
		int /* uint_least32_t */lenToWrite = bufLen;
		try {
			if (lenToWrite > 0) {
				for (int i = (bufLen - lenToWrite); i < lenToWrite; i++) {
					toFile.write(buffer[bufferOffset + i]);
				}
			}
		} catch (IOException e) {
			info.statusString = txt_fileIoError;
			return false;
		}
		info.statusString = txt_noErrors;
		return true;
	}

	protected SidTuneInfo info = new SidTuneInfo();

	protected boolean status;

	protected short /* uint_least8_t */songSpeed[] = new short[SIDTUNE_MAX_SONGS];

	protected short /* uint_least8_t */clockSpeed[] = new short[SIDTUNE_MAX_SONGS];

	protected short /* uint_least16_t */songLength[] = new short[SIDTUNE_MAX_SONGS];

	/**
	 * holds text info from the format headers etc.
	 */
	protected String infoString[] = new String[SIDTUNE_MAX_CREDIT_STRINGS];

	/**
	 * For files with header: offset to real data
	 */
	protected int /* uint_least32_t */fileOffset;

	/**
	 * Needed for MUS/STR player installation.
	 */
	protected int /* uint_least16_t */musDataLen;

	protected Buffer_sidtt /* Buffer_sidtt<const uint_least8_t> */cache = new Buffer_sidtt();

	/**
	 * Default sidtune file name extensions. This selection can be overridden by
	 * specifying a custom list in the constructor.
	 */
	private static final String defaultFileNameExt[] = new String[] {
	// Preferred default file extension for single-file sidtunes
			// or sidtune description files in SIDPLAY INFOFILE format.
			".sid",
			// Common file extension for single-file sidtunes due to SIDPLAY/DOS
			// displaying files *.DAT in its file selector by default.
			// Originally this was intended to be the extension of the raw data
			// file
			// of two-file sidtunes in SIDPLAY INFOFILE format.
			".dat",
			// Extension of Amiga Workbench tooltype icon info files, which
			// have been cut to MS-DOS file name length (8.3).
			".inf",
			// No extension for the raw data file of two-file sidtunes in
			// PlaySID Amiga Workbench tooltype icon info format.
			"",
			// Common upper-case file extensions from MS-DOS (unconverted).
			".DAT", ".SID", ".INF",
			// File extensions used (and created) by various C64 emulators and
			// related utilities. These extensions are recommended to be used as
			// a replacement for ".dat" in conjunction with two-file sidtunes.
			".c64", ".prg", ".p00", ".C64", ".PRG", ".P00",
			// Uncut extensions from Amiga.
			".info", ".INFO", ".data", ".DATA",
			// Stereo Sidplayer (.mus/.MUS ought not be included because
			// these must be loaded first; it sometimes contains the first
			// credit lines of a MUS/STR pair).
			".str", ".STR", ".mus", ".MUS" };

	/**
	 * Filename extensions to append for various file types.
	 */
	protected static/* final */String[] fileNameExtensions = defaultFileNameExt;

	/**
	 * Convert 32-bit PSID-style speed word to internal tables.
	 * 
	 * @param speed
	 * @param clock
	 */
	protected void convertOldStyleSpeedToTables(
			long /* uint_least32_t */speed, short clock) {
		// Create the speed/clock setting tables.
		//
		// This does not take into account the PlaySID bug upon evaluating the
		// SPEED field. It would most likely break compatibility to lots of
		// sidtunes, which have been converted from .SID format and vice versa.
		// The .SID format does the bit-wise/song-wise evaluation of the SPEED
		// value correctly, like it is described in the PlaySID documentation.

		int toDo = ((info.songs <= SIDTUNE_MAX_SONGS) ? info.songs
				: SIDTUNE_MAX_SONGS);
		for (int s = 0; s < toDo; s++) {
			clockSpeed[s] = clock;
			if (((speed >> (s & 31)) & 1) == 0)
				songSpeed[s] = SIDTUNE_SPEED_VBI;
			else
				songSpeed[s] = SIDTUNE_SPEED_CIA_1A;
		}
	}

	protected int convertPetsciiToAscii(
			SmartPtr_sidtt /* SmartPtr_sidtt<const uint8_t>& */spPet,
			StringBuffer dest) {
		int count = 0;
		short c;
		if (dest != null) {
			do {
				c = _sidtune_CHRtab[spPet.operatorMal()]; // ASCII CHR$
				// conversion
				if ((c >= 0x20) && (count <= 31)) {
					dest.setLength(count + 1);
					dest.setCharAt(count++, (char) c); // copy to info string
				}
				// If character is 0x9d (left arrow key) then move back.
				if ((spPet.operatorMal() == 0x9d) && (count >= 0))
					count--;
				spPet.operatorPlusPlus();
			} while (!((c == 0x0D) || (c == 0x00) || spPet.fail()));
		} else { // Just find end of string
			do {
				c = _sidtune_CHRtab[spPet.operatorMal()]; // ASCII CHR$
				// conversion
				spPet.operatorPlusPlus();
			} while (!((c == 0x0D) || (c == 0x00) || spPet.fail()));
		}
		return count;
	}

	/**
	 * Check compatibility details are sensible
	 * 
	 * @return
	 */
	protected boolean checkCompatibility() {
		switch (info.compatibility) {
		case SIDTUNE_COMPATIBILITY_R64:
			// Check valid init address
			switch (info.initAddr >> 12) {
			case 0x0F:
			case 0x0E:
			case 0x0D:
			case 0x0B:
			case 0x0A:
				info.statusString = txt_badAddr;
				return false;
			default:
				if ((info.initAddr < info.loadAddr)
						|| (info.initAddr > (info.loadAddr + info.c64dataLen - 1))) {
					info.statusString = txt_badAddr;
					return false;
				}
			}
			// deliberate run on

		case SIDTUNE_COMPATIBILITY_BASIC:
			// Check tune is loadable on a real C64
			if (info.loadAddr < SIDTUNE_R64_MIN_LOAD_ADDR) {
				info.statusString = txt_badAddr;
				return false;
			}
			break;
		}
		return true;
	}

	/**
	 * Check for valid relocation information
	 * 
	 * @return
	 */
	protected boolean checkRelocInfo() {
		short /* uint_least8_t */startp, endp;

		// Fix relocation information
		if (info.relocStartPage == 0xFF) {
			info.relocPages = 0;
			return true;
		} else if (info.relocPages == 0) {
			info.relocStartPage = 0;
			return true;
		}

		// Calculate start/end page
		startp = info.relocStartPage;
		endp = (short) ((startp + info.relocPages - 1) & 0xff);
		if (endp < startp) {
			info.statusString = txt_badReloc;
			return false;
		}

		{ // Check against load range
			short /* uint_least8_t */startlp, endlp;
			startlp = (short /* uint_least8_t */) (info.loadAddr >> 8);
			endlp = startlp;
			endlp += (short /* uint_least8_t */) ((info.c64dataLen - 1) >> 8);

			if (((startp <= startlp) && (endp >= startlp))
					|| ((startp <= endlp) && (endp >= endlp))) {
				info.statusString = txt_badReloc;
				return false;
			}
		}

		// Check that the relocation information does not use the following
		// memory areas: 0x0000-0x03FF, 0xA000-0xBFFF and 0xD000-0xFFFF
		if ((startp < 0x04) || ((0xa0 <= startp) && (startp <= 0xbf))
				|| (startp >= 0xd0) || ((0xa0 <= endp) && (endp <= 0xbf))
				|| (endp >= 0xd0)) {
			info.statusString = txt_badReloc;
			return false;
		}
		return true;
	}

	/**
	 * Common address resolution procedure
	 * 
	 * @param c64data
	 * @param fileOffset2
	 * @return
	 */
	protected boolean resolveAddrs(final short[] /* uint_least8_t* */c64data,
			int fileOffset2) {
		// Originally used as a first attempt at an RSID
		// style format. Now reserved for future use
		if (info.playAddr == 0xffff)
			info.playAddr = 0;

		// loadAddr = 0 means, the address is stored in front of the C64 data.
		if (info.loadAddr == 0) {
			if (info.c64dataLen < 2) {
				info.statusString = txt_corrupt;
				return false;
			}
			info.loadAddr = endian_16(c64data[fileOffset + 1],
					c64data[fileOffset + 0]);
			fileOffset += 2;
			// c64data += 2;
			info.c64dataLen -= 2;
		}

		if (info.compatibility == SIDTUNE_COMPATIBILITY_BASIC) {
			if (info.initAddr != 0) {
				info.statusString = txt_badAddr;
				return false;
			}
		} else if (info.initAddr == 0)
			info.initAddr = info.loadAddr;
		return true;
	}

	//
	// Support for various file formats.
	//

	private PSid psid = new PSid(this);

	protected LoadStatus PSID_fileSupport(
			Buffer_sidtt /* Buffer_sidtt<const uint_least8_t>& */dataBuf) {
		return psid.PSID_fileSupport(dataBuf);
	}

	protected boolean PSID_fileSupportSave(OutputStream toFile,
			final short[] /* uint_least8_t* */dataBuffer) {
		return psid.PSID_fileSupportSave(toFile, dataBuffer);
	}

	protected LoadStatus SID_fileSupport(
			Buffer_sidtt /* Buffer_sidtt<const uint_least8_t>& */dataBuf,
			Buffer_sidtt /* Buffer_sidtt<const uint_least8_t>& */sidBuf) {
		return LoadStatus.LOAD_NOT_MINE;
	}

	protected boolean SID_fileSupportSave(OutputStream toFile) {
		return true;
	}

	Mus mus = new Mus(this);

	protected LoadStatus MUS_fileSupport(
			Buffer_sidtt /* Buffer_sidtt<const uint_least8_t>& */musBuf,
			Buffer_sidtt /* Buffer_sidtt<const uint_least8_t>& */strBuf) {
		return mus.MUS_fileSupport(musBuf, strBuf);
	}

	protected LoadStatus MUS_load(
			Buffer_sidtt /* Buffer_sidtt<const uint_least8_t>& */musBuf,
			boolean init) {
		return mus.MUS_load(musBuf, init);
	}

	protected LoadStatus MUS_load(
			Buffer_sidtt /* Buffer_sidtt<const uint_least8_t>& */musBuf,
			Buffer_sidtt /* Buffer_sidtt<const uint_least8_t>& */strBuf,
			boolean init) {
		return mus.MUS_load(musBuf, strBuf, init);
	}

	protected boolean MUS_detect(final short[] /* const void* */buffer,
			final int /* uint_least32_t */bufLen,
			Voice3Index /* uint_least32_t& */voice3Index) {
		return mus.MUS_detect(buffer, bufLen, voice3Index);
	}

	protected boolean MUS_mergeParts(
			Buffer_sidtt /* Buffer_sidtt<const uint_least8_t>& */musBuf,
			Buffer_sidtt /* Buffer_sidtt<const uint_least8_t>& */strBuf) {
		return mus.MUS_mergeParts(musBuf, strBuf);
	}

	protected void MUS_setPlayerAddress() {
		mus.MUS_setPlayerAddress();
	}

	protected void MUS_installPlayer(short[] /* uint_least8_t * */c64buf) {
		mus.MUS_installPlayer(c64buf);
	}

	InfoFile inf = new InfoFile(this);

	protected LoadStatus INFO_fileSupport(
			Buffer_sidtt /* Buffer_sidtt<const uint_least8_t>& */dataBuf,
			Buffer_sidtt /* Buffer_sidtt<const uint_least8_t>& */infoBuf) {
		return inf.INFO_fileSupport(dataBuf, infoBuf);
	}

	Prg prg = new Prg(this);

	protected LoadStatus PRG_fileSupport(final String fileName,
			Buffer_sidtt /* Buffer_sidtt<const uint_least8_t>& */dataBuf) {
		return prg.PRG_fileSupport(fileName, dataBuf);
	}

	P00 p00 = new P00(this);

	protected LoadStatus X00_fileSupport(final String fileName,
			Buffer_sidtt /* Buffer_sidtt<const uint_least8_t>& */dataBuf) {
		return p00.X00_fileSupport(fileName, dataBuf);
	}

	//
	// Error and status message strings.
	//

	protected static final String txt_songNumberExceed = "SIDTUNE WARNING: Selected song number was too high";

	protected static final String txt_empty = "SIDTUNE ERROR: No data to load";

	protected static final String txt_unrecognizedFormat = "SIDTUNE ERROR: Could not determine file format";

	protected static final String txt_noDataFile = "SIDTUNE ERROR: Did not find the corresponding data file";

	protected static final String txt_notEnoughMemory = "SIDTUNE ERROR: Not enough free memory";

	protected static final String txt_cantLoadFile = "SIDTUNE ERROR: Could not load input file";

	protected static final String txt_cantOpenFile = "SIDTUNE ERROR: Could not open file for binary input";

	protected static final String txt_fileTooLong = "SIDTUNE ERROR: Input data too long";

	protected static final String txt_dataTooLong = "SIDTUNE ERROR: Size of music data exceeds C64 memory";

	protected static final String txt_cantCreateFile = "SIDTUNE ERROR: Could not create output file";

	protected static final String txt_fileIoError = "SIDTUNE ERROR: File I/O error";

	protected static final String txt_VBI = "VBI";

	protected static final String txt_CIA = "CIA 1 Timer A";

	protected static final String txt_noErrors = "No errors";

	protected static final String txt_na = "N/A";

	protected static final String txt_badAddr = "SIDTUNE ERROR: Bad address data";

	protected static final String txt_badReloc = "SIDTUNE ERROR: Bad reloc data";

	protected static final String txt_corrupt = "SIDTUNE ERROR: File is incomplete or corrupt";

	/**
	 * Petscii to Ascii conversion table.<BR>
	 * 
	 * CHR$ conversion table (0x01 = no output)
	 */
	private static final short _sidtune_CHRtab[] = { 0x0, 0x1, 0x1, 0x1, 0x1,
			0x1, 0x1, 0x1, 0x1, 0x1, 0x1, 0x1, 0x1, 0xd, 0x1, 0x1, 0x1, 0x1,
			0x1, 0x1, 0x1, 0x1, 0x1, 0x1, 0x1, 0x1, 0x1, 0x1, 0x1, 0x1, 0x1,
			0x1, 0x20, 0x21, 0x1, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29,
			0x2a, 0x2b, 0x2c, 0x2d, 0x2e, 0x2f, 0x30, 0x31, 0x32, 0x33, 0x34,
			0x35, 0x36, 0x37, 0x38, 0x39, 0x3a, 0x3b, 0x3c, 0x3d, 0x3e, 0x3f,
			0x40, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4a,
			0x4b, 0x4c, 0x4d, 0x4e, 0x4f, 0x50, 0x51, 0x52, 0x53, 0x54, 0x55,
			0x56, 0x57, 0x58, 0x59, 0x5a, 0x5b, 0x24, 0x5d, 0x20, 0x20,
			/* alternative: CHR$(92=0x5c) => ISO Latin-1(0xa3) */
			0x2d, 0x23, 0x7c, 0x2d, 0x2d, 0x2d, 0x2d, 0x7c, 0x7c, 0x5c, 0x5c,
			0x2f, 0x5c, 0x5c, 0x2f, 0x2f, 0x5c, 0x23, 0x5f, 0x23, 0x7c, 0x2f,
			0x58, 0x4f, 0x23, 0x7c, 0x23, 0x2b, 0x7c, 0x7c, 0x26, 0x5c,
			/* 0x80-0xFF */
			0x1, 0x1, 0x1, 0x1, 0x1, 0x1, 0x1, 0x1, 0x1, 0x1, 0x1, 0x1, 0x1,
			0x1, 0x1, 0x1, 0x1, 0x1, 0x1, 0x1, 0x1, 0x1, 0x1, 0x1, 0x1, 0x1,
			0x1, 0x1, 0x1, 0x1, 0x1, 0x1, 0x20, 0x7c, 0x23, 0x2d, 0x2d, 0x7c,
			0x23, 0x7c, 0x23, 0x2f, 0x7c, 0x7c, 0x2f, 0x5c, 0x5c, 0x2d, 0x2f,
			0x2d, 0x2d, 0x7c, 0x7c, 0x7c, 0x7c, 0x2d, 0x2d, 0x2d, 0x2f, 0x5c,
			0x5c, 0x2f, 0x2f, 0x23, 0x2d, 0x23, 0x7c, 0x2d, 0x2d, 0x2d, 0x2d,
			0x7c, 0x7c, 0x5c, 0x5c, 0x2f, 0x5c, 0x5c, 0x2f, 0x2f, 0x5c, 0x23,
			0x5f, 0x23, 0x7c, 0x2f, 0x58, 0x4f, 0x23, 0x7c, 0x23, 0x2b, 0x7c,
			0x7c, 0x26, 0x5c, 0x20, 0x7c, 0x23, 0x2d, 0x2d, 0x7c, 0x23, 0x7c,
			0x23, 0x2f, 0x7c, 0x7c, 0x2f, 0x5c, 0x5c, 0x2d, 0x2f, 0x2d, 0x2d,
			0x7c, 0x7c, 0x7c, 0x7c, 0x2d, 0x2d, 0x2d, 0x2f, 0x5c, 0x5c, 0x2f,
			0x2f, 0x23 };

	private void init() {
		// Initialize the object with some safe defaults.
		status = false;

		info.statusString = txt_na;
		info.path = info.infoFileName = info.dataFileName = null;
		info.dataFileLen = info.c64dataLen = 0;
		info.formatString = txt_na;
		info.speedString = txt_na;
		info.loadAddr = (info.initAddr = (info.playAddr = 0));
		info.songs = (info.startSong = (info.currentSong = 0));
		info.sidChipBase1 = 0xd400;
		info.sidChipBase2 = 0;
		info.musPlayer = false;
		info.fixLoad = false;
		info.songSpeed = SIDTUNE_SPEED_VBI;
		if (SIDTUNE_PSID2NG) {
			info.clockSpeed = SIDTUNE_CLOCK_UNKNOWN;
			info.sidModel = SIDTUNE_SIDMODEL_UNKNOWN;
		} else {
			info.clockSpeed = SIDTUNE_CLOCK_PAL;
			info.sidModel = SIDTUNE_SIDMODEL_6581;
		}
		info.compatibility = SIDTUNE_COMPATIBILITY_C64;
		//info.songLength = 0;
		info.relocStartPage = 0;
		info.relocPages = 0;

		for (int /* uint_least16_t */si = 0; si < SIDTUNE_MAX_SONGS; si++) {
			songSpeed[si] = info.songSpeed;
			clockSpeed[si] = info.clockSpeed;
			songLength[si] = 0;
		}

		fileOffset = 0;
		musDataLen = 0;

		for (int /* uint_least16_t */sNum = 0; sNum < SIDTUNE_MAX_CREDIT_STRINGS; sNum++) {
			infoString[sNum] = null;
		}
		info.numberOfInfoStrings = 0;

		// Not used!!!
		info.numberOfCommentStrings = 1;
		info.commentString = new String[info.numberOfCommentStrings];
		if (info.commentString != null)
			info.commentString[0] = "--- SAVED WITH SIDPLAY ---";
		else
			info.commentString[0] = null;
	}

	private void cleanup() {
		// Remove copy of comment field.
		info.commentString = null;

		deleteFileNameCopies();

		status = false;
	}

	private void getFromStdIn() {
		// Assume a failure, so we can simply return.
		status = false;
		// Assume the memory allocation to fail.
		info.statusString = txt_notEnoughMemory;
		short[] /* uint_least8_t* */fileBuf;
		fileBuf = new short /* uint_least8_t */[SIDTUNE_MAX_FILELEN];

		// We only read as much as fits in the buffer.
		// This way we avoid choking on huge data.
		int /* uint_least32_t */i = 0;
		int datb;
		try {
			while ((datb = System.in.read()) != -1 && i < SIDTUNE_MAX_FILELEN)
				fileBuf[i++] = (short /* uint_least8_t */) datb;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		info.dataFileLen = i;
		getFromBuffer(fileBuf, info.dataFileLen);
		//fileBuf = null;
	}

	/**
	 * Initializing the object based upon what we find in the specified file.
	 * 
	 * @param fileName
	 */
	private void getFromFiles(final String fileName) {
		// Assume a failure, so we can simply return.
		status = false;

		Buffer_sidtt /* Buffer_sidtt<const uint_least8_t> */fileBuf1 = new Buffer_sidtt(), fileBuf2 = new Buffer_sidtt();
		StringBuffer /* Buffer_sidtt<char> */fileName2 = new StringBuffer();

		// Try to load the single specified file. The original method didn't
		// quite work that well, so instead we now let the support files take
		// ownership of a known file and don't assume we should just
		// continue searching when an error is found.
		if (loadFile(fileName, fileBuf1)) {
			LoadStatus ret;

			// File loaded. Now check if it is in a valid single-file-format.
			ret = PSID_fileSupport(fileBuf1);
			if (ret != LoadStatus.LOAD_NOT_MINE) {
				if (ret == LoadStatus.LOAD_OK)
					status = acceptSidTune(fileName, null, fileBuf1);
				return;
			}

			// -------------------------------------- Support for multiple-files
			// formats.
			else {
				// We cannot simply try to load additional files, if a
				// description file was
				// specified. It would work, but is error-prone. Imagine a
				// filename mismatch
				// or more than one description file (in another) format. Any
				// other file
				// with an appropriate file name can be the C64 data file.

				// First we see if ``fileName'' could be a raw data file. In
				// that case we
				// have to find the corresponding description file.

				// Right now we do not have a second file (fileBuf2 empty). This
				// will not hurt the file support procedures.

				// Make sure that ``fileBuf1'' does not contain a description
				// file.
				ret = orStatus(SID_fileSupport(fileBuf2, fileBuf1),
						INFO_fileSupport(fileBuf2, fileBuf1));
				if (ret == LoadStatus.LOAD_NOT_MINE) {
					// Assuming ``fileName'' to hold the name of the raw data
					// file,
					// we now create the name of a description file (=fileName2)
					// by
					// appending various filename extensions.

					// ------------------------------------------ Looking for a
					// description file.

					int n = 0;
					while (n < fileNameExtensions.length) {
						if (!createNewFileName(fileName2, fileName,
								fileNameExtensions[n]))
							return;
						// 1st data file was loaded into ``fileBuf1'',
						// so we load the 2nd one into ``fileBuf2''.
						// Do not load the first file again if names are equal.
						if (!fileName.equalsIgnoreCase(fileName2.toString())
								&& loadFile(fileName2.toString(), fileBuf2)) {
							if ((SID_fileSupport(fileBuf1, fileBuf2) == LoadStatus.LOAD_OK)
									|| (INFO_fileSupport(fileBuf1, fileBuf2) == LoadStatus.LOAD_OK)) {
								status = acceptSidTune(fileName, fileName2
										.toString(), fileBuf1);
								return;
							}
						}
						n++;
					}
					

					// --------------------------------------- Could not find a
					// description file.

					// Try some native C64 file formats
					ret = MUS_fileSupport(fileBuf1, fileBuf2);
					if (ret != LoadStatus.LOAD_NOT_MINE) {
						if (ret == LoadStatus.LOAD_ERROR)
							return;

						// Try to find second file.
						n = 0;
						while (n < fileNameExtensions.length) {
							if (!createNewFileName(fileName2, fileName,
									fileNameExtensions[n]))
								return;
							// 1st data file was loaded into ``fileBuf1'',
							// so we load the 2nd one into ``fileBuf2''.
							// Do not load the first file again if names are
							// equal.
							if (!fileName
									.equalsIgnoreCase(fileName2.toString())
									&& loadFile(fileName2.toString(), fileBuf2)) {
								// Check if tunes in wrong order and therefore
								// swap them here
								if (fileNameExtensions[n]
										.equalsIgnoreCase(".mus")) {
									if (MUS_fileSupport(fileBuf2, fileBuf1) == LoadStatus.LOAD_OK) {
										if (MUS_mergeParts(fileBuf2, fileBuf1))
											status = acceptSidTune(fileName2
													.toString(), fileName,
													fileBuf2);
										return;
									}
								} else {
									if (MUS_fileSupport(fileBuf1, fileBuf2) == LoadStatus.LOAD_OK) {
										if (MUS_mergeParts(fileBuf1, fileBuf2))
											status = acceptSidTune(fileName,
													fileName2.toString(),
													fileBuf1);
										return;
									}
								}
								// The first tune loaded ok, so ignore errors on
								// the
								// second tune, may find an ok one later
							}
							n++;
						}
						
						// No (suitable) second file, so reload first without
						// second
						fileBuf2.erase();
						MUS_fileSupport(fileBuf1, fileBuf2);
						status = acceptSidTune(fileName, null, fileBuf1);
						return;
					}

					// Now directly support x00 (p00, etc)
					ret = X00_fileSupport(fileName, fileBuf1);
					if (ret != LoadStatus.LOAD_NOT_MINE) {
						if (ret == LoadStatus.LOAD_OK)
							status = acceptSidTune(fileName, null, fileBuf1);
						return;
					}

					// Now directly support prgs and equivalents
					ret = PRG_fileSupport(fileName, fileBuf1);
					if (ret != LoadStatus.LOAD_NOT_MINE) {
						if (ret == LoadStatus.LOAD_OK)
							status = acceptSidTune(fileName, null, fileBuf1);
						return;
					}

					info.statusString = txt_unrecognizedFormat;
					return;
				}

				// -------------------------------------------------------------------------
				// Still unsuccessful ? Probably one put a description file name
				// into
				// ``fileName''. Assuming ``fileName'' to hold the name of a
				// description
				// file, we now create the name of the data file and swap both
				// used memory
				// buffers - fileBuf1 and fileBuf2 - when calling the format
				// support.
				// If it works, the second file is the data file ! If it is not,
				// but does
				// exist, we are out of luck, since we cannot detect data files.

				// Make sure ``fileBuf1'' contains a description file.
				else if (ret == LoadStatus.LOAD_OK) {

					// --------------------- Description file found. --- Looking
					// for a data file.

					int n = 0;
					while (n < fileNameExtensions.length) {
						if (!createNewFileName(fileName2, fileName,
								fileNameExtensions[n]))
							return;
						// 1st info file was loaded into ``fileBuf'',
						// so we load the 2nd one into ``fileBuf2''.
						// Do not load the first file again if names are equal.
						if (!fileName.equalsIgnoreCase(fileName2.toString()) &&

						loadFile(fileName2.toString(), fileBuf2)) {
							// -------------- Some data file found, now
							// identifying the description file.

							if ((SID_fileSupport(fileBuf2, fileBuf1) == LoadStatus.LOAD_OK)
									|| (INFO_fileSupport(fileBuf2, fileBuf1) == LoadStatus.LOAD_OK)) {
								status = acceptSidTune(fileName2.toString(),
										fileName, fileBuf2);
								return;
							}
						}
						n++;
					}
					

					// ---------------------------------------- No corresponding
					// data file found.

					info.statusString = txt_noDataFile;
					return;
				} // end else if ( = is description file )
			} // end else ( = is no singlefile )

			// ---------------------------------------------------------- File
			// I/O error.

		} // if loaddatafile
		else {
			// returned fileLen was 0 = error. The info.statusString is
			// already set then.
			return;
		}
	}

	/**
	 * Support for OR-ing two LoadStatus enums
	 * 
	 * @param support
	 * @param support2
	 * @return
	 */
	private LoadStatus orStatus(LoadStatus support, LoadStatus support2) {
		int val1 = (support == LoadStatus.LOAD_NOT_MINE) ? 0
				: (support == LoadStatus.LOAD_OK) ? 1 : 2;
		int val2 = (support2 == LoadStatus.LOAD_NOT_MINE) ? 0
				: (support2 == LoadStatus.LOAD_OK) ? 1 : 2;
		int erg = val1 | val2;
		return (erg == 0) ? LoadStatus.LOAD_NOT_MINE
				: (erg == 1) ? LoadStatus.LOAD_OK : LoadStatus.LOAD_ERROR;
	}

	private void deleteFileNameCopies() {
		// When will it be fully safe to call delete[](0) on every system?
		if (info.dataFileName != null)
			info.dataFileName = null;
		if (info.infoFileName != null)
			info.infoFileName = null;
		if (info.path != null)
			info.path = null;
		info.dataFileName = null;
		info.infoFileName = null;
		info.path = null;
	}

	/**
	 * Try to retrieve single-file sidtune from specified buffer.
	 */
	private void getFromBuffer(final short[] /* uint_least8_t* const */buffer,
			final int /* uint_least32_t */bufferLen) {
		// Assume a failure, so we can simply return.
		status = false;

		if (buffer == null || bufferLen == 0) {
			info.statusString = txt_empty;
			return;
		} else if (bufferLen > SIDTUNE_MAX_FILELEN) {
			info.statusString = txt_fileTooLong;
			return;
		}

		short[] /* uint_least8_t* */tmpBuf = new short /* uint_least8_t */[bufferLen]; 
//		if (null == tmpBuf) {
//			info.statusString = txt_notEnoughMemory;
//			return;
//		}
		System.arraycopy(buffer, 0, tmpBuf, 0, bufferLen);

		Buffer_sidtt /* Buffer_sidtt<const uint_least8_t> */buf1 = new Buffer_sidtt(
				tmpBuf, bufferLen);
		Buffer_sidtt /* Buffer_sidtt<const uint_least8_t> */buf2 = new Buffer_sidtt(); // empty

		if (decompressPP20(buf1) < 0)
			return;

		boolean foundFormat = false;
		LoadStatus ret;
		// Here test for the possible single file formats. --------------
		ret = PSID_fileSupport(buf1);
		if (ret != LoadStatus.LOAD_NOT_MINE) {
			if (ret == LoadStatus.LOAD_ERROR)
				return;
			foundFormat = true;
		} else {
			ret = MUS_fileSupport(buf1, buf2);
			if (ret != LoadStatus.LOAD_NOT_MINE) {
				if (ret == LoadStatus.LOAD_ERROR)
					return;
				foundFormat = MUS_mergeParts(buf1, buf2);
			} else {
				// No further single-file-formats available.
				info.statusString = txt_unrecognizedFormat;
			}
		}

		if (foundFormat) {
			status = acceptSidTune("-", "-", buf1);
		}
	}

	/**
	 * Cache the data of a single-file or two-file sidtune and its corresponding
	 * file names.
	 * 
	 * @param dataFileName
	 * @param infoFileName
	 * @param buf
	 * @return
	 */
	private boolean acceptSidTune(final String dataFileName,
			final String infoFileName,
			Buffer_sidtt /* Buffer_sidtt<const uint_least8_t>& */buf) {
		// @FIXME@ - MUS
		if (info.numberOfInfoStrings == 3) { // Add <?> (HVSC standard) to
			// missing title, author,
			// release fields
			for (int i = 0; i < 3; i++) {
				if (infoString[i].length() == 0) {
					infoString[i] = "<?>";
					info.infoString[i] = infoString[i];
				}
			}
		}

		deleteFileNameCopies();
		// Make a copy of the data file name and path, if available.
		if (dataFileName != null) {
			File file = new File(info.path = dataFileName);
			info.dataFileName = file.getName();
			info.path = file.getParentFile() != null ? file
					.getParentFile().getPath() : ""; // path
			// only
			if ((info.path == null) || (info.dataFileName == null)) {
				info.statusString = txt_notEnoughMemory;
				return false;
			}
		} else {
			// Provide empty strings.
			info.path = "";
			info.dataFileName = "";
		}
		// Make a copy of the info file name, if available.
		if (infoFileName != null) {
			info.infoFileName = new File(infoFileName).getName();
//			if ((tmp == null) || (info.infoFileName == null)) {
//				info.statusString = txt_notEnoughMemory;
//				return false;
//			}
		} else {
			// Provide empty string.
			info.infoFileName = "";
		}
		// Fix bad sidtune set up.
		if (info.songs > SIDTUNE_MAX_SONGS)
			info.songs = SIDTUNE_MAX_SONGS;
		else if (info.songs == 0)
			info.songs++;
		if (info.startSong > info.songs)
			info.startSong = 1;
		else if (info.startSong == 0)
			info.startSong++;

		if (info.musPlayer)
			MUS_setPlayerAddress();

		info.dataFileLen = buf.len();
		info.c64dataLen = buf.len() - fileOffset;

		// Calculate any remaining addresses and then
		// confirm all the file details are correct
		if (resolveAddrs(buf.get(), fileOffset) == false)
			return false;
		if (checkRelocInfo() == false)
			return false;
		if (checkCompatibility() == false)
			return false;

		if (info.dataFileLen >= 2) {
			// We only detect an offset of two. Some position independent
			// sidtunes contain a load address of 0xE000, but are loaded
			// to 0x0FFE and call player at 0x1000.
			info.fixLoad = (endian_little16(buf.get(), fileOffset) == (info.loadAddr + 2));
		}

		// Check the size of the data.
		if (info.c64dataLen > SIDTUNE_MAX_MEMORY) {
			info.statusString = txt_dataTooLong;
			return false;
		} else if (info.c64dataLen == 0) {
			info.statusString = txt_empty;
			return false;
		}

		cache.assign(buf.xferPtr(), buf.xferLen());

		info.statusString = txt_noErrors;
		return true;
	}

	private boolean createNewFileName(
			StringBuffer /* Buffer_sidtt<char>& */destString,
			final String sourceName, final String sourceExt) {
		if (destString.length()>0)
			destString.delete(0, destString.length() - 1);
		int extPos = sourceName.lastIndexOf(".");
		if (extPos != -1) {
			destString.append(sourceName.substring(0, extPos))
					.append(sourceExt);
		} else {
			destString.append(sourceName);
			destString.append(sourceExt);
		}
		return true;
	}

	public static class Decompressed {
		short[] /* uint_least8_t* */destBufRef;
	}

	/**
	 * @param buf
	 * @return 0 for no decompression (buf unchanged), 1 for decompression and
	 *         -1 for error
	 */
	private int decompressPP20(
			Buffer_sidtt /* Buffer_sidtt<const uint_least8_t>& */buf) {
		// Check for PowerPacker compression: load and decompress, if PP20 file.
		PP20 myPP = new PP20();
		int /* uint_least32_t */fileLen;
		if (myPP.isCompressed(buf.get(), buf.len())) {
			Decompressed decomp = new Decompressed();
			if (0 == (fileLen = myPP.decompress(buf.get(), buf.len(), decomp))) {
				info.statusString = myPP.getStatusString();
				return -1;
			} else {
				info.statusString = myPP.getStatusString();
				// Replace compressed buffer with uncompressed buffer.
				buf.assign(decomp.destBufRef, fileLen);
			}
			return 1;
		}
		return 0;
	}

}
