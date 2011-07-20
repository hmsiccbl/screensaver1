// $HeadURL$
// $Id$
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.io.IOException;

import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.datafetcher.TupleToKeyFunction;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.test.MakeDummyEntities;
import edu.harvard.med.screensaver.ui.arch.view.AbstractBackingBeanTest;

public class ReagentFinderTest extends AbstractBackingBeanTest
{
  private static Logger log = Logger.getLogger(ReagentFinderTest.class);

  @Autowired
  protected ReagentFinder reagentFinder;
  @Autowired
  protected WellSearchResults wellsBrowser;

  private Library _library1;
  private Library _library2;

  protected void setUp() throws Exception
  {
    super.setUp();
    genericEntityDao.doInTransaction(new DAOTransaction() {
      @Override
      public void runTransaction()
      {
        _admin = genericEntityDao.reattachEntity(_admin);
        currentScreensaverUser.setScreensaverUser(_admin);
        genericEntityDao.persistEntity(_admin);
        _library1 = MakeDummyEntities.makeDummyLibrary(1, ScreenType.SMALL_MOLECULE, 1);
        
        for(Well well:_library1.getWells()) {
          if(well.getWellId().equals("01000:A03"))
          {
            well.setFacilityId("HMSL100005-002-01");
           log.info("setting: " + well + "," + well.getFacilityId());
           }
          if(well.getWellId().equals("01000:A04"))
          {
            well.setFacilityId("HMSL100015-002-01");
            log.info("setting: " + well + "," + well.getFacilityId());
          }
        }
        
        genericEntityDao.persistEntity(_library1);
        _library2 = MakeDummyEntities.makeDummyLibrary(2, ScreenType.SMALL_MOLECULE, 1);
        for(Well well:_library2.getWells()) {
          if(well.getWellId().equals("02000:A03"))
          {
            well.setFacilityId("HMSL100005-001-01"); 
            log.info("setting: " + well + "," + well.getFacilityId());
          }
          if(well.getWellId().equals("02000:A04"))
          {
            well.setFacilityId("HMSL100015-001-01");
            ((SmallMoleculeReagent)well.getLatestReleasedReagent()).getCompoundNames().add("xyzcompound");
            log.info("setting: " + well + "," + well.getFacilityId());
          }
        }
        genericEntityDao.persistEntity(_library2);
      }
    });
  }

  public void testReagentFinder() throws IOException
  {
    reagentFinder.setReagentIdentifiers("\n sm1\n\nsm3 \nxxx\n");
    reagentFinder.findReagentsByVendorIdentifier();
    wellsBrowser.getRowCount();
    assertEquals(Sets.newHashSet("01000:A02", "02000:A02", "01000:A04", "02000:A04"),
                 Sets.newHashSet(Iterators.transform(wellsBrowser.getDataTableModel().iterator(), new TupleToKeyFunction<String>())));
  }
  
  public void testFindReagentsByNameFacilityVendorID() 
  {
    reagentFinder.setNameFacilityVendorIDInput("100005-002\n100015-001");
    reagentFinder.findWellsByNameFacilityVendorID();
    wellsBrowser.getRowCount();
    assertEquals(Sets.newHashSet("01000:A03", "02000:A04"),
                 Sets.newHashSet(Iterators.transform(wellsBrowser.getDataTableModel().iterator(), new TupleToKeyFunction<String>())));

    reagentFinder.setNameFacilityVendorIDInput("100005");
    reagentFinder.findWellsByNameFacilityVendorID();
    wellsBrowser.getRowCount();
    assertEquals(Sets.newHashSet("01000:A03", "02000:A03"),
                 Sets.newHashSet(Iterators.transform(wellsBrowser.getDataTableModel().iterator(), new TupleToKeyFunction<String>())));

    reagentFinder.setNameFacilityVendorIDInput("100005\nxyz");
    reagentFinder.findWellsByNameFacilityVendorID();
    wellsBrowser.getRowCount();
    assertEquals(Sets.newHashSet("01000:A03", "02000:A03", "02000:A04"),
                 Sets.newHashSet(Iterators.transform(wellsBrowser.getDataTableModel().iterator(), new TupleToKeyFunction<String>())));
    
  }
  
}
