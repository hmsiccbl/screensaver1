// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.test;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.SchemaUtil;

/**
 * Extends AbstractSpringTest by automatically providing a GenericEntityDAO object and
 * truncating the test database during test setup.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
@TestExecutionListeners({ TransactionalTestExecutionListener.class })
public abstract class AbstractSpringPersistenceTest extends AbstractSpringTest
{
  private static Logger log = Logger.getLogger(AbstractSpringPersistenceTest.class);

  @Autowired
  protected GenericEntityDAO genericEntityDao;
  @Autowired
  protected SchemaUtil schemaUtil;
  @Autowired
  protected TestDataFactory dataFactory;

  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    dataFactory.resetToDefaults();
    schemaUtil.truncateTables();
  }

  protected void flushAndClear()
  {
    genericEntityDao.flush();
    genericEntityDao.clear();
  }

  public AbstractSpringPersistenceTest()
  {
  }

  public AbstractSpringPersistenceTest(String testName)
  {
    super(testName);
  }
}

