/*
** delta_data.java
**
** Copyright (c) 2008 Peter McQuillan
**
** All Rights Reserved.
**
** Distributed under the BSD Software License (see license.txt)
**
*/
package com.wavpack.encoder;

class delta_data {
    int[] shaping_acc = new int[2];
    int[] shaping_delta = new int[2];
    int[] error = new int[2];
}
