// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.screenresults;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Iterator;

import edu.harvard.med.screensaver.CommandLineApplication;
import edu.harvard.med.screensaver.io.ParseError;
import edu.harvard.med.screensaver.io.ParseErrorsException;
import edu.harvard.med.screensaver.io.workbook2.Workbook;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.service.screenresult.ScreenResultLoader;
import edu.harvard.med.screensaver.service.screenresult.ScreenResultLoader.MODE;

import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.math.IntRange;
import org.apache.log4j.Logger;

public class ScreenResultImporter extends CommandLineApplication
{
  public ScreenResultImporter(String[] cmdLineArgs)
  {
    super(cmdLineArgs);
  }

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
//  static final String[] IGNORE_WELLTYPE_ERRORS_OPTION = { "ignore", "ignore-well-type-errors" };
  static final String[] IGNORE_DUPLICATE_ERRORS_OPTION = { "ignoreDuplicates", "ignore-duplicate-well-errors" };
  static final String[] INCREMENTAL_FLUSH_OPTION = { "incrementalFlush", "incremental-flush" };

  private static final String ERROR_ANNOTATED_WORKBOOK_FILE_EXTENSION = "errors.xls";

  protected static final String SCREEN_RESULT_IMPORTER_SPRING_CONFIGURATION = "spring-context-screen-result-parser-app.xml";


  @SuppressWarnings("static-access")
  public static void main(String[] args) throws FileNotFoundException
  {
    ScreenResultImporter app = new ScreenResultImporter(args);
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
    app.addCommandLineOption(OptionBuilder.withDescription("Ignore any subsequent duplicates of a well")
                             .isRequired(false)
                             .withLongOpt(IGNORE_DUPLICATE_ERRORS_OPTION[LONG_OPTION])
                             .create(IGNORE_DUPLICATE_ERRORS_OPTION[SHORT_OPTION]));

    app.addCommandLineOption(OptionBuilder.withDescription("Set the incremental flushing option; this is necessary for conserving memory in large imports (default=\"true\")")
                             .isRequired(false)
                             .withArgName("value")
                             .withLongOpt(INCREMENTAL_FLUSH_OPTION[LONG_OPTION])
                             .create(INCREMENTAL_FLUSH_OPTION[SHORT_OPTION]));

    File inputFile = null;
    try {
      if (!app.processOptions(/* acceptDatabaseOptions= */true,
                              /* showHelpOnError= */true)) {
        return;
      }
      // if parse-only mode is requested, use a spring configuration that does not have a database dependency
      if (!app.isCommandLineFlagSet(IMPORT_OPTION[SHORT_OPTION])) {
        app.setSpringConfigurationResource(SCREEN_RESULT_IMPORTER_SPRING_CONFIGURATION);
      }

      inputFile = app.getCommandLineOptionValue(INPUT_FILE_OPTION[SHORT_OPTION],
                                                           File.class);
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

      int screenNumber = Integer.parseInt(app.getCommandLineOptionValue(SCREEN_OPTION[SHORT_OPTION]));
      
      Workbook workbook = new Workbook(inputFile);

      try{
        ScreenResultLoader screenResultLoader = (ScreenResultLoader) app.getSpringBean("screenResultLoader");
        
        screenResultLoader.setIgnoreDuplicateErrors(app.isCommandLineFlagSet(IGNORE_DUPLICATE_ERRORS_OPTION[SHORT_OPTION]));
        
        ScreenResultLoader.MODE mode = MODE.DELETE_IF_EXISTS;
        if(append) mode = MODE.APPEND_IF_EXISTS;

        boolean incrementalFlush = true;
        if(app.isCommandLineFlagSet(INCREMENTAL_FLUSH_OPTION[SHORT_OPTION])){
          log.info("get incrementalFlush value");
          incrementalFlush = app.getCommandLineOptionValue(INCREMENTAL_FLUSH_OPTION[SHORT_OPTION],Boolean.class);
        }
        
        log.info("incrementalFlush: " + incrementalFlush);
        
        ScreenResult screenResult = screenResultLoader.parseAndLoad(
                                        workbook,
                                        finalPlateNumberRange,
                                        mode,
                                        screenNumber,
                                        incrementalFlush);
        if (wellsToPrint != null) {
          new ScreenResultPrinter(screenResult).print(wellsToPrint);
        }
        else {
          new ScreenResultPrinter(screenResult).print();
        }
        System.exit(0);
      }
      catch (ParseErrorsException e) 
      {
        if(e.getErrors().size() > 100 ) 
        {
          for(ParseError pe: e.getErrors())
          {
            log.warn("" + pe);
          }
          log.warn("" + e.getErrors().size() + " errors found.");
        }
        
        // Remove the error annotated workbook for now - sde4
        //        if(e.getErrors().size() > 100)
        //        { //NOTE: this is due to memory constraints
        //          log.warn("Too many errors to write out the error annotated workbook.  See console output (above) for errors");
        //        }else{
        //          File errorsFile = FileUtils.modifyFileDirectoryAndExtension(inputFile, (File) null, "error.xls");
        //          log.warn("Errors found in the input file, see ErrorAnnotatedWorkbook: " + errorsFile ); 
        //          cleanErrorAnnotatedWorkbooks(inputFile.getAbsoluteFile().getParentFile());
        //          workbook.writeErrorAnnotatedWorkbook(errorsFile);
        //        }
      }    
      catch (Exception e) {
        log.error("application error: " + e.getMessage());
        e.printStackTrace();
      }
    }
    catch (FileNotFoundException e) {
      String msg = "Screen result file not found: " + inputFile;
      log.error(msg);
    }
    catch (ParseException e) {
      log.error("error parsing command line options: " + e.getMessage());
    }
    // If here then error;
    System.exit(1);
  }

  @SuppressWarnings("unchecked")
  private static void cleanErrorAnnotatedWorkbooks(File dir)
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

