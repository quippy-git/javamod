/*
 * @(#) Mixer.java
 *
 * Created on 14.10.2007 by Daniel Becker
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
package de.quippy.javamod.mixer;

import java.io.File;

import javax.sound.sampled.AudioFormat;

import de.quippy.javamod.io.SoundOutputStream;
import de.quippy.javamod.io.SoundOutputStreamImpl;
import de.quippy.javamod.mixer.dsp.AudioProcessor;

/**
 * @author Daniel Becker
 * @since 14.10.2007
 */
public abstract class Mixer
{
	private SoundOutputStream outputStream;
	private AudioProcessor audioProcessor;
	private AudioFormat audioFormat;
	private boolean keepSilent;
	private int sourceLineBufferSize;
	
	private float currentVolume;
	private float currentBalance;
	
	protected boolean playDuringExport;
	protected File exportFile;

	/**
	 * Constructor for Mixer
	 */
	public Mixer()
	{
		super();
		this.outputStream = null;
		this.audioFormat = null;
		this.audioProcessor = null;
		this.exportFile = null;
		this.playDuringExport = false;
		this.keepSilent = false;
		this.currentVolume = 1.0f;
		this.currentBalance = 0.0f;
		this.sourceLineBufferSize = -1;
	}
	/**
	 * @param audioFormat the audioFormat to set
	 */
	protected void setAudioFormat(final AudioFormat audioFormat)
	{
		this.audioFormat = audioFormat;
	}
	/**
	 * @return AudioFormat
	 */
	protected AudioFormat getAudioFormat()
	{
		return this.audioFormat;
	}
	/**
	 * @param audioProcessor the audioProcessor to set
	 */
	public void setAudioProcessor(final AudioProcessor audioProcessor)
	{
		this.audioProcessor = audioProcessor;
	}
	/**
	 * Delegate to the universal outputStream. This will then set
	 * the MasterGain of the sound output, but never the wav writer
	 * This will only succeed, if "openAudioDevice" was called once!
	 * @since 01.11.2008
	 * @param newVolume
	 */
	public void setVolume(final float newVolume)
	{
		currentVolume = newVolume;
		if (outputStream!=null) outputStream.setVolume(newVolume);
	}
	/**
	 * Delegate to the universal outputStream. This will then set
	 * the Balance of the sound output, but never the wav writer
	 * This will only succeed, if "openAudioDevice" was called once!
	 * @since 01.11.2008
	 * @param newVolume
	 */
	public void setBalance(final float newBalance)
	{
		currentBalance = newBalance;
		if (outputStream!=null) outputStream.setBalance(newBalance);
	}
	/**
	 * Delete external stream by setting it to null.
	 * @param outputStream the outputStream to set
	 * @since 25.02.2011
	 */
	public void setSoundOutputStream(final SoundOutputStream newOutputStream)
	{
		outputStream = newOutputStream;
	}
	/**
	 * @param exportFile the exportFile to set
	 */
	public void setExportFile(final File exportFile)
	{
		this.exportFile = exportFile;
	}
	/**
	 * @param exportFile the exportFile to set
	 */
	public void setExportFile(final String exportFileName)
	{
		if (exportFileName!=null) this.exportFile = new File(exportFileName);
	}
	/**
	 * @param playDuringExport the playDuringExport to set
	 */
	public void setPlayDuringExport(final boolean playDuringExport)
	{
		this.playDuringExport = playDuringExport;
	}
	/**
	 * @param keepSilent the keepSilent to set
	 */
	public void setKeepSilent(final boolean keepSilent)
	{
		this.keepSilent = keepSilent;
	}
	/**
	 * @param bufferSize the bufferSize to set
	 */
	public void setSourceLineBufferSize(final int sourceLineBufferSize)
	{
		this.sourceLineBufferSize = sourceLineBufferSize;
	}
	/**
	 * @since 14.10.2007
	 * @param samples
	 * @param start
	 * @param length
	 */
	protected void writeSampleDataToLine(byte[] samples, int start, int length)
	{
		if (outputStream!=null) outputStream.writeSampleData(samples, start, length);
	}
	/**
	 * @since 27.11.2010
	 * @param newPosition
	 */
	protected void setInternalFramePosition(long newPosition)
	{
		if (outputStream!=null) outputStream.setInternalFramePosition(newPosition);
	}
	/**
	 * @since 14.10.2007
	 */
	protected void openAudioDevice()
	{
		closeAudioDevice();
		if (outputStream == null)
		{
			outputStream = new SoundOutputStreamImpl(this.audioFormat, this.audioProcessor, this.exportFile, this.playDuringExport, this.keepSilent, this.sourceLineBufferSize);
		}
		else
		{
			outputStream.changeAudioFormatTo(this.audioFormat, this.sourceLineBufferSize);
			outputStream.setAudioProcessor(this.audioProcessor);
			outputStream.setExportFile(this.exportFile);
			outputStream.setPlayDuringExport(playDuringExport);
			outputStream.setKeepSilent(this.keepSilent);
		}
		outputStream.setVolume(currentVolume);
		outputStream.setBalance(currentBalance);
		outputStream.open();
	}
	/**
	 * @since 14.10.2007
	 */
	protected void closeAudioDevice()
	{
		if (outputStream!=null)
		{
			outputStream.close();
//			outputStream = null;
		}
	}
	protected void fullyCloseAudioDevice()
	{
		if (outputStream!=null)
		{
			outputStream.closeAllDevices();
		}
	}
	protected void stopLine(final boolean flushOrDrain)
	{
		if (outputStream!=null) outputStream.stopLine(flushOrDrain);
	}
	protected void startLine(final boolean flushOrDrain)
	{
		if (outputStream!=null) outputStream.startLine(flushOrDrain);
	}
	protected void flushLine()
	{
		if (outputStream!=null) outputStream.flushLine();
	}
	protected void drainLine()
	{
		if (outputStream!=null) outputStream.drainLine();
	}
	protected boolean isInitialized()
	{
		if (outputStream!=null) return outputStream.isInitialized();
		return false;
	}
	public abstract boolean isPaused();
	public abstract boolean isPausing();
	public abstract boolean isNotSeeking();
	public abstract boolean isInSeeking();
	public abstract boolean isSeeking();
	public abstract boolean isNotPausingNorPaused();
	public abstract boolean isStopped();
	public abstract boolean isStopping();
	public abstract boolean isNotStoppingNorStopped();
	public abstract boolean isPlaying();
	public abstract boolean hasFinished();
	public abstract void stopPlayback();
	public abstract void pausePlayback();
	public abstract void startPlayback();
	public abstract boolean isSeekSupported();
	public abstract void setMillisecondPosition(long milliseconds);
	public abstract void setStopMillisecondPosition(long milliseconds);
	public abstract long getLengthInMilliseconds();
	public abstract long getMillisecondPosition();
	public abstract int getChannelCount();
	public abstract int getCurrentKBperSecond();
	public abstract int getCurrentSampleRate();
}
