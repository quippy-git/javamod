/*
 * @(#) PlaylistChangedListener.java
 *
 * Created on 30.01.2011 by Daniel Becker
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

/**
 * @author Daniel Becker
 * @since 30.01.2011
 */
public interface PlaylistChangedListener
{
	public void activeElementChanged(PlayListEntry oldActiveElement, PlayListEntry newActiveElement);
	public void selectedElementChanged(PlayListEntry oldSelectedElement, PlayListEntry newSelectedElement);
}
