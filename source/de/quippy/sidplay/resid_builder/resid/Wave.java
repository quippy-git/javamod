/*
 * @(#) Wave.java
 *
 * Created on 09.08.2012 by Daniel Becker
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
package de.quippy.sidplay.resid_builder.resid;

import java.io.BufferedInputStream;
import java.io.IOException;

/**
 * @author Daniel Becker
 *
 */
public class Wave
{
	private static final String RESSOURCE_PATH_6581 = "/de/quippy/sidplay/resid_builder/resid/WAVE6581";
	private static final String RESSOURCE_PATH_8580 = "/de/quippy/sidplay/resid_builder/resid/WAVE8580";

	public static final int wave6581__ST[] = new int[4096];
	public static final int wave6581_P_T[] = new int[4096];
	public static final int wave6581_PS_[] = new int[4096];
	public static final int wave6581_PST[] = new int[4096];
	
	public static final int wave8580__ST[] = new int[4096];
	public static final int wave8580_P_T[] = new int[4096];
	public static final int wave8580_PS_[] = new int[4096];
	public static final int wave8580_PST[] = new int[4096];

	static
	{
		// SAVE:
//		writeArrayToStream(Helpers.HOMEDIR + "/WAVE6581__ST", IWave6581.wave6581__ST);
//		writeArrayToStream(Helpers.HOMEDIR + "/WAVE6581_P_T", IWave6581.wave6581_P_T);
//		writeArrayToStream(Helpers.HOMEDIR + "/WAVE6581_PS_", IWave6581.wave6581_PS_);
//		writeArrayToStream(Helpers.HOMEDIR + "/WAVE6581_PST", IWave6581.wave6581_PST);
//		writeArrayToStream(Helpers.HOMEDIR + "/WAVE8580__ST", IWave8580.wave8580__ST);
//		writeArrayToStream(Helpers.HOMEDIR + "/WAVE8580_P_T", IWave8580.wave8580_P_T);
//		writeArrayToStream(Helpers.HOMEDIR + "/WAVE8580_PS_", IWave8580.wave8580_PS_);
//		writeArrayToStream(Helpers.HOMEDIR + "/WAVE8580_PST", IWave8580.wave8580_PST);
		
		//LOAD:
		readArrayFromStream(RESSOURCE_PATH_6581 + "__ST", wave6581__ST);
		readArrayFromStream(RESSOURCE_PATH_6581 + "_P_T", wave6581__ST);
		readArrayFromStream(RESSOURCE_PATH_6581 + "_PS_", wave6581__ST);
		readArrayFromStream(RESSOURCE_PATH_6581 + "_PST", wave6581__ST);
		readArrayFromStream(RESSOURCE_PATH_8580 + "__ST", wave8580__ST);
		readArrayFromStream(RESSOURCE_PATH_8580 + "_P_T", wave8580__ST);
		readArrayFromStream(RESSOURCE_PATH_8580 + "_PS_", wave8580__ST);
		readArrayFromStream(RESSOURCE_PATH_8580 + "_PST", wave8580__ST);
	}
	
//	private static void writeArrayToStream(final String ressourcePath, final int [] array)
//	{
//		File f = new File(ressourcePath);
//		BufferedOutputStream oStream = null;
//		try
//		{
//			if (f.exists()) f.delete();
//			oStream = new BufferedOutputStream(new FileOutputStream(f));
//			for (int i=0; i<array.length; i++)
//			{
//				oStream.write(array[i]&0xFF);
//			}
//		}
//		catch (Throwable ex)
//		{
//			ex.printStackTrace(System.err);
//		}
//		finally
//		{
//			if (oStream!=null) try { oStream.close(); } catch (IOException e) { /*NOOP*/ }
//		}
//	}
	private static void readArrayFromStream(final String ressourcePath, final int [] array)
	{
		BufferedInputStream iStream = null;
		try
		{
			iStream = new BufferedInputStream(Wave.class.getResourceAsStream(ressourcePath));
			final byte [] buf = new byte[array.length];
			int len;
			int writePos = 0;
			while ((len = iStream.read(buf))>0)
			{
				for (int i=0; i<len; i++)
				{
					array[writePos + i] = (int)(buf[i])&0xFF;
				}
				writePos += len;
			}
		}
		catch (Throwable ex)
		{
			ex.printStackTrace(System.err);
		}
		finally
		{
			if (iStream!=null) try { iStream.close(); } catch (IOException e) { /*NOOP*/ }
		}
	}
	
	/**
	 * 
	 * @since 09.08.2012
	 */
	private Wave()
	{
		// Singleton, only static
	}
}
