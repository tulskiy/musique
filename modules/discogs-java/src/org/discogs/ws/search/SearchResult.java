package org.discogs.ws.search;

import java.net.MalformedURLException;
import java.net.URL;

import org.benow.java.rest.XML;
import org.benow.java.rest.XMLAccessor;
import org.discogs.ws.Discogs;
import org.w3c.dom.Element;

public class SearchResult extends XMLAccessor {

  protected Discogs client;

  public SearchResult(Element element, Discogs client) {
    super(element);
    this.client = client;
  }

  /**
   * Gets the value of the title element.  This may not actually be
   * what you want.  For releases the title is not the album title, but
   * rather the 'artist - album_title'.  See ReleaseSearchResult to access
   * the real title.
   * @return
   */
  public String getTitle() {
    return getStringByPath("title");
  }

  public URL getURL() throws MalformedURLException {
    String urlStr = getStringByPath("uri");
    if (urlStr == null)
      return null;
    return new URL(urlStr);
  }

  public String getSummary() {
    return getStringByPath("summary");
  }

  public static SearchResult createFrom(
      Element elem,
      Discogs client) {
    String type = elem.getAttribute("type");
    if ("release".equals(type) || "master".equals(type))
      return new ReleaseSearchResult(elem,
        client);
    if ("artist".equals(type))
      return new ArtistSearchResult(elem,
        client);
    if ("label".equals(type))
      return new LabelSearchResult(elem,
        client);
    System.err.println("No such type: " + type + " on search result: " + XML.elementToString(elem));
    return new SearchResult(elem,
      client);
  }

}
