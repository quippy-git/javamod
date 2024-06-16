/*
 * @(#) ImpulseTrackerMod.java
 * 
 * Created on 26.05.2006 by Daniel Becker
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
package de.quippy.javamod.multimedia.mod.loader.tracker;

import java.io.IOException;

import de.quippy.javamod.io.ModfileInputStream;
import de.quippy.javamod.multimedia.mod.ModConstants;
import de.quippy.javamod.multimedia.mod.loader.Module;
import de.quippy.javamod.multimedia.mod.loader.ModuleFactory;
import de.quippy.javamod.multimedia.mod.loader.instrument.Envelope;
import de.quippy.javamod.multimedia.mod.loader.instrument.Envelope.EnvelopeType;
import de.quippy.javamod.multimedia.mod.loader.instrument.Instrument;
import de.quippy.javamod.multimedia.mod.loader.instrument.InstrumentsContainer;
import de.quippy.javamod.multimedia.mod.loader.instrument.Sample;
import de.quippy.javamod.multimedia.mod.loader.pattern.PatternContainer;
import de.quippy.javamod.multimedia.mod.loader.pattern.PatternElement;
import de.quippy.javamod.multimedia.mod.midi.MidiMacros;

/**
 * @author Daniel Becker
 * @since 26.05.2006
 */
public class ImpulseTrackerMod extends ScreamTrackerMod
{
	private static final int [] autovibit2xm = new int [] { 0, 3, 1, 4, 2, 0, 0, 0 };
	private static final String[] MODFILEEXTENSION = new String [] 
 	{
 		"it", "mptm"
 	};
	/**
	 * Will be executed during class load
	 */
	static
	{
		ModuleFactory.registerModule(new ImpulseTrackerMod());
	}

	protected int cmwt;
	protected int special;
	protected int panningSeparation;
	protected String songMessage;
	protected MidiMacros midiMacros;
	protected int pwDepth;
	
	private static final int MAX_CHANNELS = 127;
	private static final int CHANNEL_MASK = 0x7F;

	/**
	 * Constructor for ImpulseTrackerMod
	 */
	public ImpulseTrackerMod()
	{
		super();
	}
	/**
	 * Constructor for ImpulseTrackerMod
	 * @param fileExtension
	 */
	protected ImpulseTrackerMod(String fileName)
	{
		super(fileName);
	}
	/**
	 * @return the file extensions this loader is suitable for
	 * @see de.quippy.javamod.multimedia.mod.loader.Module#getFileExtensionList()
	 */
	@Override
	public String [] getFileExtensionList()
	{
		return MODFILEEXTENSION;
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.mod.loader.Module#getPanningSeparation()
	 */
	@Override
	public int getPanningSeparation()
	{
		return panningSeparation;
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.mod.loader.tracker.ScreamTrackerMod#getFrequencyTable()
	 */
	@Override
	public int getFrequencyTable()
	{
		return ((songFlags & ModConstants.SONG_LINEARSLIDES)!=0)? ModConstants.IT_LINEAR_TABLE : ModConstants.IT_AMIGA_TABLE;
	}
	/**
	 * @param channel
	 * @return
	 * @see de.quippy.javamod.multimedia.mod.loader.tracker.ScreamTrackerMod#getChannelVolume(int)
	 */
	@Override
	public int getChannelVolume(final int channel)
	{
		return channelVolume[channel];
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.mod.loader.Module#getSongMessage()
	 */
	@Override
	public String getSongMessage()
	{
		return songMessage;
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.mod.loader.Module#getMidiConfig()
	 */
	@Override
	public MidiMacros getMidiConfig()
	{
		return midiMacros;
	}
	/**
	 * @return always false for these mods
	 * @see de.quippy.javamod.multimedia.mod.loader.Module#getFT2Tremolo()
	 */
	@Override
	public boolean getFT2Tremolo()
	{
		return false;
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.mod.loader.Module#getModSpeedIsTicks()
	 */
	@Override
	public boolean getModSpeedIsTicks()
	{
		return false;
	}

	/**
	 * @param env
	 * @param add
	 * @param maxValue
	 * @param inputStream
	 * @throws IOException
	 */
	private void readEnvelopeData(final Envelope env, final int add, final int maxValue, final ModfileInputStream inputStream) throws IOException
	{
		final long pos = inputStream.getFilePointer();
		
		env.setITType(inputStream.read());
		int nPoints = inputStream.read();
		if (nPoints>25) nPoints=25;
		env.setNPoints(nPoints);
		env.setLoopStartPoint(inputStream.read());
		env.setLoopEndPoint(inputStream.read());
		env.setSustainStartPoint(inputStream.read());
		env.setSustainEndPoint(inputStream.read());
		
		int [] values = new int[nPoints];
		int [] points = new int[nPoints];
		
		for (int i=0; i<nPoints; i++)
		{
			values[i] = (inputStream.read() + add)&0xFF;
			points[i] = inputStream.readIntelUnsignedWord();
		}
		
		env.setPositions(points);
		env.setValue(values);
		
		env.sanitize(maxValue);
		
		inputStream.seek(pos+82L);
	}
	private String[] readNames(final ModfileInputStream inputStream, final int marker, final int stringSize) throws IOException
	{
		String [] result = null;
		final long readMarker = inputStream.readIntelDWord();
		if (readMarker==marker)
		{
			final int size = inputStream.readIntelDWord();
			final int anzNames = size / stringSize;
			result = new String[anzNames];
			for (int c=0; c<anzNames; c++)
				result[c] = inputStream.readString(stringSize);
		}
		else
			inputStream.skipBack(4);
		
		return result;
	}
	/**
	 * @param inputStream
	 * @return true, if this is an impulse tracker mod, false if this is not clear
	 * @see de.quippy.javamod.multimedia.mod.loader.Module#checkLoadingPossible(de.quippy.javamod.io.ModfileInputStream)
	 */
	@Override
	public boolean checkLoadingPossible(final ModfileInputStream inputStream) throws IOException
	{
		String id = inputStream.readString(4);
		inputStream.seek(0);
		return id.equals("IMPM");
	}
	/**
	 * @param fileName
	 * @return
	 * @see de.quippy.javamod.multimedia.mod.loader.Module#getNewInstance(java.lang.String)
	 */
	@Override
	protected Module getNewInstance(final String fileName)
	{
		return new ImpulseTrackerMod(fileName);
	}
	/**
	 * @param inputStream
	 * @return
	 * @see de.quippy.javamod.multimedia.mod.loader.Module#loadModFile(java.io.DataInputStream)
	 */
	@Override
	protected void loadModFileInternal(final ModfileInputStream inputStream) throws IOException
	{
		setModType(ModConstants.MODTYPE_IT);
		setSongRestart(0);
		tempoMode = ModConstants.TEMPOMODE_CLASSIC;
		rowsPerBeat = 4;
		rowsPerMeasure = 16;
		boolean hasModPlugExtensions = false;

		// IT-ID:
		setModID(inputStream.readString(4));
		if (!getModID().equals("IMPM")) throw new IOException("Unsupported IT Module!");

		// Songname
		setSongName(inputStream.readString(26));
		
		//PHiliht = Pattern row highlight information
		final int loadedRowsPerBeat = inputStream.read();
		final int loadedRowsPerMeasure = inputStream.read();
		
		//OrdNum:   Number of orders in song
		setSongLength(inputStream.readIntelUnsignedWord());
		//InsNum:   Number of instruments in song
		setNInstruments(inputStream.readIntelUnsignedWord());
		//SmpNum:   Number of samples in song
		setNSamples(inputStream.readIntelUnsignedWord());
	    //PatNum:   Number of patterns in song
		setNPattern(inputStream.readIntelUnsignedWord());
		if (getNInstruments()>0xFF || getNSamples()==0 || getNPattern()==0 || getSongLength()==0) throw new IOException("Unsupported IT Module!");

		//Cwt:      Created with tracker.
		version = inputStream.readIntelUnsignedWord();
		//Cmwt:     Compatible with tracker with version greater than value. (ie. format version)
		cmwt = inputStream.readIntelUnsignedWord();

		//Flags:    Bit 0: On = Stereo, Off = Mono
		//          Bit 1: Vol0MixOptimizations - If on, no mixing occurs if
		//                 the volume at mixing time is 0 (redundant v1.04+)
		//          Bit 2: On = Use instruments, Off = Use samples.
		//          Bit 3: On = Linear slides, Off = Amiga slides.
		//          Bit 4: On = Old Effects, Off = IT Effects
		//                  Differences:
		//                 - Vibrato is updated EVERY frame in IT mode, whereas
		//                    it is updated every non-row frame in other formats.
		//                    Also, it is two times deeper with Old Effects ON
		//                 - Command Oxx will set the sample offset to the END
		//                   of a sample instead of ignoring the command under
		//                   old effects mode.
		//                 - (More to come, probably)
		//          Bit 5: OFF = Link Effect G's memory with Effect E/F. Also
		//                      Gxx with an instrument present will cause the
		//                      envelopes to be retriggered. If you change a
		//                      sample on a row with Gxx, it'll adjust the
		//                      frequency of the current note according to:
		//                        NewFrequency = OldFrequency * NewC5 / OldC5;
		//          Bit 6: Use MIDI pitch controller, Pitch depth given by PWD
		//          Bit 7: Request embedded MIDI configuration
		//                 (Coded this way to permit cross-version saving)
		flags = inputStream.readIntelUnsignedWord();
		if ((flags & 0x01)!=0)	 songFlags |= ModConstants.SONG_ISSTEREO;
		if ((flags & 0x04)!=0)	 songFlags |= ModConstants.SONG_USEINSTRUMENTS;
		if ((flags & 0x08)!=0)	 songFlags |= ModConstants.SONG_LINEARSLIDES;
		if ((flags & 0x10)!=0)	 songFlags |= ModConstants.SONG_ITOLDEFFECTS;
		if ((flags & 0x20)!=0)	 songFlags |= ModConstants.SONG_ITCOMPATMODE;
		if ((flags & 0x80)!=0)	 songFlags |= ModConstants.SONG_EMBEDMIDICFG;
		if ((flags & 0x1000)!=0) songFlags |= ModConstants.SONG_EXFILTERRANGE;

		//Special:  Bit 0: On = song message attached.
		//                 Song message:
		//                  Stored at offset given by "Message Offset" field.
		//                  Length = MsgLgth.
		//                  NewLine = 0Dh (13 dec)
		//                  EndOfMsg = 0
		//
		//                 Note: v1.04+ of IT may have song messages of up to
		//                       8000 bytes included.
		//          Bit 1: Reserved (edit history)
		//          Bit 2: Reserved (special - whatever that is)
		//          Bit 3: MIDI configuration embedded
		//          Bit 4-15: Reserved
		special = inputStream.readIntelUnsignedWord();
		final boolean hasSongMessage = ((special&0x01)!=0);
		final boolean hasEditHistory = ((special&0x02)!=0);
		final boolean hasHighlight   = ((special&0x04)!=0); // was reserved, is now defining presence of rowHighlights 
		final boolean hasMidiMacros  = ((flags&0x80)!=0) || ((special&0x08)!=0);
		
		//GV:       Global volume. (0->128) All volumes are adjusted by this
		int headerGlobalVolume = inputStream.read();
		if (headerGlobalVolume==0 || headerGlobalVolume>ModConstants.MAXGLOBALVOLUME) headerGlobalVolume = ModConstants.MAXGLOBALVOLUME;
		setBaseVolume(headerGlobalVolume);
		//MV:       Mix volume (0->127) During mixing, this value controls the magnitude of the wave being mixed.
		int sampleMixVolume = inputStream.read()&0x7F;
		if (sampleMixVolume==0 || sampleMixVolume>127) sampleMixVolume = 127;
		setMixingPreAmp(sampleMixVolume);
		//IS:       Initial Speed of song.
		setTempo(inputStream.read());
		//IT:       Initial Tempo of song
		setBPMSpeed(inputStream.read());
		//Sep:      Panning separation between channels (0->128, 128 is max sep.)
		panningSeparation = inputStream.read();
		//PWD:      Pitch wheel depth for MIDI controllers
		pwDepth = inputStream.read();
		
		// We read the Message later...
		//MsgLgth
		final int msgLength = inputStream.readIntelUnsignedWord();
		//MsgOffset
		final int msgPointer = inputStream.readIntelDWord();
		//4 Byte reserved
		final int reserved = inputStream.readIntelDWord();

		//Chnl Pan: Each byte contains a panning value for a channel. Ranges from
		//           0 (absolute left) to 64 (absolute right). 32 = central pan,
		//           100 = Surround sound.
		//           +128 = disabled channel (notes will not be played, but note
		//                                    that effects in muted channels are
		//                                    still processed)
		boolean hasFFPanningValue = false;
		usePanningValues = true;
		panningValue = new int[64];
		for (int i=0; i<64; i++)
		{
			int panValue = inputStream.read();
			
			if (panValue==100 || panValue==0xFF || (panValue & 0x80)!=0)
			{
				// we simply store those, as mixer responds to these in "initializeMixer()"
				panningValue[i] = panValue<<2;
				if (panValue==0xFF) hasFFPanningValue = true; // needed for sanity check for MPT-Version
			}
			else
			{
				panValue = (panValue&0x7F)<<2;
				if (panValue>256) panValue = 256;
				panningValue[i]=panValue; 
			}
		}
		
		// Checking for (Open) ModPlug Tracker
		if ((version & 0xF000)==0x5000)
		{
			int mptVersion = (version & 0x0FFF) << 16;
			if (reserved == 0x4F4D5054) //"OMPT"
				setModType(getModType() | ModConstants.MODTYPE_OMPT);
			else
			if (mptVersion>=0x01290000)
				mptVersion |= reserved & 0xFFFF;
			lastSavedWithVersion = mptVersion;
			setTrackerName("OpenMPT " + ModConstants.getModPlugVersionString(lastSavedWithVersion));
			if ((getModType()&ModConstants.MODTYPE_OMPT)==0) setTrackerName(getTrackerName() + ModConstants.COMPAT_MODE); 
		}
		else
		if (version==0x0888 || cmwt==0x888)
		{
			lastSavedWithVersion = 0x01170000;
			//setTrackerName("OpenMPT 1.17.02.26-1.18"); // Exact version number will be determined later.
			setModType(getModType() | ModConstants.MODTYPE_OMPT);
		}
		else
		if (version == 0x0214 && cmwt==0x202 && reserved==0)
		{
			lastSavedWithVersion = 0x01090000;
			setTrackerName("ModPlug Tracker b3.3 - 1.09");
			setModType(getModType() | ModConstants.MODTYPE_MPT);
		}
		else
		if (version == 0x0300 && cmwt == 0x0300 && reserved==0 && getSongLength()==256 && panningSeparation == 128 && pwDepth == 0)
		{
			lastSavedWithVersion = 0x01170220;
			setTrackerName("OpenMPT 1.17.02.20-25");
			setModType(getModType() | ModConstants.MODTYPE_OMPT);
		}
		else
		{
			setTrackerName("Impulse Tracker V" + ModConstants.getAsHex((version>>8)&0xF, 1) + "." + ModConstants.getAsHex(version&0xFF, 2) + " (CmwT: " + ModConstants.getAsHex((cmwt>>8)&0xF, 1) + "." + ModConstants.getAsHex(cmwt&0xFF, 2) + ")");
		}

		if (hasHighlight)
		{
			// Identify, if the rowsPer* should be used:
			// MPT 1.09 and older (and maybe also newer) versions leave this blank (0/0), but have the "special" flag set.
			// Newer versions of MPT and OpenMPT 1.17 *always* write 4/16 here.
			// Thus, we will just ignore those old versions.
			// Note: OpenMPT 1.17.03.02 was the first version to properly make use of the time signature in the IT header.
			// This poses a small unsolvable problem:
			// - In compatible mode, we cannot distinguish this version from earlier 1.17 releases.
			//   Thus we cannot know when to read this field or not (m_dwLastSavedWithVersion will always be 1.17.00.00).
			//   Luckily OpenMPT 1.17.03.02 should not be very wide-spread.
			// - In normal mode the time signature is always present in the song extensions anyway. So it's okay if we read
			//   the signature here and maybe overwrite it later when parsing the song extensions.
			if ((lastSavedWithVersion==-1 || lastSavedWithVersion>=0x01170302) && (loadedRowsPerBeat!=0 && loadedRowsPerMeasure!=0))
			{
				rowsPerBeat = loadedRowsPerBeat;
				rowsPerMeasure = loadedRowsPerMeasure;
			}
		}

		//Chnl Vol: Volume for each channel. Ranges from 0->64
		channelVolume = new int[64];
		for (int i=0; i<64; i++)
		{
			int vol = inputStream.read();
			if (vol<0) vol=0; else if (vol>64) vol=64;
			if (panningValue[i]!=(0xFF<<2)) channelVolume[i]=vol; // support 0xFF for disabled channel of MPT songs
		}

		//Orders:   This is the order in which the patterns are played.
		//           Valid values are from 0->199.
		//           255 = "---", End of song marker
		//           254 = "+++", Skip to next order
		// Song Arrangement
		allocArrangement(getSongLength());
		final int [] arrangement = getArrangement();
		for (int i=0; i<getSongLength(); i++) arrangement[i]=inputStream.read();
		
		// Now read the pointers
		long minFilePointer = (hasSongMessage && msgLength>0)?msgPointer:inputStream.getLength(); // this is for sanity checks later
		final int [] instrumentParaPointer = new int [getNInstruments()];
		for (int i=0; i<getNInstruments(); i++)
		{
			instrumentParaPointer[i] = inputStream.readIntelDWord();
			if (instrumentParaPointer[i]<minFilePointer) minFilePointer=instrumentParaPointer[i];
		} 
		final int [] samplesParaPointer = new int [getNSamples()];
		for (int i=0; i<getNSamples(); i++)
		{
			samplesParaPointer[i] = inputStream.readIntelDWord();
			if (samplesParaPointer[i]<minFilePointer) minFilePointer=samplesParaPointer[i];
		}
		final int [] patternParaPointer = new int [getNPattern()];
		for (int i=0; i<getNPattern(); i++)
		{
			patternParaPointer[i] = inputStream.readIntelDWord();
			if (patternParaPointer[i]<minFilePointer) minFilePointer=patternParaPointer[i];
		}
		
		// Reading IT edit history Info (well, we do not read it, but need to skip it...)
		long histSize = 0;
		if (hasEditHistory && inputStream.getFilePointer()+2<inputStream.getLength())
		{
			histSize = ((long)inputStream.readIntelUnsignedWord())<<3;
			// if we overlap into lowest parapointer, something is broken.
			if (minFilePointer<inputStream.getFilePointer()+histSize) histSize=0;
		}
		if (histSize>0) inputStream.skip(histSize);

		midiMacros = new MidiMacros();
		// read the MidiMacros
		if (hasMidiMacros && inputStream.getFilePointer() + MidiMacros.SIZE_OF_SCTUCT < inputStream.getLength())
		{
			midiMacros.loadFrom(inputStream);
		}
		if (version < 0x0214) // clear midi macros
			midiMacros.clearZxxMacros();
		
		if (histSize<=0)
		{
			final int beroMarker = inputStream.readIntelDWord();
			if (beroMarker==0x4D4F4455) // "MODU"
				setTrackerName("BeroTracker");
			else
				inputStream.skipBack(4);
		}
		else
		if ((version & 0xF000)==0x6000)
		{
			if ((version&0x0FFF)==0) setTrackerName("BeRoTracker");
		}
		
		// read Pattern Names:
		final String [] patNames = readNames(inputStream, 0x4D414E50, 32); // PNAM - LE saved
		// Read Channel Names
		final String [] chnNames = readNames(inputStream, 0x4D414E43, 20); // CNAM - LE saved

		hasModPlugExtensions = patNames!=null || chnNames!=null;
		
		//TODO: read mix plugins information

		// now for some disguised MPTs
		if (version==0x0217 && cmwt==0x200 && reserved==0)
		{
			if (hasModPlugExtensions || 
				arrangement!=null && arrangement[arrangement.length-1]==0xFF ||
				hasFFPanningValue)
			{
				lastSavedWithVersion = 0x01160000;
				setTrackerName("ModPlug Tracker 1.09 - 1.16");
			}
			else
			{
				lastSavedWithVersion = 0x01170000;
				setTrackerName("OpenMPT 1.17 " + ModConstants.COMPAT_MODE);
			}
			setModType(getModType() | ModConstants.MODTYPE_MPT);
		}

		// read the song Message
		if (hasSongMessage && msgLength>0 && msgPointer+msgLength<inputStream.getLength())
		{
			inputStream.seek(msgPointer);
			songMessage = inputStream.readString(msgLength);
		}
		
		InstrumentsContainer instrumentContainer = new InstrumentsContainer(this, getNInstruments(), getNSamples());
		this.setInstrumentContainer(instrumentContainer);
		for (int i=0; i<getNInstruments(); i++)
		{
			inputStream.seek(instrumentParaPointer[i]);

			if (inputStream.readMotorolaDWord()!=0x494D5049 /*IMPI*/) throw new IOException("Unsupported IT Instrument Header!");
			
			final Instrument currentIns = new Instrument();
			currentIns.setDosFileName(inputStream.readString(13));

			final Envelope volumeEnvelope = new Envelope(EnvelopeType.volume);
			currentIns.setVolumeEnvelope(volumeEnvelope);
			final Envelope panningEnvelope = new Envelope(EnvelopeType.panning);
			currentIns.setPanningEnvelope(panningEnvelope);
			final Envelope pitchEnvelope = new Envelope(EnvelopeType.panning);
			currentIns.setPitchEnvelope(pitchEnvelope);

			// Depending on cmwt:
			if (cmwt<0x200) // Old Instrument format
			{
				volumeEnvelope.setITType(inputStream.read());
				volumeEnvelope.setLoopStartPoint(inputStream.read());
				volumeEnvelope.setLoopEndPoint(inputStream.read());
				volumeEnvelope.setSustainStartPoint(inputStream.read());
				volumeEnvelope.setSustainEndPoint(inputStream.read());
				inputStream.skip(2);
				currentIns.setVolumeFadeOut(inputStream.readIntelUnsignedWord() << 6);
				currentIns.setNNA(inputStream.read());
				currentIns.setDublicateNoteCheck(inputStream.read());
				inputStream.skip(2); // TrackerVersion, that saved the instrument - ignored
				inputStream.skip(2); // NoS - ignored
				currentIns.setGlobalVolume(128);
				currentIns.setPanning(false);
				currentIns.setDefaultPan(128);
			}
			else
			{
				currentIns.setNNA(inputStream.read());
				currentIns.setDublicateNoteCheck(inputStream.read());
				currentIns.setDublicateNoteAction(inputStream.read());
				currentIns.setVolumeFadeOut(inputStream.readIntelUnsignedWord() << 5);
				currentIns.setPitchPanSeparation(inputStream.read());
				currentIns.setPitchPanCenter(inputStream.read());
				currentIns.setGlobalVolume(inputStream.read());
				// Default Pan, 0->64, &128 => Don't use
				int panning = inputStream.read();
				currentIns.setPanning((panning&0x80)==0);
				panning = (panning&0x7F)<<2;
				if (panning>256) panning=256;
				currentIns.setDefaultPan(panning);
				currentIns.setRandomVolumeVariation(inputStream.read());
				if (currentIns.randomVolumeVariation>100) currentIns.randomVolumeVariation = 100;
				currentIns.setRandomPanningVariation(inputStream.read());
				if (currentIns.randomPanningVariation>64) currentIns.randomVolumeVariation = 64;
				inputStream.skip(4);
			}
			
			currentIns.setName(inputStream.readString(26));
			if (cmwt<0x200) // Old Instrument format
			{
				inputStream.skip(6);
			}
			else
			{
				currentIns.setInitialFilterCutoff(inputStream.read());
				currentIns.setInitialFilterResonance(inputStream.read());
				inputStream.skip(4);
			}
			
			final int [] sampleIndex = new int[120];
			final int [] noteIndex = new int[120];
			for (int j=0; j<120; j++)
			{
				noteIndex[j] = inputStream.read();
				sampleIndex[j] = inputStream.read();
			}
			currentIns.setIndexArray(sampleIndex);
			currentIns.setNoteArray(noteIndex);
			
			if (cmwt<0x200) // Old Instrument format
			{
				// now 200 bytes of volume envelope data follow, according to ITTECH.TXT,
				// but we have no idea, what exactly that is and what it is used for.
				// Educated guess: it is a pre-calculation of the volume envelope
				// however, we read it, but do not use it (yet...)
				// neither Schism nor ModPlug use it, do life calculations instead 
				// we are in good company :)
				byte [] volEnvelope = new byte[200];
				inputStream.read(volEnvelope);
				volumeEnvelope.setOldITVolumeEnvelope(volEnvelope);

				// now for the envelope data
				int [] volumeEnvelopePosition = new int[25];
				int [] volumeEnvelopeValue = new int[25];
				int maxValues = 25;
				for (int nPoints=0; nPoints<maxValues; nPoints++)
				{
					volumeEnvelopePosition[nPoints] = inputStream.read();
					volumeEnvelopeValue[nPoints] = inputStream.read();
					// end point indication: we can stop reading
					// this is last data, file pointer is set to next instrument
					if (volumeEnvelopePosition[nPoints]==0xFF) maxValues = nPoints; 
				}
				volumeEnvelope.setNPoints(maxValues);
				volumeEnvelope.setPositions(volumeEnvelopePosition);
				volumeEnvelope.setValue(volumeEnvelopeValue);
				volumeEnvelope.sanitize(64);
			}
			else
			{
				readEnvelopeData(volumeEnvelope, 0, 64, inputStream); // 0..64, no transform
				readEnvelopeData(panningEnvelope, 32, 64, inputStream); //-32..+32 - transformed to 0-64
				readEnvelopeData(pitchEnvelope, 32, 64, inputStream); //-32..+32 - transformed to 0-64
			}
			
			// We need the global IT pitchWeelDepth at all instruments for MPT files
			currentIns.pitchWheelDepth = pwDepth;
			
			instrumentContainer.setInstrument(i, currentIns);
		}
		
		for (int i=0; i<getNSamples(); i++)
		{
			inputStream.seek(samplesParaPointer[i]);

			if (inputStream.readMotorolaDWord()!=0x494D5053 /*IMPI*/) throw new IOException("Unsupported IT Sample Header!");
			
			Sample currentSample = new Sample();

			currentSample.setDosFileName(inputStream.readString(13));
			int globalVolume = inputStream.read();
			if (globalVolume > 64) globalVolume = 64;
			currentSample.setGlobalVolume(globalVolume);
			
			final int flags = inputStream.read();
			currentSample.setFlags(flags);
            /*Bit 4. On = Use loop
            Bit 5. On = Use sustain loop
            Bit 6. On = Ping Pong loop, Off = Forwards loop
            Bit 7. On = Ping Pong Sustain loop, Off = Forwards Sustain loop*/
			int loopType = 0;
			if ((flags&0x10)==0x10) loopType |= ModConstants.LOOP_ON; 
			if ((flags&0x20)==0x20) loopType |= ModConstants.LOOP_SUSTAIN_ON; 
			if ((flags&0x40)==0x40) loopType |= ModConstants.LOOP_IS_PINGPONG; 
			if ((flags&0x80)==0x80) loopType |= ModConstants.LOOP_SUSTAIN_IS_PINGPONG; 
			currentSample.setLoopType(loopType);

			int volume = inputStream.read();
			if (volume > 64) volume = 64;
			currentSample.setVolume(volume);

			currentSample.setName(inputStream.readString(26));
			int CvT = inputStream.read();
			currentSample.setCvT(CvT);
			// DfP - Default Pan. Bits 0->6 = Pan value, Bit 7 ON to USE (opposite of inst)
			int panning = inputStream.read();
			currentSample.setPanning((panning&0x80)!=0);
			panning=(panning&0x7F)<<2;
			if (panning>256) panning=256;
			currentSample.setDefaultPanning(panning);
			currentSample.setLength(inputStream.readIntelDWord());

			final int repeatStart = inputStream.readIntelDWord();
			final int repeatStop = inputStream.readIntelDWord();
			currentSample.setLoopStart(repeatStart);
			currentSample.setLoopStop(repeatStop);
			currentSample.setLoopLength(repeatStop-repeatStart);

			currentSample.setFineTune(0);
			currentSample.setTranspose(0);
			int c4Speed = inputStream.readIntelDWord();
			if (c4Speed==0) c4Speed = 8363;
			else
			if (c4Speed<256) c4Speed = 256;
			currentSample.setBaseFrequency(c4Speed); 
			
			final int sustainLoopStart = inputStream.readIntelDWord();
			final int sustainLoopStop = inputStream.readIntelDWord();
			currentSample.setSustainLoopStart(sustainLoopStart);
			currentSample.setSustainLoopStop(sustainLoopStop);
			currentSample.setSustainLoopLength(sustainLoopStop - sustainLoopStart);
			
			int sampleOffset = inputStream.readIntelDWord();
			
			currentSample.setVibratoRate(inputStream.read());
			currentSample.setVibratoDepth(inputStream.read() & 0x7F);
			currentSample.setVibratoSweep((inputStream.read() + 3) >> 2);
			currentSample.setVibratoType(autovibit2xm[inputStream.read() & 0x07]);

			if (sampleOffset>0 && currentSample.length>0)
			{
				int loadFlag = 0;
				if (CvT==0xFF && (flags & (0x02 | 0x04))==0)  // ModPlug ADPCM compression
					loadFlag |= ModConstants.SM_ADPCM;
				else
				{
					loadFlag = ((CvT&0x1)==0x1)?ModConstants.SM_PCMS:ModConstants.SM_PCMU;
					if ((flags&0x2)!=0) loadFlag |= ModConstants.SM_16BIT;
					if ((flags&0x4)!=0) loadFlag |= ModConstants.SM_STEREO;
					if ((flags&0x8)!=0) loadFlag |= (version>=0x215 && (CvT&0x4)==0x4)?ModConstants.SM_IT2158:ModConstants.SM_IT2148;
				}
				currentSample.setStereo((loadFlag&ModConstants.SM_STEREO)!=0);
				inputStream.seek(sampleOffset);
				currentSample.setSampleType(loadFlag);
				readSampleData(currentSample, inputStream);
			}
			
			instrumentContainer.setSample(i, currentSample);
		}
		
		final long beyondLastSample = inputStream.getFilePointer();

		PatternContainer patternContainer = new PatternContainer(this, getNPattern());
		setPatternContainer(patternContainer);
		int maxChannelIndex = 0;
		
		for (int pattNum=0; pattNum<getNPattern(); pattNum++)
		{
			final long seek = patternParaPointer[pattNum];
			if (seek<=0) // Empty pattern - create one with default MAX_CHANNELS rows
			{
				patternContainer.createPattern(pattNum, MAX_CHANNELS); // PatternElements will get created when we know the amount of channels.
				continue;
			} 
			inputStream.seek(seek);
			
			int patternDataLength = inputStream.readIntelUnsignedWord();
			int rows = inputStream.readIntelUnsignedWord();
			if (rows<4 || rows>0x400) continue;
			
			inputStream.skip(4); // RESERVED
			
			// First, clear them:
			patternContainer.createPattern(pattNum, rows);

			int row = 0;
			// reserving 127 ints for all memories is easier than to resize the array afterwards...
			int [] lastMask= new int[MAX_CHANNELS];
			int [] lastNote= new int[MAX_CHANNELS];
			int [] lastIns= new int[MAX_CHANNELS];
			int [] lastVolCmd = new int[MAX_CHANNELS];
			int [] lastVolOp = new int[MAX_CHANNELS];
			int [] lastCmd= new int[MAX_CHANNELS];
			int [] lastData= new int[MAX_CHANNELS];
			patternContainer.createPatternRow(pattNum, row, MAX_CHANNELS);
			while (patternDataLength>0) 
			{
				final int channelByte = inputStream.read(); patternDataLength--;
				if (channelByte==0) // end of row marker
				{
					// end of pattern data?
					if (patternDataLength<=0) break;
					patternContainer.createPatternRow(pattNum, ++row, MAX_CHANNELS);
					continue;
				}
				final int channel = (channelByte & CHANNEL_MASK) - 1;
				if (channel>maxChannelIndex) maxChannelIndex = channel;
				PatternElement element = patternContainer.createPatternElement(pattNum, row, channel);
				
				if ((channelByte & 0x80)!=0)
				{
					lastMask[channel] = inputStream.read(); patternDataLength--;
				}
				if ((lastMask[channel]&0x01)!=0 || (lastMask[channel]&0x10)!=0)
				{
					if ((lastMask[channel]&0x01)!=0)
					{
						lastNote[channel] = inputStream.read(); patternDataLength--;
					}
					int noteIndex = lastNote[channel]; 
					int period;
					if (noteIndex==0xFF) // Note Off!
					{
						noteIndex = period = ModConstants.KEY_OFF;
					}
					else
					if (noteIndex==0xFE) // Volume Off!
					{
						noteIndex = period = ModConstants.NOTE_CUT;
					}
//					else
//					// internally IT uses note 0xFD as its blank value, but loading it as such is probably
//					// undesirable since old Schism Tracker used this value incorrectly for note fade
//					// MPT however does exactly that for all ITs not being saved with OMPT
//					if (noteIndex==0xFD) // NOTE NONE!
//					{
//						noteIndex = period = ModConstants.NO_NOTE;
//					}
					else
					if (noteIndex>119) // per definition: 119<noteindex<0xFE is note_fade
					{
						noteIndex = period = ModConstants.NOTE_FADE;
					}
					else
					{
						period = (noteIndex<ModConstants.noteValues.length)?ModConstants.noteValues[noteIndex]:1;
						noteIndex++;
					}
					element.setNoteIndex(noteIndex);
					element.setPeriod(period);
				}
				if ((lastMask[channel]&0x02)!=0 || (lastMask[channel]&0x20)!=0)
				{
					if ((lastMask[channel]&0x02)!=0)
					{
						lastIns[channel] = inputStream.read(); patternDataLength--;
					}
					element.setInstrument(lastIns[channel]);
				}
				if ((lastMask[channel]&0x04)!=0 || (lastMask[channel]&0x40)!=0)
				{
					if ((lastMask[channel]&0x04)!=0)
					{
						int vol = inputStream.read(); patternDataLength--;
						int volCmd=0, volOp=0;

						// 0-64: Set Volume
						if (vol <= 64) { volCmd = 0x01; volOp = vol; } else
						// 128-192: Set Panning
						if ((vol >= 128) && (vol <= 192)) { volCmd = 0x08; volOp = vol - 128; } else
						// 65-74: Fine Volume Up
						if (vol < 75) {volCmd = 0x05; volOp = vol - 65; } else
						// 75-84: Fine Volume Down
						if (vol < 85) { volCmd = 0x04; volOp = vol - 75; } else
						// 85-94: Volume Slide Up
						if (vol < 95) { volCmd = 0x03; volOp = vol - 85; } else
						// 95-104: Volume Slide Down
						if (vol < 105) { volCmd = 0x02; volOp = vol - 95; } else
						// 105-114: Pitch Slide Down
						if (vol < 115) { volCmd = 0x0C; volOp = vol - 105; } else
						// 115-124: Pitch Slide Up
						if (vol < 125) { volCmd = 0x0D; volOp = vol - 115; } else
						// 193-202: Portamento To
						if ((vol >= 193) && (vol <= 202)) { volCmd = 0x0B; volOp = vol - 193; } else
						// 203-212: Vibrato depth
						if ((vol >= 203) && (vol <= 212))
						{ 
							volCmd = 0x07; volOp = vol - 203;
							// Old versions of ModPlug saved this as "set vibrato speed" instead, so let's fix that.
							if (volOp!=0 && lastSavedWithVersion!=-1 && lastSavedWithVersion<=0x01170254)
								volCmd = 0x06;
						}
						// 213-222: was once Velocity (MPT)
						// 223-232: Sample Offset (MPT)
						if ((vol >= 223) && (vol <= 232)) { volCmd = 0x0E; volOp = vol - 223; }
						lastVolCmd[channel] = volCmd;
						lastVolOp[channel] = volOp;
					}
					element.setVolumeEffekt(lastVolCmd[channel]);
					element.setVolumeEffektOp(lastVolOp[channel]);
				}
				if ((lastMask[channel]&0x08)!=0 || (lastMask[channel]&0x80)!=0)
				{
					if ((lastMask[channel]&0x08)!=0)
					{
						lastCmd[channel] = inputStream.read(); patternDataLength--;
						lastData[channel] = inputStream.read(); patternDataLength--;
					}
					element.setEffekt(lastCmd[channel]);
					element.setEffektOp(lastData[channel]);
				}
			}
		}
		
		inputStream.seek(beyondLastSample);
		boolean hasExtraInstrumentInfos = false;
		boolean hasExtraSongProperties = false;
		while (inputStream.getFilePointer() + 8 < inputStream.length())
		{
			final int marker = inputStream.readIntelDWord();
			if (marker == 0x4D505458) // MPTX - ModPlugExtraInstrumentInfo
			{
				inputStream.skipBack(4);
				hasExtraInstrumentInfos = loadExtendedInstrumentProperties(inputStream);
			}
			else
			if (marker == 0x4D505453) // MPTS - ModPlugExtraSongInfo
			{
				inputStream.skipBack(4);
				hasExtraSongProperties = loadExtendedSongProperties(inputStream, false);
				final int maxChannels = getNChannels();
				if (maxChannels > 0) maxChannelIndex = maxChannels-1;
			}
		}
		
		setNChannels((maxChannelIndex<3)?4:maxChannelIndex+1); // minimum 4 channels
		patternContainer.setToChannels(getNChannels());
		patternContainer.setChannelNames(chnNames);
		
		// Correct the songlength for playing, skip markerpattern... (do not want to skip them during playing!)
		cleanUpArrangement();
		
		if (lastSavedWithVersion==-1 && version==0x0888) lastSavedWithVersion = 0x01170000;
		if (lastSavedWithVersion!=-1 && (getTrackerName()==null || getTrackerName().length()==0))
		{
			setTrackerName("OpenMPT " + ModConstants.getModPlugVersionString(lastSavedWithVersion));
			boolean isCompatMode = reserved==0x4F4D5054 && (version & 0xF000)==0x5000;
			if (lastSavedWithVersion==0x01170000)
				isCompatMode = !hasExtraInstrumentInfos && !hasExtraSongProperties;
			
			if (isCompatMode)
			{
				setTrackerName(getTrackerName() + " (compatibility export)");
				// Treat compatibility export as ITs
				setModType(getModType() & ~(ModConstants.MODTYPE_MPT | ModConstants.MODTYPE_OMPT));
			}
			else
				setModType(getModType() | ModConstants.MODTYPE_OMPT);
			
			if (createdWithVersion!=-1) setTrackerName(getTrackerName() + " (first created with " + ModConstants.getModPlugVersionString(createdWithVersion)+")");
		}

		// With OpenModPlug Files we create default channel colors if none are set
		if ((getModType()&(ModConstants.MODTYPE_MPT | ModConstants.MODTYPE_OMPT))!=0 && patternContainer.getChannelColors()==null)
			patternContainer.createMPTMDefaultRainbowColors();

		// avoid devision by zero at calculateSamplesPerTick
		if (rowsPerBeat==0 && tempoMode==ModConstants.TEMPOMODE_MODERN) rowsPerBeat = 1;
	}
}