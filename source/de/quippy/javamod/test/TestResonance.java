/*
 * @(#) TestResonance.java
 *
 * Created on 02.07.2020 by Daniel Becker
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
package de.quippy.javamod.test;

import java.io.File;
import java.net.URL;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

import de.quippy.javamod.io.FileOrPackedInputStream;
import de.quippy.javamod.main.JavaModMainBase;
import de.quippy.javamod.multimedia.mod.ModConstants;
import de.quippy.javamod.multimedia.mod.loader.instrument.Sample;
import de.quippy.javamod.system.Log;

/**
 * We want to test Resonance filter on a simple
 * WAV file.
 * @author Daniel Becker
 * @since 02.07.2020
 */
public class TestResonance extends JavaModMainBase
{
	private int bufferSize;
	private byte [] output;

	private int sampleSizeInBits;
	private int sampleSizeInBytes;
	private int channels;
	private int sampleRate;
	private AudioFormat audioFormat;
	
	private URL waveFileUrl;
	private AudioInputStream audioInputStream;
	
	private long currentSamplesWritten;
	
	private static class ChannelMemory
	{
		public Sample sample;
		public boolean keyOff;
		public boolean isForwardDirection;
		public int loopCounter;
		public int interpolationMagic;
		public int currentTuning;
		public int currentTuningPos;
		public int currentSamplePos;
		public boolean instrumentFinished;
		
		public int resonance, cutOff;
		
		private int filterMode, songFlags;
		private long filter_A0, filter_B0, filter_B1, filter_HP;
		private long filter_Y1, filter_Y2, filter_Y3, filter_Y4;
	}
	private ChannelMemory aktMemo;

	/**
	 * Constructor for TestResonance
	 */
	public TestResonance()
	{
		super(true);
	}
	private void initialize()
	{
		try
		{
			aktMemo = new ChannelMemory();
			//waveFileUrl = new File("m:\Multimedia\Files MOD\__testmods__\wavs\\\GEWITTER.WAV").toURI().toURL();
			waveFileUrl = new File("m:\\Multimedia\\Files MOD\\__testmods__\\wavs\\CheckResonance - 2 - Pan Flute.wav").toURI().toURL();
			
			audioInputStream = AudioSystem.getAudioInputStream(new FileOrPackedInputStream(waveFileUrl));
			audioFormat = audioInputStream.getFormat();
			
			// Check, if conversion is necessary and possible:
			DataLine.Info sourceLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
			if (!AudioSystem.isLineSupported(sourceLineInfo))
			{
				AudioFormat[] possibleFormats = AudioSystem.getTargetFormats(AudioFormat.Encoding.PCM_SIGNED, audioFormat);
				if (possibleFormats!=null && possibleFormats.length!=0)
				{
					audioInputStream = AudioSystem.getAudioInputStream(possibleFormats[0], audioInputStream);
					audioFormat = audioInputStream.getFormat();
					Log.info("Converting input data to " + audioFormat.toString());
				}
			}

			this.channels = audioFormat.getChannels();
			this.sampleSizeInBits = audioFormat.getSampleSizeInBits();
			this.sampleSizeInBytes = this.sampleSizeInBits>>3;
			this.sampleRate = 48000;
			aktMemo.currentTuning = (int)((((long)audioFormat.getSampleRate())<<ModConstants.SHIFT) / sampleRate);
			audioFormat = new AudioFormat(this.sampleRate, 16, 2, true, false);
			
			Sample sample = aktMemo.sample = new Sample();
			sample.setStereo(channels>1);
			sample.setLength((int)audioInputStream.getFrameLength());
			sample.setLoopType(0);
			sample.setLoopStart(0);
			sample.setLoopStop(0);
			sample.setSustainLoopStart(0);
			sample.setSustainLoopStop(0);
			sample.allocSampleData();

			this.bufferSize = 250 * channels * sampleRate / 1000; // 250ms buffer
			// Now for the bits (linebuffer):
			bufferSize *= sampleSizeInBytes;
			output = new byte[bufferSize];
			
			int	nBytesRead = 0;
			int idx = 0;
			while (nBytesRead != -1)
			{
				nBytesRead = audioInputStream.read(output, 0, output.length);
				if (nBytesRead >= 0)
				{
					int i=0;
					while (i<nBytesRead)
					{
						byte b1 = output[i++];
						byte b2 = output[i++];

						short s = (short)(((b1&0xFF) | ((b2&0xFF)<<8))&0xFFFF);
						sample.sampleL[idx] = ModConstants.promoteSigned16BitToSigned32Bit((long)s);
						if (sample.sampleR!=null)
						{
							byte b3 = output[i++];
							byte b4 = output[i++];
							sample.sampleR[idx] = ((long)((short)(((b3&0xFF) | ((b4&0xFF)<<8))&0xFFFF)))<<16;
						}
						idx++;
					}
				}
			}
			sample.fixSampleLoops(ModConstants.MODTYPE_IT);
		}
		catch (Throwable ex)
		{
			Log.error("[WavMixer]", ex);
		}
		finally
		{
			try { if (audioInputStream!=null) audioInputStream.close(); } catch (Exception ex) { /*NOOP*/ }
		}
	}
	public void setupChannelFilter(final ChannelMemory aktMemo, boolean reset, int envModifier)
	{
		int cutOff = aktMemo.cutOff & 0x7F;
		cutOff = (cutOff * (envModifier + 256)) >> 8;
		if (cutOff<0) cutOff=0; else if (cutOff>0xFF) cutOff=0xFF;
		int resonance = aktMemo.resonance & 0x7F;
		if (resonance<0) resonance=0; else if (resonance>0xFF) resonance=0xFF;

		final double fac = (((aktMemo.songFlags&ModConstants.SONG_EXFILTERRANGE)!=0)? (128.0d / (20.0d * 256.0d)) : (128.0d / (24.0d * 256.0d)));
		double frequency = 110.0d * Math.pow(2.0d, (double)cutOff * fac + 0.25d);
		if (frequency < 120d) frequency = 120d;
		if (frequency > 20000d) frequency = 20000d;
		if (frequency > sampleRate>>1) frequency = sampleRate>>1;
		frequency *= 2.0d * Math.PI;
		
		final double dmpFac = ModConstants.ResonanceTable[resonance];
		double e, d;
		if ((aktMemo.songFlags&ModConstants.SONG_EXFILTERRANGE)==0) // mod.getSongFlags()
		{
			final double r = ((double)sampleRate) / frequency;
			d = dmpFac * r + dmpFac - 1.0d;
			e = r * r;
		}
		else
		{
	        final double d_dmpFac = 2.0d * dmpFac;
			final double r = frequency / ((double)(sampleRate));
	        d = (1.0d - d_dmpFac) * r;
	        if (d > 2.0d) d = 2.0d;
	        d = (d_dmpFac - d) / r;
	        e = 1.0d / (r * r);
		}

		final double fg = 1.0d / (1.0d + d + e);
		final double fb0 = (d + e + e) / (1.0d + d + e);
		final double fb1 = -e / (1.0d + d + e);

		switch(aktMemo.filterMode)
		{
			case ModConstants.FLTMODE_HIGHPASS:
				aktMemo.filter_A0 = (long)((1.0d - fg) * ModConstants.FILTER_PRECISION);
				aktMemo.filter_B0 = (long)(fb0 * ModConstants.FILTER_PRECISION);
				aktMemo.filter_B1 = (long)(fb1 * ModConstants.FILTER_PRECISION);
				aktMemo.filter_HP = -1;
				break;
			case ModConstants.FLTMODE_BANDPASS:
			case ModConstants.FLTMODE_LOWPASS:
			default:
				aktMemo.filter_A0 = (long)(fg * ModConstants.FILTER_PRECISION);
				aktMemo.filter_B0 = (long)(fb0 * ModConstants.FILTER_PRECISION);
				aktMemo.filter_B1 = (long)(fb1 * ModConstants.FILTER_PRECISION);
				aktMemo.filter_HP = 0;
				if (aktMemo.filter_A0 == 0) aktMemo.filter_A0 = 1;
				break;
		}

		if (reset) aktMemo.filter_Y1 = aktMemo.filter_Y2 = aktMemo.filter_Y3 = aktMemo.filter_Y4 = 0;
	}
	private void doResonance(final ChannelMemory aktMemo, final long buffer[])
	{
		long sampleAmp = buffer[0]<<ModConstants.FILTER_PREAMP_BITS; // with preAmp
		long fy = ((sampleAmp * aktMemo.filter_A0) + (aktMemo.filter_Y1 * aktMemo.filter_B0) + (aktMemo.filter_Y2 * aktMemo.filter_B1) + ModConstants.HALF_FILTER_PRECISION) >> ModConstants.FILTER_SHIFT_BITS;
		aktMemo.filter_Y2 = aktMemo.filter_Y1;
		aktMemo.filter_Y1 = fy - (sampleAmp & aktMemo.filter_HP);
		if (aktMemo.filter_Y1 < ModConstants.FILTER_CLIP_MIN) aktMemo.filter_Y1 = ModConstants.FILTER_CLIP_MIN;
		else if (aktMemo.filter_Y1 > ModConstants.FILTER_CLIP_MAX) aktMemo.filter_Y1 = ModConstants.FILTER_CLIP_MAX;
		buffer[0] = (fy + (1<<(ModConstants.FILTER_PREAMP_BITS-1))) >> ModConstants.FILTER_PREAMP_BITS;

		sampleAmp = buffer[1]<<ModConstants.FILTER_PREAMP_BITS; // with preAmp
		fy = ((sampleAmp * aktMemo.filter_A0) + (aktMemo.filter_Y3 * aktMemo.filter_B0) + (aktMemo.filter_Y4 * aktMemo.filter_B1) + ModConstants.HALF_FILTER_PRECISION) >> ModConstants.FILTER_SHIFT_BITS;
		aktMemo.filter_Y4 = aktMemo.filter_Y3;
		aktMemo.filter_Y3 = fy - (sampleAmp & aktMemo.filter_HP);
		if (aktMemo.filter_Y3 < ModConstants.FILTER_CLIP_MIN) aktMemo.filter_Y3 = ModConstants.FILTER_CLIP_MIN;
		else if (aktMemo.filter_Y3 > ModConstants.FILTER_CLIP_MAX) aktMemo.filter_Y3 = ModConstants.FILTER_CLIP_MAX;
		buffer[1] = (fy + (1<<(ModConstants.FILTER_PREAMP_BITS-1))) >> ModConstants.FILTER_PREAMP_BITS;
	}
	protected static void fitIntoLoops(ChannelMemory aktMemo)
	{
		aktMemo.currentTuningPos += aktMemo.currentTuning;
		if (aktMemo.currentTuningPos >= ModConstants.SHIFT_ONE)
		{
			final int addToSamplePos = aktMemo.currentTuningPos >> ModConstants.SHIFT;
			aktMemo.currentTuningPos &= ModConstants.SHIFT_MASK;

			// If Forward direction:
			if (aktMemo.isForwardDirection)
			{
				aktMemo.currentSamplePos += addToSamplePos;
				// Set the end position to check against...
				int loopStart = 0; // if this is not changed, we have no loops
				int loopEnd = aktMemo.sample.length;
				int inLoop = 0;
	
				if ((aktMemo.sample.loopType&ModConstants.LOOP_SUSTAIN_ON)!=0 && !aktMemo.keyOff) // Sustain Loop on?
				{
					loopStart = aktMemo.sample.sustainLoopStart;
					loopEnd = aktMemo.sample.sustainLoopStop;
					inLoop = ModConstants.LOOP_SUSTAIN_ON;
					aktMemo.interpolationMagic = aktMemo.sample.getSustainLoopMagic(aktMemo.currentSamplePos, aktMemo.loopCounter);
				}
				else
				if ((aktMemo.sample.loopType&ModConstants.LOOP_ON)!=0) 
				{
					loopStart = aktMemo.sample.loopStart;
					loopEnd = aktMemo.sample.loopStop;
					inLoop = ModConstants.LOOP_ON;
					aktMemo.interpolationMagic = aktMemo.sample.getLoopMagic(aktMemo.currentSamplePos, aktMemo.loopCounter);
				}
				else
					aktMemo.interpolationMagic = 0;
				
				// do we have an overrun of border?
				if (aktMemo.currentSamplePos >= loopEnd)
				{
					// We need to check against a loop set - maybe a sustain loop is finished
					// but no normal loop is set:
					if (inLoop==0)
					{
						aktMemo.instrumentFinished = true;
					}
					else
					{
						// check if loop, that was enabled, is a ping pong
						if ((inLoop == ModConstants.LOOP_ON && (aktMemo.sample.loopType & ModConstants.LOOP_IS_PINGPONG)!=0) ||
							(inLoop == ModConstants.LOOP_SUSTAIN_ON && (aktMemo.sample.loopType & ModConstants.LOOP_SUSTAIN_IS_PINGPONG)!=0))
						{
							aktMemo.isForwardDirection = false;
							aktMemo.currentSamplePos = loopEnd - 1;
							aktMemo.loopCounter++;
							aktMemo.interpolationMagic = (inLoop == ModConstants.LOOP_ON)?aktMemo.sample.getLoopMagic(aktMemo.currentSamplePos, aktMemo.loopCounter):aktMemo.sample.getSustainLoopMagic(aktMemo.currentSamplePos, aktMemo.loopCounter);
						}
						else
						{
							aktMemo.currentSamplePos = loopStart;
							aktMemo.loopCounter++;
							aktMemo.interpolationMagic = (inLoop == ModConstants.LOOP_ON)?aktMemo.sample.getLoopMagic(aktMemo.currentSamplePos, aktMemo.loopCounter):aktMemo.sample.getSustainLoopMagic(aktMemo.currentSamplePos, aktMemo.loopCounter);
						}
					}
				}
			}
			else // Backwards in Ping Pong
			{
				aktMemo.currentSamplePos -= addToSamplePos;
				
				int loopStart = 0; // support Sound Control "Play Backwards" with no loops set
				int inLoop = 0;
				if ((aktMemo.sample.loopType&ModConstants.LOOP_SUSTAIN_ON)!=0 && !aktMemo.keyOff)
				{
					loopStart = aktMemo.sample.sustainLoopStart;
					inLoop = ModConstants.LOOP_SUSTAIN_ON;
					aktMemo.interpolationMagic = aktMemo.sample.getSustainLoopMagic(aktMemo.currentSamplePos, aktMemo.loopCounter);
				}
				else
				if ((aktMemo.sample.loopType&ModConstants.LOOP_ON)!=0)
				{
					loopStart = aktMemo.sample.loopStart;
					inLoop = ModConstants.LOOP_ON;
					aktMemo.interpolationMagic = aktMemo.sample.getLoopMagic(aktMemo.currentSamplePos, aktMemo.loopCounter);
				}
				else
					aktMemo.interpolationMagic = 0;
	
				if (aktMemo.currentSamplePos < loopStart)
				{
					aktMemo.isForwardDirection = true;
					aktMemo.currentSamplePos = loopStart;
					aktMemo.interpolationMagic = (inLoop == ModConstants.LOOP_ON)?aktMemo.sample.getLoopMagic(aktMemo.currentSamplePos, aktMemo.loopCounter):(inLoop == ModConstants.LOOP_SUSTAIN_ON)?aktMemo.sample.getSustainLoopMagic(aktMemo.currentSamplePos, aktMemo.loopCounter):0;
				}
			}
		}
	}
	private static final int SET_cutOff = 96;
	private static final int SET_resonance = 97;
	private void testResonance()
	{
		currentSamplesWritten = 0;
		final long [] buffer = new long[2];
		
		aktMemo.cutOff = SET_cutOff;
		aktMemo.resonance = SET_resonance;
		aktMemo.songFlags = 0; //ModConstants.SONG_EXFILTERRANGE;
		aktMemo.filterMode = 0; //ModConstants.FLTMODE_HIGHPASS; 
		setupChannelFilter(aktMemo, true, 256); 
		
		try
		{
//			URL modUrl = new File("m:\\Multimedia\\Files MOD\\__testmods__\\panflute.it").toURI().toURL();
//			MultimediaContainer multimediaContainer = MultimediaContainerManager.getMultimediaContainer(modUrl);
//			final ModMixer mixer = (ModMixer)multimediaContainer.createNewMixer();
//			Module mod = mixer.getMod();
//			InstrumentsContainer instruments = mod.getInstrumentContainer();
//			Sample compareMe = instruments.getSample(0);
//			aktMemo.sample = compareMe;
//			
//			for (int i=0; i<compareMe.length; i++)
//			{
//				System.out.println(aktMemo.sample.sampleL[i] + "\t" + compareMe.sampleL[i]);
//				if (aktMemo.sample.sampleL[i] != compareMe.sampleL[i])
//					System.out.println("UPS");
//			}

			SourceDataLine sourceLine = null;
			DataLine.Info sourceLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
			if (AudioSystem.isLineSupported(sourceLineInfo))
			{
				sourceLineInfo.getFormats();
				sourceLine = (SourceDataLine) AudioSystem.getLine(sourceLineInfo);
				sourceLine.open();
				sourceLine.start();
			}
			if (sourceLine==null)
			{
				System.err.println("No SourceDataLine!");
				System.exit(-1);
			}
			else
			{
				while (!aktMemo.instrumentFinished)
				{
					int i=0;
					while (i<output.length)
					{
						aktMemo.sample.getInterpolatedSample(buffer, 3, aktMemo.currentSamplePos, aktMemo.currentTuningPos, !aktMemo.isForwardDirection, aktMemo.interpolationMagic);
						buffer[0]>>=1;
						buffer[1]>>=1;

						doResonance(aktMemo, buffer);
	
						// Now clipping - might not be necessary, but we want correct samples...
						if (buffer[0] < ModConstants.CLIPP32BIT_MIN) buffer[0] = ModConstants.CLIPP32BIT_MIN;
						else if (buffer[0] > ModConstants.CLIPP32BIT_MAX) buffer[0] = ModConstants.CLIPP32BIT_MAX;
						if (buffer[1] < ModConstants.CLIPP32BIT_MIN) buffer[1] = ModConstants.CLIPP32BIT_MIN;
						else if (buffer[1] > ModConstants.CLIPP32BIT_MAX) buffer[1] = ModConstants.CLIPP32BIT_MAX;
	
						// output
						output[i++] = (byte)(buffer[0]>>16);
						output[i++] = (byte)(buffer[0]>>24);
						output[i++] = (byte)(buffer[1]>>16);
						output[i++] = (byte)(buffer[1]>>24);
						
						fitIntoLoops(aktMemo);
						if (aktMemo.instrumentFinished) break;
					}

					currentSamplesWritten += sourceLine.write(output, 0, i);
				}
				sourceLine.drain();
				sourceLine.close();
			}
			System.out.println(currentSamplesWritten);
		}
		catch (Exception ex)
		{
			ex.printStackTrace(System.err);
		}
	}

	/**
	 * @since 02.07.2020
	 * @param args
	 */
	public static void main(String[] args)
	{
		TestResonance me = new TestResonance();
		me.initialize();
		me.testResonance();
	}

}
