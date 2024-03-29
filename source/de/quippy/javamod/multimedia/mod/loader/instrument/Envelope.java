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

import de.quippy.javamod.system.Helpers;

/**
 * @author Daniel Becker
 * @since 19.06.2006
 */
public class Envelope
{
	public enum EnvelopeType { volume, panning, pitch }
	
	public int [] positions;
	public int [] value;
	public int nPoints;
	public int sustainStartPoint;
	public int sustainEndPoint;
	public int loopStartPoint;
	public int loopEndPoint;
	public int endPoint;
	public EnvelopeType envelopeType;
	public boolean on, sustain, loop, carry, filter, xm_style;
	public byte[] oldITVolumeEnvelope;
	
	private static final int SHIFT = 16;
	private static final int MAXVALUE = 64<<SHIFT;
	private static final int BACKSHIFT = SHIFT - 3;

	/**
	 * Constructor for Envelope
	 */
	public Envelope(final EnvelopeType envType)
	{
		super();
		envelopeType=envType;
		on=sustain=loop=carry=filter=xm_style=false;
	}
	/**
	 * Get the new positions
	 * @since 19.06.2006
	 * @param position
	 * @param keyOff
	 * @return
	 */
	public int updatePosition(final int position, final boolean keyOff)
	{
		// we start at "-1" so first add one...
		int pos = position + 1;
		// difference between XM and IT is the way of comparison
		// >= (==) a point or > a point...
		if (xm_style) 
		{
			if (sustain && !keyOff && pos>=positions[sustainEndPoint]) pos = positions[sustainStartPoint]; 
			else 
			if (loop)
			{
				final boolean escapeLoop = loopEndPoint==sustainEndPoint && sustain && keyOff;
				if (pos>=positions[loopEndPoint] && !escapeLoop) pos = positions[loopStartPoint];
			}
		}
		else
		{
			if (sustain && !keyOff && pos>positions[sustainEndPoint]) pos = positions[sustainStartPoint]; 
			else
			if (loop && pos>positions[loopEndPoint]) pos = positions[loopStartPoint];
		}
		// End reached? We do reset behind positions[endpoint] to avoid overflow
		if (pos>positions[endPoint]) pos = positions[endPoint]+1;
		
		return pos;
	}
//	public int updatePosition(final int position, final boolean keyOff)
//	{
//		if (positions==null || positions.length==0 || envelopeFinished(position)) return position;
//		
//		if (xm_style)
//		{
//			int pos = position + 1;
//			if (loop)
//			{
//				final boolean escapeLoop = loopEndPoint==sustainEndPoint && sustain && keyOff;
//				if (pos>positions[loopEndPoint] && !escapeLoop) pos = positions[loopStartPoint];
//			}
//			else
//			if (sustain && !keyOff)
//			{
//				 if (pos>positions[sustainEndPoint]) pos = positions[sustainStartPoint];
//			}
//			else
//			if (pos>=positions[endPoint]) pos = positions[endPoint]+1;
//			
//			return pos;
//		}
//		else
//		{
//			int pos = position;
//			int start, end;
//			
//			if (sustain && !keyOff)
//			{
//				start = positions[sustainStartPoint];
//				end = positions[sustainEndPoint] + 1;
//			}
//			else
//			if (loop)
//			{
//				start = positions[loopStartPoint];
//				end = positions[loopEndPoint] + 1;
//			}
//			else
//			{
//				start = end = positions[endPoint];
//				if (pos>end)  return positions[endPoint]+1;
//			}
//			if (position>=end) pos = start;
//			return ++pos;
//		}
//	
//	}
	/**
	 * return true, if the positions is beyond end point
	 * @since 12.06.2020
	 * @param position
	 * @return
	 */
	public boolean envelopeFinished(final int position)
	{
		return position>positions[endPoint];
	}
	/**
	 * @since 15.03.2024
	 * @param position
	 * @return
	 */
	public boolean envelopeFinishedInSilence(final int position)
	{
		return envelopeFinished(position) && value[endPoint]==0;
	}
	/**
	 * get the value at the positions
	 * Returns values between 0 and 512
	 * @since 19.06.2006
	 * @param position
	 * @return
	 */
	public int getValueForPosition(final int position)
	{
		int index = endPoint;
		for (int i=0; i<index; i++)
			if (positions[i]>position) index = i; // results in a break

		int x2 = positions[index];
		int y1 = 0;

		// if we land directly on an envelope point, do nothing
		if (position>=x2) 
			y1 = value[index]<<SHIFT;
		else
		{
			// if we are somewhere in between two points, do a linear interpolation
			int x1 = 0;
			
			// get previous point, if any
			if (index>0)
			{
				y1 = value[index - 1]<<SHIFT; 
				x1 = positions[index - 1];
			}

			if(x2>x1 && position>x1)
			{
				int y2 = value[index]<<SHIFT;
				y1 += ((y2 - y1) * (position - x1)) / (x2 - x1);
			}
		}

		// Limit, just in case
		if (y1<0) y1=0;
		else if (y1>MAXVALUE) y1=MAXVALUE;

		return y1>>BACKSHIFT;
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
	 * Let's do some range checks. Values are limited to not exceed maxValue,
	 * plus array sizes are considered and MSB bug (XM)
	 * Needs to be called by the loaders, when envelope set is finished
	 * @since 22.01.2022
	 * @param maxValue the maximum value
	 */
	public void sanitize(final int maxValue)
	{
		if (positions!=null && positions.length>0)
		{
			// limit endPoint to the smallest possible array index
			// and consider arrays of different length
			setNPoints((nPoints>positions.length)?positions.length:(nPoints>value.length)?value.length:nPoints);

			// sanitize the values and positions
			positions[0]=0;
			value[0] = Helpers.limitMax(value[0], maxValue);
			for (int pos=1; pos<=endPoint; pos++)
			{
				// libmikmod code says: "Some broken XM editing program will only save the low byte of the position
				// value. Try to compensate by adding the missing high byte."
				// So, if position is smaller than prior position and no MSB is set:
				if (positions[pos]<positions[pos-1] && (positions[pos]&0xFF00)==0)
				{
					positions[pos]|=(positions[pos-1]&0xFF00); // add possible high byte of prior position
					if (positions[pos]<positions[pos-1]) positions[pos]|=0x0100; // still smaller? Force MSB set (OMPT does "+=" - which seems wrong)
				}
				positions[pos] = Math.max(positions[pos], positions[pos - 1]);
				value[pos] = Helpers.limitMax(value[pos], maxValue);
			}

			// limit loop and sustain loop positions to the maximum
			loopEndPoint = Helpers.limitMax(loopEndPoint, endPoint);
			loopStartPoint = Helpers.limitMax(loopStartPoint, loopEndPoint);
			sustainEndPoint = Helpers.limitMax(sustainEndPoint, endPoint);
			sustainStartPoint = Helpers.limitMax(sustainStartPoint, sustainEndPoint);
		}
		else
		{
			endPoint = -1;
			on=sustain=loop=carry=filter=xm_style=false;
		}
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
		endPoint = (nPoints = points) - 1;
	}
	/**
	 * @param positions The positions to set.
	 */
	public void setPositions(final int[] positions)
	{
		this.positions = positions;
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
	/**
	 * @param envelopeType the envelopeType to set
	 */
	public void setEnvelopeType(final EnvelopeType envelopeType)
	{
		this.envelopeType = envelopeType;
	}
	/**
	 * @return the envelopeType
	 */
	public EnvelopeType getEnvelopeType()
	{
		return envelopeType;
	}
	/**
	 * This is probably a pre-calculation of the volume envelope
	 * however, we read it, but do not use it (yet...)
	 * neither Schism nor ModPlug use it, do life calculations instead
	 * we are in good company :)
	 * @param oldITVolumeEnvelope the oldITVolumeEnvelope to set
	 */
	public void setOldITVolumeEnvelope(final byte[] oldITVolumeEnvelope)
	{
		this.oldITVolumeEnvelope = oldITVolumeEnvelope;
	}
}
