/*
 * @(#) HasParentDialog.java
 *
 * Created on 17.08.2020 by Daniel Becker
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
package de.quippy.javamod.multimedia;

import javax.swing.JDialog;

/**
 * @author Daniel Becker
 * This interface is used for all JPanel who need to get
 * their parent JDialog, in which they will be included.
 * So far only the modInfoPanel uses this.
 * @since 17.08.2020
 */
public interface HasParentDialog
{
	public void setParentDialog(final JDialog parent);
}
