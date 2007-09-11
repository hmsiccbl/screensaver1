//$HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
//$Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $

//Copyright 2006 by the President and Fellows of Harvard College.

//Screensaver is an open-source project developed by the ICCB-L and NSRB labs
//at Harvard Medical School. This software is distributed under the terms of
//the GNU General Public License.

package edu.harvard.med.screensaver.io.rnaiglobal;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.util.Date;

import jxl.Cell;
import jxl.Sheet;

import edu.harvard.med.screensaver.CommandLineApplication;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.DAOTransactionRollbackException;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.io.workbook2.ParseErrorManager;
import edu.harvard.med.screensaver.io.workbook2.Workbook;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.StudyType;
import edu.harvard.med.screensaver.model.users.AffiliationCategory;
import edu.harvard.med.screensaver.model.users.LabAffiliation;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUserClassification;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.util.CryptoUtils;
import edu.harvard.med.screensaver.util.DateUtil;

import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

public class BoutrosAnnotationImporter
{
  // static members

  private static final String SCREENER_LAB_AFFILIATION_NAME = "German Cancer Research Center";
  private static final String RNAIGLOBAL_LOGIN = "rnaiglobal";
  private static final String RNAI_GLOBAL_PASSWORD = "rna1global";
  private static final Date SCREEN_DATE = DateUtil.makeDate(2007, 6, 14);
  private static final String SCREENER_EMAIL = "m.boutros@dkfz.de";
  private static final String SCREEN_TITLE = "Sequence Analysis of siGENOME";
  private static final int SCREEN_NUMBER = 69120;
  protected static final String RNAI_GLOBAL_EMAIL = "info@rnaiglobal.org";
  private static Logger log = Logger.getLogger(BoutrosAnnotationImporter.class);

  @SuppressWarnings("static-access")
  public static void main(String[] args) throws ParseException
  {
    CommandLineApplication app = new CommandLineApplication(args);
    app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withDescription("data file to import").withArgName("csv file").create("f"));
    if (!app.processOptions(true, true)) {
      System.exit(1);
    }

    final File file = app.getCommandLineOptionValue("f", File.class);
    if (!(file.exists() && file.canRead())) {
      throw new IllegalArgumentException(file + " is not readable");
    }

    final GenericEntityDAO dao = (GenericEntityDAO) app.getSpringBean("genericEntityDao");

    dao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        Screen screen = dao.findEntityByProperty(Screen.class,
                                                 "hbnScreenNumber",
                                                 SCREEN_NUMBER);
        if (screen != null) {
          log.info("deleting existing screen");
          dao.deleteEntity(screen);
        }
        ScreeningRoomUser boutrosScreener = dao.findEntityByProperty(ScreeningRoomUser.class,
                                                                     "email",
                                                                     SCREENER_EMAIL);
        if (boutrosScreener != null) {
          log.info("deleting existing lead screener");
          dao.deleteEntity(boutrosScreener);
          LabAffiliation labAffiliation = dao.findEntityByProperty(LabAffiliation.class,
                                                                   "affiliationName",
                                                                   SCREENER_LAB_AFFILIATION_NAME);
          if (labAffiliation != null) {
            log.info("deleting lab affiliation");
            dao.deleteEntity(labAffiliation);
          }
        }
        ScreeningRoomUser rnaiGlobalMember = dao.findEntityByProperty(ScreeningRoomUser.class,
                                                                      "email",
                                                                      RNAI_GLOBAL_EMAIL);
        if (rnaiGlobalMember != null) {
          log.info("deleting existing RNAi Global group account");
          dao.deleteEntity(rnaiGlobalMember);
        }
      }
    });

    dao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        try {
          ScreeningRoomUser boutrosScreener =
            new ScreeningRoomUser(new Date(),
                                  "Michael",
                                  "Boutros",
                                  SCREENER_EMAIL,
                                  "",
                                  "",
                                  "",
                                  "",
                                  "",
                                  ScreeningRoomUserClassification.UNASSIGNED,
                                  true);
          LabAffiliation labAffiliation = new LabAffiliation(SCREENER_LAB_AFFILIATION_NAME,
                                                             AffiliationCategory.OTHER);
          boutrosScreener.setLabAffiliation(labAffiliation);
          Screen screen = new Screen(boutrosScreener,
                                     boutrosScreener,
                                     SCREEN_NUMBER,
                                     SCREEN_DATE,
                                     ScreenType.RNAI,
                                     StudyType.IN_SILICO,
                                     SCREEN_TITLE);
          screen.setSummary("In-silico analysis of SMARTPool siRNA gene targets.  This is a pseudo-screen.");

          ScreeningRoomUser rnaiGlobalMember =
            new ScreeningRoomUser(new Date(),
                                  "RNAi Global",
                                  "Member",
                                  "",
                                  "",
                                  "",
                                  "RNAi Global group account",
                                  "",
                                  "",
                                  ScreeningRoomUserClassification.UNASSIGNED,
                                  true);
          rnaiGlobalMember.setLoginId(RNAIGLOBAL_LOGIN);
          rnaiGlobalMember.setDigestedPassword(CryptoUtils.digest(RNAI_GLOBAL_PASSWORD));
          rnaiGlobalMember.addScreensaverUserRole(ScreensaverUserRole.RNAI_SCREENING_ROOM_USER);
          screen.addCollaborator(rnaiGlobalMember);

          importScreenResultData(screen, file, dao);
          dao.persistEntity(screen);
        }
        catch (Exception e) {
          throw new DAOTransactionRollbackException(e);
        }
      }
    });
  }

  static private void importScreenResultData(Screen screen,
                                             File file,
                                             GenericEntityDAO dao)
  throws FileNotFoundException
  {

    AnnotationType[] annot = new AnnotationType[12];
    int ordinal = 0;
    annot[ordinal] = new AnnotationType(screen,
                                        "siRNA IDs",
                                        "The siRNA Ids belonging to the SMARTPool. Concatenated by \"&\"",
                                        ordinal,
                                        false);
    ++ordinal;
    annot[ordinal] = new AnnotationType(screen,
                                        "Intended Target Gene",
                                        "Original Dharmacon annotation",
                                        ordinal,
                                        false);
    ++ordinal;
    annot[ordinal] = new AnnotationType(screen,
                                        "Intended RefSeq Targets",
                                        "The RefSeq transcript IDs that Dharmacon intended to be targeted by the SMARTPool",
                                        ordinal,
                                        false);
    ++ordinal;
    annot[ordinal] = new AnnotationType(screen,
                                        "ON-Target Flag",
                                        "Dharamcon's \"ON-Target\" flag",
                                        ordinal,
                                        false);
    ++ordinal;
    annot[ordinal] = new AnnotationType(screen,
                                        "Calculated Target Genes",
                                        "Gene symbols of genes that have been calculated to be targeted by at least one siRNAI of the SMARTPool. Concatenated by \"&\"",
                                        ordinal,
                                        false);
    ++ordinal;
    annot[ordinal] = new AnnotationType(screen,
                                        "# Calculated Target Genes",
                                        "The predicted number of genes that have been calculated to be targeted by the SMARTPool",
                                        ordinal,
                                        true);
    ++ordinal;
    annot[ordinal] = new AnnotationType(screen,
                                        "Calculated RefSeq Targets",
                                        "The RefSeq transcript IDs that have been calculated to be targeted by the SMARTPool.  Transcripts derived from the same gene are concatenated by \"+\".  Transcripts derived from different genes are concatenated by \"&\".  Transcript IDs have the same order as in \"" + annot[ordinal - 2].getName() + "\"",
                                        ordinal,
                                        false);
    ++ordinal;
    annot[ordinal] = new AnnotationType(screen,
                                        "Hits of SMARTPool siRNAs",
                                        "The number of hits for each respective RefSeq transcript target in \"" + annot[ordinal - 1].getName() + "\"",
                                        ordinal, false);
    ++ordinal;
    annot[ordinal] = new AnnotationType(screen,
                                        "# RefSeq Target Transcripts",
                                        "The total number of RefSeq transcripts target by at least one siRNA in the SMARTPool",
                                        ordinal,
                                        true);
    ++ordinal;
    annot[ordinal] = new AnnotationType(screen,
                                        "Avg SMARTPool Efficiency",
                                        "The averaged SMARTPool efficiency (according to Reynolds, et. al.)",
                                        ordinal,
                                        true);
    ++ordinal;
    annot[ordinal] = new AnnotationType(screen,
                                        "Is Annotation Changed",
                                        "\"Yes\" if annotation changed from Dharmacon's original annotation, otherwise \"No\"",
                                        ordinal,
                                        false);
    ++ordinal;
    annot[ordinal] = new AnnotationType(screen,
                                        "Summary",
                                        "A summary of the annotation change",
                                        ordinal,
                                        false);

    ParseErrorManager errors = new ParseErrorManager();
    Workbook workbook = new Workbook(file, errors);
    Sheet sheet = workbook.getWorkbook().getSheet(0);

    for (int iRow = 1; iRow < sheet.getRows(); iRow++) {
      Cell[] row = sheet.getRow(iRow);
      Cell vendorIdCell = row[0];
      String vendorId = vendorIdCell.getContents();
      for (int i = 0; i < annot.length; i++) {
        Cell cell = row[i + 1];
        String value = cell.getContents();
        AnnotationType annotation = annot[i];
        value = value.replaceAll("&", " & ").replaceAll("\\+", " + ");
        annotation.addAnnotationValue(vendorId,
                                      (annotation.isNumeric() ? null : value),
                                      (annotation.isNumeric() ? new BigDecimal(value) : null));
      }
      if (iRow % 100 == 0) {
        log.info("processed " + iRow + " rows");
      }
    }
  }
}
