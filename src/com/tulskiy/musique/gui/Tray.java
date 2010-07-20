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

package com.tulskiy.musique.gui;

import com.tulskiy.musique.audio.player.Player;
import com.tulskiy.musique.system.Application;
import com.tulskiy.musique.system.Configuration;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

/**
 * Author: Denis Tulskiy
 * Date: Jun 23, 2010
 */
public class Tray {
    private Application app = Application.getInstance();
    private Configuration config = app.getConfiguration();
    private Player player = app.getPlayer();
    private JPopupTrayIcon trayIcon;

    public void install() {
        try {
            if (trayIcon == null && SystemTray.isSupported()) {
                SystemTray systemTray = SystemTray.getSystemTray();
                Dimension size = systemTray.getTrayIconSize();
                trayIcon = createTrayIcon(size);
                systemTray.add(trayIcon);

                JPopupMenu popup = new JPopupMenu();
                trayIcon.setJPopupMenu(popup);

                createPopup(popup);
            }
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    private void createPopup(JPopupMenu popup) {
        ActionListener al = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String cmd = e.getActionCommand().trim();
                if (cmd.equalsIgnoreCase("play")) {
                    player.play();
                } else if (cmd.equalsIgnoreCase("pause")) {
                    player.pause();
                } else if (cmd.equalsIgnoreCase("next")) {
                    player.next();
                } else if (cmd.equalsIgnoreCase("previous")) {
                    player.prev();
                } else if (cmd.equalsIgnoreCase("stop")) {
                    player.stop();
                } else if (cmd.equalsIgnoreCase("quit")) {
                    app.exit();
                }

            }
        };

        popup.add("Play").addActionListener(al);
        popup.add("Pause").addActionListener(al);
        popup.add("Next").addActionListener(al);
        popup.add("Stop").addActionListener(al);
        popup.add("Previous    ").addActionListener(al);
        popup.add("Quit").addActionListener(al);
    }

    private JPopupTrayIcon createTrayIcon(Dimension size) {
        ImageIcon icon;
        if (size.height < 24)
            icon = new ImageIcon("resources/images/tray16.png");
        else
            icon = new ImageIcon("resources/images/tray24.png");
        BufferedImage img = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D d = img.createGraphics();

        Color color1 = config.getColor("tray.bgColor1", null);
        if (color1 != null) {
            Color color2 = config.getColor("tray.bgColor2", null);

            if (color2 == null)
                color2 = color1;
            Paint old = d.getPaint();
            d.setPaint(new GradientPaint(0, 0, color1, 0, size.height, color2));
            d.fillRect(0, 0, size.width, size.height);
            d.setPaint(old);
        }

        d.drawImage(icon.getImage(), 0, 0, size.width, size.height, icon.getImageObserver());
        return new JPopupTrayIcon(img);
    }

    public void uninstall() {
        if (SystemTray.isSupported()) {
            SystemTray.getSystemTray().remove(trayIcon);
            trayIcon = null;
        }
    }

    public void addMouseListener(MouseListener listener) {
        trayIcon.addMouseListener(listener);
    }

    /**
     * JPopupMenu compatible TrayIcon based on Alexander Potochkin's JXTrayIcon
     * (http://weblogs.java.net/blog/alexfromsun/archive/2008/02/jtrayicon_updat.html)
     * but uses a JWindow instead of a JDialog to workaround some bugs on linux.
     * <p/>
     * Created on Sep 15, 2008  5:51:33 PM
     *
     * @author Michael Bien
     */
    public class JPopupTrayIcon extends TrayIcon {

        private JPopupMenu menu;

        private Window window;
        private PopupMenuListener popupListener;

        private final boolean isWindows = System.getProperty("os.name").toLowerCase().contains("windows");

        public JPopupTrayIcon(Image image) {
            super(image);
            init();
        }

        public JPopupTrayIcon(Image image, String tooltip) {
            super(image, tooltip);
            init();
        }

        public JPopupTrayIcon(Image image, String tooltip, PopupMenu popup) {
            super(image, tooltip, popup);
            init();
        }

        public JPopupTrayIcon(Image image, String tooltip, JPopupMenu popup) {
            super(image, tooltip);
            init();
            setJPopupMenu(popup);
        }

        private void init() {
            popupListener = new PopupMenuListener() {
                @Override
                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                }

                @Override
                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                    if (window != null) {
                        window.dispose();
                        window = null;
                    }
                }

                @Override
                public void popupMenuCanceled(PopupMenuEvent e) {
                    if (window != null) {
                        window.dispose();
                        window = null;
                    }
                }
            };

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    showJPopupMenu(e);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    showJPopupMenu(e);
                }
            });

        }

        private void showJPopupMenu(MouseEvent e) {
            try {
                if (e.isPopupTrigger() && menu != null) {
                    if (window == null) {

                        if (isWindows) {
                            window = new JDialog((Frame) null);
                            ((JDialog) window).setUndecorated(true);
                        } else {
                            window = new JWindow((Frame) null);
                        }
                        window.setAlwaysOnTop(true);
                        Dimension size = menu.getPreferredSize();

                        Point centerPoint = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
                        if (e.getY() > centerPoint.getY())
                            window.setLocation(e.getX(), e.getY() - size.height);
                        else
                            window.setLocation(e.getX(), e.getY());

                        window.setVisible(true);

                        menu.show(((RootPaneContainer) window).getContentPane(), 0, 0);

                        // popup works only for focused windows
                        window.toFront();

                    }
                }
            } catch (Exception ignored) {
            }
        }


        public final JPopupMenu getJPopupMenu() {
            return menu;
        }

        public final void setJPopupMenu(JPopupMenu menu) {
            if (this.menu != null) {
                this.menu.removePopupMenuListener(popupListener);
            }
            this.menu = menu;
            menu.addPopupMenuListener(popupListener);
        }
    }
}
