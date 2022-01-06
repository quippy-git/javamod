/*
 * @(#) FMOPL_072.java
 *
 * Created on 11.08.2020 by Daniel Becker
 * 
 *-----------------------------------------------------------------------
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General private License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General private License for more details.
 *
 *  You should have received a copy of the GNU General private License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *----------------------------------------------------------------------
 *
 * File: fmopl.c - software implementation of FM sound generator
 *                                            types OPL and OPL2
 *
 * Copyright Jarek Burczynski (bujar at mame dot net)
 * Copyright Tatsuyuki Satoh , MultiArcadeMachineEmulator development
 *
 * Version 0.72 
 *
 * Java Port by Daniel Becker in 2020
 * No optimization regarding usage of byte or short - especially
 * as java converts all bit operations into int anyways.
 * short a; short b = (short)(a<<1): result is an int, so cast to short is 
 * needed.
 * Function-Pointers not existent in Java - need interfaces instead. But we
 * do not use callbacks here anyways.
 * The Y8950 code was documented out, we do not need it here. 
 */
package de.quippy.opl3;

/**
 * @author Daniel Becker
 * @since 11.08.2020
 */
public class FMOPL_072
{
//	private static final int BUILD_YM3812 = 1;
//	private static final int BUILD_YM3526 = 1;
//	private static final int BUILD_Y8950  = 0;
	
	interface OPL_IRQHANDLER
	{
		void invoke(int irq);
	}
	interface OPL_TIMERHANDLER
	{
		void invoke(int timer, double period);
	}
	interface OPL_UPDATEHANDLER
	{
		void invoke(int min_interval_us);
	}
//#if BUILD_Y8950
//	interface STATUS_CHANGE_HANDLER
//	{
//		public void STATUS_SET(final int status_bits);
//		public void STATUS_RESET(final int status_bits);
//	}
//	interface OPL_PORTHANDLER_R
//	{
//		public byte invoke();
//	}
//	interface OPL_PORTHANDLER_W
//	{
//		public void invoke(int data);
//	}
//#endif

	/* output final shift */
	private static final int FINAL_SH	= 0;
	private static final int MAXOUT		= 0x00007FFF;
	private static final int MINOUT		= 0xFFFF8000;

	private static final int FREQ_SH		= 16; // 16.16 fixed point (frequency calculations)
	private static final int EG_SH		= 16; // 16.16 fixed point (EG timing)
	private static final int LFO_SH		= 24; //  8.24 fixed point (LFO calculations)
	//private static final int TIMER_SH	= 16; // 16.16 fixed point (timers calculations)
	
	private static final int FREQ_MASK	= ((1<<FREQ_SH)-1);
	private static final int ENV_BITS 	= 10;
	private static final int ENV_LEN		= (1<<ENV_BITS);
	private static final double ENV_STEP	= (128.0/ENV_LEN);
	
	private static final int MAX_ATT_INDEX = ((1<<(ENV_BITS-1))-1); /*511*/
	private static final int MIN_ATT_INDEX = 0;
	
	private static final int SIN_BITS 	  = 10;
	private static final int SIN_LEN       = (1<<SIN_BITS);
	private static final int SIN_MASK      = (SIN_LEN-1);
	
	private static final int TL_RES_LEN = 256; // 8 bits addressing (real chip)
	
	/* register number to channel number, slot offset */
	private static final int SLOT1 = 0;
	private static final int SLOT2 = 1;
	
	/* Envelope Generator phases */
	private static final int EG_ATT = 4;
	private static final int EG_DEC = 3;
	private static final int EG_SUS = 2;
	private static final int EG_REL = 1;
	private static final int EG_OFF = 0;
	
	private static final int OPL_TYPE_WAVESEL  = 0x01; // waveform select
	private static final int OPL_TYPE_ADPCM    = 0x02; // DELTA-T ADPCM unit
	private static final int OPL_TYPE_KEYBOARD = 0x04; // keyboard interface
	private static final int OPL_TYPE_IO       = 0x08; // I/O port
	
	/* ---------- Generic interface section ---------- */
	public static final int OPL_TYPE_YM3526 = 0;
	public static final int OPL_TYPE_YM3812 = (OPL_TYPE_WAVESEL);
	public static final int OPL_TYPE_Y8950  = (OPL_TYPE_ADPCM|OPL_TYPE_KEYBOARD|OPL_TYPE_IO);
	
	private static final int RATE_STEPS = 8;

	//private static final int ML = 2;
	//private static final int MAX_OPL_CHIPS = 2;

	/*rate  0,    1,    2,    3,   4,   5,   6,  7,  8,  9,  10, 11, 12, 13, 14, 15 */
	/*shift 12,   11,   10,   9,   8,   7,   6,  5,  4,  3,  2,  1,  0,  0,  0,  0  */
	/*mask  4095, 2047, 1023, 511, 255, 127, 63, 31, 15, 7,  3,  1,  0,  0,  0,  0  */
	private static final int[] eg_rate_select={   /* Envelope Generator rates (16 + 64 rates + 16 RKS) */
         /* 16 infinite time rates */
	     (14*RATE_STEPS), (14*RATE_STEPS), (14*RATE_STEPS), (14*RATE_STEPS), (14*RATE_STEPS), (14*RATE_STEPS), (14*RATE_STEPS), (14*RATE_STEPS),
	     (14*RATE_STEPS), (14*RATE_STEPS), (14*RATE_STEPS), (14*RATE_STEPS), (14*RATE_STEPS), (14*RATE_STEPS), (14*RATE_STEPS), (14*RATE_STEPS),

         /* rates 00-12 */
	     ( 0*RATE_STEPS), ( 1*RATE_STEPS), ( 2*RATE_STEPS), ( 3*RATE_STEPS),
	     ( 0*RATE_STEPS), ( 1*RATE_STEPS), ( 2*RATE_STEPS), ( 3*RATE_STEPS),
	     ( 0*RATE_STEPS), ( 1*RATE_STEPS), ( 2*RATE_STEPS), ( 3*RATE_STEPS),
	     ( 0*RATE_STEPS), ( 1*RATE_STEPS), ( 2*RATE_STEPS), ( 3*RATE_STEPS),
	     ( 0*RATE_STEPS), ( 1*RATE_STEPS), ( 2*RATE_STEPS), ( 3*RATE_STEPS),
	     ( 0*RATE_STEPS), ( 1*RATE_STEPS), ( 2*RATE_STEPS), ( 3*RATE_STEPS),
	     ( 0*RATE_STEPS), ( 1*RATE_STEPS), ( 2*RATE_STEPS), ( 3*RATE_STEPS),
	     ( 0*RATE_STEPS), ( 1*RATE_STEPS), ( 2*RATE_STEPS), ( 3*RATE_STEPS),
	     ( 0*RATE_STEPS), ( 1*RATE_STEPS), ( 2*RATE_STEPS), ( 3*RATE_STEPS),
	     ( 0*RATE_STEPS), ( 1*RATE_STEPS), ( 2*RATE_STEPS), ( 3*RATE_STEPS),
	     ( 0*RATE_STEPS), ( 1*RATE_STEPS), ( 2*RATE_STEPS), ( 3*RATE_STEPS),
	     ( 0*RATE_STEPS), ( 1*RATE_STEPS), ( 2*RATE_STEPS), ( 3*RATE_STEPS),
	     ( 0*RATE_STEPS), ( 1*RATE_STEPS), ( 2*RATE_STEPS), ( 3*RATE_STEPS),

         /* rate 13 */
	     ( 4*RATE_STEPS), ( 5*RATE_STEPS), ( 6*RATE_STEPS), ( 7*RATE_STEPS),

         /* rate 14 */
	     ( 8*RATE_STEPS), ( 9*RATE_STEPS), (10*RATE_STEPS), (11*RATE_STEPS),

         /* rate 15 */
	     (12*RATE_STEPS), (12*RATE_STEPS), (12*RATE_STEPS), (12*RATE_STEPS),

         /* 16 dummy rates (same as 15 3) */
	     (12*RATE_STEPS), (12*RATE_STEPS), (12*RATE_STEPS), (12*RATE_STEPS), (12*RATE_STEPS), (12*RATE_STEPS), (12*RATE_STEPS), (12*RATE_STEPS),
	     (12*RATE_STEPS), (12*RATE_STEPS), (12*RATE_STEPS), (12*RATE_STEPS), (12*RATE_STEPS), (12*RATE_STEPS), (12*RATE_STEPS), (12*RATE_STEPS)
	};
	private static final int[] eg_rate_shift = {    /* Envelope Generator counter shifts (16 + 64 rates + 16 RKS) */
		/* 16 infinite time rates */
		0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0,

		/* rates 00-12 */
		12, 12, 12, 12, 
		11, 11, 11, 11, 
		10, 10, 10, 10, 
		 9,  9,  9,  9, 
		 8,  8,  8,  8, 
		 7,  7,  7,  7, 
		 6,  6,  6,  6, 
		 5,  5,  5,  5, 
		 4,  4,  4,  4, 
		 3,  3,  3,  3, 
		 2,  2,  2,  2, 
		 1,  1,  1,  1, 
		 0,  0,  0,  0, 

		/* rate 13 */
		 0,  0,  0,  0, 

		/* rate 14 */
		 0,  0,  0,  0, 

		/* rate 15 */
		 0,  0,  0,  0, 

		/* 16 dummy rates (same as 15 3) */
		 0,  0,  0,  0,  0,  0,  0,  0, 
		 0,  0,  0,  0,  0,  0,  0,  0
	};
//#if BUILD_Y8950
//	private static class YM_DELTAT
//	{ // AT: rearranged and tightened structure
//		private static final int YM_DELTAT_SHIFT    = 16;
//
//		private static final int YM_DELTAT_DELTA_MAX = 24576;
//		private static final int YM_DELTAT_DELTA_MIN = 127;
//		private static final int YM_DELTAT_DELTA_DEF = 127;
//
//		private static final int YM_DELTAT_DECODE_RANGE = 32768;
//		private static final int YM_DELTAT_DECODE_MIN = -(YM_DELTAT_DECODE_RANGE);
//		private static final int YM_DELTAT_DECODE_MAX = (YM_DELTAT_DECODE_RANGE)-1;
//
//		private static final int EMULATION_MODE_NORMAL = 0;
//		private static final int EMULATION_MODE_YM2610 = 1;
//		
//		/* Forecast to next Forecast (rate = *8) */
//		/* 1/8 , 3/8 , 5/8 , 7/8 , 9/8 , 11/8 , 13/8 , 15/8 */
//		private static final int[] ym_deltat_decode_tableB1 =
//		{
//			1,   3,   5,   7,   9,  11,  13,  15,
//			-1,  -3,  -5,  -7,  -9, -11, -13, -15,
//		};
//		/* delta to next delta (rate= *64) */
//		/* 0.9 , 0.9 , 0.9 , 0.9 , 1.2 , 1.6 , 2.0 , 2.4 */
//		private static final int[] ym_deltat_decode_tableB2 =
//		{
//			57,  57,  57,  57, 77, 102, 128, 153,
//			57,  57,  57,  57, 77, 102, 128, 153
//		};
//		/* 0-DRAM x1, 1-ROM, 2-DRAM x8, 3-ROM (3 is bad setting - not allowed by the manual) */
//		private static final int[] dram_rightshift = {3,0,0,0};
//		
//		private int[] output_pointer;/* pointer of output pointers   */
//		private int output_pointer_pan; /* pan : &output_pointer[pan]   */
//		private double freqbase;
//		private int memory_size = 0x4000;
//		private int output_range;
//		private int now_addr; // current address
//		private int now_step; // correct step
//		private int step; // step
//		private int start; // start address
//		private int limit; // limit address
//		private int end; // end address
//		private int delta; // delta scale
//		private int volume; // current volume
//		private int acc; // shift Measurement value
//		private int adpcmd; // next Forecast
//		private int adpcml; // current value
//		private int prev_acc; // leveling value
//		private int now_data; // current rom data
//		private int CPU_data; // current data from reg 08
//		private int portstate; // port status
//		private int control2; // control reg: SAMPLE, DA/AD, RAM TYPE (x8bit / x1bit), ROM/RAM
//		private int portshift; /* address bits shift-left:
//		                        ** 8 for YM2610,
//		                        ** 5 for Y8950 and YM2608 */
//
//		private int DRAMportshift; /* address bits shift-right:
//		                        ** 0 for ROM and x8bit DRAMs,
//		                        ** 3 for x1 DRAMs */
//
//		private int memread; // needed for reading/writing external memory
//
//		/* handlers and parameters for the status flags support */
//		private STATUS_CHANGE_HANDLER status_change_handler;
//
//		/* note that different chips have these flags on different
//		** bits of the status register
//		*/
//		private int status_change_EOS_bit; // 1 on End Of Sample (record/playback/cycle time of AD/DA converting has passed)
//		private int status_change_BRDY_bit; // 1 after recording 2 datas (2x4bits) or after reading/writing 1 data
////		private int status_change_ZERO_bit; // 1 if silence lasts for more than 290 milliseconds on ADPCM recording
//
//		/* neither Y8950 nor YM2608 can generate IRQ when PCMBSY bit changes, so instead of above,
//		** the statusflag gets ORed with PCM_BSY (below) (on each read of statusflag of Y8950 and YM2608)
//		*/
//		private int PCM_BSY; // 1 when ADPCM is playing; Y8950/YM2608 only
//
//		private int[] reg = new int[16]; // adpcm registers
//		private int emulation_mode; // which chip we're emulating
//		
//		// ROM Emulation
//		private final byte[] ROM = new byte[memory_size];
//		private int read_byte(final int offset)
//		{
//			return ((int)ROM[offset])&0xFF;
//		}
//		private void write_byte(final int offset, final int value)
//		{
//			ROM[offset] = (byte)(value&0xFF);
//		}
//
//		private int ADPCM_Read() 
//		{
//			int v = 0;
//
//			/* external memory read */
//			if ((portstate & 0xe0) == 0x20)
//			{
//				/* two dummy reads */
//				if (memread!=0)
//				{
//					now_addr = start << 1;
//					memread--;
//					return 0;
//				}
//
//				if (now_addr != (end << 1))
//				{
//					v = read_byte(now_addr>>1);
//					now_addr += 2; /* two nibbles at a time */
//
//					/* reset BRDY bit in status register, which means we are reading the memory now */
//					if (status_change_handler!=null && status_change_BRDY_bit!=0)
//						(status_change_handler).STATUS_RESET(status_change_BRDY_bit);
//
//					/* setup a timer that will callback us in 10 master clock cycles for Y8950
//					* in the callback set the BRDY flag to 1 , which means we have another data ready.
//					* For now, we don't really do this; we simply reset and set the flag in zero time, so that the IRQ will work.
//					*/
//					/* set BRDY bit in status register */
//					if (status_change_handler!=null && status_change_BRDY_bit!=0)
//						(status_change_handler).STATUS_SET(status_change_BRDY_bit);
//				}
//				else
//				{
//					/* set EOS bit in status register */
//					if (status_change_handler!=null && status_change_EOS_bit!=0)
//						(status_change_handler).STATUS_SET(status_change_EOS_bit);
//				}
//			}
//
//			return v;
//		}
//		private void ADPCM_Write(int r, int v)
//		{
//			if (r >= 0x10) return;
//			reg[r] = v; /* stock data */
//
//			switch (r)
//			{
//			case 0x00:
//		/*
//		START:
//		    Accessing *external* memory is started when START bit (D7) is set to "1", so
//		    you must set all conditions needed for recording/playback before starting.
//		    If you access *CPU-managed* memory, recording/playback starts after
//		    read/write of ADPCM data register $08.
//
//		REC:
//		    0 = ADPCM synthesis (playback)
//		    1 = ADPCM analysis (record)
//
//		MEMDATA:
//		    0 = processor (*CPU-managed*) memory (means: using register $08)
//		    1 = external memory (using start/end/limit registers to access memory: RAM or ROM)
//
//
//		SPOFF:
//		    controls output pin that should disable the speaker while ADPCM analysis
//
//		RESET and REPEAT only work with external memory.
//
//
//		some examples:
//		value:   START, REC, MEMDAT, REPEAT, SPOFF, x,x,RESET   meaning:
//		  C8     1      1    0       0       1      0 0 0       Analysis (recording) from AUDIO to CPU (to reg $08), sample rate in PRESCALER register
//		  E8     1      1    1       0       1      0 0 0       Analysis (recording) from AUDIO to EXT.MEMORY,       sample rate in PRESCALER register
//		  80     1      0    0       0       0      0 0 0       Synthesis (playing) from CPU (from reg $08) to AUDIO,sample rate in DELTA-N register
//		  a0     1      0    1       0       0      0 0 0       Synthesis (playing) from EXT.MEMORY to AUDIO,        sample rate in DELTA-N register
//
//		  60     0      1    1       0       0      0 0 0       External memory write via ADPCM data register $08
//		  20     0      0    1       0       0      0 0 0       External memory read via ADPCM data register $08
//
//		*/
//				/* handle emulation mode */
//				if (emulation_mode == EMULATION_MODE_YM2610)
//				{
//					v |= 0x20;      /*  YM2610 always uses external memory and doesn't even have memory flag bit. */
//					v &= ~0x40;     /*  YM2610 has no rec bit */
//				}
//
//				portstate = v & (0x80|0x40|0x20|0x10|0x01); /* start, rec, memory mode, repeat flag copy, reset(bit0) */
//
//				if ((portstate & 0x80)!=0)/* START,REC,MEMDATA,REPEAT,SPOFF,--,--,RESET */
//				{
//					/* set PCM BUSY bit */
//					PCM_BSY = 1;
//
//					/* start ADPCM */
//					now_step = 0;
//					acc      = 0;
//					prev_acc = 0;
//					adpcml   = 0;
//					adpcmd   = YM_DELTAT_DELTA_DEF;
//					now_data = 0;
//
//				}
//
//				if ((portstate & 0x20)!=0) /* do we access external memory? */
//				{
//					now_addr = start << 1;
//					memread = 2;    /* two dummy reads needed before accesing external memory via register $08*/
//				}
//				else    /* we access CPU memory (ADPCM data register $08) so we only reset now_addr here */
//				{
//					now_addr = 0;
//				}
//
//				if ((portstate & 0x01)!=0)
//				{
//					portstate = 0x00;
//
//					/* clear PCM BUSY bit (in status register) */
//					PCM_BSY = 0;
//
//					/* set BRDY flag */
//					if (status_change_handler!=null && status_change_BRDY_bit!=0)
//						(status_change_handler).STATUS_SET(status_change_BRDY_bit);
//				}
//				break;
//
//			case 0x01:  /* L,R,-,-,SAMPLE,DA/AD,RAMTYPE,ROM */
//				/* handle emulation mode */
//				if (emulation_mode == EMULATION_MODE_YM2610)
//				{
//					v |= 0x01;      /*  YM2610 always uses ROM as an external memory and doesn't have ROM/RAM memory flag bit. */
//				}
//
//				output_pointer_pan = (v >> 6) & 0x03;
//				if ((control2 & 3) != (v & 3))
//				{
//					/*0-DRAM x1, 1-ROM, 2-DRAM x8, 3-ROM (3 is bad setting - not allowed by the manual) */
//					if (DRAMportshift != dram_rightshift[v & 3])
//					{
//						DRAMportshift = dram_rightshift[v & 3];
//
//						/* final shift value depends on chip type and memory type selected:
//						        8 for YM2610 (ROM only),
//						        5 for ROM for Y8950 and YM2608,
//						        5 for x8bit DRAMs for Y8950 and YM2608,
//						        2 for x1bit DRAMs for Y8950 and YM2608.
//						*/
//
//						/* refresh addresses */
//						start  = (reg[0x3] * 0x0100 | reg[0x2]) << (portshift - DRAMportshift);
//						end    = (reg[0x5] * 0x0100 | reg[0x4]) << (portshift - DRAMportshift);
//						end   += (1 << (portshift - DRAMportshift)) - 1;
//						limit  = (reg[0xd]*0x0100 | reg[0xc]) << (portshift - DRAMportshift);
//					}
//				}
//				control2 = v;
//				break;
//
//			case 0x02:  /* Start Address L */
//			case 0x03:  /* Start Address H */
//				start  = (reg[0x3] * 0x0100 | reg[0x2]) << (portshift - DRAMportshift);
//				break;
//
//			case 0x04:  /* Stop Address L */
//			case 0x05:  /* Stop Address H */
//				end    = (reg[0x5]*0x0100 | reg[0x4]) << (portshift - DRAMportshift);
//				end   += (1 << (portshift - DRAMportshift)) - 1;
//				break;
//
//			case 0x06:  /* Prescale L (ADPCM and Record frq) */
//			case 0x07:  /* Prescale H */
//				break;
//
//			case 0x08:  /* ADPCM data */
//		/*
//		some examples:
//		value:   START, REC, MEMDAT, REPEAT, SPOFF, x,x,RESET   meaning:
//		  C8     1      1    0       0       1      0 0 0       Analysis (recording) from AUDIO to CPU (to reg $08), sample rate in PRESCALER register
//		  E8     1      1    1       0       1      0 0 0       Analysis (recording) from AUDIO to EXT.MEMORY,       sample rate in PRESCALER register
//		  80     1      0    0       0       0      0 0 0       Synthesis (playing) from CPU (from reg $08) to AUDIO,sample rate in DELTA-N register
//		  A0     1      0    1       0       0      0 0 0       Synthesis (playing) from EXT.MEMORY to AUDIO,        sample rate in DELTA-N register
//
//		  60     0      1    1       0       0      0 0 0       External memory write via ADPCM data register $08
//		  20     0      0    1       0       0      0 0 0       External memory read via ADPCM data register $08
//
//		*/
//
//				/* external memory write */
//				if ((portstate & 0xe0) == 0x60)
//				{
//					if (memread!=0)
//					{
//						now_addr = start << 1;
//						memread = 0;
//					}
//
//					if (now_addr != (end << 1))
//					{
//						write_byte(now_addr >> 1, v);
//						now_addr += 2; /* two nibbles at a time */
//
//						/* reset BRDY bit in status register, which means we are processing the write */
//						if (status_change_handler!=null && status_change_BRDY_bit!=0)
//							(status_change_handler).STATUS_RESET(status_change_BRDY_bit);
//
//						/* setup a timer that will callback us in 10 master clock cycles for Y8950
//						* in the callback set the BRDY flag to 1 , which means we have written the data.
//						* For now, we don't really do this; we simply reset and set the flag in zero time, so that the IRQ will work.
//						*/
//						/* set BRDY bit in status register */
//						if (status_change_handler!=null && status_change_BRDY_bit!=0)
//							(status_change_handler).STATUS_SET(status_change_BRDY_bit);
//
//					}
//					else
//					{
//						/* set EOS bit in status register */
//						if (status_change_handler!=null && status_change_EOS_bit!=0)
//							(status_change_handler).STATUS_SET(status_change_EOS_bit);
//					}
//
//					return;
//				}
//
//				/* ADPCM synthesis from CPU */
//				if ((portstate & 0xe0) == 0x80)
//				{
//					CPU_data = v;
//
//					/* Reset BRDY bit in status register, which means we are full of data */
//					if (status_change_handler!=null && status_change_BRDY_bit!=0)
//						(status_change_handler).STATUS_RESET(status_change_BRDY_bit);
//					return;
//				}
//
//				break;
//
//			case 0x09:  /* DELTA-N L (ADPCM Playback Prescaler) */
//			case 0x0a:  /* DELTA-N H */
//				delta  = (reg[0xa] * 0x0100 | reg[0x9]);
//				step     = (int)((double)(delta /* *(1<<(YM_DELTAT_SHIFT-16)) */) * freqbase);
//				break;
//
//			case 0x0b:  /* Output level control (volume, linear) */
//				{
//					final int oldvol = volume;
//					volume = (v & 0xff) * (output_range / 256) / YM_DELTAT_DECODE_RANGE;
//		/*                              v     *     ((1<<16)>>8)        >>  15;
//		*                       thus:   v     *     (1<<8)              >>  15;
//		*                       thus: output_range must be (1 << (15+8)) at least
//		*                               v     *     ((1<<23)>>8)        >>  15;
//		*                               v     *     (1<<15)             >>  15;
//		*/
//					if (oldvol != 0)
//					{
//						adpcml = (int)((double)adpcml / (double)oldvol * (double)volume);
//					}
//				}
//				break;
//
//			case 0x0c:  /* Limit Address L */
//			case 0x0d:  /* Limit Address H */
//				limit  = (reg[0xd] * 0x0100 | reg[0xc]) << (portshift - DRAMportshift);
//				break;
//			}
//		}
//
//		private void ADPCM_Reset(int panidx, int mode)
//		{
//			now_addr  = 0;
//			now_step  = 0;
//			step      = 0;
//			start     = 0;
//			end       = 0;
//			limit     = ~0; /* this way YM2610 and Y8950 (both of which don't have limit address reg) will still work */
//			volume    = 0;
//			output_pointer_pan = panidx;
//			acc       = 0;
//			prev_acc  = 0;
//			adpcmd    = 127;
//			adpcml    = 0;
//			emulation_mode = mode;
//			portstate = (emulation_mode == EMULATION_MODE_YM2610) ? 0x20 : 0;
//			control2  = (emulation_mode == EMULATION_MODE_YM2610) ? 0x01 : 0; /* default setting depends on the emulation mode. MSX demo called "facdemo_4" doesn't setup control2 register at all and still works */
//			DRAMportshift = dram_rightshift[control2 & 3];
//
//			/* The flag mask register disables the BRDY after the reset, however
//			** as soon as the mask is enabled the flag needs to be set. */
//
//			/* set BRDY bit in status register */
//			if (status_change_handler!=null && status_change_BRDY_bit!=0)
//				(status_change_handler).STATUS_SET(status_change_BRDY_bit);
//		}
////		private void postload(int[] regs)
////		{
////			/* to keep adpcml */
////			volume = 0;
////			/* update */
////			for (int r = 1; r < 16; r++)
////				ADPCM_Write(r, regs[r]);
////			reg[0] = regs[0];
////
////			/* current rom data */
////			now_data = read_byte(now_addr >> 1);
////		}
//		private void ADPCM_CALC()
//		{
//		/*
//		some examples:
//		value:   START, REC, MEMDAT, REPEAT, SPOFF, x,x,RESET   meaning:
//		  80     1      0    0       0       0      0 0 0       Synthesis (playing) from CPU (from reg $08) to AUDIO,sample rate in DELTA-N register
//		  a0     1      0    1       0       0      0 0 0       Synthesis (playing) from EXT.MEMORY to AUDIO,        sample rate in DELTA-N register
//		  C8     1      1    0       0       1      0 0 0       Analysis (recording) from AUDIO to CPU (to reg $08), sample rate in PRESCALER register
//		  E8     1      1    1       0       1      0 0 0       Analysis (recording) from AUDIO to EXT.MEMORY,       sample rate in PRESCALER register
//
//		  60     0      1    1       0       0      0 0 0       External memory write via ADPCM data register $08
//		  20     0      0    1       0       0      0 0 0       External memory read via ADPCM data register $08
//
//		*/
//
//			if ( (portstate & 0xe0)==0xa0 )
//			{
//				YM_DELTAT_synthesis_from_external_memory(this);
//			}
//			else
//			if ( (portstate & 0xe0)==0x80 )
//			{
//				/* ADPCM synthesis from CPU-managed memory (from reg $08) */
//				YM_DELTAT_synthesis_from_CPU_memory(this);    /* change output based on data in ADPCM data reg ($08) */
//			}
//			//todo: ADPCM analysis
//			//  if ( (portstate & 0xe0)==0xc0 )
//			//  if ( (portstate & 0xe0)==0xe0 )
//		}
//		private static int YM_DELTAT_Limit(final int val, final int max, final int min)
//		{
//			if ( val > max ) return max;
//			else if ( val < min ) return min;
//			return val;
//		}
//		private static void YM_DELTAT_synthesis_from_external_memory(YM_DELTAT DELTAT)
//		{
//			int step;
//			int data;
//
//			DELTAT.now_step += DELTAT.step;
//			if ( DELTAT.now_step >= (1<<YM_DELTAT_SHIFT) )
//			{
//				step = DELTAT.now_step >> YM_DELTAT_SHIFT;
//				DELTAT.now_step &= (1<<YM_DELTAT_SHIFT)-1;
//				do{
//					if ( DELTAT.now_addr == (DELTAT.limit<<1) )
//						DELTAT.now_addr = 0;
//
//					if ( DELTAT.now_addr == (DELTAT.end<<1) ) {   /* 12-06-2001 JB: corrected comparison. Was > instead of == */
//						if( (DELTAT.portstate&0x10)!=0 )
//						{
//							/* repeat start */
//							DELTAT.now_addr = DELTAT.start<<1;
//							DELTAT.acc      = 0;
//							DELTAT.adpcmd   = YM_DELTAT_DELTA_DEF;
//							DELTAT.prev_acc = 0;
//						}
//						else
//						{
//							/* set EOS bit in status register */
//							if(DELTAT.status_change_handler!=null && DELTAT.status_change_EOS_bit!=0)
//								(DELTAT.status_change_handler).STATUS_SET(DELTAT.status_change_EOS_bit);
//
//							/* clear PCM BUSY bit (reflected in status register) */
//							DELTAT.PCM_BSY = 0;
//
//							DELTAT.portstate = 0;
//							DELTAT.adpcml = 0;
//							DELTAT.prev_acc = 0;
//							return;
//						}
//					}
//
//					if( (DELTAT.now_addr&1)!=0 ) data = DELTAT.now_data & 0x0f;
//					else
//					{
//						DELTAT.now_data = DELTAT.read_byte(DELTAT.now_addr>>1);
//						data = DELTAT.now_data >> 4;
//					}
//
//					DELTAT.now_addr++;
//					/* 12-06-2001 JB: */
//					/* YM2610 address register is 24 bits wide.*/
//					/* The "+1" is there because we use 1 bit more for nibble calculations.*/
//					/* WARNING: */
//					/* Side effect: we should take the size of the mapped ROM into account */
//					DELTAT.now_addr &= ( (1<<(24+1))-1);
//
//					/* store accumulator value */
//					DELTAT.prev_acc = DELTAT.acc;
//
//					/* Forecast to next Forecast */
//					DELTAT.acc += (ym_deltat_decode_tableB1[data] * DELTAT.adpcmd / 8);
//					YM_DELTAT_Limit(DELTAT.acc,YM_DELTAT_DECODE_MAX, YM_DELTAT_DECODE_MIN);
//
//					/* delta to next delta */
//					DELTAT.adpcmd = (DELTAT.adpcmd * ym_deltat_decode_tableB2[data] ) / 64;
//					YM_DELTAT_Limit(DELTAT.adpcmd,YM_DELTAT_DELTA_MAX, YM_DELTAT_DELTA_MIN );
//
//					/* ElSemi: Fix interpolator. */
//					//DELTAT.prev_acc = prev_acc + ((DELTAT.acc - prev_acc) / 2 );
//
//				}while((--step)>0);
//
//			}
//
//			/* ElSemi: Fix interpolator. */
//			DELTAT.adpcml = DELTAT.prev_acc * (int)((1<<YM_DELTAT_SHIFT)-DELTAT.now_step);
//			DELTAT.adpcml += (DELTAT.acc * (int)DELTAT.now_step);
//			DELTAT.adpcml = (DELTAT.adpcml>>YM_DELTAT_SHIFT) * (int)DELTAT.volume;
//
//			/* output for work of output channels (outd[OPNxxxx])*/
//			DELTAT.output_pointer[DELTAT.output_pointer_pan] += DELTAT.adpcml;
//		}
//		private static void YM_DELTAT_synthesis_from_CPU_memory(YM_DELTAT DELTAT)
//		{
//			int step;
//			int data;
//
//			DELTAT.now_step += DELTAT.step;
//			if ( DELTAT.now_step >= (1<<YM_DELTAT_SHIFT) )
//			{
//				step = DELTAT.now_step >> YM_DELTAT_SHIFT;
//				DELTAT.now_step &= (1<<YM_DELTAT_SHIFT)-1;
//				do{
//					if( (DELTAT.now_addr&1)!=0 )
//					{
//						data = DELTAT.now_data & 0x0f;
//
//						DELTAT.now_data = DELTAT.CPU_data;
//
//						/* after we used CPU_data, we set BRDY bit in status register,
//						* which means we are ready to accept another byte of data */
//						if(DELTAT.status_change_handler!=null && DELTAT.status_change_BRDY_bit!=0)
//							(DELTAT.status_change_handler).STATUS_SET(DELTAT.status_change_BRDY_bit);
//					}
//					else
//					{
//						data = DELTAT.now_data >> 4;
//					}
//
//					DELTAT.now_addr++;
//
//					/* store accumulator value */
//					DELTAT.prev_acc = DELTAT.acc;
//
//					/* Forecast to next Forecast */
//					DELTAT.acc += (ym_deltat_decode_tableB1[data] * DELTAT.adpcmd / 8);
//					YM_DELTAT_Limit(DELTAT.acc,YM_DELTAT_DECODE_MAX, YM_DELTAT_DECODE_MIN);
//
//					/* delta to next delta */
//					DELTAT.adpcmd = (DELTAT.adpcmd * ym_deltat_decode_tableB2[data] ) / 64;
//					YM_DELTAT_Limit(DELTAT.adpcmd,YM_DELTAT_DELTA_MAX, YM_DELTAT_DELTA_MIN );
//
//
//				}while((--step)>0);
//
//			}
//
//			/* ElSemi: Fix interpolator. */
//			DELTAT.adpcml = DELTAT.prev_acc * (int)((1<<YM_DELTAT_SHIFT)-DELTAT.now_step);
//			DELTAT.adpcml += (DELTAT.acc * (int)DELTAT.now_step);
//			DELTAT.adpcml = (DELTAT.adpcml>>YM_DELTAT_SHIFT) * (int)DELTAT.volume;
//
//			/* output for work of output channels (outd[OPNxxxx])*/
//			DELTAT.output_pointer[DELTAT.output_pointer_pan] += DELTAT.adpcml;
//		}
//	}
//#endif
	private static class OPL_SLOT
	{
		private int ar;  // attack rate: AR<<2
		private int dr;  // decay rate:  DR<<2
		private int rr;  // release rate:RR<<2
		private int KSR;  // key scale rate
		private int ksl;  // keyscale level
		private int ksr;  // key scale rate: kcode>>>KSR
		private int mul;  // multiple: mul_tab[ML]

		/* Phase Generator */
		private int Cnt;  // frequency counter
		private int Incr;  // frequency counter step
		private int FB;  // feedback shift value
		private int[] connect1; // slot1 output pointer
		private int[] op1_out = new int[2]; // slot1 output for feedback
		private int CON;  // connection (algorithm) type

		/* Envelope Generator */
		private int eg_type;  // percussive/non-percussive mode
		private int state;  // phase type
		private int TL;  // total level: TL << 2
		private int TLL;  // adjusted now TL
		private int volume;  // envelope counter
		private int sl;  // sustain level: sl_tab[SL]
		private int eg_sh_ar;  // (attack state)
		private int eg_sel_ar;  // (attack state)
		private int eg_sh_dr;  // (decay state)
		private int eg_sel_dr;  // (decay state)
		private int eg_sh_rr;  // (release state)
		private int eg_sel_rr;  // (release state)
		private int key;  // 0 = KEY OFF, >0 = KEY ON

		/* LFO */
		private int AMmask;  // LFO Amplitude Modulation enable mask
		private int vib;  // LFO Phase Modulation enable flag (active high)

		/* waveform select */
		private int wavetable;

		private void KEYON(int key_set)
		{
			if( key==0 )
			{
				/* restart Phase Generator */
				Cnt = 0;
				/* phase -> Attack */
				state = EG_ATT;
			}
			key |= key_set;
		}

		private void KEYOFF(int key_clr)
		{
			if( key!=0 )
			{
				key &= key_clr;

				if( key==0 )
				{
					/* phase -> Release */
					if (state>EG_REL)
						state = EG_REL;
				}
			}
		}
	}
	private static class OPL_CH
	{
		private OPL_SLOT[] SLOT = new OPL_SLOT[2];
		/* phase generator state */
		private int block_fnum; /* block+fnum                   */
		private int fc;         /* Freq. Increment base         */
		private int ksl_base;   /* KeyScaleLevel Base step      */
		private int kcode;      /* key code (for key scaling)   */


		/* update phase increment counter of operator (also update the EG rates if necessary) */
		private void CALC_FCSLOT(OPL_SLOT SLOT)
		{
			/* (frequency) phase increment counter */
			SLOT.Incr = fc * SLOT.mul;
			final int ksr = kcode >>> SLOT.KSR;

			if( SLOT.ksr != ksr )
			{
				SLOT.ksr = ksr;

				/* calculate envelope generator rates */
				if ((SLOT.ar + SLOT.ksr) < 16+62)
				{
					SLOT.eg_sh_ar  = eg_rate_shift [SLOT.ar + SLOT.ksr ];
					SLOT.eg_sel_ar = eg_rate_select[SLOT.ar + SLOT.ksr ];
				}
				else
				{
					SLOT.eg_sh_ar  = 0;
					SLOT.eg_sel_ar = 13*RATE_STEPS;
				}
				SLOT.eg_sh_dr  = eg_rate_shift [SLOT.dr + SLOT.ksr ];
				SLOT.eg_sel_dr = eg_rate_select[SLOT.dr + SLOT.ksr ];
				SLOT.eg_sh_rr  = eg_rate_shift [SLOT.rr + SLOT.ksr ];
				SLOT.eg_sel_rr = eg_rate_select[SLOT.rr + SLOT.ksr ];
			}
		}

		/* CSM Key Control */
		void CSMKeyControll()
		{
			SLOT[SLOT1].KEYON(4);
			SLOT[SLOT2].KEYON(4);

			/* The key off should happen exactly one sample later - not implemented correctly yet */
			SLOT[SLOT1].KEYOFF(~4);
			SLOT[SLOT2].KEYOFF(~4);
		}
	}
	/* OPL state */
	public static class FM_OPL
//#if BUILD_Y8950
//			implements STATUS_CHANGE_HANDLER
//#endif
	{
		/* key scale level */
		/* table is 3dB/octave , DV converts this into 6dB/octave */
		/* 0.1875 is bit 0 weight of the envelope counter (volume) expressed in the 'decibel' scale */
		private static double DV = 0.1875d / 2.0d;
		private static final double[] ksl_tab = 
		{
		/* OCT 0 */
			0.000/DV, 0.000/DV, 0.000/DV, 0.000/DV,
			0.000/DV, 0.000/DV, 0.000/DV, 0.000/DV,
			0.000/DV, 0.000/DV, 0.000/DV, 0.000/DV,
			0.000/DV, 0.000/DV, 0.000/DV, 0.000/DV,
		/* OCT 1 */
			0.000/DV, 0.000/DV, 0.000/DV, 0.000/DV,
			0.000/DV, 0.000/DV, 0.000/DV, 0.000/DV,
			0.000/DV, 0.750/DV, 1.125/DV, 1.500/DV,
			1.875/DV, 2.250/DV, 2.625/DV, 3.000/DV,
		/* OCT 2 */
			0.000/DV, 0.000/DV, 0.000/DV, 0.000/DV,
			0.000/DV, 1.125/DV, 1.875/DV, 2.625/DV,
			3.000/DV, 3.750/DV, 4.125/DV, 4.500/DV,
			4.875/DV, 5.250/DV, 5.625/DV, 6.000/DV,
		/* OCT 3 */
			0.000/DV, 0.000/DV, 0.000/DV, 1.875/DV,
			3.000/DV, 4.125/DV, 4.875/DV, 5.625/DV,
			6.000/DV, 6.750/DV, 7.125/DV, 7.500/DV,
			7.875/DV, 8.250/DV, 8.625/DV, 9.000/DV,
		/* OCT 4 */
			0.000/DV, 0.000/DV, 3.000/DV, 4.875/DV,
			6.000/DV, 7.125/DV, 7.875/DV, 8.625/DV,
			9.000/DV, 9.750/DV,10.125/DV,10.500/DV,
			10.875/DV,11.250/DV,11.625/DV,12.000/DV,
		/* OCT 5 */
			0.000/DV, 3.000/DV, 6.000/DV, 7.875/DV,
			9.000/DV,10.125/DV,10.875/DV,11.625/DV,
			12.000/DV,12.750/DV,13.125/DV,13.500/DV,
			13.875/DV,14.250/DV,14.625/DV,15.000/DV,
		/* OCT 6 */
			0.000/DV, 6.000/DV, 9.000/DV,10.875/DV,
			12.000/DV,13.125/DV,13.875/DV,14.625/DV,
			15.000/DV,15.750/DV,16.125/DV,16.500/DV,
			16.875/DV,17.250/DV,17.625/DV,18.000/DV,
		/* OCT 7 */
			0.000/DV, 9.000/DV,12.000/DV,13.875/DV,
			15.000/DV,16.125/DV,16.875/DV,17.625/DV,
			18.000/DV,18.750/DV,19.125/DV,19.500/DV,
			19.875/DV,20.250/DV,20.625/DV,21.000/DV
		};
		/* 0 / 3.0 / 1.5 / 6.0 dB/OCT */
		private static int[] ksl_shift = { 31, 1, 2, 0 };

		/* sustain level table (3dB per step) */
		/* 0 - 15: 0, 3, 6, 9,12,15,18,21,24,27,30,33,36,39,42,93 (dB)*/
		private static final double SC = (2.0 / ENV_STEP);
		private static final int[] sl_tab =
		{
		    (int)(0 * SC), (int)(1 * SC), (int)(2 * SC), (int)(3 * SC), (int)(4 * SC), (int)(5 * SC), (int)(6 * SC), (int)(7 * SC), 
		    (int)(8 * SC), (int)(9 * SC), (int)(10* SC), (int)(11* SC), (int)(12* SC), (int)(13* SC), (int)(14* SC), (int)(31* SC) 
		};

		private static final int[] eg_inc =
		{
			/*cycle:0 1  2 3  4 5  6 7*/
	
			/* 0 */ 0,1, 0,1, 0,1, 0,1, /* rates 00..12 0 (increment by 0 or 1) */
			/* 1 */ 0,1, 0,1, 1,1, 0,1, /* rates 00..12 1 */
			/* 2 */ 0,1, 1,1, 0,1, 1,1, /* rates 00..12 2 */
			/* 3 */ 0,1, 1,1, 1,1, 1,1, /* rates 00..12 3 */
	
			/* 4 */ 1,1, 1,1, 1,1, 1,1, /* rate 13 0 (increment by 1) */
			/* 5 */ 1,1, 1,2, 1,1, 1,2, /* rate 13 1 */
			/* 6 */ 1,2, 1,2, 1,2, 1,2, /* rate 13 2 */
			/* 7 */ 1,2, 2,2, 1,2, 2,2, /* rate 13 3 */
	
			/* 8 */ 2,2, 2,2, 2,2, 2,2, /* rate 14 0 (increment by 2) */
			/* 9 */ 2,2, 2,4, 2,2, 2,4, /* rate 14 1 */
			/*10 */ 2,4, 2,4, 2,4, 2,4, /* rate 14 2 */
			/*11 */ 2,4, 4,4, 2,4, 4,4, /* rate 14 3 */
	
			/*12 */ 4,4, 4,4, 4,4, 4,4, /* rates 15 0, 15 1, 15 2, 15 3 (increment by 4) */
			/*13 */ 8,8, 8,8, 8,8, 8,8, /* rates 15 2, 15 3 for attack */
			/*14 */ 0,0, 0,0, 0,0, 0,0, /* infinity rates for attack and decay(s) */
		};
		/* multiple table */
		private static final int ML = 2;
		private static final int[] mul_tab = 
		{
		 	ML / 2, 1 * ML, 2 * ML, 3 * ML, 4 * ML, 5 * ML, 6 * ML, 7 * ML, 
		 	8 * ML, 9 * ML, 10 * ML, 10 * ML, 12 * ML, 12 * ML, 15 * ML, 15 * ML
		};
		/* mapping of register number (offset) to slot number used by the emulator */
		private static final int[] slot_array = 
		{
		 	0, 2, 4, 1, 3, 5, -1, -1, 
		 	6, 8, 10, 7, 9, 11, -1, -1, 
		 	12, 14, 16, 13, 15, 17, -1, -1,
		 	-1, -1, -1, -1, -1, -1, -1, -1
		};
		

		/*  TL_TAB_LEN is calculated as:
		*   12 - sinus amplitude bits     (Y axis)
		*   2  - sinus sign bit           (Y axis)
		*   TL_RES_LEN - sinus resolution (X axis)
		*/
		private static int TL_TAB_LEN = 12 * 2 * TL_RES_LEN;
		private static int ENV_QUIET = TL_TAB_LEN >> 4;

		private static int LFO_AM_TAB_ELEMENTS = 210;

		private static int[] tl_tab = new int[TL_TAB_LEN];
		/* sin waveform table in 'decibel' scale */
		/* four waveforms on OPL2 type chips */
		private static int[] sin_tab = new int[SIN_LEN * 4];

		/* LFO Amplitude Modulation table (verified on real YM3812)
		   27 output levels (triangle waveform); 1 level takes one of: 192, 256 or 448 samples

		   Length: 210 elements.

		    Each of the elements has to be repeated
		    exactly 64 times (on 64 consecutive samples).
		    The whole table takes: 64 * 210 = 13440 samples.

		    When AM = 1 data is used directly
		    When AM = 0 data is divided by 4 before being used (losing precision is important)
		*/
		private static int[] lfo_am_table =
		{
			0,0,0,0,0,0,0,
			1,1,1,1,
			2,2,2,2,
			3,3,3,3,
			4,4,4,4,
			5,5,5,5,
			6,6,6,6,
			7,7,7,7,
			8,8,8,8,
			9,9,9,9,
			10,10,10,10,
			11,11,11,11,
			12,12,12,12,
			13,13,13,13,
			14,14,14,14,
			15,15,15,15,
			16,16,16,16,
			17,17,17,17,
			18,18,18,18,
			19,19,19,19,
			20,20,20,20,
			21,21,21,21,
			22,22,22,22,
			23,23,23,23,
			24,24,24,24,
			25,25,25,25,
			26,26,26,
			25,25,25,25,
			24,24,24,24,
			23,23,23,23,
			22,22,22,22,
			21,21,21,21,
			20,20,20,20,
			19,19,19,19,
			18,18,18,18,
			17,17,17,17,
			16,16,16,16,
			15,15,15,15,
			14,14,14,14,
			13,13,13,13,
			12,12,12,12,
			11,11,11,11,
			10,10,10,10,
			9,9,9,9,
			8,8,8,8,
			7,7,7,7,
			6,6,6,6,
			5,5,5,5,
			4,4,4,4,
			3,3,3,3,
			2,2,2,2,
			1,1,1,1
		};
		/* LFO Phase Modulation table (verified on real YM3812) */
		private static int[] lfo_pm_table =
		{
			/* FNUM2/FNUM = 00 0xxxxxxx (0x0000) */
			0, 0, 0, 0, 0, 0, 0, 0, /*LFO PM depth = 0*/
			0, 0, 0, 0, 0, 0, 0, 0, /*LFO PM depth = 1*/
			
			/* FNUM2/FNUM = 00 1xxxxxxx (0x0080) */
			0, 0, 0, 0, 0, 0, 0, 0, /*LFO PM depth = 0*/
			1, 0, 0, 0,-1, 0, 0, 0, /*LFO PM depth = 1*/
			
			/* FNUM2/FNUM = 01 0xxxxxxx (0x0100) */
			1, 0, 0, 0,-1, 0, 0, 0, /*LFO PM depth = 0*/
			2, 1, 0,-1,-2,-1, 0, 1, /*LFO PM depth = 1*/
			
			/* FNUM2/FNUM = 01 1xxxxxxx (0x0180) */
			1, 0, 0, 0,-1, 0, 0, 0, /*LFO PM depth = 0*/
			3, 1, 0,-1,-3,-1, 0, 1, /*LFO PM depth = 1*/
			
			/* FNUM2/FNUM = 10 0xxxxxxx (0x0200) */
			2, 1, 0,-1,-2,-1, 0, 1, /*LFO PM depth = 0*/
			4, 2, 0,-2,-4,-2, 0, 2, /*LFO PM depth = 1*/
			
			/* FNUM2/FNUM = 10 1xxxxxxx (0x0280) */
			2, 1, 0,-1,-2,-1, 0, 1, /*LFO PM depth = 0*/
			5, 2, 0,-2,-5,-2, 0, 2, /*LFO PM depth = 1*/
			
			/* FNUM2/FNUM = 11 0xxxxxxx (0x0300) */
			3, 1, 0,-1,-3,-1, 0, 1, /*LFO PM depth = 0*/
			6, 3, 0,-3,-6,-3, 0, 3, /*LFO PM depth = 1*/
			
			/* FNUM2/FNUM = 11 1xxxxxxx (0x0380) */
			3, 1, 0,-1,-3,-1, 0, 1, /*LFO PM depth = 0*/
			7, 3, 0,-3,-7,-3, 0, 3  /*LFO PM depth = 1*/
		};

		private static int num_lock = 0;

		public FM_OPL()
		{
			super();
		}

		/* FM channel slots */
		private OPL_CH[] P_CH = new OPL_CH[9];/* OPL/OPL2 chips have 9 channels*/

		private int  eg_cnt;                 /* global envelope generator counter    */
		private int  eg_timer;               /* global envelope generator counter works at frequency = chipclock/72 */
		private int  eg_timer_add;           /* step of eg_timer                     */
		private int  eg_timer_overflow;      /* envelope generator timer overflows every 1 sample (on real chip) */

		private int   rhythm;                 /* Rhythm mode                  */

		private int[] fn_tab = new int[1024]; /* fnumber->increment counter   */

		/* LFO */
		private int  LFO_AM;
		private int  LFO_PM;

		private int  lfo_am_depth;
		private int  lfo_pm_depth_range;
		private int  lfo_am_cnt;
		private int  lfo_am_inc;
		private int  lfo_pm_cnt;
		private int  lfo_pm_inc;

		private int  noise_rng;              /* 23 bit noise shift register  */
		private int  noise_p;                /* current noise 'phase'        */
		private int  noise_f;                /* current noise period         */

		private int  wavesel;                /* waveform select enable flag  */

		private int[] T = new int[2];        /* timer counters               */
		private int[] st = new int[2];       /* timer enable                 */

//	#if BUILD_Y8950
//		/* Delta-T ADPCM unit (Y8950) */
//		private YM_DELTAT deltat;
//
//		/* Keyboard and I/O ports interface */
//		private int   portDirection;
//		//private int   portLatch;
//		OPL_PORTHANDLER_R porthandler_r;
//		OPL_PORTHANDLER_W porthandler_w;
//		OPL_PORTHANDLER_R keyboardhandler_r;
//		OPL_PORTHANDLER_W keyboardhandler_w;
//	#endif

		/* external event callback handlers */
		OPL_TIMERHANDLER  timer_handler;    /* TIMER handler                */
		OPL_IRQHANDLER    IRQHandler;       /* IRQ handler                  */
		OPL_UPDATEHANDLER UpdateHandler;    /* stream update handler        */

		private int type;                    /* chip type                    */
		private int address;                 /* address register             */
		private int status;                  /* status flag                  */
		private int statusmask;              /* status mask                  */
		private int mode;                    /* Reg.08 : CSM,notesel,etc.    */

		private int clock;                   /* master clock  (Hz)           */
		private int rate;                    /* sampling rate (Hz)           */
		private double freqbase;             /* frequency base               */
		private double TimerBase;            /* Timer base time (==sampling time)*/

		private int[] phase_modulation = new int[1]; /* phase modulation input (SLOT 2) */
		private int[] output = new int[1];
//	#if BUILD_Y8950
//	    private int[] output_deltat = new int[4];    /* for Y8950 DELTA-T, chip is mono, that 4 here is just for safety */
// #endif

	    /* status set and IRQ handling */
		public void STATUS_SET(int flag)
		{
			/* set status flag */
			status |= flag;
			if((status & 0x80)==0)
			{
				if((status & statusmask)!=0)
				{   /* IRQ on */
					status |= 0x80;
					/* callback user interrupt handler (IRQ is OFF to ON) */
					if(IRQHandler!=null) (IRQHandler).invoke(1);
				}
			}
		}

		/* status reset and IRQ handling */
		public void STATUS_RESET(int flag)
		{
			/* reset status flag */
			status &=~flag;
			if((status & 0x80)!=0)
			{
				if ((status & statusmask)==0)
				{
					status &= 0x7f;
					/* callback user interrupt handler (IRQ is ON to OFF) */
					if(IRQHandler!=null) (IRQHandler).invoke(0);
				}
			}
		}

		/* IRQ mask set */
		void STATUSMASK_SET(int flag)
		{
			statusmask = flag;
			/* IRQ handling check */
			STATUS_SET(0);
			STATUS_RESET(0);
		}

		/* advance LFO to next sample */
		void advance_lfo()
		{
			/* LFO */
			lfo_am_cnt += lfo_am_inc;
			if (lfo_am_cnt >= (LFO_AM_TAB_ELEMENTS << LFO_SH))  /* lfo_am_table is 210 elements long */
				lfo_am_cnt -= (LFO_AM_TAB_ELEMENTS << LFO_SH);

			final int tmp = lfo_am_table[ lfo_am_cnt >>> LFO_SH ];

			LFO_AM = lfo_am_depth!=0 ? tmp : tmp >> 2;

			lfo_pm_cnt += lfo_pm_inc;
			LFO_PM = (lfo_pm_cnt>>>LFO_SH & 7) | lfo_pm_depth_range;
		}

		/* advance to next sample */
		void advance()
		{
			eg_timer += eg_timer_add;

			while (eg_timer >= eg_timer_overflow)
			{
				eg_timer -= eg_timer_overflow;

				eg_cnt++;

				for (int i=0; i<9*2; i++)
				{
					OPL_CH   CH  = P_CH[i/2];
					OPL_SLOT op  = CH.SLOT[i&1];

					/* Envelope Generator */
					switch(op.state)
					{
					case EG_ATT:        /* attack phase */
						if ( (eg_cnt & ((1<<op.eg_sh_ar)-1) )==0 )
						{
							op.volume += (~op.volume *
														(eg_inc[op.eg_sel_ar + ((eg_cnt>>op.eg_sh_ar)&7)])
														) >>3;

							if (op.volume <= MIN_ATT_INDEX)
							{
								op.volume = MIN_ATT_INDEX;
								op.state = EG_DEC;
							}

						}
						break;

					case EG_DEC:    /* decay phase */
						if ( (eg_cnt & ((1<<op.eg_sh_dr)-1) )==0 )
						{
							op.volume += eg_inc[op.eg_sel_dr + ((eg_cnt>>op.eg_sh_dr)&7)];

							if ( op.volume >= op.sl )
								op.state = EG_SUS;

						}
						break;

					case EG_SUS:    /* sustain phase */

						/* this is important behaviour:
						one can change percusive/non-percussive modes on the fly and
						the chip will remain in sustain phase - verified on real YM3812 */

						if(op.eg_type!=0)    /* non-percussive mode */
						{
											/* do nothing */
						}
						else                /* percussive mode */
						{
							/* during sustain phase chip adds Release Rate (in percussive mode) */
							if ( (eg_cnt & ((1<<op.eg_sh_rr)-1) )==0 )
							{
								op.volume += eg_inc[op.eg_sel_rr + ((eg_cnt>>op.eg_sh_rr)&7)];

								if ( op.volume >= MAX_ATT_INDEX )
									op.volume = MAX_ATT_INDEX;
							}
							/* else do nothing in sustain phase */
						}
						break;

					case EG_REL:    /* release phase */
						if ( (eg_cnt & ((1<<op.eg_sh_rr)-1) )==0 )
						{
							op.volume += eg_inc[op.eg_sel_rr + ((eg_cnt>>op.eg_sh_rr)&7)];

							if ( op.volume >= MAX_ATT_INDEX )
							{
								op.volume = MAX_ATT_INDEX;
								op.state = EG_OFF;
							}

						}
						break;

					default:
						break;
					}
				}
			}

			for (int i=0; i<9*2; i++)
			{
				OPL_CH   CH = P_CH[i/2];
				OPL_SLOT op = CH.SLOT[i&1];

				/* Phase Generator */
				if(op.vib!=0)
				{
					int block_fnum      = CH.block_fnum;
					final int fnum_lfo  = (block_fnum&0x0380) >> 7;

					final int lfo_fn_table_index_offset = lfo_pm_table[LFO_PM + 16*fnum_lfo ];

					if (lfo_fn_table_index_offset!=0)  /* LFO phase modulation active */
					{
						block_fnum += lfo_fn_table_index_offset;
						final int block = (block_fnum&0x1c00) >> 10;
						op.Cnt += (fn_tab[block_fnum&0x03ff] >> (7-block)) * op.mul;
					}
					else    /* LFO phase modulation  = zero */
					{
						op.Cnt += op.Incr;
					}
				}
				else    /* LFO phase modulation disabled for this operator */
				{
					op.Cnt += op.Incr;
				}
			}

			/*  The Noise Generator of the YM3812 is 23-bit shift register.
			*   Period is equal to 2^23-2 samples.
			*   Register works at sampling frequency of the chip, so output
			*   can change on every sample.
			*
			*   Output of the register and input to the bit 22 is:
			*   bit0 XOR bit14 XOR bit15 XOR bit22
			*
			*   Simply use bit 22 as the noise output.
			*/

			noise_p += noise_f;
			int i = noise_p >> FREQ_SH;        /* number of events (shifts of the shift register) */
			noise_p &= FREQ_MASK;
			while (i>0)
			{
				/*
				int j;
				j = ( (noise_rng) ^ (noise_rng>>14) ^ (noise_rng>>15) ^ (noise_rng>>22) ) & 1;
				noise_rng = (j<<22) | (noise_rng>>1);
				*/

				/*
				    Instead of doing all the logic operations above, we
				    use a trick here (and use bit 0 as the noise output).
				    The difference is only that the noise bit changes one
				    step ahead. This doesn't matter since we don't know
				    what is real state of the noise_rng after the reset.
				*/

				if ((noise_rng & 1)!=0) noise_rng ^= 0x800302;
				noise_rng >>= 1;

				i--;
			}
		}

		/* calculate output */
		void CALC_CH(OPL_CH CH)
		{
			OPL_SLOT SLOT;
			int env;
			int out;

			phase_modulation[0] = 0;

			/* SLOT 1 */
			SLOT = CH.SLOT[SLOT1];
			env  = volume_calc(SLOT);
			out  = SLOT.op1_out[0] + SLOT.op1_out[1];
			SLOT.op1_out[0]   = SLOT.op1_out[1];
			SLOT.connect1[0] += SLOT.op1_out[0];
			SLOT.op1_out[1] = 0;
			if( env < ENV_QUIET )
			{
				if (SLOT.FB==0)
					out = 0;
				SLOT.op1_out[1] = op_calc1(SLOT.Cnt, env, (out<<SLOT.FB), SLOT.wavetable );
			}

			/* SLOT 2 */
			SLOT = CH.SLOT[SLOT2];
			env = volume_calc(SLOT);
			if( env < ENV_QUIET )
				output[0] += op_calc(SLOT.Cnt, env, phase_modulation[0], SLOT.wavetable);
		}

		/*
		    operators used in the rhythm sounds generation process:

		    Envelope Generator:

		channel  operator  register number   Bass  High  Snare Tom  Top
		/ slot   number    TL ARDR SLRR Wave Drum  Hat   Drum  Tom  Cymbal
		 6 / 0   12        50  70   90   f0  +
		 6 / 1   15        53  73   93   f3  +
		 7 / 0   13        51  71   91   f1        +
		 7 / 1   16        54  74   94   f4              +
		 8 / 0   14        52  72   92   f2                    +
		 8 / 1   17        55  75   95   f5                          +

		    Phase Generator:

		channel  operator  register number   Bass  High  Snare Tom  Top
		/ slot   number    MULTIPLE          Drum  Hat   Drum  Tom  Cymbal
		 6 / 0   12        30                +
		 6 / 1   15        33                +
		 7 / 0   13        31                      +     +           +
		 7 / 1   16        34                -----  n o t  u s e d -----
		 8 / 0   14        32                                  +
		 8 / 1   17        35                      +                 +

		channel  operator  register number   Bass  High  Snare Tom  Top
		number   number    BLK/FNUM2 FNUM    Drum  Hat   Drum  Tom  Cymbal
		   6     12,15     B6        A6      +

		   7     13,16     B7        A7            +     +           +

		   8     14,17     B8        A8            +           +     +

		*/

		/* calculate rhythm */

		void CALC_RH()
		{
			final int noise = noise_rng & 1;

			OPL_SLOT SLOT;
			int out;
			int env;


			/* Bass Drum (verified on real YM3812):
			  - depends on the channel 6 'connect' register:
			      when connect = 0 it works the same as in normal (non-rhythm) mode (op1->op2->out)
			      when connect = 1 _only_ operator 2 is present on output (op2->out), operator 1 is ignored
			  - output sample always is multiplied by 2
			*/

			phase_modulation[0] = 0;
			/* SLOT 1 */
			SLOT = P_CH[6].SLOT[SLOT1];
			env = volume_calc(SLOT);

			out = SLOT.op1_out[0] + SLOT.op1_out[1];
			SLOT.op1_out[0] = SLOT.op1_out[1];

			if (SLOT.CON==0)
				phase_modulation[0] = SLOT.op1_out[0];
			/* else ignore output of operator 1 */

			SLOT.op1_out[1] = 0;
			if( env < ENV_QUIET )
			{
				if (SLOT.FB==0)
					out = 0;
				SLOT.op1_out[1] = op_calc1(SLOT.Cnt, env, (out<<SLOT.FB), SLOT.wavetable );
			}

			/* SLOT 2 */
			SLOT = P_CH[6].SLOT[SLOT2];
			env = volume_calc(SLOT);
			if( env < ENV_QUIET )
				output[0] += op_calc(SLOT.Cnt, env, phase_modulation[0], SLOT.wavetable) * 2;


			/* Phase generation is based on: */
			/* HH  (13) channel 7->slot 1 combined with channel 8->slot 2 (same combination as TOP CYMBAL but different output phases) */
			/* SD  (16) channel 7->slot 1 */
			/* TOM (14) channel 8->slot 1 */
			/* TOP (17) channel 7->slot 1 combined with channel 8->slot 2 (same combination as HIGH HAT but different output phases) */

			/* Envelope generation based on: */
			/* HH  channel 7->slot1 */
			/* SD  channel 7->slot2 */
			/* TOM channel 8->slot1 */
			/* TOP channel 8->slot2 */


			/* The following formulas can be well optimized.
			   I leave them in direct form for now (in case I've missed something).
			*/

			/* High Hat (verified on real YM3812) */
			final OPL_SLOT SLOT7_1 = P_CH[7].SLOT[SLOT1];
			final OPL_SLOT SLOT8_2 = P_CH[8].SLOT[SLOT2];
			env = volume_calc(SLOT7_1);
			if( env < ENV_QUIET )
			{
				/* high hat phase generation:
				    phase = d0 or 234 (based on frequency only)
				    phase = 34 or 2d0 (based on noise)
				*/

				/* base frequency derived from operator 1 in channel 7 */
				final int bit7 = SLOT7_1.Cnt >>> FREQ_SH & 1<<7;
				final int bit3 = SLOT7_1.Cnt >>> FREQ_SH & 1<<3;
				final int bit2 = SLOT7_1.Cnt >>> FREQ_SH & 1<<2;

				final int res1 = (bit2 ^ bit7) | bit3;

				/* when res1 = 0 phase = 0x000 | 0xd0; */
				/* when res1 = 1 phase = 0x200 | (0xd0>>2); */
				int phase = res1!=0 ? (0x200|(0xd0>>2)) : 0xd0;

				/* enable gate based on frequency of operator 2 in channel 8 */
				final int bit5e= SLOT8_2.Cnt >>> FREQ_SH & 1<<5;
				final int bit3e= SLOT8_2.Cnt >>> FREQ_SH & 1<<3;

				final int res2 = bit3e ^ bit5e;

				/* when res2 = 0 pass the phase from calculation above (res1); */
				/* when res2 = 1 phase = 0x200 | (0xd0>>2); */
				if (res2!=0)
					phase = (0x200|(0xd0>>2));


				/* when phase & 0x200 is set and noise=1 then phase = 0x200|0xd0 */
				/* when phase & 0x200 is set and noise=0 then phase = 0x200|(0xd0>>2), ie no change */
				if ((phase&0x200)!=0)
				{
					if (noise!=0)
						phase = 0x200|0xd0;
				}
				else
				/* when phase & 0x200 is clear and noise=1 then phase = 0xd0>>2 */
				/* when phase & 0x200 is clear and noise=0 then phase = 0xd0, ie no change */
				{
					if (noise!=0)
						phase = 0xd0>>2;
				}

				output[0] += op_calc(phase<<FREQ_SH, env, 0, SLOT7_1.wavetable) * 2;
			}

			/* Snare Drum (verified on real YM3812) */
			final OPL_SLOT SLOT7_2 = P_CH[7].SLOT[SLOT2];
			env = volume_calc(SLOT7_2);
			if( env < ENV_QUIET )
			{
				/* base frequency derived from operator 1 in channel 7 */
				final int bit8 = SLOT7_1.Cnt >>> FREQ_SH & 1<<8;

				/* when bit8 = 0 phase = 0x100; */
				/* when bit8 = 1 phase = 0x200; */
				int phase = bit8!=0 ? 0x200 : 0x100;

				/* Noise bit XOR'es phase by 0x100 */
				/* when noisebit = 0 pass the phase from calculation above */
				/* when noisebit = 1 phase ^= 0x100; */
				/* in other words: phase ^= (noisebit<<8); */
				if (noise!=0)
					phase ^= 0x100;

				output[0] += op_calc(phase<<FREQ_SH, env, 0, SLOT7_2.wavetable) * 2;
			}

			/* Tom Tom (verified on real YM3812) */
			final OPL_SLOT SLOT8_1 = P_CH[8].SLOT[SLOT1];
			env = volume_calc(SLOT8_1);
			if( env < ENV_QUIET )
				output[0] += op_calc(SLOT8_1.Cnt, env, 0, SLOT8_1.wavetable) * 2;

			/* Top Cymbal (verified on real YM3812) */
			env = volume_calc(SLOT8_2);
			if( env < ENV_QUIET )
			{
				/* base frequency derived from operator 1 in channel 7 */
				final int bit7 = SLOT7_1.Cnt >>> FREQ_SH & 1<<7;
				final int bit3 = SLOT7_1.Cnt >>> FREQ_SH & 1<<3;
				final int bit2 = SLOT7_1.Cnt >>> FREQ_SH & 1<<2;

				final int res1 = (bit2 ^ bit7) | bit3;

				/* when res1 = 0 phase = 0x000 | 0x100; */
				/* when res1 = 1 phase = 0x200 | 0x100; */
				int phase = res1!=0 ? 0x300 : 0x100;

				/* enable gate based on frequency of operator 2 in channel 8 */
				final int bit5e= SLOT8_2.Cnt >>> FREQ_SH & 1<<5;
				final int bit3e= SLOT8_2.Cnt >>> FREQ_SH & 1<<3;

				final int res2 = bit3e ^ bit5e;
				/* when res2 = 0 pass the phase from calculation above (res1); */
				/* when res2 = 1 phase = 0x200 | 0x100; */
				if (res2!=0)
					phase = 0x300;

				output[0] += op_calc(phase<<FREQ_SH, env, 0, SLOT8_2.wavetable) * 2;
			}
		}

		/* initialize all tables */
		private static int init_tables()
		{
			for (int x=0; x<TL_RES_LEN; x++)
			{
				double m = Math.floor((1<<16) / Math.pow(2, (x+1) * (ENV_STEP/4.0) / 8.0));

				/* we never reach (1<<16) here due to the (x+1) */
				/* result fits within 16 bits at maximum */

				int n = (int)m;     /* 16 bits here */
				n >>= 4;        /* 12 bits here */
				if ((n&1)!=0)   /* round to nearest */
					n = (n>>1)+1;
				else
					n = n>>1;
								/* 11 bits here (rounded) */
				n <<= 1;        /* 12 bits here (as in real chip) */
				tl_tab[ x*2 + 0 ] = n;
				tl_tab[ x*2 + 1 ] = -tl_tab[ x*2 + 0 ];

				for (int i=1; i<12; i++)
				{
					tl_tab[ x*2+0 + i*2*TL_RES_LEN ] =  tl_tab[ x*2+0 ]>>i;
					tl_tab[ x*2+1 + i*2*TL_RES_LEN ] = -tl_tab[ x*2+0 + i*2*TL_RES_LEN ];
				}
			}

			for (int i=0; i<SIN_LEN; i++)
			{
				/* non-standard sinus */
				double m = Math.sin( ((i*2)+1) * Math.PI / SIN_LEN ); /* checked against the real chip */

				/* we never reach zero here due to ((i*2)+1) */
				double o;
				if (m>0.0)
					o = 8*Math.log(1.0/m)/Math.log(2.0);  /* convert to 'decibels' */
				else
					o = 8*Math.log(-1.0/m)/Math.log(2.0); /* convert to 'decibels' */

				o /= (ENV_STEP/4);

				int n = (int)(2.0*o);
				if ((n&1)!=0)                        /* round to nearest */
					n = (n>>1)+1;
				else
					n = n>>1;

				sin_tab[ i ] = n*2 + (m>=0.0? 0: 1 );
			}

			for (int i=0; i<SIN_LEN; i++)
			{
				/* waveform 1:  __      __     */
				/*             /  \____/  \____*/
				/* output only first half of the sinus waveform (positive one) */

				if ((i & (1<<(SIN_BITS-1)))!=0 )
					sin_tab[1*SIN_LEN+i] = TL_TAB_LEN;
				else
					sin_tab[1*SIN_LEN+i] = sin_tab[i];

				/* waveform 2:  __  __  __  __ */
				/*             /  \/  \/  \/  \*/
				/* abs(sin) */

				sin_tab[2*SIN_LEN+i] = sin_tab[i & (SIN_MASK>>1) ];

				/* waveform 3:  _   _   _   _  */
				/*             / |_/ |_/ |_/ |_*/
				/* abs(output only first quarter of the sinus waveform) */

				if ((i & (1<<(SIN_BITS-2)))!=0 )
					sin_tab[3*SIN_LEN+i] = TL_TAB_LEN;
				else
					sin_tab[3*SIN_LEN+i] = sin_tab[i & (SIN_MASK>>2)];

			}

			return 1;
		}
		
		/* Initialize of this chip */
		private void initialize()
		{
			/* frequency base */
			freqbase  = (rate!=0) ? ((double)clock / 72.0) / rate  : 0;
			/* Timer base time */
			TimerBase = (clock!=0) ? 1.0 / ((double)clock / 72.0) : 0;

			/* make fnumber -> increment counter table */
			for(int i=0 ; i < 1024 ; i++ )
			{
				/* opn phase increment counter = 20bit */
				fn_tab[i] = (int)( (double)i * 64 * freqbase * (1<<(FREQ_SH-10)) ); /* -10 because chip works with 10.10 fixed point, while we use 16.16 */
			}

			/* Amplitude modulation: 27 output levels (triangle waveform); 1 level takes one of: 192, 256 or 448 samples */
			/* One entry from LFO_AM_TABLE lasts for 64 samples */
			lfo_am_inc = (int)((1.0d / 64.0d ) * (double)(1<<LFO_SH) * freqbase);

			/* Vibrato: 8 output levels (triangle waveform); 1 level takes 1024 samples */
			lfo_pm_inc = (int)((1.0d / 1024.0d) * (double)(1<<LFO_SH) * freqbase);

			/* Noise generator: a step takes 1 sample */
			noise_f = (int)((1.0d / 1.0d) * (double)(1<<FREQ_SH) * freqbase);

			eg_timer_add  = (int)((double)(1<<EG_SH) * freqbase);
			eg_timer_overflow = ( 1 ) * (1<<EG_SH);
		}
		
		private void WriteReg(int r, int v)
		{
			OPL_CH CH;
			int slot;
			int block_fnum;


			/* adjust bus to 8 bits */
			r &= 0xff;
			v &= 0xff;

			switch(r&0xe0)
			{
			case 0x00:  /* 00-1f:control */
				switch(r&0x1f)
				{
				case 0x01:  /* waveform select enable */
					if((type&OPL_TYPE_WAVESEL)!=0)
					{
						wavesel = v&0x20;
						/* do not change the waveform previously selected */
					}
					break;
				case 0x02:  /* Timer 1 */
					T[0] = (256-v)*4;
					break;
				case 0x03:  /* Timer 2 */
					T[1] = (256-v)*16;
					break;
				case 0x04:  /* IRQ clear / mask and Timer enable */
					if((v&0x80)!=0)
					{   /* IRQ flag clear */
						STATUS_RESET(0x7f-0x08); /* don't reset BFRDY flag or we will have to call deltat module to set the flag */
					}
					else
					{   /* set IRQ mask ,timer enable*/
						int st1 = v&1;
						int st2 = (v>>1)&1;

						/* IRQRST,T1MSK,t2MSK,EOSMSK,BRMSK,x,ST2,ST1 */
						STATUS_RESET(v & (0x78-0x08));
						STATUSMASK_SET((~v) & 0x78);

						/* timer 2 */
						if(st[1] != st2)
						{
							st[1] = st2;
							if (timer_handler!=null) (timer_handler).invoke(1, st2!=0 ? (TimerBase * T[1]) : 0);
						}
						/* timer 1 */
						if(st[0] != st1)
						{
							st[0] = st1;
							if (timer_handler!=null) (timer_handler).invoke(0, st1!=0 ? (TimerBase * T[0]) : 0);
						}
					}
					break;
//			#if BUILD_Y8950
//				case 0x06:      /* Key Board OUT */
//					if((type&OPL_TYPE_KEYBOARD)!=0)
//					{
//						if(keyboardhandler_w!=null)
//							keyboardhandler_w.invoke(v);
//					}
//					break;
//				case 0x07:  /* DELTA-T control 1 : START,REC,MEMDATA,REPT,SPOFF,x,x,RST */
//					if((type&OPL_TYPE_ADPCM)!=0)
//						deltat.ADPCM_Write(r-0x07,v);
//					break;
//			#endif
				case 0x08:  /* MODE,DELTA-T control 2 : CSM,NOTESEL,x,x,smpl,da/ad,64k,rom */
					mode = v;
//			#if BUILD_Y8950
//					if((type&OPL_TYPE_ADPCM)!=0)
//						deltat.ADPCM_Write(r-0x07,v&0x0f); /* mask 4 LSBs in register 08 for DELTA-T unit */
//			#endif
					break;

//			#if BUILD_Y8950
//				case 0x09:      /* START ADD */
//				case 0x0a:
//				case 0x0b:      /* STOP ADD  */
//				case 0x0c:
//				case 0x0d:      /* PRESCALE   */
//				case 0x0e:
//				case 0x0f:      /* ADPCM data write */
//				case 0x10:      /* DELTA-N    */
//				case 0x11:      /* DELTA-N    */
//				case 0x12:      /* ADPCM volume */
//					if((type&OPL_TYPE_ADPCM)!=0)
//						deltat.ADPCM_Write(r-0x07,v);
//					break;
//
//				case 0x15:      /* DAC data high 8 bits (F7,F6...F2) */
//				case 0x16:      /* DAC data low 2 bits (F1, F0 in bits 7,6) */
//				case 0x17:      /* DAC data shift (S2,S1,S0 in bits 2,1,0) */
//					break;
//
//				case 0x18:      /* I/O CTRL (Direction) */
//					if((type&OPL_TYPE_IO)!=0)
//						portDirection = v&0x0f;
//					break;
//				case 0x19:      /* I/O DATA */
//					if((type&OPL_TYPE_IO)!=0)
//					{
//						//portLatch = v;
//						if(porthandler_w!=null)
//							porthandler_w.invoke(v&portDirection);
//					}
//					break;
//			#endif
				default:
					break;
				}
				break;
			case 0x20:  /* am ON, vib ON, ksr, eg_type, mul */
				slot = slot_array[r&0x1f];
				if(slot < 0) return;
				set_mul(slot,v);
				break;
			case 0x40:
				slot = slot_array[r&0x1f];
				if(slot < 0) return;
				set_ksl_tl(slot,v);
				break;
			case 0x60:
				slot = slot_array[r&0x1f];
				if(slot < 0) return;
				set_ar_dr(slot,v);
				break;
			case 0x80:
				slot = slot_array[r&0x1f];
				if(slot < 0) return;
				set_sl_rr(slot,v);
				break;
			case 0xa0:
				if (r == 0xbd)          /* am depth, vibrato depth, r,bd,sd,tom,tc,hh */
				{
					lfo_am_depth = v & 0x80;
					lfo_pm_depth_range = (v&0x40)!=0 ? 8 : 0;

					rhythm  = v&0x3f;

					if((rhythm&0x20)!=0)
					{
						/* BD key on/off */
						if((v&0x10)!=0)
						{
							P_CH[6].SLOT[SLOT1].KEYON(2);
							P_CH[6].SLOT[SLOT2].KEYON(2);
						}
						else
						{
							P_CH[6].SLOT[SLOT1].KEYOFF(~2);
							P_CH[6].SLOT[SLOT2].KEYOFF(~2);
						}
						/* HH key on/off */
						if((v&0x01)!=0) P_CH[7].SLOT[SLOT1].KEYON ( 2);
						else            P_CH[7].SLOT[SLOT1].KEYOFF(~2);
						/* SD key on/off */
						if((v&0x08)!=0) P_CH[7].SLOT[SLOT2].KEYON ( 2);
						else            P_CH[7].SLOT[SLOT2].KEYOFF(~2);
						/* TOM key on/off */
						if((v&0x04)!=0) P_CH[8].SLOT[SLOT1].KEYON ( 2);
						else            P_CH[8].SLOT[SLOT1].KEYOFF(~2);
						/* TOP-CY key on/off */
						if((v&0x02)!=0) P_CH[8].SLOT[SLOT2].KEYON ( 2);
						else            P_CH[8].SLOT[SLOT2].KEYOFF(~2);
					}
					else
					{
						/* BD key off */
						P_CH[6].SLOT[SLOT1].KEYOFF(~2);
						P_CH[6].SLOT[SLOT2].KEYOFF(~2);
						/* HH key off */
						P_CH[7].SLOT[SLOT1].KEYOFF(~2);
						/* SD key off */
						P_CH[7].SLOT[SLOT2].KEYOFF(~2);
						/* TOM key off */
						P_CH[8].SLOT[SLOT1].KEYOFF(~2);
						/* TOP-CY off */
						P_CH[8].SLOT[SLOT2].KEYOFF(~2);
					}
					return;
				}
				/* keyon,block,fnum */
				if( (r&0x0f) > 8) return;
				CH = P_CH[r&0x0f];
				if((r&0x10)==0)
				{   /* a0-a8 */
					block_fnum  = (CH.block_fnum&0x1f00) | v;
				}
				else
				{   /* b0-b8 */
					block_fnum = ((v&0x1f)<<8) | (CH.block_fnum&0xff);

					if((v&0x20)!=0)
					{
						CH.SLOT[SLOT1].KEYON ( 1);
						CH.SLOT[SLOT2].KEYON ( 1);
					}
					else
					{
						CH.SLOT[SLOT1].KEYOFF(~1);
						CH.SLOT[SLOT2].KEYOFF(~1);
					}
				}
				/* update */
				if(CH.block_fnum != block_fnum)
				{
					int block  = block_fnum >> 10;

					CH.block_fnum = block_fnum;

					CH.ksl_base = (int)(ksl_tab[block_fnum>>6]);
					CH.fc       = fn_tab[block_fnum&0x03ff] >> (7-block);

					/* BLK 2,1,0 bits -> bits 3,2,1 of kcode */
					CH.kcode    = (CH.block_fnum&0x1c00)>>9;

						/* the info below is actually opposite to what is stated in the Manuals (verifed on real YM3812) */
					/* if notesel == 0 -> lsb of kcode is bit 10 (MSB) of fnum  */
					/* if notesel == 1 -> lsb of kcode is bit 9 (MSB-1) of fnum */
					if ((mode&0x40)!=0)
						CH.kcode |= (CH.block_fnum&0x100)>>8; /* notesel == 1 */
					else
						CH.kcode |= (CH.block_fnum&0x200)>>9; /* notesel == 0 */

					/* refresh Total Level in both SLOTs of this channel */
					CH.SLOT[SLOT1].TLL = CH.SLOT[SLOT1].TL + (CH.ksl_base>>>CH.SLOT[SLOT1].ksl);
					CH.SLOT[SLOT2].TLL = CH.SLOT[SLOT2].TL + (CH.ksl_base>>>CH.SLOT[SLOT2].ksl);

					/* refresh frequency counter in both SLOTs of this channel */
					CH.CALC_FCSLOT(CH.SLOT[SLOT1]);
					CH.CALC_FCSLOT(CH.SLOT[SLOT2]);
				}
				break;
			case 0xc0:
				/* FB,C */
				if( (r&0x0f) > 8) return;
				CH = P_CH[r&0x0f];
				CH.SLOT[SLOT1].FB  = ((v>>1)&7)!=0 ? ((v>>1)&7) + 7 : 0;
				CH.SLOT[SLOT1].CON = v&1;
				CH.SLOT[SLOT1].connect1 = CH.SLOT[SLOT1].CON!=0 ? output : phase_modulation;
				break;
			case 0xe0: /* waveform select */
				/* simply ignore write to the waveform select register if selecting not enabled in test register */
				if(wavesel!=0)
				{
					slot = slot_array[r&0x1f];
					if(slot < 0) return;
					CH = P_CH[slot/2];

					CH.SLOT[slot&1].wavetable = (v&0x03)*SIN_LEN;
				}
				break;
			}
		}
		
		private void ResetChip()
		{
			eg_timer = 0;
			eg_cnt   = 0;

			noise_rng = 1; /* noise shift register */
			mode   = 0;    /* normal mode */
			STATUS_RESET(0x7f);

			/* reset with register write */
			WriteReg(0x01,0); /* wavesel disable */
			WriteReg(0x02,0); /* Timer1 */
			WriteReg(0x03,0); /* Timer2 */
			WriteReg(0x04,0); /* IRQ mask clear */
			for(int i = 0xff ; i >= 0x20 ; i-- ) WriteReg(i,0);

			/* reset operator parameters */
			for(OPL_CH CH : P_CH)
			{
				for(OPL_SLOT SLOT : CH.SLOT)
				{
					/* wave table */
					SLOT.wavetable = 0;
					SLOT.state     = EG_OFF;
					SLOT.volume    = MAX_ATT_INDEX;
				}
			}
//		#if BUILD_Y8950
//			if((type&OPL_TYPE_ADPCM)!=0)
//			{
//				deltat.freqbase = freqbase;
//				deltat.output_pointer = output_deltat;
//				deltat.portshift = 5;
//				deltat.output_range = 1<<23;
//				deltat.ADPCM_Reset(0,YM_DELTAT.EMULATION_MODE_NORMAL);
//			}
//		#endif
		}

		private void postload()
		{
			for(OPL_CH CH : P_CH)
			{
				/* Look up key scale level */
				final int block_fnum = CH.block_fnum;
				CH.ksl_base = (int)(ksl_tab[block_fnum >> 6]);
				CH.fc       = fn_tab[block_fnum & 0x03ff] >> (7 - (block_fnum >> 10));

				for(OPL_SLOT SLOT : CH.SLOT)
				{
					/* Calculate key scale rate */
					SLOT.ksr = CH.kcode >>> SLOT.KSR;

					/* Calculate attack, decay and release rates */
					if ((SLOT.ar + SLOT.ksr) < 16+62)
					{
						SLOT.eg_sh_ar  = eg_rate_shift [SLOT.ar + SLOT.ksr ];
						SLOT.eg_sel_ar = eg_rate_select[SLOT.ar + SLOT.ksr ];
					}
					else
					{
						SLOT.eg_sh_ar  = 0;
						SLOT.eg_sel_ar = 13*RATE_STEPS;
					}
					SLOT.eg_sh_dr  = eg_rate_shift [SLOT.dr + SLOT.ksr ];
					SLOT.eg_sel_dr = eg_rate_select[SLOT.dr + SLOT.ksr ];
					SLOT.eg_sh_rr  = eg_rate_shift [SLOT.rr + SLOT.ksr ];
					SLOT.eg_sel_rr = eg_rate_select[SLOT.rr + SLOT.ksr ];

					/* Calculate phase increment */
					SLOT.Incr = CH.fc * SLOT.mul;

					/* Total level */
					SLOT.TLL = SLOT.TL + (CH.ksl_base >>> SLOT.ksl);

					/* Connect output */
					SLOT.connect1 = SLOT.CON!=0 ? output : phase_modulation;
				}
			}
//		#if BUILD_Y8950
//			if ( (type & OPL_TYPE_ADPCM)!=0 && (deltat!=null) )
//			{
//				// We really should call the postload function for the YM_DELTAT, but it's hard without registers
//				// (see the way the YM2610 does it)
//				//deltat.postload(REGS);
//			}
//		#endif
		}

		/* set multi,am,vib,EG-TYP,KSR,mul */
		void set_mul(int slot, int v)
		{
			OPL_CH   CH   = P_CH[slot/2];
			OPL_SLOT SLOT = CH.SLOT[slot&1];

			SLOT.mul     = mul_tab[v&0x0f];
			SLOT.KSR     = (v & 0x10)!=0 ? 0 : 2;
			SLOT.eg_type = (v & 0x20);
			SLOT.vib     = (v & 0x40);
			SLOT.AMmask  = (v & 0x80)!=0 ? ~0 : 0;
			CH.CALC_FCSLOT(SLOT);
		}

		/* set ksl & tl */
		void set_ksl_tl(int slot, int v)
		{
			OPL_CH   CH   = P_CH[slot/2];
			OPL_SLOT SLOT = CH.SLOT[slot&1];

			SLOT.ksl = ksl_shift[v >> 6];
			SLOT.TL  = (v&0x3f)<<(ENV_BITS-1-7); /* 7 bits TL (bit 6 = always 0) */

			SLOT.TLL = SLOT.TL + (CH.ksl_base >>> SLOT.ksl);
		}

		/* set attack rate & decay rate  */
		void set_ar_dr(int slot, int v)
		{
			OPL_CH   CH   = P_CH[slot/2];
			OPL_SLOT SLOT = CH.SLOT[slot&1];

			SLOT.ar = (v>>4)!=0  ? 16 + ((v>>4)  <<2) : 0;

			if ((SLOT.ar + SLOT.ksr) < 16+62)
			{
				SLOT.eg_sh_ar  = eg_rate_shift [SLOT.ar + SLOT.ksr ];
				SLOT.eg_sel_ar = eg_rate_select[SLOT.ar + SLOT.ksr ];
			}
			else
			{
				SLOT.eg_sh_ar  = 0;
				SLOT.eg_sel_ar = 13*RATE_STEPS;
			}

			SLOT.dr    = (v&0x0f)!=0 ? 16 + ((v&0x0f)<<2) : 0;
			SLOT.eg_sh_dr  = eg_rate_shift [SLOT.dr + SLOT.ksr ];
			SLOT.eg_sel_dr = eg_rate_select[SLOT.dr + SLOT.ksr ];
		}

		/* set sustain level & release rate */
		void set_sl_rr(int slot, int v)
		{
			OPL_CH   CH   = P_CH[slot/2];
			OPL_SLOT SLOT = CH.SLOT[slot&1];

			SLOT.sl  = sl_tab[ v>>4 ];

			SLOT.rr  = (v&0x0f)!=0 ? 16 + ((v&0x0f)<<2) : 0;
			SLOT.eg_sh_rr  = eg_rate_shift [SLOT.rr + SLOT.ksr ];
			SLOT.eg_sel_rr = eg_rate_select[SLOT.rr + SLOT.ksr ];
		}

		void clock_changed(int c, int r)
		{
			clock = c;
			rate  = r;

			/* init global tables */
			initialize();
		}

		int Write(int a, int v)
		{
			if( (a&1)==0 )
			{   /* address port */
				address = v & 0xff;
			}
			else
			{   /* data port */
				if (UpdateHandler!=null) UpdateHandler.invoke(0);
				WriteReg(address, v);
			}
			return status>>7;
		}

		int Read(int a)
		{
			if( (a&1)==0 )
			{
				/* status port */

//			#if BUILD_Y8950
//				if((type&OPL_TYPE_ADPCM)!=0)    /* Y8950 */
//				{
//					return (status & (statusmask|0x80)) | (deltat.PCM_BSY&1);
//				}
//			#endif

				/* OPL and OPL2 */
				return status & (statusmask|0x80);
			}

//		#if BUILD_Y8950
//			/* data port */
//			switch (address)
//			{
//				case 0x05: /* KeyBoard IN */
//					if ((type & OPL_TYPE_KEYBOARD) != 0)
//					{
//						if (keyboardhandler_r != null) return keyboardhandler_r.invoke();
//					}
//					return 0;
//
//				case 0x0f: /* ADPCM-DATA */
//					if ((type & OPL_TYPE_ADPCM) != 0)
//					{
//						int val;
//
//						val = deltat.ADPCM_Read();
//						return val;
//					}
//					return 0;
//
//				case 0x19: /* I/O DATA */
//					if ((type & OPL_TYPE_IO) != 0)
//					{
//						if (porthandler_r != null) return porthandler_r.invoke();
//					}
//					return 0;
//				case 0x1a: /* PCM-DATA */
//					if ((type & OPL_TYPE_ADPCM) != 0)
//					{
//						return 0x80; /* 2's complement PCM data - result from A/D conversion */
//					}
//					return 0;
//			}
//		#endif

			return 0xff;
		}

		private int TimerOver(int c)
		{
			if( c!=0 )
			{   /* Timer B */
				STATUS_SET(0x20);
			}
			else
			{   /* Timer A */
				STATUS_SET(0x40);
				/* CSM mode key,TL control */
				if( (mode & 0x80)!=0 )
				{   /* CSM mode total level latch and auto key on */
					if (UpdateHandler!=null) UpdateHandler.invoke(0);
					for(int ch=0; ch<9; ch++)
						P_CH[ch].CSMKeyControll();
				}
			}
			/* reload timer */
			if (timer_handler!=null) (timer_handler).invoke(c, TimerBase * T[c]);
			return status>>7;
		}

		/* Create one of virtual YM3812/YM3526/Y8950 */
		/* 'clock' is chip clock in Hz  */
		/* 'rate'  is sampling rate  */
		private static FM_OPL Create(int clock, int rate, int type)
		{
			if (LockTable() == -1)
				return null;

			/* allocate memory block */
			final FM_OPL OPL = new FM_OPL();
			for (int i = 0; i < OPL.P_CH.length; i++)
			{
				OPL.P_CH[i] = new OPL_CH();
				for (int j = 0; j < OPL.P_CH[i].SLOT.length; j++)
					OPL.P_CH[i].SLOT[j] = new OPL_SLOT();
			}
//		#if BUILD_Y8950
//			OPL.deltat = new YM_DELTAT();
//		#endif
			
			OPL.type  = type;
			OPL.clock_changed(clock, rate);

			return OPL;
		}

		private final int volume_calc(final OPL_SLOT OP)
		{
			return OP.TLL + (int)(OP.volume) + (LFO_AM & OP.AMmask);
		}

		private int op_calc(long phase, int env, int pm, int wave_tab)
		{
			final int p = (env<<4) + sin_tab[wave_tab + ((((int)((phase & ~FREQ_MASK) + (pm<<16))) >> FREQ_SH ) & SIN_MASK) ];

			return (p >= TL_TAB_LEN) ? 0 : tl_tab[p];
		}

		private int op_calc1(long phase, int env, int pm, int wave_tab)
		{
			final int p = (env<<4) + sin_tab[wave_tab + ((((int)((phase & ~FREQ_MASK) + pm      )) >> FREQ_SH ) & SIN_MASK) ];

			return (p >= TL_TAB_LEN) ? 0 : tl_tab[p];
		}

		/* lock/unlock for common table */
		private static int LockTable()
		{
			num_lock++;
			if(num_lock>1) return 0;

			/* first time */

			/* allocate total level table (128kb space) */
			if( init_tables()==0 )
			{
				num_lock--;
				return -1;
			}

			return 0;
		}

		/* Optional handlers */
		void SetTimerHandler(OPL_TIMERHANDLER handler)
		{
			timer_handler = handler;
		}
		void SetIRQHandler(OPL_IRQHANDLER handler)
		{
			IRQHandler = handler;
		}
		void SetUpdateHandler(OPL_UPDATEHANDLER handler)
		{
			UpdateHandler = handler;
		}
	}

	/**
	 * Constructor for FMOPL_072
	 */
	public FMOPL_072()
	{
		super();
	}
	private static int limit(final int val, final int max, final int min)
	{
		if (val > max) return max;
		else 
		if (val < min) return min;

		return val;
	}
	/*******************************************************************************/
	/*		YM3812 local section                                                   */
	/*******************************************************************************/
	public static FM_OPL init(int type, int clock, int rate)
	{
		/* emulator create */
		FM_OPL chip = FM_OPL.Create(clock,rate,type);
		if (chip!=null)
		{
			chip.postload();
			reset_chip(chip);
		}
		return chip;
	}
	public static void shutdown(FM_OPL chip)
	{
		/* Nothing to do in java */
	}
	public static void reset_chip(FM_OPL chip)
	{
		chip.ResetChip();
	}
	public static int write(FM_OPL chip, int a, int v)
	{
		return chip.Write(a, v);
	}
	public static int read(FM_OPL chip, int a)
	{
		/* YM3812 always returns bit2 and bit1 in HIGH state */
		return chip.Read(a) | 0x06 ;
	}
	/*
	** Generate samples for one of the YM3812's
	**
	** 'which' is the virtual YM3812 number
	** '*buffer' is the output buffer pointer
	** 'length' is the number of samples that should be generated
	*/
	public static void update_one(FM_OPL chip, int[] buffer, int length)
	{
		final boolean rhythm = (chip.rhythm&0x20)!=0;

		for (int i=0; i < length ; i++ )
		{
			chip.output[0] = 0;

			chip.advance_lfo();

			/* FM part */
			chip.CALC_CH(chip.P_CH[0]);
			chip.CALC_CH(chip.P_CH[1]);
			chip.CALC_CH(chip.P_CH[2]);
			chip.CALC_CH(chip.P_CH[3]);
			chip.CALC_CH(chip.P_CH[4]);
			chip.CALC_CH(chip.P_CH[5]);

			if(!rhythm)
			{
				chip.CALC_CH(chip.P_CH[6]);
				chip.CALC_CH(chip.P_CH[7]);
				chip.CALC_CH(chip.P_CH[8]);
			}
			else        /* Rhythm part */
			{
				chip.CALC_RH();
			}

			/* limit check */
			/* store to sound buffer */
			buffer[i] = limit( chip.output[0] >> FINAL_SH , MAXOUT, MINOUT );

			chip.advance();
		}
	}
	public static int timer_over(FM_OPL chip, int c)
	{
		return chip.TimerOver(c);
	}
	public static void clock_changed(FM_OPL chip, int clock, int rate)
	{
		chip.clock_changed(clock, rate);
	}
	public static void set_timer_handler(FM_OPL chip, OPL_TIMERHANDLER timer_handler)
	{
		chip.SetTimerHandler(timer_handler);
	}
	public static void set_irq_handler(FM_OPL chip, OPL_IRQHANDLER IRQHandler)
	{
		chip.SetIRQHandler(IRQHandler);
	}
	public static void set_update_handler(FM_OPL chip, OPL_UPDATEHANDLER UpdateHandler)
	{
		chip.SetUpdateHandler(UpdateHandler);
	}
//#if BUILD_Y8950
//	/*******************************************************************************/
//	/*		YM8950 local section                                                   */
//	/*******************************************************************************/
//	public static FM_OPL y8950_init(int clock, int rate)
//	{
//		/* emulator create */
//		FM_OPL chip = FM_OPL.Create(clock, rate, OPL_TYPE_Y8950);
//		if (chip!=null)
//		{
//			chip.deltat.status_change_handler = chip;
//			chip.deltat.status_change_EOS_bit = 0x10;      /* status flag: set bit4 on End Of Sample */
//			chip.deltat.status_change_BRDY_bit = 0x08;     /* status flag: set bit3 on BRDY (End Of: ADPCM analysis/synthesis, memory reading/writing) */
//
//			//Y8950.deltat.write_time = 10.0 / clock;         /* a single byte write takes 10 cycles of main clock */
//			//Y8950.deltat.read_time  = 8.0 / clock;          /* a single byte read takes 8 cycles of main clock */
//			/* reset */
//			chip.postload();
//			reset_chip(chip);
//		}
//
//		return chip;
//	}
//	/*
//	** Generate samples for one of the Y8950's
//	**
//	** 'which' is the virtual Y8950 number
//	** '*buffer' is the output buffer pointer
//	** 'length' is the number of samples that should be generated
//	*/
//	public static void y8950_update_one(FM_OPL chip, int[] buffer, int length)
//	{
//		final YM_DELTAT DELTAT = chip.deltat;
//		final boolean rhythm  = (chip.rhythm&0x20)!=0;
//
//		for(int i=0; i < length ; i++ )
//		{
//			chip.output[0] = 0;
//			chip.output_deltat[0] = 0;
//
//			chip.advance_lfo();
//
//			/* deltaT ADPCM */
//			if( (DELTAT.portstate&0x80)!=0 )
//				DELTAT.ADPCM_CALC();
//
//			/* FM part */
//			chip.CALC_CH(chip.P_CH[0]);
//			chip.CALC_CH(chip.P_CH[1]);
//			chip.CALC_CH(chip.P_CH[2]);
//			chip.CALC_CH(chip.P_CH[3]);
//			chip.CALC_CH(chip.P_CH[4]);
//			chip.CALC_CH(chip.P_CH[5]);
//
//			if(!rhythm)
//			{
//				chip.CALC_CH(chip.P_CH[6]);
//				chip.CALC_CH(chip.P_CH[7]);
//				chip.CALC_CH(chip.P_CH[8]);
//			}
//			else        /* Rhythm part */
//			{
//				chip.CALC_RH();
//			}
//
//			/* limit check */
//			/* store to sound buffer */
//			buffer[i] = limit( (chip.output[0] + (chip.output_deltat[0]>>11)) >> FINAL_SH , MAXOUT, MINOUT );
//
//			chip.advance();
//		}
//	}
//	public void y8950_set_port_handler(FM_OPL chip, OPL_PORTHANDLER_W PortHandler_w, OPL_PORTHANDLER_R PortHandler_r)
//	{
//		chip.porthandler_w = PortHandler_w;
//		chip.porthandler_r = PortHandler_r;
//	}
//	public void y8950_set_keyboard_handler(FM_OPL chip, OPL_PORTHANDLER_W KeyboardHandler_w, OPL_PORTHANDLER_R KeyboardHandler_r)
//	{
//		chip.keyboardhandler_w = KeyboardHandler_w;
//		chip.keyboardhandler_r = KeyboardHandler_r;
//	}
//	#endif
}
