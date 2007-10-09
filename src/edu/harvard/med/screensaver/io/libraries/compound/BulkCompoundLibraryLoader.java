// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries.compound;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import edu.harvard.med.screensaver.CommandLineApplication;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.harvard.med.screensaver.model.libraries.Library;

/**
 * A bulk loader for the compound libraries that we have SD files for.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class BulkCompoundLibraryLoader
{
  
  // static members

  private static final Logger log = Logger.getLogger(BulkCompoundLibraryLoader.class);
  private static final File _compoundLibraryDir = new File("/usr/local/compound-libraries");
  private static final Pattern _sdFilenamePattern = Pattern.compile("^(.*?)(_\\d+)?\\.sdf$");
  
  public static void main(String[] args)
  {
    ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] { 
      CommandLineApplication.DEFAULT_SPRING_CONFIG,
    });
    BulkCompoundLibraryLoader libraryLoader =
      (BulkCompoundLibraryLoader) context.getBean("bulkCompoundLibraryLoader");
    libraryLoader.bulkLoadLibraries();
  }


  // instance data members
  
  private GenericEntityDAO _dao;
  private SDFileCompoundLibraryContentsParser _parser;

  
  // public constructors and methods

  public BulkCompoundLibraryLoader(
    GenericEntityDAO dao,
    SDFileCompoundLibraryContentsParser parser)
  {
    _dao = dao;
    _parser = parser;
  }

  /**
   * Database must be created and initialized before running this method.
   */
  public void bulkLoadLibraries()
  {
    
    File [] sdFiles = _compoundLibraryDir.listFiles(new FilenameFilter() {
      public boolean accept(File dir, String filename) {
        return filename.endsWith(".sdf");
      }
    });

    for (final File sdFile : sdFiles) {

      // tweak the compareTo for partial runs. uncomment the "if (true) continue;"
      // to see what would run without actually running it
      if (//sdFile.getName().equals("CMLD1.sdf") ||
        sdFile.getName().compareTo("0.sdf") >= 0) {
        log.info("processing SD File: " + sdFile.getName());
      }
      else {
        log.info("not processing SD File: " + sdFile.getName());
        continue;
      }
      //if (true) continue;

      _dao.doInTransaction(new DAOTransaction() {
        public void runTransaction()
        {
          Library library = getLibraryForSDFile(sdFile);
          try {
            _parser.parseLibraryContents(library, sdFile, new FileInputStream(sdFile));
          }
          catch (FileNotFoundException e) {
            throw new InternalError("braindamage: " + e.getMessage());
          }
          if (_parser.getHasErrors()) {
            for (FileParseError error : _parser.getErrors()) {
              log.error(error.toString());
            }
          }
          _dao.saveOrUpdateEntity(library);
        }
      });
      log.info("finished processing SD File: " + sdFile.getName());
    }
  }

  private Library getLibraryForSDFile(File sdFile) {
    String filename = sdFile.getName();
    Matcher matcher = _sdFilenamePattern.matcher(filename);
    if (! matcher.matches()) {
      throw new RuntimeException("sd file didnt match pattern: " + filename);
    }
    String libraryName = matcher.group(1);
    Library library = _dao.findEntityByProperty(
      Library.class,
      "libraryName",
      libraryName);
    if (library == null) {
      library = _dao.findEntityByProperty(
        Library.class,
        "shortName",
        libraryName);
    }
    if (library == null) {
      throw new RuntimeException("library not found with name: " + libraryName);
    }
    return library;
  }
  
  private Map<String,Compound> buildCompoundCache(Collection<Compound> compounds)
  {
    Map<String,Compound> cache = new HashMap<String,Compound>();
    for (Compound compound : compounds) {
      cache.put(compound.getSmiles(), compound);
    }
    return cache;
  }
}

