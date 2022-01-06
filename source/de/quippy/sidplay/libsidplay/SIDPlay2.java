/**
 *                                Public de.quippy.sidplay.sidplay header
 *                                ---------------------
 *  begin                : Fri Jun 9 2000
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
package de.quippy.sidplay.libsidplay;

import static de.quippy.sidplay.libsidplay.Player.SID2_TIME_BASE;
import de.quippy.sidplay.libsidplay.common.ISID2Types.sid2_config_t;
import de.quippy.sidplay.libsidplay.common.ISID2Types.sid2_info_t;
import de.quippy.sidplay.libsidplay.common.ISID2Types.sid2_player_t;
import de.quippy.sidplay.libsidplay.components.sidtune.SidTune;

public class SIDPlay2 {

	private Player sidplayer;

	public SIDPlay2() {
		sidplayer = new Player();
	}

	public final sid2_config_t config() {
		return sidplayer.config();
	}

	public final sid2_info_t info() {
		return sidplayer.info();
	}

	public int config(final sid2_config_t cfg) {
		return sidplayer.config(cfg);
	}

	public final String error() {
		return sidplayer.error();
	}

	public int fastForward(int percent) {
		return sidplayer.fastForward(percent);
	}

	public int load(SidTune tune) {
		return sidplayer.load(tune);
	}

	public void pause() {
		sidplayer.pause();
	}

	public long /* uint_least32_t */play(short[] buffer,
			int /* uint_least32_t */length) {
		return sidplayer.play(buffer, length);
	}

	public final sid2_player_t state() {
		return sidplayer.state();
	}

	public void stop() {
		sidplayer.stop();
	}

	public void debug(boolean enable) {
		sidplayer.debug(enable);
	}

	public final long /* uint_least32_t */timebase() {
		return SID2_TIME_BASE;
	}

	public final long /* uint_least32_t */time() {
		return sidplayer.time();
	}

	public final long /* uint_least32_t */mileage() {
		return sidplayer.mileage();
	}
}
