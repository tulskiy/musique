/*
 * Copyright (c) 2008, 2009, 2010, 2011 Denis Tulskiy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with this work.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.tulskiy.musique.audio.player.io;

import javax.sound.sampled.*;
import java.util.logging.Logger;

/**
 * Author: Denis Tulskiy
 * Date: Jul 25, 2010
 */
public class AudioOutput {
    public static final int BUFFER_SIZE = (int) (Math.pow(2, 15) / 24) * 24;
    private final Logger logger = Logger.getLogger("musique");

    private SourceDataLine line;
    private FloatControl volumeControl;
    private boolean mixerChanged;
    private Mixer mixer;
    private float volume = 1f;
    private boolean linearVolume = false;

    public void init(AudioFormat fmt) throws LineUnavailableException {
        //if it is same format and the line is opened, do nothing
        if (line != null && line.isOpen()) {
            if (mixerChanged || !line.getFormat().matches(fmt)) {
                mixerChanged = false;
                line.drain();
                line.close();
                line = null;
            } else {
                return;
            }
        }
        logger.fine("Audio format: " + fmt);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, fmt, BUFFER_SIZE);
        logger.fine("Dataline info: " + info);
        if (mixer != null && mixer.isLineSupported(info)) {
            line = (SourceDataLine) mixer.getLine(info);
            logger.fine("Mixer: " + mixer.getMixerInfo().getDescription());
        } else {
            line = AudioSystem.getSourceDataLine(fmt);
            mixer = null;
        }
        logger.fine("Line: " + line);
        line.open(fmt, BUFFER_SIZE);
        line.start();
        if (line.isControlSupported(FloatControl.Type.VOLUME)) {
            volumeControl = (FloatControl) line.getControl(FloatControl.Type.VOLUME);
            volumeControl.setValue(volume * volumeControl.getMaximum());
            linearVolume = true;
        } else if (line.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            volumeControl = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
            volumeControl.setValue(linearToDb(volume));
            linearVolume = false;
        }
    }

    public void stop() {
        if (line != null && line.isOpen())
            line.stop();
    }

    public void start() {
        if (line != null && line.isOpen())
            line.start();
    }

    public void close() {
        if (line != null) {
            line.close();
        }
    }

    public void flush() {
        if (line != null && line.isOpen())
            line.flush();
    }

    public void write(byte[] buf, int offset, int len) {
        line.write(buf, offset, len);
    }

    public void setVolume(float volume) {
        this.volume = volume;
        if (volumeControl != null) {
            if (linearVolume)
                volumeControl.setValue(volumeControl.getMaximum() * volume);
            else
                volumeControl.setValue(linearToDb(volume));
        }
    }

    public float getVolume(boolean actual) {
        if (actual && volumeControl != null) {
            if (linearVolume)
                return this.volumeControl.getValue() / volumeControl.getMaximum();
            else
                return dbToLinear(volumeControl.getValue());
        } else
            return volume;
    }

    private float linearToDb(double volume) {
        return (float) (20 * Math.log10(volume));
    }

    private float dbToLinear(double volume) {
        return (float) Math.pow(10, volume / 20);
    }

    public void setMixer(Mixer.Info info) {
        if (info == null)
            mixer = null;
        else
            mixer = AudioSystem.getMixer(info);
        mixerChanged = true;
    }

    public Mixer.Info getMixer() {
        if (mixer != null)
            return mixer.getMixerInfo();
        else
            return null;
    }

    public boolean isOverrun() {
        return line.available() - line.getBufferSize() == 0;
    }

    public int available() {
        if (line != null)
            return line.available();
        else
            return BUFFER_SIZE;
    }

    public void drain() {
        line.drain();
    }
}
