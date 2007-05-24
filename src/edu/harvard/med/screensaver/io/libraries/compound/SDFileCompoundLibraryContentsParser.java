// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries.compound;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.io.libraries.LibraryContentsParser;
import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;

import org.apache.log4j.Logger;


/**
 * Parses the contents (either partial or complete) of an compound library from
 * an SDFile into the entity model.
 * <p>
 * All wells for each plate of the library will be created, even if the wells
 * are not defined in the SDFile.
 * <p>
 * For performance reasons, uses a local cache for compounds. Doing so avoids
 * the issuing of database queries to check for the existence of compounds that
 * may already exist in the database. The compound cache is a Map that needs to
 * provided to this class by the instantiating object via setCompoundCache(),
 * and is optional. The memory requirement or compound cache can be high, and so
 * if sufficient memory is not available, the caller may opt to not provide a
 * compound cache, in which case this class will revert to making database
 * queries.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class SDFileCompoundLibraryContentsParser implements LibraryContentsParser
{
  
  // private static data
  
  private static final Logger log = Logger.getLogger(SDFileCompoundLibraryContentsParser.class);

  
  // private instance data
  
  private GenericEntityDAO _dao;
  private LibrariesDAO _librariesDao;
  private Library _library;
  private File _sdFile;
  private BufferedReader _sdFileReader;
  private SDFileParseErrorManager _errorManager;
  private Map<String,Compound> _compoundCache;

  
  // public constructor and instance methods
  
  /**
   * Construct a new <code>SDFileCompoundLibraryContentsParser</code> object.
   * @param genericEntityDao the data access object
   */
  public SDFileCompoundLibraryContentsParser(GenericEntityDAO dao,
                                             LibrariesDAO librariesDao)
  {
    _dao = dao;
    _librariesDao = librariesDao;
  }

  /**
   * Load library contents (either partial or complete) from an input
   * stream of an Excel spreadsheet into a library.
   * @param library the library to load contents of
   * @param file the name of the file that contains the library contents
   * @param stream the input stream to load library contents from
   * @return the library with the contents loaded
   */
  public Library parseLibraryContents(
    final Library library,
    final File file,
    final InputStream stream)
  {
    _dao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        initialize(library, file, stream);
        SDRecordParser sdRecordParser = new SDRecordParser(
          _sdFileReader,
          SDFileCompoundLibraryContentsParser.this);
        for (int i = 1; sdRecordParser.sdFileHasMoreRecords(); i++) {
          sdRecordParser.parseSDRecord();
          if ((i % 100) == 0) {
            log.info(
              "loaded " + i + " records into library " + library.getLibraryName());
          }
        }
      }
    });
    return _library;
  }
  
  public List<SDFileParseError> getErrors()
  {
    return _errorManager.getErrors();
  }
  
  public boolean getHasErrors()
  {
    return _errorManager != null && _errorManager.getHasErrors();
  }
  
  public void clearErrors()
  {
    _errorManager = null;
  }
  
  public void setCompoundCache(Map<String,Compound> compoundCache)
  {
    _compoundCache = compoundCache;
  }

  
  // package getters, for the SDRecordParser

  /**
   * Get the {@link GenericEntityDAO data access object}.
   * @return the data access object
   */
  GenericEntityDAO getDAO()
  {
    return _dao;
  }

  /**
   * Get the {@link Library}.
   * @return the library.
   */
  Library getLibrary()
  {
    return _library;
  }
  
  /**
   * Get the SDFile.
   * @return the SDFile
   */
  File getSdFile()
  {
    return _sdFile;
  }

  /**
   * Get the error manager.
   * @return the error manager
   */
  SDFileParseErrorManager getErrorManager()
  {
    return _errorManager;
  }

  /**
   * Get the specified well. Return null if no such well exists in the database.
   * 
   * @param key the key that identifies the well
   * @return the existing well. Return null if no such well exists
   */
  Well getWell(WellKey key)
  {
    return _librariesDao.findWell(key);
  }

  /**
   * Get an existing compound with the specified SMILES from a local cache (if
   * one exists), otherwise directly from the database. If a cache exists, the
   * database will never queried. Return null if no such compound exists.
   * 
   * @param smiles the SMILES string for the compound
   * @return the existing compound. Return null if no such compound exists.
   */
  Compound getExistingCompound(String smiles)
  {
    if (_compoundCache != null) {
      return _compoundCache.get(smiles);
    }
    else {
      return _dao.findEntityById(Compound.class, smiles);
    }
  }

  /**
   * Add the specified compound to the local cache (if it exists). Compounds
   * added to the cache will be accessible via {@link #getExistingCompound}.
   * 
   * @param compound the compound to be added to the local cache
   */
  public void cacheCompound(Compound compound)
  {
    if (_compoundCache != null) {
      _compoundCache.put(compound.getSmiles(), compound);
    }
  }
  
  
  // private instance methods
  
  /**
  /**
   * Initialize the instance variables.
   * @param library the library to load contents of
   * @param file the name of the file that contains the library contents
   * @param stream the input stream to load library contents from
   */
  private void initialize(Library library, File file, InputStream stream)
  {
    _library = library;
    _sdFile = file;
    _sdFileReader = new BufferedReader(new InputStreamReader(stream));
    _errorManager = new SDFileParseErrorManager();

    // load all of the library's wells in the Hibernate session, which avoids the need
    // to make database queries when checking for existence of wells
    _librariesDao.loadOrCreateWellsForLibrary(library);
  }
}
