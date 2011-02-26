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

package com.tulskiy.musique.gui.components;

import javax.swing.*;
import java.awt.*;

/**
 * Author: Denis Tulskiy
 * Date: 1/9/11
 */
public class SeparatorLabel extends JLabel {
    JSeparator separator = new JSeparator();

    public SeparatorLabel() {
        this("");
    }

    public SeparatorLabel(String text) {
        super(text);
        Font font = getFont();
        setFont(font.deriveFont(Font.BOLD, font.getSize() + 1));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Dimension preferredSize = getPreferredSize();
        Dimension size = getSize();
        g.setColor(Color.GRAY);
        g.drawLine(
                preferredSize.width + 10,
                getHeight() / 2,
                size.width,
                getHeight() / 2);
    }
}
