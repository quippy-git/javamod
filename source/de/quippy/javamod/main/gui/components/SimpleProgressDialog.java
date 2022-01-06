/*
 * @(#) SimpleProgressDialog.java
 *
 * Created on 22.04.2012 by Daniel Becker
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
package de.quippy.javamod.main.gui.components;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import de.quippy.javamod.system.Helpers;

/**
 * @author Daniel Becker
 * @since 22.04.2012
 */
public class SimpleProgressDialog extends JDialog implements ProgressDialog
{
	private static final long serialVersionUID = 8117746938625469328L;

	private JPanel downloadPane = null;
	private JLabel currentFileName = null;
	private JProgressBar downloadProgressBar = null;

	/**
	 * Constructor for SimpleProgressDialog
	 * @param owner
	 * @param title
	 */
	public SimpleProgressDialog(Frame owner, String title)
	{
		super(owner, title, false);
		initialize();
	}
	/**
	 * Constructor for SimpleProgressDialog
	 * @param owner
	 * @param title
	 */
	public SimpleProgressDialog(Dialog owner, String title)
	{
		super(owner, title, false);
		initialize();
	}
	/**
	 * Constructor for SimpleProgressDialog
	 * @param owner
	 * @param modalityType
	 */
	public SimpleProgressDialog(Window owner, ModalityType modalityType)
	{
		super(owner, modalityType);
		initialize();
	}
	/**
	 * Constructor for SimpleProgressDialog
	 * @param owner
	 * @param title
	 */
	public SimpleProgressDialog(Window owner, String title)
	{
		super(owner, title, ModalityType.MODELESS);
		initialize();
	}
	protected void initialize()
	{
		setName("SimplePrograssDialog");
		setContentPane(getDownloadPane());
		pack();
		setLocation(Helpers.getFrameCenteredLocation(this, getOwner()));
	}
	protected JPanel getDownloadPane()
	{
		if (downloadPane==null)
		{
			downloadPane = new javax.swing.JPanel();
			downloadPane.setName("downloadPane");
			downloadPane.setLayout(new java.awt.GridBagLayout());
			downloadPane.add(getCurrentFileName(), Helpers.getGridBagConstraint(0, 0, 1, 0, java.awt.GridBagConstraints.BOTH, java.awt.GridBagConstraints.CENTER, 1.0, 1.0));
			downloadPane.add(getDownloadProgressBar(), Helpers.getGridBagConstraint(0, 1, 1, 0, java.awt.GridBagConstraints.BOTH, java.awt.GridBagConstraints.CENTER, 1.0, 1.0));
		}
		return downloadPane;
	}
	protected JLabel getCurrentFileName()
	{
		if (currentFileName == null)
		{
			currentFileName = new JLabel();
			currentFileName.setName("currentFileName");
		}
		return currentFileName;
	}
	public void setCurrentFileName(String newFileName)
	{
		getCurrentFileName().setText(newFileName);
	}
	protected JProgressBar getDownloadProgressBar()
	{
		if (downloadProgressBar==null)
		{
			downloadProgressBar = new javax.swing.JProgressBar();
			downloadProgressBar.setMinimum(0);
			downloadProgressBar.setMaximum(100);
			downloadProgressBar.setValue(0);
		}
		return downloadProgressBar;
	}
	public void setDetailMinimum(int minValue)
	{
		getDownloadProgressBar().setMinimum(minValue);
	}
	public void setDetailMaximum(int maxValue)
	{
		getDownloadProgressBar().setMaximum(maxValue);
	}
	public void setDetailValue(int value)
	{
		getDownloadProgressBar().setValue(value);
	}
}
