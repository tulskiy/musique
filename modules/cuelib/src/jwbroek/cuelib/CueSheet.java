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
import java.util.logging.Logger;

/**
 * Simple representation of a cue sheet.
 * @author jwbroek
 */
public class CueSheet
{
  /**
   * Enumeration of available metadata fields. These can be consulted through the
   * {@link #getMetaData(jwbroek.cuelib.CueSheet.MetaDataField)} method.
   */
  public enum MetaDataField
  {
    /**
     * Performer of the album.
     */
    ALBUMPERFORMER,
    /**
     * Songwriter of the album.
     */
    ALBUMSONGWRITER,
    /**
     * Title of the album.
     */
    ALBUMTITLE,
    /**
     * The disc's media catalog number.
     */
    CATALOG,
    /**
     * CD-TEXT file.
     */
    CDTEXTFILE,
    /**
     * Album comment.
     */
    COMMENT,
    /**
     * An id for the disc. Typically the freedb disc id.
     */
    DISCID,
    /**
     * Genre of the album.
     */
    GENRE,
    /**
     * ISRC code of a track.
     */
    ISRCCODE,
    /**
     * Performer of the album or track.
     */
    PERFORMER,
    /**
     * Songwriter of the album or track.
     */
    SONGWRITER,
    /**
     * Title of the album or track.
     */
    TITLE,
    /**
     * Number of a track.
     */
    TRACKNUMBER,
    /**
     * Performer of a track.
     */
    TRACKPERFORMER,
    /**
     * Songwriter of a track.
     */
    TRACKSONGWRITER,
    /**
     * Title of a track.
     */
    TRACKTITLE,
    /**
     * Year of the album.
     */
    YEAR
  }
  
  /**
   * Messages that concern this CueSheet.
   */
  private final List<Message> messages = new ArrayList<Message>();
  
  // Various components of a cue sheet.
  /**
   * The file components of the cue sheet.
   */
  private final List<FileData> fileData = new ArrayList<FileData>();
  /**
   * The disc's media catalog number. It should be 13 digits and compliant with UPC/EAN rules. May be null.
   */
  private String catalog = null;
  /**
   * The file containing the cd text data. May be null.
   */
  private String cdTextFile = null;
  /**
   * The performer of the album. For using as cd-text, it should be a maximum of 80 characters long.
   * May be null.
   */
  private String performer = null;
  /**
   * The title of the album. For burning as cd-text, it should be a maximum of 80 characters long.
   * May be null.
   */
  private String title = null;
  /**
   * The songwriter of the album. For burning as cd-text, it should be a maximum of 80 characters long.
   * May be null.
   */
  private String songwriter = null;
  /**
   * A comment as is typically copied to ID3 tags. It may be null.
   */
  private String comment = null;
  /**
   * The year of the album. -1 signifies that it has not been specified.
   */
  private int year = -1;
  /**
   * An id for the disc. Typically the freedb disc id. May be null.
   */
  private String discid = null;
  /**
   * The genre of the album. May be null.
   */
  private String genre = null;
  
  /**
   * The logger for this class.
   */
  private final static Logger logger = Logger.getLogger(CueSheet.class.getCanonicalName());
  
  /**
   * Create a new CueSheet.
   */
  public CueSheet()
  {
    CueSheet.logger.entering(CueSheet.class.getCanonicalName(), "CueSheet()");
    CueSheet.logger.exiting(CueSheet.class.getCanonicalName(), "CueSheet()");
  }
  
  /**
   * Convenience method for getting metadata from the cue sheet. If a certain metadata field is not set, the
   * method will return the empty string. When a field is ambiguous (such as the track number on a cue sheet
   * instead of on a specific track), an IllegalArgumentException will be thrown. Otherwise, this method will
   * attempt to give a sensible answer, possibly by searching through the cue sheet.
   * @param metaDataField
   * @return The specified metadata.
   */
  public String getMetaData(MetaDataField metaDataField) throws IllegalArgumentException
  {
    CueSheet.logger.entering(CueSheet.class.getCanonicalName(), "getMetaData(MetaDataField)", metaDataField);
    String result;
    
    switch (metaDataField)
    {
      case CATALOG:
        result = this.getCatalog()==null?"":this.getCatalog();
        break;
      case CDTEXTFILE:
        result = this.getCdTextFile()==null?"":this.getCdTextFile();
        break;
      case COMMENT:
        result = this.getComment()==null?"":this.getComment();
        break;
      case DISCID:
        result = this.getDiscid()==null?"":this.getDiscid();
        break;
      case GENRE:
        result = this.getGenre()==null?"":this.getGenre();
        break;
      case PERFORMER:
      case ALBUMPERFORMER:
        result = this.getPerformer()==null?"":this.getPerformer();
        break;
      case SONGWRITER:
      case ALBUMSONGWRITER:
        result = this.getSongwriter()==null?"":this.getSongwriter();
        break;
      case TITLE:
      case ALBUMTITLE:
        result = this.getTitle()==null?"":this.getTitle();
        break;
      case YEAR:
        result = this.getYear()==-1?"":""+this.getYear();
        break;
      default:
        IllegalArgumentException exception = new IllegalArgumentException
          ("Unsupported field: " + metaDataField.toString());
        logger.throwing(CueSheet.class.getCanonicalName(), "getMetaData(MetaDataField)", exception);
        throw exception;
    }
    
    logger.exiting(CueSheet.class.getCanonicalName(), "getMetaData(MetaDataField)", result);
    return result;
  }

  /**
   * Add an error message to this cue sheet.
   * @param lineOfInput The line of input that caused the error.
   * @param message A message describing the error.
   */
  public void addError(LineOfInput lineOfInput, String message)
  {
    logger.entering
      (CueSheet.class.getCanonicalName(), "addError(LineOfInput,String)", new Object[]{lineOfInput, message});
    this.messages.add(new Error(lineOfInput, message));
    logger.exiting(CueSheet.class.getCanonicalName(), "addError(LineOfInput,String)");
  }
  
  /**
   * Add a warning message to this cue sheet.
   * @param lineOfInput The line of input that caused the warning.
   * @param message A message describing the warning.
   */
  public void addWarning(LineOfInput lineOfInput, String message)
  {
    logger.entering
      (CueSheet.class.getCanonicalName(), "addWarning(LineOfInput,String)", new Object[]{lineOfInput, message});
    this.messages.add(new Warning(lineOfInput, message));
    logger.exiting(CueSheet.class.getCanonicalName(), "addWarning(LineOfInput,String)");
  }
  
  /**
   * Get all track data described in this cue sheet.
   * @return All track data associated described in this cue sheet.
   */
  public List<TrackData> getAllTrackData()
  {
    logger.entering(CueSheet.class.getCanonicalName(), "getAllTrackData()");
    
    List<TrackData> allTrackData = new ArrayList<TrackData>();
    
    for (FileData fileData: this.fileData)
    {
      allTrackData.addAll(fileData.getTrackData());
    }
    
    logger.exiting(CueSheet.class.getCanonicalName(), "getAllTrackData()", allTrackData);
    
    return allTrackData;
  }
  
  /**
   * Get the disc's media catalog number. It should be 13 digits and compliant with UPC/EAN rules.
   * Null signifies that the catalog has not been specified.
   * @return The disc's media catalog number
   */
  public String getCatalog()
  {
    logger.entering(CueSheet.class.getCanonicalName(), "getCatalog()");
    logger.exiting(CueSheet.class.getCanonicalName(), "getCatalog()", this.catalog);
    return this.catalog;
  }
  
  /**
   * Set the disc's media catalog number. It should be 13 digits and compliant with UPC/EAN rules.
   * Null signifies that the catalog has not been specified.
   * @param catalog The disc's media catalog number
   */
  public void setCatalog(final String catalog)
  {
    logger.entering(CueSheet.class.getCanonicalName(), "setCatalog(String)", catalog);
    this.catalog = catalog;
    logger.exiting(CueSheet.class.getCanonicalName(), "setCatalog(String)");
  }
  
  /**
   * Get the file containing cd text data. Null signifies that no such file has been specified.
   * @return The file containing cd text data.
   */
  public String getCdTextFile()
  {
    logger.entering(CueSheet.class.getCanonicalName(), "getCdTextFile()");
    logger.exiting(CueSheet.class.getCanonicalName(), "getCdTextFile()", this.cdTextFile);
    return this.cdTextFile;
  }
  
  /**
   * Set the file containing cd text data. Null signifies that no such file has been specified.
   * @param cdTextFile The file containing cd text data
   */
  public void setCdTextFile(final String cdTextFile)
  {
    logger.entering(CueSheet.class.getCanonicalName(), "setCdTextFile(String)", cdTextFile);
    this.cdTextFile = cdTextFile;
    logger.exiting(CueSheet.class.getCanonicalName(), "setCdTextFile(String)");
  }
  
  /**
   * Get the performer of the album. For burning as cd-text, it should be a maximum of 80 characters.
   * May be null. 
   * @return The performer of the album
   */
  public String getPerformer()
  {
    logger.entering(CueSheet.class.getCanonicalName(), "getPerformer()");
    logger.exiting(CueSheet.class.getCanonicalName(), "getPerformer()", this.performer);
    return this.performer;
  }
  
  /**
   * Set the performer of the album. For burning as cd-text, it should be a maximum of 80 characters.
   * May be null. 
   * @param performer The performer of the album.
   */
  public void setPerformer(final String performer)
  {
    logger.entering(CueSheet.class.getCanonicalName(), "setPerformer(String)", performer);
    this.performer = performer;
    logger.exiting(CueSheet.class.getCanonicalName(), "setPerformer(String)");
  }
  
  /**
   * Get the songwriter of the album. For burning as cd-text, it should be a maximum of 80 characters.
   * May be null. 
   * @return The songwriter of the album
   */
  public String getSongwriter()
  {
    logger.entering(CueSheet.class.getCanonicalName(), "getSongwriter()");
    logger.exiting(CueSheet.class.getCanonicalName(), "getSongwriter()", this.songwriter);
    return this.songwriter;
  }
  
  /**
   * Set the songwriter of the album. For burning as cd-text, it should be a maximum of 80 characters.
   * May be null. 
   * @param songwriter The songwriter of the album.
   */
  public void setSongwriter(final String songwriter)
  {
    logger.entering(CueSheet.class.getCanonicalName(), "setSongwriter(String)", songwriter);
    this.songwriter = songwriter;
    logger.exiting(CueSheet.class.getCanonicalName(), "setSongwriter(String)");
  }
  
  /**
   * Get the title of the album. For burning as cd-text, it should be a maximum of 80 characters.
   * May be null. 
   * @return The title of the album
   */
  public String getTitle()
  {
    logger.entering(CueSheet.class.getCanonicalName(), "getTitle()");
    logger.exiting(CueSheet.class.getCanonicalName(), "getTitle()", this.title);
    return this.title;
  }
  
  /**
   * Set the title of the album. For burning as cd-text, it should be a maximum of 80 characters.
   * May be null. 
   * @param title The title of the album.
   */
  public void setTitle(final String title)
  {
    logger.entering(CueSheet.class.getCanonicalName(), "setTitle(String)", title);
    this.title = title;
    logger.exiting(CueSheet.class.getCanonicalName(), "setTitle(String)");
  }
  
  /**
   * Get the id for the disc. Typically the freedb disc id. May be null.
   * @return The id for the disc.
   */
  public String getDiscid()
  {
    logger.entering(CueSheet.class.getCanonicalName(), "getDiscid()");
    logger.exiting(CueSheet.class.getCanonicalName(), "getDiscid()", this.discid);
    return this.discid;
  }

  /**
   * Set the id for the disc. Typically the freedb disc id. May be null.
   * @param discid The id for the disc.
   */
  public void setDiscid(final String discid)
  {
    logger.entering(CueSheet.class.getCanonicalName(), "setDiscid(String)", discid);
    this.discid = discid;
    logger.exiting(CueSheet.class.getCanonicalName(), "setDiscid(String)");
  }

  /**
   * Get the genre of the album. May be null.
   * @return The genre of the album.
   */
  public String getGenre()
  {
    logger.entering(CueSheet.class.getCanonicalName(), "getGenre()");
    logger.exiting(CueSheet.class.getCanonicalName(), "getGenre()", this.genre);
    return this.genre;
  }

  /**
   * Set the genre of the album. May be null.
   * @param genre The genre of the album.
   */
  public void setGenre(final String genre)
  {
    logger.entering(CueSheet.class.getCanonicalName(), "setGenre(String)", genre);
    this.genre = genre;
    logger.exiting(CueSheet.class.getCanonicalName(), "setGenre(String)");
  }

  /**
   * Get the year of the album. -1 indicated that no year is set.
   * @return The year of the album.
   */
  public int getYear()
  {
    logger.entering(CueSheet.class.getCanonicalName(), "getYear()");
    logger.exiting(CueSheet.class.getCanonicalName(), "getYear()", this.year);
    return this.year;
  }

  /**
   * Set the year of the album. -1 indicated that no year is set.
   * @param year The year of the album.
   */
  public void setYear(final int year)
  {
    logger.entering(CueSheet.class.getCanonicalName(), "setYear(int)", year);
    this.year = year;
    logger.exiting(CueSheet.class.getCanonicalName(), "setYear(int)");
  }

  /**
   * The comment for the album. May be null.
   * @return The comment for the album.
   */
  public String getComment()
  {
    logger.entering(CueSheet.class.getCanonicalName(), "getComment()");
    logger.exiting(CueSheet.class.getCanonicalName(), "getComment()", this.comment);
    return this.comment;
  }

  /**
   * Set the comment for the album. May be null.
   * @param comment The comment for the album.
   */
  public void setComment(final String comment)
  {
    logger.entering(CueSheet.class.getCanonicalName(), "setComment(String)", comment);
    this.comment = comment;
    logger.exiting(CueSheet.class.getCanonicalName(), "setComment(String)");
  }
  
  /**
   * Get the file data for this cue sheet.
   * @return The file data for this cue sheet.
   */
  public List<FileData> getFileData()
  {
    logger.entering(CueSheet.class.getCanonicalName(), "getFileData()");
    logger.exiting(CueSheet.class.getCanonicalName(), "getFileData()", this.fileData);
    return this.fileData;
  }
  
  /**
   * Get the parsing messages for this cue sheet.
   * @return The parsing messages for the cue sheet.
   */
  public List<Message> getMessages()
  {
    logger.entering(CueSheet.class.getCanonicalName(), "getMessages()");
    logger.exiting(CueSheet.class.getCanonicalName(), "getMessages()", this.messages);
    return this.messages;
  }
}
