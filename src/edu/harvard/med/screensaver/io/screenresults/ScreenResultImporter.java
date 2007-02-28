// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.screenresults;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import edu.harvard.med.screensaver.CommandLineApplication;
import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.io.workbook.ParseError;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;

import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

public class ScreenResultImporter
{
  // static members

  private static Logger log = Logger.getLogger(ScreenResultImporter.class);

  public static final int SHORT_OPTION = 0;
  public static final int LONG_OPTION = 1;

  public static final String[] INPUT_FILE_OPTION = { "f", "input-file" };
  public static final String[] SCREEN_OPTION = { "s", "screen" };
  public static final String[] IMPORT_OPTION = { "i", "import" };
  public static final String[] WELLS_OPTION = { "w", "wells" };

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
      
      final DAO dao = (DAO) app.getSpringBean("dao");

      final Integer wellsToPrint = app.getCommandLineOptionValue(WELLS_OPTION[SHORT_OPTION],
                                                                 Integer.class);
      final Screen finalScreen = screen;
      final ScreenResultParser finalScreenResultParser = screenResultParser;
      dao.doInTransaction(new DAOTransaction() {
        public void runTransaction()
        {
          dao.reattachEntity(finalScreen);
          ScreenResult screenResult = finalScreenResultParser.parse(finalScreen,
                                                                    inputFile);
          if (wellsToPrint != null) {
            new ScreenResultPrinter(screenResult).print(wellsToPrint);
          }
          else {
            new ScreenResultPrinter(screenResult).print();
          }

        }
      });
      screenResultParser.outputErrorsInAnnotatedWorkbooks(null,
                                                          ERROR_ANNOTATED_WORKBOOK_FILE_EXTENSION);
      if (screenResultParser.getErrors()
                            .size() > 0) {
        System.err.println("Errors encountered during parse:");
        for (ParseError error : screenResultParser.getErrors()) {
          System.err.println(error.toString());
        }
      }
      else {
        dao.persistEntity(screen);
        System.err.println("Success!");
      }
    }
    catch (IOException e) {
      String errorMsg = "I/O error: " + e.getMessage();
      log.error(errorMsg);
      System.err.println(errorMsg);
    }
    catch (ParseException e) {
      System.err.println("error parsing command line options: "
                         + e.getMessage());
    }
    catch (Exception e) {
      e.printStackTrace();
      System.err.println("application error: " + e.getMessage());
    }
  }

  private static Screen findScreenOrExit(CommandLineApplication app) throws ParseException
  {
    int screenNumber = Integer.parseInt(app.getCommandLineOptionValue(SCREEN_OPTION[SHORT_OPTION]));
    DAO dao = (DAO) app.getSpringBean("dao");
    Screen screen = dao.findEntityByProperty(Screen.class, 
                                              "hbnScreenNumber",
                                              screenNumber);
    if (screen == null) {
      System.err.println("screen " + screenNumber + " does not exist");
      System.exit(1);
    }
    if (screen.getScreenResult() != null) {
      System.err.println("screen " + screenNumber + " already has a screen result");
      System.exit(1);
    }
    return screen;
  }

  private static void cleanOutputDirectory(File parentFile)
  {
    if (!parentFile.isDirectory()) {
      log.warn("cannot clean the directory '" + parentFile + "' since it is not a directory");
      return;
    }
    log.info("cleaning directory " + parentFile);
    Iterator iterator = FileUtils.iterateFiles(parentFile,
                                               new String[] {ERROR_ANNOTATED_WORKBOOK_FILE_EXTENSION, ".out"},
                                               false);
    while (iterator.hasNext()) {
      File fileToDelete = (File) iterator.next();
      log.info("deleting previously generated outputfile '" + fileToDelete + "'");
      fileToDelete.delete();
    }
  }

  // instance data members

  // public constructors and methods

  // private methods

}

