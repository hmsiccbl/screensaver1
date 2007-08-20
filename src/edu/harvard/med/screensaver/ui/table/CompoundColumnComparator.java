// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.table;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import edu.harvard.med.screensaver.db.SortDirection;

import org.apache.log4j.Logger;

// TODO: genericize

public class CompoundColumnComparator<E> implements Comparator<E>
{
  // static members

  private static Logger log = Logger.getLogger(CompoundColumnComparator.class);


  // instance data members

  private List<TableColumn<E>> _sortColumns;
  private SortDirection _sortDirection;

  
  // public constructors and methods
  
  public CompoundColumnComparator(List<TableColumn<E>> sortColumns, SortDirection sortDirection)
  {
    _sortColumns = new ArrayList<TableColumn<E>>(sortColumns);
    _sortDirection = sortDirection;
  }

  public int compare(E row1, E row2)
  {
    int result = 0;
    boolean first = true;
    for (TableColumn<E> sortColumn : _sortColumns) {
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

