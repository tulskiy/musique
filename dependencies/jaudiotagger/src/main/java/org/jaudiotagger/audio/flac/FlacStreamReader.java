package org.jaudiotagger.audio.flac;

import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.logging.ErrorMessage;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;
import org.jaudiotagger.tag.id3.ID3v22Tag;
import org.jaudiotagger.tag.id3.ID3v23Tag;
import org.jaudiotagger.tag.id3.ID3v24Tag;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

/**
 * Flac Stream
 * <p/>
 * Reader files and identifies if this is in fact a flac stream
 */
public class FlacStreamReader {
    public static final int FLAC_STREAM_IDENTIFIER_LENGTH = 4;
    public static final String FLAC_STREAM_IDENTIFIER = "fLaC";

    private RandomAccessFile raf;
    private int startOfFlacInFile;

    /**
     * Create instance for holding stream info
     *
     * @param raf
     */
    public FlacStreamReader(RandomAccessFile raf) {
        this.raf = raf;

    }

    /**
     * Reads the stream block to ensure it is a flac file
     *
     * @throws IOException
     * @throws CannotReadException
     */
    public void findStream() throws IOException, CannotReadException {
        //Begins tag parsing
        if (raf.length() == 0) {
            //Empty File
            throw new CannotReadException("Error: File empty");
        }
        raf.seek(0);

        //FLAC Stream at start
        if (isFlacHeader()) {
            startOfFlacInFile = 0;
            return;
        }

        //Ok maybe there is an ID3v24tag first
        if (isId3v24Tag()) {
            startOfFlacInFile = (int) (raf.getFilePointer() - FLAC_STREAM_IDENTIFIER_LENGTH);
            return;
        }

        //Ok maybe there is an ID3v23tag first
        if (isId3v23Tag()) {
            startOfFlacInFile = (int) (raf.getFilePointer() - FLAC_STREAM_IDENTIFIER_LENGTH);
            return;
        }

        //Ok maybe there is an ID3v22tag first
        if (isId3v22Tag()) {
            startOfFlacInFile = (int) (raf.getFilePointer() - FLAC_STREAM_IDENTIFIER_LENGTH);
            return;
        }
        throw new CannotReadException(ErrorMessage.FLAC_NO_FLAC_HEADER_FOUND.getMsg());
    }

    private boolean isId3v24Tag() throws IOException {
        int id3tagsize;
        ID3v24Tag id3tag = new ID3v24Tag();
        ByteBuffer bb = ByteBuffer.allocate(AbstractID3v2Tag.TAG_HEADER_LENGTH);
        raf.seek(0);
        raf.getChannel().read(bb);
        if (id3tag.seek(bb)) {
            id3tagsize = id3tag.readSize(bb);
            raf.seek(id3tagsize);
            //FLAC Stream immediately after end of id3 tag
            if (isFlacHeader()) {
                return true;
            }
        }
        return false;
    }

    private boolean isId3v23Tag() throws IOException {
        int id3tagsize;
        ID3v23Tag id3tag = new ID3v23Tag();
        ByteBuffer bb = ByteBuffer.allocate(AbstractID3v2Tag.TAG_HEADER_LENGTH);
        raf.seek(0);
        raf.getChannel().read(bb);
        if (id3tag.seek(bb)) {
            id3tagsize = id3tag.readSize(bb);
            raf.seek(id3tagsize);
            //FLAC Stream immediately after end of id3 tag
            if (isFlacHeader()) {
                return true;
            }
        }
        return false;
    }

    private boolean isId3v22Tag() throws IOException {
        int id3tagsize;
        ID3v22Tag id3tag = new ID3v22Tag();
        ByteBuffer bb = ByteBuffer.allocate(AbstractID3v2Tag.TAG_HEADER_LENGTH);
        raf.seek(0);
        raf.getChannel().read(bb);
        if (id3tag.seek(bb)) {
            id3tagsize = id3tag.readSize(bb);
            raf.seek(id3tagsize);
            //FLAC Stream immediately after end of id3 tag
            if (isFlacHeader()) {
                return true;
            }
        }
        return false;
    }

    private boolean isFlacHeader() throws IOException {
        //FLAC Stream at start
        byte[] b = new byte[FLAC_STREAM_IDENTIFIER_LENGTH];
        raf.read(b);
        String flac = new String(b);
        return flac.equals(FLAC_STREAM_IDENTIFIER);
    }

    /**
     * Usually flac header is at start of file, but unofficially and ID3 tag is allowed at the start of the file.
     *
     * @return the start of the Flac within file
     */
    public int getStartOfFlacInFile() {
        return startOfFlacInFile;
    }
}
