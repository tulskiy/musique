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
package jwbroek.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import jwbroek.util.LogUtil;

/**
 * Utility class for piping data from an InputStream to an OutputStream, or to nowhere. This class is particularly useful
 * for reading the output streams of a java.lang.Process, as such a Process may block if its output is not read.
 * @author jwbroek
 */
public class StreamPiper implements Runnable
{
  /**
   * Stream to read input from. mus not be null.
   */
  private InputStream from;
  /**
   * Stream to pipe input to. May be null, in which case all input is discarded.
   */
  private OutputStream to;
  /**
   * Whether or not to close the output stream after all data has been piped.
   */
  private boolean closeOutput;
  /**
   * The logger for this class.
   */
  private final static Logger logger = Logger.getLogger(StreamPiper.class.getCanonicalName());
  
  /**
   * Pipe all input from the InputStream to the OutputStream. The OutputStream is explicitly allowed to be null.
   * In such a case, all input will be discarded. In any case, the OutputStream will not be closed by StreamPiper,
   * but the InputStream will, once its end is reached.
   * @param from
   * @param to
   */
  public StreamPiper(final InputStream from, final OutputStream to)
  {
    this(from, to, false);
    StreamPiper.logger.entering
      (StreamPiper.class.getCanonicalName(), "StreamPiper(InputStream,OutputStream)", new Object[] {from, to});
    StreamPiper.logger.exiting(StreamPiper.class.getCanonicalName(), "StreamPiper(InputStream,OutputStream)");
  }
  
  /**
   * Pipe all input from the InputStream to the OutputStream. The OutputStream is explicitly allowed to be null.
   * In such a case, all input will be discarded. In any case, the OutputStream will only be closed by StreamPiper if
   * this is requested, while the InputStream will always be, once its end is reached.
   * @param from
   * @param to
   * @param closeOutput
   */
  public StreamPiper(final InputStream from, final OutputStream to, final boolean closeOutput)
  {
    StreamPiper.logger.entering
      ( StreamPiper.class.getCanonicalName()
      , "StreamPiper(InputStream,OutputStream,boolean)"
      , new Object [] {from, to, closeOutput}
      );
    this.from = from;
    this.to = to;
    this.closeOutput = closeOutput;
    StreamPiper.logger.exiting(StreamPiper.class.getCanonicalName(), "StreamPiper(InputStream,OutputStream,boolean)");
  }
  
  /**
   * Pipe the contents of the specified input stream to the specified file, or throw it away if the file is
   * null.
   * @param from The input to stream to file.
   * @param file The file to pipe input to, or null if the input should be thrown away.
   * @throws IOException
   */
  public static void pipeStream(final InputStream from, final File file) throws IOException
  {
    StreamPiper.logger.entering
      (StreamPiper.class.getCanonicalName(), "pipeStream(InputStream,File)", new Object [] {from, file});
    OutputStream out = null;
    if (file!=null)
    {
      out = new FileOutputStream(file);
    }
    new Thread(new StreamPiper(from, out, true)).start();
    StreamPiper.logger.exiting(StreamPiper.class.getCanonicalName(), "pipeStream(InputStream,File)");
  }
  
  /**
   * Perform the data piping.
   */
  public void run()
  {
    StreamPiper.logger.entering(StreamPiper.class.getCanonicalName(), "run()");
    try
    {
      int input = this.from.read();
      while (input != -1)
      {
        if (this.to != null)
        {
          this.to.write(input);
        }
        input = this.from.read();
      }
    }
    catch (IOException e)
    {
      // Nothing we can do.
      LogUtil.logStacktrace(StreamPiper.logger, Level.WARNING, e);
    }
    finally
    {
      try
      {
        this.from.close();
      }
      catch (IOException e)
      {
        // Nothing we can do.
        LogUtil.logStacktrace(StreamPiper.logger, Level.WARNING, e);
      }
      if (this.closeOutput && this.to != null)
      {
        try
        {
          this.to.close();
        }
        catch (IOException e)
        {
          // Nothing we can do.
          LogUtil.logStacktrace(StreamPiper.logger, Level.WARNING, e);
        }
      }
    }
    StreamPiper.logger.exiting(StreamPiper.class.getCanonicalName(), "run()");
  }
}