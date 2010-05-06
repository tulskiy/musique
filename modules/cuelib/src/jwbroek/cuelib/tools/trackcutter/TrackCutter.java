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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import jwbroek.cuelib.CueParser;
import jwbroek.cuelib.CueSheet;
import jwbroek.cuelib.FileData;
import jwbroek.cuelib.Position;
import jwbroek.cuelib.TrackData;
import jwbroek.io.StreamPiper;
import jwbroek.util.LogUtil;

/**
 * <p>Class that can cut up files into tracks, based on the information provided by a cue sheet.</p>
 * <p>It can do some audio type conversions, file naming based on information in the cue sheet, and
 * offers the option of having the tracks post-processed by a another application based on information
 * in the cue sheet.</p>
 * @author jwbroek
 */
public class TrackCutter
{
  /**
   * The logger for this class.
   */
  private final static Logger logger = Logger.getLogger(TrackCutter.class.getCanonicalName());
  
  /**
   * Configuation for the TrackCutter.
   */
  private TrackCutterConfiguration configuration;
  
  /**
   * Create a new TrackCutter instance, based on the configuration provided.
   * @param configuration
   */
  public TrackCutter(final TrackCutterConfiguration configuration)
  {
    TrackCutter.logger.entering
      (TrackCutter.class.getCanonicalName(), "TrackCutter(TrackCutterConfiguration)", configuration);
    this.configuration = configuration;
    TrackCutter.logger.exiting(TrackCutter.class.getCanonicalName(), "TrackCutter(TrackCutterConfiguration)");
  }
  
  /**
   * Cut the the files specified in the cue sheet into tracks.
   * @param cueFile
   * @throws IOException
   */
  public void cutTracksInCueSheet(final File cueFile) throws IOException
  {
    TrackCutter.logger.entering(TrackCutter.class.getCanonicalName(), "cutTracksInCueSheet(File)", cueFile);
    TrackCutter.logger.info("Cutting tracks in cue sheet from file '" + cueFile.toString() + "'.");
    
    CueSheet cueSheet = null;
    
    // If no parent directory specified, then set the parent directory of the cue file.
    if (getConfiguration().getParentDirectory()==null)
    {
      getConfiguration().setParentDirectory(cueFile.getParentFile());
      TrackCutter.logger.fine("Have set base directory to directory of File  '" + cueFile.toString() + "'.");
    }
    
    try
    {
      TrackCutter.logger.fine("Parsing cue sheet.");
      cueSheet = CueParser.parse(cueFile);
    }
    catch (IOException e)
    {
      TrackCutter.logger.severe("Was unable to parse the cue sheet in file '" + cueFile.toString() + "'.");
      LogUtil.logStacktrace(TrackCutter.logger, Level.SEVERE, e);
      IOException resultException = new IOException("Problem parsing cue file.");
      resultException.initCause(e);
      TrackCutter.logger.throwing(TrackCutter.class.getCanonicalName(), "cutTracksInCueSheet(File)", resultException);
      throw resultException;
    }
    
    cutTracksInCueSheet(cueSheet);
    TrackCutter.logger.exiting(TrackCutter.class.getCanonicalName(), "cutTracksInCueSheet(File)");
  }
  
  /**
   * Cut the the files specified in the cue sheet that will be read from the InputStream into tracks.
   * @param inputStream
   * @throws IOException
   */
  public void cutTracksInCueSheet(final InputStream inputStream) throws IOException
  {
    TrackCutter.logger.entering(TrackCutter.class.getCanonicalName(), "cutTracksInCueSheet(InputStream)", inputStream);
    TrackCutter.logger.info("Cutting tracks in cue sheet from InputStream.");
    
    CueSheet cueSheet = null;
    
    try
    {
      TrackCutter.logger.fine("Parsing cue sheet.");
      cueSheet = CueParser.parse(inputStream);
    }
    catch (IOException e)
    {
      TrackCutter.logger.severe("Was unable to parse the cue sheet from InputStream.");
      LogUtil.logStacktrace(TrackCutter.logger, Level.SEVERE, e);
      IOException resultException = new IOException("Problem parsing cue file.");
      resultException.initCause(e);
      TrackCutter.logger.throwing(TrackCutter.class.getCanonicalName(), "cutTracksInCueSheet(File)", resultException);
      throw resultException;
    }
    
    cutTracksInCueSheet(cueSheet);
    TrackCutter.logger.exiting(TrackCutter.class.getCanonicalName(), "cutTracksInCueSheet(InputStream)");
  }
  
  /**
   * Cut the the files specified in the cue sheet into tracks.
   * @param cueSheet
   * @throws IOException
   */
  public void cutTracksInCueSheet(final CueSheet cueSheet) throws IOException
  {
    TrackCutter.logger.entering(TrackCutter.class.getCanonicalName(), "cutTracksInCueSheet(CueSheet)", cueSheet);
    TrackCutter.logger.info("Cutting tracks in cue sheet.");
    
    // We can process each file in the cue sheet independently.
    for (FileData fileData : cueSheet.getFileData())
    {
      try
      {
        cutTracksInFileData(fileData);
      }
      catch (UnsupportedAudioFileException e)
      {
        logCaughtException(e);
      }
      catch (IOException e)
      {
        logCaughtException(e);
      }
    }
    TrackCutter.logger.info("Done cutting tracks in cue sheet.");
    TrackCutter.logger.exiting(TrackCutter.class.getCanonicalName(), "cutTracksInCueSheet(CueSheet)");
  }
  
  /**
   * Cut the the files specified in the FileData into tracks.
   * @param fileData
   * @throws IOException
   * @throws UnsupportedAudioFileException
   */
  private void cutTracksInFileData(final FileData fileData)
    throws IOException, UnsupportedAudioFileException
  {
    TrackCutter.logger.entering(TrackCutter.class.getCanonicalName(), "cutTracksInCueSheet(FileData)", fileData);
    TrackCutter.logger.info("Cutting tracks from file: '" + fileData.getFile() + "'.");
    AudioInputStream audioInputStream = null;
    
    try
    {
      // Determine the complete path to the audio file.
      TrackCutter.logger.fine("Determining complete path to audio file.");
      File audioFile = getConfiguration().getAudioFile(fileData);
      
      // Open the audio file.
      // Sadly, we can't do much with the file type information from the cue sheet, as javax.sound.sampled
      // needs more information before it can process a specific type of sound file. Best then to let it
      // determine all aspects of the audio type by itself.
      TrackCutter.logger.fine("Opening audio stream.");
      audioInputStream = AudioSystem.getAudioInputStream(audioFile);
      
      // Current position in terms of the frames as per audioInputStream.getFrameLength().
      // Note that these frames need not be equal to cue sheet frames.
      long currentAudioFramePos = 0;
      
      // Process tracks.
      for (TrackCutterProcessingAction processAction : getProcessActionList(fileData))
      {
        currentAudioFramePos = performProcessAction
          ( processAction
          , audioInputStream
          , currentAudioFramePos
          );
      }
    }
    finally
    {
      if (audioInputStream!=null)
      {
        // Don't handle exceptions, as there's really nothing we can do about them.
        TrackCutter.logger.fine("Closing audio stream.");
        audioInputStream.close();
      }
    }
    TrackCutter.logger.exiting(TrackCutter.class.getCanonicalName(), "cutTracksInCueSheet(FileData)");
  }
  
  /**
   * Get a list of ProcessActions based on the specified FileData.
   * @param fileData
   * @return A list of ProcessActions based on the specified FileData.
   */
  private List<TrackCutterProcessingAction> getProcessActionList(final FileData fileData)
  {
    TrackCutter.logger.entering(TrackCutter.class.getCanonicalName(), "getProcessActionList(FileData)", fileData);
    TrackCutter.logger.fine("Determining processing actions for file: '" + fileData.getFile() + "'.");
    List<TrackCutterProcessingAction> result = new ArrayList<TrackCutterProcessingAction>();
    TrackData previousTrackData = null;
    
    // Process all tracks in turn.
    for (TrackData currentTrackData : fileData.getTrackData())
    {
      if (previousTrackData != null)
      {
        if (currentTrackData.getIndex(0) != null)
        {
          addProcessActions(previousTrackData, currentTrackData.getIndex(0).getPosition(), result);
        }
        else
        {
          addProcessActions(previousTrackData, currentTrackData.getIndex(1).getPosition(), result);
        }
      }
      previousTrackData = currentTrackData;
    }
    
    // Handle last track, if any.
    if (previousTrackData != null)
    {
      addProcessActions(previousTrackData, null, result);
    }
    
    TrackCutter.logger.exiting(TrackCutter.class.getCanonicalName(), "getProcessActionList(FileData)", result);
    return result;
  }
  
  /**
   * Add ProcesAction instances for the specified TrackData.
   * @param trackData
   * @param nextPosition The first position after the current track, or null if there is no next position.
   * (Track continues until the end of data.) 
   * @param processActions A list of ProcessAction instances to which the actions for this TrackData
   * will be added.
   */
  private void addProcessActions
    ( final TrackData trackData
    , final Position nextPosition
    , final List<TrackCutterProcessingAction> processActions
    )
  {
    TrackCutter.logger.entering
      ( TrackCutter.class.getCanonicalName()
      , "addProcessActions(TrackData,Position,List<TrackCutterProcessingAction>)"
      , new Object[] {trackData, nextPosition, processActions}
      );
    TrackCutter.logger.fine("Adding processing action for track #" + trackData.getNumber() + ".");
    if (trackData.getIndex(0) == null)
    {
      // No pregap to handle. Just process this track.
      processActions.add(new TrackCutterProcessingAction(trackData.getIndex(1).getPosition(), nextPosition, trackData, false, getConfiguration()));
    }
    else
    {
      switch (configuration.getPregapHandling())
      {
        case DISCARD:
          // Discard the pregap, process the track.
          processActions.add(new TrackCutterProcessingAction(trackData.getIndex(1).getPosition(), nextPosition, trackData, false, getConfiguration()));
          break;
        case PREPEND:
          // Prepend the pregap, if long enough.
          if  ( trackData.getIndex(1).getPosition().getTotalFrames()
              - trackData.getIndex(0).getPosition().getTotalFrames()
              >= this.getConfiguration().getPregapFrameLengthThreshold()
              )
          {
            processActions.add(new TrackCutterProcessingAction(trackData.getIndex(0).getPosition(), nextPosition, trackData, true, getConfiguration()));
          }
          else
          {
            processActions.add(new TrackCutterProcessingAction(trackData.getIndex(1).getPosition(), nextPosition, trackData, false, getConfiguration()));
          }
          break;
        case SEPARATE:
          // Add pregap and track as separate tracks.
          // Prepend the pregap, if long enough.
          if  ( trackData.getIndex(1).getPosition().getTotalFrames()
              - trackData.getIndex(0).getPosition().getTotalFrames()
              >= this.getConfiguration().getPregapFrameLengthThreshold()
              )
          {
            processActions.add(new TrackCutterProcessingAction(trackData.getIndex(0).getPosition(), trackData.getIndex(1).getPosition(), trackData, true, getConfiguration()));
          }
          processActions.add(new TrackCutterProcessingAction(trackData.getIndex(1).getPosition(), nextPosition, trackData, false, getConfiguration()));
          break;
      }
    }
    TrackCutter.logger.exiting
      ( TrackCutter.class.getCanonicalName()
      , "addProcessActions(TrackData,Position,List<TrackCutterProcessingAction>)"
      );
  }
  
  /**
   * Perform the specified ProcessAction.
   * @param processAction
   * @param audioInputStream The audio stream from which to read.
   * @param currentAudioFramePos The current frame position in the audio stream.
   * @return The current frame position after processing.
   * @throws IOException
   */
  private long performProcessAction ( final TrackCutterProcessingAction processAction
                                    , final AudioInputStream audioInputStream
                                    , final long currentAudioFramePos
                                    ) throws IOException
  {
    TrackCutter.logger.entering
      ( TrackCutter.class.getCanonicalName()
      , "performProcessAction(TrackCutterProcessingAction,AudioInputStream,long)"
      , new Object[] {processAction, audioInputStream, currentAudioFramePos}
      );
    TrackCutter.logger.fine
      ( "Determining audio substream for processing action for "
      + (processAction.getIsPregap()?"pregap of ":"") + "track #"
      + processAction.getTrackData().getNumber() + "."
      );
    
    // Skip positions in the audioInputStream until we are at out starting position.
    long fromAudioFramePos = skipToPosition (processAction.getStartPosition(), audioInputStream, currentAudioFramePos);
    
    // Determine the position to which we should read from the input.
    long toAudioFramePos = audioInputStream.getFrameLength();
    if (processAction.getEndPosition() != null)
    {
      toAudioFramePos = getAudioFormatFrames(processAction.getEndPosition(), audioInputStream.getFormat());
    }
    
    performProcessAction
      ( processAction
      , new AudioInputStream(audioInputStream, audioInputStream.getFormat(), toAudioFramePos - fromAudioFramePos)
      );
    
    TrackCutter.logger.exiting
      ( TrackCutter.class.getCanonicalName()
      , "performProcessAction(TrackCutterProcessingAction,AudioInputStream,long)"
      );
    return toAudioFramePos;
  }
  
  /**
   * Perform the specified ProcessAction.
   * @param processAction
   * @param audioInputStream The audio stream from which to read. This stream will be closed afterward.
   * @throws IOException
   */
  private void performProcessAction ( final TrackCutterProcessingAction processAction
                                    , final AudioInputStream audioInputStream
                                    ) throws IOException
  {
    TrackCutter.logger.entering
      ( TrackCutter.class.getCanonicalName()
      , "performProcessAction(TrackCutterProcessingAction,AudioInputStream)"
      , new Object[] {processAction, audioInputStream}
      );
    TrackCutter.logger.info
      ( "Performing processing action for " + (processAction.getIsPregap()?"pregap of ":"")
      + "track #" + processAction.getTrackData().getNumber() + "."
      );
    
    if (!getConfiguration().getRedirectToPostprocessing())
    {
      // We're going to create target files, so make sure there's a directory for them.
      TrackCutter.logger.fine("Creating directory for target files.");
      processAction.getCutFile().getParentFile().mkdirs();
    }
    
    if (configuration.getDoPostProcessing() && configuration.getRedirectToPostprocessing())
    {
      OutputStream audioOutputStream = null;
      
      try
      {
        TrackCutter.logger.fine("Writing audio to postprocessor.");
        audioOutputStream = this.createPostProcessingProcess(processAction).getOutputStream();
        AudioSystem.write(audioInputStream, configuration.getTargetType(), audioOutputStream);
      }
      finally
      {
        if (audioOutputStream!=null)
        {
          // We can't do anything about any exceptions here, so we don't catch them.
          TrackCutter.logger.fine("Closing audio stream.");
          audioOutputStream.close();
        }
      }
    }
    else
    {
      TrackCutter.logger.fine("Writing audio to file.");
      AudioSystem.write(audioInputStream, configuration.getTargetType(), processAction.getCutFile());
      
      if (configuration.getDoPostProcessing())
      {
        TrackCutter.logger.fine("Performing postprocessing.");
        this.createPostProcessingProcess(processAction);
      }
    }
    
    TrackCutter.logger.exiting
      ( TrackCutter.class.getCanonicalName()
      , "performProcessAction(TrackCutterProcessingAction,AudioInputStream)"
      );
  }
  
  /**
   * Create the specified post-processing process.
   * @param processAction
   * @return The specified post-processing process.
   * @throws IOException
   */
  private Process createPostProcessingProcess
    ( final TrackCutterProcessingAction processAction
    ) throws IOException
  {
    TrackCutter.logger.entering
      ( TrackCutter.class.getCanonicalName()
      , "performProcessAction(TrackCutterProcessingAction)"
      , processAction
      );
    TrackCutter.logger.fine("Creating post-processing process for command: " + processAction.getPostProcessCommand());
    processAction.getPostProcessFile().getParentFile().mkdirs();
    Process process = Runtime.getRuntime().exec(processAction.getPostProcessCommand());
    
    StreamPiper.pipeStream(process.getInputStream(), processAction.getStdOutRedirectFile());
    StreamPiper.pipeStream(process.getErrorStream(), processAction.getErrRedirectFile());
    
    TrackCutter.logger.exiting
      (TrackCutter.class.getCanonicalName(), "performProcessAction(TrackCutterProcessingAction)", process);
    return process;
  }
  
  /**
   * Get the number of AudioFormat frames represented by the specified Position. Note that an AudioFormat
   * frame may represent a longer or shorter time than a cue sheet frame. 
   * @param position
   * @param audioFileFormat
   * @return The number of AudioFormat frames represented by the specified Position. Note that an AudioFormat
   * frame may represent a longer or shorter time than a cue sheet frame.
   */
  private static long getAudioFormatFrames(final Position position, final AudioFormat audioFormat)
  {
    TrackCutter.logger.entering
      ( TrackCutter.class.getCanonicalName()
      , "getAudioFormatFrames(Position,AudioFormat)"
      , new Object[] {position, audioFormat}
      );
    // Determine closest frame number.
    long result = (long) Math.round(((double) audioFormat.getFrameRate())/75 * position.getTotalFrames());
    TrackCutter.logger.exiting
      ( TrackCutter.class.getCanonicalName()
      , "getAudioFormatFrames(Position,AudioFormat)"
      , result
      );
    return result;
  }
  
  /**
   * Skip to the specified position in the audio data.
   * @param toPosition The position to skip to.
   * @param audioInputStream The audio data to skip in.
   * @param currentAudioFramePos The current position in frames in the audio data.
   * @return The frame position in the audio data after skipping.
   * @throws IOException
   */
  private long skipToPosition
    ( Position toPosition
    , AudioInputStream audioInputStream
    , long currentAudioFramePos
    ) throws IOException
  {
    TrackCutter.logger.entering
      ( TrackCutter.class.getCanonicalName()
      , "skipToPosition(Position,AudioInputStream,long)"
      , new Object[] {toPosition, audioInputStream, currentAudioFramePos}
      );
    long toAudioFramePos = getAudioFormatFrames(toPosition, audioInputStream.getFormat());
    audioInputStream.skip((toAudioFramePos - currentAudioFramePos)  * audioInputStream.getFormat().getFrameSize());
    TrackCutter.logger.exiting
      (TrackCutter.class.getCanonicalName(), "skipToPosition(Position,AudioInputStream,long)", toAudioFramePos);
    return toAudioFramePos;
  }
  
  /**
   * Log an exception that was caught.
   * @param exception The exception that was caught and should now be logged.
   */
  private void logCaughtException(Exception exception)
  {
    TrackCutter.logger.entering(TrackCutter.class.getCanonicalName(), "logCaughtException(Exception)", exception);
    TrackCutter.logger.severe
      ("Encountered an " + exception.getClass().getCanonicalName() + ": " + exception.getMessage());
    StringWriter writer = new StringWriter();
    exception.printStackTrace(new PrintWriter(writer));
    TrackCutter.logger.fine(writer.toString());
    TrackCutter.logger.exiting(TrackCutter.class.getCanonicalName(), "logCaughtException(Exception)");
  }
  
  /**
   * Get the configuration for this TrackCutter.
   * @return The configuration for this TrackCutter.
   */
  private TrackCutterConfiguration getConfiguration()
  {
    TrackCutter.logger.entering(TrackCutter.class.getCanonicalName(), "getConfiguration()");
    TrackCutter.logger.exiting(TrackCutter.class.getCanonicalName(), "getConfiguration()", this.configuration);
    return this.configuration;
  }
}
