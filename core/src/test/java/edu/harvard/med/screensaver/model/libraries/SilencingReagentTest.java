// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import junit.framework.TestSuite;
import org.springframework.beans.factory.annotation.Autowired;

import edu.harvard.med.screensaver.db.EntityInflator;
import edu.harvard.med.screensaver.model.AbstractEntityInstanceTest;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.service.libraries.LibraryContentsVersionManager;

public class SilencingReagentTest extends AbstractEntityInstanceTest<SilencingReagent>
{
  @Autowired
  protected LibraryContentsVersionManager libraryContentsVersionManager;

  public static TestSuite suite()
  {
    return buildTestSuite(SilencingReagentTest.class, SilencingReagent.class);
  }

  public SilencingReagentTest()
  {
    super(SilencingReagent.class);
  }
  
  public void testPoolToDuplexRelationship()
  {
    schemaUtil.truncateTables();
    
    final AdministratorUser releaseAdmin = dataFactory.newInstance(AdministratorUser.class);
    releaseAdmin.addScreensaverUserRole(ScreensaverUserRole.LIBRARIES_ADMIN);
    genericEntityDao.mergeEntity(releaseAdmin);
    
    Library duplexLibrary = dataFactory.newInstance(Library.class);
    duplexLibrary.setLibraryName("duplexLibrary");
    genericEntityDao.mergeEntity(duplexLibrary);
    
    libraryContentsVersionManager.createNewContentsVersion(duplexLibrary, releaseAdmin, "");
    
    duplexLibrary = genericEntityDao.reloadEntity(duplexLibrary, true);
    duplexLibrary = new EntityInflator<Library>(genericEntityDao, duplexLibrary, false).need(Library.wells).need(Library.contentsVersions).inflate();
    duplexLibrary.setStartPlate(1);
    duplexLibrary.setEndPlate(1);
    duplexLibrary.setScreenType(ScreenType.RNAI);
    duplexLibrary.createWell(new WellKey(duplexLibrary.getStartPlate(), "A01"), LibraryWellType.EXPERIMENTAL)
    .createSilencingReagent(new ReagentVendorIdentifier("vendor", "duplex1.1"), SilencingReagentType.SIRNA, "ATCG");
    duplexLibrary.createWell(new WellKey(duplexLibrary.getStartPlate(), "A02"), LibraryWellType.EXPERIMENTAL)
    .createSilencingReagent(new ReagentVendorIdentifier("vendor", "duplex1.2"), SilencingReagentType.SIRNA, "TCGA");
    genericEntityDao.mergeEntity(duplexLibrary);
    
    libraryContentsVersionManager.releaseLibraryContentsVersion(duplexLibrary.getLatestContentsVersion(), releaseAdmin); 
   
    duplexLibrary = genericEntityDao.reloadEntity(duplexLibrary);
    List<Well> duplexWells = genericEntityDao.findEntitiesByProperty(Well.class, "library", duplexLibrary);
    Library poolLibrary = dataFactory.newInstance(Library.class);
    poolLibrary.setLibraryName("Pool");
    poolLibrary.setStartPlate(2);
    poolLibrary.setEndPlate(2);
    poolLibrary.setScreenType(ScreenType.RNAI);
    poolLibrary.createContentsVersion(dataFactory.newInstance(AdministratorUser.class));
    Well poolWell = poolLibrary.createWell(new WellKey(poolLibrary.getStartPlate(), "A01"), LibraryWellType.EXPERIMENTAL);
    SilencingReagent poolReagent = poolWell.createSilencingReagent(new ReagentVendorIdentifier("vendor", "pool1"), SilencingReagentType.SIRNA, "ATCG,TCGA");
    poolReagent.withDuplexWell(duplexWells.get(0));
    poolReagent.withDuplexWell(duplexWells.get(1));
    poolLibrary = genericEntityDao.mergeEntity(poolLibrary);
    libraryContentsVersionManager.releaseLibraryContentsVersion(poolLibrary.getLatestContentsVersion(), releaseAdmin);
    
    poolLibrary =
      genericEntityDao.findEntityByProperty(Library.class,
                                            "libraryName",
                                            "Pool",
                                            true,
                                            Library.wells.to(Well.latestReleasedReagent).to(SilencingReagent.duplexWells).to(Well.latestReleasedReagent));
    assertEquals(Sets.newHashSet(duplexWells),
                 ((SilencingReagent) poolLibrary.getWells().iterator().next().getLatestReleasedReagent()).getDuplexWells());
    assertEquals(Sets.newHashSet("ATCG", "TCGA"),
                 Sets.newHashSet(Iterables.transform(((SilencingReagent) poolLibrary.getWells().iterator().next().getLatestReleasedReagent()).getDuplexSilencingReagents(),
                                                     new Function<SilencingReagent,String>() {
                                                       public String apply(SilencingReagent sr)
                                                       {
                                                         return sr.getSequence();
                                                       }
                                                     })));
  }
}

