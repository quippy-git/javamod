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
import de.quippy.javamod.multimedia.mod.mixer.interpolation.CubicSpline;
import de.quippy.javamod.multimedia.mod.mixer.interpolation.WindowedFIR;

/**
 * Used to store the Instruments
 * @author Daniel Becker
 * @since 21.04.2006
 */
public class Sample
{
	public String name;			// Name of the sample
	public int length;			// full length (already *2 --> Mod-Format)
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
	public int type;			// always 1 for a sample
	public String dosFileName;	// DOS File-Name
	public int flags;			// flag: 1:Looping sample 2:Stereo 4:16Bit-Sample...
	
	// XM
	public int panning;			// default Panning
	public int vibratoType;		// Vibrato Type 
	public int vibratoSweep;	// Vibrato Sweep
	public int vibratoDepth;	// Vibrato Depth
	public int vibratoRate;		// Vibrato Rate
	public byte XM_reserved;	// reserved, but some magic with 0xAD and SM_ADPCM4...

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
	
	public static final int INTERPOLATION_LOOK_AHEAD = 16;

	// The sampledata, already converted to signed 32 bit (always)
	// 8Bit: 0..127,128-255; 16Bit: -32768..0..+32767
	public long [] sampleL;
	public long [] sampleR;
	
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
		final int alloc = length + ((1 + 1 + 4 + 4 + 4 + 4) * INTERPOLATION_LOOK_AHEAD);
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
		if (sampleL==null || length==0)
		{
			loopType = loopLength = loopStop = loopStart = 
			sustainLoopLength = sustainLoopStart = sustainLoopStop = 0;
			return;
		}
		// A sample point index greater than the array index
		// needs to be allowed (! >=)
		if (loopStop>length) loopStop = length;
		if (loopStart<0) loopStart = 0;
		loopLength = loopStop - loopStart;
		
		if (sustainLoopStop>length) sustainLoopStop = length;
		if (sustainLoopStart<0) sustainLoopStart = 0;
		sustainLoopLength = sustainLoopStop - sustainLoopStart;

		// Kill invalid loops
		// with protracker, a loopsize of 2 is considered invalid
		if (((modType&ModConstants.MODTYPE_MOD)!=0 && loopStart+2>loopStop) ||
				loopStart > loopStop)
		{
			loopStart = loopStop = 0;
			loopType &= ~ModConstants.LOOP_ON;
		}
		if (sustainLoopStart>sustainLoopStop)
		{
			sustainLoopStart = sustainLoopStop = 0;
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
			
			if (readPosition == length-1 && readIncrement > 0)
			{
				if (isPingPong)
				{
					readIncrement = -1;
					//if (readPosition>0) readPosition--;
				}
				else
					readPosition = 0;
			}
			else
			if (readPosition == 0 && readIncrement < 0)
			{
				if (isPingPong)
				{
					readIncrement = 1;
					//readPosition++;
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
		final int afterSampleData = startSampleData + length;
		interpolationStopLoop = afterSampleData + INTERPOLATION_LOOK_AHEAD;
		interpolationStopSustain = interpolationStopLoop + (4 * INTERPOLATION_LOOK_AHEAD);
		interpolationStartLoop = interpolationStopSustain + (4 * INTERPOLATION_LOOK_AHEAD);
		interpolationStartSustain = interpolationStartLoop + (4 * INTERPOLATION_LOOK_AHEAD);
		
		// First move sampleData out of the way, as it is loaded at index 0
		for (int pos=length-1; pos>=0; pos--)
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
			addInterpolationLookAheadDataLoop(interpolationStopLoop, loopLength, loopStart + INTERPOLATION_LOOK_AHEAD, true, (loopType&ModConstants.LOOP_IS_PINGPONG)!=0, true);
			addInterpolationLookAheadDataLoop(interpolationStopLoop, loopLength, loopStart + INTERPOLATION_LOOK_AHEAD, false, (loopType&ModConstants.LOOP_IS_PINGPONG)!=0, true);
			addInterpolationLookAheadDataLoop(interpolationStartLoop, loopLength, loopStart + INTERPOLATION_LOOK_AHEAD, true, (loopType&ModConstants.LOOP_IS_PINGPONG)!=0, false);
			addInterpolationLookAheadDataLoop(interpolationStartLoop, loopLength, loopStart + INTERPOLATION_LOOK_AHEAD, false, (loopType&ModConstants.LOOP_IS_PINGPONG)!=0, false);
		}
		if ((loopType & ModConstants.LOOP_SUSTAIN_ON)!=0)
		{
			addInterpolationLookAheadDataLoop(interpolationStopSustain, sustainLoopLength, sustainLoopStart + INTERPOLATION_LOOK_AHEAD, true, (loopType&ModConstants.LOOP_IS_PINGPONG)!=0, true);
			addInterpolationLookAheadDataLoop(interpolationStopSustain, sustainLoopLength, sustainLoopStart + INTERPOLATION_LOOK_AHEAD, false, (loopType&ModConstants.LOOP_IS_PINGPONG)!=0, true);
			addInterpolationLookAheadDataLoop(interpolationStartSustain, sustainLoopLength, sustainLoopStart + INTERPOLATION_LOOK_AHEAD, true, (loopType&ModConstants.LOOP_IS_PINGPONG)!=0, false);
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
	public int getSustainLoopMagic(final int currentSamplePos, final int loopCounter)
	{
		if (currentSamplePos + INTERPOLATION_LOOK_AHEAD >= sustainLoopStop) // approaching sustainLoopStop?
			return interpolationStopSustain - sustainLoopStop + (INTERPOLATION_LOOK_AHEAD<<1);
		else
		if (currentSamplePos - INTERPOLATION_LOOK_AHEAD <= sustainLoopStart && loopCounter>0) // approaching/leaving sustainLoopStart?
			return interpolationStartSustain - sustainLoopStart + (INTERPOLATION_LOOK_AHEAD<<1);
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
	public int getLoopMagic(final int currentSamplePos, final int loopCounter)
	{
		if (currentSamplePos + INTERPOLATION_LOOK_AHEAD >= loopStop)  // approaching loopStop?
			return interpolationStopLoop - loopStop + (INTERPOLATION_LOOK_AHEAD<<1);
		else
		if (currentSamplePos - INTERPOLATION_LOOK_AHEAD <= loopStart && loopCounter>0) // approaching/leaving LoopStart?
			return interpolationStartLoop - loopStart + (INTERPOLATION_LOOK_AHEAD<<1);
		else 
			return 0;
	}
	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder bf = new StringBuilder(this.name);
		bf.append('(').
			append(getSampleTypeString()).append(',').
			append("fineTune:").append(fineTune).append(',').
			append("transpose:").append(transpose).append(',').
			append("baseFrequency:").append(baseFrequency).append(',').
			append("volume:").append(volume).append(',').
			append("panning:").append(panning).append(',').
			append("loopStart:").append(loopStart).append(',').
			append("loopStop:").append(loopStop).append(')').
			append("loopLength:").append(loopLength).append(',').
			append("SustainloopStart:").append(sustainLoopStart).append(',').
			append("SustainloopStop:").append(sustainLoopStop).append(')').
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
		StringBuilder bf = new StringBuilder();
		bf.append((sampleType&ModConstants.SM_16BIT)!=0 ? "16-Bit" : "8-Bit").append(", ");
		bf.append((sampleType&ModConstants.SM_PCMU)!=0 ? "unsigned" : "signed").append(", ");
		bf.append((sampleType&ModConstants.SM_PCMD)!=0 ? "delta packed" : (sampleType&ModConstants.SM_IT2148)!=0 ? "IT V2.14 packed" : (sampleType&ModConstants.SM_IT2158)!=0 ? "IT V2.15 packed" : "unpacked").append(", ");
		bf.append((sampleType&ModConstants.SM_STEREO)!=0 ? "stereo" : "mono").append(", ");
		bf.append("length: ").append(length);
		return bf.toString();
	}
	/**
	 * Does the linear interpolation with the next sample
	 * @since 06.06.2006
	 * @param currentTuningPos
	 * @return
	 */
	private void getLinearInterpolated(final long result[], final int currentSamplePos, final int currentTuningPos, final boolean isBackwards)
	{
		long s1 = ((long)sampleL[currentSamplePos  ])<<ModConstants.SAMPLE_SHIFT;
		long s2 = 
			(isBackwards)?
			    ((long)sampleL[currentSamplePos-1])<<ModConstants.SAMPLE_SHIFT
			:
				((long)sampleL[currentSamplePos+1])<<ModConstants.SAMPLE_SHIFT
		;
		result[0] = (long)((s1 + (((s2-s1)*((long)currentTuningPos))>>ModConstants.SHIFT))>>ModConstants.SAMPLE_SHIFT);
		
		if (sampleR!=null)
		{
			s1 = ((long)sampleR[currentSamplePos  ])<<ModConstants.SAMPLE_SHIFT;
			s2 = 
				(isBackwards)?
				    ((long)sampleR[currentSamplePos-1])<<ModConstants.SAMPLE_SHIFT
				:
					((long)sampleR[currentSamplePos+1])<<ModConstants.SAMPLE_SHIFT
			;
			result[1] = (long)((s1 + (((s2-s1)*((long)currentTuningPos))>>ModConstants.SHIFT))>>ModConstants.SAMPLE_SHIFT);
		}
		else
			result[1] = result[0];
	}
	/**
	 * does cubic interpolation with the next sample
	 * @since 06.06.2006
	 * @param currentTuningPos
	 * @return
	 */
	private void getCubicInterpolated(final long result [], final int currentSamplePos, final int currentTuningPos, final boolean isBackwards)
	{
		final int poslo = (currentTuningPos >> CubicSpline.SPLINE_FRACSHIFT) & CubicSpline.SPLINE_FRACMASK;
		
		long v1 = 
			(isBackwards)?
			    ((long)CubicSpline.lut[poslo  ]*(long)sampleL[currentSamplePos+1]) +
			    ((long)CubicSpline.lut[poslo+1]*(long)sampleL[currentSamplePos  ]) +
			    ((long)CubicSpline.lut[poslo+2]*(long)sampleL[currentSamplePos-1]) +
			    ((long)CubicSpline.lut[poslo+3]*(long)sampleL[currentSamplePos-2])
			:
				((long)CubicSpline.lut[poslo  ]*(long)sampleL[currentSamplePos-1]) +
				((long)CubicSpline.lut[poslo+1]*(long)sampleL[currentSamplePos  ]) +
				((long)CubicSpline.lut[poslo+2]*(long)sampleL[currentSamplePos+1]) +
				((long)CubicSpline.lut[poslo+3]*(long)sampleL[currentSamplePos+2])
		;
		
		result[0] =  (long)(v1 >> CubicSpline.SPLINE_QUANTBITS);
		if (sampleR!=null)
		{
			v1 = 
				(isBackwards)?
				    ((long)CubicSpline.lut[poslo  ]*(long)sampleR[currentSamplePos+1]) +
				    ((long)CubicSpline.lut[poslo+1]*(long)sampleR[currentSamplePos  ]) +
				    ((long)CubicSpline.lut[poslo+2]*(long)sampleR[currentSamplePos-1]) +
				    ((long)CubicSpline.lut[poslo+3]*(long)sampleR[currentSamplePos-2])
				:
					((long)CubicSpline.lut[poslo  ]*(long)sampleR[currentSamplePos-1]) +
					((long)CubicSpline.lut[poslo+1]*(long)sampleR[currentSamplePos  ]) +
					((long)CubicSpline.lut[poslo+2]*(long)sampleR[currentSamplePos+1]) +
					((long)CubicSpline.lut[poslo+3]*(long)sampleR[currentSamplePos+2])
			;
			
			result[1] = (long)(v1 >> CubicSpline.SPLINE_QUANTBITS);
		}
		else
			result[1] = result[0];
	}
	/**
	 * does a windowed fir interpolation with the next sample
	 * @since 15.06.2006
	 * @param currentTuningPos
	 * @return
	 */
	private void getFIRInterpolated(final long result[], final int currentSamplePos, final int  currentTuningPos, final boolean isBackwards)
	{
		final int poslo  = currentTuningPos & WindowedFIR.WFIR_POSFRACMASK;
		final int firidx = ((poslo+WindowedFIR.WFIR_FRACHALVE)>>WindowedFIR.WFIR_FRACSHIFT) & WindowedFIR.WFIR_FRACMASK;
		
		long v1 =
		(isBackwards)?
		    ((long)WindowedFIR.lut[firidx  ]*(long)sampleL[currentSamplePos+3]) +
			((long)WindowedFIR.lut[firidx+1]*(long)sampleL[currentSamplePos+2]) +
			((long)WindowedFIR.lut[firidx+2]*(long)sampleL[currentSamplePos+1]) +
			((long)WindowedFIR.lut[firidx+3]*(long)sampleL[currentSamplePos  ]) +
			((long)WindowedFIR.lut[firidx+4]*(long)sampleL[currentSamplePos-1]) +
			((long)WindowedFIR.lut[firidx+5]*(long)sampleL[currentSamplePos-2]) +
			((long)WindowedFIR.lut[firidx+6]*(long)sampleL[currentSamplePos-3]) +
			((long)WindowedFIR.lut[firidx+7]*(long)sampleL[currentSamplePos-4])
		:
		    ((long)WindowedFIR.lut[firidx  ]*(long)sampleL[currentSamplePos-3]) +
			((long)WindowedFIR.lut[firidx+1]*(long)sampleL[currentSamplePos-2]) +
			((long)WindowedFIR.lut[firidx+2]*(long)sampleL[currentSamplePos-1]) +
			((long)WindowedFIR.lut[firidx+3]*(long)sampleL[currentSamplePos  ]) +
			((long)WindowedFIR.lut[firidx+4]*(long)sampleL[currentSamplePos+1]) +
			((long)WindowedFIR.lut[firidx+5]*(long)sampleL[currentSamplePos+2]) +
			((long)WindowedFIR.lut[firidx+6]*(long)sampleL[currentSamplePos+3]) +
			((long)WindowedFIR.lut[firidx+7]*(long)sampleL[currentSamplePos+4])
		;
		result[0] = (long)(v1 >> WindowedFIR.WFIR_16BITSHIFT);
		
		if (sampleR!=null)
		{
			v1 =
				(isBackwards)?
				    ((long)WindowedFIR.lut[firidx  ]*(long)sampleR[currentSamplePos+3]) +
					((long)WindowedFIR.lut[firidx+1]*(long)sampleR[currentSamplePos+2]) +
					((long)WindowedFIR.lut[firidx+2]*(long)sampleR[currentSamplePos+1]) +
					((long)WindowedFIR.lut[firidx+3]*(long)sampleR[currentSamplePos  ]) +
					((long)WindowedFIR.lut[firidx+4]*(long)sampleR[currentSamplePos-1]) +
					((long)WindowedFIR.lut[firidx+5]*(long)sampleR[currentSamplePos-2]) +
					((long)WindowedFIR.lut[firidx+6]*(long)sampleR[currentSamplePos-3]) +
					((long)WindowedFIR.lut[firidx+7]*(long)sampleR[currentSamplePos-4])
				:
				    ((long)WindowedFIR.lut[firidx  ]*(long)sampleR[currentSamplePos-3]) +
					((long)WindowedFIR.lut[firidx+1]*(long)sampleR[currentSamplePos-2]) +
					((long)WindowedFIR.lut[firidx+2]*(long)sampleR[currentSamplePos-1]) +
					((long)WindowedFIR.lut[firidx+3]*(long)sampleR[currentSamplePos  ]) +
					((long)WindowedFIR.lut[firidx+4]*(long)sampleR[currentSamplePos+1]) +
					((long)WindowedFIR.lut[firidx+5]*(long)sampleR[currentSamplePos+2]) +
					((long)WindowedFIR.lut[firidx+6]*(long)sampleR[currentSamplePos+3]) +
					((long)WindowedFIR.lut[firidx+7]*(long)sampleR[currentSamplePos+4])
				;
				result[1] = (long)(v1 >> WindowedFIR.WFIR_16BITSHIFT);
		}
		else
			result[1] = result[0];
	}
	/**
	 * Update 14.06.2020 (too late): with bidi Loops, interpolation direction
	 * lookahead must change.
	 * @since 15.06.2006
	 * @return Returns the sample using desired interpolation.
	 */
	public void getInterpolatedSample(final long result[], final int doISP, final int currentSamplePos, final int currentTuningPos, final boolean isBackwards, final int interpolationMagic)
	{
		// Shit happens... indeed!
		if (sampleL!=null && currentSamplePos<=length)
		{
			final int sampleIndex = currentSamplePos + ((interpolationMagic==0)?INTERPOLATION_LOOK_AHEAD:interpolationMagic);
			// Now return correct sample
			switch (doISP)
			{
				default:
				case 0: 
					result[0] = sampleL[sampleIndex];
					result[1] = (sampleR!=null)? sampleR[sampleIndex] : result[0];
					break;
				case 1:
					getLinearInterpolated(result, sampleIndex, currentTuningPos, isBackwards);
					break;
				case 2:
					getCubicInterpolated(result, sampleIndex, currentTuningPos, isBackwards);
					break;
				case 3:
					getFIRInterpolated(result, sampleIndex, currentTuningPos, isBackwards);
					break;
			}
		}
		else
			result[0] = result[1] = 0;
	}
	/**
	 * @param baseFrequency The baseFrequency to set.
	 */
	public void setBaseFrequency(final int baseFrequency)
	{
		this.baseFrequency = baseFrequency;
	}
	/**
	 * @param dosFileName The dosFileName to set.
	 */
	public void setDosFileName(final String dosFileName)
	{
		this.dosFileName = dosFileName;
	}
	/**
	 * @param sampleType the sampleType to set
	 */
	public void setSampleType(int sampleType)
	{
		this.sampleType = sampleType;
	}
	/**
	 * @param fineTune The fineTune to set.
	 */
	public void setFineTune(final int fineTune)
	{
		this.fineTune = fineTune;
	}
	/**
	 * @param flags The flags to set.
	 */
	public void setFlags(final int newFlags)
	{
		flags = newFlags;
	}
	/**
	 * @param length The length to set.
	 */
	public void setLength(final int length)
	{
		this.length = length;
	}
	/**
	 * @param loopType The loopType to set.
	 */
	public void setLoopType(final int loopType)
	{
		this.loopType = loopType;
	}
	/**
	 * @param name The name to set.
	 */
	public void setName(final String name)
	{
		this.name = name;
	}
	/**
	 * @param loopLength The loopLength to set.
	 */
	public void setLoopLength(final int loopLength)
	{
		this.loopLength = loopLength;
	}
	/**
	 * @param loopStart The loopStart to set.
	 */
	public void setLoopStart(final int loopStart)
	{
		this.loopStart = loopStart;
	}
	/**
	 * @param loopStop The loopStop to set.
	 */
	public void setLoopStop(final int loopEnd)
	{
		this.loopStop = loopEnd;
	}
	/**
	 * @param sustainLoopStart the sustainLoopStart to set
	 */
	public void setSustainLoopStart(final int sustainLoopStart)
	{
		this.sustainLoopStart = sustainLoopStart;
	}
	/**
	 * @param sustainLoopEnd the sustainLoopEnd to set
	 */
	public void setSustainLoopStop(final int sustainLoopStop)
	{
		this.sustainLoopStop = sustainLoopStop;
	}
	/**
	 * @param sustainLoopLength the sustainLoopLength to set
	 */
	public void setSustainLoopLength(final int sustainLoopLength)
	{
		this.sustainLoopLength = sustainLoopLength;
	}
	/**
	 * @param sample The sample to set.
	 */
	public void setSampleL(final long[] sample)
	{
		this.sampleL = sample;
	}
	/**
	 * @param sample The sample to set.
	 */
	public void setSampleR(final long[] sample)
	{
		this.sampleR = sample;
	}
	/**
	 * @param transpose The transpose to set.
	 */
	public void setTranspose(final int transpose)
	{
		this.transpose = transpose;
	}
	/**
	 * @param type The type to set.
	 */
	public void setType(final int type)
	{
		this.type = type;
	}
	/**
	 * @param volume The volume to set.
	 */
	public void setVolume(final int volume)
	{
		this.volume = volume;
	}
	/**
	 * @param panning The panning to set.
	 */
	public void setPanning(final int panning)
	{
		this.panning = panning;
	}
	/**
	 * @param isStereo the isStereo to set
	 */
	public void setStereo(final boolean isStereo)
	{
		this.isStereo = isStereo;
	}
	/**
	 * @param cvT the cvT to set
	 */
	public void setCvT(final int flag_CvT)
	{
		this.flag_CvT = flag_CvT;
	}
	/**
	 * @param vibratoDepth The vibratoDepth to set.
	 */
	public void setVibratoDepth(final int vibratoDepth)
	{
		this.vibratoDepth = vibratoDepth;
	}
	/**
	 * @param vibratoRate The vibratoRate to set.
	 */
	public void setVibratoRate(final int vibratoRate)
	{
		this.vibratoRate = vibratoRate;
	}
	/**
	 * @param vibratoSweep The vibratoSweep to set.
	 */
	public void setVibratoSweep(final int vibratoSweep)
	{
		this.vibratoSweep = vibratoSweep;
	}
	/**
	 * @param vibratoType The vibratoType to set.
	 */
	public void setVibratoType(final int vibratoType)
	{
		this.vibratoType = vibratoType;
	}
	/**
	 * @param globalVolume the globalVolume to set
	 */
	public void setGlobalVolume(final int globalVolume)
	{
		this.globalVolume = globalVolume;
	}
}
