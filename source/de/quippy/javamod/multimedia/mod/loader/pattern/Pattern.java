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
	/**
	 * Constructor for Pattern
	 */
	public Pattern(int rows)
	{
		super();
		patternRows = new PatternRow[rows];
	}
	public Pattern(int rows, int channels)
	{
		this(rows);
		for (int i=0; i<rows; i++) patternRows[i]= new PatternRow(channels);
	}
	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		for (int row=0; row<patternRows.length; row++)
			sb.append(ModConstants.getAsHex(row, 2)).append(" |").append(patternRows[row].toString()).append('\n');
		return sb.toString();
	}
	public int getPatternRowCharacterLength()
	{
		if (patternRows!=null && patternRows.length>0) return 4 + patternRows[0].getPatternRowCharacterLength(); else return 4;
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
