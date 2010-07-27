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

import com.tulskiy.musique.audio.player.AudioOutput;
import com.tulskiy.musique.audio.player.Player;
import com.tulskiy.musique.audio.player.PlayerEvent;
import com.tulskiy.musique.audio.player.PlayerListener;
import com.tulskiy.musique.images.Images;
import com.tulskiy.musique.playlist.Track;
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
    private JSlider progressSlider;
    private JSlider volumeSlider;
    private JButton prevButton = new JButton();
    private JButton nextButton = new JButton();
    private JButton playButton = new JButton();
    private JButton pauseButton = new JButton();
    private JButton stopButton = new JButton();
    private Player player = app.getPlayer();
    private AudioOutput output = player.getAudioOutput();

    private Popup popup;
    private JToolTip toolTip;
    private PopupFactory popupFactory = PopupFactory.getSharedInstance();

    private boolean isSeeking = false;
    private boolean progressEnabled = false;

    public ControlPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.gray));

        progressSlider = new JSlider();
        progressSlider.setValue(0);
        progressSlider.setFocusable(false);
        toolTip = progressSlider.createToolTip();

        for (MouseListener ml : progressSlider.getMouseListeners())
            progressSlider.removeMouseListener(ml);

        stopButton = createButton("stop.png");
        prevButton = createButton("prev.png");
        playButton = createButton("play.png");
        pauseButton = createButton("pause.png");
        nextButton = createButton("next.png");

        volumeSlider = new JSlider(0, 100);
        volumeSlider.setMaximumSize(new Dimension(100, 30));
        volumeSlider.setPreferredSize(new Dimension(100, 30));
        volumeSlider.setValue((int) (output.getVolume() * 100));
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

    @Override
    public void updateUI() {
        super.updateUI();

        JButton buttons[] = new JButton[]{
                stopButton, prevButton, playButton, pauseButton, nextButton
        };
        if (UIManager.getLookAndFeel().getName().contains("GTK")) {
            for (JButton b : buttons) {
                if (b != null)
                    b.setBorderPainted(false);
            }
        } else {
            for (JButton b : buttons) {
                if (b != null)
                    b.setBorderPainted(true);
            }
        }
    }

    private JButton createButton(String icon) {
        JButton b = new JButton();
        Dimension buttonSize = new Dimension(30, 30);
        b.setIcon(Images.loadIcon(icon));
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
                output.setVolume(volumeSlider.getValue() / 100f);
            }
        });

        volumeSlider.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                volumeSlider.setValue(getSliderValueForX(volumeSlider, e.getX()));
            }
        });

        volumeSlider.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                int value = volumeSlider.getValue();
                if (e.getWheelRotation() > 0)
                    value -= 5;
                else
                    value += 5;
                volumeSlider.setValue(value);
            }
        });

        progressSlider.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (!progressEnabled)
                    return;
                hideToolTip();
                showToolTip(e);
            }
        });

        progressSlider.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                if (!progressEnabled)
                    return;
                hideToolTip();
                player.seek(progressSlider.getValue());
                isSeeking = false;
            }

            public void mousePressed(MouseEvent e) {
                if (!progressEnabled)
                    return;
                isSeeking = true;
                progressSlider.setValue(getSliderValueForX(progressSlider, e.getX()));
                hideToolTip();
                showToolTip(e);
            }
        });

        progressSlider.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (!progressEnabled)
                    return;
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

        player.addListener(new PlayerListener() {
            public void onEvent(PlayerEvent e) {
                switch (e.getEventCode()) {
                    case STOPPED:
                        progressEnabled = false;
                        progressSlider.setValue(progressSlider.getMinimum());
                        break;
                    case FILE_OPENED:
                        Track track = player.getTrack();
                        if (track != null) {
                            int max = (int) track.getTotalSamples();
                            if (max == -1) {
                                progressEnabled = false;
                            } else {
                                progressEnabled = true;
                                progressSlider.setMaximum(max);
                            }
                        }
                        progressSlider.setValue((int) player.getCurrentSample());
                        break;
                }
            }
        });

        GlobalTimer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (progressEnabled && player.isPlaying() && !isSeeking) {
                    progressSlider.setValue((int) player.getCurrentSample());
                }
            }
        });
    }

    private void showToolTip(MouseEvent e) {
        Track s = player.getTrack();
        if (s != null) {
            toolTip.setTipText(Util.samplesToTime(progressSlider.getValue() - progressSlider.getMinimum(), s.getSampleRate(), 1));
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