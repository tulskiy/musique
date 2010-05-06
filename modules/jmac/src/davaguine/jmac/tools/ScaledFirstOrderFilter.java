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

/**
 * Author: Dmitry Vaguine
 * Date: 04.03.2004
 * Time: 14:51:31
 */
public class ScaledFirstOrderFilter {
    public ScaledFirstOrderFilter(int multiply, int shift) {
        this.multiply = multiply;
        this.shift = shift;
    }

    public void Flush() {
        m_nLastValue = 0;
    }

    public int Compress(int nInput) {
        int nRetVal = nInput - ((m_nLastValue * multiply) >> shift);
        m_nLastValue = nInput;
        return nRetVal;
    }

    public int Decompress(int nInput) {
        m_nLastValue = nInput + ((m_nLastValue * multiply) >> shift);
        return m_nLastValue;
    }

    protected int m_nLastValue;
    protected int multiply;
    protected int shift;
}
