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

package com.tulskiy.musique.audio.formats.ape;

import com.tulskiy.musique.audio.AudioTagWriter;
import com.tulskiy.musique.playlist.Song;

import java.io.IOException;

/**
 * @Author: Denis Tulskiy
 * @Date: Oct 10, 2009
 */
public class APETagWriter extends AudioTagWriter {
    private APETagProcessor tagProcessor = new APETagProcessor();

    @Override
    public void write(Song song) {
        try {
            tagProcessor.writeAPEv2Tag(song);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isFileSupported(String ext) {
        return ext.equalsIgnoreCase("ape") || ext.equalsIgnoreCase("wv");
    }
}
