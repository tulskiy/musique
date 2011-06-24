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

public class WavpackConfig {
    public int bitrate;
    public int shaping_weight;
    public int bits_per_sample;
    public int bytes_per_sample;
    public int num_channels;
    public int block_samples;
    public long flags; // was uint32_t in C
    public long sample_rate; // was uint32_t in C
}
