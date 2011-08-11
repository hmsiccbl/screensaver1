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

package edu.harvard.med.iccbl.screensaver.io.users;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

import com.google.common.collect.Lists;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.joda.time.LocalDate;

import edu.harvard.med.iccbl.screensaver.io.AdminEmailApplication;
import edu.harvard.med.iccbl.screensaver.service.users.UserAgreementUpdater;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.DAOTransactionRollbackException;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.activities.AdministrativeActivity;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ChecklistItemEvent;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.service.EmailService;
import edu.harvard.med.screensaver.service.OperationRestrictedException;
import edu.harvard.med.screensaver.util.Pair;

/**
 * Locates the _not yet expired_ users who have a SMUA with an activation on or before the date given, and it expires
 * them.<br/>
 * The date will be 2 years before the current time.<br/>
 * see {@link UserAgreementUpdater#findUsersWithOldSMAgreements(LocalDate)}
 */
public class SmallMoleculeUserExpirationUpdater extends AdminEmailApplication
{

  UserAgreementUpdater _userAgreementUpdater = null;

  public SmallMoleculeUserExpirationUpdater(String[] cmdLineArgs)
  {
    super(cmdLineArgs);
    // NOTE: do NOT put spring bean initialization in the constructor, as this will obviate any database connection settings since it 
    // forces initialization of ScreensaverProperties before they are set, and the CommandLineArgumentsDatabaseConnectionSettingsResolver uses ScreensaverProperties
    //_userAgreementUpdater = (UserAgreementUpdater) getSpringBean("userAgreementUpdater");
  }

  private void init()
  {
    _userAgreementUpdater = (UserAgreementUpdater) getSpringBean("userAgreementUpdater");
  }

  private static Logger log = Logger.getLogger(SmallMoleculeUserExpirationUpdater.class);

  private static final String EXPIRATION_MESSAGE_TXT_LOCATION = "../../../../../../../userAgreementPrivacyExpirationMessage.txt";
  private static final int DEFAULT_EXPIRATION_TIME_DAYS = 730;

  public static final String[] NOTIFY_DAYS_AHEAD_OPTION =
  { "notifyonlyindays",
    "days",
    "notify-only-days-ahead",
    "specify this option to notify only, # days ahead of the expiration date"
  };

  public static final String[] EXPIRATION_TIME_OPTION =
  {
    "expireindays",
    "days",
    "expire-in-days",
    "(optional) time, in days, for the Small Molecule User Agreement to expire (using date of activation value).  Default value is " +
      DEFAULT_EXPIRATION_TIME_DAYS + " days."
  };

  public static final String[] EXPIRE_OPTION =
  {
    "expire",
    "",
    "expire-user-roles",
    "specify this option to expire the users who's Small Molecule User Agreement is dated more than " +
      EXPIRATION_TIME_OPTION[LONG_OPTION_INDEX] + " days in the past."
  };

  public static final String[] EXPIRATION_EMAIL_MESSAGE_LOCATION =
  {
    "expirationemailfilelocation",
    "file location",
    "expiration-email-file-location",
    "(optional) location of the message to send to users, first line of this text file is the subject, the rest is the email. " +
      "Default location is relative to the location of this class, on the classpath: " + EXPIRATION_MESSAGE_TXT_LOCATION
  };

  public static final String[] TEST_ONLY = {
    "testonly", "",
    "test-only",
    "run the entire operation specified, then roll-back."
    };

  @SuppressWarnings("static-access")
  public static void main(String[] args)
  {
    final SmallMoleculeUserExpirationUpdater app = new SmallMoleculeUserExpirationUpdater(args);

    app.addCommandLineOption(OptionBuilder.hasArg()
                             .withArgName(NOTIFY_DAYS_AHEAD_OPTION[ARG_INDEX])
                             .withDescription(NOTIFY_DAYS_AHEAD_OPTION[DESCRIPTION_INDEX])
                             .withLongOpt(NOTIFY_DAYS_AHEAD_OPTION[LONG_OPTION_INDEX])
                             .create(NOTIFY_DAYS_AHEAD_OPTION[SHORT_OPTION_INDEX]));

    app.addCommandLineOption(OptionBuilder.hasArg()
                             .withArgName(EXPIRATION_TIME_OPTION[ARG_INDEX])
                             .withDescription(EXPIRATION_TIME_OPTION[DESCRIPTION_INDEX])
                             .withLongOpt(EXPIRATION_TIME_OPTION[LONG_OPTION_INDEX])
                             .create(EXPIRATION_TIME_OPTION[SHORT_OPTION_INDEX]));

    app.addCommandLineOption(OptionBuilder
                             .withDescription(EXPIRE_OPTION[DESCRIPTION_INDEX])
                             .withLongOpt(EXPIRE_OPTION[LONG_OPTION_INDEX])
                             .create(EXPIRE_OPTION[SHORT_OPTION_INDEX]));

    app.addCommandLineOption(OptionBuilder.hasArg()
                             .withArgName(EXPIRATION_EMAIL_MESSAGE_LOCATION[ARG_INDEX])
                             .withDescription(EXPIRATION_EMAIL_MESSAGE_LOCATION[DESCRIPTION_INDEX])
                             .withLongOpt(EXPIRATION_EMAIL_MESSAGE_LOCATION[LONG_OPTION_INDEX])
                             .create(EXPIRATION_EMAIL_MESSAGE_LOCATION[SHORT_OPTION_INDEX]));

    app.addCommandLineOption(OptionBuilder
                             .withDescription(TEST_ONLY[DESCRIPTION_INDEX])
                             .withLongOpt(TEST_ONLY[LONG_OPTION_INDEX])
                             .create(TEST_ONLY[SHORT_OPTION_INDEX]));

    app.processOptions(true, true);

    log.info("==== Running SmallMoleculeUserExpirationUpdater: " + app.toString() + "======");

    final GenericEntityDAO dao = (GenericEntityDAO) app.getSpringBean("genericEntityDao");
    app.init();

    dao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {

        int timeToExpireDays = DEFAULT_EXPIRATION_TIME_DAYS;
        if (app.isCommandLineFlagSet(EXPIRATION_TIME_OPTION[SHORT_OPTION_INDEX])) {
          timeToExpireDays = app.getCommandLineOptionValue(EXPIRATION_TIME_OPTION[SHORT_OPTION_INDEX], Integer.class);
        }
        try
        {
          try
          {
            if (app.isCommandLineFlagSet(NOTIFY_DAYS_AHEAD_OPTION[SHORT_OPTION_INDEX]))
            {
              Integer daysAheadToNotify = app.getCommandLineOptionValue(NOTIFY_DAYS_AHEAD_OPTION[SHORT_OPTION_INDEX],
                                                                        Integer.class);
              app.notifyAhead(daysAheadToNotify, timeToExpireDays);
            }
            else if (app.isCommandLineFlagSet(EXPIRE_OPTION[SHORT_OPTION_INDEX]))
            {
              app.expire(timeToExpireDays);
            }
            else {
              app.showHelpAndExit("Must specify either the \"" + NOTIFY_DAYS_AHEAD_OPTION[LONG_OPTION_INDEX] +
                "\" option or the \"" +
                           EXPIRE_OPTION[LONG_OPTION_INDEX] + "\" option");
            }
          }
          catch (OperationRestrictedException e) {
            app.sendErrorMail("OperationRestrictedException: Could not complete expiration service", app.toString(), e);
            throw new DAOTransactionRollbackException(e);
          }
        }
        catch (MessagingException e) {
          String msg = "Admin email operation not completed due to MessagingException";
          log.error(msg + ":\nApp: " + app.toString(), e);
          throw new DAOTransactionRollbackException(msg, e);
        }

        if (app.isCommandLineFlagSet(TEST_ONLY[SHORT_OPTION_INDEX])) {
          throw new DAOTransactionRollbackException("Rollback, testing only");
        }
      }
    });
    log.info("==== finishedSmallMoleculeUserExpirationUpdater ======");
  }

  private void notifyAhead(final Integer daysToNotify, final int ageInDays)
    throws MessagingException
  {
    LocalDate expireDate = new LocalDate().plusDays(daysToNotify);
    LocalDate maxPerformedDate = expireDate.minusDays(ageInDays);

    List<Pair<ScreeningRoomUser,ChecklistItemEvent>> pairList =
      _userAgreementUpdater.findUsersWithOldSMAgreements(maxPerformedDate, false);

    if (pairList.isEmpty()) {
      String subject = getMessages().getMessage("admin.users.smallMoleculeUserAgreementExpiration.warningNotification.noaction.subject");
      String msg = "No Users have agreements (that haven't already been notified) dated earlier than the specified cutoff date: " +
        maxPerformedDate
               +
        ", or (now - ageInDaysToExpire + daysToNotify): (" +
        new LocalDate() +
        " - " +
        ageInDays +
        "D + " +
        daysToNotify + "D).";
      sendAdminEmails(subject, msg);
    }
    else {
      // send Admin summary email
      String subject = getMessages().getMessage("admin.users.smallMoleculeUserAgreementExpiration.warningNotification.subject",
                                                pairList.size(),
                                                daysToNotify);
      StringBuilder msg =
        new StringBuilder(getMessages().getMessage("admin.users.smallMoleculeUserAgreementExpiration.warningNotification.messageBoilerplate",
                                                   pairList.size(),
                                                   daysToNotify));

      if (isAdminEmailOnly()) msg.append("\n----NOTE: sending email only to data sharing level admins ---");
      if (isCommandLineFlagSet(TEST_ONLY[SHORT_OPTION_INDEX])) {
        subject = "[TEST ONLY, no commits] " + subject;
        msg.append("\n----TEST Only: no database changes committed.-------");
      }

      msg.append("\nUsers: \n");
      msg.append(printUserHeader() + "| Checklist Item Date\n");
      for (Pair<ScreeningRoomUser,ChecklistItemEvent> pair : pairList) {
        msg.append(printUser(pair.getFirst()) + "| " + pair.getSecond().getDatePerformed() + "\n");
      }

      sendAdminEmails(subject, msg.toString(), _userAgreementUpdater.findUserAgreementAdmins());

      if (isAdminEmailOnly() || isCommandLineFlagSet(TEST_ONLY[SHORT_OPTION_INDEX])) {
        for (Pair<ScreeningRoomUser,ChecklistItemEvent> pair : pairList) {
          // set the flag so that we don't notify for this CIE again.
          _userAgreementUpdater.setLastNotifiedSMUAChecklistItemEvent(pair.getFirst(), pair.getSecond());
        }
      }
      else {
        // send user an email
        Pair<String,String> subjectMessage = getExpireNotificationSubjectMessage();
        String message = MessageFormat.format(subjectMessage.getSecond(), EXPIRE_DATE_FORMATTER.print(expireDate));
        for (Pair<ScreeningRoomUser,ChecklistItemEvent> pair : pairList) {
          sendEmail(subjectMessage.getFirst(), message, pair.getFirst());

          _userAgreementUpdater.setLastNotifiedSMUAChecklistItemEvent(pair.getFirst(), pair.getSecond());
        }
      }
    }
  }

  private void expire(int timeToExpireDays)
    throws MessagingException
  {
    LocalDate expireDate = new LocalDate().minusDays(timeToExpireDays);

    List<Pair<ScreeningRoomUser,List<AdministrativeActivity>>> updates = Lists.newLinkedList();
    List<Pair<ScreeningRoomUser,ChecklistItemEvent>> pairList =
      _userAgreementUpdater.findUsersWithOldSMAgreements(expireDate, true);

    for (Pair<ScreeningRoomUser,ChecklistItemEvent> pair : pairList) {
      updates.add(new Pair<ScreeningRoomUser,List<AdministrativeActivity>>(
                                                                           pair.getFirst(), _userAgreementUpdater.expireUser(pair.getFirst(), findAdministratorUser())));
    }

    if (updates.isEmpty()) {
      String subject = getMessages().getMessage("admin.users.smallMoleculeUserAgreementExpiration.expiration.noaction.subject");
      String msg = "No Users have (non expired) agreements dated earlier than the specified cutoff date: " + expireDate;
      sendAdminEmails(subject, msg);
    }
    else {
      //Send a summary email to the admin and the recipient list
      String subject = getMessages().getMessage("admin.users.smallMoleculeUserAgreementExpiration.expiration.subject",
                                                updates.size());
      StringBuilder msg =
        new StringBuilder(getMessages().getMessage("admin.users.smallMoleculeUserAgreementExpiration.expiration.messageBoilerplate",
                                                   updates.size(),
                                                   timeToExpireDays));
      if (isCommandLineFlagSet(TEST_ONLY[SHORT_OPTION_INDEX])) {
        subject = "Testing: " + subject;
        msg.append("\n----TEST Only: no database changes committed.-------");
      }
      msg.append("\n\n");

      for (Pair<ScreeningRoomUser,List<AdministrativeActivity>> result : updates) {
        msg.append("\nUser: \n");
        msg.append(printUserHeader() + "\n");
        msg.append(printUser(result.getFirst()) + "\n");
        for (AdministrativeActivity activity : result.getSecond()) {
          msg.append("\nComments:" + activity.getComments());
        }
        msg.append("\n");
      }
      sendAdminEmails(subject, msg.toString(), _userAgreementUpdater.findUserAgreementAdmins());
    }
  }

  /**
   * Return the subject first and the message second.
   * Message:
   * {0} Expiration Date
   * 
   * @throws MessagingException
   */
  private Pair<String,String> getExpireNotificationSubjectMessage() throws MessagingException
  {
    InputStream in = null;
    if (isCommandLineFlagSet(EXPIRATION_EMAIL_MESSAGE_LOCATION[SHORT_OPTION_INDEX])) {
      try {
        in = new FileInputStream(new File(getCommandLineOptionValue(EXPIRATION_EMAIL_MESSAGE_LOCATION[SHORT_OPTION_INDEX])));
      }
      catch (FileNotFoundException e) {
        sendErrorMail("Operation not completed for SmallMoleculeUserExpirationUpdater, could not locate expiration message", toString(), e);
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