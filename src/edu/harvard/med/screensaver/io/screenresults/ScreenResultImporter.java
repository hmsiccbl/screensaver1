// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.screenresults;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Iterator;

import jxl.write.WritableWorkbook;

import edu.harvard.med.screensaver.CommandLineApplication;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.DAOTransactionRollbackException;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.ScreenResultsDAO;
import edu.harvard.med.screensaver.io.workbook2.WorkbookParseError;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.util.FileUtils;

import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.math.IntRange;
import org.apache.log4j.Logger;

public class ScreenResultImporter
{
  // static members

  private static Logger log = Logger.getLogger(ScreenResultImporter.class);

  public static final int SHORT_OPTION = 0;
  public static final int LONG_OPTION = 1;

  static final String[] INPUT_FILE_OPTION = { "f", "input-file" };
  static final String[] SCREEN_OPTION = { "s", "screen" };
  static final String[] IMPORT_OPTION = { "i", "import" };
  static final String[] WELLS_OPTION = { "w", "wells" };
  static final String[] APPEND_OPTION = { "a", "append" };
  static final String[] PLATE_NUMBER_START_OPTION = { "sp", "start-plate" };
  static final String[] PLATE_NUMBER_END_OPTION = { "ep", "end-plate" };

  private static final String ERROR_ANNOTATED_WORKBOOK_FILE_EXTENSION = "errors.xls";

  protected static final String SCREEN_RESULT_IMPORTER_SPRING_CONFIGURATION = "spring-context-screen-result-parser-app.xml";


  @SuppressWarnings("static-access")
  public static void main(String[] args) throws FileNotFoundException
  {
    CommandLineApplication app = new CommandLineApplication(args);
    app.addCommandLineOption(OptionBuilder.hasArg()
                                          .withArgName("#")
                                          .isRequired()
                                          .withDescription("the screen number of the screen for which the screen result is being parsed")
                                          .withLongOpt(SCREEN_OPTION[LONG_OPTION])
                                          .create(SCREEN_OPTION[SHORT_OPTION]));
    app.addCommandLineOption(OptionBuilder.hasArg()
                                          .withArgName("file")
                                          .isRequired()
                                          .withDescription("the file location of the Excel workbook file holding the Screen Result metadata")
                                          .withLongOpt(INPUT_FILE_OPTION[LONG_OPTION])
                                          .create(INPUT_FILE_OPTION[SHORT_OPTION]));
    app.addCommandLineOption(OptionBuilder.hasArg()
                                          .withArgName("#")
                                          .isRequired(false)
                                          .withDescription("the number of wells to print out")
                                          .withLongOpt(WELLS_OPTION[LONG_OPTION])
                                          .create(WELLS_OPTION[SHORT_OPTION]));
    app.addCommandLineOption(OptionBuilder.withDescription("The first plate number to parse/import")
                                           .hasArg()
                                           .withArgName("#")
                                           .withLongOpt(PLATE_NUMBER_START_OPTION[LONG_OPTION])
                                           .create(PLATE_NUMBER_START_OPTION[SHORT_OPTION]));
    app.addCommandLineOption(OptionBuilder.withDescription("The last plate number to parse/import")
                                          .hasArg()
                                          .withArgName("#")
                                          .withLongOpt(PLATE_NUMBER_END_OPTION[LONG_OPTION])
                                          .create(PLATE_NUMBER_END_OPTION[SHORT_OPTION]));
    app.addCommandLineOption(OptionBuilder.withDescription("Append the specified range of plate numbers to an existing screen result.")
                                          .withLongOpt(APPEND_OPTION[LONG_OPTION])
                                          .create(APPEND_OPTION[SHORT_OPTION]));
    app.addCommandLineOption(OptionBuilder.withDescription("Import screen result into database if parsing is successful.  "
                                                           + "(By default, the parser only validates the input and then exits.)")
                                          .withLongOpt(IMPORT_OPTION[LONG_OPTION])
                                          .create(IMPORT_OPTION[SHORT_OPTION]));
    try {
      if (!app.processOptions(/* acceptDatabaseOptions= */true,
                              /* showHelpOnError= */true)) {
        return;
      }
      // if parse-only mode is requested, use a spring configuration that does not have a database dependency
      if (!app.isCommandLineFlagSet(IMPORT_OPTION[SHORT_OPTION])) {
        app.setSpringConfigurationResource(SCREEN_RESULT_IMPORTER_SPRING_CONFIGURATION);
      }

      final File inputFile = app.getCommandLineOptionValue(INPUT_FILE_OPTION[SHORT_OPTION],
                                                           File.class);
      cleanOutputDirectory(inputFile.getAbsoluteFile().getParentFile());

      Screen screen = findScreenOrExit(app);
      ScreenResultParser screenResultParser = null;
      screenResultParser = (ScreenResultParser) app.getSpringBean("screenResultParser");

      final GenericEntityDAO dao = (GenericEntityDAO) app.getSpringBean("genericEntityDao");
      final ScreenResultsDAO screenResultsDao = (ScreenResultsDAO) app.getSpringBean("screenResultsDao");

      final Integer wellsToPrint = app.getCommandLineOptionValue(WELLS_OPTION[SHORT_OPTION],
                                                                 Integer.class);

      IntRange plateNumberRange = null;
      if (app.isCommandLineFlagSet(PLATE_NUMBER_START_OPTION[SHORT_OPTION]) &&
        app.isCommandLineFlagSet(PLATE_NUMBER_END_OPTION[SHORT_OPTION])) {
        plateNumberRange = 
          new IntRange(app.getCommandLineOptionValue(PLATE_NUMBER_START_OPTION[SHORT_OPTION], Integer.class),
                       app.getCommandLineOptionValue(PLATE_NUMBER_END_OPTION[SHORT_OPTION], Integer.class));
        log.info("will parse/load plates " + plateNumberRange);
      }
      final IntRange finalPlateNumberRange = plateNumberRange;
      final boolean append = app.isCommandLineFlagSet(APPEND_OPTION[SHORT_OPTION]); 

      final Screen finalScreen = screen;
      final ScreenResultParser finalScreenResultParser = screenResultParser;
      dao.doInTransaction(new DAOTransaction() {
        public void runTransaction()
        {
          dao.reattachEntity(finalScreen);

          if (finalScreen.getScreenResult() != null) {
            if (append) {
              log.info("appending existing screen result (loading existing screen result data)");
              dao.need(finalScreen.getScreenResult(), "resultValueTypes");//.resultValues");
            }
            else {
              log.info("deleting existing screen result for " + finalScreen);
              screenResultsDao.deleteScreenResult(finalScreen.getScreenResult());
            }
          }

          ScreenResult screenResult;
          try {
            screenResult = finalScreenResultParser.parse(finalScreen,
                                                                      inputFile,
                                                                      finalPlateNumberRange);
          }
          catch (FileNotFoundException e) {
            String msg = "Screen result file not found: " + inputFile;
            log.error(msg);
            throw new DAOTransactionRollbackException(msg);
          }
          if (finalScreenResultParser.getHasErrors()) {
            log.error("Errors encountered during parse:");
            for (WorkbookParseError error : finalScreenResultParser.getErrors()) {
              log.error(error.toString());
            }
            throw new DAOTransactionRollbackException("screen result errors");
          }

          if (wellsToPrint != null) {
            new ScreenResultPrinter(screenResult).print(wellsToPrint);
          }
          else {
            new ScreenResultPrinter(screenResult).print();
          }
          dao.saveOrUpdateEntity(screenResult);
          log.info("Done parsing input file.");
        }
      });
      log.info("Import completed successfully!");
    }
    catch (ParseException e) {
      log.error("error parsing command line options: " + e.getMessage());
    }
    catch (DAOTransactionRollbackException e) {
      // already handled
      log.error("aborted import due to error: " + e.getMessage());
    }
    catch (Exception e) {
      log.error("application error: " + e.getMessage());
      e.printStackTrace();
    }
    log.info("Exiting.");
  }

  private static Screen findScreenOrExit(CommandLineApplication app) throws ParseException
  {
    int screenNumber = Integer.parseInt(app.getCommandLineOptionValue(SCREEN_OPTION[SHORT_OPTION]));
    GenericEntityDAO dao = (GenericEntityDAO) app.getSpringBean("genericEntityDao");
    Screen screen = dao.findEntityByProperty(Screen.class, "screenNumber", screenNumber);
    if (screen == null) {
      log.error("screen " + screenNumber + " does not exist");
      System.exit(1);
    }
    return screen;
  }

  @SuppressWarnings("unchecked")
  private static void cleanOutputDirectory(File dir)
  {
    if (!dir.isDirectory()) {
      log.warn("cannot clean the directory '" + dir + "' since it is not a directory");
      return;
    }
    log.info("cleaning directory " + dir);
    Iterator<File> iterator = org.apache.commons.io.FileUtils.iterateFiles(dir,
                                                                           new String[] {ERROR_ANNOTATED_WORKBOOK_FILE_EXTENSION, ".out"},
                                                                           false);
    while (iterator.hasNext()) {
      File fileToDelete = (File) iterator.next();
      log.info("deleting previously generated outputfile '" + fileToDelete + "'");
      fileToDelete.delete();
    }
  }
}

