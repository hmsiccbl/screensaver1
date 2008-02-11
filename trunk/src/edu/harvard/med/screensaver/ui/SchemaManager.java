// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui;

import edu.harvard.med.screensaver.db.CherryPickRequestDAO;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.db.SchemaUtil;
import edu.harvard.med.screensaver.db.screendb.ScreenDBSynchronizer;

import org.apache.log4j.Logger;

public class SchemaManager extends AbstractBackingBean
{
  private static Logger log = Logger.getLogger(SchemaManager.class);

  private SchemaUtil _schemaUtil;
  private GenericEntityDAO _dao;
  private LibrariesDAO _librariesDao;
  private CherryPickRequestDAO _cherryPickRequestDao;

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

  public void setDao(GenericEntityDAO dao)
  {
    _dao = dao;
  }

  public void setLibrariesDao(LibrariesDAO dao)
  {
    _librariesDao = dao;
  }

  public void setCherryPickRequestDao(CherryPickRequestDAO dao)
  {
    _cherryPickRequestDao = dao;
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
      _dao,
      _librariesDao,
      _cherryPickRequestDao);
    screenDBSynchronizer.synchronize();
    showMessage("screenDBSynchronizer.screenDBSynchronized");
  }
}
