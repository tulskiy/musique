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
 * Implementation of the Message interface. Implements a specific type of message that can be freely chosen.
 * For instance, "Warning", "Error", "Debug", etc.
 * @author jwbroek
 */
public abstract class MessageImplementation implements Message
{
  /**
   * The logger for this class.
   */
  private final static Logger logger = Logger.getLogger(MessageImplementation.class.getCanonicalName());
  /**
   * The input this message applies to.
   */
  private String input;
  /**
   * The line number of the input that this message applies to.
   */
  private int lineNumber;
  /**
   * The message text.
   */
  private String message;
  /**
   * The message type.
   */
  private String type;
  
  /**
   * Create a new MessageImplementation.
   * @param type The type of the message.
   */
  public MessageImplementation(final String type)
  {
    MessageImplementation.logger.entering
      (MessageImplementation.class.getCanonicalName(), "MessageImplementation(String)", type);
    this.input = "";
    this.lineNumber = -1;
    this.message = "";
    this.type = type;
    MessageImplementation.logger.exiting(MessageImplementation.class.getCanonicalName(), "MessageImplementation(String)");
  }
  
  /**
   * Create a new MessageImplementation.
   * @param type The type of the message.
   * @param lineOfInput The line of input that this message applies to.
   * @param message The message text.
   */
  public MessageImplementation(final String type, final LineOfInput lineOfInput, final String message)
  {
    MessageImplementation.logger.entering
      ( MessageImplementation.class.getCanonicalName()
      , "MessageImplementation(String,LineOfInput,String)"
      , new Object[] {type, lineOfInput, message}
      );
    this.input = lineOfInput.getInput();
    this.lineNumber = lineOfInput.getLineNumber();
    this.message = message;
    this.type = type;
    MessageImplementation.logger.exiting
      (MessageImplementation.class.getCanonicalName(), "MessageImplementation(String,LineOfInput,String)");
  }
  
  /**
   * Create a new MessageImplementation.
   * @param type The type of the message.
   * @param input The input that this message applies to.
   * @param lineNumber The line number of the input that this message applies to.
   * @param message The message text.
   */
  public MessageImplementation(String type, String input, int lineNumber, String message)
  {
    MessageImplementation.logger.entering
      ( MessageImplementation.class.getCanonicalName()
      , "MessageImplementation(String,String,int.String)"
      , new Object[] {type, input, lineNumber, message}
      );
    this.input = input;
    this.lineNumber = lineNumber;
    this.message = message;
    this.type = type;
    MessageImplementation.logger.exiting
      (MessageImplementation.class.getCanonicalName(), "MessageImplementation(String,String,int.String)");
  }

  /**
   * Get a textual representation of this message.
   * @return A textual representation of this message.
   */
  public String toString()
  {
    MessageImplementation.logger.entering(MessageImplementation.class.getCanonicalName(), "toString()");
    StringBuilder builder = new StringBuilder(input).append('\n');
    builder.append(type).append(" [Line ").append(lineNumber).append("] ").append(message).append('\n');
    MessageImplementation.logger.exiting
      (MessageImplementation.class.getCanonicalName(), "toString()", builder.toString());
    return builder.toString();
  }

  /**
   * Get the input that this message applies to.
   * @return The input that this message applies to.
   */
  public String getInput()
  {
    MessageImplementation.logger.entering(MessageImplementation.class.getCanonicalName(), "getInput()");
    MessageImplementation.logger.exiting(MessageImplementation.class.getCanonicalName(), "getInput()", this.input);
    return this.input;
  }

  /**
   * Set the input that this message applies to.
   * @param input The input that this message applies to.
   */
  public void setInput(final String input)
  {
    MessageImplementation.logger.entering(MessageImplementation.class.getCanonicalName(), "setInput(String)", input);
    this.input = input;
    MessageImplementation.logger.exiting(MessageImplementation.class.getCanonicalName(), "setInput(String)");
  }

  /**
   * Get the line number of the input that this message applies to.
   * @return The line number of the input that this message applies to.
   */
  public int getLineNumber()
  {
    MessageImplementation.logger.entering(MessageImplementation.class.getCanonicalName(), "getLineNumber()");
    MessageImplementation.logger.exiting
      (MessageImplementation.class.getCanonicalName(), "getLineNumber()", this.lineNumber);
    return this.lineNumber;
  }

  /**
   * Set the line number of the input that this message applies to.
   * @param lineNumber The line number of the input that this message applies to.
   */
  public void setLineNumber(final int lineNumber)
  {
    MessageImplementation.logger.entering
      (MessageImplementation.class.getCanonicalName(), "setLineNumber(int)", lineNumber);
    this.lineNumber = lineNumber;
    MessageImplementation.logger.exiting(MessageImplementation.class.getCanonicalName(), "setLineNumber(int)");
  }

  /**
   * Get the text for this message.
   * @return The text for this message.
   */
  public String getMessage()
  {
    MessageImplementation.logger.entering(MessageImplementation.class.getCanonicalName(), "getMessage()");
    MessageImplementation.logger.exiting(MessageImplementation.class.getCanonicalName(), "getMessage()", this.message);
    return this.message;
  }

  /**
   * Set the text for this message.
   * @param message The text for this message.
   */
  public void setMessage(final String message)
  {
    MessageImplementation.logger.entering(MessageImplementation.class.getCanonicalName(), "setMessage(String)", message);
    this.message = message;
    MessageImplementation.logger.exiting(MessageImplementation.class.getCanonicalName(), "setMessage(String)");
  }
}
