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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUserClassification;

import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.hibernate.Hibernate;

/**
 * Parses data from a workbook files (a.k.a. Excel spreadsheets) necessary for
 * instantiating a
 * {@link edu.harvard.med.screensaver.model.screenresults.ScreenResult}.
 * ScreenResult data consists of both metadata and raw data. The parser will
 * accept an all-in-one import file, where the metadata and raw data are in the
 * same workbook, but also will accept the metadata and raw data in separate
 * workbooks. For this latter case, the raw data can be in multiple workbooks
 * and the workbook file names must be specified as comma-delimited list in cell
 * B1 of the first sheet of the metadata workbook. In the former case, the names
 * of the worksheets containing the raw data must be listed in this cell
 * instead. By convention, each worksheet contains the raw data for a single
 * plate, but the parser is indifferent to how data may be arranged across
 * worksheets.
 * <p>
 * The parser also supports two formats: a legacy format and the "new" format.
 * Differences are documents at
 * https://wiki.med.harvard.edu/ICCBL/NewScreenResultFileFormat.
 * <p>
 * The metadata is used to instantiate
 * {@link edu.harvard.med.screensaver.model.screenresults.ResultValueType}
 * objects, while the raw data is used to instantiate each of the
 * <code>ResultValueType</code>'s
 * {@link edu.harvard.med.screensaver.model.screenresults.ResultValue} objects.
 * Altogether these objects are used instantiate a {@link ScreenResult} object,
 * which is the returned result of the {@link #parseLegacy(File, boolean)}
 * method.
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
 * after each call to {@link #parseLegacy(File, boolean)}.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
// TODO: consider renaming "metadata" to "dataHeaders" (methods and variables),
// as this is more in keeping with the new file format
// TODO: now that this class supports dual formats ("legacy" and "new"), we
// should separate behavior via polymorphism, having 2 subclasses that allow
// each format to handled appropriately
public class ScreenResultParser implements ScreenResultWorkbookSpecification
{

  // static data members
  
  private static final int SHORT_OPTION = 0;
  private static final int LONG_OPTION = 1;

  private static final String[] INPUT_FILE_OPTION = { "f", "input-file" };
  private static final String[] SCREEN_OPTION = { "s", "screen" };
  private static final String[] LEGACY_FORMAT_OPTION = { "l", "legacy-format" };
  private static final String[] IMPORT_OPTION = { "i", "import" };
  private static final String[] IGNORE_FILE_PATHS_OPTION = { "p", "ignore-file-paths" };
  private static final String[] WELLS_OPTION = { "w", "wells" };

  private static final String ERROR_ANNOTATED_WORKBOOK_FILE_EXTENSION = "errors.xls";

  private static final Logger log = Logger.getLogger(ScreenResultParser.class);


  // TODO: move these a messages (Spring) resource file
  private static final String NO_METADATA_META_SHEET_ERROR = "worksheet could not be found";
  private static final String NO_CREATED_DATE_FOUND_ERROR = "\"First Date Screened\" value not found";
  private static final String NO_SCREEN_ID_FOUND_ERROR = "no screen ID found";
  private static final String UNEXPECTED_DATA_HEADER_TYPE_ERROR = "unexpected data header type";
  private static final String REFERENCED_UNDEFINED_DATA_HEADER_ERROR = "referenced undefined data header";
  private static final String UNRECOGNIZED_INDICATOR_DIRECTION_ERROR = "unrecognized \"indicator direction\" value";
  private static final String METADATA_DATA_HEADER_COLUMNS_NOT_FOUND_ERROR = "data header columns not found";
  private static final String DATA_HEADER_SHEET_NOT_FOUND_ERROR = "\"Data Headers\" sheet not found";
  private static final String METADATA_UNEXPECTED_COLUMN_TYPE_ERROR = "expected column type of \"data\"";
  private static final String METADATA_NO_RAWDATA_FILES_SPECIFIED_ERROR = "raw data workbook files not specified";
  private static final String UNKNOWN_ERROR = "unknown error";
  private static final String NO_DATA_SHEETS_FOUND_ERROR = "no data worksheets were found; no result data was imported";


  private static SortedMap<String,IndicatorDirection> indicatorDirectionMap = new TreeMap<String,IndicatorDirection>();
  private static SortedMap<String,ActivityIndicatorType> activityIndicatorTypeMap = new TreeMap<String,ActivityIndicatorType>();
  private static SortedMap<String,Boolean> rawOrDerivedMap = new TreeMap<String,Boolean>();
  private static SortedMap<String,Boolean> primaryOrFollowUpMap = new TreeMap<String,Boolean>();
  private static SortedMap<String,Boolean> booleanMap = new TreeMap<String,Boolean>();
  private static SortedMap<String,PartitionedValue> partitionedValueMap = new TreeMap<String,PartitionedValue>();
  private static SortedMap<String,AssayWellType> assayWellTypeMap = new TreeMap<String,AssayWellType>();
  static {
    indicatorDirectionMap.put("<", IndicatorDirection.LOW_VALUES_INDICATE);
    indicatorDirectionMap.put(">", IndicatorDirection.HIGH_VALUES_INDICATE);

    // TODO: add conditional code that disallows legacy values when parsing new format
    activityIndicatorTypeMap.put("High values", ActivityIndicatorType.NUMERICAL); // legacy format
    activityIndicatorTypeMap.put("Low values", ActivityIndicatorType.NUMERICAL); // legacy format
    activityIndicatorTypeMap.put("Numeric", ActivityIndicatorType.NUMERICAL);
    activityIndicatorTypeMap.put("Numerical", ActivityIndicatorType.NUMERICAL);
    activityIndicatorTypeMap.put("A", ActivityIndicatorType.NUMERICAL); // legacy format
    activityIndicatorTypeMap.put("B", ActivityIndicatorType.NUMERICAL); // legacy format
    activityIndicatorTypeMap.put("Boolean", ActivityIndicatorType.BOOLEAN);
    activityIndicatorTypeMap.put("C", ActivityIndicatorType.BOOLEAN); // legacy format
    activityIndicatorTypeMap.put("Scaled", ActivityIndicatorType.PARTITION); // legacy format
    activityIndicatorTypeMap.put("Partitioned", ActivityIndicatorType.PARTITION);
    activityIndicatorTypeMap.put("Partition", ActivityIndicatorType.PARTITION);
    activityIndicatorTypeMap.put("D", ActivityIndicatorType.PARTITION); // legacy format

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
    assayWellTypeMap.put(AssayWellType.ASSAY_NEGATIVE_CONTROL.getAbbreviation(), AssayWellType.ASSAY_NEGATIVE_CONTROL);
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
    app.addCommandLineOption(OptionBuilder.withDescription("whether to ignore the file paths for the raw data workbook "
                                                           + "files (as specified in the metadata workbook); if option is "
                                                           + "provided all files will be expected to be found in the same directory; "
                                                           + "ignored unless 'legacy' option is specified")
                                          .withLongOpt(IGNORE_FILE_PATHS_OPTION[LONG_OPTION])
                                          .create(IGNORE_FILE_PATHS_OPTION[SHORT_OPTION]));
    app.addCommandLineOption(OptionBuilder.withDescription("indicates that workbook uses the legacy format for Screen Results")
                                          .withLongOpt(LEGACY_FORMAT_OPTION[LONG_OPTION])
                                          .create(LEGACY_FORMAT_OPTION[SHORT_OPTION]));
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
      final boolean parseLegacyFormat = app.isCommandLineFlagSet(LEGACY_FORMAT_OPTION[SHORT_OPTION]);
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
                                                                    parseLegacyFormat,
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
    ScreeningRoomUser collaborator1 = new ScreeningRoomUser(new Date(),
                                                            "Cindy",
                                                            "Collaborator",
                                                            "cindy_collaborator_"
                                                              + screenNumber
                                                              + "@hms.harvard.edu",
                                                            "",
                                                            "",
                                                            "",
                                                            "",
                                                            "",
                                                            ScreeningRoomUserClassification.OTHER,
                                                            true);
    ScreeningRoomUser collaborator2 = new ScreeningRoomUser(new Date(),
                                                            "Chris",
                                                            "Collaborator",
                                                            "chrisp_collaborator_"
                                                              + screenNumber
                                                              + "@hms.harvard.edu",
                                                            "",
                                                            "",
                                                            "",
                                                            "",
                                                            "",
                                                            ScreeningRoomUserClassification.OTHER,
                                                            true);
    Screen screen = new Screen(labHead,
                               labHead,
                               screenNumber,
                               new Date(),
                               ScreenType.SMALL_MOLECULE,
                               "Dummy screen");

    screen.addCollaborator(collaborator1);
    screen.addCollaborator(collaborator2);
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
  private SortedMap<String,ResultValueType> _columnsDerivedFromMap;

  private CellVocabularyParser<ResultValueType> _columnsDerivedFromParser;
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
  private Short _metadataFirstDataHeaderColumnIndex;
  private Map<Integer,Short> _dataHeaderIndex2DataHeaderColumn;
  private boolean _parseLegacyFormat;

  
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
                   /*legacy format=*/ false, 
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
                 false /*new format*/, 
                 true /*arg ignored*/);
  }

  /**
   * Parses the specified workbook file that contains Screen Result data in the
   * <a
   * href="https://wiki.med.harvard.edu/ICCBL/ResultDataStorageMetaFile">"legacy"
   * format</a>. Errors encountered during parsing are stored with this object
   * until a parse() method is called again, and these errors can be retrieved
   * via {@link #getErrors}. The returned <code>ScreenResult</code> may only
   * be partially populated if errors are encountered, so always call
   * getErrors() to determine parsing success.
   * 
   * @param the parent Screen of the Screen Result being parsed
   * @param the workbook file to be parsed
   * @param ignoreFilePaths if <code>true</code>, the directory path of the
   *          file names of the workbooks containing the "raw data" will be
   *          ignored; only the base file name will be used and it will be
   *          assumed that the file is in the same directory as the "metadata"
   *          workbook file.
   * @return a ScreenResult object containing the data parsed from the workbook
   *         file; <code>null</code> if a fatal error occurs (e.g. file not found)
   * @see #getErrors()
   */
  public ScreenResult parseLegacy(
    Screen screen,
    File metadataWorkbookFile,
    boolean ignoreFilePaths)
  {
    try {
      return parse(screen,
                   metadataWorkbookFile,
                   new FileInputStream(metadataWorkbookFile),
                   true /*legacy format*/,
                   ignoreFilePaths);
    }
    catch (FileNotFoundException e) {
      e
        .printStackTrace();
      String errorMsg = UNKNOWN_ERROR + " of type : " + e
        .getClass() + ": " + e
        .getMessage();
      _errors
        .addError(errorMsg);
    }
    return null;
  }
  
  /**
   * Parses the specified workbook file that contains Screen Result data in
   * either the <a
   * href="https://wiki.med.harvard.edu/ICCBL/ResultDataStorageMetaFile">"legacy"
   * format</a> or the <a
   * href="https://wiki.med.harvard.edu/ICCBL/NewScreenResultFileFormat">"new"
   * format</a>. Errors encountered during parsing are stored with this object
   * until a parse() method is called again, and these errors can be retrieved
   * via {@link #getErrors}. The returned <code>ScreenResult</code> may only
   * be partially populated if errors are encountered, so always call
   * getErrors() to determine parsing success.
   * 
   * @param the parent Screen of the Screen Result being parsed
   * @param the workbook file to be parsed
   * @param an InputStream that provides the workbook file as...well...
   *          an InputStream
   * @param parseLegacyFormat whether the parser is expected to parse the
   *          workbook(s) using the "legacy" or "new" format. The legacy format
   *          is only intended to be used to import the older, historical screen
   *          results, and will never be used by the web application UI.
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
    boolean parseLegacyFormat,
    boolean ignoreFilePaths)
  {
    _screenResult = null;
    _parseLegacyFormat = parseLegacyFormat;
    _errors = new ParseErrorManager();
    _columnsDerivedFromMap = new TreeMap<String,ResultValueType>();
    _columnsDerivedFromParser = new CellVocabularyParser<ResultValueType>(_columnsDerivedFromMap, _errors);
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
      MetadataParseResult metadataParseResult = parseMetadata(screen,
                                                              workbook, 
                                                              ignoreFilePaths);
      if (_parseLegacyFormat) {
        for (Workbook rawDataWorkbook : metadataParseResult.getRawDataWorkbooks()) {
          try {
            log.info("parsing " + rawDataWorkbook.getWorkbookFile().getAbsolutePath());
            parseData(rawDataWorkbook,
                      metadataParseResult.getScreenResult());
          }
          catch (IOException e) {
            _errors.addError("raw data workbook file " + rawDataWorkbook.getWorkbookFile() + " cannot be read and will be ignored");
            throw e;
          }
        }
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
  private HSSFSheet initializeMetadataSheet(Workbook metadataWorkbook)
    throws UnrecoverableScreenResultParseException
  {
    // find the "Data Headers" sheet, handling legacy format as well
    int dataHeadersSheetIndex;
    if (_parseLegacyFormat) {
      dataHeadersSheetIndex = LEGACY__DATA_HEADERS_SHEET_INDEX;
    }
    else {
      try {
        dataHeadersSheetIndex = metadataWorkbook
          .findSheetIndex(DATA_HEADERS_SHEET_NAME);
      }
      catch (IllegalArgumentException e) {
        throw new UnrecoverableScreenResultParseException(
          DATA_HEADER_SHEET_NOT_FOUND_ERROR,
          metadataWorkbook);
      }
    }
    HSSFSheet metadataSheet = metadataWorkbook
      .getWorkbook()
      .getSheetAt(dataHeadersSheetIndex);

    // at this point, we know we have a valid metadata workbook, to which we can
    // append errors
    _errors.setErrorsWorbook(metadataWorkbook);
    _metadataCellParserFactory = new Cell.Factory(metadataWorkbook,
                                                  dataHeadersSheetIndex,
                                                  _errors);
    _dataHeaderIndex2DataHeaderColumn = new HashMap<Integer,Short>();
    _metadataFirstDataHeaderColumnIndex = findFirstDataHeaderColumnIndex(metadataSheet);
    if (_metadataFirstDataHeaderColumnIndex == null) {
      throw new UnrecoverableScreenResultParseException(
        METADATA_DATA_HEADER_COLUMNS_NOT_FOUND_ERROR,
        metadataWorkbook);
    }
    return metadataSheet;
  }

  /**
   * Finds the index of the first data header column.
   * 
   * @param metadataSheet
   * @throws UnrecoverableScreenResultParseException
   */
  private Short findFirstDataHeaderColumnIndex(HSSFSheet metadataSheet) throws UnrecoverableScreenResultParseException
  {
    if (_parseLegacyFormat) {
      Short metadataFirstDataHeaderColumnIndex = null;
      int row = MetadataRow.COLUMN_TYPE.getRowIndex(_parseLegacyFormat);
      int metadataLastDataHeaderColumnIndex = metadataSheet.getRow(row).getLastCellNum();
      for (short iCol = 0; iCol < metadataLastDataHeaderColumnIndex; ++iCol) {
        Cell cell = _metadataCellParserFactory.getCell(iCol, row);
        String columnType = cell.getString();
        if (columnType != null && columnType.equalsIgnoreCase(DATA_HEADER_COLUMN_TYPE)) {
          metadataFirstDataHeaderColumnIndex = new Short(iCol);
          break;
        }
      }
      return metadataFirstDataHeaderColumnIndex;
    }
    else {
      return (short) METADATA_FIRST_DATA_HEADER_COLUMN_INDEX;
    }
  }
  
  /**
   * Finds the total number of data header columns.
   * 
   * @param metadataSheet
   * @throws UnrecoverableScreenResultParseException
   */
  private int findDataHeaderColumnCount(HSSFSheet metadataSheet) throws UnrecoverableScreenResultParseException
  {
    if (_parseLegacyFormat) {
      int row = MetadataRow.COLUMN_TYPE.getRowIndex(_parseLegacyFormat);
      int metadataLastDataHeaderColumnIndex = metadataSheet.getRow(row).getLastCellNum();
      for (short iCol = _metadataFirstDataHeaderColumnIndex; iCol <= metadataLastDataHeaderColumnIndex; ++iCol) {
        Cell cell = _metadataCellParserFactory.getCell(iCol, row);
        String dataType = cell.getString();
        if (dataType != null && !dataType.equalsIgnoreCase(DATA_HEADER_COLUMN_TYPE)) {
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
      return (metadataLastDataHeaderColumnIndex - _metadataFirstDataHeaderColumnIndex) + 1;
    }
    else {
      return metadataSheet.getRow(MetadataRow.COLUMN_IN_DATA_WORKSHEET.getRowIndex()).getLastCellNum() - 1;
    }
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


  private Cell metadataCell(MetadataRow row, int dataHeader, boolean isRequired) 
  {
    if (!row.isUsed(_parseLegacyFormat)) {
      return _metadataCellParserFactory.getNullCell();
    }
    return _metadataCellParserFactory.getCell((short) (_metadataFirstDataHeaderColumnIndex + dataHeader),
                                              row.getRowIndex(_parseLegacyFormat),
                                              isRequired);
  }
  
  private Cell metadataCell(MetadataRow row, int dataHeader)
  {
    return metadataCell(row, dataHeader, /*required=*/false);
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
                                        
  
  private static class MetadataParseResult
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
   * @param metadataWorkbook
   * @param ignoreFilePaths
   * @throws UnrecoverableScreenResultParseException 
   */
  private MetadataParseResult parseMetadata(
    Screen screen,
    Workbook metadataWorkbook, 
    boolean ignoreFilePaths) 
    throws UnrecoverableScreenResultParseException 
  {
    MetadataParseResult metadataParseResult = new MetadataParseResult();
    HSSFSheet metadataSheet = initializeMetadataSheet(metadataWorkbook);
    if (_parseLegacyFormat) {
      metadataParseResult.setRawDataWorkbooks(parseRawDataWorkbookFilenames(metadataWorkbook,
                                                                            ignoreFilePaths));
    } 
    ParsedScreenInfo parsedScreenInfo = parseScreenInfo(metadataWorkbook, screen);
    
    metadataParseResult.setScreenResult(new ScreenResult(screen, parsedScreenInfo.getDateCreated()));
    int dataHeaderCount = findDataHeaderColumnCount(metadataSheet);
    for (int iDataHeader = 0; iDataHeader < dataHeaderCount; ++iDataHeader) {
      recordDataHeaderColumn(iDataHeader);
      ResultValueType rvt = 
        new ResultValueType(metadataParseResult.getScreenResult(),
                            metadataCell(MetadataRow.NAME, iDataHeader, true).getString(),
                            metadataCell(MetadataRow.REPLICATE, iDataHeader).getInteger(),
                            _rawOrDerivedParser.parse(metadataCell(MetadataRow.RAW_OR_DERIVED, iDataHeader)),
                            _booleanParser.parse(metadataCell(MetadataRow.IS_ASSAY_ACTIVITY_INDICATOR, iDataHeader)),
                            _primaryOrFollowUpParser.parse(metadataCell(MetadataRow.PRIMARY_OR_FOLLOWUP, iDataHeader)),
                            metadataCell(MetadataRow.ASSAY_PHENOTYPE, iDataHeader).getString(),
                            _booleanParser.parse(metadataCell(MetadataRow.IS_CHERRY_PICK, iDataHeader)));
      _columnsDerivedFromMap.put(metadataCell(MetadataRow.COLUMN_IN_DATA_WORKSHEET, iDataHeader, true).getString(), rvt);
      rvt.setDescription(metadataCell(MetadataRow.DESCRIPTION, iDataHeader).getString());
      rvt.setTimePoint(metadataCell(MetadataRow.TIME_POINT, iDataHeader).getString());
      if (rvt.isDerived()) {
        for (ResultValueType resultValueType : _columnsDerivedFromParser.parseList(metadataCell(MetadataRow.COLUMNS_DERIVED_FROM, iDataHeader, true))) {
          if (resultValueType != null) { // can be null if unparseable value is encountered in list
            rvt.addTypeDerivedFrom(resultValueType);
          }
        }
        rvt.setHowDerived(metadataCell(MetadataRow.HOW_DERIVED, iDataHeader, true).getString());
        // TODO: should warn if these values *are* defined and !isDerivedFrom()
      }
      if (rvt.isActivityIndicator()) {
        rvt.setActivityIndicatorType(_activityIndicatorTypeParser.parse(metadataCell(MetadataRow.ACTIVITY_INDICATOR_TYPE, iDataHeader, true)));
        if (rvt.getActivityIndicatorType().equals(ActivityIndicatorType.NUMERICAL)) {
          rvt.setIndicatorDirection(_indicatorDirectionParser.parse(metadataCell(MetadataRow.NUMERICAL_INDICATOR_DIRECTION, iDataHeader)));
          rvt.setIndicatorCutoff(metadataCell(MetadataRow.NUMERICAL_INDICATOR_CUTOFF, iDataHeader).getDouble());
        }
        // TODO: should warn if these values *are* defined and !isActivityIndicator()
      }
      rvt.setComments(metadataCell(MetadataRow.COMMENTS, iDataHeader).getString());
    }
    return metadataParseResult;
  }

  /**
   * @param metadataWorkbook
   * @param ignoreFilePaths
   * @return a List of Workbooks, one for each raw data filename specified; if
   *         no files were specified (indicating that raw data is in the same
   *         workbook), return a one-element list, containing the
   *         <code>metadataWorkbook</code>
   * @throws UnrecoverableScreenResultParseException
   */
  private ArrayList<Workbook> parseRawDataWorkbookFilenames(Workbook metadataWorkbook, boolean ignoreFilePaths)
    throws UnrecoverableScreenResultParseException
  {
    assert _parseLegacyFormat : "only used for legacy format";
    ArrayList<Workbook> rawDataWorkbooksResult = new ArrayList<Workbook>();
    Cell cell = _metadataCellParserFactory.getCell(LEGACY__METADATA_FILENAMES_CELL_COLUMN_INDEX,
                                                   LEGACY__METADATA_FILENAMES_CELL_ROW_INDEX);
    
    String fileNames = cell.getString();
    if (fileNames == null || fileNames.trim().length() == 0) {
      log.info("no raw data files; assuming metadata and raw data in same workbook");
      rawDataWorkbooksResult.add(metadataWorkbook);
    }
    else {
      String[] fileNamesArray = fileNames.split(FILENAMES_LIST_DELIMITER);
      for (int i = 0; i < fileNamesArray.length; i++) {
        
        File rawDataWorkbookFile = new File(fileNamesArray[i]);
        if (ignoreFilePaths && rawDataWorkbookFile.isAbsolute()) {
          log.info("ignoring absolute file path for raw data file (assuming it is in same directory as metadata file)");
          rawDataWorkbookFile = 
            makeRawDataFileInMetadataFileDirectory(rawDataWorkbookFile,
                                                   metadataWorkbook.getWorkbookFile());
        } 
        else if (!rawDataWorkbookFile.isAbsolute()) {
          rawDataWorkbookFile = 
            makeRawDataFileInMetadataFileDirectory(rawDataWorkbookFile,
                                                   metadataWorkbook.getWorkbookFile());
        }
        rawDataWorkbooksResult.add(new Workbook(rawDataWorkbookFile, _errors));
      }
    }
    return rawDataWorkbooksResult;
  }

  private File makeRawDataFileInMetadataFileDirectory(File rawDataWorkbookFile, File metadataWorkbookFile)
  {
    rawDataWorkbookFile = new File(metadataWorkbookFile.getParent(),
                                   rawDataWorkbookFile.getName());
    return rawDataWorkbookFile;
  }

  private void recordDataHeaderColumn(int iDataHeader)
  {
    assert _dataHeaderIndex2DataHeaderColumn != null :
      "uninitialized _dataHeaderIndex2DataHeaderColumn";
    String forColumnInRawDataWorksheet = metadataCell(MetadataRow.COLUMN_IN_DATA_WORKSHEET, iDataHeader, true).getString();
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
   * @throws IOException 
   */
  private void parseData(
    Workbook workbook,
    ScreenResult screenResult) 
    throws ExtantLibraryException, IOException
  {
    int dataSheetsParsed = 0;
    for (int iSheet = 0; iSheet < workbook.getWorkbook().getNumberOfSheets(); ++iSheet) {
      HSSFSheet sheet = initializeDataSheet(workbook, iSheet);
      if (isActiveSheetRawDataSheet()) {
        ++dataSheetsParsed;
        log.info("parsing sheet " + workbook.getWorkbookFile().getName() + ":" + workbook.getWorkbook().getSheetName(iSheet));
        for (int iRow = RAWDATA_FIRST_DATA_ROW_INDEX; iRow <= sheet.getLastRowNum(); ++iRow) {
          Well well = findWell(iRow);
          AssayWellType assayWellType = _assayWellTypeParser.parse(dataCell(iRow, DataColumn.ASSAY_WELL_TYPE));
          
          List<ResultValueType> wellExcludes = new ExcludeParser(screenResult).parseList(dataCell(iRow, DataColumn.EXCLUDE));
          
          int iDataHeader = 0;
          for (ResultValueType rvt : screenResult.getResultValueTypes()) {
            Cell cell = dataCell(iRow, iDataHeader);
            Object value = !rvt.isActivityIndicator()
            ? cell.getAsString()
              : rvt.getActivityIndicatorType() == ActivityIndicatorType.BOOLEAN
              ? _booleanParser.parse(cell)
                : rvt.getActivityIndicatorType() == ActivityIndicatorType.NUMERICAL
                ? cell.getDouble()
                  : rvt.getActivityIndicatorType() == ActivityIndicatorType.PARTITION
                  ? _partitionedValueParser.parse(cell) 
                    : cell.getString();
                  if (value == null) {
                    value = "";
                  }
                  new ResultValue(rvt, 
                                  well, 
                                  assayWellType,
                                  value.toString(),
                                  (wellExcludes != null && wellExcludes.contains(rvt)));
                  ++iDataHeader;
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
    String stockPlateColumnName = dataCell(RAWDATA_HEADER_ROW_INDEX, DataColumn.STOCK_PLATE_ID).getAsString();
    if (_parseLegacyFormat) {
      return stockPlateColumnName.equals(LEGACY__DATA_SHEET__STOCK_PLATE_COLUMN_NAME);
    }
    else {
      return stockPlateColumnName.equals(DATA_SHEET__STOCK_PLATE_COLUMN_NAME);
    }
  }

  private Well findWell(int iRow) throws ExtantLibraryException
  {
    Integer plateNumber = _plateNumberParser.parse(dataCell(
      iRow,
      DataColumn.STOCK_PLATE_ID,
      true));
    String wellName = _wellNameParser.parse(dataCell(
      iRow,
      DataColumn.WELL_NAME,
      true));
    Well well = _dao.findWell(plateNumber, wellName);
    
    if (well == null) {
      Library library = _dao.findLibraryWithPlate(plateNumber);
      if (library == null) {
        throw new ExtantLibraryException(
          "no library exists that contains plate " + plateNumber);
      }
      well = new Well(library, plateNumber, wellName);
    }
    else {

      // if we didnt find one well (well == null above), then the library probably
      // has none, so no point in forcing the wells to load. -s
      
      // force Hibernate to load all well for this library now
      Hibernate.initialize(well.getLibrary().getWells());
    }

    return well;
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
   * Parses the "Screen Info" worksheet (was "meta" in legacy format). Yes, we
   * actually had a tab called "meta" in a workbook file that itself contains
   * metadata (data header definitions) for our ScreenResult metadata; thus the
   * double-meta prefix. Life is complex.
   * <p>
   * For now, we just parse the ScreenResult date, but if we ever need to parse
   * more, we should return a composite object, rather than just a Date.
   * @throws UnrecoverableScreenResultParseException if a screen ID is not found
   */
  private ParsedScreenInfo parseScreenInfo(Workbook metadataWorkbook, Screen screen) 
    throws UnrecoverableScreenResultParseException
  {
    ParsedScreenInfo parsedScreenInfo = new ParsedScreenInfo();
    int metaMetaSheetIndex;
    if (_parseLegacyFormat) {
      metaMetaSheetIndex = metadataWorkbook.findSheetIndex(LEGACY__METADATA_META_SHEET_NAME);
    } 
    else {
      metaMetaSheetIndex = metadataWorkbook.findSheetIndex(SCREEN_INFO_SHEET_NAME);
    }
    HSSFSheet metametaSheet = metadataWorkbook.getWorkbook()
                                               .getSheetAt(metaMetaSheetIndex);
    Cell.Factory factory = new Cell.Factory(metadataWorkbook,
                                            metaMetaSheetIndex,
                                            _errors);
    if (metametaSheet != null) {
      for (int iRow = metametaSheet.getFirstRowNum(); iRow < metametaSheet.getLastRowNum(); iRow++) {
        Cell labelCell = factory.getCell((short) 0, iRow);
        String rowLabel = labelCell.getString();
        if (rowLabel != null) {
          Cell valueCell = factory.getCell((short) 1, iRow, true);
          if (rowLabel.equalsIgnoreCase(FIRST_DATE_SCREENED)) {
            parsedScreenInfo.setDate(valueCell.getDate());
          }
          else if (rowLabel.equalsIgnoreCase(SCREEN_ID_LABEL)) {
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
      _errors.addError(NO_CREATED_DATE_FOUND_ERROR);
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
  

  /**
   * Parses the value of a cell containing a "well name". Validates that the
   * well name follows proper syntax, defined by the regex "[A-Z]\d\d".
   * 
   * @author ant
   */
  public class ExcludeParser implements CellValueParser<ResultValueType>
  {
    private ScreenResult _screenResult;
    private Pattern columnIdPattern = Pattern.compile("[A-Z]");
    

    // public methods
    
    public ExcludeParser(ScreenResult screenResult)
    {
      _screenResult = screenResult;
    }

    public ResultValueType parse(Cell cell) 
    {
      throw new UnsupportedOperationException();
    }

    public List<ResultValueType> parseList(Cell cell)
    {
      String textMultiValue = cell.getString();
      List<ResultValueType> result = new ArrayList<ResultValueType>();
      List<ResultValueType> excludeAllResult = makeRawResultValueTypeList(_screenResult);

      if (_parseLegacyFormat) {
        if (textMultiValue != null &&
          textMultiValue.trim().length() > 0) {
          return excludeAllResult;
        }
        return result;
      }
      
      if (textMultiValue != null && 
        textMultiValue.equalsIgnoreCase(ScreenResultWorkbookSpecification.EXCLUDE_ALL_VALUE)) {
        return excludeAllResult;
      }
      
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
            "' (expected one of " + _columnsDerivedFromMap.keySet() + ")", 
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
      char parsedValue = matcher.group(0).charAt(0);
      int rvtOrdinal = parsedValue - ScreenResultWorkbookSpecification.RAWDATA_FIRST_DATA_HEADER_COLUMN_LABEL;
      if (rvtOrdinal < 0 || rvtOrdinal >= _screenResult.getResultValueTypes().size() ) {
        // can't report error since legacy format uses *any* non-null value to denote "exclude"=true
        //_errors.addError("unparseable Exclude value '" + value + "'", cell)
        return null;
      }
      return _screenResult.getResultValueTypesList().get(rvtOrdinal);
    }
    
    private List<ResultValueType> makeRawResultValueTypeList(ScreenResult screenResult) 
    {
      List<ResultValueType> result = new ArrayList<ResultValueType>();
      for (Iterator iter = screenResult.getResultValueTypes().iterator(); iter.hasNext();) {
        ResultValueType rvt = (ResultValueType) iter.next();
        if (!rvt.isDerived()) {
          result.add(rvt);
        }
      }
      return result;
    }
    
  }
}
