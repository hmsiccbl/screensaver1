// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.util.List;
import java.util.SortedSet;

import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;


public interface UsersDAO
{
  public List<String> findDeveloperECommonsIds();

  public SortedSet<LabHead> findAllLabHeads();
  
  public <UT extends ScreeningRoomUser> UT findSRU(Class<UT> userClass,
                                                          String firstName,
                                                          String lastName,
                                                          String email);

  
}
