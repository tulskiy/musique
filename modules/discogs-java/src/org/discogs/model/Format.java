package org.discogs.model;

import org.benow.java.rest.XMLAccessor;
import org.w3c.dom.Element;

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
}
