// $HeadURL: $
// $Id: $
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver;

import edu.harvard.med.screensaver.util.NullSafeUtils;

public class DatabaseConnectionSettings
{
  private String host;
  private Integer port;
  private String database;
  private String user;
  private String password;

  public DatabaseConnectionSettings(String host, Integer port, String database, String user, String password)
  {
    this.host = host;
    this.port = port;
    this.database = database;
    this.user = user;
    this.password = password;
  }

  public String getHost()
  {
    return host;
  }

  public Integer getPort()
  {
    return port;
  }

  public String getDatabase()
  {
    return database;
  }

  public String getUser()
  {
    return user;
  }

  public String getPassword()
  {
    return password;
  }
  
  public String getJdbcUrl()
  {
    // TODO: make database-agnostic
    return "jdbc:postgresql://" +
      NullSafeUtils.toString(getHost(), "localhost") +
      (getPort() == null ? "" : (":" + getPort())) + "/" +
      getDatabase();
  }

  @Override
  public String toString()
  {
    return getJdbcUrl();
  }
}
