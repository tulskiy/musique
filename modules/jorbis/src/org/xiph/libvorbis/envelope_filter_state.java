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

class envelope_filter_state {

    float[] ampbuf;
    int ampptr;

    float[] nearDC;
    float nearDC_acc;
    float nearDC_partialacc;
    int nearptr;


    public envelope_filter_state(float[] _ampbuf, int _ampptr, float[] _nearDC, float _nearDC_acc, float _nearDC_partialacc, int _nearptr) {

        ampbuf = new float[VE_AMP];
        System.arraycopy(_ampbuf, 0, ampbuf, 0, _ampbuf.length);

        ampptr = _ampptr;

        nearDC = new float[VE_NEARDC];
        System.arraycopy(_nearDC, 0, nearDC, 0, _nearDC.length);

        nearDC_acc = _nearDC_acc;
        nearDC_partialacc = _nearDC_partialacc;
        nearptr = _nearptr;
    }

    public envelope_filter_state(envelope_filter_state src) {

        this(src.ampbuf, src.ampptr, src.nearDC, src.nearDC_acc, src.nearDC_partialacc, src.nearptr);
    }

    public envelope_filter_state() {

        ampbuf = new float[VE_AMP];

        nearDC = new float[VE_NEARDC];
    }
}