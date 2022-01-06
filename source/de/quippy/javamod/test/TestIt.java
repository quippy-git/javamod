/*
 * @(#) TestIt.java
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
package de.quippy.javamod.test;

import java.io.File;
import java.net.URL;
import java.util.Properties;

import de.quippy.javamod.main.JavaModMainBase;
import de.quippy.javamod.mixer.Mixer;
import de.quippy.javamod.multimedia.MultimediaContainer;
import de.quippy.javamod.multimedia.MultimediaContainerManager;
import de.quippy.javamod.multimedia.mod.ModContainer;
import de.quippy.javamod.system.Log;

/**
 * @author Daniel Becker
 * @since 21.04.2006
 */
public class TestIt extends JavaModMainBase
{
	private static class PlayThread extends Thread
	{
		private Mixer currentMixer;
		
		public PlayThread(URL modUrl)
		{
			super();

			try
			{
				Properties props = new Properties();
				props.setProperty(ModContainer.PROPERTY_PLAYER_ISP, ModContainer.DEFAULT_INTERPOLATION_INDEX);
				props.setProperty(ModContainer.PROPERTY_PLAYER_STEREO, ModContainer.DEFAULT_CHANNEL);
				props.setProperty(ModContainer.PROPERTY_PLAYER_WIDESTEREOMIX, "FALSE");
				props.setProperty(ModContainer.PROPERTY_PLAYER_NOISEREDUCTION, ModContainer.DEFAULT_NOISEREDUCTION);
				props.setProperty(ModContainer.PROPERTY_PLAYER_NOLOOPS, ModContainer.DEFAULT_NOLOOPS);
				props.setProperty(ModContainer.PROPERTY_PLAYER_MEGABASS, ModContainer.DEFAULT_MEGABASS);
				props.setProperty(ModContainer.PROPERTY_PLAYER_BITSPERSAMPLE, ModContainer.DEFAULT_BITSPERSAMPLE);			
				props.setProperty(ModContainer.PROPERTY_PLAYER_FREQUENCY, ModContainer.DEFAULT_SAMPLERATE);
				props.setProperty(ModContainer.PROPERTY_PLAYER_MSBUFFERSIZE, ModContainer.DEFAULT_MSBUFFERSIZE);
				props.setProperty(ModContainer.PROPERTY_PLAYER_MAXNNACHANNELS, ModContainer.DEFAULT_MAXNNACHANNELS);
				MultimediaContainerManager.configureContainer(props);
				MultimediaContainer multimediaContainer = MultimediaContainerManager.getMultimediaContainer(modUrl);
				
				currentMixer = multimediaContainer.createNewMixer();
			}
			catch (Throwable ex)
			{
				ex.printStackTrace(System.err);
			}
		}
//		public Mixer getCurrentMixer()
//		{
//			return currentMixer;
//		}
		@Override
		public void run()
		{
			currentMixer.startPlayback();
		}
	}
	
	/**
	 * Constructor for TestIt
	 */
	public TestIt()
	{
		super(true);
	}
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		try
		{
			// LINUX!!!
			//URL modUrl = new File("/daten/Multimedia/Files MOD/__testmods__/Gewitter - Stereo.it").toURI().toURL();
			//URL modUrl = new File("/daten/Multimedia/Files MOD/__testmods__/CheckResonance.it").toURI().toURL();
			URL modUrl = new File("/daten/Multimedia/Files MOD/1_channel_moog.it").toURI().toURL();

			// WINDOWS
			//URL modUrl = new File("m:\\Multimedia\\Files MOD\\1_channel_moog.it").toURI().toURL();
			//URL modUrl = new File("m:\\Multimedia\\Files MOD\\__testmods__\\glissando.mod").toURI().toURL();
			
			PlayThread playerThread = new PlayThread(modUrl);
//			final ModMixer mixer = (ModMixer)playerThread.getCurrentMixer();
//			BasicModMixer modMixer = mixer.getModMixer();
//			Module mod = mixer.getMod();
			playerThread.start();
			
			while (playerThread.isAlive())
			{
//				if (modMixer!=null)
//				{
//					System.out.printf("Channels: %03d\r", Integer.valueOf(modMixer.getCurrentUsedChannels()));
//				}
			}
		}
		catch (Exception ex)
		{
			Log.error("MIST", ex);
		}
	}
}
