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
 * Simple representation for a position field in a cue sheet.
 * @author jwbroek
 */
public class Position
{
  /**
   * The logger for this class.
   */
  private final static Logger logger = Logger.getLogger(Position.class.getCanonicalName());
  /**
   * The number of minutes in this position. Must be >= 0. Should be < 60.
   */
  private int minutes = 0;
  /**
   * The number of seconds in this position. Must be >= 0. Should be < 60.
   */
  private int seconds = 0;
  /**
   * The number of frames in this position. Must be >= 0. Should be < 75.
   */
  private int frames = 0;
  
  /**
   * Create a new Position.
   */
  public Position()
  {
    Position.logger.entering(Position.class.getCanonicalName(), "Position()");
    Position.logger.exiting(Position.class.getCanonicalName(), "Position()");
  }
  
  /**
   * Create a new Position.
   * @param minutes The number of minutes in this position. Must be >= 0. Should be < 60.
   * @param seconds The number of seconds in this position. Must be >= 0. Should be < 60.
   * @param frames The number of frames in this position. Must be >= 0. Should be < 75.
   */
  public Position(final int minutes, final int seconds, final int frames)
  {
    Position.logger.entering
      ( Position.class.getCanonicalName()
      , "LineOfInput(int,int,int)"
      , new Object[] {minutes, seconds, frames}
      );
    this.minutes = minutes;
    this.seconds = seconds;
    this.frames = frames;
    Position.logger.exiting(Position.class.getCanonicalName(), "Position(int,int,int)");
  }
  
  /**
   * Get the total number of frames represented by this position. This is equal to
   * frames + (75 * (seconds + 60 * minutes)).
   * @return The total number of frames represented by this position.
   */
  public int getTotalFrames()
  {
    Position.logger.entering(Position.class.getCanonicalName(), "getTotalFrames()");
    int result = frames + (75 * (seconds + 60 * minutes));
    Position.logger.exiting(Position.class.getCanonicalName(), "getTotalFrames()", result);
    return result;
  }
  
  /**
   * Get the number of frames in this position. Must be >= 0. Should be < 75.
   * @return The number of frames in this position. Must be >= 0. Should be < 75.
   */
  public int getFrames()
  {
    Position.logger.entering(Position.class.getCanonicalName(), "getFrames()");
    Position.logger.exiting(Position.class.getCanonicalName(), "getFrames()", this.frames);
    return this.frames;
  }

  /**
   * Set the number of frames in this position. Must be >= 0. Should be < 75.
   * @param frames The number of frames in this position. Must be >= 0. Should be < 75.
   */
  public void setFrames(final int frames)
  {
    Position.logger.entering(Position.class.getCanonicalName(), "setFrames(int)", frames);
    this.frames = frames;
    Position.logger.exiting(Position.class.getCanonicalName(), "setFrames(int)");
  }

  /**
   * Get the number of minutes in this position. Must be >= 0. Should be < 60.
   * @return The number of minutes in this position. Must be >= 0. Should be < 60.
   */
  public int getMinutes()
  {
    Position.logger.entering(Position.class.getCanonicalName(), "getMinutes()");
    Position.logger.exiting(Position.class.getCanonicalName(), "getMinutes()", this.minutes);
    return this.minutes;
  }

  /**
   * Set the number of minutes in this position. Must be >= 0. Should be < 60.
   * @param minutes The number of minutes in this position. Must be >= 0. Should be < 60.
   */
  public void setMinutes(final int minutes)
  {
    Position.logger.entering(Position.class.getCanonicalName(), "setMinutes(int)", minutes);
    this.minutes = minutes;
    Position.logger.exiting(Position.class.getCanonicalName(), "setMinutes(int)");
  }

  /**
   * Get the number of seconds in this position. Must be >= 0. Should be < 60.
   * @return The seconds of seconds in this position. Must be >= 0. Should be < 60.
   */
  public int getSeconds()
  {
    Position.logger.entering(Position.class.getCanonicalName(), "getSeconds()");
    Position.logger.exiting(Position.class.getCanonicalName(), "getSeconds()", this.seconds);
    return this.seconds;
  }

  /**
   * Set the number of seconds in this position. Must be >= 0. Should be < 60.
   * @param seconds The number of seconds in this position. Must be >= 0. Should be < 60.
   */
  public void setSeconds(final int seconds)
  {
    Position.logger.entering(Position.class.getCanonicalName(), "setSeconds(int)", seconds);
    this.seconds = seconds;
    Position.logger.exiting(Position.class.getCanonicalName(), "setSeconds(int)");
  }
}
