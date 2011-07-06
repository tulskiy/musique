package org.discogs.model;

import java.util.ArrayList;
import java.util.List;

import org.benow.java.rest.XMLAccessor;
import org.discogs.ws.Discogs;
import org.w3c.dom.Element;

public class ReleaseArtist extends XMLAccessor {

  private transient Discogs client;

  public ReleaseArtist(Element element, Discogs client) {
    super(element);
    this.client = client;
  }

  public Artist getArtist() {
    String n = getName();
    if (n == null)
      return null;
    return client.getArtist(n, getANV());
  }
  public String getName() {
    return getStringByPath("name");
  }

  public String getANV() {
    return getStringByPath("anv");
  }

  public String getJoin() {
    return getStringByPath("join");
  }

  public List<String> getRoles() {
    List<String> roles = new ArrayList<String>();
    String r = getStringByPath("role");
    if (r.contains(",")) {
      for (String c : r.split(","))
        roles.add(c);
    } else
      roles.add(r);
    return roles;
  }
}
