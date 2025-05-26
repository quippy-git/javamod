/*
 * 11/19/04	 1.0 moved to LGPL.
 *
 * 12/12/99  Initial Version based on FileObuffer.	mdm@techie.com.
 *
 * FileObuffer:
 * 15/02/99  Java Conversion by E.B ,javalayer@javazoom.net
 *
 *-----------------------------------------------------------------------
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published
 *   by the Free Software Foundation; either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this program; if not, write to the Free Software
 *   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *----------------------------------------------------------------------
 */

package de.quippy.mp3.decoder;

/**
 * The <code>SampleBuffer</code> class implements an output buffer
 * that provides storage for a fixed size block of samples.
 */
public class SampleBuffer extends Obuffer
{
  private final short[] 		buffer;
  private final int[] 		bufferp;
  private final int 			channels;
  private final int			frequency;

  /**
   * Constructor
   */
  public SampleBuffer(final int sample_frequency, final int number_of_channels)
  {
  	buffer = new short[OBUFFERSIZE];
	bufferp = new int[MAXCHANNELS];
	channels = number_of_channels;
	frequency = sample_frequency;

	for (int i = 0; i < number_of_channels; ++i)
		bufferp[i] = (short)i;

  }

  public int getChannelCount()
  {
	return this.channels;
  }

  public int getSampleFrequency()
  {
	  return this.frequency;
  }

  public short[] getBuffer()
  {
	return this.buffer;
  }

  public int getBufferLength()
  {
	  return bufferp[0];
  }

  /**
   * Takes a 16 Bit PCM sample.
   */
  @Override
public void append(final int channel, final short value)
  {
	buffer[bufferp[channel]] = value;
	bufferp[channel] += channels;
  }

	@Override
	public void appendSamples(final int channel, final float[] f)
	{
	    int pos = bufferp[channel];

	    for (int i=0; i<32;)
	    {
		  	final float fs = f[i++];
			buffer[pos] = (short)(fs>32767.0f ? 32767.0f : (fs < -32768.0f ? -32768.0f : fs));
			pos += channels;
	    }

		bufferp[channel] = pos;
	}


  /**
   * Write the samples to the file (Random Access).
   */
  @Override
public void write_buffer(final int val)
  {
	//for (int i = 0; i < channels; ++i)
	//	bufferp[i] = (short)i;
  }

  @Override
public void close()
  {}

  /**
   *
   */
  @Override
public void clear_buffer()
  {
	for (int i = 0; i < channels; ++i)
		bufferp[i] = (short)i;
  }

  /**
   *
   */
  @Override
public void set_stop_flag()
  {}
}
