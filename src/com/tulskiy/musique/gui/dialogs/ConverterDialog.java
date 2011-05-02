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

package com.tulskiy.musique.gui.dialogs;

import com.tulskiy.musique.audio.Converter;
import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.system.Application;
import com.tulskiy.musique.system.Configuration;
import com.tulskiy.musique.util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.List;

/**
 * Author: Denis Tulskiy
 * Date: Jul 26, 2010
 */
public class ConverterDialog extends JDialog {
    private Application app = Application.getInstance();
    private Configuration config = app.getConfiguration();
    private HashMap<String, JComponent> coders = new LinkedHashMap<String, JComponent>();
    private HashMap<String, String> formatToCoder = new HashMap<String, String>() {{
        put("WAV", "wav");
        put("OGG Vorbis", "ogg");
        put("WavPack", "wv");
        put("Monkey's Audio", "ape");

    }};

    public ConverterDialog(final JComponent owner, final List<Track> tracks) {
        super(SwingUtilities.windowForComponent(owner), "Convert Files", ModalityType.MODELESS);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        coders.put("WAV", null);
        coders.put("OGG Vorbis", createVorbisSettings());
        coders.put("WavPack", createWavpackSettings());
        coders.put("Monkey's Audio", createAPESettings());

        final JComboBox encoder = new JComboBox(coders.keySet().toArray());

        final Box box = Box.createVerticalBox();
        box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        JPanel format = new JPanel(new BorderLayout());
        format.add(encoder, BorderLayout.CENTER);
        format.setBorder(BorderFactory.createTitledBorder("Format"));
        final JButton coderSettings = new JButton("...");
        final JDialog comp = this;
        coderSettings.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String key = (String) encoder.getSelectedItem();
                showSettingsDialog(key, comp);
            }
        });

        format.add(coderSettings, BorderLayout.EAST);
        encoder.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                String key = (String) e.getItem();
                coderSettings.setEnabled(coders.get(key) != null);
            }
        });
        encoder.setSelectedIndex(-1);
        String codec = config.getString("converter.encoder", "wav");
        for (Map.Entry<String, String> entry : formatToCoder.entrySet()) {
            if (entry.getValue().equals(codec))
                encoder.setSelectedItem(entry.getKey());
        }

        JPanel folder = new JPanel(new GridLayout(2, 2));
        folder.setBorder(BorderFactory.createTitledBorder("Path"));

        final JRadioButton pathSource = new JRadioButton("Source track folder");
        folder.add(pathSource);
        boolean b = config.getBoolean("converter.saveToSourceFolder", true);
        pathSource.setSelected(b);
        JRadioButton pathSpecify = new JRadioButton("Specify folder");
        pathSpecify.setSelected(!b);
        folder.add(pathSpecify);
        ButtonGroup g1 = new ButtonGroup();
        g1.add(pathSource);
        g1.add(pathSpecify);
        folder.add(new JLabel());
        final PathChooser path = new PathChooser(config.getString("converter.path", ""));
        folder.add(path);

        JPanel output = new JPanel(new GridLayout(3, 2));
        output.setBorder(BorderFactory.createTitledBorder("Output files"));
        output.add(new JLabel("When file exists"));
        final JComboBox whenExists = new JComboBox(new String[]{"Ask", "Skip", "Overwrite"});
        whenExists.setSelectedItem(config.getString("converter.actionWhenExists", "Ask"));
        output.add(whenExists);
        output.add(new JLabel("Filename format"));
        final JTextField fileNameFormat = new JTextField();
        fileNameFormat.setText(config.getString("converter.fileNameFormat", "%fileName%"));
        output.add(fileNameFormat);
        final JCheckBox merge = new JCheckBox("Merge files into one image");
        merge.setSelected(config.getBoolean("converter.merge", false));
        output.add(merge);

        box.add(format);
        box.add(folder);
        box.add(output);
        box.add(Box.createVerticalGlue());

        Box buttons = Box.createHorizontalBox();
        buttons.add(Box.createHorizontalGlue());
        JButton ok = new JButton("  OK  ");
        buttons.add(ok);
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String codec = (String) encoder.getSelectedItem();
                config.setString("converter.encoder", formatToCoder.get(codec));
                config.setBoolean("converter.saveToSourceFolder", pathSource.isSelected());
                config.setString("converter.path", path.getPath());
                config.setString("converter.actionWhenExists", (String) whenExists.getSelectedItem());
                config.setString("converter.fileNameFormat", fileNameFormat.getText());
                config.setBoolean("converter.merge", merge.isSelected());
                setVisible(false);
                dispose();

                ProgressDialog progress = new ProgressDialog(owner, "Converting files");
                progress.show(new ConvertTask(tracks));
            }
        });
        buttons.add(Box.createHorizontalStrut(3));
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                dispose();
            }
        });
        buttons.add(cancel);
        buttons.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setLayout(new BorderLayout());
        add(box, BorderLayout.NORTH);
        add(buttons, BorderLayout.PAGE_END);
        setSize(500, 400);
        setLocationRelativeTo(SwingUtilities.windowForComponent(owner));
    }

    private void showSettingsDialog(String key, JDialog owner) {
        final JDialog dialog = new JDialog(owner, true);
        dialog.setLayout(new BorderLayout());
        final JComponent comp = coders.get(key);
        setTitle(comp.getName() + " Settings");
        comp.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        dialog.add(comp, BorderLayout.CENTER);

        Box buttons = Box.createHorizontalBox();
        buttons.add(Box.createHorizontalGlue());
        JButton ok = new JButton("  OK  ");
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                comp.firePropertyChange("accept", 1, 2);
                dialog.setVisible(false);
                dialog.dispose();
            }
        });
        buttons.add(ok);
        buttons.add(Box.createHorizontalStrut(3));
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.setVisible(false);
                dialog.dispose();
            }
        });
        buttons.add(cancel);
        buttons.add(Box.createHorizontalStrut(10));

        dialog.add(buttons, BorderLayout.PAGE_END);
        dialog.setSize(500, 250);
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }

    private JComponent createWavpackSettings() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setName("WavPack Encoder");
        JPanel misc = new JPanel(new GridLayout(6, 2));
        misc.add(new JLabel("Quality"));
        final JComboBox quality = new JComboBox(new String[]{
                "fast", "normal", "high", "very high"
        });
        quality.setSelectedItem(config.getString("wavpack.encoder.mode", "normal"));
        misc.add(quality);
        final JCheckBox hybrid = new JCheckBox("Enable hybrid mode");
        misc.add(hybrid);
        hybrid.setSelected(config.getBoolean("wavpack.encoder.hybrid.enable", false));
        final JCheckBox maxHybrid = new JCheckBox("Maximum hybrid compression");
        misc.add(maxHybrid);
        maxHybrid.setSelected(config.getBoolean("wavpack.encoder.hybrid.wvc.optimize", false));
        final JCheckBox wvc = new JCheckBox("Create correction file");
        wvc.setSelected(config.getBoolean("wavpack.encoder.hybrid.wvc", false));
        misc.add(wvc);
        misc.add(new JLabel());
        misc.add(new JLabel("Bitrate, bits/sample"));
        final JSpinner bitrate = new JSpinner(
                new SpinnerNumberModel(4.0, 2.0, 16.0, 0.1));
        bitrate.setValue((double) config.getFloat("wavpack.encoder.hybrid.bitrate", 4f));
        misc.add(bitrate);
        misc.add(new JLabel("Noize shape override"));
        final JSpinner noise = new JSpinner(new SpinnerNumberModel(0, -1.0, 1.0, 0.1));
        noise.setValue((double) config.getFloat("wavpack.encoder.hybrid.noiseShape", 0));
        misc.add(noise);

        ItemListener hybridListener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                boolean b = hybrid.isSelected();
                noise.setEnabled(b);
                wvc.setEnabled(b);
                maxHybrid.setEnabled(b);
                bitrate.setEnabled(b);
            }
        };
        hybrid.addItemListener(hybridListener);
        hybridListener.itemStateChanged(null);

        panel.addPropertyChangeListener("accept", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                config.setString("wavpack.encoder.mode", (String) quality.getSelectedItem());
                config.setBoolean("wavpack.encoder.hybrid.enable", hybrid.isSelected());
                if (hybrid.isSelected()) {
                    config.setFloat("wavpack.encoder.hybrid.bitrate", ((Number) bitrate.getValue()).floatValue());
                    config.setBoolean("wavpack.encoder.hybrid.wvc", wvc.isSelected());
                    config.setBoolean("wavpack.encoder.hybrid.wvc.optimize", maxHybrid.isSelected());
                    config.setFloat("wavpack.encoder.hybrid.noiseShape", ((Number) noise.getValue()).floatValue());
                }
            }
        });
        panel.add(misc, BorderLayout.PAGE_START);
        return panel;
    }

    private JComponent createVorbisSettings() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setName("Ogg Vorbis Encoder");
        panel.add(new JLabel("Quality"), BorderLayout.PAGE_START);
        final JSlider slider = new JSlider(-1, 10);
        slider.setMajorTickSpacing(1);
        slider.setPaintLabels(true);
        slider.setPaintTicks(true);
        slider.setSnapToTicks(true);
        slider.setValue((int) (config.getFloat("vorbis.encoder.quality", .3f) * 10));
        Box box = Box.createVerticalBox();
        box.add(slider);
        box.add(Box.createVerticalGlue());
        panel.add(box, BorderLayout.CENTER);

        panel.addPropertyChangeListener("accept", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                config.setFloat("vorbis.encoder.quality", slider.getValue() / 10f);
            }
        });
        return panel;
    }

    private JComponent createAPESettings() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setName("Monkey's Audio Encoder");
        panel.add(new JLabel("Compression Level"), BorderLayout.PAGE_START);
        final JSlider slider = new JSlider(1, 5);
        slider.setMajorTickSpacing(1);
        slider.setPaintLabels(true);
        slider.setPaintTicks(true);
        slider.setSnapToTicks(true);
        Hashtable<Integer, JLabel> labels = new Hashtable<Integer, JLabel>();
        labels.put(1, new JLabel("Fast"));
        labels.put(2, new JLabel("Normal"));
        labels.put(3, new JLabel("High"));
        labels.put(4, new JLabel("Extra High"));
        labels.put(5, new JLabel("Insane"));
        slider.setLabelTable(labels);
        slider.setValue(config.getInt("ape.encoder.level", 2000) / 1000);
        Box box = Box.createVerticalBox();
        box.add(slider);
        box.add(Box.createVerticalGlue());
        panel.add(box, BorderLayout.CENTER);

        panel.addPropertyChangeListener("accept", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                config.setInt("ape.encoder.level", slider.getValue() * 1000);
            }
        });
        return panel;
    }

    class ConvertTask extends Task {
        Converter converter = new Converter();
        private List<Track> tracks;
        private Formatter formatter;
        private StringBuilder sb;

        ConvertTask(List<Track> tracks) {
            this.tracks = tracks;
            sb = new StringBuilder();
            formatter = new Formatter(sb);
        }

        @Override
        public String getStatus() {
            try {
                Track track = converter.getTrack();
                String status = "Input: ";
                if (track.getTrackData().isFile())
                    status += track.getTrackData().getFile().getAbsolutePath();
                else
                    status += track.getTrackData().getLocation().toString();

                status += "\nOutput: ";
                status += converter.getOutput().getAbsolutePath();
                return status;
            } catch (Exception ignored) {
            }
            return null;
        }

        @Override
        public void abort() {
            converter.stop();
        }

        @Override
        public void start() {
            converter.convert(tracks);
        }

        @Override
        public boolean isIndeterminate() {
            return converter.getTotalSamples() < 0;
        }

        @Override
        public float getProgress() {
            return (float) converter.getCurrentSample() / converter.getTotalSamples();
        }

        @Override
        public String getTitle() {
            String elapsed = Util.formatSeconds(
                    converter.getElapsed() / 1000f, 0);
            String estimated = Util.samplesToTime(
                    (long) converter.getEstimated(),
                    converter.getTrack().getTrackData().getSampleRate(), 0);
            sb.setLength(0);
            formatter.format("Converting. Elapsed: %s Estimated: %s Speed: %.2fx",
                    elapsed, estimated, converter.getSpeed());
            return sb.toString();
        }
    }
}
