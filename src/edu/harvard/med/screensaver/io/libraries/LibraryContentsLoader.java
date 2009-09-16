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
import java.io.FileInputStream;

import edu.harvard.med.screensaver.CommandLineApplication;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.io.ParseError;
import edu.harvard.med.screensaver.io.ParseErrorsException;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.service.libraries.LibraryCreator;

import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

/**
 * Front end for the Library Loading methods on {@link LibraryCreator}
 */
public class LibraryContentsLoader
{
  private static Logger log = Logger.getLogger(LibraryContentsLoader.class);

  public static final int SHORT_OPTION_INDEX = 0;
  public static final int LONG_OPTION_INDEX = 1;
  public static final int DESCRIPTION_INDEX = 2;

  static final String[] INPUT_FILE_OPTION = {
    "f",
    "input-file",
    "the file to load library contents from"
  };
  static final String[] LIBRARY_SHORT_NAME_OPTION = {
    "l",
    "library-short-name",
    "the short name of the library to load contents for"
  };
  static final String[] ADMIN_USER_ECOMMONS_ID_OPTION = {
    "u",
    "ecommons-id",
    "the eCommons ID of the administrative user performing the load"
  };

  @SuppressWarnings("static-access")
  public static void main(String[] args) throws ParseException
  {
    CommandLineApplication application = new CommandLineApplication(args);
    application.addCommandLineOption(
        OptionBuilder
            .hasArg()
            .withArgName(LIBRARY_SHORT_NAME_OPTION[SHORT_OPTION_INDEX])
            .isRequired()
            .withDescription(LIBRARY_SHORT_NAME_OPTION[DESCRIPTION_INDEX])
            .withLongOpt(LIBRARY_SHORT_NAME_OPTION[LONG_OPTION_INDEX])
            .create(LIBRARY_SHORT_NAME_OPTION[SHORT_OPTION_INDEX]));
    application.addCommandLineOption(
        OptionBuilder
            .hasArg()
            .withArgName(INPUT_FILE_OPTION[SHORT_OPTION_INDEX])
            .isRequired()
            .withDescription(INPUT_FILE_OPTION[DESCRIPTION_INDEX])
            .withLongOpt(INPUT_FILE_OPTION[LONG_OPTION_INDEX])
            .create(INPUT_FILE_OPTION[SHORT_OPTION_INDEX]));
    
    //TODO: allow this to be optional and glean the eCommons ID from the environment - sde4
    application.addCommandLineOption(
        OptionBuilder
            .hasArg()
            .withArgName(ADMIN_USER_ECOMMONS_ID_OPTION[SHORT_OPTION_INDEX])
            .isRequired()
            .withDescription(ADMIN_USER_ECOMMONS_ID_OPTION[DESCRIPTION_INDEX])
            .withLongOpt(ADMIN_USER_ECOMMONS_ID_OPTION[LONG_OPTION_INDEX])
            .create(ADMIN_USER_ECOMMONS_ID_OPTION[SHORT_OPTION_INDEX]));
    
    if (! application.processOptions(true, true)) {
      return;
    }
    String libraryShortName =
        application.getCommandLineOptionValue(LIBRARY_SHORT_NAME_OPTION[SHORT_OPTION_INDEX]);
    File libraryContentsFile =
        application.getCommandLineOptionValue(INPUT_FILE_OPTION[SHORT_OPTION_INDEX], File.class);
    String ecommonsId = 
        application.getCommandLineOptionValue(ADMIN_USER_ECOMMONS_ID_OPTION[SHORT_OPTION_INDEX]);
    
    edu.harvard.med.screensaver.service.libraries.LibraryContentsLoader libraryContentsLoader =
      (edu.harvard.med.screensaver.service.libraries.LibraryContentsLoader) application.getSpringBean("libraryContentsLoader");
    GenericEntityDAO dao = (GenericEntityDAO) application.getSpringBean("genericEntityDao");

    try {
      Library library = dao.findEntityByProperty(Library.class, "shortName", libraryShortName);
      if (library == null) {
        throw new IllegalArgumentException("no library with short name: " + libraryShortName);
      }
      AdministratorUser admin = dao.findEntityByProperty(AdministratorUser.class, "ECommonsId", ecommonsId);
      if (admin == null) {
        throw new IllegalArgumentException("no administrator user with eCommons ID: " + ecommonsId);
      }
      libraryContentsLoader.loadLibraryContents(library, admin, null /*TODO*/, new FileInputStream(libraryContentsFile));
    } 
    catch (ParseErrorsException e) {
      for (ParseError error : e.getErrors()) {
        log.error(error.toString());
      }
    }
    catch (Exception e) {
      log.error("application exception", e);
    }
  }
}
