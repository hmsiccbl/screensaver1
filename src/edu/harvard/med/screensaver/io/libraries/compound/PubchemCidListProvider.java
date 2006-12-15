// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries.compound;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * Queries PubChem to provide a list of PubChem CIDs for a given SMILES string.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class PubchemCidListProvider
{
  // static fields

  private static final Logger log = Logger.getLogger(PubchemCidListProvider.class);
  private static final String EUTILS_ROOT = "http://www.ncbi.nlm.nih.gov/entrez/eutils";
  private static final String ESEARCH_URL = EUTILS_ROOT + "/esearch.fcgi";
  private static final String EFETCH_URL = EUTILS_ROOT + "/efetch.fcgi";
  private static final int NUM_RETRIES = 3;

  
  // static methods
  
  public static void main(String [] args)
  {
    OpenBabelClient openBabelClient = new OpenBabelClient();
    PubchemCidListProvider pubchemCidListProvider = new PubchemCidListProvider();
    for (String smiles : new String [] {
      "CC",
      "CCC",
      "CC(=O)C",
      //"O=C1CCCC=2OC(=O)C(=CC1=2)NC(=O)c3ccccc3",
      //"COc1ccc(cc1)N3N=C(C(=O)Oc2ccccc2)c4ccccc4(C3(=O))",
      //"CON=CNC(=O)c1cc(ccc1(OCC(F)(F)F))OCC(F)(F)F",
    }) {
      log.info("SMILES = " + smiles);
      String inchi = openBabelClient.convertSmilesToInchi(smiles);
      log.info("InChI  = " + inchi);
      List<String> pubchemCids = pubchemCidListProvider.getPubchemCidListForInchi(inchi);
      for (String pubchemCid : pubchemCids) {
        log.info("pubchemCid = " + pubchemCid);
      }
    }
  }
  
  
  // public instance fields
  
  private DocumentBuilder _documentBuilder;
  private class PubChemConnectionException extends Exception {
    private static final long serialVersionUID = 1L;
  };
  
  
  // public constructor
  
  public PubchemCidListProvider()
  {
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    try {
      _documentBuilder = documentBuilderFactory.newDocumentBuilder();
    }
    catch (ParserConfigurationException e) {
      log.error("unable to initialize the XML document builder", e);
    }
  }
  
  
  // public instance methods

  public List<String> getPubchemCidListForInchi(String inchi)
  {
    for (int i = 0; i < NUM_RETRIES; i ++) {
      try {
        return getPubChemCidListForInchi0(inchi);        
      }
      catch (PubChemConnectionException e) {
      }
    }
    log.error("couldnt get PubChem CIDs for InChI after " + NUM_RETRIES + " tries.");
    throw new NullPointerException();
  }


  private List<String> getPubChemCidListForInchi0(String inchi)
  throws PubChemConnectionException {
    List<String> pubchemCids = new ArrayList<String>();
    InputStream esearchContent = getEsearchContent(inchi);
    if (esearchContent == null) {
      return pubchemCids;
    }
    
    Document esearchDocument = getDocumentFromInputStream(esearchContent);
    String queryKey = esearchDocument.getElementsByTagName("QueryKey").item(0).getTextContent();
    String webEnv = esearchDocument.getElementsByTagName("WebEnv").item(0).getTextContent();
    
    InputStream efetchContent = getEfetchContent(queryKey, webEnv, inchi);
    if (efetchContent == null) {
      return pubchemCids;
    }
    
    //dumpInputStreamToStdout(efetchContent);
    //if (true) return pubchemCids;
    
    Document efetchDocument = getDocumentFromInputStream(efetchContent);
    NodeList efetchIds = efetchDocument.getElementsByTagName("Id");
    for (int i = 0; i < efetchIds.getLength(); i ++) {
      String efetchId = efetchIds.item(i).getTextContent();
      pubchemCids.add(efetchId);
    }
    
    return pubchemCids;
  }

  private InputStream getEsearchContent(String inchi)
  throws PubChemConnectionException
  {
    try {
      URL url = new URL(
        ESEARCH_URL + "?db=pccompound&usehistory=y&tool=screensaver&email=" +
        URLEncoder.encode("{john_sullivan,andrew_tolopko}@hms.harvard.edu", "UTF-8") +
        "&term=\"" +
        URLEncoder.encode(inchi, "UTF-8") +
        "\"[inchi]"
        );
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.connect();
      return connection.getInputStream();
    }
    catch (Exception e) {
      log.warn(
        "couldnt get eSearch content from NCBI for inchi " + inchi + ": " +
        e.getMessage());
      throw new PubChemConnectionException();
    }
  }

  private Document getDocumentFromInputStream(InputStream epostContent)
  throws PubChemConnectionException
  {
    try {
      return _documentBuilder.parse(epostContent);
    }
    catch (Exception e) {
      log.warn("unable to get content from NCBI: " + e.getMessage());
      throw new PubChemConnectionException();
    }
  }

  private InputStream getEfetchContent(String queryKey, String webEnv, String inchi)
  throws PubChemConnectionException
  {
    try {
      URL url = new URL(
        EFETCH_URL +
        "?db=pccompound&rettype=uilist&mode=xml&query_key=" + queryKey +
        "&WebEnv=" + webEnv);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.connect();
      return connection.getInputStream();
    }
    catch (Exception e) {
      log.warn(
        "couldnt get esummary content from NCBI for inchi " + inchi + ": " +
        e.getMessage());
      throw new PubChemConnectionException();
    }
  }

  // debugging helper method:
  private void dumpInputStreamToStdout(InputStream inputStream) {
    try {
      for (int ch = inputStream.read(); ch != -1; ch = inputStream.read()) {
        System.out.print((char) ch);
      }
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }
}

