// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import edu.harvard.med.screensaver.db.ScreenResultSortQuery;
import edu.harvard.med.screensaver.db.ScreenResultSortQuery.SortByWellProperty;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.ui.table.TableColumn;

public abstract class WellColumn extends TableColumn<Well>
{
  private SortByWellProperty _wellProperty;

  public WellColumn(ScreenResultSortQuery.SortByWellProperty wellProperty,
                    String name,
                    String description,
                    boolean isNumeric)
  {
    super(name,
          description,
          isNumeric);
    _wellProperty = wellProperty;
  }

  public SortByWellProperty getWellProperty()
  {
    return _wellProperty;
  }
}