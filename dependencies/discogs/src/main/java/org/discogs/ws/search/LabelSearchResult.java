package org.discogs.ws.search;

import org.discogs.ws.Discogs;
import org.w3c.dom.Element;

public class LabelSearchResult extends SearchResult {

  public LabelSearchResult(Element element, Discogs client) {
    super(element, client);
  }

}
