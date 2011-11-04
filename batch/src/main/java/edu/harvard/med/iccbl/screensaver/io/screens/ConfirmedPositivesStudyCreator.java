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
import javax.mail.internet.InternetAddress;

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
import edu.harvard.med.screensaver.model.screens.ProjectPhase;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenDataSharingLevel;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.StudyType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.service.EmailService;

/**
 * Create a study for confirmed positive statistics.<br>
 * <br>
 * Details: <br>
 * <br>
 * We need a cmd-line study creator that can create a study for summarizing
 * confirmed positives for screened experimental wells in "primary"
 * screens. In fact, we'll probably want 2 study creators, one for SM and or RNAi
 * screens, since they'll be treated differently. <br>
 * <br>
 * <ul>
 * RNAi study columns include:
 * <li>- Well (from primary screen)
 * <li>- Count of follow-up screens for well - M+1 columns named "N duplexes confirming positive", where 0 <= N <= M,
 * and M is the max number of duplexes per pool in any library, currently = 4). The value in each column is the number
 * of follow-up screens that confirmed the well as a positive with N duplexes
 * </ul>
 * <ul>
 * TODO: SM study columns include:
 * <li>- Well (from primary screen)
 * <li>- Count of follow-up screens for well
 * <li>- # follow-up screens confirming well as a positive
 * <li>- A column for every follow-up screen and duplex, containing the confirmation result of the well
 * </ul>
 */
public class ConfirmedPositivesStudyCreator extends AdminEmailApplication
{
  public static final String DEFAULT_STUDY_SUMMARY =
    "Summary and Count of the follow-up screens for a well/reagent.\n"
      +
      "Where:\n"
      +
      "\"N\" is 0 <= N <= M, M is the max number of duplexes per pool in any library (currently = 4), and,\n"
      +
      "\"Number of screens confirming with N duplexes\" is the number of follow-up screens that confirmed the well as a positive with N duplexes.\n"
      +
      "\"Weighted Average\" is the average number of confirmed positives per screen (the # screens per count, weighted average = ( Sum(count[N]*N)/Sum(count[N]) )";
  public static final String DEFAULT_STUDY_TITLE = "Confirmed Positives";

  public ConfirmedPositivesStudyCreator(String[] cmdLineArgs)
  {
    super(cmdLineArgs);
  }

  private static Logger log = Logger.getLogger(ConfirmedPositivesStudyCreator.class);

  public static final String[] OPTION_STUDY_TITLE = { "title", "title", "study-title", "Title for the study" };
  public static final String[] OPTION_STUDY_SUMMARY = { "summary", "summary", "study-summary", "Summary for the study" };
  public static final String[] OPTION_STUDY_NUMBER = { "number", "number", "study-number",
    "Study number (not required, recommended to use default values), (must be unique, unless specifying the \"replace\" option" };
  public static final String[] OPTION_REPLACE = {
    "replace",
    "",
    "replace-if-exists",
    "the default action is to replace the study if it is out of date; as determined by comparing the study date to the date of the latest screen result.  If this option is specified, delete the existing study unconditionally." };

  public static final String[] TEST_ONLY = { "testonly", "", "test-only", "run the entire operation specified, then roll-back." };

  @SuppressWarnings("static-access")
  public static void main(String[] args)
  {
    final ConfirmedPositivesStudyCreator app = new ConfirmedPositivesStudyCreator(args);

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
    option = OPTION_REPLACE;
    app.addCommandLineOption(OptionBuilder.withDescription(option[DESCRIPTION_INDEX])
                             .withLongOpt(option[LONG_OPTION_INDEX])
                             .create(option[SHORT_OPTION_INDEX]));

    app.addCommandLineOption(OptionBuilder.withDescription(TEST_ONLY[DESCRIPTION_INDEX])
                             .withLongOpt(TEST_ONLY[LONG_OPTION_INDEX])
                             .create(TEST_ONLY[SHORT_OPTION_INDEX]));
    app.processOptions(/* acceptDatabaseOptions= */true,
                       /* acceptAdminUserOptions= */true);

    log.info("==== Running ConfirmedPositivesStudyCreator: " + app.toString() + "======");

    final GenericEntityDAO dao = (GenericEntityDAO) app.getSpringBean("genericEntityDao");
    final ScreenResultReporter report = (ScreenResultReporter) app.getSpringBean("screenResultReporter");
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

          String studyFacilityId = ScreensaverConstants.DEFAULT_BATCH_STUDY_ID_CONFIRMATION_SUMMARY;
          if (app.isCommandLineFlagSet(OPTION_STUDY_NUMBER[SHORT_OPTION_INDEX])) {
            studyFacilityId = app.getCommandLineOptionValue(OPTION_STUDY_NUMBER[SHORT_OPTION_INDEX], String.class);
          }

          title = DEFAULT_STUDY_TITLE;
          summary = DEFAULT_STUDY_SUMMARY;
          if (app.isCommandLineFlagSet(OPTION_STUDY_TITLE[SHORT_OPTION_INDEX])) {
            title = app.getCommandLineOptionValue(OPTION_STUDY_TITLE[SHORT_OPTION_INDEX]);
          }
          if (app.isCommandLineFlagSet(OPTION_STUDY_SUMMARY[SHORT_OPTION_INDEX])) {
            summary = app.getCommandLineOptionValue(OPTION_STUDY_SUMMARY[SHORT_OPTION_INDEX]);
          }

          //TODO: refactor this code that checks for the exisiting study
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

          int count = app.createStudy(admin, studyFacilityId, title, summary,
                                      dao, report);
          study = dao.findEntityByProperty(Screen.class, Screen.facilityId.getPropertyName(), studyFacilityId);

          String subject = "Study created: \"" + study.getTitle() + "\""; //app.getMessages().getMessage("Study generated");
          if (app.isCommandLineFlagSet(TEST_ONLY[SHORT_OPTION_INDEX])) {
            subject = "[TEST ONLY, no commits] " + subject;
          }
          String msg = "Study: " + study.getFacilityId() + "\n\"" + study.getTitle() + "\"\n" + study.getSummary()
            + "\n\nTotal count of Confirmed Positives considered in the study: " + count;
          app.sendAdminEmails(subject, msg);
        }
        catch (MessagingException e) {
          throw new DAOTransactionRollbackException(e);
        }
        if (app.isCommandLineFlagSet(TEST_ONLY[SHORT_OPTION_INDEX])) {
          throw new DAOTransactionRollbackException("Rollback, testing only");
        }
      }
    });
    log.info("==== finished ConfirmedPositivesStudyCreator ======");

  }

  /**
   * @return the count of confirmed positives considered in the study
   */
  public int createStudy(AdministratorUser admin,
                         String studyName,
                         String title,
                         String summary,
                         GenericEntityDAO dao,
                         ScreenResultReporter report)
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
                       ScreenType.RNAI,
                       StudyType.IN_SILICO,
                       ProjectPhase.ANNOTATION,
                       title);
    study.setDataSharingLevel(ScreenDataSharingLevel.SHARED);
    study.setSummary(summary);

    dao.persistEntity(study);
    dao.flush();
    return report.createSilencingReagentConfirmedPositiveSummary(study);
  }
}