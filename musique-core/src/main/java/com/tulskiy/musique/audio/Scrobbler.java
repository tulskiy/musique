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
import com.tulskiy.musique.playlist.TrackData;
import com.tulskiy.musique.system.Application;
import com.tulskiy.musique.system.configuration.Configuration;
import com.tulskiy.musique.util.AudioMath;
import com.tulskiy.musique.util.Util;
import de.umass.lastfm.scrobble.ResponseStatus;
import de.umass.lastfm.scrobble.Source;
import de.umass.lastfm.scrobble.SubmissionData;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;

import static de.umass.lastfm.scrobble.Scrobbler.newScrobbler;

/**
 * Author: Denis Tulskiy
 * Date: Sep 3, 2010
 */
public class Scrobbler {
    private static final String CLIENT_ID = "mqe";
    private static final String CLIENT_VERSION = "1.0";

    private Logger logger = Logger.getLogger(getClass().getName());
    private de.umass.lastfm.scrobble.Scrobbler scrobbler;
    private Application app = Application.getInstance();
    private Configuration config = app.getConfiguration();
    private boolean authorized;
    private Player player;

    private SubmissionData nowPlaying;
    private int nowPlayingLength;

    private final Queue<SubmissionData> submitQueue = new LinkedList<SubmissionData>();

    public void start() {
        Thread submitThread = new Thread(new SubmitSender());
        submitThread.start();

        authorized = false;
        config.addPropertyChangeListener("lastfm.user", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                authorized = false;
            }
        });
        config.addPropertyChangeListener("lastfm.password", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                authorized = false;
            }
        });

        player = app.getPlayer();
        player.addListener(new PlayerListener() {
            @Override
            public void onEvent(PlayerEvent e) {
                if (config.getBoolean("lastfm.enabled", false)) {
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
    	TrackData trackData = track.getTrackData();
        String artist = trackData.getArtist();
        String title = trackData.getTitle();
        String album = trackData.getAlbum();
        long start = System.currentTimeMillis() / 1000;
        int trackNumber = -1;
        try {
            trackNumber = Integer.valueOf(trackData.getTrackNumber());
        } catch (NumberFormatException ignored) {
        }

        nowPlayingLength = (int) (AudioMath.samplesToMillis(trackData.getTotalSamples(), trackData.getSampleRate()) / 1000);

        if (Util.isEmpty(artist) || Util.isEmpty(title) || nowPlayingLength < 30) {
            // do not submit this
            nowPlaying = null;
        } else {
            nowPlaying = new SubmissionData(artist, title, album, nowPlayingLength,
                    trackNumber, Source.USER, start);
        }
    }

    private void auth() {
        String user = config.getString("lastfm.user", null);
        String password = config.getString("lastfm.password", null);
        if (Util.isEmpty(user) || Util.isEmpty(password)) {
            authorized = false;
            scrobbler = null;
        } else {
            try {
                logger.fine("Authorizing user: " + user);
                scrobbler = newScrobbler(CLIENT_ID, CLIENT_VERSION, user);
                ResponseStatus status = scrobbler.handshake(password);
                authorized = status.ok();
                if (!authorized) {
                    switch (status.getStatus()) {
                        case ResponseStatus.BADAUTH:
                        case ResponseStatus.BANNED:
                            config.setBoolean("lastfm.enabled", false);
                    }
                    logger.warning("Scrobbler handshake returned error: " + status.getMessage());
                }
            } catch (IOException e) {
                e.printStackTrace();
                authorized = false;
            }
        }
    }

    class SubmitSender implements Runnable {
        private int MAX_WAIT_TIME = 7200000;
        private int MIN_WAIT_TIME = 60000;
        private int waitTime = MIN_WAIT_TIME;

        @Override
        public void run() {
            while (true) {
                try {
                    SubmissionData data;
                    synchronized (submitQueue) {
                        while (!config.getBoolean("lastfm.enabled", false) ||
                               submitQueue.isEmpty()) {
                            submitQueue.wait();
                        }
                        data = submitQueue.peek();
                    }

                    if (!authorized) {
                        auth();
                        if (!authorized) {
                            Thread.sleep(waitTime);
                            waitTime = Math.min(waitTime * 2, MAX_WAIT_TIME);
                            continue;
                        }
                    }
                    logger.fine("Submitting data: " + data.toString());
                    ResponseStatus status = scrobbler.submit(data);
                    if (status.ok()) {
                        waitTime = MIN_WAIT_TIME;
                        synchronized (submitQueue) {
                            submitQueue.poll();
                        }
                    } else {
                        switch (status.getStatus()) {
                            case ResponseStatus.BADSESSION:
                            case ResponseStatus.FAILED:
                                authorized = false;
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
                    authorized = false;
                    try {
                        Thread.sleep(waitTime);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
    }
}
