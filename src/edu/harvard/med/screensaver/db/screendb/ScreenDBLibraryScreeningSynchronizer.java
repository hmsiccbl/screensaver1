// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db.screendb;

import java.math.BigDecimal;
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
import edu.harvard.med.screensaver.model.DuplicateEntityException;
import edu.harvard.med.screensaver.model.screens.AssayProtocolType;
import edu.harvard.med.screensaver.model.screens.LibraryScreening;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;

public class ScreenDBLibraryScreeningSynchronizer
{

  // static members

  private static Logger log = Logger.getLogger(ScreenDBLibraryScreeningSynchronizer.class);


  // instance data members
  
  private Connection _connection;
  private DAO _dao;
  private ScreenDBUserSynchronizer _userSynchronizer;
  private ScreenDBScreenSynchronizer _screenSynchronizer;
  private Map<Integer,LibraryScreening> _screenDBVisitIdToLibraryScreeningMap =
    new HashMap<Integer,LibraryScreening>();
  private ScreenDBSynchronizationException _synchronizationException = null;

  
  // public constructors and methods

  public ScreenDBLibraryScreeningSynchronizer(
    Connection connection,
    DAO dao,
    ScreenDBUserSynchronizer userSynchronizer,
    ScreenDBScreenSynchronizer screenSynchronizer)
  {
    _connection = connection;
    _dao = dao;
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
          synchronizeLibraryScreeningsProper();
          // TODO: get the plates used
          // TODO: get the equipment used
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
  
  private void synchronizeLibraryScreeningsProper() throws SQLException, ScreenDBSynchronizationException
  {
    Statement statement = _connection.createStatement();
    ResultSet resultSet = statement.executeQuery(
      "SELECT v.*, s.screen_type, s.user_id AS lead_screener_id FROM visits v, screens s " +
      "WHERE v.screen_id = s.id AND visit_type IN ('Library', 'Special')");
    while (resultSet.next()) {
      LibraryScreening screening = findOrCreateLibraryScreening(resultSet);
      screening.setComments(resultSet.getString("comments"));
      screening.setAssayProtocol(resultSet.getString("assay_protocol"));
      screening.setAssayProtocolLastModifiedDate(resultSet.getDate("assay_date"));
      screening.setNumberOfReplicates(resultSet.getInt("no_replicate_screen"));
      screening.setAbaseTestsetId(resultSet.getString("abase_testset_id"));
      screening.setIsSpecial(resultSet.getString("visit_type").equals("Special"));
      synchronizeVolumeTransferredPerWell(resultSet, screening);
      synchronizeEstimatedFinalScreenConcentration(resultSet, screening);
      synchronizeAssayProtocolType(resultSet, screening);
      
      _screenDBVisitIdToLibraryScreeningMap.put(resultSet.getInt("id"), screening);
      _dao.persistEntity(screening);
    }
  }

  /**
   * Find an existing, or create a new, {@link LibraryScreening} object for the given result set.
   * The returned <code>LibraryScreening</code> has exactly the following properties synchronized:
   * <ul>
   * <li><code>screen</code>
   * <li><code>performedBy</code>
   * <li><code>dateCreated</code>
   * <li><code>dateOfVisit</code>
   * </ul>
   * 
   * @param resultSet the SQL result set to get the needed information to find an existing, or
   * create a new, LibraryScreening
   * @return the new or existing LibraryScreening
   * @throws SQLException
   * @throws ScreenDBSynchronizationException
   */
  private LibraryScreening findOrCreateLibraryScreening(ResultSet resultSet)
  throws SQLException, ScreenDBSynchronizationException {
    Integer screenNumber = resultSet.getInt("screen_id");
    Date dateCreated = resultSet.getDate("date_created");
    Date dateOfActivity = resultSet.getDate("date_of_visit");
    Screen screen = _screenSynchronizer.getScreenForScreenNumber(screenNumber);
    ScreeningRoomUser performedBy = getPerformedBy(resultSet);
    LibraryScreening screening = lookupExistingLibraryScreening(screen, performedBy, dateOfActivity);
    if (screening == null) {
      try {
        screening = new LibraryScreening(screen, performedBy, dateCreated, dateOfActivity);
      }
      catch (DuplicateEntityException e) {
        throw new ScreenDBSynchronizationException(
          "duplicate library screening for screen number " + screenNumber, e);
      }
    }
    return screening;
  }

  private ScreeningRoomUser getPerformedBy(ResultSet resultSet) throws SQLException {
    Integer screenDBUserId = resultSet.getInt("performed_by");
    if (screenDBUserId == null || screenDBUserId.equals(0)) {
      screenDBUserId = resultSet.getInt("lead_screener_id");
    }
    ScreeningRoomUser performedBy = _userSynchronizer.getScreeningRoomUserForScreenDBUserId(screenDBUserId);
    return performedBy;
  }

  private LibraryScreening lookupExistingLibraryScreening(
    Screen screen,
    ScreeningRoomUser performedBy,
    Date dateOfActivity)
  {
    Map<String,Object> libraryScreeningLookupProperties = new HashMap<String,Object>();
    libraryScreeningLookupProperties.put("screen", screen);
    libraryScreeningLookupProperties.put("hbnPerformedBy", performedBy);
    libraryScreeningLookupProperties.put("dateOfActivity", dateOfActivity);
    LibraryScreening screening = _dao.findEntityByProperties(LibraryScreening.class, libraryScreeningLookupProperties);
    return screening;
  }

  // TODO: synchronize volume transferred properly
  //
  // FROM GROUP DISCUSSION:
  //   2. Units for Volume of Compound Transferred: For one RNAi Screen (612), 
  //   I have unitless values "11" that I will take as "11 uL". For the rest, I 
  //   will take unitless values >= 12 to have nL as unit, and values <= 2.5 to 
  //   have uL as unit. The report includes a worksheet with all visits with 
  //   unitless values.
  //
  //   3. Products and Sums for Volume of Compound Transferred: For the 
  //   products, such as "2 X 40nL", 8 out of 28 have a "Number of Replicates" 
  //   that matches the unitless multiplicand. This is not a great match, so I 
  //   don't want to try to deduce much from that. The report includes a 
  //   worksheet with these visits, including my notes on how the Assay 
  //   Protocol jives with the values for Number of Replicates and Volume of 
  //   Compound Transferred. (In most cases, they do not jive very well. The 
  //   impression I got is that the assay protocols recorded were preliminary 
  //   versions, and were tweaked later on, so that the actual protocol for the
  //   visit differs in minor ways from the protocol recorded in ScreenDB.)
  //
  //   For the time being, when synchronizing, I will just carry out the 
  //   arithmetic, store the total volume transferred with nL units, and add a 
  //   note in the comments section containing the original value. If we manage 
  //   to clean this data up in ScreenDB, then the cleaned up data will 
  //   transfer to Screensaver as soon as the ScreenDBSynchronizer is run again.
  //
  //   I will also meet with Katrina the week after next to look these over. (I 
  //   am going to be on vacation next week.)
  private void synchronizeVolumeTransferredPerWell(ResultSet resultSet, LibraryScreening screening) throws SQLException
  {
    String volumeTransferredString = resultSet.getString("vol_of_compd_transf");  
    BigDecimal microliterVolumeTransferedPerWell = null;
    screening.setMicroliterVolumeTransferedPerWell(microliterVolumeTransferedPerWell);
  }

  // TODO: synchronize est final screen concentration properly
  //
  // FROM GROUP DISCUSSION:
  //   1. Estimated Final Screen Concentration: For the 4 sensible values that 
  //   we have, I will move these two the comments section (with date, 
  //   initials, and an explanation of what happened.) I will take the units to 
  //   be nM (for values 13 and 20).
  private void synchronizeEstimatedFinalScreenConcentration(ResultSet resultSet, LibraryScreening screening) throws SQLException
  {
    String estimatedFinalScreenConcentrationString = resultSet.getString("est_final_screen_conc");
    BigDecimal estimatedFinalScreenConcentration = null;
    screening.setEstimatedFinalScreenConcentrationInMoles(estimatedFinalScreenConcentration);
  }

  private void synchronizeAssayProtocolType(ResultSet resultSet, LibraryScreening screening) throws SQLException
  {
    String assayProtocolTypeString = resultSet.getString("assay_protocol_type");
    if (assayProtocolTypeString != null) {
      if (assayProtocolTypeString.equals("Preliminary")) {
        screening.setAssayProtocolType(AssayProtocolType.PRELIMINARY);
        return;
      }
      else if (
        assayProtocolTypeString.equals("Established") ||
        assayProtocolTypeString.equals("Protocol 1")) {
        screening.setAssayProtocolType(AssayProtocolType.ESTABLISHED);
        return;
      }
    }
    screening.setAssayProtocolType(null);
  }

}

