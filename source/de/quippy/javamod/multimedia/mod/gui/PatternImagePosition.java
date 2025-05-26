/*
 * @(#) PatternImagePosition.java
 *
 * Created on 21.01.2024 by Daniel Becker
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
package de.quippy.javamod.multimedia.mod.gui;

import de.quippy.javamod.multimedia.mod.loader.pattern.Pattern;

/**
 * @author Daniel Becker
 * @since 21.01.2024
 */
public class PatternImagePosition
{
	public static final int NOT_SET = -1;
	public static final int COLUMN_BEYOND_LEFT = 0;
	public static final int COLUMN_NOTE = 1;
	public static final int COLUMN_INSTRUMENT = 2;
	public static final int COLUMN_VOLEFFECT = 3;
	public static final int COLUMN_VOLEFFECT_OP = 4;
	public static final int COLUMN_EFFECT = 5;
	public static final int COLUMN_EFFECT_OP = 6;
	public static final int COLUMN_BEYOND_RIGHT = 7;

	public int channel, row, column, charInView, rowInView;
	public Pattern pattern;

	/**
	 * Constructor for PatternImagePosition
	 */
	public PatternImagePosition()
	{
		super();
		channel = row = column = charInView = rowInView = NOT_SET;
		pattern = null;
	}
	public PatternImagePosition(final Pattern newPattern, final int newRow)
	{
		this();
		pattern = newPattern;
		row = newRow;
	}
//	public PatternImagePosition(final Pattern newPattern, final int newRow, final int newChannel)
//	{
//		this(newPattern, newRow);
//		channel = newChannel;
//	}
//	public PatternImagePosition(final Pattern newPattern, final int newRow, final int newChannel, final int newColumn)
//	{
//		this(newPattern, newRow, newChannel);
//		column = newColumn;
//	}
	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		if (channel<0) return "Row indicator " + row;
		final StringBuilder sb = new StringBuilder("Channel:");
		sb.append(channel).append(" Column:");
		switch (column)
		{
			case COLUMN_BEYOND_LEFT: sb.append("Beyond Left"); break;
			case COLUMN_NOTE: sb.append("Note"); break;
			case COLUMN_INSTRUMENT: sb.append("Instrument"); break;
			case COLUMN_VOLEFFECT: sb.append("Volumn Effect"); break;
			case COLUMN_VOLEFFECT_OP: sb.append("Volume Effect OP"); break;
			case COLUMN_EFFECT: sb.append("Effect"); break;
			case COLUMN_EFFECT_OP: sb.append("Effect OP"); break;
			case COLUMN_BEYOND_RIGHT: sb.append("Beyond Right"); break;
			case NOT_SET:
			default: sb.append("NOT SET"); break;
		}
		sb.append(" Row:").append(row).append(" Row in View:").append(rowInView);
		return sb.toString();
	}
}
