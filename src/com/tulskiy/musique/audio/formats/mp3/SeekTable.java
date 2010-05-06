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

class SeekTable {
    private SeekPoint[] points;
    private int pointsCount;

    public SeekTable(int totalPoints) {
        points = new SeekPoint[totalPoints];
        pointsCount = 0;
    }

    public int getPointsCount() {
        return pointsCount;
    }

    public void addSeekPoint(long sampleNumber, long byteOffset) {
        points[pointsCount++] = new SeekPoint(sampleNumber, byteOffset);
    }

    public SeekPoint getHigher(long targetSample) {
        if (targetSample < 0)
            targetSample = 0;
        for (int i = 0; i < pointsCount; i++) {
            if (points[i].sampleNumber > targetSample)
                return points[i - 1];
            else if (points[i].sampleNumber == targetSample)
                return points[i];
        }
        return points[pointsCount - 1];
    }

    public class SeekPoint {
        public long sampleNumber;
        public long byteOffset;

        SeekPoint(long sampleNumber, long byteOffset) {
            this.sampleNumber = sampleNumber;
            this.byteOffset = byteOffset;
        }
    }
}
