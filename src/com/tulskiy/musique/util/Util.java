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

    public static String stringToHTMLString(String string) {
        StringBuffer sb = new StringBuffer(string.length());
        // true if last char was blank
        int len = string.length();
        char c;

        for (int i = 0; i < len; i++) {
            c = string.charAt(i);

            //
            // HTML Special Chars
            if (c == '"')
                sb.append("&quot;");
            else if (c == '&')
                sb.append("&amp;");
            else if (c == '<')
                sb.append("&lt;");
            else if (c == '>')
                sb.append("&gt;");
            else if (c == '\n')
                // Handle Newline
                sb.append("&lt;br/&gt;");
            else {
                int ci = 0xffff & c;
                if (ci < 160)
                    // nothing special only 7 Bit
                    sb.append(c);
                else {
                    // Not 7 Bit use the unicode system
                    sb.append("&#");
                    sb.append(Integer.toString(ci));
                    sb.append(';');
                }
            }
        }
        return sb.toString();
    }

    public static String htmlToString(String string) {
        String ans = string.replaceAll("&quot;", "\"");
        ans = ans.replaceAll("&amp;", "&");
        ans = ans.replaceAll("&lt;", "<");
        ans = ans.replaceAll("&gt;", ">");
        ans = ans.replaceAll("<br />", "");

        return ans;
    }

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
//        if (precision)
        builder.append(new Formatter().format(Locale.US, fmt, seconds));
//        else
//            builder.append(new Formatter().format(Locale.US, "%02.0f", seconds));
//        String s = new Formatter().format(Locale.US, "%02d:%04.1f", min, seconds).toString();
//        if (hrs > 0)
//            s = hrs + ":" + s;
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

    public static HashMap<String, Object> loadFields(String field) {
        if (field == null)
            return null;

        final HashMap<String, Object> map = new LinkedHashMap<String, Object>(2);

        try {
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            parser.parse(new InputSource(new StringReader(field)), new DefaultHandler() {
                @Override
                public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                    if (qName.equals("field")) {
                        String name = attributes.getValue("name");
                        String value = attributes.getValue("value");

                        String attr = attributes.getValue("type");
                        if (attr.equals("string")) {
                            map.put(name, value);
                        } else if (attr.equals("int")) {
                            map.put(name, Integer.valueOf(value));
                        } else if (attr.equals("long")) {
                            map.put(name, Long.valueOf(value));
                        }
                    }
                }
            });
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        return map;
    }

    public static String getFields(HashMap<String, Object> map) {
        if (map == null)
            return "";
        StringBuilder sb = new StringBuilder("<root>");
        for (String key : map.keySet()) {
            Object o = map.get(key);
            sb.append("<field name=\"").append(key).
                    append("\" value=\"").append(stringToHTMLString(o.toString())).
                    append("\" type=\"");
            if (o instanceof String)
                sb.append("string");
            else if (o instanceof Integer)
                sb.append("int");
            else if (o instanceof Long)
                sb.append("long");
            sb.append("\" />");
        }
        sb.append("</root>");

        return sb.toString();
    }
}