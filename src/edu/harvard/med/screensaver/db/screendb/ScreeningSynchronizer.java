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
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.model.screens.AssayProtocolType;
import edu.harvard.med.screensaver.model.screens.EquipmentUsed;
import edu.harvard.med.screensaver.model.screens.LibraryScreening;
import edu.harvard.med.screensaver.model.screens.PlatesUsed;
import edu.harvard.med.screensaver.model.screens.Screening;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;

abstract class ScreeningSynchronizer
{
  
  // private static fields
  
  private static Logger log = Logger.getLogger(ScreeningSynchronizer.class);
  
  
  // protected instance fields
  
  protected UserSynchronizer _userSynchronizer;
  protected Connection _connection;
  protected GenericEntityDAO _dao;
  protected LibrariesDAO _librariesDao;
  protected ScreenSynchronizer _screenSynchronizer;
  protected ScreenDBSynchronizationException _synchronizationException = null;
  protected Map<Integer,Screening> _screenDBVisitIdToScreeningMap = new HashMap<Integer,Screening>();

  
  // protected instance methods
  
  protected ScreeningRoomUser getPerformedBy(ResultSet resultSet) throws SQLException
  {
    Integer screenDBUserId = resultSet.getInt("performed_by");
    if (screenDBUserId == null || screenDBUserId.equals(0)) {
      screenDBUserId = resultSet.getInt("lead_screener_id");
    }
    return _userSynchronizer.getScreeningRoomUserForScreenDBUserId(screenDBUserId);
  }

  protected boolean getIsSpecial(ResultSet resultSet) throws SQLException
  {
    String visitType = resultSet.getString("visit_type");
    return visitType.equals("Special") || visitType.equals("Liquid Handling only");
  }

  protected void synchronizeVolumeTransferredPerWell(ResultSet resultSet, Screening screening)
  throws SQLException, ScreenDBSynchronizationException
  {
    final VisitVolumeTransferredPerWellParser parser = new VisitVolumeTransferredPerWellParser();
    String volumeTransferredString = resultSet.getString("vol_of_compd_transf");
    String screenType = resultSet.getString("screen_type");
    BigDecimal microliterVolumeTransferedPerWell =
      parser.getVolumeTransferredPerWell(volumeTransferredString, screenType);
    screening.setMicroliterVolumeTransferedPerWell(microliterVolumeTransferedPerWell);
  }

  // FROM GROUP DISCUSSION:
  //   1. Estimated Final Screen Concentration: For the 4 sensible values that 
  //   we have, I will move these two the comments section (with date, 
  //   initials, and an explanation of what happened.) I will take the units to 
  //   be nM (for values 13 and 20).
  protected void synchronizeEstimatedFinalScreenConcentration(
    ResultSet resultSet,
    Screening screening)
  throws SQLException
  {
    String estimatedFinalScreenConcentrationString = resultSet.getString("est_final_screen_conc");
    BigDecimal estimatedFinalScreenConcentration = null;
    if (estimatedFinalScreenConcentrationString != null) {
      if (estimatedFinalScreenConcentrationString.contains("13")) {
        estimatedFinalScreenConcentration = new BigDecimal(13);
      }
      else if (estimatedFinalScreenConcentrationString.contains("20")) {
        estimatedFinalScreenConcentration = new BigDecimal(20);
      }
    }
    screening.setEstimatedFinalScreenConcentrationInMoles(
      estimatedFinalScreenConcentration);
  }

  protected void synchronizeAssayProtocolType(ResultSet resultSet, Screening screening)
  throws SQLException
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

  protected void synchronizePlatesUsed() throws SQLException
  {
    Statement statement = _connection.createStatement();
    ResultSet resultSet = statement.executeQuery("SELECT * FROM plate_used");
    while (resultSet.next()) {
      Integer visitId = resultSet.getInt("visit_id");
      Integer startPlate = resultSet.getInt("start_plate");
      Integer endPlate = resultSet.getInt("end_plate");
      String copy = resultSet.getString("copy");
      LibraryScreening screening = (LibraryScreening) _screenDBVisitIdToScreeningMap.get(visitId);
      
      // ScreenDB has plate_used records unconnected to a visits[sic] record
      if (screening == null) {
        continue;
      }
      
      new PlatesUsed(screening, startPlate, endPlate, copy);
    }
    statement.close();
  }

  protected void synchronizeEquipmentUsed() throws SQLException
  {
    Statement statement = _connection.createStatement();
    ResultSet resultSet = statement.executeQuery("SELECT * FROM equip_used");
    while (resultSet.next()) {
      Integer visitId = resultSet.getInt("visit_id");
      String equipment = resultSet.getString("equipment");
      String protocol = resultSet.getString("protocol");
      String description = resultSet.getString("description");
  
      // screening == null when the equip_used is for the other type of Screening
      // (LibraryScreening or RNAiCherryPickScreening)
      Screening screening = _screenDBVisitIdToScreeningMap.get(visitId);
      if (screening != null) {
        new EquipmentUsed(screening, equipment, protocol, description);
      }
    }
    statement.close();
  }
}

