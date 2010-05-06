package org.jaudiotagger.test;

import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;
import org.jaudiotagger.tag.id3.ID3v23Frame;
import org.jaudiotagger.tag.id3.ID3v23Tag;
import org.jaudiotagger.tag.id3.framebody.FrameBodyTCOP;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 */
public class TestCreatingTag {
    public static void main(final String[] args) {
        MP3File mp3 = null;
        // Open the MP3 File
        try {
            mp3 = new MP3File("D:/Code/jthink/opensrc/jaudiotagger/testdatatmp/testv1.mp3");
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (TagException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (ReadOnlyFileException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (InvalidAudioFrameException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (mp3.hasID3v2Tag()) {
            AbstractID3v2Tag tag = mp3.getID3v2Tag();
            if (tag.hasFrameOfType("TCOP")) {
                System.out.println("A TCOP frame has been   found");
                Object currentFrame = tag.getFrame("TCOP");
                System.out.println(currentFrame.toString());
            } else {
                FrameBodyTCOP fBodyTCOP = new FrameBodyTCOP();
                fBodyTCOP.setText("4 Text to be displayed");
                //Create TCOP frame and add it to the MP3
                ID3v23Frame frameTCOP = new ID3v23Frame("TCOP");
                frameTCOP.setBody(fBodyTCOP);
                ID3v23Tag tagTCOP = new ID3v23Tag();
                tagTCOP.setFrame(frameTCOP);
                mp3.setID3v2TagOnly(tagTCOP);
            }
        }

        File overRight = new File("D:/Code/jthink/opensrc/jaudiotagger/testdatatmp/testv1.mp3");
        try {
            //mp3.setFile(overRight);
            //mp3.save(overRight);
            mp3.save();
        }
        catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (TagException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
