package org.discogs.ws.search;

import java.util.ArrayList;
import java.util.List;

import org.benow.java.rest.XMLAccessor;
import org.discogs.ws.Discogs;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Search extends XMLAccessor {

  private final Discogs client;

  public Search(Element element, Discogs client) {
    super(element);
    this.client = client;
  }

  public List<SearchResult> getExactResults() {
    List<SearchResult> results = new ArrayList<SearchResult>();
    Element es = (Element) getNodeByPath("exactresults");
    if (es != null) {
      NodeList cn = es.getElementsByTagName("result");
      for (int i = 0; i < cn.getLength(); i++)
        results.add(SearchResult.createFrom((Element) cn.item(i), client));
    }
    return results;
  }

  public List<SearchResult> getSearchResults() {
    List<SearchResult> results = new ArrayList<SearchResult>();
    NodeList cn = element.getElementsByTagName("result");
    for (int i = 0; i < cn.getLength(); i++)
      results.add(SearchResult.createFrom((Element) cn.item(i),
        client));
    return results;
  }

}
