/*
** WavpackHeader.java
**
** Copyright (c) 2008 Peter McQuillan
**
** All Rights Reserved.
**
** Distributed under the BSD Software License (see license.txt)
**
*/
package com.wavpack.encoder;

class WavpackHeader {
    char[] ckID = new char[4];
    long ckSize; // was uint32_t in C
    short version;
    short track_no; // was uchar in C
    short index_no; // was uchar in C
    long total_samples; // was uint32_t in C
    long block_index; // was uint32_t in C
    long block_samples; // was uint32_t in C
    long flags; // was uint32_t in C
    long crc; // was uint32_t in C
}
