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

class highlevel_byblocktype {

    float tone_mask_setting;
    float tone_peaklimit_setting;
    float noise_bias_setting;
    float noise_compand_setting;


    public highlevel_byblocktype(float _tone_mask_setting, float _tone_peaklimit_setting, float _noise_bias_setting, float _noise_compand_setting) {

        tone_mask_setting = _tone_mask_setting;
        tone_peaklimit_setting = _tone_peaklimit_setting;
        noise_bias_setting = _noise_bias_setting;
        noise_compand_setting = _noise_compand_setting;
    }
}