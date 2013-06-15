package org.xiph.libvorbis;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * User: tulskiy
 * Date: 1/22/13
 */
public class vorbis_float_cache {
    private static Map<Integer, Queue<float[]>> arrays = new HashMap<Integer, Queue<float[]>>();

    public static synchronized float[] get(int size) {
        Queue<float[]> floats = arrays.get(size);

        if (floats == null) {
            floats = new LinkedList<float[]>();
            arrays.put(size, floats);
        }

        if (floats.isEmpty()) {
            floats.add(new float[size]);
        }

        return floats.poll();
    }

    public static synchronized void ret(float[] ... arr) {
        for (float[] floats : arr) {
            arrays.get(floats.length).add(floats);
        }
    }
}
