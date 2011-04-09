package com.wavpack.decoder;

import java.io.RandomAccessFile;

/*
** WavpackContext.java
**
** Copyright (c) 2007 - 2008 Peter McQuillan
**
** All Rights Reserved.
**                       
** Distributed under the BSD Software License (see license.txt)  
**
*/

public class WavpackContext {
    WavpackConfig config = new WavpackConfig();
    WavpackStream stream = new WavpackStream();


    byte read_buffer[] = new byte[65536];    // was uchar in C
    int[] temp_buffer = new int[Defines.SAMPLE_BUFFER_SIZE];
    int[] temp_buffer2 = new int[Defines.SAMPLE_BUFFER_SIZE];
    String error_message = "";
    boolean error;

    RandomAccessFile infile;
    long total_samples, crc_errors, first_flags;        // was uint32_t in C
    int open_flags, norm_offset;
    int reduced_channels = 0;
    int lossy_blocks;
    int status = 0;    // 0 ok, 1 error

    public boolean isError() {
        return error;
    }

    public String getErrorMessage() {
        return error_message;
    }
}