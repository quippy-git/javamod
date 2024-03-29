/*
 * @(#) LEDScrollPanel.java
 *
 * Created on 21.09.2008 by Daniel Becker
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
import java.awt.Graphics2D;
import java.util.HashMap;

import de.quippy.javamod.system.Log;

/**
 * @author Daniel Becker
 * @since 21.09.2008
 */
public class LEDScrollPanel extends MeterPanelBase
{
	private static final long serialVersionUID = 8634277922087324325L;

	private static HashMap<Character, byte[]> ledCharSet;
	static
	{
		ledCharSet = new HashMap<Character, byte[]>();
		ledCharSet.put(Character.valueOf(' '), ((new byte[]
		{
				0, 0, 0, 0, 0
		})));
		ledCharSet.put(Character.valueOf('A'), ((new byte[]
		{
				126, 9, 9, 9, 126
		})));
		ledCharSet.put(Character.valueOf('a'), ((new byte[]
		{
				32, 84, 84, 84, 120
		})));
		ledCharSet.put(Character.valueOf('á'), ((new byte[]
		{
		 		32, 84, 86, 85, 120
		})));
		ledCharSet.put(Character.valueOf('à'), ((new byte[]
		{
         		32, 85, 86, 84, 120
		})));
		ledCharSet.put(Character.valueOf('B'), ((new byte[]
		{
				127, 73, 73, 73, 62
		})));
		ledCharSet.put(Character.valueOf('b'), ((new byte[]
		{
				127, 68, 68, 68, 56
		})));
		ledCharSet.put(Character.valueOf('C'), ((new byte[]
		{
				62, 65, 65, 65, 34
		})));
		ledCharSet.put(Character.valueOf('c'), ((new byte[]
		{
				56, 68, 68, 68, 0
		})));
		ledCharSet.put(Character.valueOf('D'), ((new byte[]
		{
				65, 127, 65, 65, 62
		})));
		ledCharSet.put(Character.valueOf('d'), ((new byte[]
		{
				56, 68, 68, 72, 127
		})));
		ledCharSet.put(Character.valueOf('E'), ((new byte[]
		{
				127, 73, 73, 65, 65
		})));
		ledCharSet.put(Character.valueOf('e'), ((new byte[]
		{
				56, 84, 84, 84, 24
		})));
		ledCharSet.put(Character.valueOf('é'), ((new byte[]
		{
				56, 84, 86, 85, 24
		})));
		ledCharSet.put(Character.valueOf('è'), ((new byte[]
		{
				56, 85, 86, 84, 24
		})));
		ledCharSet.put(Character.valueOf('F'), ((new byte[]
		{
				127, 9, 9, 1, 1
		})));
		ledCharSet.put(Character.valueOf('f'), ((new byte[]
		{
				8, 126, 9, 1, 2
		})));
		ledCharSet.put(Character.valueOf('G'), ((new byte[]
		{
				62, 65, 65, 73, 58
		})));
		ledCharSet.put(Character.valueOf('g'), ((new byte[]
		{
				72, 84, 84, 84, 60
		})));
		ledCharSet.put(Character.valueOf('H'), ((new byte[]
		{
				127, 8, 8, 8, 127
		})));
		ledCharSet.put(Character.valueOf('h'), ((new byte[]
		{
				127, 8, 4, 4, 120
		})));
		ledCharSet.put(Character.valueOf('I'), ((new byte[]
		{
				0, 65, 127, 65, 0
		})));
		ledCharSet.put(Character.valueOf('i'), ((new byte[]
		{
				0, 68, 125, 64, 0
		})));
		ledCharSet.put(Character.valueOf('í'), ((new byte[]
		{
				0, 68, 126, 65, 0
		})));
		ledCharSet.put(Character.valueOf('ì'), ((new byte[]
		{
				0, 69, 126, 64, 0
		})));
		ledCharSet.put(Character.valueOf('J'), ((new byte[]
		{
				32, 64, 65, 63, 1
		})));
		ledCharSet.put(Character.valueOf('j'), ((new byte[]
		{
				32, 64, 68, 61, 0
		})));
		ledCharSet.put(Character.valueOf('K'), ((new byte[]
		{
				127, 8, 20, 34, 65
		})));
		ledCharSet.put(Character.valueOf('k'), ((new byte[]
		{
				127, 16, 40, 68, 0
		})));
		ledCharSet.put(Character.valueOf('L'), ((new byte[]
		{
				127, 64, 64, 64, 64
		})));
		ledCharSet.put(Character.valueOf('l'), ((new byte[]
		{
				0, 65, 127, 64, 0
		})));
		ledCharSet.put(Character.valueOf('M'), ((new byte[]
		{
				127, 2, 12, 2, 127
		})));
		ledCharSet.put(Character.valueOf('m'), ((new byte[]
		{
				124, 4, 24, 4, 120
		})));
		ledCharSet.put(Character.valueOf('N'), ((new byte[]
		{
				127, 4, 8, 16, 127
		})));
		ledCharSet.put(Character.valueOf('n'), ((new byte[]
		{
				124, 8, 4, 4, 120
		})));
		ledCharSet.put(Character.valueOf('O'), ((new byte[]
		{
				62, 65, 65, 65, 62
		})));
		ledCharSet.put(Character.valueOf('o'), ((new byte[]
		{
				56, 68, 68, 68, 56
		})));
		ledCharSet.put(Character.valueOf('ó'), ((new byte[]
		{
         		56, 68, 70, 69, 56
		})));
		ledCharSet.put(Character.valueOf('ò'), ((new byte[]
		{
         		56, 69, 70, 68, 56
		})));
		ledCharSet.put(Character.valueOf('P'), ((new byte[]
		{
				127, 9, 9, 9, 6
		})));
		ledCharSet.put(Character.valueOf('p'), ((new byte[]
		{
				124, 20, 20, 20, 8
		})));
		ledCharSet.put(Character.valueOf('Q'), ((new byte[]
		{
				62, 65, 81, 33, 94
		})));
		ledCharSet.put(Character.valueOf('q'), ((new byte[]
		{
				8, 20, 20, 20, 124
		})));
		ledCharSet.put(Character.valueOf('R'), ((new byte[]
		{
				127, 9, 25, 41, 70
		})));
		ledCharSet.put(Character.valueOf('r'), ((new byte[]
		{
				124, 8, 4, 4, 8
		})));
		ledCharSet.put(Character.valueOf('S'), ((new byte[]
		{
				38, 73, 73, 73, 50
		})));
		ledCharSet.put(Character.valueOf('s'), ((new byte[]
		{
				72, 84, 84, 84, 32
		})));
		ledCharSet.put(Character.valueOf('T'), ((new byte[]
		{
				1, 1, 127, 1, 1
		})));
		ledCharSet.put(Character.valueOf('t'), ((new byte[]
		{
				4, 63, 68, 64, 64
		})));
		ledCharSet.put(Character.valueOf('U'), ((new byte[]
		{
				63, 64, 64, 64, 63
		})));
		ledCharSet.put(Character.valueOf('u'), ((new byte[]
		{
				60, 64, 64, 32, 124
		})));
		ledCharSet.put(Character.valueOf('ú'), ((new byte[]
		{
				60, 64, 66, 33, 124
		})));
		ledCharSet.put(Character.valueOf('ù'), ((new byte[]
		{
				60, 65, 66, 32, 124
		})));
		ledCharSet.put(Character.valueOf('V'), ((new byte[]
		{
				7, 24, 96, 24, 7
		})));
		ledCharSet.put(Character.valueOf('v'), ((new byte[]
		{
				28, 32, 64, 32, 28
		})));
		ledCharSet.put(Character.valueOf('W'), ((new byte[]
		{
				127, 32, 24, 32, 127
		})));
		ledCharSet.put(Character.valueOf('w'), ((new byte[]
		{
				60, 64, 48, 64, 60
		})));
		ledCharSet.put(Character.valueOf('X'), ((new byte[]
		{
				99, 20, 8, 20, 99
		})));
		ledCharSet.put(Character.valueOf('x'), ((new byte[]
		{
				68, 40, 16, 40, 68
		})));
		ledCharSet.put(Character.valueOf('Y'), ((new byte[]
		{
				7, 8, 120, 8, 7
		})));
		ledCharSet.put(Character.valueOf('y'), ((new byte[]
		{
				12, 80, 80, 80, 60
		})));
		ledCharSet.put(Character.valueOf('Z'), ((new byte[]
		{
				97, 81, 73, 69, 67
		})));
		ledCharSet.put(Character.valueOf('z'), ((new byte[]
		{
				68, 100, 84, 76, 68
		})));
		ledCharSet.put(Character.valueOf('0'), ((new byte[]
		{
				62, 81, 73, 69, 62
		})));
		ledCharSet.put(Character.valueOf('1'), ((new byte[]
		{
				0, 66, 127, 64, 0
		})));
		ledCharSet.put(Character.valueOf('2'), ((new byte[]
		{
				98, 81, 81, 73, 70
		})));
		ledCharSet.put(Character.valueOf('3'), ((new byte[]
		{
				34, 65, 73, 73, 54
		})));
		ledCharSet.put(Character.valueOf('4'), ((new byte[]
		{
				24, 20, 18, 127, 16
		})));
		ledCharSet.put(Character.valueOf('5'), ((new byte[]
		{
				39, 69, 69, 69, 57
		})));
		ledCharSet.put(Character.valueOf('6'), ((new byte[]
		{
				60, 74, 73, 73, 49
		})));
		ledCharSet.put(Character.valueOf('7'), ((new byte[]
		{
				1, 113, 9, 5, 3
		})));
		ledCharSet.put(Character.valueOf('8'), ((new byte[]
		{
				54, 73, 73, 73, 54
		})));
		ledCharSet.put(Character.valueOf('9'), ((new byte[]
		{
				70, 73, 73, 41, 30
		})));
		ledCharSet.put(Character.valueOf('~'), ((new byte[]
		{
				2, 1, 2, 4, 2
		})));
		ledCharSet.put(Character.valueOf('`'), ((new byte[]
		{
				1, 2, 4, 0, 0
		})));
		ledCharSet.put(Character.valueOf('!'), ((new byte[]
		{
				0, 0, 111, 0, 0
		})));
		ledCharSet.put(Character.valueOf('@'), ((new byte[]
		{
				62, 65, 93, 85, 14
		})));
		ledCharSet.put(Character.valueOf('#'), ((new byte[]
		{
				20, 127, 20, 127, 20
		})));
		ledCharSet.put(Character.valueOf('$'), ((new byte[]
		{
				44, 42, 127, 42, 26
		})));
		ledCharSet.put(Character.valueOf('%'), ((new byte[]
		{
				38, 22, 8, 52, 50
		})));
		ledCharSet.put(Character.valueOf('^'), ((new byte[]
		{
				4, 2, 1, 2, 4
		})));
		ledCharSet.put(Character.valueOf('&'), ((new byte[]
		{
				54, 73, 86, 32, 80
		})));
		ledCharSet.put(Character.valueOf('*'), ((new byte[]
		{
				42, 28, 127, 28, 42
		})));
		ledCharSet.put(Character.valueOf('('), ((new byte[]
		{
				0, 0, 62, 65, 0
		})));
		ledCharSet.put(Character.valueOf(')'), ((new byte[]
		{
				0, 65, 62, 0, 0
		})));
		ledCharSet.put(Character.valueOf('-'), ((new byte[]
		{
				8, 8, 8, 8, 8
		})));
		ledCharSet.put(Character.valueOf('_'), ((new byte[]
		{
				64, 64, 64, 64, 64
		})));
		ledCharSet.put(Character.valueOf('+'), ((new byte[]
		{
				8, 8, 127, 8, 8
		})));
		ledCharSet.put(Character.valueOf('='), ((new byte[]
		{
				36, 36, 36, 36, 36
		})));
		ledCharSet.put(Character.valueOf('\\'), ((new byte[]
		{
				3, 4, 8, 16, 96
		})));
		ledCharSet.put(Character.valueOf('|'), ((new byte[]
		{
				0, 0, 127, 0, 0
		})));
		ledCharSet.put(Character.valueOf('{'), ((new byte[]
		{
				0, 8, 54, 65, 65
		})));
		ledCharSet.put(Character.valueOf('}'), ((new byte[]
		{
				65, 65, 54, 8, 0
		})));
		ledCharSet.put(Character.valueOf('['), ((new byte[]
		{
				0, 127, 65, 65, 0
		})));
		ledCharSet.put(Character.valueOf(']'), ((new byte[]
		{
				0, 65, 65, 127, 0
		})));
		ledCharSet.put(Character.valueOf(':'), ((new byte[]
		{
				0, 0, 54, 54, 0
		})));
		ledCharSet.put(Character.valueOf(';'), ((new byte[]
		{
				0, 91, 59, 0, 0
		})));
		ledCharSet.put(Character.valueOf(','), ((new byte[]
		{
				0, 0, 88, 56, 0
		})));
		ledCharSet.put(Character.valueOf('.'), ((new byte[]
		{
				0, 96, 96, 0, 0
		})));
		ledCharSet.put(Character.valueOf('<'), ((new byte[]
		{
				8, 20, 34, 65, 0
		})));
		ledCharSet.put(Character.valueOf('>'), ((new byte[]
		{
				65, 34, 20, 8, 0
		})));
		ledCharSet.put(Character.valueOf('?'), ((new byte[]
		{
				2, 1, 89, 5, 2
		})));
		ledCharSet.put(Character.valueOf('/'), ((new byte[]
		{
				96, 16, 8, 4, 3
		})));
		ledCharSet.put(Character.valueOf('\''), ((new byte[]
		{
				0, 0, 7, 0, 0
		})));
		ledCharSet.put(Character.valueOf('\"'), ((new byte[]
		{
				0, 7, 0, 7, 0
		})));
		ledCharSet.put(Character.valueOf('´'), ((new byte[]
		{
         		0, 0, 4, 2, 1
		})));
		ledCharSet.put(Character.valueOf('`'), ((new byte[]
		{
         		1, 2, 4, 0, 0
		})));
		ledCharSet.put(Character.valueOf('\344'), ((new byte[]
		{
				32, 85, 84, 85, 120
		})));
		ledCharSet.put(Character.valueOf('\304'), ((new byte[]
		{
				124, 19, 18, 19, 124
		})));
		ledCharSet.put(Character.valueOf('\366'), ((new byte[]
		{
				56, 69, 68, 69, 56
		})));
		ledCharSet.put(Character.valueOf('\326'), ((new byte[]
		{
				60, 67, 66, 67, 60
		})));
		ledCharSet.put(Character.valueOf('\374'), ((new byte[]
		{
				60, 65, 64, 33, 124
		})));
		ledCharSet.put(Character.valueOf('\334'), ((new byte[]
		{
				62, 65, 64, 65, 62
		})));
		ledCharSet.put(Character.valueOf('\337'), ((new byte[]
		{
				126, 33, 73, 78, 112
		})));
		ledCharSet.put(Character.valueOf('\u20AC'), ((new byte[] // €
		{
				62, 85, 85, 65, 34
		})));
		ledCharSet.put(Character.valueOf('\u2591'), ((new byte[] // ░
		{
				85, 0, 85, 0, 85
		})));
		ledCharSet.put(Character.valueOf('\u2592'), ((new byte[] // 
		{
				85, 42, 85, 42, 85
		})));
		ledCharSet.put(Character.valueOf('©'), ((new byte[]
		{
				62, 73, 85, 85, 62
		})));
	}
	
	private static final int BYTES_PER_CHAR = 5;
	private static final int DISPLAY_BYTES_PER_CHAR = 6;

	private String scrollText;
	private int brickWidth;
	private int brickHeight;
	private int smallBrickWidth;
	private int smallBrickHeight;
	private int anzChars;
	private int fullWidth;
	private int fullHeight;
	
	private int appendIndexMarker;
	private int scrollTextIndex;
	private int scrollBufferIndex;
	private byte[][] currentScrollLayer;
	
	private Color darkColor;
	private Color lightColor;
	
	private int syncToFPScounter;
	private int syncToFPSAdd;
	// lets avoid floats, so we use the good old fractions by shifting
	private static final int SYNC2FPS_BITS	= 16;
	private static final int SYNC2FPS_FRAC	= 1<<SYNC2FPS_BITS;
	private static final int SYNC2FPS_MASK	= SYNC2FPS_FRAC-1;
	private static final int DEFAULT_FPS	= 25;

	/**
	 * Constructor for LEDScrollPanel
	 */
	public LEDScrollPanel(final int updateRate, final String theScrollText, final int displayChars, final Color displayLightColor, final Color displayDarkColor)
	{
		super(updateRate);
		appendIndexMarker = -1;
		scrollText = theScrollText;
		anzChars = displayChars;
		brickWidth = 1;
		brickHeight = 1;
		smallBrickWidth = 0;
		smallBrickHeight = 0;
		// Each char is 6 dots wide (inc empty space between chars)
		fullWidth = anzChars * DISPLAY_BYTES_PER_CHAR;
		// Each char is 8 dots in height
		fullHeight = 8;
		scrollBufferIndex = 0;
		scrollTextIndex = -1;
		currentScrollLayer = new byte[anzChars + 1][];
		darkColor = displayDarkColor;
		lightColor = displayLightColor;
		
		syncToFPScounter = 0;
		syncToFPSAdd = (DEFAULT_FPS<<SYNC2FPS_BITS) / updateRate;
		
		startThread();
	}
	public synchronized void setScrollTextTo(String newScrollText)
	{
		for (int i=0; i<=anzChars; i++) currentScrollLayer[i] = null;
		appendIndexMarker = -1;
		scrollBufferIndex = 0;
		scrollTextIndex = -1;
		scrollText = newScrollText;
	}
	public synchronized void addScrollText(String appender)
	{
		if (appendIndexMarker!=-1)
		{
			if (scrollTextIndex>=appendIndexMarker)
			{
				scrollText = scrollText.substring(appendIndexMarker);
				scrollTextIndex -= appendIndexMarker;
			}
			else
			{
				scrollText = scrollText.substring(0, appendIndexMarker);
			}
		}
		appendIndexMarker=scrollText.length();
		scrollText += appender;
	}
	/**
	 * 
	 * @see de.quippy.javamod.main.gui.components.MeterPanelBase#componentWasResized()
	 */
	@Override
	protected synchronized void componentWasResized(int newTop, int newLeft, int newWidth, int newHeight)
	{
		brickWidth = newWidth / fullWidth;
		brickHeight = newHeight / fullHeight;
		if (brickWidth==0) brickWidth = 1;
		if (brickHeight==0) brickHeight = 1;
		smallBrickWidth = brickWidth - 1;
		smallBrickHeight = brickHeight - 1;

		final int newAnzChars = (int)(((float)newWidth / ((float)DISPLAY_BYTES_PER_CHAR * (float)brickWidth))+0.5f) + 1;
		if (newAnzChars != anzChars)
		{
			anzChars = newAnzChars;
			currentScrollLayer = new byte[anzChars + 1][];
			setScrollTextTo(scrollText);
		}
	}
	private int drawDots(Graphics2D g, int startIndex, int x, byte[] charBuffer, int compWidth, int compHeight)
	{
		for (; startIndex<DISPLAY_BYTES_PER_CHAR; startIndex++)
		{
			byte line = (startIndex<BYTES_PER_CHAR)?((charBuffer==null)?0:charBuffer[startIndex]):0;
			for (int y=0; y<8; y++)
			{
				int c = line & (1<<y);
				
				g.setColor((c==0)?darkColor:lightColor);
				g.fillRect(x, y * brickHeight, smallBrickWidth, smallBrickHeight);
			}
			g.setColor(darkColor);
			g.fillRect(x, 8*brickHeight, smallBrickWidth, compHeight);
			x+=brickWidth;
		}
		
		return x;
	}
	/**
	 * @param g
	 * @see de.quippy.javamod.main.gui.components.MeterPanelBase#drawMeter(java.awt.Graphics)
	 */
	@Override
	protected synchronized void drawMeter(Graphics2D g, int newTop, int newLeft, int newWidth, int newHeight)
	{
		syncToFPScounter += syncToFPSAdd;
		if (syncToFPScounter >= SYNC2FPS_FRAC)
		{
			scrollBufferIndex++;
			syncToFPScounter &= SYNC2FPS_MASK;
			
			if (scrollBufferIndex>=DISPLAY_BYTES_PER_CHAR)
			{
				scrollTextIndex += scrollBufferIndex / DISPLAY_BYTES_PER_CHAR;
				scrollBufferIndex %= DISPLAY_BYTES_PER_CHAR;

				if (scrollTextIndex>=scrollText.length()) scrollTextIndex = 0;
				
				if (appendIndexMarker!=-1 && scrollTextIndex>=appendIndexMarker)
				{
					scrollText = scrollText.substring(appendIndexMarker);
					scrollTextIndex-=appendIndexMarker;
					appendIndexMarker = -1;
				}
				
				Character c = Character.valueOf(scrollText.charAt(scrollTextIndex));
				byte [] newChar = ledCharSet.get(c);
				if (newChar==null)
				{
					Log.debug("Charachter unknown: " + c.toString() + "[\\u"+ Integer.toHexString((int)c.charValue()) +"]");
					newChar=ledCharSet.get(Character.valueOf('?'));
				}
				
				for (int i=0; i<anzChars; i++) currentScrollLayer[i] = currentScrollLayer[i+1];
				currentScrollLayer[anzChars] = newChar;
			}
	
			if (g==null) return;
			
			final int startIndex = scrollBufferIndex % DISPLAY_BYTES_PER_CHAR;
			int x=0;
			for (int i=0; i<anzChars; i++)
			{
				byte[] display = currentScrollLayer[i];
				int start = (i==0)?startIndex:0;
				x = drawDots(g, start, x, display, newWidth, newHeight);
			}
		}
	}
}
