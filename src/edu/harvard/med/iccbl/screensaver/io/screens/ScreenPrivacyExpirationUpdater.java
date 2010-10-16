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
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import edu.harvard.med.iccbl.screensaver.service.screens.ScreenDataSharingLevelUpdater;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.DAOTransactionRollbackException;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.AdministrativeActivity;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenDataSharingLevel;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.service.EmailService;
import edu.harvard.med.screensaver.util.Pair;

/**
 * Find and "expire" the private screens for which the dataPrivacyExpirationDate
 * is greater than the time at which this script is run. <br/>
 * "Expiration" of a screen means that the DSL is set to ScreenDataSharingLevel.MUTUAL_SCREENS,
 * when it was something greater before. see
 * {@link ScreenDataSharingLevel#MUTUAL_SCREENS}
 * <br>
 * <br>
 * see [#2175] Expiration services to become notification services only.
 */
public class ScreenPrivacyExpirationUpdater extends AdminEmailApplication
{
  private static final String EXPIRATION_MESSAGE_TXT_LOCATION = "../../../../../../../screenPrivacyExpirationMessage.txt";

  public static int SCREEN_ACTIVITY_DATA_PRIVACY_EXPIRATION_AGE_DAYS = 365*2 + 30*2;

  private ScreenDataSharingLevelUpdater _privacyUpdater = null;
  
  public ScreenPrivacyExpirationUpdater(String[] cmdLineArgs)
  {
    super(cmdLineArgs);
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
    "notifyofexpiration","days", 
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
    "expirationemailfilelocation", "file location",
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
    
    try {
      if (!app.processOptions(/* acceptDatabaseOptions= */true,
                              /* acceptAdminUserOptions= */true,
                              /* showHelpOnError= */true)) {
        return;
      }

      final GenericEntityDAO dao = (GenericEntityDAO) app.getSpringBean("genericEntityDao");

      dao.doInTransaction(new DAOTransaction() {
        public void runTransaction()
        {
          try {
            AdministratorUser admin = app.findAdministratorUser();
     
            EmailService emailService = app.getEmailServiceBasedOnCommandLineOption(admin);

            // check that not too many options are specified
            int numberOfActions = 0;
            if (app.isCommandLineFlagSet(NOTIFY_PRIVACY_EXPIRATION[SHORT_OPTION_INDEX])) numberOfActions++; 
            if (app.isCommandLineFlagSet(NOTIFY_OF_PUBLICATIONS[SHORT_OPTION_INDEX])) numberOfActions++;
            if (app.isCommandLineFlagSet(EXPIRE_PRIVACY[SHORT_OPTION_INDEX])) numberOfActions++;
            if (app.isCommandLineFlagSet(ADJUST_DATA_PRIVACY_EXPIRATION_DATE_BASED_ON_ACTIVITY[SHORT_OPTION_INDEX])) numberOfActions++; 
            if (numberOfActions > 1 ) 
            {
              log.error("May only specify one of: " + NOTIFY_PRIVACY_EXPIRATION[SHORT_OPTION_INDEX] 
                                                    + ", " + NOTIFY_OF_PUBLICATIONS[SHORT_OPTION_INDEX] 
                                                    + ", " + EXPIRE_PRIVACY[SHORT_OPTION_INDEX] 
                                                    + ", " + ADJUST_DATA_PRIVACY_EXPIRATION_DATE_BASED_ON_ACTIVITY[SHORT_OPTION_INDEX] );
              System.exit(1);
            }
            
            if (app.isCommandLineFlagSet(NOTIFY_PRIVACY_EXPIRATION[SHORT_OPTION_INDEX])) 
            {
              Integer daysAheadToNotify = app.getCommandLineOptionValue(NOTIFY_PRIVACY_EXPIRATION[SHORT_OPTION_INDEX],
                                                                        Integer.class);
              app.findNewExpiredAndNotifyAhead(admin, daysAheadToNotify, emailService);
            }
            else if (app.isCommandLineFlagSet(NOTIFY_OF_PUBLICATIONS[SHORT_OPTION_INDEX])) 
            {
              app.notifyOfPublications(admin, emailService);
            }
            else if (app.isCommandLineFlagSet(EXPIRE_PRIVACY[SHORT_OPTION_INDEX])) 
            {
              app.expireScreenDataSharingLevels(admin, emailService);
            }
            else if (app.isCommandLineFlagSet(ADJUST_DATA_PRIVACY_EXPIRATION_DATE_BASED_ON_ACTIVITY[SHORT_OPTION_INDEX])) 
            {
              Integer ageToExpireFromActivityDateInDays = 
                app.getCommandLineOptionValue(ADJUST_DATA_PRIVACY_EXPIRATION_DATE_BASED_ON_ACTIVITY[SHORT_OPTION_INDEX],Integer.class);
              app.adjustDataPrivacyExpirationByActivities(admin, ageToExpireFromActivityDateInDays, emailService);
            }else {
              log.error("No action specified (expire, notify of privacy expirations, notify of publications, or adjust)?");
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
  
  private void notifyOfPublications(AdministratorUser admin,
                                    EmailService emailService) throws MessagingException, ParseException
  {
    InternetAddress adminEmail = getEmail(admin);
    List<Screen> publishedScreens = _privacyUpdater.findNewPublishedPrivate();

    if (publishedScreens.isEmpty()) {
      String subject = getMessages().getMessage("admin.screens.dataPrivacyExpiration.publicationNotification.noaction.subject");
      String msg = "No published private Screens.";
      log.info(msg);
      emailService.send(subject,
                        msg,
                        adminEmail,
                        new InternetAddress[] { adminEmail }, null);
    }
    else {
      // 1. send a summary email to the admin, dateSharingLevelAdmin's and recipients specified on the command line
      String subject = getMessages().getMessage("admin.screens.dataPrivacyExpiration.publicationNotification.subject", 
                                                publishedScreens.size(),
                                                new LocalDate());
      StringBuilder msg = new StringBuilder(
          getMessages().getMessage("admin.screens.dataPrivacyExpiration.publicationNotification.messageBoilerplate",
                               publishedScreens.size(), new LocalDate()));
      msg.append("\n");
      
      Set<InternetAddress> adminRecipients = getExtraRecipients();
      adminRecipients.add(adminEmail);
      adminRecipients.addAll(getDataSharingLevelAdminEmails(adminEmail, emailService));
      
      msg.append("\n\n");
      for (Screen screen : publishedScreens) 
      {
        msg.append("\nScreen with Publication:\n");
        msg.append(printScreenHeader() + "\n");
        msg.append(printScreen(screen) + "\n");
        msg.append("Associated Users: \n");
        msg.append("\t" + printUserHeader() + "\n");
        for(ScreensaverUser user:screen.getAssociatedScreeningRoomUsers())
        {
          msg.append("\t").append(printUser(user) + "\n");
        }
      }
      emailService.send(subject,
                     msg.toString(),
                     adminEmail,
                     adminRecipients.toArray(new InternetAddress[] {}), null);
    }    
  }

  private void adjustDataPrivacyExpirationByActivities(AdministratorUser admin, 
                                                       Integer ageToExpireFromActivityDateInDays,
                                                       EmailService emailService) throws ParseException, MessagingException
  {
    ScreenDataSharingLevelUpdater.DataPrivacyAdjustment adjustment 
        = _privacyUpdater.adjustDataPrivacyExpirationByActivities(ageToExpireFromActivityDateInDays, admin);
    InternetAddress adminEmail = getEmail(admin);

    if (adjustment.isEmpty(isCommandLineFlagSet(NOTIFY_OF_OVERRIDES[SHORT_OPTION_INDEX]))) {
      String subject = getMessages().getMessage("admin.screens.dataPrivacyExpiration.dataPrivacyExpirationdate.adjustment.noaction.subject");
      String msg = "No Screens with Activities to adjust the dataPrivacyExpirationDate";
      log.info(msg);
      //TODO: do we want this noise if the results can be put in a log file?
      emailService.send(subject,
                        msg,
                        adminEmail,
                        new InternetAddress[] { adminEmail }, null);
    }
    else 
    {
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
      if(isCommandLineFlagSet(TEST_ONLY[SHORT_OPTION_INDEX])) {
        subject = "Testing: " + subject;
        msg.append("\n----TEST Only: no database changes committed.-------");
      }
      msg.append("\n\n");
      
      if(!adjustment.screensAdjusted.isEmpty())
      {
        msg.append("\n\nAdjusted to " + ageToExpireFromActivityDateInDays + " days from the last Library Screening Activity:\n");
        for (Pair<Screen,AdministrativeActivity> result:adjustment.screensAdjusted) 
        {
          Screen screen = result.getFirst();
          msg.append("\nAdjusted Screen:\n");
          msg.append(printScreenHeader() + "\n");
          msg.append(printScreen(screen) + "\n");
          msg.append("Comment: " + result.getSecond().getComments() + "\n" );
          msg.append("Associated Users: \n");
          msg.append("\t" + printUserHeader() + "\n");
          for(ScreensaverUser user:screen.getAssociatedScreeningRoomUsers())
          {
            msg.append("\t").append(printUser(user) + "\n");
          }
        }
      }
      
      if(!adjustment.screensAdjustedToAllowed.isEmpty())
      {
        msg.append("\n\nAdjustment to the allowed value for the screens:\n");
        for (Pair<Screen,AdministrativeActivity> result:adjustment.screensAdjustedToAllowed) 
        {
          Screen screen = result.getFirst();
          msg.append("\nAdjusted Screen:\n");
          msg.append(printScreenHeader() + "\n");
          msg.append(printScreen(screen) + "\n");
          msg.append("Comment: " + result.getSecond().getComments() + "\n" );
          msg.append("Associated Users: \n");
          msg.append("\t" + printUserHeader() + "\n");
          for(ScreensaverUser user:screen.getAssociatedScreeningRoomUsers())
          {
            msg.append("\t").append(printUser(user) + "\n");
          }
        }
      }
      
      if( isCommandLineFlagSet(NOTIFY_OF_OVERRIDES[SHORT_OPTION_INDEX]) 
        && !adjustment.screenPrivacyAdjustmentNotAllowed.isEmpty())
      {
        msg.append("\n\nAdjustment not allowed for the screens:\n");
        for (Pair<Screen,String> result:adjustment.screenPrivacyAdjustmentNotAllowed) 
        {
          Screen screen = result.getFirst();
          msg.append("\nAdjustment overridden for Screen:\n");
          msg.append(printScreenHeader() + "\n");
          msg.append(printScreen(screen) + "\n");
          msg.append("Comment: " + result.getSecond() + "\n" );
          msg.append("Associated Users: \n");
          msg.append("\t" + printUserHeader() + "\n");
          for(ScreensaverUser user:screen.getAssociatedScreeningRoomUsers())
          {
            msg.append("\t").append(printUser(user) + "\n");
          }
        }
      }
      
      Set<InternetAddress> adminRecipients = getExtraRecipients();
      adminRecipients.add(adminEmail);
      adminRecipients.addAll(getDataSharingLevelAdminEmails(adminEmail, emailService));

      emailService.send(subject,
                        msg.toString(),
                        adminEmail,
                        adminRecipients.toArray(new InternetAddress[] {}), null);
                        
    }
  }

  
  private void findNewExpiredAndNotifyAhead(AdministratorUser admin, Integer daysAheadToNotify, EmailService emailService)
    throws ParseException, MessagingException, IOException
  {
    LocalDate expireDate = new LocalDate().plusDays(daysAheadToNotify);

    InternetAddress adminEmail = getEmail(admin);
    
    List<Screen> oldScreens = new ArrayList<Screen>(_privacyUpdater.findNewExpiredNotNotified(expireDate));
    // sort by DPED
    Collections.sort(oldScreens, new Comparator<Screen>() {
      public int compare(Screen o1, Screen o2)
      {
        if(o1 == null || o2 == null) return o1==null?o2==null?0:-1:1;
        return o1.getDataPrivacyExpirationDate().compareTo(o2.getDataPrivacyExpirationDate());
      }
    });
    
    if (oldScreens.isEmpty()) {
      String subject = getMessages().getMessage("admin.screens.dataPrivacyExpiration.dataPrivacyExpirattion.warningNotification.noaction.subject", daysAheadToNotify);
      String msg = "No Screens have agreements set to expire prior to: " 
        + expireDate + " that have not already been notified.";
      log.info(msg);
      emailService.send(subject,
                        msg,
                        adminEmail,
                        new InternetAddress[] { adminEmail }, null);
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
      if(isAdminEmailOnly()) msg.append("\n---- NOTE: sending email only to data sharing level admins ---");
      if(isCommandLineFlagSet(TEST_ONLY[SHORT_OPTION_INDEX])) {
        subject = "Testing: " + subject;
        msg.append("\n----TEST Only: no database changes committed.-------");
      }
      
      msg.append("\nScreens expiring on or before " + EXPIRE_DATE_FORMATTER.print(expireDate) + "\n");
      for (Screen screen : oldScreens) 
      {
        msg.append("\nScreen: \n");
        msg.append(printScreenHeader() + "\n");
        msg.append(printScreen(screen) + "\n" );
        msg.append("Associated Users: \n");
        msg.append("\t" + printUserHeader() + "\n");
        for(ScreensaverUser user:screen.getAssociatedScreeningRoomUsers())
        {
          msg.append("\t").append(printUser(user)).append("\n");
        }
      }

      Pair<String,String> subjectMessage = getScreenExpireNotificationSubjectMessage(); // Note: nothing to replace
      String notificationMessage = MessageFormat.format(subjectMessage.getSecond(),
                                            "---",
                                            "[-- a Small Molecule Screen Title]", 
                                            "[expire_date]" );
      msg.append("\n\n=============================example email=================================");
      msg.append("\nSubject: " + subjectMessage.getFirst());
      msg.append(notificationMessage);
      
      Set<InternetAddress> adminRecipients = getExtraRecipients();
      adminRecipients.add(adminEmail);
      adminRecipients.addAll(getDataSharingLevelAdminEmails(adminEmail, emailService));

      emailService.send(subject,
                     msg.toString(),
                     adminEmail,
                     adminRecipients.toArray(new InternetAddress[] {}), null);
      if(isAdminEmailOnly() || isCommandLineFlagSet(TEST_ONLY[SHORT_OPTION_INDEX]))
      {
        for (Screen screen : oldScreens) 
        {
          _privacyUpdater.setDataPrivacyExpirationNotifiedDate(screen);
        }
      } else {
        // 2. For each screen, send a notification to the screen's PI, lead screener, and collaborators
        List<MessagingException> exceptions = Lists.newLinkedList();
        for (Screen screen : oldScreens) 
        {
          List<InternetAddress> recipients = Lists.newLinkedList();
          for(ScreensaverUser user: screen.getAssociatedScreeningRoomUsers())
          {
            try {
              recipients.add(new InternetAddress(user.getEmail()));  
            }catch(MessagingException e) {
              String errMsg = "Warn: could not validate the email address for the dataSharingLevelAdmin role: " 
                + printUserInformation(user);
              emailService.send("Error sending expiration notification for screen: #" + screen.getFacilityId(),
                                errMsg,
                                adminEmail,
                                adminRecipients.toArray(new InternetAddress[] {}), null);
            }
          }
          try {
            notificationMessage = MessageFormat.format(subjectMessage.getSecond(),
                                      screen.getFacilityId(),
                                      getScreenTitle(screen), 
                                      EXPIRE_DATE_FORMATTER.print(screen.getDataPrivacyExpirationDate()) );

            emailService.send(subjectMessage.getFirst(),
                              notificationMessage,
                              adminEmail,
                              recipients.toArray(new InternetAddress[] {}),
                              null);
            //if(!isAdminEmailOnly())
              _privacyUpdater.setDataPrivacyExpirationNotifiedDate(screen);
          }
          catch (MessagingException e)
          {
            //TODO: should this be a transaction rollback? yes, collect and then rethrow - sde4
            log.info("Could not send a message for: " + screen + " to " + recipients, e);
            exceptions.add(e);
          }
          if(!exceptions.isEmpty())
          {
            String errMsg = "Warn: Email errors while trying to send Data PrivacyExpiration Emails";
            emailService.send(errMsg,
                            "Error while emailing for screens: " + oldScreens + "\nErrors:\n" + exceptions,
                            adminEmail,
                            adminRecipients.toArray(new InternetAddress[] {}), null);
            throw new MessagingException("Messaging exceptions thrown, stacktrace " + exceptions, exceptions.get(0));
          }
        }
      }
    }
  }
  
  private void expireScreenDataSharingLevels(AdministratorUser admin, EmailService emailService)
    throws IllegalArgumentException,
    ParseException,
    MessagingException
  {
    LocalDate expireDate = new LocalDate();
    List<Pair<Screen,AdministrativeActivity>> results = _privacyUpdater.expireScreenDataSharingLevels(expireDate,admin);
    
    InternetAddress adminEmail = getEmail(admin);

    if (results.isEmpty()) {
      String subject = getMessages().getMessage("admin.screens.dataPrivacyExpiration.dataPrivacyExpirattion.notification.noaction.subject");
      String msg = "No Small Molecule Screens with SDSL > 1 have agreements dated on or before: " + expireDate;
      log.info(msg);
      emailService.send(subject,
                        msg,
                        adminEmail,
                        new InternetAddress[] {adminEmail}, null);
    }
    else {
      // Send a notification that the screens have expired to this admin and recipients specified on the command line
      String subject = getMessages().getMessage("admin.screens.dataPrivacyExpiration.dataPrivacyExpirattion.notification.subject", results.size());
      StringBuilder msg = new StringBuilder(
          getMessages().getMessage("admin.screens.dataPrivacyExpiration.dataPrivacyExpirattion.notification.messageBoilerplate", 
                               results.size(), new LocalDate()));
      if(isCommandLineFlagSet(TEST_ONLY[SHORT_OPTION_INDEX])) {
        subject = "Testing: " + subject;
        msg.append("\n----TEST Only: no database changes committed.-------");
      }
      
      msg.append("\n");
      for (Pair<Screen,AdministrativeActivity> result : results) 
      {
        Screen screen = result.getFirst();
        msg.append("\nExpired Screen:\n");
        msg.append(printScreenHeader() + "\n");
        msg.append(printScreen(screen) + "\n");
        msg.append("Comment: " + result.getSecond().getComments() + "\n" );
        msg.append("Associated Users: \n");
        msg.append("\t" + printUserHeader() + "\n");
        for(ScreensaverUser user:screen.getAssociatedScreeningRoomUsers())
        {
          msg.append("\t").append(printUser(user) + "\n");
        }
      }

      Set<InternetAddress> adminRecipients = getExtraRecipients();
      adminRecipients.add(adminEmail);
      adminRecipients.addAll(getDataSharingLevelAdminEmails(adminEmail, emailService));
      
      emailService.send(subject,
                        msg.toString(),
                        adminEmail,
                        adminRecipients.toArray(new InternetAddress[] {}), null);
    }
  }
  
  //  private InternetAddress getEmail(ScreensaverUser admin)
  //    throws MessagingException
  //  {
  //    try {
  //      return new InternetAddress(admin.getEmail());
  //    }
  //    catch (AddressException e) {
  //      throw new MessagingException("Admin address is wrong: " + printUserInformation(admin), e);
  //    }
  //  }

  public List<InternetAddress> getDataSharingLevelAdminEmails(InternetAddress emailAddressForErrors, 
                                                              EmailService emailServiceForErrorReporting) 
                                                              throws MessagingException, ParseException
  {
    List<InternetAddress> adminRecipients = Lists.newLinkedList();
    List<MessagingException> errors = Lists.newLinkedList();
    List<String> failUsers = Lists.newLinkedList();
    for(ScreensaverUser adminUser: _privacyUpdater.findDataSharingLevelAdminUsers())
    {
      try {
        adminRecipients.add(getEmail(adminUser));
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
    switch (screen.getScreenType())
    {
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
   * @throws ParseException 
   */
  private Pair<String,String> getScreenExpireNotificationSubjectMessage() throws IOException, ParseException
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

  //  private String printScreensHtml(Collection<Screen> screens)
  //  {
  //    StringBuilder msg = new StringBuilder();
  //    msg.append("<table>");
  //    msg.append(printTableHtmlHeader(new String[] {
  //      "Number","Expiration Date", "Sharing Level", "Title", "Associated Users"
  //    }));
  //    for (Screen screen:screens) {
  //      printScreenHtml(msg, screen);
  //    }
  //    msg.append("</table>");
  //    return msg.toString();
  //  }
  //
  //  private void printScreenHtml(StringBuilder msg, Screen screen)
  //  {
  //    String tempUserTable = "<table>" + printTableHtmlHeader(new String[] {
  //      "Id", "Name", "Email"
  //    });
  //    for(ScreensaverUser user:screen.getAssociatedScreeningRoomUsers())
  //    {
  //      String id = StringUtils.isEmpty(user.getECommonsId())? user.getLoginId() : user.getECommonsId();
  //      tempUserTable += printTableRow(new String[] {
  //        id, user.getFullNameFirstLast(), user.getEmail()
  //      });
  //    }
  //    tempUserTable += "</table>";
  //    msg.append(printTableRow(new String[] {
  //      "" + screen.getName(),"" + screen.getDataPrivacyExpirationDate(), "" + screen.getDataSharingLevel(), screen.getTitle(), tempUserTable 
  //    }));
  //  }

  //  public static String printTableHtmlHeader(String[] headers)
  //  {
  //    StringBuffer buf = new StringBuffer();
  //    buf.append("<tr>");
  //    for(String header:headers) buf.append("<th>" + header + "</th>");
  //    buf.append("</tr>");
  //    return buf.toString();
  //  }
  //  
  //  public static String printTableRow(String[] row)
  //  {
  //    StringBuffer buf = new StringBuffer();
  //    buf.append("<tr>");
  //    for(String val:row) buf.append("<td>" + val + "</td>");
  //    buf.append("</tr>");
  //    return buf.toString();
  //  }
}