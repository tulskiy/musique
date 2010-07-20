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

import com.tulskiy.musique.audio.player.PlayerEvent;
import com.tulskiy.musique.audio.player.PlayerListener;
import com.tulskiy.musique.gui.playlist.PlaylistPanel;
import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.playlist.formatting.Parser;
import com.tulskiy.musique.playlist.formatting.tokens.Expression;
import com.tulskiy.musique.system.Application;
import com.tulskiy.musique.system.Configuration;
import com.tulskiy.musique.util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * @Author: Denis Tulskiy
 * @Date: 07.06.2009
 */
public class MainWindow extends JFrame {
    private Application app = Application.getInstance();
    private Configuration config = app.getConfiguration();
    private PlaylistPanel playlistPanel;
    private Tray tray;
    private Expression windowFormat;

    public MainWindow() {
        setIconImage(new ImageIcon("resources/images/icon.png").getImage());
        ControlPanel controlPanel = new ControlPanel();
        StatusBar statusBar = new StatusBar();
        playlistPanel = new PlaylistPanel();
        JSplitPane side = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new LyricsPanel(), new AlbumArtPanel());
        side.setDividerLocation(400);
        side.setDividerSize(5);
        JSplitPane center = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, side, playlistPanel);
        center.setDividerSize(5);
        center.setDividerLocation(300);
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        playlistPanel.addMenu(menuBar);
        add(controlPanel, BorderLayout.NORTH);
        add(statusBar, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        Rectangle r = config.getRectangle("gui.mainWindowPosition", new Rectangle(0, 0, 790, 480));
        setLocation((int) r.getX(), (int) r.getY());
        setSize((int) r.getWidth(), (int) r.getHeight());
        setExtendedState(config.getInt("gui.mainWindowState", 0));

        updateTray();

        updateTitleFormat();
        app.getPlayer().addListener(new PlayerListener() {
            @Override
            public void onEvent(PlayerEvent e) {
                formatTitle();
            }
        });

        config.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String prop = evt.getPropertyName();
                if (prop.startsWith("tray.")) {
                    updateTray();
                } else if (prop.equals("format.window")) {
                    updateTitleFormat();
                }
            }
        });
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (tray == null || !config.getBoolean("tray.minimizeOnClose", true)) {
                    app.exit();
                } else {
                    setVisible(false);
                }
            }
        });
    }

    private void formatTitle() {
        Track track = app.getPlayer().getTrack();
        String value = null;
        if (track != null)
            value = (String) windowFormat.eval(track);

        if (Util.isEmpty(value) || app.getPlayer().isStopped()) {
            setTitle(app.getVersion());
        } else {
            setTitle(value + " [" + app.getVersion() + "]");
        }
    }

    private void updateTitleFormat() {
        windowFormat = Parser.parse(config.getString("format.window", ""));
        formatTitle();
    }

    private void updateTray() {
        if (config.getBoolean("tray.enabled", false)) {
            if (tray == null) {
                tray = new Tray();
            } else {
                tray.uninstall();
            }

            tray.install();
            tray.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        int state = getExtendedState();
                        if ((state & ICONIFIED) != 0) {
                            state &= ~ICONIFIED;
                            setExtendedState(state);
                        } else {
                            setVisible(!isVisible());
                        }
                    } else if (e.getButton() == MouseEvent.BUTTON2) {
                        app.getPlayer().pause();
                    }
                }
            });
        } else if (tray != null) {
            tray.uninstall();
        }
    }

    public void shutdown() {
        if (tray != null)
            tray.uninstall();
        setVisible(false);
        config.setRectangle("gui.mainWindowPosition", new Rectangle(getX(), getY(), getWidth(), getHeight()));
        config.setInt("gui.mainWindowState", getExtendedState());
        playlistPanel.saveSettings();
    }
}



