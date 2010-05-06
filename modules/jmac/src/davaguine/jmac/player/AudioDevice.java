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

import davaguine.jmac.decoder.IAPEDecompress;

/**
 * Author: Dmitry Vaguine
 * Date: 04.03.2004
 * Time: 14:51:31
 */
public interface AudioDevice {
    /**
     * Prepares the AudioDevice for playback of audio samples.
     *
     * @param decoder The core that will be providing the audio
     *                samples.
     *                <p/>
     *                If the audio device is already open, this method returns silently.
     */
    public void open(IAPEDecompress decoder);

    /**
     * Retrieves the open state of this audio device.
     *
     * @return <code>true</code> if this audio device is open and playing
     *         audio samples, or <code>false</code> otherwise.
     */
    public boolean isOpen();

    /**
     * Writes a number of samples to this <code>AudioDevice</code>.
     *
     * @param samples The array of samples to write
     *                to the audio device.
     * @param offs    The offset of the first sample.
     * @param len     The number of samples to write.
     *                <p/>
     *                This method may return prior to the samples actually being played
     *                by the audio device.
     */
    public void write(byte[] samples, int offs, int len);


    /**
     * Closes this audio device. Any currently playing audio is stopped
     * as soon as possible. Any previously written audio data that has not been heard
     * is discarded.
     * <p/>
     * The implementation should ensure that any threads currently blocking
     * on the device (e.g. during a <code>write</code> or <code>flush</code>
     * operation should be unblocked by this method.
     */
    public void close();


    /**
     * Blocks until all audio samples previously written to this audio device have
     * been heard.
     */
    public void flush();

    /**
     * Retrieves the current playback position in milliseconds.
     */
    public int getPosition();
}
