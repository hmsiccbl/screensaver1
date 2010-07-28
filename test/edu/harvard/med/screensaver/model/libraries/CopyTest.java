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
    copy.getLibrary().setStartPlate(1);
    copy.getLibrary().setStartPlate(1);
    copy.createPlate(1, "fridge1", PlateType.EPPENDORF, new Volume(30));
    persistEntityNetwork(copy);
    //genericEntityDao.persistEntity(copy.getLibrary());
    
    Copy copy2 = genericEntityDao.findAllEntitiesOfType(Copy.class, true, Copy.plates.getPath()).get(0);
    Plate plate2 = genericEntityDao.findAllEntitiesOfType(Plate.class, true, Plate.copy.getPath()).get(0);
    assertEquals(copy2.getPlates().get(1), plate2);
    assertEquals(plate2.getCopy(), copy2);
  }
}

