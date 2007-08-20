// $HeadURL$
// $Id$

// Copyright 2006 by the President and Fellows of Harvard College.

// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries;

import java.io.File;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import jxl.Cell;
import jxl.NumberCell;
import jxl.Sheet;
import jxl.Workbook;

import edu.harvard.med.screensaver.CommandLineApplication;
import edu.harvard.med.screensaver.model.libraries.CopyInfo;
import edu.harvard.med.screensaver.model.libraries.PlateType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.util.StringUtils;

import org.apache.commons.cli.OptionBuilder;
import org.apache.log4j.Logger;

/**
 * Generates a new library copy, limited to a specific set of plates. If copy or
 * individual copy plates already exist, skips creation attempt without error.
 * Plate numbers must be specified in an Excel workbook, in column 0 of first
 * worksheet (with no column header).
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
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
      app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("yyyy-mm-dd").withLongOpt("date-plated").create("d"));
      app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("name").withLongOpt("copy-name").create("c"));
      app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("plate type").withLongOpt("plate-type").withDescription(StringUtils.makeListString(Arrays.asList(PlateType.values()), "|")).create("p"));
      app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("microliter volume").withLongOpt("volume").create("v"));
      app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("xls file").withLongOpt("input-file").create("f"));
      if (!app.processOptions(true, true)) {
        System.exit(1);
      }
      BigDecimal volume = new BigDecimal(app.getCommandLineOptionValue("v")).setScale(Well.VOLUME_SCALE);
      String copyName = app.getCommandLineOptionValue("c");
      DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");
      Date datePlated = dateFormat.parse(app.getCommandLineOptionValue("d"));
      PlateType plateType = PlateType.valueOf(app.getCommandLineOptionValue("p").toUpperCase());
      Workbook workbook = Workbook.getWorkbook(new File(app.getCommandLineOptionValue("f")));
      Sheet sheet = workbook.getSheet(0);
      Cell[] column = sheet.getColumn(0);
      List<Integer> plateNumbers = new ArrayList<Integer>();
      for (Cell cell : column) {
        int plateNumber = (int) ((NumberCell) cell).getValue();
        plateNumbers.add(plateNumber);
      }
      log.info("creating RNAi cherry pick library copy " + copyName + 
               " with volume " + volume + 
               ", plate type " + plateType +
               ", plated on " + datePlated + 
               ", for plates: " + StringUtils.makeListString(plateNumbers, ", "));

      edu.harvard.med.screensaver.service.libraries.LibraryCopyGenerator libraryCopyGenerator = 
        (edu.harvard.med.screensaver.service.libraries.LibraryCopyGenerator) app.getSpringBean("libraryCopyGenerator");
      List<CopyInfo> plateCopiesCreated = libraryCopyGenerator.createPlateCopies(plateNumbers, 
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
  
  // instance data members
  
  // public constructors and methods

  // private methods

}

