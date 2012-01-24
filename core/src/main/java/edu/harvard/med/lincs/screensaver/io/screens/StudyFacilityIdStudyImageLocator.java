// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.lincs.screensaver.io.screens;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.io.screens.StudyImageLocator;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.util.UrlEncrypter;

/**
 * ImageProvider that looks up a study image by study facility ID.
 */
public class StudyFacilityIdStudyImageLocator implements StudyImageLocator<Screen>
{
  private static final String IMAGE_FILE_EXTENSION = ".png";

  private static Logger log = Logger.getLogger(StudyFacilityIdStudyImageLocator.class);

  private String _baseUrl;
	private UrlEncrypter _urlEncrypter;

  public StudyFacilityIdStudyImageLocator(String baseUrl)
  {
    _baseUrl = baseUrl;
  }
  
  public StudyFacilityIdStudyImageLocator(String baseUrl, UrlEncrypter urlEncrypter)
  {
    _baseUrl = baseUrl;
    _urlEncrypter = urlEncrypter;
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
      String name = facilityId;
      if(_urlEncrypter != null) {
      	// NOTE: do not encrypt the baseUrl, as this is required by the web.xml to identify the image locator servlet
      	name = _urlEncrypter.encryptUrl(name);
      }
       name = name + IMAGE_FILE_EXTENSION;

      File relativeLocation = new File(name);
      URL url = new URL(_baseUrl + relativeLocation);
      if (log.isDebugEnabled()) {
        log.debug("image URL for screen " + screen + ": " + url);
      }
      return url;
    }
    catch (MalformedURLException e) {
      log.error("On getImageUrl", e);
      return null;
    }
  }
}
