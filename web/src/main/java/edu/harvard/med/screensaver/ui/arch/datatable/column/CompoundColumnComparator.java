// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.arch.datatable.column;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import edu.harvard.med.screensaver.db.SortDirection;

import org.apache.log4j.Logger;

public class CompoundColumnComparator<R> implements Comparator<R>
{
  // static members

  private static Logger log = Logger.getLogger(CompoundColumnComparator.class);


  // instance data members

  private List<TableColumn<R,?>> _sortColumns;
  private SortDirection _sortDirection;


  // public constructors and methods

  public CompoundColumnComparator(List<? extends TableColumn<R,?>> sortColumns, SortDirection sortDirection)
  {
    _sortColumns = new ArrayList<TableColumn<R,?>>(sortColumns);
    _sortDirection = sortDirection;
  }

  public int compare(R row1, R row2)
  {
    int result = 0;
    boolean first = true;
    for (TableColumn<R,?> sortColumn : _sortColumns) {
      SortDirection dir = SortDirection.ASCENDING;
      if (first && _sortDirection.equals(SortDirection.DESCENDING)) {
        // only the most significant column obeys the requested sort direction;
        // other columns are always ascending
        dir = SortDirection.DESCENDING;
        first = false;
      }
      result = sortColumn.getComparator(dir).compare(row1, row2);
      if (result != 0) {
        // no need to compare values in less significant column
        break;
      }
    }
    return result;
  }

  // private methods

}

