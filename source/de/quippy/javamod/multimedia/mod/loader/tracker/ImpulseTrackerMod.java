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
import de.quippy.javamod.multimedia.mod.loader.instrument.Instrument;
import de.quippy.javamod.multimedia.mod.loader.instrument.InstrumentsContainer;
import de.quippy.javamod.multimedia.mod.loader.instrument.Sample;
import de.quippy.javamod.multimedia.mod.loader.pattern.Pattern;
import de.quippy.javamod.multimedia.mod.loader.pattern.PatternContainer;
import de.quippy.javamod.multimedia.mod.loader.pattern.PatternElement;
import de.quippy.javamod.multimedia.mod.loader.pattern.PatternRow;
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
 		"it"
 	};
	/**
	 * Will be executed during class load
	 */
	static
	{
		ModuleFactory.registerModule(new ImpulseTrackerMod());
	}

	private int special;
	private int panningSeparation;
	private int [] channelVolume;
	private String songMessage;
	private MidiMacros midiMacros;
	
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
	public int getChannelVolume(int channel)
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

	private void readEnvelopeData(Envelope env, int add, ModfileInputStream inputStream) throws IOException
	{
		long pos = inputStream.getFilePointer();
		
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
		
		inputStream.seek(pos+82L);
	}
	/**
	 * @param inputStream
	 * @return true, if this is an impulse tracker mod, false if this is not clear
	 * @see de.quippy.javamod.multimedia.mod.loader.Module#checkLoadingPossible(de.quippy.javamod.io.ModfileInputStream)
	 */
	@Override
	public boolean checkLoadingPossible(ModfileInputStream inputStream) throws IOException
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
	protected Module getNewInstance(String fileName)
	{
		return new ImpulseTrackerMod(fileName);
	}
	/**
	 * @param inputStream
	 * @return
	 * @see de.quippy.javamod.multimedia.mod.loader.Module#loadModFile(java.io.DataInputStream)
	 */
	@Override
	public void loadModFileInternal(ModfileInputStream inputStream) throws IOException
	{
		setModType(ModConstants.MODTYPE_IT);
		setSongRestart(0);

		// IT-ID:
		setModID(inputStream.readString(4));
		if (!getModID().equals("IMPM")) throw new IOException("Unsupported IT Module!");

		// Songname
		setSongName(inputStream.readString(26));
		
		//PHiliht = Pattern row highlight information. Only relevant for pattern editing situations.
		inputStream.skip(2);
		
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
		final int cmwt = inputStream.readIntelUnsignedWord();

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
		if ((flags & 0x01)!=0)		songFlags |= ModConstants.SONG_ISSTEREO;
		if ((flags & 0x08)!=0) 		songFlags |= ModConstants.SONG_LINEARSLIDES;
		if ((flags & 0x10)!=0) 		songFlags |= ModConstants.SONG_ITOLDEFFECTS;
		if ((flags & 0x20)!=0) 		songFlags |= ModConstants.SONG_ITCOMPATMODE;
		if ((flags & 0x80)!=0) 		songFlags |= ModConstants.SONG_EMBEDMIDICFG;
		if ((flags & 0x1000)!=0) 	songFlags |= ModConstants.SONG_EXFILTERRANGE;

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
		//final boolean hasEditHistory = ((special&0x02)!=0); // we do not want to know for playback
		final boolean hasSpecialData = ((special&0x04)!=0); // this is special data - read a int and than int-times 8 bytes 
		final boolean hasMidiMacros = ((flags&0x80)!=0) || ((special&0x08)!=0);
		
		//GV:       Global volume. (0->128) All volumes are adjusted by this
		int headerGlobalVolume = inputStream.read();
		if (headerGlobalVolume==0 || headerGlobalVolume > ModConstants.MAXGLOBALVOLUME) headerGlobalVolume = ModConstants.MAXGLOBALVOLUME;
		setBaseVolume(headerGlobalVolume);
		//MV:       Mix volume (0->128) During mixing, this value controls the magnitude of the wave being mixed.
		headerGlobalVolume = inputStream.read()&0x7F;
		if (headerGlobalVolume==0 || headerGlobalVolume > ModConstants.MAXGLOBALVOLUME) headerGlobalVolume = ModConstants.MAXGLOBALVOLUME;
		setMixingPreAmp(headerGlobalVolume);
		//IS:       Initial Speed of song.
		setTempo(inputStream.read());
		//IT:       Initial Tempo of song
		setBPMSpeed(inputStream.read());
		//Sep:      Panning separation between channels (0->128, 128 is max sep.)
		panningSeparation = inputStream.read();
		//PWD:      Pitch wheel depth for MIDI controllers
		final int pwd = inputStream.read();
		
		// We read the Message later...
		//MsgLgth
		final int msgLength = inputStream.readIntelUnsignedWord();
		//MsgOffset
		final int msgPointer = inputStream.readIntelDWord();
		//4 Byte reserved
		String reserved = inputStream.readString(4);

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
			if (panValue==0xFF) hasFFPanningValue = true; // needed for sanity check for MPT-Version
			
			if (panValue==100 || (panValue & 0x80)!=0)
			{
				// we simply store those, as mixer responds to these
				// in "initializeMixer()"
				panningValue[i] = panValue<<2;
			}
			else
			{
				panValue = (panValue&0x7F)<<2;
				if (panValue>256) panValue = 256;
				panningValue[i]=panValue; 
			}
		}
		
		// Checking for
		if ((version & 0xF000)==0x5000)
		{
			setTrackerName("ModPlug Tracker " + ModConstants.getAsHex((version>>8)&0xF, 1) + "." + ModConstants.getAsHex(version&0xFF, 2));
			if (reserved.equals("OMPT")) 
				setModType(ModConstants.MODTYPE_MPT);
			else
				setModType(ModConstants.MODTYPE_OMPT);
		}
		else
		if (version == 0x0888 || cmwt==0x888)
		{
			setTrackerName("OpenMPT 1.17.02.26-1.18");
			version=0x5117;
			setModType(ModConstants.MODTYPE_MPT);
		}
		else
		if (version == 0x0217 && cmwt==0x200 && reserved.length()==0)
		{
			if (hasFFPanningValue)
			{
				version=0x5109;
				setTrackerName("ModPlug Tracker 1.09 - 1.16");
			}
			else
			{
				version=0x5117;
				setTrackerName("OpenMPT 1.17 (compatibility export)");
			}
			setModType(ModConstants.MODTYPE_MPT);
		}
		else
		if (version == 0x0214 && cmwt==0x202 && reserved.length()==0)
		{
			version=0x5109;
			setTrackerName("ModPlug Tracker b3.3 - 1.09");
			setModType(ModConstants.MODTYPE_MPT);
		}
		else
		if (version == 0x0300 && cmwt == 0x0300 && reserved.length()==0 && getSongLength()==256 && panningSeparation == 128 && pwd == 0)
		{
			version=0x5117;
			setTrackerName("ModPlug Tracker 1.17.02.20-25");
			setModType(ModConstants.MODTYPE_MPT);
		}
		else
		{
			setTrackerName("Impulse Tracker V" + ModConstants.getAsHex((version>>8)&0xF, 1) + "." + ModConstants.getAsHex(version&0xFF, 2) + " (CmwT: " + ModConstants.getAsHex((cmwt>>8)&0xF, 1) + "." + ModConstants.getAsHex(cmwt&0xFF, 2) + ")");
		}

		//Chnl Vol: Volume for each channel. Ranges from 0->64
		channelVolume = new int[64];
		for (int i=0; i<64; i++)
		{
			int vol = inputStream.read();
			if (vol<0) vol=0; else if (vol>64) vol=64;
			channelVolume[i]=vol;
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
		final int [] instrumentParaPointer = new int [getNInstruments()];
		for (int i=0; i<getNInstruments(); i++) instrumentParaPointer[i] = inputStream.readIntelDWord();
		final int [] samplesParaPointer = new int [getNSamples()];
		for (int i=0; i<getNSamples(); i++) samplesParaPointer[i] = inputStream.readIntelDWord();
		final int [] patternParaPointer = new int [getNPattern()];
		for (int i=0; i<getNPattern(); i++) patternParaPointer[i] = inputStream.readIntelDWord();
		
		// Reading IT Extra Info
		if (hasSpecialData && inputStream.getFilePointer() + 2 < inputStream.getLength())
		{
			long nflt = (long)inputStream.readIntelUnsignedWord();
			long skipMe = nflt<<3;
			if (inputStream.getFilePointer() + skipMe < inputStream.getLength()) inputStream.skip(skipMe);
		}

		midiMacros = new MidiMacros();
		// read the MidiMacros
		if (hasMidiMacros && inputStream.getFilePointer() + MidiMacros.SIZE_OF_SCTUCT < inputStream.getLength())
		{
			midiMacros.loadFrom(inputStream);
			if (cmwt < 0x0214) // clear midi macros
				midiMacros.clearZxxMacros();
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
			
			Instrument currentIns = new Instrument();
			currentIns.setDosFileName(inputStream.readString(13));

			Envelope volumeEnvelope = new Envelope();
			currentIns.setVolumeEnvelope(volumeEnvelope);
			Envelope panningEnvelope = new Envelope();
			currentIns.setPanningEnvelope(panningEnvelope);
			Envelope pitchEnvelope = new Envelope();
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
				inputStream.skip(4);
				currentIns.setGlobalVolume(128);
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
				if ((panning&0x80)==0)
				{
					panning = (panning&0x7F)<<2;
					if (panning>256) panning=256;
				}
				else
					panning = -1;
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
				int [] volumeEnvelopePosition = new int[25];
				int [] volumeEnvelopeValue = new int[25];
				int nPoints = 0;
				for (; nPoints<25; nPoints++)
				{
					volumeEnvelopePosition[nPoints] = inputStream.read();
					volumeEnvelopeValue[nPoints] = inputStream.read();
				}
				volumeEnvelope.setNPoints(nPoints);
				volumeEnvelope.setPositions(volumeEnvelopePosition);
				volumeEnvelope.setValue(volumeEnvelopeValue);
			}
			else
			{
				readEnvelopeData(volumeEnvelope, 0, inputStream); // 0..64
				readEnvelopeData(panningEnvelope, 32, inputStream); //-32..+32
				readEnvelopeData(pitchEnvelope, 32, inputStream); //-32..+32
			}
			
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
			if ((panning&0x80)!=0) 
			{
				panning=(panning&0x7F)<<2;
				if (panning>256) panning=256;
			}
			else
				panning=-1;
			currentSample.setPanning(panning);
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
				if (CvT==0xFF && (flags&0x02)!=0x02)
				{
					throw new IOException("ADPCM not supported");
				}
				else
				{
					loadFlag = ((CvT&0x1)==0x1)?ModConstants.SM_PCMS:ModConstants.SM_PCMU;
					if ((flags&0x2)==0x2) loadFlag |= ModConstants.SM_16BIT;
					if ((flags&0x4)==0x4) loadFlag |= ModConstants.SM_STEREO;
					if ((flags&0x8)==0x8) loadFlag |= (version>=0x215 && (CvT&0x4)==0x4)?ModConstants.SM_IT2158:ModConstants.SM_IT2148;
				}
				currentSample.setStereo((loadFlag&ModConstants.SM_STEREO)!=0);
				inputStream.seek(sampleOffset);
				currentSample.setSampleType(loadFlag);
				readSampleData(currentSample, inputStream);
			}
			
			instrumentContainer.setSample(i, currentSample);
		}

		PatternContainer patternContainer = new PatternContainer(getNPattern());
		setPatternContainer(patternContainer);
		int maxChannels = 0;
		for (int pattNum=0; pattNum<getNPattern(); pattNum++)
		{
			inputStream.seek(patternParaPointer[pattNum]);
			
			int patternDataLength = inputStream.readIntelUnsignedWord();
			int rows = inputStream.readIntelUnsignedWord();
			
			inputStream.skip(4); // RESERVED
			
			// First, clear them:
			patternContainer.setPattern(pattNum, new Pattern(rows));
			for (int row=0; row<rows; row++)
			{
				patternContainer.setPatternRow(pattNum, row, new PatternRow(64));
				for (int channel=0; channel<64; channel++)
				{
					PatternElement currentElement = new PatternElement(pattNum, row, channel);
					patternContainer.setPatternElement(currentElement);
				}
			}
			int row = 0;
			int [] lastMask= new int[64];
			int [] lastNote= new int[64];
			int [] lastIns= new int[64];
			int [] lastVolCmd = new int[64];
			int [] lastVolOp = new int[64];
			int [] lastCmd= new int[64];
			int [] lastData= new int[64];
			while (patternDataLength>0) 
			{
				int channelByte = inputStream.read(); patternDataLength--;
				if (channelByte==0)
				{
					row++;
					continue;
				}
				int channel = (channelByte - 1) & 0x3F;
				if (channel>maxChannels) maxChannels = channel;
				PatternElement element = patternContainer.getPatternElement(pattNum, row, channel);
				
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
					else
					if (noteIndex>119) // per definition: 119<noteindex<0xFE is note_fade
					{
						noteIndex = period = ModConstants.NOTE_FADE;
					}
					else
					{
						if (noteIndex<ModConstants.noteValues.length) period = ModConstants.noteValues[noteIndex]; else period = 1;
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
						if ((vol >= 203) && (vol <= 212)) { volCmd = 0x07; volOp = vol - 203; }
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
		if (maxChannels<4) maxChannels=4;
		setNChannels(maxChannels+1);
		
		// Correct the songlength for playing, skip markerpattern... (do not want to skip them during playing!)
		int realLen = 0;
		for (int i=0; i<getSongLength(); i++)
		{
			if (getArrangement()[i]==255) // end of Song:
				break;
			else
			if (getArrangement()[i]<254 && getArrangement()[i]<getNPattern())
				getArrangement()[realLen++]=getArrangement()[i];
		}
		setSongLength(realLen);
		cleanUpArrangement();
	}
}