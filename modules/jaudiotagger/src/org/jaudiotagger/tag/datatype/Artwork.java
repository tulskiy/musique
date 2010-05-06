package org.jaudiotagger.tag.datatype;

import org.jaudiotagger.tag.id3.valuepair.ImageFormats;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Represents artwork in a format independent way
 */
public class Artwork {
    private byte[] binaryData;
    private String mimeType;
    private String description;
    private boolean isLinked;
    private String imageUrl;
    private int pictureType;

    public byte[] getBinaryData() {
        return binaryData;
    }

    public void setBinaryData(byte[] binaryData) {
        this.binaryData = binaryData;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BufferedImage getImage() throws IOException {
        BufferedImage bi = ImageIO.read(ImageIO
                .createImageInputStream(new ByteArrayInputStream(getBinaryData())));
        return bi;
    }

    public boolean isLinked() {
        return isLinked;
    }

    public void setLinked(boolean linked) {
        isLinked = linked;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getPictureType() {
        return pictureType;
    }

    public void setPictureType(int pictureType) {
        this.pictureType = pictureType;
    }

    public void setFromFile(File file) throws IOException {
        RandomAccessFile imageFile = new RandomAccessFile(file, "r");
        byte[] imagedata = new byte[(int) imageFile.length()];
        imageFile.read(imagedata);

        setBinaryData(imagedata);
        setMimeType(ImageFormats.getMimeTypeForBinarySignature(imagedata));
    }

    public static Artwork createArtworkFromFile(File file) throws IOException {
        Artwork artwork = new Artwork();
        artwork.setFromFile(file);
        return artwork;
    }
}
