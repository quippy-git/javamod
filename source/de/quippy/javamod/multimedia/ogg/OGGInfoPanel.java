/*
 * @(#) OGGInfoPanel.java
 *
 * Created on 05.12.2010 by Daniel Becker
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
package de.quippy.javamod.multimedia.ogg;

import java.awt.LayoutManager;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import de.quippy.javamod.multimedia.mp3.id3.NullsoftID3GenreTable;
import de.quippy.javamod.multimedia.ogg.metadata.OggMetaData;
import de.quippy.javamod.system.Helpers;

/**
 * @author Daniel Becker
 * @since 05.12.2010
 */
public class OGGInfoPanel extends JPanel
{
	private static final long serialVersionUID = -6645559669915111121L;

	private JLabel oggInfo_L_Filename = null;
	private JTextField oggInfo_Filename = null;
	private JLabel oggInfo_L_ShortDescription = null;
	private JTextField oggInfo_ShortDescription = null;

	private JPanel oggIDPanel = null;
	private JLabel v1_L_Track = null;
	private JTextField v1_Track = null;
	private JLabel v1_L_Title = null;
	private JTextField v1_Title = null;
	private JLabel v1_L_Artist = null;
	private JTextField v1_Artist = null;
	private JLabel v1_L_Album = null;
	private JTextField v1_Album = null;
	private JLabel v1_L_Year = null;
	private JTextField v1_Year = null;
	private JLabel v1_L_Genre = null;
	private JComboBox<String> v1_Genre = null;
	private JLabel v1_L_Comment = null;
	private JScrollPane scrollPane_comment = null;
	private JTextArea v1_Comment = null;

	/**
	 * Constructor for OGGInfoPanel
	 */
	public OGGInfoPanel()
	{
		super();
		initialize();
	}
	/**
	 * Constructor for OGGInfoPanel
	 * @param layout
	 */
	public OGGInfoPanel(LayoutManager layout)
	{
		super(layout);
		initialize();
	}
	/**
	 * Constructor for OGGInfoPanel
	 * @param isDoubleBuffered
	 */
	public OGGInfoPanel(boolean isDoubleBuffered)
	{
		super(isDoubleBuffered);
		initialize();
	}
	/**
	 * Constructor for OGGInfoPanel
	 * @param layout
	 * @param isDoubleBuffered
	 */
	public OGGInfoPanel(LayoutManager layout, boolean isDoubleBuffered)
	{
		super(layout, isDoubleBuffered);
		initialize();
	}
	private void initialize()
	{
		this.setName("OGGInfoPane");
		this.setLayout(new java.awt.GridBagLayout());
		
		this.add(getOGGInfo_L_Filename(),			Helpers.getGridBagConstraint(0, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getOGGInfo_Filename(),				Helpers.getGridBagConstraint(1, 0, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		this.add(getOGGInfo_L_ShortDescription(),	Helpers.getGridBagConstraint(0, 1, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getOGGInfo_ShortDescription(),		Helpers.getGridBagConstraint(1, 1, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		this.add(getOggIDPanel(),					Helpers.getGridBagConstraint(0, 2, 1, 0, java.awt.GridBagConstraints.BOTH, java.awt.GridBagConstraints.WEST, 1.0, 1.0));
	}
	private javax.swing.JLabel getOGGInfo_L_Filename()
	{
		if (oggInfo_L_Filename==null)
		{
			oggInfo_L_Filename = new javax.swing.JLabel();
			oggInfo_L_Filename.setName("oggInfo_L_Filename");
			oggInfo_L_Filename.setText("File");
			oggInfo_L_Filename.setFont(Helpers.getDialogFont());
		}
		return oggInfo_L_Filename;
	}
	private javax.swing.JTextField getOGGInfo_Filename()
	{
		if (oggInfo_Filename==null)
		{
			oggInfo_Filename = new javax.swing.JTextField();
			oggInfo_Filename.setName("oggInfo_Filename");
			oggInfo_Filename.setFont(Helpers.getDialogFont());
			oggInfo_Filename.setEditable(false);
		}
		return oggInfo_Filename;
	}
	private javax.swing.JLabel getOGGInfo_L_ShortDescription()
	{
		if (oggInfo_L_ShortDescription==null)
		{
			oggInfo_L_ShortDescription = new javax.swing.JLabel();
			oggInfo_L_ShortDescription.setName("oggInfo_L_ShortDescription");
			oggInfo_L_ShortDescription.setText("Name");
			oggInfo_L_ShortDescription.setFont(Helpers.getDialogFont());
		}
		return oggInfo_L_ShortDescription;
	}
	public javax.swing.JTextField getOGGInfo_ShortDescription()
	{
		if (oggInfo_ShortDescription==null)
		{
			oggInfo_ShortDescription = new javax.swing.JTextField();
			oggInfo_ShortDescription.setName("oggInfo_ShortDescription");
			oggInfo_ShortDescription.setFont(Helpers.getDialogFont());
			oggInfo_ShortDescription.setEditable(false);
		}
		return oggInfo_ShortDescription;
	}
	private JPanel getOggIDPanel()
	{
		if (oggIDPanel==null)
		{
			oggIDPanel = new JPanel();
			oggIDPanel.setLayout(new java.awt.GridBagLayout());
			
			oggIDPanel.add(getV1_L_Track(),		Helpers.getGridBagConstraint(0, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.NORTHEAST, 0.0, 0.0));
			oggIDPanel.add(getV1_Track(),		Helpers.getGridBagConstraint(1, 0, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.NORTHWEST, 1.0, 0.0));
			oggIDPanel.add(getV1_L_Title(),		Helpers.getGridBagConstraint(0, 1, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.NORTHEAST, 0.0, 0.0));
			oggIDPanel.add(getV1_Title(),		Helpers.getGridBagConstraint(1, 1, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.NORTHWEST, 1.0, 0.0));
			oggIDPanel.add(getV1_L_Artist(),	Helpers.getGridBagConstraint(0, 2, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.NORTHEAST, 0.0, 0.0));
			oggIDPanel.add(getV1_Artist(),		Helpers.getGridBagConstraint(1, 2, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.NORTHWEST, 1.0, 0.0));
			oggIDPanel.add(getV1_L_Album(),		Helpers.getGridBagConstraint(0, 3, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.NORTHEAST, 0.0, 0.0));
			oggIDPanel.add(getV1_Album(),		Helpers.getGridBagConstraint(1, 3, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.NORTHWEST, 1.0, 0.0));
			oggIDPanel.add(getV1_L_Year(),		Helpers.getGridBagConstraint(0, 4, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.NORTHEAST, 0.0, 0.0));
			oggIDPanel.add(getV1_Year(),		Helpers.getGridBagConstraint(1, 4, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.NORTHWEST, 1.0, 0.0));
			oggIDPanel.add(getV1_L_Genre(),		Helpers.getGridBagConstraint(2, 4, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.NORTHEAST, 0.0, 0.0));
			oggIDPanel.add(getV1_Genre(),		Helpers.getGridBagConstraint(3, 4, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.NORTHWEST, 1.0, 0.0));
			oggIDPanel.add(getV1_L_Comment(),	Helpers.getGridBagConstraint(0, 5, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.NORTHEAST, 0.0, 0.0));
			oggIDPanel.add(getScrollPane_Comment(),		Helpers.getGridBagConstraint(1, 5, 1, 0, java.awt.GridBagConstraints.BOTH, java.awt.GridBagConstraints.NORTHWEST, 1.0, 1.0));
			oggIDPanel.add(new JPanel(),		Helpers.getGridBagConstraint(1, 5, 1, 0, java.awt.GridBagConstraints.BOTH, java.awt.GridBagConstraints.NORTHEAST, 1.0, 1.0));
		}
		return oggIDPanel;
	}
	private javax.swing.JLabel getV1_L_Track()
	{
		if (v1_L_Track==null)
		{
			v1_L_Track = new javax.swing.JLabel();
			v1_L_Track.setName("v1_L_Track");
			v1_L_Track.setText("Track #");
			v1_L_Track.setFont(Helpers.getDialogFont());
		}
		return v1_L_Track;
	}
	private javax.swing.JTextField getV1_Track()
	{
		if (v1_Track==null)
		{
			v1_Track = new javax.swing.JTextField();
			v1_Track.setName("v1_Track");
			v1_Track.setFont(Helpers.getDialogFont());
			v1_Track.setEditable(true);
		}
		return v1_Track;
	}
	private javax.swing.JLabel getV1_L_Title()
	{
		if (v1_L_Title==null)
		{
			v1_L_Title = new javax.swing.JLabel();
			v1_L_Title.setName("v1_L_Title");
			v1_L_Title.setText("Title");
			v1_L_Title.setFont(Helpers.getDialogFont());
		}
		return v1_L_Title;
	}
	private javax.swing.JTextField getV1_Title()
	{
		if (v1_Title==null)
		{
			v1_Title = new javax.swing.JTextField();
			v1_Title.setName("v1_Title");
			v1_Title.setFont(Helpers.getDialogFont());
			v1_Title.setEditable(true);
		}
		return v1_Title;
	}
	private javax.swing.JLabel getV1_L_Artist()
	{
		if (v1_L_Artist==null)
		{
			v1_L_Artist = new javax.swing.JLabel();
			v1_L_Artist.setName("v1_L_Artist");
			v1_L_Artist.setText("Artist");
			v1_L_Artist.setFont(Helpers.getDialogFont());
		}
		return v1_L_Artist;
	}
	private javax.swing.JTextField getV1_Artist()
	{
		if (v1_Artist==null)
		{
			v1_Artist = new javax.swing.JTextField();
			v1_Artist.setName("v1_Artist");
			v1_Artist.setFont(Helpers.getDialogFont());
			v1_Artist.setEditable(true);
		}
		return v1_Artist;
	}
	private javax.swing.JLabel getV1_L_Album()
	{
		if (v1_L_Album==null)
		{
			v1_L_Album = new javax.swing.JLabel();
			v1_L_Album.setName("v1_L_Album");
			v1_L_Album.setText("Album");
			v1_L_Album.setFont(Helpers.getDialogFont());
		}
		return v1_L_Album;
	}
	private javax.swing.JTextField getV1_Album()
	{
		if (v1_Album==null)
		{
			v1_Album = new javax.swing.JTextField();
			v1_Album.setName("v1_Album");
			v1_Album.setFont(Helpers.getDialogFont());
			v1_Album.setEditable(true);
		}
		return v1_Album;
	}
	private javax.swing.JLabel getV1_L_Year()
	{
		if (v1_L_Year==null)
		{
			v1_L_Year = new javax.swing.JLabel();
			v1_L_Year.setName("v1_L_Year");
			v1_L_Year.setText("Year");
			v1_L_Year.setFont(Helpers.getDialogFont());
		}
		return v1_L_Year;
	}
	private javax.swing.JTextField getV1_Year()
	{
		if (v1_Year==null)
		{
			v1_Year = new javax.swing.JTextField();
			v1_Year.setName("v1_Year");
			v1_Year.setFont(Helpers.getDialogFont());
			v1_Year.setEditable(true);
		}
		return v1_Year;
	}
	private javax.swing.JLabel getV1_L_Genre()
	{
		if (v1_L_Genre==null)
		{
			v1_L_Genre = new javax.swing.JLabel();
			v1_L_Genre.setName("v1_L_Genre");
			v1_L_Genre.setText("Genre");
			v1_L_Genre.setFont(Helpers.getDialogFont());
		}
		return v1_L_Genre;
	}
	private javax.swing.JComboBox<String> getV1_Genre()
	{
		if (v1_Genre==null)
		{
			v1_Genre = new javax.swing.JComboBox<String>(NullsoftID3GenreTable.getGenres());
			v1_Genre.setName("v1_Genre");
			v1_Genre.setFont(Helpers.getDialogFont());
			v1_Genre.setEditable(true);
		}
		return v1_Genre;
	}
	private javax.swing.JLabel getV1_L_Comment()
	{
		if (v1_L_Comment==null)
		{
			v1_L_Comment = new javax.swing.JLabel();
			v1_L_Comment.setName("v1_L_Comment");
			v1_L_Comment.setText("Comment");
			v1_L_Comment.setFont(Helpers.getDialogFont());
		}
		return v1_L_Comment;
	}
	private JScrollPane getScrollPane_Comment()
	{
		if (scrollPane_comment == null)
		{
			scrollPane_comment = new javax.swing.JScrollPane();
			scrollPane_comment.setName("scrollPane_comment");
			scrollPane_comment.setViewportView(getV1_Comment());
		}
		return scrollPane_comment;
	}
	private JTextArea getV1_Comment()
	{
		if (v1_Comment==null)
		{
			v1_Comment = new javax.swing.JTextArea();
			v1_Comment.setName("v1_Comment");
			v1_Comment.setFont(Helpers.getDialogFont());
			v1_Comment.setLineWrap(true);
			v1_Comment.setWrapStyleWord(true);
			v1_Comment.setEditable(true);
		}
		return v1_Comment;
	}
	public void fillInfoPanelWith(OggMetaData currentID, String fileName)
	{
		getOGGInfo_Filename().setText(fileName);
		getOGGInfo_ShortDescription().setText(currentID.getShortDescription());
		
		getV1_Track().setText(currentID.getTrackNumber());
		getV1_Title().setText(currentID.getTitle());
		getV1_Artist().setText(currentID.getArtist());
		getV1_Album().setText(currentID.getAlbum());
		getV1_Year().setText(currentID.getDate());
		getV1_Genre().setSelectedItem(currentID.getGenre());
		getV1_Comment().setText(currentID.getComment());
		getV1_Comment().select(0, 0);
	}
}