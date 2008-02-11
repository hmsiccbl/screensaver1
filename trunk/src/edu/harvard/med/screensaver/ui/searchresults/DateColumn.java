// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.util.Comparator;
import java.util.Date;

import edu.harvard.med.screensaver.ui.table.TableColumn;
import edu.harvard.med.screensaver.util.NullSafeComparator;

public abstract class DateColumn<T> extends TableColumn<T,Date>
{
  abstract protected Date getDate(T o);

  public DateColumn(String name, String description)
  {
    super(name, description, ColumnType.DATE);
  }

  @Override
  public Date getCellValue(T o)
  {
    return getDate(o);
  }
  @Override
  protected Comparator<T> getAscendingComparator()
  {
    return new NullSafeComparator<T>() {
      @Override
      protected int doCompare(T o1, T o2) { return getDate(o1).compareTo(getDate(o2)); }
    };
  }

}
