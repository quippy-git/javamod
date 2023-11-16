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

/**
 * @author Daniel Becker
 * @since 11.11.2023
 */
public interface ModUpdateListener
{
	public class InformationObject
	{
		public long samplesMixed;
		public long timeCode;
		public long position;
		public String toString()
		{
			final int index = (int)((position >> 48)&0xFFFF);
			final int row = (int)((position >> 16)&0xFFFF);
			return samplesMixed+"/"+timeCode+"-->"+ModConstants.getAsHex(index, 2)+"/"+ModConstants.getAsHex(row, 2);
		}
	}
	/**
	 * This method is called during a row change (new row).
	 * As it is blocking the mixing, it <b>must</b> finish very shortly!
	 * Complex things like displaying the next pattern should not be done
	 * here. Simply memorize the position and its time stamp - either
	 * use the samples mixed till this event occurred or the timeCode in
	 * milliseconds.
	 * @since 13.11.2023
	 * @param infoObject
	 */
	public void getMixerInformation(final InformationObject infoObject);
}
