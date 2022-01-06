/*
 * @(#) Module.java
 * 
 * Created on 21.04.2006 by Daniel Becker
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
package de.quippy.javamod.multimedia.mod.loader;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import de.quippy.javamod.io.ModfileInputStream;
import de.quippy.javamod.multimedia.mod.ModConstants;
import de.quippy.javamod.multimedia.mod.loader.instrument.InstrumentsContainer;
import de.quippy.javamod.multimedia.mod.loader.instrument.Sample;
import de.quippy.javamod.multimedia.mod.loader.pattern.PatternContainer;
import de.quippy.javamod.multimedia.mod.midi.MidiMacros;
import de.quippy.javamod.multimedia.mod.mixer.BasicModMixer;
import de.quippy.javamod.system.Log;

/**
 * @author Daniel Becker
 * @since 21.04.2006
 */
public abstract class Module
{
	private String fileName;
	private String trackerName;
	private String modID;
	
	private int modType;
	protected int version; // so far only used to recognize OMPT versions

	private String songName;
	private int nChannels;
	private int nInstruments;
	private int nSamples;
	private int nPattern;
	private int BPMSpeed;
	private int tempo;
	private InstrumentsContainer instrumentContainer;
	private PatternContainer patternContainer;
	private int songLength;
	private int songRestart;
	private long lengthInMilliseconds;
	private int [] arrangement;
	private boolean [] arrangementPositionPlayed;
	private int baseVolume; // 0..128
	private int mixingPreAmp; //0..128
	
	protected int songFlags;
	
	/**
	 * This class is used to decrompress the IT>=2.14 samples
	 * It is a mix from open cubic player and mod plug tracker adopted for
	 * Java by Daniel Becker
	 * 
	 * Read, what Tammo Hinrichs (OCP) wrote to this:
	 * ********************************************************
	 * And to make it even worse: A short (?) description of what the routines
	 * in this file do.
	 * 
	 * It's all about sample compression. Due to the rather "analog" behaviour
	 * of audio streams, it's not always possible to gain high reduction rates
	 * with generic compression algorithms. So the idea is to find an algorithm
	 * which is specialized for the kind of data we're actually dealing with:
	 * mono sample data.
	 * 
	 * in fact, PKZIP etc. is still somewhat better than this algorithm in most
	 * cases, but the advantage of this is it's decompression speed which might
	 * enable sometimes players or even synthesizer chips to decompress IT
	 * samples in real-time. And you can still pack these compressed samples with
	 * "normal" algorithms and get better results than these algorothms would
	 * ever achieve alone.
	 *
	 * some assumptions i made (and which also pulse made - and without which it
	 * would have been impossible for me to figure out the algorithm) :
	 *
	 * - it must be possible to find values which are found more often in the
	 *   file than others. Thus, it's possible to somehow encode the values
	 *   which we come across more often with less bits than the rest.
	 * - In general, you can say that low values (considering distance to
	 *   the null line) are found more often, but then, compression results
	 *   would heavily depend on signal amplitude and DC offsets and such.
	 * - But: ;)
	 * - higher frequencies have generally lower amplitudes than low ones, just
	 *   due to the nature of sound and our ears
	 * - so we could somehow filter the signal to decrease the low frequencies'
	 *   amplitude, thus resulting in lesser overall amplitude, thus again resul-
	 *   ting in better ratios, if we take the above thoughts into consideration.
	 * - every signal can be split into a sum of single frequencies, that is a
	 *   sum of a(f)*sin(f*t) terms (just believe me if you don't already know).
	 * - if we differentiate this sum, we get a sum of (a(f)*f)*cos(f*t). Due to
	 *   f being scaled to the nyquist of the sample frequency, it's always
	 *   between 0 and 1, and we get just what we want - we decrease the ampli-
	 *   tude of the low frequencies (and shift the signal's phase by 90�, but
	 *   that's just a side-effect that doesn't have to interest us)
	 * - the backwards way is simple integrating over the data and is completely
	 *   lossless. good.
	 * - so how to differentiate or integrate a sample stream? the solution is
	 *   simple: we simply use deltas from one sample to the next and have the
	 *   perfectly numerically differentiated curve. When we decompress, we
	 *   just add the value we get to the last one and thus restore the original
	 *   signal.
	 * - then, we assume that the "-1"st sample value is always 0 to avoid nasty
	 *   DC offsets when integrating.
	 *   
	 * ok. now we have a sample stream which definitely contains more low than
	 * high values. How do we compress it now?
	 * 
	 * Pulse had chosen a quite unusual, but effective solution: He encodes the
	 * values with a specific "bit width" and places markers between the values
	 * which indicate if this width would change. He implemented three different
	 * methods for that, depending on the bit width we actually have (i'll write
	 * it down for 8 bit samples, values which change for 16bit ones are in these
	 * brackets [] ;):
	 * 
	 * * method 1: 1 to 6 bits
	 *   there are two possibilities (example uses a width of 6)
	 *   - 100000 (a one with (width-1) zeroes ;) :
	 *     the next 3 [4] bits are read, incremented and used as new width...
	 *     and as it would be completely useless to switch to the same bit
	 *     width again, any value equal or greater the actual width is
	 *     incremented, thus resulting in a range from 1-9 [1-17] bits (which
	 *     we definitely need).
	 *   - any other value is expanded to a signed byte [word], integrated
	 *     and stored.
	 * * method 2: 7 to 8 [16] bits
	 *   again two possibilities (this time using a width of eg. 8 bits)
	 *   - 01111100 to 10000011 [01111000 to 10000111] :
	 *     this value will be subtracted by 01111011 [01110111], thus resulting
	 *     again in a 1-8 [1-16] range which will be expanded to 1-9 [1-17] in
	 *     the same manner as above
	 *   - any other value is again expanded (if necessary), integrated and
	 *     stored
	 * * method 3: 9 [17] bits
	 *   this time it depends on the highest bit:
	 *   - if 0, the last 8 [16] bits will be integrated and stored
	 *   - if 1, the last 8 [16] bits (+1) will be used as new bit width.
	 * any other width isnt supposed to exist and will result in a premature
	 * exit of the decompressor.
	 * 
	 * Few annotations:
	 * - The compressed data is processed in blocks of 0x8000 bytes. I dont
	 *   know the reason of this (it's definitely NOT better concerning compres-
	 *   sion ratio), i just think that it has got something to do with Pulse's
	 *   EMS memory handling or such. Anyway, this was really nasty to find
	 *   out ;)
	 * - The starting bit width is 9 [17]
	 * - IT2.15 compression simply doubles the differentiation/integration
	 *   of the signal, thus eliminating low frequencies some more and turning
	 *   the signal phase to 180� instead of 90� which can eliminate some sig-
	 *   nal peaks here and there - all resulting in a somewhat better ratio.
	 * 
	 * ok, but now lets start... but think before you easily somehow misuse
	 * this code, the algorithm is (C) Jeffrey Lim aka Pulse... and my only
	 * intention is to make IT's file format more open to the Tracker Community
	 * and especially the rest of the scene. Trackers ALWAYS were open standards,
	 * which everyone was able (and WELCOME) to adopt, and I don't think this
	 * should change. There are enough other things in the computer world
	 * which did, let's just not be mainstream, but open-minded. Thanks.
	 * 
	 *                     Tammo Hinrichs [ KB / T.O.M / PuRGE / Smash Designs ]
	 * 
	 * @author Daniel Becker
	 * @since 03.11.2007
	 */
	private static class ITDeCompressor
	{
		// StreamData
		private ModfileInputStream input;
		// Block of Data
		private byte[] sourceBuffer;
		private int sourceIndex;
		// Destination (24Bit signed mono!)
		private long[] destBuffer;
		private int destIndex;
		// Samples to fill
		private int anzSamples;
		// Bits remaining
		private int bitsRemain;
		// true, if we have IT Version >2.15 packed Data
		private boolean isIT215;

		public ITDeCompressor(final long [] buffer, final int length, boolean isIT215, ModfileInputStream inputStream)
		{
			this.input = inputStream;
			this.sourceBuffer = null;
			this.sourceIndex = 0;
			this.bitsRemain = 0;
			this.destBuffer = buffer;
			this.destIndex = 0;
			this.anzSamples = length;
			this.isIT215 = isIT215;
		}

		/**
		 * reads b bits from the stream
		 * Works for 8 bit streams but 8 or 16 bit samples
		 * @since 03.11.2007
		 * @param b
		 * @return
		 */
		private int readbits(int b)
		{
			// Slow version but always working and easy to understand
//			long value = 0;
//			int i = b;
//			while (i>0)
//			{
//				if (bitsRemain==0)
//				{
//					sourceIndex++;
//					bitsRemain = 8;
//				}
//				value >>= 1;
//				value |= (((long)sourceBuffer[sourceIndex] & 0x01) << 31) & 0xFFFFFFFF;
//				sourceBuffer[sourceIndex] >>= 1;
//				bitsRemain--;
//				i--;
//			}
//			return (int)((value >> (32 - b)) & 0xFFFFFFFF);
			// adopted version vom OCP - much faster
			long value = 0;
			if (b <= bitsRemain)
			{
				value = sourceBuffer[sourceIndex] & ((1 << b) - 1);
				sourceBuffer[sourceIndex] >>= b;
				bitsRemain -= b;
			}
			else
			{
				int nbits = b - bitsRemain;
				value = ((long)sourceBuffer[sourceIndex++]) & ((1 << bitsRemain) - 1);
				while (nbits>8)
				{
					value |= ((long)(sourceBuffer[sourceIndex++] & 0xFF)) << bitsRemain;
					nbits-=8; bitsRemain += 8;
				}
				value |= ((long)(sourceBuffer[sourceIndex] & ((1 << nbits) - 1))) << bitsRemain;
				sourceBuffer[sourceIndex] >>= nbits;
				bitsRemain = 8 - nbits;
			}
			return (int)(value & 0xFFFFFFFF);
		}

		/**
		 * gets block of compressed data from file
		 * 
		 * @since 03.11.2007
		 * @return
		 */
		private boolean readblock() throws IOException
		{
			if (input.available()==0) return false; // EOF?!
			int size = input.readIntelWord();
			if (size == 0) return false;
			if (input.available()<size) size = input.available(); // Dirty Hack - should never happen
			
			sourceBuffer = new byte[size];
			input.read(sourceBuffer, 0, size);
			sourceIndex = 0;
			bitsRemain = 8;
			return true;
		}

		/**
		 * This will decompress to 8 Bit samples
		 * @since 03.11.2007
		 * @return
		 */
		public boolean decompress8() throws IOException
		{
			int blklen;		// length of compressed data block in samples
			int blkpos;		// position in block
			int width;		// actual "bit width"
			int value;		// value read from file to be processed
			byte d1, d2;	// integrator buffers (d2 for it2.15)

			// now unpack data till the dest buffer is full
			while (anzSamples > 0)
			{
				// read a new block of compressed data and reset variables
				if (!readblock()) return false;
				blklen = (anzSamples < 0x8000) ? anzSamples : 0x8000;
				blkpos = 0;

				width = 9; // start with width of 9 bits
				d1 = d2 = 0; // reset integrator buffers
				// now uncompress the data block
				while (blkpos < blklen)
				{
					value = readbits(width); // read bits

					if (width < 7) // method 1 (1-6 bits)
					{
						if (value == (1 << (width - 1))) // check for "100..."
						{
							value = readbits(3) + 1; // yes -> read new width;
							width = (value < width) ? value : value + 1; // and expand it
							continue; // ... next value
						}
					}
					else if (width < 9) // method 2 (7-8 bits)
					{
						int border = (0xFF >> (9 - width)) - 4; // lower border for width chg

						if (value > border && value <= (border + 8))
						{
							value -= border; // convert width to 1-8
							width = (value < width) ? value : value + 1; // and expand it
							continue; // ... next value
						}
					}
					else if (width == 9) // method 3 (9 bits)
					{
						if ((value & 0x100) != 0) // bit 8 set?
						{
							width = (value + 1) & 0xFF; // new width...
							continue; // ... and next value
						}
					}
					else
					// illegal width, abort
					{
						return false;
					}

					// now expand value to signed byte
					byte v; // sample value
					if (width < 8)
					{
						int shift = 8 - width;
						v = (byte)((value << shift)&0xFF);
						v >>= shift;
					}
					else
						v = (byte)(value & 0xFF);

					// integrate upon the sample values
					d1 += v;
					d2 += d1;

					// ... and store it into the buffer
					this.destBuffer[destIndex++] = ModConstants.promoteSigned8BitToSigned32Bit((isIT215) ? d2 : d1);
					blkpos++;
				}

				// now subtract block lenght from total length and go on
				anzSamples -= blklen;
			}

			return true;
		}
		/**
		 * This will decompress to 16 Bit samples
		 * @since 03.11.2007
		 * @return
		 */
		public boolean decompress16() throws IOException
		{
			int blklen;		// length of compressed data block in samples
			int blkpos;		// position in block
			int width;		// actual "bit width"
			int value;		// value read from file to be processed
			short d1, d2;	// integrator buffers (d2 for it2.15)

			// now unpack data till the dest buffer is full
			while (anzSamples > 0)
			{
				// read a new block of compressed data and reset variables
				if (!readblock()) return false;
				blklen = (anzSamples < 0x4000) ? anzSamples : 0x4000; // 0x4000 samples => 0x8000 bytes again
				blkpos = 0;

				width = 17; // start with width of 17 bits
				d1 = d2 = 0; // reset integrator buffers

				// now uncompress the data block
				while (blkpos < blklen)
				{
					value = readbits(width); // read bits

					if (width < 7) // method 1 (1-6 bits)
					{
						if (value == (1 << (width - 1))) // check for "100..."
						{
							value = readbits(4) + 1; // yes -> read new width;
							width = (value < width) ? value : value + 1; // and expand it
							continue; // ... next value
						}
					}
					else if (width < 17) // method 2 (7-16 bits)
					{
						int border = (0xFFFF >> (17 - width)) - 8; // lower border for width chg

						if (value > border && value <= (border + 16))
						{
							value -= border; // convert width to 1-8
							width = (value < width) ? value : value + 1; // and expand it
							continue; // ... next value
						}
					}
					else if (width == 17) // method 3 (17 bits)
					{
						if ((value & 0x10000) != 0) // bit 16 set?
						{
							width = (value + 1) & 0xFF; // new width...
							continue; // ... and next value
						}
					}
					else
					// illegal width, abort
					{
						return false;
					}

					// now expand value to signed word
					short v; // sample value
					if (width < 16)
					{
						int shift = 16 - width;
						v = (short) ((value << shift) & 0xFFFF);
						v >>= shift;
					}
					else
						v = (short) value;

					// integrate upon the sample values
					d1 += v;
					d2 += d1;

					// ... and store it into the buffer
					this.destBuffer[destIndex++] = ModConstants.promoteSigned16BitToSigned32Bit((isIT215) ? d2 : d1);
					blkpos++;
				}

				// now subtract block lenght from total length and go on
				anzSamples -= blklen;
			}

			return true;
		}
	}
	
	/**
	 * Constructor for Module
	 */
	public Module()
	{
		super();
		lengthInMilliseconds = -1;
	}
	/**
	 * Constructor for Module
	 */
	protected Module(final String fileName)
	{
		this();
		this.fileName = fileName;
	}
	/**
	 * Loads a Module. This Method will delegate the task to loadModFile(InputStream)
	 * 
	 * @param fileName
	 * @return
	 */
	public Module loadModFile(final String fileName) throws IOException
	{
		return loadModFile(new File(fileName));
	}
	/**
	 * Loads a Module.
	 * This Method will delegate the task to loadModFile(URL)
	 * @param file
	 * @return
	 */
	public Module loadModFile(final File file) throws IOException
	{
		return loadModFile(file.toURI().toURL());
	}
	/**
	 * @since 12.10.2007
	 * @param url
	 * @return
	 */
	public Module loadModFile(final URL url) throws IOException
	{
		ModfileInputStream inputStream = null;
		try
		{
			inputStream = new ModfileInputStream(url);
			return loadModFile(inputStream);
		}
		finally
		{
			if (inputStream!=null) try { inputStream.close(); } catch (Exception ex) { Log.error("IGNORED", ex); }
		}
	}
	/**
	 * @since 31.12.2007
	 * @param inputStream
	 * @return
	 * @throws IOException
	 */
	public Module loadModFile(final ModfileInputStream inputStream) throws IOException
	{
		Module mod = this.getNewInstance(inputStream.getFileName());
		mod.loadModFileInternal(inputStream);
		return mod;
	}
	/**
	 * Loads samples
	 * @since 03.11.2007
	 * @param current
	 * @param flags
	 * @param input
	 * @param offset
	 * @return the new offset after loading
	 */
	protected void readSampleData(final Sample current, final ModfileInputStream inputStream) throws IOException
	{
		final int flags = current.sampleType;
		final boolean isStereo = (flags&ModConstants.SM_STEREO)!=0;
		final boolean isUnsigned = (flags&ModConstants.SM_PCMU)!=0;
		//current.setStereo(isStereo); // just to be sure...
		
		if (current.length>0)
		{
			current.allocSampleData();
			if ((flags & ModConstants.SM_IT21416)==ModConstants.SM_IT21416 || (flags & ModConstants.SM_IT21516)==ModConstants.SM_IT21516)
			{
				final ITDeCompressor reader = new ITDeCompressor(current.sampleL, current.length, (flags & ModConstants.SM_IT21516)==ModConstants.SM_IT21516, inputStream);
				reader.decompress16();
				if (isStereo)
				{
					final ITDeCompressor reader2 = new ITDeCompressor(current.sampleR, current.length, (flags & ModConstants.SM_IT21516)==ModConstants.SM_IT21516, inputStream);
					reader2.decompress16();
				}
			}
			else
			if ((flags & ModConstants.SM_IT2148)==ModConstants.SM_IT2148 || (flags & ModConstants.SM_IT2158)==ModConstants.SM_IT2158)
			{
				final ITDeCompressor reader = new ITDeCompressor(current.sampleL, current.length, (flags & ModConstants.SM_IT2158)==ModConstants.SM_IT2158, inputStream);
				reader.decompress8();
				if (isStereo)
				{
					final ITDeCompressor reader2 = new ITDeCompressor(current.sampleR, current.length, (flags & ModConstants.SM_IT2158)==ModConstants.SM_IT2158, inputStream);
					reader2.decompress8();
				}
			}
			else
			if ((flags&ModConstants.SM_PCM16D)==ModConstants.SM_PCM16D)
			{
				short delta = 0;
				for (int s=0; s<current.length; s++)
					current.sampleL[s] = ModConstants.promoteSigned16BitToSigned32Bit((long)(delta += inputStream.readIntelWord()));
				if (isStereo)
				{
					delta = 0;
					for (int s=0; s<current.length; s++)
						current.sampleR[s] = ModConstants.promoteSigned16BitToSigned32Bit((long)(delta += inputStream.readIntelWord()));
				}
			}
			else
			if ((flags&ModConstants.SM_PCMD)==ModConstants.SM_PCMD)
			{
				byte delta = 0;
				for (int s=0; s<current.length; s++)
					current.sampleL[s] = ModConstants.promoteSigned8BitToSigned32Bit((long)(delta += inputStream.readByte()));
				if (isStereo)
				{
					delta = 0;
					for (int s=0; s<current.length; s++)
						current.sampleR[s] = ModConstants.promoteSigned8BitToSigned32Bit((long)(delta += inputStream.readByte()));
				}
			}
			else
			if ((flags&ModConstants.SM_16BIT)!=0) // 16 Bit PCM Samples
			{
				for (int s=0; s<current.length; s++)
				{
					final short sample = inputStream.readIntelWord();
					if (isUnsigned) // unsigned
						current.sampleL[s]=ModConstants.promoteUnsigned16BitToSigned32Bit((long)sample);
					else
						current.sampleL[s]=ModConstants.promoteSigned16BitToSigned32Bit((long)sample);
				}
				if (isStereo)
				{
					for (int s=0; s<current.length; s++)
					{
						final short sample = inputStream.readIntelWord();
						if (isUnsigned) // unsigned
							current.sampleR[s]=ModConstants.promoteUnsigned16BitToSigned32Bit((long)sample);
						else
							current.sampleR[s]=ModConstants.promoteSigned16BitToSigned32Bit((long)sample);
					}
				}
			}
			else // 8 Bit Samples, singed or unsigned
			{
				for (int s=0; s<current.length; s++)
				{
					final byte sample = inputStream.readByte();
					if (isUnsigned) // unsigned
						current.sampleL[s]=ModConstants.promoteUnsigned8BitToSigned32Bit((long)sample);
					else
						current.sampleL[s]=ModConstants.promoteSigned8BitToSigned32Bit((long)sample);
				}
				if (isStereo)
				{
					for (int s=0; s<current.length; s++)
					{
						final byte sample = inputStream.readByte();
						if (isUnsigned) // unsigned
							current.sampleR[s]=ModConstants.promoteUnsigned8BitToSigned32Bit((long)sample);
						else
							current.sampleR[s]=ModConstants.promoteSigned8BitToSigned32Bit((long)sample);
					}
				}
				
			}
			current.fixSampleLoops(getModType());
		}
	}
	/**
	 * Returns true if the loader thinks this mod can be loaded by him
	 * @since 10.01.2010
	 * @param inputStream
	 * @return
	 * @throws IOException
	 */
	public abstract boolean checkLoadingPossible(final ModfileInputStream inputStream) throws IOException;
	/**
	 * Create an Instance of your own - is used by loadModFile before loadModFileInternal is called
	 * @since 10.01.2010
	 * @return
	 */
	protected abstract Module getNewInstance(final String fileName);
	/**
	 * @since 31.12.2007
	 * @param inputStream
	 * @return
	 * @throws IOException
	 */
	protected abstract void loadModFileInternal(final ModfileInputStream inputStream) throws IOException;
	/**
	 * @return Returns the mixer.
	 */
	public abstract BasicModMixer getModMixer(final int sampleRate, final int doISP, final int doNoLoops, final int maxNNAChannels);
	/**
	 * Retrieve the file extension list this loader/player is used for
	 */
	public abstract String [] getFileExtensionList();
	/**
	 * @since 22.07.2020
	 * @return 0..128 (0-> results in mono, 128 is wide)
	 */
	public abstract int getPanningSeparation();
	/**
	 * Give panning value 0..256 (128 is center)
	 * @param channel
	 * @return
	 */
	public abstract int getPanningValue(final int channel);
	/**
	 * Give the channel volume for this channel. 0->64
	 * @since 25.06.2006
	 * @param channel
	 * @return
	 */
	public abstract int getChannelVolume(final int channel);
	/**
	 * Return value from Helpers, section "The frequency tables supported"
	 * Return 1: XM IT AmigaMod Table
	 * Return 2: XM IT Linear Frequency Table
	 * @return
	 */
	public abstract int getFrequencyTable();
	/**
	 * Returns the IT / XM Song Message, if any
	 * @since 15.06.2020
	 * @return
	 */
	public abstract String getSongMessage();
	/**
	 * For XMs and IT, return the midi config
	 * @since 15.06.2020
	 */
	public abstract MidiMacros getMidiConfig();
	
	/**
	 * @since 25.06.2006
	 * @param length
	 */
	protected void allocArrangement(final int length)
	{
		arrangement = new int[length];
		arrangementPositionPlayed = new boolean[length];
	}
	/**
	 * @return Returns the arrangement.
	 */
	public int[] getArrangement()
	{
		return arrangement;
	}
	/**
	 * @param arrangement The arrangement to set.
	 */
	public void setArrangement(final int[] arrangement)
	{
		this.arrangement = arrangement;
	}
	/**
	 * Automatically cleans up the arrangement data (if illegal pattnums
	 * are in there...)
	 * @since 03.10.2010
	 */
	public void cleanUpArrangement()
	{
		int illegalPatternNum = 0;
		for (int i=0; i<songLength; i++)
		{
			if (arrangement[i-illegalPatternNum]>=nPattern)
			{
				illegalPatternNum++;
				System.arraycopy(arrangement, i+1, arrangement, i, arrangement.length - i - 1);
			}
		}
		songLength -= illegalPatternNum;
	}
	public void resetLoopRecognition()
	{
		for (int i=0; i<arrangementPositionPlayed.length; i++) arrangementPositionPlayed[i] = false;
		getPatternContainer().resetRowsPlayed();
	}
	public boolean isArrangementPositionPlayed(final int position)
	{
		return arrangementPositionPlayed[position];
	}
	public void setArrangementPositionPlayed(final int position)
	{
		arrangementPositionPlayed[position] = true;
	}
	/**
	 * @return Returns the bPMSpeed.
	 */
	public int getBPMSpeed()
	{
		return BPMSpeed;
	}
	/**
	 * @param speed The bPMSpeed to set.
	 */
	protected void setBPMSpeed(final int speed)
	{
		BPMSpeed = speed;
	}
	/**
	 * @return Returns the instruments.
	 */
	public InstrumentsContainer getInstrumentContainer()
	{
		return instrumentContainer;
	}
	/**
	 * @param instruments The instruments to set.
	 */
	protected void setInstrumentContainer(final InstrumentsContainer instrumentContainer)
	{
		this.instrumentContainer = instrumentContainer;
	}
	/**
	 * @return Returns the nChannels.
	 */
	public int getNChannels()
	{
		return nChannels;
	}
	/**
	 * @param channels The nChannels to set.
	 */
	protected void setNChannels(final int channels)
	{
		nChannels = channels;
	}
	/**
	 * @return Returns the nPattern.
	 */
	public int getNPattern()
	{
		return nPattern;
	}
	/**
	 * @param pattern The nPattern to set.
	 */
	protected void setNPattern(final int pattern)
	{
		nPattern = pattern;
	}
	/**
	 * @return Returns the nInstruments.
	 */
	public int getNInstruments()
	{
		return nInstruments;
	}
	/**
	 * @param samples The nInstruments to set.
	 */
	protected void setNInstruments(final int instruments)
	{
		nInstruments = instruments;
	}
	/**
	 * @return Returns the nSamples.
	 */
	public int getNSamples()
	{
		return nSamples;
	}
	/**
	 * @param samples The nSamples to set.
	 */
	protected void setNSamples(final int samples)
	{
		nSamples = samples;
	}
	/**
	 * @return Returns the songLength.
	 */
	public int getSongLength()
	{
		return songLength;
	}
	/**
	 * @param songLength The songLength to set.
	 */
	protected void setSongLength(final int songLength)
	{
		this.songLength = songLength;
	}
	/**
	 * @return the songRestart
	 */
	public int getSongRestart()
	{
		return songRestart;
	}
	/**
	 * @param songRestart the songRestart to set
	 */
	protected void setSongRestart(int songRestart)
	{
		this.songRestart = songRestart;
	}
	/**
	 * @return Returns the songName.
	 */
	public String getSongName()
	{
		return songName;
	}
	/**
	 * @param songName The songName to set.
	 */
	protected void setSongName(final String songName)
	{
		this.songName = songName;
	}
	/**
	 * @return Returns the tempo.
	 */
	public int getTempo()
	{
		return tempo;
	}
	/**
	 * @param tempo The tempo to set.
	 */
	protected void setTempo(final int tempo)
	{
		this.tempo = tempo;
	}
	/**
	 * @return Returns the trackerName.
	 */
	public String getTrackerName()
	{
		return trackerName;
	}
	/**
	 * @param trackerName The trackerName to set.
	 */
	protected void setTrackerName(final String trackerName)
	{
		this.trackerName = trackerName;
	}
	/**
	 * @return Returns the patternContainer.
	 */
	public PatternContainer getPatternContainer()
	{
		return patternContainer;
	}
	/**
	 * @param patternContainer The patternContainer to set.
	 */
	protected void setPatternContainer(final PatternContainer patternContainer)
	{
		this.patternContainer = patternContainer;
	}
	/**
	 * @return the fileName
	 */
	public String getFileName()
	{
		return fileName;
	}
	/**
	 * @return Returns the modID.
	 */
	public String getModID()
	{
		return modID;
	}
	/**
	 * @param modID The modID to set.
	 */
	protected void setModID(final String modID)
	{
		this.modID = modID;
	}
	/**
	 * @return Returns the baseVolume (0..128)
	 */
	public int getBaseVolume()
	{
		return baseVolume;
	}
	/**
	 * @param baseVolume The baseVolume to set.
	 */
	protected void setBaseVolume(final int baseVolume)
	{
		this.baseVolume = baseVolume;
	}
	/**
	 * @return the mixingPreAmp (0..128)
	 */
	public int getMixingPreAmp()
	{
		return mixingPreAmp;
	}
	/**
	 * @param mixingPreAmp The mixing Pre-Amp to set
	 */
	protected void setMixingPreAmp(final int mixingPreAmp)
	{
		this.mixingPreAmp = mixingPreAmp;
	}
	/**
	 * @return the songFlags
	 */
	public int getSongFlags()
	{
		return songFlags;
	}
	/**
	 * @param songFlags the songFlags to set
	 */
	protected void setSongFlags(final int songFlags)
	{
		this.songFlags = songFlags;
	}
	/**
	 * @return Returns the modType.
	 */
	public int getModType()
	{
		return modType;
	}
	/**
	 * @param modType The modType to set.
	 */
	protected void setModType(final int modType)
	{
		this.modType = modType;
	}
	/**
	 * @return the version
	 */
	public int getVersion()
	{
		return version;
	}
	/**
	 * @param version the version to set
	 */
	public void setVersion(int version)
	{
		this.version = version;
	}
	/**
	 * @return the lenthInMilliseconds
	 */
	public long getLengthInMilliseconds()
	{
		return lengthInMilliseconds;
	}
	/**
	 * @param lenthInMilliseconds the lenthInMilliseconds to set
	 */
	public void setLengthInMilliseconds(final long lengthInMilliseconds)
	{
		this.lengthInMilliseconds = lengthInMilliseconds;
	}
	/**
	 * @since 29.03.2010
	 * @return
	 */
	public String toShortInfoString()
	{
		StringBuilder modInfo = new StringBuilder(getTrackerName());
		modInfo.append(((songFlags&ModConstants.SONG_ISSTEREO)!=0)?" (stereo) ":" (mono) ").append(" mod with ").append(getNSamples()).append(" samples and ").append(getNChannels()).append(" channels using ");
		switch (getFrequencyTable())
		{
			case ModConstants.AMIGA_TABLE: modInfo.append("Protracker"); break;
			case ModConstants.STM_S3M_TABLE: modInfo.append("Scream Tracker"); break;
			case ModConstants.XM_AMIGA_TABLE: modInfo.append("Fast Tracker log"); break;
			case ModConstants.XM_LINEAR_TABLE: modInfo.append("Fast Tracker linear"); break;
			case ModConstants.IT_LINEAR_TABLE: modInfo.append("Impulse Tracker linear"); break;
			case ModConstants.IT_AMIGA_TABLE: modInfo.append("Impulse Tracker log"); break;
		}
		modInfo.append(" frequency table");
		return modInfo.toString();
	}
	/**
	 * @since 29.03.2010
	 * @return
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder modInfo = new StringBuilder(toShortInfoString());
		modInfo.append("\n\nSong named: ")
				.append(getSongName()).append('\n')
				.append(getSongMessage()).append('\n')
				.append(getInstrumentContainer().toString());
		return modInfo.toString();
	}
}
