/*
 * @(#) CueIndex.java
 *
 * Created on 14.02.2012 by Daniel Becker
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
package de.quippy.javamod.main.playlist.cuesheet;

/**
 * @author Daniel Becker
 *
 */
public class CueIndex
{
	private int indexNo;
	private long millisecondIndex;
	/**
	 * 
	 * @since 14.02.2012
	 */
	public CueIndex()
	{
		super();
	}

	/**
	 * @return the indexNo
	 * @since 14.02.2012
	 */
	public int getIndexNo()
	{
		return indexNo;
	}
	/**
	 * @param indexNo the indexNo to set
	 * @since 14.02.2012
	 */
	public void setIndexNo(int indexNo)
	{
		this.indexNo = indexNo;
	}
	/**
	 * @return the millisecondIndex
	 * @since 14.02.2012
	 */
	public long getMillisecondIndex()
	{
		return millisecondIndex;
	}
	/**
	 * @param millisecondIndex the millisecondIndex to set
	 * @since 14.02.2012
	 */
	public void setMillisecondIndex(long millisecondIndex)
	{
		this.millisecondIndex = millisecondIndex;
	}
}
