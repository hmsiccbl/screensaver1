// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.table;

import edu.harvard.med.screensaver.db.SortDirection;

import org.apache.log4j.Logger;

public class SortChangedEvent<R>
{
  // static members

  private static Logger log = Logger.getLogger(SortChangedEvent.class);


  // instance data members

  private SortDirection _direction;
  private TableColumn<R,?> _column;

  // public constructors and methods

  public SortChangedEvent()
  {
  }

  public SortChangedEvent(SortDirection direction)
  {
    _direction = direction;
  }

  public SortChangedEvent(TableColumn<R,?> column)
  {
    _column = column;
  }

  public TableColumn<R,?> getColumn()
  {
    return _column;
  }

  public SortDirection getDirection()
  {
    return _direction;
  }

  public void setColumn(TableColumn<R,?> column)
  {
    _column = column;
  }

  public void setDirection(SortDirection direction)
  {
    _direction = direction;
  }

  @Override
  public String toString()
  {
    StringBuilder s = new StringBuilder();
    s.append(getClass().getSimpleName());
    if (_direction != null) {
      s.append(" new dir=").append(_direction);
    }
    if (_column != null) {
      s.append(" new col=").append(_direction);
    }
    return s.toString();
  }

  // private methods

}
