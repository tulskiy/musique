/*
 *  21.04.2004 Original verion. davagin@udm.ru.
 *-----------------------------------------------------------------------
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *----------------------------------------------------------------------
 */
package davaguine.jmac.core;

import davaguine.jmac.info.CompressionLevel;
import davaguine.jmac.tools.ProgressCallback;

/**
 * Author: Dmitry Vaguine
 * Date: 24.05.2004
 * Time: 10:50:38
 */
public class jmacu extends ProgressCallback {
    private static long g_nInitialTickCount;

    public void callback(int persent) {
        double dProgress = ((double) (pPercentageDone)) / 1000;
        double dElapsedMS = (System.currentTimeMillis() - g_nInitialTickCount);

        double dSecondsRemaining = (((dElapsedMS * 100) / dProgress) - dElapsedMS) / 1000;
        System.out.println("Progress: " + dProgress + " (" + dSecondsRemaining + " seconds remaining)");
    }

    public static void main(String[] args) {
        try {
            ///////////////////////////////////////////////////////////////////////////////
            // error check the command line parameters
            ///////////////////////////////////////////////////////////////////////////////
            boolean processed = false;
            if (args.length >= 2) {
                if (args[0].equals("d") && args.length == 3) {
                    g_nInitialTickCount = System.currentTimeMillis();
                    System.out.println("Decompressing '" + args[1] + "'...");

                    APESimple.DecompressFile(args[1], args[2], new jmacu());
                    processed = true;
                } else if (args[0].startsWith("c") && args[0].length() > 1) {
                    int compressionLevel = getCompressionLevel(args[0]);
                    if (compressionLevel > 0) {
                        g_nInitialTickCount = System.currentTimeMillis();
                        System.out.println("Compressing '" + args[1] + "'...");

                        // do the verify (call unmac.dll)
                        APESimple.CompressFile(args[1], args[2], compressionLevel, new jmacu());
                        processed = true;
                    }
                } else if (args[0].equals("v") && args.length == 2) {
                    g_nInitialTickCount = System.currentTimeMillis();
                    System.out.println("Verifying '" + args[1] + "'...");

                    APESimple.VerifyFile(args[1], new jmacu());
                    processed = true;
                } else if (args[0].startsWith("t") && args[0].length() > 1) {
                    int compressionLevel = getCompressionLevel(args[0]);
                    if (compressionLevel > 0) {
                        g_nInitialTickCount = System.currentTimeMillis();
                        System.out.println("Converting '" + args[1] + "'...");

                        APESimple.ConvertFile(args[1], args[2], compressionLevel, new jmacu());
                        processed = true;
                    }
                }
            }
            if (!processed)
                printUsage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getCompressionLevel(String command) {
        int compressionLevel = -1;
        try {
            compressionLevel = Integer.parseInt(command.substring(1));
        } catch (Exception e) {
            compressionLevel = -1;
        }
        if (compressionLevel > 0 && compressionLevel < 6) {
            switch (compressionLevel) {
                case 1:
                    compressionLevel = CompressionLevel.COMPRESSION_LEVEL_FAST;
                    break;
                case 2:
                    compressionLevel = CompressionLevel.COMPRESSION_LEVEL_NORMAL;
                    break;
                case 3:
                    compressionLevel = CompressionLevel.COMPRESSION_LEVEL_HIGH;
                    break;
                case 4:
                    compressionLevel = CompressionLevel.COMPRESSION_LEVEL_EXTRA_HIGH;
                    break;
                case 5:
                    compressionLevel = CompressionLevel.COMPRESSION_LEVEL_INSANE;
                    break;
            }
        }
        return compressionLevel;
    }

    public static void printUsage() {
        System.out.println("Usage: <Command> <Input file (wav or ape)> <Output file (wav or ape)>\n");
        System.out.println("Commands:\n");
        System.out.println("d - decompress file\n");
        System.out.println("cX - compress file");
        System.out.println("     X = 1 (Fast compression)");
        System.out.println("     X = 2 (Normal compression)");
        System.out.println("     X = 3 (High compression)");
        System.out.println("     X = 4 (Extra High compression)");
        System.out.println("     X = 5 (Insane compression)\n");
        System.out.println("v - verify file\n");
        System.out.println("tX - convert file");
        System.out.println("      X = 1 (Fast compression)");
        System.out.println("      X = 2 (Normal compression)");
        System.out.println("      X = 3 (High compression)");
        System.out.println("      X = 4 (Extra High compression)");
        System.out.println("      X = 5 (Insane compression)\n");
    }
}
