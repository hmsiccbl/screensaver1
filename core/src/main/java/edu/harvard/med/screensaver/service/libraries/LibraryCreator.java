// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.service.libraries;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.model.DataModelViolationException;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryWellType;
import edu.harvard.med.screensaver.model.libraries.WellKey;

/**
 * Service that creates a new library and its wells and imports its well
 * contents into the database.
 */
public class LibraryCreator
{
  private static Logger log = Logger.getLogger(LibraryCreator.class);

  private GenericEntityDAO _dao;
  private LibrariesDAO _librariesDao;
  private LibraryContentsLoader _libraryContentsLoader;
  
  /**
   * @motivation for CGLIB2
   */
  protected LibraryCreator() {}

  public LibraryCreator(GenericEntityDAO dao,
                        LibrariesDAO librariesDao,
                        LibraryContentsLoader libraryContentLoader)
  {
    _dao = dao;
    _librariesDao = librariesDao;
    _libraryContentsLoader = libraryContentLoader;
  }

  /**
   * Prepare and create the Library specified.
   * 
   * @throws IllegalArgumentException if the specified Library has already been persisted to the Database
   * @param newLibrary Library entity that has not been persisted to the database <br>
   *          -Library Name cannot be reused. <br>
   *          -Short Name cannot be reused.
   */
//TODO: also create copies
//TODO: we need serializable isolation, to ensure that plate range is not taken about another process (we need a table-level lock)
  @Transactional
  public void createLibrary(Library newLibrary)
  {
    validateLibrary(newLibrary);
    createWells(newLibrary);
    _dao.saveOrUpdateEntity(newLibrary);
    _dao.flush();
    log.info("added library definition for " + newLibrary.getLibraryName() + ", " + newLibrary);
  }

  public void createWells(Library library)
  {
    for (int iPlate = library.getStartPlate(); iPlate <= library.getEndPlate(); ++iPlate) {
      for (int iRow = 0; iRow < library.getPlateSize().getRows(); ++iRow) {
        for (int iCol = 0; iCol < library.getPlateSize().getColumns(); ++iCol) {
          WellKey wellKey = new WellKey(iPlate, iRow, iCol);
          library.createWell(wellKey, LibraryWellType.UNDEFINED);
        }
      }
    }
  }

  public void validateLibrary(Library newLibrary) throws DataModelViolationException
  {
    Library dbLibrary = _dao.findEntityByProperty(Library.class, "libraryName", newLibrary.getLibraryName());
    if (dbLibrary == null) {
      dbLibrary = _dao.findEntityByProperty(Library.class, "shortName", newLibrary.getShortName()); 
    }
    if (dbLibrary != null) {
      throw new DataModelViolationException("library name already in use");
    }

    if (!_librariesDao.isPlateRangeAvailable(newLibrary.getStartPlate(), newLibrary.getEndPlate())) {
      throw new DataModelViolationException("plate range [" 
          + newLibrary.getStartPlate() + "," + newLibrary.getEndPlate() + "] is not available");
    }
  }

}
