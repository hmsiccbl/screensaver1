// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.io.CellReader.Factory;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screenresults.ActivityIndicatorType;
import edu.harvard.med.screensaver.model.screenresults.IndicatorDirection;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Parses data from Excel spreadsheet files necessary for instantiating a
 * {@link edu.harvard.med.screensaver.model.screenresults.ScreenResult}. Two
 * files are parsed to obtain the required data: the first contains the
 * raw/derived data (which populates
 * {@link edu.harvard.med.screensaver.model.screenresults.ResultValue} objects,
 * while the second contains the "data header" metadata (which populates
 * {@link edu.harvard.med.screensaver.model.screenresults.ResultValueType}
 * objects).
 * <p>
 * The class attempts to parse files as fully as possible, carrying on in the
 * face of errors, in order to catch as many errors as possible, as this will
 * aid the manual effort of correcting the files' format and content between
 * import attempts. Validation checks performed include:
 * <ul>
 * <li> Data header count in data file matches data header definitions in
 * metadata file. (TODO)
 * <li> File name listed in cell "Sheet1:A2" of metadata file must match data
 * file name. (TODO)
 * <li> Data Header names, as defined in the metadata, match the actual Data
 * Header names in the data file. (TODO)
 * </ul>
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class ScreenResultParser
{
  
  // static data members
  
  private static final Logger log = Logger.getLogger(ScreenResultParser.class);
  
  private static final String FIRST_DATE_SCREENED = "First Date Screened";
  private static final String METADATA_META_SHEET_NAME = "meta";
  private static final String NO_METADATA_META_SHEET_ERROR = "worksheet could not be found";
  private static final String NO_CREATED_DATE_FOUND_ERROR = "\"First Date Screened\" value not found";
  private static final String UNEXPECTED_DATA_HEADER_TYPE_ERROR = "unexpected data header type";
  private static final String REFERENCED_UNDEFINED_DATA_HEADER_ERROR = "referenced undefined data header";
  private static final String UNRECOGNIZED_INDICATOR_DIRECTION_ERROR = "unrecognized \"indicator direction\" value";
  private static final String METADATA_DATA_HEADER_COLUMNS_NOT_FOUND_ERROR = "data header columns not found in metadata workbook";
  private static final String METADATA_UNEXPECTED_COLUMN_TYPE_ERROR = "expected column type of \"data\"";
  private static final String METADATA_NO_RAWDATA_FILES_SPECIFIED_ERROR = "raw data workbook files not specified";
  private static final String UNKNOWN_ERROR = "unknown error";

  private static final short METADATA_FILENAMES_CELL_COLUMN_INDEX = 1;
  private static final int   METADATA_FILENAMES_CELL_ROW_INDEX = 0;
  
  private static final int METADATA_FIRST_DATA_ROW_INDEX = 1;
  private static final int RAWDATA_FIRST_DATA_ROW_INDEX = 1;
  
  /**
   * The data rows of the metadata worksheet. Order of enum values is
   * significant, as we use the ordinal() method.
   */
  private enum MetadataRow { 
    COLUMN_IN_TEMPLATE,
    COLUMN_TYPE,
    NAME,
    DESCRIPTION,
    REPLICATE,
    TIME_POINT,
    RAW_OR_DERIVED,
    HOW_DERIVED,
    COLUMNS_DERIVED_FROM,
    IS_ASSAY_ACTIVITY_INDICATOR,
    ACTIVITY_INDICATOR_TYPE,
    INDICATOR_DIRECTION,
    INDICATOR_CUTOFF,
    PRIMARY_OR_FOLLOWUP,
    ASSAY_PHENOTYPE,
    IS_CHERRY_PICK,
    COMMENTS 
  };
  private static final int METADATA_ROW_COUNT = MetadataRow.values().length;
  public static final String FILENAMES_LIST_DELIMITER = "\\s*,\\s*";

  /**
   * The standard columns of a data worksheet. Order of enum values is
   * significant, as we use the ordinal() method.
   */
  private enum DataColumn {
    STOCK_PLATE_ID,
    WELL_NAME,
    TYPE,
    EXCLUDE,
    FIRST_DATA_HEADER
  };

  private static Map<String,IndicatorDirection> indicatorDirectionMap = new HashMap<String,IndicatorDirection>();
  private static Map<String,ActivityIndicatorType> activityIndicatorTypeMap = new HashMap<String,ActivityIndicatorType>();
  private static Map<String,Boolean> rawOrDerivedMap = new HashMap<String,Boolean>();
  private static Map<String,Boolean> primaryOrFollowUpMap = new HashMap<String,Boolean>();
  private static Map<String,Boolean> booleanMap = new HashMap<String,Boolean>();
  static {
    indicatorDirectionMap.put("<",IndicatorDirection.LOW_VALUES_INDICATE);
    indicatorDirectionMap.put(">",IndicatorDirection.HIGH_VALUES_INDICATE);
    
    activityIndicatorTypeMap.put("High values",ActivityIndicatorType.NUMERICAL);
    activityIndicatorTypeMap.put("A",ActivityIndicatorType.NUMERICAL);
    activityIndicatorTypeMap.put("Low values",ActivityIndicatorType.NUMERICAL);
    activityIndicatorTypeMap.put("B",ActivityIndicatorType.NUMERICAL);
    activityIndicatorTypeMap.put("Boolean",ActivityIndicatorType.BOOLEAN);
    activityIndicatorTypeMap.put("C",ActivityIndicatorType.BOOLEAN);
    activityIndicatorTypeMap.put("Scaled",ActivityIndicatorType.SCALED);
    activityIndicatorTypeMap.put("D",ActivityIndicatorType.SCALED);
    
    rawOrDerivedMap.put("", false);
    rawOrDerivedMap.put("raw", false);
    rawOrDerivedMap.put("derived", true);

    primaryOrFollowUpMap.put("", false);
    primaryOrFollowUpMap.put("Primary", false);
    primaryOrFollowUpMap.put("Follow Up", true);
    
    booleanMap.put("", false);
    booleanMap.put("false", false);
    booleanMap.put("no", false);
    booleanMap.put("n", false);
    booleanMap.put("0", false);
    booleanMap.put("true", true);
    booleanMap.put("yes", true);
    booleanMap.put("y", true);
    booleanMap.put("1", true);
  }
  

  // static methods

  public static void main(String[] args)
  {
    Options options = new Options();
    options.addOption(new Option("metadatafile",
                                 true,
                                 "the file location of the Excel spreadsheet holding the Screen Result metadata"));
    Option wellsToPrintOption = new Option("wellstoprint",
                                           true,
                                           "the number of wells to print out");
    wellsToPrintOption.setOptionalArg(true);
    options.addOption(wellsToPrintOption);
    try {
      CommandLine cmdLine = new GnuParser().parse(options, args);

      ClassPathXmlApplicationContext appCtx = new ClassPathXmlApplicationContext(new String[] { "spring-context-services.xml", "spring-context-screenresultparser-test.xml" });
      ScreenResultParser screenResultParser = (ScreenResultParser) appCtx.getBean("screenResultParser");
      ScreenResult screenResult = screenResultParser.parse(new File(cmdLine.getOptionValue("metadatafile")));
      if (screenResultParser.getErrors().size() > 0) {
        System.err.println("Errors encountered during parse:");
        for (String error : screenResultParser.getErrors()) {
          System.err.println(error);
        }
      }
      if (cmdLine.hasOption("wellstoprint")) {
        new ScreenResultPrinter(screenResult).print(new Integer(cmdLine.getOptionValue("wellstoprint")));
      }
      else {
        new ScreenResultPrinter(screenResult).print();
      }
    }
    catch (ParseException e) {
      System.err.println("error parsing command line options: " + e.getMessage());
    }
  }

  
  // instance data members
  
  private DAO _dao;
  /**
   * The ScreenResult object to be populated with data parsed from the spreadsheet.
   */
  private ScreenResult _screenResult;
  private HSSFWorkbook _metadataWorkbook;
  
  private Map<String,ResultValueType> _columnsDerivedFromMap = new HashMap<String,ResultValueType>();
  private CellVocabularyParser<ResultValueType> _columnsDerivedFromParser = 
    new CellVocabularyParser<ResultValueType>(_columnsDerivedFromMap);
  private CellVocabularyParser<IndicatorDirection> indicatorDirectionParser = 
    new CellVocabularyParser<IndicatorDirection>(indicatorDirectionMap);
  private CellVocabularyParser<ActivityIndicatorType> _activityIndicatorTypeParser = 
    new CellVocabularyParser<ActivityIndicatorType>(activityIndicatorTypeMap, ActivityIndicatorType.NUMERICAL);
  private CellVocabularyParser<Boolean> _rawOrDerivedParser = 
    new CellVocabularyParser<Boolean>(rawOrDerivedMap, Boolean.FALSE);
  private CellVocabularyParser<Boolean> _primaryOrFollowUpParser = 
    new CellVocabularyParser<Boolean>(primaryOrFollowUpMap, Boolean.FALSE);
  private CellVocabularyParser<Boolean> _booleanParser = 
    new CellVocabularyParser<Boolean>(booleanMap, Boolean.FALSE);
  private PlateNumberParser _plateNumberParser = new PlateNumberParser();
  private WellNameParser _wellNameParser = new WellNameParser();
  private ParseErrorManager _errors = new ParseErrorManager();
  private Factory _metadataCellParserFactory;
  private Factory _dataCellParserFactory;
  private Short _metadataFirstDataHeaderColumnIndex;
  private int _nDataHeaders;
  private Map<Integer,Short> _dataHeaderIndex2DataHeaderColumn = new HashMap<Integer,Short>();
  private List<String> _rawDataWorkbookFiles = new ArrayList<String>();
  
  
  // public methods and constructors

  public ScreenResultParser(DAO dao) 
  {
    _dao = dao;
  }

  /**
   * Parse the worksheets specified at instantiation time and uses the parsed
   * data to populate a {@link ScreenResult} object (non-persisted). If parsing
   * errors are encountered, they will be available via {@link #getErrors()}.
   * The returned <code>ScreenResult</code> may only be partially populated if
   * errors are encountered, so always call getErrors() to determine parsing
   * success.
   * 
   * @see #getErrors()
   * @return a {@link ScreenResult} object containing the data parsed from the
   *         worksheet.
   */
  public ScreenResult parse(File metadataExcelFile)
  {
    InputStream metadataInputStream = null;
    try {
      metadataInputStream = new FileInputStream(metadataExcelFile);
      if (metadataInputStream == null) {
        throw new UnrecoverableScreenResultParseException("metadata workbook file " + 
                                                          metadataExcelFile + " cannot be read");
      }
      POIFSFileSystem metadataFs = new POIFSFileSystem(new BufferedInputStream(metadataInputStream));
      _metadataWorkbook = new HSSFWorkbook(metadataFs);
      parseMetadata();
      for (String filename : _rawDataWorkbookFiles) {
        InputStream inputStream = null;
        try {
          File rawDataFile = new File(metadataExcelFile.getParent(),
                                      filename);
          log.info("parsing " + rawDataFile.getAbsolutePath());
          inputStream = new FileInputStream(rawDataFile);
          if (inputStream == null) {
            _errors.addError("raw data workbook file " + filename + " cannot be read and will be ignored");
          }
          else {
            POIFSFileSystem dataFs = new POIFSFileSystem(new BufferedInputStream(inputStream));
            parseData(new HSSFWorkbook(dataFs));
          }
        }
        catch (IOException e) {
          IOUtils.closeQuietly(metadataInputStream);
          throw e;
        }
      }
    }
    catch (UnrecoverableScreenResultParseException e) {
      _errors.addError("serious parse error encountered (could not continue further parsing): " + e.getMessage());
    }
    catch (Exception e) {
      // TODO: log this
      e.printStackTrace();
      _errors.addError(UNKNOWN_ERROR + ": " + e.getMessage());
    }
    finally {
      IOUtils.closeQuietly(metadataInputStream);
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
   * @return a
   *         <code>List&lt;String&gt;</code of all errors generated during parsing
   */
  public List<String> getErrors()
  {
    return _errors.getErrors();
  }
  

  // private methods

  /**
   * Initialize the metadata worksheet and related data members.
   * 
   * @throws UnrecoverableScreenResultParseException if metadata worksheet could not be
   *           initialized or does not appear to be a valid metadata definition
   */
  private void initializeMetadataSheet() throws UnrecoverableScreenResultParseException
  {
    // TODO: find the sheet in a more reliable, flexible fashion
    HSSFSheet metadataSheet = _metadataWorkbook.getSheetAt(0);

    // TODO: take action if verification fails
    verifyStandardMetadataFormat(metadataSheet);

    _metadataCellParserFactory = new CellReader.Factory(_metadataWorkbook.getSheetName(0),
                                                        metadataSheet,
                                                        _errors);

    initializeFirstDataHeaderColumnIndex(metadataSheet);
  }

  private void initializeFirstDataHeaderColumnIndex(HSSFSheet metadataSheet) throws UnrecoverableScreenResultParseException
  {
    int row = MetadataRow.COLUMN_TYPE.ordinal() + METADATA_FIRST_DATA_ROW_INDEX;
    int metadataLastDataHeaderColumnIndex = metadataSheet.getRow(row).getLastCellNum();
    for (short iCol = 0; iCol < metadataLastDataHeaderColumnIndex; ++iCol) {
      CellReader cell = _metadataCellParserFactory.newCellReader(iCol, row);
      String dataType = cell.getString();
      if (dataType != null && dataType.equalsIgnoreCase("data")) {
        _metadataFirstDataHeaderColumnIndex = new Short(iCol);
        break;
      }
    }
    if (_metadataFirstDataHeaderColumnIndex == null) {
      throw new UnrecoverableScreenResultParseException(METADATA_DATA_HEADER_COLUMNS_NOT_FOUND_ERROR);
    }

    for (short iCol = _metadataFirstDataHeaderColumnIndex; iCol <= metadataLastDataHeaderColumnIndex; ++iCol) {
      CellReader cell = _metadataCellParserFactory.newCellReader(iCol, row);
      String dataType = cell.getString();
      if (dataType != null && !dataType.equalsIgnoreCase("data")) {
        _errors.addError(METADATA_UNEXPECTED_COLUMN_TYPE_ERROR, cell);
        metadataLastDataHeaderColumnIndex = (short) (iCol - 1);
        break;
      }
      if (dataType == null || dataType.trim().equals("")) {
        // okay if column is blank
        // TODO: verify *entire* column in blank, otherwise report error
        metadataLastDataHeaderColumnIndex = (short) (iCol - 1);
        break;
      }
    }
    _nDataHeaders = (metadataLastDataHeaderColumnIndex - _metadataFirstDataHeaderColumnIndex) + 1;
  }
  
  private HSSFSheet initializeDataSheet(HSSFWorkbook workbook, int index)
  {
    // TODO: find the sheet in a more reliable, flexible fashion
    HSSFSheet dataSheet = workbook.getSheetAt(index);

    // TODO: take action if verification fails
    //verifyStandardDataFormat(metadataSheet);

    _dataCellParserFactory = new CellReader.Factory(workbook.getSheetName(0),
                                                    dataSheet,
                                                    _errors);  
    return dataSheet;
  }


  private CellReader metadataCell(MetadataRow row, int dataHeader, boolean isRequired) 
  {
    return _metadataCellParserFactory.newCellReader((short) (_metadataFirstDataHeaderColumnIndex + dataHeader),
                                                    (METADATA_FIRST_DATA_ROW_INDEX + row.ordinal()),
                                                    isRequired);
  }
  
  private CellReader metadataCell(MetadataRow row, int dataHeader)
  {
    return metadataCell(row, dataHeader, /*required=*/false);
  }
  
  private CellReader dataCell(int row, DataColumn column)
  {
    return _dataCellParserFactory.newCellReader((short) column.ordinal(), row);
  }

  private CellReader dataCell(int row, int iDataHeader)
  {
    return _dataCellParserFactory.newCellReader((short) (_dataHeaderIndex2DataHeaderColumn.get(iDataHeader)),
                                                row);
  }
                                        
  
  /**
   * Parse the workbook containing the ScreenResult metadata
   * @throws UnrecoverableScreenResultParseException 
   */
  private void parseMetadata() throws UnrecoverableScreenResultParseException 
  {
    initializeMetadataSheet();
    parseRawDataWorkbookFilenames();
    parseMetaMetadata();
    for (int iDataHeader = 0; iDataHeader < _nDataHeaders; ++iDataHeader) {
      recordDataHeaderColumn(iDataHeader);
      ResultValueType rvt = 
        new ResultValueType(_screenResult,
                            metadataCell(MetadataRow.NAME, iDataHeader, true).getString(),
                            metadataCell(MetadataRow.REPLICATE, iDataHeader).getInteger(),
                            _rawOrDerivedParser.parse(metadataCell(MetadataRow.RAW_OR_DERIVED, iDataHeader)),
                            _booleanParser.parse(metadataCell(MetadataRow.IS_ASSAY_ACTIVITY_INDICATOR, iDataHeader)),
                            _primaryOrFollowUpParser.parse(metadataCell(MetadataRow.PRIMARY_OR_FOLLOWUP, iDataHeader)),
                            metadataCell(MetadataRow.ASSAY_PHENOTYPE, iDataHeader).getString(),
                            _booleanParser.parse(metadataCell(MetadataRow.IS_CHERRY_PICK, iDataHeader)));
      _columnsDerivedFromMap.put(metadataCell(MetadataRow.COLUMN_IN_TEMPLATE, iDataHeader, true).getString(), rvt);
      rvt.setDescription(metadataCell(MetadataRow.DESCRIPTION, iDataHeader).getString());
      rvt.setTimePoint(metadataCell(MetadataRow.TIME_POINT, iDataHeader).getString());
      if (rvt.isDerived()) {
        rvt.setDerivedFrom(new TreeSet<ResultValueType>(_columnsDerivedFromParser.parseList(metadataCell(MetadataRow.COLUMNS_DERIVED_FROM, iDataHeader, true))));
        rvt.setHowDerived(metadataCell(MetadataRow.HOW_DERIVED, iDataHeader, true).getString());
        // TODO: should warn if these values *are* defined and !isDerivedFrom()
      }
      if (rvt.isActivityIndicator()) {
        rvt.setActivityIndicatorType(_activityIndicatorTypeParser.parse(metadataCell(MetadataRow.ACTIVITY_INDICATOR_TYPE, iDataHeader, true)));
        if (rvt.getActivityIndicatorType().equals(ActivityIndicatorType.NUMERICAL)) {
          rvt.setIndicatorDirection(indicatorDirectionParser.parse(metadataCell(MetadataRow.INDICATOR_DIRECTION, iDataHeader, true)));
          rvt.setIndicatorCutoff(metadataCell(MetadataRow.INDICATOR_CUTOFF, iDataHeader, true).getDouble());
        }
        // TODO: should warn if these values *are* defined and !isActivityIndicator()
      }
      rvt.setComments(metadataCell(MetadataRow.COMMENTS, iDataHeader).getString());
    }
  }

  private void parseRawDataWorkbookFilenames() throws UnrecoverableScreenResultParseException
  {
    CellReader cell = _metadataCellParserFactory.newCellReader(METADATA_FILENAMES_CELL_COLUMN_INDEX,
                                                               METADATA_FILENAMES_CELL_ROW_INDEX,
                                                               true);
    String fileNames = cell.getString();
    if (fileNames.equals("")) {
      throw new UnrecoverableScreenResultParseException(METADATA_NO_RAWDATA_FILES_SPECIFIED_ERROR, cell);
    }
    String[] fileNamesArray = fileNames.split(FILENAMES_LIST_DELIMITER);
    for (int i = 0; i < fileNamesArray.length; i++) {
      _rawDataWorkbookFiles.add(fileNamesArray[i]);
    }  
  }

  private void recordDataHeaderColumn(int iDataHeader)
  {
    String forColumnInRawDataWorksheet = metadataCell(MetadataRow.COLUMN_IN_TEMPLATE, iDataHeader, true).getString();
    if (forColumnInRawDataWorksheet != null) {
      _dataHeaderIndex2DataHeaderColumn.put(iDataHeader,
                                            new Short((short) (forColumnInRawDataWorksheet.charAt(0) - 'A')));
    }
  }

  /**
   * Parse the workbook containing the ScreenResult data.
   * 
   * @param workbook the workbook containing some or all of the raw data for a
   *          ScreenResult
   * @throws ExtantLibraryException if an existing Well entity cannot be found
   *           in the database
   */
  private void parseData(HSSFWorkbook workbook) throws ExtantLibraryException
  {
    for (int iSheet = 0; iSheet < workbook.getNumberOfSheets(); ++iSheet) {
      HSSFSheet sheet = initializeDataSheet(workbook, iSheet);
      for (int iRow = RAWDATA_FIRST_DATA_ROW_INDEX; iRow <= sheet.getLastRowNum(); ++iRow) {
        Well well = findWell(iRow);
        dataCell(iRow, DataColumn.TYPE).getString(); // TODO: use this value?
        boolean excludeWell = _booleanParser.parse(dataCell(iRow,
                                                            DataColumn.EXCLUDE));
        int iDataHeader = 0;
        for (ResultValueType rvt : _screenResult.getResultValueTypes()) {
          CellReader cell = dataCell(iRow, iDataHeader);
          Object value = !rvt.isActivityIndicator()
              ? cell.getDouble()
              : rvt.getActivityIndicatorType() == ActivityIndicatorType.BOOLEAN
                ? _booleanParser.parse(cell)
                : rvt.getActivityIndicatorType() == ActivityIndicatorType.NUMERICAL
                  ? cell.getDouble()
                  : rvt.getActivityIndicatorType() == ActivityIndicatorType.SCALED
                    ? cell.getString() : cell.getString();
          if (value == null) {
            value = "";
          }
          new ResultValue(rvt, well, value.toString(), excludeWell);
          ++iDataHeader;
        }
      }
    }
  }

  // TODO: this needs to be moved to our DAO; probably as a
  // findEntityByBusinessKey()
  private Well findWell(int iRow) throws ExtantLibraryException
  {
    Map<String,Object> businessKey = new HashMap<String,Object>();
    businessKey.put("plateNumber",
                    _plateNumberParser.parse(dataCell(iRow,
                                                      DataColumn.STOCK_PLATE_ID)));
    businessKey.put("wellName",
                    _wellNameParser.parse(dataCell(iRow, DataColumn.WELL_NAME)));
    Well well = _dao.findEntityByProperties(Well.class, businessKey);
    if (well == null) {
      throw new ExtantLibraryException("well entity has not been loaded for plate " + 
                                       businessKey.get("plateNumber") + " and well " + 
                                       businessKey.get("wellName"));
    }
    return well;
  }

  /**
   * Returns <code>true</code> iff the specified worksheet "looks like" it
   * contains a valid metadata structure. It could be wrong.
   * 
   * @param sheet the sheet to verify
   * @return <code>true</code> iff the specified worksheet "looks like" it
   *         contains a valid metadata structure
   */
  private boolean verifyStandardMetadataFormat(HSSFSheet sheet) {
    // TODO: implement
    return true;
  }

  /**
   * Find a worksheet with the given name, case-insensitively. If not found,
   * adds an error and returns <code>null</code>.
   * 
   * @motivation the HSSFWorkbook API is case sensitive for worksheet names
   * @param name the worksheet name
   * @return the first HSSFSheet to match the specified name
   *         (case-insensitively)
   */
  private HSSFSheet findSheet(String targetName)
  {
    for (int i = 0; i < _metadataWorkbook.getNumberOfSheets(); ++i) {
      String name = _metadataWorkbook.getSheetName(i);
      if (targetName.equalsIgnoreCase(name)) {
        return _metadataWorkbook.getSheetAt(i);
      }
    }
    _errors.addError(NO_METADATA_META_SHEET_ERROR + " \"" + targetName + "\"");
    return null;
  }

  /**
   * Parses the "meta" worksheet. Yes, we actually have a tab called "meta" in a
   * workbook file that itself contains metadata for our ScreenResult metadata;
   * thus the double-meta prefix.  Life is complex.
   */
  private void parseMetaMetadata() {
    HSSFSheet metametaSheet = findSheet(METADATA_META_SHEET_NAME);
    if (metametaSheet != null) {
      Iterator iterator = metametaSheet.rowIterator();
      while (iterator.hasNext()) {
        HSSFRow row = (HSSFRow) iterator.next();
        HSSFCell nameCell = row.getCell(row.getFirstCellNum());
        if (nameCell.getStringCellValue().equalsIgnoreCase(FIRST_DATE_SCREENED)) {
          HSSFCell valueCell = row.getCell((short)(row.getFirstCellNum() + 1));
          _screenResult = new ScreenResult(valueCell.getDateCellValue());
          return;
        }
      }
      _errors.addError(NO_CREATED_DATE_FOUND_ERROR);
    }
    _screenResult = new ScreenResult(new Date());
  }
  
  /**
   * Interface for the various cell parsers used by the
   * {@link ScreenResultParser} class. Cell parsers read the value from a cell
   * and convert to a more strictly typed object, for use within our entity
   * model. (This interface is probably a case of over-engineering.)
   * 
   * @author ant
   */
  public interface CellValueParser<T>
  {
    /**
     * Parse the value in a cell, returning <T>
     * @param cell the cell to be parsed
     * @return a <T>, representing the value of the cell
     */
    T parse(CellReader cell);
    
    List<T> parseList(CellReader cell);
  }

  /**
   * Parses the value of a cell, mapping a text value to an internal, system
   * object representation. Handles single-valued cells as well as cells that
   * contain lists of values. Note that this class is a non-static inner class
   * and references instance methods of {@link ScreenResultParser}.
   */
  public class CellVocabularyParser<T> implements CellValueParser<T>
  {
    // TODO: class methods needs javadocs
    
    // static data members
    
    private static final String DEFAULT_ERROR_MESSAGE = "unparseable value";
    private static final String DEFAULT_DELIMITER_REGEX = ",";

    
    // instance data members
    
    private Map<String,T> _parsedValue2SystemValue;
    private T _valueToReturnIfUnparseable = null;
    private String _delimiterRegex = ",";
    private String _errorMessage;
    
    
    // constructors

    public CellVocabularyParser(Map<String,T> parsedValue2SystemValue) 
    {
      this(parsedValue2SystemValue,
           null,
           DEFAULT_ERROR_MESSAGE,
           DEFAULT_DELIMITER_REGEX);
    }
                           
    public CellVocabularyParser(Map<String,T> parsedValue2SystemValue,
                                T valueToReturnIfUnparseable)
    {
      this(parsedValue2SystemValue,
           valueToReturnIfUnparseable,
           DEFAULT_ERROR_MESSAGE,
           DEFAULT_DELIMITER_REGEX);
    }
    
    public CellVocabularyParser(Map<String,T> parsedValue2SystemValue,
                                T valueToReturnIfUnparseable,
                                String errorMessage)
    {
      this(parsedValue2SystemValue,
           valueToReturnIfUnparseable,
           errorMessage,
           DEFAULT_DELIMITER_REGEX);
    }
    
    public CellVocabularyParser(Map<String,T> parsedValue2SystemValue,
                                T valueToReturnIfUnparseable,
                                String errorMessage,
                                String delimiterRegex)
    {
      _parsedValue2SystemValue = parsedValue2SystemValue;
      _valueToReturnIfUnparseable = valueToReturnIfUnparseable;
      _errorMessage = errorMessage;
      _delimiterRegex = delimiterRegex;
    }
    
    
    // public methods

    public T parse(CellReader cell) 
    {
      return doParse(cell.getString(), cell);
    }

    public List<T> parseList(CellReader cell) {
      List<T> result = new ArrayList<T>();
      String textMultiValue = cell.getString();
      if (textMultiValue == null) {
        return result;
      }
      String[] textValues = textMultiValue.split(_delimiterRegex);
      for (int i = 0; i < textValues.length; i++) {
        String text = textValues[i];
        result.add(doParse(text, cell));
      }
      return result;
    }
    

    // private methods

    private T doParse(String text, CellReader cell)
    {
      if (text == null) {
        return _valueToReturnIfUnparseable;
      }
      text = text.toLowerCase().trim();
      for (Iterator iter = _parsedValue2SystemValue.keySet()
                                                   .iterator(); iter.hasNext();) {
        String pattern = (String) iter.next();
        if (pattern.equalsIgnoreCase(text)) {
          return _parsedValue2SystemValue.get(pattern);
        }
      }
      _errors.addError(_errorMessage + " \"" + text + "\" (expected one of " + _parsedValue2SystemValue.keySet() + ")", cell);
      return _valueToReturnIfUnparseable;
    }

  }
  
  /**
   * Parses the value of a cell containing a "plate number". Converts from a
   * "PL-####" format to an <code>Integer</code>.
   * 
   * @author ant
   */
  public class PlateNumberParser implements CellValueParser<Integer>
  {
    
    // instance data members
    
    private Pattern plateNumberPattern = Pattern.compile("PL-(\\d+)");

    
    // public methods
    
    public Integer parse(CellReader cell) 
    {
      Matcher matcher = plateNumberPattern.matcher(cell.getString());
      if (!matcher.matches()) {
        _errors.addError("unparseable plate number '" + cell.getString() + "'",
                         cell);
        return -1;
      }
      return new Integer(matcher.group(1));
    }

    public List<Integer> parseList(CellReader cell)
    {
      throw new UnsupportedOperationException();
    }
  }

  /**
   * Parses the value of a cell containing a "well name". Validates that the
   * well name follows proper syntax, defined by the regex "[A-Z]\d\d".
   * 
   * @author ant
   */
  public class WellNameParser implements CellValueParser<String>
  {
    
    // instance data members
    
    private Pattern plateNumberPattern = Pattern.compile("[A-P]\\d\\d");

    
    // public methods
    
    public String parse(CellReader cell) 
    {
      Matcher matcher = plateNumberPattern.matcher(cell.getString());
      if (!matcher.matches()) {
        _errors.addError("unparseable well name '" + cell.getString() + "'",
                         cell);
        return "";
      }
      return matcher.group(0);
    }

    public List<String> parseList(CellReader cell)
    {
      throw new UnsupportedOperationException();
    }
  }


}
