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

package com.tulskiy.musique.gui;

import com.tulskiy.musique.audio.player.Player;
import com.tulskiy.musique.audio.player.PlayerEvent;
import com.tulskiy.musique.audio.player.PlayerListener;
import com.tulskiy.musique.playlist.formatting.Parser;
import com.tulskiy.musique.playlist.formatting.tokens.Expression;
import com.tulskiy.musique.system.Application;
import com.tulskiy.musique.system.Configuration;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Author: Denis Tulskiy
 * Date: 10.10.2008
 */
public class StatusBar extends JPanel {
    private JLabel info;

    private Application app = Application.getInstance();
    private Player player = app.getPlayer();
    private Configuration config = app.getConfiguration();
    private Expression statusFormat;
    private final String defaultFormat = "%codec% | %bitrate% kbps | %sampleRate% Hz | %channelsAsString% | $playingTime()[ / %length%]";

    public StatusBar() {
        info = new JLabel("Stopped");

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(10, 23));
        setBackground(new Color(238, 238, 238));
        setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, Color.lightGray));

        Box box = new Box(BoxLayout.X_AXIS);
        box.add(info);
        box.add(Box.createGlue());
        box.add(Box.createHorizontalStrut(20));
        add(box);

        buildListeners();
    }

    private void buildListeners() {
        final Timer timer = new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (player.isPlaying()) {
                    info.setText((String) statusFormat.eval(player.getTrack()));
                }
            }
        });
        timer.start();

        player.addListener(new PlayerListener() {
            public void onEvent(PlayerEvent e) {
                switch (e.getEventCode()) {
                    case PLAYING_STARTED:
                        timer.start();
                        break;
                    case STOPPED:
                        info.setText("Stopped");
                    case PAUSED:
                        timer.stop();
                }
            }
        });

        config.addPropertyChangeListener("format.statusBar", true, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                statusFormat = Parser.parse(config.getString(evt.getPropertyName(), defaultFormat));
            }
        });
    }
}