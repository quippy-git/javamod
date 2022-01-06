/*
 * @(#) EnvelopePanel.java
 *
 * Created on 01.08.2020 by Daniel Becker
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
package de.quippy.javamod.multimedia.mod.gui;

import java.awt.GridBagLayout;
import java.awt.LayoutManager;

import javax.swing.JPanel;

import de.quippy.javamod.main.gui.tools.FixedStateCheckBox;
import de.quippy.javamod.multimedia.mod.loader.instrument.Envelope;
import de.quippy.javamod.system.Helpers;

/**
 * @author Daniel Becker
 * @since 01.08.2020
 */
public class EnvelopePanel extends JPanel
{
	private static final long serialVersionUID = 5511415780545189305L;
	
	public enum EnvelopeType { volume, panning, pitch }
	
	private EnvelopeImagePanel envelopeImagePanel = null;

	private FixedStateCheckBox isEnabled = null;
	private FixedStateCheckBox isCarryEnabled = null;
	private FixedStateCheckBox isFilterEnabled = null;
	private FixedStateCheckBox isLoopEnabled = null;
	private FixedStateCheckBox isSustainEnabled = null;
	
	//private Envelope envelope = null;

	/**
	 * Constructor for EnvelopePanel
	 */
	public EnvelopePanel()
	{
		super();
		initialize();
	}
	/**
	 * Constructor for EnvelopePanel
	 * @param layout
	 */
	public EnvelopePanel(LayoutManager layout)
	{
		super(layout);
		initialize();
	}
	/**
	 * Constructor for EnvelopePanel
	 * @param isDoubleBuffered
	 */
	public EnvelopePanel(boolean isDoubleBuffered)
	{
		super(isDoubleBuffered);
		initialize();
	}
	/**
	 * Constructor for EnvelopePanel
	 * @param layout
	 * @param isDoubleBuffered
	 */
	public EnvelopePanel(LayoutManager layout, boolean isDoubleBuffered)
	{
		super(layout, isDoubleBuffered);
		initialize();
	}
	private void initialize()
	{
		setLayout(new GridBagLayout());
		add(getIsEnabled(), 			Helpers.getGridBagConstraint(0, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		add(getIsCarryEnabled(), 		Helpers.getGridBagConstraint(1, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		add(getIsFilterEnabled(), 		Helpers.getGridBagConstraint(2, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		add(getIsLoopEnabled(), 		Helpers.getGridBagConstraint(3, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		add(getIsSustainEnabled(), 		Helpers.getGridBagConstraint(4, 0, 1, 1, java.awt.GridBagConstraints.NONE, java.awt.GridBagConstraints.WEST, 0.0, 0.0));
		add(getEnvelopeImagePanel(), 		Helpers.getGridBagConstraint(0, 1, 1, 0, java.awt.GridBagConstraints.BOTH, java.awt.GridBagConstraints.WEST, 1.0, 1.0));
	}
	private FixedStateCheckBox getIsEnabled()
	{
		if (isEnabled==null)
		{
			isEnabled = new FixedStateCheckBox();
			isEnabled.setName("isEnabled");
			isEnabled.setText("Enabled");
			isEnabled.setFont(Helpers.getDialogFont());
		}
		return isEnabled;
	}
	private FixedStateCheckBox getIsCarryEnabled()
	{
		if (isCarryEnabled==null)
		{
			isCarryEnabled = new FixedStateCheckBox();
			isCarryEnabled.setName("isCarryEnabled");
			isCarryEnabled.setText("Carry");
			isCarryEnabled.setFont(Helpers.getDialogFont());
		}
		return isCarryEnabled;
	}
	private FixedStateCheckBox getIsFilterEnabled()
	{
		if (isFilterEnabled==null)
		{
			isFilterEnabled = new FixedStateCheckBox();
			isFilterEnabled.setName("isFilterEnabled");
			isFilterEnabled.setText("Filter");
			isFilterEnabled.setFont(Helpers.getDialogFont());
		}
		return isFilterEnabled;
	}
	private FixedStateCheckBox getIsLoopEnabled()
	{
		if (isLoopEnabled==null)
		{
			isLoopEnabled = new FixedStateCheckBox();
			isLoopEnabled.setName("isLoopEnabled");
			isLoopEnabled.setText("Loop");
			isLoopEnabled.setFont(Helpers.getDialogFont());
		}
		return isLoopEnabled;
	}
	private FixedStateCheckBox getIsSustainEnabled()
	{
		if (isSustainEnabled==null)
		{
			isSustainEnabled = new FixedStateCheckBox();
			isSustainEnabled.setName("isSustainEnabled");
			isSustainEnabled.setText("Sustain");
			isSustainEnabled.setFont(Helpers.getDialogFont());
		}
		return isSustainEnabled;
	}
	private EnvelopeImagePanel getEnvelopeImagePanel()
	{
		if (envelopeImagePanel==null)
		{
			envelopeImagePanel = new EnvelopeImagePanel();
		}
		return envelopeImagePanel;
	}
	public void setEnvelope(Envelope envelope, EnvelopeType type)
	{
		//this.envelope = envelope;
		if (envelope!=null)
		{
			getIsEnabled().setFixedState(envelope.on);
			getIsCarryEnabled().setFixedState(envelope.carry);
			getIsFilterEnabled().setFixedState(envelope.filter);
			getIsFilterEnabled().setEnabled(type == EnvelopeType.pitch);
			getIsLoopEnabled().setFixedState(envelope.loop);
			getIsSustainEnabled().setFixedState(envelope.sustain);
		}
		else
		{
			getIsEnabled().setFixedState(false);
			getIsCarryEnabled().setFixedState(false);
			getIsFilterEnabled().setFixedState(false);
			getIsFilterEnabled().setEnabled(false);
			getIsLoopEnabled().setFixedState(false);
			getIsSustainEnabled().setFixedState(false);
		}
		getEnvelopeImagePanel().setEnvelope(envelope);
	}
}
