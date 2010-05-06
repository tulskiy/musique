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
public abstract class AudioDeviceBase implements AudioDevice {
    private boolean open = false;

    private IAPEDecompress decoder = null;

    /**
     * Opens this audio device.
     *
     * @param decoder The core that will provide audio data
     *                to this audio device.
     */
    public synchronized void open(IAPEDecompress decoder) {
        if (!isOpen()) {
            this.decoder = decoder;
            openImpl();
            setOpen(true);
        }
    }

    /**
     * Template method to provide the
     * implementation for the opening of the audio device.
     */
    protected void openImpl() {
    }

    /**
     * Sets the open state for this audio device.
     */
    protected void setOpen(boolean open) {
        this.open = open;
    }

    /**
     * Determines if this audio device is open or not.
     *
     * @return <code>true</code> if the audio device is open,
     *         <code>false</code> if it is not.
     */
    public synchronized boolean isOpen() {
        return open;
    }

    /**
     * Closes this audio device. If the device is currently playing
     * audio, playback is stopped immediately without flushing
     * any buffered audio data.
     */
    public synchronized void close() {
        if (isOpen()) {
            closeImpl();
            setOpen(false);
            decoder = null;
        }
    }

    /**
     * Template method to provide the implementation for
     * closing the audio device.
     */
    protected void closeImpl() {
    }

    /**
     * Writes audio data to this audio device. Audio data is
     * assumed to be in the output format of the core. This
     * method may return before the data has actually been sounded
     * by the device if the device buffers audio samples.
     *
     * @param samples The samples to write to the audio device.
     * @param offs    The offset into the array of the first sample to write.
     * @param len     The number of samples from the array to write.
     *                If the audio device is not open, this method does nthing.
     */
    public void write(byte[] samples, int offs, int len) {
        if (isOpen()) {
            writeImpl(samples, offs, len);
        }
    }

    /**
     * Template method to provide the implementation for
     * writing audio samples to the audio device.
     */
    protected void writeImpl(byte[] samples, int offs, int len) {
    }

    /**
     * Waits for any buffered audio samples to be played by the
     * audio device. This method should only be called prior
     * to closing the device.
     */
    public void flush() {
        if (isOpen()) {
            flushImpl();
        }
    }

    /**
     * Template method to provide the implementation for
     * flushing any buffered audio data.
     */
    protected void flushImpl() {
    }

    /**
     * Retrieves the core that provides audio data to this
     * audio device.
     *
     * @return The associated core.
     */
    protected IAPEDecompress getDecoder() {
        return decoder;
    }
}
