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
 * Date: 06.05.2004
 * Time: 21:05:48
 */
public class CircleBuffer {
    // construction / destruction
    public CircleBuffer() {
        m_pBuffer = null;
        m_nTotal = 0;
        m_nHead = 0;
        m_nTail = 0;
        m_nEndCap = 0;
        m_nMaxDirectWriteBytes = 0;
    }

    // create the buffer
    public void CreateBuffer(int nBytes, int nMaxDirectWriteBytes) {
        m_nMaxDirectWriteBytes = nMaxDirectWriteBytes;
        m_nTotal = nBytes + 1 + nMaxDirectWriteBytes;
        m_pBuffer = new byte[m_nTotal];
        byteBuffer = new ByteBuffer();
        m_nHead = 0;
        m_nTail = 0;
        m_nEndCap = m_nTotal;
    }

    // query
    public int MaxAdd() {
        int nMaxAdd = (m_nTail >= m_nHead) ? (m_nTotal - 1 - m_nMaxDirectWriteBytes) - (m_nTail - m_nHead) : m_nHead - m_nTail - 1;
        return nMaxAdd;
    }

    public int MaxGet() {
        return (m_nTail >= m_nHead) ? m_nTail - m_nHead : (m_nEndCap - m_nHead) + m_nTail;
    }

    // direct writing
    public ByteBuffer GetDirectWritePointer() {
        // return a pointer to the tail -- note that it will always be safe to write
        // at least m_nMaxDirectWriteBytes since we use an end cap region
        byteBuffer.reset(m_pBuffer, m_nTail);
        return byteBuffer;
    }

    public void UpdateAfterDirectWrite(int nBytes) {
        // update the tail
        m_nTail += nBytes;

        // if the tail enters the "end cap" area, set the end cap and loop around
        if (m_nTail >= (m_nTotal - m_nMaxDirectWriteBytes)) {
            m_nEndCap = m_nTail;
            m_nTail = 0;
        }
    }

    // get data
    public int Get(byte[] pBuffer, int index, int nBytes) {
        int nTotalGetBytes = 0;

        if (pBuffer != null && nBytes > 0) {
            int nHeadBytes = Math.min(m_nEndCap - m_nHead, nBytes);
            int nFrontBytes = nBytes - nHeadBytes;

            System.arraycopy(m_pBuffer, m_nHead, pBuffer, index, nHeadBytes);
            nTotalGetBytes = nHeadBytes;

            if (nFrontBytes > 0) {
                System.arraycopy(m_pBuffer, 0, pBuffer, index + nHeadBytes, nFrontBytes);
                nTotalGetBytes += nFrontBytes;
            }

            RemoveHead(nBytes);
        }

        return nTotalGetBytes;
    }

    // remove / empty
    public void Empty() {
        m_nHead = 0;
        m_nTail = 0;
        m_nEndCap = m_nTotal;
    }

    public int RemoveHead(int nBytes) {
        nBytes = Math.min(MaxGet(), nBytes);
        m_nHead += nBytes;
        if (m_nHead >= m_nEndCap)
            m_nHead -= m_nEndCap;
        return nBytes;
    }

    public int RemoveTail(int nBytes) {
        nBytes = Math.min(MaxGet(), nBytes);
        m_nTail -= nBytes;
        if (m_nTail < 0)
            m_nTail += m_nEndCap;
        return nBytes;
    }

    private int m_nTotal;
    private int m_nMaxDirectWriteBytes;
    private int m_nEndCap;
    private int m_nHead;
    private int m_nTail;
    private byte[] m_pBuffer;
    private ByteBuffer byteBuffer;
}
