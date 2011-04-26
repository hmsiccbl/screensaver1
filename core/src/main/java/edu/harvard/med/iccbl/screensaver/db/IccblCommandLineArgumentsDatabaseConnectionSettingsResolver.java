// $HeadURL: $
// $Id: $
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.iccbl.screensaver.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.DatabaseConnectionSettings;
import edu.harvard.med.screensaver.ScreensaverProperties;
import edu.harvard.med.screensaver.db.CommandLineArgumentsDatabaseConnectionSettingsResolver;
import edu.harvard.med.screensaver.db.DatabaseConnectionSettingsResolutionException;
import edu.harvard.med.screensaver.db.NeedsScreensaverProperties;

/**
 * Resolves database connection settings from command-line arguments, with the following enhancements:
 * <ul>
 * <li>if user is not specified will resolve using the $USER environment variable</li>
 * <li>if password is not specified (as it should not be, on a multi-user host) will resolve by inspecting the uesr's
 * ~/.pgpass file</li>
 * </ul>
 * 
 * @author atolopko
 */
public class IccblCommandLineArgumentsDatabaseConnectionSettingsResolver extends CommandLineArgumentsDatabaseConnectionSettingsResolver implements NeedsScreensaverProperties

{
  private static final Logger log = Logger.getLogger(IccblCommandLineArgumentsDatabaseConnectionSettingsResolver.class);

  private ScreensaverProperties _screensaverProperties;

  @Override
  public void setScreensaverProperties(ScreensaverProperties screensaverProperties)
  {
    _screensaverProperties = screensaverProperties;
  }

  @Override
  public DatabaseConnectionSettings resolve() throws DatabaseConnectionSettingsResolutionException
  {
    String orchestraAuthFilename = _screensaverProperties.getProperty("orchestra.db.connection.file");
    if (orchestraAuthFilename == null) {
      return null;
    }
    log.info("using orchestra database connection settings from file " + orchestraAuthFilename);

    // parse the datasource properties out of the file
    try {
      Pattern pattern = Pattern.compile("SetEnv\\s+(\\S+)\\s+(\\S+)");
      File authFile = new File(orchestraAuthFilename);
      FileInputStream authFileInputStream = new FileInputStream(authFile);
      InputStreamReader authInputStreamReader = new InputStreamReader(authFileInputStream);
      BufferedReader authBufferedReader = new BufferedReader(authInputStreamReader);
      String line = authBufferedReader.readLine();
      while (line != null) {
        Matcher matcher = pattern.matcher(line);
        if (matcher.matches()) {
          String property = matcher.group(1);
          String propertyValue = matcher.group(2);
          //_properties.setProperty(property, propertyValue);
        }
        line = authBufferedReader.readLine();
      }
      return null;
    }
    catch (IOException e) {
      throw new DatabaseConnectionSettingsResolutionException("unable to read and parse the orchestra auth file", e);
    }

    //    return new DatabaseConnectionSettings(settings.getHost(),
    //                                          settings.getPort(),
    //                                          settings.getDatabase(),
    //                                          settings.getUser(),
    //                                          settings.getPassword());
  }
}
