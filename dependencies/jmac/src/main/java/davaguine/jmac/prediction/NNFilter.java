/*
 *  21.04.2004 Original verion. davagin@udm.ru.
 *-----------------------------------------------------------------------
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
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

package davaguine.jmac.prediction;

import davaguine.jmac.tools.JMACException;
import davaguine.jmac.tools.RollBufferShort;

import java.util.Arrays;

/**
 * Author: Dmitry Vaguine
 * Date: 04.03.2004
 * Time: 14:51:31
 */
public abstract class NNFilter {
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

    public int Compress(int nInput) {
        RollBufferShort input = m_rbInput;
        RollBufferShort delta = m_rbDeltaM;
        int order = m_nOrder;
        int shift = m_nShift;
        short[] pary = m_paryM;
        short[] inputData = input.m_pData;
        int inputIndex = input.index;
        short[] deltaData = delta.m_pData;
        int deltaIndex = delta.index;

        // convert the input to a short and store it
        inputData[inputIndex] = (short) ((nInput >= Short.MIN_VALUE && nInput <= Short.MAX_VALUE) ? nInput : (nInput >> 31) ^ 0x7FFF);

        // figure a dot product
        int nDotProduct = CalculateDotProductNoMMX(inputData, inputIndex - order, pary, 0);

        // calculate the output
        int nOutput = nInput - ((nDotProduct + (1 << (m_nShift - 1))) >> m_nShift);

        // adapt
        AdaptNoMMX(pary, 0, deltaData, deltaIndex - order, nOutput);

        int nTempABS = Math.abs(nInput);

        if (nTempABS > (m_nRunningAverage * 3))
            deltaData[deltaIndex] = (short) (((nInput >> 25) & 64) - 32);
        else if (nTempABS > (m_nRunningAverage * 4) / 3)
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
//        input.IncrementSafe();
        if ((++input.index) == orderPlusWindow) {
            System.arraycopy(inputData, input.index - order, inputData, 0, order);
            input.index = order;
        }
//        delta.IncrementSafe();
        if ((++delta.index) == orderPlusWindow) {
            System.arraycopy(deltaData, delta.index - order, deltaData, 0, order);
            delta.index = order;
        }

        return nOutput;
    }

    public int Decompress(int nInput) {
        // figure a dot product
        RollBufferShort input = m_rbInput;
        RollBufferShort delta = m_rbDeltaM;
        int order = m_nOrder;
        int shift = m_nShift;
        short[] pary = m_paryM;
        short[] inputData = input.m_pData;
        int inputIndex = input.index;
        short[] deltaData = delta.m_pData;
        int deltaIndex = delta.index;
        int nDotProduct = CalculateDotProductNoMMX(inputData, inputIndex - order, pary, 0);

        // adapt
        AdaptNoMMX(pary, 0, deltaData, deltaIndex - order, nInput);

        // store the output value
        int nOutput = nInput + ((nDotProduct + (1 << (shift - 1))) >> shift);

        // update the input buffer
        inputData[inputIndex] = (short) ((nOutput >= Short.MIN_VALUE && nOutput <= Short.MAX_VALUE) ? nOutput : (nOutput >> 31) ^ 0x7FFF);

        if (m_nVersion >= 3980) {
            int nTempABS = Math.abs(nOutput);

            if (nTempABS > (m_nRunningAverage * 3))
                deltaData[deltaIndex] = (short) (((nOutput >> 25) & 64) - 32);
            else if (nTempABS > (m_nRunningAverage * 4) / 3)
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
//        input.IncrementSafe();
        if ((++input.index) == orderPlusWindow) {
            System.arraycopy(inputData, input.index - order, inputData, 0, order);
            input.index = order;
        }
//        delta.IncrementSafe();
        if ((++delta.index) == orderPlusWindow) {
            System.arraycopy(deltaData, delta.index - order, deltaData, 0, order);
            delta.index = order;
        }

        return nOutput;
    }

    public void Flush() {
        Arrays.fill(m_paryM, (short) 0);
        m_rbInput.Flush();
        m_rbDeltaM.Flush();
        m_nRunningAverage = 0;
    }

    protected int m_nOrder;
    protected int m_nShift;
    protected int m_nVersion;
    protected int orderPlusWindow;
    private int m_nRunningAverage;

    private RollBufferShort m_rbInput = new RollBufferShort();
    private RollBufferShort m_rbDeltaM = new RollBufferShort();

    private short[] m_paryM;

    protected abstract int CalculateDotProductNoMMX(short[] pA, int indexA, short[] pB, int indexB);

    protected abstract void AdaptNoMMX(short[] pM, int indexM, short[] pAdapt, int indexA, int nDirection);
}
