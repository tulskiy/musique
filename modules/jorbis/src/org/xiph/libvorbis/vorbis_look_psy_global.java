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

class vorbis_look_psy_global {

    float ampmax;
    int channels;

    vorbis_info_psy_global gi;
    int[][] coupling_pointlimit;     // int coupling_pointlimit[2][P_NOISECURVES];


    public vorbis_look_psy_global(float _ampmax, int _channels, vorbis_info_psy_global _gi, int[][] _coupling_pointlimit) {

        ampmax = _ampmax;
        channels = _channels;
        gi = _gi;

        coupling_pointlimit = new int[2][P_NOISECURVES];
        for (int i = 0; i < _coupling_pointlimit.length; i++)
            System.arraycopy(_coupling_pointlimit[i], 0, coupling_pointlimit[i], 0, _coupling_pointlimit[i].length);
    }

    public vorbis_look_psy_global(vorbis_look_psy_global src) {

        this(src.ampmax, src.channels, new vorbis_info_psy_global(src.gi), src.coupling_pointlimit);
    }

    public vorbis_look_psy_global(vorbis_info _vi) {

        coupling_pointlimit = new int[2][P_NOISECURVES];

        channels = _vi.channels;

        ampmax = -9999.0f;
        gi = _vi.codec_setup.psy_g_param;
    }
}

