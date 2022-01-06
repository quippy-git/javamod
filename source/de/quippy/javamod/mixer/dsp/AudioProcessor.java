/*
 * @(#) AudioProcessor.java
 *
 * This class will first of all convert the signal given via writeSampleData
 * into a float array containing values from 1.0<=x<=-1.0 (L/R or mono).
 * 
 * Next the writeSampleData is capable of directing all sample data to
 * signal processing call backs doing their things and will return a 
 * byte array of sampledata coresponding to the audio format provided 
 *
 * Depending on the frames per second this data is than send split into
 * left and right channel to all callbacks who whant to be informed.
 * 
 * Created on 29.09.2007 by Daniel Becker
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
package de.quippy.javamod.mixer.dsp;

import java.util.ArrayList;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.SourceDataLine;

/**
 * @author Daniel Becker
 * @since 29.09.2007
 */
public class AudioProcessor
{
	private final Object lock = new Object();

	private final int desiredBufferSize;
	private final long waitForNanos;
	private final ArrayList<DspProcessorCallBack> callBacks;
	private final ArrayList<DSPEffekt> effectCallBacks;

	private static final int SAMPLEBUFFERSIZE = 96000;
	private SourceDataLine sourceDataLine;
	private volatile long internalFramePosition;
	private volatile boolean useInternalCounter;
	private int sampleBufferSize;
	private float [] sampleBuffer;
	private byte [] resultSampleBuffer;
	private int currentWritePosition;
	private ProcessorTask processorThread;
	
	private AudioFormat audioFormat;
	private boolean isBigEndian;
	private boolean isSigned;
	private int sampleSizeInBits;
	private int bytesPerChannel;
	private long mask;
	private long neg_Bit;
	private long neg_mask;
	private long minSample;
	private long maxSample;
	
	private boolean dspEnabled;
	
	private final class ProcessorTask extends Thread
	{
		private final AudioProcessor me;
		private final float [] leftBuffer;
		private final float [] rightBuffer;
		private final long nanoWait;
		private volatile boolean process;
		private volatile boolean process_alive;
		
		public ProcessorTask(AudioProcessor parent)
		{
			this.me = parent;
			this.leftBuffer = new float [me.desiredBufferSize];
			this.rightBuffer = new float [me.desiredBufferSize];
			this.process = true;
			this.nanoWait = parent.waitForNanos;
			this.setDaemon(true);
			this.setName("AudioProcessor");
			this.setPriority(Thread.MAX_PRIORITY);
		}
		/**
		 * @since 29.09.2007
		 */
		public void stopProcessorTask()
		{
			process = false; // Signal endless loop to stop
			while (process_alive) // wait till that happened
			{
				try { Thread.sleep(10L); } catch (InterruptedException ex) { /*NOOP*/ }
			}
			// Inform listeners that we stopped
			me.fireCurrentSampleChanged(null, null);
		}
		/**
		 * 
		 * @see java.lang.Runnable#run()
		 */
		public void run()
		{
			process_alive = true;
			while (process)
			{
				final long now = System.nanoTime();
				
				synchronized (lock)
				{
					int channels = me.audioFormat.getChannels();
					int currentReadPosition = (int)((me.getFramePosition() * channels) % me.sampleBufferSize);
					for (int i=0; i<me.desiredBufferSize; i++)
					{
						if (currentReadPosition>=me.sampleBufferSize) currentReadPosition = 0;
						if (channels==2)
						{
							leftBuffer[i] = me.sampleBuffer[currentReadPosition++];
							rightBuffer[i] = me.sampleBuffer[currentReadPosition++];
						}
						else
						{
							leftBuffer[i] = rightBuffer[i] = me.sampleBuffer[currentReadPosition++];
						}
					}
				}
				
				me.fireCurrentSampleChanged(leftBuffer, rightBuffer);
				
				final long stillToWait = nanoWait - (System.nanoTime() - now);
				if (stillToWait>0)
				{
					try { Thread.sleep(stillToWait/1000000L); } catch (InterruptedException ex) { /*noop*/ }
				}
				else
				{
					try { Thread.sleep(1L); } catch (InterruptedException ex) { /*noop*/ }
				}
			}
			process_alive = false;
		}
	}
	/**
	 * Constructor for AudioProcessor
	 * @param desiredBufferSize
	 * @param desiredFPS
	 */
	public AudioProcessor(int desiredBufferSize, int desiredFPS)
	{
		super();
		this.desiredBufferSize = desiredBufferSize;
		this.waitForNanos = 1000000000L / (long)desiredFPS;
		this.callBacks = new ArrayList<DspProcessorCallBack>();
		this.effectCallBacks = new ArrayList<DSPEffekt>();
		dspEnabled = true;
	}
	/**
	 * Constructor for AudioProcessor
	 */
	public AudioProcessor()
	{
		this(1024, 70);
	}
	/**
	 * @since 29.09.2007
	 * @param callBack
	 */
	public synchronized void addListener(DspProcessorCallBack callBack)
	{
		if (!callBacks.contains(callBack)) callBacks.add(callBack);
	}
	/**
	 * @since 29.09.2007
	 * @param callBack
	 */
	public synchronized void removeListener(DspProcessorCallBack callBack)
	{
		callBacks.remove(callBack);
	}
	/**
	 * @param leftBuffer
	 * @param rightBuffer
	 * @since 06.01.2012
	 */
	private synchronized void fireCurrentSampleChanged(float[] leftBuffer, float[] rightBuffer)
	{
		final int size = callBacks.size();
		for (int i=0; i<size; i++)
		{
			callBacks.get(i).currentSampleChanged(leftBuffer, rightBuffer);
		}
	}
	/**
	 * @since 15.01.2012
	 * @param effectCallBack
	 */
	public synchronized void addEffectListener(DSPEffekt effectCallBack)
	{
		if (!effectCallBacks.contains(effectCallBack)) effectCallBacks.add(effectCallBack);
	}
	/**
	 * @since 15.01.2012
	 * @param effectCallBack
	 */
	public synchronized void removeEffectListener(DSPEffekt effectCallBack)
	{
		effectCallBacks.remove(effectCallBack);
	}
	private synchronized void initializeEffects(final AudioFormat audioFormat, final int sampleBufferLength)
	{
		final int size = effectCallBacks.size();
		for (int i=0; i<size; i++)
		{
			effectCallBacks.get(i).initialize(audioFormat, sampleBufferLength);
		}
	}
	private synchronized int callEffects(final float[] buffer, final int start, final int length)
	{
		final int size = effectCallBacks.size();
		int anzSamples = length;
		for (int i=0; i<size; i++)
		{
			anzSamples = effectCallBacks.get(i).doEffekt(buffer, start, anzSamples);
		}
		return anzSamples;
	}
	/**
	 * @param useInternalCounter the useInternalCounter to set
	 */
	public void setUseInternalCounter(boolean useInternalCounter)
	{
		this.useInternalCounter = useInternalCounter;
	}
	/**
	 * @param internalFramePosition the internalFramePosition to set
	 * This is the amount of samples written
	 */
	public void setInternalFramePosition(long internalFramePosition)
	{
		this.internalFramePosition = internalFramePosition;
	}
	/**
	 * returns the current FramePosition
	 * @since 27.12.2012
	 * @return
	 */
	public long getFramePosition()
	{
		return (useInternalCounter || sourceDataLine==null)?internalFramePosition:sourceDataLine.getLongFramePosition();
	}
	/**
	 * @since 29.09.2007
	 * @param sourceDataLine
	 */
	public void initializeProcessor(SourceDataLine sourceDataLine)
	{
		this.sourceDataLine = sourceDataLine;
		initializeProcessor(sourceDataLine.getFormat());
	}
	/**
	 * @since 29.09.2007
	 */
	public void initializeProcessor(AudioFormat audioFormat)
	{
		this.audioFormat = audioFormat;

		isBigEndian = audioFormat.isBigEndian();
		isSigned = audioFormat.getEncoding().equals(Encoding.PCM_SIGNED);
		sampleSizeInBits = audioFormat.getSampleSizeInBits();
		bytesPerChannel = sampleSizeInBits>>3;
		mask = (long)(1L<<sampleSizeInBits)-1;
		neg_Bit = (long)(1L<<(sampleSizeInBits-1));
		maxSample = neg_Bit - 1;
		minSample = -neg_Bit;
		neg_mask = 0xFFFFFFFF ^ mask;

		sampleBufferSize = (sourceDataLine==null)?SAMPLEBUFFERSIZE:sourceDataLine.getBufferSize();
		sampleBuffer = new float [sampleBufferSize];
		resultSampleBuffer = new byte [sampleBufferSize * bytesPerChannel];
		currentWritePosition = 0;
		internalFramePosition = 0;
		useInternalCounter = false;
		
		initializeEffects(audioFormat, sampleBufferSize);
		
		processorThread = new ProcessorTask(this);
		processorThread.start();
	}
	/**
	 * @since 29.09.2007
	 */
	public void stop()
	{
		if (processorThread!=null)
		{
			processorThread.stopProcessorTask();
			processorThread = null;
			sampleBuffer = null;
		}
	}
	/**
	 * @return the dspEnabled
	 */
	public boolean isDspEnabled()
	{
		return dspEnabled;
	}
	/**
	 * @param dspEnabled the dspEnabled to set
	 */
	public void setDspEnabled(boolean dspEnabled)
	{
		this.dspEnabled = dspEnabled;
	}
	/**
	 * @return the resultSampleBuffer
	 */
	public byte[] getResultSampleBuffer()
	{
		return resultSampleBuffer;
	}
	/**
	 * @since 27.12.2011
	 * @param newSampleData
	 * @param offset
	 * @param length
	 * @return
	 */
	private int writeIntoFloatArrayBuffer(final int anzSamples)
	{
		int ox = 0;
		int wx = currentWritePosition;
		while (ox<anzSamples)
		{
			long sample = 0;
			if (isBigEndian)
			{
				for (int b=bytesPerChannel-1, s=0; b>=0; b--, s+=8)
					sample |= ((long)(resultSampleBuffer[ox+b]&0xFF))<<s;
			}
			else
			{
				for (int b=0, s=0; b<bytesPerChannel; b++, s+=8)
					sample |= ((long)(resultSampleBuffer[ox+b]&0xFF))<<s;
			}
			if (isSigned)
			{
				if ((sample & neg_Bit)!=0) sample |= neg_mask;
			}
			else
			{
				sample = (sample & mask) - neg_Bit;
			}
			sampleBuffer[(wx++) % sampleBufferSize] = (float)sample / (float)neg_Bit;
			ox+=bytesPerChannel;
		}
		return wx - currentWritePosition;
	}
	/**
	 * @since 27.12.2011
	 * @param currentReadPosition
	 * @param anzSamples
	 * @return
	 */
	private int readFromFloatArrayBuffer(final int anzSamples)
	{
		int rx = currentWritePosition;
		int ox = 0;
		for (int i=0; i<anzSamples; i++, ox+=bytesPerChannel)
		{
			long sample = (long)((sampleBuffer[(rx++) % sampleBufferSize]*(float)neg_Bit));
			
			if (sample > maxSample) sample = maxSample;
			else if (sample < minSample) sample = minSample;

			if (!isSigned) sample += neg_Bit;
			if (isBigEndian)
			{
				for (int b=bytesPerChannel-1, s=0; b>=0; b--, s+=8)
					resultSampleBuffer[ox+b] = (byte)((sample>>s)&0xFF);
			}
			else
			{
				for (int b=0, s=0; b<bytesPerChannel; b++, s+=8)
					resultSampleBuffer[ox+b] = (byte)((sample>>s)&0xFF);
			}
		}
		return ox;
	}
	/**
	 * This method will write the sample data to the dsp buffer
	 * It will convert all sampledata to a stereo or mono float of 1.0<=x<=-1.0
	 * @since 23.12.2011
	 * @param newSampleData
	 * @param offset
	 * @param length
	 */
	public int writeSampleData(final byte [] newSampleData, final int offset, int length)
	{
		synchronized(lock)
		{
			final int chunkSize = (length>resultSampleBuffer.length)?resultSampleBuffer.length:length;
			System.arraycopy(newSampleData, offset, resultSampleBuffer, 0, chunkSize);
			int anzSamples = writeIntoFloatArrayBuffer(chunkSize);
			if (dspEnabled)
			{
				// call the callbacks for digital signal processing
				// ...
				anzSamples = callEffects(sampleBuffer, currentWritePosition, anzSamples);
				// and recalc from the float array...
				/*int written = */readFromFloatArrayBuffer(anzSamples);
			}
			currentWritePosition = (currentWritePosition + anzSamples) % sampleBufferSize;
			return chunkSize;
		}
	}
	/**
	 * @since 23.12.2011
	 * @param newSampleData
	 */
	public int writeSampleData(final byte [] newSampleData)
	{
		return writeSampleData(newSampleData, 0, newSampleData.length);
	}
}
