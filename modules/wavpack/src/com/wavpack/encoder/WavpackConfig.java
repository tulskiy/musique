/*
** WavpackConfig.java
**
** Copyright (c) 2008 Peter McQuillan
**
** All Rights Reserved.
**
** Distributed under the BSD Software License (see license.txt)
**
*/
package com.wavpack.encoder;

class WavpackConfig {
    int bitrate;
    int shaping_weight;
    int bits_per_sample;
    int bytes_per_sample;
    int num_channels;
    int block_samples;
    long flags; // was uint32_t in C
    long sample_rate; // was uint32_t in C
}
