/*
** Bitstream.java
**
** Copyright (c) 2008 Peter McQuillan
**
** All Rights Reserved.
**
** Distributed under the BSD Software License (see license.txt)
**
*/

package com.wavpack.encoder;

class Bitstream {
    int end; // was uchar in c
    long sr; // was uint32_t in C
    int error;
    int bc;
    int buf_index = 0;
    int start_index = 0;
    int active = 0; // if 0 then this bitstream is not being used
}
