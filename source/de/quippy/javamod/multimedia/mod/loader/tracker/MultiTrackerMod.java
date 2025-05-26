/*
 * @(#) MultiTrackerMod.java
 *
 * Created on 15.08.2022 by Daniel Becker
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
import de.quippy.javamod.multimedia.mod.midi.MidiMacros;
import de.quippy.javamod.multimedia.mod.mixer.BasicModMixer;
import de.quippy.javamod.multimedia.mod.mixer.ProTrackerMixer;

/**
 * @author Daniel Becker
 * @since 15.08.2022
 */
public class MultiTrackerMod extends ProTrackerMod
{
	private static final String[] MODFILEEXTENSION = new String []
   	{
   		"mtm"
   	};
	/**
	 * Will be executed during class load
	 */
	static
	{
		ModuleFactory.registerModule(new MultiTrackerMod());
	}

	protected int [] panningValue;
	private String songMessage;

	/**
	 * Constructor for MultiTrackerMod
	 */
	public MultiTrackerMod()
	{
		super();
	}
	/**
	 * Constructor for MultiTrackerMod
	 * @param fileName
	 */
	public MultiTrackerMod(final String fileName)
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
	 * @return
	 * @see de.quippy.javamod.multimedia.mod.loader.Module#getModMixer(int)
	 */
	@Override
	public BasicModMixer getModMixer(final int sampleRate, final int doISP, final int doNoLoops, final int maxNNAChannels)
	{
		return new ProTrackerMixer(this, sampleRate, doISP, doNoLoops, maxNNAChannels);
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
		return ModConstants.XM_AMIGA_TABLE;
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
	 * @return true, if this is a protracker mod, false if this is not clear
	 * @see de.quippy.javamod.multimedia.mod.loader.Module#checkLoadingPossible(de.quippy.javamod.io.ModfileInputStream)
	 */
	@Override
	public boolean checkLoadingPossible(final ModfileInputStream inputStream) throws IOException
	{
		final String id = inputStream.readString(3);
		inputStream.seek(0);
		return id.equals("MTM");
	}
	/**
	 * @param fileName
	 * @return
	 * @see de.quippy.javamod.multimedia.mod.loader.Module#getNewInstance(java.lang.String)
	 */
	@Override
	protected Module getNewInstance(final String fileName)
	{
		return new MultiTrackerMod(fileName);
	}
	/**
	 * @param inputStream
	 * @throws IOException
	 * @see de.quippy.javamod.multimedia.mod.loader.Module#loadModFileInternal(de.quippy.javamod.io.ModfileInputStream)
	 */
	@Override
	protected void loadModFileInternal(final ModfileInputStream inputStream) throws IOException
	{
		songFlags = ModConstants.SONG_AMIGALIMITS;
		songFlags |= ModConstants.SONG_ISSTEREO;
		setModType(ModConstants.MODTYPE_MOD); // MultiTracker mods are converted to ProTracker
		setTempo(6);
		setBPMSpeed(125);

		// ID
		final String id = inputStream.readString(3);
		setModID(id);
		// Tracker version
		final int version = inputStream.read();
		// ASCIIZ songname
		final String songname = inputStream.readString(20);
		setSongName(songname);
		// Number of tracks saved
		final int numTracks = inputStream.readIntelUnsignedWord();
		// Last pattern number saved
		final int lastPattern = inputStream.read();
		setNPattern(lastPattern + 1);
		// Last order number to play (songlength-1)
		final int lastOrder = inputStream.read();
		setSongLength(lastOrder + 1);
		// Length of comment field
		final int commentSize = inputStream.readIntelUnsignedWord();
		// Number of samples saved
		final int numSamples = inputStream.read();
		setNSamples(numSamples);
		// Attribute byte (unused)
		/*final int attribute = */inputStream.read();
		// Numbers of rows in every pattern (MultiTrackerMod itself does not seem to support values != 64)
		int beatsPerTrack = inputStream.read();
		if (beatsPerTrack==0) beatsPerTrack = 64;
		// Number of channels used
		final int numChannels = inputStream.read();
		setNChannels(numChannels);
		// Channel pan positions
		panningValue = new int[32];
		for (int ch=0; ch<32; ch++)
		{
			final int readByte = inputStream.read();
			panningValue[ch] = ((readByte & 0x0F) << 4) + 8;
		}

		// Sanity check
		if(!id.equals("MTM")|| version >= 0x20 || lastOrder > 127 || beatsPerTrack > 64 || numChannels > 32 || numChannels == 0)
			throw new IOException("Unsupported MultiTrackerMod MOD");

		setBaseVolume(ModConstants.MAXGLOBALVOLUME);
		final int preAmp = ModConstants.MAX_MIXING_PREAMP / getNChannels();
		setMixingPreAmp((preAmp<ModConstants.MIN_MIXING_PREAMP)?ModConstants.MIN_MIXING_PREAMP:(preAmp>0x80)?0x80:preAmp);

		setTrackerName("MultiTrackerMod V" + ((version>>4)&0x0F) + '.' + (version&0xF));

		setNInstruments(getNSamples());
		final InstrumentsContainer instrumentContainer = new InstrumentsContainer(this, 0, getNSamples());
		setInstrumentContainer(instrumentContainer);
		for (int i=0; i<getNSamples(); i++)
		{
			final Sample current = new Sample();
			// Samplename
			final String sampleName = inputStream.readString(22);

			// Length
			int length = inputStream.readIntelDWord();

			// Repeat start and stop
			int repeatStart  = inputStream.readIntelDWord();
			int repeatStop = inputStream.readIntelDWord();

			// finetune Value>0x7F means negative
			int fine = inputStream.read();

			// volume
			final int vol  = inputStream.read();

			final int sampleType = inputStream.read();
			if ((sampleType & 0x01)!=0) //16Bit:
			{
				length>>=1;
				repeatStart>>=1;
				repeatStop>>=1;
			}

			// Loops sanity check
			if (repeatStart + 4 >=repeatStop) repeatStart = repeatStop = 0;
			if (repeatStart<repeatStop)
				current.setLoopType(ModConstants.LOOP_ON);
			else
				current.setLoopType(0);

			current.setName(sampleName);
			current.setLength(length);

			current.setLoopStart(repeatStart);
			current.setLoopStop(repeatStop);
			current.setLoopLength(repeatStop-repeatStart);

			// Defaults for non-existent SustainLoop
			current.setSustainLoopStart(0);
			current.setSustainLoopStop(0);
			current.setSustainLoopLength(0);

			// setFineTune
			fine = (fine>0x7F)?fine-0x100:fine;
			current.setFineTune(fine);
			current.setBaseFrequency(ModConstants.IT_fineTuneTable[(fine>>4)+8]);
			current.setTranspose(0);

			// Voplume 64 is maximum
			current.setVolume((vol>64)?64:vol);
			current.setGlobalVolume(ModConstants.MAXSAMPLEVOLUME);

			// Defaults!
			current.setPanning(false);
			current.setDefaultPanning(128);

			// SampleData
			int flags = ModConstants.SM_PCMU;
			if ((sampleType & 0x01)!=0) flags|=ModConstants.SM_16BIT;
			current.setSampleType(flags);

			instrumentContainer.setSample(i, current);
		}

		// always space for 128 pattern...
		allocArrangement(128);
		for (int i=0; i<128; i++) getArrangement()[i]=inputStream.read();

		// now follow #tracks of 192 bytes, we will seek to them
		// during read, so memorize this file pointer...
		final long tracksStart = inputStream.getFilePointer();
		// and skip to pattern data:
		inputStream.skip(192 * numTracks);

		final PatternContainer patternContainer = new PatternContainer(this, getNPattern(), beatsPerTrack, getNChannels());
		setPatternContainer(patternContainer);
		for (int pattNum=0; pattNum<getNPattern(); pattNum++)
		{
			for (int chn=0; chn<32; chn++) // always read 32 track pointers!
			{
				final int track = inputStream.readIntelUnsignedWord();
				if (track==0 || track>numTracks || chn>=getNChannels())
				{
					if (chn<getNChannels()) // we need to fill with empty patternData:
					{
						for (int row=0; row<beatsPerTrack; row++)
						{
							patternContainer.createPatternElement(pattNum, row, chn);
						}
					}
				}
				else
				{
					final long resetToPos = inputStream.getFilePointer();
					inputStream.seek(tracksStart + (192 * (track - 1)));
					for (int row=0; row<beatsPerTrack; row++)
					{
						final PatternElement pe = patternContainer.createPatternElement(pattNum, row, chn);

						final int readArray = (inputStream.readByte()&0xFF)<<16 | (inputStream.readByte()&0xFF)<<8 | (inputStream.readByte()&0xFF);
						final int note     = (readArray & 0xFC0000)>>18;
						final int instr    = (readArray & 0x03F000)>>12;
						final int effekt   = (readArray & 0x000F00)>>8;
						final int effektOp = (readArray & 0x0000FF);
						if (note>0 && note<72)
						{
							final int noteIndex = note + 25;
							if (noteIndex<ModConstants.noteValues.length)
							{
								pe.setNoteIndex(noteIndex);
								pe.setPeriod(ModConstants.noteValues[noteIndex]);
							}
						}
						pe.setInstrument(instr);
						pe.setEffekt(effekt);
						pe.setEffektOp(effektOp);

						patternContainer.setPatternElement(pe);
					}
					inputStream.seek(resetToPos);
				}
			}

		}

		// read the comment, if there is any...
		if (commentSize>0)
		{
			int start = 0;
			int rest = 40;
			final int rows = commentSize / rest;
			final StringBuilder b = new StringBuilder(commentSize+rows); // length plus "\n"
			for (int i=0; i<rows; i++)
			{
				final String line = inputStream.readString(rest);
				b.append(line).append('\n');
				start+=rest;
				if ((start+rest)>commentSize) rest = commentSize-start;
			}
			songMessage = b.toString();
		}

		// read the sampleData
		for (int i=0; i<getNSamples(); i++)
		{
			final Sample current = getInstrumentContainer().getSample(i);
			readSampleData(current, inputStream);
		}
	}
}
