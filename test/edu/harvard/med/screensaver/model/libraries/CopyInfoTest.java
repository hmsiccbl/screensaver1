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
import java.math.BigDecimal;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.model.AbstractEntityInstanceTest;
import edu.harvard.med.screensaver.model.screens.ScreenType;

import org.apache.log4j.Logger;

public class CopyInfoTest extends AbstractEntityInstanceTest
{
  // static members

  private static Logger log = Logger.getLogger(CopyInfoTest.class);


  // instance data members

  
  // public constructors and methods

  public CopyInfoTest() throws IntrospectionException
  {
    super(CopyInfo.class);
  }
  
  public void testMicroliterWellVolume()
  {
    final BigDecimal defaultWellVolume = new BigDecimal("10.0");
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        Library library = new Library("library", "lib", ScreenType.RNAI, LibraryType.COMMERCIAL, 1, 2);
        for (int plateNumber = library.getStartPlate(); plateNumber <= library.getEndPlate(); plateNumber++) {
          for (int iRow = 0; iRow < Well.PLATE_ROWS; iRow++) {
            for (int iCol = 0; iCol < Well.PLATE_COLUMNS; iCol++) {
              new Well(library, new WellKey(plateNumber, new WellName(iRow, iCol)), WellType.EXPERIMENTAL);
            }
          }
        }
        Copy copy = new Copy(library, CopyUsageType.FOR_CHERRY_PICK_SCREENING, "C");
        CopyInfo copyInfo1 = new CopyInfo(copy, 1, "loc1", PlateType.EPPENDORF, defaultWellVolume);
        copyInfo1.setMicroliterWellVolume(new WellKey(1, "A01"), new BigDecimal("9.0"));
        CopyInfo copyInfo2 = new CopyInfo(copy, 2, "loc2", PlateType.EPPENDORF, defaultWellVolume);
        copyInfo2.setMicroliterWellVolume(new WellKey(2, "P24"), new BigDecimal("11.0"));
        genericEntityDao.persistEntity(library);
      }
    });
    
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        Library library = genericEntityDao.findEntityByProperty(Library.class, "libraryName", "library");
        Copy copy = library.getCopies().iterator().next();
        for (CopyInfo copyInfo : copy.getCopyInfos()) {
          for (int iRow = 0; iRow < Well.PLATE_ROWS; iRow++) {
            for (int iCol = 0; iCol < Well.PLATE_COLUMNS; iCol++) {
              WellKey wellKey = new WellKey(copyInfo.getPlateNumber(), iRow, iCol);
              if (wellKey.equals(new WellKey(1, "A01"))) {
                assertEquals("plate 1, A01 well volume", new BigDecimal("9.0"), copyInfo.getMicroliterWellVolume(wellKey));
              }
              else if (wellKey.equals(new WellKey(2, "P24"))) {
                assertEquals("plate 1, A01 well volume", new BigDecimal("11.0"), copyInfo.getMicroliterWellVolume(wellKey));
              } 
              else {
                assertEquals("default well volume", defaultWellVolume, copyInfo.getMicroliterWellVolume(wellKey));
              }                
            }
          }
        }
      }
    });
  }
}

