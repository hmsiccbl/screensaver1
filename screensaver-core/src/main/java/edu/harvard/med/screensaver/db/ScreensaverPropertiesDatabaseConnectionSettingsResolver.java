// $HeadURL: $
// $Id: $
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.DatabaseConnectionSettings;
import edu.harvard.med.screensaver.ScreensaverProperties;

public class ScreensaverPropertiesDatabaseConnectionSettingsResolver extends NamedVariablesDatabaseConnectionSettingsResolver
{
  private static final Logger log = Logger.getLogger(ScreensaverPropertiesDatabaseConnectionSettingsResolver.class);

  public ScreensaverPropertiesDatabaseConnectionSettingsResolver()
  {
    super("database.host",
          "database.port",
          "database.name",
          "database.user",
          "database.password");
  }

  @Override
  public DatabaseConnectionSettings resolve(ScreensaverProperties screensaverProperties) throws DatabaseConnectionSettingsResolutionException
  {
    if (screensaverProperties.getProperty(databaseVariableName) == null) {
      log.warn("screensaver properties file does not contain database connection settings");
      return null;
    }
    String port = System.getenv(portVariableName);
    Integer portNumber = null;
    try {
      if (port != null) {
        portNumber = Integer.parseInt(port);
      }
    }
    catch (NumberFormatException e) {
      throw new DatabaseConnectionSettingsResolutionException("invalid port number " + port);
    }
    return new DatabaseConnectionSettings(System.getenv(hostVariableName),
                                          portNumber,
                                          System.getenv(databaseVariableName),
                                          System.getenv(userVariableName),
                                          System.getenv(passwordVariableName));
  }

}
