/*
 * @(#) ModUpdateListener.java
 *
 * Created on 11.11.2023 by Daniel Becker
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

import de.quippy.javamod.multimedia.mod.ModConstants;
import de.quippy.javamod.system.Helpers;

/**
 * @author Daniel Becker
 * @since 11.11.2023
 */
public interface ModUpdateListener
{
	public class TimedInformation
	{
		public long samplesMixed;
		public long timeCode;
		
		public TimedInformation(final int sampleRate, final long samplesMixed)
		{
			this.samplesMixed = samplesMixed;
			this.timeCode = (samplesMixed * 1000L) / (long)sampleRate;
		}
		
		public String toString()
		{
			return "Timer: " + samplesMixed + "/" + timeCode;
		}
	}
	public class PatternPositionInformation extends TimedInformation
	{
		public boolean active;
		public int patternIndex;
		public int patternRow;

		public PatternPositionInformation(final int sampleRate, final long samplesMixed, final long position)
		{
			super(sampleRate, samplesMixed);
			this.patternIndex = (int)((position >> 48)&0xFFFF);
			this.patternRow = (int)((position >> 16)&0xFFFF);
		}
		
		public String toString()
		{
			return super.toString()+"-->Position: "+ModConstants.getAsHex(patternIndex, 2)+"/"+ModConstants.getAsHex(patternRow, 2);
		}
	}
	public class PeekInformation extends TimedInformation
	{
		public int channel;
		public int actPeekLeft;
		public int actPeekRight;
		public boolean isSurround;
		
		public PeekInformation(final int sampleRate, final long samplesMixed, final int channel, final int actPeekLeft, final int actPeekRight, final boolean isSurround)
		{
			super(sampleRate, samplesMixed);
			this.channel = channel;
			this.actPeekLeft = actPeekLeft;
			this.actPeekRight = actPeekRight;
			this.isSurround = isSurround;
		}
		public String toString()
		{
			return super.toString()+"-->Peek: "+channel+": " + actPeekLeft + "/" + actPeekRight + ((isSurround)?" is surround":Helpers.EMPTY_STING);
		}
	}
	public class StatusInformation
	{
		public boolean status;
		
		public StatusInformation(final boolean newStatus)
		{
			status = newStatus;
		}
		public String toString()
		{
			return "Status: " + Boolean.toString(status);
		}
	}
	/**
	 * This method is called during a row change (new row).<br>
	 * As it is blocking the mixing, it <b>must</b> finish very shortly!
	 * Complex things like displaying the next pattern should not be done
	 * here. Simply memorize the position and its time stamp - either
	 * use the samples mixed till this event occurred or the timeCode in
	 * milliseconds.
	 * @since 13.11.2023
	 * @param infoObject
	 */
	public void getPatternPositionInformation(final PatternPositionInformation infoObject);
	/**
	 * This method is called to inform listeners about peek informations on
	 * a specific channel.<br>
	 * As it is blocking the mixing, it <b>must</b> finish very shortly!
	 * Complex things like displaying the next pattern should not be done
	 * here. Simply memorize the position and its time stamp - either
	 * use the samples mixed till this event occurred or the timeCode in
	 * milliseconds.
	 * @since 13.11.2023
	 * @param infoObject
	 */
	public void getPeekInformation(final PeekInformation infoObject);
	/**
	 * This method will inform any listener, that status informations will
	 * be send (status==true) - or not (status==false). This is somewhat equal
	 * to the playback was stopped or finished - or we are seeking
	 * @since 13.11.2023
	 * @param infoObject
	 */
	public void getStatusInformation(final StatusInformation infoObject);
}
