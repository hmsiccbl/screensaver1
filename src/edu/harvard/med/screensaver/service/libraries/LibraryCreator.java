// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.service.libraries;

import java.io.InputStream;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.io.libraries.LibraryContentsParser;
import edu.harvard.med.screensaver.io.libraries.compound.SDFileCompoundLibraryContentsParser;
import edu.harvard.med.screensaver.io.libraries.rnai.RNAiLibraryContentsParser;
import edu.harvard.med.screensaver.model.DataModelViolationException;
import edu.harvard.med.screensaver.model.DuplicateEntityException;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.screens.ScreenType;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service that creates a new library and its wells and imports its well
 * contents into the database.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class LibraryCreator
{
  // static members

  private static Logger log = Logger.getLogger(LibraryCreator.class);


  // instance data members

  private GenericEntityDAO _dao;
  private LibrariesDAO _librariesDao;
  private RNAiLibraryContentsParser _rnaiLibraryContentsParser;
  private SDFileCompoundLibraryContentsParser _compoundLibraryContentsParser;

  // public constructors and methods

  public LibraryCreator(GenericEntityDAO dao,
                        LibrariesDAO librariesDao,
                        RNAiLibraryContentsParser rnaiLibraryContentsParser,
                        SDFileCompoundLibraryContentsParser compoundLibraryContentsParser)
  {
    _dao = dao;
    _librariesDao = librariesDao;
    _rnaiLibraryContentsParser = rnaiLibraryContentsParser;
    _compoundLibraryContentsParser = compoundLibraryContentsParser;
  }

  // TODO: also create copies
  // TODO: we need serializable isolation, to ensure that plate range is not taken about another process (we need a table-level lock)
  //@Transactional(isolation=Isolation.SERIALIZABLE)
  @Transactional
  public Library createLibrary(Library library, InputStream libraryContentsIn)
  {
    if (library.getLibraryId() != null)
    {
      throw new IllegalArgumentException("library entity must be transient (i.e., no entity ID, never persisted)");
    }

    // verify uniqueness constraints will not be violated.
    // this would happen at flush time, but we can throw a more explicit exception by checking manually
    if (_dao.findEntityByProperty(Library.class, "libraryName", library.getLibraryName()) != null) {
      throw new DuplicateEntityException(library);
    }
    if (_dao.findEntityByProperty(Library.class, "shortName", library.getShortName()) != null) {
      throw new DuplicateEntityException(library);
    }
    if (!_librariesDao.isPlateRangeAvailable(library.getStartPlate(), library.getEndPlate())) {
      throw new DataModelViolationException("plate range [" + library.getStartPlate() + "," + library.getEndPlate() + "] is not available");
    }

    _dao.saveOrUpdateEntity(library);
    log.info("added library definition for " + library);

    _librariesDao.loadOrCreateWellsForLibrary(library);
    log.info("created " + library.getNumWells() + " wells for library " + library);

    if (libraryContentsIn != null) {
      LibraryContentsParser parser;
      if (library.getScreenType().equals(ScreenType.RNAI)) {
        parser = _rnaiLibraryContentsParser;
      }
      else if (library.getScreenType().equals(ScreenType.SMALL_MOLECULE)) {
        parser = _compoundLibraryContentsParser;
      }
      else {
        throw new UnsupportedOperationException("can only import library contents for RNAi or Small Molecule libraries");
      }
      parser.parseLibraryContents(library, null, libraryContentsIn);
      log.info("parsed library contents for " + library);
    }
    else {
      log.info("no library contents file specified; library contents not imported");
    }
    return library;
  }

  // protected constructor

  /**
   * @motivation for CGLIB2
   */
  protected LibraryCreator()
  {
  }

  // private methods

}
