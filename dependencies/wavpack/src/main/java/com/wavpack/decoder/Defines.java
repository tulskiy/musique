package com.wavpack.decoder;

/*
** Defines.java
**
** Copyright (c) 2007 - 2008 Peter McQuillan
**
** All Rights Reserved.
**                       
** Distributed under the BSD Software License (see license.txt)  
**
*/

public class Defines {
    // Change the following value to an even number to reflect the maximum number of samples to be processed
    // per call to WavPackUtils.WavpackUnpackSamples

    public static int SAMPLE_BUFFER_SIZE = 4096;

    static int FALSE = 0;
    static int TRUE = 1;

    // or-values for "flags"


    static int BYTES_STORED = 3;       // 1-4 bytes/sample
    static int MONO_FLAG = 4;       // not stereo
    static int HYBRID_FLAG = 8;       // hybrid mode
    static int FALSE_STEREO = 0x40000000;      // block is stereo, but data is mono

    static int SHIFT_LSB = 13;
    static long SHIFT_MASK = (0x1fL << SHIFT_LSB);

    static int FLOAT_DATA = 0x80;    // ieee 32-bit floating point data

    static int SRATE_LSB = 23;
    static long SRATE_MASK = (0xfL << SRATE_LSB);

    static int FINAL_BLOCK = 0x1000;  // final block of multichannel segment


    static int MIN_STREAM_VERS = 0x402;       // lowest stream version we'll decode
    static int MAX_STREAM_VERS = 0x410;       // highest stream version we'll decode


    static final short ID_DUMMY = 0x0;
    static short ID_ENCODER_INFO = 0x1;
    static final short ID_DECORR_TERMS = 0x2;
    static final short ID_DECORR_WEIGHTS = 0x3;
    static final short ID_DECORR_SAMPLES = 0x4;
    static final short ID_ENTROPY_VARS = 0x5;
    static final short ID_HYBRID_PROFILE = 0x6;
    static final short ID_SHAPING_WEIGHTS = 0x7;
    static final short ID_FLOAT_INFO = 0x8;
    static final short ID_INT32_INFO = 0x9;
    static final short ID_WV_BITSTREAM = 0xa;
    static final short ID_WVC_BITSTREAM = 0xb;
    static final short ID_WVX_BITSTREAM = 0xc;
    static final short ID_CHANNEL_INFO = 0xd;

    static int JOINT_STEREO = 0x10;    // joint stereo
    static int CROSS_DECORR = 0x20;    // no-delay cross decorrelation
    static int HYBRID_SHAPE = 0x40;    // noise shape (hybrid mode only)

    static int INT32_DATA = 0x100;   // special extended int handling
    static int HYBRID_BITRATE = 0x200;   // bitrate noise (hybrid mode only)
    static int HYBRID_BALANCE = 0x400;   // balance noise (hybrid stereo mode only)

    static int INITIAL_BLOCK = 0x800;   // initial block of multichannel segment

    static int FLOAT_SHIFT_ONES = 1;      // bits left-shifted into float = '1'
    static int FLOAT_SHIFT_SAME = 2;      // bits left-shifted into float are the same
    static int FLOAT_SHIFT_SENT = 4;      // bits shifted into float are sent literally
    static int FLOAT_ZEROS_SENT = 8;      // "zeros" are not all real zeros
    static int FLOAT_NEG_ZEROS = 0x10;   // contains negative zeros
    static int FLOAT_EXCEPTIONS = 0x20;   // contains exceptions (inf, nan, etc.)


    static short ID_OPTIONAL_DATA = 0x20;
    static int ID_ODD_SIZE = 0x40;
    static int ID_LARGE = 0x80;

    static int MAX_NTERMS = 16;
    static int MAX_TERM = 8;

    static int MAG_LSB = 18;
    static long MAG_MASK = (0x1fL << MAG_LSB);

    static final short ID_RIFF_HEADER = 0x21;
    static final short ID_RIFF_TRAILER = 0x22;
    static final short ID_REPLAY_GAIN = 0x23;
    static final short ID_CUESHEET = 0x24;
    static final short ID_CONFIG_BLOCK = 0x25;
    static final short ID_MD5_CHECKSUM = 0x26;
    static final short ID_SAMPLE_RATE = 0x27;

    static long CONFIG_BYTES_STORED = 3;       // 1-4 bytes/sample
    static long CONFIG_MONO_FLAG = 4;       // not stereo
    static long CONFIG_HYBRID_FLAG = 8;       // hybrid mode
    static long CONFIG_JOINT_STEREO = 0x10;    // joint stereo
    static long CONFIG_CROSS_DECORR = 0x20;    // no-delay cross decorrelation
    static long CONFIG_HYBRID_SHAPE = 0x40;    // noise shape (hybrid mode only)
    static long CONFIG_FLOAT_DATA = 0x80;    // ieee 32-bit floating point data
    static long CONFIG_FAST_FLAG = 0x200;   // fast mode
    static long CONFIG_HIGH_FLAG = 0x800;   // high quality mode
    static long CONFIG_VERY_HIGH_FLAG = 0x1000;  // very high
    static long CONFIG_BITRATE_KBPS = 0x2000;  // bitrate is kbps, not bits / sample
    static long CONFIG_AUTO_SHAPING = 0x4000;  // automatic noise shaping
    static long CONFIG_SHAPE_OVERRIDE = 0x8000;  // shaping mode specified
    static long CONFIG_JOINT_OVERRIDE = 0x10000; // joint-stereo mode specified
    static long CONFIG_CREATE_EXE = 0x40000; // create executable
    static long CONFIG_CREATE_WVC = 0x80000; // create correction file
    static long CONFIG_OPTIMIZE_WVC = 0x100000; // maximize bybrid compression
    static long CONFIG_CALC_NOISE = 0x800000; // calc noise in hybrid mode
    static long CONFIG_LOSSY_MODE = 0x1000000; // obsolete (for information)
    static long CONFIG_EXTRA_MODE = 0x2000000; // extra processing mode
    static long CONFIG_SKIP_WVX = 0x4000000; // no wvx stream w/ floats & big ints
    static long CONFIG_MD5_CHECKSUM = 0x8000000; // compute & store MD5 signature
    static long CONFIG_OPTIMIZE_MONO = 0x80000000; // optimize for mono streams posing as stereo

    static int MODE_WVC = 0x1;
    static int MODE_LOSSLESS = 0x2;
    static int MODE_HYBRID = 0x4;
    static int MODE_FLOAT = 0x8;
    static int MODE_VALID_TAG = 0x10;
    static int MODE_HIGH = 0x20;
    static int MODE_FAST = 0x40;

}