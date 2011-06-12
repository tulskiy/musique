package org.discogs.ws.search;

import org.discogs.model.Artist;
import org.discogs.ws.Discogs;
import org.w3c.dom.Element;

public class ArtistSearchResult extends SearchResult {

  public ArtistSearchResult(Element element, Discogs client) {
    super(element, client);
  }

  public Artist getArtist() {
    return client.getArtist(getTitle());
  }
}
