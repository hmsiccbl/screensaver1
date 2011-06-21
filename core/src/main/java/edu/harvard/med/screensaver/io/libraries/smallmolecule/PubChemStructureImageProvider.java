// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries.smallmolecule;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;

public class PubChemStructureImageProvider implements StructureImageProvider
{
  private static Logger log = Logger.getLogger(PubChemStructureImageProvider.class);
  
  private static final String IMAGE_URL_PREFIX =
    "http://pubchem.ncbi.nlm.nih.gov/image/imagefly.cgi?width=300&height=300&cid=";

  @Override
  public URL getImageUrl(SmallMoleculeReagent reagent)
  {
    try {
      Integer pubchemCid = getPubchemCid(reagent);
      if (pubchemCid == null) {
        return null;
      }
      return new URL(IMAGE_URL_PREFIX + pubchemCid);
    }
    catch (MalformedURLException e) {
      // should never occur
      return null;
    }
  }

  private Integer getPubchemCid(SmallMoleculeReagent reagent) 
  {
    if (reagent.getPubchemCids().size() > 0) {
      return reagent.getPubchemCids().iterator().next();
    }
    return null;
  }
}
