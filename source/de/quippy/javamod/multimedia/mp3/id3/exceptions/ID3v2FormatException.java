/*
 * @(#) ID3v2FormatException.java
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

/**
 * Description: 
 *  This exception is thrown when an data in an id3v2 tag violates the 
 *  id3v2 standards.
 *
 * @author:  Jonathan Hilliker modified by Daniel Becker
 */
public class ID3v2FormatException extends Exception
{
	private static final long serialVersionUID = 1668617234586193326L;

	/**
	 * Create an ID3v2FormatException with a default message
	 *
	 */
	public ID3v2FormatException()
	{
		super("ID3v2 tag is not formatted correctly.");
	}

	/**
	 * Create an ID3v2FormatException with a specified message
	 *
	 * @param msg the message for this exception
	 */
	public ID3v2FormatException(String msg)
	{
		super(msg);
	}

	/**
	 * Create an ID3v2FormatException with a specified message
	 * and original exception
	 * 
	 * @param msg the message for this exception
	 * @param cause the cause that wraps this exception
	 */
	public ID3v2FormatException(String message, Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * Create an ID3v2FormatException with the original
	 * exception
	 * 
	 * @param cause the cause that wraps this exception
	 */
	public ID3v2FormatException(Throwable cause)
	{
		super(cause);
	}
}
