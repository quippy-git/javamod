/*
 * @(#) ScreamTrackerMod.java
 *
 * Created on 09.05.2006 by Daniel Becker
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
import de.quippy.javamod.multimedia.mod.loader.instrument.InstrumentsContainer;
import de.quippy.javamod.multimedia.mod.loader.instrument.Sample;
import de.quippy.javamod.multimedia.mod.loader.pattern.PatternContainer;
import de.quippy.javamod.multimedia.mod.loader.pattern.PatternElement;
import de.quippy.javamod.multimedia.mod.loader.pattern.PatternRow;
import de.quippy.javamod.multimedia.mod.midi.MidiMacros;
import de.quippy.javamod.multimedia.mod.mixer.BasicModMixer;
import de.quippy.javamod.multimedia.mod.mixer.ScreamTrackerMixer;

/**
 * @author Daniel Becker
 * @since 09.05.2006
 */
public class ScreamTrackerMod extends Module
{
	private static final String[] MODFILEEXTENSION = new String []
	{
		"s3m"
	};
	private static final String S3M_ID = "SCRM";
	private static final boolean REMOVE_UNUSED_CHANNELS = true;

	private static final int sampleStereo        = 0x02;
	private static final int sample16Bit         = 0x04;

	// Flags
//	38 (26h)|  2  | FLAGS.  This is the flags section.  You need to bitwise AND
//	|     |	it with the following values to see if the corresponding flags
//	|     | are set.  Most of them are for Scream Tracker specific
//	|     |	information so can just about be ignored if you like.
//	|     | if ((value AND  1) >0) : st2vibrato (not supported in st3.01)
//	|     | if ((value AND  2) >0) : st2tempo (not supported in st3.01)
//	|     | if ((value AND  4) >0) : amigaslides (not supported in st3.01)
//	|     | if ((value AND  8) >0) : 0vol optimizations: Automatically
//	|     |                          turn off looping notes whose volume
//	|     |                          is 0 for >2 note rows.  Don't bother
//	|     |                          with this.
//	|     | if ((value AND 16) >0) : amiga limits: Disallow any notes
//	|     |                          that go beyond the amiga hardware
//	|     |                          limits (like amiga does). This means
//	|     |                          that sliding up stops at B-5 etc.
//	|     |                          This also affects some minor amiga
//	|     |                          compatibility issues.
//	|     | if ((value AND 32) >0) : enable filter/sfx (not supported)
//	|     | if ((value AND 64) >0) : st3.00 volumeslides.  Normally
//	|     |                          volumeslide is NOT performed on first
//	|     |                          frame of each row (this is according
//	|     |                          to amiga playing). If this is set,
//	|     |                          volumeslide is performed ALSO on the
//	|     |                          first row. This is set by default if
//	|     |                          the CWT/V value is 1300h.
//	|     | if ((value AND 128) >0): special custom data in file.  If
//	|     |                          this is set then you can use the
//	|     |                          'Special' pointer at offset 3Eh.
//	|     |				  See more about this when describing
//	|     |				  this special pointer.
	private static final int songST2Vibrato		 = 0x01;
	private static final int songST2Tempo		 = 0x02;
	private static final int songAmigaSlides	 = 0x04;
//	private static final int songVolOptimized	 = 0x08;
	private static final int songAmigaLimit		 = 0x10;
//	private static final int songEnableFilter	 = 0x20;
	private static final int songFastVolSlide	 = 0x40;
//	private static final int songCustomData		 = 0x80;

	/**
	 * Will be executed during class load
	 */
	static
	{
		ModuleFactory.registerModule(new ScreamTrackerMod());
	}

	protected int flags;
	protected int samplesType;
	protected boolean usePanningValues;

	// Due to deactivated Channels, we need to remap:
	private int[] channelMap;
	private byte[] channelStatus;

	/**
	 * Constructor for ScreamTrackerMod
	 */
	public ScreamTrackerMod()
	{
		super();
	}
	/**
	 * Constructor for ScreamTrackerMod
	 * @param fileExtension
	 */
	protected ScreamTrackerMod(final String fileName)
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
	 * @param sampleRate
	 * @param doISP
	 * @return
	 * @see de.quippy.javamod.multimedia.mod.loader.Module#getModMixer(int, boolean)
	 */
	@Override
	public BasicModMixer getModMixer(final int sampleRate, final int doISP, final int doNoLoops, final int maxNNAChannels)
	{
		return new ScreamTrackerMixer(this, sampleRate, doISP, doNoLoops, maxNNAChannels);
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.mod.loader.Module#getPanningSeparation()
	 */
	@Override
	public int getPanningSeparation()
	{
		return 128;
	}
	/**
	 * @param channel
	 * @return
	 * @see de.quippy.javamod.multimedia.mod.loader.Module#getPanningValue(int)
	 */
	@Override
	public int getPanningValue(final int channel)
	{
		return panningValue[channel];
	}
	/**
	 * @param channel
	 * @return
	 * @see de.quippy.javamod.multimedia.mod.loader.Module#getChannelVolume(int)
	 */
	@Override
	public int getChannelVolume(final int channel)
	{
		return 64;
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.mod.loader.Module#getFrequencyTable()
	 */
	@Override
	public int getFrequencyTable()
	{
		return ModConstants.STM_S3M_TABLE;
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.mod.loader.Module#getSongMessage()
	 */
	@Override
	public String getSongMessage()
	{
		return null;
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.mod.loader.Module#getMidiConfig()
	 */
	@Override
	public MidiMacros getMidiConfig()
	{
		return null;
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
	 * @param inputStream
	 * @return true, if this is a Scream Tracker 3 mod, false if this is not clear
	 * @see de.quippy.javamod.multimedia.mod.loader.Module#checkLoadingPossible(de.quippy.javamod.io.ModfileInputStream)
	 */
	@Override
	public boolean checkLoadingPossible(final ModfileInputStream inputStream) throws IOException
	{
		inputStream.seek(0x2C);
		final String s3mID = inputStream.readString(4);
		inputStream.seek(0);
		return s3mID.equals(S3M_ID);
	}
	/**
	 * @param fileName
	 * @return
	 * @see de.quippy.javamod.multimedia.mod.loader.Module#getNewInstance(java.lang.String)
	 */
	@Override
	protected Module getNewInstance(final String fileName)
	{
		return new ScreamTrackerMod(fileName);
	}
	/**
	 * Set a Pattern by interpreting
	 * @param input
	 * @param offset
	 * @param pattNum
	 */
	private void setPattern(final int pattNum, final ModfileInputStream inputStream) throws IOException
	{
		int row=0;
		PatternRow currentRow = getPatternContainer().getPatternRow(pattNum, row);

		int count = inputStream.readIntelUnsignedWord()-2; // this read byte also counts
		while (count>=0)
		{
			final int packByte = inputStream.read(); count--;
			if (packByte==0)
			{
				row++;
				if (row>=64)
					break; // Maximum. But do we have to break?! Donnow...
				else
					currentRow = getPatternContainer().getPatternRow(pattNum, row);
			}
			else
			{
				int channel = packByte&31; // there is the channel
				channel = channelMap[channel];

				int period = 0;
				int noteIndex = 0;
				int instrument = 0;
				int volume = -1;
				int effekt = 0;
				int effektOp = 0;

				if ((packByte&0x20)!=0) // Note and Sample follow
				{
					final int ton = inputStream.read(); count--;
					switch (ton)
					{
						case 0xFF: noteIndex = period = ModConstants.NO_NOTE; break;
						case 0xFE: noteIndex = period = ModConstants.NOTE_CUT; break;
						default:
							// calculate the new note
							noteIndex = ((ton>>4)+1)*12+(ton&0xF); // fit to IT octaves
							if (noteIndex>=ModConstants.noteValues.length)
							{
								period = noteIndex = ModConstants.NO_NOTE;
							}
							else
							{
								period = ModConstants.noteValues[noteIndex];
								noteIndex++;
							}
							break;

					}

					instrument = inputStream.read(); count--;
				}

				if ((packByte&0x40)!=0) // volume following
				{
					volume = inputStream.read(); count--;
				}

				if ((packByte&0x80)!=0) // Effects!
				{
					effekt = inputStream.read(); count--;
					effektOp = inputStream.read(); count--;
				}

				if (channel!=-1)
				{
					final PatternElement currentElement = currentRow.getPatternElement(channel);
					currentElement.setNoteIndex(noteIndex);
					currentElement.setPeriod(period);
					currentElement.setInstrument(instrument);
					if (volume!=-1)
					{
						if(volume >= 128 && volume <= 192)
						{
							currentElement.setVolumeEffekt(0x08);
							currentElement.setVolumeEffektOp(volume - 128);
						}
						else
						{
							currentElement.setVolumeEffekt(1);
							currentElement.setVolumeEffektOp((volume>64)?64:volume);
						}
					}
					currentElement.setEffekt(effekt);
					currentElement.setEffektOp(effektOp);
				}
			}
		}
	}
	/**
	 * @param inputStream
	 * @return
	 * @see de.quippy.javamod.multimedia.mod.loader.Module#loadModFile(byte[])
	 */
	@Override
	protected void loadModFileInternal(final ModfileInputStream inputStream) throws IOException
	{
		setModType(ModConstants.MODTYPE_S3M);
		setSongRestart(0);

		// Songname
		setSongName(inputStream.readString(28));

		// Skip arbitrary data...
		inputStream.readByte(); // should always be 0x1A to allow "TYPE SONG.S3M" on DOS
		final byte type = inputStream.readByte();
		if (type!=0x10) throw new IOException("Unsupported S3M MOD (ID!=0x10)");
		/*int reserved = */inputStream.readIntelUnsignedWord();

		setSongLength(inputStream.readIntelUnsignedWord());
		setNSamples(inputStream.readIntelUnsignedWord());
		setNInstruments(getNSamples());
		setNPattern(inputStream.readIntelUnsignedWord());
		setNChannels(32);

		flags = inputStream.readIntelUnsignedWord();

		// Version number (cwtv)
		version = inputStream.readIntelUnsignedWord();

		// Set the flags:
		if ((flags&songST2Vibrato)!=0) songFlags |= ModConstants.SONG_ST2VIBRATO;
		if ((flags&songST2Tempo)!=0) songFlags |= ModConstants.SONG_ST2TEMPO;
		if ((flags&songAmigaSlides)!=0) songFlags |= ModConstants.SONG_AMIGASLIDES;
		if ((flags&songAmigaLimit)!=0) songFlags |= ModConstants.SONG_AMIGALIMITS;
		if ((flags&songFastVolSlide)!=0 || version == 0x1300) songFlags |= ModConstants.SONG_FASTVOLSLIDES;
//		final boolean hasCustomData = (flags&songCustomData)!=0;
		songFlags |= ModConstants.SONG_ITOLDEFFECTS; // default, so we do not need to check for IT

		// Samples Type (version)
		samplesType = inputStream.readIntelUnsignedWord();

		// ModID
		setModID(inputStream.readString(4));

		// Global Volume
		int globalVolume = inputStream.read()<<1;
		if (globalVolume==0 || globalVolume>128) globalVolume = 128;
		setBaseVolume(globalVolume);

		// Tempo
		final int speed = inputStream.read();
		setTempo((speed<=0)?6:speed);

		// BPM
		final int tempo = inputStream.read();
		setBPMSpeed((tempo<=32)?125:tempo); // tempo <= 32 is ignored by Scream Tracker

		// MasterVolume (mv&0x80)!=0 --> Stereo else Mono, MasterVolume&0x7F is SoundBlaster specific
		final int masterVol = inputStream.read();
		if ((masterVol & 0x80)!=0) songFlags |= ModConstants.SONG_ISSTEREO;
		final int mixingPreAmp = masterVol & 0x7F;
		setMixingPreAmp((mixingPreAmp<0x10)?0x10:mixingPreAmp);

		// UltraClick removal --> ignored (only for GUS playback)
		/*int uc = */inputStream.read();

		// DefaultPanning
		usePanningValues = inputStream.read()==0xFC;

		// skip again arbitrary data (8Byte unused, 2Byte is pointer to special data, if "special data flag" at offset 0x26 is set
		final int extVersionInfo = inputStream.readIntelUnsignedWord();
		inputStream.skip(6);
		/*final int specialDataParaPointer = */inputStream.readIntelUnsignedWord();

		// PanningValues and active or inactive Channels
		channelStatus = new byte[32];
		inputStream.read(channelStatus);

		// prepare panningValue array with panning and mute status
		final int [] tmpPanning = new int[32];
		for (int c=0; c<32; c++)
		{
			int status = channelStatus[c] & 0xFF;
			if ((status&0x80)!=0) // Muted channel - not necessarily disabled (&0xF==0!)
			{
				tmpPanning[c] |= ModConstants.CHANNEL_IS_MUTED; // this is like IT does it, but in this case will also lead to a completely disabled channel without even doing effects
				status &= ~0x80;
			}
			if (status < 0x08) // L1-L8, panned 0x3 in ST3, that is 0x30 here
				tmpPanning[c] |= 0x03<<4;
			else
			if (status < 0x10) // R1-R8, panned 0xc in ST3, that is 0xC0 here
				tmpPanning[c] |= 0x0C<<4;
			else
			if (status < 0x19) // AdlibChannel
				tmpPanning[c] |= 128;
			else				// disabled or broken
				tmpPanning[c] = ModConstants.CHANNEL_IS_MUTED;
		}

		// Song Arrangement
		int songLength = getSongLength();
		if (songLength<=0) songLength = 1;
		else
		if (songLength>256) songLength = 256;
		allocArrangement(songLength);
		for (int i=0; i<songLength; i++)  getArrangement()[i]=inputStream.read();
		if (getArrangement()[0]==255)
		{
			getArrangement()[0]=0;
			getArrangement()[1]=255;
		}

		// if songLength is odd, there might be a 0xFF left...
		long startSeek = 96L + getSongLength();
		if ((songLength&0x01)!=0)
		{
			final byte skipByte = inputStream.readByte();
			if (skipByte==0xFF) startSeek++;
		}

		final int anzPointers = getNSamples() + getNPattern();
		final long [] paraPointers = new long[anzPointers];
		inputStream.seek(startSeek);
		for (int i=0; i<anzPointers; i++) paraPointers[i] = (inputStream.readIntelUnsignedWord()<<4);

		// After the paraPointers we have the Panning Section - if any!
		// so, we read it here - this might even overwrite disabled channels from above
		if (usePanningValues)
		{
			for (int c=0; c<32; c++)
			{
				final int readByte = inputStream.read();
				if ((readByte&0x20)!=0)
				{
					tmpPanning[c] = (tmpPanning[c]&ModConstants.CHANNEL_IS_MUTED) | (readByte & 0x0F) << 4;
				}
			}
		}

		// now find out about the real amount of channels so we just do not
		// add those unused channels.
		// S3M knows of channels and of muted channels. Muted channels are
		// not played but are loaded. Inactive channels do not have a channel
		// number in the channel status map
		channelMap = new int[32];
		int anzChannels = 0;
		for (int c=0; c<32; c++)
		{
			final int status = channelStatus[c]&0xFF;
			if ((status & ~0x80)<19 || !REMOVE_UNUSED_CHANNELS) // active channel
			{
				tmpPanning[anzChannels] = tmpPanning[c];
				channelMap[c] = anzChannels++;
			}
			else
				channelMap[c] = -1;
		}
		setNChannels(anzChannels);
		panningValue = new int[anzChannels];
		System.arraycopy(tmpPanning, 0, panningValue, 0, anzChannels);
		// At this point we could now sort for the effects order. What? Yes!
		// Because ST3 does some ideocracy: Channels get a label like L1 or R2
		// channels without such a label are inactive / not present. We got rid
		// of those above.
		// Effects are now interpreted: first all L1-L8 than R1-R8.
		// Furthermore, if two channels have label L2, only the global effects
		// of the rightmost L2 are played. What a fuckery!

		// read the samples
		final InstrumentsContainer instrumentContainer = new InstrumentsContainer(this, 0, getNSamples());
		setInstrumentContainer(instrumentContainer);
		int gusAdresses = 0;
		boolean anySamples = false;
		for (int i=0; i<getNSamples(); i++)
		{
			final long pointer = paraPointers[i];
			if ((pointer == 0) || (pointer + 0x50)>inputStream.getLength()) continue;
			inputStream.seek(pointer);
			byte packingScheme = 0;

			final Sample current = new Sample();

			// Defaults
			current.setGlobalVolume(ModConstants.MAXSAMPLEVOLUME);

			final int instrumentType = inputStream.read();
			current.setType(instrumentType);
			// Sample name
			current.setDosFileName(inputStream.readString(12));

			// Sample Para Pointer (useless for adlib...)
			final int highByte= inputStream.read();
			final int lowByte = inputStream.readIntelUnsignedWord();
			long sampleOffset = (lowByte | (highByte<<16))<<4;
			if (sampleOffset > inputStream.getLength()) sampleOffset&=0xFFFF;

			if (instrumentType==1) // Sample
			{
				// Length
				final int sampleLength = inputStream.readIntelDWord();
				current.setLength(sampleLength);
				current.setByteLength(current.length);

				if (sampleLength>0) anySamples = true;

				// Repeat start and stop
				int repeatStart = inputStream.readIntelDWord();
				if (repeatStart > sampleLength) repeatStart = sampleLength-1;
				int repeatStop  = inputStream.readIntelDWord();
				if (repeatStop > sampleLength) repeatStop = sampleLength;

				int repeateLength = repeatStop-repeatStart;
				if ((repeatStart>repeatStop) || repeateLength<2)
					repeatStart = repeatStop = repeateLength = 0;

				current.setLoopStart(repeatStart);
				current.setLoopStop(repeatStop);
				current.setLoopLength(repeateLength);

				// Defaults for non-existent SustainLoop
				current.setSustainLoopStart(0);
				current.setSustainLoopStop(0);
				current.setSustainLoopLength(0);

				// volume
				final int volume = inputStream.read();
				current.setVolume((volume>64)?64:volume);

				// Reserved (Sample Beginning Offset?!)
				inputStream.skip(1);
				packingScheme = inputStream.readByte(); // 0: unpacked, 1: DP30ADPCM packing

				// Flags: 1:Loop 2:Stereo 4:16Bit-Sample...
				current.setFlags(inputStream.read());
				current.setLoopType(((current.flags&0x01)==0x01) ? ModConstants.LOOP_ON : 0);
			}
			else
			if (instrumentType>1 && instrumentType<8)
			{
				// According to tech.txt S3M supports AdLib channels for instruments
				// 2:Melody 3:Basedrum 4:Snare 5:Tom 6:Cym 7:HiHat
				// I have a real AdLib DSP emulator - so I intend to fully use it!
				current.adLib_Instrument = new byte[12];
				inputStream.read(current.adLib_Instrument, 0, 12);
				if (needsOPL==NO_OPL) needsOPL = OPL2; // activate OPL2 support
				// Example S3M coming with OMPT has wave forms of OPL3 - I cannot reproduce how that worked.
				// Maybe ModPlug 1.28 had a flaw and was always supporting OPL3 as being downward
				// compatible with OPL2. With 1.31 you cannot set OPL3 wave forms with s3m. Even converting an MPTM
				// does not do the trick.
				if (current.getAdlibWaveSelect(0)>3 || current.getAdlibWaveSelect(1)>3) needsOPL = OPL3;
				// volume
				final int volume = inputStream.read();
				current.setVolume((volume>64)?64:volume);
				// "dsk" - unknown + plus 2 unused bytes
				inputStream.skip(3);
			}
			else // Something we do not know
			{
				inputStream.skip(12);
				// volume
				final int volume = inputStream.read();
				current.setVolume((volume>64)?64:volume);
				inputStream.skip(3);
			}

			// C4SPD
			current.setFineTune(0);
			current.setTranspose(0);
			int baseFreq = inputStream.readIntelDWord();

			if (instrumentType>1 && instrumentType<8 && ((baseFreq<1000 || baseFreq>0xFFFF))) // If this is adlib
				baseFreq = ModConstants.BASEFREQUENCY;
			else
			if (baseFreq<=0) baseFreq = ModConstants.BASEFREQUENCY;
			else
			if (baseFreq<1024) baseFreq = 1024;
			current.setBaseFrequency(baseFreq);

			// Again reserved data - but we pick out the GUS addresses. Schism and ModPlug use that to identify certain playback quirks
			inputStream.skip(4);
			final int gusAdress = inputStream.readIntelUnsignedWord();
			gusAdresses |= gusAdress;
			inputStream.skip(6);

			// SampleName
			current.setName(inputStream.readString(28));

			// Key
			inputStream.skip(4); // should be "SCRS" (sample) or "SCRI" (adlib instrument) - we ignore that, because of already known "instrumentType"

			// Defaults!
			current.setPanning(false);
			current.setDefaultPanning(128);

			if (instrumentType==1)
			{
				// SampleData
				int flags = 0;
				if (packingScheme!=0 && (current.flags & (sampleStereo | sample16Bit)) == 0)  // ModPlug ADPCM compression, only mono 8Bit
					flags = ModConstants.SM_ADPCM;
				else
				{
					flags = ((samplesType&0x02)!=0)?ModConstants.SM_PCMU:ModConstants.SM_PCMS;
					if ((current.flags&sampleStereo)!=0) flags|=ModConstants.SM_STEREO;
					if ((current.flags&sample16Bit)!=0) flags|=ModConstants.SM_16BIT;
				}

				current.setStereo((flags&ModConstants.SM_STEREO)!=0);
				inputStream.seek(sampleOffset);
				current.setSampleType(flags);
				readSampleData(current, inputStream);
			}

			instrumentContainer.setSample(i, current);
		}

		// Pattern data
		final PatternContainer patternContainer = new PatternContainer(this, getNPattern(), 64, getNChannels());
		setPatternContainer(patternContainer);
		for (int pattNum=0; pattNum<getNPattern(); pattNum++)
		{
			// First, clear them:
			for (int row=0; row<64; row++)
			{
				for (int channel=0; channel<getNChannels(); channel++)
				{
					patternContainer.createPatternElement(pattNum, row, channel);
				}
			}

			final long pointer = paraPointers[getNSamples() + pattNum];
			if (pointer + 0x40 > inputStream.getLength()) continue;
			if (pointer>0) // 0== empty pattern
			{
				inputStream.seek(pointer);
				setPattern(pattNum, inputStream);
			}
		}
		patternContainer.setChannelActiveStatus(panningValue);

		// Correct the songlength for playing, skip markerpattern... (do not want to skip them during playing!)
		cleanUpArrangement();

		String trackerName = null;
		switch (version>>12)
		{
			case 0:
				if (version==0x0208) setTrackerName("Akord");
				break;
			case 1:
				trackerName = "ScreamTracker %1d.%02x";
				if (gusAdresses>1)
				{
					trackerName += " (GUS)";
					songFlags |= ModConstants.SONG_S3M_GUS;
				}
				else
				if (gusAdresses<=1 || version==0x1300 || !anySamples)
				{
					trackerName += " (SB)";
					songFlags &= ~ModConstants.SONG_S3M_GUS;
				}
				break;
			case 2:
				if (version==0x2013)
					setTrackerName("PlayerPRO");
				else
					trackerName = "Imago Orpheus %1d.%02x";
				break;
			case 3:
				if (version<=0x3214)
					trackerName = "Impulse Tracker %1d.%02x";
				else
					setTrackerName(String.format("Impulse Tracker 2.14p%d", Integer.valueOf(version-0x3214)));
				break;
			case 4:
				if (version==0x4100)
					setTrackerName("BeRoTracker");
				else
					setTrackerName("Schism Tracker "+ModConstants.getSchismVersionString((version<<16) | extVersionInfo));
				break;
			case 5:
				if (version==0x5447)
					trackerName = "Graoumf Tracker %1d.%02x";
				else
				{
					setTrackerName("OpenMPT "+ModConstants.getModPlugVersionString(((version&0xFFF)<<16) | extVersionInfo));
					// Set this as OMPT file - however there are no special things to handle then - so for now
					setModType(getModType() | ModConstants.MODTYPE_OMPT);
					songFlags |= ModConstants.SONG_S3M_GUS; // OpenMPT plays Sample Offset in GUS-Mode
				}
				break;
			case 6:
				trackerName = "BeRoTracker %1d.%02x";
				break;
			case 7:
				trackerName = "CreamTracker %1d.%02x";
				break;
			case 12:
				if (version==0xCA00)
					setTrackerName("Camoto");
				break;
			default:
				break;
		}
		if (trackerName==null && (getTrackerName()==null || getTrackerName().isEmpty()))
			trackerName = ("Unknown Tracker (ID: "+((version&0xF000)>>12)+") %1d, %02x");
		if (trackerName!=null)
			setTrackerName(String.format(trackerName, Integer.valueOf((version>>8)&0x0F), Integer.valueOf(version&0xFF)));

		// With OpenModPlug Files we create default channel colors if none are set
		// but only, if standards of S3M are broken (in this case: OOPL3 is needed)
		if ((getModType()&(ModConstants.MODTYPE_MPT | ModConstants.MODTYPE_OMPT))!=0 && patternContainer.getChannelColors()==null)
			patternContainer.createMPTMDefaultRainbowColors();
	}
}
