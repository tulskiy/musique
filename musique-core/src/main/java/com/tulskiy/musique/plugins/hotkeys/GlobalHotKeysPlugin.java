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

package com.tulskiy.musique.plugins.hotkeys;

import com.tulskiy.keymaster.common.HotKey;
import com.tulskiy.keymaster.common.HotKeyListener;
import com.tulskiy.keymaster.common.Provider;
import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.spi.Plugin;
import com.tulskiy.musique.util.AudioMath;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Author: Denis Tulskiy
 * Date: 7/31/11
 */
public class GlobalHotKeysPlugin extends Plugin {
    private static final int SEEK_DISTANCE = 3000;

    @SuppressWarnings({"UnusedDeclaration"})
    static enum HotKeyEvent {
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

    private Map<KeyStroke, HotKeyEvent> activeHotKeys = new LinkedHashMap<KeyStroke, HotKeyEvent>();

    @Override
    public boolean init() {
        provider = Provider.getCurrentProvider(true);
        if (provider == null)
            return false;

        config.addPropertyChangeListener("hotkeys.list", true, new ConfigListener());

        return true;
    }

    @Override
    public void shutdown() {
        provider.reset();
        provider.stop();
    }

    @Override
    public Description getDescription() {
        return new Description("Global HotKeys Plugin", "Denis Tulskiy");
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public void configure(Window parent) {
        provider.reset();
        GlobalHotKeysSettings settings = new GlobalHotKeysSettings(parent);
        settings.init(activeHotKeys);
        settings.setVisible(true);
    }

    private class ConfigListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            provider.reset();
            parseConfig();

            for (Map.Entry<KeyStroke, HotKeyEvent> entry : activeHotKeys.entrySet()) {
                provider.register(entry.getKey(), entry.getValue().getAction());
            }
        }

        private void parseConfig() {
            ArrayList<String> hotKeys = config.getList("hotkeys.list", new ArrayList<String>());
            activeHotKeys.clear();

            for (String hotKey : hotKeys) {
                try {
                    String[] tokens = hotKey.split(": ");

                    HotKeyEvent event = HotKeyEvent.valueOf(tokens[0]);
                    KeyStroke keyStroke = KeyStroke.getKeyStroke(tokens[1]);

                    activeHotKeys.put(keyStroke, event);
                } catch (IllegalArgumentException e) {
                    logger.warning("Could not parse hotkey for string: " + hotKey);
                }
            }
        }
    }
}
