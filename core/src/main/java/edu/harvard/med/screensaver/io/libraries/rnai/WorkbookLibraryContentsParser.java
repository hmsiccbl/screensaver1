// $HeadURL:
// http://forge.abcd.harvard.edu/svn/screensaver/branches/iccbl/library-mgmt-rework/src/edu/harvard/med/screensaver/io/libraries/rnai/RNAiLibraryContentsParser.java
// $
// $Id$

// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries.rnai;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.io.ParseError;
import edu.harvard.med.screensaver.io.ParseException;
import edu.harvard.med.screensaver.io.libraries.LibraryContentsParser;
import edu.harvard.med.screensaver.io.workbook2.Cell;
import edu.harvard.med.screensaver.io.workbook2.Row;
import edu.harvard.med.screensaver.io.workbook2.Workbook;
import edu.harvard.med.screensaver.io.workbook2.Worksheet;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.util.AlphabeticCounter;
import edu.harvard.med.screensaver.util.CSVPrintWriter;
import edu.harvard.med.screensaver.util.Pair;
import edu.harvard.med.screensaver.util.StringUtils;

import org.apache.commons.lang.math.IntRange;
import org.apache.log4j.Logger;

import com.google.common.collect.Maps;

/**
 * This class accomplishes both:
 * <ul>
 * <li>Parsing of the library input file, and
 * <li>Loading of the data into Library and related domain model objects on the
 * database.
 */
abstract public class WorkbookLibraryContentsParser<R extends Reagent> extends LibraryContentsParser<R>
{
  private static final Logger log = Logger.getLogger(WorkbookLibraryContentsParser.class);
  
  private static final String FIELD_DELIMITER = "~~";
  public static final int FIRST_DATA_ROW = 1;
  
  protected final Pair<Well,R> EMPTY_PARSE_RESULT = new Pair<Well,R>(null, null);

  private BufferedReader _reader;
  private int _lineNumber;

  private Map<IntRange,String> _worksheetRecordRanges;

  public WorkbookLibraryContentsParser(GenericEntityDAO dao, InputStream stream, Library library)
  {
    super(dao, stream, library);
  }

  public void convert() throws IOException
  {
    log.debug("converting workbook into csv file...");
    Workbook _workbook = new Workbook("Library Contents Input Stream", getStream());
    File tmpFile = File.createTempFile("workbook", ".csv");
    tmpFile.deleteOnExit();
    CSVPrintWriter writer = new CSVPrintWriter(new BufferedWriter(new FileWriter(tmpFile)), "\n", FIELD_DELIMITER);
    Iterator<Worksheet> worksheetsIterator = _workbook.iterator();
    int prevSheetLastRecord = 0;
    int record = 0;
    _worksheetRecordRanges = Maps.newHashMap();
    while (worksheetsIterator.hasNext()) {
      Worksheet worksheet = worksheetsIterator.next().forOrigin(0, FIRST_DATA_ROW);
      Iterator<Row> rowsIterator = worksheet.iterator();
      while (rowsIterator.hasNext()) {
        for (Cell cell : rowsIterator.next()) {
          writer.print(cell.getAsString());
        }
        writer.println();
        ++record;
      }
      IntRange worksheetRecordRange = new IntRange(prevSheetLastRecord, record);
      _worksheetRecordRanges.put(worksheetRecordRange, worksheet.getName());
      prevSheetLastRecord = record;
    }
    writer.close();
    _reader = new BufferedReader(new FileReader(tmpFile)); 
    log.debug("done converting workbook into csv file");
  }

  public Pair<Well,R> parseNext() throws ParseException, IOException
  {
    if (_reader == null) {
      convert();
    }
    String line = _reader.readLine();
    ++_lineNumber;
    if (StringUtils.isEmpty(line)) {
      return null;
    }
    String[] values = line.split(FIELD_DELIMITER);
    try {
      return parse(values);
    }
    catch (ParseException e) {
      throw new ParseException(new ParseError(e.getError().getErrorMessage(),
                                              makeWorkbookCellLocation(_lineNumber, e.getError().getErrorLocation())));
    }
    catch (RuntimeException e) {
      // in case subclass throws a RuntimeException, instead of a ParseException
      throw new ParseException(new ParseError(e.toString(), _lineNumber));
    }
  }

  private String makeWorkbookCellLocation(int recordIndex, Object errorLocation)
  {
    try {
      for (Map.Entry<IntRange,String> worksheetRecordRangeEntry : _worksheetRecordRanges.entrySet()) {
        if (worksheetRecordRangeEntry.getKey().containsInteger(recordIndex)) {
          String cellColumn = AlphabeticCounter.toLabel(Integer.parseInt(errorLocation.toString()) - 1);
          int cellRow = recordIndex - worksheetRecordRangeEntry.getKey().getMinimumInteger() + 1;
          return worksheetRecordRangeEntry.getValue() + ":(" + cellColumn + "," + cellRow + ")";
        }
      }
    } 
    catch (Exception e) {}
    return "<workbook>";
  }

  abstract protected Pair<Well,R> parse(String[] values) throws ParseException;
}
