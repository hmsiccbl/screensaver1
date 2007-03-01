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
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUserClassification;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;

public class ScreenDBUserSynchronizer
{
  // static members

  private static Logger log = Logger.getLogger(ScreenDBUserSynchronizer.class);


  // instance data members
  
  private Connection _connection;
  private DAO _dao;

  private Map<Integer,Integer> _screenDBUserIdToLabHeadId = new HashMap<Integer,Integer>();
  private Map<Integer,ScreeningRoomUser> _screenDBUserIdToScreeningRoomUserMap =
    new HashMap<Integer,ScreeningRoomUser>();
  ScreeningRoomUserClassification.UserType _userClassificationUserType =
    new ScreeningRoomUserClassification.UserType();

  
  // public constructors and methods

  public ScreenDBUserSynchronizer(Connection connection, DAO dao)
  {
    _connection = connection;
    _dao = dao;
  }
  
  public void synchronizeUsers()
  {
    _dao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        constructMappings();
        connectUsersToLabHeads();
        persistScreeningRoomUsers();
      }
    });
  }
  
  
  // private methods

  /**
   * Construct the {@link #_screenDBUserIdToLabHeadId} and
   * {@link #_screenDBUserIdToScreensaverUserMap} maps. 
   */
  private void constructMappings()
  {
    try {
      Statement statement = _connection.createStatement();
      ResultSet resultSet = statement.executeQuery("SELECT * FROM users");
      while (resultSet.next()) {
        ScreeningRoomUser user = constructScreeningRoomUser(resultSet);
        Integer id = resultSet.getInt("id");
        _screenDBUserIdToLabHeadId.put(id, resultSet.getInt("lab_name"));
        _screenDBUserIdToScreeningRoomUserMap.put(id, user);
      }
    }
    catch (SQLException e) {
      log.error("sql error: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private ScreeningRoomUser constructScreeningRoomUser(ResultSet resultSet) throws SQLException
  {
    Date dateCreated = resultSet.getDate("date_created");
    String firstName = resultSet.getString("first");
    String lastName = resultSet.getString("last");
    String email = getEmail(resultSet);
    String phone = resultSet.getString("phone");
    String mailingAddress = resultSet.getString("lab_location");
    String comments = resultSet.getString("comments");
    String ecommonsId = getEcommonsId(resultSet);
    String harvardId = resultSet.getString("harvard_id");
    //String labAffiliation = resultSet.getString("lab_affiliation");
    ScreeningRoomUserClassification classification = getClassification(resultSet);
    boolean isNonScreeningUser = resultSet.getBoolean("non_user");
    boolean isRnaiUser = resultSet.getBoolean("rani_user" /*[sic]*/);
    
    ScreeningRoomUser user = getExistingUser(firstName, lastName);
    if (user == null) {
      user = new ScreeningRoomUser(dateCreated, firstName, lastName, email, phone,
        mailingAddress, comments, ecommonsId, harvardId, classification, isNonScreeningUser);
    }
    else {
      user.setDateCreated(dateCreated);
      user.setEmail(email);
      user.setPhone(phone);
      user.setMailingAddress(mailingAddress);
      user.setComments(comments);
      user.setECommonsId(ecommonsId);
      user.setHarvardId(harvardId);
      user.setUserClassification(classification);
      user.setNonScreeningUser(isNonScreeningUser);
    }

    if (isRnaiUser) {
      user.addScreensaverUserRole(ScreensaverUserRole.RNAI_SCREENING_ROOM_USER);
    }
    else {
      user.addScreensaverUserRole(ScreensaverUserRole.COMPOUND_SCREENING_ROOM_USER);
    }

    // TODO: lab affiliation
    // TODO: checklist items
    
    return user;
  }
  
  private ScreeningRoomUser getExistingUser(String firstName, String lastName) {
    ScreeningRoomUser user;
    Map<String,Object> nameMap = new HashMap<String,Object>();
    nameMap.put("firstName", firstName);
    nameMap.put("lastName", lastName);
    user = _dao.findEntityByProperties(ScreeningRoomUser.class, nameMap);
    return user;
  }

  private String getEcommonsId(ResultSet resultSet) throws SQLException {
    String eCommonsId = resultSet.getString("ecommons_id");
    if (eCommonsId != null) {
      eCommonsId = eCommonsId.toLowerCase();
    }
    return eCommonsId;
  }

  /**
   * Get the email for the user. Return a fake email with high likelihood of uniqueness, and
   * clearly recognizable as a fake email, if ScreenDB is missing the email.
   */
  private String getEmail(ResultSet resultSet) throws SQLException {
    String email = resultSet.getString("email");
    if (email == null || email.contains("unknown") || email.contains("notknown")) {
      email =
        resultSet.getString("first") + "." +
        resultSet.getString("last") + "@has.missing.email";
    }
    return email;
  }
  
  private ScreeningRoomUserClassification getClassification(ResultSet resultSet)
  throws SQLException
  {
    String classificationString = resultSet.getString("classification");
    if (classificationString != null && classificationString.equals("PI")) {
      classificationString = "Principal Investigator";
    }
    ScreeningRoomUserClassification classification = 
      _userClassificationUserType.getTermForValue(classificationString);
    if (classification == null) {
      classification = ScreeningRoomUserClassification.UNASSIGNED;
    }
    return classification;
  }
  
  private void connectUsersToLabHeads()
  {
    for (Integer memberId : _screenDBUserIdToLabHeadId.keySet()) {
      Integer headId = _screenDBUserIdToLabHeadId.get(memberId);
      ScreeningRoomUser member = _screenDBUserIdToScreeningRoomUserMap.get(memberId);
      ScreeningRoomUser head = _screenDBUserIdToScreeningRoomUserMap.get(headId);
      if (head != null && head != member) {
        member.setLabHead(head);
      }
    }
  }
  
  private void persistScreeningRoomUsers()
  {
    for (ScreeningRoomUser user : _screenDBUserIdToScreeningRoomUserMap.values()) {
      _dao.persistEntity(user);
    }
  }
}

