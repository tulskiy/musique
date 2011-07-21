/*
** RiffChunkHeader.java
**
** Copyright (c) 2008 Peter McQuillan
**
** All Rights Reserved.
**
** Distributed under the BSD Software License (see license.txt)
**
*/
package com.wavpack.encoder;

class RiffChunkHeader {
    char[] ckID = new char[4];
    long ckSize; // was uint32_t in C
    char[] formType = new char[4];
}
