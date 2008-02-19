// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.table.column;

import java.util.Comparator;
import java.util.Date;

import edu.harvard.med.screensaver.util.NullSafeComparator;

public abstract class DateColumn<R> extends TableColumn<R,Date>
{
  abstract protected Date getDate(R o);

  public DateColumn(String name, String description, String group)
  {
    super(name, description, ColumnType.DATE, group);
  }

  @Override
  public Date getCellValue(R o)
  {
    return getDate(o);
  }
  @Override
  protected Comparator<R> getAscendingComparator()
  {
    return new NullSafeComparator<R>() {
      @Override
      protected int doCompare(R o1, R o2) { return getDate(o1).compareTo(getDate(o2)); }
    };
  }

}
