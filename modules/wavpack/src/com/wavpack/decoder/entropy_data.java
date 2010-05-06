package com.wavpack.decoder;

/*
** entropy_data.java
**
** Copyright (c) 2007 - 2008 Peter McQuillan
**
** All Rights Reserved.
**                       
** Distributed under the BSD Software License (see license.txt)  
**
*/

class entropy_data {
    long slow_level;
    long median[] = {0, 0, 0};    // was uint32_t in C, we initialize in order to remove run time errors
    long error_limit; // was uint32_t in C
}