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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import edu.harvard.med.screensaver.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.ui.CurrentScreensaverUser;

@ContextConfiguration(locations = { "classpath:spring-context-test-ui.xml" }, inheritLocations = false)
public abstract class AbstractBackingBeanTest extends AbstractSpringPersistenceTest
{
  private static Logger log = Logger.getLogger(AbstractBackingBeanTest.class);

  @Autowired
  protected CurrentScreensaverUser currentScreensaverUser;

  protected AdministratorUser _admin;

  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    _admin = new AdministratorUser("Admin", "User");
    _admin = genericEntityDao.mergeEntity(_admin);
    currentScreensaverUser.setScreensaverUser(_admin);
  }
}
