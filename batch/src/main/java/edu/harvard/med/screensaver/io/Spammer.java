// $HeadURL: http://forge.abcd.harvard.edu/svn/screensaver/trunk/core/src/main/java/edu/harvard/med/iccbl/screensaver/service/SmtpEmailService.java $
// $Id: SmtpEmailService.java 6535 2011-10-04 17:29:21Z seanderickson1 $
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io;

import java.io.File;

import org.apache.commons.cli.OptionBuilder;
import org.apache.log4j.Logger;

import static edu.harvard.med.screensaver.service.EmailService.DELIMITER;
import edu.harvard.med.screensaver.service.SmtpEmailService;

public class Spammer
{
  private static Logger log = Logger.getLogger(Spammer.class);

  public static final int SHORT_OPTION_INDEX = 0;
  public static final int LONG_OPTION_INDEX = 1;
  public static final int DESCRIPTION_INDEX = 2;
  public static final String[] MAIL_FROM_OPTION = { "mf", "mail-from", "from address for the mail, defaults to the username" };
  public static final String[] MAIL_CC_LIST_OPTION =
  { "cclist", "mail-cc-list", "the cc recipient(s) of the message, delimited by \"" + DELIMITER + "\"" };
  public static final String[] MAIL_REPLYTO_LIST_OPTION =
  { "replyTos", "mail-replyto-list", "the replyto address(es) of the message, delimited by \"" + DELIMITER + "\"" };
  public static final String[] MAIL_RECIPIENT_LIST_OPTION =
  { "recipientlist", "mail-recipient-list", "the recipient(s) of the message, delimited by \"" + DELIMITER + "\"" };
  public static final String[] MAIL_MESSAGE_OPTION = { "mm", "mail-message", "the mail message" };
  public static final String[] MAIL_FILE_ATTACHMENT = { "f", "mail-file-attachment", "file to attach" };
  public static final String[] MAIL_SUBJECT_OPTION = { "ms", "mail-subject", "the mail subject" };
  public static final String[] MAIL_SERVER_OPTION = { "mh", "mail-host", "the smtp mail server host" };
  public static final String[] MAIL_USERNAME_OPTION = { "mu", "mail-user", "the smtp mail user" };
  public static final String[] MAIL_USER_PASSWORD_OPTION = { "mp", "mail-password", "the smtp mail user password" };
  public static final String[] MAIL_USE_SMTPS = { "smtps", "use-smtps", "use SMTPS Auth (gmail)" };

  @SuppressWarnings("static-access")
  public static void main(String[] args) throws Exception
  {

    CommandLineApplication app = new CommandLineApplication(args);
    String[] option = MAIL_RECIPIENT_LIST_OPTION;
    app.addCommandLineOption(OptionBuilder
                                           .withType(Integer.class)
                                           .hasArg()
                                          .isRequired()
                                          .withArgName(option[SHORT_OPTION_INDEX])
                                          .withDescription(option[DESCRIPTION_INDEX])
                                          .withLongOpt(option[LONG_OPTION_INDEX])
                                          .create(option[SHORT_OPTION_INDEX]));

    option = MAIL_CC_LIST_OPTION;
    app.addCommandLineOption(OptionBuilder
                                           .hasArg()
                                          .withArgName(option[SHORT_OPTION_INDEX])
                                          .withDescription(option[DESCRIPTION_INDEX])
                                          .withLongOpt(option[LONG_OPTION_INDEX])
                                          .create(option[SHORT_OPTION_INDEX]));

    option = MAIL_REPLYTO_LIST_OPTION;
    app.addCommandLineOption(OptionBuilder
                                           .hasArg()
                                          .withArgName(option[SHORT_OPTION_INDEX])
                                          .withDescription(option[DESCRIPTION_INDEX])
                                          .withLongOpt(option[LONG_OPTION_INDEX])
                                          .create(option[SHORT_OPTION_INDEX]));

    option = MAIL_MESSAGE_OPTION;
    app.addCommandLineOption(OptionBuilder
                                           .hasArg()
                                          .isRequired()
                                          .withArgName(option[SHORT_OPTION_INDEX])
                                          .withDescription(option[DESCRIPTION_INDEX])
                                          .withLongOpt(option[LONG_OPTION_INDEX])
                                          .create(option[SHORT_OPTION_INDEX]));

    option = MAIL_FILE_ATTACHMENT;
    app.addCommandLineOption(OptionBuilder
                                           .hasArg()
                                          .withArgName(option[SHORT_OPTION_INDEX])
                                          .withDescription(option[DESCRIPTION_INDEX])
                                          .withLongOpt(option[LONG_OPTION_INDEX])
                                          .create(option[SHORT_OPTION_INDEX]));

    option = MAIL_SUBJECT_OPTION;
    app.addCommandLineOption(OptionBuilder
                                           .hasArg()
                                          .isRequired()
                                          .withArgName(option[SHORT_OPTION_INDEX])
                                          .withDescription(option[DESCRIPTION_INDEX])
                                          .withLongOpt(option[LONG_OPTION_INDEX])
                                          .create(option[SHORT_OPTION_INDEX]));

    option = MAIL_SERVER_OPTION;
    app.addCommandLineOption(OptionBuilder
                                           .hasArg()
                                          .isRequired()
                                          .withArgName(option[SHORT_OPTION_INDEX])
                                          .withDescription(option[DESCRIPTION_INDEX])
                                          .withLongOpt(option[LONG_OPTION_INDEX])
                                          .create(option[SHORT_OPTION_INDEX]));

    option = MAIL_USERNAME_OPTION;
    app.addCommandLineOption(OptionBuilder
                                           .hasArg()
                                          .isRequired()
                                          .withArgName(option[SHORT_OPTION_INDEX])
                                          .withDescription(option[DESCRIPTION_INDEX])
                                          .withLongOpt(option[LONG_OPTION_INDEX])
                                          .create(option[SHORT_OPTION_INDEX]));

    option = MAIL_USER_PASSWORD_OPTION;
    app.addCommandLineOption(OptionBuilder
                                           .hasArg()
                                          .isRequired()
                                          .withArgName(option[SHORT_OPTION_INDEX])
                                          .withDescription(option[DESCRIPTION_INDEX])
                                          .withLongOpt(option[LONG_OPTION_INDEX])
                                          .create(option[SHORT_OPTION_INDEX]));

    option = MAIL_FROM_OPTION;
    app.addCommandLineOption(OptionBuilder
                                           .hasArg()
                                          .withArgName(option[SHORT_OPTION_INDEX])
                                          .withDescription(option[DESCRIPTION_INDEX])
                                          .withLongOpt(option[LONG_OPTION_INDEX])
                                          .create(option[SHORT_OPTION_INDEX]));

    option = MAIL_USE_SMTPS;
    app.addCommandLineOption(OptionBuilder
                                          .withArgName(option[SHORT_OPTION_INDEX])
                                          .withDescription(option[DESCRIPTION_INDEX])
                                          .withLongOpt(option[LONG_OPTION_INDEX])
                                          .create(option[SHORT_OPTION_INDEX]));


    app.processOptions(true, true);

    String message = app.getCommandLineOptionValue(MAIL_MESSAGE_OPTION[SHORT_OPTION_INDEX]);

    File attachedFile = null;
    if (app.isCommandLineFlagSet(MAIL_FILE_ATTACHMENT[SHORT_OPTION_INDEX])) {
      attachedFile = new File(app.getCommandLineOptionValue(MAIL_FILE_ATTACHMENT[SHORT_OPTION_INDEX]));
      if (!attachedFile.exists()) {
        log.error("Specified file does not exist: " + attachedFile.getCanonicalPath());
        System.exit(1);
      }
    }

    String subject = app.getCommandLineOptionValue(MAIL_SUBJECT_OPTION[SHORT_OPTION_INDEX]);
    String recipientlist = app.getCommandLineOptionValue(MAIL_RECIPIENT_LIST_OPTION[SHORT_OPTION_INDEX]);
    String[] recipients = recipientlist.split(DELIMITER);

    String[] ccrecipients = null;
    if (app.isCommandLineFlagSet(MAIL_CC_LIST_OPTION[SHORT_OPTION_INDEX])) {
      String cclist = app.getCommandLineOptionValue(MAIL_CC_LIST_OPTION[SHORT_OPTION_INDEX]);
      ccrecipients = cclist.split(DELIMITER);
    }

    String replytos = null;
    if (app.isCommandLineFlagSet(MAIL_REPLYTO_LIST_OPTION[SHORT_OPTION_INDEX])) {
      replytos = app.getCommandLineOptionValue(MAIL_CC_LIST_OPTION[SHORT_OPTION_INDEX]);
    }

    String mailHost = app.getCommandLineOptionValue(MAIL_SERVER_OPTION[SHORT_OPTION_INDEX]);
    String username = app.getCommandLineOptionValue(MAIL_USERNAME_OPTION[SHORT_OPTION_INDEX]);
    String password = app.getCommandLineOptionValue(MAIL_USER_PASSWORD_OPTION[SHORT_OPTION_INDEX]);
    boolean useSmtps = app.isCommandLineFlagSet(MAIL_USE_SMTPS[SHORT_OPTION_INDEX]);

    String mailFrom = username;
    if (app.isCommandLineFlagSet(MAIL_FROM_OPTION[SHORT_OPTION_INDEX])) {
      mailFrom = app.getCommandLineOptionValue(MAIL_FROM_OPTION[SHORT_OPTION_INDEX]);
    }

    SmtpEmailService service = new SmtpEmailService(mailHost, username, replytos, password, useSmtps);
    service.send(subject, message, mailFrom, recipients, ccrecipients, attachedFile);
  }


}