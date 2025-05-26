/*
 *  21.04.2004 Original verion. davagin@udm.ru.
 *-----------------------------------------------------------------------
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
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *----------------------------------------------------------------------
 */

package de.quippy.jmac.prediction;

import java.nio.ShortBuffer;


/**
 * Author: Dmitry Vaguine
 * Date: 04.03.2004
 * Time: 14:51:31
 */
public class NNFilter1280 extends NNFilter {

    public NNFilter1280(final int nShift, final int nVersion) {
        super(1280, nShift, nVersion);
        orderPlusWindow = 1792 /* NN_WINDOW_ELEMENTS + m_nOrder */;
    }

    @Override
	protected int CalculateDotProductNoMMX(final short[] pA, final int indexA, final short[] pB, final int indexB) {
        int nDotProduct = 0;
        final ShortBuffer a = ShortBuffer.wrap(pA);
        a.position(indexA);
        final ShortBuffer b = ShortBuffer.wrap(pB);
        b.position(indexB);
        for (int i = 0; i < 40; i++) {
            nDotProduct += a.get() * b.get() +
                    a.get() * b.get() +
                    a.get() * b.get() +
                    a.get() * b.get() +
                    a.get() * b.get() +
                    a.get() * b.get() +
                    a.get() * b.get() +
                    a.get() * b.get() +
                    a.get() * b.get() +
                    a.get() * b.get() +
                    a.get() * b.get() +
                    a.get() * b.get() +
                    a.get() * b.get() +
                    a.get() * b.get() +
                    a.get() * b.get() +
                    a.get() * b.get() +
                    a.get() * b.get() +
                    a.get() * b.get() +
                    a.get() * b.get() +
                    a.get() * b.get() +
                    a.get() * b.get() +
                    a.get() * b.get() +
                    a.get() * b.get() +
                    a.get() * b.get() +
                    a.get() * b.get() +
                    a.get() * b.get() +
                    a.get() * b.get() +
                    a.get() * b.get() +
                    a.get() * b.get() +
                    a.get() * b.get() +
                    a.get() * b.get() +
                    a.get() * b.get();
/*            nDotProduct += pA[indexA++] * pB[indexB++] +
                    pA[indexA++] * pB[indexB++] +
                    pA[indexA++] * pB[indexB++] +
                    pA[indexA++] * pB[indexB++] +
                    pA[indexA++] * pB[indexB++] +
                    pA[indexA++] * pB[indexB++] +
                    pA[indexA++] * pB[indexB++] +
                    pA[indexA++] * pB[indexB++] +
                    pA[indexA++] * pB[indexB++] +
                    pA[indexA++] * pB[indexB++] +
                    pA[indexA++] * pB[indexB++] +
                    pA[indexA++] * pB[indexB++] +
                    pA[indexA++] * pB[indexB++] +
                    pA[indexA++] * pB[indexB++] +
                    pA[indexA++] * pB[indexB++] +
                    pA[indexA++] * pB[indexB++] +
                    pA[indexA++] * pB[indexB++] +
                    pA[indexA++] * pB[indexB++] +
                    pA[indexA++] * pB[indexB++] +
                    pA[indexA++] * pB[indexB++] +
                    pA[indexA++] * pB[indexB++] +
                    pA[indexA++] * pB[indexB++] +
                    pA[indexA++] * pB[indexB++] +
                    pA[indexA++] * pB[indexB++] +
                    pA[indexA++] * pB[indexB++] +
                    pA[indexA++] * pB[indexB++] +
                    pA[indexA++] * pB[indexB++] +
                    pA[indexA++] * pB[indexB++] +
                    pA[indexA++] * pB[indexB++] +
                    pA[indexA++] * pB[indexB++] +
                    pA[indexA++] * pB[indexB++] +
                    pA[indexA++] * pB[indexB++];*/

        }
        return nDotProduct;
    }

    @Override
	protected void AdaptNoMMX(final short[] pM, int indexM, final short[] pAdapt, int indexA, final int nDirection) {
        if (nDirection < 0) {
            for (int i = 0; i < 40; i++) {
                pM[indexM++] += pAdapt[indexA++];
                pM[indexM++] += pAdapt[indexA++];
                pM[indexM++] += pAdapt[indexA++];
                pM[indexM++] += pAdapt[indexA++];
                pM[indexM++] += pAdapt[indexA++];
                pM[indexM++] += pAdapt[indexA++];
                pM[indexM++] += pAdapt[indexA++];
                pM[indexM++] += pAdapt[indexA++];
                pM[indexM++] += pAdapt[indexA++];
                pM[indexM++] += pAdapt[indexA++];
                pM[indexM++] += pAdapt[indexA++];
                pM[indexM++] += pAdapt[indexA++];
                pM[indexM++] += pAdapt[indexA++];
                pM[indexM++] += pAdapt[indexA++];
                pM[indexM++] += pAdapt[indexA++];
                pM[indexM++] += pAdapt[indexA++];
                pM[indexM++] += pAdapt[indexA++];
                pM[indexM++] += pAdapt[indexA++];
                pM[indexM++] += pAdapt[indexA++];
                pM[indexM++] += pAdapt[indexA++];
                pM[indexM++] += pAdapt[indexA++];
                pM[indexM++] += pAdapt[indexA++];
                pM[indexM++] += pAdapt[indexA++];
                pM[indexM++] += pAdapt[indexA++];
                pM[indexM++] += pAdapt[indexA++];
                pM[indexM++] += pAdapt[indexA++];
                pM[indexM++] += pAdapt[indexA++];
                pM[indexM++] += pAdapt[indexA++];
                pM[indexM++] += pAdapt[indexA++];
                pM[indexM++] += pAdapt[indexA++];
                pM[indexM++] += pAdapt[indexA++];
                pM[indexM++] += pAdapt[indexA++];
            }
        } else if (nDirection > 0) {
            for (int i = 0; i < 40; i++) {
                pM[indexM++] -= pAdapt[indexA++];
                pM[indexM++] -= pAdapt[indexA++];
                pM[indexM++] -= pAdapt[indexA++];
                pM[indexM++] -= pAdapt[indexA++];
                pM[indexM++] -= pAdapt[indexA++];
                pM[indexM++] -= pAdapt[indexA++];
                pM[indexM++] -= pAdapt[indexA++];
                pM[indexM++] -= pAdapt[indexA++];
                pM[indexM++] -= pAdapt[indexA++];
                pM[indexM++] -= pAdapt[indexA++];
                pM[indexM++] -= pAdapt[indexA++];
                pM[indexM++] -= pAdapt[indexA++];
                pM[indexM++] -= pAdapt[indexA++];
                pM[indexM++] -= pAdapt[indexA++];
                pM[indexM++] -= pAdapt[indexA++];
                pM[indexM++] -= pAdapt[indexA++];
                pM[indexM++] -= pAdapt[indexA++];
                pM[indexM++] -= pAdapt[indexA++];
                pM[indexM++] -= pAdapt[indexA++];
                pM[indexM++] -= pAdapt[indexA++];
                pM[indexM++] -= pAdapt[indexA++];
                pM[indexM++] -= pAdapt[indexA++];
                pM[indexM++] -= pAdapt[indexA++];
                pM[indexM++] -= pAdapt[indexA++];
                pM[indexM++] -= pAdapt[indexA++];
                pM[indexM++] -= pAdapt[indexA++];
                pM[indexM++] -= pAdapt[indexA++];
                pM[indexM++] -= pAdapt[indexA++];
                pM[indexM++] -= pAdapt[indexA++];
                pM[indexM++] -= pAdapt[indexA++];
                pM[indexM++] -= pAdapt[indexA++];
                pM[indexM++] -= pAdapt[indexA++];
            }
        }
    }
}
