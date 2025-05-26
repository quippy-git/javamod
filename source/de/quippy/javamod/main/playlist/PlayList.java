/*
 * @(#) PlayList.java
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import de.quippy.javamod.main.gui.tools.FileChooserFilter;
import de.quippy.javamod.main.playlist.cuesheet.CueFile;
import de.quippy.javamod.main.playlist.cuesheet.CueIndex;
import de.quippy.javamod.main.playlist.cuesheet.CueSheet;
import de.quippy.javamod.main.playlist.cuesheet.CueTrack;
import de.quippy.javamod.multimedia.MultimediaContainerManager;
import de.quippy.javamod.system.Helpers;
import de.quippy.javamod.system.Log;

/**
 * @author Daniel Becker
 * @since 03.12.2006
 */
public class PlayList
{
	public static String [] SUPPORTEDPLAYLISTS = { "pls", "m3u", "m3u8", "cue", "zip" };
	public static FileChooserFilter PLAYLIST_FILE_FILTER = new FileChooserFilter(PlayList.SUPPORTEDPLAYLISTS, PlayList.getFileChooserDescription());

	public static String [] SUPPORTEDSAVELISTS = { "pls", "m3u", "m3u8" };
	public static FileChooserFilter PLAYLIST_SAVE_FILE_FILTER = new FileChooserFilter(PlayList.SUPPORTEDSAVELISTS, PlayList.getFileChooserDescription());

	private static String INDEX_STRING = "  index ";

	private URL loadedFromURL;
	private ArrayList<PlayListEntry> entries;
	private int current;
	private boolean repeat;

	private final ArrayList<PlaylistChangedListener> listeners = new ArrayList<>();

	/**
	 * Constructor for PlayList
	 */
	public PlayList(final boolean shuffle, final boolean repeat)
	{
		this.entries = new ArrayList<>();
		this.current = -1;
		this.repeat = repeat;
		if (shuffle) doShuffle();
	}
	/**
	 * @param entries
	 * @param shuffle
	 * @since 23.03.2011
	 */
	public PlayList(final ArrayList<PlayListEntry> entries, final boolean shuffle, final boolean repeat)
	{
		this.entries = entries;
		for (final PlayListEntry entry : entries)
			entry.setSavedInPlaylist(this);
		this.current = -1;
		this.repeat = repeat;
		if (shuffle) doShuffle();
	}
	/**
	 * Constructor for PlayList
	 */
	public PlayList(final File [] files, final boolean shuffle, final boolean repeat)
	{
		this(generateURLListFromFiles(files), shuffle, repeat);
	}
	/**
	 * Constructor for PlayList
	 */
	public PlayList(final String [] fileNames, final boolean shuffle, final boolean repeat)
	{
		this(generateURLListFromFileNames(fileNames), shuffle, repeat);
	}
	/**
	 * Constructor for PlayList
	 */
	public PlayList(final URL [] urls, final boolean shuffle, final boolean repeat)
	{
		this.entries = new ArrayList<>(urls.length);
		for (final URL url : urls)
		{
			// "Expand" playlist files
			if (PlayList.isPlaylistFile(url))
			{
				try
				{
					final PlayList newPlayList = PlayList.createFromFile(url, false, false);
					if (this.getLoadedFromURL()==null) this.setLoadedFromURL(url);
					final Iterator<PlayListEntry> elementIter = newPlayList.getIterator();
					while (elementIter.hasNext())
					{
						final PlayListEntry entry = elementIter.next();
						entry.setSavedInPlaylist(this);
						entries.add(entry);
					}
				}
				catch (final IOException ex)
				{
					Log.error("PlayList", ex);
				}
			}
			else
			{
				entries.add(new PlayListEntry(url, this));
			}
		}
		this.current = -1;
		this.repeat = repeat;
		if (shuffle) doShuffle();
	}
	public synchronized void addPlaylistChangedListener(final PlaylistChangedListener listener)
	{
		if (!listeners.contains(listener)) listeners.add(listener);
	}
	public synchronized void removePlaylistChangedListener(final PlaylistChangedListener listener)
	{
		listeners.remove(listener);
	}
	public synchronized void fireActiveElementChanged(final PlayListEntry oldElement, final PlayListEntry newEntry)
	{
		final int size = listeners.size();
		for (int i=0; i<size; i++)
		{
			listeners.get(i).activeElementChanged(oldElement, newEntry);
		}
	}
	public synchronized void fireSelectedElementChanged(final PlayListEntry oldElement, final PlayListEntry newEntry)
	{
		final int size = listeners.size();
		for (int i=0; i<size; i++)
		{
			listeners.get(i).selectedElementChanged(oldElement, newEntry);
		}
	}
	/**
	 * @return the repeat
	 * @since 22.11.2011
	 */
	public synchronized boolean isRepeat()
	{
		return repeat;
	}
	/**
	 * @param repeat the repeat to set
	 * @since 22.11.2011
	 */
	public synchronized void setRepeat(final boolean repeat)
	{
		this.repeat = repeat;
	}
	/**
	 * @since 08.11.2008
	 */
	public synchronized void doShuffle()
	{
		final Random rnd = new Random();
		final ArrayList<PlayListEntry> newEntries = new ArrayList<>(size());
		while (!entries.isEmpty())
		{
			newEntries.add(entries.remove(rnd.nextInt(entries.size())));
		}
		entries = newEntries;
		current = -1;
		for (int i=0; i<entries.size(); i++)
		{
			final PlayListEntry entry = entries.get(i);
			if (entry.isActive())
			{
				if (current==-1)
					current = i;
				else
					entry.setActive(false);
			}
		}
	}
	private synchronized ArrayList<PlayListEntry> getEntries()
	{
		return entries;
	}
	public synchronized PlayListEntry[] getAllEntries()
	{
		return entries.toArray(new PlayListEntry[entries.size()]);
	}
	/**
	 * Retrieve the current playlist entry
	 * if no (more) entries are set this will return null
	 * @since 03.12.2006
	 * @return
	 */
	public synchronized PlayListEntry getCurrentEntry()
	{
		if (current>=0 && current<size()) return entries.get(current);
		return null;
	}
	/**
	 * @param index
	 * @return
	 * @since 23.03.2011
	 */
	public synchronized PlayListEntry getEntry(final int index)
	{
		if (index>=0 && index<size()) return entries.get(index);
		return null;
	}
	/**
	 * @since 30.01.2011
	 */
	private synchronized void activateCurrentEntry()
	{
		final PlayListEntry current = getCurrentEntry();
		if (current!=null) current.setActive(true);
	}
	/**
	 * @since 30.01.2011
	 */
	private synchronized void deactivateCurrentEntry()
	{
		final PlayListEntry current = getCurrentEntry();
		if (current!=null) current.setActive(false);
	}
	/**
	 * @since 12.02.2011
	 * @param index
	 * @return
	 */
	public synchronized PlayListEntry setCurrentElement(final int index)
	{
		if (index>=0 && index<size() && index!=current)
		{
			final PlayListEntry oldEntry = getCurrentEntry();
			deactivateCurrentEntry();
			current = index;
			activateCurrentEntry();
			final PlayListEntry newEntry = getCurrentEntry();
			fireActiveElementChanged(oldEntry, newEntry);
			return newEntry;
		}
		return getCurrentEntry();
	}
	/**
	 * set the current Element by timeIndex - this is only done
	 * when the time index referes to a single file in the playlist
	 * This Method refers to the starttimeIndex and Duration of each entry
	 * The index is searched in the current entries file
	 * @param timeIndex
	 * @since 15.02.2012
	 */
	public synchronized void setCurrentElementByTimeIndex(final long timeIndex)
	{
		if (current==-1 || entries==null) return;
		int currentIndex = current;
		final int end = entries.size()-1;

		if (currentIndex > end) currentIndex = end;
		else
		if (currentIndex < 0) currentIndex = 0;

		URL file = null;
		do
		{
			final PlayListEntry currentEntry = entries.get(currentIndex);
			if (file!=null && !Helpers.isEqualURL(file, currentEntry.getFile())) return;
			file = currentEntry.getFile();
			final long startIndex = currentEntry.getTimeIndex();
			if (startIndex > timeIndex)
			{
				currentIndex--;
			}
			else
			{
				final long endIndex = startIndex + currentEntry.getDuration();
				if (endIndex < timeIndex)
				{
					currentIndex++;
				}
				else
					break;
			}
		}
		while (currentIndex>=0 && currentIndex<=end);
		if (currentIndex != current)
			setCurrentElement(currentIndex);
	}
	/**
	 * @since 03.04.2011
	 * @return
	 */
	public synchronized PlayListEntry [] getSelectedEntries()
	{
		if (entries.size()>0)
		{
			final ArrayList<PlayListEntry> selected = new ArrayList<>(entries.size());
			for (int i=0; i<entries.size(); i++)
			{
				final PlayListEntry entry = entries.get(i);
				if (entry.isSelected()) selected.add(entry);
			}
			if (selected.size()>0)
				return selected.toArray(new PlayListEntry[selected.size()]);
		}
		return null;
	}
	/**
	 * @since 03.04.2011
	 * @param index
	 */
	public synchronized void addSelectedElement(final int index)
	{
		final PlayListEntry entry = entries.get(index);
		entry.setSelected(true);
		fireSelectedElementChanged(null, entry);
	}
	/**
	 * @since 03.04.2011
	 * @param index
	 */
	public synchronized void toggleSelectedElement(final int index)
	{
		final PlayListEntry entry = entries.get(index);
		entry.setSelected(!entry.isSelected());
		fireSelectedElementChanged(null, entry);
	}
	/**
	 * @since 12.02.2011
	 * @param index (-1 means deselect any!)
	 * @return
	 */
	public synchronized void setSelectedElement(final int index)
	{
		setSelectedElements(index, index);
	}
	/**
	 * @since 03.04.2011
	 * @param fromIndex
	 * @param toIndex
	 */
	public synchronized void setSelectedElements(int fromIndex, int toIndex)
	{
		if (fromIndex>toIndex)
		{
			final int swap = fromIndex; fromIndex = toIndex; toIndex = swap;
		}
		for (int i=0; i<entries.size(); i++)
		{
			final PlayListEntry entry = entries.get(i);
			if (entry.isSelected() && (i<fromIndex || i>toIndex))
			{
				entry.setSelected(false);
				fireSelectedElementChanged(entry, null);
			}
			else
			if (!entry.isSelected() && (i>=fromIndex && i<=toIndex))
			{
				entry.setSelected(true);
				fireSelectedElementChanged(null, entry);
			}
		}
	}
	/**
	 * set index to next element or return false, if
	 * end is reached.
	 * The first call of "next" steps to the first element
	 * @since 03.12.2006
	 */
	public synchronized boolean next()
	{
		if (hasNext())
		{
			final PlayListEntry oldEntry = getCurrentEntry();
			deactivateCurrentEntry();
			if (current >= size()-1) current = 0; else current ++;
			activateCurrentEntry();
			fireActiveElementChanged(oldEntry, getCurrentEntry());
			return true;
		}
		else
			return false;
	}
	/**
	 * set index to prev element and wrap around
	 * @since 03.12.2006
	 */
	public synchronized boolean previous()
	{
		if (hasPrevious())
		{
			final PlayListEntry oldEntry = getCurrentEntry();
			deactivateCurrentEntry();
			current--;
			activateCurrentEntry();
			fireActiveElementChanged(oldEntry, getCurrentEntry());
			return true;
		}
		else
			return false;
	}
	/**
	 * @since 14.09.2008
	 * @return
	 */
	public synchronized boolean hasNext()
	{
		if ((current >= size()-1) && !repeat)
			return false;
		else
			return true;
	}
	/**
	 * @since 14.09.2008
	 * @return
	 */
	public synchronized boolean hasPrevious()
	{
		if (current <= 0)
			return false;
		else
			return true;
	}
	/**
	 * @return
	 * @since 08.03.2011
	 */
	public synchronized int size()
	{
		return entries.size();
	}
	/**
	 * @since 08.03.2011
	 * @param entry
	 * @return
	 */
	public synchronized int indexOf(final PlayListEntry entry)
	{
		return entries.indexOf(entry);
	}
	/**
	 * @since 08.03.2011
	 * @param indexAt
	 * @param newPlaylist
	 */
	public synchronized void addAllAt(int indexAt, final PlayList newPlaylist)
	{
		final ArrayList<PlayListEntry> newEntries = newPlaylist.getEntries();
		final int size = newEntries.size();
		for (int i=0; i<size; i++) newEntries.get(i).setSavedInPlaylist(this);
		if (indexAt>entries.size()) indexAt = entries.size();
		entries.addAll(indexAt, newEntries);
		if (current >= indexAt) current += size;
	}
	/**
	 * @since 08.03.2011
	 * @param newPlaylistEntry
	 */
	public synchronized void addEntry(final PlayListEntry newPlaylistEntry)
	{
		newPlaylistEntry.setSavedInPlaylist(this);
		entries.add(newPlaylistEntry);
	}
	/**
	 * @since 08.03.2011
	 * @param fromIndex
	 * @param toIndex
	 */
	public synchronized void move(final int fromIndex, final int toIndex)
	{
		final PlayListEntry mover = entries.remove(fromIndex);
		entries.add(toIndex, mover);

		if (current == fromIndex) current = toIndex;
		else
		if (current == toIndex) current++;
	}
	/**
	 * @since 08.03.2011
	 * @param fromIndex
	 */
	public synchronized void remove(final int fromIndex)
	{
		/*PlayListEntry mover = */entries.remove(fromIndex);
		if (current>fromIndex && current>-1) current--;
		else
		if (current==fromIndex) current=-1;
	}
	/**
	 * @return the loadedFromURL
	 */
	public URL getLoadedFromURL()
	{
		return loadedFromURL;
	}
	/**
	 * @param loadedFromURL the loadedFromURL to set
	 */
	private void setLoadedFromURL(final URL loadedFromURL)
	{
		this.loadedFromURL = loadedFromURL;
	}
	/**
	 * Saves the current playlist to a File
	 * @since 03.12.2006
	 * @param f
	 * @throws IOException
	 */
	public synchronized void savePlayListTo(File f) throws IOException
	{
		PrintWriter ps = null;

		try
		{
			String playlistPath = f.getAbsolutePath();
			// need this only for comparison of file suffix
			final String lowerCasePlaylistPath = playlistPath.toLowerCase();
			final boolean writePLSFile = lowerCasePlaylistPath.endsWith(".pls");
			final boolean writeCueSheet = lowerCasePlaylistPath.endsWith(".cue");
			final boolean writeM3U8 = lowerCasePlaylistPath.endsWith(".m3u8");
			final boolean writeM3U = lowerCasePlaylistPath.endsWith(".m3u");
			// if none of the above, write m3u (and add extension as it is missing)
			if (!writePLSFile && !writeCueSheet && !writeM3U8 && !writeM3U)
				f = new File(playlistPath+=".m3u");

			// Overwrite by default - for permission was asked in advance!
			if (f.exists())
			{
				boolean ok = f.delete();
				if (ok) ok = f.createNewFile();
				if (!ok) throw new IOException("Could not overwrite file " + playlistPath);
			}

			final String pathPrefix = playlistPath.substring(0, playlistPath.lastIndexOf(File.separatorChar)+1);

			CueSheet cueSheet = null;
			CueFile currentCueFile = null;
			CueTrack currentCueTrack = null;
			int cueTrackIndex = 0;
			int cueTrackNo = 0;

			if (!writeCueSheet)
			{
				ps = new PrintWriter(new OutputStreamWriter(new FileOutputStream(f), (writeM3U8)?Helpers.CODING_UTF8:Helpers.CODING_M3U));
				ps.println((writePLSFile)?"[playlist]":"#EXTM3U");
			}
			else
			{
				cueSheet = new CueSheet();
				cueSheet.setCueSheedFileName(Helpers.createURLfromFile(f));
			}

			for (int i=0; i<size(); i++)
			{
				final PlayListEntry entry = entries.get(i);
				final URL fileURL = entry.getFile();
				if (fileURL!=null)
				{
					if (writeCueSheet && cueSheet!=null)
					{
						if (currentCueFile==null || !Helpers.isEqualURL(currentCueFile.getFile(), fileURL))
						{
							cueSheet.addFile(currentCueFile = new CueFile());
							currentCueFile.setFile(fileURL);
							final String ext = Helpers.getExtensionFromURL(fileURL).toLowerCase();
							if (ext.equals("mp3"))
								currentCueFile.setType("MP3");
							else
								currentCueFile.setType("WAVE");
						}
						if (!entry.getFormattedName().startsWith(INDEX_STRING) || currentCueTrack==null)
						{
							currentCueFile.addTrack(currentCueTrack = new CueTrack());
							currentCueTrack.setFormat("AUDIO");
							currentCueTrack.setTrackNo(++cueTrackNo);
							currentCueTrack.setTitle(entry.getFormattedName());
							cueTrackIndex = 0;
						}
						final CueIndex cueIndex = new CueIndex();
						cueIndex.setIndexNo(++cueTrackIndex);
						cueIndex.setMillisecondIndex(entry.getTimeIndex());
						currentCueTrack.addIndex(cueIndex);
					}
					else
					if (ps!=null)
					{
						String fileString = Helpers.createLocalFileStringFromURL(fileURL, true);
						if (Helpers.isFile(fileURL)) // try to make relative to playlist path if its a file location
							fileString = Helpers.createRelativePathForFile(pathPrefix, fileString);
						final String duration = Long.toString(Helpers.getMillisecondsFromTimeString(entry.getDurationString()) / 1000L);
						if (writePLSFile)
						{
							final String index = Integer.toString(i+1) + '=';
							ps.println("File" + index + fileString);
							ps.println("Title" + index + entry.getFormattedName());
							ps.println("Length" + index + duration);
						}
						else
						{
							ps.println("#EXTINF:" + duration + ',' + entry.getFormattedName());
							ps.println(fileString);
						}
					}
				}
			}
			if (writePLSFile && ps!=null)
			{
				ps.println("NumberOfEntries=" + size());
				ps.println("Version=2");
			}
			else
			if (writeCueSheet && cueSheet!=null)
			{
				cueSheet.writeCueSheet(f);
			}
		}
		finally
		{
			if (ps!=null) ps.close();
		}
	}
	/**
	 * Saves the current playlist to a File
	 * @since 03.12.2006
	 * @param fileName
	 * @throws IOException
	 */
	public synchronized void savePlayListTo(final String fileName) throws IOException
	{
		savePlayListTo(new File(fileName));
	}
	/**
	 * @param fileNames
	 * @return
	 * @since 23.03.2011
	 */
	private static URL[] generateURLListFromFileNames(final String [] fileNames)
	{
		final ArrayList<File> files = new ArrayList<>(fileNames.length);
		for (final String fileName : fileNames)
		{
			files.add(new File(fileName));
		}
		return generateURLListFromFiles(files.toArray(new File[files.size()]));
	}
	/**
	 * @param files
	 * @return
	 * @since 23.03.2011
	 */
	private static URL[] generateURLListFromFiles(final File [] files)
	{
		final ArrayList<URL> urls = new ArrayList<>(files.length);
		for (final File file : files)
		{
			final URL url = Helpers.createURLfromFile(file);
			if (url!=null) urls.add(url);
		}
		return urls.toArray(new URL[urls.size()]);
	}
	private static PlayList readPlainFile(String line, final URL playListURL, final BufferedReader br, final boolean shuffle, final boolean repeat) throws IOException
	{
		final ArrayList<PlayListEntry> entries = new ArrayList<>();
		do
		{
			line = line.trim();
			if (!line.isEmpty())
			{
				final PlayListEntry entry = new PlayListEntry(line, null);
				entries.add(entry);
			}
		}
		while ((line=br.readLine())!=null);
		if (entries.size()>0)
		{
			final PlayList playList = new PlayList(entries, shuffle, repeat);
			playList.setLoadedFromURL(playListURL);
			return playList;
		}
		else
			return null;
	}
	/**
	 * @since 14.09.2008
	 * @param playListURL
	 * @param br
	 * @return
	 * @throws IOException
	 */
	private static PlayList readPLSFile(final URL playListURL, final BufferedReader br, final boolean shuffle, final boolean repeat) throws IOException
	{
		String line;
		final ArrayList<String> songName = new ArrayList<>();
		final ArrayList<String> duration = new ArrayList<>();
		final ArrayList<URL> file = new ArrayList<>();
		int highestIndex = -1;
		while ((line=br.readLine())!=null)
		{
			line = line.trim();
			if (!line.isEmpty())
			{
				final String compare = line.toLowerCase();
				final int equalOp = line.indexOf('=');
				final String value = line.substring(equalOp+1);
//				if (compare.startsWith("numberofentries"))
//				{
//					numOfEntries = Integer.parseInt(value);
//				}
//				else
				if (compare.startsWith("file"))
				{
					final int index = Integer.parseInt(line.substring(4, equalOp)) - 1;
					if (index>highestIndex) highestIndex = index;
					file.add(index, Helpers.createAbsolutePathForFile(playListURL, value));
				}
				else
				if (compare.startsWith("title"))
				{
					final int index = Integer.parseInt(line.substring(5, equalOp)) - 1;
					if (index>highestIndex) highestIndex = index;
					songName.add(index, value);
				}
				else if (compare.startsWith("length") && !value.equals("-1"))
				{
					final int index = Integer.parseInt(line.substring(6, equalOp)) - 1;
					if (index>highestIndex) highestIndex = index;
					duration.add(index, value);
				}
			}
		}

		final ArrayList<PlayListEntry> entries = new ArrayList<>();
		for (int i=0; i<=highestIndex; i++)
		{
			if (i<file.size())
			{
				final PlayListEntry entry = new PlayListEntry(file.get(i), null);
				if (i<songName.size())
				{
					final String name = songName.get(i);
					if (name!=null && !name.isEmpty()) entry.setSongName(name);
				}
				if (i<duration.size())
				{
					final String dura = duration.get(i);
					if (dura!=null && !dura.isEmpty())
					{
						final int seconds = Integer.parseInt(dura);
						entry.setDuration(seconds*1000L);
					}
				}
				entries.add(entry);
			}
		}

		if (entries.size()>0)
		{
			final PlayList playList = new PlayList(entries, shuffle, repeat);
			playList.setLoadedFromURL(playListURL);
			return playList;
		}
		else
			return null;
	}
	/**
	 * @since 14.09.2008
	 * @param playListURL
	 * @param br
	 * @return
	 * @throws IOException
	 */
	private static PlayList readM3UFile(final URL playListURL, final BufferedReader br, final boolean shuffle, final boolean repeat) throws IOException
	{
		final ArrayList<PlayListEntry> entries = new ArrayList<>();
		String line;
		String songName = null;
		String duration = null;
		while ((line=br.readLine())!=null)
		{
			line = line.trim();
			if (!line.isEmpty())
			{
				final String compare = line.toLowerCase();
				if (compare.startsWith("#extm3u")) continue; // should be consumed!
				if (compare.startsWith("#extinf:"))
				{
					final int comma = line.indexOf(',');
					if (comma>-1)
					{
						duration = line.substring(8, comma);
						songName = line.substring(comma+1);
					}
				}
				else
				{
					PlayListEntry entry;
					URL normalizedEntry = Helpers.createAbsolutePathForFile(playListURL, line);
					if (normalizedEntry==null) normalizedEntry = Helpers.createURLfromString(line);
					entries.add(entry = new PlayListEntry(normalizedEntry, null));

					if (songName!=null && !songName.isEmpty())
						entry.setSongName(songName);
					if (duration!=null && !duration.isEmpty())
					{
						final int seconds = Integer.parseInt(duration);
						entry.setDuration(seconds*1000L);
					}
					songName = duration = null;
				}
			}
		}

		if (entries.size()>0)
		{
			final PlayList playList = new PlayList(entries, shuffle, repeat);
			playList.setLoadedFromURL(playListURL);
			return playList;
		}
		else
			return null;
	}
	/**
	 * @since 02.01.2011
	 * @param playListURL
	 * @param shuffle
	 * @return
	 * @throws IOException
	 */
	private static PlayList readZIPFile(final URL playListURL, final boolean shuffle, final boolean repeat) throws IOException
	{
		final ArrayList<File> entries = new ArrayList<>();
		ZipInputStream input = null;
		try
		{
			final File zipFile = new File(playListURL.toURI());
			input = new ZipInputStream(playListURL.openStream());
			ZipEntry entry;
			while ((entry = input.getNextEntry())!=null)
			{
				if (entry.isDirectory()) continue;
				entries.add(new File(zipFile.getCanonicalPath() + File.separatorChar + entry.getName()));
			}
		}
		catch (final Throwable ex)
		{
		}
		finally
		{
			if (input!=null) try { input.close(); } catch (final IOException ex) { /* Log.error("IGNORED", ex); */ }
		}
		if (entries.size()>0)
			return new PlayList(entries.toArray(new File[entries.size()]), shuffle, repeat);
		else
			return null;
	}
	/**
	 * @since 04.03.2012
	 * @param playListURL
	 * @param shuffle
	 * @param repeat
	 * @return
	 * @throws IOException
	 */
	private static PlayList readCUEFile(final URL playListURL, final boolean shuffle, final boolean repeat) throws IOException
	{
		final CueSheet cueSheet = CueSheet.createCueSheet(playListURL);
		// Now iterate throw the entries and create a playlist
		int list_index = 0;
		final ArrayList<CueFile> cueFiles = cueSheet.getCueFiles();
		final int filesSize = cueFiles.size();
		if (filesSize>0)
		{
			final ArrayList<PlayListEntry> entries = new ArrayList<>();
			for (int i=0; i<filesSize; i++)
			{
				final CueFile cueFile = cueFiles.get(i);
				final Object [] infos = MultimediaContainerManager.getSongInfosFor(cueFile.getFile());
				long fullDuration = (infos[1]!=null)?((Long)infos[1]).longValue():-1;

				final ArrayList<CueTrack> cueTracks = cueFile.getTracks();
				final int tracksSize = cueTracks.size();
				for (int j=0; j<tracksSize; j++)
				{
					final CueTrack cueTrack = cueTracks.get(j);
					final ArrayList<CueIndex> indexes = cueTrack.getIndexes();
					final int indexSize = indexes.size();
					for (int k=0; k<indexSize; k++)
					{
						final CueIndex index = indexes.get(k);
						// Skip all index0 values (silence)
						final int indexNo = index.getIndexNo();
						if (indexNo>0)
						{
							final long millisecondIndex = index.getMillisecondIndex();
							// Correct Time of previous Index of this track if we are not at track 0 anymore
							if (j > 0)
							{
								final PlayListEntry previousEntry = entries.get(list_index - 1);
								final long duration = millisecondIndex - previousEntry.getTimeIndex();
								previousEntry.setDuration(duration);
								fullDuration -= duration;
							}

							final PlayListEntry entry = new PlayListEntry(cueFile.getFile(), null);
							if (indexNo > 1)
							{
								entry.setSongName(INDEX_STRING + indexNo);
							}
							else
							{
								final StringBuilder songName = new StringBuilder();
								if (cueSheet.getTitle()!=null) songName.append(cueSheet.getTitle());
								if (cueTrack.getTitle()!=null)
								{
									if (!songName.isEmpty()) songName.append(" - ");
									songName.append(cueTrack.getTitle());
								}
								entry.setSongName(songName.toString());
							}
							entry.setTimeIndexInFile(millisecondIndex);
							entry.setDuration(fullDuration);
							entries.add(list_index++, entry);
						}
					}
				}
			}
			return new PlayList(entries, shuffle, repeat);
		}
		else
			return null;
	}
	/**
	 * will create a playlists with the given files
	 * if a file represents a playlist, it will get expanded
	 * @param url
	 * @param shuffle
	 * @return
	 * @since 28.04.2011
	 */
	public static PlayList createNewListWithFiles(final URL[] url, final boolean shuffle, final boolean repeat)
	{
		return new PlayList(url, shuffle, repeat);
	}
	/**
	 * will create a playlists with the given file
	 * if the file represents a playlist, it will get expanded
	 * @param url
	 * @param shuffle
	 * @return
	 * @since 28.04.2011
	 */
	public static PlayList createNewListWithFile(final URL url, final boolean shuffle, final boolean repeat)
	{
		return createNewListWithFiles(new URL[] { url }, shuffle, repeat);
	}
	/**
	 * will create a playlists with the given files
	 * if a file represents a playlist, it will get expanded
	 * @param file
	 * @param shuffle
	 * @return
	 * @since 28.04.2011
	 */
	public static PlayList createNewListWithFiles(final File[] file, final boolean shuffle, final boolean repeat)
	{
		return new PlayList(file, shuffle, repeat);
	}
	/**
	 * will create a playlists with the given file
	 * if the file represents a playlist, it will get expanded
	 * @param file
	 * @param shuffle
	 * @return
	 * @since 28.04.2011
	 */
	public static PlayList createNewListWithFile(final File file, final boolean shuffle, final boolean repeat)
	{
		return createNewListWithFiles(new File[] { file }, shuffle, repeat);
	}
	/**
	 * reads a playlist from a file
	 * @since 03.12.2006
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public static PlayList createFromFile(final URL url, final boolean shuffle, final boolean repeat) throws IOException
	{
		PlayList result = null;

		final String fileName = url.getPath().toLowerCase();
		if (fileName.endsWith(".zip"))
		{
			result = readZIPFile(url, shuffle, repeat);
		}
		else
		if (fileName.endsWith(".cue"))
		{
			result = readCUEFile(url, shuffle, repeat);
		}
		else
		if (isPlaylistFile(url))
		{
			final boolean readM3U8 = (fileName.endsWith("m3u8"));
			BufferedReader br = null;
			try
			{
				br = new BufferedReader(new InputStreamReader(url.openStream(), (readM3U8)?Helpers.CODING_UTF8:Helpers.CODING_M3U));
				String line = Helpers.EMPTY_STING;
				while (line!=null && line.isEmpty()) line = br.readLine().trim();
				if (line!=null)
				{
					line = line.toLowerCase();
					if (line.equalsIgnoreCase("[playlist]"))
						result = readPLSFile(url, br, shuffle, repeat);
					else
					if (line.equalsIgnoreCase("#extm3u"))
						result = readM3UFile(url, br, shuffle, repeat);
					else
						result = readPlainFile(line, url, br, shuffle, repeat);
				}
			}
			finally
			{
				if (br!=null) try { br.close(); } catch (final IOException ex) { /* Log.error("IGNORED", ex); */ }
			}
		}
		else
		{
			result = createNewListWithFile(url, shuffle, repeat);
		}
		return result;
	}
	/**
	 * reads a Playlist from a file
	 * @since 03.12.2006
	 * @param f
	 * @param shuffle
	 * @return
	 * @throws IOException
	 */
	public static PlayList createFromFile(final File f, final boolean shuffle, final boolean repeat) throws IOException
	{
		return createFromFile(Helpers.createURLfromFile(f), shuffle, repeat);
	}
	/**
	 * reads a Playlist from a file
	 * @since 03.12.2006
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public static PlayList createFromFile(final String fileName, final boolean shuffle, final boolean repeat) throws IOException
	{
		return PlayList.createFromFile(new File(fileName), shuffle, repeat);
	}
	/**
	 * URL points to a Playlist File
	 * @since 02.01.2011
	 * @param fileURL
	 * @return
	 */
	public static boolean isPlaylistFile(final URL fileURL)
	{
		return isPlaylistFile(fileURL.getPath().toLowerCase());
	}
	/**
	 * filename points to a Playlist File
	 * @since 02.01.2011
	 * @param fileName
	 * @return
	 */
	public static boolean isPlaylistFile(final String fileName)
	{
		for (final String element : SUPPORTEDPLAYLISTS)
		{
			if (fileName.endsWith('.'+element)) return true;
		}
		return false;
	}
	/**
	 * @since 02.01.2011
	 * @return
	 */
	public static String getFileChooserDescription()
	{
		final StringBuilder sb = new StringBuilder("Playlist (");
		for (int i=0; i<SUPPORTEDPLAYLISTS.length; i++)
		{
			sb.append("*.").append(SUPPORTEDPLAYLISTS[i]);
			if (i<SUPPORTEDPLAYLISTS.length-1) sb.append(", ");
		}
		sb.append(')');
		return sb.toString();
	}
	/**
	 * @since 03.04.2011
	 * @return
	 */
	public static String getFileChooserSaveDescription()
	{
		final StringBuilder sb = new StringBuilder("Playlist (");
		for (int i=0; i<SUPPORTEDSAVELISTS.length; i++)
		{
			sb.append("*.").append(SUPPORTEDSAVELISTS[i]);
			if (i<SUPPORTEDSAVELISTS.length-1) sb.append(", ");
		}
		sb.append(')');
		return sb.toString();
	}
	/**
	 * @since 02.01.2011
	 * @return
	 */
	public synchronized Iterator<PlayListEntry> getIterator()
	{
		return entries.iterator();
	}
	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		final StringBuilder result = new StringBuilder();
		final int lastIndex = size()-1;
		for (int i=0; i<=lastIndex; i++)
		{
			result.append('[').append(entries.get(i)).append(']');
			if (i<lastIndex) result.append(',');
		}
		return result.toString();
	}
}
