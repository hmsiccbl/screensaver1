// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import java.beans.IntrospectionException;

import junit.framework.TestSuite;

import edu.harvard.med.screensaver.model.AbstractEntityInstanceTest;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.cherrypicks.LabCherryPick;

public class WellVolumeAdjustmentTest extends AbstractEntityInstanceTest<WellVolumeAdjustment>
{
  public static TestSuite suite()
  {
    return buildTestSuite(WellVolumeAdjustmentTest.class, WellVolumeAdjustment.class);
  }

  public WellVolumeAdjustmentTest()
  {
    super(WellVolumeAdjustment.class);
  }
  
  public void testLabCherryPickRelationship()
  {
    schemaUtil.truncateTables();
    Copy copy = dataFactory.newInstance(Copy.class);
    Well well = dataFactory.newInstance(Well.class);
    Volume volume = dataFactory.newInstance(Volume.class);
    LabCherryPick labCherryPick = dataFactory.newInstance(LabCherryPick.class);
    WellVolumeAdjustment wellVolumeAdjustment = new WellVolumeAdjustment(copy, well, volume, labCherryPick);
    wellVolumeAdjustment = genericEntityDao.mergeEntity(wellVolumeAdjustment);

    labCherryPick = genericEntityDao.reloadEntity(labCherryPick);
    wellVolumeAdjustment = genericEntityDao.reloadEntity(wellVolumeAdjustment, true, WellVolumeAdjustment.labCherryPick);
    assertEquals(labCherryPick, wellVolumeAdjustment.getLabCherryPick());
  }
  
  public void testWellVolumeCorrectActivityRelationship()
  {
    schemaUtil.truncateTables();
    Copy copy = dataFactory.newInstance(Copy.class);
    Well well = dataFactory.newInstance(Well.class);
    Volume volume = dataFactory.newInstance(Volume.class);
    WellVolumeCorrectionActivity wellVolumeCorrectionActivity = dataFactory.newInstance(WellVolumeCorrectionActivity.class);
    WellVolumeAdjustment wellVolumeAdjustment = new WellVolumeAdjustment(copy, well, volume, wellVolumeCorrectionActivity);
    wellVolumeAdjustment = genericEntityDao.mergeEntity(wellVolumeAdjustment);
    
    wellVolumeCorrectionActivity = genericEntityDao.reloadEntity(wellVolumeCorrectionActivity);
    wellVolumeAdjustment = genericEntityDao.reloadEntity(wellVolumeAdjustment, true, WellVolumeAdjustment.wellVolumeCorrectionActivity);
    assertEquals(wellVolumeCorrectionActivity, wellVolumeAdjustment.getWellVolumeCorrectionActivity());
  }
}

