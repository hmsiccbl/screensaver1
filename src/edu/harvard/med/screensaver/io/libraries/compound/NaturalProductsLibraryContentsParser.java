// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries.compound;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.io.libraries.LibraryContentsParser;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.libraries.WellType;


/**
 * Parses the contents (either partial or complete) of a natural products library from
 * an Excel file.
 * <p>
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class NaturalProductsLibraryContentsParser implements LibraryContentsParser
{

  // private static data
  
  private static final Logger log = Logger.getLogger(NaturalProductsLibraryContentsParser.class);

  private static final WorkbookSettings WORKBOOK_SETTINGS = new WorkbookSettings();
  static {
    WORKBOOK_SETTINGS.setIgnoreBlanks(true);
  }
  private static final String PLATE_HEADER = "plate";
  private static final String WELL_HEADER = "well";
  private static final String VENDOR_IDENTIFIER_HEADER = "vendor_id";
  private static final String ICCB_NUMBER_HEADER = "iccb_num";
  private static class NaturalProductsLibraryContentsException extends Exception
  {
    private static final long serialVersionUID = -6789546240931907100L;
  }

  
  // private instance data
  
  private GenericEntityDAO _dao;
  private LibrariesDAO _librariesDao;
  private Library _library;
  private FileParseErrorManager _errorManager = new FileParseErrorManager();
  private File _file;
  private int _plateColumn, _wellColumn, _vendorIdentifierColumn, _iccbNumberColumn;

  
  // public constructor and instance methods
  
  /**
   * Construct a new <code>SDFileCompoundLibraryContentsParser</code> object.
   * @param genericEntityDao the data access object
   */
  public NaturalProductsLibraryContentsParser(GenericEntityDAO dao,
                                             LibrariesDAO librariesDao)
  {
    _dao = dao;
    _librariesDao = librariesDao;
  }

  /**
   * Load library contents (either partial or complete) from an input
   * stream of an Excel spreadsheet into a library.
   * @param library the library to load contents of
   * @param file the name of the file that contains the library contents
   * @param stream the input stream to load library contents from
   * @return the library with the contents loaded
   */
  public Library parseLibraryContents(
    final Library library,
    final File file,
    final InputStream stream)
  {
    log.info("parsing natural products Excel file " + file.getName());
    _library = library;
    _file = file;
    final Workbook workbook;
    try {
      workbook = Workbook.getWorkbook(stream, WORKBOOK_SETTINGS);
    }
    catch (Exception e) {
      _errorManager.addError("error opening Excel file: " + e.getMessage());
      return null;
    }
    _dao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        _librariesDao.loadOrCreateWellsForLibrary(_library);
        try {
          parseWorkbook(workbook);
        }
        catch (NaturalProductsLibraryContentsException e) {
        }
      }
    });
    return _library;
  }
  
  public List<FileParseError> getErrors()
  {
    return _errorManager.getErrors();
  }
  
  public boolean getHasErrors()
  {
    return _errorManager != null && _errorManager.getHasErrors();
  }
  
  public void clearErrors()
  {
    _errorManager = new FileParseErrorManager();
  }
  
  
  // private instance methods
  
  private void parseWorkbook(Workbook workbook) throws NaturalProductsLibraryContentsException
  {
    for (Sheet sheet : workbook.getSheets()) {
      parseSheet(sheet);
    }
  }
  
  private void parseSheet(Sheet sheet) throws NaturalProductsLibraryContentsException
  {
    log.info("parsing worksheet " + sheet.getName());
    parseHeaders(sheet);
    for (int i = 1; i < sheet.getRows(); i ++) {
      try {
        Integer plateNumber = getPlateNumber(sheet, i);
        String wellName = getWellName(sheet, i);
        String vendorIdentifier = getVendorIdentifier(sheet, i);
        String iccbNumber = getIccbNumber(sheet, i);
        
        Well well = _librariesDao.findWell(new WellKey(plateNumber, wellName));
        well.setVendorIdentifier(vendorIdentifier);
        well.setIccbNumber(iccbNumber);
        well.setWellType(WellType.EXPERIMENTAL);
        _dao.persistEntity(well);
      }
      catch (NaturalProductsLibraryContentsException e) {
      }
    }
  }

  private Integer getPlateNumber(Sheet sheet, int i) throws NaturalProductsLibraryContentsException
  {
    Integer plateNumber;
    try {
      plateNumber = new Integer(sheet.getCell(_plateColumn, i).getContents());
    }
    catch (NumberFormatException e) {
      _errorManager.addError(
        "non-numeric plate number on sheet '" + sheet.getName() + "'", _file, i + 1);
      throw new NaturalProductsLibraryContentsException();
    }
    if (plateNumber < _library.getStartPlate() || plateNumber > _library.getEndPlate()) {
      _errorManager.addError(
        "plate number out of library range on sheet '" + sheet.getName() + "'", _file, i + 1);
      throw new NaturalProductsLibraryContentsException();      
    }
    return plateNumber;
  }
  
  private String getWellName(Sheet sheet, int i) throws NaturalProductsLibraryContentsException
  {
    String wellName = sheet.getCell(_wellColumn, i).getContents();
    if (! Well.isValidWellName(wellName)) {
      _errorManager.addError(
        "invalid well name on sheet '" + sheet.getName() + "'", _file, i + 1);
      throw new NaturalProductsLibraryContentsException();
    }
    return wellName;
  }

  private String getVendorIdentifier(Sheet sheet, int i) throws NaturalProductsLibraryContentsException
  {
    Cell cell = sheet.getCell(_vendorIdentifierColumn, i);
    String contents = cell.getContents();
    if (contents == null || contents.equals("")) {
      return null;
    }
    if (contents.equals("0")) {
      _errorManager.addError(
        "suspicious vendor identifier value '0' on sheet '" + sheet.getName() +
        "'. Try changing the cell format to 'Text'.", _file, i + 1);
      throw new NaturalProductsLibraryContentsException();      
    }
    return contents;
  }
  
  private String getIccbNumber(Sheet sheet, int i) throws NaturalProductsLibraryContentsException
  {
    if (_iccbNumberColumn == -1) {
      return null;
    }
    Cell cell = sheet.getCell(_iccbNumberColumn, i);
    String contents = cell.getContents();
    if (contents == null || contents.equals("")) {
      return null;
    }
    if (contents.equals("0")) {
      _errorManager.addError(
        "suspicious iccb number value '0' on sheet '" + sheet.getName() +
        "'. Try changing the cell format to 'Text'.", _file, i + 1);
      throw new NaturalProductsLibraryContentsException();      
    }
    return contents;
  }  
  
  private void parseHeaders(Sheet sheet) throws NaturalProductsLibraryContentsException
  {
    _plateColumn = _wellColumn = _vendorIdentifierColumn = _iccbNumberColumn = -1;
    for (int i = 0; i < sheet.getColumns(); i ++) {
      String header = sheet.getCell(i,0).getContents();
      if (header.equalsIgnoreCase(PLATE_HEADER)) {
        _plateColumn = i;
      }
      else if (header.equalsIgnoreCase(WELL_HEADER)) {
        _wellColumn = i;
      }
      else if (header.equalsIgnoreCase(VENDOR_IDENTIFIER_HEADER)) {
        _vendorIdentifierColumn = i;
      }
      else if (header.equalsIgnoreCase(ICCB_NUMBER_HEADER)) {
        _iccbNumberColumn = i;
      }
    }
    if (_plateColumn == -1 || _wellColumn == -1 || _vendorIdentifierColumn == -1) {
      if (_plateColumn == -1) {
        _errorManager.addError(
          "couldn't find header for column '" + PLATE_HEADER + "' on sheet '" + sheet.getName() + "'");
      }
      if (_wellColumn == -1) {
        _errorManager.addError(
          "couldn't find header for column '" + WELL_HEADER + "' on sheet '" + sheet.getName() + "'");
      }
      if (_vendorIdentifierColumn == -1) {
        _errorManager.addError(
          "couldn't find header for column '" + VENDOR_IDENTIFIER_HEADER + "' on sheet '" + sheet.getName() + "'");
      }
      throw new NaturalProductsLibraryContentsException();
    }
  }
}
