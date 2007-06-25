// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db.screendb;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import edu.harvard.med.screensaver.CommandLineApplication;
import edu.harvard.med.screensaver.db.CherryPickRequestDAO;
import edu.harvard.med.screensaver.db.DotPgpassFileParser;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;

/**
 * Standalone application for running the {@link ScreenDBSynchronizer} on orchestra.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class OrchestraStandaloneScreenDBSynchronizer
{
  // static members

  private static Logger log = Logger.getLogger(OrchestraStandaloneScreenDBSynchronizer.class);
  
  public static void main(String [] args)
  {
    ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] { 
      CommandLineApplication.DEFAULT_SPRING_CONFIG,
    });
    Options options = new Options();
    options.addOption("S", true, "server");
    options.addOption("D", true, "database");
    options.addOption("U", true, "username");
    // no passwords on command lines on orchestra!
    CommandLine commandLine;
    try {
      commandLine = new GnuParser().parse(options, args);
    }
    catch (ParseException e) {
      log.error("error parsing command line options", e);
      return;
    }
    String hostname = commandLine.getOptionValue("S");
    String database = commandLine.getOptionValue("D");
    String user = commandLine.getOptionValue("U");
    ScreenDBSynchronizer synchronizer = new ScreenDBSynchronizer(
      hostname,
      database,
      user,
      new DotPgpassFileParser().getPasswordFromDotPgpassFile(hostname, null, database, user),
      (GenericEntityDAO) context.getBean("genericEntityDao"),
      (LibrariesDAO) context.getBean("librariesDao"),
      (CherryPickRequestDAO) context.getBean("cherryPickRequestDao"));
    synchronizer.synchronize();
    log.info("successfully synchronized with ScreenDB.");
  }
}

