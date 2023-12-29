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
	public PatternRow(final int channels)
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
		return toString(false);
	}
	/**
	 * toString - however regarding if ImpulseTracker family or not
	 * @since 22.12.2023
	 * @param isIT
	 * @return
	 */
	public String toString(final boolean isIT)
	{
		final StringBuilder sb = new StringBuilder();
		addToStringBuilder(sb, isIT);
		return sb.toString();
	}
	/**
	 * Add to patternRow string representation to a StringBuilder
	 * @since 22.12.2023
	 * @param sb
	 * @param isIT
	 */
	public void addToStringBuilder(final StringBuilder sb, final boolean isIT)
	{
		for (int channel=0; channel<patternElements.length; channel++)
		{
			if (patternElements[channel]!=null) patternElements[channel].addToStringBuilder(sb, isIT);
			sb.append(" |");
		}
	}
	/**
	 * Set this row to have nChannels channels
	 * @since 24.11.2023
	 * @param nChannels
	 */
	public void setToChannels(final int patternIndex, final int row, final int nChannels)
	{
		final PatternElement[] newPatternElements = new PatternElement[nChannels];
		for (int channel=0; channel<nChannels; channel++)
		{
			if (channel < patternElements.length && patternElements[channel]!=null) 
				newPatternElements[channel] = patternElements[channel];
			else
				newPatternElements[channel] = new PatternElement(patternIndex, row, channel);
		}
		patternElements = newPatternElements;
	}
	/**
	 * @since 27.11.2023
	 * @return
	 */
	public int getChannels()
	{
		return (patternElements!=null)?patternElements.length:0;
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
