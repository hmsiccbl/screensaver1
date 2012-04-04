package edu.harvard.med.screensaver.io.workbook2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.math.IntRange;
import org.apache.log4j.Logger;

import com.google.common.collect.Maps;

import edu.harvard.med.screensaver.io.ParseException;
import edu.harvard.med.screensaver.io.libraries.rnai.WorkbookLibraryContentsParser;
import edu.harvard.med.screensaver.util.AlphabeticCounter;
import edu.harvard.med.screensaver.util.CSVPrintWriter;
import edu.harvard.med.screensaver.util.StringUtils;

/**
 * Parse the workbook as a temporary CSV file, for memory performance<br/>
   *<br/> <b>NOTE:</b> writing the workbook contents to the file system and reading it back may not be a commutative process!  It has been observed to cause unwanted effects due to encoding issues.<br/>
   * Test carefully if using this utility.<br/>
 */
public class WorkbookAsCsvParser {
  private static final Logger log = Logger.getLogger(WorkbookLibraryContentsParser.class);

  private static final String FIELD_DELIMITER = "~~";
  public static final int DEFAULT_FIRST_DATA_ROW = 1;
  private BufferedReader _reader;
  private int _lineNumber;

  private Map<IntRange,String> _worksheetRecordRanges;
	
  public WorkbookAsCsvParser(File file) throws IOException
  {
  	convert(file, DEFAULT_FIRST_DATA_ROW);
  } 
  
  /**
   *<br/> <b>NOTE:</b> writing the workbook contents to the file system and reading it back may not be a commutative process!  It has been observed to cause unwanted effects due to encoding issues.<br/>
   * Test carefully if using this utility.<br/>
   * @param file
   * @param startRowFromZero the first row of the worksheet to read (start from zero); to be used if it is desired to skip the header row, for instance
   * @throws IOException
   */
  public WorkbookAsCsvParser(File file, int startRowFromZero) throws IOException
  {
  	convert(file, startRowFromZero);
  }
	private void convert(File file, int startRow) throws IOException
  {
    log.debug("converting workbook into csv file...");
    Workbook _workbook = new Workbook(file);
    File tmpFile = File.createTempFile("workbook", ".csv");
    tmpFile.deleteOnExit();
    CSVPrintWriter writer = new CSVPrintWriter(new BufferedWriter(new FileWriter(tmpFile)), "\n", FIELD_DELIMITER);
    Iterator<Worksheet> worksheetsIterator = _workbook.iterator();
    int prevSheetLastRecord = 0;
    int record = 0;
    _worksheetRecordRanges = Maps.newHashMap();
    while (worksheetsIterator.hasNext()) {
      Worksheet worksheet = worksheetsIterator.next().forOrigin(0, startRow);
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
	
  public String[] parseNext() throws ParseException, IOException
  {
    String line = _reader.readLine();
    ++_lineNumber;
    if (StringUtils.isEmpty(line)) {
      return null;
    }
    String[] fields = line.split(FIELD_DELIMITER);
    if(fields == null || fields.length == 0 ) { //|| StringUtils.isEmpty(fields[0]) ) {
    	return null;
    }
    return  fields;
  }
  
  public int getLineNumber() { return _lineNumber; }

  /**
   * For error reporting; will determine the cell location in the original worksheet
   * @param lineNumber
   * @param columnFromZero
   * @return
   */
  public String makeWorkbookCellLocation(int lineNumber, int columnFromZero)
  {
    try {
      for (Map.Entry<IntRange,String> worksheetRecordRangeEntry : _worksheetRecordRanges.entrySet()) {
        if (worksheetRecordRangeEntry.getKey().containsInteger(lineNumber)) {
          String cellColumn = AlphabeticCounter.toLabel(columnFromZero);
          int cellRow = lineNumber - worksheetRecordRangeEntry.getKey().getMinimumInteger() + 1;
          return worksheetRecordRangeEntry.getValue() + ":(" + cellColumn + "," + cellRow + ")";
        }
      }
    } 
    catch (Exception e) {}
    return "<workbook>";
  }
}
