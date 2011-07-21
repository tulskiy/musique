package org.discogs.ws.search;

import java.net.MalformedURLException;

import org.discogs.ws.Discogs;
import org.w3c.dom.Element;

public class ReleaseSearchResult extends SearchResult {


  public ReleaseSearchResult(Element element, Discogs client) {
    super(element, client);
  }

  /**
   * Gets the id of the release.  This not available directly in the search
   * results, but is extracted from the url
   * @return id of the release.
   * @throws MalformedURLException
   */
  public String getId() throws MalformedURLException {
    String urlStr = getURL().toString();
    int pos = urlStr.lastIndexOf("/");
    if (pos > -1)
      return urlStr.substring(pos + 1);
    return null;
  }

  /**
   * Get the title of the album.  This is the second half of the 
   * title element value, which is 'artist - album_title'
   * @return title of the album
   */
  public String getAlbumTitle() {
    int pos = getTitle().lastIndexOf(" - ");
    if (pos > -1)
      return getTitle().substring(pos + 3);
    return getTitle();
  }

  /**
   * Get the artist of the album.  This is the first half of the 
   * title element value, which is 'artist - album_title'
   * @return artist of the album
   */
  public String getAlbumArtist() {
    int pos = getTitle().lastIndexOf(" - ");
    if (pos > -1)
      return getTitle().substring(0, pos);
    return getTitle();
  }

}
