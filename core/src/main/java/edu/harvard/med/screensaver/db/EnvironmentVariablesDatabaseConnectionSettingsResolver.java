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

public class EnvironmentVariablesDatabaseConnectionSettingsResolver extends NamedVariablesDatabaseConnectionSettingsResolver
{
  private static final Logger log = Logger.getLogger(EnvironmentVariablesDatabaseConnectionSettingsResolver.class);

  public EnvironmentVariablesDatabaseConnectionSettingsResolver()
  {
    super("environment variables",
          "DB_HOST",
          "DB_PORT",
          "DB_NAME",
          "DB_USER",
          "DB_PASSWORD");
  }

  @Override
  protected String resolveProperty(String variableName)
  {
    return System.getenv(variableName);
  }
}
