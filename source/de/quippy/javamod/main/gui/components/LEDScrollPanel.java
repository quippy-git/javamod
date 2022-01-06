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
import java.awt.Graphics;
import java.util.HashMap;

import de.quippy.javamod.system.Log;

/**
 * @author Daniel Becker
 * @since 21.09.2008
 */
public class LEDScrollPanel extends MeterPanelBase
{
	private static final long serialVersionUID = 8634277922087324325L;

	private static HashMap<String, byte[]> ledCharSet;
	static
	{
		ledCharSet = new HashMap<String, byte[]>();
		ledCharSet.put(" ", ((new byte[]
		{
				0, 0, 0, 0, 0
		})));
		ledCharSet.put("A", ((new byte[]
		{
				126, 9, 9, 9, 126
		})));
		ledCharSet.put("a", ((new byte[]
		{
				32, 84, 84, 84, 120
		})));
		ledCharSet.put("á", ((new byte[]
		{
		 		32, 84, 86, 85, 120
		})));
		ledCharSet.put("à", ((new byte[]
		{
         		32, 85, 86, 84, 120
		})));
		ledCharSet.put("B", ((new byte[]
		{
				127, 73, 73, 73, 62
		})));
		ledCharSet.put("b", ((new byte[]
		{
				127, 68, 68, 68, 56
		})));
		ledCharSet.put("C", ((new byte[]
		{
				62, 65, 65, 65, 34
		})));
		ledCharSet.put("c", ((new byte[]
		{
				56, 68, 68, 68, 0
		})));
		ledCharSet.put("D", ((new byte[]
		{
				65, 127, 65, 65, 62
		})));
		ledCharSet.put("d", ((new byte[]
		{
				56, 68, 68, 72, 127
		})));
		ledCharSet.put("E", ((new byte[]
		{
				127, 73, 73, 65, 65
		})));
		ledCharSet.put("e", ((new byte[]
		{
				56, 84, 84, 84, 24
		})));
		ledCharSet.put("é", ((new byte[]
		{
				56, 84, 86, 85, 24
		})));
		ledCharSet.put("è", ((new byte[]
		{
				56, 85, 86, 84, 24
		})));
		ledCharSet.put("F", ((new byte[]
		{
				127, 9, 9, 1, 1
		})));
		ledCharSet.put("f", ((new byte[]
		{
				8, 126, 9, 1, 2
		})));
		ledCharSet.put("G", ((new byte[]
		{
				62, 65, 65, 73, 58
		})));
		ledCharSet.put("g", ((new byte[]
		{
				72, 84, 84, 84, 60
		})));
		ledCharSet.put("H", ((new byte[]
		{
				127, 8, 8, 8, 127
		})));
		ledCharSet.put("h", ((new byte[]
		{
				127, 8, 4, 4, 120
		})));
		ledCharSet.put("I", ((new byte[]
		{
				0, 65, 127, 65, 0
		})));
		ledCharSet.put("i", ((new byte[]
		{
				0, 68, 125, 64, 0
		})));
		ledCharSet.put("í", ((new byte[]
		{
				0, 68, 126, 65, 0
		})));
		ledCharSet.put("ì", ((new byte[]
		{
				0, 69, 126, 64, 0
		})));
		ledCharSet.put("J", ((new byte[]
		{
				32, 64, 65, 63, 1
		})));
		ledCharSet.put("j", ((new byte[]
		{
				32, 64, 68, 61, 0
		})));
		ledCharSet.put("K", ((new byte[]
		{
				127, 8, 20, 34, 65
		})));
		ledCharSet.put("k", ((new byte[]
		{
				127, 16, 40, 68, 0
		})));
		ledCharSet.put("L", ((new byte[]
		{
				127, 64, 64, 64, 64
		})));
		ledCharSet.put("l", ((new byte[]
		{
				0, 65, 127, 64, 0
		})));
		ledCharSet.put("M", ((new byte[]
		{
				127, 2, 12, 2, 127
		})));
		ledCharSet.put("m", ((new byte[]
		{
				124, 4, 24, 4, 120
		})));
		ledCharSet.put("N", ((new byte[]
		{
				127, 4, 8, 16, 127
		})));
		ledCharSet.put("n", ((new byte[]
		{
				124, 8, 4, 4, 120
		})));
		ledCharSet.put("O", ((new byte[]
		{
				62, 65, 65, 65, 62
		})));
		ledCharSet.put("o", ((new byte[]
		{
				56, 68, 68, 68, 56
		})));
		ledCharSet.put("ó", ((new byte[]
		{
         		56, 68, 70, 69, 56
		})));
		ledCharSet.put("ò", ((new byte[]
		{
         		56, 69, 70, 68, 56
		})));
		ledCharSet.put("P", ((new byte[]
		{
				127, 9, 9, 9, 6
		})));
		ledCharSet.put("p", ((new byte[]
		{
				124, 20, 20, 20, 8
		})));
		ledCharSet.put("Q", ((new byte[]
		{
				62, 65, 81, 33, 94
		})));
		ledCharSet.put("q", ((new byte[]
		{
				8, 20, 20, 20, 124
		})));
		ledCharSet.put("R", ((new byte[]
		{
				127, 9, 25, 41, 70
		})));
		ledCharSet.put("r", ((new byte[]
		{
				124, 8, 4, 4, 8
		})));
		ledCharSet.put("S", ((new byte[]
		{
				38, 73, 73, 73, 50
		})));
		ledCharSet.put("s", ((new byte[]
		{
				72, 84, 84, 84, 32
		})));
		ledCharSet.put("T", ((new byte[]
		{
				1, 1, 127, 1, 1
		})));
		ledCharSet.put("t", ((new byte[]
		{
				4, 63, 68, 64, 64
		})));
		ledCharSet.put("U", ((new byte[]
		{
				63, 64, 64, 64, 63
		})));
		ledCharSet.put("u", ((new byte[]
		{
				60, 64, 64, 32, 124
		})));
		ledCharSet.put("ú", ((new byte[]
		{
				60, 64, 66, 33, 124
		})));
		ledCharSet.put("ù", ((new byte[]
		{
				60, 65, 66, 32, 124
		})));
		ledCharSet.put("V", ((new byte[]
		{
				7, 24, 96, 24, 7
		})));
		ledCharSet.put("v", ((new byte[]
		{
				28, 32, 64, 32, 28
		})));
		ledCharSet.put("W", ((new byte[]
		{
				127, 32, 24, 32, 127
		})));
		ledCharSet.put("w", ((new byte[]
		{
				60, 64, 48, 64, 60
		})));
		ledCharSet.put("X", ((new byte[]
		{
				99, 20, 8, 20, 99
		})));
		ledCharSet.put("x", ((new byte[]
		{
				68, 40, 16, 40, 68
		})));
		ledCharSet.put("Y", ((new byte[]
		{
				7, 8, 120, 8, 7
		})));
		ledCharSet.put("y", ((new byte[]
		{
				12, 80, 80, 80, 60
		})));
		ledCharSet.put("Z", ((new byte[]
		{
				97, 81, 73, 69, 67
		})));
		ledCharSet.put("z", ((new byte[]
		{
				68, 100, 84, 76, 68
		})));
		ledCharSet.put("0", ((new byte[]
		{
				62, 81, 73, 69, 62
		})));
		ledCharSet.put("1", ((new byte[]
		{
				0, 66, 127, 64, 0
		})));
		ledCharSet.put("2", ((new byte[]
		{
				98, 81, 81, 73, 70
		})));
		ledCharSet.put("3", ((new byte[]
		{
				34, 65, 73, 73, 54
		})));
		ledCharSet.put("4", ((new byte[]
		{
				24, 20, 18, 127, 16
		})));
		ledCharSet.put("5", ((new byte[]
		{
				39, 69, 69, 69, 57
		})));
		ledCharSet.put("6", ((new byte[]
		{
				60, 74, 73, 73, 49
		})));
		ledCharSet.put("7", ((new byte[]
		{
				1, 113, 9, 5, 3
		})));
		ledCharSet.put("8", ((new byte[]
		{
				54, 73, 73, 73, 54
		})));
		ledCharSet.put("9", ((new byte[]
		{
				70, 73, 73, 41, 30
		})));
		ledCharSet.put("~", ((new byte[]
		{
				2, 1, 2, 4, 2
		})));
		ledCharSet.put("`", ((new byte[]
		{
				1, 2, 4, 0, 0
		})));
		ledCharSet.put("!", ((new byte[]
		{
				0, 0, 111, 0, 0
		})));
		ledCharSet.put("@", ((new byte[]
		{
				62, 65, 93, 85, 14
		})));
		ledCharSet.put("#", ((new byte[]
		{
				20, 127, 20, 127, 20
		})));
		ledCharSet.put("$", ((new byte[]
		{
				44, 42, 127, 42, 26
		})));
		ledCharSet.put("%", ((new byte[]
		{
				38, 22, 8, 52, 50
		})));
		ledCharSet.put("^", ((new byte[]
		{
				4, 2, 1, 2, 4
		})));
		ledCharSet.put("&", ((new byte[]
		{
				54, 73, 86, 32, 80
		})));
		ledCharSet.put("*", ((new byte[]
		{
				42, 28, 127, 28, 42
		})));
		ledCharSet.put("(", ((new byte[]
		{
				0, 0, 62, 65, 0
		})));
		ledCharSet.put(")", ((new byte[]
		{
				0, 65, 62, 0, 0
		})));
		ledCharSet.put("-", ((new byte[]
		{
				8, 8, 8, 8, 8
		})));
		ledCharSet.put("_", ((new byte[]
		{
				64, 64, 64, 64, 64
		})));
		ledCharSet.put("+", ((new byte[]
		{
				8, 8, 127, 8, 8
		})));
		ledCharSet.put("=", ((new byte[]
		{
				36, 36, 36, 36, 36
		})));
		ledCharSet.put("\\", ((new byte[]
		{
				3, 4, 8, 16, 96
		})));
		ledCharSet.put("|", ((new byte[]
		{
				0, 0, 127, 0, 0
		})));
		ledCharSet.put("{", ((new byte[]
		{
				0, 8, 54, 65, 65
		})));
		ledCharSet.put("}", ((new byte[]
		{
				65, 65, 54, 8, 0
		})));
		ledCharSet.put("[", ((new byte[]
		{
				0, 127, 65, 65, 0
		})));
		ledCharSet.put("]", ((new byte[]
		{
				0, 65, 65, 127, 0
		})));
		ledCharSet.put(":", ((new byte[]
		{
				0, 0, 54, 54, 0
		})));
		ledCharSet.put(";", ((new byte[]
		{
				0, 91, 59, 0, 0
		})));
		ledCharSet.put(",", ((new byte[]
		{
				0, 0, 88, 56, 0
		})));
		ledCharSet.put(".", ((new byte[]
		{
				0, 96, 96, 0, 0
		})));
		ledCharSet.put("<", ((new byte[]
		{
				8, 20, 34, 65, 0
		})));
		ledCharSet.put(">", ((new byte[]
		{
				65, 34, 20, 8, 0
		})));
		ledCharSet.put("?", ((new byte[]
		{
				2, 1, 89, 5, 2
		})));
		ledCharSet.put("/", ((new byte[]
		{
				96, 16, 8, 4, 3
		})));
		ledCharSet.put("'", ((new byte[]
		{
				0, 0, 7, 0, 0
		})));
		ledCharSet.put("\"", ((new byte[]
		{
				0, 7, 0, 7, 0
		})));
		ledCharSet.put("´", ((new byte[]
		{
         		0, 0, 4, 2, 1
		})));
		ledCharSet.put("`", ((new byte[]
		{
         		1, 2, 4, 0, 0
		})));
		ledCharSet.put("\344", ((new byte[]
		{
				32, 85, 84, 85, 120
		})));
		ledCharSet.put("\304", ((new byte[]
		{
				124, 19, 18, 19, 124
		})));
		ledCharSet.put("\366", ((new byte[]
		{
				56, 69, 68, 69, 56
		})));
		ledCharSet.put("\326", ((new byte[]
		{
				60, 67, 66, 67, 60
		})));
		ledCharSet.put("\374", ((new byte[]
		{
				60, 65, 64, 33, 124
		})));
		ledCharSet.put("\334", ((new byte[]
		{
				62, 65, 64, 65, 62
		})));
		ledCharSet.put("\337", ((new byte[]
		{
				126, 33, 73, 78, 112
		})));
		ledCharSet.put("\u20AC", ((new byte[] // €
		{
				62, 85, 85, 65, 34
		})));
		ledCharSet.put("\u2591", ((new byte[] // ░
		{
				85, 0, 85, 0, 85
		})));
		ledCharSet.put("\u2592", ((new byte[] // 
		{
				85, 42, 85, 42, 85
		})));
		ledCharSet.put("©", ((new byte[]
		{
				62, 73, 85, 85, 62
		})));
	}

	private String scrollText;
	private int brickWidth;
	private int brickHeight;
	private int anzChars;
	private int fullWidth;
	private int fullHeight;
	
	private int appendIndexMarker;
	private int scrollTextIndex;
	private int scrollBufferIndex;
	private byte[][] currentScrollLayer;
	
	private Color darkColor;
	private Color lightColor;
	
	/**
	 * Constructor for LEDScrollPanel
	 */
	public LEDScrollPanel(int updateRate, String scrollText, int anzChars, Color lightColor, Color darkColor)
	{
		super(updateRate);
		this.appendIndexMarker = -1;
		this.scrollText = scrollText;
		this.anzChars = anzChars;
		this.brickWidth = 1;
		this.brickHeight = 1;
		// Each char is 6 dots wide (inc empty space between chars)
		this.fullWidth = anzChars * 6;
		// Each char is 8 dots in height
		this.fullHeight = 8;
		this.scrollBufferIndex = 0;
		this.scrollTextIndex = -1;
		this.currentScrollLayer = new byte[this.anzChars + 1][];
		this.darkColor = darkColor;
		this.lightColor = lightColor;
		
		startThread();
	}
	public synchronized void setScrollTextTo(String newScrollText)
	{
		for (int i=0; i<=anzChars; i++) currentScrollLayer[i] = null;
		this.appendIndexMarker = -1;
		this.scrollBufferIndex = 0;
		this.scrollTextIndex = -1;
		this.scrollText = newScrollText;
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
		int newAnzChars = (int)((newWidth / (6.0 * brickWidth))+0.5) + 1;
		if (newAnzChars != anzChars)
		{
			this.anzChars = newAnzChars;
			this.currentScrollLayer = new byte[this.anzChars + 1][];
			this.setScrollTextTo(this.scrollText);
		}
	}
	private int drawDots(Graphics g, int startIndex, int width, int x, byte[] charBuffer, int compWidth, int compHeight)
	{
		for (; startIndex<width; startIndex++)
		{
			byte line = (startIndex<5)?((charBuffer==null)?0:charBuffer[startIndex]):0;
			int x2 = x + brickWidth - 2;
			if (x2<x) x2 = x;
			for (int y=0; y<8; y++)
			{
				int c = line & (1<<y);
				
				g.setColor((c==0)?darkColor:lightColor);
				
				int y1 = y * brickHeight;
				int y2 = y1 + brickHeight - 2;
				if (y2<y1) y2 = y1;
				for (; y1<=y2; y1++)
					g.drawLine(x, y1, x2, y1);
			}
			x+=brickWidth;
		}
		
		g.setColor(darkColor);
		for (int y = 8*brickHeight; y<compHeight; y++)
			g.drawLine(0, y, compWidth, y);

		return x;
	}
	/**
	 * @param g
	 * @see de.quippy.javamod.main.gui.components.MeterPanelBase#drawMeter(java.awt.Graphics)
	 */
	@Override
	protected synchronized void drawMeter(Graphics g, int newTop, int newLeft, int newWidth, int newHeight)
	{
		scrollBufferIndex++;
		if (scrollBufferIndex>=6)
		{
			scrollBufferIndex = 0;
			scrollTextIndex++;
			if (scrollTextIndex>=scrollText.length()) scrollTextIndex = 0;
			
			if (appendIndexMarker!=-1 && scrollTextIndex>=appendIndexMarker)
			{
				scrollText = scrollText.substring(appendIndexMarker);
				scrollTextIndex-=appendIndexMarker;
				appendIndexMarker = -1;
			}
			
			String c = String.valueOf(scrollText.charAt(scrollTextIndex));
			byte [] newChar = ledCharSet.get(c);
			if (newChar==null)
			{
				Log.debug("Charachter unknown: " + c + "[\\u"+ Integer.toHexString((int)c.charAt(0)) +"]");
				newChar=ledCharSet.get("?");
			}
			
			for (int i=0; i<anzChars; i++) currentScrollLayer[i] = currentScrollLayer[i+1];
			currentScrollLayer[anzChars] = newChar;
		}

		if (g==null) return;
		
		int startIndex = scrollBufferIndex % 6;
		int x=0;
		for (int i=0; i<anzChars; i++)
		{
			byte[] display = currentScrollLayer[i];
			int start = (i==0)?startIndex:0;
			x = drawDots(g, start, 6, x, display, newWidth, newHeight);
		}
	}
}
