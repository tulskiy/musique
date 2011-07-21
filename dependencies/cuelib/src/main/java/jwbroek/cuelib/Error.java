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
 * Simple error for use by a cue sheet.
 * @author jwbroek
 */
public class Error extends MessageImplementation
{
  /**
   * The logger for this class.
   */
  private final static Logger logger = Logger.getLogger(Error.class.getCanonicalName());
  
  /**
   * Create a new Error message.
   * @param input The input that caused the error.
   * @param lineNumber The line number of the input that caused the error.
   * @param message A message explaining what is wrong.
   */
  public Error(final String input, final int lineNumber, final String message)
  {
    super("Error", input, lineNumber, message);
    Error.logger.entering(Error.class.getCanonicalName(), "Error()");
    Error.logger.exiting(Error.class.getCanonicalName(), "Error()");
  }

  /**
   * Create a new Error message.
   * @param lineOfInput The input that caused the error.
   * @param message A message explaining what is wrong.
   */
  public Error(final LineOfInput lineOfInput, final String message)
  {
    super("Error", lineOfInput, message);
    Error.logger.entering(Error.class.getCanonicalName(), "Error()");
    Error.logger.exiting(Error.class.getCanonicalName(), "Error()");
  }
}
