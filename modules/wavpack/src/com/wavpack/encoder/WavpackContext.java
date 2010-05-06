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

class WavpackContext {
    WavpackConfig config = new WavpackConfig();
    WavpackStream stream = new WavpackStream();
    String error_message = "";
    java.io.DataInputStream infile;
    java.io.FileOutputStream outfile;
    java.io.FileOutputStream correction_outfile;
    long total_samples; // was uint32_t in C
    int lossy_blocks;
    int wvc_flag;
    long block_samples;
    long acc_samples;
    long filelen;
    long file2len;
    short stream_version;
    int byte_idx = 0; // holds the current buffer position for the input WAV data
}
