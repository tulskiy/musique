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
import com.tulskiy.musique.playlist.PlaylistManager;
import com.tulskiy.musique.playlist.Song;
import com.tulskiy.musique.system.Application;
import com.tulskiy.musique.system.Settings;
import com.tulskiy.musique.util.Util;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTableUI;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;

/**
 * @Author: Denis Tulskiy
 * @Date: 23.06.2009
 */
@SuppressWarnings({"FieldCanBeLocal"})
public class PlaylistPanel_Old extends JTable {
    private Color textColor = (Color) Settings.get("textColor");
    private Color bgColor1 = (Color) Settings.get("bgColor1");
    private Color bgColor2 = (Color) Settings.get("bgColor2");
    private Color selectionBg = (Color) Settings.get("selectionBg");
    private Color tableHeaderBg = (Color) Settings.get("tableHeaderBg");
    private Color tableHeaderTextColor = (Color) Settings.get("tableHeaderTextColor");
    private Color playingBg = (Color) Settings.get("playingBg");

    private String[] columns = (String[]) Settings.get("columns");
    private Font playlistFont = (Font) Settings.get("playlistFont");
    private Font headerFont = (Font) Settings.get("headerFont");

    private int[] columnWidth = (int[]) Settings.get("columnWidth");
    private JScrollPane scrollPane;
    private PlaylistTableModel model = new PlaylistTableModel();
    private PlaylistTableCellRenderer cellRenderer = new PlaylistTableCellRenderer();

    private Application app = Application.getInstance();
    private PlaylistManager playlistManager = app.getPlaylistManager();
    private Player player = app.getPlayer();

//    private PlaylistSearchDialog searchDialog;

    public PlaylistPanel_Old() {
//        setUI(new CTUI());
        setIntercellSpacing(new Dimension(0, 0));
        setFocusable(true);
        setDragEnabled(true);
        setFocusTraversalKeysEnabled(false);
        setRowSelectionAllowed(true);
        setForeground(textColor);
//        playlistFont = new Font(getFont().getName(), Font.PLAIN, 16);
        setFont(playlistFont);
        setOpaque(false);
        setSelectionBackground(selectionBg);
        setSelectionForeground(textColor);
        setRowHeight(playlistFont.getSize() + 7);
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        setModel(model);
        setDefaultRenderer(Object.class, cellRenderer);
        for (int i = 0; i < columns.length; i++) {
            TableColumn column = getColumn(columns[i]);
            column.setPreferredWidth(columnWidth[i]);
        }
        setFillsViewportHeight(true);
        JTableHeader th = getTableHeader();
        th.setBackground(tableHeaderBg);
        th.setForeground(tableHeaderTextColor);
        th.setFont(headerFont);
        ((DefaultTableCellRenderer) th.getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

        getTableHeader().setResizingAllowed(true);
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        scrollPane = new JScrollPane(this, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        createListeners();
    }

    private void createListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
                    Song song = playlistManager.getCurrentPlaylist().get(getSelectedRow());
                    player.open(song);
                    player.play();
                }
            }
        });

        getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                Settings.set("playlistSelection", getSelectedRows());
            }
        });

        getColumnModel().addColumnModelListener(new TableColumnModelListener() {
            public void columnMarginChanged(ChangeEvent event) {
                Enumeration e = getColumnModel().getColumns();
                int[] columns = new int[getColumnCount()];
                int i = 0;
                while (e.hasMoreElements()) {
                    TableColumn tableColumn = (TableColumn) e.nextElement();
                    columns[i++] = tableColumn.getWidth();
                }
                Settings.set("columnWidth", columns);
            }

            public void columnAdded(TableColumnModelEvent e) {
            }

            public void columnRemoved(TableColumnModelEvent e) {
            }

            public void columnMoved(TableColumnModelEvent e) {
            }

            public void columnSelectionChanged(ListSelectionEvent e) {
            }
        });

        InputMap imap = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap amap = getActionMap();

        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "playSelectedFile");
        amap.put("playSelectedFile", new KeyProcessor("playSelectedFile"));

        player.addListener(new PlayerListener() {
            public void onEvent(PlayerEvent e) {
                switch (e.getEventCode()) {
                    case FILE_OPENED:
                        repaint();
                        break;
                }
            }
        });
    }

    public JScrollPane getScrollPane() {
        return scrollPane;
    }

    public void scrollToRow(int currentItem) {
        Rectangle r = getCellRect(currentItem, 0, true);

        if (!isVerticallyVisible(r)) center(r);
    }

    public boolean isVerticallyVisible(Rectangle r) {
        Rectangle visible = getVisibleRect();

        return visible.y <= r.y
                && visible.y + visible.height >= r.y + r.height;
    }

    public void center(Rectangle r) {
        Rectangle visible = getVisibleRect();

        visible.x = r.x - (visible.width - r.width) / 2;
        visible.y = r.y - (visible.height - r.height) / 2;

        Rectangle bounds = getBounds();
        Insets i = getInsets();
        bounds.x = i.left;
        bounds.y = i.top;
        bounds.width -= i.left + i.right;
        bounds.height -= i.top + i.bottom;

        if (visible.x < bounds.x)
            visible.x = bounds.x;

        if (visible.x + visible.width > bounds.x + bounds.width)
            visible.x = bounds.x + bounds.width - visible.width;

        if (visible.y < bounds.y)
            visible.y = bounds.y;

        if (visible.y + visible.height > bounds.y + bounds.height)
            visible.y = bounds.y + bounds.height - visible.height;

        scrollRectToVisible(visible);
    }

    class KeyProcessor extends AbstractAction {
        private String actionName;

        KeyProcessor(String actionName) {
            this.actionName = actionName;
        }

        public void actionPerformed(ActionEvent e) {
//            FileInfo info = new FileInfo(getSelectedRow());
//            info.setVisible(true);
        }
    }

//    @Override
//    public Rectangle getCellRect(int row, int column, boolean includeSpacing) {
//        // add widths of all spanned logical cells
//        if (row % 4 == 0) {
//            Rectangle r1 = super.getCellRect(row, 0, includeSpacing);
//            for (int i = 1; i < getColumnCount(); i++) {
//                r1.width += getColumnModel().getColumn(i).getWidth();
//            }
//            return r1;
//        } else return super.getCellRect(row, column, includeSpacing);
//    }

//    @Override
//    public int columnAtPoint(Point p) {
//        int x = super.columnAtPoint(p);
//        // -1 is returned by columnAtPoint if the point is not in the table
//        if (x < 0) return x;
//        int y = super.rowAtPoint(p);
//        return y % 4 == 0 ? 0 : x;
//    }

    class CTUI extends BasicTableUI {
        public void paint(Graphics g, JComponent c) {
            Rectangle r = g.getClipBounds();
            int firstRow = table.rowAtPoint(new Point(0, r.y));
            int lastRow = table.rowAtPoint(new Point(0, r.y + r.height));
            // -1 is a flag that the ending point is outside the table
            if (lastRow < 0)
                lastRow = table.getRowCount() - 1;
            for (int i = firstRow; i <= lastRow; i++)
                paintRow(i, g);
        }

        private void paintRow(int row, Graphics g) {
            Rectangle r = g.getClipBounds();
            for (int i = 0; i < table.getColumnCount(); i++) {
                Rectangle r1 = table.getCellRect(row, i, true);
                if (r1.intersects(r)) // at least a part is visible
                {
                    int sk = row % 4 == 0 ? 0 : i;
                    paintCell(row, sk, g, r1);
                    // increment the column counter
                    i += row % 4 == 0 ? getColumnCount() : 0;
                }
            }
        }

        private void paintCell(int row, int column, Graphics g, Rectangle area) {
            int verticalMargin = table.getRowMargin();
            int horizontalMargin = table.getColumnModel().getColumnMargin();

            Color c = g.getColor();
            g.setColor(table.getGridColor());
            g.drawRect(area.x, area.y, area.width - 1, area.height - 1);
            g.setColor(c);

            area.setBounds(area.x + horizontalMargin / 2,
                    area.y + verticalMargin / 2,
                    area.width - horizontalMargin,
                    area.height - verticalMargin);

            if (table.isEditing() && table.getEditingRow() == row &&
                    table.getEditingColumn() == column) {
                Component component = table.getEditorComponent();
                component.setBounds(area);
                component.validate();
            } else {
                TableCellRenderer renderer = table.getCellRenderer(row, column);
                Component component = table.prepareRenderer(renderer, row, column);
                if (component.getParent() == null)
                    rendererPane.add(component);
                rendererPane.paintComponent(g, component, table, area.x, area.y,
                        area.width, area.height, true);
            }
        }
    }

    class PlaylistTableModel extends AbstractTableModel {

        public int getRowCount() {
            return playlistManager.getCurrentPlaylist().size();
        }

        public int getColumnCount() {
            return columns.length;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            Song song = playlistManager.getCurrentPlaylist().get(rowIndex);

            switch (columnIndex) {
                case 0:
                    return rowIndex + 1;
                case 1:
                    return song.getArtist() + " - " + song.getTitle();
                case 2:
                    return Util.samplesToTime(song.getTotalSamples(), song.getSamplerate(), 0);
                case 3:
                    return song.getAlbum();
                case 4:
                    return song.getYear();
            }

            return null;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }
    }

    class PlaylistTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value, boolean isSelected, boolean hasFocus, int row,
                                                       int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
                    row, column);
            if (!c.getBackground().equals(getSelectionBackground())) {
//                if (playlistManager.getCurrentPlaylist().get(row) == player.getAudioFile()) {
//                    c.setBackground(playingBg);
//                } else {
                c.setBackground(row % 2 == 0 ? bgColor1 : bgColor2);
//                }
            }

            if (column == 0 || column == 2) {
                setHorizontalAlignment(RIGHT);
            } else {
                setHorizontalAlignment(LEFT);
            }

            setBorder(BorderFactory.createEmptyBorder(1, 3, 1, 3));

            return c;
        }
    }
}
