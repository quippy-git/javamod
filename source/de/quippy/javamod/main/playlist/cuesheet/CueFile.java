/*
 * @(#) CueFile.java
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

import java.net.URL;
import java.util.ArrayList;

/**
 * @author Daniel Becker
 *
 */
public class CueFile
{
	private URL file;
	private String type;
	private ArrayList<CueTrack> tracks;
	
	/**
	 * 
	 * @since 14.02.2012
	 */
	public CueFile()
	{
		super();
		tracks = new ArrayList<CueTrack>();
	}

	/**
	 * @return the file
	 * @since 14.02.2012
	 */
	public URL getFile()
	{
		return file;
	}
	/**
	 * @param file the file to set
	 * @since 14.02.2012
	 */
	public void setFile(URL file)
	{
		this.file = file;
	}
	/**
	 * @return the type
	 * @since 14.02.2012
	 */
	public String getType()
	{
		return type;
	}
	/**
	 * @param type the type to set
	 * @since 14.02.2012
	 */
	public void setType(String type)
	{
		this.type = type;
	}
	/**
	 * @param cueTrack
	 * @since 14.02.2012
	 */
	public void addTrack(CueTrack cueTrack)
	{
		tracks.add(cueTrack);
	}
	/**
	 * @return the tracks
	 * @since 14.02.2012
	 */
	public ArrayList<CueTrack> getTracks()
	{
		return tracks;
	}
}