package com.wavpack.decoder;

/*
** FloatUtils.java
**
** Copyright (c) 2007 - 2008 Peter McQuillan
**
** All Rights Reserved.
**                       
** Distributed under the BSD Software License (see license.txt)  
**
*/

class FloatUtils {


    static int read_float_info(WavpackStream wps, WavpackMetadata wpmd) {
        int bytecnt = wpmd.byte_length;
        byte byteptr[] = wpmd.data;
        int counter = 0;


        if (bytecnt != 4)
            return Defines.FALSE;

        wps.float_flags = byteptr[counter];
        counter++;
        wps.float_shift = byteptr[counter];
        counter++;
        wps.float_max_exp = byteptr[counter];
        counter++;
        wps.float_norm_exp = byteptr[counter];

        return Defines.TRUE;
    }


    static int[] float_values(WavpackStream wps, int[] values, long num_values) {
        int shift = wps.float_max_exp - wps.float_norm_exp + wps.float_shift;
        int value_counter = 0;

        if (shift > 32)
            shift = 32;
        else if (shift < -32)
            shift = -32;

        while (num_values > 0) {
            if (shift > 0)
                values[value_counter] <<= shift;
            else if (shift < 0)
                values[value_counter] >>= -shift;

            if (values[value_counter] > 8388607L)
                values[value_counter] = (int) 8388607L;
            else if (values[value_counter] < -8388608L)
                values[value_counter] = (int) -8388608L;

            value_counter++;
            num_values--;
        }

        return values;
    }

}
