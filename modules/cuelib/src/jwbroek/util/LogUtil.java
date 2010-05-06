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
package jwbroek.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class containing utility methods related to logging.
 * @author jwbroek
 */
public final class LogUtil
{
  /**
   * The logger for this class.
   */
  private final static Logger logger = Logger.getLogger(LogUtil.class.getCanonicalName());
  
  /**
   * This constructor need never be called as all members of this class are static.  
   */
  private LogUtil()
  {
    // Intentionally empty (except for logging). This class does not need to be instantiated.
    LogUtil.logger.entering(LogUtil.class.getCanonicalName(), "TrackCutterCommand()");
    LogUtil.logger.warning("jwbroek.util.LogUtil should not be instantiated.");
    LogUtil.logger.exiting(LogUtil.class.getCanonicalName(), "TrackCutterCommand()");
  }
  
  /**
   * Convenience method to log the stack trace of a Throwable to the specified Logger at the specified Level.
   * @param logger Logger to log to.
   * @param level Level to log at.
   * @param throwable Throwable to log the stack strace of.
   */
  public static void logStacktrace(final Logger logger, final Level level, final Throwable throwable)
  {
    LogUtil.logger.entering
      ( LogUtil.class.getCanonicalName()
      , "logStacktrace(Logger,Level,Throwable)"
      , new Object[] {logger, level, throwable}
      );
    
    final StringWriter sw = new StringWriter();
    throwable.printStackTrace(new PrintWriter(sw));
    logger.log(level, sw.toString());
    
    LogUtil.logger.exiting(LogUtil.class.getCanonicalName(), "logStacktrace(Logger,Level,Throwable)");
  }
  
  /**
   * Get the Level that is currently active on the specified Logger. Will search though parent Logger as necessary.
   * @param logger The Logger to determine the Level of.
   * @return The Level that is currently active on the specified Logger.
   */
  public static Level getActiveLoggingLevel(Logger logger)
  {
    LogUtil.logger.entering(LogUtil.class.getCanonicalName(), "getActiveLoggingLevel(Logger)", logger);
    
    Logger currentLogger = logger;
    Level result = null;
    
    do 
    {
      result = logger.getLevel();
      
      if (currentLogger.getUseParentHandlers())
      {
        currentLogger = currentLogger.getParent();
      }
      else
      {
        currentLogger = null;
      }
    } while (result==null && currentLogger != null);
    
    LogUtil.logger.exiting(LogUtil.class.getCanonicalName(), "getActiveLoggingLevel(Logger)", result);
    return result;
  }
  
  /**
   * Get whether or not the information that is logged at the specified Level to the specified Logger
   * will be handled by a Handler that is an instance of the specified class. Note that since loggers
   * may be added and removed at any time, this information is not guaranteed to be correct at any moment.
   * Also, even when this method return true, messages logged to the specified logger, at the specified level,
   * may still not be logged by a handler of the specified type due to Filters that are configured.
   * @param logger The Logger to check for.
   * @param level The level to check for.
   * @param handlerClass The class of Handler to check for.
   * @return Whether or not the information that is logged at the specified Level to the specified Logger
   * will be handled by a Handler that is an instance of the specified class.
   */
  public static boolean hasHandlerActive(final Logger logger, final Level level, final Class handlerClass)
  {
    LogUtil.logger.entering
      ( LogUtil.class.getCanonicalName()
      , "hasHandlerActive(Logger,Level,Class)"
      , new Object[] {logger, level, handlerClass}
      );
    
    Logger currentLogger = logger;
    boolean result = false;
    
    loopOverLoggers:
    while (currentLogger != null && currentLogger.isLoggable(level))
    {
      for (Handler handler : currentLogger.getHandlers())
      {
        // Handler is correct type of class and has low enough logging level.
        if  ( handlerClass.isInstance(handler)
            && handler.getLevel().intValue() <= level.intValue()
            )
        {
          result = true;
          break loopOverLoggers;
        }
      }
      
      if (currentLogger.getUseParentHandlers())
      {
        currentLogger = currentLogger.getParent();
      }
      else
      {
        currentLogger = null;
      }
    }
    
    LogUtil.logger.exiting(LogUtil.class.getCanonicalName(), "hasHandlerActive(Logger,Level,Class)", result);
    return result;
  }
  
  /**
   * Get a list of all currently active Handlers on the specified Logger. Note that handlers can be
   * dynamically added and removed, so this information is not guaranteed to be correct at any moment.
   * @param logger
   * @return A list of all currently active Handlers on the specified Logger.
   */
  public static Set<Handler> getAllActiveHandlers(final Logger logger)
  {
    LogUtil.logger.entering(LogUtil.class.getCanonicalName(), "getAllActiveHandlers(Logger)", logger);
    final Set<Handler> handlers = new HashSet<Handler>();
    
    Logger currentLogger = logger;
    
    while (currentLogger != null)
    {
      handlers.addAll(Arrays.asList(currentLogger.getHandlers()));
      if (currentLogger.getUseParentHandlers())
      {
        currentLogger = currentLogger.getParent();
      }
      else
      {
        currentLogger = null;
      }
    }
    
    LogUtil.logger.exiting(LogUtil.class.getCanonicalName(), "getAllActiveHandlers(Logger)", handlers);
    return handlers;
  }
}
