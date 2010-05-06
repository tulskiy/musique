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
public class Globals {
    private static final boolean SHOW_ACCESS_CONTROL_EXCEPTIONS = false;
    private static final String PROPERTY_PREFIX = "jmac.";

    public final static int MAC_VERSION_NUMBER = 3990;
    public final static boolean DEBUG = getBooleanProperty("DEBUG");
//    public final static boolean NATIVE = getBooleanProperty("NATIVE");

    private static boolean getBooleanProperty(String strName) {
        String strPropertyName = PROPERTY_PREFIX + strName;
        String strValue = "false";
        try {
            strValue = System.getProperty(strPropertyName, "false");
        } catch (Exception e) {
            if (SHOW_ACCESS_CONTROL_EXCEPTIONS)
                e.printStackTrace();
        }
        return strValue.toLowerCase().equals("true");
    }

    public static boolean isNative() {
        return getBooleanProperty("NATIVE");
    }
}
