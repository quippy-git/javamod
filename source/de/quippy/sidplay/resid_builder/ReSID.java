/**
 *                           ReSid Emulation
 *                           ---------------
 *  begin                : Fri Apr 4 2001
 *  copyright            : (C) 2001 by Simon White
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
package de.quippy.sidplay.resid_builder;

import static de.quippy.sidplay.resid_builder.resid.ISIDDefs.resid_version_string;

import java.util.logging.Level;
import java.util.logging.Logger;

import de.quippy.sidplay.libsidplay.common.C64Env;
import de.quippy.sidplay.libsidplay.common.IEventContext;
import de.quippy.sidplay.libsidplay.common.SIDBuilder;
import de.quippy.sidplay.libsidplay.common.SIDEmu;
import de.quippy.sidplay.libsidplay.common.Event.event_phase_t;
import de.quippy.sidplay.libsidplay.common.ISID2Types.sid2_model_t;
import de.quippy.sidplay.resid_builder.resid.SID;
import de.quippy.sidplay.resid_builder.resid.ISIDDefs.chip_model;
import de.quippy.sidplay.resid_builder.resid.ISIDDefs.sampling_method;


public class ReSID extends SIDEmu {
	private static final Logger RESID = Logger.getLogger(ReSID.class.getName());

	private static final String VERSION = "0.0.2";

	private IEventContext m_context;

	private event_phase_t m_phase;

	private SID m_sid;

	private long /* event_clock_t */m_accessClk;

	private long /* int_least32_t */m_gain;

	private static String m_credit;

	private final String m_error;

	private boolean m_status;

	private boolean m_locked;

	private byte /* uint_least8_t */m_optimisation;
	
	static
	{
		// Setup credits
		m_credit = "ReSID V" + VERSION + " Engine:"
				 + "\t(C) 1999-2002 Simon White <sidplay2@yahoo.com>"
				 + "MOS6581 (SID) Emulation (ReSID V" + resid_version_string + "):"
				 + "\t(C) 1999-2002 Dag Lem <resid@nimrod.no>";
	}

	public ReSID(SIDBuilder builder) {
		super(builder);
		m_context = (null);
		m_phase = (event_phase_t.EVENT_CLOCK_PHI1);
		m_sid = (new SID());
		m_gain = (100);
		m_status = (true);
		m_locked = (false);
		m_optimisation = (0);

		if (m_sid == null) {
			m_error = "RESID ERROR: Unable to create sid object";
			m_status = false;
			return;
		} else {
			m_error = "N/A";
		}
		reset((short) 0);
	}

	// Standard component functions

	public final String credits() {
		return m_credit;
	}

	public void reset() {
		super.reset();
	}

	public void reset(short /* uint8_t */volume) {
		m_accessClk = 0;
		m_sid.reset();
		m_sid.write(0x18, volume);
	}

	public short /* uint8_t */read(short /* uint_least8_t */addr) {
		long /* event_clock_t */cycles = m_context.getTime(m_accessClk,
				m_phase);
		m_accessClk += cycles;
		if (m_optimisation != 0) {
			if (cycles != 0)
				m_sid.clock((int) cycles);
		} else {
			while ((cycles--) != 0)
				m_sid.clock();

		}
		return (short) m_sid.read(addr);
	}

	public void write(short /* uint_least8_t */addr, short /* uint8_t */data) {
		if (RESID.isLoggable(Level.FINE)) {
			RESID.fine(String.format("write 0x%02x=0x%02x", Short.valueOf(addr), Short.valueOf(data)));
			RESID.fine("\n");
		}
		long /* event_clock_t */cycles = m_context.getTime(m_accessClk,
				m_phase);
		m_accessClk += cycles;
		if (m_optimisation != 0) {
			if (cycles != 0)
				m_sid.clock((int) cycles);
		} else {
			while ((cycles--) != 0)
				m_sid.clock();

		}
		m_sid.write(addr, data);
	}

	public final String error() {
		return m_error;
	}

	// Standard SID functions

	public long /* int_least32_t */output(short /* uint_least8_t */bits) {
		long /* event_clock_t */cycles = m_context.getTime(m_accessClk,
				m_phase);
		m_accessClk += cycles;
		if (m_optimisation != 0) {
			if (cycles != 0)
				m_sid.clock((int) cycles);
		} else {
			while ((cycles--) != 0)
				m_sid.clock();

		}
		return m_sid.output(bits) * m_gain / 100;
	}

	public void filter(boolean enable) {
		m_sid.enable_filter(enable);
	}

	public void voice(short /* uint_least8_t */num,
			short /* uint_least8_t */volume, boolean mute) {
		// At this time
		// only mute is
		// supported
		m_sid.mute(num, mute);
	}

	public void gain(short /* uint_least8_t */percent) {
		// 0 to 99 is loss, 101 - 200 is gain
		m_gain = percent;
		m_gain += 100;
		if (m_gain > 200)
			m_gain = 200;
	}

	/**
	 * Set optimisation level
	 */
	public void optimisation(byte /* uint_least8_t */level) {
		m_optimisation = level;
	}

	public boolean bool() {
		return m_status;
	}

	// Specific to ReSID

	public void sampling(long /* uint_least32_t */freq) {
		m_sid.set_sampling_parameters(1000000, sampling_method.SAMPLE_FAST,
				freq, -1, 0.79);
	}

	public boolean filter(final sid_filter_t filter) {
		int fc[][] = new int[0x802][2] /* fc_point */;
		final int f0[] /* fc_point */[] = fc;
		int points = 0;

		if (filter == null) {
			// Select default filter
			// m_sid.fc_default(f0, points);
			SID.FCPoints fcp = new SID.FCPoints();
			m_sid.fc_default(fcp);
			fc = fcp.points;
			points = fcp.count;
		} else {
			// Make sure there are enough filter points and they are
			// legal
			points = filter.points;
			if ((points < 2) || (points > 0x800))
				return false;

			{
				final int /* sid_fc_t */[] fstart = {
						-1, 0 };
				int /* sid_fc_t */[] fprev = fstart;
				int fin = 0;
				int fout = 0;
				// Last check, make sure they are list in numerical order
				// for both axis
				while (points-- > 0) {
					if ((fprev)[0] >= filter.cutoff[fin][0])
						return false;
					fout++;
					fc[fout][0] = filter.cutoff[fin][0];
					fc[fout][1] = filter.cutoff[fin][1];
					fprev = filter.cutoff[fin++];
				}
				// Updated ReSID interpolate requires we
				// repeat the end points
				fc[fout + 1][0] = fc[fout][0];
				fc[fout + 1][1] = fc[fout][1];
				fc[0][0] = fc[1][0];
				fc[0][1] = fc[1][1];
				points = filter.points + 2;
			}
		}

		// function from reSID
		points--;
		m_sid.filter.interpolate(f0, 0, points, m_sid.fc_plotter(), 1.0);

		if (filter != null && filter.Lthreshold != 0)
			m_sid.set_distortion_properties(filter.Lthreshold,
					filter.Lsteepness, filter.Llp, filter.Lbp, filter.Lhp,
					filter.Hthreshold, filter.Hsteepness, filter.Hlp,
					filter.Hbp, filter.Hhp);

		return true;
	}

	/**
	 * Set the emulated SID model
	 * 
	 * @param model
	 */
	public void model(sid2_model_t model) {
		if (model == sid2_model_t.SID2_MOS8580)
			m_sid.set_chip_model(chip_model.MOS8580);
		else
			m_sid.set_chip_model(chip_model.MOS6581);
	}

	// Must lock the SID before using the standard functions

	/**
	 * Set execution environment and lock sid to it
	 * 
	 * @param env
	 * @return
	 */
	public boolean lock(C64Env env) {
		if (env == null) {
			if (!m_locked)
				return false;
			m_locked = false;
			m_context = null;
		} else {
			if (m_locked)
				return false;
			m_locked = true;
			m_context = env.context();
		}
		return true;
	}

}
