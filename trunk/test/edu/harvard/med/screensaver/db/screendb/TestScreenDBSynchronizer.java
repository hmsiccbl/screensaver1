// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/test/edu/harvard/med/screensaver/model/EntityClassesExercisor.java $
// $Id: EntityClassesExercisor.java 962 2007-01-23 00:52:16Z ant4 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db.screendb;

import edu.harvard.med.screensaver.AbstractSpringTest;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.SchemaUtil;

/**
 * Exercise the entity classes.
 */
public class TestScreenDBSynchronizer extends AbstractSpringTest
{
  protected GenericEntityDAO genericEntityDao;
  protected SchemaUtil schemaUtil;
  protected void onSetUp() throws Exception
  {
    super.onSetUp();
    schemaUtil.truncateTablesOrCreateSchema();
  }
  
  // commenting out the actual tests, since they depend on there being a screendb database with
  // the right connection strings, which is a bit much to ask of a testing env
  /*
  public void testSynchronizer()
  {
    new ScreenDBSynchronizer("localhost", "screendb", "s", "", genericEntityDao).synchronize();
  }
  public void testSynchronizer2x()
  {
    new ScreenDBSynchronizer("localhost", "screendb", "s", "", genericEntityDao).synchronize();
    new ScreenDBSynchronizer("localhost", "screendb", "s", "", genericEntityDao).synchronize();
  }
  */
}
