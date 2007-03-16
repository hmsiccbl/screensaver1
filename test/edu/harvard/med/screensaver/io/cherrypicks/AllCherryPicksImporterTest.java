//$HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
//$Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $

//Copyright 2006 by the President and Fellows of Harvard College.

//Screensaver is an open-source project developed by the ICCB-L and NSRB labs
//at Harvard Medical School. This software is distributed under the terms of
//the GNU General Public License.

package edu.harvard.med.screensaver.io.cherrypicks;

import java.io.File;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import edu.harvard.med.screensaver.AbstractSpringTest;
import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.SchemaUtil;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.screens.ScreenType;

import org.apache.log4j.Logger;

public class AllCherryPicksImporterTest extends AbstractSpringTest
{
  public static final File TEST_INPUT_FILE_DIR = new File("test/edu/harvard/med/screensaver/io/cherrypicks");
  private static final String ALL_CHERRY_PICKS_TEST_FILE = "AllCherryPicks.xls";


  // static members

  private static Logger log = Logger.getLogger(AllCherryPicksImporterTest.class);


  // instance data members

  protected DAO dao;
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
    dao.persistEntity(_lib1);
    dao.persistEntity(_lib2);
    dao.persistEntity(_lib3);
  }

  public void testImportCherryPickCopies() throws Exception
  {
    final Set<Copy> copies = allCherryPicksImporter.importCherryPickCopies(new File(TEST_INPUT_FILE_DIR, ALL_CHERRY_PICKS_TEST_FILE));
    dao.doInTransaction(new DAOTransaction() 
    {
      public void runTransaction() 
      {
        _lib1 = (Library) dao.reloadEntity(_lib1);
        _lib2 = (Library) dao.reloadEntity(_lib2);
        _lib3 = (Library) dao.reloadEntity(_lib3);
        assertEquals("copies count", 6, copies.size());
        assertEquals("lib1 copies count", 2, _lib1.getCopies().size());
        assertEquals("lib2 copies count", 2, _lib2.getCopies().size());
        assertEquals("lib3 copies count", 2, _lib3.getCopies().size());
        Iterator<Copy> iter;
        BigDecimal expectedVolume = new BigDecimal(22);
        expectedVolume.setScale(2);

        iter = _lib1.getCopies().iterator();
        Copy copy1 = iter.next();
        Copy copy2 = iter.next();
        assertEquals("lib1 copy names", 
                     new HashSet<String>(Arrays.asList("C", "D")), 
                     new HashSet<String>(Arrays.asList(copy1.getName(), copy2.getName())));
        assertEquals("lib1 copy1 copy info volume", expectedVolume, copy1.getCopyInfo(_lib1.getStartPlate()).getVolume());
        assertEquals("lib1 copy1 copyinfo count", 116, copy1.getCopyInfos().size());
        assertEquals("lib1 copy2 copyinfo count", 116, copy2.getCopyInfos().size());

        iter = _lib2.getCopies().iterator();
        copy1 = iter.next();
        copy2 = iter.next();
        assertEquals("lib2 copy names", 
                     new HashSet<String>(Arrays.asList("C", "D")), 
                     new HashSet<String>(Arrays.asList(copy1.getName(), copy2.getName())));
        assertEquals("lib2 copy1 copy info volume", expectedVolume, copy1.getCopyInfo(_lib2.getStartPlate()).getVolume());
        assertEquals("lib2 copy1 copyinfo count", 115, copy1.getCopyInfos().size());
        assertEquals("lib2 copy2 copyinfo count", 115, copy2.getCopyInfos().size());

        iter = _lib3.getCopies().iterator();
        copy1 = iter.next();
        copy2 = iter.next();
        assertEquals("lib3 copy names", 
                     new HashSet<String>(Arrays.asList("C", "D")), 
                     new HashSet<String>(Arrays.asList(copy1.getName(), copy2.getName())));
        assertEquals("lib3 copy1 copy info volume", expectedVolume, copy1.getCopyInfo(_lib3.getStartPlate()).getVolume());
        assertEquals("lib3 copy1 copyinfo count", 115, copy1.getCopyInfos().size());
        assertEquals("lib3 copy2 copyinfo count", 115, copy2.getCopyInfos().size());
      }
    });
  }

  // private methods

}

