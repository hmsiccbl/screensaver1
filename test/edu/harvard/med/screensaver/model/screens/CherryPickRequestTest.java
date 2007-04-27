// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;

import java.beans.IntrospectionException;
import java.util.Date;
import java.util.List;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.io.screenresults.MockDaoForScreenResultImporter;
import edu.harvard.med.screensaver.model.AbstractEntityInstanceTest;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.CopyUsageType;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.PlateType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUserClassification;

import org.apache.log4j.Logger;

public class CherryPickRequestTest extends AbstractEntityInstanceTest
{
  // static members

  private static Logger log = Logger.getLogger(CherryPickRequestTest.class);


  // instance data members

  
  // public constructors and methods

  public CherryPickRequestTest() throws IntrospectionException
  {
    super(CherryPickRequest.class);
  }
  
  public void testGetActiveCherryPickAssayPlates()
  {
    Screen screen = MockDaoForScreenResultImporter.makeDummyScreen(1);
    CherryPickRequest cherryPickRequest = new RNAiCherryPickRequest(screen, 
                                                                    screen.getLeadScreener(), 
                                                                    new Date());
    for (int plateOrdinal = 0; plateOrdinal < 3; ++plateOrdinal) {
      for (int attempt = 0; attempt <= plateOrdinal; ++attempt) {
        new CherryPickAssayPlate(cherryPickRequest,
                                 plateOrdinal,
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
  
  public void testAssayPlateRequiringSourcePlateReload()
  {
    fail("not implemented");
  }

  public void testDeleteCompoundCherryPick()
  {
    schemaUtil.truncateTablesOrCreateSchema();
    final Integer cherryPickRequestNumber = 2504;
    final Integer screenNumber = 900;
    dao.doInTransaction(new DAOTransaction()
    {
      // create
      public void runTransaction()
      {
        Library library = new Library("name", "short", ScreenType.SMALL_MOLECULE, LibraryType.DOS,
          5, 5);
        dao.persistEntity(library);
        dao.loadOrCreateWellsForLibrary(library);
        Well well = library.getWells().iterator().next();
        ScreeningRoomUser user = new ScreeningRoomUser(
          new Date(), "joe", "user",  "email", "phone", "addr", "comments", "ecommons",
          "harvardId", ScreeningRoomUserClassification.GRADUATE_STUDENT, false);
        dao.persistEntity(user);
        Screen screen = new Screen(user, user, screenNumber, new Date(), ScreenType.SMALL_MOLECULE, "title");
        dao.persistEntity(screen);
        
        CompoundCherryPickRequest request =
          new CompoundCherryPickRequest(screen, user, new Date(), cherryPickRequestNumber);
        CherryPickAssayPlate plate = new CherryPickAssayPlate(request, 1, 0 , PlateType.ABGENE);
        ScreenerCherryPick screenerCherryPick = new ScreenerCherryPick(request, well);
        LabCherryPick labCherryPick = new LabCherryPick(screenerCherryPick, well);
        labCherryPick.setAllocated(new Copy(library, CopyUsageType.FOR_CHERRY_PICK_SCREENING, "A"));
        labCherryPick.setMapped(plate, 0, 0);
        
        CherryPickLiquidTransfer transfer =
          new CherryPickLiquidTransfer(user, new Date(), new Date(), request);
        transfer.addCherryPickAssayPlate(plate);

        dao.persistEntity(request);
      }
    });
    
    // delete
    dao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        CompoundCherryPickRequest request =
          dao.findEntityByProperty(CompoundCherryPickRequest.class, "legacyCherryPickRequestNumber", cherryPickRequestNumber);
        dao.deleteCherryPickRequest(request, true);
      }
    });

    // test that its gone
    dao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        CompoundCherryPickRequest request =
          dao.findEntityByProperty(CompoundCherryPickRequest.class, "legacyCherryPickRequestNumber", cherryPickRequestNumber);
        assertNull("cherry pick request is deleted", request);
        
        Screen screen = dao.findEntityByProperty(Screen.class, "hbnScreenNumber", screenNumber);
        assertNotNull("screen is not deleted", screen);
        assertEquals("screen has no activities", 0, screen.getScreeningRoomActivities().size());
      }
    });
  }
  
  public void testLegacyCherryPickNumber()
  {
    Screen screen = MockDaoForScreenResultImporter.makeDummyScreen(1);
    CherryPickRequest cherryPickRequest = new RNAiCherryPickRequest(screen, 
                                                                    screen.getLeadScreener(), 
                                                                    new Date(),
                                                                    4000);
    dao.persistEntity(cherryPickRequest);
    
    CherryPickRequest cherryPickRequest2 = dao.findEntityById(CherryPickRequest.class, cherryPickRequest.getEntityId());
    assertEquals("cherryPickRequestNumber", new Integer(4000), cherryPickRequest2.getCherryPickRequestNumber());
  }
  
  public void testFindCherryPickRequestByNumber()
  {
    schemaUtil.truncateTablesOrCreateSchema();
    Screen screen = MockDaoForScreenResultImporter.makeDummyScreen(1);
    CherryPickRequest cherryPickRequest1 = new RNAiCherryPickRequest(screen, 
                                                                     screen.getLeadScreener(), 
                                                                     new Date(),
                                                                     4000);
    CherryPickRequest cherryPickRequest2 = new RNAiCherryPickRequest(screen, 
                                                                     screen.getLeadScreener(), 
                                                                     new Date());
    dao.persistEntity(screen);

    CherryPickRequest foundCherryPickRequest1 = 
      dao.findCherryPickRequestByNumber(4000);
    assertEquals("found legacy cherryPickRequest", 
                 cherryPickRequest1.getCherryPickRequestNumber(), 
                 foundCherryPickRequest1.getCherryPickRequestNumber());

    CherryPickRequest foundCherryPickRequest2 = 
      dao.findCherryPickRequestByNumber(cherryPickRequest2.getCherryPickRequestNumber());
    assertEquals("found legacy cherryPickRequest", 
                 cherryPickRequest2.getCherryPickRequestNumber(), 
                 foundCherryPickRequest2.getCherryPickRequestNumber());
  }
}

