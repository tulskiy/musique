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

package davaguine.jmac.player;

/**
 * Author: Dmitry Vaguine
 * Date: 04.03.2004
 * Time: 14:51:31
 */
public abstract class AudioDeviceFactory {
    /**
     * Creates a new <code>AudioDevice</code>.
     *
     * @return	a new instance of a specific class of <code>AudioDevice</code>.
     */
    public abstract AudioDevice createAudioDevice();

    /**
     * Creates an instance of an AudioDevice implementation.
     *
     * @param loader The <code>ClassLoader</code> to use to
     *               load the named class, or null to use the
     *               system class loader.
     * @param name   The name of the class to load.
     * @return			A newly-created instance of the audio device class.
     */
    protected AudioDevice instantiate(ClassLoader loader, String name)
            throws ClassNotFoundException,
            IllegalAccessException,
            InstantiationException {
        AudioDevice dev = null;

        Class cls = null;
        if (loader == null) {
            cls = Class.forName(name);
        } else {
            cls = loader.loadClass(name);
        }

        Object o = cls.newInstance();
        dev = (AudioDevice) o;

        return dev;
    }
}
