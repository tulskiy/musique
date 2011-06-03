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

package com.tulskiy.musique.gui.components;

import com.tulskiy.musique.images.Images;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
 * Author: Denis Tulskiy
 * Date: 12/23/10
 */
public class SearchField extends JTextField {
    private ArrayList<ChangeListener> listeners = new ArrayList<ChangeListener>();
    private AlphaComposite alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f);
    private boolean isLabelFocused = false;

    public SearchField() {
        super();
        setLayout(new BorderLayout());
        final JLabel clear = new JLabel(Images.loadIcon("clear.png")) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                if (!isLabelFocused)
                    g2d.setComposite(alpha);
                super.paintComponent(g2d);
            }
        };
        clear.setCursor(Cursor.getDefaultCursor());
        clear.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isLabelFocused = true;
                clear.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isLabelFocused = false;
                clear.repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                setText("");
            }
        });
//        setMargin(new Insets(0, 0, 0, 20));
        getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                fireStateChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                fireStateChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                fireStateChanged();
            }
        });
        add(clear, BorderLayout.LINE_END);
    }

    private void fireStateChanged() {
        ChangeEvent e = new ChangeEvent(this);
        for (ChangeListener listener : listeners) {
            listener.stateChanged(e);
        }
    }

    public void addChangeListener(ChangeListener listener) {
        listeners.add(listener);
    }
}
