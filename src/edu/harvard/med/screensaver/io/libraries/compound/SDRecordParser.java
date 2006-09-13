// $HeadURL: svn+ssh://js163@orchestra/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries.compound;

import java.io.BufferedReader;
import java.io.IOException;

import org.apache.log4j.Logger;

class SDRecordParser
{

  // private static data
  
  private static final Logger log = Logger.getLogger(SDRecordParser.class);

  
  // private instance data
  
  private BufferedReader _sdFileReader;
  private SDFileCompoundLibraryContentsParser _libraryContentsParser;
  private String _nextLine;
  private int _sdRecordNumber = 0;
  
  
  // package-private constructor and instance methods
  
  /**
   * Construct a new <code>SDRecordParser</code> object.
   * @param sdFileReader the <code>BufferedReader</code> for the SDFile
   * @param libraryContentsParser the parent SDFile compound library contents
   * parser
   */
  SDRecordParser(
    BufferedReader sdFileReader,
    SDFileCompoundLibraryContentsParser libraryContentsParser)
  {
    _sdFileReader = sdFileReader;
    _libraryContentsParser = libraryContentsParser;
    prepareNextRecord();
  }
  
  /**
   * @return
   */
  boolean sdFileHasMoreRecords()
  {
    return _nextLine != null;
  }
  
  /**
   * Parse an SD record from the SDFile.
   */
  void parseSDRecord()
  {
    SDRecordData recordData = gatherSDRecordData();

    // TODO: do something with the record data
    log.info("record data = " + recordData);

    prepareNextRecord();
  }
  
  
  // private instance methods
  
  private void prepareNextRecord()
  {
    _sdRecordNumber ++;
    _nextLine = readNextLine();
  }

  /**
   * 
   */
  private String readNextLine() {
    try {
      return _sdFileReader.readLine();
    }
    catch (IOException e) {
      log.error(e, e);
      _libraryContentsParser.getErrorManager().addError(
        "encountered an IOException reading SDFile: " + e.getMessage(),
        _libraryContentsParser.getSdFile(),
        _sdRecordNumber);
      return null;
    }
  }
  
  private SDRecordData gatherSDRecordData()
  {
    StringBuffer molfile = new StringBuffer();
    String line = _nextLine;
    while (! line.equals("M  END")) {
      molfile.append(line).append('\n');
      line = readNextLine();
    }
    SDRecordData recordData = new SDRecordData();
    recordData.setMolfile(new String(molfile));
    while (! line.equals("$$$$")) {
      
      if (line.matches("^>  <.*>(\\s+\\(.*\\))?")) {
        String header = line.substring(4, line.indexOf('>', 4));
        line = readNextLine();
        
        if (header.equals("Plate")) {
          try {
            recordData.setPlateNumber(Integer.parseInt(line));
          }
          catch (NumberFormatException e) {
            _libraryContentsParser.getErrorManager().addError(
              "Plate specified was not a number",
              _libraryContentsParser.getSdFile(),
              _sdRecordNumber);
          }
        }
        else if (header.equals("Well")) {
          recordData.setWellName(line);
        }
        else if (header.equals("ICCB_NUM") || header.equals("ICCB_Num")) {
          recordData.setIccbNumber(line);
        }
        else if (header.equals("CAS_Number") || header.equals("CAS_number")) {
          recordData.setIccbNumber(line);
        }
        else if (header.equals("Vendor_ID")) {
          recordData.setVendorIdentifier(line);
        }
        else if (
          header.equals("compound_identifier") ||
          header.equals("CompoundName") ||
          header.equals("ChemicalName") ||
          header.equals("Chemical_Name")) {
          recordData.setCompoundName(line);
        }
      }
      
      line = readNextLine();
    }
    return recordData;
  }
}
