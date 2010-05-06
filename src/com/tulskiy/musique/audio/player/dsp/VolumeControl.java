/*
 * Copyright (c) 2008, 2009, 2010 Denis Tulskiy
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

package com.tulskiy.musique.audio.player.dsp;

import com.tulskiy.musique.audio.player.PlayerListener;

import java.util.ArrayList;

/**
 * @Author: Denis Tulskiy
 * @Date: 26.07.2009
 */
public class VolumeControl implements Processor {
    private static ArrayList<PlayerListener> listeners = new ArrayList<PlayerListener>();
    private double value;

    public String getName() {
        return "Fader";
    }

    public void process(float[] samples, int len) {
        for (int i = 0; i < len; i++) {
            samples[i] *= value;
        }
    }

    public void addActionListener(PlayerListener listener) {
        listeners.add(listener);
    }

    public void setVolume(double value) {
        this.value = value;
    }
}
