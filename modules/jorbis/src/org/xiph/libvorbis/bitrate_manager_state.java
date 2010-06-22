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

import static org.xiph.libvorbis.vorbis_constants.integer_constants.*;

class bitrate_manager_state {

    int managed;

    int avg_reservoir;        // long
    int minmax_reservoir;    // long
    int avg_bitsper;        // long
    int min_bitsper;        // long
    int max_bitsper;        // long

    int short_per_long;        // long
    float avgfloat;            // double

    vorbis_block vb;
    int choice;


    public bitrate_manager_state(vorbis_info vi) {

        codec_setup_info ci = vi.codec_setup;
        bitrate_manager_info bi = ci.bi;

        if (bi.reservoir_bits > 0) {

            int ratesamples = vi.rate;
            int halfsamples = ci.blocksizes[0] >> 1;

            short_per_long = ci.blocksizes[1] / ci.blocksizes[0];
            managed = 1;

            avg_bitsper = new Double(Math.rint(1. * bi.avg_rate * halfsamples / ratesamples)).intValue();
            min_bitsper = new Double(Math.rint(1. * bi.min_rate * halfsamples / ratesamples)).intValue();
            max_bitsper = new Double(Math.rint(1. * bi.max_rate * halfsamples / ratesamples)).intValue();

            avgfloat = PACKETBLOBS / 2;

            // not a necessary fix, but one that leads to a more balanced typical initialization
            {
                int desired_fill = new Float(bi.reservoir_bits * bi.reservoir_bias).intValue();    // long
                minmax_reservoir = desired_fill;
                avg_reservoir = desired_fill;
            }
        }
    }
}