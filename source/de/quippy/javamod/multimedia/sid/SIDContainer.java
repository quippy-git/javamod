/*
 * @(#) SIDContainer.java
 *
 * Created on 04.10.2009 by Daniel Becker
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
package de.quippy.javamod.multimedia.sid;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import javax.swing.JPanel;

import de.quippy.javamod.io.FileOrPackedInputStream;
import de.quippy.javamod.mixer.Mixer;
import de.quippy.javamod.multimedia.MultimediaContainer;
import de.quippy.javamod.multimedia.MultimediaContainerEvent;
import de.quippy.javamod.multimedia.MultimediaContainerManager;
import de.quippy.sidplay.libsidplay.components.sidtune.SidTune;
import de.quippy.sidplay.libsidplay.components.sidtune.SidTuneInfo;

/**
 * @author Daniel Becker
 * @since 04.10.2009
 */
public class SIDContainer extends MultimediaContainer
{
	/** these are copied from libsidplay.components.sidtune.defaultFileNameExt */
	private static final String[] SIDFILEEXTENSION = new String []
   	{
	 	// Preferred default file extension for single-file sidtunes
		// or sidtune description files in SIDPLAY INFOFILE format.
		"sid",
		// Common file extension for single-file sidtunes due to SIDPLAY/DOS
		// displaying files *.DAT in its file selector by default.
		// Originally this was intended to be the extension of the raw data
		// file
		// of two-file sidtunes in SIDPLAY INFOFILE format.
		"dat",
		// Extension of Amiga Workbench tooltype icon info files, which
		// have been cut to MS-DOS file name length (8.3).
		"inf"
   	};
	public static final String PROPERTY_SID_FREQUENCY = "javamod.player.sid.frequency";
	public static final String PROPERTY_SID_MODEL = "javamod.player.sid.sidmodel";
	public static final String PROPERTY_SID_OPTIMIZATION = "javamod.player.sid.optimization";
	public static final String PROPERTY_SID_USEFILTER = "javamod.player.sid.usesidfilter";
	public static final String PROPERTY_SID_VIRTUALSTEREO = "javamod.player.sid.virtualstrereo";
	/* GUI Constants ---------------------------------------------------------*/
	public static final String DEFAULT_SAMPLERATE = "44100";
	public static final String DEFAULT_SIDMODEL = "0";
	public static final String DEFAULT_OPTIMIZATION = "1";
	public static final String DEFAULT_USEFILTER = "true";
	public static final String DEFAULT_VIRTUALSTEREO = "false";

	public static final String[] SAMPLERATE = new String[]
 	{
		"8000", "11025", "16000", "22050", "33075", DEFAULT_SAMPLERATE, "48000", "96000"
 	};
	public static final String[] SIDMODELS = new String[]
	{
		"best", "SID 6581 (old model)", "SID 8580 (new model)"
	};

	private Properties currentProps = null;

	private SidTune sidTune;
	private SIDMixer currentMixer;
	private SIDConfigPanel sidConfigPanel;
	private SIDInfoPanel sidInfoPanel;

	/**
	 * Will be executed during class load
	 */
	static
	{
		MultimediaContainerManager.registerContainer(new SIDContainer());
	}
	/**
	 * Constructor for SIDContainer
	 */
	public SIDContainer()
	{
		super();
	}
	/**
	 * @param url
	 * @return
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#getInstance(java.net.URL)
	 */
	@Override
	public MultimediaContainer getInstance(final URL sidFileUrl)
	{
		final MultimediaContainer result = super.getInstance(sidFileUrl);
		sidTune = loadSidTune(sidFileUrl);
		if (!MultimediaContainerManager.isHeadlessMode()) ((SIDInfoPanel)getInfoPanel()).fillInfoPanelWith(getFileURL(), sidTune);
		return result;
	}
	@Override
	public String getSongName()
	{
		if (sidTune!=null)
			return getShortDescriptionFrom(sidTune);
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
			final SidTune sidTune = loadSidTune(url);
			if (sidTune!=null)
			{
				songName = getShortDescriptionFrom(sidTune);
				duration = Long.valueOf(sidTune.getInfo().songs * 1000);
			}
		}
		catch (final Throwable ex)
		{
		}
		return new Object[] { songName, duration };
	}
	public void nameChanged()
	{
		if (!MultimediaContainerManager.isHeadlessMode()) ((SIDInfoPanel)getInfoPanel()).fillInfoPanelWith(getFileURL(), sidTune);
		fireMultimediaContainerEvent(new MultimediaContainerEvent(this, MultimediaContainerEvent.SONG_NAME_CHANGED_OLD_INVALID, getSongName()));
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
	 * @since 11.10.2009
	 * @param sidFileURL
	 * @return a SIDTune
	 */
	private SidTune loadSidTune(final URL sidFileURL)
	{
		InputStream in = null;
		try
		{
			in = new FileOrPackedInputStream(sidFileURL);
			int size = in.available();
			if (size<1024) size = 1024;
			short [] sidTuneData = new short[size];
			int b;
			int index = 0;
			while ((b = in.read())!=-1)
			{
				sidTuneData[index++] = (short)(b&0xFF);
				if (index>=sidTuneData.length)
				{
					final short [] newBuffer = new short[sidTuneData.length + size];
					System.arraycopy(sidTuneData, 0, newBuffer, 0, sidTuneData.length);
					sidTuneData = newBuffer;
				}
			}
			return new SidTune(sidTuneData, index);
		}
		catch (final Exception ex)
		{
			throw new RuntimeException(ex);
		}
		finally
		{
			if (in!=null) try { in.close(); } catch (final IOException ex) { /* Log.error("IGNORED", ex); */ }
		}
	}
	/**
	 * @since 12.02.2011
	 * @param sidTune
	 * @return
	 */
	private String getShortDescriptionFrom(final SidTune sidTune)
	{
		final SidTuneInfo info = sidTune.getInfo();
		final String [] infoString = info.infoString;
		return infoString[0] + " [" + infoString[1] + "] " + Integer.toString(info.currentSong) + '/' + Integer.toString(info.songs) + " (" + infoString[2] + ')';
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#createNewMixer()
	 */
	@Override
	public Mixer createNewMixer()
	{
		configurationSave(currentProps); // fill with default values

		final int frequency = Integer.parseInt(currentProps.getProperty(PROPERTY_SID_FREQUENCY, DEFAULT_SAMPLERATE));
		final int sidModel = Integer.parseInt(currentProps.getProperty(PROPERTY_SID_MODEL, DEFAULT_SIDMODEL));
		final int optimization = Integer.parseInt(currentProps.getProperty(PROPERTY_SID_OPTIMIZATION, DEFAULT_OPTIMIZATION));
		final boolean useSIDFilter = Boolean.parseBoolean(currentProps.getProperty(PROPERTY_SID_USEFILTER, DEFAULT_USEFILTER));
		final boolean isStereo = Boolean.parseBoolean(currentProps.getProperty(PROPERTY_SID_VIRTUALSTEREO, DEFAULT_VIRTUALSTEREO));

		currentMixer = new SIDMixer(sidTune, this, frequency, sidModel, optimization, useSIDFilter, isStereo);
		return currentMixer;
	}
	public SIDMixer getCurrentMixer()
	{
		return currentMixer;
	}
	/**
	 * @param newProps
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#configurationChanged(java.util.Properties)
	 */
	@Override
	public void configurationChanged(final Properties newProps)
	{
		if (currentProps==null) currentProps = new Properties();
		currentProps.setProperty(PROPERTY_SID_FREQUENCY, newProps.getProperty(PROPERTY_SID_FREQUENCY, DEFAULT_SAMPLERATE));
		currentProps.setProperty(PROPERTY_SID_MODEL, newProps.getProperty(PROPERTY_SID_MODEL, DEFAULT_SIDMODEL));
		currentProps.setProperty(PROPERTY_SID_USEFILTER, newProps.getProperty(PROPERTY_SID_USEFILTER, DEFAULT_USEFILTER));
		currentProps.setProperty(PROPERTY_SID_VIRTUALSTEREO, newProps.getProperty(PROPERTY_SID_VIRTUALSTEREO, DEFAULT_VIRTUALSTEREO));

		if (!MultimediaContainerManager.isHeadlessMode())
		{
			final SIDConfigPanel configPanel = (SIDConfigPanel)getConfigPanel();
			configPanel.configurationChanged(newProps);
		}
	}
	/**
	 * @param props
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#configurationSave(java.util.Properties)
	 */
	@Override
	public void configurationSave(final Properties props)
	{
		if (currentProps==null) currentProps = new Properties();
		if (!MultimediaContainerManager.isHeadlessMode())
		{
			final SIDConfigPanel configPanel = (SIDConfigPanel)getConfigPanel();
			configPanel.configurationSave(currentProps);
		}

		if (props!=null)
		{
			props.setProperty(PROPERTY_SID_FREQUENCY, (currentProps!=null)?currentProps.getProperty(PROPERTY_SID_FREQUENCY, DEFAULT_SAMPLERATE):DEFAULT_SAMPLERATE);
			props.setProperty(PROPERTY_SID_MODEL, (currentProps!=null)?currentProps.getProperty(PROPERTY_SID_MODEL, DEFAULT_SIDMODEL):DEFAULT_SIDMODEL);
			props.setProperty(PROPERTY_SID_USEFILTER, (currentProps!=null)?currentProps.getProperty(PROPERTY_SID_USEFILTER, DEFAULT_USEFILTER):DEFAULT_USEFILTER);
			props.setProperty(PROPERTY_SID_VIRTUALSTEREO, (currentProps!=null)?currentProps.getProperty(PROPERTY_SID_VIRTUALSTEREO, DEFAULT_VIRTUALSTEREO):DEFAULT_VIRTUALSTEREO);
		}
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#getInfoPanel()
	 */
	@Override
	public JPanel getInfoPanel()
	{
		if (sidInfoPanel==null)
		{
			sidInfoPanel = new SIDInfoPanel();
			sidInfoPanel.setParentContainer(this);
		}
		return sidInfoPanel;
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#getConfigPanel()
	 */
	@Override
	public JPanel getConfigPanel()
	{
		if (sidConfigPanel==null)
		{
			sidConfigPanel = new SIDConfigPanel();
			sidConfigPanel.setParentContainer(this);
		}
		return sidConfigPanel;
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#getFileExtensionList()
	 */
	@Override
	public String[] getFileExtensionList()
	{
		return SIDFILEEXTENSION;
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#getName()
	 */
	@Override
	public String getName()
	{
		return "SID-File";
	}
	/**
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#cleanUp()
	 */
	@Override
	public void cleanUp()
	{
	}
}
