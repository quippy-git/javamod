/*
 * IcyTag.
 * 
 * jicyshout : http://sourceforge.net/projects/jicyshout/
 *  
 * JavaZOOM : mp3spi@javazoom.net
 * 			  http://www.javazoom.net
 * 
 *-----------------------------------------------------------------------
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published
 *   by the Free Software Foundation; either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this program; if not, write to the Free Software
 *   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *----------------------------------------------------------------------
 */

package de.quippy.javamod.multimedia.mp3.streaming;

import java.io.Serializable;

/** 
 * A tag parsed from an icecast tag. 
 */
public class IcyTag implements Serializable
{
	private static final long serialVersionUID = -5433537975531168164L;

	private String name;
	private String value;

	public IcyTag(String name, String value)
	{
		this.name = name;
		this.value = value;
	}
	public String getName()
	{
		return name;
	}
	public String getValue()
	{
		return value;
	}
	public String toString()
	{
		return getName() + ":" + getValue();
	}
}
