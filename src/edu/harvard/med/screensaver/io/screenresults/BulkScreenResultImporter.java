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
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.harvard.med.screensaver.CommandLineApplication;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.DAOTransactionRollbackException;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.ScreenResultsDAO;
import edu.harvard.med.screensaver.io.workbook.ParseError;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.util.StringUtils;

import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.springframework.orm.hibernate3.HibernateTemplate;

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
  protected static final int MAX_ERRORS_TO_LOG = 4;
  
  private static final int SHORT_OPTION = ScreenResultImporter.SHORT_OPTION;
  private static final int LONG_OPTION = ScreenResultImporter.LONG_OPTION;
  private static final String[] INPUT_DIRECTORY_OPTION = new String[] { "d", "input-directory" };
  private static final String[] FROM_FILE_OPTION = new String[] { "f", "from-file" };
  private static final String[] TO_FILE_OPTION = new String[] { "t", "to-file" };
  private static final String[] IMPORT_OPTION = ScreenResultImporter.IMPORT_OPTION;
  
  
  @SuppressWarnings("static-access")
  public static void main(String[] args) throws ParseException
  {
    CommandLineApplication app  = new CommandLineApplication(args);
    
    app.addCommandLineOption(OptionBuilder.withDescription("Directory containing screen result workbook files.  ")
                                                           .withLongOpt(INPUT_DIRECTORY_OPTION[LONG_OPTION])
                                                           .hasArg()
                                                           .create(INPUT_DIRECTORY_OPTION[SHORT_OPTION]),
                                                           _screenResultsDir);
    app.addCommandLineOption(OptionBuilder.withDescription("process files that are lexigraphically greater than or equal to the specified file; file name should be relative to input directory")
                             .hasArg()
                             .isRequired(false)
                             .withLongOpt(FROM_FILE_OPTION[LONG_OPTION])
                             .create(FROM_FILE_OPTION[SHORT_OPTION]));
    app.addCommandLineOption(OptionBuilder.withDescription("process files that are lexigraphically less than the specified file (specified file is not processed); file name should be relative to input directory")
                             .hasArg()
                             .isRequired(false)
                             .withLongOpt(TO_FILE_OPTION[LONG_OPTION])
                             .create(TO_FILE_OPTION[SHORT_OPTION]));
    app.addCommandLineOption(OptionBuilder
                             .withDescription("Import screen result into database if parsing is successful.  " +
                                              "(By default, the parser only validates the input and then exits.)")
                             .hasArg(false)
                             .withLongOpt(IMPORT_OPTION[LONG_OPTION])
                             .create(IMPORT_OPTION[SHORT_OPTION]));
    
    try {
      if (!app.processOptions(/* acceptDatabaseOptions= */true, 
                              /* showHelpOnError= */true)) {
        System.exit(1);
      }
      File inputDir = app.getCommandLineOptionValue(INPUT_DIRECTORY_OPTION[SHORT_OPTION], File.class);
      boolean importFlag = app.isCommandLineFlagSet(IMPORT_OPTION[SHORT_OPTION]);
      
      File fromFile = null;
      String fromFileName = app.getCommandLineOptionValue(FROM_FILE_OPTION[SHORT_OPTION]);
      if (fromFileName != null && fromFileName.length() > 0) {
        fromFile = new File(inputDir, fromFileName);
        if (!fromFile.exists()) {
          log.error("no such \"from\" file: " + fromFile);
          System.exit(1);
        }
      }
      File toFile = null;
      String toFileName = app.getCommandLineOptionValue(TO_FILE_OPTION[SHORT_OPTION]);
      if (toFileName != null && toFileName.length() > 0) {
        toFile = new File(inputDir, toFileName);
        if (!fromFile.exists()) {
          log.error("no such \"to\" file: " + toFile);
          System.exit(1);
        }
      }
      
      BulkScreenResultImporter resultImporter =
        (BulkScreenResultImporter) app.getSpringBean("bulkScreenResultImporter");
      resultImporter.bulkLoadLibraries(inputDir, importFlag, fromFile, toFile);
    }
    catch (ParseException e) {
      System.err.println("error parsing command line options: "
                         + e.getMessage());
      System.exit(1);
    }
    catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
    
  }
  
  
  // instance data
  
  private ScreenResultParser _parser;
  private GenericEntityDAO _dao;
  private ScreenResultsDAO _screenResultsDao;
  
  private HibernateTemplate _hibernateTemplate;
  

  // public constructors and methods
  
  public BulkScreenResultImporter(ScreenResultParser parser, 
                                  GenericEntityDAO dao, 
                                  ScreenResultsDAO screenResultsDao, 
                                  HibernateTemplate hibernateTemplate)
  {
    _parser = parser;
    _dao = dao;
    _screenResultsDao = screenResultsDao;
    _hibernateTemplate = hibernateTemplate;
  }

  /**
   * Database must be created and initialized before running this method.
   * @param importFlag 
   * @param inputDir 
   */
  public void bulkLoadLibraries(File inputDir, final boolean importFlag, File fromFile, File toFile)
  {    
    List<Integer> succeededScreenNumbers = new ArrayList<Integer>();
    List<Integer> failedImports = new ArrayList<Integer>();

    File [] screenResultFiles = inputDir.listFiles(new FilenameFilter() {
      public boolean accept(File dir, String filename) {
        return filename.endsWith(".xls") && ! filename.endsWith(".errors.xls");
      }
    });
    SortedSet<File> sortedScreenResultFiles = new TreeSet<File>(Arrays.asList(screenResultFiles));
    if (fromFile != null) {
      sortedScreenResultFiles = sortedScreenResultFiles.tailSet(fromFile);
    }
    if (toFile != null) {
      sortedScreenResultFiles = sortedScreenResultFiles.headSet(toFile);
    }
    
    for (final File screenResultFile : sortedScreenResultFiles) {
      String filename = screenResultFile.getName();
      Matcher matcher = _screenResultFilenamePattern.matcher(filename);
      if (! matcher.matches()) {
        throw new RuntimeException("screen result file didnt match pattern: " + filename);
      }
      final int screenNumber = Integer.parseInt(matcher.group(1));
      try {

        if (importFlag) {
          // delete extant screen result; do this in separate txn, to minimize memory usage
          _dao.doInTransaction(new DAOTransaction() 
          {
            public void runTransaction() 
            {
              final Screen screen = _dao.findEntityByProperty(Screen.class, "hbnScreenNumber", screenNumber);
              if (screen == null) {
                throw new DAOTransactionRollbackException("no such screen " + screenNumber);
              }
              if (screen.getScreenResult() != null) {
                log.info("deleting existing screen result for screen " + screenNumber);
                _screenResultsDao.deleteScreenResult(screen.getScreenResult());
              }
            };
          });
        }
        
        // parse & (maybe) import
        _dao.doInTransaction(new DAOTransaction() 
        {
          public void runTransaction() 
          {
            final Screen screen = _dao.findEntityByProperty(Screen.class, "hbnScreenNumber", screenNumber);
            if (screen == null) {
              throw new DAOTransactionRollbackException("no such screen " + screenNumber);
            }
            _parser.parse(screen, screenResultFile);
            if (_parser.getHasErrors()) {
              int nErrors = 0;
              for (ParseError error : _parser.getErrors()) {
                log.error("parse error " + (++nErrors) + ": " + error);
                int totalErrors = _parser.getErrors().size();
                if (nErrors == MAX_ERRORS_TO_LOG && nErrors < totalErrors) {
                  log.error("additional errors not shown (" + totalErrors  + " total errors)");
                  break;
                }
              }
              String failureMessage = "screen result file " + screenResultFile + " for screen " + screenNumber + " had errors";
              throw new DAOTransactionRollbackException(failureMessage);
            }
            if (!importFlag) {
              throw new SkipCommitException();
            }
          }
        });
        succeededScreenNumbers.add(screenNumber);
        log.info("successfully imported screen result file: " + screenResultFile.getName());
      }
      catch (SkipCommitException e) {
        succeededScreenNumbers.add(screenNumber);
        log.info("successfully parsed screen result file: " + screenResultFile.getName());
      }
      catch (DAOTransactionRollbackException e) {
        failedImports.add(screenNumber);
        log.error("failed to " + (importFlag ? "importe" : "parse") + 
                  "screen result for screen " + screenNumber + " : " + e.getMessage());
      }
    }
    log.info(succeededScreenNumbers.size() + " of " + sortedScreenResultFiles.size() + 
             " screen result(s) were successfully " + (importFlag ? "imported" : "parsed") + 
             "; skipped " + 
             (screenResultFiles.length - sortedScreenResultFiles.size())); 
    log.info("succeeded: " + StringUtils.makeListString(succeededScreenNumbers, ", "));
    if (failedImports.size() > 0) {
      log.info("failed: " + StringUtils.makeListString(failedImports, ", "));
    }
  }
}

