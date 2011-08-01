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

package com.tulskiy.musique.gui.dialogs;

import com.tulskiy.musique.audio.AudioFileReader;
import com.tulskiy.musique.audio.player.io.AudioOutput;
import com.tulskiy.musique.gui.library.LibraryAction;
import com.tulskiy.musique.system.Application;
import com.tulskiy.musique.system.Configuration;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import javax.swing.*;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Author: Denis Tulskiy
 * Date: 12/23/10
 */

public class OptionsDialog extends JDialog {
    private Application app = Application.getInstance();
    private Configuration config = app.getConfiguration();
    private JComponent owner;

    public OptionsDialog(JComponent owner) {
        this(owner, null);
    }

    public OptionsDialog(JComponent owner, String selectedTab) {
        this.owner = owner;
        initComponents();
        initDynamicComponents();
        setLocationRelativeTo(null);

        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            if (tabbedPane.getTitleAt(i).equals(selectedTab)) {
                tabbedPane.setSelectedIndex(i);
            }
        }
    }

    private void initDynamicComponents() {
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyButton.doClick();
                cancelButton.doClick();
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                dispose();
            }
        });
        initSystemSettings();
        initNetworkSettings();
        initLibrarySettings();
        initGUISettings();
        initColorsAndFontsSettings();
    }

    private void initSystemSettings() {
        final AudioOutput output = app.getPlayer().getAudioOutput();

        Vector<String> mixerVector = new Vector<String>();
        mixerVector.add("Detect automatically");
        final Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
        int selectedIndex = Arrays.asList(mixerInfo).indexOf(output.getMixer());
        for (Mixer.Info info : mixerInfo) {
            String s = info.getDescription() + ", " + info.getName();
            mixerVector.add(s);
        }

        audioMixer.setModel(new DefaultComboBoxModel(mixerVector));
        audioMixer.setSelectedIndex(selectedIndex + 1);
        audioMixer.setPrototypeDisplayValue(mixerVector.get(0));

        Charset charset = AudioFileReader.getDefaultCharset();
        defaultEncoding.setModel(new DefaultComboBoxModel(Charset.availableCharsets().values().toArray()));
        defaultEncoding.setSelectedItem(charset);

        singleInstance.setSelected(config.getBoolean("system.oneInstance", false));

        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = audioMixer.getSelectedIndex();
                if (index > 0) {
                    Mixer.Info info = mixerInfo[index - 1];
                    output.setMixer(info);
                    config.setString("player.mixer", info.getName());
                } else {
                    output.setMixer(null);
                    config.remove("player.mixer");
                }
                Charset defaultCharset = (Charset) defaultEncoding.getSelectedItem();
                AudioFileReader.setDefaultCharset(defaultCharset);
                config.setString("tag.defaultEncoding", defaultCharset.name());
                config.setBoolean("system.oneInstance", singleInstance.isSelected());
            }
        });
    }

    private void initNetworkSettings() {
        enableHttpProxy.setSelected(config.getBoolean("proxy.enabled", false));
        httpProxyHost.setText(config.getString("proxy.host", null));
        httpProxyPort.setText(config.getString("proxy.port", null));
        httpProxyUsername.setText(config.getString("proxy.user", null));
        httpProxyPassword.setText(config.getString("proxy.password", null));

        enableLastFmScrobbling.setSelected(config.getBoolean("lastfm.enabled", false));
        lastfmUsername.setText(config.getString("lastfm.user", null));
        lastfmPassword.setText(config.getString("lastfm.password", null));

        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                config.setBoolean("lastfm.enabled", enableLastFmScrobbling.isSelected());
                config.setString("lastfm.user", lastfmUsername.getText());
                config.setString("lastfm.password", String.valueOf(lastfmPassword.getPassword()));

                config.setBoolean("proxy.enabled", enableHttpProxy.isSelected());
                config.setString("proxy.host", httpProxyHost.getText());
                config.setString("proxy.port", httpProxyPort.getText());
                config.setString("proxy.user", httpProxyUsername.getText());
                config.setString("proxy.password", String.valueOf(httpProxyPassword.getPassword()));

                if (enableHttpProxy.isSelected()) {
                    System.setProperty("http.proxyHost", httpProxyHost.getText());
                    System.setProperty("http.proxyPort", httpProxyPort.getText());
                } else {
                    System.setProperty("http.proxyHost", "");
                    System.setProperty("http.proxyPort", "");
                }
            }
        });
    }

    private void initLibrarySettings() {
        final ArrayList<String> musicFoldersModel = config.getList("library.folders", new ArrayList<String>());
        musicFolders.setModel(new AbstractListModel() {
            public int getSize() {
                return musicFoldersModel.size();
            }

            public Object getElementAt(int i) {
                return musicFoldersModel.get(i);
            }
        });

        addMusicFolder.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TreeFileChooser fileChooser = new TreeFileChooser(getRootPane(), "Select folder", false);
                File[] files = fileChooser.showOpenDialog();
                for (File file : files) {
                    String path = file.getAbsolutePath();
                    if (!musicFoldersModel.contains(path))
                        musicFoldersModel.add(path);
                }
                musicFolders.setListData(musicFoldersModel.toArray());
                musicFolders.repaint();
            }
        });

        removeMusicFolder.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object[] values = musicFolders.getSelectedValues();
                if (values != null)
                    musicFoldersModel.removeAll(Arrays.asList(values));
                musicFolders.setListData(musicFoldersModel.toArray());
                musicFolders.repaint();
            }
        });

        enableLibraryView.setSelected(config.getBoolean("library.libraryView", false));

        libraryDoubleClickAction.setModel(new DefaultComboBoxModel(new LibraryAction[]{
                LibraryAction.SEND_TO_CURRENT,
                LibraryAction.ADD_TO_CURRENT,
                LibraryAction.SEND_TO_NEW,
                LibraryAction.EXPAND_COLLAPSE
        }));

        libraryMiddleClickAction.setModel(new DefaultComboBoxModel(new LibraryAction[]{
                LibraryAction.SEND_TO_NEW,
                LibraryAction.SEND_TO_CURRENT,
                LibraryAction.ADD_TO_CURRENT,
        }));
        LibraryAction doubleAction = config.getEnum("library.doubleClickAction", LibraryAction.SEND_TO_CURRENT);
        libraryDoubleClickAction.setSelectedItem(doubleAction);

        LibraryAction middleAction = config.getEnum("library.middleClickAction", LibraryAction.SEND_TO_NEW);
        libraryMiddleClickAction.setSelectedItem(middleAction);

        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                config.setList("library.folders", musicFoldersModel);
                config.setBoolean("library.libraryView", enableLibraryView.isSelected());
                config.setEnum("library.doubleClickAction", (Enum) libraryDoubleClickAction.getSelectedItem());
                config.setEnum("library.middleClickAction", (Enum) libraryMiddleClickAction.getSelectedItem());
            }
        });
    }

    private void initGUISettings() {
        final UIManager.LookAndFeelInfo[] lafs = UIManager.getInstalledLookAndFeels();
        Vector<String> lafsVector = new Vector<String>();
        for (UIManager.LookAndFeelInfo laf : lafs) {
            lafsVector.add(laf.getName());
        }
        lookAndFeel.setModel(new DefaultComboBoxModel(lafsVector));
        String name = UIManager.getLookAndFeel().getName();
        if (name.contains("GTK"))
            name = "GTK+";
        lookAndFeel.setSelectedItem(name);

        enableTray.setSelected(config.getBoolean("tray.enabled", false));
        minimizeOnClose.setSelected(config.getBoolean("tray.minimizeOnClose", true));
        shuffleAlbumsPattern.setText(config.getString("playbackOrder.albumFormat", "%album%"));
        windowTitleFormat.setText(config.getString("format.window", ""));
        statusBarFormat.setText(config.getString("format.statusBar", ""));
        showSideBar.setSelected(config.getBoolean("sidebar.enabled", true));
        searchLyrics.setSelected(config.getBoolean("lyrics.searchOnline", true));
        boolean nowPlaying = config.getBoolean("albumart.nowPlayingOnly", false);
        albumArtPlaying.setSelected(nowPlaying);
        albumArtSelected.setSelected(!nowPlaying);
        ArrayList<String> stubList = config.getList("albumart.stubs", null);
        StringBuilder sb = new StringBuilder();
        for (String s : stubList) {
            sb.append(s).append("\n");
        }
        albumArtStubs.setText(sb.toString());

        final JDialog comp = this;

        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = lookAndFeel.getSelectedIndex();
                if (index != -1) {
                    try {
                        String laf = lafs[index].getClassName();
                        UIManager.setLookAndFeel(laf);
                        SwingUtilities.updateComponentTreeUI(SwingUtilities.getRoot(owner));
                        SwingUtilities.updateComponentTreeUI(comp);
                        config.setString("gui.LAF", laf);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }

                config.setBoolean("tray.enabled", enableTray.isSelected());
                config.setBoolean("tray.minimizeOnClose", minimizeOnClose.isSelected());
                config.setString("format.window", windowTitleFormat.getText());
                config.setString("format.statusBar", statusBarFormat.getText());
                config.setBoolean("sidebar.enabled", showSideBar.isSelected());
                config.setBoolean("lyrics.searchOnline", searchLyrics.isSelected());
                config.setBoolean("albumart.nowPlayingOnly", albumArtPlaying.isSelected());
                config.setString("playbackOrder.albumFormat", shuffleAlbumsPattern.getText());
                java.util.List<String> stubList = Arrays.asList(albumArtStubs.getText().split("\n"));
                config.setList("albumart.stubs", new ArrayList<String>(stubList));
            }
        });
    }

    private void initColorsAndFontsSettings() {
        textColor.setColor(config.getColor("gui.color.text", null));

        backgroundColor.setColor(config.getColor("gui.color.background", null));
        selectionColor.setColor(config.getColor("gui.color.selection", null));
        highlightColor.setColor(config.getColor("gui.color.highlight", null));
        trayBgColor1.setColor(config.getColor("tray.bgColor1", null));
        trayBgColor2.setColor(config.getColor("tray.bgColor2", null));
        defaultFont.setSelectedFont(config.getFont("gui.font.default", null));

        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                config.setColor("gui.color.text", textColor.getColor());
                config.setColor("gui.color.background", backgroundColor.getColor());
                config.setColor("gui.color.selection", selectionColor.getColor());
                config.setColor("gui.color.highlight", highlightColor.getColor());
                config.setFont("gui.font.default", defaultFont.getSelectedFont());
                config.setColor("tray.bgColor1", trayBgColor1.getColor());
                config.setColor("tray.bgColor2", trayBgColor2.getColor());
                SwingUtilities.updateComponentTreeUI(SwingUtilities.getRoot(owner));
            }
        });
    }

    @SuppressWarnings({"unchecked", "RedundantArrayCreation"})
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.ButtonGroup buttonGroup1 = new javax.swing.ButtonGroup();
        tabbedPane = new javax.swing.JTabbedPane();
        javax.swing.JPanel systemPanel = new javax.swing.JPanel();
        javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
        audioMixer = new javax.swing.JComboBox();
        javax.swing.JLabel jLabel2 = new javax.swing.JLabel();
        defaultEncoding = new javax.swing.JComboBox();
        singleInstance = new javax.swing.JCheckBox();
        javax.swing.JPanel jPanel1 = new javax.swing.JPanel();
        javax.swing.JLabel jLabel3 = new javax.swing.JLabel();
        javax.swing.JLabel jLabel4 = new javax.swing.JLabel();
        javax.swing.JLabel jLabel5 = new javax.swing.JLabel();
        javax.swing.JLabel jLabel6 = new javax.swing.JLabel();
        httpProxyHost = new javax.swing.JTextField();
        httpProxyPort = new javax.swing.JTextField();
        httpProxyUsername = new javax.swing.JTextField();
        httpProxyPassword = new javax.swing.JPasswordField();
        lastfmUsername = new javax.swing.JTextField();
        javax.swing.JLabel jLabel7 = new javax.swing.JLabel();
        javax.swing.JLabel jLabel8 = new javax.swing.JLabel();
        lastfmPassword = new javax.swing.JPasswordField();
        com.tulskiy.musique.gui.components.SeparatorLabel separatorLabel5 = new com.tulskiy.musique.gui.components.SeparatorLabel();
        com.tulskiy.musique.gui.components.SeparatorLabel separatorLabel6 = new com.tulskiy.musique.gui.components.SeparatorLabel();
        enableHttpProxy = new javax.swing.JCheckBox();
        enableLastFmScrobbling = new javax.swing.JCheckBox();
        javax.swing.JPanel jPanel4 = new javax.swing.JPanel();
        com.tulskiy.musique.gui.components.SeparatorLabel separatorLabel7 = new com.tulskiy.musique.gui.components.SeparatorLabel();
        addMusicFolder = new javax.swing.JButton();
        removeMusicFolder = new javax.swing.JButton();
        enableLibraryView = new javax.swing.JCheckBox();
        com.tulskiy.musique.gui.components.SeparatorLabel separatorLabel9 = new com.tulskiy.musique.gui.components.SeparatorLabel();
        javax.swing.JScrollPane jScrollPane3 = new javax.swing.JScrollPane();
        musicFolders = new javax.swing.JList();
        javax.swing.JLabel jLabel20 = new javax.swing.JLabel();
        libraryDoubleClickAction = new javax.swing.JComboBox();
        javax.swing.JLabel jLabel21 = new javax.swing.JLabel();
        libraryMiddleClickAction = new javax.swing.JComboBox();
        com.tulskiy.musique.gui.components.SeparatorLabel separatorLabel10 = new com.tulskiy.musique.gui.components.SeparatorLabel();
        javax.swing.JScrollPane jScrollPane4 = new javax.swing.JScrollPane();
        libraryViewsTable = new javax.swing.JTable();
        removeView = new javax.swing.JButton();
        addView = new javax.swing.JButton();
        javax.swing.JPanel jPanel5 = new javax.swing.JPanel();
        javax.swing.JLabel jLabel9 = new javax.swing.JLabel();
        lookAndFeel = new javax.swing.JComboBox();
        enableTray = new javax.swing.JCheckBox();
        minimizeOnClose = new javax.swing.JCheckBox();
        showSideBar = new javax.swing.JCheckBox();
        searchLyrics = new javax.swing.JCheckBox();
        javax.swing.JLabel jLabel10 = new javax.swing.JLabel();
        statusBarFormat = new javax.swing.JTextField();
        windowTitleFormat = new javax.swing.JTextField();
        javax.swing.JLabel jLabel11 = new javax.swing.JLabel();
        albumArtSelected = new javax.swing.JRadioButton();
        javax.swing.JLabel jLabel12 = new javax.swing.JLabel();
        javax.swing.JLabel jLabel13 = new javax.swing.JLabel();
        albumArtPlaying = new javax.swing.JRadioButton();
        javax.swing.JScrollPane jScrollPane2 = new javax.swing.JScrollPane();
        albumArtStubs = new javax.swing.JTextArea();
        com.tulskiy.musique.gui.components.SeparatorLabel separatorLabel4 = new com.tulskiy.musique.gui.components.SeparatorLabel();
        com.tulskiy.musique.gui.components.SeparatorLabel separatorLabel3 = new com.tulskiy.musique.gui.components.SeparatorLabel();
        javax.swing.JLabel jLabel23 = new javax.swing.JLabel();
        shuffleAlbumsPattern = new javax.swing.JTextField();
        javax.swing.JPanel jPanel8 = new javax.swing.JPanel();
        textColor = new com.tulskiy.musique.gui.components.ColorChooser();
        backgroundColor = new com.tulskiy.musique.gui.components.ColorChooser();
        selectionColor = new com.tulskiy.musique.gui.components.ColorChooser();
        highlightColor = new com.tulskiy.musique.gui.components.ColorChooser();
        trayBgColor1 = new com.tulskiy.musique.gui.components.ColorChooser();
        trayBgColor2 = new com.tulskiy.musique.gui.components.ColorChooser();
        javax.swing.JLabel jLabel14 = new javax.swing.JLabel();
        javax.swing.JLabel jLabel15 = new javax.swing.JLabel();
        javax.swing.JLabel jLabel16 = new javax.swing.JLabel();
        javax.swing.JLabel jLabel17 = new javax.swing.JLabel();
        javax.swing.JLabel jLabel18 = new javax.swing.JLabel();
        javax.swing.JLabel jLabel19 = new javax.swing.JLabel();
        javax.swing.JLabel jLabel22 = new javax.swing.JLabel();
        defaultFont = new com.tulskiy.musique.gui.components.FontChooser();
        com.tulskiy.musique.gui.components.SeparatorLabel separatorLabel1 = new com.tulskiy.musique.gui.components.SeparatorLabel();
        com.tulskiy.musique.gui.components.SeparatorLabel separatorLabel2 = new com.tulskiy.musique.gui.components.SeparatorLabel();
        applyButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Settings");
        setAlwaysOnTop(true);

        tabbedPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        tabbedPane.setTabPlacement(javax.swing.JTabbedPane.LEFT);
        tabbedPane.setFocusable(false);

        jLabel1.setText("Audio Mixer");

        jLabel2.setText("Default Encoding for tags");

        singleInstance.setText("Allow only one instance (requires restart)");
        singleInstance.setFocusPainted(false);
        singleInstance.setMargin(new java.awt.Insets(2, -1, 2, 2));

        javax.swing.GroupLayout systemPanelLayout = new javax.swing.GroupLayout(systemPanel);
        systemPanel.setLayout(systemPanelLayout);
        systemPanelLayout.setHorizontalGroup(
                systemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(systemPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(systemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(systemPanelLayout.createSequentialGroup()
                                                .addComponent(jLabel1)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(audioMixer, 0, 480, Short.MAX_VALUE))
                                        .addGroup(systemPanelLayout.createSequentialGroup()
                                                .addComponent(jLabel2)
                                                .addGap(18, 18, 18)
                                                .addComponent(defaultEncoding, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(singleInstance))
                                .addContainerGap())
        );
        systemPanelLayout.setVerticalGroup(
                systemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(systemPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(systemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel1)
                                        .addComponent(audioMixer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(systemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel2)
                                        .addComponent(defaultEncoding, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(singleInstance)
                                .addContainerGap(360, Short.MAX_VALUE))
        );

        tabbedPane.addTab("System", systemPanel);

        jLabel3.setText("Host");

        jLabel4.setText("Port");

        jLabel5.setText("Username");

        jLabel6.setText("Password");

        jLabel7.setText("Username");

        jLabel8.setText("Password");

        separatorLabel5.setText("HTTP Proxy");

        separatorLabel6.setText("Last.fm Scrobbling");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(enableHttpProxy)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(separatorLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, 519, Short.MAX_VALUE))
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(jLabel3)
                                                        .addComponent(jLabel5)
                                                        .addComponent(jLabel6)
                                                        .addComponent(jLabel7)
                                                        .addComponent(jLabel8)
                                                        .addComponent(jLabel4))
                                                .addGap(43, 43, 43)
                                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                        .addComponent(httpProxyHost, javax.swing.GroupLayout.PREFERRED_SIZE, 289, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(httpProxyPort, javax.swing.GroupLayout.PREFERRED_SIZE, 289, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(httpProxyUsername, javax.swing.GroupLayout.PREFERRED_SIZE, 289, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(httpProxyPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 289, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(lastfmPassword, javax.swing.GroupLayout.DEFAULT_SIZE, 289, Short.MAX_VALUE)
                                                        .addComponent(lastfmUsername, javax.swing.GroupLayout.PREFERRED_SIZE, 289, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(enableLastFmScrobbling)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(separatorLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, 519, Short.MAX_VALUE)))
                                .addContainerGap())
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[]{httpProxyHost, httpProxyPassword, httpProxyPort, httpProxyUsername, lastfmPassword, lastfmUsername});

        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(16, 16, 16)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                        .addComponent(separatorLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(enableHttpProxy))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel3)
                                        .addComponent(httpProxyHost, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(httpProxyPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel4))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel5)
                                        .addComponent(httpProxyUsername, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel6)
                                        .addComponent(httpProxyPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(11, 11, 11)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                        .addComponent(enableLastFmScrobbling)
                                        .addComponent(separatorLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel7)
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(lastfmUsername, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(lastfmPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(jLabel8))))
                                .addContainerGap(227, Short.MAX_VALUE))
        );

        tabbedPane.addTab("Network", jPanel1);

        separatorLabel7.setText("Music Folders");

        addMusicFolder.setText("Add");

        removeMusicFolder.setText("Remove");

        enableLibraryView.setText("Enable Library View playlist");

        separatorLabel9.setText("Actions");

        musicFolders.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane3.setViewportView(musicFolders);

        jLabel20.setText("Double-Click Action");

        jLabel21.setText("Middle-Click Action");

        separatorLabel10.setText("Views");

        libraryViewsTable.setFont(libraryViewsTable.getFont().deriveFont((float) 10));
        libraryViewsTable.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{

                },
                new String[]{
                        "Title", "Format"
                }
        ) {
            Class[] types = new Class[]{
                    java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }
        });
        libraryViewsTable.setDragEnabled(true);
        libraryViewsTable.getTableHeader().setReorderingAllowed(false);
        libraryViewsTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        libraryViewsTable.getColumnModel().getColumn(0).setMaxWidth(100);
        jScrollPane4.setViewportView(libraryViewsTable);

        removeView.setText("Remove");

        addView.setText("Add");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
                jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 540, Short.MAX_VALUE)
                                        .addGroup(jPanel4Layout.createSequentialGroup()
                                                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 458, Short.MAX_VALUE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                        .addComponent(removeMusicFolder, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(addMusicFolder, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                .addGap(5, 5, 5))
                                        .addComponent(separatorLabel7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 540, Short.MAX_VALUE)
                                        .addComponent(separatorLabel9, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 540, Short.MAX_VALUE)
                                        .addComponent(enableLibraryView, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 198, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel4Layout.createSequentialGroup()
                                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(jLabel20)
                                                        .addComponent(jLabel21))
                                                .addGap(62, 62, 62)
                                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                        .addComponent(libraryMiddleClickAction, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(libraryDoubleClickAction, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addComponent(separatorLabel10, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 540, Short.MAX_VALUE)
                                        .addGroup(jPanel4Layout.createSequentialGroup()
                                                .addComponent(addView)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(removeView)))
                                .addContainerGap())
        );

        jPanel4Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[]{addView, removeView});

        jPanel4Layout.setVerticalGroup(
                jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel4Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(separatorLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel4Layout.createSequentialGroup()
                                                .addComponent(addMusicFolder)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(removeMusicFolder))
                                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(separatorLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(enableLibraryView)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel20)
                                        .addComponent(libraryDoubleClickAction, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel21)
                                        .addComponent(libraryMiddleClickAction, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(separatorLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(removeView)
                                        .addComponent(addView))
                                .addContainerGap(43, Short.MAX_VALUE))
        );

        tabbedPane.addTab("Library", jPanel4);

        jPanel5.setPreferredSize(new java.awt.Dimension(488, 400));

        jLabel9.setText("Look And Feel");

        enableTray.setText("Enable Notification Area Icon");
        enableTray.setFocusPainted(false);

        minimizeOnClose.setText("Minimize to tray on close");
        minimizeOnClose.setFocusPainted(false);

        showSideBar.setText("Show Side Bar");
        showSideBar.setFocusPainted(false);

        searchLyrics.setText("Search Lyrics Online");
        searchLyrics.setFocusPainted(false);

        jLabel10.setText("Window Title");

        jLabel11.setText("Status Bar");

        buttonGroup1.add(albumArtSelected);
        albumArtSelected.setSelected(true);
        albumArtSelected.setText("Selected Track");
        albumArtSelected.setFocusPainted(false);

        jLabel12.setText("Show Album Art for");

        jLabel13.setText("Album Art Stubs");

        buttonGroup1.add(albumArtPlaying);
        albumArtPlaying.setText("Playing Track");
        albumArtPlaying.setFocusPainted(false);

        albumArtStubs.setColumns(20);
        albumArtStubs.setRows(5);
        jScrollPane2.setViewportView(albumArtStubs);

        separatorLabel4.setText("Album Art");

        separatorLabel3.setText("Display Formatting");

        jLabel23.setText("Shuffle Albums mode pattern");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
                jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel5Layout.createSequentialGroup()
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel5Layout.createSequentialGroup()
                                                .addContainerGap()
                                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(jLabel13)
                                                        .addGroup(jPanel5Layout.createSequentialGroup()
                                                                .addComponent(jLabel12)
                                                                .addGap(6, 6, 6)
                                                                .addComponent(albumArtSelected)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                .addComponent(albumArtPlaying))
                                                        .addGroup(jPanel5Layout.createSequentialGroup()
                                                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                        .addComponent(jLabel10)
                                                                        .addComponent(jLabel11))
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                        .addComponent(statusBarFormat, javax.swing.GroupLayout.DEFAULT_SIZE, 475, Short.MAX_VALUE)
                                                                        .addComponent(windowTitleFormat, javax.swing.GroupLayout.DEFAULT_SIZE, 475, Short.MAX_VALUE)))
                                                        .addComponent(separatorLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 540, Short.MAX_VALUE)
                                                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 540, Short.MAX_VALUE)
                                                        .addComponent(separatorLabel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 540, Short.MAX_VALUE)))
                                        .addGroup(jPanel5Layout.createSequentialGroup()
                                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(jPanel5Layout.createSequentialGroup()
                                                                .addGap(10, 10, 10)
                                                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                        .addComponent(enableTray)
                                                                        .addComponent(showSideBar)))
                                                        .addGroup(jPanel5Layout.createSequentialGroup()
                                                                .addContainerGap()
                                                                .addComponent(jLabel9)))
                                                .addGap(36, 36, 36)
                                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                        .addComponent(searchLyrics)
                                                        .addComponent(minimizeOnClose)
                                                        .addComponent(lookAndFeel, 0, 213, Short.MAX_VALUE)
                                                        .addComponent(shuffleAlbumsPattern, javax.swing.GroupLayout.DEFAULT_SIZE, 238, Short.MAX_VALUE)))
                                        .addGroup(jPanel5Layout.createSequentialGroup()
                                                .addContainerGap()
                                                .addComponent(jLabel23)))
                                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
                jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel5Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addGroup(jPanel5Layout.createSequentialGroup()
                                                .addComponent(enableTray)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(showSideBar))
                                        .addGroup(jPanel5Layout.createSequentialGroup()
                                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(lookAndFeel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(jLabel9))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(minimizeOnClose)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(searchLyrics)))
                                .addGap(9, 9, 9)
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel23)
                                        .addComponent(shuffleAlbumsPattern, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(separatorLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(windowTitleFormat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel10))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel11)
                                        .addComponent(statusBarFormat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addComponent(separatorLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel12)
                                        .addComponent(albumArtPlaying)
                                        .addComponent(albumArtSelected))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel13)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(82, Short.MAX_VALUE))
        );

        tabbedPane.addTab("GUI", jPanel5);

        jLabel14.setText("Text");

        jLabel15.setText("Background");

        jLabel16.setText("Selection");

        jLabel17.setText("Highlight");

        jLabel18.setText("Tray Background 1");

        jLabel19.setText("Tray Background 2");

        jLabel22.setText("Default");

        separatorLabel1.setText("Colors");

        separatorLabel2.setText("Fonts");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
                jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel8Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(separatorLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 540, Short.MAX_VALUE)
                                        .addGroup(jPanel8Layout.createSequentialGroup()
                                                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(jLabel14)
                                                        .addComponent(jLabel15)
                                                        .addComponent(jLabel16)
                                                        .addComponent(jLabel17)
                                                        .addComponent(jLabel18)
                                                        .addComponent(jLabel19)
                                                        .addComponent(jLabel22))
                                                .addGap(114, 114, 114)
                                                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                                .addComponent(trayBgColor2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addComponent(trayBgColor1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addComponent(highlightColor, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 221, Short.MAX_VALUE)
                                                                .addComponent(selectionColor, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 221, Short.MAX_VALUE)
                                                                .addComponent(backgroundColor, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 221, Short.MAX_VALUE)
                                                                .addComponent(textColor, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addComponent(defaultFont, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addComponent(separatorLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 540, Short.MAX_VALUE))
                                .addContainerGap())
        );

        jPanel8Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[]{backgroundColor, defaultFont, highlightColor, selectionColor, textColor, trayBgColor1, trayBgColor2});

        jPanel8Layout.setVerticalGroup(
                jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel8Layout.createSequentialGroup()
                                .addGap(16, 16, 16)
                                .addComponent(separatorLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                        .addComponent(jLabel14)
                                        .addComponent(textColor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                        .addComponent(jLabel15)
                                        .addComponent(backgroundColor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                        .addComponent(jLabel16)
                                        .addComponent(selectionColor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                        .addComponent(jLabel17)
                                        .addComponent(highlightColor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                        .addComponent(jLabel18)
                                        .addComponent(trayBgColor1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                        .addComponent(jLabel19)
                                        .addComponent(trayBgColor2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(31, 31, 31)
                                .addComponent(separatorLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                        .addComponent(jLabel22)
                                        .addComponent(defaultFont, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(401, 401, 401))
        );

        tabbedPane.addTab("Colors and Fonts", jPanel8);

        applyButton.setText("Apply");

        cancelButton.setText("Cancel");

        okButton.setText("OK");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap(437, Short.MAX_VALUE)
                                .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cancelButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(applyButton)
                                .addContainerGap())
                        .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 666, Short.MAX_VALUE)
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[]{applyButton, cancelButton, okButton});

        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(tabbedPane, javax.swing.GroupLayout.PREFERRED_SIZE, 455, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 17, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(applyButton)
                                        .addComponent(cancelButton)
                                        .addComponent(okButton))
                                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    public static void main(String args[]) {
        Application.getInstance().load();
        try {
            UIManager.setLookAndFeel(new MetalLookAndFeel());
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(OptionsDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                final JLabel fakeOwner = new JLabel();
                new JFrame().add(fakeOwner);
                OptionsDialog optionsDialog = new OptionsDialog(fakeOwner);
                optionsDialog.setModal(true);
                optionsDialog.setVisible(true);
                Application.getInstance().exit();
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addMusicFolder;
    private javax.swing.JButton addView;
    private javax.swing.JRadioButton albumArtPlaying;
    private javax.swing.JRadioButton albumArtSelected;
    private javax.swing.JTextArea albumArtStubs;
    private javax.swing.JButton applyButton;
    private javax.swing.JComboBox audioMixer;
    private com.tulskiy.musique.gui.components.ColorChooser backgroundColor;
    private javax.swing.JButton cancelButton;
    private javax.swing.JComboBox defaultEncoding;
    private com.tulskiy.musique.gui.components.FontChooser defaultFont;
    private javax.swing.JCheckBox enableHttpProxy;
    private javax.swing.JCheckBox enableLastFmScrobbling;
    private javax.swing.JCheckBox enableLibraryView;
    private javax.swing.JCheckBox enableTray;
    private com.tulskiy.musique.gui.components.ColorChooser highlightColor;
    private javax.swing.JTextField httpProxyHost;
    private javax.swing.JPasswordField httpProxyPassword;
    private javax.swing.JTextField httpProxyPort;
    private javax.swing.JTextField httpProxyUsername;
    private javax.swing.JPasswordField lastfmPassword;
    private javax.swing.JTextField lastfmUsername;
    private javax.swing.JComboBox libraryDoubleClickAction;
    private javax.swing.JComboBox libraryMiddleClickAction;
    private javax.swing.JTable libraryViewsTable;
    private javax.swing.JComboBox lookAndFeel;
    private javax.swing.JCheckBox minimizeOnClose;
    private javax.swing.JList musicFolders;
    private javax.swing.JButton okButton;
    private javax.swing.JButton removeMusicFolder;
    private javax.swing.JButton removeView;
    private javax.swing.JCheckBox searchLyrics;
    private com.tulskiy.musique.gui.components.ColorChooser selectionColor;
    private javax.swing.JCheckBox showSideBar;
    private javax.swing.JTextField shuffleAlbumsPattern;
    private javax.swing.JCheckBox singleInstance;
    private javax.swing.JTextField statusBarFormat;
    private javax.swing.JTabbedPane tabbedPane;
    private com.tulskiy.musique.gui.components.ColorChooser textColor;
    private com.tulskiy.musique.gui.components.ColorChooser trayBgColor1;
    private com.tulskiy.musique.gui.components.ColorChooser trayBgColor2;
    private javax.swing.JTextField windowTitleFormat;
    // End of variables declaration//GEN-END:variables

}
