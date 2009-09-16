// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import java.beans.IntrospectionException;
import java.util.List;

import junit.framework.TestSuite;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.model.AbstractEntityInstanceTest;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.service.libraries.LibraryContentsVersionManager;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public class SilencingReagentTest extends AbstractEntityInstanceTest<SilencingReagent>
{
  protected LibraryContentsVersionManager libraryContentsVersionManager;

  public static TestSuite suite()
  {
    return buildTestSuite(SilencingReagentTest.class, SilencingReagent.class);
  }

  public SilencingReagentTest() throws IntrospectionException
  {
    super(SilencingReagent.class);
  }
  
  public void testPoolToDuplexRelationship()
  {
    schemaUtil.truncateTablesOrCreateSchema();
    
    final AdministratorUser releaseAdmin = dataFactory.newInstance(AdministratorUser.class);
    releaseAdmin.addScreensaverUserRole(ScreensaverUserRole.LIBRARIES_ADMIN);
    genericEntityDao.saveOrUpdateEntity(releaseAdmin);
    
    Library duplexLibrary = dataFactory.newInstance(Library.class);
    duplexLibrary.setLibraryName("duplexLibrary");
    genericEntityDao.persistEntity(duplexLibrary);
    
    libraryContentsVersionManager.createNewContentsVersion(duplexLibrary, releaseAdmin, "");
    
    duplexLibrary = genericEntityDao.findEntityByProperty(Library.class, "libraryName", "duplexLibrary", false, Library.wells.getPath(), Library.contentsVersions.getPath());
    duplexLibrary.setStartPlate(1);
    duplexLibrary.setEndPlate(1);
    duplexLibrary.setScreenType(ScreenType.RNAI);
    duplexLibrary.createWell(new WellKey(duplexLibrary.getStartPlate(), "A01"), LibraryWellType.EXPERIMENTAL)
    .createSilencingReagent(new ReagentVendorIdentifier("vendor", "duplex1.1"), SilencingReagentType.SIRNA, "ATCG");
    duplexLibrary.createWell(new WellKey(duplexLibrary.getStartPlate(), "A02"), LibraryWellType.EXPERIMENTAL)
    .createSilencingReagent(new ReagentVendorIdentifier("vendor", "duplex1.2"), SilencingReagentType.SIRNA, "TCGA");
    genericEntityDao.saveOrUpdateEntity(duplexLibrary); 
    
    libraryContentsVersionManager.releaseLibraryContentsVersion(duplexLibrary.getLatestContentsVersion(), releaseAdmin); 
   
    new DAOTransaction() { 
      public void runTransaction() {
        Library duplexLibrary = genericEntityDao.findEntityByProperty(Library.class, "libraryName", "duplexLibrary");
        List<Well> duplexWells = genericEntityDao.findEntitiesByProperty(Well.class, "library", duplexLibrary); 
        final Library poolLibrary = dataFactory.newInstance(Library.class);
        poolLibrary.setLibraryName("Pool");
        dataFactory.newInstance(LibraryContentsVersion.class, poolLibrary);
        poolLibrary.setStartPlate(2);
        poolLibrary.setEndPlate(2);
        poolLibrary.setScreenType(ScreenType.RNAI);
        Well poolWell = poolLibrary.createWell(new WellKey(poolLibrary.getStartPlate(), "A01"), LibraryWellType.EXPERIMENTAL);
        SilencingReagent poolReagent = poolWell.createSilencingReagent(new ReagentVendorIdentifier("vendor", "pool1"), SilencingReagentType.SIRNA, "ATCG,TCGA");
        poolReagent.withDuplexWell(duplexWells.get(0));
        poolReagent.withDuplexWell(duplexWells.get(1));
        genericEntityDao.persistEntity(poolLibrary);
        libraryContentsVersionManager.releaseLibraryContentsVersion(poolLibrary.getLatestContentsVersion(), releaseAdmin);
      }
    }.runTransaction();
    
    {
      Library poolLibrary = 
        genericEntityDao.findEntityByProperty(Library.class, 
                                              "libraryName", 
                                              "Pool",
                                              true, 
                                              Library.wells.to(Well.latestReleasedReagent).to(SilencingReagent.duplexWells).to(Well.latestReleasedReagent).getPath());
      assertEquals(duplexLibrary.getWells(),
                   ((SilencingReagent) poolLibrary.getWells().iterator().next().getLatestReleasedReagent()).getDuplexWells());
      assertEquals(Sets.newHashSet("ATCG", "TCGA"),
                   Sets.newHashSet(Iterables.transform(((SilencingReagent) poolLibrary.getWells().iterator().next().getLatestReleasedReagent()).getDuplexSilencingReagents(),
                                                       new Function<SilencingReagent,String>() { public String apply(SilencingReagent sr) { return sr.getSequence(); } })));
                   
    }
  }
}

