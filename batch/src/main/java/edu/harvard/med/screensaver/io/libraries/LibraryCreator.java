// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries;

import java.util.Arrays;

import org.apache.commons.cli.OptionBuilder;
import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.io.CommandLineApplication;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.PlateSize;
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
  private static Logger log = Logger.getLogger(LibraryCreator.class);

  @SuppressWarnings("static-access")
  public static void main(String[] args)
  {
    CommandLineApplication app = new CommandLineApplication(args);
    try {
      DateTimeFormatter dateFormat = 
        DateTimeFormat.forPattern(CommandLineApplication.DEFAULT_DATE_PATTERN);
      
      app.addCommandLineOption(OptionBuilder.hasArg().isRequired()
                               .withArgName("library name").withLongOpt("name")
                               .withDescription("full, official name for the library").create("n"));
      app.addCommandLineOption(OptionBuilder.hasArg().isRequired()
                               .withArgName("short name").withLongOpt("short-name")
                               .withDescription("a short name for identifying the library").create("s"));
      app.addCommandLineOption(OptionBuilder.hasArg().isRequired()
                               .withArgName("library type").withLongOpt("library-type")
                               .withDescription(StringUtils.makeListString(Lists.transform(Lists.newArrayList(LibraryType.values()), new Function<LibraryType,String>() {
                                 @Override
                                 public String apply(LibraryType arg0)
                                {
                                  return arg0.name();
                                }
                               }), ", "))
                               .create("lt"));
      app.addCommandLineOption(OptionBuilder.hasArg().isRequired()
                               .withArgName("screen type").withLongOpt("screen-type")
                               .withDescription(StringUtils.makeListString(Lists.transform(Lists.newArrayList(ScreenType.values()), new Function<ScreenType,String>() {
                                 @Override
                                 public String apply(ScreenType arg0)
                                {
                                  return arg0.name();
                                }
                               }), ", "))
                               .create("st"));
      app.addCommandLineOption(OptionBuilder.hasArg(false)
                               .withLongOpt("is-pool")
                               .withDescription("well contents are pools of reagents (only valid when library-type=RNAI)")
                               .create("ip"));
      app.addCommandLineOption(
          OptionBuilder.hasArg().isRequired()
          .withArgName("#").withLongOpt("start-plate").create("sp"));
      app.addCommandLineOption(
          OptionBuilder.hasArg().isRequired()
          .withArgName("#").withLongOpt("end-plate").create("ep"));
      
      app.addCommandLineOption(
          OptionBuilder.hasArg()
            .withArgName("name").withLongOpt("provider").create("lp"));
      app.addCommandLineOption(
          OptionBuilder.hasArg()
          .withArgName("text").withLongOpt("description").create("d"));
      app.addCommandLineOption(
          OptionBuilder.hasArg()
          .withArgName(CommandLineApplication.DEFAULT_DATE_PATTERN).withLongOpt("date-received")
          .create("dr"));
      app.addCommandLineOption(
          OptionBuilder.hasArg()
          .withArgName(CommandLineApplication.DEFAULT_DATE_PATTERN).withLongOpt("date-screenable")
          .create("ds"));
      app.addCommandLineOption(
          OptionBuilder.hasArg()
          .withArgName("plate size")
          .withDescription(StringUtils.makeListString(Lists.transform(Lists.newArrayList(PlateSize.values()), new Function<PlateSize,String>() {
                                 @Override
                                 public String apply(PlateSize arg0)
                                {
                                  return arg0.name();
                                }
                               }), ", "))
            .withLongOpt("plate-size").create("ps"));

      app.processOptions(true, true);

      String libraryName = app.getCommandLineOptionValue("n");
      String shortName = app.getCommandLineOptionValue("s");
      LibraryType libraryType = app.getCommandLineOptionEnumValue("lt", LibraryType.class);
      boolean isPool = app.isCommandLineFlagSet("ip");
      ScreenType screenType = app.getCommandLineOptionEnumValue("st", ScreenType.class);
      int startPlate = app.getCommandLineOptionValue("sp", Integer.class);
      int endPlate = app.getCommandLineOptionValue("ep", Integer.class);
      String vendor = 
        app.isCommandLineFlagSet("lp") ? app.getCommandLineOptionValue("lp") : null;
      String description = 
        app.isCommandLineFlagSet("d") ? app.getCommandLineOptionValue("d") : null;
      LocalDate dateReceived = 
        app.isCommandLineFlagSet("dr") ? 
            app.getCommandLineOptionValue("dr", dateFormat).toLocalDate() : null;
      LocalDate dateScreenable = 
        app.isCommandLineFlagSet("ds") ? 
            app.getCommandLineOptionValue("ds", dateFormat).toLocalDate() : null;
      PlateSize plateSize =  
        app.isCommandLineFlagSet("ps") ? 
            app.getCommandLineOptionEnumValue("ps", PlateSize.class) : ScreensaverConstants.DEFAULT_PLATE_SIZE;

      Library library = 
        new Library(app.findAdministratorUser(),
                    libraryName,
                    shortName,
                    screenType,
                    libraryType,
                    startPlate,
                    endPlate,
                    plateSize);
      library.setPool(isPool);
      library.setDescription(description);
      library.setProvider(vendor);
      library.setDateReceived(dateReceived);
      library.setDateScreenable(dateScreenable);

      edu.harvard.med.screensaver.service.libraries.LibraryCreator libraryCreator 
          = (edu.harvard.med.screensaver.service.libraries.LibraryCreator) 
              app.getSpringBean("libraryCreator");
      libraryCreator.createLibrary(library);
      log.info("library succesfully added to database");
    }
    catch (Exception e) {
      e.printStackTrace();
      log.error(e.toString());
      System.err.println("error: " + e.getMessage());
      System.exit(1);
    }
  }
}
