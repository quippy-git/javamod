/*
 * @(#) PatternElementIT.java
 *
 * Created on 09.01.2024 by Daniel Becker
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
 */
package de.quippy.javamod.multimedia.mod.loader.pattern;

import de.quippy.javamod.multimedia.mod.ModConstants;
import de.quippy.javamod.multimedia.mod.loader.Module;
import de.quippy.javamod.system.Helpers;

/**
 * @author Daniel Becker
 * @since 09.01.2024
 */
public class PatternElementIT extends PatternElement
{
	/**
	 * Constructor for PatternElementIT
	 * @param parentMod
	 * @param patternIndex
	 * @param patternRow
	 * @param channel
	 */
	public PatternElementIT(final Module parentMod, final PatternRow parentPatternRow, final int patternIndex, final int patternRow, final int channel)
	{
		super(parentMod, parentPatternRow, patternIndex, patternRow, channel);
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.mod.loader.pattern.PatternElement#getEffektChar()
	 */
	@Override
	public char getEffektChar()
	{
		return (effekt==0x1B)?'#':(effekt==0 && effektOp!=0)?'.':(char)('A' + effekt - 1);
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.mod.loader.pattern.PatternElement#getEffectName()
	 */
	@Override
	public String getEffectName()
	{
		switch (effekt)
		{
			case 0x00: return Helpers.EMPTY_STING;
			case 0x01: return "Set Speed";
			case 0x02: return "Pattern Position Jump";
			case 0x03: return "Pattern break";
			case 0x04: return "Volume Slide";
			case 0x05: return "Porta Down";
			case 0x06: return "Porta Up";
			case 0x07: return "Porta To Note";
			case 0x08: return "Vibrato";
			case 0x09: return "Tremor";
			case 0x0A: return "Arpeggio";
			case 0x0B: return "Vibrato + VolSlide";
			case 0x0C: return "PortaNote + VolSlide";
			case 0x0D: return "Set Volume";
			case 0x0E: return "Volume Slide";
			case 0x0F: return "Sample Offset";
			case 0x10: return "Panning Slide";
			case 0x11: return "(Multi) Retrig Note";
			case 0x12: return "Tremolo";
			case 0x13 : 		// Extended
				final int effektOpEx = effektOp&0x0F;
				switch (effektOp>>4)
				{
					case 0x1: return "Glissando";
					case 0x2: return "Set FineTune";
					case 0x3: return "Set Vibrato Type";
					case 0x4: return "Set Tremolo Type";
					case 0x5: return "Set Panbrello Type";
					case 0x6: return "Pattern Delay Frame";
					case 0x7:	// set NNA and others
						switch (effektOpEx)
						{
							case 0x0: return "Note Cut all NNAs";
							case 0x1: return "Note Off all NNAs";
							case 0x2: return "Note Fade all NNAs";
							case 0x3: return "NNA Cut";
							case 0x4: return "NNA Continue";
							case 0x5: return "NNA Off";
							case 0x6: return "NNA Fade";
							case 0x7: return "Volume Envelope Off";
							case 0x8: return "Volume Envelope On";
							case 0x9: return "Panning Envelope Off";
							case 0xA: return "Panning Envelope On";
							case 0xB: return "Pitch Envelope Off";
							case 0xC: return "Pitch Envelope On";
						}
						break;
					case 0x8: return "Set Fine Panning";
					case 0x9:	// Sound Control
						switch (effektOpEx)
						{
							case 0x0: return "No Surround";
							case 0x1: return "Enabl. Surround";
							// MPT Effects only
//							case 0x8: // Disable reverb for this channel
//								break;
//							case 0x9: // Force reverb for this channel
//								break;
//							case 0xA: // Select mono surround mode (center channel). This is the default
//								break;
//							case 0xB: // Select quad surround mode: this allows you to pan in the rear channels, especially useful for 4-speakers playback. Note that S9A and S9B do not activate the surround for the current channel, it is a global setting that will affect the behavior of the surround for all channels. You can enable or disable the surround for individual channels by using the S90 and S91 effects. In quad surround mode, the channel surround will stay active until explicitely disabled by a S90 effect
//								break;
							case 0xC: return "Global FilterMode Off";
							case 0xD: return "Global FilterMode On";
							case 0xE: return "Play Forward";
							case 0xF: return "Play Backwards";
						}
						break;
					case 0xA: return "Set High Offset";
					case 0xB: if (effektOpEx==0) return "Jump Loop Set"; else return "Jump Loop";
					case 0xC: return "Note Cut";
					case 0xD: return "Note Delay";
					case 0xE: return "Pattern Delay";
					case 0xF: if ((parentMod.getModType()&ModConstants.MODTYPE_IT)!=0) return "Set Active Macro"; else return "Funk Repeat";
				}
				break;
			case 0x14: return 
				(effektOp>>4==0)? 	// 0x0X
					"Set BPM (slower)":
				(effektOp>>4==1)? 	// 0x1X
					"Set BPM (faster)":
					"Set BPM";		// else
			case 0x15: return "Fine Vibrato";
			case 0x16: return "Set Global Volume";
			case 0x17: return "Global Volume Slide";
			case 0x18: return "Set Panning";
			case 0x19: return "Panbrello";
			case 0x1A: return "Midi Macro";
			case 0x1B: return "Parameter Extension";
			case 0x1C: return "Smooth Midi Macro";
		}
		//Log.error("Unknown: " + ModConstants.getAsHex(effekt, 2) + "/" + ModConstants.getAsHex(effektOp, 2));
		return Helpers.EMPTY_STING;
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.mod.loader.pattern.PatternElement#getEffectCategory()
	 */
	@Override
	public int getEffectCategory()
	{
		switch (effekt)
		{
			case 0x00: return (effektOp==0)?EFFECT_NORMAL:EFFECT_NONE;
			case 0x01: return EFFECT_GLOBAL;
			case 0x02: return EFFECT_GLOBAL;
			case 0x03: return EFFECT_GLOBAL;
			case 0x04: return EFFECT_VOLUME;
			case 0x05: return EFFECT_PITCH;
			case 0x06: return EFFECT_PITCH;
			case 0x07: return EFFECT_PITCH;
			case 0x08: return EFFECT_PITCH;
			case 0x09: return EFFECT_VOLUME;
			case 0x0A: return EFFECT_NORMAL;
			case 0x0B: return EFFECT_VOLUME;
			case 0x0C: return EFFECT_VOLUME;
			case 0x0D: return EFFECT_VOLUME;
			case 0x0E: return EFFECT_VOLUME;
			case 0x0F: return EFFECT_NORMAL;
			case 0x10: return EFFECT_PANNING;
			case 0x11: return EFFECT_NORMAL;
			case 0x12: return EFFECT_VOLUME;
			case 0x13 : 		// Extended
				final int effektOpEx = effektOp&0x0F;
				switch (effektOp>>4)
				{
					case 0x1: return EFFECT_PITCH;
					case 0x2: return EFFECT_PITCH;
					case 0x3: return EFFECT_PITCH;
					case 0x4: return EFFECT_VOLUME;
					case 0x5: return EFFECT_PANNING;
					case 0x6: return EFFECT_GLOBAL;
					case 0x7:	// set NNA and others
						switch (effektOpEx)
						{
							case 0x0: return EFFECT_NORMAL;
							case 0x1: return EFFECT_NORMAL;
							case 0x2: return EFFECT_NORMAL;
							case 0x3: return EFFECT_NORMAL;
							case 0x4: return EFFECT_NORMAL;
							case 0x5: return EFFECT_NORMAL;
							case 0x6: return EFFECT_NORMAL;
							case 0x7: return EFFECT_VOLUME;
							case 0x8: return EFFECT_VOLUME;
							case 0x9: return EFFECT_PANNING;
							case 0xA: return EFFECT_PANNING;
							case 0xB: return EFFECT_PITCH;
							case 0xC: return EFFECT_PITCH;
						}
						break;
					case 0x8: return EFFECT_PANNING;
					case 0x9:	// Sound Control
						switch (effektOpEx)
						{
							case 0x0: return EFFECT_PANNING;
							case 0x1: return EFFECT_PANNING;
							// MPT Effects only
//							case 0x8: // Disable reverb for this channel
//								break;
//							case 0x9: // Force reverb for this channel
//								break;
//							case 0xA: // Select mono surround mode (center channel). This is the default
//								break;
//							case 0xB: // Select quad surround mode: this allows you to pan in the rear channels, especially useful for 4-speakers playback. Note that S9A and S9B do not activate the surround for the current channel, it is a global setting that will affect the behavior of the surround for all channels. You can enable or disable the surround for individual channels by using the S90 and S91 effects. In quad surround mode, the channel surround will stay active until explicitely disabled by a S90 effect
//								break;
							case 0xC: return EFFECT_GLOBAL;
							case 0xD: return EFFECT_GLOBAL;
							case 0xE: return EFFECT_NORMAL;
							case 0xF: return EFFECT_NORMAL;
						}
						break;
					case 0xA: return EFFECT_NORMAL;
					case 0xB: return EFFECT_GLOBAL;
					case 0xC: return EFFECT_NORMAL;
					case 0xD: return EFFECT_NORMAL;
					case 0xE: return EFFECT_GLOBAL;
					case 0xF: return EFFECT_NORMAL;
				}
				break;
			case 0x14: return EFFECT_GLOBAL;
			case 0x15: return EFFECT_PITCH;
			case 0x16: return EFFECT_GLOBAL;
			case 0x17: return EFFECT_GLOBAL;
			case 0x18: return EFFECT_PANNING;
			case 0x19: return EFFECT_PANNING;
			case 0x1A: return EFFECT_NORMAL;
			case 0x1B: return EFFECT_NORMAL;
			case 0x1C: return EFFECT_NORMAL;
		}
		return EFFECT_UNKNOWN;
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.mod.loader.pattern.PatternElement#getVolumeColumEffektChar()
	 */
	@Override
	public char getVolumeColumEffektChar()
	{
		switch (volumeEffekt)
		{
			case 0x01: return 'v';
			case 0x02: return 'd';
			case 0x03: return 'c';
			case 0x04: return 'b';
			case 0x05: return 'a';
			case 0x06: return 'u';
			case 0x07: return 'h';
			case 0x08: return 'p';
			case 0x09: return 'l';
			case 0x0A: return 'r';
			case 0x0B: return 'g';
			case 0x0C: return 'e';
			case 0x0D: return 'f';
			case 0x0E: return 'o'; // MPT Specific, not supported
		}
		return '?';
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.mod.loader.pattern.PatternElement#getVolEffectName()
	 */
	@Override
	public String getVolEffectName()
	{
		switch (volumeEffekt)
		{
			case 0x00: return Helpers.EMPTY_STING; 
			case 0x01: return "Set Volume";
			case 0x02: return "Volslide down";
			case 0x03: return "Volslide up";
			case 0x04: return "Fine Volslide Down";
			case 0x05: return "Fine Volslide Up";
			case 0x06: return "Set Vibrato Speed"; // normally not existent with IT
			case 0x07: if (volumeEffektOp!=0) return "Set Vibrato Depth"; else return "Vibrato";
			case 0x08: return "Set Panning";
			case 0x09: return "Panning Slide Left";
			case 0x0A: return "Panning Slide Right";
			case 0x0B: return "Porta To Note";
			case 0x0C: return "Porta Down";
			case 0x0D: return "Porta Up";
			case 0x0E: return "Sample cues";
		}
		//Log.error("Unknown: " + ModConstants.getAsHex(assignedVolumeEffekt, 2) + "/" + ModConstants.getAsHex(assignedVolumeEffektOp, 2));
		return Helpers.EMPTY_STING;
	}
	/**
	 * @return
	 * @see de.quippy.javamod.multimedia.mod.loader.pattern.PatternElement#getVolEffectCategory()
	 */
	@Override
	public int getVolEffectCategory()
	{
		switch (volumeEffekt)
		{
			case 0x00: return EFFECT_NORMAL; 
			case 0x01: return EFFECT_VOLUME;
			case 0x02: return EFFECT_VOLUME;
			case 0x03: return EFFECT_VOLUME;
			case 0x04: return EFFECT_VOLUME;
			case 0x05: return EFFECT_VOLUME;
			case 0x06: return EFFECT_UNKNOWN; // normally not existent with IT
			case 0x07: return EFFECT_PITCH; 
			case 0x08: return EFFECT_PANNING;
			case 0x09: return EFFECT_PANNING;
			case 0x0A: return EFFECT_PANNING;
			case 0x0B: return EFFECT_PITCH;
			case 0x0C: return EFFECT_PITCH;
			case 0x0D: return EFFECT_PITCH;
			case 0x0E: return EFFECT_UNKNOWN;
		}
		return EFFECT_UNKNOWN;
	}
}
