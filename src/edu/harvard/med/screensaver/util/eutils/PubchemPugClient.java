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
import java.net.URL;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

/**
 * An abstract class providing utility methods for querying PubChem via the
 * PUG (Power Users Gateway) interface.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public abstract class PubchemPugClient extends EutilsUtils
{
  // static members

  private static final Logger log = Logger.getLogger(PubchemPugClient.class);
  protected static final String PUG_URL = "http://pubchem.ncbi.nlm.nih.gov/pug/pug.cgi";
  protected static final int NUM_RETRIES = 5;
  protected static final int CONNECT_TIMEOUT = 5000;


  // protracted instance methods
  
  abstract protected void reportError(String errorMessage);
  
  protected PubchemPugClient()
  {
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    try {
      _documentBuilder = documentBuilderFactory.newDocumentBuilder();
    }
    catch (ParserConfigurationException e) {
      reportError(e.getMessage());
    }
  }
  
  protected Document getXMLForPugQuery(Document query)
  {
    URL url = getUrlForUrlString(PUG_URL);
    for (int i = 0; i < NUM_RETRIES; i ++) {
      try {
        return getXMLForPugQuery0(url, query);
      }
      catch (EutilsConnectionException e) {
      }
    }
    reportError("couldnt get XML for query after " + NUM_RETRIES + " tries.");
    return null;
  }
  
  
  // private instance methods

  private Document getXMLForPugQuery0(URL url, Document query)
  throws EutilsConnectionException
  {
    InputStream esearchContent = getResponseContent(url, query);
    assert(esearchContent != null);
    return getDocumentFromInputStream(esearchContent);
  }

  private InputStream getResponseContent(URL url, Document query)
  throws EutilsConnectionException
  {
    try {
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setConnectTimeout(CONNECT_TIMEOUT);
      connection.setReadTimeout(CONNECT_TIMEOUT);
      connection.setDoOutput(true);
      printDocumentToOutputStream(query, connection.getOutputStream());
      connection.connect();
      return connection.getInputStream();
    }
    catch (Exception e) {
      log.warn("failed to connect to PUG URL \"" + url + "\"");
      throw new EutilsConnectionException();
    }
  }
}

