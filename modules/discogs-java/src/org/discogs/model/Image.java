package org.discogs.model;

import java.net.MalformedURLException;
import java.net.URL;

import org.benow.java.rest.XMLAccessor;
import org.w3c.dom.Element;

public class Image extends XMLAccessor {

  public Image(Element element) {
    super(element);
  }

  public int getHeight() {
    return getIntByPath("@height");
  }

  public int getWidth() {
    return getIntByPath("@width");
  }

  public String getType() {
    return getStringByPath("@type");
  }

  public URL getURL() throws MalformedURLException {
    return new URL(getStringByPath("@uri"));
  }

  public URL getThumbURL() throws MalformedURLException {
    return new URL(getStringByPath("@uri150"));
  }

}
