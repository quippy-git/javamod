/*
 * @(#) FLACMixer.java
 *
 * Created on 01.01.2011 by Daniel Becker
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
package de.quippy.javamod.multimedia.flac;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.sound.sampled.AudioFormat;

import de.quippy.javamod.io.FileOrPackedInputStream;
import de.quippy.javamod.mixer.BasicMixer;
import de.quippy.javamod.system.Log;
import de.quippy.jflac.FLACDecoder;
import de.quippy.jflac.FrameDecodeException;
import de.quippy.jflac.frame.Frame;
import de.quippy.jflac.frame.Header;
import de.quippy.jflac.util.ByteData;

/**
 * @author Daniel Becker
 * @since 01.01.2011
 */
public class FLACMixer extends BasicMixer
{
	private InputStream inputStream;
	private FLACDecoder decoder;

	private final URL flacFileUrl;

	private int channels;
	private int sampleRate;
	private int sampleSizeInBytes;
	private int lengthInMilliseconds;

	/**
	 * Constructor for FLACMixer
	 */
	public FLACMixer(final URL flacFileUrl)
	{
		super();
		this.flacFileUrl = flacFileUrl;
		initialize();
	}
	private void initialize()
	{
		try
		{
			if (inputStream!=null) try { inputStream.close(); } catch (final IOException ex) { /* Log.error("IGNORED", ex); */ }
			inputStream = new FileOrPackedInputStream(flacFileUrl);
			decoder = new FLACDecoder(inputStream);
			decoder.readMetadata();
			final AudioFormat audioFormat = decoder.getStreamInfo().getAudioFormat();
			setAudioFormat(audioFormat);
			channels = audioFormat.getChannels();
			sampleRate = (int)audioFormat.getSampleRate();
			int sampleSizeInBits = audioFormat.getSampleSizeInBits();
			sampleSizeInBytes = sampleSizeInBits>>3;
			lengthInMilliseconds = (int)(decoder.getStreamInfo().getTotalSamples() * 1000L / sampleRate);
		}
		catch (final Exception ex)
		{
			if (inputStream!=null) try { inputStream.close(); inputStream = null; } catch (final IOException e) { /* Log.error("IGNORED", e); */ }
			Log.error("[FLACMixer]", ex);
		}
	}
	/**
	 * @return
	 * @see de.quippy.javamod.mixer.Mixer#getChannelCount()
	 */
	@Override
	public int getChannelCount()
	{
		return channels;
	}
	/**
	 * @return
	 * @see de.quippy.javamod.mixer.Mixer#getCurrentKBperSecond()
	 */
	@Override
	public int getCurrentKBperSecond()
	{
		if (decoder!=null)
		{
			final Frame f = decoder.getCurrentFrame();
			if (f!=null)
			{
				final Header h = f.getHeader();
				if (h!=null) return (h.blockSize * h.bitsPerSample * h.channels) * 1000 / h.sampleRate;
			}
		}
		return 0;
	}
	/**
	 * @return
	 * @see de.quippy.javamod.mixer.Mixer#getCurrentSampleRate()
	 */
	@Override
	public int getCurrentSampleRate()
	{
		return sampleRate;
	}
	/**
	 * @return
	 * @see de.quippy.javamod.mixer.Mixer#getLengthInMilliseconds()
	 */
	@Override
	public long getLengthInMilliseconds()
	{
		return lengthInMilliseconds;
	}
	/**
	 * @return
	 * @see de.quippy.javamod.mixer.Mixer#getMillisecondPosition()
	 */
	@Override
	public long getMillisecondPosition()
	{
		if (decoder != null)
		{
			final Frame f = decoder.getCurrentFrame();
			if (f!=null)
			{
				final Header h = f.getHeader();
				if (h!=null) return h.sampleNumber * 1000L / sampleRate;
			}
			return decoder.getSamplesDecoded() * 1000L / sampleRate;
		}
		return 0;
	}
	/**
	 * @return
	 * @see de.quippy.javamod.mixer.Mixer#isSeekSupported()
	 */
	@Override
	public boolean isSeekSupported()
	{
		return true;
	}
	/**
	 * @param milliseconds
	 * @see de.quippy.javamod.mixer.BasicMixer#seek(long)
	 * @since 13.02.2012
	 */
	@Override
	protected void seek(final long milliseconds)
	{
		try
		{
			final long seekToSamples = milliseconds * sampleRate / 1000L;
			final long currentSamples = getMillisecondPosition() * sampleRate / 1000L;
			if (currentSamples>seekToSamples || decoder.getSeekTable()!=null)
			{
				if (inputStream!=null) try { inputStream.close(); } catch (final IOException ex) { /* Log.error("IGNORED", ex); */ }
				inputStream = flacFileUrl.openStream();
				decoder = new FLACDecoder(inputStream);
				decoder.readMetadata();
			}
			decoder.seekTo(seekToSamples);
		}
		catch (final Throwable ex)
		{
			Log.error("[FLACMixer::seek]", ex);
		}
	}
	private ByteData decode() throws IOException
	{
		try
		{
	        decoder.findFrameSync();
			final Frame currentFrame = decoder.readFrame();
	    	return decoder.decodeFrame(currentFrame, null);
		}
		catch (final FrameDecodeException ex)
		{
			return null;
		}
	}
	/**
	 *
	 * @see de.quippy.javamod.mixer.Mixer#startPlayback()
	 */
	@Override
	public void startPlayback()
	{
		initialize();
		setIsPlaying();

		if (getSeekPosition()>0) seek(getSeekPosition());

		try
		{
			openAudioDevice();
			if (!isInitialized()) return;

			boolean finished = false;

			do
			{
				try
				{
					final long bytesToWrite = (hasStopPosition())?getSamplesToWriteLeft() * getChannelCount() * sampleSizeInBytes:-1;
			    	final ByteData bd = decode();
			    	if (bd!=null)
			    	{
				    	final byte [] b = bd.getData();
				    	int byteCount = bd.getLen();
						// find out, if all decoded samples are to write
						if (bytesToWrite>0 && (byteCount)>bytesToWrite) byteCount = (int)bytesToWrite;

						writeSampleDataToLine(b, 0, byteCount);
			    	}
				}
				catch (final EOFException ex)
				{
					finished = true;
				}

				if (stopPositionIsReached()) setIsStopping();

				if (isStopping())
				{
					setIsStopped();
					break;
				}
				if (isPausing())
				{
					setIsPaused();
					while (isPaused())
					{
						try { Thread.sleep(10L); } catch (final InterruptedException ex) { /*noop*/ }
					}
				}
				if (isInSeeking())
				{
					setIsSeeking();
					while (isInSeeking())
					{
						try { Thread.sleep(10L); } catch (final InterruptedException ex) { /*noop*/ }
					}
				}
			}
			while (!finished);
			if (finished) setHasFinished(); // piece finished
		}
		catch (final Throwable ex)
		{
			throw new RuntimeException(ex);
		}
		finally
		{
			setIsStopped();
			closeAudioDevice();
			if (inputStream!=null) try { inputStream.close(); inputStream = null; } catch (final IOException ex) { /* Log.error("IGNORED", ex); */ }
		}
	}
}
