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

package com.tulskiy.musique.gui;

import com.tulskiy.musique.audio.player.Player;
import com.tulskiy.musique.audio.player.PlayerEvent;
import com.tulskiy.musique.audio.player.PlayerListener;
import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.system.Application;
import com.tulskiy.musique.system.Configuration;
import com.tulskiy.musique.util.Util;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTextPaneUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Scanner;
import java.util.logging.Logger;

/**
 * Author: Denis Tulskiy
 * Date: Jul 20, 2010
 */
public class LyricsPanel extends JPanel {
    private static Application app = Application.getInstance();
    private static Configuration config = app.getConfiguration();
    private static final String searchURL = "http://www.lyricsplugin.com/winamp03/plugin/?";
    private static Logger logger = Logger.getLogger("musique");

    public LyricsPanel() {
        setLayout(new BorderLayout());
        final JTextPane textPane = new TextPane();

        final Player player = app.getPlayer();
        player.addListener(new PlayerListener() {
            @Override
            public void onEvent(PlayerEvent e) {
                switch (e.getEventCode()) {
                    case FILE_OPENED:
                        new Thread(new Search(textPane, player.getTrack())).start();
                }
            }
        });

        add(new JScrollPane(textPane), BorderLayout.CENTER);
    }

    class TextPane extends JTextPane {
        public final Style artistStyle;
        public final Style titleStyle;

        public TextPane() {
            super();
            Style style = getStyle("default");
            StyleConstants.setAlignment(style, StyleConstants.ALIGN_CENTER);
            artistStyle = addStyle("artist", null);
            titleStyle = addStyle("title", null);
            setEditable(false);
            setOpaque(true);
            setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            updateUI();
        }

        @Override
        public void updateUI() {
            setUI(new BasicTextPaneUI());
            Color background = config.getColor("gui.color.background", null);
            if (background != null) {
                setBackground(background);
            }

            Color text = config.getColor("gui.color.text", null);
            if (text != null)
                setForeground(text);

            Color selection = config.getColor("gui.color.selection", null);
            if (selection != null) {
                setSelectionColor(selection);
                setSelectedTextColor(Util.getContrastColor(selection));
            }

            Font font = config.getFont("gui.font.default", null);
            if (font != null) {
                setFont(font);
                if (artistStyle != null)
                    StyleConstants.setFontSize(artistStyle, font.getSize() + 4);
                if (titleStyle != null)
                    StyleConstants.setFontSize(titleStyle, font.getSize() + 2);
            }
        }
    }

    class Search implements Runnable {
        private JTextPane textPane;
        private Track track;

        Search(JTextPane textPane, Track track) {
            this.textPane = textPane;
            this.track = track;
        }

        @Override
        public void run() {
            String artist = track.getArtist();
            String title = track.getTitle();
            if (track != null && !Util.isEmpty(artist) && !Util.isEmpty(title)) {
                textPane.setText("");
                StyledDocument doc = textPane.getStyledDocument();
                try {
                    doc.insertString(doc.getLength(), artist + "\n", textPane.getStyle("artist"));
                    doc.insertString(doc.getLength(), title + "\n\n", textPane.getStyle("title"));

                    URL search = new URL(searchURL +
                                         "artist=" + URLEncoder.encode(artist, "utf8") +
                                         "&title=" + URLEncoder.encode(title, "utf8"));
                    logger.fine("Searching for lyrics, url: " + URLDecoder.decode(search.toString(), "utf8"));
                    URLConnection conn = search.openConnection();

                    Scanner fi = new Scanner(conn.getInputStream(), "utf-8");
                    while (fi.hasNextLine()) {
                        String s = fi.nextLine();
                        if (s.startsWith("<div id=\"lyrics\">")) {
                            while (!(s = fi.nextLine()).equals("</div>")) {
                                s = s.replaceAll("<.*?>", "") + "\n";
                                doc.insertString(doc.getLength(), s, null);
                            }
                        }
                    }
                } catch (BadLocationException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}