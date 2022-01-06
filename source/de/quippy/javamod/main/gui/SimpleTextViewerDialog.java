/*
 * @(#) SimpleTextViewerDialog.java
 *
 * Created on 24.01.2010 by Daniel Becker
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
package de.quippy.javamod.main.gui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.swing.JDialog;
import javax.swing.JFrame;

import de.quippy.javamod.system.Helpers;
import de.quippy.javamod.system.Log;

/**
 * @author Daniel Becker
 * @since 24.01.2010
 */
public class SimpleTextViewerDialog extends JDialog
{
	private static final long serialVersionUID = -5666092255473846658L;
	
	private static final String DEFAULT_CODING = "ISO-8859-1";

	private javax.swing.JButton closeButton;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JTextArea textArea;
	private URL url;
	private String coding;

	/**
	 * Constructor for SimpleTextViewerDialog
	 * @param owner
	 * @throws HeadlessException
	 */
	public SimpleTextViewerDialog(JFrame owner, boolean modal)
	{
		super(owner, modal);
		url = null;
		coding = DEFAULT_CODING;
		initialize();
	}
	public SimpleTextViewerDialog(JFrame owner, boolean modal, URL url, String coding)
	{
		this(owner, modal);
		this.coding = coding;
		setDisplayTextFromURL(url);
	}
	public SimpleTextViewerDialog(JFrame owner, boolean modal, String url, String coding)
	{
		this(owner, modal, Helpers.createURLfromString(url), coding);
	}
	public SimpleTextViewerDialog(JFrame owner, boolean modal, URL url)
	{
		this(owner, modal, url, DEFAULT_CODING);
	}
	public SimpleTextViewerDialog(JFrame owner, boolean modal, String url)
	{
		this(owner, modal, url, DEFAULT_CODING);
	}
	private void initialize()
	{
		setTitle("Text/File Viewer");
		setName("SimpleTextFileViewer");
		setSize(new Dimension(640, 480));
		setPreferredSize(getSize());
		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(true);
		addWindowListener(new java.awt.event.WindowAdapter()
		{
			@Override
			public void windowClosing(java.awt.event.WindowEvent e)
			{
				doClose();
			}
		});

		setLayout(new java.awt.GridBagLayout());
		Container panel = getContentPane();

		panel.add(getScrollPane(), Helpers.getGridBagConstraint(0, 0, 1, 0, java.awt.GridBagConstraints.BOTH, java.awt.GridBagConstraints.WEST, 1.0, 1.0));
		panel.add(getCloseButton(), Helpers.getGridBagConstraint(0, 1, 1, 0, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.CENTER, 0.0, 0.0));

        if (url != null) fillTextArea();

        pack();
	}
	private javax.swing.JScrollPane getScrollPane()
	{
		if (scrollPane == null)
		{
			scrollPane = new javax.swing.JScrollPane();
			scrollPane.setName("scrollPane_TextField");
			scrollPane.setViewportView(getTextArea());
		}
		return scrollPane;
	}
	private javax.swing.JTextArea getTextArea()
	{
		if (textArea==null)
		{
			textArea = new javax.swing.JTextArea();
			textArea.setName("modInfo_Instruments");
			textArea.setEditable(false);
			textArea.setFont(Helpers.getTextAreaFont());
		}
		return textArea;
	}
	private javax.swing.JButton getCloseButton()
	{
		if (closeButton==null)
		{
	        closeButton = new javax.swing.JButton();
	        closeButton.setMnemonic('C');
	        closeButton.setText("Close");
	        closeButton.setToolTipText("Close");
	        closeButton.setFont(Helpers.getDialogFont());
	        closeButton.addActionListener(new java.awt.event.ActionListener()
	        {
	            public void actionPerformed(java.awt.event.ActionEvent evt)
	            {
	                doClose();
	            }
	        });
		}
		return closeButton;
	}
	public void doClose()
	{
		setVisible(false);
		dispose();
		//if we are alone in the world, exit the vm
		if (getParent() == null) System.exit(0); // this should not be needed! 
	}
	private void fillTextArea(final String text)
	{
		getTextArea().setText(text);
		getTextArea().select(0,0);
	}
	private void fillTextArea()
	{
		if (url!=null)
		{
			BufferedReader reader = null;
			try
			{
				reader = new BufferedReader(new InputStreamReader(url.openStream(), coding));
				StringBuilder fullText = new StringBuilder();
				String line;
				while ((line=reader.readLine())!=null)
				{
					fullText.append(line).append('\n');
				}
				fillTextArea(fullText.toString());
			}
			catch (Throwable ex)
			{
				Log.error("reading text failed", ex);
			}
			finally
			{
				if (reader!=null) try { reader.close(); } catch (IOException ex) { Log.error("IGNORED", ex); }
			}
		}
	}
	public void setDisplayTextFromURL(final URL url)
	{
		this.url = url;
		fillTextArea();
	}
	public void setDisplayTextFromURL(final String url)
	{
		setDisplayTextFromURL(Helpers.createURLfromString(url));
	}
	public void setDisplayText(final String text)
	{
		fillTextArea(text);
	}
}