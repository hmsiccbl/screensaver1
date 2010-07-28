// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.domainlogic;

import java.util.Set;

import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Plate;
import edu.harvard.med.screensaver.model.screenresults.AssayPlate;
import edu.harvard.med.screensaver.model.screens.LibraryScreening;

import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public class LibraryScreeningEntityUpdater implements EntityUpdater<LibraryScreening>
{
  private LibrariesDAO _librariesDao;
  
  
  /** for CGLIB2 */
  protected LibraryScreeningEntityUpdater()
  {
  }

  public LibraryScreeningEntityUpdater(LibrariesDAO librariesDao)
  {
    _librariesDao = librariesDao;
  }

  @Override
  @Transactional
  public void apply(LibraryScreening libraryScreeningIn)
  {
    LibraryScreening libraryScreening = (LibraryScreening) libraryScreeningIn;
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
  }

  @Override
  public Class<LibraryScreening> getEntityClass()
  {
    return LibraryScreening.class;
  }
}
