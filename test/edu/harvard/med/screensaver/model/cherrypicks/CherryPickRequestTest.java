// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.cherrypicks;

import java.beans.IntrospectionException;
import java.util.List;

import junit.framework.TestSuite;

import edu.harvard.med.screensaver.db.CherryPickRequestDAO;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.model.AbstractEntityInstanceTest;
import edu.harvard.med.screensaver.model.DuplicateEntityException;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.libraries.LibraryWellType;
import edu.harvard.med.screensaver.model.libraries.PlateType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;

import org.joda.time.LocalDate;

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
    CherryPickRequest cherryPickRequest = screen.createCherryPickRequest();
    for (int plateOrdinal = 0; plateOrdinal < 3; ++plateOrdinal) {
      for (int attempt = 0; attempt <= plateOrdinal; ++attempt) {
        cherryPickRequest.createCherryPickAssayPlate(plateOrdinal,
                                                     attempt,
                                                     PlateType.EPPENDORF);
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

  public void testLegacyCherryPickNumber()
  {
    schemaUtil.truncateTablesOrCreateSchema();
    class Txn implements DAOTransaction {
      CherryPickRequest _cherryPickRequest;

      public void runTransaction() {
        Screen screen = MakeDummyEntities.makeDummyScreen(1, ScreenType.RNAI);
        genericEntityDao.persistEntity(screen.getLeadScreener());
        genericEntityDao.persistEntity(screen.getLabHead());
        genericEntityDao.persistEntity(screen);
        _cherryPickRequest = screen.createCherryPickRequest(screen.getLeadScreener(), 
                                                                             new LocalDate(),
                                                                             4000);
        genericEntityDao.persistEntity(_cherryPickRequest);
      }
    };
    Txn txn = new Txn();
    genericEntityDao.doInTransaction(txn);
    CherryPickRequest cherryPickRequest2 = genericEntityDao.findEntityById(CherryPickRequest.class, txn._cherryPickRequest.getEntityId());
    assertEquals("cherryPickRequestNumber", new Integer(4000), cherryPickRequest2.getCherryPickRequestNumber());
  }

}

