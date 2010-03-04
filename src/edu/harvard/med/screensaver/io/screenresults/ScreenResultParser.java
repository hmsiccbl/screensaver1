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
import edu.harvard.med.screensaver.model.screenresults.AssayWellType;
import edu.harvard.med.screensaver.model.screenresults.PartitionedValue;
import edu.harvard.med.screensaver.model.screenresults.PositiveIndicatorType;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ResultValueTypeNumericalnessException;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.AssayReadoutType;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.util.AlphabeticCounter;
import edu.harvard.med.screensaver.util.DevelopmentException;

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
 * between import attempts. By calling {@link #getErrorAnnotatedWorkbook()}, a
 * new error-annotated workbook will be generated (in memory only), containing
 * errors messages in each cell that encountered an error during parsing. Error
 * messages that are not cell-specific will be written to a new "Parse Errors"
 * sheet in the error-annotated workbook.
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
   * of the ResultValues that are being cached on the ResultValueType objects.
   * This value should be matched to the hibernate.jdbc.batch_size property on the hibernateSessionFactory bean.
   */
  public static final int ROWS_TO_CACHE = 50;

  private static final String NO_SCREEN_ID_FOUND_ERROR = "Screen ID not found for row: ";
  private static final String DATA_HEADER_SHEET_NOT_FOUND_ERROR = "\"Data Headers\" sheet not found";
  private static final String UNKNOWN_ERROR = "unknown error";
  private static final String NO_DATA_SHEETS_FOUND_ERROR = "no data worksheets were found; no result data was imported";
  private static final String NO_SUCH_WELL = "library well does not exist";
  private static final String NO_SUCH_LIBRARY_WITH_PLATE = "no library with given plate number";
  private static final String ASSAY_WELL_TYPE_INCONSISTENCY = "assay well type cannot be changed";

  private static SortedMap<String,AssayReadoutType> assayReadoutTypeMap = new TreeMap<String,AssayReadoutType>();
  private static SortedMap<String,PositiveIndicatorType> activityIndicatorTypeMap = new TreeMap<String,PositiveIndicatorType>();
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

    activityIndicatorTypeMap.put("Boolean", PositiveIndicatorType.BOOLEAN);
    activityIndicatorTypeMap.put("Partitioned", PositiveIndicatorType.PARTITION);
    activityIndicatorTypeMap.put("Partition", PositiveIndicatorType.PARTITION);

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
    assayWellTypeMap.put(AssayWellType.ASSAY_CONTROL_SHARED.getAbbreviation(), AssayWellType.ASSAY_CONTROL_SHARED);
    assayWellTypeMap.put(AssayWellType.ASSAY_CONTROL.getAbbreviation(), AssayWellType.ASSAY_CONTROL);
    assayWellTypeMap.put(AssayWellType.BUFFER.getAbbreviation(), AssayWellType.BUFFER);
    assayWellTypeMap.put(AssayWellType.DMSO.getAbbreviation(), AssayWellType.DMSO);
    assayWellTypeMap.put(AssayWellType.OTHER.getAbbreviation(), AssayWellType.OTHER);
    assert assayWellTypeMap.size() == AssayWellType.values().length : "assayWellTypeMap not initialized properly";
  }

  private LibrariesDAO _librariesDao;
  private ScreenResultsDAO _screenResultsDao;
  private edu.harvard.med.screensaver.db.GenericEntityDAO _genericEntityDao;

  /**
   * The ScreenResult object to be populated with data parsed from the spreadsheet.
   */
  private ScreenResult _screenResult;
  private Workbook _workbook;

  private DerivedFromParser _columnsDerivedFromParser;
  private ExcludeParser _excludeParser;
  private CellVocabularyParser<AssayReadoutType> _assayReadoutTypeParser;
  private CellVocabularyParser<PositiveIndicatorType> _positiveIndicatorTypeParser;
  private CellVocabularyParser<Boolean> _rawOrDerivedParser;
  private CellVocabularyParser<Boolean> _primaryOrFollowUpParser;
  private CellVocabularyParser<Boolean> _booleanParser;
  private CellVocabularyParser<PartitionedValue> _partitionedValueParser;
  private CellVocabularyParser<AssayWellType> _assayWellTypeParser;
  private WellNameParser _wellNameParser;

  
  private SortedMap<String,ResultValueType> _dataTableColumnLabel2RvtMap;
  /**
   * @motivation runtime detection of duplicate wells in the input stream
   */
  private Set<String> parsedWellKeys = new HashSet<String>();
  private Map<Integer,Integer> _dataHeaderIndex2DataHeaderColumn;
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
    _dataTableColumnLabel2RvtMap = new TreeMap<String,ResultValueType>();
    _columnsDerivedFromParser = new DerivedFromParser(_dataTableColumnLabel2RvtMap);
    _excludeParser = new ExcludeParser(_dataTableColumnLabel2RvtMap);
    _positiveIndicatorTypeParser = new CellVocabularyParser<PositiveIndicatorType>(activityIndicatorTypeMap, PositiveIndicatorType.BOOLEAN);
    _rawOrDerivedParser = new CellVocabularyParser<Boolean>(rawOrDerivedMap, Boolean.FALSE);
    _primaryOrFollowUpParser = new CellVocabularyParser<Boolean>(primaryOrFollowUpMap, Boolean.FALSE);
    _booleanParser = new CellVocabularyParser<Boolean>(booleanMap, Boolean.FALSE);
    _partitionedValueParser = new CellVocabularyParser<PartitionedValue>(partitionedValueMap, PartitionedValue.NONE);
    _assayWellTypeParser = new CellVocabularyParser<AssayWellType>(assayWellTypeMap, AssayWellType.EXPERIMENTAL);
    _wellNameParser = new WellNameParser();

    try {
      log.info("parsing " + _workbook.getName());
      if (screen.getScreenResult() == null) {
        _screenResult = screen.createScreenResult();
        if (!parseDataHeaders(_screenResult, _workbook)) {
          log.info("errors found in data headers, will not attempt to parse data sheets");
          return _screenResult;
        }
      }
      else {
        // incremental parsing of new data
        _screenResult = screen.getScreenResult();
      }

      initializeDataHeaders(_screenResult, _workbook);
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
   * Finds the total number of data header columns.
   * TODO: this does not account for non-contiguous blocks of empty cells
   * @param dataHeadersSheet
   * @return highest cell column index plus 1
   * @throws UnrecoverableScreenResultParseException
   */
  private int findDataHeaderColumnCount(Worksheet dataHeadersSheet) 
  {
    dataHeadersSheet.getColumns();
    int rows = dataHeadersSheet.getRows();
    
    if (rows == 0) {
      return 0;
    }
    
    int n = 0;
    for (Cell cell : dataHeadersSheet.getRow(0)) {
      if(cell.isEmpty()) break;
      n++;
    }
    return n;
  }

  private int getDataColumn(int dataHeaderColumn)
  {
    return _dataHeaderIndex2DataHeaderColumn.get(dataHeaderColumn);
  }

  /**
   * Parse the worksheet containing the ScreenResult data headers.
   * This method returns error results by add them to the {@link Workbook}. 
   * Therefore check the workbook after running for errors.
   * @param workbook
   * 
   */
  private boolean parseDataHeaders(ScreenResult screenResult, Workbook workbook)
  {
    log.info("parse data headers sheet");
    
    Worksheet dataHeadersSheet = 
      _workbook.getWorksheet(DATA_HEADERS_SHEET_NAME).forOrigin(DATA_HEADERS_FIRST_DATA_HEADER_COLUMN_INDEX, 0);
    if (dataHeadersSheet == null) {
      _workbook.addError(DATA_HEADER_SHEET_NOT_FOUND_ERROR);
      return false;
    } 
    Map<DataHeaderProperty,Row> dataHeaderPropertyRows = parseDataHeaderProperties(dataHeadersSheet);
    int dataHeaderCount = findDataHeaderColumnCount(dataHeadersSheet);
    for (int iDataHeader = 0; iDataHeader < dataHeaderCount; ++iDataHeader) {
      ResultValueType rvt = 
        screenResult.createResultValueType(dataHeaderPropertyRows.get(DataHeaderProperty.NAME).getCell(iDataHeader, true).getString());
      if (dataHeaderPropertyRows.containsKey(DataHeaderProperty.REPLICATE)) {
        rvt.setReplicateOrdinal(dataHeaderPropertyRows.get(DataHeaderProperty.REPLICATE).getCell(iDataHeader).getInteger());
      }
      if (dataHeaderPropertyRows.containsKey(DataHeaderProperty.RAW_OR_DERIVED)) {
        boolean isDerived = _rawOrDerivedParser.parse(dataHeaderPropertyRows.get(DataHeaderProperty.RAW_OR_DERIVED).getCell(iDataHeader));
        if (isDerived) {
          if (validateRequiredDataPropertyDefined(DataHeaderProperty.COLUMNS_DERIVED_FROM, DataHeaderProperty.RAW_OR_DERIVED, dataHeaderPropertyRows, iDataHeader) &&
            validateRequiredDataPropertyDefined(DataHeaderProperty.HOW_DERIVED, DataHeaderProperty.RAW_OR_DERIVED, dataHeaderPropertyRows, iDataHeader)) {
            rvt.makeDerived(dataHeaderPropertyRows.get(DataHeaderProperty.HOW_DERIVED).getCell(iDataHeader, true).getString(),
                            Sets.newHashSet(Iterables.filter(_columnsDerivedFromParser.parseList(dataHeaderPropertyRows.get(DataHeaderProperty.COLUMNS_DERIVED_FROM).getCell(iDataHeader, true)),
                                                             Predicates.notNull())));
          }
        }
      }
      if (!!!rvt.isDerived()) {
        if (dataHeaderPropertyRows.containsKey(DataHeaderProperty.ASSAY_READOUT_TYPE)) {
          rvt.setAssayReadoutType(_assayReadoutTypeParser.parse(dataHeaderPropertyRows.get(DataHeaderProperty.ASSAY_READOUT_TYPE).getCell(iDataHeader, true)));
        }
      }
      if (dataHeaderPropertyRows.containsKey(DataHeaderProperty.PRIMARY_OR_FOLLOWUP)) {
        rvt.setFollowUpData(_primaryOrFollowUpParser.parse(dataHeaderPropertyRows.get(DataHeaderProperty.PRIMARY_OR_FOLLOWUP).getCell(iDataHeader)));
      }
      if (dataHeaderPropertyRows.containsKey(DataHeaderProperty.ASSAY_PHENOTYPE)) {
        rvt.setAssayPhenotype(dataHeaderPropertyRows.get(DataHeaderProperty.ASSAY_PHENOTYPE).getCell(iDataHeader).getString());
      }
      if (dataHeaderPropertyRows.containsKey(DataHeaderProperty.DESCRIPTION)) {
        rvt.setDescription(dataHeaderPropertyRows.get(DataHeaderProperty.DESCRIPTION).getCell(iDataHeader).getString());
      }
      if (dataHeaderPropertyRows.containsKey(DataHeaderProperty.COMMENTS)) {
        rvt.setComments(dataHeaderPropertyRows.get(DataHeaderProperty.COMMENTS).getCell(iDataHeader).getString());
      }
      if (dataHeaderPropertyRows.containsKey(DataHeaderProperty.TIME_POINT)) {
        rvt.setTimePoint(dataHeaderPropertyRows.get(DataHeaderProperty.TIME_POINT).getCell(iDataHeader).getString());
      }
      if (dataHeaderPropertyRows.containsKey(DataHeaderProperty.CHANNEL)) {
        rvt.setChannel(dataHeaderPropertyRows.get(DataHeaderProperty.CHANNEL).getCell(iDataHeader).getInteger());
      }
      if (dataHeaderPropertyRows.containsKey(DataHeaderProperty.TIME_POINT_ORDINAL)) {
        rvt.setTimePointOrdinal(dataHeaderPropertyRows.get(DataHeaderProperty.TIME_POINT_ORDINAL).getCell(iDataHeader).getInteger());
      }
      if (dataHeaderPropertyRows.containsKey(DataHeaderProperty.ZDEPTH_ORDINAL)) {
        rvt.setZdepthOrdinal(dataHeaderPropertyRows.get(DataHeaderProperty.ZDEPTH_ORDINAL).getCell(iDataHeader).getInteger());
      }
      if (dataHeaderPropertyRows.containsKey(DataHeaderProperty.IS_POSITIVE_INDICATOR)) {
        Boolean isPositivesIndicator = _booleanParser.parse(dataHeaderPropertyRows.get(DataHeaderProperty.IS_POSITIVE_INDICATOR).getCell(iDataHeader));
        if (isPositivesIndicator) {
          if (validateRequiredDataPropertyDefined(DataHeaderProperty.POSITIVE_INDICATOR_TYPE, DataHeaderProperty.IS_POSITIVE_INDICATOR, dataHeaderPropertyRows, iDataHeader)) {
            rvt.makePositivesIndicator(_positiveIndicatorTypeParser.parse(dataHeaderPropertyRows.get(DataHeaderProperty.POSITIVE_INDICATOR_TYPE).getCell(iDataHeader, true)));
          }
        }
      }
      // note: we do this last so that _columnsDerivedFromParser does not allow the current column to be considered a valid "derived from" value
      _dataTableColumnLabel2RvtMap.put(dataHeaderPropertyRows.get(DataHeaderProperty.COLUMN_IN_DATA_WORKSHEET).getCell(iDataHeader, true).getAsString(), rvt);
    }
    return !!!_workbook.getHasErrors();
  }

  private boolean validateRequiredDataPropertyDefined(DataHeaderProperty requiredDataHeaderProperty,
                                                      DataHeaderProperty parentDataHeaderProperty,
                                                      Map<DataHeaderProperty,Row> dataHeaderPropertyRows,
                                                      int iDataHeader)
  {
    if (!!!dataHeaderPropertyRows.containsKey(requiredDataHeaderProperty)) {
      dataHeaderPropertyRows.get(parentDataHeaderProperty).getCell(iDataHeader).addError(requiredDataHeaderProperty + " data header property row must be defined when " + parentDataHeaderProperty + " data header property row is defined");
      return false;
    }
    return true;
  }

  private Map<DataHeaderProperty,Row> parseDataHeaderProperties(Worksheet dataHeadersSheet)
  {
    Map<DataHeaderProperty,Row> result = Maps.newHashMap();
    Iterator<Row> dataRows = dataHeadersSheet.forOrigin(1, 0).iterator(); 
    for (Row row : dataHeadersSheet.forOrigin(0, 0)) {
      if (row.isEmpty()) {
        break;
      }
      
      String dataHeaderPropertyLabel = row.getCell(0).getString().trim();
      DataHeaderProperty dataHeaderProperty = DataHeaderProperty.fromDisplayText(dataHeaderPropertyLabel);
      Row dataOnlyRow = dataRows.next();
      if (dataHeaderProperty != null) {
        result.put(dataHeaderProperty, dataOnlyRow);
      }
      else {
        row.getCell(0).addError("unknown data header property: " + dataHeaderPropertyLabel);
      }
    }
    return result;
  }

  private void initializeDataHeaders(ScreenResult screenResult,
                                     Workbook workbook)
    throws UnrecoverableScreenResultParseException
  {
    Worksheet dataHeadersSheet = workbook.getWorksheet(DATA_HEADERS_SHEET_NAME).forOrigin(DATA_HEADERS_FIRST_DATA_HEADER_COLUMN_INDEX, 0);
    if (dataHeadersSheet == null) { 
      throw new UnrecoverableScreenResultParseException(DATA_HEADER_SHEET_NOT_FOUND_ERROR);
    }

    _dataHeaderIndex2DataHeaderColumn = Maps.newHashMap();

    int dataHeaderCount = findDataHeaderColumnCount(dataHeadersSheet);
    Map<DataHeaderProperty,Row> dataHeaderProperties = parseDataHeaderProperties(dataHeadersSheet);
    for (int iDataHeader = 0; iDataHeader < dataHeaderCount; ++iDataHeader) {
      Cell cell = dataHeaderProperties.get(DataHeaderProperty.COLUMN_IN_DATA_WORKSHEET).getCell(iDataHeader, true);
      String forColumnInRawDataWorksheet = cell.getString().trim();
      try {
        if (forColumnInRawDataWorksheet != null) {
          _dataHeaderIndex2DataHeaderColumn.put(iDataHeader,
                                                AlphabeticCounter.toIndex(forColumnInRawDataWorksheet));
          ResultValueType rvt = screenResult.getResultValueTypesList().get(iDataHeader);
          _dataTableColumnLabel2RvtMap.put(forColumnInRawDataWorksheet, rvt);
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
    int firstDataSheetIndex = workbook.getWorksheet(DATA_HEADERS_SHEET_NAME).getSheetIndex() + 1;
    int totalDataSheets = Math.max(0, totalSheets - firstDataSheetIndex);
    plateNumberRange = plateNumberRange == null ? new IntRange(Integer.MIN_VALUE, Integer.MAX_VALUE) : plateNumberRange;

    // Note: we do this to make sure that the RVT's are persisted before we persist and clear the RV's
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
      Worksheet worksheet = workbook.getWorksheet(iSheet).forOrigin(0, RAWDATA_FIRST_DATA_ROW_INDEX);

      int iDataHeader = 0;
      if (iSheet == firstDataSheetIndex && plateNumberRange.containsInteger(iSheet)) {
        for (ResultValueType rvt : screenResult.getResultValueTypes()) {
          determineNumericalnessOfDataHeader(rvt, workbook, iSheet, iDataHeader++);
        }
      }
      for (Row row : worksheet) {
        // bring in the old findNextRow() logic
        if (row.getColumns() > 0 
          && !row.getCell(0).isEmpty()            
          && row.getCell(0).getAsString().trim().length() > 0 ) 
        {
          Integer plateNumber = row.getCell(DataColumn.PLATE.ordinal(), true).getInteger();
          Cell wellNameCell = row.getCell(DataColumn.WELL_NAME.ordinal()); 
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
    for (ResultValueType rvt : screenResult.getResultValueTypes()) {
      _genericEntityDao.saveOrUpdateEntity(rvt); 
      //TODO: this should not be required, but the writes to the DB were missing some of the RV's and this fixed it -sde4
      for(ResultValue rv : rvt.getResultValues())
      {
        _genericEntityDao.saveOrUpdateEntity(rv);
      }
    }
    if(incrementalFlush)
    {
      for (ResultValueType rvt : screenResult.getResultValueTypes()) {
        rvt.clearResultValues();
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
    AssayWellType assayWellType = 
      _assayWellTypeParser.parse(row.getCell(DataColumn.ASSAY_WELL_TYPE.ordinal()));
    try {
      AssayWell assayWell = findOrCreateAssayWell(well, assayWellType);
      //TODO: temporarily suppressed the DataModelViolations until we decide if AssayWellType MUST match LibraryWellType
      
      List<ResultValueType> wellExcludes = _excludeParser.parseList(row.getCell(DataColumn.EXCLUDE.ordinal()));
      int iDataHeader = 0;
      for (ResultValueType rvt : screenResult.getResultValueTypes()) 
      {
        Cell cell = row.getCell(getDataColumn(iDataHeader));
        boolean isExclude = (wellExcludes != null && wellExcludes.contains(rvt));
        try {
          ResultValue newResultValue = null;
          if (rvt.isPositiveIndicator()) {
            String value;
            if (rvt.getPositiveIndicatorType() == PositiveIndicatorType.BOOLEAN) {
              if (cell.isBoolean()) {
                value = cell.getBoolean().toString();
              }
              else {
                value = _booleanParser.parse(cell).toString();
              }
              newResultValue =
                rvt.createResultValue(assayWell,
                                      value,
                                      isExclude);
            }
            else if (rvt.getPositiveIndicatorType() == PositiveIndicatorType.PARTITION) {
              newResultValue =
                rvt.createResultValue(assayWell,
                                      _partitionedValueParser.parse(cell).toString(),
                                      isExclude);
            }
            else {
              throw new DevelopmentException("unhandled PositiveIndicatorType: " + rvt.getPositiveIndicatorType());
            }
          }
          else { // not assay activity indicator
            if (rvt.isNumeric()) {
              newResultValue =
                rvt.createResultValue(assayWell,
                                      cell.getDouble(),
                                      cell.getDoublePrecision(),
                                      isExclude);
            }
            else {
              newResultValue =
                rvt.createResultValue(assayWell,
                                      cell.getString(),
                                      isExclude);
            }
          }
          if (newResultValue == null) {
            cell.addError("duplicate well");
          }
        }
        catch (ResultValueTypeNumericalnessException e) {
          // inconsistency in numeric or string types in RVT's result values
          cell.addError(e.getMessage());
        }
        ++iDataHeader;
      } // RVT's
  
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
      row.getCell(DataColumn.ASSAY_WELL_TYPE.ordinal()).addError(e.getMessage());
    }
  }

    
  /**
   * Determines if a data header contains numeric or non-numeric data, by
   * reading ahead and making the determination based upon the first non-empty
   * cell in the column for the specified data header. Note that if all cells
   * for the column are empty, a non-numeric data header will be assumed
   * (further worksheets of data are not considered).
   */
  private void determineNumericalnessOfDataHeader(ResultValueType rvt,
                                                  Workbook workbook,
                                                  int iSheet,
                                                  int iDataHeader)
  {
    assert rvt.getResultValues().size() == 0 : "should not be attempting to set RVT numeric flag if it already has result values";
    for(Row row: workbook.getWorksheet(iSheet).forOrigin(RAWDATA_FIRST_DATA_HEADER_COLUMN_INDEX, RAWDATA_FIRST_DATA_ROW_INDEX))
    {
      Cell cell = row.getCell(iDataHeader);
      if (!cell.isEmpty() && cell.getAsString().trim().length() > 0) {
        rvt.setNumeric(cell.isNumeric());
        return;
      }
    }
    log.warn("all cells for data header " + rvt + " are empty on the first data worksheet; assuming data header is non-numeric");
  }
  
  private AssayWell findOrCreateAssayWell(Well well, AssayWellType assayWellType)
    throws DataModelViolationException
  {
    // TODO: assay well should not already exist, unless we start supporting the appending of new data headers to existing assay well data  
//    AssayWell assayWell = _screenResultsDao.findAssayWell(_screenResult, wellKey);
//    if (assayWell != null) {
//      if (assayWell.getAssayWellType() != assayWellType) {
//        _workbook.addError(ASSAY_WELL_TYPE_INCONSISTENCY + ": " + wellKey);
//        return null;
//      }
//      return assayWell;
//    }
    return _screenResult.createAssayWell(well, assayWellType);
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

  public class ColumnLabelsParser implements CellValueParser<ResultValueType>
  {
    protected Map<String,ResultValueType> _columnLabel2RvtMap;
    private Pattern columnIdPattern = Pattern.compile("[A-Z]+");

    public ColumnLabelsParser(Map<String,ResultValueType> columnLabel2RvtMap)
    {
      _columnLabel2RvtMap =  columnLabel2RvtMap;
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
          cell.addError("invalid Data Header column reference '" + text +
            "' (expected one of " + _columnLabel2RvtMap.keySet() + ")");
        }
      }
      return result;
    }

    protected ResultValueType doParseSingleValue(String value, Cell cell)
    {
      Matcher matcher = columnIdPattern.matcher(value);
      if (!matcher.matches()) {
        return null;
      }
      String columnLabel = matcher.group(0);
      return _columnLabel2RvtMap.get(columnLabel);
    }
  }

  private class DerivedFromParser extends ColumnLabelsParser
  {

    public DerivedFromParser(Map<String,ResultValueType> columnLabel2RvtMap)
    {
      super(columnLabel2RvtMap);
    }

    @Override
    public List<ResultValueType> parseList(Cell cell)
    {
      if (DERIVED_FROM_MISSING.equalsIgnoreCase(cell.getString().trim())) {
        return Collections.emptyList();
      }
      return super.parseList(cell);
    }
  }

  private class ExcludeParser extends ColumnLabelsParser
  {
    public ExcludeParser(Map<String,ResultValueType> columnLabel2RvtMap)
    {
      super(columnLabel2RvtMap);
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

}
