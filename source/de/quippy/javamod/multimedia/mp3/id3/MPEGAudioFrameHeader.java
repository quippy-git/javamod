/*
 * @(#) MPEGAudioFrameHeader.java
 *
 * Created on 24.12.2008 by Daniel Becker
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
package de.quippy.javamod.multimedia.mp3.id3;


import java.io.FileNotFoundException;
import java.io.IOException;

import de.quippy.javamod.io.RandomAccessInputStream;
import de.quippy.javamod.multimedia.mp3.id3.exceptions.NoMPEGFramesException;

/**
 * Description: 
 *  This class reads through the file specified and tries to find an mpeg
 *  frame.  It then reads data from the header of the first frame encountered.
 *  Parts of this class will not work correctly on VBR files.
 *
 * @author:  Jonathan Hilliker modified by Daniel becker
 */
public class MPEGAudioFrameHeader
{
	private static final int HEADER_SIZE = 4;
	private static final int[][] bitrateTable =
	{
			{
					-1, -1, -1, -1, -1
			},
			{
					32, 32, 32, 32, 8
			},
			{
					64, 48, 40, 48, 16
			},
			{
					96, 56, 48, 56, 24
			},
			{
					128, 64, 56, 64, 32
			},
			{
					160, 80, 64, 80, 40
			},
			{
					192, 96, 80, 96, 48
			},
			{
					224, 112, 96, 112, 56
			},
			{
					256, 128, 112, 128, 64
			},
			{
					288, 160, 128, 144, 80
			},
			{
					320, 192, 160, 160, 96
			},
			{
					352, 224, 192, 176, 112
			},
			{
					384, 256, 224, 192, 128
			},
			{
					416, 320, 256, 224, 144
			},
			{
					448, 384, 320, 256, 160
			},
			{
					-1, -1, -1, -1, -1
			}
	};
	private static final int[][] sampleTable =
	{
			{
					44100, 22050, 11025
			},
			{
					48000, 24000, 12000
			},
			{
					32000, 16000, 8000
			},
			{
					-1, -1, -1
			}
	};
	private static final String[] versionLabels =
	{
			"MPEG Version 2.5", null, "MPEG Version 2.0", "MPEG Version 1.0"
	};
	private static final String[] layerLabels =
	{
			null, "Layer III", "Layer II", "Layer I"
	};
	private static final String[] channelLabels =
	{
			"Stereo", "Joint Stereo (STEREO)", "Dual Channel (STEREO)", "Single Channel (MONO)"
	};
	private static final String[] emphasisLabels =
	{
			"none", "50/15 ms", null, "CCIT J.17"
	};
	private static final int MPEG_V_25 = 0;
	private static final int MPEG_V_2 = 2;
	private static final int MPEG_V_1 = 3;
	
	private static final int MPEG_L_3 = 1;
	private static final int MPEG_L_2 = 2;
	private static final int MPEG_L_1 = 3;

	private int version = -1;
	private int layer = -1;
	private int bitRate = -1;
	private int sampleRate = -1;
	private int channelMode = -1;
	private boolean copyrighted = false;
	private boolean crced = false;
	private boolean original = false;
	private int emphasis = -1;

	/**
	 * Create an MPEGAudioFrameHeader from the file specified.  Upon creation
	 * information will be read in from the first frame header the object 
	 * encounters in the file.
	 *
	 * @param mp3 the file to read from
	 * @exception NoMPEGFramesException if the file is not a valid mpeg
	 * @exception FileNotFoundException if an error occurs
	 * @exception IOException if an error occurs
	 */
	public MPEGAudioFrameHeader(RandomAccessInputStream raf) throws NoMPEGFramesException, IOException
	{
		this(raf, 0);
	}

	/**
	 * Create an MPEGAudioFrameHeader from the file specified.  Upon creation
	 * information will be read in from the first frame header the object 
	 * encounters in the file.  The offset tells the object where to start
	 * searching for an MPEG frame.  If you know the size of an id3v2 tag 
	 * attached to the file and pass it to this ctor, it will take less time
	 * to find the frame.
	 *
	 * @param mp3 the file to read from
	 * @param offset the offset to start searching from
	 * @exception NoMPEGFramesException if the file is not a valid mpeg
	 * @exception FileNotFoundException if an error occurs
	 * @exception IOException if an error occurs
	 */
	public MPEGAudioFrameHeader(RandomAccessInputStream raf, int offset) throws NoMPEGFramesException, IOException
	{
		long location = findFrame(raf, offset);

		if (location != -1)
		{
			readHeader(raf, location);
		}
		else
		{
			throw new NoMPEGFramesException();
		}
	}

	/**
	 * Searches through the file and finds the first occurrence of an mpeg 
	 * frame.  Returns the location of the header of the frame.
	 *
	 * @param offset the offset to start searching from
	 * @return the location of the header of the frame
	 * @exception FileNotFoundException if an error occurs
	 * @exception IOException if an error occurs
	 */
	private long findFrame(RandomAccessInputStream raf, int offset) throws IOException
	{
		long loc = -1;
		raf.seek(offset);

		while (loc == -1)
		{
			byte test = raf.readByte();

			if ((test & 0xFF) == 0xFF)
			{
				test = raf.readByte();

				if ((test & 0xE0) == 0xE0)
				{
					return raf.getFilePointer() - 2;
				}
			}
		}

		return -1;
	}

	/**
	 * Read in all the information found in the mpeg header.
	 *
	 * @param location the location of the header (found by findFrame)
	 * @exception FileNotFoundException if an error occurs
	 * @exception IOException if an error occurs
	 */
	private void readHeader(RandomAccessInputStream raf, long location) throws IOException
	{
		byte[] head = new byte[HEADER_SIZE];
		raf.seek(location);

		if (raf.read(head) != HEADER_SIZE)
		{
			throw new IOException("Error reading MPEG frame header.");
		}

		version = (head[1]&0x18)>>3;
		layer = (head[1]&0x06)>>1;
		crced = (head[1]&0x01)==0;
		bitRate = findBitRate((head[2]&0xF0)>>4, version, layer);
		sampleRate = findSampleRate((head[2]&0x0C)>>2, version);
		channelMode = (head[3]&0xC0)>>6;
		copyrighted = (head[3]&0x08)!=0;
		original = (head[3]&0x04)!=0;
		emphasis = head[3]&0x03;
	}

	/**
	 * Based on the bitrate index found in the header, try to find and set the 
	 * bitrate from the table.
	 *
	 * @param bitrateIndex the bitrate index read from the header
	 */
	private int findBitRate(int bitrateIndex, int version, int layer)
	{
		int ind = -1;

		if (version == MPEG_V_1)
		{
			if (layer == MPEG_L_1)
			{
				ind = 0;
			}
			else if (layer == MPEG_L_2)
			{
				ind = 1;
			}
			else if (layer == MPEG_L_3)
			{
				ind = 2;
			}
		}
		else if ((version == MPEG_V_2) || (version == MPEG_V_25))
		{
			if (layer == MPEG_L_1)
			{
				ind = 3;
			}
			else if ((layer == MPEG_L_2) || (layer == MPEG_L_3))
			{
				ind = 4;
			}
		}

		if ((ind != -1) && (bitrateIndex >= 0) && (bitrateIndex <= 15))
		{
			return bitrateTable[bitrateIndex][ind];
		}
		return -1;
	}

	/**
	 * Based on the sample rate index found in the header, attempt to lookup
	 * and set the sample rate from the table.
	 *
	 * @param sampleIndex the sample rate index read from the header
	 */
	private int findSampleRate(int sampleIndex, int version)
	{
		int ind = -1;

		switch (version)
		{
			case MPEG_V_1:
				ind = 0;
				break;
			case MPEG_V_2:
				ind = 1;
				break;
			case MPEG_V_25:
				ind = 2;
		}

		if ((ind != -1) && (sampleIndex >= 0) && (sampleIndex <= 3))
		{
			return sampleTable[sampleIndex][ind];
		}
		return -1;
	}

	/**
	 * Return a string representation of this object.  Includes all information
	 * read in.
	 *
	 * @return a string representation of this object
	 */
	public String toString()
	{
		return getVersion() + " " + getLayer() + "\nBitRate:\t\t\t" + getBitRate() + "kbps\nSampleRate:\t\t\t" + getSampleRate() + "Hz\nChannelMode:\t\t\t" + getChannelMode() + "\nCopyrighted:\t\t\t" + isCopyrighted() + "\nOriginal:\t\t\t" + isOriginal()
				+ "\nCRC:\t\t\t\t" + isProtected() + "\nEmphasis:\t\t\t" + getEmphasis();
	}

	/**
	 * Return the version of the mpeg in string form.  Ex: MPEG Version 1.0
	 *
	 * @return the version of the mpeg
	 */
	public String getVersion()
	{
		String str = null;

		if ((version >= 0) && (version < versionLabels.length))
		{
			str = versionLabels[version];
		}

		return str;
	}

	/**
	 * Return the layer description of the mpeg in string form.
	 * Ex: Layer III
	 *
	 * @return the layer description of the mpeg
	 */
	public String getLayer()
	{
		String str = null;

		if ((layer >= 0) && (layer < layerLabels.length))
		{
			str = layerLabels[layer];
		}

		return str;
	}

	/**
	 * Return the channel mode of the mpeg in string form.
	 * Ex: Joint Stereo (STEREO)
	 *
	 * @return the channel mode of the mpeg
	 */
	public String getChannelMode()
	{
		String str = null;

		if ((channelMode >= 0) && (channelMode < channelLabels.length))
		{
			str = channelLabels[channelMode];
		}

		return str;
	}

	/**
	 * Returns the bitrate of the mpeg in kbps
	 *
	 * @return the bitrate of the mpeg in kbps
	 */
	public int getBitRate()
	{
		return bitRate;
	}

	/**
	 * Returns the sample rate of the mpeg in Hz
	 *
	 * @return the sample rate of the mpeg in Hz
	 */
	public int getSampleRate()
	{
		return sampleRate;
	}

	/**
	 * Returns true if the audio is copyrighted
	 *
	 * @return true if the audio is copyrighted
	 */
	public boolean isCopyrighted()
	{
		return copyrighted;
	}

	/**
	 * Returns true if this mpeg is protected by CRC
	 *
	 * @return true if this mpeg is protected by CRC
	 */
	public boolean isProtected()
	{
		return crced;
	}

	/**
	 * Returns true if this is the original media
	 *
	 * @return true if this is the original media
	 */
	public boolean isOriginal()
	{
		return original;
	}

	/**
	 * Returns the emphasis.  I don't know what this means, it just does it...
	 *
	 * @return the emphasis
	 */
	public String getEmphasis()
	{
		String str = null;

		if ((emphasis >= 0) && (emphasis < emphasisLabels.length))
		{
			str = emphasisLabels[emphasis];
		}

		return str;
	}

	/**
	 * Returns true if the file passed to the constructor is an mp3 (MPEG 
	 * layer III).
	 *
	 * @return true if the file is an mp3
	 */
	public boolean isMP3()
	{
		return (layer == MPEG_L_3);
	}

}
