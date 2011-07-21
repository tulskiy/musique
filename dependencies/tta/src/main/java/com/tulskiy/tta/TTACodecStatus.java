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
public enum TTACodecStatus {
    TTA_SUCCESS,    // setjmp data saved
    TTA_NO_ERROR,    // no known errors found
    TTA_OPEN_ERROR,    // can't open file
    TTA_FORMAT_ERROR,    // not compatible file format
    TTA_FILE_ERROR,    // file is corrupted
    TTA_READ_ERROR,    // can't read from input file
    TTA_WRITE_ERROR,    // can't write to output file
    TTA_SEEK_ERROR,    // file seek error
    TTA_MEMORY_ERROR,    // insufficient memory available
    TTA_PASSWORD_ERROR,    // password protected file
    TTA_NOT_SUPPORTED    // unsupported architecture

}
