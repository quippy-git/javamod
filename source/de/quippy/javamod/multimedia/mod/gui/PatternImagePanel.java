/*
 * @(#) PatternImagePanel.java
 *
 * Created on 04.01.2024 by Daniel Becker
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

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JComponent;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

import de.quippy.javamod.multimedia.mod.ModConstants;
import de.quippy.javamod.multimedia.mod.loader.pattern.Pattern;
import de.quippy.javamod.multimedia.mod.loader.pattern.PatternElement;
import de.quippy.javamod.multimedia.mod.loader.pattern.PatternRow;

/**
 * @author Daniel Becker
 * @since 04.01.2024
 */
public class PatternImagePanel extends JComponent implements Scrollable
{
	private static final long serialVersionUID = -8032820780987918878L;

	private static final Color SELECTION_COLOR		= new Color(0xc0c0c0);
	private static final Color PLAY_SELECTION_COLOR	= new Color(0xffff80);

	private static final Color[]   BACKGROUND		=  { new Color(0xFFFFFF), new Color(0xf6f6f6) };
	private static final Color[]   FOREGROUND		=  { new Color(0x000000), new Color(0x787878) };
	private static final Color[]   BUTTONS			=  { new Color(0xCCCCCC), new Color(0xEEEEEE) };
	private static final Color[]   HIGHLIGHT_LINE_1	=  { new Color(0xe0e8e0), new Color(0xeaeaea) };
	private static final Color[]   HIGHLIGHT_LINE_2	=  { new Color(0xf2f6f2), new Color(0xf1f1f1) };
	private static final Color[]   NOTE				=  { new Color(0x000080), new Color(0x888888) };
	private static final Color[]   INSTRUMENT		=  { new Color(0x008880), new Color(0xa8a8a8) };
	private static final Color[][] EFFECT			= {{ FOREGROUND[0]		, FOREGROUND[1]		  }, // standard == foreground
	                                     			   { new Color(0x008000), new Color(0x989898) }, // Volume
	                                             	   { new Color(0x008080), new Color(0xa8a8a8) }, // panning
	                                             	   { new Color(0x808000), new Color(0xa8a8a8) }, // pitch
	                                             	   { new Color(0x800000), new Color(0x888888) }, // global
	                                             	   { new Color(0x400000), new Color(0x404040) }, // unknown
	   												   { new Color(0x808080), new Color(0x9A9A9A) }};// none
	private static final Color[][] PATTERNLINE		= {{ new Color(0xCCCCCC), new Color(0xDDDDDD) },
	                                          		   { new Color(0xAAAAAA), new Color(0xBBBBBB) }};

	private static final int PATTERN_ELEMENT_CHARS = 13;
	private static final int BUTTON_CHARS = 4;

	private Container parentContainer = null;

	private Color selectionColor = SELECTION_COLOR;
	private Color playSelectionColor = PLAY_SELECTION_COLOR;

	private Pattern prevPattern, currentPattern, nextPattern;
	private final int [] columnPositionsX = new int[14]; // positions of pattern elements
	private int buttonLength;			// length in pixel of the buttons
	private int patternElementLength;	// length of a pattern element
	private int patternRowLength;		// length of a row without button
	private int fullRowLength;			// length of a row WITH button
	private int rowsAbove = -1;			// rows above our play indicator
	private int rowsBelow = -1;			// rows below (and with) our play indicator. rowsAbove + rowsBelow are the full amount of displayable rows
	private int currentChannels = -1;	// channels of the currentPattern
	private int parentWidth = -1;		// width of the canvas (JViewport)
	private int parentHeight = -1;		// height of the canvas (JViewport)
	private Dimension charDim = null;	// dimensions of one char

	private PatternImagePosition currentPlayingRow = null;	// the current Pattern/row to display the payer marker on - if its our currentPattern
	private PatternImagePosition currentEditingRow = null;	// the current Pattern/row to display the edit marker on

	/**
	 * Constructor for PatternImagePanel
	 */
	public PatternImagePanel()
	{
		super();
		setDoubleBuffered(true);
	}
//	private Color getColorAsGrayscale(final Color c)
//	{
//		int gray = (((((c.getRed() + c.getGreen() + c.getBlue()) * 10) / 3 ) + 5) / 10)<<1;
//		if (gray>255) gray = 255;
//		else if (gray<0) gray = 0;
//		return new Color(gray, gray, gray);
//	}
	// ------ Scrollable Interface ---------------------------------------------
	@Override
	public Dimension getPreferredScrollableViewportSize()
	{
		return getPreferredSize();
	}
	@Override
	public int getScrollableUnitIncrement(final Rectangle visibleRect, final int orientation, final int direction)
	{
		final Dimension charDim = getCharDimensions();

		if (orientation == SwingConstants.VERTICAL)
			return charDim.height;
		if (orientation == SwingConstants.HORIZONTAL)
			return charDim.width;

		return 8;
	}
	@Override
	public int getScrollableBlockIncrement(final Rectangle visibleRect, final int orientation, final int direction)
	{
		return getScrollableUnitIncrement(visibleRect, orientation, direction) * 4;
	}
	@Override
	public boolean getScrollableTracksViewportWidth()
	{
		return false;
	}
	@Override
	public boolean getScrollableTracksViewportHeight()
	{
		return false;
	}
	// ------ JComponent Overrides ---------------------------------------------
	/**
	 *
	 * @see javax.swing.JComponent#addNotify()
	 */
	@Override
	public void addNotify()
	{
		super.addNotify();
		parentContainer = getParent();
		if (parentContainer instanceof JViewport)
		{
			((JViewport)parentContainer).addComponentListener(new ComponentListener()
			{
				@Override
				public void componentShown(final ComponentEvent e) {}
				@Override
				public void componentMoved(final ComponentEvent e) {}
				@Override
				public void componentHidden(final ComponentEvent e) {}
				@Override
				public void componentResized(final ComponentEvent e)
				{
					if (resizeForPattern()) scrollOrRepaint(new Rectangle(-1, 0, parentWidth, parentHeight));
				}
			});
		}
	}
	/**
	 * @param font
	 * @see javax.swing.JComponent#setFont(java.awt.Font)
	 */
	@Override
	public void setFont(final Font font)
	{
		super.setFont(font);
		charDim = null;
		getCharDimensions();
	}
	// ------ public methods ---------------------------------------------------
	public Point model2View(final PatternImagePosition position)
	{
		if (currentPattern==null || position==null) return null;

		final Point p = new Point();
		int row = -1;
		if (prevPattern!=null && position.pattern == prevPattern)
			row = -prevPattern.getRowCount() - rowsAbove;
		else
		if (currentPattern!=null && position.pattern == currentPattern)
			row = rowsAbove;
		else
		if (nextPattern!=null && position.pattern == nextPattern)
			row = rowsAbove + currentPattern.getRowCount();
		if (row==-1) return null;

		p.y = (position.row + row) * getCharDimensions().height;

		p.x = buttonLength + ((position.channel * PATTERN_ELEMENT_CHARS) * getCharDimensions().width);
		if (currentEditingRow.column!=PatternImagePosition.NOT_SET)
		{
			final int index = (currentEditingRow.column-1)<<1;
			p.x += columnPositionsX[index];
		}
		return p;
	}
	public PatternImagePosition view2Model(final Point p)
	{
		if (currentPattern==null || p==null) return null;

		final PatternImagePosition result = new PatternImagePosition();
		if (p.x<buttonLength)
		{
			result.channel = result.column = result.charInView = PatternImagePosition.NOT_SET;
		}
		else
		{
			result.channel = (p.x - buttonLength) / patternElementLength;
			result.charInView = (p.x - buttonLength) / getCharDimensions().width;
			final int charPos = result.charInView % PATTERN_ELEMENT_CHARS;
			if (charPos < 0)				result.column = PatternImagePosition.COLUMN_BEYOND_LEFT;
			else
			if (charPos>= 0 && charPos< 3)	result.column = PatternImagePosition.COLUMN_NOTE;
			else
			if (charPos>= 3 && charPos< 6)	result.column = PatternImagePosition.COLUMN_INSTRUMENT;
			else
			if (charPos>= 6 && charPos< 7)	result.column = PatternImagePosition.COLUMN_VOLEFFECT;
			else
			if (charPos>= 7 && charPos< 9)	result.column = PatternImagePosition.COLUMN_VOLEFFECT_OP;
			else
			if (charPos>= 9 && charPos<10)	result.column = PatternImagePosition.COLUMN_EFFECT;
			else
			if (charPos>=10 && charPos<12)	result.column = PatternImagePosition.COLUMN_EFFECT_OP;
			else
			if (charPos>=12)				result.column = PatternImagePosition.COLUMN_BEYOND_RIGHT;
		}

		result.rowInView = (p.y / getCharDimensions().height);
		if (result.rowInView>=0 && result.rowInView<rowsAbove && prevPattern!=null)
		{
			result.pattern = prevPattern;
			result.row = prevPattern.getRowCount() - rowsAbove + result.rowInView;
		}
		else
		if (result.rowInView>=rowsAbove && result.rowInView-rowsAbove<currentPattern.getRowCount())
		{
			result.pattern = currentPattern;
			result.row = result.rowInView - rowsAbove;
		}
		else
		if (result.rowInView-rowsAbove>=currentPattern.getRowCount() && nextPattern!=null)
		{
			result.pattern = nextPattern;
			result.row = result.rowInView - rowsAbove - currentPattern.getRowCount();
		}
		else
		{
			result.pattern = null;
			result.row = PatternImagePosition.NOT_SET;
		}
		return result;
	}
	public void setSelectionColor(final Color newSelectionColor, final Color newDimSelectionColor)
	{
		selectionColor = newSelectionColor;
		playSelectionColor = newDimSelectionColor;
	}
	public static Color getButtonColor()
	{
		return BUTTONS[0];
	}
	public static int getButtonWidth()
	{
		return BUTTON_CHARS;
	}
	public int getButtonPixelWidth()
	{
		return buttonLength;
	}
	public int getButtonPixelHeight()
	{
		return getCharDimensions().height;
	}
	public static int getRowElementWidth()
	{
		return PATTERN_ELEMENT_CHARS;
	}
	public int getRowElementPixelWidth()
	{
		return patternElementLength;
	}
	public int getRowElementPixelHeight()
	{
		return getCharDimensions().height;
	}
	public void setCurrentPattern(final Pattern newPrevPattern, final Pattern newCurrentPattern, final Pattern newNextPattern)
	{
		prevPattern = newPrevPattern;
		currentPattern = newCurrentPattern;
		nextPattern = newNextPattern;
		resizeForPattern();
		final int row = (currentEditingRow!=null && currentEditingRow.pattern == currentPattern)?currentEditingRow.row:
						(currentPlayingRow!=null && currentPlayingRow.pattern == currentPattern)?currentPlayingRow.row:
						0;
		scrollOrRepaint(new Rectangle(-1, row * getCharDimensions().height, parentWidth, parentHeight));
	}
	public void setActivePlayingRow(final PatternImagePosition newPlayingRow)
	{
		// no followSong or just to remove the marker, so
		// - remove the old marker and
		// - draw a new one, if the currentPattern displayed
		// is also the one we want to see the marker on
		final Graphics2D gfx = (Graphics2D)getGraphics().create();
		try
		{
			final Rectangle clipping = gfx.getClipBounds();
			final Dimension charDim = getCharDimensions();

			final int x = buttonLength;
			// remove the marker, if one is displayed at the current showing pattern
			if (currentPlayingRow!=null && currentPlayingRow.pattern==currentPattern)
			{
				final int row = currentPlayingRow.row;
				final int y = (row + rowsAbove) * charDim.height;
				currentPlayingRow = null;
				drawPatternRow(gfx, x, y, currentPattern.getPatternRow(row), 0, row, clipping, charDim);
			}
			// draw the new one, if we see the newCurrentPattern
			currentPlayingRow = newPlayingRow;
			if (currentPlayingRow!=null && currentPlayingRow.pattern==currentPattern)
			{
				final int row = currentPlayingRow.row;
				final int y = (row + rowsAbove) * charDim.height;
				drawPatternRow(gfx, x, y, currentPattern.getPatternRow(row), 0, row, clipping, charDim);
			}
		}
		finally
		{
			gfx.dispose();
		}
	}
	public void setActiveEditingRow(final Pattern newPrevPattern, final Pattern newCurrentPattern, final Pattern newNextPattern, final PatternImagePosition newEditingRow)
	{
		if (newEditingRow!=null)
		{
			currentEditingRow = newEditingRow;
			if (newCurrentPattern!=currentPattern)
				setCurrentPattern(newPrevPattern, newCurrentPattern, newNextPattern);
			else
			{
				int x = -1;
				int width = parentWidth;
				if (currentEditingRow.column != PatternImagePosition.NOT_SET)
				{
					final Point p = model2View(currentEditingRow);
					if (p!=null)
					{
						x = (currentEditingRow.channel==0 && currentEditingRow.column==PatternImagePosition.COLUMN_NOTE)?0:p.x;
						width = columnPositionsX[((currentEditingRow.column-1)<<1)+1] + getCharDimensions().width;
					}
				}
				scrollOrRepaint(new Rectangle(x, currentEditingRow.row * getCharDimensions().height, width, parentHeight));
			}
		}
		else
		{
			// just remove the editing marker
			final Graphics2D gfx = (Graphics2D)getGraphics().create();
			try
			{
				final Rectangle clipping = gfx.getClipBounds();
				final Dimension charDim = getCharDimensions();

				final int x = buttonLength;
				// remove the marker, if one is displayed at the current showing pattern
				if (currentEditingRow!=null && currentEditingRow.pattern==currentPattern)
				{
					final int row = currentEditingRow.row;
					final int y = (row + rowsAbove) * charDim.height;
					currentEditingRow = null; // set to null explicitly to remove
					drawPatternRow(gfx, x, y, currentPattern.getPatternRow(row), 0, row, clipping, charDim);
				}
				// draw the new one, if we see the newCurrentPattern
				// this should be dead code...
//				currentEditingRow = newEditingRow;
//				if (currentEditingRow!=null && currentEditingRow.pattern==currentPattern)
//				{
//					final int row = currentEditingRow.row;
//					final int y = (row + rowsAbove) * charDim.height;
//					drawPatternRow(gfx, x, y, currentPattern.getPatternRow(row), 0, row, clipping, charDim);
//				}
			}
			finally
			{
				gfx.dispose();
			}
		}
	}
	public PatternImagePosition getCurrentPlayingRow()
	{
		return currentPlayingRow;
	}
	public PatternImagePosition getCurrentEditingRow()
	{
		return currentEditingRow;
	}
	// -------------------------------------------------------------------------
	private void scrollOrRepaint(final Rectangle whereTo)
	{
        final Point viewPositionOld = (parentContainer instanceof JViewport)?((JViewport)parentContainer).getViewPosition():null;
        // replace the vertical/horizontal coordinate with the current setting if not specified
        if (viewPositionOld!=null)
        {
        	if (whereTo.x==-1) whereTo.x = viewPositionOld.x;
        	if (whereTo.y==-1) whereTo.y = viewPositionOld.y;
        }
		scrollRectToVisible(whereTo);
        // as scrolling will issue a repaint of the damaged area, we do not want to issue a repaint again
        // but if no scrolling occurred, we will have to. Only repaint what the user wants to see.
        final Point viewPositionNew = (parentContainer instanceof JViewport)?((JViewport)parentContainer).getViewPosition():null;
        if (viewPositionOld==null || viewPositionNew==null || (viewPositionOld.x==viewPositionNew.x && viewPositionOld.y==viewPositionNew.y))
        {
           	whereTo.x = 0;
           	whereTo.width = fullRowLength;
        	repaint(whereTo);
        }
	}
	private Dimension getCharDimensions()
	{
		if (charDim==null)
		{
			final FontMetrics fontMetrics = getFontMetrics(getFont());
			charDim = new Dimension(fontMetrics.charWidth('0'), fontMetrics.getHeight());
			patternElementLength = PATTERN_ELEMENT_CHARS * charDim.width;
			buttonLength = BUTTON_CHARS * charDim.width;

			final int half1 = charDim.width>>1;
			final int half2 = charDim.width - half1;

			//NOTE
			columnPositionsX[0] = 0;
			columnPositionsX[1] = charDim.width * 3;
			//Instrument
			columnPositionsX[2] = columnPositionsX[0] + columnPositionsX[1] + half1;
			columnPositionsX[3] = charDim.width<<1;
			//VolCommand
			columnPositionsX[4] = columnPositionsX[2] + columnPositionsX[3];
			columnPositionsX[5] = charDim.width;
			//VolCommandOp
			columnPositionsX[6] = columnPositionsX[4] + columnPositionsX[5];
			columnPositionsX[7] = charDim.width<<1;
			//Effect
			columnPositionsX[8] = columnPositionsX[6] + columnPositionsX[7] + half2;
			columnPositionsX[9] = charDim.width;
			//EffectOp
			columnPositionsX[10] = columnPositionsX[8] + columnPositionsX[9];
			columnPositionsX[11] = charDim.width<<1;
			// Border
			columnPositionsX[12] = columnPositionsX[10] + columnPositionsX[11] + half1 - 1;
			columnPositionsX[13] = 1;
		}
		return charDim;
	}
	private boolean resizeForPattern()
	{
		parentHeight = (parentContainer==null)?0:parentContainer.getHeight();
		parentWidth = (parentContainer==null)?0:parentContainer.getWidth();

		final Dimension charDim = getCharDimensions();

		final int displayableRows = (((parentHeight * 10) / charDim.height) + 5) / 10;
		rowsAbove = displayableRows>>1;
		rowsBelow = displayableRows - rowsAbove;

		currentChannels = (currentPattern==null)?0:currentPattern.getChannels();
		patternRowLength = currentChannels * patternElementLength;

		fullRowLength = (currentPattern==null)?0:patternRowLength + buttonLength;
		final int rows = (currentPattern==null)?0:currentPattern.getRowCount();
		int fullRowsHeight = (currentPattern==null)?parentHeight:(rows + rowsAbove + rowsBelow) * charDim.height; // two pixels insets at bottom...

		setSize(fullRowLength, fullRowsHeight);
		setPreferredSize(getSize());
		return true;
	}
	private void fillRectWithClipping(final Graphics2D g, final int x, final int y, final int width, final int height, final Rectangle clipping)
	{
		Rectangle fillMe = new Rectangle(x, y, width, height);
		if (clipping!=null) fillMe = clipping.intersection(fillMe);
		if (fillMe.width>0 || fillMe.height>0) g.fillRect(fillMe.x, fillMe.y, fillMe.width, fillMe.height);
	}
	private int drawPatternElement(final Graphics2D g, final int x, final int y, final PatternElement element, final int colorIndex, final Rectangle clipping, final Dimension charDim, final int markColumn)
	{
		final boolean intersects = (clipping!=null)?clipping.intersects(new Rectangle(x, y-charDim.height+1, patternElementLength, charDim.height)):true;
		if  (intersects) // if the element is not visible, do not draw. We however do not check that for all elements of the row...
		{
			if (markColumn!=PatternImagePosition.NOT_SET)
			{
				final int index = (markColumn-1)<<1;
				g.setColor(Color.BLACK);
				g.fillRect(x + columnPositionsX[index], y-getCharDimensions().height+1, columnPositionsX[index + 1], getCharDimensions().height);
			}

			// Note:
			if (markColumn==PatternImagePosition.COLUMN_NOTE)
				g.setColor(Color.WHITE);
			else
			{
				switch (element.getNoteIndex())
				{
					case ModConstants.NO_NOTE:
					case ModConstants.KEY_OFF:
					case ModConstants.NOTE_CUT:
					case ModConstants.NOTE_FADE: g.setColor(FOREGROUND[colorIndex]); break;
					default: g.setColor(NOTE[colorIndex]);
				}
			}
			g.drawString(ModConstants.getNoteNameForIndex(element.getNoteIndex()), x + columnPositionsX[0], y);

			// Instrument
			if (element.getInstrument()==0)
			{
				g.setColor((markColumn==PatternImagePosition.COLUMN_INSTRUMENT)?Color.WHITE:FOREGROUND[colorIndex]);
				g.drawString("..", x + columnPositionsX[2], y);
			}
			else
			{
				g.setColor((markColumn==PatternImagePosition.COLUMN_INSTRUMENT)?Color.WHITE:INSTRUMENT[colorIndex]);
				g.drawString(ModConstants.getAsHex(element.getInstrument(), 2), x + columnPositionsX[2], y);
			}

			// VolumeColumn
			if (element.getVolumeEffekt()==0)
			{
				g.setColor((markColumn==PatternImagePosition.COLUMN_VOLEFFECT_OP)?Color.WHITE:FOREGROUND[colorIndex]);
				g.drawString("..", x + columnPositionsX[6], y); // one empty
			}
			else
			{
				final int volEffectType = element.getVolEffectCategory();
				g.setColor((markColumn==PatternImagePosition.COLUMN_VOLEFFECT)?Color.WHITE:EFFECT[volEffectType][colorIndex]);
				g.drawString(Character.toString(element.getVolumeColumEffektChar()), x + columnPositionsX[4], y);
				g.setColor((markColumn==PatternImagePosition.COLUMN_VOLEFFECT_OP)?Color.WHITE:EFFECT[volEffectType][colorIndex]);
				g.drawString(ModConstants.getAsHex(element.getVolumeEffektOp(), 2), x + columnPositionsX[6], y);
			}
			// EffektColumn
			if (element.getEffekt()==0 && element.getEffektOp()==0)
			{
				g.setColor((markColumn==PatternImagePosition.COLUMN_EFFECT)?Color.WHITE:FOREGROUND[colorIndex]);
				g.drawString(".", x + columnPositionsX[8], y);
				g.setColor((markColumn==PatternImagePosition.COLUMN_EFFECT_OP)?Color.WHITE:FOREGROUND[colorIndex]);
				g.drawString("..", x + columnPositionsX[10], y);
			}
			else
			{
				final int effectType = element.getEffectCategory();
				g.setColor((markColumn==PatternImagePosition.COLUMN_EFFECT)?Color.WHITE:EFFECT[effectType][colorIndex]);
				g.drawString(Character.toString(element.getEffektChar()), x + columnPositionsX[8], y);
				g.setColor((markColumn==PatternImagePosition.COLUMN_EFFECT_OP)?Color.WHITE:EFFECT[effectType][colorIndex]);
				g.drawString(ModConstants.getAsHex(element.getEffektOp(), 2), x + columnPositionsX[10], y);
			}
			g.setColor(PATTERNLINE[0][colorIndex]);
			g.drawLine(x + columnPositionsX[12], y-charDim.height, x + columnPositionsX[12], y);
			g.setColor(PATTERNLINE[1][colorIndex]);
			g.drawLine(x + columnPositionsX[12] + columnPositionsX[13], y-charDim.height, x + columnPositionsX[12] + columnPositionsX[13], y);
			// We do drawLines. But maybe the others are nice, too?
//			g.fillRect  (x+half1-2, y-charDim.height, 3, charDim.height);
//			g.fill3DRect(x+half1-2, y-charDim.height, 3, charDim.height, true);
//			g.drawString("|", x, y);
		}

		return x + patternElementLength;
	}
	private int drawPatternRow(final Graphics2D g, int x, int y, final PatternRow row, final int colorIndex, final int rowNumber, final Rectangle clipping, final Dimension charDim)
	{
		final boolean intersects = (clipping!=null)?clipping.intersects(new Rectangle(x, y, patternRowLength, charDim.height)):true;
		if  (intersects) // if the row is not visible, do not draw
		{
			final PatternImagePosition position = getCurrentEditingRow();
			int markColumn = PatternImagePosition.NOT_SET;
			int markChannel = PatternImagePosition.NOT_SET;
			if (colorIndex==0 && position!=null && position.pattern==currentPattern && position.row==rowNumber)
			{
				g.setColor(selectionColor);
				markColumn = position.column;
				markChannel = position.channel;
			}
			else
			if (colorIndex==0 && currentPlayingRow!=null && currentPlayingRow.pattern==currentPattern && currentPlayingRow.row==rowNumber)
			{
				g.setColor(playSelectionColor);
			}
			else
			{
				final int checkRow = rowNumber+1;
				final Pattern parentPattern = row.getParentPattern();
				final int rowsPerMeasure = parentPattern.getRowsPerBeat();
				final int rowsPerBeat = parentPattern.getRowsPerBeat();
				g.setColor((checkRow%rowsPerMeasure)==0?HIGHLIGHT_LINE_1[colorIndex]:(checkRow%rowsPerBeat)==0?HIGHLIGHT_LINE_2[colorIndex]:BACKGROUND[colorIndex]);
			}

			fillRectWithClipping(g, x, y, patternRowLength, charDim.height, clipping);
			// Row number text - drawing text means y is baseline (not top line)
			y += charDim.height - 1;

			for (int channel=0; channel<currentChannels; channel++)
			{
				x = drawPatternElement(g, x, y, row.getPatternElement(channel), colorIndex, clipping, charDim, (markChannel==channel)?markColumn:PatternImagePosition.NOT_SET);
			}
		}
		else
			x += patternRowLength;
		return x;
	}
	private int drawButton(final Graphics2D g, final int x, final int y, final int colorIndex, final int rowNumber, final Rectangle clipping, final Dimension charDim)
	{
		final boolean intersects = (clipping!=null)?clipping.intersects(new Rectangle(x, y, buttonLength, charDim.height)):true;
		if  (intersects) // do not draw buttons that are not visible anyways
		{
			g.setColor(BUTTONS[colorIndex]);
			g.fill3DRect(x, y, x + buttonLength - 1, y + charDim.height, true);
			g.setColor(FOREGROUND[colorIndex]);
			if (rowNumber<0x100)
				g.drawString(ModConstants.getAsHex(rowNumber, 2), x + charDim.width, y + charDim.height - 1);
			else
				g.drawString(ModConstants.getAsHex(rowNumber, 3), x + (charDim.width>>1), y + charDim.height - 1);
		}
		return x + buttonLength;
	}
	private int drawPattern(final Graphics2D g, final int startX, final int startY, int startRow, int anzRows, final Pattern pattern, final boolean current, final Rectangle clipping, final Dimension charDim)
	{
		final int colorIndex = (current)?0:1;
		int y = startY;

		if (pattern==null)
		{
			final int fillRows = anzRows*charDim.height;
			g.setColor(BACKGROUND[colorIndex]);
			fillRectWithClipping(g, startX, startY, fullRowLength, fillRows, clipping);
			return y + fillRows;
		}
		else
		if (startRow<0) // fill on top rows till startRow is 0
		{
			final int fillRows = (-startRow)*charDim.height;
			startRow = 0;
			anzRows = pattern.getRowCount();
			g.setColor(BACKGROUND[colorIndex]);
			fillRectWithClipping(g, startX, startY, fullRowLength, fillRows, clipping);
			y += fillRows;
		}
		else // fill bottom, as pattern is not big enough
		if (startRow==0 && anzRows>pattern.getRowCount())
		{
			final int fillRows = (anzRows - pattern.getRowCount())*charDim.height;
			startRow = 0;
			anzRows = pattern.getRowCount();
			g.setColor(BACKGROUND[colorIndex]);
			fillRectWithClipping(g, startX, startY + (anzRows*charDim.height), fullRowLength, fillRows, clipping);
		}

		final int maxRow = startRow + anzRows;
		for (int rowNumber=startRow; rowNumber<maxRow; rowNumber++)
		{
			int x = startX;

			final boolean intersects = (clipping!=null)?clipping.intersects(new Rectangle(x, y, fullRowLength, charDim.height)):true;
			if (intersects) // avoid drawing a row that is clipped anyways
			{
				final PatternRow row = pattern.getPatternRow(rowNumber);

				x = drawButton(g, x, y, colorIndex, rowNumber, clipping, charDim);
				x = drawPatternRow(g, x, y, row, colorIndex, rowNumber, clipping, charDim);
			}

			y += charDim.height;
		}
		return y;
	}
	private void drawPatterns(final Graphics2D g)
	{
		final Rectangle clipping = g.getClipBounds();
		final Dimension charDim = getCharDimensions();
		int y = 0;
		y = drawPattern(g, 0, y, (prevPattern==null)?0:prevPattern.getRowCount() - rowsAbove, rowsAbove, prevPattern, false, clipping, charDim);
		y = drawPattern(g, 0, y, 0, (currentPattern==null)?0:currentPattern.getRowCount(), currentPattern, true, clipping, charDim);
		y = drawPattern(g, 0, y, 0, rowsBelow, nextPattern, false, clipping, charDim);
	}
	/**
	 * @param g
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	public void paintComponent(final Graphics g)
	{
		super.paintComponent(g);
		final Graphics2D gfx = (Graphics2D)g.create();
		try
		{
			drawPatterns(gfx);
		}
		finally
		{
			gfx.dispose();
		}
	}
}
