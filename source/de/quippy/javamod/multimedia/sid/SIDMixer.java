/*
 * @(#) SIDMixer.java
 *
 * Created on 04.10.2009 by Daniel Becker
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
package de.quippy.javamod.multimedia.sid;

import javax.sound.sampled.AudioFormat;

import de.quippy.javamod.mixer.BasicMixer;
import de.quippy.javamod.system.Log;
import de.quippy.sidplay.libsidplay.SIDPlay2;
import de.quippy.sidplay.libsidplay.common.ISID2Types;
import de.quippy.sidplay.libsidplay.common.SIDBuilder;
import de.quippy.sidplay.libsidplay.components.sidtune.SidTune;
import de.quippy.sidplay.resid_builder.ReSIDBuilder;

/**
 * @author Daniel Becker
 * @since 04.10.2009
 */
public class SIDMixer extends BasicMixer
{
	private SIDPlay2 sidPlayer;
	private SidTune sidTune;

	private byte[] output;

	private int sampleRate;
	private int sidModel;
	private int optimization;
	private boolean sidFilter;
	private boolean isStereo;

	private static final int MULTIPLIER_SHIFT = 4;
	private static final int MULTIPLIER_VALUE = 1 << MULTIPLIER_SHIFT;

	private int multiplier;
	private int songNumber;

	private SIDContainer parentSIDContainer;

	/**
	 * Constructor for SIDMixer
	 */
	public SIDMixer(SidTune sidTune, SIDContainer parent, int sampleRate, int sidModel, int optimization, boolean sidFilter, boolean isStereo)
	{
		super();
		this.sidTune = sidTune;
		if (sidTune != null)
		{
			songNumber = sidTune.getInfo().currentSong;
			if (songNumber == 0) songNumber = 1;
		}
		else
			songNumber = 1;
		this.parentSIDContainer = parent;
		this.optimization = optimization;
		this.sampleRate = sampleRate;
		this.sidModel = sidModel;
		this.sidFilter = sidFilter;
		this.isStereo = isStereo;
	}
	private void initialize()
	{
		try
		{
			sidPlayer = new SIDPlay2();

			ISID2Types.sid2_config_t sidConfig = sidPlayer.config();
			sidConfig.frequency = sampleRate;
			sidConfig.emulateStereo = isStereo;
			sidConfig.playback = (isStereo ? ISID2Types.sid2_playback_t.sid2_stereo : ISID2Types.sid2_playback_t.sid2_mono);
			sidConfig.optimisation = (byte)optimization;
			sidConfig.sidModel = (sidModel==0)?ISID2Types.sid2_model_t.SID2_MODEL_CORRECT:((sidModel==1)?ISID2Types.sid2_model_t.SID2_MOS6581:ISID2Types.sid2_model_t.SID2_MOS8580);

			sidConfig.clockDefault = ISID2Types.sid2_clock_t.SID2_CLOCK_CORRECT;
			sidConfig.clockSpeed = ISID2Types.sid2_clock_t.SID2_CLOCK_CORRECT;
			sidConfig.clockForced = false;
			sidConfig.environment = ISID2Types.sid2_env_t.sid2_envR;
			sidConfig.forceDualSids = false;
			sidConfig.leftVolume = 255;
			sidConfig.rightVolume = 255;
			sidConfig.sampleFormat = ISID2Types.sid2_sample_t.SID2_LITTLE_SIGNED;
			sidConfig.sidDefault = ISID2Types.sid2_model_t.SID2_MOS6581;
			sidConfig.sidSamples = true;
			sidConfig.precision = ISID2Types.SID2_DEFAULT_PRECISION;
			sidPlayer.config(sidConfig);
			
			ReSIDBuilder rs = new ReSIDBuilder("ReSID");
			if (rs.bool())
			{
				sidConfig.sidEmulation = rs;
				// Setup the emulation
				rs.create(sidPlayer.info().maxsids);
				rs.filter(sidFilter);
				rs.sampling(sampleRate);
			}

			sidTune.selectSong(songNumber);
			sidPlayer.load(sidTune);

			multiplier = MULTIPLIER_VALUE;
		}
		catch (Exception ex)
		{
			Log.error("[SIDMixer]", ex);
		}
	}
	public void setSampleRate(int newSampleRate)
	{
		int oldSampleRate = sampleRate;

		boolean wasPlaying = !isPaused();
		if (wasPlaying) pausePlayback();

		sampleRate = newSampleRate;
		if (wasPlaying)
		{
			setAudioFormat(new AudioFormat(sampleRate, 16, 2, true, false));
			openAudioDevice();
			if (!isInitialized())
			{
				sampleRate = oldSampleRate;
				setAudioFormat(new AudioFormat(sampleRate, 16, 2, true, false));
				openAudioDevice();
			}
			else
			{
				ISID2Types.sid2_config_t sidConfig = sidPlayer.config();
				sidConfig.frequency = sampleRate;
				SIDBuilder rs = sidConfig.sidEmulation;
				if (rs!=null && rs.bool()&& rs instanceof ReSIDBuilder) 
					((ReSIDBuilder)rs).sampling(sampleRate);
				sidPlayer.config(sidConfig);
			}
			pausePlayback();
		}
	}
	public void setSIDModel(int newSidModel)
	{
		boolean wasPlaying = !isPaused();
		if (wasPlaying) pausePlayback();

		sidModel = newSidModel;
		ISID2Types.sid2_config_t sidConfig = sidPlayer.config();
		sidConfig.sidModel = (sidModel==0)?ISID2Types.sid2_model_t.SID2_MODEL_CORRECT:((sidModel==1)?ISID2Types.sid2_model_t.SID2_MOS6581:ISID2Types.sid2_model_t.SID2_MOS8580);
		sidPlayer.config(sidConfig);

		ReSIDBuilder rs = new ReSIDBuilder("ReSID");
		if (rs.bool())
		{
			sidConfig.sidEmulation = rs;
			// Setup the emulation
			rs.create(sidPlayer.info().maxsids);
			rs.filter(sidFilter);
			rs.sampling(sampleRate);
		}
		sidPlayer.config(sidConfig);
		
		if (wasPlaying) pausePlayback();
	}
	public void setOptimization(int newOptimization)
	{
		boolean wasPlaying = !isPaused();
		if (wasPlaying) pausePlayback();
		
		optimization = newOptimization;
		ISID2Types.sid2_config_t sidConfig = sidPlayer.config();
		sidConfig.optimisation = (byte)optimization;
		sidPlayer.config(sidConfig);

		ReSIDBuilder rs = new ReSIDBuilder("ReSID");
		if (rs.bool())
		{
			sidConfig.sidEmulation = rs;
			// Setup the emulation
			rs.create(sidPlayer.info().maxsids);
			rs.filter(sidFilter);
			rs.sampling(sampleRate);
		}
		sidPlayer.config(sidConfig);

		if (wasPlaying) pausePlayback();
	}
	public void setUseSIDFilter(boolean useSIDFilter)
	{
		boolean wasPlaying = !isPaused();
		if (wasPlaying) pausePlayback();
		
		sidFilter = useSIDFilter;
		ISID2Types.sid2_config_t sidConfig = sidPlayer.config();
		SIDBuilder rs = sidConfig.sidEmulation;
		if (rs!=null && rs.bool() && rs instanceof ReSIDBuilder) 
			((ReSIDBuilder)rs).filter(sidFilter);
		sidPlayer.config(sidConfig);

		if (wasPlaying) pausePlayback();
	}
	public void setVirtualStereo(boolean newIsStereo)
	{
		boolean wasPlaying = !isPaused();
		if (wasPlaying) pausePlayback();
		
		isStereo = newIsStereo;
		ISID2Types.sid2_config_t sidConfig = sidPlayer.config();
		sidConfig.emulateStereo = isStereo;
		sidConfig.playback = (isStereo ? ISID2Types.sid2_playback_t.sid2_stereo : ISID2Types.sid2_playback_t.sid2_mono);
		sidPlayer.config(sidConfig);

		ReSIDBuilder rs = new ReSIDBuilder("ReSID");
		if (rs.bool())
		{
			sidConfig.sidEmulation = rs;
			// Setup the emulation
			rs.create(sidPlayer.info().maxsids);
			rs.filter(sidFilter);
			rs.sampling(sampleRate);
		}
		sidPlayer.config(sidConfig);

		if (wasPlaying) pausePlayback();
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
	 * @return
	 * @see de.quippy.javamod.mixer.Mixer#getLengthInMilliseconds()
	 */
	@Override
	public long getLengthInMilliseconds()
	{
		return (sidTune != null) ? sidTune.getInfo().songs * 1000 : 0;
	}
	/**
	 * @return
	 * @see de.quippy.javamod.mixer.Mixer#getMillisecondPosition()
	 */
	@Override
	public long getMillisecondPosition()
	{
		return songNumber * 1000L;
	}
	/**
	 * @return
	 * @see de.quippy.javamod.mixer.Mixer#getChannelCount()
	 */
	@Override
	public int getChannelCount()
	{
		if (sidPlayer!=null)
		{
			return sidPlayer.info().channels;
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
		return (getChannelCount()*16*sampleRate)/1000;
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
	 * @param milliseconds
	 * @see de.quippy.javamod.mixer.BasicMixer#seek(long)
	 * @since 13.02.2012
	 */
	protected void seek(long milliseconds)
	{
		if (sidTune != null)
		{
			pausePlayback();
			songNumber = (int)(milliseconds / 1000L) + 1;
			sidTune.selectSong(songNumber);
			sidPlayer.load(sidTune);
			parentSIDContainer.nameChanged();
			pausePlayback();
		}
	}
	/**
	 * @param milliseconds
	 * @see de.quippy.javamod.mixer.Mixer#setMillisecondPosition(long)
	 */
	@Override
	public void setMillisecondPosition(long milliseconds)
	{
		seek(milliseconds);
	}
	private byte[] getOutputBuffer(int length)
	{
		if (output == null || output.length < length) output = new byte[length];
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
		parentSIDContainer.nameChanged();
		setIsPlaying();

		try
		{
			setAudioFormat(new AudioFormat(this.sampleRate, 16, 2, true, false));
			openAudioDevice();
			if (!isInitialized()) return;

			boolean finished = false;

			final int bufferSize = sampleRate;
			final short[] shortBuffer = new short[bufferSize];
			do
			{
				sidPlayer.play(shortBuffer, bufferSize);

				// convert short buffer to byte array
				int byteBufferSize = (isStereo) ? bufferSize : bufferSize << 1;
				byte[] b = getOutputBuffer(byteBufferSize);
				int idx = byteBufferSize;
				int pos = bufferSize;
				while (pos > 0)
				{
					byte ll, rl, lh, rh;
					if (isStereo)
					{
						int sl = (short) ((shortBuffer[--pos] << 8) | (shortBuffer[--pos]));
						int sr = (short) ((shortBuffer[--pos] << 8) | (shortBuffer[--pos]));
						sl = (int) (sl * multiplier) >> MULTIPLIER_SHIFT;
						sr = (int) (sr * multiplier) >> MULTIPLIER_SHIFT;

						ll = (byte) (sl & 0xFF);
						lh = (byte) (sl >> 8);
						rl = (byte) (sr & 0xFF);
						rh = (byte) (sr >> 8);
					}
					else
					{
						int s = (short) ((shortBuffer[--pos] << 8) | (shortBuffer[--pos]));
						s = (int) (s * multiplier) >> MULTIPLIER_SHIFT;
						ll = rl = (byte) (s & 0xFF);
						lh = rh = (byte) (s >> 8);
					}
					b[--idx] = (byte) lh;
					b[--idx] = (byte) ll;
					b[--idx] = (byte) rh;
					b[--idx] = (byte) rl;
				}
				writeSampleDataToLine(b, 0, byteBufferSize);

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
			}
			while (!finished);
			if (finished) setHasFinished(); // Piece was played full
		}
		catch (Throwable ex)
		{
			throw new RuntimeException(ex);
		}
		finally
		{
			sidPlayer.stop();
			setIsStopped();
			closeAudioDevice();
		}
	}
}
