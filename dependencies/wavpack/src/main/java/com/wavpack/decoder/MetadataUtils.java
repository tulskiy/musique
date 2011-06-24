package com.wavpack.decoder;

/*
** MetadataUtils.java
**
** Copyright (c) 2007 - 2008 Peter McQuillan
**
** All Rights Reserved.
**                       
** Distributed under the BSD Software License (see license.txt)  
**
*/

class MetadataUtils {
    static int read_metadata_buff(WavpackContext wpc, WavpackMetadata wpmd) {
        long bytes_to_read;
        short tchar;

        if (wpmd.bytecount >= wpc.stream.wphdr.ckSize) {
            // we have read all the data in this block
            return Defines.FALSE;
        }

        try {
            wpmd.id = (short) wpc.infile.readUnsignedByte();
            tchar = (short) wpc.infile.readUnsignedByte();
        }
        catch (Exception e) {
            wpmd.status = 1;
            return Defines.FALSE;
        }

        wpmd.bytecount += 2;

        wpmd.byte_length = tchar << 1;

        if ((wpmd.id & Defines.ID_LARGE) != 0) {
            wpmd.id &= ~Defines.ID_LARGE;

            try {
                tchar = (short) wpc.infile.readUnsignedByte();
            }
            catch (Exception e) {
                wpmd.status = 1;
                return Defines.FALSE;
            }

            wpmd.byte_length += (int) tchar << 9;

            try {
                tchar = (short) wpc.infile.readUnsignedByte();
            }
            catch (Exception e) {
                wpmd.status = 1;
                return Defines.FALSE;
            }

            wpmd.byte_length += (int) tchar << 17;
            wpmd.bytecount += 2;
        }

        if ((wpmd.id & Defines.ID_ODD_SIZE) != 0) {
            wpmd.id &= ~Defines.ID_ODD_SIZE;
            wpmd.byte_length--;
        }

        if (wpmd.byte_length == 0 || wpmd.id == Defines.ID_WV_BITSTREAM) {
            wpmd.hasdata = Defines.FALSE;
            return Defines.TRUE;
        }

        bytes_to_read = wpmd.byte_length + (wpmd.byte_length & 1);

        wpmd.bytecount += bytes_to_read;

        if (bytes_to_read > wpc.read_buffer.length) {
            int bytes_read;
            wpmd.hasdata = Defines.FALSE;

            while (bytes_to_read > wpc.read_buffer.length) {
                try {
                    bytes_read = wpc.infile.read(wpc.read_buffer, 0, wpc.read_buffer.length);
                    if (bytes_read != wpc.read_buffer.length) {
                        return Defines.FALSE;
                    }
                }
                catch (Exception e) {
                    return Defines.FALSE;
                }
                bytes_to_read -= wpc.read_buffer.length;
            }
        } else {
            wpmd.hasdata = Defines.TRUE;
            wpmd.data = wpc.read_buffer;
        }

        if (bytes_to_read != 0) {
            int bytes_read;

            try {
                bytes_read = wpc.infile.read(wpc.read_buffer, 0, (int) bytes_to_read);
                if (bytes_read != (int) bytes_to_read) {
                    wpmd.hasdata = Defines.FALSE;
                    return Defines.FALSE;
                }
            }
            catch (Exception e) {
                wpmd.hasdata = Defines.FALSE;
                return Defines.FALSE;
            }
        }

        return Defines.TRUE;
    }

    static int process_metadata(WavpackContext wpc, WavpackMetadata wpmd) {
        WavpackStream wps = wpc.stream;

        switch (wpmd.id) {
            case Defines.ID_DUMMY: {
                return Defines.TRUE;
            }

            case Defines.ID_DECORR_TERMS: {
                return UnpackUtils.read_decorr_terms(wps, wpmd);
            }

            case Defines.ID_DECORR_WEIGHTS: {
                return UnpackUtils.read_decorr_weights(wps, wpmd);
            }

            case Defines.ID_DECORR_SAMPLES: {
                return UnpackUtils.read_decorr_samples(wps, wpmd);
            }

            case Defines.ID_ENTROPY_VARS: {
                return WordsUtils.read_entropy_vars(wps, wpmd);
            }

            case Defines.ID_HYBRID_PROFILE: {
                return WordsUtils.read_hybrid_profile(wps, wpmd);
            }

            case Defines.ID_FLOAT_INFO: {
                return FloatUtils.read_float_info(wps, wpmd);
            }

            case Defines.ID_INT32_INFO: {
                return UnpackUtils.read_int32_info(wps, wpmd);
            }

            case Defines.ID_CHANNEL_INFO: {
                return UnpackUtils.read_channel_info(wpc, wpmd);
            }

            case Defines.ID_SAMPLE_RATE: {
                return UnpackUtils.read_sample_rate(wpc, wpmd);
            }

            case Defines.ID_CONFIG_BLOCK: {
                return UnpackUtils.read_config_info(wpc, wpmd);
            }

            case Defines.ID_WV_BITSTREAM: {
                return UnpackUtils.init_wv_bitstream(wpc, wpmd);
            }

            case Defines.ID_SHAPING_WEIGHTS:
            case Defines.ID_WVC_BITSTREAM:
            case Defines.ID_WVX_BITSTREAM: {
                return Defines.TRUE;
            }

            default: {
                if ((wpmd.id & Defines.ID_OPTIONAL_DATA) != 0) {
                    return Defines.TRUE;
                } else {
                    return Defines.FALSE;
                }
            }
        }
    }
}