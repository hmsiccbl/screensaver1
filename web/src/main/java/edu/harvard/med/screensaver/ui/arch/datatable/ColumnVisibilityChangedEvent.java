// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.arch.datatable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import edu.harvard.med.screensaver.ui.arch.datatable.column.TableColumn;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

public class ColumnVisibilityChangedEvent
{
  // static members

  private static Logger log = Logger.getLogger(ColumnVisibilityChangedEvent.class);


  // instance data members

  private Set<TableColumn<?,?>> _added = new HashSet<TableColumn<?,?>>();
  private Set<TableColumn<?,?>> _removed = new HashSet<TableColumn<?,?>>();


  // public constructors and methods

  public ColumnVisibilityChangedEvent()
  {
  }

  public ColumnVisibilityChangedEvent(Collection<? extends TableColumn<?,?>> added, 
                                      Collection<? extends TableColumn<?,?>> removed)
  {
    if (CollectionUtils.intersection(added, removed).size() > 0) {
      throw new IllegalArgumentException("'added' and 'removed' collections must be disjoint");
    }
    if (added != null) {
      _added.addAll(added);
    }
    if (removed != null) {
      _removed.addAll(removed);
    }
  }
  
  public ColumnVisibilityChangedEvent added(TableColumn<?,?> added) 
  {
    if (_removed.contains(added)) {
      throw new IllegalArgumentException("column is in 'removed'");
    }
    _added.add(added);
    return this;
  }

  public ColumnVisibilityChangedEvent removed(TableColumn<?,?> removed) 
  {
    if (_removed.contains(removed)) {
      throw new IllegalArgumentException("column is in 'added'");
    }
    _removed.add(removed);
    return this;
  }

  public Set<TableColumn<?,?>> getColumnsAdded()
  {
    return _added;
  }

  public Set<TableColumn<?,?>> getColumnsRemoved()
  {
    return _removed;
  }
  
  @Override
  public String toString()
  {
    return new StringBuilder().append("ColumnVisibilityChanged[added=").append(_added).append("; removed=").append(_removed).toString();
  }
}
