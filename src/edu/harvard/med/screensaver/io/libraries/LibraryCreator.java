// $HeadURL: $
// $Id: $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.dao.DataIntegrityViolationException;

import edu.harvard.med.screensaver.CommandLineApplication;
import edu.harvard.med.screensaver.io.ParseError;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.util.StringUtils;

/**
 * Command-line application that creates a new library and its wells and imports
 * its well contents into the database.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class LibraryCreator
{
  // static members

  private static Logger log = Logger.getLogger(LibraryCreator.class);

  @SuppressWarnings("static-access")
  public static void main(String[] args)
  {
    final CommandLineApplication app = new CommandLineApplication(args);
    try {
      DateTimeFormatter dateFormat = DateTimeFormat.forPattern(CommandLineApplication.DEFAULT_DATE_PATTERN);
      app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("library name").withLongOpt("name").withDescription("full, official name for the library").create("n"));
      app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("short name").withLongOpt("short-name").withDescription("a short name for identifying the library").create("s"));
      app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("library type").withLongOpt("library-type").withDescription(StringUtils.makeListString(Arrays.asList(LibraryType.values()), ", ")).create("lt"));
      app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("screen type").withLongOpt("screen-type").withDescription(StringUtils.makeListString(Arrays.asList(ScreenType.values()), ", ")).create("st"));
      app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("#").withLongOpt("start-plate").create("sp"));
      app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("#").withLongOpt("end-plate").create("ep"));
      app.addCommandLineOption(OptionBuilder.hasArg().withArgName("name").withLongOpt("vendor").create("v"));
      app.addCommandLineOption(OptionBuilder.hasArg().withArgName("text").withLongOpt("description").create("d"));
      app.addCommandLineOption(OptionBuilder.hasArg().withArgName(CommandLineApplication.DEFAULT_DATE_PATTERN).withLongOpt("date-received").create("dr"));
      app.addCommandLineOption(OptionBuilder.hasArg().withArgName(CommandLineApplication.DEFAULT_DATE_PATTERN).withLongOpt("date-screenable").create("ds"));
      app.addCommandLineOption(OptionBuilder.hasArg().withArgName("file").withLongOpt("contents-file").create("f"));
      app.addCommandLineOption(OptionBuilder.hasArg().withArgName("Number plate rows").withLongOpt("plate-rows").create("pr"));
      app.addCommandLineOption(OptionBuilder.hasArg().withArgName("Number plate columns").withLongOpt("plate-columns").create("pc"));

      if (!app.processOptions(true, true)) {
        System.exit(1);
      }

      String libraryName = app.getCommandLineOptionValue("n");
      String shortName = app.getCommandLineOptionValue("s");
      LibraryType libraryType = app.getCommandLineOptionEnumValue("lt", LibraryType.class);
      ScreenType screenType = app.getCommandLineOptionEnumValue("st", ScreenType.class);
      int startPlate = app.getCommandLineOptionValue("sp", Integer.class);
      int endPlate = app.getCommandLineOptionValue("ep", Integer.class);
      String vendor = app.isCommandLineFlagSet("v") ? app.getCommandLineOptionValue("v") : null;
      String description = app.isCommandLineFlagSet("d") ? app.getCommandLineOptionValue("d") : null;
      LocalDate dateReceived = app.isCommandLineFlagSet("dr") ? app.getCommandLineOptionValue("dr", dateFormat).toLocalDate() : null;
      LocalDate dateScreenable = app.isCommandLineFlagSet("ds") ? app.getCommandLineOptionValue("ds", dateFormat).toLocalDate() : null;
      final File libraryContentsFile = app.isCommandLineFlagSet("f") ?  app.getCommandLineOptionValue("f", File.class) : null;
      int plateRows =  app.isCommandLineFlagSet("pr") ? app.getCommandLineOptionValue("pr",Integer.class) : Well.PLATE_ROWS_DEFAULT;
      int plateColumns =  app.isCommandLineFlagSet("pc") ? app.getCommandLineOptionValue("pc",Integer.class) : Well.PLATE_COLUMNS_DEFAULT;

      final Library library = new Library(libraryName, shortName, screenType, libraryType, startPlate, endPlate,plateRows,plateColumns);
      library.setDescription(description);
      library.setVendor(vendor);
      library.setDateReceived(dateReceived);
      library.setDateScreenable(dateScreenable);

      edu.harvard.med.screensaver.service.libraries.LibraryCreator libraryCreator = (edu.harvard.med.screensaver.service.libraries.LibraryCreator) app.getSpringBean("libraryCreator");

      InputStream contentsIn = app.isCommandLineFlagSet("f") ? new FileInputStream(libraryContentsFile) : null;
      libraryCreator.createLibrary(library, contentsIn);
      log.info("library succesfully added to database");
    }
    catch (ParseException e) {
      String msg = "bad command line argument " + app.getLastAccessOption().getOpt() + e.getMessage();
      System.out.println(msg);
      log.error(msg);
      System.exit(1);
    }
    catch (DataIntegrityViolationException e) {
      String msg = "data integrity error: " + e.getMessage();
      System.out.println(msg);
      log.error(msg);
      System.exit(1);
    }
    catch (ParseLibraryContentsException e) {
      log.error("library contents file contained errors:");
      for (ParseError error : e.getErrors()) {
        log.error(error.getErrorMessage());
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      log.error(e.toString());
      System.err.println("error: " + e.getMessage());
      System.exit(1);
    }
  }
}
