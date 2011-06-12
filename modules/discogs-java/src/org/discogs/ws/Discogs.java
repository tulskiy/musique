package org.discogs.ws;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.benow.java.rest.DocumentLoader;
import org.benow.java.rest.XML;
import org.discogs.model.Artist;
import org.discogs.model.Label;
import org.discogs.model.Release;
import org.discogs.model.Track;
import org.discogs.ws.search.Search;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Various tools for discogs lookup via rest interface.
 * <p/>
 * Usage:
 * <pre>
 *  // create new client with your api key
 *  // see https://www.discogs.com/users/api_key 
 *  Discogs discogs=new Discogs("1234");
 *  Artist artist=discogs.getArtist("Richard H. Kirk");
 *  System.out.println(artist);
 * </pre>
 * @author andy
 *
 */
public class Discogs {
  /**d
   * API key to use in discogs communication.  May be set directly, specfified in Discogs(String) constructor
   * or as the system property 'apiKey' on startup (ie -DapiKey=12345).  An API key must be provided in some
   * form. 
   */
  public static final String API_KEY = System.getProperty("apiKey");

  public static final String SEARCH_TYPE_RELEASE = "release";
  public static final String SEARCH_TYPE_LABEL = "label";
  public static final String SEARCH_TYPE_ARTIST = "artist";

  public final DocumentLoader loader;
  private final String apiKey;

  /**
   * Create client with API_KEY key statically set or via apiKey system property (-DapiKey=12345 ).
   * <p/>
   * See <a href="https://www.discogs.com/users/api_key">discogs</a> to get an api key.  
   */
  public Discogs() {
    this(API_KEY);
  }

  /**
   * Create client with a given api key.
   * <p/>
   * See <a href="https://www.discogs.com/users/api_key">discogs</a> to get an api key.  
   */
  public Discogs(String apiKey) {
    super();
    if (apiKey == null)
      throw new NullPointerException("Error, an apiKey must be given.  Specify directly in constructor, set the API_KEY field or provide an apiKey system property (-DapiKey=1234)");

    try {
      loader = new DocumentLoader(new URL("http://discogs.com"));
      loader.setCharset(Charset.forName("UTF-8"));
      loader.setLoadInterval(1000);
      loader.setUserAgent("discogs-java/0.01 +http://benow.ca/projects/discogs-java");
    } catch (MalformedURLException e) {
      throw new RuntimeException("Impossible",
        e);
    }
    this.apiKey = apiKey;
  }

  public Release getRelease(
      String id) {
    Element resultE = loadResult("/release/" + id);
    if (resultE != null)
    return new Release(resultE,
      this);
    return null;
  }

  public Artist getArtist(
      String name,
      String anv) {
    try {
      String convName = URLEncoder.encode(name, "ISO-8859-1");
      String convAnv = null;
      if (anv != null)
        convAnv = URLEncoder.encode(name, "ISO-8859-1");
      Element resultE = loadResult("/artist/" + convName + (anv != null ? "?anv=" + convAnv : ""));
      if (resultE != null)
      return new Artist(resultE,
        this);
      return null;
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
      return null;
    }
  }

  public Artist getArtist(
      String name) {
    return getArtist(name, null);
  }

  public Label getLabel(
      String name) {
    try {
      String convName = URLEncoder.encode(name, "ISO-8859-1");
      Element resultE = loadResult("/label/" + convName);
    if (resultE != null)
    return new Label(resultE,
      this);
    return null;
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
      return null;
    }
  }

  public Search search(
      String term) {
    return search(null, term);
  }

  public Search search(
      String type,
      String term) {
    try {
      String convTerm = URLEncoder.encode(term, "UTF-8");
      Element resultE = loadResult("/search?" + (type == null ? "" : "type=" + type + "&") + "q=" + convTerm);
    if (resultE != null)
    return new Search(resultE,
      this);
    return null;
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
      return null;
    }
  }

  private Element loadResult(
      String relURL) {
    try {
      Document doc = loader.loadDocument(relURL + (relURL.contains("?") ? "&" : "?") + "f=xml&api_key=" + apiKey);
      Element resp = doc.getDocumentElement();
      if (!resp.hasAttribute("stat") || !resp.getAttribute("stat").equals("ok"))
        throw new IOException("Error fetching from: " + relURL + ".  Expected stat='ok' in:\n" + XML.elementToString(resp));
      NodeList cn = resp.getChildNodes();
      for (int i = 0; i < cn.getLength(); i++) {
        Node curr = cn.item(0);
        if (curr instanceof Element)
          return (Element) curr;
      }
      throw new IOException("Error fetching from: " + relURL + ".  Expected contained element in:\n" + XML.elementToString(resp));
    } catch (IOException e) {
      throw new RuntimeException("Error during fetch",
        e);
    }

  }

  public static String readFromURL(
      URL fromURL,
      Map<String, String> headers) throws IOException {
    URLConnection conn = fromURL.openConnection();
    conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.5; Windows NT 5.0");
    if (headers != null) {
      for (String key : headers.keySet()) {
        String val = headers.get(key);
        conn.setRequestProperty(key, val);
      }
    }
    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
    String inStr = "";
    String currStr = in.readLine();
    while (currStr != null) {
      inStr += currStr + "\n";
      currStr = in.readLine();
    }
    in.close();
    return inStr;
  }

  public static void main(
      String[] args) {
    try {
      Discogs discogs = new Discogs();
      String id = "3614";
      Release release = discogs.getRelease(id);
      if (release == null)
        System.out.println("Release not found for id: " + id);
      else {
        System.out.println(release);
        System.out.println("Title: " + release.getTitle());
        System.out.println("Artist: " + release.getArtists().get(0).getName());
        System.out.println("Tracks");
        List<Track> tracks = release.getTracks();
        for (int i = 0; i < tracks.size(); i++) {
          Track curr = tracks.get(i);
          System.out.println("\t" + (i + 1) + ": " + curr.getTitle());
        }
      }
      Artist artist = discogs.getArtist("Richard H. Kirk", "Richard H Kirk");
      System.out.println(artist);
      Label label = discogs.getLabel("Warp Records");
      System.out.println(label.getProfile());
      // this fails on extended characters.
      artist = discogs.getArtist("Stéphane Pompougnac");
      System.out.println(artist.getProfile());
      Search s = discogs.search(SEARCH_TYPE_ARTIST, "Stéphane Pompougnac");
      System.out.println(s);
      Release r = discogs.getRelease("507569");
      System.out.println("Track name with UTF-8 char: " + r.getTracks().get(1).getTitle());
      // System.out.println(r);

      System.exit(0);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(-1);
    }
  }

}
