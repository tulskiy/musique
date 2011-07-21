package org.jaudiotagger.audio;

/**
 * Files formats currently supported by Library
 */
public enum SupportedFileFormat {
    OGG("ogg"),
    MP3("mp3"),
    FLAC("flac"),
    MP4("mp4"),
    M4A("m4a"),
    M4P("m4p"),
    WAV("wav"),
    M4B("m4b");

    private String filesuffix;

    SupportedFileFormat(String filesuffix) {
        this.filesuffix = filesuffix;
    }

    public String getFilesuffix() {
        return filesuffix;
    }
}
