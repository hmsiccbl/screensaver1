// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import java.beans.IntrospectionException;

import junit.framework.TestSuite;

import edu.harvard.med.screensaver.model.AbstractEntityInstanceTest;
import edu.harvard.med.screensaver.model.EntityNetworkPersister;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.cherrypicks.LabCherryPick;

public class WellVolumeAdjustmentTest extends AbstractEntityInstanceTest<WellVolumeAdjustment>
{
  public static TestSuite suite()
  {
    return buildTestSuite(WellVolumeAdjustmentTest.class, WellVolumeAdjustment.class);
  }

  public WellVolumeAdjustmentTest() throws IntrospectionException
  {
    super(WellVolumeAdjustment.class);
  }
  
  public void testLabCherryPickRelationship()
  {
    schemaUtil.truncateTablesOrCreateSchema();
    Copy copy = dataFactory.newInstance(Copy.class);
    Well well = dataFactory.newInstance(Well.class);
    Volume volume = dataFactory.getTestValueForType(Volume.class);
    LabCherryPick labCherryPick = dataFactory.newInstance(LabCherryPick.class);
    WellVolumeAdjustment wellVolumeAdjustment = new WellVolumeAdjustment(copy, well, volume, labCherryPick);
    new EntityNetworkPersister(genericEntityDao, wellVolumeAdjustment).persistEntityNetwork();

    labCherryPick = genericEntityDao.reloadEntity(labCherryPick);
    WellVolumeAdjustment wellVolumeAdjustment2 = genericEntityDao.findEntityById(WellVolumeAdjustment.class, wellVolumeAdjustment.getEntityId(), true, "labCherryPick");
    assertEquals(labCherryPick, wellVolumeAdjustment2.getLabCherryPick());
  }
  
  public void testWellVolumeCorrectActivityRelationship()
  {
    schemaUtil.truncateTablesOrCreateSchema();
    Copy copy = dataFactory.newInstance(Copy.class);
    Well well = dataFactory.newInstance(Well.class);
    Volume volume = dataFactory.getTestValueForType(Volume.class);
    WellVolumeCorrectionActivity wellVolumeCorrectionActivity = dataFactory.newInstance(WellVolumeCorrectionActivity.class);
    WellVolumeAdjustment wellVolumeAdjustment = new WellVolumeAdjustment(copy, well, volume, wellVolumeCorrectionActivity);
    new EntityNetworkPersister(genericEntityDao, wellVolumeAdjustment).persistEntityNetwork();
    
    wellVolumeCorrectionActivity = genericEntityDao.reloadEntity(wellVolumeCorrectionActivity);
    WellVolumeAdjustment wellVolumeAdjustment2 = genericEntityDao.findEntityById(WellVolumeAdjustment.class, wellVolumeAdjustment.getEntityId(), true, "wellVolumeCorrectionActivity");
    assertEquals(wellVolumeCorrectionActivity, wellVolumeAdjustment2.getWellVolumeCorrectionActivity());
  }
}

