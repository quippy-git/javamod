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

/**
 * An instance of this structure is used to transport values to and from SidTune
 * objects.<BR>
 * You must read (i.e. activate) sub-song specific information via:
 * 
 * <pre>
 * final SidTuneInfo tuneInfo = SidTune[songNumber];
 * final SidTuneInfo tuneInfo = SidTune.getInfo();
 * void SidTune.getInfo(tuneInfo);
 * </pre>
 * 
 * Consider the following fields as read-only, because the SidTune class does
 * not provide an implementation of:
 * 
 * <pre>
 *  boolean setInfo(final SidTuneInfo)
 * </pre>
 * 
 * Currently, the only way to get the class to accept values which are written
 * to these fields is by creating a derived class.
 * 
 * @author Ken H�ndel
 * 
 */
public class SidTuneInfo {

	/**
	 * the name of the identified file format
	 */
	public String formatString;

	/**
	 * error/status message of last operation
	 */
	public String statusString;

	/**
	 * describing the speed a song is running at
	 */
	public String speedString;

	public int /* uint_least16_t */loadAddr;

	public int /* uint_least16_t */initAddr;

	public int /* uint_least16_t */playAddr;

	public int /* uint_least16_t */songs;

	public int /* uint_least16_t */startSong;

	/**
	 * The SID chip base address used by the sidtune.
	 * 
	 * 0xD400 (normal, 1st SID)
	 */
	public int /* uint_least16_t */sidChipBase1;

	/**
	 * The SID chip base address used by the sidtune.
	 * 
	 * 0xD?00 (2nd SID) or 0 (no 2nd SID)
	 */
	public int /* uint_least16_t */sidChipBase2;

	//
	// Available after song initialization.
	//

	/**
	 * the one that has been initialized
	 */
	public int /* uint_least16_t */currentSong;

	/**
	 * intended speed, see top
	 */
	public short /* uint_least8_t */songSpeed;

	/**
	 * intended speed, see top
	 */
	public short /* uint_least8_t */clockSpeed;

	/**
	 * First available page for relocation
	 */
	public short /* uint_least8_t */relocStartPage;

	/**
	 * Number of pages available for relocation
	 */
	public short /* uint_least8_t */relocPages;

	/**
	 * whether Sidplayer routine has been installed
	 */
	public boolean musPlayer;

	/**
	 * Sid Model required for this sid
	 */
	public int sidModel;

	/**
	 * compatibility requirements
	 */
	public int compatibility;

	/**
	 * whether load address might be duplicate
	 */
	boolean fixLoad;

	/**
	 * --- not yet supported ---
	 */
	//int /* uint_least16_t */songLength;

	/**
	 * Song title, credits, ... 0 = Title, 1 = Author, 2 = Copyright/Publisher
	 * 
	 * the number of available text info lines
	 */
	public short /* uint_least8_t */numberOfInfoStrings;

	/**
	 * holds text info from the format headers etc.
	 */
	public String infoString[] = new String[SidTune.SIDTUNE_MAX_CREDIT_STRINGS];

	/**
	 * --- not yet supported ---
	 */
	int /* uint_least16_t */numberOfCommentStrings;

	/**
	 * --- not yet supported ---
	 */
	String[] commentString;

	/**
	 * length of single-file sidtune file
	 */
	public int /* uint_least32_t */dataFileLen;

	/**
	 * length of raw C64 data without load address
	 */
	public int /* uint_least32_t */c64dataLen;

	/**
	 * path to sidtune files; "", if cwd
	 */
	String path;

	/**
	 * a first file: e.g. "foo.c64"; "", if none
	 */
	public String dataFileName;

	/**
	 * a second file: e.g. "foo.sid"; "", if none
	 */
	public String infoFileName;
}