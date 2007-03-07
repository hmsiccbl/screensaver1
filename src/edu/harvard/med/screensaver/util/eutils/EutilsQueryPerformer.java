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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

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
  
  abstract protected void reportError(Exception e);
  
  protected void initializeDocumentBuilder()
  {
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    try {
      _documentBuilder = documentBuilderFactory.newDocumentBuilder();
    }
    catch (ParserConfigurationException e) {
      reportError(e);
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
    reportError(new Exception("couldnt get XML for query after " + NUM_RETRIES + " tries."));
    return null;
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
  
  
  // private instance methods
  
  private URL getUrlForUrlString(String urlString) {
    try {
      return new URL(urlString);
    }
    catch (MalformedURLException e) {
      reportError(e);
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
      log.warn("connection timed out to eUtils URL \"" + url + "\"");
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
