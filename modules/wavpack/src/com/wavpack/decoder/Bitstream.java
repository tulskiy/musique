package com.wavpack.decoder;

import java.io.RandomAccessFile;
/*
** Bitstream.java
**
** Copyright (c) 2007 - 2008 Peter McQuillan
**
** All Rights Reserved.
**                       
** Distributed under the BSD Software License (see license.txt)
**
*/

class Bitstream {
    short end, ptr;    // was uchar in c
    long file_bytes, sr;    // was uint32_t in C
    int error, bc;
    RandomAccessFile file;
    int bitval = 0;
    byte[] buf;
    byte[] temp_buf = new byte[65536];
    int buf_index = 0;
}