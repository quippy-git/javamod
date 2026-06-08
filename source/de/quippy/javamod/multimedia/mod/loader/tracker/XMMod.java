/*
 * @(#) XMMod.java
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
import de.quippy.javamod.system.Helpers;

/**
 * @author Daniel Becker
 * @since 26.05.2006
 */
public class XMMod extends ProTrackerMod
{
	private static final String[] MODFILEEXTENSION = new String[]
   	{
   		"xm"
   	};
	private static final int XM_HEADER_SIZE = 276;
	private static final int INSTR_HEADER_SIZE = 263;
	private static final int SAMPLE_HEADER_SIZE = 40;

	// OMPT and MPT also saved XMs - and we want to find out:
	private static final int verUnknown         =  0x00;  // Probably not made with MPT
	private static final int verOldModPlug      =  0x01;  // Made with MPT Alpha / Beta
	private static final int verNewModPlug      =  0x02;  // Made with MPT (not Alpha / Beta)
	private static final int verModPlugBidiFlag =  0x04;  // MPT up to v1.11 sets both normal loop and pingpong loop flags
	private static final int verOpenMPT         =  0x08;  // Made with OpenMPT
	private static final int verConfirmed       =  0x10;  // We are very sure that we found the correct tracker version.

	private static final int verFT2Generic      =  0x20;  // "FastTracker v2.00", but FastTracker has NOT been ruled out
//	private static final int verOther           =  0x40;  // Something we don't know, testing for DigiTrakker.
	private static final int verFT2Clone        =  0x80;  // NOT FT2: itype changed between instruments, or \0 found in song title
	private static final int verPlayerPRO       = 0x100;  // Could be PlayerPRO
	private static final int verDigiTrakker     = 0x200;  // Probably DigiTrakker
//	private static final int verUNMO3           = 0x400;  // TODO: UNMO3-ed XMs are detected as MPT 1.16
	private static final int verEmptyOrders     = 0x800;  // Allow empty order list like in OpenMPT (FT2 just plays pattern 0 if the order list is empty according to the header)
	
	/**
	 * Will be executed during class load
	 */
	static
	{
		ModuleFactory.registerModule(new XMMod());
	}

	private int version;
	private int flag;
	private String songMessage;
	private MidiMacros midiMacros;

	/**
	 * Constructor for XMMod
	 */
	public XMMod()
	{
		super();
	}
	/**
	 * Constructor for XMMod
	 * @param fileExtension
	 */
	protected XMMod(final String fileName)
	{
		super(fileName);
	}
	/**
	 * @return the file extensions this loader is suitable for
	 * @see de.quippy.javamod.multimedia.mod.loader.Module#getFileExtensionList()
	 */
	@Override
	public String[] getFileExtensionList()
	{
		return MODFILEEXTENSION;
	}
	/**
	 * @param channel
	 * @return
	 * @see de.quippy.javamod.multimedia.mod.loader.Module#getPanningValue(int)
	 */
	@Override
	public int getPanningValue(final int channel)
	{
		return ModConstants.PANNING_CENTER;
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.mod.loader.Module#getFrequencyTable()
	 */
	@Override
	public int getFrequencyTable()
	{
		return ((songFlags & ModConstants.SONG_LINEARSLIDES)!=0)?ModConstants.XM_LINEAR_TABLE:ModConstants.XM_AMIGA_TABLE;
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
	 * @return
	 * @see de.quippy.javamod.multimedia.mod.loader.Module#getSongMessage()
	 */
	@Override
	public String getSongMessage()
	{
		return songMessage;
	}
	/**
	 * @return always true for XMs
	 * @see de.quippy.javamod.multimedia.mod.loader.tracker.ProTrackerMod#getFT2Tremolo()
	 */
	@Override
	public boolean getFT2Tremolo()
	{
		return true;
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
	 * @param inputStream
	 * @return true, if this is a FastTracker mod, false if this is not clear
	 * @see de.quippy.javamod.multimedia.mod.loader.Module#checkLoadingPossible(de.quippy.javamod.io.ModfileInputStream)
	 */
	@Override
	public boolean checkLoadingPossible(final ModfileInputStream inputStream) throws IOException
	{
		final String xmID = inputStream.readString(17);
		inputStream.seek(0);
		return isXMMod(xmID);
	}
	/**
	 * @param fileName
	 * @return
	 * @see de.quippy.javamod.multimedia.mod.loader.Module#getNewInstance(java.lang.String)
	 */
	@Override
	protected Module getNewInstance(final String fileName)
	{
		return new XMMod(fileName);
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.mod.loader.Module#supportsAmigaFilter()
	 */
	@Override
	public boolean supportsAmigaFilter()
	{
		return false;
	}
	/**
	 * @since 26.05.2006
	 * @param currentElement
	 * @param inputStream
	 * @throws IOException
	 */
	private void setIntoPatternElement(final ModfileInputStream inputStream, final PatternElement currentElement) throws IOException
	{
		int flags = inputStream.read();
		if ((flags&0x80) == 0) // is not packed
		{
			flags = 0xFF; // read all
			inputStream.skipBack(1); // and push back the note
		}
		int noteIndex		= ((flags&0x01)!=0)?inputStream.read():0;
		int instrument		= ((flags&0x02)!=0)?inputStream.read():0;
		final int volume	= ((flags&0x04)!=0)?inputStream.read():0;
		final int effect	= ((flags&0x08)!=0)?inputStream.read():0;
		final int effectOp	= ((flags&0x10)!=0)?inputStream.read():0;

		// sanitize all
		if (noteIndex==97) // Key Off!
			noteIndex = ModConstants.KEY_OFF;
		else
		if (noteIndex<0 || noteIndex>97)
			noteIndex = ModConstants.NO_NOTE;
		currentElement.setNoteIndex(noteIndex);
		currentElement.setPeriod((noteIndex==ModConstants.NO_NOTE)?0:
									(noteIndex==ModConstants.KEY_OFF)?ModConstants.KEY_OFF:
										ModConstants.noteValues[noteIndex - 1]);

		if (instrument==0xFF) instrument = 0;
		currentElement.setInstrument(instrument);

		if (volume!=0)
		{
			if (volume>=0x10 && volume<=0x50)
			{
				currentElement.setVolumeEffekt(1);
				currentElement.setVolumeEffektOp(volume-0x10);
			}
			else
			{
				currentElement.setVolumeEffekt((volume>>4)-0x4);
				currentElement.setVolumeEffektOp(volume&0x0F);
			}
		}

		currentElement.setEffekt(effect);
		currentElement.setEffektOp(effectOp);
	}
	/**
	 * To support Versions below 0104 we need a separate method to load at a
	 * different place.
	 * @since 23.01.2024
	 * @param inputStream
	 * @throws IOException
	 */
	private void readXMPattern(final ModfileInputStream inputStream) throws IOException
	{
		final PatternContainer patternContainer = new PatternContainer(this, getNPattern());
		for (int pattNum=0; pattNum<getNPattern(); pattNum++)
		{
			final long LSEEK = inputStream.getFilePointer();
			final int patternHeaderSize = inputStream.readIntelDWord();

//			We ignore the packing type - as everybody does...
			inputStream.skip(1);

			int rows = (version==0x0102)?inputStream.read()+1:inputStream.readIntelUnsignedWord();
			if (rows==0)
				rows=64;
			else
				if (rows>4096) rows = 4096;
//			{
//				final int MaxPatternSize = ((getModType()&(ModConstants.MODTYPE_MPT|ModConstants.MODTYPE_OMPT))!=0)?1024:256;
//				if (rows>MaxPatternSize) rows = MaxPatternSize;
//			}

			final int packedPatternDataSize = inputStream.readIntelUnsignedWord();
			if (packedPatternDataSize==0)
			{
				patternContainer.createPattern(pattNum, rows, getNChannels());
				for (int row=0; row<rows; row++)
				{
					for (int channel=0; channel<getNChannels(); channel++)
					{
						patternContainer.createPatternElement(pattNum, row, channel);
					}
				}
				continue;
			}

			inputStream.seek(LSEEK + patternHeaderSize);

			// Stop reading, if either end of file or packed pattern size is reached
			long endPos = inputStream.getFilePointer() +  packedPatternDataSize;
			if (endPos > inputStream.length()) endPos = inputStream.length();

			patternContainer.createPattern(pattNum, rows);
			for (int row=0; row<rows; row++)
			{
				patternContainer.createPatternRow(pattNum, row, getNChannels());
				for (int channel=0; channel<getNChannels(); channel++)
				{
					final PatternElement currentElement = patternContainer.createPatternElement(pattNum, row, channel);
					if (inputStream.getFilePointer()<endPos) setIntoPatternElement(inputStream, currentElement);
				}
			}
			// With some corrupted XMs with flipped bits we will not load all pattern data.
			// Most XM loaders load the compressed pattern data into a separate buffer, we don't
			// so we need to seek...
			if (inputStream.getFilePointer()!=endPos)
			{
//				final long dif = endPos - inputStream.getFilePointer();
//				Log.info("Read not enough bytes (" + dif + ") in pattern " + pattNum);
				setTrackerName(getTrackerName() + " (corrupt!)");
				inputStream.seek(endPos);
			}
		}
		setPatternContainer(patternContainer);
	}
	/**
	 * @since 23.01.2024
	 * @param inputStream
	 * @param instrumentContainer
	 * @param anzSamples
	 * @param sampleOffsetIndex
	 * @param sampleLoadingFlags
	 * @throws IOException
	 */
	private void readXMSampleData(final ModfileInputStream inputStream, final InstrumentsContainer instrumentContainer, final int anzSamples, final int sampleOffsetIndex) throws IOException
	{
		for (int samIndex=0; samIndex<anzSamples; samIndex++)
		{
			final Sample current = instrumentContainer.getSample(samIndex + sampleOffsetIndex);
			// XMs can have 16bit samples with an uneven amount of bytes. Even though I have no idea why that is:
			// as we convert to "amount of samples to read", that extra byte is not read nor skipped.
			// Therefore lets seek at the end of sample data.
			final long filePointer = inputStream.getFilePointer();
			readSampleData(current, inputStream);
			inputStream.seek(filePointer + current.byteLength);
		}
	}
	/**
	 * Get the ModType
	 * @param kennung
	 * @return
	 */
	private boolean isXMMod(final String id)
	{
		if (id.equalsIgnoreCase("Extended Module: ")) return true;
		return false;
	}
	/**
	 * This is only for some fun - to be honest. The C4-Period is never used, just displayed.
	 * @since 26.07.2024
	 * @param sample
	 * @param useTable
	 * @return
	 */
	private int getPeriod2Hz(final Sample sample, final int useTable)
	{
		if (sample==null) return -1;

		final int note = (4*12) + sample.transpose;
		if ((note<0) || (note>=(10*12)-1)) return -1;
		final int C4Period = ((note<<4) + ((sample.fineTune>>3) + 16));

		switch (useTable)
		{
			case ModConstants.XM_AMIGA_TABLE:
				return (ModConstants.BASEFREQUENCY * 1712) / (ModConstants.FT2_amigaPeriods[C4Period]&0xFFFF);
			case ModConstants.XM_LINEAR_TABLE:
				final int period = ModConstants.FT2_linearPeriods[C4Period]&0xFFFF;
				// Original FT2 method with doubles - is a bit more precise in rounding
//				final int invPeriodDouble = ((12 * 192 * 4) - C4Period) & 0xFFFF; // 12 octaves * (12 * 16 * 4) LUT entries = 9216, add 767 for rounding
//				final int quotientDouble  = invPeriodDouble / (12 * 16 * 4);
//				final int remainderDouble = invPeriodDouble % (12 * 16 * 4);
//				final double logValue = (ModConstants.BASEFREQUENCY * 256d) * Math.pow(2d, (double)remainderDouble / (4d * 12d * 16d));
//				final double frequencyDouble = logValue * (1d /Math.pow(2d, (double)((14 - quotientDouble) & 0x1F)));

				final int invPeriod = ((12 * 192 * 4) + 767 - period) & 0xFFFF; // 12 octaves * (12 * 16 * 4) LUT entries = 9216, add 767 for rounding
				final int quotient  = invPeriod / (12 * 16 * 4);
				final int remainder = period % (12 * 16 * 4);
				return ModConstants.lintab[remainder] >> (((14 - quotient) & 0x1F)-2); // values are 4 times bigger in FT2
		}
		return -1;
	}
	/**
	 * @param inputStream
	 * @return
	 * @see de.quippy.javamod.multimedia.mod.loader.Module#loadModFile(java.io.DataInputStream)
	 */
	@Override
	protected void loadModFileInternal(final ModfileInputStream inputStream) throws IOException
	{
		setBaseVolume(ModConstants.MAXGLOBALVOLUME);
		setMixingPreAmp(ModConstants.MIN_MIXING_PREAMP);

		// XM-ID:
		setModID(inputStream.readString(17));
		if (!isXMMod(getModID())) throw new IOException("Unsupported XM Module!");

		// Songname
		final byte[] songNameBuffer = new byte[20];
		final int read = inputStream.read(songNameBuffer, 0, 20);
		setSongName(Helpers.retrieveAsString(songNameBuffer, 0, read));
		
		// 0x1A:
		inputStream.skip(1);

		// Trackername
		final String trackerName = inputStream.readString(20);

		// Version
		version = inputStream.readIntelUnsignedWord();

		long LSEEK = inputStream.getFilePointer();

		// Header Size
		int headerSize = inputStream.readIntelDWord();

		setModType(ModConstants.MODTYPE_XM);
		setTrackerName(trackerName.trim());

		// OrderNum:
		setSongLength(inputStream.readIntelUnsignedWord());

		//SongRestart
		final int songRestart = inputStream.readIntelUnsignedWord();
		setSongRestart((songRestart > getSongLength())?0:songRestart);

		// NChannels
		setNChannels(inputStream.readIntelUnsignedWord());

		// NPattern
		setNPattern(inputStream.readIntelUnsignedWord());

		// Instruments
		setNInstruments(inputStream.readIntelUnsignedWord());

		// a Flag
		flag = inputStream.readIntelUnsignedWord();
		if ((flag & 0x0001)!=0) songFlags |= ModConstants.SONG_LINEARSLIDES;
		if ((flag & 0x1000)!=0) songFlags |= ModConstants.SONG_EXFILTERRANGE;
		songFlags |= ModConstants.SONG_ISSTEREO;

		// Tempo
		setTempo(inputStream.readIntelUnsignedWord());

		// BPMSpeed
		setBPMSpeed(inputStream.readIntelUnsignedWord());

		// always space for 256 pattern...
		allocArrangement(256);
		final int[] arrangement = getArrangement();
		for (int i=0; i<256; i++) arrangement[i]=inputStream.read();

		inputStream.seek(LSEEK + headerSize);

		// lets start with some version / tracker guessing
		// Version detection stuff:
		boolean instrumentWithSamplesEncountered = false;
		boolean anyADPCM = false;
		int sampleReserved = 0;
		int lastSampleHeaderSize = -1;
		int lastInstrumentType = -1;
		int lastSampleReserved = -1;
		String madeWithTracker = null;
		int madeWith = verUnknown;
		//boolean isMadTracker = false; // only used to identify the encoding
		if (trackerName.startsWith("FastTracker v2.00   ") && headerSize==XM_HEADER_SIZE)
		{
			int firstZero=-1;
			int space=-1;
			final int songNameSize = songNameBuffer.length;
			for (int i=0; i<songNameSize; i++)
			{
				if (songNameBuffer[i]==0 && firstZero==-1)
					firstZero=i;
				if (songNameBuffer[i]==0x20 && firstZero!=-1 && space==-1)
					space=i;
			}

			if (version < 0x0104)
			{
				madeWith = verFT2Generic | verConfirmed;
			}
			else
			if (firstZero!=-1)
			{
				// FT2 pads the song title with spaces, some other trackers use null chars
				// PlayerPRO fills the remaining buffer after the null terminator with space characters.
				// PlayerPRO does not support song restart position.
				if (songRestart!=0)
					madeWith = verFT2Clone | verNewModPlug | verEmptyOrders;
				else
				if(firstZero==songNameSize-1)
					madeWith = verFT2Clone | verNewModPlug | verPlayerPRO | verEmptyOrders;
				else
				if (space!=-1)
					madeWith = verPlayerPRO | verConfirmed;
				else
					madeWith = verFT2Clone | verNewModPlug | verEmptyOrders;
			}
			else
			{
				if (songRestart!=0)
					madeWith = verFT2Generic | verNewModPlug;
				else
					madeWith = verFT2Generic | verNewModPlug | verPlayerPRO;
			}
		}
		else
		if (trackerName.equals("FastTracker v 2.00  "))
		{
			// MPT 1.0 (exact version to be determined later)
			madeWith = verOldModPlug;
		}
		else
		{
			// Something else!
			madeWith = verUnknown | verConfirmed;

			//madeWithTracker = mpt::ToUnicode(mpt::Charset::CP437, mpt::String::ReadBuf(mpt::String::spacePadded, fileHeader.trackerName));

			if (trackerName.startsWith("OpenMPT"))
			{
				madeWith = verOpenMPT | verConfirmed | verEmptyOrders;
			}
			else
			if (trackerName.startsWith("MilkyTracker "))
			{
				madeWithTracker = trackerName;
//				// MilkyTracker prior to version 0.90.87 doesn't set a version string.
//				// Luckily, starting with v0.90.87, MilkyTracker also implements the FT2 panning scheme.
//				if (trackerName.endsWith("        "))
//				{
//					m_nMixLevels = MixLevels::CompatibleFT2;
//				}
			}
			else
			if (trackerName.startsWith("Fasttracker II clone"))
			{
				// 8bitbubsy's FT2 clone should be treated exactly like FT2
				madeWith = verFT2Generic | verConfirmed;
			}
			else
			if (trackerName.equals("MadTracker 2.0")) // ends with a zero '\x0'
			{
				madeWithTracker = trackerName;
//				// Fix channel 2 in m3_cha.xm
//				m_playBehaviour.reset(kFT2PortaNoNote);
//				// Fix arpeggios in kragle_-_happy_day.xm
//				m_playBehaviour.reset(kFT2Arpeggio);
//				isMadTracker = true;
			}
			else
			if (trackerName.equals("Skale Tracker") || trackerName.equals("Sk@le Tracker"))
			{
				madeWithTracker = trackerName;
//				m_playBehaviour.reset(kFT2ST3OffsetOutOfRange);
//				// Fix arpeggios in KAPTENFL.XM
//				m_playBehaviour.reset(kFT2Arpeggio);
			}
			else
			if (trackerName.startsWith("*Converted "))
			{
				madeWithTracker = "DigiTrakker";
				madeWith = verDigiTrakker;
			}
		}
		if ((songFlags&ModConstants.SONG_EXFILTERRANGE)!=0 && (madeWith & verNewModPlug)!=0)
			madeWith = verFT2Clone | verNewModPlug | verConfirmed | verEmptyOrders;

		// Read the patternData
		if (version>=0x0104) readXMPattern(inputStream);

		final InstrumentsContainer instrumentContainer = new InstrumentsContainer(this, getNInstruments(), 0);
		setInstrumentContainer(instrumentContainer);

		int sampleOffsetIndex = 0;
		// Read the instrument data
		for (int ins=0; ins<getNInstruments(); ins++)
		{
			int vibratoType = 0;
			int vibratoSweep = 0;
			int vibratoDepth = 0;
			int vibratoRate = 0;

			LSEEK = inputStream.getFilePointer();

			final Instrument currentIns = new Instrument();

			 // Default for values from IT
			currentIns.globalVolume = 128;
			currentIns.setPanning = false;
			currentIns.defaultPanning = 128;
			currentIns.pitchPanSeparation = -1;
			currentIns.NNA = -1;
			currentIns.initialFilterCutoff = 0;
			currentIns.initialFilterResonance = 0;
			currentIns.randomPanningVariation = -1;

			int instrumentHeaderSize = inputStream.readIntelDWord();
			final int readInstrumentHeaderSize = instrumentHeaderSize; // nead the pure size for version guessing
			if (instrumentHeaderSize==0) instrumentHeaderSize = INSTR_HEADER_SIZE;
			if (instrumentHeaderSize<0) continue;
			
			// Read the instrument header
			// In C we would now read as many bytes into a struct, as are presented here
			// and if it's less than the struct has place, the rest simply stays zero/uninitialized
			currentIns.name = inputStream.readString(22);
			final int insType = inputStream.read();
			int anzSamples = inputStream.readIntelWord();
			if (anzSamples<0) anzSamples=0;

			setNSamples(getNSamples()+anzSamples);

			int sampleHeaderSize = inputStream.readIntelDWord();
			final int readSampleHeaderSize = sampleHeaderSize; // nead the pure size for version guessing
			if (sampleHeaderSize<=0 || sampleHeaderSize>SAMPLE_HEADER_SIZE) sampleHeaderSize = SAMPLE_HEADER_SIZE;
			
			// A headersize of 33 means, only header, no instrument data
			// However, some values need to be initialized to avoid NullPointer Exceptions
			final Envelope volumeEnvelope = new Envelope(EnvelopeType.volume);
			final Envelope panningEnvelope = new Envelope(EnvelopeType.panning);
			if (instrumentHeaderSize>33)
			{
				currentIns.sampleIndex = new int[96];
				currentIns.noteIndex = new int[96];
				for (int i=0; i<96; i++)
				{
					final int sampleIndex = inputStream.read();
					if (sampleIndex < anzSamples) // if this instrument has no samples associated, sampleIndex=0, anzSamples=0
						currentIns.sampleIndex[i] = sampleIndex + sampleOffsetIndex + 1;
					else
						currentIns.sampleIndex[i] = 0;
					currentIns.noteIndex[i] = i;
				}
	
				final int[] volumeEnvelopePosition = new int[12];
				final int[] volumeEnvelopeValue = new int[12];
				for (int i=0; i<12; i++)
				{
					volumeEnvelopePosition[i] = inputStream.readIntelUnsignedWord();
					volumeEnvelopeValue[i] = inputStream.readIntelUnsignedWord();
				}
				volumeEnvelope.positions = volumeEnvelopePosition;
				volumeEnvelope.value = volumeEnvelopeValue;
				currentIns.volumeEnvelope = volumeEnvelope;
	
				final int[] panningEnvelopePosition = new int[12];
				final int[] panningEnvelopeValue = new int[12];
				for (int i=0; i<12; i++)
				{
					panningEnvelopePosition[i] = inputStream.readIntelUnsignedWord();
					panningEnvelopeValue[i] = inputStream.readIntelUnsignedWord();
				}
				panningEnvelope.positions = panningEnvelopePosition;
				panningEnvelope.value = panningEnvelopeValue;
				currentIns.panningEnvelope = panningEnvelope;
	
				volumeEnvelope.setNumberOfPoints(inputStream.read());
				panningEnvelope.setNumberOfPoints(inputStream.read());
	
				volumeEnvelope.setSustainPoint_XM(inputStream.read());
				volumeEnvelope.loopStartPoint = inputStream.read();
				volumeEnvelope.loopEndPoint = inputStream.read();
	
				panningEnvelope.setSustainPoint_XM(inputStream.read());
				panningEnvelope.loopStartPoint = inputStream.read();
				panningEnvelope.loopEndPoint = inputStream.read();
	
				volumeEnvelope.setXMType(inputStream.read());
				panningEnvelope.setXMType(inputStream.read());
	
				volumeEnvelope.sanitize(64);
				panningEnvelope.sanitize(64);
	
				vibratoType = inputStream.read();
				vibratoSweep = inputStream.read();
				vibratoDepth = inputStream.read();
				vibratoRate = inputStream.read();
	
				currentIns.volumeFadeOut = inputStream.readIntelUnsignedWord();
				
				// most of my doku says, 2 bytes follow. Only one says 22 bytes follow
	
				// Read Midi Data
				currentIns.xm_enableMidi = inputStream.read()>0;			// MIDI Out Enabled (0 / 1)
				currentIns.midiChannel = inputStream.read();				// MIDI Channel (0...15)
				currentIns.midiProgram = inputStream.readIntelWord();		// MIDI Program (0...127)
				currentIns.pitchWheelDepth = inputStream.readIntelWord();	// MIDI Pitch Wheel Range (0...36 halftones)
				currentIns.xm_muteComputer = inputStream.read()>0;			// Mute instrument if MIDI is enabled (0 / 1)
				// sanitize if midi is enabled
				if (currentIns.xm_enableMidi)
				{
					currentIns.midiChannel++;
					if (currentIns.midiChannel<1) currentIns.midiChannel = 1;
					else
					if (currentIns.midiChannel>16) currentIns.midiChannel = 16;
					
					currentIns.midiProgram++;
					if (currentIns.midiProgram<1) currentIns.midiProgram = 1;
					else
					if (currentIns.midiProgram>128) currentIns.midiProgram = 128;
				}
				// save once if instrument has valid midi data and midi output
				currentIns.hasValidMidiData = (currentIns.hasValidMidiChannel() && currentIns.hasValidMidiProgram() && anzSamples==0);
			}				
			// At this point 15 bytes of junk follows - we ignore that by
			inputStream.seek(LSEEK+=instrumentHeaderSize);

			// Time for some version detection stuff.
			if ((madeWith&verOldModPlug)!=0)
			{
				madeWith |= verConfirmed;
				if (readInstrumentHeaderSize==245)
				{
					// ModPlug Tracker Alpha
					madeWithTracker = "ModPlug Tracker 1.0 alpha";
					lastSavedWithVersion = 0x10000A5;
				}
				else
				if (readInstrumentHeaderSize==INSTR_HEADER_SIZE)
				{
					// ModPlug Tracker Beta (Beta 1 still behaves like Alpha, but Beta 3.3 does it this way)
					madeWithTracker = "ModPlug Tracker 1.0 beta";
					lastSavedWithVersion = 0x10000B3;
				}
				else
				{
					// WTF?
					madeWith = (verUnknown | verConfirmed);
				}
			}
			else
			if (anzSamples==0)
			{
				// Empty instruments make tracker identification pretty easy!
				if (readInstrumentHeaderSize==INSTR_HEADER_SIZE && readSampleHeaderSize==0 && (madeWith&verNewModPlug)!=0)
					madeWith |= verConfirmed;
				else
				if(readInstrumentHeaderSize != 29 && (madeWith&verDigiTrakker)!=0)
					madeWith &= ~verDigiTrakker;
				else
				if ((madeWith&(verFT2Clone | verFT2Generic))!=0 && readInstrumentHeaderSize!=33)
				{
					// Sure isn't FT2.
					// 4-mat's eternity.xm has an empty instruments with a header size of 29.
					// Another module using that size is funky_dumbass.xm. Mysterious!
					// Note: This may happen when the XM Commenter by Aka (XMC.EXE) adds empty instruments at the end of the list,
					// which would explain the latter case, but in eternity.xm the empty slots are not at the end of the list.
					madeWith = verUnknown;
				}

				if (readInstrumentHeaderSize != 33)
				{
					madeWith &= ~verPlayerPRO;
				}
				else
				if (readSampleHeaderSize>SAMPLE_HEADER_SIZE && (madeWith&verPlayerPRO)!=0)
				{
					// Older PlayerPRO versions appear to write garbage in the sampleHeaderSize field, and it's different for each sample.
					// Note: FT2 NORMALLY writes sampleHeaderSize=40 for all samples, but for any instruments before the first
					// instrument that has numSamples != 0, sampleHeaderSize will be uninitialized. It will always be the same
					// value, though.
					if (instrumentWithSamplesEncountered || (lastSampleHeaderSize!=-1 && readSampleHeaderSize!=lastSampleHeaderSize))
						madeWith = verPlayerPRO | verConfirmed;
					lastSampleHeaderSize = readSampleHeaderSize;
				}
			}

			if (lastInstrumentType==-1)
			{
				lastInstrumentType = insType;
			}
			else
			if (lastInstrumentType!=insType && (madeWith&verFT2Generic)!=0)
			{
				// FT2 writes some random junk for the instrument type field,
				// but it's always the SAME junk for every instrument saved.
				// Note: This may happen when running an FT2-made XM through PutInst and adding new instrument slots.
				madeWith &= ~verFT2Generic;
				madeWith |= verFT2Clone;
			}

			if (anzSamples>0) // lets skip this, if nothing is to do!
			{
				instrumentWithSamplesEncountered = true;
				// If MIDI settings are present, this is definitely not an old MPT or PlayerPRO.
				if ((currentIns.midiChannel | currentIns.midiProgram) != 0 || currentIns.xm_enableMidi || currentIns.xm_muteComputer)
					madeWith &= ~(verOldModPlug | verNewModPlug | verPlayerPRO);
				
				if (readInstrumentHeaderSize!=INSTR_HEADER_SIZE || insType!=0)
					madeWith &= ~verPlayerPRO;
				
				if((madeWith&verConfirmed)==0 && (madeWith&verPlayerPRO)!=0)
				{
					// Note: Earlier (?) PlayerPRO versions do not seem to set the loop points to 0xFF (george_megas_-_q.xm)
					if ((!volumeEnvelope.loop  && volumeEnvelope.loopStartPoint==0xFF  && volumeEnvelope.loopEndPoint==0xFF) ||
						(!panningEnvelope.loop && panningEnvelope.loopStartPoint==0xFF && panningEnvelope.loopEndPoint==0xFF))
					{
						madeWith |= verConfirmed;
						madeWith &= ~verNewModPlug;
					}
				}
				
				instrumentContainer.reallocSampleSpace(getNSamples());
				for (int samIndex=0; samIndex<anzSamples; samIndex++)
				{
					final Sample current = new Sample();
	
					current.vibratoType = vibratoType;
					current.vibratoSweep = vibratoSweep;
					current.vibratoDepth = vibratoDepth;
					current.vibratoRate = vibratoRate;
	
					// Length
					current.byteLength = current.sampleLength = inputStream.readIntelDWord();
	
					// Repeat start and stop
					int repeatStart  = inputStream.readIntelDWord();
					final int repeatLength = inputStream.readIntelDWord();
					int repeatStop = repeatStart+repeatLength;
	
					// volume 64 is maximum
					final int vol  = inputStream.read() & 0x7F;
					current.volume = (vol>64)?64:vol;
					current.globalVolume = ModConstants.MAXSAMPLEVOLUME;
	
					// finetune Value>0x7F means negative
					final int fine = inputStream.read();
					current.fineTune = (fine>0x7F)?fine-0x100:fine;
	
					current.flags = inputStream.read();
					int loopType = 0;
					if ((current.flags&0x03)!=0) loopType |= ModConstants.LOOP_ON;
					if ((current.flags&0x02)!=0) loopType |= ModConstants.LOOP_IS_PINGPONG;
					current.loopType = loopType;

					if ((current.flags & 3) == 3 && (madeWith&verNewModPlug)!=0)
						madeWith |= verModPlugBidiFlag;
	
					int sampleLoadingFlags = 0;
					if ((current.flags&0x10)!=0)
					{
						sampleLoadingFlags |= ModConstants.SM_16BIT;
						current.sampleLength>>=1;
						repeatStart>>=1;
						repeatStop>>=1;
					}
					if ((current.flags&0x20)!=0)
					{
						sampleLoadingFlags |= ModConstants.SM_STEREO; // this is new, not standard. Support is easy, so why not!
						current.sampleLength>>=1;
						repeatStart>>=1;
						repeatStop>>=1;
					}
					current.isStereo = (sampleLoadingFlags&ModConstants.SM_STEREO)!=0;
	
					current.loopStart = repeatStart;
					current.loopStop = repeatStop;
					current.loopLength = repeatStop-repeatStart;
	
					// Defaults for non-existent SustainLoop
					current.sustainLoopStart = 0;
					current.sustainLoopStop = 0;
					current.sustainLoopLength = 0;
	
					// Panning 0..255
					current.setPanning = true;
					current.defaultPanning = inputStream.read();
	
					// Transpose -128..127
					final int transpose = inputStream.read();
					current.transpose = (transpose>0x7F)?transpose-0x100:transpose;
	
					current.baseFrequency = getPeriod2Hz(current, getFrequencyTable());
	
					// Reserved
					current.XM_reserved = inputStream.read(); // Reserved (abused for ModPlug's ADPCM compression) - its the length of the sample name (Pascal String)
	
					// Samplename
					current.name = inputStream.readString(22);
	
					// Interpreting the loaded flags
					if (current.XM_reserved == 0xAD && (current.flags & (0x10 | 0x20))==0) // ModPlug ADPCM compression
					{
						sampleLoadingFlags |= ModConstants.SM_ADPCM;
						anyADPCM = true;
					}
					else
						sampleLoadingFlags |= ModConstants.SM_PCMD; // XM save in deltas
	
					current.sampleType = sampleLoadingFlags;
	
					instrumentContainer.setSample(samIndex + sampleOffsetIndex, current);

					// now let's seek to end of sample header - although we should already be there.
					inputStream.seek(LSEEK+=sampleHeaderSize);
					
					// Again version stuff:
					sampleReserved |= current.XM_reserved;
					if (current.XM_reserved!=0 && current.XM_reserved!=0xAD)
						madeWith &=~(verOldModPlug | verNewModPlug | verOpenMPT);

					if (lastSampleReserved==-1)
						lastSampleReserved = current.XM_reserved;
					else
					if (lastSampleReserved!=current.XM_reserved)
						madeWith &= ~verPlayerPRO;

					if (current.defaultPanning != 128)
						madeWith &= ~verPlayerPRO;
					if ((current.fineTune & 0x0F)!=0 && current.fineTune!=127)
						madeWith &= ~verPlayerPRO;

					// FT2 stores the sample name length here (it just copies the entire Pascal string, but that string might have ended with spaces even before space-padding it in the file, so we cannot do an exact length comparison)
					if ((madeWith&(verFT2Generic | verFT2Clone))!=0 && (madeWith&(verNewModPlug | verPlayerPRO))!=0 && (madeWith&verConfirmed)==0 &&
							(current.XM_reserved>22 || !(current.XM_reserved>=current.name.trim().length() && current.XM_reserved<=22)))
					{
						madeWith &= ~verFT2Generic;
						madeWith |= (verFT2Clone | verConfirmed);
					}
				}

				if (version>=0x0104) readXMSampleData(inputStream, instrumentContainer, anzSamples, sampleOffsetIndex);
				sampleOffsetIndex += anzSamples;
			}
			instrumentContainer.setInstrument(ins, currentIns);
		}

		if (sampleReserved==0 && (madeWith&verNewModPlug)!=0)
		{
			// Null-terminated song name: Quite possibly MPT. (could really be an MPT-made file re-saved in FT2, though)
			for (byte b : songNameBuffer)
			{
				if (b==0)
				{
					madeWith |= verConfirmed;
					break;
				}
			}
		}

		if (version<0x0104)
		{
			readXMPattern(inputStream);
			readXMSampleData(inputStream, instrumentContainer, sampleOffsetIndex, 0);
		}

		midiMacros = new MidiMacros();
		boolean hasMidiConfig = false;
		boolean hasExtraInstrumentInfos = false;
		boolean hasExtraSongProperties = false;

		if (inputStream.checkMagic(ModConstants.getMagicLE("text"))) // 0x74786574 'text'
		{
			// read the song text
			final int len = inputStream.readIntelDWord();
			songMessage = inputStream.readString(len < inputStream.available()?len:inputStream.available());
			madeWith |= verConfirmed;
			madeWith &= ~verPlayerPRO;
		}

		if (inputStream.checkMagic(ModConstants.getMagicLE("MIDI"))) // 0x4944494D 'MIDI'
		{
			// read the MidiMacros
			final int len = inputStream.readIntelDWord();
			if (len==MidiMacros.SIZE_OF_SCTUCT && len<inputStream.getLength())
			{
				midiMacros.loadFrom(inputStream);
			}
			hasMidiConfig = true;
			madeWith |= verConfirmed;
			madeWith &= ~verPlayerPRO;
		}

		// OMPT extensions with FastTracker:
		// read Pattern Names:
		final String[] patNames = readNames(inputStream, ModConstants.getMagicLE("PNAM"), 32); // 0x4D414E50 PNAM - LE saved
		if (patNames!=null)
		{
			getPatternContainer().setPatternNames(patNames);
			madeWith |= verConfirmed;
			madeWith &= ~verPlayerPRO;
		}			
		// Read Channel Names
		final String[] chnNames = readNames(inputStream, ModConstants.getMagicLE("CNAM"), 20); // 0x4D414E43 CNAM - LE saved
		if (chnNames!=null)
		{
			getPatternContainer().setChannelNames(chnNames);
			madeWith |= verConfirmed;
			madeWith &= ~verPlayerPRO;
		}

		final int result = loadMixPlugins(inputStream);
		final boolean hasMixPlugins = (result&0xF0)!=0;
		if (hasMixPlugins)
		{
			madeWith |= verConfirmed;
			madeWith &= ~verPlayerPRO;
		}

		hasExtraInstrumentInfos = loadExtendedInstrumentProperties(inputStream);
		hasExtraSongProperties = loadExtendedSongProperties(inputStream, true);
		boolean isMPT = hasExtraInstrumentInfos || hasExtraSongProperties;
		
		if ((madeWith&verConfirmed)!=0)
		{
			if ((madeWith&verModPlugBidiFlag)!=0)
			{
				lastSavedWithVersion = 0x1110000;
				madeWithTracker = "ModPlug Tracker 1.0 - 1.11";
			}
			else
			if ((madeWith&verNewModPlug)!=0 && (madeWith&verPlayerPRO)==0)
			{
				lastSavedWithVersion = 0x1160000;
				madeWithTracker = "ModPlug Tracker 1.0 - 1.16";
			}
			else
			if ((madeWith&verNewModPlug)!=0 && (madeWith&verPlayerPRO)!=0)
			{
				lastSavedWithVersion = 0x1160000;
				madeWithTracker = "ModPlug Tracker 1.0 - 1.16 / PlayerPRO";
			}
			else
			if ((madeWith&verNewModPlug)==0 && (madeWith&verPlayerPRO)!=0)
			{
				madeWithTracker = "PlayerPRO";
			}
		}

		if (trackerName.startsWith("OpenMPT"))
		{
			lastSavedWithVersion = (trackerName.length()>7)?ModConstants.parseModPlugVersionString(trackerName.substring(8)):0;
			madeWith = verOpenMPT | verConfirmed;

			setModType(getModType() | ModConstants.MODTYPE_OMPT); 
			if (lastSavedWithVersion < 0x1220719)
				setModType(getModType() | ModConstants.MODTYPE_MIX_Compatible);
			else
				setModType(getModType() | ModConstants.MODTYPE_MIX_CompatibleFT2);
		}

		if (lastSavedWithVersion>0 && (madeWith&verOpenMPT)==0)
		{
			setModType(getModType() | ModConstants.MODTYPE_MPT); 
		}

		// ModPlug allowed marker pattern like in IT
		//  255 = "---", End of song marker
		//  254 = "+++", Skip to next order
		// We no longer allow any --- or +++ items in the order list now.
		if (lastSavedWithVersion>0 && lastSavedWithVersion<0x1220202) // V1.22.02.02
		{
			int realLen = 0;
			for (int i=0; i<getSongLength(); i++)
			{
				int patNum = arrangement[i];
				if (patNum==0xFE && !isValidPatternNumber(patNum)) // remove +++ markers
					continue;
				if (patNum==0xFF && !isValidPatternNumber(patNum)) // replace --- marker
					patNum = ModConstants.INVALID_PAT_INDEX;
				arrangement[realLen++]=patNum;
			}
			setSongLength(realLen);
			removeEndOfArrangement(); // so we centrally can decide (XM, S3M, IT) if we want to keep those
		}
		
		// Fix lamb_-_dark_lighthouse.xm, which only contains one pattern and an empty order list
		if (getSongLength()==0 && (madeWith&verEmptyOrders)==0)
		{
			arrangement[0] = 0;
			setSongLength(1);
		}

		if ((madeWith&verFT2Generic)!=0)
		{
			setModType(getModType() | ModConstants.MODTYPE_MIX_Compatible);
			if (!hasMidiConfig)
			{
				// FT2 allows typing in arbitrary unsupported effect letters such as Zxx.
				// Prevent these commands from being interpreted as filter commands by erasing the default MIDI Config.
				midiMacros.clearZxxMacros();
			}

			if (version >= 0x0104)	// Old versions of FT2 didn't have (smooth) ramping. Disable it for those versions where we can be sure that there should be no ramping.
			{
				// apply FT2-style super-soft volume ramping
				songFlags |= ModConstants.SONG_FT2VOLUMERAMPING;
			}
		}
		if (madeWithTracker==null)
		{
			if ((madeWith&verDigiTrakker)!=0 && sampleReserved==0 && (lastInstrumentType!=0?lastInstrumentType:-1)==-1)
			{
				madeWithTracker = "DigiTrakker";
			}
			else
			if((madeWith&verFT2Generic)!=0)
			{
				madeWithTracker = "FastTracker 2 or compatible";
				final int highVersion = (version>>8)&0xFF;
				madeWithTracker = "FastTracker II - File version " + ModConstants.getAsHex(highVersion, (highVersion>0x0f)?2:1) + "." + ModConstants.getAsHex(version&0xFF, 2);
				if (!trackerName.endsWith("   "))
					madeWithTracker += " (generic)";
			}
			else
			{
				madeWithTracker = "Unknown";
			}
		}
		
		if (isMPT && lastSavedWithVersion<0x1170000)
		{
			// Up to OpenMPT 1.17.02.45 (r165), it was possible that the "last saved with" field was 0
			// when saving a file in OpenMPT for the first time.
			lastSavedWithVersion = 0x1170000;
		}
		if (lastSavedWithVersion>=0x1170000)
		{
			madeWithTracker = "OpenMPT " + ModConstants.getModPlugVersionString(lastSavedWithVersion);
			if (!isMPT)
			{
				madeWithTracker += ModConstants.COMPAT_MODE;
				// Treat compatibility export as XMs
				setModType(getModType() & ~(ModConstants.MODTYPE_MPT | ModConstants.MODTYPE_OMPT));
			}
			if (createdWithVersion!=-1) madeWithTracker += " (first created with " + ModConstants.getModPlugVersionString(createdWithVersion)+")";
		}
		
		if (anyADPCM)
			madeWithTracker += " (ADPCM packed)";

		setTrackerName(madeWithTracker);

		// reset if instrument has valid midi data and midi output - with OMPT the plugin is important - even if that instrument has a sample mapping!
		if (isMPT)
		{
			final Instrument[] ins = instrumentContainer.getInstruments();
			for (int i=0; i<ins.length; i++)
			{
				final Instrument currentIns = ins[i];
				currentIns.hasValidMidiData = (currentIns.mixPlugIn>0 && currentIns.hasValidMidiChannel() && currentIns.hasValidMidiProgram());
			}
		}

		// With OpenModPlug Files we create default channel colors if none are set
		if ((getModType()&ModConstants.MODTYPE_OMPT)!=0 && getPatternContainer().getChannelColors()==null)
			getPatternContainer().createMPTMDefaultRainbowColors();
	}
}
