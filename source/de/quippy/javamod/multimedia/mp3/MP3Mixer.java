/*
 * @(#) MP3Mixer.java
 *
 * Created on 17.10.2007 by Daniel Becker
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
package de.quippy.javamod.multimedia.mp3;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

import javax.sound.sampled.AudioFormat;

import de.quippy.javamod.io.FileOrPackedInputStream;
import de.quippy.javamod.io.HttpResource;
import de.quippy.javamod.mixer.BasicMixer;
import de.quippy.javamod.multimedia.mp3.streaming.IcyInputStream;
import de.quippy.javamod.multimedia.mp3.streaming.TagParseListener;
import de.quippy.javamod.system.Helpers;
import de.quippy.javamod.system.Log;
import de.quippy.mp3.decoder.Bitstream;
import de.quippy.mp3.decoder.BitstreamException;
import de.quippy.mp3.decoder.Decoder;
import de.quippy.mp3.decoder.Header;
import de.quippy.mp3.decoder.SampleBuffer;

/**
 * @author Daniel Becker
 * @since 17.10.2007
 */
public class MP3Mixer extends BasicMixer
{
	private byte [] output;
	
	private HttpResource httpResource;
	private InputStream inputStream;
	private Bitstream bitStream;
	private Decoder	decoder; 
	
	private URL mp3FileUrl;
	
	private TagParseListener tagParseListener;
	
	private long played_ms;
	private long samplesWritten;
	private AudioFormat audioFormat;
	
	private Boolean isStreaming;

	/**
	 * Constructor for MP3Mixer
	 */
	public MP3Mixer(URL mp3FileUrl)
	{
		super();
		this.mp3FileUrl = mp3FileUrl;
	}
	/**
	 * @since 27.12.2008
	 * @param tagParseListener
	 */
	public void setTagParserListener(TagParseListener tagParseListener)
	{
		this.tagParseListener = tagParseListener;
	}
	private InputStream createHttpRessource(final URL mp3FileUrl) throws IOException
	{
		InputStream result = null;
		if (httpResource!=null) httpResource.close();
		httpResource = new HttpResource(mp3FileUrl);
		httpResource.setUser_agent(Helpers.USER_AGENT);
		HashMap<String, String> additionalHeaders = new HashMap<String, String>();
		additionalHeaders.put("Ultravox-transport-type", "TCP");
		additionalHeaders.put("Icy-MetaData", "1");
		result = httpResource.getResource(additionalHeaders, true);
		if (!httpResource.isOK()) result = null;
		return result;
	}
	private void initialize()
	{
		try
		{
			closeAllInputStreams();
			
			if (!isStreaming())
			{
				inputStream = new FileOrPackedInputStream(mp3FileUrl);
			}
			else
			{
				InputStream httpInputStream = createHttpRessource(mp3FileUrl);
				if (httpInputStream!=null)
					inputStream = new IcyInputStream(new BufferedInputStream(httpInputStream), tagParseListener, httpResource.getResourceHeaders());
			}
			if (inputStream==null) throw new IOException("File not found: " + mp3FileUrl);
			this.bitStream = new Bitstream(inputStream);
			this.decoder = new Decoder();
			this.played_ms = 0;
			this.samplesWritten = 0;
			// Setting the AudioFormat is only possible during
			// playback so it is done in startPlayBack
		}
		catch (Exception ex)
		{
			if (inputStream!=null) try { inputStream.close(); inputStream = null; } catch (IOException e) { Log.error("IGNORED", e); }
			Log.error("[MP3Mixer]", ex);
		}
	}
	/**
	 * @since 10.04.2010
	 * @return
	 */
	private boolean isStreaming()
	{
		if (isStreaming==null)
		{
			if (Helpers.isFile(mp3FileUrl)) 
				isStreaming = Boolean.FALSE;
			else
			{
				if (Helpers.isHTTP(mp3FileUrl))
				{
					isStreaming = Boolean.TRUE;
//					try
//					{
//						InputStream inputStream = createHttpRessource(mp3FileUrl);
//						if (inputStream!=null)
//						{
//							inputStream.close();
//							return (isStreaming = Boolean.TRUE).booleanValue();
//						}
//					}
//					catch (Throwable ex)
//					{
//						Log.error("[MP3Mixer::isStreamaing]", ex);
//					}
				}
				else
					isStreaming = Boolean.FALSE;
			}
		}
		return isStreaming.booleanValue();
	}
	/**
	 * 
	 * @see de.quippy.javamod.mixer.Mixer#isSeekSupported()
	 */
	@Override
	public boolean isSeekSupported()
	{
		return !isStreaming();
	}
	/**
	 * 
	 * @see de.quippy.javamod.mixer.Mixer#getMillisecondPosition()
	 */
	@Override
	public long getMillisecondPosition()
	{
		if (!isStreaming())
			return played_ms;
		else
			return 0;
	}
	/**
	 * 
	 * @see de.quippy.javamod.mixer.Mixer#getLengthInMilliseconds()
	 */
	@Override
	public long getLengthInMilliseconds()
	{
		if (!isStreaming())
		{
			try
			{
				initialize();
				Header h = bitStream.readFrame();
				if (h!=null)  
					return (long)(h.total_ms(inputStream.available()) + 0.5);
			}
			catch (Throwable ex)
			{
				Log.error("IGNORED", ex);
			}
		}
		return 0;
	}
	/**
	 * Close all input resources
	 * @since 03.10.2016
	 */
	private void closeAllInputStreams()
	{
		if (bitStream!=null) try { bitStream.close(); bitStream = null; } catch (BitstreamException e) { Log.error("IGNORED", e); }
		if (inputStream!=null) try { inputStream.close(); inputStream = null; } catch (IOException e) { Log.error("IGNORED", e); }
		if (httpResource!=null) try { httpResource.close(); httpResource = null; } catch (IOException e) { Log.error("IGNORED", e); }
		isStreaming = null;
	}
	/**
	 * @param milliseconds
	 * @see de.quippy.javamod.mixer.BasicMixer#seek(long)
	 * @since 13.02.2012
	 */
	@Override
	protected void seek(long milliseconds)
	{
		try
		{
			if (!isStreaming())
			{
				if (played_ms>milliseconds)
				{
					closeAllInputStreams();
					
					inputStream = new FileOrPackedInputStream(mp3FileUrl);
					bitStream = new Bitstream(inputStream);
					this.decoder = new Decoder();
					played_ms = 0;
				}
				
				float f_played_ms = (float)played_ms;
				boolean isFirstFrame = true;
				int sampleRate = 0;
				while (f_played_ms < milliseconds)
				{
					Header h = bitStream.readFrame();
					if (h==null) break;
					f_played_ms += h.ms_per_frame(); 
					bitStream.closeFrame();
					if (isFirstFrame)
					{
						decoder.decodeFrame(h, bitStream);
						sampleRate = decoder.getOutputFrequency();
						isFirstFrame = false;
					}
				}
				played_ms = (int)(f_played_ms + 0.5);
				samplesWritten = (long)(played_ms * (float)sampleRate / 1000.0);
			}
		}
		catch (Throwable ex)
		{
			Log.error("[MP3Mixer]", ex);
		}
	}
	/**
	 * @return
	 * @see de.quippy.javamod.mixer.Mixer#getChannelCount()
	 */
	@Override
	public int getChannelCount()
	{
		if (decoder != null)
		{
			return decoder.getOutputChannels();
		}
		return 0;
	}
	/**
	 * @return
	 * @see de.quippy.javamod.mixer.Mixer#getCurrentKBperSecond()
	 */
	@Override
	public int getCurrentKBperSecond()
	{
		if (bitStream!=null)
		{
			Header h = bitStream.getHeader();
			if (h!=null) return h.bitrate_instant()/1000;
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
		if (decoder!=null)
			return decoder.getOutputFrequency();
		else
			return 0;
	}
	/**
	 * @since 30.03.2010
	 * @param length
	 * @return
	 */
	private byte[] getOutputBuffer(int length)
	{
		if (output==null || output.length<length)
			output = new byte[length];
		return output;
	}
	/**
	 * 
	 * @see de.quippy.javamod.mixer.Mixer#startPlayback()
	 */
	@Override
	public void startPlayback()
	{
		initialize();
		if (bitStream==null) return; // something went wrong...
		
		setIsPlaying();
		
		if (getSeekPosition()>0) seek(getSeekPosition());

		try
		{
			boolean isFirstFrame = true;
			Header h = null;
			int channels = -1;
			
			do
			{
				h = bitStream.readFrame();
				if (h!=null)
				{
					final SampleBuffer output = (SampleBuffer)decoder.decodeFrame(h, bitStream);
					bitStream.closeFrame();

					if (isFirstFrame)
					{
						// At this point we know our AudioFormat
						audioFormat = new AudioFormat(decoder.getOutputFrequency(), 16, channels = decoder.getOutputChannels(), true, false);
						setAudioFormat(audioFormat);
						openAudioDevice();
						if (!isInitialized()) return;
					}
					
					final long samplesToWrite = (hasStopPosition() && channels!=-1)?getSamplesToWriteLeft() * getChannelCount():-1;
					
					final short[] samples = output.getBuffer();
					int origLen = output.getBufferLength();
					// find out, if all decoded samples are to write
					if (samplesToWrite>0 && (long)(origLen)>samplesToWrite) 
						origLen = (int)samplesToWrite;
					samplesWritten += origLen / (long)decoder.getOutputChannels();
					played_ms = samplesWritten * 1000L / (long)decoder.getOutputFrequency();
					final int len = origLen<<1;
					byte[] b = getOutputBuffer(len);
					
					int idx = 0;
					int pos = 0;
					short s;
					boolean allZero = true;
					while (origLen-- > 0)
					{
						s = samples[pos++];
						if (allZero && s!=0) allZero = false;
						b[idx++] = (byte)(s&0xFF);
						b[idx++] = (byte)((s>>8)&0xFF);
					}
	
					if (!isFirstFrame || !allZero) writeSampleDataToLine(b, 0, len);
				}

				isFirstFrame = false;

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
						try { Thread.sleep(10L); } catch (InterruptedException ex) { /*noop*/ }
					}
				}
				if (isInSeeking())
				{
					setIsSeeking();
					while (isInSeeking())
					{
						try { Thread.sleep(10L); } catch (InterruptedException ex) { /*noop*/ }
					}
				}
			}
			while (h!=null);
			if (h==null) setHasFinished(); // piece finished
		}
		catch (Throwable ex)
		{
			throw new RuntimeException(ex);
		}
		finally
		{
			setIsStopped();
			closeAudioDevice();
			closeAllInputStreams();
		}
	}
}
