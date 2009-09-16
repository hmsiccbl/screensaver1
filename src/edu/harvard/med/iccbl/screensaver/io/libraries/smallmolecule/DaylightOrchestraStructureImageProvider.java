// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.iccbl.screensaver.io.libraries.smallmolecule;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import edu.harvard.med.screensaver.io.libraries.smallmolecule.StructureImageProvider;
import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;

import org.apache.log4j.Logger;

public class DaylightOrchestraStructureImageProvider implements StructureImageProvider
{
  private static Logger log = Logger.getLogger(DaylightOrchestraStructureImageProvider.class);

  private static final String IMAGE_URL_PREFIX =
    "https://screensaver.med.harvard.edu/render_molecule.png?smiles=";


  public InputStream getImage(SmallMoleculeReagent reagent) throws IOException
  {
    return getImageUrl(reagent).openStream();
  }

  public URL getImageUrl(SmallMoleculeReagent reagent)
  {
    try {
      return new URL(IMAGE_URL_PREFIX + URLEncoder.encode(reagent.getSmiles(), "UTF-8"));
    }
    catch (MalformedURLException e) {
      // should never occur
      return null;
    }
    catch (UnsupportedEncodingException e){
      throw new RuntimeException("UTF-8 not supported", e);
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
