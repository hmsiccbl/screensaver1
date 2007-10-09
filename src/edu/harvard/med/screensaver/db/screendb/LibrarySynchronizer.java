// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db.screendb;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.screens.ScreenType;

import org.apache.log4j.Logger;

/**
 * Assumes the start plate of a library never changes, that it is safe to use as business key
 * across the two databases.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class LibrarySynchronizer
{

  // static members

  private static Logger log = Logger.getLogger(LibrarySynchronizer.class);


  // instance data members
  
  private Connection _connection;
  private GenericEntityDAO _dao;
  private LibrariesDAO _librariesDao;
  private ScreenDBSynchronizationException _synchronizationException = null;
  private LibraryType.UserType _libraryTypeUserType = new LibraryType.UserType();
  
  
  // public constructors and methods

  public LibrarySynchronizer(Connection connection, 
                                     GenericEntityDAO dao,
                                     LibrariesDAO librariesDao)
  {
    _connection = connection;
    _dao = dao;
    _librariesDao = librariesDao;
  }

  public void synchronizeLibraries() throws ScreenDBSynchronizationException
  {
    _dao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        try {
          // TODO: review this, make sure we are not missing anything (e.g., copy info)
          Statement statement = _connection.createStatement();
          ResultSet resultSet = statement.executeQuery(
            "SELECT l.*, lt.name AS library_type\n" +
            "FROM library l, library_type lt\n" +
            "WHERE l.library_type_id = lt.id");
          while (resultSet.next()) {
            String libraryName = resultSet.getString("name");
            String shortName = resultSet.getString("short_name");
            ScreenType screenType = getScreenType(resultSet);
            LibraryType libraryType = getLibraryType(resultSet);
            Integer startPlate = resultSet.getInt("start_plate");
            Integer endPlate = resultSet.getInt("end_plate");
            String vendor = resultSet.getString("vendor");
            Library library = _librariesDao.findLibraryWithPlate(startPlate);
            if (library == null) {
              library = new Library(
                libraryName,
                shortName,
                screenType,
                libraryType,
                startPlate,
                endPlate);
            }
            else {
              library.setLibraryName(libraryName);
              library.setShortName(shortName);
              library.setScreenType(screenType);
              library.setLibraryType(libraryType);
            }
            library.setDescription(resultSet.getString("description"));
            library.setVendor(vendor);
            _dao.persistEntity(library);
          }
        }
        catch (SQLException e) {
          _synchronizationException = new ScreenDBSynchronizationException(
            "SQL exception synchronizing libraries: " + e.getMessage(),
            e);
        }
      }
    });
    if (_synchronizationException != null) {
      throw _synchronizationException;
    }
  }

  private ScreenType getScreenType(ResultSet resultSet) throws SQLException
  {
    if (resultSet.getString("library_type").equals("RNAi")) {
      return ScreenType.RNAI;
    }
    else {
      return ScreenType.SMALL_MOLECULE;
    }
  }  

  private LibraryType getLibraryType(ResultSet resultSet) throws SQLException
  {
    String rawLibraryType = resultSet.getString("library_type");
    if (rawLibraryType.equals("RNAi")) {
      return LibraryType.SIRNA;
    }
    else {
      return _libraryTypeUserType.getTermForValue(rawLibraryType);
    }
  }
}

