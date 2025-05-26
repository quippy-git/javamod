/*
 * @(#) FLACContainer.java
 *
 * Created on 01.01.2011 by Daniel Becker
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
package de.quippy.javamod.multimedia.flac;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import javax.sound.sampled.AudioFormat;
import javax.swing.JPanel;

import de.quippy.javamod.io.FileOrPackedInputStream;
import de.quippy.javamod.mixer.Mixer;
import de.quippy.javamod.multimedia.MultimediaContainer;
import de.quippy.javamod.multimedia.MultimediaContainerManager;
import de.quippy.javamod.system.Helpers;
import de.quippy.jflac.FLACDecoder;
import de.quippy.jflac.metadata.VorbisComment;

/**
 * @author Daniel Becker
 * @since 01.01.2011
 */
public class FLACContainer extends MultimediaContainer
{
	private static final String[] FLACFILEEXTENSION = new String []
 	{
 		"flac"
 	};
 	private FLACInfoPanel flacInfoPanel;

 	private VorbisComment vorbisComment;
 	/**
	 * Will be executed during class load
	 */
	static
	{
		MultimediaContainerManager.registerContainer(new FLACContainer());
	}
	/**
	 * Constructor for FLACContainer
	 */
	public FLACContainer()
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
		InputStream inputStream = null;
		try
		{
			inputStream = new FileOrPackedInputStream(url);
			final FLACDecoder decoder = new FLACDecoder(inputStream);
			decoder.readMetadata();
			vorbisComment = decoder.getVorbisComment();
			final AudioFormat audioFormat = decoder.getStreamInfo().getAudioFormat();
			final long sampleRate = (long)audioFormat.getSampleRate();
			long duration = decoder.getStreamInfo().getTotalSamples() * 1000L / sampleRate;
			if (!MultimediaContainerManager.isHeadlessMode()) ((FLACInfoPanel)getInfoPanel()).fillInfoPanelWith(audioFormat, duration, Helpers.getFileNameFromURL(url), getSongName(), decoder.getVorbisComment());
		}
		catch (final Exception ex)
		{
			throw new RuntimeException(ex);
		}
		finally
		{
			if (inputStream!=null) try { inputStream.close(); } catch (final IOException ex) { /* Log.error("IGNORED", ex); */ }
		}
		return result;
	}
	private String getSongName(final VorbisComment vorbisComment, final URL forURL)
	{
		if (vorbisComment!=null)
		{
			try
			{
				final String artist = vorbisComment.getArtist();
				final String album = vorbisComment.getAlbum();
				String title = vorbisComment.getTitle();
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
		if (vorbisComment!=null)
			return getSongName(vorbisComment, getFileURL());
		else
			return super.getSongName();
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#createNewMixer()
	 */
	@Override
	public Mixer createNewMixer()
	{
		return new FLACMixer(getFileURL());
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
		InputStream inputStream = null;
		try
		{
			inputStream = new FileOrPackedInputStream(url);
			final FLACDecoder decoder = new FLACDecoder(inputStream);
			decoder.readMetadata();
			final VorbisComment vorbisComment = decoder.getVorbisComment();
			songName = getSongName(vorbisComment, url);
			final AudioFormat audioFormat = decoder.getStreamInfo().getAudioFormat();
			final long sampleRate = (long)audioFormat.getSampleRate();
			duration = Long.valueOf(decoder.getStreamInfo().getTotalSamples() * 1000L / sampleRate);
		}
		catch (final Throwable ex)
		{
		}
		finally
		{
			if (inputStream!=null) try { inputStream.close(); } catch (final IOException ex) { /* Log.error("IGNORED", ex); */ }
		}
		return new Object[] { songName, duration };
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#getName()
	 */
	@Override
	public String getName()
	{
		return "FLAC-File";
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#getFileExtensionList()
	 */
	@Override
	public String[] getFileExtensionList()
	{
		return FLACFILEEXTENSION;
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
		if (flacInfoPanel==null)
		{
			flacInfoPanel = new FLACInfoPanel();
			flacInfoPanel.setParentContainer(this);
		}
		return flacInfoPanel;
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
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#cleanUp()
	 */
	@Override
	public void cleanUp()
	{
	}
}
