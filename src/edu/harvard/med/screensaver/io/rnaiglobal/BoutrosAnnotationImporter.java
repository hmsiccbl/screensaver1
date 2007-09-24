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
import edu.harvard.med.screensaver.model.DataModelViolationException;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screenresults.AnnotationValue;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.Study;
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

  private static Logger log = Logger.getLogger(BoutrosAnnotationImporter.class);

  private static final String RNAIGLOBAL_LOGIN = "rnaiglobal";
  private static final String RNAI_GLOBAL_EMAIL = "info@rnaiglobal.org";

  private static final int STUDY_NUMBER = 100000;
  private static final String STUDY_TITLE = "Sequence Annotation of the Dharmacon/Thermofisher siGENOME Whole Human Genome siRNA Library";
  private static final String STUDY_SUMMARY = "In-silico analysis of SMARTPool siRNA gene targets.";
  private static final String STUDY_URL = "http://www.dkfz.de/signaling2/siGENOME/";
  private static final Date STUDY_DATE = DateUtil.makeDate(2007, 6, 14);
  private static final String LAB_AFFILIATION_NAME = "DKFZ German Cancer Research Center";
  private static final String LAB_HEAD_EMAIL = "m.boutros@dkfz.de";
  private static final String SCREENER_EMAIL = "t.horn@dkfz.de";


  @SuppressWarnings("static-access")
  public static void main(String[] args) throws ParseException
  {
    CommandLineApplication app = new CommandLineApplication(args);
    app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withDescription("data file to import").withArgName("csv file").create("f"));
    app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withDescription("password for RNAi Global user").withArgName("password").create("rp"));
    app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withDescription("password for user accounts associated with study").withArgName("password").create("up"));
    if (!app.processOptions(true, true)) {
      System.exit(1);
    }

    final File file = app.getCommandLineOptionValue("f", File.class);
    if (!(file.exists() && file.canRead())) {
      throw new IllegalArgumentException(file + " is not readable");
    }

    final GenericEntityDAO dao = (GenericEntityDAO) app.getSpringBean("genericEntityDao");
    final String rnaiGlobalUserPassword = app.getCommandLineOptionValue("rp");
    final String studyUserAccountPassword = app.getCommandLineOptionValue("up");

    dao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        Study study = dao.findEntityByProperty(Screen.class,
                                               "hbnScreenNumber",
                                               STUDY_NUMBER);
        if (study != null) {
          log.info("deleting existing screen " + study);
          dao.deleteEntity(study);
        }

        deleteUser(SCREENER_EMAIL);
        deleteUser(LAB_HEAD_EMAIL);
        deleteUser(RNAI_GLOBAL_EMAIL);

        LabAffiliation labAffiliation = dao.findEntityByProperty(LabAffiliation.class,
                                                                 "affiliationName",
                                                                 LAB_AFFILIATION_NAME);
        if (labAffiliation != null) {
          if (labAffiliation.getScreeningRoomUsers().size() > 0) {
            log.warn("lab affiliation " + labAffiliation + " referenced by other users...not deleting");
          }
          else {
            log.info("deleting lab affiliation " + labAffiliation);
            dao.deleteEntity(labAffiliation);
          }
        }
      }

      private void deleteUser(String email)
      {
        ScreeningRoomUser user = dao.findEntityByProperty(ScreeningRoomUser.class,
                                                          "email",
                                                          email);
        if (user != null) {
          log.info("deleting existing user " + user);
          user.setLabAffiliation(null);
          dao.deleteEntity(user);
        }
      }
    });

    dao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        try {
          ScreeningRoomUser labHead =
            new ScreeningRoomUser(new Date(),
                                  "Michael",
                                  "Boutros",
                                  LAB_HEAD_EMAIL,
                                  "",
                                  "",
                                  "",
                                  "",
                                  "",
                                  ScreeningRoomUserClassification.UNASSIGNED,
                                  true);
          labHead.setLoginId("mboutros");
          labHead.updateScreensaverPassword(studyUserAccountPassword);
          labHead.addScreensaverUserRole(ScreensaverUserRole.GUEST_USER);

          ScreeningRoomUser leadScreener =
            new ScreeningRoomUser(new Date(),
                                  "Thomas",
                                  "Horn",
                                  SCREENER_EMAIL,
                                  "",
                                  "",
                                  "",
                                  "",
                                  "",
                                  ScreeningRoomUserClassification.UNASSIGNED,
                                  true);
          leadScreener.setLoginId("thorn");
          leadScreener.updateScreensaverPassword(studyUserAccountPassword);
          leadScreener.addScreensaverUserRole(ScreensaverUserRole.GUEST_USER);

          LabAffiliation labAffiliation = dao.findEntityByProperty(LabAffiliation.class,
                                                                   "affiliationName",
                                                                   LAB_AFFILIATION_NAME);
          if (labAffiliation == null) {
            labAffiliation = new LabAffiliation(LAB_AFFILIATION_NAME,
                                                AffiliationCategory.OTHER);
          }

          labHead.setLabAffiliation(labAffiliation);
          leadScreener.setLabAffiliation(labAffiliation);

          Screen screen = new Screen(leadScreener,
                                     labHead,
                                     STUDY_NUMBER,
                                     STUDY_DATE,
                                     ScreenType.RNAI,
                                     StudyType.IN_SILICO,
                                     STUDY_TITLE);
          screen.setSummary(STUDY_SUMMARY);
          screen.setUrl(STUDY_URL);
          screen.setShareable(true);
          screen.setDownloadable(false);

          ScreeningRoomUser rnaiGlobalMember =
            new ScreeningRoomUser(new Date(),
                                  "RNAi Global",
                                  "Member",
                                  RNAI_GLOBAL_EMAIL,
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

    List<AnnotationValueBuilder> builders = getAnnotationValueBuilder(screen, dao);
    for (int iRow = 1; iRow < sheet.getRows(); iRow++) {
      Cell[] row = sheet.getRow(iRow);
      for (AnnotationValueBuilder builder : builders) {
        builder.addAnnotationValue(row);
      }
      if (iRow % 100 == 0) {
        log.info("processed " + iRow + " rows");
      }
    }
    log.info("processed " + sheet.getRows() + " total rows ");
  }

  private static List<AnnotationValueBuilder> getAnnotationValueBuilder(final Screen screen,
                                                                        final GenericEntityDAO dao) {
    final List<AnnotationValueBuilder> builders = new ArrayList<AnnotationValueBuilder>();
    int ordinal = 0;
    int column = 1;
    builders.add(new AnnotationValueBuilderImpl(column++,
                                                new AnnotationType(screen,
                                                                   "siRNA IDs",
                                                                   "The Dharmacon/Thermofisher siRNA IDs of the individual duplexes that comprise the SMARTPool. " +
                                                                   "Concatenated by \"&\".",
                                                                   ordinal++,
                                                                   false)) {
      public String transformValue(String value) {
        return value.replaceAll("([&+])", " $1 ");
      }
    });
    builders.add(new AnnotationValueBuilderImpl(column++,
                                                new AnnotationType(screen,
                                                                   "Intended Target Gene Symbol",
                                                                   "Entrez Gene symbol of targeted gene, as originally annotated by Dharmacon/Thermofisher.",
                                                                   ordinal++,
                                                                   false)));
    final Integer finalOrdinal = ordinal++;
    builders.add(new AnnotationValueBuilder() {

      private AnnotationType _annotationType =
        new AnnotationType(screen,
                           "Intended Target Gene ID",
                           "Entrez Gene ID of targeted gene.  (This annotation type was added to the study by ICCB-L/Screensaver).",
                           finalOrdinal,
                           false);

      public AnnotationType getAnnotationType() {
        return _annotationType;
      }

      public void addAnnotationValue(Cell[] row)
      {
        String vendorIdentifier = row[0].getContents();
        List<Well> wells = dao.findEntitiesByProperty(Well.class, "vendorIdentifier", vendorIdentifier, true, "hbnSilencingReagents.gene");
        if (wells.size() == 0) {
          throw new DataModelViolationException("unknown vendor identifer " + vendorIdentifier);
        }
        Integer entrezId = wells.get(0).getGene().getEntrezgeneId();
        _annotationType.addAnnotationValue(new ReagentVendorIdentifier(DHARMACON_VENDOR_NAME, vendorIdentifier),
                                           entrezId.toString(),
                                           true);
      }
    });
    builders.add(new AnnotationValueBuilderImpl(column++,
                                                new AnnotationType(screen,
                                                                   "Intended RefSeq Targets",
                                                                   "The RefSeq transcript IDs that Dharmacon/Thermofisher intended to be targeted by the SMARTPool " +
                                                                   "and that was originally provided in the product documentation.",
                                                                   ordinal++,
                                                                   false)));
    builders.add(new AnnotationValueBuilderImpl(column++,
                                                new AnnotationType(screen,
                                                                   "ON-Target Modification",
                                                                   "Dharamcon's \"ON-Target\" flag, indicating whether chemical modification of the sense strand " +
                                                                   "siRNA has been performed to reduce off-target effects",
                                                                   ordinal++,
                                                                   false))
    {
      @Override
      public String transformValue(String value)
      {
        return Integer.parseInt(value) == 1 ?  "Yes" : "No";
      }
    });

    builders.add(new AnnotationValueBuilderImpl(column++,
                                                new AnnotationType(screen,
                                                                   "Entrez Gene IDs of Predicted Targets",
                                                                   "Entrez Gene IDs of genes that have been computationally predicted by this study to be targeted by at " +
                                                                   "least one siRNA duplex in the SMARTPool. Concatenated by \"&\".",
                                                                   ordinal++,
                                                                   false)) {
      public String transformValue(String value) {
        return value.replaceAll("GeneID:", "").replaceAll("([&+])", " $1 ");
      }
    });

    builders.add(new AnnotationValueBuilderImpl(column++,
                                                new AnnotationType(screen,
                                                                   "# Predicted Target Genes",
                                                                   "The number of genes that have been computationally predicted by this study to be targeted " +
                                                                   "by the SMARTPool.",
                                                                   ordinal++,
                                                                   true)));

    builders.add(new AnnotationValueBuilderImpl(column++,
                                                new AnnotationType(screen,
                                                                   "Predicted RefSeq Targets",
                                                                   "The RefSeq transcript IDs that have been computationally predicted by this study to be targeted " +
                                                                   "by the SMARTPool.  Transcripts derived from the same gene are concatenated by \"+\".  Transcripts " +
                                                                   "derived from different genes are concatenated by \"&\".  Transcript IDs have the same order as in \"" +
                                                                   builders.get(builders.size() - 2).getAnnotationType().getName() + "\" column.",
                                                                   ordinal++,
                                                                   false)) {
      public String transformValue(String value) {
        return value.replaceAll("([&+])", " $1 ");
      }
    });

    builders.add(new AnnotationValueBuilderImpl(column++,
                                                new AnnotationType(screen,
                                                                   "# Duplexes Targeting each RefSeq",
                                                                   "The number of duplex siRNAs from the SMARTPool that are predicted by this study to target each " +
                                                                   "RefSeq.  Ordered and concatenated as in \"" + builders.get(builders.size() - 1).getAnnotationType().getName() + "\" column.",
                                                                   ordinal++,
                                                                   false)) {
      public String transformValue(String value) {
        return value.replaceAll("([&+])", " $1 ");
      }
    });

    builders.add(new AnnotationValueBuilderImpl(column++,
                                                new AnnotationType(screen,
                                                                   "# RefSeq Target Transcripts",
                                                                   "The total number of RefSeq transcripts predicted by this study to be targeted by the SMARTPool.",
                                                                   ordinal++,
                                                                   true)));

    // note: excluding "Avg SMARTPool Efficiency" annotation due to controversial nature of this algorithm (per request of Laura Selfors, 2007-09-13)
    column++;

    builders.add(new AnnotationValueBuilderImpl(column++,
                                                new AnnotationType(screen,
                                                                   "Is Annotation Changed",
                                                                   "\"Yes\" if annotation from the Dharmacon/Thermofisher annotation, otherwise \"No\".",
                                                                   ordinal++,
                                                                   false)));

    builders.add(new AnnotationValueBuilderImpl(column++,
                                                new AnnotationType(screen,
                                                                   "Comments",
                                                                   "Comments on the annotation change.",
                                                                   ordinal++,
                                                                   false)));
    return builders;
  }
}
