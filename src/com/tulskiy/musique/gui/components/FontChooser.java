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

import com.tulskiy.musique.gui.dialogs.FontChooserDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Author: Denis Tulskiy
 * Date: 12/23/10
 */
public class FontChooser extends JPanel {
    Font selectedFont;
    JLabel text = new JLabel();

    public FontChooser() {
        this(null);
    }

    public FontChooser(Font font) {
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
