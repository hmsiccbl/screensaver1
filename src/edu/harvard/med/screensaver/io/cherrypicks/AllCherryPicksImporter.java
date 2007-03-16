// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.cherrypicks;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jxl.LabelCell;
import jxl.NumberCell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.CopyInfo;
import edu.harvard.med.screensaver.model.libraries.CopyUsageType;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.PlateType;

import org.apache.log4j.Logger;

public class AllCherryPicksImporter
{
  // static members

  private static Logger log = Logger.getLogger(AllCherryPicksImporter.class);

  private static final String CHERRY_PICK_COPIES_SHEET_NAME = "cherry pick copies";
  private static final String CHERRY_PICK_REQUESTS_SHEET_NAME = "cherry pick requests";

  private static final int COPY_PLATE_COLUMN = 0;
  private static final int COPY_COPY_NAME_COLUMN = 1;
  private static final int COPY_VOLUME_COLUMN = 2;

  // instance data members
  
  private DAO _dao;
  

  // public constructors and methods
  
  public AllCherryPicksImporter(DAO dao)
  {
    _dao = dao;
  }

  public Set<Copy> importCherryPickCopies(File workbookFile) 
    throws IOException, CherryPickCopiesDataException, FatalParseException
  {
    Workbook workbook;
    try {
      workbook = Workbook.getWorkbook(workbookFile);
    }
    catch (BiffException e) {
      throw new FatalParseException(e);
    }

    final Sheet sheet = workbook.getSheet(CHERRY_PICK_COPIES_SHEET_NAME);
    final Set<Copy> copies = new HashSet<Copy>();
    final Map<CopyInfo,Integer> copyInfo2Volume = new HashMap<CopyInfo,Integer>();
    _dao.doInTransaction(new DAOTransaction() 
    {
      public void runTransaction() 
      {
        for (int iRow = 1; iRow < sheet.getRows(); iRow++) {
          Integer plate;
          String copyName;
          BigDecimal volume;
          int iCol = 0;
          try {
            plate = new Integer((int) ((NumberCell) sheet.getCell(COPY_PLATE_COLUMN, iRow)).getValue());
            ++iCol;
            copyName = ((LabelCell) sheet.getCell(COPY_COPY_NAME_COLUMN, iRow)).getString();
            ++iCol;
            volume = new BigDecimal(((NumberCell) sheet.getCell(COPY_VOLUME_COLUMN, iRow)).getValue());
          }
          catch (Exception e) {
            throw new CherryPickCopiesDataException("illegal data type: "  + e.getMessage(), iRow, iCol);
          }

          Library library = _dao.findLibraryWithPlate(plate);
          if (library == null) {
            throw new CherryPickCopiesDataException("invalid plate number (no library for plate)", iRow);
          }
          Copy copy = library.getCopy(copyName);
          if (copy == null) {
            copy = new Copy(library, CopyUsageType.FOR_CHERRY_PICK_SCREENING, copyName);
          }
          CopyInfo copyInfo = copy.getCopyInfo(plate);
          if (copyInfo == null) {
            new CopyInfo(copy, plate, "<unknown>", PlateType.EPPENDORF, volume);
          }
          if (copyInfo2Volume.containsKey(copyInfo)) {
            Integer expectedVolume = copyInfo2Volume.get(copyInfo);
            if (!volume.equals(expectedVolume)) {
              throw new CherryPickCopiesDataException("volume not consistent for plate " + plate, iRow);
            }
          }
          copies.add(copy);
        }
      }
    });
    return copies;
  }

  // private methods

}

