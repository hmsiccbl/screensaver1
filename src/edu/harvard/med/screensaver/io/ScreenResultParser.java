// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
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
  
  private static final String FIRST_DATE_SCREENED = "First Date Screened";
  private static final String METADATA_META_SHEET_NAME = "meta";
  private static final String NO_METADATA_META_SHEET_ERROR = "\"meta\" worksheet not found";
  private static final String NO_CREATED_DATE_FOUND_ERROR = "\"First Date Screened\" value not found";
  private static final String UNEXPECTED_DATA_HEADER_TYPE_ERROR = "unexpected data header type";
  private static final String REFERENCED_UNDEFINED_DATA_HEADER_ERROR = "referenced undefined data header";
  private static final String UNRECOGNIZED_INDICATOR_DIRECTION_ERROR = "unrecognized \"indicator direction\" value";
  private static final String UNKNOWN_ERROR = "unknown error";

  private static final int METADATA_FIRST_DATA_ROW_INDEX = 1;
  private static final int METADATA_FIRST_DATA_HEADER__COLUMN_INDEX = 5;
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
    booleanMap.put("true", false);
    booleanMap.put("yes", false);
    booleanMap.put("y", false);
    booleanMap.put("1", false);
  }
  

  // static methods

  public static void main(String[] args)
  {
    Options options = new Options();
    options.addOption(new Option("datafile",
                                 true,
                                 "the file location of the Excel spreadsheet holding the Screen Result raw data"));
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
      InputStream dataFileInStream = new FileInputStream(cmdLine.getOptionValue("datafile"));
      InputStream metadataFileInStream = new FileInputStream(cmdLine.getOptionValue("metadatafile"));

      ClassPathXmlApplicationContext appCtx = new ClassPathXmlApplicationContext(new String[] { "spring-context-services.xml", "spring-context-screenresultparser-test.xml" });
      ScreenResultParser screenResultParser = (ScreenResultParser) appCtx.getBean("screenResultParser");
      ScreenResult screenResult = screenResultParser.parse(metadataFileInStream,
                                                           dataFileInStream);
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
    catch (FileNotFoundException e) {
      System.err.println("could not read input file: " + e.getMessage());
    }
  }

  
  // instance data members
  
  private DAO _dao;
  private InputStream _dataExcelFileInStream;
  private InputStream _metadataExcelFileInStream;
  /**
   * The ScreenResult object to be populated with data parsed from the spreadsheet.
   */
  private ScreenResult _screenResult;
  private HSSFWorkbook _dataWorkbook;
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
  public ScreenResult parse(
    InputStream metadataExcelFileInStream,
    InputStream dataExcelFileInStream)
  {
    _metadataExcelFileInStream = metadataExcelFileInStream;
    _dataExcelFileInStream = dataExcelFileInStream;
    try {
      POIFSFileSystem metadataFs = new POIFSFileSystem(_metadataExcelFileInStream);
      _metadataWorkbook = new HSSFWorkbook(metadataFs);
      parseMetadata();
      POIFSFileSystem dataFs = new POIFSFileSystem(_dataExcelFileInStream);
      _dataWorkbook = new HSSFWorkbook(dataFs);
      parseData();
    }
    catch (Exception e) {
      // TODO: log this
      e.printStackTrace();
      _errors.addError(UNKNOWN_ERROR + ": " + e.getMessage());
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
   * Find the metadata worksheet.
   */
  private void initializeMetadataSheet() {
    // TODO: find the sheet in a more reliable, flexible fashion
    HSSFSheet metadataSheet = _metadataWorkbook.getSheetAt(0);

    // TODO: take action if verification fails
    verifyStandardMetadataFormat(metadataSheet);

    _metadataCellParserFactory = new CellReader.Factory(_metadataWorkbook.getSheetName(0),
                                               metadataSheet,
                                               _errors);  
  }
  
  private HSSFSheet initializeDataSheet(int index)
  {
    // TODO: find the sheet in a more reliable, flexible fashion
    HSSFSheet dataSheet = _dataWorkbook.getSheetAt(index);

    // TODO: take action if verification fails
    //verifyStandardDataFormat(metadataSheet);

    _dataCellParserFactory = new CellReader.Factory(_dataWorkbook.getSheetName(0),
                                                    dataSheet,
                                                    _errors);  
    return dataSheet;
  }


  private CellReader metadataCell(MetadataRow row, int dataHeader) 
  {
    return _metadataCellParserFactory.newCellReader((short) (METADATA_FIRST_DATA_HEADER__COLUMN_INDEX + dataHeader),
                                                    (short) (METADATA_FIRST_DATA_ROW_INDEX + row.ordinal()));
  }
  
  private CellReader dataCell(int row, DataColumn column)
  {
    return _dataCellParserFactory.newCellReader((short) column.ordinal(),
                                                (short) row);
  }

  private CellReader dataCell(int row, int iDataHeader)
  {
    return _dataCellParserFactory.newCellReader((short) (DataColumn.FIRST_DATA_HEADER.ordinal() + iDataHeader),
                                                (short) row);
  }
                                        
  
  /**
   * Parse the workbook containing the ScreenResult metadata
   */
  private void parseMetadata() {
    initializeMetadataSheet();
    parseMetaMetadata();
    for (int iDataHeader = 0; metadataCell(MetadataRow.COLUMN_TYPE,
                                   iDataHeader).
                                   getString().equalsIgnoreCase("data"); ++iDataHeader) {
      ResultValueType rvt = 
        new ResultValueType(_screenResult,
                            metadataCell(MetadataRow.NAME, iDataHeader).getString(),
                            metadataCell(MetadataRow.REPLICATE, iDataHeader).getInteger(),
                            _rawOrDerivedParser.parse(metadataCell(MetadataRow.RAW_OR_DERIVED, iDataHeader)),
                            _booleanParser.parse(metadataCell(MetadataRow.IS_ASSAY_ACTIVITY_INDICATOR, iDataHeader)),
                            _primaryOrFollowUpParser.parse(metadataCell(MetadataRow.PRIMARY_OR_FOLLOWUP, iDataHeader)),
                            metadataCell(MetadataRow.ASSAY_PHENOTYPE,iDataHeader).getString(),
                            _booleanParser.parse(metadataCell(MetadataRow.IS_CHERRY_PICK, iDataHeader)));
      _columnsDerivedFromMap.put(metadataCell(MetadataRow.COLUMN_IN_TEMPLATE, iDataHeader).getString(), rvt);
      rvt.setDescription(metadataCell(MetadataRow.DESCRIPTION, iDataHeader).getString());
      rvt.setTimePoint(metadataCell(MetadataRow.TIME_POINT, iDataHeader).getString());
      if (rvt.isDerived()) {
        rvt.setDerivedFrom(new TreeSet<ResultValueType>(_columnsDerivedFromParser.parseList(metadataCell(MetadataRow.COLUMNS_DERIVED_FROM, iDataHeader))));
        rvt.setHowDerived(metadataCell(MetadataRow.HOW_DERIVED, iDataHeader).getString());
        // TODO: should warn if these values *are* defined and !isDerivedFrom()
      }
      if (rvt.isActivityIndicator()) {
        rvt.setActivityIndicatorType(_activityIndicatorTypeParser.parse(metadataCell(MetadataRow.ACTIVITY_INDICATOR_TYPE, iDataHeader)));
        rvt.setIndicatorDirection(indicatorDirectionParser.parse(metadataCell(MetadataRow.INDICATOR_DIRECTION, iDataHeader)));
        rvt.setIndicatorCutoff(metadataCell(MetadataRow.INDICATOR_CUTOFF, iDataHeader).getDouble());
        // TODO: should warn if these values *are* defined and !isActivityIndicator()
      }
      rvt.setComments(metadataCell(MetadataRow.COMMENTS, iDataHeader).getString());
    }
  }

  /**
   * Parse the workbook containing the ScreenResult data.
   * @throws ExtantLibraryException if an existing Well entity cannot be found in the database
   */
  private void parseData() throws ExtantLibraryException
  {
    for (int iSheet = 0; iSheet < _dataWorkbook.getNumberOfSheets(); ++iSheet) {
      HSSFSheet sheet = initializeDataSheet(iSheet);
      for (int iRow = RAWDATA_FIRST_DATA_ROW_INDEX; iRow <= sheet.getLastRowNum(); ++iRow) {
        Well well = findWell(iRow);
        dataCell(iRow, DataColumn.TYPE).getString(); // TODO: use this value?
        boolean excludeWell = _booleanParser.parse(dataCell(iRow, DataColumn.EXCLUDE));
        int iDataHeader = 0;
        for (ResultValueType rvt : _screenResult.getResultValueTypes()) {
          CellReader cell = dataCell(iRow, iDataHeader);
          String value =
            !rvt.isActivityIndicator() ? cell.getDouble().toString() :
              rvt.getActivityIndicatorType() == ActivityIndicatorType.BOOLEAN ? cell.getBoolean().toString() :
                rvt.getActivityIndicatorType() == ActivityIndicatorType.NUMERICAL ? cell.getDouble().toString() :
                  cell.getString();
          new ResultValue(rvt,
                          well,
                          value,
                          excludeWell);
          ++iDataHeader;
        }
      }
    }
  }

  // TODO: this needs to be moved to our DAO; probably as a findEntityByBusinessKey()
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
   * Parses the "meta" worksheet. Yes, we actually have a tab called "meta" in a
   * workbook file that itself contains metadata for our ScreenResult metadata;
   * thus the double-meta prefix.  Life is complex.
   */
  private void parseMetaMetadata() {
    HSSFSheet metametaSheet = _metadataWorkbook.getSheet(METADATA_META_SHEET_NAME);
    if (metametaSheet == null) {
      _errors.addError(NO_METADATA_META_SHEET_ERROR);
      return;
    }
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
  
  /**
   * Interface for the various cell parsers used by the
   * {@link ScreenResultParser} class. Cell parsers read the value from a cell
   * and convert to a more strictly typed object, for use within our entity
   * model. (This interface is probably a case of over-engineering.)
   * 
   * @author ant
   */
  private interface CellValueParser<T>
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
  private class CellVocabularyParser<T> implements CellValueParser<T>
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
                                String delimeterRegex)
    {
      _parsedValue2SystemValue = parsedValue2SystemValue;
      _valueToReturnIfUnparseable = valueToReturnIfUnparseable;
      _errorMessage = errorMessage;
      _delimiterRegex = delimeterRegex;
    }
    
    
    // public methods

    public T parse(CellReader cell) 
    {
      String cellValue = cell.getString().toLowerCase().trim();
      return doParse(cellValue, cell);
    }

    public List<T> parseList(CellReader cell) {
      List<T> result = new ArrayList<T>();
      String textMultiValue = cell.getString().toLowerCase().trim();
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
      text = text.trim();
      for (Iterator iter = _parsedValue2SystemValue.keySet()
                                                   .iterator(); iter.hasNext();) {
        String pattern = (String) iter.next();
        if (pattern.equalsIgnoreCase(text)) {
          return _parsedValue2SystemValue.get(pattern);
        }
      }
      _errors.addError(_errorMessage, cell);
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
