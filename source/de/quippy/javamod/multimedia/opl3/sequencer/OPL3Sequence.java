/*
 * @(#) OPL3Sequence.java
 *
 * Created on 03.08.2020 by Daniel Becker
 * 
 *-----------------------------------------------------------------------
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
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
package de.quippy.javamod.multimedia.opl3.sequencer;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import de.quippy.javamod.io.RandomAccessInputStreamImpl;
import de.quippy.javamod.multimedia.opl3.emu.EmuOPL;
import de.quippy.javamod.multimedia.opl3.emu.EmuOPL.oplType;
import de.quippy.javamod.system.Helpers;
import de.quippy.javamod.system.Log;

/**
 * @author Daniel Becker
 * @since 03.08.2020
 */
public abstract class OPL3Sequence
{
	/**
	 * Constructor for OPL3Sequence
	 */
	public OPL3Sequence()
	{
		super();
	}
	/**
	 * @param fileName
	 * @return
	 */
	public static OPL3Sequence createOPL3Sequence(final String fileName, final String bnkFileName) throws IOException
	{
		return createOPL3Sequence(new File(fileName), new File(bnkFileName));
	}
	/**
	 * @param file
	 * @return
	 */
	public static OPL3Sequence createOPL3Sequence(final File file, final File bnkFile) throws IOException
	{
		if (file==null || bnkFile==null) return null;
		return createOPL3Sequence(file.toURI().toURL(), bnkFile.toURI().toURL());
	}
	/**
	 * @param url
	 * @return
	 */
	public static OPL3Sequence createOPL3Sequence(final URL url, final URL bnkURL) throws IOException
	{
		OPL3Sequence newSequence = getOPL3SequenceInstanceFor(url);
		if (newSequence!=null)
		{
			if (newSequence instanceof ROLSequence)
			{
				if (bnkURL==null) throw new IOException("No bank file specified!");
				((ROLSequence)newSequence).setBNKFile(bnkURL);
			}
			RandomAccessInputStreamImpl inputStream = null;
			try
			{
				inputStream = new RandomAccessInputStreamImpl(url);
				newSequence.setURL(url);
				newSequence.readOPL3Sequence(inputStream);
			}
			finally
			{
				if (inputStream!=null) try { inputStream.close(); } catch (Exception ex) { Log.error("IGNORED", ex); }
			}
		}
		else
			throw new IOException("Unsupported OPL3 Sequence");
		return newSequence;
	}
	
	/**
	 * This central method will know of all available sequence-types - hardcoded
	 * We do not use a factory like we did with the mods. Highly flexible,
	 * but sometimes a pain in the ass...
	 * @since 03.08.2020
	 * @param url
	 * @return
	 */
	private static OPL3Sequence getOPL3SequenceInstanceFor(URL url)
	{
		String extension = Helpers.getExtensionFromURL(url).toUpperCase();
		if (extension.equals("DRO"))
			return new DROSequence();
		else
		if (extension.equals("LAA") || extension.equals("CMF") || extension.equals("SCI"))
			return new MIDSequence();
		else
		if (extension.equals("ROL"))
			return new ROLSequence();
		else
			return null;
	}
	/**
	 * @since 03.08.2020
	 * @param opl
	 */
	protected void resetOPL(final EmuOPL opl)
	{
		opl.resetOPL();
	}
	/**
	 * @since 03.08.2020
	 * @return
	 */
	public long getLengthInMilliseconds()
	{
		double ms = 0d;
		final EmuOPL measureOPL = EmuOPL.createInstance(EmuOPL.version.OPL3, 49712, getOPLType());
		initialize(measureOPL);
		while (updateToOPL(measureOPL) && ms<60d*60d*1000d) 
			ms += 1000d / getRefresh();
		return (long)(ms+2000.5d);
	}
	/**
	 * @since 03.08.2020
	 * @param inputStream
	 * @throws IOException
	 */
	protected abstract void readOPL3Sequence(final RandomAccessInputStreamImpl inputStream) throws IOException;
	/**
	 * @since 03.08.2020
	 * @param url
	 */
	public abstract void setURL(URL url);
	/**
	 * @since 03.08.2020
	 * @param opl
	 * @return
	 */
	public abstract boolean updateToOPL(final EmuOPL opl);
	/**
	 * @since 03.08.2020
	 * @param opl
	 */
	public abstract void initialize(final EmuOPL opl);
	/**
	 * @since 03.08.2020
	 * @return
	 */
	public abstract double getRefresh();
	/**
	 * @since 03.08.2020
	 * @return
	 */
	public abstract String getSongName();
	/**
	 * @since 03.08.2020
	 * @return
	 */
	public abstract String getAuthor();
	/**
	 * @since 03.08.2020
	 * @return
	 */
	public abstract String getDescription();
	/**
	 * @since 03.08.2020
	 * @return
	 */
	public abstract String getTypeName();
	/**
	 * Return the needed OPLType: OPL2, DUAL_OPL2 or OPL3
	 * @since 16.08.2020
	 * @return
	 */
	public abstract oplType getOPLType();
}
