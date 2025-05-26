/*
 * @(#) SIDInfoPanel.java
 *
 * Created on 01.02.2024 by Daniel Becker
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
package de.quippy.javamod.multimedia.sid;

import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.net.URL;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import de.quippy.javamod.multimedia.mod.ModConstants;
import de.quippy.javamod.system.Helpers;
import de.quippy.sidplay.libsidplay.components.sidtune.SidTune;
import de.quippy.sidplay.libsidplay.components.sidtune.SidTuneInfo;

/**
 * @author Daniel Becker
 * @since 01.02.2024
 */
public class SIDInfoPanel extends JPanel
{
	private static final long serialVersionUID = -8536617277576828277L;

	private JLabel sidFileNameLabel = null;
	private JTextField sidFileName = null;
	private JLabel sidSongNumberLabel = null;
	private JTextField sidSongNumber = null;
	private JLabel sidSongNameLabel = null;
	private JTextField sidSongName = null;
	private JLabel sidAuthorLabel = null;
	private JTextField sidAuthor = null;
	private JLabel sidCopyrightLabel = null;
	private JTextField sidCopyright = null;
	private JLabel sidSongInfoLabel = null;
	private JTextArea sidSongInfo = null;
	private JScrollPane sidSongInfoScrollPane = null;
	private JLabel sidSongCommentLabel = null;
	private JTextArea sidSongComment = null;
	private JScrollPane sidSongCommentScrollPane = null;
	private JLabel sidSIDModelLabel = null;
	private JTextField sidSIDModel = null;
	private JLabel sidSIDBaseAddrsLabel = null;
	private JTextField sidBaseAddr1 = null;
	private JTextField sidBaseAddr2 = null;
	private JLabel sidAddrsLabel = null;
	private JTextField sidLoadAddr = null;
	private JTextField sidInitAddr = null;
	private JTextField sidPlayAddr = null;
	private JLabel sidFormatStringLabel = null;
	private JTextField sidFormatString = null;
	private JLabel sidDataFileNameLabel = null;
	private JTextField sidDataFileName = null;
	private JLabel sidInfoFileNameLabel = null;
	private JTextField sidInfoFileName = null;

	private SIDContainer parentContainer;

	/**
	 * Constructor for SIDInfoPanel
	 */
	public SIDInfoPanel()
	{
		super();
		initialize();
	}
	/**
	 * Constructor for SIDInfoPanel
	 * @param layout
	 */
	public SIDInfoPanel(final LayoutManager layout)
	{
		super(layout);
		initialize();
	}
	/**
	 * Constructor for SIDInfoPanel
	 * @param isDoubleBuffered
	 */
	public SIDInfoPanel(final boolean isDoubleBuffered)
	{
		super(isDoubleBuffered);
		initialize();
	}
	/**
	 * Constructor for SIDInfoPanel
	 * @param layout
	 * @param isDoubleBuffered
	 */
	public SIDInfoPanel(final LayoutManager layout, final boolean isDoubleBuffered)
	{
		super(layout, isDoubleBuffered);
		initialize();
	}
	/**
	 * @return the parent
	 */
	public SIDContainer getParentContainer()
	{
		return parentContainer;
	}
	/**
	 * @param parent the parent to set
	 */
	public void setParentContainer(final SIDContainer parent)
	{
		parentContainer = parent;
	}
	private void initialize()
	{
		setName("SIDInfoPane");
		setLayout(new GridBagLayout());

		add(getSIDFileNameLabel(),			Helpers.getGridBagConstraint(0, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		add(getSIDFileName(),				Helpers.getGridBagConstraint(1, 0, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		add(getSIDSongNumberLabel(),		Helpers.getGridBagConstraint(0, 1, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		add(getSIDSongNumber(),				Helpers.getGridBagConstraint(1, 1, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		add(getSIDSongNameLabel(),			Helpers.getGridBagConstraint(0, 2, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		add(getSIDSongName(),				Helpers.getGridBagConstraint(1, 2, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		add(getSIDAuthorLabel(),			Helpers.getGridBagConstraint(0, 3, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		add(getSIDAuthor(),					Helpers.getGridBagConstraint(1, 3, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		add(getSIDCopyrightLabel(),			Helpers.getGridBagConstraint(0, 4, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		add(getSIDCopyright(),				Helpers.getGridBagConstraint(1, 4, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		add(getSIDSongInfoLabel(),			Helpers.getGridBagConstraint(0, 5, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		add(getSIDSongInfoScrollPane(),		Helpers.getGridBagConstraint(1, 5, 3, 0, java.awt.GridBagConstraints.BOTH, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		add(getSIDSongCommentLabel(),		Helpers.getGridBagConstraint(0, 8, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		add(getSIDSongCommentScrollPane(),	Helpers.getGridBagConstraint(1, 8, 3, 0, java.awt.GridBagConstraints.BOTH, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		add(getSIDSIDModelLabel(),			Helpers.getGridBagConstraint(0,11, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		add(getSIDSIDModel(),				Helpers.getGridBagConstraint(1,11, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		add(getSIDSIDBaseAddrsLabel(),		Helpers.getGridBagConstraint(0,12, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		add(getSIDBaseAddr1(),				Helpers.getGridBagConstraint(1,12, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		add(getSIDBaseAddr2(),				Helpers.getGridBagConstraint(2,12, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		add(getSIDAddrsLabel(),				Helpers.getGridBagConstraint(0,13, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		add(getSIDLoadAddr(),				Helpers.getGridBagConstraint(1,13, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		add(getSIDInitAddr(),				Helpers.getGridBagConstraint(2,13, 1, 1, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		add(getSIDPlayAddr(),				Helpers.getGridBagConstraint(3,13, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		add(getSIDFormatStringLabel(),		Helpers.getGridBagConstraint(0,14, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		add(getSIDFormatString(),			Helpers.getGridBagConstraint(1,14, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		add(getSIDDataFileNameLabel(),		Helpers.getGridBagConstraint(0,15, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		add(getSIDDataFileName(),			Helpers.getGridBagConstraint(1,15, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
		add(getSIDInfoFileNameLabel(),		Helpers.getGridBagConstraint(0,16, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		add(getSIDInfoFileName(),			Helpers.getGridBagConstraint(1,16, 1, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, 1.0, 0.0));
	}
	private JLabel getSIDFileNameLabel()
	{
		if (sidFileNameLabel==null)
		{
			sidFileNameLabel = new JLabel();
			sidFileNameLabel.setName("sidFileNameLabel");
			sidFileNameLabel.setText("SID file name:");
			sidFileNameLabel.setFont(Helpers.getDialogFont());
		}
		return sidFileNameLabel;
	}
	private JTextField getSIDFileName()
	{
		if (sidFileName==null)
		{
			sidFileName = new JTextField();
			sidFileName.setName("sidFileName");
			sidFileName.setEditable(false);
			sidFileName.setFont(Helpers.getDialogFont());
		}
		return sidFileName;
	}
	private JLabel getSIDSongNumberLabel()
	{
		if (sidSongNumberLabel==null)
		{
			sidSongNumberLabel = new JLabel();
			sidSongNumberLabel.setName("sidSongNumberLabel");
			sidSongNumberLabel.setText("Song number of:");
			sidSongNumberLabel.setFont(Helpers.getDialogFont());
		}
		return sidSongNumberLabel;
	}
	private JTextField getSIDSongNumber()
	{
		if (sidSongNumber==null)
		{
			sidSongNumber = new JTextField();
			sidSongNumber.setName("sidSongNumber");
			sidSongNumber.setEditable(false);
			sidSongNumber.setFont(Helpers.getDialogFont());
		}
		return sidSongNumber;
	}
	private JLabel getSIDSongNameLabel()
	{
		if (sidSongNameLabel==null)
		{
			sidSongNameLabel = new JLabel();
			sidSongNameLabel.setName("sidSongNameLabel");
			sidSongNameLabel.setText("Song name:");
			sidSongNameLabel.setFont(Helpers.getDialogFont());
		}
		return sidSongNameLabel;
	}
	private JTextField getSIDSongName()
	{
		if (sidSongName==null)
		{
			sidSongName = new JTextField();
			sidSongName.setName("sidSongName");
			sidSongName.setEditable(false);
			sidSongName.setFont(Helpers.getDialogFont());
		}
		return sidSongName;
	}
	private JLabel getSIDAuthorLabel()
	{
		if (sidAuthorLabel==null)
		{
			sidAuthorLabel = new JLabel();
			sidAuthorLabel.setName("sidAuthorLabel");
			sidAuthorLabel.setText("Author:");
			sidAuthorLabel.setFont(Helpers.getDialogFont());
		}
		return sidAuthorLabel;
	}
	private JTextField getSIDAuthor()
	{
		if (sidAuthor==null)
		{
			sidAuthor = new JTextField();
			sidAuthor.setName("sidAuthor");
			sidAuthor.setEditable(false);
			sidAuthor.setFont(Helpers.getDialogFont());
		}
		return sidAuthor;
	}
	private JLabel getSIDCopyrightLabel()
	{
		if (sidCopyrightLabel==null)
		{
			sidCopyrightLabel = new JLabel();
			sidCopyrightLabel.setName("sidCopyrightLabel");
			sidCopyrightLabel.setText("Copyright:");
			sidCopyrightLabel.setFont(Helpers.getDialogFont());
		}
		return sidCopyrightLabel;
	}
	private JTextField getSIDCopyright()
	{
		if (sidCopyright==null)
		{
			sidCopyright = new JTextField();
			sidCopyright.setName("sidCopyright");
			sidCopyright.setEditable(false);
			sidCopyright.setFont(Helpers.getDialogFont());
		}
		return sidCopyright;
	}
	private JLabel getSIDSongInfoLabel()
	{
		if (sidSongInfoLabel==null)
		{
			sidSongInfoLabel = new JLabel();
			sidSongInfoLabel.setName("sidSongInfoLabel");
			sidSongInfoLabel.setText("Song infos:");
			sidSongInfoLabel.setFont(Helpers.getDialogFont());
		}
		return sidSongInfoLabel;
	}
	private JTextArea getSIDSongInfo()
	{
		if (sidSongInfo==null)
		{
			sidSongInfo = new JTextArea();
			sidSongInfo.setName("sidSongInfo");
			sidSongInfo.setEditable(false);
			sidSongInfo.setFont(Helpers.getDialogFont());
		}
		return sidSongInfo;
	}
	private JScrollPane getSIDSongInfoScrollPane()
	{
		if (sidSongInfoScrollPane==null)
		{
			sidSongInfoScrollPane = new JScrollPane();
			sidSongInfoScrollPane.setViewportView(getSIDSongInfo());
		}
		return sidSongInfoScrollPane;
	}
	private JLabel getSIDSongCommentLabel()
	{
		if (sidSongCommentLabel==null)
		{
			sidSongCommentLabel = new JLabel();
			sidSongCommentLabel.setName("sidSongCommentLabel");
			sidSongCommentLabel.setText("Song comments:");
			sidSongCommentLabel.setFont(Helpers.getDialogFont());
		}
		return sidSongCommentLabel;
	}
	private JTextArea getSIDSongComment()
	{
		if (sidSongComment==null)
		{
			sidSongComment = new JTextArea();
			sidSongComment.setName("sidSongComment");
			sidSongComment.setEditable(false);
			sidSongComment.setFont(Helpers.getDialogFont());
		}
		return sidSongComment;
	}
	private JScrollPane getSIDSongCommentScrollPane()
	{
		if (sidSongCommentScrollPane==null)
		{
			sidSongCommentScrollPane = new JScrollPane();
			sidSongCommentScrollPane.setViewportView(getSIDSongComment());
		}
		return sidSongCommentScrollPane;
	}
	private JLabel getSIDSIDModelLabel()
	{
		if (sidSIDModelLabel==null)
		{
			sidSIDModelLabel = new JLabel();
			sidSIDModelLabel.setName("sidInfoFileNameLabel");
			sidSIDModelLabel.setText("SID model:");
			sidSIDModelLabel.setFont(Helpers.getDialogFont());
		}
		return sidSIDModelLabel;
	}
	private JTextField getSIDSIDModel()
	{
		if (sidSIDModel==null)
		{
			sidSIDModel = new JTextField();
			sidSIDModel.setName("sidSIDModel");
			sidSIDModel.setEditable(false);
			sidSIDModel.setFont(Helpers.getDialogFont());
		}
		return sidSIDModel;
	}
	private JLabel getSIDAddrsLabel()
	{
		if (sidAddrsLabel==null)
		{
			sidAddrsLabel = new JLabel();
			sidAddrsLabel.setName("sidAddrsLabel");
			sidAddrsLabel.setText("SID Load/Init/Play Addrs:");
			sidAddrsLabel.setFont(Helpers.getDialogFont());
		}
		return sidAddrsLabel;
	}
	private JTextField getSIDLoadAddr()
	{
		if (sidLoadAddr==null)
		{
			sidLoadAddr = new JTextField();
			sidLoadAddr.setName("sidLoadAddr");
			sidLoadAddr.setEditable(false);
			sidLoadAddr.setFont(Helpers.getDialogFont());
		}
		return sidLoadAddr;
	}
	private JTextField getSIDInitAddr()
	{
		if (sidInitAddr==null)
		{
			sidInitAddr = new JTextField();
			sidInitAddr.setName("sidInitAddr");
			sidInitAddr.setEditable(false);
			sidInitAddr.setFont(Helpers.getDialogFont());
		}
		return sidInitAddr;
	}
	private JTextField getSIDPlayAddr()
	{
		if (sidPlayAddr==null)
		{
			sidPlayAddr = new JTextField();
			sidPlayAddr.setName("sidPlayAddr");
			sidPlayAddr.setEditable(false);
			sidPlayAddr.setFont(Helpers.getDialogFont());
		}
		return sidPlayAddr;
	}
	private JLabel getSIDSIDBaseAddrsLabel()
	{
		if (sidSIDBaseAddrsLabel==null)
		{
			sidSIDBaseAddrsLabel = new JLabel();
			sidSIDBaseAddrsLabel.setName("sidSIDBaseAddrsLabel");
			sidSIDBaseAddrsLabel.setText("SID 1/2 Base Addrs:");
			sidSIDBaseAddrsLabel.setFont(Helpers.getDialogFont());
		}
		return sidSIDBaseAddrsLabel;
	}
	private JTextField getSIDBaseAddr1()
	{
		if (sidBaseAddr1==null)
		{
			sidBaseAddr1 = new JTextField();
			sidBaseAddr1.setName("sidBaseAddr1");
			sidBaseAddr1.setEditable(false);
			sidBaseAddr1.setFont(Helpers.getDialogFont());
		}
		return sidBaseAddr1;
	}
	private JTextField getSIDBaseAddr2()
	{
		if (sidBaseAddr2==null)
		{
			sidBaseAddr2 = new JTextField();
			sidBaseAddr2.setName("sidBaseAddr2");
			sidBaseAddr2.setEditable(false);
			sidBaseAddr2.setFont(Helpers.getDialogFont());
		}
		return sidBaseAddr2;
	}
	private JLabel getSIDFormatStringLabel()
	{
		if (sidFormatStringLabel==null)
		{
			sidFormatStringLabel = new JLabel();
			sidFormatStringLabel.setName("sidFormatStringLabel");
			sidFormatStringLabel.setText("SID format info:");
			sidFormatStringLabel.setFont(Helpers.getDialogFont());
		}
		return sidFormatStringLabel;
	}
	private JTextField getSIDFormatString()
	{
		if (sidFormatString==null)
		{
			sidFormatString = new JTextField();
			sidFormatString.setName("sidFormatString");
			sidFormatString.setEditable(false);
			sidFormatString.setFont(Helpers.getDialogFont());
		}
		return sidFormatString;
	}
	private JLabel getSIDDataFileNameLabel()
	{
		if (sidDataFileNameLabel==null)
		{
			sidDataFileNameLabel = new JLabel();
			sidDataFileNameLabel.setName("sidDataFileNameLabel");
			sidDataFileNameLabel.setText("SID data file name:");
			sidDataFileNameLabel.setFont(Helpers.getDialogFont());
		}
		return sidDataFileNameLabel;
	}
	private JTextField getSIDDataFileName()
	{
		if (sidDataFileName==null)
		{
			sidDataFileName = new JTextField();
			sidDataFileName.setName("sidDataFileName");
			sidDataFileName.setEditable(false);
			sidDataFileName.setFont(Helpers.getDialogFont());
		}
		return sidDataFileName;
	}
	private JLabel getSIDInfoFileNameLabel()
	{
		if (sidInfoFileNameLabel==null)
		{
			sidInfoFileNameLabel = new JLabel();
			sidInfoFileNameLabel.setName("sidInfoFileNameLabel");
			sidInfoFileNameLabel.setText("SID info file name:");
			sidInfoFileNameLabel.setFont(Helpers.getDialogFont());
		}
		return sidInfoFileNameLabel;
	}
	private JTextField getSIDInfoFileName()
	{
		if (sidInfoFileName==null)
		{
			sidInfoFileName = new JTextField();
			sidInfoFileName.setName("sidInfoFileName");
			sidInfoFileName.setEditable(false);
			sidInfoFileName.setFont(Helpers.getDialogFont());
		}
		return sidInfoFileName;
	}
	public void fillInfoPanelWith(final SidTuneInfo sidTuneInfo)
	{
		getSIDSongNumber().setText(sidTuneInfo.currentSong+"/"+sidTuneInfo.songs);
		getSIDSongName().setText(sidTuneInfo.infoString[0]);
		getSIDAuthor().setText(sidTuneInfo.infoString[1]);
		getSIDCopyright().setText(sidTuneInfo.infoString[2]);
		if (sidTuneInfo.numberOfInfoStrings>3)
		{
			final StringBuilder sb = new StringBuilder();
			for (int i=3; i<sidTuneInfo.numberOfInfoStrings; i++)
				sb.append(sidTuneInfo.infoString[i]).append('\n');
			getSIDSongInfo().setText(sb.toString());
		}
		if (sidTuneInfo.commentString!=null && sidTuneInfo.commentString.length>0)
		{
			final StringBuilder sb = new StringBuilder();
			for (final String element : sidTuneInfo.commentString)
				sb.append(element).append('\n');
			getSIDSongComment().setText(sb.toString());
		}
		final int sidModel = sidTuneInfo.sidModel;
		final String [] sidModels = SIDContainer.SIDMODELS;
		if (sidModel>0 && sidModel<sidModels.length)
			getSIDSIDModel().setText(sidModels[sidModel]);
		getSIDLoadAddr().setText('$'+ModConstants.getAsHex(sidTuneInfo.loadAddr, 4));
		getSIDInitAddr().setText('$'+ModConstants.getAsHex(sidTuneInfo.initAddr, 4));
		getSIDPlayAddr().setText('$'+ModConstants.getAsHex(sidTuneInfo.playAddr, 4));
		getSIDBaseAddr1().setText('$'+ModConstants.getAsHex(sidTuneInfo.sidChipBase1, 4));
		getSIDBaseAddr2().setText('$'+ModConstants.getAsHex(sidTuneInfo.sidChipBase2, 4));
		getSIDFormatString().setText(sidTuneInfo.formatString);
		getSIDDataFileName().setText(sidTuneInfo.dataFileName);
		getSIDInfoFileName().setText(sidTuneInfo.infoFileName);
	}
	public void fillInfoPanelWith(final URL sidFileUrl, final SidTune sidTune)
	{
		if (sidFileUrl!=null) getSIDFileName().setText(Helpers.getFileNameFromURL(sidFileUrl));
		if (sidTune!=null)
		{
			final SidTuneInfo sidTuneInfo = sidTune.getInfo();
			if (sidTuneInfo!=null) fillInfoPanelWith(sidTuneInfo);
		}
	}
}
