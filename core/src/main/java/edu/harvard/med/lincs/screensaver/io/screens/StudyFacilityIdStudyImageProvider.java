// $HeadURL: http://seanderickson1@forge.abcd.harvard.edu/svn/screensaver/branches/go/trunk/src/edu/harvard/med/screensaver/io/libraries/smallmolecule/StructureImageProvider.java $
// $Id: StructureImageProvider.java 3968 2010-04-08 17:04:35Z atolopko $
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.lincs.screensaver.io.screens;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.io.screens.StudyImageProvider;
import edu.harvard.med.screensaver.model.screens.Screen;

/**
 * ImageProvider that looks up a study image by study facility ID.
 */
public class StudyFacilityIdStudyImageProvider implements StudyImageProvider<Screen>
{
  private static final String IMAGE_FILE_EXTENSION = ".png";

  private static Logger log = Logger.getLogger(StudyFacilityIdStudyImageProvider.class);

  private String _baseUrl;

  //private ImageProviderServlet _imageProviderServlet;

  public StudyFacilityIdStudyImageProvider(String baseUrl/* , ImageProviderServlet imageProviderServlet */)
  {
    _baseUrl = baseUrl;
    //_imageProviderServlet = imageProviderServlet;
  }

  @Override
  public URL getImageUrl(Screen screen)
  {
    try {
      if (screen == null) {
        return null;
      }
      if (screen.isRestricted()) {
        return null;
      }
      String facilityId = screen.getFacilityId();
      if (facilityId == null) {
        log.warn("No facility id for the screen: " + screen.getTitle());
        return null;
      }

      String name = facilityId + IMAGE_FILE_EXTENSION;

      File relativeLocation = new File(name);
      URL url = new URL(_baseUrl + relativeLocation);
      if (log.isDebugEnabled()) {
        log.debug("image URL for screen " + screen + ": " + url);
      }
      /*
       * if (!_imageProviderServlet.canFindImage(url) ){
       * log.info("image not available from the url: " + url);
       * return null;
       * }
       */
      return url;
    }
    catch (MalformedURLException e) {
      log.error("On getImageUrl", e);
      return null;
    }
  }
}
