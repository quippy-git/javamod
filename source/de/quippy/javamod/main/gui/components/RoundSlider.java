/*
 * @(#) RoundSlider.java
 *
 * Created on 01.11.2008 by Daniel Becker
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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class RoundSlider extends JComponent
{
	private static final long serialVersionUID = 7401158894851891182L;

	private static final RenderingHints AALIAS = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	private static final double PI_2 = Math.PI * 2.0;
	private static final double START_ANG = (315.0 / 360.0) * PI_2;
	private static final double LENGTH_ANG = (270.0 / 360.0 ) * PI_2;
//	private static final double MULTIP = 180.0 / Math.PI;

	private ChangeEvent changeEvent = null;

	private double lastAngle;
	private double currentAngle;
	private float currentValue;

	public RoundSlider()
	{
		super();
		initialize();
	}
	private void initialize()
	{
		setValue(0);

//		addComponentListener(new ComponentListener()
//		{
//			public void componentHidden(ComponentEvent e) {}
//			public void componentMoved(ComponentEvent e) {}
//			public void componentShown(ComponentEvent e) {}
//			public void componentResized(ComponentEvent e)
//			{
//				int width = getWidth();
//				int height = getHeight();
//				int size = Math.min(width, height);
//			}
//		});

		this.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(final MouseEvent me)
			{
				lastAngle = getAngle(me);
				RoundSlider.this.requestFocusInWindow();
			}

			@Override
			public void mouseClicked(final MouseEvent me)
			{
				final double ang = getAngle(me);
				RoundSlider.this.setValue((float)((START_ANG - ang) / LENGTH_ANG));
			}
		});

		addMouseMotionListener(new MouseMotionAdapter()
		{
			/**
			 * @param me
			 * @see java.awt.event.MouseMotionAdapter#mouseDragged(java.awt.event.MouseEvent)
			 */
			@Override
			public void mouseDragged(final MouseEvent me)
			{
				final double ang = getAngle(me);
				final double diff = ang - lastAngle;
				lastAngle = ang;
				final float newValue = (float) (getValue() - (diff / LENGTH_ANG));
				if (Math.abs(newValue - getValue())<0.5) setValue(newValue);
			}

			@Override
			public void mouseMoved(final MouseEvent me) {}
		});
	}

	public float getValue()
	{
		return currentValue;
	}
	public void setValue(final float newVal)
	{
		if (newVal < 0)
			this.currentValue = 0;
		else
		if (newVal > 1)
			currentValue = 1;
		else
			this.currentValue = newVal;

		currentAngle = START_ANG - (LENGTH_ANG * currentValue);
		repaint();
		fireChangeEvent();
	}

	public void addChangeListener(final ChangeListener cl)
	{
		listenerList.add(ChangeListener.class, cl);
	}
	public void removeChangeListener(final ChangeListener cl)
	{
		listenerList.remove(ChangeListener.class, cl);
	}
	protected void fireChangeEvent()
	{
		final Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2)
		{
			if (listeners[i] == ChangeListener.class)
			{
				if (changeEvent == null) changeEvent = new ChangeEvent(this);
				((ChangeListener) listeners[i + 1]).stateChanged(changeEvent);
			}
		}
	}
	private int getMaxSize()
	{
		final int width = getWidth();
		final int height = getHeight();
		return (width<height)?width:height;
	}
	private double getAngle(final MouseEvent me)
	{
		final int middle = getMaxSize()>>1;
		final int xpos = me.getX() - middle;
		final int ypos = me.getY() - middle;
		double ang = Math.atan2(xpos, ypos);
		if (xpos<0) ang += PI_2; // Values: y>0: 0.0° - 180° y<0: 0.0° -  -180°
		return ang;
	}
	/**
	 * @param g
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	public void paintComponent(final Graphics g)
	{
		int size = getMaxSize();
		int middle = size>>1;

		final Graphics2D gfx = (Graphics2D)g.create();
		try
		{
			gfx.setBackground(getParent().getBackground());
			gfx.setRenderingHints(AALIAS);

			size-=2;
			middle--;
			int startColor = 64;
			final int colorStep = (255 - startColor) / size;
			for (int i=size; i>=0; i--)
			{
				gfx.setColor(new Color(startColor, startColor, startColor));
				final int x = 1+middle-(i>>1);
				gfx.fillOval(x, x, i, i);
				startColor += colorStep;
			}

			gfx.setColor(Color.RED);
			final double sin = Math.sin(currentAngle);
			final double cos = Math.cos(currentAngle);
			final int x = middle + (int) (middle * sin);
			final int y = middle + (int) (middle * cos);
			gfx.drawLine(middle, middle, x, y);

	//		final int dx = (int) (2 * sin);
	//		final int dy = (int) (2 * cos);
	//		gfx.drawLine(middle + dx, middle + dy, x, y);
	//		gfx.drawLine(middle - dx, middle - dy, x, y);
		}
		finally
		{
			gfx.dispose();
		}
	}
}