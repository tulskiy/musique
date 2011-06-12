package org.discogs.model;

import java.util.ArrayList;
import java.util.List;

import org.benow.java.rest.XMLAccessor;
import org.discogs.ws.Discogs;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

class DiscogsObject extends XMLAccessor {

  protected transient Discogs client;

  public DiscogsObject(Element elem, Discogs client) {
    super(elem);
    this.client = client;
    // this.inputEncoding = "ISO-8859-1";
    // this.outputEncoding = "UTF-8";

  }

  public List<Image> getImages() {
    List<Image> results = new ArrayList<Image>();
    Element imagesE = (Element) getNodeByPath("images");
    if (imagesE != null) {
      NodeList cn = imagesE.getElementsByTagName("image");
      for (int i = 0; i < cn.getLength(); i++)
        results.add(new Image((Element) cn.item(i)));
    }
    return results;
  }

  public List<String> getURLs() {
    List<String> results = new ArrayList<String>();
    Element es = (Element) getNodeByPath("urls");
    if (es != null) {
      NodeList cn = es.getElementsByTagName("url");
      for (int i = 0; i < cn.getLength(); i++)
        results.add(((Element) cn.item(i)).getFirstChild().getNodeValue());
    }
    return results;
  }

}
