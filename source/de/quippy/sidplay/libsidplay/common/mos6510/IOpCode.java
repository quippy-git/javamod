/**
 *                                  description
 *                                  -----------
 *  begin                : Thu May 11 2000
 *  copyright            : (C) 2000 by Simon White
 *  email                : s_a_white@email.com
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; either version 3 of the License, or
 *   (at your option) any later version.
 *
 * @author Ken H�ndel
 *
 */
package de.quippy.sidplay.libsidplay.common.mos6510;

public interface IOpCode {

	public static final int OPCODE_MAX = 0x100;

	/*
	 * HLT
	 * 
	 * case 0x02: case 0x12: case 0x22: case 0x32: case 0x42: case 0x52:
	 * 
	 * case 0x62: case 0x72: case 0x92: case 0xb2: case 0xd2: case 0xf2:
	 * 
	 * case 0x02: case 0x12: case 0x22: case 0x32: case 0x42: case 0x52:
	 * 
	 * case 0x62: case 0x72: case 0x92: case 0xb2: case 0xd2: case 0xf2:
	 */

	public static final int BRKn = 0x00;

	public static final int JSRw = 0x20;

	public static final int RTIn = 0x40;

	public static final int RTSn = 0x60;

	public static final int NOPb = 0x80;

	public static final int NOPb_1 = 0x82;

	public static final int NOPb_2 = 0xC2;

	public static final int NOPb_3 = 0xE2;

	public static final int NOPb_4 = 0x89;

	public static final int LDYb = 0xA0;

	public static final int CPYb = 0xC0;

	public static final int CPXb = 0xE0;

	//

	public static final int ORAix = 0x01;

	public static final int ANDix = 0x21;

	public static final int EORix = 0x41;

	public static final int ADCix = 0x61;

	public static final int STAix = 0x81;

	public static final int LDAix = 0xA1;

	public static final int CMPix = 0xC1;

	public static final int SBCix = 0xE1;

	//

	public static final int LDXb = 0xA2;

	//

	public static final int SLOix = 0x03;

	public static final int RLAix = 0x23;

	public static final int SREix = 0x43;

	public static final int RRAix = 0x63;

	public static final int SAXix = 0x83;

	public static final int LAXix = 0xA3;

	public static final int DCPix = 0xC3;

	public static final int ISBix = 0xE3;

	//

	public static final int NOPz = 0x04;

	public static final int NOPz_1 = 0x44;

	public static final int NOPz_2 = 0x64;

	public static final int BITz = 0x24;

	public static final int STYz = 0x84;

	public static final int LDYz = 0xA4;

	public static final int CPYz = 0xC4;

	public static final int CPXz = 0xE4;

	//

	public static final int ORAz = 0x05;

	public static final int ANDz = 0x25;

	public static final int EORz = 0x45;

	public static final int ADCz = 0x65;

	public static final int STAz = 0x85;

	public static final int LDAz = 0xA5;

	public static final int CMPz = 0xC5;

	public static final int SBCz = 0xE5;

	//

	public static final int ASLz = 0x06;

	public static final int ROLz = 0x26;

	public static final int LSRz = 0x46;

	public static final int RORz = 0x66;

	public static final int STXz = 0x86;

	public static final int LDXz = 0xA6;

	public static final int DECz = 0xC6;

	public static final int INCz = 0xE6;

	//

	public static final int SLOz = 0x07;

	public static final int RLAz = 0x27;

	public static final int SREz = 0x47;

	public static final int RRAz = 0x67;

	public static final int SAXz = 0x87;

	public static final int LAXz = 0xA7;

	public static final int DCPz = 0xC7;

	public static final int ISBz = 0xE7;

	//

	public static final int PHPn = 0x08;

	public static final int PLPn = 0x28;

	public static final int PHAn = 0x48;

	public static final int PLAn = 0x68;

	public static final int DEYn = 0x88;

	public static final int TAYn = 0xA8;

	public static final int INYn = 0xC8;

	public static final int INXn = 0xE8;

	//

	public static final int ORAb = 0x09;

	public static final int ANDb = 0x29;

	public static final int EORb = 0x49;

	public static final int ADCb = 0x69;

	public static final int LDAb = 0xA9;

	public static final int CMPb = 0xC9;

	public static final int SBCb = 0xE9;

	public static final int SBCb_1 = 0XEB;

	//

	public static final int ASLn = 0x0A;

	public static final int ROLn = 0x2A;

	public static final int LSRn = 0x4A;

	public static final int RORn = 0x6A;

	public static final int TXAn = 0x8A;

	public static final int TAXn = 0xAA;

	public static final int DEXn = 0xCA;

	public static final int NOPn = 0xEA;

	public static final int NOPn_1 = 0x1A;

	public static final int NOPn_2 = 0x3A;

	public static final int NOPn_3 = 0x5A;

	public static final int NOPn_4 = 0x7A;

	public static final int NOPn_5 = 0xDA;

	public static final int NOPn_6 = 0xFA;

	//

	public static final int ANCb = 0x0B;

	public static final int ANCb_1 = 0x2B;

	public static final int ASRb = 0x4B;

	public static final int ARRb = 0x6B;

	public static final int ANEb = 0x8B;

	public static final int XAAb = 0x8B;

	public static final int LXAb = 0xAB;

	public static final int SBXb = 0xCB;

	//

	public static final int NOPa = 0x0C;

	public static final int BITa = 0x2C;

	public static final int JMPw = 0x4C;

	public static final int JMPi = 0x6C;

	public static final int STYa = 0x8C;

	public static final int LDYa = 0xAC;

	public static final int CPYa = 0xCC;

	public static final int CPXa = 0xEC;

	//

	public static final int ORAa = 0x0D;

	public static final int ANDa = 0x2D;

	public static final int EORa = 0x4D;

	public static final int ADCa = 0x6D;

	public static final int STAa = 0x8D;

	public static final int LDAa = 0xAD;

	public static final int CMPa = 0xCD;

	public static final int SBCa = 0xED;

	//

	public static final int ASLa = 0x0E;

	public static final int ROLa = 0x2E;

	public static final int LSRa = 0x4E;

	public static final int RORa = 0x6E;

	public static final int STXa = 0x8E;

	public static final int LDXa = 0xAE;

	public static final int DECa = 0xCE;

	public static final int INCa = 0xEE;

	//

	public static final int SLOa = 0x0F;

	public static final int RLAa = 0x2F;

	public static final int SREa = 0x4F;

	public static final int RRAa = 0x6F;

	public static final int SAXa = 0x8F;

	public static final int LAXa = 0xAF;

	public static final int DCPa = 0xCF;

	public static final int ISBa = 0xEF;

	//

	public static final int BPLr = 0x10;

	public static final int BMIr = 0x30;

	public static final int BVCr = 0x50;

	public static final int BVSr = 0x70;

	public static final int BCCr = 0x90;

	public static final int BCSr = 0xB0;

	public static final int BNEr = 0xD0;

	public static final int BEQr = 0xF0;

	//

	public static final int ORAiy = 0x11;

	public static final int ANDiy = 0x31;

	public static final int EORiy = 0x51;

	public static final int ADCiy = 0x71;

	public static final int STAiy = 0x91;

	public static final int LDAiy = 0xB1;

	public static final int CMPiy = 0xD1;

	public static final int SBCiy = 0xF1;

	//

	public static final int SLOiy = 0x13;

	public static final int RLAiy = 0x33;

	public static final int SREiy = 0x53;

	public static final int RRAiy = 0x73;

	public static final int SHAiy = 0x93;

	public static final int LAXiy = 0xB3;

	public static final int DCPiy = 0xD3;

	public static final int ISBiy = 0xF3;

	//

	public static final int NOPzx = 0x14;

	public static final int NOPzx_1 = 0x34;

	public static final int NOPzx_2 = 0x54;

	public static final int NOPzx_3 = 0x74;

	public static final int NOPzx_4 = 0xD4;

	public static final int NOPzx_5 = 0xF4;

	public static final int STYzx = 0x94;

	public static final int LDYzx = 0xB4;

	//

	public static final int ORAzx = 0x15;

	public static final int ANDzx = 0x35;

	public static final int EORzx = 0x55;

	public static final int ADCzx = 0x75;

	public static final int STAzx = 0x95;

	public static final int LDAzx = 0xB5;

	public static final int CMPzx = 0xD5;

	public static final int SBCzx = 0xF5;

	//

	public static final int ASLzx = 0x16;

	public static final int ROLzx = 0x36;

	public static final int LSRzx = 0x56;

	public static final int RORzx = 0x76;

	public static final int STXzy = 0x96;

	public static final int LDXzy = 0xB6;

	public static final int DECzx = 0xD6;

	public static final int INCzx = 0xF6;

	//

	public static final int SLOzx = 0x17;

	public static final int RLAzx = 0x37;

	public static final int SREzx = 0x57;

	public static final int RRAzx = 0x77;

	public static final int SAXzy = 0x97;

	public static final int LAXzy = 0xB7;

	public static final int DCPzx = 0xD7;

	public static final int ISBzx = 0xF7;

	//

	public static final int CLCn = 0x18;

	public static final int SECn = 0x38;

	public static final int CLIn = 0x58;

	public static final int SEIn = 0x78;

	public static final int TYAn = 0x98;

	public static final int CLVn = 0xB8;

	public static final int CLDn = 0xD8;

	public static final int SEDn = 0xF8;

	//

	public static final int ORAay = 0x19;

	public static final int ANDay = 0x39;

	public static final int EORay = 0x59;

	public static final int ADCay = 0x79;

	public static final int STAay = 0x99;

	public static final int LDAay = 0xB9;

	public static final int CMPay = 0xD9;

	public static final int SBCay = 0xF9;

	//

	public static final int TXSn = 0x9A;

	public static final int TSXn = 0xBA;

	//

	public static final int SLOay = 0x1B;

	public static final int RLAay = 0x3B;

	public static final int SREay = 0x5B;

	public static final int RRAay = 0x7B;

	public static final int SHSay = 0x9B;

	public static final int TASay = 0x9B;

	public static final int LASay = 0xBB;

	public static final int DCPay = 0xDB;

	public static final int ISBay = 0xFB;

	//

	public static final int NOPax = 0x1C;

	public static final int NOPax_1 = 0x3C;

	public static final int NOPax_2 = 0x5C;

	public static final int NOPax_3 = 0x7C;

	public static final int NOPax_4 = 0xDC;

	public static final int NOPax_5 = 0xFC;

	public static final int SHYax = 0x9C;

	public static final int LDYax = 0xBC;

	//

	public static final int ORAax = 0x1D;

	public static final int ANDax = 0x3D;

	public static final int EORax = 0x5D;

	public static final int ADCax = 0x7D;

	public static final int STAax = 0x9D;

	public static final int LDAax = 0xBD;

	public static final int CMPax = 0xDD;

	public static final int SBCax = 0xFD;

	//

	public static final int ASLax = 0x1E;

	public static final int ROLax = 0x3E;

	public static final int LSRax = 0x5E;

	public static final int RORax = 0x7E;

	public static final int SHXay = 0x9E;

	public static final int LDXay = 0xBE;

	public static final int DECax = 0xDE;

	public static final int INCax = 0xFE;

	//

	public static final int SLOax = 0x1F;

	public static final int RLAax = 0x3F;

	public static final int SREax = 0x5F;

	public static final int RRAax = 0x7F;

	public static final int SHAay = 0x9F;

	public static final int LAXay = 0xBF;

	public static final int DCPax = 0xDF;

	public static final int ISBax = 0xFF;

	//
	// Instruction Aliases
	//

	public static final int ASOix = SLOix;

	public static final int LSEix = SREix;

	public static final int AXSix = SAXix;

	public static final int DCMix = DCPix;

	public static final int INSix = ISBix;

	public static final int ASOz = SLOz;

	public static final int LSEz = SREz;

	public static final int AXSz = SAXz;

	public static final int DCMz = DCPz;

	public static final int INSz = ISBz;

	public static final int ALRb = ASRb;

	public static final int OALb = LXAb;

	public static final int ASOa = SLOa;

	public static final int LSEa = SREa;

	public static final int AXSa = SAXa;

	public static final int DCMa = DCPa;

	public static final int INSa = ISBa;

	public static final int ASOiy = SLOiy;

	public static final int LSEiy = SREiy;

	public static final int AXAiy = SHAiy;

	public static final int DCMiy = DCPiy;

	public static final int INSiy = ISBiy;

	public static final int ASOzx = SLOzx;

	public static final int LSEzx = SREzx;

	public static final int AXSzy = SAXzy;

	public static final int DCMzx = DCPzx;

	public static final int INSzx = ISBzx;

	public static final int ASOay = SLOay;

	public static final int LSEay = SREay;

	public static final int DCMay = DCPay;

	public static final int INSay = ISBay;

	public static final int SAYax = SHYax;

	public static final int XASay = SHXay;

	public static final int ASOax = SLOax;

	public static final int LSEax = SREax;

	public static final int AXAay = SHAay;

	public static final int DCMax = DCPax;

	public static final int INSax = ISBax;

	public static final int SKBn = NOPb;

	public static final int SKWn = NOPa;
}
