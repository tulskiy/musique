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

/**
 * @Author: Denis Tulskiy
 * @Date: 02.10.2008
 */

import com.sun.org.apache.xerces.internal.parsers.SAXParser;
import com.tulskiy.musique.util.SettingsBuilder;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.beans.ExceptionListener;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.util.HashMap;
import java.util.Stack;
import java.util.logging.Logger;

public class Settings {
    private static HashMap<String, Object> settings;

    private Settings() {

    }

    public static void loadSettings() {
        try {
            File f = new File("resources/settings.xml");
            if (!f.exists()) {
                SettingsBuilder.buildSettings();
            }
            XMLDecoder reader = new XMLDecoder(new FileInputStream(f));
            Object o;
            while ((o = reader.readObject()) == null) {
                SettingsBuilder.buildSettings();
            }
            settings = (HashMap<String, Object>) o;
            Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static int getInt(String key) {
        Object o = get(key);
        if (o != null && o instanceof Integer)
            return (Integer) get(key);
        else
            return 0;
    }

    public static float getFloat(String key) {
        Object o = get(key);
        if (o != null && o instanceof Float)
            return (Float) get(key);
        else
            return 0.0f;
    }

    public static void saveSettings() {
        try {
            XMLEncoder writer = new XMLEncoder(new FileOutputStream("resources/settings.xml"));
            writer.setExceptionListener(new ExceptionListener() {
                public void exceptionThrown(Exception e) {
                    System.out.println("Error saving settings");
                }
            });
            writer.writeObject(settings);
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static JMenuBar loadMenu(ActionListener listener) {
        return new MenuLoader().load(listener);
    }

    public static Object get(String key) {
        return settings.get(key);
    }

    public static void set(String key, Object value) {
        settings.put(key, value);
    }

    public static void main(String[] args) {

    }

    private static class MenuLoader extends DefaultHandler {
        private Stack<JMenu> stack;
        private ButtonGroup buttonGroup;
        private JMenuBar bar;
        private ActionListener listener;

        public MenuLoader() {
            bar = new JMenuBar();
            stack = new Stack<JMenu>();
        }

        public JMenuBar load(ActionListener listener) {
            this.listener = listener;
            XMLReader reader = new SAXParser();
            reader.setContentHandler(this);
            try {
                reader.parse(new InputSource("resources/mainMenu.xml"));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            }

            return bar;
        }

        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (localName.equals("menu") || localName.equals("subMenu")) {
                stack.push(new JMenu(attributes.getValue("name")));
            } else if (localName.equals("buttonGroup")) {
                buttonGroup = new ButtonGroup();
            } else if (localName.equals("menuItem")) {
                if (attributes.getValue("name").equals("separator")) {
                    stack.peek().addSeparator();
                } else {
                    JMenuItem item;
                    if (buttonGroup != null) {
                        item = new JRadioButtonMenuItem();
                        buttonGroup.add(item);
                    } else {
                        item = new JMenuItem();
                    }
                    item.setName(attributes.getValue("name"));
                    item.setText(attributes.getValue("label"));
                    item.addActionListener(listener);
                    String s = attributes.getValue("hotkey");
                    if (s != null) item.setAccelerator(KeyStroke.getKeyStroke(s));
                    stack.peek().add(item);
                }
            }

        }

        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (localName.equals("menu")) {
                bar.add(stack.pop());
            } else if (localName.equals("subMenu")) {
                JMenu m = stack.pop();
                stack.peek().add(m);
            } else if (localName.equals("buttonGroup")) {
                buttonGroup = null;
            }
        }
    }
}
