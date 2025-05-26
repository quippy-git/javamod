/*
 * @(#) APEContainer.java
 *
 * Created on 22.12.2010 by Daniel Becker
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
package de.quippy.javamod.multimedia.ape;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import javax.swing.JPanel;

import de.quippy.javamod.mixer.Mixer;
import de.quippy.javamod.multimedia.MultimediaContainer;
import de.quippy.javamod.multimedia.MultimediaContainerManager;
import de.quippy.jmac.decoder.IAPEDecompress;
import de.quippy.jmac.info.APETag;
import de.quippy.jmac.tools.File;

/**
 * @author Daniel Becker
 * @since 22.12.2010
 */
public class APEContainer extends MultimediaContainer
{
	private static final String[] APEFILEEXTENSION = new String []
 	{
 		"ape", "apl", "mac"
 	};
	private APEInfoPanel apeInfoPanel;

	private APETag idTag;

	/**
	 * Will be executed during class load
	 */
	static
	{
		MultimediaContainerManager.registerContainer(new APEContainer());
	}
	/**
	 * Constructor for APEContainer
	 */
	public APEContainer()
	{
		super();
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#canExport()
	 */
	@Override
	public boolean canExport()
	{
		return true;
	}
	/**
	 * @param url
	 * @return
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#getInstance(java.net.URL)
	 */
	@Override
	public MultimediaContainer getInstance(final URL url)
	{
		final MultimediaContainer result = super.getInstance(url);
		File apeFile = null;
		try
		{
			apeFile = File.createFile(url, "r");
			final IAPEDecompress spAPEDecompress = IAPEDecompress.CreateIAPEDecompress(apeFile);
			idTag = spAPEDecompress.getApeInfoTag();
			if (!MultimediaContainerManager.isHeadlessMode()) ((APEInfoPanel)getInfoPanel()).fillInfoPanelWith(spAPEDecompress, getPrintableFileUrl(), getSongName());
		}
		catch (final IOException ex)
		{
		}
		finally
		{
			if (apeFile!=null) try { apeFile.close(); } catch (final IOException ex) { /* Log.error("IGNORED", ex); */ }
		}
		return result;
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#createNewMixer()
	 */
	@Override
	public Mixer createNewMixer()
	{
		return new APEMixer(getFileURL());
	}
	private String getSongName(final APETag idTag, final URL forURL)
	{
		if (idTag!=null)
		{
			try
			{
				final String artist = idTag.GetFieldString(APETag.APE_TAG_FIELD_ARTIST);
				final String album = idTag.GetFieldString(APETag.APE_TAG_FIELD_ALBUM);
				String title = idTag.GetFieldString(APETag.APE_TAG_FIELD_TITLE);
				if (title==null || title.isEmpty()) title = MultimediaContainerManager.getSongNameFromURL(forURL);

				final StringBuilder str = new StringBuilder();
				if (artist!=null && !artist.isEmpty())
				{
					str.append(artist).append(" - ");
				}
				if (album!=null && !album.isEmpty())
				{
					str.append(album).append(" - ");
				}
				return str.append(title).toString();
			}
			catch (final Throwable ex) // we can get the runtime exception "Unsupported Function"
			{
			}
		}
		return MultimediaContainerManager.getSongNameFromURL(forURL);
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#getSongName()
	 */
	@Override
	public String getSongName()
	{
		if (idTag!=null)
			return getSongName(idTag, getFileURL());
		else
			return super.getSongName();
	}
	/**
	 * @param url
	 * @return
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#getSongInfosFor(java.net.URL)
	 */
	@Override
	public Object[] getSongInfosFor(final URL url)
	{
		String songName = MultimediaContainerManager.getSongNameFromURL(url);
		Long duration = Long.valueOf(-1);
		try
		{
			final File apeFile = File.createFile(url, "r");
			final IAPEDecompress spAPEDecompress = IAPEDecompress.CreateIAPEDecompress(apeFile);
			final APETag idTag = spAPEDecompress.getApeInfoTag();
			songName = getSongName(idTag, url);
			duration = Long.valueOf(spAPEDecompress.getApeInfoDecompressLengthMS());
		}
		catch (final Throwable ex)
		{
		}
		return new Object[] { songName, duration };
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#getConfigPanel()
	 */
	@Override
	public JPanel getConfigPanel()
	{
		return null;
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#getInfoPanel()
	 */
	@Override
	public JPanel getInfoPanel()
	{
		if (apeInfoPanel==null)
		{
			apeInfoPanel = new APEInfoPanel();
			apeInfoPanel.setParentContainer(this);
		}
		return apeInfoPanel;
	}
	/**
	 * @param newProps
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#configurationChanged(java.util.Properties)
	 */
	@Override
	public void configurationChanged(final Properties newProps)
	{
	}
	/**
	 * @param props
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#configurationSave(java.util.Properties)
	 */
	@Override
	public void configurationSave(final Properties props)
	{
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#getFileExtensionList()
	 */
	@Override
	public String[] getFileExtensionList()
	{
		return APEFILEEXTENSION;
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#getName()
	 */
	@Override
	public String getName()
	{
		return "APE-File";
	}
	/**
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#cleanUp()
	 */
	@Override
	public void cleanUp()
	{
	}
}
