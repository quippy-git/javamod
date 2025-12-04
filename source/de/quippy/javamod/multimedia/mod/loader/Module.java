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

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

import de.quippy.javamod.io.ModfileInputStream;
import de.quippy.javamod.multimedia.mod.ModConstants;
import de.quippy.javamod.multimedia.mod.loader.instrument.Instrument;
import de.quippy.javamod.multimedia.mod.loader.instrument.InstrumentsContainer;
import de.quippy.javamod.multimedia.mod.loader.instrument.Sample;
import de.quippy.javamod.multimedia.mod.loader.pattern.PatternContainer;
import de.quippy.javamod.multimedia.mod.midi.MidiMacros;
import de.quippy.javamod.multimedia.mod.mixer.BasicModMixer;

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
	protected int version;

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
	private long [] msTimeIndex;
	private boolean [] arrangementPositionPlayed;
	private int baseVolume; // 0..128
	private int mixingPreAmp; //0..256 (see ModConstants.MAX_MIXING_PREAMP)
	private int synthMixingPreAmp; // 0..256 like mixingPreAmp but for synth Channels

	protected int songFlags;

	// OMPT specific (or S3M and IT), but manipulated in extended song messages
	protected int [] panningValue;
	protected int [] channelVolume;
	protected int tempoMode;
	protected int rowsPerBeat;
	protected int rowsPerMeasure;
	protected int [] tempoSwing;
	protected int createdWithVersion;
	protected int lastSavedWithVersion;
	protected String author;
	protected int resampling;

	protected int needsOPL;
	protected static final int NO_OPL = 0;
	protected static final int OPL2 = 0x01;
	protected static final int OPL3 = 0x02;

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
	 *   tude of the low frequencies (and shift the signal's phase by 90°, but
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
	 *   the signal phase to 180° instead of 90° which can eliminate some sig-
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
		private final ModfileInputStream input;
		// Block of Data
		private byte[] sourceBuffer;
		private int sourceIndex;
		// Destination (24Bit signed mono!)
		private final long[] destBuffer;
		private int destIndex;
		// Samples to fill
		private int anzSamples;
		// Bits remaining
		private int bitsRemain;
		// true, if we have IT Version >2.15 packed Data
		private final boolean isIT215;

		public ITDeCompressor(final long [] buffer, final int length, final boolean isIT215, final ModfileInputStream inputStream)
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
		private int readbits(final int b)
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
			// adopted version from OCP - much faster
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
						final int border = (0xFF >> (9 - width)) - 4; // lower border for width chg

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
					else // illegal width, abort
					{
						return false;
					}

					// now expand value to signed byte
					byte v; // sample value
					if (width < 8)
					{
						final int shift = 8 - width;
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
						final int border = (0xFFFF >> (17 - width)) - 8; // lower border for width chg

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
					else // illegal width, abort
					{
						return false;
					}

					// now expand value to signed word
					short v; // sample value
					if (width < 16)
					{
						final int shift = 16 - width;
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
		tempoMode = ModConstants.TEMPOMODE_CLASSIC;
		rowsPerBeat = 4;
		rowsPerMeasure = 16;
		createdWithVersion = -1;
		lastSavedWithVersion = -1;
		resampling = -1;
		needsOPL = NO_OPL;
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
			if (inputStream!=null) try { inputStream.close(); } catch (final Exception ex) { /* Log.error("IGNORED", ex); */ }
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
		final Module mod = this.getNewInstance(inputStream.getFileName());
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
		final boolean is16Bit = (flags&ModConstants.SM_16BIT)!=0;
		final boolean isBigEndian = (flags&ModConstants.SM_BigEndian)!=0;
		//current.setStereo(isStereo); // just to be sure...

		if (current.length>0)
		{
			current.allocSampleData();
			if ((flags & ModConstants.SM_IT214)!=0 || (flags & ModConstants.SM_IT215)!=0)
			{
				final boolean isIT215 = (flags & ModConstants.SM_IT215)!=0;
				final ITDeCompressor reader = new ITDeCompressor(current.sampleL, current.length, isIT215, inputStream);
				if (is16Bit)
					reader.decompress16();
				else
					reader.decompress8();
				if (isStereo)
				{
					final ITDeCompressor reader2 = new ITDeCompressor(current.sampleR, current.length, isIT215, inputStream);
					if (is16Bit)
						reader2.decompress16();
					else
						reader.decompress8();
				}
			}
			else
			if ((flags&ModConstants.SM_ADPCM)!=0)
			{
				final byte [] deltaLUT = new byte[16];
				inputStream.read(deltaLUT);

				final int length = (current.length + 1)>>1;
				byte currentSample = 0;
				for (int i=0,s=0; i<length; i++)
				{
					final int nibble = inputStream.read();

					currentSample += deltaLUT[nibble & 0x0F];
					current.sampleL[s++] = ModConstants.promoteSigned8BitToSigned32Bit(currentSample);

					currentSample += deltaLUT[nibble >> 4];
					current.sampleL[s++] = ModConstants.promoteSigned8BitToSigned32Bit(currentSample);
				}
			}
			else
			if ((flags&ModConstants.SM_PCMD)!=0 || (flags&ModConstants.SM_PTM8Dto16)!=0)
			{
				if (is16Bit && (flags&ModConstants.SM_PTM8Dto16)==0)
				{
					short delta = 0;
					for (int s=0; s<current.length; s++)
						current.sampleL[s] = ModConstants.promoteSigned16BitToSigned32Bit(delta += (isBigEndian)?inputStream.readMotorolaWord():inputStream.readIntelWord());
					if (isStereo)
					{
						delta = 0;
						for (int s=0; s<current.length; s++)
							current.sampleR[s] = ModConstants.promoteSigned16BitToSigned32Bit(delta += (isBigEndian)?inputStream.readMotorolaWord():inputStream.readIntelWord());
					}
				}
				else
				{
					byte delta = 0;
					for (int s=0; s<current.length; s++)
						current.sampleL[s] = ModConstants.promoteSigned8BitToSigned32Bit(delta += inputStream.readByte());
					if (isStereo)
					{
						delta = 0;
						for (int s=0; s<current.length; s++)
							current.sampleR[s] = ModConstants.promoteSigned8BitToSigned32Bit(delta += inputStream.readByte());
					}
				}
			}
			else
			if ((flags&ModConstants.SM_16BIT)!=0) // 16 Bit PCM Samples
			{
				for (int s=0; s<current.length; s++)
				{
					final short sample = (isBigEndian)?inputStream.readMotorolaWord():inputStream.readIntelWord();
					if (isUnsigned) // unsigned
						current.sampleL[s]=ModConstants.promoteUnsigned16BitToSigned32Bit(sample);
					else
						current.sampleL[s]=ModConstants.promoteSigned16BitToSigned32Bit(sample);
				}
				if (isStereo)
				{
					for (int s=0; s<current.length; s++)
					{
						final short sample = (isBigEndian)?inputStream.readMotorolaWord():inputStream.readIntelWord();
						if (isUnsigned) // unsigned
							current.sampleR[s]=ModConstants.promoteUnsigned16BitToSigned32Bit(sample);
						else
							current.sampleR[s]=ModConstants.promoteSigned16BitToSigned32Bit(sample);
					}
				}
			}
			else // 8 Bit Samples, singed or unsigned
			{
				for (int s=0; s<current.length; s++)
				{
					final byte sample = inputStream.readByte();
					if (isUnsigned) // unsigned
						current.sampleL[s]=ModConstants.promoteUnsigned8BitToSigned32Bit(sample);
					else
						current.sampleL[s]=ModConstants.promoteSigned8BitToSigned32Bit(sample);
				}
				if (isStereo)
				{
					for (int s=0; s<current.length; s++)
					{
						final byte sample = inputStream.readByte();
						if (isUnsigned) // unsigned
							current.sampleR[s]=ModConstants.promoteUnsigned8BitToSigned32Bit(sample);
						else
							current.sampleR[s]=ModConstants.promoteSigned8BitToSigned32Bit(sample);
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
	 * @since 15.12.2023
	 * @return
	 */
	public abstract boolean getFT2Tremolo();
	/**
	 * @since 20.12.2023
	 * @return true for some MODs not supporting BPM
	 */
	public abstract boolean getModSpeedIsTicks();
	/**
	 * @since 25.06.2006
	 * @param length
	 */
	protected void allocArrangement(final int length)
	{
		arrangement = new int[length];
		msTimeIndex = new long[length];
		arrangementPositionPlayed = new boolean[length];
	}
	/**
	 * Automatically cleans up the arrangement data (if illegal pattnums
	 * or marker pattern are in there...)
	 * @since 03.10.2010
	 */
	protected void cleanUpArrangement()
	{
		int realLen = 0;
		for (int i=0; i<songLength; i++)
		{
			if (getArrangement()[i]==255) // end of Song:
				break;
			else
			if (getArrangement()[i]<254 && getArrangement()[i]<getNPattern())
				getArrangement()[realLen++]=getArrangement()[i];
		}
		songLength = realLen;
	}
	/**
	 * @return Returns the arrangement.
	 */
	public int[] getArrangement()
	{
		return arrangement;
	}
	/**
	 * @since 07.03.2024
	 * @return the ms time index of an arrangement position
	 */
	public long[] getMsTimeIndex()
	{
		return msTimeIndex;
	}
	/**
	 * @param arrangement The arrangement to set.
	 */
	public void setArrangement(final int[] arrangement)
	{
		this.arrangement = arrangement;
	}
	public void resetLoopRecognition()
	{
		Arrays.fill(arrangementPositionPlayed, false);
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
	protected void setSongLength(final int newSongLength)
	{
		songLength = newSongLength;
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
	protected void setSongRestart(final int newSongRestart)
	{
		songRestart = newSongRestart;
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
	protected void setSongName(final String newSongName)
	{
		songName = newSongName;
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
	protected void setTempo(final int newTempo)
	{
		tempo = newTempo;
	}
	/**
	 * @return Returns the trackerName.
	 */
	public String getTrackerName()
	{
		return trackerName;
	}
	/**
	 * @return the author
	 */
	public String getAuthor()
	{
		return author;
	}
	public int getResampling()
	{
		return resampling;
	}
	/**
	 * @param trackerName The trackerName to set.
	 */
	protected void setTrackerName(final String newTrackerName)
	{
		trackerName = newTrackerName;
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
	protected void setPatternContainer(final PatternContainer newPatternContainer)
	{
		patternContainer = newPatternContainer;
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
	protected void setModID(final String newModID)
	{
		modID = newModID;
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
	protected void setBaseVolume(final int newBaseVolume)
	{
		baseVolume = newBaseVolume;
	}
	/**
	 * @return the mixingPreAmp (0..256)
	 */
	public int getMixingPreAmp()
	{
		return mixingPreAmp;
	}
	/**
	 * @param mixingPreAmp The mixing Pre-Amp to set
	 */
	protected void setMixingPreAmp(final int newMixingPreAmp)
	{
		mixingPreAmp = newMixingPreAmp;
	}
	/**
	 * @return the synthMixingPreAmp (0..256)
	 */
	public int getSynthMixingPreAmp()
	{
		return synthMixingPreAmp;
	}
	/**
	 * @param synthMixingPreAmp The synth mixing Pre-Amp to set
	 */
	protected void setSynthMixingPreAmp(final int newSynthMixingPreAmp)
	{
		synthMixingPreAmp = newSynthMixingPreAmp;
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
	protected void setSongFlags(final int newSongFlags)
	{
		songFlags = newSongFlags;
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
	protected void setModType(final int newModType)
	{
		modType = newModType;
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
	public void setVersion(final int newVersion)
	{
		version = newVersion;
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
	public void setLengthInMilliseconds(final long newLengthInMilliseconds)
	{
		lengthInMilliseconds = newLengthInMilliseconds;
	}
	public int getTempoMode()
	{
		return tempoMode;
	}
	/**
	 * @return the rowsPerBeat
	 */
	public int getRowsPerBeat()
	{
		return rowsPerBeat;
	}
	/**
	 * @return the rowsPerMeasure
	 */
	public int getRowsPerMeasure()
	{
		return rowsPerMeasure;
	}
	public int [] getTempoSwing()
	{
		return tempoSwing;
	}
	/**
	 * @since 18.12.2023
	 * @return
	 */
	public String getFrequencyTableString()
	{
		switch (getFrequencyTable())
		{
			case ModConstants.STM_S3M_TABLE: return "Scream Tracker";
			case ModConstants.IT_AMIGA_TABLE: return "Impulse Tracker log";
			case ModConstants.IT_LINEAR_TABLE: return "Impulse Tracker linear";
			case ModConstants.AMIGA_TABLE: return "Protracker log";
			case ModConstants.XM_AMIGA_TABLE: return "Fast Tracker log";
			case ModConstants.XM_LINEAR_TABLE: return "Fast Tracker linear";
		}
		return "Unknown";
	}
	public boolean isStereo()
	{
		return ((songFlags&ModConstants.SONG_ISSTEREO)!=0);
	}
	/**
	 * @since 29.03.2010
	 * @return
	 */
	public String toShortInfoString()
	{
		final StringBuilder modInfo = new StringBuilder(getTrackerName());
		modInfo.append(isStereo()?" stereo":" mono").append(" mod with ");
		if (instrumentContainer!=null && instrumentContainer.hasInstruments()) modInfo.append(getNInstruments()).append(" instruments mapping ");
		modInfo.append(getNSamples()).append(" samples and ").append(getNChannels()).append(" channels using ")
		.append(getFrequencyTableString()).append(" frequency table");
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
		final StringBuilder modInfo = new StringBuilder(toShortInfoString());
		modInfo.append("\n\nSong named: ")
				.append(getSongName()).append('\n')
				.append(getSongMessage()).append('\n')
				.append(getInstrumentContainer().toString());
		return modInfo.toString();
	}

	// Flags for readExtendedFlags
	private static final int dFdd_VOLUME 		= 0x0001;
	private static final int dFdd_VOLSUSTAIN 	= 0x0002;
	private static final int dFdd_VOLLOOP 		= 0x0004;
	private static final int dFdd_PANNING 		= 0x0008;
	private static final int dFdd_PANSUSTAIN 	= 0x0010;
	private static final int dFdd_PANLOOP 		= 0x0020;
	private static final int dFdd_PITCH 		= 0x0040;
	private static final int dFdd_PITCHSUSTAIN 	= 0x0080;
	private static final int dFdd_PITCHLOOP 	= 0x0100;
	private static final int dFdd_SETPANNING 	= 0x0200;
	private static final int dFdd_FILTER 		= 0x0400;
	private static final int dFdd_VOLCARRY 		= 0x0800;
	private static final int dFdd_PANCARRY 		= 0x1000;
	private static final int dFdd_PITCHCARRY 	= 0x2000;
	private static final int dFdd_MUTE 			= 0x4000;
	/**
	 * These flags are not written anymore - and I guess that OMPT reads them
	 * wrongly now (reads only 8 bits instead of 16 bits).
	 * We support this for backwards compatibility
	 * @since 13.02.2024
	 * @param inputStream
	 * @param ins
	 * @param size
	 * @throws IOException
	 */
	protected void readExtendedFlags(final ModfileInputStream inputStream, final Instrument ins, final int size) throws IOException
	{
		final int flag = (int)inputStream.readIntelBytes(size); // OMPT reads only 8 bits, but flags indicate 16 bit! We rely on "size"
		if ((flag & dFdd_VOLUME)		!=0) ins.volumeEnvelope.on = true;
		if ((flag & dFdd_VOLSUSTAIN)	!=0) ins.volumeEnvelope.sustain = true;
		if ((flag & dFdd_VOLLOOP)		!=0) ins.volumeEnvelope.loop = true;
		if ((flag & dFdd_VOLCARRY)		!=0) ins.volumeEnvelope.carry = true;

		if ((flag & dFdd_PANNING)		!=0) ins.panningEnvelope.on = true;
		if ((flag & dFdd_PANSUSTAIN)	!=0) ins.panningEnvelope.sustain = true;
		if ((flag & dFdd_PANLOOP)		!=0) ins.panningEnvelope.loop = true;
		if ((flag & dFdd_PANCARRY)		!=0) ins.panningEnvelope.carry = true;

		if ((flag & dFdd_PITCH)			!=0) ins.pitchEnvelope.on = true;
		if ((flag & dFdd_PITCHSUSTAIN)	!=0) ins.pitchEnvelope.sustain = true;
		if ((flag & dFdd_PITCHLOOP)		!=0) ins.pitchEnvelope.loop = true;
		if ((flag & dFdd_PITCHCARRY)	!=0) ins.pitchEnvelope.carry = true;
		if ((flag & dFdd_FILTER)		!=0) ins.pitchEnvelope.filter = true;

		if ((flag & dFdd_SETPANNING)	!=0) ins.setPanning = true;
		if ((flag & dFdd_MUTE)			!=0) ins.mute = true;
	}
	/**
	 * @since 03.02.2024
	 * @param inputStream
	 * @param ins
	 * @param code
	 * @param size
	 * @throws IOException
	 */
	protected void readInstrumentExtensionField(final ModfileInputStream inputStream, final Instrument ins, final int code, final int size) throws IOException
	{
		if (size > inputStream.length() || ins==null) return;
		switch (code)
		{
			case 0x56522E2E: //"VR.." VOLRampUp
				ins.volRampUp = (int)inputStream.readIntelBytes(size);
				break;
			case 0x464F2E2E: //"FO.." FadeOut
				ins.volumeFadeOut = (int)inputStream.readIntelBytes(size);
				break;
			case 0x64462E2E: //"dF.." extended instrument flags
				readExtendedFlags(inputStream, ins, size);
				break;
			case 0x47562E2E: //"GV.." Global Volume
				ins.globalVolume = (int)inputStream.readIntelBytes(size);
				break;
			case 0x502E2E2E: //"P..." Pan
				ins.defaultPanning = (int)inputStream.readIntelBytes(size);
				break;
			case 0x564C532E: //"VLS." VolEnv LoopStart
				ins.volumeEnvelope.loopStartPoint = (int)inputStream.readIntelBytes(size);
				break;
			case 0x564C452E: //"VLE." VolEnv LoopEnd
				ins.volumeEnvelope.loopEndPoint = (int)inputStream.readIntelBytes(size);
				break;
			case 0x5653422E: //"VSB." VolEnv SustainLoopStart
				ins.volumeEnvelope.sustainStartPoint = (int)inputStream.readIntelBytes(size);
				break;
			case 0x5653452E: //"VSE." VolEnv SustainLoopEnd
				ins.volumeEnvelope.sustainEndPoint = (int)inputStream.readIntelBytes(size);
				break;
			case 0x504C532E: //"PLS." PanEnv LoopStart
				ins.panningEnvelope.loopStartPoint = (int)inputStream.readIntelBytes(size);
				break;
			case 0x504C452E: //"PLE." PanEnv LoopEnd
				ins.panningEnvelope.loopEndPoint = (int)inputStream.readIntelBytes(size);
				break;
			case 0x5053422E: //"PSB." PanEnv SustainLoopStart
				ins.panningEnvelope.sustainStartPoint = (int)inputStream.readIntelBytes(size);
				break;
			case 0x5053452E: //"PSE." PanEnv SustainLoopEnd
				ins.panningEnvelope.sustainEndPoint = (int)inputStream.readIntelBytes(size);
				break;
			case 0x50694C53: //"PiLS" PitchEnvelope LoopStart
				ins.pitchEnvelope.loopStartPoint = (int)inputStream.readIntelBytes(size);
				break;
			case 0x50694C45: //"PiLE" PitchEnvelope LoopEnd
				ins.pitchEnvelope.loopEndPoint = (int)inputStream.readIntelBytes(size);
				break;
			case 0x50695342: //"PiSB" PitchEnvelope SustainLoopStart
				ins.pitchEnvelope.sustainStartPoint = (int)inputStream.readIntelBytes(size);
				break;
			case 0x50695345: //"PiSE" PitchEnvelope SustainLoopEnd
				ins.pitchEnvelope.sustainEndPoint = (int)inputStream.readIntelBytes(size);
				break;
			case 0x4E4E412E: //"NNA." NewNoteAction
				ins.NNA = (int)inputStream.readIntelBytes(size);
				break;
			case 0x4443542E: //"DCT." DuplicateNoteCheck
				ins.dublicateNoteCheck = (int)inputStream.readIntelBytes(size);
				break;
			case 0x444E412E: //"DNA." DuplicateNoteAction
				ins.dublicateNoteAction = (int)inputStream.readIntelBytes(size);
				break;
			case 0x50532E2E: //"PS..." PanSwing
				ins.randomPanningVariation = (int)inputStream.readIntelBytes(size);
				break;
			case 0x56532E2E: //"VS..." VolSwing
				ins.randomVolumeVariation = (int)inputStream.readIntelBytes(size);
				break;
			case 0x4D69502E: //"MiP." MixPlugIn
				ins.plugin = (int)inputStream.readIntelBytes(size);
				break;
			case 0x50564548: //"PVEH" PluginVelocityHandling
			case 0x50564F48: //"PVOH" PluginVolumeHandling
				inputStream.skip(size);
				break;
			case 0x4D422E2E: //"MB.." MidiBank
				ins.midiBank = (int)inputStream.readIntelBytes(size);
				break;
			case 0x4D502E2E: //"MP.." MidiProgramm
				ins.midiProgram = (int)inputStream.readIntelBytes(size);
				break;
			case 0x4D432E2E: //"MC.." MidiChannel
				ins.midiChannel = (int)inputStream.readIntelBytes(size);
				break;
			case 0x4D505744: //"MPWD" MidiPWD
				ins.pitchWheelDepth = (int)inputStream.readIntelBytes(size);
				break;
			case 0x522E2E2E: //"R..." resampling
				ins.resampling = (int)inputStream.readIntelBytes(size);
				ins.resampling--; // our "default" is -1
				if (ins.resampling>3) ins.resampling = 3; // our max value;
				break;
			case 0x43532E2E: //"CS.." CutSwing
				ins.randomCutOffVariation = (int)inputStream.readIntelBytes(size);
				break;
			case 0x52532E2E: //"RS.." ResSwing
				ins.randomResonanceVariation = (int)inputStream.readIntelBytes(size);
				break;
			case 0x464D2E2E: //"FM.." filterMode
				ins.filterMode = (int)inputStream.readIntelBytes(size);
				break;
			case 0x5045524E: //"PERN" PitchEnv.nReleaseNode
			case 0x4145524E: //"AERN" PanEnv.nReleaseNode
			case 0x5645524E: //"VERN" VolEnv.nReleaseNode
			case 0x5054544C: //"PTTL" pitchToTempoLock
			case 0x46545450: //"FTTP" pitchToTempoLock FracPart
			default:
				inputStream.skip(size);
		}
	}
	/**
	 * @since 19.01.2024
	 * @param inputStream
	 * @return
	 * @throws IOException
	 */
	protected boolean loadExtendedInstrumentProperties(final ModfileInputStream inputStream) throws IOException
	{
		final int marker = inputStream.readIntelDWord();
		if (marker != 0x4D505458) // MPTX - ModPlugExtraInstrumentInfo
		{
			inputStream.skipBack(4);
			return false;
		}
//		System.out.println("\nExtendedInstrumentProperties");
		while (inputStream.length()>=6)
		{
			final int code = inputStream.readIntelDWord();
			if (code==0x4D505453 || // Start of MPTM extensions, non-ASCII ID or truncated field
					(code&0x80808080)!=0 || (code&0x60606060)==0)
			{
				inputStream.skipBack(4);
				break;
			}
//			System.out.println("case 0x"+ModConstants.getAsHex(code, 8) + ": //\"" + Helpers.retrieveAsString(new byte[] {(byte)((code>>24)&0xFF), (byte)((code>>16)&0xFF), (byte)((code>>8)&0xFF), (byte)(code&0xFF)}, 0, 4)+"\"");
			// size of this property for ONE instrument
			final int size = inputStream.readIntelWord();
			for (int i=0; i<getNInstruments(); i++)
			{
				readInstrumentExtensionField(inputStream, getInstrumentContainer().getInstrument(i), code, size);
			}
		}
		return true;
	}
	/**
	 * @since 03.02.2024
	 * @param inputStream
	 * @param ignoreChannelCount
	 * @return
	 * @throws IOException
	 */
	protected boolean loadExtendedSongProperties(final ModfileInputStream inputStream, final boolean ignoreChannelCount) throws IOException
	{
		final int marker = inputStream.readIntelDWord();
		if (marker != 0x4D505453) // MPTS - ModPlugExtraSongInfo
		{
			inputStream.skipBack(4);
			return false;
		}
//		System.out.println("\nExtendedSongProperties");
		while (inputStream.length()>=6)
		{
			final int code = inputStream.readIntelDWord();
//			System.out.println("case 0x"+ModConstants.getAsHex(code, 8) + ": //\"" + Helpers.retrieveAsString(new byte[] {(byte)((code>>24)&0xFF), (byte)((code>>16)&0xFF), (byte)((code>>8)&0xFF), (byte)(code&0xFF)}, 0, 4)+"\"");
			final int size = inputStream.readIntelWord();

			if (code==0x04383232) // Start of MPTM extensions, non-ASCII ID or truncated field
			{
				inputStream.skipBack(6);
				break;
			}
			else
			if ((code&0x80808080)!=0 || (code&0x60606060)==0 || inputStream.length()<size)
			{
				break;
			}

			switch (code)
			{
				case 0x44542E2E: //"DT.." - default BPM
					final int bpm = (int)inputStream.readIntelBytes(size);
					setBPMSpeed(bpm);
					break;
				case 0x52464544: //"DTFR" - default BPM - fraction - is written as MagicLE
					/*final int bpmFrac = (int)*/inputStream.readIntelBytes(size);
					break;
				case 0x5250422E: //"RPB." - RowsPerBeat
					rowsPerBeat = (int)inputStream.readIntelBytes(size);
					break;
				case 0x52504D2E: //"RPM." - RowsPerMeasure
					rowsPerMeasure = (int)inputStream.readIntelBytes(size);
					break;
				case 0x432E2E2E: //"C..." - # channels
					final int channels = (int)inputStream.readIntelBytes(size);
					if (!ignoreChannelCount) setNChannels(channels);
					break;
				case 0x544D2E2E: //"TM.." - Tempo mode
					tempoMode = (int)inputStream.readIntelBytes(size);
					if (tempoMode<0 || tempoMode>ModConstants.TEMPOMODE_MODERN) tempoMode = ModConstants.TEMPOMODE_CLASSIC;
					break;
				case 0x4357562E: //"CWV." - created with version
					createdWithVersion = (int)inputStream.readIntelBytes(size);
					break;
				case 0x4C535756: //"LSWV" - last saved with version
					lastSavedWithVersion = (int)inputStream.readIntelBytes(size);
					break;
				case 0x5350412E: //"SPA." - SamplePreAmp
					mixingPreAmp = (int)inputStream.readIntelBytes(size);
					break;
				case 0x56535456: //"VSTV" - VSTiVolume
					synthMixingPreAmp = (int)inputStream.readIntelBytes(size);
					break;
				case 0x4447562E: //"DGV." - defaultGlobalVolume
					baseVolume = (int)inputStream.readIntelBytes(size);
					break;
				case 0x52502E2E: //"RP.." - Song Restart
					final int restartPosition = (int)inputStream.readIntelBytes(size);
					if ((getModType()&ModConstants.MODTYPE_XM)==0) setSongRestart(restartPosition); // Skip for XMs!
					break;
				case 0x504D5352: //"RSMP" - Resampling Method - written as MagicLE
					resampling = (int)inputStream.readIntelBytes(size);
					resampling--; // our "default" is -1
					if (resampling>ModConstants.INTERPOLATION_WINDOWSFIR) resampling = ModConstants.INTERPOLATION_WINDOWSFIR; // our max value;
					break;
				case 0x4C4F4343: //"CCOL" - Channel Colors - written as MagicLE
					if ((size % 4)==0)
					{
						final int numChannels = size>>2;
						final Color [] chnColors = new Color[numChannels];

						final byte [] rgb = new byte[4];
						for (int c=0; c<numChannels; c++)
						{
							inputStream.read(rgb, 0, 4);
							if (rgb[3]!=0)
								chnColors[c] = null;
							else
								chnColors[c] = new Color(rgb[0]&0xFF | ((rgb[1]&0xFF) << 8) | ((rgb[2]&0xFF) << 16));
						}
					}
					else
						inputStream.skip(size);
					break;
				case 0x48545541: //"AUTH" - Author - written as MagicLE
					author = inputStream.readString(size);
					break;
				case 0x43686E53: //"ChnS" - channel settings for channels 65-127
					if ((getModType()&ModConstants.MODTYPE_XM)==0 && size<=64*2 && size%2==0) // Skip for XMs!
					{
						final int loopLimit = 64 + size>>1;
						final int [] newPanningValues = new int[loopLimit];
						final int [] newChannelVolume = new int[loopLimit];
						System.arraycopy(panningValue, 0, newPanningValues, 0, 64);
						System.arraycopy(channelVolume, 0, newChannelVolume, 0, 64);

						for (int c=64; c<loopLimit; c++)
						{
							int pan = inputStream.read();
							final int vol = inputStream.read();
							if (pan!=0xFF)
							{
								newChannelVolume[c] = vol;
								if (pan==100 || (pan & 0x80)!=0)
								{
									// we simply store those, as mixer responds to these in "initializeMixer()"
									newPanningValues[c] = pan<<2;
								}
								else
								{
									pan = (pan&0x7F)<<2;
									if (pan>256) pan = 256;
									newPanningValues[c]=pan;
								}
							}
						}

						panningValue = newPanningValues;
						channelVolume = newChannelVolume;
					}
					else
						inputStream.skip(size);
					break;
				case 0x53455543: //"CUES" - Sample Cues - written as MagicLE
					if (size>2)
					{
						final int cues = (size - 2)>>2; // should be MAX_CUES (or less)
						final int sampleIndex = inputStream.readIntelWord();
						if (sampleIndex>0 && sampleIndex<=getNSamples())
						{
							final Sample sample = getInstrumentContainer().getSample(sampleIndex);
							// future versions of OMPT might have more than 9 cues...
							final int [] theCues = new int[cues<Sample.MAX_CUES?Sample.MAX_CUES:cues];
							int cue = 0;
							for (; cue<cues; cue++) theCues[cue] = inputStream.readIntelDWord();
							// if we had less than max_cues, fill up with default
							for (; cue<Sample.MAX_CUES; cue++) theCues[cue] = sample.length;
							sample.setCues(theCues);
						}
						else
							inputStream.skip(cues<<2);
					}
					else
						inputStream.skip(size);
					break;
				case 0x474E5753: //"SWNG" - Tempo Swing factors - written as MagicLE
					if (size>2)
					{
						final int anzNums = inputStream.readIntelWord();
						tempoSwing = new int[anzNums];
						for (int i=0; i<anzNums; i++) tempoSwing[i] = inputStream.readIntelDWord();
					}
					else
						inputStream.skip(size);
					break;
				case 0x504D4D2E: //"PMM." - MixLevels - this is OMPT specific to let old MPTs sound equally - we ignore that for now
				case 0x4D53462E: //"MSF." - Playback Compatibility Flags - OMPT specific - we ignore that
				case 0x4D494D41: //"MIMA" - MidiMapper - guess we cannot use this - especially when running on Linux
				default: // if it is not implemented, skip it!
					inputStream.skip(size);
					break;
			}
		}
		return true;
	}
}
