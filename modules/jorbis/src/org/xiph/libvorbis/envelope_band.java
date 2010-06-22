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

class envelope_band {

    int begin;
    int end;
    float[] window;
    float total;


    public envelope_band(int _begin, int _end, float[] _window, float _total) {

        begin = _begin;
        end = _end;

        window = new float[_window.length];
        System.arraycopy(_window, 0, window, 0, _window.length);

        total = _total;
    }

    public envelope_band(envelope_band src) {

        this(src.begin, src.end, src.window, src.total);
    }

    public envelope_band() {
    }
}