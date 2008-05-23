// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries.rnai;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import edu.harvard.med.screensaver.CommandLineApplication;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.io.ParseError;
import edu.harvard.med.screensaver.io.libraries.ParseLibraryContentsException;
import edu.harvard.med.screensaver.model.libraries.Library;

import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

/**
 * A command line tool to load a single RNAi library contents file.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class RNAiLibraryContentsFileLoader
{
  
  // static members

  private static final Logger log = Logger.getLogger(RNAiLibraryContentsFileLoader.class);

  public static final int SHORT_OPTION_INDEX = 0;
  public static final int LONG_OPTION_INDEX = 1;
  public static final int DESCRIPTION_INDEX = 2;

  public static final String[] INPUT_FILE_OPTION = {
    "f",
    "input-file",
    "the file to load library contents from"
  };
  public static final String[] LIBRARY_SHORT_NAME_OPTION = {
    "l",
    "library-short-name",
    "the short name of the library to load contents for"
  };
  public static final String[] START_PLATE_OPTION = {
    "sp",
    "start-plate",
    "the beginning of the plate range to be loaded (inclusive)"
  };
  public static final String[] END_PLATE_OPTION = {
    "ep",
    "end-plate",
    "the end of the plate range to be loaded (inclusive)"
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
    application.addCommandLineOption(OptionBuilder
                                     .hasArg()
                                     .withArgName(START_PLATE_OPTION[SHORT_OPTION_INDEX])
                                     .isRequired(false)
                                     .withDescription(START_PLATE_OPTION[DESCRIPTION_INDEX])
                                     .withLongOpt(START_PLATE_OPTION[LONG_OPTION_INDEX])
                                     .create(START_PLATE_OPTION[SHORT_OPTION_INDEX]));
    application.addCommandLineOption(OptionBuilder
                                     .hasArg()
                                     .withArgName(END_PLATE_OPTION[SHORT_OPTION_INDEX])
                                     .isRequired(false)
                                     .withDescription(END_PLATE_OPTION[DESCRIPTION_INDEX])
                                     .withLongOpt(END_PLATE_OPTION[LONG_OPTION_INDEX])
                                     .create(END_PLATE_OPTION[SHORT_OPTION_INDEX]));
    try {
      if (! application.processOptions(true, true)) {
        return;
      }
      String libraryShortName =
        application.getCommandLineOptionValue(LIBRARY_SHORT_NAME_OPTION[SHORT_OPTION_INDEX]);
      File libraryContentsFile =
        application.getCommandLineOptionValue(INPUT_FILE_OPTION[SHORT_OPTION_INDEX], File.class);
      RNAiLibraryContentsFileLoader libraryLoader = (RNAiLibraryContentsFileLoader)
        application.getSpringBean("rnaiLibraryContentsFileLoader");
      Integer startPlate = application.getCommandLineOptionValue(START_PLATE_OPTION[SHORT_OPTION_INDEX], Integer.class);
      Integer endPlate = application.getCommandLineOptionValue(END_PLATE_OPTION[SHORT_OPTION_INDEX], Integer.class);
      libraryLoader.loadLibrary(libraryShortName, libraryContentsFile, startPlate, endPlate);
    }
    catch (ParseException e) {
      log.error("error processing command line options", e);
    }
    catch (Exception e) {
      log.error("application exception", e);
    }
  }


  // instance data members
  
  private GenericEntityDAO _dao;
  private RNAiLibraryContentsParser _parser;
  
  
  // public constructors and methods

  public RNAiLibraryContentsFileLoader(GenericEntityDAO dao, RNAiLibraryContentsParser parser)
  {
    _dao = dao;
    _parser = parser;
  }

  /**
   * Database must be created and initialized before running this method.
   * 
   * @param startPlate the first plate in the plate range to be loaded; null
   *          okay; if endPlate is null, all plate after and including
   *          startPlate will be loaded
   * @param endPlate the last plate, inclusive, in the plate range to be loaded;
   *          null okay; if startPlate is null, all plates before and including
   *          endPlate will be loaded
   */
  public void loadLibrary(final String libraryShortName, 
                          final File libraryContentsFile, 
                          final Integer startPlate, 
                          final Integer endPlate)
  {
    _dao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        log.info("processing RNAi File: " + libraryContentsFile.getName());
        Library library = _dao.findEntityByProperty(Library.class, "shortName", libraryShortName);
        if (library == null) {
          log.error("couldn't find library with shortName \"" + libraryShortName + "\"");
          return;
        }
        try {
          _parser.parseLibraryContents(
            library,
            libraryContentsFile,
            new FileInputStream(libraryContentsFile),
            startPlate,
            endPlate);
        }
        catch (FileNotFoundException e) {
          throw new InternalError("braindamage: " + e.getMessage());
        }
        catch (ParseLibraryContentsException e) {
          for (ParseError error : e.getErrors()) {
            log.error(error.toString());
          }
        }
        _dao.saveOrUpdateEntity(library);
        log.info("finished processing RNAi File: " + libraryContentsFile.getName());
      }
    });
  }
}

