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

class private_state {

    envelope_lookup ve;            // envelope lookup - *ve;
    int[] window;                // window[2];

    mdct_lookup[][] transform;        // vorbis_look_transform **transform[2];// block, type
    drft_lookup[] fft_look;            // fft_look[2];

    int modebits;

// TODO - floor, residue OOP function classes

// flr data & functions will always be floor1

    vorbis_look_floor1[] flr;        // **flr;

// residue data will always be res0

    vorbis_look_residue0[] residue;        // **residue;

    vorbis_look_psy[] psy;            // *psy;
    vorbis_look_psy_global psy_g_look;    // *psy_g_look;

    // local storage, only used on the encoding side.  This way the
    // application does not need to worry about freeing some packets
    // memory and not others; packet storage is always tracked.
    // Cleared next call to a _dsp_ function

    byte[] header;        // unsigned char *header;
    byte[] header1;        // unsigned char *header1;
    byte[] header2;        // unsigned char *header2;

    bitrate_manager_state bms;

    int sample_count;


    public private_state() {

        window = new int[2];

        // backend_state.transform[0] = _ogg_calloc(VI_TRANSFORMB,sizeof(*backend_state.transform[0]));
        // backend_state.transform[1] = _ogg_calloc(VI_TRANSFORMB,sizeof(*backend_state.transform[1]));
        // MDCT is tranform 0
        // backend_state.transform[0][0] = _ogg_calloc(1,sizeof(mdct_lookup));
        // backend_state.transform[1][0] = _ogg_calloc(1,sizeof(mdct_lookup));

        transform = new mdct_lookup[2][2];
        transform[0][0] = new mdct_lookup();
        transform[0][1] = new mdct_lookup();
        transform[1][0] = new mdct_lookup();
        transform[1][1] = new mdct_lookup();

        fft_look = new drft_lookup[2];
        fft_look[0] = new drft_lookup();
        fft_look[1] = new drft_lookup();
    }
}