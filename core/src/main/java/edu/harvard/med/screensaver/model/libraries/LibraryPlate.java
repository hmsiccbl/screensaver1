// $HeadURL$
// $Id$
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import java.util.Collection;
import java.util.Set;
import java.util.SortedSet;

import com.google.common.base.Functions;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.joda.time.LocalDate;

import edu.harvard.med.screensaver.model.AdministrativeActivity;
import edu.harvard.med.screensaver.model.NonPersistentEntity;
import edu.harvard.med.screensaver.model.screenresults.AssayPlate;
import edu.harvard.med.screensaver.model.screens.LibraryScreening;

public class LibraryPlate extends NonPersistentEntity<Integer> implements Comparable<LibraryPlate>
{
  private Library _library;
  private SortedSet<LibraryScreening> _libraryScreenings;
  private int _assayPlateCount;
  private LocalDate _firstDateScreened;
  private LocalDate _lastDateScreened;
  private int _dataLoadingCount;
  private LocalDate _firstDateDataLoaded;
  private LocalDate _lastDateDataLoaded;
  private SortedSet<Copy> _copiesScreened;

  public LibraryPlate(Integer plateNumber,
                      Library library,
                      Set<AssayPlate> assayPlates)
  {
    super(plateNumber);
    
    _library = library; 
    _assayPlateCount = assayPlates.size();
    Iterable<AssayPlate> assayPlatesUniqueAttempts = Iterables.filter(assayPlates, AssayPlate.IsFirstReplicate);
    _copiesScreened = Sets.newTreeSet(Iterables.transform(Iterables.filter(assayPlates, AssayPlate.HasLibraryScreening),
                                                          Functions.compose(Plate.ToCopy, AssayPlate.ToPlate)));
    _libraryScreenings = Sets.newTreeSet(Iterables.filter(Iterables.transform(assayPlatesUniqueAttempts, AssayPlate.ToLibraryScreening), Predicates.notNull()));
    if (!_libraryScreenings.isEmpty()) {
      _firstDateScreened = _libraryScreenings.first().getDateOfActivity();
      _lastDateScreened = _libraryScreenings.last().getDateOfActivity();
    }
    SortedSet<AdministrativeActivity> dataLoadings =
      Sets.newTreeSet(Iterables.filter(Iterables.transform(assayPlatesUniqueAttempts, AssayPlate.ToScreenResultDataLoading), Predicates.notNull()));
    _dataLoadingCount = dataLoadings.size();
    if (_dataLoadingCount > 0) {
      _firstDateDataLoaded = dataLoadings.first().getDateOfActivity();
      _lastDateDataLoaded = dataLoadings.last().getDateOfActivity();
    }
  }
  
  public int getPlateNumber()
  {
    return getEntityId();
  }
  
  public Library getLibrary()
  {
    return _library;
  }
  
  public void setLibrary(Library library)
  {
    _library = library;
  }

  public int getAssayPlateCount()
  {
    return _assayPlateCount;
  }

  public int getScreeningCount() 
  {
    return _libraryScreenings.size();
  }
  
  public Collection<LibraryScreening> getLibraryScreenings()
  {
    return _libraryScreenings;
  }
  
  public LocalDate getFirstDateScreened()
  {
    return _firstDateScreened;
  }
  
  public LocalDate getLastDateScreened() 
  {
    return _lastDateScreened;
  }
  
  public boolean isScreened()
  {
    return !_libraryScreenings.isEmpty();
  }
  
  public boolean isDataLoaded()
  {
    return _dataLoadingCount > 0;
  }
  
  public LocalDate getFirstDateDataLoaded() 
  {
    return _firstDateDataLoaded;
  }
  
  public LocalDate getLastDateDataLoaded() 
  {
    return _lastDateDataLoaded;
  }
  
  public Set<Copy> getCopiesScreened()
  {
    return _copiesScreened;
  }

  @Override
  public int compareTo(LibraryPlate other)
  {
    return getEntityId().compareTo(other.getEntityId());
  }
}
