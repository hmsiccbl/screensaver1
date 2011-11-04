// $HeadURL:
// http://seanderickson1@forge.abcd.harvard.edu/svn/screensaver/branches/iccbl/data-sharing-levels/src/edu/harvard/med/screensaver/io/screenresults/ScreenResultImporter.java
// $
// $Id: ConfirmedPositivesStudyCreator.java 5158 2011-01-06 14:26:53Z atolopko $
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.iccbl.screensaver.io.libraries;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;

import javax.mail.MessagingException;
import javax.swing.table.TableColumn;

import jxl.Workbook;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import org.apache.commons.cli.OptionBuilder;
import org.apache.log4j.Logger;

import edu.harvard.med.iccbl.screensaver.io.AdminEmailApplication;
import edu.harvard.med.screensaver.db.Criterion;
import edu.harvard.med.screensaver.db.Criterion.Operator;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;

/**
 * Copies to retire report<br/>
 * <br/>
 * Note: this utility will email the admin email address (as specified by the "-admin-" options), unless the "-noemail"
 * option is specified. <br/>
 * <br/>
 * For: [#2932] Create a "Copies to retire" report:<br/>
 * Weekly emailed report for small molecule, library screening copies that have non-retired plates that have exceeded 12
 * freeze/thaws.
 */
public class CopyFreezeThawReport extends AdminEmailApplication
{

  public CopyFreezeThawReport(String[] cmdLineArgs)
  {
    super(cmdLineArgs);
 }

  private static Logger log = Logger.getLogger(CopyFreezeThawReport.class);

  public static final String[] OPTION_FREEZE_THAW_THRESHOLD = { "threshold", "freeze-thaw-threshold", "freeze-thaw-threshold",
    "Number of freeze-thaws on a copy plate" };
  public static final String[] OPTION_OUTPUT_FILE = { "f", "file", "output-file",
    "Output file, extension \".xls\" will be added automatically." };

  public static final String[] TEST_ONLY = { "testonly", "", "test-only", "run the entire operation specified, then roll-back." };

  @SuppressWarnings("static-access")
  public static void main(String[] args) throws MessagingException
  {
    final CopyFreezeThawReport app = new CopyFreezeThawReport(args);

    app.setSpringConfigurationResource("spring-context-cmdline-copies-to-retire.xml");
    
    String[] option = OPTION_FREEZE_THAW_THRESHOLD;
    app.addCommandLineOption(OptionBuilder.hasArg().withType(Integer.class)
                                          .isRequired()
                                          .withArgName(option[ARG_INDEX])
                                          .withDescription(option[DESCRIPTION_INDEX])
                                          .withLongOpt(option[LONG_OPTION_INDEX])
                                          .create(option[SHORT_OPTION_INDEX]));

    option = OPTION_OUTPUT_FILE;
    app.addCommandLineOption(OptionBuilder.hasArg().withType(Integer.class)
                                          .isRequired()
                                          .withArgName(option[ARG_INDEX])
                                          .withDescription(option[DESCRIPTION_INDEX])
                                          .withLongOpt(option[LONG_OPTION_INDEX])
                                          .create(option[SHORT_OPTION_INDEX]));

    app.processOptions(/* acceptDatabaseOptions= */true,
                         /* acceptAdminUserOptions= */true);
    log.info("==== Running CopyFreezeThawReport: " + app.toString() + "======");

      // TODO: reinstate, once we figure out how to remove dependencies upon UI code
//    try {
//      AdministratorUser admin = app.findAdministratorUser();
//
//      String fileName = app.getCommandLineOptionValue(OPTION_OUTPUT_FILE[SHORT_OPTION_INDEX]);
//      int threshold = app.getCommandLineOptionValue(OPTION_FREEZE_THAW_THRESHOLD[SHORT_OPTION_INDEX], Integer.class);
//
//      GenericEntityDAO dao = (GenericEntityDAO) app.getSpringBean("genericEntityDao");
//      LibraryCopySearchResults lcsr = (LibraryCopySearchResults) app.getSpringBean("libraryCopiesBrowser2");
//
//      lcsr.searchAll();
//      BigDecimal bThreshold = new BigDecimal(threshold);
//      FixedDecimalColumn<Copy> column = (FixedDecimalColumn<Copy>) lcsr.getColumnManager().getColumn("Plate Screening Count Average");
//      column.addCriterion(new Criterion<BigDecimal>(Operator.GREATER_THAN_EQUAL, bThreshold));
//      column.setVisible(true);
//
//      TableColumn<Copy,ScreenType> column2 = (TableColumn<Copy,ScreenType>) lcsr.getColumnManager().getColumn("Screen Type");
//      column2.addCriterion(new Criterion<ScreenType>(Operator.EQUAL, ScreenType.SMALL_MOLECULE));
//      column2.setVisible(true);
//
//      //      TableColumn<Copy,PlateStatus> column3 = (TableColumn<Copy,PlateStatus>) lcsr.getColumnManager().getColumn("Primary Plate Status");
//      //      column3.addCriterion(new Criterion<PlateStatus>(Operator.LESS_THAN, PlateStatus.RETIRED));
//      //      column3.setVisible(true);
//
//      TableColumn<Copy,Integer> column3a = (TableColumn<Copy,Integer>) lcsr.getColumnManager().getColumn("Plates Available");
//      column3a.addCriterion(new Criterion<Integer>(Operator.GREATER_THAN, 0));
//      column3a.setVisible(true);
//
//      //TODO: katriana has asked about filtering a list of old libraries out; and to filter out "DOS" Library Types
//      TableColumn<Copy,LibraryType> column4 = (TableColumn<Copy,LibraryType>) lcsr.getColumnManager().getColumn("Library Type");
//      column4.addCriterion(new Criterion<LibraryType>(Operator.NOT_EQUAL, LibraryType.DOS));
//      column4.setVisible(true);
//
//      log.debug("starting exporting data for download, threshold " + threshold);
//
//      int rowCount = lcsr.getDataTableModel().getRowCount();
//      log.info("preparing to write: " + rowCount + " rows");
//      ExcelWorkbookDataExporter<Copy> dataExporter = new ExcelWorkbookDataExporter<Copy>("searchResult");
//      dataExporter.setTableColumns(lcsr.getColumnManager().getAllColumns()); //getVisibleColumns());
//
//      ByteArrayOutputStream bos = new ByteArrayOutputStream();
//
//      WritableWorkbook workbook = Workbook.createWorkbook(bos);
//      dataExporter.writeWorkbook(workbook, lcsr.getDataTableModel().iterator());
//      workbook.write();
//      workbook.close();
//
//      File file = new File(fileName + ".xls"); //TODO: if the email option is used, then this file should default to an email attachment
//      if (file.exists()) {
//        log.warn("file: " + file + ", exists, overwriting");
//      }
//      FileOutputStream fos = new FileOutputStream(file);
//      bos.writeTo(fos);
//      fos.close();
//      log.info("finished exporting data to: " + file.getCanonicalPath());
//
//      String subject = "Small Molecule Freeze Thaw Report";
//      String msg = "Small Molecule Libraries Freeze Thaw report:\n"
//        + "Found " + rowCount + " copies that match the criteria:\n" +
//            "- Plate Screening Count Average  >= " + threshold + " \n" +
//            "- has \"Available\" Plates\n" +
//            "- Libary Type != \"DOS\"\n" +
//            "Please see the attached file for the full report.";
//      log.info(msg);
//      app.sendAdminEmails(subject, msg, file);
//
//    }
//    catch (IOException e) {
//      app.sendErrorMail("CopyFreezeThawReport: Error exporting", app.toString(), e);
//      System.exit(1); // error
//    }
//    catch (RowsExceededException e) {
//      app.sendErrorMail("CopyFreezeThawReport: too many rows to export as a workbook ", app.toString(), e);
//      System.exit(1); // error
//    }
//    catch (WriteException e) {
//      app.sendErrorMail("CopyFreezeThawReport: Error exporting", app.toString(), e);
//      System.exit(1); // error
//    }
//    catch (MessagingException e) {
//      app.sendErrorMail("CopyFreezeThawReport: Error mailing", app.toString(), e);
//      System.exit(1); // error
//    }
//    log.info("==== Running CopyFreezeThawReport: " + app.toString() + "======");
  }
}