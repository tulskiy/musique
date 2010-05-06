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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Simple parser for options (typically command line arguments).
 * @author jwbroek
 */
public class SimpleOptionsParser
{
  /**
   * The logger for this class.
   */
  private final static Logger logger = Logger.getLogger(SimpleOptionsParser.class.getCanonicalName());
  
  /**
   * Interface that you must implement if you want to handle options. {@link jwbroek.util.SimpleOptionsParser}
   * will call the OptionHandler that is registered for an option, when it encounters that option.
   */
  public interface OptionHandler
  {
    /**
     * Handle the option found at the specified offset in the options list. The handler must handle at least the option
     * at the offset. If it cannot, it must throw an Exception. It may handle more than one option.
     * @param options
     * @param offset
     * @return The index of the last option parsed + 1. The handler must handle at least the option at the offset. If
     * it cannot, it must throw an Exception.
     */
    public int handleOption(String [] options, int offset);
  }
  
  /**
   * Map from option key to handler for that option.
   */
  Map<String, OptionHandler> optionHandlers = new HashMap<String, OptionHandler>();
  
  /**
   * Create a new SimpleOptionsParser.
   */
  public SimpleOptionsParser()
  {
    SimpleOptionsParser.logger.entering(SimpleOptionsParser.class.getCanonicalName(), "SimpleOptionsParser()");
    SimpleOptionsParser.logger.exiting(SimpleOptionsParser.class.getCanonicalName(), "SimpleOptionsParser()");
  }
  
  /**
   * <p>Register an option with the parser. If the option is found, then the specified handler will be called.</p>
   * @param optionKey The option to be registered. For instance "-a".
   * @param handler The handler to handle this option.
   * @deprecated The prefered method is {@link #registerOption(jwbroek.util.SimpleOptionsParser.OptionHandler, String[])}.
   */
  public void registerOption(String optionKey, OptionHandler handler)
  {
    SimpleOptionsParser.logger.entering
      ( SimpleOptionsParser.class.getCanonicalName()
      , "registerOption(String,OptionHandler)"
      , new Object [] {optionKey, handler}
      );
    this.optionHandlers.put(optionKey, handler);
    SimpleOptionsParser.logger.exiting
      (SimpleOptionsParser.class.getCanonicalName(), "registerOption(String,OptionHandler)");
  }
  
  /**
   * <p>Register options with identical handler with the parser. When one of the the options is found,
   * the specified handler will be called.</p>
   * @param optionKeys The options to be registered. For instance "-a", "--alpha".
   * @param handler The handler to handle these options. 
   */
  public void registerOption(OptionHandler handler, String ... optionKeys)
  {
    SimpleOptionsParser.logger.entering
      ( SimpleOptionsParser.class.getCanonicalName()
      , "registerOption(OptionHandler,String[])"
      , new Object [] {handler, optionKeys}
      );
    
    for (String optionKey : optionKeys)
    {
      this.optionHandlers.put(optionKey, handler);
    }
    
    SimpleOptionsParser.logger.exiting
      (SimpleOptionsParser.class.getCanonicalName(), "registerOption(OptionHandler,String[])");
  }
  
  /**
   * Parse the previously registered options.
   * @param options The options to parse. Each element will be considered an atomic option element.
   * @return The index of the first option that could not be matched, or options.length if everything was matched.
   */
  public int parseOptions(final String [] options)
  {
    SimpleOptionsParser.logger.entering
      (SimpleOptionsParser.class.getCanonicalName(), "parseOptions(String[])", new Object [] {options});
    int result = parseOptions(options, 0);
    SimpleOptionsParser.logger.exiting
      (SimpleOptionsParser.class.getCanonicalName(), "registerOption(OptionHandler,String[])", result);
    return result;
  }

  /**
   * Parse the previously registered options.
   * @param options The options to parse. Each element will be considered an atomic option element.
   * @param offset First option element to parse.
   * @return The index of the first option that could not be matched, or options.length if everything was matched.
   */
  public int parseOptions(final String [] options, final int offset)
  {
    SimpleOptionsParser.logger.entering
      (SimpleOptionsParser.class.getCanonicalName(), "parseOptions(String[],int)", new Object [] {options, offset});
    
    int currentOffset = offset;
    OptionHandler currentHandler = null;
    
    loopOverOptions:
    while (currentOffset < options.length)
    {
      currentHandler = this.optionHandlers.get(options[currentOffset]);
      
      // Can't handle this option.
      if (currentHandler==null)
      {
        break loopOverOptions;
      }
      
      int nextOffset = currentHandler.handleOption(options, currentOffset);
      
      // Handler did not handle at least the first option. This is an exception state, as it would lead
      // to an infinite loop.
      if (nextOffset==currentOffset)
      {
        IllegalStateException e = new IllegalStateException
          ( "Handler registered for option \""
          + options[currentOffset]
          + "\" did not handle it."
          );
      SimpleOptionsParser.logger.throwing(SimpleOptionsParser.class.getCanonicalName(), "parseOptions(String[],int)", e);
      throw e;
      }
      
      // Handler claims to have handled more options than actually exist.
      if (nextOffset > options.length)
      {
        IllegalStateException e = new IllegalStateException
          ( "Handler registered for option \""
          + options[currentOffset]
          + "\" claims to have handled more options than are existent."
          );
        SimpleOptionsParser.logger.throwing(SimpleOptionsParser.class.getCanonicalName(), "parseOptions(String[],int)", e);
        throw e;
      }
      
      currentOffset = nextOffset;
    }
    
    SimpleOptionsParser.logger.exiting
      (SimpleOptionsParser.class.getCanonicalName(), "parseOptions(String[],int)", currentOffset);
    return currentOffset;
  }
}
