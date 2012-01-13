// $HeadURL$
// $Id$
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.ScreensaverProperties;

public class ScreensaverPropertiesDatabaseConnectionSettingsResolver extends NamedVariablesDatabaseConnectionSettingsResolver implements NeedsScreensaverProperties
{
  private static final Logger log = Logger.getLogger(ScreensaverPropertiesDatabaseConnectionSettingsResolver.class);

  ScreensaverProperties _screensaverProperties;

  public ScreensaverPropertiesDatabaseConnectionSettingsResolver()
  {
    super("screensaver properties file",
          "database.host",
          "database.port",
          "database.name",
          "database.user",
          "database.password");
  }

  @Override
  protected String resolveProperty(String variableName)
  {
    return _screensaverProperties.getProperty(variableName);
  }

  @Override
  public void setScreensaverProperties(ScreensaverProperties screensaverProperties)
  {
    _screensaverProperties = screensaverProperties;
  }
}
