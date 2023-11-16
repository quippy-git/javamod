/*
 * @(#) PatternRow.java
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

/**
 * @author Daniel Becker
 * @since 28.04.2006
 */
public class PatternRow
{
	private PatternElement [] patternElements;
	private boolean rowPlayed;
	
	/**
	 * Constructor for PatternRow
	 */
	public PatternRow(int channels)
	{
		super();
		patternElements = new PatternElement[channels];
		resetRowPlayed();
	}
	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		for (int channel=0; channel<patternElements.length; channel++)
			sb.append(patternElements[channel].toString()).append(" |");
		//sb.append(Boolean.toString(rowPlayed)).append(" | ");
		return sb.toString();
	}
	/**
	 * @since 11.11.2023
	 * @return the amount of characters for representing this pattern row
	 */
	public int getPatternRowCharacterLength()
	{
		if (patternElements!=null && patternElements.length>0) 
			return patternElements.length * 16;
		else
			return 0;
	}
	/**
	 * @since 23.08.2008
	 */
	public void resetRowPlayed()
	{
		rowPlayed = false;
	}
	/**
	 * @since 23.08.2008
	 */
	public void setRowPlayed()
	{
		rowPlayed = true;
	}
	/**
	 * @since 23.08.2008
	 * @return
	 */
	public boolean isRowPlayed()
	{
		return rowPlayed;
	}
	/**
	 * @return Returns the patternElements.
	 */
	public PatternElement[] getPatternElements()
	{
		return patternElements;
	}
	/**
	 * @return Returns the patternElements.
	 */
	public PatternElement getPatternElement(int channel)
	{
		return patternElements[channel];
	}
	/**
	 * @param patternElements The patternElements to set.
	 */
	public void setPatternElement(PatternElement[] patternElement)
	{
		this.patternElements = patternElement;
	}
	/**
	 * @param patternElements The patternElements to set.
	 */
	public void setPatternElement(int channel, PatternElement patternElement)
	{
		this.patternElements[channel] = patternElement;
	}
}
