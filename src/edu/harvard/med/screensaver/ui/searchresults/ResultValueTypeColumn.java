// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.ui.table.TableColumn;

public class ResultValueTypeColumn extends TableColumn<Well>
{
  private ResultValueType _resultValueType;

  public ResultValueTypeColumn(ResultValueType resultValueType)
  {
    super(resultValueType.getName(),
          resultValueType.getDescription(),
          resultValueType.isNumeric());
    _resultValueType = resultValueType;
  }

  @Override
  public Object getCellValue(Well well)
  {
    ResultValue resultValue = well.getResultValues().get(_resultValueType);
    return resultValue == null ? null : ResultValue.getTypedValue(resultValue, _resultValueType);
  }

  public ResultValueType getResultValueType()
  {
    return _resultValueType;
  }
}