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

package com.tulskiy.musique.system.configuration;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import com.tulskiy.musique.plugins.hotkeys.HotkeyConfiguration;
import com.tulskiy.musique.system.Application;

/**
 * Author: Denis Tulskiy
 * Date: Jun 15, 2010
 */
public class Configuration extends XMLConfiguration {
    
    public static final int VERSION = 1;
    
    public static final String PROPERTY_INFO_VERSION = "info.version";

    private Logger logger = Logger.getLogger(getClass().getName());
    // TODO move to ConfigurationListener given by Configuration Commons
    private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    @Deprecated
    private Map<String, Object> map = new TreeMap<String, Object>();
    
    {
        setDelimiterParsingDisabled(true);
    }

    @Override
    public void load(Reader reader) {
        logger.fine("Loading configuration");
        
        try {
            super.load(reader);
        } catch (ConfigurationException e) {
            // error log disabled as we're gonna try to load deprecated configuration file of v0.2
//            logger.severe("Failed to load configuration: " + e.getMessage());
            convertOldConfiguration(reader);
        }

        int version = getInt(PROPERTY_INFO_VERSION, -1);
        if (version > VERSION) {
            logger.warning(String.format("Configuration of newer v%d found, but v%d is latest supported." +
                    " Backward compatibility is not guaranteed.", version, VERSION));
        }
        else if (version == -1) {
            logger.warning("Configuration of unknown version is loaded." +
                    " Backward compatibility is not guaranteed.");
        }
        else {
            logger.config(String.format("Configuration of v%d is loaded.", version));
        }
    }

    @SuppressWarnings("unchecked")
    @Deprecated
    private void convertOldConfiguration(Reader reader) {
        loadFromCustomFormat(reader);

        setFile(new File(Application.getInstance().CONFIG_HOME, "config"));
        addProperty(PROPERTY_INFO_VERSION, VERSION);

        Iterator<Entry<String, Object>> entries = map.entrySet().iterator();
        while (entries.hasNext()) {
            Entry<String, Object> entry = entries.next();
            String key = /*"musique." +*/ entry.getKey();
            if (entry.getValue() instanceof List) {
                List values = (List) entry.getValue();
                if (key.equals("albumart.stubs")) {
                    AlbumArtConfiguration.setStubs(values);
                }
                else if (key.equals("fileOperations.patterns")) {
                    FileOperationsConfiguration.setPatterns(values);
                }
                else if (key.equals("hotkeys.list")) {
                    HotkeyConfiguration.setHotkeysRaw(values);
                }
                else if (key.equals("library.folders")) {
                    LibraryConfiguration.setFolders(values);
                }
                else if (key.equals("playlist.columns")) {
                    PlaylistConfiguration.setColumnsRaw(values);
                }
                else if (key.equals("playlist.tabs.bounds")) {
                    PlaylistConfiguration.setTabBoundsRaw(values);
                }
                else if (key.equals("playlists")) {
                    PlaylistConfiguration.setPlaylistsRaw(values);
                }
                else {
                    for (Object value : values) {
                        addProperty(key, value);
                    }
                }
            }
            else {
                if (key.equals("wavpack.encoder.hybrid.wvc")) {
                    addProperty("wavpack.encoder.hybrid.wvc.enabled", entry.getValue());
                }
                else {
                    addProperty(key, entry.getValue());
                }
            }
        }
        
        map.clear();

        try {
            save();
        } catch (ConfigurationException e) {
            logger.severe("Failed to save converted configuration: " + e.getMessage());
        }
    }

    @Override
    public void save(Writer writer) {
        logger.fine("Saving configuration");
        
        try {
            OutputFormat format = new OutputFormat(createDocument());
            format.setLineWidth(65);
            format.setIndenting(true);
            format.setIndent(2);
            XMLSerializer serializer = new XMLSerializer(writer, format);
            serializer.serialize(getDocument());
            writer.close();
        }
        catch (ConfigurationException ce) {
            logger.severe("Failed to save configuration: " + ce.getMessage());
        }
        catch (IOException ioe) {
            logger.severe("Failed to save configuration: " + ioe.getMessage());
        }
    }

    // TODO remove when 0.3 released
    @Deprecated
    public void loadFromCustomFormat(Reader reader) {
        try {
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

    @Deprecated
    /**
     * use addProperty instead
     */
    public void add(String key, Object value) {
        Object old = get(key);
        addProperty(key, value == null ? null : value.toString());
        changeSupport.firePropertyChange(key, old, value);
    }

    @Deprecated
    /**
     * use setProperty instead
     */
    public void put(String key, Object value) {
        Object old = get(key);
        if (value == null) {
            remove(key);
        }
        else {
            setProperty(key, value.toString());
        }
        changeSupport.firePropertyChange(key, old, value);
    }

    public void remove(String key) {
        clearTree(key);
    }

    @Deprecated
    /**
     * use getProperty instead
     */
    public Object get(String key) {
        return getProperty(key);
    }

    public void setInt(String key, int value) {
        put(key, value);
    }

    public void setFloat(String key, float value) {
        put(key, value);
    }

    public void setString(String key, String value) {
        put(key, value);
    }

    public Color getColor(String key, Color def) {
        try {
            String s = getString(key).substring(1);
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
            String value = getString(key);
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
            String value = getString(key);
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

    public void setBoolean(String key, boolean value) {
        put(key, value);
    }

    public void setList(String key, List<?> values) {
        remove(key);
        if (values == null) {
            // TODO refactor when dev cycle finished (right now check implemented for debug in emergency case)
            logger.severe("Illegal argument (empty list). Please check calling code.");
        }
        for (Object value : values) {
            add(key, value);
        }
    }

    @SuppressWarnings({"unchecked"})
    public <E extends Enum<E>> E getEnum(String key, E def) {
        String val = getString(key, def.name());
        Class<E> clazz = (Class<E>) def.getClass();
        return E.valueOf(clazz, val);
    }

    public <E extends Enum<E>> void setEnum(String key, E value) {
        setString(key, value.name());
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


