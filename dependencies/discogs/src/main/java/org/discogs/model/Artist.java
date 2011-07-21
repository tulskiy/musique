package org.discogs.model;

import java.util.ArrayList;
import java.util.List;

import org.discogs.ws.Discogs;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


public class Artist extends DiscogsObject {

  public Artist(Element artistElem, Discogs client) {
    super(artistElem, client);
  }

  public String getName() {
    return getStringByPath("name");
  }

  public String getProfile() {
    return getStringByPath("profile");
  }

  public List<String> getNameVariations() {
    List<String> results = new ArrayList<String>();
    Element es = (Element) getNodeByPath("namevariations");
    if (es != null) {
      NodeList cn = es.getElementsByTagName("name");
      for (int i = 0; i < cn.getLength(); i++)
        results.add(((Element) cn.item(i)).getFirstChild().getNodeValue());
    }
    return results;
  }

  public String getRealName() {
    return getStringByPath("realname");
  }

  public List<String> getAliases() {
    List<String> results = new ArrayList<String>();
    Element es = (Element) getNodeByPath("aliases");
    if (es != null) {
      NodeList cn = es.getElementsByTagName("name");
      for (int i = 0; i < cn.getLength(); i++)
        results.add(((Element) cn.item(i)).getFirstChild().getNodeValue());
    }
    return results;
  }

  public List<String> getGroups() {
    List<String> results = new ArrayList<String>();
    Element es = (Element) getNodeByPath("groups");
    if (es != null) {
      NodeList cn = es.getElementsByTagName("name");
      for (int i = 0; i < cn.getLength(); i++)
        results.add(((Element) cn.item(i)).getFirstChild().getNodeValue());
    }
    return results;
  }

  public List<ArtistRelease> getReleases() {
    List<ArtistRelease> results = new ArrayList<ArtistRelease>();
    Element es = (Element) getNodeByPath("releases");
    if (es != null) {
      NodeList cn = es.getElementsByTagName("release");
      for (int i = 0; i < cn.getLength(); i++)
        results.add(new ArtistRelease((Element) cn.item(i),
          client));
    }
    return results;
  }

}
