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

/**
 * @Author: Denis Tulskiy
 * @Date: Oct 30, 2009
 */
package com.tulskiy.musique.system;

import com.tulskiy.musique.audio.AudioFileReader;
import com.tulskiy.musique.audio.Scrobbler;
import com.tulskiy.musique.audio.player.io.AudioOutput;
import com.tulskiy.musique.audio.player.Player;
import com.tulskiy.musique.gui.MainWindow;
import com.tulskiy.musique.playlist.PlaybackOrder;
import com.tulskiy.musique.playlist.PlaylistManager;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import javax.swing.*;
import javax.swing.plaf.metal.MetalFileChooserUI;
import javax.swing.plaf.metal.MetalIconFactory;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

public class Application {
    private static Application ourInstance = new Application();
    private final Logger logger = Logger.getLogger("musique");

    private Player player;
    private Configuration configuration;
    private PlaylistManager playlistManager;
    private MainWindow mainWindow;
    public final String VERSION = "Musique 0.2";
    public final File CONFIG_HOME =
            new File(System.getProperty("user.home"), ".musique").getAbsoluteFile();
    private final File configFile = new File(CONFIG_HOME, "config");

    public static Application getInstance() {
        return ourInstance;
    }

    private Application() {
        //noinspection ResultOfMethodCallIgnored
        CONFIG_HOME.mkdirs();

        logger.fine("Using '" + CONFIG_HOME + "' as a home directory");
    }

    public void load() {
        configuration = new Configuration();
        try {
            configuration.load(new FileReader(configFile));
        } catch (FileNotFoundException ignored) {
        }

        if (configuration.getBoolean("system.oneInstance", false)
                && !tryLock()) {
            JOptionPane.showMessageDialog(null, "Only one instance of Musique can be run at a time", VERSION, JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
        player = new Player();
        Scrobbler scrobbler = new Scrobbler();
        scrobbler.start();

        playlistManager = new PlaylistManager();
        playlistManager.loadPlaylists();

        loadSettings();
    }

    private boolean tryLock() {
        try {
            RandomAccessFile randomFile = new RandomAccessFile(new File(CONFIG_HOME, "lock"), "rw");
            FileChannel channel = randomFile.getChannel();
            //we couldn't acquire lock as it is already locked by another program instance)
            if (channel.tryLock() == null)
                return false;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private void loadSettings() {
        System.setProperty("http.agent", "Mozilla/5.001 (windows; U; NT4.0; en-US; rv:1.0) Gecko/25250101");

        AudioOutput audioOutput = player.getAudioOutput();
        audioOutput.setVolume(configuration.getFloat("player.volume", 1));
        String mixer = configuration.getString("player.mixer", null);
        if (mixer != null) {
            Mixer.Info[] infos = AudioSystem.getMixerInfo();
            for (Mixer.Info info : infos) {
                if (info.getName().equals(mixer)) {
                    audioOutput.setMixer(info);
                    break;
                }
            }
        }
        if (configuration.getBoolean("proxy.enabled", false)) {
            System.setProperty("http.proxyHost", configuration.getString("proxy.host", null));
            System.setProperty("http.proxyPort", configuration.getString("proxy.port", null));
        }
        Authenticator.setDefault(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                String user = configuration.getString("proxy.user", null);
                String password = configuration.getString("proxy.password", null);

                if (user != null && password != null)
                    return new PasswordAuthentication(user, password.toCharArray());
                else
                    return null;
            }
        });
        configuration.addPropertyChangeListener("player.stopAfterCurrent", true, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                player.setStopAfterCurrent(configuration.getBoolean("player.stopAfterCurrent", false));
            }
        });
        configuration.addPropertyChangeListener("player.playbackOrder", true, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                int index = configuration.getInt(evt.getPropertyName(), 0);
                player.getPlaybackOrder().setOrder(PlaybackOrder.Order.values()[index]);
            }
        });
        UIManager.put("Slider.paintValue", Boolean.FALSE);
        UIManager.put("FileChooser.readOnly", Boolean.TRUE);
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        Object thumbWidth = configuration.get("gui.thumbWidth");
        if (thumbWidth != null) {
            try {
                UIManager.put("Slider.thumbWidth", Integer.valueOf(thumbWidth.toString()));
            } catch (NumberFormatException ignored) {
            }
        }

        Charset charset = Charset.forName(configuration.getString("tag.defaultEncoding", "windows-1251"));
        AudioFileReader.setDefaultCharset(charset);
        try {
            String laf = configuration.getString("gui.LAF", "");
            if (laf.isEmpty()) {
                laf = "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";
            }
            UIManager.setLookAndFeel(laf);
        } catch (Exception e) {
            System.err.println("Could not load LaF: " + e.getCause());
        }

        configuration.addPropertyChangeListener("gui.LAF", true, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String laf = configuration.getString(evt.getPropertyName(), "");
                if (laf.endsWith("GTKLookAndFeel")) {
                    UIManager.put("FileChooserUI", MetalFileChooserUI.class.getName());
                    UIManager.put("FileChooser.newFolderIcon", MetalIconFactory.getFileChooserNewFolderIcon());
                    UIManager.put("FileChooser.upFolderIcon", MetalIconFactory.getFileChooserUpFolderIcon());
                    UIManager.put("FileChooser.homeFolderIcon", MetalIconFactory.getFileChooserHomeFolderIcon());
                    UIManager.put("FileChooser.detailsViewIcon", MetalIconFactory.getFileChooserDetailViewIcon());
                    UIManager.put("FileChooser.listViewIcon", MetalIconFactory.getFileChooserListViewIcon());
                } else {
                    UIManager.put("FileChooserUI", null);
                }
//                Font defaultFont = new Font("Sans Serif", 0, 11);
//                UIManager.put("defaultFont", defaultFont);
//                UIManager.put("Table.font", defaultFont);
//                UIManager.put("Menu.font", defaultFont);
//                UIManager.put("Button.font", defaultFont);
//                UIManager.put("ComboBox.font", defaultFont);
//                UIManager.put("Tree.font", defaultFont);
//                UIManager.put("CheckBox.font", defaultFont);
//                UIManager.put("TableHeader.font", defaultFont);
//                UIManager.put("PopupMenu.font", defaultFont);
//                UIManager.put("RadioButtonMenuItem.font", defaultFont);
//                UIManager.put("RadioButton.font", defaultFont);
//                UIManager.put("CheckBoxMenuItem.font", defaultFont);
//                UIManager.put("TextField.font", defaultFont);
//                UIManager.put("MenuItem.font", defaultFont);
//                UIManager.put("Label.font", defaultFont);
//                UIManager.put("TitledBorder.font", defaultFont.deriveFont(Font.BOLD));
//                UIManager.put("TabbedPane.font", defaultFont);
//                UIManager.put("TextArea.font", defaultFont);
            }
        });
    }

    private void saveSettings() {
        AudioOutput audioOutput = player.getAudioOutput();
        configuration.setFloat("player.volume", audioOutput.getVolume());
        Mixer.Info mixer = audioOutput.getMixer();
        if (mixer != null)
            configuration.setString("player.mixer", mixer.getName());
        else
            configuration.remove("player.mixer");
        Charset value = AudioFileReader.getDefaultCharset();
        if (value != null)
            configuration.setString("tag.defaultEncoding", value.name());
        else
            configuration.remove("tag.defaultEncoding");
    }

    public void start() {
        try {
            if (mainWindow != null) {
                mainWindow.shutdown();
                mainWindow = null;
            }

            mainWindow = new MainWindow();
            mainWindow.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void exit() {
        player.stop();
        if (mainWindow != null)
            mainWindow.shutdown();
        playlistManager.saveSettings();
        saveSettings();
        try {
            configuration.save(new FileWriter(configFile));
        } catch (IOException e) {
            logger.severe("Could not save configuration to " + configFile);
        }
        System.exit(0);
    }

    public Player getPlayer() {
        return player;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public PlaylistManager getPlaylistManager() {
        return playlistManager;
    }
}
