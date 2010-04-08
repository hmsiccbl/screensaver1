// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.table.column;

import java.util.Comparator;

import edu.harvard.med.screensaver.util.NullSafeComparator;

import org.joda.time.DateTime;

public abstract class DateTimeColumn<R> extends TableColumn<R,DateTime>
{
  abstract protected DateTime getDateTime(R o);

  public DateTimeColumn(String name, String description, String group)
  {
    super(name, description, ColumnType.DATE_TIME, group);
  }

  @Override
  public DateTime getCellValue(R o)
  {
    return getDateTime(o);
  }
  
  @Override
  protected Comparator<R> getAscendingComparator()
  {
    return new NullSafeComparator<R>() {
      NullSafeComparator<DateTime> _dateComparator = new NullSafeComparator<DateTime>() {
        @Override
        protected int doCompare(DateTime d1, DateTime d2)
        {
          return d1.compareTo(d2);
        }
      };

      @Override
      protected int doCompare(R o1, R o2) { return _dateComparator.compare(getDateTime(o1), getDateTime(o2)); }
    };
  }

}
