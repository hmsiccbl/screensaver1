// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.iccbl.screensaver.reports.icbg;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jxl.Cell;
import jxl.NumberCell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.read.biff.BlankCell;
import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;


/**
 * Maps from an ICCB-L Plate-Well (as represented by a {@link Well}) to an
 * INBio LQ.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class ICCBLPlateWellToINBioLQMapper
{
  
  private static Logger log = Logger.getLogger(ICCBLPlateWellToINBioLQMapper.class);
  private static String _mappingFilename = "ICCB-L Plate-Well to INBio LQ Mapping.xls";
  
  private Sheet _mappingWorksheet;
  private Set<Integer> _mappedPlates = new HashSet<Integer>();
  private Map<WellKey,String> _wellKeyToLQMap = new HashMap<WellKey,String>();
  
  
  public ICCBLPlateWellToINBioLQMapper() throws BiffException, IOException
  {
    initializeMappingWorksheet();
    populateWellKeyToLQMap();
  }
  
  public Set<Integer> getMappedPlates()
  {
    return _mappedPlates;
  }
  
  public String getLQForWellKey(WellKey wellKey)
  {
    return _wellKeyToLQMap.get(wellKey);
  }
  
  private void initializeMappingWorksheet() throws BiffException, IOException
  {
    File mappingFile = new File(_mappingFilename);
    try {
      Workbook workbook = Workbook.getWorkbook(mappingFile);
      _mappingWorksheet = workbook.getSheet(0);
    }
    catch (IOException e) {
      log.error("could not read workbook: " + mappingFile.getAbsolutePath());
      throw e;
    }
  }
  
  private void populateWellKeyToLQMap()
  {
    int lastRowNum = _mappingWorksheet.getRows();
    for (int i = 1; i < lastRowNum; i++) {
      NumberCell plateNumberCell = (NumberCell) _mappingWorksheet.getCell(0, i);
      int plateNumber = new Double(plateNumberCell.getValue()).intValue();
      
      Cell wellNameCell = _mappingWorksheet.getCell(1, i);
      String wellName = wellNameCell.getContents();
      
      // NOTE skipping column 2 which is ss_code
      
      Cell lqCell = _mappingWorksheet.getCell(3, i);
      if (lqCell == null || lqCell instanceof BlankCell) {
        continue;
      }
      String lq;
      lq = lqCell.getContents();
      if (lq.equals("")) { continue; }
      //try {
      //  lq = String.valueOf((int) lqCell.getNumericCellValue());
      //}
      //catch (NullPointerException e) {
      //  continue;
      //}
      
      _wellKeyToLQMap.put(new WellKey(plateNumber, wellName), lq);
      _mappedPlates.add(plateNumber);
      //log.info("mapped " + plateNumber + wellName + " to " + lq);
    }
  }
}
