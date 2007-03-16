// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries.rnai;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import edu.harvard.med.screensaver.CommandLineApplication;
import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.io.workbook.ParseError;
import edu.harvard.med.screensaver.model.libraries.Library;

/**
 * A bulk loader for the RNAi libraries.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class BulkRNAiLibraryLoader
{
  
  // static members

  private static final Logger log = Logger.getLogger(BulkRNAiLibraryLoader.class);
  private static final File _rnaiLibraryDir = new File("/usr/local/rnai-libraries");
  private static final Pattern _pattern = Pattern.compile(
    "^((Mitchison1)|(([^_]*)_([^_]*)_(Pools|Duplexes)))\\.xls$");
  
  public static void main(String[] args)
  {
    ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] { 
      CommandLineApplication.DEFAULT_SPRING_CONFIG,
    });
    BulkRNAiLibraryLoader libraryLoader =
      (BulkRNAiLibraryLoader) context.getBean("bulkRNAiLibraryLoader");
    libraryLoader.bulkLoadLibraries();
  }


  // instance data members
  
  private DAO _dao;
  private RNAiLibraryContentsParser _parser;
  
  
  // public constructors and methods

  public BulkRNAiLibraryLoader(DAO dao, RNAiLibraryContentsParser parser)
  {
    _dao = dao;
    _parser = parser;
  }

  /**
   * Database must be created and initialized before running this method.
   */
  public void bulkLoadLibraries()
  {
    
    File [] rnaiFiles = _rnaiLibraryDir.listFiles(new FilenameFilter() {
      public boolean accept(File dir, String filename) {
        return filename.endsWith(".xls");
      }
    });
    for (final File rnaiFile : rnaiFiles) {
      _dao.doInTransaction(new DAOTransaction() {
        public void runTransaction()
        {
          log.info("processing RNAi File: " + rnaiFile.getName());
          Library library = getLibraryForRNAiFile(rnaiFile);
          if (! library.getLibraryName().contains("Duplex")) {
            return;
          }
          try {
            _parser.parseLibraryContents(library, rnaiFile, new FileInputStream(rnaiFile));
          }
          catch (FileNotFoundException e) {
            throw new InternalError("braindamage: " + e.getMessage());
          }
          if (_parser.getHasErrors()) {
            for (ParseError error : _parser.getErrors()) {
              log.error(error.toString());
            }
          }
          _dao.persistEntity(library);
          log.info("finished processing RNAi File: " + rnaiFile.getName());
        }
      });
    }
  }

  private Library getLibraryForRNAiFile(File sdFile) {
    String filename = sdFile.getName();
    Matcher matcher = _pattern.matcher(filename);
    if (! matcher.matches()) {
      throw new RuntimeException("RNAi file didnt match pattern: " + filename);
    }
    // first try to match Mitchison1
    String libraryName = matcher.group(2);
    if (libraryName == null) {
      // if not Mitchison1, build Dharmacon library shortName
      libraryName = matcher.group(4) + " " + matcher.group(6);
    }
    Library library = _dao.findEntityByProperty(
      Library.class,
      "shortName",
      libraryName);
    if (library == null) {
      throw new RuntimeException("library not found with name: " + libraryName);
    }
    return library;
  }
}

