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

import de.quippy.javamod.system.Helpers;

/**
 * A tag parsed from an icecast tag.
 */
public class IcyTag implements Serializable
{
	private static final long serialVersionUID = -5433537975531168164L;

	private final String name;
	private final String value;

	public IcyTag(final String name, final String value)
	{
		this.name = (name!=null)?name.toLowerCase():"NULL";
		// This is somewhat ridiculous but unavoidable. We must read from the stream in ISO-8859-1 (cannot accept encoding UTF-8 - would mangle even more)
		// After that the headers (that are not affected by "Accept-Encodig" due to conventions) will contain two byte characters for UTF-8 ones - so we need to re-encode with UTF-8
		// and now do that in general here.
		this.value = Helpers.convertStringEncoding(value, Helpers.CODING_ICY, Helpers.CODING_UTF8);
	}
	public String getName()
	{
		return name;
	}
	public String getValue()
	{
		return value;
	}
	@Override
	public String toString()
	{
		return getName() + ":" + getValue();
	}
}
