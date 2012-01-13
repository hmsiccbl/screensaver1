// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries;

import java.io.File;
import java.io.FileInputStream;

import org.apache.commons.cli.OptionBuilder;
import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.io.CommandLineApplication;
import edu.harvard.med.screensaver.io.ParseError;
import edu.harvard.med.screensaver.io.ParseErrorsException;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryContentsVersion;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.service.libraries.LibraryCreator;

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

  static final String[] RELEASE_IF_SUCCESS_OPTION = {
    "r",
    "release-library-contents-version",
    "release the library contents version if successful"
  };
  @SuppressWarnings("static-access")
  public static void main(String[] args)
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
    application.addCommandLineOption(
        OptionBuilder
            .isRequired(false)
            .withDescription(RELEASE_IF_SUCCESS_OPTION[DESCRIPTION_INDEX])
            .withLongOpt(RELEASE_IF_SUCCESS_OPTION[LONG_OPTION_INDEX])
            .create(RELEASE_IF_SUCCESS_OPTION[SHORT_OPTION_INDEX]));

    application.processOptions(true, true);
    String libraryShortName =
        application.getCommandLineOptionValue(LIBRARY_SHORT_NAME_OPTION[SHORT_OPTION_INDEX]);
    File libraryContentsFile =
        application.getCommandLineOptionValue(INPUT_FILE_OPTION[SHORT_OPTION_INDEX], File.class);
    boolean releaseIfSuccess = application.isCommandLineFlagSet(RELEASE_IF_SUCCESS_OPTION[SHORT_OPTION_INDEX]);

    edu.harvard.med.screensaver.service.libraries.LibraryContentsLoader libraryContentsLoader =
      (edu.harvard.med.screensaver.service.libraries.LibraryContentsLoader) application.getSpringBean("libraryContentsLoader");
    edu.harvard.med.screensaver.service.libraries.LibraryContentsVersionManager libraryContentsVersionManager =
      (edu.harvard.med.screensaver.service.libraries.LibraryContentsVersionManager) application.getSpringBean("libraryContentsVersionManager");
    GenericEntityDAO dao = (GenericEntityDAO) application.getSpringBean("genericEntityDao");

    try {
      AdministratorUser admin = application.findAdministratorUser();
      Library library = dao.findEntityByProperty(Library.class, "shortName", libraryShortName);
      if (library == null) {
        throw new IllegalArgumentException("no library with short name: " + libraryShortName);
      }
      LibraryContentsVersion lcv = libraryContentsLoader.loadLibraryContents(library, admin, null /*TODO*/, new FileInputStream(libraryContentsFile));
      if (releaseIfSuccess) {
        log.info("releasing...");
        libraryContentsVersionManager.releaseLibraryContentsVersion(lcv, admin);
      }
    } 
    catch (ParseErrorsException e) {
      for (ParseError error : e.getErrors()) {
        log.error(error.toString(), e);
      }
      System.exit(1);
    }
    catch (Exception e) {
      log.error("application exception", e);
      System.exit(1);
    }
  }
}
