// $HeadURL: $
// $Id: $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.screens;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.harvard.med.screensaver.CommandLineApplication;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.DAOTransactionRollbackException;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultParser;
import edu.harvard.med.screensaver.io.workbook2.WorkbookParseError;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.StudyType;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.util.StringUtils;

import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * Command-line application that creates a new screen in the database.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class ScreenCreator
{
  // static members

  private static Logger log = Logger.getLogger(ScreenCreator.class);

  @SuppressWarnings("static-access")
  public static void main(String[] args)
  {
    final CommandLineApplication app = new CommandLineApplication(args);
    try {
      DateTimeFormatter dateFormat = DateTimeFormat.forPattern(CommandLineApplication.DEFAULT_DATE_PATTERN);
      app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("#").withLongOpt("number").create("n"));
      app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("title").withLongOpt("title").withDescription("the title of the screen").create("t"));
      app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("screen type").withLongOpt("screen-type").withDescription(StringUtils.makeListString(Arrays.asList(ScreenType.values()), ", ")).create("y"));
      app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("study type").withLongOpt("study-type").withDescription(StringUtils.makeListString(Arrays.asList(ScreenType.values()), ", ")).create("yy"));
      app.addCommandLineOption(OptionBuilder.hasArg().withArgName("text").withLongOpt("summary").create("s"));
      app.addCommandLineOption(OptionBuilder.hasArg().withArgName("text").withLongOpt("protocol").create("p"));
      app.addCommandLineOption(OptionBuilder.hasArg().withArgName(CommandLineApplication.DEFAULT_DATE_PATTERN).withLongOpt("date-created").create("d"));
      app.addCommandLineOption(OptionBuilder.hasArg().withArgName("file").withLongOpt("result-file").create("f"));

      app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("first name").withLongOpt("lab-head-first-name").create("hf"));
      app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("last name").withLongOpt("lab-head-last-name").create("hl"));
      app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("email").withLongOpt("lab-head-email").create("he"));

      app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("first name").withLongOpt("lead-screener-first-name").create("lf"));
      app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("last name").withLongOpt("lead-screener-last-name").create("ll"));
      app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("email").withLongOpt("lead-screener-email").create("le"));

      if (!app.processOptions(true, true)) {
        System.exit(1);
      }

      final GenericEntityDAO dao = (GenericEntityDAO) app.getSpringBean("genericEntityDao");

      final int screenNumber = app.getCommandLineOptionValue("n", Integer.class);
      final String title = app.getCommandLineOptionValue("t");
      final ScreenType screenType = app.getCommandLineOptionEnumValue("y", ScreenType.class);
      final StudyType studyType = app.getCommandLineOptionEnumValue("yy", StudyType.class);
      final String summary = app.isCommandLineFlagSet("s") ? app.getCommandLineOptionValue("s") : null;
      final String protocol = app.isCommandLineFlagSet("p") ? app.getCommandLineOptionValue("p") : null;
      final DateTime dateCreated = app.isCommandLineFlagSet("d") ? app.getCommandLineOptionValue("d", dateFormat) : new DateTime();
      final File screenResultFile = app.isCommandLineFlagSet("f") ?  app.getCommandLineOptionValue("f", File.class) : null;

      final String labHeadFirstName = app.getCommandLineOptionValue("hf");
      final String labHeadLastName = app.getCommandLineOptionValue("hl");
      final String labHeadEmail= app.getCommandLineOptionValue("he");

      final String leadScreenerFirstName = app.getCommandLineOptionValue("lf");
      final String leadScreenerLastName = app.getCommandLineOptionValue("ll");
      final String leadScreenerEmail = app.getCommandLineOptionValue("le");

      dao.doInTransaction(new DAOTransaction() {
        public void runTransaction() {
          try {
            ScreeningRoomUser labHead = findOrCreateScreeningRoomUser(dao, labHeadFirstName, labHeadLastName, labHeadEmail);
            ScreeningRoomUser leadScreener = findOrCreateScreeningRoomUser(dao, leadScreenerFirstName, leadScreenerLastName, leadScreenerEmail);
            if (leadScreener.getLabHead() == null) {
              leadScreener.setLabHead(labHead);
              log.info("set lab head for lead screener");
            }

            Screen screen = new Screen(leadScreener, labHead, screenNumber, screenType, studyType, title);
            screen.setDateCreated(dateCreated);
            screen.setSummary(summary);
            screen.setPublishableProtocol(protocol);

            if (app.isCommandLineFlagSet("f")) {
              ScreenResultParser parser = (ScreenResultParser) app.getSpringBean("screenResultParser");
              parser.parse(screen, screenResultFile);
              if (parser.getHasErrors()) {
                log.error("errors found in screen result file");
                for (WorkbookParseError error : parser.getErrors()) {
                  log.error(error.toString());
                }
                System.exit(1);
                log.info("screen result successfully imported");
              }
            }
            dao.persistEntity(screen.getLabHead());
            dao.persistEntity(screen.getLeadScreener());
            dao.persistEntity(screen);
          }
          catch (Exception e) {
            throw new DAOTransactionRollbackException(e);
          }
        }
      });
      log.info("screen succesfully added to database");
    }
    catch (ParseException e) {
      String msg = "bad command line argument " + app.getLastAccessOption().getOpt() + e.getMessage();
      System.out.println(msg);
      log.error(msg);
      System.exit(1);
    }
    catch (DataIntegrityViolationException e) {
      String msg = "data integrity error: " + e.getMessage();
      System.out.println(msg);
      log.error(msg);
      System.exit(1);
    }
    catch (Exception e) {
      e.printStackTrace();
      log.error(e.toString());
      System.err.println("error: " + e.toString());
      System.exit(1);
    }
  }

  public static ScreeningRoomUser findOrCreateScreeningRoomUser(GenericEntityDAO dao,
                                                                 String firstName,
                                                                 String lastName,
                                                                 String email)
    throws Exception
  {
    Map<String,Object> props = new HashMap<String,Object>();
    props.put("firstName", firstName);
    props.put("lastName", lastName);
    props.put("email", email);
    List<ScreeningRoomUser> users = dao.findEntitiesByProperties(ScreeningRoomUser.class, props, true);
    if (users.size() > 1) {
      throw new Exception("multiple screening room users for " + firstName + " " + lastName + " (" + email + ")");
    }
    if (users.size() == 1) {
      log.info("found existing user " + users.get(0) + " for " + firstName + " " + lastName + " (" + email + ")");
      return users.get(0);
    }
    ScreeningRoomUser newUser = new ScreeningRoomUser(firstName, lastName, email);
    log.info("created new user " + newUser + " for " + firstName + " " + lastName + " (" + email + ")");
    return newUser;
  }
}
