/*
 * @(#) OggMetaData.java
 *
 * Created on 01.11.2010 by Daniel Becker
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

package de.quippy.javamod.multimedia.ogg.metadata;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.quippy.javamod.io.FileOrPackedInputStream;
import de.quippy.javamod.multimedia.MultimediaContainerManager;

public class OggMetaData
{
	private URL urlName = null;
	private Map<String, String> oggInfo = null;
	private int lengthInMilliseconds;

	/**
	 * Create an id3v1tag from the file specified.  If the file contains a
	 * tag, the information is automatically extracted.
	 *
	 * @param mp3 the file to read/write the tag to
	 * @exception FileNotFoundException if an error occurs
	 * @exception IOException if an error occurs
	 */
	public OggMetaData(final URL oggFileURL)
	{
		super();
		readMetaData(oggFileURL);
	}
	public OggMetaData(final InputStream in)
	{
		super();
		readMetaData(in);
	}
	private void readMetaData(final InputStream in)
	{
		try
		{
		    final JOrbisComment jorbiscomment=new JOrbisComment();
		    jorbiscomment.read(in);
		    in.close();

		    lengthInMilliseconds = jorbiscomment.getLengthInMilliseconds();
		    oggInfo = new HashMap<>();
			// get data from vorbis comment
			for (int i = 99; i >= 0; --i)
			{
				final String comment = jorbiscomment.getComment().getComment(i);
				if (comment!=null && !comment.isEmpty())
				{
					final int equalIndex = comment.indexOf('=');
					final String key = comment.substring(0, equalIndex);
					final String value = new String(comment.substring(equalIndex+1).getBytes(), "UTF-8");
					if (equalIndex!=-1 && key!=null) oggInfo.put(key.toUpperCase(), value);
				}
			}
		}
		catch (final Exception ex)
		{
			throw new RuntimeException(ex);
		}
	}
	private void readMetaData(final URL oggFileURL)
	{
		InputStream in = null;
		try
		{

			urlName = oggFileURL;
			in = new FileOrPackedInputStream(oggFileURL);
			readMetaData(in);
		}
		catch (final Exception ex)
		{
			throw new RuntimeException(ex);
		}
		finally
		{
			if (in!=null)
			{
				try { in.close(); } catch (final IOException ex) { /* Log.error("IGNORED", ex); */ }
				in = null;
			}
		}
	}
	/**
	 * @return the lengthInMilliseconds
	 */
	public int getLengthInMilliseconds()
	{
		return lengthInMilliseconds;
	}
	public String getAlbum()
	{
		return oggInfo.get("ALBUM");
	}
	public String getTitle()
	{
		return oggInfo.get("TITLE");
	}
	public String getGenre()
	{
		return oggInfo.get("GENRE");
	}
	public String getComposer()
	{
		return oggInfo.get("COMPOSER");
	}
	public String getTrackNumber()
	{
		return oggInfo.get("TRACKNUMBER");
	}
	public String getComment()
	{
		return oggInfo.get("COMMENT");
	}
	public String getPublisher()
	{
		return oggInfo.get("PUBLISHER");
	}
	public String getBPM()
	{
		return oggInfo.get("bpm");
	}
	public String getArtist()
	{
		return oggInfo.get("ARTIST");
	}
	public String getDiscNumber()
	{
		return oggInfo.get("DISCNUMBER");
	}
	public String getAlbumArtist()
	{
		return oggInfo.get("ALBUMARTIST");
	}
	public String getDate()
	{
		return oggInfo.get("DATE");
	}
	/**
	 * A short description like winamp does in its default
	 * @since 26.12.2008
	 * @return
	 */
	public String getShortDescription()
	{
		final String artist = getArtist();
		final String album = getAlbum();
		String title = getTitle();

		final StringBuilder str = new StringBuilder();
		if (artist!=null && !artist.isEmpty())
		{
			str.append(artist).append(" - ");
		}
		if (album!=null && !album.isEmpty())
		{
			str.append(album).append(" - ");
		}
		if (title==null || !title.isEmpty()) title = MultimediaContainerManager.getSongNameFromURL(urlName);
		return str.append(title).toString();
	}
	@Override
	public String toString()
	{
		final StringBuilder builder = new StringBuilder("OggMetaData\nURL\t\t");
		builder.append(urlName);
		final Set<String> keys = oggInfo.keySet();
		for (final String key : keys)
		{
			final String value = oggInfo.get(key);
			builder.append('\n').append(key).append("\t\t").append(value);
		}
		builder.append("\nLength:\t\t\t").append(getLengthInMilliseconds());
		return builder.toString();
	}
}

