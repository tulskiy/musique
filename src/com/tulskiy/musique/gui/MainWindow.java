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
import com.tulskiy.musique.gui.dialogs.TextDialog;
import com.tulskiy.musique.gui.library.LibraryView;
import com.tulskiy.musique.gui.playlist.PlaylistPanel;
import com.tulskiy.musique.images.Images;
import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.playlist.formatting.Parser;
import com.tulskiy.musique.playlist.formatting.tokens.Expression;
import com.tulskiy.musique.system.Application;
import com.tulskiy.musique.system.Configuration;
import com.tulskiy.musique.util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

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
    private final String defaultWindowFormat = "%title% - [%artist%][ - '['[%album% ][CD%discNumber% ][#%trackNumber%]']' ]";
    private final JSplitPane side;
    private final JSplitPane center;

    public MainWindow() {
        setIconImage(Images.loadImage("icon.png"));
        ControlPanel controlPanel = new ControlPanel();
        StatusBar statusBar = new StatusBar();
        playlistPanel = new PlaylistPanel();
        LyricsPanel lyricsPanel = new LyricsPanel();
        LibraryView libraryView = new LibraryView();
        JTabbedPane topLeftSide = new JTabbedPane();
        topLeftSide.add("Library", libraryView);
        topLeftSide.add("Lyrics", lyricsPanel);
        topLeftSide.setFocusable(false);
        side = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topLeftSide, new AlbumArtPanel());
        side.setDividerSize(6);
        center = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, side, playlistPanel);

        int sideBarWidth = config.getInt("sidebar.width", 300);
        center.setDividerLocation(sideBarWidth);
        int sideBarSeparator = config.getInt("sidebar.divider", 400);
        side.setDividerLocation(sideBarSeparator);
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        add(controlPanel, BorderLayout.NORTH);
        add(statusBar, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);
        playlistPanel.addMenu(menuBar);
        libraryView.addMenu(menuBar);
        addHelpMenu(menuBar);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        Rectangle r = config.getRectangle("gui.mainWindowPosition", new Rectangle(50, 0, 1000, 730));
        setLocation((int) r.getX(), (int) r.getY());
        setSize((int) r.getWidth(), (int) r.getHeight());
        setExtendedState(config.getInt("gui.mainWindowState", 0));

        final Runnable formatTitle = new Runnable() {
            @Override
            public void run() {
                formatTitle();
            }
        };
        app.getPlayer().addListener(new PlayerListener() {
            @Override
            public void onEvent(PlayerEvent e) {
                SwingUtilities.invokeLater(formatTitle);
            }
        });
        config.addPropertyChangeListener("sidebar.enabled", true, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                boolean enabled = config.getBoolean(evt.getPropertyName(), true);
                side.setVisible(enabled);
                center.setDividerSize(enabled ? 6 : 0);
            }
        });

        updateTray();
        config.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String prop = evt.getPropertyName();
                if (prop.startsWith("tray.")) {
                    updateTray();
                }
            }
        });

        config.addPropertyChangeListener("format.window", true, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                windowFormat = Parser.parse(config.getString("format.window", defaultWindowFormat));
                formatTitle();
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

    private void addHelpMenu(JMenuBar menuBar) {
        Icon emptyIcon = Images.getEmptyIcon();

        JMenu helpMenu = new JMenu("Help");
        menuBar.add(helpMenu);

        final JFrame comp = this;
        JMenuItem helpMenuItem = helpMenu.add("Help     ");
        helpMenuItem.setIcon(emptyIcon);
        helpMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new TextDialog(comp, "Readme", new File("README")).setVisible(true);
            }
        });

        helpMenu.add("License").addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new TextDialog(comp, "License", new File("Copying")).setVisible(true);
            }
        });

        helpMenu.add("About").addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new TextDialog(comp, "About", new File("ABOUT")).setVisible(true);
            }
        });
    }

    private void formatTitle() {
        Track track = app.getPlayer().getTrack();
        String value = null;
        if (track != null)
            value = (String) windowFormat.eval(track);

        String title;
        if (Util.isEmpty(value) || app.getPlayer().isStopped()) {
            title = app.VERSION;
        } else {
            title = value + " [" + app.VERSION + "]";
        }
        setTitle(title);

        // deadlocks the jvm :( pity...
//        if (tray != null)
//            tray.setToolTip(title);
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
        config.setInt("sidebar.width", center.getDividerLocation());
        config.setInt("sidebar.divider", side.getDividerLocation());
        playlistPanel.saveSettings();
    }
}



