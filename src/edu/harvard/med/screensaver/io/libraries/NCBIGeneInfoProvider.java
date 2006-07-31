// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.harvard.med.screensaver.io.workbook.Cell;
import edu.harvard.med.screensaver.io.workbook.ParseErrorManager;


/**
 */
public class NCBIGeneInfoProvider
{
  
  // static fields
  
  private static final Logger log = Logger.getLogger(NCBIGeneInfoProvider.class);
  private static final String EFETCH_URL_PREFIX =
    "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?db=gene&retmode=xml&id=";

  
  // instance fields
  
  private ParseErrorManager _errorManager;
  private DocumentBuilder _documentBuilder;

  
  // public constructor and instance methods
  
  public NCBIGeneInfoProvider(ParseErrorManager errorManager)
  {
    _errorManager = errorManager;
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    try {
      _documentBuilder = documentBuilderFactory.newDocumentBuilder();
    }
    catch (ParserConfigurationException e) {
      _errorManager.addError(
        "unable to initialize the XML document builder: " + e.getMessage());
    }
  }
  
  public NCBIGeneInfo getGeneInfoForEntrezgeneId(Integer entrezGeneId, Cell cell)
  {
    if (_documentBuilder == null) {
      return null;
    }
    String efetchURL = EFETCH_URL_PREFIX + entrezGeneId;
    InputStream efetchContent = getContent(efetchURL, entrezGeneId, cell);
    if (efetchContent == null) {
      return null;
    }
    Document efetchDocument = getEfetchDocument(efetchContent, entrezGeneId, cell);
    if (efetchDocument == null) {
      return null;
    }
    String geneName = getGeneNameFromDocument(efetchDocument);
    String speciesName = getSpeciesNameFromDocument(efetchDocument);
    return new NCBIGeneInfo(geneName, speciesName);
  }

  private InputStream getContent(String url, Integer entrezGeneId, Cell cell)
  {
    try {
      HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
      connection.connect();
      return connection.getInputStream();
    }
    catch (Exception e) {
      _errorManager.addError(
        "unable to get URL connection to NCBI for " + entrezGeneId + ": " + e.getMessage(),
        cell);
      return null;
    }
  }

  /**
   * @param efetchContent
   * @return
   */
  private Document getEfetchDocument(
    InputStream efetchContent,
    Integer entrezGeneId,
    Cell cell)
  {
    try {
      return _documentBuilder.parse(efetchContent);
    }
    catch (Exception e) {
      _errorManager.addError(
        "unable to get content from NCBI for " + entrezGeneId + ": " + e.getMessage(),
        cell);
      return null;
    }
  }
  
  private String getGeneNameFromDocument(Document efetchDocument)
  {
    NodeList nodes = efetchDocument.getElementsByTagName("Item");
    for (int i = 0; i < nodes.getLength(); i++) {
      Node node = nodes.item(i);
      if (node.getAttributes().getNamedItem("Name").getNodeValue().equals("Description")) {
        return node.getTextContent();
      }
    }
    return null;
  }

  private String getSpeciesNameFromDocument(Document efetchDocument)
  {
    NodeList nodes = efetchDocument.getElementsByTagName("Item");
    for (int i = 0; i < nodes.getLength(); i++) {
      Node node = nodes.item(i);
      if (node.getAttributes().getNamedItem("Name").getNodeValue().equals("Orgname")) {
        return node.getTextContent();
      }
    }
    return null;
  }
}
