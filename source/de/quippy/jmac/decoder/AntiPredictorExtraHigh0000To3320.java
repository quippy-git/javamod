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

package de.quippy.jmac.decoder;


/**
 * Author: Dmitry Vaguine
 * Date: 04.03.2004
 * Time: 14:51:31
 */
public class AntiPredictorExtraHigh0000To3320 extends AntiPredictor {
    public void AntiPredict(int[] pInputArray, int[] pOutputArray, int NumberOfElements, int Iterations, long[] pOffsetValueArrayA, long[] pOffsetValueArrayB) {
        for (int z = Iterations; z >= 0; z--) {
            AntiPredictorOffset(pInputArray, pOutputArray, NumberOfElements, (int) pOffsetValueArrayB[z], -1, 64);
            AntiPredictorOffset(pOutputArray, pInputArray, NumberOfElements, (int) pOffsetValueArrayA[z], 1, 64);
        }

        AntiPredictor.antiPredict(pInputArray, pOutputArray, NumberOfElements);
    }

    private AntiPredictorHigh0000To3320 AntiPredictor = new AntiPredictorHigh0000To3320();

    protected void AntiPredictorOffset(int[] Input_Array, int[] Output_Array, int Number_of_Elements, int g, int dm, int Max_Order) {
        int q;

        if ((g == 0) || (Number_of_Elements <= Max_Order)) {
            System.arraycopy(Input_Array, 0, Output_Array, 0, Number_of_Elements);
            return;
        }

        System.arraycopy(Input_Array, 0, Output_Array, 0, Max_Order);

        if (dm > 0)
            for (q = Max_Order; q < Number_of_Elements; q++) {
                Output_Array[q] = Input_Array[q] + (Output_Array[q - g] >> 3);
            }
        else
            for (q = Max_Order; q < Number_of_Elements; q++) {
                Output_Array[q] = Input_Array[q] - (Output_Array[q - g] >> 3);
            }
    }
}
