/*
 * @(#) ScreamTrackerSTXMod.java
 *
 * Created on 24.07.2024 by Daniel Becker
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
import de.quippy.javamod.multimedia.mod.loader.pattern.PatternContainer;
import de.quippy.javamod.multimedia.mod.loader.pattern.PatternElement;
import de.quippy.javamod.multimedia.mod.loader.pattern.PatternRow;
import de.quippy.javamod.multimedia.mod.midi.MidiMacros;
import de.quippy.javamod.multimedia.mod.mixer.BasicModMixer;
import de.quippy.javamod.multimedia.mod.mixer.ScreamTrackerMixer;

/**
 * @author Daniel Becker
 * @since 24.07.2024
 */
public class ScreamTrackerSTXMod extends ScreamTrackerOldMod
{
	private static final String[] MODFILEEXTENSION = new String []
	{
		"stx"
	};

	/**
	 * Will be executed during class load
	 */
	static
	{
		ModuleFactory.registerModule(new ScreamTrackerSTXMod());
	}
	/**
	 * Constructor for ScreamTrackerSTXMod
	 */
	public ScreamTrackerSTXMod()
	{
		super();
	}
	/**
	 * Constructor for ScreamTrackerSTXMod
	 * @param fileName
	 */
	public ScreamTrackerSTXMod(final String fileName)
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
		if ((channel%3)!=0)
			return ModConstants.OLD_PANNING_RIGHT;
		else
			return ModConstants.OLD_PANNING_LEFT;
//		return panningValue[channel];
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
	 * @return true, if this is a stx mod, false if this is not clear
	 * @see de.quippy.javamod.multimedia.mod.loader.Module#checkLoadingPossible(de.quippy.javamod.io.ModfileInputStream)
	 */
	@Override
	public boolean checkLoadingPossible(final ModfileInputStream inputStream) throws IOException
	{
		inputStream.seek(0x14);
		// We should not be too picky about the !Scream!-Tag...
		// so simply accept any printable ASCII as ID
		final byte [] stmID = new byte[8];
		inputStream.read(stmID);
		for (int c=0; c<8; c++)
		{
			if (stmID[c]<0x20 || stmID[c]>0x7E)
				return false;
		}
		// STX have a second header field
		inputStream.seek(0x3C);
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
		return new ScreamTrackerSTXMod(fileName);
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
		while (row<64)
		{
			final int mask = inputStream.read();
			final int channel = mask&0x1F;
			if (mask==-1) return;
			if (mask==0)
			{
				row++;
				continue;
			}
			int period = 0;
			int noteIndex = 0;
			int instrument = 0;
			int volume = -1;
			int effekt = 0;
			int effektOp = 0;
			if ((mask&0x20)!=0) // Note and Sample follow
			{
				final int ton = inputStream.read();
				instrument = inputStream.read();
				switch (ton)
				{
					case 0xFF: period = noteIndex = ModConstants.NO_NOTE; break;
					case 0xFE: period = noteIndex = ModConstants.NOTE_CUT; break;
					default:
						noteIndex = ((ton>>4)+3)*12+(ton&0xF); // fit to IT octaves
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
			}
			if ((mask&0x40)!=0) // volume following
			{
				volume = inputStream.read();
			}
			if ((mask&0x80)!=0) // Effects!
			{
				effekt = inputStream.read();
				effektOp = inputStream.read();
			}
			if (channel!=-1)
			{
				final PatternRow currentRow = getPatternContainer().getPatternRow(pattNum, row);
				final PatternElement currentElement = currentRow.getPatternElement(channel);
				currentElement.setNoteIndex(noteIndex);
				currentElement.setPeriod(period);
				currentElement.setInstrument(instrument);
				if (volume!=-1)
				{
					currentElement.setVolumeEffekt(1);
					currentElement.setVolumeEffektOp((volume>64)?64:volume);
				}
				currentElement.setEffekt(effekt);
				currentElement.setEffektOp(effektOp);
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
		// is apparently not stereo
		//songFlags |= ModConstants.SONG_ISSTEREO;
		// defaults
		songFlags |= ModConstants.SONG_ST2VIBRATO;
		songFlags |= ModConstants.SONG_ST2TEMPO;
		songFlags |= ModConstants.SONG_ITOLDEFFECTS;
		setSongRestart(0);

		int subversion = 1;

		// Songname
		setSongName(inputStream.readString(20));

		// ID. Should be "!Scream!"
		setModID(inputStream.readString(8));
		final int headerPatternSize = inputStream.readIntelUnsignedWord();
		inputStream.skip(2);

		final int patternPointer = inputStream.readIntelUnsignedWord();
		final int samplePointer = inputStream.readIntelUnsignedWord();
		final int orderListPointer = inputStream.readIntelUnsignedWord();

		inputStream.skip(4);
		setBaseVolume(inputStream.read()<<1);
		setMixingPreAmp(ModConstants.MIN_MIXING_PREAMP);

		final int playBackTempo = inputStream.read();
		setTempo((playBackTempo>>4)!=0?playBackTempo>>4:6);
		setBPMSpeed(ModConstants.convertST2tempo(playBackTempo));
		inputStream.skip(4);

		final int patternCount = inputStream.readIntelUnsignedWord();
		int nSamples = inputStream.readIntelUnsignedWord();
		final int songLength = inputStream.readIntelUnsignedWord();
		if (nSamples>31) nSamples = 31;
		setNSamples(nSamples);
		inputStream.skip(6);
		setModID(getModID()+"/"+inputStream.readString(4));

		if (songLength>256 || patternCount>240) throw new IOException("Unsupported STX");

		// Orderlist:
		inputStream.seek((orderListPointer<<4)+0x20);
		allocArrangement(songLength);
		setSongLength(songLength);
		for (int i=0; i<songLength; i++)
		{
			getArrangement()[i]=inputStream.read();
			inputStream.skip(4);
		}

		inputStream.seek(samplePointer<<4);
		final long [] paraSamples = new long[nSamples];
		for (int i=0; i<nSamples; i++) paraSamples[i] = (inputStream.readIntelUnsignedWord()<<4);
		inputStream.seek(patternPointer<<4);
		final long [] paraPattern = new long[patternCount];
		for (int i=0; i<patternCount; i++) paraPattern[i] = (inputStream.readIntelUnsignedWord()<<4);

		// Instruments
		setNInstruments(getNSamples());
		final InstrumentsContainer instrumentContainer = new InstrumentsContainer(this, 0, getNSamples());
		setInstrumentContainer(instrumentContainer);
		for (int i=0; i<getNSamples(); i++)
		{
			final Sample current = new Sample();
			current.setStereo(false); // Default
			current.setPanning(false);
			current.setDefaultPanning(128);
			current.setGlobalVolume(ModConstants.MAXSAMPLEVOLUME);

			inputStream.seek(paraSamples[i]);
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
				final int volume = inputStream.read();
				current.setVolume((volume>64)?64:volume);

				// Reserved
				inputStream.skip(2);

				// Flags: 1:Loop
				current.setFlags(inputStream.read());
				current.setLoopType(((current.flags&0x01)==0x01) ? ModConstants.LOOP_ON : 0);
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
			if (baseFreq<=0) baseFreq = ModConstants.BASEFREQUENCY;
			else
			if (baseFreq<1024) baseFreq = 1024;
			current.setBaseFrequency(baseFreq);

			// Unused space - GUS Address also a point here?
			inputStream.skip(12);

			// SampleName
			current.setName(inputStream.readString(28));

			if (instrumentType==1)
			{
				current.setSampleType(ModConstants.SM_PCMS);
				current.setStereo(false);
				inputStream.seek(sampleOffset);
				readSampleData(current, inputStream);
			}

			instrumentContainer.setSample(i, current);
		}

		// Pattern data
		if (headerPatternSize!=0x1A) // that would be "EOF"
		{
			inputStream.seek(paraPattern[0]);
			final int tmp = inputStream.readIntelUnsignedWord();
			if (headerPatternSize == tmp)
				subversion = 0;
		}
		setNPattern(patternCount);
		setNChannels(4);
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

			if (paraPattern[pattNum]==0) continue;
			inputStream.seek(paraPattern[pattNum]);
			if (subversion==0) inputStream.skip(2); // skip the pattern size in front of pattern
			setPattern(pattNum, inputStream);
		}
		cleanUpArrangement();
		setTrackerName("ST Music Interface Kit V1." + subversion);
	}
}
