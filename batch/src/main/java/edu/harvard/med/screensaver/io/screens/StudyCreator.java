// $HeadURL$
// $Id$
//
// Copyright © 2008, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.screens;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.OptionBuilder;
import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.DAOTransactionRollbackException;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.UsersDAO;
import edu.harvard.med.screensaver.io.CommandLineApplication;
import edu.harvard.med.screensaver.io.screens.StudyAnnotationParser.KEY_COLUMN;
import edu.harvard.med.screensaver.io.workbook2.Workbook;
import edu.harvard.med.screensaver.model.DuplicateEntityException;
import edu.harvard.med.screensaver.model.screens.ProjectPhase;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenDataSharingLevel;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.StudyType;
import edu.harvard.med.screensaver.model.users.LabAffiliation;
import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.util.StringUtils;

/**
 * Command-line application that creates a new study in the database.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class StudyCreator
{
  private static Logger log = Logger.getLogger(StudyCreator.class);

  @SuppressWarnings("static-access")
  public static void main(String[] args)
  {
    final CommandLineApplication app = new CommandLineApplication(args);

    app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("first name").withLongOpt("lead-screener-first-name").create("lf"));
    app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("last name").withLongOpt("lead-screener-last-name").create("ll"));
    app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("email").withLongOpt("lead-screener-email").create("le"));

    app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("first name").withLongOpt("lab-head-first-name").create("hf"));
    app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("last name").withLongOpt("lab-head-last-name").create("hl"));
    app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("email").withLongOpt("lab-head-email").create("he"));

    List<String> desc = new ArrayList<String>();
    for (ScreenType t : EnumSet.allOf(ScreenType.class))
      desc.add(t.name());
    app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("screen type").withLongOpt("screen-type").withDescription(StringUtils.makeListString(desc, ", ")).create("y"));
    desc.clear();
    for (StudyType t : EnumSet.allOf(StudyType.class))
      desc.add(t.name());
    app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("study type").withLongOpt("study-type").withDescription(StringUtils.makeListString(desc, ", ")).create("yy"));
    app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("summary").withLongOpt("summary").create("s"));
    app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("title").withLongOpt("title").create("t"));
    app.addCommandLineOption(OptionBuilder.hasArg().withArgName("protocol").withLongOpt("protocol").create("p"));
    app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("#").withLongOpt("facilityId").create("i"));
    app.addCommandLineOption(OptionBuilder.hasArg(false).withLongOpt("replace").withDescription("replace an existing Screen with the same facilityId").create("r"));

    app.addCommandLineOption(OptionBuilder.withLongOpt("key-input-by-wellkey")
                             .withDescription("default value is to key by reagent vendor id").create("keyByWellId"));
    app.addCommandLineOption(OptionBuilder.withLongOpt("key-input-by-facility-id")
                             .withDescription("default value is to key by reagent vendor id").create("keyByFacilityId"));
    app.addCommandLineOption(OptionBuilder.withLongOpt("key-input-by-compound-name")
                             .withDescription("default value is to key by reagent vendor id").create("keyByCompoundName"));
    app.addCommandLineOption(OptionBuilder.withDescription("optional: pivot the input col/rows so that the AT names are in Col1, and the AT well_id/rvi's are in Row1").withLongOpt("annotation-names-in-col1").create("annotationNamesInCol1"));
    app.addCommandLineOption(OptionBuilder.hasArg().withArgName("file").withLongOpt("data-file").create("f"));

    app.addCommandLineOption(OptionBuilder.withArgName("parseLincsSpecificFacilityID")
                             .withLongOpt("parseLincsSpecificFacilityID").create("parseLincsSpecificFacilityID"));

    app.processOptions(/* acceptDatabaseOptions= */true, /* acceptAdminUserOptions= */true);

    execute(app);
  }

  private static void execute(final CommandLineApplication app)
  {
    final GenericEntityDAO dao = (GenericEntityDAO) app.getSpringBean("genericEntityDao");
    final UsersDAO usersDAO = (UsersDAO) app.getSpringBean("usersDao");
    final String facilityId = app.getCommandLineOptionValue("i");

    boolean replace = app.isCommandLineFlagSet("r");
    Screen screen = dao.findEntityByProperty(Screen.class, "facilityId", facilityId);
    if (screen != null) {
      if (!replace) {
        log.error("screen " + facilityId + " already exists (use --replace flag to delete existing screen first)");
        return;
      }
      dao.deleteEntity(screen);
      log.error("deleted existing screen " + facilityId);
    }

    dao.doInTransaction(new DAOTransaction() {
      @Override
      public void runTransaction()
      {
        try {
          String lsfn = app.getCommandLineOptionValue("lf");
          String lsln = app.getCommandLineOptionValue("ll");
          String lse = app.getCommandLineOptionValue("le");
          String lhfn = app.getCommandLineOptionValue("hf");
          String lhln = app.getCommandLineOptionValue("hl");
          String lhe = app.getCommandLineOptionValue("he");
          StudyType studyType = app.getCommandLineOptionEnumValue("yy", StudyType.class);
          ScreenType screenType = app.getCommandLineOptionEnumValue("y", ScreenType.class);

          LabHead labHead = usersDAO.findSRU(LabHead.class, lhfn, lhln, lhe);
          if(labHead == null) {
                throw new RuntimeException("could not find existing user for " + lhfn + " " + lhln + ", " + lhe);
          }
          ScreeningRoomUser leadScreener = usersDAO.findSRU(ScreeningRoomUser.class, lsfn, lsln, lse);
          if(leadScreener == null) {
                throw new RuntimeException("could not find existing user for " + lsfn + " " + lsln + ", " + lse);
          }
          if (leadScreener.getLab().getLabHead() == null) {
            leadScreener.setLab(labHead.getLab());
            log.info("set lab head for lead screener");
          }
          Screen screen = new Screen(app.findAdministratorUser(), facilityId, leadScreener, labHead, screenType, studyType, ProjectPhase.ANNOTATION, app.getCommandLineOptionValue("t"));
          screen.setDataSharingLevel(ScreenDataSharingLevel.SHARED);
          screen.setSummary(app.getCommandLineOptionValue("s"));
          if (app.isCommandLineFlagSet("p")) {
            screen.setPublishableProtocol(app.getCommandLineOptionValue("p"));
          }
          boolean parseLincsSpecificFacilityID = app.isCommandLineFlagSet("parseLincsSpecificFacilityID");

          // import

          boolean keyByWellId = false;
          boolean keyByFacilityId = false;
          boolean keyByCompoundName = false;
          if (app.isCommandLineFlagSet("keyByWellId")) keyByWellId = true;
          if (app.isCommandLineFlagSet("keyByFacilityId")) keyByFacilityId = true;
          if (app.isCommandLineFlagSet("keyByCompoundName")) keyByCompoundName = true;
          if (((keyByFacilityId ? 1 : 0) + (keyByWellId ? 1 : 0) + (keyByCompoundName ? 1 : 0)) != 1) {
            throw new IllegalArgumentException("must specify either \"keyByWellId\" or \"keyByFacilityId\" or \"keyByCompoundName\" ");
          }
          StudyAnnotationParser.KEY_COLUMN keyColumn = StudyAnnotationParser.KEY_COLUMN.RVI; // keyByReagentVendorId is the default
          if (keyByWellId) keyColumn = KEY_COLUMN.WELL_ID;
          if (keyByFacilityId) keyColumn = KEY_COLUMN.FACILITY_ID;
          if (keyByCompoundName) keyColumn = KEY_COLUMN.COMPOUND_NAME;
          boolean annotationNamesInCol1 = app.isCommandLineFlagSet("annotationNamesInCol1");

          File screenResultFile = null;
          if (app.isCommandLineFlagSet("f")) {
            screenResultFile = app.getCommandLineOptionValue("f", File.class);
            if (!screenResultFile.exists()) throw new IllegalArgumentException("File does not exist: " +
              screenResultFile.getAbsolutePath());
            StudyAnnotationParser parser = (StudyAnnotationParser) app.getSpringBean("studyAnnotationParser");
            parser.parse(screen, new Workbook(screenResultFile), keyColumn, annotationNamesInCol1, parseLincsSpecificFacilityID);
            dao.persistEntity(screen);
          }
          else {
            log.info("no file specified for import");
            screenResultFile = null;
          }
        }
        catch (Exception e) {
          throw new DAOTransactionRollbackException(e);
        }
      }
    });
    log.info("study created");
  }

  /**
   * @deprecated use findSRU instead; put the burden of creating the users if they do not exist, on the client code.
   */
  public static ScreeningRoomUser findOrCreateScreeningRoomUser(final GenericEntityDAO dao,
                                                                final String firstName,
                                                                final String lastName,
                                                                final String email,
                                                                final boolean isLabHead,
                                                                final LabAffiliation labAffiliation)
  {
    Map<String,Object> props = new HashMap<String,Object>();
    props.put("firstName", firstName);
    props.put("lastName", lastName);
    props.put("email", email);
    List<ScreeningRoomUser> users = dao.findEntitiesByProperties(ScreeningRoomUser.class, props);
    if (users.size() > 1) {
      throw new DuplicateEntityException(users.get(0));
    }
    if (users.size() == 1) {
      log.info("found existing user " + users.get(0) + " for " + firstName + " " + lastName + " (" + email + ")");
      return users.get(0);
    }

    final ScreeningRoomUser[] _value = new ScreeningRoomUser[1];
    dao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        try {
          ScreeningRoomUser newUser;
          if (isLabHead) {
            newUser = new LabHead(firstName, lastName, labAffiliation);
            newUser.setEmail(email);
          }
          else {
            newUser = new ScreeningRoomUser(firstName, lastName);
          }
          dao.persistEntity(newUser);
          log.info("created new user " + newUser + " for " + firstName + " " + lastName + " (" + email + ")");
          _value[0] = newUser;
        }
        catch (Exception e) {
          throw new DAOTransactionRollbackException(e);
        }
      }
    });
    return _value[0];
  }

}
