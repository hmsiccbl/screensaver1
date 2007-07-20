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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.CopyUsageType;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.PlateType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.libraries.WellName;
import edu.harvard.med.screensaver.model.screens.CherryPickAssayPlate;
import edu.harvard.med.screensaver.model.screens.CherryPickLiquidTransfer;
import edu.harvard.med.screensaver.model.screens.CherryPickRequest;
import edu.harvard.med.screensaver.model.screens.CompoundCherryPickRequest;
import edu.harvard.med.screensaver.model.screens.LabCherryPick;
import edu.harvard.med.screensaver.model.screens.LegacyCherryPickAssayPlate;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenerCherryPick;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;

public class CompoundCherryPickSynchronizer
{

  // static members

  private static Logger log = Logger.getLogger(CompoundCherryPickSynchronizer.class);
  

  // instance data members
  
  private Connection _connection;
  private GenericEntityDAO _dao;
  private LibrariesDAO _librariesDao;
  private UserSynchronizer _userSynchronizer;
  private ScreenSynchronizer _screenSynchronizer;

  
  // public constructors and methods

  public CompoundCherryPickSynchronizer(
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

  public void synchronizeCompoundCherryPicks() throws ScreenDBSynchronizationException
  {
    _dao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        try {
          loadOrCreateWellsForCherryPicks();
          synchronizeCompoundCherryPicksProper();
        }
        catch (SQLException e) {
          throw new ScreenDBSynchronizationException(
            "Encountered an SQL exception while synchronizing library screenings: " + e.getMessage(),
            e);
        }
      }
    });
  }


  private void loadOrCreateWellsForCherryPicks() throws SQLException
  {
    Set<Library> librariesToLoadOrCreateWellsFor = new HashSet<Library>();
    Statement statement = _connection.createStatement();
    ResultSet resultSet = statement.executeQuery(
      "SELECT DISTINCT plate FROM cherry_pick WHERE plate IS NOT NULL ORDER BY plate");
    while (resultSet.next()) {
      Integer sourcePlateNumber = resultSet.getInt("plate");
      Library library = _librariesDao.findLibraryWithPlate(sourcePlateNumber);
      assert(library != null);
      if (library != null) {
        librariesToLoadOrCreateWellsFor.add(library);
      }
    }
    for (Library library : librariesToLoadOrCreateWellsFor) {
      _librariesDao.loadOrCreateWellsForLibrary(library);
    }
  }

  // private instance methods

  private void synchronizeCompoundCherryPicksProper() throws SQLException, ScreenDBSynchronizationException
  {
    Statement statement = _connection.createStatement();
    ResultSet resultSet = statement.executeQuery(
      "SELECT * FROM visits v WHERE visit_type = 'Cherry Pick'");
    while (resultSet.next()) {
      Integer visitId = resultSet.getInt("id");
      BigDecimal volumeTransferred = getVolumeTransferred(visitId);
      CompoundCherryPickRequest request =
        createCompoundCherryPickRequest(resultSet, volumeTransferred);
      CherryPickLiquidTransfer liquidTransfer =
        createCherryPickLiquidTransfer(resultSet, volumeTransferred, request);
      CherryPickAssayPlate assayPlate =
        createCherryPickAssayPlate(visitId, request, liquidTransfer);
      createCherryPicks(visitId, request, assayPlate);
      _dao.persistEntity(request);
    }
    statement.close();
  }
  
  /**
   * Get a volume transferred value for the cherry pick request. Return the ScreenDB value
   * cherry_pick.liq_volumn only if all the cherry picks for the visit have the same volume.
   * Otherwise, return null.
   * 
   * TODO: ScreenDB visit 4602 has cherry picks with 3 different volumes. what to do in
   * this case? currently i just use a null volume everywhere. note this method could be
   * simpler (and less costly in db cycles) if i could assume that every cp for a single
   * visit had the same volume.
   */
  private BigDecimal getVolumeTransferred(Integer visitId) throws SQLException
  {
    BigDecimal volumeTransferred = null;
    PreparedStatement preparedStatement = _connection.prepareStatement(
      "SELECT DISTINCT(liq_volumn) FROM cherry_pick WHERE visit_id = ?\n" +
      "AND (SELECT COUNT(DISTINCT(liq_volumn)) FROM cherry_pick WHERE visit_id = ?) = 1");
    preparedStatement.setInt(1, visitId);
    preparedStatement.setInt(2, visitId);
    ResultSet resultSet = preparedStatement.executeQuery();
    while (resultSet.next()) {
      volumeTransferred = new BigDecimal(resultSet.getFloat("liq_volumn"));
      break;
    }
    preparedStatement.close();
    return volumeTransferred;
  }

  private CompoundCherryPickRequest createCompoundCherryPickRequest(ResultSet resultSet, BigDecimal volumeTransferred)
  throws SQLException
  {
    Screen screen = _screenSynchronizer.getScreenForScreenNumber(resultSet.getInt("screen_id"));
    ScreeningRoomUser requestedBy =
      _userSynchronizer.getScreeningRoomUserForScreenDBUserId(resultSet.getInt("performed_by"));
    Date dateRequested = resultSet.getDate("cherry_pick_request_date");
    if (dateRequested == null) {
      dateRequested = resultSet.getDate("date_of_visit");
    }
    Integer visitId = resultSet.getInt("id");
    CompoundCherryPickRequest request =
      new CompoundCherryPickRequest(screen, requestedBy, dateRequested, visitId);
    request.setMicroliterTransferVolumePerWellRequested(volumeTransferred);
    request.setMicroliterTransferVolumePerWellApproved(volumeTransferred);
    request.setComments(resultSet.getString("comments"));
    return request;
  }

  private CherryPickLiquidTransfer createCherryPickLiquidTransfer(
    ResultSet resultSet,
    BigDecimal volumeTransferred,
    CompoundCherryPickRequest request)
  throws SQLException
  {
    CherryPickLiquidTransfer liquidTransfer = new CherryPickLiquidTransfer(
      _userSynchronizer.getScreeningRoomUserForScreenDBUserId(resultSet.getInt("performed_by")),
      resultSet.getDate("date_created"),
      resultSet.getDate("date_of_visit"), // date_of_visit => CPLiquidTransfer.dateOfActivity
      request);
    liquidTransfer.setMicroliterVolumeTransferedPerWell(volumeTransferred);
    return liquidTransfer;
  }

  private CherryPickAssayPlate createCherryPickAssayPlate(
    Integer visitId,
    CompoundCherryPickRequest request,
    CherryPickLiquidTransfer liquidTransfer)
  throws SQLException
  {
    String filename = getCherryPickFilename(visitId);
    // TODO: is EPPENDORF the correct plate type here?
    LegacyCherryPickAssayPlate assayPlate = new LegacyCherryPickAssayPlate(request, 1, 0, PlateType.EPPENDORF, filename);
    assayPlate.setCherryPickLiquidTransfer(liquidTransfer);
    return assayPlate;
  }

  private String getCherryPickFilename(Integer visitId) throws SQLException
  {
    String cherryPickFilename = null;
    PreparedStatement preparedStatement = _connection.prepareStatement(
      "SELECT filename FROM cherry_pick_file WHERE visit_id = ?");
    preparedStatement.setInt(1, visitId);
    ResultSet resultSet = preparedStatement.executeQuery();
    while (resultSet.next()) {
      cherryPickFilename = resultSet.getString("filename");
      break;
    }
    preparedStatement.close();
    return cherryPickFilename;
  }
  
  private void createCherryPicks(
    Integer visitId,
    CherryPickRequest request,
    CherryPickAssayPlate assayPlate)
  throws SQLException
  {
    PreparedStatement preparedStatement = _connection.prepareStatement(
      "SELECT * FROM cherry_pick WHERE visit_id = ?");
    preparedStatement.setInt(1, visitId);
    ResultSet resultSet = preparedStatement.executeQuery();
    while (resultSet.next()) {
      String sourceWellName = resultSet.getString("well");
      Integer sourcePlateNumber = resultSet.getInt("plate");
      String destinationWell = resultSet.getString("map");
      String copyName = resultSet.getString("copy");
      // TODO: need to capture ScreenDB comments here as well!
      
      // i cant do shit if the source well name is null or not valid
      if (sourceWellName == null || ! Well.isValidWellName(sourceWellName)) {
        continue;
      }
      
      Well sourceWell = _librariesDao.findWell(new WellKey(sourcePlateNumber, sourceWellName));
      if (sourceWell == null) {
        log.error(
          "couldn't find well with plate number " + sourcePlateNumber + " and well name " + sourceWellName);
        continue;
      }
      
      ScreenerCherryPick screenerCherryPick = new ScreenerCherryPick(request, sourceWell);
      LabCherryPick labCherryPick = new LabCherryPick(screenerCherryPick, sourceWell);

      labCherryPick.setAllocated(getSourceCopy(sourceWell.getLibrary(), copyName));

      // i cant map it if the destination well name is null or not valid
      if (destinationWell == null || ! Well.isValidWellName(destinationWell)) {
        continue;
      }
      
      WellName destinationWellName = new WellName(destinationWell);
      labCherryPick.setMapped(assayPlate,
        destinationWellName.getRowIndex(),
        destinationWellName.getColumnIndex());
    }
    preparedStatement.close();
  }
  
  private Copy getSourceCopy(Library sourceLibrary, String copyName)
  {
    String copyId = sourceLibrary.getShortName() + ":" + copyName;
    Copy sourceCopy = _dao.findEntityById(Copy.class, copyId);
    if (sourceCopy == null) {
      sourceCopy = new Copy(sourceLibrary, CopyUsageType.FOR_CHERRY_PICK_SCREENING, copyName);
      _dao.persistEntity(sourceCopy);
    }
    return sourceCopy;
  }
}
