/*
 * Copyright (c) 2008, 2009, 2010, 2011 Denis Tulskiy
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

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Set;

/**
 * @Author: Denis Tulskiy
 * @Date: 21.11.2008
 */
public class Util {
    public static String htmlToString(String string) {
        String ans = string.replaceAll("&quot;", "\"");
        ans = ans.replaceAll("&amp;", "&");
        ans = ans.replaceAll("&lt;", "<");
        ans = ans.replaceAll("&gt;", ">");
        ans = ans.replaceAll("<.+?>", "");

        return ans;
    }

    public static String formatSeconds(double seconds, int precision) {
        int min = (int) ((Math.round(seconds)) / 60);
        int hrs = min / 60;
        if (min > 0) seconds -= min * 60;
        if (seconds < 0) seconds = 0;
        if (hrs > 0) min -= hrs * 60;
        int days = hrs / 24;
        if (days > 0) hrs -= days * 24;
        int weeks = days / 7;
        if (weeks > 0) days -= weeks * 7;

        StringBuilder builder = new StringBuilder();
        if (weeks > 0) builder.append(weeks).append("wk ");
        if (days > 0) builder.append(days).append("d ");
        if (hrs > 0) builder.append(hrs).append(":");
        if (hrs > 0 && min < 10) builder.append("0");
        builder.append(min).append(":");
//        int n = precision + ((precision == 0) ? 2 : 3);
//        String fmt = "%0" + n + "." + precision + "f";
//        builder.append(new Formatter().format(Locale.US, fmt, seconds));
        int sec = (int) seconds;
        if (sec < 10) builder.append("0");
        builder.append(Math.round(sec));
        return builder.toString();
    }

    public static String samplesToTime(long samples, int sampleRate, int precision) {
        if (samples <= 0)
            return "-:--";
        double seconds = AudioMath.samplesToMillis(samples, sampleRate) / 1000f;
        return formatSeconds(seconds, precision);
    }

    public static String getFileExt(File file) {
        return getFileExt(file.getName());
    }

    public static String removeExt(String s) {
        int index = s.lastIndexOf(".");
        if (index == -1) index = s.length();
        return s.substring(0, index);
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
        return s == null || s.isEmpty();
    }

    public static String humanize(String property) {
        String s = property.replaceAll("(?=\\p{Upper})", " ");
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public static String capitalize(String str, String delim) {
        str = str.replaceAll("&", "and");
        String[] strings = str.split("[ _]+");
        final StringBuilder sb = new StringBuilder();

        for (String s : strings) {
            s = s.toLowerCase();
            sb.append(s.substring(0, 1).toUpperCase());
            sb.append(s.substring(1)).append(delim);
        }
        sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }

    public static Color getContrastColor(Color bg) {
        int threshold = 105;
        int delta = (int) (bg.getRed() * 0.299 + bg.getGreen() * 0.587 + bg.getBlue() * 0.114);
        return (255 - delta < threshold) ? Color.black : Color.white;
    }

    public static void fixIconTextGap(JComponent menu) {
        if (isNimbusLaF()) {
            Component[] components = menu.getComponents();
            for (Component component : components) {
                if (component instanceof AbstractButton) {
                    AbstractButton b = (AbstractButton) component;
                    b.setIconTextGap(0);
                }

                if (component instanceof JMenu)
                    fixIconTextGap(((JMenu) component).getPopupMenu());
            }
        }
    }

    public static boolean isNimbusLaF() {
        return UIManager.getLookAndFeel().getName().contains("Nimbus");
    }

    public static boolean isWindowsLaF() {
        return UIManager.getLookAndFeel().getName().contains("Windows");
    }

    public static boolean isGTKLaF() {
        return UIManager.getLookAndFeel().getName().contains("GTK");
    }

    public static String center(String str, int maxSize, int size) {
        if (str == null || size <= 0 || str.length() >= maxSize) {
            return str;
        }
        int strLen = str.length();
        int pads = size - strLen;
        if (pads <= 0) {
            return str;
        }
        str = leftPad(str, strLen + pads / 2);
        str = rightPad(str, size);
        return str;
    }

    public static String rightPad(String s, int n) {
        return String.format("%1$-" + n + "s", s);
    }

    public static String leftPad(String s, int n) {
        return String.format("%1$#" + n + "s", s);
    }

    public static String formatFieldValues(Object values) {
		String result = null;

		if (values != null) {
        	if (values instanceof String) {
        		result = (String) values;
        	}
        	else if (values instanceof Set) {
        		Set vs = (Set) values;
        		if (vs.size() > 1) {
        			result = "<multiple values> " + vs.toString();
        		}
        		else if (vs.size() == 1) {
        			Object value = vs.iterator().next();
        			result = value == null ? "" : value.toString();
        		}
        	}
        }
        
        return result;
    }

    public static String formatFieldValues(Object values, String separator) {
        if (values != null && separator != null) {
        	if (values instanceof String) {
        		return (String) values;
        	}
        	else if (values instanceof Set) {
            	StringBuilder sb = new StringBuilder();
            	for (Object value : ((Set) values).toArray()) {
            		if (sb.length() != 0) {
            			sb.append(separator);
            		}
            		sb.append(value == null ? "" : value.toString());
            	}
            	return sb.toString();
        	}
        }
        
        return null;
    }
}