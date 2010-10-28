// $HeadURL$
// $Id$
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.NonPersistentEntity;


public class CopyScreeningStatus extends NonPersistentEntity<Integer> implements Comparable<CopyScreeningStatus>
{
  private Copy _copy;
  private int _assayPlateCount;
  private int _screeningCount;
  private int _plateScreeningCount;
  private LocalDate _firstDateScreened;
  private LocalDate _lastDateScreened;

  //  private int _dataLoadingCount;
  //  private LocalDate _firstDateDataLoaded;
  //  private LocalDate _lastDateDataLoaded;

  public CopyScreeningStatus(Copy copy)
  {
    super(copy.getEntityId());
    
    _copy = copy;
    // TODO: _assayPlateCount = assayPlates.size();

    // TODO: _screeningCount = libraryScreenings.size();

    //    SortedSet<AdministrativeActivity> dataLoadings =
    //      Sets.newTreeSet(Iterables.filter(Iterables.transform(assayPlatesFirstReplicate, AssayPlate.ToScreenResultDataLoading), Predicates.notNull()));
    //    _dataLoadingCount = dataLoadings.size();
    //    if (_dataLoadingCount > 0) {
    //      _firstDateDataLoaded = dataLoadings.first().getDateOfActivity();
    //      _lastDateDataLoaded = dataLoadings.last().getDateOfActivity();
    //    }
  }
  
  public Copy getCopy()
  {
    return _copy;
  }
  
  public int getAssayPlateCount()
  {
    return _assayPlateCount;
  }

  public int getScreeningCount() 
  {
    return _screeningCount;
  }
  
  public int getPlateScreeningCount()
  {
    return _plateScreeningCount;
  }

  public BigDecimal getAveragePlateScreeningCount()
  {
    return new BigDecimal(_plateScreeningCount / (double) getLibraryPlateCount()).setScale(1, RoundingMode.HALF_EVEN);
  }

  public int getLibraryPlateCount()
  {
    return getCopy().getLibrary().getEndPlate() - getCopy().getLibrary().getStartPlate() + 1;
  }

  //  public Collection<LibraryScreening> getLibraryScreenings()
  //  {
  //    return _libraryScreenings;
  //  }
  
  public LocalDate getFirstDateScreened()
  {
    return _firstDateScreened;
  }
  
  public LocalDate getLastDateScreened() 
  {
    return _lastDateScreened;
  }
  

  //  public boolean isScreened()
  //  {
  //    return _screeningCount>0;
  //  }
  

  //  public boolean isDataLoaded()
  //  {
  //    return _dataLoadingCount > 0;
  //  }
  //  
  //  public LocalDate getFirstDateDataLoaded() 
  //  {
  //    return _firstDateDataLoaded;
  //  }
  //  
  //  public LocalDate getLastDateDataLoaded() 
  //  {
  //    return _lastDateDataLoaded;
  //  }
  
  @Override
  public boolean isRestricted()
  {
    return false;
  }

  @Override
  public int compareTo(CopyScreeningStatus other)
  {
    return getEntityId().compareTo(other.getEntityId());
  }

  public void setScreeningCount(int screeningCount)
  {
    _screeningCount = screeningCount;
  }

  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }
  
  public DateTime getDateCreated()
  {
    return _copy.getDateCreated();
  }

}
