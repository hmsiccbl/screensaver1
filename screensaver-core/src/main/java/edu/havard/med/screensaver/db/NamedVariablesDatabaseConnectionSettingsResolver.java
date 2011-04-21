// $HeadURL: $
// $Id: $
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.havard.med.screensaver.db;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.DatabaseConnectionSettingsResolver;

public abstract class NamedVariablesDatabaseConnectionSettingsResolver implements DatabaseConnectionSettingsResolver
{
  private static final Logger log = Logger.getLogger(NamedVariablesDatabaseConnectionSettingsResolver.class);

  protected String hostVariableName;
  protected String portVariableName;
  protected String databaseVariableName;
  protected String userVariableName;
  protected String passwordVariableName;

  protected NamedVariablesDatabaseConnectionSettingsResolver(String defaultHostVariableName,
                                                             String defaultPortVariableName,
                                                             String defaultDatabaseVariableName,
                                                             String defaultUserVariableName,
                                                             String defaultPasswordVariableName)
  {
    setHostVariableName(defaultHostVariableName);
    setPortVariableName(defaultPortVariableName);
    setDatabaseVariableName(defaultDatabaseVariableName);
    setUserVariableName(defaultUserVariableName);
    setPasswordVariableName(defaultPasswordVariableName);
  }

  public void setHostVariableName(String hostVariableName)
  {
    this.hostVariableName = hostVariableName;
    log.debug("host variable name=" + hostVariableName);
  }

  public void setPortVariableName(String portVariableName)
  {
    this.portVariableName = portVariableName;
    log.debug("port variable name=" + portVariableName);
  }

  public void setDatabaseVariableName(String databaseVariableName)
  {
    this.databaseVariableName = databaseVariableName;
    log.debug("database variable name=" + databaseVariableName);
  }

  public void setUserVariableName(String userVariableName)
  {
    this.userVariableName = userVariableName;
    log.debug("user variable name=" + userVariableName);
  }

  public void setPasswordVariableName(String passwordVariableName)
  {
    this.passwordVariableName = passwordVariableName;
    log.debug("password variable name=" + passwordVariableName);
  }
}
