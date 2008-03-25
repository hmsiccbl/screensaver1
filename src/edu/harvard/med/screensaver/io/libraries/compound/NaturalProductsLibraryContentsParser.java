// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries.compound;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;

import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.CommandLineApplication;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.io.ParseError;
import edu.harvard.med.screensaver.io.libraries.LibraryContentsParser;
import edu.harvard.med.screensaver.io.libraries.ParseLibraryContentsException;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
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

  public static final int SHORT_OPTION_INDEX = 0;
  public static final int LONG_OPTION_INDEX = 1;
  public static final int DESCRIPTION_INDEX = 2;

  public static final String[] INPUT_FILE_OPTION = {
    "f",
    "input-file",
    "the file to load library contents from"
  };
  public static final String[] LIBRARY_SHORT_NAME_OPTION = {
    "l",
    "library-short-name",
    "the short name of the library to load contents for"
  };
  
  
  // static methods

  @SuppressWarnings("static-access")
  public static void main(String[] args)
  {
    CommandLineApplication application = new CommandLineApplication(args);
    application.addCommandLineOption(
      OptionBuilder
      .hasArg()
      .withArgName(LIBRARY_SHORT_NAME_OPTION[SHORT_OPTION_INDEX])
      .isRequired()
      .withDescription(LIBRARY_SHORT_NAME_OPTION[DESCRIPTION_INDEX])
      .withLongOpt(LIBRARY_SHORT_NAME_OPTION[LONG_OPTION_INDEX])
      .create(LIBRARY_SHORT_NAME_OPTION[SHORT_OPTION_INDEX]));
    application.addCommandLineOption(
      OptionBuilder
      .hasArg()
      .withArgName(INPUT_FILE_OPTION[SHORT_OPTION_INDEX])
      .isRequired()
      .withDescription(INPUT_FILE_OPTION[DESCRIPTION_INDEX])
      .withLongOpt(INPUT_FILE_OPTION[LONG_OPTION_INDEX])
      .create(INPUT_FILE_OPTION[SHORT_OPTION_INDEX]));
    try {
      if (! application.processOptions(true, true)) {
        return;
      }
      String libraryShortName =
        application.getCommandLineOptionValue(LIBRARY_SHORT_NAME_OPTION[SHORT_OPTION_INDEX]);
      File libraryContentsFile =
        application.getCommandLineOptionValue(INPUT_FILE_OPTION[SHORT_OPTION_INDEX], File.class);

      GenericEntityDAO genericEntityDao = (GenericEntityDAO) application.getSpringBean("genericEntityDao");
      LibrariesDAO librariesDao = (LibrariesDAO) application.getSpringBean("librariesDao");
      NaturalProductsLibraryContentsParser parser =
        new NaturalProductsLibraryContentsParser(genericEntityDao, librariesDao);

      parser.parseLibraryContents(libraryShortName, libraryContentsFile);
    }
    catch (ParseException e) {
      log.error("error processing command line options", e);
    }
    catch (Exception e) {
      log.error("application exception", e);
    }
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
   */
  public NaturalProductsLibraryContentsParser(GenericEntityDAO dao,
                                              LibrariesDAO librariesDao)
  {
    _dao = dao;
    _librariesDao = librariesDao;
  }

  /**
   * Parse the library contents from a file in a command-line application context. Report errors to the logger.
   * @param libraryShortName
   * @param libraryContentsFile
   */
  public void parseLibraryContents(final String libraryShortName, final File libraryContentsFile)
  {
    _dao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        log.info("processing small molecule file: " + libraryContentsFile.getName());
        Library library = _dao.findEntityByProperty(Library.class, "shortName", libraryShortName);
        if (library == null) {
          log.error("couldn't find library with shortName \"" + libraryShortName + "\"");
          return;
        }
        try {
          parseLibraryContents(
            library,
            libraryContentsFile,
            new FileInputStream(libraryContentsFile));
        }
        catch (FileNotFoundException e) {
          throw new InternalError("braindamage: " + e.getMessage());
        }
        if (getHasErrors()) {
          for (ParseError error : getErrors()) {
            log.error(error.toString());
          }
        }
        _dao.saveOrUpdateEntity(library);
        log.info("finished processing small molecule File: " + libraryContentsFile.getName());
      }
    });
  }

  /**
   * Load library contents (either partial or complete) from an input
   * stream of an Excel spreadsheet into a library.
   * @param library the library to load contents of
   * @param file the name of the file that contains the library contents
   * @param stream the input stream to load library contents from
   * @return the library with the contents loaded
   * @throws ParseLibraryContentsException if parse errors encountered. The
   *           exception will contain a reference to a ParseErrors object which
   *           can be inspected and/or reported to the user.
   */
  public Library parseLibraryContents(
    final Library library,
    final File file,
    final InputStream stream)
  throws ParseLibraryContentsException
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
        if (getHasErrors()) {
          throw new ParseLibraryContentsException(_errorManager);
        }
      }
    });
    return _library;
  }

  public List<? extends ParseError> getErrors()
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
        if (well.getReagent() == null) {
          ReagentVendorIdentifier reagentVendorIdentifier =
            new ReagentVendorIdentifier(well.getLibrary().getVendor(), vendorIdentifier);
          Reagent reagent = _dao.findEntityById(Reagent.class, reagentVendorIdentifier);
          if (reagent == null) {
            reagent = new Reagent(reagentVendorIdentifier);
            _dao.saveOrUpdateEntity(reagent); // place into session so it can be found again before flush
            log.info("created new reagent " + reagent + " for " + well);
          }
          well.setReagent(reagent);
        }
        well.setIccbNumber(iccbNumber);
        well.setWellType(WellType.EXPERIMENTAL);
        _dao.saveOrUpdateEntity(well);
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
