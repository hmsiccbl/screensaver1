// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.service.screens;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.ScreenDAO;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Plate;
import edu.harvard.med.screensaver.model.screenresults.AssayPlate;
import edu.harvard.med.screensaver.model.screens.Screen;

public class ScreenDerivedPropertiesUpdater
{
  private GenericEntityDAO _dao;
  private ScreenDAO _screenDao;
  
  
  /** for CGLIB2 */
  protected ScreenDerivedPropertiesUpdater()
  {
  }

  @Autowired
  public ScreenDerivedPropertiesUpdater(GenericEntityDAO dao,
                                        ScreenDAO screenDao)
  {
    _dao = dao;
    _screenDao = screenDao;
  }

  @Transactional
  public void updateScreeningStatistics(Screen screenIn)
  {
    _dao.flush(); // ensure all of the data that were are about to query is flushed to the database
    Screen screen = (Screen) _dao.reloadEntity(screenIn);
    SortedSet<AssayPlate> assayPlatesScreened = screen.getAssayPlatesScreened();
    SortedSet<AssayPlate> assayPlatesDataLoaded = screen.getAssayPlatesDataLoaded();

    Set<Integer> plateNumbersScreened = Sets.newHashSet(Iterables.transform(assayPlatesScreened, AssayPlate.ToPlateNumber));
    Set<Integer> plateNumbersLoaded = Sets.newHashSet(Iterables.transform(assayPlatesDataLoaded, AssayPlate.ToPlateNumber));
    
    Function<AssayPlate,Library> AssayPlateToLibrary = Functions.compose(Copy.ToLibrary, Functions.compose(Plate.ToCopy, AssayPlate.ToPlate));
    Set<Library> librariesScreened = Sets.newHashSet(Iterables.transform(assayPlatesScreened, AssayPlateToLibrary));
    
    screen.setAssayPlatesScreenedCount(assayPlatesScreened.size());
    screen.setLibraryPlatesScreenedCount(plateNumbersScreened.size());
    screen.setLibraryPlatesDataLoadedCount(plateNumbersLoaded.size());
    // TODO: currently, all loaded data is also already analyzed; this will change in the future; see [#1315]
    screen.setLibraryPlatesDataAnalyzedCount(plateNumbersLoaded.size());
    screen.setLibrariesScreenedCount(librariesScreened.size());

    SortedSet<Integer> maxReplicatesPerPlateNumber = findMaxReplicatesPerPlateNumber(assayPlatesScreened);
    screen.setMinScreenedReplicateCount(maxReplicatesPerPlateNumber.isEmpty() ? null : maxReplicatesPerPlateNumber.first());
    screen.setMaxScreenedReplicateCount(maxReplicatesPerPlateNumber.isEmpty() ? null : maxReplicatesPerPlateNumber.last());

    maxReplicatesPerPlateNumber = findMaxReplicatesPerPlateNumber(assayPlatesDataLoaded);
    screen.setMinDataLoadedReplicateCount(maxReplicatesPerPlateNumber.isEmpty() ? null : maxReplicatesPerPlateNumber.first());
    screen.setMaxDataLoadedReplicateCount(maxReplicatesPerPlateNumber.isEmpty() ? null : maxReplicatesPerPlateNumber.last());
    
    screen.setScreenedExperimentalWellCount(_screenDao.countScreenedExperimentalWells(screen, false));
    screen.setUniqueScreenedExperimentalWellCount(_screenDao.countScreenedExperimentalWells(screen, true));

    if (screen.getScreenResult() != null) {
      screen.getScreenResult().setExperimentalWellCount(_screenDao.countLoadedExperimentalWells(screen));
    }
  }

  private SortedSet<Integer> findMaxReplicatesPerPlateNumber(SortedSet<AssayPlate> assayPlatesScreened)
  {
    Multimap<Integer,AssayPlate> plateNumbersToAssayPlates = Multimaps.index(assayPlatesScreened, AssayPlate.ToPlateNumber);
    SortedSet<Integer> maxReplicatesPerPlateNumber = Sets.newTreeSet(Iterables.transform(plateNumbersToAssayPlates.asMap().entrySet(),
                        new Function<Map.Entry<Integer,Collection<AssayPlate>>,Integer>() {
      @Override
      public Integer apply(Map.Entry<Integer,Collection<AssayPlate>> e) {
        Iterable<Integer> replicateOrdinals = Iterables.transform(e.getValue(), AssayPlate.ToReplicateOrdinal);
        return Ordering.natural().max(replicateOrdinals) + 1;
      }
    }));
    return maxReplicatesPerPlateNumber;
  }

  @Transactional
  public void updateTotalPlatedLabCherryPickCount(Screen screen)
  {
    _dao.flush(); // ensure all of the data that were are about to query is flushed to the database
    screen = _dao.reloadEntity(screen);
    screen.setTotalPlatedLabCherryPicks(_screenDao.countTotalPlatedLabCherryPicks(screen));
  }

}
