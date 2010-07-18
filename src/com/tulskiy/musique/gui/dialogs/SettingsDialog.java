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

import com.tulskiy.musique.system.Application;
import com.tulskiy.musique.system.Configuration;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

/**
 * Author: Denis Tulskiy
 * Date: Jul 18, 2010
 */
public class SettingsDialog extends JDialog {
    private Application app = Application.getInstance();
    private Configuration config = app.getConfiguration();

    private JButton saveButton;
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
        buttons.add(defaults);
        buttons.add(Box.createHorizontalGlue());
        saveButton = new JButton(" Save ");
        buttons.add(saveButton);
        buttons.add(Box.createHorizontalStrut(5));
        JButton cancelButton = new JButton("Close");
        buttons.add(cancelButton);
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                dispose();
            }
        });

        tabs.add(createGUIPanel());
        tabs.add(createColorsAndFontsPanel());
        add(tabs, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        pack();
        setSize(500, getHeight());
        setLocationRelativeTo(owner);
    }

    private boolean isSelected(JComponent component) {
        return tabs.getSelectedComponent() == component;
    }

    private JComponent createGUIPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setName("GUI");
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 100));

        JPanel mainPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        mainPanel.setBorder(BorderFactory.createTitledBorder(""));

        mainPanel.add(new JLabel("Look and Feel"));
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
        mainPanel.add(laf);

        final JCheckBox trayEnabled = new JCheckBox("Enable System Tray", config.getBoolean("tray.enabled", false));
        mainPanel.add(trayEnabled);
        final JCheckBox minimizeOnClose = new JCheckBox("Minimize to Tray on close", config.getBoolean("tray.minimizeOnClose", true));
        mainPanel.add(minimizeOnClose);

        panel.add(mainPanel, BorderLayout.NORTH);

        final JDialog comp = this;
        saveButton.addActionListener(new ActionListener() {
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
                app.getMainWindow().updateTray();
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

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                config.setColor("gui.color.text", text.getColor());
                config.setColor("gui.color.background", background.getColor());
                config.setColor("gui.color.selection", selection.getColor());
                config.setColor("gui.color.highlight", highlight.getColor());
                config.setFont("gui.font.default", defaultFont.getFont());
                config.setColor("tray.bgColor1", trayBg1.getColor());
                config.setColor("tray.bgColor2", trayBg2.getColor());
                app.getMainWindow().updateTray();
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
        Font font;
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
                    setFont(null);
                }
            });

            this.font = font;
            setFont(font);
            setPreferredSize(new Dimension(10, 25));
            text.setHorizontalAlignment(JLabel.CENTER);
            text.setBorder(BorderFactory.createEtchedBorder());
            final JComponent comp = this;
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Font f = FontChooserDialog.show(comp, getFont());
                    if (f != null)
                        setFont(f);
                }
            });
        }

        public Font getFont() {
            return font;
        }

        public void setFont(Font font) {
            super.setFont(font);
            this.font = font;
            String s = "";
            if (font != null) {
                s += font.getName() + ", " + font.getSize() + "pt";
            }
            if (text != null)
                text.setText(s);
        }
    }
}
