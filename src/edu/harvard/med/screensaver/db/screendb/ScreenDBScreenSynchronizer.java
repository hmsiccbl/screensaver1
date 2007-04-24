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
import edu.harvard.med.screensaver.model.screens.AbaseTestset;
import edu.harvard.med.screensaver.model.screens.BillingInfoToBeRequested;
import edu.harvard.med.screensaver.model.screens.BillingInformation;
import edu.harvard.med.screensaver.model.screens.FundingSupport;
import edu.harvard.med.screensaver.model.screens.Publication;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.StatusItem;
import edu.harvard.med.screensaver.model.screens.StatusValue;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.util.eutils.PublicationInfo;
import edu.harvard.med.screensaver.util.eutils.PublicationInfoProvider;

public class ScreenDBScreenSynchronizer
{

  // static members

  private static Logger log = Logger.getLogger(ScreenDBScreenSynchronizer.class);


  // instance data members
  
  private Connection _connection;
  private DAO _dao;
  private ScreenDBUserSynchronizer _userSynchronizer;
  private ScreenType.UserType _screenUserType = new ScreenType.UserType();
  private StatusValue.UserType _statusValueUserType = new StatusValue.UserType();
  private FundingSupport.UserType _fundingSupportUserType = new FundingSupport.UserType();
  private Map<Integer,Screen> _screenNumberToScreenMap = new HashMap<Integer,Screen>();
  private PublicationInfoProvider _publicationInfoProvider = new PublicationInfoProvider();

  
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
          synchronizeAbaseTestsets();
          synchronizePublications();
        }
        catch (SQLException e) {
          throw new ScreenDBSynchronizationException(
            "Encountered an SQL exception while synchonrizing screens: " + e.getMessage(),
            e);
        }
      }
    });
  }

  public Screen getScreenForScreenNumber(Integer screenNumber)
  {
    return _dao.reloadEntity(_screenNumberToScreenMap.get(screenNumber));
  }
  

  // private instance methods
  
  private void synchronizeScreensProper() throws SQLException, ScreenDBSynchronizationException
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
      String keywords = resultSet.getString("keywords");
      String fundingSupportString = resultSet.getString("funding_support");

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
      synchronizeKeywords(keywords, screen);
      synchronizeFundingSupports(fundingSupportString, screen);
      _dao.persistEntity(screen);
      synchronizeBillingInformation(resultSet, screen);
      _screenNumberToScreenMap.put(screenNumber, screen);
    }
  }

  private ScreeningRoomUser getLeadScreener(ResultSet resultSet) throws SQLException {
    ScreeningRoomUser leadScreener =
      _userSynchronizer.getScreeningRoomUserForScreenDBUserId(resultSet.getInt("user_id"));
    return leadScreener;
  }

  private ScreeningRoomUser getLabHead(ScreeningRoomUser leadScreener) {
    ScreeningRoomUser labHead = leadScreener.getLabHead();
    if (labHead == null) {
      labHead = leadScreener;
    }
    return labHead;
  }

  private void synchronizeKeywords(String keywords, Screen screen) {
    Set<String> keywordSet = screen.getKeywords();
    keywordSet.removeAll(keywordSet);
    if (keywords == null || keywords.equals("")) {
      return;
    }
    for (String keyword : keywords.split(",\\s*")) {
      keywordSet.add(keyword);
    }
  }

  private void synchronizeFundingSupports(String fundingSupportString, Screen screen) throws ScreenDBSynchronizationException {
    Set<FundingSupport> fundingSupports = screen.getFundingSupports();
    fundingSupports.removeAll(fundingSupports);
    FundingSupport fundingSupport = _fundingSupportUserType.getTermForValue(fundingSupportString);
    if (fundingSupport == null) {
      throw new ScreenDBSynchronizationException(
        "unrecognized funding support category \"" + fundingSupportString + "\"");
    }
    fundingSupports.add(fundingSupport);
  }

  private void synchronizeBillingInformation(ResultSet resultSet, Screen screen) throws SQLException
  {
    BillingInformation oldBillingInformation = screen.getBillingInformation();
    if (oldBillingInformation != null) {
      _dao.deleteEntity(oldBillingInformation);
    }
    boolean billingInfoToBeRequested = resultSet.getBoolean("billing_info_to_be_requested");
    Date billingInfoReturnDate = resultSet.getDate("billing_info_return_date");
    String billingComments = resultSet.getString("billing_comments");
    BillingInformation billingInformation = new BillingInformation(
      screen,
      billingInfoToBeRequested ? BillingInfoToBeRequested.YES : BillingInfoToBeRequested.NO);
    billingInformation.setBillingInfoReturnDate(billingInfoReturnDate);
    billingInformation.setComments(billingComments);
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
      ScreeningRoomUser user = getCollaborator(resultSet);
      Screen screen = getScreenFromTable("collaborators", resultSet.getInt("screen_id"));
      screen.addCollaborator(user);
    }
    statement.close();
  }

  private ScreeningRoomUser getCollaborator(ResultSet resultSet)
  throws SQLException, ScreenDBSynchronizationException
  {
    Integer userId = resultSet.getInt("user_id");
    ScreeningRoomUser user = _userSynchronizer.getScreeningRoomUserForScreenDBUserId(userId);
    if (user == null) {
      throw new ScreenDBSynchronizationException(
        "invalid user_id in screendb collaborators table: " + userId);
    }
    return user;
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
      Screen screen = getScreenFromTable("screen_status", screenNumber);
      Date statusDate = resultSet.getDate("status_date");
      StatusValue statusValue = getStatusValue(resultSet);
      try {
        new StatusItem(screen, statusDate, statusValue);
      }
      catch (DuplicateEntityException e) {
        throw new ScreenDBSynchronizationException(
          "duplicate status item for screen number " + screenNumber, e);
      }
    }
    statement.close();
  }

  private Screen getScreenFromTable(String tablename, Integer screenNumber)
  throws ScreenDBSynchronizationException
  {
    Screen screen = _screenNumberToScreenMap.get(screenNumber);
    if (screen == null) {
      throw new ScreenDBSynchronizationException(
        "invalid screen_id in screendb " + tablename + " table: " + screenNumber);        
    }
    return screen;
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

  private void synchronizeAbaseTestsets() throws SQLException, ScreenDBSynchronizationException
  {
    // synchronizing requires emptying out all previous abase testsets
    for (Screen screen : _screenNumberToScreenMap.values()) {
      Set<AbaseTestset> abaseTestsets = screen.getAbaseTestsets();
      abaseTestsets.removeAll(abaseTestsets);
    }
    Statement statement = _connection.createStatement();
    ResultSet resultSet = statement.executeQuery("SELECT * FROM abase");
    while (resultSet.next()) {
      Integer screenNumber = resultSet.getInt("screen_id");
      Screen screen = getScreenFromTable("abase", screenNumber);
      Date testsetDate = resultSet.getDate("abase_date");
      String testsetName = resultSet.getString("testset_name");
      String comments = resultSet.getString("comments");
      try {
        new AbaseTestset(screen, testsetDate, testsetName, comments);
      }
      catch (DuplicateEntityException e) {
        throw new ScreenDBSynchronizationException(
          "duplicate abase testset for screen number " + screenNumber, e);
      }
    }
    statement.close();
  }
  
  private void synchronizePublications() throws SQLException, ScreenDBSynchronizationException
  {
    // synchronizing requires emptying out all previous publications
    for (Screen screen : _screenNumberToScreenMap.values()) {
      Set<Publication> publications = screen.getPublications();
      publications.removeAll(publications);
    }
    Statement statement = _connection.createStatement();
    ResultSet resultSet = statement.executeQuery("SELECT * FROM pub_med");
    while (resultSet.next()) {
      Integer screenNumber = resultSet.getInt("screen_id");
      Screen screen = getScreenFromTable("pub_med", screenNumber);
      String pubmedId = resultSet.getString("pubmed_id");
      PublicationInfo publicationInfo = 
        _publicationInfoProvider.getPublicationInfoForPubmedId(new Integer(pubmedId));
      if (publicationInfo == null) {
        throw new ScreenDBSynchronizationException("unable to get publication info from pubmed");
      }
      try {
        new Publication(
          screen,
          pubmedId,
          publicationInfo.getYearPublished(),
          publicationInfo.getAuthors(),
          publicationInfo.getTitle());
      }
      catch (DuplicateEntityException e) {
        throw new ScreenDBSynchronizationException(
          "duplicate publication for screen number " + screenNumber, e);
      }
    }
    statement.close();
  }
}

