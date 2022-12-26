/*
 * @(#) FarandoleTrackerMod.java
 *
 * Created on 13.08.2022 by Daniel Becker
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
package de.quippy.javamod.multimedia.mod.loader.tracker;

import java.io.IOException;

import de.quippy.javamod.io.ModfileInputStream;
import de.quippy.javamod.multimedia.mod.ModConstants;
import de.quippy.javamod.multimedia.mod.loader.Module;
import de.quippy.javamod.multimedia.mod.loader.ModuleFactory;
import de.quippy.javamod.multimedia.mod.loader.instrument.InstrumentsContainer;
import de.quippy.javamod.multimedia.mod.loader.instrument.Sample;
import de.quippy.javamod.multimedia.mod.loader.pattern.Pattern;
import de.quippy.javamod.multimedia.mod.loader.pattern.PatternContainer;
import de.quippy.javamod.multimedia.mod.loader.pattern.PatternElement;
import de.quippy.javamod.multimedia.mod.loader.pattern.PatternRow;
import de.quippy.javamod.multimedia.mod.midi.MidiMacros;
import de.quippy.javamod.multimedia.mod.mixer.BasicModMixer;
import de.quippy.javamod.multimedia.mod.mixer.ScreamTrackerMixer;

/**
 * @author Daniel Becker
 * @since 13.08.2022
 */
public class FarandoleTrackerMod extends ScreamTrackerMod
{
	private static final String[] MODFILEEXTENSION = new String [] 
	{
		"far"
	};
	/**
	 * Will be executed during class load
	 */
	static
	{
		ModuleFactory.registerModule(new FarandoleTrackerMod());
	}
	
	private static final int FARFILEMAGIC = 0xFE524146;
	private static final int BREAK_ROW_INVALID = -1;
	
	private String songMessage;
	
	/**
	 * Constructor for FarandoleTrackerMod
	 */
	public FarandoleTrackerMod()
	{
		super();
	}
	/**
	 * Constructor for FarandoleTrackerMod
	 * @param fileName
	 */
	public FarandoleTrackerMod(String fileName)
	{
		super(fileName);
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.mod.loader.Module#getFileExtensionList()
	 */
	@Override
	public String[] getFileExtensionList()
	{
		return MODFILEEXTENSION;
	}
	/**
	 * We load Farandole Mods as S3M
	 * @param sampleRate
	 * @param doISP
	 * @param doNoLoops
	 * @param maxNNAChannels
	 * @return
	 * @see de.quippy.javamod.multimedia.mod.loader.Module#getModMixer(int, int, int, int)
	 */
	@Override
	public BasicModMixer getModMixer(int sampleRate, int doISP, int doNoLoops, int maxNNAChannels)
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
		return songMessage;
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
	 * @param inputStream
	 * @return
	 * @throws IOException
	 * @see de.quippy.javamod.multimedia.mod.loader.Module#checkLoadingPossible(de.quippy.javamod.io.ModfileInputStream)
	 */
	@Override
	public boolean checkLoadingPossible(ModfileInputStream inputStream) throws IOException
	{
		int id = inputStream.readIntelDWord();
		inputStream.seek(0);
		return id == FARFILEMAGIC;
	}
	/**
	 * @param fileName
	 * @return
	 * @see de.quippy.javamod.multimedia.mod.loader.Module#getNewInstance(java.lang.String)
	 */
	@Override
	protected Module getNewInstance(String fileName)
	{
		return new FarandoleTrackerMod(fileName);
	}
	/**
	 * Internal Routine for reading and converting a pattern entry 
	 * @since 13.08.2022
	 * @param pattNum
	 * @param patternSize
	 * @param patternContainer
	 * @param inputStream
	 * @throws IOException
	 */
	private void setPattern(final int pattNum, final int patternSize, final PatternContainer patternContainer, final ModfileInputStream inputStream) throws IOException
	{
		//final int rows = (patternSizes[pattNum] - 2) / (16 * 4); // documentation: 16 Channels, 4 bytes each
		final int rows = (patternSize - 2) >> 6;
		
		// read length in rows - is interpreted as pattern row break:
		int breakRow = inputStream.read();
		inputStream.skip(1); // Tempo for pattern, - Unsupported, use not recommended
		if (breakRow > 0 && breakRow < (rows - 2))
			breakRow++;
		else 
			breakRow = BREAK_ROW_INVALID;
		patternContainer.setPattern(pattNum, new Pattern(rows));
		for (int row=0; row<rows; row++)
		{
			// create the PatternRow
			patternContainer.setPatternRow(pattNum, row, new PatternRow(getNChannels()));
			// now read the data and set the pattern row:
			for (int channel=0; channel<getNChannels(); channel++)
			{
				PatternElement currentElement = new PatternElement(pattNum, row, channel);
				patternContainer.setPatternElement(currentElement);
				
				// now read in:
				final int note = inputStream.read(); // 0 - 72
				final int inst = inputStream.read();
				final int vol  = inputStream.read(); // 0 - 16
				final int eff  = inputStream.read();
				
				if (note>0 && note<72)
				{
					final int noteIndex = note + 48;
					if (noteIndex<ModConstants.noteValues.length)
					{
						currentElement.setNoteIndex(noteIndex);
						currentElement.setPeriod(ModConstants.noteValues[noteIndex]);
					}
				}
				
				currentElement.setInstrument(inst + 1);

				if (note>0 || vol>0)
				{
					currentElement.setVolumeEffekt(0x01); // Default setVolume effect
					currentElement.setVolumeEffektOp((vol>16)?64:(vol-1)<<2); // max 64 instead 16
				}
				
				int effekt = eff>>4;
				int effektOp = eff&0x0F;
				
				if (effekt == 0x09) // special treatment!
				{
					currentElement.setVolumeEffekt(0x01); // Default setVolume effect
					currentElement.setVolumeEffektOp((effektOp+1)<<2); // max 64 instead 15
					effekt = effektOp = 0;
				}
				
				// Translation:
				switch (effekt)
				{
					case 0x01: // Porta Up
						effekt = 0x06;
						effektOp |= 0xF0; // fine porta
						break;
					case 0x02: // Porta Down
						effekt = 0x05;
						effektOp |= 0xF0;
						break;
					case 0x03: // Porta To Note
						effekt = 0x07;
						effektOp <<=2;
						break;
					case 0x04: // Retrig
						effekt = 0x11;
						effektOp = (6 / (1 + effektOp)) + 1; // ugh?
						break;
					case 0x05: // set Vibrato Depth
						effekt = 0x08;
						break;
					case 0x06: // Vibrato Speed
						effekt = 0x08;
						effektOp <<= 4;
						break;
					case 0x07: // Volume Slide Up
						effekt = 0x04;
						effektOp <<= 4;
						break;
					case 0x08: // Volume Slide Down
						effekt = 0x04;
						break;
//					case 0x0A: // Port to Volume
//						break;
					case 0x0B:// set Balance
						effekt = 0x13;
						effektOp |= 0x80; // set Fine Panning 
						break;
					case 0x0D: // Fine Tempo Down
						break;
					case 0x0E: // Fine Tempo Up
						break;
					case 0x0F:
						effekt = 0x01;
						break;
				}
				currentElement.setEffekt(effekt);
				currentElement.setEffektOp(effektOp);
			}
		}
	}
	private void readSampleData(final int sampleIndex, final ModfileInputStream inputStream) throws IOException
	{
		Sample current = new Sample();
		current.setName(inputStream.readString(32));
		
		// Length
		int length = inputStream.readIntelDWord();
		
		// finetune Value>7 means negative 8..15= -8..-1
		/*final int fine = */inputStream.read(); // shall we use this?!
		current.setFineTune(0);
		
		// volume
		final int vol = inputStream.read();
		current.setVolume((vol>64)?64:vol);

		// Repeat start and stop
		int repeatStart  = inputStream.readIntelDWord();
		int repeatStop = inputStream.readIntelDWord();

		// Flags
		final int sampleType = inputStream.read();
		final int loopType = inputStream.read();
		
		if (current.length>0)
		{
			if (repeatStart > current.length) repeatStart=current.length-1;
			if (repeatStop > current.length) repeatStop=current.length;
			if (repeatStop <= repeatStart) repeatStart = repeatStop = 0;
		}
		
		if ((sampleType & 0x01)!=0) //16Bit:
		{
			length>>=1;
			repeatStart>>=1;
			repeatStop>>=1;
		}
		
		current.setLength(length);
		current.setLoopStart(repeatStart);
		current.setLoopStop(repeatStop);
		current.setLoopLength(repeatStop-repeatStart);
		if ((loopType & 8)!=0 && repeatStop > repeatStart)
			current.setLoopType(ModConstants.LOOP_ON);
		
		// Defaults for non-existent SustainLoop
		current.setSustainLoopStart(0);
		current.setSustainLoopStop(0);
		current.setSustainLoopLength(0);

		// Defaults!
		current.setStereo(false);
		current.setGlobalVolume(ModConstants.MAXSAMPLEVOLUME);
		current.setTranspose(0);
		current.setBaseFrequency(8363);
		current.setPanning(-1);
		
		// SampleData
		int flags = ModConstants.SM_PCMS;
		if ((sampleType & 0x01)!=0) flags|=ModConstants.SM_16BIT;
		current.setSampleType(flags);
		readSampleData(current, inputStream);
		
		getInstrumentContainer().setSample(sampleIndex, current);
	}
	/**
	 * @param inputStream
	 * @throws IOException
	 * @see de.quippy.javamod.multimedia.mod.loader.Module#loadModFileInternal(de.quippy.javamod.io.ModfileInputStream)
	 */
	@Override
	protected void loadModFileInternal(ModfileInputStream inputStream) throws IOException
	{
		setModType(ModConstants.MODTYPE_S3M); // Farandole is converted internally to s3m
		setNChannels(16);
		setBaseVolume(ModConstants.MAXGLOBALVOLUME);
		final int preAmp = ModConstants.MAX_MIXING_PREAMP / getNChannels();
		setMixingPreAmp((preAmp<ModConstants.DEFAULT_MIXING_PREAMP)?ModConstants.DEFAULT_MIXING_PREAMP:(preAmp>0x80)?0x80:preAmp);
		songFlags = ModConstants.SONG_ISSTEREO;
		
		// ModID
		final int modID = inputStream.readIntelDWord();

		// Songname
		setSongName(inputStream.readString(40));

		// EOF from header should be 0x0D0A1A --> so old DOS Type command would stop here...
		final int eof = (inputStream.readByte()<<16) | (inputStream.readByte()<<8) | inputStream.readByte();

		// Header Length
		final int headerLength = inputStream.readIntelUnsignedWord();

		// check if header is valid...
		if (modID!=FARFILEMAGIC || eof!=0x000D0A1A || headerLength < 98) throw new IOException("Unsupported Farandole MOD");
		
		// Composer Version
		version = inputStream.read();

		// onOff
		final byte [] onOff = new byte[16];
		inputStream.read(onOff, 0, 16);
		
		// skip Editing State of Composer:
		inputStream.skip(9);
		
		// Tempo & BPM
		setTempo(inputStream.read());
		setBPMSpeed(80); // default BPM
		
		// Panning values:
		usePanningValues = true;
		panningValue = new int[16];
		for (int ch=0; ch<16; ch++)
		{
			final int readByte = inputStream.read(); 
			panningValue[ch] = ((readByte & 0x0F) << 4) + 8;
		}
		
		// skip Pattern state
		inputStream.skip(4);
		
		// Message Length
		final int messageLength = inputStream.readIntelUnsignedWord();
		if (messageLength>0)
		{
			final String message = inputStream.readString(messageLength);
			int start = 0;
			int rest = 132;
			final int rows = message.length() / rest;
			StringBuilder b = new StringBuilder(messageLength+rows); // length plus "\n"
			for (int i=0; i<rows; i++)
			{
				b.append(message.substring(start, start+rest));
				b.append('\n');
				start+=rest;
				if ((start+rest)>message.length()) rest = message.length()-start;
			}
			songMessage = b.toString();
		}
		
		setModID("FAR");
		setTrackerName("Farandole Composer V" + ((version>>4)&0x0F) + '.' + (version&0x0F));
	
		// now for the pattern order
		allocArrangement(256);
		for (int i=0; i<256; i++) getArrangement()[i]=inputStream.read();
		/*final int numPatterns = */inputStream.read(); // obviously this is a lie - so we allocate always all 256 and skip loading of unused
		setNPattern(256);
		final int numOrders = inputStream.read();
		final int restartPos = inputStream.read();
		final int [] patternSizes = new int[256];
		for (int i=0; i<256; i++) patternSizes[i] = inputStream.readIntelUnsignedWord();
		setSongRestart(restartPos);
		setSongLength(numOrders);
		
		// now skip to patterns
		inputStream.seek(headerLength);

		PatternContainer patternContainer = new PatternContainer(getNPattern());
		setPatternContainer(patternContainer);
		for (int pattNum=0; pattNum<getNPattern(); pattNum++)
		{
			if (patternSizes[pattNum]==0)
			{
				// We need an empty pattern - player is not supporting "NULL" patterns...
				patternContainer.setPattern(pattNum, new Pattern(0));
				continue; // Empty pattern, nothing to do
			}
			// Create the Pattern
			setPattern(pattNum, patternSizes[pattNum], patternContainer, inputStream);
		}
		
		final byte [] sampleMap = new byte[8];
		inputStream.read(sampleMap, 0, 8);
		// 64 Instruments max (8 bytes) if a bit is set, the instrument is stored!
		setNSamples(64);
		InstrumentsContainer instrumentContainer = new InstrumentsContainer(this, 0, getNSamples());
		this.setInstrumentContainer(instrumentContainer);
		for (int instIndex=0; instIndex<64; instIndex++)
		{
			if ((sampleMap[instIndex>>3] & (1<<(instIndex&0x07)))!=0)
			{
				readSampleData(instIndex, inputStream);
			}
		}
	}
}
