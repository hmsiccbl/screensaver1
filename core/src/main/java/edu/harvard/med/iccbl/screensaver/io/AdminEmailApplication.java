// $HeadURL:
// http://seanderickson1@forge.abcd.harvard.edu/svn/screensaver/trunk/core/src/main/java/edu/harvard/med/iccbl/screensaver/io/AdminEmailApplication.java
// $
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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
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
import edu.harvard.med.screensaver.model.users.ChecklistItemEvent;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.service.EmailService;
import edu.harvard.med.screensaver.service.ServiceMessages;
import edu.harvard.med.screensaver.util.Pair;

public class AdminEmailApplication extends CommandLineApplication
{
  private static Logger log = Logger.getLogger(AdminEmailApplication.class);

  public static final int SHORT_OPTION_INDEX = 0;
  public static final int ARG_INDEX = 1;
  public static final int LONG_OPTION_INDEX = 2;
  public static final int DESCRIPTION_INDEX = 3;

  public static final DateTimeFormatter EXPIRE_DATE_FORMATTER = DateTimeFormat.fullDate();
  public static String USER_PRINT_FORMAT = "|%1$-10s|%2$-30s|%3$-60s";

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

  public static final String[] EMAIL_DSL_ADMINS_ONLY = {
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

  public boolean isAdminEmailOnly()
  {
    return isCommandLineFlagSet(EMAIL_DSL_ADMINS_ONLY[SHORT_OPTION_INDEX]);
  }

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
    buf.add("eCommons ID: " + user.getECommonsId());
    buf.add("Name: " + user.getFullNameFirstLast());
    buf.add("Email: \"" + user.getEmail() + "\"");
    return buf;
  }

  @Override
  /**
   * Throws IllegalArgumentException if the AdministratorUser account does not have an email address.
   * 
   * @throws IllegalArgumentException
   */
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
   * Generate an email, with the subject, message, and stacktrace to the admin user running the application.
   * 
   * @param subject
   * @param msg
   * @param e the exception (may be null)
   * @throws MessagingException
   */
  public final void sendErrorMail(String subject, String msg, Exception e) throws MessagingException
  {
    if (e != null) {
      log.error(subject + "; " + msg, e);
      StringWriter out = new StringWriter();
      e.printStackTrace(new PrintWriter(out));
      msg += "\nException:\n" + out.toString();
    }
    else {
      log.error(subject + "; " + msg);
    }
    sendAdminEmails(subject, msg);
  }

  public boolean sendEmail(String subject, String msg, ScreensaverUser user) throws MessagingException
  {
    List<String> failMessages = Lists.newArrayList();
    if (StringUtils.isEmpty(user.getEmail())) {
      failMessages.add("Empty address for the user: " + printUser(user));
    }
    else {
      EmailService emailService = getEmailServiceBasedOnCommandLineOption();
      if (isAdminEmailOnly()) {
        sendAdminEmails("Admin email only: " + subject, "originally for: " + printUser(user) + "\nOriginal Message:\n" + msg);
      }
      else {
        try {
          InternetAddress userAddress = new InternetAddress(user.getEmail());
          emailService.send(subject,
                              msg,
                              getAdminEmail(),
                              new InternetAddress[] { userAddress },
                              null);
        }
        catch (AddressException e) {
          failMessages.add("Address exception for user: " + printUser(user) + ", " + e.getMessage());
        }
      }
    }
    if (!failMessages.isEmpty()) {
      sendFailMessages("User message: " + subject, msg, failMessages);
    }
    return failMessages.isEmpty();
  }

  /**
   * Send email to the administratorUser (running this program), to the &quot;extra recipients&quot; (specified on the
   * command line), and to the admin accounts, passed in.
   * 
   * @param subject
   * @param msg
   * @return 
   * @throws MessagingException
   */
  public boolean sendEmails(String subject, String msg, Collection<? extends ScreensaverUser> users) throws MessagingException
  {
    List<String> failMessages = Lists.newArrayList();
    Set<InternetAddress> recipients = Sets.newHashSet();

    if (users != null) {
      for (ScreensaverUser user : users) {
        String address = user.getEmail();
        if (StringUtils.isEmpty(address))
          failMessages.add("Empty address for the user: " + printUser(user));
        else {
          try {
            recipients.add(new InternetAddress(address));
          }
          catch (AddressException e) {
            failMessages.add("Address excption for user: " + printUser(user) + ", " + e.getMessage());
          }
        }
      }
    }

    if (!recipients.isEmpty()) {
      EmailService emailService = getEmailServiceBasedOnCommandLineOption();

      emailService.send(subject,
                        msg.toString(),
                        getAdminEmail(),
                        recipients.toArray(new InternetAddress[] {}),
                        (InternetAddress[]) null);
    }
    if (!failMessages.isEmpty()) {
      sendFailMessages(subject, msg, failMessages);
    }
    return failMessages.isEmpty();
  }

  public void sendAdminEmails(String subject, String msg) throws MessagingException
  {
    sendAdminEmails(subject, msg, null, null);
  }
  public void sendAdminEmails(String subject, String msg, File attachedFile) throws MessagingException
  {
    sendAdminEmails(subject, msg, null, attachedFile);
  }

  /**
   * Send email to the administratorUser (running this program), to the &quot;extra recipients&quot; (specified on the
   * command line), and to the admin accounts, passed in.
   * 
   * @param subject
   * @param msg
   * @throws MessagingException
   */
  public void sendAdminEmails(String subject, String msg, Collection<ScreensaverUser> adminUsers) throws MessagingException
  {
    sendAdminEmails(subject, msg, adminUsers, null);
  }
  public void sendAdminEmails(String subject, String msg, Collection<ScreensaverUser> adminUsers, File attachedFile) throws MessagingException
  {
    List<String> failMessages = Lists.newArrayList();
    Set<InternetAddress> adminRecipients = Sets.newHashSet();
    adminRecipients.add(getAdminEmail());

    for (String r : getExtraRecipients()) {
      try {
        adminRecipients.add(new InternetAddress(r));
      }
      catch (AddressException e) {
        failMessages.add("Address excption for: " + r + ", " + e.getMessage());
      }
    }

    if (adminUsers != null) {
      for (ScreensaverUser user : adminUsers) {
        String address = user.getEmail();
        if (StringUtils.isEmpty(address))
          failMessages.add("Empty address for the user: " + printUser(user));
        else {
          try {
            adminRecipients.add(new InternetAddress(address));
          }
          catch (AddressException e) {
            failMessages.add("Address excption for user: " + printUser(user) + ", " + e.getMessage());
          }
        }
      }
    }

    EmailService emailService = getEmailServiceBasedOnCommandLineOption();

    try {
      emailService.send(subject,
                          msg.toString(),
                          getAdminEmail(),
                          adminRecipients.toArray(new InternetAddress[] {}),
                          (InternetAddress[]) null, attachedFile);
    }
    catch (IOException e) {
      String errmsg = "Exception when trying to attach the file: " + attachedFile;
      log.warn(errmsg, e);
      failMessages.add(errmsg  + ", " + e.getMessage());
    }

    if (!failMessages.isEmpty()) {
      sendFailMessages(subject, msg, failMessages);
    }

  }

  private void sendFailMessages(String subject, String msg, List<String> failMessages) throws MessagingException
  {
    StringBuilder errmsg = new StringBuilder("Failures: \n");
    for (String m : failMessages) {
      errmsg.append("\n").append(m);
    }
    errmsg.append("\nOriginal Message:\n").append(msg);
    sendAdminEmails("Failed Message delivery for: " + subject, errmsg.toString());
  }

  public final Set<String> getExtraRecipients()
  {
    Set<String> stringSet = Sets.newHashSet();
    if (isCommandLineFlagSet(EMAIL_RECIPIENT_LIST_OPTION[SHORT_OPTION_INDEX])) {
      String recipientList = getCommandLineOptionValue(EMAIL_RECIPIENT_LIST_OPTION[SHORT_OPTION_INDEX]);
      stringSet.addAll(Arrays.asList(recipientList.split(EmailService.DELIMITER)));
    }
    return stringSet;
  }

  public ServiceMessages getMessages()
  {
    if (_messages == null) {
      _messages = (ServiceMessages) getSpringBean("serviceMessages");
    }
    return _messages;
  }

  public final EmailService getEmailServiceBasedOnCommandLineOption()
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
         catch (IOException e) { // this shall never happen
           e.printStackTrace();
         }
       }

        public void send(String subject,
                         String message,
                         InternetAddress from,
                         InternetAddress[] recipients,
                         InternetAddress[] cclist, File attachedFile) throws MessagingException, IOException
        {
          log.info("Mock Email (Not Sent):\n" +
            SmtpEmailService.printEmail(subject, message, from, recipients, cclist, (attachedFile == null ? "" : "" +
              attachedFile.getCanonicalFile())));
        }
      };
    }
    else if (isCommandLineFlagSet(TEST_EMAIL_ONLY[SHORT_OPTION_INDEX])) {
      InternetAddress adminEmail = null;
      try {
        adminEmail = new InternetAddress(findAdministratorUser().getEmail());
      }
      catch (AddressException e) {
        String msg = "Admin account used has an email problem: " + printUserInformation(findAdministratorUser());
        throw new IllegalArgumentException(msg, e);
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
          message = "Testing Email Wrapper:  redirect email to admin, original email to be sent to:\n" +
            Arrays.asList(recipients) + "\n=======message=========\n" +
                    message;
          wrappedEmailService.send("Redirected to: " + finalAdminEmail + ", Subject: " + subject,
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

  private InternetAddress getAdminEmail()
  {
    try {
      return new InternetAddress(findAdministratorUser().getEmail());
    }
    catch (AddressException e) {
      throw new IllegalArgumentException("Problem with the admin email: " + printUserInformation(findAdministratorUser()), e);
    }
  }
}
