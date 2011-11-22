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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import com.google.common.collect.Lists;

import edu.harvard.med.iccbl.screensaver.io.AdminEmailApplication;
import edu.harvard.med.iccbl.screensaver.service.screens.ScreenDataSharingLevelUpdater;
import edu.harvard.med.screensaver.db.CommandLineArgumentsDatabaseConnectionSettingsResolver;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.DAOTransactionRollbackException;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.activities.AdministrativeActivity;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenDataSharingLevel;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.service.EmailService;
import edu.harvard.med.screensaver.service.OperationRestrictedException;
import edu.harvard.med.screensaver.util.Pair;

/**
 * Find and "expire" the private screens for which the dataPrivacyExpirationDate
 * is greater than the time at which this script is run. <br/>
 * "Expiration" of a screen means that the DSL is set to ScreenDataSharingLevel.MUTUAL_SCREENS,
 * when it was something greater before. see {@link ScreenDataSharingLevel#MUTUAL_SCREENS} <br>
 * <br>
 * see [#2175] Expiration services to become notification services only.
 */
public class ScreenPrivacyExpirationUpdater extends AdminEmailApplication
{
  private static final String EXPIRATION_MESSAGE_TXT_LOCATION = "../../../../../../../screenPrivacyExpirationMessage.txt";

  public static int SCREEN_ACTIVITY_DATA_PRIVACY_EXPIRATION_AGE_DAYS = 365 * 2 + 30 * 2;

  private ScreenDataSharingLevelUpdater _privacyUpdater = null;

  public ScreenPrivacyExpirationUpdater(String[] cmdLineArgs)
  {
    super(cmdLineArgs);
  }

  public void init()
  {
    _privacyUpdater = (ScreenDataSharingLevelUpdater) getSpringBean("screenDataSharingLevelUpdater");
  }

  private static Logger log = Logger.getLogger(ScreenPrivacyExpirationUpdater.class);

  public static final String[] ADJUST_DATA_PRIVACY_EXPIRATION_DATE_BASED_ON_ACTIVITY = {
    "adjustexpiration", "days",
    "adjust-expiration-on-activity-date",
    "Adjust the Screen.dataPrivacyExpirationDate by the given interval (days) from the last activity"
    };

  public static final String[] EXPIRE_PRIVACY = {
    "expireprivacy", "",
    "expire-screen-privacy",
    "Find and expire privacy of screens for which the dataPrivacyExpirationDate has passed."
    };

  public static final String[] NOTIFY_PRIVACY_EXPIRATION = {
    "notifyofexpiration", "days",
    "notify-privacy-expiration",
    "Find screens for which the dataPrivacyExpirationDate will expire in the given interval (days) from present. " +
      "Send \"expiration notifications\" to all admins having a data" +
      "privacy data expiration admin role (\"dataSharingLevelAdmin\"), and the" +
      "screen's PI, lead screener, and collaborators."
    };

  public static final String[] NOTIFY_OF_PUBLICATIONS = {
    "notifyofpublications", "",
    "notify-of-publications",
    "Find private screens that have been published." +
      "Send \"publication notifications\" to all admins having a data" +
      "privacy data expiration admin role (\"dataSharingLevelAdmin\")"
    };

  public static final String[] EXPIRATION_EMAIL_MESSAGE_LOCATION =
  {
    "expirationemailfilelocation",
    "file location",
    "expiration-email-file-location",
    "(optional) location of the message to send to users, first line of this text file is the subject, the rest is the email. " +
      "Default location is relative to the location of this class, on the classpath: " + EXPIRATION_MESSAGE_TXT_LOCATION
  };

  public static final String[] NOTIFY_OF_OVERRIDES =
  {
    "notifyofoverrides", "",
    "notify-of-adjust-overrides",
    "(optional - " + ADJUST_DATA_PRIVACY_EXPIRATION_DATE_BASED_ON_ACTIVITY[LONG_OPTION_INDEX] +
      ") when using the adjust operation, whether to notify (repeatedly) " +
      "of the Earliest/Last Allowed Data Privacy Expiration Dates being invoked."
  };

  public static final String[] TEST_ONLY = {
    "testonly", "",
    "test-only",
    "run the entire operation specified, then roll-back."
    };

  @SuppressWarnings("static-access")
  public static void main(String[] args)
  {
    final ScreenPrivacyExpirationUpdater app = new ScreenPrivacyExpirationUpdater(args);
    // TODO: allow this to be optional and glean the eCommons ID from the
    // environment - sde4

    app.addCommandLineOption(OptionBuilder.hasArg()
                             .withArgName(ADJUST_DATA_PRIVACY_EXPIRATION_DATE_BASED_ON_ACTIVITY[ARG_INDEX])
                             .withDescription(ADJUST_DATA_PRIVACY_EXPIRATION_DATE_BASED_ON_ACTIVITY[DESCRIPTION_INDEX])
                             .withLongOpt(ADJUST_DATA_PRIVACY_EXPIRATION_DATE_BASED_ON_ACTIVITY[LONG_OPTION_INDEX])
                             .create(ADJUST_DATA_PRIVACY_EXPIRATION_DATE_BASED_ON_ACTIVITY[SHORT_OPTION_INDEX]));

    app.addCommandLineOption(OptionBuilder
                             .withDescription(NOTIFY_OF_OVERRIDES[DESCRIPTION_INDEX])
                             .withLongOpt(NOTIFY_OF_OVERRIDES[LONG_OPTION_INDEX])
                             .create(NOTIFY_OF_OVERRIDES[SHORT_OPTION_INDEX]));

    app.addCommandLineOption(OptionBuilder
                             .withDescription(EXPIRE_PRIVACY[DESCRIPTION_INDEX])
                             .withLongOpt(EXPIRE_PRIVACY[LONG_OPTION_INDEX])
                             .create(EXPIRE_PRIVACY[SHORT_OPTION_INDEX]));

    app.addCommandLineOption(OptionBuilder.hasArg()
                             .withArgName(NOTIFY_PRIVACY_EXPIRATION[ARG_INDEX])
                             .withDescription(NOTIFY_PRIVACY_EXPIRATION[DESCRIPTION_INDEX])
                             .withLongOpt(NOTIFY_PRIVACY_EXPIRATION[LONG_OPTION_INDEX])
                             .create(NOTIFY_PRIVACY_EXPIRATION[SHORT_OPTION_INDEX]));

    app.addCommandLineOption(OptionBuilder.hasArg()
                             .withArgName(EXPIRATION_EMAIL_MESSAGE_LOCATION[ARG_INDEX])
                             .withDescription(EXPIRATION_EMAIL_MESSAGE_LOCATION[DESCRIPTION_INDEX])
                             .withLongOpt(EXPIRATION_EMAIL_MESSAGE_LOCATION[LONG_OPTION_INDEX])
                             .create(EXPIRATION_EMAIL_MESSAGE_LOCATION[SHORT_OPTION_INDEX]));

    app.addCommandLineOption(OptionBuilder
                             .withDescription(NOTIFY_OF_PUBLICATIONS[DESCRIPTION_INDEX])
                             .withLongOpt(NOTIFY_OF_PUBLICATIONS[LONG_OPTION_INDEX])
                             .create(NOTIFY_OF_PUBLICATIONS[SHORT_OPTION_INDEX]));

    app.addCommandLineOption(OptionBuilder
                         .withDescription(TEST_ONLY[DESCRIPTION_INDEX])
                         .withLongOpt(TEST_ONLY[LONG_OPTION_INDEX])
                         .create(TEST_ONLY[SHORT_OPTION_INDEX]));

    app.processOptions(/* acceptDatabaseOptions= */true,
                       /* acceptAdminUserOptions= */true);

    log.info("==== Running ScreenPrivacyExpirationUpdater: " + app.toString() + "======");

    final GenericEntityDAO dao = (GenericEntityDAO) app.getSpringBean("genericEntityDao");
    app.init();

    dao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        try {
          // check that not too many options are specified
          int numberOfActions = 0;
          if (app.isCommandLineFlagSet(NOTIFY_PRIVACY_EXPIRATION[SHORT_OPTION_INDEX])) numberOfActions++;
          if (app.isCommandLineFlagSet(NOTIFY_OF_PUBLICATIONS[SHORT_OPTION_INDEX])) numberOfActions++;
          if (app.isCommandLineFlagSet(EXPIRE_PRIVACY[SHORT_OPTION_INDEX])) numberOfActions++;
          if (app.isCommandLineFlagSet(ADJUST_DATA_PRIVACY_EXPIRATION_DATE_BASED_ON_ACTIVITY[SHORT_OPTION_INDEX])) numberOfActions++;
          if (numberOfActions > 1)
          {
            log.error("May only specify one of: " +
              NOTIFY_PRIVACY_EXPIRATION[SHORT_OPTION_INDEX]
                                                                              +
              ", " +
              NOTIFY_OF_PUBLICATIONS[SHORT_OPTION_INDEX]
                                                                                                              +
              ", " +
              EXPIRE_PRIVACY[SHORT_OPTION_INDEX]
                                                                                                                                      +
              ", " + ADJUST_DATA_PRIVACY_EXPIRATION_DATE_BASED_ON_ACTIVITY[SHORT_OPTION_INDEX]);
            System.exit(1);
          }

          try {
            if (app.isCommandLineFlagSet(NOTIFY_PRIVACY_EXPIRATION[SHORT_OPTION_INDEX]))
            {
              Integer daysAheadToNotify = app.getCommandLineOptionValue(NOTIFY_PRIVACY_EXPIRATION[SHORT_OPTION_INDEX],
                                                                        Integer.class);
              app.findNewExpiredAndNotifyAhead(daysAheadToNotify);
            }
            else if (app.isCommandLineFlagSet(NOTIFY_OF_PUBLICATIONS[SHORT_OPTION_INDEX]))
            {
              app.notifyOfPublications();
            }
            else if (app.isCommandLineFlagSet(EXPIRE_PRIVACY[SHORT_OPTION_INDEX]))
            {
              app.expireScreenDataSharingLevels();
            }
            else if (app.isCommandLineFlagSet(ADJUST_DATA_PRIVACY_EXPIRATION_DATE_BASED_ON_ACTIVITY[SHORT_OPTION_INDEX]))
            {
              Integer ageToExpireFromActivityDateInDays =
                app.getCommandLineOptionValue(ADJUST_DATA_PRIVACY_EXPIRATION_DATE_BASED_ON_ACTIVITY[SHORT_OPTION_INDEX], Integer.class);
              app.adjustDataPrivacyExpirationByActivities(ageToExpireFromActivityDateInDays);
            }
            else {
              app.showHelpAndExit("No action specified (expire, notify of privacy expirations, notify of publications, or adjust)?");
            }
          }
          catch (OperationRestrictedException e) {
            app.sendErrorMail("Warn: Could not complete expiration service operation", "Warn: Could not complete expiration service operation", e);
            throw new DAOTransactionRollbackException(e);
          }
        }
        catch (MessagingException e) {
          String msg = "Admin email operation not completed due to MessagingException";
          log.error(msg + ":\nApp: " + app.toString(), e);
          throw new DAOTransactionRollbackException(e);
        }
        if (app.isCommandLineFlagSet(TEST_ONLY[SHORT_OPTION_INDEX])) {
          throw new DAOTransactionRollbackException("Rollback, testing only");
        }
      }
    });
    log.info("==== finished ScreenPrivacyExpirationUpdater ======");

  }

  private void notifyOfPublications() throws MessagingException
  {
    List<Screen> publishedScreens = _privacyUpdater.findNewPublishedPrivate();

    if (publishedScreens.isEmpty()) {
      String subject = getMessages().getMessage("admin.screens.dataPrivacyExpiration.publicationNotification.noaction.subject");
      String msg = "No published private Screens.";
      sendAdminEmails(subject, msg);
    }
    else {
      // 1. send a summary email to the admin, dateSharingLevelAdmin's and recipients specified on the command line
      String subject = getMessages().getMessage("admin.screens.dataPrivacyExpiration.publicationNotification.subject",
                                                publishedScreens.size(),
                                                new LocalDate());
      StringBuilder msg = new StringBuilder(
                                            getMessages().getMessage("admin.screens.dataPrivacyExpiration.publicationNotification.messageBoilerplate",
                                                                     publishedScreens.size(), new LocalDate()));
      msg.append("\n\n");
      for (Screen screen : publishedScreens) {
        msg.append("\nScreen with Publication:\n");
        msg.append(printScreenHeader() + "\n");
        msg.append(printScreen(screen) + "\n");
        msg.append("Associated Users: \n");
        msg.append("\t" + printUserHeader() + "\n");
        for (ScreensaverUser user : screen.getAssociatedScreeningRoomUsers()) {
          msg.append("\t").append(printUser(user) + "\n");
        }
      }
      sendAdminEmails(subject, msg.toString(), _privacyUpdater.findDataSharingLevelAdminUsers());
    }
  }

  private void adjustDataPrivacyExpirationByActivities(Integer ageToExpireFromActivityDateInDays) throws MessagingException
  {
    ScreenDataSharingLevelUpdater.DataPrivacyAdjustment adjustment = _privacyUpdater.adjustDataPrivacyExpirationByActivities(ageToExpireFromActivityDateInDays, findAdministratorUser());
    if (adjustment.isEmpty(isCommandLineFlagSet(NOTIFY_OF_OVERRIDES[SHORT_OPTION_INDEX]))) {
      String subject = getMessages().getMessage("admin.screens.dataPrivacyExpiration.dataPrivacyExpirationdate.adjustment.noaction.subject");
      String msg = "No Screens with Activities to adjust the dataPrivacyExpirationDate";
      sendAdminEmails(subject, msg);
    }
    else {
      // Send a summary notification to this admin and recipients specified on the command line
      String subject = getMessages().getMessage("admin.screens.dataPrivacyExpiration.dataPrivacyExpirationdate.adjustment.subject",
                                                adjustment.screensAdjusted.size() + adjustment.screensAdjustedToAllowed.size(),
                                                adjustment.screenPrivacyAdjustmentNotAllowed.size());

      StringBuilder msg = new StringBuilder(
                                            getMessages().getMessage("admin.screens.dataPrivacyExpiration.dataPrivacyExpirationdate.adjustment.messageBoilerplate",
                                                                     ageToExpireFromActivityDateInDays,
                                                                     adjustment.screensAdjusted.size(),
                                                                     adjustment.screensAdjustedToAllowed.size(),
                                                                     adjustment.screenPrivacyAdjustmentNotAllowed.size()
                                              ));
      if (isCommandLineFlagSet(TEST_ONLY[SHORT_OPTION_INDEX])) {
        subject = "Testing: " + subject;
        msg.append("\n----TEST Only: no database changes committed.-------");
      }
      msg.append("\n\n");

      if (!adjustment.screensAdjusted.isEmpty()) {
        msg.append("\n\nAdjusted to " + ageToExpireFromActivityDateInDays + " days from the last Library Screening Activity:\n");
        for (Pair<Screen,AdministrativeActivity> result : adjustment.screensAdjusted) {
          Screen screen = result.getFirst();
          msg.append("\nAdjusted Screen:\n");
          msg.append(printScreenHeader() + "\n");
          msg.append(printScreen(screen) + "\n");
          msg.append("Comment: " + result.getSecond().getComments() + "\n");
          msg.append("Associated Users: \n");
          msg.append("\t" + printUserHeader() + "\n");
          for (ScreensaverUser user : screen.getAssociatedScreeningRoomUsers()) {
            msg.append("\t").append(printUser(user) + "\n");
          }
        }
      }

      if (!adjustment.screensAdjustedToAllowed.isEmpty()) {
        msg.append("\n\nAdjustment to the allowed value for the screens:\n");
        for (Pair<Screen,AdministrativeActivity> result : adjustment.screensAdjustedToAllowed) {
          Screen screen = result.getFirst();
          msg.append("\nAdjusted Screen:\n");
          msg.append(printScreenHeader() + "\n");
          msg.append(printScreen(screen) + "\n");
          msg.append("Comment: " + result.getSecond().getComments() + "\n");
          msg.append("Associated Users: \n");
          msg.append("\t" + printUserHeader() + "\n");
          for (ScreensaverUser user : screen.getAssociatedScreeningRoomUsers()) {
            msg.append("\t").append(printUser(user) + "\n");
          }
        }
      }

      if (isCommandLineFlagSet(NOTIFY_OF_OVERRIDES[SHORT_OPTION_INDEX])
        && !adjustment.screenPrivacyAdjustmentNotAllowed.isEmpty()) {
        msg.append("\n\nAdjustment not allowed for the screens:\n");
        for (Pair<Screen,String> result : adjustment.screenPrivacyAdjustmentNotAllowed) {
          Screen screen = result.getFirst();
          msg.append("\nAdjustment overridden for Screen:\n");
          msg.append(printScreenHeader() + "\n");
          msg.append(printScreen(screen) + "\n");
          msg.append("Comment: " + result.getSecond() + "\n");
          msg.append("Associated Users: \n");
          msg.append("\t" + printUserHeader() + "\n");
          for (ScreensaverUser user : screen.getAssociatedScreeningRoomUsers()) {
            msg.append("\t").append(printUser(user) + "\n");
          }
        }
      }
      sendAdminEmails(subject, msg.toString(), _privacyUpdater.findDataSharingLevelAdminUsers());
    }
  }

  private void findNewExpiredAndNotifyAhead(Integer daysAheadToNotify)
    throws MessagingException
  {
    LocalDate expireDate = new LocalDate().plusDays(daysAheadToNotify);

    List<Screen> oldScreens = new ArrayList<Screen>(_privacyUpdater.findNewExpiredNotNotified(expireDate));
    // sort by DPED
    Collections.sort(oldScreens, new Comparator<Screen>() {
      public int compare(Screen o1, Screen o2)
      {
        if (o1 == null || o2 == null) return o1 == null ? o2 == null ? 0 : -1 : 1;
        return o1.getDataPrivacyExpirationDate().compareTo(o2.getDataPrivacyExpirationDate());
      }
    });

    if (oldScreens.isEmpty()) {
      String subject = getMessages().getMessage("admin.screens.dataPrivacyExpiration.dataPrivacyExpirattion.warningNotification.noaction.subject", daysAheadToNotify);
      String msg = "No Screens have agreements set to expire prior to: "
        + expireDate + " that have not already been notified.";
      log.info(msg);
      sendAdminEmails(subject, msg);
    }
    else {
      // 1. send a summary email to the admin, dateSharingLevelAdmin's and recipients specified on the command line
      String subject = getMessages().getMessage("admin.screens.dataPrivacyExpiration.dataPrivacyExpirattion.warningNotification.subject",
                                                oldScreens.size(),
                                                daysAheadToNotify);
      StringBuilder msg =
        new StringBuilder(getMessages().getMessage("admin.screens.dataPrivacyExpiration.dataPrivacyExpirattion.warningNotification.messageBoilerplate",
                                                   oldScreens.size(),
                                                   daysAheadToNotify));
      if (isAdminEmailOnly()) msg.append("\n---- NOTE: sending email only to data sharing level admins ---");
      if (isCommandLineFlagSet(TEST_ONLY[SHORT_OPTION_INDEX])) {
        subject = "Testing: " + subject;
        msg.append("\n----TEST Only: no database changes committed.-------");
      }

      msg.append("\nScreens expiring on or before " + EXPIRE_DATE_FORMATTER.print(expireDate) + "\n");
      for (Screen screen : oldScreens) {
        msg.append("\nScreen: \n");
        msg.append(printScreenHeader() + "\n");
        msg.append(printScreen(screen) + "\n");
        msg.append("Associated Users: \n");
        msg.append("\t" + printUserHeader() + "\n");
        for (ScreensaverUser user : screen.getAssociatedScreeningRoomUsers()) {
          msg.append("\t").append(printUser(user)).append("\n");
        }
      }

      Pair<String,String> subjectMessage = getScreenExpireNotificationSubjectMessage(); // Note: nothing to replace
      String notificationMessage = MessageFormat.format(subjectMessage.getSecond(),
                                                        "---",
                                                        "[-- a Small Molecule Screen Title]",
                                                        "[current screen data sharing level]",
                                                       EXPIRE_DATE_FORMATTER.print(expireDate));
      msg.append("\n\n[example email]\n");
      msg.append("\nSubject: " + subjectMessage.getFirst() + "\n\n");
      msg.append(notificationMessage);

      sendAdminEmails(subject, msg.toString(), _privacyUpdater.findDataSharingLevelAdminUsers());

      if (isAdminEmailOnly() || 
        ( isCommandLineFlagSet(TEST_ONLY[SHORT_OPTION_INDEX]) && ! isCommandLineFlagSet(TEST_EMAIL_ONLY[SHORT_OPTION_INDEX]) )) {
        for (Screen screen : oldScreens) {
          _privacyUpdater.setDataPrivacyExpirationNotifiedDate(screen);
        }
      }
      else {
        // 2. For each screen, send a notification to the screen's PI, lead screener, and collaborators
        List<MessagingException> exceptions = Lists.newLinkedList();
        for (Screen screen : oldScreens) {
          notificationMessage = MessageFormat.format(subjectMessage.getSecond(),
                                                     screen.getFacilityId(),
                                                     getScreenTitle(screen),
                                                     screen.getDataSharingLevel(),
                                                     EXPIRE_DATE_FORMATTER.print(screen.getDataPrivacyExpirationDate()));
          if(sendEmails(subjectMessage.getFirst(), notificationMessage, screen.getAssociatedScreeningRoomUsers()))
          {
            _privacyUpdater.setDataPrivacyExpirationNotifiedDate(screen);
          }
        }
      }
    }
  }

  private void expireScreenDataSharingLevels()
    throws MessagingException
  {
    LocalDate expireDate = new LocalDate();
    List<Pair<Screen,AdministrativeActivity>> results = _privacyUpdater.expireScreenDataSharingLevels(expireDate, findAdministratorUser());

    if (results.isEmpty()) {
      String subject = getMessages().getMessage("admin.screens.dataPrivacyExpiration.dataPrivacyExpirattion.notification.noaction.subject");
      String msg = "No Small Molecule Screens with SDSL > 1 have agreements dated on or before: " + expireDate;
      sendAdminEmails(subject, msg);
    }
    else {
      // Send a notification that the screens have expired to this admin and recipients specified on the command line
      String subject = getMessages().getMessage("admin.screens.dataPrivacyExpiration.dataPrivacyExpirattion.notification.subject", results.size());
      StringBuilder msg = new StringBuilder(
                                            getMessages().getMessage("admin.screens.dataPrivacyExpiration.dataPrivacyExpirattion.notification.messageBoilerplate",
                                                                     results.size(), new LocalDate()));
      if (isCommandLineFlagSet(TEST_ONLY[SHORT_OPTION_INDEX])) {
        subject = "Testing: " + subject;
        msg.append("\n----TEST Only: no database changes committed.-------");
      }

      msg.append("\n");
      for (Pair<Screen,AdministrativeActivity> result : results) {
        Screen screen = result.getFirst();
        msg.append("\nExpired Screen:\n");
        msg.append(printScreenHeader() + "\n");
        msg.append(printScreen(screen) + "\n");
        msg.append("Comment: " + result.getSecond().getComments() + "\n");
        msg.append("Associated Users: \n");
        msg.append("\t" + printUserHeader() + "\n");
        for (ScreensaverUser user : screen.getAssociatedScreeningRoomUsers()) {
          msg.append("\t").append(printUser(user) + "\n");
        }
      }

      sendAdminEmails(subject, msg.toString(), _privacyUpdater.findDataSharingLevelAdminUsers());
    }
  }

  public static String SCREEN_PRINT_FORMAT = "|%1$-6s|%2$-12s|%3$-25s|%4$-60s";

  public static String printScreenHeader()
  {
    return String.format(SCREEN_PRINT_FORMAT,
                         "#",
                         "Expiration",
                         "Sharing Level",
                         "Title");
  }

  public static String printScreen(Screen screen)
  {
    Object[] values = {
      "" + screen.getFacilityId(),
      "" + screen.getDataPrivacyExpirationDate(),
      "" + screen.getDataSharingLevel(),
      getScreenTitle(screen).trim()
    };
    return String.format(SCREEN_PRINT_FORMAT, values);
  }

  //TODO: move this method to the Screen object?
  private static String getScreenTitle(Screen screen)
  {
    switch (screen.getScreenType()) {
      case RNAI:
        return screen.getTitle();
      case SMALL_MOLECULE:
        return "A screen for compounds that " + screen.getTitle();
      default:
        return screen.getTitle();
    }
  }

  /**
   * Return the subject first and the message second.
   * Message:
   * {0} Screen Number
   * {1} Screen Title
   * {2} timeToNotify (tbd, formatting)
   * 
   * @throws MessagingException
   */
  private Pair<String,String> getScreenExpireNotificationSubjectMessage() throws MessagingException
  {
    InputStream in = null;
    if (isCommandLineFlagSet(EXPIRATION_EMAIL_MESSAGE_LOCATION[SHORT_OPTION_INDEX])) {
      try {
        in = new FileInputStream(new File(getCommandLineOptionValue(EXPIRATION_EMAIL_MESSAGE_LOCATION[SHORT_OPTION_INDEX])));
      }
      catch (FileNotFoundException e) {
        sendErrorMail("Operation not completed for ScreenPrivacyExpirationUpdater, could not locate expiration message", toString(), e);
        throw new DAOTransactionRollbackException(e);
      }
    }
    else {
      in = this.getClass()
               .getResourceAsStream(EXPIRATION_MESSAGE_TXT_LOCATION);
    }
    Scanner scanner = new Scanner(in);
    try {
      StringBuilder builder = new StringBuilder();
      String subject = scanner.nextLine(); // first line is the subject
      while (scanner.hasNextLine()) {
        builder.append(scanner.nextLine()).append("\n");
      }
      return Pair.newPair(subject, builder.toString());
    }
    finally {
      scanner.close();
    }
  }

}