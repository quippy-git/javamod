/**
 *             ReSID builder class for creating/controlling resids
 *             ---------------------------------------------------
 *  begin                : Wed Sep 5 2001
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

import java.util.ArrayList;

import de.quippy.sidplay.libsidplay.common.C64Env;
import de.quippy.sidplay.libsidplay.common.SIDBuilder;
import de.quippy.sidplay.libsidplay.common.SIDEmu;
import de.quippy.sidplay.libsidplay.common.ISID2Types.sid2_model_t;


/*******************************************************************************
 * Since ReSID is not part of this project we are actually creating a wrapper
 * instead of implementing a SID emulation
 * *****************************************************************************
 * ReSID Builder Class
 * *****************************************************************************
 * Create the SID builder object
 */
public class ReSIDBuilder extends SIDBuilder {

	protected ArrayList < SIDEmu > sidobjs = new ArrayList < SIDEmu >();

	// Error String(s)

	private static final String ERR_FILTER_DEFINITION = "RESID ERROR: Filter definition is not valid (see docs).";

	private String m_error;

	public ReSIDBuilder(final String name) {
		super(name);
		m_error = "N/A";
	}

	/**
	 * @param created
	 * true will give you the number of used devices.<BR>
	 * false will give you all available sids.
	 * @return
	 * <UL>
	 * <LI> used==true? 0 none, >0 is used sids
	 * <LI> used==false? 0 endless, >0 is available sids.
	 * </UL>
	 * use bool operator to determine error
	 */
	public int /* uint */devices(boolean created) {
		m_status = true;
		if (created)
			return sidobjs.size();
		else
			// Available devices
			return 0;
	}

	/**
	 * Create a new sid emulation. Called by libsidplay2 only.
	 * 
	 * @param sids
	 * @return
	 */
	public int create(int sids) {
		int count;
		ReSID sid = null;
		m_status = true;

		// Check available devices
		count = devices(false);
		if (!m_status) {
			m_status = false;
			return count;
		}
		if ((count != 0) && (count < sids))
			sids = count;

		for (count = 0; count < sids; count++) {
			sid = new ReSID(this);

			// SID init failed?
			if (!sid.bool()) {
				m_error = sid.error();
				m_status = false;
				sid = null;
				return count;
			}
			sidobjs.add(sid);
		}
		return count;
	}

	/**
	 * Find a free SID of the required specs
	 */
	public SIDEmu lock(C64Env env, sid2_model_t model) {
		int size = sidobjs.size();
		m_status = true;

		for (int i = 0; i < size; i++) {
			ReSID sid = (ReSID) sidobjs.get(i);
			if (sid.lock(env)) {
				sid.model(model);
				return sid;
			}
		}
		// Unable to locate free SID
		m_status = false;
		m_error = name() + " ERROR: No available SIDs to lock";
		return null;
	}

	/**
	 * Allow something to use this SID
	 */
	public void unlock(SIDEmu device) {
		int size = sidobjs.size();
		// Maek sure this is our SID
		for (int i = 0; i < size; i++) {
			ReSID sid = (ReSID) sidobjs.get(i);
			if (sid == device) {
				// Unlock it
				sid.lock(null);
				break;
			}
		}
	}

	/**
	 * Remove all SID emulations.
	 */
	public void remove() {
		int size = sidobjs.size();
		for (int i = 0; i < size; i++)
			sidobjs.remove(sidobjs.get(i));
		sidobjs.clear();
	}

	public final String error() {
		return m_error;
	}

	public final String credits() {
		m_status = true;

		// Available devices
		if (sidobjs.size() != 0) {
			ReSID sid = (ReSID) sidobjs.get(0);
			return sid.credits();
		}

		{ // Create an emulation to obtain credits
			ReSID sid = new ReSID(this);
			return sid.credits();
		}
	}

	// Settings that affect all SIDS

	public void filter(boolean enable) {
		int size = sidobjs.size();
		m_status = true;
		for (int i = 0; i < size; i++) {
			ReSID sid = (ReSID) sidobjs.get(i);
			sid.filter(enable);
		}
	}

	public void filter(final sid_filter_t filter) {
		int size = sidobjs.size();
		m_status = true;
		for (int i = 0; i < size; i++) {
			ReSID sid = (ReSID) sidobjs.get(i);
			if (!sid.filter(filter)) {
				m_error = ERR_FILTER_DEFINITION;
				m_status = false;
			}
		}
	}

	public void sampling(long /* uint_least32_t */freq) {
		int size = sidobjs.size();
		m_status = true;
		for (int i = 0; i < size; i++) {
			ReSID sid = (ReSID) sidobjs.get(i);
			sid.sampling(freq);
		}
	}
}
