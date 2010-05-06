package com.wavpack.decoder;

/*
** WavpackMetadata.java
**
** Copyright (c) 2007 - 2008 Peter McQuillan
**
** All Rights Reserved.
**                       
** Distributed under the BSD Software License (see license.txt)  
**
*/

class WavpackMetadata {
    int byte_length;
    byte data[];
    short id;        // was uchar in C
    int hasdata = 0;    // 0 does not have data, 1 has data
    int status = 0;    // 0 ok, 1 error
    long bytecount = 24;// we use this to determine if we have read all the metadata 
    // in a block by checking bytecount again the block length
    // ckSize is block size minus 8. WavPack header is 32 bytes long so we start at 24
}