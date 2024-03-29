/*
 * @(#) PatternElementXM.java
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
public class PatternElementXM extends PatternElement
{
	/**
	 * Constructor for PatternElementXM
	 * @param parentMod
	 * @param patternIndex
	 * @param patternRow
	 * @param channel
	 */
	public PatternElementXM(final Module parentMod, final PatternRow parentPatternRow, final int patternIndex, final int patternRow, int channel)
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
		if (effekt <= 0x0F)
			return ModConstants.numbers[effekt];
		else if (effekt == 0x24)
			return '\\';
		else if (effekt == 0x26)
			return '#';
		else
			return (char) ('G' + effekt - 0x10);
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
			case 0x00: return (effektOp==0)?Helpers.EMPTY_STING:"Arpeggio";
			case 0x01: return "Porta Up";
			case 0x02: return "Porta Down";
			case 0x03: return "Porta To Note";
			case 0x04: return "Vibrato";
			case 0x05: return "PortaNote + VolSlide";
			case 0x06: return "Vibrato + VolSlide";
			case 0x07: return "Tremolo";
			case 0x08: return "Set Panning";
			case 0x09: return "Sample Offset";
			case 0x0A: return "Volume Slide";
			case 0x0B: return "Pattern Position Jump";
			case 0x0C: return "Set volume";
			case 0x0D: return "Pattern break";
			case 0x0E:
				final int effektOpEx = effektOp&0x0F;
				switch (effektOp>>4)
				{
					case 0x0: return "Set filter";
					case 0x1: return "Fine Porta Up";
					case 0x2: return "Fine Porta Down";
					case 0x3: return "Glissando";
					case 0x4: return "Set Vibrato Type";
					case 0x5: return "Set FineTune";
					case 0x6: if (effektOpEx==0) return "Jump Loop Set"; else return "Jump Loop";
					case 0x7: return "Set Tremolo Type";
					case 0x8: return ((parentMod.getModType()&ModConstants.MODTYPE_MOD)!=0)?"Karplus Strong":"Set Fine Panning";
					case 0x9: return "Retrig Note";
					case 0xA: return "Fine Volume Up";
					case 0xB: return "Fine Volume Down";
					case 0xC: return "Note Cut";
					case 0xD: return "Note Delay";
					case 0xE: return "Pattern Delay";
					case 0xF: return ((parentMod.getModType()&ModConstants.MODTYPE_XM)!=0)?"Set MIDI Macro":"Funk It!";
				}
				break;
			case 0x0F: return (effektOp>31 && !parentMod.getModSpeedIsTicks())?"Set BPM":"Set Speed"; 
			case 0x10: return "Set global volume";
			case 0x11: return "Global Volume Slide";
			case 0x14: return "Key off";
			case 0x15: return "Set Envelope Position";
			case 0x19: return "Panning Slide";
			case 0x1B: return "Retrig Note + VolSlide";
			case 0x1D: return "Tremor";
			case 0x20: return "Empty";
			case 0x21: // Extended XM Effects
				switch (effektOp>>4)
				{
					case 0x1: return "Extra Fine Porta Up";
					case 0x2: return "Extra Fine Porta Down";
					case 0x5: return "set Panbrello Waveform";
					case 0x6: return "Fine Pattern Delay";
					case 0x9: // Sound Control
						switch (effektOp&0x0F)
						{
							case 0x0: return "No Surround";
							case 0x1: return "Enable Surround";
							// MPT Effects only
							case 0x8: return "No Reverb";
							case 0x9: return "Enable Reverb";
							case 0xA: return "Mono Surround";
							case 0xB: return "Quad Surround";
							// ----------------
							case 0xC: return "Global FilterMode Off";
							case 0xD: return "Global FilterMode On";
							case 0xE: return "Play Forward";
							case 0xF: return "Play Backwards";
						}
						break;
					case 0xA: return "Set High Offset";
				}
				break;
			case 0x22: return "Panbrello"; 
			case 0x23: return "Midi Macro";
			case 0x24: return "Smooth Midi Macro";
			case 0x26: return "Parameter Extension";
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
			case 0x00: return (effektOp==0)?EFFECT_NORMAL:EFFECT_PITCH;
			case 0x01: return EFFECT_PITCH;
			case 0x02: return EFFECT_PITCH;
			case 0x03: return EFFECT_PITCH;
			case 0x04: return EFFECT_PITCH;
			case 0x05: return EFFECT_VOLUME;
			case 0x06: return EFFECT_VOLUME;
			case 0x07: return EFFECT_VOLUME;
			case 0x08: return EFFECT_PANNING;
			case 0x09: return EFFECT_NORMAL;
			case 0x0A: return EFFECT_VOLUME;
			case 0x0B: return EFFECT_GLOBAL;
			case 0x0C: return EFFECT_VOLUME;
			case 0x0D: return EFFECT_GLOBAL;
			case 0x0E:
				switch (effektOp>>4)
				{
					case 0x0: return EFFECT_PITCH;
					case 0x1: return EFFECT_PITCH;
					case 0x2: return EFFECT_PITCH;
					case 0x3: return EFFECT_PITCH;
					case 0x4: return EFFECT_PITCH;
					case 0x5: return EFFECT_PITCH;
					case 0x6: return EFFECT_GLOBAL;
					case 0x7: return EFFECT_VOLUME;
					case 0x8: return ((parentMod.getModType()&ModConstants.MODTYPE_MOD)!=0)?((ModConstants.SUPPORT_E8x_EFFECT)?EFFECT_UNKNOWN:EFFECT_PITCH):EFFECT_PANNING;
					case 0x9: return EFFECT_NORMAL;
					case 0xA: return EFFECT_VOLUME;
					case 0xB: return EFFECT_VOLUME;
					case 0xC: return EFFECT_NORMAL;
					case 0xD: return EFFECT_NORMAL;
					case 0xE: return EFFECT_GLOBAL;
					case 0xF: return EFFECT_NORMAL;
				}
				break;
			case 0x0F: return EFFECT_GLOBAL; 
			case 0x10: return EFFECT_GLOBAL;
			case 0x11: return EFFECT_GLOBAL;
			case 0x14: return EFFECT_NORMAL;
			case 0x15: return EFFECT_NORMAL;
			case 0x19: return EFFECT_PANNING;
			case 0x1B: return EFFECT_NORMAL;
			case 0x1D: return EFFECT_VOLUME;
			case 0x20: return EFFECT_UNKNOWN;
			case 0x21: // Extended XM Effects
				switch (effektOp>>4)
				{
					case 0x1: return EFFECT_PITCH;
					case 0x2: return EFFECT_PITCH;
					case 0x5: return EFFECT_PANNING;
					case 0x6: return EFFECT_GLOBAL;
					case 0x9: // Sound Control
						switch (effektOp&0x0F)
						{
							case 0x0: return EFFECT_PANNING;
							case 0x1: return EFFECT_PANNING;
							case 0x8: return EFFECT_PITCH;
							case 0x9: return EFFECT_PITCH;
							case 0xA: return EFFECT_PANNING;
							case 0xB: return EFFECT_PANNING;
							case 0xC: return EFFECT_GLOBAL;
							case 0xD: return EFFECT_GLOBAL;
							case 0xE: return EFFECT_NORMAL;
							case 0xF: return EFFECT_NORMAL;
						}
						break;
					case 0xA: return EFFECT_NORMAL;
				}
				break;
			case 0x22: return EFFECT_PANNING; 
			case 0x23: return EFFECT_NORMAL;
			case 0x24: return EFFECT_NORMAL;
			case 0x26: return EFFECT_NORMAL;
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
//			case 0x0C: return 'e';
//			case 0x0D: return 'f';
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
			case 0x06: return "Set Vibrato Speed";
			case 0x07: if (volumeEffektOp!=0) return "Set Vibrato Depth"; else return "Vibrato";
			case 0x08: return "Set Panning";
			case 0x09: return "Panning Slide Left";
			case 0x0A: return "Panning Slide Right";
			case 0x0B: return "Porta To Note";
//			case 0x0C: return "Porta Down";
//			case 0x0D: return "Porta Up";
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
			case 0x06: return EFFECT_PITCH;
			case 0x07: return EFFECT_PITCH;
			case 0x08: return EFFECT_PANNING;
			case 0x09: return EFFECT_PANNING;
			case 0x0A: return EFFECT_PANNING;
			case 0x0B: return EFFECT_PITCH;
//			case 0x0C: return EFFECT_UNKNOWN;
//			case 0x0D: return EFFECT_UNKNOWN;
		}
		return EFFECT_UNKNOWN;
	}
}
