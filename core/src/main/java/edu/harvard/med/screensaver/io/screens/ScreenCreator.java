// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.screens;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.DAOTransactionRollbackException;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.io.CommandLineApplication;
import edu.harvard.med.screensaver.model.DuplicateEntityException;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenDataSharingLevel;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.service.screens.ScreenGenerator;
import edu.harvard.med.screensaver.util.StringUtils;

/**
 * Command-line application that creates a new screen in the database.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class ScreenCreator
{
  private static Logger log = Logger.getLogger(ScreenCreator.class);


  @SuppressWarnings("static-access")
  public static void main(String[] args) throws ParseException
  {
    final CommandLineApplication app = new CommandLineApplication(args);

    app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("first name").withLongOpt("lead-screener-first-name").create("lf"));
    app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("last name").withLongOpt("lead-screener-last-name").create("ll"));
    app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("email").withLongOpt("lead-screener-email").create("le"));

    app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("first name").withLongOpt("lab-head-first-name").create("hf"));
    app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("last name").withLongOpt("lab-head-last-name").create("hl"));
    app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("email").withLongOpt("lab-head-email").create("he"));

    List<String> desc = new ArrayList<String>();
    for(ScreenType t: EnumSet.allOf(ScreenType.class) ) desc.add(t.name());
    app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("screen type").withLongOpt("screen-type").withDescription(StringUtils.makeListString(desc, ", ")).create("st"));
    app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("summary").withLongOpt("summary").create("s"));
    app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("title").withLongOpt("title").create("t"));
    app.addCommandLineOption(OptionBuilder.hasArg().withArgName("protocol").withLongOpt("protocol").create("p"));
    app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("#").withLongOpt("facilityId").create("i"));
    app.addCommandLineOption(OptionBuilder.hasArg(false).withLongOpt("replace").withDescription("replace an existing Screen with the same facilityId").create("r"));

    app.processOptions(/* acceptDatabaseOptions= */false, /* acceptAdminUserOptions= */true);

    final GenericEntityDAO dao = (GenericEntityDAO) app.getSpringBean("genericEntityDao");
    final ScreenGenerator screenGenerator = (ScreenGenerator) app.getSpringBean("screenGenerator");
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

          ScreenType screenType = app.getCommandLineOptionEnumValue("st", ScreenType.class);
          LabHead labHead = findSRU(LabHead.class, dao, lhfn, lhln, lhe);
          ScreeningRoomUser screener = findSRU(ScreeningRoomUser.class, dao, lsfn, lsln, lse);
          
          Screen screen = screenGenerator.createPrimaryScreen(app.findAdministratorUser(),
                                                              screener,
                                                              screenType);
          screen.setFacilityId(facilityId);
          screen.setTitle(app.getCommandLineOptionValue("t"));
          screen.setLabHead(labHead);
          screen.setDataSharingLevel(ScreenDataSharingLevel.SHARED); // TODO: create command line param
          screen.setSummary(app.getCommandLineOptionValue("s"));
          if (app.isCommandLineFlagSet("p")) {
            screen.setPublishableProtocol(app.getCommandLineOptionValue("p"));
          }
          dao.persistEntity(screen);
          
        }catch (Exception e) {
          throw new DAOTransactionRollbackException(e);
        }
      }
    });
    
    log.info("Screen created");
          
  }

  private static <UT extends ScreeningRoomUser> UT findSRU(Class<UT> userClass,
                                                    GenericEntityDAO dao,
                                                    String firstName,
                                                    String lastName,
                                                    String email)
  {
    Map<String,Object> props = new HashMap<String,Object>();
    props.put("firstName", firstName);
    props.put("lastName", lastName);
    props.put("email", email);
    List<UT> users = dao.findEntitiesByProperties(userClass, props);
    if (users.size() > 1) {
      throw new DuplicateEntityException(users.get(0));
    }
    if (users.size() == 1) {
      log.info("found existing user " + users.get(0) + " for " + firstName + " " + lastName + ", " + email);
      return users.get(0);
    }
    throw new RuntimeException("could not find existing user for " + firstName + " " + lastName + ", " + email);
  }
}
