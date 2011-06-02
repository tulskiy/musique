package net.sourceforge.jaad.mp4.boxes.impl.meta;

import net.sourceforge.jaad.mp4.MP4InputStream;
import net.sourceforge.jaad.mp4.boxes.SimpleBox;

import java.io.IOException;

/**
 * Author: Denis Tulskiy
 * Date: 4/3/11
 */
public class DataBox extends SimpleBox {
    private long dataType;

    public DataBox() {
        super("data", "data");
    }

    public long getDataType() {
        return dataType;
    }

    @Override
    public void decode(MP4InputStream in) throws IOException {
        dataType = in.readBytes(4);
        left -= 4;
        super.decode(in);
    }
}
