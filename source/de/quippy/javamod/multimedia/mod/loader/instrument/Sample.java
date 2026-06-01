/*
 * @(#) Sample.java
 *
 * Created on 21.04.2006 by Daniel Becker
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

import de.quippy.javamod.multimedia.mod.ModConstants;
import de.quippy.javamod.multimedia.mod.SampleFrame;
import de.quippy.javamod.multimedia.mod.mixer.interpolation.CubicSpline;
import de.quippy.javamod.multimedia.mod.mixer.interpolation.Kaiser;
import de.quippy.javamod.multimedia.mod.mixer.interpolation.WindowedFIR;
import de.quippy.javamod.system.Helpers;

/**
 * Used to store the Instruments
 * @author Daniel Becker
 * @since 21.04.2006
 */
public class Sample
{
	public String name;			// Name of the sample
	public int byteLength;		// not always equal to sampleLength (sampleLength is changed during loading)
	public int sampleLength;	// full length in samples (already *2 --> Mod-Format)
	public int sampleType;		// normalized loading flags (signed, unsigned, 8-Bit, compressed, ...)
	public int fineTune;		// Finetuning -8..+8
	public int volume;			// Basisvolume
	public int loopStart;		// # of the loop start (already *2 --> Mod-Fomat)
	public int loopStop;		// # of the loop end   (already *2 --> Mod-Fomat)
	public int loopLength;		// length of the loop
	public int loopType;		// 0: no Looping, 1: normal, 2: Sustain, 4: pingpong 8: Sustain pingpong
	public int transpose;		// PatternNote + transpose
	public int baseFrequency;	// BaseFrequency
	public boolean isStereo;	// true, if this is a stereo-sample

	//S3M:
	public int type;			// always 1 for a sample, 1-7 AdLib (2:Melody 3:Basedrum 4:Snare 5:Tom 6:Cym 7:HiHat)
	public String dosFileName;	// DOS File-Name
	public int flags;			// flag: 1:Looping sample 2:Stereo 4:16Bit-Sample...

	// XM
	public boolean setPanning;	// set the panning
	public int defaultPanning;	// default Panning
	public int vibratoType;		// Vibrato Type
	public int vibratoSweep;	// Vibrato Sweep
	public int vibratoDepth;	// Vibrato Depth
	public int vibratoRate;		// Vibrato Rate
	public int XM_reserved;		// reserved, but some magic with 0xAD and SM_ADPCM4...

	// IT
	public int sustainLoopStart;// SustainLoopStart
	public int sustainLoopStop; // SustainLoopEnd
	public int sustainLoopLength; // SustainLoop Length
	public int flag_CvT;		// Flag for Instrument Save
	public int globalVolume;	// GlobalVolume

	// Interploation Magic
	private int interpolationStopLoop;
	private int interpolationStopSustain;
	private int interpolationStartLoop;
	private int interpolationStartSustain;

	// If this is adlib...
	public byte[] adLib_Instrument;

	// MPT specific cue points
	private int[] cues;
	public static final int MAX_CUES = 9;

	public static final int INTERPOLATION_LOOK_AHEAD = 16;

	// The sample data, already converted to signed 32 bit (always)
	// 8Bit: 0..127,128-255; 16Bit: -32768..0..+32767
	public long[] sampleL;
	public long[] sampleR;

	/**
	 * Constructor for Sample
	 */
	public Sample()
	{
		super();
		isStereo = false;
	}
	/**
	 * Allocate the sample data inclusive interpolation look ahead buffers
	 * @since 03.07.2020
	 */
	public void allocSampleData()
	{
		final int alloc = sampleLength + ((1 + 1 + 4 + 4 + 4 + 4) * INTERPOLATION_LOOK_AHEAD);
		sampleL = new long[alloc];
		if (isStereo) sampleR = new long[alloc]; else sampleR = null;
	}
	/**
	 * Fits the loop-data given in instruments loaded
	 * These values are often not correct
	 * Furthermore we add sample data for interpolation
	 * @since 27.08.2006
	 * @param modType
	 */
	public void fixSampleLoops(final int modType)
	{
		if (sampleL==null || sampleLength==0)
		{
			loopType = loopLength = loopStop = loopStart =
			sustainLoopLength = sustainLoopStart = sustainLoopStop = 0;
			return;
		}
		// A sample point index greater than the array index
		// needs to be allowed (! >=)
		if (loopStop>sampleLength) loopStop = sampleLength;
		if (loopStart<0) loopStart = 0;
		loopLength = loopStop - loopStart;

		if (sustainLoopStop>sampleLength) sustainLoopStop = sampleLength;
		if (sustainLoopStart<0) sustainLoopStart = 0;
		sustainLoopLength = sustainLoopStop - sustainLoopStart;

		// Kill invalid loops
		// with protracker, a loopsize of 2 is considered invalid
		if (((modType&ModConstants.MODTYPE_MOD)!=0 && loopStart+2>loopStop) ||
				loopStart>loopStop || loopLength<=0)
		{
			loopStart = loopStop = loopLength = 0;
			loopType &= ~ModConstants.LOOP_ON;
		}
		if (sustainLoopStart>sustainLoopStop || sustainLoopLength<=0)
		{
			sustainLoopStart = sustainLoopStop = sustainLoopLength = 0;
			loopType &= ~ModConstants.LOOP_SUSTAIN_ON;
		}

		addInterpolationLookAheadData();
	}
	/**
	 * We copy now for a loop - for short Loops we need to simulate it
	 * @since 03.07.2020
	 * @param start
	 * @param length
	 * @param isPingPong
	 */
	private void addInterpolationLookAheadDataLoop(final int startIndex, final int length, final int sourceIndex, final boolean isForward, final boolean isPingPong, final boolean forLoopEnd)
	{
		final int numSamples = 2 * INTERPOLATION_LOOK_AHEAD + ((isForward&&forLoopEnd) || (!isForward&&!forLoopEnd)?1:0);
		int destIndex = startIndex + (2 * INTERPOLATION_LOOK_AHEAD) - (forLoopEnd?1:0);
		int readPosition = (forLoopEnd)? length-1 : 0;
		final int writeIncrement = isForward?1:-1;
		int readIncrement = writeIncrement;

		for (int i=0; i<numSamples; i++)
		{
			sampleL[destIndex] = sampleL[sourceIndex + readPosition];
			if (sampleR!=null) sampleR[destIndex] = sampleR[sourceIndex + readPosition];
			destIndex += writeIncrement;

			if (readPosition==length-1 && readIncrement>0)
			{
				if (isPingPong)
				{
					readIncrement = -1;
				}
				else
					readPosition = 0;
			}
			else
			if (readPosition==0 && readIncrement<0)
			{
				if (isPingPong)
				{
					readIncrement = 1;
				}
				else
					readPosition = length-1;
			}
			else
				readPosition += readIncrement;
		}
	}
	/**
	 * @since 03.07.2020
	 */
	private void addInterpolationLookAheadData()
	{
		// At the end, we want to have
		// [PRE | sample data | POST | 4x endLoop | 4x endSustain]

		final int startSampleData = INTERPOLATION_LOOK_AHEAD;
		final int afterSampleData = startSampleData + sampleLength;
		interpolationStopLoop = afterSampleData + INTERPOLATION_LOOK_AHEAD;
		interpolationStopSustain = interpolationStopLoop + (4 * INTERPOLATION_LOOK_AHEAD);
		interpolationStartLoop = interpolationStopSustain + (4 * INTERPOLATION_LOOK_AHEAD);
		interpolationStartSustain = interpolationStartLoop + (4 * INTERPOLATION_LOOK_AHEAD);

		// First move sampleData out of the way, as it is loaded at index 0
		for (int pos=sampleLength-1; pos>=0; pos--)
		{
			sampleL[startSampleData+pos] = sampleL[pos];
			if (sampleR!=null) sampleR[startSampleData + pos] = sampleR[pos];
		}

		// now add sample data in PRE and POST
		for (int pos=0; pos<INTERPOLATION_LOOK_AHEAD; pos++)
		{
			sampleL[afterSampleData + pos] = sampleL[afterSampleData - 1];
			if (sampleR!=null) sampleR[afterSampleData + pos] = sampleR[afterSampleData - 1];
			sampleL[pos] = sampleL[startSampleData];
			if (sampleR!=null) sampleR[pos] = sampleR[startSampleData];

		}

		if ((loopType & ModConstants.LOOP_ON)!=0)
		{
			addInterpolationLookAheadDataLoop(interpolationStopLoop,  loopLength, loopStart + INTERPOLATION_LOOK_AHEAD, true,  (loopType&ModConstants.LOOP_IS_PINGPONG)!=0, true);
			addInterpolationLookAheadDataLoop(interpolationStopLoop,  loopLength, loopStart + INTERPOLATION_LOOK_AHEAD, false, (loopType&ModConstants.LOOP_IS_PINGPONG)!=0, true);
			addInterpolationLookAheadDataLoop(interpolationStartLoop, loopLength, loopStart + INTERPOLATION_LOOK_AHEAD, true,  (loopType&ModConstants.LOOP_IS_PINGPONG)!=0, false);
			addInterpolationLookAheadDataLoop(interpolationStartLoop, loopLength, loopStart + INTERPOLATION_LOOK_AHEAD, false, (loopType&ModConstants.LOOP_IS_PINGPONG)!=0, false);
		}
		if ((loopType & ModConstants.LOOP_SUSTAIN_ON)!=0)
		{
			addInterpolationLookAheadDataLoop(interpolationStopSustain,  sustainLoopLength, sustainLoopStart + INTERPOLATION_LOOK_AHEAD, true,  (loopType&ModConstants.LOOP_IS_PINGPONG)!=0, true);
			addInterpolationLookAheadDataLoop(interpolationStopSustain,  sustainLoopLength, sustainLoopStart + INTERPOLATION_LOOK_AHEAD, false, (loopType&ModConstants.LOOP_IS_PINGPONG)!=0, true);
			addInterpolationLookAheadDataLoop(interpolationStartSustain, sustainLoopLength, sustainLoopStart + INTERPOLATION_LOOK_AHEAD, true,  (loopType&ModConstants.LOOP_IS_PINGPONG)!=0, false);
			addInterpolationLookAheadDataLoop(interpolationStartSustain, sustainLoopLength, sustainLoopStart + INTERPOLATION_LOOK_AHEAD, false, (loopType&ModConstants.LOOP_IS_PINGPONG)!=0, false);
		}
	}
	/**
	 * returns a new index into samples if currentSamplePos
	 * is too near loop end or loop start
	 * @since 03.07.2020
	 * @param currentSamplePos
	 * @return
	 */
	public int getSustainLoopMagic(final int currentSamplePos, final boolean inLoop)
	{
		if (currentSamplePos + INTERPOLATION_LOOK_AHEAD >= sustainLoopStop) // approaching sustainLoopStop?
			return interpolationStopSustain - sustainLoopStop + (2*INTERPOLATION_LOOK_AHEAD);
		else
		if (currentSamplePos - INTERPOLATION_LOOK_AHEAD <= sustainLoopStart && inLoop) // approaching/leaving sustainLoopStart?
			return interpolationStartSustain - sustainLoopStart + (2*INTERPOLATION_LOOK_AHEAD);
		else
			return 0;
	}
	/**
	 * returns a new index into samples if currentSamplePos
	 * is too near loop end or loop start
	 * @since 03.07.2020
	 * @param currentSamplePos
	 * @return
	 */
	public int getLoopMagic(final int currentSamplePos, final boolean inLoop)
	{
		if (currentSamplePos + INTERPOLATION_LOOK_AHEAD >= loopStop)  // approaching loopStop?
			return interpolationStopLoop - loopStop + (2*INTERPOLATION_LOOK_AHEAD);
		else
		if (currentSamplePos - INTERPOLATION_LOOK_AHEAD <= loopStart && inLoop) // approaching/leaving LoopStart?
			return interpolationStartLoop - loopStart + (2*INTERPOLATION_LOOK_AHEAD);
		else
			return 0;
	}
	/**
	 * @since 12.03.2024
	 * @return true, if this sample as any sample data. That is, if at least
	 * the left buffer (mono sample) is not null and has a length>0
	 */
	public boolean hasSampleData()
	{
		return (sampleL!=null && sampleL.length>0);
	}
	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		final StringBuilder bf = new StringBuilder((name==null)?Helpers.EMPTY_STING:name);
		bf.append('(').
			append(getSampleTypeString()).append(", ").
			append("fineTune:").append(fineTune).append(", ").
			append("transpose:").append(transpose).append(", ").
			append("baseFrequency:").append(baseFrequency).append(", ").
			append("volume:").append(volume).append(", ").
			append("set panning:").append(setPanning).append(", ").
			append("panning:").append(defaultPanning).append(", ").
			append("loopStart:").append(loopStart).append(", ").
			append("loopStop:").append(loopStop).append(", ").
			append("loopLength:").append(loopLength).append(", ").
			append("SustainloopStart:").append(sustainLoopStart).append(", ").
			append("SustainloopStop:").append(sustainLoopStop).append(", ").
			append("SustainloopLength:").append(sustainLoopLength).append(')');

		return bf.toString();
	}
	public String toShortString()
	{
		return this.name;
	}
	/**
	 * @since 31.07.2020
	 * @return a String representing of the loading factors
	 */
	public String getSampleTypeString()
	{
		if (adLib_Instrument!=null) return "OPL Instrument";

		final StringBuilder bf = new StringBuilder();
		bf.append((sampleType&ModConstants.SM_16BIT)!=0		? "16-Bit" : "8-Bit").append(", ");
		bf.append((sampleType&ModConstants.SM_BigEndian)!=0	? "big" : "little").append(" endian, ");
		bf.append((sampleType&ModConstants.SM_PCMU)!=0		? "unsigned" : "signed").append(", ");
		bf.append((sampleType&ModConstants.SM_PCMD)!=0		? "delta packed" :
				  (sampleType&ModConstants.SM_IT214)!=0		? "IT V2.14 packed" :
				  (sampleType&ModConstants.SM_IT215)!=0		? "IT V2.15 packed" :
				  (sampleType&ModConstants.SM_ADPCM)!=0		? "ADPCM packed" :
															  "unpacked").append(", ");
		bf.append((sampleType&ModConstants.SM_STEREO)!=0	? "stereo" : "mono").append(", ");
		bf.append("length: ").append(sampleLength);
		return bf.toString();
	}
	/**
	 * Does the linear interpolation with the next sample
	 * @since 06.06.2006
	 * @param result
	 * @param currentSamplePos
	 * @param currentTuningPos
	 * @param isBackwards
	 */
	private void getLinearInterpolated(final SampleFrame result, final int currentSamplePos, final int currentTuningPos)
	{
		long s1 = (sampleL[currentSamplePos  ])<<ModConstants.SAMPLE_SHIFT;
		long s2 = (sampleL[currentSamplePos+1])<<ModConstants.SAMPLE_SHIFT;
		result.left = (s1 + (((s2-s1)*(currentTuningPos))>>ModConstants.SHIFT))>>ModConstants.SAMPLE_SHIFT;

		if (sampleR!=null)
		{
			s1 = (sampleR[currentSamplePos  ])<<ModConstants.SAMPLE_SHIFT;
			s2 = (sampleR[currentSamplePos+1])<<ModConstants.SAMPLE_SHIFT;
			result.right = (s1 + (((s2-s1)*(currentTuningPos))>>ModConstants.SHIFT))>>ModConstants.SAMPLE_SHIFT;
		}
		else
			result.right = result.left;
	}
	/**
	 * does cubic interpolation with the next sample
	 * @since 06.06.2006
	 * @param result
	 * @param currentSamplePos
	 * @param currentTuningPos
	 * @param isBackwards
	 */
	private void getCubicInterpolated(final SampleFrame result, final int currentSamplePos, final int currentTuningPos)
	{
		final int poslo = (currentTuningPos >> CubicSpline.SPLINE_FRACSHIFT) & CubicSpline.SPLINE_FRACMASK;

		long v1 = (CubicSpline.lut[poslo  ]*sampleL[currentSamplePos-1]) +
				  (CubicSpline.lut[poslo+1]*sampleL[currentSamplePos  ]) +
				  (CubicSpline.lut[poslo+2]*sampleL[currentSamplePos+1]) +
				  (CubicSpline.lut[poslo+3]*sampleL[currentSamplePos+2]);
		result.left =  v1 >> CubicSpline.SPLINE_QUANTBITS;

		if (sampleR!=null)
		{
			v1 = (CubicSpline.lut[poslo  ]*sampleR[currentSamplePos-1]) +
				 (CubicSpline.lut[poslo+1]*sampleR[currentSamplePos  ]) +
				 (CubicSpline.lut[poslo+2]*sampleR[currentSamplePos+1]) +
				 (CubicSpline.lut[poslo+3]*sampleR[currentSamplePos+2]);
			result.right = v1 >> CubicSpline.SPLINE_QUANTBITS;
		}
		else
			result.right = result.left;
	}
	/**
	 * does a Kaiser Window interpolation with the next sample
	 * @since 21.02.2024
	 * @param result
	 * @param currentSamplePos
	 * @param currentTuningPos
	 * @param isBackwards
	 */
	private void getKaiser8Interpolated(final SampleFrame result, final int currentSamplePos, final int currentTuning, final int currentTuningPos)
	{
		final int poslo = ((currentTuningPos>>Kaiser.SINC_FRACSHIFT) & Kaiser.SINC_MASK) * 8;
		// Why MPT does this and where this specific borders come from - beyond my knowledge - but, well...
		final int[] sinc = (currentTuning>Kaiser.gDownsample2x_Limit )?Kaiser.gDownsample2x_8:
							(currentTuning>Kaiser.gDownsample13x_Limit)?Kaiser.gDownsample13x_8:
							Kaiser.gKaiserSinc_8;

		long v1 = (sinc[poslo  ]*sampleL[currentSamplePos-3]) +
				  (sinc[poslo+1]*sampleL[currentSamplePos-2]) +
				  (sinc[poslo+2]*sampleL[currentSamplePos-1]) +
				  (sinc[poslo+3]*sampleL[currentSamplePos  ]) +
				  (sinc[poslo+4]*sampleL[currentSamplePos+1]) +
				  (sinc[poslo+5]*sampleL[currentSamplePos+2]) +
				  (sinc[poslo+6]*sampleL[currentSamplePos+3]) +
				  (sinc[poslo+7]*sampleL[currentSamplePos+4]);
		result.left = v1 >> Kaiser.SINC_QUANTSHIFT;

		if (sampleR!=null)
		{
			v1 = (sinc[poslo  ]*sampleR[currentSamplePos-3]) +
				 (sinc[poslo+1]*sampleR[currentSamplePos-2]) +
				 (sinc[poslo+2]*sampleR[currentSamplePos-1]) +
				 (sinc[poslo+3]*sampleR[currentSamplePos  ]) +
				 (sinc[poslo+4]*sampleR[currentSamplePos+1]) +
				 (sinc[poslo+5]*sampleR[currentSamplePos+2]) +
				 (sinc[poslo+6]*sampleR[currentSamplePos+3]) +
				 (sinc[poslo+7]*sampleR[currentSamplePos+4]);
			result.right = v1 >> Kaiser.SINC_QUANTSHIFT;
		}
		else
			result.right = result.left;
	}
	/**
	 * does a Kaiser Window interpolation with the next sample
	 * @since 21.02.2024
	 * @param result
	 * @param currentSamplePos
	 * @param currentTuningPos
	 * @param isBackwards
	 */
	private void getKaiser16Interpolated(final SampleFrame result, final int currentSamplePos, final int currentTuning, final int currentTuningPos)
	{
		final int poslo = ((currentTuningPos>>Kaiser.SINC_FRACSHIFT) & Kaiser.SINC_MASK) * 16;
		// Why MPT does this and where this specific borders come from - beyond my knowledge - but, well...
		final int[] sinc = (currentTuning>Kaiser.gDownsample2x_Limit )?Kaiser.gDownsample2x_16:
							(currentTuning>Kaiser.gDownsample13x_Limit)?Kaiser.gDownsample13x_16:
							Kaiser.gKaiserSinc_16;

		long v1 = (sinc[poslo   ]*sampleL[currentSamplePos-7]) +
				  (sinc[poslo+ 1]*sampleL[currentSamplePos-6]) +
				  (sinc[poslo+ 2]*sampleL[currentSamplePos-5]) +
				  (sinc[poslo+ 3]*sampleL[currentSamplePos-4]) +
				  (sinc[poslo+ 4]*sampleL[currentSamplePos-3]) +
				  (sinc[poslo+ 5]*sampleL[currentSamplePos-2]) +
				  (sinc[poslo+ 6]*sampleL[currentSamplePos-1]) +
				  (sinc[poslo+ 7]*sampleL[currentSamplePos  ]) + 
				  (sinc[poslo+ 8]*sampleL[currentSamplePos+1]) + 
				  (sinc[poslo+ 9]*sampleL[currentSamplePos+2]) + 
				  (sinc[poslo+10]*sampleL[currentSamplePos+3]) + 
				  (sinc[poslo+11]*sampleL[currentSamplePos+4]) + 
				  (sinc[poslo+12]*sampleL[currentSamplePos+5]) + 
				  (sinc[poslo+13]*sampleL[currentSamplePos+6]) + 
				  (sinc[poslo+14]*sampleL[currentSamplePos+7]) + 
				  (sinc[poslo+15]*sampleL[currentSamplePos+8]); 
		result.left = v1 >> Kaiser.SINC_QUANTSHIFT;

		if (sampleR!=null)
		{
			v1 = (sinc[poslo   ]*sampleR[currentSamplePos-7]) +
				 (sinc[poslo+ 1]*sampleR[currentSamplePos-6]) +
				 (sinc[poslo+ 2]*sampleR[currentSamplePos-5]) +
				 (sinc[poslo+ 3]*sampleR[currentSamplePos-4]) +
				 (sinc[poslo+ 4]*sampleR[currentSamplePos-3]) +
				 (sinc[poslo+ 5]*sampleR[currentSamplePos-2]) +
				 (sinc[poslo+ 6]*sampleR[currentSamplePos-1]) +
				 (sinc[poslo+ 7]*sampleR[currentSamplePos  ]) + 
				 (sinc[poslo+ 8]*sampleR[currentSamplePos+1]) + 
				 (sinc[poslo+ 9]*sampleR[currentSamplePos+2]) + 
				 (sinc[poslo+10]*sampleR[currentSamplePos+3]) + 
				 (sinc[poslo+11]*sampleR[currentSamplePos+4]) + 
				 (sinc[poslo+12]*sampleR[currentSamplePos+5]) + 
				 (sinc[poslo+13]*sampleR[currentSamplePos+6]) + 
				 (sinc[poslo+14]*sampleR[currentSamplePos+7]) + 
				 (sinc[poslo+15]*sampleR[currentSamplePos+8]); 
			result.right = v1 >> Kaiser.SINC_QUANTSHIFT;
		}
		else
			result.right = result.left;
	}
	/**
	 * does a windowed fir interpolation with the next sample
	 * @since 21.02.2024
	 * @param currentTuningPos
	 * @return
	 */
	private void getFIRInterpolated(final SampleFrame result, final int currentSamplePos, final int  currentTuningPos)
	{
		final int poslo = ((currentTuningPos+WindowedFIR.WFIR_FRACHALVE)>>WindowedFIR.WFIR_FRACSHIFT) & WindowedFIR.WFIR_FRACMASK;

		long v1 = (WindowedFIR.lut[poslo  ]*sampleL[currentSamplePos-3]) +
				  (WindowedFIR.lut[poslo+1]*sampleL[currentSamplePos-2]) +
				  (WindowedFIR.lut[poslo+2]*sampleL[currentSamplePos-1]) +
				  (WindowedFIR.lut[poslo+3]*sampleL[currentSamplePos  ]);
		long v2 = (WindowedFIR.lut[poslo+4]*sampleL[currentSamplePos+1]) +
				  (WindowedFIR.lut[poslo+5]*sampleL[currentSamplePos+2]) +
				  (WindowedFIR.lut[poslo+6]*sampleL[currentSamplePos+3]) +
				  (WindowedFIR.lut[poslo+7]*sampleL[currentSamplePos+4]);
		result.left = (v1>>1) + (v2>>1) >> (WindowedFIR.WFIR_QUANTBITS-1);

		if (sampleR!=null)
		{
			v1 = (WindowedFIR.lut[poslo  ]*sampleR[currentSamplePos-3]) +
				 (WindowedFIR.lut[poslo+1]*sampleR[currentSamplePos-2]) +
				 (WindowedFIR.lut[poslo+2]*sampleR[currentSamplePos-1]) +
				 (WindowedFIR.lut[poslo+3]*sampleR[currentSamplePos  ]);
			v2 = (WindowedFIR.lut[poslo+4]*sampleR[currentSamplePos+1]) +
				 (WindowedFIR.lut[poslo+5]*sampleR[currentSamplePos+2]) +
				 (WindowedFIR.lut[poslo+6]*sampleR[currentSamplePos+3]) +
				 (WindowedFIR.lut[poslo+7]*sampleR[currentSamplePos+4]);
			result.right = (v1>>1) + (v2>>1) >> (WindowedFIR.WFIR_QUANTBITS-1);
		}
		else
			result.right = result.left;
	}
	/**
	 * Update 14.06.2020 (too late): with bidi Loops, interpolation direction
	 * lookahead must change.
	 * @since 15.06.2006
	 * @return Returns the sample using desired interpolation.
	 */
	public void getInterpolatedSample(final SampleFrame result, final int doISP, final int currentTuning, final int currentSamplePos, final int currentTuningPos, final int interpolationMagic)
	{
		// Shit happens... indeed! Test is <=length because for XM PingPong we run into our added sample data (ridiculous, but that's how it is...)
		if (currentTuning>0 && hasSampleData()/* && currentSamplePos<=length*/)
		{
			final int sampleIndex = currentSamplePos + ((interpolationMagic==0)?INTERPOLATION_LOOK_AHEAD:interpolationMagic);
			// Now return correct sample
			switch (doISP)
			{
				case ModConstants.INTERPOLATION_NONE:
					result.left = sampleL[sampleIndex];
					result.right = (sampleR!=null)? sampleR[sampleIndex] : result.left;
					break;
				case ModConstants.INTERPOLATION_LINEAR:
					getLinearInterpolated(result, sampleIndex, currentTuningPos);
					break;
				case ModConstants.INTERPOLATION_CUBIC:
					getCubicInterpolated(result, sampleIndex, currentTuningPos);
					break;
				case ModConstants.INTERPOLATION_WINDOWSFIR:
					getFIRInterpolated(result, sampleIndex, currentTuningPos);
					break;
				case ModConstants.INTERPOLATION_KAISER_8:
					getKaiser8Interpolated(result, sampleIndex, currentTuning, currentTuningPos);
					break;
				default:
				case ModConstants.INTERPOLATION_KAISER_16:
					getKaiser16Interpolated(result, sampleIndex, currentTuning, currentTuningPos);
					break;
			}
		}
		else
			result.left = result.right = 0;
	}
	/**
	 * @param cues the cues to set
	 */
	public void setCues(final int[] newCues)
	{
		cues = newCues;
	}
	/**
	 * @return the cues
	 */
	public int[] getCues()
	{
		return cues;
	}
	// Do not need this (yet!)
//	public boolean hasCuePoints()
//	{
//		if (cues!=null)
//		{
//			for (int i=0; i<cues.length; i++)
//				if (cues[i]<length) return true;
//		}
//		return false;
//	}
//	public boolean hasCustomCuePoints()
//	{
//		if (cues!=null)
//		{
//			for (int i=0; i<cues.length; i++)
//			{
//				final int defaultPoint = (i+1)<<11;
//				if (cues[i]!=defaultPoint && (cues[i]<length || defaultPoint<length)) return true;
//			}
//		}
//		return false;
//	}
//	public boolean setDefaultCuePoints()
//	{
//		if (cues==null) cues = new int[MAX_CUES];
//		for (int i=0; i<cues.length; i++)
//		{
//			final int defaultPoint = (i+1)<<11;
//			if (defaultPoint<length) cues[i] = defaultPoint; else cues[i] = length;
//		}
//		return false;
//	}
//	public boolean set16BitCuePoints()
//	{
//		if (cues==null) cues = new int[MAX_CUES];
//		for (int i=0; i<cues.length; i++)
//		{
//			final int defaultPoint = (i+1)<<16;
//			if (defaultPoint<length) cues[i] = defaultPoint; else cues[i] = length;
//		}
//		return false;
//	}
	public boolean getAdlibAmplitudeVibrato(final int cm)
	{
		return ((adLib_Instrument[0+cm]>>7)&0x01)>0;
	}
	public boolean getAdlibFrequencyVibrato(final int cm)
	{
		return ((adLib_Instrument[0+cm]>>6)&0x01)>0;
	}
	public boolean getAdlibSustainSound(final int cm)
	{
		return ((adLib_Instrument[0+cm]>>5)&0x01)>0;
	}
	public boolean getAdlibEnvelopeScaling(final int cm)
	{
		return ((adLib_Instrument[0+cm]>>4)&0x01)>0;
	}
	public int getAdlibFrequencyMultiplier(final int cm)
	{
		return adLib_Instrument[0+cm]&0x0F;
	}
	public int getAdlibKeyScaleLevel(final int cm)
	{
		return (adLib_Instrument[2+cm]>>6)&0x03;
	}
	public int getAdlibVolumeLevel(final int cm)
	{
		return adLib_Instrument[2+cm]&0x3F;
	}
	public int getAdlibAttackRate(final int cm)
	{
		return (adLib_Instrument[4+cm]>>4)&0x0F;
	}
	public int getAdlibDecaykRate(final int cm)
	{
		return adLib_Instrument[4+cm]&0x0F;
	}
	public int getAdlibSustainLevel(final int cm)
	{
		return (adLib_Instrument[6+cm]>>4)&0x0F;
	}
	public int getAdlibReleaseRate(final int cm)
	{
		return adLib_Instrument[6+cm]&0x0F;
	}
	public int getAdlibWaveSelect(final int cm)
	{
		return adLib_Instrument[8+cm]&0x07;
	}
	public int getAdlibModulationFeedback()
	{
		return (adLib_Instrument[10]>>1)&0x7;
	}
	public boolean getAdlibAdditiveSynthesis()
	{
		return (adLib_Instrument[10]&0x01)>0;
	}
}
