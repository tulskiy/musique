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

/**
 * Interface for messages such as warnings and errors. For use by CueSheet.
 * @author jwbroek
 */
public interface Message
{
  /**
   * Get the message text.
   * @return The message text.
   */
  public String getMessage();
  
  /**
   * Get the line number that this message applies to.
   * @return The line number that this message applies to.
   */
  public int getLineNumber();
  
  /**
   * Get the input that this message applies to.
   * @return The input that this message applies to.
   */
  public String getInput();
  
  /**
   * Get a textual representation of this Message. It is highly desirable to have an informative
   * String representation.
   * @return Aa textual representation of this Message.
   */
  public String toString();
}
