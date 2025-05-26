/* -*-mode:java; c-basic-offset:2; indent-tabs-mode:nil -*- */
/* JOrbis
 * Copyright (C) 2000 ymnk, JCraft,Inc.
 *
 * Written by: 2000 ymnk<ymnk@jcraft.com>
 *
 * Many thanks to
 *   Monty <monty@xiph.org> and
 *   The XIPHOPHORUS Company http://www.xiph.org/ .
 * JOrbis has been based on their awesome works, Vorbis codec.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public License
 * as published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package de.quippy.ogg.jorbis;

import de.quippy.ogg.jogg.Buffer;

class Time0 extends FuncTime{
  @Override
void pack(final Object i, final Buffer opb){
  }

  @Override
Object unpack(final Info vi, final Buffer opb){
    return "";
  }

  @Override
Object look(final DspState vd, final InfoMode mi, final Object i){
    return "";
  }

  @Override
void free_info(final Object i){
  }

  @Override
void free_look(final Object i){
  }

  @Override
int inverse(final Block vb, final Object i, final float[] in, final float[] out){
    return 0;
  }
}
