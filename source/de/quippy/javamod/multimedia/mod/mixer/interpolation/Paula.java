/*
 * @(#) Paula.java
 *
 * Created on 24.04.2026 by Daniel Becker
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
package de.quippy.javamod.multimedia.mod.mixer.interpolation;

import de.quippy.javamod.multimedia.mod.ModConstants;

/**
 * This is migrated from 8bitbubsy PT2 implementation
 * using Aciddose LUT table
 * @author Daniel Becker
 * @since 24.04.2026
 */
public class Paula
{
	private static final double	SMALL_NUMBER		= (1E-4);

	private OnePoleFilter filterLo, filterHi;
	private TwoPoleFilter filterLED;
	private boolean useLEDFilter, useLowpassFilter, useHighpassFilter;
	private BLEP[] blep;
	
	/**
	 * 1-pole RC low-pass/high-pass filter, based on:
	 * https://www.musicdsp.org/en/latest/Filters/116-one-pole-lp-and-hp.html
	 */
	public class OnePoleFilter
	{
		private long tmpL, tmpR, a1, a2;

		public OnePoleFilter(final double audioRate, final double cutOff)
		{
			super();
			initialize(audioRate, cutOff);
		}
		public void initialize(final double audioRate, final double cutOff)
		{
			clearOnePoleFilterState();
			setupOnePoleFilter(audioRate, cutOff);
		}
		public void setupOnePoleFilter(final double audioRate, double cutOff)
		{
			if (cutOff >= audioRate/2.0) cutOff = (audioRate/2.0) - SMALL_NUMBER;

			final double a = 2.0d - Math.cos((ModConstants.TWO_PI * cutOff) / audioRate);
			final double b = a - Math.sqrt((a * a) - 1.0d);

			a1 = (long)((1.0d - b)	* ModConstants.FILTER_PRECISION);
			a2 = (long)(b			* ModConstants.FILTER_PRECISION);
		}
		public void clearOnePoleFilterState()
		{
			tmpL = tmpR = 0;
		}
		public long onePoleLPFilterLeft(final long in)
		{
			tmpL = (((in * a1) + (tmpL * a2)) + ModConstants.HALF_FILTER_PRECISION)>>ModConstants.FILTER_SHIFT_BITS;
			return tmpL;
		}
		public long onePoleLPFilterRight(final long in)
		{
			tmpR = (((in * a1) + (tmpR * a2)) + ModConstants.HALF_FILTER_PRECISION)>>ModConstants.FILTER_SHIFT_BITS;
			return tmpR;
		}
		public long onePoleHPFilterLeft(final long in)
		{
			tmpL = (((in * a1) + (tmpL * a2)) + ModConstants.HALF_FILTER_PRECISION)>>ModConstants.FILTER_SHIFT_BITS;
			return in - tmpL;
		}
		public long onePoleHPFilterRight(final long in)
		{
			tmpR = (((in * a1) + (tmpR * a2)) + ModConstants.HALF_FILTER_PRECISION)>>ModConstants.FILTER_SHIFT_BITS;
			return in - tmpR;
		}
	}
	
	/**
	 * 2-pole RC low-pass filter with Q factor, based on:
	 * https://www.musicdsp.org/en/latest/Filters/38-lp-and-hp-filter.html
	 */
	public class TwoPoleFilter
	{
		private long a1, a2, b1, b2;
		private long tmpL[] = new long[4];
		private long tmpR[] = new long[4];
				
		public TwoPoleFilter(final double audioRate, final double cutOff, final double qFactor)
		{
			super();
			initialize(audioRate, cutOff, qFactor);
		}
		public void initialize(final double audioRate, final double cutOff, final double qFactor)
		{
			clearTwoPoleFilterState();
			setupTwoPoleFilter(audioRate, cutOff, qFactor);
		}
		public void setupTwoPoleFilter(final double audioRate, double cutOff, final double qFactor)
		{
			if (cutOff >= audioRate/2.0) cutOff = (audioRate/2.0) - SMALL_NUMBER;

			final double a = 1.0d / Math.tan((Math.PI * cutOff) / audioRate);
			final double b = 1.0d / qFactor;

			final double d_a1 = 1.0d / (1.0d + b * a + a * a);
			final double d_a2 = 2.0d * d_a1;
			final double d_b1 = 2.0d * (1.0d - a*a) * d_a1;
			final double d_b2 = (1.0d - b * a + a * a) * d_a1;

			a1 = (long)(d_a1 * ModConstants.FILTER_PRECISION);
			a2 = (long)(d_a2 * ModConstants.FILTER_PRECISION);
			b1 = (long)(d_b1 * ModConstants.FILTER_PRECISION);
			b2 = (long)(d_b2 * ModConstants.FILTER_PRECISION);
		}
		public void clearTwoPoleFilterState()
		{
			tmpL[0] = tmpL[1] = tmpL[2] = tmpL[3] =
			tmpR[0] = tmpR[1] = tmpR[2] = tmpR[3] = 0;
		}
		public long twoPoleLPFilterLeft(final long in)
		{
			final long out = (((in*a1) + (tmpL[0]*a2) + (tmpL[1]*a1) - (tmpL[2]*b1) - (tmpL[3]*b2)) + ModConstants.HALF_FILTER_PRECISION) >> ModConstants.FILTER_SHIFT_BITS;

			// shift states
			tmpL[1] = tmpL[0];
			tmpL[0] = in;
			tmpL[3] = tmpL[2];
			tmpL[2] = out;

			// set output
			return out;
		}
		public long twoPoleLPFilterRight(final long in)
		{
			final long out = (((in*a1) + (tmpR[0]*a2) + (tmpR[1]*a1) - (tmpR[2]*b1) - (tmpR[3]*b2)) + ModConstants.HALF_FILTER_PRECISION) >> ModConstants.FILTER_SHIFT_BITS;

			// shift states
			tmpR[1] = tmpR[0];
			tmpR[0] = in;
			tmpR[3] = tmpR[2];
			tmpR[2] = out;

			// set output
			return out;
		}
	}

	/**
	 * @author Daniel Becker
	 * @since 25.04.2026
	 */
	public class BLEP
	{
		// this BLEP table was coded by aciddose
		private static final double[] ACIDDOSE_LUT =
		{
           	 1.000047730261351741631870027, 1.000070326525919428561905988, 1.000026295486963423542192686, 0.999910424773336803383472216,
        	 0.999715744379055859525351480, 0.999433014919733908598686867, 0.999050085771328588712947294, 0.998551121919525108694415394,
        	 0.997915706233591937035498631, 0.997117832692634098457062919, 0.996124815495205595539118804, 0.994896148570364013963285288,
        	 0.993382359323431773923118726, 0.991523909003057091204880180, 0.989250199364479221308954493, 0.986478750833182482793404233,
        	 0.983114620682589257505412661, 0.979050130425507592057954298, 0.974164969358674692756494551, 0.968326735771705471300663248,
        	 0.961391968634788374181709969, 0.953207710646355677042151910, 0.943613628528589098998224927, 0.932444698727279863703643059,
        	 0.919534446669115101968827730, 0.904718706053873278349897191, 0.887839842029686909796737382, 0.868751359331251915563143484,
        	 0.847322794437510795617640724, 0.823444770447693374926245724, 0.797034075604916458779314326, 0.768038612100722994924240083,
        	 0.736442051783192774827568883, 0.702268030364621043126760469, 0.665583712234169455612686761, 0.626502564400415073997407944,
        	 0.585186190589438104403541274, 0.541845095055891845525763983, 0.496738269945924404424886234, 0.450171529567763739621000241,
        	 0.402494548939336449500103754, 0.354096601546017020201162495, 0.305401031227847563620514393, 0.256858534249934655768754510,
        	 0.208939368513054252174399039, 0.162124646097439151226637932, 0.116896901459689991908952322, 0.073730159227936173382822460,
        	 0.033079751373986221452128120,-0.004627847551233893637345762,-0.039004887382349466562470042,-0.069711629260494178961238276,
        	-0.096464776709362487494558991,-0.119044790560825133884925719,-0.137301851759562276722448360,-0.151160268717908163882412964,
        	-0.160621165999489917686204876,-0.165763337555641210308010614,-0.166742199141503621984128358,-0.163786829309547077304642926,
        	-0.157195144771094669211564110,-0.147327312088839507131510231,-0.134597551740997606328775760,-0.119464540741507418974975963,
        	-0.102420664473805989036492292,-0.083980405628003879092702277,-0.064668186778692057781192659,-0.045006002136687713044427284,
        	-0.025501182606377806316722001,-0.006634636085273460347211394, 0.011150108072625494748386643, 0.027456744995838545247979212,
        	 0.041944658451493331552395460, 0.054335772988313046916175608, 0.064419613137017092685532305, 0.072056443764197217194400480,
        	 0.077178424602408784993556878, 0.079788772844964592212413379, 0.079958988468210145938996902, 0.077824255600564926083073658,
        	 0.073577187846729327769246254, 0.067460134194076051827870799, 0.059756303384108616638670242, 0.050779997099921050929260957,
        	 0.040866264989502618099059816, 0.030360306751458156215850437, 0.019606947961234157812304701, 0.008940507097049575288560952,
        	-0.001324648208126466466388882,-0.010902586500777250097526938,-0.019543107356485005243751374,-0.027037657667711743197935803,
        	-0.033223730539404389139335194,-0.037987660680654206091233505,-0.041265784496258707536586741,-0.043043987097204458591725995,
        	-0.043355710312406585404954029,-0.042278543756813086185175621,-0.039929563528408616723819335,-0.036459618831699062979634363,
        	-0.032046794692908199542191738,-0.026889298182776331241905510,-0.021198025763611533928143515,-0.015189070430455576393713457,
        	-0.009076419455364113236806034,-0.003065077316892155564337363, 0.002655175361548794011473662, 0.007915206247809156159256361,
        	 0.012570993866018958726171739, 0.016507022146950881685834034, 0.019638542128970374461838233, 0.021912677934460975809338734,
        	 0.023308395228452325614876273, 0.023835389125096861917540991, 0.023531983633596965932444078, 0.022462165098004915897433875,
        	 0.020711896771322700627759872, 0.018384880003473082210607714, 0.015597939105523964467558962, 0.012476211636472618604631890,
        	 0.009148323764166686397625305, 0.005741721847351755579624832, 0.002378317065490789024989615,-0.000829419425005380466127403,
        	-0.003781796760683148444365242,-0.006394592475568152724341164,-0.008601029202315702004710829,-0.010353009833494400057651852,
        	-0.011621609729198234539637724,-0.012396851839770069853008394,-0.012686815497510708569683935,-0.012516151297987356677543502,
        	-0.011924092281628086154032786,-0.010962065062340150406461348,-0.009691013345942120493781147,-0.008178550345073604815882007,
        	-0.006496056025074358440674072,-0.004715830178801836899959987,-0.002908403455554361104196115,-0.001140096220006421448914247,
        	 0.000529099845866712065883819, 0.002047259427257062253113773, 0.003371840725899812995364213, 0.004470560830528139475981142,
        	 0.005321826318522163493107691, 0.005914733331712991419581993, 0.006248666963769690732566353, 0.006332543236118748190832672,
        	 0.006183747823531677602348910, 0.005826833745009315536356187, 0.005292045316197638814281756, 0.004613737750803969042689978,
        	 0.003828761006839943078355892, 0.002974873016054367744903653, 0.002089241623845963964634098, 0.001207086791118088800120467,
        	 0.000360505313776118753877481,-0.000422490021201840063209965,-0.001118695810940623525803206,-0.001710197865787731110603920,
        	-0.002184605984988852254297109,-0.002535053949380879686342771,-0.002759983640474954029453425,-0.002862739419386348127538611,
        	-0.002851004510552685496799219,-0.002736115017663406229209144,-0.002532289317276390557681642,-0.002255810970882980801693884,
        	-0.001924202080457771161722813,-0.001555421358101014960712005,-0.001167117308478276627506376,-0.000775962090624877551779670,
        	-0.000397086112821087974713435,-0.000043627508742770710237387, 0.000273595371614305691784774, 0.000546286179945486565119606,
        	 0.000768750680536383766867925, 0.000937862797633428843004089, 0.001052928740415311594305625, 0.001115464566897328381120391,
        	 0.001128904823558016723081265, 0.001098261208043675284801166, 0.001029750572351520628011645, 0.000930411077785372455858925,
        	 0.000807724029027989654482000, 0.000669256978018619233528064, 0.000522341243078797709889494, 0.000373794189875729877727689,
        	 0.000229693626208723626408101, 0.000095208625460110475721871,-0.000025511844279469758173208,-0.000129393822077144692462430,
        	-0.000214440509776957504047001,-0.000279687383686450809737456,-0.000325117484787396354272565,-0.000351545144152561614761532,
        	-0.000360476947384593608518510,-0.000353958823942885139873099,-0.000334417806276329982514278,-0.000304506298090264292902779,
        	-0.000266955691183075056582136,-0.000224444953912546549387383,-0.000179488462294699944012469,-0.000134345936549333598141603,
        	-0.000090955956048889888797271,-0.000050893220267083281950406,-0.000015348557788785960678823, 0.000014870297520588306796089,
        	 0.000039319915274267510328903, 0.000057887658269859997505705, 0.000070747063526119185008535, 0.000078305819543209774719408,
        	 0.000081149939344861922907622, 0.000079987451948008292520673, 0.000075594520611004130655058, 0.000068766382254997158183021,
        	 0.000060274927715552435942073, 0.000050834144795034220151546, 0.000041074060306237671633470, 0.000031523273709852359548023,
        	 0.000022599698095009569128290, 0.000014608732178951863478521, 0.000007747790946751235789929, 0.000002115927046159247276264,
        	-0.000002272821614624657917699,-0.000005473727618614671290435,-0.000007594607649309153682893,-0.000008779148282948222000235,
        	-0.000009191289914471795693680,-0.000009001415957147717553273,-0.000008374862616584595860708,-0.000007463035714495605026032,
        	-0.000006397209079216532043657,-0.000005284894977678962862369,-0.000004208528817468251939280,-0.000003226102468093129124012,
        	-0.000002373314390120065336351,-0.000001666778737492929471595,-0.000001107845639559032712541,-0.000000686624974759576246945,
        	-0.000000385868818649388185867,-0.000000184445440432801241354,-0.000000060222272723601931954, 0.000000007740724047001495273,
        	 0.000000037708832885684096027, 0.000000044457942869060847864, 0.000000039034296310091607592, 0.000000028962932871006776366,
        	 0.000000018763994698223029203, 0.000000010636937008622986639, 0.000000005187099504706206719, 0.000000002093670467469700098,
        	 0.000000000648951812097509606, 0.000000000132018063854003986, 0.000000000011591335682393882, 0.000000000000000000000000000,
        	 0.000000000000000000000000000 // 8bitbubsy: one extra zero is required for interpolation look-up
		};

		private static final int BLEP_SIZE			= ACIDDOSE_LUT.length;
		private static final int BLEP_ZC			= 16;
		private static final int BLEP_OS			= 16;
		private static final int BLEP_SP			= 16;
		private static final int BLEP_NS			= (BLEP_ZC * BLEP_OS / BLEP_SP);

		private static final int BLEP_BUFFER_SIZE	= BLEP_ZC + BLEP_OS;
		private static final int BLEP_BUFFER_MASK	= BLEP_BUFFER_SIZE-1;
		
		// Scaling the bits
		private static final int SAMPLE_PRESHIFT	= 32 - 8; // we only have 8 Bit samples with Paula
		private static final int BLEP_SCALE			= 64 - (32-SAMPLE_PRESHIFT) - 1; // 64-8-1=55 Bits. At least Q48 needed so the table keeps its values. The 64-8 would work, but lets keep one bit of head room
		private static final int BLEP_RESTSHIFT		= BLEP_SCALE - SAMPLE_PRESHIFT;

		// Factors to use - so we do not miss the 1L (!)
		private static final long BLEP_FACTOR		= 1L<<BLEP_SCALE;
		private static final long SAMPLE_FACTOR		= 1L<<SAMPLE_PRESHIFT;
		private static final long BLEP_REST_FACTOR	= 1L<<BLEP_RESTSHIFT;

		private static final long[] BLEP_TABLE = new long[BLEP_SIZE];
		private long[] blepBuffer = new long[BLEP_BUFFER_SIZE];

		private int blepPos, blepSamplesLeft;
		private int lastDelta, lastPhase, blepPhase;
		private long lastSample;
		
		static
		{
			initialize();
		}
		
		/**
		 * @since 26.04.2026
		 */
		private static void initialize()
		{
			// We use the original table from Aciddose and convert it on the fly
			// at class loading.
			for (int i=0; i<BLEP_SIZE; i++)
				BLEP_TABLE[i] = (long)(ACIDDOSE_LUT[i] * (double)BLEP_FACTOR);
		}

		public BLEP()
		{
			super();
			resetBlep();
		}
		public void resetBlep()
		{
			blepPos = 0;
			lastDelta = lastPhase = blepPhase = 0;
			lastSample = 0;
			blepSamplesLeft = 0;
			for (int i=0, len=blepBuffer.length; i<len; i++) blepBuffer[i]=0;
		}
		/**
		 * This is called from "BasicModMixer::mixChannelIntoBuffer".
		 * 8BitBubsy Translation:
		 *		v->dPhase					v->dDelta;
		 * we:	aktMemo.currentTuningPos 	aktMemo.currentTuning;
		 * Remark: if the output sample rate is very low (i.e. currentTuning > 1)
		 * we will skip samples in the sampleBuffer. In that case this whole BLEP
		 * does not work as intended.
		 * @since 26.04.2026
		 * @param newSample
		 */
		public void blepAdd(final long newSample)
		{
			// Only trigger on detected discontinuity
			final long delta = lastSample - newSample;
			lastSample = newSample;
			// ensure, that we have a new note and lastPhase is only the overshoot part.
			// Same check as "blepPhase is in range 0..lastDelta"
			// If we do not check that, the index of blepPhaseIndex will be wrong
			if (delta!=0 && lastDelta>lastPhase)
			{
				// blepPhase is currentTuningPos(overshoot) / currentTuning aka phase / delta
				// This is to be 0.0<=blepPhase<1.0 in Q16 fixed point
				int factor = blepPhase * BLEP_SP;

				// Starting index into BLEP table (the phase index)
				int blepPhaseIndex  = (int)(factor >> ModConstants.SHIFT);

				// Factor for linear interpolation between blep points (subsample position)
				factor &= ModConstants.SHIFT_MASK;

				// set current buffer index
				int blepBufferIndex = blepPos;
				for (int n=0; n<BLEP_NS; n++)
				{
					final long v1 = BLEP_TABLE[blepPhaseIndex];
					final long v2 = BLEP_TABLE[blepPhaseIndex+1];
					final long interpolation = (v1 + (((v2 - v1) * (long)factor) / (1L<<ModConstants.SHIFT)));
					// This is a kind of hack. We exceed the capacity of long when trying to multiply the BLEP-TABLE
					// and the 32 bit signed delta sample. The good thing is: when Paula is active, we always(!)
					// only talk about 8 bit signed samples (ProTracker) so we simply get rid of the whole zeros
					// we have by reducing the delta, multiply with the interpolated BLEP value
					// and finally shift back the rest 
					blepBuffer[blepBufferIndex] += ((delta / SAMPLE_FACTOR) * interpolation) / BLEP_REST_FACTOR;

					// advance
					blepPhaseIndex+=BLEP_SP;
					blepBufferIndex = (blepBufferIndex + 1) & BLEP_BUFFER_MASK;
				}
				blepSamplesLeft = BLEP_NS;
			}
		}
		/**
		 * This will return the sample delta to add to the current
		 * sample we use.
		 * If no sample delta are left, we return zero.
		 * then over the top.
		 * @since 26.04.2026
		 * @param inputSample
		 * @return
		 */
		public long blepRun()
		{
			// For performance. If there is nothing in the buffer, we can return the zero right away
			if (blepSamplesLeft<=0) return 0;
			
			// as we did not normalize in blepAdd, we *must* normalize here!
			final long result = blepBuffer[blepPos];
			blepBuffer[blepPos] = 0; // clear buffer!
			
			blepPos = (blepPos + 1) & BLEP_BUFFER_MASK; // and advance to next index
			blepSamplesLeft--;

			return result;
		}
		/**
		 * Something changed:
		 * - new Sample
		 * - new Tuning
		 * --> we now keep the old tuning (aka delta) and tuningPos (phase)
		 * currentTuningPos should (must) be the "overshoot", i.e. tuningPos
		 * is between 0 and currentTuning. We result in Values for blepOffest
		 * of 0..1 (in Q16 notation)
		 * This must be called when we proceed to the next sample
		 * and when we recalculate the playerTuning (i.e. set a new period)
		 * --> is called in
		 *   BasicModMixer::resetInstrumentPointers (restart the/new instrument)
		 *   BasicModMixer::fitIntoLoops (proceed to next sample)
		 * @since 26.04.2026
		 * @param currentTuning
		 * @param currentTuningPos
		 */
		public void refetchPeriod(final int currentTuning, final int currentTuningPosOvershoot)
		{
			lastDelta = currentTuning;
			lastPhase = currentTuningPosOvershoot;
			blepPhase = (currentTuning!=0)?(int)(((long)(lastPhase)<<ModConstants.SHIFT) / (long)lastDelta):0;
		}
	}

	/**
	 * Constructor for Paula
	 */
	public Paula(final int amigaModel, final int sampleRate, final int channels)
	{
		super();
		initialize(amigaModel, sampleRate, channels);
	}
	public void initialize(final int amigaModel, final int sampleRate, final int channels)
	{
		if (blep==null || blep.length!=channels)
		{
			blep = new BLEP[channels];
			for (int i=0; i<channels; i++) blep[i] = new BLEP();
		}
		else
		{
			for (int i=0; i<channels; i++) blep[i].resetBlep();
		}
		
		useLEDFilter = false;
		useLowpassFilter = useHighpassFilter = true;
		
		// We use the values from 8BitBubsy's ProTracker Clone here
		double R, C, R1, R2, C1, C2, cutoff, qfactor;

		if (amigaModel == ModConstants.AMIGAEMULATION_AMIGA500)
		{
			// A500 1-pole (6dB/oct) RC low-pass filter:
			R = 360.0d; // R321 (360 ohm)
			C = 1e-7d;  // C321 (0.1uF)
			cutoff = 1.0d / (ModConstants.TWO_PI * R * C); // ~4420.971Hz
			if (filterLo!=null)
				filterLo.initialize(sampleRate, cutoff);
			else
				filterLo = new OnePoleFilter(sampleRate, cutoff);

			// A500 1-pole (6dB/oct) RC high-pass filter:
			R = 1390.0d;   // R324 (1K ohm) + R325 (390 ohm)
			C = 2.233e-5d; // C334 (22uF) + C335 (0.33uF)
			cutoff = 1.0d / (ModConstants.TWO_PI * R * C); // ~5.128Hz
			if (filterHi!=null)
				filterHi.initialize(sampleRate, cutoff);
			else
				filterHi = new OnePoleFilter(sampleRate, cutoff);
		}
		else
		{
			/* Don't use the A1200 low-pass filter since its cutoff
			** is well above human hearable range anyway (~34.4kHz).
			** We don't do volume PWM, so we have nothing we need to
			** filter away.
			*/
			filterLo = null;
			useLowpassFilter = false;

			// A1200 1-pole (6dB/oct) RC high-pass filter:
			R = 1360.0d; // R324 (1K ohm resistor) + R325 (360 ohm resistor)
			C = 2.2e-5d; // C334 (22uF capacitor)
			cutoff = 1.0d / (ModConstants.TWO_PI * R * C); // ~5.319Hz
			if (filterHi!=null)
				filterHi.initialize(sampleRate, cutoff);
			else
				filterHi = new OnePoleFilter(sampleRate, cutoff);
		}
		// Note: A500 rev3 (old) -may- be C1 = 7500pF (cutoff = 2942.776Hz, qfactor = 0.693375)
		
		// 2-pole (12dB/oct) RC low-pass filter ("LED" filter, same values on A500/A1200):
		R1 = 10000.0d; // R322 (10K ohm)
		R2 = 10000.0d; // R323 (10K ohm)
		C1 = 6.8e-9d;  // C322 (6800pF)
		C2 = 3.9e-9d;  // C323 (3900pF)
		cutoff = 1.0d / (ModConstants.TWO_PI * Math.sqrt(R1 * R2 * C1 * C2)); // ~3090.533Hz
		qfactor = Math.sqrt(R1 * R2 * C1 * C2) / (C2 * (R1 + R2)); // ~0.660225
		if (filterLED!=null)
			filterLED.initialize(sampleRate, cutoff, qfactor);
		else
			filterLED = new TwoPoleFilter(sampleRate, cutoff, qfactor);
	}
	/**
	 * This is called from "BasicModMixer::mixChannelIntoBuffer".
	 * 8BitBubsy Translation:
	 *		v->dPhase					v->dDelta;
	 * we:	aktMemo.currentTuningPos 	aktMemo.currentTuning;
	 * Remark: if the output sample rate is very low (i.e. currentTuning > 1)
	 * we will skip samples in the sampleBuffer. In that case this whole BLEP
	 * does not work as intended.
	 * @since 26.04.2026
	 * @param channel
	 * @param currentTuning
	 * @param currentTuningPos
	 * @param newSample
	 */
	public void blepAdd(final int channel, final long newSample)
	{
		if (blep!=null && blep[channel]!=null)
			blep[channel].blepAdd(newSample);
	}
	/**
	 * This is called from "BasicModMixer::mixChannelIntoBuffer".
	 * @since 26.04.2026
	 * @param channel
	 * @param newSample
	 * @return
	 */
	public long blepRun(final int channel)
	{
		if (blep!=null && blep[channel]!=null)
			return blep[channel].blepRun();
		return 0;
	}
	/**
	 * @since 26.04.2026
	 * @param channel
	 * @param currentTuning
	 * @param currentTuningPos
	 */
	public void refetchPeriod(final int channel, final int currentTuning, final int currentTuningPosOvershoot)
	{
		if (blep!=null && blep[channel]!=null)
			blep[channel].refetchPeriod(currentTuning, currentTuningPosOvershoot);
	}
	/**
	 * Disables low-pass/high-pass filter ("LED" filter is kept) 
	 * @since 26.04.2026
	 */
	public void disableFilters()
	{
		useHighpassFilter = useLowpassFilter = false;
	}
	/**
	 * Needed for extended effect (0xE0x) - setFilter 
	 * @since 26.04.2026
	 * @param newState
	 */
	public void setLEDFilter(final boolean newState)
	{
		final boolean oldState = useLEDFilter;
		useLEDFilter = newState;
		if (oldState!=newState && filterLED!=null) filterLED.clearTwoPoleFilterState();
	}
	/**
	 * Will apply the filters needed for Amiga emulation.
	 * This is the last call in "BasicModMixer::mixIntoBuffer".
	 * @since 26.04.2026
	 * @param leftBuffer
	 * @param rightBuffer
	 * @param startIndex
	 * @param endIndex
	 */
	public void performFilters(final long[] leftBuffer, final long[] rightBuffer, final int startIndex, final int endIndex)
	{
		for (int i=startIndex; i<endIndex; i++)
		{
			if (useLowpassFilter)
			{
				leftBuffer[i] = filterLo.onePoleLPFilterLeft(leftBuffer[i]);
				rightBuffer[i] = filterLo.onePoleLPFilterRight(rightBuffer[i]);
			}
			if (useLEDFilter)
			{
				leftBuffer[i] = filterLED.twoPoleLPFilterLeft(leftBuffer[i]);
				rightBuffer[i] = filterLED.twoPoleLPFilterRight(rightBuffer[i]);
			}
			if (useHighpassFilter)
			{
				leftBuffer[i] = filterHi.onePoleHPFilterLeft(leftBuffer[i]);
				rightBuffer[i] = filterHi.onePoleHPFilterRight(rightBuffer[i]);
			}
		}
	}
}
