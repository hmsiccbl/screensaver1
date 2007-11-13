// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.util.EnumSet;
import java.util.Set;

import edu.harvard.med.screensaver.db.ScreenResultSortQuery;
import edu.harvard.med.screensaver.db.ScreenResultSortQuery.SortByWellProperty;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screenresults.AssayWellType;
import edu.harvard.med.screensaver.ui.table.TableColumn;

public abstract class WellColumn<T> extends TableColumn<Well,T>
{
  private SortByWellProperty _wellProperty;
  private Set<T> _items;

  @SuppressWarnings("unchecked")
  public WellColumn(ScreenResultSortQuery.SortByWellProperty wellProperty,
                    String name,
                    String description)
  {
    super(name,
          description,
          wellProperty == SortByWellProperty.PLATE_NUMBER ? ColumnType.INTEGER :
            wellProperty == SortByWellProperty.ASSAY_WELL_TYPE ? ColumnType.VOCABULARY :
              ColumnType.TEXT);
    _items = (Set<T>) EnumSet.allOf(AssayWellType.class);
    _wellProperty = wellProperty;
  }

  public SortByWellProperty getWellProperty()
  {
    return _wellProperty;
  }

  public Set<T> getVocabularly()
  {
    return _items;
  }
}