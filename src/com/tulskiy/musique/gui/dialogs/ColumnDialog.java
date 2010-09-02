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

import com.tulskiy.musique.gui.playlist.PlaylistColumn;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Author: Denis Tulskiy
 * Date: May 21, 2010
 */
public class ColumnDialog extends JDialog {
    private static Object[] formats = new Object[]{
            "[%artist% - ]%title%", "%title%", "%artist%", "%album%", "%year%",
            "%length%", "$isPlaying", "%fileName%", "%albumArtist% - %year% - %album% - %discNumber% - %trackNumber% - %title% - %fileName%"
    };

    private boolean accept = false;

    public ColumnDialog(JFrame parent, String title, final PlaylistColumn column) {
        super(parent, title, true);
        setLayout(new BorderLayout());

        JPanel p1 = new JPanel(new GridLayout(4, 1));
        p1.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(p1, BorderLayout.NORTH);
        p1.add(new JLabel("Name"));
        final JTextField columnName = new JTextField();
        columnName.setText(column.getName());
        p1.add(columnName);
        p1.add(new JLabel("Format"));
        final JComboBox format = new JComboBox(formats);
        format.setEditable(true);
        format.setSelectedItem(column.getExpression());
        p1.add(format);

        Box b2 = new Box(BoxLayout.X_AXIS);
        b2.add(Box.createHorizontalGlue());
        JButton okButton = new JButton("  OK  ");
        getRootPane().setDefaultButton(okButton);
        b2.add(okButton);
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = columnName.getText();
                String fmt = (String) format.getSelectedItem();
                if (name == null || name.isEmpty() || fmt == null || fmt.isEmpty()) {
                    accept = false;
                    return;
                }
                accept = true;
                column.setName(name);
                column.setExpression(fmt);
                setVisible(false);
            }
        });
        b2.add(Box.createHorizontalStrut(5));

        JButton cancelButton = new JButton("Cancel");
        b2.add(cancelButton);
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                accept = false;
                setVisible(false);
            }
        });
        b2.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 10));
        add(b2, BorderLayout.SOUTH);
        pack();
        setSize(300, getHeight());
        setLocationRelativeTo(parent);

    }

    public boolean showDialog() {
        setVisible(true);
        return accept;
    }
}

