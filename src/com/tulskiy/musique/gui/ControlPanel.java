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

package com.tulskiy.musique.gui;

import com.tulskiy.musique.audio.player.Player;
import com.tulskiy.musique.audio.player.PlayerEvent;
import com.tulskiy.musique.audio.player.PlayerListener;
import com.tulskiy.musique.audio.player.io.AudioOutput;
import com.tulskiy.musique.playlist.PlaybackOrder;
import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.playlist.formatting.Parser;
import com.tulskiy.musique.playlist.formatting.tokens.Expression;
import com.tulskiy.musique.system.Application;
import com.tulskiy.musique.system.Configuration;
import com.tulskiy.musique.util.Util;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Author: Denis Tulskiy
 * Date: 17.04.2011
 */

public class ControlPanel extends javax.swing.JPanel {
    private Application app = Application.getInstance();
    private Configuration config = app.getConfiguration();
    private Player player = app.getPlayer();
    private AudioOutput output = player.getAudioOutput();
    private Popup popup;
    private JToolTip toolTip;
    private PopupFactory popupFactory = PopupFactory.getSharedInstance();

    private boolean isSeeking = false;
    private boolean progressEnabled = false;
    private Expression statusExpression = Parser.parse("$if3($playingTime(), '0:00')[/%length%]");
    private MouseAdapter progressMouseListener;

    /**
     * Creates new form ControlBar
     */
    public ControlPanel() {
        initComponents();
        initButtonListeners();
        initSliders();
        initPlayerListeners();
        initPlaybackOrder();
        updateUI();
    }

    private void initPlayerListeners() {
        final Timer timer = new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (progressEnabled && player.isPlaying() && !isSeeking) {
                    progressSlider.setValue((int) player.getCurrentSample());
                }
                if (player.isPlaying())
                    updateStatus();
            }
        });
        timer.start();

        player.addListener(new PlayerListener() {
            public void onEvent(PlayerEvent e) {
                pauseButton.setSelected(player.isPaused());
                switch (e.getEventCode()) {
                    case PLAYING_STARTED:
                        timer.start();
                        break;
                    case PAUSED:
                        timer.stop();
                        break;
                    case STOPPED:
                        timer.stop();
                        progressEnabled = false;
                        progressSlider.setValue(progressSlider.getMinimum());
                        statusLabel.setText(null);
                        break;
                    case FILE_OPENED:
                        Track track = player.getTrack();
                        if (track != null) {
                            int max = (int) track.getTrackData().getTotalSamples();
                            if (max == -1) {
                                progressEnabled = false;
                            } else {
                                progressEnabled = true;
                                progressSlider.setMaximum(max);
                            }
                        }
                        progressSlider.setValue((int) player.getCurrentSample());
                        updateStatus();
                        break;
                    case SEEK_FINISHED:
                        isSeeking = false;
                        break;
                }
            }
        });
    }

    private void initPlaybackOrder() {
        playbackOrder.setModel(new DefaultComboBoxModel(PlaybackOrder.Order.values()));

        config.addPropertyChangeListener("player.playbackOrder", true, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                int value = config.getInt(evt.getPropertyName(), 0);
                playbackOrder.setSelectedIndex(value);
            }
        });

        playbackOrder.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                config.setInt("player.playbackOrder", playbackOrder.getSelectedIndex());
            }
        });
    }

    private void updateStatus() {
        statusLabel.setText((String) statusExpression.eval(player.getTrack()));
    }

    private void initSliders() {
        toolTip = progressSlider.createToolTip();

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

        progressMouseListener = new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                if (!progressEnabled)
                    return;
                hideToolTip();
                player.seek(progressSlider.getValue());
            }

            public void mousePressed(MouseEvent e) {
                if (!progressEnabled)
                    return;
                isSeeking = true;
                progressSlider.setValue(getSliderValueForX(progressSlider, e.getX()));
                hideToolTip();
                showToolTip(e);
            }
        };
        progressSlider.addMouseListener(progressMouseListener);

        progressSlider.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (!progressEnabled)
                    return;
                progressSlider.setValue(getSliderValueForX(progressSlider, e.getX()));
            }
        });
    }

    private int getSliderValueForX(JSlider slider, int x) {
        return ((BasicSliderUI) slider.getUI()).valueForXPosition(x);
    }

    private void showToolTip(MouseEvent e) {
        Track s = player.getTrack();
        if (s != null) {
            toolTip.setTipText(Util.samplesToTime(progressSlider.getValue() - progressSlider.getMinimum(), s.getTrackData().getSampleRate(), 1));
            int x = e.getXOnScreen();
            x = Math.max(x, progressSlider.getLocationOnScreen().x);
            x = Math.min(x, progressSlider.getLocationOnScreen().x + progressSlider.getWidth() - toolTip.getWidth());
            popup = popupFactory.getPopup(progressSlider, toolTip, x, progressSlider.getLocationOnScreen().y + 25);
            popup.show();
        }
    }

    private void hideToolTip() {
        if (popup != null) {
            popup.hide();
            popup = null;
        }
    }

    private void initButtonListeners() {
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
        nextRandomButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Track track = player.getPlaybackOrder().nextRandom();
                player.open(track);
            }
        });
    }

    @Override
    public void updateUI() {
        super.updateUI();
        fixSliderWidth();
    }

    private void fixSliderWidth() {
        if (progressSlider != null) {
            boolean windowsLaF = Util.isWindowsLaF();
            progressSlider.setPaintTicks(windowsLaF);
            volumeSlider.setPaintTicks(windowsLaF);

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    for (MouseListener ml : progressSlider.getMouseListeners()) {
                        progressSlider.removeMouseListener(ml);
                    }
                    progressSlider.addMouseListener(progressMouseListener);
                }
            });
        }
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.JToolBar jToolBar1 = new javax.swing.JToolBar();
        stopButton = new javax.swing.JButton();
        playButton = new javax.swing.JButton();
        pauseButton = new javax.swing.JToggleButton();
        prevButton = new javax.swing.JButton();
        nextButton = new javax.swing.JButton();
        nextRandomButton = new javax.swing.JButton();
        volumeSlider = new javax.swing.JSlider();
        progressSlider = new javax.swing.JSlider();
        playbackOrder = new javax.swing.JComboBox();
        statusLabel = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, java.awt.Color.gray), javax.swing.BorderFactory.createEmptyBorder(0, 5, 0, 5)));
        setFocusable(false);
        setPreferredSize(new java.awt.Dimension(669, 32));

        jToolBar1.setFloatable(false);
        jToolBar1.setForeground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        jToolBar1.setRollover(true);
        jToolBar1.setBorderPainted(false);
        jToolBar1.setFocusable(false);

        stopButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/tulskiy/musique/images/stop.png"))); // NOI18N
        stopButton.setFocusable(false);
        stopButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        stopButton.setMargin(new java.awt.Insets(2, 3, 2, 3));
        stopButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(stopButton);

        playButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/tulskiy/musique/images/play.png"))); // NOI18N
        playButton.setFocusable(false);
        playButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        playButton.setMargin(new java.awt.Insets(2, 3, 2, 3));
        playButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(playButton);

        pauseButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/tulskiy/musique/images/pause.png"))); // NOI18N
        pauseButton.setFocusable(false);
        pauseButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        pauseButton.setMargin(new java.awt.Insets(2, 3, 2, 3));
        pauseButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(pauseButton);

        prevButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/tulskiy/musique/images/prev.png"))); // NOI18N
        prevButton.setFocusable(false);
        prevButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        prevButton.setMargin(new java.awt.Insets(2, 3, 2, 3));
        prevButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(prevButton);

        nextButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/tulskiy/musique/images/next.png"))); // NOI18N
        nextButton.setFocusable(false);
        nextButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        nextButton.setMargin(new java.awt.Insets(2, 3, 2, 3));
        nextButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(nextButton);

        nextRandomButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/tulskiy/musique/images/next-random.png"))); // NOI18N
        nextRandomButton.setFocusable(false);
        nextRandomButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        nextRandomButton.setIconTextGap(0);
        nextRandomButton.setMargin(new java.awt.Insets(2, 3, 2, 3));
        nextRandomButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(nextRandomButton);

        volumeSlider.setValue((int) (output.getVolume() * 100));
        volumeSlider.setFocusable(false);

        progressSlider.setValue(0);
        progressSlider.setFocusable(false);

        playbackOrder.setMaximumRowCount(10);
        playbackOrder.setFocusable(false);
        playbackOrder.setMinimumSize(new java.awt.Dimension(23, 25));
        playbackOrder.setPreferredSize(new java.awt.Dimension(28, 18));

        statusLabel.setFont(statusLabel.getFont().deriveFont(statusLabel.getFont().getStyle() | java.awt.Font.BOLD));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(4, 4, 4)
                                .addComponent(volumeSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(progressSlider, javax.swing.GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE)
                                .addGap(9, 9, 9)
                                .addComponent(statusLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(playbackOrder, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(volumeSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(progressSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(statusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(playbackOrder, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    javax.swing.JButton nextButton;
    javax.swing.JButton nextRandomButton;
    javax.swing.JToggleButton pauseButton;
    javax.swing.JButton playButton;
    javax.swing.JComboBox playbackOrder;
    javax.swing.JButton prevButton;
    javax.swing.JSlider progressSlider;
    javax.swing.JLabel statusLabel;
    javax.swing.JButton stopButton;
    javax.swing.JSlider volumeSlider;
    // End of variables declaration//GEN-END:variables

}
