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

package com.tulskiy.musique.gui;

import com.tulskiy.musique.audio.player.Player;
import com.tulskiy.musique.audio.player.PlayerEvent;
import com.tulskiy.musique.audio.player.PlayerListener;
import com.tulskiy.musique.audio.player.PlayerState;
import com.tulskiy.musique.playlist.Song;
import com.tulskiy.musique.system.Application;
import com.tulskiy.musique.util.GlobalTimer;
import com.tulskiy.musique.util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Formatter;
import java.util.Locale;

/**
 * @Author: Denis Tulskiy
 * @Date: 10.10.2008
 */
public class StatusBar extends JPanel {
    private JLabel info;
    private JLabel time;
    private JLabel gain;

    private Player player = Application.getInstance().getPlayer();

    public StatusBar() {
        info = new JLabel(" ");
        time = new JLabel("0:00");
//        gain = new JLabel(volumeToString(player.getVolume()));
//        gain = new JLabel();
//        gain.setPreferredSize(new Dimension(70, 23));
//        gain.setHorizontalAlignment(SwingConstants.RIGHT);

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(10, 23));
        setBackground(new Color(238, 238, 238));

        Box box = new Box(BoxLayout.X_AXIS);
        box.add(info);
        box.add(Box.createGlue());
        box.add(time);
        box.add(Box.createHorizontalStrut(20));
//        box.add(gain);
//        box.add(Box.createHorizontalStrut(5));

        add(box);

        buildListeners();
    }

    private String volumeToString(float volume) {
        return new Formatter().format(Locale.US, "%2.2f dB", volume).toString();
    }

    private void buildListeners() {
        GlobalTimer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (player.getState() == PlayerState.PLAYING) {
                    Song song = player.getSong();
                    String s = Util.samplesToTime(player.getCurrentSample(), song.getSamplerate(), 0);
                    s += " / " + Util.samplesToTime(song.getTotalSamples(), song.getSamplerate(), 0);
                    time.setText(s);
                }
            }
        });

        player.addListener(new PlayerListener() {
            public void onEvent(PlayerEvent e) {
                switch (e.getEventCode()) {
                    case VOLUME_CHANGED:
                        gain.setText(volumeToString(player.getVolume()));
                        break;
                    case FINISHED_PLAYING:
                        time.setText("0:00");
                }
            }
        });
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int y = 0;
        g.setColor(new Color(156, 154, 140));
        g.drawLine(0, y, getWidth(), y);
        y++;
        g.setColor(new Color(196, 194, 183));
        g.drawLine(0, y, getWidth(), y);
        y++;
        g.setColor(new Color(218, 215, 201));
        g.drawLine(0, y, getWidth(), y);
        y++;
        g.setColor(new Color(233, 231, 217));
        g.drawLine(0, y, getWidth(), y);

        y = getHeight() - 3;
        g.setColor(new Color(233, 232, 218));
        g.drawLine(0, y, getWidth(), y);
        y++;
        g.setColor(new Color(233, 231, 216));
        g.drawLine(0, y, getWidth(), y);
        y = getHeight() - 1;
        g.setColor(new Color(221, 221, 220));
        g.drawLine(0, y, getWidth(), y);
    }
}