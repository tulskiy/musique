/*
** WaveHeader.java
**
** Copyright (c) 2008 Peter McQuillan
**
** All Rights Reserved.
**
** Distributed under the BSD Software License (see license.txt)
**
*/
package com.wavpack.encoder;

class WaveHeader {
    int FormatTag; // was ushort in C
    int NumChannels; // was ushort in C
    long SampleRate; // was uint32_t in C
    long BytesPerSecond; // was uint32_t in C
    int BlockAlign; // was ushort in C
    int BitsPerSample; // was ushort in C
    int cbSize; // was ushort in C
    int ValidBitsPerSample; // was ushort in C
    int ChannelMask; // int32_t
    int SubFormat; // was ushort in C
    char[] GUID = new char[14];
}
