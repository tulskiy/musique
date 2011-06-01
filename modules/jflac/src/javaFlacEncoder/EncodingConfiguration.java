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
 * This class defines the configuration options that are allowed to change
 * within a FLAC stream. Options here may be changed from one frame to the next.
 * In general, the settings should not need altered, but the option to do so
 * remains.
 * 
 * @author Preston Lacey
 */
public class EncodingConfiguration implements Cloneable {

   /**
    * Defines the options for channel configuration to use. LEFT & RIGHT
    * channels refers to stereo audio as expected. INDEPENDENT channels refer to
    * each channel encoded separately. SIDE channel is the difference of the
    * LEFT and RIGHT channels(LEFT-RIGHT). MID channel is the integer average of
    * LEFT and RIGHT channels( (LEFT+RIGHT)/2 ). Options using MID and/or SIDE
    * channels may benefit encoding by taking advantage of similarities between
    * the channels, and are available in FLAC for STEREO streams only. In
    * general, ENCODER_CHOICE should be chosen.
    */
   public enum ChannelConfig {
     /** Encode channels independently **/
     INDEPENDENT,
     /** Encode LEFT and SIDE channels for stereo stream */
     LEFT_SIDE,
     /** Encode RIGHT and SIDE channels for stereo stream */
     RIGHT_SIDE,
     /** Encode MID and SIDE channels for stereo stream */
     MID_SIDE,
     /** Encode all options possible, and take the best(slow)*/
     EXHAUSTIVE,
     /** Let the encoder decide which to use.(recommended) */
     ENCODER_CHOICE
   };

   /**
    * Defines the various subframe types that may be used. If you don't know
    * what these are, choose EXHAUSTIVE(for description of subframe types, see
    * the flac format documentation at http://flac.sourceforge.net/format.html)
    */
   public enum SubframeType {
      /** Constant subframe, do not choose unless you are sure you're encoding
       * digital silence */
      CONSTANT,
      /** Decent compression, fasted option */
      FIXED,
      /** Better compression, slower */
      LPC,
      /** No compression, simply wraps unencoded audio into a FLAC stream */
      VERBATIM,
      /** Best compression, slightly slower than LPC alone, lets encoder choose
       * the best(Recommended). */
      EXHAUSTIVE
   }
   
   /** Maximum LPC order possible(as defined by FLAC format) */
   public static final int MAX_LPC_ORDER = 32;
   /** Minimum LPC order possible(as defined by FLAC format) */
   public static final int MIN_LPC_ORDER = 1;
   /** Maximum Rice Partition order possible(as defined by FLAC Format) */
   public static final int MAX_RICE_PARTITION_ORDER = 15;
   /** Default subframe type to use*/
   public static final SubframeType DEFAULT_SUBFRAME_TYPE =
           SubframeType.EXHAUSTIVE;
   /** Default channel configuration */
   public static final ChannelConfig DEFAULT_CHANNEL_CONFIG =
           ChannelConfig.ENCODER_CHOICE;
   /** Default maximum lpc order to use */
   public static final int DEFAULT_MAX_LPC_ORDER = 12;
   /** Default minimum lpc order to use */
   public static final int DEFAULT_MIN_LPC_ORDER = 1;
   /** Default maximum Rice partition order */
   public static final int DEFAULT_MAX_RICE_ORDER = 0;


   ChannelConfig channelConfig;
   SubframeType subframeType;
   int minimumLPCOrder = 1;
   int maximumLPCOrder = 16;
   int maximumRicePartitionOrder = 0;

   /**
    * Constructor, uses defaults for all options. These defaults should be good
    * for most purposes.
    */
   public EncodingConfiguration() {
       subframeType = DEFAULT_SUBFRAME_TYPE;
       channelConfig = DEFAULT_CHANNEL_CONFIG;
       maximumLPCOrder = DEFAULT_MAX_LPC_ORDER;
       minimumLPCOrder = DEFAULT_MIN_LPC_ORDER;
       maximumRicePartitionOrder = DEFAULT_MAX_RICE_ORDER;
   }

   /**
    * Copy constructor.
    * @param e EncodingConfiguration object to copy. Must not be null.
    */
   public EncodingConfiguration(EncodingConfiguration e) {
       subframeType = e.subframeType;
       channelConfig = e.channelConfig;
       minimumLPCOrder = e.minimumLPCOrder;
       maximumLPCOrder = e.maximumLPCOrder;
       maximumRicePartitionOrder = e.maximumRicePartitionOrder;
   }

   /**
    * Set the channel configuration to use. Channel configuration refers to the
    * way multiple channels are processed. See documentation for
    * {@link ChannelConfig ChannelConfig} for more info on choices.
    * @param conf Channel configuration to use.
    */
   public void setChannelConfig(ChannelConfig conf) {
       channelConfig = conf;
   }

   /**
    * Get the current channel configuration value.
    * @return current channel configuration value
    */
   public ChannelConfig getChannelConfig() {
       return channelConfig;
   }

   /**
    * Set the subframe type to use. This refers to the way each subframe(channel)
    * is compressed. See documentation for {@link SubframeType SubframeType} for
    * more info on choices.
    * @param type
    */
   public void setSubframeType(SubframeType type) {
       subframeType = type;
   }

   /**
    * Get the current subframe type
    * @return current subframe type
    */
   public SubframeType getSubframeType() {
       return subframeType;
   }

   /**
    * Get current minimum LPC order
    * @return current minimum lpc order
    */
   public int getMinLPCOrder() { return minimumLPCOrder; }

   /**
    * Get maximum LPC order
    * @return current maximum lpc order
    */
   public int getMaxLPCOrder() { return maximumLPCOrder; }
   /**
    * Set the minimum LPC order. If order given is out of the valid range(as
    * defined by {@link EncodingConfiguration#MAX_LPC_ORDER MAX_LPC_ORDER} and
    * {@link EncodingConfiguration#MIN_LPC_ORDER MIN_LPC_ORDER}), it will be
    * set to the closest valid value instead.
    * @param order minimum LPC order to use
    */
   public void setMinLPCOrder(int order) {
      minimumLPCOrder = (order < MIN_LPC_ORDER) ? MIN_LPC_ORDER:order;
      minimumLPCOrder = (minimumLPCOrder > MAX_LPC_ORDER) ?
         MAX_LPC_ORDER:minimumLPCOrder;
   }
   /**
    * Set the maximum LPC order. If order given is out of the valid range
    * (as defined by {@link EncodingConfiguration#MAX_LPC_ORDER MAX_LPC_ORDER}
    * and {@link EncodingConfiguration#MIN_LPC_ORDER MIN_LPC_ORDER}), it will be
    * set to the closest valid value instead.
    * @param order maximum LPC order to use
    */
   public void setMaxLPCOrder(int order) {
      maximumLPCOrder = (order < MIN_LPC_ORDER) ? MIN_LPC_ORDER:order;
      maximumLPCOrder = (maximumLPCOrder > MAX_LPC_ORDER) ?
         MAX_LPC_ORDER:maximumLPCOrder;
   }

}
