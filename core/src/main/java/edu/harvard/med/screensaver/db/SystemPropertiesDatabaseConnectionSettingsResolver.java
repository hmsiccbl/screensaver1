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
  protected String resolveProperty(String variableName)
  {
    return System.getProperty(variableName);
  }
}
