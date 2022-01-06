/**
 *                          SIDPLAY INFOFILE format support.
 *                          --------------------------------
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

import static de.quippy.sidplay.libsidplay.components.sidtune.SidTune.LoadStatus.LOAD_NOT_MINE;
import de.quippy.sidplay.libsidplay.components.sidtune.SidTune.LoadStatus;

public class InfoFile {

//	private static final String text_format = "Raw plus SIDPLAY ASCII text file (SID)";
//	private static final String text_truncatedError = "SIDTUNE ERROR: SID file is truncated";
//	private static final String text_noMemError = "SIDTUNE ERROR: Not enough free memory";
//	private static final String text_invalidError = "SIDTUNE ERROR: File contains invalid data";
//	private static final String keyword_id = "SIDPLAY INFOFILE";

	//
	// No white-space characters we want to use a white-space eating string
	// stream to parse most of the header.
	//

//	private static final String keyword_name = "NAME=";
//	private static final String keyword_author = "AUTHOR=";
//	private static final String keyword_copyright = "COPYRIGHT="; // deprecated
//	private static final String keyword_released = "RELEASED=";
//	private static final String keyword_address = "ADDRESS=";
//	private static final String keyword_songs = "SONGS=";
//	private static final String keyword_speed = "SPEED=";
//	private static final String keyword_musPlayer = "SIDSONG=YES";
//	private static final String keyword_reloc = "RELOC=";
//	private static final String keyword_clock = "CLOCK=";
//	private static final String keyword_sidModel = "SIDMODEL=";
//	private static final String keyword_compatibility = "COMPATIBILITY=";

	/**
	 * Just to avoid a first segm.fault.
	 */
//	private static final int /* uint_least16_t */sidMinFileSize = 1 + keyword_id.length();

	/**
	 * Enough for all keywords incl. their values.
	 */
//	private static final int /* uint_least16_t */parseChunkLen = 80;

//	private SidTune sidtune;
//	private SidTuneInfo info;

	public InfoFile(SidTune sidtune) {
//		this.sidtune = sidtune;
//		this.info = sidtune.info;
	}

	protected LoadStatus INFO_fileSupport(
			Buffer_sidtt /* Buffer_sidtt<const uint_least8_t>& */dataBuf,
			Buffer_sidtt /* Buffer_sidtt<const uint_least8_t>& */sidBuf) {
		return LOAD_NOT_MINE;
		// int /* uint_least32_t */ sidBufLen = sidBuf.len();
		// // Make sure SID buffer pointer is not zero.
		// // Check for a minimum file size. If it is smaller, we will not
		// proceed.
		// if (sidBufLen<sidMinFileSize)
		// {
		// return LOAD_NOT_MINE;
		// }
		//
		// final short[] /* const char* */ pParseBuf = (short[] /* const char*
		// */)sidBuf.get();
		// int pParseBufPos = 0;
		// // First line has to contain the exact identification string.
		// if ( new String(pParseBuf).equals(keyword_id ))
		// {
		// // At least the ID was found, so set a default error message.
		// info.formatString = text_truncatedError;
		//	        
		// // Defaults.
		// sidtune.fileOffset = 0; // no header in separate data file
		// info.sidChipBase1 = 0xd400;
		// info.sidChipBase2 = 0;
		// info.musPlayer = false;
		// info.numberOfInfoStrings = 0;
		// long /* uint_least32_t */ oldStyleSpeed = 0;
		//
		// // Flags for required entries.
		// boolean hasAddress = false,
		// hasName = false,
		// hasAuthor = false,
		// hasReleased = false,
		// hasSongs = false,
		// hasSpeed = false,
		// hasInitAddr = false;
		//	    
		// // Using a temporary instance of an input string chunk.
		// char[] pParseChunk = new char[parseChunkLen+1];
		// if ( pParseChunk == null )
		// {
		// info.formatString = text_noMemError;
		// return LOAD_ERROR;
		// }
		//	        
		// // Parse as long we have not collected all ``required'' entries.
		// //while ( !hasAddress || !hasName || !hasAuthor || !hasCopyright
		// // || !hasSongs || !hasSpeed )
		//
		// // Above implementation is wrong, we need to get all known
		// // fields and then check if all ``required'' ones were found.
		// for (;;)
		// {
		// // Skip to next line. Leave loop, if none.
		// if (( returnNextLine( pParseBuf, pParseBufPos)) == 0 )
		// {
		// break;
		// }
		// // And get a second pointer to the following line.
		// int /* const char* */ pNextLine = returnNextLine( pParseBuf,
		// pParseBufPos );
		// int /* uint_least32_t */ restLen;
		// if ( pNextLine != 0 )
		// {
		// // Calculate number of chars between current pos and next line.
		// restLen = (int /* uint_least32_t */)(pNextLine - pParseBufPos);
		// }
		// else
		// {
		// // Calculate number of chars between current pos and end of buf.
		// restLen = sidBufLen - (int /* uint_least32_t */)(pParseBufPos);
		// }
		// istrstream parseStream((char *) pParseBuf, restLen );
		// istrstream parseCopyStream((char *) pParseBuf, restLen );
		// if ( !parseStream || !parseCopyStream )
		// {
		// break;
		// }
		// // Now copy the next X characters except white-spaces.
		// for ( uint_least16_t i = 0; i < parseChunkLen; i++ )
		// {
		// char c;
		// parseCopyStream >> c;
		// pParseChunk[i] = c;
		// }
		// pParseChunk[parseChunkLen]=0;
		// // Now check for the possible keywords.
		// // ADDRESS
		// if ( new String(pParseChunk).equals(keyword_address ) )
		// {
		// SidTuneTools::skipToEqu( parseStream );
		// info.loadAddr = (uint_least16_t)SidTuneTools::readHex( parseStream );
		// info.initAddr = info.loadAddr;
		// hasInitAddr = true;
		// if ( parseStream )
		// {
		// info.initAddr = (uint_least16_t)SidTuneTools::readHex( parseStream );
		// if ( !parseStream )
		// break;
		// info.playAddr = (uint_least16_t)SidTuneTools::readHex( parseStream );
		// hasAddress = true;
		// }
		// }
		// // NAME
		// else if ( new String( pParseChunk).equals(keyword_name ))
		// {
		// SidTuneTools::copyStringValueToEOL(pParseBuf,&infoString[0][0],SIDTUNE_MAX_CREDIT_STRLEN);
		// info.infoString[0] = &infoString[0][0];
		// hasName = true;
		// }
		// // AUTHOR
		// else if ( new String( pParseChunk).equals(keyword_author ) )
		// {
		// SidTuneTools::copyStringValueToEOL(pParseBuf,&infoString[1][0],SIDTUNE_MAX_CREDIT_STRLEN);
		// info.infoString[1] = &infoString[1][0];
		// hasAuthor = true;
		// }
		// // COPYRIGHT
		// else if ( new String( pParseChunk).equals(keyword_copyright ) )
		// {
		// SidTuneTools::copyStringValueToEOL(pParseBuf,&infoString[2][0],SIDTUNE_MAX_CREDIT_STRLEN);
		// info.infoString[2] = &infoString[2][0];
		// hasReleased = true;
		// }
		// // RELEASED
		// else if ( new String( pParseChunk).equals(keyword_released ) )
		// {
		// SidTuneTools::copyStringValueToEOL(pParseBuf,&infoString[2][0],SIDTUNE_MAX_CREDIT_STRLEN);
		// info.infoString[2] = &infoString[2][0];
		// hasReleased = true;
		// }
		// // SONGS
		// else if ( new String( pParseChunk).equals(keyword_songs ) )
		// {
		// SidTuneTools::skipToEqu( parseStream );
		// info.songs = (uint_least16_t)SidTuneTools::readDec( parseStream );
		// info.startSong = (uint_least16_t)SidTuneTools::readDec( parseStream
		// );
		// hasSongs = true;
		// }
		// // SPEED
		// else if ( new String( pParseChunk).equals(keyword_speed ) )
		// {
		// SidTuneTools::skipToEqu( parseStream );
		// oldStyleSpeed = SidTuneTools::readHex(parseStream);
		// hasSpeed = true;
		// }
		// // SIDSONG
		// else if ( new String( pParseChunk).equals(keyword_musPlayer ) )
		// {
		// info.musPlayer = true;
		// }
		// // RELOC
		// else if ( new String( pParseChunk).equals(keyword_reloc ) )
		// {
		// info.relocStartPage = (uint_least8_t)SidTuneTools::readHex(
		// parseStream );
		// if ( !parseStream )
		// break;
		// info.relocPages = (uint_least8_t)SidTuneTools::readHex( parseStream
		// );
		// }
		// // CLOCK
		// else if ( new String( pParseChunk).equals(keyword_clock ) )
		// {
		// char clock[] = new char[8];
		// SidTuneTools::copyStringValueToEOL(pParseBuf,clock,sizeof(clock));
		// if ( new String( clock).equals("UNKNOWN" ) )
		// info.clockSpeed = SIDTUNE_CLOCK_UNKNOWN;
		// else if ( new String( clock).equals("PAL" ) )
		// info.clockSpeed = SIDTUNE_CLOCK_PAL;
		// else if ( new String( clock).equals("NTSC" ) )
		// info.clockSpeed = SIDTUNE_CLOCK_NTSC;
		// else if ( new String( clock).equals("ANY" ) )
		// info.clockSpeed = SIDTUNE_CLOCK_ANY;
		// }
		// // SIDMODEL
		// else if ( new String( pParseChunk).equals(keyword_sidModel ) )
		// {
		// char model[] = new char[8];
		// SidTuneTools::copyStringValueToEOL(pParseBuf,model,sizeof(model));
		// if ( new String( model).equals("UNKNOWN" ) )
		// info.sidModel = SIDTUNE_SIDMODEL_UNKNOWN;
		// else if ( new String( model).equals("6581" ) )
		// info.sidModel = SIDTUNE_SIDMODEL_6581;
		// else if ( new String( model).equals("8580" ) )
		// info.sidModel = SIDTUNE_SIDMODEL_8580;
		// else if ( new String( model).equals("ANY" ) )
		// info.sidModel = SIDTUNE_SIDMODEL_ANY;
		// }
		// // COMPATIBILITY
		// else if ( new String( pParseChunk).equals(keyword_compatibility ) )
		// {
		// char comp[] = new char[6];
		// SidTuneTools::copyStringValueToEOL(pParseBuf,comp,sizeof(comp));
		// if ( new String( comp).equals("C64" ) )
		// info.compatibility = SIDTUNE_COMPATIBILITY_C64;
		// else if ( new String( comp).equals("PSID" ) )
		// info.compatibility = SIDTUNE_COMPATIBILITY_PSID;
		// else if ( new String( comp).equals("R64" ) )
		// info.compatibility = SIDTUNE_COMPATIBILITY_R64;
		// else if ( new String( comp).equals("BASIC" ) )
		// info.compatibility = SIDTUNE_COMPATIBILITY_BASIC;
		// }
		// }
		//
		// delete[] pParseChunk;
		//	        
		// if ( !(hasName && hasAuthor && hasReleased && hasSongs) )
		// { // Something is missing (or damaged ?).
		// // Error string set above.
		// return LOAD_ERROR;
		// }
		//
		// switch ( info.compatibility )
		// {
		// case SIDTUNE_COMPATIBILITY_PSID:
		// case SIDTUNE_COMPATIBILITY_C64:
		// if ( !(hasAddress && hasSpeed) )
		// return LOAD_ERROR; // Error set above
		// break;
		//
		// case SIDTUNE_COMPATIBILITY_R64:
		// if ( !(hasInitAddr || hasAddress) )
		// return LOAD_ERROR; // Error set above
		// // Allow user to provide single address
		// if ( !hasAddress )
		// info.loadAddr = 0;
		// else if ( info.loadAddr || info.playAddr )
		// {
		// info.formatString = text_invalidError;
		// return LOAD_ERROR;
		// }
		// // Deliberate run on
		//
		// case SIDTUNE_COMPATIBILITY_BASIC:
		// oldStyleSpeed = ~0;
		// }
		//
		// // Create the speed/clock setting table.
		// convertOldStyleSpeedToTables(oldStyleSpeed, info.clockSpeed);
		// info.numberOfInfoStrings = 3;
		// // We finally accept the input data.
		// info.formatString = text_format;
		// if ( info.musPlayer && !dataBuf.isEmpty() )
		// return MUS_load (dataBuf);
		// return LOAD_OK;
		// }
		// return LOAD_NOT_MINE;
	}

	// Search terminated string for next newline sequence.
	// Skip it and return pointer to start of next line.
	// int returnNextLine(final short[] s, int pos)
	// {
	// // Unix: LF = 0x0A
	// // Windows, DOS: CR,LF = 0x0D,0x0A
	// // Mac: CR = 0x0D
	// char c;
	// while ((c = (char)s[pos]) != 0)
	// {
	// pos++; // skip read character
	// if (c == 0x0A)
	// {
	// break; // LF found
	// }
	// else if (c == 0x0D)
	// {
	// if (s[pos] == 0x0A)
	// {
	// pos++; // CR,LF found, skip LF
	// }
	// break; // CR or CR,LF found
	// }
	// }
	// if (s[pos] == 0) // end of string ?
	// {
	// return 0; // no next line available
	// }
	// return pos; // next line available
	// }
}
