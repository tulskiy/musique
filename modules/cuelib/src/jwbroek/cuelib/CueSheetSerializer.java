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

import java.util.Set;
import java.util.logging.Logger;

/**
 * Class for serializing a {@link jwbroek.cuelib.CueSheet CueSheet} back to a string representation. Does the
 * inverse job of CueParser.
 * @author jwbroek
 */
public class CueSheetSerializer
{
  /**
   * Character sequence for a single indentation level.
   */
  private String indentationValue = "  ";
  /**
   * The logger for this class.
   */
  private final static Logger logger = Logger.getLogger(CueSheetSerializer.class.getCanonicalName());
  
  /**
   * Create a default CueSheetSerializer.
   */
  public CueSheetSerializer()
  {
    CueSheetSerializer.logger.entering(CueSheetSerializer.class.getCanonicalName(), "CueSheetSerializer()");
    CueSheetSerializer.logger.exiting(CueSheetSerializer.class.getCanonicalName(), "CueSheetSerializer()");
  }
  
  /**
   * Create a CueSheetSerializer with the specified indentationValue.
   * @param indentationValue This String will be used for indentation.
   */
  public CueSheetSerializer(final String indentationValue)
  {
    CueSheetSerializer.logger.entering
      (CueSheetSerializer.class.getCanonicalName(), "CueSheetSerializer(String)", indentationValue);
    CueSheetSerializer.logger.config("Setting CueSheetSerializer indentation value to: '" + indentationValue + "'");
    this.indentationValue = indentationValue;
    CueSheetSerializer.logger.exiting(CueSheetSerializer.class.getCanonicalName(), "CueSheetSerializer(String)");
  }
  
  /**
   * Get a textual representation of the cue sheet. If the cue sheet was parsed, then the output
   * of this method is not necessarily identical to the parsed sheet, though it will contain the
   * same data. Fields may appear in a different order, whitespace may change, comments may be
   * gone, etc.
   * @param cueSheet The CueSheet to serialize.
   * @return A textual representation of the cue sheet. 
   */
  public String serializeCueSheet(final CueSheet cueSheet)
  {
    CueSheetSerializer.logger.entering
      (CueSheetSerializer.class.getCanonicalName(), "serializeCueSheet(CueSheet)", cueSheet);
    StringBuilder builder = new StringBuilder();
    
    serializeCueSheet(builder, cueSheet, "");
    
    String result = builder.toString();
    CueSheetSerializer.logger.exiting(CueSheetSerializer.class.getCanonicalName(), "serializeCueSheet(CueSheet)", result);
    return result;
  }
  
  /**
   * Serialize the CueSheet.
   * @param builder The StringBuilder to serialize to.
   * @param cueSheet The CueSheet to serialize.
   * @param indentation The current indentation.
   */
  private void serializeCueSheet(final StringBuilder builder, final CueSheet cueSheet, final String indentation)
  {
    CueSheetSerializer.logger.entering
      ( CueSheetSerializer.class.getCanonicalName()
      , "serializeCueSheet(StringBuilder,CueSheet,String)"
      , new Object[] {builder, cueSheet, indentation}
      );
    
    CueSheetSerializer.logger.fine("Serializing cue sheet to cue format.");
    
    addField(builder, "REM GENRE", indentation, cueSheet.getGenre());
    addField(builder, "REM DATE", indentation, cueSheet.getYear());
    addField(builder, "REM DISCID", indentation, cueSheet.getDiscid());
    addField(builder, "REM COMMENT", indentation, cueSheet.getComment());
    addField(builder, "CATALOG", indentation, cueSheet.getCatalog());
    addField(builder, "PERFORMER", indentation, cueSheet.getPerformer());
    addField(builder, "TITLE", indentation, cueSheet.getTitle());
    addField(builder, "SONGWRITER", indentation, cueSheet.getSongwriter());
    addField(builder, "CDTEXTFILE", indentation, cueSheet.getCdTextFile());
    
    for (FileData fileData : cueSheet.getFileData())
    {
      serializeFileData(builder, fileData, indentation);
    }
    
    CueSheetSerializer.logger.exiting
      (CueSheetSerializer.class.getCanonicalName(), "serializeCueSheet(StringBuilder,CueSheet,String)");
  }
  
  /**
   * Serialize the FileData.
   * @param builder The StringBuilder to serialize to.
   * @param fileData The FileData to serialize.
   * @param indentation The current indentation.
   */
  private void serializeFileData(final StringBuilder builder, final FileData fileData, final String indentation)
  {
    CueSheetSerializer.logger.entering
      ( CueSheetSerializer.class.getCanonicalName()
      , "serializeFileData(StringBuilder,FileData,String)"
      , new Object[] {builder, fileData, indentation}
      );
    
    builder.append(indentation).append("FILE");
    
    if (fileData.getFile() != null)
    {
      builder.append(' ').append(quoteIfNecessary(fileData.getFile()));
    }
                
    if (fileData.getFileType() != null)
    {
      builder.append(' ').append(quoteIfNecessary(fileData.getFileType()));
    }

    builder.append('\n');
    
    for (TrackData trackData : fileData.getTrackData())
    {
      serializeTrackData(builder, trackData, indentation + this.getIndentationValue());
    }
    
    CueSheetSerializer.logger.exiting
      (CueSheetSerializer.class.getCanonicalName(), "serializeFileData(StringBuilder,FileData,String)");
  }

  /**
   * Serialize the TrackData.
   * @param builder The StringBuilder to serialize to.
   * @param trackData The TrackData to serialize.
   * @param indentation The current indentation.
   */
  private void serializeTrackData ( final StringBuilder builder
                                  , final TrackData trackData
                                  , final String indentation
                                  )
  {
    CueSheetSerializer.logger.entering
      ( CueSheetSerializer.class.getCanonicalName()
      , "serializeTrackData(StringBuilder,TrackData,String)"
      , new Object[] {builder, trackData, indentation}
      );
    
    builder.append(indentation).append("TRACK");
    
    if (trackData.getNumber() > -1)
    {
      builder.append(' ').append(String.format("%1$02d", trackData.getNumber()));
    }
                
    if (trackData.getDataType() != null)
    {
      builder.append(' ').append(quoteIfNecessary(trackData.getDataType()));
    }
    
    builder.append('\n');
    
    String childIndentation = indentation + this.getIndentationValue();

    addField(builder, "ISRC", childIndentation, trackData.getIsrcCode());
    addField(builder, "PERFORMER", childIndentation, trackData.getPerformer());
    addField(builder, "TITLE", childIndentation, trackData.getTitle());
    addField(builder, "SONGWRITER", childIndentation, trackData.getSongwriter());
    addField(builder, "PREGAP", childIndentation, trackData.getPregap());
    addField(builder, "POSTGAP", childIndentation, trackData.getPostgap());
    
    if (trackData.getFlags().size() > 0)
    {
      serializeFlags(builder, trackData.getFlags(), childIndentation);
    }
    
    for (Index index : trackData.getIndices())
    {
      serializeIndex(builder, index, childIndentation);
    }
    
    CueSheetSerializer.logger.exiting
      ( CueSheetSerializer.class.getCanonicalName()
      , "serializeTrackData(StringBuilder,TrackData,String)"
      );
  }
  
  /**
   * Serialize the flags.
   * @param builder The StringBuilder to serialize to.
   * @param flags The flags to serialize.
   * @param indentation The current indentation.
   */
  private void serializeFlags(final StringBuilder builder, final Set<String> flags, final String indentation)
  {
    CueSheetSerializer.logger.entering
      ( CueSheetSerializer.class.getCanonicalName()
      , "serializeFlags(StringBuilder,Set<String>,String)"
      , new Object[] {builder, flags, indentation}
      );
    
    builder.append(indentation).append("FLAGS");
    for (String flag : flags)
    {
      builder.append(' ').append(quoteIfNecessary(flag));
    }
    builder.append('\n');
    
    CueSheetSerializer.logger.exiting
      (CueSheetSerializer.class.getCanonicalName(), "serializeFlags(StringBuilder,Set<String>,String)");
  }
  
  /**
   * Serialize the index.
   * @param builder The StringBuilder to serialize to.
   * @param index The Index to serialize.
   * @param indentation The current indentation.
   */
  private void serializeIndex(final StringBuilder builder, final Index index, final String indentation)
  {
    CueSheetSerializer.logger.entering
      ( CueSheetSerializer.class.getCanonicalName()
      , "serializeIndex(StringBuilder,Index,String)"
      , new Object[] {builder, index, indentation}
      );
    
    builder.append(indentation).append("INDEX");
    if (index.getNumber() > -1)
    {
      builder.append(' ').append(String.format("%1$02d", index.getNumber()));
    }

    if (index.getPosition() != null)
    {
      builder.append(' ').append(formatPosition(index.getPosition()));
    }
    
    builder.append('\n');
    
    CueSheetSerializer.logger.exiting
      (CueSheetSerializer.class.getCanonicalName(), "serializeIndex(StringBuilder,Index,String)");
  }
  
  /**
   * Format the specified position.
   * @param position
   * @return The formatted position.
   */
  private String formatPosition(final Position position)
  {
    CueSheetSerializer.logger.entering(CueSheetSerializer.class.getCanonicalName(), "formatPosition(Position)", position);
    String result = String.format
      ("%1$02d:%2$02d:%3$02d", position.getMinutes(), position.getSeconds(), position.getFrames());
    CueSheetSerializer.logger.exiting(CueSheetSerializer.class.getCanonicalName(), "formatPosition(Position)", result);
    return result;
  }
  
  /**
   * Add a field to the builder. The field is only added if the value is != null.
   * @param cueBuilder
   * @param command The command to add.
   * @param value The value to add. Will be formatted as per formatPosition(Position).
   * @param indentation The indentation for this field.
   */
  private void addField ( final StringBuilder cueBuilder
                        , final String command
                        , final String indentation
                        , final Position value
                        )
  {
    CueSheetSerializer.logger.entering
      ( CueSheetSerializer.class.getCanonicalName()
      , "addField(StringBuilder,String,String,Position)"
      , new Object[] {cueBuilder, command, indentation, value}
      );
    if (value != null)
    {
      cueBuilder  .append(indentation)
                  .append(command)
                  .append(' ')
                  .append(formatPosition(value))
                  .append('\n');
    }
    CueSheetSerializer.logger.exiting
      ( CueSheetSerializer.class.getCanonicalName()
      , "addField(StringBuilder,String,String,Position)"
      );
  }

  /**
   * Add a field to the builder. The field is only added if the value is != null.
   * @param cueBuilder
   * @param command The command to add.
   * @param value The value to add.
   * @param indentation The indentation for this field.
   */
  private void addField ( final StringBuilder cueBuilder
                        , final String command
                        , final String indentation
                        , final String value
                        )
  {
    CueSheetSerializer.logger.entering
      ( CueSheetSerializer.class.getCanonicalName()
      , "addField(StringBuilder,String,String,String)"
      , new Object[] {cueBuilder, command, indentation, value}
      );
    
    if (value != null)
    {
      cueBuilder  .append(indentation)
                  .append(command)
                  .append(' ')
                  .append(quoteIfNecessary(value))
                  .append('\n');
    }
    
    CueSheetSerializer.logger.exiting
      (CueSheetSerializer.class.getCanonicalName(), "addField(StringBuilder,String,String,String)");
  }
  
  /**
   * Add a field to the builder. The field is only added if the value is > -1.
   * @param cueBuilder
   * @param command The command to add.
   * @param value The value to add.
   * @param indentation The indentation for this field.
   */
  private void addField ( final StringBuilder cueBuilder
                        , final String command
                        , final String indentation
                        , final int value
                        )
  {
    CueSheetSerializer.logger.entering
      ( CueSheetSerializer.class.getCanonicalName()
      , "addField(StringBuilder,String,String,int)"
      , new Object[] {cueBuilder, command, indentation, value}
      );
    
    if (value > -1)
    {
      cueBuilder  .append(indentation)
                  .append(command)
                  .append(' ')
                  .append("" + value)
                  .append('\n');
    }
    
    CueSheetSerializer.logger.exiting
      (CueSheetSerializer.class.getCanonicalName(), "addField(StringBuilder,String,String,int)");
  }
  
  /**
   * Enclose the string in double quotes if it contains whitespace.
   * @param input
   * @return The input string, which will be surrounded in double quotes if it contains any whitespace.
   */
  private String quoteIfNecessary(final String input)
  {
    CueSheetSerializer.logger.entering(CueSheetSerializer.class.getCanonicalName(), "quoteIfNecessary(String)", input);
    
    // Search for whitespace
    for (int index = 0; index < input.length(); index++)
    {
      if (Character.isWhitespace(input.charAt(index)))
      {
        String result = '"' + input + '"';
        logger.exiting(CueSheetSerializer.class.getCanonicalName(), "quoteIfNecessary(String)", result);
        return result;
      }
    }
    
    CueSheetSerializer.logger.exiting(CueSheetSerializer.class.getCanonicalName(), "quoteIfNecessary(String)", input);
    return input;
  }

  /**
   * Get the character sequence for a single indentation value.
   * @return The character sequence for a single indentation value.
   */
  public String getIndentationValue()
  {
    CueSheetSerializer.logger.entering(CueSheetSerializer.class.getCanonicalName(), "getIndentationValue()");
    CueSheetSerializer.logger.exiting
      (CueSheetSerializer.class.getCanonicalName(), "getIndentationValue()", this.indentationValue);
    return this.indentationValue;
  }

  /**
   * Set the character sequence for a single indentation value.
   * @param indentationValue The character sequence for a single indentation value.
   */
  public void setIndentationValue(final String indentationValue)
  {
    CueSheetSerializer.logger.entering
      (CueSheetSerializer.class.getCanonicalName(), "setIndentationValue(String)", indentationValue);
    this.indentationValue = indentationValue;
    CueSheetSerializer.logger.exiting(CueSheetSerializer.class.getCanonicalName(), "setIndentationValue()");
  }
}
