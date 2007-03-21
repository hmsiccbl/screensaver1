// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screens;

import java.util.Comparator;
import java.util.Map;

import edu.harvard.med.screensaver.ui.searchresults.SortDirection;

import org.apache.log4j.Logger;

// TODO: genericize

public class CherryPickTableRowComparator implements Comparator<Map>
{
  // static members

  private static Logger log = Logger.getLogger(CherryPickTableRowComparator.class);


  // instance data members

  private String _sortKey;
  private SortDirection _sortDirection;

  
  // public constructors and methods
  
  public CherryPickTableRowComparator(String sortKey, SortDirection sortDirection)
  {
    _sortKey = sortKey;
    _sortDirection = sortDirection;
  }

  @SuppressWarnings("unchecked")
  public int compare(Map row1, Map row2)
  {
    int result = ((Comparable) row1.get(_sortKey)).compareTo((Comparable) row2.get(_sortKey));
    if (_sortDirection.equals(SortDirection.DESCENDING)) {
      result = result * -1;
    }
    return result;
  }
  

  // private methods

}

