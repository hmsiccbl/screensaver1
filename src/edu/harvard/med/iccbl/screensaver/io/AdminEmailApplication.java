// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.iccbl.screensaver.io;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.harvard.med.iccbl.screensaver.service.SmtpEmailService;
import edu.harvard.med.screensaver.CommandLineApplication;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.service.EmailService;
import edu.harvard.med.screensaver.service.ServiceMessages;
import edu.harvard.med.screensaver.util.StringUtils;

public class AdminEmailApplication extends CommandLineApplication
{
  private static Logger log = Logger.getLogger(AdminEmailApplication.class);

  public static final int SHORT_OPTION_INDEX = 0;
  public static final int ARG_INDEX = 1;
  public static final int LONG_OPTION_INDEX = 2;
  public static final int DESCRIPTION_INDEX = 3;

  public static final DateTimeFormatter EXPIRE_DATE_FORMATTER = DateTimeFormat.fullDate();
  private ServiceMessages _messages = null;
  
  static final String[] ADMIN_USER_ECOMMONS_ID_OPTION = { "u", "u",
    "ecommons-id", "the eCommons ID (or login id) of the administrative user performing the expiration" };

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
                             .withArgName(ADMIN_USER_ECOMMONS_ID_OPTION[ARG_INDEX])
                             .isRequired()
                             .withDescription(ADMIN_USER_ECOMMONS_ID_OPTION[DESCRIPTION_INDEX])
                             .withLongOpt(ADMIN_USER_ECOMMONS_ID_OPTION[LONG_OPTION_INDEX])
                             .create(ADMIN_USER_ECOMMONS_ID_OPTION[SHORT_OPTION_INDEX]));
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

    _messages = (ServiceMessages)getSpringBean("serviceMessages");
  }

  public boolean isAdminEmailOnly() throws ParseException
  {
    return isCommandLineFlagSet(EMAIL_DSL_ADMINS_ONLY[SHORT_OPTION_INDEX]);
  }
  
  public final EmailService getEmailServiceBasedOnCommandLineOption(AdministratorUser admin)
    throws ParseException, AddressException
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
          log.info("Mock Email (Not Sent):\n" + SmtpEmailService.printEmail(subject, message, from, recipients, cclist));
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
                         InternetAddress[] ccrecipients) throws MessagingException
        {
          message = "Testing Email Wrapper:  redirect email to admin, original email to be sent to:\n" + Arrays.asList(recipients) + "\n=======message=========\n" +
                    message;
          wrappedEmailService.send("Redirected to: " + finalAdminEmail + ", Subject: "  + subject,
                                   message,
                                   finalAdminEmail,
                                   new InternetAddress[] { finalAdminEmail },
                                   null);
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
    String id = user.getECommonsId();
    if (StringUtils.isEmpty(id)) id = user.getLoginId();
    return String.format(USER_PRINT_FORMAT,
                         id,
                         user.getFullNameFirstLast(),
                         user.getEmail());
  }

  public static List<String> printUserInformation(ScreensaverUser user)
  {
    List<String> buf = Lists.newLinkedList();
    buf.add("Entity id: "+ user.getEntityId());
    buf.add("eCommonsId: " + user.getECommonsId());
    buf.add("loginId: " + user.getLoginId());
    buf.add("Name: " + user.getFullNameFirstLast());
    buf.add("Email: \"" + user.getEmail() + "\"");
    return buf;
  }  
  
  public final AdministratorUser getAdminUser(final GenericEntityDAO dao)
    throws ParseException,
    IllegalArgumentException
  {
    String adminEcommonsId = getCommandLineOptionValue(ADMIN_USER_ECOMMONS_ID_OPTION[SHORT_OPTION_INDEX]);

    AdministratorUser admin = dao.findEntityByProperty(AdministratorUser.class,
                                                       "ECommonsId",
                                                       adminEcommonsId);
    if (admin == null) {
      admin = dao.findEntityByProperty(AdministratorUser.class,
                                       "loginId",
                                       adminEcommonsId);
      if (admin == null) {
        throw new IllegalArgumentException("no administrator user with eCommons ID: " +
                                           adminEcommonsId);
      }
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
  public final Set<InternetAddress> getExtraRecipients() throws ParseException, AddressException
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
    return _messages;
  }

}
