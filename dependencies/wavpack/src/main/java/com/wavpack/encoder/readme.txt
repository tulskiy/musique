////////////////////////////////////////////////////////////////////////////
//            Java Implementation of WavPack Encoder                      //
//              Copyright (c) 2008 Peter McQuillan                        //
//                          All Rights Reserved.                          //
//      Distributed under the BSD Software License (see license.txt)      //
////////////////////////////////////////////////////////////////////////////

This package contains a Java implementation of the tiny version of the WavPack 
4.40 encoder. 
It is packaged with a demo command-line program that accepts file specifications
for a RIFF WAV file source and a WavPack file (.wv) destination and optionally a
correction file (.wvc) for demonstrating the hybrid lossless mode. 

This program (and the tiny encoder) do not handle placing the WAV RIFF header
into the WavPack file. The latest version of the regular WavPack unpacker
(4.40) and the "tiny decoder" will generate the RIFF header automatically on
unpacking and plugins do not generally use the RIFF header information because
all relevant information is stored natively. However, older versions of the
command-line program will complain about this and require unpacking in "raw"
mode.

For the highest performance the hybrid mode noise shaping default is off,
so the noise in lossy mode will have a perfectly flat spectrum. However, it
can be turned on from the command-line for testing. Also note that unlike
the regular command-line version of WavPack, the hybrid mode bitrate must
be specified in bits per sample rather than kbps.

Usage:   java WvEncode [-options] infile.wav outfile.wv [outfile.wvc]
 (default is lossless)

Options: -bn = enable hybrid compression, n = 2.0 to 16.0 bits/sample 
         -c  = create correction file (.wvc) for hybrid mode (=lossless)
         -cc = maximum hybrid compression (hurts lossy quality & decode speed)
         -f  = fast mode (fast, but some compromise in compression ratio)
         -h  = high quality (better compression in all modes, but slower)
         -hh = very high quality (best compression in all modes, but slowest
                              and NOT recommended for portable hardware use)
         -jn = joint-stereo override (0 = left/right, 1 = mid/side)
         -sn = noise shaping override (hybrid only, n = -1.0 to 1.0, 0 = off)

Please direct any questions or comments to beatofthedrum@gmail.com