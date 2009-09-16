// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * Determines the application's build number, which is taken to be the same as
 * the Subversion revision number.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class BuildNumber
{
  // static members

  /**
   * match valid outputs of 'svnversion'; see 'svnversion --help' for possible formats of its output
   */
  private static final String SVN_VERSION_NUMBER_REGEX = "(\\d+)[MS]?(:\\d+[MS]?)?";

  private static Logger log = Logger.getLogger(BuildNumber.class);
  
  private static String buildNumber;
  
  public static String getBuildNumber()
  {
    if (buildNumber == null) {
      InputStream inputStream = BuildNumber.class.getResourceAsStream(ScreensaverConstants.BUILD_NUMBER_FILE);
      BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
      try {
        synchronized (BuildNumber.class) {
          if (buildNumber == null) {
            buildNumber = reader.readLine();
            Pattern pattern = Pattern.compile(SVN_VERSION_NUMBER_REGEX);
            Matcher matcher = pattern.matcher(buildNumber);
            if (!matcher.matches()) {
              throw new RuntimeException("unrecognizable build number string: '" + buildNumber + "'");
            }
            buildNumber = matcher.group(1);
          }
        }
      }
      catch (Exception e) {
        log.warn("cannot determine build number: " + e.getMessage());
        buildNumber = "";
      }
      finally {
        IOUtils.closeQuietly(reader);
      }
    }
    return buildNumber;
  }

  // private methods

}

