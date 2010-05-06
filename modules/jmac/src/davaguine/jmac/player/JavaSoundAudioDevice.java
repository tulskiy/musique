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

import javax.sound.sampled.*;

/**
 * Author: Dmitry Vaguine
 * Date: 04.03.2004
 * Time: 14:51:31
 */
public class JavaSoundAudioDevice extends AudioDeviceBase {
    private SourceDataLine source = null;

    private AudioFormat fmt = null;

    protected void setAudioFormat(AudioFormat fmt0) {
        fmt = fmt0;
    }

    protected AudioFormat getAudioFormat() {
        if (fmt == null) {
            IAPEDecompress decoder = getDecoder();
            fmt = new AudioFormat(decoder.getApeInfoSampleRate(),
                    decoder.getApeInfoBitsPerSample(),
                    decoder.getApeInfoChannels(),
                    true,
                    false);
        }
        return fmt;
    }

    protected DataLine.Info getSourceLineInfo() {
        AudioFormat fmt = getAudioFormat();
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, fmt, 4000);
        return info;
    }

    public void open(AudioFormat fmt) {
        if (!isOpen()) {
            setAudioFormat(fmt);
            openImpl();
            setOpen(true);
        }
    }

    protected void openImpl() {
    }


    // createSource fix.
    protected void createSource() {
        Throwable t = null;
        try {
            Line line = AudioSystem.getLine(getSourceLineInfo());
            if (line instanceof SourceDataLine) {
                source = (SourceDataLine) line;
                source.open(fmt, millisecondsToBytes(fmt, 2000));
                source.start();
            }
        } catch (RuntimeException ex) {
            t = ex;
        } catch (LinkageError ex) {
            t = ex;
        } catch (LineUnavailableException ex) {
            t = ex;
        }
        if (source == null) throw new JMACPlayerException("cannot obtain source audio line", t);
    }

    public int millisecondsToBytes(AudioFormat fmt, int time) {
        return (int) (time * (fmt.getSampleRate() * fmt.getChannels() * fmt.getSampleSizeInBits()) / 8000.0);
    }

    protected void closeImpl() {
        if (source != null) {
            source.close();
        }
    }

    protected void writeImpl(byte[] samples, int offs, int len) {
        if (source == null)
            createSource();

        source.write(samples, offs, len);
    }

    protected void flushImpl() {
        if (source != null) {
            source.drain();
        }
    }

    public int getPosition() {
        int pos = 0;
        if (source != null) {
            pos = (int) (source.getMicrosecondPosition() / 1000);
        }
        return pos;
    }

    /**
     * Runs a short test by playing a short silent sound.
     */
    public void test() {
        try {
            open(new AudioFormat(22050, 16, 1, true, false));
            byte[] data = new byte[22050 / 5];
            write(data, 0, data.length);
            flush();
            close();
        } catch (RuntimeException ex) {
            throw new JMACPlayerException("Device test failed: ", ex);
        }
    }
}
