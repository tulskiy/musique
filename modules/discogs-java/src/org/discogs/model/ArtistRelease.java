package org.discogs.model;

import org.benow.java.rest.XMLAccessor;
import org.discogs.ws.Discogs;
import org.w3c.dom.Element;

public class ArtistRelease extends XMLAccessor {

  private transient Discogs client;

  public ArtistRelease(Element item, Discogs client) {
    super(item);
    this.client = client;
  }

  public String getId() {
    return getStringByPath("@id");
  }

  public String getStatus() {
    return getStringByPath("@status");
  }

  public String getType() {
    return getStringByPath("@type");
  }

  public String getTitle() {
    return getStringByPath("title");
  }

  public String getFormatString() {
    return getStringByPath("format");
  }

  public String[] getFormats() {
    String fmts = getFormatString();
    if (fmts != null)
      return fmts.split(",");
    return new String[] {};
  }

  public String getLabelName() {
    return getStringByPath("label");
  }

  public String[] getLabelNames() {
    String lbl = getStringByPath("label");
    if (lbl == null)
      return new String[] {};
    return lbl.split(":");
  }

  public Label getLabel() {
    String[] lblStr = getLabelNames();
    if (lblStr.length == 0)
      return null;
    return client.getLabel(lblStr[0]);
  }

  public Label[] getLabels() {
    String[] lblStr = getLabelNames();
    if (lblStr.length == 0)
      return new Label[] {};
    Label[] labels = new Label[lblStr.length];
    for (int i = 0; i < lblStr.length; i++) {
      String curr = lblStr[i];
      labels[i] = client.getLabel(curr);
    }
    return labels;
  }

  public int getYear() {
    return getIntByPath("year");
  }

}
