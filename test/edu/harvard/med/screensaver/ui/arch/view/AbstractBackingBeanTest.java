// $HeadURL: $
// $Id: $
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.arch.view;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.ui.CurrentScreensaverUser;

public class AbstractBackingBeanTest extends AbstractSpringPersistenceTest
{
  private static Logger log = Logger.getLogger(AbstractBackingBeanTest.class);

  protected AdministratorUser _admin;
  protected CurrentScreensaverUser currentScreensaverUser;

  @Override
  protected void onSetUp() throws Exception
  {
    super.onSetUp();
    _admin = new AdministratorUser("Admin", "User", "admin_user@hms.harvard.edu", "", "", "", "", "");
    genericEntityDao.persistEntity(_admin);
    currentScreensaverUser.setScreensaverUser(_admin);
  }
}
