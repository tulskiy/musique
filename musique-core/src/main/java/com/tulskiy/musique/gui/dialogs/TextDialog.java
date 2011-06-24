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

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Author: Denis Tulskiy
 * Date: Sep 29, 2010
 */
public class TextDialog extends JDialog {
    public TextDialog(JFrame owner, String title, File file) {
        super(owner, title);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        StringBuilder sb = new StringBuilder();
        try {
            Scanner fi = new Scanner(file);
            while (fi.hasNextLine()) {
                sb.append(fi.nextLine()).append("\n");
            }
            JTextArea textArea = new JTextArea(sb.toString());
            textArea.setOpaque(true);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setEditable(false);
            add(new JScrollPane(textArea));

            Box buttonBox = Box.createHorizontalBox();
            buttonBox.add(Box.createHorizontalGlue());
            JButton close = new JButton("Close");
            buttonBox.add(close);
            getRootPane().setDefaultButton(close);

            close.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setVisible(false);
                    dispose();
                }
            });
            InputMap iMap = getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
            iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escape");

            ActionMap aMap = getRootPane().getActionMap();
            aMap.put("escape", new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    setVisible(false);
                    dispose();
                }
            });
            buttonBox.add(Box.createHorizontalStrut(10));
            add(buttonBox, BorderLayout.PAGE_END);
            setSize(600, 500);
            setLocationRelativeTo(null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
