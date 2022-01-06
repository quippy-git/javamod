/*
 * @(#) MP3StreamInfoPanel.java
 *
 * Created on 27.12.2008 by Daniel Becker
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
import java.util.Collection;
import java.util.HashMap;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.quippy.javamod.multimedia.mp3.streaming.IcyTag;
import de.quippy.javamod.system.Helpers;

/**
 * @author Daniel Becker
 * @since 27.12.2008
 */
public class MP3StreamInfoPanel extends JPanel
{
	private static final long serialVersionUID = 2105268194816135760L;

	private JLabel mp3Info_L_ServerType = null;
	private JTextField mp3Info_ServerType = null;
	private JLabel mp3Info_L_ServerName = null;
	private JTextField mp3Info_ServerName = null;
	private JLabel mp3Info_L_ServerDesc = null;
	private JTextField mp3Info_ServerDesc = null;
	private JLabel mp3Info_L_ServerGenre = null;
	private JTextField mp3Info_ServerGenre = null;
	private JLabel mp3Info_L_ContentType = null;
	private JTextField mp3Info_ContentType = null;
	private JLabel mp3Info_L_MetaDataInt = null;
	private JTextField mp3Info_MetaDataInt = null;
	private JLabel mp3Info_L_BandWidth = null;
	private JTextField mp3Info_BandWidth = null;
	private JLabel mp3Info_L_URL = null;
	private JTextField mp3Info_URL = null;
	private JLabel mp3Info_L_ShortDescription = null;
	private JTextField mp3Info_ShortDescription = null;
	
	private HashMap<String, JTextField> fields;
	/**
	 * Constructor for MP3StreamInfoPanel
	 */
	public MP3StreamInfoPanel()
	{
		super();
		initialize();
	}
	/**
	 * Constructor for MP3StreamInfoPanel
	 * @param layout
	 */
	public MP3StreamInfoPanel(LayoutManager layout)
	{
		super(layout);
		initialize();
	}
	/**
	 * Constructor for MP3StreamInfoPanel
	 * @param isDoubleBuffered
	 */
	public MP3StreamInfoPanel(boolean isDoubleBuffered)
	{
		super(isDoubleBuffered);
		initialize();
	}
	/**
	 * Constructor for MP3StreamInfoPanel
	 * @param layout
	 * @param isDoubleBuffered
	 */
	public MP3StreamInfoPanel(LayoutManager layout, boolean isDoubleBuffered)
	{
		super(layout, isDoubleBuffered);
		initialize();
	}
	private void initialize()
	{
		this.setName("MP3StreamInfoPane");
		this.setLayout(new java.awt.GridBagLayout());

		this.add(getMP3Info_L_ServerType(),			Helpers.getGridBagConstraint(0, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.EAST, 0.0, 0.0));
		this.add(getMP3Info_ServerType(),			Helpers.getGridBagConstraint(1, 0, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		this.add(getMP3Info_L_ServerName(),			Helpers.getGridBagConstraint(0, 1, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.EAST, 0.0, 0.0));
		this.add(getMP3Info_ServerName(),			Helpers.getGridBagConstraint(1, 1, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		this.add(getMP3Info_L_ServerDesc(),			Helpers.getGridBagConstraint(0, 2, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.EAST, 0.0, 0.0));
		this.add(getMP3Info_ServerDesc(),			Helpers.getGridBagConstraint(1, 2, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		this.add(getMP3Info_L_ServerGenre(),		Helpers.getGridBagConstraint(0, 3, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.EAST, 0.0, 0.0));
		this.add(getMP3Info_ServerGenre(),			Helpers.getGridBagConstraint(1, 3, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		this.add(getMP3Info_L_URL(),				Helpers.getGridBagConstraint(0, 4, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.EAST, 0.0, 0.0));
		this.add(getMP3Info_URL(),					Helpers.getGridBagConstraint(1, 4, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		this.add(getMP3Info_L_ContentType(),		Helpers.getGridBagConstraint(0, 5, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.EAST, 0.0, 0.0));
		this.add(getMP3Info_ContentType(),			Helpers.getGridBagConstraint(1, 5, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		this.add(getMP3Info_L_MetaDataInt(),		Helpers.getGridBagConstraint(0, 6, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.EAST, 0.0, 0.0));
		this.add(getMP3Info_MetaDataInt(),			Helpers.getGridBagConstraint(1, 6, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		this.add(getMP3Info_L_BandWidth(),			Helpers.getGridBagConstraint(0, 7, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.EAST, 0.0, 0.0));
		this.add(getMP3Info_BandWidth(),			Helpers.getGridBagConstraint(1, 7, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		this.add(getMP3Info_L_ShortDescription(),	Helpers.getGridBagConstraint(0, 8, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.EAST, 0.0, 0.0));
		this.add(getMP3Info_ShortDescription(),		Helpers.getGridBagConstraint(1, 8, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		this.add(new JPanel(),						Helpers.getGridBagConstraint(0, 9, 1, 0, java.awt.GridBagConstraints.BOTH, java.awt.GridBagConstraints.EAST, 1.0, 1.0));

		this.fields = new HashMap<String, JTextField>();
		fields.put("streamtitle", getMP3Info_ShortDescription()); 
		fields.put("icy-notice2", getMP3Info_ServerType());
		fields.put("server", getMP3Info_ServerType());
		fields.put("icy-name", getMP3Info_ServerName()); 
		fields.put("icy-description", getMP3Info_ServerDesc()); 
		fields.put("icy-genre", getMP3Info_ServerGenre()); 
		fields.put("content-type", getMP3Info_ContentType()); 
		fields.put("icy-metaint", getMP3Info_MetaDataInt()); 
		fields.put("icy-br", getMP3Info_BandWidth()); 
		fields.put("icy-url", getMP3Info_URL()); 
	}
	private javax.swing.JLabel getMP3Info_L_ServerType()
	{
		if (mp3Info_L_ServerType==null)
		{
			mp3Info_L_ServerType = new javax.swing.JLabel();
			mp3Info_L_ServerType.setName("mp3Info_L_ServerType");
			mp3Info_L_ServerType.setText("Server");
			mp3Info_L_ServerType.setFont(Helpers.getDialogFont());
		}
		return mp3Info_L_ServerType;
	}
	private javax.swing.JTextField getMP3Info_ServerType()
	{
		if (mp3Info_ServerType==null)
		{
			mp3Info_ServerType = new javax.swing.JTextField();
			mp3Info_ServerType.setName("mp3Info_ServerType");
			mp3Info_ServerType.setFont(Helpers.getDialogFont());
			mp3Info_ServerType.setEditable(false);
		}
		return mp3Info_ServerType;
	}
	private javax.swing.JLabel getMP3Info_L_ServerName()
	{
		if (mp3Info_L_ServerName==null)
		{
			mp3Info_L_ServerName = new javax.swing.JLabel();
			mp3Info_L_ServerName.setName("mp3Info_L_ServerName");
			mp3Info_L_ServerName.setText("Stream name");
			mp3Info_L_ServerName.setFont(Helpers.getDialogFont());
		}
		return mp3Info_L_ServerName;
	}
	private javax.swing.JTextField getMP3Info_ServerName()
	{
		if (mp3Info_ServerName==null)
		{
			mp3Info_ServerName = new javax.swing.JTextField();
			mp3Info_ServerName.setName("mp3Info_ServerName");
			mp3Info_ServerName.setFont(Helpers.getDialogFont());
			mp3Info_ServerName.setEditable(false);
		}
		return mp3Info_ServerName;
	}
	private javax.swing.JLabel getMP3Info_L_ServerDesc()
	{
		if (mp3Info_L_ServerDesc==null)
		{
			mp3Info_L_ServerDesc = new javax.swing.JLabel();
			mp3Info_L_ServerDesc.setName("mp3Info_L_ServerDesc");
			mp3Info_L_ServerDesc.setText("Stream description");
			mp3Info_L_ServerDesc.setFont(Helpers.getDialogFont());
		}
		return mp3Info_L_ServerDesc;
	}
	private javax.swing.JTextField getMP3Info_ServerDesc()
	{
		if (mp3Info_ServerDesc==null)
		{
			mp3Info_ServerDesc = new javax.swing.JTextField();
			mp3Info_ServerDesc.setName("mp3Info_ServerDesc");
			mp3Info_ServerDesc.setFont(Helpers.getDialogFont());
			mp3Info_ServerDesc.setEditable(false);
		}
		return mp3Info_ServerDesc;
	}
	private javax.swing.JLabel getMP3Info_L_ServerGenre()
	{
		if (mp3Info_L_ServerGenre==null)
		{
			mp3Info_L_ServerGenre = new javax.swing.JLabel();
			mp3Info_L_ServerGenre.setName("mp3Info_L_ServerGenre");
			mp3Info_L_ServerGenre.setText("Genre");
			mp3Info_L_ServerGenre.setFont(Helpers.getDialogFont());
		}
		return mp3Info_L_ServerGenre;
	}
	private javax.swing.JTextField getMP3Info_ServerGenre()
	{
		if (mp3Info_ServerGenre==null)
		{
			mp3Info_ServerGenre = new javax.swing.JTextField();
			mp3Info_ServerGenre.setName("mp3Info_ServerGenre");
			mp3Info_ServerGenre.setFont(Helpers.getDialogFont());
			mp3Info_ServerGenre.setEditable(false);
		}
		return mp3Info_ServerGenre;
	}
	private javax.swing.JLabel getMP3Info_L_URL()
	{
		if (mp3Info_L_URL==null)
		{
			mp3Info_L_URL = new javax.swing.JLabel();
			mp3Info_L_URL.setName("mp3Info_L_URL");
			mp3Info_L_URL.setText("URL");
			mp3Info_L_URL.setFont(Helpers.getDialogFont());
		}
		return mp3Info_L_URL;
	}
	private javax.swing.JTextField getMP3Info_URL()
	{
		if (mp3Info_URL==null)
		{
			mp3Info_URL = new javax.swing.JTextField();
			mp3Info_URL.setName("mp3Info_URL");
			mp3Info_URL.setFont(Helpers.getDialogFont());
			mp3Info_URL.setEditable(false);
		}
		return mp3Info_URL;
	}
	private javax.swing.JLabel getMP3Info_L_ContentType()
	{
		if (mp3Info_L_ContentType==null)
		{
			mp3Info_L_ContentType = new javax.swing.JLabel();
			mp3Info_L_ContentType.setName("mp3Info_L_ContentType");
			mp3Info_L_ContentType.setText("Content Type");
			mp3Info_L_ContentType.setFont(Helpers.getDialogFont());
		}
		return mp3Info_L_ContentType;
	}
	private javax.swing.JTextField getMP3Info_ContentType()
	{
		if (mp3Info_ContentType==null)
		{
			mp3Info_ContentType = new javax.swing.JTextField();
			mp3Info_ContentType.setName("mp3Info_ContentType");
			mp3Info_ContentType.setFont(Helpers.getDialogFont());
			mp3Info_ContentType.setEditable(false);
		}
		return mp3Info_ContentType;
	}
	private javax.swing.JLabel getMP3Info_L_MetaDataInt()
	{
		if (mp3Info_L_MetaDataInt==null)
		{
			mp3Info_L_MetaDataInt = new javax.swing.JLabel();
			mp3Info_L_MetaDataInt.setName("mp3Info_L_MetaDataInt");
			mp3Info_L_MetaDataInt.setText("Metadata interval");
			mp3Info_L_MetaDataInt.setFont(Helpers.getDialogFont());
		}
		return mp3Info_L_MetaDataInt;
	}
	private javax.swing.JTextField getMP3Info_MetaDataInt()
	{
		if (mp3Info_MetaDataInt==null)
		{
			mp3Info_MetaDataInt = new javax.swing.JTextField();
			mp3Info_MetaDataInt.setName("mp3Info_MetaDataInt");
			mp3Info_MetaDataInt.setFont(Helpers.getDialogFont());
			mp3Info_MetaDataInt.setEditable(false);
		}
		return mp3Info_MetaDataInt;
	}
	private javax.swing.JLabel getMP3Info_L_BandWidth()
	{
		if (mp3Info_L_BandWidth==null)
		{
			mp3Info_L_BandWidth = new javax.swing.JLabel();
			mp3Info_L_BandWidth.setName("mp3Info_L_BandWidth");
			mp3Info_L_BandWidth.setText("kb/s");
			mp3Info_L_BandWidth.setFont(Helpers.getDialogFont());
		}
		return mp3Info_L_BandWidth;
	}
	private javax.swing.JTextField getMP3Info_BandWidth()
	{
		if (mp3Info_BandWidth==null)
		{
			mp3Info_BandWidth = new javax.swing.JTextField();
			mp3Info_BandWidth.setName("mp3Info_BandWidth");
			mp3Info_BandWidth.setFont(Helpers.getDialogFont());
			mp3Info_BandWidth.setEditable(false);
		}
		return mp3Info_BandWidth;
	}
	private javax.swing.JLabel getMP3Info_L_ShortDescription()
	{
		if (mp3Info_L_ShortDescription==null)
		{
			mp3Info_L_ShortDescription = new javax.swing.JLabel();
			mp3Info_L_ShortDescription.setName("mp3Info_L_ShortDescription");
			mp3Info_L_ShortDescription.setText("Current title");
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
	public void clearFields()
	{
		Collection<JTextField> textFields = fields.values();
		for (JTextField textField : textFields) textField.setText("");
	}
	public void fillInfoPanelWith(IcyTag icyTag)
	{
		JTextField field = fields.get(icyTag.getName().toLowerCase());
		if (field!=null)
		{
			String value = icyTag.getValue();
			if (value!=null) field.setText(value.trim());
		}
	}
	public String getCurrentSongName()
	{
		StringBuilder b = new StringBuilder();
		String songName = fields.get("streamtitle").getText();
		if (songName!=null && songName.length()!=0) b.append(songName).append(' ');
		
		String serverName = fields.get("icy-name").getText();
		if (serverName!=null && serverName.length()!=0) b.append('(').append(serverName).append(')');
		return b.toString();
	}
}
