package org.benow.java.rest;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.zip.GZIPInputStream;

import org.w3c.dom.Document;

/**
 * Utility class to load pages and XML documents from a site.  Uses caching 
 * and gzip compression for performance.  Implements throttling of requests,
 * if specified.
 * 
 * @author andy
 *
 */
public class DocumentLoader {
  /**
   * The maximum age of files in the cache.  Files older than this will be deleted.
   */
  public static long MAX_CACHE_AGE_MILLIS = (2 * 7 * 24 * 60 * 60 * 1000);
  private File cacheDir;
  private boolean cacheDisabled;
  private final URL baseURL;
  private long lastLoad;
  private long loadInterval = 0;
  private String agent;
  // charset for string read
  private Charset charset = null;
  private static long cacheTrimTime = 0;

  public DocumentLoader(URL baseURL) {
    this.baseURL = baseURL;
    this.cacheDisabled = false;
    this.cacheDir = new File("var/cache/" + baseURL.getHost() + (baseURL.getPort() != -1 ? "-" + baseURL.getPort() : ""));
  }

  public void setCharset(
      Charset charset) {
    this.charset = charset;
  }

  private void trimCache() {
    long oldTime = System.currentTimeMillis() - MAX_CACHE_AGE_MILLIS;
    if (cacheDir != null && cacheDir.exists()) {
      for (File curr : cacheDir.listFiles()) {
        if (!curr.getName().endsWith(".stamp")) {
          if (curr.isFile() && curr.lastModified() < oldTime) {
            curr.delete();
            File stampFile = getStampFile(curr);
            if (stampFile.exists())
              stampFile.delete();
          }
        }
      }
    }
  }

  private File getStampFile(
      File cacheFile) {
    return new File(cacheFile.getParentFile(),
      cacheFile.getName() + ".stamp");
  }

  /**
   * Changes the cache directory from the default
   * @param cacheDir
   */
  public void setCacheDir(
      File cacheDir) {
    this.cacheDir = cacheDir;
  }

  public void setUserAgent(
      String agent) {
    this.agent = agent;
  }

  /**
   * set the minimum time between requests (in millis).  If requests arrive
   * more frequently than this interval, then the thread will be stalled, 
   * throttling requests.  Set to 0 or less to disable.  
   * @param loadInterval
   */
  public void setLoadInterval(
      long loadInterval) {
    this.loadInterval = loadInterval;
  }

  public InputStream loadStream(
      String urlStr) throws IOException {
    // trim cache on vm startup or daily
    if (System.currentTimeMillis() - cacheTrimTime > 24 * 60 * 60 * 1000)
      trimCache();
    /*
     * urls with extended characters fail:
     * http://www.discogs.com/artist/St%C3%A9phane+Pompougnac
     * http://www.discogs.com/artist/St%C3%A9phane+Pompougnac
    urlStr = URLEncoder.encode(urlStr, "UTF-8");
    */
    if (urlStr.contains(" ")) {
      urlStr = urlStr.replace(" ", "+");
    }
    URL url = new URL(baseURL,
      urlStr);
    InputStream result = null;
    long lastModified = -1;

    System.out.println("Hitting url: " + url);
    HttpURLConnection.setFollowRedirects(true);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

    // must accept gzip: http://www.discogs.com/help/api
    // http://www.oreillynet.com/onjava/blog/2004/07/optimizing_http_downloads_in_j.html
    conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
    if (agent != null)
      conn.setRequestProperty("User-Agent", agent);
    try {
      conn.connect();
    } catch (ConnectException e) {
      System.err.println("Error connecting to: " + url);
      result = loadFromCache(url, -1);
      if (result != null)
        System.err.println("Returning result directly from cache.");
      return result;
    }

    // use last modified, if supported by server
    String lmStr = conn.getHeaderField("Last-Modified");
    /*
    Map<String, List<String>> fields = conn.getHeaderFields();
    for (String field : fields.keySet()) {
      List<String> vals = fields.get(field);
      String val = null;
      for (int i = 0; i < vals.size(); i++) {
        if (val == null)
          val = vals.get(i);
        else
          val = val + "," + vals.get(i);
      }
      System.out.println(field + ": " + val);
    }
    */
    if (lmStr != null) {
      try {
        lastModified = Long.parseLong(lmStr);
      } catch (NumberFormatException e) {
        System.err.println("Error in Last-Modified header: " + lmStr + ".  Loading new.");
      }
    }

    result = loadFromCache(url, lastModified);
    if (result == null) {
      String encoding = conn.getContentEncoding();
      InputStream in = conn.getInputStream();
      if (encoding != null && encoding.equalsIgnoreCase("gzip"))
        in = new GZIPInputStream(in);

      if (loadInterval > 0 && lastLoad != 0 && System.currentTimeMillis() - lastLoad < loadInterval) {
        long wait = System.currentTimeMillis() - lastLoad;
        System.out.println("Waiting for " + wait + " ms before fetch.  Must be " + loadInterval + "ms between requests.");
        try {
          Thread.sleep(wait);
        } catch (InterruptedException e) {
          // ignore
        }
      }

      in = saveToCache(in, url, lastModified);

      lastLoad = System.currentTimeMillis();
      result = in;
    }
    return result;
  }

  public static String readFromStream(
      InputStream inputStream) throws IOException {
    String read = "";
    BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
    String line = in.readLine();
    while (line != null) {
      read += line + "\n";
      line = in.readLine();
    }
    return read;
  }

  private InputStream saveToCache(
      InputStream in,
      URL url,
      long lastModified) throws IOException {
    if (cacheDisabled)
      return in;
    File cacheFile = urlToCacheFile(url);
    cacheFile.getParentFile().mkdirs();
    OutputStream out = new BufferedOutputStream(new FileOutputStream(cacheFile));
    try {
      byte[] buff = new byte[512];
      int read = in.read(buff);
      while (read > 0) {
        out.write(buff, 0, read);
        read = in.read(buff);
      }
    } finally {
      out.flush();
      out.close();
    }
    if (lastModified > 0) {
      File stampFile = getStampFile(cacheFile);
      stampFile.createNewFile();
      stampFile.setLastModified(lastModified);
    }
    return new BufferedInputStream(new FileInputStream(cacheFile));
  }

  private InputStream loadFromCache(
      URL url,
      long lastModified) throws IOException {
    if (cacheDisabled)
      return null;
    File cacheFile = urlToCacheFile(url);
    if (!cacheFile.exists())
      return null;
    File stampFile = getStampFile(cacheFile);
    if (lastModified >= 0 && stampFile.exists()) {
    // don't load from cache if current last modified is newer
      if (stampFile.lastModified() < lastModified)
      return null;
    }
    return new BufferedInputStream(new FileInputStream(cacheFile));
  }

  private File urlToCacheFile(
      URL url) {
    String fn = url.getFile() + ".xml";
    fn = fn.replace("&", "-");
    fn = fn.replace("/", "-");
    fn = fn.replace("?", "-");
    fn = fn.replace("'", "-");
    fn = fn.replace("\"", "-");
    fn = fn.replace(";", "-");
    fn = fn.replace(":", "-");
    File cacheFile = new File(cacheDir,
      fn);
    return cacheFile;
  }

  /**
   * Turns caching off.  Responses will not be cached and results will not be returned from the cache.
   */
  public void disableCaching() {
    cacheDisabled = true;
  }
  
  public void setCacheEnabled(boolean b) {
	  cacheDisabled = !b;
  }

  public String loadString(
      String urlStr) throws IOException {
    InputStream in = loadStream(urlStr);
    try {
      return readFromStream(in);
    } finally {
      in.close();
    }
  }

  public Document loadDocument(
      String urlStr) throws IOException {
    // http://www.velocityreviews.com/forums/t143346-xml-and-invalid-byte-utf-8-a.html
    String read = loadString(urlStr);
    if (charset != null)
      read = new String(read.getBytes(),
        charset);
    return XML.loadDocument(read);
  }

}
