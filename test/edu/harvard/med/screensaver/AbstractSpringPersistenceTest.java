// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.SchemaUtil;

import org.apache.log4j.Logger;

/**
 * Extends AbstractSpringTest by automatically providing a GenericEntityDAO object and
 * truncating the test database during test setup.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class AbstractSpringPersistenceTest extends AbstractSpringTest
{
  // static members

  private static Logger log = Logger.getLogger(AbstractSpringPersistenceTest.class);


  // instance data members

  protected GenericEntityDAO genericEntityDao;
  protected SchemaUtil schemaUtil;
  
  @Override
  protected void onSetUp() throws Exception
  {
    super.onSetUp();
    schemaUtil.truncateTablesOrCreateSchema();
  }
  
  // public constructors and methods
  
  public AbstractSpringPersistenceTest()
  {
  }
  
  public AbstractSpringPersistenceTest(String testName)
  {
    super(testName);
  }

  // private methods

}

