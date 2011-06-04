/*
 * Based on TTA1-C++ library functions
 * Copyright (c) 2011 Aleksander Djuric. All rights reserved.
 * Distributed under the GNU Lesser General Public License (LGPL).
 * The complete text of the license can be found in the COPYING
 * file included in the distribution.
 */

package com.tulskiy.tta;

/**
 * Author: Denis Tulskiy
 * Date: 5/30/11
 */
public class tta_exception extends RuntimeException {
    public tta_exception(TTACodecStatus ttaFormatError) {
        super("TTA Exception: " + ttaFormatError);
    }

    public tta_exception(TTACodecStatus ttaFormatError, Throwable cause) {
        super("TTA Exception: " + ttaFormatError, cause);
    }
}
