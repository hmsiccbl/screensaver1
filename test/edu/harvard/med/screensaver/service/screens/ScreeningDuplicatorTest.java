// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.service.screens;

import org.apache.log4j.Logger;
import org.joda.time.LocalDate;

import edu.harvard.med.screensaver.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.model.Concentration;
import edu.harvard.med.screensaver.model.ConcentrationUnit;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.VolumeUnit;
import edu.harvard.med.screensaver.model.cherrypicks.RNAiCherryPickRequest;
import edu.harvard.med.screensaver.model.screens.AssayProtocolType;
import edu.harvard.med.screensaver.model.screens.CherryPickScreening;
import edu.harvard.med.screensaver.model.screens.LibraryScreening;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;

public class ScreeningDuplicatorTest extends AbstractSpringPersistenceTest
{
  private static Logger log = Logger.getLogger(ScreeningDuplicatorTest.class);

  protected ScreeningDuplicator screeningDuplicator;

  private Screen _screen;
  
  @Override
  protected void onSetUp() throws Exception
  {
    super.onSetUp();
    _screen = MakeDummyEntities.makeDummyScreen(1, ScreenType.RNAI);
    genericEntityDao.persistEntity(_screen);
  }


  public void testAddLibraryScreening()
  {
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        genericEntityDao.reattachEntity(_screen);
        final LibraryScreening libraryScreening1 = screeningDuplicator.addLibraryScreening(_screen, (AdministratorUser) _screen.getCreatedBy());
        _screen = libraryScreening1.getScreen();
        assertEquals(1, _screen.getLabActivities().size());
        assertNull(libraryScreening1.getAssayProtocol()); 
        assertNull(libraryScreening1.getAssayProtocolLastModifiedDate()); 
        assertNull(libraryScreening1.getAssayProtocolType()); 
        assertNull(libraryScreening1.getNumberOfReplicates()); 
        assertNull(libraryScreening1.getVolumeTransferredPerWell()); 
        assertNull(libraryScreening1.getConcentration());
        assertEquals(_screen.getLeadScreener(), libraryScreening1.getPerformedBy());

        // setup for next part of test
        libraryScreening1.setAssayProtocol("assay protocol");
        libraryScreening1.setAssayProtocolLastModifiedDate(new LocalDate(2009, 1, 1));
        libraryScreening1.setAssayProtocolType(AssayProtocolType.ESTABLISHED);
        libraryScreening1.setNumberOfReplicates(1);
        libraryScreening1.setVolumeTransferredPerWell(new Volume("1.00", VolumeUnit.MILLILITERS));
        libraryScreening1.setConcentration(new Concentration("2.00", ConcentrationUnit.MILLIMOLAR));
        libraryScreening1.setPerformedBy(_screen.getLabHead());
        genericEntityDao.saveOrUpdateEntity(libraryScreening1);
      }
    });
    
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        genericEntityDao.reattachEntity(_screen);
        LibraryScreening libraryScreening2 = screeningDuplicator.addLibraryScreening(_screen, (AdministratorUser) _screen.getCreatedBy());
        assertEquals(2, _screen.getLabActivities().size());
        assertEquals("assay protocol", libraryScreening2.getAssayProtocol()); 
        assertEquals(new LocalDate(2009, 1, 1), libraryScreening2.getAssayProtocolLastModifiedDate()); 
        assertEquals(AssayProtocolType.ESTABLISHED, libraryScreening2.getAssayProtocolType()); 
        assertEquals(Integer.valueOf(1), libraryScreening2.getNumberOfReplicates());
        assertEquals(new Volume("1.00", VolumeUnit.MILLILITERS), libraryScreening2.getVolumeTransferredPerWell()); 
        assertEquals(new Concentration("2.00", ConcentrationUnit.MILLIMOLAR), libraryScreening2.getConcentration()); 
        assertEquals(_screen.getLabHead(), libraryScreening2.getPerformedBy());
      }
    });
  }
  
  public void testAddRnaiCherryPickScreening()
  {
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        genericEntityDao.reattachEntity(_screen);
        genericEntityDao.persistEntity(_screen.getCreatedBy());
        /*RNAiCherryPickRequest cpr = (RNAiCherryPickRequest)*/ _screen.createCherryPickRequest((AdministratorUser) _screen.getCreatedBy());
      }
    });
    
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        genericEntityDao.reattachEntity(_screen);
        RNAiCherryPickRequest cpr = (RNAiCherryPickRequest) _screen.getCherryPickRequests().iterator().next();
    
        CherryPickScreening rnaiCpScreening1 = 
          screeningDuplicator.addCherryPickScreening(_screen, cpr.getRequestedBy(), (AdministratorUser) _screen.getCreatedBy(), cpr);
        assertEquals(1, _screen.getLabActivities().size());
        assertNull(rnaiCpScreening1.getAssayProtocol()); 
        assertNull(rnaiCpScreening1.getAssayProtocolLastModifiedDate()); 
        assertNull(rnaiCpScreening1.getAssayProtocolType()); 
        assertNull(rnaiCpScreening1.getNumberOfReplicates()); 
        assertNull(rnaiCpScreening1.getVolumeTransferredPerWell()); 
        assertNull(rnaiCpScreening1.getConcentration());
        assertEquals(cpr.getRequestedBy(), rnaiCpScreening1.getPerformedBy());

        // setup for next part of test
        rnaiCpScreening1.setAssayProtocol("assay protocol");
        rnaiCpScreening1.setAssayProtocolLastModifiedDate(new LocalDate(2009, 1, 1));
        rnaiCpScreening1.setAssayProtocolType(AssayProtocolType.ESTABLISHED);
        rnaiCpScreening1.setNumberOfReplicates(1);
        rnaiCpScreening1.setVolumeTransferredPerWell(new Volume("1.00", VolumeUnit.MILLILITERS));
        rnaiCpScreening1.setConcentration(new Concentration("2.00", ConcentrationUnit.MILLIMOLAR));
        rnaiCpScreening1.setPerformedBy(_screen.getLabHead());
        genericEntityDao.saveOrUpdateEntity(rnaiCpScreening1);
      }
    });
    
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        genericEntityDao.reattachEntity(_screen);
        RNAiCherryPickRequest cpr = (RNAiCherryPickRequest) _screen.getCherryPickRequests().iterator().next();
        CherryPickScreening rnaiCpScreening2 = 
          screeningDuplicator.addCherryPickScreening(_screen, cpr.getRequestedBy(), (AdministratorUser) _screen.getCreatedBy(), cpr);
        assertEquals(2, _screen.getLabActivities().size());
        assertEquals("assay protocol", rnaiCpScreening2.getAssayProtocol()); 
        assertEquals(new LocalDate(2009, 1, 1), rnaiCpScreening2.getAssayProtocolLastModifiedDate()); 
        assertEquals(AssayProtocolType.ESTABLISHED, rnaiCpScreening2.getAssayProtocolType()); 
        assertEquals(Integer.valueOf(1), rnaiCpScreening2.getNumberOfReplicates());
        assertEquals(new Volume("1.00", VolumeUnit.MILLILITERS), rnaiCpScreening2.getVolumeTransferredPerWell()); 
        assertEquals(new Concentration("2.00", ConcentrationUnit.MILLIMOLAR), rnaiCpScreening2.getConcentration()); 
        assertEquals(_screen.getLabHead(), rnaiCpScreening2.getPerformedBy());
      }
    });
  }
  
}

