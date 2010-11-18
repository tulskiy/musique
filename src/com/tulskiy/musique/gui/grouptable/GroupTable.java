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

package com.tulskiy.musique.gui.grouptable;

import com.tulskiy.musique.util.Util;

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
public class GroupTable extends JTable {
    private static final double FACTOR = 0.90;

    private Color bgColor1;
    private Color bgColor2;
    private Color selectBgColor1;
    private Color selectBgColor2;

    private Font separatorFont;
    private Color separatorColor;
    private final Font defaultFont = getFont();

    private boolean trackSelection = true;

    public GroupTable() {
        setDefaultRenderer(Object.class, new DefaultCellRenderer());
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        buildListeners();
    }

    protected boolean isSeparator(int row) {
        return getRowCount() > 0 && getColumnCount() > 0 && getModel().getValueAt(row, 0) instanceof Separator;
    }

    public void setTrackSelection(boolean trackSelection) {
        this.trackSelection = trackSelection;
    }

    private void buildListeners() {
        getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                // workaround for search dialog
                if (trackSelection && getSelectedRowCount() == 1) {
                    if (isSeparator(getSelectedRow())) {
                        Separator value = (Separator) getModel().getValueAt(getSelectedRow(), 0);
                        int size = value.getGroupSize();
                        int row = getSelectedRow() + 1;

                        while (size > 0 && row < getModel().getRowCount() && !isSeparator(row)) {
                            row++;
                            size--;
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
                int count = getModel().getRowCount();
                if (getRowSorter() != null) {
                    count = getRowSorter().getViewRowCount();
                }
                selectedRow = Math.min(selectedRow + 1, count - 1);
                changeSelection(selectedRow, 0, false, false);
            }
        });

        imap.put(KeyStroke.getKeyStroke("HOME"), "selectFirstRow");
        imap.put(KeyStroke.getKeyStroke("END"), "selectLastRow");
    }

    public void runAction(Object actionKey) {
        Action action = getActionMap().get(actionKey);
        if (action != null)
            action.actionPerformed(new ActionEvent(this, 0, actionKey.toString()));
    }

    public void addKeyboardAction(KeyStroke keyStroke, String name, Action action) {
        InputMap imap = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap amap = getActionMap();

        imap.put(keyStroke, name);
        amap.put(name, action);
    }

    private void initUI() {
        setUI(new GroupTableUI());

        setBackground(Color.white);
        setIntercellSpacing(new Dimension(0, 0));
        setFocusable(true);
        setDragEnabled(true);
        setFocusTraversalKeysEnabled(false);
        setFillsViewportHeight(true);
        setRowSelectionAllowed(true);
        setShowGrid(false);
        setOpaque(true);
        setFont(getFont());
        setForeground(getForeground());
        setSelectionBackground(getSelectionBackground());
        setSeparatorColor(getForeground());

        //fix dropLine colors, I hate Nimbus
        Color droplineColor = UIManager.getColor("Table.dropLineShortColor");
        UIManager.getDefaults().put("Table.dropLineColor", droplineColor);
        UIDefaults defaults = new UIDefaults();
        defaults.put("Table.dropLineColor", droplineColor);
        putClientProperty("Nimbus.Overrides", defaults);
        putClientProperty("Nimbus.Overrides.InheritDefaults", false);
    }

    @Override
    public void updateUI() {
        setForeground(null);
        setBackground(null);
        setFont(null);
        try {
            setSelectionBackground(null);
            setSelectionForeground(null);
        } catch (Exception e) {
            //NPE will happen here
            //but it's ok
        }

        super.updateUI();
        initUI();
    }

    @Override
    public void setFont(Font font) {
        if (font == null) {
            super.setFont(font);
        } else {
            Font newFont;
            if (defaultFont != null)
                newFont = defaultFont.deriveFont(font.getAttributes());
            else
                newFont = font;
            super.setFont(newFont);
            separatorFont = newFont.deriveFont(Font.BOLD, newFont.getSize() + 2f);
            setRowHeight(newFont.getSize() + 10);
        }
    }

    @Override
    public void setSelectionBackground(Color selectionBackground) {
        super.setSelectionBackground(selectionBackground);
        if (selectionBackground != null) {
            setSelectionForeground(Util.getContrastColor(selectionBackground));
            selectBgColor1 = new Color(selectionBackground.getRGB());
            selectBgColor2 = darker(selectionBackground);
        }
    }

    public void setSeparatorColor(Color color) {
        this.separatorColor = color;
    }

    public void setBackground(Color color) {
        super.setBackground(color);
        if (color != null) {
            bgColor1 = color;
            bgColor2 = darker(color);
        }
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
        if (visible.isEmpty()) {
            scrollRectToVisible(r);
            return;
        }

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

    public Rectangle getCellRect(int row, int column, boolean includeSpacing) {
        final TableModel model = getModel();

        // sometimes JTable asks for a cellrect that doesn't exist anymore, due
        // to an editor being installed before a bunch of rows were removed.
        // In this case, just return an empty rectangle, since it's going to
        // be discarded anyway
        if (row >= model.getRowCount() || model.getColumnCount() <= 0) {
            return new Rectangle();
        }

        // if it's the separator row, return the entire row as one big rectangle
        Object rowValue = model.getValueAt(row, 0);
        if (rowValue instanceof Separator) {
            Rectangle firstColumn = super.getCellRect(row, 0, includeSpacing);
            Rectangle lastColumn = super.getCellRect(row, getColumnCount() - 1, includeSpacing);
            return firstColumn.union(lastColumn);

            // otherwise it's business as usual
        } else {
            return super.getCellRect(row, column, includeSpacing);
        }
    }

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
                c = super.getTableCellRendererComponent(table, value, isSelected, false,
                        row, column);
            }

            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? bgColor1 : bgColor2);
            } else if (!(value instanceof Separator)) {
                if (table.getSelectedRowCount() > 1)
                    c.setBackground(row % 2 == 0 ? selectBgColor1 : selectBgColor2);
                else
                    c.setBackground(selectBgColor1);
            }

            return c;
        }

        @Override
        protected void setValue(Object value) {
            if (value instanceof ImageIcon) {
                setIcon((Icon) value);
                setText(null);
            } else {
                super.setValue(value);
                setIcon(null);
            }

            if (value instanceof String)
                setHorizontalAlignment(LEFT);
            else
                setHorizontalAlignment(CENTER);
        }
    }

    class SeparatorCellRenderer extends JPanel implements TableCellRenderer {
        private String value;

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setBorder(BorderFactory.createLineBorder(selectBgColor1, 1));
            } else {
                setBorder(BorderFactory.createEmptyBorder());
            }

            setFont(separatorFont);
            setForeground(separatorColor);
            this.value = ((Separator) value).getGroupName();
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            FontMetrics fm = g2d.getFontMetrics(separatorFont);
            Rectangle2D bounds = fm.getStringBounds(value, g);
            g2d.drawLine((int) (bounds.getWidth() + 20), getHeight() / 2, getWidth(), getHeight() / 2);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-alias!
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.drawString(value, 5, getHeight() - (getHeight() - fm.getAscent()) / 2 - fm.getDescent() + 2);
        }
    }
}
