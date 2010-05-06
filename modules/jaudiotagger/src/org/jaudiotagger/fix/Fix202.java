package org.jaudiotagger.fix;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.ogg.OggFileReader;

import java.io.File;

/**
 * Simple class that will attempt to recusively read all files within a directory, flags
 * errors that occur.
 */
public class Fix202 {

    public static void main(final String[] args) {
        Fix202 test = new Fix202();

        if (args.length != 1) {
            System.err.println("usage Fix202 Folder");
            System.err.println("      You must enter the folder containing the corrupted files");
            System.exit(1);
        }

        File dir = new File(args[0]);

        if (!dir.exists()) {
            System.err.println("usage Fix202 Folder");
            System.err.println("      File " + args[0] + " does not exist");
            System.exit(1);
        }
        if (!dir.isDirectory()) {
            System.err.println("usage Fix202 Folder");
            System.err.println("      File " + args[0] + " is not a folder");
            System.exit(1);
        }

        try {
            final File[] audioFiles = dir.listFiles(new OggFileFilter());
            if (audioFiles.length > 0) {
                for (File oggFile : audioFiles) {
                    System.out.print("Processing " + oggFile.getPath() + " ");
                    try {
                        //Read as broken dir, and save to fix
                        OggFileReader fileReader = new OggFileReader();
                        AudioFile audioFile = fileReader.read(oggFile);

                        //Read normally so not broken
                        System.out.println(":Not Broken");
                        continue;

                    }
                    catch (Throwable t) {
                        //Nneds fix continue
                    }

                    try {
                        //Read as broken dir, and save to fix
                        OggFileReader fileReader = new OggFileReader(Fix.FIX_OGG_VORBIS_COMMENT_NOT_COUNTING_EMPTY_COLUMNS);
                        AudioFile audioFile = fileReader.read(oggFile);
                        audioFile.commit();

                        //Read again normally to check fix
                        fileReader = new OggFileReader();
                        audioFile = fileReader.read(oggFile);
                        audioFile.commit();
                        System.out.println(":********Fixed*************");

                    }
                    catch (Throwable t) {
                        System.err.println("Unable to fix");
                    }
                }
            }
        }
        catch (Exception e) {
            System.err.println("Unable to extract tag");
            System.exit(1);
        }
    }

    static class OggFileFilter
            extends javax.swing.filechooser.FileFilter
            implements java.io.FileFilter {

        /**
         * Create a default OggFileFilter.  The allowDirectories field will
         * default to false.
         */
        public OggFileFilter() {
        }

        /**
         * Determines whether or not the file is an mp3 file.  If the file is
         * a directory, whether or not is accepted depends upon the
         * allowDirectories flag passed to the constructor.
         *
         * @param file the file to test
         * @return true if this file or directory should be accepted
         */
        public final boolean accept(final File file) {
            return (
                    (file.getName()).toLowerCase().endsWith(".ogg")

            );
        }

        /**
         * Returns the Name of the Filter for use in the Chooser Dialog
         *
         * @return The Description of the Filter
         */
        public final String getDescription() {
            return new String(".ogg Files");
        }
    }

    public static final String IDENT = "$Id: Fix202.java,v 1.1 2008/02/21 14:26:33 paultaylor Exp $";

}
