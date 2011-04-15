// $HeadURL: $
// $Id: $
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import edu.harvard.med.screensaver.DatabaseConnectionSettings;

public class CommandLineArgumentsDatabaseConnectionSettingsResolver implements DatabaseConnectionSettingsResolver
{
  private DatabaseConnectionSettings _settings;

  @Override
  public DatabaseConnectionSettings resolve() throws DatabaseConnectionSettingsResolutionException
  {
    if (_settings == null) {
      return null;
    }
    return _settings;
  }

  public void setDatabaseConnectionSettings(DatabaseConnectionSettings settings)
  {
    _settings = settings;
  }
}
