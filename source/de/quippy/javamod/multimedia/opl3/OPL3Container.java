/*
 * @(#) OPL3Container.java
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
package de.quippy.javamod.multimedia.opl3;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import javax.swing.JPanel;

import de.quippy.javamod.mixer.Mixer;
import de.quippy.javamod.multimedia.MultimediaContainer;
import de.quippy.javamod.multimedia.MultimediaContainerManager;
import de.quippy.javamod.multimedia.opl3.emu.EmuOPL;
import de.quippy.javamod.multimedia.opl3.sequencer.OPL3Sequence;
import de.quippy.javamod.system.Helpers;
import de.quippy.javamod.system.Log;

/**
 * @author Daniel Becker
 * @since 03.08.2020
 */
public class OPL3Container extends MultimediaContainer
{
	private static final String[] OPL3FILEEXTENSION = new String [] 
  	{
  		"rol", "laa", "cmf", "dro", "sci"
  	};
	public static final String PROPERTY_OPL3PLAYER_SOUNDBANK = "javamod.player.opl3.soundbankurl";
	public static final String PROPERTY_OPL3PLAYER_OPLVERSION = "javamod.player.opl3.oplversion";
	public static final String PROPERTY_OPL3PLAYER_VIRTUAL_STEREO = "javamod.player.opl3.virtualStereo";
	
	public static final String DEFAULT_SOUNDBANKURL = Helpers.EMPTY_STING;
	public static final String DEFAULT_VIRTUAL_STEREO = "false";
	public static final String DEFAULT_OPLVERSION = "FMOPL_072_YM3812";
	
	private OPL3ConfigPanel OPL3ConfigPanel;
	private OPL3InfoPanel OPL3InfoPanel;
	
	private OPL3Sequence opl3Sequence;
	
	OPL3Mixer currentMixer;

	/**
	 * Will be executed during class load
	 */
	static
	{
		MultimediaContainerManager.registerContainer(new OPL3Container());
	}
	/**
	 * Constructor for OPL3Container
	 */
	public OPL3Container()
	{
		super();
	}
	private float getSampleRate()
	{
		return 49716;
	}
	private EmuOPL.version getOPLVersion()
	{
		return ((OPL3ConfigPanel)getConfigPanel()).getOPLVersion();
	}
	public OPL3Mixer getCurrentMixer()
	{
		return currentMixer;
	}
	/**
	 * @param url
	 * @return
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#getInstance(java.net.URL)
	 */
	@Override
	public MultimediaContainer getInstance(final URL url)
	{
		MultimediaContainer result = super.getInstance(url);
		try
		{
			opl3Sequence = OPL3Sequence.createOPL3Sequence(url, OPL3ConfigPanel.getSoundBankURL());
			((OPL3InfoPanel)getInfoPanel()).fillInfoPanelWith(opl3Sequence);
		}
		catch (IOException ex)
		{
			Log.error("Loading of sequence failed", ex);
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
		String songName = MultimediaContainerManager.getSongNameFromURL(url);
		Long duration = Long.valueOf(-1);
		try
		{
			final OPL3Sequence opl3Sequence = OPL3Sequence.createOPL3Sequence(url, OPL3ConfigPanel.getSoundBankURL());
			songName = opl3Sequence.getSongName();
			duration = Long.valueOf(opl3Sequence.getLengthInMilliseconds());
		}
		catch (Throwable ex)
		{
			/* NOOP */
		}
		return new Object[] { songName, duration };
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#getSongName()
	 */
	@Override
	public String getSongName()
	{
		if (opl3Sequence!=null)
			return opl3Sequence.getSongName();
		else
			return super.getSongName();
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
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#getInfoPanel()
	 */
	@Override
	public JPanel getInfoPanel()
	{
		if (OPL3InfoPanel==null)
		{
			OPL3InfoPanel = new OPL3InfoPanel();
		}
		return OPL3InfoPanel;
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#getConfigPanel()
	 */
	@Override
	public JPanel getConfigPanel()
	{
		if (OPL3ConfigPanel==null)
		{
			OPL3ConfigPanel = new OPL3ConfigPanel();
			OPL3ConfigPanel.setParentContainer(this);
		}
		return OPL3ConfigPanel;
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#getFileExtensionList()
	 */
	@Override
	public String[] getFileExtensionList()
	{
		return OPL3FILEEXTENSION;
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#getName()
	 */
	@Override
	public String getName()
	{
		return "OPL3-File";
	}
	/**
	 * @param newProps
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#configurationChanged(java.util.Properties)
	 */
	@Override
	public void configurationChanged(final Properties props)
	{
		OPL3ConfigPanel configPanel = (OPL3ConfigPanel)getConfigPanel();
		configPanel.getRolSoundBankURL().setText(props.getProperty(PROPERTY_OPL3PLAYER_SOUNDBANK, DEFAULT_SOUNDBANKURL));
		configPanel.getVirtualStereo().setSelected(Boolean.parseBoolean(props.getProperty(PROPERTY_OPL3PLAYER_VIRTUAL_STEREO, DEFAULT_VIRTUAL_STEREO)));
		EmuOPL.version version = Enum.valueOf(EmuOPL.version.class, props.getProperty(PROPERTY_OPL3PLAYER_OPLVERSION, DEFAULT_OPLVERSION));
		configPanel.setOPLVersion(version);
	}
	/**
	 * @param props
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#configurationSave(java.util.Properties)
	 */
	@Override
	public void configurationSave(final Properties props)
	{
		OPL3ConfigPanel configPanel = (OPL3ConfigPanel)getConfigPanel();
		props.setProperty(PROPERTY_OPL3PLAYER_SOUNDBANK, configPanel.getRolSoundBankURL().getText());
		props.setProperty(PROPERTY_OPL3PLAYER_VIRTUAL_STEREO, Boolean.toString(configPanel.getVirtualStereo().isSelected()));
		props.setProperty(PROPERTY_OPL3PLAYER_OPLVERSION, configPanel.getOPLVersion().toString());
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#createNewMixer()
	 */
	@Override
	public Mixer createNewMixer()
	{
		if (opl3Sequence == null) return null;
		
		Properties props = new Properties();
		configurationSave(props);
		
		boolean doVirtualStereoMix = Boolean.parseBoolean(props.getProperty(PROPERTY_OPL3PLAYER_VIRTUAL_STEREO, DEFAULT_VIRTUAL_STEREO));
		
		currentMixer = new OPL3Mixer(getOPLVersion(), getSampleRate(), opl3Sequence, doVirtualStereoMix);
		return currentMixer;
	}

}
