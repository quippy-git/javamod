/*
 * @(#) APEInfoPanel.java
 *
 * Created on 23.12.2010 by Daniel Becker
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
package de.quippy.javamod.multimedia.ape;

import java.awt.LayoutManager;
import java.io.IOException;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.quippy.javamod.system.Helpers;
import de.quippy.jmac.decoder.IAPEDecompress;
import de.quippy.jmac.info.APETag;
import de.quippy.jmac.info.ID3Genre;

/**
 * @author Daniel Becker
 * @since 23.12.2010
 */
public class APEInfoPanel extends JPanel
{
	private static final long serialVersionUID = 7064938824632940420L;

	private JLabel apeInfo_L_Filename = null;
	private JTextField apeInfo_Filename = null;
	private JLabel apeInfo_L_ShortDescription = null;
	private JTextField apeInfo_ShortDescription = null;
	private JLabel apeInfo_SampleSizeInBitsLabel = null;
	private JTextField apeInfo_SampleSizeInBits = null;
	private JLabel apeInfo_FrequencyLabel = null;
	private JTextField apeInfo_Frequency = null;
	private JLabel apeInfo_ChannelsLabel = null;
	private JTextField apeInfo_Channels = null;
	private JLabel apeInfo_AvgBitRateLabel = null;
	private JTextField apeInfo_AvgBitRate = null;
	private JLabel apeInfo_CompressionLevelLabel = null;
	private JTextField apeInfo_CompressionLevel = null;
	private JLabel apeInfo_VersionInfoLabel = null;
	private JTextField apeInfo_VersionInfo = null;

	private JPanel APEIDPanel = null;
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
	private JLabel v1_L_Track = null;
	private JTextField v1_Track = null;

	/**
	 * Constructor for APEInfoPanel
	 */
	public APEInfoPanel()
	{
		super();
		initialize();
	}
	/**
	 * Constructor for APEInfoPanel
	 * @param layout
	 */
	public APEInfoPanel(LayoutManager layout)
	{
		super(layout);
		initialize();
	}
	/**
	 * Constructor for APEInfoPanel
	 * @param isDoubleBuffered
	 */
	public APEInfoPanel(boolean isDoubleBuffered)
	{
		super(isDoubleBuffered);
		initialize();
	}
	/**
	 * Constructor for APEInfoPanel
	 * @param layout
	 * @param isDoubleBuffered
	 */
	public APEInfoPanel(LayoutManager layout, boolean isDoubleBuffered)
	{
		super(layout, isDoubleBuffered);
		initialize();
	}
	private void initialize()
	{
		this.setName("APEInfoPane");
		this.setLayout(new java.awt.GridBagLayout());
		
		this.add(getAPEInfo_L_Filename(),			Helpers.getGridBagConstraint(0, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getAPEInfo_Filename(),				Helpers.getGridBagConstraint(1, 0, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		this.add(getAPEInfo_L_ShortDescription(),	Helpers.getGridBagConstraint(0, 1, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getAPEInfo_ShortDescription(),		Helpers.getGridBagConstraint(1, 1, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		this.add(getAPEInfo_FrequencyLabel(),		Helpers.getGridBagConstraint(0, 2, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getAPEInfo_Frequency(),			Helpers.getGridBagConstraint(1, 2, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		this.add(getAPEInfo_SampleSizeInBitsLabel(),Helpers.getGridBagConstraint(2, 2, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getAPEInfo_SampleSizeInBits(),		Helpers.getGridBagConstraint(3, 2, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		this.add(getAPEInfo_ChannelsLabel(),		Helpers.getGridBagConstraint(4, 2, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getAPEInfo_Channels(),				Helpers.getGridBagConstraint(5, 2, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		this.add(getAPEInfo_AvgBitRateLabel(),		Helpers.getGridBagConstraint(0, 3, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getAPEInfo_AvgBitRate(),			Helpers.getGridBagConstraint(1, 3, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		this.add(getAPEInfo_CompressionLevelLabel(),Helpers.getGridBagConstraint(2, 3, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getAPEInfo_CompressionLevel(),		Helpers.getGridBagConstraint(3, 3, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		this.add(getAPEInfo_VersionInfoLabel(),		Helpers.getGridBagConstraint(4, 3, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		this.add(getAPEInfo_VersionInfo(),			Helpers.getGridBagConstraint(5, 3, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		this.add(getAPEIDPanel(),					Helpers.getGridBagConstraint(0, 4, 1, 0, java.awt.GridBagConstraints.BOTH, java.awt.GridBagConstraints.WEST, 1.0, 1.0));
	}
	private javax.swing.JLabel getAPEInfo_L_Filename()
	{
		if (apeInfo_L_Filename==null)
		{
			apeInfo_L_Filename = new javax.swing.JLabel();
			apeInfo_L_Filename.setName("apeInfo_L_Filename");
			apeInfo_L_Filename.setText("File");
			apeInfo_L_Filename.setFont(Helpers.getDialogFont());
		}
		return apeInfo_L_Filename;
	}
	private javax.swing.JTextField getAPEInfo_Filename()
	{
		if (apeInfo_Filename==null)
		{
			apeInfo_Filename = new javax.swing.JTextField();
			apeInfo_Filename.setName("apeInfo_Filename");
			apeInfo_Filename.setFont(Helpers.getDialogFont());
			apeInfo_Filename.setEditable(false);
		}
		return apeInfo_Filename;
	}
	private javax.swing.JLabel getAPEInfo_L_ShortDescription()
	{
		if (apeInfo_L_ShortDescription==null)
		{
			apeInfo_L_ShortDescription = new javax.swing.JLabel();
			apeInfo_L_ShortDescription.setName("apeInfo_L_ShortDescription");
			apeInfo_L_ShortDescription.setText("Name");
			apeInfo_L_ShortDescription.setFont(Helpers.getDialogFont());
		}
		return apeInfo_L_ShortDescription;
	}
	public javax.swing.JTextField getAPEInfo_ShortDescription()
	{
		if (apeInfo_ShortDescription==null)
		{
			apeInfo_ShortDescription = new javax.swing.JTextField();
			apeInfo_ShortDescription.setName("apeInfo_ShortDescription");
			apeInfo_ShortDescription.setFont(Helpers.getDialogFont());
			apeInfo_ShortDescription.setEditable(false);
		}
		return apeInfo_ShortDescription;
	}
	private javax.swing.JLabel getAPEInfo_FrequencyLabel()
	{
		if (apeInfo_FrequencyLabel==null)
		{
			apeInfo_FrequencyLabel = new JLabel("Rate:");
			apeInfo_FrequencyLabel.setFont(Helpers.getDialogFont());
		}
		return apeInfo_FrequencyLabel;
	}
	private JTextField getAPEInfo_Frequency()
	{
		if (apeInfo_Frequency==null)
		{
			apeInfo_Frequency = new javax.swing.JTextField();
			apeInfo_Frequency.setEditable(false);
			apeInfo_Frequency.setFont(Helpers.getDialogFont());
		}
		return apeInfo_Frequency;
	}
	private javax.swing.JLabel getAPEInfo_SampleSizeInBitsLabel()
	{
		if (apeInfo_SampleSizeInBitsLabel==null)
		{
			apeInfo_SampleSizeInBitsLabel = new JLabel("Bits:");
			apeInfo_SampleSizeInBitsLabel.setFont(Helpers.getDialogFont());
		}
		return apeInfo_SampleSizeInBitsLabel;
	}
	private JTextField getAPEInfo_SampleSizeInBits()
	{
		if (apeInfo_SampleSizeInBits==null)
		{
			apeInfo_SampleSizeInBits = new javax.swing.JTextField();
			apeInfo_SampleSizeInBits.setEditable(false);
			apeInfo_SampleSizeInBits.setFont(Helpers.getDialogFont());
		}
		return apeInfo_SampleSizeInBits;
	}
	private javax.swing.JLabel getAPEInfo_ChannelsLabel()
	{
		if (apeInfo_ChannelsLabel==null)
		{
			apeInfo_ChannelsLabel = new JLabel("Channel:");
			apeInfo_ChannelsLabel.setFont(Helpers.getDialogFont());
		}
		return apeInfo_ChannelsLabel;
	}
	private JTextField getAPEInfo_Channels()
	{
		if (apeInfo_Channels==null)
		{
			apeInfo_Channels = new javax.swing.JTextField();
			apeInfo_Channels.setEditable(false);
			apeInfo_Channels.setFont(Helpers.getDialogFont());
		}
		return apeInfo_Channels;
	}
	private javax.swing.JLabel getAPEInfo_AvgBitRateLabel()
	{
		if (apeInfo_AvgBitRateLabel==null)
		{
			apeInfo_AvgBitRateLabel = new JLabel("Avg. Bitrate:");
			apeInfo_AvgBitRateLabel.setFont(Helpers.getDialogFont());
		}
		return apeInfo_AvgBitRateLabel;
	}
	private JTextField getAPEInfo_AvgBitRate()
	{
		if (apeInfo_AvgBitRate==null)
		{
			apeInfo_AvgBitRate = new javax.swing.JTextField();
			apeInfo_AvgBitRate.setEditable(false);
			apeInfo_AvgBitRate.setFont(Helpers.getDialogFont());
		}
		return apeInfo_AvgBitRate;
	}
	private javax.swing.JLabel getAPEInfo_CompressionLevelLabel()
	{
		if (apeInfo_CompressionLevelLabel==null)
		{
			apeInfo_CompressionLevelLabel = new JLabel("Compression Level:");
			apeInfo_CompressionLevelLabel.setFont(Helpers.getDialogFont());
		}
		return apeInfo_CompressionLevelLabel;
	}
	private JTextField getAPEInfo_CompressionLevel()
	{
		if (apeInfo_CompressionLevel==null)
		{
			apeInfo_CompressionLevel = new javax.swing.JTextField();
			apeInfo_CompressionLevel.setEditable(false);
			apeInfo_CompressionLevel.setFont(Helpers.getDialogFont());
		}
		return apeInfo_CompressionLevel;
	}
	private javax.swing.JLabel getAPEInfo_VersionInfoLabel()
	{
		if (apeInfo_VersionInfoLabel==null)
		{
			apeInfo_VersionInfoLabel = new JLabel("Version:");
			apeInfo_VersionInfoLabel.setFont(Helpers.getDialogFont());
		}
		return apeInfo_VersionInfoLabel;
	}
	private JTextField getAPEInfo_VersionInfo()
	{
		if (apeInfo_VersionInfo==null)
		{
			apeInfo_VersionInfo = new javax.swing.JTextField();
			apeInfo_VersionInfo.setEditable(false);
			apeInfo_VersionInfo.setFont(Helpers.getDialogFont());
		}
		return apeInfo_VersionInfo;
	}
	private JPanel getAPEIDPanel()
	{
		if (APEIDPanel==null)
		{
			APEIDPanel = new JPanel();
			APEIDPanel.setLayout(new java.awt.GridBagLayout());
			
			APEIDPanel.add(getV1_L_Track(),		Helpers.getGridBagConstraint(0, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.NORTHEAST, 0.0, 0.0));
			APEIDPanel.add(getV1_Track(),		Helpers.getGridBagConstraint(1, 0, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.NORTHWEST, 1.0, 0.0));
			APEIDPanel.add(getV1_L_Title(),		Helpers.getGridBagConstraint(0, 1, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.NORTHEAST, 0.0, 0.0));
			APEIDPanel.add(getV1_Title(),		Helpers.getGridBagConstraint(1, 1, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.NORTHWEST, 1.0, 0.0));
			APEIDPanel.add(getV1_L_Artist(),	Helpers.getGridBagConstraint(0, 2, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.NORTHEAST, 0.0, 0.0));
			APEIDPanel.add(getV1_Artist(),		Helpers.getGridBagConstraint(1, 2, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.NORTHWEST, 1.0, 0.0));
			APEIDPanel.add(getV1_L_Album(),		Helpers.getGridBagConstraint(0, 3, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.NORTHEAST, 0.0, 0.0));
			APEIDPanel.add(getV1_Album(),		Helpers.getGridBagConstraint(1, 3, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.NORTHWEST, 1.0, 0.0));
			APEIDPanel.add(getV1_L_Year(),		Helpers.getGridBagConstraint(0, 4, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.NORTHEAST, 0.0, 0.0));
			APEIDPanel.add(getV1_Year(),		Helpers.getGridBagConstraint(1, 4, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.NORTHWEST, 1.0, 0.0));
			APEIDPanel.add(getV1_L_Genre(),		Helpers.getGridBagConstraint(2, 4, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.NORTHEAST, 0.0, 0.0));
			APEIDPanel.add(getV1_Genre(),		Helpers.getGridBagConstraint(3, 4, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.NORTHWEST, 1.0, 0.0));
			APEIDPanel.add(new JPanel(),		Helpers.getGridBagConstraint(1, 5, 1, 0, java.awt.GridBagConstraints.BOTH, java.awt.GridBagConstraints.NORTHEAST, 1.0, 1.0));
		}
		return APEIDPanel;
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
			v1_Genre = new javax.swing.JComboBox<String>(ID3Genre.getGenres());
			v1_Genre.setName("v1_Genre");
			v1_Genre.setFont(Helpers.getDialogFont());
			v1_Genre.setEditable(true);
		}
		return v1_Genre;
	}
	public void fillInfoPanelWith(final IAPEDecompress spAPEDecompress, final String fileName, final String songName)
	{
		getAPEInfo_Filename().setText(fileName);
		getAPEInfo_ShortDescription().setText(songName);
		getAPEInfo_Channels().setText(Integer.toString(spAPEDecompress.getApeInfoChannels()));
		getAPEInfo_SampleSizeInBits().setText(Integer.toString(spAPEDecompress.getApeInfoBitsPerSample()));
		getAPEInfo_Frequency().setText(Integer.toString(spAPEDecompress.getApeInfoSampleRate()));
		getAPEInfo_AvgBitRate().setText(Integer.toString(spAPEDecompress.getApeInfoAverageBitrate()));
		getAPEInfo_CompressionLevel().setText(Integer.toString(spAPEDecompress.getApeInfoCompressionLevel()));
		final int apeVersion = spAPEDecompress.getApeInfoFileVersion();
		final int v1 = apeVersion / 1000;
		final int v2 = (apeVersion-(v1*1000)) / 10;
		final int v3 = (apeVersion-(v1*1000)-(v2*10));
		
		getAPEInfo_VersionInfo().setText(v1+"."+v2+"."+v3);
		
		final APETag apeTag = spAPEDecompress.getApeInfoTag();
		try
		{
			getV1_Track().setText(apeTag.GetFieldString(APETag.APE_TAG_FIELD_TRACK));
			getV1_Title().setText(apeTag.GetFieldString(APETag.APE_TAG_FIELD_TITLE));
			getV1_Artist().setText(apeTag.GetFieldString(APETag.APE_TAG_FIELD_ARTIST));
			getV1_Album().setText(apeTag.GetFieldString(APETag.APE_TAG_FIELD_ALBUM));
			getV1_Year().setText(apeTag.GetFieldString(APETag.APE_TAG_FIELD_YEAR));
			getV1_Genre().setSelectedItem(apeTag.GetFieldString(APETag.APE_TAG_FIELD_GENRE));
		}
		catch (IOException ex)
		{
		}
	}
}
