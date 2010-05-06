/**
 *  @author : Paul Taylor
 *
 *  Version @version:$Id: ImageFormats.java,v 1.10 2008/12/10 13:14:30 paultaylor Exp $
 *
 *  MusicTag Copyright (C)2003,2004
 *
 *  This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser
 *  General Public  License as published by the Free Software Foundation; either version 2.1 of the License,
 *  or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 *  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License along with this library; if not,
 *  you can get a copy from http://www.opensource.org/licenses/lgpl-license.php or write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Description:
 * This class maps from v2.2 Image formats (PIC) to v2.3/v2.4 Mimetypes (APIC) and
 *  vice versa.
 */
package org.jaudiotagger.tag.id3.valuepair;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the image formats support by ID3, provides a mapping between the format field supported in ID3v22 and the
 * mimetype field supported by ID3v23/ID3v24.
 */
public class ImageFormats {
    public static final String V22_JPG_FORMAT = "JPG";
    public static final String V22_PNG_FORMAT = "PNG";
    public static final String V22_GIF_FORMAT = "GIF";
    public static final String V22_BMP_FORMAT = "COVERART_BMP";

    public static final String MIME_TYPE_JPEG = "image/jpeg";
    public static final String MIME_TYPE_PNG = "image/png";
    public static final String MIME_TYPE_GIF = "image/gif";
    public static final String MIME_TYPE_BMP = "image/bmp";

    /**
     * Sometimes this is used for jpg instead :or have I made this up
     */
    public static final String MIME_TYPE_JPG = "image/jpg";

    private static Map<String, String> imageFormatsToMimeType = new HashMap<String, String>();
    private static Map<String, String> imageMimeTypeToFormat = new HashMap<String, String>();

    static {
        imageFormatsToMimeType.put(V22_JPG_FORMAT, MIME_TYPE_JPEG);
        imageFormatsToMimeType.put(V22_PNG_FORMAT, MIME_TYPE_PNG);
        imageFormatsToMimeType.put(V22_GIF_FORMAT, MIME_TYPE_GIF);
        imageFormatsToMimeType.put(V22_BMP_FORMAT, MIME_TYPE_BMP);
        String value;
        for (String key : imageFormatsToMimeType.keySet()) {
            value = imageFormatsToMimeType.get(key);
            imageMimeTypeToFormat.put(value, key);
        }

        //The mapping isnt one-one lets add other mimetypes
        imageMimeTypeToFormat.put(MIME_TYPE_JPG, V22_JPG_FORMAT);
    }

    /**
     * Get v2.3 mimetype from v2.2 format
     */
    public static String getMimeTypeForFormat(String format) {
        return imageFormatsToMimeType.get(format);
    }

    /**
     * Get v2.2 format from v2.3 mimetype
     */
    public static String getFormatForMimeType(String mimeType) {
        return imageMimeTypeToFormat.get(mimeType);
    }

    /**
     * @param data
     * @return true if binary data matches expected header for a png
     */
    public static boolean binaryDataIsPngFormat(byte[] data) {
        //Read signature
        if ((0x89 == (data[0] & 0xff)) && (0x50 == (data[1] & 0xff)) && (0x4E == (data[2] & 0xff)) && (0x47 == (data[3] & 0xff))) {
            return true;
        }
        return false;
    }

    /**
     * @param data
     * @return true if binary data matches expected header for a jpg
     */
    public static boolean binaryDataIsJpgFormat(byte[] data) {
        //Read signature
        if ((0xff == (data[0] & 0xff)) && (0xd8 == (data[1] & 0xff)) && (0xff == (data[2] & 0xff)) && (0xff == (data[3] & 0xe0))) {
            return true;
        }
        return false;
    }

    /**
     * @param data
     * @return true if binary data matches expected header for a gif
     */
    public static boolean binaryDataIsGifFormat(byte[] data) {
        //Read signature
        if ((0x47 == (data[0] & 0xff)) && (0x49 == (data[1] & 0xff)) && (0x46 == (data[2] & 0xff))) {
            return true;
        }
        return false;
    }

    /**
     * @param data
     * @return true if binary data matches expected header for a bmp
     */
    public static boolean binaryDataIsBmpFormat(byte[] data) {
        //Read signature
        if ((0x42 == (data[0] & 0xff)) && (0x4d == (data[1] & 0xff)) && (0x3c == (data[2] & 0xff))) {
            return true;
        }
        return false;
    }

    /**
     * @param data
     * @return correct mimetype for the image data represented by this byte data
     */
    public static String getMimeTypeForBinarySignature(byte[] data) {
        if (binaryDataIsPngFormat(data)) {
            return MIME_TYPE_PNG;
        } else if (binaryDataIsJpgFormat(data)) {
            return MIME_TYPE_JPEG;
        } else if (binaryDataIsGifFormat(data)) {
            return MIME_TYPE_GIF;
        } else if (binaryDataIsBmpFormat(data)) {
            return MIME_TYPE_BMP;
        } else {
            return null;
        }
    }
}
