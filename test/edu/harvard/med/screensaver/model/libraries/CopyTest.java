// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import java.beans.IntrospectionException;

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
  
  public CopyTest() throws IntrospectionException
  {
    super(Copy.class);
  }
  
  @SuppressWarnings("deprecation")
  public void testPlates()
  {
    schemaUtil.truncateTablesOrCreateSchema();
    
    Copy copy = dataFactory.newInstance(Copy.class);
    int plateNumber = copy.getLibrary().getStartPlate();
    copy.findPlate(plateNumber).withWellVolume(new Volume(30));
    persistEntityNetwork(copy);
    
    Copy copy2 = genericEntityDao.findAllEntitiesOfType(Copy.class, true, Copy.plates.getPath()).get(0);
    Plate plate2 = Sets.newTreeSet(genericEntityDao.findAllEntitiesOfType(Plate.class, true, Plate.copy.getPath())).first();
    assertEquals(copy2.findPlate(plateNumber), plate2);
    assertEquals(plate2.getCopy(), copy2);
  }
}

