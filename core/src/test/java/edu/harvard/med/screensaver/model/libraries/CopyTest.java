// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import com.google.common.collect.Sets;
import junit.framework.TestSuite;

import edu.harvard.med.screensaver.model.AbstractEntityInstanceTest;
import edu.harvard.med.screensaver.model.Volume;

public class CopyTest extends AbstractEntityInstanceTest<Copy>
{
  public static TestSuite suite()
  {
    return buildTestSuite(CopyTest.class, Copy.class);
  }
  
  public CopyTest()
  {
    super(Copy.class);
    dataFactory.resetToDefaults();
  }
  
  @SuppressWarnings("deprecation")
  public void testPlates()
  {
    schemaUtil.truncateTables();
    
    Copy copy = dataFactory.newInstance(Copy.class);
    int plateNumber = copy.getLibrary().getStartPlate();
    copy.findPlate(plateNumber).withWellVolume(new Volume(30));
    genericEntityDao.mergeEntity(copy);
    
    Copy copy2 = genericEntityDao.reloadEntity(copy, true, Copy.plates);
    Plate plate2 = Sets.newTreeSet(genericEntityDao.findAllEntitiesOfType(Plate.class, true, Plate.copy)).first();
    assertEquals(copy2.findPlate(plateNumber), plate2);
    assertEquals(plate2.getCopy(), copy2);
  }
}

