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

package davaguine.jmac.tools;

import java.util.Arrays;

/**
 * Author: Dmitry Vaguine
 * Date: 04.03.2004
 * Time: 14:51:31
 */
public class RollBufferShort {
    public RollBufferShort() {
        m_pData = null;
    }

    public int Create(int nWindowElements, int nHistoryElements) {
        m_nWindowElements = nWindowElements;
        m_nHistoryElements = nHistoryElements;
        windowPlusHistory = nWindowElements += nHistoryElements;

        m_pData = new short[nWindowElements];

        Flush();
        return 0;
    }

    public void Flush() {
        Arrays.fill(m_pData, 0, m_nHistoryElements, (short) 0);
        index = m_nHistoryElements;
    }

    public void IncrementSafe() {
        if ((++index) == windowPlusHistory) {
            short aword0[];
            int i;
            System.arraycopy(aword0 = m_pData, index - (i = m_nHistoryElements), aword0, 0, i);
            index = i;
        }
    }

    public short[] m_pData;
    public int index;

    protected int m_nHistoryElements;
    protected int m_nWindowElements;
    protected int windowPlusHistory;
}
