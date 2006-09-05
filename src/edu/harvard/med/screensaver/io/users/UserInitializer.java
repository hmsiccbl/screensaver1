//$HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
//$Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
//Copyright 2006 by the President and Fellows of Harvard College.
//
//Screensaver is an open-source project developed by the ICCB-L and NSRB labs
//at Harvard Medical School. This software is distributed under the terms of
//the GNU General Public License.

package edu.harvard.med.screensaver.io.users;

import edu.harvard.med.screensaver.CommandLineApplication;
import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;

import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

/**
 * Bootstraps users and roles in the Screensaver database, via invocation as a
 * command-line application. Aborts if any user or roles already exist. Also
 * adds a single user with the 'developer' role and 'usersAdmin' role, allowing
 * this special user to create/import new users. The login, password, and email
 * of this user are specified with the <code>--developer-login</code>,
 * <code>--developer-password</code>, and <code>--developer-email</code>
 * command-line options, respectively. Database connection settings are taken
 * from the datasource.properties file (on the classpath), unless the
 * SCREENSAVER_PGSQL_{SERVER,DB,USER,PASSWORD} environment variables are
 * specified.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class UserInitializer
{
  private static Logger log = Logger.getLogger(UserInitializer.class);
  
  private DAO _dao;
  
  public DAO getDao()
  {
    return _dao;
  }
  
  public void setDao(DAO dao)
  {
    _dao = dao;
  }
  
  public void initialize(
    final String developerLoginId,
    final String developerPassword,
    final String developerEmail)
  {
    _dao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        try {
          verifyVirginState();
          initializeUserRoles();
          initializeAdministrators();
          initializeDeveloper(developerLoginId, developerPassword, developerEmail);
        }
        catch (UserInitializerException e) {
          log.error(e.getMessage());
        }
      }
    });
  }
  
  private void initializeDeveloper(
    String developerLoginId,
    String developerPassword,
    String developerEmail) throws UserInitializerException
  {
    ScreensaverUserRole readEverythingAdminRole = _dao.findEntityByProperty(ScreensaverUserRole.class,
                                                                            "roleName",
                                                                            "readEverythingAdmin");
    ScreensaverUserRole usersAdminRole = _dao.findEntityByProperty(ScreensaverUserRole.class,
                                                                   "roleName",
                                                                   "usersAdmin");
    ScreensaverUserRole developerRole = _dao.findEntityByProperty(ScreensaverUserRole.class,
                                                                  "roleName",
                                                                  "developer");

    ScreensaverUser developer = _dao.defineEntity(ScreensaverUser.class,
                                                  "",
                                                  "",
                                                  developerEmail);
    developer.addScreensaverUserRole(readEverythingAdminRole);
    developer.addScreensaverUserRole(usersAdminRole);
    developer.addScreensaverUserRole(developerRole);
    developer.setLoginId(developerLoginId);
    developer.updateScreensaverPassword(developerPassword);
    _dao.persistEntity(developer);

  }
  
  private void initializeUserRoles() throws UserInitializerException
  {

    // TODO: read these roles from a file, thus justifying the package this
    // class in
    _dao.defineEntity(ScreensaverUserRole.class,
                      "readEverythingAdmin",
                      "Read-everything administrators will have the ability to view and search over data of all categories, except a screen's billing information. In addition, they will have the ability to generate various reports on screens.");
    _dao.defineEntity(ScreensaverUserRole.class,
                      "librariesAdmin",
                      "Administrators that can create and modify libraries.");
    _dao.defineEntity(ScreensaverUserRole.class,
                      "usersAdmin",
                      "Administrators that can create and modify user accounts.");
    _dao.defineEntity(ScreensaverUserRole.class,
                      "screensAdmin",
                      "Administrators that can create and modify screens.");
    _dao.defineEntity(ScreensaverUserRole.class,
                      "screenResultsAdmin",
                      "Administrators that can create and modify screen results.");
    _dao.defineEntity(ScreensaverUserRole.class,
                      "billingAdmin",
                      "Administrators that can view, create, and modify billing information for a screen.");
    _dao.defineEntity(ScreensaverUserRole.class,
                      "screeningRoomUser",
                      "User that have permission to view and search over non-administrative information for certain data records.");
    _dao.defineEntity(ScreensaverUserRole.class,
                      "rnaiScreeningRoomUser",
                      "User that have permission to view and search over non-administrative information for all RNAi screens.");
    _dao.defineEntity(ScreensaverUserRole.class,
                      "compoundScreeningRoomUser",
                      "User that have permission to view and search over non-administrative information for all compound screens and any compound screen results which are demarked 'shareable'.");
    _dao.defineEntity(ScreensaverUserRole.class,
                      "developer",
                      "User that have permission to invoke development-related functionality and view low-level system information.");
  }
  
  private void initializeAdministrators() throws UserInitializerException
  {
    // TODO:
  }
  
  private void verifyVirginState() throws UserInitializerException
  {
    if (_dao.findAllEntitiesWithType(ScreensaverUserRole.class).size() > 0) {
      throw new UserInitializerException("aborting since user and/or roles already exist");
    }
  }
  
  
  @SuppressWarnings("static-access")
  public static void main(String[] args) throws ParseException
  {
    CommandLineApplication app = new CommandLineApplication(args);
    app.addCommandLineOption(OptionBuilder.withArgName("developer-password")
                             .withLongOpt("developer-password")
                             .hasArg()
                             .isRequired()
                             .create());
    app.addCommandLineOption(OptionBuilder.withArgName("developer-login")
                             .withLongOpt("developer-login")
                             .hasArg()
                             .isRequired()
                             .create());
    app.addCommandLineOption(OptionBuilder.withArgName("developer-email")
                             .withLongOpt("developer-email")
                             .hasArg()
                             .isRequired()
                             .create());
    if (app.processOptions(true, true)) {
      UserInitializer userInitializer = (UserInitializer) app.getSpringBean("userInitializer");
      userInitializer.initialize(app.getCommandLineOptionValue("developer-login"),
                                 app.getCommandLineOptionValue("developer-password"),
                                 app.getCommandLineOptionValue("developer-email"));
    }
  }
}
