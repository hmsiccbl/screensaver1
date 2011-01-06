// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.cherrypicks;

import java.beans.IntrospectionException;
import java.util.List;

import junit.framework.TestSuite;

import edu.harvard.med.screensaver.db.CherryPickRequestDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.model.AbstractEntityInstanceTest;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.libraries.PlateType;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;

public abstract class CherryPickRequestTest<CPR extends CherryPickRequest> extends AbstractEntityInstanceTest<CPR>
{
  public static TestSuite suite()
  {
    return buildTestSuite(CherryPickRequestTest.class, CherryPickRequest.class);
  }

  protected CherryPickRequestDAO cherryPickRequestDao;
  protected LibrariesDAO librariesDao;
  
  public CherryPickRequestTest(Class<CPR> clazz) throws IntrospectionException
  {
    super(clazz);
  }

  public void testGetActiveCherryPickAssayPlates()
  {
    schemaUtil.truncateTablesOrCreateSchema();
    Screen screen = MakeDummyEntities.makeDummyScreen(1, ScreenType.RNAI);
    CherryPickRequest cherryPickRequest = screen.createCherryPickRequest((AdministratorUser) screen.getCreatedBy());
    for (int plateOrdinal = 0; plateOrdinal < 3; ++plateOrdinal) {
      for (int attempt = 0; attempt <= plateOrdinal; ++attempt) {
        cherryPickRequest.createCherryPickAssayPlate(plateOrdinal,
                                                     attempt,
                                                     PlateType.EPPENDORF_384);
      }
    }
    List<CherryPickAssayPlate> activeAssayPlates = cherryPickRequest.getActiveCherryPickAssayPlates();
    assertEquals(3, activeAssayPlates.size()); 
    int expectedAttemptOrdinal = 0;
    for (CherryPickAssayPlate activeAssayPlate : activeAssayPlates) {
      assertEquals("active assay plate is the last one attempted", 
                   expectedAttemptOrdinal++, 
                   activeAssayPlate.getAttemptOrdinal().intValue());
    }
  }
  
//  public void testAssayPlateRequiringSourcePlateReload()
//  {
//    fail("not implemented");
//  }
}

