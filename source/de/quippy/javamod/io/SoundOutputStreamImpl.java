/*
 * @(#) SoundOutputStreamImpl.java
 *
 * Created on 30.12.2007 by Daniel Becker
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
package de.quippy.javamod.io;

import java.io.File;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;

import de.quippy.javamod.io.wav.WaveFile;
import de.quippy.javamod.mixer.dsp.AudioProcessor;
import de.quippy.javamod.system.Helpers;
import de.quippy.javamod.system.Log;

/**
 * This outputstream will wrap audiolines and file-exports
 * so that the mixers do not have to think about it.
 * @author Daniel Becker
 * @since 30.12.2007
 */
public class SoundOutputStreamImpl implements SoundOutputStream
{
	protected AudioProcessor audioProcessor;
	protected AudioFormat audioFormat;
	protected File exportFile;
	
	protected float currentVolume;
	protected float currentBalance;

	protected SourceDataLine sourceLine;
	protected WaveFile waveExportFile;
	protected boolean playDuringExport;
	protected boolean keepSilent;
	
	public SoundOutputStreamImpl()
	{
		super();
	}
	/**
	 * Constructor for SoundOutputStreamImpl
	 * @param audioFormat		the Format of delivered Audio
	 * @param audioProcessor	the class of the audioProcessor - if any
	 * @param exportFile		exportFile - the File to write to
	 * @param playDuringExport	if true, data will be send to line and file
	 * @param keepSilent		if true, 0 bytes will be send to the line
	 */
	public SoundOutputStreamImpl(final AudioFormat audioFormat, final AudioProcessor audioProcessor, final File exportFile, final boolean playDuringExport, final boolean keepSilent)
	{
		this();
		this.audioFormat = audioFormat;
		this.audioProcessor = audioProcessor;
		this.exportFile = exportFile;
		this.playDuringExport = playDuringExport;
		this.keepSilent = keepSilent;
	}
	/**
	 * @since 30.12.2007
	 */
	protected synchronized void openSourceLine()
	{
		if (audioFormat!=null)
		{
			try
			{
				closeSourceLine();
				closeAudioProcessor();
				DataLine.Info sourceLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
				if (AudioSystem.isLineSupported(sourceLineInfo))
				{
					//sourceLineInfo.getFormats();
					sourceLine = (SourceDataLine) AudioSystem.getLine(sourceLineInfo);
					sourceLine.open();
					sourceLine.start();
					setVolume(currentVolume);
					setBalance(currentBalance);
					openAudioProcessor();
				}
				else
					Log.info("Audioformat is not supported");
			}
			catch (Exception ex)
			{
				sourceLine = null;
				Log.error("Error occured when opening audio device", ex);
			}
		}
	}
	/**
	 * @since 30.12.2007
	 */
	protected synchronized void openAudioProcessor()
	{
		if (audioProcessor!=null)
		{
			if (sourceLine!=null)
			{
				audioProcessor.initializeProcessor(sourceLine);
				audioProcessor.setUseInternalCounter(keepSilent);
			}
			else
			{
				audioProcessor.initializeProcessor(audioFormat);
				audioProcessor.setUseInternalCounter(true);
			}
		}
	}
	protected synchronized void openExportFile()
	{
		if (exportFile!=null)
		{
			waveExportFile = new WaveFile();
			final int result = waveExportFile.openForWrite(exportFile, audioFormat); 
			if (result!=WaveFile.DDC_SUCCESS)
			{
				waveExportFile = null;
				Log.error("Creation of exportfile was NOT successfull! " + exportFile.getAbsolutePath());
			}
		}
	}
	/**
	 * @since 30.12.2007
	 */
	protected synchronized void closeSourceLine()
	{
		if (sourceLine!=null)
		{
			stopLine(true);
			// should be closed and null now
			if (sourceLine!=null)
			{
				sourceLine.close();
				sourceLine = null;
			}
		}
	}
	/**
	 * @since 30.12.2007
	 */
	protected synchronized void closeAudioProcessor()
	{
		if (audioProcessor!=null) audioProcessor.stop();
	}
	/**
	 * @since 30.12.2007
	 */
	protected synchronized void closeExportFile()
	{
		if (waveExportFile!=null) waveExportFile.close();
	}
	/**
	 * @since 30.12.2007
	 */
	public synchronized void open()
	{
		close();
		if (playDuringExport || exportFile==null) openSourceLine();
		openExportFile();
	}
	/**
	 * @since 30.12.2007
	 */
	public synchronized void close()
	{
		closeSourceLine();
		closeAudioProcessor();
		closeExportFile();
	}
	public synchronized void closeAllDevices()
	{
		close();
	}
	/**
	 * @since 30.12.2007
	 * @return
	 */
	public synchronized boolean isInitialized()
	{
		return (sourceLine!=null && sourceLine.isOpen()) || exportFile!=null;
	}
	/**
	 * @since 30.12.2007
	 */
	public synchronized void startLine(final boolean closeWhenOpen)
	{
		// if there is a line, flush or drain it 
		if (sourceLine!=null && closeWhenOpen)
		{
			stopLine(closeWhenOpen); // if running, drain or flush and close the line
		}
		// now start it - if sourceLine is null, open a new line
		// stopping sourceline will eventually make it null - so no ELSE here!
		if (sourceLine==null)
			openSourceLine();
		else
			sourceLine.start();
	}
	/**
	 * @since 30.12.2007
	 */
	public synchronized void stopLine(final boolean closeLine)
	{
		if (sourceLine!=null)
		{
			// play, whatever is left in the buffers. Caution! Will block, until everything is played
			if (closeLine)
			{
				if (sourceLine.isOpen() && sourceLine.isRunning()) 
					drainLine();
				else
					flushLine();
			}
			sourceLine.stop();
			if (closeLine)
			{
				sourceLine.close();
				sourceLine = null;
			}
		}
	}
	/**
	 * Manually flush the line
	 * @see de.quippy.javamod.io.SoundOutputStream#flushLine()
	 */
	@Override
	public void flushLine()
	{
		if (sourceLine!=null)
		{
			sourceLine.flush();
			// on Linux a small amount can be left over - but waiting is not reliable
			//try { Thread.sleep(150L); } catch (InterruptedException ex) { /*NOOP*/ }
		}
	}
	/**
	 * BLOCKING Method!!!
	 * Will play buffer, till empty
	 * If a thread is still pumping data in or if the line is closed
	 * draining will run forever - at least very long
	 * No Check, if line is stopped, is done here!
	 * @see de.quippy.javamod.io.SoundOutputStream#drainLine()
	 */
	@Override
	public void drainLine()
	{
		if (sourceLine!=null)
		{
			sourceLine.drain();
			// on Linux a small amount can be left over - but waiting is not reliable
			//try { Thread.sleep(150L); } catch (InterruptedException ex) { /*NOOP*/ }
		}
	}
	/**
	 * @since 27.12.2011
	 * @param samples
	 * @param start
	 * @param length
	 */
	protected synchronized void writeSampleDataInternally(final byte[] samples, final int start, final int length)
	{
		if (sourceLine!=null && !keepSilent) sourceLine.write(samples, start, length);
		if (waveExportFile!=null) waveExportFile.writeSamples(samples, start, length);
	}
	/**
	 * @since 30.12.2007
	 * @param samples
	 * @param start
	 * @param length
	 */
	public synchronized void writeSampleData(final byte[] samples, final int start, final int length)
	{
		if (audioProcessor!=null)
		{
			int bytesToWrite = length;
			int startFrom = start;
			while (bytesToWrite>0)
			{
				int anzSamples = audioProcessor.writeSampleData(samples, startFrom, bytesToWrite);
				writeSampleDataInternally(audioProcessor.getResultSampleBuffer(), 0, anzSamples);
				startFrom += anzSamples;
				bytesToWrite -= anzSamples;
			}
		}
		else
			writeSampleDataInternally(samples, start, length);
	}
	/**
	 * @since 27.11.2010
	 * @param newFramePosition
	 * @see de.quippy.javamod.io.SoundOutputStream#setInternalFramePosition(long)
	 */
	public synchronized void setInternalFramePosition(final long newFramePosition)
	{
		if (audioProcessor!=null) audioProcessor.setInternalFramePosition(newFramePosition);
	}
	/**
	 * @since 27.12.2012
	 * @return -1 if no frameposition is available
	 * @see de.quippy.javamod.io.SoundOutputStream#getFramePosition()
	 */
	public synchronized long getFramePosition()
	{
		if (audioProcessor!=null) return audioProcessor.getFramePosition();
		else
		if (sourceLine!=null) return sourceLine.getLongFramePosition();
		else
			return -1;
	}
	/**
	 * Set the Gain of the sourceLine
	 * @since 01.11.2008
	 * @param gain
	 */
	public synchronized void setVolume(final float gain)
	{
		if (currentVolume != gain)
		{
			currentVolume = gain;
		    if (sourceLine!=null && sourceLine.isControlSupported(FloatControl.Type.MASTER_GAIN))
		    {
		    	FloatControl gainControl = (FloatControl)sourceLine.getControl(FloatControl.Type.MASTER_GAIN);
		        float dB = (float)(Helpers.getDBValueFrom(gain));
		        if (dB > gainControl.getMaximum()) dB = gainControl.getMaximum();
		        else
		        if (dB < gainControl.getMinimum()) dB = gainControl.getMinimum();
	        	gainControl.setValue(dB);
		    }
		}
	}
	/**
	 * Set the Balance of the sourceLine
	 * @since 01.11.2008
	 * @param gain
	 */
	public synchronized void setBalance(final float balance)
	{
		if (currentBalance != balance)
		{
			currentBalance = balance;
		    if (sourceLine!=null && sourceLine.isControlSupported(FloatControl.Type.BALANCE))
		    {
		    	FloatControl balanceControl = (FloatControl)sourceLine.getControl(FloatControl.Type.BALANCE);
		    	if (balance <= balanceControl.getMaximum() && balance >= balanceControl.getMinimum())
		    		balanceControl.setValue(balance);
		    }
		}
	}
	/**
	 * @param audioProcessor the audioProcessor to set
	 * @since 25.02.2011
	 */
	public synchronized void setAudioProcessor(final AudioProcessor audioProcessor)
	{
		this.audioProcessor = audioProcessor;
	}
	/**
	 * @param exportFile the exportFile to set
	 * @since 25.02.2011
	 */
	public synchronized void setExportFile(final File exportFile)
	{
		this.exportFile = exportFile;
	}
	/**
	 * @param waveExportFile the waveExportFile to set
	 * @since 25.02.2011
	 */
	public synchronized void setWaveExportFile(final WaveFile waveExportFile)
	{
		this.waveExportFile = waveExportFile;
	}
	/**
	 * @param playDuringExport the playDuringExport to set
	 * @since 25.02.2011
	 */
	public synchronized void setPlayDuringExport(final boolean playDuringExport)
	{
		this.playDuringExport = playDuringExport;
	}
	/**
	 * @param keepSilent the keepSilent to set
	 * @since 25.02.2011
	 */
	public synchronized void setKeepSilent(final boolean keepSilent)
	{
		this.keepSilent = keepSilent;
	}
	public boolean matches(final SoundOutputStream otherStream)
	{
		return getAudioFormat().matches(otherStream.getAudioFormat());
	}
	/** 
	 * @see de.quippy.javamod.io.SoundOutputStream#getAudioFormat()
	 */
	public synchronized AudioFormat getAudioFormat()
	{
		return audioFormat;
	}
	/**
	 * @see de.quippy.javamod.io.SoundOutputStream#changeAudioFormatTo(javax.sound.sampled.AudioFormat)
	 */
	public synchronized void changeAudioFormatTo(final AudioFormat newAudioFormat)
	{
		boolean reOpen = sourceLine!=null && sourceLine.isOpen();
		close();
		audioFormat = newAudioFormat;
		if (reOpen) open();
	}
}
