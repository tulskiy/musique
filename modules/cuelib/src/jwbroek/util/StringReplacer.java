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

import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>A StringReplacer will perform a number of string replacements on the same string in a single pass. For instance,
 * you can replace all occurrences of "schnauzer" by "bulldog" and all occurrences of "dog" by "cat". Doing this in a
 * single pass may yield a different result than doing it in sequence.</p>
 * 
 * <p>For instance, consider the string "The schnauzer chases the other dog." Replacing in a single pass will yield "The
 * bulldog chases the other cat.", while doing the replacements in sequence yields first "The bulldog chases the other
 * dog.", and then finally "The bullcat chases the other cat."</p>
 * 
 * <p>Searches are done greedily. That is to say, the string "bulldogs rule" will match the search string "bulldog" in
 * preference over the search strings "bull" (matches less) and "dogs rule" (matches more, but later).</p>
 * 
 * <p>Instances of this class are reusable. They are also safe for concurrent use, as long as the Map instance they are
 * constructed on is safe for concurrent reads. (The Map need not be safe for concurrent writes.) Most Map
 * implementations, including {@link java.util.HashMap}, {@link java.util.Hashtable}, and {@link java.util.TreeMap},
 * will meet this requirement.</p>
 * @author jwbroek
 */
public class StringReplacer
{
  /**
   * The logger for this class.
   */
  private final static Logger logger = Logger.getLogger(StringReplacer.class.getCanonicalName());
  /**
   * A Pattern that is used to perform the replacements.
   */
  private Pattern replacementPattern;
  /**
   * A map from "value to search for", to "value to change to".
   */
  private Map<String, String> replacements;
  
  /**
   * Build a reusable replacer based on a "from" "to" mapping of search and replace strings.
   * @param replacements A "from" "to" mapping. This Map should not be modified after being passed to this constructor,
   * or the behaviour of the StringReplacer will be undefined.
   */
  public StringReplacer(Map<String, String> replacements)
  {
    StringReplacer.logger.entering
      (StringReplacer.class.getCanonicalName(), "StringReplacer(Map<String,String>)", replacements);
    StringBuilder builder = new StringBuilder();
    
    builder.append('(');
    
    boolean isFirst = true;
    
    for (String key : replacements.keySet())
    {
      if (isFirst)
      {
        isFirst = false;
      }
      else
      {
        builder.append('|');
      }
      builder.append("(?:").append(Pattern.quote(key)).append(')');
    }
    
    builder.append(')');
    
    this.replacementPattern = Pattern.compile(builder.toString());
    this.replacements = replacements;
    StringReplacer.logger.exiting(StringReplacer.class.getCanonicalName(), "StringReplacer(Map<String,String>)");
  }
  
  /**
   * Perform the replacements on the specified input.
   * @param input The string to perform replacements on. Note that this String instance will not be modified, as
   * String instances are immutable in java.
   * @return The result of doing all relevant replacements on the input string. 
   */
  public String replace(String input)
  {
    StringReplacer.logger.entering(StringReplacer.class.getCanonicalName(), "replace(String)", input);
    StringBuffer buffer = new StringBuffer();
    
    Matcher matcher = this.replacementPattern.matcher(input);
    
    while(matcher.find())
    {
      matcher.appendReplacement(buffer, Matcher.quoteReplacement(this.replacements.get(matcher.group())));
    }
    
    matcher.appendTail(buffer);
    
    String result = buffer.toString();
    StringReplacer.logger.entering(StringReplacer.class.getCanonicalName(), "replace(String)", result);
    return result;
  }
}
