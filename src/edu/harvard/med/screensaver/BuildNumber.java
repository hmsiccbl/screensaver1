// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
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

import org.apache.log4j.Logger;

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
            Matcher matcher =pattern.matcher(buildNumber);
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
    }
    return buildNumber;
  }

  // private methods

}

