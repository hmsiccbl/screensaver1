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

import javax.mail.MessagingException;

import org.apache.commons.cli.OptionBuilder;
import org.apache.log4j.Logger;

import edu.harvard.med.iccbl.screensaver.io.AdminEmailApplication;
import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.DAOTransactionRollbackException;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.ScreenDAO;
import edu.harvard.med.screensaver.db.ScreenResultsDAO;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultReporter;
import edu.harvard.med.screensaver.io.screens.StudyCreator;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.LabHead;

/**
 * TODO: Move the transactional code out of this class and into service methods
 * Create a studies to count overlapping positives and to count confirmed positive statistics.<br>
 * 1. Overlapping positives:<br>
 * see [#2268] new column to display # overlapping screens <br>
 * Details: <br>
 * <br>
 * 1. Overlapping positives:<br>
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
 * appropriate, as new data columns. <br>
 */
public class ScreenPositivesCountStudyCreator extends AdminEmailApplication
{
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
  public static final String[] OPTION_STUDY_NUMBER = { "number", "number", "study-number",
    "Study number (not required, recommended to use default values), (must be unique, unless specifying the \"replace\" option" };
  public static final String[] OPTION_SCREEN_TYPE_SM = { "typesm", "", "screen-type-sm",
    "create the study for Small Molecule screens" };
  public static final String[] OPTION_SCREEN_TYPE_RNAI = { "typernai", "", "screen-type-rnai",
    "create the study for RNAi screens" };
  public static final String[] OPTION_REPLACE = {
    "replace",
    "",
    "replace-if-exists",
    "the default action is to replace the study if it is out of date; as determined by comparing the study date to the date of the latest screen result.  If this option is specified, delete the existing study unconditionally." };

  public static final String[] TEST_ONLY = { "testonly", "", "test-only", "run the entire operation specified, then roll-back." };

  @SuppressWarnings("static-access")
  public static void main(String[] args) throws MessagingException
  {
    final ScreenPositivesCountStudyCreator app = new ScreenPositivesCountStudyCreator(args);

    String[] option = OPTION_STUDY_TITLE;
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
    app.processOptions(/* acceptDatabaseOptions= */true,
                       /* acceptAdminUserOptions= */true);

    log.info("==== Running ScreenPositivesCountStudyCreator: " + app.toString() + "======");

    try {
      app.execute();
    }
    catch (Exception e) {
      String subject = "Execution failure for " + app.getClass().getName();
      String msg = subject + "\n" + app.toString();
      app.sendErrorMail(subject, msg, e);
      System.exit(1);
    }

  }

  private void execute()
  {
    final GenericEntityDAO dao = (GenericEntityDAO) getSpringBean("genericEntityDao");
    final ScreenResultReporter report = (ScreenResultReporter) getSpringBean("screenResultReporter");
    final ScreenResultsDAO screenResultsDao = (ScreenResultsDAO) getSpringBean("screenResultsDao");
    final ScreenDAO screenDao = (ScreenDAO) getSpringBean("screenDao");

    //TODO: Move the transactional code out of this class and into service methods
    dao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        try {
          AdministratorUser admin = findAdministratorUser();

          boolean replaceStudyIfExists = isCommandLineFlagSet(OPTION_REPLACE[SHORT_OPTION_INDEX]);
          String title = null;
          String summary = null;
          String annotationDesc = null;
          String overallAnnotationDesc = null;

          String studyFacilityId = null;
          ScreenType screenType = null;
          if (isCommandLineFlagSet(OPTION_SCREEN_TYPE_SM[SHORT_OPTION_INDEX])) {
            screenType = ScreenType.SMALL_MOLECULE;
            studyFacilityId = ScreensaverConstants.DEFAULT_BATCH_STUDY_ID_POSITIVE_COUNT_SM;
            if (isCommandLineFlagSet(OPTION_STUDY_NUMBER[SHORT_OPTION_INDEX])) {
              studyFacilityId = getCommandLineOptionValue(OPTION_STUDY_NUMBER[SHORT_OPTION_INDEX], String.class);
            }
            // todo: title = getMessages().getMessage("admin.studies.cross_screen_comparison_study.study_title");
            title = DEFAULT_SM_STUDY_TITLE;
            summary = DEFAULT_SM_STUDY_SUMMARY;
            annotationDesc = DEFAULT_SM_POSITIVES_ANNOTATION_DESC;
            overallAnnotationDesc = DEFAULT_SM_OVERALL_ANNOTATION_DESC;
          }
          if (isCommandLineFlagSet(OPTION_SCREEN_TYPE_RNAI[SHORT_OPTION_INDEX])) {
            if (isCommandLineFlagSet(OPTION_SCREEN_TYPE_SM[SHORT_OPTION_INDEX])) {
              showHelpAndExit("Must specify either the \"" + OPTION_SCREEN_TYPE_SM[SHORT_OPTION_INDEX] + "\" or the \"" +
                OPTION_SCREEN_TYPE_RNAI[SHORT_OPTION_INDEX] + "\" Option");
            }
            screenType = ScreenType.RNAI;
            studyFacilityId = ScreensaverConstants.DEFAULT_BATCH_STUDY_ID_POSITIVE_COUNT_RNAI;
            if (isCommandLineFlagSet(OPTION_STUDY_NUMBER[SHORT_OPTION_INDEX])) {
              studyFacilityId = getCommandLineOptionValue(OPTION_STUDY_NUMBER[SHORT_OPTION_INDEX], String.class);
            }
            title = DEFAULT_RNAi_STUDY_TITLE;
            summary = DEFAULT_RNAi_STUDY_SUMMARY;
            annotationDesc = DEFAULT_RNAi_POSITIVES_ANNOTATION_DESC;
            overallAnnotationDesc = DEFAULT_RNAi_OVERALL_ANNOTATION_DESC;
          }
          if (screenType == null) {
            showHelpAndExit("Must specify either the \"" + OPTION_SCREEN_TYPE_SM[SHORT_OPTION_INDEX] + "\" or the \"" +
              OPTION_SCREEN_TYPE_RNAI[SHORT_OPTION_INDEX] + "\" Option");
          }

          if (isCommandLineFlagSet(OPTION_STUDY_TITLE[SHORT_OPTION_INDEX])) {
            title = getCommandLineOptionValue(OPTION_STUDY_TITLE[SHORT_OPTION_INDEX]);
          }
          if (isCommandLineFlagSet(OPTION_STUDY_SUMMARY[SHORT_OPTION_INDEX])) {
            summary = getCommandLineOptionValue(OPTION_STUDY_SUMMARY[SHORT_OPTION_INDEX]);
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
            }
            else {
              String msg = "study " + studyFacilityId +
                " already exists and is up-to-date (as determined by the date of the latest uploaded screen result).  " +
                "Use the --" + OPTION_REPLACE[LONG_OPTION_INDEX] + " flag to delete the existing study first.";
              log.info(msg);
              System.exit(0);
            }
          }

          LabHead labHead = (LabHead) StudyCreator.findOrCreateScreeningRoomUser(dao,
                                                                                 admin.getFirstName(),
                                                                                 admin.getLastName(),
                                                                                 admin.getEmail(),
                                                                                 true,
                                                                                 null);
          int count = report.createReagentCountStudy(admin,
                                                     labHead,
                                                     studyFacilityId,
                                                     title,
                                                     summary,
                                                     DEFAULT_POSITIVES_ANNOTATION_NAME,
                                                     annotationDesc,
                                                     DEFAULT_OVERALL_ANNOTATION_NAME,
                                                     overallAnnotationDesc,
                                                     screenType);

          String subject = "Study created: " + study.getTitle(); //getMessages().getMessage("Study generated");
          if (isCommandLineFlagSet(TEST_ONLY[SHORT_OPTION_INDEX])) {
            subject = "[TEST ONLY, no commits] " + subject;
          }
          String msg = "Study: " + study.getFacilityId() + ", " + study.getTitle() + ": " + study.getSummary()
            + "\nCross-screen Reagents found: " + count;
          sendAdminEmails(subject, msg);
        }
        catch (MessagingException e) {
          throw new DAOTransactionRollbackException(e);
        }
        if (isCommandLineFlagSet(TEST_ONLY[SHORT_OPTION_INDEX])) {
          throw new DAOTransactionRollbackException("Rollback, testing only");
        }
      }
    });
    log.info("==== finished ScreenPositivesCountStudyCreator ======");
  }
}