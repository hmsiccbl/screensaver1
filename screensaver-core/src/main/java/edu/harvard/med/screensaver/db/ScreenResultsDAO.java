// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.util.List;
import java.util.Map;

import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.AssayWell;
import edu.harvard.med.screensaver.model.screenresults.DataColumn;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;

public interface ScreenResultsDAO
{
  public Map<WellKey,ResultValue> findResultValuesByPlate(Integer plateNumber, DataColumn col);

  public void deleteScreenResult(ScreenResult screenResult);
  
  public AssayWell findAssayWell(ScreenResult screenResult, WellKey wellKey);

  public List<DataColumn> findMutualPositiveColumns(ScreenResult sr);

  public List<DataColumn> findMutualPositiveColumns(HqlBuilderCallback hqlBuilderCallback);

  public ScreenResult getLatestScreenResult();
}

