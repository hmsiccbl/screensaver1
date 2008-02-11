// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.util.eutils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * Queries PubChem to provide a list of PubChem CIDs for a given SMILES string.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class PubchemCidListProvider extends EutilsUtils
{
  // static fields

  private static final Logger log = Logger.getLogger(PubchemCidListProvider.class);
  
  
  // public constructor and instance method
  
  public PubchemCidListProvider()
  {
    initializeDocumentBuilder();
  }

  public List<String> getPubchemCidListForInchi(String inchi)
  {
    List<String> pubchemCids = new ArrayList<String>();
    try {
      Document document = getXMLForEutilsQuery(
        "esearch.fcgi",
        "&db=pccompound&&rettype=uilist&term=\"" + URLEncoder.encode(inchi, "UTF-8") + "\"[inchi]");
      if (document == null) {
        return pubchemCids;
      }
      NodeList pubchemCidNodes = document.getElementsByTagName("Id");
      for (int i = 0; i < pubchemCidNodes.getLength(); i ++) {
        String pubchemCid = getTextContent(pubchemCidNodes.item(i));
        pubchemCids.add(pubchemCid);
      }
    }
    catch (UnsupportedEncodingException e) {
    }
    return pubchemCids;
  }

  
  // protected instance method

  protected void reportError(String errorMessage)
  {
    log.error("Error querying PubChem by InChI: " + errorMessage);
  }
}
