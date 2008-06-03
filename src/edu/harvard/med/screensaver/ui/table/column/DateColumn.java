// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.table.column;

import java.util.Comparator;

import edu.harvard.med.screensaver.util.NullSafeComparator;

import org.joda.time.LocalDate;

public abstract class DateColumn<R> extends TableColumn<R,LocalDate>
{
  abstract protected LocalDate getDate(R o);

  public DateColumn(String name, String description, String group)
  {
    super(name, description, ColumnType.DATE, group);
  }

  @Override
  public LocalDate getCellValue(R o)
  {
    return getDate(o);
  }
  
  @Override
  protected Comparator<R> getAscendingComparator()
  {
    return new NullSafeComparator<R>() {
      NullSafeComparator<LocalDate> _dateComparator = new NullSafeComparator<LocalDate>() {
        @Override
        protected int doCompare(LocalDate d1, LocalDate d2)
        {
          return d1.compareTo(d2);
        }
      };

      @Override
      protected int doCompare(R o1, R o2) { return _dateComparator.compare(getDate(o1), getDate(o2)); }
    };
  }

}
