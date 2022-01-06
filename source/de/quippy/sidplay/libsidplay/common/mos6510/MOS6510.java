/**
 *                             Cycle Accurate 6510 Emulation
 *                             -----------------------------
 *  begin                : Thu May 11 06:22:40 BST 2000
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

import static de.quippy.sidplay.libsidplay.Config.S_A_WHITE_EMAIL;
import static de.quippy.sidplay.libsidplay.common.SIDEndian.endian_16;
import static de.quippy.sidplay.libsidplay.common.SIDEndian.endian_16hi8;
import static de.quippy.sidplay.libsidplay.common.SIDEndian.endian_16lo8;
import static de.quippy.sidplay.libsidplay.common.SIDEndian.endian_32hi8;
import static de.quippy.sidplay.libsidplay.common.SIDEndian.endian_32lo16;
import static de.quippy.sidplay.libsidplay.common.SIDEndian.endian_32lo8;
import static de.quippy.sidplay.libsidplay.common.mos6510.IConf6510.MOS6510_ACCURATE_CYCLES;
import static de.quippy.sidplay.libsidplay.common.mos6510.IOpCode.*;

import java.util.logging.Level;
import java.util.logging.Logger;

import de.quippy.sidplay.libsidplay.common.Event;
import de.quippy.sidplay.libsidplay.common.IEventContext;
import de.quippy.sidplay.libsidplay.common.Event.event_phase_t;


public class MOS6510 extends C64Environment /* extends Event */{

	public static final String MOS6510_VERSION = "1.08";

	public static final String MOS6510_DATE = "23th May 2000";

	public static final String MOS6510_AUTHOR = "Simon White";

	public static final String MOS6510_EMAIL = S_A_WHITE_EMAIL;

	public static final int MOS6510_INTERRUPT_DELAY = 2;

	//
	// Status Register flag definitions
	//

	public static final int SR_NEGATIVE = 7;

	public static final int SR_OVERFLOW = 6;

	public static final int SR_NOTUSED = 5;

	public static final int SR_BREAK = 4;

	public static final int SR_DECIMAL = 3;

	public static final int SR_INTERRUPT = 2;

	public static final int SR_ZERO = 1;

	public static final int SR_CARRY = 0;

	//

	public static final short SP_PAGE = 0x01;

	public static final boolean PC64_TESTSUITE = false;

	/**
	 * CHR$ conversion table (0x01 = no output)
	 */
	private static final char _sidtune_CHRtab[] = {
			0x0, 0x1, 0x1, 0x1, 0x1, 0x1, 0x1, 0x1, 0x1, 0x1, 0x1, 0x1, 0x1,
			0xd, 0x1, 0x1, 0x1, 0x1, 0x1, 0x1, 0x1, 0x1, 0x1, 0x1, 0x1, 0x1,
			0x1, 0x1, 0x1, 0x1, 0x1, 0x1, 0x20, 0x21, 0x1, 0x23, 0x24, 0x25,
			0x26, 0x27, 0x28, 0x29, 0x2a, 0x2b, 0x2c, 0x2d, 0x2e, 0x2f, 0x30,
			0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3a, 0x3b,
			0x3c, 0x3d, 0x3e, 0x3f, 0x40, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46,
			0x47, 0x48, 0x49, 0x4a, 0x4b, 0x4c, 0x4d, 0x4e, 0x4f, 0x50, 0x51,
			0x52, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x5a, 0x5b, 0x24,
			0x5d, 0x20, 0x20,
			/* alternative: CHR$(92=0x5c) => ISO Latin-1(0xa3) */
			0x2d, 0x23, 0x7c, 0x2d, 0x2d, 0x2d, 0x2d, 0x7c, 0x7c, 0x5c, 0x5c,
			0x2f, 0x5c, 0x5c, 0x2f, 0x2f, 0x5c, 0x23, 0x5f, 0x23, 0x7c, 0x2f,
			0x58, 0x4f, 0x23, 0x7c, 0x23, 0x2b, 0x7c, 0x7c, 0x26, 0x5c,
			/* 0x80-0xFF */
			0x1, 0x1, 0x1, 0x1, 0x1, 0x1, 0x1, 0x1, 0x1, 0x1, 0x1, 0x1, 0x1,
			0x1, 0x1, 0x1, 0x1, 0x1, 0x1, 0x1, 0x1, 0x1, 0x1, 0x1, 0x1, 0x1,
			0x1, 0x1, 0x1, 0x1, 0x1, 0x1, 0x20, 0x7c, 0x23, 0x2d, 0x2d, 0x7c,
			0x23, 0x7c, 0x23, 0x2f, 0x7c, 0x7c, 0x2f, 0x5c, 0x5c, 0x2d, 0x2f,
			0x2d, 0x2d, 0x7c, 0x7c, 0x7c, 0x7c, 0x2d, 0x2d, 0x2d, 0x2f, 0x5c,
			0x5c, 0x2f, 0x2f, 0x23, 0x2d, 0x23, 0x7c, 0x2d, 0x2d, 0x2d, 0x2d,
			0x7c, 0x7c, 0x5c, 0x5c, 0x2f, 0x5c, 0x5c, 0x2f, 0x2f, 0x5c, 0x23,
			0x5f, 0x23, 0x7c, 0x2f, 0x58, 0x4f, 0x23, 0x7c, 0x23, 0x2b, 0x7c,
			0x7c, 0x26, 0x5c, 0x20, 0x7c, 0x23, 0x2d, 0x2d, 0x7c, 0x23, 0x7c,
			0x23, 0x2f, 0x7c, 0x7c, 0x2f, 0x5c, 0x5c, 0x2d, 0x2f, 0x2d, 0x2d,
			0x7c, 0x7c, 0x7c, 0x7c, 0x2d, 0x2d, 0x2d, 0x2f, 0x5c, 0x5c, 0x2f,
			0x2f, 0x23 };

	private static char filetmp[] = new char[0x100];

	private static int filepos = 0;

	//
	// External signals
	//

	/**
	 * Address Controller, blocks reads
	 */
	protected boolean aec;

	protected boolean m_blocked;

	protected long /* event_clock_t */m_stealingClk;

	protected long /* event_clock_t */m_dbgClk;

	protected static final Logger MOS6510 = Logger.getLogger(MOS6510.class.getName());

	protected boolean dodump;

	protected IEventContext eventContext;

	/**
	 * Clock phase in use by the processor
	 */
	protected event_phase_t m_phase;

	/**
	 * Clock phase when external events appear
	 */
	protected event_phase_t m_extPhase;

	/**
	 * Resolve use of function pointer
	 * 
	 * @author Ken H�ndel
	 * 
	 */
	protected interface IFunc {
		void invoke();
	}

	protected static class ProcessorCycle {
		IFunc func;

		boolean nosteal;

		ProcessorCycle() {
			func = null;
			nosteal = (false);
		}
	}

	/**
	 * Declare processor operations
	 * 
	 * @author Ken H�ndel
	 * 
	 */
	protected static class ProcessorOperations {
		ProcessorCycle cycle[];

		//short /* uint */cycles;

		//short /* uint_least8_t */opcode;

		ProcessorOperations() {
			cycle = null;
			//cycles = 0;
		}
	}

	protected ProcessorCycle fetchCycle = new ProcessorCycle();

	protected ProcessorCycle procCycle[];

	protected ProcessorOperations instrTable[] = new ProcessorOperations[0x100];

	protected ProcessorOperations interruptTable[] = new ProcessorOperations[3];

	protected ProcessorOperations instrCurrent;

	protected int /* uint_least16_t */instrStartPC;

	protected short /* uint_least8_t */instrOpcode;

	protected byte /* int_least8_t */lastAddrCycle;

	protected byte /* int_least8_t */cycleCount;

	//
	// Pointers to the current instruction cycle
	//

	protected int /* uint_least16_t */Cycle_EffectiveAddress;

	protected short /* uint8_t */Cycle_Data;

	protected int /* uint_least16_t */Cycle_Pointer;

	protected short /* uint8_t */Register_Accumulator;

	protected short /* uint8_t */Register_X;

	protected short /* uint8_t */Register_Y;

	protected long /* uint_least32_t */Register_ProgramCounter;

	protected short /* uint8_t */Register_Status;

	protected short /* uint_least8_t */Register_c_Flag;

	protected short /* uint_least8_t */Register_n_Flag;

	protected short /* uint_least8_t */Register_v_Flag;

	protected short /* uint_least8_t */Register_z_Flag;

	protected int /* uint_least16_t */Register_StackPointer;

	protected int /* uint_least16_t */Instr_Operand;

	//
	// Interrupts
	//

	protected static class Interrupts {
		short /* uint_least8_t */pending;

		short /* uint_least8_t */irqs;

		long /* event_clock_t */nmiClk;

		long /* event_clock_t */irqClk;

		boolean irqRequest;

		boolean irqLatch;
	}

	protected Interrupts interrupts = new Interrupts();

	protected short /* uint8_t */Debug_Data;

	protected int /* uint_least16_t */Debug_EffectiveAddress;

	protected short/* uint_least8_t */Debug_Opcode;

	protected int /* uint_least16_t */Debug_Operand;

	protected int /* uint_least16_t */Debug_ProgramCounter;

	/**
	 * Emulate One Complete Cycle
	 */
	protected void clock() {
		byte /* int_least8_t */i = cycleCount++;
		if (procCycle[i].nosteal || aec) {
			this.procCycle[i].func.invoke();
			return;
		} else if (!m_blocked) {
			m_blocked = true;
			m_stealingClk = eventContext.getTime(m_phase);
		}
		cycleCount--;
		eventContext.cancel(event);
	}

	/**
	 * Resolve multiple inheritance
	 */
	protected Event event = new Event("CPU") {
		public void event() {
			eventContext.schedule(event, 1, m_phase);
			clock();
		}
	};

	/**
	 * Initialize CPU Emulation (Registers)
	 */
	protected void Initialise() {
		// Reset stack
		Register_StackPointer = endian_16(SP_PAGE, (short) 0xFF);

		// Reset Cycle Count
		cycleCount = 0;
		procCycle = new ProcessorCycle[] {
			fetchCycle };

		// Reset Status Register
		Register_Status = (1 << SR_NOTUSED) | (1 << SR_BREAK);
		// FLAGS are set from data directly and do not require
		// being calculated first before setting. E.g. if you used
		// SetFlags (0), N flag would = 0, and Z flag would = 1.
		setFlagsNZ((short) 1);
		setFlagC((short) 0);
		setFlagV((short) 0);

		// Set PC to some value
		Register_ProgramCounter = 0;
		// IRQs pending check
		interrupts.irqLatch = false;
		interrupts.irqRequest = false;
		if ((interrupts.irqs) != 0)
			interrupts.irqRequest = true;

		// Signals
		aec = true;

		m_blocked = false;
		eventContext.schedule(event, 0, m_phase);
	}

	//
	// Declare Interrupt Routines
	//

	protected void RSTRequest() {
		envReset();
	}

	// protected void RST1Request() {;}

	protected void NMIRequest() {
		Cycle_EffectiveAddress = endian_16lo8(Cycle_EffectiveAddress,
				envReadMemDataByte(0xFFFA));
	}

	protected void NMI1Request() {
		Cycle_EffectiveAddress = endian_16hi8(Cycle_EffectiveAddress,
				envReadMemDataByte(0xFFFB));
		Register_ProgramCounter = endian_32lo16(Register_ProgramCounter,
				Cycle_EffectiveAddress);
	}

	protected void IRQRequest() {
		PushSR(false);
		setFlagI((short) 1);
		interrupts.irqRequest = false;
	}

	protected void IRQ1Request() {
		Cycle_EffectiveAddress = endian_16lo8(Cycle_EffectiveAddress,
				envReadMemDataByte(0xFFFE));
	}

	protected void IRQ2Request() {
		Cycle_EffectiveAddress = endian_16hi8(Cycle_EffectiveAddress,
				envReadMemDataByte(0xFFFF));
		Register_ProgramCounter = endian_32lo16(Register_ProgramCounter,
				Cycle_EffectiveAddress);
	}

	protected boolean interruptPending() {
		byte /* int_least8_t */offset, pending;
		final byte /* int_least8_t */offTable[] = {
				oNONE, oRST, oNMI, oRST, oIRQ, oRST, oNMI, oRST };
		// Update IRQ pending
		if (!interrupts.irqLatch) {
			interrupts.pending &= ~iIRQ & 0xff;
			if (interrupts.irqRequest)
				interrupts.pending |= iIRQ;
		}

		pending = (byte) interrupts.pending;

		MOS6510_interruptPending_check: while (true) {
			// Service the highest priority interrupt
			offset = offTable[pending];
			switch (offset) {
			case oNONE:
				return false;

			case oNMI: {
				// Try to determine if we should be processing the NMI yet
				long /* event_clock_t */cycles = eventContext.getTime(
						interrupts.nmiClk, m_extPhase);
				if (cycles >= MOS6510_INTERRUPT_DELAY) {
					interrupts.pending &= ~iNMI & 0xff;
					break;
				}

				// NMI delayed so check for other interrupts
				pending &= ~iNMI & 0xff;
				continue; // MOS6510_interruptPending_check;
			}

			case oIRQ: {
				// Try to determine if we should be processing the IRQ yet
				long /* event_clock_t */cycles = eventContext.getTime(
						interrupts.irqClk, m_extPhase);
				if (cycles >= MOS6510_INTERRUPT_DELAY)
					break;

				// NMI delayed so check for other interrupts
				pending &= ~iIRQ & 0xff;
				continue; // MOS6510_interruptPending_check;
			}

			case oRST:
				break;
			}

			if (MOS6510.isLoggable(Level.FINE)) {
				long /* event_clock_t */cycles = eventContext.getTime(m_phase);
				if (dodump) {
					MOS6510.fine("****************************************************\n");
					switch (offset) {
					case oIRQ:
						MOS6510.fine(String.format(" IRQ Routine (%d)\n", Long.valueOf(cycles)));
						break;
					case oNMI:
						MOS6510.fine(String.format(" NMI Routine (%d)\n", Long.valueOf(cycles)));
						break;
					case oRST:
						MOS6510.fine(String.format(" RST Routine (%d)\n", Long.valueOf(cycles)));
						break;
					}
					MOS6510
							.fine("****************************************************\n");
				}
			}
			// END PSEUDO LOOP
			break MOS6510_interruptPending_check;
		}

		// Start the interrupt
		instrCurrent = interruptTable[offset];
		procCycle = instrCurrent.cycle;
		cycleCount = 0;
		clock();
		return true;
	}

	//
	// Declare Instruction Routines
	//

	//
	// Common Instruction Addressing Routines
	// Addressing operations as described in 64doc by John West and
	// Marko Makela
	//

	/**
	 * Fetch opcode, increment PC<BR>
	 * 
	 * Addressing Modes: All
	 */
	protected void FetchOpcode() {
		// On new instruction all interrupt delays are reset
		interrupts.irqLatch = false;

		if (MOS6510.isLoggable(Level.FINE)) {
			m_dbgClk = eventContext.getTime(m_phase);
		}

		instrStartPC = endian_32lo16(Register_ProgramCounter++);
		instrOpcode = envReadMemByte(instrStartPC);

		// Convert opcode to pointer in instruction table
		instrCurrent = instrTable[instrOpcode];
		Instr_Operand = 0;
		procCycle = instrCurrent.cycle;
		cycleCount = 0;

	}

	protected void NextInstr() {
		if (!interruptPending()) {
			cycleCount = 0;
			procCycle = new ProcessorCycle[] {
				fetchCycle };
			clock();
		}
	}

	/**
	 * Fetch value, increment PC<BR>
	 * 
	 * Addressing Modes:
	 * <UL>
	 * <LI> Immediate
	 * <LI> Relative
	 * </UL>
	 */
	protected void FetchDataByte() {
		// Get data byte from memory
		Cycle_Data = envReadMemByte(endian_32lo16(Register_ProgramCounter));
		Register_ProgramCounter++;

		// Next line used for Debug
		Instr_Operand = (int /* uint_least16_t */) Cycle_Data;
	}

	/**
	 * Fetch low address byte, increment PC<BR>
	 * 
	 * Addressing Modes:
	 * <UL>
	 * <LI> Stack Manipulation
	 * <LI> Absolute
	 * <LI> Zero Page
	 * <LI> Zero Page Indexed
	 * <LI> Absolute Indexed
	 * <LI> Absolute Indirect
	 * </UL>
	 */
	protected void FetchLowAddr() {
		Cycle_EffectiveAddress = envReadMemByte(endian_32lo16(Register_ProgramCounter));
		Register_ProgramCounter++;

		// Next line used for Debug
		Instr_Operand = Cycle_EffectiveAddress;
	}

	/**
	 * Read from address, add index register X to it<BR>
	 * 
	 * Addressing Modes:
	 * <UL>
	 * <LI> Zero Page Indexed
	 * </UL>
	 */
	protected void FetchLowAddrX() {
		FetchLowAddr();
		Cycle_EffectiveAddress = (Cycle_EffectiveAddress + Register_X) & 0xFF;
	}

	/**
	 * Read from address, add index register Y to it<BR>
	 * 
	 * Addressing Modes:
	 * <UL>
	 * <LI> Zero Page Indexed
	 * </UL>
	 */
	protected void FetchLowAddrY() {
		FetchLowAddr();
		Cycle_EffectiveAddress = (Cycle_EffectiveAddress + Register_Y) & 0xFF;
	}

	/**
	 * Fetch high address byte, increment PC (Absolute Addressing)<BR>
	 * 
	 * Low byte must have been obtained first!<BR>
	 * 
	 * Addressing Modes:
	 * <UL>
	 * <LI> Absolute
	 * </UL>
	 */
	protected void FetchHighAddr() {
		// Get the high byte of an address from memory
		Cycle_EffectiveAddress = endian_16hi8(Cycle_EffectiveAddress,
				envReadMemByte(endian_32lo16(Register_ProgramCounter)));
		Register_ProgramCounter++;

		// Next line used for Debug
		Instr_Operand = endian_16hi8(Instr_Operand,
				endian_16hi8(Cycle_EffectiveAddress));
	}

	/**
	 * Fetch high byte of address, add index register X to low address byte,<BR>
	 * 
	 * increment PC<BR>
	 * 
	 * Addressing Modes:
	 * <UL>
	 * <LI> Absolute Indexed
	 * </UL>
	 */
	protected void FetchHighAddrX() {
		short /* uint8_t */page;
		// Rev 1.05 (saw) - Call base Function
		FetchHighAddr();
		page = endian_16hi8(Cycle_EffectiveAddress);
		Cycle_EffectiveAddress += Register_X;

		if (MOS6510_ACCURATE_CYCLES) {
			// Handle page boundary crossing
			if (endian_16hi8(Cycle_EffectiveAddress) == page)
				cycleCount++;
		}
	}

	/**
	 * Same as above except dosen't worry about page crossing
	 */
	protected void FetchHighAddrX2() {
		FetchHighAddr();
		Cycle_EffectiveAddress += Register_X;
	}

	/**
	 * Fetch high byte of address, add index register Y to low address byte,<BR>
	 * 
	 * increment PC<BR>
	 * 
	 * Addressing Modes:
	 * <UL>
	 * <LI> Absolute Indexed
	 * </UL>
	 */
	protected void FetchHighAddrY() {
		short /* uint8_t */page;
		// Rev 1.05 (saw) - Call base Function
		FetchHighAddr();
		page = endian_16hi8(Cycle_EffectiveAddress);
		Cycle_EffectiveAddress += Register_Y;

		if (MOS6510_ACCURATE_CYCLES) {
			// Handle page boundary crossing
			if (endian_16hi8(Cycle_EffectiveAddress) == page)
				cycleCount++;
		}
	}

	/**
	 * Same as above except dosen't worry about page crossing
	 */
	protected void FetchHighAddrY2() {
		FetchHighAddr();
		Cycle_EffectiveAddress += Register_Y;
	}

	/**
	 * Fetch effective address low<BR>
	 * 
	 * Addressing Modes:
	 * <UL>
	 * <LI> Indirect
	 * <LI> Indexed Indirect (pre X)
	 * <LI> Indirect indexed (post Y)
	 * </UL>
	 */
	protected void FetchLowEffAddr() {
		Cycle_EffectiveAddress = envReadMemDataByte(Cycle_Pointer);
	}

	/**
	 * Fetch effective address high<BR>
	 * 
	 * Addressing Modes:
	 * <UL>
	 * <LI> Indirect
	 * <LI> Indexed Indirect (pre X)
	 * </UL>
	 */
	protected void FetchHighEffAddr() {
		// Rev 1.03 (Mike) - Extra +1 removed
		Cycle_Pointer = endian_16lo8(Cycle_Pointer,
				(short) ((Cycle_Pointer + 1) & 0xff));
		Cycle_EffectiveAddress = endian_16hi8(Cycle_EffectiveAddress,
				envReadMemDataByte(Cycle_Pointer));
	}

	/**
	 * Fetch effective address high, add Y to low byte of effective address<BR>
	 * 
	 * Addressing Modes:
	 * <UL>
	 * <LI> Indirect indexed (post Y)
	 * <UL>
	 */
	protected void FetchHighEffAddrY() {
		short /* uint8_t */page;
		// Rev 1.05 (saw) - Call base Function
		FetchHighEffAddr();
		page = endian_16hi8(Cycle_EffectiveAddress);
		Cycle_EffectiveAddress += Register_Y;

		if (MOS6510_ACCURATE_CYCLES) {
			// Handle page boundary crossing
			if (endian_16hi8(Cycle_EffectiveAddress) == page)
				cycleCount++;
		}
	}

	/**
	 * Same as above except dosen't worry about page crossing
	 */
	protected void FetchHighEffAddrY2() {
		FetchHighEffAddr();
		Cycle_EffectiveAddress += Register_Y;
	}

	/**
	 * Fetch pointer address low, increment PC<BR>
	 * 
	 * Addressing Modes:
	 * <UL>
	 * <LI> Absolute Indirect
	 * <LI> Indirect indexed (post Y)
	 * </UL>
	 */
	protected void FetchLowPointer() {
		Cycle_Pointer = envReadMemByte(endian_32lo16(Register_ProgramCounter));
		Register_ProgramCounter++;
		// Next line used for Debug
		Instr_Operand = Cycle_Pointer;
	}

	/**
	 * Read pointer from the address and add X to it<BR>
	 * 
	 * Addressing Modes:
	 * <UL>
	 * <LI> Indexed Indirect (pre X)
	 * </UL>
	 */
	protected void FetchLowPointerX() {
		Cycle_Pointer = endian_16hi8(Cycle_Pointer,
				envReadMemDataByte(Cycle_Pointer));
		// Page boundary crossing is not handled
		Cycle_Pointer = (Cycle_Pointer + Register_X) & 0xFF;
	}

	/**
	 * Fetch pointer address high, increment PC<BR>
	 * 
	 * Addressing Modes:
	 * <UL>
	 * <LI> Absolute Indirect
	 * </UL>
	 */
	protected void FetchHighPointer() {
		Cycle_Pointer = endian_16hi8(Cycle_Pointer,
				envReadMemByte(endian_32lo16(Register_ProgramCounter)));
		Register_ProgramCounter++;

		// Next line used for Debug
		Instr_Operand = endian_16hi8(Instr_Operand, endian_16hi8(Cycle_Pointer));
	}

	//
	// Common Data Accessing Routines
	// Data Accessing operations as described in 64doc by John West and
	// Marko Makela
	//

	protected void FetchEffAddrDataByte() {
		Cycle_Data = envReadMemDataByte(Cycle_EffectiveAddress);
	}

	protected void PutEffAddrDataByte() {
		envWriteMemByte(Cycle_EffectiveAddress, Cycle_Data);
	}

	/**
	 * Push Program Counter Low Byte on stack, decrement S
	 */
	protected void PushLowPC() {
		int /* uint_least16_t */addr;
		addr = Register_StackPointer;
		addr = endian_16hi8(addr, SP_PAGE);
		envWriteMemByte(addr, endian_32lo8(Register_ProgramCounter));
		Register_StackPointer--;
	}

	/**
	 * Push Program Counter High Byte on stack, decrement S
	 */
	protected void PushHighPC() {
		int /* uint_least16_t */addr;
		addr = Register_StackPointer;
		addr = endian_16hi8(addr, SP_PAGE);
		envWriteMemByte(addr, endian_32hi8(Register_ProgramCounter));
		Register_StackPointer--;
	}

	/**
	 * Push P on stack, decrement S
	 * 
	 * @param b_flag
	 */
	protected void PushSR(boolean b_flag) {
		int /* uint_least16_t */addr = Register_StackPointer;
		addr = endian_16hi8(addr, SP_PAGE);
		/* Rev 1.04 - Corrected flag mask */
		Register_Status &= ((1 << SR_NOTUSED) | (1 << SR_INTERRUPT)
				| (1 << SR_DECIMAL) | (1 << SR_BREAK));
		Register_Status |= ((getFlagN() ? 1 : 0) << SR_NEGATIVE);
		Register_Status |= ((getFlagV() ? 1 : 0) << SR_OVERFLOW);
		Register_Status |= ((getFlagZ() ? 1 : 0) << SR_ZERO);
		Register_Status |= ((getFlagC() ? 1 : 0) << SR_CARRY);
		envWriteMemByte(
				addr,
				(short) (Register_Status & (~(((!b_flag) ? 1 : 0) << SR_BREAK) & 0xff)));
		Register_StackPointer--;
	}

	protected void PushSR() {
		PushSR(true);
	}

	/**
	 * Increment stack and pull program counter low byte from stack,
	 */
	protected void PopLowPC() {
		int /* uint_least16_t */addr;
		Register_StackPointer++;
		addr = Register_StackPointer;
		addr = endian_16hi8(addr, SP_PAGE);
		Cycle_EffectiveAddress = endian_16lo8(Cycle_EffectiveAddress,
				envReadMemDataByte(addr));
	}

	/**
	 * Increment stack and pull program counter high byte from stack,
	 */
	protected void PopHighPC() {
		int /* uint_least16_t */addr;
		Register_StackPointer++;
		addr = Register_StackPointer;
		addr = endian_16hi8(addr, SP_PAGE);
		Cycle_EffectiveAddress = endian_16hi8(Cycle_EffectiveAddress,
				envReadMemDataByte(addr));
	}

	/**
	 * increment S, Pop P off stack
	 */
	protected void PopSR() {
		boolean newFlagI, oldFlagI;
		oldFlagI = getFlagI();

		// Get status register off stack
		Register_StackPointer++;
		{
			int /* uint_least16_t */addr = Register_StackPointer;
			addr = endian_16hi8(addr, SP_PAGE);
			Register_Status = envReadMemDataByte(addr);
		}
		Register_Status |= ((1 << SR_NOTUSED) | (1 << SR_BREAK));
		setFlagN(Register_Status);
		setFlagV((short) (Register_Status & (1 << SR_OVERFLOW)));
		setFlagZ((short) (((Register_Status & (1 << SR_ZERO)) == 0) ? 1 : 0));
		setFlagC((short) (Register_Status & (1 << SR_CARRY)));

		// I flag change is delayed by 1 instruction
		newFlagI = getFlagI();
		interrupts.irqLatch = oldFlagI ^ newFlagI;
		// Check to see if interrupts got re-enabled
		if (!newFlagI && (interrupts.irqs != 0))
			interrupts.irqRequest = true;
	}

	protected void WasteCycle() {
		if (!MOS6510_ACCURATE_CYCLES) {
			clock();
		}
	}

	protected void DebugCycle() {
		if (dodump)
			DumpState();
		clock();
	}

	//
	// Generic Instruction Addressing Routines
	//

	//
	// Generic Instruction Undocumented Opcodes
	// See documented 6502-nmo.opc by Adam Vardy for more details
	//

	//
	// Generic Instruction Opcodes
	// See and 6510 Assembly Book for more information on these instructions
	//

	protected void adc_instr() {
		Perform_ADC();
		clock();
	}

	/**
	 * Undocumented - This opcode ANDs the contents of the A register with an
	 * immediate value and then LSRs the result.
	 */
	protected void alr_instr() {
		Register_Accumulator &= Cycle_Data;
		setFlagC((short) (Register_Accumulator & 0x01));
		setFlagsNZ(Register_Accumulator >>= 1);
		clock();
	}

	/**
	 * Undocumented - ANC ANDs the contents of the A register with an immediate
	 * value and then moves bit 7 of A into the Carry flag. This opcode works
	 * basically identically to AND #immed. except that the Carry flag is set to
	 * the same state that the Negative flag is set to.
	 */
	protected void anc_instr() {
		setFlagsNZ(Register_Accumulator &= Cycle_Data);
		setFlagC((short) (getFlagN() ? 1 : 0));
		clock();
	}

	protected void and_instr() {
		setFlagsNZ(Register_Accumulator &= Cycle_Data);
		clock();
	}

	protected void ane_instr() {
		setFlagsNZ(Register_Accumulator = (short) ((Register_Accumulator | 0xee)
				& Register_X & Cycle_Data));
		clock();
	}

	/**
	 * Undocumented - This opcode ANDs the contents of the A register with an
	 * immediate value and then RORs the result (Implementation based on that of
	 * Frodo C64 Emulator)
	 */
	protected void arr_instr() {
		short /* uint8_t */data = (short) (Cycle_Data & Register_Accumulator);
		Register_Accumulator = (short) (data >> 1);
		if (getFlagC())
			Register_Accumulator |= 0x80;

		if (getFlagD()) {
			setFlagN((short) 0);
			if (getFlagC())
				setFlagN((short) (1 << SR_NEGATIVE));
			setFlagZ(Register_Accumulator);
			setFlagV((short) ((data ^ Register_Accumulator) & 0x40));

			if ((data & 0x0f) + (data & 0x01) > 5)
				Register_Accumulator = (short) (Register_Accumulator & 0xf0 | (Register_Accumulator + 6) & 0x0f);
			setFlagC((short) (((data + (data & 0x10)) & 0x1f0) > 0x50 ? 1 : 0));
			if (getFlagC())
				Register_Accumulator += 0x60;
		} else {
			setFlagsNZ(Register_Accumulator);
			setFlagC((short) (Register_Accumulator & 0x40));
			setFlagV((short) ((Register_Accumulator & 0x40) ^ ((Register_Accumulator & 0x20) << 1)));
		}
		clock();
	}

	protected void asl_instr() {
		PutEffAddrDataByte();
		setFlagC((short) (Cycle_Data & 0x80));
		setFlagsNZ(Cycle_Data = (short) ((Cycle_Data << 1) & 0xff));
	}

	protected void asla_instr() {
		setFlagC((short) (Register_Accumulator & 0x80));
		setFlagsNZ(Register_Accumulator = (short) ((Register_Accumulator << 1) & 0xff));
		clock();
	}

	/**
	 * Undocumented - This opcode ASLs the contents of a memory location and
	 * then ORs the result with the accumulator.
	 */
	protected void aso_instr() {
		PutEffAddrDataByte();
		setFlagC((short) (Cycle_Data & 0x80));
		Cycle_Data = (short) ((Cycle_Data << 1) & 0xff);
		setFlagsNZ(Register_Accumulator |= Cycle_Data);
	}

	/**
	 * Undocumented - This opcode stores the result of A AND X AND the high byte
	 * of the target address of the operand +1 in memory.
	 */
	protected void axa_instr() {
		Cycle_Data = (short) (Register_X & Register_Accumulator & (endian_16hi8(Cycle_EffectiveAddress) + 1));
		PutEffAddrDataByte();
	}

	/**
	 * Undocumented - AXS ANDs the contents of the A and X registers (without
	 * changing the contents of either register) and stores the result in
	 * memory. AXS does not affect any flags in the processor status register.
	 */
	protected void axs_instr() {
		Cycle_Data = (short) (Register_Accumulator & Register_X);
		PutEffAddrDataByte();
	}

	protected void bcc_instr() {
		branch_instr(!getFlagC());
	}

	protected void bcs_instr() {
		branch_instr(getFlagC());
	}

	protected void beq_instr() {
		branch_instr(getFlagZ());
	}

	protected void bit_instr() {
		setFlagZ((short) (Register_Accumulator & Cycle_Data));
		setFlagN(Cycle_Data);
		setFlagV((short) (Cycle_Data & 0x40));
		clock();
	}

	protected void bmi_instr() {
		branch_instr(getFlagN());
	}

	protected void bne_instr() {
		branch_instr(!getFlagZ());
	}

	protected void branch_instr(boolean condition) {
		if (MOS6510_ACCURATE_CYCLES) {
			if (condition) {
				short /* uint8_t */page;
				page = endian_32hi8(Register_ProgramCounter);
				Register_ProgramCounter += (byte /* int8_t */) Cycle_Data;

				// Handle page boundary crossing
				if (endian_32hi8(Register_ProgramCounter) != page)
					cycleCount++;
			} else {
				cycleCount += 2;
				clock();
			}
		} else {
			if (condition) {
				Register_ProgramCounter += (byte /* int8_t */) Cycle_Data;
			}
		}
	}

	protected void branch2_instr() {
		// This only gets processed when page boundary
		// is no crossed. This causes pending interrupts
		// to be delayed by a cycle
		interrupts.irqClk++;
		interrupts.nmiClk++;
		cycleCount++;
		clock();
	}

	protected void bpl_instr() {
		branch_instr(!getFlagN());
	}

	protected void brk_instr() {
		PushSR();
		setFlagI((short) 1);
		interrupts.irqRequest = false;

		// Check for an NMI, and switch over if pending
		if ((interrupts.pending & iNMI) != 0) {
			long /* event_clock_t */cycles = eventContext.getTime(
					interrupts.nmiClk, m_extPhase);
			if (cycles > MOS6510_INTERRUPT_DELAY) {
				interrupts.pending &= ~iNMI & 0xff;
				instrCurrent = interruptTable[oNMI];
				procCycle = instrCurrent.cycle;
			}
		}
	}

	protected void bvc_instr() {
		branch_instr(!getFlagV());
	}

	protected void bvs_instr() {
		branch_instr(getFlagV());
	}

	protected void clc_instr() {
		setFlagC((short) 0);
		clock();
	}

	protected void cld_instr() {
		setFlagD((short) 0);
		clock();
	}

	protected void cli_instr() {
		boolean oldFlagI = getFlagI();
		setFlagI((short) 0);
		// I flag change is delayed by 1 instruction
		interrupts.irqLatch = oldFlagI ^ getFlagI();
		// Check to see if interrupts got re-enabled
		if ((interrupts.irqs) != 0)
			interrupts.irqRequest = true;
		clock();
	}

	protected void clv_instr() {
		setFlagV((short) 0);
		clock();
	}

	protected void cmp_instr() {
		int /* uint_least16_t */tmp = (int /* uint_least16_t */) Register_Accumulator
				- Cycle_Data & 0xffff;
		setFlagsNZ((short) tmp);
		setFlagC((short) ((tmp < 0x100) ? 1 : 0));
		clock();
	}

	protected void cpx_instr() {
		int /* uint_least16_t */tmp = (int /* uint_least16_t */) Register_X
				- Cycle_Data & 0xffff;
		setFlagsNZ((short) tmp);
		setFlagC((short) ((tmp < 0x100) ? 1 : 0));
		clock();
	}

	protected void cpy_instr() {
		int /* uint_least16_t */tmp = (int /* uint_least16_t */) Register_Y
				- Cycle_Data & 0xffff;
		setFlagsNZ((short) tmp);
		setFlagC((short) ((tmp < 0x100) ? 1 : 0));
		clock();
	}

	/**
	 * Undocumented - This opcode DECs the contents of a memory location and
	 * then CMPs the result with the A register.
	 */
	protected void dcm_instr() {
		int /* uint_least16_t */tmp;
		PutEffAddrDataByte();
		Cycle_Data = (short) ((Cycle_Data-1) & 0xff);
		tmp = (int /* uint_least16_t */) Register_Accumulator - Cycle_Data;
		setFlagsNZ((short) tmp);
		setFlagC((short) ((tmp < 0x100) ? 1 : 0));
	}

	protected void dec_instr() {
		PutEffAddrDataByte();
		setFlagsNZ(Cycle_Data = (short) ((Cycle_Data-1) & 0xff));
	}

	protected void dex_instr() {
		setFlagsNZ(Register_X = (short) ((Register_X-1) & 0xff));
		clock();
	}

	protected void dey_instr() {
		setFlagsNZ(Register_Y = (short) ((Register_Y-1) & 0xff));
		clock();
	}

	protected void eor_instr() {
		setFlagsNZ(Register_Accumulator ^= Cycle_Data);
		clock();
	}

	// /**
	// * Not required - Operation performed By another method Undocumented - HLT
	// * crashes the microprocessor. When this opcode is executed, program
	// * execution ceases. No hardware interrupts will execute either. The
	// author
	// * has characterized this instruction as a halt instruction since this is
	// * the most straightforward explanation for this opcode's behavior. Only a
	// * reset will restart execution. This opcode leaves no trace of any
	// * operation performed! No registers affected.
	// */
	// protected void hlt_instr() {
	// ;
	// }

	protected void inc_instr() {
		PutEffAddrDataByte();
		setFlagsNZ(Cycle_Data = (short) ((Cycle_Data+1) & 0xff));
	}

	/**
	 * Undocumented - This opcode INCs the contents of a memory location and
	 * then SBCs the result from the A register.
	 */
	protected void ins_instr() {
		PutEffAddrDataByte();
		Cycle_Data++;
		Perform_SBC();
	}

	protected void inx_instr() {
		setFlagsNZ(Register_X = (short) ((Register_X+1) & 0xff));
		clock();
	}

	protected void iny_instr() {
		setFlagsNZ(Register_Y = (short) ((Register_Y+1) & 0xff));
		clock();
	}

	protected void jmp_instr() {
		Register_ProgramCounter = endian_32lo16(Register_ProgramCounter,
				Cycle_EffectiveAddress);

		if (PC64_TESTSUITE) {
			// Hack - Output character to screen
			int pc = endian_32lo16(Register_ProgramCounter);
			if (pc == 0xffd2) {
				char ch = _sidtune_CHRtab[Register_Accumulator];
				switch (ch) {
				case 0:
					break;
				case 1:
//					System.out.print(" ");
//					System.err.printf(" ");
					break;
				case 0xd:
//					System.out.print("\n");
//					System.err.printf("\n");
					filepos = 0;
					break;
				default:
					filetmp[filepos++] = ch;
//					System.out.printf("%c", Character.valueOf(ch));
//					System.err.printf("%c", Character.valueOf(ch));
				}
			}

			if (pc == 0xe16f) {
				filetmp[filepos] = '\0';
				envLoadFile(new String(filetmp, 0, filepos));
			}
		}

		clock();
	}

	protected void jsr_instr() {
		// JSR uses absolute addressing in this emulation,
		// hence the -1. The real SID does not use this addressing mode.
		Register_ProgramCounter--;
		PushHighPC();
	}

	/**
	 * Undocumented - This opcode ANDs the contents of a memory location with
	 * the contents of the stack pointer register and stores the result in the
	 * accumulator, the X register, and the stack pointer. Affected flags: N Z.
	 */
	protected void las_instr() {
		setFlagsNZ(Cycle_Data &= endian_16lo8(Register_StackPointer));
		Register_Accumulator = Cycle_Data;
		Register_X = Cycle_Data;
		Register_StackPointer = Cycle_Data;
		clock();
	}

	/**
	 * Undocumented - This opcode loads both the accumulator and the X register
	 * with the contents of a memory location.
	 */
	protected void lax_instr() {
		setFlagsNZ(Register_Accumulator = Register_X = Cycle_Data);
		clock();
	}

	protected void lda_instr() {
		setFlagsNZ(Register_Accumulator = Cycle_Data);
		clock();
	}

	protected void ldx_instr() {
		setFlagsNZ(Register_X = Cycle_Data);
		clock();
	}

	protected void ldy_instr() {
		setFlagsNZ(Register_Y = Cycle_Data);
		clock();
	}

	/**
	 * Undocumented - LSE LSRs the contents of a memory location and then EORs
	 * the result with the accumulator.
	 */
	protected void lse_instr() {
		PutEffAddrDataByte();
		setFlagC((short) (Cycle_Data & 0x01));
		Cycle_Data >>= 1;
		setFlagsNZ(Register_Accumulator ^= Cycle_Data);
	}

	protected void lsr_instr() {
		PutEffAddrDataByte();
		setFlagC((short) (Cycle_Data & 0x01));
		setFlagsNZ(Cycle_Data >>= 1);
	}

	protected void lsra_instr() {
		setFlagC((short) (Register_Accumulator & 0x01));
		setFlagsNZ(Register_Accumulator >>= 1);
		clock();
	}

	// /**
	// * Not required - Operation performed By another method
	// */
	// protected void nop_instr() {
	// ;
	// }

	/**
	 * Undocumented - This opcode ORs the A register with #xx, ANDs the result
	 * with an immediate value, and then stores the result in both A and X. xx
	 * may be EE,EF,FE, OR FF, but most emulators seem to use EE
	 */
	protected void oal_instr() {
		setFlagsNZ(Register_X = (Register_Accumulator = (short) (Cycle_Data & (Register_Accumulator | 0xee))));
		clock();
	}

	protected void ora_instr() {
		setFlagsNZ(Register_Accumulator |= Cycle_Data);
		clock();
	}

	protected void pha_instr() {
		int /* uint_least16_t */addr;
		addr = Register_StackPointer;
		addr = endian_16hi8(addr, SP_PAGE);
		envWriteMemByte(addr, Register_Accumulator);
		Register_StackPointer--;
	}

	// /**
	// * Not required - Operation performed By another method
	// */
	// protected void php_instr() {
	// ;
	// }

	protected void pla_instr() {
		int /* uint_least16_t */addr;
		Register_StackPointer++;
		addr = Register_StackPointer;
		addr = endian_16hi8(addr, SP_PAGE);
		setFlagsNZ(Register_Accumulator = envReadMemDataByte(addr));
	}

	/**
	 * Undocumented - RLA ROLs the contents of a memory location and then ANDs
	 * the result with the accumulator.
	 */
	protected void rla_instr() {
		short /* uint8_t */tmp = (short) (Cycle_Data & 0x80);
		PutEffAddrDataByte();
		Cycle_Data = (short) ((Cycle_Data << 1) & 0xff);
		if (getFlagC())
			Cycle_Data |= 0x01;
		setFlagC(tmp);
		setFlagsNZ(Register_Accumulator &= Cycle_Data);
	}

	protected void rol_instr() {
		short /* uint8_t */tmp = (short) (Cycle_Data & 0x80);
		PutEffAddrDataByte();
		Cycle_Data = (short) ((Cycle_Data << 1) & 0xff);
		if (getFlagC())
			Cycle_Data |= 0x01;
		setFlagsNZ(Cycle_Data);
		setFlagC(tmp);
	}

	protected void rola_instr() {
		short /* uint8_t */tmp = (short) (Register_Accumulator & 0x80);
		Register_Accumulator = (short) ((Register_Accumulator << 1) & 0xff);
		if (getFlagC())
			Register_Accumulator |= 0x01;
		setFlagsNZ(Register_Accumulator);
		setFlagC(tmp);
		clock();
	}

	protected void ror_instr() {
		short /* uint8_t */tmp = (short) (Cycle_Data & 0x01);
		PutEffAddrDataByte();
		Cycle_Data >>= 1;
		if (getFlagC())
			Cycle_Data |= 0x80;
		setFlagsNZ(Cycle_Data);
		setFlagC(tmp);
	}

	protected void rora_instr() {
		short /* uint8_t */tmp = (short) (Register_Accumulator & 0x01);
		Register_Accumulator >>= 1;
		if (getFlagC())
			Register_Accumulator |= 0x80;
		setFlagsNZ(Register_Accumulator);
		setFlagC(tmp);
		clock();
	}

	/**
	 * Undocumented - RRA RORs the contents of a memory location and then ADCs
	 * the result with the accumulator.
	 */
	protected void rra_instr() {
		short /* uint8_t */tmp = (short) (Cycle_Data & 0x01);
		PutEffAddrDataByte();
		Cycle_Data >>= 1;
		if (getFlagC())
			Cycle_Data |= 0x80;
		setFlagC(tmp);
		Perform_ADC();
	}

	/**
	 * RTI does not delay the IRQ I flag change as it is set 3 cycles before the
	 * end of the opcode, and thus the 6510 has enough time to call the
	 * interrupt routine as soon as the opcode ends, if necessary.
	 */
	protected void rti_instr() {
		if (MOS6510.isLoggable(Level.FINE)) {
			if (dodump)
				MOS6510
						.fine("****************************************************\n\n");
		}

		Register_ProgramCounter = endian_32lo16(Register_ProgramCounter,
				Cycle_EffectiveAddress);
		interrupts.irqLatch = false;
		clock();
	}

	protected void rts_instr() {
		Register_ProgramCounter = endian_32lo16(Register_ProgramCounter,
				Cycle_EffectiveAddress);
		Register_ProgramCounter++;
	}

	protected void sbx_instr() {
		long /* uint */tmp = (Register_X & Register_Accumulator) - Cycle_Data;
		setFlagsNZ((Register_X = (short) (tmp & 0xff)));
		setFlagC((short) ((tmp < 0x100) ? 1 : 0));
		clock();
	}

	/**
	 * Undocumented - This opcode ANDs the contents of the Y register with
	 * &lt;ab+1&gt; and stores the result in memory.
	 */
	protected void say_instr() {
		Cycle_Data = (short) (Register_Y & (endian_16hi8(Cycle_EffectiveAddress) + 1));
		PutEffAddrDataByte();
	}

	protected void sbc_instr() {
		Perform_SBC();
		clock();
	}

	protected void sec_instr() {
		setFlagC((short) 1);
		clock();
	}

	protected void sed_instr() {
		setFlagD((short) 1);
		clock();
	}

	protected void sei_instr() {
		boolean oldFlagI = getFlagI();
		setFlagI((short) 1);
		// I flag change is delayed by 1 instruction
		interrupts.irqLatch = oldFlagI ^ getFlagI();
		interrupts.irqRequest = false;
		clock();
	}

	/**
	 * Generic Instruction Undocumented Opcodes See documented 6502-nmo.opc by
	 * Adam Vardy for more details
	 */
	protected void shs_instr() {
		Register_StackPointer = endian_16lo8(Register_StackPointer,
				(short) (Register_Accumulator & Register_X));
		Cycle_Data = (short) ((endian_16hi8(Cycle_EffectiveAddress) + 1) & Register_StackPointer);
		PutEffAddrDataByte();
	}

	// /**
	// * Not required - Operation performed By another method<BR>
	// *
	// * Undocumented - skip next byte.
	// */
	// protected void skb_instr() {
	// Register_ProgramCounter++;
	// }

	protected void sta_instr() {
		Cycle_Data = Register_Accumulator;
		PutEffAddrDataByte();
	}

	protected void stx_instr() {
		Cycle_Data = Register_X;
		PutEffAddrDataByte();
	}

	protected void sty_instr() {
		Cycle_Data = Register_Y;
		PutEffAddrDataByte();
	}

	/**
	 * Undocumented - This opcode ANDs the contents of the A and X registers
	 * (without changing the contents of either register) and transfers the
	 * result to the stack pointer. It then ANDs that result with the contents
	 * of the high byte of the target address of the operand +1 and stores that
	 * final result in memory.
	 */
	protected void tas_instr() {
		Register_StackPointer = endian_16lo8(Register_StackPointer,
				(short) (Register_Accumulator & Register_X));
		int /* uint_least16_t */tmp = Register_StackPointer
				& (Cycle_EffectiveAddress + 1);
		Cycle_Data = /* (signed) */endian_16lo8(tmp);
	}

	protected void tax_instr() {
		setFlagsNZ(Register_X = Register_Accumulator);
		clock();
	}

	protected void tay_instr() {
		setFlagsNZ(Register_Y = Register_Accumulator);
		clock();
	}

	protected void tsx_instr() {
		// Rev 1.03 (saw) - Got these tsx and txs reversed
		setFlagsNZ(Register_X = endian_16lo8(Register_StackPointer));
		clock();
	}

	protected void txa_instr() {
		setFlagsNZ(Register_Accumulator = Register_X);
		clock();
	}

	protected void txs_instr() {
		// Rev 1.03 (saw) - Got these tsx and txs reversed
		Register_StackPointer = endian_16lo8(Register_StackPointer, Register_X);
		clock();
	}

	protected void tya_instr() {
		setFlagsNZ(Register_Accumulator = Register_Y);
		clock();
	}

	/**
	 * Undocumented - This opcode ANDs the contents of the X register with
	 * &lt;ab+1&gt; and stores the result in memory.
	 */
	protected void xas_instr() {
		Cycle_Data = (short) (Register_X & (endian_16hi8(Cycle_EffectiveAddress) + 1));
		PutEffAddrDataByte();
	}

	protected void illegal_instr() {
		MOS6510
				.log(Level.SEVERE, "\n\n ILLEGAL INSTRUCTION, resetting emulation. **************\n");
		DumpState();
		MOS6510
				.log(Level.SEVERE, "*********************************************************\n");
		// Perform Environment Reset
		envReset();
	}

	//
	// Generic Binary Coded Decimal Correction
	//

	protected void Perform_ADC() {
		int /* uint */C = getFlagC() ? 1 : 0;
		int /* uint */A = Register_Accumulator;
		int /* uint */s = Cycle_Data;
		int /* uint */regAC2 = A + s + C;

		if (getFlagD()) {
			// BCD mode
			int /* uint */lo = (A & 0x0f) + (s & 0x0f) + C;
			int /* uint */hi = (A & 0xf0) + (s & 0xf0);
			if (lo > 0x09)
				lo += 0x06;
			if (lo > 0x0f)
				hi += 0x10;

			setFlagZ((short) regAC2);
			setFlagN((short) hi);
			setFlagV((((hi ^ A) & 0x80) != 0) && (((A ^ s) & 0x80) == 0) ? (short) 1
					: (short) 0);
			if (hi > 0x90)
				hi += 0x60;

			setFlagC((hi > 0xff) ? (short) 1 : (short) 0);
			Register_Accumulator = (short) (hi | (lo & 0x0f));
		} else {
			// Binary mode
			setFlagC((regAC2 > 0xff) ? (short) 1 : (short) 0);
			setFlagV((((regAC2 ^ A) & 0x80) != 0) && (((A ^ s) & 0x80) == 0) ? (short) 1
					: (short) 0);
			setFlagsNZ(Register_Accumulator = (short) (regAC2 & 0xff));
		}
	}

	protected void Perform_SBC() {
		int /* uint */C = getFlagC() ? 0 : 1;
		int /* uint */A = Register_Accumulator;
		int /* uint */s = Cycle_Data;
		int /* uint */regAC2 = A - s - C & 0xffff;

		setFlagC((regAC2 < 0x100) ? (short) 1 : (short) 0);
		setFlagV(((((regAC2 ^ A) & 0x80) != 0) && (((A ^ s) & 0x80) != 0)) ? (short) 1
				: (short) 0);
		setFlagsNZ((short) regAC2);

		if (getFlagD()) {
			// BCD mode
			int /* uint */lo = (A & 0x0f) - (s & 0x0f) - C;
			int /* uint */hi = (A & 0xf0) - (s & 0xf0);
			if ((lo & 0x10) != 0) {
				lo -= 0x06;
				hi -= 0x10;
			}
			if ((hi & 0x100) != 0)
				hi -= 0x60;
			Register_Accumulator = (short) (hi | (lo & 0x0f));
		} else {
			// Binary mode
			Register_Accumulator = (short) (regAC2 & 0xff);
		}
	}

	/**
	 * Overridden in the Sub-class SID6510 for Sidplay compatibility
	 */
	protected void IRQRequest_sidplay_irq() {
		IRQRequest();
	}
	
	/**
	 * Overridden in the Sub-class SID6510 for Sidplay compatibility
	 */
	protected void PushHighPC_sidplay_brk() {
		PushHighPC();
	}

	/**
	 * Overridden in the Sub-class SID6510 for Sidplay compatibility
	 */
	protected void PopSR_sidplay_rti() {
		PopSR();
	}

	/**
	 * Initialize and create CPU Chip
	 * 
	 * @param context
	 */
	public MOS6510(IEventContext context) {
		eventContext = context;
		m_phase = Event.event_phase_t.EVENT_CLOCK_PHI2;
		m_extPhase = Event.event_phase_t.EVENT_CLOCK_PHI1;
		ProcessorOperations instr;
		boolean /* uint8_t */legalMode = true;
		boolean /* uint8_t */legalInstr = true;

		// ----------------------------------------------------------------------
		// Build up the processor instruction table
		for (int i = 0; i < 0x100; i++) {
			if (MOS6510.isLoggable(Level.FINE)) {
				MOS6510.fine(String.format("Building Command %d[%02x]..", Integer.valueOf(i), Integer.valueOf(i)));
			}

			// Pass 1 allocates the memory, Pass 2 builds the instruction
			instr = instrTable[i] = new ProcessorOperations();
			procCycle = null;

			for (int pass = 0; pass < 2; pass++) {
				int WRITE = 0;
				int READ = 1;
				int access = WRITE;
				cycleCount = -1;
				legalMode = true;
				legalInstr = true;

				switch ((int) i) {
				// Accumulator or Implied addressing
				case ASLn:
				case CLCn:
				case CLDn:
				case CLIn:
				case CLVn:
				case DEXn:
				case DEYn:
				case INXn:
				case INYn:
				case LSRn:
				case NOPn:
				case NOPn_1:
				case NOPn_2:
				case NOPn_3:
				case NOPn_4:
				case NOPn_5:
				case NOPn_6:
				case PHAn:
				case PHPn:
				case PLAn:
				case PLPn:
				case ROLn:
				case RORn:
				case RTIn:
				case RTSn:
				case SECn:
				case SEDn:
				case SEIn:
				case TAXn:
				case TAYn:
				case TSXn:
				case TXAn:
				case TXSn:
				case TYAn:
					cycleCount++;
					if ((pass) != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								WasteCycle();
							}

						};
					break;

				// Immediate and Relative Addressing Mode Handler
				case ADCb:
				case ANDb:
				case ANCb:
				case ANCb_1:
				case ANEb:
				case ASRb:
				case ARRb:
				case BCCr:
				case BCSr:
				case BEQr:
				case BMIr:
				case BNEr:
				case BPLr:
				case BRKn:
				case BVCr:
				case BVSr:
				case CMPb:
				case CPXb:
				case CPYb:
				case EORb:
				case LDAb:
				case LDXb:
				case LDYb:
				case LXAb:
				case NOPb:
				case NOPb_1:
				case NOPb_2:
				case NOPb_3:
				case NOPb_4:
				case ORAb:
				case SBCb:
				case SBCb_1:
				case SBXb:
					cycleCount++;
					if ((pass) != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								FetchDataByte();
							}

						};
					break;

				// Zero Page Addressing Mode Handler - Read & RMW
				case ADCz:
				case ANDz:
				case BITz:
				case CMPz:
				case CPXz:
				case CPYz:
				case EORz:
				case LAXz:
				case LDAz:
				case LDXz:
				case LDYz:
				case ORAz:
				case NOPz:
				case NOPz_1:
				case NOPz_2:
				case SBCz:
				case ASLz:
				case DCPz:
				case DECz:
				case INCz:
				case ISBz:
				case LSRz:
				case ROLz:
				case RORz:
				case SREz:
				case SLOz:
				case RLAz:
				case RRAz:
					access++;
				case SAXz:
				case STAz:
				case STXz:
				case STYz:
					cycleCount++;
					if ((pass) != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								FetchLowAddr();
							}

						};
					if (access == READ) {
						cycleCount++;
						if ((pass) != 0)
							procCycle[cycleCount].func = new IFunc() {

								public void invoke() {
									FetchEffAddrDataByte();
								}

							};
					}
					break;

				// Zero Page with X Offset Addressing Mode Handler
				case ADCzx:
				case ANDzx:
				case CMPzx:
				case EORzx:
				case LDAzx:
				case LDYzx:
				case NOPzx:
				case NOPzx_1:
				case NOPzx_2:
				case NOPzx_3:
				case NOPzx_4:
				case NOPzx_5:
				case ORAzx:
				case SBCzx:
				case ASLzx:
				case DCPzx:
				case DECzx:
				case INCzx:
				case ISBzx:
				case LSRzx:
				case RLAzx:
				case ROLzx:
				case RORzx:
				case RRAzx:
				case SLOzx:
				case SREzx:
					access++;
				case STAzx:
				case STYzx:
					cycleCount++;
					if ((pass) != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								FetchLowAddrX();
							}

						};
					cycleCount++;
					if ((pass) != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								WasteCycle();
							}

						};
					if (access == READ) {
						cycleCount++;
						if ((pass) != 0)
							procCycle[cycleCount].func = new IFunc() {

								public void invoke() {
									FetchEffAddrDataByte();
								}

							};
					}
					break;

				// Zero Page with Y Offset Addressing Mode Handler
				case LDXzy:
				case LAXzy:
					access = READ;
				case STXzy:
				case SAXzy:
					cycleCount++;
					if ((pass) != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								FetchLowAddrY();
							}

						};
					cycleCount++;
					if ((pass) != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								WasteCycle();
							}

						};
					if (access == READ) {
						cycleCount++;
						if ((pass) != 0)
							procCycle[cycleCount].func = new IFunc() {

								public void invoke() {
									FetchEffAddrDataByte();
								}

							};
					}
					break;

				// Absolute Addressing Mode Handler
				case ADCa:
				case ANDa:
				case BITa:
				case CMPa:
				case CPXa:
				case CPYa:
				case EORa:
				case LAXa:
				case LDAa:
				case LDXa:
				case LDYa:
				case NOPa:
				case ORAa:
				case SBCa:
				case ASLa:
				case DCPa:
				case DECa:
				case INCa:
				case ISBa:
				case LSRa:
				case ROLa:
				case RORa:
				case SLOa:
				case SREa:
				case RLAa:
				case RRAa:
					access++;
				case JMPw:
				case JSRw:
				case SAXa:
				case STAa:
				case STXa:
				case STYa:
					cycleCount++;
					if ((pass) != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								FetchLowAddr();
							}

						};
					cycleCount++;
					if ((pass) != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								FetchHighAddr();
							}

						};
					if (access == READ) {
						cycleCount++;
						if ((pass) != 0)
							procCycle[cycleCount].func = new IFunc() {

								public void invoke() {
									FetchEffAddrDataByte();
								}

							};
					}
					break;

				// Absolute With X Offset Addressing Mode Handler (Read)
				case ADCax:
				case ANDax:
				case CMPax:
				case EORax:
				case LDAax:
				case LDYax:
				case NOPax:
				case NOPax_1:
				case NOPax_2:
				case NOPax_3:
				case NOPax_4:
				case NOPax_5:
				case ORAax:
				case SBCax:
					cycleCount++;
					if ((pass) != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								FetchLowAddr();
							}

						};
					cycleCount++;
					if ((pass) != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								FetchHighAddrX();
							}

						};
					cycleCount++;
					if ((pass) != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								WasteCycle();
							}

						};
					cycleCount++;
					if ((pass) != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								FetchEffAddrDataByte();
							}

						};
					break;

				// Absolute X (No page crossing handled)
				case ASLax:
				case DCPax:
				case DECax:
				case INCax:
				case ISBax:
				case LSRax:
				case RLAax:
				case ROLax:
				case RORax:
				case RRAax:
				case SLOax:
				case SREax:
					access = READ;
				case SHYax:
				case STAax:
					cycleCount++;
					if ((pass) != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								FetchLowAddr();
							}

						};
					cycleCount++;
					if ((pass) != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								FetchHighAddrX2();
							}

						};
					cycleCount++;
					if ((pass) != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								WasteCycle();
							}

						};
					if (access == READ) {
						cycleCount++;
						if ((pass) != 0)
							procCycle[cycleCount].func = new IFunc() {

								public void invoke() {
									FetchEffAddrDataByte();
								}

							};
					}
					break;

				// Absolute With Y Offset Addressing Mode Handler (Read)
				case ADCay:
				case ANDay:
				case CMPay:
				case EORay:
				case LASay:
				case LAXay:
				case LDAay:
				case LDXay:
				case ORAay:
				case SBCay:
					cycleCount++;
					if ((pass) != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								FetchLowAddr();
							}

						};
					cycleCount++;
					if ((pass) != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								FetchHighAddrY();
							}

						};
					cycleCount++;
					if ((pass) != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								WasteCycle();
							}

						};
					cycleCount++;
					if ((pass) != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								FetchEffAddrDataByte();
							}

						};
					break;

				// Absolute Y (No page crossing handled)
				case DCPay:
				case ISBay:
				case RLAay:
				case RRAay:
				case SLOay:
				case SREay:
					access = READ;
				case SHAay:
				case SHSay:
				case SHXay:
				case STAay:
					cycleCount++;
					if ((pass) != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								FetchLowAddr();
							}

						};
					cycleCount++;
					if ((pass) != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								FetchHighAddrY2();
							}

						};
					cycleCount++;
					if ((pass) != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								WasteCycle();
							}

						};
					if (access == READ) {
						cycleCount++;
						if ((pass) != 0)
							procCycle[cycleCount].func = new IFunc() {

								public void invoke() {
									FetchEffAddrDataByte();
								}

							};
					}
					break;

				// Absolute Indirect Addressing Mode Handler
				case JMPi:
					cycleCount++;
					if ((pass) != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								FetchLowPointer();
							}

						};
					cycleCount++;
					if ((pass) != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								FetchHighPointer();
							}

						};
					cycleCount++;
					if ((pass) != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								FetchLowEffAddr();
							}

						};
					cycleCount++;
					if ((pass) != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								FetchHighEffAddr();
							}

						};
					break;

				// Indexed with X Preinc Addressing Mode Handler
				case ADCix:
				case ANDix:
				case CMPix:
				case EORix:
				case LAXix:
				case LDAix:
				case ORAix:
				case SBCix:
				case DCPix:
				case ISBix:
				case SLOix:
				case SREix:
				case RLAix:
				case RRAix:
					access++;
				case SAXix:
				case STAix:
					cycleCount++;
					if ((pass) != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								FetchLowPointer();
							}

						};
					cycleCount++;
					if ((pass) != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								FetchLowPointerX();
							}

						};
					cycleCount++;
					if ((pass) != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								FetchLowEffAddr();
							}

						};
					cycleCount++;
					if ((pass) != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								FetchHighEffAddr();
							}

						};
					if (access == READ) {
						cycleCount++;
						if ((pass) != 0)
							procCycle[cycleCount].func = new IFunc() {

								public void invoke() {
									FetchEffAddrDataByte();
								}

							};
					}
					break;

				// Indexed with Y Postinc Addressing Mode Handler (Read)
				case ADCiy:
				case ANDiy:
				case CMPiy:
				case EORiy:
				case LAXiy:
				case LDAiy:
				case ORAiy:
				case SBCiy:
					cycleCount++;
					if ((pass) != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								FetchLowPointer();
							}

						};
					cycleCount++;
					if ((pass) != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								FetchLowEffAddr();
							}

						};
					cycleCount++;
					if ((pass) != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								FetchHighEffAddrY();
							}

						};
					cycleCount++;
					if ((pass) != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								WasteCycle();
							}

						};
					cycleCount++;
					if ((pass) != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								FetchEffAddrDataByte();
							}

						};
					break;

				// Indexed Y (No page crossing handled)
				case DCPiy:
				case ISBiy:
				case RLAiy:
				case RRAiy:
				case SLOiy:
				case SREiy:
					access = READ;
				case SHAiy:
				case STAiy:
					cycleCount++;
					if ((pass) != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								FetchLowPointer();
							}

						};
					cycleCount++;
					if ((pass) != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								FetchLowEffAddr();
							}

						};
					cycleCount++;
					if ((pass) != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								FetchHighEffAddrY2();
							}

						};
					cycleCount++;
					if ((pass) != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								WasteCycle();
							}

						};
					if (access == READ) {
						cycleCount++;
						if ((pass) != 0)
							procCycle[cycleCount].func = new IFunc() {

								public void invoke() {
									FetchEffAddrDataByte();
								}

							};
					}
					break;

				default:
					legalMode = false;
					break;
				}

				if ((pass) != 0) {
					// Everything up to now is reads and can
					// therefore be blocked through cycle stealing
					for (int c = -1; c < cycleCount;)
						procCycle[++c].nosteal = false;
				}

				if (MOS6510.isLoggable(Level.FINE)) {
					if (legalMode) {
						cycleCount++;
						if (pass != 0)
							procCycle[cycleCount].func = new IFunc() {

								public void invoke() {
									DebugCycle();
								}

							};
					}
				}

				// ---------------------------------------------------------------------------------------
				// Addressing Modes Finished, other cycles are instruction
				// dependent
				switch ((int) i) {
				case ADCz:
				case ADCzx:
				case ADCa:
				case ADCax:
				case ADCay:
				case ADCix:
				case ADCiy:
				case ADCb:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								adc_instr();
							}

						};
					break;

				case ANCb:
				case ANCb_1:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								anc_instr();
							}

						};
					break;

				case ANDz:
				case ANDzx:
				case ANDa:
				case ANDax:
				case ANDay:
				case ANDix:
				case ANDiy:
				case ANDb:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								and_instr();
							}

						};
					break;

				case ANEb: // Also known as XAA
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								ane_instr();
							}

						};
					break;

				case ARRb:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								arr_instr();
							}

						};
					break;

				case ASLn:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								asla_instr();
							}

						};
					break;

				case ASLz:
				case ASLzx:
				case ASLa:
				case ASLax:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								asl_instr();
							}

						};
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								PutEffAddrDataByte();
							}

						};
					break;

				case ASRb: // Also known as ALR
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								alr_instr();
							}

						};
					break;

				case BCCr:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								bcc_instr();
							}

						};
					if (MOS6510_ACCURATE_CYCLES) {
						cycleCount++;
						if (pass != 0)
							procCycle[cycleCount].func = new IFunc() {

								public void invoke() {
									branch2_instr();
								}

							};
						cycleCount++;
						if (pass != 0)
							procCycle[cycleCount].func = new IFunc() {

								public void invoke() {
									WasteCycle();
								}

							};
					}
					break;

				case BCSr:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								bcs_instr();
							}

						};
					if (MOS6510_ACCURATE_CYCLES) {
						cycleCount++;
						if (pass != 0)
							procCycle[cycleCount].func = new IFunc() {

								public void invoke() {
									branch2_instr();
								}

							};

						cycleCount++;
						if (pass != 0)
							procCycle[cycleCount].func = new IFunc() {

								public void invoke() {
									WasteCycle();
								}

							};
					}
					break;

				case BEQr:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								beq_instr();
							}

						};
					if (MOS6510_ACCURATE_CYCLES) {
						cycleCount++;
						if (pass != 0)
							procCycle[cycleCount].func = new IFunc() {

								public void invoke() {
									branch2_instr();
								}

							};
						cycleCount++;
						if (pass != 0)
							procCycle[cycleCount].func = new IFunc() {

								public void invoke() {
									WasteCycle();
								}

							};
					}
					break;

				case BITz:
				case BITa:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								bit_instr();
							}

						};
					break;

				case BMIr:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								bmi_instr();
							}

						};
					if (MOS6510_ACCURATE_CYCLES) {
						cycleCount++;
						if (pass != 0)
							procCycle[cycleCount].func = new IFunc() {

								public void invoke() {
									branch2_instr();
								}

							};
						cycleCount++;
						if (pass != 0)
							procCycle[cycleCount].func = new IFunc() {

								public void invoke() {
									WasteCycle();
								}

							};
					}
					break;

				case BNEr:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								bne_instr();
							}

						};
					if (MOS6510_ACCURATE_CYCLES) {
						cycleCount++;
						if (pass != 0)
							procCycle[cycleCount].func = new IFunc() {

								public void invoke() {
									branch2_instr();
								}

							};
						cycleCount++;
						if (pass != 0)
							procCycle[cycleCount].func = new IFunc() {

								public void invoke() {
									WasteCycle();
								}

							};
					}
					break;

				case BPLr:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								bpl_instr();
							}

						};
					if (MOS6510_ACCURATE_CYCLES) {
						cycleCount++;
						if (pass != 0)
							procCycle[cycleCount].func = new IFunc() {

								public void invoke() {
									branch2_instr();
								}

							};
						cycleCount++;
						if (pass != 0)
							procCycle[cycleCount].func = new IFunc() {

								public void invoke() {
									WasteCycle();
								}

							};
					}
					break;

				case BRKn:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								PushHighPC_sidplay_brk();
							}

						};
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								PushLowPC();
							}

						};
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								brk_instr();
							}

						};
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								IRQ1Request();
							}

						};
					if (pass != 0)
						procCycle[cycleCount].nosteal = false;
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								IRQ2Request();
							}

						};
					if (pass != 0)
						procCycle[cycleCount].nosteal = false;
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								FetchOpcode();
							}

						};
					break;

				case BVCr:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								bvc_instr();
							}

						};
					if (MOS6510_ACCURATE_CYCLES) {
						cycleCount++;
						if (pass != 0)
							procCycle[cycleCount].func = new IFunc() {

								public void invoke() {
									branch2_instr();
								}

							};
						cycleCount++;
						if (pass != 0)
							procCycle[cycleCount].func = new IFunc() {

								public void invoke() {
									WasteCycle();
								}

							};
					}
					break;

				case BVSr:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								bvs_instr();
							}

						};
					if (MOS6510_ACCURATE_CYCLES) {
						cycleCount++;
						if (pass != 0)
							procCycle[cycleCount].func = new IFunc() {

								public void invoke() {
									branch2_instr();
								}

							};
						cycleCount++;
						if (pass != 0)
							procCycle[cycleCount].func = new IFunc() {

								public void invoke() {
									WasteCycle();
								}

							};
					}
					break;

				case CLCn:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								clc_instr();
							}

						};
					break;

				case CLDn:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								cld_instr();
							}

						};
					break;

				case CLIn:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								cli_instr();
							}

						};
					break;

				case CLVn:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								clv_instr();
							}

						};
					break;

				case CMPz:
				case CMPzx:
				case CMPa:
				case CMPax:
				case CMPay:
				case CMPix:
				case CMPiy:
				case CMPb:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								cmp_instr();
							}

						};
					break;

				case CPXz:
				case CPXa:
				case CPXb:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								cpx_instr();
							}

						};
					break;

				case CPYz:
				case CPYa:
				case CPYb:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								cpy_instr();
							}

						};
					break;

				case DCPz:
				case DCPzx:
				case DCPa:
				case DCPax:
				case DCPay:
				case DCPix:
				case DCPiy: // Also known as DCM
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								dcm_instr();
							}

						};
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								PutEffAddrDataByte();
							}

						};
					break;

				case DECz:
				case DECzx:
				case DECa:
				case DECax:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								dec_instr();
							}

						};
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								PutEffAddrDataByte();
							}

						};
					break;

				case DEXn:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								dex_instr();
							}

						};
					break;

				case DEYn:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								dey_instr();
							}

						};
					break;

				case EORz:
				case EORzx:
				case EORa:
				case EORax:
				case EORay:
				case EORix:
				case EORiy:
				case EORb:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								eor_instr();
							}

						};
					break;

				/*
				 * HLT // Also known as JAM case 0x02: case 0x12: case 0x22:
				 * case 0x32: case 0x42: case 0x52: case 0x62: case 0x72: case
				 * 0x92: case 0xb2: case 0xd2: case 0xf2: case 0x02: case 0x12:
				 * case 0x22: case 0x32: case 0x42: case 0x52: case 0x62: case
				 * 0x72: case 0x92: case 0xb2: case 0xd2: case 0xf2:
				 * cycleCount++; if (pass != 0)
				 * procCycle[cycleCount].func=hlt_instrMethod; break;
				 */

				case INCz:
				case INCzx:
				case INCa:
				case INCax:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								inc_instr();
							}

						};
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								PutEffAddrDataByte();
							}

						};
					break;

				case INXn:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								inx_instr();
							}

						};
					break;

				case INYn:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								iny_instr();
							}

						};
					break;

				case ISBz:
				case ISBzx:
				case ISBa:
				case ISBax:
				case ISBay:
				case ISBix:
				case ISBiy: // Also known as INS
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								ins_instr();
							}

						};
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								PutEffAddrDataByte();
							}

						};
					break;

				case JSRw:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								jsr_instr();
							}

						};
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								PushLowPC();
							}

						};
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								WasteCycle();
							}

						};
				case JMPw:
				case JMPi:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								jmp_instr();
							}

						};
					break;

				case LASay:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								las_instr();
							}

						};
					break;

				case LAXz:
				case LAXzy:
				case LAXa:
				case LAXay:
				case LAXix:
				case LAXiy:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								lax_instr();
							}

						};
					break;

				case LDAz:
				case LDAzx:
				case LDAa:
				case LDAax:
				case LDAay:
				case LDAix:
				case LDAiy:
				case LDAb:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								lda_instr();
							}

						};
					break;

				case LDXz:
				case LDXzy:
				case LDXa:
				case LDXay:
				case LDXb:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								ldx_instr();
							}

						};
					break;

				case LDYz:
				case LDYzx:
				case LDYa:
				case LDYax:
				case LDYb:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								ldy_instr();
							}

						};
					break;

				case LSRn:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								lsra_instr();
							}

						};
					break;

				case LSRz:
				case LSRzx:
				case LSRa:
				case LSRax:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								lsr_instr();
							}

						};
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								PutEffAddrDataByte();
							}

						};
					break;

				case NOPn:
				case NOPn_1:
				case NOPn_2:
				case NOPn_3:
				case NOPn_4:
				case NOPn_5:
				case NOPn_6:
				case NOPb:
				case NOPb_1:
				case NOPb_2:
				case NOPb_3:
				case NOPb_4:
				case NOPz:
				case NOPz_1:
				case NOPz_2:
				case NOPzx:
				case NOPzx_1:
				case NOPzx_2:
				case NOPzx_3:
				case NOPzx_4:
				case NOPzx_5:
				case NOPa:
				case NOPax:
				case NOPax_1:
				case NOPax_2:
				case NOPax_3:
				case NOPax_4:
				case NOPax_5:
					// NOPb NOPz NOPzx - Also known as SKBn
					// NOPa NOPax - Also known as SKWn
					break;

				case LXAb: // Also known as OAL
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								oal_instr();
							}

						};
					break;

				case ORAz:
				case ORAzx:
				case ORAa:
				case ORAax:
				case ORAay:
				case ORAix:
				case ORAiy:
				case ORAb:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								ora_instr();
							}

						};
					break;

				case PHAn:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								pha_instr();
							}

						};
					break;

				case PHPn:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								PushSR();
							}

						};
					break;

				case PLAn:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								WasteCycle();
							}

						};
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								pla_instr();
							}

						};
					if (pass != 0)
						procCycle[cycleCount].nosteal = false;
					break;

				case PLPn:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								WasteCycle();
							}

						};
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								PopSR();
							}

						};
					if (pass != 0)
						procCycle[cycleCount].nosteal = false;
					break;

				case RLAz:
				case RLAzx:
				case RLAix:
				case RLAa:
				case RLAax:
				case RLAay:
				case RLAiy:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								rla_instr();
							}

						};
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								PutEffAddrDataByte();
							}

						};
					break;

				case ROLn:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								rola_instr();
							}

						};
					break;

				case ROLz:
				case ROLzx:
				case ROLa:
				case ROLax:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								rol_instr();
							}

						};
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								PutEffAddrDataByte();
							}

						};
					break;

				case RORn:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								rora_instr();
							}

						};
					break;

				case RORz:
				case RORzx:
				case RORa:
				case RORax:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								ror_instr();
							}

						};
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								PutEffAddrDataByte();
							}

						};
					break;

				case RRAa:
				case RRAax:
				case RRAay:
				case RRAz:
				case RRAzx:
				case RRAix:
				case RRAiy:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								rra_instr();
							}

						};
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								PutEffAddrDataByte();
							}

						};
					break;

				case RTIn:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								WasteCycle();
							}

						};
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								PopSR_sidplay_rti();
							}

						};
					if (pass != 0)
						procCycle[cycleCount].nosteal = false;
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								PopLowPC();
							}

						};
					if (pass != 0)
						procCycle[cycleCount].nosteal = false;
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								PopHighPC();
							}

						};
					if (pass != 0)
						procCycle[cycleCount].nosteal = false;
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								rti_instr();
							}

						};
					break;

				case RTSn:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								WasteCycle();
							}

						};
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								PopLowPC();
							}

						};
					if (pass != 0)
						procCycle[cycleCount].nosteal = false;
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								PopHighPC();
							}

						};
					if (pass != 0)
						procCycle[cycleCount].nosteal = false;
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								rts_instr();
							}

						};
					break;

				case SAXz:
				case SAXzy:
				case SAXa:
				case SAXix: // Also known as AXS
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								axs_instr();
							}

						};
					break;

				case SBCz:
				case SBCzx:
				case SBCa:
				case SBCax:
				case SBCay:
				case SBCix:
				case SBCiy:
				case SBCb:
				case SBCb_1:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								sbc_instr();
							}

						};
					break;

				case SBXb:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								sbx_instr();
							}

						};
					break;

				case SECn:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								sec_instr();
							}

						};
					break;

				case SEDn:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								sed_instr();
							}

						};
					break;

				case SEIn:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								sei_instr();
							}

						};
					break;

				case SHAay:
				case SHAiy: // Also known as AXA
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								axa_instr();
							}

						};
					break;

				case SHSay: // Also known as TAS
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								shs_instr();
							}

						};
					break;

				case SHXay: // Also known as XAS
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								xas_instr();
							}

						};
					break;

				case SHYax: // Also known as SAY
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								say_instr();
							}

						};
					break;

				case SLOz:
				case SLOzx:
				case SLOa:
				case SLOax:
				case SLOay:
				case SLOix:
				case SLOiy: // Also known as ASO
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								aso_instr();
							}

						};
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								PutEffAddrDataByte();
							}

						};
					break;

				case SREz:
				case SREzx:
				case SREa:
				case SREax:
				case SREay:
				case SREix:
				case SREiy: // Also known as LSE
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								lse_instr();
							}

						};
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								PutEffAddrDataByte();
							}

						};
					break;

				case STAz:
				case STAzx:
				case STAa:
				case STAax:
				case STAay:
				case STAix:
				case STAiy:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								sta_instr();
							}

						};
					break;

				case STXz:
				case STXzy:
				case STXa:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								stx_instr();
							}

						};
					break;

				case STYz:
				case STYzx:
				case STYa:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								sty_instr();
							}

						};
					break;

				case TAXn:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								tax_instr();
							}

						};
					break;

				case TAYn:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								tay_instr();
							}

						};
					break;

				case TSXn:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								tsx_instr();
							}

						};
					break;

				case TXAn:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								txa_instr();
							}

						};
					break;

				case TXSn:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								txs_instr();
							}

						};
					break;

				case TYAn:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								tya_instr();
							}

						};
					break;

				default:
					legalInstr = false;
					break;
				}

				if (!(legalMode || legalInstr)) {
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								illegal_instr();
							}

						};
				} else if (!(legalMode && legalInstr)) {
					System.err.printf(
							"\nInstruction 0x%x: Not built correctly.\n\n", Integer.valueOf(i));
					throw new RuntimeException("MOS6510 ERROR: no legal mode nor legal instruction");
				}

				cycleCount++;
				if (pass != 0)
					procCycle[cycleCount].func = new IFunc() {

						public void invoke() {
							NextInstr();
						}

					};
				cycleCount++;
				if (pass == 0) {
					// Pass 1 - Allocate Memory
					if ((cycleCount) != 0) {
						instr.cycle = new ProcessorCycle[cycleCount];
						procCycle = instr.cycle;

						int c = cycleCount;
						while (c > 0) {
							procCycle[--c] = new ProcessorCycle();
							procCycle[c].nosteal = true;
						}
					}
				} 
//				else
//					instr.opcode = (short) i;

				if (MOS6510.isLoggable(Level.FINE)) {
					MOS6510.fine(".");
				}
			}

			//instr.cycles = cycleCount;
			if (MOS6510.isLoggable(Level.FINE)) {
				MOS6510.fine(String.format("Done [%d Cycles]\n", Byte.valueOf(cycleCount)));
			}
		}

		// ----------------------------------------------------------------------
		// Build interrupts
		for (int i = 0; i < 3; i++) {
			if (MOS6510.isLoggable(Level.FINE)) {
				MOS6510.fine(String.format("Building Interrupt %d[%02x]..", Integer.valueOf(i), Integer.valueOf(i)));
			}

			// Pass 1 allocates the memory, Pass 2 builds the interrupt
			instr = interruptTable[i] = new ProcessorOperations();
			instr.cycle = null;
			//instr.opcode = 0;

			for (int pass = 0; pass < 2; pass++) {
				cycleCount = -1;
				if (pass != 0)
					procCycle = instr.cycle;

				switch (i) {
				case oRST:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								WasteCycle();
							}

						};
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								WasteCycle();
							}

						};
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								WasteCycle();
							}

						};
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								WasteCycle();
							}

						};
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								WasteCycle();
							}

						};
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								RSTRequest();
							}

						};
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								FetchOpcode();
							}

						};
					break;

				case oNMI:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								WasteCycle();
							}

						};
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								WasteCycle();
							}

						};
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								PushHighPC();
							}

						};
					if (pass != 0)
						procCycle[cycleCount].nosteal = true;
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								PushLowPC();
							}

						};
					if (pass != 0)
						procCycle[cycleCount].nosteal = true;
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								IRQRequest();
							}

						};
					if (pass != 0)
						procCycle[cycleCount].nosteal = true;
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								NMIRequest();
							}

						};
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								NMI1Request();
							}

						};
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								FetchOpcode();
							}

						};
					break;

				case oIRQ:
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								WasteCycle();
							}

						};
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								WasteCycle();
							}

						};
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								PushHighPC();
							}

						};
					if (pass != 0)
						procCycle[cycleCount].nosteal = true;
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								PushLowPC();
							}

						};
					if (pass != 0)
						procCycle[cycleCount].nosteal = true;
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								IRQRequest_sidplay_irq();
							}

						};
					if (pass != 0)
						procCycle[cycleCount].nosteal = true;
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								IRQ1Request();
							}

						};
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								IRQ2Request();
							}

						};
					cycleCount++;
					if (pass != 0)
						procCycle[cycleCount].func = new IFunc() {

							public void invoke() {
								FetchOpcode();
							}

						};
					break;
				}

				cycleCount++;
				if (pass == 0) {
					// Pass 1 - Allocate Memory
					if (cycleCount != 0) {
						instr.cycle = new ProcessorCycle[cycleCount];
						procCycle = instr.cycle;
						for (int c = 0; c < cycleCount; c++) {
							procCycle[c] = new ProcessorCycle();
							procCycle[c].nosteal = false;
						}
					}
				}

				if (MOS6510.isLoggable(Level.FINE)) {
					MOS6510.fine(".");
				}
			}

			//instr.cycles = cycleCount;
			if (MOS6510.isLoggable(Level.FINE)) {
				MOS6510.fine(String.format("Done [%d Cycles]\n", Byte.valueOf(cycleCount)));
			}
		}

		// Initialize Processor Registers
		Register_Accumulator = 0;
		Register_X = 0;
		Register_Y = 0;

		Cycle_EffectiveAddress = 0;
		Cycle_Data = 0;
		fetchCycle.func = new IFunc() {

			public void invoke() {
				FetchOpcode();
			}

		};

		dodump = false;
		Initialise();
	}

	/**
	 * Reset CPU Emulation
	 */
	public void reset() {
		// Reset Interrupts
		interrupts.pending = 0;
		interrupts.irqs = 0;

		// Internal Stuff
		Initialise();

		// Requires External Bits
		// Read from reset vector for program entry point
		Cycle_EffectiveAddress = endian_16lo8(Cycle_EffectiveAddress,
				envReadMemDataByte(0xFFFC));
		Cycle_EffectiveAddress = endian_16hi8(Cycle_EffectiveAddress,
				envReadMemDataByte(0xFFFD));
		Register_ProgramCounter = Cycle_EffectiveAddress;
		// filepos = 0;
	}

	/**
	 * Module Credits
	 * 
	 * @param sbuffer
	 */
	public void credits(StringBuffer sbuffer) { // Copy credits to buffer

		sbuffer.append(String
				.format("Module     : MOS6510 Cycle Exact Emulation\n"));
		sbuffer.append(String.format("Written By : %s\n", MOS6510_AUTHOR));
		sbuffer.append(String.format("Version    : %s\n", MOS6510_VERSION));
		sbuffer.append(String.format("Released   : %s\n", MOS6510_DATE));
		sbuffer.append(String.format("Email      : %s\n", MOS6510_EMAIL));
	}
	int line = 0;
	public void DumpState() {
		short /* uint8_t */opcode, data;
		int /* uint_least16_t */operand, address;

		StringBuffer m_fdbg = new StringBuffer();
		m_fdbg.append(String.format("%5d :", Integer.valueOf(line++))); 
	    m_fdbg.append(String.format(" PC  I  A  X  Y  SP  DR PR NV-BDIZC  Instruction (%d)\n", Long.valueOf(m_dbgClk)));
		m_fdbg.append(String.format("XXXXXXX%04x ", Integer.valueOf(instrStartPC)));
		m_fdbg.append(String.format("%d ", Short.valueOf(interrupts.irqs)));
		m_fdbg.append(String.format("%02x ", Short.valueOf(Register_Accumulator)));
		m_fdbg.append(String.format("%02x ", Short.valueOf(Register_X)));
		m_fdbg.append(String.format("%02x ", Short.valueOf(Register_Y)));
		m_fdbg.append(String.format("01%02x ", Short.valueOf(endian_16lo8(Register_StackPointer))));
		m_fdbg.append(String.format("%02x ", Short.valueOf(envReadMemDataByte(0))));
		m_fdbg.append(String.format("%02x ", Short.valueOf(envReadMemDataByte(1))));

		if (getFlagN())
			m_fdbg.append("1");
		else
			m_fdbg.append("0");
		if (getFlagV())
			m_fdbg.append("1");
		else
			m_fdbg.append("0");
		if ((Register_Status & (1 << SR_NOTUSED)) != 0)
			m_fdbg.append("1");
		else
			m_fdbg.append("0");
		if ((Register_Status & (1 << SR_BREAK)) != 0)
			m_fdbg.append("1");
		else
			m_fdbg.append("0");
		if (getFlagD())
			m_fdbg.append("1");
		else
			m_fdbg.append("0");
		if (getFlagI())
			m_fdbg.append("1");
		else
			m_fdbg.append("0");
		if (getFlagZ())
			m_fdbg.append("1");
		else
			m_fdbg.append("0");
		if (getFlagC())
			m_fdbg.append("1");
		else
			m_fdbg.append("0");

		opcode = instrOpcode;
		operand = Instr_Operand;
		data = Cycle_Data;

		switch (opcode) {
		case BCCr:
		case BCSr:
		case BEQr:
		case BMIr:
		case BNEr:
		case BPLr:
		case BVCr:
		case BVSr:
			address = (int /* uint_least16_t */) (Register_ProgramCounter + (byte /* int8_t */) operand);
			break;

		default:
			address = Cycle_EffectiveAddress;
			break;
		}

		m_fdbg.append(String.format("  %02x ", Short.valueOf(opcode)));

		switch (opcode) {
		// Accumulator or Implied addressing
		case ASLn:
		case LSRn:
		case ROLn:
		case RORn:
			m_fdbg.append("      ");
			break;
		// Zero Page Addressing Mode Handler
		case ADCz:
		case ANDz:
		case ASLz:
		case BITz:
		case CMPz:
		case CPXz:
		case CPYz:
		case DCPz:
		case DECz:
		case EORz:
		case INCz:
		case ISBz:
		case LAXz:
		case LDAz:
		case LDXz:
		case LDYz:
		case LSRz:
		case NOPz:
		case NOPz_1:
		case NOPz_2:
		case ORAz:
		case ROLz:
		case RORz:
		case SAXz:
		case SBCz:
		case SREz:
		case STAz:
		case STXz:
		case STYz:
		case SLOz:
		case RLAz:
		case RRAz:
			// ASOz AXSz DCMz INSz LSEz - Optional Opcode Names
			m_fdbg.append(String.format("%02x    ", Short.valueOf((short /* uint8_t */) operand)));
			break;
		// Zero Page with X Offset Addressing Mode Handler
		case ADCzx:
		case ANDzx:
		case ASLzx:
		case CMPzx:
		case DCPzx:
		case DECzx:
		case EORzx:
		case INCzx:
		case ISBzx:
		case LDAzx:
		case LDYzx:
		case LSRzx:
		case NOPzx:
		case NOPzx_1:
		case NOPzx_2:
		case NOPzx_3:
		case NOPzx_4:
		case NOPzx_5:
		case ORAzx:
		case RLAzx:
		case ROLzx:
		case RORzx:
		case RRAzx:
		case SBCzx:
		case SLOzx:
		case SREzx:
		case STAzx:
		case STYzx:
			// ASOzx DCMzx INSzx LSEzx - Optional Opcode Names
			m_fdbg.append(String.format("%02x    ", Short.valueOf((short /* uint8_t */) operand)));
			break;
		// Zero Page with Y Offset Addressing Mode Handler
		case LDXzy:
		case STXzy:
		case SAXzy:
		case LAXzy:
			// AXSzx - Optional Opcode Names
			m_fdbg.append(String.format("%02x    ", Short.valueOf(endian_16lo8(operand))));
			break;
		// Absolute Addressing Mode Handler
		case ADCa:
		case ANDa:
		case ASLa:
		case BITa:
		case CMPa:
		case CPXa:
		case CPYa:
		case DCPa:
		case DECa:
		case EORa:
		case INCa:
		case ISBa:
		case JMPw:
		case JSRw:
		case LAXa:
		case LDAa:
		case LDXa:
		case LDYa:
		case LSRa:
		case NOPa:
		case ORAa:
		case ROLa:
		case RORa:
		case SAXa:
		case SBCa:
		case SLOa:
		case SREa:
		case STAa:
		case STXa:
		case STYa:
		case RLAa:
		case RRAa:
			// ASOa AXSa DCMa INSa LSEa - Optional Opcode Names
			m_fdbg.append(String.format("%02x %02x ", Short.valueOf(endian_16lo8(operand)), Short.valueOf(endian_16hi8(operand))));
			break;
		// Absolute With X Offset Addresing Mode Handler
		case ADCax:
		case ANDax:
		case ASLax:
		case CMPax:
		case DCPax:
		case DECax:
		case EORax:
		case INCax:
		case ISBax:
		case LDAax:
		case LDYax:
		case LSRax:
		case NOPax:
		case NOPax_1:
		case NOPax_2:
		case NOPax_3:
		case NOPax_4:
		case NOPax_5:
		case ORAax:
		case RLAax:
		case ROLax:
		case RORax:
		case RRAax:
		case SBCax:
		case SHYax:
		case SLOax:
		case SREax:
		case STAax:
			// ASOax DCMax INSax LSEax SAYax - Optional Opcode Names
			m_fdbg.append(String.format("%02x %02x ", Short.valueOf(endian_16lo8(operand)), Short.valueOf(endian_16hi8(operand))));
			break;
		// Absolute With Y Offset Addresing Mode Handler
		case ADCay:
		case ANDay:
		case CMPay:
		case DCPay:
		case EORay:
		case ISBay:
		case LASay:
		case LAXay:
		case LDAay:
		case LDXay:
		case ORAay:
		case RLAay:
		case RRAay:
		case SBCay:
		case SHAay:
		case SHSay:
		case SHXay:
		case SLOay:
		case SREay:
		case STAay:
			// ASOay AXAay DCMay INSax LSEay TASay XASay - Optional Opcode Names
			m_fdbg.append(String.format("%02x %02x ", Short.valueOf(endian_16lo8(operand)), Short.valueOf(endian_16hi8(operand))));
			break;
		// Immediate and Relative Addressing Mode Handler
		case ADCb:
		case ANDb:
		case ANCb:
		case ANCb_1:
		case ANEb:
		case ASRb:
		case ARRb:

		case CMPb:
		case CPXb:
		case CPYb:
		case EORb:
		case LDAb:
		case LDXb:
		case LDYb:
		case LXAb:
		case NOPb:
		case NOPb_1:
		case NOPb_2:
		case NOPb_3:
		case NOPb_4:
		case ORAb:
		case SBCb:
		case SBCb_1:
		case SBXb:
			// OALb ALRb XAAb - Optional Opcode Names
			m_fdbg.append(String.format("%02x    ", Short.valueOf(endian_16lo8(operand))));
			break;
		case BCCr:
		case BCSr:
		case BEQr:
		case BMIr:
		case BNEr:
		case BPLr:
		case BVCr:
		case BVSr:
			m_fdbg.append(String.format("%02x    ", Short.valueOf(endian_16lo8(operand))));
			break;
		// Indirect Addressing Mode Handler
		case JMPi:
			m_fdbg.append(String.format("%02x %02x ", Short.valueOf(endian_16lo8(operand)), Short.valueOf(endian_16hi8(operand))));
			break;
		// Indexed with X Preinc Addressing Mode Handler
		case ADCix:
		case ANDix:
		case CMPix:
		case DCPix:
		case EORix:
		case ISBix:
		case LAXix:
		case LDAix:
		case ORAix:
		case SAXix:
		case SBCix:
		case SLOix:
		case SREix:
		case STAix:
		case RLAix:
		case RRAix:
			// ASOix AXSix DCMix INSix LSEix - Optional Opcode Names
			m_fdbg.append(String.format("%02x    ", Short.valueOf(endian_16lo8(operand))));
			break;
		// Indexed with Y Postinc Addressing Mode Handler
		case ADCiy:
		case ANDiy:
		case CMPiy:
		case DCPiy:
		case EORiy:
		case ISBiy:
		case LAXiy:
		case LDAiy:
		case ORAiy:
		case RLAiy:
		case RRAiy:
		case SBCiy:
		case SHAiy:
		case SLOiy:
		case SREiy:
		case STAiy:
			// AXAiy ASOiy LSEiy DCMiy INSiy - Optional Opcode Names
			m_fdbg.append(String.format("%02x    ", Short.valueOf(endian_16lo8(operand))));
			break;
		default:
			m_fdbg.append("      ");
			break;
		}

		switch (opcode) {
		case ADCb:
		case ADCz:
		case ADCzx:
		case ADCa:
		case ADCax:
		case ADCay:
		case ADCix:
		case ADCiy:
			m_fdbg.append(" ADC");
			break;
		case ANCb:
		case ANCb_1:
			m_fdbg.append("*ANC");
			break;
		case ANDb:
		case ANDz:
		case ANDzx:
		case ANDa:
		case ANDax:
		case ANDay:
		case ANDix:
		case ANDiy:
			m_fdbg.append(" AND");
			break;
		case ANEb: // Also known as XAA
			m_fdbg.append("*ANE");
			break;
		case ARRb:
			m_fdbg.append("*ARR");
			break;
		case ASLn:
		case ASLz:
		case ASLzx:
		case ASLa:
		case ASLax:
			m_fdbg.append(" ASL");
			break;
		case ASRb: // Also known as ALR
			m_fdbg.append("*ASR");
			break;
		case BCCr:
			m_fdbg.append(" BCC");
			break;
		case BCSr:
			m_fdbg.append(" BCS");
			break;
		case BEQr:
			m_fdbg.append(" BEQ");
			break;
		case BITz:
		case BITa:
			m_fdbg.append(" BIT");
			break;
		case BMIr:
			m_fdbg.append(" BMI");
			break;
		case BNEr:
			m_fdbg.append(" BNE");
			break;
		case BPLr:
			m_fdbg.append(" BPL");
			break;
		case BRKn:
			m_fdbg.append(" BRK");
			break;
		case BVCr:
			m_fdbg.append(" BVC");
			break;
		case BVSr:
			m_fdbg.append(" BVS");
			break;
		case CLCn:
			m_fdbg.append(" CLC");
			break;
		case CLDn:
			m_fdbg.append(" CLD");
			break;
		case CLIn:
			m_fdbg.append(" CLI");
			break;
		case CLVn:
			m_fdbg.append(" CLV");
			break;
		case CMPb:
		case CMPz:
		case CMPzx:
		case CMPa:
		case CMPax:
		case CMPay:
		case CMPix:
		case CMPiy:
			m_fdbg.append(" CMP");
			break;
		case CPXb:
		case CPXz:
		case CPXa:
			m_fdbg.append(" CPX");
			break;
		case CPYb:
		case CPYz:
		case CPYa:
			m_fdbg.append(" CPY");
			break;
		case DCPz:
		case DCPzx:
		case DCPa:
		case DCPax:
		case DCPay:
		case DCPix:
		case DCPiy: // Also known as DCM
			m_fdbg.append("*DCP");
			break;
		case DECz:
		case DECzx:
		case DECa:
		case DECax:
			m_fdbg.append(" DEC");
			break;
		case DEXn:
			m_fdbg.append(" DEX");
			break;
		case DEYn:
			m_fdbg.append(" DEY");
			break;
		case EORb:
		case EORz:
		case EORzx:
		case EORa:
		case EORax:
		case EORay:
		case EORix:
		case EORiy:
			m_fdbg.append(" EOR");
			break;
		case INCz:
		case INCzx:
		case INCa:
		case INCax:
			m_fdbg.append(" INC");
			break;
		case INXn:
			m_fdbg.append(" INX");
			break;
		case INYn:
			m_fdbg.append(" INY");
			break;
		case ISBz:
		case ISBzx:
		case ISBa:
		case ISBax:
		case ISBay:
		case ISBix:
		case ISBiy: // Also known as INS
			m_fdbg.append("*ISB");
			break;
		case JMPw:
		case JMPi:
			m_fdbg.append(" JMP");
			break;
		case JSRw:
			m_fdbg.append(" JSR");
			break;
		case LASay:
			m_fdbg.append("*LAS");
			break;
		case LAXz:
		case LAXzy:
		case LAXa:
		case LAXay:
		case LAXix:
		case LAXiy:
			m_fdbg.append("*LAX");
			break;
		case LDAb:
		case LDAz:
		case LDAzx:
		case LDAa:
		case LDAax:
		case LDAay:
		case LDAix:
		case LDAiy:
			m_fdbg.append(" LDA");
			break;
		case LDXb:
		case LDXz:
		case LDXzy:
		case LDXa:
		case LDXay:
			m_fdbg.append(" LDX");
			break;
		case LDYb:
		case LDYz:
		case LDYzx:
		case LDYa:
		case LDYax:
			m_fdbg.append(" LDY");
			break;
		case LSRz:
		case LSRzx:
		case LSRa:
		case LSRax:
		case LSRn:
			m_fdbg.append(" LSR");
			break;
		case NOPn:
		case NOPn_1:
		case NOPn_2:
		case NOPn_3:
		case NOPn_4:
		case NOPn_5:
		case NOPn_6:
		case NOPb:
		case NOPb_1:
		case NOPb_2:
		case NOPb_3:
		case NOPb_4:
		case NOPz:
		case NOPz_1:
		case NOPz_2:
		case NOPzx:
		case NOPzx_1:
		case NOPzx_2:
		case NOPzx_3:
		case NOPzx_4:
		case NOPzx_5:
		case NOPa:
		case NOPax:
		case NOPax_1:
		case NOPax_2:
		case NOPax_3:
		case NOPax_4:
		case NOPax_5:
			if (opcode != NOPn)
				m_fdbg.append("*");
			else
				m_fdbg.append(" ");
			m_fdbg.append("NOP");
			break;
		case LXAb: // Also known as OAL
			m_fdbg.append("*LXA");
			break;
		case ORAb:
		case ORAz:
		case ORAzx:
		case ORAa:
		case ORAax:
		case ORAay:
		case ORAix:
		case ORAiy:
			m_fdbg.append(" ORA");
			break;
		case PHAn:
			m_fdbg.append(" PHA");
			break;
		case PHPn:
			m_fdbg.append(" PHP");
			break;
		case PLAn:
			m_fdbg.append(" PLA");
			break;
		case PLPn:
			m_fdbg.append(" PLP");
			break;
		case RLAz:
		case RLAzx:
		case RLAix:
		case RLAa:
		case RLAax:
		case RLAay:
		case RLAiy:
			m_fdbg.append("*RLA");
			break;
		case ROLz:
		case ROLzx:
		case ROLa:
		case ROLax:
		case ROLn:
			m_fdbg.append(" ROL");
			break;
		case RORz:
		case RORzx:
		case RORa:
		case RORax:
		case RORn:
			m_fdbg.append(" ROR");
			break;
		case RRAa:
		case RRAax:
		case RRAay:
		case RRAz:
		case RRAzx:
		case RRAix:
		case RRAiy:
			m_fdbg.append("*RRA");
			break;
		case RTIn:
			m_fdbg.append(" RTI");
			break;
		case RTSn:
			m_fdbg.append(" RTS");
			break;
		case SAXz:
		case SAXzy:
		case SAXa:
		case SAXix: // Also known as AXS
			m_fdbg.append("*SAX");
			break;
		case SBCb:
		case SBCb_1:
			if (opcode != SBCb)
				m_fdbg.append("*");
			else
				m_fdbg.append(" ");
			m_fdbg.append("SBC");
			break;
		case SBCz:
		case SBCzx:
		case SBCa:
		case SBCax:
		case SBCay:
		case SBCix:
		case SBCiy:
			m_fdbg.append(" SBC");
			break;
		case SBXb:
			m_fdbg.append("*SBX");
			break;
		case SECn:
			m_fdbg.append(" SEC");
			break;
		case SEDn:
			m_fdbg.append(" SED");
			break;
		case SEIn:
			m_fdbg.append(" SEI");
			break;
		case SHAay:
		case SHAiy: // Also known as AXA
			m_fdbg.append("*SHA");
			break;
		case SHSay: // Also known as TAS
			m_fdbg.append("*SHS");
			break;
		case SHXay: // Also known as XAS
			m_fdbg.append("*SHX");
			break;
		case SHYax: // Also known as SAY
			m_fdbg.append("*SHY");
			break;
		case SLOz:
		case SLOzx:
		case SLOa:
		case SLOax:
		case SLOay:
		case SLOix:
		case SLOiy: // Also known as ASO
			m_fdbg.append("*SLO");
			break;
		case SREz:
		case SREzx:
		case SREa:
		case SREax:
		case SREay:
		case SREix:
		case SREiy: // Also known as LSE
			m_fdbg.append("*SRE");
			break;
		case STAz:
		case STAzx:
		case STAa:
		case STAax:
		case STAay:
		case STAix:
		case STAiy:
			m_fdbg.append(" STA");
			break;
		case STXz:
		case STXzy:
		case STXa:
			m_fdbg.append(" STX");
			break;
		case STYz:
		case STYzx:
		case STYa:
			m_fdbg.append(" STY");
			break;
		case TAXn:
			m_fdbg.append(" TAX");
			break;
		case TAYn:
			m_fdbg.append(" TAY");
			break;
		case TSXn:
			m_fdbg.append(" TSX");
			break;
		case TXAn:
			m_fdbg.append(" TXA");
			break;
		case TXSn:
			m_fdbg.append(" TXS");
			break;
		case TYAn:
			m_fdbg.append(" TYA");
			break;
		default:
			m_fdbg.append("*HLT");
			break;
		}

		switch (opcode) {
		// Accumulator or Implied addressing
		case ASLn:
		case LSRn:
		case ROLn:
		case RORn:
			m_fdbg.append("n  A");
			break;

		// Zero Page Addressing Mode Handler
		case ADCz:
		case ANDz:
		case ASLz:
		case BITz:
		case CMPz:
		case CPXz:
		case CPYz:
		case DCPz:
		case DECz:
		case EORz:
		case INCz:
		case ISBz:
		case LAXz:
		case LDAz:
		case LDXz:
		case LDYz:
		case LSRz:
		case ORAz:

		case ROLz:
		case RORz:
		case SBCz:
		case SREz:
		case SLOz:
		case RLAz:
		case RRAz:
			// ASOz AXSz DCMz INSz LSEz - Optional Opcode Names
			m_fdbg.append(String.format("z  %02x {%02x}", Short.valueOf((short /* uint8_t */) operand), Short.valueOf(data)));
			break;
		case SAXz:
		case STAz:
		case STXz:
		case STYz:
		case NOPz:
		case NOPz_1:
		case NOPz_2:
			if (!MOS6510.isLoggable(Level.FINE)
					&& (opcode == NOPz || opcode == NOPz_1 || opcode == NOPz_2)) {
				break;
			}
			m_fdbg.append(String.format("z  %02x", Short.valueOf(endian_16lo8(operand))));
			break;

		// Zero Page with X Offset Addressing Mode Handler
		case ADCzx:
		case ANDzx:
		case ASLzx:
		case CMPzx:
		case DCPzx:
		case DECzx:
		case EORzx:
		case INCzx:
		case ISBzx:
		case LDAzx:
		case LDYzx:
		case LSRzx:
		case ORAzx:
		case RLAzx:
		case ROLzx:
		case RORzx:
		case RRAzx:
		case SBCzx:
		case SLOzx:
		case SREzx:
			// ASOzx DCMzx INSzx LSEzx - Optional Opcode Names
			m_fdbg.append(String.format("zx %02x,X", Short.valueOf(endian_16lo8(operand))));
			m_fdbg.append(String.format(" [%04x]{%02x}", Integer.valueOf(address), Short.valueOf(data)));
			break;
		case STAzx:
		case STYzx:
		case NOPzx:
		case NOPzx_1:
		case NOPzx_2:
		case NOPzx_3:
		case NOPzx_4:
		case NOPzx_5:
			if (!MOS6510.isLoggable(Level.FINE)
					&& (opcode == NOPzx || opcode == NOPzx_1
							|| opcode == NOPzx_2 || opcode == NOPzx_3
							|| opcode == NOPzx_4 || opcode == NOPzx_5)) {
				break;
			}
			m_fdbg
			.append(String.format("zx %02x,X",
					Short.valueOf(endian_16lo8(operand))));
			m_fdbg.append(String.format(" [%04x]", Integer.valueOf(address)));
			break;

		// Zero Page with Y Offset Addressing Mode Handler
		case LAXzy:
		case LDXzy:
			// AXSzx - Optional Opcode Names
			m_fdbg.append(String.format("zy %02x,Y", Short.valueOf(endian_16lo8(operand))));
			m_fdbg.append(String.format(" [%04x]{%02x}", Integer.valueOf(address), Short.valueOf(data)));
			break;
		case STXzy:
		case SAXzy:
			m_fdbg.append(String.format("zy %02x,Y", Short.valueOf(endian_16lo8(operand))));
			m_fdbg.append(String.format(" [%04x]", Integer.valueOf(address)));
			break;

		// Absolute Addressing Mode Handler
		case ADCa:
		case ANDa:
		case ASLa:
		case BITa:
		case CMPa:
		case CPXa:
		case CPYa:
		case DCPa:
		case DECa:
		case EORa:
		case INCa:
		case ISBa:
		case LAXa:
		case LDAa:
		case LDXa:
		case LDYa:
		case LSRa:
		case ORAa:
		case ROLa:
		case RORa:
		case SBCa:
		case SLOa:
		case SREa:
		case RLAa:
		case RRAa:
			// ASOa AXSa DCMa INSa LSEa - Optional Opcode Names
			m_fdbg.append(String.format("a  %04x {%02x}", Integer.valueOf(operand), Short.valueOf(data)));
			break;
		case SAXa:
		case STAa:
		case STXa:
		case STYa:
		case NOPa:
			if (!MOS6510.isLoggable(Level.FINE)
					&& (opcode == NOPa)) {
				break;
			}
			m_fdbg.append(String.format("a  %04x", Integer.valueOf(operand)));
			break;
		case JMPw:
		case JSRw:
			m_fdbg.append(String.format("w  %04x", Integer.valueOf(operand)));
			break;

		// Absolute With X Offset Addresing Mode Handler
		case ADCax:
		case ANDax:
		case ASLax:
		case CMPax:
		case DCPax:
		case DECax:
		case EORax:
		case INCax:
		case ISBax:
		case LDAax:
		case LDYax:
		case LSRax:
		case ORAax:
		case RLAax:
		case ROLax:
		case RORax:
		case RRAax:
		case SBCax:
		case SLOax:
		case SREax:
			// ASOax DCMax INSax LSEax SAYax - Optional Opcode Names
			m_fdbg.append(String.format("ax %04x,X", Integer.valueOf(operand)));
			m_fdbg.append(String.format(" [%04x]{%02x}", Integer.valueOf(address), Short.valueOf(data)));
			break;
		case SHYax:
		case STAax:
		case NOPax:
		case NOPax_1:
		case NOPax_2:
		case NOPax_3:
		case NOPax_4:
		case NOPax_5:
			if (!MOS6510.isLoggable(Level.FINE)
					&& (opcode == NOPax || opcode == NOPax_1
							|| opcode == NOPax_2 || opcode == NOPax_3
							|| opcode == NOPax_4 || opcode == NOPax_5)) {
				break;
			}
			m_fdbg.append(String.format("ax %04x,X", Integer.valueOf(operand)));
			m_fdbg.append(String.format(" [%04x]", Integer.valueOf(address)));
			break;

		// Absolute With Y Offset Addresing Mode Handler
		case ADCay:
		case ANDay:
		case CMPay:
		case DCPay:
		case EORay:
		case ISBay:
		case LASay:
		case LAXay:
		case LDAay:
		case LDXay:
		case ORAay:
		case RLAay:
		case RRAay:
		case SBCay:
		case SHSay:
		case SLOay:
		case SREay:
			// ASOay AXAay DCMay INSax LSEay TASay XASay - Optional Opcode Names
			m_fdbg.append(String.format("ay %04x,Y", Integer.valueOf(operand)));
			m_fdbg.append(String.format(" [%04x]{%02x}", Integer.valueOf(address), Short.valueOf(data)));
			break;
		case SHAay:
		case SHXay:
		case STAay:
			m_fdbg.append(String.format("ay %04x,Y", Integer.valueOf(operand)));
			m_fdbg.append(String.format(" [%04x]", Integer.valueOf(address)));
			break;

		// Immediate Addressing Mode Handler
		case ADCb:
		case ANDb:
		case ANCb:
		case ANCb_1:
		case ANEb:
		case ASRb:
		case ARRb:
		case CMPb:
		case CPXb:
		case CPYb:
		case EORb:
		case LDAb:
		case LDXb:
		case LDYb:
		case LXAb:
		case ORAb:
		case SBCb:
		case SBCb_1:
		case SBXb:
			// OALb ALRb XAAb - Optional Opcode Names
		case NOPb:
		case NOPb_1:
		case NOPb_2:
		case NOPb_3:
		case NOPb_4:
			if (!MOS6510.isLoggable(Level.FINE)
					&& (opcode == NOPb || opcode == NOPb_1 || opcode == NOPb_2
							|| opcode == NOPb_3 || opcode == NOPb_4)) {
				break;
			}
			m_fdbg.append(String.format("b  #%02x", Short.valueOf(endian_16lo8(operand))));
			break;

		// Relative Addressing Mode Handler
		case BCCr:
		case BCSr:
		case BEQr:
		case BMIr:
		case BNEr:
		case BPLr:
		case BVCr:
		case BVSr:
			m_fdbg.append(String.format("r  #%02x", Short.valueOf(endian_16lo8(operand))));
			m_fdbg.append(String.format(" [%04x]", Integer.valueOf(address)));
			break;

		// Indirect Addressing Mode Handler
		case JMPi:
			m_fdbg.append(String.format("i  (%04x)", Integer.valueOf(operand)));
			m_fdbg.append(String.format(" [%04x]", Integer.valueOf(address)));
			break;

		// Indexed with X Preinc Addressing Mode Handler
		case ADCix:
		case ANDix:
		case CMPix:
		case DCPix:
		case EORix:
		case ISBix:
		case LAXix:
		case LDAix:
		case ORAix:
		case SBCix:
		case SLOix:
		case SREix:
		case RLAix:
		case RRAix:
			// ASOix AXSix DCMix INSix LSEix - Optional Opcode Names
			m_fdbg.append(String.format("ix (%02x,X)", Short.valueOf(endian_16lo8(operand))));
			m_fdbg.append(String.format(" [%04x]{%02x}", Integer.valueOf(address), Short.valueOf(data)));
			break;
		case SAXix:
		case STAix:
			m_fdbg.append(String.format("ix (%02x,X)", Short.valueOf(endian_16lo8(operand))));
			m_fdbg.append(String.format(" [%04x]", Integer.valueOf(address)));
			break;

		// Indexed with Y Postinc Addressing Mode Handler
		case ADCiy:
		case ANDiy:
		case CMPiy:
		case DCPiy:
		case EORiy:
		case ISBiy:
		case LAXiy:
		case LDAiy:
		case ORAiy:
		case RLAiy:
		case RRAiy:
		case SBCiy:
		case SLOiy:
		case SREiy:
			// AXAiy ASOiy LSEiy DCMiy INSiy - Optional Opcode Names
			m_fdbg.append(String.format("iy (%02x),Y", Short.valueOf(endian_16lo8(operand))));
			m_fdbg.append(String.format(" [%04x]{%02x}", Integer.valueOf(address), Short.valueOf(data)));
			break;
		case SHAiy:
		case STAiy:
			m_fdbg.append(String.format("iy (%02x),Y", Short.valueOf(endian_16lo8(operand))));
			m_fdbg.append(String.format(" [%04x]", Integer.valueOf(address)));
			break;

		default:
			break;
		}

		m_fdbg.append("\n");
		MOS6510.info(m_fdbg.toString());
	}

	/**
	 * Handle bus access signals
	 * 
	 * @param state
	 */
	public void aecSignal(boolean state) {
		if (aec != state) {
			long /* event_clock_t */clock = eventContext.getTime(m_extPhase);

			// If the CPU blocked waiting for the bus
			// then schedule a retry.
			aec = state;
			if (state && m_blocked) {
				// Correct IRQs that appeared before the steal
				long /* event_clock_t */stolen = clock - m_stealingClk;
				interrupts.nmiClk += stolen;
				interrupts.irqClk += stolen;
				// IRQs that appeared during the steal must have
				// there clocks corrected
				if (interrupts.nmiClk > clock)
					interrupts.nmiClk = clock - 1;
				if (interrupts.irqClk > clock)
					interrupts.irqClk = clock - 1;
				m_blocked = false;
			}

			eventContext.schedule(event, (eventContext.phase() == m_phase ? 1
					: 0), m_phase);
		}
	}

	//
	// Interrupt Routines
	//

	public static final int iIRQSMAX = 3;

	public static final int oNONE = -1;

	public static final int oRST = 0;

	public static final int oNMI = 1;

	public static final int oIRQ = 2;

	public static final int iNONE = 0;

	public static final int iRST = 1 << oRST;

	public static final int iNMI = 1 << oNMI;

	public static final int iIRQ = 1 << oIRQ;

	//
	// Non-standard functions
	//

	public void triggerRST() {
		interrupts.pending |= iRST;
	}

	public void triggerNMI() {
		interrupts.pending |= iNMI;
		interrupts.nmiClk = eventContext.getTime(m_extPhase);
	}

	/**
	 * Level triggered interrupt
	 */
	public void triggerIRQ() {
		// IRQ Suppressed
		if (!getFlagI())
			interrupts.irqRequest = true;
		if ((interrupts.irqs++ == 0))
			interrupts.irqClk = eventContext.getTime(m_extPhase);

		if (interrupts.irqs > iIRQSMAX) {
			MOS6510
					.log(Level.SEVERE, "\nMOS6510 ERROR: An external component is not clearing down it's IRQs.\n\n");
			throw new RuntimeException("MOS6510 Error: too many IRQs");
		}
	}

	public void clearIRQ() {
		if (interrupts.irqs > 0) {
			if ((--interrupts.irqs) == 0) {
				// Clear off the interrupts
				interrupts.irqRequest = false;
			}
		}
	}

	//
	// Status Register Routines
	// Set N and Z flags according to byte
	//

	void setFlagsNZ(short x) {
		Register_z_Flag = (Register_n_Flag = (short /* uint_least8_t */) (x));
	}

	void setFlagN(short x) {
		Register_n_Flag = (short /* uint_least8_t */) (x);
	}

	void setFlagV(short x) {
		Register_v_Flag = (short /* uint_least8_t */) (x);
	}

	void setFlagD(short x) {
		Register_Status = (short) ((Register_Status & (~(1 << SR_DECIMAL) & 0xff)) | ((((x) != 0) ? 1
				: 0) << SR_DECIMAL));
	}

	void setFlagI(short x) {
		Register_Status = (short) ((Register_Status & (~(1 << SR_INTERRUPT) & 0xff)) | ((((x) != 0) ? 1
				: 0) << SR_INTERRUPT));
	}

	void setFlagZ(short x) {
		Register_z_Flag = (short /* uint_least8_t */) (x);
	}

	void setFlagC(short x) {
		Register_c_Flag = (short /* uint_least8_t */) (x);
	}

	boolean getFlagN() {
		return (Register_n_Flag & (1 << SR_NEGATIVE)) != 0;
	}

	boolean getFlagV() {
		return Register_v_Flag != 0;
	}

	boolean getFlagD() {
		return (Register_Status & (1 << SR_DECIMAL)) != 0;
	}

	boolean getFlagI() {
		return (Register_Status & (1 << SR_INTERRUPT)) != 0;
	}

	boolean getFlagZ() {
		return Register_z_Flag == 0;
	}

	boolean getFlagC() {
		return Register_c_Flag != 0;
	}

	public void debug(boolean enable) {
		dodump = enable;
	}

}
