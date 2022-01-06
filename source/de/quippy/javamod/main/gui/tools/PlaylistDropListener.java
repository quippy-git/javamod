/*
 * @(#) PlaylistDropListener.java
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

import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import de.quippy.javamod.main.playlist.PlayList;
import de.quippy.javamod.multimedia.MultimediaContainerManager;
import de.quippy.javamod.system.Helpers;
import de.quippy.javamod.system.Log;

/**
 * @author Daniel Becker
 *
 */
public class PlaylistDropListener extends DropTargetAdapter
{
	private PlaylistDropListenerCallBack callBack;

	/**
	 * 
	 * @since 08.03.2011
	 */
	public PlaylistDropListener(PlaylistDropListenerCallBack callBack)
	{
		this.callBack = callBack;
	}

	private void fillWithPlayableFiles(ArrayList<URL> urls, File startDir)
	{
		String [] files = startDir.list(new FilenameFilter()
		{
			public boolean accept(File dir, String name)
			{
				File fullFileName = new File(dir.getAbsolutePath() + File.separatorChar + name);
				if (fullFileName.isDirectory()) return true;
				try
				{
					return MultimediaContainerManager.getMultimediaContainerSingleton(fullFileName.toURI().toURL()) != null;
				}
				catch (Exception ex)
				{
					//NOOP;
				}
				return false;
			}
		});
		for (int i=0; i<files.length; i++)
		{
			File fullFileName = new File(startDir.getAbsolutePath() + File.separatorChar + files[i]);
			if (fullFileName.isDirectory())
				fillWithPlayableFiles(urls, fullFileName);
			else
			{
				try
				{
					urls.add(fullFileName.toURI().toURL());
				}
				catch (Exception ex)
				{
					//NOOP;
				}
			}
		}
	}
	/**
	 * @param dtde
	 * @see java.awt.dnd.DropTargetListener#drop(java.awt.dnd.DropTargetDropEvent)
	 * @since 08.03.2011
	 */
	public void drop(DropTargetDropEvent dtde)
	{
		try
		{
			URL addToLastLoaded = null;
			List<?> files = Helpers.getDropData(dtde);
			if (files!=null)
			{
				final ArrayList<URL> urls = new ArrayList<URL>(files.size());

				for (int i=0; i<files.size(); i++)
				{
					final String fileName = files.get(i).toString(); // can be files, an be strings...
					File f = new File(fileName);
					if (f.isDirectory())
					{
						fillWithPlayableFiles(urls, f);
					}
					else
					{
						final URL url = f.toURI().toURL();
						if (files.size()==1) addToLastLoaded = url;
						urls.add(url);
					}
				}
    			PlayList playList = PlayList.createNewListWithFiles(urls.toArray(new URL[urls.size()]), false, false);
				callBack.playlistRecieved(dtde, playList, addToLastLoaded);
            }
		}
		catch (Exception ex)
		{
			Log.error("[MainForm::DropListener]", ex);
		}
		finally
		{
			dtde.dropComplete(true);
		}
	}

}
