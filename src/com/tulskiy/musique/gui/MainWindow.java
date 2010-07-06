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

import com.tulskiy.musique.gui.playlist.PlaylistPanel;
import com.tulskiy.musique.system.Application;
import com.tulskiy.musique.system.Configuration;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * @Author: Denis Tulskiy
 * @Date: 07.06.2009
 */
public class MainWindow extends JFrame {
    private Application app = Application.getInstance();
    private Configuration config = app.getConfiguration();
    private PlaylistPanel playlistPanel;
    private Tray tray;

    public MainWindow() {
        super("Musique");
        setIconImage(new ImageIcon("resources/images/icon.png").getImage());

        ControlPanel controlPanel = new ControlPanel();
        StatusBar statusBar = new StatusBar();
        playlistPanel = new PlaylistPanel();
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        playlistPanel.addMenu(menuBar);
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setOpaque(true);
        panel.add(playlistPanel);
        add(controlPanel, BorderLayout.NORTH);
        add(statusBar, BorderLayout.SOUTH);
        add(panel, BorderLayout.CENTER);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        Rectangle r = config.getRectangle("gui.mainWindowPosition", new Rectangle(0, 0, 790, 480));
        setLocation((int) r.getX(), (int) r.getY());
        setSize((int) r.getWidth(), (int) r.getHeight());
        setExtendedState(config.getInt("gui.mainWindowState", 0));

        boolean trayEnabled = config.getBoolean("tray.enabled", false);
        if (trayEnabled && tray == null) {
            tray = new Tray();
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
        }
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (tray == null) {
                    app.exit();
                } else {
                    setVisible(false);
                }
            }
        });
    }

    public void shutdown() {
        setVisible(false);
        config.setRectangle("gui.mainWindowPosition", new Rectangle(getX(), getY(), getWidth(), getHeight()));
        config.setInt("gui.mainWindowState", getExtendedState());
        playlistPanel.saveSettings();
    }
}



