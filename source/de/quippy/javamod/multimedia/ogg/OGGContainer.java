/*
 * @(#) OGGContainer.java
 *
 * Created on 01.11.2010 by Daniel Becker
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
package de.quippy.javamod.multimedia.ogg;

import java.net.URL;
import java.util.Properties;

import javax.swing.JPanel;

import de.quippy.javamod.mixer.Mixer;
import de.quippy.javamod.multimedia.MultimediaContainer;
import de.quippy.javamod.multimedia.MultimediaContainerManager;
import de.quippy.javamod.multimedia.ogg.metadata.OggMetaData;

/**
 * @author Daniel Becker
 * @since 01.11.2010
 */
public class OGGContainer extends MultimediaContainer
{
	private static final String[] OGGFILEEXTENSION = new String [] 
	{
		"ogg", "oga"
	};
//	private JPanel oggConfigPanel;
	private JPanel oggInfoPanel;
	private OggMetaData oggMetaData = null;
	/**
	 * Will be executed during class load
	 */
	static
	{
		MultimediaContainerManager.registerContainer(new OGGContainer());
	}
	/**
	 * Constructor for OGGContainer
	 */
	public OGGContainer()
	{
		super();
	}
	/**
	 * @param url
	 * @return
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#getInstance(java.net.URL)
	 */
	@Override
	public MultimediaContainer getInstance(URL url)
	{
		MultimediaContainer result = super.getInstance(url);
		oggMetaData = new OggMetaData(url);
		((OGGInfoPanel)getInfoPanel()).fillInfoPanelWith(oggMetaData, getPrintableFileUrl());
		return result;
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#getSongName()
	 */
	@Override
	public String getSongName()
	{
		if (oggMetaData!=null) 
			return oggMetaData.getShortDescription();
		else
			return super.getSongName();
	}
	/**
	 * @param url
	 * @return
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#getSongInfosFor(java.net.URL)
	 */
	@Override
	public Object[] getSongInfosFor(URL url)
	{
		String songName = MultimediaContainerManager.getSongNameFromURL(url);
		Long duration = Long.valueOf(-1);
		try
		{
			OggMetaData metaData = new OggMetaData(url);
			songName = metaData.getShortDescription();
			duration = Long.valueOf(metaData.getLengthInMilliseconds());
		}
		catch (Throwable ex)
		{
		}
		return new Object[] { songName, duration };
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#createNewMixer()
	 */
	@Override
	public Mixer createNewMixer()
	{
		return new OGGMixer(getFileURL(), oggMetaData.getLengthInMilliseconds());
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
	 * @return
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#getFileExtensionList()
	 */
	@Override
	public String[] getFileExtensionList()
	{
		return OGGFILEEXTENSION;
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#getName()
	 */
	@Override
	public String getName()
	{
		return "ogg/vorbis-File";
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#getConfigPanel()
	 */
	@Override
	public JPanel getConfigPanel()
	{
		return null;
//		if (oggConfigPanel==null)
//		{
//			oggConfigPanel = new JPanel();
//		}
//		return oggConfigPanel;
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#getInfoPanel()
	 */
	@Override
	public JPanel getInfoPanel()
	{
		if (oggInfoPanel==null)
		{
			oggInfoPanel = new OGGInfoPanel();
		}
		return oggInfoPanel;
	}
	/**
	 * @param newProps
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#configurationChanged(java.util.Properties)
	 */
	@Override
	public void configurationChanged(Properties newProps)
	{
	}

	/**
	 * @param props
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#configurationSave(java.util.Properties)
	 */
	@Override
	public void configurationSave(Properties props)
	{
	}
}
