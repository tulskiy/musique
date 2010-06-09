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

package com.tulskiy.musique.gui.playlist;

import com.tulskiy.musique.audio.player.Player;
import com.tulskiy.musique.audio.player.PlayerEvent;
import com.tulskiy.musique.audio.player.PlayerListener;
import com.tulskiy.musique.db.DBMapper;
import com.tulskiy.musique.playlist.Playlist;
import com.tulskiy.musique.playlist.PlaylistManager;
import com.tulskiy.musique.playlist.Song;
import com.tulskiy.musique.system.Application;
import com.tulskiy.musique.system.Configuration;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * @Author: Denis Tulskiy
 * @Date: Feb 6, 2010
 */
public class PlaylistPanel extends JPanel {
    private DBMapper<PlaylistColumn> columnDBMapper = DBMapper.create(PlaylistColumn.class);

    private Application app = Application.getInstance();
    private Configuration config = app.getConfiguration();
    private PlaylistTable table;
    private PlaylistManager playlistManager;
//    private JComboBox playlistSelection;
    private ArrayList<PlaylistColumn> columns;
    private Playlist playlist;
    private JTabbedPane tabbedPane;

    private JTextField searchField;

    private Playlist tabPlaylist;
    private int tabIndex;
    private int dragTo = -1;
    private int dragFrom;

    public PlaylistPanel() {
        playlistManager = app.getPlaylistManager();
        playlist = playlistManager.getCurrentPlaylist();
//        playlistSelection = new JComboBox(new Vector<Playlist>(playlistManager.getPlaylists()));
//        playlistSelection.setSelectedItem(playlist);

//        playlistSelection.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                JComboBox box = (JComboBox) e.getSource();
//                playlist = (Playlist) box.getSelectedItem();
//                searchField.setText("");
//                table.setPlaylist(playlist);
//                playlistManager.selectPlaylist(playlist);
//            }
//        });

        columns = new ArrayList<PlaylistColumn>() {
            @Override
            public boolean add(PlaylistColumn playlistColumn) {
                columnDBMapper.save(playlistColumn);
                return super.add(playlistColumn);
            }

            @Override
            public PlaylistColumn remove(int index) {
                PlaylistColumn pc = get(index);
                columnDBMapper.delete(pc);
                return super.remove(index);
            }
        };
        columnDBMapper.loadAll("select * from playlist_columns order by position", columns);
        table = new PlaylistTable(playlist, columns);
        app.getPlayer().setPlaybackOrder(table);

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        Box box = new Box(BoxLayout.X_AXIS);
        box.add(Box.createHorizontalStrut(5));
        box.add(new JLabel("Playlist "));
//        box.add(playlistSelection);
        box.add(Box.createHorizontalStrut(10));
        box.add(new JLabel("Search: "));
        searchField = new JTextField();
        searchField.setMaximumSize(new Dimension(300, 40));
        searchField.setPreferredSize(new Dimension(300, 0));
        box.add(searchField);
        box.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

//        add(box, BorderLayout.NORTH);

//        add(tableScrollPane, BorderLayout.CENTER);
        tabbedPane = new JTabbedPane() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (dragTo != -1 && dragFrom != -1 && dragTo != dragFrom) {
                    Rectangle b = tabbedPane.getBoundsAt(dragTo);
                    g.setColor(Color.GRAY);
                    int x = (int) (b.getX());
                    if (dragTo > dragFrom)
                        x += b.getWidth();
                    int y = (int) b.getY();
                    g.fillRect(x - 1, y + 2, 3, (int) b.getHeight() - 2);

                    int[] xP = {x - 1, x - 5, x + 5, x + 1};
                    int[] yP = {y + 2, y - 5, y - 5, y + 2};
                    g.fillPolygon(xP, yP, xP.length);
                }
            }
        };
        final ArrayList<Playlist> playlists = playlistManager.getPlaylists();
        for (Playlist pl : playlists) {
            tabbedPane.add(pl.getName(), null);
        }

        if (UIManager.getLookAndFeel().getName().contains("GTK")) {
            tabbedPane.setPreferredSize(new Dimension(10000, 30));
        } else {
            tabbedPane.setPreferredSize(new Dimension(10000, 25));
        }
        tabbedPane.setFocusable(false);
        tabbedPane.setSelectedIndex(playlists.indexOf(playlist));

        add(tabbedPane, BorderLayout.NORTH);
        add(table.getScrollPane(), BorderLayout.CENTER);

        tabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int index = tabbedPane.getSelectedIndex();
                if (index == -1) return;
                playlist = playlistManager.getPlaylist(index);
                table.setPlaylist(playlist);
                playlistManager.selectPlaylist(playlist);
            }
        });

        tabbedPane.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                dragTo = tabbedPane.indexAtLocation(e.getX(), e.getY());
                tabbedPane.repaint();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
            }
        });

        tabbedPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (dragFrom != -1 && dragTo != -1 && dragFrom != dragTo) {
                    playlistManager.movePlaylist(dragFrom, dragTo);

                    for (int i = 0; i < playlists.size(); i++) {
                        Playlist pl = playlists.get(i);
                        tabbedPane.setTitleAt(i, pl.getName());
                    }

                    tabbedPane.setSelectedIndex(dragTo);
                }
                dragTo = -1;
            }

            @Override
            public void mousePressed(MouseEvent e) {
                dragFrom = tabbedPane.indexAtLocation(e.getX(), e.getY());
            }
        });

//        add(cPanel, BorderLayout.CENTER);

        int sortingColumn = config.getInt("playlist.sortingColumn", -1);
        if (sortingColumn != -1 && sortingColumn < columns.size()) {
            ArrayList<RowSorter.SortKey> list = new ArrayList<RowSorter.SortKey>(1);
            list.add(new RowSorter.SortKey(sortingColumn, SortOrder.ASCENDING));
            table.getRowSorter().setSortKeys(list);
        }

        int lastPlayed = config.getInt("player.lastPlayed", -1);
        table.setLastPlayed(new Song(lastPlayed));

        buildListeners();
        createPopupMenu();
    }

    private void createPopupMenu() {

        final JPopupMenu tabMenu = new JPopupMenu();

        tabMenu.add(new JMenuItem("New Playlist")).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = JOptionPane.showInputDialog("Enter Playlist Name", "New Playlist");
                addPlaylist(name);
            }
        });
        tabMenu.add(new JMenuItem("Rename")).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (tabIndex == -1)
                    return;
                String name = JOptionPane.showInputDialog("Rename", tabPlaylist.getName());
                if (name == null || name.isEmpty()) return;
                tabPlaylist.setName(name);
                tabbedPane.setTitleAt(tabIndex, name);
            }
        });
        tabMenu.add(new JMenuItem("Remove Playlist")).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (tabIndex == -1) return;
                tabbedPane.remove(tabIndex);
                playlistManager.removePlaylist(tabPlaylist);
                if (playlistManager.getTotalPlaylists() == 0) {
                    addPlaylist("Default");
                }
            }
        });

        tabbedPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                show(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                show(e);
            }

            public void show(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    tabIndex = tabbedPane.indexAtLocation(e.getX(), e.getY());
                    if (tabIndex != -1)
                        tabPlaylist = playlistManager.getPlaylist(tabIndex);
                    tabMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }


    private void playSelected() {
        app.getPlayer().open(table.getSelectedSong());
        app.getPlayer().play();
    }

    public void buildListeners() {
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                    playSelected();
                }
            }
        });

        table.addKeyboardAction(KeyStroke.getKeyStroke("ENTER"), "playSelected", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                playSelected();
            }
        });

        final Player player = app.getPlayer();

        AbstractAction controlAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String cmd = actionEvent.getActionCommand();
                if (cmd.equals("b")) {
                    player.next();
                }
                if (cmd.equals("v")) {
                    player.play();
                }
                if (cmd.equals("c")) {
                    player.pause();
                }
                if (cmd.equals("x")) {
                    player.stop();
                }
                if (cmd.equals("z")) {
                    player.prev();
                }
            }
        };

        table.addKeyboardAction(KeyStroke.getKeyStroke("B"), "next", controlAction);
        table.addKeyboardAction(KeyStroke.getKeyStroke("V"), "stop", controlAction);
        table.addKeyboardAction(KeyStroke.getKeyStroke("C"), "pause", controlAction);
        table.addKeyboardAction(KeyStroke.getKeyStroke("X"), "play", controlAction);
        table.addKeyboardAction(KeyStroke.getKeyStroke("Z"), "prev", controlAction);

        table.addKeyboardAction(KeyStroke.getKeyStroke("alt ENTER"), "showInfo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                table.showInfo(table.getSelectedSong());
            }
        });

        player.addListener(new PlayerListener() {
            public void onEvent(PlayerEvent e) {
                table.update();
                switch (e.getEventCode()) {
                    case FILE_OPENED:
                        table.scrollToSong(player.getSong());
                }
            }
        });

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                table.filter(searchField.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                table.filter(searchField.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                table.filter(searchField.getText());
            }
        });
    }

    public void shutdown() {
        table.saveColumns();

        for (PlaylistColumn c : columns) {
            columnDBMapper.save(c);
        }

        List<? extends RowSorter.SortKey> keys = table.getRowSorter().getSortKeys();
        if (!keys.isEmpty()) {
            config.setInt("playlist.sortingColumn", keys.get(0).getColumn());
        }
    }

    public void addPlaylist(String name) {
        if (name == null || name.isEmpty()) return;
        playlist = playlistManager.addPlaylist(name);
//        playlistSelection.addItem(playlist);
//        playlistSelection.setSelectedItem(playlist);

        tabbedPane.add(name, null);
        tabbedPane.setSelectedIndex(playlistManager.getTotalPlaylists() - 1);
    }

    private JMenuItem newItem(String name, String hotkey, ActionListener al) {
        JMenuItem item = new JMenuItem(name);
        item.setAccelerator(KeyStroke.getKeyStroke(hotkey));
        item.addActionListener(al);

        return item;
    }

    public void addMenu(JMenuBar menuBar) {
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        JMenu editMenu = new JMenu("Edit");
        menuBar.add(editMenu);
        JMenu viewMenu = new JMenu("View");
        menuBar.add(viewMenu);
        JMenu playbackMenu = new JMenu("Playback");
        menuBar.add(playbackMenu);

        fileMenu.add("New Playlist").addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String name = JOptionPane.showInputDialog("Enter Playlist Name", "New Playlist");
                addPlaylist(name);
            }
        });

        fileMenu.add("Remove Playlist").addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tabbedPane.remove(playlistManager.getPlaylists().indexOf(playlist));
//                playlistSelection.removeItem(playlist);
                playlistManager.removePlaylist(playlist);
                if (playlistManager.getTotalPlaylists() == 0) {
                    addPlaylist("Default");
                }
//                playlistSelection.setSelectedIndex(0);
            }
        });

        fileMenu.addSeparator();

        fileMenu.add("Add Files").addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                String path = config.getString("playlist.lastDir", "");
                if (!path.isEmpty()) fc.setCurrentDirectory(new File(path));
                fc.setMultiSelectionEnabled(true);
                fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                int retVal = fc.showOpenDialog(null);
                if (retVal == JFileChooser.APPROVE_OPTION) {
//                    ProgressMonitor pm = new ProgressMonitor(null, "Adding Files", "", 0, 100);
                    playlistManager.getCurrentPlaylist().addFiles(fc.getSelectedFiles());
                }

                config.setString("playlist.lastDir", fc.getCurrentDirectory().getAbsolutePath());
                table.dataChanged();
                table.update();
            }
        });

        fileMenu.addSeparator();

        fileMenu.add(newItem("Quit", "control Q", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                app.exit();
            }
        }));


        editMenu.add("Clear").addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Playlist pl = playlistManager.getCurrentPlaylist();
                pl.clear();
                table.update();
            }
        });

        editMenu.add(newItem("Remove", "DELETE", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                table.removeSelected();
            }
        }));

        editMenu.addSeparator();
        editMenu.add(new JMenuItem("Clear playback queue")).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                table.clearQueue();
            }
        });


        JMenu laf = new JMenu("Look and Feel");
        ActionListener lafListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String cmd = e.getActionCommand();
                    if (cmd.equals("Metal")) {
                        UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
                    } else if (cmd.equals("Nimbus")) {
                        UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
                    } else {
                        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    }
                    shutdown();
                    app.start();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        };

        laf.add("Metal").addActionListener(lafListener);
        laf.add("Nimbus").addActionListener(lafListener);
        laf.add("Native").addActionListener(lafListener);
        viewMenu.add(laf);

        JMenu orderMenu = new JMenu("Order");
        playbackMenu.add(orderMenu);
        ActionListener orderListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JMenuItem item = (JMenuItem) e.getSource();
                PlaylistTable.Order o = PlaylistTable.Order.valueOf(item.getName());
                table.setOrder(o);
                config.setInt("player.playbackOrder", o.ordinal());
            }
        };

        int index = config.getInt("player.playbackOrder", 0);
        PlaylistTable.Order[] orders = PlaylistTable.Order.values();
        ButtonGroup gr = new ButtonGroup();
        for (PlaylistTable.Order o : orders) {
            JCheckBoxMenuItem item = new JCheckBoxMenuItem(o.getText());
            if (o.ordinal() == index) {
                item.setSelected(true);
                table.setOrder(o);
            }
            item.addActionListener(orderListener);
            item.setName(o.toString());
            gr.add(item);
            orderMenu.add(item);
        }

        JMenu controlMenu = new JMenu("Control");
        playbackMenu.add(controlMenu);

        final Player player = app.getPlayer();
        ActionListener controlListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String cmd = e.getActionCommand();
                if (cmd.equals("Next")) {
                    player.next();
                }
                if (cmd.equals("Play")) {
                    player.play();
                }
                if (cmd.equals("Pause")) {
                    player.pause();
                }
                if (cmd.equals("Stop")) {
                    player.stop();
                }
                if (cmd.equals("Previous")) {
                    player.prev();
                }
            }
        };
        controlMenu.add(newItem("Next", "", controlListener));
        controlMenu.add(newItem("Play", "", controlListener));
        controlMenu.add(newItem("Pause", "", controlListener));
        controlMenu.add(newItem("Stop", "", controlListener));
        controlMenu.add(newItem("Previous", "", controlListener));

        playbackMenu.addSeparator();

        playbackMenu.add(newItem("Show Now Playing", "SPACE", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                table.scrollToSong(player.getSong());
            }
        }));
    }
}
