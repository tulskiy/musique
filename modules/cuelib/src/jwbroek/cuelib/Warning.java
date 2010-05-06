/*
 * Cuelib library for manipulating cue sheets.
 * Copyright (C) 2007-2008 Jan-Willem van den Broek
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package jwbroek.cuelib;

import java.util.logging.Logger;

/**
 * Simple warning for use by a cue sheet.
 * @author jwbroek
 */
public class Warning extends MessageImplementation
{
  /**
   * The logger for this class.
   */
  private final static Logger logger = Logger.getLogger(CueSheetSerializer.class.getCanonicalName());
  
  /**
   * Create a new Warning message.
   * @param input The input that caused the warning.
   * @param lineNumber The line number of the input that caused the warning.
   * @param message A message explaining what is wrong.
   */
  public Warning(final String input, final int lineNumber, final String message)
  {
    super("Warning", input, lineNumber, message);
    Warning.logger.entering(Warning.class.getCanonicalName(), "Warning()");
    Warning.logger.exiting(Warning.class.getCanonicalName(), "Warning()");
  }

  /**
   * Create a new Warning message.
   * @param lineOfInput The input that caused the warning.
   * @param message A message explaining what is wrong.
   */
  public Warning(final LineOfInput lineOfInput, final String message)
  {
    super("Warning", lineOfInput, message);
    Warning.logger.entering(Warning.class.getCanonicalName(), "Warning()");
    Warning.logger.exiting(Warning.class.getCanonicalName(), "Warning()");
  }
}
