// $HeadURL: http://seanderickson1@forge.abcd.harvard.edu/svn/screensaver/branches/iccbl/data-sharing-levels/src/edu/harvard/med/screensaver/io/screenresults/ScreenResultImporter.java $
// $Id: ScreenResultImporter.java 3655 2009-11-20 00:57:00Z seanderickson1 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.iccbl.screensaver.io.users;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.mail.MessagingException;

import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.joda.time.LocalDate;

import edu.harvard.med.iccbl.screensaver.service.users.UserAgreementUpdater;
import edu.harvard.med.screensaver.CommandLineApplication;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.AdministrativeActivity;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.service.EmailService;
import edu.harvard.med.screensaver.util.Pair;

public class SmallMoleculeUserExpirationUpdater extends CommandLineApplication
{
  public SmallMoleculeUserExpirationUpdater(String[] cmdLineArgs)
  {
    super(cmdLineArgs);
  }

  private static Logger log = Logger.getLogger(SmallMoleculeUserExpirationUpdater.class);

  public static final int SHORT_OPTION_INDEX = 0;
  public static final int LONG_OPTION_INDEX = 1;
  public static final int DESCRIPTION_INDEX = 2;

  static final String[] ADMIN_USER_ECOMMONS_ID_OPTION = {
    "u",
    "ecommons-id",
    "the eCommons ID of the administrative user performing the load"
  };

  public static final String[] EMAIL_RECIPIENT_LIST_OPTION = {
    "mr", 
    "mail-recipient-list", 
    "default to the admin user email, the recipient(s) of the message, delimited by \"" + EmailService.DELIMITER + "\"" 
  };

  @SuppressWarnings("static-access")
  public static void main(String[] args)
  {
    SmallMoleculeUserExpirationUpdater app = new SmallMoleculeUserExpirationUpdater(args);
    //TODO: allow this to be optional and glean the eCommons ID from the environment - sde4
    app.addCommandLineOption(OptionBuilder
                                 .hasArg()
                                 .withArgName(ADMIN_USER_ECOMMONS_ID_OPTION[SHORT_OPTION_INDEX])
                                 .isRequired()
                                 .withDescription(ADMIN_USER_ECOMMONS_ID_OPTION[DESCRIPTION_INDEX])
                                 .withLongOpt(ADMIN_USER_ECOMMONS_ID_OPTION[LONG_OPTION_INDEX])
                                 .create(ADMIN_USER_ECOMMONS_ID_OPTION[SHORT_OPTION_INDEX]));

    app.addCommandLineOption(OptionBuilder
                             .hasArg()
                             .withArgName(EMAIL_RECIPIENT_LIST_OPTION[SHORT_OPTION_INDEX])
                             .withDescription(EMAIL_RECIPIENT_LIST_OPTION[DESCRIPTION_INDEX])
                             .withLongOpt(EMAIL_RECIPIENT_LIST_OPTION[LONG_OPTION_INDEX])
                             .create(EMAIL_RECIPIENT_LIST_OPTION[SHORT_OPTION_INDEX]));

    try {
      if (!app.processOptions(/* acceptDatabaseOptions= */true,
                              /* showHelpOnError= */true)) {
        return;
      }
      
      String ecommonsId = 
        app.getCommandLineOptionValue(ADMIN_USER_ECOMMONS_ID_OPTION[SHORT_OPTION_INDEX]);

      GenericEntityDAO dao = (GenericEntityDAO) app.getSpringBean("genericEntityDao");

      AdministratorUser admin = dao.findEntityByProperty(AdministratorUser.class, "ECommonsId", ecommonsId);
      if (admin == null) {
        throw new IllegalArgumentException("no administrator user with eCommons ID: " + ecommonsId);
      }
      
      UserAgreementUpdater userAgreementUpdater =
        (UserAgreementUpdater) app.getSpringBean("userAgreementUpdater");

      LocalDate expireDate = new LocalDate(new Date()).minusYears(2);
      List<Pair<ScreeningRoomUser,List<AdministrativeActivity>>> results = 
        userAgreementUpdater.findAndUpdateUsersWithExpiredSMUserAgreements(expireDate, admin);
      
      if(results.isEmpty())
      {
        log.info("No Users have expired for the date: " + expireDate + " (2 years ago).");
      }else{

        String subject = "The Small Molecule User Agreement Update Service has updated " 
            + (results.size()>0? (results.size() + " Users") : "1 User");
        StringBuilder msg = new StringBuilder("User Update Actions:\n");
        for(Pair<ScreeningRoomUser,List<AdministrativeActivity>> result:results)
        {
          msg.append("User: \t" + result.getFirst().getFullNameFirstLast()
                   + "\n\t\teCommons:" + result.getFirst().getECommonsId() 
                   + "\n\t\temail:" + result.getFirst().getEmail());
          for(AdministrativeActivity activity:result.getSecond())
          {
            msg.append("\n\t\tAction:" + activity.getComments());
          }
          msg.append("\n");
        }
        log.info(subject);
        log.info(msg.toString());
        
        if(app.isCommandLineFlagSet(EMAIL_RECIPIENT_LIST_OPTION[SHORT_OPTION_INDEX]))
        {
          String[] recipients = new String[] { admin.getEmail() };
          if(app.isCommandLineFlagSet(EMAIL_RECIPIENT_LIST_OPTION[SHORT_OPTION_INDEX]))
          {
            String recipientList = app.getCommandLineOptionValue(EMAIL_RECIPIENT_LIST_OPTION[SHORT_OPTION_INDEX]);
            recipients = recipientList.split(EmailService.DELIMITER);
          }
          EmailService emailService = (EmailService) app.getSpringBean("emailService");
          log.info("sending email to " + Arrays.asList(recipients));
          emailService.send(subject, msg.toString(),admin.getEmail(),recipients);
        }
      }
      System.exit(0);
    }
    catch (ParseException e) {
      log.error("error parsing command line options: " + e.getMessage());
    }
    catch (MessagingException e) {
      log.error("error sending email", e);
    }
    System.exit(1); //error
  }
}