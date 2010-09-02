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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Author: Denis Tulskiy
 * Date: Aug 1, 2010
 */
public class PathChooser extends JPanel {
    private Application app = Application.getInstance();
    private Configuration config = app.getConfiguration();
    String path;
    JTextField text = new JTextField();
    private JButton button;

    PathChooser(String path) {
        setPath(path);
        setLayout(new BorderLayout());
        add(text, BorderLayout.CENTER);
        button = new JButton("...");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser(config.getString("playlist.lastDir", ""));
                fc.setSize(500, 500);
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                int ret = fc.showDialog(getParent(), "Choose Folder");
                if (ret == JFileChooser.APPROVE_OPTION) {
                    setPath(fc.getSelectedFile().getAbsolutePath());
                }
            }
        });

        text.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                fire();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                fire();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                fire();
            }

            private void fire() {
                firePropertyChange("path", null, null);
            }
        });

        add(button, BorderLayout.LINE_END);
    }

    public String getPath() {
        return text.getText();
    }

    public void setPath(String path) {
        this.path = path;
        text.setText(path);
        firePropertyChange("path", null, path);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        text.setEnabled(enabled);
        button.setEnabled(enabled);
    }

    public JTextField getTextComponent() {
        return text;
    }
}
