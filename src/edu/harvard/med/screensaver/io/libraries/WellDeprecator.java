// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import edu.harvard.med.screensaver.CommandLineApplication;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.DAOTransactionRollbackException;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.io.FatalParseException;
import edu.harvard.med.screensaver.model.AdministrativeActivity;
import edu.harvard.med.screensaver.model.AdministrativeActivityType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.users.AdministratorUser;

import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;

/**
 * Command-line application that deprecates a set of wells
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class WellDeprecator
{
  // static members

  private static Logger log = Logger.getLogger(WellDeprecator.class);

  @SuppressWarnings("static-access")
  public static void main(String[] args)
  {
    final CommandLineApplication app = new CommandLineApplication(args);
    try {
      app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("eCommons ID").withLongOpt("user-performed-by").withDescription("eCommons ID of administrator performing this well deprecation activity").create("p"));
      app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("eCommons ID").withLongOpt("user-approved-by").withDescription("eCommons ID of administrator that approved this well deprecation activity").create("a"));
      app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("yyyy-mm-dd").withLongOpt("approval-date").withDescription("date this well deprecation activity was approved").create("d"));
      app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("text").withLongOpt("comments").create("c"));
      app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("file").withLongOpt("input-file").withDescription("workbook file containing list of wells to be deprecated").create("f"));
      if (!app.processOptions(true, true)) {
        System.exit(1);
      }

      String comments = app.getCommandLineOptionValue("c");
      String approvedByEcommonsId = app.getCommandLineOptionValue("a");
      String performedByEcommonsId = app.getCommandLineOptionValue("p");
      LocalDate dateApproved = app.getCommandLineOptionValue("d", DateTimeFormat.forPattern(CommandLineApplication.DEFAULT_DATE_PATTERN)).toLocalDate();

      GenericEntityDAO dao = (GenericEntityDAO) app.getSpringBean("genericEntityDao");
      final LibrariesDAO librariesDao = (LibrariesDAO) app.getSpringBean("librariesDao");
      AdministratorUser performedBy = findAdminUser(performedByEcommonsId, dao);
      AdministratorUser approvedBy = findAdminUser(approvedByEcommonsId, dao);

      final AdministrativeActivity activity =
        new AdministrativeActivity(performedBy,
                                   approvedBy,
                                   dateApproved,
                                   AdministrativeActivityType.WELL_DEPRECATION);
      activity.setComments(comments);

      final Set<WellKey> wellKeys = readWellsFromFile(app);
      dao.doInTransaction(new DAOTransaction() {
        public void runTransaction() {
          for (WellKey wellKey : wellKeys) {
            Well well = librariesDao.findWell(wellKey);
            if (well == null) {
              throw new DAOTransactionRollbackException("no such well " + wellKey);
            }
            else {
              if (well.getDeprecationActivity() != null) {
                throw new DAOTransactionRollbackException("well " + wellKey + " is already deprecated");
              }
              well.setDeprecationActivity(activity);
            }
          }
        }
      });

      log.info("successfully deprecated " + wellKeys.size() + " wells");
    }
    catch (Exception e) {
      e.printStackTrace();
      log.error(e.toString());
      System.exit(1);
    }
  }

  private static AdministratorUser findAdminUser(String performedByEcommonsId,
                                                 GenericEntityDAO dao)
    throws Exception
  {
    AdministratorUser performedBy =
      dao.findEntityByProperty(AdministratorUser.class,
                               "ECommonsId",
                               performedByEcommonsId,
                               false,
                               "activitiesPerformed",
                               "activitiesApproved");
    if (performedBy == null) {
      throw new Exception("no such user with eCommons ID " + performedByEcommonsId);
    }
    return performedBy;
  }

  private static Set<WellKey> readWellsFromFile(CommandLineApplication app) throws IOException, ParseException
  {
    Set<WellKey> wellKeys = new HashSet<WellKey>();
    LineIterator lines = FileUtils.lineIterator(new File(app.getCommandLineOptionValue("f")), null);
    while (lines.hasNext()) {
      String line = lines.nextLine().trim();
      if (line.length() > 0) {
        try {
          wellKeys.add(new WellKey(line));
        }
        catch (Exception e) {
          throw new FatalParseException("invalid well key '" + line + "': " + e);
        }
      }
    }
    return wellKeys;
  }
}
