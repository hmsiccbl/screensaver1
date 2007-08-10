//$HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
//$Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $

//Copyright 2006 by the President and Fellows of Harvard College.

//Screensaver is an open-source project developed by the ICCB-L and NSRB labs
//at Harvard Medical School. This software is distributed under the terms of
//the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries.rnai;

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
import edu.harvard.med.screensaver.service.libraries.LibraryCopyGenerator;
import edu.harvard.med.screensaver.util.StringUtils;

import org.apache.commons.cli.OptionBuilder;
import org.apache.log4j.Logger;

public class RNAiCherryPickCopyGenerator 
{
  // static members

  private static Logger log = Logger.getLogger(RNAiCherryPickCopyGenerator.class);

  @SuppressWarnings("static-access")
  public static void main(String[] args)
  {
    try {
      CommandLineApplication app = new CommandLineApplication(args);
      app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("yyyy-mm-dd").withLongOpt("date-plated").create("d"));
      app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("name").withLongOpt("copy-name").create("c"));
      app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("microliter volume").withLongOpt("volume").create("v"));
      app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("xls file").withLongOpt("input-file").create("f"));
      if (!app.processOptions(true, true)) {
        System.exit(1);
      }
      LibraryCopyGenerator libraryCopyGenerator = (LibraryCopyGenerator) app.getSpringBean("libraryCopyGenerator");
      Workbook workbook = Workbook.getWorkbook(new File(app.getCommandLineOptionValue("f")));
      Sheet sheet = workbook.getSheet(0);
      Cell[] column = sheet.getColumn(0);
      List<Integer> plateNumbers = new ArrayList<Integer>();
      for (Cell cell : column) {
        int plateNumber = (int) ((NumberCell) cell).getValue();
        plateNumbers.add(plateNumber);
      }
      BigDecimal volume = new BigDecimal(app.getCommandLineOptionValue("v")).setScale(Well.VOLUME_SCALE);
      String copyName = app.getCommandLineOptionValue("c");
      DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");
      Date datePlated = dateFormat.parse(app.getCommandLineOptionValue("d"));
      log.info("creating RNAi cherry pick library copy " + copyName + 
               " with volume " + volume + 
               " plated on " + datePlated + 
               " for plates: " + StringUtils.makeListString(plateNumbers, ", "));
      List<CopyInfo> plateCopiesCreated = libraryCopyGenerator.createPlateCopies(plateNumbers, 
                                                                                 Arrays.asList(copyName),
                                                                                 volume,
                                                                                 PlateType.EPPENDORF,
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

