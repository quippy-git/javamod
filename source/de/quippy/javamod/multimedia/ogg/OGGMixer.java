/*
 * @(#) OGGMixer.java
 *
 * Created on 01.11.2010 by Daniel Becker
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
package de.quippy.javamod.multimedia.ogg;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.sound.sampled.AudioFormat;

import de.quippy.javamod.io.FileOrPackedInputStream;
import de.quippy.javamod.mixer.BasicMixer;
import de.quippy.javamod.system.Log;
import de.quippy.ogg.jogg.Packet;
import de.quippy.ogg.jogg.Page;
import de.quippy.ogg.jogg.StreamState;
import de.quippy.ogg.jogg.SyncState;
import de.quippy.ogg.jorbis.Block;
import de.quippy.ogg.jorbis.Comment;
import de.quippy.ogg.jorbis.DspState;
import de.quippy.ogg.jorbis.Info;


/**
 * @author Daniel Becker
 * @since 01.11.2010
 */
public class OGGMixer extends BasicMixer
{
	private static final int STATE_INITIAL = 0;
	private static final int STATE_READHEADER = 1;
	private static final int STATE_PREPARE = 2;
	private static final int STATE_READFIRSTFRAME = 3;
	private static final int STATE_PROCESSPACKET = 4;
	private static final int STATE_NEEDMOREDATA = 5;
	private static final int STATE_CONVERTPCM = 6;
	private static final int STATE_EOS = 7;

	private static final int CHUNKSIZE = 4096;

	private boolean oggEOS;
	private int decoderState;

	private SyncState oggSyncState;
	private StreamState oggStreamState;
	private Page oggPage;
	private Packet oggPacket;
	private Info vorbisInfo;
	private Comment vorbisComment;
	private DspState vorbisDSPState;
	private Block vorbisBlock;

	private final float[][][] pcmFloatBuffer = new float[1][][];
	private int[] pcmGeneratorIndex;

	private int bufferSize;
	private byte[] output;
	private int samplesProcessed;
	
	private long currentSamplesWritten;
	private int lengthInMilliseconds;
	
	private InputStream inputStream;
	private URL oggFileUrl;
	
	/**
	 * Constructor for OGGMixer
	 */
	public OGGMixer(URL oggFileUrl, int lengthInMilliseconds)
	{
		super();
		this.oggFileUrl = oggFileUrl;
		this.lengthInMilliseconds = lengthInMilliseconds;
	}
	private void initialize()
	{
		try
		{
			if (inputStream!=null) try { inputStream.close(); inputStream = null; } catch (IOException e) { Log.error("IGNORED", e); }
			
			inputStream = new FileOrPackedInputStream(oggFileUrl);
			
			oggEOS = false;
			decoderState = STATE_INITIAL;
			
			bufferSize = 0;
			output = null;
		}
		catch (Exception ex)
		{
			if (inputStream!=null) try { inputStream.close(); inputStream = null; } catch (IOException e) { Log.error("IGNORED", e); }
			Log.error("[OGGMixer]", ex);
		}
	}
	/**
	 * @return
	 * @see de.quippy.javamod.mixer.Mixer#getChannelCount()
	 */
	@Override
	public int getChannelCount()
	{
		if (vorbisInfo!=null) return vorbisInfo.channels;
		return 0;
	}
	/**
	 * @return
	 * @see de.quippy.javamod.mixer.Mixer#getCurrentKBperSecond()
	 */
	@Override
	public int getCurrentKBperSecond()
	{
		if (vorbisInfo!=null)
		{
			int bitRate = vorbisInfo.bitrate();
			if (bitRate==-1) return (16*vorbisInfo.rate*vorbisInfo.channels) / 1000;
			else return bitRate / 1000;
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
		if (vorbisInfo!=null) return vorbisInfo.rate;
		return 0;
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
		if (vorbisInfo!=null && vorbisInfo.rate!=0)
			return (long)currentSamplesWritten * 1000L / (long)vorbisInfo.rate;
		else
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
	protected void seek(long milliseconds)
	{
		try
		{
			if (milliseconds < getMillisecondPosition())
			{
				cleanUp();
				initialize();
			}
			int byteCount = 1;
			while (getMillisecondPosition()<milliseconds && byteCount>0) 
				byteCount = decodeFrame();
		}
		catch (Exception ex)
		{
			Log.error("[OGGMixer]", ex);
		}
	}
	public int decodeFrame() throws Exception
	{
		while (true)
		{
			switch (decoderState)
			{
				case STATE_EOS:
//					if (inputStream.available()>0) // Are we streaming?!
//					{
//						decoderState = STATE_INITIAL;
//						OggMetaData metaData = new OggMetaData(inputStream);
//						break;
//					}
//					else
						return -1;
				case STATE_INITIAL:
					decoderState = doStateInitial();
					break;
				case STATE_READHEADER:
					decoderState = doStateReadHeader();
					break;
				case STATE_PREPARE:
					decoderState = doStatePrepare();
					break;
				case STATE_READFIRSTFRAME:
					decoderState = STATE_NEEDMOREDATA;
					break;
				case STATE_NEEDMOREDATA:
					decoderState = doStateNeedMoreData();
					break;
				case STATE_PROCESSPACKET:
					decoderState = doStateProcessPacket();
					break;
				case STATE_CONVERTPCM:
					decoderState = doStateConvertPCM();
					if (decoderState == STATE_CONVERTPCM) 
						return samplesProcessed * 2 * vorbisInfo.channels;
					break;
				default:
					throw new IOException("invalid decoder state " + decoderState);
			}
		}
	}
	private void fetchMoreData() throws IOException
	{
		if (!oggEOS)
		{
			final int oggIndex = oggSyncState.buffer(CHUNKSIZE);
			final int bytesRead = inputStream.read(oggSyncState.data, oggIndex, CHUNKSIZE);
			if (bytesRead <= 0)
			{
				oggEOS = true;
			}
			else
			{
				oggSyncState.wrote(bytesRead);
			}
		}
	}
	private int doStateConvertPCM() throws Exception
	{
		int nextState = STATE_PROCESSPACKET;
		int samplesGenerated = vorbisDSPState.synthesis_pcmout(pcmFloatBuffer, pcmGeneratorIndex);
		if (samplesGenerated > 0)
		{
			samplesProcessed = (samplesGenerated > bufferSize) ? bufferSize : samplesGenerated;
			for (int i = 0; i < vorbisInfo.channels; i++)
			{
				int sampleIndex = i<<1;

				// For every sample in our range...
				for(int j = 0; j < samplesProcessed; j++)
				{
					int value = (int) (pcmFloatBuffer[0][i][pcmGeneratorIndex[i] + j] * 32767);

					if (value > 32767) value = 32767;
					else
					if (value < -32768) value = -32768;

					if (value < 0) value |= 0x8000;

					output[sampleIndex] = (byte) (value&0xFF);
					output[sampleIndex + 1] = (byte)((value >> 8)&0xFF);

					sampleIndex += vorbisInfo.channels<<1;
				}
			}
			currentSamplesWritten += samplesProcessed;
			vorbisDSPState.synthesis_read(samplesProcessed);
			nextState = STATE_CONVERTPCM;
		}
		return nextState;
	}
	private int doStateNeedMoreData() throws Exception
	{
		if (oggEOS)
		{
			return STATE_EOS;
		}
		while (!oggEOS)
		{
			int result;
			do
			{
				result = oggSyncState.pageout(oggPage);
				if (result == 0)
				{
					fetchMoreData();
				}
			}
			while (!oggEOS && result == 0);

			if (result == -1)
			{
				// missing or corrupt data at this page
				continue;
			}

			oggStreamState.pagein(oggPage);
			if (oggPage.granulepos() == 0)
			{
				oggEOS = true;
				return STATE_EOS;
			}
			break;
		}
		return STATE_PROCESSPACKET;
	}
	private int doStateProcessPacket() throws Exception
	{
		int nextState = STATE_PROCESSPACKET;
		int result = oggStreamState.packetout(oggPacket);
		if (result == 0)
		{
			if (!oggEOS)
			{
				oggEOS = oggPage.eos() != 0;
			}
			nextState = STATE_NEEDMOREDATA;
		}
		else if (result == -1)
		{
			// missing or corrupt data at this page position
		}
		else
		{
			// we have a packet. Decode it
			if (vorbisBlock.synthesis(oggPacket) == 0)
			{ // test for success!
				vorbisDSPState.synthesis_blockin(vorbisBlock);
			}
			nextState = STATE_CONVERTPCM;
		}
		return nextState;
	}
	private int doStateInitial() throws Exception
	{
		oggSyncState = new SyncState();
		oggStreamState = new StreamState();
		oggPage = new Page();
		oggPacket = new Packet();

		vorbisInfo = new Info();
		vorbisComment = new Comment();
		vorbisDSPState = new DspState();
		vorbisBlock = new Block(vorbisDSPState);

		oggSyncState.init();
		oggEOS = false;

		return STATE_READHEADER;
	}
	private int doStateReadHeader() throws Exception
	{
		fetchMoreData();

		if (oggSyncState.pageout(oggPage) != 1)
		{
			throw new IOException("Input does not appear to be an Ogg bitstream");
		}

		oggStreamState.init(oggPage.serialno());
		oggStreamState.reset();

		if (oggStreamState.pagein(oggPage) < 0)
		{
			throw new IOException("Error reading first page of Ogg bitstream data");
		}

		if (oggStreamState.packetout(oggPacket) != 1)
		{
			// no page? must not be vorbis
			throw new IOException("Error reading initial header packet");
		}

		vorbisInfo.init();
		vorbisComment.init();

		if (vorbisInfo.synthesis_headerin(vorbisComment, oggPacket) < 0)
		{
			// error case; not a vorbis header
			throw new IOException("This Ogg bitstream does not contain Vorbis audio data");
		}

		int i = 0;

		while (i < 2)
		{
			while (i < 2)
			{
				int result = oggSyncState.pageout(oggPage);
				if (result == 0)
				{
					break; // Need more data
				}
				else if (result == 1)
				{
					oggStreamState.pagein(oggPage);
					while (i < 2)
					{
						result = oggStreamState.packetout(oggPacket);
						if (result == 0) break;
						if (result == -1)
						{
							throw new IOException("Corrupt secondary header");
						}
						vorbisInfo.synthesis_headerin(vorbisComment, oggPacket);
						i++;
					}
				}
				else
				{
					throw new IOException("Unhandled pageout() return code " + result);
				}
			}

			fetchMoreData();
			if (oggEOS)
			{
				throw new IOException("End of file before finding all Vorbis headers");
			}
		} // while i < 2
		return STATE_PREPARE;
	}
	private final int doStatePrepare() throws Exception
	{
		vorbisDSPState.synthesis_init(vorbisInfo);
		vorbisBlock.init(vorbisDSPState);

		pcmGeneratorIndex = new int[vorbisInfo.channels];
	
		currentSamplesWritten = 0;

		// create SampleBuffer for output
		bufferSize = 250 * vorbisInfo.channels * vorbisInfo.rate / 1000; // 250ms buffer

		// Now for the bits (linebuffer):
		bufferSize<<=1;
		output = new byte[bufferSize];

		AudioFormat audioFormat = new AudioFormat((float)vorbisInfo.rate, 16, vorbisInfo.channels, true, false);  
		setAudioFormat(audioFormat);

		openAudioDevice();

		return STATE_READFIRSTFRAME;
	}
	private void cleanUp()
	{
		if (oggStreamState!=null) { oggStreamState.clear(); oggStreamState = null; }
		if (vorbisBlock!=null) { vorbisBlock.clear(); vorbisBlock = null; }
		if (vorbisDSPState!=null) { vorbisDSPState.clear(); vorbisDSPState = null; }
		if (vorbisInfo!=null) { vorbisInfo.clear(); vorbisInfo = null; }
		if (oggSyncState!=null) { oggSyncState.clear(); oggSyncState = null; }
		if (inputStream!=null) try { inputStream.close(); inputStream = null; } catch (IOException e) { Log.error("IGNORED", e); }
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
			int byteCount = 0;
			
			do
			{
				final long bytesToWrite = (hasStopPosition())?getSamplesToWriteLeft() * getChannelCount() * 2:-1;
				byteCount = decodeFrame();
				if (byteCount>0 && isInitialized())
				{
					// find out, if all decoded samples are to write
					if (bytesToWrite>0 && (long)(byteCount)>bytesToWrite) byteCount = (int)bytesToWrite;
					writeSampleDataToLine(output, 0, byteCount);
					
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
							try { Thread.sleep(10L); } catch (InterruptedException ex) { /* noop */ }
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
			}
			while (byteCount!=-1);
			if (byteCount<=0) setHasFinished(); // Piece finished!
		}
		catch (Throwable ex)
		{
			throw new RuntimeException(ex);
		}
		finally
		{
			setIsStopped();
			closeAudioDevice();
			cleanUp();
		}
	}
}
