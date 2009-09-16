// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.service.screens;

import edu.harvard.med.screensaver.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.model.Concentration;
import edu.harvard.med.screensaver.model.ConcentrationUnit;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.VolumeUnit;
import edu.harvard.med.screensaver.model.cherrypicks.RNAiCherryPickRequest;
import edu.harvard.med.screensaver.model.screens.AssayProtocolType;
import edu.harvard.med.screensaver.model.screens.LibraryScreening;
import edu.harvard.med.screensaver.model.screens.RNAiCherryPickScreening;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;

import org.apache.log4j.Logger;
import org.joda.time.LocalDate;

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
        final LibraryScreening libraryScreening1 = screeningDuplicator.addLibraryScreening(_screen);
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
        LibraryScreening libraryScreening2 = screeningDuplicator.addLibraryScreening(_screen);
        assertEquals(2, _screen.getLabActivities().size());
        assertEquals("assay protocol", libraryScreening2.getAssayProtocol()); 
        assertEquals(new LocalDate(2009, 1, 1), libraryScreening2.getAssayProtocolLastModifiedDate()); 
        assertEquals(AssayProtocolType.ESTABLISHED, libraryScreening2.getAssayProtocolType()); 
        assertEquals(new Integer(1), libraryScreening2.getNumberOfReplicates()); 
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
        RNAiCherryPickRequest cpr = (RNAiCherryPickRequest) _screen.createCherryPickRequest();
        genericEntityDao.persistEntity(cpr);
    
        RNAiCherryPickScreening rnaiCpScreening1 = 
          screeningDuplicator.addRnaiCherryPickScreening(_screen, cpr);
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
        RNAiCherryPickScreening rnaiCpScreening2 = 
          screeningDuplicator.addRnaiCherryPickScreening(_screen, cpr);
        assertEquals(2, _screen.getLabActivities().size());
        assertEquals("assay protocol", rnaiCpScreening2.getAssayProtocol()); 
        assertEquals(new LocalDate(2009, 1, 1), rnaiCpScreening2.getAssayProtocolLastModifiedDate()); 
        assertEquals(AssayProtocolType.ESTABLISHED, rnaiCpScreening2.getAssayProtocolType()); 
        assertEquals(new Integer(1), rnaiCpScreening2.getNumberOfReplicates()); 
        assertEquals(new Volume("1.00", VolumeUnit.MILLILITERS), rnaiCpScreening2.getVolumeTransferredPerWell()); 
        assertEquals(new Concentration("2.00", ConcentrationUnit.MILLIMOLAR), rnaiCpScreening2.getConcentration()); 
        assertEquals(_screen.getLabHead(), rnaiCpScreening2.getPerformedBy());
      }
    });
  }
  
}

