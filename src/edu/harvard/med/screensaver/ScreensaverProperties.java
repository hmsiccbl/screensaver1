// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

public class ScreensaverProperties
{
  // static fields

  private static Logger log = Logger.getLogger(ScreensaverProperties.class);
  private static final String SCREENSAVER_PROPERTIES_RESOURCE = 
    "../../../../screensaver.properties"; // relative to current package
  private static Properties _screensaverProperties = new Properties();
  static {
    InputStream screensaverPropertiesInputStream =
      ScreensaverProperties.class.getResourceAsStream(SCREENSAVER_PROPERTIES_RESOURCE);
    try {
      _screensaverProperties.load(screensaverPropertiesInputStream);
    }
    catch (IOException e) {
      log.error("error loading screensaver.properties resource", e);
    }
  }
  
  
  // static methods
  
  public static String getProperty(String name)
  {
    return _screensaverProperties.getProperty(name);
  }
  
  /** 
   * @param name
   * @return false if the property is nonexistent or not equal (case-insensitive) "t" or "true"
   */
  public static boolean getBooleanProperty(String name)
  {
    String temp = _screensaverProperties.getProperty(name, "false").toLowerCase();
    return temp.equals("true") || temp.equals("t");
  }
}

