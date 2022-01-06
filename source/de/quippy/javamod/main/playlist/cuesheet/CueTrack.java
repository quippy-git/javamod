/*
 * @(#) CueTrack.java
 *
 * Created on 14.02.2012 by Daniel Becker
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
package de.quippy.javamod.main.playlist.cuesheet;

import java.util.ArrayList;

/**
 * @author Daniel Becker
 *
 */
public class CueTrack
{
	private int trackNo;
	private String format;
	private String title;
	private String performer;
	private String songwriter;
	private ArrayList<CueIndex> indexes;

	/**
	 * 
	 * @since 14.02.2012
	 */
	public CueTrack()
	{
		super();
		indexes = new ArrayList<CueIndex>();
	}

	/**
	 * @return the trackNo
	 * @since 14.02.2012
	 */
	public int getTrackNo()
	{
		return trackNo;
	}
	/**
	 * @param trackNo the trackNo to set
	 * @since 14.02.2012
	 */
	public void setTrackNo(int trackNo)
	{
		this.trackNo = trackNo;
	}
	/**
	 * @return the format
	 * @since 14.02.2012
	 */
	public String getFormat()
	{
		return format;
	}

	/**
	 * @param format the format to set
	 * @since 14.02.2012
	 */
	public void setFormat(String format)
	{
		this.format = format;
	}

	/**
	 * @return the title
	 * @since 14.02.2012
	 */
	public String getTitle()
	{
		return title;
	}
	/**
	 * @param title the title to set
	 * @since 14.02.2012
	 */
	public void setTitle(String title)
	{
		this.title = title;
	}
	/**
	 * @return the performer
	 * @since 14.02.2012
	 */
	public String getPerformer()
	{
		return performer;
	}
	/**
	 * @param performer the performer to set
	 * @since 14.02.2012
	 */
	public void setPerformer(String performer)
	{
		this.performer = performer;
	}
	/**
	 * @return the songwriter
	 * @since 14.02.2012
	 */
	public String getSongwriter()
	{
		return songwriter;
	}
	/**
	 * @param songwriter the songwriter to set
	 * @since 14.02.2012
	 */
	public void setSongwriter(String songwriter)
	{
		this.songwriter = songwriter;
	}
	/**
	 * @param cueTrack
	 * @since 14.02.2012
	 */
	public void addIndex(CueIndex index)
	{
		indexes.add(index);
	}
	/**
	 * @return the tracks
	 * @since 14.02.2012
	 */
	public ArrayList<CueIndex> getIndexes()
	{
		return indexes;
	}
}
