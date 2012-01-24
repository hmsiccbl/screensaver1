// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.iccbl.screensaver.io.libraries.smallmolecule;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.io.libraries.smallmolecule.StructureImageLocator;
import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.util.UrlEncrypter;

/**
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class PlateWellStructureImageLocator implements StructureImageLocator
{
  private static final String IMAGE_FILE_EXTENSION = ".png";

  private static Logger log = Logger.getLogger(PlateWellStructureImageLocator.class);
  
  private String _baseUrl;

	private UrlEncrypter _urlEncrypter;

  public PlateWellStructureImageLocator(String baseUrl)
  {
    _baseUrl = baseUrl;
  }
  
  public PlateWellStructureImageLocator(String baseUrl, UrlEncrypter urlEncrypter)
  {
    _baseUrl = baseUrl;
    _urlEncrypter = urlEncrypter;
  }

  public URL getImageUrl(SmallMoleculeReagent reagent)
  {
    try {
      if (reagent == null) {
        return null;
      }
      WellKey wellKey = reagent.getWell().getWellKey();
      String plateNumberLabel = WellKey.getPlateNumberLabel(wellKey.getPlateNumber());
      String relativeLocation = (new File(WellKey.getPlateNumberLabel(wellKey.getPlateNumber()),
                                       plateNumberLabel + wellKey.getWellName() )).toString();
      if(_urlEncrypter != null) {
      	// NOTE: do not encrypt the baseUrl, as this is required by the web.xml to identify the image locator servlet
      	relativeLocation = _urlEncrypter.encryptUrl(relativeLocation);
      }
      URL url = new URL(_baseUrl + relativeLocation+ IMAGE_FILE_EXTENSION);
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
