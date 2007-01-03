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
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.harvard.med.screensaver.CommandLineApplication;
import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.db.DAOTransaction;
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
import edu.harvard.med.screensaver.model.screenresults.ActivityIndicatorType;
import edu.harvard.med.screensaver.model.screenresults.AssayWellType;
import edu.harvard.med.screensaver.model.screenresults.IndicatorDirection;
import edu.harvard.med.screensaver.model.screenresults.PartitionedValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.AssayReadoutType;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUserClassification;

import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
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
  
  private static final int SHORT_OPTION = 0;
  private static final int LONG_OPTION = 1;

  private static final String[] INPUT_FILE_OPTION = { "f", "input-file" };
  private static final String[] SCREEN_OPTION = { "s", "screen" };
  private static final String[] IMPORT_OPTION = { "i", "import" };
  private static final String[] IGNORE_FILE_PATHS_OPTION = { "p", "ignore-file-paths" };
  private static final String[] WELLS_OPTION = { "w", "wells" };

  private static final String ERROR_ANNOTATED_WORKBOOK_FILE_EXTENSION = "errors.xls";

  private static final Logger log = Logger.getLogger(ScreenResultParser.class);


  // TODO: move these a messages (Spring) resource file
  private static final String NO_FIRST_DATA_DEPOSITION_DATE_FOUND_ERROR = "\"First Data Deposition\" value not found";
  private static final String NO_SCREEN_ID_FOUND_ERROR = "Screen ID not found";
  private static final String DATA_HEADER_SHEET_NOT_FOUND_ERROR = "\"Data Headers\" sheet not found";
  private static final String UNKNOWN_ERROR = "unknown error";
  private static final String NO_DATA_SHEETS_FOUND_ERROR = "no data worksheets were found; no result data was imported";
  private static final String NO_SUCH_WELL = "library well does not exist";
  private static final String NO_SUCH_LIBRARY_WITH_PLATE = "no library with given plate number";


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
      partitionedValueMap.put(pv.getValue()
                                .toLowerCase(), pv);
      partitionedValueMap.put(pv.getValue()
                                .toUpperCase(), pv);
    }

    assayWellTypeMap.put(AssayWellType.EXPERIMENTAL.getAbbreviation(), AssayWellType.EXPERIMENTAL);
    assayWellTypeMap.put(AssayWellType.EMPTY.getAbbreviation(), AssayWellType.EMPTY);
    assayWellTypeMap.put(AssayWellType.ASSAY_POSITIVE_CONTROL.getAbbreviation(), AssayWellType.ASSAY_POSITIVE_CONTROL);
    assayWellTypeMap.put(AssayWellType.ASSAY_CONTROL.getAbbreviation(), AssayWellType.ASSAY_CONTROL);
    assayWellTypeMap.put(AssayWellType.OTHER.getAbbreviation(), AssayWellType.OTHER);
    assayWellTypeMap.put(AssayWellType.LIBRARY_CONTROL.getAbbreviation(), AssayWellType.LIBRARY_CONTROL);
    assayWellTypeMap.put(AssayWellType.BUFFER.getAbbreviation(), AssayWellType.BUFFER);
    assert assayWellTypeMap.size() == AssayWellType.values().length : "assayWellTypeMap not initialized properly";
  }


  // static methods

  @SuppressWarnings("static-access")
  public static void main(String[] args) throws FileNotFoundException
  {
    CommandLineApplication app = new CommandLineApplication(args);
    app.addCommandLineOption(OptionBuilder.hasArg()
                                          .withArgName("#")
                                          .isRequired()
                                          .withDescription("the screen number of the screen for which the screen result is being parsed")
                                          .withLongOpt(SCREEN_OPTION[LONG_OPTION])
                                          .create(SCREEN_OPTION[SHORT_OPTION]));
    app.addCommandLineOption(OptionBuilder.hasArg()
                                          .withArgName("file")
                                          .isRequired()
                                          .withDescription("the file location of the Excel workbook file holding the Screen Result metadata")
                                          .withLongOpt(INPUT_FILE_OPTION[LONG_OPTION])
                                          .create(INPUT_FILE_OPTION[SHORT_OPTION]));
    app.addCommandLineOption(OptionBuilder.hasArg()
                                          .withArgName("#")
                                          .isRequired(false)
                                          .withDescription("the number of wells to print out")
                                          .withLongOpt(WELLS_OPTION[LONG_OPTION])
                                          .create(WELLS_OPTION[SHORT_OPTION]));
    app.addCommandLineOption(OptionBuilder.withDescription("Import screen result into database if parsing is successful.  "
                                                           + "(By default, the parser only validates the input and then exits.)")
                                          .withLongOpt(IMPORT_OPTION[LONG_OPTION])
                                          .create(IMPORT_OPTION[SHORT_OPTION]));
    try {
      if (!app.processOptions(/* acceptDatabaseOptions= */true, 
                              /* showHelpOnError= */true)) {
        return;
      }
      
      app.setDatabaseRequired(app.isCommandLineFlagSet(IMPORT_OPTION[SHORT_OPTION]));
      
      final File inputFile = app.getCommandLineOptionValue(INPUT_FILE_OPTION[SHORT_OPTION],
                                                           File.class);
      cleanOutputDirectory(inputFile.getAbsoluteFile().getParentFile());

      Screen screen = null;
      ScreenResultParser screenResultParser = null;
      if (app.isCommandLineFlagSet(IMPORT_OPTION[SHORT_OPTION])) {
        // database-dependent screenResultParser
        screen = findScreenOrExit(app);
        screenResultParser = (ScreenResultParser) app.getSpringBean("screenResultParser");
      }
      else {
        // database-independent screenResultParser
        int screenNumber = Integer.parseInt(app.getCommandLineOptionValue(SCREEN_OPTION[SHORT_OPTION]));
        screen = makeDummyScreen(screenNumber);
        screenResultParser = (ScreenResultParser) app.getSpringBean("mockScreenResultParser");
      }

      final boolean ignoreFilePathOptions = app.isCommandLineFlagSet(IGNORE_FILE_PATHS_OPTION[SHORT_OPTION]);
      final Integer wellsToPrint = app.getCommandLineOptionValue(WELLS_OPTION[SHORT_OPTION],
                                                                 Integer.class);
      final Screen finalScreen = screen;
      final ScreenResultParser finalScreenResultParser = screenResultParser;
      final InputStream inputFileStream = new FileInputStream(inputFile);
      screenResultParser._dao.doInTransaction(new DAOTransaction() {
        public void runTransaction()
        {
          ScreenResult screenResult = finalScreenResultParser.parse(finalScreen,
                                                                    inputFile,
                                                                    inputFileStream,
                                                                    ignoreFilePathOptions);
          if (wellsToPrint != null) {
            new ScreenResultPrinter(screenResult).print(wellsToPrint);
          }
          else {
            new ScreenResultPrinter(screenResult).print();
          }

        }
      });
      screenResultParser.outputErrorsInAnnotatedWorkbooks(null,
                                                          ERROR_ANNOTATED_WORKBOOK_FILE_EXTENSION);
      if (screenResultParser.getErrors()
                            .size() > 0) {
        System.err.println("Errors encountered during parse:");
        for (ParseError error : screenResultParser.getErrors()) {
          System.err.println(error.toString());
        }
      }
      else {
        screenResultParser._dao.persistEntity(screen);
        System.err.println("Success!");
      }
    }
    catch (IOException e) {
      String errorMsg = "I/O error: " + e.getMessage();
      log.error(errorMsg);
      System.err.println(errorMsg);
    }
    catch (ParseException e) {
      System.err.println("error parsing command line options: "
                         + e.getMessage());
    }
    catch (Exception e) {
      e.printStackTrace();
      System.err.println("application error: " + e.getMessage());
    }
  }

  public static Screen makeDummyScreen(int screenNumber)
  {
    ScreeningRoomUser labHead = new ScreeningRoomUser(new Date(),
                                                      "Joe",
                                                      "Screener",
                                                      "joe_screener_"
                                                        + screenNumber
                                                        + "@hms.harvard.edu",
                                                      "",
                                                      "",
                                                      "",
                                                      "",
                                                      "",
                                                      ScreeningRoomUserClassification.ICCBL_NSRB_STAFF,
                                                      true);
    Screen screen = new Screen(labHead,
                               labHead,
                               screenNumber,
                               new Date(),
                               ScreenType.SMALL_MOLECULE,
                               "Dummy screen");

    return screen;
  }

  private static Screen findScreenOrExit(CommandLineApplication app) throws ParseException
  {
    int screenNumber = Integer.parseInt(app.getCommandLineOptionValue(SCREEN_OPTION[SHORT_OPTION]));
    DAO dao = (DAO) app.getSpringBean("dao");
    Screen screen = dao.findEntityByProperty(Screen.class, 
                                              "hbnScreenNumber", 
                                              screenNumber);
    if (screen == null) {
      System.err.println("screen " + screenNumber + " does not exist");
      System.exit(1);
    }
    if (screen.getScreenResult() != null) {
      System.err.println("screen " + screenNumber + " already has a screen result");
      System.exit(1);
    }
    return screen;
  }

  private static void cleanOutputDirectory(File parentFile)
  {
    if (!parentFile.isDirectory()) {
      log.warn("cannot clean the directory '" + parentFile + "' since it is not a directory");
      return;
    }
    log.info("cleaning directory " + parentFile);
    Iterator iterator = FileUtils.iterateFiles(parentFile,
                                               new String[] {ERROR_ANNOTATED_WORKBOOK_FILE_EXTENSION, ".out"},
                                               false);
    while (iterator.hasNext()) {
      File fileToDelete = (File) iterator.next();
      log.info("deleting previously generated outputfile '" + fileToDelete + "'");
      fileToDelete.delete();
    }
  }
  
  
  // instance data members

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

  private DAO _dao;
  private Factory _metadataCellParserFactory;
  private Factory _dataCellParserFactory;
  private Map<Integer,Short> _dataHeaderIndex2DataHeaderColumn;
  private Set<Library> _preloadedLibraries;
  private Map<Integer,Library> _plate2Library;

  
  // public methods and constructors

  public ScreenResultParser(DAO dao) 
  {
    _dao = dao;
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
      return parse(screen, 
                   workbookFile, 
                   new FileInputStream(workbookFile), 
                   /*ignored file paths=*/ true);
    }
    catch (FileNotFoundException e) {
      e.printStackTrace();
      String errorMsg = UNKNOWN_ERROR + " of type : " + e
      .getClass() + ": " + e
      .getMessage();
      _errors
      .addError(errorMsg);
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
   * @param the workbook file to be parsed; this File object is used only to
   *          communicate the name of the file; the file itself is not accessed
   *          (we rely upon the inputStream argument instead)
   * @param an InputStream that provides the workbook file as...well...
   *          an InputStream
   * @return a ScreenResult object containing the data parsed from the workbook
   *         file; <code>null</code> if a fatal error occurs
   * @see #getErrors()
   * @motivation For use by the web application UI; the InputStream allows us to
   *             avoid making (another) temporary copy of the file.
   */
  public ScreenResult parse(Screen screen, File workbookFile, InputStream inputStream)
  {
    return parse(screen, 
                 workbookFile, 
                 inputStream, 
                 true /*arg ignored*/);
  }

  /**
   * Parses the specified workbook file that contains Screen Result data in this
   * <a
   * href="https://wiki.med.harvard.edu/ICCBL/NewScreenResultFileFormat">
   * format</a>. Errors encountered during parsing are stored with this object
   * until a parse() method is called again, and these errors can be retrieved
   * via {@link #getErrors}. The returned <code>ScreenResult</code> may only
   * be partially populated if errors are encountered, so always call
   * getErrors() to determine parsing success.
   * 
   * @param the parent Screen of the Screen Result being parsed
   * @param the workbook file to be parsed
   * @param an InputStream that provides the workbook file as...well... an
   *          InputStream
   * @param ignoreFilePaths if <code>true</code>, the directory path of the
   *          file names of the workbooks containing the "raw data" will be
   *          ignored; only the base file name will be used and it will be
   *          assumed that the file is in the same directory as the "metadata"
   *          workbook file.
   * @return a ScreenResult object containing the data parsed from the workbook
   *         file; <code>null</code> if a fatal error occurs
   * @see #getErrors()
   */
  public ScreenResult parse(
    Screen screen,
    File workbookFile,
    InputStream workbookInputStream,
    boolean ignoreFilePaths)
  {
    _screenResult = null;
    _errors = new ParseErrorManager();
    _preloadedLibraries = new HashSet<Library>();
    _plate2Library = new HashMap<Integer,Library>();
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
                                                                    workbook, 
                                                                    ignoreFilePaths);
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

  private HSSFSheet initializeDataSheet(Workbook workbook, int sheetIndex)
  {
    // TODO: find the sheet in a more reliable, flexible fashion
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
   * @param ignoreFilePaths
   * @throws UnrecoverableScreenResultParseException 
   */
  private DataHeadersParseResult parseDataHeaders(
    Screen screen,
    Workbook workbook, 
    boolean ignoreFilePaths) 
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
    String forColumnInRawDataWorksheet = dataHeadersCell(DataHeaderRow.COLUMN_IN_DATA_WORKSHEET, iDataHeader, true).getString();
    if (forColumnInRawDataWorksheet != null) {
      _dataHeaderIndex2DataHeaderColumn.put(iDataHeader,
                                            (short) Cell.columnLabelToIndex(forColumnInRawDataWorksheet));
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
  private void parseData(
    Workbook workbook,
    ScreenResult screenResult) 
    throws ExtantLibraryException, IOException, UnrecoverableScreenResultParseException
  {
    int dataSheetsParsed = 0;
    for (int iSheet = 0; iSheet < workbook.getWorkbook().getNumberOfSheets(); ++iSheet) {
      HSSFSheet sheet = initializeDataSheet(workbook, iSheet);
      if (isActiveSheetRawDataSheet()) {
        ++dataSheetsParsed;
        log.info("parsing sheet " + workbook.getWorkbookFile().getName() + ":" + workbook.getWorkbook().getSheetName(iSheet));
        for (int iRow = RAWDATA_FIRST_DATA_ROW_INDEX; iRow <= sheet.getLastRowNum(); ++iRow) {
          Well well = findWell(iRow);
          if (well != null) {
            // TODO: should verify with Library Well Type, if not specific to AssayWellType
            AssayWellType assayWellType = _assayWellTypeParser.parse(dataCell(iRow, DataColumn.ASSAY_WELL_TYPE));

            List<ResultValueType> wellExcludes = _excludeParser.parseList(dataCell(iRow, DataColumn.EXCLUDE));

            int iDataHeader = 0;
            for (ResultValueType rvt : screenResult.getResultValueTypes()) {
              Cell cell = dataCell(iRow, iDataHeader);
              try {
                if (rvt.isActivityIndicator()) {
                  String value;
                  if (rvt.getActivityIndicatorType() == ActivityIndicatorType.BOOLEAN) {
                    if (cell.isBoolean()) {
                      value = cell.getBoolean().toString();
                    } 
                    else {
                      value = _booleanParser.parse(cell).toString();
                    }
                    rvt.addResultValue(well,
                                       assayWellType,
                                       value,
                                       (wellExcludes != null && wellExcludes.contains(rvt)));
                  }
                  else if (rvt.getActivityIndicatorType() == ActivityIndicatorType.PARTITION) {
                    rvt.addResultValue(well,
                                       assayWellType,
                                       _partitionedValueParser.parse(cell).toString(),
                                       (wellExcludes != null && wellExcludes.contains(rvt)));
                  }
                  else if (rvt.getActivityIndicatorType() == ActivityIndicatorType.NUMERICAL) {
                    rvt.addResultValue(well,
                                       assayWellType,
                                       cell.getDouble(),
                                       cell.getDoublePrecision(),
                                       (wellExcludes != null && wellExcludes.contains(rvt)));
                  }
                }
                else {
                  // TODO: avoid the subtle bug of implicitly flagging a
                  // ResultValueType as non-numeric if an initial set of values are blank, but are
                  // then followed by (valid) numeric data
                  if ((!rvt.isNumericalnessDetermined() && cell.isNumeric()) || 
                    rvt.isNumericalnessDetermined() && rvt.isNumeric()) {
                    rvt.addResultValue(well,
                                       assayWellType,
                                       cell.getDouble(),
                                       cell.getDoublePrecision(),
                                       (wellExcludes != null && wellExcludes.contains(rvt)));
                  }
                  else {
                    rvt.addResultValue(well,
                                       assayWellType,
                                       cell.getString(),
                                       (wellExcludes != null && wellExcludes.contains(rvt)));
                  }
                }
              }
              catch (IllegalArgumentException e) {
                // inconsistency in numeric or string types in RVT's result values
                _errors.addError(e.getMessage(), cell);
              }
              ++iDataHeader;
            }
          }
        }
      }
    }
    if (dataSheetsParsed == 0) {
      _errors.addError(NO_DATA_SHEETS_FOUND_ERROR);
    }
  }

  private boolean isActiveSheetRawDataSheet()
  {
    String stockPlateColumnName = dataCell(RAWDATA_HEADER_ROW_INDEX, DataColumn.STOCK_PLATE_ID).getAsString(false);
    return stockPlateColumnName.equals(DATA_SHEET__STOCK_PLATE_COLUMN_NAME);
  }

  private Well findWell(int iRow) throws UnrecoverableScreenResultParseException
  {
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
    
    Well well = _dao.findWell(plateNumber, wellName);
    if (well == null) {
      throw new UnrecoverableScreenResultParseException(
        NO_SUCH_WELL + ": " + plateNumber + ":" + wellName,
        wellNameCell);
    }

    return well;
  }

  /**
   * @motivation database I/O optimization
   */
  private Library findLibraryWithPlate(Integer plateNumber)
  {
    if (!_plate2Library.containsKey(plateNumber)) {
      Library library = _dao.findLibraryWithPlate(plateNumber); 
      for (int p = library.getStartPlate(); p <= library.getEndPlate(); ++p) {
        _plate2Library.put(p, library);
      }
    }
    return _plate2Library.get(plateNumber);
  }

  /**
   * @motivation database I/O optimization
   */
  private void preloadLibraryWells(Library library)
  {
    if (!_preloadedLibraries.contains(library)) {
      _dao.loadOrCreateWellsForLibrary(library);
      _preloadedLibraries.add(library);
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
          Cell valueCell = factory.getCell((short) 1, iRow, true);
          if (rowLabel.equalsIgnoreCase(ScreenInfoRow.FIRST_DATA_DEPOSITION.getDisplayText())) {
            parsedScreenInfo.setDate(valueCell.getDate());
          }
          else if (rowLabel.equalsIgnoreCase(ScreenInfoRow.ID.getDisplayText())) {
            parsedScreenInfo.setScreenId(valueCell.getInteger());
          }
        }
      }
    }
    if (parsedScreenInfo.getScreenNumber() == null) {
      _errors.addError(NO_SCREEN_ID_FOUND_ERROR);
    }
    else if (!parsedScreenInfo.getScreenNumber().equals(screen.getScreenNumber())) {
      _errors.addError("screen result data file is for screen number " + parsedScreenInfo.getScreenNumber() + ", expected " + screen.getScreenNumber());
    }
    if (parsedScreenInfo.getDateCreated() == null) {
      _errors.addError(NO_FIRST_DATA_DEPOSITION_DATE_FOUND_ERROR);
      parsedScreenInfo.setDate(new Date());
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

      if (textMultiValue == null) {
        return result;
      }
      
      String[] textValues = textMultiValue.split(",");
      for (int i = 0; i < textValues.length; i++) {
        String text = textValues[i];
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
}
