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
import com.tulskiy.musique.playlist.Playlist;
import com.tulskiy.musique.playlist.PlaylistManager;
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

    public MainWindow() {
        super("Musique");

//        setBackground(config.getColor("gui.backgroundColor"));
        createMenu();
        ControlPanel controlPanel = new ControlPanel();
        StatusBar statusBar = new StatusBar();
        playlistPanel = new PlaylistPanel();
        JPanel panel = new JPanel();
//        panel.setBackground(config.getColor("gui.backgroundColor"));
        panel.setLayout(new BorderLayout());
        panel.setOpaque(true);
        panel.add(playlistPanel);
        add(controlPanel, BorderLayout.NORTH);
        add(statusBar, BorderLayout.SOUTH);
        add(panel, BorderLayout.CENTER);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        Rectangle r = config.getRectangle("gui.mainWindowPosition");
        setLocation((int) r.getX(), (int) r.getY());
        setSize((int) r.getWidth(), (int) r.getHeight());
        setExtendedState(config.getInt("gui.mainWindowState"));
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                app.exit();
            }
        });
    }

    private JMenuItem newItem(String name, String hotkey, ActionListener al) {
        JMenuItem item = new JMenuItem(name);
        item.setAccelerator(KeyStroke.getKeyStroke(hotkey));
        item.addActionListener(al);

        return item;
    }

    private void createMenu() {
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        final PlaylistManager playlistManager = app.getPlaylistManager();

        fileMenu.add("New Playlist").addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String name = JOptionPane.showInputDialog("Enter Playlist Name");
                playlistPanel.addPlaylist(name);
            }
        });

        fileMenu.add("Remove Playlist").addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                playlistPanel.removePlaylist();
            }
        });

        fileMenu.addSeparator();

        fileMenu.add("Add Files").addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setMultiSelectionEnabled(true);
                fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                int retVal = fc.showOpenDialog(null);
                if (retVal == JFileChooser.APPROVE_OPTION) {
                    ProgressMonitor pm = new ProgressMonitor(null, "Adding Files", "", 0, 100);
                    playlistManager.getCurrentPlaylist().addFiles(fc.getSelectedFiles());
                }

                playlistPanel.update();
            }
        });

        fileMenu.addSeparator();

        fileMenu.add(newItem("Quit", "control Q", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                app.exit();
            }
        }));

        JMenu editMenu = new JMenu("Edit");
        menuBar.add(editMenu);

        editMenu.add("Clear").addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                playlistPanel.clearPlaylist();
            }
        });

        editMenu.add(newItem("Remove", "DELETE", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                playlistPanel.removeSelected();   /**/
            }
        }));

    }

    public void shutdown() {
        setVisible(false);
        config.setRectangle("gui.mainWindowPosition", new Rectangle(getX(), getY(), getWidth(), getHeight()));
        config.setInt("gui.mainWindowState", getExtendedState());
        playlistPanel.shutdown();
    }
}
