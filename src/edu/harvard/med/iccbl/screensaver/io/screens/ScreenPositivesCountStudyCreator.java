// $HeadURL:
// http://seanderickson1@forge.abcd.harvard.edu/svn/screensaver/branches/iccbl/data-sharing-levels/src/edu/harvard/med/screensaver/io/screenresults/ScreenResultImporter.java
// $
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.


package edu.harvard.med.iccbl.screensaver.io.screens;

import javax.mail.internet.InternetAddress;

import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import edu.harvard.med.iccbl.screensaver.io.AdminEmailApplication;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.DAOTransactionRollbackException;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.ScreenDAO;
import edu.harvard.med.screensaver.db.ScreenResultsDAO;
import edu.harvard.med.screensaver.io.screens.StudyCreator;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenDataSharingLevel;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.Study;
import edu.harvard.med.screensaver.model.screens.StudyType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.service.EmailService;

/**
 * Create a study that annotates Reagents with the count of positive hits 
 * per Reagent (LibraryWell) across all Screens.<br>
 * NOTE: because Reagents  are one-to-one with LibraryWells (by LibraryContentsVersion).
 * 
 * see [#2268] new column to display # overlapping screens <br>
 *  <br>
 * Details:  <br> 
 * <br>
 * We want to provide a column in Well Search Results to display the number of screens that
 * have marked the well as a positive. I believe we can include even screens
 * w/DSL 3 (Private), but we should confirm this. It would be nice to be able to
 * calculate this number dynamically, but it almost certainly would not be
 * performant. Instead, we'll probably need to pre-compute this and store in the
 * database. To avoid creating new infrastructure for this, I propose that we
 * simply create a new study that provides a single data column ("annotation
 * type") for this pre-computed count. The study can be recreated via a batch
 * cron job that runs a command-line app for this purpose. If we come up with
 * new cross-screen comparison statistics, we can add them to this study, as
 * appropriate, as new data columns.
 */
// TODO: rename this class to be more specific to what we are doing with the cross-screen comparison
public class ScreenPositivesCountStudyCreator extends AdminEmailApplication
{
  public static String DEFAULT_SMALL_MOLECULE_STUDY_FACILITY_ID = Study.STUDY_FACILITY_ID_PREFIX + "200001";
  public static String DEFAULT_RNAI_STUDY_FACILITY_ID = Study.STUDY_FACILITY_ID_PREFIX + "200002";
  
  public static final String DEFAULT_SM_STUDY_TITLE = "Reagent Counts for Small Molecule Screens";
  public static final String DEFAULT_SM_STUDY_SUMMARY =
    "Annotates each small molecule reagent with the number of times " +
    "it has been screened, and the count of positive hits per reagent (library well) " +
    "across all small molecule screens. This data is based on primary screening data " +
    "only and the \"positive\" designations were determined by the " +
    "investigator(s) responsible for each screen.";
  public static final String DEFAULT_RNAi_STUDY_TITLE = "Reagent Counts for RNAi Screens";
  public static final String DEFAULT_RNAi_STUDY_SUMMARY =
    "Annotates each RNAi reagent with the number of times it has " +
    "been screened, and the count of positive hits per reagent (library well) across " +
    "all RNAi Screens. This data is based on primary screening data only and the " +
    "\"positive\" designations were determined by the investigator(s) " +
    "responsible for each screen.";

  public static final String DEFAULT_POSITIVES_ANNOTATION_NAME = "Screen Positives Count";
  public static final String DEFAULT_SM_POSITIVES_ANNOTATION_DESC = "Number of times scored as positive across all Small Molecule Screens";
  public static final String DEFAULT_RNAi_POSITIVES_ANNOTATION_DESC = "Number of times scored as positive across all RNAi Screens";

  public static final String DEFAULT_OVERALL_ANNOTATION_NAME = "Screened Count";
  public static final String DEFAULT_SM_OVERALL_ANNOTATION_DESC = "Number of times screened for all Small Molecule Screens";
  public static final String DEFAULT_RNAi_OVERALL_ANNOTATION_DESC = "Number of times screened for all RNAi Screens";

  public ScreenPositivesCountStudyCreator(String[] cmdLineArgs)
  {
    super(cmdLineArgs);
 }

  private static Logger log = Logger.getLogger(ScreenPositivesCountStudyCreator.class);

  public static final String[] OPTION_STUDY_TITLE = { "title", "title", "study-title", "Title for the study" };
  public static final String[] OPTION_STUDY_SUMMARY = { "summary", "summary", "study-summary", "Summary for the study" };
//  public static final String[] OPTION_ANNOTATION_NAME = { "annotation", "name", "annotation-name", "name for the annotation in the study" };
//  public static final String[] OPTION_ANNOTATION_DESC = { "annotationdesc", "desc", "annotation-desc", "descriptive name of the annotation" };
  public static final String[] OPTION_STUDY_NUMBER = { "number", "number", "study-number", "Study number (not required, recommended to use default values), (must be unique, unless specifying the \"replace\" option" };
  public static final String[] OPTION_SCREEN_TYPE_SM = { "typesm", "", "screen-type-sm", "create the study for Small Molecule screens" };
  public static final String[] OPTION_SCREEN_TYPE_RNAI = { "typernai", "", "screen-type-rnai", "create the study for RNAi screens" };
  public static final String[] OPTION_REPLACE = {
    "replace",
    "",
    "replace-if-exists",
    "the default action is to replace the study if it is out of date; as determined by comparing the study date to the date of the latest screen result.  If this option is specified, delete the existing study unconditionally." };

  public static final String[] TEST_ONLY = { "testonly", "", "test-only", "run the entire operation specified, then roll-back." };


  @SuppressWarnings("static-access")
  public static void main(String[] args)
  {
    final ScreenPositivesCountStudyCreator app = new ScreenPositivesCountStudyCreator(args);

    String [] option = OPTION_STUDY_TITLE;
    app.addCommandLineOption(OptionBuilder.hasArg()
                                          .withArgName(option[ARG_INDEX])
                                          .withDescription(option[DESCRIPTION_INDEX])
                                          .withLongOpt(option[LONG_OPTION_INDEX])
                                          .create(option[SHORT_OPTION_INDEX]));

    option = OPTION_STUDY_NUMBER;
    app.addCommandLineOption(OptionBuilder.hasArg().withType(Integer.class)
                                          .withArgName(option[ARG_INDEX])
                                          .withDescription(option[DESCRIPTION_INDEX])
                                          .withLongOpt(option[LONG_OPTION_INDEX])
                                          .create(option[SHORT_OPTION_INDEX]));

    option = OPTION_STUDY_SUMMARY;
    app.addCommandLineOption(OptionBuilder.hasArg()
                                          .withArgName(option[ARG_INDEX])
                                          .withDescription(option[DESCRIPTION_INDEX])
                                          .withLongOpt(option[LONG_OPTION_INDEX])
                                          .create(option[SHORT_OPTION_INDEX]));
    option = OPTION_SCREEN_TYPE_SM;
    app.addCommandLineOption(OptionBuilder.withDescription(option[DESCRIPTION_INDEX])
                                          .withLongOpt(option[LONG_OPTION_INDEX])
                                          .create(option[SHORT_OPTION_INDEX]));

    option = OPTION_SCREEN_TYPE_RNAI;
    app.addCommandLineOption(OptionBuilder.withDescription(option[DESCRIPTION_INDEX])
                                          .withLongOpt(option[LONG_OPTION_INDEX])
                                          .create(option[SHORT_OPTION_INDEX]));

    option = OPTION_REPLACE;
    app.addCommandLineOption(OptionBuilder.withDescription(option[DESCRIPTION_INDEX])
                                          .withLongOpt(option[LONG_OPTION_INDEX])
                                          .create(option[SHORT_OPTION_INDEX]));


    app.addCommandLineOption(OptionBuilder.withDescription(TEST_ONLY[DESCRIPTION_INDEX])
                                          .withLongOpt(TEST_ONLY[LONG_OPTION_INDEX])
                                          .create(TEST_ONLY[SHORT_OPTION_INDEX]));
    try {
      if (!app.processOptions(/* acceptDatabaseOptions= */true,
                              /* acceptAdminUserOptions= */true,
                              /* showHelpOnError= */true)) {
        return;
      }

      final GenericEntityDAO dao = (GenericEntityDAO) app.getSpringBean("genericEntityDao");
      final ScreenResultsDAO screenResultsDao = (ScreenResultsDAO) app.getSpringBean("screenResultsDao");
      final ScreenDAO screenDao = (ScreenDAO) app.getSpringBean("screenDao");

      dao.doInTransaction(new DAOTransaction() {
        public void runTransaction()
        {
          try {
            AdministratorUser admin = app.findAdministratorUser();

            boolean replaceStudyIfExists = app.isCommandLineFlagSet(OPTION_REPLACE[SHORT_OPTION_INDEX]);
            String title = null;
            String summary = null;
            String annotationDesc = null;
            String overallAnnotationDesc = null;

            String studyFacilityId = null;
            ScreenType screenType = null;
            if(app.isCommandLineFlagSet(OPTION_SCREEN_TYPE_SM[SHORT_OPTION_INDEX])) {
              screenType = ScreenType.SMALL_MOLECULE;
              studyFacilityId = DEFAULT_SMALL_MOLECULE_STUDY_FACILITY_ID;
              if (app.isCommandLineFlagSet(OPTION_STUDY_NUMBER[SHORT_OPTION_INDEX])) {
                studyFacilityId = app.getCommandLineOptionValue(OPTION_STUDY_NUMBER[SHORT_OPTION_INDEX], String.class);
              }
              // todo: title = getMessages().getMessage("admin.studies.cross_screen_comparison_study.study_title");
              title = DEFAULT_SM_STUDY_TITLE;
              summary = DEFAULT_SM_STUDY_SUMMARY;
              annotationDesc = DEFAULT_SM_POSITIVES_ANNOTATION_DESC;
              overallAnnotationDesc = DEFAULT_SM_OVERALL_ANNOTATION_DESC;
            }
            if(app.isCommandLineFlagSet(OPTION_SCREEN_TYPE_RNAI[SHORT_OPTION_INDEX])) {
              if(app.isCommandLineFlagSet(OPTION_SCREEN_TYPE_SM[SHORT_OPTION_INDEX])) {
                System.out.println("Must specify either the \""+ OPTION_SCREEN_TYPE_SM[SHORT_OPTION_INDEX]
                        + "\" or the \""+ OPTION_SCREEN_TYPE_RNAI[SHORT_OPTION_INDEX]+ "\" Option");
                app.showHelp();
                System.exit(1);
              }
              screenType = ScreenType.RNAI;
              studyFacilityId = DEFAULT_RNAI_STUDY_FACILITY_ID;
              if (app.isCommandLineFlagSet(OPTION_STUDY_NUMBER[SHORT_OPTION_INDEX])) {
                studyFacilityId = app.getCommandLineOptionValue(OPTION_STUDY_NUMBER[SHORT_OPTION_INDEX], String.class);
              }
              title = DEFAULT_RNAi_STUDY_TITLE;
              summary = DEFAULT_RNAi_STUDY_SUMMARY;
              annotationDesc = DEFAULT_RNAi_POSITIVES_ANNOTATION_DESC;
              overallAnnotationDesc = DEFAULT_RNAi_OVERALL_ANNOTATION_DESC;
            }
            if(screenType == null) {
                System.out.println("Must specify either the \""+ OPTION_SCREEN_TYPE_SM[SHORT_OPTION_INDEX]
                        + "\" or the \""+ OPTION_SCREEN_TYPE_RNAI[SHORT_OPTION_INDEX]+ "\" Option");
              app.showHelp();
              System.exit(1);
            }
            
            if(app.isCommandLineFlagSet(OPTION_STUDY_NUMBER[SHORT_OPTION_INDEX])) {
              app.getCommandLineOptionValue(OPTION_STUDY_NUMBER[SHORT_OPTION_INDEX], Integer.class);
            }
            if(app.isCommandLineFlagSet(OPTION_STUDY_TITLE[SHORT_OPTION_INDEX])){
              title = app.getCommandLineOptionValue(OPTION_STUDY_TITLE[SHORT_OPTION_INDEX]);
            }
            if(app.isCommandLineFlagSet(OPTION_STUDY_SUMMARY[SHORT_OPTION_INDEX])){
              summary = app.getCommandLineOptionValue(OPTION_STUDY_SUMMARY[SHORT_OPTION_INDEX]);
            }

            Screen study = dao.findEntityByProperty(Screen.class, Screen.facilityId.getPropertyName(), studyFacilityId);
            if (study != null) {
              // first check if the study is out of date
              ScreenResult latestScreenResult = screenResultsDao.getLatestScreenResult();
              if (latestScreenResult == null)
              {
                log.info("No screen results found in the database");
                System.exit(0);
              }
              else if (study.getDateCreated().compareTo(latestScreenResult.getDateCreated()) < 1) {
                screenDao.deleteStudy(study);
              }
              else if (replaceStudyIfExists) {
                screenDao.deleteStudy(study);
              }else {                
                String msg = "study " + studyFacilityId +
                  " already exists and is up-to-date (as determined by the date of the latest uploaded screen result).  " +
                  "Use the --" + OPTION_REPLACE[LONG_OPTION_INDEX] + " flag to delete the existing study first.";
                log.info(msg);
                System.exit(0);
                }
            }

            int count = app.createReagentCountStudy(admin,
                                                    studyFacilityId,
                                                title,
                                                summary,
                                                DEFAULT_POSITIVES_ANNOTATION_NAME,
                                                annotationDesc,
                                                DEFAULT_OVERALL_ANNOTATION_NAME,
                                                overallAnnotationDesc,
                                                screenType,
                                                dao,
                                                screenResultsDao);
            
            InternetAddress adminEmail = app.getEmail(admin);
            String subject = "Study created: " + study.getTitle(); //app.getMessages().getMessage("Study generated");
            String msg = "Study: " + study.getFacilityId() + ", " + study.getTitle() + ": " + study.getSummary()
              + "\nCross-screen Reagents found: " + count;
            log.info(msg);
            EmailService emailService = app.getEmailServiceBasedOnCommandLineOption(admin);
            emailService.send(subject,
                              msg,
                              adminEmail,
                              new InternetAddress[] { adminEmail }, null);
    
            if (app.isCommandLineFlagSet(TEST_ONLY[SHORT_OPTION_INDEX])) {
              throw new DAOTransactionRollbackException("Rollback, testing only");
            }
          }
          catch (Exception e) {
            throw new DAOTransactionRollbackException(e);
          }
        }
      });
      System.exit(0);
    }
    catch (ParseException e) {
      log.error("error parsing command line options: " + e.getMessage());
    }
    System.exit(1); // error
  }

  /**
   * @return the count of reagents
   */
  public int createReagentCountStudy(AdministratorUser admin,
                                     String studyName,
                                     String title,
                                     String summary,
                                     String positiveCountAnnotationName,
                                     String positiveCountAnnotationDescription,
                                     String overallCountAnnotationName,
                                     String overallCountAnnotationDesc,
                                     ScreenType screenType,
                                     GenericEntityDAO dao,
                                     ScreenResultsDAO _screenResultsDao)
  {
    Screen study = dao.findEntityByProperty(Screen.class,
                                            Screen.facilityId.getPropertyName(),
                                            studyName);
    if (study != null) {
      String errMsg = "study " + studyName +
        " already exists (use --replace flag to delete existing study first)";
      throw new IllegalArgumentException(errMsg);
    }

    LabHead labHead = (LabHead) StudyCreator.findOrCreateScreeningRoomUser(dao,
                                                                           admin.getFirstName(),
                                                                           admin.getLastName(),
                                                                           admin.getEmail(),
                                                                           true,
                                                                           null);

    study = new Screen(admin,
                       studyName,
                       labHead,
                       labHead,
                       screenType,
                       StudyType.IN_SILICO,
                       title);
    study.setDataSharingLevel(ScreenDataSharingLevel.SHARED);
    study.setSummary(summary);

    AnnotationType positivesCountAnnotType = study.createAnnotationType(positiveCountAnnotationName,
                                                                        positiveCountAnnotationDescription,
                                                                        true);
    AnnotationType overallCountAnnotType = study.createAnnotationType(overallCountAnnotationName,
                                                                      overallCountAnnotationDesc,
                                                                      true);
    dao.persistEntity(study);
    dao.persistEntity(positivesCountAnnotType);
    dao.persistEntity(overallCountAnnotType);

    return _screenResultsDao.createScreenedReagentCounts(screenType, study, positivesCountAnnotType, overallCountAnnotType);
  }
}