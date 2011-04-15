// $HeadURL: $
// $Id: $
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.havard.med.screensaver.db;

import edu.harvard.med.screensaver.DatabaseConnectionSettings;
import edu.harvard.med.screensaver.db.DatabaseConnectionSettingsResolutionException;

public class EnvironmentVariablesDatabaseConnectionSettingsResolver extends NamedVariablesDatabaseConnectionSettingsResolver
{
  public EnvironmentVariablesDatabaseConnectionSettingsResolver()
  {
    super("DB_HOST",
          "DB_PORT",
          "DB_NAME",
          "DB_USER",
          "DB_PASSWORD");
  }

  @Override
  public DatabaseConnectionSettings resolve() throws DatabaseConnectionSettingsResolutionException
  {
    if (System.getenv(databaseVariableName) == null) {
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
