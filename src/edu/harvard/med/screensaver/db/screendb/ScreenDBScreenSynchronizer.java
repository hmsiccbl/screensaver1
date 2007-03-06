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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.model.DuplicateEntityException;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.StatusItem;
import edu.harvard.med.screensaver.model.screens.StatusValue;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;

public class ScreenDBScreenSynchronizer
{

  // static members

  private static Logger log = Logger.getLogger(ScreenDBScreenSynchronizer.class);


  // instance data members
  
  private Connection _connection;
  private DAO _dao;
  private ScreenDBUserSynchronizer _userSynchronizer;
  private ScreenDBSynchronizationException _synchronizationException = null;
  private ScreenType.UserType _screenUserType = new ScreenType.UserType();
  private StatusValue.UserType _statusValueUserType = new StatusValue.UserType();
  private Map<Integer,Screen> _screenNumberToScreenMap = new HashMap<Integer,Screen>();

  
  // public constructors and methods

  public ScreenDBScreenSynchronizer(Connection connection, DAO dao, ScreenDBUserSynchronizer userSynchronizer)
  {
    _connection = connection;
    _dao = dao;
    _userSynchronizer = userSynchronizer;
  }

  public void synchronizeScreens() throws ScreenDBSynchronizationException
  {
    _dao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        try {
          synchronizeScreensProper();
          synchronizeCollaborators();
          synchronizeStatusItems();
          // TODO: get all the one-to-many fields
        }
        catch (SQLException e) {
          _synchronizationException = new ScreenDBSynchronizationException(
            "Encountered an SQL exception while synchonrizing screens: " + e.getMessage(),
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
  
  private void synchronizeScreensProper() throws SQLException
  {
    Statement statement = _connection.createStatement();
    ResultSet resultSet = statement.executeQuery(
      "SELECT * FROM screens");
    while (resultSet.next()) {
      ScreeningRoomUser leadScreener = getLeadScreener(resultSet);
      ScreeningRoomUser labHead = getLabHead(leadScreener);
      Integer screenNumber = resultSet.getInt("id");
      Date dateCreated = resultSet.getDate("date_created");
      ScreenType screenType = _screenUserType.getTermForValue(resultSet.getString("screen_type"));
      String screenTitle = resultSet.getString("screen_title");
      Date dataMeetingScheduled = resultSet.getDate("data_mtg_schld");
      Date dataMeetingComplete = resultSet.getDate("data_mtg_done");
      String summary = resultSet.getString("summary");
      String comments = resultSet.getString("comments");
      String abaseStudyId = resultSet.getString("study_id");
      String abaseProtocolId = resultSet.getString("protocol_id");

      Screen screen = _dao.findEntityByProperty(Screen.class, "hbnScreenNumber", screenNumber);
      if (screen == null) {
        screen = new Screen(leadScreener, labHead, screenNumber, dateCreated, screenType,
          screenTitle, dataMeetingScheduled, dataMeetingComplete, summary, comments, abaseStudyId,
          abaseProtocolId);
      }
      else {
        screen.setLeadScreener(leadScreener);
        screen.setLabHead(labHead);
        screen.setDateCreated(dateCreated);
        screen.setScreenType(screenType);
        screen.setTitle(screenTitle);
        screen.setDataMeetingScheduled(dataMeetingScheduled);
        screen.setDataMeetingComplete(dataMeetingComplete);
        screen.setSummary(summary);
        screen.setComments(comments);
        screen.setAbaseStudyId(abaseStudyId);
        screen.setAbaseProtocolId(abaseProtocolId);
      }
      _dao.persistEntity(screen);
      _screenNumberToScreenMap.put(screenNumber, screen);
    }
  }

  private ScreeningRoomUser getLabHead(ScreeningRoomUser leadScreener) {
    ScreeningRoomUser labHead = leadScreener.getLabHead();
    if (labHead == null) {
      labHead = leadScreener;
    }
    return labHead;
  }

  private ScreeningRoomUser getLeadScreener(ResultSet resultSet) throws SQLException {
    ScreeningRoomUser leadScreener =
      _userSynchronizer.getScreeningRoomUserForScreenDBUserId(resultSet.getInt("user_id"));
    return leadScreener;
  }

  private void synchronizeCollaborators() throws SQLException, ScreenDBSynchronizationException
  {
    // synchronizing requires emptying out all previous collaborators
    for (Screen screen : _screenNumberToScreenMap.values()) {
      screen.setCollaboratorsList(new ArrayList<ScreeningRoomUser>());
    }
    Statement statement = _connection.createStatement();
    ResultSet resultSet = statement.executeQuery("SELECT user_id, screen_id FROM collaborators");
    while (resultSet.next()) {
      Integer userId = resultSet.getInt("user_id");
      ScreeningRoomUser user = _userSynchronizer.getScreeningRoomUserForScreenDBUserId(userId);
      Integer screenId = resultSet.getInt("screen_id");
      Screen screen = _screenNumberToScreenMap.get(screenId);
      if (user != null && screen != null) {
        screen.addCollaborator(user);
      }
      else {
        String message;
        if (user == null) {
          message = "invalid user_id in screendb collaborators table: " + userId; 
        }
        else {
          message = "invalid screen_id in screendb collaborators table: " + screenId;
        }
        log.error(message);
        throw new ScreenDBSynchronizationException(message);
      }
    }
    statement.close();
  }

  private void synchronizeStatusItems() throws SQLException, ScreenDBSynchronizationException
  {
    // synchronizing requires emptying out all previous statuses
    for (Screen screen : _screenNumberToScreenMap.values()) {
      Set<StatusItem> statusItems = screen.getStatusItems();
      statusItems.removeAll(statusItems);
    }
    Statement statement = _connection.createStatement();
    ResultSet resultSet = statement.executeQuery("SELECT * FROM screen_status");
    while (resultSet.next()) {
      Integer screenNumber = resultSet.getInt("screen_id");
      Screen screen = _screenNumberToScreenMap.get(screenNumber);
      Date statusDate = resultSet.getDate("status_date");
      StatusValue statusValue = getStatusValue(resultSet);
      if (screen != null) {
        try {
          new StatusItem(screen, statusDate, statusValue);
        }
        catch (DuplicateEntityException e) {
          throw new ScreenDBSynchronizationException(
            "duplicate status item for screen number " + screenNumber, e);
        }
      }
      else {
        throw new ScreenDBSynchronizationException(
          "invalid screen_id in screendb screen_status table: " + screenNumber);
      }
    }
    statement.close();
  }

  private StatusValue getStatusValue(ResultSet resultSet) throws SQLException, ScreenDBSynchronizationException {
    String statusValueString = resultSet.getString("status");
    if (statusValueString.equals("Moved to Broad Institute")) {
      statusValueString = "Transferred to Broad Institute";
    }
    StatusValue statusValue = _statusValueUserType.getTermForValue(statusValueString);
    if (statusValue == null) {
      throw new ScreenDBSynchronizationException(
        "invalid status value in screendb screen_status table: \"" + statusValueString + "\".");
    }
    return statusValue;
  }
}

