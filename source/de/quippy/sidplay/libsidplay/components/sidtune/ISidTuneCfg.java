/**
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
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package de.quippy.sidplay.libsidplay.components.sidtune;

public interface ISidTuneCfg {

	/**
	 * Define to add PSID2NG support
	 */
	boolean SIDTUNE_PSID2NG = true;

	/**
	 * Minimum load address for real c64 only tunes
	 */
	int SIDTUNE_R64_MIN_LOAD_ADDR = 0x07e8;

}
