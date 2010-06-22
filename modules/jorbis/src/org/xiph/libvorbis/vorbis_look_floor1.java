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

class vorbis_look_floor1 {

    int[] sorted_index;    // int sorted_index[VIF_POSIT+2];
    int[] forward_index;    // int forward_index[VIF_POSIT+2];
    int[] reverse_index;    // int reverse_index[VIF_POSIT+2];

    int[] hineighbor;    // int hineighbor[VIF_POSIT];
    int[] loneighbor;    // int loneighbor[VIF_POSIT];
    int posts;

    int n;
    int quant_q;
    vorbis_info_floor1 vi;

    int phrasebits;
    int postbits;
    int frames;


    public vorbis_look_floor1() {

        sorted_index = new int[VIF_POSIT + 2];
        forward_index = new int[VIF_POSIT + 2];
        reverse_index = new int[VIF_POSIT + 2];

        hineighbor = new int[VIF_POSIT];
        loneighbor = new int[VIF_POSIT];
    }
}