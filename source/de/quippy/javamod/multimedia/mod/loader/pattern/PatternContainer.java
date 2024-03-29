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

import java.awt.Color;

import de.quippy.javamod.multimedia.mod.ModConstants;
import de.quippy.javamod.multimedia.mod.loader.Module;

/**
 * @author Daniel Becker
 * @since 28.04.2006
 */
public class PatternContainer
{
	protected Module parentMod;
	protected Pattern [] patterns;

	// MPTP specific information
	protected String [] channelNames;
	protected Color [] channelColors;

	/**
	 * Constructor for PatternContainer
	 */
	public PatternContainer(final Module newParentMod, final int anzPattern)
	{
		super();
		patterns = new Pattern[anzPattern];
		parentMod = newParentMod;
	}
	public PatternContainer(final Module parentMod, final int anzPattern, final int rows)
	{
		this(parentMod, anzPattern);
		for (int i=0; i<anzPattern; i++) createPattern(i, rows);
	}
	public PatternContainer(final Module parentMod, final int anzPattern, final int rows, final int channels)
	{
		this(parentMod, anzPattern);
		for (int i=0; i<anzPattern; i++) createPattern(i, rows, channels);
	}
	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<patterns.length; i++)
		{
			sb.append(i).append(". Pattern:\n");
			if (patterns[i]!=null) sb.append(patterns[i].toString());
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
		for (int i=0; i<patterns.length; i++)
		{
			if (patterns[i]!=null) patterns[i].setToChannels(i, nChannels);
		}
	}
	/**
	 * @return the parentMod
	 */
	public Module getParentMod()
	{
		return parentMod;
	}
	public int getChannels()
	{
		return (patterns!=null && patterns.length>0 && patterns[0]!=null)?patterns[0].getChannels():0; 
	}
	/**
	 * @since 23.08.2008
	 */
	public void resetRowsPlayed()
	{
		for (int i=0; i<patterns.length; i++)
			if (patterns[i]!=null) patterns[i].resetRowsPlayed();
	}
	/**
	 * @return Returns the patterns.
	 */
	public Pattern[] getPattern()
	{
		return patterns;
	}
	/**
	 * @return Returns the pattern.
	 */
	public Pattern getPattern(final int patternIndex)
	{
		return patterns[patternIndex];
	}
	/**
	 * @return Returns the pattern.
	 */
	public PatternRow getPatternRow(final int patternIndex, final int row)
	{
		return (patterns[patternIndex]!=null)?patterns[patternIndex].getPatternRow(row):null;
	}
	/**
	 * @return Returns the pattern.
	 */
	public PatternElement getPatternElement(final int patternIndex, final int row, int channel)
	{
		return (patterns[patternIndex]!=null)?patterns[patternIndex].getPatternElement(row, channel):null;
	}
	/**
	 * @param pattern The pattern to set.
	 */
	public void setPatterns(final Pattern[] newPatterns)
	{
		patterns = newPatterns;
	}
	/**
	 * @param patterns The patterns to set.
	 */
	public void setPattern(final int patternIndex, final Pattern newPattern)
	{
		patterns[patternIndex] = newPattern;
	}
	public Pattern createPattern(final int patternIndex, final int rows)
	{
		final Pattern newPattern = new Pattern(parentMod, this, rows);
		patterns[patternIndex] = newPattern;
		return newPattern;
	}
	public Pattern createPattern(final int patternIndex, final int rows, final int channels)
	{
		final Pattern newPattern = new Pattern(parentMod, this, rows, channels);
		patterns[patternIndex] = newPattern;
		return newPattern;
	}
	/**
	 * @param patterns The patterns to set.
	 */
	public void setPatternRow(final int patternIndex, final int row, final PatternRow patternRow)
	{
		patterns[patternIndex].setPatternRow(row, patternRow);
	}
	public PatternRow createPatternRow(final int patternIndex, final int row, final int channels)
	{
		Pattern currentPattern = getPattern(patternIndex);
		currentPattern.setPatternRow(row, new PatternRow(parentMod, currentPattern, channels));
		return getPatternRow(patternIndex, row);
	}
	/**
	 * @param patterns The patterns to set.
	 */
	public void setPatternElement(final int patternIndex, final int row, final int channel, final PatternElement patternElement)
	{
		patterns[patternIndex].setPatternElement(row, channel, patternElement);
	}
	public PatternElement createPatternElement(final int patternIndex, final int row, final int channel)
	{
		PatternRow currentPatternRow = getPatternRow(patternIndex, row);
		final boolean isImpulseTracker = (parentMod.getModType()&ModConstants.MODTYPE_IMPULSETRACKER)!=0;
		final PatternElement newElement = (isImpulseTracker)?new PatternElementIT(parentMod, currentPatternRow, patternIndex, row, channel):new PatternElementXM(parentMod, currentPatternRow, patternIndex, row, channel);
		currentPatternRow.setPatternElement(channel, newElement);
		return newElement;
	}
	/**
	 * @param patterns The patterns to set.
	 */
	public void setPatternElement(final PatternElement patternElement)
	{
		patterns[patternElement.getPatternIndex()].setPatternElement(patternElement.getRow(), patternElement.getChannel(), patternElement);
	}
	/**
	 * Copies the Channel Names, if any
	 * @since 06.02.2024
	 * @param chnNames
	 */
	public void setChannelNames(final String [] chnNames)
	{
		if (chnNames==null) return;

		final int anzChannels = (patterns!=null && patterns[0]!=null)?patterns[0].getChannels():chnNames.length;
		channelNames = new String[anzChannels];
		for (int c=0; c<anzChannels; c++)
		{
			channelNames[c] = (c<chnNames.length)?chnNames[c]:null;
		}
	}
	/**
	 * @since 06.02.2024
	 * @return
	 */
	public String[] getChannelNames()
	{
		return channelNames;
	}
	/**
	 * @since 06.02.2024
	 * @param channel
	 * @return
	 */
	public String getChannelName(final int channel)
	{
		if (channelNames!=null && channel<channelNames.length) return channelNames[channel];
		return null;
	}
	/**
	 * Copies the Channel Names, if any
	 * @since 07.02.2024
	 * @param chnNames
	 */
	public void setChannelColor(final Color [] chnColors)
	{
		final int anzChannels = (patterns!=null && patterns[0]!=null)?patterns[0].getChannels():chnColors.length;
		channelColors = new Color[anzChannels];
		for (int c=0; c<anzChannels; c++)
		{
			channelColors[c] = (c<chnColors.length)?chnColors[c]:null;
		}
	}
	/**
	 * @since 07.02.2024
	 * @return
	 */
	public Color[] getChannelColors()
	{
		return channelColors;
	}
	/**
	 * @since 07.02.2024
	 * @param channel
	 * @return
	 */
	public Color getChannelColor(final int channel)
	{
		if (channelColors!=null && channel<channelColors.length) return channelColors[channel];
		return null;
	}
	/**
	 * Of course we do not check if "rainbow colors" was selected in the ModPlug setup (we can't)
	 * so we just assume the default "random". This is transformed from CModDoc::SetDefaultChannelColors
	 * from OpenModPlug
	 * @since 07.02.2024
	 */
	private static final boolean rainbow = false; // assumed as default for now - could be a configuration sometime...
	public void createMPTMDefaultRainbowColors()
	{
		channelColors = new Color[getChannels()];
		int numGroups = 0;
		if (rainbow)
		{
			for (int c=1; c<channelColors.length; c++)
				if (channelNames==null || channelNames[c]==null || channelNames[c].length()==0 || !channelNames[c].equals(channelNames[c-1]))
					numGroups++;
		}
		final double hueFactor = (rainbow)?(1.5d * Math.PI) / (double)((numGroups>1)?numGroups-1:1): 1000d;  // Three quarters of the color wheel, red to purple
		for(int c=0, group=0; c<channelColors.length; c++)
		{
			if(c>0 && (channelNames==null || channelNames[c]==null || channelNames[c].length()==0 || !channelNames[c].equals(channelNames[c-1])))
				group++;
			final double hue = group * hueFactor;	// 0...2pi
			final double saturation = 0.3d;			// 0...2/3
			final double brightness = 1.2d;			// 0...4/3
			final int r = (int)Math.min(brightness * (1 + saturation * (Math.cos(hue)           - 1.0)) * 255d, 255d);
			final int g = (int)Math.min(brightness * (1 + saturation * (Math.cos(hue - 2.09439) - 1.0)) * 255d, 255d);
			final int b = (int)Math.min(brightness * (1 + saturation * (Math.cos(hue + 2.09439) - 1.0)) * 255d, 255d);
			channelColors[c] = new Color(r, g, b);
		}
	}
}
