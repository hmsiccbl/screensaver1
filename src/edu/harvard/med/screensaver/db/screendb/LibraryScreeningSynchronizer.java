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
import java.util.Date;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.model.screens.LibraryScreening;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;

class LibraryScreeningSynchronizer extends ScreeningSynchronizer
{

  // static members

  private static Logger log = Logger.getLogger(LibraryScreeningSynchronizer.class);

  
  // public constructors and methods

  public LibraryScreeningSynchronizer(
    Connection connection,
    GenericEntityDAO dao,
    LibrariesDAO librariesDao,
    UserSynchronizer userSynchronizer,
    ScreenSynchronizer screenSynchronizer)
  {
    _connection = connection;
    _dao = dao;
    _librariesDao = librariesDao;
    _userSynchronizer = userSynchronizer;
    _screenSynchronizer = screenSynchronizer;
  }

  public void synchronizeLibraryScreenings() throws ScreenDBSynchronizationException
  {
    _dao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        try {
          deleteOldLibraryScreenings();
          synchronizeLibraryScreeningsProper();
          synchronizePlatesUsed();
          synchronizeEquipmentUsed();
        }
        catch (SQLException e) {
          _synchronizationException = new ScreenDBSynchronizationException(
            "Encountered an SQL exception while synchonrizing library screenings: " + e.getMessage(),
            e);
        }
        catch (ScreenDBSynchronizationException e) {
          _synchronizationException = e;
        }
      }
    });
    if (_synchronizationException != null) {
      throw _synchronizationException;
    }
  }


  // private instance methods
  
  private void deleteOldLibraryScreenings() {
    for (LibraryScreening libraryScreening : _dao.findAllEntitiesOfType(LibraryScreening.class)) {
      libraryScreening.getScreen().getScreeningRoomActivities().remove(libraryScreening);
      libraryScreening.getPerformedBy().getHbnActivitiesPerformed().remove(libraryScreening);
    }
  }

  private void synchronizeLibraryScreeningsProper() throws SQLException, ScreenDBSynchronizationException
  {
    Statement statement = _connection.createStatement();
    ResultSet resultSet = statement.executeQuery(
      "SELECT v.*, s.screen_type, s.user_id AS lead_screener_id FROM visits v, screens s " +
      "WHERE v.screen_id = s.id AND visit_type IN ('Library', 'Special', 'Preliminary', 'Liquid Handling only')");
    while (resultSet.next()) {
      LibraryScreening screening = createLibraryScreening(resultSet);
      screening.setComments(resultSet.getString("comments"));
      screening.setAssayProtocol(resultSet.getString("assay_protocol"));
      screening.setAssayProtocolLastModifiedDate(resultSet.getDate("assay_date"));
      screening.setNumberOfReplicates(resultSet.getInt("no_replicate_screen"));
      screening.setAbaseTestsetId(resultSet.getString("abase_testset_id"));
      screening.setIsSpecial(getIsSpecial(resultSet));
      synchronizeVolumeTransferredPerWell(resultSet, screening);
      synchronizeEstimatedFinalScreenConcentration(resultSet, screening);
      synchronizeAssayProtocolType(resultSet, screening);
      
      _screenDBVisitIdToScreeningMap.put(resultSet.getInt("id"), screening);
      _dao.persistEntity(screening);
    }
    statement.close();
  }

  /**
   * Create a new {@link LibraryScreening} object for the given result set.
   * The returned <code>LibraryScreening</code> has exactly the following properties initialized:
   * <ul>
   * <li><code>screen</code>
   * <li><code>performedBy</code>
   * <li><code>dateCreated</code>
   * <li><code>dateOfVisit</code>
   * </ul>
   * 
   * @param resultSet the SQL result set to get the needed information to 
   * create a new LibraryScreening
   * @return the new LibraryScreening
   * @throws SQLException
   * @throws ScreenDBSynchronizationException
   */
  private LibraryScreening createLibraryScreening(ResultSet resultSet)
  throws SQLException, ScreenDBSynchronizationException
  {
    Integer screenNumber = resultSet.getInt("screen_id");
    Date dateCreated = resultSet.getDate("date_created");
    Date dateOfActivity = resultSet.getDate("date_of_visit");
    Screen screen = _screenSynchronizer.getScreenForScreenNumber(screenNumber);
    ScreeningRoomUser performedBy = getPerformedBy(resultSet);
    return new LibraryScreening(screen, performedBy, dateCreated, dateOfActivity);
  }
}

