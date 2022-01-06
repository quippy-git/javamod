/*
 * @(#) FLACInfoPanel.java
 *
 * Created on 17.02.2011 by Daniel Becker
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
package de.quippy.javamod.multimedia.flac;

import java.awt.LayoutManager;

import javax.sound.sampled.AudioFormat;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import de.quippy.javamod.multimedia.mp3.id3.NullsoftID3GenreTable;
import de.quippy.javamod.system.Helpers;
import de.quippy.jflac.metadata.VorbisComment;

/**
 * @author Daniel Becker
 * @since 17.02.2011
 */
public class FLACInfoPanel extends JPanel
{
	private static final long serialVersionUID = 8005898124149359343L;

	private JLabel flacFileNameLabel = null;
	private JTextField flacFileName = null;
	private JLabel flacL_ShortDescription = null;
	private JTextField flacShortDescription = null;
	private JLabel flacDurationLabel = null;
	private JTextField flacDuration = null;
	private JLabel flacSampleSizeInBitsLabel = null;
	private JTextField flacSampleSizeInBits = null;
	private JLabel flacFrequencyLabel = null;
	private JTextField flacFrequency = null;
	private JLabel flacChannelsLabel = null;
	private JTextField flacChannels = null;
	private JLabel flacEncodingLabel = null;
	private JTextField flacEncoding = null;

	private JPanel flacIDPanel = null;
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
	 * Constructor for FLACInfoPanel
	 */
	public FLACInfoPanel()
	{
		super();
		initialize();
	}
	/**
	 * Constructor for FLACInfoPanel
	 * @param layout
	 */
	public FLACInfoPanel(LayoutManager layout)
	{
		super(layout);
		initialize();
	}
	/**
	 * Constructor for FLACInfoPanel
	 * @param isDoubleBuffered
	 */
	public FLACInfoPanel(boolean isDoubleBuffered)
	{
		super(isDoubleBuffered);
		initialize();
	}
	/**
	 * Constructor for FLACInfoPanel
	 * @param layout
	 * @param isDoubleBuffered
	 */
	public FLACInfoPanel(LayoutManager layout, boolean isDoubleBuffered)
	{
		super(layout, isDoubleBuffered);
		initialize();
	}
	private void initialize()
	{
		this.setName("FlacInfoPane");
		this.setLayout(new java.awt.GridBagLayout());
		this.add(getFlacFileNameLabel(), 		Helpers.getGridBagConstraint(0, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getFlacFileName(), 			Helpers.getGridBagConstraint(1, 0, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		this.add(getFlacShortDescriptionLabel(),Helpers.getGridBagConstraint(0, 1, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getFlacShortDescription(),		Helpers.getGridBagConstraint(1, 1, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		this.add(getFlacFrequencyLabel(),		Helpers.getGridBagConstraint(0, 2, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getFlacFrequency(),			Helpers.getGridBagConstraint(1, 2, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		this.add(getFlacSampleSizeInBitsLabel(),Helpers.getGridBagConstraint(2, 2, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getFlacSampleSizeInBits(),		Helpers.getGridBagConstraint(3, 2, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		this.add(getFlacChannelsLabel(),		Helpers.getGridBagConstraint(4, 2, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getFlacChannels(),				Helpers.getGridBagConstraint(5, 2, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		this.add(getFlacEncodingLabel(),		Helpers.getGridBagConstraint(0, 3, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getFlacEncoding(),				Helpers.getGridBagConstraint(1, 3, 1, 3, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		this.add(getFlacDurationLabel(),		Helpers.getGridBagConstraint(4, 3, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getFlacDuration(),				Helpers.getGridBagConstraint(5, 3, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		this.add(getFlacIDPanel(),				Helpers.getGridBagConstraint(0, 4, 1, 0, java.awt.GridBagConstraints.BOTH, java.awt.GridBagConstraints.WEST, 1.0, 1.0));
	}
	private javax.swing.JLabel getFlacFileNameLabel()
	{
		if (flacFileNameLabel==null)
		{
			flacFileNameLabel = new JLabel("Flac file name:");
			flacFileNameLabel.setFont(Helpers.getDialogFont());
		}
		return flacFileNameLabel;
	}
	private JTextField getFlacFileName()
	{
		if (flacFileName==null)
		{
			flacFileName = new javax.swing.JTextField();
			flacFileName.setEditable(false);
			flacFileName.setFont(Helpers.getDialogFont());
		}
		return flacFileName;
	}
	private javax.swing.JLabel getFlacShortDescriptionLabel()
	{
		if (flacL_ShortDescription==null)
		{
			flacL_ShortDescription = new javax.swing.JLabel();
			flacL_ShortDescription.setName("flacL_ShortDescription");
			flacL_ShortDescription.setText("Name");
			flacL_ShortDescription.setFont(Helpers.getDialogFont());
		}
		return flacL_ShortDescription;
	}
	private javax.swing.JTextField getFlacShortDescription()
	{
		if (flacShortDescription==null)
		{
			flacShortDescription = new javax.swing.JTextField();
			flacShortDescription.setName("flacShortDescription");
			flacShortDescription.setFont(Helpers.getDialogFont());
			flacShortDescription.setEditable(false);
		}
		return flacShortDescription;
	}
	private javax.swing.JLabel getFlacDurationLabel()
	{
		if (flacDurationLabel==null)
		{
			flacDurationLabel = new JLabel("Duration:");
			flacDurationLabel.setFont(Helpers.getDialogFont());
		}
		return flacDurationLabel;
	}
	private JTextField getFlacDuration()
	{
		if (flacDuration==null)
		{
			flacDuration = new javax.swing.JTextField();
			flacDuration.setEditable(false);
			flacDuration.setFont(Helpers.getDialogFont());
		}
		return flacDuration;
	}
	private javax.swing.JLabel getFlacFrequencyLabel()
	{
		if (flacFrequencyLabel==null)
		{
			flacFrequencyLabel = new JLabel("Rate:");
			flacFrequencyLabel.setFont(Helpers.getDialogFont());
		}
		return flacFrequencyLabel;
	}
	private JTextField getFlacFrequency()
	{
		if (flacFrequency==null)
		{
			flacFrequency = new javax.swing.JTextField();
			flacFrequency.setEditable(false);
			flacFrequency.setFont(Helpers.getDialogFont());
		}
		return flacFrequency;
	}
	private javax.swing.JLabel getFlacSampleSizeInBitsLabel()
	{
		if (flacSampleSizeInBitsLabel==null)
		{
			flacSampleSizeInBitsLabel = new JLabel("Bits:");
			flacSampleSizeInBitsLabel.setFont(Helpers.getDialogFont());
		}
		return flacSampleSizeInBitsLabel;
	}
	private JTextField getFlacSampleSizeInBits()
	{
		if (flacSampleSizeInBits==null)
		{
			flacSampleSizeInBits = new javax.swing.JTextField();
			flacSampleSizeInBits.setEditable(false);
			flacSampleSizeInBits.setFont(Helpers.getDialogFont());
		}
		return flacSampleSizeInBits;
	}
	private javax.swing.JLabel getFlacChannelsLabel()
	{
		if (flacChannelsLabel==null)
		{
			flacChannelsLabel = new JLabel("Channel:");
			flacChannelsLabel.setFont(Helpers.getDialogFont());
		}
		return flacChannelsLabel;
	}
	private JTextField getFlacChannels()
	{
		if (flacChannels==null)
		{
			flacChannels = new javax.swing.JTextField();
			flacChannels.setEditable(false);
			flacChannels.setFont(Helpers.getDialogFont());
		}
		return flacChannels;
	}
	private javax.swing.JLabel getFlacEncodingLabel()
	{
		if (flacEncodingLabel==null)
		{
			flacEncodingLabel = new JLabel("Encoding:");
			flacEncodingLabel.setFont(Helpers.getDialogFont());
		}
		return flacEncodingLabel;
	}
	private JTextField getFlacEncoding()
	{
		if (flacEncoding==null)
		{
			flacEncoding = new javax.swing.JTextField();
			flacEncoding.setEditable(false);
			flacEncoding.setFont(Helpers.getDialogFont());
		}
		return flacEncoding;
	}
	private JPanel getFlacIDPanel()
	{
		if (flacIDPanel==null)
		{
			flacIDPanel = new JPanel();
			flacIDPanel.setLayout(new java.awt.GridBagLayout());
			
			flacIDPanel.add(getV1_L_Track(),		Helpers.getGridBagConstraint(0, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.NORTHEAST, 0.0, 0.0));
			flacIDPanel.add(getV1_Track(),			Helpers.getGridBagConstraint(1, 0, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.NORTHWEST, 1.0, 0.0));
			flacIDPanel.add(getV1_L_Title(),		Helpers.getGridBagConstraint(0, 1, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.NORTHEAST, 0.0, 0.0));
			flacIDPanel.add(getV1_Title(),			Helpers.getGridBagConstraint(1, 1, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.NORTHWEST, 1.0, 0.0));
			flacIDPanel.add(getV1_L_Artist(),		Helpers.getGridBagConstraint(0, 2, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.NORTHEAST, 0.0, 0.0));
			flacIDPanel.add(getV1_Artist(),			Helpers.getGridBagConstraint(1, 2, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.NORTHWEST, 1.0, 0.0));
			flacIDPanel.add(getV1_L_Album(),		Helpers.getGridBagConstraint(0, 3, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.NORTHEAST, 0.0, 0.0));
			flacIDPanel.add(getV1_Album(),			Helpers.getGridBagConstraint(1, 3, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.NORTHWEST, 1.0, 0.0));
			flacIDPanel.add(getV1_L_Year(),			Helpers.getGridBagConstraint(0, 4, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.NORTHEAST, 0.0, 0.0));
			flacIDPanel.add(getV1_Year(),			Helpers.getGridBagConstraint(1, 4, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.NORTHWEST, 1.0, 0.0));
			flacIDPanel.add(getV1_L_Genre(),		Helpers.getGridBagConstraint(2, 4, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.NORTHEAST, 0.0, 0.0));
			flacIDPanel.add(getV1_Genre(),			Helpers.getGridBagConstraint(3, 4, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.NORTHWEST, 1.0, 0.0));
			flacIDPanel.add(getV1_L_Comment(),		Helpers.getGridBagConstraint(0, 5, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.NORTHEAST, 0.0, 0.0));
			flacIDPanel.add(getScrollPane_Comment(),Helpers.getGridBagConstraint(1, 5, 1, 0, java.awt.GridBagConstraints.BOTH, java.awt.GridBagConstraints.NORTHWEST, 1.0, 1.0));
			flacIDPanel.add(new JPanel(),			Helpers.getGridBagConstraint(1, 5, 1, 0, java.awt.GridBagConstraints.BOTH, java.awt.GridBagConstraints.NORTHEAST, 1.0, 1.0));
		}
		return flacIDPanel;
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
	public void fillInfoPanelWith(final AudioFormat audioFormat, final long lengthInMilliseconds, final String fileName, final String songName, final VorbisComment vorbisComment)
	{
		getFlacFileName().setText(fileName);
		getFlacShortDescription().setText(songName);
		getFlacFrequency().setText(Integer.toString((int)audioFormat.getSampleRate()));
		getFlacSampleSizeInBits().setText(Integer.toString(audioFormat.getSampleSizeInBits()));
		getFlacChannels().setText(Integer.toString(audioFormat.getChannels()));
		getFlacEncoding().setText(audioFormat.getEncoding().toString());
		getFlacDuration().setText(Helpers.getTimeStringFromMilliseconds(lengthInMilliseconds));
		
		if (vorbisComment!=null)
		{
			// BAND, ALBUMARTIST, COMPOSER
			StringBuilder sb = new StringBuilder();
			String track = vorbisComment.getTrackNumber();
			if (track.length()>0) sb.append("Track ").append(track);
			String totalTracks = vorbisComment.getTotalTracks();
			if (totalTracks.length()>0) sb.append(" of ").append(totalTracks);
			String disc = vorbisComment.getDiscNumber();
			if (disc.length()>0) sb.append(" Disc ").append(disc);
			String totalDiscs = vorbisComment.getTotalDiscs();
			if (totalDiscs.length()>0) sb.append(" of ").append(totalDiscs);
			getV1_Track().setText(sb.toString());

			getV1_Title().setText(vorbisComment.getTitle());
			getV1_Artist().setText(vorbisComment.getArtist());
			getV1_Album().setText(vorbisComment.getAlbum());
			getV1_Year().setText(vorbisComment.getDate());
			getV1_Genre().setSelectedItem(vorbisComment.getGenre());
			getV1_Comment().setText(vorbisComment.getComment());
			getV1_Comment().select(0, 0);
		}
	}
}
