/*
 * @(#) Pattern.java
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

/**
 * @author Daniel Becker
 * @since 28.04.2006
 */
public class Pattern
{
	private PatternRow [] patternRows;
	public static int LINEINDEX_LENGTH = 4;
	public static int ROW_LENGTH = 16;
	
	/**
	 * Constructor for Pattern
	 */
	public Pattern(final int rows)
	{
		super();
		patternRows = new PatternRow[rows];
	}
	public Pattern(final int rows, final int channels)
	{
		this(rows);
		for (int i=0; i<rows; i++) patternRows[i] = new PatternRow(channels);
	}
	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return toString(true, false);
	}
	/**
	 * @since 27.11.2023
	 * @param withRowMarker: display row indices
	 * @param isIT true: display effects for IT, false: for XM
	 * @return
	 */
	public String toString(final boolean withRowMarker, final boolean isIT)
	{
		final StringBuilder sb = new StringBuilder();
		addToStringBuilder(sb, withRowMarker, isIT);
		return sb.toString();
	}
	/**
	 * @since 22.12.2023
	 * @param sb
	 * @param withRowMarker
	 * @param isIT
	 */
	public void addToStringBuilder(final StringBuilder sb, final boolean withRowMarker, final boolean isIT)
	{
		for (int row=0; row<patternRows.length; row++)
		{
			if (withRowMarker) sb.append(ModConstants.getAsHex(row, 2)).append(" |");
			if (patternRows[row]!=null) patternRows[row].addToStringBuilder(sb, isIT);
			sb.append('\n');
		}
	}
	/**
	 * @since 11.11.2023
	 * @return
	 */
	public int getPatternRowCharacterLength(final boolean withRowIndex)
	{
		final int length = getChannels()*ROW_LENGTH;
		return (withRowIndex)?LINEINDEX_LENGTH + length:length;
	}
	/**
	 * Set this Pattern to have nChannels channels afterwards
	 * @since 24.11.2023
	 * @param nChannels
	 */
	public void setToChannels(final int patternIndex, final int nChannels)
	{
		for (int row=0; row<patternRows.length; row++)
		{
			if (patternRows[row]==null) patternRows[row] = new PatternRow(nChannels);
			patternRows[row].setToChannels(patternIndex, row, nChannels);
		}
	}
	/**
	 * @since 23.08.2008
	 * @return
	 */
	public int getRowCount()
	{
		return patternRows.length;
	}
	/**
	 * Retrieves the amount of channels this pattern represents
	 * @since 27.11.2023
	 * @return
	 */
	public int getChannels()
	{
		return (patternRows!=null && patternRows.length>0 && patternRows[0]!=null)?patternRows[0].getChannels():0;
	}
	/**
	 * @since 23.08.2008
	 */
	public void resetRowsPlayed()
	{
		for (int i=0; i<patternRows.length; i++)
		{
			PatternRow row = patternRows[i];
			if (row!=null) row.resetRowPlayed();
		}
	}
	/**
	 * @return Returns the patternRows.
	 */
	public PatternRow[] getPatternRows()
	{
		return patternRows;
	}
	/**
	 * @return Returns the patternRows.
	 */
	public PatternRow getPatternRow(int row)
	{
		return patternRows[row];
	}
	/**
	 * @return Returns the patternElement.
	 */
	public PatternElement getPatternElement(int row, int channel)
	{
		return patternRows[row].getPatternElement(channel);
	}
	/**
	 * @param patternRows The patternRows to set.
	 */
	public void setPatternRow(PatternRow[] patternRow)
	{
		this.patternRows = patternRow;
	}
	/**
	 * @param patternRows The patternRows to set.
	 */
	public void setPatternRow(int row, PatternRow patternRow)
	{
		this.patternRows[row] = patternRow;
	}
	/**
	 * @param patternElement The patternElement to set.
	 */
	public void setPatternElement(int row, int channel, PatternElement patternElement)
	{
		this.patternRows[row].setPatternElement(channel, patternElement);
	}
}
