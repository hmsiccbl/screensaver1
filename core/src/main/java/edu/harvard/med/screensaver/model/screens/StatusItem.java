// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;


import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.hibernate.annotations.Type;
import org.joda.time.LocalDate;

@Embeddable
public class StatusItem implements Comparable<StatusItem>
{
  private LocalDate _statusDate;
  private ScreenStatus _status;

  public StatusItem(LocalDate statusDate, ScreenStatus screenStatus)
  {
    _statusDate = statusDate;
    _status = screenStatus;
  }

  /**
   * @motivation for hibernate and proxy/concrete subclass constructors
   * @motivation DTO for user interface
   */
  public StatusItem()
  {}

  @Column(nullable=false, updatable=false)
  @Type(type="edu.harvard.med.screensaver.db.usertypes.LocalDateType")
  public LocalDate getStatusDate()
  {
    return _statusDate;
  }

  private void setStatusDate(LocalDate statusDate)
  {
    _statusDate = statusDate;
  }

  @Column(nullable = false, updatable = false)
  @org.hibernate.annotations.Type(type = "edu.harvard.med.screensaver.model.screens.ScreenStatus$UserType")
  public ScreenStatus getStatus()
  {
    return _status;
  }

  private void setStatus(ScreenStatus status)
  {
    _status = status;
  }

  public int compareTo(StatusItem other)
  {
    int result = getStatus().ordinal() < other.getStatus().ordinal() ? -1 : getStatus().ordinal() > other.getStatus().ordinal()
      ? 1 : 0;
    if (result == 0) {
      result = getStatusDate().compareTo(other.getStatusDate());
    }
    return result;
  }

  @Override
  public int hashCode()
  {
    return _status.hashCode();
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    StatusItem other = (StatusItem) obj;
    if (_status != other._status) return false;
    return true;
  }
}
