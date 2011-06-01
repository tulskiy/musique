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
 * Description: This abstract class declares the methods needed to retrieve
 * encoded data in a standard format(across the different implemented Subframe
 * types), as well as the generic methods needed to write the subframe header.
 * It is assumed that objects of this type will be reused in future frames,
 * rather than being destroyed and made anew. This is to avoid the overhead
 * associated with creating and destroying objects in Java.
 * @author Preston Lacey
 */
public abstract class Subframe {

    /** StreamConfiguration used for encoding. The same StreamConfiguration
     *  MUST be used throughout an entire FLAC stream */
    protected StreamConfiguration sc;

    /** EncodingConfiguration used for encoding. This may be altered between
     * frames, but must be the same for each subframe within that frame */
    protected EncodingConfiguration ec;
    /** Store for size of last subframe encoded(in bits). */
    protected int lastEncodedSize;

    /**
     * Constructor is private to prevenet it's use, as a subframe is not usable
     * without first setting a StreamConfiguration(therefore, use other
     * constructor.
     */
    private Subframe() {

    }

    /**
     * Constructor. Sets StreamConfiguration to use. If the StreamConfiguration
     * must later be changed, a new Subframe object must be created as well. A
     * default EncodingConfiguration is created using EncodingConfiguration's
     * default Constructor. This configuration should create a decent encode,
     * but may be altered if desired.
     *
     * @param sc StreamConfiguration to use for encoding.
     */
    public Subframe(StreamConfiguration sc) {
        this.sc = sc;
        ec = new EncodingConfiguration();
    }

    /**
     * This method is used to set the encoding configuration.
     * @param ec    encoding configuration to use.
     * @return      true if configuration was changed, false otherwise
     */
    public boolean registerConfiguration(EncodingConfiguration ec) {
        this.ec = ec;
        
        return true;
    }

    /**
     * Encodes samples into the appropriate compressed format, saving the result
     * in the given “data” EncodedElement list. Encodes 'count' samples, from
     * index 'start', to index 'start' times 'skip', where “skip” is the format
     * that samples may be packed in an array. For example, 'samples' may
     * include both left and right samples of a stereo stream, while this
     * SubFrame is only encoding the 'right' channel(channel 2). Therefore,
     * “skip” would equal 2, resulting in the valid indices being only those
     * where “index mod 2 equals 1”.
     * @param samples   the audio samples to encode. This array may contain
     * samples for multiple channels, interleaved; only one of these channels is
     * encoded by a subframe.
     * @param count     the number of samples to encode.
     * @param start     the index to start at in the array.
     * @param skip      the number of indices to skip between successive samples
     *                  (for use when channels are interleaved in the given
     *                  array).
     * @param data      the EncodedElement to attach encoded data to. Data in
     *                  EncodedElement given is not altered. New data is
     *                  attached starting with “data.getNext()”. If “data”
     *                  already has a “next” set, it will be lost!
     * @param offset
     * @param bitsPerSample Number of bits per single-channel sample. This may
     * differ from the StreamConfiguration's sample size, depending on the
     * subframe used(i.e, the "side-channel" of a FLAC stream uses one extra bit
     * compared to the input channels).
     *
     * @return          number of encoded samples, or negative value indicating
     *                  an error has occurred.
     *
     */
    public abstract int encodeSamples(int[] samples, int count, int start, int skip,
        EncodedElement data, int offset, int bitsPerSample);

/*    public abstract int encodeSamples(int[] samples, int count, int start, int skip,
        int offset, int bitsPerSample);

    public abstract int estimatedSize();

    public abstract EncodedElement getData();*/

    /**
     * Returns the total number of valid bits used in the last encoding(i.e, the
     * number of compressed bits used). This is here for convenience, as the
     * calling object may also loop through the resulting EncodingElement from
     * the encoding process and sum the valid bits.
     *
     * @return      an integer with value of the number of bits used in last
     *              encoding.
     */
    public int getEncodedSize() {
        return lastEncodedSize;
    }

}
