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

public class vorbis_info_mapping0 {

    int submaps;        // <= 16
    int[] chmuxlist;    // up to 256 channels in a Vorbis stream

    int[] floorsubmap;    // [mux] submap to floors 16
    int[] residuesubmap;    // [mux] submap to residue 16

    int coupling_steps;
    int[] coupling_mag;    // 256
    int[] coupling_ang;    // 256


    public vorbis_info_mapping0(int _submaps, int[] _chmuxlist, int[] _floorsubmap, int[] _residuesubmap, int _coupling_steps, int[] _coupling_mag, int[] _coupling_ang) {

        submaps = _submaps;

        chmuxlist = new int[256];
        System.arraycopy(_chmuxlist, 0, chmuxlist, 0, _chmuxlist.length);

        floorsubmap = new int[16];
        System.arraycopy(_floorsubmap, 0, floorsubmap, 0, _floorsubmap.length);

        residuesubmap = new int[16];
        System.arraycopy(_residuesubmap, 0, residuesubmap, 0, _residuesubmap.length);

        coupling_steps = _coupling_steps;

        coupling_mag = new int[256];
        System.arraycopy(_coupling_mag, 0, coupling_mag, 0, _coupling_mag.length);

        coupling_ang = new int[256];
        System.arraycopy(_coupling_ang, 0, coupling_ang, 0, _coupling_ang.length);
    }

    public vorbis_info_mapping0(vorbis_info_mapping0 src) {

        this(src.submaps, src.chmuxlist, src.floorsubmap, src.residuesubmap, src.coupling_steps, src.coupling_mag, src.coupling_ang);
    }
}