/*
 * @(#) MidiContainer.java
 *
 * Created on 28.12.2007 by Daniel Becker
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
package de.quippy.javamod.multimedia.midi;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JPanel;

import de.quippy.javamod.io.FileOrPackedInputStream;
import de.quippy.javamod.io.wav.RMIFile;
import de.quippy.javamod.mixer.Mixer;
import de.quippy.javamod.multimedia.MultimediaContainer;
import de.quippy.javamod.multimedia.MultimediaContainerManager;
import de.quippy.javamod.system.Helpers;

/**
 * @author Daniel Becker
 * @since 28.12.2007
 */
public class MidiContainer extends MultimediaContainer
{
	private static final String[] MIDIFILEEXTENSION = new String []
  	{
  		"mid", "rmf", "rmi"
  	};
	public static final String PROPERTY_MIDIPLAYER_OUTPUTDEVICE = "javamod.player.midi.outputdevice";
	public static final String PROPERTY_MIDIPLAYER_SOUNDBANK = "javamod.player.midi.soundbankurl";
	public static final String PROPERTY_MIDIPLAYER_CAPTURE = "javamod.player.midi.capture";
	public static final String PROPERTY_MIDIPLAYER_MIXERNAME = "javamod.player.midi.mixername";
	public static final String PROPERTY_MIDIPLAYER_PORTNAME = "javamod.player.midi.portname";
	/* GUI Constants ---------------------------------------------------------*/
	public static final String DEFAULT_OUTPUTDEVICE = "Java Sound Synthesizer";
	public static final String DEFAULT_SOUNDBANKURL = Helpers.EMPTY_STING;
	public static final String DEFAULT_CAPUTRE = "0";
	public static final String DEFAULT_MIXERNAME = Helpers.EMPTY_STING;
	public static final String DEFAULT_PORTNAME = Helpers.EMPTY_STING;

	public static MidiDevice.Info[] MIDIOUTDEVICEINFOS;
	public static javax.sound.sampled.Mixer.Info[] MIXERDEVICEINFOS;

	private Properties currentProps = null;

	private MidiMixer currentMixer;
	private Sequence currentSequence;
	private MidiConfigPanel midiConfigPanel;
	private MidiInfoPanel midiInfoPanel;

	/**
	 * Will be executed during class load
	 */
	static
	{
		// This can sometimes take a while
		MIDIOUTDEVICEINFOS = getMidiOutDevices();
		MIXERDEVICEINFOS = getInputMixerNames();
		MultimediaContainerManager.registerContainer(new MidiContainer());
	}
	/**
	 * @since 24.10.2010
	 * @return
	 */
	private static MidiDevice.Info[] getMidiOutDevices()
	{
		final ArrayList<MidiDevice.Info> midiOuts = new ArrayList<>();
		final MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
		for (final Info element : infos)
		{
			try
			{
				final MidiDevice device = MidiSystem.getMidiDevice(element);
				if (device.getMaxReceivers() != 0) midiOuts.add(element);
			}
			catch (final MidiUnavailableException e)
			{
			}
		}
		final MidiDevice.Info[] result = new MidiDevice.Info[midiOuts.size()];
		midiOuts.toArray(result);
		return result;
	}
	/**
	 * @since 28.11.2010
	 * @param midiDeviceName
	 * @return
	 */
	protected static MidiDevice.Info getMidiOutDeviceByName(final String midiDeviceName)
	{
		for (final Info element : MidiContainer.MIDIOUTDEVICEINFOS)
		{
			if (element.getName().equalsIgnoreCase(midiDeviceName))
				return element;
		}
		return null;
	}
	/**
	 * @since 27.11.2010
	 * @return
	 */
	private static javax.sound.sampled.Mixer.Info[] getInputMixerNames()
	{
		final ArrayList<javax.sound.sampled.Mixer.Info> mixers = new ArrayList<>();
		final javax.sound.sampled.Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
		final Line.Info lineInfo = new Line.Info(TargetDataLine.class);
		for (final javax.sound.sampled.Mixer.Info element : mixerInfos)
		{
			final javax.sound.sampled.Mixer mixer = AudioSystem.getMixer(element);
			if (mixer.isLineSupported(lineInfo))
			{
				mixers.add(element);
			}
		}
		return mixers.toArray(new javax.sound.sampled.Mixer.Info[mixers.size()]);
	}
	protected static javax.sound.sampled.Mixer.Info getInputMixerByName(final String inputMixerDeviceName)
	{
		for (final javax.sound.sampled.Mixer.Info element : MidiContainer.MIXERDEVICEINFOS)
		{
			if (element.getName().equalsIgnoreCase(inputMixerDeviceName))
				return element;
		}
		return null;
	}

	/**
	 * Constructor for MidiContainer
	 */
	public MidiContainer()
	{
		super();
	}
	/**
	 * @param url
	 * @return
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#getInstance(java.net.URL)
	 */
	@Override
	public MultimediaContainer getInstance(final URL midiFileUrl)
	{
		final MultimediaContainer result = super.getInstance(midiFileUrl);
		currentSequence = getSequenceFromURL(midiFileUrl);
		if (!MultimediaContainerManager.isHeadlessMode()) ((MidiInfoPanel)getInfoPanel()).fillInfoPanelWith(currentSequence, getSongName());
		return result;
	}
	/**
	 * Some midi files have "EndOfTrack" messages that are far beyond the
	 * last valid midi message - which results in long silence.
	 * We try to correct that by simply setting a new "TrackEnd" at the
	 * highest tick count of the last event before an EndOfTrack message
	 * plus the amount of ticks for a whole frame.
	 * All existing EndOfTrack messages are deleted.
	 * @since 28.11.2025
	 * @param currentSequence
	 */
	private Sequence setCorrectEndingMessage(Sequence currentSequence)
	{
		try
		{
			long highestTick = 0;
			for (Track track : currentSequence.getTracks())
			{
				int lastIndex = track.size()-1;
				if (lastIndex<0) continue;
				
				MidiEvent lastTrackEvent = track.get(lastIndex);
				if (lastTrackEvent==null) continue;
				
				MidiMessage midiMessage = lastTrackEvent.getMessage();
				// if MetaMessage.ImmutableEndOfTrack would not be a private static class,
				// this check would be much easier!
				if (midiMessage!=null && midiMessage instanceof MetaMessage)
				{
					byte [] message = ((MetaMessage)midiMessage).getMessage();
					if (message!=null && message.length>2 && message[0]==-1 && message[1]==0x2F)
					{
						track.remove(lastTrackEvent);
						if (lastIndex>0) lastTrackEvent = track.get(lastIndex-1);
					}
				}
				final long lastTick = lastTrackEvent.getTick();
				if (lastTick > highestTick) highestTick = lastTick;
			}
			highestTick += currentSequence.getResolution();
			final MidiMessage endOfTrack = new MetaMessage(0x2F, null, 0);
			for (Track track : currentSequence.getTracks())
			{
				track.add(new MidiEvent(endOfTrack, highestTick));
			}
		}
		catch (InvalidMidiDataException ex)
		{
			// Ignore
		}
		return currentSequence;
	}
	/**
	 * @since 12.02.2011
	 * @param midiFileUrl
	 * @return
	 */
	private Sequence getSequenceFromURL(final URL midiFileUrl)
	{
		Sequence result = null;
		try
		{
			final String fileName = midiFileUrl.getPath();
			final String extension = fileName.substring(fileName.lastIndexOf('.')+1).toLowerCase();
			if (extension.equals("rmi"))
				result =  RMIFile.open(midiFileUrl);
			else
			{
				FileOrPackedInputStream input = null;
				try
				{
					input = new FileOrPackedInputStream(midiFileUrl);
					result =  MidiSystem.getSequence(input);
				}
				finally
				{
					if (input!=null) try { input.close(); } catch (final IOException ex) { /* Log.error("IGNORED", ex); */ }
				}
			}
		}
		catch (final Throwable ex)
		{
			throw new RuntimeException(ex);
		}
		return setCorrectEndingMessage(result);
	}
	private boolean getCapture()
	{
		return Boolean.valueOf((currentProps!=null)?currentProps.getProperty(PROPERTY_MIDIPLAYER_CAPTURE, DEFAULT_CAPUTRE):DEFAULT_CAPUTRE).booleanValue();
	}
	private File getSoundBankFile()
	{
		final String soundBankFile = (currentProps!=null)?currentProps.getProperty(PROPERTY_MIDIPLAYER_SOUNDBANK, DEFAULT_SOUNDBANKURL):DEFAULT_SOUNDBANKURL;
		if (soundBankFile==null || soundBankFile.isEmpty()) return null;
		return new File(soundBankFile);
	}
	private MidiDevice.Info getMidiInfo()
	{
		return MidiContainer.getMidiOutDeviceByName((currentProps!=null)?currentProps.getProperty(PROPERTY_MIDIPLAYER_OUTPUTDEVICE, DEFAULT_OUTPUTDEVICE):DEFAULT_OUTPUTDEVICE);
	}
	private javax.sound.sampled.Mixer.Info getMixerInfo()
	{
		return MidiContainer.getInputMixerByName((currentProps!=null)?currentProps.getProperty(PROPERTY_MIDIPLAYER_MIXERNAME, DEFAULT_MIXERNAME):DEFAULT_MIXERNAME);
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
			final Sequence sequence = getSequenceFromURL(url);
			duration = Long.valueOf((sequence!=null)?(sequence.getMicrosecondLength()/1000L):0);
		}
		catch (final Throwable ex)
		{
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
		return super.getSongName();
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#getFileExtensionList()
	 */
	@Override
	public String[] getFileExtensionList()
	{
		return MIDIFILEEXTENSION;
	}
	/**
	 * @return the name of the group of files this container knows
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#getName()
	 */
	@Override
	public String getName()
	{
		return "Midi-File";
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#canExport()
	 */
	@Override
	public boolean canExport()
	{
		return getCapture();
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#getInfoPanel()
	 */
	@Override
	public JPanel getInfoPanel()
	{
		if (midiInfoPanel==null)
		{
			midiInfoPanel = new MidiInfoPanel();
			midiInfoPanel.setParentContainer(this);
		}
		return midiInfoPanel;
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#getConfigPanel()
	 */
	@Override
	public JPanel getConfigPanel()
	{
		if (midiConfigPanel==null)
		{
			midiConfigPanel = new MidiConfigPanel();
			midiConfigPanel.setParentContainer(this);
		}
		return midiConfigPanel;
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#createNewMixer()
	 */
	@Override
	public Mixer createNewMixer()
	{
		configurationSave(currentProps);

		final MidiDevice.Info info = getMidiInfo();

		final javax.sound.sampled.Mixer.Info mixerInfo = getMixerInfo();
		boolean capture = getCapture();
		if (capture && mixerInfo==null) capture = false;

		currentMixer = new MidiMixer(currentSequence, info, getSoundBankFile(), capture, mixerInfo);
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
		currentProps.setProperty(PROPERTY_MIDIPLAYER_OUTPUTDEVICE, newProps.getProperty(PROPERTY_MIDIPLAYER_OUTPUTDEVICE, DEFAULT_OUTPUTDEVICE));
		currentProps.setProperty(PROPERTY_MIDIPLAYER_SOUNDBANK, newProps.getProperty(PROPERTY_MIDIPLAYER_SOUNDBANK, DEFAULT_SOUNDBANKURL));
		currentProps.setProperty(PROPERTY_MIDIPLAYER_CAPTURE, newProps.getProperty(PROPERTY_MIDIPLAYER_CAPTURE, DEFAULT_CAPUTRE));
		currentProps.setProperty(PROPERTY_MIDIPLAYER_MIXERNAME, newProps.getProperty(PROPERTY_MIDIPLAYER_MIXERNAME, DEFAULT_MIXERNAME));

		if (!MultimediaContainerManager.isHeadlessMode())
		{
			final MidiConfigPanel configPanel = (MidiConfigPanel)getConfigPanel();
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
			final MidiConfigPanel configPanel = (MidiConfigPanel)getConfigPanel();
			configPanel.configurationSave(currentProps);
		}

		if (props!=null)
		{
			props.setProperty(PROPERTY_MIDIPLAYER_OUTPUTDEVICE, currentProps.getProperty(PROPERTY_MIDIPLAYER_OUTPUTDEVICE, DEFAULT_OUTPUTDEVICE));
			props.setProperty(PROPERTY_MIDIPLAYER_SOUNDBANK, currentProps.getProperty(PROPERTY_MIDIPLAYER_SOUNDBANK, DEFAULT_SOUNDBANKURL));
			props.setProperty(PROPERTY_MIDIPLAYER_CAPTURE, currentProps.getProperty(PROPERTY_MIDIPLAYER_CAPTURE, DEFAULT_CAPUTRE));
			props.setProperty(PROPERTY_MIDIPLAYER_MIXERNAME, currentProps.getProperty(PROPERTY_MIDIPLAYER_MIXERNAME, DEFAULT_MIXERNAME));
		}
	}
	/**
	 * @see de.quippy.javamod.multimedia.MultimediaContainer#cleanUp()
	 */
	@Override
	public void cleanUp()
	{
	}
}
