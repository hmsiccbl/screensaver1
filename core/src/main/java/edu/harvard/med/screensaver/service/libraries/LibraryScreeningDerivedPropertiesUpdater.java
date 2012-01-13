// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.service.libraries;

import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Plate;
import edu.harvard.med.screensaver.model.screenresults.AssayPlate;
import edu.harvard.med.screensaver.model.screens.LibraryScreening;
import edu.harvard.med.screensaver.service.screens.ScreenDerivedPropertiesUpdater;

public class LibraryScreeningDerivedPropertiesUpdater
{
  private GenericEntityDAO _dao;
  private LibrariesDAO _librariesDao;
  private ScreenDerivedPropertiesUpdater _screenDerivedPropertiesUpdater;
  
  
  /** for CGLIB2 */
  protected LibraryScreeningDerivedPropertiesUpdater()
  {
  }

  @Autowired
  public LibraryScreeningDerivedPropertiesUpdater(GenericEntityDAO dao,
                                                  LibrariesDAO librariesDao,
                                                  ScreenDerivedPropertiesUpdater screenDerivedPropertiesUpdater)
  {
    _dao = dao;
    _librariesDao = librariesDao;
    _screenDerivedPropertiesUpdater = screenDerivedPropertiesUpdater;
  }

  @Transactional
  public void updateScreeningStatistics(LibraryScreening libraryScreeningIn)
  {
    _dao.flush(); // ensure that new library screenings are flushed
    LibraryScreening libraryScreening = _dao.reloadEntity(libraryScreeningIn);
    int n = 0;
    
    Set<Integer> plateNumbersScreened = Sets.newHashSet(Iterables.transform(libraryScreening.getAssayPlatesScreened(), AssayPlate.ToPlateNumber));
    for (Integer plateNumber: plateNumbersScreened) {
      n += _librariesDao.countExperimentalWells(plateNumber, plateNumber);
    }
    libraryScreening.setScreenedExperimentalWellCount(n);

    Function<AssayPlate,Library> AssayPlateToLibrary = Functions.compose(Copy.ToLibrary, Functions.compose(Plate.ToCopy, AssayPlate.ToPlate));
    Set<Library> librariesScreened = Sets.newHashSet(Iterables.transform(libraryScreening.getAssayPlatesScreened(), AssayPlateToLibrary));
    libraryScreening.setLibrariesScreenedCount(librariesScreened.size());
    libraryScreening.setLibraryPlatesScreenedCount(plateNumbersScreened.size());

    _screenDerivedPropertiesUpdater.updateScreeningStatistics(libraryScreening.getScreen());
  }
}
