/*
 * Cuelib library for manipulating cue sheets.
 * Copyright (C) 2007-2008 Jan-Willem van den Broek
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package jwbroek.cuelib;

import java.io.File;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <p>Class for serializing a {@link jwbroek.cuelib.CueSheet CueSheet} to an XML representation. The serialized
 * cue sheet will conform to the following XML Schema, which closely resembles the cue sheet syntax, except for
 * the fact that it is less restrictive with respect to allowed element values. This is necessary, as the
 * {@link jwbroek.cuelib.CueSheet CueSheet} structure is more lenient than the cue sheet standard.</p>
 * 
 * {@code
<xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema"
  xmlns:tns="http://jwbroek/cuelib/2008/cuesheet/1"
  targetNamespace="http://jwbroek/cuelib/2008/cuesheet/1"
  elementFormDefault="qualified"
  attributeFormDefault="unqualified"
  >
  
  <xsd:element name="cuesheet" type="tns:cuesheet"/>
  
  <xsd:complexType name="cuesheet">
    <xsd:sequence>
      <xsd:element name="file" type="tns:file" minOccurs="0" maxOccurs="unbounded"/>
    </xsd:sequence>
    <xsd:attribute name="genre" type="xsd:string" use="optional"/>
    <xsd:attribute name="date" type="xsd:integer" use="optional"/>
    <xsd:attribute name="discid" type="xsd:string" use="optional"/>
    <xsd:attribute name="comment" type="xsd:string" use="optional"/>
    <xsd:attribute name="catalog" type="xsd:string" use="optional"/>
    <xsd:attribute name="performer" type="xsd:string" use="optional"/>
    <xsd:attribute name="title" type="xsd:string" use="optional"/>
    <xsd:attribute name="songwriter" type="xsd:string" use="optional"/>
    <xsd:attribute name="cdtextfile" type="xsd:string" use="optional"/>
  </xsd:complexType>
  
  <xsd:complexType name="file">
    <xsd:sequence>
      <xsd:element name="track" type="tns:track" minOccurs="0" maxOccurs="unbounded"/>
    </xsd:sequence>
    <xsd:attribute name="file" type="xsd:string" use="optional"/>
    <xsd:attribute name="type" type="xsd:string" use="optional"/>
  </xsd:complexType>
  
  <xsd:complexType name="track">
    <xsd:sequence>
      <xsd:element name="pregap" type="tns:position" minOccurs="0"/>
      <xsd:element name="postgap" type="tns:position" minOccurs="0"/>
      <xsd:element name="flags" type="tns:flags" minOccurs="0"/>
      <xsd:element name="index" type="tns:index" minOccurs="0" maxOccurs="unbounded"/>
    </xsd:sequence>
    <xsd:attribute name="number" type="xsd:integer" use="optional"/>
    <xsd:attribute name="type" type="xsd:string" use="optional"/>
    <xsd:attribute name="isrc" type="xsd:string" use="optional"/>
    <xsd:attribute name="performer" type="xsd:string" use="optional"/>
    <xsd:attribute name="title" type="xsd:string" use="optional"/>
    <xsd:attribute name="songwriter" type="xsd:string" use="optional"/>
  </xsd:complexType>
  
  <xsd:complexType name="position">
    <xsd:attribute name="minutes" type="xsd:integer"/>
    <xsd:attribute name="seconds" type="xsd:integer"/>
    <xsd:attribute name="frames" type="xsd:integer"/>
  </xsd:complexType>
  
  <xsd:complexType name="flags">
    <xsd:sequence>
      <xsd:element name="flag" type="xsd:string" maxOccurs="unbounded"/>
    </xsd:sequence>
  </xsd:complexType>
  
  <xsd:complexType name="index">
    <xsd:annotation>
      <xsd:documentation>
        The attributes in this type will either all be present, or all absent. Unfortunately,
        I know of no way to capture this constraint in XML Schema version 1.0.
      </xsd:documentation>
    </xsd:annotation>
    <xsd:attribute name="minutes" type="xsd:integer" use="optional"/>
    <xsd:attribute name="seconds" type="xsd:integer" use="optional"/>
    <xsd:attribute name="frames" type="xsd:integer" use="optional"/>
    <xsd:attribute name="number" type="xsd:integer" use="optional"/>
  </xsd:complexType>
  
</xsd:schema>}
 * 
 * @author jwbroek
 */
public class CueSheetToXmlSerializer
{
  /**
   * The builder for creating XML documents.
   */
  private DocumentBuilder docBuilder;
  /**
   * The namespace for the elements in the XML document.
   */
  private String namespace = "http://jwbroek/cuelib/2008/cuesheet/1";
  /**
   * The logger for this class.
   */
  private final static Logger logger = Logger.getLogger(CueSheetToXmlSerializer.class.getCanonicalName());
  
  /**
   * Create a default CueSheetToXmlSerializer.
   * @throws ParserConfigurationException 
   */
  public CueSheetToXmlSerializer() throws ParserConfigurationException
  {
    CueSheetToXmlSerializer.logger.entering(CueSheetToXmlSerializer.class.getCanonicalName(), "CueSheetToXmlSerializer()");
    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    docBuilderFactory.setNamespaceAware(true);
    this.docBuilder = docBuilderFactory.newDocumentBuilder();
    CueSheetToXmlSerializer.logger.exiting(CueSheetToXmlSerializer.class.getCanonicalName(), "CueSheetToXmlSerializer()");
  }
  
  /**
   * Write an XML representation of the cue sheet.
   * @param cueSheet The CueSheet to serialize.
   * @param writer The Writer to write the XML representation to. 
   * @throws TransformerException 
   */
  public void serializeCueSheet(final CueSheet cueSheet, final Writer writer) throws TransformerException
  {
    CueSheetToXmlSerializer.logger.entering
      ( CueSheetToXmlSerializer.class.getCanonicalName()
      , "serializeCueSheet(CueSheet,Writer)"
      , new Object[] {cueSheet, writer}
      );
    serializeCueSheet(cueSheet, new StreamResult(writer));
    CueSheetToXmlSerializer.logger.exiting
      (CueSheetToXmlSerializer.class.getCanonicalName(), "serializeCueSheet(CueSheet,Writer)");
  }
  
  /**
   * Write an XML representation of the cue sheet.
   * @param cueSheet The CueSheet to serialize.
   * @param outputStream The OutputStream to write the XML representation to. 
   * @throws TransformerException 
   */
  public void serializeCueSheet(final CueSheet cueSheet, final OutputStream outputStream)
    throws TransformerException
  {
    CueSheetToXmlSerializer.logger.entering
      ( CueSheetToXmlSerializer.class.getCanonicalName()
      , "serializeCueSheet(CueSheet,OutputStream)"
      , new Object[] {cueSheet, outputStream}
      );
    serializeCueSheet(cueSheet, new StreamResult(outputStream));
    CueSheetToXmlSerializer.logger.exiting
      (CueSheetToXmlSerializer.class.getCanonicalName(), "serializeCueSheet(CueSheet,OutputStream)");
  }

  /**
   * Write an XML representation of the cue sheet.
   * @param cueSheet The CueSheet to serialize.
   * @param file The File to write the XML representation to. 
   * @throws TransformerException 
   */
  public void serializeCueSheet(final CueSheet cueSheet, final File file) throws TransformerException
  {
    CueSheetToXmlSerializer.logger.entering
      ( CueSheetToXmlSerializer.class.getCanonicalName()
      , "serializeCueSheet(CueSheet,File)"
      , new Object[] {cueSheet, file}
      );
    serializeCueSheet(cueSheet, new StreamResult(file));
    CueSheetToXmlSerializer.logger.exiting
      (CueSheetToXmlSerializer.class.getCanonicalName(), "serializeCueSheet(CueSheet,File)");
  }

  /**
   * Write an XML representation of the cue sheet.
   * @param cueSheet The CueSheet to serialize.
   * @param result The Result to write the XML representation to. 
   * @throws TransformerException 
   */
  public void serializeCueSheet(final CueSheet cueSheet, final Result result) throws TransformerException
  {
    CueSheetToXmlSerializer.logger.entering
      ( CueSheetToXmlSerializer.class.getCanonicalName()
      , "serializeCueSheet(CueSheet,Result)"
      , new Object[] {cueSheet, result}
      );
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer identityTransformer = transformerFactory.newTransformer();
    Source cueSheetSource = new DOMSource(serializeCueSheet(cueSheet));
    identityTransformer.transform(cueSheetSource, result);
    CueSheetToXmlSerializer.logger.exiting
      (CueSheetToXmlSerializer.class.getCanonicalName(), "serializeCueSheet(CueSheet,Result)");
  }

  /**
   * Get an XML DOM tree representation of the cue sheet.
   * @param cueSheet The CueSheet to serialize.
   * @return An XML DOM tree representation of the cue sheet. 
   */
  public Document serializeCueSheet(final CueSheet cueSheet)
  {
    CueSheetToXmlSerializer.logger.entering
      (CueSheetToXmlSerializer.class.getCanonicalName(), "serializeCueSheet(CueSheet)", cueSheet);
    Document doc = docBuilder.newDocument();
    Element cueSheetElement = doc.createElementNS(this.namespace, "cuesheet");
    doc.appendChild(cueSheetElement);
    
    addAttribute(cueSheetElement, "genre", cueSheet.getGenre());
    addAttribute(cueSheetElement, "date", cueSheet.getYear());
    addAttribute(cueSheetElement, "discid", cueSheet.getDiscid());
    addAttribute(cueSheetElement, "comment", cueSheet.getComment());
    addAttribute(cueSheetElement, "catalog", cueSheet.getCatalog());
    addAttribute(cueSheetElement, "performer", cueSheet.getPerformer());
    addAttribute(cueSheetElement, "title", cueSheet.getTitle());
    addAttribute(cueSheetElement, "songwriter", cueSheet.getSongwriter());
    addAttribute(cueSheetElement, "cdtextfile", cueSheet.getCdTextFile());
    
    for (FileData fileData : cueSheet.getFileData())
    {
      serializeFileData(cueSheetElement, fileData);
    }
    
    CueSheetToXmlSerializer.logger.exiting
      (CueSheetToXmlSerializer.class.getCanonicalName(), "serializeCueSheet(CueSheet)", doc);
    return doc;
  }
  
  /**
   * Serialize the FileData.
   * @param parentElement The parent element for the FileData.
   * @param fileData The FileData to serialize.
   */
  private void serializeFileData(final Element parentElement, final FileData fileData)
  {
    CueSheetToXmlSerializer.logger.entering
      ( CueSheetToXmlSerializer.class.getCanonicalName()
      , "serializeFileData(Element,FileData)"
      , new Object[] {parentElement, fileData}
      );
    Document doc = parentElement.getOwnerDocument();
    Element fileElement = doc.createElementNS(this.namespace, "file");
    parentElement.appendChild(fileElement);
    
    addAttribute(fileElement, "file", fileData.getFile());
    addAttribute(fileElement, "type", fileData.getFileType());
    
    for (TrackData trackData : fileData.getTrackData())
    {
      serializeTrackData(fileElement, trackData);
    }
    CueSheetToXmlSerializer.logger.exiting
      (CueSheetToXmlSerializer.class.getCanonicalName(), "serializeFileData(Element,FileData)");
  }

  /**
   * Serialize the TrackData.
   * @param parentElement The parent element for the TrackData.
   * @param trackData The TrackData to serialize.
   */
  private void serializeTrackData(final Element parentElement, final TrackData trackData)
  {
    CueSheetToXmlSerializer.logger.entering
      ( CueSheetToXmlSerializer.class.getCanonicalName()
      , "serializeTrackData(Element,TrackData)"
      , new Object[] {parentElement, trackData}
      );
    Document doc = parentElement.getOwnerDocument();
    Element trackElement = doc.createElementNS(this.namespace, "track");
    parentElement.appendChild(trackElement);
    
    addAttribute(trackElement, "number", trackData.getNumber());
    addAttribute(trackElement, "type", trackData.getDataType());
    
    addAttribute(trackElement, "isrc", trackData.getIsrcCode());
    addAttribute(trackElement, "performer", trackData.getPerformer());
    addAttribute(trackElement, "title", trackData.getTitle());
    addAttribute(trackElement, "songwriter", trackData.getSongwriter());
    
    addElement(trackElement, "pregap", trackData.getPregap());
    addElement(trackElement, "postgap", trackData.getPostgap());
    
    if (trackData.getFlags().size() > 0)
    {
      serializeFlags(trackElement, trackData.getFlags());
    }
    
    for (Index index : trackData.getIndices())
    {
      serializeIndex(trackElement, index);
    }
    CueSheetToXmlSerializer.logger.exiting
      (CueSheetToXmlSerializer.class.getCanonicalName(), "serializeTrackData(Element,TrackData)");
  }
  
  /**
   * Serialize the flags.
   * @param parentElement The parent element for the TrackData.
   * @param flags The flags to serialize.
   */
  private void serializeFlags(final Element parentElement, final Set<String> flags)
  {
    CueSheetToXmlSerializer.logger.entering
      ( CueSheetToXmlSerializer.class.getCanonicalName()
      , "serializeFlags(Element,Set<String>)"
      , new Object[] {parentElement, flags}
      );
    Document doc = parentElement.getOwnerDocument();
    Element flagsElement = doc.createElementNS(this.namespace, "flags");
    parentElement.appendChild(flagsElement);

    for (String flag : flags)
    {
      addElement(flagsElement, "flag", flag);
    }
    CueSheetToXmlSerializer.logger.exiting
      (CueSheetToXmlSerializer.class.getCanonicalName(), "serializeFlags(Element,Set<String>)");
  }
  
  /**
   * Serialize the index.
   * @param parentElement The parent element for the TrackData.
   * @param index The Index to serialize.
   */
  private void serializeIndex(final Element parentElement, final Index index)
  {
    CueSheetToXmlSerializer.logger.entering
      ( CueSheetToXmlSerializer.class.getCanonicalName()
      , "serializeIndex(Element,Index)"
      , new Object[] {parentElement, index}
      );
    Element indexElement = addElement(parentElement, "index", index.getPosition(), true);
    
    addAttribute(indexElement, "number", index.getNumber());
    CueSheetToXmlSerializer.logger.exiting
      (CueSheetToXmlSerializer.class.getCanonicalName(), "serializeIndex(Element,Index)");
  }
  
  /**
   * Add a position element. The element is only added if the position is != null.
   * In the latter case, the attributes with position data will still only be added if present.
   * @param parentElement The parent element for the position element.
   * @param elementName The name for the position element to add.
   * @param value The value to add.
   * @return The element that was created, or null if no element was created.
   */
  private Element addElement  ( final Element parentElement
                              , final String elementName
                              , final Position position
                              )
  {
    CueSheetToXmlSerializer.logger.entering
      ( CueSheetToXmlSerializer.class.getCanonicalName()
      , "addElement(Element,String,Position)"
      , new Object[] {parentElement, elementName, position}
      );
    Element result = addElement(parentElement, elementName, position, false);
    CueSheetToXmlSerializer.logger.exiting
      (CueSheetToXmlSerializer.class.getCanonicalName(), "addElement(Element,String,Position)", result);
    return result;
  }

  /**
   * Add a position element. The element is only added if the position is != null, or if creation is forced.
   * In the latter case, the attributes with position data will still only be added if present.
   * @param parentElement The parent element for the position element.
   * @param elementName The name for the position element to add.
   * @param forceElement Force creation of the element, but not necessarily of the attributes.
   * @param value The value to add.
   * @return The element that was created, or null if no element was created.
   */
  private Element addElement  ( final Element parentElement
                              , final String elementName
                              , final Position position
                              , final boolean forceElement
                              )
  {
    CueSheetToXmlSerializer.logger.entering
      ( CueSheetToXmlSerializer.class.getCanonicalName()
      , "addElement(Element,String,Position,boolean)"
      , new Object[] {parentElement, elementName, position, forceElement}
      );
    Element positionElement = null;
    
    if (position != null || forceElement)
    {
      positionElement = parentElement.getOwnerDocument().createElementNS(this.namespace, elementName);
      parentElement.appendChild(positionElement);
      
      if (position != null)
      {
        positionElement.setAttribute("minutes", ""+position.getMinutes());
        positionElement.setAttribute("seconds", ""+position.getSeconds());
        positionElement.setAttribute("frames", ""+position.getFrames());
      }
    }
    
    CueSheetToXmlSerializer.logger.exiting
      ( CueSheetToXmlSerializer.class.getCanonicalName()
      , "addElement(Element,String,Position,boolean)"
      , positionElement
      );
    return positionElement;
  }

  /**
   * Add an element to the document. The element is only added if the value is != null.
   * @param cueBuilder
   * @param parentElement The parent element for this element.
   * @param elementName The name for the element.
   * @param value The value for the element.
   * @return The element that was created, or null if no element was created.
   */
  private Element addElement(final Element parentElement, final String elementName, final String value)
  {
    CueSheetToXmlSerializer.logger.entering
      ( CueSheetToXmlSerializer.class.getCanonicalName()
      , "addElement(Element,String,String)"
      , new Object[] {parentElement, elementName, value}
      );
    Element newElement = null;
    
    if (value != null)
    {
      newElement = parentElement.getOwnerDocument().createElementNS(this.namespace, elementName);
      newElement.appendChild(parentElement.getOwnerDocument().createTextNode(value));
      parentElement.appendChild(newElement);
    }
    
    CueSheetToXmlSerializer.logger.exiting
      ( CueSheetToXmlSerializer.class.getCanonicalName()
      , "addElement(Element,String,String)"
      , newElement
      );
    return newElement;
  }
  
  /**
   * Add an element to the document. The element is only added if the value is > -1.
   * @param cueBuilder
   * @param parentElement The parent element for this element.
   * @param elementName The name for the element.
   * @param value The value for the element.
   * @return The element that was created, or null if no element was created.
   */
  private Element addElement(final Element parentElement, final String elementName, final int value)
  {
    CueSheetToXmlSerializer.logger.entering
      ( CueSheetToXmlSerializer.class.getCanonicalName()
      , "addElement(Element,String,int)"
      , new Object[] {parentElement, elementName, value}
      );
    Element newElement = null;
    
    if (value > -1)
    {
      newElement = parentElement.getOwnerDocument().createElementNS(this.namespace, elementName);
      newElement.appendChild(parentElement.getOwnerDocument().createTextNode("" + value));
      parentElement.appendChild(newElement);
    }
    
    CueSheetToXmlSerializer.logger.exiting
      (CueSheetToXmlSerializer.class.getCanonicalName(), "addElement(Element,String,int)", newElement);
    return newElement;
  }

  /**
   * Add an attribute to the document. The attribute is only added if the value is != null.
   * @param cueBuilder
   * @param parentElement The parent element for this attribute.
   * @param attributeName The name for the attribute.
   * @param value The value for the attribute.
   */
  private void addAttribute(final Element parentElement, final String attributeName, final String value)
  {
    CueSheetToXmlSerializer.logger.entering
      ( CueSheetToXmlSerializer.class.getCanonicalName()
      , "addAttribute(Element,String,String)"
      , new Object[] {parentElement, attributeName, value}
      );
    if (value != null)
    {
      parentElement.setAttribute(attributeName, value);
    }
    CueSheetToXmlSerializer.logger.exiting
      (CueSheetToXmlSerializer.class.getCanonicalName(), "addAttribute(Element,String,String)");
  }
  
  /**
   * Add an attribute to the document. The attribute is only added if the value is > -1.
   * @param cueBuilder
   * @param parentElement The parent element for this attribute.
   * @param attributeName The name for the attribute.
   * @param value The value for the attribute.
   */
  private void addAttribute(final Element parentElement, final String attributeName, final int value)
  {
    CueSheetToXmlSerializer.logger.entering
      ( CueSheetToXmlSerializer.class.getCanonicalName()
      , "addAttribute(Element,String,int)"
      , new Object[] {parentElement, attributeName, value}
      );
    if (value > -1)
    {
      parentElement.setAttribute(attributeName, "" + value);
    }
    CueSheetToXmlSerializer.logger.exiting
      (CueSheetToXmlSerializer.class.getCanonicalName(), "addAttribute(Element,String,int)");
  }
}
