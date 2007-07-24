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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.model.screens.RNAiCherryPickRequest;
import edu.harvard.med.screensaver.model.screens.RNAiCherryPickScreening;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;

class RNAiCherryPickScreeningSynchronizer extends ScreeningSynchronizer
{

  // static members

  private static Logger log = Logger.getLogger(RNAiCherryPickScreeningSynchronizer.class);
  private static Pattern _numericalVolumeTransferredPattern =
    Pattern.compile(".*?([\\d.]+)(([nu][lL])?\\s*(x|X|and)\\s*(\\d+))?.*");

  
  // public constructors and methods

  public RNAiCherryPickScreeningSynchronizer(
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

  public void synchronizeRnaiCherryPickScreenings() throws ScreenDBSynchronizationException
  {
    _dao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        try {
          deleteOldRnaiCherryPickScreenings();
          synchronizeRnaiCherryPickScreeningsProper();
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
  
  private void deleteOldRnaiCherryPickScreenings() {
    for (RNAiCherryPickScreening rnaiCherryPickScreening :
      _dao.findAllEntitiesOfType(RNAiCherryPickScreening.class)) {
      rnaiCherryPickScreening.getScreen().getScreeningRoomActivities().remove(rnaiCherryPickScreening);
      rnaiCherryPickScreening.getPerformedBy().getHbnActivitiesPerformed().remove(rnaiCherryPickScreening);
    }
  }

  private void synchronizeRnaiCherryPickScreeningsProper() throws SQLException, ScreenDBSynchronizationException
  {
    Statement statement = _connection.createStatement();
    ResultSet resultSet = statement.executeQuery(
      "SELECT v.*, s.screen_type, s.user_id AS lead_screener_id FROM visits v, screens s " +
      "WHERE v.screen_id = s.id AND visit_type = 'RNAi Cherry Pick Screening'");
    while (resultSet.next()) {
      RNAiCherryPickScreening screening = createRnaiCherryPickScreening(resultSet);
      if (screening == null) {
        continue; // forced at present because some of these visits have null cprNumber
      }
      screening.setComments(resultSet.getString("comments"));
      screening.setAssayProtocol(resultSet.getString("assay_protocol"));
      screening.setAssayProtocolLastModifiedDate(resultSet.getDate("assay_date"));
      screening.setNumberOfReplicates(resultSet.getInt("no_replicate_screen"));
      synchronizeVolumeTransferredPerWell(resultSet, screening);
      synchronizeEstimatedFinalScreenConcentration(resultSet, screening);
      synchronizeAssayProtocolType(resultSet, screening);
      
      _screenDBVisitIdToScreeningMap.put(resultSet.getInt("id"), screening);
      _dao.persistEntity(screening);
    }
    statement.close();
  }

  /**
   * Create a new {@link RNAiCherryPickScreening} object for the given result set.
   * The returned <code>RNAiCherryPickScreening</code> has exactly the following properties
   * initialized:
   * <ul>
   * <li><code>screen</code>
   * <li><code>performedBy</code>
   * <li><code>dateCreated</code>
   * <li><code>dateOfVisit</code>
   * <li><code>rnaiCherryPickRequest</code>
   * </ul>
   * 
   * @param resultSet the SQL result set to get the needed information to 
   * create a new RNAiCherryPickScreening
   * @return the new RNAiCherryPickScreening
   * @throws SQLException
   * @throws ScreenDBSynchronizationException
   */
  private RNAiCherryPickScreening createRnaiCherryPickScreening(ResultSet resultSet)
  throws SQLException, ScreenDBSynchronizationException
  {
    Integer cherryPickRequestNumber = resultSet.getInt("cpr_number_for_cp_screen");
    if (cherryPickRequestNumber == null || cherryPickRequestNumber == 0) {
      log.warn(
        "encountered an RNAi Cherry Pick Screening without a CPR Number: " +
        resultSet.getInt("id"));
      return null;
    }
    Integer screenNumber = resultSet.getInt("screen_id");
    Date dateCreated = resultSet.getDate("date_created");
    Date dateOfActivity = resultSet.getDate("date_of_visit");
    Screen screen = _screenSynchronizer.getScreenForScreenNumber(screenNumber);
    ScreeningRoomUser performedBy = getPerformedBy(resultSet);
    RNAiCherryPickRequest cherryPickRequest =
      getRNAiCherryPickRequestByCherryPickRequestNumber(cherryPickRequestNumber);
    return new RNAiCherryPickScreening(
      screen,
      performedBy,
      dateCreated,
      dateOfActivity,
      cherryPickRequest);
  }
  
  private RNAiCherryPickRequest getRNAiCherryPickRequestByCherryPickRequestNumber(
    Integer cherryPickRequestNumber)
  {
    RNAiCherryPickRequest cherryPickRequest = _dao.findEntityByProperty(
      RNAiCherryPickRequest.class,
      "legacyCherryPickRequestNumber",
      cherryPickRequestNumber,
      true);
    if (cherryPickRequest == null) {
      throw new ScreenDBSynchronizationException(
        "couldnt find RNAiCherryPickRequest with cherryPickRequestNumber " + cherryPickRequestNumber);
    }
    return cherryPickRequest;
  }
}
