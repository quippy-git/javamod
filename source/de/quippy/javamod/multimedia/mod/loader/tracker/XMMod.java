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
import de.quippy.javamod.multimedia.mod.loader.instrument.Instrument;
import de.quippy.javamod.multimedia.mod.loader.instrument.InstrumentsContainer;
import de.quippy.javamod.multimedia.mod.loader.instrument.Sample;
import de.quippy.javamod.multimedia.mod.loader.pattern.Pattern;
import de.quippy.javamod.multimedia.mod.loader.pattern.PatternContainer;
import de.quippy.javamod.multimedia.mod.loader.pattern.PatternElement;
import de.quippy.javamod.multimedia.mod.loader.pattern.PatternRow;
import de.quippy.javamod.multimedia.mod.midi.MidiMacros;
import de.quippy.javamod.system.Log;

/**
 * @author Daniel Becker
 * @since 26.05.2006
 */
public class XMMod extends ProTrackerMod
{
	private static final String[] MODFILEEXTENSION = new String [] 
   	{
   		"xm"
   	};
	/**
	 * Will be executed during class load
	 */
	static
	{
		ModuleFactory.registerModule(new XMMod());
	}

	private int version;
	private int headerSize;
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
	protected XMMod(String fileName)
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
	 * @since 26.05.2006
	 * @param currentElement
	 * @param inputStream
	 * @throws IOException
	 */
	private void setIntoPatternElement(PatternElement currentElement, ModfileInputStream inputStream) throws IOException
	{
		long pos = inputStream.getFilePointer();
		int lookahead = inputStream.read();
		inputStream.seek(pos);
		int flags = ((lookahead&0x80)!=0)? inputStream.read() : 0x1F; // Packed or not packed...
		if( (flags&0x01)!=0)
		{
			int period = 0;
			int noteIndex = inputStream.read();
			if (noteIndex==97) // Key Off!
			{
				noteIndex = period = ModConstants.KEY_OFF;
			}
			else
			if (noteIndex!=0)
			{
				if (noteIndex<97) noteIndex +=12;
				noteIndex -= 12;
				period = ModConstants.noteValues[noteIndex - 1];
			}
			currentElement.setNoteIndex(noteIndex);
			currentElement.setPeriod(period);
		}
		if( (flags&0x02)!=0 ) currentElement.setInstrument(inputStream.read()); // Inst
		if( (flags&0x04)!=0 )
		{
			int volume = inputStream.read();
			if (volume!=0)
			{
				if (volume<=0x50)
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
		}
		if( (flags&0x08)!=0 ) currentElement.setEffekt(inputStream.read()); // FX
		if( (flags&0x10)!=0 ) currentElement.setEffektOp(inputStream.read()); // FXP
	}
	/**
	 * Get the ModType
	 * @param kennung
	 * @return
	 */
	private boolean isXMMod(String kennung)
	{
		if (kennung.equals("Extended Module: ")) return true;
		if (kennung.toLowerCase().equals("extended module: ")) return true;
		return false;
	}
	/**
	 * @param inputStream
	 * @return true, if this is a protracker mod, false if this is not clear
	 * @see de.quippy.javamod.multimedia.mod.loader.Module#checkLoadingPossible(de.quippy.javamod.io.ModfileInputStream)
	 */
	@Override
	public boolean checkLoadingPossible(ModfileInputStream inputStream) throws IOException
	{
		String xmID = inputStream.readString(17);
		inputStream.seek(0);
		return isXMMod(xmID);
	}
	/**
	 * @param fileName
	 * @return
	 * @see de.quippy.javamod.multimedia.mod.loader.Module#getNewInstance(java.lang.String)
	 */
	@Override
	protected Module getNewInstance(String fileName)
	{
		return new XMMod(fileName);
	}
	/**
	 * @param inputStream
	 * @return
	 * @see de.quippy.javamod.multimedia.mod.loader.Module#loadModFile(java.io.DataInputStream)
	 */
	@Override
	public void loadModFileInternal(ModfileInputStream inputStream) throws IOException
	{
		setModType(ModConstants.MODTYPE_XM);
		setBaseVolume(ModConstants.MAXGLOBALVOLUME);
		setMixingPreAmp(ModConstants.DEFAULT_MIXING_PREAMP);

		// XM-ID:
		setModID(inputStream.readString(17));
		if (!isXMMod(getModID())) throw new IOException("Unsupported XM Module!");

		// Songname
		setSongName(inputStream.readString(20));
		// 0x1A:
		inputStream.skip(1);

		// Trackername
		String trackerName = inputStream.readString(20); 
		setTrackerName(trackerName.trim());
		
		// Version
		version = inputStream.readIntelUnsignedWord();
		if (version<0x0104) Log.info("XM-Version is below 0x0104... ");
		
		long LSEEK = inputStream.getFilePointer();
		// Header Size
		headerSize = inputStream.readIntelDWord();
		
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
		for (int i=0; i<256; i++) getArrangement()[i]=inputStream.read();
		
		inputStream.seek(LSEEK + headerSize);
		
		// Read the patternData
		PatternContainer patternContainer = new PatternContainer(getNPattern());
		for (int pattNum=0; pattNum<getNPattern(); pattNum++)
		{
			LSEEK = inputStream.getFilePointer();
			int patternHeaderSize = inputStream.readIntelDWord();
			int packingType = inputStream.read();
			if (packingType!=0) throw new IOException("Unknown pattern packing type: " + packingType);
			int rows = inputStream.readIntelUnsignedWord();
			int packedPatternDataSize = inputStream.readIntelUnsignedWord();
			inputStream.seek(LSEEK + patternHeaderSize);
			
			Pattern currentPattern = new Pattern(rows);
			if (packedPatternDataSize>0)
			{
				for (int row=0; row<rows; row++)
				{
					PatternRow currentRow = new PatternRow(getNChannels());
					for (int channel=0; channel<getNChannels(); channel++)
					{
						PatternElement currentElement = new PatternElement(pattNum, row, channel);
						setIntoPatternElement(currentElement, inputStream);
						currentRow.setPatternElement(channel, currentElement);
					}
					currentPattern.setPatternRow(row, currentRow);
				}
			}
			patternContainer.setPattern(pattNum, currentPattern);
		}
		setPatternContainer(patternContainer);
		
		InstrumentsContainer instrumentContainer = new InstrumentsContainer(this, getNInstruments(), 0);
		this.setInstrumentContainer(instrumentContainer);

		int sampleOffsetIndex = 0;
		// Read the instrument data
		for (int ins=0; ins<getNInstruments(); ins++)
		{
			int vibratoType = 0; 
			int vibratoSweep = 0;
			int vibratoDepth = 0;
			int vibratoRate = 0; 

			LSEEK = inputStream.getFilePointer();
			
			Instrument currentIns = new Instrument();

			 // Default for values from IT
			currentIns.setGlobalVolume(128);
			currentIns.setDefaultPan(-1);
			currentIns.setPitchPanSeparation(-1);
			currentIns.setNNA(-1);
			currentIns.setInitialFilterCutoff(-1);
			currentIns.setInitialFilterResonance(-1);
			currentIns.setRandomPanningVariation(-1);

			final int instrumentHeaderSize = inputStream.readIntelDWord();
			currentIns.setName(inputStream.readString(22));
			/*int insType = */inputStream.read();
			final int anzSamples = inputStream.readIntelUnsignedWord();
			
			final int [] sampleIndex = new int[96];
			final int [] noteIndex = new int[96];
			currentIns.setIndexArray(sampleIndex);
			currentIns.setNoteArray(noteIndex);
			if (anzSamples<=0) // if no samples, at least set to defaults
			{
				for (int i=0; i<96; i++)
				{
					sampleIndex[i] = 0;
					noteIndex[i] = 0x80 | i;
				}
			}
			else
			{
				setNSamples(getNSamples()+anzSamples);
				/*final int sampleHeaderSize = */inputStream.readIntelDWord();
				
				for (int i=0; i<96; i++)
				{
					sampleIndex[i] = inputStream.read() + sampleOffsetIndex + 1;
					noteIndex[i] = i;
				}
				
				final int [] volumeEnvelopePosition = new int[12];
				final int [] volumeEnvelopeValue = new int[12];
				for (int i=0; i<12; i++)
				{
					volumeEnvelopePosition[i] = inputStream.readIntelUnsignedWord();
					volumeEnvelopeValue[i] = inputStream.readIntelUnsignedWord();
				}
				final Envelope volumeEnvelope = new Envelope();
				volumeEnvelope.setPositions(volumeEnvelopePosition);
				volumeEnvelope.setValue(volumeEnvelopeValue);
				currentIns.setVolumeEnvelope(volumeEnvelope);
				
				final int [] panningEnvelopePosition = new int[12];
				final int [] panningEnvelopeValue = new int[12];
				for (int i=0; i<12; i++)
				{
					panningEnvelopePosition[i] = inputStream.readIntelUnsignedWord();
					panningEnvelopeValue[i] = inputStream.readIntelUnsignedWord();
				}
				final Envelope panningEnvelope = new Envelope();
				panningEnvelope.setPositions(panningEnvelopePosition);
				panningEnvelope.setValue(panningEnvelopeValue);
				currentIns.setPanningEnvelope(panningEnvelope);
				
				volumeEnvelope.setNPoints(inputStream.read());
				panningEnvelope.setNPoints(inputStream.read());
				
				volumeEnvelope.setSustainPoint(inputStream.read());
				volumeEnvelope.setLoopStartPoint(inputStream.read());
				volumeEnvelope.setLoopEndPoint(inputStream.read());
				
				panningEnvelope.setSustainPoint(inputStream.read());
				panningEnvelope.setLoopStartPoint(inputStream.read());
				panningEnvelope.setLoopEndPoint(inputStream.read());

				volumeEnvelope.setXMType(inputStream.read());
				panningEnvelope.setXMType(inputStream.read());
				
				vibratoType = inputStream.read();
				vibratoSweep = inputStream.read();
				vibratoDepth = inputStream.read();
				vibratoRate = inputStream.read();
				
				currentIns.setVolumeFadeOut(inputStream.readIntelUnsignedWord());
				
				// Reserved
				inputStream.skip(2);
			}
			inputStream.seek(LSEEK+instrumentHeaderSize);
			
			instrumentContainer.reallocSampleSpace(getNSamples());
			int sampleLoadingFlags = ModConstants.SM_PCMD; // XM save in deltas
			for (int samIndex=0; samIndex<anzSamples; samIndex++)
			{
				Sample current = new Sample();
				
				current.setVibratoType(vibratoType);
				current.setVibratoSweep(vibratoSweep);
				current.setVibratoDepth(vibratoDepth);
				current.setVibratoRate(vibratoRate);
				
				// Length
				current.setLength(inputStream.readIntelDWord());
				
				// Repeat start and stop
				int repeatStart  = inputStream.readIntelDWord();
				int repeatLength = inputStream.readIntelDWord();
				int repeatStop = repeatStart+repeatLength;
				
				// volume 64 is maximum
				int vol  = inputStream.read() & 0x7F;
				current.setVolume((vol>64)?64:vol);
				current.setGlobalVolume(ModConstants.MAXSAMPLEVOLUME);
				
				// finetune Value>0x7F means negative
				int fine = inputStream.read();
				fine = (fine>0x7F)?fine-0x100:fine;
				current.setFineTune(fine);
				current.setBaseFrequency(ModConstants.it_fineTuneTable[(fine>>4)+8]);
				
				current.setFlags(inputStream.read());
				int loopType = 0;
				if ((current.flags&0x03)!=0) loopType |= ModConstants.LOOP_ON;
				if ((current.flags&0x02)!=0) loopType |= ModConstants.LOOP_IS_PINGPONG;
				current.setLoopType(loopType);
				
				if ((current.flags&0x10)!=0)
				{
					sampleLoadingFlags |= ModConstants.SM_16BIT;
					current.length>>=1;
					repeatStart>>=1;
					repeatStop>>=1;
				}
				if ((current.flags&0x20)!=0)
				{
					sampleLoadingFlags |= ModConstants.SM_STEREO; // this is new, not standard. Support is easy, so why not!
					current.length>>=1;
					repeatStart>>=1;
					repeatStop>>=1;
				}
				current.setStereo((sampleLoadingFlags&ModConstants.SM_STEREO)!=0);

				current.setLoopStart(repeatStart);
				current.setLoopStop(repeatStop);
				current.setLoopLength(repeatStop-repeatStart);

				// Defaults for non-existent SustainLoop
				current.setSustainLoopStart(0);
				current.setSustainLoopStop(0);
				current.setSustainLoopLength(0);

				// Panning 0..255
				current.setPanning(inputStream.read());
				
				int transpose = inputStream.read();
				current.setTranspose((transpose>0x7F)?transpose-0x100:transpose);
				
				// Reserved
				current.XM_reserved = inputStream.readByte();
				
				// Samplename
				current.setName(inputStream.readString(22));
				
				instrumentContainer.setSample(samIndex + sampleOffsetIndex, current);
			}
			
			for (int samIndex=0; samIndex<anzSamples; samIndex++)
			{
				Sample current = instrumentContainer.getSample(samIndex + sampleOffsetIndex); 
				if (current.XM_reserved == 0xAD && (current.flags&0x30)==0) // OpenMPT ADPVM4 compression 
				{
//					sampleLoadingFlags = ModConstants.SM_ADPCM4;
//					current.length = ((current.length + 1)>>1) + 16;
					throw new IOException("ADPCM not supported");
				}
				current.setSampleType(sampleLoadingFlags);
				readSampleData(current, inputStream);
			}
			instrumentContainer.setInstrument(ins, currentIns);
			sampleOffsetIndex += anzSamples;
		}
		cleanUpArrangement();
		
		while (inputStream.getFilePointer() + 8 < inputStream.length())
		{
			final int marker = inputStream.readMotorolaDWord();
			final int len = inputStream.readIntelDWord();
			if (marker == 0x74657874) // 'text'
			{
				if (len < inputStream.getLength()) songMessage = inputStream.readString(len);
			}
			else
			if (marker == 0x4D494449) // 'MIDI'
			{
				midiMacros = new MidiMacros();
				// read the MidiMacros
				if (len==MidiMacros.SIZE_OF_SCTUCT && len < inputStream.getLength()) midiMacros.loadFrom(inputStream);
			}
			else
			{
				// Skip it
				inputStream.skip(len);
			}
		}
	}
}
