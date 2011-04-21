// $HeadURL: $
// $Id: $
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.iccbl.screensaver.db;

import java.io.IOException;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.DatabaseConnectionSettings;
import edu.harvard.med.screensaver.db.CommandLineArgumentsDatabaseConnectionSettingsResolver;
import edu.harvard.med.screensaver.db.DatabaseConnectionSettingsResolutionException;

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
public class IccblCommandLineArgumentsDatabaseConnectionSettingsResolver extends CommandLineArgumentsDatabaseConnectionSettingsResolver
{
  private static final Logger log = Logger.getLogger(IccblCommandLineArgumentsDatabaseConnectionSettingsResolver.class);

  @Override
  public DatabaseConnectionSettings resolve() throws DatabaseConnectionSettingsResolutionException
  {
    DatabaseConnectionSettings settings = super.resolve();
    if (settings == null) {
      return null;
    }

    String user = settings.getUser();
    if (user == null) {
      user = System.getenv("USER");
      log.info("resolved database user using logged in user account name: " + user);
    }
    String password = settings.getPassword();
    if (password == null) {
      try {
        password = new DotPgpassFileParser().getPasswordFromDotPgpassFile(settings.getHost(),
                                                                          settings.getPort().toString(),
                                                                          settings.getDatabase(),
                                                                          settings.getUser());
        if (password != null) {
          log.info("resolved database password in ~/.pgpass: " + password);
        }
        else {
          log.warn("could not resolve database password in ~/.pgpass");
        }
      }
      catch (IOException e) {
        throw new DatabaseConnectionSettingsResolutionException(e);
      }
    }
    return new DatabaseConnectionSettings(settings.getHost(),
                                          settings.getPort(),
                                          settings.getDatabase(),
                                          settings.getUser(),
                                          settings.getPassword());
  }
}
