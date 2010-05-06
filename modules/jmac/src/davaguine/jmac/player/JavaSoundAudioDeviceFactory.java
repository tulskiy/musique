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
public class JavaSoundAudioDeviceFactory extends AudioDeviceFactory {
    private boolean tested = false;

    private static final String DEVICE_CLASS_NAME = "davaguine.jmac.player.JavaSoundAudioDevice";

    public synchronized AudioDevice createAudioDevice() {
        if (!tested) {
            testAudioDevice();
            tested = true;
        }

        try {
            return createAudioDeviceImpl();
        } catch (Exception ex) {
            throw new JMACPlayerException("unable to create JavaSound device: " + ex);
        } catch (LinkageError ex) {
            throw new JMACPlayerException("unable to create JavaSound device: " + ex);
        }
    }

    protected JavaSoundAudioDevice createAudioDeviceImpl() {
        ClassLoader loader = getClass().getClassLoader();
        try {
            JavaSoundAudioDevice dev = (JavaSoundAudioDevice) instantiate(loader, DEVICE_CLASS_NAME);
            return dev;
        } catch (Exception ex) {
            throw new JMACPlayerException("Cannot create JavaSound device", ex);
        } catch (LinkageError ex) {
            throw new JMACPlayerException("Cannot create JavaSound device", ex);
        }
    }

    public void testAudioDevice() {
        JavaSoundAudioDevice dev = createAudioDeviceImpl();
        dev.test();
    }
}
