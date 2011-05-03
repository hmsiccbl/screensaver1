// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.util.eutils;


import java.io.IOException;
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

abstract public class EutilsUtils
{
  private static final Logger log = Logger.getLogger(EutilsUtils.class);
  protected static final int NUM_RETRIES = 5;
  protected static final int CONNECT_TIMEOUT = 5000;
  protected static final String EUTILS_BASE_URL = "http://www.ncbi.nlm.nih.gov/entrez/eutils/";
  private static final String EUTILS_BASE_PARAMS =
    "?retmode=xml" +
    "&tool=screensaver" +
    "&email=screensaver-feedback%40hms.harvard.edu";

  protected static class EutilsConnectionException extends Exception {
    private static final long serialVersionUID = 1L;
  };

  protected DocumentBuilder _documentBuilder;

  protected void initializeDocumentBuilder() {
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    try {
      _documentBuilder = documentBuilderFactory.newDocumentBuilder();
    }
    catch (ParserConfigurationException e) {
      throw new RuntimeException(new EutilsException(e));
    }
  }

  /**
   * Find the element node in the node list that has an attribute named "Name" with the
   * specified attribute value. Return the text content of that element node.
   * @param nodes the list of element nodes
   * @param attributeValue the attribute value
   * @return the text content of the specified element node. Return null if the specified
   * element node is not found.
   * @throws EutilsException 
   */
  protected String getNamedItemFromNodeList(NodeList nodes, String attributeValue) throws EutilsException
  {
    return getNamedItemFromNodeList(nodes, attributeValue, true);
  }

  /**
   * Find the element node in the node list that has an attribute named "Name" with the
   * specified attribute value. Return the text content of that element node.
   * @param nodes the list of element nodes
   * @param attributeValue the attribute value
   * @return the text content of the specified element node. Return null if the specified
   * element node is not found.
   * @throws EutilsException 
   */
  protected String getNamedItemFromNodeList(NodeList nodes, String attributeValue, boolean reportError) throws EutilsException
  {
    for (int i = 0; i < nodes.getLength(); i++) {
      Node node = nodes.item(i);
      if (node.getAttributes().getNamedItem("Name").getNodeValue().equals(attributeValue)) {
        return getTextContent(node);
      }
    }
    throw new EutilsException("eUtils results did not include a \"" + attributeValue + "\" in the XML response");
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

  protected URL getUrlForUrlString(String urlString) throws EutilsException
  {
    try {
      return new URL(urlString);
    }
    catch (MalformedURLException e) {
      throw new EutilsException(e);
    }
  }

  /**
   * Get the XML document response for an eUtils query.
   * @param fcgi one of "esummary.fcgi", "esearch.fcgi"
   * @param queryParams any extra query params, starting with '&'. needs to include param for
   * "db".
   * @return the xml document. return null if any errors were encountered.
   * @throws EutilsException 
   */
  protected Document getXMLForEutilsQuery(String fcgi, String queryParams) throws EutilsException
  {
    String urlString = EUTILS_BASE_URL + fcgi + EUTILS_BASE_PARAMS + queryParams;
    URL url = getUrlForUrlString(urlString);
    for (int i = 0; i < NUM_RETRIES; i ++) {
      try {
        return getXMLForEutilsQuery0(url);
      }
      catch (EutilsConnectionException e) {
      }
    }
    throw new EutilsException("couldnt get XML for query after " + NUM_RETRIES + " tries.");
  }

  private Document getXMLForEutilsQuery0(URL url) throws EutilsConnectionException
  {
    InputStream esearchContent = getResponseContent(url);
    assert(esearchContent != null);
    //    printInputStreamToOutputStream(esearchContent, System.out);
    //    if (true) return null;
    return getDocumentFromInputStream(esearchContent);
  }

  protected InputStream getResponseContent(URL url) throws EutilsConnectionException
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

  protected Document getDocumentFromInputStream(InputStream epostContent) throws EutilsConnectionException
  {
    try {
      return _documentBuilder.parse(epostContent);
    }
    catch (Exception e) {
      e.printStackTrace();
      log.warn("error parsing eUtils response content: " + e.getMessage());
      throw new EutilsConnectionException();
    }
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

      // throw in a newline for output formatting 
      outputStream.write('\n');
      outputStream.flush();
    }
    catch (Exception e) {
    }
  }

  /**
   * Debugging helper method.
   * @param inputStream
   * @param outputStream
   */
  protected void printInputStreamToOutputStream(InputStream inputStream, OutputStream outputStream)
  {
    try {
      for (int ch = inputStream.read(); ch != -1; ch = inputStream.read()) {
        outputStream.write(ch);
      }
    }
    catch (IOException e) {
    }
  }
}
