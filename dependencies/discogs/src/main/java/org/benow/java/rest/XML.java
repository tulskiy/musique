package org.benow.java.rest;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Select utilities from the benow-xml api
 * {link http://benow.ca/projects/XML/} 
 * @author andy
 *
 */
public class XML {
  private static DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

  public static Document loadDocument(
      InputStream in) throws IOException {
    StringWriter writer = new StringWriter();
    InputStreamReader reader = new InputStreamReader(in);
    copy(reader, writer);
    // http://www.velocityreviews.com/forums/t143346-xml-and-invalid-byte-utf-8-a.html
    in = new ByteArrayInputStream(writer.toString().getBytes("UTF-8"));

    String xml = writer.toString();
    return loadDocument(xml);
  }

  public static Document loadDocument(
      String xml) throws IOException {
    try {
      factory.setNamespaceAware(true);
      DocumentBuilder builder;
      try {
        builder = factory.newDocumentBuilder();
      } catch (ParserConfigurationException e) {
        e.printStackTrace();
        throw new IOException("Unexpected error: " + e.getMessage());
      }
      return builder.parse(new ByteArrayInputStream(xml.getBytes("utf8")));
    } catch (SAXException sxe) {
      System.err.println("Error parsing:\n"+xml);
      // Error generated during parsing
      Exception x = sxe;
      if (sxe.getException() != null)
        x = sxe.getException();
      x.printStackTrace();
      throw new IOException("Error parsing: " + x.getMessage());
    }
  }

  private static void copy(
      InputStreamReader reader,
      StringWriter writer) throws IOException {
    char[] buff = new char[512];
    int read = reader.read(buff);
    while (read > 0) {
      writer.write(buff, 0, read);
      read = reader.read(buff);
    }
  }

  /**
   * Get node value by path name, in limited xpath format.  For example
   * <ul>
   * <li>getByPath(curr,"@attr"); - gets attribute attr value from given node</li>
   * <li>getByPath(curr,"/some/elem"); - gets element value for elem element within some element off root</li>
   * < li>getByPath(curr,"/some/@attr"); - gets attribute value for attr attribute of some element</li>
   * <li>getByPath(curr,"some"); - gets attribute value for some element</li>
  * </ul>
  *  // and other xpath operators are not supported.
  *  
   * @param releaseElem
   * @param path
   * @return object at path, a string, or node of some sort
   */
  public static Object getByPath(
      Element releaseElem,
      String path) {
    while (path.indexOf('/') == 0)
      path = path.substring(1, path.length());

    if (path.substring(0, 1).equals("@")) {
      String attrName = path.substring(1, path.length());
      Node attr = _getAttributeNode(releaseElem, attrName);
      if (attr != null)
        return attr.getNodeValue();
      else
        return null;
    }
    Node node = getNodeByPath(releaseElem, path);
    if (node == null)
      return null;

    if (node.getNodeType() == Node.ELEMENT_NODE) { // is element, get text node
      if (node.getChildNodes().getLength() == 0)
        return null;
      if (node.getChildNodes().getLength() == 1)
        return node.getFirstChild().getNodeValue();
      return node;
    } else
      // is attribute, get value
      return node.getNodeValue();
  }

  public static Node getNodeByPath(
      Node curr,
      String path) {
    if (path.substring(0, 2).equals("@/")) {
      String attrName = path.substring(2, path.length());
      return _getAttributeNode(curr, attrName);
    }

    if (path.substring(0, 1).equals("@")) {
      String attrName = path.substring(1, path.length());
      return _getAttributeNode(curr, attrName);
    }

    int pos = path.indexOf("/");
    int pos2 = path.indexOf("@");

    if (pos == -1 && pos2 == -1)
      return getChildElement(curr, path);

    String nodeName;
    String nextPath;
    if (pos2 != -1 && (pos > pos2 || pos == -1)) {
      nodeName = path.substring(0, pos2);
      nextPath = path.substring(pos2);
    } else {
      nodeName = path.substring(0, pos);
      nextPath = path.substring(pos + 1);
    }
    Node node = getChildElement(curr, nodeName);
    if (node == null) // node not found
      return null;

    return getNodeByPath(node, nextPath);
  }

  private static Node getChildElement(
      Node node,
      String name) {
    if (node == null)
      return null;
    NodeList nodes = node.getChildNodes();
    if (nodes != null) {
      for (int i = 0; i < nodes.getLength(); i++) {
        Node curr = nodes.item(i);
        if (curr.getNodeType() == Node.ELEMENT_NODE && curr.getNodeName().equals(name)) {
          return curr;
        }
      }
    }
    return null;
  }

  private static Node _getAttributeNode(
      Node node,
      String name) {
    if (node == null || node.getAttributes().getLength() == 0)
      return null;
    NamedNodeMap attrs = node.getAttributes();
    for (int i = 0; i < attrs.getLength(); i++) {
      Node curr = attrs.item(i);
      // alert("curr: name: "+curr.nodeName+" val:"+curr.nodeValue);
      if (curr.getNodeName().equals(name)) {
        // alert("found: "+name);
        return curr;
      }
    }
    return null;
  }

  public static String elementToString(
      Element toShow) {
    return elementToString(toShow, "");
  }

  private static String elementToString(
      Element curr,
      String indent) {
    String result=indent+"<"+curr.getNodeName();
    if (curr.hasAttributes()) {
      NamedNodeMap nm = curr.getAttributes();
      for (int i=0;i<nm.getLength();i++) {
        result+=" "+nm.item(i).getNodeName()+"=\""+nm.item(i).getNodeValue()+"\"";
      }
    }
    if (curr.hasChildNodes()) {
      NodeList cn = curr.getChildNodes();
      if (cn.getLength()==1) {
        Node currN=cn.item(0);
        if (currN.getNodeType()==Node.TEXT_NODE) { 
          result += "><![CDATA[" + currN.getNodeValue() + "]]>";
          result += "</" + curr.getNodeName() + ">";
          return result;
        } else if (currN.getNodeType() == Node.CDATA_SECTION_NODE) {
          result += "><![CDATA[" + currN.getNodeValue() + "]]>";
          result += "</" + curr.getNodeName() + ">";
          return result;
        }
      }
      result+=">";
      for (int i=0;i<cn.getLength();i++) {
        Node currN=cn.item(i);
        if (currN.getNodeType() == Node.TEXT_NODE)
          result += "<![CDATA[" + currN.getNodeValue() + "]]>";
        if (currN.getNodeType()==Node.ELEMENT_NODE)
          result += "\n" + elementToString((Element) currN, indent + "  ");
        if (currN.getNodeType()==Node.COMMENT_NODE)
          result+="\n<!--"+currN.getNodeValue()+"-->";
      }
      result += "\n" + indent + "</" + curr.getNodeName() + ">";
    } else {
      result+="/>";
    }
    return result;
      
  }

  /**
   * Get immediate child elements with the given name
   * (whereas getElementsByTagName will get any named elems,
   * not just immediate children)
   * 
   * @param element
   * @param childElemName
   * @return elements with given name
   */
  public static List<Element> getChildElements(
      Element element,
      String childElemName) {
    List<Element> result = new ArrayList<Element>();

    NodeList children = element.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node curr = children.item(i);
      if (curr.getNodeType() == Node.ELEMENT_NODE) {
        if (curr.getNodeName().equals(childElemName))
          result.add((Element) curr);
      }
    }

    return result;
  }

  public static void saveDocument(
      Document doc,
      File toSaveTo) throws IOException {
    FileOutputStream fos = new FileOutputStream(toSaveTo);
    try {
      String xmlStr = elementToString(doc.getDocumentElement());
      fos.write(xmlStr.getBytes());
    } finally {
      fos.flush();
      fos.close();
    }
  }

  public static String documentToString(
      Document ownerDocument) {
    String enc = "UTF-8";
    if (ownerDocument.getXmlEncoding() == null) {
      if (ownerDocument.getInputEncoding() != null)
        enc = ownerDocument.getInputEncoding();
    } else
      enc = ownerDocument.getXmlEncoding();
    String xml = "<?xml version=\"1.0\" encoding=\"" + enc + "\"?>";
    xml += "\n" + elementToString(ownerDocument.getDocumentElement());
    return xml;
  }

}
