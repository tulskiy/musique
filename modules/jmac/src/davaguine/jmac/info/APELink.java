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

package davaguine.jmac.info;

import davaguine.jmac.tools.File;
import davaguine.jmac.tools.JMACException;
import davaguine.jmac.tools.RandomAccessFile;

import java.io.IOException;

/**
 * Author: Dmitry Vaguine
 * Date: 04.03.2004
 * Time: 14:51:31
 */
public class APELink {
    private final static String APE_LINK_HEADER = "[Monkey's Audio Image Link File]";
    private final static String APE_LINK_IMAGE_FILE_TAG = "Image File=";
    private final static String APE_LINK_START_BLOCK_TAG = "Start Block=";
    private final static String APE_LINK_FINISH_BLOCK_TAG = "Finish Block=";

    public APELink(String pFilename) throws IOException {
        // empty
        m_bIsLinkFile = false;
        m_nStartBlock = 0;
        m_nFinishBlock = 0;
        m_cImageFilename = "";

        // open the file
        File ioLinkFile = new RandomAccessFile(new java.io.File(pFilename), "r");
        // create a buffer
        byte[] spBuffer = new byte[1024];

        // fill the buffer from the file and null terminate it
        int numRead = ioLinkFile.read(spBuffer);

        byte[] buffer = new byte[numRead];
        System.arraycopy(spBuffer, 0, buffer, 0, numRead);

        // call the other constructor (uses a buffer instead of opening the file)
        ParseData(buffer, pFilename);
    }

    public APELink(byte[] pData, String pFilename) {
        ParseData(pData, pFilename);
    }

    public boolean GetIsLinkFile() {
        return m_bIsLinkFile;
    }

    public int GetStartBlock() {
        return m_nStartBlock;
    }

    public int GetFinishBlock() {
        return m_nFinishBlock;
    }

    public String GetImageFilename() {
        return m_cImageFilename;
    }

    protected boolean m_bIsLinkFile;
    protected int m_nStartBlock;
    protected int m_nFinishBlock;
    protected String m_cImageFilename;

    protected void ParseData(byte[] pData, String pFilename) {
        // empty
        m_bIsLinkFile = false;
        m_nStartBlock = 0;
        m_nFinishBlock = 0;
        m_cImageFilename = "";

        if (pData != null) {
            String data = null;
            try {
                // parse out the information
                data = new String(pData, "US-ASCII");
            } catch (java.io.UnsupportedEncodingException e) {
                throw new JMACException("Unsupported encoding", e);
            }

            int pHeader = data.indexOf(APE_LINK_HEADER);
            int pImageFile = data.indexOf(APE_LINK_IMAGE_FILE_TAG);
            int pStartBlock = data.indexOf(APE_LINK_START_BLOCK_TAG);
            int pFinishBlock = data.indexOf(APE_LINK_FINISH_BLOCK_TAG);

            if (pHeader >= 0 && pImageFile >= 0 && pStartBlock >= 0 && pFinishBlock >= 0) {
                // get the start and finish blocks
                int i1 = data.indexOf('\r', pStartBlock);
                int i2 = data.indexOf('\n', pStartBlock);
                int ii = i1 > 0 && i2 > 0 ? Math.min(i1, i2) : Math.max(i1, i2);

                try {
                    m_nStartBlock = Integer.parseInt(data.substring(pStartBlock + APE_LINK_START_BLOCK_TAG.length(), ii >= 0 ? ii : data.length()));
                } catch (Exception e) {
                    m_nStartBlock = -1;
                }

                i1 = data.indexOf('\r', pFinishBlock);
                i2 = data.indexOf('\n', pFinishBlock);
                ii = i1 > 0 && i2 > 0 ? Math.min(i1, i2) : Math.max(i1, i2);
                try {
                    m_nFinishBlock = Integer.parseInt(data.substring(pFinishBlock + APE_LINK_FINISH_BLOCK_TAG.length(), ii >= 0 ? ii : data.length()));
                } catch (Exception e) {
                    m_nFinishBlock = -1;
                }

                // get the path
                i1 = data.indexOf('\r', pImageFile);
                i2 = data.indexOf('\n', pImageFile);
                ii = i1 > 0 && i2 > 0 ? Math.min(i1, i2) : Math.max(i1, i2);
                String cImageFile = data.substring(pImageFile + APE_LINK_IMAGE_FILE_TAG.length(), ii >= 0 ? ii : data.length());

                // process the path
                if (cImageFile.lastIndexOf('\\') < 0) {
                    int ij = pFilename.lastIndexOf('\\');
                    m_cImageFilename = ij >= 0 ? pFilename.substring(0, ij) + cImageFile : cImageFile;
                } else {
                    m_cImageFilename = cImageFile;
                }

                // this is a valid link file
                m_bIsLinkFile = true;
            }
        }
    }
}
