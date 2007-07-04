//$HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
//$Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $

//Copyright 2006 by the President and Fellows of Harvard College.

//Screensaver is an open-source project developed by the ICCB-L and NSRB labs
//at Harvard Medical School. This software is distributed under the terms of
//the GNU General Public License.

package edu.harvard.med.screensaver.io.cherrypicks;

import java.io.File;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import edu.harvard.med.screensaver.AbstractSpringTest;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.db.SchemaUtil;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.screens.CherryPickRequest;
import edu.harvard.med.screensaver.model.screens.LabCherryPick;
import edu.harvard.med.screensaver.model.screens.RNAiCherryPickRequest;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;

import org.apache.log4j.Logger;

public class AllCherryPicksImporterTest extends AbstractSpringTest
{
  public static final File TEST_INPUT_FILE_DIR = new File("test/edu/harvard/med/screensaver/io/cherrypicks");
  private static final String ALL_CHERRY_PICKS_TEST_FILE = "AllCherryPicks.xls";


  // static members

  private static Logger log = Logger.getLogger(AllCherryPicksImporterTest.class);

  private static MathContext _mathContext  = new MathContext(4);

  // instance data members

  protected GenericEntityDAO genericEntityDao;
  protected LibrariesDAO librariesDao;
  protected SchemaUtil schemaUtil;
  protected AllCherryPicksImporter allCherryPicksImporter;

  private Library _lib1;
  private Library _lib2;
  private Library _lib3;

  // public constructors and methods

  @Override
  protected void onSetUp() throws Exception
  {
    super.onSetUp();
    schemaUtil.truncateTablesOrCreateSchema();

    _lib1 = new Library("library1", "lib1", ScreenType.RNAI, LibraryType.SIRNA, 50093, 50208);
    _lib2 = new Library("library2", "lib2", ScreenType.RNAI, LibraryType.SIRNA, 50209, 50323);
    _lib3 = new Library("library3", "lib3", ScreenType.RNAI, LibraryType.SIRNA, 50324, 50438);
    genericEntityDao.persistEntity(_lib1);
    genericEntityDao.persistEntity(_lib2);
    genericEntityDao.persistEntity(_lib3);
  }

  public void testImportCherryPickCopies() throws Exception
  {
    final Set<Copy> copies = allCherryPicksImporter.importCherryPickCopies(new File(TEST_INPUT_FILE_DIR, ALL_CHERRY_PICKS_TEST_FILE));
    genericEntityDao.doInTransaction(new DAOTransaction() 
    {
      public void runTransaction() 
      {
        _lib1 = (Library) genericEntityDao.reloadEntity(_lib1);
        _lib2 = (Library) genericEntityDao.reloadEntity(_lib2);
        _lib3 = (Library) genericEntityDao.reloadEntity(_lib3);
        assertEquals("copies count", 6, copies.size());
        assertEquals("lib1 copies count", 2, _lib1.getCopies().size());
        assertEquals("lib2 copies count", 2, _lib2.getCopies().size());
        assertEquals("lib3 copies count", 2, _lib3.getCopies().size());
        Iterator<Copy> iter;
        BigDecimal expectedVolume = new BigDecimal("22.00");

        iter = _lib1.getCopies().iterator();
        Copy copy1 = iter.next();
        Copy copy2 = iter.next();
        assertEquals("lib1 copy names", 
                     new HashSet<String>(Arrays.asList("C", "D")), 
                     new HashSet<String>(Arrays.asList(copy1.getName(), copy2.getName())));
        assertEquals("lib1 copy1 copy info volume", expectedVolume, copy1.getCopyInfo(_lib1.getStartPlate()).getMicroliterWellVolume());
        assertEquals("lib1 copy1 copyinfo count", 116, copy1.getCopyInfos().size());
        assertEquals("lib1 copy2 copyinfo count", 116, copy2.getCopyInfos().size());

        iter = _lib2.getCopies().iterator();
        copy1 = iter.next();
        copy2 = iter.next();
        assertEquals("lib2 copy names", 
                     new HashSet<String>(Arrays.asList("C", "D")), 
                     new HashSet<String>(Arrays.asList(copy1.getName(), copy2.getName())));
        assertEquals("lib2 copy1 copy info volume", expectedVolume, copy1.getCopyInfo(_lib2.getStartPlate()).getMicroliterWellVolume());
        assertEquals("lib2 copy1 copyinfo count", 115, copy1.getCopyInfos().size());
        assertEquals("lib2 copy2 copyinfo count", 115, copy2.getCopyInfos().size());

        iter = _lib3.getCopies().iterator();
        copy1 = iter.next();
        copy2 = iter.next();
        assertEquals("lib3 copy names", 
                     new HashSet<String>(Arrays.asList("C", "D")), 
                     new HashSet<String>(Arrays.asList(copy1.getName(), copy2.getName())));
        assertEquals("lib3 copy1 copy info volume", expectedVolume, copy1.getCopyInfo(_lib3.getStartPlate()).getMicroliterWellVolume());
        assertEquals("lib3 copy1 copyinfo count", 115, copy1.getCopyInfos().size());
        assertEquals("lib3 copy2 copyinfo count", 115, copy2.getCopyInfos().size());
      }
    });
  }
  
  public void testImportCherryPickRequests() throws Exception
  {
    genericEntityDao.doInTransaction(new DAOTransaction() 
    {
      public void runTransaction() 
      {
        try {
          testImportCherryPickCopies(); // setup the database with the library copies
          
          Screen screen1 = MakeDummyEntities.makeDummyScreen(1);
          Screen screen2 = MakeDummyEntities.makeDummyScreen(1);
          RNAiCherryPickRequest cpr1 = new RNAiCherryPickRequest(screen1, screen1.getLeadScreener(), new Date(), 4710);
          RNAiCherryPickRequest cpr2 = new RNAiCherryPickRequest(screen2, screen2.getLeadScreener(), new Date(), 4711);
          cpr1.setMicroliterTransferVolumePerWellApproved(new BigDecimal("12"));
          cpr2.setMicroliterTransferVolumePerWellApproved(new BigDecimal("3"));
          genericEntityDao.persistEntity(screen1);
          genericEntityDao.persistEntity(screen2);

          _lib1 = (Library) genericEntityDao.reloadEntity(_lib1);
          librariesDao.loadOrCreateWellsForLibrary(_lib1);
          _lib2 = (Library) genericEntityDao.reloadEntity(_lib2);
          librariesDao.loadOrCreateWellsForLibrary(_lib2);
          _lib3 = (Library) genericEntityDao.reloadEntity(_lib3);
          librariesDao.loadOrCreateWellsForLibrary(_lib3);
        }
        catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    });
    
    allCherryPicksImporter.importRnaiCherryPicks(new File(TEST_INPUT_FILE_DIR, ALL_CHERRY_PICKS_TEST_FILE));

    CherryPickRequest cpr1 = genericEntityDao.findEntityByProperty(CherryPickRequest.class, 
                                                                   "legacyCherryPickRequestNumber", 
                                                                   4710,
                                                                   true,
                                                                   "screenerCherryPicks",
                                                                   "labCherryPicks");
    assertNotNull("CPR 4710 exists", cpr1);
    assertEquals("CPR 4710 screener cherry pick count", 528, cpr1.getScreenerCherryPicks().size());
    assertEquals("CPR 4710 lab cherry pick count", 528, cpr1.getLabCherryPicks().size());
    for (LabCherryPick labCherryPick : cpr1.getLabCherryPicks()) {
      assertTrue("CPR 4710 lab cherry pick allocated " + labCherryPick, labCherryPick.isAllocated());
    }
    
    CherryPickRequest cpr2 = genericEntityDao.findEntityByProperty(CherryPickRequest.class, 
                                                                   "legacyCherryPickRequestNumber", 
                                                                   4711,
                                                                   true,
                                                                   "screenerCherryPicks",
                                                                   "labCherryPicks");
    assertNotNull("CPR 4711 exists", cpr2);
    assertEquals("CPR 4711 screener cherry pick count", 124, cpr2.getScreenerCherryPicks().size());
    assertEquals("CPR 4711 lab cherry pick count", 124, cpr2.getLabCherryPicks().size());
    for (LabCherryPick labCherryPick : cpr2.getLabCherryPicks()) {
      assertTrue("CPR 4711 lab cherry pick allocated " + labCherryPick, labCherryPick.isAllocated());
    }
    
  }

  // private methods

}

