// $HeadURL$
// $Id$
//
// Copyright 2008 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.screens;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.dao.DataIntegrityViolationException;

import edu.harvard.med.screensaver.CommandLineApplication;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.DAOTransactionRollbackException;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.io.UnrecoverableParseException;
import edu.harvard.med.screensaver.model.DuplicateEntityException;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenDataSharingLevel;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.Study;
import edu.harvard.med.screensaver.model.screens.StudyType;
import edu.harvard.med.screensaver.model.users.LabAffiliation;
import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.util.StringUtils;

/**
 * Command-line application that creates a new study in the database from data
 * provided in an Excel workbook.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class StudyCreator
{
  // static members

  private static Logger log = Logger.getLogger(StudyCreator.class);

  protected static final DateTimeFormatter dateFormat = DateTimeFormat.forPattern(CommandLineApplication.DEFAULT_DATE_PATTERN);

  protected CommandLineApplication app;

  protected GenericEntityDAO dao;

  protected int studyNumber;
  protected String title;
  protected ScreenType screenType;
  protected StudyType studyType;
  protected String summary;
  protected DateTime dateCreated;
  protected File screenResultFile;

  protected String labHeadFirstName;
  protected String labHeadLastName;
  protected String labHeadEmail;

  protected String leadScreenerFirstName;
  protected String leadScreenerLastName;
  protected String leadScreenerEmail;

  protected boolean replace;


  public StudyCreator(String[] args) throws ParseException
  {
    app = new CommandLineApplication(args);
    configureCommandLineArguments(app);

    if (!app.processOptions(true, true)) {
      throw new IllegalArgumentException("invalid usage");
    }

    dao = (GenericEntityDAO) app.getSpringBean("genericEntityDao");

    studyNumber = app.getCommandLineOptionValue("n", Integer.class);
    title = app.getCommandLineOptionValue("t");
    screenType = app.getCommandLineOptionEnumValue("y", ScreenType.class);
    studyType = app.getCommandLineOptionEnumValue("yy", StudyType.class);
    summary = app.isCommandLineFlagSet("s") ? app.getCommandLineOptionValue("s") : null;

    labHeadFirstName = app.getCommandLineOptionValue("hf");
    labHeadLastName = app.getCommandLineOptionValue("hl");
    labHeadEmail = app.getCommandLineOptionValue("he");

    leadScreenerFirstName = app.getCommandLineOptionValue("lf");
    leadScreenerLastName = app.getCommandLineOptionValue("ll");
    leadScreenerEmail = app.getCommandLineOptionValue("le");

    screenResultFile = app.isCommandLineFlagSet("f") ?  app.getCommandLineOptionValue("f", File.class) : null;
    
    replace = app.isCommandLineFlagSet("r");
    

    validateStudyNumber();
  }

  protected void validateStudyNumber()
  {
    if (studyNumber < Study.MIN_STUDY_NUMBER) {
      throw new IllegalArgumentException("study number must >= " + Study.MIN_STUDY_NUMBER);
    }
  }

  @SuppressWarnings("static-access")
  protected void configureCommandLineArguments(CommandLineApplication app)
  {
    app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("#").withLongOpt("number").create("n"));
    app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("title").withLongOpt("title").withDescription("the title of the screen").create("t"));
    List<String> desc = new ArrayList<String>();
    for(ScreenType t: EnumSet.allOf(ScreenType.class) ) desc.add(t.name());
    app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("screen type").withLongOpt("screen-type").withDescription(StringUtils.makeListString(desc, ", ")).create("y"));
    desc.clear();
    for(StudyType t: EnumSet.allOf(StudyType.class) ) desc.add(t.name());
    app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("study type").withLongOpt("study-type").withDescription(StringUtils.makeListString(desc, ", ")).create("yy"));
    app.addCommandLineOption(OptionBuilder.hasArg().withArgName("text").withLongOpt("summary").create("s"));
    app.addCommandLineOption(OptionBuilder.hasArg().withArgName(CommandLineApplication.DEFAULT_DATE_PATTERN).withLongOpt("date-created").create("d"));
    app.addCommandLineOption(OptionBuilder.hasArg().withArgName("file").withLongOpt("data-file").create("f"));

    app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("first name").withLongOpt("lab-head-first-name").create("hf"));
    app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("last name").withLongOpt("lab-head-last-name").create("hl"));
    app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("email").withLongOpt("lab-head-email").create("he"));

    app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("first name").withLongOpt("lead-screener-first-name").create("lf"));
    app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("last name").withLongOpt("lead-screener-last-name").create("ll"));
    app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("email").withLongOpt("lead-screener-email").create("le"));

    app.addCommandLineOption(OptionBuilder.hasArg(false).withLongOpt("replace").withDescription("replace an existing with the same study number").create("r"));
}

  public static void main(String[] args)
  {
    try {
      StudyCreator studyCreator = new StudyCreator(args);
      studyCreator.createStudy();
    }
    catch (ParseException e) {
      String msg = "bad command line argument: " + /*app.getLastAccessOption().getOpt() +*/ e.getMessage();
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

  public void createStudy()
  {
    final GenericEntityDAO dao = (GenericEntityDAO) app.getSpringBean("genericEntityDao");
    
    Screen study = dao.findEntityByProperty(Screen.class, "screenNumber", studyNumber);
    if (study != null) {
      if (!replace) {
        log.error("study " + studyNumber + " already exists (use --replace flag to delete existing study first)"); 
        return;
      }
      dao.deleteEntity(study);
      log.error("deleted existing study " + studyNumber); 
    }

    dao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        try {
          Study study = newStudy();
          importData(study);
          if (study.getLabHead() != null) {
            dao.persistEntity(study.getLabHead());
          }
          dao.persistEntity(study.getLeadScreener());
          dao.persistEntity(study);
        }
        catch (Exception e) {
          throw new DAOTransactionRollbackException(e);
        }
      }

    });
    log.info("study " + studyNumber + " succesfully added to database");
  }

  protected Study newStudy()
  {
    LabHead labHead = (LabHead) findOrCreateScreeningRoomUser(dao, labHeadFirstName, labHeadLastName, labHeadEmail, true, null);
    ScreeningRoomUser leadScreener = findOrCreateScreeningRoomUser(dao, leadScreenerFirstName, leadScreenerLastName, leadScreenerEmail, false, null);
    if (leadScreener.getLab().getLabHead() == null) {
      leadScreener.setLab(labHead.getLab());
      log.info("set lab head for lead screener");
    }

    Screen study = new Screen(leadScreener, labHead, studyNumber, screenType, studyType, title);
    study.setDataSharingLevel(ScreenDataSharingLevel.SHARED);
    study.setSummary(summary);
    return study;
  }

  protected void importData(Study study) throws UnrecoverableParseException, FileNotFoundException
  {
    StudyAnnotationParser parser = (StudyAnnotationParser) app.getSpringBean("studyAnnotationParser");
    parser.parse((Screen) study, new FileInputStream(screenResultFile));
  }

  public static ScreeningRoomUser findOrCreateScreeningRoomUser(GenericEntityDAO dao,
                                                                String firstName,
                                                                String lastName,
                                                                String email,
                                                                boolean isLabHead,
                                                                LabAffiliation labAffiliation)
  {
    Map<String,Object> props = new HashMap<String,Object>();
    props.put("firstName", firstName);
    props.put("lastName", lastName);
    props.put("email", email);
    List<ScreeningRoomUser> users = dao.findEntitiesByProperties(ScreeningRoomUser.class, props, true);
    if (users.size() > 1) {
      throw new DuplicateEntityException(users.get(0));
    }
    if (users.size() == 1) {
      log.info("found existing user " + users.get(0) + " for " + firstName + " " + lastName + " (" + email + ")");
      return users.get(0);
    }
    ScreeningRoomUser newUser;
    if (isLabHead) {
      newUser = new LabHead(firstName, lastName, labAffiliation);
      newUser.setEmail(email);
    }
    else {
      newUser = new ScreeningRoomUser(firstName, lastName);
    }
    log.info("created new user " + newUser + " for " + firstName + " " + lastName + " (" + email + ")");
    return newUser;
  }
}
