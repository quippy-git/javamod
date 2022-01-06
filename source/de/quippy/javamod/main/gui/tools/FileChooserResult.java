/*
 * @(#) FileChooserResult.java
 *
 * Created on 05.01.2008 by Daniel Becker
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

import javax.swing.filechooser.FileFilter;

/**
 * Returns the filechooser selection
 * We need the selected FileFilter to select
 * the appropriate wave file encoding
 * @author Daniel Becker
 * @since 05.01.2008
 */
public class FileChooserResult
{
	public FileFilter selectedFilter;
	public File[] selectedFiles; 
	/**
	 * Constructor for FileChooserResult
	 */
	public FileChooserResult(FileFilter selectedFilter, File[] selectedFiles)
	{
		super();
		this.selectedFilter = selectedFilter;
		this.selectedFiles = selectedFiles;
	}
	/**
	 * @return the selectedFilter
	 */
	public FileFilter getSelectedFilter()
	{
		return selectedFilter;
	}
	/**
	 * @param selectedFilter the selectedFilter to set
	 */
	public void setSelectedFilter(FileFilter selectedFilter)
	{
		this.selectedFilter = selectedFilter;
	}
	/**
	 * @return the selectedFile
	 */
	public File [] getSelectedFiles()
	{
		return selectedFiles;
	}
	/**
	 * @return the first selectedFile
	 */
	public File getSelectedFile()
	{
		if (selectedFiles!=null && selectedFiles.length>0)
			return selectedFiles[0];
		else
			return null;
	}
	/**
	 * @param selectedFile the selectedFile to set
	 */
	public void setSelectedFile(File[] selectedFiles)
	{
		this.selectedFiles = selectedFiles;
	}
}
