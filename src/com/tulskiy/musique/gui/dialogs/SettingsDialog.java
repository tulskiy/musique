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

package com.tulskiy.musique.gui.dialogs;

import com.tulskiy.musique.audio.AudioFileReader;
import com.tulskiy.musique.audio.player.AudioOutput;
import com.tulskiy.musique.system.Application;
import com.tulskiy.musique.system.Configuration;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

/**
 * Author: Denis Tulskiy
 * Date: Jul 18, 2010
 */
public class SettingsDialog extends JDialog {
    private Application app = Application.getInstance();
    private Configuration config = app.getConfiguration();

    private JButton applyButton;
    private JComponent owner;
    private JButton defaults;
    private JTabbedPane tabs;

    public SettingsDialog(JComponent owner) {
        super(SwingUtilities.windowForComponent(owner), "Settings", ModalityType.MODELESS);
        this.owner = owner;

        tabs = new JTabbedPane();
        tabs.setFocusable(false);

        Box buttons = Box.createHorizontalBox();
        buttons.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        defaults = new JButton("Defaults");
        applyButton = new JButton(" Apply ");

        final JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                dispose();
            }
        });

        JButton okButton = new JButton("   OK   ");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyButton.doClick();
                cancelButton.doClick();
            }
        });
        buttons.add(defaults);
        buttons.add(Box.createHorizontalGlue());
        buttons.add(okButton);
        buttons.add(Box.createHorizontalStrut(3));
        buttons.add(cancelButton);
        buttons.add(Box.createHorizontalStrut(3));
        buttons.add(applyButton);

        tabs.add(createSystemPanel());
        tabs.add(createNetworkPanel());
        tabs.add(createGUIPanel());
        tabs.add(createColorsAndFontsPanel());
        add(tabs, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        pack();
        setSize(600, getHeight());
        setLocationRelativeTo(SwingUtilities.windowForComponent(owner));
    }

    private boolean isSelected(JComponent component) {
        return tabs.getSelectedComponent() == component;
    }

    private JComponent createGUIPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setName("GUI");
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 100));

        Box mainBox = Box.createVerticalBox();
        JPanel misc = new JPanel(new GridLayout(2, 2, 10, 10));
        mainBox.add(misc);

        misc.add(new JLabel("Look and Feel"));
        final UIManager.LookAndFeelInfo[] lafs = UIManager.getInstalledLookAndFeels();
        Vector<String> lafsVector = new Vector<String>();
        for (UIManager.LookAndFeelInfo laf : lafs) {
            lafsVector.add(laf.getName());
        }
        final JComboBox laf = new JComboBox(lafsVector);
        String name = UIManager.getLookAndFeel().getName();
        if (name.contains("GTK"))
            name = "GTK+";
        laf.setSelectedItem(name);
        misc.add(laf);

        final JCheckBox trayEnabled = new JCheckBox("Enable System Tray", config.getBoolean("tray.enabled", false));
        misc.add(trayEnabled);
        final JCheckBox minimizeOnClose = new JCheckBox("Minimize to Tray on close", config.getBoolean("tray.minimizeOnClose", true));
        misc.add(minimizeOnClose);

        Box format = Box.createVerticalBox();
        format.setBorder(BorderFactory.createTitledBorder("Display Formatting"));
        format.add(new JLabel("Window Title"));
        format.add(Box.createVerticalStrut(2));
        final JTextField window = new JTextField(config.getString("format.window", ""));
        window.setCaretPosition(0);
        format.add(window);
        format.add(Box.createVerticalStrut(2));
        format.add(new JLabel("Status Bar"));
        final JTextField status = new JTextField(config.getString("format.statusBar", ""));
        status.setCaretPosition(0);
        format.add(status);

        mainBox.add(Box.createVerticalStrut(20));
        mainBox.add(format);

        Box side = Box.createVerticalBox();
        JPanel sidePanel = new JPanel(new BorderLayout());
        sidePanel.setBorder(BorderFactory.createTitledBorder("Side Bar"));
        sidePanel.add(side, BorderLayout.CENTER);

        final JCheckBox sidebar = new JCheckBox("Show Side Bar", config.getBoolean("sidebar.enabled", true));
        side.add(sidebar);
        side.add(Box.createVerticalStrut(10));
        final JCheckBox lyrics = new JCheckBox("Search lyrics online",
                config.getBoolean("lyrics.searchOnline", true));
        side.add(lyrics);
        side.add(Box.createVerticalStrut(5));
        Box aaBox = Box.createHorizontalBox();
        ButtonGroup bg = new ButtonGroup();
        boolean nowPlaying = config.getBoolean("albumart.nowPlayingOnly", false);
        final JRadioButton plTrack = new JRadioButton("Playing track", nowPlaying);
        JRadioButton selTrack = new JRadioButton("Selected track", !nowPlaying);
        bg.add(plTrack);
        bg.add(selTrack);
        aaBox.setAlignmentX(0);
        aaBox.add(new JLabel("Show Album Art for: "));
        aaBox.add(Box.createHorizontalStrut(10));
        aaBox.add(plTrack);
        aaBox.add(Box.createHorizontalStrut(10));
        aaBox.add(selTrack);

        side.add(aaBox);
        side.add(Box.createVerticalStrut(5));

        side.add(new JLabel("Album Art stubs: "));
        ArrayList<String> stubList = config.getList("albumart.stubs", null);
        StringBuilder sb = new StringBuilder();
        for (String s : stubList) {
            sb.append(s).append("\n");
        }
        final JTextArea stubs = new JTextArea(sb.toString(), 6, 1);
        stubs.setAlignmentX(0);
        side.add(stubs);

        mainBox.add(sidePanel);

        panel.add(mainBox, BorderLayout.NORTH);

        final JDialog comp = this;
        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = laf.getSelectedIndex();
                if (index != -1) {
                    try {
                        UIManager.setLookAndFeel(lafs[index].getClassName());
                        SwingUtilities.updateComponentTreeUI(SwingUtilities.getRoot(owner));
                        SwingUtilities.updateComponentTreeUI(comp);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }

                config.setBoolean("tray.enabled", trayEnabled.isSelected());
                config.setBoolean("tray.minimizeOnClose", minimizeOnClose.isSelected());
                config.setString("format.window", window.getText());
                config.setString("format.statusBar", status.getText());
                config.setBoolean("sidebar.enabled", sidebar.isSelected());
                config.setBoolean("lyrics.searchOnline", lyrics.isSelected());
                config.setBoolean("albumart.nowPlayingOnly", plTrack.isSelected());
                List<String> stubList = Arrays.asList(stubs.getText().split("\n"));
                config.setList("albumart.stubs", new ArrayList<String>(stubList));
            }
        });

        return panel;
    }

    private JComponent createColorsAndFontsPanel() {
        final JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 100));
        panel.setName("Colors and Fonts");
        panel.setLayout(new BorderLayout());
        Box mainBox = Box.createVerticalBox();
        panel.add(mainBox, BorderLayout.NORTH);

        JPanel colors = new JPanel(new GridLayout(6, 2, 10, 10));
        colors.setBorder(BorderFactory.createTitledBorder("Colors"));

        colors.add(new JLabel("Text"));
        final ColorChooser text = new ColorChooser(config.getColor("gui.color.text", null));
        colors.add(text);

        colors.add(new JLabel("Background"));
        final ColorChooser background = new ColorChooser(config.getColor("gui.color.background", null));
        colors.add(background);

        colors.add(new JLabel("Selection"));
        final ColorChooser selection = new ColorChooser(config.getColor("gui.color.selection", null));
        colors.add(selection);

        colors.add(new JLabel("Highlight"));
        final ColorChooser highlight = new ColorChooser(config.getColor("gui.color.highlight", null));
        colors.add(highlight);

        colors.add(new JLabel("Tray Background 1"));
        final ColorChooser trayBg1 = new ColorChooser(config.getColor("tray.bgColor1", null));
        colors.add(trayBg1);

        colors.add(new JLabel("Tray Background 2"));
        final ColorChooser trayBg2 = new ColorChooser(config.getColor("tray.bgColor2", null));
        colors.add(trayBg2);

        mainBox.add(colors);
        mainBox.add(Box.createVerticalStrut(20));

        JPanel fonts = new JPanel(new GridLayout(1, 2, 10, 10));
        fonts.setBorder(BorderFactory.createTitledBorder("Fonts"));

        fonts.add(new JLabel("Default"));
        final FontChooser defaultFont = new FontChooser(config.getFont("gui.font.default", null));
        fonts.add(defaultFont);

//        fonts.add(new JLabel("Tabs"));
//        FontChooser tabsFont = new FontChooser(config.getFont("gui.font.tabs", null));
//        fonts.add(tabsFont);

        mainBox.add(fonts);

        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                config.setColor("gui.color.text", text.getColor());
                config.setColor("gui.color.background", background.getColor());
                config.setColor("gui.color.selection", selection.getColor());
                config.setColor("gui.color.highlight", highlight.getColor());
                config.setFont("gui.font.default", defaultFont.getSelectedFont());
                config.setColor("tray.bgColor1", trayBg1.getColor());
                config.setColor("tray.bgColor2", trayBg2.getColor());
                SwingUtilities.updateComponentTreeUI(SwingUtilities.getRoot(owner));
            }
        });

        defaults.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isSelected(panel)) {
                    text.setColor(null);
                    background.setColor(null);
                    selection.setColor(null);
                    highlight.setColor(null);
                    defaultFont.setFont(null);
                    trayBg1.setColor(null);
                    trayBg2.setColor(null);
                }
            }
        });

        return panel;
    }

    private JComponent createSystemPanel() {
        final JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setName("System");
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 100));

        Box mainBox = Box.createVerticalBox();
        JPanel misc = new JPanel(new GridLayout(1, 2, 10, 10));

        final AudioOutput output = app.getPlayer().getAudioOutput();

        Box mix = Box.createHorizontalBox();
        mix.add(new JLabel("Audio Mixer: "));
        Vector<String> mixerVector = new Vector<String>();
        mixerVector.add("Detect automatically");
        final Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
        int selectedIndex = Arrays.asList(mixerInfo).indexOf(output.getMixer());
        for (Mixer.Info info : mixerInfo) {
            String s = info.getDescription() + ", " + info.getName();
            mixerVector.add(s);
        }
        final JComboBox mixers = new JComboBox(mixerVector);
        mixers.setSelectedIndex(selectedIndex + 1);
        mixers.setPrototypeDisplayValue(mixerVector.get(0));
        mix.add(mixers);
        mainBox.add(mix);
        mainBox.add(Box.createVerticalStrut(5));
        mainBox.add(misc);

        misc.add(new JLabel("Default Encoding for Tags"));
        Charset charset = AudioFileReader.getDefaultCharset();
        final JComboBox encoding = new JComboBox(Charset.availableCharsets().values().toArray());
        encoding.setSelectedItem(charset);
        misc.add(encoding);


        panel.add(mainBox, BorderLayout.NORTH);

        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = mixers.getSelectedIndex();
                if (index > 0) {
                    Mixer.Info info = mixerInfo[index - 1];
                    output.setMixer(info);
                } else {
                    output.setMixer(null);
                }
                AudioFileReader.setDefaultCharset((Charset) encoding.getSelectedItem());
            }
        });

        return panel;
    }

    private JComponent createNetworkPanel() {
        final JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setName("Network");
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 100));
        Box mainBox = Box.createVerticalBox();
        panel.add(mainBox, BorderLayout.NORTH);

        JPanel proxy = new JPanel(new GridLayout(5, 2, 0, 5));
        mainBox.add(proxy);
        proxy.setBorder(BorderFactory.createTitledBorder("HTTP Proxy"));

        final JCheckBox proxyEnabled = new JCheckBox("Use HTTP Proxy");
        proxyEnabled.setSelected(config.getBoolean("proxy.enabled", false));
        proxy.add(proxyEnabled);
        proxy.add(new JLabel());

        proxy.add(new JLabel("Host"));
        final JTextField host = new JTextField();
        host.setText(config.getString("proxy.host", null));
        proxy.add(host);

        proxy.add(new JLabel("Port"));
        final JTextField port = new JTextField();
        port.setText(config.getString("proxy.port", null));
        proxy.add(port);

        proxy.add(new JLabel("Username"));
        final JTextField user = new JTextField();
        user.setText(config.getString("proxy.user", null));
        proxy.add(user);

        proxy.add(new JLabel("Password"));
        final JPasswordField password = new JPasswordField();
        password.setText(config.getString("proxy.password", null));
        proxy.add(password);

        final JPanel lastfm = new JPanel(new GridLayout(3, 2));
        lastfm.setBorder(BorderFactory.createTitledBorder("Last.fm Scrobbling"));

        final JCheckBox lfmEnabled = new JCheckBox("Enable Last.fm Scrobbling");
        lastfm.add(lfmEnabled);
        lfmEnabled.setSelected(config.getBoolean("lastfm.enabled", false));
        lastfm.add(new JLabel());

        lastfm.add(new JLabel("Username"));
        final JTextField lfmUsername = new JTextField();
        lfmUsername.setText(config.getString("lastfm.user", null));
        lastfm.add(lfmUsername);

        lastfm.add(new JLabel("Password"));
        final JPasswordField lfmPassword = new JPasswordField();
        lfmPassword.setText(config.getString("lastfm.password", null));
        lastfm.add(lfmPassword);

        mainBox.add(lastfm);

        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                config.setBoolean("lastfm.enabled", lfmEnabled.isSelected());
                config.setString("lastfm.user", lfmUsername.getText());
                config.setString("lastfm.password", String.valueOf(lfmPassword.getPassword()));

                config.setBoolean("proxy.enabled", proxyEnabled.isSelected());
                config.setString("proxy.host", host.getText());
                config.setString("proxy.port", port.getText());
                config.setString("proxy.user", user.getText());
                config.setString("proxy.password", String.valueOf(password.getPassword()));

                if (proxyEnabled.isSelected()) {
                    System.setProperty("http.proxyHost", host.getText());
                    System.setProperty("http.proxyPort", port.getText());
                } else {
                    System.setProperty("http.proxyHost", "");
                    System.setProperty("http.proxyPort", "");
                }
            }
        });

        return panel;
    }

    class ColorChooser extends JPanel {
        private Color color;
        private JPanel panel = new JPanel();

        ColorChooser(Color color) {
            super(new BorderLayout());
            add(panel, BorderLayout.CENTER);
            JButton clear = new JButton("X");
            int width = 43;
            String laf = UIManager.getLookAndFeel().getName();
            if (laf.contains("GTK"))
                width = 25;
            else if (laf.contains("Nimbus"))
                width = 35;

            clear.setPreferredSize(new Dimension(width, 25));
            clear.setFocusable(false);
            add(clear, BorderLayout.LINE_END);
            clear.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setColor(null);
                }
            });
            setColor(color);
            setPreferredSize(new Dimension(10, 25));
            panel.setBorder(BorderFactory.createEtchedBorder());

            final JComponent comp = this;
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Color c = JColorChooser.showDialog(comp, "Choose Color", getColor());
                    if (c != null)
                        setColor(c);
                }
            });
        }

        public Color getColor() {
            return color;
        }

        public void setColor(Color color) {
            this.color = color;
            panel.setBackground(color);
        }
    }

    class FontChooser extends JPanel {
        Font selectedFont;
        JLabel text = new JLabel();

        FontChooser(Font font) {
            super(new BorderLayout());
            add(text, BorderLayout.CENTER);
            JButton clear = new JButton("X");
            int width = 43;
            String laf = UIManager.getLookAndFeel().getName();
            if (laf.contains("GTK"))
                width = 25;
            else if (laf.contains("Nimbus"))
                width = 35;

            clear.setPreferredSize(new Dimension(width, 25));
            clear.setFocusable(false);
            add(clear, BorderLayout.LINE_END);
            clear.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setSelectedFont(null);
                }
            });

            this.selectedFont = font;
            setSelectedFont(font);
            setPreferredSize(new Dimension(10, 25));
            text.setHorizontalAlignment(JLabel.CENTER);
            text.setBorder(BorderFactory.createEtchedBorder());
            final JComponent comp = this;
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Font f = FontChooserDialog.show(comp, getFont());
                    if (f != null)
                        setSelectedFont(f);
                }
            });
        }

        public Font getSelectedFont() {
            return selectedFont;
        }

        public void setSelectedFont(Font font) {
            setFont(font);
            this.selectedFont = font;
            String s = "";
            if (font != null) {
                s += font.getName() + ", " + font.getSize() + "pt";
            }
            if (text != null)
                text.setText(s);
        }
    }
}
