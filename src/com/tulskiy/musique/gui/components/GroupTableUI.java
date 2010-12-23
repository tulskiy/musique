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

package com.tulskiy.musique.gui.components;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTableUI;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * <p>UI for GroupTable.
 * Most of this code is taken from BasicTableUI, the only change
 * is that if a given region contains group separators, we paint them
 * separately
 * </p>
 * <p/>
 * Author: Denis Tulskiy
 * Date: Jun 30, 2010
 */

public class GroupTableUI extends BasicTableUI {
    @Override
    public void paint(Graphics g, JComponent c) {
        super.paint(g, c);
        Rectangle clip = g.getClipBounds();

        Rectangle bounds = table.getBounds();
        // account for the fact that the graphics has already been translated
        // into the table's bounds
        bounds.x = bounds.y = 0;

        Point upperLeft = clip.getLocation();
        Point lowerRight = new Point(clip.x + clip.width - 1,
                clip.y + clip.height - 1);

        int rMin = table.rowAtPoint(upperLeft);
        int rMax = table.rowAtPoint(lowerRight);
        // This should never happen (as long as our bounds intersect the clip,
        // which is why we bail above if that is the case).
        if (rMin == -1) {
            rMin = 0;
        }
        // If the table does not have enough rows to fill the view we'll get -1.
        // (We could also get -1 if our bounds don't intersect the clip,
        // which is why we bail above if that is the case).
        // Replace this with the index of the last row.
        if (rMax == -1) {
            rMax = table.getRowCount() - 1;
        }

        // paint groups
        for (int row = rMin; row <= rMax; row++) {
            if (((GroupTable) table).isSeparator(row)) {
                Rectangle cellRect = table.getCellRect(row, 0, false);
                paintGroup(g, cellRect, row);
            }
        }
    }

    private void paintGroup(Graphics g, Rectangle cellRect, int row) {
        TableCellRenderer renderer = table.getCellRenderer(row, 0);
        Component component = table.prepareRenderer(renderer, row, 0);
        rendererPane.paintComponent(g, component, table, cellRect.x, cellRect.y,
                cellRect.width, cellRect.height, true);
    }
}
