// $HeadURL: http://seanderickson1@forge.abcd.harvard.edu/svn/screensaver/branches/go/2.2.3-dev/src/edu/harvard/med/iccbl/screensaver/io/libraries/smallmolecule/PrimaryCompoundNameStructureImageProvider.java $
// $Id: PrimaryCompoundNameStructureImageProvider.java 5269 2011-02-02 22:47:10Z seanderickson1 $
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.lincs.screensaver.io.libraries.smallmolecule;

import java.net.URL;

import org.apache.log4j.Logger;

import edu.harvard.med.lincs.screensaver.LincsScreensaverConstants;
import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;
import edu.harvard.med.screensaver.ui.arch.images.ImageProvider;

public abstract class SmallMoleculeReagentFacilitySaltIdGenericImageProvider implements ImageProvider<SmallMoleculeReagent>
{
  private static final String IMAGE_FILE_EXTENSION = ".png";

  private static Logger log = Logger.getLogger(SmallMoleculeReagentFacilitySaltIdGenericImageProvider.class);
  
  private String _baseUrl;

  public SmallMoleculeReagentFacilitySaltIdGenericImageProvider(String baseUrl)
  {
    _baseUrl = baseUrl;
  }

  @Override
  public URL getImageUrl(SmallMoleculeReagent reagent)
  {
    try {
      if (reagent == null) {
        return null;
      }
      if (reagent.isRestricted()) {
        return null;
      }

      String name = reagent.getWell().getFacilityId() + LincsScreensaverConstants.FACILITY_ID_SEPARATOR + reagent.getSaltFormId();
      if (name.equals(LincsScreensaverConstants.FACILITY_ID_SEPARATOR)) {
        return null;
      }

      name = name + IMAGE_FILE_EXTENSION;
      URL url = new URL(_baseUrl + name);
      if (log.isDebugEnabled()) {
        log.debug("image URL for reagent " + reagent + ": " + url);
      }
      return url;
    }
    catch (Exception e) {
      log.error(e);
      return null;
    }
  }

}
