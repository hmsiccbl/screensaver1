// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.util.eutils;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class EutilsQueryPerformer
{

  // static fields
  
  private static Logger log = Logger.getLogger(EutilsQueryPerformer.class);
  protected static final String EUTILS_BASE_URL = "http://www.ncbi.nlm.nih.gov/entrez/eutils/";
  private static final String EUTILS_BASE_PARAMS =
    "?retmode=xml&usehistory=n&tool=screensaver" +
    "&email=%7Bjohn_sullivan%2Candrew_tolopko%7D%40hms.harvard.edu";
  protected static final int NUM_RETRIES = 5;
  protected static final int CONNECT_TIMEOUT = 5000;
  
  
  // private instance classes
  
  private class EutilsConnectionException extends Exception {
    private static final long serialVersionUID = 1L;
  };
  
  
  // protected instance fields
  
  protected DocumentBuilder _documentBuilder;

  
  // protected instance methods
  
  abstract protected void reportError(String errorMessage);
  
  protected void initializeDocumentBuilder()
  {
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    try {
      _documentBuilder = documentBuilderFactory.newDocumentBuilder();
    }
    catch (ParserConfigurationException e) {
      reportError(e.getMessage());
    }
  }
  
  /**
   * Get the XML document response for an eUtils query.
   * @param fcgi one of "esummary.fcgi", "esearch.fcgi"
   * @param queryParams any extra query params, starting with '&'. needs to include param for
   * "db".
   * @return the xml document. return null if any errors were ecountered.
   */
  protected Document getXMLForQuery(String fcgi, String queryParams)
  {
    String urlString = EUTILS_BASE_URL + fcgi + EUTILS_BASE_PARAMS + queryParams;
    URL url = getUrlForUrlString(urlString);
    for (int i = 0; i < NUM_RETRIES; i ++) {
      try {
        return getXMLForQuery0(url);
      }
      catch (EutilsConnectionException e) {
      }
    }
    reportError("couldnt get XML for query after " + NUM_RETRIES + " tries.");
    return null;
  }

  /**
   * Find the element node in the node list that has an attribute named "Name" with the
   * specified attribute value. Return the text content of that element node. 
   * @param nodes the list of element nodes
   * @param attributeValue the attribute value
   * @return the text content of the specified element node. Return null if the specified
   * element node is not found. 
   */
  protected String getNamedItemFromNodeList(NodeList nodes, String attributeValue) {
    for (int i = 0; i < nodes.getLength(); i++) {
      Node node = nodes.item(i);
      if (node.getAttributes().getNamedItem("Name").getNodeValue().equals(attributeValue)) {
        return getTextContent(node);
      }
    }
    reportError("eUtils results did not include a \"" + attributeValue + "\" in the XML response");
    return null;
  }

  /**
   * Find all the element node in the node list that has an attribute named "Name" with the
   * specified attribute value. Return a list of the text content of those element node. 
   * @param nodes the list of element nodes
   * @param attributeValue the attribute value
   * @return the a list of the text content of the specified element nodes
   */
  protected List<String> getNamedItemsFromNodeList(NodeList nodes, String attributeValue) {
    List<String> namedItems = new ArrayList<String>();
    for (int i = 0; i < nodes.getLength(); i++) {
      Node node = nodes.item(i);
      if (node.getAttributes().getNamedItem("Name").getNodeValue().equals(attributeValue)) {
        namedItems.add(getTextContent(node));
      }
    }
    return namedItems;
  }
  
  /**
   * Recursively traverse the nodal structure of the node, accumulating the accumulate
   * parts of the text content of the node and all its children.
   * @param node the node to traversalate
   * @return the accumulative recursive text content of the traversalated node
   */
  protected String getTextContent(Node node)
  {
    if (node.getNodeType() == Node.TEXT_NODE) {
      return node.getNodeValue();
    }
    String textContent = "";
    for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
      textContent += getTextContent(child);
    }
    return textContent;
  }
  
  /**
   * Debugging helper method.
   * @param document
   * @param outputStream
   */
  protected void printDocumentToOutputStream(Document document, OutputStream outputStream)
  {
    try {
      TransformerFactory tFactory = TransformerFactory.newInstance();
      Transformer transformer = tFactory.newTransformer();

      DOMSource source = new DOMSource(document);
      StreamResult result = new StreamResult(outputStream);
      transformer.transform(source, result);
    }
    catch (Exception e) {
    }
  }
  
  
  // private instance methods
  
  private URL getUrlForUrlString(String urlString) {
    try {
      return new URL(urlString);
    }
    catch (MalformedURLException e) {
      reportError(e.getMessage());
    }
    return null;
  }
  
  private Document getXMLForQuery0(URL url) throws EutilsConnectionException
  {
    InputStream esearchContent = getResponseContent(url);
    assert(esearchContent != null);
    return getDocumentFromInputStream(esearchContent);
  }

  private InputStream getResponseContent(URL url) throws EutilsConnectionException
  {
    try {
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setConnectTimeout(CONNECT_TIMEOUT);
      connection.setReadTimeout(CONNECT_TIMEOUT);
      connection.connect();
      return connection.getInputStream();
    }
    catch (Exception e) {
      log.warn("failed to connect to eUtils URL \"" + url + "\"");
      throw new EutilsConnectionException();
    }
  }
  
  private Document getDocumentFromInputStream(InputStream epostContent)
  throws EutilsConnectionException
  {
    try {
      return _documentBuilder.parse(epostContent);
    }
    catch (Exception e) {
      log.warn("error parsing eUtils response content: " + e.getMessage());
      throw new EutilsConnectionException();
    }
  }
}
