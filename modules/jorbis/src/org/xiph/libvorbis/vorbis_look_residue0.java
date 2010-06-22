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

package org.xiph.libvorbis;

class vorbis_look_residue0 {

    vorbis_info_residue0 info;

    int parts;
    int stages;

    codebook[] fullbooks;        // *fullbooks;
    codebook phrasebook;        // *phrasebook;
    codebook[][] partbooks;        // ***partbooks;

    int partvals;
    int[][] decodemap;        // **decodemap;

    int postbits;
    int phrasebits;
    int frames;

    // #ifdef TRAIN_RES
    // int        train_seq;
    // long      *training_data[8][64];
    // float      training_max[8][64];
    // float      training_min[8][64];
    // float     tmin;
    // float     tmax;
    // #endif


    public vorbis_look_residue0() {

    }
}
