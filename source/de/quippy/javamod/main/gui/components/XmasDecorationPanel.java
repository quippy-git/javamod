/*
 * @(#) XmasDecorationPanel.java
 *
 * Created on 05.12.2023 by Daniel Becker
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
package de.quippy.javamod.main.gui.components;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.swing.ImageIcon;

/**
 * @author Daniel Becker
 * @since 05.12.2023
 */
public class XmasDecorationPanel extends MeterPanelBase
{
	private static final long serialVersionUID = -3507211484792572812L;

	private static final int SYNC2FPS_BITS	= 16;
	private static final int SYNC2FPS_FRAC	= 1<<SYNC2FPS_BITS;
	private static final int SYNC2FPS_MASK	= SYNC2FPS_FRAC-1;
	private static final int DEFAULT_FPS	= 2;

	private static final int FLICKERTYPE_ALL_OFF = 0;
	private static final int FLICKERTYPE_ALL_ON = 1;
	private static final int FLICKERTYPE_ALTERNATE = 2;
	private static final int FLICKERTYPE_CHASE = 3;
	private static final int FLICKERTYPE_RANDOM = 4;
	private static final int FLICKERTYPE_SOME = 5;
	private static final int FLICKERTYPE_SOME_FLICKER = 6;
	private static final int FLICKERTYPE_ALL_FLASH = 7;

	public static String [] FLICKER_TYPES =
	{
	 	"All off", "All on", "Alternating", "Chase bulbs", "Random on/off", "Some on/off", "Some flash", "All flash"
	};

	private final ImageIcon[] bulbs;
	private int[] useIndex;
	private BufferedImage imageBuffer;

	private final Random rand;

	private int flickerType; // 0: all off, 1: all On, 2: alternate, 3: chase, 4: random
	private boolean withSpace;
	private int syncToFPScounter;
	private int syncToFPSAdd;
	private volatile int dontDraw; // if dontDraw>0 do not draw
	private boolean inDraw;

	/**
	 * Constructor for XmasDecorationPanel
	 * @param desiredFPS
	 */
	public XmasDecorationPanel(final int desiredFPS, final ImageIcon [] useBulbs)
	{
		super(desiredFPS);

		bulbs = useBulbs;

		syncToFPScounter = SYNC2FPS_FRAC;
		setUpdateFPS(DEFAULT_FPS);
		rand = new Random();
		dontDraw = 0;
		inDraw = false;
		withSpace = false;
		setFlickerType(2);

		//startThread(); // will do that only when is set visible in XmasScreenConfigPanel
	}
	private void enterCritical()
	{
		dontDraw++;
		while (inDraw) try { Thread.sleep(10L); } catch (final InterruptedException ex) { /*NOOP*/ }
	}
	private void leaveCritical()
	{
		dontDraw--;
	}
	private void createBulbIndex(final int forWidth)
	{
		enterCritical();
		if (bulbs!=null && bulbs[0]!=null)
		{
			int anz = forWidth / bulbs[0].getIconWidth();
			if (forWidth % bulbs[0].getIconWidth()!=0) anz++;
			useIndex = new int[anz];
			int oldIndex = 0;
			for (int i=0; i<anz; i++)
			{
				if (withSpace && (i%2)==0)
					useIndex[i] = 0;
				else
				{
					int newIndex = oldIndex;
					while (newIndex==oldIndex) newIndex = (rand.nextInt(7)<<1) + 1;
					final int bulbNumber = (withSpace)?(i>>1) : i;
					if (flickerType == FLICKERTYPE_ALTERNATE && (bulbNumber%2)==0) newIndex++;
					if (flickerType == FLICKERTYPE_CHASE && (bulbNumber%4)==0) newIndex++;
					useIndex[i] = (oldIndex = newIndex);
				}
			}
		}
		// We should invalidate the buffer to get a fresh clean one
		imageBuffer = null;
		// and instantly do a redraw:
		syncToFPScounter = SYNC2FPS_FRAC;

		leaveCritical();
	}
	public void setFlickerType(final int newFlickerType)
	{
		enterCritical();
		flickerType = newFlickerType;
		createBulbIndex(getWidth());
		leaveCritical();
	}
	public void setUpdateFPS(final int updateFPS)
	{
		syncToFPSAdd = (updateFPS<<SYNC2FPS_BITS) / desiredFPS;
	}
	public void setWithSpace(final boolean newWithSpace)
	{
		enterCritical();
		withSpace = newWithSpace;
		createBulbIndex(getWidth());
		leaveCritical();
	}
	/**
	 * @return
	 * @see de.quippy.javamod.main.gui.components.MeterPanelBase#getDoubleBuffer()
	 */
	@Override
	protected synchronized BufferedImage getDoubleBuffer(final int myWidth, final int myHeight)
	{
    	if (imageBuffer==null && myWidth>0 && myHeight>0)
		{
			final GraphicsConfiguration graConf = getGraphicsConfiguration();
			if (graConf!=null)
			{
				imageBuffer = graConf.createCompatibleImage(myWidth, myHeight, Transparency.TRANSLUCENT);
				final Graphics2D g2d = (Graphics2D)imageBuffer.getGraphics();
				g2d.setColor(new Color(0, true));
				g2d.setComposite(AlphaComposite.Clear);
				g2d.fillRect(0, 0, myWidth, myHeight);
				//g2d.clearRect(0, 0, myWidth, myHeight);
				g2d.dispose();
			}
		}
		return imageBuffer;
	}
	private int drawBulbAt(final Graphics2D g2d, final int x, final int bulbIndex)
	{
		if (bulbs!=null)
		{
			final ImageIcon imageIcon = bulbs[bulbIndex];
			if (imageIcon!=null)
			{
				g2d.drawImage(imageIcon.getImage(), x, 0, null);
				return imageIcon.getIconWidth();
			}
		}
		return 0;
	}
	/**
	 * @param g
	 * @param newTop
	 * @param newLeft
	 * @param newWidth
	 * @param newHeight
	 * @see de.quippy.javamod.main.gui.components.MeterPanelBase#drawMeter(java.awt.Graphics, int, int, int, int)
	 */
	@Override
	protected void drawMeter(final Graphics2D g, final int newTop, final int newLeft, final int newWidth, final int newHeight)
	{
		syncToFPScounter += syncToFPSAdd;
		if (syncToFPScounter >= SYNC2FPS_FRAC)
		{
			syncToFPScounter &= SYNC2FPS_MASK;
			if (bulbs!=null && bulbs.length!=0 && useIndex!=null && useIndex.length!=0 && dontDraw==0)
			{
				inDraw = true;
				try
				{
					int x = 0;
					for (int index=0; index<useIndex.length; index++)
					{
						int bulbIndex = useIndex[index];
						if (bulbIndex!=0) // 0 is the hanger
						{
							final boolean isLit = (bulbIndex%2)==0;
							if (bulbIndex>0)
							{
								switch (flickerType)
								{
									case FLICKERTYPE_ALL_OFF:
										useIndex[index] = (isLit)?--bulbIndex:bulbIndex;
										break;
									case FLICKERTYPE_ALL_ON:
										useIndex[index] = (isLit)?bulbIndex:++bulbIndex;
										break;
									case FLICKERTYPE_ALTERNATE:
									case FLICKERTYPE_ALL_FLASH: //same as 2, but initially all bulbs are off - so all alternate between on/off
										useIndex[index] = (isLit)?--bulbIndex:++bulbIndex;
										break;
									case FLICKERTYPE_CHASE:
										if (isLit)
										{
											useIndex[index] = --bulbIndex;
											x += drawBulbAt(g, x, bulbIndex);
											index++;
											if (index>=useIndex.length) x = index = 0;
											if (withSpace)
											{
												x += bulbs[useIndex[index]].getIconWidth();
												index++;
												if (index>=useIndex.length)
												{
													index = 0;
													x += bulbs[useIndex[index++]].getIconWidth();
												}
											}
											bulbIndex = useIndex[index];
											if ((bulbIndex%2)!=0) useIndex[index] = ++bulbIndex;
											if ((!withSpace && index==0) || (withSpace && index==1))
											{
												drawBulbAt(g, x, bulbIndex);
												index = useIndex.length;
											}
										}
										break; //??
									case FLICKERTYPE_RANDOM:
										if (rand.nextBoolean())
											useIndex[index] = (isLit)?--bulbIndex:++bulbIndex;
										break;
									case FLICKERTYPE_SOME:
										if (rand.nextInt(100)<10)
											useIndex[index] = (isLit)?--bulbIndex:++bulbIndex;
										break;
									case FLICKERTYPE_SOME_FLICKER:
										if (!isLit && rand.nextInt(100)<10) bulbIndex++;
										break;
									default:
										break;
								}
							}
						}
						x += drawBulbAt(g, x, bulbIndex);
					}
				}
				finally
				{
					inDraw=false;
				}
			}
		}
	}
	/**
	 * @param newTop
	 * @param newLeft
	 * @param newWidth
	 * @param newHeight
	 * @see de.quippy.javamod.main.gui.components.MeterPanelBase#componentWasResized(int, int, int, int)
	 */
	@Override
	protected void componentWasResized(final int newTop, final int newLeft, final int newWidth, final int newHeight)
	{
		createBulbIndex(newWidth);
	}
}
