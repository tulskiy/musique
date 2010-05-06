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
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.sound.sampled.AudioFileFormat;

import jwbroek.cuelib.Position;
import jwbroek.cuelib.tools.trackcutter.TrackCutterConfiguration.PregapHandling;
import jwbroek.io.FileSelector;
import jwbroek.util.LogUtil;
import jwbroek.util.SimpleOptionsParser;

/**
 * Command line interface for TrackCutter.
 * @author jwbroek
 */
public class TrackCutterCommand
{
  /**
   * The configuration for the TrackCutter. Will be modified based on the command line arguments.
   */
  private TrackCutterConfiguration configuration= new TrackCutterConfiguration();
  /**
   * Whether or not to do any processing. Can be set to false by options such as "-?" to indicate that
   * no actual processing should be done. 
   */
  private boolean doProcessing = true;
  /**
   * Recursion depth for file selection.
   */
  private long recursionDepth = 1;
  /**
   * Pattern that paths of files must match in order for the file to be selected..
   */
  private Pattern pathSelectionPattern = null;
  /**
   * Pattern that file names must match in order for the file to be selected.
   */
  private Pattern fileNameSelectionPattern = null;
  /**
   * Base directory for file selection. Default is the directory from which the java VM was started.
   */
  private File selectionBaseDirectory = new File(System.getProperty("user.dir"));
  /**
   * Whether or not to read a cue sheet from standard input.
   */
  private boolean readCueSheetFromStdIn = false;
  /**
   * The logger for this class.
   */
  private final static Logger logger = Logger.getLogger(TrackCutterCommand.class.getCanonicalName());

  /**
   * Create a new TrackCutterCommand instance. 
   */
  private TrackCutterCommand()
  {
    TrackCutterCommand.logger.entering(TrackCutterCommand.class.getCanonicalName(), "TrackCutterCommand()");
    TrackCutterCommand.logger.exiting(TrackCutterCommand.class.getCanonicalName(), "TrackCutterCommand()");
  }
  
  /**
   * Print a help message.
   */
  private static void printHelp()
  {
    TrackCutterCommand.logger.entering(TrackCutterCommand.class.getCanonicalName(), "printHelp()");
    System.out.println("Syntax: [options] [cuefiles]");
    System.out.println("cuefiles need not be specified if the -fp, -pp, or -i option is used.");
    System.out.println("Options:");
    System.out.println(" -pf regex           Process files such that their names match the specified regular");
    System.out.println("                     expression. This is in addition to files specified as parameter.");
    System.out.println("                     The regex is evaluated case-insensitively.");
    System.out.println(" -pp regex           Process files such that their canonical paths match the specified");
    System.out.println("                     regular expression. This is in addition to files specified as parameter.");
    System.out.println("                     The regex is evaluated case-insensitively.");
    System.out.println(" -pb directory       Base directory for file selection based on pattern.");
    System.out.println(" -r                  Process directories recursively. Only applies when the -pf or -pp");
    System.out.println("                     options are specified. Should not be used in combination with -rd.");
    System.out.println(" -rd depth           Process directories recursively up to the specified depth. Files in the");
    System.out.println("                     base directory are at depth 1. Only applies when the -pf or -pp options");
    System.out.println("                     are used. Should not be used in combination with -r.");
    System.out.println(" -b directory        Base directory for relative file references in cue sheet, and for relative");
    System.out.println("                     output location. If not specified, the directory of the cue sheet will be");
    System.out.println("                     used.");
    System.out.println(" -i                  Read cue sheet from standard input.");
    System.out.println(" -f file             Template for file name. Implies no redirect to post-processing.");
    System.out.println(" -t type             Audio type to convert to. Valid types are AIFC, AIFF, AU, SND, WAVE.");
    System.out.println("                     Not all conversions may be supported.");
    System.out.println(" -p file command     Template for post-processing file name and command.");
    System.out.println(" -g type [templates] Pregap handling. Choose from \"prepend\", \"discard\", \"separate\"");
    System.out.println("                     If \"separate\" is chosen, you must also specify templates for file");
    System.out.println("                     name, command, and name for intermediate file. The latter options will");
    System.out.println("                     not be used if redirect is enabled for post-processing. It must still be");
    System.out.println("                     specified.");
    System.out.println(" -pt length          Threshold for pregap processing. Pregaps with length shorter than this");
    System.out.println("                     will not be processed. Length as per the position field in cue sheets.");
    System.out.println(" -s                  Redirect audio to post-processing step.");
    System.out.println(" -ro                 Redirect output of post-processing step to log file.");
    System.out.println(" -re                 Redirect error output of post-processing step to log file.");
    System.out.println(" -l level            Override jdk 1.4 logging settings. The following levels are supported:");
    System.out.println("                     \"none\" (no cuelib logging), \"specific\" (only TrackCutter logging),");
    System.out.println("                     \"all\" (all cuelib logging). When logging is enabled, TrackCutter will");
    System.out.println("                     also try to ensure that logging is directed to the console. This setting");
    System.out.println("                     does not influence logging outside of cuelib. For this, use the standard");
    System.out.println("                     jdk 1.4 logging settings.");
    System.out.println(" -? | --help         Displays this help message and exits.");
    System.out.println("Templates:");
    System.out.println(" <title>             Title of the track.");
    System.out.println(" <artist>            Artist of the track, or artist of the album, if unknown.");
    System.out.println(" <album>             Title of the album");
    System.out.println(" <year>              Year of the track, or year of the album, if unknown.");
    System.out.println(" <comment>           Comment of the album.");
    System.out.println(" <track>             Track number");
    System.out.println(" <genre>             Genre of the album.");
    System.out.println(" <cutFile>           Name of the file after cutting. Can only be used in the post-");
    System.out.println("                     processing command template.");
    System.out.println(" <postProcessFile>   Name of the file after post-processing. Can only be used in");
    System.out.println("                     the post-processing command template.");
    System.out.println("Examples:");
    System.out.println(" Cut the tracks in a cue sheet:");
    System.out.println("  \"c:\\tmp\\Skunk Anansie - Stoosh.cue\"");
    System.out.println(" Cut the tracks in a cue sheet and prepend the pregaps:");
    System.out.println("  -p prepend \"c:\\tmp\\Skunk Anansie - Stoosh.cue\"");
    System.out.println(" Cut the tracks in a cue sheet and prepend pregaps longer than 3 seconds:");
    System.out.println("  -p prepend -pt 00:02:00 \"c:\\tmp\\Skunk Anansie - Stoosh.cue\"");
    System.out.println(" Cut the tracks in a cue sheet and give them names based on the data in the sheet:");
    System.out.println("  -f \"<artist>\\<album>\\<track>_<title>.wav\" \"c:\\tmp\\Skunk Anansie - Stoosh.cue\"");
    System.out.println(" Cut the tracks with separate pregaps, convert to WAV format, and redirect to lame while");
    System.out.println(" setting the appropriate ID3 tags, and creating log files:");
    System.out.println("  -f \"waves\\<artist>\\<album>\\<track>_<title>.wav\"");
    System.out.println("  -p \"mp3\\<artist>\\<album>\\<track>_<title>.mp3\"");
    System.out.println("  \"C:\\lame\\lame.exe --vbr-new -V 0 -t --tt \\\"<title>\\\" --ta \\\"<artist>\\\" --tl \\\"<album>\\\"");
    System.out.println("  --ty \\\"<year>\\\" --tc \\\"<comment>\\\" --tn \\\"<track>\\\" --tg \\\"<genre>\\\" - ");
    System.out.println("  \\\"<postProcessFile>\\\"\"");
    System.out.println("  -g separate \"mp3\\<artist>\\<album>\\<track>_0_<title>.mp3\"");
    System.out.println("  \"C:\\lame\\lame.exe --vbr-new -V 0 -t --tt \\\"00 Pregap of <title>\\\" --ta \\\"<artist>\\\"");
    System.out.println("  --tl \\\"<album>\\\" --ty \\\"<year>\\\" --tc \\\"Pregap of <title>\\\" --tn \\\"<track>\\\" --tg");
    System.out.println("  \\\"<genre>\\\" - \\\"<postProcessFile>\\\"\"");
    System.out.println("  -s -ro -re -t WAVE \"c:\\tmp\\Skunk Anansie - Stoosh.cue\"");
    System.out.println("Notes:");
    System.out.println("No guarantees are made as to the order in which files are processed. Some effort is made");
    System.out.println("to provent files from being processed more than once per run.");
    System.out.println("Conflicting options may result in unpredictable behaviour.");
    TrackCutterCommand.logger.exiting(TrackCutterCommand.class.getCanonicalName(), "printHelp()");
  }
  
  /**
   * Get a configurated parser for the command line arguments.
   * @return A configurated parser for the command line arguments.
   */
  private SimpleOptionsParser getArgumentsParser()
  {
    TrackCutterCommand.logger.entering(TrackCutterCommand.class.getCanonicalName(), "getArgumentsParser()");
    SimpleOptionsParser argumentsParser = new SimpleOptionsParser();
    argumentsParser.registerOption
      ( new SimpleOptionsParser.OptionHandler()
        {
          public int handleOption(String [] options, int offset)
          {
            // Set selection pattern for file name.
            TrackCutterCommand.this.setFileNameSelectionPattern
              (Pattern.compile(options[offset+1], Pattern.CASE_INSENSITIVE));
            return offset+2;
          }
        }
      , "-pf"
      );
    argumentsParser.registerOption
      ( new SimpleOptionsParser.OptionHandler()
        {
          public int handleOption(String [] options, int offset)
          {
            // Set selection pattern for file path.
            TrackCutterCommand.this.setPathSelectionPattern
              (Pattern.compile(options[offset+1], Pattern.CASE_INSENSITIVE));
            return offset+2;
          }
        }
      , "-pp"
      );
    argumentsParser.registerOption
      ( new SimpleOptionsParser.OptionHandler()
        {
          public int handleOption(String [] options, int offset)
          {
            // Base directory for file selection.
            TrackCutterCommand.this.setSelectionBaseDirectory(new File(options[offset+1]));
            return offset+2;
          }
        }
      , "-pb"
      );
    argumentsParser.registerOption
      ( new SimpleOptionsParser.OptionHandler()
        {
          public int handleOption(String [] options, int offset)
          {
            // Recurse into subdirectories.
            TrackCutterCommand.this.setRecursionDepth(Long.MAX_VALUE);
            return offset+1;
          }
        }
      , "-r"
      );
    argumentsParser.registerOption
      ( new SimpleOptionsParser.OptionHandler()
        {
          public int handleOption(String [] options, int offset)
          {
            // Recurse into subdirectories.
            TrackCutterCommand.this.setRecursionDepth(Long.parseLong(options[offset+1]));
            return offset+2;
          }
        }
      , "-rd"
      );
    argumentsParser.registerOption
      ( new SimpleOptionsParser.OptionHandler()
        {
          public int handleOption(String [] options, int offset)
          {
            // Base directory for cue sheet processing.
            TrackCutterCommand.this.getConfiguration().setParentDirectory(new File(options[offset+1]));
            return offset+2;
          }
        }
      , "-b"
      );
    argumentsParser.registerOption
      ( new SimpleOptionsParser.OptionHandler()
        {
          public int handleOption(String [] options, int offset)
          {
            // Read a cue sheet from standard input.
            TrackCutterCommand.this.setReadCueSheetFromStdIn(true);
            return offset+1;
          }
        }
      , "-i"
      );
    argumentsParser.registerOption
      ( new SimpleOptionsParser.OptionHandler()
        {
          public int handleOption(String [] options, int offset)
          {
            // Create a target file. This implies no streaming to the postprocessor.
            TrackCutterCommand.this.getConfiguration().setRedirectToPostprocessing(false);
            TrackCutterCommand.this.getConfiguration().setCutFileNameTemplate(options[offset+1]);
            return offset+2;
          }
        }
      , "-f"
      );
    argumentsParser.registerOption
      ( new SimpleOptionsParser.OptionHandler()
        {
          public int handleOption(String [] options, int offset)
          {
            String type = options[offset+1];
            AudioFileFormat.Type audioType = null;
            if ("AIFC".equalsIgnoreCase(type))
            {
              audioType = AudioFileFormat.Type.AIFC;
            }
            else if ("AIFF".equalsIgnoreCase(type))
            {
              audioType = AudioFileFormat.Type.AIFF;
            }
            else if ("AU".equalsIgnoreCase(type))
            {
              audioType = AudioFileFormat.Type.AU;
            }
            else if ("SND".equalsIgnoreCase(type))
            {
              audioType = AudioFileFormat.Type.SND;
            }
            else if ("WAVE".equalsIgnoreCase(type))
            {
              audioType = AudioFileFormat.Type.WAVE;
            }
            else
            {
              throw new IllegalArgumentException("Unsupported audio type: " + type);
            }
            // Set target audio type.
            TrackCutterCommand.this.getConfiguration().setTargetType(audioType);
            return offset+2;
          }
        }
      , "-t"
      );
    argumentsParser.registerOption
      ( new SimpleOptionsParser.OptionHandler()
        {
          public int handleOption(String [] options, int offset)
          {
            // Create a postprocessing command. This implies that we do postprocessing.
            TrackCutterCommand.this.getConfiguration().setDoPostProcessing(true);
            TrackCutterCommand.this.getConfiguration().setPostProcessFileNameTemplate(options[offset+1]);
            TrackCutterCommand.this.getConfiguration().setPostProcessCommandTemplate(options[offset+2]);
            return offset+3;
          }
        }
      , "-p"
      );
    argumentsParser.registerOption
      ( new SimpleOptionsParser.OptionHandler()
        {
          public int handleOption(String [] options, int offset)
          {
            // Pregap handling
            if ("prepend".equalsIgnoreCase(options[offset + 1]))
            {
              TrackCutterCommand.this.getConfiguration().setPregapHandling(PregapHandling.PREPEND);
              return offset+2;
            }
            else if ("discard".equalsIgnoreCase(options[offset + 1]))
            {
              TrackCutterCommand.this.getConfiguration().setPregapHandling(PregapHandling.DISCARD);
              return offset+2;
            }
            else if ("separate".equalsIgnoreCase(options[offset + 1]))
            {
              TrackCutterCommand.this.getConfiguration().setPregapHandling(PregapHandling.SEPARATE);
              TrackCutterCommand.this.getConfiguration().setPregapPostProcessFileNameTemplate(options[offset + 2]);
              TrackCutterCommand.this.getConfiguration().setPregapPostProcessCommandTemplate(options[offset + 3]);
              TrackCutterCommand.this.getConfiguration().setPregapCutFileNameTemplate(options[offset + 4]);
              return offset+5;
            }
            else
            {
              throw new IllegalArgumentException("Invalid type for pregap handling: " + options[1]);
            }
          }
        }
      , "-g"
      );
    argumentsParser.registerOption
      ( new SimpleOptionsParser.OptionHandler()
        {
          public int handleOption(String [] options, int offset)
          {
            // Stream to postprocessor
            TrackCutterCommand.this.getConfiguration().setRedirectToPostprocessing(true);
            return offset+1;
          }
        }
      , "-s"
      );
    argumentsParser.registerOption
      ( new SimpleOptionsParser.OptionHandler()
        {
          public int handleOption(String [] options, int offset)
          {
            // Set frame length threshold for pregap handling.
            Scanner scanner = new Scanner(options[offset+1]).useDelimiter(":");
            Position thresholdPosition = new Position(scanner.nextInt(), scanner.nextInt(), scanner.nextInt());
            scanner.close();
            TrackCutterCommand.this.getConfiguration().setPregapFrameLengthThreshold(thresholdPosition.getTotalFrames());
            return offset+2;
          }
        }
      , "-pt"
      );
    argumentsParser.registerOption
      ( new SimpleOptionsParser.OptionHandler()
        {
          public int handleOption(String [] options, int offset)
          {
            // Redirect standard out.
            TrackCutterCommand.this.getConfiguration().setRedirectStdOut(true);
            return offset+1;
          }
        }
      , "-ro"
      );
    argumentsParser.registerOption
      ( new SimpleOptionsParser.OptionHandler()
        {
          public int handleOption(String [] options, int offset)
          {
            // Redirect err.
            TrackCutterCommand.this.getConfiguration().setRedirectErr(true);
            return offset+1;
          }
        }
      , "-re"
      );
    argumentsParser.registerOption
      ( new SimpleOptionsParser.OptionHandler()
        {
          public int handleOption(String [] options, int offset)
          {
            // Override logging.
            String level = options[offset+1];
            if ("none".equals(level))
            {
              // Suppress logging of the jwbroek.cuelib tree.
              Logger.getLogger("jwbroek.cuelib").setLevel(Level.OFF);
            }
            else if ("specific".equals(level))
            {
              // Suppress logging of the jwbroek.cuelib tree.
              Logger.getLogger("jwbroek.cuelib").setLevel(Level.OFF);
              // Do allow INFO logging of the trackcutter log.
              Logger trackCutterLogger = Logger.getLogger(TrackCutter.class.getCanonicalName());
              if (! trackCutterLogger.isLoggable(Level.INFO))
              {
                trackCutterLogger.setLevel(Level.INFO);
              }
              // If there is no ConsoleLogger configured, then add one.
              if  ( ! LogUtil.hasHandlerActive  ( trackCutterLogger
                                                , Level.INFO
                                                , ConsoleHandler.class
                                                )
                  )
              {
                trackCutterLogger.addHandler(new ConsoleHandler());
              }
            }
            else if ("all".equals(level))
            {
              // Enable INFO logging of the jwbroek.cuelib tree, and specifically the
              // TrackCutter log.
              Logger cuelibLoggger = Logger.getLogger("jwbroek.cuelib");
              if (! cuelibLoggger.isLoggable(Level.INFO))
              {
                cuelibLoggger.setLevel(Level.INFO);
              }
              Logger trackCutterLogger = Logger.getLogger(TrackCutter.class.getCanonicalName());
              if (! trackCutterLogger.isLoggable(Level.INFO))
              {
                trackCutterLogger.setLevel(Level.INFO);
              }
              // If there is no ConsoleLogger configured, then add one.
              if  ( ! LogUtil.hasHandlerActive  ( Logger.getLogger("jwbroek.cuelib")
                                                , Level.INFO
                                                , ConsoleHandler.class
                                                )
                  )
              {
                Logger.getLogger("jwbroek.cuelib").addHandler(new ConsoleHandler());
              }
              if  ( ! LogUtil.hasHandlerActive  ( trackCutterLogger
                                                , Level.INFO
                                                , ConsoleHandler.class
                                                )
                  )
              {
                trackCutterLogger.addHandler(new ConsoleHandler());
              }
            }
            else
            {
              throw new IllegalArgumentException("Invalid level setting for -l option: '" + level + "'");
            }
            return offset+2;
          }
        }
      , "-l"
      );
    argumentsParser.registerOption
      ( new SimpleOptionsParser.OptionHandler()
        {
          public int handleOption(String [] options, int offset)
          {
            // Display help message.
            TrackCutterCommand.printHelp();
            // Don't do any processing.
            TrackCutterCommand.this.setDoProcessing(false);
            return offset+1;
          }
        }
      , "-?", "--help"
      );
    
    TrackCutterCommand.logger.exiting(TrackCutterCommand.class.getCanonicalName(), "getArgumentsParser()", argumentsParser);
    return argumentsParser;
  }
  
  /**
   * Process based on the provided command line arguments.
   * @param args The command line arguments.
   */
  public void performProcessing(final String [] args)
  {
    TrackCutterCommand.logger.entering(TrackCutterCommand.class.getCanonicalName(), "performProcessing(String[])", args);
    TrackCutter cutter = new TrackCutter(this.getConfiguration());
    SimpleOptionsParser argumentsParser = getArgumentsParser();
    
    int firstFileIndex = argumentsParser.parseOptions(args);
    
    if  (  firstFileIndex == -1                           // Something went wrong with parsing the options.
        || (  firstFileIndex == args.length               // All parameters were parsed.
           && this.getFileNameSelectionPattern() == null  // No files are to be selected based on file name.
           && this.getPathSelectionPattern() == null      // No files are to be selected based on path.
           && ! this.getReadCueSheetFromStdIn()           // No cue sheet will be read from standard input.
           )
        )
    {
      // Something went wrong, or no files or standard input were specified.
      System.err.println("A problem occurred when parsing the command line arguments. Please check for syntax.");
      printHelp();
      return;
    }
    
    if (this.getDoProcessing())
    {
      Set<File> fileSet = new HashSet<File>();
      
      // Add the explicitly specified files to the set.
      for (int fileIndex = firstFileIndex; fileIndex < args.length; fileIndex++)
      {
        fileSet.add(new File(args[fileIndex]));
      }
      
      // Add the files based on the specified selection criteria, if applicable.
      List<FileFilter> fileFilters = new ArrayList<FileFilter>();
      // Only select files.
      fileFilters.add(FileSelector.getFilesFilter());
      // Add filter for selection based on path, if applicable.
      if (this.getPathSelectionPattern()!=null)
      {
        fileFilters.add(FileSelector.getPathPatternFilter(this.getPathSelectionPattern()));
      }
      // Add filter for selection based on file name, if applicable.
      if (this.getFileNameSelectionPattern()!=null)
      {
        fileFilters.add(FileSelector.getFileNamePatternFilter(this.getFileNameSelectionPattern()));
      }
      // Only do a select if we have a filter other than the filter that accepts only files.
      if (fileFilters.size() > 1)
      {
        List<File> fileList = new ArrayList<File>();
        FileSelector.selectFiles
          ( this.getSelectionBaseDirectory()
          , FileSelector.getCombinedFileFilter(fileFilters)
          , fileList
          , this.getRecursionDepth()
          , false
          , true
          );
        fileSet.addAll(fileList);
      }
      
      // Process all specified files.
      for (File cueFile : fileSet)
      {
        try
        {
          cutter.cutTracksInCueSheet(cueFile);
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
      }
      
      // Process cue sheet from standard input, if specified.
      if (this.readCueSheetFromStdIn)
      {
        try
        {
          cutter.cutTracksInCueSheet(System.in);
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
      }
    }
    
    // Set doProcessing to true, as someone may want to reuse this instance.
    this.setDoProcessing(true);
    TrackCutterCommand.logger.exiting(TrackCutterCommand.class.getCanonicalName(), "performProcessing(String[])");
  }

  /**
   * Entry-point.
   * @param args Command line arguments.
   */
  public static void main(final String[] args)
  {
    TrackCutterCommand.logger.entering(TrackCutterCommand.class.getCanonicalName(), "main(String[])", args);
    new TrackCutterCommand().performProcessing(args);
    TrackCutterCommand.logger.exiting(TrackCutterCommand.class.getCanonicalName(), "main(String[])");
  }

  /**
   * Get the configuration for the TrackCutter.
   * @return The configuration for the TrackCutter.
   */
  private TrackCutterConfiguration getConfiguration()
  {
    TrackCutterCommand.logger.entering(TrackCutterCommand.class.getCanonicalName(), "getConfiguration()");
    TrackCutterCommand.logger.exiting
      (TrackCutterCommand.class.getCanonicalName(), "getConfiguration()", this.configuration);
    return this.configuration;
  }
  
  /**
   * Get whether or not to do any processing.
   * @return Whether or not to do any processing.
   */
  private boolean getDoProcessing()
  {
    TrackCutterCommand.logger.entering(TrackCutterCommand.class.getCanonicalName(), "getDoProcessing()");
    TrackCutterCommand.logger.exiting
      (TrackCutterCommand.class.getCanonicalName(), "getDoProcessing()", this.doProcessing);
    return this.doProcessing;
  }

  /**
   * Set whether or not to do any processing.
   * @param doProcessing Whether or not to do any processing.
   */
  private void setDoProcessing(final boolean doProcessing)
  {
    TrackCutterCommand.logger.entering
      (TrackCutterCommand.class.getCanonicalName(), "setDoProcessing(boolean)", doProcessing);
    this.doProcessing = doProcessing;
    TrackCutterCommand.logger.exiting
      (TrackCutterCommand.class.getCanonicalName(), "setDoProcessing(boolean)", this.doProcessing);
  }

  /**
   * Get the pattern that file names must pass in order for the file to be selected. If null,
   * then there is no selection based on this criterium.
   * @return The pattern that file names must pass in order for the file to be selected.
   */
  private Pattern getFileNameSelectionPattern()
  {
    TrackCutterCommand.logger.entering(TrackCutterCommand.class.getCanonicalName(), "getFileNameSelectionPattern()");
    TrackCutterCommand.logger.exiting
      ( TrackCutterCommand.class.getCanonicalName()
      , "getFileNameSelectionPattern()"
      , this.fileNameSelectionPattern
      );
    return this.fileNameSelectionPattern;
  }

  /**
   * Set the pattern that file names must pass in order for the file to be selected. If null,
   * then there is no selection based on this criterium.
   * @param fileNameSelectionPattern The pattern that file names must pass in order for the
   * file to be selected.
   */
  private void setFileNameSelectionPattern(final Pattern fileNameSelectionPattern)
  {
    TrackCutterCommand.logger.entering
      ( TrackCutterCommand.class.getCanonicalName()
      , "setFileNameSelectionPattern(Pattern)"
      , fileNameSelectionPattern
      );
    this.fileNameSelectionPattern = fileNameSelectionPattern;
    TrackCutterCommand.logger.exiting(TrackCutterCommand.class.getCanonicalName(), "setFileNameSelectionPattern(Pattern)");
  }

  /**
   * Get the pattern that paths must pass in order for the file to be selected. If null,
   * then there is no selection based on this criterium.
   * @return The pattern that paths must pass in order for the file to be selected.
   */
  private Pattern getPathSelectionPattern()
  {
    TrackCutterCommand.logger.entering(TrackCutterCommand.class.getCanonicalName(), "getPathSelectionPattern()");
    TrackCutterCommand.logger.exiting
      (TrackCutterCommand.class.getCanonicalName(), "getPathSelectionPattern()", this.pathSelectionPattern);
    return this.pathSelectionPattern;
  }

  /**
   * Set the pattern that paths must pass in order for the file to be selected. If null,
   * then there is no selection based on this criterium.
   * @param pathSelectionPattern The pattern that paths must pass in order for the
   * file to be selected.
   */
  private void setPathSelectionPattern(final Pattern pathSelectionPattern)
  {
    TrackCutterCommand.logger.entering
      (TrackCutterCommand.class.getCanonicalName(), "getPathSelectionPattern(Pattern)", pathSelectionPattern);
    this.pathSelectionPattern = pathSelectionPattern;
    TrackCutterCommand.logger.exiting(TrackCutterCommand.class.getCanonicalName(), "getPathSelectionPattern(Pattern)");
  }

  /**
   * Get the recursion depth for file selection.
   * @return The recursion depth for file selection.
   */
  private long getRecursionDepth()
  {
    TrackCutterCommand.logger.entering(TrackCutterCommand.class.getCanonicalName(), "getRecursionDepth()");
    TrackCutterCommand.logger.exiting(TrackCutterCommand.class.getCanonicalName(), "getRecursionDepth()", this.recursionDepth);
    return this.recursionDepth;
  }

  /**
   * Set the recursion depth for file selection.
   * @param recurseIntoSubdirectories The recursion depth for file selection.
   * @see FileSelector#selectFiles(File, FileFilter, List, long, boolean, boolean)
   */
  private void setRecursionDepth(final long recursionDepth)
  {
    TrackCutterCommand.logger.entering
      (TrackCutterCommand.class.getCanonicalName(), "setRecursionDepth(long)", recursionDepth);
    this.recursionDepth = recursionDepth;
    TrackCutterCommand.logger.exiting(TrackCutterCommand.class.getCanonicalName(), "setRecursionDepth(long)");
  }

  /**
   * Get the base directory for file selection.
   * @return The base directory for file selection.
   * @see FileSelector#selectFiles(File, FileFilter, List, long, boolean, boolean)
   */
  private File getSelectionBaseDirectory()
  {
    TrackCutterCommand.logger.entering(TrackCutterCommand.class.getCanonicalName(), "getSelectionBaseDirectory()");
    TrackCutterCommand.logger.exiting
      ( TrackCutterCommand.class.getCanonicalName()
      , "getSelectionBaseDirectory()"
      , this.selectionBaseDirectory
      );
    return this.selectionBaseDirectory;
  }

  /**
   * Set the base directory for file selection.
   * @param selectionBaseDirectory The base directory for file selection.
   */
  private void setSelectionBaseDirectory(final File selectionBaseDirectory)
  {
    TrackCutterCommand.logger.entering
      ( TrackCutterCommand.class.getCanonicalName()
      , "setSelectionBaseDirectory(File)"
      , selectionBaseDirectory
      );
    this.selectionBaseDirectory = selectionBaseDirectory;
    TrackCutterCommand.logger.exiting
      ( TrackCutterCommand.class.getCanonicalName()
      , "setSelectionBaseDirectory(File)"
      );
  }
  
  /**
   * Get whether or not to read a cue sheet from standard input.
   * @return Whether or not to read a cue sheet from standard input.
   */
  private boolean getReadCueSheetFromStdIn()
  {
    TrackCutterCommand.logger.entering(TrackCutterCommand.class.getCanonicalName(), "getReadCueSheetFromStdIn()");
    TrackCutterCommand.logger.exiting
      (TrackCutterCommand.class.getCanonicalName(), "getReadCueSheetFromStdIn()", this.readCueSheetFromStdIn);
    return this.readCueSheetFromStdIn;
  }

  /**
   * Set whether or not to read a cue sheet from standard input.
   * @param readCueSheetFromStdIn Whether or not to read a cue sheet from standard input.
   */
  private void setReadCueSheetFromStdIn(final boolean readCueSheetFromStdIn)
  {
    TrackCutterCommand.logger.entering(TrackCutterCommand.class.getCanonicalName(), "setReadCueSheetFromStdIn(boolean)");
    this.readCueSheetFromStdIn = readCueSheetFromStdIn;
    TrackCutterCommand.logger.exiting
      ( TrackCutterCommand.class.getCanonicalName()
      , "setReadCueSheetFromStdIn(boolean)"
      , this.readCueSheetFromStdIn
      );
  }
}
