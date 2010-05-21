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

package com.tulskiy.musique;

import sun.swing.ImageIconUIResource;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * @Author: Denis Tulskiy
 * @Date: Jan 21, 2010
 */
public class Test {
    public static void main(String[] args) throws ClassNotFoundException, UnsupportedLookAndFeelException, IllegalAccessException, InstantiationException {

        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        JFrame f = new JFrame();

        f.setLayout(new BorderLayout());
        Box b = new Box(BoxLayout.X_AXIS);
        f.add(b, BorderLayout.NORTH);
        f.setSize(300, 100);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        b.add(createButton("resources/images/prev.png", null));
        b.add(createButton("resources/images/stop.png", null));
        b.add(createButton("resources/images/play.png", null));
        b.add(createButton("resources/images/pause.png", null));
        b.add(createButton("resources/images/next.png", null));

        f.setVisible(true);
    }

    private static JComponent createButton(String path, ActionListener l) {
//        Image i = new ImageIcon(path).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
        JButton b = new JButton();
        b.setIcon(new ImageIcon(path));
        b.setBorderPainted(false);
        b.setFocusable(false);
        b.addActionListener(l);

        return b;
    }
}
