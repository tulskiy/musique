package org.benow.xml.sax;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
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
public abstract class DOMProducer extends DefaultHandler {
  public static class FinishedException extends SAXException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

  }

  private static DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
  private Element currElem;
  private boolean trimEmpty = false;
  protected final String matchElementName;
  private int maxObjects = -1;
  private int currObjNum;
  private String currText;
  protected boolean finished = false;;

  public DOMProducer(String matchElementName) {
    this.matchElementName = matchElementName;
  }

  /**
   * If set to true, elements which have no contained attributes or nodes (elements, text)
   * will be trimmed.
   * 
   * @param trimEmpty
   */
  public void setTrimEmpty(
      boolean trimEmpty) {
    this.trimEmpty = trimEmpty;
  }

  @Override
  public void startElement(
      String uri,
      String localName,
      String qName,
      Attributes attributes) throws SAXException {
    if (qName.equals(matchElementName)) {
      if (currElem == null) {
        currElem = newDoc().createElement(qName);
        int len = attributes.getLength();
        for (int i = 0; i < len; i++) {
          String name = attributes.getQName(i);
          String val = attributes.getValue(i);
          currElem.setAttribute(name, val);
        }
        return;
      }
    }
    if (currElem != null) {
      Element newElem = currElem.getOwnerDocument().createElement(qName);
      currElem.appendChild(newElem);
      currElem = newElem;

      int len = attributes.getLength();
      for (int i = 0; i < len; i++) {
        String name = attributes.getQName(i);
        String val = attributes.getValue(i);
        currElem.setAttribute(name, val);
      }
    }
  }

  public void preCalculate(
      InputStream in) throws ParserConfigurationException, SAXException, IOException {
    System.out.print("Precalculating...");
    System.out.flush();
    maxObjects = ElementCounter.getCount(in, matchElementName);
    System.out.println(" " + maxObjects + " objects will be produced.");
    in.close();
  }

  /**
   * Gets the number of objects which have been produced.
   * @return
   */
  public int getCurrentObjectNumber() {
    return currObjNum;
  }

  /**
   * Gets the maximum number of objects which will be produced.  Available
   * by default, unless precalculation has been disabled with disablePreCalc().
   * 
   * @return
   */
  public int getMaxNumberOfObjects() {
    return maxObjects;
  }

  /**
   * Get the percentage of iteration through the results.  Available by default
   * unless precalculation has been disabled with disablePreCalc()
   * 
   * @return
   */
  public double getPercentComplete() {
    return (((double) getCurrentObjectNumber()) / ((double) getMaxNumberOfObjects())) * 100.0;
  }

  public String getPercentCompleteString() {
    return (((int) (getPercentComplete() * 10.0)) / 10.0) + "%";
  }

  public void parse(
      InputStream in) throws ParserConfigurationException, SAXException, IOException {
    SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
    try {
      try {
        parser.parse(in, this);
      } catch (FinishedException e) {
        // done
      }
    } finally {
      in.close();
    }
  }

  @Override
  public void endElement(
      String uri,
      String localName,
      String qName) throws SAXException {
    if (currElem != null) {
      if (currText != null) {
        currElem.appendChild(currElem.getOwnerDocument().createCDATASection(currText.trim()));
        currText = null;
      }
      if (currElem.getParentNode() == null) {
        currObjNum++;
        try {
          onConstruct(currElem);
        } catch (Throwable t) {
          t.printStackTrace(System.err);
          System.exit(-1);
        }
        if (finished)
          throw new FinishedException();
        currElem = (Element) currElem.getParentNode();
      } else if (trimEmpty && currElem.getAttributes().getLength() == 0 && currElem.getChildNodes().getLength() == 0) {
        // trim empty
        Element p = (Element) currElem.getParentNode();
        p.removeChild(currElem);
        currElem = p;
      } else
        currElem = (Element) currElem.getParentNode();
    }
  }

  protected abstract void onConstruct(
      Element constructed);

  @Override
  public void characters(
      char[] ch,
      int start,
      int length) throws SAXException {
    String txt = new String(ch,
        start,
      length);
    if (currElem != null) {
      if (txt.length() > 0) {
        if (currText == null)
          currText = txt;
        else
          currText += txt;
      }
    }
  }

  private Document newDoc() {
    try {
      return factory.newDocumentBuilder().newDocument();
    } catch (ParserConfigurationException e) {
      throw new RuntimeException("Unexcpected exception",
        e);
    }
  }

}
