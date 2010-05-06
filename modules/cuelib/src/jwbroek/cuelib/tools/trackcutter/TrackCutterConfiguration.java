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
package jwbroek.cuelib.tools.trackcutter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFileFormat;

import jwbroek.cuelib.CueSheet;
import jwbroek.cuelib.FileData;
import jwbroek.cuelib.TrackData;
import jwbroek.util.StringReplacer;

/**
 * This class represents a configuration for a TrackCutter instance. It takes care of much of the bookkeeping,
 * allowing TrackCutter to focus on his core task.
 * @author jwbroek
 */
public class TrackCutterConfiguration
{
  /**
   * Allowed modes for pregap handling.
   */
  public enum PregapHandling
  {
    PREPEND,
    DISCARD,
    SEPARATE
  };
  
  /**
   * Parent directory for relative paths.
   */
  private File parentDirectory = null;
  /**
   * How to handle pregaps.
   */
  private PregapHandling pregapHandling = PregapHandling.DISCARD;
  /**
   * Only process pregaps with a frame length greater than this.
   */
  private long pregapFrameLengthThreshold = 0;
  /**
   * Audio type to convert to.
   */
  private AudioFileFormat.Type targetType = AudioFileFormat.Type.WAVE;
  /**
   * Whether or not error output from post-processing should be redirected.
   */
  private boolean redirectErr = false;
  /**
   * Whether or not standard output from post-processing should be redirected.
   */
  private boolean redirectStdOut = false;
  /**
   * Whether or not we should do post-processing.
   */
  private boolean doPostProcessing = false;
  /**
   * Whether or not we should redirect output directly to post-processing.
   */
  private boolean redirectToPostprocessing = false;
  /**
   * Template for the file name of the cut tracks.
   */
  private String cutFileNameTemplate = "<artist>_<album>_<track>_<title>.wav";
  /**
   * Template for the file name of the post-processed tracks.
   */
  private String postProcessFileNameTemplate = "<artist>/<album>/<track>_<title>.mp3";
  /**
   * Template for the post-processing command.
   */
  private String postProcessCommandTemplate =
    "C:\\lame\\lame.exe --vbr-new -V 0 -t --tt \"<title>\" --ta \"<artist>\" --tl \"<album>\" --ty \"<year>\""
    + " --tc \"<comment>\" --tn \"<track>\" --tg \"<genre>\" \"<targetFile>\" \"<postProcessFile>\"";
  /**
   * Template for the file name of the cut pregaps.
   */
  private String pregapCutFileNameTemplate = "<artist>_<album>_<track>__pre_<title>.wav";
  /**
   * Template for the file name of the post-processed pregaps.
   */
  private String pregapPostProcessFileNameTemplate = "<artist>/<album>/<track>__pre_<title>.mp3";
  /**
   * Template for the post-processing command for the pregaps.
   */
  private String pregapPostProcessCommandTemplate =
    "C:\\lame\\lame.exe --vbr-new -V 0 -t --tt \"Pregap of <title>\" --ta \"<artist>\" --tl \"<album>\" --ty \"<year>\""
    + " --tc \"Pregap of <title>\" --tn \"<track>\" --tg \"<genre>\" \"<targetFile>\" \"<postProcessFile>\"";
  /**
   * The logger for this class.
   */
  private final static Logger logger = Logger.getLogger(TrackCutterConfiguration.class.getCanonicalName());
  
  /**
   * Replacer for the template values.
   */
  public static final StringReplacer templateReplacer =
    new StringReplacer(getHumanReadableToFormatStringReplacements());
  
  /**
   * Create a new TrackCutterConfiguration instance, with default values.
   */
  public TrackCutterConfiguration()
  {
    TrackCutterConfiguration.logger.entering
      (TrackCutterConfiguration.class.getCanonicalName(), "TrackCutterConfiguration()");
    TrackCutterConfiguration.logger.exiting
      (TrackCutterConfiguration.class.getCanonicalName(), "TrackCutterConfiguration()");
  }
  
  /**
   * Get a replacements map for human readable fields to formatting string fields.
   * @return A replacements map for human readable fields to formatting string fields.
   */
  private static Map<String, String> getHumanReadableToFormatStringReplacements()
  {
    TrackCutterConfiguration.logger.entering
      (TrackCutterConfiguration.class.getCanonicalName(), "getHumanReadableToFormatStringReplacements()");
    HashMap<String, String> replacements = new HashMap<String, String>();
    replacements.put("<title>", "%1$s");
    replacements.put("<artist>", "%2$s");
    replacements.put("<album>", "%3$s");
    replacements.put("<year>", "%4$s");
    replacements.put("<comment>", "%5$s");
    replacements.put("<track>", "%6$s");
    replacements.put("<genre>", "%7$s");
    replacements.put("<cutFile>", "%8$s");
    replacements.put("<postProcessFile>", "%9$s");
    TrackCutterConfiguration.logger.exiting
      ( TrackCutterConfiguration.class.getCanonicalName()
      , "getHumanReadableToFormatStringReplacements()"
      , replacements
      );
    return replacements;
  }
  
  /**
   * Get a file instance representing the audio file specified in the FileData.
   * @param fileData
   * @return A file instance representing the audio file specified in the FileData.
   */
  public File getAudioFile(final FileData fileData)
  {
    TrackCutterConfiguration.logger.entering(TrackCutterConfiguration.class.getCanonicalName(), "getAudioFile(FileData)");
    File audioFile = new File(fileData.getFile());
    if (audioFile.getParent()==null)
    {
      audioFile = new File(this.getParentDirectory(), fileData.getFile());
    }
    TrackCutterConfiguration.logger.exiting
      (TrackCutterConfiguration.class.getCanonicalName(), "getAudioFile(FileData)", audioFile);
    return audioFile;
  }
  
  /**
   * Normalize the specified file name (without path component) so that it will likely be valid on modern
   * file and operating systems.
   * @param fileName The file name to normalize. Must not contain a path component.
   * @return The input file name, normalized to be likely to be valid on modern file and operating systems.
   */
  private static String normalizeFileName(final String fileName)
  {
    TrackCutterConfiguration.logger.entering
      (TrackCutterConfiguration.class.getCanonicalName(), "normalizeFileName(String)", fileName);
    StringBuilder builder = new StringBuilder(fileName.length());
    int length = fileName.length();
    for (int index = 0; index < length; index++)
    {
      char currentChar = fileName.charAt(index);
      if (currentChar < 32)
      {
        // No control characters in file name.
        builder.append('_');
      }
      else
      {
        switch (currentChar)
        {
          // These characters are likely to be troublesome in file names.
          case '/':
          case '\\':
          case ':':
          case '*':
          case '?':
          case '"':
          case '|':
            builder.append('_');
            break;
          // Everything else should be okay on modern file system.
          default:
            builder.append(currentChar);
            break;
        }
      }
    }
    String result = builder.toString();
    TrackCutterConfiguration.logger.exiting
      (TrackCutterConfiguration.class.getCanonicalName(), "normalizeFileName(String)", result);
    return result;
  }
  
  /**
   * Get the expanded file from a template and track data.
   * @param trackData TrackData to use for expanding the file name template.
   * @param fileNameTemplate The template for the file name.
   * @return The expanded file.
   */
  public File getFileFromTemplate(final TrackData trackData, final String fileNameTemplate)
  {
    TrackCutterConfiguration.logger.entering
      ( TrackCutterConfiguration.class.getCanonicalName()
      , "getFileFromTemplate(TrackData, String)"
      , new Object [] {trackData, fileNameTemplate}
      );
    String targetFileName = getExpandedFileName(trackData, fileNameTemplate);
    
    File targetFile = new File(targetFileName);
    if (!targetFile.isAbsolute())
    {
      targetFile = new File(this.getParentDirectory(), targetFileName);
    }
    
    TrackCutterConfiguration.logger.exiting
      ( TrackCutterConfiguration.class.getCanonicalName()
      , "getFileFromTemplate(TrackData, String)"
      , targetFile
      );
    return targetFile;
  }
  
  /**
   * Get the expanded file name from a template and track data.
   * @param trackData TrackData to use for expanding the file name template.
   * @param fileNameTemplate The template for the file name.
   * @return The expanded file name.
   */
  private String getExpandedFileName(final TrackData trackData, final String fileNameTemplate)
  {
    TrackCutterConfiguration.logger.entering
      ( TrackCutterConfiguration.class.getCanonicalName()
      , "getExpandedFileName(TrackData, String)"
      , new Object [] {trackData, fileNameTemplate}
      );
    String result = String.format
      ( this.getTemplateReplacer().replace(fileNameTemplate)
      , normalizeFileName(trackData.getMetaData(CueSheet.MetaDataField.TITLE))
      , normalizeFileName(trackData.getMetaData(CueSheet.MetaDataField.PERFORMER))
      , normalizeFileName(trackData.getMetaData(CueSheet.MetaDataField.ALBUMTITLE))
      , normalizeFileName(trackData.getMetaData(CueSheet.MetaDataField.YEAR))
      , normalizeFileName(trackData.getMetaData(CueSheet.MetaDataField.COMMENT))
      , normalizeFileName(trackData.getMetaData(CueSheet.MetaDataField.TRACKNUMBER))
      , normalizeFileName(trackData.getMetaData(CueSheet.MetaDataField.GENRE))
      );
    TrackCutterConfiguration.logger.exiting
      (TrackCutterConfiguration.class.getCanonicalName(), "getExpandedFileName(TrackData, String)", result);
    return result;
  }

  /**
   * Get the expanded post-processing command.
   * @param trackData TrackData to use in expanding the template.
   * @param processCommandTemplate Template for the post-processing command
   * @param cutFileName The name of the file of the track that was cut.
   * @param processFileName The file name for after the post-processing step.
   * @return The expanded post-processing command.
   */
  private String getExpandedProcessCommand
    ( final TrackData trackData
    , final String processCommandTemplate
    , final String cutFileName
    , final String processFileName
    )
  {
    TrackCutterConfiguration.logger.entering
      ( TrackCutterConfiguration.class.getCanonicalName()
      , "getExpandedFileName(TrackData, String, String, String)"
      , new Object [] {trackData, processCommandTemplate, cutFileName, processFileName}
      );
    String result = String.format
      ( this.getTemplateReplacer().replace(processCommandTemplate)
      , normalizeFileName(trackData.getMetaData(CueSheet.MetaDataField.TITLE))
      , normalizeFileName(trackData.getMetaData(CueSheet.MetaDataField.PERFORMER))
      , normalizeFileName(trackData.getMetaData(CueSheet.MetaDataField.ALBUMTITLE))
      , normalizeFileName(trackData.getMetaData(CueSheet.MetaDataField.YEAR))
      , normalizeFileName(trackData.getMetaData(CueSheet.MetaDataField.COMMENT))
      , normalizeFileName(trackData.getMetaData(CueSheet.MetaDataField.TRACKNUMBER))
      , normalizeFileName(trackData.getMetaData(CueSheet.MetaDataField.GENRE))
      , cutFileName
      , processFileName
      );
    TrackCutterConfiguration.logger.exiting
      ( TrackCutterConfiguration.class.getCanonicalName()
      , "getExpandedFileName(TrackData, String, String, String)"
      , result
      );
    return result;
  }
  
  /**
   * Get a File instance representing the file after cutting the track.
   * @param processAction The associated processing action.
   * @return A File instance representing the file after cutting the track.
   */
  public File getCutFile(final TrackCutterProcessingAction processAction)
  {
    TrackCutterConfiguration.logger.entering
      ( TrackCutterConfiguration.class.getCanonicalName()
      , "getCutFile(TrackCutterProcessingAction)"
      , processAction
      );
    TrackData trackData = processAction.getTrackData();
    
    String fileNameTemplate =
      processAction.getIsPregap()?this.getPregapCutFileNameTemplate():this.getCutFileNameTemplate();
    
    File result = getFileFromTemplate(trackData, fileNameTemplate);
    TrackCutterConfiguration.logger.exiting
      ( TrackCutterConfiguration.class.getCanonicalName()
      , "getCutFile(TrackCutterProcessingAction)"
      , result
      );
    return result;
  }
  
  /**
   * Get a File instance representing the file after post-processing.
   * @param processAction The associated processing action.
   * @return A File instance representing the file after post-processing.
   */
  public File getPostProcessFile(final TrackCutterProcessingAction processAction)
  {
    TrackCutterConfiguration.logger.entering
      ( TrackCutterConfiguration.class.getCanonicalName()
      , "getPostProcessFile(TrackCutterProcessingAction)"
      , processAction
      );
    TrackData trackData = processAction.getTrackData();
    
    String fileNameTemplate =
      processAction.getIsPregap()?this.getPregapPostProcessFileNameTemplate():this.getPostProcessFileNameTemplate();
    
    File result = getFileFromTemplate(trackData, fileNameTemplate);
    TrackCutterConfiguration.logger.exiting
      ( TrackCutterConfiguration.class.getCanonicalName()
      , "getPostProcessFile(TrackCutterProcessingAction)"
      , result
      );
    return result;
  }

  /**
   * Get the command to use for post-processing.
   * @param processAction The associated processing action.
   * @return The command to use for post-processing.
   */
  public String getPostProcessCommand(final TrackCutterProcessingAction processAction)
  {
    TrackCutterConfiguration.logger.entering
      ( TrackCutterConfiguration.class.getCanonicalName()
      , "getPostProcessCommand(TrackCutterProcessingAction)"
      , processAction
      );
    TrackData trackData = processAction.getTrackData();
    String commandTemplate =
      processAction.getIsPregap()?this.getPregapPostProcessCommandTemplate():this.getPostProcessCommandTemplate();
    String processCommand = getExpandedProcessCommand
      ( trackData
      , commandTemplate
      , processAction.getCutFile().getPath()
      , processAction.getPostProcessFile().getPath()
      );
    
    TrackCutterConfiguration.logger.exiting
      ( TrackCutterConfiguration.class.getCanonicalName()
      , "getPostProcessCommand(TrackCutterProcessingAction)"
      , processCommand
      );
    return processCommand;
  }
  
  /**
   * Get the replacer for the template values.
   * @return The replacer for the template values.
   */
  private StringReplacer getTemplateReplacer()
  {
    TrackCutterConfiguration.logger.entering(TrackCutterConfiguration.class.getCanonicalName(), "getTemplateReplacer()");
    TrackCutterConfiguration.logger.exiting
      ( TrackCutterConfiguration.class.getCanonicalName()
      , "getTemplateReplacer()"
      , TrackCutterConfiguration.templateReplacer
      );
    return TrackCutterConfiguration.templateReplacer;
  }

  /**
   * Get the parent directory for relative paths.
   * @return The parent directory for relative paths.
   */
  public File getParentDirectory()
  {
    TrackCutterConfiguration.logger.entering(TrackCutterConfiguration.class.getCanonicalName(), "getParentDirectory()");
    TrackCutterConfiguration.logger.exiting
      ( TrackCutterConfiguration.class.getCanonicalName()
      , "getParentDirectory()"
      , this.parentDirectory
      );
    return this.parentDirectory;
  }

  /**
   * Set the parent directory for relative paths.
   * @param parentDirectory The parent directory for relative paths.
   */
  public void setParentDirectory(final File parentDirectory)
  {
    TrackCutterConfiguration.logger.entering
      (TrackCutterConfiguration.class.getCanonicalName(), "setParentDirectory(File)", parentDirectory);
    this.parentDirectory = parentDirectory;
    TrackCutterConfiguration.logger.exiting
      (TrackCutterConfiguration.class.getCanonicalName(), "setParentDirectory(File)");
  }

  /**
   * Get the template for the file name of the tracks after cutting.
   * @return The template for the file name of the tracks after cutting.
   */
  public String getCutFileNameTemplate()
  {
    TrackCutterConfiguration.logger.entering
      (TrackCutterConfiguration.class.getCanonicalName(), "getCutFileNameTemplate()", parentDirectory);
    TrackCutterConfiguration.logger.exiting
      ( TrackCutterConfiguration.class.getCanonicalName()
      , "getCutFileNameTemplate()"
      , this.cutFileNameTemplate
      );
    return this.cutFileNameTemplate;
  }

  /**
   * Set the template for the file name of the tracks after cutting.
   * @param targetFileNameTemplate The template for the file name of the tracks after cutting.
   */
  public void setCutFileNameTemplate(final String targetFileNameTemplate)
  {
    TrackCutterConfiguration.logger.entering
      (TrackCutterConfiguration.class.getCanonicalName(), "setCutFileNameTemplate(String)", targetFileNameTemplate);
    this.cutFileNameTemplate = targetFileNameTemplate;
    TrackCutterConfiguration.logger.exiting
      (TrackCutterConfiguration.class.getCanonicalName(), "setCutFileNameTemplate(String)");
  }

  /**
   * Get the template for the file name of the post-processed tracks.
   * @return The template for the file name of the post-processed tracks.
   */
  public String getPostProcessFileNameTemplate()
  {
    TrackCutterConfiguration.logger.entering
      (TrackCutterConfiguration.class.getCanonicalName(), "getPostProcessFileNameTemplate()");
    TrackCutterConfiguration.logger.exiting
      ( TrackCutterConfiguration.class.getCanonicalName()
      , "getPostProcessFileNameTemplate()"
      , this.postProcessFileNameTemplate
      );
    return this.postProcessFileNameTemplate;
  }

  /**
   * Set the template for the file name of the post-processed tracks.
   * @param postProcessFileNameTemplate The template for the file name of the post-processed tracks.
   */
  public void setPostProcessFileNameTemplate(String postProcessFileNameTemplate)
  {
    TrackCutterConfiguration.logger.entering
      ( TrackCutterConfiguration.class.getCanonicalName()
      , "setPostProcessFileNameTemplate(String)"
      , postProcessFileNameTemplate
      );
    this.postProcessFileNameTemplate = postProcessFileNameTemplate;
    TrackCutterConfiguration.logger.exiting
      ( TrackCutterConfiguration.class.getCanonicalName()
      , "setPostProcessFileNameTemplate(String)"
      );
  }
  
  /**
   * Get the template for the command for post-processing tracks.
   * @return The template for the command for post-processing tracks.
   */
  public String getPostProcessCommandTemplate()
  {
    TrackCutterConfiguration.logger.entering
      (TrackCutterConfiguration.class.getCanonicalName(), "getPostProcessCommandTemplate()");
    TrackCutterConfiguration.logger.exiting
      ( TrackCutterConfiguration.class.getCanonicalName()
      , "getPostProcessCommandTemplate()"
      , this.postProcessCommandTemplate
      );
    return this.postProcessCommandTemplate;
  }

  /**
   * Set the template for the command for post-processing tracks.
   * @param postProcessCommandTemplate The template for the command for post-processing tracks.
   */
  public void setPostProcessCommandTemplate(final String postProcessCommandTemplate)
  {
    TrackCutterConfiguration.logger.entering
      ( TrackCutterConfiguration.class.getCanonicalName()
      , "setPostProcessCommandTemplate(String)"
      , postProcessCommandTemplate
      );
    this.postProcessCommandTemplate = postProcessCommandTemplate;
    TrackCutterConfiguration.logger.exiting
      (TrackCutterConfiguration.class.getCanonicalName(), "setPostProcessCommandTemplate(String)");
  }
  
  /**
   * Get the mode for pregap handling.
   * @return The mode for pregap handling.
   */
  public PregapHandling getPregapHandling()
  {
    TrackCutterConfiguration.logger.entering(TrackCutterConfiguration.class.getCanonicalName(), "getPregapHandling()");
    TrackCutterConfiguration.logger.exiting
      ( TrackCutterConfiguration.class.getCanonicalName()
      , "getPregapHandling()"
      , this.pregapHandling
      );
    return this.pregapHandling;
  }

  /**
   * Set the mode for pregap handling.
   * @param pregapHandling The mode for pregap handling.
   */
  public void setPregapHandling(final PregapHandling pregapHandling)
  {
    TrackCutterConfiguration.logger.entering
      (TrackCutterConfiguration.class.getCanonicalName(), "setPregapHandling(PregapHandling)", pregapHandling);
    this.pregapHandling = pregapHandling;
    TrackCutterConfiguration.logger.exiting
      (TrackCutterConfiguration.class.getCanonicalName(), "setPregapHandling(PregapHandling)");
  }

  /**
   * Get whether or not error output from post-processing should be redirected. 
   * @return the redirectErr Whether or not error output from post-processing should be redirected.
   */
  public boolean getRedirectErr()
  {
    TrackCutterConfiguration.logger.entering(TrackCutterConfiguration.class.getCanonicalName(), "getRedirectErr()");
    TrackCutterConfiguration.logger.exiting
      ( TrackCutterConfiguration.class.getCanonicalName()
      , "getRedirectErr()"
      , this.redirectErr
      );
    return this.redirectErr;
  }

  /**
   * Set whether or not error output from post-processing should be redirected. 
   * @param redirectErr Whether or not error output from post-processing should be redirected.
   */
  public void setRedirectErr(final boolean redirectErr)
  {
    TrackCutterConfiguration.logger.entering
      (TrackCutterConfiguration.class.getCanonicalName(), "setRedirectErr(boolean)", redirectErr);
    this.redirectErr = redirectErr;
    TrackCutterConfiguration.logger.exiting(TrackCutterConfiguration.class.getCanonicalName(), "setRedirectErr(boolean)");
  }

  /**
   * Get whether or not standard output from post-processing should be redirected. 
   * @return the redirectErr Whether or not standard output from post-processing should be redirected.
   */
  public boolean getRedirectStdOut()
  {
    TrackCutterConfiguration.logger.entering(TrackCutterConfiguration.class.getCanonicalName(), "getRedirectStdOut()");
    TrackCutterConfiguration.logger.exiting
      ( TrackCutterConfiguration.class.getCanonicalName()
      , "getRedirectStdOut()"
      , this.redirectStdOut
      );
    return this.redirectStdOut;
  }

  /**
   * Set whether or not standard output from post-processing should be redirected. 
   * @param redirectStdOut Whether or not standard output from post-processing should be redirected.
   */
  public void setRedirectStdOut(final boolean redirectStdOut)
  {
    TrackCutterConfiguration.logger.entering
      (TrackCutterConfiguration.class.getCanonicalName(), "setRedirectStdOut(boolean)", redirectStdOut);
    this.redirectStdOut = redirectStdOut;
    TrackCutterConfiguration.logger.exiting
      (TrackCutterConfiguration.class.getCanonicalName(), "setRedirectStdOut(boolean)");
  }

  /**
   * Get the audio type to convert to.
   * @return The audio type to convert to.
   */
  public AudioFileFormat.Type getTargetType()
  {
    TrackCutterConfiguration.logger.entering(TrackCutterConfiguration.class.getCanonicalName(), "getTargetType()");
    TrackCutterConfiguration.logger.exiting
      ( TrackCutterConfiguration.class.getCanonicalName()
      , "getTargetType()"
      , this.targetType
      );
    return this.targetType;
  }

  /**
   * Set the audio type to convert to.
   * @param targetType The audio type to convert to.
   */
  public void setTargetType(final AudioFileFormat.Type targetType)
  {
    TrackCutterConfiguration.logger.entering
      (TrackCutterConfiguration.class.getCanonicalName(), "setTargetType(AudioFileFormat.Type)", targetType);
    this.targetType = targetType;
    TrackCutterConfiguration.logger.exiting
      (TrackCutterConfiguration.class.getCanonicalName(), "setTargetType(AudioFileFormat.Type)");
  }

  /**
   * Get whether or not to do post-processing.
   * @return Whether or not to do post-processing.
   */
  public boolean getDoPostProcessing()
  {
    TrackCutterConfiguration.logger.entering(TrackCutterConfiguration.class.getCanonicalName(), "getDoPostProcessing()");
    TrackCutterConfiguration.logger.exiting
      ( TrackCutterConfiguration.class.getCanonicalName()
      , "getDoPostProcessing()"
      , this.doPostProcessing
      );
    return this.doPostProcessing;
  }

  /**
   * Set whether or not to do post-processing.
   * @param doPostProcessing Whether or not to do post-processing.
   */
  public void setDoPostProcessing(final boolean doPostProcessing)
  {
    TrackCutterConfiguration.logger.entering
      (TrackCutterConfiguration.class.getCanonicalName(), "setDoPostProcessing(boolean)", doPostProcessing);
    this.doPostProcessing = doPostProcessing;
    TrackCutterConfiguration.logger.exiting
      (TrackCutterConfiguration.class.getCanonicalName(), "setDoPostProcessing(boolean)");
  }

  /**
   * Get whether or not to redirect the cut track directly to post-processing.
   * @return Whether or not to redirect the cut track directly to post-processing.
   */
  public boolean getRedirectToPostprocessing()
  {
    TrackCutterConfiguration.logger.entering
      (TrackCutterConfiguration.class.getCanonicalName(), "getRedirectToPostprocessing()");
    TrackCutterConfiguration.logger.exiting
      ( TrackCutterConfiguration.class.getCanonicalName()
      , "getRedirectToPostprocessing()"
      , this.redirectToPostprocessing
      );
    return this.redirectToPostprocessing;
  }

  /**
   * Set whether or not to redirect the cut track directly to post-processing.
   * @param redirectToPostprocessing Whether or not to redirect the cut track directly to post-processing.
   */
  public void setRedirectToPostprocessing(final boolean redirectToPostprocessing)
  {
    TrackCutterConfiguration.logger.entering
      ( TrackCutterConfiguration.class.getCanonicalName()
      , "setRedirectToPostprocessing(boolean)"
      , redirectToPostprocessing
      );
    this.redirectToPostprocessing = redirectToPostprocessing;
    TrackCutterConfiguration.logger.exiting
      (TrackCutterConfiguration.class.getCanonicalName(), "setRedirectToPostprocessing(boolean)");
  }

  /**
   * Get the template for the file name of the pregap tracks after cutting.
   * @return The template for the file name of the tracks after cutting.
   */
  public String getPregapCutFileNameTemplate()
  {
    TrackCutterConfiguration.logger.entering
      (TrackCutterConfiguration.class.getCanonicalName(), "getPregapCutFileNameTemplate()");
    TrackCutterConfiguration.logger.exiting
      ( TrackCutterConfiguration.class.getCanonicalName()
      , "getPregapCutFileNameTemplate()"
      , this.pregapCutFileNameTemplate
      );
    return this.pregapCutFileNameTemplate;
  }

  /**
   * Set the template for the file name of the pregap tracks after cutting.
   * @param pregapTargetFileNameTemplate The template for the file name of the pregap tracks after cutting.
   */
  public void setPregapCutFileNameTemplate(final String pregapTargetFileNameTemplate)
  {
    TrackCutterConfiguration.logger.entering
      ( TrackCutterConfiguration.class.getCanonicalName()
      , "setPregapCutFileNameTemplate(String)"
      , pregapTargetFileNameTemplate
      );
    this.pregapCutFileNameTemplate = pregapTargetFileNameTemplate;
    TrackCutterConfiguration.logger.exiting
      (TrackCutterConfiguration.class.getCanonicalName(), "setPregapCutFileNameTemplate(String)");
  }

  /**
   * Get the template for the file name of the post-processed tracks.
   * @return The template for the file name of the post-processed tracks.
   */
  public String getPregapPostProcessFileNameTemplate()
  {
    TrackCutterConfiguration.logger.entering
      (TrackCutterConfiguration.class.getCanonicalName(), "getPregapPostProcessFileNameTemplate()");
    TrackCutterConfiguration.logger.exiting
      ( TrackCutterConfiguration.class.getCanonicalName()
      , "getPregapPostProcessFileNameTemplate()"
      , this.pregapPostProcessFileNameTemplate
      );
    return this.pregapPostProcessFileNameTemplate;
  }

  /**
   * Set the template for the file name of the post-processed tracks.
   * @param pregapPostProcessFileNameTemplate The template for the file name of the post-processed tracks.
   */
  public void setPregapPostProcessFileNameTemplate(final String pregapPostProcessFileNameTemplate)
  {
    TrackCutterConfiguration.logger.entering
      ( TrackCutterConfiguration.class.getCanonicalName()
      , "setPregapPostProcessFileNameTemplate(String)"
      , pregapPostProcessFileNameTemplate
      );
    this.pregapPostProcessFileNameTemplate = pregapPostProcessFileNameTemplate;
    TrackCutterConfiguration.logger.exiting
      (TrackCutterConfiguration.class.getCanonicalName(), "setPregapPostProcessFileNameTemplate(String)");
  }
  
  /**
   * Get the template for the command for post-processing tracks.
   * @return The template for the command for post-processing tracks.
   */
  public String getPregapPostProcessCommandTemplate()
  {
    TrackCutterConfiguration.logger.entering
      (TrackCutterConfiguration.class.getCanonicalName(), "getPregapPostProcessCommandTemplate()");
    TrackCutterConfiguration.logger.exiting
      ( TrackCutterConfiguration.class.getCanonicalName()
      , "getPregapPostProcessCommandTemplate()"
      , this.pregapPostProcessCommandTemplate
      );
    return this.pregapPostProcessCommandTemplate;
  }

  /**
   * Set the template for the command for post-processing tracks.
   * @param pregapPostProcessCommandTemplate The template for the command for post-processing tracks.
   */
  public void setPregapPostProcessCommandTemplate(final String pregapPostProcessCommandTemplate)
  {
    TrackCutterConfiguration.logger.entering
      ( TrackCutterConfiguration.class.getCanonicalName()
      , "setPregapPostProcessCommandTemplate(String)"
      , pregapPostProcessCommandTemplate
      );
    this.pregapPostProcessCommandTemplate = pregapPostProcessCommandTemplate;
    TrackCutterConfiguration.logger.exiting
      (TrackCutterConfiguration.class.getCanonicalName(), "setPregapPostProcessCommandTemplate(String)");
  }

  /**
   * Get the threshold on pregaps in frame length. Pregaps shorter than this will not be processed.
   * @return The threshold on pregaps in frame length.
   */
  public long getPregapFrameLengthThreshold()
  {
    TrackCutterConfiguration.logger.entering
      (TrackCutterConfiguration.class.getCanonicalName(), "getPregapFrameLengthThreshold()");
    TrackCutterConfiguration.logger.exiting
      ( TrackCutterConfiguration.class.getCanonicalName()
      , "getPregapFrameLengthThreshold()"
      , this.pregapFrameLengthThreshold
      );
    return this.pregapFrameLengthThreshold;
  }

  /**
   * Set the threshold on pregaps in frame length. Pregaps shorter than this will not be processed.
   * @param pregapFrameLengthThreshold The threshold on pregaps in frame length.
   */
  public void setPregapFrameLengthThreshold(final long pregapFrameLengthThreshold)
  {
    TrackCutterConfiguration.logger.entering
      ( TrackCutterConfiguration.class.getCanonicalName()
      , "setPregapFrameLengthThreshold(long)"
      , pregapFrameLengthThreshold
      );
    this.pregapFrameLengthThreshold = pregapFrameLengthThreshold;
    TrackCutterConfiguration.logger.exiting
      (TrackCutterConfiguration.class.getCanonicalName(), "setPregapFrameLengthThreshold(long)");
  }
}