package org.discogs.model;

import java.util.ArrayList;
import java.util.List;

import org.discogs.ws.Discogs;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Label extends DiscogsObject {

  public Label(Element labelE, Discogs client) {
    super(labelE, client);
  }

  public String getName() {
    return getStringByPath("name");
  }

  public String getContactInfo() {
    return getStringByPath("contact-info");
  }

  public String getProfile() {
    return getStringByPath("profile");
  }

  public List<String> getSubLabels() {
    List<String> results = new ArrayList<String>();
    Element es = (Element) getNodeByPath("sublabels");
    if (es != null) {
      NodeList cn = es.getElementsByTagName("label");
      for (int i = 0; i < cn.getLength(); i++)
        results.add(((Element) cn.item(i)).getFirstChild().getNodeValue());
    }
    return results;
  }

  public List<LabelRelease> getReleases() {
    List<LabelRelease> results = new ArrayList<LabelRelease>();
    Element es = (Element) getNodeByPath("releases");
    if (es != null) {
      NodeList cn = es.getElementsByTagName("release");
      for (int i = 0; i < cn.getLength(); i++)
        results.add(new LabelRelease((Element) cn.item(i),
          client));
    }
    return results;
  }


}
