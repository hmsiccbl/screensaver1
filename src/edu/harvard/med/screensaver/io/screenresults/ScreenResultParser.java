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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.db.ScreenResultsDAO;
import edu.harvard.med.screensaver.io.libraries.ExtantLibraryException;
import edu.harvard.med.screensaver.io.workbook2.Cell;
import edu.harvard.med.screensaver.io.workbook2.CellValueParser;
import edu.harvard.med.screensaver.io.workbook2.CellVocabularyParser;
import edu.harvard.med.screensaver.io.workbook2.Row;
import edu.harvard.med.screensaver.io.workbook2.WellNameParser;
import edu.harvard.med.screensaver.io.workbook2.Workbook;
import edu.harvard.med.screensaver.io.workbook2.WorkbookParseError;
import edu.harvard.med.screensaver.io.workbook2.Worksheet;
import edu.harvard.med.screensaver.model.DataModelViolationException;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.AssayWell;
import edu.harvard.med.screensaver.model.screenresults.AssayWellControlType;
import edu.harvard.med.screensaver.model.screenresults.DataColumn;
import edu.harvard.med.screensaver.model.screenresults.DataType;
import edu.harvard.med.screensaver.model.screenresults.PartitionedValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.AssayReadoutType;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.util.AlphabeticCounter;
import edu.harvard.med.screensaver.util.DevelopmentException;
import edu.harvard.med.screensaver.util.StringUtils;

import org.apache.commons.lang.math.IntRange;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Parses data from a workbook files (a.k.a. Excel spreadsheets) necessary for
 * instantiating a
 * {@link edu.harvard.med.screensaver.model.screenresults.ScreenResult}. A
 * {@link ScreenResult} is comprised of {@link DataColumn}s that in turn contain
 * {@link ResultValue}s. By convention, each worksheet contains the raw data for
 * a single plate, but the parser is indifferent to how data may be arranged
 * across worksheets.
 * <p>
 * The "Data Columns" worksheet is used to create
 * {@link DataColumn} objects,
 * while the raw data is used to instantiate each of the {@link DataColumn}s'
 * {@link ResultValue}
 * objects. Together, these objects are used instantiate a {@link ScreenResult}
 * object, which is the returned result of the {@link #parse} method.
 * <p>
 * The class attempts to parse the file(s) as fully as possible, carrying on in
 * the face of errors, in order to catch as many errors as possible, as this
 * will aid the manual effort of correcting the files' format and content
 * between import attempts. The {@link #getErrors()} method will
 * the errors messages encountered during parsing.
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
  private static final Logger log = Logger.getLogger(ScreenResultParser.class);

  /**
   * This controls the number of Rows to be read before flushing the hibernate cache and persisting all 
   * of the ResultValues that are being cached on the DataColumn objects.
   * This value should be matched to the hibernate.jdbc.batch_size property on the hibernateSessionFactory bean.
   */
  public static final int ROWS_TO_CACHE = 50;

  private static final String NO_SCREEN_ID_FOUND_ERROR = "Screen ID not found for row: ";
  private static final String DATA_COLUMNS_SHEET_NOT_FOUND_ERROR = "\"Data Columns\" sheet not found";
  private static final String UNKNOWN_ERROR = "unknown error";
  private static final String NO_DATA_SHEETS_FOUND_ERROR = "no data worksheets were found; no result data was imported";
  private static final String NO_SUCH_WELL = "library well does not exist";
  private static final String NO_SUCH_LIBRARY_WITH_PLATE = "no library with given plate number";
  private static final String ASSAY_WELL_TYPE_INCONSISTENCY = "assay well type cannot be changed";

  private static SortedMap<String,AssayReadoutType> assayReadoutTypeMap = new TreeMap<String,AssayReadoutType>();
  private static SortedMap<String,DataType> dataTypeMap = new TreeMap<String,DataType>();
  private static SortedMap<String,Boolean> primaryOrFollowUpMap = new TreeMap<String,Boolean>();
  private static SortedMap<String,Boolean> booleanMap = new TreeMap<String,Boolean>();
  private static SortedMap<String,PartitionedValue> partitionedValueMap = new TreeMap<String,PartitionedValue>();
  private static SortedMap<String,AssayWellControlType> assayWellControlTypeMap = new TreeMap<String,AssayWellControlType>();
  static {
    for (AssayReadoutType assayReadoutType : AssayReadoutType.values()) {
      assayReadoutTypeMap.put(assayReadoutType.getValue(),
                              assayReadoutType);
    }

    for (DataType dataType : DataType.values()) {
      dataTypeMap.put(dataType.getValue(), dataType);
    }

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

    for (AssayWellControlType awct : AssayWellControlType.values()) {
      assayWellControlTypeMap.put(awct.getAbbreviation(), awct);
    }
  }

  private LibrariesDAO _librariesDao;
  private ScreenResultsDAO _screenResultsDao;
  private edu.harvard.med.screensaver.db.GenericEntityDAO _genericEntityDao;

  /**
   * The ScreenResult object to be populated with data parsed from the spreadsheet.
   */
  private ScreenResult _screenResult;
  private Workbook _workbook;

  private ColumnLabelsParser _columnsDerivedFromParser;
  private ExcludeParser _excludeParser;
  private CellVocabularyParser<AssayReadoutType> _assayReadoutTypeParser;
  private CellVocabularyParser<DataType> _dataTypeParser;
  private CellVocabularyParser<Boolean> _rawOrDerivedParser;
  private CellVocabularyParser<Boolean> _primaryOrFollowUpParser;
  private CellVocabularyParser<Boolean> _booleanParser;
  private CellVocabularyParser<PartitionedValue> _partitionedValueParser;
  private CellVocabularyParser<AssayWellControlType> _assayWellControlTypeParser;
  private WellNameParser _wellNameParser;
  
  private SortedMap<String,DataColumn> _worksheetColumnLabel2DataColumnObjectMap;
  /**
   * @motivation runtime detection of duplicate wells in the input stream
   */
  private Set<String> parsedWellKeys = new HashSet<String>();
  private Map<Integer,Integer> _dataColumnIndex2WorksheetColumnIndex;
  private boolean _ignoreDuplicateErrors = false;
  /**
   * The library that was associated with the plate that was last accessed.
   * @motivation optimization for findLibraryWithPlate(); reduce db I/O
   */
  private Library _lastLibrary;



  public ScreenResultParser(LibrariesDAO librariesDao, 
                            ScreenResultsDAO screenResultsDao, 
                            GenericEntityDAO genericEntityDao)
  {
    _librariesDao = librariesDao;
    _screenResultsDao = screenResultsDao;
    _genericEntityDao = genericEntityDao;
  }

  public void setIgnoreDuplicateErrors(boolean value)
  {
    _ignoreDuplicateErrors = value;
  }

  /**
   * Main function.
   * Parses the specified workbook file that contains Screen Result data in the
   * <a
   * href="https://wiki.med.harvard.edu/ICCBL/NewScreenResultFileFormat">"new"
   * format</a>. Errors encountered during parsing are stored with this object
   * until a parse() method is called again, and these errors can be retrieved
   * via {@link #getErrors}. The returned <code>ScreenResult</code> may only
   * be partially populated if errors are encountered, so always call
   * getErrors() to determine parsing success.
   *
   * @param screen the parent Screen of the Screen Result being parsed
   * @param workbookFile the workbook file to be parsed
   * @param plateNumberRange the range of plate numbers to be parsed, allowing for only a subset
   *          of the data to be imported. This may be required for resource
   *          utilization purposes, where the ScreenResult must be imported over
   *          multiple passes. If null, well data for all plates will be imported.
   * @return a ScreenResult object containing the data parsed from the workbook
   *         file; <code>null</code> if a fatal error occurs (e.g. file not
   *         found)
   * @see #getErrors()
   */
  public ScreenResult parse(Screen screen, Workbook workbook, IntRange plateNumberRange, boolean incrementalFlush)
  {
    _workbook = workbook;
    return doParse(screen,
                   plateNumberRange,
                   incrementalFlush);
  }

  /**
   * Note: incrementalFlush is turned off by default
   */
  public ScreenResult parse(Screen screen, File workbookFile)
    throws FileNotFoundException
  {
    return parse(screen, workbookFile, null, false);
  }
  
  public ScreenResult parse(Screen screen, File workbookFile, boolean incrementalFlush)
    throws FileNotFoundException
  {
    return parse(screen, workbookFile, null, incrementalFlush);
  }

  public ScreenResult parse(Screen screen, File workbookFile, IntRange plateNumberRange, boolean incrementalFlush)
    throws FileNotFoundException
  {
    return parse(screen, new Workbook(workbookFile), plateNumberRange, incrementalFlush);
  }

  private ScreenResult doParse(Screen screen,
                               IntRange plateNumberRange,
                               boolean incrementalFlush)
  {
    _screenResult = null;
    _lastLibrary = null;
    _assayReadoutTypeParser = new CellVocabularyParser<AssayReadoutType>(assayReadoutTypeMap);
    _worksheetColumnLabel2DataColumnObjectMap = new TreeMap<String,DataColumn>();
    _columnsDerivedFromParser = new ColumnLabelsParser(_worksheetColumnLabel2DataColumnObjectMap);
    _excludeParser = new ExcludeParser(_worksheetColumnLabel2DataColumnObjectMap);
    _dataTypeParser = new CellVocabularyParser<DataType>(dataTypeMap);
    _primaryOrFollowUpParser = new CellVocabularyParser<Boolean>(primaryOrFollowUpMap, Boolean.FALSE);
    _booleanParser = new CellVocabularyParser<Boolean>(booleanMap, Boolean.FALSE);
    _partitionedValueParser = new CellVocabularyParser<PartitionedValue>(partitionedValueMap, PartitionedValue.NONE);
    _assayWellControlTypeParser = new CellVocabularyParser<AssayWellControlType>(assayWellControlTypeMap);
    _wellNameParser = new WellNameParser();

    try {
      log.info("parsing " + _workbook.getName());
      if (screen.getScreenResult() == null) {
        _screenResult = screen.createScreenResult();
        if (!parseDataColumnDefinitions(_screenResult, _workbook)) {
          log.info("errors found in data column definitions, will not attempt to parse data sheets");
          return _screenResult;
        }
      }
      else {
        // incremental parsing of new data
        _screenResult = screen.getScreenResult();
      }

      initializeDataColumnLocations(_screenResult, _workbook);
      log.info("parsing data sheets");
      parseData(_workbook,
                _screenResult,
                plateNumberRange,
                incrementalFlush);
      _screenResult.setDateLastImported(new DateTime());
    }
    catch (UnrecoverableScreenResultParseException e) {
      _workbook.addError("serious parse error encountered (could not continue further parsing): " + e.getMessage());
    }
    catch (Exception e) {
      e.printStackTrace();
      String errorMsg = UNKNOWN_ERROR + " of type : " + e.getClass() + ": " + e.getMessage();
      _workbook.addError(errorMsg);
    }
    return _screenResult;
  }

  public boolean getHasErrors()
  {
    return _workbook.getHasErrors();
  }

  /**
   * Return all errors that were detected during parsing. This class attempts to
   * parse as much of the workbook as possible, continuing on after finding an
   * error. The hope is that multiple errors will help a user/administrator
   * correct a workbook's errors in a batch fashion, rather than in a piecemeal
   * fashion.
   *
   * @return a <code>List&lt;String&gt;</code> of all errors generated during
   *         parsing
   */
  public List<WorkbookParseError> getErrors()
  {
    return _workbook.getErrors();
  }

  public ScreenResult getParsedScreenResult()
  {
    return _screenResult;
  }

  /**
   * Finds the total number of data columns.
   * TODO: this does not account for non-contiguous blocks of empty cells
   * @param dataColumnsSheet
   * @return highest cell column index plus 1
   * @throws UnrecoverableScreenResultParseException
   */
  private int findDataColumnCount(Worksheet dataColumnsSheet) 
  {
    dataColumnsSheet.getColumns();
    int rows = dataColumnsSheet.getRows();
    
    if (rows == 0) {
      return 0;
    }
    
    int n = 0;
    for (Cell cell : dataColumnsSheet.getRow(0)) {
      if(cell.isEmpty()) break;
      n++;
    }
    return n;
  }

  private int getDataColumn(int dataColumn)
  {
    return _dataColumnIndex2WorksheetColumnIndex.get(dataColumn);
  }

  /**
   * Parse the worksheet containing the ScreenResult data columns.
   * This method returns error results by add them to the {@link Workbook}. 
   * Therefore check the workbook after running for errors.
   * @param workbook
   * 
   */
  private boolean parseDataColumnDefinitions(ScreenResult screenResult, Workbook workbook)
  {
    log.info("parse data columns sheet");
    
    Worksheet dataColumnsSheet = 
      _workbook.getWorksheet(DATA_COLUMNS_SHEET_NAME).forOrigin(DATA_COLUMNS_SHEET__FIRST_DATA_COLUMN__WORKSHEET_COLUMN_INDEX, 0);
    if (dataColumnsSheet == null) {
      _workbook.addError(DATA_COLUMNS_SHEET_NOT_FOUND_ERROR);
      return false;
    } 
    Map<DataColumnProperty,Row> dataColumnPropertyRows = parseDataColumnPropertyNames(dataColumnsSheet);
    int dataColumnCount = findDataColumnCount(dataColumnsSheet);
    for (int iDataColumn = 0; iDataColumn < dataColumnCount; ++iDataColumn) {
      if (!!!dataColumnPropertyRows.containsKey(DataColumnProperty.NAME)) {
        _workbook.addError(DataColumnProperty.NAME + " data column property is required");
      }
      DataColumn dataColumn = 
        screenResult.createDataColumn(dataColumnPropertyRows.get(DataColumnProperty.NAME).getCell(iDataColumn, true).getString());
      if (dataColumnPropertyRows.containsKey(DataColumnProperty.DATA_TYPE)) {
        Cell cell = dataColumnPropertyRows.get(DataColumnProperty.DATA_TYPE).getCell(iDataColumn, true);
        DataType dataType = _dataTypeParser.parse(cell);
        if (dataType != null) {
          switch (dataType) {
          case NUMERIC: {
            Integer decimalPlaces = null;
            if (dataColumnPropertyRows.containsKey(DataColumnProperty.DECIMAL_PLACES)) {
              Cell cell2 = dataColumnPropertyRows.get(DataColumnProperty.DECIMAL_PLACES).getCell(iDataColumn);
              decimalPlaces = cell2.getInteger();
              if (decimalPlaces != null && decimalPlaces < 0) {
                cell2.addError("illegal value");
              }
            }
            dataColumn.makeNumeric(decimalPlaces);
            break;
          }
          case TEXT: dataColumn.makeTextual(); break;
          case POSITIVE_INDICATOR_BOOLEAN: dataColumn.makeBooleanPositiveIndicator(); break;
          case POSITIVE_INDICATOR_PARTITION: dataColumn.makePartitionPositiveIndicator(); break;
          default: throw new DevelopmentException("unhandled data type " + dataType);
          }
        }
      }
      else {
        dataColumn.makeNumeric(null);
      }
      
      if (dataColumnPropertyRows.containsKey(DataColumnProperty.REPLICATE)) {
        dataColumn.forReplicate(dataColumnPropertyRows.get(DataColumnProperty.REPLICATE).getCell(iDataColumn).getInteger());
      }
      String howDerived = null;
      if (dataColumnPropertyRows.containsKey(DataColumnProperty.HOW_DERIVED)) {
        howDerived = dataColumnPropertyRows.get(DataColumnProperty.HOW_DERIVED).getCell(iDataColumn).getString();
      }
      Set<DataColumn> columnsDerivedFrom = Collections.emptySet();
      if (dataColumnPropertyRows.containsKey(DataColumnProperty.COLUMNS_DERIVED_FROM)) {
        columnsDerivedFrom = Sets.newHashSet(Iterables.filter(_columnsDerivedFromParser.parseList(dataColumnPropertyRows.get(DataColumnProperty.COLUMNS_DERIVED_FROM).getCell(iDataColumn)),
                                                              Predicates.notNull()));
      }
      if (!!!(StringUtils.isEmpty(howDerived) && columnsDerivedFrom.isEmpty())) { 
        dataColumn.makeDerived(howDerived, columnsDerivedFrom);
      }
      if (!!!dataColumn.isDerived()) {
        if (dataColumnPropertyRows.containsKey(DataColumnProperty.ASSAY_READOUT_TYPE)) {
          dataColumn.setAssayReadoutType(_assayReadoutTypeParser.parse(dataColumnPropertyRows.get(DataColumnProperty.ASSAY_READOUT_TYPE).getCell(iDataColumn, true)));
        }
      }
      if (dataColumnPropertyRows.containsKey(DataColumnProperty.PRIMARY_OR_FOLLOWUP)) {
        dataColumn.setFollowUpData(_primaryOrFollowUpParser.parse(dataColumnPropertyRows.get(DataColumnProperty.PRIMARY_OR_FOLLOWUP).getCell(iDataColumn)));
      }
      if (dataColumnPropertyRows.containsKey(DataColumnProperty.ASSAY_PHENOTYPE)) {
        dataColumn.forPhenotype(dataColumnPropertyRows.get(DataColumnProperty.ASSAY_PHENOTYPE).getCell(iDataColumn).getString());
      }
      if (dataColumnPropertyRows.containsKey(DataColumnProperty.DESCRIPTION)) {
        dataColumn.setDescription(dataColumnPropertyRows.get(DataColumnProperty.DESCRIPTION).getCell(iDataColumn).getString());
      }
      if (dataColumnPropertyRows.containsKey(DataColumnProperty.COMMENTS)) {
        dataColumn.setComments(dataColumnPropertyRows.get(DataColumnProperty.COMMENTS).getCell(iDataColumn).getString());
      }
      if (dataColumnPropertyRows.containsKey(DataColumnProperty.TIME_POINT)) {
        dataColumn.forTimePoint(dataColumnPropertyRows.get(DataColumnProperty.TIME_POINT).getCell(iDataColumn).getString());
      }
      if (dataColumnPropertyRows.containsKey(DataColumnProperty.TIME_POINT_ORDINAL)) {
        dataColumn.forTimePointOrdinal(dataColumnPropertyRows.get(DataColumnProperty.TIME_POINT_ORDINAL).getCell(iDataColumn).getInteger());
      }
      if (dataColumnPropertyRows.containsKey(DataColumnProperty.CHANNEL)) {
        dataColumn.forChannel(dataColumnPropertyRows.get(DataColumnProperty.CHANNEL).getCell(iDataColumn).getInteger());
      }
      if (dataColumnPropertyRows.containsKey(DataColumnProperty.ZDEPTH_ORDINAL)) {
        dataColumn.forZdepthOrdinal(dataColumnPropertyRows.get(DataColumnProperty.ZDEPTH_ORDINAL).getCell(iDataColumn).getInteger());
      }
      // note: we do this last so that _columnsDerivedFromParser does not allow the current column to be considered a valid "derived from" value
      _worksheetColumnLabel2DataColumnObjectMap.put(dataColumnPropertyRows.get(DataColumnProperty.COLUMN_IN_DATA_WORKSHEET).getCell(iDataColumn, true).getAsString(), dataColumn);
    }
    return !!!_workbook.getHasErrors();
  }

  private boolean validateRequiredDataPropertyDefined(DataColumnProperty requiredDataColumnProperty,
                                                      DataColumnProperty parentDataColumnProperty,
                                                      Map<DataColumnProperty,Row> dataColumnPropertyRows,
                                                      int iDataColumn)
  {
    if (!!!dataColumnPropertyRows.containsKey(requiredDataColumnProperty)) {
      dataColumnPropertyRows.get(parentDataColumnProperty).getCell(iDataColumn).addError(requiredDataColumnProperty + " data column property row must be defined when " + parentDataColumnProperty + " data column property row is defined");
      return false;
    }
    return true;
  }

  private Map<DataColumnProperty,Row> parseDataColumnPropertyNames(Worksheet dataColumnsSheet)
  {
    Map<DataColumnProperty,Row> result = Maps.newHashMap();
    Iterator<Row> dataRows = dataColumnsSheet.forOrigin(1, 0).iterator(); 
    for (Row row : dataColumnsSheet.forOrigin(0, 0)) {
      if (row.isEmpty()) {
        break;
      }
      
      String dataColumnPropertyLabel = row.getCell(0).getString().trim();
      DataColumnProperty dataColumnProperty = DataColumnProperty.fromDisplayText(dataColumnPropertyLabel);
      Row dataOnlyRow = dataRows.next();
      if (dataColumnProperty != null) {
        result.put(dataColumnProperty, dataOnlyRow);
      }
      else {
        row.getCell(0).addError("unknown data column property: " + dataColumnPropertyLabel);
      }
    }
    return result;
  }

  private void initializeDataColumnLocations(ScreenResult screenResult,
                                             Workbook workbook)
    throws UnrecoverableScreenResultParseException
  {
    Worksheet dataColumnsSheet = workbook.getWorksheet(DATA_COLUMNS_SHEET_NAME).forOrigin(DATA_COLUMNS_SHEET__FIRST_DATA_COLUMN__WORKSHEET_COLUMN_INDEX, 0);
    if (dataColumnsSheet == null) { 
      throw new UnrecoverableScreenResultParseException(DATA_COLUMNS_SHEET_NOT_FOUND_ERROR);
    }

    _dataColumnIndex2WorksheetColumnIndex = Maps.newHashMap();

    int dataColumnCount = findDataColumnCount(dataColumnsSheet);
    Map<DataColumnProperty,Row> dataColumnProperties = parseDataColumnPropertyNames(dataColumnsSheet);
    for (int iDataColumn = 0; iDataColumn < dataColumnCount; ++iDataColumn) {
      Cell cell = dataColumnProperties.get(DataColumnProperty.COLUMN_IN_DATA_WORKSHEET).getCell(iDataColumn, true);
      String forColumnInDataWorksheet = cell.getString().trim();
      try {
        if (forColumnInDataWorksheet != null) {
          _dataColumnIndex2WorksheetColumnIndex.put(iDataColumn,
                                                    AlphabeticCounter.toIndex(forColumnInDataWorksheet));
          DataColumn dataColumn = screenResult.getDataColumnsList().get(iDataColumn);
          _worksheetColumnLabel2DataColumnObjectMap.put(forColumnInDataWorksheet, dataColumn);
        }
      }
      catch (IllegalArgumentException e) {
        cell.addError(e.getMessage());
      }
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
  private void parseData(Workbook workbook,
                         ScreenResult screenResult,
                         IntRange plateNumberRange,
                         boolean incrementalFlush)
    throws ExtantLibraryException, IOException, UnrecoverableScreenResultParseException
  {
    log.info("parseData: incrementalFlush:" + incrementalFlush);
    long startTime = System.currentTimeMillis();
    long loopTime = startTime;
    
    int wellsWithDataLoaded = 0;
    int dataSheetsParsed = 0;
    int totalSheets = workbook.getWorkbook().getNumberOfSheets();
    int firstDataSheetIndex = workbook.getWorksheet(DATA_COLUMNS_SHEET_NAME).getSheetIndex() + 1;
    int totalDataSheets = Math.max(0, totalSheets - firstDataSheetIndex);
    plateNumberRange = plateNumberRange == null ? new IntRange(Integer.MIN_VALUE, Integer.MAX_VALUE) : plateNumberRange;

    // Note: we do this to make sure that the DataColumn's are persisted before we persist and clear the RV's
    if (screenResult.getEntityId() == null) {
      _genericEntityDao.persistEntity(screenResult);
    }
    else {
      _genericEntityDao.saveOrUpdateEntity(screenResult);
    }
    _genericEntityDao.flush();

    for (int iSheet = firstDataSheetIndex; iSheet < totalSheets; ++iSheet) {
      String sheetName = workbook.getWorkbook().getSheet(iSheet).getName();
      log.info("parsing sheet " + (dataSheetsParsed + 1) + " of " + totalDataSheets + ", " + sheetName);
      Worksheet worksheet = workbook.getWorksheet(iSheet).forOrigin(0, DATA_SHEET__FIRST_DATA_ROW_INDEX);

      for (Row row : worksheet) {
        // bring in the old findNextRow() logic
        if (row.getColumns() > 0 
          && !row.getCell(0).isEmpty()            
          && row.getCell(0).getAsString().trim().length() > 0 ) 
        {
          Integer plateNumber = row.getCell(WellInfoColumn.PLATE.ordinal(), true).getInteger();
          Cell wellNameCell = row.getCell(WellInfoColumn.WELL_NAME.ordinal()); 
          String wellName = _wellNameParser.parse(wellNameCell);
          if (!wellName.equals("")) {
            WellKey wellKey = new WellKey(plateNumber, wellName);
            if (!plateNumberRange.containsInteger(wellKey.getPlateNumber())) {
              if (log.isDebugEnabled()) {
                log.debug("Skipping, excluded range: " + plateNumberRange + ", row: " + row.getRow());
              }
            } 
            else {
              boolean duplicate = !parsedWellKeys.add(wellKey.getKey());
              if (duplicate) {
                if (!_ignoreDuplicateErrors) {
                  wellNameCell.addError("already parsed this well");
                }
                else {
                  log.debug("Already parsed: " + wellKey + ", duplicate found at: " + wellNameCell );
                }
              } 
              else {
                if (findLibraryWithPlate(wellKey.getPlateNumber()) == null) 
                {
                  wellNameCell.addError(NO_SUCH_LIBRARY_WITH_PLATE);
                } 
                else {
                  Well well = _librariesDao.findWell(wellKey);
                  if (well == null) {
                    wellNameCell.addError(NO_SUCH_WELL + ": " + wellKey);
                  } 
                  else {
                    readResultValues(screenResult, row, well, incrementalFlush);
                    ++wellsWithDataLoaded;
                    if (incrementalFlush && wellsWithDataLoaded % ROWS_TO_CACHE == 0) {
                      saveResultValuesAndFlush(screenResult, incrementalFlush);
                      if (log.isInfoEnabled() && wellsWithDataLoaded % (ROWS_TO_CACHE * 100) == 0) {
                        long time = System.currentTimeMillis();
                        long cumulativeTime = time - startTime;
                        log.info("wellsWithDataLoaded: " + wellsWithDataLoaded 
                                 + ", cumulative time: " + (double)cumulativeTime/(double)60000 
                                 + " min, avg row time: " + (double)cumulativeTime/(double)wellsWithDataLoaded 
                                 + ", loopTime: " + (time - loopTime) );
                        loopTime = time;
                      }
                    } // incremental
                  }
                }
              }
            }
          }
        }
      } // for row
      ++dataSheetsParsed;
      if (wellsWithDataLoaded > 0) {
        saveResultValuesAndFlush(screenResult, incrementalFlush);
        log.info("Sheet: " + sheetName + " done, save, count: " + wellsWithDataLoaded);
        long time = System.currentTimeMillis();
        long cumulativeTime = time - startTime;
        log.info("wellsWithDataLoaded: " + wellsWithDataLoaded 
                 + ", cumulative time: " + (double)cumulativeTime/(double)60000 
                 + " min, avg row time: " + (double)cumulativeTime/(double)wellsWithDataLoaded );
      }    
    }

    if (dataSheetsParsed == 0) {
      _workbook.addError(NO_DATA_SHEETS_FOUND_ERROR);
    } else {
      log.info("done parsing " + dataSheetsParsed + " data sheet(s) " + workbook.getName());
      log.info("loaded data for " + wellsWithDataLoaded + " well(s) ");
    }
  }

  private void saveResultValuesAndFlush(ScreenResult screenResult, boolean incrementalFlush)
  {
    log.debug("incrementally save the screen result and clear values");
    for (DataColumn dataColumn : screenResult.getDataColumns()) {
      _genericEntityDao.saveOrUpdateEntity(dataColumn); 
      //TODO: this should not be required, but the writes to the DB were missing some of the RV's and this fixed it -sde4
      for(ResultValue rv : dataColumn.getResultValues())
      {
        _genericEntityDao.saveOrUpdateEntity(rv);
      }
    }
    if(incrementalFlush)
    {
      for (DataColumn dataColumn : screenResult.getDataColumns()) {
        dataColumn.clearResultValues();
      }
      for(AssayWell assayWell: screenResult.getAssayWells())
      {
        _genericEntityDao.saveOrUpdateEntity(assayWell);
      }
      screenResult.getAssayWells().clear();
    }
    _genericEntityDao.flush();
    _genericEntityDao.clear();
  }

  private void readResultValues(ScreenResult screenResult, Row row, Well well, boolean incrementalFlush)
  {
    AssayWellControlType assayWellControlType = 
      _assayWellControlTypeParser.parse(row.getCell(WellInfoColumn.ASSAY_WELL_TYPE.ordinal()));
    try {
      AssayWell assayWell = findOrCreateAssayWell(well, assayWellControlType);
      List<DataColumn> wellExcludes = _excludeParser.parseList(row.getCell(WellInfoColumn.EXCLUDE.ordinal()));
      int iDataColumn = 0;
      for (DataColumn dataColumn : screenResult.getDataColumns()) 
      {
        Cell cell = row.getCell(getDataColumn(iDataColumn));
        boolean isExclude = (wellExcludes != null && wellExcludes.contains(dataColumn));

        ResultValue newResultValue = null;
        if (dataColumn.isPositiveIndicator()) {
          String value;
          if (dataColumn.isBooleanPositiveIndicator()) {
            if (cell.isBoolean()) {
              value = cell.getBoolean().toString();
            }
            else {
              value = _booleanParser.parse(cell).toString();
            }
            newResultValue =
              dataColumn.createResultValue(assayWell,
                                    value,
                                    isExclude);
          }
          else if (dataColumn.isPartitionPositiveIndicator()) {
            newResultValue =
              dataColumn.createResultValue(assayWell,
                                    _partitionedValueParser.parse(cell).toString(),
                                    isExclude);
          }
          else {
            throw new DevelopmentException("unhandled positive indicator type " + dataColumn.getDataType());
          }
        }
        else { // not assay activity indicator
          if (dataColumn.isNumeric()) {
            newResultValue =
              dataColumn.createResultValue(assayWell,
                                    cell.getDouble(),
                                    isExclude);
          }
          else {
            newResultValue =
              dataColumn.createResultValue(assayWell,
                                    cell.getString(),
                                    isExclude);
          }
        }
        if (newResultValue == null) {
          cell.addError("duplicate well");
        }

        ++iDataColumn;
      } // DataColumns
  
      if(incrementalFlush)
      {
        // [#2119] Optimize ScreenResultParser for scalability:
        // - in memory RV's must be reloaded as needed
        // - many-to-many relationship screen_result_well_link will be populated manually see ScreenResultsDao.saveScreenResultWellLinkTable()
        assayWell.getLibraryWell().getResultValues().clear();
        
        // TODO (once implemented): assayWell.getResultValues().clear();
        screenResult.getWells().clear();
        //screenResult.getAssayWells().clear();
      }
    }
    catch (DataModelViolationException e) 
    {
      row.getCell(WellInfoColumn.ASSAY_WELL_TYPE.ordinal()).addError(e.getMessage());
    }
  }

  private AssayWell findOrCreateAssayWell(Well well, AssayWellControlType assayWellControlType)
    throws DataModelViolationException
  {
    // TODO: assay well should not already exist, unless we start supporting the appending of new data columns to existing assay well data  
//    AssayWell assayWell = _screenResultsDao.findAssayWell(_screenResult, wellKey);
//    if (assayWell != null) {
//      if (assayWell.getAssayWellType() != assayWellType) {
//        _workbook.addError(ASSAY_WELL_TYPE_INCONSISTENCY + ": " + wellKey);
//        return null;
//      }
//      return assayWell;
//    }
    AssayWell assayWell = _screenResult.createAssayWell(well);
    assayWell.setAssayWellControlType(assayWellControlType);
    return assayWell;
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

  private static class ParsedScreenInfo {
    private Integer _screenId;

    public Integer getScreenId()
    {
      return _screenId;
    }

    public void setScreenId(Integer screenId)
    {
      _screenId = screenId;
    }
  }

  public class ColumnLabelsParser implements CellValueParser<DataColumn>
  {
    protected Map<String,DataColumn> _columnLabel2ColMap;
    private Pattern columnIdPattern = Pattern.compile("[A-Z]+");

    public ColumnLabelsParser(Map<String,DataColumn> columnLabel2ColMap)
    {
      _columnLabel2ColMap =  columnLabel2ColMap;
    }

    public DataColumn parse(Cell cell)
    {
      throw new UnsupportedOperationException();
    }

    public List<DataColumn> parseList(Cell cell)
    {
      String textMultiValue = cell.getString();
      List<DataColumn> result = new ArrayList<DataColumn>();

      if (textMultiValue == null || textMultiValue.trim().length() == 0) {
        return result;
      }

      String[] textValues = textMultiValue.split(",");
      for (int i = 0; i < textValues.length; i++) {
        String text = textValues[i].trim();
        DataColumn dataColumn = doParseSingleValue(text, cell);
        if (dataColumn != null) {
          result.add(dataColumn);
        }
        else {
          cell.addError("invalid Data Column worksheet column label '" + text +
            "' (expected one of " + _columnLabel2ColMap.keySet() + ")");
        }
      }
      return result;
    }

    protected DataColumn doParseSingleValue(String value, Cell cell)
    {
      Matcher matcher = columnIdPattern.matcher(value);
      if (!matcher.matches()) {
        return null;
      }
      String columnLabel = matcher.group(0);
      return _columnLabel2ColMap.get(columnLabel);
    }
  }

  private class ExcludeParser extends ColumnLabelsParser
  {
    public ExcludeParser(Map<String,DataColumn> columnLabel2ColMap)
    {
      super(columnLabel2ColMap);
    }

    public List<DataColumn> parseList(Cell cell)
    {
      String textMultiValue = cell.getString();

      if (textMultiValue != null &&
        textMultiValue.equalsIgnoreCase(ScreenResultWorkbookSpecification.EXCLUDE_ALL_VALUE)) {
        return new ArrayList<DataColumn>(_columnLabel2ColMap.values());
      }

      if (textMultiValue == null) {
        return Collections.emptyList();
      }

      return super.parseList(cell);
    }
  }

}
