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
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.joda.time.LocalDate;

import com.google.common.collect.Lists;

import edu.harvard.med.iccbl.screensaver.io.AdminEmailApplication;
import edu.harvard.med.iccbl.screensaver.service.users.UserAgreementUpdater;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.DAOTransactionRollbackException;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.AdministrativeActivity;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ChecklistItemEvent;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.service.EmailService;
import edu.harvard.med.screensaver.util.Pair;

/**
* Locates the _not yet expired_ users who have a SMUA with an activation on or before the date given, and it expires them.<br/>
* The date will be 2 years before the current time.<br/>
* see {@link UserAgreementUpdater#findUsersWithOldSMAgreements(LocalDate)}
 * 
 */
public class SmallMoleculeUserExpirationUpdater extends AdminEmailApplication
{
  
  UserAgreementUpdater _userAgreementUpdater = null;
  
  public SmallMoleculeUserExpirationUpdater(String[] cmdLineArgs)
  {
    super(cmdLineArgs);
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
  { "expireindays", 
    "days", 
    "expire-in-days", 
    "(optional) time, in days, for the Small Molecule User Agreement to expire (using date of activation value).  Default value is " + DEFAULT_EXPIRATION_TIME_DAYS + " days." 
  };

  public static final String[] EXPIRE_OPTION = 
  { "expire", 
    "", 
    "expire-user-roles", 
    "specify this option to expire the users who's Small Molecule User Agreement is dated more than " + EXPIRATION_TIME_OPTION[LONG_OPTION_INDEX] + " days in the past."
  };

  public static final String[] EXPIRATION_EMAIL_MESSAGE_LOCATION = 
  { 
  "expirationemailfilelocation", "file location",
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
    
    try {
      if (!app.processOptions(/* acceptDatabaseOptions= */true, /* showHelpOnError= */true)) 
      {
        return;
      }

      final GenericEntityDAO dao = (GenericEntityDAO) app.getSpringBean("genericEntityDao");

      dao.doInTransaction(new DAOTransaction() {
        public void runTransaction()
        {
          try 
          {
            AdministratorUser admin = app.getAdminUser(dao);
            EmailService emailService = app.getEmailServiceBasedOnCommandLineOption(admin);

            int timeToExpireDays = DEFAULT_EXPIRATION_TIME_DAYS;
            if(app.isCommandLineFlagSet(EXPIRATION_TIME_OPTION[SHORT_OPTION_INDEX])) {
              timeToExpireDays = app.getCommandLineOptionValue(EXPIRATION_TIME_OPTION[SHORT_OPTION_INDEX], Integer.class);
            }
            
            if (app.isCommandLineFlagSet(NOTIFY_DAYS_AHEAD_OPTION[SHORT_OPTION_INDEX])) 
            {
              Integer daysAheadToNotify = app.getCommandLineOptionValue(NOTIFY_DAYS_AHEAD_OPTION[SHORT_OPTION_INDEX],
                                                                        Integer.class);
              app.notifyAhead(admin, emailService, daysAheadToNotify, timeToExpireDays);
            }
            else if (app.isCommandLineFlagSet(EXPIRE_OPTION[SHORT_OPTION_INDEX]))
            {
              app.expire(admin, emailService, timeToExpireDays);
            } else {
              log.info("Must specify either the \"" + NOTIFY_DAYS_AHEAD_OPTION[LONG_OPTION_INDEX] + "\" option or the \"" + EXPIRE_OPTION[LONG_OPTION_INDEX] + "\" option");
              app.showHelp();
              System.exit(1);
            }
            if(app.isCommandLineFlagSet(TEST_ONLY[SHORT_OPTION_INDEX])) {
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


  private void notifyAhead(AdministratorUser admin, EmailService emailService, final Integer daysToNotify, final int ageInDays)
    throws ParseException,
    MessagingException, IOException
  {
    LocalDate expireDate = new LocalDate().plusDays(daysToNotify);
    LocalDate maxPerformedDate = expireDate.minusDays(ageInDays);

    InternetAddress adminEmail = new InternetAddress(admin.getEmail());

    List<Pair<ScreeningRoomUser, ChecklistItemEvent>> pairList = 
      _userAgreementUpdater.findUsersWithOldSMAgreements(maxPerformedDate, false);

    if (pairList.isEmpty()) {
      String subject = getMessages().getMessage("admin.users.smallMoleculeUserAgreementExpiration.warningNotification.noaction.subject");
      String msg = "No Users have agreements (that haven't already been notified) dated earlier than the specified cutoff date: " + maxPerformedDate 
               + ", or (now - ageInDaysToExpire + daysToNotify): (" + new LocalDate() + " - " + ageInDays + "D + "+ daysToNotify + "D).";
      emailService.send(subject,
                        msg,
                        adminEmail,
                        new InternetAddress[] { adminEmail }, null);
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
      
      if(isAdminEmailOnly()) msg.append("\n----NOTE: sending email only to data sharing level admins ---");
      if(isCommandLineFlagSet(TEST_ONLY[SHORT_OPTION_INDEX])) {
        subject = "Testing: " + subject;
        msg.append("\n----TEST Only: no database changes committed.-------");
      }

      msg.append("\nUsers: \n");
      msg.append(printUserHeader() + "| Checklist Item Date\n");
      for(Pair<ScreeningRoomUser, ChecklistItemEvent> pair: pairList)
      {
        msg.append(printUser(pair.getFirst()) + "| " + pair.getSecond().getDatePerformed() + "\n");
      }      

      Set<InternetAddress> adminRecipients = getExtraRecipients();
      adminRecipients.add(adminEmail);
      adminRecipients.addAll(getUserAgreementAdmins(adminEmail, emailService));

      emailService.send(subject,
                        msg.toString(),
                        adminEmail,
                        adminRecipients.toArray(new InternetAddress[] {}),
                        (InternetAddress[])null);
      if(isAdminEmailOnly() || isCommandLineFlagSet(TEST_ONLY[SHORT_OPTION_INDEX]))
      {
        for(Pair<ScreeningRoomUser, ChecklistItemEvent> pair: pairList)
        {
          // set the flag so that we don't notify for this CIE again.
          _userAgreementUpdater.setLastNotifiedSMUAChecklistItemEvent(pair.getFirst(), pair.getSecond());
        }
      }
      else
      {
        // send user an email
        Pair<String,String> subjectMessage = getExpireNotificationSubjectMessage(); 
        String message = MessageFormat.format(subjectMessage.getSecond(), EXPIRE_DATE_FORMATTER.print(expireDate));
        for (Pair<ScreeningRoomUser, ChecklistItemEvent> pair: pairList) 
        {
          try {
            InternetAddress userAddress = new InternetAddress(pair.getFirst().getEmail());
            emailService.send(subjectMessage.getFirst(),
                              message,
                              adminEmail,
                              new InternetAddress[] { userAddress },
                              null);
            // set the flag so that we don't notify for this CIE again.
            _userAgreementUpdater.setLastNotifiedSMUAChecklistItemEvent(pair.getFirst(), pair.getSecond());
          }catch(MessagingException e) {
            //TODO: verify that we do _not_ want to stop on email addressee failure! - sde4
            String errmsg = "Error, could not send the email to the User: " + 
                          printUserInformation(pair.getFirst());
            log.info(errmsg,e);
            emailService.send("Send Failure for: " + subjectMessage.getFirst(),
                              errmsg + "\n\n========Original Message=========" + message,
                              adminEmail,
                              adminRecipients.toArray(new InternetAddress[] {}),
                              null );
          }
        }
      }
    }
  }

  private void expire(AdministratorUser admin, EmailService emailService, int timeToExpireDays)
    throws ParseException,
    MessagingException, IOException
  {
    LocalDate expireDate = new LocalDate().minusDays(timeToExpireDays);

    List<Pair<ScreeningRoomUser,List<AdministrativeActivity>>> updates = Lists.newLinkedList();
    List<Pair<ScreeningRoomUser, ChecklistItemEvent>> pairList = 
      _userAgreementUpdater.findUsersWithOldSMAgreements(expireDate, true);
    
    for (Pair<ScreeningRoomUser, ChecklistItemEvent> pair : pairList) {
      updates.add(new Pair<ScreeningRoomUser,List<AdministrativeActivity>>(
        pair.getFirst(),_userAgreementUpdater.expireUser(pair.getFirst(),admin)));
    }

    InternetAddress adminEmail = new InternetAddress(admin.getEmail());
    
    if (updates.isEmpty()) {
      String subject = getMessages().getMessage("admin.users.smallMoleculeUserAgreementExpiration.expiration.noaction.subject");
      String msg = "No Users have (non expired) agreements dated earlier than the specified cutoff date: " + expireDate;
      log.info(msg);
      emailService.send(subject,
                        msg,
                        adminEmail,
                        new InternetAddress[] { adminEmail }, null);
    }
    else {
      //Send a summary email to the admin and the recipient list
      String subject = getMessages().getMessage("admin.users.smallMoleculeUserAgreementExpiration.expiration.subject", 
                                            updates.size());
      StringBuilder msg = 
        new StringBuilder(getMessages().getMessage("admin.users.smallMoleculeUserAgreementExpiration.expiration.messageBoilerplate",
                                               updates.size(),
                                               timeToExpireDays));
      if(isCommandLineFlagSet(TEST_ONLY[SHORT_OPTION_INDEX])) {
        subject = "Testing: " + subject;
        msg.append("\n----TEST Only: no database changes committed.-------");
      }
      msg.append("\n\n");

      for (Pair<ScreeningRoomUser,List<AdministrativeActivity>> result : updates) 
      {
        msg.append("\nUser: \n");
        msg.append(printUserHeader() + "\n");
        msg.append(printUser(result.getFirst()) + "\n");
        for (AdministrativeActivity activity : result.getSecond()) 
        {
          msg.append("\nComments:" + activity.getComments());
        }
        msg.append("\n");
      }
      Set<InternetAddress> adminRecipients = getExtraRecipients();
      adminRecipients.add(adminEmail);
      adminRecipients.addAll(getUserAgreementAdmins(adminEmail, emailService));

      emailService.send(subject,
                        msg.toString(),
                        adminEmail,
                        adminRecipients.toArray(new InternetAddress[] {}),
                        (InternetAddress[])null);

      // Have removed this per spec
      //      // Send email to the users
      //      List<String> ccrecipients = recipients;
      //      Pair<String,String> subjectMessage = getExpireNotificationSubjectMessage(); // Note: nothing to replace
      //      for (ScreeningRoomUser user: oldUsers) {
      //        msg = new StringBuilder("Detail:\n");
      //        emailService.send(subjectMessage.getFirst(),
      //                          subjectMessage.getSecond(),
      //                          admin.getEmail(),
      //                          new String[] { user.getEmail() },
      //                          ccrecipients.toArray(new String[] {})
      //                          );
      //      }
    }
  }

  private Collection<? extends InternetAddress> getUserAgreementAdmins(InternetAddress emailAddressForErrors, 
                                                                       EmailService emailServiceForErrorReporting)
                                                              throws MessagingException
  {
    List<InternetAddress> adminRecipients = Lists.newLinkedList();
    List<MessagingException> errors = Lists.newLinkedList();
    List<String> failUsers = Lists.newLinkedList();
    for(ScreensaverUser adminUser: _userAgreementUpdater.findUserAgreementAdmins())
    {
      try {
        adminRecipients.add(new InternetAddress(adminUser.getEmail()));
      }catch(MessagingException e) {
        errors.add(e);
        failUsers.add("" + printUserInformation(adminUser));
      }
    }
    if(!errors.isEmpty())
    {
      String errMsg = "Warn: could not validate the email address for the dataSharingLevelAdmin roles";
      emailServiceForErrorReporting.send("Error getting the email address for dataSharingLevelAdmin roles", 
                                         errMsg + "\n" + failUsers + "\nErrors: " + errors, 
                                         emailAddressForErrors, 
                                         new InternetAddress[] { emailAddressForErrors } , null );
    }
    return adminRecipients;
  }

  /**
   * Return the subject first and the message second.  
   * Message:
   * {0} Expiration Date
   * @throws ParseException 
   */
  private Pair<String,String> getExpireNotificationSubjectMessage() throws IOException, ParseException
  {
    InputStream in = null;
    if (isCommandLineFlagSet(EXPIRATION_EMAIL_MESSAGE_LOCATION[SHORT_OPTION_INDEX])) {
      in = new FileInputStream(new File(getCommandLineOptionValue(EXPIRATION_EMAIL_MESSAGE_LOCATION[SHORT_OPTION_INDEX])));
    }
    else {
      in = this.getClass()
               .getResourceAsStream(EXPIRATION_MESSAGE_TXT_LOCATION);
    }      
    Scanner scanner = new Scanner(in);
    try {
      StringBuilder builder = new StringBuilder();
      String subject = scanner.nextLine(); // first line is the subject
      while(scanner.hasNextLine())
      {
        builder.append(scanner.nextLine()).append("\n");
      }
      return Pair.newPair(subject, builder.toString());
    }
    finally
    {
      scanner.close();
    }
  }
}