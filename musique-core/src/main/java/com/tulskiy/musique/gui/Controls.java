package com.tulskiy.musique.gui;

import com.tulskiy.musique.audio.player.Player;
import com.tulskiy.musique.audio.player.PlayerEvent;
import com.tulskiy.musique.audio.player.PlayerListener;
import com.tulskiy.musique.audio.player.io.AudioOutput;
import com.tulskiy.musique.images.Images;
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
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * User: tulskiy
 * Date: 6/26/14
 */
public class Controls {
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

    private JButton prev;

    private JButton playPause;
    private JButton next;
    private JSlider volume;
    private JSlider progress;
    private JComboBox<PlaybackOrder.Order> mode;
    private JPanel panel;
    private JLabel time;
    private JPanel buttons;

    /**
     * Creates new form ControlBar
     */
    public Controls() {
        initButtonListeners();
        initSliders();
        initPlayerListeners();
        initPlaybackOrder();
        panel.updateUI();
        buttons.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
    }

    private void initPlayerListeners() {
        final Timer timer = new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (progressEnabled && player.isPlaying() && !isSeeking) {
                    progress.setValue((int) player.getCurrentSample());
                }
                if (player.isPlaying())
                    updateStatus();
            }
        });
        timer.start();

        player.addListener(new PlayerListener() {
            public void onEvent(PlayerEvent e) {
                switch (e.getEventCode()) {
                    case PLAYING_STARTED:
                        playPause.setIcon(Images.loadIcon("pause.png"));
                        timer.start();
                        break;
                    case PAUSED:
                        playPause.setIcon(Images.loadIcon("play.png"));
                        timer.stop();
                        break;
                    case STOPPED:
                        timer.stop();
                        progressEnabled = false;
                        progress.setValue(progress.getMinimum());
                        time.setText(null);
                        break;
                    case FILE_OPENED:
                        Track track = player.getTrack();
                        if (track != null) {
                            int max = (int) track.getTrackData().getTotalSamples();
                            if (max == -1) {
                                progressEnabled = false;
                            } else {
                                progressEnabled = true;
                                progress.setMaximum(max);
                            }
                        }
                        progress.setValue((int) player.getCurrentSample());
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
        mode.setModel(new DefaultComboBoxModel<PlaybackOrder.Order>(PlaybackOrder.Order.values()));

        config.addPropertyChangeListener("player.playbackOrder", true, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                int value = config.getInt(evt.getPropertyName(), 0);
                mode.setSelectedIndex(value);
            }
        });

        mode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                config.setInt("player.playbackOrder", mode.getSelectedIndex());
            }
        });
    }

    private void updateStatus() {
        if (player.getTrack() == null)
            System.out.println("wtf");
        time.setText((String) statusExpression.eval(player.getTrack()));
    }

    private void initSliders() {
        toolTip = progress.createToolTip();

        volume.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                float vol = volume.getValue() / 100f;
                output.setVolume(vol);
                config.setFloat("player.volume", vol);
            }
        });

        volume.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                volume.setValue(getSliderValueForX(volume, e.getX()));
            }
        });

        volume.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                int value = volume.getValue();
                if (e.getWheelRotation() > 0)
                    value -= 5;
                else
                    value += 5;
                volume.setValue(value);
            }
        });

        progress.addMouseMotionListener(new MouseMotionAdapter() {
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
                player.seek(progress.getValue());
            }

            public void mousePressed(MouseEvent e) {
                if (!progressEnabled)
                    return;
                isSeeking = true;
                progress.setValue(getSliderValueForX(progress, e.getX()));
                hideToolTip();
                showToolTip(e);
            }
        };
        progress.addMouseListener(progressMouseListener);

        progress.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (!progressEnabled)
                    return;
                progress.setValue(getSliderValueForX(progress, e.getX()));
            }
        });
    }

    private int getSliderValueForX(JSlider slider, int x) {
        return ((BasicSliderUI) slider.getUI()).valueForXPosition(x);
    }

    private void showToolTip(MouseEvent e) {
        Track s = player.getTrack();
        if (s != null) {
            toolTip.setTipText(Util.samplesToTime(progress.getValue() - progress.getMinimum(), s.getTrackData().getSampleRate(), 1));
            int x = e.getXOnScreen();
            x = Math.max(x, progress.getLocationOnScreen().x);
            x = Math.min(x, progress.getLocationOnScreen().x + progress.getWidth() - toolTip.getWidth());
            popup = popupFactory.getPopup(progress, toolTip, x, progress.getLocationOnScreen().y + 25);
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
        prev.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                player.prev();
            }
        });
        playPause.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (player.getTrack() == null)
                    player.play();
                else
                    player.pause();
            }
        });
        next.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                player.next();
            }
        });
    }

    private void fixSliderWidth() {
        if (progress != null) {
            boolean windowsLaF = Util.isWindowsLaF();
            progress.setPaintTicks(windowsLaF);
            volume.setPaintTicks(windowsLaF);

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    for (MouseListener ml : progress.getMouseListeners()) {
                        progress.removeMouseListener(ml);
                    }
                    progress.addMouseListener(progressMouseListener);
                }
            });
        }
    }

    public JPanel getPanel() {
        return panel;
    }

    private void createUIComponents() {
        panel = new JPanel() {
            @Override
            public void updateUI() {
                super.updateUI();
                fixSliderWidth();
            }
        };
    }
}
