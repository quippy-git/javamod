/*
 * @(#)ModContainer.java
 *
 * Created on 12.10.2007 by Daniel Becker
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
package de.quippy.javamod.multimedia.mod;

import java.net.URL;
import java.util.Properties;

import javax.swing.JPanel;

import de.quippy.javamod.mixer.Mixer;
import de.quippy.javamod.multimedia.MultimediaContainer;
import de.quippy.javamod.multimedia.MultimediaContainerManager;
import de.quippy.javamod.multimedia.mod.loader.Module;
import de.quippy.javamod.multimedia.mod.loader.ModuleFactory;
import de.quippy.javamod.multimedia.mod.mixer.BasicModMixer;
import de.quippy.javamod.system.Log;

/**
 * @author: Daniel Becker
 * @since: 12.10.2007
 */
public class ModContainer extends MultimediaContainer
{
	public static final String PROPERTY_PLAYER_BITSPERSAMPLE = "javamod.player.bitspersample"; 
	public static final String PROPERTY_PLAYER_STEREO = "javamod.player.stereo"; 
	public static final String PROPERTY_PLAYER_FREQUENCY = "javamod.player.frequency"; 
	public static final String PROPERTY_PLAYER_MSBUFFERSIZE = "javamod.player.msbuffersize"; 
	public static final String PROPERTY_PLAYER_ISP = "javamod.player.ISP"; 
	public static final String PROPERTY_PLAYER_WIDESTEREOMIX = "javamod.player.widestereomix"; 
	public static final String PROPERTY_PLAYER_NOISEREDUCTION = "javamod.player.noisereduction"; 
	public static final String PROPERTY_PLAYER_MEGABASS = "javamod.player.megabass"; 
	public static final String PROPERTY_PLAYER_DCREMOVAL = "javamod.player.dcremoval"; 
	public static final String PROPERTY_PLAYER_NOLOOPS = "javamod.player.noloops"; 
	public static final String PROPERTY_PLAYER_MAXNNACHANNELS = "javamod.player.max_nna_channels"; 
	public static final String PROPERTY_PLAYER_DITHERFILTER = "javamod.player.ditherfilter"; 
	public static final String PROPERTY_PLAYER_DITHERTYPE = "javamod.player.dithertype"; 
	public static final String PROPERTY_PLAYER_DITHERBYPASS = "javamod.player.ditherbypass"; 
	/* GUI Constants ---------------------------------------------------------*/
	public static final String DEFAULT_BITSPERSAMPLE = "16";
   	public static final String DEFAULT_CHANNEL = "2";
	public static final String DEFAULT_SAMPLERATE = "48000";
	public static final String DEFAULT_MSBUFFERSIZE = "250";
	public static final String DEFAULT_WIDESTEREOMIX = "false";
	public static final String DEFAULT_NOISEREDUCTION = "false";
	public static final String DEFAULT_MEGABASS = "true";
	public static final String DEFAULT_DCREMOVAL = "true";
	public static final String DEFAULT_NOLOOPS = "1";
	public static final String DEFAULT_MAXNNACHANNELS  = "200";
	public static final String DEFAULT_INTERPOLATION_INDEX = "3";
	public static final String DEFAULT_DITHERFILTER = "4";
	public static final String DEFAULT_DITHERTYPE = "2";
	public static final String DEFAULT_DITHERBYPASS = "false";
	public static final String[] SAMPLERATE = new String[]
	{
		"8000", "11025", "16000", "22050", "33075", "44100", DEFAULT_SAMPLERATE, "96000", "192000"
	};
	public static final String[] CHANNELS = new String[]
   	{
   		"1", DEFAULT_CHANNEL
   	};
	public static final String[] BITSPERSAMPLE = new String[]
	{
		"8", DEFAULT_BITSPERSAMPLE, "24", "32"
	};
	public static final String[] INTERPOLATION = new String[]
  	{
  		"none", "linear", "cubic spline", "windowed FIR"
  	};
	public static final String[] BUFFERSIZE = new String[]
  	{
  		"30", "50", "75", "100", "125", "150", "175", "200", "225", "250", "500", "750"
  	};
	public static final String[] MAX_NNA_CHANNELS= new String[]
  	{
  		"25", "50", "75", "100", "125", "150", "175", "200", "225", "250", "275", "300", "325", "350", "375", "400", "1000"
  	};

	private Module currentMod;
	private ModMixer currentMixer;
	private ModInfoPanel modInfoPanel;
	private ModConfigPanel modConfigPanel;
	
	/**
	 * Will be executed during class load
	 */
	static
	{
		MultimediaContainerManager.registerContainer(new ModContainer());
	}
	
	/**
	 * @since: 12.10.2007
	 */
	public ModContainer()
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
		try
		{
			currentMod = ModuleFactory.getInstance(url);
			if (currentMod==null)
			{
				Log.error("[ModContainer] Failed with loading of " + url.toString(), new Exception("Modfile "+url.toString()+" is obviously corrupt!"));
				result = null;
			}
			else
			{
				((ModInfoPanel)getInfoPanel()).fillInfoPanelWith(currentMod);
			}
		}
		catch (Exception ex)
		{
			currentMod = null;
			throw new RuntimeException(ex);
		}
		return result;
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#getSongName()
	 */
	@Override
	public String getSongName()
	{
		if (currentMod!=null)
		{
			String songName = currentMod.getSongName();
			if (songName!=null && songName.trim().length()!=0)
				return songName;
		}
		return super.getSongName();
	}
	/**
	 * @param url
	 * @return gentleman agreement: Object[] { String::songname, Long::duration }
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#getSongInfosFor(java.net.URL)
	 */
	@Override
	public Object[] getSongInfosFor(URL url)
	{
		String songName = MultimediaContainerManager.getSongNameFromURL(url);
		Long duration = Long.valueOf(-1);
		try
		{
			final Module theMod = ModuleFactory.getInstance(url);
			final String modSongName = theMod.getSongName();
			if (modSongName!=null && modSongName.trim().length()!=0) songName = modSongName;
			final ModConfigPanel configPanel = (ModConfigPanel)getConfigPanel();
			int loopValue = configPanel.getLoopValue();
			if (loopValue == ModConstants.PLAYER_LOOP_DEACTIVATED) loopValue = ModConstants.PLAYER_LOOP_IGNORE;
			ModMixer theMixer = new ModMixer(theMod, 8, 1, 22050, 0, false, false, false, false, loopValue, 0, 500, 0, 0, true);
			duration = Long.valueOf(theMixer.getLengthInMilliseconds());
		}
		catch (Throwable ex)
		{
			/* NOOP */
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
		return true;
	}
	/**
	 * @return
	 * @since 13.10.2007
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#getInfoPanel()
	 */
	@Override
	public JPanel getInfoPanel()
	{
		if (modInfoPanel==null) modInfoPanel = new ModInfoPanel();
		return modInfoPanel;
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#getConfigPanel()
	 */
	@Override
	public JPanel getConfigPanel()
	{
		if (modConfigPanel==null)
		{
			modConfigPanel = new ModConfigPanel();
			modConfigPanel.setParentContainer(this);
		}
		return modConfigPanel;
	}
	/**
	 * @see de.quippy.javamod.multimedia.MultimediaContainerInterface#getFileExtensionList()
	 * @since: 12.10.2007
	 * @return
	 */
	@Override
	public String[] getFileExtensionList()
	{
		return ModuleFactory.getSupportedFileExtensions();
	}
	/**
	 * @return the name of the group of files this container knows
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#getName()
	 */
	@Override
	public String getName()
	{
		return "Mod-File";
	}
	/**
	 * @param newProps
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#configurationChanged(java.util.Properties)
	 */
	@Override
	public void configurationChanged(Properties props)
	{
		ModConfigPanel configPanel = (ModConfigPanel)getConfigPanel();
		configPanel.getPlayerSetUp_SampleRate().setSelectedItem(props.getProperty(PROPERTY_PLAYER_FREQUENCY, DEFAULT_SAMPLERATE));
		configPanel.getPlayerSetUp_BufferSize().setSelectedItem(props.getProperty(PROPERTY_PLAYER_MSBUFFERSIZE, DEFAULT_MSBUFFERSIZE));
		configPanel.getPlayerSetUp_BitsPerSample().setSelectedItem(props.getProperty(PROPERTY_PLAYER_BITSPERSAMPLE, DEFAULT_BITSPERSAMPLE)); 
		configPanel.getPlayerSetUp_Channels().setSelectedItem(props.getProperty(PROPERTY_PLAYER_STEREO, DEFAULT_CHANNEL)); 
		configPanel.getPlayerSetUp_Interpolation().setSelectedIndex(Integer.parseInt(props.getProperty(PROPERTY_PLAYER_ISP, DEFAULT_INTERPOLATION_INDEX)));
		configPanel.getPlayerSetUp_WideStereoMix().setSelected(Boolean.parseBoolean(props.getProperty(PROPERTY_PLAYER_WIDESTEREOMIX, DEFAULT_WIDESTEREOMIX)));
		configPanel.getPlayerSetUp_NoiseReduction().setSelected(Boolean.parseBoolean(props.getProperty(PROPERTY_PLAYER_NOISEREDUCTION, DEFAULT_NOISEREDUCTION)));
		configPanel.getPlayerSetUp_MegaBass().setSelected(Boolean.parseBoolean(props.getProperty(PROPERTY_PLAYER_MEGABASS, DEFAULT_MEGABASS)));
		configPanel.getPlayerSetUp_DCRemoval().setSelected(Boolean.parseBoolean(props.getProperty(PROPERTY_PLAYER_DCREMOVAL, DEFAULT_DCREMOVAL)));
		configPanel.setLoopValue(Integer.parseInt(props.getProperty(PROPERTY_PLAYER_NOLOOPS, DEFAULT_NOLOOPS)));
		configPanel.getPlayerSetUp_MaxNNAChannels().setSelectedItem(props.getProperty(PROPERTY_PLAYER_MAXNNACHANNELS, DEFAULT_MAXNNACHANNELS));
		configPanel.getPlayerSetUp_DitherFilterType().setSelectedIndex(Integer.parseInt(props.getProperty(PROPERTY_PLAYER_DITHERFILTER, DEFAULT_DITHERFILTER)));
		configPanel.getPlayerSetUp_DitherType().setSelectedIndex(Integer.parseInt(props.getProperty(PROPERTY_PLAYER_DITHERTYPE, DEFAULT_DITHERTYPE)));
		configPanel.getPlayerSetUp_ByPassDither().setSelected(Boolean.parseBoolean(props.getProperty(PROPERTY_PLAYER_DITHERBYPASS, DEFAULT_DITHERBYPASS)));
	}
	/**
	 * Get the values from the gui and store them into the main Properties
	 * @since 13.10.2007
	 */
	@Override
	public void configurationSave(Properties props)
	{
		ModConfigPanel configPanel = (ModConfigPanel)getConfigPanel();
		props.setProperty(PROPERTY_PLAYER_FREQUENCY, configPanel.getPlayerSetUp_SampleRate().getSelectedItem().toString());
		props.setProperty(PROPERTY_PLAYER_MSBUFFERSIZE, configPanel.getPlayerSetUp_BufferSize().getSelectedItem().toString());
		props.setProperty(PROPERTY_PLAYER_BITSPERSAMPLE, configPanel.getPlayerSetUp_BitsPerSample().getSelectedItem().toString());
		props.setProperty(PROPERTY_PLAYER_STEREO, configPanel.getPlayerSetUp_Channels().getSelectedItem().toString());
		props.setProperty(PROPERTY_PLAYER_ISP, Integer.toString(configPanel.getPlayerSetUp_Interpolation().getSelectedIndex()));
		props.setProperty(PROPERTY_PLAYER_WIDESTEREOMIX, Boolean.toString(configPanel.getPlayerSetUp_WideStereoMix().isSelected()));
		props.setProperty(PROPERTY_PLAYER_NOISEREDUCTION, Boolean.toString(configPanel.getPlayerSetUp_NoiseReduction().isSelected()));
		props.setProperty(PROPERTY_PLAYER_MEGABASS, Boolean.toString(configPanel.getPlayerSetUp_MegaBass().isSelected()));
		props.setProperty(PROPERTY_PLAYER_DCREMOVAL, Boolean.toString(configPanel.getPlayerSetUp_DCRemoval().isSelected()));
		props.setProperty(PROPERTY_PLAYER_NOLOOPS, Integer.toString(configPanel.getLoopValue()));
		props.setProperty(PROPERTY_PLAYER_MAXNNACHANNELS, configPanel.getPlayerSetUp_MaxNNAChannels().getSelectedItem().toString());
		props.setProperty(PROPERTY_PLAYER_DITHERFILTER, Integer.toString(configPanel.getPlayerSetUp_DitherFilterType().getSelectedIndex()));
		props.setProperty(PROPERTY_PLAYER_DITHERTYPE, Integer.toString(configPanel.getPlayerSetUp_DitherType().getSelectedIndex()));
		props.setProperty(PROPERTY_PLAYER_DITHERBYPASS, Boolean.toString(configPanel.getPlayerSetUp_ByPassDither().isSelected()));
	}
	/**
	 * Will create a new mixer for the currently loaded mod.
	 * We assume, that a mod is already loaded and therefore registering a listener is valid!
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#createNewMixer()
	 * @since: 12.10.2007
	 * @return
	 */
	@Override
	public Mixer createNewMixer()
	{
		deregisterUpdateListener();

		if (currentMod==null) return null; // you cannot get a mixer without a mod loaded.
		
		Properties props = new Properties();
		configurationSave(props);
		
		final int frequency = Integer.parseInt(props.getProperty(PROPERTY_PLAYER_FREQUENCY, DEFAULT_SAMPLERATE)); 
		final int bitsPerSample = Integer.parseInt(props.getProperty(PROPERTY_PLAYER_BITSPERSAMPLE, DEFAULT_BITSPERSAMPLE)); 
		final int channels = Integer.parseInt(props.getProperty(PROPERTY_PLAYER_STEREO, DEFAULT_CHANNEL));
		final int isp = Integer.parseInt(props.getProperty(PROPERTY_PLAYER_ISP, DEFAULT_INTERPOLATION_INDEX)); 
		final boolean wideStereoMix = Boolean.parseBoolean(props.getProperty(PROPERTY_PLAYER_WIDESTEREOMIX, DEFAULT_WIDESTEREOMIX)); 
		final boolean noiseReduction = Boolean.parseBoolean(props.getProperty(PROPERTY_PLAYER_NOISEREDUCTION, DEFAULT_NOISEREDUCTION));
		final boolean megaBass = Boolean.parseBoolean(props.getProperty(PROPERTY_PLAYER_MEGABASS, DEFAULT_MEGABASS));
		final boolean dcRemoval = Boolean.parseBoolean(props.getProperty(PROPERTY_PLAYER_DCREMOVAL, DEFAULT_DCREMOVAL));
		final int loopValue = Integer.parseInt(props.getProperty(PROPERTY_PLAYER_NOLOOPS, DEFAULT_NOLOOPS));
		final int maxNNAChannels = Integer.parseInt(props.getProperty(PROPERTY_PLAYER_MAXNNACHANNELS, DEFAULT_MAXNNACHANNELS));
		final int msBufferSize = Integer.parseInt(props.getProperty(PROPERTY_PLAYER_MSBUFFERSIZE, DEFAULT_MSBUFFERSIZE));
		final int ditherFilter = Integer.parseInt(props.getProperty(PROPERTY_PLAYER_DITHERFILTER, DEFAULT_DITHERFILTER));
		final int ditherType = Integer.parseInt(props.getProperty(PROPERTY_PLAYER_DITHERTYPE, DEFAULT_DITHERTYPE));
		boolean ditherByPass = Boolean.parseBoolean(props.getProperty(PROPERTY_PLAYER_DITHERBYPASS, DEFAULT_DITHERBYPASS));
		currentMixer = new ModMixer(currentMod, bitsPerSample, channels, frequency, isp, wideStereoMix, noiseReduction, megaBass, dcRemoval, loopValue, maxNNAChannels, msBufferSize, ditherFilter, ditherType, ditherByPass);
		
		registerUpdateListener();
		
		return currentMixer;
	}
	/**
	 * @since 11.11.2023
	 * @param currentMixer
	 */
	private void deregisterUpdateListener()
	{
		// These null checks are a bit superfluous, because we know when we call it
		if (currentMixer!=null && modInfoPanel!=null)
		{
			BasicModMixer mixer = currentMixer.getModMixer();
			if (mixer!=null)
			{
				mixer.deregisterUpdateListener(modInfoPanel.getModPatternDialog());
			}
		}
		// always stop the update thread - if it is not there, this does no harm
		((ModInfoPanel)getInfoPanel()).getModPatternDialog().stopUpdateThread();
		((ModInfoPanel)getInfoPanel()).getModPatternDialog().setMixer(null);
	}
	/**
	 * @since 11.11.2023
	 * @param currentMixer
	 */
	private void registerUpdateListener()
	{
		// These null checks are a bit superfluous, because we know when we call it
		if (currentMixer!=null && modInfoPanel!=null)
		{
			BasicModMixer mixer = currentMixer.getModMixer();
			if (mixer!=null)
			{
				mixer.registerUpdateListener(((ModInfoPanel)getInfoPanel()).getModPatternDialog());
				((ModInfoPanel)getInfoPanel()).getModPatternDialog().startUpdateThread();
			}
			((ModInfoPanel)getInfoPanel()).getModPatternDialog().setMixer(mixer);
		}
	}
	/**
	 * @since 14.10.2007
	 * @return
	 */
	public ModMixer getCurrentMixer()
	{
		return currentMixer;
	}
	public Module getCurrentMod()
	{
		return currentMod;
	}
	/**
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#cleanUp()
	 */
	@Override
	public void cleanUp()
	{
		deregisterUpdateListener();
	}
}
