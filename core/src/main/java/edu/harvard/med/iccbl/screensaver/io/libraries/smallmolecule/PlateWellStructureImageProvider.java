// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.iccbl.screensaver.io.libraries.smallmolecule;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.io.libraries.smallmolecule.StructureImageProvider;
import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.ui.arch.util.servlet.ImageProviderServlet;

/**
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class PlateWellStructureImageProvider implements StructureImageProvider<SmallMoleculeReagent>
{
  private static final String IMAGE_FILE_EXTENSION = ".png";

  private static Logger log = Logger.getLogger(PlateWellStructureImageProvider.class);
  
  private String _baseUrl;
  private ImageProviderServlet _imageProviderServlet;
  
  public PlateWellStructureImageProvider(String baseUrl, ImageProviderServlet imageProviderServlet)
  {
    _baseUrl = baseUrl;
    _imageProviderServlet = imageProviderServlet;
  }

  public URL getImageUrl(SmallMoleculeReagent reagent)
  {
    try {
      if (reagent == null) {
        return null;
      }
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
      
      if (!_imageProviderServlet.canFindImage(url) ){
        log.info("image not available from the url: " + url);
        return null;
      }
      
      return url;
    }
    catch (MalformedURLException e) {
      return null;
    }
  }

}
