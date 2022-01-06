/*
 * @(#) TableGenerator.java
 *
 * Created on 07.06.2020 by Daniel Becker
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
package de.quippy.javamod.test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import de.quippy.javamod.multimedia.mod.mixer.BasicModMixer;

/**
 * @author Daniel Becker
 * @since 07.06.2020
 */
public class TableGenerator
{
	/**
	 * Constructor for TableGenerator
	 */
	public TableGenerator()
	{
		super();
	}
	
	private static String [] skippingFields = new String [] {
	    "currentAssignedNotePeriod", "currentAssignedNoteIndex", "currentEffekt", "currentEffektParam", "currentVolumeEffekt", "currentVolumeEffektOp", "currentAssignedInstrumentIndex",
	    "currentAssignedInstrument",
	    "isNNA",
	    "currentElement",
		"assignedNotePeriod", "assignedNoteIndex",
		"effekt", "effektParam", "volumeEffekt", "volumeEffektOp",
 		"glissando",
		"arpegioIndex", "arpegioNote", "arpegioParam",
		"portaStepUp", "portaStepUpEnd", "portaStepDown", "portaStepDownEnd",
		"finePortaUp", "finePortaDown", "finePortaUpEx", "finePortaDownEx", 
		"portaNoteStep", "portaTargetNotePeriod", 
		"volumSlideValue", "globalVolumSlideValue", 
		"panningSlideValue",
		"vibratoTablePos", "vibratoStep", "vibratoAmplitude", "vibratoType",
		"vibratoOn", "vibratoNoRetrig",
		"tremoloTablePos", "tremoloStep", "tremoloAmplitude", "tremoloType",
		"tremoloOn", "tremoloNoRetrig",
		"panbrelloTablePos", "panbrelloStep", "panbrelloAmplitude", "panbrelloType",
		"panbrelloOn", "panbrelloNoRetrig",
		"tremorOntime", "tremorOfftime", "tremorOntimeSet", "tremorOfftimeSet",
		"tremorWasActive",
		"retrigCount", "retrigMemo", "retrigVolSlide",
		"sampleOffset", "highSampleOffset",
		"jumpLoopPatternRow", "jumpLoopRepeatCount", "lastJumpCounterRow", "jumpLoopPositionSet",
		"noteDelayCount", "noteCutCount",
		"S_Effect_Memory", "IT_EFG"
	};
	
	private static void generateNNAChannelCopy()
	{
		try
		{
			Arrays.sort(skippingFields);
			System.out.println("\t\tprotected void setUpFrom(ChannelMemory fromMe)\n\t\t{");
			Field [] fields = BasicModMixer.ChannelMemory.class.getDeclaredFields();
			for (Field f : fields)
			{
				if (Modifier.isFinal(f.getModifiers())) continue;
				final boolean isSkippingField = Arrays.binarySearch(skippingFields, f.getName()) > -1; 
				
				if (isSkippingField) System.out.print("//");

				if (f.getType().isArray())
					System.out.print("\t\t\tfor (int i=0; i<" + f.getName()+ ".length; i++) " + f.getName()+ "[i] = fromMe." + f.getName()+ "[i];");
				else
					System.out.print("\t\t\t" + f.getName()+ " = fromMe." + f.getName()+";");
				
				System.out.print((isSkippingField)?" // Effect memory - not for NNA\n":"\n");
			}
			System.out.println("\t\t}\n\n");
		}
		catch (Exception ex)
		{
			throw new RuntimeException(ex);
		}
	}
//	private static void checkEnvelopes()
//	{
//		int [] values = new int [] {32, 32, 38, 38, 42, 42, 46, 46, 56, 56, 46, 46, 38, 38, 42, 42, 52, 52, 50, 50, 42, 42, 46};
//		Envelope env = new Envelope();
//		env.setValue(values);
//		env.setNPoints(values.length);
//		env.setPositions(new int [] {0, 2, 3, 14, 15, 26, 27, 98, 99, 123, 124, 195, 196, 207, 208, 219, 220, 267, 268, 315, 316, 339, 340});
//		for (int i=0; i<341; i++)
//		{
//			System.out.print(env.getValueForPosition(i, 256)+", ");
//			if (env.envelopeFinished(i))
//			{
//				System.out.println("point is "+i);
//				break;
//			}
//		}
//	}
//	private static void generateHalfToneTable()
//	{
//		System.out.print("\tpublic static final int [] halfToneTab = new int[]\n\t{\n\t\t");
//		for (int i=0; i<16; i++)
//		{
//			double factor = 1.0d / Math.pow(2D, (double)i/12D);
//			long longFactor = (long)((factor*(double)Helpers.HALFTONE_FAC) + 0.5D);
//			System.out.print(longFactor);
//			if (i<15) System.out.print(", ");
//		}
//		System.out.print("\n\t};\n");
//	}
//	private static void verifyHalfToneTable()
//	{
//		for (int i=0; i<16; i++)
//		{
//			double factor = Math.pow(2D, (double)i/12D);
//			int intFactor = (int)(((1.0D/factor)*(double)Helpers.HALFTONE_FAC) + 0.5D);
//			
//			int checkPeriod = Helpers.logtab[0];
//			int exactPeriod = (int)((((double)(checkPeriod))/factor)+0.5D);
//			//int intPeriod = (int)((((long)checkPeriod)<<Helpers.HALFTONE_SHIFT) / intFactor);
//			int intPeriod = (int)((((long)checkPeriod) * intFactor)>>Helpers.HALFTONE_SHIFT);
//			
//			System.out.println(exactPeriod + "<->" + intPeriod +"\t"+intFactor);
//		}
//	}

	/**
	 * @since 07.06.2020
	 * @param args
	 */
	public static void main(String[] args)
	{
		generateNNAChannelCopy();
	}
}
