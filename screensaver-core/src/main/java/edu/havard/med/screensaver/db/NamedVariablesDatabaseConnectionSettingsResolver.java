// $HeadURL: $
// $Id: $
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.havard.med.screensaver.db;

import edu.harvard.med.screensaver.db.DatabaseConnectionSettingsResolver;

public abstract class NamedVariablesDatabaseConnectionSettingsResolver implements DatabaseConnectionSettingsResolver
{
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
    this.hostVariableName = defaultHostVariableName;
    this.portVariableName = defaultPortVariableName;
    this.databaseVariableName = defaultDatabaseVariableName;
    this.userVariableName = defaultUserVariableName;
    this.passwordVariableName = defaultPasswordVariableName;
  }

  public void setHostVariableName(String hostVariableName)
  {
    this.hostVariableName = hostVariableName;
  }

  public void setPortVariableName(String portVariableName)
  {
    this.portVariableName = portVariableName;
  }

  public void setDatabaseVariableName(String databaseVariableName)
  {
    this.databaseVariableName = databaseVariableName;
  }

  public void setUserVariableName(String userVariableName)
  {
    this.userVariableName = userVariableName;
  }

  public void setPasswordVariableName(String passwordVariableName)
  {
    this.passwordVariableName = passwordVariableName;
  }
}
