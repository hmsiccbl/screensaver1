// $HeadURL$
// $Id$
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
import edu.harvard.med.screensaver.ScreensaverProperties;
import edu.harvard.med.screensaver.db.CommandLineArgumentsDatabaseConnectionSettingsResolver;
import edu.harvard.med.screensaver.db.DatabaseConnectionSettingsResolutionException;
import edu.harvard.med.screensaver.db.NeedsScreensaverProperties;
import edu.harvard.med.screensaver.util.NullSafeUtils;

/**
 * Resolves database connection settings from command-line arguments, with the following enhancements:
 * <ul>
 * <li><strike>if user is not specified will resolve using the $USER environment variable</strike></li>
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
    DatabaseConnectionSettings settings = super.resolve();
    if (settings == null) {
      return null;
    }
    try {
      if (settings.getPassword() == null) {
        // TODO: if user is undefined, use the USER env variable 
        String passwordFromDotPgpassFile =
          new DotPgpassFileParser().getPasswordFromDotPgpassFile(settings.getHost(),
                                                                 NullSafeUtils.toString(settings.getPort(), (String) null),
                                                                 settings.getDatabase(),
                                                                 settings.getUser());
        log.info("resolved database password from user's .pgpass file");
        settings = new DatabaseConnectionSettings(settings.getHost(),
                                                  settings.getPort(),
                                                  settings.getDatabase(),
                                                  settings.getUser(),
                                                  passwordFromDotPgpassFile);
      }
      return settings;
    }
    catch (IOException e) {
      throw new DatabaseConnectionSettingsResolutionException("error while trying to resolve password from .pgpass file", e);
    }
  }
}
