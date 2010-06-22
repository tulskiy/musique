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

// NOTE - class updated to VorbisLib 1.1.2

class codec_setup_info {

    // Vorbis supports only short and long blocks, but allows the encoder to choose the sizes

    int[] blocksizes;                     // long blocksizes[2]

    // modes are the primary means of supporting on-the-fly different blocksizes, different channel mappings (LR or M/A),
    // different residue backends, etc.  Each mode consists of a blocksize flag and a mapping (along with the mapping setup

    int modes;
    int maps;
    int floors;
    int residues;
    int books;
    int psys;                                // encode only

    vorbis_info_mode[] mode_param;            // codec_setup *mode_param[64]

// TODO - Abstract classes
// vorbis_info_mapping
// vorbis_info_floor
// vorbis_info_residue

    int[] map_type;                            // int map_type[64] - will always be map0
    vorbis_info_mapping0[] map_param;        // vorbis_info_mapping *map_param[64]

    int[] floor_type;                        // int floor_type[64] - will always be floor1
    vorbis_info_floor1[] floor_param;        // vorbis_info_floor *floor_param[64]

    int[] residue_type;                        // int residue_type[64] - this guy might change from 0,1,2 for functions
    vorbis_info_residue0[] residue_param;    // vorbis_info_residue *residue_param[64] - data will always fit into res0

    static_codebook[] book_param;            // static_codebook *book_param[256]
    codebook[] fullbooks;                    // codebook *fullbooks

    vorbis_info_psy[] psy_param;            // encode only // psy_param[4]
    vorbis_info_psy_global psy_g_param;

    bitrate_manager_info bi;
    highlevel_encode_setup hi;                // used only by vorbisenc.c - Redundant

    int halfrate_flag;                        // painless downsample for decode


    public codec_setup_info() {

        blocksizes = new int[2];

        mode_param = new vorbis_info_mode[64];
        map_type = new int[64];
        map_param = new vorbis_info_mapping0[64];

        floor_type = new int[64];
        floor_param = new vorbis_info_floor1[64];

        residue_type = new int[64];
        residue_param = new vorbis_info_residue0[64];

        book_param = new static_codebook[256];

        psy_param = new vorbis_info_psy[4];

        bi = new bitrate_manager_info();
        hi = new highlevel_encode_setup();
    }
}