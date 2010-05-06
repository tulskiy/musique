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

package com.tulskiy.musique.db;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * @Author: Denis Tulskiy
 * @Date: Feb 1, 2010
 */
public class AnnotationMapper<T> {
    private HashMap<String, Property> properties = new HashMap<String, Property>();
    private Class<T> mainClass;
    private Property id;

    public AnnotationMapper(Class<T> mainClass) {
        this.mainClass = mainClass;
    }

    private void loadFields() {
        for (Field field : mainClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class)) {
                Property prop = createProperty(field);
                properties.put(prop.name, prop);
            }

            if (field.isAnnotationPresent(Id.class)) {
                id = createProperty(field);
            }
        }
    }

    private Property createProperty(Field field) {
        Property p = new Property();
        p.name = field.getName();
        String propertyName = p.name.substring(0, 1).toUpperCase() +
                p.name.substring(1);

        try {
            if (field.getType() == boolean.class)
                p.getter = mainClass.getMethod("is" + propertyName);
            else
                p.getter = mainClass.getMethod("get" + propertyName);
            p.setter = mainClass.getMethod("set" + propertyName, field.getType());
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        return p;
    }

    public T newInstance() {
        try {
            return mainClass.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    public T copyOf(T src) {
        T dst = newInstance();

        for (String name : properties.keySet()) {
            set(dst, name, get(src, name));
        }

        return dst;
    }

    public Object get(T o, String name) {
        Property prop = properties.get(name);

        if (prop != null) {
            try {
                return prop.getter.invoke(o);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public void set(T o, String name, Object value) {
        Property prop = properties.get(name);

        if (prop != null) {
            try {
                prop.setter.invoke(o, value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    public int getId(T o) {
        try {
            return (Integer) id.getter.invoke(o);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public void setId(T o, int value) {
        try {
            id.setter.invoke(o, value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public class Property {
        public String name;
        public Method getter;
        public Method setter;
    }
}
