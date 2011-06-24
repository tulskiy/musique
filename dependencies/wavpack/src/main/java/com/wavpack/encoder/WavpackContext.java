/*
** WavpackContext.java
**
** Copyright (c) 2008 Peter McQuillan
**
** All Rights Reserved.
**
** Distributed under the BSD Software License (see license.txt)
**
*/
package com.wavpack.encoder;

import java.io.RandomAccessFile;

public class WavpackContext {
    public WavpackConfig config = new WavpackConfig();
    WavpackStream stream = new WavpackStream();
    String error_message = "";
    java.io.DataInputStream infile;
    public RandomAccessFile outfile;
    public RandomAccessFile correction_outfile;
    public int first_block_size = -1;
    long total_samples; // was uint32_t in C
    int lossy_blocks;
    int wvc_flag;
    long block_samples;
    long acc_samples;
    long filelen;
    long file2len;
    short stream_version;
    public int byte_idx = 0; // holds the current buffer position for the input WAV data
}
