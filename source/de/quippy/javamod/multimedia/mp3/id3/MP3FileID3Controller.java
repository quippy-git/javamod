/*
 * @(#) MP3FileID3Controller.java
 *
 * Created on 24.12.2008 by Daniel Becker
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;

import de.quippy.javamod.io.RandomAccessInputStream;
import de.quippy.javamod.io.RandomAccessInputStreamImpl;
import de.quippy.javamod.multimedia.MultimediaContainerManager;
import de.quippy.javamod.multimedia.mp3.id3.exceptions.ID3v2FormatException;
import de.quippy.javamod.system.Log;

/**
 * @author Daniel Becker
 * @since 24.12.2008
 */
public class MP3FileID3Controller
{
	public static final int ID3V1 = 1;
	public static final int ID3V2 = 2;

	private File mp3File;
	private ID3v1Tag id3v1;
	private ID3v2Tag id3v2;
	private MPEGAudioFrameHeader head;

	public MP3FileID3Controller(URL mp3URL)
	{
		super();
		getTagsFromURL(mp3URL);
	}
	public MP3FileID3Controller(RandomAccessInputStream raf)
	{
		super();
		getTagsFromRAF(raf);
	}
	private void getTagsFromURL(URL mp3URL)
	{
		RandomAccessInputStream raf = null;
		try
		{
			raf = new RandomAccessInputStreamImpl(mp3URL);
			getTagsFromRAF(raf);
		}
		catch (Throwable ex)
		{
			throw new RuntimeException(ex);
		}
		finally
		{
			if (raf!=null) try { raf.close(); } catch (IOException ex) { Log.error("IGNORED", ex); }
		}
	}
	private void getTagsFromRAF(RandomAccessInputStream raf)
	{
		try
		{
			mp3File = raf.getFile();
			id3v1 = new ID3v1Tag(raf);
			id3v2 = new ID3v2Tag(raf);
			head = new MPEGAudioFrameHeader(raf, id3v2.getSize());
		}
		catch (Throwable ex)
		{
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Removes id3 tags from the file.  The argument specifies which tags to
	 * remove.  This can either be BOTH_TAGS, ID3V1_ONLY, ID3V2_ONLY, or
	 * EXISTING_TAGS_ONLY.
	 *
	 * @param type specifies what tag(s) to remove
	 * @exception FileNotFoundException if an error occurs
	 * @exception IOException if an error occurs
	 */
	public void removeTags(int type) throws FileNotFoundException, IOException
	{
		RandomAccessFile raf = null;
		try
		{
			raf = new RandomAccessFile(mp3File, "rw");

			if (allow(type&ID3V1))
			{
				id3v1.removeTag(raf);
			}
			if (allow(type&ID3V2))
			{
				id3v2.removeTag(raf);
			}
		}
		finally
		{
			if (raf != null) raf.close();
		}
	}

	/**
	 * Writes the current state of the id3 tags to the file.  What tags are
	 * written depends upon the tagType passed to the constructor.
	 *
	 * @exception FileNotFoundException if an error occurs
	 * @exception IOException if an error occurs
	 */
	public void writeTags() throws FileNotFoundException, IOException
	{
		RandomAccessFile raf = null;
		try
		{
			raf = new RandomAccessFile(mp3File, "rw");
			// Write out id3v2 first because if the filesize is changed when an
			// id3v2 is written then the id3v1 may be moved away from the end
			// of the file which would cause it to not be recognized.
			if (id3v2.tagExists())
			{
				id3v2.writeTag(raf);
			}
			if (id3v1.tagExists())
			{
				id3v1.writeTag(raf);
			}
		}
		finally
		{
			if (raf != null) raf.close();
		}
	}

	/**
	 * Set the title of this mp3.
	 *
	 * @param title the title of the mp3
	 */
	public void setTitle(String title, int type)
	{
		if (allow(type&ID3V1))
		{
			id3v1.setTitle(title);
		}
		if (allow(type&ID3V2))
		{
			id3v2.setTextFrame(ID3v2Frames.TITLE, title);
		}
	}

	/**
	 * Set the album of this mp3.
	 *
	 * @param album the album of the mp3
	 */
	public void setAlbum(String album, int type)
	{
		if (allow(type&ID3V1))
		{
			id3v1.setAlbum(album);
		}
		if (allow(type&ID3V2))
		{
			id3v2.setTextFrame(ID3v2Frames.ALBUM, album);
		}
	}

	/**
	 * Set the artist of this mp3.
	 *
	 * @param artist the artist of the mp3
	 */
	public void setArtist(String artist, int type)
	{
		if (allow(type&ID3V1))
		{
			id3v1.setArtist(artist);
		}
		if (allow(type&ID3V2))
		{
			id3v2.setTextFrame(ID3v2Frames.LEAD_PERFORMERS, artist);
		}
	}

	/**
	 * Add a comment to this mp3.
	 *
	 * @param comment a comment to add to the mp3
	 */
	public void setComment(String comment, int type)
	{
		if (allow(type&ID3V1))
		{
			id3v1.setComment(comment);
		}
		if (allow(type&ID3V2))
		{
			id3v2.setCommentFrame("", comment);
		}
	}

	/**
	 * Set the genre of this mp3.
	 *
	 * @param genre the genre of the mp3
	 */
	public void setGenre(String genre, int type)
	{
		if (allow(type&ID3V1))
		{
			id3v1.setGenreString(genre);
		}
		if (allow(type&ID3V2))
		{
			id3v2.setTextFrame(ID3v2Frames.CONTENT_TYPE, genre);
		}
	}

	/**
	 * Set the year of this mp3.
	 *
	 * @param year of the mp3
	 */
	public void setYear(String year, int type)
	{
		if (allow(type&ID3V1))
		{
			id3v1.setYear(year);
		}
		if (allow(type&ID3V2))
		{
			id3v2.setTextFrame(ID3v2Frames.YEAR, year);
		}
	}

	/**
	 * Set the track number of this mp3.
	 *
	 * @param track the track number of this mp3
	 */
	public void setTrack(int track, int type)
	{
		if (allow(type&ID3V1))
		{
			id3v1.setTrack(track);
		}
		if (allow(type&ID3V2))
		{
			id3v2.setTextFrame(ID3v2Frames.TRACK_NUMBER, String.valueOf(track));
		}
	}

	/**
	 * Set the composer of this mp3 (id3v2 only).
	 *
	 * @param composer the composer of this mp3
	 */
	public void setComposer(String composer)
	{
		if (allow(ID3V2))
		{
			id3v2.setTextFrame(ID3v2Frames.COMPOSER, composer);
		}
	}

	/**
	 * Set the original artist of this mp3 (id3v2 only).
	 *
	 * @param artist the original artist of this mp3
	 */
	public void setOriginalArtist(String artist)
	{
		if (allow(ID3V2))
		{
			id3v2.setTextFrame(ID3v2Frames.ORIGINAL_ARTIST, artist);
		}
	}

	/**
	 * Add some copyright information to this mp3 (id3v2 only).
	 *
	 * @param copyright copyright information related to this mp3
	 */
	public void setCopyrightInfo(String copyright)
	{
		if (allow(ID3V2))
		{
			id3v2.setTextFrame(ID3v2Frames.COPYRIGHT_MESSAGE, copyright);
		}
	}

	/**
	 * Add a link to this mp3 (id3v2 only).  This includes a description of 
	 * the url and the url itself.
	 *
	 * @param desc a description of the url
	 * @param url the url itself
	 */
	public void setUserDefinedURL(String desc, String url)
	{
		if (allow(ID3V2))
		{
			id3v2.setUserDefinedURLFrame(desc, url);
		}
	}

	/**
	 * Add a field of miscellaneous text (id3v2 only).  This includes a 
	 * description of the text and the text itself.
	 *
	 * @param desc a description of the text
	 * @param text the text itself
	 */
	public void setUserDefinedText(String desc, String text)
	{
		if (allow(ID3V2))
		{
			id3v2.setUserDefinedTextFrame(desc, text);
		}
	}

	/**
	 * Set who encoded the mp3 (id3v2 only).
	 *
	 * @param encBy who encoded the mp3
	 */
	public void setEncodedBy(String encBy)
	{
		if (allow(ID3V2))
		{
			id3v2.setTextFrame(ID3v2Frames.ENCODED_BY, encBy);
		}
	}

	/**
	 * Set the text of the text frame specified by the id (id3v2 only).  The
	 * id should be one of the static strings specifed in ID3v2Frames class.
	 * All id's that begin with 'T' (excluding "TXXX") are considered text
	 * frames.
	 *
	 * @param id the id of the frame to set the data for
	 * @param data the data to set
	 */
	public void setTextFrame(String id, String data)
	{
		if (allow(ID3V2))
		{
			id3v2.setTextFrame(id, data);
		}
	}

	/**
	 * Set the data of the frame specified by the id (id3v2 only).  The id 
	 * should be one of the static strings specified in ID3v2Frames class.
	 *
	 * @param id the id of the frame to set the data for
	 * @param data the data to set
	 */
	public void setFrameData(String id, byte[] data)
	{
		if (allow(ID3V2))
		{
			id3v2.updateFrameData(id, data);
		}
	}

	/**
	 * Returns the length (in seconds) of the playing time of this mp3.  This
	 * will not return an accurate value for VBR files.
	 *
	 * @return the playing time (in seconds) of this mp3
	 */
	public long getPlayingTime()
	{
		long datasize = (mp3File.length() * 8) - id3v2.getSize();
		long bps = head.getBitRate() * 1000;
		return datasize / bps;
	}

	/**
	 * Return a formatted version of the getPlayingTime method.  The string
	 * will be formated "m:ss" where 'm' is minutes and 'ss' is seconds.
	 *
	 * @return a formatted version of the getPlayingTime method
	 */
	public String getPlayingTimeString()
	{
		long time = getPlayingTime();
		long mins = time / 60;
		long secs = time % 60;

		StringBuilder str = new StringBuilder();
		if (mins<10) str.append('0');
		str.append(mins).append(':');
		if (secs<10) str.append('0');
		str.append(secs);
		return str.toString();
	}

	/**
	 * Returns true if an id3v2 tag currently exists.
	 *
	 * @return true if an id3v2 tag currently exists
	 */
	public boolean id3v2Exists()
	{
		return id3v2!=null && id3v2.tagExists();
	}

	/**
	 * Returns true if an id3v1 tag currently exists.
	 *
	 * @return true if an id3v1 tag currently exists
	 */
	public boolean id3v1Exists()
	{
		return id3v1!=null && id3v1.tagExists();
	}

	/**
	 * Returns true if this file is an mp3.  This means simply that an
	 * MPEGAudioFrameHeader was found and the layer is 3.
	 *
	 * @return true if this file is an mp3
	 */
	public boolean isMP3()
	{
		return head.isMP3();
	}

	/**
	 * Returns the bit rate of this mp3 in kbps.
	 *
	 * @return the bit rate of this mp3 in kbps
	 */
	public int getBitRate()
	{
		return head.getBitRate();
	}

	/**
	 * Returns the sample rate of this mp3 in Hz.
	 *
	 * @return the sample reate of this mp3 in Hz
	 */
	public int getSampleRate()
	{
		return head.getSampleRate();
	}

	/**
	 * Returns the emphasis of this mp3.
	 *
	 * @return the emphasis of this mp3
	 */
	public String getMPEGEmphasis()
	{
		return head.getEmphasis();
	}

	/**
	 * Returns a string specifying the layer of the mpeg.  Ex: Layer III
	 *
	 * @return a string specifying the layer of the mpeg
	 */
	public String getMPEGLayer()
	{
		return head.getLayer();
	}

	/**
	 * Returns a string specifying the version of the mpeg.  This can either 
	 * be 1.0, 2.0, or 2.5.
	 *
	 * @return a string specifying the version of the mpeg
	 */
	public String getMPEGVersion()
	{
		return head.getVersion();
	}

	/**
	 * Return the channel mode of the mpeg.  Ex: Stereo
	 *
	 * @return the channel mode of the mpeg
	 */
	public String getMPEGChannelMode()
	{
		return head.getChannelMode();
	}

	/**
	 * Returns true if this mpeg is copyrighted.
	 *
	 * @return true if this mpeg is copyrighted
	 */
	public boolean isMPEGCopyrighted()
	{
		return head.isCopyrighted();
	}

	/**
	 * Returns true if this mpeg is the original.
	 *
	 * @return true if this mpeg is the original
	 */
	public boolean isMPEGOriginal()
	{
		return head.isOriginal();
	}

	/**
	 * Returns true if this mpeg is protected by CRC.
	 *
	 * @return true if this mpeg is protected by CRC
	 */
	public boolean isMPEGProtected()
	{
		return head.isProtected();
	}

	/**
	 * Returns the artist of the mp3 if set and the empty string if not.
	 *
	 * @return the artist of the mp3
	 * @exception ID3v2FormatException if the data of the field is incorrect
	 */
	public String getArtist(int type)
	{
		if (allow(type&ID3V1))
		{
			return id3v1.getArtist();
		}
		if (allow(type&ID3V2))
		{
			return id3v2.getFrameDataString(ID3v2Frames.LEAD_PERFORMERS);
		}
		return null;
	}

	/**
	 * Returns the album of the mp3 if set and the empty string if not.
	 *
	 * @return the album of the mp3
	 * @exception ID3v2FormatException if the data of the field is incorrect
	 */
	public String getAlbum(int type)
	{
		if (allow(type&ID3V1))
		{
			return id3v1.getAlbum();
		}
		if (allow(type&ID3V2))
		{
			return id3v2.getFrameDataString(ID3v2Frames.ALBUM);
		}
		return null;
	}

	/**
	 * Returns the comment field of this mp3 if set and the empty string if
	 * not.
	 *
	 * @return the comment field of this mp3
	 * @exception ID3v2FormatException if the data of the field is incorrect
	 */
	public String getComment(int type)
	{
		if (allow(type&ID3V1))
		{
			return id3v1.getComment();
		}
		if (allow(type&ID3V2))
		{
			return id3v2.getFrameDataString(ID3v2Frames.COMMENTS);
		}
		return null;
	}

	/**
	 * Returns the genre of this mp3 if set and the empty string if not.
	 *
	 * @return the genre of this mp3
	 * @exception ID3v2FormatException if the data of this field is incorrect
	 */
	public String getGenre(int type)
	{
		if (allow(type&ID3V1))
		{
			return id3v1.getGenreString();
		}
		if (allow(type&ID3V2))
		{
			return id3v2.getFrameDataString(ID3v2Frames.CONTENT_TYPE);
		}
		return null;
	}

	/**
	 * Returns the title of this mp3 if set and the empty string if not.
	 *
	 * @return the title of this mp3
	 * @exception ID3v2FormatException if the data of this field is incorrect
	 */
	public String getTitle(int type)
	{
		if (allow(type&ID3V1))
		{
			return id3v1.getTitle();
		}
		if (allow(type&ID3V2))
		{
			return id3v2.getFrameDataString(ID3v2Frames.TITLE);
		}
		return null;
	}

	/**
	 * Returns the track of this mp3 if set and the empty string if not.
	 *
	 * @return the track of this mp3
	 * @exception ID3v2FormatException if the data of this field is incorrect
	 */
	public String getTrack(int type)
	{
		if (allow(type&ID3V1))
		{
			return Integer.toString(id3v1.getTrack());
		}
		if (allow(type&ID3V2))
		{
			return id3v2.getFrameDataString(ID3v2Frames.TRACK_NUMBER);
		}
		return null;
	}

	/**
	 * Returns the year of this mp3 if set and the empty string if not.
	 *
	 * @return the year of this mp3
	 * @exception ID3v2FormatException if the data of this field is incorrect
	 */
	public String getYear(int type)
	{
		if (allow(type&ID3V1))
		{
			return id3v1.getYear();
		}
		if (allow(type&ID3V2))
		{
			return id3v2.getFrameDataString(ID3v2Frames.YEAR);
		}
		return null;
	}

	/**
	 * Returns the composer of this mp3 if set and the empty string if not
	 * (id3v2 only).
	 *
	 * @return the composer of this mp3
	 * @exception ID3v2FormatException if the data of this field is incorrect
	 */
	public String getComposer()
	{
		if (allow(ID3V2))
		{
			return id3v2.getFrameDataString(ID3v2Frames.COMPOSER);
		}
		return null;
	}

	/**
	 * Returns the number of CD this song is on
	 * (id3v2 only).
	 *
	 * @return the number of the CD this song is on
	 * @exception ID3v2FormatException if the data of this field is incorrect
	 */
	public String getDisc()
	{
		if (allow(ID3V2))
		{
			return id3v2.getFrameDataString(ID3v2Frames.PART_OF_SET);
		}
		return null;
	}

	/**
	 * Returns the original artist of this mp3 if set and the empty string
	 * if not (id3v2 only).
	 *
	 * @return the original artist of this mp3
	 * @exception ID3v2FormatException if the data of this field is incorrect
	 */
	public String getOriginalArtist()
	{
		if (allow(ID3V2))
		{
			return id3v2.getFrameDataString(ID3v2Frames.ORIGINAL_ARTIST);
		}
		return null;
	}

	/**
	 * Returns the album artist of this mp3 if set and the empty string
	 * if not (id3v2 only).
	 *
	 * @return the album artist of this mp3
	 * @exception ID3v2FormatException if the data of this field is incorrect
	 */
	public String getAlbumArtist()
	{
		if (allow(ID3V2))
		{
			return id3v2.getFrameDataString(ID3v2Frames.ACCOMPANIMENT);
		}
		return null;
	}

	/**
	 * Returns the original artist of this mp3 if set and the empty string
	 * if not (id3v2 only).
	 *
	 * @return the original artist of this mp3
	 * @exception ID3v2FormatException if the data of this field is incorrect
	 */
	public String getPublisher()
	{
		if (allow(ID3V2))
		{
			return id3v2.getFrameDataString(ID3v2Frames.PUBLISHER);
		}
		return null;
	}

	/**
	 * Returns the copyright info of this mp3 if set and the empty string
	 * if not (id3v2 only).
	 *
	 * @return the copyright info of this mp3
	 * @exception ID3v2FormatException if the data of this field is incorrect
	 */
	public String getCopyrightInfo()
	{
		if (allow(ID3V2))
		{
			return id3v2.getFrameDataString(ID3v2Frames.COPYRIGHT_MESSAGE);
		}
		return null;
	}

	/**
	 * Returns the user defined url of this mp3 if set and the empty string
	 * if not (id3v2 only).
	 *
	 * @return the user defined url of this mp3
	 * @exception ID3v2FormatException if the data of this field is incorrect
	 */
	public String getUserDefinedURL()
	{
		if (allow(ID3V2))
		{
			return id3v2.getFrameDataString(ID3v2Frames.USER_DEFINED_URL);
		}
		return null;
	}

	/**
	 * Returns who encoded this mp3 if set and the empty string if not 
	 * (id3v2 only).
	 *
	 * @return who encoded this mp3
	 * @exception ID3v2FormatException if the data of this field is incorrect
	 */
	public String getEncodedBy()
	{
		if (allow(ID3V2))
		{
			return id3v2.getFrameDataString(ID3v2Frames.ENCODED_BY);
		}
		return null;
	}

	/**
	 * Returns the BPM of this song 
	 * (id3v2 only).
	 *
	 * @return BPM of this mp3
	 * @exception ID3v2FormatException if the data of this field is incorrect
	 */
	public String getBPM()
	{
		if (allow(ID3V2))
		{
			return id3v2.getFrameDataString(ID3v2Frames.BPM);
		}
		return null;
	}

	/**
	 * Returns the textual information contained in the frame specifed by the
	 * id.  If the frame does not contain any textual information or does not
	 * exist, then the empty string is returned (id3v2 only).  The id should 
	 * be one of the static strings defined in the ID3v2Frames class.
	 *
	 * @param id the id of the frame to get data from
	 * @return the textual information of the frame
	 * @exception ID3v2FormatException if the data of the frame is incorrect
	 */
	public String getFrameDataString(String id)
	{
		if (allow(ID3V2))
		{
			return id3v2.getFrameDataString(id);
		}
		return null;
	}

	/**
	 * Returns the data contained in the frame specified by the id (id3v2 only)
	 * .  If the frame does not exist, a zero length array will be returned.
	 * The id should be one of the static strings defined in the ID3v2Frames
	 * class.
	 *
	 * @param id the id of the frame to get data from
	 * @return the data contained in the frame
	 */
	public byte[] getFrameDataBytes(String id)
	{
		if (allow(ID3V2))
		{
			return id3v2.getFrameData(id);
		}
		return null;
	}

	/**
	 * returns the full path of this mp3 file
	 * @since 26.12.2008
	 * @return
	 */
	public String getFileName()
	{
		if (mp3File==null) return "";
		try
		{
			return mp3File.getCanonicalPath();
		}
		catch (IOException ex)
		{
			return mp3File.getAbsolutePath();
		}
	}
	
	/**
	 * A short description like winamp does in its default
	 * @since 26.12.2008
	 * @return
	 */
	public String getShortDescription()
	{
		String artist = null;
		String album = null;
		String title = null;
		
		if (id3v2Exists())
		{
			artist = getArtist(ID3V2);
			title = getTitle(ID3V2);
			album = getAlbum(ID3V2);
		}
		if (id3v1Exists())
		{
			if (artist==null || artist.length()==0) artist = getArtist(ID3V1);
			if (title==null || title.length()==0) title = getTitle(ID3V1);
			if (album==null || album.length()==0) album = getAlbum(ID3V1);
		}
		
		StringBuilder str = new StringBuilder();
		if (artist!=null && artist.length()!=0)
		{
			str.append(artist).append(" - ");
		}
		if (album!=null && album.length()!=0)
		{
			str.append(album).append(" - ");
		}
		if (title==null || title.length()==0) title = MultimediaContainerManager.getSongNameFromFile(mp3File);
		return str.append(title).toString();
	}

	/**
	 * Checks whether it is ok to read or write from the tag version specified
	 * based on the tagType passed to the method.  The tagVersion parameter
	 * should be either ID3V1 or ID3V2.  The type parameter should be either
	 * BOTH_TAGS, ID3V1_ONLY, ID3V2_ONLY, NO_TAGS, or EXISTING_TAGS_ONLY.
	 *
	 * @param tagVersion the id3 version to check
	 * @param type specifies what conditions the tags are allowed to proceed
	 * @return true if it is ok to proceed with the read/write
	 */
	private boolean allow(int type)
	{
		return (((type&ID3V1)!=0 && id3v1Exists()) ||
				((type&ID3V2)!=0 && id3v2Exists()));
	}
	
	/**
	 * Return a string representation of this object.  This includes all the 
	 * information contained within the mpeg header and id3 tags as well as
	 * certain file attributes.
	 *
	 * @return a string representation of this object
	 */
	public String toString()
	{
		return "MP3File" + "\nPath:\t\t\t\t" + mp3File.getAbsolutePath() + "\nFileSize:\t\t\t" + mp3File.length() + " bytes\nPlayingTime:\t\t\t" + getPlayingTimeString() + "\n" + head + "\n" + id3v1 + "\n" + id3v2;
	}
}
