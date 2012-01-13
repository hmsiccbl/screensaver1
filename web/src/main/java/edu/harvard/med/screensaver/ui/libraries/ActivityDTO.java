// $HeadURL$
// $Id$
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import org.joda.time.LocalDate;

import edu.harvard.med.screensaver.model.users.ScreensaverUser;

/** Data transfer object for activity properties */
public class ActivityDTO
{
  private ScreensaverUser _performedBy;
  private LocalDate _dateOfActivity;
  private String _comments;

  public ScreensaverUser getPerformedBy()
  {
    return _performedBy;
  }

  public void setPerformedBy(ScreensaverUser performedBy)
  {
    _performedBy = performedBy;
  }

  public LocalDate getDateOfActivity()
  {
    return _dateOfActivity;
  }

  public void setDateOfActivity(LocalDate dateOfActivity)
  {
    _dateOfActivity = dateOfActivity;
  }

  public String getComments()
  {
    return _comments;
  }

  public void setComments(String comments)
  {
    _comments = comments;
  }
}
