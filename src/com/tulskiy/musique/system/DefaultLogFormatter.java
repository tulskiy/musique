/*
 * Copyright (c) 2008, 2009, 2010, 2011 Denis Tulskiy
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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
* Author: Denis Tulskiy
* Date: 5/21/11
*/
public class DefaultLogFormatter extends Formatter {
    @Override
    public String format(LogRecord record) {
        java.util.Formatter formatter = new java.util.Formatter();
        formatter.format("%1$tm/%1$td/%1$ty %1$tH:%1$tM:%1$tS,%1$tL %4$s %2$s <%3$s>: %5$s\n",
                record.getMillis(),
                record.getSourceClassName().replaceAll(".*?\\.(.*?)", "$1"),
                record.getSourceMethodName(),
                record.getLevel().getName(),
                formatMessage(record));
        if (record.getThrown() != null) {
            try {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                record.getThrown().printStackTrace(pw);
                pw.close();
                formatter.format("%s", sw.toString());
            } catch (Exception ignored) {
            }
        }

        return formatter.toString();
    }
}
