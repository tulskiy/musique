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

package com.tulskiy.musique.audio.formats.mp3;

import java.util.TreeSet;

class SeekTable {
    private TreeSet<SeekPoint> points = new TreeSet<SeekPoint>();

    SeekTable() {
        points.add(new SeekPoint(0, 0));
    }

    public void add(int frame, long offset) {
        points.add(new SeekPoint(frame, offset));
    }

    public SeekPoint get(int frame) {
        if (frame < 0)
            frame = 0;
        return points.floor(new SeekPoint(frame, 0));
    }

    public class SeekPoint implements Comparable<SeekPoint> {
        public int frame;
        public long offset;

        SeekPoint(int frame, long offset) {
            this.frame = frame;
            this.offset = offset;
        }

        @Override
        public int compareTo(SeekPoint seekPoint) {
            return ((Integer) frame).compareTo(seekPoint.frame);
        }
    }
}
