package com.wavpack.decoder;

/*
** decorr_pass.java
**
** Copyright (c) 2007 - 2008 Peter McQuillan
**
** All Rights Reserved.
**                       
** Distributed under the BSD Software License (see license.txt)  
**
*/

class decorr_pass {
    short term, delta, weight_A, weight_B;
    int[] samples_A = new int[Defines.MAX_TERM];
    int[] samples_B = new int[Defines.MAX_TERM];
}