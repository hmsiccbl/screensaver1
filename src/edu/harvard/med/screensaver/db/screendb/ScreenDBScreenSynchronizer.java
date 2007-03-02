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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
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
  private Map<Integer,Screen> _screenDBScreenIdToScreenMap = new HashMap<Integer,Screen>();

  
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
          for (Screen screen : _screenDBScreenIdToScreenMap.values()) {
            synchronizeScreenCollaborators(screen);
            // TODO: get all the one-to-many fields
          }
        }
        catch (SQLException e) {
          _synchronizationException = new ScreenDBSynchronizationException(
            "Encountered an SQL exception while synchonrizing screens: " + e.getMessage(),
            e);
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
      _screenDBScreenIdToScreenMap.put(screenNumber, screen);
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

  private void synchronizeScreenCollaborators(Screen screen) throws SQLException
  {
    screen.setCollaboratorsList(new ArrayList<ScreeningRoomUser>());
    PreparedStatement statement = 
      _connection.prepareStatement("SELECT user_id FROM collaborators WHERE screen_id = ?");
    statement.setInt(1, screen.getScreenNumber());
    ResultSet resultSet = statement.executeQuery();
    while (resultSet.next()) {
      Integer userId = resultSet.getInt("user_id");
      ScreeningRoomUser collaborator =
        _userSynchronizer.getScreeningRoomUserForScreenDBUserId(userId);
      if (collaborator == null) {
        log.error("no user " + userId);
      }
      else {
        screen.addCollaborator(collaborator);
      }
    }
  }
}

