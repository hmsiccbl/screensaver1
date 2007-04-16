// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.db.SchemaUtil;
import edu.harvard.med.screensaver.db.screendb.ScreenDBSynchronizer;
import edu.harvard.med.screensaver.ui.screens.ScreenViewer;

public class SchemaManager extends AbstractBackingBean
{
  private static Logger log = Logger.getLogger(ScreenViewer.class);
  
  private SchemaUtil _schemaUtil;
  private DAO _dao;

  private String _screenDBServer = "localhost";
  private String _screenDBDatabase = "screendb";
  private String _screenDBUsername = "s";
  private String _screenDBPassword;
  
  
  // getters and setters
  
  public SchemaUtil getSchemaUtil()
  {
    return _schemaUtil;
  }

  public void setSchemaUtil(SchemaUtil schemaUtil)
  {
    _schemaUtil = schemaUtil;
  }

  public DAO getDao()
  {
    return _dao;
  }

  public void setDao(DAO dao)
  {
    _dao = dao;
  }

  public String getScreenDBServer()
  {
    return _screenDBServer;
  }

  public void setScreenDBServer(String screenDBServer)
  {
    _screenDBServer = screenDBServer;
  }

  public String getScreenDBDatabase()
  {
    return _screenDBDatabase;
  }

  public void setScreenDBDatabase(String screenDBDatabase)
  {
    _screenDBDatabase = screenDBDatabase;
  }

  public String getScreenDBUsername()
  {
    return _screenDBUsername;
  }

  public void setScreenDBUsername(String screenDBUsername)
  {
    _screenDBUsername = screenDBUsername;
  }

  public String getScreenDBPassword()
  {
    return _screenDBPassword;
  }

  public void setScreenDBPassword(String screenDBPassword)
  {
    _screenDBPassword = screenDBPassword;
  }

  
  // JSF application methods
  
  public void dropSchema()
  {
    _schemaUtil.dropSchema();
  }
  
  public void createSchema()
  {
    _schemaUtil.createSchema();
  }
  
  public void initializeDatabase()
  {
    _schemaUtil.initializeDatabase();
  }
  
  public void truncateTables()
  {
    _schemaUtil.truncateTablesOrCreateSchema();
  }
  
  public void grantDeveloperPermissions()
  {
    _schemaUtil.grantDeveloperPermissions();
  }
  
  public void synchronizeScreenDB()
  {
    if (_screenDBUsername.equals("")) {
      showMessage("screenDBSynchronizer.missingUsernamePassword");
      return; 
    }
    ScreenDBSynchronizer screenDBSynchronizer = new ScreenDBSynchronizer(
      _screenDBServer,
      _screenDBDatabase,
      _screenDBUsername,
      _screenDBPassword,
      _dao);
    screenDBSynchronizer.synchronize();
    showMessage("screenDBSynchronizer.screenDBSynchronized");
  }
}
