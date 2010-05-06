package org.jaudiotagger.logging;

import java.text.MessageFormat;

/**
 * Defines Error Messages
 */
public enum ErrorMessage {
    MP4_FILE_NOT_CONTAINER("This file does not appear to be an Mp4  file"),
    MP4_FILE_NOT_AUDIO("This file does not appear to be an Mp4 Audio file, could be corrupted or video "),
    MP4_FILE_IS_VIDEO("This file appears to be an Mp4 Video file, video files are not supported "),
    MP4_UNABLE_TO_PRIME_FILE_FOR_WRITE_SAFETLY("Unable to safetly check consistency in Mp4 file so cancelling save"),
    MP4_FILE_CONTAINS_MULTIPLE_DATA_ATOMS("File contains multiple data atoms"),
    MP4_CHANGES_TO_FILE_FAILED("Unable to make changes to Mp4 file"),
    MP4_CHANGES_TO_FILE_FAILED_NO_DATA("Unable to make changes to Mp4 file, no data was written"),
    MP4_CHANGES_TO_FILE_FAILED_DATA_CORRUPT("Unable to make changes to Mp4 file, invalid data length has been written"),
    MP4_CHANGES_TO_FILE_FAILED_NO_TAG_DATA("Unable to make changes to Mp4 file, no tag data has been written"),
    MP4_CHANGES_TO_FILE_FAILED_INCORRECT_OFFSETS("Unable to make changes to Mp4 file, incorrect offsets written difference was {0}"),
    FLAC_NO_FLAC_HEADER_FOUND("Flac Header not found, not a flac file"),
    OGG_VORBIS_NO_VORBIS_HEADER_FOUND("Cannot find vorbis setup parentHeader"),
    MP4_REVERSE_DNS_FIELD_HAS_NO_DATA("Reverse dns field:{0} has no data"),
    MP4_UNABLE_READ_REVERSE_DNS_FIELD("Unable to create reverse dns field because of exception:{0} adding as binary data instead"),
    OGG_VORBIS_NO_FRAMING_BIT("The OGG Stream is not valid, Vorbis tag valid framing bit is wrong {0} "),
    GENERAL_WRITE_FAILED("Cannot make changes to file {0}"),
    GENERAL_WRITE_FAILED_FILE_LOCKED("Cannot make changes to file {0} because it is being used by another application"),
    GENERAL_WRITE_FAILED_BECAUSE_FILE_IS_TOO_SMALL("Cannot make changes to file {0} because too small to be an audio file"),
    GENERAL_WRITE_FAILED_TO_DELETE_ORIGINAL_FILE("Cannot make changes to file {0} because unable to delete the original file ready for updating from temporary file {1}"),
    GENERAL_WRITE_FAILED_TO_RENAME_TO_ORIGINAL_FILE("Cannot make changes to file {0} because unable to rename from temporary file {1}"),
    GENERAL_WRITE_FAILED_TO_RENAME_ORIGINAL_FILE_TO_BACKUP("Cannot make changes to file {0} because unable to rename the original file to {1}"),
    GENERAL_WRITE_FAILED_TO_RENAME_ORIGINAL_BACKUP_TO_ORIGINAL("Unable to rename backup {0} back to file {1}"),
    GENERAL_WRITE_FAILED_NEW_FILE_DOESNT_EXIST("New file {0} does not appear to exist"),
    GENERAL_WRITE_FAILED_BECAUSE("Cannot make changes to file {0} because {1}"),
    GENERAL_WRITE_FAILED_BECAUSE_FILE_NOT_FOUND("Cannot make changes to file {0} because the file cannot be found"),
    GENERAL_WRITE_WARNING_UNABLE_TO_DELETE_BACKUP_FILE("Unable to delete the backup file {0}"),
    GENERAL_WRITE_PROBLEM_CLOSING_FILE_HANDLE("Problem closing file handles for file {0} because {1}"),
    GENERAL_DELETE_FAILED("Cannot delete file {0}"),
    GENERAL_DELETE_FAILED_FILE_LOCKED("Cannot delete file {0} because it is being used by another application"),
    GENERAL_DELETE_FAILED_BECAUSE_FILE_IS_TOO_SMALL("Cannot write to file {0} because too small to be an audio file"),
    MP3_ID3TAG_LENGTH_INCORRECT(" {0}:Checking further because the ID3 Tag ends at {1} but the mp3 audio doesnt start until {2}"),
    MP3_RECALCULATED_POSSIBLE_START_OF_MP3_AUDIO("{0}: Recalculated possible start of the audio to be at {1}"),
    MP3_RECALCULATED_START_OF_MP3_AUDIO("{0}: Recalculated the start of the audio to be at {1}"),
    MP3_START_OF_AUDIO_CONFIRMED("{0}: Confirmed audio starts at {1} whether searching from start or from end of ID3 tag"),
    MP3_URL_SAVED_ENCODED("Url:{0} saved in encoded form as {1}"),
    MP3_UNABLE_TO_ENCODE_URL("Unable to save url:{0} because cannot encode all characters setting to blank instead"),
    MP4_UNABLE_TO_FIND_NEXT_ATOM_BECAUSE_IDENTIFIER_IS_INVALID("Unable to find next atom because identifier is invalid {0}"),
    MP4_UNABLE_TO_FIND_NEXT_ATOM_BECAUSE_LENGTH_IS_INVALID("Unable to find next atom {0} because length is invalid {1}"),
    GENERAL_INVALID_NULL_ARGUMENT("Argument cannot be null"),
    MP4_GENRE_OUT_OF_RANGE("Genre Id {0} does not map to a valid genre"),
    MP3_PICTURE_TYPE_INVALID("Picture Type is set to invalid value:{0}"),
    MP3_REFERENCE_KEY_INVALID("{0}:No key could be found with the value of:{1}"),
    MP3_UNABLE_TO_ADJUST_PADDING("Problem adjusting padding in large file, expecting to write:{0} only wrote:{1}"),
    GENERAL_WRITE_FAILED_TO_DELETE_TEMPORARY_FILE("Unable to delete the temporary file {0}"),
    GENERAL_WRITE_FAILED_TO_CREATE_TEMPORARY_FILE_IN_FOLDER("Cannot modify {0} because do not have permissions to create files in the folder {1}"),
    GENERAL_WRITE_FAILED_TO_MODIFY_TEMPORARY_FILE_IN_FOLDER("Cannot modify {0} because do not have permissions to modify files in the folder {1}"),
    GENERAL_WRITE_FAILED_TO_OPEN_FILE_FOR_EDITING("Cannot modify {0} because do not have permissions to modify file"),
    NULL_PADDING_FOUND_AT_END_OF_MP4("Null Padding found at end of file starting at offset {0}"),
    OGG_VORBIS_NO_SETUP_BLOCK("Could not find the Ogg Setup block"),
    OGG_HEADER_CANNOT_BE_FOUND("OggS Header could not be found, not an ogg stream {0}"),
    GENERAL_READ_FAILED_UNABLE_TO_CLOSE_RANDOM_ACCESS_FILE("Unable to close random access file: {0}"),
    GENERAL_READ_FAILED_FILE_TOO_SMALL("Unable to read file because it is too small to be valid audio file: {0}"),
    GENERAL_READ_FAILED_DO_NOT_HAVE_PERMISSION_TO_READ_FILE("Unable to read file do not have permission to read: {0}"),
    ASF_FILE_HEADER_SIZE_DOES_NOT_MATCH_FILE_SIZE("For file {0} the File header size is {1} but different to actual file size of {2}"),
    ASF_FILE_HEADER_MISSING("For file {0} the File Header missing. Invalid ASF/WMA file."),
    ASF_HEADER_MISSING("For file {0} the Asf Header missing. Invalid ASF/WMA file."),
    GENERAL_UNIDENITIFED_IMAGE_FORMAT("Cannot safetly identify the format of this image setting to default type of Png"),
    MP4_IMAGE_FORMAT_IS_NOT_TO_EXPECTED_TYPE("ImageFormat for cover art atom is not set to a known image format, instead set to {0}"),
    MP3_FRAME_IS_COMPRESSED("Filename {0}:{1} is compressed"),
    MP3_FRAME_IS_ENCRYPTED("Filename {0}:{1} is encrypted"),
    MP3_FRAME_IS_GROUPED("Filename {0}:{1} is grouped"),
    MP3_FRAME_IS_UNSYNCHRONISED("Filename {0}:{1} is unsynchronised"),
    MP3_FRAME_IS_DATA_LENGTH_INDICATOR("Filename {0}:{1} has a data length indicator"),
    MP4_FILE_HAS_NO_METADATA("This file does not currently contain any metadata"),
    MP4_FILE_META_ATOM_CHILD_DATA_NOT_NULL("Expect data in meta box to be null"),;

    String msg;

    ErrorMessage(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public String getMsg(Object... args) {
        return MessageFormat.format(getMsg(), args);
    }

}
