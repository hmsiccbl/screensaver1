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

public class SystemPropertiesDatabaseConnectionSettingsResolver extends NamedVariablesDatabaseConnectionSettingsResolver
{
  private static final Logger log = Logger.getLogger(SystemPropertiesDatabaseConnectionSettingsResolver.class);

  public SystemPropertiesDatabaseConnectionSettingsResolver()
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
    if (System.getProperty(databaseVariableName) == null) {
      log.warn("system properties do not contain database connection settings");
      return null;
    }
    return new DatabaseConnectionSettings(System.getProperty(hostVariableName, "localhost"),
                                          Integer.getInteger(portVariableName),
                                          System.getProperty(databaseVariableName),
                                          System.getProperty(userVariableName),
                                          System.getProperty(passwordVariableName));
  }
}
