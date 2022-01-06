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
	protected int [] channelSettings;
	protected int [] panningValue;
	// Due to deactivated Channels, we need to remap:
	private int[] channelMap;

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
	protected ScreamTrackerMod(String fileName)
	{
		super(fileName);
	}
	/**
	 * @return the Fileextensions this loader is suitable for
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
	public int getPanningValue(int channel)
	{
		return panningValue[channel];
	}
	/**
	 * @param channel
	 * @return
	 * @see de.quippy.javamod.multimedia.mod.loader.Module#getChannelVolume(int)
	 */
	@Override
	public int getChannelVolume(int channel)
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
	 * Set a Pattern by interpreting
	 * @param input
	 * @param offset
	 * @param pattNum
	 */
	private void setPattern(int pattNum, ModfileInputStream inputStream) throws IOException
	{
		int row=0;
		PatternRow currentRow = getPatternContainer().getPatternRow(pattNum, row);

		int count = inputStream.readIntelUnsignedWord()-2; // this read byte also counts
		while (count>=0)
		{
			int packByte = inputStream.read(); count--;
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
				
				if ((packByte&32)!=0) // Note and Sample follow
				{
					int ton = inputStream.read(); count--;
					if (ton==254)
					{
						noteIndex = period = ModConstants.NOTE_CUT; // This is our NoteCutValue!
					}
					else 
					{
						// calculate the new note
						noteIndex = ((ton>>4)+1)*12+(ton&0xF); // fit to it octacves
						if (noteIndex>=ModConstants.noteValues.length)
						{
							period = 0;
							noteIndex = 0;
						}
						else
						{
							period = ModConstants.noteValues[noteIndex];
							noteIndex++;
						}
					}
					
					instrument = inputStream.read(); count--;
				}
				
				if ((packByte&64)!=0) // volume following
				{
					volume = inputStream.read(); count--;
				}
				
				if ((packByte&128)!=0) // Effekts!
				{
					effekt = inputStream.read(); count--;
					effektOp = inputStream.read(); count--;
				}
				
				if (channel!=-1)
				{
					PatternElement currentElement = currentRow.getPatternElement(channel);
					currentElement.setNoteIndex(noteIndex);
					currentElement.setPeriod(period);
					currentElement.setInstrument(instrument);
					if (volume!=-1)
					{
						currentElement.setVolumeEffekt(1);
						currentElement.setVolumeEffektOp(volume);
					}
					currentElement.setEffekt(effekt);
					currentElement.setEffektOp(effektOp);
				}
			}
		}
	}
	/**
	 * @param inputStream
	 * @return true, if this is a protracker mod, false if this is not clear
	 * @see de.quippy.javamod.multimedia.mod.loader.Module#checkLoadingPossible(de.quippy.javamod.io.ModfileInputStream)
	 */
	@Override
	public boolean checkLoadingPossible(ModfileInputStream inputStream) throws IOException
	{
		inputStream.seek(0x2C);
		String s3mID = inputStream.readString(4);
		inputStream.seek(0);
		return s3mID.equals("SCRM"); 
	}
	/**
	 * @param fileName
	 * @return
	 * @see de.quippy.javamod.multimedia.mod.loader.Module#getNewInstance(java.lang.String)
	 */
	@Override
	protected Module getNewInstance(String fileName)
	{
		return new ScreamTrackerMod(fileName);
	}
	/**
	 * @param inputStream
	 * @return
	 * @see de.quippy.javamod.multimedia.mod.loader.Module#loadModFile(byte[])
	 */
	@Override
	public void loadModFileInternal(ModfileInputStream inputStream) throws IOException
	{
		setModType(ModConstants.MODTYPE_S3M);
		setSongRestart(0);
		
		// Songname
		setSongName(inputStream.readString(28));
		
		// Skip arbitrary data...
		inputStream.readByte(); // should always be 0x1A to allow "TYPE SONG.S3M" on DOS
		byte type = inputStream.readByte();
		if (type!=0x10) throw new IOException("Unsupported S3M MOD (ID!=0x10)");
		/*int reserved = */inputStream.readIntelUnsignedWord();

		setSongLength(inputStream.readIntelUnsignedWord());
		setNSamples(inputStream.readIntelUnsignedWord());
		setNInstruments(getNSamples());
		setNPattern(inputStream.readIntelUnsignedWord());
		setNChannels(32);
		
		// Flags
//		38 (26h)|  2  | FLAGS.  This is the flags section.  You need to bitwise AND
//		|     |	it with the following values to see if the corresponding flags
//		|     | are set.  Most of them are for Scream Tracker specific
//		|     |	information so can just about be ignored if you like.
//		|     | if ((value AND  1) >0) : st2vibrato (not supported in st3.01)
//		|     | if ((value AND  2) >0) : st2tempo (not supported in st3.01)
//		|     | if ((value AND  4) >0) : amigaslides (not supported in st3.01)
//		|     | if ((value AND  8) >0) : 0vol optimizations: Automatically
//		|     |                          turn off looping notes whose volume
//		|     |                          is 0 for >2 note rows.  Don't bother
//		|     |                          with this.
//		|     | if ((value AND 16) >0) : amiga limits: Disallow any notes
//		|     |                          that go beyond the amiga hardware
//		|     |                          limits (like amiga does). This means
//		|     |                          that sliding up stops at B-5 etc.
//		|     |                          This also affects some minor amiga
//		|     |                          compatibility issues.
//		|     | if ((value AND 32) >0) : enable filter/sfx (not supported)
//		|     | if ((value AND 64) >0) : st3.00 volumeslides.  Normally
//		|     |                          volumeslide is NOT performed on first
//		|     |                          frame of each row (this is according
//		|     |                          to amiga playing). If this is set,
//		|     |                          volumeslide is performed ALSO on the
//		|     |                          first row. This is set by default if
//		|     |                          the CWT/V value is 1300h.
//		|     | if ((value AND 128) >0): special custom data in file.  If
//		|     |                          this is set then you can use the
//		|     |                          'Special' pointer at offset 3Eh.
//		|     |				  See more about this when describing
//		|     |				  this special pointer.
		flags = inputStream.readIntelUnsignedWord();

		// Version number (cwtv)
		version = inputStream.readIntelUnsignedWord();
		
		if ((flags&0x40)!=0 || version < 0x1300) songFlags |= ModConstants.SONG_FASTVOLSLIDES;
		if ((flags&0x80)!=0) songFlags |= ModConstants.SONG_AMIGALIMITS;

		// Samples Type (version)
		samplesType = inputStream.readIntelUnsignedWord();

		// ModID
		setModID(inputStream.readString(4));
		setTrackerName("ScreamTracker V" + ((version>>8)&0x0F) + '.' + (version&0xFF));

		// Global Volume
		int globalVolume = inputStream.read()<<1;
		if (globalVolume==0 || globalVolume>128) globalVolume = 128;
		setBaseVolume(globalVolume);
		
		// Tempo
		setTempo(inputStream.read());
		
		// BPM
		setBPMSpeed(inputStream.read());
		
		// MasterVolume (mv&0x80)!=0 --> Stereo else Mono, MasterVolume&0x7F is SoundBlaster specific
		int masterVol = inputStream.read();
		if ((masterVol & 0x80)!=0) songFlags |= ModConstants.SONG_ISSTEREO;
		final int mixingPreAmp = masterVol & 0x7F;
		setMixingPreAmp((mixingPreAmp<0x10)?0x10:mixingPreAmp);
		// UltraClick removal --> ignored
		/*int uc = */inputStream.read();
		// DefaultPanning
		usePanningValues = inputStream.read()==0xFC;
		
		// skip again arbitrary data (8Byte unused, 2Byte is pointer to special data, if "special data flag" at offset 0x26 is set
		inputStream.skip(10);
		
		// PanningValues and active or inactive Channels
		channelSettings = new int[32];
		channelMap = new int[32];
		int anzChannel = 0;
		for (int i=0; i<32; i++)
		{
			int readByte = inputStream.read(); 
			if ((readByte&0x80)==0)
			{
				channelMap[i]=anzChannel;
				channelSettings[anzChannel++] = readByte;
			}
			else
				channelMap[i]=-1;
		}
		setNChannels(anzChannel);
		
		// Song Arrangement
		int songLength = getSongLength();
		if (songLength<=0) songLength = 1;
		else 
		if (songLength>256) songLength = 256; 
		allocArrangement(songLength);
		for (int i=0; i<songLength; i++)  getArrangement()[i]=inputStream.read();

		// if songLength is odd, there might be a 0xFF left...
		long startSeek = 96L + getSongLength();
		if ((songLength&0x01)!=0)
		{
			byte skipByte = inputStream.readByte();
			if (skipByte==0xFF) startSeek++; 
		}
		
		final int anzPointers = getNSamples() + getNPattern();
		final long [] paraPointers = new long[anzPointers];
		inputStream.seek(startSeek);
		for (int i=0; i<anzPointers; i++) paraPointers[i] = (inputStream.readIntelUnsignedWord()<<4);
		
		// After the paraPointers we have the Panning Section - if any!
		// so, we read it here or at least set all values for panning here
		panningValue = new int[getNChannels()];
		if (usePanningValues)
		{
			//inputStream.seek(startSeek+(getNSamples()+getNPattern())<<1);
			for (int i=0; i<getNChannels(); i++)
			{
				int readByte = inputStream.read(); 
				int ch = channelMap[i];
				if (ch!=-1)
				{
					int val = (readByte & 0x0F) << 4;
					if ((readByte&0x20)!=0) 
						panningValue[ch] = val;
					else
						panningValue[ch] = ((channelSettings[i] & 0x08)!=0) ? 0xC0 : 0x40;
				}
			}
		}
		else
		if ((songFlags&ModConstants.SONG_ISSTEREO)==0) // MONO: everything to the middle
		{
			for (int i=0; i<getNChannels(); i++) panningValue[i]=128;
		}
		else
		{
			for (int i=0; i<getNChannels(); i++)
			{
				// value < 8 : left, else right
				panningValue[i] = ((channelSettings[i] & 0x08)!=0) ? 0xC0 : 0x40;
			}
		}

		// read the samples
		InstrumentsContainer instrumentContainer = new InstrumentsContainer(this, 0, getNSamples());
		this.setInstrumentContainer(instrumentContainer);
		for (int i=0; i<getNSamples(); i++)
		{
			final long pointer = paraPointers[i];
			if ((pointer == 0) || (pointer + 0x50)>inputStream.getLength()) continue;
			inputStream.seek(pointer);
			
			Sample current = new Sample();
			current.setStereo(false); // Default
			
			final int instrumentType = inputStream.read(); 
			current.setType(instrumentType);
			// Samplename
			current.setDosFileName(inputStream.readString(12));
			
			int highByte= inputStream.read();
			int lowByte = inputStream.readIntelUnsignedWord();
			long sampleOffset = (lowByte | (highByte<<16))<<4;
			if (sampleOffset > inputStream.getLength()) sampleOffset&=0xFFFF;
			
			// Length
			int sampleLength = inputStream.readIntelDWord();
			if (sampleLength<4) sampleLength = 0;
			if (instrumentType!=1) sampleLength=0;
			current.setLength(sampleLength);
			
			// Repeat start and stop
			int repeatStart = inputStream.readIntelDWord();
			if (repeatStart > sampleLength) repeatStart = sampleLength-1;
			int repeatStop  = inputStream.readIntelDWord();
			if (repeatStop > sampleLength) repeatStop = sampleLength;
			
			int repeateLength = repeatStop-repeatStart;
			if ((repeatStart>repeatStop) || repeateLength<8)
				repeatStart = repeatStop = repeateLength = 0;

			current.setLoopStart(repeatStart);
			current.setLoopStop(repeatStop);
			current.setLoopLength(repeateLength);
			
			// Defaults for non-existent SustainLoop
			current.setSustainLoopStart(0);
			current.setSustainLoopStop(0);
			current.setSustainLoopLength(0);

			// volume
			int volume = inputStream.read();
			current.setVolume((volume>64)?64:volume);
			current.setGlobalVolume(ModConstants.MAXSAMPLEVOLUME);
			
			// Reserved (Sample Beginning Offset?!)
			inputStream.skip(1);
			/*byte packingScheme = */inputStream.readByte(); // 0: unpacked, 1: DP30ADPCM packing - but s3m are never packed...

			// Flags: 1:Loop 2:Stereo 4:16Bit-Sample...
			current.setFlags(inputStream.read());
			current.setLoopType(((current.flags&0x01)==0x01) ? ModConstants.LOOP_ON : 0);
			
			// C4SPD
			current.setFineTune(0);
			current.setTranspose(0);
			int baseFreq = inputStream.readIntelDWord();
			if (baseFreq==0) baseFreq = 8363;
			else if (baseFreq<1024) baseFreq = 1024;
			current.setBaseFrequency(baseFreq);
			
			// Again reserved data...
			inputStream.skip(12);
			
			// SampleName
			current.setName(inputStream.readString(28));
			
			// Key
			inputStream.skip(4); // should be "SCRS" (sample) or "SCRI" (adlib instrument) - we ignore that
			
			current.setPanning(-1);
			
			if (instrumentType==1)
			{
				// SampleData
				int flags = (samplesType==2)?ModConstants.SM_PCMU:ModConstants.SM_PCMS;
				if ((current.flags&0x02)!=0) flags|=ModConstants.SM_STEREO;
				if ((current.flags&0x04)!=0) flags|=ModConstants.SM_16BIT;

				current.setStereo((flags&ModConstants.SM_STEREO)!=0); 
				inputStream.seek(sampleOffset);
				current.setSampleType(flags);
				readSampleData(current, inputStream);
			}
			
			instrumentContainer.setSample(i, current);
		}
		
		// Pattern data
		PatternContainer patternContainer = new PatternContainer(getNPattern(), 64, getNChannels());
		setPatternContainer(patternContainer);
		for (int pattNum=0; pattNum<getNPattern(); pattNum++)
		{
			// First, clear them:
			for (int row=0; row<64; row++)
			{
				for (int channel=0; channel<getNChannels(); channel++)
				{
					PatternElement currentElement = new PatternElement(pattNum, row, channel);
					patternContainer.setPatternElement(currentElement);
				}
			}

			final long pointer = paraPointers[getNSamples() + pattNum];
			if (pointer + 0x40 > inputStream.getLength()) continue;
			inputStream.seek(pointer);
			setPattern(pattNum, inputStream);
		}

		// Correct the songlength for playing, skip markerpattern... (do not want to skip them during playing!)
		int realLen = 0;
		for (int i=0; i<getSongLength(); i++)
		{
			if (getArrangement()[i]<254 && getArrangement()[i]<getNPattern())
				getArrangement()[realLen++]=getArrangement()[i];
		}
		setSongLength(realLen);
		cleanUpArrangement();
	}
}
