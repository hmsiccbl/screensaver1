// $HeadURL:
// http://seanderickson1@forge.abcd.harvard.edu/svn/screensaver/branches/lincs/ui-cleanup/core/src/main/java/edu/harvard/med/screensaver/io/screenresults/ScreenResultImporter.java
// $
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

import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.math.IntRange;
import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.io.CommandLineApplication;
import edu.harvard.med.screensaver.io.ParseError;
import edu.harvard.med.screensaver.io.ParseErrorsException;
import edu.harvard.med.screensaver.io.workbook2.Workbook;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.service.EntityNotFoundException;
import edu.harvard.med.screensaver.service.screenresult.ScreenResultDeleter;
import edu.harvard.med.screensaver.service.screenresult.ScreenResultLoader;

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
  static final String[] PLATE_NUMBER_START_OPTION = { "sp", "start-plate" };
  static final String[] PLATE_NUMBER_END_OPTION = { "ep", "end-plate" };
  static final String[] IGNORE_DUPLICATE_ERRORS_OPTION = { "ignoreDuplicates", "ignore-duplicate-well-errors" };
  static final String[] INCREMENTAL_FLUSH_OPTION = { "incrementalFlush", "incremental-flush" };
  static final String[] COMMENTS = { "c", "comments", "Comments to be recorded for this screen result data loading activity" };
  static final String[] DELETE_EXISTING = { "d", "delete", "Delete existing screen result, if it exists" };

  private static final String ERROR_ANNOTATED_WORKBOOK_FILE_EXTENSION = "errors.xls";

  protected static final String SCREEN_RESULT_IMPORTER_SPRING_CONFIGURATION = "spring-context-screen-result-parser-app.xml";

  @SuppressWarnings("static-access")
  public static void main(String[] args) 
  {
    ScreenResultImporter app = new ScreenResultImporter(args);
    app.addCommandLineOption(OptionBuilder.hasArg()
                                          .withArgName("screen facility ID")
                                          .isRequired()
                                          .withDescription("the facility-assigned ID of the screen for which the screen result is being parsed")
                                          .withLongOpt(SCREEN_OPTION[LONG_OPTION])
                                          .create(SCREEN_OPTION[SHORT_OPTION]));
    app.addCommandLineOption(OptionBuilder.hasArg()
                                          .withArgName("file")
                                          .isRequired()
                                          .withDescription("the file location of the Excel workbook file holding the Screen Result metadata")
                                          .withLongOpt(INPUT_FILE_OPTION[LONG_OPTION])
                                          .create(INPUT_FILE_OPTION[SHORT_OPTION]));
    app.addCommandLineOption(OptionBuilder.hasArg()
                             .withArgName("comments")
                             .isRequired(false)
                             .withDescription("comments to associate with the data loading activity")
                             .withLongOpt(COMMENTS[LONG_OPTION])
                             .create(COMMENTS[SHORT_OPTION]));
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
    app.addCommandLineOption(OptionBuilder.withDescription(DELETE_EXISTING[2])
                             .hasArg(false)
                             .withLongOpt(DELETE_EXISTING[LONG_OPTION])
                             .create(DELETE_EXISTING[SHORT_OPTION]));

    app.processOptions(/* acceptDatabaseOptions= */true,
                       /* acceptAdminUserOptions= */true);
    try {
      execute(app);
    }
    catch (ParseErrorsException e) {
      if (!e.getErrors().isEmpty()) {
        for (ParseError pe : e.getErrors()) {
          log.error("" + pe);
        }
        log.error("" + e.getErrors().size() + " errors found.");
      }
      System.exit(1);
    }
    catch (Exception e) {
      log.error("Failed to create the screen result", e);
      System.exit(1);
    }

  }

  private static void execute(ScreenResultImporter app) throws EntityNotFoundException, FileNotFoundException, ParseException, ParseErrorsException
  {
    File inputFile;
    GenericEntityDAO dao = (GenericEntityDAO) app.getSpringBean("genericEntityDao");

    // if parse-only mode is requested, use a spring configuration that does not have a database dependency
    if (!app.isCommandLineFlagSet(IMPORT_OPTION[SHORT_OPTION])) { // TODO: this is a bug, since cannot instantiate the spring-application-context after getting the genericEntityDao bean (or any bean) - sde4
      app.setSpringConfigurationResource(SCREEN_RESULT_IMPORTER_SPRING_CONFIGURATION);
    }

    inputFile = app.getCommandLineOptionValue(INPUT_FILE_OPTION[SHORT_OPTION],
                                                         File.class);

    IntRange plateNumberRange = null;
    if (app.isCommandLineFlagSet(PLATE_NUMBER_START_OPTION[SHORT_OPTION]) &&
      app.isCommandLineFlagSet(PLATE_NUMBER_END_OPTION[SHORT_OPTION])) {
      plateNumberRange =
        new IntRange(app.getCommandLineOptionValue(PLATE_NUMBER_START_OPTION[SHORT_OPTION], Integer.class),
                     app.getCommandLineOptionValue(PLATE_NUMBER_END_OPTION[SHORT_OPTION], Integer.class));
      log.info("will parse/load plates " + plateNumberRange);
    }
    final IntRange finalPlateNumberRange = plateNumberRange;

    String screenFacilityId = app.getCommandLineOptionValue(SCREEN_OPTION[SHORT_OPTION]);
    Screen screen = dao.findEntityByProperty(Screen.class, Screen.facilityId.getPropertyName(), screenFacilityId);
    if (screen == null) {
      throw new EntityNotFoundException(Screen.class, screenFacilityId);
    }

    Workbook workbook = new Workbook(inputFile);
    ScreenResultLoader screenResultLoader = (ScreenResultLoader) app.getSpringBean("screenResultLoader");
    screenResultLoader.setIgnoreDuplicateErrors(app.isCommandLineFlagSet(IGNORE_DUPLICATE_ERRORS_OPTION[SHORT_OPTION]));

    boolean incrementalFlush = true;
    if (app.isCommandLineFlagSet(INCREMENTAL_FLUSH_OPTION[SHORT_OPTION])) {
      log.info("get incrementalFlush value");
      incrementalFlush = app.getCommandLineOptionValue(INCREMENTAL_FLUSH_OPTION[SHORT_OPTION], Boolean.class);
    }

    log.info("incrementalFlush: " + incrementalFlush);

    AdministratorUser admin = app.findAdministratorUser();
    deleteIfNecessary(app, screen, admin);
    String comments =
      app.getCommandLineOptionValue(COMMENTS[SHORT_OPTION]);
    screenResultLoader.parseAndLoad(screen,
                                    workbook,
                                    admin,
                                    comments,
                                    finalPlateNumberRange,
                                    incrementalFlush);
  }

  private static void deleteIfNecessary(ScreenResultImporter app, Screen screen, AdministratorUser admin)
    throws ParseException
  {
    boolean deleteExisting = app.isCommandLineFlagSet(DELETE_EXISTING[SHORT_OPTION]);
    if (deleteExisting && screen.getScreenResult() != null) {
      ScreenResultDeleter screenResultDeleter = (ScreenResultDeleter) app.getSpringBean("screenResultDeleter");
      screenResultDeleter.deleteScreenResult(screen.getScreenResult(), admin);
    }
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
                                                                           new String[] {
                                                                             ERROR_ANNOTATED_WORKBOOK_FILE_EXTENSION, ".out" },
                                                                           false);
    while (iterator.hasNext()) {
      File fileToDelete = (File) iterator.next();
      log.info("deleting previously generated outputfile '" + fileToDelete + "'");
      fileToDelete.delete();
    }
  }
}
