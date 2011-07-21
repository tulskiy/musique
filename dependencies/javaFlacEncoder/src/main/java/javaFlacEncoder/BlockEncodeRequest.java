/*
 * Copyright (C) 2010  Preston Lacey http://javaflacencoder.sourceforge.net/
 * All Rights Reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package javaFlacEncoder;
/**
 * BlockEncodeRequests are used to store a full block and necessary information
 * to encode such block. It is assumed member variables will be accessed
 * directly(for speed considerations). This Class simply gathers all values
 * into a single object, but handles no encoding logic itself.
 * @author Preston Lacey
 */
public class BlockEncodeRequest {
    /* Sample data, may be interleaved if multiple channels exist.*/
    volatile int[] samples;
    /* Number of valid samples in this request */
    volatile int count;
    /* Index of samples[] where the first valid sample exists */
    volatile int start;
    /* Number of indices to skip between valid samples */
    volatile int skip;
    /* Frame-number this block is assigned */
    volatile long frameNumber;
    /* Location to store results to. For safety, use an empty element*/
    volatile EncodedElement result;
    /* Stores whether the result should be valid */
    volatile boolean valid;
    /* Number of elements actually encoded. */
    volatile int encodedSamples;

    /**
     * Set all values, preparing this object to be sent to an encoder. Member
     * variable "valid" is set to false by this call.
     *
     * @param samples Sample data, interleaved if multiple channels are used
     * @param count Number of valid samples
     * @param start Index of first valid sample
     * @param skip  Number of samples to skip between samples(this should be
     * equal to number-of-channels minus 1.
     * @param frameNumber Framenumber assigned to this block.
     * @param result Location to store result of encode.
     */
    synchronized public void setAll(int[] samples, int count, int start, int skip,
            long frameNumber, EncodedElement result) {
        this.samples = samples;
        this.count = count;
        this.start = start;
        this.skip = skip;
        this.frameNumber = frameNumber;
        this.result = result;
        valid = false;
        this.encodedSamples = 0;
    }
}
