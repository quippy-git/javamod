/*
 * @(#) SoundOutputStream.java
 *
 * Created on 02.10.2010 by Daniel Becker
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

import de.quippy.javamod.io.wav.WaveFile;
import de.quippy.javamod.mixer.dsp.AudioProcessor;


/**
 * @author Daniel Becker
 * This Interface describes a soundoutput stream for playback
 * @since 02.10.2010
 */
public interface SoundOutputStream
{
	public void open();
	public void close();
	public void closeAllDevices();
	public boolean isInitialized();
	public void startLine(final boolean flushOrDrain);
	public void stopLine(final boolean flushOrDrain);
	public void flushLine();
	public void drainLine();
	public void writeSampleData(final byte[] samples, final int start, final int length);
	public void setInternalFramePosition(final long newPosition);
	public long getFramePosition();
	public void setVolume(final float gain);
	public void setBalance(final float balance);
	public void setAudioProcessor(final AudioProcessor audioProcessor);
	public void setExportFile(final File exportFile);
	public void setWaveExportFile(final WaveFile waveExportFile);
	public void setPlayDuringExport(final boolean playDuringExport);
	public void setKeepSilent(final boolean keepSilent);
	public void changeAudioFormatTo(final AudioFormat newFormat);
	public AudioFormat getAudioFormat();
	public boolean matches(final SoundOutputStream otherStream);
}
