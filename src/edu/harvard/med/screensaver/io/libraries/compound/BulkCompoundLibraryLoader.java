// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
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
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.harvard.med.screensaver.model.libraries.Library;

import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

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
  private static final Pattern _pattern = Pattern.compile("^(.*?)(_\\d+)?\\.sdf$");
  
  public static void main(String[] args)
  {
    ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] { 
      "spring-context.xml",
    });
    BulkCompoundLibraryLoader libraryLoader =
      (BulkCompoundLibraryLoader) context.getBean("bulkCompoundLibraryLoader");
    libraryLoader.bulkLoadLibraries();
  }


  // instance data members
  
  private DAO _dao;
  private SDFileCompoundLibraryContentsParser _parser;

  
  // public constructors and methods

  public BulkCompoundLibraryLoader(
    DAO dao,
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
      _dao.doInTransaction(new DAOTransaction() {
        public void runTransaction()
        {
//          log.info("preloading extant compounds");
//          List<Compound> compounds = _dao.findAllEntitiesWithType(Compound.class);
//          Map<String,Compound> compoundCache = buildCompoundCache(compounds);
//          log.info("preloaded " + compounds.size() + " compounds");
//          _parser.setCompoundCache(compoundCache);
          
          log.info("processing SD File: " + sdFile.getName());
          Library library = getLibraryForSDFile(sdFile);
          try {
            _parser.parseLibraryContents(library, sdFile, new FileInputStream(sdFile));
          }
          catch (FileNotFoundException e) {
            throw new InternalError("braindamage: " + e.getMessage());
          }
          if (_parser.getHasErrors()) {
            for (SDFileParseError error : _parser.getErrors()) {
              log.error(error.toString());
            }
            //throw new RuntimeException("SD File has parse errors: " + sdFile.getName());
          }
          _dao.persistEntity(library);
          
          log.info("finished processing SD File: " + sdFile.getName());
        }
      });
    }
  }

  private Library getLibraryForSDFile(File sdFile) {
    String filename = sdFile.getName();
    Matcher matcher = _pattern.matcher(filename);
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

