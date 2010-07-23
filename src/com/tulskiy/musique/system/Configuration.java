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

package com.tulskiy.musique.system;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;

/**
 * Author: Denis Tulskiy
 * Date: Jun 15, 2010
 */
public class Configuration {
    private Logger logger = Logger.getLogger("musique");
    private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    private Map<String, Object> map = new TreeMap<String, Object>();

    public void load(Reader reader) {
        try {
            logger.fine("Loading configuration");
            BufferedReader r = new BufferedReader(reader);

            ArrayList<String> array = null;
            String key = null;
            while (r.ready()) {
                String line = r.readLine();

                if (line == null)
                    break;

                if (line.startsWith("  ") && array != null) {
                    array.add(line.trim());
                } else {
                    if (array != null) {
                        if (array.size() > 0)
                            map.put(key, array);
                        array = null;
                    }

                    int index = line.indexOf(':');
                    if (index == -1)
                        continue;

                    key = line.substring(0, index);
                    String value = line.substring(index + 1).trim();
                    if (value.isEmpty()) {
                        array = new ArrayList<String>();
                    } else {
                        map.put(key, value);
                    }
                }
            }

            if (array != null)
                map.put(key, array);
        } catch (IOException e) {
            logger.severe("Failed to load configuration: " + e.getMessage());
        }
    }

    @SuppressWarnings({"unchecked"})
    public void save(Writer writer) {
        logger.fine("Saving configuration");
        PrintWriter w = new PrintWriter(writer);

        for (String key : map.keySet()) {
            Object value = get(key);
            if (value == null) {
                continue;
            }
            w.printf("%s: ", key);

            if (value instanceof List) {
                w.println();

                List<Object> list = (List<Object>) value;
                for (Object o : list) {
                    w.println("  " + o);
                }
            } else {
                w.println(value);
            }
        }

        w.close();
    }

    public void put(String key, Object value) {
        Object old = get(key);
        map.put(key, value);
        changeSupport.firePropertyChange(key, old, value);
    }

    public void remove(String key) {
        map.remove(key);
    }

    public Object get(String key) {
        return map.get(key);
    }

    public int getInt(String key, int def) {
        try {
            return Integer.valueOf(get(key).toString());
        } catch (Exception e) {
            setInt(key, def);
            return def;
        }
    }

    public void setInt(String key, int value) {
        put(key, value);
    }

    public float getFloat(String key, float def) {
        try {
            return Float.valueOf(get(key).toString());
        } catch (Exception e) {
            setFloat(key, def);
            return def;
        }
    }

    public void setFloat(String key, float value) {
        put(key, value);
    }

    public String getString(String key, String def) {
        try {
            return get(key).toString();
        } catch (Exception e) {
            setString(key, def);
            return def;
        }
    }

    public void setString(String key, String value) {
        put(key, value);
    }

    public Color getColor(String key, Color def) {
        try {
            String s = get(key).toString().substring(1);
            return new Color(Integer.parseInt(s, 16));
        } catch (Exception e) {
            setColor(key, def);
            return def;
        }
    }

    public void setColor(String key, Color value) {
        if (value == null)
            remove(key);
        else {
            String s = new Formatter().format(
                    "#%06X", value.getRGB() & 0xFFFFFF).toString();
            put(key, s);
        }
    }

    public Rectangle getRectangle(String key, Rectangle def) {
        try {
            String value = get(key).toString();
            String[] tokens = value.split(" ");
            if (tokens.length != 4)
                throw new NumberFormatException();

            int[] values = new int[4];
            for (int i = 0; i < tokens.length; i++) {
                String s = tokens[i];
                values[i] = Integer.parseInt(s);
            }
            return new Rectangle(values[0], values[1], values[2], values[3]);
        } catch (Exception e) {
            setRectangle(key, def);
            return def;
        }
    }

    public void setRectangle(String key, Rectangle value) {
        if (value == null)
            remove(key);
        else {
            String s = new Formatter().format("%d %d %d %d",
                    (int) value.getX(),
                    (int) value.getY(),
                    (int) value.getWidth(),
                    (int) value.getHeight()).toString();
            put(key, s);
        }
    }

    public Font getFont(String key, Font def) {
        try {
            String value = get(key).toString();
            String[] tokens = value.split(", ");

            return new Font(tokens[0],
                    Integer.parseInt(tokens[1]),
                    Integer.parseInt(tokens[2]));
        } catch (Exception e) {
            setFont(key, def);
            return def;
        }
    }

    public void setFont(String key, Font value) {
        if (value == null)
            remove(key);
        else {
            String s = new Formatter().format(
                    "%s, %d, %d",
                    value.getName(), value.getStyle(),
                    value.getSize()).toString();
            put(key, s);
        }
    }

    public boolean getBoolean(String key, boolean def) {
        try {
            String value = get(key).toString();
            return Boolean.parseBoolean(value);
        } catch (Exception e) {
            setBoolean(key, def);
            return def;
        }
    }

    public void setBoolean(String key, boolean value) {
        put(key, value);
    }

    @SuppressWarnings({"unchecked"})
    public ArrayList<String> getList(String key, ArrayList<String> def) {
        try {
            ArrayList<String> strings = (ArrayList<String>) get(key);
            if (strings != null)
                return strings;
        } catch (Exception ignored) {
        }
        setList(key, def);
        return def;
    }

    public void setList(String key, ArrayList<?> value) {
        if (value == null)
            remove(key);
        else {
            ArrayList<String> s = new ArrayList<String>(value.size());
            for (Object o : value) {
                s.add(o.toString());
            }
            put(key, s);
        }
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(propertyName, listener);
    }

    public void addPropertyChangeListener(String propertyName, boolean initialize, PropertyChangeListener listener) {
        addPropertyChangeListener(propertyName, listener);
        if (initialize)
            listener.propertyChange(new PropertyChangeEvent(this, propertyName, null, get(propertyName)));
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }
}


