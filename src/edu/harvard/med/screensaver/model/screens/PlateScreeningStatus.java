// $HeadURL$
// $Id$
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import edu.harvard.med.screensaver.model.AdministrativeActivity;
import edu.harvard.med.screensaver.model.NonPersistentEntity;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Plate;
import edu.harvard.med.screensaver.model.screenresults.AssayPlate;

import org.joda.time.LocalDate;

import com.google.common.base.Functions;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;


public class PlateScreeningStatus extends NonPersistentEntity<Integer> implements Comparable<PlateScreeningStatus>
{
  private Library _library;
  // note: we use a List instead of a Set, because legacy LibraryScreenings can
  // contain the same plate number multiple times, and we want to count that
  // multiply when counting screenings
  private List<LibraryScreening> _libraryScreenings;
  private int _assayPlateCount;
  private LocalDate _firstDateScreened;
  private LocalDate _lastDateScreened;
  private int _dataLoadingCount;
  private LocalDate _firstDateDataLoaded;
  private LocalDate _lastDateDataLoaded;
  private SortedSet<String> _copyNames;

  public PlateScreeningStatus(Integer plateNumber,
                              Library library,
                              Set<AssayPlate> assayPlates)
  {
    super(plateNumber);
    
    _library = library; 
    _assayPlateCount = assayPlates.size();
    Iterable<AssayPlate> assayPlatesUniqueAttempts = Iterables.filter(assayPlates, AssayPlate.IsFirstReplicate);
    _copyNames = Sets.newTreeSet(Iterables.transform(Iterables.filter(assayPlates, AssayPlate.HasLibraryScreening), 
                                                     Functions.compose(Copy.ToName, Functions.compose(Plate.ToCopy, AssayPlate.ToPlate))));
    _libraryScreenings = Lists.newArrayList(Iterables.filter(Iterables.transform(assayPlatesUniqueAttempts, AssayPlate.ToLibraryScreening), Predicates.notNull()));
    if (!_libraryScreenings.isEmpty()) {
      _firstDateScreened = _libraryScreenings.get(0).getDateOfActivity();
      _lastDateScreened = Iterables.getLast(_libraryScreenings).getDateOfActivity();
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
  
  public Set<String> getCopiesScreened()
  {
    return _copyNames;
  }

  @Override
  public boolean isRestricted()
  {
    return false;
  }

  @Override
  public int compareTo(PlateScreeningStatus other)
  {
    return getEntityId().compareTo(other.getEntityId());
  }
}
