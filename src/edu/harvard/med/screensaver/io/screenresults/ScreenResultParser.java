// $HeadURL:
// svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/io/screenresults/ScreenResultParser.java
// $
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.screenresults;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.io.workbook.Cell;
import edu.harvard.med.screensaver.io.workbook.CellValueParser;
import edu.harvard.med.screensaver.io.workbook.CellVocabularyParser;
import edu.harvard.med.screensaver.io.workbook.ParseError;
import edu.harvard.med.screensaver.io.workbook.ParseErrorManager;
import edu.harvard.med.screensaver.io.workbook.PlateNumberParser;
import edu.harvard.med.screensaver.io.workbook.WellNameParser;
import edu.harvard.med.screensaver.io.workbook.Workbook;
import edu.harvard.med.screensaver.io.workbook.Cell.Factory;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.ActivityIndicatorType;
import edu.harvard.med.screensaver.model.screenresults.AssayWellType;
import edu.harvard.med.screensaver.model.screenresults.IndicatorDirection;
import edu.harvard.med.screensaver.model.screenresults.PartitionedValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ResultValueTypeNumericalnessException;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.AssayReadoutType;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreeningRoomActivity;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;

/**
 * Parses data from a workbook files (a.k.a. Excel spreadsheets) necessary for
 * instantiating a
 * {@link edu.harvard.med.screensaver.model.screenresults.ScreenResult}.
 * ScreenResult data consists of both "data headers" and "raw" data. By
 * convention, each worksheet contains the raw data for a single plate, but the
 * parser is indifferent to how data may be arranged across worksheets.
 * <p>
 * The data header info is used to instantiate
 * {@link edu.harvard.med.screensaver.model.screenresults.ResultValueType}
 * objects, while the raw data is used to instantiate each of the
 * <code>ResultValueType</code>s'
 * {@link edu.harvard.med.screensaver.model.screenresults.ResultValue} objects.
 * Altogether these objects are used instantiate a {@link ScreenResult} object,
 * which is the returned result of the {@link #parse} method.
 * <p>
 * The class attempts to parse the file(s) as fully as possible, carrying on in
 * the face of errors, in order to catch as many errors as possible, as this
 * will aid the manual effort of correcting the files' format and content
 * between import attempts. By calling
 * {@link #outputErrorsInAnnotatedWorkbooks(String)}, a new set of workbooks
 * will be written to the same directory as the metadata file. These workbooks
 * will contain errors messages in each cell that encountered an error during
 * parsing. Error messages that are not cell-specific will be written to a new
 * "Parse Errors" sheet in the new metadata workbook. Error-annotated workbook
 * files will only be written for input workbooks that generated parse errors.
 * Each error annotated workbook will be named the same as its respective
 * workbook input file with an ".errors.xls" suffix.
 * <p>
 * Each call to {@link #parse} will clear the errors accumulated from the
 * previous call, and so the result of calling {@link #getErrors()} will change
 * after each call to {@link #parse}.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class ScreenResultParser implements ScreenResultWorkbookSpecification
{

  // static data members
  
  private static final Logger log = Logger.getLogger(ScreenResultParser.class);

  private static final String NO_SCREEN_ID_FOUND_ERROR = "Screen ID not found";
  private static final String DATA_HEADER_SHEET_NOT_FOUND_ERROR = "\"Data Headers\" sheet not found";
  private static final String UNKNOWN_ERROR = "unknown error";
  private static final String NO_DATA_SHEETS_FOUND_ERROR = "no data worksheets were found; no result data was imported";
  private static final String NO_SUCH_WELL = "library well does not exist";
  private static final String NO_SUCH_LIBRARY_WITH_PLATE = "no library with given plate number";

  private static final int RELOAD_WORKBOOK_AFTER_SHEET_COUNT = 32;

  private static SortedMap<String,AssayReadoutType> assayReadoutTypeMap = new TreeMap<String,AssayReadoutType>();
  private static SortedMap<String,IndicatorDirection> indicatorDirectionMap = new TreeMap<String,IndicatorDirection>();
  private static SortedMap<String,ActivityIndicatorType> activityIndicatorTypeMap = new TreeMap<String,ActivityIndicatorType>();
  private static SortedMap<String,Boolean> rawOrDerivedMap = new TreeMap<String,Boolean>();
  private static SortedMap<String,Boolean> primaryOrFollowUpMap = new TreeMap<String,Boolean>();
  private static SortedMap<String,Boolean> booleanMap = new TreeMap<String,Boolean>();
  private static SortedMap<String,PartitionedValue> partitionedValueMap = new TreeMap<String,PartitionedValue>();
  private static SortedMap<String,AssayWellType> assayWellTypeMap = new TreeMap<String,AssayWellType>();
  static {
    for (AssayReadoutType assayReadoutType : AssayReadoutType.values()) {
      assayReadoutTypeMap.put(assayReadoutType.getValue(),
                              assayReadoutType);
    }
    
    indicatorDirectionMap.put(NUMERICAL_INDICATOR_DIRECTION_LOW_VALUES_INDICATE, IndicatorDirection.LOW_VALUES_INDICATE);
    indicatorDirectionMap.put(NUMERICAL_INDICATOR_DIRECTION_HIGH_VALUES_INDICATE, IndicatorDirection.HIGH_VALUES_INDICATE);

    activityIndicatorTypeMap.put("Numeric", ActivityIndicatorType.NUMERICAL);
    activityIndicatorTypeMap.put("Numerical", ActivityIndicatorType.NUMERICAL);
    activityIndicatorTypeMap.put("Boolean", ActivityIndicatorType.BOOLEAN);
    activityIndicatorTypeMap.put("Partitioned", ActivityIndicatorType.PARTITION);
    activityIndicatorTypeMap.put("Partition", ActivityIndicatorType.PARTITION);

    rawOrDerivedMap.put("", false);
    rawOrDerivedMap.put(RAW_VALUE, false);
    rawOrDerivedMap.put(DERIVED_VALUE, true);

    primaryOrFollowUpMap.put("", false);
    primaryOrFollowUpMap.put(PRIMARY_VALUE, false);
    primaryOrFollowUpMap.put(FOLLOWUP_VALUE, true);

    booleanMap.put("", false);
    booleanMap.put("false", false);
    booleanMap.put("no", false);
    booleanMap.put("n", false);
    booleanMap.put("0", false);
    booleanMap.put("true", true);
    booleanMap.put("yes", true);
    booleanMap.put("y", true);
    booleanMap.put("1", true);

    for (PartitionedValue pv : PartitionedValue.values()) {
      partitionedValueMap.put(pv.getDisplayValue().toLowerCase(), pv);
      partitionedValueMap.put(pv.getDisplayValue().toUpperCase(), pv);
      partitionedValueMap.put(pv.getValue(), pv);
    }

    assayWellTypeMap.put(AssayWellType.EXPERIMENTAL.getAbbreviation(), AssayWellType.EXPERIMENTAL);
    assayWellTypeMap.put(AssayWellType.EMPTY.getAbbreviation(), AssayWellType.EMPTY);
    assayWellTypeMap.put(AssayWellType.LIBRARY_CONTROL.getAbbreviation(), AssayWellType.LIBRARY_CONTROL);
    assayWellTypeMap.put(AssayWellType.ASSAY_POSITIVE_CONTROL.getAbbreviation(), AssayWellType.ASSAY_POSITIVE_CONTROL);
    assayWellTypeMap.put(AssayWellType.ASSAY_CONTROL.getAbbreviation(), AssayWellType.ASSAY_CONTROL);
    assayWellTypeMap.put(AssayWellType.BUFFER.getAbbreviation(), AssayWellType.BUFFER);
    assayWellTypeMap.put(AssayWellType.DMSO.getAbbreviation(), AssayWellType.DMSO);
    assayWellTypeMap.put(AssayWellType.OTHER.getAbbreviation(), AssayWellType.OTHER);
    assert assayWellTypeMap.size() == AssayWellType.values().length : "assayWellTypeMap not initialized properly";
  }


  // static methods


  /**
   * The ScreenResult object to be populated with data parsed from the spreadsheet.
   */
  private ScreenResult _screenResult;
  private ParseErrorManager _errors = new ParseErrorManager(); // init at instantiation, to avoid NPE from various method calls before parse() is called
  private SortedMap<String,ResultValueType> _dataTableColumnLabel2RvtMap;

  private ColumnLabelsParser _columnsDerivedFromParser;
  private ExcludeParser _excludeParser;
  private CellVocabularyParser<AssayReadoutType> _assayReadoutTypeParser;
  private CellVocabularyParser<IndicatorDirection> _indicatorDirectionParser;
  private CellVocabularyParser<ActivityIndicatorType> _activityIndicatorTypeParser;
  private CellVocabularyParser<Boolean> _rawOrDerivedParser;
  private CellVocabularyParser<Boolean> _primaryOrFollowUpParser;
  private CellVocabularyParser<Boolean> _booleanParser;
  private CellVocabularyParser<PartitionedValue> _partitionedValueParser;
  private CellVocabularyParser<AssayWellType> _assayWellTypeParser;
  private PlateNumberParser _plateNumberParser;
  private WellNameParser _wellNameParser;

  private LibrariesDAO _librariesDao;
  private Factory _metadataCellParserFactory;
  private Factory _dataCellParserFactory;
  private Map<Integer,Short> _dataHeaderIndex2DataHeaderColumn;
  private Set<Library> _preloadedLibraries;
  /**
   * The library that was associated with the plate that was last accessed.
   * @motivation optimization for findLibraryWithPlate(); reduce db I/O
   */
  private Library _lastLibrary;


  
  // public methods and constructors

  public ScreenResultParser(LibrariesDAO librariesDao) 
  {
    _librariesDao = librariesDao;
  }
  
  /**
   * Parses the specified workbook file that contains Screen Result data in the
   * <a
   * href="https://wiki.med.harvard.edu/ICCBL/NewScreenResultFileFormat">"new"
   * format</a>. Errors encountered during parsing are stored with this object
   * until a parse() method is called again, and these errors can be retrieved
   * via {@link #getErrors}. The returned <code>ScreenResult</code> may only
   * be partially populated if errors are encountered, so always call
   * getErrors() to determine parsing success.
   * 
   * @param the parent Screen of the Screen Result being parsed
   * @param the workbook file to be parsed
   * @return a ScreenResult object containing the data parsed from the workbook
   *         file; <code>null</code> if a fatal error occurs (e.g. file not
   *         found)
   * @throws FileNotFoundException
   * @see #getErrors()
   */
  public ScreenResult parse(Screen screen, File workbookFile)
  {
    try {
      return doParse(screen, 
                     workbookFile,
                     new BufferedInputStream(new FileInputStream(workbookFile)));
    }
    catch (FileNotFoundException e) {
      _errors.addError("input file not found: " + e.getMessage());
    }
    return null;
  }

  /**
   * Parses the specified workbook file that contains Screen Result data in the
   * <a
   * href="https://wiki.med.harvard.edu/ICCBL/NewScreenResultFileFormat">"new"
   * format</a>. Errors encountered during parsing are stored with this object
   * until a parse() method is called again, and these errors can be retrieved
   * via {@link #getErrors}. The returned <code>ScreenResult</code> may only
   * be partially populated if errors are encountered, so always call
   * getErrors() to determine parsing success.
   * 
   * @param the parent Screen of the Screen Result being parsed
   * @param the workbook file to be parsed; if inputStream is null, will be used
   *          to obtain the workbook file, otherwise just used to hold a name
   *          for display/output purposes; if named file does not actually
   *          exist, inputStream must not be null
   * @param an InputStream that provides the workbook file as...well... an
   *          InputStream
   * @return a ScreenResult object containing the data parsed from the workbook
   *         file; <code>null</code> if a fatal error occurs
   * @see #getErrors()
   * @motivation For use by the web application UI; the InputStream allows us to
   *             avoid making (another) temporary copy of the file.
   */
  public ScreenResult parse(Screen screen, String inputSourceName, InputStream inputStream)
  {
    return doParse(screen, 
                   new File(inputSourceName), 
                   inputStream);
  }

  private ScreenResult doParse(Screen screen,
                               File workbookFile,
                               InputStream workbookInputStream)
  {
    _screenResult = null;
    _errors = new ParseErrorManager();
    _preloadedLibraries = new HashSet<Library>();
    _lastLibrary = null;
    _assayReadoutTypeParser = new CellVocabularyParser<AssayReadoutType>(assayReadoutTypeMap, _errors);
    _dataTableColumnLabel2RvtMap = new TreeMap<String,ResultValueType>();
    _columnsDerivedFromParser = new ColumnLabelsParser(_dataTableColumnLabel2RvtMap, _errors);
    _excludeParser = new ExcludeParser(_dataTableColumnLabel2RvtMap, _errors);
    _indicatorDirectionParser = new CellVocabularyParser<IndicatorDirection>(indicatorDirectionMap, _errors);
    _activityIndicatorTypeParser = new CellVocabularyParser<ActivityIndicatorType>(activityIndicatorTypeMap, ActivityIndicatorType.NUMERICAL, _errors);
    _rawOrDerivedParser = new CellVocabularyParser<Boolean>(rawOrDerivedMap, Boolean.FALSE, _errors);
    _primaryOrFollowUpParser = new CellVocabularyParser<Boolean>(primaryOrFollowUpMap, Boolean.FALSE, _errors);
    _booleanParser = new CellVocabularyParser<Boolean>(booleanMap, Boolean.FALSE, _errors);
    _partitionedValueParser = new CellVocabularyParser<PartitionedValue>(partitionedValueMap, PartitionedValue.NONE, _errors);
    _assayWellTypeParser = new CellVocabularyParser<AssayWellType>(assayWellTypeMap, AssayWellType.EXPERIMENTAL, _errors);
    _plateNumberParser = new PlateNumberParser(_errors);
    _wellNameParser = new WellNameParser(_errors);
    try {
      Workbook workbook = new Workbook(workbookFile, workbookInputStream, _errors);
      log.info("parsing " + workbookFile.getAbsolutePath());
      DataHeadersParseResult metadataParseResult = parseDataHeaders(screen,
                                                                    workbook);
      if (_errors.getHasErrors()) {
        log.info("errors found in data headers, will not attempt to parse data sheets");
      }
      else {
        log.info("parsing data sheets");
        parseData(workbook,
                  metadataParseResult.getScreenResult());
      }
      _screenResult = metadataParseResult.getScreenResult();
    }
    catch (UnrecoverableScreenResultParseException e) {
      _errors.addError("serious parse error encountered (could not continue further parsing): " + e.getMessage());
    }
    catch (Exception e) {
      e.printStackTrace();
      String errorMsg = UNKNOWN_ERROR + " of type : " + e.getClass() + ": " + e.getMessage();
      _errors.addError(errorMsg);
    }
    finally {
      // TODO: close workbooks' inputstreams?
    }
    return _screenResult;
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
    return _errors.getErrors();
  }
  
  public boolean getHasErrors()
  {
    return _errors.getHasErrors();
  }
  
  public ScreenResult getParsedScreenResult()
  {
    return _screenResult;
  }


  // private methods

  /**
   * Initialize the metadata worksheet and related data members.
   * 
   * @throws UnrecoverableScreenResultParseException if metadata worksheet could
   *           not be initialized or does not appear to be a valid metadata
   *           definition
   */
  private HSSFSheet initializeDataHeadersSheet(Workbook metadataWorkbook)
    throws UnrecoverableScreenResultParseException
  {
    // find the "Data Headers" sheet
    int dataHeadersSheetIndex;
    try {
      dataHeadersSheetIndex = metadataWorkbook.findSheetIndex(DATA_HEADERS_SHEET_NAME);
    }
    catch (IllegalArgumentException e) {
      throw new UnrecoverableScreenResultParseException(
                                                        DATA_HEADER_SHEET_NOT_FOUND_ERROR,
                                                        metadataWorkbook);
    }
    HSSFSheet metadataSheet = metadataWorkbook.getWorkbook().getSheetAt(dataHeadersSheetIndex);

    // at this point, we know we have a valid metadata workbook, to which we can
    // append errors
    _errors.setErrorsWorbook(metadataWorkbook);
    _metadataCellParserFactory = new Cell.Factory(metadataWorkbook,
                                                  dataHeadersSheetIndex,
                                                  _errors);
    _dataHeaderIndex2DataHeaderColumn = new HashMap<Integer,Short>();
    return metadataSheet;
  }

  /**
   * Finds the total number of data header columns.
   * 
   * @param metadataSheet
   * @throws UnrecoverableScreenResultParseException
   */
  private int findDataHeaderColumnCount(HSSFSheet metadataSheet) throws UnrecoverableScreenResultParseException
  {
    HSSFRow row = metadataSheet.getRow(DataHeaderRow.COLUMN_IN_DATA_WORKSHEET.getRowIndex());
    short n = 0;
    while (row.getCell(n) != null && row.getCell(n).getCellType() != HSSFCell.CELL_TYPE_BLANK) { ++n; }
    return n - 1;
  }

  /**
   * Prepares the _dataCellParserFactory to return Cells from the specified sheet.
   * @param workbook
   * @param sheetIndex
   * @return
   */
  private HSSFSheet initializeDataSheet(final Workbook workbook, int sheetIndex)
  {
//    // HACK: occassionally reclaim memory, since data held by previously loaded
//    // sheets are no longer needed
//    if (sheetIndex % RELOAD_WORKBOOK_AFTER_SHEET_COUNT == 0) {
//      log.debug("releasing memory held by workbook");
//      releaseMemory(new Runnable() { public void run() { workbook.reload(); } });
//    }
    
    HSSFSheet dataSheet = workbook.getWorkbook().getSheetAt(sheetIndex);
    _dataCellParserFactory = new Cell.Factory(workbook,
                                              sheetIndex,
                                              _errors);  
    return dataSheet;
  }


  private Cell dataHeadersCell(DataHeaderRow row, int dataHeader, boolean isRequired) 
  {
    return _metadataCellParserFactory.getCell((short) (METADATA_FIRST_DATA_HEADER_COLUMN_INDEX + dataHeader),
                                              row.getRowIndex(),
                                              isRequired);
  }
  
  private Cell dataHeadersCell(DataHeaderRow row, int dataHeader)
  {
    return dataHeadersCell(row, dataHeader, /*required=*/false);
  }
  
  private Cell dataCell(int row, DataColumn column)
  {
    return dataCell(row, column, false);
  }

  private Cell dataCell(int row, DataColumn column, boolean isRequired)
  {
    return _dataCellParserFactory.getCell((short) column.ordinal(), row, isRequired);
  }

  private Cell dataCell(int row, int iDataHeader)
  {
    return _dataCellParserFactory.getCell((short) (_dataHeaderIndex2DataHeaderColumn.get(iDataHeader)),
                                          row);
  }
                                        
  
  private static class DataHeadersParseResult
  {
    private ScreenResult _screenResult;
    private ArrayList<Workbook> _rawDataWorkbooks;

    public ArrayList<Workbook> getRawDataWorkbooks()
    {
      return _rawDataWorkbooks;
    }

    public void setRawDataWorkbooks(ArrayList<Workbook> rawDataWorkbooks)
    {
      _rawDataWorkbooks = rawDataWorkbooks;
    }

    public ScreenResult getScreenResult()
    {
      return _screenResult;
    }

    public void setScreenResult(ScreenResult screenResult)
    {
      _screenResult = screenResult;
    }
  }
  
  /**
   * Parse the workbook containing the ScreenResult metadata
   * @param workbook
   * @throws UnrecoverableScreenResultParseException 
   */
  private DataHeadersParseResult parseDataHeaders(
    Screen screen,
    Workbook workbook)
    throws UnrecoverableScreenResultParseException 
  {
    DataHeadersParseResult dataHeadersParseResult = new DataHeadersParseResult();
    HSSFSheet dataHeadersSheet = initializeDataHeadersSheet(workbook);
    ParsedScreenInfo parsedScreenInfo = parseScreenInfo(workbook, screen);
    
    dataHeadersParseResult.setScreenResult(new ScreenResult(screen, parsedScreenInfo.getDateCreated()));
    int dataHeaderCount = findDataHeaderColumnCount(dataHeadersSheet);
    for (int iDataHeader = 0; iDataHeader < dataHeaderCount; ++iDataHeader) {
      recordDataHeaderColumn(iDataHeader);
      ResultValueType rvt = 
        new ResultValueType(dataHeadersParseResult.getScreenResult(),
                            dataHeadersCell(DataHeaderRow.NAME, iDataHeader, true).getString(),
                            dataHeadersCell(DataHeaderRow.REPLICATE, iDataHeader).getInteger(),
                            _rawOrDerivedParser.parse(dataHeadersCell(DataHeaderRow.RAW_OR_DERIVED, iDataHeader)),
                            _booleanParser.parse(dataHeadersCell(DataHeaderRow.IS_ASSAY_ACTIVITY_INDICATOR, iDataHeader)),
                            _primaryOrFollowUpParser.parse(dataHeadersCell(DataHeaderRow.PRIMARY_OR_FOLLOWUP, iDataHeader)),
                            dataHeadersCell(DataHeaderRow.ASSAY_PHENOTYPE, iDataHeader).getString());
      _dataTableColumnLabel2RvtMap.put(dataHeadersCell(DataHeaderRow.COLUMN_IN_DATA_WORKSHEET, iDataHeader, true).getAsString(), rvt);
      rvt.setDescription(dataHeadersCell(DataHeaderRow.DESCRIPTION, iDataHeader).getString());
      rvt.setTimePoint(dataHeadersCell(DataHeaderRow.TIME_POINT, iDataHeader).getString());
      if (rvt.isDerived()) {
        for (ResultValueType resultValueType : _columnsDerivedFromParser.parseList(dataHeadersCell(DataHeaderRow.COLUMNS_DERIVED_FROM, iDataHeader, true))) {
          if (resultValueType != null) { // can be null if unparseable value is encountered in list
            rvt.addTypeDerivedFrom(resultValueType);
          }
        }
        rvt.setHowDerived(dataHeadersCell(DataHeaderRow.HOW_DERIVED, iDataHeader, true).getString());
        // TODO: should warn if these values *are* defined and !isDerivedFrom()
      }
      else {
        rvt.setAssayReadoutType(_assayReadoutTypeParser.parse(dataHeadersCell(DataHeaderRow.ASSAY_READOUT_TYPE, iDataHeader, true)));
      }
      if (rvt.isActivityIndicator()) {
        rvt.setActivityIndicatorType(_activityIndicatorTypeParser.parse(dataHeadersCell(DataHeaderRow.ACTIVITY_INDICATOR_TYPE, iDataHeader, true)));
        if (rvt.getActivityIndicatorType().equals(ActivityIndicatorType.NUMERICAL)) {
          rvt.setIndicatorDirection(_indicatorDirectionParser.parse(dataHeadersCell(DataHeaderRow.NUMERICAL_INDICATOR_DIRECTION, iDataHeader, true)));
          rvt.setIndicatorCutoff(dataHeadersCell(DataHeaderRow.NUMERICAL_INDICATOR_CUTOFF, iDataHeader, true).getDouble());
        }
        // TODO: should warn if these values *are* defined and !isActivityIndicator()
      }
      rvt.setComments(dataHeadersCell(DataHeaderRow.COMMENTS, iDataHeader).getString());
    }
    return dataHeadersParseResult;
  }

  private void recordDataHeaderColumn(int iDataHeader)
  {
    assert _dataHeaderIndex2DataHeaderColumn != null :
      "uninitialized _dataHeaderIndex2DataHeaderColumn";
    Cell cell = dataHeadersCell(DataHeaderRow.COLUMN_IN_DATA_WORKSHEET, iDataHeader, true);
    String forColumnInRawDataWorksheet = cell.getString().trim();
    try {
      if (forColumnInRawDataWorksheet != null) {
        _dataHeaderIndex2DataHeaderColumn.put(iDataHeader,
                                              (short) Cell.columnLabelToIndex(forColumnInRawDataWorksheet));
      }
    }
    catch (IllegalArgumentException e) {
      _errors.addError(e.getMessage(), cell);
    }
  }

  /**
   * Parse the workbook containing the ScreenResult data.
   * 
   * @param workbook the workbook containing some or all of the raw data for a
   *          ScreenResult
   * @throws ExtantLibraryException if an existing Well entity cannot be found
   *           in the database
   * @throws IOException 
   * @throws UnrecoverableScreenResultParseException 
   */
  void parseData(
    Workbook workbook,
    ScreenResult screenResult) 
    throws ExtantLibraryException, IOException, UnrecoverableScreenResultParseException
  {
    int dataSheetsParsed = 0;
    int totalSheets = workbook.getWorkbook().getNumberOfSheets();
    int totalDataSheets = totalSheets - FIRST_DATA_SHEET_INDEX;
    for (int iSheet = FIRST_DATA_SHEET_INDEX; iSheet < totalSheets; ++iSheet) {
      String sheetName = workbook.getWorkbook().getSheetName(iSheet);
      log.info("parsing sheet " + (dataSheetsParsed + 1) + " of " + totalDataSheets + ", " + sheetName);
      final HSSFSheet sheet = initializeDataSheet(workbook, iSheet);
      DataRowIterator rowIter = new DataRowIterator(workbook, iSheet);
      
      int iDataHeader = 0;
      for (ResultValueType rvt : screenResult.getResultValueTypes()) {
        determineNumericalnessOfDataHeader(rvt, workbook, iSheet, iDataHeader++);
      }
      
      while (rowIter.hasNext()) {
        Integer iRow = rowIter.next();
        Well well = rowIter.getWell();

        // TODO: should verify with Library Well Type, if not specific to AssayWellType
        AssayWellType assayWellType = _assayWellTypeParser.parse(dataCell(iRow, DataColumn.ASSAY_WELL_TYPE));

        List<ResultValueType> wellExcludes = _excludeParser.parseList(dataCell(iRow, DataColumn.EXCLUDE));
        iDataHeader = 0;
        for (ResultValueType rvt : screenResult.getResultValueTypes()) {
          Cell cell = dataCell(iRow, iDataHeader);
          boolean isExclude = (wellExcludes != null && wellExcludes.contains(rvt));
          try {
            boolean resultValueAdded = false;
            if (rvt.isActivityIndicator()) {
              String value;
              if (rvt.getActivityIndicatorType() == ActivityIndicatorType.BOOLEAN) {
                if (cell.isBoolean()) {
                  value = cell.getBoolean().toString();
                } 
                else {
                  value = _booleanParser.parse(cell).toString();
                }
                resultValueAdded = 
                  rvt.addResultValue(well,
                                     assayWellType,
                                     value,
                                     isExclude);
              }
              else if (rvt.getActivityIndicatorType() == ActivityIndicatorType.PARTITION) {
                resultValueAdded = 
                  rvt.addResultValue(well,
                                     assayWellType,
                                     _partitionedValueParser.parse(cell).toString(),
                                     isExclude);
              }
              else if (rvt.getActivityIndicatorType() == ActivityIndicatorType.NUMERICAL) {
                resultValueAdded = 
                  rvt.addResultValue(well,
                                     assayWellType,
                                     cell.getDouble(),
                                     cell.getDoublePrecision(),
                                     isExclude);
              }
            }
            else { // not assay activity indicator
              if (rvt.isNumeric()) {
                resultValueAdded = 
                  rvt.addResultValue(well,
                                     assayWellType,
                                     cell.getDouble(),
                                     cell.getDoublePrecision(),
                                     isExclude);
              }
              else {
                resultValueAdded = 
                  rvt.addResultValue(well,
                                     assayWellType,
                                     cell.getString(),
                                     isExclude);
              }
            }
            if (!resultValueAdded) {
              _errors.addError("duplicate well", cell);
            }
          }
          catch (ResultValueTypeNumericalnessException e) {
            // inconsistency in numeric or string types in RVT's result values
            _errors.addError(e.getMessage(), cell);
          }
          ++iDataHeader;
        }
      }
      ++dataSheetsParsed;
      releaseMemory(new Runnable() {
        public void run() { sheet.releaseData(); }
      });
    }
    if (dataSheetsParsed == 0) {
      _errors.addError(NO_DATA_SHEETS_FOUND_ERROR);
    } else {
      log.info("done parsing " + dataSheetsParsed + " data sheet(s) " + workbook.getWorkbookFile().getName());
    }
  }

  /**
   * Determines if a data header contains numeric or non-numeric data, by
   * reading ahead and making the determination based upon the first non-empty
   * cell in the column for the specified data header. Note that the test may be
   * inconclusive for a given worksheet (if the entire column contains empty
   * cells), but that a later worksheet may used to make the determination.
   */
  private void determineNumericalnessOfDataHeader(ResultValueType rvt, 
                                                  Workbook workbook, 
                                                  int iSheet, 
                                                  int iDataHeader)
  {
    if (!rvt.isNumericalnessDetermined()) {
      if (rvt.getActivityIndicatorType() == ActivityIndicatorType.NUMERICAL) {
        rvt.setNumeric(true);
      } 
      else {
        DataRowIterator rowIter = new DataRowIterator(workbook, iSheet);
        while (rowIter.hasNext()) {
          Cell cell = dataCell(rowIter.next(), iDataHeader);
          if (cell.isEmpty()) {
            continue;
          }
          if (cell.isNumeric()) {
            rvt.setNumeric(true);
          }
          else if (cell.isBoolean()) {
            rvt.setNumeric(false);
          }
          else {
            rvt.setNumeric(false);
          }
          break;
        }
      }
    }
  }
  
  private class DataRowIterator implements Iterator<Integer>
  {
    private HSSFSheet _sheet;
    private int _iRow;
    private Well _well;
    private int _lastRowNum;

    public DataRowIterator(Workbook workbook,
                           int sheetIndex)
    {
      _sheet = workbook.getWorkbook().getSheetAt(sheetIndex);
      _iRow = RAWDATA_FIRST_DATA_ROW_INDEX - 1;
      _lastRowNum = _sheet.getLastRowNum();
    }
    
    public DataRowIterator(DataRowIterator rowIter)
    {
      _sheet = rowIter._sheet;
      _iRow = rowIter._iRow;
      _well = rowIter._well;
      _lastRowNum = rowIter._lastRowNum;
    }

    public boolean hasNext()
    {
      return findNextRow() != _iRow;
    }

    public Integer next()
    {
      int nextRow = findNextRow();
      if (nextRow != _iRow) {
        _iRow = nextRow;
        _well = findWell(_iRow);
        return getRowIndex();
      }
      else {
        _well = null;
        return null;
      }
    }
    
    public Integer getRowIndex()
    {
      return new Integer(_iRow);
    }
    
    public Well getWell()
    {
      return _well;
    }
    
    public void remove()
    {
      throw new UnsupportedOperationException();
    }

    private int findNextRow()
    {
      int iRow = _iRow;
      while (++iRow <= _lastRowNum) {
        if (!ignoreRow(_sheet, iRow)) {
          if (findWell(iRow) != null) {
            return iRow;
          }
        }
      }
      return _iRow;
    }

    private Well findWell(int iRow)
    {
      // TODO: dataCell() call assumes initializeDataSheet() has been called before this iterator object is used! (bad!)
      Integer plateNumber = _plateNumberParser.parse(dataCell(iRow,
                                                              DataColumn.STOCK_PLATE_ID,
                                                              true));
      Cell wellNameCell = dataCell(iRow,
                                   DataColumn.WELL_NAME,
                                   true);
      String wellName = _wellNameParser.parse(wellNameCell);
      if (wellName.equals("")) {
        return null;
      }
      Library library = findLibraryWithPlate(plateNumber);
      if (library == null) {
        _errors.addError(NO_SUCH_LIBRARY_WITH_PLATE + ": " + plateNumber, wellNameCell);
        return null;
      }
      preloadLibraryWells(library);
      
      Well well = _librariesDao.findWell(new WellKey(plateNumber, wellName));
      if (well == null) {
        _errors.addError(NO_SUCH_WELL + ": " + plateNumber + ":" + wellName, wellNameCell);
      }

      return well;
    }
  }

  /**
   * Determines if the logical row at the specified 0-based index should be ignored,
   * which is the case if the row is undefined, has no cells, or the first cell
   * is blank or contains the empty string or only whitespace.
   * 
   * @param sheet
   * @param rowIndex
   * @return
   */
  private boolean ignoreRow(HSSFSheet sheet, int rowIndex)
  {
    HSSFRow row = sheet.getRow(rowIndex);
    if (row == null) return true;
    if (row.getPhysicalNumberOfCells() == 0) {
      return true;
    }
    HSSFCell cell = row.getCell((short) 0);
    if (cell == null) {
      return true;
    }
    if (cell.getCellType() == HSSFCell.CELL_TYPE_BLANK) {
      return true;
    }
    if (cell.getStringCellValue() == null) {
      return true;
    }
    if (cell.getStringCellValue().trim().length() == 0) {
      return true;
    }
    return false;
  }

  /**
   * @motivation database I/O optimization
   */
  private Library findLibraryWithPlate(Integer plateNumber)
  {
    if (_lastLibrary == null ||
      !_lastLibrary.containsPlate(plateNumber)) {
      _lastLibrary = _librariesDao.findLibraryWithPlate(plateNumber); 
    }
    return _lastLibrary;
  }

  /**
   * @motivation database I/O optimization
   */
  private void preloadLibraryWells(Library library)
  {
    if (!_preloadedLibraries.contains(library)) {
      _librariesDao.loadOrCreateWellsForLibrary(library);
      _preloadedLibraries.add(library);
      //log.debug("flushing hibernate session after loading library");
      //releaseMemory(new Runnable() { public void run() { _dao.flush(); } });
    }
  }

  private static class ParsedScreenInfo {
    private Integer _screenId;
    private Date _date;

    public Date getDateCreated()
    {
      return _date;
    }

    public void setDate(Date date)
    {
      _date = date;
    }

    public Integer getScreenNumber()
    {
      return _screenId;
    }

    public void setScreenId(Integer screenId)
    {
      _screenId = screenId;
    }
  }
  
  /**
   * Parses the "Screen Info" worksheet.
   * <p>
   * For now, we just parse the ScreenResult date, but if we ever need to parse
   * more, we should return a composite object, rather than just a Date.
   * 
   * @throws UnrecoverableScreenResultParseException if a screen ID is not found
   */
  private ParsedScreenInfo parseScreenInfo(Workbook workbook, Screen screen) 
    throws UnrecoverableScreenResultParseException
  {
    ParsedScreenInfo parsedScreenInfo = new ParsedScreenInfo();
    int screenInfoSheetIndex;
    screenInfoSheetIndex = workbook.findSheetIndex(SCREEN_INFO_SHEET_NAME);
    HSSFSheet screenInfoSheet = workbook.getWorkbook().getSheetAt(screenInfoSheetIndex);
    Cell.Factory factory = new Cell.Factory(workbook,
                                            screenInfoSheetIndex,
                                            _errors);
    if (screenInfoSheet != null) {
      for (int iRow = screenInfoSheet.getFirstRowNum(); iRow < screenInfoSheet.getLastRowNum(); iRow++) {
        Cell labelCell = factory.getCell((short) 0, iRow);
        String rowLabel = labelCell.getString();
        if (rowLabel != null) {
          if (rowLabel.equalsIgnoreCase(ScreenInfoRow.DATE_FIRST_LIBRARY_SCREENING.getDisplayText())) {
            Cell valueCell = factory.getCell((short) 1, iRow, false);
            parsedScreenInfo.setDate(valueCell.getDate());
          }
          else if (rowLabel.equalsIgnoreCase(ScreenInfoRow.ID.getDisplayText())) {
            Cell valueCell = factory.getCell((short) 1, iRow, true);
            parsedScreenInfo.setScreenId(valueCell.getInteger());
          }
        }
      }
    }
    if (parsedScreenInfo.getScreenNumber() == null) {
      _errors.addError(NO_SCREEN_ID_FOUND_ERROR);
    }
    else if (!parsedScreenInfo.getScreenNumber().equals(screen.getScreenNumber())) {
      _errors.addError("screen result data file is for screen number " + 
                       parsedScreenInfo.getScreenNumber() + 
                       ", expected " + screen.getScreenNumber());
    }
    if (parsedScreenInfo.getDateCreated() == null) {
      if (screen.getScreeningRoomActivities().size() > 0) {
        SortedSet<ScreeningRoomActivity> sortedScreeningRoomActivities = 
          new TreeSet<ScreeningRoomActivity>(screen.getScreeningRoomActivities());
        parsedScreenInfo.setDate(sortedScreeningRoomActivities.first().getDateOfActivity());
      }
      else {
        log.warn("screen result's screen has no library screenings, so screen result's \"date created\" property will be set to today");
        parsedScreenInfo.setDate(new Date());
      }
    }
    return parsedScreenInfo;
  }
  
  /**
   * Annotate copies of parsed workbooks with parse errors. Only workbooks
   * containing errors are written. In fact, we simply save the workbooks, since
   * cells have already been modified in the in-memory representation.
   * 
   * @param newDirectory the output directory; if null the workbook's original
   *          file directory is used.
   * @param newExtension the extension to use when saving the workbook,
   *          replacing the workbook's original filename extension; if null
   *          original filename extension is used. A leading period will added
   *          iff it does not exist.
   * @return a Map of the <@link Workbook>s for which error-annotate workbooks
   *         (copies) were written out, mapped to their respective output file
   * @throws IOException
   */
  public Map<Workbook,File> outputErrorsInAnnotatedWorkbooks(
    File newDirectory,
    String newExtension) throws IOException
  {
    Map<Workbook,File> result = new HashMap<Workbook,File>();
    for (Workbook workbook : _errors.getWorkbooksWithErrors()) {
      File file = workbook.save(newDirectory,
                                newExtension);
      result.put(workbook, file);
    }
    return result;
  }
  
  public class ColumnLabelsParser implements CellValueParser<ResultValueType>
  {
    protected Map<String,ResultValueType> _columnLabel2RvtMap;
    private Pattern columnIdPattern = Pattern.compile("[A-Z]+");
    private ParseErrorManager _errors;
    

    // public methods
    
    public ColumnLabelsParser(Map<String,ResultValueType> columnLabel2RvtMap, ParseErrorManager errors)
    {
      _columnLabel2RvtMap =  columnLabel2RvtMap;
      _errors = errors;
    }

    public ResultValueType parse(Cell cell) 
    {
      throw new UnsupportedOperationException();
    }

    public List<ResultValueType> parseList(Cell cell)
    {
      String textMultiValue = cell.getString();
      List<ResultValueType> result = new ArrayList<ResultValueType>();

      if (textMultiValue == null || textMultiValue.trim().length() == 0) {
        return result;
      }
      
      String[] textValues = textMultiValue.split(",");
      for (int i = 0; i < textValues.length; i++) {
        String text = textValues[i].trim();
        ResultValueType rvt = doParseSingleValue(text, cell);
        if (rvt != null) {
          result.add(rvt);
        }
        else {
          _errors.addError("invalid Data Header column reference '" + text + 
            "' (expected one of " + _columnLabel2RvtMap.keySet() + ")", 
            cell);
        }
      }
      return result;
    }

    
    // private methods
    
    private ResultValueType doParseSingleValue(String value, Cell cell)
    {
      Matcher matcher = columnIdPattern.matcher(value);
      if (!matcher.matches()) {
        return null;
      }
      String columnLabel = matcher.group(0);
      return _columnLabel2RvtMap.get(columnLabel);
    }
  }

  private class ExcludeParser extends ColumnLabelsParser
  {

    // public methods
    
    public ExcludeParser(Map<String,ResultValueType> columnLabel2RvtMap, ParseErrorManager errors)
    {
      super(columnLabel2RvtMap, errors);
    }

    public List<ResultValueType> parseList(Cell cell)
    {
      String textMultiValue = cell.getString();
      List<ResultValueType> result = new ArrayList<ResultValueType>();

      if (textMultiValue != null && 
        textMultiValue.equalsIgnoreCase(ScreenResultWorkbookSpecification.EXCLUDE_ALL_VALUE)) {
        return new ArrayList<ResultValueType>(_columnLabel2RvtMap.values());
      }
      
      if (textMultiValue == null) {
        return result;
      }
      
      return super.parseList(cell);
    }
  }
  
  private void releaseMemory(Runnable runBeforeGarbageCollection)
  {
    long freeMem = 0L;
    //long usedMem = Runtime.getRuntime().totalMemory() - freeMem;
    if (log.isDebugEnabled()) {
      freeMem = Runtime.getRuntime().freeMemory();
      //log.debug(String.format("totalMem=%.2fMB", Runtime.getRuntime().totalMemory() / (1024.0*1024.0)));
      //log.debug(String.format("freeMem=%.2fMB", Runtime.getRuntime().freeMemory() / (1024.0*1024.0)));
      long availMem = (Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory()) + freeMem;
      log.debug(String.format("Before GC: avail mem=%.2fMB",  availMem / (1024.0*1024.0)));
    }
    runBeforeGarbageCollection.run();
    if (log.isDebugEnabled()) {
      Runtime.getRuntime().gc();
      //log.debug(String.format("freeMem=%.2fMB", Runtime.getRuntime().freeMemory() / (1024.0*1024.0)));
      long availMem = (Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory()) + Runtime.getRuntime().freeMemory();
      log.debug(String.format("After GC:  avail mem=%.2fMB, delta=%.2fMB",  
                              availMem / (1024.0*1024.0),
                              (freeMem - Runtime.getRuntime().freeMemory()) / (1024.0*1024.0)));
    }
  }

}
