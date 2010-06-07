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
import com.tulskiy.musique.audio.player.PlayerEvent;
import com.tulskiy.musique.playlist.Song;
import com.tulskiy.musique.system.Application;
import com.tulskiy.musique.util.GlobalTimer;
import com.tulskiy.musique.util.Util;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;
import java.awt.event.*;

/**
 * @Author: Denis Tulskiy
 * @Date: 07.09.2008
 */
public class ControlPanel extends JPanel {
    private Application app = Application.getInstance();
    private JSlider progressSlider = new JSlider();
    private JSlider volumeSlider;
    private JButton prevButton = new JButton();
    private JButton nextButton = new JButton();
    private JButton playButton = new JButton();
    private JButton pauseButton = new JButton();
    private JButton stopButton = new JButton();
    private Player player = app.getPlayer();

    private Popup popup;
    private JToolTip toolTip = progressSlider.createToolTip();
    private PopupFactory popupFactory = PopupFactory.getSharedInstance();

    private boolean isSeeking = false;

    public ControlPanel() {
        setLayout(new BorderLayout());

        progressSlider.setValue(0);
        progressSlider.setFocusable(false);
        for (MouseListener ml : progressSlider.getMouseListeners())
            progressSlider.removeMouseListener(ml);

        stopButton = createButton("resources/images/stop.png");
        prevButton = createButton("resources/images/prev.png");
        playButton = createButton("resources/images/play.png");
        pauseButton = createButton("resources/images/pause.png");
        nextButton = createButton("resources/images/next.png");

        volumeSlider = new JSlider(0, 100);
        volumeSlider.setMaximumSize(new Dimension(100, 30));
        volumeSlider.setPreferredSize(new Dimension(100, 30));
        volumeSlider.setValue((int) (player.getVolume() * 100));
        volumeSlider.setFocusable(false);

        //hack to make volume and progress sliders be on same level
        progressSlider.setMaximumSize(new Dimension(10000, 30));
        progressSlider.setPreferredSize(new Dimension(100, 30));


        Box box = new Box(BoxLayout.X_AXIS);
        box.add(Box.createHorizontalStrut(5));
        box.add(stopButton);
        box.add(playButton);
        box.add(pauseButton);
        box.add(prevButton);
        box.add(nextButton);
        box.add(Box.createHorizontalStrut(10));
        box.add(volumeSlider);
        box.add(Box.createHorizontalStrut(10));
        box.add(progressSlider);
        box.add(Box.createHorizontalStrut(5));

        add(box);

        buildListeners();
    }

    private JButton createButton(String path) {
        JButton b = new JButton();
        Dimension buttonSize = new Dimension(30, 30);
        b.setIcon(new ImageIcon(path));
        b.setName("nextButton");
        b.setFocusable(false);
        String laf = UIManager.getLookAndFeel().getName();
        if (laf.contains("GTK")) {
            b.setBorderPainted(false);
        }

        b.setPreferredSize(buttonSize);

        return b;
    }

    private int getSliderValueForX(JSlider slider, int x) {
        return ((BasicSliderUI) slider.getUI()).valueForXPosition(x);
    }

    private void buildListeners() {
        volumeSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                player.setVolume(volumeSlider.getValue() / 100f);
            }
        });

        volumeSlider.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                volumeSlider.setValue(getSliderValueForX(volumeSlider, e.getX()));
            }
        });

        progressSlider.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                hideToolTip();
                showToolTip(e);
            }
        });

        progressSlider.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                hideToolTip();
                player.seek(progressSlider.getValue());
                isSeeking = false;
            }

            public void mousePressed(MouseEvent e) {
                isSeeking = true;
                progressSlider.setValue(getSliderValueForX(progressSlider, e.getX()));
                hideToolTip();
                showToolTip(e);
            }
        });

        progressSlider.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                progressSlider.setValue(getSliderValueForX(progressSlider, e.getX()));
            }
        });

        prevButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                player.prev();
            }
        });
        playButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                player.play();
            }
        });

        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                player.stop();
            }
        });

        pauseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                player.pause();
            }
        });
        nextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                player.next();
            }
        });

        player.addListener(new com.tulskiy.musique.audio.player.PlayerListener() {
            public void onEvent(PlayerEvent e) {
                switch (e.getEventCode()) {
                    case STOPPED:
                        progressSlider.setValue(progressSlider.getMinimum());
                        break;
                    case FILE_OPENED:
                        Song song = player.getSong();
                        if (song != null)
                            progressSlider.setMaximum((int) song.getTotalSamples());
                        progressSlider.setValue((int) player.getCurrentSample());
                        break;
                }
            }
        });

        GlobalTimer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (player.isPlaying() && !isSeeking) {
                    progressSlider.setValue((int) player.getCurrentSample());
                }
            }
        });
    }

    private void showToolTip(MouseEvent e) {
        Song s = player.getSong();
        if (s != null) {
            toolTip.setTipText(Util.samplesToTime(progressSlider.getValue() - progressSlider.getMinimum(), s.getSamplerate(), 1));
            int x = e.getXOnScreen();
            x = Math.max(x, progressSlider.getLocationOnScreen().x);
            x = Math.min(x, progressSlider.getLocationOnScreen().x + progressSlider.getWidth() - toolTip.getWidth());
            popup = popupFactory.getPopup(progressSlider, toolTip, x, progressSlider.getLocationOnScreen().y + 25);
            popup.show();
        }
    }

    private void hideToolTip() {
        if (popup != null)
            popup.hide();
    }
}