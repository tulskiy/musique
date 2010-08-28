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
import com.tulskiy.musique.playlist.Track;
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
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Author: Denis Tulskiy
 * Date: Jul 19, 2010
 */
public class AlbumArtPanel extends JPanel {
    private static Logger logger = Logger.getLogger("musique");

    private Application app = Application.getInstance();
    private Configuration config = app.getConfiguration();
    private final ArrayList<String> stubDefaults = new ArrayList<String>(Arrays.asList(
            "front.jpg",
            "cover.jpg",
            "%fileName%.jpg",
            "%album%.jpg",
            "folder.jpg"));
    private ImageIcon image;
    private Track track;
    private LinkedHashMap<File, ImageIcon> cache = new LinkedHashMap<File, ImageIcon>(10, 0.7f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<File, ImageIcon> eldest) {
            return size() > 10;
        }
    };

    private ArrayList<Expression> stubs = new ArrayList<Expression>();
    private Timer timer;
    private boolean nowPlayingOnly;

    public AlbumArtPanel() {
        setLayout(new BorderLayout());
        final JLabel canvas = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                if (image == null) {
                    setText("[no image]");
                    super.paintComponent(g);
                } else {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

                    double scaleW = (double) getWidth() / image.getIconWidth();
                    double scaleH = (double) getHeight() / image.getIconHeight();

                    double scale = Math.min(scaleW, scaleH);
                    int height = (int) (image.getIconHeight() * scale);
                    int width = (int) (image.getIconWidth() * scale);

                    g2d.drawImage(image.getImage(), (getWidth() - width) / 2, (getHeight() - height) / 2, width, height, getBackground(), image.getImageObserver());
                }
            }
        };

        canvas.setHorizontalAlignment(JLabel.CENTER);
        canvas.setVerticalAlignment(JLabel.CENTER);

        config.addPropertyChangeListener("albumart.nowPlayingOnly", true, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                nowPlayingOnly = config.getBoolean(evt.getPropertyName(), false);
            }
        });

        config.addPropertyChangeListener("playlist.selectedTrack", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (!nowPlayingOnly && evt.getNewValue() instanceof Track) {
                    track = (Track) evt.getNewValue();
                    timer.restart();
                }
            }
        });
        final Player player = app.getPlayer();
        player.addListener(new PlayerListener() {
            @Override
            public void onEvent(PlayerEvent e) {
                if (nowPlayingOnly && e.getEventCode() == PlayerEvent.PlayerEventCode.FILE_OPENED) {
                    track = player.getTrack();
                    timer.restart();
                }
            }
        });

        config.addPropertyChangeListener("albumart.stubs", true, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                stubs.clear();
                ArrayList<String> list = config.getList("albumart.stubs", stubDefaults);
                for (String s : list) {
                    stubs.add(Parser.parse(s));
                }
            }
        });
        add(canvas, BorderLayout.CENTER);
        timer = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                image = null;
                if (track != null && track.isFile()) {
                    for (Expression stub : stubs) {
                        try {
                            String path = stub.eval(track).toString();
                            File file = new File(path);
                            if (!file.isAbsolute()) {
                                String parentFile = track.getFile().getParentFile().getAbsolutePath();
                                file = new File(parentFile, path);
                            }
                            if (!file.exists())
                                continue;
                            image = cache.get(file);
                            if (image == null) {
                                logger.fine("Loading Album Art from file: " + file);
                                image = new ImageIcon(file.getAbsolutePath());
                                cache.put(file, image);
                            } else {
                                logger.fine("Loading Album Art from cache for file: " + file);
                            }
                            break;
                        } catch (Exception ignored) {
                        }
                    }
                }
                canvas.repaint();
                timer.stop();
            }
        });
    }

}
