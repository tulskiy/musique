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

// NOTE - rewritten in Vorbislib 1.1.2

class highlevel_encode_setup {

    ve_setup_data_template setup;    // void *setup
    int set_in_stone;

    float base_setting;                // double base_setting
    float long_setting;                // double long_setting
    float short_setting;            // double short_setting
    float impulse_noisetune;        // double impulse_noisetune

    int managed;
    int bitrate_min;                // long bitrate_min
    int bitrate_av;                    // long bitrate_av
    float bitrate_av_damp;            // double bitrate_av_damp
    int bitrate_max;                // long bitrate_max
    int bitrate_reservoir;            // long bitrate_reservoir
    float bitrate_reservoir_bias;    // double bitrate_reservoir_bias

    int impulse_block_p;
    int noise_normalize_p;

    float stereo_point_setting;        // double stereo_point_setting
    float lowpass_kHz;                // double lowpass_kHz

    float ath_floating_dB;            // double ath_floating_dB
    float ath_absolute_dB;            // double ath_absolute_dB

    float amplitude_track_dBpersec;    // double amplitude_track_dBpersec
    float trigger_setting;            // double trigger_setting

    highlevel_byblocktype[] block;    // highlevel_byblocktype block[4] // padding, impulse, transition, long


    public highlevel_encode_setup() {

        setup = null;

        block = new highlevel_byblocktype[4];
    }
}