/*
 * @(#) MidiMixer.java
 *
 * Created on 28.12.2007 by Daniel Becker
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
package de.quippy.javamod.multimedia.midi;

import java.io.File;

import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Transmitter;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.CompoundControl;
import javax.sound.sampled.Control;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;

import de.quippy.javamod.mixer.BasicMixer;
import de.quippy.javamod.system.Log;

/**
 * @author Daniel Becker
 * @since 28.12.2007
 */
public class MidiMixer extends BasicMixer
{
	private boolean capture;
	private Mixer.Info mixerInfo;
	
	private long seekPosition;
	
	private int bufferSize;
	private byte [] output;

	private int sampleSizeInBits;
	private int sampleSizeInBytes;
	private int channels;
	private int sampleRate;
	private int frameCalc;
	private TargetDataLine targetDataLine;

	private Sequence sequence;
	private Sequencer sequencer;
	private MidiDevice midiOutput;
	private MidiDevice.Info outputDeviceInfo;
	private File soundBankFile;

	/**
	 * Constructor for MidiMixer
	 */
	public MidiMixer(Sequence sequence, MidiDevice.Info outputDeviceInfo, File soundBankFile, boolean capture, Mixer.Info mixerInfo)
	{
		super();
		this.seekPosition = 0;
		this.sequencer = null;
		this.midiOutput = null;
		this.outputDeviceInfo = outputDeviceInfo;
		this.soundBankFile = soundBankFile;
		this.sequence = sequence;
		this.capture = capture;
		this.mixerInfo = mixerInfo;
		this.targetDataLine = null;

		if (capture)
		{
			this.setAudioFormat(new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100.0F, 16, 2, 4, 44100.0F, false));
			this.channels = getAudioFormat().getChannels();
			this.sampleSizeInBits = getAudioFormat().getSampleSizeInBits();
			this.sampleSizeInBytes = this.sampleSizeInBits>>3;
			this.sampleRate = (int)getAudioFormat().getSampleRate();
			this.frameCalc = channels * sampleSizeInBytes;
			
			this.bufferSize = 250 * channels * sampleSizeInBytes * sampleRate / 1000; // 250ms buffer
			
			// Now for the bits (linebuffer):
			bufferSize *= sampleSizeInBytes;
			output = new byte[bufferSize];
		}
	}
	private void initialize()
	{
		if (capture)
		{
			DataLine.Info info = new DataLine.Info(TargetDataLine.class, getAudioFormat());
			try
			{
				if (mixerInfo!=null)
				{
					Mixer mixer = AudioSystem.getMixer(mixerInfo);
					targetDataLine = (TargetDataLine) mixer.getLine(info);
				}
				else
					targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
				
				targetDataLine.open();
				// Now for some rediculous programming - the interfaces do not provide anything good...
				Control controles[] = targetDataLine.getControls();
				for (int i=0; i<controles.length; i++)
				{
					if (controles[i] instanceof CompoundControl) // Found it...
					{
						Control[] children = ((CompoundControl) controles[i]).getMemberControls();
						for (int j=0; j<children.length; j++)
						{
							if (children[i] instanceof BooleanControl)
							{
								if (children[i].getType().getClass().getName().endsWith("BCT")) // this is so far the "Select"
									((BooleanControl)children[i]).setValue(true);
							}
						}
					}
				}
				targetDataLine.close();
			}
			catch (LineUnavailableException ex)
			{
				targetDataLine = null;
			}
		}
	}
	/**
	 * 
	 * @see de.quippy.javamod.mixer.Mixer#isSeekSupported()
	 */
	@Override
	public boolean isSeekSupported()
	{
		return true;
	}
	/**
	 * 
	 * @see de.quippy.javamod.mixer.Mixer#getMillisecondPosition()
	 */
	@Override
	public long getMillisecondPosition()
	{
		if (sequencer!=null)
		{
			return sequencer.getMicrosecondPosition()/1000L;
		}
		return 0;
	}
	/**
	 * @param milliseconds
	 * @see de.quippy.javamod.mixer.BasicMixer#seek(long)
	 * @since 13.02.2012
	 */
	protected void seek(long milliseconds)
	{
		if (sequencer!=null)
		{
			sequencer.setMicrosecondPosition(milliseconds*1000L);
		}
		else
			seekPosition = milliseconds;
	}
	/**
	 * 
	 * @see de.quippy.javamod.mixer.Mixer#setMillisecondPosition(long)
	 */
	@Override
	public void setMillisecondPosition(long milliseconds)
	{
		seek(milliseconds);
	}
	/**
	 * 
	 * @see de.quippy.javamod.mixer.Mixer#getLengthInMilliseconds()
	 */
	@Override
	public long getLengthInMilliseconds()
	{
		return (sequence!=null)?sequence.getMicrosecondLength()/1000L:0L;
	}
	/**
	 * @return
	 * @see de.quippy.javamod.mixer.Mixer#getChannelCount()
	 */
	@Override
	public int getChannelCount()
	{
		if (sequencer!=null)
		{
			Sequence sequence = sequencer.getSequence();
			if (sequence!=null)
			{
				return sequence.getTracks().length;
			}
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
		return (44100 * 16 * 2) / 1000;
	}
	/**
	 * @return
	 * @see de.quippy.javamod.mixer.Mixer#getCurrentSampleRate()
	 */
	@Override
	public int getCurrentSampleRate()
	{
		return 44100; // sampleRate is only for capture
	}
	public void setNewOutputDevice(MidiDevice.Info newDeviceInfo)
	{
		boolean isPlaying = isPlaying();
		if (isPlaying) stopLine(false);
		try
		{
			if (midiOutput!=null)
			{
				midiOutput.close();
				midiOutput = null;
			}
			if (newDeviceInfo!=null)
			{
				midiOutput = MidiSystem.getMidiDevice(newDeviceInfo);
			}
			else
			{
				midiOutput = MidiSystem.getMidiDevice(outputDeviceInfo);
			}
			if (!midiOutput.isOpen()) midiOutput.open();
			if (midiOutput instanceof Synthesizer && (soundBankFile!=null))
			{
				try
				{
					Soundbank bank = MidiSystem.getSoundbank(soundBankFile);
					((Synthesizer)midiOutput).loadAllInstruments(bank);
				}
				catch (Exception ex)
				{
					Log.error("Error occured when opening soundfont bank", ex);
				}
			}
			Receiver    synthReceiver  = midiOutput.getReceiver();
			Transmitter seqTransmitter = sequencer.getTransmitter();
			seqTransmitter.setReceiver(synthReceiver);
			if (isPlaying) startLine(false);
		}
		catch (MidiUnavailableException ex)
		{
			closeAudioDevice();
			Log.error("Error occured when opening midi device", ex);
		}
	}
	/**
	 * 
	 * @see de.quippy.javamod.mixer.Mixer#openAudioDevice()
	 */
	@Override
	protected void openAudioDevice()
	{
		if (capture && targetDataLine!=null)
		{
			try
			{
				setKeepSilent(true);
				super.openAudioDevice(); // Does closeAudioDevice himself
				targetDataLine.open();
			}
			catch (LineUnavailableException ex)
			{
				closeAudioDevice();
				Log.error("[MidiMixer]: TargetDataLine", ex);
			}
		}
		else
			closeAudioDevice();
		
		try
		{
			sequencer = MidiSystem.getSequencer(false); // get an unconnected sequencer
			sequencer.addMetaEventListener(new MetaEventListener()
			{
				public void meta(MetaMessage event)
				{
					if (event.getType() == 47) // Song finished
					{
						setHasFinished();
						stopPlayback();
					}
				}
			});

			sequencer.setSequence(sequence);
			sequencer.open();

			if (! (sequencer instanceof Synthesizer))
			{
				setNewOutputDevice(outputDeviceInfo);
			}
		}
		catch (Exception ex)
		{
			Log.error("[MidiMixer]", ex);
		}
	}
	/**
	 * 
	 * @see de.quippy.javamod.mixer.Mixer#closeAudioDevice()
	 */
	@Override
	protected void closeAudioDevice()
	{
		stopLine(false);
		if (midiOutput!=null && midiOutput.isOpen()) midiOutput.close();
		if (sequencer!=null && sequencer.isOpen()) sequencer.close();
		midiOutput = null;
		sequencer = null;
		if (capture)
		{
			if (targetDataLine!=null) targetDataLine.close();
			super.closeAudioDevice();
		}
		super.fullyCloseAudioDevice();
	}
	/**
	 * 
	 * @see de.quippy.javamod.mixer.Mixer#startLine()
	 */
	@Override
	protected void startLine(final boolean flushOrDrain)
	{
		if (targetDataLine!=null) targetDataLine.start();
		if (sequencer!=null) sequencer.start();
	}
	/**
	 * 
	 * @see de.quippy.javamod.mixer.Mixer#stopLine()
	 */
	@Override
	protected void stopLine(final boolean flushOrDrain)
	{
		if (sequencer!=null) sequencer.stop();
		if (targetDataLine!=null)
		{
			// if (targetDataLine.isRunning()) targetDataLine.drain(); // Not good, if data is not fetched!
			targetDataLine.stop();
		}
	}
	/**
	 * @return
	 * @see de.quippy.javamod.mixer.Mixer#isInitialized()
	 */
	@Override
	protected boolean isInitialized()
	{
		if (capture && targetDataLine==null) return false;
		return sequencer!=null;
	}
	/**
	 * 
	 * @see de.quippy.javamod.mixer.Mixer#startPlayback()
	 */
	@Override
	public void startPlayback()
	{
		initialize();

		if (seekPosition>0) seek(seekPosition);

		try
		{
			openAudioDevice();
			if (!isInitialized()) return;
			startLine(false);
			setIsPlaying();
			long framePosition = 0;

			do
			{
				if (targetDataLine!=null)
				{
					int amount = targetDataLine.available();
					if (amount>bufferSize) amount = bufferSize;
					int byteCount = targetDataLine.read(output, 0, amount);
					if (byteCount>0)
					{
						writeSampleDataToLine(output, 0, byteCount);
						setInternatFramePosition(framePosition);
						framePosition += (byteCount / frameCalc);
					}
				}
				try { Thread.sleep(10L); } catch (InterruptedException ex) { /*noop*/ }

				if (stopPositionIsReached()) setIsStopping();
				
				if (isStopping())
				{
					setIsStopped();
				}
				if (isPausing())
				{
					setIsPaused();
					while (isPaused())
					{
						try { Thread.sleep(10L); } catch (InterruptedException ex) { /*noop*/ }
					}
				}
			}
			while (isPlaying()); 
		}
		catch (Throwable ex)
		{
			throw new RuntimeException(ex);
		}
		finally
		{
			setIsStopped();
			closeAudioDevice();
		}
	}
}
