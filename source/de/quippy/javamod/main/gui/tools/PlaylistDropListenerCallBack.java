/*
 * @(#) PlaylistDropListenerCallBack.java
 *
 * Created on 08.03.2011 by Daniel Becker
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
package de.quippy.javamod.main.gui.tools;

import java.awt.dnd.DropTargetDropEvent;
import java.net.URL;

import de.quippy.javamod.main.playlist.PlayList;

/**
 * @author Daniel Becker
 *
 */
public interface PlaylistDropListenerCallBack
{
	public void playlistRecieved(DropTargetDropEvent dtde, PlayList dropResult, URL addToLastLoaded);
}
