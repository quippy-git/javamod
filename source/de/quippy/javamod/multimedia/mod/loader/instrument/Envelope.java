/*
 * @(#) Envelope.java
 * 
 * Created on 19.06.2006 by Daniel Becker
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
package de.quippy.javamod.multimedia.mod.loader.instrument;

/**
 * @author Daniel Becker
 * @since 19.06.2006
 */
public class Envelope
{
	public int [] positions;
	public int [] value;
	public int nPoints;
	public int sustainStartPoint;
	public int sustainEndPoint;
	public int loopStartPoint;
	public int loopEndPoint;
	public int endPoint;
	public boolean on, sustain, loop, carry, filter, xm_style;
	
	private static final int SHIFT = 3;
	private static final int MAXVALUE = 64<<SHIFT;

	/**
	 * Constructor for Envelope
	 */
	public Envelope()
	{
		super();
		on=sustain=loop=carry=filter=xm_style=false;
	}
	/**
	 * Get the new positions
	 * @since 19.06.2006
	 * @param position
	 * @param keyOff
	 * @return
	 */
	public int updatePosition(int position, final boolean keyOff)
	{
		// we start at "-1" so first add one...
		position++;
		// difference between xm and it is only the way of comparison
		// >= (==) a point or > a point...
		if (xm_style) 
		{
			if (sustain && !keyOff && position>=positions[sustainEndPoint]) position = positions[sustainStartPoint]; 
			else 
			if (loop && position>=positions[loopEndPoint]) position = positions[loopStartPoint];
		}
		else
		{
			if (sustain && !keyOff && position>positions[sustainEndPoint]) position = positions[sustainStartPoint]; 
			else
			if (loop && position>positions[loopEndPoint]) position = positions[loopStartPoint];
		}
		// End reached? We do not reset anymore to find NNAs beeing far beyond endpoint
		//if (position>positions[endPoint]) position = positions[endPoint];
		
		return position;
	}
	/**
	 * return true, if the positions is beyond end point
	 * @since 12.06.2020
	 * @param p
	 * @return
	 */
	public boolean envelopeFinished(final int position, final boolean keyOff)
	{
		return ((sustain || keyOff) && !loop && position>=positions[endPoint]);
	}
	/**
	 * get the value at the positions
	 * Returns values between 0 and 512
	 * @since 19.06.2006
	 * @param p
	 * @param defautlValue the defaultValue (middle-Position 256 with a range 0..512)
	 * @return
	 */
	public int getValueForPosition(int p, final int defautlValue)
	{
		int pt = endPoint;
		for (int i=0; i<pt; i++)
		{
			if (p <= positions[i]) 
			{ 
				pt = i; 
				break; 
			}
		}
		int x2 = positions[pt];
		int x1, v;
		if (p>=x2)
		{
			v = value[pt]<<SHIFT;
			x1 = x2;
		}
		else
		if (pt>0)
		{
			v = value[pt-1]<<SHIFT;
			x1 = positions[pt-1];
		}
		else
		{
			x1 = 0; v=defautlValue;
		}
		
		if (p>x2) p=x2;
		if ((x2>x1) && (p>x1))
		{
			v += ((p - x1) * ((value[pt]<<SHIFT) - v)) / (x2 - x1);
		}
		if (v<0) v=0;
		else
		if (v>MAXVALUE) v = MAXVALUE;
		
		return v;
	}
	/**
	 * Sets the boolean values corresponding to the flag value
	 * XM-Version
	 * @since 19.06.2006
	 * @param flag
	 */
	public void setXMType(final int flag)
	{
		on = (flag&0x01)!=0;
		sustain = (flag&0x02)!=0;
		loop = (flag&0x04)!=0;
		carry = filter = false;
		xm_style = true;
	}
	/**
	 * Sets the boolean values corresponding to the flag value
	 * IT-Version (why on earth needed this to be swaped?!)
	 * @since 12.11.2006
	 * @param flag
	 */
	public void setITType(final int flag)
	{
		on = (flag&0x01)!=0;
		loop = (flag&0x02)!=0;
		sustain = (flag&0x04)!=0;
		carry = (flag&0x08)!=0;
		filter = (flag&0x80)!=0;
		xm_style = false;
	}
	/**
	 * @param loopEndPoint The loopEndPoint to set.
	 */
	public void setLoopEndPoint(final int loopEndPoint)
	{
		this.loopEndPoint = loopEndPoint;
	}
	/**
	 * @param loopStartPoint The loopStartPoint to set.
	 */
	public void setLoopStartPoint(final int loopStartPoint)
	{
		this.loopStartPoint = loopStartPoint;
	}
	/**
	 * @param points The nPoints to set.
	 */
	public void setNPoints(final int points)
	{
		nPoints = points;
		endPoint = nPoints - 1;
	}
	/**
	 * @param positions The positions to set.
	 */
	public void setPositions(final int[] positions)
	{
		this.positions = positions;
		// libmikmod code says: "Some broken XM editing program will only save the low byte of the position
		// value. Try to compensate by adding the missing high byte."
		for (int i=1; i<positions.length; i++)
		{
			if (positions[i]<positions[i-1] && (positions[i]&0xFF00)==0)
			{
				positions[i]|=(positions[i-1]&0xFF00); // add possible high byte of prior position
				if (positions[i] < positions[i-1]) positions[i] |=0x0100; // still smaller? Force MSB set
			}
		}
	}
	/**
	 * @param value The value to set.
	 */
	public void setValue(final int[] value)
	{
		this.value = value;
	}
	/**
	 * @param sustainPoint The sustainPoint to set. (XM-Version)
	 */
	public void setSustainPoint(final int sustainPoint)
	{
		this.sustainStartPoint = this.sustainEndPoint = sustainPoint;
	}
	/**
	 * @param sustainEndPoint the sustainEndPoint to set (IT-Version)
	 */
	public void setSustainEndPoint(final int sustainEndPoint)
	{
		this.sustainEndPoint = sustainEndPoint;
	}
	/**
	 * @param sustainStartPoint the sustainStartPoint to set (IT-Version)
	 */
	public void setSustainStartPoint(final int sustainStartPoint)
	{
		this.sustainStartPoint = sustainStartPoint;
	}
}
