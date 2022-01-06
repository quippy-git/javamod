/*
 * @(#) PlayListEntry.java
 *
 * Created on 03.12.2006 by Daniel Becker
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
package de.quippy.javamod.main.playlist;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import de.quippy.javamod.multimedia.MultimediaContainerManager;
import de.quippy.javamod.system.Helpers;

/**
 * @author Daniel Becker
 * @since 03.12.2006
 */
public class PlayListEntry
{
	private URL file;
	private boolean isActive;
	private boolean isSelected;
	private String songName;
	private Long duration;
	private Long timeIndexInFile;
	private PlayList playList;
	
	/**
	 * Constructor for PlayListEntry
	 * @param file
	 */
	public PlayListEntry(URL file, PlayList savedInPlaylist)
	{
		this.file = file;
		this.playList = savedInPlaylist;
	}
	/**
	 * Constructor for PlayListEntry
	 */
	public PlayListEntry(File file, PlayList savedInPlaylist) throws MalformedURLException
	{
		this(file.toURI().toURL(), savedInPlaylist);
	}
	/**
	 * Constructor for PlayListEntry
	 */
	public PlayListEntry(String fileName, PlayList savedInPlaylist) throws MalformedURLException
	{
		this(new URL(fileName), savedInPlaylist);
	}
	/**
	 * @return the file
	 */
	public URL getFile()
	{
		return file;
	}

	public void setFile(URL newFile)
	{
		file = newFile;
	}
	/**
	 * @param songName
	 * @since 27.02.2011
	 */
	public synchronized void setSongName(String songName)
	{
		this.songName = songName;
	}
	/**
	 * @param duration
	 * @since 27.02.2011
	 */
	public synchronized void setDuration(Long duration)
	{
		this.duration = duration;
	}
	/**
	 * @param duration
	 * @since 27.02.2011
	 */
	public synchronized void setDuration(long duration)
	{
		this.duration = Long.valueOf(duration);
	}
	/**
	 * @param timeIndex
	 * @since 13.02.2012
	 */
	public synchronized void setTimeIndexInFile(Long timeIndex)
	{
		this.timeIndexInFile = timeIndex;
	}
	/**
	 * @param timeIndex
	 * @since 13.02.2012
	 */
	public synchronized void setTimeIndexInFile(long timeIndex)
	{
		this.timeIndexInFile = Long.valueOf(timeIndex);
	}
	public synchronized String getFormattedName()
	{
		if (songName==null)
		{
			Object [] infos = MultimediaContainerManager.getSongInfosFor(file);
			songName = (String)infos[0];
			if (duration==null) duration = (Long)infos[1];
		}
		return songName;
	}
	public synchronized long getDuration()
	{
		if (duration==null)
		{
			Object [] infos = MultimediaContainerManager.getSongInfosFor(file);
			duration = (Long)infos[1];
			if (songName==null) songName = (String)infos[0];
		}
		return duration.longValue();
	}
	public synchronized String getDurationString()
	{
		return Helpers.getTimeStringFromMilliseconds(getDuration());
	}
	/**
	 * @return
	 * @since 13.02.2012
	 */
	public synchronized String getTimeIndexString()
	{
		if (timeIndexInFile==null)
			return " ";
		else
			return Helpers.getTimeStringFromMilliseconds(timeIndexInFile.longValue());
	}
	/**
	 * @return
	 * @since 13.02.2012
	 */
	public synchronized long getTimeIndex()
	{
		if (timeIndexInFile==null)
			return 0L;
		else
			return timeIndexInFile.longValue();
	}
	/**
	 * @return the songName if its not null
	 * @since 27.02.2011
	 */
	public synchronized String getQuickSongName()
	{
		if (songName==null)
			return MultimediaContainerManager.getSongNameFromURL(getFile());
		else
			return getFormattedName();
	}
	/**
	 * @return the duration string if its not null
	 * @since 27.02.2011
	 */
	public synchronized String getQuickDuration()
	{
		if (duration==null)
			return " ";
		else
			return getDurationString();
	}
	/**
	 * @return the isActive
	 */
	public boolean isActive()
	{
		return isActive;
	}
	/**
	 * @param isActive the isActive to set
	 */
	public void setActive(boolean isActive)
	{
		this.isActive = isActive;
	}
	/**
	 * @return the isActive
	 */
	public boolean isSelected()
	{
		return isSelected;
	}
	/**
	 * @param isActive the isActive to set
	 */
	public void setSelected(boolean isSelected)
	{
		this.isSelected = isSelected;
	}
	/**
	 * @return the indexInPlaylist
	 */
	public int getIndexInPlaylist()
	{
		if (playList!=null)
			return playList.indexOf(this);
		else
			return -1;
	}
	/**
	 * @param playList the playList to set
	 * @since 10.03.2011
	 */
	public void setSavedInPlaylist(PlayList playList)
	{
		this.playList = playList;
	}
	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return this.file.toString();
	}
}
