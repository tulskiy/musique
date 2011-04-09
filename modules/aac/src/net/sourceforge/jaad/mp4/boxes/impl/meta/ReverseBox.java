package net.sourceforge.jaad.mp4.boxes.impl.meta;

import net.sourceforge.jaad.mp4.MP4InputStream;

import java.io.IOException;

/**
 * Author: Denis Tulskiy
 * Date: 4/3/11
 */
public class ReverseBox extends Mp4TagBox {
    public ReverseBox() {
        super("----", "----");
    }

    @Override
    public void decode(MP4InputStream in) throws IOException {
        readChildren(in);
    }
}
