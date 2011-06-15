// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.iccbl.screensaver.io;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import edu.harvard.med.iccbl.screensaver.service.SmtpEmailService;
import edu.harvard.med.screensaver.io.CommandLineApplication;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.service.EmailService;
import edu.harvard.med.screensaver.service.ServiceMessages;

public class AdminEmailApplication extends CommandLineApplication
{
  private static Logger log = Logger.getLogger(AdminEmailApplication.class);

  public static final int SHORT_OPTION_INDEX = 0;
  public static final int ARG_INDEX = 1;
  public static final int LONG_OPTION_INDEX = 2;
  public static final int DESCRIPTION_INDEX = 3;

  public static final DateTimeFormatter EXPIRE_DATE_FORMATTER = DateTimeFormat.fullDate();
  private ServiceMessages _messages = null;
  
  public static final String[] EMAIL_RECIPIENT_LIST_OPTION = { 
    "mr", "comma-separated-list",
    "mail-recipient-list", 
    "Additional recipients to notify, delimited by \"" + EmailService.DELIMITER + "\"" 
    }; 
  
  public static final String[] NO_NOTIFY_OPTION = { 
    "noemail", "",
    "no-email", 
    "Do not send email notifications" 
    };
  
  public static final String[] TEST_EMAIL_ONLY = { 
    "testemail", "",
    "test-email-only", 
    "send all email notifications to the admin only (not to any users, for testing)" 
    };  

  public static final String[] EMAIL_DSL_ADMINS_ONLY= { 
    "emaildsladminsonly", "",
    "email-dsl-admins-only", 
    "send email only to the data sharing level admins (and to the admin running the process), not to any users" 
    };

  @SuppressWarnings("static-access")
  public AdminEmailApplication(String[] cmdLineArgs)
  {
    super(cmdLineArgs);
    
    addCommandLineOption(OptionBuilder.hasArg()
                             .withArgName(EMAIL_RECIPIENT_LIST_OPTION[ARG_INDEX])
                             .withDescription(EMAIL_RECIPIENT_LIST_OPTION[DESCRIPTION_INDEX])
                             .withLongOpt(EMAIL_RECIPIENT_LIST_OPTION[LONG_OPTION_INDEX])
                             .create(EMAIL_RECIPIENT_LIST_OPTION[SHORT_OPTION_INDEX]));
    addCommandLineOption(OptionBuilder
                             .withDescription(NO_NOTIFY_OPTION[DESCRIPTION_INDEX])
                             .withLongOpt(NO_NOTIFY_OPTION[LONG_OPTION_INDEX])
                             .create(NO_NOTIFY_OPTION[SHORT_OPTION_INDEX]));    
    addCommandLineOption(OptionBuilder
                         .withDescription(TEST_EMAIL_ONLY[DESCRIPTION_INDEX])
                         .withLongOpt(TEST_EMAIL_ONLY[LONG_OPTION_INDEX])
                         .create(TEST_EMAIL_ONLY[SHORT_OPTION_INDEX]));
    addCommandLineOption(OptionBuilder
                             .withDescription(EMAIL_DSL_ADMINS_ONLY[DESCRIPTION_INDEX])
                             .withLongOpt(EMAIL_DSL_ADMINS_ONLY[LONG_OPTION_INDEX])
                             .create(EMAIL_DSL_ADMINS_ONLY[SHORT_OPTION_INDEX]));
  }

  public boolean isAdminEmailOnly() throws ParseException
  {
    return isCommandLineFlagSet(EMAIL_DSL_ADMINS_ONLY[SHORT_OPTION_INDEX]);
  }
  
  public InternetAddress getEmail(ScreensaverUser user)
    throws MessagingException
  {
    try {
      log.info("admin email for: " + user + ", " + user.getEmail() );
      if(StringUtils.isEmpty(user.getEmail()))
      {
        log.error("Email address for the admin user is null! user: " + user.getFullNameFirstLast() + ", " + user );
        return null;
      }
      return new InternetAddress(user.getEmail());
    }
    catch (AddressException e) {
      if (isCommandLineFlagSet(NO_NOTIFY_OPTION[SHORT_OPTION_INDEX])) {
        log.warn("email address is wrong: " +
                                   printUserInformation(user) + "," + e.getMessage());
        //Note: this is just to clean up the name for testing, this doesn't fix the address
        return new InternetAddress(user.getFullNameFirstLast().replaceAll("[^a-zA-Z0-9]", "_")+ "@dev.null");
      } else {
        throw new MessagingException("email address is wrong: " +
                                   printUserInformation(user), e);
      }
    }
  }
  
  public final EmailService getEmailServiceBasedOnCommandLineOption(AdministratorUser admin)
    throws AddressException
  {
    EmailService emailService = null;
    if (isCommandLineFlagSet(NO_NOTIFY_OPTION[SHORT_OPTION_INDEX])) {
      emailService = new EmailService() {
        public void send(String subject,
                         String message,
                         InternetAddress from,
                         InternetAddress[] recipients,
                         InternetAddress[] cclist) throws MessagingException
       {
         try {
          send(subject, message, from, recipients, cclist, null);
        }
        catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
       }

        public void send(String subject,
                         String message,
                         InternetAddress from,
                         InternetAddress[] recipients,
                         InternetAddress[] cclist, File attachedFile) throws MessagingException, IOException
        {
          log.info("Mock Email (Not Sent):\n" + SmtpEmailService.printEmail(subject, message, from, recipients, cclist, (attachedFile == null ? "" : "" + attachedFile.getCanonicalFile())));
        }
      };
    }
    else if (isCommandLineFlagSet(TEST_EMAIL_ONLY[SHORT_OPTION_INDEX])) {
      InternetAddress adminEmail = null;
      try {
        adminEmail = new InternetAddress(admin.getEmail());
      }
      catch (AddressException e) {
        log.error("Admin account used has an email problem: " + printUserInformation(admin));
        throw e;
      }
      final InternetAddress finalAdminEmail = adminEmail;
      final EmailService wrappedEmailService = (EmailService) getSpringBean("emailService");
      emailService = new EmailService() {
        public void send(String subject,
                           String message,
                           InternetAddress from,
                           InternetAddress[] recipients,
                           InternetAddress[] cclist) throws MessagingException
        {
          try {
            send(subject, message, from, recipients, cclist, null);
          }
          catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        }

        public void send(String subject,
                         String message,
                         InternetAddress from,
                         InternetAddress[] recipients,
                         InternetAddress[] ccrecipients, File attachedFile) throws MessagingException, IOException
        {
          message = "Testing Email Wrapper:  redirect email to admin, original email to be sent to:\n" + Arrays.asList(recipients) + "\n=======message=========\n" +
                    message;
          wrappedEmailService.send("Redirected to: " + finalAdminEmail + ", Subject: "  + subject,
                                   message,
                                   finalAdminEmail,
                                   new InternetAddress[] { finalAdminEmail },
                                   null,
                                   attachedFile);
        }
      };
    }
    else {
      emailService = (EmailService) getSpringBean("emailService");
    }
    return emailService;
  }
  
  
  public static String USER_PRINT_FORMAT = "|%1$-10s|%2$-30s|%3$-60s";
  public static String printUserHeader()
  {
    return String.format(USER_PRINT_FORMAT, 
                         "ID",
                         "Name",
                         "Email");
  }
  
  public static String printUser(ScreensaverUser user)
  {
    return String.format(USER_PRINT_FORMAT,
                         user.getEntityId(),
                         user.getFullNameFirstLast(),
                         user.getEmail());
  }

  public static List<String> printUserInformation(ScreensaverUser user)
  {
    List<String> buf = Lists.newLinkedList();
    buf.add("User ID: " + user.getEntityId());
    buf.add("Login ID: " + user.getLoginId());
    buf.add("Name: " + user.getFullNameFirstLast());
    buf.add("Email: \"" + user.getEmail() + "\"");
    return buf;
  }  

  @Override
  public AdministratorUser findAdministratorUser() throws IllegalArgumentException
  {
    AdministratorUser admin = super.findAdministratorUser();
    if (admin != null) {
      if (admin.getEmail() == null) {
        throw new IllegalArgumentException("The administrative account given does not have an email address");
      }
    }
    return admin;
  }

  /**
   * @return
   * @throws ParseException
   * @throws AddressException
   */
  public final Set<InternetAddress> getExtraRecipients() throws AddressException
  {
    Set<String> stringSet = Sets.newHashSet();
    if (isCommandLineFlagSet(EMAIL_RECIPIENT_LIST_OPTION[SHORT_OPTION_INDEX])) {
      String recipientList = getCommandLineOptionValue(EMAIL_RECIPIENT_LIST_OPTION[SHORT_OPTION_INDEX]);
      stringSet.addAll(Arrays.asList(recipientList.split(EmailService.DELIMITER)));
    }
    Set<InternetAddress> addressList = Sets.newHashSet();
    for(String r:stringSet)
    {
      addressList.add(new InternetAddress(r));
    }
    return addressList;
  }

  public ServiceMessages getMessages()
  {
    if (_messages == null) {
      _messages = (ServiceMessages) getSpringBean("serviceMessages");
    }
    return _messages;
  }

}
