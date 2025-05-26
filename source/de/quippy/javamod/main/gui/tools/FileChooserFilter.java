/*
 * @(#) FileChooserFilter.java
 *
 * Created on 22.06.2006 by Daniel Becker
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

import java.io.File;
import java.util.ArrayList;

import javax.swing.filechooser.FileFilter;

import de.quippy.javamod.system.Helpers;

/**
 * @author Daniel Becker
 * @since 22.06.2006
 * Defines a file chooser filter for the <CODE>JFileChooser</CODE> component
 */
public class FileChooserFilter extends FileFilter
{
	private final ArrayList<String> extensions = new java.util.ArrayList<>();
	private String description;

	/**
	 * FileChooserFilter - Konstruktorkommentar.
	 */
	public FileChooserFilter()
	{
		super();
	}
	/**
	 * FileChooserFilter - Konstruktorkommentar.
	 * @param extension java.lang.String
	 * @param description java.lang.String
	 */
	public FileChooserFilter(final String extension, final String description)
	{
		this();
		extensions.add(extension.toLowerCase());
		this.description = (description == null ? extension + " files" : description);
	}
	/**
	 * FileChooserFilter - Konstruktorkommentar.
	 * @param extension java.lang.String
	 * @param description java.lang.String
	 */
	public FileChooserFilter(final String extension)
	{
		this(extension, null);
	}
	/**
	 * FileChooserFilter - Konstruktorkommentar.
	 * @param extension java.lang.String
	 * @param description java.lang.String
	 */
	public FileChooserFilter(final String[] extensions, final String description)
	{
		this();
		if (description != null)
			this.description = description;
		else
			this.description = Helpers.EMPTY_STING;

		for (final String extension : extensions)
		{
			final String suffix = extension.toLowerCase();
			if (description == null) this.description += "*." + suffix + " ";
			this.extensions.add(extension.toLowerCase());
		}
	}
	/**
	 * FileChooserFilter
	 * @param extension java.lang.String
	 * @param description java.lang.String
	 */
	public FileChooserFilter(final String[] extensions)
	{
		this(extensions, null);
	}
	/**
	 * Whether the given file is accepted by this filter.
	 * @param f java.io.File
	 * @return boolean
	 */
	@Override
	public boolean accept(final File f)
	{
		if (f.isDirectory()) return true;
		final int len = extensions.size();
		if (len == 0) return true;
		for (int i=0; i<len; i++)
		{
			final String suffix = extensions.get(i);
			if (suffix.equals("*") || f.getName().toLowerCase().endsWith('.' + suffix) || f.getName().toLowerCase().startsWith(suffix + '.')) return true; // Amiga Mods *start* with suffix, e.g. "mod.songname"
		}
		return false;
	}
	/**
	 * The description of this filter. For example: "JPG and GIF Images"
	 * @return java.lang.String
	 */
	@Override
	public String getDescription()
	{
		return description;
	}
}
