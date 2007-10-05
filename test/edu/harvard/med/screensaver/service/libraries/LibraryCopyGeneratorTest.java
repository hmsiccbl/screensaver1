// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.service.libraries;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.harvard.med.screensaver.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.io.screenresults.ExtantLibraryException;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.CopyInfo;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.PlateType;
import edu.harvard.med.screensaver.model.screens.ScreenType;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;

public class LibraryCopyGeneratorTest extends AbstractSpringPersistenceTest
{
  // static members

  private static Logger log = Logger.getLogger(LibraryCopyGeneratorTest.class);

  // instance data members
  
  protected LibraryCopyGenerator libraryCopyGenerator;
  protected LibrariesDAO librariesDao;
  private Library _library;


  // public constructors and methods
  
  @Override
  protected void onSetUp() throws Exception
  {
    super.onSetUp();
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        _library = new Library("library", "lib", ScreenType.RNAI, LibraryType.COMMERCIAL, 2, 5);
        genericEntityDao.persistEntity(_library);
      }
    });
  }

  public void testCreatePlateCopy() throws ExtantLibraryException
  {
    Date today = new Date();
    BigDecimal volume = new BigDecimal("22.00");

    try {
      libraryCopyGenerator.createPlateCopies(1, Arrays.asList("A"), volume, PlateType.EPPENDORF, today);
      fail("expected ExtantLibraryException");
    }
    catch (ExtantLibraryException e) {}

    List<Integer> plateNumbers = Arrays.asList(2, 3, 5);
    List<String> copyNames = Arrays.asList("A", "B");
    List<CopyInfo> copyInfos = libraryCopyGenerator.createPlateCopies(plateNumbers, copyNames, volume, PlateType.EPPENDORF, today);
    assertEquals("copyInfos size", plateNumbers.size() * copyNames.size(), copyInfos.size());
    Iterator<CopyInfo> copyInfoIter = copyInfos.iterator();
    for (Integer expectedPlateNumber : plateNumbers) {
      for (String expectedCopyName : copyNames) {
        CopyInfo copyInfo = copyInfoIter.next();
        assertEquals(expectedPlateNumber, copyInfo.getPlateNumber());
        assertEquals(expectedCopyName, copyInfo.getCopy().getName());
        assertEquals(volume, copyInfo.getMicroliterWellVolume());
        assertEquals(PlateType.EPPENDORF, copyInfo.getPlateType());
        assertEquals(DateUtils.truncate(today, Calendar.DATE), copyInfo.getDatePlated());
      }
    }
    
    Library library = genericEntityDao.findEntityByProperty(Library.class, "libraryName", "library", true, "copies.copyInfos");
    Set<Copy> libraryCopies = library.getCopies();
    assertEquals("persisted library copies count", copyNames.size(), libraryCopies.size());
    
  }

  // private methods
    
}
