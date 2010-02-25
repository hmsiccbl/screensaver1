//$HeadURL$
//$Id$

//Copyright 2006 by the President and Fellows of Harvard College.

//Screensaver is an open-source project developed by the ICCB-L and NSRB labs
//at Harvard Medical School. This software is distributed under the terms of
//the GNU General Public License.

package edu.harvard.med.iccbl.screensaver.io.rnaiglobal;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import jxl.Cell;
import jxl.Sheet;

import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.CommandLineApplication;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.DAOTransactionRollbackException;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.io.workbook2.Workbook;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.Study;
import edu.harvard.med.screensaver.model.screens.StudyType;
import edu.harvard.med.screensaver.model.users.AffiliationCategory;
import edu.harvard.med.screensaver.model.users.LabAffiliation;
import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;

public class BoutrosAnnotationImporter
{

  // static members

  private static Logger log = Logger.getLogger(BoutrosAnnotationImporter.class);

  private static final String RNAIGLOBAL_LOGIN = "rnaiglobal";
  private static final String RNAI_GLOBAL_EMAIL = "info@rnaiglobal.org";

  private static final int STUDY_NUMBER = 100000;
  private static final String STUDY_TITLE = "Sequence Annotation of the Dharmacon/Thermofisher siGENOME Whole Human Genome siRNA Library";
  private static final String STUDY_SUMMARY = "In-silico analysis of SMARTPool siRNA gene targets, using RefSeq release 23";
  private static final String STUDY_URL = "http://www.dkfz.de/signaling2/siGENOME/";
  private static final String LAB_AFFILIATION_NAME = "DKFZ German Cancer Research Center";
  private static final String LAB_HEAD_EMAIL = "m.boutros@dkfz.de";
  private static final String SCREENER_EMAIL = "t.horn@dkfz.de";


  @SuppressWarnings("static-access")
  public static void main(String[] args) throws ParseException
  {
    CommandLineApplication app = new CommandLineApplication(args);
    app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withDescription("data file to import").withArgName("Excel workbook file").create("f"));
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
    final LibrariesDAO librariesDao = (LibrariesDAO) app.getSpringBean("librariesDao");
    final String rnaiGlobalUserPassword = app.getCommandLineOptionValue("rp");
    final String studyUserAccountPassword = app.getCommandLineOptionValue("up");

    dao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        Study study = dao.findEntityByProperty(Screen.class,
                                               "screenNumber",
                                               STUDY_NUMBER);
        if (study != null) {
          log.info("deleting existing screen " + study);
          if (study.getLabHead() != null) {
            study.getLabHead().getScreensHeaded().remove(study);
          }
          study.getLeadScreener().getScreensLed().remove(study);
          dao.deleteEntity(study);
        }
      }
    });

    dao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        try {
          LabAffiliation labAffiliation = findOrCreateLabAffiliation(dao);
          LabHead labHead = (LabHead)
          findOrCreateUser("Michael",
                           "Boutros",
                           LAB_HEAD_EMAIL,
                           "mboutros",
                           studyUserAccountPassword,
                           true,
                           labAffiliation,
                           dao);
          ScreeningRoomUser leadScreener = findOrCreateUser("Thomas",
                                                            "Horn",
                                                            SCREENER_EMAIL,
                                                            "thorn",
                                                            studyUserAccountPassword,
                                                            false,
                                                            null,
                                                            dao);
          leadScreener.setLab(labHead.getLab());
          Screen screen = new Screen(leadScreener,
                                     labHead,
                                     STUDY_NUMBER,
                                     ScreenType.RNAI,
                                     StudyType.IN_SILICO,
                                     STUDY_TITLE);
          screen.setSummary(STUDY_SUMMARY);
          screen.setUrl(STUDY_URL);

          ScreeningRoomUser rnaiGlobalMember = findOrCreateUser("RNAi Global",
                                                                "Member",
                                                                RNAI_GLOBAL_EMAIL,
                                                                RNAIGLOBAL_LOGIN,
                                                                rnaiGlobalUserPassword,
                                                                false,
                                                                null,
                                                                dao);
          rnaiGlobalMember.setComments("RNAi Global group account");
          dao.saveOrUpdateEntity(rnaiGlobalMember);

          importAnnotationData(screen, file, librariesDao);
          dao.saveOrUpdateEntity(screen);
        }
        catch (Exception e) {
          throw new DAOTransactionRollbackException(e);
        }
      }
    });
  }

  static private ScreeningRoomUser findOrCreateUser(String firstName,
                                                    String lastName,
                                                    String email,
                                                    String loginId,
                                                    String password,
                                                    boolean isLabHead,
                                                    LabAffiliation labAffiliation,
                                                    GenericEntityDAO dao)
  {
    ScreeningRoomUser user = dao.findEntityByProperty(ScreeningRoomUser.class,
                                                      "email",
                                                      email);
    if (user == null) {
      if (isLabHead) {
        user = new LabHead(firstName, lastName, labAffiliation);
      }
      else {
        user = new ScreeningRoomUser(firstName, lastName);
      }
      user.setEmail(email);
      user.setLoginId(loginId);
      user.updateScreensaverPassword(password);
      user.addScreensaverUserRole(ScreensaverUserRole.SCREENSAVER_USER);
    }
    else {
      log.warn("user with email " + email +
      " already exists and will be associated with imported study");
    }
    return user;
  }

  private static LabAffiliation findOrCreateLabAffiliation(GenericEntityDAO dao)
  {
    LabAffiliation labAffiliation = dao.findEntityByProperty(LabAffiliation.class,
                                                             "affiliationName",
                                                             LAB_AFFILIATION_NAME);
    if (labAffiliation == null) {
      labAffiliation = new LabAffiliation(LAB_AFFILIATION_NAME,
                                          AffiliationCategory.OTHER);
    }
    return labAffiliation;
  }


  static private void importAnnotationData(Screen screen,
                                           File file,
                                           LibrariesDAO librariesDao)
  throws FileNotFoundException
  {
    Workbook workbook = new Workbook(file);
    Sheet sheet = workbook.getWorkbook().getSheet(0);

    List<AnnotationValueBuilder> builders = getAnnotationValueBuilders(screen, librariesDao);
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

  private static List<AnnotationValueBuilder> getAnnotationValueBuilders(final Screen screen,
                                                                         final LibrariesDAO librariesDao) {
    final List<AnnotationValueBuilder> builders = new ArrayList<AnnotationValueBuilder>();
    int column = 1;
    builders.add(new AnnotationValueBuilderImpl(column++,
                                                screen.createAnnotationType("siRNA IDs",
                                                                            "The Dharmacon/Thermofisher siRNA IDs of the individual duplexes that comprise the SMARTPool.",
                                                                            false),
                                                                            librariesDao) {
      public String transformValue(String value) {
        return value.replaceAll("&", ", ");
      }
    });
    builders.add(new AnnotationValueBuilderImpl(column++,
                                                screen.createAnnotationType("Intended Target Gene Symbol",
                                                                            "Entrez Gene symbol of targeted gene, as originally annotated by Dharmacon/Thermofisher.",
                                                                            false),
                                                                            librariesDao));
//  builders.add(new AnnotationValueBuilder() {

//  private AnnotationType _annotationType =
//  screen.createAnnotationType(
//  "Intended Target Gene ID",
//  "Entrez Gene ID of targeted gene.  (This annotation type was added to the study by ICCB-L/Screensaver).",
//  false);

//  public AnnotationType getAnnotationType() {
//  return _annotationType;
//  }

//  public void addAnnotationValue(Cell[] row)
//  {
//  String vendorIdentifier = row[0].getContents();
//  List<Well> wells = dao.findEntitiesByProperty(Well.class, "vendorIdentifier", vendorIdentifier, true, "silencingReagents.gene");
//  if (wells.size() == 0) {
//  throw new DataModelViolationException("unknown vendor identifier " + vendorIdentifier);
//  }
//  Integer entrezId = wells.get(0).getGene().getEntrezgeneId();
//  _annotationType.createAnnotationValue(new ReagentVendorIdentifier(DHARMACON_VENDOR_NAME, vendorIdentifier),
//  entrezId.toString(),
//  true);
//  }
//  });
    builders.add(new AnnotationValueBuilderImpl(column++,
                                                screen.createAnnotationType("Intended RefSeq Targets",
                                                                            "The RefSeq transcript IDs that Dharmacon/Thermofisher intended to be targeted by the SMARTPool " +
                                                                            "and that was originally provided in the product documentation.",
                                                                            false), librariesDao));
    builders.add(new AnnotationValueBuilderImpl(column++,
                                                screen.createAnnotationType("ON-Target Modification",
                                                                            "Dharamcon's \"ON-Target\" flag, indicating whether chemical modification of the sense strand " +
                                                                            "siRNA has been performed to reduce off-target effects",
                                                                            false), librariesDao)
    {
      @Override
      public String transformValue(String value)
      {
        return Integer.parseInt(value) == 1 ?  "Yes" : "No";
      }
    });

    builders.add(new AnnotationValueBuilderImpl(++column,
                                                screen.createAnnotationType("# Predicted Target Genes",
                                                                            "The number of genes that have been computationally predicted by this study to be targeted " +
                                                                            "by the SMARTPool.",
                                                                            true), librariesDao));

    builders.add(new AnnotationValueBuilderImpl(column++ - 1,
                                                screen.createAnnotationType("Gene IDs of Predicted Targets",
                                                                            "Entrez Gene IDs of genes that have been computationally predicted by this study to be targeted by at " +
                                                                            "least one siRNA duplex in the SMARTPool.",
                                                                            false), librariesDao) {
      public String transformValue(String value) {
        return value.replaceAll("GeneID:", "").replaceAll("&", ", ");
      }
    });

    builders.add(new AnnotationValueBuilderImpl(column++,
                                                screen.createAnnotationType("Predicted RefSeq Targets",
                                                                            "The RefSeq transcript IDs that have been computationally predicted by this study to be targeted " +
                                                                            "by the SMARTPool.  Transcripts derived from the same gene are grouped with square brackets and " +
                                                                            "gene group order matches Gene ID order in \"" +
                                                                            builders.get(builders.size() - 1).getAnnotationType().getName() + "\".",
                                                                            false), librariesDao) {
      public String transformValue(String value) {
        return transformDelimiters(value);
      }
    });

    builders.add(new AnnotationValueBuilderImpl(column++,
                                                screen.createAnnotationType("# Duplexes Targeting each RefSeq",
                                                                            "The number of duplex siRNAs from the SMARTPool that are predicted by this study to target each " +
                                                                            "RefSeq.  Duplex grouping and ordering matches RefSeq transcripts in \"" +
                                                                            builders.get(builders.size() - 1).getAnnotationType().getName() + "\".",
                                                                            false), librariesDao) {
      public String transformValue(String value) {
        return transformDelimiters(value);
      }
    });

    builders.add(new AnnotationValueBuilderImpl(column++,
                                                screen.createAnnotationType("# RefSeq Target Transcripts",
                                                                            "The total number of RefSeq transcripts predicted by this study to be targeted by the SMARTPool.",
                                                                            true), librariesDao));

    // note: excluding "Avg SMARTPool Efficiency" annotation due to controversial nature of this algorithm (per request of Laura Selfors, 2007-09-13)
    column++;

    builders.add(new AnnotationValueBuilderImpl(column++,
                                                screen.createAnnotationType("Is Annotation Changed",
                                                                            "\"Yes\" if the predicted RefSeq target(s) differ from the originally intended RefSeq target, " +
                                                                            "otherwise \"No\".",
                                                                            false), librariesDao));

    builders.add(new AnnotationValueBuilderImpl(column++,
                                                screen.createAnnotationType("Comments",
                                                                            "Comments on the annotation change.",
                                                                            false), librariesDao));
    return builders;
  }

  private static String transformDelimiters(String v)
  {
    String[] vs = v.replaceAll("&", ", ").split(", ");
    StringBuilder r = new StringBuilder();
    for (int i = 0; i < vs.length; i++) {
      if (r.length() > 0) {
        r.append(", ");
      }
      r.append('[').append(vs[i].replaceAll("\\+", ", ")).append(']');
    }
    return r.toString();
  }

}
