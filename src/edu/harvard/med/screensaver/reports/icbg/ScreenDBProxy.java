// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.reports.icbg;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;


/**
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class ScreenDBProxy
{
  
  // static stuff
  
  private static Logger log = Logger.getLogger(ICBGReportGenerator.class);

  static {
    try {
      Class.forName("org.postgresql.Driver");
    }
    catch (ClassNotFoundException e) {
      log.error("couldn't find postgresql driver");
    }    
  }

  
  // instance fields
  
  private Connection _connection;
  
  public ScreenDBProxy()
  {
    try {
      _connection = DriverManager.getConnection(
        "jdbc:postgresql://localhost/screendb",
        "screendbweb",
        "screendbweb");
    }
    catch (SQLException e) {
      log.error("couldnt connection to database: " + e.getMessage());
      e.printStackTrace();
    }
  }
  
  public AssayInfo getAssayInfoForScreen(Integer screenID)
  {
    AssayInfo assayInfo = new AssayInfo();
    String sql =
      "SELECT\n" +
      "  s.id, s.screen_title, s.summary, h.last, s.keywords, s.summary\n" +
      "FROM screens s, users l, users h\n" +
      "WHERE\n" +
      "  s.id = " + screenID + " AND\n" +
      "  s.user_id = l.id AND\n" +
      "  l.lab_name = h.id\n";
    String sql2 =
      "SELECT MAX(status_date) FROM screen_status WHERE screen_id = " +
      screenID;
    String assayCategoryText = null;
    try {
      Statement statement = _connection.createStatement();
      ResultSet resultSet = statement.executeQuery(sql);
      resultSet.next();
      assayInfo.setAssayName(
        "ICCBL" + resultSet.getString(1));
      assayInfo.setProtocolDescription(
        resultSet.getString(2));
      assayInfo.setPNote(
        resultSet.getString(3));
      assayInfo.setInvestigator(
        resultSet.getString(4).toUpperCase());

      assayCategoryText = "";
      String keywords =  resultSet.getString(5);
      if (keywords != null) {
        assayCategoryText = keywords.toUpperCase() + " ";
      }
      String summary = resultSet.getString(6);
      if (summary != null) {
        assayCategoryText += summary.toUpperCase();
      }

      statement.close();

      statement = _connection.createStatement();
      resultSet = statement.executeQuery(sql2);
      resultSet.next();
      assayInfo.setAssayDate(resultSet.getString(1));
      statement.close();
    }
    catch (SQLException e) {
      log.error("sql error: " + e.getMessage());
      e.printStackTrace();
    }
    
    setAssayCategory(assayInfo, assayCategoryText);
    return assayInfo;
  }

  private void setAssayCategory(AssayInfo assayInfo, String assayCategoryText)
  {
    if (
      assayCategoryText.contains("MALARIA")) {
      assayInfo.setAssayCategory("ANTI-MALARIA");
    }
    else if (
      assayCategoryText.contains("DIABETES") ||
      assayCategoryText.contains("OBESITY")) {
      assayInfo.setAssayCategory("OBESITY_DIABETES");
    }
    else if (
      assayCategoryText.contains("TRYPANOSOMIASIS")) {
      assayInfo.setAssayCategory("TRYPANOSOMIASIS");
    }
    else if (
      assayCategoryText.contains("LEISHMANIASIS")) {
      assayInfo.setAssayCategory("LEISHMANIASIS");
    }
    else if (
      assayCategoryText.contains("INFLAM")) {
      assayInfo.setAssayCategory("INFLAMMATION");
    }
    else if (
      assayCategoryText.contains("CYTOTOXIC")) {
      assayInfo.setAssayCategory("CYTOTOXICITY");
    }
    else if (
      assayCategoryText.contains("CONTRACEPTION")) {
      assayInfo.setAssayCategory("CONTRACEPTION");
    }
    else if (
      assayCategoryText.contains("CHAGAS")) {
      assayInfo.setAssayCategory("CHAGAS_DISEASE");
    }
    else if (
      assayCategoryText.contains("CARDIOVASC")) {
      assayInfo.setAssayCategory("CARDIOVASCULAR");
    }
    else if (
      assayCategoryText.contains("CANCER")) {
      assayInfo.setAssayCategory("ANTI-CANCER");
    }
    else if (
      assayCategoryText.contains("TUBERCULOSIS")) {
      assayInfo.setAssayCategory("ANTI-TUBERCULOSIS");
    }
    else if (
      assayCategoryText.contains("FUNG")) {
      assayInfo.setAssayCategory("ANTI-FUNGAL");
    }
    else if (
      assayCategoryText.contains("ANTHRAX") ||
      assayCategoryText.contains("BOTULINUM") ||
      assayCategoryText.contains("TULARENSIS") ||
      assayCategoryText.contains("TULARENSIS") ||
      assayCategoryText.contains("CHOLER") ||
      assayCategoryText.contains("AERUGINOSA") ||
      assayCategoryText.contains("YERSINIA PESTIS") ||
      assayCategoryText.contains("AMINOARABINOSE") ||
      assayCategoryText.contains("BACTERI")) {
      assayInfo.setAssayCategory("ANTI-BACTERIAL");
    }
    else if (
      assayCategoryText.contains("INFLUENZA") ||
      assayCategoryText.contains("VACCINIA") ||
      assayCategoryText.contains("WEST NILE") ||
      assayCategoryText.contains("VIRUS")) {
      assayInfo.setAssayCategory("ANTI-VIRAL_NO_HIV");
    }
    else if (
      assayCategoryText.contains("AIDS") ||
      assayCategoryText.contains("HIV")) {
      assayInfo.setAssayCategory("ANTI-HIV_AIDS");
    }
    else {
      assayInfo.setAssayCategory("OTHER");
    }
  }
}
