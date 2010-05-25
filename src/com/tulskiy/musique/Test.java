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

import com.sun.java.swing.plaf.gtk.GTKLookAndFeel;
import com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel;
import sun.swing.ImageIconUIResource;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalTabbedPaneUI;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Author: Denis Tulskiy
 *
 * @Date: Jan 21, 2010
 */
public class Test {
    public static void main(String[] args) throws ClassNotFoundException, UnsupportedLookAndFeelException, IllegalAccessException, InstantiationException {
//        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        UIManager.setLookAndFeel(new GTKLookAndFeel());

        UIDefaults uiDefaults = UIManager.getDefaults();
        for (Map.Entry<Object, Object> e : uiDefaults.entrySet()) {
//            if (e.getKey().toString().startsWith("Tabbed"))
//                System.out.println(e);
        }
        JFrame f = new JFrame();

        f.setLayout(new BorderLayout());
        f.setSize(300, 100);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTabbedPane tp = new JTabbedPane();
        tp.setUI(new MyUI());
        tp.setFocusable(false);
//        tp.setOpaque(true);

        addTab(tp, "Tab 1");
        addTab(tp, "Default");
        addTab(tp, "Very, very long tab");

        f.add(tp, BorderLayout.CENTER);

        f.setVisible(true);
    }

    private static void addTab(JTabbedPane tp, String name) {
        JLabel l = new JLabel(name);
        tp.addTab(name, l);
        int index = tp.indexOfComponent(l);
        JLabel tab = new JLabel(name);
        tp.setTabComponentAt(index, tab);
//        tab.setMaximumSize(new Dimension(100, 16));
//        tab.setMinimumSize(new Dimension(40, 16));
    }

    static class MyUI extends MetalTabbedPaneUI {
        protected int minTabWidth = 60;

        @Override
        protected void installDefaults() {
            Color oldForeground = UIManager.getColor("TabbedPane.foreground");
            Color oldLight = UIManager.getColor("TabbedPane.light");
            Color oldBackground = UIManager.getColor("TabbedPane.background");
            super.installDefaults();

//            darkShadow = Color.black;
//            shadow = Color.blue;
//            selectColor = oldBackground;
//            selectHighlight = selectColor;
        }

        @Override
        protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
            return Math.max(minTabWidth, super.calculateTabWidth(tabPlacement, tabIndex, metrics) + 10);
        }

        @Override
        protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
            Polygon p = new Polygon();
            int[][] c = new int[][]{
                    {x, y + h - 2},
                    {x, y + 1},
                    {x + 1, y + 0},
                    {x + w - h - 1, y + 0},
                    {x + w - h, y + 1},
                    {x + w - h + 1, y + 1},
                    {x + w - 2, y + h - 2},
                    {x + w - 1, y + h - 2},
                    {x + w - 2, y + h - 3}
            };

            g.setColor(Color.black);
            for (int i = 0; i < c.length - 1; i++) {
                g.drawLine(c[i][0], c[i][1], c[i + 1][0], c[i + 1][1]);
            }
        }

        @Override
        protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
//            super.paintTabBackground(g, tabPlacement, tabIndex, x, y, w, h, isSelected);
        }
    }
}
