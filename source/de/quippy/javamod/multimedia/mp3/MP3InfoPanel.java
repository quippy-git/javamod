/*
 * @(#) MP3InfoPanel.java
 *
 * Created on 26.12.2008 by Daniel Becker
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
package de.quippy.javamod.multimedia.mp3;

import java.awt.LayoutManager;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import de.quippy.javamod.multimedia.mp3.id3.MP3FileID3Controller;
import de.quippy.javamod.multimedia.mp3.id3.NullsoftID3GenreTable;
import de.quippy.javamod.system.Helpers;
import de.quippy.mp3.decoder.Header;

/**
 * @author Daniel Becker
 * @since 26.12.2008
 */
public class MP3InfoPanel extends JPanel
{
	private static final long serialVersionUID = -7222399300330777886L;

	private JLabel mp3Info_L_Filename = null;
	private JTextField mp3Info_Filename = null;
	private JLabel mp3Info_L_ShortDescription = null;
	private JTextField mp3Info_ShortDescription = null;
	private JLabel mp3Info_BitRateLabel = null;
	private JTextField mp3Info_BitRate = null;
	private JLabel mp3Info_SampleSizeInBitsLabel = null;
	private JTextField mp3Info_SampleSizeInBits = null;
	private JLabel mp3Info_FrequencyLabel = null;
	private JTextField mp3Info_Frequency = null;
	private JLabel mp3Info_ChannelsLabel = null;
	private JTextField mp3Info_Channels = null;
	private JLabel mp3Info_EncodingLabel = null;
	private JTextField mp3Info_Encoding = null;
	
	private JTabbedPane tabbedPane = null;
	private JPanel ID3v1Panel = null;
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

	private JPanel ID3v2Panel = null;
	private JLabel v2_L_Track = null;
	private JTextField v2_Track = null;
	private JLabel v2_L_Disc = null;
	private JTextField v2_Disc = null;
	private JLabel v2_L_Title = null;
	private JTextField v2_Title = null;
	private JLabel v2_L_Artist = null;
	private JTextField v2_Artist = null;
	private JLabel v2_L_Album = null;
	private JTextField v2_Album = null;
	private JLabel v2_L_Year = null;
	private JTextField v2_Year = null;
	private JLabel v2_L_Genre = null;
	private JComboBox<String> v2_Genre = null;
	private JLabel v2_L_Comment = null;
	private JScrollPane v2_Comment_Scrollpane = null;
	private JTextArea v2_Comment = null;
	private JLabel v2_L_AlbumArtist = null;
	private JTextField v2_AlbumArtist = null;
	private JLabel v2_L_Composer = null;
	private JTextField v2_Composer = null;
	private JLabel v2_L_Publisher= null;
	private JTextField v2_Publisher = null;
	private JLabel v2_L_OrigArtist = null;
	private JTextField v2_OrigArtist = null;
	private JLabel v2_L_Copyright = null;
	private JTextField v2_Copyright = null;
	private JLabel v2_L_URL = null;
	private JTextField v2_URL = null;
	private JLabel v2_L_Encoded = null;
	private JTextField v2_Encoded = null;
	private JLabel v2_L_BPM = null;
	private JTextField v2_BPM = null;
	
	private static final String EMPTY_STRING = "";
	
	/**
	 * Constructor for MP3InfoPanel
	 */
	public MP3InfoPanel()
	{
		super();
		initialize();
	}
	/**
	 * Constructor for MP3InfoPanel
	 * @param layout
	 */
	public MP3InfoPanel(LayoutManager layout)
	{
		super(layout);
		initialize();
	}
	/**
	 * Constructor for MP3InfoPanel
	 * @param isDoubleBuffered
	 */
	public MP3InfoPanel(boolean isDoubleBuffered)
	{
		super(isDoubleBuffered);
		initialize();
	}
	/**
	 * Constructor for MP3InfoPanel
	 * @param layout
	 * @param isDoubleBuffered
	 */
	public MP3InfoPanel(LayoutManager layout, boolean isDoubleBuffered)
	{
		super(layout, isDoubleBuffered);
		initialize();
	}

	private void initialize()
	{
		this.setName("MP3InfoPane");
		this.setLayout(new java.awt.GridBagLayout());
		
		this.add(getMP3Info_L_Filename(),			Helpers.getGridBagConstraint(0, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getMP3Info_Filename(),				Helpers.getGridBagConstraint(1, 0, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		this.add(getMP3Info_L_ShortDescription(),	Helpers.getGridBagConstraint(0, 1, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getMP3Info_ShortDescription(),		Helpers.getGridBagConstraint(1, 1, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		this.add(getmp3Info_FrequencyLabel(),		Helpers.getGridBagConstraint(0, 2, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getmp3Info_Frequency(),			Helpers.getGridBagConstraint(1, 2, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		this.add(getmp3Info_SampleSizeInBitsLabel(),Helpers.getGridBagConstraint(2, 2, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getmp3Info_SampleSizeInBits(),		Helpers.getGridBagConstraint(3, 2, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		this.add(getmp3Info_ChannelsLabel(),		Helpers.getGridBagConstraint(4, 2, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getmp3Info_Channels(),				Helpers.getGridBagConstraint(5, 2, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		this.add(getmp3Info_EncodingLabel(),		Helpers.getGridBagConstraint(0, 3, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getmp3Info_Encoding(),				Helpers.getGridBagConstraint(1, 3, 1, 3, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		this.add(getmp3Info_BitRateLabel(),		Helpers.getGridBagConstraint(4, 3, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getmp3Info_BitRate(),				Helpers.getGridBagConstraint(5, 3, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		this.add(getTabbedPane(), 					Helpers.getGridBagConstraint(0, 4, 1, 0, java.awt.GridBagConstraints.BOTH, java.awt.GridBagConstraints.WEST, 1.0, 1.0));
	}
	private javax.swing.JLabel getMP3Info_L_Filename()
	{
		if (mp3Info_L_Filename==null)
		{
			mp3Info_L_Filename = new javax.swing.JLabel();
			mp3Info_L_Filename.setName("mp3Info_L_Filename");
			mp3Info_L_Filename.setText("File");
			mp3Info_L_Filename.setFont(Helpers.getDialogFont());
		}
		return mp3Info_L_Filename;
	}
	private javax.swing.JTextField getMP3Info_Filename()
	{
		if (mp3Info_Filename==null)
		{
			mp3Info_Filename = new javax.swing.JTextField();
			mp3Info_Filename.setName("mp3Info_Filename");
			mp3Info_Filename.setFont(Helpers.getDialogFont());
			mp3Info_Filename.setEditable(false);
		}
		return mp3Info_Filename;
	}
	private javax.swing.JLabel getMP3Info_L_ShortDescription()
	{
		if (mp3Info_L_ShortDescription==null)
		{
			mp3Info_L_ShortDescription = new javax.swing.JLabel();
			mp3Info_L_ShortDescription.setName("mp3Info_L_ShortDescription");
			mp3Info_L_ShortDescription.setText("Name");
			mp3Info_L_ShortDescription.setFont(Helpers.getDialogFont());
		}
		return mp3Info_L_ShortDescription;
	}
	private javax.swing.JTextField getMP3Info_ShortDescription()
	{
		if (mp3Info_ShortDescription==null)
		{
			mp3Info_ShortDescription = new javax.swing.JTextField();
			mp3Info_ShortDescription.setName("mp3Info_ShortDescription");
			mp3Info_ShortDescription.setFont(Helpers.getDialogFont());
			mp3Info_ShortDescription.setEditable(false);
		}
		return mp3Info_ShortDescription;
	}
	private javax.swing.JLabel getmp3Info_BitRateLabel()
	{
		if (mp3Info_BitRateLabel==null)
		{
			mp3Info_BitRateLabel = new JLabel("Bit Rate:");
			mp3Info_BitRateLabel.setFont(Helpers.getDialogFont());
		}
		return mp3Info_BitRateLabel;
	}
	private JTextField getmp3Info_BitRate()
	{
		if (mp3Info_BitRate==null)
		{
			mp3Info_BitRate = new javax.swing.JTextField();
			mp3Info_BitRate.setEditable(false);
			mp3Info_BitRate.setFont(Helpers.getDialogFont());
		}
		return mp3Info_BitRate;
	}
	private javax.swing.JLabel getmp3Info_FrequencyLabel()
	{
		if (mp3Info_FrequencyLabel==null)
		{
			mp3Info_FrequencyLabel = new JLabel("Rate:");
			mp3Info_FrequencyLabel.setFont(Helpers.getDialogFont());
		}
		return mp3Info_FrequencyLabel;
	}
	private JTextField getmp3Info_Frequency()
	{
		if (mp3Info_Frequency==null)
		{
			mp3Info_Frequency = new javax.swing.JTextField();
			mp3Info_Frequency.setEditable(false);
			mp3Info_Frequency.setFont(Helpers.getDialogFont());
		}
		return mp3Info_Frequency;
	}
	private javax.swing.JLabel getmp3Info_SampleSizeInBitsLabel()
	{
		if (mp3Info_SampleSizeInBitsLabel==null)
		{
			mp3Info_SampleSizeInBitsLabel = new JLabel("Bits:");
			mp3Info_SampleSizeInBitsLabel.setFont(Helpers.getDialogFont());
		}
		return mp3Info_SampleSizeInBitsLabel;
	}
	private JTextField getmp3Info_SampleSizeInBits()
	{
		if (mp3Info_SampleSizeInBits==null)
		{
			mp3Info_SampleSizeInBits = new javax.swing.JTextField();
			mp3Info_SampleSizeInBits.setEditable(false);
			mp3Info_SampleSizeInBits.setFont(Helpers.getDialogFont());
		}
		return mp3Info_SampleSizeInBits;
	}
	private javax.swing.JLabel getmp3Info_ChannelsLabel()
	{
		if (mp3Info_ChannelsLabel==null)
		{
			mp3Info_ChannelsLabel = new JLabel("Channel:");
			mp3Info_ChannelsLabel.setFont(Helpers.getDialogFont());
		}
		return mp3Info_ChannelsLabel;
	}
	private JTextField getmp3Info_Channels()
	{
		if (mp3Info_Channels==null)
		{
			mp3Info_Channels = new javax.swing.JTextField();
			mp3Info_Channels.setEditable(false);
			mp3Info_Channels.setFont(Helpers.getDialogFont());
		}
		return mp3Info_Channels;
	}
	private javax.swing.JLabel getmp3Info_EncodingLabel()
	{
		if (mp3Info_EncodingLabel==null)
		{
			mp3Info_EncodingLabel = new JLabel("Encoding:");
			mp3Info_EncodingLabel.setFont(Helpers.getDialogFont());
		}
		return mp3Info_EncodingLabel;
	}
	private JTextField getmp3Info_Encoding()
	{
		if (mp3Info_Encoding==null)
		{
			mp3Info_Encoding = new javax.swing.JTextField();
			mp3Info_Encoding.setEditable(false);
			mp3Info_Encoding.setFont(Helpers.getDialogFont());
		}
		return mp3Info_Encoding;
	}
	private JTabbedPane getTabbedPane()
	{
		if (tabbedPane==null)
		{
			tabbedPane = new JTabbedPane();
			tabbedPane.addTab("ID3v1", getID3v1Panel());
			tabbedPane.addTab("ID3v2", getID3v2Panel());
		}
		return tabbedPane;
	}
	private JPanel getID3v1Panel()
	{
		if (ID3v1Panel==null)
		{
			ID3v1Panel = new JPanel();
			ID3v1Panel.setLayout(new java.awt.GridBagLayout());
			
			ID3v1Panel.add(getV1_L_Track(),		Helpers.getGridBagConstraint(0, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.NORTHEAST, 0.0, 0.0));
			ID3v1Panel.add(getV1_Track(),		Helpers.getGridBagConstraint(1, 0, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.NORTHWEST, 1.0, 0.0));
			ID3v1Panel.add(getV1_L_Title(),		Helpers.getGridBagConstraint(0, 1, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.NORTHEAST, 0.0, 0.0));
			ID3v1Panel.add(getV1_Title(),		Helpers.getGridBagConstraint(1, 1, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.NORTHWEST, 1.0, 0.0));
			ID3v1Panel.add(getV1_L_Artist(),	Helpers.getGridBagConstraint(0, 2, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.NORTHEAST, 0.0, 0.0));
			ID3v1Panel.add(getV1_Artist(),		Helpers.getGridBagConstraint(1, 2, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.NORTHWEST, 1.0, 0.0));
			ID3v1Panel.add(getV1_L_Album(),		Helpers.getGridBagConstraint(0, 3, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.NORTHEAST, 0.0, 0.0));
			ID3v1Panel.add(getV1_Album(),		Helpers.getGridBagConstraint(1, 3, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.NORTHWEST, 1.0, 0.0));
			ID3v1Panel.add(getV1_L_Year(),		Helpers.getGridBagConstraint(0, 4, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.NORTHEAST, 0.0, 0.0));
			ID3v1Panel.add(getV1_Year(),		Helpers.getGridBagConstraint(1, 4, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.NORTHWEST, 1.0, 0.0));
			ID3v1Panel.add(getV1_L_Genre(),		Helpers.getGridBagConstraint(2, 4, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.NORTHEAST, 0.0, 0.0));
			ID3v1Panel.add(getV1_Genre(),		Helpers.getGridBagConstraint(3, 4, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.NORTHWEST, 1.0, 0.0));
			ID3v1Panel.add(getV1_L_Comment(),	Helpers.getGridBagConstraint(0, 5, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.NORTHEAST, 0.0, 0.0));
			ID3v1Panel.add(getScrollPane_Comment(),		Helpers.getGridBagConstraint(1, 5, 1, 0, java.awt.GridBagConstraints.BOTH, java.awt.GridBagConstraints.NORTHWEST, 1.0, 1.0));
			ID3v1Panel.add(new JPanel(),		Helpers.getGridBagConstraint(1, 5, 1, 0, java.awt.GridBagConstraints.BOTH, java.awt.GridBagConstraints.NORTHEAST, 1.0, 1.0));
		}
		return ID3v1Panel;
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
	private JPanel getID3v2Panel()
	{
		if (ID3v2Panel==null)
		{
			ID3v2Panel = new JPanel();
			ID3v2Panel.setLayout(new java.awt.GridBagLayout());
			
			ID3v2Panel.add(getV2_L_Track(),				Helpers.getGridBagConstraint(0, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.NORTHEAST, 0.0, 0.0));
			ID3v2Panel.add(getV2_Track(),				Helpers.getGridBagConstraint(1, 0, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.NORTHWEST, 1.0, 0.0));
			ID3v2Panel.add(getV2_L_Disc(),				Helpers.getGridBagConstraint(2, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.NORTHEAST, 0.0, 0.0));
			ID3v2Panel.add(getV2_Disc(),				Helpers.getGridBagConstraint(3, 0, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.NORTHWEST, 1.0, 0.0));
			ID3v2Panel.add(getV2_L_URL(),				Helpers.getGridBagConstraint(4, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.NORTHEAST, 0.0, 0.0));
			ID3v2Panel.add(getV2_URL(),					Helpers.getGridBagConstraint(5, 0, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.NORTHWEST, 1.0, 0.0));
			ID3v2Panel.add(getV2_L_Album(),				Helpers.getGridBagConstraint(0, 1, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.NORTHEAST, 0.0, 0.0));
			ID3v2Panel.add(getV2_Album(),				Helpers.getGridBagConstraint(1, 1, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.NORTHWEST, 1.0, 0.0));
			ID3v2Panel.add(getV2_L_Title(),				Helpers.getGridBagConstraint(0, 2, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.NORTHEAST, 0.0, 0.0));
			ID3v2Panel.add(getV2_Title(),				Helpers.getGridBagConstraint(1, 2, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.NORTHWEST, 1.0, 0.0));
			ID3v2Panel.add(getV2_L_Artist(),			Helpers.getGridBagConstraint(0, 3, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.NORTHEAST, 0.0, 0.0));
			ID3v2Panel.add(getV2_Artist(),				Helpers.getGridBagConstraint(1, 3, 1, 3, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.NORTHWEST, 1.0, 0.0));
			ID3v2Panel.add(getV2_L_Copyright(),			Helpers.getGridBagConstraint(4, 3, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.NORTHEAST, 0.0, 0.0));
			ID3v2Panel.add(getV2_Copyright(),			Helpers.getGridBagConstraint(5, 3, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.NORTHWEST, 1.0, 0.0));
			ID3v2Panel.add(getV2_L_Year(),				Helpers.getGridBagConstraint(0, 4, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.NORTHEAST, 0.0, 0.0));
			ID3v2Panel.add(getV2_Year(),				Helpers.getGridBagConstraint(1, 4, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.NORTHWEST, 1.0, 0.0));
			ID3v2Panel.add(getV2_L_Genre(),				Helpers.getGridBagConstraint(2, 4, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.NORTHEAST, 0.0, 0.0));
			ID3v2Panel.add(getV2_Genre(),				Helpers.getGridBagConstraint(3, 4, 1, 2, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.NORTHWEST, 1.0, 0.0));
			ID3v2Panel.add(getV2_L_Encoded(),			Helpers.getGridBagConstraint(5, 4, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.NORTHEAST, 0.0, 0.0));
			ID3v2Panel.add(getV2_Encoded(),				Helpers.getGridBagConstraint(6, 4, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.NORTHWEST, 1.0, 0.0));
			ID3v2Panel.add(getV2_L_BPM(),				Helpers.getGridBagConstraint(7, 4, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.NORTHEAST, 0.0, 0.0));
			ID3v2Panel.add(getV2_BPM(),					Helpers.getGridBagConstraint(8, 4, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.NORTHWEST, 1.0, 0.0));
			ID3v2Panel.add(getV2_L_AlbumArtist(),		Helpers.getGridBagConstraint(0, 5, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.NORTHEAST, 0.0, 0.0));
			ID3v2Panel.add(getV2_AlbumArtist(),			Helpers.getGridBagConstraint(1, 5, 1, 3, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.NORTHWEST, 1.0, 0.0));
			ID3v2Panel.add(getV2_L_OrigArtist(),		Helpers.getGridBagConstraint(4, 5, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.NORTHEAST, 0.0, 0.0));
			ID3v2Panel.add(getV2_OrigArtist(),			Helpers.getGridBagConstraint(5, 5, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.NORTHWEST, 1.0, 0.0));
			ID3v2Panel.add(getV2_L_Composer(),			Helpers.getGridBagConstraint(0, 6, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.NORTHEAST, 0.0, 0.0));
			ID3v2Panel.add(getV2_Composer(),			Helpers.getGridBagConstraint(1, 6, 1, 3, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.NORTHWEST, 1.0, 0.0));
			ID3v2Panel.add(getV2_L_Publisher(),			Helpers.getGridBagConstraint(4, 6, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.NORTHEAST, 0.0, 0.0));
			ID3v2Panel.add(getV2_Publisher(),			Helpers.getGridBagConstraint(5, 6, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.NORTHWEST, 1.0, 0.0));
			ID3v2Panel.add(getV2_L_Comment(),			Helpers.getGridBagConstraint(0, 7, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.NORTHEAST, 0.0, 0.0));
			ID3v2Panel.add(getV2_Comment_Scrollpane(), 	Helpers.getGridBagConstraint(1, 7, 1, 0, java.awt.GridBagConstraints.BOTH, java.awt.GridBagConstraints.NORTHWEST, 1.0, 1.0));
		}
		return ID3v2Panel;
	}
	private javax.swing.JLabel getV2_L_Track()
	{
		if (v2_L_Track==null)
		{
			v2_L_Track = new javax.swing.JLabel();
			v2_L_Track.setName("v2_L_Track");
			v2_L_Track.setText("Track #");
			v2_L_Track.setFont(Helpers.getDialogFont());
		}
		return v2_L_Track;
	}
	private javax.swing.JTextField getV2_Track()
	{
		if (v2_Track==null)
		{
			v2_Track = new javax.swing.JTextField();
			v2_Track.setName("v2_Track");
			v2_Track.setFont(Helpers.getDialogFont());
			v2_Track.setEditable(true);
		}
		return v2_Track;
	}
	private javax.swing.JLabel getV2_L_Disc()
	{
		if (v2_L_Disc==null)
		{
			v2_L_Disc = new javax.swing.JLabel();
			v2_L_Disc.setName("v2_L_Disc");
			v2_L_Disc.setText("Disc #");
			v2_L_Disc.setFont(Helpers.getDialogFont());
		}
		return v2_L_Disc;
	}
	private javax.swing.JTextField getV2_Disc()
	{
		if (v2_Disc==null)
		{
			v2_Disc = new javax.swing.JTextField();
			v2_Disc.setName("v2_Disc");
			v2_Disc.setFont(Helpers.getDialogFont());
			v2_Disc.setEditable(true);
		}
		return v2_Disc;
	}
	private javax.swing.JLabel getV2_L_Title()
	{
		if (v2_L_Title==null)
		{
			v2_L_Title = new javax.swing.JLabel();
			v2_L_Title.setName("v2_L_Title");
			v2_L_Title.setText("Title");
			v2_L_Title.setFont(Helpers.getDialogFont());
		}
		return v2_L_Title;
	}
	private javax.swing.JTextField getV2_Title()
	{
		if (v2_Title==null)
		{
			v2_Title = new javax.swing.JTextField();
			v2_Title.setName("v2_Title");
			v2_Title.setFont(Helpers.getDialogFont());
			v2_Title.setEditable(true);
		}
		return v2_Title;
	}
	private javax.swing.JLabel getV2_L_Artist()
	{
		if (v2_L_Artist==null)
		{
			v2_L_Artist = new javax.swing.JLabel();
			v2_L_Artist.setName("v2_L_Artist");
			v2_L_Artist.setText("Artist");
			v2_L_Artist.setFont(Helpers.getDialogFont());
		}
		return v2_L_Artist;
	}
	private javax.swing.JTextField getV2_Artist()
	{
		if (v2_Artist==null)
		{
			v2_Artist = new javax.swing.JTextField();
			v2_Artist.setName("v2_Artist");
			v2_Artist.setFont(Helpers.getDialogFont());
			v2_Artist.setEditable(true);
		}
		return v2_Artist;
	}
	private javax.swing.JLabel getV2_L_Album()
	{
		if (v2_L_Album==null)
		{
			v2_L_Album = new javax.swing.JLabel();
			v2_L_Album.setName("v2_L_Album");
			v2_L_Album.setText("Album");
			v2_L_Album.setFont(Helpers.getDialogFont());
		}
		return v2_L_Album;
	}
	private javax.swing.JTextField getV2_Album()
	{
		if (v2_Album==null)
		{
			v2_Album = new javax.swing.JTextField();
			v2_Album.setName("v2_Album");
			v2_Album.setFont(Helpers.getDialogFont());
			v2_Album.setEditable(true);
		}
		return v2_Album;
	}
	private javax.swing.JLabel getV2_L_Year()
	{
		if (v2_L_Year==null)
		{
			v2_L_Year = new javax.swing.JLabel();
			v2_L_Year.setName("v2_L_Year");
			v2_L_Year.setText("Year");
			v2_L_Year.setFont(Helpers.getDialogFont());
		}
		return v2_L_Year;
	}
	private javax.swing.JTextField getV2_Year()
	{
		if (v2_Year==null)
		{
			v2_Year = new javax.swing.JTextField();
			v2_Year.setName("v2_Year");
			v2_Year.setFont(Helpers.getDialogFont());
			v2_Year.setEditable(true);
		}
		return v2_Year;
	}
	private javax.swing.JLabel getV2_L_Genre()
	{
		if (v2_L_Genre==null)
		{
			v2_L_Genre = new javax.swing.JLabel();
			v2_L_Genre.setName("v2_L_Genre");
			v2_L_Genre.setText("Genre");
			v2_L_Genre.setFont(Helpers.getDialogFont());
		}
		return v2_L_Genre;
	}
	private javax.swing.JComboBox<String> getV2_Genre()
	{
		if (v2_Genre==null)
		{
			v2_Genre = new javax.swing.JComboBox<String>(NullsoftID3GenreTable.getGenres());
			v2_Genre.setName("v2_Genre");
			v2_Genre.setFont(Helpers.getDialogFont());
			v2_Genre.setEditable(true);
		}
		return v2_Genre;
	}
	private javax.swing.JLabel getV2_L_Comment()
	{
		if (v2_L_Comment==null)
		{
			v2_L_Comment = new javax.swing.JLabel();
			v2_L_Comment.setName("v2_L_Comment");
			v2_L_Comment.setText("Comment");
			v2_L_Comment.setFont(Helpers.getDialogFont());
		}
		return v2_L_Comment;
	}
	private javax.swing.JScrollPane getV2_Comment_Scrollpane()
	{
		if (v2_Comment_Scrollpane == null)
		{
			v2_Comment_Scrollpane = new javax.swing.JScrollPane();
			v2_Comment_Scrollpane.setName("v2_Comment_Scrollpane");
			v2_Comment_Scrollpane.setViewportView(getV2_Comment());
		}
		return v2_Comment_Scrollpane;
	}
	private javax.swing.JTextArea getV2_Comment()
	{
		if (v2_Comment==null)
		{
			v2_Comment = new javax.swing.JTextArea();
			v2_Comment.setName("v2_Comment");
			v2_Comment.setFont(Helpers.getDialogFont());
			v2_Comment.setLineWrap(true);
			v2_Comment.setWrapStyleWord(true);
			v2_Comment.setEditable(true);
		}
		return v2_Comment;
	}
	private javax.swing.JLabel getV2_L_AlbumArtist()
	{
		if (v2_L_AlbumArtist==null)
		{
			v2_L_AlbumArtist = new javax.swing.JLabel();
			v2_L_AlbumArtist.setName("v2_L_AlbumArtist");
			v2_L_AlbumArtist.setText("Album Artist");
			v2_L_AlbumArtist.setFont(Helpers.getDialogFont());
		}
		return v2_L_AlbumArtist;
	}
	private javax.swing.JTextField getV2_AlbumArtist()
	{
		if (v2_AlbumArtist==null)
		{
			v2_AlbumArtist = new javax.swing.JTextField();
			v2_AlbumArtist.setName("v2_AlbumArtist");
			v2_AlbumArtist.setFont(Helpers.getDialogFont());
			v2_AlbumArtist.setEditable(true);
		}
		return v2_AlbumArtist;
	}
	private javax.swing.JLabel getV2_L_Composer()
	{
		if (v2_L_Composer==null)
		{
			v2_L_Composer = new javax.swing.JLabel();
			v2_L_Composer.setName("v2_L_Composer");
			v2_L_Composer.setText("Composer");
			v2_L_Composer.setFont(Helpers.getDialogFont());
		}
		return v2_L_Composer;
	}
	private javax.swing.JTextField getV2_Composer()
	{
		if (v2_Composer==null)
		{
			v2_Composer = new javax.swing.JTextField();
			v2_Composer.setName("v2_Composer");
			v2_Composer.setFont(Helpers.getDialogFont());
			v2_Composer.setEditable(true);
		}
		return v2_Composer;
	}
	private javax.swing.JLabel getV2_L_Publisher()
	{
		if (v2_L_Publisher==null)
		{
			v2_L_Publisher = new javax.swing.JLabel();
			v2_L_Publisher.setName("v2_L_Publisher");
			v2_L_Publisher.setText("Publisher");
			v2_L_Publisher.setFont(Helpers.getDialogFont());
		}
		return v2_L_Publisher;
	}
	private javax.swing.JTextField getV2_Publisher()
	{
		if (v2_Publisher==null)
		{
			v2_Publisher = new javax.swing.JTextField();
			v2_Publisher.setName("v2_Publisher");
			v2_Publisher.setFont(Helpers.getDialogFont());
			v2_Publisher.setEditable(true);
		}
		return v2_Publisher;
	}
	private javax.swing.JLabel getV2_L_OrigArtist()
	{
		if (v2_L_OrigArtist==null)
		{
			v2_L_OrigArtist = new javax.swing.JLabel();
			v2_L_OrigArtist.setName("v2_L_OrigArtist");
			v2_L_OrigArtist.setText("Orig. Artist");
			v2_L_OrigArtist.setFont(Helpers.getDialogFont());
		}
		return v2_L_OrigArtist;
	}
	private javax.swing.JTextField getV2_OrigArtist()
	{
		if (v2_OrigArtist==null)
		{
			v2_OrigArtist = new javax.swing.JTextField();
			v2_OrigArtist.setName("v2_OrigArtist");
			v2_OrigArtist.setFont(Helpers.getDialogFont());
			v2_OrigArtist.setEditable(true);
		}
		return v2_OrigArtist;
	}
	private javax.swing.JLabel getV2_L_Copyright()
	{
		if (v2_L_Copyright==null)
		{
			v2_L_Copyright = new javax.swing.JLabel();
			v2_L_Copyright.setName("v2_L_Copyright");
			v2_L_Copyright.setText("Copyright");
			v2_L_Copyright.setFont(Helpers.getDialogFont());
		}
		return v2_L_Copyright;
	}
	private javax.swing.JTextField getV2_Copyright()
	{
		if (v2_Copyright==null)
		{
			v2_Copyright = new javax.swing.JTextField();
			v2_Copyright.setName("v2_Copyright");
			v2_Copyright.setFont(Helpers.getDialogFont());
			v2_Copyright.setEditable(true);
		}
		return v2_Copyright;
	}
	private javax.swing.JLabel getV2_L_URL()
	{
		if (v2_L_URL==null)
		{
			v2_L_URL = new javax.swing.JLabel();
			v2_L_URL.setName("v2_L_URL");
			v2_L_URL.setText("URL");
			v2_L_URL.setFont(Helpers.getDialogFont());
		}
		return v2_L_URL;
	}
	private javax.swing.JTextField getV2_URL()
	{
		if (v2_URL==null)
		{
			v2_URL = new javax.swing.JTextField();
			v2_URL.setName("v2_URL");
			v2_URL.setFont(Helpers.getDialogFont());
			v2_URL.setEditable(true);
		}
		return v2_URL;
	}
	private javax.swing.JLabel getV2_L_Encoded()
	{
		if (v2_L_Encoded==null)
		{
			v2_L_Encoded = new javax.swing.JLabel();
			v2_L_Encoded.setName("v2_L_Encoded");
			v2_L_Encoded.setText("Encoded");
			v2_L_Encoded.setFont(Helpers.getDialogFont());
		}
		return v2_L_Encoded;
	}
	private javax.swing.JTextField getV2_Encoded()
	{
		if (v2_Encoded==null)
		{
			v2_Encoded = new javax.swing.JTextField();
			v2_Encoded.setName("v2_Encoded");
			v2_Encoded.setFont(Helpers.getDialogFont());
			v2_Encoded.setEditable(true);
		}
		return v2_Encoded;
	}
	private javax.swing.JLabel getV2_L_BPM()
	{
		if (v2_L_BPM==null)
		{
			v2_L_BPM = new javax.swing.JLabel();
			v2_L_BPM.setName("v2_L_BPM");
			v2_L_BPM.setText("BPM");
			v2_L_BPM.setFont(Helpers.getDialogFont());
		}
		return v2_L_BPM;
	}
	private javax.swing.JTextField getV2_BPM()
	{
		if (v2_BPM==null)
		{
			v2_BPM = new javax.swing.JTextField();
			v2_BPM.setName("v2_BPM");
			v2_BPM.setFont(Helpers.getDialogFont());
			v2_BPM.setEditable(true);
		}
		return v2_BPM;
	}
	public void fillInfoPanelWith(Header h, MP3FileID3Controller currentID)
	{
		getMP3Info_Filename().setText(currentID.getFileName());
		getMP3Info_ShortDescription().setText(currentID.getShortDescription());
		
		getmp3Info_Frequency().setText(h.sample_frequency_string());
		getmp3Info_SampleSizeInBits().setText("16");
		getmp3Info_Channels().setText(h.mode_string());
		getmp3Info_Encoding().setText(h.version_string()+" Layer "+h.layer_string());
		getmp3Info_BitRate().setText(h.bitrate_string());
		
		if (currentID.id3v1Exists())
		{
			getV1_Track().setEnabled(true);
			getV1_Track().setText(currentID.getTrack(MP3FileID3Controller.ID3V1));
			getV1_Title().setEnabled(true);
			getV1_Title().setText(currentID.getTitle(MP3FileID3Controller.ID3V1));
			getV1_Artist().setEnabled(true);
			getV1_Artist().setText(currentID.getArtist(MP3FileID3Controller.ID3V1));
			getV1_Album().setEnabled(true);
			getV1_Album().setText(currentID.getAlbum(MP3FileID3Controller.ID3V1));
			getV1_Year().setEnabled(true);
			getV1_Year().setText(currentID.getYear(MP3FileID3Controller.ID3V1));
			getV1_Genre().setEnabled(true);
			getV1_Genre().setSelectedItem(currentID.getGenre(MP3FileID3Controller.ID3V1));
			getV1_Comment().setEnabled(true);
			getV1_Comment().setText(currentID.getComment(MP3FileID3Controller.ID3V1));
			getV1_Comment().select(0, 0);
		}
		else
		{
			getV1_Track().setEnabled(false);
			getV1_Track().setText(EMPTY_STRING);
			getV1_Title().setEnabled(false);
			getV1_Title().setText(EMPTY_STRING);
			getV1_Artist().setEnabled(false);
			getV1_Artist().setText(EMPTY_STRING);
			getV1_Album().setEnabled(false);
			getV1_Album().setText(EMPTY_STRING);
			getV1_Year().setEnabled(false);
			getV1_Year().setText(EMPTY_STRING);
			getV1_Genre().setEnabled(false);
			getV1_Genre().setSelectedItem(EMPTY_STRING);
			getV1_Comment().setEnabled(false);
			getV1_Comment().setText(EMPTY_STRING);
		}

		if (currentID.id3v2Exists())
		{
			getV2_Track().setEnabled(true);
			getV2_Track().setText(currentID.getTrack(MP3FileID3Controller.ID3V2));
			getV2_Disc().setEnabled(true);
			getV2_Disc().setText(currentID.getDisc());
			getV2_Title().setEnabled(true);
			getV2_Title().setText(currentID.getTitle(MP3FileID3Controller.ID3V2));
			getV2_Artist().setEnabled(true);
			getV2_Artist().setText(currentID.getArtist(MP3FileID3Controller.ID3V2));
			getV2_Album().setEnabled(true);
			getV2_Album().setText(currentID.getAlbum(MP3FileID3Controller.ID3V2));
			getV2_Year().setEnabled(true);
			getV2_Year().setText(currentID.getYear(MP3FileID3Controller.ID3V2));
			getV2_Genre().setEnabled(true);
			getV2_Genre().setSelectedItem(currentID.getGenre(MP3FileID3Controller.ID3V2));
			getV2_Comment().setEnabled(true);
			getV2_Comment().setText(currentID.getComment(MP3FileID3Controller.ID3V2));
			getV2_Comment().select(0, 0);
			getV2_AlbumArtist().setEnabled(true);
			getV2_AlbumArtist().setText(currentID.getAlbumArtist());
			getV2_Composer().setEnabled(true);
			getV2_Composer().setText(currentID.getComposer());
			getV2_Publisher().setEnabled(true);
			getV2_Publisher().setText(currentID.getPublisher());
			getV2_OrigArtist().setEnabled(true);
			getV2_OrigArtist().setText(currentID.getOriginalArtist());
			getV2_Copyright().setEnabled(true);
			getV2_Copyright().setText(currentID.getCopyrightInfo());
			getV2_URL().setEnabled(true);
			getV2_URL().setText(currentID.getUserDefinedURL());
			getV2_Encoded().setEnabled(true);
			getV2_Encoded().setText(currentID.getEncodedBy());
			getV2_BPM().setEnabled(true);
			getV2_BPM().setText(currentID.getBPM());
		}
		else
		{
			getV2_Track().setEnabled(false);
			getV2_Track().setText(EMPTY_STRING);
			getV2_Disc().setEnabled(false);
			getV2_Disc().setText(EMPTY_STRING);
			getV2_Title().setEnabled(false);
			getV2_Title().setText(EMPTY_STRING);
			getV2_Artist().setEnabled(false);
			getV2_Artist().setText(EMPTY_STRING);
			getV2_Album().setEnabled(false);
			getV2_Album().setText(EMPTY_STRING);
			getV2_Year().setEnabled(false);
			getV2_Year().setText(EMPTY_STRING);
			getV2_Genre().setEnabled(false);
			getV2_Genre().setSelectedItem(EMPTY_STRING);
			getV2_Comment().setEnabled(false);
			getV2_Comment().setText(EMPTY_STRING);
			getV2_AlbumArtist().setEnabled(false);
			getV2_AlbumArtist().setText(EMPTY_STRING);
			getV2_Composer().setEnabled(false);
			getV2_Composer().setText(EMPTY_STRING);
			getV2_Publisher().setEnabled(false);
			getV2_Publisher().setText(EMPTY_STRING);
			getV2_OrigArtist().setEnabled(false);
			getV2_OrigArtist().setText(EMPTY_STRING);
			getV2_Copyright().setEnabled(false);
			getV2_Copyright().setText(EMPTY_STRING);
			getV2_URL().setEnabled(false);
			getV2_URL().setText(EMPTY_STRING);
			getV2_Encoded().setEnabled(false);
			getV2_Encoded().setText(EMPTY_STRING);
			getV2_BPM().setEnabled(false);
			getV2_BPM().setText(EMPTY_STRING);
		}
		getTabbedPane().setSelectedIndex(currentID.id3v2Exists()?1:0);
	}
}
