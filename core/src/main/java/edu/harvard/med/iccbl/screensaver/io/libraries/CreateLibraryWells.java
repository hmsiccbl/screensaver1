// $HeadURL$
// $Id$
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.iccbl.screensaver.io.libraries;

import java.util.List;

import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.io.CommandLineApplication;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.service.libraries.LibraryCreator;

/**
 * Command-line application that adds missing wells to a library 
 *  
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class CreateLibraryWells
{
  private static Logger log = Logger.getLogger(CreateLibraryWells.class);

  @SuppressWarnings("static-access")
  public static void main(String[] args) throws ParseException
  {
    final CommandLineApplication app = new CommandLineApplication(args);
    app.addCommandLineOption(OptionBuilder.hasArgs().isRequired()
                             .withArgName("short name").withLongOpt("short-name")
                             .withDescription("a short name for identifying the library").create("l"));

    if (!app.processOptions(true, false, true)) {
      System.exit(1);
    }

    final GenericEntityDAO dao = (GenericEntityDAO) app.getSpringBean("genericEntityDao");
    dao.doInTransaction(new DAOTransaction() {

      @Override
      public void runTransaction()
      {
        try {
          LibraryCreator libraryCreator = (LibraryCreator) app.getSpringBean("libraryCreator");
          List<String> libraryShortNames = app.getCommandLineOptionValues("l");
          for (String libraryShortName : libraryShortNames) {
            libraryCreator.createWells(dao.findEntityByProperty(Library.class, "shortName", libraryShortName));
          }
        }
        catch (Exception e) {
          e.printStackTrace();
          log.error(e.toString());
          System.err.println("error: " + e.getMessage());
          System.exit(1);
        }
      }

    });
  }
}
