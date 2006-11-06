// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.analysis;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

public class ChainedFilter<T> implements Filter<T>
{
  // static members

  private static Logger log = Logger.getLogger(ChainedFilter.class);

  
  // instance data members

  private Filter<T> _filter;
  private ChainedFilter<T> _next;

  
  // public methods
  
  public ChainedFilter(Filter<T>... filters)
  {
    this(Arrays.asList(filters));
  }

  @SuppressWarnings("unchecked")
  public ChainedFilter(List<Filter<T>> filters)
  {
    if (filters.size() == 0) {
      _filter = null;
      _next = null;
    }
    else {
      _filter = filters.get(0);
      if (filters.size() > 1) {
        _next = new ChainedFilter(filters.subList(1, filters.size()));
      }
    }
  }

  public ChainedFilter(Filter<T> filter)
  {
    _filter = filter;
    _next = null;
  }

  public ChainedFilter(Filter<T> filter, ChainedFilter<T> next)
  {
    _filter = filter;
    _next = next;
  }

  public boolean exclude(T element)
  {
    if (_filter == null) {
      return false;
    }
    if (_filter.exclude(element)) {
      return true;
    }
    if (_next == null) {
      return false;
    }
    return _next.exclude(element);
  }
}

