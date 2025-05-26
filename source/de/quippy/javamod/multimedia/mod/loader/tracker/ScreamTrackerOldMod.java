/*
 * @(#) ScreamTrackerOldMod.java
 *
 * Created on 07.05.2006 by Daniel Becker
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
import de.quippy.javamod.multimedia.mod.midi.MidiMacros;
import de.quippy.javamod.multimedia.mod.mixer.BasicModMixer;
import de.quippy.javamod.multimedia.mod.mixer.ScreamTrackerMixer;

/**
 * @author Daniel Becker
 * @since 07.05.2006
 */
public class ScreamTrackerOldMod extends Module
{
	private static final String[] MODFILEEXTENSION = new String []
	{
		"stm", "sts"
	};

	protected static final String S3M_ID = "SCRM";

	/**
	 * Will be executed during class load
	 */
	static
	{
		ModuleFactory.registerModule(new ScreamTrackerOldMod());
	}

	/**
	 * Constructor for ScreamTrackerOldMod
	 */
	public ScreamTrackerOldMod()
	{
		super();
	}
	/**
	 * Constructor for ScreamTrackerOldMod
	 */
	protected ScreamTrackerOldMod(final String fileName)
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
	 * @see de.quippy.javamod.multimedia.mod.loader.Module#getMidiConfig()
	 */
	@Override
	public MidiMacros getMidiConfig()
	{
		return null;
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
	 * @return true, if this is a Scream Tracker 2 mod, false if this is not clear
	 * @see de.quippy.javamod.multimedia.mod.loader.Module#checkLoadingPossible(de.quippy.javamod.io.ModfileInputStream)
	 */
	@Override
	public boolean checkLoadingPossible(final ModfileInputStream inputStream) throws IOException
	{
		inputStream.seek(0);
		// We should not be too picky about the !Scream!-Tag...
		// According to ModPlug: Magic bytes that have been found in the wild are !Scream!, BMOD2STM, WUZAMOD! and SWavePro.
		// But simply accepting any printable ASCII as ID leads to false positives - would need to check more
		// of the header
		final byte [] header = new byte[32];
		inputStream.read(header);
		if (//header[28]!=0x1A ||	// EOF - there seem to be exceptions to this rule
			header[29]!=2	||		// FileType == 2 (1: we do not load, but is valid STM!)
			header[30]!=2	||		// VerHi && VerMin we want to support
			(header[31]!=0 && header[31]!=10 && header[31]!=20 && header[31]!=21))
		{
			return false;
		}
		for (int c=20; c<28; c++)
		{
			if (header[c]<0x20 || header[c]>0x7E)
				return false;
		}
		// STX files could now produce false positives. So check for those as well
		inputStream.seek(0x3C);
		final String s3mID = inputStream.readString(4);
		inputStream.seek(0);
		return !s3mID.equals(S3M_ID);
	}
	/**
	 * @param fileName
	 * @return
	 * @see de.quippy.javamod.multimedia.mod.loader.Module#getNewInstance(java.lang.String)
	 */
	@Override
	protected Module getNewInstance(final String fileName)
	{
		return new ScreamTrackerOldMod(fileName);
	}
	/**
	 * Read the STM pattern data
	 * @param pattNum
	 * @param row
	 * @param channel
	 * @param note
	 * @return
	 */
	private void createNewPatternElement(final PatternContainer patternContainer, final int pattNum, final int row, final int channel, final int note)
	{
		final PatternElement pe = patternContainer.createPatternElement(pattNum, row, channel);

		pe.setInstrument((note&0xF80000)>>19);

		final int oktave = (note&0xF0000000)>>28;
		if (oktave!=-1)
		{
			final int ton = (note&0x0F000000)>>24;
			final int index = (oktave+3)*12+ton; // fit to it octaves
			pe.setPeriod((index<ModConstants.noteValues.length) ? ModConstants.noteValues[index] : 0);
			pe.setNoteIndex(index+1);
		}
		else
		{
			pe.setPeriod(0);
			pe.setNoteIndex(0);
		}

		pe.setEffekt((note&0xF00)>>8);
		pe.setEffektOp(note&0xFF);

		// All trackers say: "No effect memory" - but throwing them all out is a very bad idea!
		// For instance: if it is a porta2note effect, the effect is ignored when op is zero,
		// however, the note is not played instead.
		//if (pe.getEffektOp()==0) pe.setEffekt(0);

		final int volume =((note&0x70000)>>16) | ((note&0xF000)>>9);
		if (volume<=64)
		{
			pe.setVolumeEffekt(1);
			pe.setVolumeEffektOp(volume);
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
		setModType(ModConstants.MODTYPE_STM);
		setNSamples(31);
		setNChannels(4);
		setSongRestart(0);

		// Songname
		setSongName(inputStream.readString(20));

		// ID. Should be "!SCREAM!"
		setModID(inputStream.readString(8));

		// 0x1A as file end signal... read over
		inputStream.skip(1);
		// Type: 1=Song 2=MOD
		final int STMType = inputStream.read();
		if (STMType!=0x02) throw new IOException("Unsupported STM MOD (ID!=0x02)");

		// Version
		final int vHi = inputStream.read();
		final int vLow = inputStream.read();
		version = vHi<<4|vLow;
		setTrackerName("ScreamTracker V" + vHi + '.' + vLow);

		// is always mono
		//songFlags |= ModConstants.SONG_ISSTEREO;
		// default, so we do not need to check for IT
		songFlags |= ModConstants.SONG_ST2VIBRATO;
		songFlags |= ModConstants.SONG_ST2TEMPO;
		songFlags |= ModConstants.SONG_ITOLDEFFECTS;

		// PlaybackTemp
		int playBackTempo = inputStream.read();
		if (vLow<21) playBackTempo = ((playBackTempo/10)<<4)+(playBackTempo%10);
		setTempo((playBackTempo>>4)!=0?playBackTempo>>4:1);
		setBPMSpeed(ModConstants.convertST2tempo(playBackTempo));

		// count of pattern in arrangement
		int patternCount = inputStream.read();
		if (patternCount>64) patternCount = 64;
		setNPattern(patternCount);

		// Base volume
		setBaseVolume(inputStream.read()<<1);
//		final int preAmp = ModConstants.MAX_MIXING_PREAMP / getNChannels();
//		setMixingPreAmp((preAmp<ModConstants.MIN_MIXING_PREAMP)?ModConstants.MIN_MIXING_PREAMP:(preAmp>0x80)?0x80:preAmp);
		setMixingPreAmp(ModConstants.MIN_MIXING_PREAMP);

		// Skip these reserved bytes
		inputStream.skip(13);

		// Instruments
		setNInstruments(getNSamples());
		final InstrumentsContainer instrumentContainer = new InstrumentsContainer(this, 0, getNSamples());
		this.setInstrumentContainer(instrumentContainer);
		for (int i=0; i<getNSamples(); i++)
		{
			final Sample current = new Sample();
			// Samplename
			current.setName(inputStream.readString(12));
			current.setStereo(false); // Default

			// reserved
			inputStream.skip(1);

			// instrument Disk number, if song (not yet supported)
			final int diskNumber = inputStream.read();
			if (STMType==0x01) current.setName(current.name+" #"+diskNumber);

			// Reserved (Sample Beginning Offset?!)
			inputStream.skip(2);

			// Length
			current.setLength(inputStream.readIntelUnsignedWord());

			// Repeat start and stop
			final int repeatStart = inputStream.readIntelUnsignedWord();
			final int repeatStop = inputStream.readIntelUnsignedWord();

			if (repeatStart<repeatStop && repeatStop!=0xFFFF)
				current.setLoopType(ModConstants.LOOP_ON);
			else
				current.setLoopType(0);

			current.setLoopStart(repeatStart);
			current.setLoopStop(repeatStop);
			current.setLoopLength(repeatStop-repeatStart);

			// Defaults for non-existent SustainLoop
			current.setSustainLoopStart(0);
			current.setSustainLoopStop(0);
			current.setSustainLoopLength(0);

			// volume 64 is maximum
			final int vol  = inputStream.read() & 0x7F;
			current.setVolume((vol>64)?64:vol);
			current.setGlobalVolume(ModConstants.MAXSAMPLEVOLUME);

			// reserved
			inputStream.skip(1);

			// Defaults!
			current.setPanning(false);
			current.setDefaultPanning(128);

			// Base Frequency
			current.setFineTune(0);
			current.setTranspose(0);
			current.setBaseFrequency(inputStream.readIntelUnsignedWord());

			// Reserved
			inputStream.skip(4);

			// Length in Paragraphs. Ignoring:
			inputStream.skip(2);
			instrumentContainer.setSample(i, current);
		}

		// always space for 128 pattern... With STMs we need to guess the arrangement length
		allocArrangement(128);
		int currentSongLenth = -1;
		for (int i=0; i<128; i++)
		{
			final int nextPatternIndex = inputStream.read();
			getArrangement()[i]=nextPatternIndex;
			if (currentSongLenth==-1 && nextPatternIndex==99) currentSongLenth = i;
		}
		while (getArrangement()[currentSongLenth-1]>=getNPattern()) currentSongLenth--;
		setSongLength(currentSongLenth);

		final PatternContainer patternContainer = new PatternContainer(this, getNPattern(), 64, getNChannels());
		setPatternContainer(patternContainer);
		for (int pattNum=0; pattNum<getNPattern(); pattNum++)
		{
			for (int row=0; row<64; row++)
			{
				for (int channel=0; channel<getNChannels(); channel++)
				{
					createNewPatternElement(patternContainer, pattNum, row, channel, inputStream.readMotorolaDWord());
				}
			}
		}

		for (int i=0; i<getNSamples(); i++)
		{
			final Sample current = getInstrumentContainer().getSample(i);
			current.setSampleType(ModConstants.SM_PCMS);
			readSampleData(current, inputStream);
		}

		cleanUpArrangement();
	}
}
