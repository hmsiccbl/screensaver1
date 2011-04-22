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

public class CommandLineArgumentsDatabaseConnectionSettingsResolver implements DatabaseConnectionSettingsResolver
{
  private static final Logger log = Logger.getLogger(CommandLineArgumentsDatabaseConnectionSettingsResolver.class);

  private DatabaseConnectionSettings _settings;

  @Override
  public DatabaseConnectionSettings resolve(ScreensaverProperties screensaverProperties) throws DatabaseConnectionSettingsResolutionException
  {
    if (_settings == null) {
      log.warn("command line arguments do not contain database connection settings");
      return null;
    }
    return _settings;
  }

  public void setDatabaseConnectionSettings(DatabaseConnectionSettings settings)
  {
    _settings = settings;
  }
}
