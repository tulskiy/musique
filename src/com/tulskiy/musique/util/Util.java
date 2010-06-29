/*
 * Copyright (c) 2008, 2009, 2010 Denis Tulskiy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with this work.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.tulskiy.musique.util;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

/**
 * @Author: Denis Tulskiy
 * @Date: 21.11.2008
 */
public class Util {
    public static String samplesToTime(long samples, int sampleRate, int precision) {
        double seconds = AudioMath.samplesToMillis(samples, sampleRate) / 1000d;
        int min = (int) (seconds / 60);
        int hrs = min / 60;
        if (min > 0) seconds -= min * 60;
        if (hrs > 0) min -= hrs * 60;

        StringBuilder builder = new StringBuilder();
        if (hrs > 0) builder.append(hrs).append(":");
        if (hrs > 0 && min < 10) builder.append("0");
        builder.append(min).append(":");
        int n = precision + ((precision == 0) ? 2 : 3);
        String fmt = "%0" + n + "." + precision + "f";
        builder.append(new Formatter().format(Locale.US, fmt, seconds));
        return builder.toString();
    }

    public static String getFileExt(File file) {
        return getFileExt(file.getName());
    }

    public static String removeExt(String s) {
        return s.replaceAll("\\.[^\\.]*", "");
    }

    public static String getFileExt(String fileName) {
        int pos = fileName.lastIndexOf(".");
        if (pos == -1) return "";
        return fileName.substring(pos + 1).toLowerCase();
    }

    public static String longest(String... args) {
        if (args.length == 0) return "";
        String longest = args[0] == null ? "" : args[0];
        for (String s : args)
            if (s != null && s.length() > longest.length())
                longest = s;
        return longest;
    }

    public static String firstNotEmpty(String... values) {
        for (String value : values) {
            if (value != null && !value.isEmpty())
                return value;
        }

        return null;
    }

    public static boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }
}