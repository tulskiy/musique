package org.discogs.model;

import java.util.ArrayList;
import java.util.List;

import org.benow.java.rest.XMLAccessor;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Format extends XMLAccessor {

  public Format(Element element) {
    super(element);
  }

  public String getName() {
    return getStringByPath("@name");
  }

  public int getQuantity() {
    return getIntByPath("@qty");
  }

  public String getDescription() {
    return getStringByPath("descriptions/description");
  }

  public List<String> getDescriptions() {
    List<String> descriptions = new ArrayList<String>();
    Element gE = (Element) getNodeByPath("descriptions");
    if (gE != null) {
      NodeList ges = gE.getElementsByTagName("description");
      for (int i = 0; i < ges.getLength(); i++) {
        Element curr = (Element) ges.item(i);
        descriptions.add(curr.getFirstChild().getNodeValue());
      }
    }
    return descriptions;
  }

}
