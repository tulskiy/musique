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

package com.tulskiy.musique.gui.custom;

import com.tulskiy.musique.system.Application;
import com.tulskiy.musique.system.Configuration;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;

/**
 * @Author: Denis Tulskiy
 * @Date: Sep 30, 2009
 */
public class SeparatorTable extends JTable {
    private static final double FACTOR = 0.90;

    private Color bgColor1;
    private Color bgColor2;
    private Color selectBgColor1;
    private Color selectBgColor2;

    private Font separatorFont;
    private Color separatorColor;
    private final Font defaultFont = getFont();

    public SeparatorTable() {
        initUI();
        buildListeners();
    }

    private boolean isSeparator(int row) {
        return getModel().getValueAt(row, 0) instanceof Separator;
    }

    private void buildListeners() {
        getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (getSelectedRowCount() == 1) {
                    if (isSeparator(getSelectedRow())) {
                        int row = getSelectedRow() + 1;

                        while (row < getModel().getRowCount() && !isSeparator(row)) {
                            row++;
                        }

                        getSelectionModel().setSelectionInterval(getSelectedRow(), row - 1);
                    }
                }
            }
        });

        InputMap imap = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap amap = getActionMap();

        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "goUp");
        amap.put("goUp", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                int selectedRow = getSelectionModel().getAnchorSelectionIndex();
                selectedRow = Math.max(0, selectedRow - 1);
                changeSelection(selectedRow, 0, false, false);
            }
        });

        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "goDown");
        amap.put("goDown", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                int selectedRow = getSelectionModel().getAnchorSelectionIndex();
                selectedRow = Math.min(selectedRow + 1, getModel().getRowCount() - 1);
                changeSelection(selectedRow, 0, false, false);
            }
        });
    }

    private void initUI() {
//        setUI(new SpanTableUI());

        setBackground(Color.white);
        setIntercellSpacing(new Dimension(0, 0));
        setFocusable(true);
        setDragEnabled(true);
        setFocusTraversalKeysEnabled(false);
        setFillsViewportHeight(true);
        setRowSelectionAllowed(true);
        setOpaque(false);
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        setFont(getFont());
        setBackground(getBackground());
        setForeground(getForeground());
        setSelectionBackground(getSelectionBackground());
        setDefaultRenderer(ImageIcon.class, new IconCellRenderer());
        setDefaultRenderer(Object.class, new DefaultCellRenderer());
    }

    @Override
    public void setFont(Font font) {
        Font newFont;
        if (defaultFont != null)
            newFont = defaultFont.deriveFont(font.getAttributes());
        else
            newFont = font;
        super.setFont(newFont);
        separatorFont = newFont.deriveFont(newFont.getSize() + 3);
        setRowHeight(newFont.getSize() + 10);
    }

    @Override
    public void setSelectionBackground(Color selectionBackground) {
        super.setSelectionBackground(selectionBackground);
        selectBgColor1 = selectionBackground;
        selectBgColor2 = darker(selectionBackground);
    }

    public void setSeparatorColor(Color color) {
        this.separatorColor = color;
    }

    public void setBackground(Color color) {
        super.setBackground(color);
        bgColor1 = color;
        bgColor2 = darker(color);
    }

    private Color darker(Color c) {
        return new Color(Math.max((int) (c.getRed() * FACTOR), 0),
                Math.max((int) (c.getGreen() * FACTOR), 0),
                Math.max((int) (c.getBlue() * FACTOR), 0));
    }

    public void scrollToRow(int currentItem) {
        Rectangle r = getCellRect(currentItem, 0, true);

        if (!isVerticallyVisible(r)) center(r);
    }

    private boolean isVerticallyVisible(Rectangle r) {
        Rectangle visible = getVisibleRect();

        return visible.y <= r.y
                && visible.y + visible.height >= r.y + r.height;
    }

    private void center(Rectangle r) {
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

    /*public Rectangle getCellRect(int row, int column, boolean includeSpacing) {
        final TableModel eventTableModel = getModel();

        // sometimes JTable asks for a cellrect that doesn't exist anymore, due
        // to an editor being installed before a bunch of rows were removed.
        // In this case, just return an empty rectangle, since it's going to
        // be discarded anyway
        if (row >= eventTableModel.getRowCount()) {
            return new Rectangle();
        }

        // if it's the separator row, return the entire row as one big rectangle
        Object rowValue = eventTableModel.getValueAt(row, 0);
        if (rowValue instanceof Separator) {
            Rectangle firstColumn = super.getCellRect(row, 0, includeSpacing);
            Rectangle lastColumn = super.getCellRect(row, getColumnCount() - 1, includeSpacing);
            return firstColumn.union(lastColumn);

            // otherwise it's business as usual
        } else {
            return super.getCellRect(row, column, includeSpacing);
        }
    }*/

/*
    public Object getValueAt(int row, int column) {
        final Object rowValue = getModel().getValueAt(row, 0);

        // if it's the separator row, return the value directly
        if (rowValue instanceof Separator)
            return rowValue;

        // otherwise it's business as usual
        return super.getValueAt(row, column);
    }
*/

    class DefaultCellRenderer extends DefaultTableCellRenderer {
        private SeparatorCellRenderer separatorCellRenderer = new SeparatorCellRenderer();

        @Override
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value, boolean isSelected, boolean hasFocus, int row,
                                                       int column) {
            Component c;
            if (value instanceof Separator) {
                c = separatorCellRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus,
                        row, column);
            } else {
                c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
                        row, column);
                setBorder(BorderFactory.createEmptyBorder(1, 3, 1, 3));
            }

            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? bgColor1 : bgColor2);
            } else if (table.getSelectedRowCount() > 1 && !(value instanceof Separator)) {
                c.setBackground(row % 2 == 0 ? selectBgColor1 : selectBgColor2);
            }

            return c;
        }
    }

    class IconCellRenderer extends DefaultCellRenderer {
        IconCellRenderer() {
            setHorizontalAlignment(JLabel.CENTER);
        }

        @Override
        protected void setValue(Object value) {
            setIcon((value instanceof Icon) ? (Icon) value : null);
        }
    }

    class SeparatorCellRenderer extends JPanel implements TableCellRenderer {
        private String value;

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setBorder(BorderFactory.createLineBorder(Color.gray, 1));
            } else {
                setBorder(BorderFactory.createEmptyBorder());
            }

            setFont(separatorFont);
            setForeground(separatorColor);
            this.value = value.toString();
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            FontMetrics fm = g.getFontMetrics(separatorFont);
            Rectangle2D bounds = fm.getStringBounds(value, g);
//            System.out.println(bounds);
            g.drawString(value, 5, getHeight() - 5);
            g.drawLine((int) (bounds.getWidth() + 20), getHeight() / 2, getWidth(), getHeight() / 2);
        }
    }
}
