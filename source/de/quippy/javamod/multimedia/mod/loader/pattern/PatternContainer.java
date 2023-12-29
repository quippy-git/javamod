/*
 * @(#) PatternContainer.java
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
public class PatternContainer
{
	private Pattern [] pattern;
	/**
	 * Constructor for PatternContainer
	 */
	public PatternContainer(int anzPattern)
	{
		super();
		pattern = new Pattern[anzPattern];
	}
	public PatternContainer(int anzPattern, int row)
	{
		this(anzPattern);
		for (int i=0; i<anzPattern; i++) pattern[i] = new Pattern(row);
	}
	public PatternContainer(final int anzPattern, final int rows, final int channels)
	{
		this(anzPattern);
		for (int i=0; i<anzPattern; i++) pattern[i] = new Pattern(rows, channels);
	}
	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<pattern.length; i++)
		{
			sb.append(i).append(". Pattern:\n");
			if (pattern[i]!=null) sb.append(pattern[i].toString());
			sb.append('\n');
		}
		return sb.toString();
	}
	/**
	 * Set this PatternContainer to have Patterns with nChannels channels afterwards
	 * @since 24.11.2023
	 * @param nChannels
	 */
	public void setToChannels(final int nChannels)
	{
		for (int i=0; i<pattern.length; i++)
		{
			if (pattern[i]!=null) pattern[i].setToChannels(i, nChannels);
		}
	}
	public int getChannels()
	{
		return (pattern!=null && pattern.length>0 && pattern[0]!=null)?pattern[0].getChannels():0; 
	}
	/**
	 * @since 23.08.2008
	 */
	public void resetRowsPlayed()
	{
		for (int i=0; i<pattern.length; i++)
			if (pattern[i]!=null) pattern[i].resetRowsPlayed();
	}
	/**
	 * @return Returns the pattern.
	 */
	public Pattern[] getPattern()
	{
		return pattern;
	}
	/**
	 * @return Returns the pattern.
	 */
	public Pattern getPattern(int patternIndex)
	{
		return pattern[patternIndex];
	}
	/**
	 * @return Returns the pattern.
	 */
	public PatternRow getPatternRow(int patternIndex, int row)
	{
		return (pattern[patternIndex]!=null)?pattern[patternIndex].getPatternRow(row):null;
	}
	/**
	 * @return Returns the pattern.
	 */
	public PatternElement getPatternElement(int patternIndex, int row, int channel)
	{
		return (pattern[patternIndex]!=null)?pattern[patternIndex].getPatternElement(row, channel):null;
	}
	/**
	 * @param pattern The pattern to set.
	 */
	public void setPatterns(Pattern[] newPatterns)
	{
		pattern = newPatterns;
	}
	/**
	 * @param pattern The pattern to set.
	 */
	public void setPattern(int patternIndex, Pattern newPattern)
	{
		pattern[patternIndex] = newPattern;
	}
	/**
	 * @param pattern The pattern to set.
	 */
	public void setPatternRow(int patternIndex, int row, PatternRow patternRow)
	{
		pattern[patternIndex].setPatternRow(row, patternRow);
	}
	/**
	 * @param pattern The pattern to set.
	 */
	public void setPatternElement(int patternIndex, int row, int channel, PatternElement patternElement)
	{
		pattern[patternIndex].setPatternElement(row, channel, patternElement);
	}
	/**
	 * @param pattern The pattern to set.
	 */
	public void setPatternElement(PatternElement patternElement)
	{
		pattern[patternElement.getPatternIndex()].setPatternElement(patternElement.getRow(), patternElement.getChannel(), patternElement);
	}
}
