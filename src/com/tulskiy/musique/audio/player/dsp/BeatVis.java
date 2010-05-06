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
/*
import ddf.minim.analysis.BeatDetect;
import ddf.minim.analysis.FFT;
import ddf.minim.analysis.FourierTransform;

import javax.swing.*;
import java.awt.*;

*//**
 * @Author: Denis Tulskiy
 * @Date: Jan 3, 2010
 *//*
public class BeatVis extends JPanel implements Processor {
    private BeatDetect beatDetect;
    private boolean hat, kick, snare;
    private JFrame frame;
    FourierTransform fft;

    public BeatVis() {
        beatDetect = new BeatDetect();
        beatDetect.detectMode(BeatDetect.FREQ_ENERGY);
        beatDetect.setSensitivity(100);

        setBackground(Color.white);
    }

    public String getName() {
        return null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.black);
        int spec = fft.specSize();
        int w = getWidth() / spec;
        if (w == 0) w = 1;
        g2d.setColor(Color.green.darker());
        int p = 0;
        for (int i = 0; i < spec; i++) {
            g2d.drawRect(p, getHeight(), w, (int) (getHeight() - fft.getBand(i) * 10));
            p += w;
        }
    }

    public void process(float[] samples, int len) {
        if (frame == null) {
            frame = new JFrame();
            frame.setContentPane(this);
            frame.setSize(300, 300);
            frame.setVisible(true);
        }

        try {
//            if (beatDetect.getTimeSize() != len) {
//                beatDetect = new BeatDetect(len, 44100);
//                beatDetect.setSensitivity(100);
//            }
//            beatDetect.detect(samples, len);
//            hat = kick = snare = false;
//            if (beatDetect.isHat()) {
//                hat = true;
//            } else if (beatDetect.isKick()) {
//                kick = true;
//            } else if (beatDetect.isSnare()) {
//                snare = true;
//            }

            if (fft == null) {
                fft = new FFT(len, 44100);
            }

            fft.forward(samples, len);

            repaint();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}*/
