// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.iccbl.screensaver.io.libraries.smallmolecule;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import edu.harvard.med.screensaver.io.libraries.smallmolecule.StructureImageProvider;
import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;
import edu.harvard.med.screensaver.model.libraries.WellKey;

import org.apache.log4j.Logger;

/**
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class PlateWellFileNameStructureImageProvider implements StructureImageProvider
{
  private static final String IMAGE_FILE_EXTENSION = ".png";

  private static Logger log = Logger.getLogger(PlateWellFileNameStructureImageProvider.class);
  
  private String _baseUrl;

  public PlateWellFileNameStructureImageProvider(String baseUrl)
  {
    _baseUrl = baseUrl;
  }

  public InputStream getImage(SmallMoleculeReagent reagent) throws IOException
  {
    return getImageUrl(reagent).openStream();
  }

  public URL getImageUrl(SmallMoleculeReagent reagent)
  {
    try {
      if (reagent.isRestricted()) {
        return null;
      }
      WellKey wellKey = reagent.getWell().getWellKey();
      String plateNumberLabel = WellKey.getPlateNumberLabel(wellKey.getPlateNumber());
      File relativeLocation = new File(WellKey.getPlateNumberLabel(wellKey.getPlateNumber()),
                                       plateNumberLabel + wellKey.getWellName() + IMAGE_FILE_EXTENSION);
      URL url = new URL(_baseUrl + relativeLocation);
      if (log.isDebugEnabled()) {
        log.debug("image URL for reagent " + reagent + ": " + url);
      }
      return url;
    }
    catch (MalformedURLException e) {
      return null;
    }
  }
}
