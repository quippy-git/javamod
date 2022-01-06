/*
 * @(#) ID3v2Frames.java
 *
 * Created on 23.12.2008 by Daniel Becker
 * 
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

package de.quippy.javamod.multimedia.mp3.id3;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Description: 
 *  This class is a collection that is used to hold the ID3v2Frames.
 *
 * @author:  Jonathan Hilliker modified by Daniel Becker
 */
public class ID3v2Frames<K,V> extends HashMap<K,V>
{
	private static final long serialVersionUID = -1434484594524119778L;

	// Want to know what these fields store?  Go to www.id3.org
	public static final String ALBUM = "TALB";
	public static final String BPM = "TBPM";
	public static final String COMPOSER = "TCOM";
	public static final String CONTENT_TYPE = "TCON";
	public static final String COPYRIGHT_MESSAGE = "TCOP";
	public static final String ENCODING_TIME = "TDEN";
	public static final String PLAYLIST_DELAY = "TDLY";
	public static final String ORIGINAL_RELEASE_TIME = "TDOR";
	public static final String RECORDING_TIME = "TDRC";
	public static final String RELEASE_TIME = "TDRL";
	public static final String TAGGING_TIME = "TDTG";
	public static final String ENCODED_BY = "TENC";
	public static final String LYRICIST = "TEXT";
	public static final String FILE_TYPE = "TFLT";
	public static final String INVOLVED_PEOPLE = "TIPL";
	public static final String CONTENT_GROUP = "TIT1";
	public static final String TITLE = "TIT2";
	public static final String SUBTITLE = "TIT3";
	public static final String INITIAL_KEY = "TKEY";
	public static final String LANGUAGE = "TLAN";
	public static final String LENGTH = "TLEN";
	public static final String MUSICIAN_CREDITS = "TMCL";
	public static final String MEDIA_TYPE = "TMED";
	public static final String MOOD = "TMOO";
	public static final String ORIGINAL_ALBUM = "TOAL";
	public static final String ORIGINAL_FILENAME = "TOFN";
	public static final String ORIGINAL_LYRICIST = "TOLY";
	public static final String ORIGINAL_ARTIST = "TOPE";
	public static final String FILE_OWNER = "TOWN";
	public static final String LEAD_PERFORMERS = "TPE1";
	public static final String ACCOMPANIMENT = "TPE2";
	public static final String CONDUCTOR = "TPE3";
	public static final String REMIXED_BY = "TPE4";
	public static final String PART_OF_SET = "TPOS";
	public static final String PRODUCED_NOTICE = "TPRO";
	public static final String PUBLISHER = "TPUB";
	public static final String TRACK_NUMBER = "TRCK";
	public static final String INTERNET_RADIO_STATION_NAME = "TRSN";
	public static final String INTERNET_RADIO_STATION_OWNER = "TRSO";
	public static final String ALBUM_SORT_ORDER = "TSOA";
	public static final String PERFORMER_SORT_ORDER = "TSOP";
	public static final String TITLE_SORT_ORDER = "TSOT";
	public static final String ISRC = "TSRC";
	public static final String SOFTWARE_HARDWARE_SETTINGS = "TSSE";
	public static final String SET_SUBTITLE = "TSST";
	public static final String USER_DEFINED_TEXT_INFO = "TXXX";
	public static final String YEAR = "TYER";
	public static final String COMMERCIAL_INFO_URL = "WCOM";
	public static final String COPYRIGHT_INFO_URL = "WCOP";
	public static final String OFFICIAL_FILE_WEBPAGE_URL = "WOAF";
	public static final String OFFICIAL_ARTIST_WEBPAGE_URL = "WOAR";
	public static final String OFFICIAL_SOURCE_WEBPAGE_URL = "WOAS";
	public static final String OFFICIAL_INTERNET_RADIO_WEBPAGE_URL = "WOAS";
	public static final String PAYMENT_URL = "WPAY";
	public static final String OFFICIAL_PUBLISHER_WEBPAGE_URL = "WPUB";
	public static final String USER_DEFINED_URL = "WXXX";
	public static final String AUDIO_ENCRYPTION = "AENC";
	public static final String ATTACHED_PICTURE = "APIC";
	public static final String AUDIO_SEEK_POINT_INDEX = "ASPI";
	public static final String COMMENTS = "COMM";
	public static final String COMMERCIAL_FRAME = "COMR";
	public static final String ENCRYPTION_METHOD_REGISTRATION = "ENCR";
	public static final String EQUALISATION = "EQU2";
	public static final String EVENT_TIMING_CODES = "ETCO";
	public static final String GENERAL_ENCAPSULATED_OBJECT = "GEOB";
	public static final String GROUP_IDENTIFICATION_REGISTRATION = "GRID";
	public static final String LINKED_INFORMATION = "LINK";
	public static final String MUSIC_CD_IDENTIFIER = "MCDI";
	public static final String MPEG_LOCATION_LOOKUP_TABLE = "MLLT";
	public static final String OWNERSHIP_FRAME = "OWNE";
	public static final String PRIVATE_FRAME = "PRIV";
	public static final String PLAY_COUNTER = "PCNT";
	public static final String POPULARIMETER = "POPM";
	public static final String POSITION_SYNCHRONISATION_FRAME = "POSS";
	public static final String RECOMMENDED_BUFFER_SIZE = "RBUF";
	public static final String RELATIVE_VOLUME_ADJUSTMENT = "RVA2";
	public static final String REVERB = "RVRB";
	public static final String SEEK_FRAME = "SEEK";
	public static final String SIGNATURE_FRAME = "SIGN";
	public static final String SYNCHRONISED_LYRIC = "SYLT";
	public static final String SYNCHRONISED_TEMPO_CODES = "SYTC";
	public static final String UNIQUE_FILE_IDENTIFIER = "UFID";
	public static final String TERMS_OF_USE = "USER";
	public static final String UNSYNCHRONISED_LYRIC_TRANSCRIPTION = "USLT";

	/**
	 * Returns a string representation of this object.  Returns the toStrings
	 * of all the frames contained within seperated by line breaks.
	 *
	 * @return a string representation of this object
	 */
	public String toString()
	{
		StringBuilder str = new StringBuilder();

		Iterator<V> it = this.values().iterator();
		while (it.hasNext())
		{
			str.append(it.next().toString()).append('\n');
		}

		return str.toString();
	}

	/**
	 * Returns the length in bytes of all the frames contained in this object.
	 *
	 * @return the length of all the frames contained in this object.
	 */
	public int getLength()
	{
		int length = 0;

		Iterator<V> it = this.values().iterator();
		while (it.hasNext())
		{
			length += ((ID3v2Frame) it.next()).getFrameLength();
		}

		return length;
	}

	/**
	 * Return an array bytes containing all frames contained in this object.
	 * This can be used to easily write the frames to a file.
	 *
	 * @return an array of bytes contain all frames contained in this object
	 */
	public byte[] getBytes()
	{
		byte b[] = new byte[getLength()];
		int bytesCopied = 0;

		Iterator<V> it = this.values().iterator();
		while (it.hasNext())
		{
			ID3v2Frame frame = (ID3v2Frame) it.next();
			System.arraycopy(frame.getFrameBytes(), 0, b, bytesCopied, frame.getFrameLength());
			bytesCopied += frame.getFrameLength();
		}

		return b;
	}

}
