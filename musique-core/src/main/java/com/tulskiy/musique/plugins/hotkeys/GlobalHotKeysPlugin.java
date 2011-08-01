package com.tulskiy.musique.plugins.hotkeys;

import com.tulskiy.keymaster.common.HotKey;
import com.tulskiy.keymaster.common.HotKeyListener;
import com.tulskiy.keymaster.common.Provider;
import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.spi.Plugin;
import com.tulskiy.musique.util.AudioMath;

import javax.swing.*;
import java.util.ArrayList;

/**
 * Author: Denis Tulskiy
 * Date: 7/31/11
 */
public class GlobalHotKeysPlugin extends Plugin {
    private static final int SEEK_DISTANCE = 3000;

    enum HotKeyEvent {
        PLAYER_PLAY_PAUSE(new HotKeyListener() {
            @Override
            public void onHotKey(HotKey hotKey) {
                if (player.isStopped())
                    player.play();
                else
                    player.pause();
            }
        }),
        PLAYER_STOP(new HotKeyListener() {
            @Override
            public void onHotKey(HotKey hotKey) {
                player.stop();
            }
        }),
        PLAYER_NEXT_TRACK(new HotKeyListener() {
            @Override
            public void onHotKey(HotKey hotKey) {
                player.next();
            }
        }),
        PLAYER_PREV_TRACK(new HotKeyListener() {
            @Override
            public void onHotKey(HotKey hotKey) {
                player.prev();
            }
        }),
        NEXT_RANDOM(new HotKeyListener() {
            @Override
            public void onHotKey(HotKey hotKey) {
                Track track = player.getPlaybackOrder().nextRandom();
                player.open(track);
            }
        }),
        SEEK_FORWARD(new HotKeyListener() {
            @Override
            public void onHotKey(HotKey hotKey) {
                Track track = player.getTrack();
                if (track != null) {
                    long sample = player.getCurrentSample();

                    sample += AudioMath.millisToSamples(SEEK_DISTANCE, track.getTrackData().getSampleRate());
                    if (track.getTrackData().getTotalSamples() >= sample) {
                        player.seek(sample);
                    }
                }
            }
        }),
        SEEK_BACKWARD(new HotKeyListener() {
            @Override
            public void onHotKey(HotKey hotKey) {
                Track track = player.getTrack();
                if (track != null) {
                    long sample = player.getCurrentSample();

                    sample -= AudioMath.millisToSamples(SEEK_DISTANCE, track.getTrackData().getSampleRate());
                    if (sample < 0)
                        sample = 0;
                    player.seek(sample);
                }
            }
        });

        private HotKeyListener action;

        HotKeyEvent(HotKeyListener action) {
            this.action = action;
        }

        public HotKeyListener getAction() {
            return action;
        }
    }

    private Provider provider;

    @Override
    public boolean init() {
        provider = Provider.getCurrentProvider(true);
        if (provider == null)
            return false;

        ArrayList<String> hotKeys = config.getList("hotkeys.list", new ArrayList<String>());

        for (String hotKey : hotKeys) {
            String[] tokens = hotKey.split(": ");

            HotKeyEvent event = HotKeyEvent.valueOf(tokens[0]);
            KeyStroke keyStroke = KeyStroke.getKeyStroke(tokens[1]);

            provider.register(keyStroke, event.getAction());
        }

        return true;
    }

    @Override
    public void shutdown() {
        provider.reset();
        provider.stop();
    }

    @Override
    public Description getDescription() {
        return new Description("Global hotkeys", "1.0");
    }
}
