package com.wavpack.decoder;

/*
** WavpackStream.java
**
** Copyright (c) 2007 - 2008 Peter McQuillan
**
** All Rights Reserved.
**                       
** Distributed under the BSD Software License (see license.txt)  
**
*/

public class WavpackStream {
    WavpackHeader wphdr = new WavpackHeader();
    Bitstream wvbits = new Bitstream();

    words_data w = new words_data();

    int num_terms = 0;
    int mute_error;
    long sample_index, crc;    // was uint32_t in C

    short int32_sent_bits, int32_zeros, int32_ones, int32_dups;        // was uchar in C
    short float_flags, float_shift, float_max_exp, float_norm_exp;    // was uchar in C

    decorr_pass dp1 = new decorr_pass();
    decorr_pass dp2 = new decorr_pass();
    decorr_pass dp3 = new decorr_pass();
    decorr_pass dp4 = new decorr_pass();
    decorr_pass dp5 = new decorr_pass();
    decorr_pass dp6 = new decorr_pass();
    decorr_pass dp7 = new decorr_pass();
    decorr_pass dp8 = new decorr_pass();
    decorr_pass dp9 = new decorr_pass();
    decorr_pass dp10 = new decorr_pass();
    decorr_pass dp11 = new decorr_pass();
    decorr_pass dp12 = new decorr_pass();
    decorr_pass dp13 = new decorr_pass();
    decorr_pass dp14 = new decorr_pass();
    decorr_pass dp15 = new decorr_pass();
    decorr_pass dp16 = new decorr_pass();

    decorr_pass decorr_passes[] = {dp1, dp2, dp3, dp4, dp5, dp6, dp7, dp8, dp9, dp10, dp11, dp12, dp13, dp14, dp15, dp16};

}