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

package com.tulskiy.musique.audio;

import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.playlist.formatting.Parser;
import com.tulskiy.musique.playlist.formatting.tokens.Expression;
import com.tulskiy.musique.system.Application;
import com.tulskiy.musique.system.Codecs;
import com.tulskiy.musique.system.Configuration;
import com.tulskiy.musique.system.TrackIO;
import com.tulskiy.musique.util.AudioMath;

import javax.swing.*;
import java.io.File;
import java.util.List;
import java.util.logging.Logger;

/**
 * Author: Denis Tulskiy
 * Date: Jul 27, 2010
 */
public class Converter {
    private Logger logger = Logger.getLogger(getClass().getName());
    private Configuration config = Application.getInstance().getConfiguration();
    private Expression fileNameFormat;
    private Encoder encoder;
    private Decoder decoder;
    private Track track;
    private boolean stop;
    private long cueTotalBytes;
    private long currentByte;
    private long currentSample;
    private long totalSamples;
    private long startTime;
    private long elapsed;
    private double speed;
    private double estimated;
    private File output;

    public Converter() {
        String fileName = config.getString("converter.fileNameFormat", "%fileName%");
        fileNameFormat = Parser.parse(fileName);
    }

    public void convert(List<Track> tracks) {
        stop = false;
        totalSamples = 0;
        currentSample = 0;
        startTime = System.currentTimeMillis();
        boolean merge = config.getBoolean("converter.merge", false);
        for (Track track : tracks) {
            totalSamples += track.getTrackData().getTotalSamples();
        }
        byte[] buf = new byte[65536];
        for (Track track : tracks) {
            if (stop)
                break;
            this.track = track;
            if (!openDecoder()) {
                currentSample += track.getTrackData().getTotalSamples();
                continue;
            }
            if (!merge || encoder == null) {
                if (!openEncoder()) {
                    currentSample += track.getTrackData().getTotalSamples();
                    continue;
                }
            }
            int len;
            boolean cueFinished = false;
            while (!stop && !cueFinished) {
                len = decoder.decode(buf);

                if (len == -1)
                    break;

                if (track.getTrackData().isCue()) {
                    if (cueTotalBytes <= currentByte + len) {
                        len = (int) (cueTotalBytes - currentByte);
                        cueFinished = true;
                    }
                }

                currentByte += len;
                currentSample += AudioMath.bytesToSamples(len, decoder.getAudioFormat().getFrameSize());
                updateStats();
                if (len != 0)
                    encoder.encode(buf, len);
            }

            if (!merge) {
                Track newTrack = track.copy();
                newTrack.getTrackData().setLocation(output.toURI().toString());
                encoder.close();
                encoder = null;

                TrackIO.write(newTrack);
            }
            decoder.close();
            decoder = null;
        }

        if (merge) {
            encoder.close();
            encoder = null;
        }
    }

    private void updateStats() {
        elapsed = System.currentTimeMillis() - startTime;
        speed = AudioMath.samplesToMillis(currentSample,
                (int) decoder.getAudioFormat().getSampleRate()) /
                elapsed;
        estimated = totalSamples / speed;
    }

    public double getEstimated() {
        return estimated;
    }

    public long getElapsed() {
        return elapsed;
    }

    public double getSpeed() {
        return speed;
    }

    public Track getTrack() {
        return track;
    }

    public long getCurrentSample() {
        return currentSample;
    }

    public long getTotalSamples() {
        return totalSamples;
    }

    public void stop() {
        stop = true;
    }

    public boolean openDecoder() {
        decoder = Codecs.getNewDecoder(track);

        if (decoder == null || !decoder.open(track)) {
            logger.info("Couldn't initialize decoder for track: " + track.getTrackData().getLocation());
            return false;
        }

        cueTotalBytes = 0;
        currentByte = 0;
        if (track.getTrackData().isCue()) {
            decoder.seekSample(track.getTrackData().getStartPosition());
            cueTotalBytes = AudioMath.samplesToBytes(track.getTrackData().getTotalSamples(), decoder.getAudioFormat().getFrameSize());
        }
        return true;
    }

    public boolean openEncoder() {
        if (decoder == null) {
            logger.severe("Need to initialize decoder before encoder!");
        }

        logger.info("Converting track: " + track.getTrackData().getLocation());
        File parent = null;
        if (config.getBoolean("converter.saveToSourceFolder", true)) {
            if (track.getTrackData().isFile()) {
                parent = track.getTrackData().getFile().getParentFile();
            }
        } else {
            String path = config.getString("converter.path", "");
            parent = new File(path);
        }

        if (parent == null || !parent.isDirectory()) {
            logger.warning("Don't know where to save track: " + track.getTrackData().getLocation());
            return false;
        }

        if (!parent.canWrite()) {
            logger.warning("Cannot write to folder: " + parent);
            return false;
        }
        //noinspection ResultOfMethodCallIgnored
        parent.mkdirs();

        String format = config.getString("converter.encoder", "wav");
        String fileName = String.valueOf(fileNameFormat.eval(track)) +
                "." + format;
        output = new File(parent, fileName);

        if (output.exists()) {
            String action = config.getString("converter.actionWhenExists", "Ask");
            if (action.equals("Ask")) {
                int ret = JOptionPane.showConfirmDialog(null,
                        "File " + output.getAbsolutePath() + " exists, overwrite?",
                        "File exists, overwrite",
                        JOptionPane.YES_NO_CANCEL_OPTION);

                if (ret == JOptionPane.YES_OPTION) {
                    //noinspection ResultOfMethodCallIgnored
                    output.delete();
                } else if (ret == JOptionPane.NO_OPTION) {
                    return false;
                } else if (ret == JOptionPane.CANCEL_OPTION) {
                    stop();
                    return false;
                }
            } else if (action.equals("Overwrite")) {
                //noinspection ResultOfMethodCallIgnored
                output.delete();
            } else if (action.equals("Skip")) {
                return false;
            }
        }
        logger.info("Saving track to file: " + output.getAbsolutePath());
        encoder = Codecs.getEncoder(format);
        if (!encoder.open(output, decoder.getAudioFormat(), config)) {
            logger.warning("Couldn't initialize encoder for track: " + track.getTrackData().getLocation());
            return false;
        }
        return true;
    }

    public File getOutput() {
        return output;
    }
}
