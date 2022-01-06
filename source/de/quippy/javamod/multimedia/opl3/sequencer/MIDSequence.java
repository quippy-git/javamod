/*
 * @(#) MIDSequence.java
 *
 * Created on 03.08.2020 by Daniel Becker
 * 
 *-----------------------------------------------------------------------
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
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
 *
 * As a proof of concept this was taken from mid.cpp of the adplug project
 * and ported to java.
 * Corrections and additions by to work with OPL3.java 
 * 2008 Robson Cozendey
 * 2020 Daniel Becker
 * Remark: whoever wrote the coding in mid.cpp: do yourself a favor and
 * get some education in code formatting and naming of variables.
 * Using global vars is not a good idea! SysEx, variable "i" value is
 * unpredictable!
 */
package de.quippy.javamod.multimedia.opl3.sequencer;

import java.io.IOException;
import java.net.URL;

import de.quippy.javamod.io.RandomAccessInputStreamImpl;
import de.quippy.javamod.multimedia.MultimediaContainerManager;
import de.quippy.javamod.multimedia.opl3.emu.EmuOPL;
import de.quippy.javamod.multimedia.opl3.emu.EmuOPL.oplType;
import de.quippy.javamod.system.Helpers;

/**
 * @author Daniel Becker
 * @since 03.08.2020
 */
public class MIDSequence extends OPL3Sequence
{
	private class midi_track
	{
		long tend  = 0;
		long spos = 0;
		long pos = 0;
		long iwait = 0;
		boolean on = false;
		int pv = 0;

		midi_track()
		{
			super();
		}
	}

	private class midi_channel
	{
		int inum = 0;
		int ins[] = null;
		int vol = 0;
		int nshift = 0;
		boolean on = false;

		midi_channel()
		{
			super();
			ins = new int[11];
		}
	}

	private URL url = null;
	private int type = 0;
	private byte [] data = null;
	
	private String author = null;
	private String title = null;
	private String remarks = null;
	private long flen = 0;
	private long pos = 0;
	private long sierra_pos = 0;
	private int subsongs = 0;
	private int adlib_data[] = null;
	private int adlib_style = 0;
	private int adlib_mode = 0;
	private int myinsbank[][] = null;
	private int smyinsbank[][] = null;
	private midi_channel ch[] = null;
	private int chp[][] = null;
	private int deltas = 0;
	private long msqtr = 0; // the only usage of msqtr is documented out
	private midi_track track[] = null;
	private double fwait = 0;
	private long iwait = 0;
	private boolean firstRound = false;
	private int tins = 0;
	private int stins = 0;

	private static final int LUCAS_STYLE	= 1;
	private static final int CMF_STYLE		= 2;
	private static final int MIDI_STYLE		= 4;
	private static final int SIERRA_STYLE	= 8;

	// AdLib melodic and rhythm mode defines
	private static final int ADLIB_MELODIC	= 0;
	private static final int ADLIB_RYTHM	= 1;

	// File types
	private static final int FILE_LUCAS		= 1;
	private static final int FILE_MIDI		= 2;
	private static final int FILE_CMF		= 3;
	private static final int FILE_SIERRA	= 4;
	private static final int FILE_ADVSIERRA	= 5;
	private static final int FILE_OLDLUCAS	= 6;
	
	// AdLib standard operator table
	private static final int adlib_opadd[] = {0x00, 0x01, 0x02, 0x08, 0x09, 0x0A, 0x10, 0x11, 0x12};
	
	// map CMF drum channels 12 - 15 to corresponding AdLib drum operators
	// bass drum (channel 11) not mapped, cause it's handled like a normal instrument
	private static final int map_chan[] = { 0x14, 0x12, 0x15, 0x11 };

	// Standard AdLib frequency table
	private static final int fnums[] = { 0x16b,0x181,0x198,0x1b0,0x1ca,0x1e5,0x202,0x220,0x241,0x263,0x287,0x2ae };

	// Map CMF drum channels 11 - 15 to corresponding AdLib drum channels
	private static final int percussion_map[] = { 6, 7, 8, 8, 7 };

   // This set of GM instrument patches was provided by Jorrit Rouwe...
	private static short midi_fm_instruments[][] =
	{
	   { 0x21, 0x21, 0x8f, 0x0c, 0xf2, 0xf2, 0x45, 0x76, 0x00, 0x00, 0x08, 0, 0, 0 }, /* Acoustic Grand */
	   { 0x31, 0x21, 0x4b, 0x09, 0xf2, 0xf2, 0x54, 0x56, 0x00, 0x00, 0x08, 0, 0, 0 }, /* Bright Acoustic */
	   { 0x31, 0x21, 0x49, 0x09, 0xf2, 0xf2, 0x55, 0x76, 0x00, 0x00, 0x08, 0, 0, 0 }, /* Electric Grand */
	   { 0xb1, 0x61, 0x0e, 0x09, 0xf2, 0xf3, 0x3b, 0x0b, 0x00, 0x00, 0x06, 0, 0, 0 }, /* Honky-Tonk */
	   { 0x01, 0x21, 0x57, 0x09, 0xf1, 0xf1, 0x38, 0x28, 0x00, 0x00, 0x00, 0, 0, 0 }, /* Electric Piano 1 */
	   { 0x01, 0x21, 0x93, 0x09, 0xf1, 0xf1, 0x38, 0x28, 0x00, 0x00, 0x00, 0, 0, 0 }, /* Electric Piano 2 */
	   { 0x21, 0x36, 0x80, 0x17, 0xa2, 0xf1, 0x01, 0xd5, 0x00, 0x00, 0x08, 0, 0, 0 }, /* Harpsichord */
	   { 0x01, 0x01, 0x92, 0x09, 0xc2, 0xc2, 0xa8, 0x58, 0x00, 0x00, 0x0a, 0, 0, 0 }, /* Clav */
	   { 0x0c, 0x81, 0x5c, 0x09, 0xf6, 0xf3, 0x54, 0xb5, 0x00, 0x00, 0x00, 0, 0, 0 }, /* Celesta */
	   { 0x07, 0x11, 0x97, 0x89, 0xf6, 0xf5, 0x32, 0x11, 0x00, 0x00, 0x02, 0, 0, 0 }, /* Glockenspiel */
	   { 0x17, 0x01, 0x21, 0x09, 0x56, 0xf6, 0x04, 0x04, 0x00, 0x00, 0x02, 0, 0, 0 }, /* Music Box */
	   { 0x18, 0x81, 0x62, 0x09, 0xf3, 0xf2, 0xe6, 0xf6, 0x00, 0x00, 0x00, 0, 0, 0 }, /* Vibraphone */
	   { 0x18, 0x21, 0x23, 0x09, 0xf7, 0xe5, 0x55, 0xd8, 0x00, 0x00, 0x00, 0, 0, 0 }, /* Marimba */
	   { 0x15, 0x01, 0x91, 0x09, 0xf6, 0xf6, 0xa6, 0xe6, 0x00, 0x00, 0x04, 0, 0, 0 }, /* Xylophone */
	   { 0x45, 0x81, 0x59, 0x89, 0xd3, 0xa3, 0x82, 0xe3, 0x00, 0x00, 0x0c, 0, 0, 0 }, /* Tubular Bells */
	   { 0x03, 0x81, 0x49, 0x89, 0x74, 0xb3, 0x55, 0x05, 0x01, 0x00, 0x04, 0, 0, 0 }, /* Dulcimer */
	   { 0x71, 0x31, 0x92, 0x09, 0xf6, 0xf1, 0x14, 0x07, 0x00, 0x00, 0x02, 0, 0, 0 }, /* Drawbar Organ */
	   { 0x72, 0x30, 0x14, 0x09, 0xc7, 0xc7, 0x58, 0x08, 0x00, 0x00, 0x02, 0, 0, 0 }, /* Percussive Organ */
	   { 0x70, 0xb1, 0x44, 0x09, 0xaa, 0x8a, 0x18, 0x08, 0x00, 0x00, 0x04, 0, 0, 0 }, /* Rock Organ */
	   { 0x23, 0xb1, 0x93, 0x09, 0x97, 0x55, 0x23, 0x14, 0x01, 0x00, 0x04, 0, 0, 0 }, /* Church Organ */
	   { 0x61, 0xb1, 0x13, 0x89, 0x97, 0x55, 0x04, 0x04, 0x01, 0x00, 0x00, 0, 0, 0 }, /* Reed Organ */
	   { 0x24, 0xb1, 0x48, 0x09, 0x98, 0x46, 0x2a, 0x1a, 0x01, 0x00, 0x0c, 0, 0, 0 }, /* Accoridan */
	   { 0x61, 0x21, 0x13, 0x09, 0x91, 0x61, 0x06, 0x07, 0x01, 0x00, 0x0a, 0, 0, 0 }, /* Harmonica */
	   { 0x21, 0xa1, 0x13, 0x92, 0x71, 0x61, 0x06, 0x07, 0x00, 0x00, 0x06, 0, 0, 0 }, /* Tango Accordian */
	   { 0x02, 0x41, 0x9c, 0x89, 0xf3, 0xf3, 0x94, 0xc8, 0x01, 0x00, 0x0c, 0, 0, 0 }, /* Acoustic Guitar(nylon) */
	   { 0x03, 0x11, 0x54, 0x09, 0xf3, 0xf1, 0x9a, 0xe7, 0x01, 0x00, 0x0c, 0, 0, 0 }, /* Acoustic Guitar(steel) */
	   { 0x23, 0x21, 0x5f, 0x09, 0xf1, 0xf2, 0x3a, 0xf8, 0x00, 0x00, 0x00, 0, 0, 0 }, /* Electric Guitar(jazz) */
	   { 0x03, 0x21, 0x87, 0x89, 0xf6, 0xf3, 0x22, 0xf8, 0x01, 0x00, 0x06, 0, 0, 0 }, /* Electric Guitar(clean) */
	   { 0x03, 0x21, 0x47, 0x09, 0xf9, 0xf6, 0x54, 0x3a, 0x00, 0x00, 0x00, 0, 0, 0 }, /* Electric Guitar(muted) */
	   { 0x23, 0x21, 0x4a, 0x0e, 0x91, 0x84, 0x41, 0x19, 0x01, 0x00, 0x08, 0, 0, 0 }, /* Overdriven Guitar */
	   { 0x23, 0x21, 0x4a, 0x09, 0x95, 0x94, 0x19, 0x19, 0x01, 0x00, 0x08, 0, 0, 0 }, /* Distortion Guitar */
	   { 0x09, 0x84, 0xa1, 0x89, 0x20, 0xd1, 0x4f, 0xf8, 0x00, 0x00, 0x08, 0, 0, 0 }, /* Guitar Harmonics */
	   { 0x21, 0xa2, 0x1e, 0x09, 0x94, 0xc3, 0x06, 0xa6, 0x00, 0x00, 0x02, 0, 0, 0 }, /* Acoustic Bass */
	   { 0x31, 0x31, 0x12, 0x09, 0xf1, 0xf1, 0x28, 0x18, 0x00, 0x00, 0x0a, 0, 0, 0 }, /* Electric Bass(finger) */
	   { 0x31, 0x31, 0x8d, 0x09, 0xf1, 0xf1, 0xe8, 0x78, 0x00, 0x00, 0x0a, 0, 0, 0 }, /* Electric Bass(pick) */
	   { 0x31, 0x32, 0x5b, 0x09, 0x51, 0x71, 0x28, 0x48, 0x00, 0x00, 0x0c, 0, 0, 0 }, /* Fretless Bass */
	   { 0x01, 0x21, 0x8b, 0x49, 0xa1, 0xf2, 0x9a, 0xdf, 0x00, 0x00, 0x08, 0, 0, 0 }, /* Slap Bass 1 */
	   { 0x21, 0x21, 0x8b, 0x11, 0xa2, 0xa1, 0x16, 0xdf, 0x00, 0x00, 0x08, 0, 0, 0 }, /* Slap Bass 2 */
	   { 0x31, 0x31, 0x8b, 0x09, 0xf4, 0xf1, 0xe8, 0x78, 0x00, 0x00, 0x0a, 0, 0, 0 }, /* Synth Bass 1 */
	   { 0x31, 0x31, 0x12, 0x09, 0xf1, 0xf1, 0x28, 0x18, 0x00, 0x00, 0x0a, 0, 0, 0 }, /* Synth Bass 2 */
	   { 0x31, 0x21, 0x15, 0x09, 0xdd, 0x56, 0x13, 0x26, 0x01, 0x00, 0x08, 0, 0, 0 }, /* Violin */
	   { 0x31, 0x21, 0x16, 0x09, 0xdd, 0x66, 0x13, 0x06, 0x01, 0x00, 0x08, 0, 0, 0 }, /* Viola */
	   { 0x71, 0x31, 0x49, 0x09, 0xd1, 0x61, 0x1c, 0x0c, 0x01, 0x00, 0x08, 0, 0, 0 }, /* Cello */
	   { 0x21, 0x23, 0x4d, 0x89, 0x71, 0x72, 0x12, 0x06, 0x01, 0x00, 0x02, 0, 0, 0 }, /* Contrabass */
	   { 0xf1, 0xe1, 0x40, 0x09, 0xf1, 0x6f, 0x21, 0x16, 0x01, 0x00, 0x02, 0, 0, 0 }, /* Tremolo Strings */
	   { 0x02, 0x01, 0x1a, 0x89, 0xf5, 0x85, 0x75, 0x35, 0x01, 0x00, 0x00, 0, 0, 0 }, /* Pizzicato Strings */
	   { 0x02, 0x01, 0x1d, 0x89, 0xf5, 0xf3, 0x75, 0xf4, 0x01, 0x00, 0x00, 0, 0, 0 }, /* Orchestral Strings */
	   { 0x10, 0x11, 0x41, 0x09, 0xf5, 0xf2, 0x05, 0xc3, 0x01, 0x00, 0x02, 0, 0, 0 }, /* Timpani */
	   { 0x21, 0xa2, 0x9b, 0x0a, 0xb1, 0x72, 0x25, 0x08, 0x01, 0x00, 0x0e, 0, 0, 0 }, /* String Ensemble 1 */
	   { 0xa1, 0x21, 0x98, 0x09, 0x7f, 0x3f, 0x03, 0x07, 0x01, 0x01, 0x00, 0, 0, 0 }, /* String Ensemble 2 */
	   { 0xa1, 0x61, 0x93, 0x09, 0xc1, 0x4f, 0x12, 0x05, 0x00, 0x00, 0x0a, 0, 0, 0 }, /* SynthStrings 1 */
	   { 0x21, 0x61, 0x18, 0x09, 0xc1, 0x4f, 0x22, 0x05, 0x00, 0x00, 0x0c, 0, 0, 0 }, /* SynthStrings 2 */
	   { 0x31, 0x72, 0x5b, 0x8c, 0xf4, 0x8a, 0x15, 0x05, 0x00, 0x00, 0x00, 0, 0, 0 }, /* Choir Aahs */
	   { 0xa1, 0x61, 0x90, 0x09, 0x74, 0x71, 0x39, 0x67, 0x00, 0x00, 0x00, 0, 0, 0 }, /* Voice Oohs */
	   { 0x71, 0x72, 0x57, 0x09, 0x54, 0x7a, 0x05, 0x05, 0x00, 0x00, 0x0c, 0, 0, 0 }, /* Synth Voice */
	   { 0x90, 0x41, 0x00, 0x09, 0x54, 0xa5, 0x63, 0x45, 0x00, 0x00, 0x08, 0, 0, 0 }, /* Orchestra Hit */
	   { 0x21, 0x21, 0x92, 0x0a, 0x85, 0x8f, 0x17, 0x09, 0x00, 0x00, 0x0c, 0, 0, 0 }, /* Trumpet */
	   { 0x21, 0x21, 0x94, 0x0e, 0x75, 0x8f, 0x17, 0x09, 0x00, 0x00, 0x0c, 0, 0, 0 }, /* Trombone */
	   { 0x21, 0x61, 0x94, 0x09, 0x76, 0x82, 0x15, 0x37, 0x00, 0x00, 0x0c, 0, 0, 0 }, /* Tuba */
	   { 0x31, 0x21, 0x43, 0x09, 0x9e, 0x62, 0x17, 0x2c, 0x01, 0x01, 0x02, 0, 0, 0 }, /* Muted Trumpet */
	   { 0x21, 0x21, 0x9b, 0x09, 0x61, 0x7f, 0x6a, 0x0a, 0x00, 0x00, 0x02, 0, 0, 0 }, /* French Horn */
	   { 0x61, 0x22, 0x8a, 0x0f, 0x75, 0x74, 0x1f, 0x0f, 0x00, 0x00, 0x08, 0, 0, 0 }, /* Brass Section */
	   { 0xa1, 0x21, 0x86, 0x8c, 0x72, 0x71, 0x55, 0x18, 0x01, 0x00, 0x00, 0, 0, 0 }, /* SynthBrass 1 */
	   { 0x21, 0x21, 0x4d, 0x09, 0x54, 0xa6, 0x3c, 0x1c, 0x00, 0x00, 0x08, 0, 0, 0 }, /* SynthBrass 2 */
	   { 0x31, 0x61, 0x8f, 0x09, 0x93, 0x72, 0x02, 0x0b, 0x01, 0x00, 0x08, 0, 0, 0 }, /* Soprano Sax */
	   { 0x31, 0x61, 0x8e, 0x09, 0x93, 0x72, 0x03, 0x09, 0x01, 0x00, 0x08, 0, 0, 0 }, /* Alto Sax */
	   { 0x31, 0x61, 0x91, 0x09, 0x93, 0x82, 0x03, 0x09, 0x01, 0x00, 0x0a, 0, 0, 0 }, /* Tenor Sax */
	   { 0x31, 0x61, 0x8e, 0x09, 0x93, 0x72, 0x0f, 0x0f, 0x01, 0x00, 0x0a, 0, 0, 0 }, /* Baritone Sax */
	   { 0x21, 0x21, 0x4b, 0x09, 0xaa, 0x8f, 0x16, 0x0a, 0x01, 0x00, 0x08, 0, 0, 0 }, /* Oboe */
	   { 0x31, 0x21, 0x90, 0x09, 0x7e, 0x8b, 0x17, 0x0c, 0x01, 0x01, 0x06, 0, 0, 0 }, /* English Horn */
	   { 0x31, 0x32, 0x81, 0x09, 0x75, 0x61, 0x19, 0x19, 0x01, 0x00, 0x00, 0, 0, 0 }, /* Bassoon */
	   { 0x32, 0x21, 0x90, 0x09, 0x9b, 0x72, 0x21, 0x17, 0x00, 0x00, 0x04, 0, 0, 0 }, /* Clarinet */
	   { 0xe1, 0xe1, 0x1f, 0x09, 0x85, 0x65, 0x5f, 0x1a, 0x00, 0x00, 0x00, 0, 0, 0 }, /* Piccolo */
	   { 0xe1, 0xe1, 0x46, 0x09, 0x88, 0x65, 0x5f, 0x1a, 0x00, 0x00, 0x00, 0, 0, 0 }, /* Flute */
	   { 0xa1, 0x21, 0x9c, 0x09, 0x75, 0x75, 0x1f, 0x0a, 0x00, 0x00, 0x02, 0, 0, 0 }, /* Recorder */
	   { 0x31, 0x21, 0x8b, 0x09, 0x84, 0x65, 0x58, 0x1a, 0x00, 0x00, 0x00, 0, 0, 0 }, /* Pan Flute */
	   { 0xe1, 0xa1, 0x4c, 0x09, 0x66, 0x65, 0x56, 0x26, 0x00, 0x00, 0x00, 0, 0, 0 }, /* Blown Bottle */
	   { 0x62, 0xa1, 0xcb, 0x09, 0x76, 0x55, 0x46, 0x36, 0x00, 0x00, 0x00, 0, 0, 0 }, /* Skakuhachi */
	   { 0x62, 0xa1, 0xa2, 0x09, 0x57, 0x56, 0x07, 0x07, 0x00, 0x00, 0x0b, 0, 0, 0 }, /* Whistle */
	   { 0x62, 0xa1, 0x9c, 0x09, 0x77, 0x76, 0x07, 0x07, 0x00, 0x00, 0x0b, 0, 0, 0 }, /* Ocarina */
	   { 0x22, 0x21, 0x59, 0x09, 0xff, 0xff, 0x03, 0x0f, 0x02, 0x00, 0x00, 0, 0, 0 }, /* Lead 1 (square) */
	   { 0x21, 0x21, 0x0e, 0x09, 0xff, 0xff, 0x0f, 0x0f, 0x01, 0x01, 0x00, 0, 0, 0 }, /* Lead 2 (sawtooth) */
	   { 0x22, 0x21, 0x46, 0x89, 0x86, 0x64, 0x55, 0x18, 0x00, 0x00, 0x00, 0, 0, 0 }, /* Lead 3 (calliope) */
	   { 0x21, 0xa1, 0x45, 0x09, 0x66, 0x96, 0x12, 0x0a, 0x00, 0x00, 0x00, 0, 0, 0 }, /* Lead 4 (chiff) */
	   { 0x21, 0x22, 0x8b, 0x09, 0x92, 0x91, 0x2a, 0x2a, 0x01, 0x00, 0x00, 0, 0, 0 }, /* Lead 5 (charang) */
	   { 0xa2, 0x61, 0x9e, 0x49, 0xdf, 0x6f, 0x05, 0x07, 0x00, 0x00, 0x02, 0, 0, 0 }, /* Lead 6 (voice) */
	   { 0x20, 0x60, 0x1a, 0x09, 0xef, 0x8f, 0x01, 0x06, 0x00, 0x02, 0x00, 0, 0, 0 }, /* Lead 7 (fifths) */
	   { 0x21, 0x21, 0x8f, 0x86, 0xf1, 0xf4, 0x29, 0x09, 0x00, 0x00, 0x0a, 0, 0, 0 }, /* Lead 8 (bass+lead) */
	   { 0x77, 0xa1, 0xa5, 0x09, 0x53, 0xa0, 0x94, 0x05, 0x00, 0x00, 0x02, 0, 0, 0 }, /* Pad 1 (new age) */
	   { 0x61, 0xb1, 0x1f, 0x89, 0xa8, 0x25, 0x11, 0x03, 0x00, 0x00, 0x0a, 0, 0, 0 }, /* Pad 2 (warm) */
	   { 0x61, 0x61, 0x17, 0x09, 0x91, 0x55, 0x34, 0x16, 0x00, 0x00, 0x0c, 0, 0, 0 }, /* Pad 3 (polysynth) */
	   { 0x71, 0x72, 0x5d, 0x09, 0x54, 0x6a, 0x01, 0x03, 0x00, 0x00, 0x00, 0, 0, 0 }, /* Pad 4 (choir) */
	   { 0x21, 0xa2, 0x97, 0x09, 0x21, 0x42, 0x43, 0x35, 0x00, 0x00, 0x08, 0, 0, 0 }, /* Pad 5 (bowed) */
	   { 0xa1, 0x21, 0x1c, 0x09, 0xa1, 0x31, 0x77, 0x47, 0x01, 0x01, 0x00, 0, 0, 0 }, /* Pad 6 (metallic) */
	   { 0x21, 0x61, 0x89, 0x0c, 0x11, 0x42, 0x33, 0x25, 0x00, 0x00, 0x0a, 0, 0, 0 }, /* Pad 7 (halo) */
	   { 0xa1, 0x21, 0x15, 0x09, 0x11, 0xcf, 0x47, 0x07, 0x01, 0x00, 0x00, 0, 0, 0 }, /* Pad 8 (sweep) */
	   { 0x3a, 0x51, 0xce, 0x09, 0xf8, 0x86, 0xf6, 0x02, 0x00, 0x00, 0x02, 0, 0, 0 }, /* FX 1 (rain) */
	   { 0x21, 0x21, 0x15, 0x09, 0x21, 0x41, 0x23, 0x13, 0x01, 0x00, 0x00, 0, 0, 0 }, /* FX 2 (soundtrack) */
	   { 0x06, 0x01, 0x5b, 0x09, 0x74, 0xa5, 0x95, 0x72, 0x00, 0x00, 0x00, 0, 0, 0 }, /* FX 3 (crystal) */
	   { 0x22, 0x61, 0x92, 0x8c, 0xb1, 0xf2, 0x81, 0x26, 0x00, 0x00, 0x0c, 0, 0, 0 }, /* FX 4 (atmosphere) */
	   { 0x41, 0x42, 0x4d, 0x09, 0xf1, 0xf2, 0x51, 0xf5, 0x01, 0x00, 0x00, 0, 0, 0 }, /* FX 5 (brightness) */
	   { 0x61, 0xa3, 0x94, 0x89, 0x11, 0x11, 0x51, 0x13, 0x01, 0x00, 0x06, 0, 0, 0 }, /* FX 6 (goblins) */
	   { 0x61, 0xa1, 0x8c, 0x89, 0x11, 0x1d, 0x31, 0x03, 0x00, 0x00, 0x06, 0, 0, 0 }, /* FX 7 (echoes) */
	   { 0xa4, 0x61, 0x4c, 0x09, 0xf3, 0x81, 0x73, 0x23, 0x01, 0x00, 0x04, 0, 0, 0 }, /* FX 8 (sci-fi) */
	   { 0x02, 0x07, 0x85, 0x0c, 0xd2, 0xf2, 0x53, 0xf6, 0x00, 0x01, 0x00, 0, 0, 0 }, /* Sitar */
	   { 0x11, 0x13, 0x0c, 0x89, 0xa3, 0xa2, 0x11, 0xe5, 0x01, 0x00, 0x00, 0, 0, 0 }, /* Banjo */
	   { 0x11, 0x11, 0x06, 0x09, 0xf6, 0xf2, 0x41, 0xe6, 0x01, 0x02, 0x04, 0, 0, 0 }, /* Shamisen */
	   { 0x93, 0x91, 0x91, 0x09, 0xd4, 0xeb, 0x32, 0x11, 0x00, 0x01, 0x08, 0, 0, 0 }, /* Koto */
	   { 0x04, 0x01, 0x4f, 0x09, 0xfa, 0xc2, 0x56, 0x05, 0x00, 0x00, 0x0c, 0, 0, 0 }, /* Kalimba */
	   { 0x21, 0x22, 0x49, 0x09, 0x7c, 0x6f, 0x20, 0x0c, 0x00, 0x01, 0x06, 0, 0, 0 }, /* Bagpipe */
	   { 0x31, 0x21, 0x85, 0x09, 0xdd, 0x56, 0x33, 0x16, 0x01, 0x00, 0x0a, 0, 0, 0 }, /* Fiddle */
	   { 0x20, 0x21, 0x04, 0x8a, 0xda, 0x8f, 0x05, 0x0b, 0x02, 0x00, 0x06, 0, 0, 0 }, /* Shanai */
	   { 0x05, 0x03, 0x6a, 0x89, 0xf1, 0xc3, 0xe5, 0xe5, 0x00, 0x00, 0x06, 0, 0, 0 }, /* Tinkle Bell */
	   { 0x07, 0x02, 0x15, 0x09, 0xec, 0xf8, 0x26, 0x16, 0x00, 0x00, 0x0a, 0, 0, 0 }, /* Agogo */
	   { 0x05, 0x01, 0x9d, 0x09, 0x67, 0xdf, 0x35, 0x05, 0x00, 0x00, 0x08, 0, 0, 0 }, /* Steel Drums */
	   { 0x18, 0x12, 0x96, 0x09, 0xfa, 0xf8, 0x28, 0xe5, 0x00, 0x00, 0x0a, 0, 0, 0 }, /* Woodblock */
	   { 0x10, 0x00, 0x86, 0x0c, 0xa8, 0xfa, 0x07, 0x03, 0x00, 0x00, 0x06, 0, 0, 0 }, /* Taiko Drum */
	   { 0x11, 0x10, 0x41, 0x0c, 0xf8, 0xf3, 0x47, 0x03, 0x02, 0x00, 0x04, 0, 0, 0 }, /* Melodic Tom */
	   { 0x01, 0x10, 0x8e, 0x09, 0xf1, 0xf3, 0x06, 0x02, 0x02, 0x00, 0x0e, 0, 0, 0 }, /* Synth Drum */
	   { 0x0e, 0xc0, 0x00, 0x09, 0x1f, 0x1f, 0x00, 0xff, 0x00, 0x03, 0x0e, 0, 0, 0 }, /* Reverse Cymbal */
	   { 0x06, 0x03, 0x80, 0x91, 0xf8, 0x56, 0x24, 0x84, 0x00, 0x02, 0x0e, 0, 0, 0 }, /* Guitar Fret Noise */
	   { 0x0e, 0xd0, 0x00, 0x0e, 0xf8, 0x34, 0x00, 0x04, 0x00, 0x03, 0x0e, 0, 0, 0 }, /* Breath Noise */
	   { 0x0e, 0xc0, 0x00, 0x09, 0xf6, 0x1f, 0x00, 0x02, 0x00, 0x03, 0x0e, 0, 0, 0 }, /* Seashore */
	   { 0xd5, 0xda, 0x95, 0x49, 0x37, 0x56, 0xa3, 0x37, 0x00, 0x00, 0x00, 0, 0, 0 }, /* Bird Tweet */
	   { 0x35, 0x14, 0x5c, 0x11, 0xb2, 0xf4, 0x61, 0x15, 0x02, 0x00, 0x0a, 0, 0, 0 }, /* Telephone ring */
	   { 0x0e, 0xd0, 0x00, 0x09, 0xf6, 0x4f, 0x00, 0xf5, 0x00, 0x03, 0x0e, 0, 0, 0 }, /* Helicopter */
	   { 0x26, 0xe4, 0x00, 0x09, 0xff, 0x12, 0x01, 0x16, 0x00, 0x01, 0x0e, 0, 0, 0 }, /* Applause */
	   { 0x00, 0x00, 0x00, 0x09, 0xf3, 0xf6, 0xf0, 0xc9, 0x00, 0x02, 0x0e, 0, 0, 0 }  /* Gunshot */
	};
	
	// logarithmic relationship between midi and FM volumes
	private static final int my_midi_fm_vol_table[] =
	{
		   0,  0xb, 0x10, 0x13, 0x16, 0x19, 0x1b, 0x1d, 0x20, 0x21, 
		0x23, 0x25, 0x27, 0x28, 0x2a, 0x2b, 0x2d, 0x2e, 0x30, 0x31, 
		0x32, 0x33, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3a, 0x3b, 0x3c, 
		0x3d, 0x3e, 0x40, 0x40, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 
		0x47, 0x48, 0x49, 0x4a, 0x4b, 0x4b, 0x4c, 0x4d, 0x4e, 0x4f, 
		0x50, 0x50, 0x51, 0x52, 0x53, 0x53, 0x54, 0x55, 0x56, 0x56, 
		0x57, 0x58, 0x59, 0x59, 0x5a, 0x5b, 0x5b, 0x5c, 0x5d, 0x5d, 
		0x5e, 0x5f, 0x60, 0x60, 0x61, 0x61, 0x62, 0x63, 0x63, 0x64, 
		0x65, 0x65, 0x66, 0x67, 0x67, 0x68, 0x68, 0x69, 0x6a, 0x6a, 
		0x6b, 0x6b, 0x6c, 0x6d, 0x6d, 0x6e, 0x6e, 0x6f, 0x70, 0x70, 
		0x71, 0x71, 0x72, 0x72, 0x73, 0x73, 0x74, 0x75, 0x75, 0x76, 
		0x76, 0x77, 0x77, 0x78, 0x78, 0x79, 0x79, 0x7a, 0x7a, 0x7b, 
		0x7b, 0x7c, 0x7c, 0x7d, 0x7d, 0x7e, 0x7e, 0x7f
	};

	/**
	 * Constructor for MIDSequence
	 */
	public MIDSequence()
	{
		super();
		adlib_data = new int[256];
		myinsbank = new int[128][16];
		smyinsbank = new int[128][16];
		chp = new int[18][3];

		ch = new midi_channel[16];
		for (int i=0; i<ch.length; i++) ch[i]=new midi_channel();
		
		track = new midi_track[16];
		for (int i=0; i<track.length; i++) track[i] = new midi_track();
	}

	private long datalook(long pos)
	{
		if (pos<0 || pos>=flen)
			return 0;
		else
			return (long)(data[(int)pos])&0xFF;
	}
	private long getnexti(final int num)
	{
		long v = 0;

		for (int i=0; i<num; i++)
		{
			v += (datalook(pos++) << (8 * i));
		}
		return v;
	}
	private long getnext(long num)
	{
		long v = 0;

		for (long i=0; i<num; i++)
		{
			v <<= 8;
			v += datalook(pos++);
		}
		return v;
	}
	private long getval()
	{
		long b = getnext(1);
		long v = b & 0x7f;
		while ((b & 0x80) != 0)
		{
			b = getnext(1);
			v = (v << 7) + (b & 0x7F);
		}
		return v;
	}
	private void copyInsBanks()
	{
		for (int x=0; x<128; x++)
			for (int y=0; y<16; y++)
				smyinsbank[x][y] = myinsbank[x][y];
	}
	/**
	 * We are looking for a patch.003 here. This is adplug specific,
	 * as sci and patch.003 need to have the same 3 char prefix, so
	 * we can here get the two that belong together.
	 * Like KQ4_music.sci + KQ4patch.003
	 * The patch.003 is the patch needed for sound blaster cards
	 * See here: http://www.vgmpf.com/Wiki/index.php?title=AdPlug at topic "sci"
	 * As javamod constantly works with URLs instead of File, this is a bit
	 * more tricky...
	 * @since 04.08.2020
	 * @param fileURL
	 * @return
	 * @throws IOException
	 */
	private boolean load_sierra_ins(final URL fileURL)
	{
		// get patch.003 URL with 3 char prefix of fileURL
		final String fileName = Helpers.getFileNameFromURL(fileURL);
		if (fileName.length()<3) return false; // already finished
		
		final String path = fileURL.getFile();
		final String patchFileName = ((new StringBuilder()).append(path.substring(0, path.lastIndexOf('/'))).append('/').append(fileName.substring(0, 3)).append("patch.003")).toString();

		RandomAccessInputStreamImpl inputStream = null;
		try
		{
			final URL patchFileURL = new URL(fileURL.getProtocol(), fileURL.getHost(), fileURL.getPort(), patchFileName);
			if (!Helpers.urlExists(patchFileURL)) return false;
			inputStream = new RandomAccessInputStreamImpl(patchFileURL);
			if (inputStream.available()==0) return false;
			
			stins = 0;
			int [] ins = new int[28];
			for (int i=0; i<2; i++)
			{
				inputStream.skip(2);
				for (int k=0; k<48; k++)
				{
					int l = i*48+k;
					for (int j=0; j<28; j++) ins[j] = inputStream.read();

					myinsbank[l][0] = (ins[ 9] * 0x80) + (ins[10] * 0x40) + (ins[ 5] * 0x20) + (ins[11] * 0x10) + ins[ 1]; // 1=ins5
					myinsbank[l][1] = (ins[22] * 0x80) + (ins[23] * 0x40) + (ins[18] * 0x20) + (ins[24] * 0x10) + ins[14]; // 1=ins18
					myinsbank[l][2] = (ins[ 0] << 6) + ins[ 8];
					myinsbank[l][3] = (ins[13] << 6) + ins[21];
					myinsbank[l][4] = (ins[ 3] << 4) + ins[ 6];
					myinsbank[l][5] = (ins[16] << 4) + ins[19];
					myinsbank[l][6] = (ins[ 4] << 4) + ins[ 7];
					myinsbank[l][7] = (ins[17] << 4) + ins[20];
					myinsbank[l][8] =  ins[26];
					myinsbank[l][9] =  ins[27];
					myinsbank[l][10] = ((ins[2] << 1)) + (1 - (ins[12] & 1));

					stins++;
				}
			}
			copyInsBanks();
			// java 8 - creates a new array set
			//smyinsbank = Arrays.stream(myinsbank).map(int[]::clone).toArray(int[][]::new);
		}
		catch (Throwable ex)
		{
			return false; // something went wrong, so we return false
		}
		finally
		{
			if (inputStream!=null) try { inputStream.close(); } catch (Throwable ex) { /* NOOP */ }
		}
		
		return true;
	}
	private void sierra_next_section()
	{
		for (int i = 0; i < 16; i++)
			track[i].on = false;

		pos = sierra_pos;
		int i = 0;
		int j = 0;
		while (i != 0xff)
		{
			getnext(1);
			final int curtrack = j;
			j++;
			if (curtrack >= 16) break;
			track[curtrack].on = true;
			track[curtrack].spos = getnext(1);
			track[curtrack].spos += (getnext(1) << 8) + 4; // 4 best usually +3? not 0,1,2 or 5
			track[curtrack].tend = flen; // 0xFC will kill it
			track[curtrack].iwait = 0;
			track[curtrack].pv = 0;

			getnext(2);
			i = (int) getnext(1);
		}
		getnext(2);
		deltas = 0x20;
		sierra_pos = pos;
		fwait = 0;
		firstRound = true;
	}
	/**
	 * @param inputStream
	 * @throws IOException
	 * @see de.quippy.javamod.multimedia.opl3.sequencer.OPL3Sequence#readOPL3Sequence(de.quippy.javamod.io.RandomAccessInputStreamImpl)
	 */
	@Override
	protected void readOPL3Sequence(final RandomAccessInputStreamImpl inputStream) throws IOException
	{
		if (inputStream==null || inputStream.available()<=0) return;
		
		final long lengthOfStream = inputStream.getLength();

		final int [] magicBytes = new int[6];
		for (int i=0; i<magicBytes.length; i++)
			magicBytes[i] = (int)(inputStream.readByte()&0xFF);
		
		int type = 0;
		switch (magicBytes[0])
		{
			case 0x41: //ADL
				if (magicBytes[1]==0x44 && magicBytes[2]==0x4C && magicBytes[3]==0x20)
					type = FILE_LUCAS;
				break;
			case 0x4D: // MThd
				if (magicBytes[1] == 0x54 && magicBytes[2] == 0x68 && magicBytes[3] == 0x64)
					type = FILE_MIDI;
				break;
			case 0x43: // CTMF
				if (magicBytes[1] == 0x54 && magicBytes[2] == 0x4d && magicBytes[3] == 0x46)
					type = FILE_CMF;
				break;
			case 0x84:
				if (magicBytes[1]==0 && load_sierra_ins(url))
					if (magicBytes[2] == 0xF0)
						type = FILE_ADVSIERRA;
					else
						type = FILE_SIERRA;
				break;
			default:
				final long size = ((long)magicBytes[0]&0xFF) | (((long)magicBytes[1]&0xFF)<<8) | (((long)magicBytes[3]&0xFF)<<24) | (((long)magicBytes[2]&0xFF)<<16);
				if (size == lengthOfStream && magicBytes[4]==0x41 && magicBytes[5]==0x44)
					type = FILE_OLDLUCAS;
		}
		if (type==0) throw new IOException("Unsupported file type");
		this.type = type;
		flen = lengthOfStream;
		data = new byte [(int)flen];
		inputStream.seek(0);
		inputStream.read(data, 0, (int)flen);
	}
	private void midi_write_adlib(final EmuOPL opl, final int r, final int v)
	{
		opl.writeOPL2(r, v);
		adlib_data[r] = v;
	}
	private void midi_fm_instrument(final EmuOPL opl, final int voice, final int inst[])
	{
		// just got to make sure this happens.. 'cause who knows when it'll be reset otherwise.
		if ((adlib_style & SIERRA_STYLE) != 0) midi_write_adlib(opl, 0xbd, 0);

		midi_write_adlib(opl, 0x20 + adlib_opadd[voice], inst[0]);
		midi_write_adlib(opl, 0x23 + adlib_opadd[voice], inst[1]);

		if ((adlib_style & LUCAS_STYLE) != 0)
		{
			midi_write_adlib(opl, 0x43 + adlib_opadd[voice], 0x3f);
			if ((inst[10] & 1) == 0)
				midi_write_adlib(opl, 0x40 + adlib_opadd[voice], inst[2]);
			else
				midi_write_adlib(opl, 0x40 + adlib_opadd[voice], 0x3F);

		}
		else
		if ((adlib_style & SIERRA_STYLE) != 0 || (adlib_style & CMF_STYLE) != 0)
		{
			midi_write_adlib(opl, 0x40 + adlib_opadd[voice], inst[2]);
			midi_write_adlib(opl, 0x43 + adlib_opadd[voice], inst[3]);

		}
		else
		{
			midi_write_adlib(opl, 0x40 + adlib_opadd[voice], inst[2]);
			if ((inst[10] & 1) == 0)
				midi_write_adlib(opl, 0x43 + adlib_opadd[voice], inst[3]);
			else
				midi_write_adlib(opl, 0x43 + adlib_opadd[voice], 0);
		}

		midi_write_adlib(opl, 0x60 + adlib_opadd[voice], inst[4]);
		midi_write_adlib(opl, 0x63 + adlib_opadd[voice], inst[5]);
		midi_write_adlib(opl, 0x80 + adlib_opadd[voice], inst[6]);
		midi_write_adlib(opl, 0x83 + adlib_opadd[voice], inst[7]);
		midi_write_adlib(opl, 0xE0 + adlib_opadd[voice], inst[8]);
		midi_write_adlib(opl, 0xE3 + adlib_opadd[voice], inst[9]);
		midi_write_adlib(opl, 0xC0 + voice, inst[10]);
	}
	private void midi_fm_percussion(final EmuOPL opl, final int ch, final int inst[])
	{
		if (ch<12) return; // should never happen!
		int opadd = map_chan[ch - 12];

		midi_write_adlib(opl, 0x20 + opadd, inst[0]);
		midi_write_adlib(opl, 0x40 + opadd, inst[2]);
		midi_write_adlib(opl, 0x60 + opadd, inst[4]);
		midi_write_adlib(opl, 0x80 + opadd, inst[6]);
		midi_write_adlib(opl, 0xE0 + opadd, inst[8]);
		if (opadd < 0x13) // only output this for the modulator, not the carrier, as it affects the entire channel
			midi_write_adlib(opl, 0xc0 + percussion_map[ch - 11], inst[10]);
	}
	private void midi_fm_volume(final EmuOPL opl, final int voice, final int volume)
	{
		if ((adlib_style & SIERRA_STYLE) == 0) // sierra likes it loud!
		{
			int vol = volume >> 2;

			if ((adlib_style & LUCAS_STYLE) != 0)
			{
				if ((adlib_data[0xc0 + voice] & 1) == 1) 
					midi_write_adlib(opl, 0x40 + adlib_opadd[voice], (63 - vol) | (adlib_data[0x40 + adlib_opadd[voice]] & 0xC0));
				midi_write_adlib(opl, 0x43 + adlib_opadd[voice], (63 - vol) | (adlib_data[0x43 + adlib_opadd[voice]] & 0xC0));
			}
			else
			{
				if ((adlib_data[0xc0 + voice] & 1) == 1) 
					midi_write_adlib(opl, 0x40 + adlib_opadd[voice], (63 - vol) | (adlib_data[0x40 + adlib_opadd[voice]] & 0xC0));
				midi_write_adlib(opl, 0x43 + adlib_opadd[voice], (63 - vol) | (adlib_data[0x43 + adlib_opadd[voice]] & 0xC0));
			}
		}
	}
	private void midi_fm_playnote(final EmuOPL opl, final int voice, final int note, final int volume)
	{
		int n = (note < 0) ? 12 - (note % 12) : note;
		int freq = fnums[n % 12];
		int oct = n / 12;

		midi_fm_volume(opl, voice, volume);
		midi_write_adlib(opl, 0xA0 + voice, freq & 0xFF);

		int c = ((freq & 0x300) >> 8) + ((oct & 7) << 2) + ((adlib_mode == ADLIB_MELODIC || voice < 6) ? (1 << 5) : 0);
		midi_write_adlib(opl, 0xB0 + voice, c);
	}
	private void midi_fm_endnote(final EmuOPL opl, int voice)
	{
		//midi_fm_volume(opl, voice, 0);
		//midi_write_adlib(opl, 0xb0 + voice, 0);

		midi_write_adlib(opl, 0xB0 + voice, adlib_data[0xB0 + voice] & (255 - 32));
	}
	private void midi_fm_reset(final EmuOPL opl)
	{
		// reset OPL
		resetOPL(opl);

	    for (int j = 0xc0; j <= 0xc8; j++)
			midi_write_adlib(opl, j, 0xf0);

	    midi_write_adlib(opl, 0x01, 0x20);
	    midi_write_adlib(opl, 0xBD, 0xC0);
	}
	/**
	 * @param opl
	 * @return
	 * @see de.quippy.javamod.multimedia.opl3.sequencer.OPL3Sequence#updateToOPL(de.quippy.opl3.OPL3)
	 */
	@Override
	public boolean updateToOPL(final EmuOPL opl)
	{
		if (firstRound)
		{
			// just get the first wait and ignore it :>
			for (int curtrack=0; curtrack<16; curtrack++)
				if (track[curtrack].on)
				{
					pos = track[curtrack].pos;
					if (type != FILE_SIERRA && type != FILE_ADVSIERRA)
						track[curtrack].iwait += getval();
					else
						track[curtrack].iwait += getnext(1);
					track[curtrack].pos = pos;
				}
			firstRound = false;
		}

		iwait = 0;
		boolean running = true;

	    while (iwait==0 && running)
        {
			for (int curtrack=0; curtrack<16; curtrack++)
			{
				if (track[curtrack].on && track[curtrack].iwait==0 && track[curtrack].pos<track[curtrack].tend)
				{
					pos = track[curtrack].pos;
					int v = (int)getnext(1);
					
					// This is to do implied MIDI events.
					if (v < 0x80)
					{
						v = track[curtrack].pv;
						pos--;
					}
					track[curtrack].pv = v;

					int c =  v & 0x0f;
					switch (v & 0xf0)
					{
						case 0x80: /* note off */
							int note = (int)getnext(1);
							int vel = (int)getnext(1);
							for (int i = 0; i < 9; i++)
								if (chp[i][0] == c && chp[i][1] == note)
								{
									midi_fm_endnote(opl, i);
									chp[i][0] = -1;
								}
							break;
						case 0x90: /* note on */
							// firstRound=0;
							note = (int)getnext(1);
							vel = (int)getnext(1);

							int numchan = (adlib_mode == ADLIB_RYTHM) ? 6 : 9;

							if (ch[c].on)
							{
								for (int i = 0; i < 18; i++)
									chp[i][2]++;

								int on = -1;
								if (c < 11 || adlib_mode == ADLIB_MELODIC)
								{
									int j = 0;
									int onl = 0;
									for (int i = 0; i < numchan; i++)
										if (chp[i][0] == -1 && chp[i][2] > onl)
										{
											onl = chp[i][2];
											on = i;
											j = 1;
										}

									if (on == -1)
									{
										onl = 0;
										for (int i = 0; i < numchan; i++)
											if (chp[i][2] > onl)
											{
												onl = chp[i][2];
												on = i;
											}
									}

									if (j == 0) midi_fm_endnote(opl, on);
								}
								else
									on = percussion_map[c - 11];

								if (vel != 0 && ch[c].inum >= 0 && ch[c].inum < 128)
								{
									// 11 == bass drum, handled like a normal instrument, on == channel 6 thanks to
									// percussion_map[] above
									if (adlib_mode == ADLIB_MELODIC || c < 12)
										midi_fm_instrument(opl, on, ch[c].ins);
									else
										midi_fm_percussion(opl, c, ch[c].ins);

									int nv;
									if ((adlib_style & MIDI_STYLE) != 0)
									{
										nv = ((ch[c].vol * vel) / 128);
										if ((adlib_style & LUCAS_STYLE) != 0) nv *= 2;
										if (nv > 127) nv = 127;
										nv = my_midi_fm_vol_table[nv];
										if ((adlib_style & LUCAS_STYLE) != 0) nv = (int) (Math.sqrt((double) nv) * 11);
									}
									else if ((adlib_style & CMF_STYLE) != 0)
									{
										// CMF doesn't support note velocity (even though some files have them!)
										nv = 127;
									}
									else
									{
										nv = vel;
									}

									midi_fm_playnote(opl, on, note + ch[c].nshift, nv * 2); // sets freq in rhythm mode
									chp[on][0] = c;
									chp[on][1] =  note;
									chp[on][2] = 0;

									if (adlib_mode == ADLIB_RYTHM && c >= 11)
									{
										// Still need to turn off the perc instrument before playing it again,
										// as not all songs send a noteoff.
										midi_write_adlib(opl, 0xbd, adlib_data[0xbd] & ~(0x10 >> (c - 11)));
										// Play the perc instrument
										midi_write_adlib(opl, 0xbd, adlib_data[0xbd] | (0x10 >> (c - 11)));
									}

								}
								else
								{
									if (vel == 0)
									{ // same code as end note
										if (adlib_mode == ADLIB_RYTHM && c >= 11)
										{
											// Turn off the percussion instrument
											midi_write_adlib(opl, 0xbd, adlib_data[0xbd] & ~(0x10 >> (c - 11)));
											// midi_fm_endnote(percussion_map[c]);
											chp[percussion_map[c - 11]][0] = -1;
										}
										else
										{
											for (int i = 0; i < 9; i++)
											{
												if (chp[i][0] == c && chp[i][1] == note)
												{
													// midi_fm_volume(i,0); // really end the note
													midi_fm_endnote(opl, i);
													chp[i][0] = -1;
												}
											}
										}
									}
									else
									{
										// i forget what this is for.
										chp[on][0] = -1;
										chp[on][2] = 0;
									}
								}
							}
							break;
			            case 0xa0: /*key after touch */
							note = (int)getnext(1);
							vel = (int)getnext(1);
//							// this might all be good
//							for (int i = 0; i < 9; i++)
//								if (chp[i][0] == c & chp[i][1] == note)
//									midi_fm_playnote(opl, i, note + cnote[c], my_midi_fm_vol_table[(cvols[c] * vel) / 128] * 2);
							break;
						case 0xb0: /* control change .. pitch bend? */
							int ctrl = (int)getnext(1);
							vel = (int)getnext(1);

							switch (ctrl)
							{
								case 0x07:
									ch[c].vol = vel;
									break;
								case 0x63:
									if ((adlib_style & CMF_STYLE) != 0)
									{
										// Custom extension to allow CMF files to switch the
										// AM+VIB depth on and off (officially this is on,
										// and there's no way to switch it off.) Controller
										// values:
										// 0 == AM+VIB off
										// 1 == VIB on
										// 2 == AM on
										// 3 == AM+VIB on
										midi_write_adlib(opl, 0xbd, (adlib_data[0xbd] & ~0xC0) | (vel << 6));
									}
									break;
								case 0x67:
									if ((adlib_style & CMF_STYLE) != 0)
									{
										adlib_mode = vel;
										if (adlib_mode == ADLIB_RYTHM)
											midi_write_adlib(opl, 0xbd, adlib_data[0xbd] | (1 << 5));
										else
											midi_write_adlib(opl, 0xbd, adlib_data[0xbd] & ~(1 << 5));
									}
									break;
							}
							break;
						case 0xc0: /* patch change */
							int x = (int)getnext(1);
							ch[c].inum = x & 0x7f;
							for (int j = 0; j < 11; j++)
								ch[c].ins[j] = myinsbank[ch[c].inum][j];
							break;
						case 0xd0: /* channel touch */
							/*int x = (int)*/getnext(1);
							break;
						case 0xe0: /* pitch wheel */
							/*x =  (int)*/getnext(1);
							/*x =  (int)*/getnext(1);
							break;
			            case 0xf0:
							switch (v)
							{
								case 0xf0:
								case 0xf7: /* sysex */
									long l = getval();
									boolean readAdd = false;
									if (datalook(pos + l) == 0xf7) readAdd=true;

									if (datalook(pos) == 0x7d && datalook(pos + 1) == 0x10 && datalook(pos + 2) < 16)
									{
										adlib_style = LUCAS_STYLE | MIDI_STYLE;
										getnext(1);
										getnext(1);
										int channel = (int)getnext(1) & 0x0f;
										getnext(1);

										// getnext(22); //temp
										ch[channel].ins[ 0] = (int)((getnext(1L) << 4) + getnext(1L));
										ch[channel].ins[ 2] = (int)(0xffL - ((getnext(1L) << 4) + getnext(1L) & 0x3fL));
										ch[channel].ins[ 4] = (int)(0xffL - ((getnext(1L) << 4) + getnext(1L)));
										ch[channel].ins[ 6] = (int)(0xffL - ((getnext(1L) << 4) + getnext(1L)));
										ch[channel].ins[ 8] = (int)((getnext(1L) << 4) + getnext(1L));
										ch[channel].ins[ 1] = (int)((getnext(1L) << 4) + getnext(1L));
										ch[channel].ins[ 3] = (int)(0xffL - ((getnext(1L) << 4) + getnext(1L) & 0x3fL));
										ch[channel].ins[ 5] = (int)(0xffL - ((getnext(1L) << 4) + getnext(1L)));
										ch[channel].ins[ 7] = (int)(0xffL - ((getnext(1L) << 4) + getnext(1L)));
										ch[channel].ins[ 9] = (int)((getnext(1L) << 4) + getnext(1L));
										ch[channel].ins[10] = (int)(getnext(1) << 4 + getnext(1));

										// if ((i&1)==1) ch[channel].ins[10]=1;

										getnext(l - 26);
									}
									else
									{
			                            for (int y=0; y<l; y++) getnext(1);
									}

									if (readAdd) getnext(1);
									break;
								case 0xf1:
									break;
								case 0xf2:
									getnext(2);
									break;
								case 0xf3:
									getnext(1);
									break;
								case 0xf4:
									break;
								case 0xf5:
									break;
								case 0xf6: /* something */
								case 0xf8:
								case 0xfa:
								case 0xfb:
								case 0xfc:
									// this ends the track for sierra.
									if (type == FILE_SIERRA || type == FILE_ADVSIERRA)
										track[curtrack].tend = pos;
									break;
								case 0xfe:
									break;
								case 0xfd:
									break;
								case 0xff:
									v = (int)getnext(1);
									l = getval();
									if (v == 0x51)
									{
										msqtr = getnext(l); /* set tempo */
									}
		                            else
		                            {
		                            	for (int y=0; y<l; y++) getnext(1);
		                            }
									break;
							}
							break;
			            default: 
			            	/* if we get down here, an error occurred */
						break;
					}

					if (pos < track[curtrack].tend)
					{
						long w;
						if (type != FILE_SIERRA && type != FILE_ADVSIERRA)
							w = getval();
						else
							w = getnext(1);
						track[curtrack].iwait = w;

//						if (w!=0)
//						{
//							float f = ((float)w/(float)deltas)*((float)msqtr/(float)1000000);
//							if (firstRound==1) f=0; //not playing yet. don't wait yet
//						}
					}
					else
						track[curtrack].iwait = 0;

					track[curtrack].pos = pos;
				}
			}

			running = false; // end of song.
			iwait = 0;
			for (int curtrack = 0; curtrack < 16; curtrack++)
				if (track[curtrack].on && track[curtrack].pos < track[curtrack].tend) running = true; // not yet..

			if (running)
			{
				iwait = 0xffffff; // bigger than any wait can be!
				for (int curtrack = 0; curtrack < 16; curtrack++)
					if (track[curtrack].on && track[curtrack].pos < track[curtrack].tend && track[curtrack].iwait < iwait) iwait = track[curtrack].iwait;
			}
		}

	    if (iwait != 0 && running)
		{
			for (int curtrack = 0; curtrack < 16; curtrack++)
				if (track[curtrack].on) track[curtrack].iwait -= iwait;

			fwait = 1.0d / (((double) iwait / (double) deltas) * ((double) msqtr / (double) 1000000));
		}
		else
			fwait = 50; // 1/50th of a second
		
//		if (!running && type == FILE_ADVSIERRA) 
//			if (datalook(sierra_pos - 2) != 0xff)
//			{
//				sierra_next_section(p);
//				fwait = 50;
//				running = true;
//			}

	    return running;
	}
	/**
	 * @param opl
	 * @see de.quippy.javamod.multimedia.opl3.sequencer.OPL3Sequence#initialize(de.quippy.opl3.OPL3)
	 */
	@Override
	public void initialize(final EmuOPL opl)
	{
		initialize(opl, 0);
	}
	/**
	 * Sierra games have subsongs - we will support that later
	 * @since 04.08.2020
	 * @param opl
	 * @param subsong
	 */
	public void initialize(final EmuOPL opl, int subsong)
	{
		pos = 0;
		tins = 0;
		adlib_style = MIDI_STYLE | CMF_STYLE;
		adlib_mode = ADLIB_MELODIC;
		for (int x = 0; x < 128; x++)
		{
			for (int y = 0; y < 14; y++)
				myinsbank[x][y] = midi_fm_instruments[x][y];
			
			myinsbank[x][14] = 0;
			myinsbank[x][15] = 0;
		}
		
		for (int x=0; x<16; x++)
		{
			ch[x].inum = 0;
			for (int y=0; y<11; y++)
				ch[x].ins[y] = myinsbank[ch[x].inum][y];
			ch[x].vol = 127;
			ch[x].nshift = -25;
			ch[x].on = true;
		}

		/* General init */
		for (int x=0; x<9; x++)
		{
			chp[x][0] = -1;
			chp[x][2] = 0;
		}

		deltas = 250; // just a number, not a standard
		msqtr = 500000;
		fwait = 123; // gotta be a small thing.. sorta like nothing
		iwait = 0;

		subsongs = 1;

		for (int x=0; x<16; x++)
		{
			track[x].tend = 0;
			track[x].spos = 0;
			track[x].pos = 0;
			track[x].iwait = 0;
			track[x].on = false;
			track[x].pv = 0;
		}

		/* specific to file-type init */
		pos = 0;
		int i = (int)getnext(1);
		switch (type)
		{
			case FILE_LUCAS:
				getnext(24); // skip junk and get to the midi.
				adlib_style = LUCAS_STYLE | MIDI_STYLE;
				// note: no break, we go right into midi headers...
			case FILE_MIDI:
				if (type != FILE_LUCAS) tins = 128;
				getnext(11); /* skip header */
				deltas = (int)getnext(2);
				getnext(4);

				track[0].on = true;
				track[0].tend = getnext(4);
				track[0].spos = pos;
				break;
			case FILE_CMF:
				getnext(3); // ctmf
				getnexti(2); // version
				int n = (int)getnexti(2); // instrument offset
				int m = (int)getnexti(2); // music offset
				deltas = (int)getnexti(2); // ticks/qtr note
				i = (int)getnexti(2); // stuff in cmf is click ticks per second..
				if (i != 0) msqtr = 1000000L / i * deltas;

				i = (int)getnexti(2);
				if (i > 0 && i < flen) title = Helpers.retrieveAsString(data, i, (int) (flen - i));

				i = (int)getnexti(2);
				if (i > 0 && i < flen) author = Helpers.retrieveAsString(data, i, (int) (flen - i));

				i = (int)getnexti(2);
				if (i > 0 && i < flen) remarks = Helpers.retrieveAsString(data, i, (int) (flen - i));

				getnext(16); // channel in use table ..
				i = (int)getnexti(2); // num instr
				if (i > 128) i = 128; // to ward of bad numbers...
				getnexti(2); // basic tempo

				pos = n; // jump to instruments
				tins = i;
				for (int j = 0; j < i; j++)
				{
					for (int l = 0; l < 16; l++)
					{
						myinsbank[j][l] = (int) getnext(1);
					}
				}

				for (int x = 0; x < 16; x++)
					ch[x].nshift = -13;

				adlib_style = CMF_STYLE;

				track[0].on = true;
				track[0].tend = flen; // music until the end of the file
				track[0].spos = m; // jump to midi music
				break;
			case FILE_OLDLUCAS:
				msqtr = 250000;
				pos = 9;
				deltas = (int)getnext(1);

				i = 8;
				pos = 0x19; // jump to instruments
				tins = i;
				final int ins[] = new int[16];
				for (int j = 0; j < i; j++)
				{
					for (int l = 0; l < 16; l++)
						ins[l] = (int)getnext(1);

					myinsbank[j][10] = ins[2];
					myinsbank[j][ 0] = ins[3];
					myinsbank[j][ 2] = ins[4];
					myinsbank[j][ 4] = ins[5];
					myinsbank[j][ 6] = ins[6];
					myinsbank[j][ 8] = ins[7];
					myinsbank[j][ 1] = ins[8];
					myinsbank[j][ 3] = ins[9];
					myinsbank[j][ 5] = ins[10];
					myinsbank[j][ 7] = ins[11];
					myinsbank[j][ 9] = ins[12];

				}

				for (int x = 0; x < 16; x++)
				{
					if (x < tins)
					{
						ch[x].inum = x;
						for (int y = 0; y < 11; y++)
							ch[x].ins[y] = myinsbank[ch[x].inum][y];
					}
				}

				adlib_style = LUCAS_STYLE | MIDI_STYLE;

				track[0].on = true;
				track[0].tend = flen; // music until the end of the file
				track[0].spos = 0x98; // jump to midi music
				break;
			case FILE_ADVSIERRA:
				copyInsBanks();
				tins = stins;
				deltas = 0x20;
				getnext(11); // worthless empty space and "stuff" :)

				long o_sierra_pos = sierra_pos = pos;
				sierra_next_section();
				while (datalook(sierra_pos - 2) != 0xff && pos < flen)
				{
					sierra_next_section();
					subsongs++;
				}

				if (subsong < 0 || subsong >= subsongs) subsong = 0;

				sierra_pos = o_sierra_pos;
				sierra_next_section();
				i = 0;
				while (i != subsong)
				{
					sierra_next_section();
					i++;
				}

				adlib_style = SIERRA_STYLE | MIDI_STYLE; // advanced sierra tunes use volume
				break;
			case FILE_SIERRA:
				copyInsBanks();
				tins = stins;
				getnext(2);
				deltas = 0x20;

				track[0].on = true;
				track[0].tend = flen; // music until the end of the file

				for (int x = 0; x < 16; x++)
				{
					ch[x].nshift = -13;
					ch[x].on = (int)getnext(1)!=0;
					ch[x].inum = (int)getnext(1) & 0x7f;
					for (int y = 0; y < 11; y++)
						ch[x].ins[y] = myinsbank[ch[x].inum][y];
				}

				track[0].spos = pos;
				adlib_style = SIERRA_STYLE | MIDI_STYLE;
				break;
		}

		for (int x = 0; x < 16; x++)
		{
			if (track[x].on)
			{
				track[x].pos = track[x].spos;
				track[x].pv = 0;
				track[x].iwait = 0;
			}
		}

		firstRound = true;
		midi_fm_reset(opl);
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.opl3.sequencer.OPL3Sequence#getRefresh()
	 */
	@Override
	public double getRefresh()
	{
	    return (fwait > 0.01d ? fwait : 0.01d);
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.opl3.sequencer.OPL3Sequence#getSongName()
	 */
	@Override
	public String getSongName()
	{
		if (title!=null && title.length()!=0)
			return title;
		else
			return MultimediaContainerManager.getSongNameFromURL(url);
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.opl3.sequencer.OPL3Sequence#getAuthor()
	 */
	@Override
	public String getAuthor()
	{
		if (author!=null && author.length()!=0)
			return author;
		else
			return Helpers.EMPTY_STING;
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.opl3.sequencer.OPL3Sequence#getDescription()
	 */
	@Override
	public String getDescription()
	{
		if (remarks!=null && remarks.length()>0)
			return remarks;
		else
			return Helpers.EMPTY_STING;
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.opl3.sequencer.OPL3Sequence#getTypeName()
	 */
	@Override
	public String getTypeName()
	{
		switch (type)
		{
			case FILE_LUCAS:
				return "LucasArts AdLib MIDI";
			case FILE_MIDI:
				return "General MIDI";
			case FILE_CMF:
				return "Creative Music Format (CMF MIDI)";
			case FILE_SIERRA:
				return "Sierra On-Line EGA MIDI";
			case FILE_ADVSIERRA:
				return "Sierra On-Line VGA MIDI";
			case FILE_OLDLUCAS:
				return "Lucasfilm Adlib MIDI";
		}
		return "MIDI unknown";
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.opl3.sequencer.OPL3Sequence#getOPLType()
	 */
	@Override
	public oplType getOPLType()
	{
		return EmuOPL.oplType.OPL2;
	}
	/**
	 * @param url
	 * @see de.quippy.javamod.multimedia.opl3.sequencer.OPL3Sequence#setURL(java.net.URL)
	 */
	@Override
	public void setURL(URL url)
	{
		this.url = url;
	}
}
