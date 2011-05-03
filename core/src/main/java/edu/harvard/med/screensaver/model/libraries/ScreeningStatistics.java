// $HeadURL$
// $Id$
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import org.joda.time.LocalDate;

public class ScreeningStatistics
{
  public static final ScreeningStatistics NullScreeningStatistics = new ScreeningStatistics();

  private int _assayPlateCount;
  private int _screeningCount;
  private int _plateScreeningCount;
  private LocalDate _firstDateScreened;
  private LocalDate _lastDateScreened;
  private int _dataLoadingCount;
  private LocalDate _firstDateDataLoaded;
  private LocalDate _lastDateDataLoaded;
  private int _plateCount;

  public void setAssayPlateCount(int assayPlateCount)
  {
    _assayPlateCount = assayPlateCount;
  }

  public int getAssayPlateCount()
  {
    return _assayPlateCount;
  }

  public void setScreeningCount(int screeningCount)
  {
    _screeningCount = screeningCount;
  }

  public int getScreeningCount()
  {
    return _screeningCount;
  }

  public void setPlateScreeningCount(int plateScreeningCount)
  {
    _plateScreeningCount = plateScreeningCount;
  }

  public int getPlateScreeningCount()
  {
    return _plateScreeningCount;
  }

  public void setFirstDateScreened(LocalDate firstDateScreened)
  {
    _firstDateScreened = firstDateScreened;
  }

  public LocalDate getFirstDateScreened()
  {
    return _firstDateScreened;
  }

  public void setLastDateScreened(LocalDate lastDateScreened)
  {
    _lastDateScreened = lastDateScreened;
  }

  public LocalDate getLastDateScreened()
  {
    return _lastDateScreened;
  }

  public void setDataLoadingCount(int dataLoadingCount)
  {
    _dataLoadingCount = dataLoadingCount;
  }

  public int getDataLoadingCount()
  {
    return _dataLoadingCount;
  }

  public void setFirstDateDataLoaded(LocalDate firstDateDataLoaded)
  {
    _firstDateDataLoaded = firstDateDataLoaded;
  }

  public LocalDate getFirstDateDataLoaded()
  {
    return _firstDateDataLoaded;
  }

  public void setLastDateDataLoaded(LocalDate lastDateDataLoaded)
  {
    _lastDateDataLoaded = lastDateDataLoaded;
  }

  public LocalDate getLastDateDataLoaded()
  {
    return _lastDateDataLoaded;
  }

  public void setPlateCount(int plateCount)
  {
    _plateCount = plateCount;
  }

  public int getPlateCount()
  {
    return _plateCount;
  }
}