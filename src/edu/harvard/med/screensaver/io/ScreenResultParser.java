// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import edu.harvard.med.screensaver.io.CellParser.Factory;
import edu.harvard.med.screensaver.model.screenresults.ActivityIndicatorType;
import edu.harvard.med.screensaver.model.screenresults.IndicatorDirection;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

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

  private static final int FIRST_DATA_ROW_INDEX = 1;
  private static final int FIRST_DATA_HEADER__COLUMN_INDEX = 5;
  /**
   * The data rows of the metadata worksheet. Order of enum values is
   * significant, as we use the ordinal() method.
   */
  private enum Row { 
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
  private static final int METADATA_ROW_COUNT = Row.values().length;

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

  
  // instance data members
  
  private InputStream _dataExcelFileInStream;
  private InputStream _metadataExcelFileInStream;
  /**
   * The ScreenResult object to be populated with data parsed from the spreadsheet.
   */
  private ScreenResult _screenResult;
  private HSSFWorkbook _wb;
  private Map<String,ResultValueType> _columnsDerivedFromMap = new HashMap<String,ResultValueType>();
  private CellValueParser<ResultValueType> _columnsDerivedFromParser = 
    new CellValueParser<ResultValueType>(_columnsDerivedFromMap);
  private CellValueParser<IndicatorDirection> indicatorDirectionParser = 
    new CellValueParser<IndicatorDirection>(indicatorDirectionMap);
  private CellValueParser<ActivityIndicatorType> activityIndicatorTypeParser = 
    new CellValueParser<ActivityIndicatorType>(activityIndicatorTypeMap);
  private CellValueParser<Boolean> rawOrDerivedParser = 
    new CellValueParser<Boolean>(rawOrDerivedMap);
  private CellValueParser<Boolean> primaryOrFollowUpParser = 
    new CellValueParser<Boolean>(primaryOrFollowUpMap);
  private CellValueParser<Boolean> booleanParser = 
    new CellValueParser<Boolean>(booleanMap);
  private ParseErrorManager _errors = new ParseErrorManager();
  private Factory _metadataSheetCellParserFactory;
  
  
  // constructors

  public ScreenResultParser(InputStream metadataExcelFileInStream,
                            InputStream dataExcelFileInStream)
  {
    _metadataExcelFileInStream = metadataExcelFileInStream;
    _dataExcelFileInStream = dataExcelFileInStream;
  }
  

  // public methods
  
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
  public ScreenResult parse()
  {
    try {
      POIFSFileSystem fs = new POIFSFileSystem(_metadataExcelFileInStream);
      _wb = new HSSFWorkbook(fs);
      parseMetaMetadata();
      parseMetadata();
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
    HSSFSheet metadataSheet = _wb.getSheetAt(0);

    // TODO: take action if verification fails
    verifyStandardFormat(metadataSheet);

    _metadataSheetCellParserFactory = new CellParser.Factory(_wb.getSheetName(0),
                                                             metadataSheet,
                                                             _errors);  
  }
  
  private CellParser cell(Row row, int dataHeader) 
  {
    return _metadataSheetCellParserFactory.newCellParser((short) (FIRST_DATA_HEADER__COLUMN_INDEX + dataHeader),
                                                   (short) (FIRST_DATA_ROW_INDEX + row.ordinal()));
  }
  
  /**
   * Parse the metaata worksheet. This is where the real work of this class gets
   * done. Once upon a time, it was really long, ugly method. But look at it
   * now!
   */
  private void parseMetadata() {
    initializeMetadataSheet();
    for (int iDataHeader = 0; cell(Row.COLUMN_TYPE,
                                   iDataHeader).
                                   getString().equalsIgnoreCase("data"); ++iDataHeader) {
      ResultValueType rvt = 
        new ResultValueType(_screenResult,
                            cell(Row.NAME, iDataHeader).getString(),
                            cell(Row.REPLICATE, iDataHeader).getInteger(),
                            rawOrDerivedParser.parse(cell(Row.RAW_OR_DERIVED, iDataHeader)),
                            booleanParser.parse(cell(Row.IS_ASSAY_ACTIVITY_INDICATOR, iDataHeader)),
                            primaryOrFollowUpParser.parse(cell(Row.PRIMARY_OR_FOLLOWUP, iDataHeader)),
                            cell(Row.ASSAY_PHENOTYPE,iDataHeader).getString(),
                            booleanParser.parse(cell(Row.IS_CHERRY_PICK, iDataHeader)));
      _columnsDerivedFromMap.put(cell(Row.COLUMN_IN_TEMPLATE, iDataHeader).getString(), rvt);
      rvt.setDescription(cell(Row.DESCRIPTION, iDataHeader).getString());
      rvt.setTimePoint(cell(Row.TIME_POINT, iDataHeader).getString());
      if (rvt.isDerived()) {
        rvt.setDerivedFrom(new TreeSet<ResultValueType>(_columnsDerivedFromParser.parseList(cell(Row.COLUMNS_DERIVED_FROM, iDataHeader))));
        rvt.setHowDerived(cell(Row.HOW_DERIVED, iDataHeader).getString());
        // TODO: should warn if these values are defined and !isDerivedFrom()
      }
      if (rvt.isActivityIndicator()) {
        rvt.setActivityIndicatorType(activityIndicatorTypeParser.parse(cell(Row.ACTIVITY_INDICATOR_TYPE, iDataHeader)));
        rvt.setIndicatorDirection(indicatorDirectionParser.parse(cell(Row.INDICATOR_DIRECTION, iDataHeader)));
        rvt.setIndicatorCutoff(cell(Row.INDICATOR_CUTOFF, iDataHeader).getDouble());
        // TODO: should warn if these values are defined and
        // !isActivityIndicator()
      }
      rvt.setComments(cell(Row.COMMENTS, iDataHeader).getString());
    }
  }

  /**
   * Returns <code>true</code> iff the specified worksheet "looks like" it
   * contains a valid metadata structure. It could be wrong.
   * 
   * @param sheet the sheet to verify
   * @return <code>true</code> iff the specified worksheet "looks like" it
   *         contains a valid metadata structure
   */
  private boolean verifyStandardFormat(HSSFSheet sheet) {
    // TODO: implement
    return true;
  }

  /**
   * Parses the "meta" worksheet. Yes, we actually have a tab called "meta" in a
   * workbook file that itself contains metadata for our ScreenResult metadata;
   * thus the double-meta prefix.  Life is complex.
   */
  private void parseMetaMetadata() {
    HSSFSheet metametaSheet = _wb.getSheet(METADATA_META_SHEET_NAME);
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
   * Parses the value of a cell, mapping a text value to an internal, system
   * object representation. Handles single-valued cells as well as cells that
   * contain lists of values. Note that this class is a non-static inner class
   * and references instance methods of {@link ScreenResultParser}.
   */
  private class CellValueParser<T>
  {
    // TODO: class methods needs javadocs
    
    // static data members
    
    private static final String DEFAULT_ERROR_MESSAGE = "unparseable value";
    private static final String DEFAULT_DELIMITER_REGEX = ",";

    
    // instance data members
    
    private Map<String,T> _parsedValue2SystemValue;
    private String _delimiterRegex = ",";
    private String _errorMessage;
    
    
    // constructors
    
    public CellValueParser(Map<String,T> parsedValue2SystemValue)
    {
      this(parsedValue2SystemValue,
           DEFAULT_ERROR_MESSAGE,
           DEFAULT_DELIMITER_REGEX);
    }
                           

    public CellValueParser(Map<String,T> parsedValue2SystemValue,
                           String errorMessage)
    {
      this(parsedValue2SystemValue,
           errorMessage,
           DEFAULT_DELIMITER_REGEX);
    }
    
    public CellValueParser(Map<String,T> parsedValue2SystemValue,
                           String errorMessage,
                           String delimeterRegex)
    {
      _parsedValue2SystemValue = parsedValue2SystemValue;
      _errorMessage = errorMessage;
      _delimiterRegex = delimeterRegex;
    }
    
    
    // public methods

    public T parse(CellParser cell) 
    {
      String cellValue = cell.getString().toLowerCase().trim();
      return doParse(cellValue, cell);
    }

    public List<T> parseList(CellParser cell) {
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

    private T doParse(String text, CellParser cell) {
      text = text.trim();
      for (Iterator iter = _parsedValue2SystemValue.keySet()
                                                   .iterator(); iter.hasNext();) {
        String pattern = (String) iter.next();
        if (pattern.equalsIgnoreCase(text)) {
          return _parsedValue2SystemValue.get(pattern);
        }
      }
      _errors.addError(_errorMessage, cell);
      return null;
    }

  }
}
