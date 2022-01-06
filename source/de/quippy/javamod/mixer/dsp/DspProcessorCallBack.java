/*
 * @(#) DspProcessorCallBack.java
 *
 * Created on 28.09.2007 by Daniel Becker
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
package de.quippy.javamod.mixer.dsp;

import java.util.EventListener;

/**
 * @author Daniel Becker
 * @since 28.09.2007
 */
public interface DspProcessorCallBack extends EventListener
{
	/** This method will communicate new samples 1.0<=x<=-1.0 for left and right channel */
	public void currentSampleChanged(float [] leftSample, float [] rightSample);
}
