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
	private final PlaylistDropListenerCallBack callBack;

	/**
	 *
	 * @since 08.03.2011
	 */
	public PlaylistDropListener(final PlaylistDropListenerCallBack callBack)
	{
		this.callBack = callBack;
	}

	private void fillWithPlayableFiles(final ArrayList<URL> urls, final File startDir)
	{
		final String [] files = startDir.list(new FilenameFilter()
		{
			@Override
			public boolean accept(final File dir, final String name)
			{
				final File fullFileName = new File(dir.getAbsolutePath() + File.separatorChar + name);
				if (fullFileName.isDirectory()) return true;
				try
				{
					return MultimediaContainerManager.getMultimediaContainerSingleton(fullFileName.toURI().toURL()) != null;
				}
				catch (final Exception ex)
				{
					//NOOP;
				}
				return false;
			}
		});
		for (final String file : files)
		{
			final File fullFileName = new File(startDir.getAbsolutePath() + File.separatorChar + file);
			if (fullFileName.isDirectory())
				fillWithPlayableFiles(urls, fullFileName);
			else
			{
				try
				{
					urls.add(fullFileName.toURI().toURL());
				}
				catch (final Exception ex)
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
	@Override
	public void drop(final DropTargetDropEvent dtde)
	{
		try
		{
			URL addToLastLoaded = null;
			final List<?> files = Helpers.getDropData(dtde);
			if (files!=null)
			{
				final ArrayList<URL> urls = new ArrayList<>(files.size());

				for (final Object file : files)
				{
					final String fileName = file.toString(); // can be files, can be strings...
					final File f = new File(fileName);
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
    			final PlayList playList = PlayList.createNewListWithFiles(urls.toArray(new URL[urls.size()]), false, false);
				callBack.playlistRecieved(dtde, playList, addToLastLoaded);
            }
		}
		catch (final Exception ex)
		{
			Log.error("[MainForm::DropListener]", ex);
		}
		finally
		{
			dtde.dropComplete(true);
		}
	}

}
