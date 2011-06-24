/*
 * Copyright (C) 2010 in-somnia
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.sourceforge.jaad;

/**
 * The SampleBuffer holds the decoded AAC frame. It contains the raw PCM data
 * and its format.
 * @author in-somnia
 */
public class SampleBuffer {

	private int sampleRate;
    private int channels;
    private int bitsPerSample;
    private int samplesWritten;
    private double length, bitrate, encodedBitrate;
	private byte[] data;

    public SampleBuffer() {
		data = new byte[0];
		sampleRate = 0;
		channels = 0;
		bitsPerSample = 0;
	}

	/**
	 * Returns the buffer's PCM data.
	 * @return the audio data
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * Returns the data's sample rate.
	 * @return the sample rate
	 */
	public int getSampleRate() {
		return sampleRate;
	}

	/**
	 * Returns the number of channels stored in the data buffer.
	 * @return the number of channels
	 */
	public int getChannels() {
		return channels;
	}

	/**
	 * Returns the number of bits per sample. Usually this is 16, meaning a
	 * sample is stored in two bytes.
	 * @return the number of bits per sample
	 */
	public int getBitsPerSample() {
		return bitsPerSample;
	}

	/**
	 * Returns the length of the current frame in seconds.
	 * length = samplesPerChannel / sampleRate
	 * @return the length in seconds
	 */
	public double getLength() {
		return length;
	}

	/**
	 * Returns the bitrate of the decoded PCM data.
	 * <code>bitrate = (samplesPerChannel * bitsPerSample) / length</code>
	 * @return the bitrate
	 */
	public double getBitrate() {
		return bitrate;
	}

	/**
	 * Returns the AAC bitrate of the current frame.
	 * @return the AAC bitrate
	 */
	public double getEncodedBitrate() {
		return encodedBitrate;
	}

    /**
     * Returns number of pcm samples in the buffer
     *
     * @return number of samples
     */
    public int getSamples() {
        return samplesWritten;
    }

    public void setData(byte[] data, int sampleRate, int channels, int bitsPerSample, int bitsRead, int samplesWritten) {
		this.data = data;
		this.sampleRate = sampleRate;
		this.channels = channels;
		this.bitsPerSample = bitsPerSample;
        this.samplesWritten = samplesWritten;

        if(sampleRate==0) {
			length = 0;
			bitrate = 0;
			encodedBitrate = 0;
		}
		else {
			final int bytesPerSample = bitsPerSample/8; //usually 2
            int samplesPerChannel = data.length / (bytesPerSample * channels);
			length = (double) samplesPerChannel /(double) sampleRate;
			bitrate = (double) (samplesPerChannel *bitsPerSample*channels)/length;
			encodedBitrate = (double) bitsRead/length;
		}
	}
}
