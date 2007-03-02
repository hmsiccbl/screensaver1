// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db.screendb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.DAO;

public class ScreenDBSynchronizer
{
  // static members

  private static Logger log = Logger.getLogger(ScreenDBSynchronizer.class);
  static {
    try {
      Class.forName("org.postgresql.Driver");
    }
    catch (ClassNotFoundException e) {
      log.error("couldn't find postgresql driver");
    }    
  }
  

  // instance data members
  
  private String _server;
  private String _database;
  private String _username;
  private String _password;
  private Connection _connection;
  private DAO _dao;
  private String _errorMessageKey;
  

  // public constructors and methods

  public ScreenDBSynchronizer(String server, String database, String username, String password, DAO dao)
  {
    _server = server;
    _database = database;
    _username = username;
    _password = password;
    _dao = dao;
  }
  
  /**
   * Attempt to synchronize Screensaver with ScreenDB. Return true when successful. When
   * unsuccessul, set the error message key, and return false. It is expected that this method
   * is only called once per ScreenDBSynchronizer connection.
   * @return true whenever the synchronization was successful
   */
  public boolean synchronize()
  {
    if (! initializeConnection()) {
      _errorMessageKey = "screenDBSynchronizer.couldNotConnect";
      return false;
    }
    try {
      ScreenDBUserSynchronizer userSynchronizer = new ScreenDBUserSynchronizer(_connection, _dao);
      userSynchronizer.synchronizeUsers();
    }
    catch (ScreenDBSynchronizationException e) {
      // TODO: report error message as well
      log.error(e);
      _errorMessageKey = "screenDBSynchronizer.synchronizationException";
      return false;
    }

    // TODO: screens, visits, libraries

    return true;
  }
  
  /**
   * Get the message key for the error that occurred. It is expected that this method is only
   * called after {@link #synchronize()}, and only when that method returned <code>false</code>.
   * @return the message key for the error that occurred
   */
  public String getErrorMessageKey()
  {
    return _errorMessageKey;
  }
  
  
  // private methods

  private boolean initializeConnection()
  {
    try {
      _connection = DriverManager.getConnection(
        "jdbc:postgresql://" + _server + "/" + _database,
        _username,
        _password);
    }
    catch (SQLException e) {
      log.error("could not connect to ScreenDB database: " + e.getMessage());
      e.printStackTrace();
      return false;
    }
    return true;
  }
}

