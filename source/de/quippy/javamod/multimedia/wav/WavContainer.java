/*
 * @(#) WavContainer.java
 *
 * Created on 14.10.2007 by Daniel Becker
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
package de.quippy.javamod.multimedia.wav;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JPanel;

import de.quippy.javamod.io.FileOrPackedInputStream;
import de.quippy.javamod.mixer.Mixer;
import de.quippy.javamod.multimedia.MultimediaContainer;
import de.quippy.javamod.multimedia.MultimediaContainerManager;

/**
 * @author Daniel Becker
 * @since 14.10.2007
 */
public class WavContainer extends MultimediaContainer
{
	private static final String[] wavefile_Extensions;

	private WavInfoPanel wavInfoPanel;
	private WavMixer currentMixer;

	/**
	 * Will be executed during class load
	 */
	static
	{
		final AudioFileFormat.Type[] types = AudioSystem.getAudioFileTypes();
		wavefile_Extensions = new String[types.length + 1];
		for (int i=0; i<types.length; i++)
			wavefile_Extensions[i] = types[i].getExtension();
		wavefile_Extensions[types.length] = "img"; // we will interpret IMG files as CloneCD IMG files from an audio CD cue sheets
		MultimediaContainerManager.registerContainer(new WavContainer());
	}
	/**
	 * Constructor for WavContainer
	 */
	public WavContainer()
	{
		super();
	}
	/**
	 * @since 06.12.2024
	 * @param waveFileUrl
	 * @return
	 * @throws IOException
	 * @throws UnsupportedAudioFileException
	 */
	public static AudioInputStream getAudioInputStream(final URL waveFileUrl) throws IOException, UnsupportedAudioFileException
	{
		if (waveFileUrl.getFile().toLowerCase().endsWith(".img"))
			return new AudioInputStream(new FileOrPackedInputStream(waveFileUrl), new AudioFormat(44100, 16, 2, true, false), AudioSystem.NOT_SPECIFIED);
		else
			return AudioSystem.getAudioInputStream(new FileOrPackedInputStream(waveFileUrl));
	}
	/**
	 * @param url
	 * @return
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#getInstance(java.net.URL)
	 */
	@Override
	public MultimediaContainer getInstance(final URL waveFileUrl)
	{
		final MultimediaContainer result = super.getInstance(waveFileUrl);
		AudioInputStream audioInputStream = null;
		try
		{
			audioInputStream = getAudioInputStream(waveFileUrl);
			if (!MultimediaContainerManager.isHeadlessMode()) ((WavInfoPanel)getInfoPanel()).fillInfoPanelWith(audioInputStream, getSongName());
		}
		catch (final Exception ex)
		{
			throw new RuntimeException(ex);
		}
		finally
		{
			if (audioInputStream!=null) try { audioInputStream.close(); } catch (final IOException ex) { /* Log.error("IGNORED", ex); */ }
		}
		return result;
	}
	/**
	 * @param url
	 * @return
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#getSongInfosFor(java.net.URL)
	 */
	@Override
	public Object[] getSongInfosFor(final URL url)
	{
		final String songName = MultimediaContainerManager.getSongNameFromURL(url);
		Long duration = Long.valueOf(-1);
		try
		{
			final AudioInputStream audioInputStream = getAudioInputStream(url);
			final AudioFormat audioFormat = audioInputStream.getFormat();
			final float frameRate = audioFormat.getFrameRate();
			final long frameLength = audioInputStream.getFrameLength();
			if (frameRate != AudioSystem.NOT_SPECIFIED && frameLength != AudioSystem.NOT_SPECIFIED)
			{
				duration = Long.valueOf((long)((frameLength * 1000f / frameRate)+0.5));
			}
			else
			{
				final int channels = audioFormat.getChannels();
				final int sampleSizeInBits = audioFormat.getSampleSizeInBits();
				final int sampleSizeInBytes = sampleSizeInBits>>3;
				final int sampleRate = (int)audioFormat.getSampleRate();
				duration = Long.valueOf(((long)audioInputStream.available() / ((long)sampleSizeInBytes) / channels) * 1000L / sampleRate);
			}
		}
		catch (final Throwable ex)
		{
		}
		return new Object[] { songName, duration };
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#canExport()
	 */
	@Override
	public boolean canExport()
	{
		return false;
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#getInfoPanel()
	 */
	@Override
	public JPanel getInfoPanel()
	{
		if (wavInfoPanel==null)
		{
			wavInfoPanel = new WavInfoPanel();
			wavInfoPanel.setParentContainer(this);
		}
		return wavInfoPanel;
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
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#getFileExtensionList()
	 */
	@Override
	public String[] getFileExtensionList()
	{
		return wavefile_Extensions;
	}
	/**
	 * @return the name of the group of files this container knows
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#getName()
	 */
	@Override
	public String getName()
	{
		return "Wave-File";
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
	 * @throws UnsupportedAudioFileException
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#createNewMixer()
	 */
	@Override
	public Mixer createNewMixer()
	{
		currentMixer = new WavMixer(getFileURL());
		return currentMixer;
	}
	/**
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#cleanUp()
	 */
	@Override
	public void cleanUp()
	{
	}
}
