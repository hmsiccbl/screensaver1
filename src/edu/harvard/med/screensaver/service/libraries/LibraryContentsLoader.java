// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.service.libraries;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.io.ParseError;
import edu.harvard.med.screensaver.io.ParseErrorsException;
import edu.harvard.med.screensaver.io.ParseException;
import edu.harvard.med.screensaver.io.libraries.LibraryContentsParser;
import edu.harvard.med.screensaver.io.libraries.rnai.RNAiLibraryContentsParser;
import edu.harvard.med.screensaver.io.libraries.smallmolecule.NaturalProductsLibraryContentsParser;
import edu.harvard.med.screensaver.io.libraries.smallmolecule.SmallMoleculeLibraryContentsParser;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryContentsVersion;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.util.DevelopmentException;
import edu.harvard.med.screensaver.util.Pair;

/**
 * Service that creates a new library and its wells and imports its well
 * contents into the database.
 */
public class LibraryContentsLoader
{
  /**
   * The number of record to process between flushing to the database, and
   * clearing the Hibernate session cache. Larger numbers (may) increase
   * performance, while smaller values reduce memory requirements
   */
  private static final int FLUSH_BATCH_SIZE = 384;

  private static Logger log = Logger.getLogger(LibraryContentsLoader.class);

  private GenericEntityDAO _dao;
  private LibrariesDAO _librariesDao;
  private LibraryContentsVersionManager _libraryContentsVersionManager;
  
  /**
   * @motivation for CGLIB2
   */
  protected LibraryContentsLoader() {}

  public LibraryContentsLoader(GenericEntityDAO dao,
                               LibrariesDAO librariesDao,
                               LibraryContentsVersionManager libraryContentsVersionManager)
  {
    _dao = dao;
    _librariesDao = librariesDao;
    _libraryContentsVersionManager = libraryContentsVersionManager;
  }

  @Transactional(rollbackFor = { ParseErrorsException.class, IOException.class })
  public LibraryContentsVersion loadLibraryContents(Library library, 
                                                    AdministratorUser performedBy,
                                                    String loadingComments,
                                                    InputStream stream) 
    throws ParseErrorsException, IOException
  {
    LibraryContentsVersion lcv = _libraryContentsVersionManager.createNewContentsVersion(library, performedBy, loadingComments);
    // flush & clear the Hibernate session to ensure that no library child entities
    // become referenced by the library or lcv, thereby allowing GC to occur on those entities
    _dao.flush();
    _dao.clear();
    loadLibraryContents(lcv, stream);
    return lcv;
  }
    
  private void loadLibraryContents(LibraryContentsVersion lcv, 
                                  InputStream stream) 
    throws ParseErrorsException, IOException
  {
    Library library = lcv.getLibrary();
    LibraryContentsParser parser;
    if (library.getScreenType().equals(ScreenType.RNAI)) {
      parser = new RNAiLibraryContentsParser(_dao, stream, library);
    }
    else if (library.getScreenType().equals(ScreenType.SMALL_MOLECULE)) 
    {
      if (library.getLibraryType().equals(LibraryType.NATURAL_PRODUCTS)) {
        parser = new NaturalProductsLibraryContentsParser(_dao, stream, library);
      } 
      else {
        parser = new SmallMoleculeLibraryContentsParser(_dao, stream, library);
      }
    } 
    else {
      throw new DevelopmentException("unhandled Library Screen Type: " + library.getScreenType() );
    }
    int nProcessed = 0;
    List<ParseError> errors = Lists.newArrayList();
    Pair<Well,? extends Reagent> result;
    while (true) {
      try {
        result = parser.parseNext();
        if (result == null) {
          break;
        }
        // due to memory optimizations, the relationship from Well to Reagent
        // may not be set, which would prevent Reagent from being persisted (no
        // cascading)
        if (result.getSecond() != null) { 
          _dao.saveOrUpdateEntity(result.getSecond());
        }
        if (++nProcessed % FLUSH_BATCH_SIZE == 0) {
          // allow GC to occur
          _dao.flush();
          _dao.clear();
          log.info(nProcessed + " record(s) processed with " + errors.size()  + " failure(s)...");
        }
      }
      catch (ParseException e) {
        ++nProcessed;
        errors.add(e.getError());
      }
    }
    log.info(nProcessed + " record(s) processed with " + errors.size()  + " failure(s)");
    if (errors.size() > 0) {
      throw new ParseErrorsException(errors);
    }
  }  
}
