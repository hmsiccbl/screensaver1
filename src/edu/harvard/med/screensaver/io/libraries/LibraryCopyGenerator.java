// $HeadURL$
// $Id$

// Copyright 2006 by the President and Fellows of Harvard College.

// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jxl.Cell;
import jxl.CellType;
import jxl.NumberCell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import edu.harvard.med.screensaver.CommandLineApplication;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.Volume.Units;
import edu.harvard.med.screensaver.model.libraries.CopyInfo;
import edu.harvard.med.screensaver.model.libraries.PlateType;
import edu.harvard.med.screensaver.util.StringUtils;

import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Creates a set of new library copy plates for a given copy name, initializing
 * each plate to a specified initial volume. If copy or individual copy plates
 * already exist, skips creation attempt without error unless plate type and/or
 * initial volume differ from existing values. Plate numbers may be specified in
 * an Excel workbook, as list of numbers in the first column of the first
 * worksheet (with no column header), or via the 'plate-numbers' command-line
 * argument, which accepts a list of plate numbers and/or plate ranges, where
 * each number/range is separated by whitespace.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class LibraryCopyGenerator
{
  // static members

  private static Logger log = Logger.getLogger(LibraryCopyGenerator.class);


  @SuppressWarnings("static-access")
  public static void main(String[] args)
  {
    try {
      CommandLineApplication app = new CommandLineApplication(args);
      app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName(CommandLineApplication.DEFAULT_DATE_PATTERN).withLongOpt("date-plated").create("d"));
      app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("name").withLongOpt("copy-name").create("c"));
      app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("plate type").withLongOpt("plate-type").withDescription(StringUtils.makeListString(Arrays.asList(PlateType.values()), "|")).create("p"));
      app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("microliter volume").withLongOpt("volume").create("v"));
      app.addCommandLineOption(OptionBuilder.hasArgs().isRequired(false).withArgName("plate numbers").withLongOpt("plate-numbers").withDescription("The space-separated list of plate numbers or plate ranges.  Plate ranges are specified as #####-#####").create("n"));
      app.addCommandLineOption(OptionBuilder.hasArg().isRequired(false).withArgName("xls file").withLongOpt("input-file").withDescription("Excel workbook containing plate numbers in the first column of the first worksheet").create("f"));
      if (!app.processOptions(true, true)) {
        System.exit(1);
      }
      Volume volume = new Volume(app.getCommandLineOptionValue("v"), Units.MICROLITERS);
      String copyName = app.getCommandLineOptionValue("c");
      DateTimeFormatter dateFormat = DateTimeFormat.forPattern(CommandLineApplication.DEFAULT_DATE_PATTERN);
      LocalDate datePlated = app.getCommandLineOptionValue("d", dateFormat).toLocalDate();
      PlateType plateType = PlateType.valueOf(app.getCommandLineOptionValue("p").toUpperCase());
      Set<Integer> plateNumbers = readPlateNumbers(app);
      log.info("creating RNAi cherry pick library copy " + copyName +
               " with volume " + volume +
               ", plate type " + plateType +
               ", plated on " + datePlated +
               ", for plates: " + StringUtils.makeListString(plateNumbers, ", "));

      edu.harvard.med.screensaver.service.libraries.LibraryCopyGenerator libraryCopyGenerator =
        (edu.harvard.med.screensaver.service.libraries.LibraryCopyGenerator) app.getSpringBean("libraryCopyGenerator");
      List<CopyInfo> plateCopiesCreated = libraryCopyGenerator.createPlateCopies(new ArrayList<Integer>(plateNumbers),
                                                                                 Arrays.asList(copyName),
                                                                                 volume,
                                                                                 plateType,
                                                                                 datePlated);
      log.info("created " + plateCopiesCreated.size() + " plate copies: " + StringUtils.makeListString(plateCopiesCreated, ", "));
    }
    catch (Exception e) {
      e.printStackTrace();
      log.error(e.toString());
      System.err.println("error: " + e.toString());
      System.exit(1);
    }
  }

  private static Set<Integer> readPlateNumbers(CommandLineApplication app)
    throws IOException,
    BiffException,
    ParseException
  {
    if (app.isCommandLineFlagSet("f")) {
      return readPlateNumbersFromWorkbook(app);
    }
    else if (app.isCommandLineFlagSet("n")) {
      return readPlateNumbersFromCommandLineArg(app);
    }
    else {
      throw new IllegalArgumentException("you must specify either the 'plate-numbers' or 'input-file' option");
    }
  }

  private static Set<Integer> readPlateNumbersFromCommandLineArg(CommandLineApplication app)
    throws ParseException
  {
    Set<Integer> plateNumbers = new TreeSet<Integer>();
    Pattern pattern = Pattern.compile("(\\d+)(-(\\d+))?");
    List<String> plateNumbersOrRanges = app.getCommandLineOptionValues("n");
    for (String plateNumberOrRange : plateNumbersOrRanges) {
      Matcher matcher = pattern.matcher(plateNumberOrRange);
      if (matcher.matches()) {
        if (matcher.group(3) != null) {
          int startPlateNumber = Integer.parseInt(matcher.group(1));
          int endPlateNumber = Integer.parseInt(matcher.group(3));
          for (int n = startPlateNumber; n <= endPlateNumber; ++n) {
            plateNumbers.add(n);
          }
        }
        else if (matcher.group(1) != null) {
          plateNumbers.add(Integer.parseInt(matcher.group(1)));
        }
        else {
          throw new IllegalArgumentException("invalid plate number or plate range: " + plateNumberOrRange);
        }
      }
    }
    return plateNumbers;
  }

  private static Set<Integer> readPlateNumbersFromWorkbook(CommandLineApplication app) throws BiffException, IOException, ParseException
  {
    Set<Integer> plateNumbers = new TreeSet<Integer>();
    Workbook workbook = Workbook.getWorkbook(new File(app.getCommandLineOptionValue("f")));
    Sheet sheet = workbook.getSheet(0);
    Cell[] column = sheet.getColumn(0);
    for (Cell cell : column) {
      if (cell.getType() != CellType.EMPTY) {
        int plateNumber = (int) ((NumberCell) cell).getValue();
        plateNumbers.add(plateNumber);
      }
    }
    return plateNumbers;
  }
}

