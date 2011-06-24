/*
 * Copyright (c) 2008, 2009, 2010 Denis Tulskiy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with this work.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.tulskiy.musique.util;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @Author: Denis Tulskiy
 * @Date: 01.08.2009
 */
public class RMSCalculator {
    public static void main(String[] args) {
        String file1 = "testfiles/compl.wav";
        String file2 = "testfiles/compl_jl.wav";
        int n = 248832 - 529;

        try {
            RandomAccessFile f1 = new RandomAccessFile(file1, "r");
            RandomAccessFile f2 = new RandomAccessFile(file2, "r");

            f1.seek(44 + 1058);
            f2.seek(44);

            double rms = 0;
            double lin = 0.1;
            double max = 0;
            for (int i = 0; i < n; i++) {
                double s1 = (readLEShort(f1) * lin);
                double s2 = (readLEShort(f2) * lin);

                double diff = s1 - s2;
                if (Math.abs(diff) > max) {
                    max = Math.abs(diff);
                }
                rms += Math.pow(diff, 2);
            }

            rms /= n;
            rms = Math.sqrt(rms);
            double refLimited = Math.pow(2, -11) / Math.sqrt(12);
            double refFully = Math.pow(2, -15) / Math.sqrt(12);

            System.out.printf("RMS: %e (%s)\n", rms, (rms <= refFully ? "PASS" : rms <= refLimited ? "LIMITED" : "FAIL"));
            System.out.printf("Max: %e %.3fDb (%s)\n", max, linearToDb(max), max <= Math.pow(2, -14) ? "PASS" : "FAIL");

            f1.close();
            f2.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static double linearToDb(double value) {
        return (Math.log(value == 0.0D ? 1.0E-4D : value) / Math.log(10.0D) * 20.0D);
    }

    private static double readLEShort(RandomAccessFile f) {
        try {
            byte b1 = (byte) f.read();
            byte b2 = (byte) f.read();
            return (double) (b2 << 8 | b1 & 0xFF) / 32767.0;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
