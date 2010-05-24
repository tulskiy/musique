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

import com.tulskiy.musique.db.*;
import com.tulskiy.musique.util.Util;

import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @Author: Denis Tulskiy
 * @Date: Jan 7, 2010
 */

public class Configuration {
    private Application app = Application.getInstance();

    enum PropertyType {
        STRING,
        INTEGER,
        BOOLEAN,
        DOUBLE,
        COLOR,
        FONT,
        RECTANGLE,
        ARRAY,
        MAP
    }

    private DBMapper<Property> propertyDBMapper = DBMapper.create(Property.class);
    private HashMap<String, Property> properties = new HashMap<String, Property>();

    public void load() {
        try {
            ResultSet res = app.getDbManager().executeQuery("select * from settings");
            while (res.next()) {
                Property p = new Property();
                propertyDBMapper.load(p, res);

                switch (p.getPropertyType()) {
                    case INTEGER:
                        p.setValue(res.getInt("value"));
                        break;
                    case BOOLEAN:
                        p.setValue(res.getBoolean("value"));
                        break;
                    case DOUBLE:
                        p.setValue(res.getDouble("value"));
                        break;
                }

                properties.put(p.getKey(), p);
            }
            res.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        for (Property p : properties.values()) {
            propertyDBMapper.save(p);
        }
    }

    public Set<String> getKeys() {
        return properties.keySet();
    }

    public PropertyType getType(String key) {
        Property p = properties.get(key);
        if (p != null)
            return p.getPropertyType();
        else
            return null;
    }

    public Object getValue(String key) {
        Property p = properties.get(key);
        if (p != null)
            return p.getValue();
        else
            return null;
    }

    public void setValue(String key, Object value, PropertyType type) {
        Property p = properties.get(key);
        if (p != null) {
            p.setValue(value);
        } else {
            p = new Property();
            p.setKey(key);
            p.setValue(value);
            p.setPropertyType(type);
            properties.put(key, p);
        }
    }

    public String getProperty(String key) {
        return (String) getValue(key);
    }

    public void setProperty(String key, String value) {
        setValue(key, value, PropertyType.STRING);
    }


    public Integer getInt(String key, int def) {
        Integer val = (Integer) getValue(key);
        return val != null ? val : def;
    }

    public void setInt(String key, int value) {
        setValue(key, value, PropertyType.INTEGER);
    }

    public Double getDouble(String key, double def) {
        Double val = (Double) getValue(key);
        return val != null ? val : def;
    }

    public void setDouble(String key, double value) {
        setValue(key, value, PropertyType.DOUBLE);
    }

    public Boolean getBoolean(String key, boolean def) {
        Boolean val = (Boolean) getValue(key);
        return val != null ? val : def;
    }

    public void setBoolean(String key, boolean value) {
        setValue(key, value, PropertyType.BOOLEAN);
    }

    public Object[] getArray(String key) {
        String value = (String) getValue(key);
        if (value == null)
            return null;
        HashMap<String, Object> map = Util.loadFields(value);
        Object[] res = new Object[map.size()];
        for (Map.Entry<String, Object> s : map.entrySet()) {
            res[Integer.valueOf(s.getKey())] = s.getValue();
        }

        return res;
    }

    public void setArray(String key, Object... a) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        for (int i = 0; i < a.length; i++) {
            map.put(String.valueOf(i), a[i]);
        }

        setValue(key, Util.getFields(map), PropertyType.ARRAY);
    }

    public HashMap<String, Object> getMap(String key) {
        return Util.loadFields((String) getValue(key));
    }

    public void setMap(String key, HashMap<String, Object> value) {
        setValue(key, Util.getFields(value), PropertyType.MAP);
    }

    private void setMap(String key, HashMap<String, Object> value, PropertyType type) {
        setValue(key, Util.getFields(value), type);
    }

    public Color getColor(String key) {
        HashMap<String, Object> map = getMap(key);
        if (map == null)
            return null;

        return new Color(
                (Integer) map.get("r"),
                (Integer) map.get("g"),
                (Integer) map.get("b"));
    }

    public void setColor(String key, Color value) {
        if (value == null)
            return;
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("r", value.getRed());
        map.put("g", value.getGreen());
        map.put("b", value.getBlue());

        setMap(key, map, PropertyType.COLOR);
    }

    public Font getFont(String key) {
        HashMap<String, Object> map = getMap(key);
        if (map == null)
            return null;

        return new Font(
                (String) map.get("name"),
                (Integer) map.get("style"),
                (Integer) map.get("size"));
    }

    public void setFont(String key, Font value) {
        if (value == null)
            return;
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("name", value.getName());
        map.put("style", value.getStyle());
        map.put("size", value.getSize());

        setMap(key, map, PropertyType.FONT);
    }

    public Rectangle getRectangle(String key) {
        HashMap<String, Object> map = getMap(key);
        if (map == null)
            return null;

        return new Rectangle(
                (Integer) map.get("x"),
                (Integer) map.get("y"),
                (Integer) map.get("width"),
                (Integer) map.get("height"));
    }

    public void setRectangle(String key, Rectangle value) {
        if (value == null)
            return;
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("x", (int) value.getX());
        map.put("y", (int) value.getY());
        map.put("width", (int) value.getWidth());
        map.put("height", (int) value.getHeight());

        setMap(key, map, PropertyType.RECTANGLE);
    }

    public static void main(String[] args) {
//        DBManager dbManager = DBManager.getInstance();
//        dbManager.runScript("defaultSettings.sql");
//        dbManager.runScript("install.sql");

        Configuration conf = new Configuration();
        conf.load();

//        System.out.println(conf.getColor("color"));
//        System.out.println(conf.getFont("font"));
//        System.out.println(conf.getRectangle("rectangle"));
//        conf.setColor("color", Color.MAGENTA);
//        conf.setFont("font", Font.decode("Arial-plain-16"));
//        conf.setRectangle("rectangle", new Rectangle(12, 23, 234, 345));

//        conf.setInt("value1", 15);
//        conf.setInt("intValue", 10);
//        conf.setBoolean("boolValue", true);
//        conf.setDouble("doubleValue", 4.654);
//        conf.setValue("stringValue", "hello");
//        conf.setArray("arr", 10, "string", 127);

        conf.save();

        System.out.println("");

//        dbManager.closeConnection();
    }

    @Entity("settings")
    public class Property {
        @Id
        private int id = -1;

        @Column
        private String key;
        @Column
        private Object value;
        @Column
        private String type;

        private PropertyType propertyType;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public String getType() {
            return propertyType.name();
        }

        public void setType(String type) {
            this.type = type;
            this.propertyType = PropertyType.valueOf(type);
        }

        public PropertyType getPropertyType() {
            return propertyType;
        }

        public void setPropertyType(PropertyType propertyType) {
            this.propertyType = propertyType;
            this.type = propertyType.name();
        }
    }
}
