//$HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
//$Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $

//Copyright 2006 by the President and Fellows of Harvard College.

//Screensaver is an open-source project developed by the ICCB-L and NSRB labs
//at Harvard Medical School. This software is distributed under the terms of
//the GNU General Public License.

package edu.harvard.med.screensaver.io.rnaiglobal;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

  private static final String SCREENER_LAB_AFFILIATION_NAME = "DKFZ German Cancer Research Center";
  private static final String RNAIGLOBAL_LOGIN = "rnaiglobal";
  private static final Date SCREEN_DATE = DateUtil.makeDate(2007, 6, 14);
  private static final String SCREENER_EMAIL = "m.boutros@dkfz.de";
  private static final String SCREEN_TITLE = "Sequence Annotation of the Dharmacon/Thermofisher siGENOME Whole Human Genome siRNA Library";
  private static final int SCREEN_NUMBER = 100000;
  protected static final String RNAI_GLOBAL_EMAIL = "info@rnaiglobal.org";
  // http://www.dkfz.de/index.html
  private static Logger log = Logger.getLogger(BoutrosAnnotationImporter.class);

  @SuppressWarnings("static-access")
  public static void main(String[] args) throws ParseException
  {
    CommandLineApplication app = new CommandLineApplication(args);
    app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withDescription("data file to import").withArgName("csv file").create("f"));
    app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withDescription("password for RNAi Global user").withArgName("password").create("p"));
    if (!app.processOptions(true, true)) {
      System.exit(1);
    }

    final File file = app.getCommandLineOptionValue("f", File.class);
    if (!(file.exists() && file.canRead())) {
      throw new IllegalArgumentException(file + " is not readable");
    }

    final GenericEntityDAO dao = (GenericEntityDAO) app.getSpringBean("genericEntityDao");
    final String rnaiGlobalUserPassword = app.getCommandLineOptionValue("p");

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
          screen.setSummary("In-silico analysis of SMARTPool siRNA gene targets.");

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
          rnaiGlobalMember.setDigestedPassword(CryptoUtils.digest(rnaiGlobalUserPassword));
          rnaiGlobalMember.addScreensaverUserRole(ScreensaverUserRole.GUEST_USER);
          dao.persistEntity(rnaiGlobalMember);

          importAnnotationData(screen, file, dao);
          dao.persistEntity(screen);
        }
        catch (Exception e) {
          throw new DAOTransactionRollbackException(e);
        }
      }
    });
  }

  static private void importAnnotationData(Screen screen,
                                           File file,
                                           GenericEntityDAO dao)
  throws FileNotFoundException
  {
    ParseErrorManager errors = new ParseErrorManager();
    Workbook workbook = new Workbook(file, errors);
    Sheet sheet = workbook.getWorkbook().getSheet(0);

    List<AnnotationValueBuilder> builders = getAnnotationValueBuilder(screen);
    for (int iRow = 1; iRow < sheet.getRows(); iRow++) {
      Cell[] row = sheet.getRow(iRow);
      for (AnnotationValueBuilder builder : builders) {
        builder.buildAnnotationValue(row);
      }
      if (iRow % 100 == 0) {
        log.info("processed " + iRow + " rows");
      }
    }
    log.info("processed " + sheet.getRows() + " total rows ");
  }

  private static List<AnnotationValueBuilder> getAnnotationValueBuilder(Screen screen)
  {
    List<AnnotationValueBuilder> builders = new ArrayList<AnnotationValueBuilder>();
    builders.add(new AnnotationValueBuilder(builders.size() + 1,
                                            new AnnotationType(screen,
                                                               "siRNA IDs",
                                                               "The Dharmacon/Thermofisher siRNA IDs of the individual duplexes that comprise the SMARTPool. " +
                                                               "Concatenated by \"&\".",
                                                               builders.size(),
                                                               false)) {
      public String transformValue(String value) {
        return value.replaceAll("([&+])", " $1 ");
      }
    });
    builders.add(new AnnotationValueBuilder(builders.size() + 1,
                                            new AnnotationType(screen,
                                                               "Intended Target Gene",
                                                               "Original Dharmacon/Thermofisher annotation.",
                                                               builders.size(),
                                                               false)));
    builders.add(new AnnotationValueBuilder(builders.size() + 1,
                                            new AnnotationType(screen,
                                                               "Intended RefSeq Targets",
                                                               "The RefSeq transcript IDs that Dharmacon/Thermofisher intended to be targeted by the SMARTPool " +
                                                               "and that was originally provided in the product documentation.",
                                                               builders.size(),
                                                               false)));
    builders.add(new AnnotationValueBuilder(builders.size() + 1,
                                            new AnnotationType(screen,
                                                               "ON-Target Flag",
                                                               "Dharamcon's \"ON-Target\" flag",
                                                               builders.size(),
                                                               false)));

    builders.add(new AnnotationValueBuilder(builders.size() + 1,
                                            new AnnotationType(screen,
                                                               "Entrez Gene IDs of Predicted Targets",
                                                               "Entrez Gene IDs of genes that have been computationally predicted by this study to be targeted by at " +
                                                               "least one siRNA duplex in the SMARTPool. Concatenated by \"&\".",
                                                               builders.size(),
                                                               false)) {
      public String transformValue(String value) {
        return value.replaceAll("GeneID:", "").replaceAll("([&+])", " $1 ");
      }
    });

    builders.add(new AnnotationValueBuilder(builders.size() + 1,
                                            new AnnotationType(screen,
                                                               "# Predicted Target Genes",
                                                               "The number of genes that have been computationally predicted by this study to be targeted " +
                                                               "by the SMARTPool.",
                                                               builders.size(),
                                                               true)));

    builders.add(new AnnotationValueBuilder(builders.size() + 1,
                                            new AnnotationType(screen,
                                                               "Predicted RefSeq Targets",
                                                               "The RefSeq transcript IDs that have been computationally predicted by this study to be targeted " +
                                                               "by the SMARTPool.  Transcripts derived from the same gene are concatenated by \"+\".  Transcripts " +
                                                               "derived from different genes are concatenated by \"&\".  Transcript IDs have the same order as in \"" +
                                                               builders.get(builders.size() - 2).getAnnotationType().getName() + "\" column.",
                                                               builders.size(),
                                                               false)) {
      public String transformValue(String value) {
        return value.replaceAll("([&+])", " $1 ");
      }
    });

    builders.add(new AnnotationValueBuilder(builders.size() + 1,
                                            new AnnotationType(screen,
                                                               "# Duplexes Targeting each RefSeq",
                                                               "The number of duplex siRNAs from the SMARTPool that are predicted by this study to target each " +
                                                               "RefSeq.  Ordered and concatenated as in \"" + builders.get(builders.size() - 1).getAnnotationType().getName() + "\" column.",
                                                               builders.size(),
                                                               false)) {
      public String transformValue(String value) {
        // TODO: not working!!!
        return value.replaceAll("([&+])", " $1 ");
      }
    });

    builders.add(new AnnotationValueBuilder(builders.size() + 1,
                                            new AnnotationType(screen,
                                                               "# RefSeq Target Transcripts",
                                                               "The total number of RefSeq transcripts predicted by this study to be targeted by the SMARTPool.",
                                                               builders.size(),
                                                               true)));

    // note: excluding "Avg SMARTPool Efficiency" annotation due to controversial nature of this algorithm (per request of Laura Selfors, 2007-09-13)

    builders.add(new AnnotationValueBuilder(builders.size() + 2,
                                            new AnnotationType(screen,
                                                               "Is Annotation Changed",
                                                               "\"Yes\" if annotation from the Dharmacon/Thermofisher annotation, otherwise \"No\".",
                                                               builders.size(),
                                                               false)));

    builders.add(new AnnotationValueBuilder(builders.size() + 2,
                                            new AnnotationType(screen,
                                                               "Comments",
                                                               "Comments on the annotation change.",
                                                               builders.size(),
                                                               false)));
    return builders;
  }
}
