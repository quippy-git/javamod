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
import de.quippy.javamod.multimedia.mod.gui.ModInstrumentDialog;
import de.quippy.javamod.multimedia.mod.gui.ModPatternDialog;
import de.quippy.javamod.multimedia.mod.gui.ModSampleDialog;
import de.quippy.javamod.multimedia.mod.loader.Module;
import de.quippy.javamod.multimedia.mod.loader.ModuleFactory;
import de.quippy.javamod.system.Helpers;
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

	private static final String PROPERTY_PATTERN_POS = "javamod.player.position.patterns";
	private static final String PROPERTY_PATTERN_SIZE = "javamod.player.size.patterns";
	private static final String PROPERTY_PATTERN_VISABLE = "javamod.player.open.patterns";
	private static final String PROPERTY_SAMPLE_POS = "javamod.player.position.samples";
	private static final String PROPERTY_SAMPLE_SIZE = "javamod.player.size.samples";
	private static final String PROPERTY_SAMPLE_VISABLE = "javamod.player.open.samples";
	private static final String PROPERTY_INSTRUMENT_POS = "javamod.player.position.instruments";
	private static final String PROPERTY_INSTRUMENT_SIZE = "javamod.player.size.instruments";
	private static final String PROPERTY_INSTRUMENT_VISABLE = "javamod.player.open.instruments";

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
	protected static final String[] SAMPLERATE = new String[]
	{
		"8000", "11025", "16000", "22050", "33075", "44100", DEFAULT_SAMPLERATE, "96000", "192000"
	};
	protected static final String[] CHANNELS = new String[]
   	{
   		"1", DEFAULT_CHANNEL
   	};
	protected static final String[] BITSPERSAMPLE = new String[]
	{
		"8", DEFAULT_BITSPERSAMPLE, "24", "32"
	};
	protected static final String[] INTERPOLATION = new String[]
  	{
  		"none", "linear", "cubic spline", "windowed FIR"
  	};
	protected static final String[] BUFFERSIZE = new String[]
  	{
  		"30", "50", "75", "100", "125", "150", "175", "200", "225", DEFAULT_MSBUFFERSIZE, "500", "750"
  	};
	protected static final String[] MAX_NNA_CHANNELS= new String[]
  	{
  		"25", "50", "75", "100", "125", "150", "175", DEFAULT_MAXNNACHANNELS, "225", "250", "275", "300", "325", "350", "375", "400", "1000"
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
			// try to re-use an existing mixer, if its one for the same mod.
			// The "ModMixer::getLengthInMilliseconds" is synchronized to avoid double entry
			ModMixer theMixer = getCurrentMixer();
			if (theMixer==null || !theMixer.getMod().getFileName().equals(theMod.getFileName()))
					theMixer = new ModMixer(theMod, 8, 1, 22050, 0, false, false, false, false, loopValue, 0, 500, 0, 0, true);
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
		if (modInfoPanel==null)
		{
			modInfoPanel = new ModInfoPanel();
			modInfoPanel.setParentContainer(this);
		}
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
		final ModConfigPanel configPanel = (ModConfigPanel)getConfigPanel();
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
		// Info Dialog sizes and locations
		final ModInfoPanel infoPanel = (ModInfoPanel)getInfoPanel();
		infoPanel.setPatternDialogLocation(Helpers.getPointFromString(props.getProperty(PROPERTY_PATTERN_POS, "-1x-1")));
		infoPanel.setPatternDialogSize(Helpers.getDimensionFromString(props.getProperty(PROPERTY_PATTERN_SIZE, "640x480")));
		infoPanel.setPatternDialogVisable(Boolean.parseBoolean(props.getProperty(PROPERTY_PATTERN_VISABLE, "false")));
		infoPanel.setSampleDialogLocation(Helpers.getPointFromString(props.getProperty(PROPERTY_SAMPLE_POS, "-1x-1")));
		infoPanel.setSampleDialogSize(Helpers.getDimensionFromString(props.getProperty(PROPERTY_SAMPLE_SIZE, "640x480")));
		infoPanel.setSampleDialogVisable(Boolean.parseBoolean(props.getProperty(PROPERTY_SAMPLE_VISABLE, "false")));
		infoPanel.setInstrumentDialogLocation(Helpers.getPointFromString(props.getProperty(PROPERTY_INSTRUMENT_POS, "-1x-1")));
		infoPanel.setInstrumentDialogSize(Helpers.getDimensionFromString(props.getProperty(PROPERTY_INSTRUMENT_SIZE, "640x480")));
		infoPanel.setInstrumentDialogVisable(Boolean.parseBoolean(props.getProperty(PROPERTY_INSTRUMENT_VISABLE, "false")));
	}
	/**
	 * Get the values from the GUI and store them into the main Properties
	 * @since 13.10.2007
	 */
	@Override
	public void configurationSave(Properties props)
	{
		final ModConfigPanel configPanel = (ModConfigPanel)getConfigPanel();
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
		// Info Dialog sizes and locations
		final ModPatternDialog patternDialog = ((ModInfoPanel)getInfoPanel()).getModPatternDialog();
		props.setProperty(PROPERTY_PATTERN_POS, Helpers.getStringFromPoint(patternDialog.getLocation()));
		props.setProperty(PROPERTY_PATTERN_SIZE, Helpers.getStringFromDimension(patternDialog.getSize()));
		props.setProperty(PROPERTY_PATTERN_VISABLE, Boolean.toString(((ModInfoPanel)getInfoPanel()).getModPatternDialogisVisible()));
		final ModSampleDialog sampleDialog = ((ModInfoPanel)getInfoPanel()).getModSampleDialog();
		props.setProperty(PROPERTY_SAMPLE_POS, Helpers.getStringFromPoint(sampleDialog.getLocation()));
		props.setProperty(PROPERTY_SAMPLE_SIZE, Helpers.getStringFromDimension(sampleDialog.getSize()));
		props.setProperty(PROPERTY_SAMPLE_VISABLE, Boolean.toString(((ModInfoPanel)getInfoPanel()).getModSampleDialogisVisible()));
		final ModInstrumentDialog instrumentDialog = ((ModInfoPanel)getInfoPanel()).getModInstrumentDialog();
		props.setProperty(PROPERTY_INSTRUMENT_POS, Helpers.getStringFromPoint(instrumentDialog.getLocation()));
		props.setProperty(PROPERTY_INSTRUMENT_SIZE, Helpers.getStringFromDimension(instrumentDialog.getSize()));
		props.setProperty(PROPERTY_INSTRUMENT_VISABLE, Boolean.toString(((ModInfoPanel)getInfoPanel()).getModInstrumentDialogisVisible()));
	}
	public ModMixer createNewMixer0()
	{
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
		return new ModMixer(currentMod, bitsPerSample, channels, frequency, isp, wideStereoMix, noiseReduction, megaBass, dcRemoval, loopValue, maxNNAChannels, msBufferSize, ditherFilter, ditherType, ditherByPass);
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
		deregisterMixer();

		currentMixer = createNewMixer0();
		if (currentMixer==null) return null;
		
		registerMixer();
		
		return currentMixer;
	}
	/**
	 * @since 11.11.2023
	 * @param currentMixer
	 */
	private void deregisterMixer()
	{
		((ModInfoPanel)getInfoPanel()).getModPatternDialog().setMixer(null);
	}
	/**
	 * @since 11.11.2023
	 * @param currentMixer
	 */
	private void registerMixer()
	{
		((ModInfoPanel)getInfoPanel()).getModPatternDialog().setMixer(currentMixer);
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
		deregisterMixer();
	}
}
