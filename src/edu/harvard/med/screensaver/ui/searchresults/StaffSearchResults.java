// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.ui.users.UserViewer;

public class StaffSearchResults extends UserSearchResults<AdministratorUser>
{
  protected StaffSearchResults()
  {
  }

  public StaffSearchResults(GenericEntityDAO dao,
                            UserViewer userViewer)
  {
    super(AdministratorUser.class, dao, userViewer);
  }
}
