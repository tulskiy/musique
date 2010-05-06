/*
** words_data.java
**
** Copyright (c) 2008 Peter McQuillan
**
** All Rights Reserved.
**
** Distributed under the BSD Software License (see license.txt)
**
*/
package com.wavpack.encoder;

class words_data {
    long[] bitrate_delta = new long[2]; // was uint32_t  in C
    long[] bitrate_acc = new long[2]; // was uint32_t  in C
    long pend_data; // was uint32_t  in C
    long holding_one; // was uint32_t  in C
    long zeros_acc; // was uint32_t  in C
    long[][] median = new long[3][2]; // was uint32_t  in C
    long[] slow_level = new long[2]; // was uint32_t  in C
    long[] error_limit = new long[2]; // was uint32_t  in C
    int holding_zero;
    int pend_count;

    //    entropy_data temp_ed1 = new entropy_data();
    //    entropy_data temp_ed2 = new entropy_data();
    //    entropy_data c[] = {temp_ed1 , temp_ed2 };
}
