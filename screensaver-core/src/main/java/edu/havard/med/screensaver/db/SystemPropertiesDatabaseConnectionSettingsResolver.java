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

public class SystemPropertiesDatabaseConnectionSettingsResolver extends NamedVariablesDatabaseConnectionSettingsResolver
{
  public SystemPropertiesDatabaseConnectionSettingsResolver()
  {
    super("database.host",
          "database.port",
          "database.name",
          "database.user",
          "database.password");
  }

  @Override
  public DatabaseConnectionSettings resolve() throws DatabaseConnectionSettingsResolutionException
  {
    if (System.getProperty(databaseVariableName) == null) {
      return null;
    }
    return new DatabaseConnectionSettings(System.getProperty(hostVariableName, "localhost"),
                                          Integer.getInteger(portVariableName),
                                          System.getProperty(databaseVariableName),
                                          System.getProperty(userVariableName),
                                          System.getProperty(passwordVariableName));
  }
}
