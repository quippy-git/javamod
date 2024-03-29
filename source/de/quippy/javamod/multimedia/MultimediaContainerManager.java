/*
 * @(#)MultimediaContainerManager.java
 *
 * Created on 12.10.2007 by Daniel Becker
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
package de.quippy.javamod.multimedia;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;

import javax.sound.sampled.UnsupportedAudioFileException;

import de.quippy.javamod.system.Helpers;

/**
 * @author: Daniel Becker
 * @since: 12.10.2007
 */
public class MultimediaContainerManager
{
	private static HashMap<String, MultimediaContainer> fileExtensionMap;
	private static ArrayList<MultimediaContainer> containerArray;
	private static boolean headlessMode = true;
	
	/**
	 * @since: 12.10.2007
	 */
	private MultimediaContainerManager()
	{
		super();
	}
	/**
	 * To avoid instantiating any dialogs if on command line
	 * @since 15.01.2024
	 * @param isHeadless
	 */
	public static void setIsHeadlessMode(final boolean isHeadless)
	{
		headlessMode = isHeadless;
	}
	public static boolean isHeadlessMode()
	{
		return headlessMode;
	}
	public static HashMap<String, MultimediaContainer> getFileExtensionMap()
	{
		if (fileExtensionMap==null)
			fileExtensionMap = new HashMap<String, MultimediaContainer>();
		
		return fileExtensionMap;
	}
	public static ArrayList<MultimediaContainer> getContainerArray()
	{
		if (containerArray==null)
			containerArray = new ArrayList<MultimediaContainer>();
		return containerArray;
	}
	public static void getContainerConfigs(final Properties intoProps)
	{
		ArrayList<MultimediaContainer> listeners = getContainerArray();
		for (int i=0; i<listeners.size(); i++)
			listeners.get(i).configurationSave(intoProps);
	}
	public static void configureContainer(final Properties fromProps)
	{
		ArrayList<MultimediaContainer> listeners = getContainerArray();
		for (int i=0; i<listeners.size(); i++)
			listeners.get(i).configurationChanged(fromProps);
	}
	public static void registerContainer(final MultimediaContainer container)
	{
		if (container!=null)
		{
			getContainerArray().add(container);
			String [] extensions = container.getFileExtensionList();
			for (int i=0; i<extensions.length; i++)
				getFileExtensionMap().put(extensions[i], container);
		}
	}
	public static void deregisterContainer(final MultimediaContainer container)
	{
		if (container!=null)
		{
			getContainerArray().remove(container);
			String [] extensions = container.getFileExtensionList();
			for (int i=0; i<extensions.length; i++)
				getFileExtensionMap().remove(extensions[i]);
		}
	}
	public static void cleanUpAllContainers()
	{
		ArrayList<MultimediaContainer> containers = getContainerArray();
		for (int i=0; i<containers.size(); i++)
		{
			MultimediaContainer container = containers.get(i); 
			deregisterContainer(container);
			container.cleanUp();
		}
	}
	public static void updateLookAndFeel()
	{
		ArrayList<MultimediaContainer> listeners = getContainerArray();
		for (int i=0; i<listeners.size(); i++)
			listeners.get(i).updateLookAndFeel();
	}
	public static String[] getSupportedFileExtensions()
	{
		Set<String> keys = getFileExtensionMap().keySet();
		String[] result = new String[keys.size()];
		return keys.toArray(result);
	}
	public static HashMap<String, String[]>getSupportedFileExtensionsPerContainer()
	{
		ArrayList<MultimediaContainer> listeners = getContainerArray();
		HashMap<String, String[]> result = new HashMap<String, String []>(listeners.size());
		for (int i=0; i<listeners.size(); i++)
			result.put(listeners.get(i).getName(), listeners.get(i).getFileExtensionList());
		return result;
	}
	public static MultimediaContainer getMultimediaContainerForType(final String type) throws UnsupportedAudioFileException
	{
		MultimediaContainer container = getFileExtensionMap().get(type.toLowerCase());
		if (container==null) 
			throw new UnsupportedAudioFileException(type);
		else
			return container;
	}
	public static MultimediaContainer getMultimediaContainerSingleton(final URL url) throws UnsupportedAudioFileException
	{
		String fileName = url.getPath();

		// we default to mp3 with wrong extensions
		MultimediaContainer baseContainer = getFileExtensionMap().get(Helpers.getExtensionFrom(fileName));
		if (baseContainer==null) baseContainer = getFileExtensionMap().get(Helpers.getPreceedingExtensionFrom(fileName));
		if (baseContainer==null) // no extensions found?!
		{
			if (Helpers.isFile(url))
				throw new UnsupportedAudioFileException(fileName); // in Filemode we are ready now
			else
				baseContainer = getFileExtensionMap().get("mp3"); // otherwise we try a streaming protocol!
		}

		return baseContainer;
	}
	/**
	 * Will use getMultimediaContainerSingleton to retrieve the basic singleton
	 * and then create an instance by getInstance on that singleton
	 * This will also update the info panels, if getInstance is overridden.
	 * @since 15.01.2024
	 * @param url The URL of the file to load
	 * @param theParentWindow the parent window - if one exists - or null
	 * @return
	 * @throws UnsupportedAudioFileException
	 */
	public static MultimediaContainer getMultimediaContainer(final URL url) throws UnsupportedAudioFileException
	{
		MultimediaContainer baseContainer = getMultimediaContainerSingleton(url);
		MultimediaContainer container = baseContainer.getInstance(url);
		if (container==null) 
			throw new UnsupportedAudioFileException(url.getPath());
		else
			return container;
	}
	public static MultimediaContainer getMultimediaContainer(final URI uri) throws MalformedURLException, UnsupportedAudioFileException
	{
		return getMultimediaContainer(uri.toURL());
	}
	public static MultimediaContainer getMultimediaContainer(final File file) throws MalformedURLException, UnsupportedAudioFileException
	{
		return getMultimediaContainer(file.toURI());
	}
	public static MultimediaContainer getMultimediaContainer(final String fileName) throws MalformedURLException, UnsupportedAudioFileException
	{
		return getMultimediaContainer(new File(fileName));
	}
	public static void addMultimediaContainerEventListener(final MultimediaContainerEventListener listener)
	{
		ArrayList<MultimediaContainer> containers = getContainerArray();
		for (int i=0; i<containers.size(); i++)
			containers.get(i).addListener(listener);
	}
	public static void removeMultimediaContainerEventListener(final MultimediaContainerEventListener listener)
	{
		ArrayList<MultimediaContainer> containers = getContainerArray();
		for (int i=0; i<containers.size(); i++)
			containers.get(i).removeListener(listener);
	}
	public static String getSongNameFromURL(final URL url)
	{
		if (url==null) return Helpers.EMPTY_STING;

		final String result = Helpers.createStringFomURL(url);
		final int lastSlash = result.lastIndexOf('/');
		int dot = result.lastIndexOf('.');
		if (dot == -1 || dot<lastSlash) dot = result.length();
		return result.substring(lastSlash + 1, dot); 
	}
	public static String getSongNameFromFile(final File fileName)
	{
		if (fileName==null) return Helpers.EMPTY_STING;

		String result = fileName.getAbsolutePath();
		final int lastSlash = result.lastIndexOf(File.separatorChar);
		int dot = result.lastIndexOf('.');
		if (dot == -1 || dot<lastSlash) dot = result.length();
		return result.substring(lastSlash + 1, dot); 
	}
	/**
	 * This method will only do (!)localy(!) what is needed to pick up
	 * the song name String at [0] and time in milliseconds as Long at [1]
	 * @param url
	 * @return
	 * @since 12.02.2011
	 */
	public static Object [] getSongInfosFor(final URL url)
	{
		try
		{
			MultimediaContainer container = getMultimediaContainerSingleton(url);
			if (container!=null) return container.getSongInfosFor(url);
		}
		catch (UnsupportedAudioFileException ex)
		{
			//Log.error("IGNORED", ex);
		}
		return new Object[] { getSongNameFromURL(url) + " UNSUPPORTED FILE", Long.valueOf(-1) };
	}
}
