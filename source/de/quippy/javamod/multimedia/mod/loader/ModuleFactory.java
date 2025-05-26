/*
 * @(#) ModuleFactory.java
 *
 * Created on 21.04.2006 by Daniel Becker
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
package de.quippy.javamod.multimedia.mod.loader;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.quippy.javamod.io.ModfileInputStream;

/**
 * Returns the appropiate ModuleClass for the desired ModFile
 * @author Daniel Becker
 * @since 21.04.2006
 */
public class ModuleFactory
{
	private static Map<String, Module> fileExtensionMap;
	private static ArrayList<Module> modulesArray;
	/**
	 * Constructor for ModuleFactory - This Class Is A Singleton
	 */
	private ModuleFactory()
	{
		super();
	}

	/**
	 * Lazy instantiation access method
	 * @since 04.01.2010
	 * @return
	 */
	private static Map<String, Module> getFileExtensionMap()
	{
		if (fileExtensionMap==null)
			fileExtensionMap= new HashMap<>();

		return fileExtensionMap;
	}
	/**
	 * Lazy instantiation access method
	 * @since 04.01.2010
	 * @return
	 */
	private static ArrayList<Module> getModulesArray()
	{
		if (modulesArray==null)
			modulesArray = new ArrayList<>();
		return modulesArray;
	}
	public static void registerModule(final Module mod)
	{
		getModulesArray().add(mod);
		final String [] extensions = mod.getFileExtensionList();
		for (final String extension : extensions)
			getFileExtensionMap().put(extension, mod);
	}
	public static void deregisterModule(final Module mod)
	{
		getModulesArray().remove(mod);
		final String [] extensions = mod.getFileExtensionList();
		for (final String extension : extensions)
			getFileExtensionMap().remove(extension);
	}
	public static String [] getSupportedFileExtensions()
	{
		final Set<String> keys = getFileExtensionMap().keySet();
		final String[] result = new String[keys.size()];
		return keys.toArray(result);
	}
	public static Module getModuleFromExtension(final String extension)
	{
		return getFileExtensionMap().get(extension.toLowerCase());
	}
	/**
	 * Finds the appropriate loader through the IDs
	 * @since 04.01.2010
	 * @param input
	 * @return
	 */
	private static Module getModuleFromStreamByID(final ModfileInputStream input)
	{
		for (final Module mod : getModulesArray())
		{
			try
			{
				if (mod.checkLoadingPossible(input)) return mod;
			}
			catch (final IOException ex)
			{
				/* Ignoring */
			}
		}
		return null;
	}
	/**
	 * Finds the appropriate loader through simply loading it!
	 * @since 13.06.2010
	 * @param input
	 * @return
	 */
	private static Module getModuleFromStream(final ModfileInputStream input)
	{
		for (final Module mod : getModulesArray())
		{
			try
			{
				final Module result = mod.loadModFile(input);
				input.seek(0);
				return result; // <-- here this loading was a success!
			}
			catch (final Throwable ex)
			{
				/* Ignoring */
			}
		}
		return null;
	}
	/**
	 * Uses the File-Extension to find a suitable loader.
	 * @param fileName The Filename of the mod
	 * @return null, if fails
	 */
	public static Module getInstance(final String fileName) throws IOException
	{
		return getInstance(new File(fileName));
	}
	/**
	 * Uses the File-Extension to find a suitable loader.
	 * @param file The File-Instance of the modfile
	 * @return null, if fails
	 */
	public static Module getInstance(final File file) throws IOException
	{
		return getInstance(file.toURI().toURL());
	}
	/**
	 * Uses the File-Extension to find a suitable loader.
	 * @param url URL-Instance of the path to the modfile
	 * @return null, if fails
	 */
	public static Module getInstance(final URL url) throws IOException
	{
		ModfileInputStream inputStream = null;
		try
		{
			inputStream = new ModfileInputStream(url);
			Module mod = getModuleFromStreamByID(inputStream);
			// If the header gives no infos, it's obviously a Noise Tracker file
			// So let's try all loaders
			if (mod!=null)
				return mod.loadModFile(inputStream);
			else
			{
				mod = getModuleFromStream(inputStream);
				if (mod!=null)
					return mod;
				else
					throw new IOException("Unsupported MOD-Type: " + inputStream.getFileName());
			}
		}
		catch (final Throwable ex)
		{
			throw new IOException("[ModuleFactory] Failed with loading of " + url.toString(), ex);
		}
		finally
		{
			if (inputStream!=null) try { inputStream.close(); } catch (final IOException ex) { /* Log.error("IGNORED", ex); */ }
		}
	}
}
