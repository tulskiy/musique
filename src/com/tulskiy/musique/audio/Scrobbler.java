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

package com.tulskiy.musique.audio;

import com.tulskiy.musique.audio.player.Player;
import com.tulskiy.musique.audio.player.PlayerEvent;
import com.tulskiy.musique.audio.player.PlayerListener;
import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.system.Application;
import com.tulskiy.musique.system.Configuration;
import com.tulskiy.musique.util.AudioMath;
import com.tulskiy.musique.util.Util;
import net.roarsoftware.lastfm.scrobble.ResponseStatus;
import net.roarsoftware.lastfm.scrobble.Source;
import net.roarsoftware.lastfm.scrobble.SubmissionData;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;

import static net.roarsoftware.lastfm.scrobble.Scrobbler.newScrobbler;

/**
 * Author: Denis Tulskiy
 * Date: Sep 3, 2010
 */
public class Scrobbler {
    private Logger logger = Logger.getLogger("musique");
    private net.roarsoftware.lastfm.scrobble.Scrobbler scrobbler;
    private Application app = Application.getInstance();
    private Configuration config = app.getConfiguration();
    private ResponseStatus status;
    private boolean active;
    private Player player;

    private SubmissionData nowPlaying;
    private int nowPlayingLength;

    private final Queue<SubmissionData> submitQueue = new LinkedList<SubmissionData>();
    private Thread submitThread;


    public void start() {
        submitThread = new Thread(new SubmitSender());
        submitThread.start();

        config.addPropertyChangeListener("lastfm.enabled", true, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (submitThread.isAlive() && config.getBoolean(evt.getPropertyName(), false)) {
                    if (!active)
                        authentificate();
                } else {
                    active = false;
                }
            }
        });
        authentificate();

        player = app.getPlayer();
        player.addListener(new PlayerListener() {
            @Override
            public void onEvent(PlayerEvent e) {
                if (active) {
                    SubmissionData data = nowPlaying;
                    switch (e.getEventCode()) {
                        case FILE_OPENED:
                            initNowPlaying(player.getTrack());
                            // no need to break here!
                        case STOPPED:
                            if (data != null)
                                submit(data);
                    }
                }
            }
        });
    }

    private void submit(SubmissionData data) {
        int time = (int) (player.getPlaybackTime() / 1000);
        if (time >= 240 || time >= nowPlayingLength / 2) {
            synchronized (submitQueue) {
                submitQueue.add(data);
                submitQueue.notify();
            }
        }
    }

    private void initNowPlaying(Track track) {
        String artist = track.getArtist();
        String title = track.getTitle();
        String album = track.getAlbum();
        long start = System.currentTimeMillis() / 1000;
        int trackNumber = -1;
        try {
            trackNumber = Integer.valueOf(track.getTrackNumber());
        } catch (NumberFormatException ignored) {
        }

        nowPlayingLength = (int) (AudioMath.samplesToMillis(track.getTotalSamples(), track.getSampleRate()) / 1000);

        if (Util.isEmpty(artist) || Util.isEmpty(title) || nowPlayingLength < 30) {
            // do not submit this
            nowPlaying = null;
        } else {
            nowPlaying = new SubmissionData(artist, title, album, nowPlayingLength,
                    trackNumber, Source.USER, start);
        }
    }

    private void authentificate() {
        String user = config.getString("lastfm.user", null);
        String password = config.getString("lastfm.password", null);
        if (Util.isEmpty(user) || Util.isEmpty(password)) {
            active = false;
            scrobbler = null;
        } else {
            try {
                scrobbler = newScrobbler("tst", "1.0", user);
                status = scrobbler.handshake(password);
                active = status.ok();
                if (!active) {
                    logger.warning("Scrobbler handshake returned error: " + status.getMessage());
                }
            } catch (IOException e) {
                e.printStackTrace();
                active = false;
            }
        }
    }

    class SubmitSender implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    SubmissionData data;
                    synchronized (submitQueue) {
                        while (!active || submitQueue.isEmpty()) {
                            submitQueue.wait();
                        }
                        data = submitQueue.peek();
                    }
                    ResponseStatus status = scrobbler.submit(data);
                    if (status.ok()) {
                        synchronized (submitQueue) {
                            submitQueue.poll();
                        }
                    } else {
                        switch (status.getStatus()) {
                            case ResponseStatus.BADSESSION:
                                authentificate();
                                continue;
                            case ResponseStatus.BANNED:
                                logger.warning("Last.fm says that we're banned :(");
                                config.setBoolean("lastfm.enabled", false);
                                submitQueue.clear();
                                return;
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                    try {
                        Thread.sleep(300000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
    }
}
