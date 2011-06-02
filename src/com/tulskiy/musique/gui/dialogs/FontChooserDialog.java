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

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Author: Denis Tulskiy
 * Date: Jul 18, 2010
 */
public class FontChooserDialog extends JDialog {
    private JTextField preview;
    private boolean accepted = false;

    public FontChooserDialog(Component owner, Font font) {
        super(SwingUtilities.getWindowAncestor(owner), "Choose Font", ModalityType.APPLICATION_MODAL);
        setLayout(new BorderLayout());
        setSize(450, 400);
        setLocationRelativeTo(owner);

        final JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        preview = new JTextField("abcdefjhk ABCDEFJHK");
        preview.setPreferredSize(new Dimension(10, 50));

        final JList fontsList = new JList(fonts);
        final JList styleList = new JList(new Object[]{"Regular", "Italic", "Bold", "Bold Italic"});
        final JList sizesList = new JList(new Integer[]{7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18,
                19, 20, 22, 24, 30, 36});

        if (font != null) {
            fontsList.setSelectedValue(font.getName(), true);
            styleList.setSelectedIndex(font.getStyle());
            sizesList.setSelectedValue(font.getSize(), true);
        } else {
            fontsList.setSelectedIndex(0);
            styleList.setSelectedIndex(0);
            sizesList.setSelectedIndex(0);
        }

        ListSelectionListener listener = new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                String name = (String) fontsList.getSelectedValue();
                String style = (String) styleList.getSelectedValue();
                Integer size = (Integer) sizesList.getSelectedValue();
                if (name == null) name = (String) fontsList.getModel().getElementAt(0);
                if (style == null) style = (String) styleList.getModel().getElementAt(0);
                if (size == null) size = (Integer) sizesList.getModel().getElementAt(0);

                int styleValue = 0;
                if (style.contains("Bold")) {
                    styleValue += Font.BOLD;
                }

                if (style.contains("Italic")) {
                    styleValue += Font.ITALIC;
                }

                Font font = new Font(name, styleValue, size);
                preview.setFont(font);
                panel.validate();
            }
        };
        fontsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        styleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sizesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fontsList.addListSelectionListener(listener);
        styleList.addListSelectionListener(listener);
        sizesList.addListSelectionListener(listener);
        listener.valueChanged(null);

        JScrollPane fontsScroll = new JScrollPane(fontsList);
        fontsScroll.setBorder(BorderFactory.createTitledBorder("Name"));
        JScrollPane styleScroll = new JScrollPane(styleList);
        styleScroll.setBorder(BorderFactory.createTitledBorder("Style"));
        JScrollPane sizesScroll = new JScrollPane(sizesList);
        sizesScroll.setBorder(BorderFactory.createTitledBorder("Size"));
        sizesScroll.setPreferredSize(new Dimension(80, 0));

        Box centralBox = Box.createHorizontalBox();
        centralBox.add(fontsScroll);
        centralBox.add(Box.createHorizontalStrut(10));
        centralBox.add(styleScroll);

        panel.add(centralBox, BorderLayout.CENTER);
        panel.add(sizesScroll, BorderLayout.LINE_END);
        panel.add(preview, BorderLayout.SOUTH);

        Box buttons = Box.createHorizontalBox();
        buttons.add(Box.createHorizontalGlue());
        JButton ok = new JButton("  OK  ");
        buttons.add(ok);
        ActionListener buttonListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String cmd = e.getActionCommand().trim().toLowerCase();
                accepted = cmd.equals("ok");
                setVisible(false);
            }
        };
        ok.addActionListener(buttonListener);
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(buttonListener);
        buttons.add(cancel);
        buttons.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

        add(panel, BorderLayout.CENTER);
        add(buttons, BorderLayout.PAGE_END);
    }

    public JTextField getPreview() {
        return preview;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public static Font show(Component owner, Font initialFont) {
        FontChooserDialog dialog = new FontChooserDialog(owner, initialFont);
        dialog.setVisible(true);
        return dialog.isAccepted() ? dialog.getPreview().getFont() : null;
    }
}
