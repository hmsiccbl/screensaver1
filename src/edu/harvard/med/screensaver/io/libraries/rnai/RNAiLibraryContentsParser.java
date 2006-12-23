// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries.rnai;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.io.libraries.LibraryContentsParser;
import edu.harvard.med.screensaver.io.workbook.Cell;
import edu.harvard.med.screensaver.io.workbook.ParseError;
import edu.harvard.med.screensaver.io.workbook.ParseErrorManager;
import edu.harvard.med.screensaver.io.workbook.PlateNumberParser;
import edu.harvard.med.screensaver.io.workbook.WellNameParser;
import edu.harvard.med.screensaver.io.workbook.Workbook;
import edu.harvard.med.screensaver.io.workbook.Cell.Factory;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.SilencingReagentType;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;


/**
 * Parses the contents (either partial or complete) of an RNAi library
 * from an Excel spreadsheet into the entity model.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class RNAiLibraryContentsParser implements LibraryContentsParser
{
  
  // static fields
  
  private static final Logger log = Logger.getLogger(RNAiLibraryContentsParser.class);
  public static final SilencingReagentType DEFAULT_SILENCING_REAGENT_TYPE =
    SilencingReagentType.SIRNA;

  
  // private instance fields
  
  private DAO _dao;
  private Library _library;
  private Workbook _workbook;
  private ParseErrorManager _errorManager;
  private PlateNumberParser _plateNumberParser;
  private WellNameParser _wellNameParser;
  private NCBIGeneInfoProvider _geneInfoProvider;
  private SilencingReagentType _silencingReagentType = DEFAULT_SILENCING_REAGENT_TYPE;
  
  
  // public constructor and instance methods
  
  /**
   * Construct a new <code>RNAiLibraryContentsParser</code> object.
   * @param dao the data access object
   */
  public RNAiLibraryContentsParser(DAO dao)
  {
    _dao = dao;
  }

  /**
   * Get the {@link SilencingReagentType} for the {@link SilencingReagent SilencingReagents}
   * in this RNAi library.
   * @return the SilencingReagentType for the SilencingReagents
   * in this RNAi library.
   */
  public SilencingReagentType getSilencingReagentType()
  {
    return _silencingReagentType;
  }

  /**
   * Set the {@link SilencingReagentType} for the {@link SilencingReagent SilencingReagents}
   * in this RNAi library.
   * @param reagentType the SilencingReagentType for the SilencingReagents
   * in this RNAi library.
   */
  public void setSilencingReagentType(SilencingReagentType reagentType)
  {
    _silencingReagentType = reagentType;
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
    _dao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        initialize(library, file, stream);
        HSSFWorkbook hssfWorkbook = _workbook.getWorkbook();
        for (int i = 0; i < hssfWorkbook.getNumberOfSheets(); i++) {
          loadLibraryContentsFromHSSFSheet(i, hssfWorkbook.getSheetAt(i));
        }        
      }
    });
    return _library;
  }

  /**
   * Return all errors the were detected during parsing. This class attempts to
   * parse as much of the workbook as possible, continuing on after finding an
   * error. The hope is that multiple errors will help a user/administrator
   * correct a workbook's errors in a batch fashion, rather than in a piecemeal
   * fashion.
   * 
   * @return a <code>List&lt;String&gt;</code> of all errors generated during
   *         parsing
   */
  public List<ParseError> getErrors()
  {
    if (_errorManager == null) {
      return null;
    }
    return _errorManager.getErrors();
  }
  
  public boolean getHasErrors()
  {
    return _errorManager != null && _errorManager.getHasErrors();
  }
  
  public void clearErrors()
  {
    _errorManager = null;
  }
  
    
  // package getters, for the DataRowParser

  /**
   * Get the {@link DAO data access object}.
   * @return the data access object
   */
  DAO getDAO()
  {
    return _dao;
  }
  
  /**
   * Get the {@link NCBIGeneInfoProvider}.
   * @return the geneInfoProvider.
   */
  NCBIGeneInfoProvider getGeneInfoProvider()
  {
    return _geneInfoProvider;
  }

  /**
   * Get the {@link Library}.
   * @return the library.
   */
  Library getLibrary()
  {
    return _library;
  }

  /**
   * Get the {@link PlateNumberParser}.
   * @return the plateNumberParser.
   */
  PlateNumberParser getPlateNumberParser()
  {
    return _plateNumberParser;
  }

  /**
   * Get the {@link WellNameParser}.
   * @return the wellNameParser.
   */
  WellNameParser getWellNameParser()
  {
    return _wellNameParser;
  }
  
  
  // private instance methods

  /**
   * Initialize the instance variables.
   * @param library the library to load contents of
   * @param file the name of the file that contains the library contents
   * @param stream the input stream to load library contents from
   */
  private void initialize(Library library, File file, InputStream stream)
  {
    _library = library;
    _errorManager = new ParseErrorManager();
    _workbook = new Workbook(file, stream, _errorManager);
    _plateNumberParser = new PlateNumberParser(_errorManager);
    _wellNameParser = new WellNameParser(_errorManager);
    _geneInfoProvider = new NCBIGeneInfoProvider(_errorManager);

    // load all of the library's wells in the Hibernate session, which avoids the need
    // to make database queries when checking for existence of wells
    _dao.loadOrCreateWellsForLibrary(library);
  }
  

  /**
   * Load library contents from a single worksheet.
   * @param sheetIndex the index of the worksheet to load library contents from
   * @param hssfSheet the worksheet to load library contents from
   */
  private void loadLibraryContentsFromHSSFSheet(int sheetIndex, HSSFSheet hssfSheet)
  {
    Cell.Factory cellFactory = new Cell.Factory(_workbook, sheetIndex, _errorManager);
    String sheetName = _workbook.getWorkbook().getSheetName(sheetIndex);
    RNAiLibraryColumnHeaders columnHeaders =
      parseColumnHeaders(hssfSheet.getRow(0), sheetName, cellFactory);
    if (columnHeaders == null) {
      return;
    }
    for (short i = 1; i <= hssfSheet.getLastRowNum(); i++) {
      DataRowParser dataRowParser = new DataRowParser(
        this,
        columnHeaders,
        hssfSheet.getRow(i),
        i,
        cellFactory);
      dataRowParser.parseDataRow();
    }
  }

  /**
   * Parse the column headers. Return the resulting {@link RNAiLibraryColumnHeaders}.
   * @param columnHeaderRow the row containing the column headers
   * @param sheetName the name of the worksheet
   * @param cellFactory the cell factory 
   * @return the RequiredRNAiLibraryColumn
   */
  private RNAiLibraryColumnHeaders parseColumnHeaders(
    HSSFRow columnHeaderRow,
    String sheetName,
    Factory cellFactory)
  {
    if (columnHeaderRow == null) {
      _errorManager.addError("ecountered a sheet without any rows: " + sheetName);
      return null;
    }
    RNAiLibraryColumnHeaders columnHeaders = new RNAiLibraryColumnHeaders(
      columnHeaderRow,
      _errorManager,
      cellFactory,
      sheetName);
    if (! columnHeaders.parseColumnHeaders()) {
      _errorManager.addError(
        "couldn't import sheet contents due to problems with column headers: " + sheetName);
      return null;
    }
    return columnHeaders;
  }
}
