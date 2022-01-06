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

import de.quippy.jmac.tools.JMACException;
import de.quippy.jmac.tools.RollBufferShort;

import java.util.Arrays;

/**
 * Author: Dmitry Vaguine
 * Date: 04.03.2004
 * Time: 14:51:31
 */
public abstract class NNFilter {
    protected int m_nOrder;
    protected int m_nShift;
    protected int m_nVersion;
    protected int orderPlusWindow;
    private int m_nRunningAverage;

    private RollBufferShort m_rbInput = new RollBufferShort();
    private RollBufferShort m_rbDeltaM = new RollBufferShort();

    private short[] m_paryM;

    public final static int NN_WINDOW_ELEMENTS = 512;

    public NNFilter(int nOrder, int nShift, int nVersion) {
        if ((nOrder <= 0) || ((nOrder % 16) != 0))
            throw new JMACException("Wrong Order");
        m_nOrder = nOrder;
        m_nShift = nShift;
        m_nVersion = nVersion;
        m_rbInput.Create(512 /* NN_WINDOW_ELEMENTS */, nOrder);
        m_rbDeltaM.Create(512 /*NN_WINDOW_ELEMENTS */, nOrder);
        m_paryM = new short[nOrder];
    }

    public int Compress(final int nInput) {

        final short[] inputData = m_rbInput.m_pData;
        final int inputIndex = m_rbInput.index;
        // convert the input to a short and store it
        inputData[inputIndex] = (short) ((nInput >= Short.MIN_VALUE && nInput <= Short.MAX_VALUE) ? nInput : (nInput >> 31) ^ 0x7FFF);

        // figure a dot product
        final int nDotProduct = CalculateDotProductNoMMX(inputData, inputIndex - m_nOrder, m_paryM, 0);

        // calculate the output
        final int nOutput = nInput - ((nDotProduct + (1 << (m_nShift - 1))) >> m_nShift);

        final short[] deltaData = m_rbDeltaM.m_pData;
        final int deltaIndex = m_rbDeltaM.index;
        // adapt
        AdaptNoMMX(m_paryM, 0, deltaData, deltaIndex - m_nOrder, nOutput);

        final int nTempABS = Math.abs(nInput);

        if (nTempABS > (m_nRunningAverage * 3))
            deltaData[deltaIndex] = (short) (((nInput >> 25) & 64) - 32);
        else if (nTempABS > (m_nRunningAverage<<2) / 3)
            deltaData[deltaIndex] = (short) (((nInput >> 26) & 32) - 16);
        else if (nTempABS > 0)
            deltaData[deltaIndex] = (short) (((nInput >> 27) & 16) - 8);
        else
            deltaData[deltaIndex] = (short) 0;

        m_nRunningAverage += (nTempABS - m_nRunningAverage) / 16;
        
        deltaData[deltaIndex - 1] >>= 1;
        deltaData[deltaIndex - 2] >>= 1;
        deltaData[deltaIndex - 8] >>= 1;

        // increment and roll if necessary
//        m_rbInput.IncrementSafe();
        if ((++m_rbInput.index) == orderPlusWindow) {
            System.arraycopy(inputData, m_rbInput.index - m_nOrder, inputData, 0, m_nOrder);
            m_rbInput.index = m_nOrder;
        }
//        m_rbDeltaM.IncrementSafe();
        if ((++m_rbDeltaM.index) == orderPlusWindow) {
            System.arraycopy(deltaData, m_rbDeltaM.index - m_nOrder, deltaData, 0, m_nOrder);
            m_rbDeltaM.index = m_nOrder;
        }

        return nOutput;
    }

    public int Decompress(final int nInput) {
        
        final short[] inputData = m_rbInput.m_pData;
        final int inputIndex = m_rbInput.index;
        // figure a dot product
        final int nDotProduct = CalculateDotProductNoMMX(inputData, inputIndex - m_nOrder, m_paryM, 0);

        final short[] deltaData = m_rbDeltaM.m_pData;
        final int deltaIndex = m_rbDeltaM.index;
        // adapt
        AdaptNoMMX(m_paryM, 0, deltaData, deltaIndex - m_nOrder, nInput);

        // store the output value
        final int nOutput = nInput + ((nDotProduct + (1 << (m_nShift - 1))) >> m_nShift);

        // update the input buffer
        inputData[inputIndex] = (short) ((nOutput >= Short.MIN_VALUE && nOutput <= Short.MAX_VALUE) ? nOutput : (nOutput >> 31) ^ 0x7FFF);

        if (m_nVersion >= 3980) {
            final int nTempABS = Math.abs(nOutput);

            if (nTempABS > (m_nRunningAverage * 3))
                deltaData[deltaIndex] = (short) (((nOutput >> 25) & 64) - 32);
            else if (nTempABS > (m_nRunningAverage<<2) / 3)
                deltaData[deltaIndex] = (short) (((nOutput >> 26) & 32) - 16);
            else if (nTempABS > 0)
                deltaData[deltaIndex] = (short) (((nOutput >> 27) & 16) - 8);
            else
                deltaData[deltaIndex] = 0;

            m_nRunningAverage += (nTempABS - m_nRunningAverage) / 16;

            deltaData[deltaIndex - 1] >>= 1;
            deltaData[deltaIndex - 2] >>= 1;
            deltaData[deltaIndex - 8] >>= 1;
        } else {
            deltaData[deltaIndex] = (short) ((nOutput == 0) ? 0 : ((nOutput >> 28) & 8) - 4);
            deltaData[deltaIndex - 4] >>= 1;
            deltaData[deltaIndex - 8] >>= 1;
        }

        // increment and roll if necessary
//        m_rbInput.IncrementSafe();
        if ((++m_rbInput.index) == orderPlusWindow) {
            System.arraycopy(inputData, m_rbInput.index - m_nOrder, inputData, 0, m_nOrder);
            m_rbInput.index = m_nOrder;
        }
//        m_rbDeltaM.IncrementSafe();
        if ((++m_rbDeltaM.index) == orderPlusWindow) {
            System.arraycopy(deltaData, m_rbDeltaM.index - m_nOrder, deltaData, 0, m_nOrder);
            m_rbDeltaM.index = m_nOrder;
        }

        return nOutput;
    }

    public void Flush() {
        Arrays.fill(m_paryM, (short) 0);
        m_rbInput.Flush();
        m_rbDeltaM.Flush();
        m_nRunningAverage = 0;
    }

    protected abstract int CalculateDotProductNoMMX(final short[] pA, int indexA, final short[] pB, int indexB);
    protected abstract void AdaptNoMMX(final short[] pM, int indexM, final short[] pAdapt, int indexA, final int nDirection);
}
