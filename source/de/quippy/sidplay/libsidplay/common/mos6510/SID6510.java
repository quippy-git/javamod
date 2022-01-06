/**
 *                             Special MOS6510 to be fully
 *                               compatible with de.quippy.sidplay.sidplay
 *                             ---------------------------
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

import static de.quippy.sidplay.libsidplay.common.SIDEndian.endian_16hi8;
import static de.quippy.sidplay.libsidplay.common.SIDEndian.endian_32hi16;
import static de.quippy.sidplay.libsidplay.common.SIDEndian.endian_32lo16;
import static de.quippy.sidplay.libsidplay.common.mos6510.IConf6510.NO_RTS_UPON_BRK;

import java.util.logging.Level;

import de.quippy.sidplay.libsidplay.common.IEventContext;
import de.quippy.sidplay.libsidplay.common.ISID2Types.sid2_env_t;


/**
 * Sidplay Specials
 * 
 * @author Ken H�ndel
 * 
 */
public class SID6510 extends MOS6510 {

	private boolean m_sleeping;

	private sid2_env_t m_mode;

	private long /* event_clock_t */m_delayClk;

	private boolean m_framelock;

	public SID6510(IEventContext context) {
		super(context);
		m_mode = sid2_env_t.sid2_envR;
		m_framelock = false;

		//
		// The hacks for de.quippy.sidplay.sidplay are done with overridden methods of MOS6510
		//

		// Used to insert busy delays into the CPU emulation
		delayCycle.func = new IFunc() {

			public void invoke() {
				sid_delay();
			}

		};
	}

	//
	// Standard Functions
	//

	public void reset() {
		m_sleeping = false;
		// Call inherited reset
		super.reset();
	}

	public void reset(int /* uint_least16_t */pc, short /* uint8_t */a,
			short /* uint8_t */x, short /* uint8_t */y) {
		// Reset the processor
		reset();

		// Registers not touched by a reset
		Register_Accumulator = a;
		Register_X = x;
		Register_Y = y;
		Register_ProgramCounter = pc;
	}

	public void environment(sid2_env_t mode) {
		m_mode = mode;
	}

	//
	// Sidplay compatibility interrupts. Basically wakes CPU if it is m_sleeping
	//

	public void triggerRST() {
		// All modes
		super.triggerRST();
		if (m_sleeping) {
			m_sleeping = false;
			eventContext.schedule(event, (eventContext.phase() == m_phase) ? 1
					: 0, m_phase);
		}
	}

	public void triggerNMI() {
		// Only in Real C64 mode
		if (m_mode == sid2_env_t.sid2_envR) {
			super.triggerNMI();
			if (m_sleeping) {
				m_sleeping = false;
				eventContext.schedule(event,
						(eventContext.phase() == m_phase) ? 1 : 0, m_phase);
			}
		}
	}

	public void triggerIRQ() {
		switch (m_mode) {
		default:
			if (MOS6510.isLoggable(Level.FINE)) {
				if (dodump) {
					MOS6510
							.fine("****************************************************\n");
					MOS6510.fine(" Fake IRQ Routine\n");
					MOS6510
							.fine("****************************************************\n");
				}
			}
			return;
		case sid2_envR:
			super.triggerIRQ();
			if (m_sleeping) {
				// Simulate busy loop
				m_sleeping = !(interrupts.irqRequest || (interrupts.pending != 0));
				if (!m_sleeping)
					eventContext.schedule(event,
							(eventContext.phase() == m_phase) ? 1 : 0, m_phase);
			}
		}
	}

	/**
	 * Send CPU is about to sleep. Only a reset or interrupt will wake up the
	 * processor
	 */
	public void sleep() {
		// Simulate a delay for JMPw
		m_delayClk = m_stealingClk = eventContext.getTime(m_phase);
		procCycle = new ProcessorCycle[] {
			delayCycle };
		cycleCount = 0;
		m_sleeping = !(interrupts.irqRequest || (interrupts.pending != 0));
		envSleep();
	}

	//
	// Ok start all the hacks for de.quippy.sidplay.sidplay. This prevents
	// execution of code in roms. For real c64 emulation
	// create object from base class! Also stops code
	// rom execution when bad code switches roms in over
	// itself.
	//

	/**
	 * Hack for de.quippy.sidplay.sidplay: Suppresses Illegal Instructions
	 */
	protected void illegal_instr() {
		sid_illegal();
	}

	/**
	 * Hack for de.quippy.sidplay.sidplay: Stop jumps into ROM code
	 */
	protected void jmp_instr() {
		sid_jmp();
	}

	/**
	 * Hack for de.quippy.sidplay.sidplay: No overlapping IRQs allowed
	 */
	protected void cli_instr() {
		sid_cli();
	}

	/**
	 * Hack for de.quippy.sidplay.sidplay: Since no real IRQs, all RTIs mapped to RTS Required for
	 * fix bad tunes in old modes
	 */
	protected void PopSR_sidplay_rti() {
		sid_rti();
	}

	/**
	 * Hack for de.quippy.sidplay.sidplay: Support of sidplays BRK functionality
	 */
	protected void PushHighPC_sidplay_brk() {
		sid_brk();
	}

	/**
	 * Hack for de.quippy.sidplay.sidplay: RTI behaves like RTI in sidplay1 modes
	 */
	protected void IRQRequest_sidplay_irq() {
		sid_irq();
	}

	protected void FetchOpcode() {
		if (m_mode == sid2_env_t.sid2_envR) {
			super.FetchOpcode();
			return;
		}

		// Sid tunes end by wrapping the stack. For compatibility it
		// has to be handled.
		m_sleeping |= (endian_16hi8(Register_StackPointer) != SP_PAGE);
		m_sleeping |= (endian_32hi16(Register_ProgramCounter) != 0);
		if (!m_sleeping)
			super.FetchOpcode();

		if (m_framelock == false) {
			int timeout = 6000000;
			m_framelock = true;
			// Simulate sidplay1 frame based execution
			while (!m_sleeping && (timeout != 0)) {
				super.clock();
				timeout--;
			}
			if (timeout == 0) {
				MOS6510
						.log(Level.SEVERE, "\n\nINFINITE LOOP DETECTED *********************************\n");
				envReset();
			}
			sleep();
			m_framelock = false;
		}
	}

	private ProcessorCycle delayCycle = new ProcessorCycle();

	//
	// For de.quippy.sidplay.sidplay compatibility implement those instructions which don't behave
	// properly.
	//

	/**
	 * Sidplay Suppresses Illegal Instructions
	 */
	private void sid_illegal() {
		if (m_mode == sid2_env_t.sid2_envR) {
			super.illegal_instr();
			return;
		}
		if (MOS6510.isLoggable(Level.FINE)) {
			DumpState();
		}
	}

	private void sid_delay() {
		long /* event_clock_t */stolen = eventContext.getTime(m_stealingClk,
				m_phase);
		long /* event_clock_t */delayed = eventContext.getTime(m_delayClk,
				m_phase);

		// Check for stealing. The relative clock cycle
		// differences are compared here rather than the
		// clocks directly. This means we don't have to
		// worry about the clocks wrapping
		if (delayed > stolen) {
			// No longer stealing so adjust clock
			delayed -= stolen;
			m_delayClk += stolen;
			m_stealingClk = m_delayClk;
		}

		cycleCount--;
		// Woken from sleep just to handle the stealing release
		if (m_sleeping)
			eventContext.cancel(event);
		else {
			long /* event_clock_t */cycle = delayed % 3;
			if (cycle == 0) {
				if (interruptPending())
					return;
			}
			eventContext.schedule(event, 3 - cycle, m_phase);
		}
	}

	private void sid_brk() {
		if (m_mode == sid2_env_t.sid2_envR) {
			super.PushHighPC();
			return;
		}

		sei_instr();
		if (!NO_RTS_UPON_BRK) {
			sid_rts();
		}
		FetchOpcode();
	}

	private void sid_jmp() {
		// For de.quippy.sidplay.sidplay compatibility, inherited from environment
		if (m_mode == sid2_env_t.sid2_envR) {
			// If a busy loop then just sleep
			if (Cycle_EffectiveAddress == instrStartPC) {
				Register_ProgramCounter = endian_32lo16(
						Register_ProgramCounter, Cycle_EffectiveAddress);
				if (!interruptPending())
					this.sleep();
			} else
				super.jmp_instr();
			return;
		}

		if (envCheckBankJump(Cycle_EffectiveAddress))
			super.jmp_instr();
		else
			sid_rts();
	}

	/**
	 * Will do a full rts in 1 cycle, to destroy current function and quit
	 */
	private void sid_rts() {
		PopLowPC();
		PopHighPC();
		rts_instr();
	}

	private void sid_cli() {
		if (m_mode == sid2_env_t.sid2_envR)
			super.cli_instr();
	}

	private void sid_rti() {
		if (m_mode == sid2_env_t.sid2_envR) {
			PopSR();
			return;
		}

		// Fake RTS
		sid_rts();
		FetchOpcode();
	}

	private void sid_irq() {
		super.IRQRequest();
		if (m_mode != sid2_env_t.sid2_envR) {
			// RTI behaves like RTI in sidplay1 modes
			Register_StackPointer++;
		}
	}
}
