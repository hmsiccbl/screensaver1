// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.reports.icbg;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.Well;


/**
 * Maps from an ICCB-L Plate-Well (as represented by a {@link Well}) to an
 * INBio LQ.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class ICCBLPlateWellToINBioLQMapper
{

  // static fields
  
  private static Logger log = Logger.getLogger(ICCBLPlateWellToINBioLQMapper.class);
  private static String _mappingFilename =
    "icbg-support/ICCB-L Plate-Well to INBio LQ Mapping.xls";
  
  
  // instance fields
  
  private HSSFSheet _mappingWorksheet;
  private Map<Well,String> _wellToLQMap = new HashMap<Well,String>();
  
  
  // public constructor and instance methods
  
  public ICCBLPlateWellToINBioLQMapper()
  {
    initializeMappingWorksheet();
    populateWellToLQMap();
  }
  
  public String getLQForWell(Well well)
  {
    return _wellToLQMap.get(well);
  }
  
  
  // private instance methods
  
  private void initializeMappingWorksheet()
  {
    File mappingFile = new File(_mappingFilename);
    try {
      InputStream inputStream = new FileInputStream(mappingFile);
      POIFSFileSystem fileSystem =
        new POIFSFileSystem(new BufferedInputStream(inputStream));
      HSSFWorkbook workbook = new HSSFWorkbook(fileSystem);
      _mappingWorksheet = workbook.getSheetAt(0);
    }
    catch (IOException e) {
      log.error("could not read workbook: " + e.getMessage());
    }
  }
  
  private void populateWellToLQMap()
  {
    Library library = new Library(
      "fake library",
      "fake lib",
      LibraryType.NATURAL_PRODUCTS,
      0,
      1);
    int lastRowNum = _mappingWorksheet.getLastRowNum();
    for (int i = 1; i <= lastRowNum; i++) {
      HSSFRow row = _mappingWorksheet.getRow(i);
      HSSFCell plateNumberCell = row.getCell((short) 0);
      //int plateNumber = (int) plateNumberCell.getNumericCellValue();
      int plateNumber = Integer.parseInt(plateNumberCell.getStringCellValue());
      HSSFCell wellNameCell = row.getCell((short) 1);
      String wellName = wellNameCell.getStringCellValue();
      HSSFCell lqCell = row.getCell((short) 3);
      String lq;
      //lq = lqCell.getStringCellValue();
      //if (lq.equals("")) { continue; }
      try {
        lq = String.valueOf((int) lqCell.getNumericCellValue());
      }
      catch (NullPointerException e) {
        continue;
      }
      Well well = new Well(library, plateNumber, wellName);
      _wellToLQMap.put(well, lq);
    }
  }
}
