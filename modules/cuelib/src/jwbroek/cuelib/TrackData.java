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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import jwbroek.cuelib.CueSheet.MetaDataField;

/**
 * Simple representation of a TRACK block of a cue sheet.
 * @author jwbroek
 */
public class TrackData
{
  /**
   * The logger for this class.
   */
  private final static Logger logger = Logger.getLogger(TrackData.class.getCanonicalName());
  /**
   * The indices in this track,
   */
  private final List<Index> indices = new ArrayList<Index>();
  /**
   * The flags for this track.
   */
  private final Set<String> flags = new TreeSet<String>();
  /**
   * The track number. -1 signifies that it has not been set.
   */
  private int number = -1;
  /**
   * The data type of this track. Null signifies that it has not been set.
   */
  private String dataType = null;
  /**
   * The ISRC code of this track. Null signifies that it has not been set.
   */
  private String isrcCode = null;
  /**
   * The performer of this track. Null signifies that it has not been set. Should be a maximum of 80
   * characters if you want to burn to CD-TEXT.
   */
  private String performer = null;
  /**
   * The title of this track. Null signifies that it has not been set. Should be a maximum of 80
   * characters if you want to burn to CD-TEXT.
   */
  private String title = null;
  /**
   * The pregap of this track. Null signifies that it has not been set.
   */
  private Position pregap = null;
  /**
   * The postgap of this track. Null signifies that it has not been set.
   */
  private Position postgap = null;
  /**
   * The songwriter of this track. Null signifies that it has not been set. Should be a maximum of 80
   * characters if you want to burn to CD-TEXT.
   */
  private String songwriter = null;
  /**
   * The file data that this track data belongs to.
   */
  private FileData parent;
  
  /**
   * Create a new TrackData instance.
   * @param parent The file data that this track data belongs to. Should not be null.
   */
  public TrackData(final FileData parent)
  {
    TrackData.logger.entering(TrackData.class.getCanonicalName(), "TrackData(FileData)", parent);
    this.parent = parent;
    TrackData.logger.exiting(TrackData.class.getCanonicalName(), "TrackData(FileData)");
  }
  
  /**
   * Create a new TrackData instance.
   * @param parent The file data that this track data belongs to. Should not be null.
   * @param number The track number. -1 signifies that it has not been set.
   * @param dataType The data type of this track. Null signifies that it has not been set.
   */
  public TrackData(final FileData parent, final int number, final String dataType)
  {
    TrackData.logger.entering
      ( TrackData.class.getCanonicalName()
      , "TrackData(FileData,int,String)"
      , new Object[] {parent, number, dataType}
      );
    this.parent = parent;
    this.number = number;
    this.dataType = dataType;
    TrackData.logger.exiting(TrackData.class.getCanonicalName(), "TrackData(FileData,int,String)");
  }
  
  /**
   * Convenience method for getting metadata from the cue sheet. If a certain metadata field is not set, the method
   * will return the empty string. When a field is ambiguous (such as the track number on a cue sheet instead of on a
   * specific track), an IllegalArgumentException will be thrown. Otherwise, this method will attempt to give a sensible
   * answer, possibly by searching through the cue sheet.
   * @param metaDataField
   * @return The specified metadata.
   */
  public String getMetaData(final MetaDataField metaDataField) throws IllegalArgumentException
  {
    TrackData.logger.entering(TrackData.class.getCanonicalName(), "getMetaData(MetaDataField)", metaDataField);
    String result;
    switch (metaDataField)
    {
      case ISRCCODE:
        result = this.getIsrcCode()==null?"":this.getIsrcCode();
        break;
      case PERFORMER:
        result = this.getPerformer()==null?this.getParent().getParent().getPerformer():this.getPerformer();
        break;
      case TRACKPERFORMER:
        result = this.getPerformer()==null?"":this.getPerformer();
        break;
      case SONGWRITER:
        result = this.getSongwriter()==null?this.getParent().getParent().getSongwriter():this.getSongwriter();
        break;
      case TRACKSONGWRITER:
        result = this.getSongwriter();
        break;
      case TITLE:
        result = this.getTitle()==null?this.getParent().getParent().getTitle():this.getTitle();
        break;
      case TRACKTITLE:
        result = this.getTitle();
        break;
      case TRACKNUMBER:
        result = Integer.toString(this.getNumber());
        break;
      default:
        result = this.getParent().getParent().getMetaData(metaDataField);
        break;
    }
    
    TrackData.logger.exiting(TrackData.class.getCanonicalName(), "getMetaData()", result);
    return result;
  }

  /**
   * Get the data type of this track. Null signifies that it has not been set.
   * @return The data type of this track. Null signifies that it has not been set.
   */
  public String getDataType()
  {
    TrackData.logger.entering(TrackData.class.getCanonicalName(), "getDataType()");
    TrackData.logger.exiting(TrackData.class.getCanonicalName(), "getDataType()", this.dataType);
    return this.dataType;
  }
  
  /**
   * Set the data type of this track. Null signifies that it has not been set.
   * @param dataType The data type of this track. Null signifies that it has not been set.
   */
  public void setDataType(final String dataType)
  {
    TrackData.logger.entering(TrackData.class.getCanonicalName(), "setDataType(String)", dataType);
    this.dataType = dataType;
    TrackData.logger.exiting(TrackData.class.getCanonicalName(), "setDataType(String)");
  }
  
  /**
   * Get the ISRC code of this track. Null signifies that it has not been set.
   * @return The ISRC code of this track. Null signifies that it has not been set.
   */
  public String getIsrcCode()
  {
    TrackData.logger.entering(TrackData.class.getCanonicalName(), "getIsrcCode()");
    TrackData.logger.exiting(TrackData.class.getCanonicalName(), "getIsrcCode()", this.isrcCode);
    return this.isrcCode;
  }
  
  /**
   * Set the ISRC code of this track. Null signifies that it has not been set.
   * @param isrcCode The ISRC code of this track. Null signifies that it has not been set.
   */
  public void setIsrcCode(final String isrcCode)
  {
    TrackData.logger.entering(TrackData.class.getCanonicalName(), "setIsrcCode(String)", isrcCode);
    this.isrcCode = isrcCode;
    TrackData.logger.exiting(TrackData.class.getCanonicalName(), "setIsrcCode(String)");
  }
  
  /**
   * Get the track number. -1 signifies that it has not been set.
   * @return The track number. -1 signifies that it has not been set.
   */
  public int getNumber()
  {
    TrackData.logger.entering(TrackData.class.getCanonicalName(), "getNumber()");
    TrackData.logger.exiting(TrackData.class.getCanonicalName(), "getNumber()", this.number);
    return this.number;
  }
  
  /**
   * Set the track number. -1 signifies that it has not been set.
   * @param number The track number. -1 signifies that it has not been set.
   */
  public void setNumber(final int number)
  {
    TrackData.logger.entering(TrackData.class.getCanonicalName(), "setNumber(int)", number);
    this.number = number;
    TrackData.logger.exiting(TrackData.class.getCanonicalName(), "setNumber(int)");
  }
  
  /**
   * Get the performer of this track. Null signifies that it has not been set.
   * @return The performer of this track. Null signifies that it has not been set.
   */
  public String getPerformer()
  {
    TrackData.logger.entering(TrackData.class.getCanonicalName(), "getPerformer()");
    TrackData.logger.exiting(TrackData.class.getCanonicalName(), "getPerformer()", this.performer);
    return this.performer;
  }
  
  /**
   * Set the performer of this track. Null signifies that it has not been set.
   * @param performer The performer of this track. Null signifies that it has not been set. Should be a maximum of 80
   * characters if you want to burn to CD-TEXT.
   */
  public void setPerformer(final String performer)
  {
    TrackData.logger.entering(TrackData.class.getCanonicalName(), "setPerformer(String)", performer);
    this.performer = performer;
    TrackData.logger.exiting(TrackData.class.getCanonicalName(), "setPerformer(String)");
  }
  
  /**
   * Get the postgap of this track. Null signifies that it has not been set.
   * @return The postgap of this track. Null signifies that it has not been set.
   */
  public Position getPostgap()
  {
    TrackData.logger.entering(TrackData.class.getCanonicalName(), "getPostgap()");
    TrackData.logger.exiting(TrackData.class.getCanonicalName(), "getPostgap()", this.postgap);
    return this.postgap;
  }
  
  /**
   * Set the postgap of this track. Null signifies that it has not been set.
   * @param postgap The postgap of this track. Null signifies that it has not been set.
   */
  public void setPostgap(final Position postgap)
  {
    TrackData.logger.entering(TrackData.class.getCanonicalName(), "setPostgap(Position)", postgap);
    this.postgap = postgap;
    TrackData.logger.exiting(TrackData.class.getCanonicalName(), "setPostgap(Position)");
  }
  
  /**
   * Get the pregap of this track. Null signifies that it has not been set.
   * @return The pregap of this track. Null signifies that it has not been set.
   */
  public Position getPregap()
  {
    TrackData.logger.entering(TrackData.class.getCanonicalName(), "getPregap()");
    TrackData.logger.exiting(TrackData.class.getCanonicalName(), "getPregap()", this.pregap);
    return this.pregap;
  }
  
  /**
   * Set the pregap of this track. Null signifies that it has not been set.
   * @param pregap The pregap of this track. Null signifies that it has not been set.
   */
  public void setPregap(final Position pregap)
  {
    TrackData.logger.entering(TrackData.class.getCanonicalName(), "setPregap(Position)", pregap);
    this.pregap = pregap;
    TrackData.logger.exiting(TrackData.class.getCanonicalName(), "setPregap(Position)");
  }
  
  /**
   * Get the songwriter of this track. Null signifies that it has not been set.
   * @return The songwriter of this track. Null signifies that it has not been set. 
   */
  public String getSongwriter()
  {
    TrackData.logger.entering(TrackData.class.getCanonicalName(), "getSongwriter()");
    TrackData.logger.exiting(TrackData.class.getCanonicalName(), "getSongwriter()", this.songwriter);
    return this.songwriter;
  }
  
  /**
   * Set the songwriter of this track. Null signifies that it has not been set. Should be a maximum of 80
   * characters if you want to burn to CD-TEXT.
   * @param songwriter The songwriter of this track. Null signifies that it has not been set. Should be a maximum of 80
   * characters if you want to burn to CD-TEXT.
   */
  public void setSongwriter(final String songwriter)
  {
    TrackData.logger.entering(TrackData.class.getCanonicalName(), "setSongwriter(String)", songwriter);
    this.songwriter = songwriter;
    TrackData.logger.exiting(TrackData.class.getCanonicalName(), "setSongwriter(String)");
  }
  
  /**
   * Get the title of this track. Null signifies that it has not been set.
   * @return The title of this track. Null signifies that it has not been set. 
   */
  public String getTitle()
  {
    TrackData.logger.entering(TrackData.class.getCanonicalName(), "getTitle()");
    TrackData.logger.exiting(TrackData.class.getCanonicalName(), "getTitle()", this.title);
    return this.title;
  }
  
  /**
   * Set the title of this track. Null signifies that it has not been set. Should be a maximum of 80
   * characters if you want to burn to CD-TEXT.
   * @param title The title of this track. Null signifies that it has not been set. Should be a maximum of 80
   * characters if you want to burn to CD-TEXT.
   */
  public void setTitle(final String title)
  {
    TrackData.logger.entering(TrackData.class.getCanonicalName(), "setTitle(String)", title);
    this.title = title;
    TrackData.logger.exiting(TrackData.class.getCanonicalName(), "setTitle(String)");
  }
  
  /**
   * Get the index with the specified number, or null if there is no such index.
   * @param number The number of the desired index.
   * @return The index with the specified number, or null if there is no such index.
   */
  public Index getIndex(final int number)
  {
    TrackData.logger.entering(TrackData.class.getCanonicalName(), "getIndex(int)");
    
    Index result = null;
    
    // Note: we have to pass all indices until we've found the right one, as we don't enforce that indices are sorted.
    // Normally, this shouldn't be a problem, as there are generally very few indices. (Only rarely more than 2).
    indexLoop:
    for (Index index : this.indices)
    {
      if (index.getNumber()==number)
      {
        result = index;
        break indexLoop;  // No need to continue searching, so break out of the loop.
      }
    }
    
    TrackData.logger.exiting(TrackData.class.getCanonicalName(), "getIndex(int)", result);
    return result;
  }
  
  /**
   * Get the indices for this track data.
   * @return The indices for this track data.
   */
  public List<Index> getIndices()
  {
    TrackData.logger.entering(TrackData.class.getCanonicalName(), "getIndices()");
    TrackData.logger.exiting(TrackData.class.getCanonicalName(), "getIndices()", this.indices);
    return this.indices;
  }
  
  /**
   * Get the flags for this track data.
   * @return The flags for this track data.
   */
  public Set<String> getFlags()
  {
    TrackData.logger.entering(TrackData.class.getCanonicalName(), "getFlags()");
    TrackData.logger.exiting(TrackData.class.getCanonicalName(), "getFlags()", this.flags);
    return this.flags;
  }

  /**
   * Get the file data that this track data belong to..
   * @return The file data that this track data belong to..
   */
  public FileData getParent()
  {
    TrackData.logger.entering(TrackData.class.getCanonicalName(), "getParent()");
    TrackData.logger.exiting(TrackData.class.getCanonicalName(), "getParent()", this.parent);
    return this.parent;
  }

  /**
   * Set the file data that this track data belong to..
   * @param parent The file data that this track data belong to..
   */
  public void setParent(final FileData parent)
  {
    TrackData.logger.entering(TrackData.class.getCanonicalName(), "setParent(FileData)", parent);
    this.parent = parent;
    TrackData.logger.exiting(TrackData.class.getCanonicalName(), "setParent(FileData)");
  }
}
