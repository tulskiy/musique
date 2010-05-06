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

import com.tulskiy.musique.playlist.Song;
import com.tulskiy.musique.system.Application;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: Denis Tulskiy
 * @Date: Jan 5, 2010
 */
public class DBMapper<T> {
    private DBManager dbManager = Application.getInstance().getDbManager();

    private PreparedStatement insert;
    private PreparedStatement update;
    private ArrayList<Property> properties = new ArrayList<Property>();
    private Property tableID;
    private String table;
    private Class<T> mainClass;

    public static <T> DBMapper<T> create(Class<T> c) {
        return new DBMapper<T>(c);
    }

    public DBMapper(Class<T> mainClass) {
        System.out.println("Mapper for class" + mainClass);
        this.mainClass = mainClass;
        loadFields();

        insert = dbManager.prepareStatement(createInsert());
        update = dbManager.prepareStatement(createUpdate());
    }

    private void loadFields() {
        Entity annotation = mainClass.getAnnotation(Entity.class);
        table = annotation.value();

        for (Field field : mainClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class)) {
                Property prop = createProperty(field);
                properties.add(prop);
            }

            if (field.isAnnotationPresent(Id.class)) {
                tableID = createProperty(field);
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

    public void loadAll(List<T> list) {
        loadAll("select * from " + table, list);
    }

    public void loadAll(String query, List<T> list) {
        try {
            ResultSet resultSet = dbManager.executeQuery(query);

            while (resultSet.next()) {
                T o = newInstance();
                load(o, resultSet);
                list.add(o);
            }

            resultSet.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void load(T o, ResultSet res) {
        try {
            for (Property prop : properties) {
                prop.setter.invoke(o, res.getObject(prop.name));
            }

            tableID.setter.invoke(o, res.getInt(tableID.name));
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void save(T o) {
        try {
            Integer id = (Integer) tableID.getter.invoke(o);
            PreparedStatement st;
            if (id == -1) {
                st = insert;
            } else {
                st = update;
                st.setInt(st.getParameterMetaData().getParameterCount(), id);
            }

            for (int i = 0; i < properties.size(); i++) {
                st.setObject(i + 1, properties.get(i).getter.invoke(o));
            }

            st.executeUpdate();

            if (id == -1) {
                ResultSet resultSet = st.getGeneratedKeys();
                resultSet.next();
                tableID.setter.invoke(o, resultSet.getInt(1));
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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

        try {
            for (Property prop : properties) {
                prop.setter.invoke(
                        dst,
                        prop.getter.invoke(src));
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return dst;
    }

    @SuppressWarnings({"ForLoopReplaceableByForEach"})
    private String createInsert() {
        StringBuilder sb = new StringBuilder("insert into ");
        sb.append(table).append(" (");
        for (Property p : properties)
            if (p != tableID)
                sb.append(p.name).append(",");
        sb.deleteCharAt(sb.length() - 1);
        sb.append(") values ");

        for (int i = 0; i < properties.size(); i++)
            sb.append("?,");
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    private String createUpdate() {
        StringBuilder sb = new StringBuilder("update ");
        sb.append(table).append(" set ");
        for (Property p : properties) {
            sb.append(p.name).append("=?,");
        }

        sb.deleteCharAt(sb.length() - 1);
        sb.append(" where ").append(tableID.name).append("=?");

        return sb.toString();
    }

    public void delete(T o) {
        try {
            dbManager.executeUpdate("delete from " + table + " where " +
                    tableID.name + "=" + tableID.getter.invoke(o));
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
