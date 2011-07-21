package org.discogs.model;

import org.benow.java.rest.XMLAccessor;
import org.discogs.ws.Discogs;
import org.w3c.dom.Element;

public class LabelRelease extends XMLAccessor {

  private final Discogs client;

  public LabelRelease(Element element, Discogs client) {
    super(element);
    this.client = client;
  }

  public String getLabelName() {
    return getStringByPath("@name");
  }

  public String getCatalogNumber() {
    String res = getStringByPath("@catno");
    if (res == null)
      res = getStringByPath("catno");
    return res;
  }

  public String getId() {
    return getStringByPath("@id");
  }

  public String getStatus() {
    return getStringByPath("@status");
  }

  public String getTitle() {
    return getStringByPath("title");
  }

  public String getFormat() {
    return getStringByPath("format");
  }

  public String getArtist() {
    return getStringByPath("artist");
  }

  public String[] getFormats() {
    String fmts = getFormat();
    if (fmts == null)
      return new String[] {};
    return fmts.split(",");
  }

  public Release getRelease() {
    return client.getRelease(getId());
  }
}
