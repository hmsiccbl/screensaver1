// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db.screendb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import edu.harvard.med.screensaver.CommandLineApplication;
import edu.harvard.med.screensaver.db.CherryPickRequestDAO;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Synchronizes the Screensaver database to the latest contents of ScreenDB. For every ScreenDB
 * entity parsed, the synchronizer tries to find a matching corresponding entity in Screensaver.
 * If it finds one, it will update the existing entity to match the contents in ScreenDB. If not,
 * it will create a new Screensaver entity to represent the ScreenDB entity.
 * <p>
 * This procedure works fine, unless a ScreenDB entity previously synchronized over to Screensaver
 * is modified in such a way that the synchronizer is unable to make the connection between the
 * new version of the ScreenDB entity, and the old version as translated into Screensaver terms.
 * In such a case, the original entity will be duplicated in Screensaver, having both an up-to-date
 * version, and an out-of-date version.
 * <p>
 * This could only happen when part of the "business key" for the entity changes. These aren't
 * necessarilly Screensaver entity business keys, and also should not really change that much.
 * A list of the different entity types, and the "business keys" used by the synchronizer, follows:
 * 
 * <ul>
 * <li>ScreeningRoomUsers are looked up by firstName, lastName
 * <li>Libraries are looked up by startPlate
 * <li>Screens are looked up by screenNumber
 * </ul>
 * 
 * <p>
 * It should be noted that this synchronizer may not run correctly against a newly created Screensaver
 * database, or a Screensaver databaase that is out-of-date in relation to the ScreenDB database.
 * There are two places where this kind of issue currently comes up:
 * 
 * <ul>
 * <li>For {@link CompoundCherryPickSynchronizer compound cherry picks}, if a cherry pick in ScreenDB
 * goes against a well in Screensaver that is non-experimental, then an error is reported, and the
 * cherry pick is not added to Screensaver. This is appropriate in most circumstances, but can be
 * problematic if the contents for the library have not been loaded yet, since the library wells will
 * appear as {@link WellType#EMPTY} in this situation.
 * <li>The {@link RNAiCherryPickScreeningSynchronizer} assumes that the RNAi cherry pick requests
 * already exist in the Screensaver database, since the ScreenDB records in the RNAi cherry pick
 * screenings refer to the cherry pick request numbers for this request.
 * </ul>
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class ScreenDBSynchronizer
{
  // static members

  private static Logger log = Logger.getLogger(ScreenDBSynchronizer.class);
  static {
    try {
      Class.forName("org.postgresql.Driver");
    }
    catch (ClassNotFoundException e) {
      log.error("couldn't find postgresql driver");
    }    
  }
  
  public static void main(String [] args)
  {
    ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] { 
      CommandLineApplication.DEFAULT_SPRING_CONFIG,
    });
    Options options = new Options();
    options.addOption("S", true, "server");
    options.addOption("D", true, "database");
    options.addOption("U", true, "username");
    options.addOption("P", true, "password");
    CommandLine commandLine;
    try {
      commandLine = new GnuParser().parse(options, args);
    }
    catch (ParseException e) {
      log.error("error parsing command line options", e);
      return;
    }
    ScreenDBSynchronizer synchronizer = new ScreenDBSynchronizer(
      commandLine.getOptionValue("S"),
      commandLine.getOptionValue("D"),
      commandLine.getOptionValue("U"),
      commandLine.getOptionValue("P"),
      (GenericEntityDAO) context.getBean("genericEntityDao"),
      (LibrariesDAO) context.getBean("librariesDao"),
      (CherryPickRequestDAO) context.getBean("cherryPickRequestDao"));
    synchronizer.synchronize();
    log.info("successfully synchronized with ScreenDB.");
  }
  

  // instance data members
  
  private String _server;
  private String _database;
  private String _username;
  private String _password;
  private Connection _connection;
  private GenericEntityDAO _dao;
  private LibrariesDAO _librariesDao;
  private CherryPickRequestDAO _cherryPickRequestDao;
  private CompoundCherryPickSynchronizer compoundCherryPickSynchronizer;
  

  // public constructors and methods

  public ScreenDBSynchronizer(String server, 
                              String database, 
                              String username, 
                              String password, 
                              GenericEntityDAO dao,
                              LibrariesDAO librariesDao,
                              CherryPickRequestDAO cherryPickRequestDao)
  {
    _server = server;
    _database = database;
    _username = username;
    _password = password;
    _dao = dao;
    _librariesDao = librariesDao;
    _cherryPickRequestDao = cherryPickRequestDao;
  }
  
  /**
   * Synchronize Screensaver with ScreenDB.
   * @throws ScreenDBSynchronizationException whenever there is a problem synchronizing
   */
  public void synchronize() throws ScreenDBSynchronizationException
  {
    initializeConnection();
    deleteOldCompoundCherryPickRequests();
    synchronizeLibraries();
    synchronizeNonLibraries();
    closeConnection();
  }

  private void initializeConnection() throws ScreenDBSynchronizationException
  {
    try {
      _connection = DriverManager.getConnection(
        "jdbc:postgresql://" + _server + "/" + _database,
        _username,
        _password);
    }
    catch (SQLException e) {
      throw new ScreenDBSynchronizationException("could not connect to ScreenDB database", e);
    }
  }

  /**
   * Delete the old compound cherry pick requests in a separate transaction.
   * 
   * @motivation This should really be part of
   *             {@link CompoundCherryPickSynchronizer}, but I need to run
   *             it in a separate transaction or I get hibernate exceptions
   *             about deleted entities that would be resaved by cascade. I
   *             probably should refactor things a bit so I can call
   *             "deleteOldCherryPickRequests" methods in the
   *             CCPSynchronizer, in separate transactions.
   */
  private void deleteOldCompoundCherryPickRequests()
  {
    log.info("deleting old compound cherry pick requests..");
    _dao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        _cherryPickRequestDao.deleteAllCompoundCherryPickRequests();
      }
    });
    log.info("done deleting old compound cherry pick requests.");
  }

  private void synchronizeLibraries() throws ScreenDBSynchronizationException
  {
    log.info("synchronizing libraries..");
    _dao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        LibrarySynchronizer librarySynchronizer =
          new LibrarySynchronizer(_connection, _dao, _librariesDao);
        librarySynchronizer.synchronizeLibraries();
      }
    }); 
    log.info("done synchronizing libraries.");
 }

  private void synchronizeNonLibraries() throws ScreenDBSynchronizationException
  {
    final UserSynchronizer userSynchronizer =
      new UserSynchronizer(_connection, _dao);
    final ScreenSynchronizer screenSynchronizer =
      new ScreenSynchronizer(_connection, _dao, userSynchronizer);
    final LibraryScreeningSynchronizer libraryScreeningSynchronizer =
      new LibraryScreeningSynchronizer(_connection, _dao, _librariesDao, userSynchronizer, screenSynchronizer);
    final CompoundCherryPickSynchronizer compoundCherryPickSynchronizer =
      new CompoundCherryPickSynchronizer(_connection, _dao, _librariesDao, userSynchronizer, screenSynchronizer);
    final RNAiCherryPickScreeningSynchronizer rnaiCherryPickScreeningSynchronizer =
      new RNAiCherryPickScreeningSynchronizer(_connection, _dao, _librariesDao, userSynchronizer, screenSynchronizer);
    
    log.info("synchronizing non-libraries..");
    _dao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        log.info("synchronizing users..");
        userSynchronizer.synchronizeUsers();
        log.info("done synchronizing users.");
        log.info("synchronizing screens..");
        screenSynchronizer.synchronizeScreens();
        log.info("done synchronizing screens.");
        log.info("synchronizing library screenings..");
        libraryScreeningSynchronizer.synchronizeLibraryScreenings();
        log.info("done synchronizing library screenings.");
        log.info("synchronizing compound cherry picks..");
        compoundCherryPickSynchronizer.synchronizeCompoundCherryPicks();
        log.info("done synchronizing compound cherry picks.");
        log.info("synchronizing rnai cherry pick screenings..");
        rnaiCherryPickScreeningSynchronizer.synchronizeRnaiCherryPickScreenings();
        log.info("done synchronizing rnai cherry pick screenings.");
      }
    });
    log.info("done synchronizing non-libraries.");
  }

  private void closeConnection()
  {
    try {
      _connection.close();
    }
    catch (SQLException e) {
    }
  }
}

