/*
 * Based on TTA1-C++ library functions
 * Copyright (c) 2011 Aleksander Djuric. All rights reserved.
 * Distributed under the GNU Lesser General Public License (LGPL).
 * The complete text of the license can be found in the COPYING
 * file included in the distribution.
 */

package com.tulskiy.tta;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


/**
 * Author: Denis Tulskiy
 * Date: 6/2/11
 */
public class Main {
    public static void main(String[] args) throws InterruptedException {
        try {
            long time = System.currentTimeMillis();

            TTA_Decoder decoder = new TTA_Decoder(
                    new FileInputStream(
                            new File("sample.tta")));

            TTA_info info = decoder.init_get_info(0);
            int smp_size = info.nch * ((info.bps + 7) / 8);

            FileOutputStream fos = new FileOutputStream("output.wav");
            ByteBuffer header = ByteBuffer.allocate(44);
            header.order(ByteOrder.LITTLE_ENDIAN);
            header.put("RIFF".getBytes());
            long length = info.samples * smp_size;
            header.putInt((int) (36 + length));
            header.put("WAVE".getBytes());
            header.put("fmt ".getBytes());
            header.putInt(16);
            header.putShort((short) 1);
            header.putShort((short) info.nch);
            header.putInt(info.sps);
            header.putInt(info.sps * info.nch * info.bps / 8);
            header.putShort((short) (info.nch * info.bps / 8));
            header.putShort((short) info.bps);
            header.put("data".getBytes());
            header.putInt((int) length);
            fos.write(header.array());
            byte[] buffer = new byte[5120 + 4];

            while (true) {
                int len = decoder.process_stream(buffer);
                if (len <= 0) {
                    break;
                }

                fos.write(buffer, 0, len);
            }

            fos.close();
            System.out.println(System.currentTimeMillis() - time);
            Thread.sleep(1000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
