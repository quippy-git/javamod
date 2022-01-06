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

/**
 * Description: 
 *  An exception to be thrown if the parser is unable to find an mpeg header.
 *
 * @author:  Jonathan Hilliker
 * @version: $Id: NoMPEGFramesException.java,v 1.3 2013-03-24 13:29:01 quippy Exp $
 * Revsisions: 
 *  $Log: not supported by cvs2svn $
 *  Revision 1.2  2010/04/07 17:03:49  quippy
 *  *** empty log message ***
 *
 *  Revision 1.1  2008/12/24 13:08:27  quippy
 *  *** empty log message ***
 *
 *  Revision 1.2  2001/10/19 03:57:53  helliker
 *  All set for release.
 *
 *
 */
public class NoMPEGFramesException extends Exception
{
	private static final long serialVersionUID = -8189457738550297675L;

	/**
	 * Create a NoMPEGFramesException with a default message.
	 *
	 */
	public NoMPEGFramesException()
	{
		super("The file specified is not a valid MPEG.");
	}

	/**
	 * Create a NoMPEGFramesException with a specified message.
	 *
	 * @param msg the message for this exception
	 */
	public NoMPEGFramesException(String msg)
	{
		super(msg);
	}
}
