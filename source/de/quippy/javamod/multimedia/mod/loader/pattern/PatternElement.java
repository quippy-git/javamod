/*
 * @(#) PatternElement.java
 * 
 * Created on 28.04.2006 by Daniel Becker
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
package de.quippy.javamod.multimedia.mod.loader.pattern;

import de.quippy.javamod.multimedia.mod.ModConstants;
import de.quippy.javamod.multimedia.mod.loader.Module;

/**
 * @author Daniel Becker
 * @since 28.04.2006
 */
public abstract class PatternElement
{
	public static final int EFFECT_NORMAL	= 0;
	public static final int EFFECT_VOLUME	= 1;
	public static final int EFFECT_PANNING	= 2;
	public static final int EFFECT_PITCH	= 3;
	public static final int EFFECT_GLOBAL	= 4;
	public static final int EFFECT_UNKNOWN	= 5;
	public static final int EFFECT_NONE		= 6;
	
	protected Module parentMod;
	protected PatternRow parentPatternRow;
	
	protected int patternIndex;
	protected int row;
	protected int channel;
	protected int period;
	protected int noteIndex;
	protected int instrument;
	protected int effekt;
	protected int effektOp;
	protected int volumeEffekt;
	protected int volumeEffektOp;
	
	/**
	 * Constructor for PatternElement
	 */
	public PatternElement(final Module parentMod, final PatternRow parentPatternRow, final int patternIndex, final int patternRow, final int channel)
	{
		super();
		this.patternIndex = patternIndex;
		this.row = patternRow;
		this.channel = channel;
		this.parentMod = parentMod;
		this.parentPatternRow = parentPatternRow;
		this.period = 0;
		this.noteIndex = 0;
		this.instrument = 0;
		this.volumeEffekt = 0;
		this.volumeEffektOp = 0;
		this.effekt = 0;
		this.effektOp = 0;
	}
	/**
	 * @since 09.01.2024
	 * @return the char representation of the current effect op
	 */
	public abstract char getEffektChar();
	/**
	 * @since 09.01.2024
	 * @return the name of the effect op
	 */
	public abstract String getEffectName();
	/**
	 * @since 09.01.2024
	 * @return a category for the effect op (see EFFECT_* constants)
	 */
	public abstract int getEffectCategory();
	/**
	 * @since 09.01.2024
	 * @return the char representation of the current volume effect op
	 */
	public abstract char getVolumeColumEffektChar();
	/**
	 * @since 09.01.2024
	 * @return the name of the volume effect op 
	 */
	public abstract String getVolEffectName();
	/**
	 * @since 09.01.2024
	 * @return a category for the volume effect op (see EFFECT_* constants)
	 */
	public abstract int getVolEffectCategory();
	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		addToStringBuilder(sb);
		return sb.toString();
	}
	/**
	 * Add patternElement string representation to a StringBuilder
	 * @since 22.12.2023
	 * @param sb
	 * @param isIT
	 */
	public void addToStringBuilder(final StringBuilder sb)
	{
		sb.append(ModConstants.getNoteNameForIndex(noteIndex)).append(' ');
		if (instrument==0) 
			sb.append("..");
		else 
			sb.append(ModConstants.getAsHex(instrument, 2)); 
		
		if (volumeEffekt==0)
			sb.append(" ..");
		else
		{
			sb.append(getVolumeColumEffektChar());
			sb.append(ModConstants.getAsHex(volumeEffektOp, 2));
		}
		
		sb.append(' ');
		if (effekt==0 && effektOp==0)
			sb.append("...");
		else
		{
			sb.append(getEffektChar());
			sb.append(ModConstants.getAsHex(effektOp, 2));
		}
	}
	/**
	 * @return the parentMod
	 */
	public Module getParentMod()
	{
		return parentMod;
	}
	/**
	 * @return the parentPatternRow
	 */
	public PatternRow getParentPatternRow()
	{
		return parentPatternRow;
	}
	/**
	 * @return Returns the channel.
	 */
	public int getChannel()
	{
		return channel;
	}
	/**
	 * @param channel The channel to set.
	 */
	public void setChannel(final int channel)
	{
		this.channel = channel;
	}
	/**
	 * @return Returns the effekt.
	 */
	public int getEffekt()
	{
		return effekt;
	}
	/**
	 * @param effekt The effekt to set.
	 */
	public void setEffekt(final int effekt)
	{
		this.effekt = effekt;
	}
	/**
	 * @return Returns the effektOp.
	 */
	public int getEffektOp()
	{
		return effektOp;
	}
	/**
	 * @param effektOp The effektOp to set.
	 */
	public void setEffektOp(final int effektOp)
	{
		this.effektOp = effektOp;
	}
	/**
	 * @return Returns the instrument.
	 */
	public int getInstrument()
	{
		return instrument;
	}
	/**
	 * @param instrument The instrument to set.
	 */
	public void setInstrument(final int instrument)
	{
		this.instrument = instrument;
	}
	/**
	 * @return Returns the noteIndex.
	 */
	public int getNoteIndex()
	{
		return noteIndex;
	}
	/**
	 * @param noteIndex The noteIndex to set.
	 */
	public void setNoteIndex(final int noteIndex)
	{
		this.noteIndex = noteIndex;
	}
	/**
	 * @return Returns the patternIndex.
	 */
	public int getPatternIndex()
	{
		return patternIndex;
	}
	/**
	 * @param patternIndex The patternIndex to set.
	 */
	public void setPatternIndex(final int patternIndex)
	{
		this.patternIndex = patternIndex;
	}
	/**
	 * @return Returns the period.
	 */
	public int getPeriod()
	{
		return period;
	}
	/**
	 * @param period The period to set.
	 */
	public void setPeriod(final int period)
	{
		this.period = period;
	}
	/**
	 * @return Returns the row.
	 */
	public int getRow()
	{
		return row;
	}
	/**
	 * @param row The row to set.
	 */
	public void setRow(final int row)
	{
		this.row = row;
	}
	/**
	 * @return Returns the volume.
	 */
	public int getVolumeEffekt()
	{
		return volumeEffekt;
	}
	/**
	 * @param volume The volume to set.
	 */
	public void setVolumeEffekt(final int volumeEffekt)
	{
		this.volumeEffekt = volumeEffekt;
	}
	/**
	 * @return Returns the assignedVolumeEffektOp.
	 */
	public int getVolumeEffektOp()
	{
		return volumeEffektOp;
	}
	/**
	 * @param assignedVolumeEffektOp The assignedVolumeEffektOp to set.
	 */
	public void setVolumeEffektOp(final int volumeEffektOp)
	{
		this.volumeEffektOp = volumeEffektOp;
	}
}
