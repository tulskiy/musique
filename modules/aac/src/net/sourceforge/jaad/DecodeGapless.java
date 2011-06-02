package net.sourceforge.jaad;

import net.sourceforge.jaad.mp4.AudioFrame;
import net.sourceforge.jaad.mp4.MP4Reader;
import net.sourceforge.jaad.util.wav.WaveFileWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class DecodeGapless {

    private static final String USAGE = "usage:\njaad.DecodeGapless <infile> <outfile>\n\n";

    public static void main(String[] args) {
        try {
            if (args.length < 2) printUsage();
            decodeMP4(args[0], args[1]);
        } catch (IOException e) {
            System.err.println("error while decoding: " + e.toString());
        }
    }

    private static void printUsage() {
        System.out.println(USAGE);
        System.exit(1);
    }

    private static void decodeMP4(String in, String out) throws IOException {
        WaveFileWriter wav = null;
        try {
            final MP4Reader mp4 = new MP4Reader(new FileInputStream(in));
            final DecoderConfig conf = DecoderConfig.parseMP4DecoderSpecificInfo(mp4.getDecoderSpecificInfo());
            final Decoder dec = new Decoder(conf);

            AudioFrame frame;
            final SampleBuffer buf = new SampleBuffer();

            long numSamples = mp4.getNumSamples();
            int delay = (int) (mp4.getGaplessDelay() * mp4.getChannelCount());
            int offset;
            int sampleCount;

            for (int sample = 0; sample < numSamples; sample++) {
                frame = mp4.readSample(sample);
                if (frame == null) {
                    System.out.println("Error reading sample #" + sample);
                    break;
                }
                dec.decodeFrame(frame.getData(), buf);
                if (sample == numSamples - 1)
                    sampleCount = (int) (mp4.getGaplessPadding() * mp4.getChannelCount());
                else
                    sampleCount = buf.getSamples();

                if (wav == null) {
                    File output = new File(out);
                    //noinspection ResultOfMethodCallIgnored
                    output.delete();
                    wav = new WaveFileWriter(output, buf.getSampleRate(), buf.getChannels(), buf.getBitsPerSample());
                }

                if (delay > 0) {
                    if (delay < sampleCount) {
                        sampleCount -= delay;
                        offset = delay * buf.getBitsPerSample() / 8;
                        delay = 0;
                    } else {
                        delay -= sampleCount;
                        sampleCount = 0;
                        offset = 0;
                    }
                } else {
                    offset = 0;
                }

                wav.write(buf.getData(), offset, sampleCount * buf.getBitsPerSample() / 8);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (wav != null) wav.close();
        }
    }
}
