/*
 * @(#) ID3FieldDataException.java
 *
 * Created on 23.12.2008 by Daniel Becker
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

package de.quippy.javamod.multimedia.mp3.id3.exceptions;

public class ID3FieldDataException extends Exception
{
	private static final long serialVersionUID = -2358436509702583539L;

	/**
	 * Create an ID3FieldDataException with a default message
	 *
	 */
	public ID3FieldDataException()
	{
		super("Invalid data supplied to ID3 tag.");
	}

	/**
	 * Create an ID3FieldDataException with the specified message
	 *
	 * @param msg a String specifying the specific problem encountered
	 */
	public ID3FieldDataException(String msg)
	{
		super(msg);
	}

}
