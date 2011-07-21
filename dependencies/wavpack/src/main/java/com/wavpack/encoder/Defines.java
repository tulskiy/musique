/*
** Defines.java
**
** Copyright (c) 2008 Peter McQuillan
**
** All Rights Reserved.
**
** Distributed under the BSD Software License (see license.txt)
**
*/
package com.wavpack.encoder;

public class Defines {
    static int BIT_BUFFER_SIZE = 65536; // This should be carefully chosen for the
    // application and platform. Larger buffers are
    // somewhat more efficient, but the code will
    // allow smaller buffers and simply terminate
    // blocks early. If the hybrid lossless mode
    // (2 file) is not needed then the wvc_buffer
    // can be made very small.
    // or-values for "flags"
    public static int INPUT_SAMPLES = 65536;
    public static int BYTES_STORED = 3; // 1-4 bytes/sample
    public static long CONFIG_AUTO_SHAPING = 0x4000; // automatic noise shaping
    public static long CONFIG_BITRATE_KBPS = 0x2000; // bitrate is kbps, not bits / sample
    public static long CONFIG_BYTES_STORED = 3; // 1-4 bytes/sample
    public static long CONFIG_CALC_NOISE = 0x800000; // calc noise in hybrid mode
    public static long CONFIG_CREATE_EXE = 0x40000; // create executable
    public static long CONFIG_CREATE_WVC = 0x80000; // create correction file
    public static long CONFIG_CROSS_DECORR = 0x20; // no-delay cross decorrelation
    public static long CONFIG_EXTRA_MODE = 0x2000000; // extra processing mode
    public static long CONFIG_FAST_FLAG = 0x200; // fast mode
    public static long CONFIG_FLOAT_DATA = 0x80; // ieee 32-bit floating point data
    public static long CONFIG_HIGH_FLAG = 0x800; // high quality mode
    public static long CONFIG_HYBRID_FLAG = 8; // hybrid mode
    public static long CONFIG_HYBRID_SHAPE = 0x40; // noise shape (hybrid mode only)
    public static long CONFIG_JOINT_OVERRIDE = 0x10000; // joint-stereo mode specified
    public static long CONFIG_JOINT_STEREO = 0x10; // joint stereo
    public static long CONFIG_LOSSY_MODE = 0x1000000; // obsolete (for information)
    public static long CONFIG_MD5_CHECKSUM = 0x8000000; // compute & store MD5 signature
    public static long CONFIG_MONO_FLAG = 4; // not stereo
    public static long CONFIG_OPTIMIZE_MONO = 0x80000000; // optimize for mono streams posing as stereo
    public static long CONFIG_OPTIMIZE_WVC = 0x100000; // maximize bybrid compression
    public static long CONFIG_SHAPE_OVERRIDE = 0x8000; // shaping mode specified
    public static long CONFIG_SKIP_WVX = 0x4000000; // no wvx stream w/ floats & big ints
    public static long CONFIG_VERY_HIGH_FLAG = 0x1000; // very high
    public static int CROSS_DECORR = 0x20; // no-delay cross decorrelation
    public static short CUR_STREAM_VERS = 0x405; // stream version we are writing now

    // encountered
    public static int FALSE = 0;
    public static int FALSE_STEREO = 0x40000000; // block is stereo, but data is mono
    public static int FINAL_BLOCK = 0x1000; // final block of multichannel segment
    public static int FLOAT_DATA = 0x80; // ieee 32-bit floating point data
    public static int FLOAT_EXCEPTIONS = 0x20; // contains exceptions (inf, nan, etc.)
    public static int FLOAT_NEG_ZEROS = 0x10; // contains negative zeros
    public static int FLOAT_SHIFT_ONES = 1; // bits left-shifted into float = '1'
    public static int FLOAT_SHIFT_SAME = 2; // bits left-shifted into float are the same
    public static int FLOAT_SHIFT_SENT = 4; // bits shifted into float are sent literally
    public static int FLOAT_ZEROS_SENT = 8; // "zeros" are not all real zeros
    public static int HARD_ERROR = 2;
    public static int HYBRID_BALANCE = 0x400; // balance noise (hybrid stereo mode only)
    public static int HYBRID_BITRATE = 0x200; // bitrate noise (hybrid mode only)
    public static int HYBRID_FLAG = 8; // hybrid mode
    public static int HYBRID_SHAPE = 0x40; // noise shape (hybrid mode only)
    public static final short ID_CHANNEL_INFO = 0xd;
    public static final short ID_CONFIG_BLOCK = 0x25;
    public static final short ID_CUESHEET = 0x24;
    public static final short ID_DECORR_SAMPLES = 0x4;
    public static final short ID_DECORR_TERMS = 0x2;
    public static final short ID_DECORR_WEIGHTS = 0x3;
    public static final short ID_DUMMY = 0x0;
    public static short ID_ENCODER_INFO = 0x1;
    public static final short ID_ENTROPY_VARS = 0x5;
    public static final short ID_FLOAT_INFO = 0x8;
    public static final short ID_HYBRID_PROFILE = 0x6;
    public static final short ID_INT32_INFO = 0x9;
    public static int ID_LARGE = 0x80;
    public static final short ID_MD5_CHECKSUM = 0x26;
    public static int ID_ODD_SIZE = 0x40;
    public static short ID_OPTIONAL_DATA = 0x20;
    public static final short ID_REPLAY_GAIN = 0x23;
    public static final short ID_RIFF_HEADER = 0x21;
    public static final short ID_RIFF_TRAILER = 0x22;
    public static final short ID_SAMPLE_RATE = 0x27;
    public static final short ID_SHAPING_WEIGHTS = 0x7;
    public static final short ID_WVC_BITSTREAM = 0xb;
    public static final short ID_WVX_BITSTREAM = 0xc;
    public static final short ID_WV_BITSTREAM = 0xa;
    public static int IGNORED_FLAGS = 0x18000000; // reserved, but ignore if encountered
    public static int INITIAL_BLOCK = 0x800; // initial block of multichannel segment
    public static int INT32_DATA = 0x100; // special extended int handling
    public static int JOINT_STEREO = 0x10; // joint stereo
    public static int MAG_LSB = 18;
    public static int MAX_NTERMS = 16;
    public static int MAX_STREAM_VERS = 0x410; // highest stream version we'll decode
    public static int MAX_TERM = 8;
    public static int MIN_STREAM_VERS = 0x402; // lowest stream version we'll decode
    public static int MODE_FAST = 0x40;
    public static int MODE_FLOAT = 0x8;
    public static int MODE_HIGH = 0x20;
    public static int MODE_HYBRID = 0x4;
    public static int MODE_LOSSLESS = 0x2;
    public static int MODE_VALID_TAG = 0x10;
    public static int MODE_WVC = 0x1;
    public static int MONO_FLAG = 4; // not stereo
    public static int NEW_SHAPING = 0x20000000; // use IIR filter for negative shaping
    public static int NO_ERROR = 0;

    // Change the following value to an even number to reflect the maximum number of samples to be processed
    // per call to WavPackUtils.WavpackUnpackSamples
    public static int SAMPLE_BUFFER_SIZE = 256;
    public static int SHIFT_LSB = 13;
    public static int SOFT_ERROR = 1;
    public static int SRATE_LSB = 23;
    public static int TRUE = 1;
    public static int UNKNOWN_FLAGS = 0x80000000; // also reserved, but refuse decode if
    public static int WAVPACK_HEADER_SIZE = 32;
    public static long SRATE_MASK = (0xfL << SRATE_LSB);
    public static long SHIFT_MASK = (0x1fL << SHIFT_LSB);
    public static long MAG_MASK = (0x1fL << MAG_LSB);
}
