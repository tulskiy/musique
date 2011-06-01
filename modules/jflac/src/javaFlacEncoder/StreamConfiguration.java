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
 * This class defines the configuration options that may not change throughout
 * a FLAC stream. In general, these setting must be set to match the input
 * audio used(sample rate, sample size, channels, etc). After a stream has
 * started, these settings must not change.
 *
 * @author Preston Lacey
 */
public class StreamConfiguration implements Cloneable {

   /** Maximum Block size allowed(defined by flac spec) */
   public static final int MAX_BLOCK_SIZE = 65535;
   /** Minimum block size allowed(defined by flac spec) */
   public static final int MIN_BLOCK_SIZE = 16 ;
   /** Maximum channel count allowed(defined by flac spec) */
   public static final int MAX_CHANNEL_COUNT = 8;
   /** Minimum sample rate allowed(defined by flac spec) */
   public static final int MIN_SAMPLE_RATE = 1;
   /** Maximum sample rate allowed(defined by flac spec) */
   public static final int MAX_SAMPLE_RATE = 655350;
   /** Minimum bits per sample allowed(defined by flac spec) */
   public static final int MIN_BITS_PER_SAMPLE = 4;
   /** Maximum bits per sample allowed(FLAC spec allows 32, limited to 24 here
    * due to limits in code) */
   public static final int MAX_BITS_PER_SAMPLE = 24;
   /** Default channel count */
   public static final int DEFAULT_CHANNEL_COUNT = 2;
   /** Default maximum block size */
   public static final int DEFAULT_MAX_BLOCK_SIZE = 4096;
   /** Default minimum block size */
   public static final int DEFAULT_MIN_BLOCK_SIZE = 4096;
   /** Default sample rate */
   public static final int DEFAULT_SAMPLE_RATE = 44100;
   /** Default sample size */
   public static final int DEFAULT_BITS_PER_SAMPLE = 16;

   int channelCount;
   int maxBlockSize;
   int minBlockSize;
   int sampleRate;
   int bitsPerSample;

   /* is the currently set configuration valid? Encoders should not attempt to
    * use an invalid configuration */
   boolean validConfig = false;

   /**
    * Constructor, sets defaults for most values. Some default values must be
    * changed to match the audio characteristics(channel count, sample rate,
    * sample size).
    */
   public StreamConfiguration() {
      channelCount = DEFAULT_CHANNEL_COUNT;
      maxBlockSize = DEFAULT_MAX_BLOCK_SIZE;
      minBlockSize = DEFAULT_MIN_BLOCK_SIZE;
      sampleRate = DEFAULT_SAMPLE_RATE;
      bitsPerSample = DEFAULT_BITS_PER_SAMPLE;
      validConfig = true;
   }

   /**
    * Copy Constructor. No values are altered or verified for sanity
    * @param sc StreamConfiguration object to copy
    */
   public StreamConfiguration(StreamConfiguration sc) {
      channelCount = sc.channelCount;
      maxBlockSize = sc.maxBlockSize;
      minBlockSize = sc.minBlockSize;
      sampleRate = sc.sampleRate;
      bitsPerSample = sc.bitsPerSample;
      validConfig = sc.validConfig;
   }

   /**
    * Constructor, allows setting of all options. In general, parameters given
    * must match the input audio characteristics. minBlock and maxBlock may be
    * set as desired, though minBlock is expected to be less than or equal to
    * maxBlock. If minBlock or maxBlock is out of a valid range, it will be
    * automatically adjusted to the closest valid value.
    * 
    * @param channelCount number of channels in source audio stream
    * @param minBlock minimum block to use in FLAC stream.
    * @param maxBlock maximum block size to use in FLAC stream
    * @param sampleRate sample rate in Hz of audio stream
    * @param bitsPerSample sample size of audio stream
    */
   public StreamConfiguration(int channelCount, int minBlock, int maxBlock,
           int sampleRate, int bitsPerSample) {
      validConfig = true;
      validConfig &= setChannelCount(channelCount);
      validConfig &= setSampleRate(sampleRate);
      validConfig &= setBitsPerSample(bitsPerSample);
      setMaxBlockSize(maxBlock);
      setMinBlockSize(minBlock);
   }

   /**
    * Test if the current configuration is valid. While most set methods will
    * ensure the values are in a valid range before setting them, some
    * settings(such as number of channels), cannot be guessed. This method may
    * alter current values to make them valid if possible.
    * @return true if configuration defines a valid FLAC stream, false othwerise.
    */
   public boolean isValid() {
      validConfig = true;
      setMinBlockSize(minBlockSize);
      setMaxBlockSize(maxBlockSize);
      validConfig &= (minBlockSize <= maxBlockSize);
      validConfig &= setChannelCount(channelCount);
      validConfig &= setSampleRate(sampleRate);
      validConfig &= setBitsPerSample(bitsPerSample);
      return validConfig;
   }

   /**
    * Set number of channels in stream. Because this is not a value that may be
    * guessed and corrected, the value will be set to that given even if it is
    * not valid.
    * @param count Number of channels
    * @return true if the channel count is within the valid range, false
    * otherwise.
    */
   public boolean setChannelCount(int count) {
      boolean result = count > 0 && count <= MAX_CHANNEL_COUNT;
      channelCount = count;
      return result;
   }

   /**
    * Get the currently set channel count
    * @return channel count
    */
   public int getChannelCount() {
      return channelCount;
   }

   /**
    * Get the currently set maximum block size
    * @return maximum block size
    */
   public int getMaxBlockSize() {
      return maxBlockSize;
   }

   /**
    * Get the currently set minimum block size
    * @return minimum block size
    */
   public int getMinBlockSize() {
      return minBlockSize;
   }

   /**
    * Get the currently set sample rate
    * @return sample rate(in Hz)
    */
   public int getSampleRate() {
      return sampleRate;
   }

   /**
    * Set the sample rate. Because this is not a value that may be
    * guessed and corrected, the value will be set to that given even if it is
    * not valid.
    * @param rate sample rate(in Hz)
    * @return true if given rate was within the valid range, false otherwise.
    */
   public boolean setSampleRate(int rate) {
      boolean result = (rate <= MAX_SAMPLE_RATE && rate >= MIN_SAMPLE_RATE);
      sampleRate = rate;
      return result;
   }

   /**
    * Get the number of bits per sample
    * @return bits per sample
    */
   public int getBitsPerSample() {
       return bitsPerSample;
   }

   /**
    * Set the bits per sample. Because this is not a value that may be
    * guessed and corrected, the value will be set to that given even if it is
    * not valid.
    * @param bitsPerSample number of bits per sample
    * @return true if value given is within the valid range, false otherwise.
    */
   public boolean setBitsPerSample(int bitsPerSample) {
      boolean result = ((bitsPerSample <= MAX_BITS_PER_SAMPLE) &&
              (bitsPerSample >= MIN_BITS_PER_SAMPLE) );
      this.bitsPerSample = bitsPerSample;
      return result;
   }

   /**
    * Set the maximum block size to use. If this value is out of a valid range,
    * it will be set to the closest valid value. User must ensure that this
    * value is set above or equal to the minimum block size.
    * @param size maximum block size to use.
    * @return actual size set
    */
   public int setMaxBlockSize(int size) {
      maxBlockSize = (size <= MAX_BLOCK_SIZE) ? size:MAX_BLOCK_SIZE;
      maxBlockSize = (maxBlockSize >= MIN_BLOCK_SIZE) ? maxBlockSize:MIN_BLOCK_SIZE;
      return maxBlockSize;
   }

   /**
    * Set the minimum block size to use. If this value is out of a valid range,
    * it will be set to the closest valid value. User must ensure that this
    * value is set below or equal to the maximum block size.
    * @param size minimum block size to use.
    * @return actual size set
    */
   public int setMinBlockSize(int size) {
      minBlockSize = (size <= MAX_BLOCK_SIZE) ? size:MAX_BLOCK_SIZE;
      minBlockSize = (minBlockSize >= MIN_BLOCK_SIZE) ? maxBlockSize:MIN_BLOCK_SIZE;
      return minBlockSize;
   }

   /**
    * Test if stream is Subset compliant. FLAC defines a subset of options to
    * ensure resulting FLAC streams are streamable. Not all options in that
    * subset are defined by the StreamConfiguration class, however, but exist
    * in the EncodingConfiguration class. Therefore, the alternative method
    * {@link StreamConfiguration#isEncodingSubsetCompliant(javaFlacEncoder.EncodingConfiguration)
    * isEncodingSubsetCompliant}
    * should be checked as well to ensure the combined Stream/Encoding
    * configurations are BOTH valid.
    * @return true if this configuration is Subset compliant, false otherwise.
    */
   public boolean isStreamSubsetCompliant() {
      boolean result = true;
      result &= (maxBlockSize < 16384);
      if(sampleRate <= 48000)
         result &= (maxBlockSize < 4608);
      switch(bitsPerSample) {
         case 8:
         case 12:
         case 16:
         case 20:
         case 24: break;
         default: result = false;
      }
      return result;
    }

   /**
    * Test if this StreamConfiguration and a paired EncodingConfiguration define
    * a Subset compliant stream. FLAC defines a subset of options to
    * ensure resulting FLAC streams are streamable.
    * @param ec EncodingConfiguration object to check against
    * @return true if these configurations are Subset compliant, false otherwise.
    */
   public boolean isEncodingSubsetCompliant(EncodingConfiguration ec) {
      boolean result = true;
      result = isStreamSubsetCompliant();
      if(this.sampleRate <= 48000) {
         result &= ec.maximumLPCOrder <= 12;
         result &= ec.maximumRicePartitionOrder <= 8;
      }
      return result;
   }
}
