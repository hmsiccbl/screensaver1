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
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * A bulk loader for screen results. Originally written to load screen results for
 * the ICBG report. Some code here may be a bit specific to that purpose. For instance,
 * the file name pattern is probably a little over-specific, but matches all the
 * screen result files I am currently working with. This proggie should probably take
 * command line options for such things as the name of the directory where the screen
 * results are located, instead of using a hard-coded value. Also should consider
 * threading through some of the {@link ScreenResultParser} command line args.
 * 
 * TODO: figure out why logging stops after the first screen result.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class BulkScreenResultImporter
{
  
  // static members

  private static final Logger log = Logger.getLogger(BulkScreenResultImporter.class);
  private static final File _screenResultsDir = new File("/usr/local/screen-results");
  private static final Pattern _screenResultFilenamePattern = Pattern.compile("^(\\d+)_\\w+\\.xls$");
  
  public static void main(String[] args)
  {
    ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] { 
      "spring-context.xml",
    });
    BulkScreenResultImporter resultImporter =
      (BulkScreenResultImporter) context.getBean("bulkScreenResultImporter");
    resultImporter.bulkLoadLibraries();
  }
  

  // public constructors and methods

  /**
   * Database must be created and initialized before running this method.
   */
  public void bulkLoadLibraries()
  {    
    File [] screenResultFiles = _screenResultsDir.listFiles(new FilenameFilter() {
      public boolean accept(File dir, String filename) {
        return filename.endsWith(".xls") && ! filename.endsWith(".errors.xls");
      }
    });

    for (final File screenResultFile : screenResultFiles) {
      
      // code to hack for doing partial runs:
      if (screenResultFile.getName().compareTo("000_finalResults.xls") >= 0) {
        log.info("processing screen result file: " + screenResultFile.getName());
      }
      else {
        log.info("not processing screen result file: " + screenResultFile.getName());
        continue;
      }
      //if (true) continue;
      
      String filename = screenResultFile.getName();
      Matcher matcher = _screenResultFilenamePattern.matcher(filename);
      if (! matcher.matches()) {
        throw new RuntimeException("screen result file didnt match pattern: " + filename);
      }
      String screenResultNumber = matcher.group(1);
      try {
        ScreenResultParser.main(new String [] {
          "-s", screenResultNumber,
          "-f", screenResultFile.getAbsolutePath(),
          "-i",
        });
      }
      catch (FileNotFoundException e) {
        log.error("braindamage: " + e.getMessage());
      }
      log.info("finished processing screen result file: " + screenResultFile.getName());
    }
  }
}

