/*
 * @(#) OPL3InfoPanel.java
 *
 * Created on 03.08.2020 by Daniel Becker
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
package de.quippy.javamod.multimedia.opl3;

import java.awt.LayoutManager;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import de.quippy.javamod.multimedia.opl3.sequencer.OPL3Sequence;
import de.quippy.javamod.system.Helpers;

/**
 * @author Daniel Becker
 * @since 03.08.2020
 */
public class OPL3InfoPanel extends JPanel
{
	private static final long serialVersionUID = 6059322698770106687L;
	
	private JLabel opl3NameLabel = null;
	private JTextField opl3Name = null;
	private JLabel opl3FileTypeLabel = null;
	private JTextField opl3FileType = null;
	private JLabel opl3AuthorLabel = null;
	private JTextField opl3Author = null;
	private JLabel opl3DescriptionLabel = null;
	private JScrollPane opl3DescriptionScrollPane = null;
	private JTextArea opl3Description = null;
	/**
	 * Constructor for OPL3InfoPanel
	 */
	public OPL3InfoPanel()
	{
		super();
		initialize();
	}
	/**
	 * Constructor for OPL3InfoPanel
	 * @param layout
	 */
	public OPL3InfoPanel(LayoutManager layout)
	{
		super(layout);
		initialize();
	}
	/**
	 * Constructor for OPL3InfoPanel
	 * @param isDoubleBuffered
	 */
	public OPL3InfoPanel(boolean isDoubleBuffered)
	{
		super(isDoubleBuffered);
		initialize();
	}
	/**
	 * Constructor for OPL3InfoPanel
	 * @param layout
	 * @param isDoubleBuffered
	 */
	public OPL3InfoPanel(LayoutManager layout, boolean isDoubleBuffered)
	{
		super(layout, isDoubleBuffered);
		initialize();
	}
	private void initialize()
	{
		setName("OPL3InfoPane");
		setLayout(new java.awt.GridBagLayout());
		
		this.add(getOP3NameLabel(), 			Helpers.getGridBagConstraint(0, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getOPL3Name(), 				Helpers.getGridBagConstraint(1, 0, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		this.add(getOP3FileTypeLabel(),			Helpers.getGridBagConstraint(0, 1, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getOPL3FileType(), 			Helpers.getGridBagConstraint(1, 1, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		this.add(getOPL3AuthorLabel(),			Helpers.getGridBagConstraint(0, 2, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getOPL3Author(), 				Helpers.getGridBagConstraint(1, 2, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		this.add(getOPL3DescriptionLabel(), 	Helpers.getGridBagConstraint(0, 3, 1, 0, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getOPL3DescriptionScrollPane(),Helpers.getGridBagConstraint(0, 4, 1, 0, java.awt.GridBagConstraints.BOTH, java.awt.GridBagConstraints.WEST, 1.0, 1.0));
	}
	private JLabel getOP3NameLabel()
	{
		if (opl3NameLabel==null)
		{
			opl3NameLabel = new JLabel();
			opl3NameLabel.setName("opl3NameLabel");
			opl3NameLabel.setText("OPL file name:");			
			opl3NameLabel.setFont(Helpers.getDialogFont());
		}
		return opl3NameLabel;
	}
	private JTextField getOPL3Name()
	{
		if (opl3Name==null)
		{
			opl3Name = new javax.swing.JTextField();
			opl3Name.setName("opl3Name");
			opl3Name.setEditable(false);
			opl3Name.setFont(Helpers.getDialogFont());
		}
		return opl3Name;
	}
	private JLabel getOP3FileTypeLabel()
	{
		if (opl3FileTypeLabel==null)
		{
			opl3FileTypeLabel = new JLabel();
			opl3FileTypeLabel.setName("opl3FileTypeLabel");
			opl3FileTypeLabel.setText("OPL file type:");
			opl3FileTypeLabel.setFont(Helpers.getDialogFont());
		}
		return opl3FileTypeLabel;
	}
	private JTextField getOPL3FileType()
	{
		if (opl3FileType==null)
		{
			opl3FileType = new javax.swing.JTextField();
			opl3FileType.setName("opl3FileType");
			opl3FileType.setEditable(false);
			opl3FileType.setFont(Helpers.getDialogFont());
		}
		return opl3FileType;
	}
	private JLabel getOPL3AuthorLabel()
	{
		if (opl3AuthorLabel==null)
		{
			opl3AuthorLabel = new JLabel();
			opl3AuthorLabel.setName("opl3AuthorLabel");
			opl3AuthorLabel.setText("Author:");
			opl3AuthorLabel.setFont(Helpers.getDialogFont());
		}
		return opl3AuthorLabel;
	}
	private JTextField getOPL3Author()
	{
		if (opl3Author==null)
		{
			opl3Author = new javax.swing.JTextField();
			opl3Author.setName("opl3Author");
			opl3Author.setEditable(false);
			opl3Author.setFont(Helpers.getDialogFont());
		}
		return opl3Author;
	}
	private JLabel getOPL3DescriptionLabel()
	{
		if (opl3DescriptionLabel==null)
		{
			opl3DescriptionLabel = new JLabel();
			opl3DescriptionLabel.setName("opl3DescriptionLabel");
			opl3DescriptionLabel.setText("Song Informations:");
			opl3DescriptionLabel.setFont(Helpers.getDialogFont());
		}
		return opl3DescriptionLabel;
	}
	private JScrollPane getOPL3DescriptionScrollPane()
	{
		if (opl3DescriptionScrollPane == null)
		{
			opl3DescriptionScrollPane = new JScrollPane();
			opl3DescriptionScrollPane.setName("opl3DescriptionScrollPane");
			opl3DescriptionScrollPane.setViewportView(getOPL3Desciption());
		}
		return opl3DescriptionScrollPane;
	}
	private JTextArea getOPL3Desciption()
	{
		if (opl3Description==null)
		{
			opl3Description = new JTextArea();
			opl3Description.setName("opl3Description");
			opl3Description.setEditable(false);
			opl3Description.setFont(Helpers.getTextAreaFont());
		}
		return opl3Description;
	}
	public void fillInfoPanelWith(OPL3Sequence opl3Sequence)
	{
		getOPL3Name().setText(opl3Sequence.getSongName());
		getOPL3FileType().setText(opl3Sequence.getTypeName());
		getOPL3Author().setText(opl3Sequence.getAuthor());
		getOPL3Desciption().setText(opl3Sequence.getDescription());
	}
}
