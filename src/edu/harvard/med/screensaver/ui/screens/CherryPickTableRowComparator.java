// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screens;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import edu.harvard.med.screensaver.db.SortDirection;

import org.apache.log4j.Logger;

// TODO: genericize

public class CherryPickTableRowComparator implements Comparator<Map>
{
  // static members

  private static Logger log = Logger.getLogger(CherryPickTableRowComparator.class);


  // instance data members

  private List<String> _sortKeys;
  private SortDirection _sortDirection;

  
  // public constructors and methods
  
  public CherryPickTableRowComparator(String sortKey, SortDirection sortDirection)
  {
    _sortKeys = new ArrayList<String>();
    _sortKeys.add(sortKey);
    _sortDirection = sortDirection;
  }

  public CherryPickTableRowComparator(String[] sortKeys, SortDirection sortDirection)
  {
    _sortKeys = Arrays.asList(sortKeys);
    _sortDirection = sortDirection;
  }

  @SuppressWarnings("unchecked")
  public int compare(Map row1, Map row2)
  {
    int result = 0;
    boolean first = true;
    for (String sortKey : _sortKeys) {
      Comparable value1 = (Comparable) row1.get(sortKey);
      Comparable value2 = (Comparable) row2.get(sortKey);
      
      // handle null values
      if (value1 == null) {
        if (value2 == null) {
          return 0;
        }
        return -1;
      }
      else if (value2 == null) {
        return 1;
      }

      result = ((Comparable) row1.get(sortKey)).compareTo((Comparable) row2.get(sortKey));
      if (first && _sortDirection.equals(SortDirection.DESCENDING)) {
        result = result * -1;
        first = false;
      }
      if (result != 0) {
        break;
      }
    }
    return result;
  }
  

  // private methods

}

