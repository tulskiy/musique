package org.benow.xml.sax;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Creates DOM fragments from the SAX events.  Fragments are created
 * for each subtree which initially matches the given element name.  For example,
 * for the xml document:
 * <pre>
 * &lt;items&gt;
 * &lt;item&gt;
 *   &lt;name&gt;boo&lt;/name&gt;
 *   &lt;item&gt;...&lt;/item&gt;
 * &lt;/item&gt;
 * &lt;item&gt;
 *   &lt;name&gt;two&lt;/item&gt;
 * &lt;/item&gt;
 * <pre> 
 * with a DOMConstructor("item"), then element subtrees will be created for 
 * the first item (and contained item) and the second item.
 * 
 * 
 * @author andy
 *
 */
public class ElementCounter extends DefaultHandler {
  private final String matchElementName;
  private int stack = 0;
  private int count = 0;

  public ElementCounter(String matchElementName) {
    this.matchElementName = matchElementName;
  }

  @Override
  public void startElement(
      String uri,
      String localName,
      String qName,
      Attributes attributes) throws SAXException {
    if (qName.equals(matchElementName))
      stack++;
  }

  @Override
  public void endElement(
      String uri,
      String localName,
      String qName) throws SAXException {
    if (qName.equals(matchElementName))
      stack--;
    if (stack == 0)
      count++;
  }
  
  public int getCount() {
    return count;
  }
  

  public static int getCount(
      InputStream in,
      String elemName) throws ParserConfigurationException, SAXException, IOException {
    SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
    ElementCounter counter = new ElementCounter(elemName);
    try {
      parser.parse(in, counter);
    } catch (SAXParseException e) {
      System.err.println("Error parsing count.  Returning known value of " + counter.count);
    }

    return counter.count;
  }


}
