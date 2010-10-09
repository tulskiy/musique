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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: Denis Tulskiy
 * Date: Jul 20, 2010
 */
public class LyricsPanel extends JPanel {
    private static Application app = Application.getInstance();
    private static Configuration config = app.getConfiguration();
    private static Logger logger = Logger.getLogger("musique");
    private final File lyricsDir = new File(app.CONFIG_HOME, "lyrics");
    private final Timer timer;
    private Search search;

    public LyricsPanel() {
        setLayout(new BorderLayout());
        final JTextPane textPane = new TextPane();

        final Player player = app.getPlayer();
        timer = new Timer(200, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Track track = player.getTrack();

                try {
                    textPane.setText("");
                    StyledDocument doc = textPane.getStyledDocument();
                    doc.insertString(doc.getLength(), track.getArtist() + "\n", textPane.getStyle("artist"));
                    doc.insertString(doc.getLength(), track.getTitle() + "\n\n", textPane.getStyle("title"));
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }

                if (search != null && !search.isDone())
                    search.cancel(true);

                search = new Search(textPane, track);
                search.execute();
                timer.stop();
            }
        });

        player.addListener(new PlayerListener() {
            @Override
            public void onEvent(PlayerEvent e) {
                switch (e.getEventCode()) {
                    case FILE_OPENED:
                        timer.restart();
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
            super.updateUI();
            Color oldSelectionColor = getSelectionColor();
            setUI(new BasicTextPaneUI());
            Color background = config.getColor("gui.color.background", null);
            if (background != null) {
                setBackground(background);
            } else {
                setBackground(Color.white);
            }

            Color text = config.getColor("gui.color.text", null);
            if (text != null)
                setForeground(text);
            else
                setForeground(null);

            Color selection = config.getColor("gui.color.selection", null);
            if (selection != null) {
                setSelectionColor(selection);
            } else {
                setSelectionColor(oldSelectionColor);
            }

            setSelectedTextColor(Util.getContrastColor(getSelectionColor()));

            Font font = config.getFont("gui.font.default", null);
            if (font != null) {
                setFont(font);
                if (artistStyle != null)
                    StyleConstants.setFontSize(artistStyle, font.getSize() + 4);
                if (titleStyle != null)
                    StyleConstants.setFontSize(titleStyle, font.getSize() + 2);
            } else {
                setFont(null);
            }
        }
    }

    class Search extends SwingWorker<String, Object> {
        private final JTextPane textPane;
        private Track track;

        Search(JTextPane textPane, Track track) {
            this.textPane = textPane;
            this.track = track;
        }

        @Override
        protected String doInBackground() throws Exception {
            String artist = track.getArtist();
            String title = track.getTitle();
            if (track != null && !Util.isEmpty(artist) && !Util.isEmpty(title)) {
                try {
                    Scanner fi;
                    File file = new File(lyricsDir, artist + " - " + title + ".txt");
                    StringBuilder result = new StringBuilder();
                    if (file.exists()) {
                        logger.fine("Loading lyrics from file: " + file.getName());
                        fi = new Scanner(file);
                        while (fi.hasNextLine())
                            result.append(fi.nextLine()).append("\n");
                    } else if (config.getBoolean("lyrics.searchOnline", true)) {
                        searchLyrics(artist, title, result);

                        String text = result.toString().trim();
                        if (text.length() > 0) {
                            //noinspection ResultOfMethodCallIgnored
                            lyricsDir.mkdirs();
                            PrintWriter writer = new PrintWriter(file);
                            writer.print(text);
                            writer.close();
                        }
                    }

                    return result.toString();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void done() {
            try {
                String result = get();
                if (result != null) {
                    textPane.setText("");
                    StyledDocument doc = textPane.getStyledDocument();
                    doc.insertString(doc.getLength(), track.getArtist() + "\n", textPane.getStyle("artist"));
                    doc.insertString(doc.getLength(), track.getTitle() + "\n\n", textPane.getStyle("title"));
                    doc.insertString(doc.getLength(), Util.htmlToString(result), null);
                    textPane.setCaretPosition(0);
                }
            } catch (Exception ignored) {
            }
        }

        //        private void searchLyrics(String artist, String title, StringBuilder sb) throws IOException {
//            Scanner fi;
//            URL search = new URL(searchURL +
//                                 "artist=" + URLEncoder.encode(artist, "utf8") +
//                                 "&title=" + URLEncoder.encode(title, "utf8"));
//            logger.fine("Searching for lyrics at url: " + URLDecoder.decode(search.toString(), "utf8"));
//            URLConnection conn = search.openConnection();
//            fi = new Scanner(conn.getInputStream(), "utf-8");
//            while (fi.hasNextLine()) {
//                String s = fi.nextLine();
//                if (s.startsWith("<div id=\"lyrics\">")) {
//                    while (!(s = fi.nextLine()).equals("</div>")) {
//                        sb.append(Util.htmlToString(s)).append("\n");
//                    }
//                }
//            }
//        }

        private void searchLyrics(String artist, String title, StringBuilder sb) throws IOException {
            try {
//                String search = "http://lyrics.wikia.com/Special:Search?search=";
//                search += URLEncoder.encode(artist, "utf-8");
//                search += "+";
//                search += URLEncoder.encode(title, "utf-8");
//
//                URL searchURL = new URL(search);
//                Scanner scan = new Scanner(searchURL.openStream());
//                StringBuilder content = new StringBuilder();
//                while (scan.hasNextLine())
//                    content.append(scan.nextLine());
//
//                Matcher matcher = Pattern.compile("http://lyrics.wikia.com/wiki/[^\"]*").matcher(content);
//                if (!matcher.find()) {
//                    return;
//                }
                String search = "http://lyrics.wikia.com/";
                artist = Util.capitalize(artist);
                title = Util.capitalize(title);
                search += URLEncoder.encode(artist, "utf-8");
                search += ":";
                search += URLEncoder.encode(title, "utf-8");

                URL url = new URL(search);
                InputStream is = url.openStream();
                Scanner fi = new Scanner(is);
                while (fi.hasNextLine()) {
                    String s = fi.nextLine();
                    if (s.startsWith("<div class='lyricbox'")) {
                        int index = s.lastIndexOf("</div>");
                        if (index > 0 && s.length() > 6)
                            s = s.substring(index);
                        s = s.replaceAll("<br />", "\n");
                        s = s.replaceAll("(<!--)|(<.*?>)", "");
                        Matcher m = Pattern.compile("&#(\\d+);").matcher(s);
                        m.reset();
                        StringBuffer buffer = new StringBuffer();
                        while (m.find()) {
                            String group = m.group(1);
                            char ch = (char) (int) Integer.valueOf(group);
                            m.appendReplacement(buffer, String.valueOf(ch));
                        }
                        m.appendTail(buffer);
                        sb.append(buffer);
                    }
                }
                fi.close();
            } catch (IOException e) {
//                e.printStackTrace();
            }
        }
    }
}